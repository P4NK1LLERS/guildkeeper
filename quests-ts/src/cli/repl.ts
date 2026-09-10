import * as readline from 'node:readline';
import { dispatch } from './parser/dispatch.ts';
import { tokenize } from './parser/tokenize.ts';
import { CliError } from './errors.ts';
import type { CommandContext } from './parser/registry.ts';

const EXIT_WORDS = new Set(['exit', 'quit', 'q']);

/**
 * Traite une ligne du REPL. Extrait pour etre testable sans `readline`.
 * Le REPL est **sans etat** : chaque ligne repart de zero.
 */
export async function handleReplLine(
  line: string,
  ctx: CommandContext,
): Promise<'continue' | 'exit'> {
  const trimmed = line.trim();
  if (trimmed === '') {
    return 'continue';
  }
  if (EXIT_WORDS.has(trimmed)) {
    return 'exit';
  }

  let tokens: string[];
  try {
    tokens = tokenize(trimmed);
  } catch (error) {
    ctx.err(error instanceof CliError ? `Erreur : ${error.message}` : String(error));
    return 'continue';
  }

  await dispatch(tokens, ctx);
  return 'continue';
}

export function runRepl(
  ctx: CommandContext,
  io: { input: NodeJS.ReadableStream; output: NodeJS.WritableStream },
): Promise<void> {
  return new Promise((resolve) => {
    const rl = readline.createInterface({
      input: io.input,
      output: io.output,
      prompt: 'guildkeeper> ',
    });
    let finished = false;
    const finish = (): void => {
      if (finished) {
        return;
      }
      finished = true;
      rl.close();
      resolve();
    };

    ctx.out("GuildKeeper — mode interactif. « help » pour l'aide, « exit » pour quitter.");
    rl.prompt();

    rl.on('line', (line) => {
      rl.pause();
      handleReplLine(line, ctx)
        .then((result) => {
          if (result === 'exit' || finished) {
            finish();
            return;
          }
          rl.resume();
          rl.prompt();
        })
        .catch(() => finish());
    });
    rl.on('close', finish);
  });
}
