import { describe, it, expect } from 'vitest';
import { parse, type CommandSpec } from '../../src/cli/parser/parse';

const specs: CommandSpec[] = [
  { path: ['quests', 'list'], summary: '', positionals: [], flags: { json: 'boolean' } },
  {
    path: ['quests', 'show'],
    summary: '',
    positionals: [{ name: 'id' }],
    flags: { json: 'boolean' },
  },
  {
    path: ['simulate', 'reward'],
    summary: '',
    positionals: [{ name: 'quete' }],
    flags: { luck: 'value', json: 'boolean' },
  },
  {
    path: ['unlock-status'],
    summary: '',
    positionals: [{ name: 'quete' }],
    flags: { completed: 'values', json: 'boolean' },
  },
];

describe('parse', () => {
  it('matches a two-word command path', () => {
    // Act
    const outcome = parse(['quests', 'list'], specs);

    // Assert
    expect(outcome.kind).toBe('command');
    if (outcome.kind === 'command') {
      expect(outcome.spec.path).toEqual(['quests', 'list']);
    }
  });

  it('collects a boolean flag', () => {
    // Act
    const outcome = parse(['quests', 'list', '--json'], specs);

    // Assert
    expect(outcome.kind === 'command' && outcome.flags.has('json')).toBe(true);
  });

  it('reads a value flag written as --luck value', () => {
    // Act
    const outcome = parse(['simulate', 'reward', 'Dragon', '--luck', '5'], specs);

    // Assert
    expect(outcome.kind === 'command' && outcome.flags.value('luck')).toBe('5');
  });

  it('reads a value flag written as --luck=value', () => {
    // Act
    const outcome = parse(['simulate', 'reward', 'Dragon', '--luck=7'], specs);

    // Assert
    expect(outcome.kind === 'command' && outcome.flags.value('luck')).toBe('7');
  });

  it('collects a repeatable flag', () => {
    // Act
    const outcome = parse(
      ['unlock-status', 'Caravane', '--completed', 'Caves', '--completed', 'Dragon'],
      specs,
    );

    // Assert
    expect(outcome.kind === 'command' && outcome.flags.values('completed')).toEqual([
      'Caves',
      'Dragon',
    ]);
  });

  it('stops option parsing after --', () => {
    // Act
    const outcome = parse(['quests', 'show', '--', '--json'], specs);

    // Assert
    expect(outcome.kind === 'command' && outcome.positionals).toEqual(['--json']);
  });

  it('rejects an unknown flag with a suggestion', () => {
    // Act
    const outcome = parse(['simulate', 'reward', 'Dragon', '--luk', '5'], specs);

    // Assert
    expect(outcome.kind).toBe('error');
    if (outcome.kind === 'error') {
      expect(outcome.message).toContain('--luck');
    }
  });

  it('rejects an unknown command with a suggestion', () => {
    // Act
    const outcome = parse(['quest', 'list'], specs);

    // Assert
    expect(outcome.kind).toBe('error');
    if (outcome.kind === 'error') {
      expect(outcome.message).toContain('quests list');
    }
  });

  it('rejects a missing required positional', () => {
    // Act
    const outcome = parse(['quests', 'show'], specs);

    // Assert
    expect(outcome.kind).toBe('error');
    if (outcome.kind === 'error') {
      expect(outcome.message).toContain('Argument manquant');
    }
  });

  it('rejects a value flag with no value', () => {
    // Act
    const outcome = parse(['simulate', 'reward', 'Dragon', '--luck'], specs);

    // Assert
    expect(outcome.kind).toBe('error');
  });

  it('returns a help outcome for --help', () => {
    // Act
    const outcome = parse(['quests', 'list', '--help'], specs);

    // Assert
    expect(outcome.kind).toBe('help');
    if (outcome.kind === 'help') {
      expect(outcome.spec?.path).toEqual(['quests', 'list']);
    }
  });

  it('returns a general help outcome for `help`', () => {
    // Act & Assert
    expect(parse(['help'], specs).kind).toBe('help');
  });

  it('returns a command help outcome for `help <command>`', () => {
    // Act
    const outcome = parse(['help', 'simulate', 'reward'], specs);

    // Assert
    expect(outcome.kind === 'help' && outcome.spec?.path).toEqual(['simulate', 'reward']);
  });
});
