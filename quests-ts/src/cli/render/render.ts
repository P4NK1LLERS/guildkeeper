import type { MemberDto, QuestDto } from '../../client/dto.ts';
import type { ProgressionReport } from '../../planner/progressionReport.ts';
import type { SimulatedReward } from '../../planner/simulate.ts';

/** Fonctions pures : donnee -> texte pour le terminal. */

export function renderMemberList(members: MemberDto[]): string {
  if (members.length === 0) {
    return 'Aucun membre.';
  }
  const lines = members.map(
    (member) =>
      `  ${member.name} — ${member.rank}, ${member.experiencePoints} XP (chance ${member.luck})`,
  );
  return [`Membres (${members.length}) :`, ...lines].join('\n');
}

export function renderQuestList(quests: QuestDto[]): string {
  if (quests.length === 0) {
    return 'Catalogue vide.';
  }
  const lines = quests.map((quest) => {
    const prerequisite = quest.prerequisiteQuestId
      ? ` | prerequis #${quest.prerequisiteQuestId}`
      : '';
    return `  #${quest.id} ${quest.title} [${quest.difficulty}] — ${quest.baseExperienceReward} XP / ${quest.baseLootValue} or${prerequisite}`;
  });
  return [`Catalogue (${quests.length} quetes) :`, ...lines].join('\n');
}

export function renderQuest(quest: QuestDto): string {
  return [
    `#${quest.id} — ${quest.title}`,
    `  difficulte : ${quest.difficulty}`,
    `  recompense de base : ${quest.baseExperienceReward} XP, ${quest.baseLootValue} pieces d'or`,
    `  prerequis : ${quest.prerequisiteQuestId ? `#${quest.prerequisiteQuestId}` : 'aucun'}`,
  ].join('\n');
}

export function renderUnlockStatus(input: {
  title: string;
  unlocked: boolean;
  prerequisiteTitle: string | null;
}): string {
  if (input.unlocked) {
    return `« ${input.title} » est debloquee.`;
  }
  const prerequisite = input.prerequisiteTitle ?? 'inconnu';
  return `« ${input.title} » est verrouillee (prerequis non complete : « ${prerequisite} »).`;
}

export function renderSimulatedReward(input: {
  title: string;
  luck: number;
  reward: SimulatedReward;
}): string {
  return (
    `Recompense simulee de « ${input.title} » ` +
    `(membre NOVICE, chance ${input.luck}) : ${input.reward.experience} XP, ` +
    `${input.reward.loot} pieces d'or. [indicatif — le serveur fait foi]`
  );
}

export interface PlanView {
  memberName: string;
  inProgress: string | null;
  next: { title: string; reward: SimulatedReward } | null;
}

export function renderPlan(view: PlanView): string {
  if (view.inProgress) {
    return `${view.memberName} est deja en quete : « ${view.inProgress} ».`;
  }
  if (view.next) {
    return (
      `Prochaine quete conseillee pour ${view.memberName} : « ${view.next.title} » ` +
      `(≈ ${view.next.reward.experience} XP, ${view.next.reward.loot} or — indicatif).`
    );
  }
  return `${view.memberName} a termine toutes les quetes disponibles.`;
}

export function renderProgressionReport(report: ProgressionReport): string {
  const list = (titles: string[]): string =>
    titles.length === 0 ? '    (aucune)' : titles.map((title) => `    - ${title}`).join('\n');
  return [
    `Progression de ${report.member.name} — rang ${report.member.rank}, ${report.member.experiencePoints} XP`,
    `  En cours : ${report.inProgress ?? '(aucune)'}`,
    '  Completees :',
    list(report.completed),
    '  Disponibles :',
    list(report.available),
    '  Verrouillees :',
    list(report.locked),
    `  Prochaine conseillee : ${report.nextRecommended ?? '(aucune)'}`,
  ].join('\n');
}
