import process from 'node:process';
import { pathToFileURL } from 'node:url';
import { createGuildKeeperClient } from '../client/guildKeeperClient.ts';
import { dispatch } from './parser/dispatch.ts';
import { runRepl } from './repl.ts';
import type { CommandContext } from './parser/registry.ts';

/**
 * Point d'entree du CLI. Seul endroit qui touche a l'I/O : `process.argv`, les
 * flux standard, et la resolution de l'URL de l'API.
 */

const DEFAULT_API_URL = 'http://localhost:7070';

export async function main(argv: string[]): Promise<number> {
  const baseUrl = process.env.GUILDKEEPER_API_URL ?? DEFAULT_API_URL;
  const ctx: CommandContext = {
    client: createGuildKeeperClient({ baseUrl }),
    out: (line) => process.stdout.write(`${line}\n`),
    err: (line) => process.stderr.write(`${line}\n`),
  };

  if (argv.length === 0) {
    await runRepl(ctx, { input: process.stdin, output: process.stdout });
    return 0;
  }
  return dispatch(argv, ctx);
}

const invokedDirectly =
  process.argv[1] !== undefined && import.meta.url === pathToFileURL(process.argv[1]).href;

if (invokedDirectly) {
  const argv = process.argv.slice(2);
  main(argv)
    .then((code) => {
      if (argv.length === 0) {
        // Mode interactif : laisser la boucle d'evenements se vider
        // (ne pas arracher stdin, cf. assertion libuv sous Windows).
        process.exitCode = code;
      } else {
        process.exit(code);
      }
    })
    .catch((error: unknown) => {
      process.stderr.write(
        `${error instanceof Error ? (error.stack ?? error.message) : String(error)}\n`,
      );
      process.exit(1);
    });
}
