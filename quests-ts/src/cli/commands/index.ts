import type { Command } from '../parser/registry.ts';
import { questsListCommand, questsShowCommand } from './quests.ts';
import { membersListCommand } from './members.ts';
import { unlockStatusCommand } from './unlockStatus.ts';
import { simulateRewardCommand } from './simulateReward.ts';
import { planCommand } from './plan.ts';
import { reportCommand } from './report.ts';

/** Registre des commandes du CLI. */
export const commands: readonly Command[] = [
  questsListCommand,
  questsShowCommand,
  membersListCommand,
  unlockStatusCommand,
  simulateRewardCommand,
  planCommand,
  reportCommand,
];
