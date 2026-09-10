import type { Command } from '../parser/registry.ts';
import { CliError } from '../errors.ts';
import { buildProgressionReport } from '../../planner/progressionReport.ts';
import { renderProgressionReport } from '../render/render.ts';

export const reportCommand: Command = {
  path: ['report'],
  summary: "Synthese de progression d'un membre",
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

    const report = buildProgressionReport(member, quests, assignments);
    ctx.out(
      args.flags.has('json') ? JSON.stringify(report, null, 2) : renderProgressionReport(report),
    );
  },
};
