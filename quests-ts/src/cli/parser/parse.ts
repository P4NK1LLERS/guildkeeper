/**
 * Parseur d'arguments maison. Pur : `tokens -> resultat`, aucune I/O.
 *
 * Gere : `--flag valeur` et `--flag=valeur`, `--` (fin des options), options
 * repetables, `--help` / `-h` a chaque niveau, commande inconnue -> suggestion.
 * Ne gere PAS : abreviations, completion, binding d'environnement, fichier de
 * config.
 */

export type FlagKind = 'boolean' | 'value' | 'values';

export interface PositionalSpec {
  name: string;
  optional?: boolean;
}

export interface CommandSpec {
  /** Chemin de la commande, ex. `['quests', 'list']` ou `['report']`. */
  path: string[];
  summary: string;
  positionals: PositionalSpec[];
  flags: Record<string, FlagKind>;
}

export interface FlagValues {
  has(name: string): boolean;
  /** Valeur d'une option `value` (la premiere si repetee), sinon `undefined`. */
  value(name: string): string | undefined;
  /** Toutes les valeurs d'une option `values`. */
  values(name: string): string[];
}

export type ParseOutcome<S extends CommandSpec = CommandSpec> =
  | { kind: 'command'; spec: S; positionals: string[]; flags: FlagValues }
  | { kind: 'help'; spec?: S }
  | { kind: 'error'; message: string };

export function parse<S extends CommandSpec>(
  tokens: string[],
  specs: readonly S[],
): ParseOutcome<S> {
  // `help [commande...]`
  if (tokens[0] === 'help') {
    const words = tokens.slice(1).filter((token) => !token.startsWith('-'));
    if (words.length === 0) {
      return { kind: 'help' };
    }
    const spec = matchSpec(words, specs);
    return spec ? { kind: 'help', spec } : { kind: 'error', message: unknownCommand(words, specs) };
  }

  const spec = matchSpec(tokens, specs);
  if (!spec) {
    const words = leadingWords(tokens);
    if (words.length === 0) {
      return { kind: 'help' };
    }
    return { kind: 'error', message: unknownCommand(words, specs) };
  }

  const rest = tokens.slice(spec.path.length);
  if (rest.includes('--help') || rest.includes('-h')) {
    return { kind: 'help', spec };
  }

  const positionals: string[] = [];
  const store = new Map<string, string[] | true>();
  let optionsDone = false;

  for (let i = 0; i < rest.length; i += 1) {
    const token = rest[i]!;

    if (optionsDone || !token.startsWith('--')) {
      positionals.push(token);
      continue;
    }
    if (token === '--') {
      optionsDone = true;
      continue;
    }

    const eq = token.indexOf('=');
    const name = eq >= 0 ? token.slice(2, eq) : token.slice(2);
    const inlineValue = eq >= 0 ? token.slice(eq + 1) : undefined;
    const kind = spec.flags[name];

    if (kind === undefined) {
      return { kind: 'error', message: unknownFlag(name, spec) };
    }
    if (kind === 'boolean') {
      if (inlineValue !== undefined) {
        return { kind: 'error', message: `L'option --${name} ne prend pas de valeur.` };
      }
      store.set(name, true);
      continue;
    }

    let value: string;
    if (inlineValue !== undefined) {
      value = inlineValue;
    } else {
      const next = rest[i + 1];
      if (next === undefined || next === '--') {
        return { kind: 'error', message: `L'option --${name} attend une valeur.` };
      }
      value = next;
      i += 1;
    }

    if (kind === 'values') {
      const existing = (store.get(name) as string[] | undefined) ?? [];
      existing.push(value);
      store.set(name, existing);
    } else {
      store.set(name, [value]);
    }
  }

  const required = spec.positionals.filter((positional) => !positional.optional).length;
  if (positionals.length < required) {
    return { kind: 'error', message: `Argument manquant. ${usageLine(spec)}` };
  }
  if (positionals.length > spec.positionals.length) {
    return { kind: 'error', message: `Trop d'arguments. ${usageLine(spec)}` };
  }

  const flags: FlagValues = {
    has: (name) => store.has(name),
    value: (name) => {
      const entry = store.get(name);
      return Array.isArray(entry) ? entry[0] : undefined;
    },
    values: (name) => {
      const entry = store.get(name);
      return Array.isArray(entry) ? entry : [];
    },
  };

  return { kind: 'command', spec, positionals, flags };
}

export function usageLine(spec: CommandSpec): string {
  const parts = ['guildkeeper', ...spec.path];
  for (const positional of spec.positionals) {
    parts.push(positional.optional ? `[<${positional.name}>]` : `<${positional.name}>`);
  }
  for (const [name, kind] of Object.entries(spec.flags)) {
    parts.push(kind === 'boolean' ? `[--${name}]` : `[--${name} <valeur>]`);
  }
  return `Usage : ${parts.join(' ')}`;
}

function leadingWords(tokens: string[]): string[] {
  const words: string[] = [];
  for (const token of tokens) {
    if (token.startsWith('-')) {
      break;
    }
    words.push(token);
  }
  return words;
}

function matchSpec<S extends CommandSpec>(tokens: string[], specs: readonly S[]): S | undefined {
  const words = leadingWords(tokens);
  let best: S | undefined;
  for (const spec of specs) {
    const isPrefix =
      spec.path.length <= words.length && spec.path.every((segment, i) => segment === words[i]);
    if (isPrefix && (!best || spec.path.length > best.path.length)) {
      best = spec;
    }
  }
  return best;
}

function unknownCommand(words: string[], specs: readonly CommandSpec[]): string {
  const attempted = words.join(' ');
  const names = specs.map((spec) => spec.path.join(' '));
  const [closest, distance] = names
    .map((name) => [name, levenshtein(attempted, name)] as const)
    .sort((a, b) => a[1] - b[1])[0] ?? ['', Infinity];
  const hint =
    distance <= Math.max(2, Math.ceil(attempted.length / 3))
      ? ` Vouliez-vous dire « ${closest} » ?`
      : '';
  return `Commande inconnue : « ${attempted} ».${hint} Tapez « help » pour la liste.`;
}

function unknownFlag(name: string, spec: CommandSpec): string {
  const names = Object.keys(spec.flags);
  const [closest, distance] = names
    .map((flag) => [flag, levenshtein(name, flag)] as const)
    .sort((a, b) => a[1] - b[1])[0] ?? ['', Infinity];
  const hint = distance <= 2 ? ` Vouliez-vous dire --${closest} ?` : '';
  return `Option inconnue : --${name}.${hint} ${usageLine(spec)}`;
}

function levenshtein(a: string, b: string): number {
  const rows = a.length + 1;
  const cols = b.length + 1;
  const distances: number[] = Array.from({ length: cols }, (_, i) => i);
  for (let i = 1; i < rows; i += 1) {
    let previous = distances[0]!;
    distances[0] = i;
    for (let j = 1; j < cols; j += 1) {
      const temp = distances[j]!;
      distances[j] =
        a[i - 1] === b[j - 1] ? previous : 1 + Math.min(previous, distances[j]!, distances[j - 1]!);
      previous = temp;
    }
  }
  return distances[cols - 1]!;
}
