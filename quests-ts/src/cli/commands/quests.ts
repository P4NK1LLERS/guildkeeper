import type { Command } from '../parser/registry.ts';
import { CliError } from '../errors.ts';
import { renderQuest, renderQuestList } from '../render/render.ts';

export const questsListCommand: Command = {
  path: ['quests', 'list'],
  summary: 'Liste le catalogue de quetes',
  positionals: [],
  flags: { json: 'boolean' },
  async run(ctx, args) {
    const quests = await ctx.client.quests.list();
    ctx.out(args.flags.has('json') ? json(quests) : renderQuestList(quests));
  },
};

export const questsShowCommand: Command = {
  path: ['quests', 'show'],
  summary: 'Affiche une quete par identifiant',
  positionals: [{ name: 'id' }],
  flags: { json: 'boolean' },
  async run(ctx, args) {
    const id = args.positionals[0];
    if (id === undefined || id.trim() === '') {
      throw new CliError('Identifiant de quete manquant.');
    }
    const quest = await ctx.client.quests.get(id);
    ctx.out(args.flags.has('json') ? json(quest) : renderQuest(quest));
  },
};

function json(value: unknown): string {
  return JSON.stringify(value, null, 2);
}
