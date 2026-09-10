import type { MemberRank, QuestDto } from '../client/dto.ts';

/**
 * Simulation locale de la recompense d'une quete.
 *
 * ⚠️ INDICATIF — le serveur fait foi. Ces formules reproduisent celles du
 * domaine Java (`ExperienceCalculator`, `LootCalculator`) pour donner un apercu
 * hors ligne ; l'endpoint `GET /api/v1/quests/{id}/reward-preview?luck=n` renvoie
 * la valeur autoritative. Un test de contrat verifie que ce miroir reste
 * d'accord avec le serveur (`tests/planner/rewardPreview.contract.test.ts`).
 */

const RANK_ORDER: readonly MemberRank[] = [
  'NOVICE',
  'APPRENTICE',
  'VETERAN',
  'ELITE',
  'GUILD_MASTER',
];

const BONUS_PER_RANK = 0.1;
const LEGENDARY_NOVICE_BOOST = 50;

/**
 * Experience gagnee pour une quete completee :
 * - base = `quest.baseExperienceReward`
 * - +10 % par rang au-dessus de NOVICE
 * - +50 points fixes si quete LEGENDARY completee par un NOVICE
 */
export function calculateExperienceReward(quest: QuestDto, rank: MemberRank): number {
  const stepsAboveNovice = RANK_ORDER.indexOf(rank);
  const rankFactor = 1 + BONUS_PER_RANK * stepsAboveNovice;
  let reward = Math.round(quest.baseExperienceReward * rankFactor);

  if (quest.difficulty === 'LEGENDARY' && rank === 'NOVICE') {
    reward += LEGENDARY_NOVICE_BOOST;
  }
  return reward;
}

/**
 * Butin en pieces d'or : `base + trunc(base * luck / 20)` (arithmetique entiere,
 * comme le `LootCalculator` Java). `luck` doit etre compris entre 1 et 10.
 */
export function calculateLoot(baseLootValue: number, luck: number): number {
  return baseLootValue + Math.trunc((baseLootValue * luck) / 20);
}

export interface SimulatedReward {
  experience: number;
  loot: number;
}

/**
 * Recompense simulee pour un membre d'un rang donne et d'une chance donnee.
 * `simulateReward(quest, 'NOVICE', luck)` doit correspondre a `reward-preview`.
 */
export function simulateReward(quest: QuestDto, rank: MemberRank, luck: number): SimulatedReward {
  return {
    experience: calculateExperienceReward(quest, rank),
    loot: calculateLoot(quest.baseLootValue, luck),
  };
}
