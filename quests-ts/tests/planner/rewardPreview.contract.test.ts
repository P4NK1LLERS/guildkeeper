import { describe, it, expect } from 'vitest';
import { simulateReward } from '../../src/planner/simulate';
import { findQuestById } from '../../src/planner/unlockTree';
import type { QuestDto } from '../../src/client/dto';
import questsFixture from '../../fixtures/quests.json';
import rewardPreviews from '../../fixtures/reward-previews.json';

/**
 * Test de contrat : la simulation locale (`simulateReward`, point de vue NOVICE)
 * doit coller a la valeur autoritative renvoyee par
 * `GET /api/v1/quests/{id}/reward-preview?luck=n`, enregistree dans
 * `fixtures/reward-previews.json`.
 *
 * Si une formule bouge cote Java sans etre repercutee ici, ce test casse.
 * (La version qui interroge le serveur en direct tourne en CI :
 * `tests/contract/live.contract.test.ts`, `npm run test:contract`.)
 */

const quests = questsFixture as QuestDto[];

interface RewardPreviewFixture {
  questId: string;
  luck: number;
  experience: number;
  loot: number;
}

describe('reward-preview contract', () => {
  it.each(rewardPreviews as RewardPreviewFixture[])(
    'matches the server for quest $questId at luck $luck',
    ({ questId, luck, experience, loot }) => {
      // Arrange
      const quest = findQuestById(quests, questId);
      expect(quest, `quete #${questId} absente du catalogue de fixtures`).toBeDefined();

      // Act & Assert
      expect(simulateReward(quest!, 'NOVICE', luck)).toEqual({ experience, loot });
    },
  );
});
