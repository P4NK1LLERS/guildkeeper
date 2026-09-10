import { CliError } from '../errors.ts';

/**
 * Decoupe une ligne (mode REPL) en tokens facon shell : les espaces separent,
 * les guillemets simples ou doubles regroupent. Le shell fait deja ce travail
 * pour `process.argv` — cette fonction n'est utile qu'au REPL.
 *
 * Volontairement minimal : pas d'echappement `\`, pas d'expansion de variables.
 */
export function tokenize(line: string): string[] {
  const tokens: string[] = [];
  let current = '';
  let quote: '"' | "'" | null = null;
  let started = false;

  for (const char of line) {
    if (quote) {
      if (char === quote) {
        quote = null;
      } else {
        current += char;
      }
      continue;
    }
    if (char === '"' || char === "'") {
      quote = char;
      started = true;
      continue;
    }
    if (char === ' ' || char === '\t') {
      if (started) {
        tokens.push(current);
        current = '';
        started = false;
      }
      continue;
    }
    current += char;
    started = true;
  }

  if (quote) {
    throw new CliError(`Guillemet ${quote} non ferme.`);
  }
  if (started) {
    tokens.push(current);
  }
  return tokens;
}
