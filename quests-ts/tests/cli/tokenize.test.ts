import { describe, it, expect } from 'vitest';
import { tokenize } from '../../src/cli/parser/tokenize';
import { CliError } from '../../src/cli/errors';

describe('tokenize', () => {
  it('splits on whitespace', () => {
    expect(tokenize('quests list --json')).toEqual(['quests', 'list', '--json']);
  });

  it('keeps double-quoted groups together', () => {
    expect(tokenize('unlock-status "Escorter la caravane marchande"')).toEqual([
      'unlock-status',
      'Escorter la caravane marchande',
    ]);
  });

  it('keeps single-quoted groups together', () => {
    expect(tokenize("plan 'Jean Bon'")).toEqual(['plan', 'Jean Bon']);
  });

  it('treats --flag=value as a single token', () => {
    expect(tokenize('simulate reward "Dragon" --luck=5')).toEqual([
      'simulate',
      'reward',
      'Dragon',
      '--luck=5',
    ]);
  });

  it('collapses runs of spaces and trims', () => {
    expect(tokenize('   quests    list   ')).toEqual(['quests', 'list']);
  });

  it('produces an empty array for a blank line', () => {
    expect(tokenize('   ')).toEqual([]);
  });

  it('throws on an unterminated quote', () => {
    expect(() => tokenize('plan "Jean')).toThrow(CliError);
  });
});
