import { describe, it, expect } from 'vitest';
import { createGuildKeeperClient } from '../../src/client/guildKeeperClient';
import { findQuestById } from '../../src/planner/unlockTree';
import { simulateReward } from '../../src/planner/simulate';
import type { MemberRank, QuestDifficulty } from '../../src/client/dto';

/**
 * Contrat cross-stack contre le **serveur reel**.
 *
 * Ignore par `npm test` ; execute par `npm run test:contract` (CI, apres avoir
 * demarre le jar Javalin). Verifie que les DTO ecrits a la main correspondent
 * toujours aux reponses du serveur, et que la simulation locale
 * (`simulateReward`) reste d'accord avec l'endpoint autoritatif `reward-preview`.
 */
const enabled = process.env.GUILDKEEPER_CONTRACT === '1';
const baseUrl = process.env.GUILDKEEPER_API_URL ?? 'http://localhost:7070';
const difficulties: QuestDifficulty[] = ['EASY', 'MEDIUM', 'HARD', 'LEGENDARY'];
const ranks: MemberRank[] = ['NOVICE', 'APPRENTICE', 'VETERAN', 'ELITE', 'GUILD_MASTER'];

describe.runIf(enabled)('contrat cross-stack (serveur reel)', () => {
  const client = createGuildKeeperClient({ baseUrl });

  it('le catalogue renvoye a la forme attendue par QuestDto', async () => {
    // Act
    const quests = await client.quests.list();

    // Assert
    expect(quests.length).toBeGreaterThan(0);
    for (const quest of quests) {
      expect(typeof quest.id).toBe('string');
      expect(typeof quest.title).toBe('string');
      expect(difficulties).toContain(quest.difficulty);
      expect(typeof quest.baseExperienceReward).toBe('number');
      expect(typeof quest.baseLootValue).toBe('number');
      expect(
        quest.prerequisiteQuestId === null || typeof quest.prerequisiteQuestId === 'string',
      ).toBe(true);
    }
  });

  it('la liste des membres a la forme attendue par MemberDto', async () => {
    // Act
    const [members, guild] = await Promise.all([client.members.list(), client.guild()]);

    // Assert
    expect(members.length).toBe(guild.memberCount);
    for (const member of members) {
      expect(typeof member.id).toBe('string');
      expect(typeof member.name).toBe('string');
      expect(ranks).toContain(member.rank);
      expect(typeof member.experiencePoints).toBe('number');
      expect(typeof member.luck).toBe('number');
    }
  });

  it('la synthese de guilde a la forme attendue par GuildStatusDto', async () => {
    // Act
    const guild = await client.guild();

    // Assert
    expect(typeof guild.balance).toBe('number');
    expect(typeof guild.memberCount).toBe('number');
  });

  it.each([
    ['1', 1],
    ['1', 10],
    ['2', 3],
    ['3', 1],
    ['3', 5],
    ['3', 10],
  ])(
    "simulateReward est d'accord avec reward-preview (quete %s, chance %i)",
    async (questId, luck) => {
      // Arrange
      const quests = await client.quests.list();
      const quest = findQuestById(quests, questId);
      expect(quest, `quete #${questId} absente du catalogue serveur`).toBeDefined();

      // Act
      const server = await client.quests.rewardPreview(questId, luck);
      const local = simulateReward(quest!, 'NOVICE', luck);

      // Assert
      expect(local).toEqual({ experience: server.experience, loot: server.loot });
    },
  );
});
