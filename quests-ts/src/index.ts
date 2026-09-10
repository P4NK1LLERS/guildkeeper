/**
 * Point d'entree du module : le client HTTP de l'API GuildKeeper et le planner
 * (logique de progression pure au-dessus).
 */

export * from './client/dto.ts';
export * from './client/errors.ts';
export { createGuildKeeperClient } from './client/guildKeeperClient.ts';
export type { GuildKeeperClient, GuildKeeperClientOptions } from './client/guildKeeperClient.ts';

export {
  findQuestById,
  findQuestByTitle,
  isQuestUnlocked,
  availableQuests,
} from './planner/unlockTree.ts';
export {
  calculateExperienceReward,
  calculateLoot,
  simulateReward,
  type SimulatedReward,
} from './planner/simulate.ts';
export { recommendNextQuest } from './planner/recommend.ts';
export { buildProgressionReport, type ProgressionReport } from './planner/progressionReport.ts';
