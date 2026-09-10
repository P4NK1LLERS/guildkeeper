import { describe, expect, it, vi } from 'vitest';
import { createGuildKeeperClient } from '../../src/client/guildKeeperClient';
import { NotFoundError } from '../../src/client/errors';
import { fetchedUrl, jsonResponse } from './httpTestSupport';
import assignmentsFixture from '../../fixtures/assignments.json';
import notFoundFixture from '../../fixtures/error-not-found.json';

// Chapitre 3 — « Tester une fonction asynchrone » (TP).
//
// Modèle : tests/client/guildKeeperClient.test.ts (fetchImpl simulé + async/await).
// Cible : client.members.assignments(name) et client.members.get(name).

const BASE = 'http://api.test';

function clientWith(response: Response) {
  const fetchImpl = vi.fn().mockResolvedValue(response);
  return { client: createGuildKeeperClient({ baseUrl: BASE, fetchImpl }), fetchImpl };
}

describe('members (asynchrone) — TP chapitre 3', () => {
  it('members.assignments(name) retourne la liste des attributions pour un membre connu', async () => {
    // Arrange
    const { client, fetchImpl } = clientWith(jsonResponse(assignmentsFixture));

    // Act
    const assignments = await client.members.assignments('Dragan');

    // Assert
    expect(fetchedUrl(fetchImpl)).toBe('http://api.test/api/v1/members/Dragan/assignments');
    expect(assignments).toHaveLength(2);
    expect(assignments.map((a) => a.status)).toEqual(['COMPLETED', 'ASSIGNED']);
  });

  it('members.get(name) rejette avec NotFoundError pour un membre inconnu', async () => {
    // Arrange
    const { client } = clientWith(jsonResponse(notFoundFixture, { status: 404 }));

    // Act & Assert
    await expect(client.members.get('Gandalf')).rejects.toBeInstanceOf(NotFoundError);
  });
});
