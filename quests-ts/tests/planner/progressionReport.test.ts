import { describe, it, expect } from 'vitest';
import { buildProgressionReport } from '../../src/planner/progressionReport';
import type { AssignmentDto, MemberDto, QuestDto } from '../../src/client/dto';
import questsFixture from '../../fixtures/quests.json';
import memberFixture from '../../fixtures/member.json';
import assignmentsFixture from '../../fixtures/assignments.json';

const quests = questsFixture as QuestDto[];
const member = memberFixture as MemberDto;
const assignments = assignmentsFixture as AssignmentDto[];

describe('buildProgressionReport', () => {
  it('summarises the recorded state of Dragan', () => {
    // Act
    const report = buildProgressionReport(member, quests, assignments);

    // Assert
    expect(report.member).toEqual({ name: 'Dragan', rank: 'NOVICE', experiencePoints: 0 });
    expect(report.completed).toEqual(['Nettoyer les caves de la guilde']);
    expect(report.inProgress).toBe('Escorter la caravane marchande');
    expect(report.available).toEqual([]); // quest 3 still locked (caravan not completed)
    expect(report.locked).toEqual(['Terrasser le dragon des cimes']);
    expect(report.nextRecommended).toBeNull(); // a quest is in progress
  });

  it('reports the next recommended quest for a member with no assignment', () => {
    // Act
    const report = buildProgressionReport(member, quests, []);

    // Assert
    expect(report.completed).toEqual([]);
    expect(report.inProgress).toBeNull();
    expect(report.available).toEqual(['Nettoyer les caves de la guilde']);
    expect(report.locked).toEqual([
      'Escorter la caravane marchande',
      'Terrasser le dragon des cimes',
    ]);
    expect(report.nextRecommended).toBe('Nettoyer les caves de la guilde');
  });
});
