/**
 * Interfaces miroir des DTO renvoyes par le serveur Java (`.../http/dto/`).
 *
 * Ecrites a la main, volontairement : pas de generation depuis un schema. Un
 * test de contrat verifie qu'elles restent alignees sur les reponses reelles
 * enregistrees dans `fixtures/`.
 */

export type QuestDifficulty = 'EASY' | 'MEDIUM' | 'HARD' | 'LEGENDARY';
export type MemberRank = 'NOVICE' | 'APPRENTICE' | 'VETERAN' | 'ELITE' | 'GUILD_MASTER';
export type QuestAssignmentStatus = 'ASSIGNED' | 'COMPLETED' | 'ABANDONED';

export interface QuestDto {
  id: string;
  title: string;
  difficulty: QuestDifficulty;
  baseExperienceReward: number;
  baseLootValue: number;
  /** `null` quand la quete n'a pas de prerequis (le serveur serialise `null`, pas d'absence). */
  prerequisiteQuestId: string | null;
}

export interface MemberDto {
  id: string;
  name: string;
  rank: MemberRank;
  experiencePoints: number;
  luck: number;
}

export interface AssignmentDto {
  questId: string;
  questTitle: string;
  status: QuestAssignmentStatus;
}

export interface GuildStatusDto {
  balance: number;
  memberCount: number;
}

export interface RewardPreviewDto {
  questId: string;
  luck: number;
  experience: number;
  loot: number;
}
