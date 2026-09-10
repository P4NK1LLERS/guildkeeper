import type { QuestDto } from '../client/dto.ts';
import { availableQuests } from './unlockTree.ts';

/**
 * Recommande la prochaine quete a tenter pour un membre.
 *
 * Regle : parmi les quetes debloquees et non completees, la moins couteuse en
 * experience de base (le premier maillon non franchi de la chaine). Renvoie
 * `undefined` si le membre a deja une quete en cours, ou si rien n'est
 * disponible.
 *
 * Fonction pure : `completedQuestIds` et `inProgressQuestId` proviennent des
 * attributions deja chargees.
 */
export function recommendNextQuest(
  quests: QuestDto[],
  completedQuestIds: string[],
  inProgressQuestId?: string,
): QuestDto | undefined {
  if (inProgressQuestId !== undefined) {
    return undefined;
  }
  return [...availableQuests(quests, completedQuestIds)].sort(
    (a, b) => a.baseExperienceReward - b.baseExperienceReward,
  )[0];
}
