import type { Command } from '../parser/registry.ts';
import { CliError } from '../errors.ts';
import { recommendNextQuest } from '../../planner/recommend.ts';
import { simulateReward } from '../../planner/simulate.ts';
import { renderPlan, type PlanView } from '../render/render.ts';

export const planCommand: Command = {
  path: ['plan'],
  summary: 'Prochaine quete conseillee pour un membre',
  positionals: [{ name: 'membre' }],
  flags: { json: 'boolean' },
  async run(ctx, args) {
    const name = args.positionals[0];
    if (name === undefined || name.trim() === '') {
      throw new CliError('Nom de membre manquant.');
    }

    const [member, quests, assignments] = await Promise.all([
      ctx.client.members.get(name),
      ctx.client.quests.list(),
      ctx.client.members.assignments(name),
    ]);

    const completedIds = assignments
      .filter((assignment) => assignment.status === 'COMPLETED')
      .map((assignment) => assignment.questId);
    const inProgress = assignments.find((assignment) => assignment.status === 'ASSIGNED');
    const next = recommendNextQuest(quests, completedIds, inProgress?.questId);

    const view: PlanView = {
      memberName: member.name,
      inProgress: inProgress?.questTitle ?? null,
      next: next
        ? { title: next.title, reward: simulateReward(next, member.rank, member.luck) }
        : null,
    };

    ctx.out(args.flags.has('json') ? JSON.stringify(view, null, 2) : renderPlan(view));
  },
};
