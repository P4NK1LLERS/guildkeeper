import { describe, it, expect, vi } from 'vitest';
import { createGuildKeeperClient } from '../../src/client/guildKeeperClient';
import { NotFoundError } from '../../src/client/errors';
import { jsonResponse, fetchedUrl } from './httpTestSupport';
import questsFixture from '../../fixtures/quests.json';
import questFixture from '../../fixtures/quest.json';
import rewardPreviewFixture from '../../fixtures/reward-preview.json';
import memberFixture from '../../fixtures/member.json';
import membersFixture from '../../fixtures/members.json';
import assignmentsFixture from '../../fixtures/assignments.json';
import guildFixture from '../../fixtures/guild.json';
import notFoundFixture from '../../fixtures/error-not-found.json';

const BASE = 'http://api.test';

function clientWith(response: Response) {
  const fetchImpl = vi.fn().mockResolvedValue(response);
  return { client: createGuildKeeperClient({ baseUrl: BASE, fetchImpl }), fetchImpl };
}

describe('GuildKeeperClient', () => {
  it('quests.list() GETs /api/v1/quests and returns the recorded catalog', async () => {
    // Arrange
    const { client, fetchImpl } = clientWith(jsonResponse(questsFixture));

    // Act
    const quests = await client.quests.list();

    // Assert
    expect(fetchedUrl(fetchImpl)).toBe('http://api.test/api/v1/quests');
    expect(quests).toHaveLength(3);
    expect(quests[1]?.prerequisiteQuestId).toBe('1');
  });

  it('quests.get(id) GETs /api/v1/quests/{id}', async () => {
    // Arrange
    const { client, fetchImpl } = clientWith(jsonResponse(questFixture));

    // Act
    const quest = await client.quests.get('1');

    // Assert
    expect(fetchedUrl(fetchImpl)).toBe('http://api.test/api/v1/quests/1');
    expect(quest.title).toBe('Nettoyer les caves de la guilde');
    expect(quest.prerequisiteQuestId).toBeNull();
  });

  it('quests.rewardPreview(id, luck) passes luck as a query parameter', async () => {
    // Arrange
    const { client, fetchImpl } = clientWith(jsonResponse(rewardPreviewFixture));

    // Act
    const preview = await client.quests.rewardPreview('3', 5);

    // Assert
    expect(fetchedUrl(fetchImpl)).toBe('http://api.test/api/v1/quests/3/reward-preview?luck=5');
    expect(preview).toEqual({ questId: '3', luck: 5, experience: 550, loot: 375 });
  });

  it('members.list() GETs /api/v1/members and returns every member', async () => {
    // Arrange
    const { client, fetchImpl } = clientWith(jsonResponse(membersFixture));

    // Act
    const members = await client.members.list();

    // Assert
    expect(fetchedUrl(fetchImpl)).toBe('http://api.test/api/v1/members');
    expect(members).toHaveLength(6);
    expect(members[0]?.name).toBe('Albéric');
  });

  it('members.get(name) GETs /api/v1/members/{name}', async () => {
    // Arrange
    const { client, fetchImpl } = clientWith(jsonResponse(memberFixture));

    // Act
    const member = await client.members.get('Dragan');

    // Assert
    expect(fetchedUrl(fetchImpl)).toBe('http://api.test/api/v1/members/Dragan');
    expect(member.rank).toBe('NOVICE');
  });

  it('members.assignments(name) returns the assignment list', async () => {
    // Arrange
    const { client, fetchImpl } = clientWith(jsonResponse(assignmentsFixture));

    // Act
    const assignments = await client.members.assignments('Dragan');

    // Assert
    expect(fetchedUrl(fetchImpl)).toBe('http://api.test/api/v1/members/Dragan/assignments');
    expect(assignments.map((a) => a.status)).toEqual(['COMPLETED', 'ASSIGNED']);
  });

  it('guild() GETs /api/v1/guild', async () => {
    // Arrange
    const { client, fetchImpl } = clientWith(jsonResponse(guildFixture));

    // Act
    const guild = await client.guild();

    // Assert
    expect(fetchedUrl(fetchImpl)).toBe('http://api.test/api/v1/guild');
    expect(guild).toEqual({ balance: 0, memberCount: 5 });
  });

  it('surfaces a 404 as NotFoundError', async () => {
    // Arrange
    const { client } = clientWith(jsonResponse(notFoundFixture, { status: 404 }));

    // Act & Assert
    await expect(client.members.get('Gandalf')).rejects.toBeInstanceOf(NotFoundError);
  });

  it('URL-encodes path segments', async () => {
    // Arrange
    const { client, fetchImpl } = clientWith(jsonResponse(memberFixture));

    // Act
    await client.members.get('Jean Bon');

    // Assert
    expect(fetchedUrl(fetchImpl)).toBe('http://api.test/api/v1/members/Jean%20Bon');
  });
});
