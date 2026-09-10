/**
 * Taxonomie d'erreurs du client. Miroir des codes stables renvoyes par le
 * serveur dans `{ "error": "<CODE>", "message": "..." }`.
 */

/** Aucune reponse recue : reseau coupe, DNS, ou delai depasse. */
export class NetworkError extends Error {
  constructor(message: string, options?: { cause?: unknown }) {
    super(message, options);
    this.name = 'NetworkError';
  }
}

/** Reponse d'erreur de l'API (statut HTTP >= 400). */
export class ApiError extends Error {
  readonly status: number;
  readonly code: string;

  constructor(status: number, code: string, message: string) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
  }
}

/** `404` / `error: "NOT_FOUND"`. */
export class NotFoundError extends ApiError {
  constructor(message: string) {
    super(404, 'NOT_FOUND', message);
    this.name = 'NotFoundError';
  }
}

/** `400` / `error: "VALIDATION"`. */
export class ValidationError extends ApiError {
  constructor(message: string) {
    super(400, 'VALIDATION', message);
    this.name = 'ValidationError';
  }
}

interface ApiErrorBody {
  error: string;
  message: string;
}

function isApiErrorBody(value: unknown): value is ApiErrorBody {
  return (
    typeof value === 'object' &&
    value !== null &&
    typeof (value as ApiErrorBody).error === 'string' &&
    typeof (value as ApiErrorBody).message === 'string'
  );
}

/**
 * Construit l'erreur correspondant a une reponse en echec. `body` est le corps
 * deja parse (ou `undefined` s'il n'etait pas du JSON exploitable).
 */
export function toApiError(status: number, body: unknown): ApiError {
  const code = isApiErrorBody(body) ? body.error : 'UNKNOWN';
  const message = isApiErrorBody(body) ? body.message : `Reponse HTTP ${status}.`;

  if (code === 'NOT_FOUND' || status === 404) {
    return new NotFoundError(message);
  }
  if (code === 'VALIDATION' || status === 400) {
    return new ValidationError(message);
  }
  return new ApiError(status, code, message);
}
