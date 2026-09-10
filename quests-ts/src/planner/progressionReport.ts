import type { AssignmentDto, MemberDto, MemberRank, QuestDto } from '../client/dto.ts';
import { availableQuests, isQuestUnlocked } from './unlockTree.ts';
import { recommendNextQuest } from './recommend.ts';

/**
 * Synthese de la progression d'un membre, en titres de quetes (pas d'identifiant
 * technique). Fonction pure : membre, catalogue et attributions deja charges.
 */

export interface ProgressionReport {
  member: { name: string; rank: MemberRank; experiencePoints: number };
  /** Quetes completees. */
  completed: string[];
  /** Quete en cours, ou `null`. */
  inProgress: string | null;
  /** Quetes debloquees, ni completees ni en cours. */
  available: string[];
  /** Quetes dont le prerequis n'est pas encore complete. */
  locked: string[];
  /** Prochaine quete conseillee, ou `null`. */
  nextRecommended: string | null;
}

export function buildProgressionReport(
  member: MemberDto,
  quests: QuestDto[],
  assignments: AssignmentDto[],
): ProgressionReport {
  const completedIds = assignments
    .filter((assignment) => assignment.status === 'COMPLETED')
    .map((assignment) => assignment.questId);
  const completedIdSet = new Set(completedIds);
  const inProgress = assignments.find((assignment) => assignment.status === 'ASSIGNED');

  const available = availableQuests(quests, completedIds)
    .filter((quest) => quest.id !== inProgress?.questId)
    .map((quest) => quest.title);

  const locked = quests
    .filter(
      (quest) =>
        !completedIdSet.has(quest.id) &&
        quest.id !== inProgress?.questId &&
        !isQuestUnlocked(quest, completedIds),
    )
    .map((quest) => quest.title);

  const next = recommendNextQuest(quests, completedIds, inProgress?.questId);

  return {
    member: {
      name: member.name,
      rank: member.rank,
      experiencePoints: member.experiencePoints,
    },
    completed: assignments
      .filter((assignment) => assignment.status === 'COMPLETED')
      .map((assignment) => assignment.questTitle),
    inProgress: inProgress?.questTitle ?? null,
    available,
    locked,
    nextRecommended: next?.title ?? null,
  };
}
