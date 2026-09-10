import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    environment: 'node',
    include: ['tests/**/*.test.ts'],
    coverage: {
      provider: 'v8',
      include: ['src/**/*.ts'],
      // `dto.ts` n'est que des interfaces ; `cli/main.ts` est le cablage I/O
      // (argv, flux standard) teste manuellement, pas en unitaire.
      exclude: ['src/client/dto.ts', 'src/cli/main.ts'],
      reporter: ['text', 'html', 'lcov'],
      reportsDirectory: 'coverage',
      // Le repo souche contient volontairement des tests TODO qui echouent :
      // on veut quand meme un rapport de couverture exploitable.
      reportOnFailure: true,
    },
  },
});
