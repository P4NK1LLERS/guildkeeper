/**
 * Erreur d'usage / metier du CLI, destinee a etre affichee telle quelle a
 * l'utilisateur (pas de stack trace).
 */
export class CliError extends Error {
  /** Code de sortie du processus (2 = usage, 1 = erreur d'execution). */
  readonly exitCode: number;

  constructor(message: string, exitCode = 2) {
    super(message);
    this.name = 'CliError';
    this.exitCode = exitCode;
  }
}
