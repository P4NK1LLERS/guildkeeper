import type { Command } from '../parser/registry.ts';
import { CliError } from '../errors.ts';
import { findQuestById, findQuestByTitle, isQuestUnlocked } from '../../planner/unlockTree.ts';
import { renderUnlockStatus } from '../render/render.ts';

export const unlockStatusCommand: Command = {
  path: ['unlock-status'],
  summary: "Indique si une quete est debloquee, d'apres les quetes deja completees",
  positionals: [{ name: 'quete' }],
  flags: { completed: 'values', json: 'boolean' },
  async run(ctx, args) {
    const title = args.positionals[0];
    if (title === undefined || title.trim() === '') {
      throw new CliError('Titre de quete manquant.');
    }

    const quests = await ctx.client.quests.list();
    const quest = findQuestByTitle(quests, title);
    if (!quest) {
      throw new CliError(`Aucune quete intitulee « ${title} » au catalogue.`);
    }

    const completedIds = args.flags.values('completed').map((completedTitle) => {
      const completed = findQuestByTitle(quests, completedTitle);
      if (!completed) {
        throw new CliError(`Aucune quete intitulee « ${completedTitle} » au catalogue.`);
      }
      return completed.id;
    });

    const unlocked = isQuestUnlocked(quest, completedIds);
    const prerequisiteTitle =
      quest.prerequisiteQuestId === null
        ? null
        : (findQuestById(quests, quest.prerequisiteQuestId)?.title ?? quest.prerequisiteQuestId);

    if (args.flags.has('json')) {
      ctx.out(JSON.stringify({ quest: quest.title, unlocked, prerequisiteTitle }, null, 2));
      return;
    }
    ctx.out(renderUnlockStatus({ title: quest.title, unlocked, prerequisiteTitle }));
  },
};
