import type { Command } from '../parser/registry.ts';
import { CliError } from '../errors.ts';
import { findQuestByTitle } from '../../planner/unlockTree.ts';
import { simulateReward } from '../../planner/simulate.ts';
import { renderSimulatedReward } from '../render/render.ts';

export const simulateRewardCommand: Command = {
  path: ['simulate', 'reward'],
  summary: "Recompense simulee d'une quete pour un membre NOVICE",
  positionals: [{ name: 'quete' }],
  flags: { luck: 'value', json: 'boolean' },
  async run(ctx, args) {
    const title = args.positionals[0];
    if (title === undefined || title.trim() === '') {
      throw new CliError('Titre de quete manquant.');
    }

    const luckRaw = args.flags.value('luck');
    if (luckRaw === undefined) {
      throw new CliError("L'option --luck <1-10> est obligatoire.");
    }
    const luck = Number(luckRaw);
    if (!Number.isInteger(luck) || luck < 1 || luck > 10) {
      throw new CliError('--luck doit etre un entier entre 1 et 10.');
    }

    const quests = await ctx.client.quests.list();
    const quest = findQuestByTitle(quests, title);
    if (!quest) {
      throw new CliError(`Aucune quete intitulee « ${title} » au catalogue.`);
    }

    const reward = simulateReward(quest, 'NOVICE', luck);
    if (args.flags.has('json')) {
      ctx.out(JSON.stringify({ quest: quest.title, luck, ...reward }, null, 2));
      return;
    }
    ctx.out(renderSimulatedReward({ title: quest.title, luck, reward }));
  },
};
