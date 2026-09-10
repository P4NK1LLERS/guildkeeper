import type { Command } from '../parser/registry.ts';
import { renderMemberList } from '../render/render.ts';

export const membersListCommand: Command = {
  path: ['members', 'list'],
  summary: 'Liste les membres de la guilde',
  positionals: [],
  flags: { json: 'boolean' },
  async run(ctx, args) {
    const members = await ctx.client.members.list();
    ctx.out(args.flags.has('json') ? JSON.stringify(members, null, 2) : renderMemberList(members));
  },
};
