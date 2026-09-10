import { commands } from '../commands/index.ts';
import { CliError } from '../errors.ts';
import { parse } from './parse.ts';
import { renderHelp, type Command, type CommandContext } from './registry.ts';

/**
 * Analyse `tokens`, trouve la commande, l'execute. Renvoie le code de sortie :
 * 0 succes, 2 erreur d'usage, 1 erreur d'execution.
 */
export async function dispatch(tokens: string[], ctx: CommandContext): Promise<number> {
  const outcome = parse(tokens, commands);

  if (outcome.kind === 'error') {
    ctx.err(outcome.message);
    return 2;
  }
  if (outcome.kind === 'help') {
    ctx.out(renderHelp(commands, outcome.spec));
    return 0;
  }

  const command: Command = outcome.spec;
  try {
    await command.run(ctx, { positionals: outcome.positionals, flags: outcome.flags });
    return 0;
  } catch (error) {
    if (error instanceof CliError) {
      ctx.err(`Erreur : ${error.message}`);
      return error.exitCode;
    }
    ctx.err(`Erreur : ${error instanceof Error ? error.message : String(error)}`);
    return 1;
  }
}
