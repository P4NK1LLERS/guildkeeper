import { describe, it, expect } from 'vitest';
import { recommendNextQuest } from '../../src/planner/recommend';
import type { QuestDto } from '../../src/client/dto';
import questsFixture from '../../fixtures/quests.json';

const quests = questsFixture as QuestDto[];

describe('recommendNextQuest', () => {
  it('recommends the cheapest available quest for a fresh member', () => {
    expect(recommendNextQuest(quests, [])?.id).toBe('1');
  });

  it('advances along the prerequisite chain as quests are completed', () => {
    expect(recommendNextQuest(quests, ['1'])?.id).toBe('2');
    expect(recommendNextQuest(quests, ['1', '2'])?.id).toBe('3');
  });

  it('returns undefined when every quest is completed', () => {
    expect(recommendNextQuest(quests, ['1', '2', '3'])).toBeUndefined();
  });

  it('returns undefined when the member already has a quest in progress', () => {
    expect(recommendNextQuest(quests, [], '1')).toBeUndefined();
  });
});
