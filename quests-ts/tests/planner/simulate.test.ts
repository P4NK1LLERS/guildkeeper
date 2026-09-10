import { describe, it, expect } from 'vitest';
import {
  calculateExperienceReward,
  calculateLoot,
  simulateReward,
} from '../../src/planner/simulate';
import type { MemberRank, QuestDto } from '../../src/client/dto';

const easyQuest: QuestDto = {
  id: '1',
  title: 'Nettoyer les caves de la guilde',
  difficulty: 'EASY',
  baseExperienceReward: 100,
  baseLootValue: 20,
  prerequisiteQuestId: null,
};

const legendaryQuest: QuestDto = {
  id: '3',
  title: 'Terrasser le dragon des cimes',
  difficulty: 'LEGENDARY',
  baseExperienceReward: 500,
  baseLootValue: 300,
  prerequisiteQuestId: '2',
};

describe('calculateExperienceReward', () => {
  // Miroir du cas Java "should_grant_base_experience_when_member_is_novice"
  it('grants the base experience for a NOVICE member', () => {
    // Act
    const reward = calculateExperienceReward(easyQuest, 'NOVICE');

    // Assert
    expect(reward).toBe(100);
  });

  // Miroir du cas Java "should_grant_20_percent_bonus_when_member_is_veteran"
  it('grants a +20% bonus for a VETERAN member', () => {
    // Act
    const reward = calculateExperienceReward(easyQuest, 'VETERAN');

    // Assert
    expect(reward).toBe(120);
  });

  it('adds the +50 boost for a LEGENDARY quest completed by a NOVICE', () => {
    // Act
    const reward = calculateExperienceReward(legendaryQuest, 'NOVICE');

    // Assert
    expect(reward).toBe(550);
  });

  // Chapitre 3 — « Atelier pratique - Consolider les tests Vitest du module quêtes »
  // +10 % par rang au-dessus de NOVICE, sur une base de 100 XP.
  it.each<[MemberRank, number]>([
    ['NOVICE', 100],
    ['APPRENTICE', 110],
    ['VETERAN', 120],
    ['ELITE', 130],
    ['GUILD_MASTER', 140],
  ])('mirrors the Java experience formula for rank %s', (rank, expected) => {
    // Act
    const reward = calculateExperienceReward(easyQuest, rank);

    // Assert
    expect(reward).toBe(expected);
  });

  // Le coup de pouce LEGENDARY est reserve aux NOVICE : les autres rangs n'ont que le bonus de rang.
  it.each<[MemberRank, number]>([
    ['NOVICE', 550],
    ['APPRENTICE', 550],
    ['VETERAN', 600],
    ['ELITE', 650],
    ['GUILD_MASTER', 700],
  ])('applies the +50 LEGENDARY boost to NOVICE only (rank %s)', (rank, expected) => {
    // Act
    const reward = calculateExperienceReward(legendaryQuest, rank);

    // Assert
    expect(reward).toBe(expected);
  });
});

describe('calculateLoot', () => {
  it('mirrors the Java integer formula base + trunc(base * luck / 20)', () => {
    // Act & Assert
    expect(calculateLoot(20, 1)).toBe(21);
    expect(calculateLoot(20, 10)).toBe(30);
    expect(calculateLoot(300, 5)).toBe(375);
  });

  it('truncates the bonus instead of rounding it, like the Java integer division', () => {
    // 1 * 10 / 20 = 0.5 -> tronque a 0, comme en arithmetique entiere Java
    expect(calculateLoot(1, 10)).toBe(1);
    expect(calculateLoot(50, 1)).toBe(52);
    expect(calculateLoot(0, 10)).toBe(0);
  });
});

describe('simulateReward', () => {
  it('combines experience and loot for a given rank and luck', () => {
    // Act
    const reward = simulateReward(legendaryQuest, 'NOVICE', 5);

    // Assert
    expect(reward).toEqual({ experience: 550, loot: 375 });
  });
});
