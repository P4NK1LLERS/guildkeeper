import type { QuestDto } from '../client/dto.ts';

/**
 * Navigation dans le catalogue de quetes et statut de deblocage.
 *
 * Fonctions pures : elles recoivent le catalogue et la liste des quetes
 * completees deja charges (via le client HTTP), sans faire d'appel elles-memes.
 */

/** Recherche une quete par identifiant. */
export function findQuestById(quests: QuestDto[], id: string): QuestDto | undefined {
  return quests.find((quest) => quest.id === id);
}

/** Recherche une quete par titre exact. */
export function findQuestByTitle(quests: QuestDto[], title: string): QuestDto | undefined {
  return quests.find((quest) => quest.title === title);
}

/**
 * Une quete est debloquee si elle n'a pas de prerequis, ou si son prerequis
 * figure parmi les quetes completees.
 */
export function isQuestUnlocked(quest: QuestDto, completedQuestIds: string[]): boolean {
  if (quest.prerequisiteQuestId === null) {
    return true;
  }
  return completedQuestIds.includes(quest.prerequisiteQuestId);
}

/** Quetes debloquees et pas encore completees, dans l'ordre du catalogue. */
export function availableQuests(quests: QuestDto[], completedQuestIds: string[]): QuestDto[] {
  return quests.filter(
    (quest) => !completedQuestIds.includes(quest.id) && isQuestUnlocked(quest, completedQuestIds),
  );
}
