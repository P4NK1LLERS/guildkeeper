import { it } from 'vitest';

/**
 * Test à compléter par l'étudiant.
 *
 * Ignoré par `npm test` (le run par défaut doit rester vert) ; exécuté — et donc
 * rouge tant qu'il n'est pas écrit — par `npm run test:todo`.
 * Une fois le test rempli, remplacer `todo(` par `it(`.
 */
export const todo = it.runIf(process.env.GUILDKEEPER_TODO === '1');
