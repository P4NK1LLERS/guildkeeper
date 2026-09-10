import { describe, it, expect } from 'vitest';
import { createGuildKeeperClient } from '../../src/client/guildKeeperClient';
import { dispatch } from '../../src/cli/parser/dispatch';
import type { CommandContext } from '../../src/cli/parser/registry';
import { routingFetch, type Route } from '../client/httpTestSupport';
import questsFixture from '../../fixtures/quests.json';
import memberFixture from '../../fixtures/member.json';
import membersFixture from '../../fixtures/members.json';
import assignmentsFixture from '../../fixtures/assignments.json';

const QUEST_ROUTES: Route[] = [
  { match: /^\/api\/v1\/quests$/, body: questsFixture },
  { match: /^\/api\/v1\/quests\/1$/, body: (questsFixture as unknown[])[0] },
  { match: /^\/api\/v1\/members$/, body: membersFixture },
  { match: /^\/api\/v1\/members\/Dragan$/, body: memberFixture },
  { match: /^\/api\/v1\/members\/Dragan\/assignments$/, body: assignmentsFixture },
];

function run(tokens: string[], routes: Route[] = QUEST_ROUTES) {
  const lines: string[] = [];
  const ctx: CommandContext = {
    client: createGuildKeeperClient({
      baseUrl: 'http://cli.test',
      fetchImpl: routingFetch(routes),
    }),
    out: (line) => lines.push(line),
    err: (line) => lines.push(line),
  };
  return dispatch(tokens, ctx).then((code) => ({ code, output: lines.join('\n') }));
}

describe('dispatch + commands', () => {
  it('quests list prints the catalog', async () => {
    // Act
    const { code, output } = await run(['quests', 'list']);

    // Assert
    expect(code).toBe(0);
    expect(output).toContain('Catalogue (3 quetes)');
  });

  it('quests list --json prints JSON', async () => {
    // Act
    const { output } = await run(['quests', 'list', '--json']);

    // Assert
    expect(JSON.parse(output)).toHaveLength(3);
  });

  it('quests show <id> prints one quest', async () => {
    // Act
    const { output } = await run(['quests', 'show', '1']);

    // Assert
    expect(output).toContain('#1 — Nettoyer les caves de la guilde');
  });

  it('members list prints every member', async () => {
    // Act
    const { code, output } = await run(['members', 'list']);

    // Assert
    expect(code).toBe(0);
    expect(output).toContain('Membres (6)');
    expect(output).toContain('Albéric');
  });

  it('unlock-status resolves --completed titles to ids', async () => {
    // Act
    const { output } = await run([
      'unlock-status',
      'Escorter la caravane marchande',
      '--completed',
      'Nettoyer les caves de la guilde',
    ]);

    // Assert
    expect(output).toContain('est debloquee');
  });

  it('unlock-status reports a locked quest', async () => {
    // Act
    const { output } = await run(['unlock-status', 'Terrasser le dragon des cimes']);

    // Assert
    expect(output).toContain('verrouillee');
    expect(output).toContain('Escorter la caravane marchande');
  });

  it('simulate reward computes the NOVICE reward', async () => {
    // Act
    const { output } = await run([
      'simulate',
      'reward',
      'Terrasser le dragon des cimes',
      '--luck',
      '5',
    ]);

    // Assert
    expect(output).toContain('550 XP');
    expect(output).toContain('375 pieces');
  });

  it('simulate reward rejects a missing --luck with exit code 2', async () => {
    // Act
    const { code, output } = await run(['simulate', 'reward', 'Terrasser le dragon des cimes']);

    // Assert
    expect(code).toBe(2);
    expect(output).toContain('--luck');
  });

  it('plan recommends the next quest for a member', async () => {
    // Act
    const { output } = await run(['plan', 'Dragan']);

    // Assert
    // Dragan a la quete 2 en cours dans la fixture d'attributions
    expect(output).toContain('deja en quete');
  });

  it('report lays out the progression of a member', async () => {
    // Act
    const { output } = await run(['report', 'Dragan']);

    // Assert
    expect(output).toContain('Progression de Dragan');
    expect(output).toContain('Nettoyer les caves de la guilde');
  });

  it('surfaces a 404 from the API as an execution error (exit 1)', async () => {
    // Act
    const { code, output } = await run(['report', 'Gandalf']);

    // Assert
    expect(code).toBe(1);
    expect(output).toContain('Erreur :');
  });

  it('unknown command exits 2 with a suggestion', async () => {
    // Act
    const { code, output } = await run(['quest', 'list']);

    // Assert
    expect(code).toBe(2);
    expect(output).toContain('quests list');
  });

  it('help lists the commands', async () => {
    // Act
    const { code, output } = await run(['help']);

    // Assert
    expect(code).toBe(0);
    expect(output).toContain('quests list');
    expect(output).toContain('mode interactif');
  });
});
