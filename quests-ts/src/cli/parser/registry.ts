import type { GuildKeeperClient } from '../../client/guildKeeperClient.ts';
import type { CommandSpec, FlagValues } from './parse.ts';
import { usageLine } from './parse.ts';

/** Ce dont une commande a besoin pour s'executer : le client et les sorties. */
export interface CommandContext {
  client: GuildKeeperClient;
  out(line: string): void;
  err(line: string): void;
}

export interface CommandArgs {
  positionals: string[];
  flags: FlagValues;
}

export interface Command extends CommandSpec {
  run(ctx: CommandContext, args: CommandArgs): Promise<void>;
}

/** Aide generale (sans `spec`) ou detaillee pour une commande. */
export function renderHelp(commands: readonly Command[], spec?: CommandSpec): string {
  if (spec) {
    const flagLines = Object.entries(spec.flags).map(
      ([name, kind]) =>
        `  --${name}${kind === 'boolean' ? '' : ' <valeur>'}${kind === 'values' ? ' (repetable)' : ''}`,
    );
    return [spec.summary, '', usageLine(spec), ...flagLines].join('\n');
  }

  const width = Math.max(...commands.map((command) => command.path.join(' ').length));
  const lines = commands.map(
    (command) => `  ${command.path.join(' ').padEnd(width)}  ${command.summary}`,
  );
  return [
    'GuildKeeper — client de progression',
    '',
    'Usage : guildkeeper <commande> [arguments]',
    '        guildkeeper            (sans argument : mode interactif)',
    '',
    'Commandes :',
    ...lines,
    '',
    'Ajoutez --json a une commande de lecture pour une sortie machine.',
    "L'URL de l'API vient de GUILDKEEPER_API_URL (defaut http://localhost:7070).",
  ].join('\n');
}
