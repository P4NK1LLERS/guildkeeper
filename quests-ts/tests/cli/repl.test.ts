import { describe, it, expect, vi } from 'vitest';
import { handleReplLine } from '../../src/cli/repl';
import type { CommandContext } from '../../src/cli/parser/registry';
import { createGuildKeeperClient } from '../../src/client/guildKeeperClient';
import { routingFetch } from '../client/httpTestSupport';
import questsFixture from '../../fixtures/quests.json';

function context() {
  const lines: string[] = [];
  const ctx: CommandContext = {
    client: createGuildKeeperClient({
      baseUrl: 'http://cli.test',
      fetchImpl: routingFetch([{ match: /^\/api\/v1\/quests$/, body: questsFixture }]),
    }),
    out: (line) => lines.push(line),
    err: (line) => lines.push(line),
  };
  return { ctx, lines };
}

describe('handleReplLine', () => {
  it('ignores a blank line', async () => {
    // Arrange
    const { ctx, lines } = context();

    // Act & Assert
    expect(await handleReplLine('   ', ctx)).toBe('continue');
    expect(lines).toEqual([]);
  });

  it('recognises exit words', async () => {
    // Arrange
    const { ctx } = context();

    // Act & Assert
    expect(await handleReplLine('exit', ctx)).toBe('exit');
    expect(await handleReplLine('quit', ctx)).toBe('exit');
  });

  it('tokenises then dispatches a command line', async () => {
    // Arrange
    const { ctx, lines } = context();

    // Act
    const result = await handleReplLine('quests list', ctx);

    // Assert
    expect(result).toBe('continue');
    expect(lines.join('\n')).toContain('Catalogue (3 quetes)');
  });

  it('handles quoted arguments', async () => {
    // Arrange
    const { ctx, lines } = context();

    // Act
    await handleReplLine('unlock-status "Terrasser le dragon des cimes"', ctx);

    // Assert
    expect(lines.join('\n')).toContain('verrouillee');
  });

  it('reports an unterminated quote without crashing', async () => {
    // Arrange
    const { ctx, lines } = context();

    // Act
    const result = await handleReplLine('plan "Jean', ctx);

    // Assert
    expect(result).toBe('continue');
    expect(lines.join('\n')).toContain('Guillemet');
  });

  it('is stateless: does not leak between lines', async () => {
    // Arrange
    const { ctx } = context();
    const spy = vi.spyOn(ctx, 'out');

    // Act
    await handleReplLine('quests list', ctx);
    await handleReplLine('quests list', ctx);

    // Assert
    // deux invocations independantes -> deux rendus identiques
    expect(spy.mock.calls.length).toBe(2);
    expect(spy.mock.calls[0]).toEqual(spy.mock.calls[1]);
  });
});
