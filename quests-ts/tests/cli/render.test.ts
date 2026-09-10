import { describe, it, expect } from 'vitest';
import {
  renderMemberList,
  renderPlan,
  renderProgressionReport,
  renderQuest,
  renderQuestList,
  renderSimulatedReward,
  renderUnlockStatus,
} from '../../src/cli/render/render';
import type { MemberDto, QuestDto } from '../../src/client/dto';
import type { ProgressionReport } from '../../src/planner/progressionReport';
import questsFixture from '../../fixtures/quests.json';
import membersFixture from '../../fixtures/members.json';

const quests = questsFixture as QuestDto[];
const members = membersFixture as MemberDto[];

describe('render', () => {
  it('renderMemberList shows name, rank, XP and luck', () => {
    // Act
    const text = renderMemberList(members);

    // Assert
    expect(text).toContain('Membres (6) :');
    expect(text).toContain('Albéric — NOVICE, 0 XP (chance 1)');
  });

  it('renderMemberList handles an empty guild', () => {
    // Act & Assert
    expect(renderMemberList([])).toBe('Aucun membre.');
  });

  it('renderQuestList lists every quest with its prerequisite', () => {
    // Act
    const text = renderQuestList(quests);

    // Assert
    expect(text).toContain('Catalogue (3 quetes)');
    expect(text).toContain('#2 Escorter la caravane marchande');
    expect(text).toContain('prerequis #1');
  });

  it('renderQuestList handles an empty catalog', () => {
    // Act & Assert
    expect(renderQuestList([])).toBe('Catalogue vide.');
  });

  it('renderQuest shows "aucun" when there is no prerequisite', () => {
    // Act & Assert
    expect(renderQuest(quests[0]!)).toContain('prerequis : aucun');
  });

  it('renderUnlockStatus reports locked with the prerequisite title', () => {
    // Act & Assert
    expect(
      renderUnlockStatus({
        title: 'Escorter la caravane',
        unlocked: false,
        prerequisiteTitle: 'Nettoyer les caves',
      }),
    ).toContain('verrouillee (prerequis non complete : « Nettoyer les caves »)');
  });

  it('renderSimulatedReward flags the value as indicative', () => {
    // Act
    const text = renderSimulatedReward({
      title: 'Dragon',
      luck: 5,
      reward: { experience: 550, loot: 375 },
    });

    // Assert
    expect(text).toContain('550 XP');
    expect(text).toContain('indicatif');
  });

  it('renderPlan announces the next quest', () => {
    // Act
    const text = renderPlan({
      memberName: 'Dragan',
      inProgress: null,
      next: { title: 'Nettoyer les caves', reward: { experience: 50, loot: 21 } },
    });

    // Assert
    expect(text).toContain('Prochaine quete conseillee pour Dragan');
    expect(text).toContain('Nettoyer les caves');
  });

  it('renderPlan says when the member is already busy', () => {
    // Act & Assert
    expect(
      renderPlan({ memberName: 'Dragan', inProgress: 'Escorter la caravane', next: null }),
    ).toContain('deja en quete');
  });

  it('renderProgressionReport lays out every section', () => {
    // Arrange
    const report: ProgressionReport = {
      member: { name: 'Dragan', rank: 'NOVICE', experiencePoints: 50 },
      completed: ['Nettoyer les caves'],
      inProgress: 'Escorter la caravane',
      available: [],
      locked: ['Terrasser le dragon'],
      nextRecommended: null,
    };

    // Act
    const text = renderProgressionReport(report);

    // Assert
    expect(text).toContain('Progression de Dragan — rang NOVICE, 50 XP');
    expect(text).toContain('En cours : Escorter la caravane');
    expect(text).toContain('- Terrasser le dragon');
    expect(text).toContain('Disponibles :\n    (aucune)');
  });
});
