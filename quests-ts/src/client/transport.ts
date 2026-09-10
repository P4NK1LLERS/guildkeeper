import { NetworkError, toApiError, ApiError } from './errors.ts';

/**
 * Couche transport : un `fetch` enveloppe avec delai maximum, reessais et
 * en-tetes par defaut. Ne connait rien du domaine (chemins, DTO) : c'est le role
 * de `guildKeeperClient.ts`.
 */

export interface TransportOptions {
  /** Racine de l'API, ex. `http://localhost:7070`. Jamais de valeur par defaut ici. */
  baseUrl: string;
  /** Delai maximum par tentative, en ms (defaut 5000). */
  timeoutMs?: number;
  /** En-tetes ajoutes a chaque requete (pas d'auth cote cours, juste le point d'injection). */
  headers?: Record<string, string>;
  /** Nombre de reessais apres la premiere tentative (defaut 2). */
  maxRetries?: number;
  /** Attente avant le reessai n (0-indexe), en ms. Surchargeable pour les tests. */
  backoffMs?: (retry: number) => number;
  /** `fetch` a utiliser (defaut : celui de l'environnement). Injecte dans les tests. */
  fetchImpl?: typeof fetch;
}

export interface RequestOptions {
  query?: Record<string, string | number | undefined>;
}

export interface Transport {
  request<T>(path: string, options?: RequestOptions): Promise<T>;
}

const DEFAULT_TIMEOUT_MS = 5000;
const DEFAULT_MAX_RETRIES = 2;
const RETRYABLE_STATUS = new Set([429, 502, 503, 504]);

const defaultBackoff = (retry: number): number => [100, 300][retry] ?? 500;
const sleep = (ms: number): Promise<void> => new Promise((resolve) => setTimeout(resolve, ms));

export function createTransport(options: TransportOptions): Transport {
  const {
    baseUrl,
    timeoutMs = DEFAULT_TIMEOUT_MS,
    headers = {},
    maxRetries = DEFAULT_MAX_RETRIES,
    backoffMs = defaultBackoff,
  } = options;
  const doFetch: typeof fetch =
    options.fetchImpl ?? ((input, init) => globalThis.fetch(input, init));

  async function attemptOnce<T>(url: string): Promise<T> {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), timeoutMs);

    let response: Response;
    try {
      response = await doFetch(url, {
        method: 'GET',
        headers: { Accept: 'application/json', ...headers },
        signal: controller.signal,
      });
    } catch (cause) {
      if (controller.signal.aborted) {
        throw new NetworkError(`Delai depasse (${timeoutMs} ms) pour GET ${url}.`, { cause });
      }
      throw new NetworkError(`Echec reseau pour GET ${url}.`, { cause });
    } finally {
      clearTimeout(timer);
    }

    let text: string;
    try {
      text = await response.text();
    } catch (cause) {
      throw new NetworkError(`Lecture de la reponse impossible pour GET ${url}.`, { cause });
    }
    const body: unknown = text.length > 0 ? safeJsonParse(text) : undefined;

    if (!response.ok) {
      throw toApiError(response.status, body);
    }
    return body as T;
  }

  return {
    async request<T>(path: string, requestOptions?: RequestOptions): Promise<T> {
      const url = buildUrl(baseUrl, path, requestOptions?.query);
      let lastError: unknown;

      for (let attempt = 0; attempt <= maxRetries; attempt += 1) {
        if (attempt > 0) {
          await sleep(backoffMs(attempt - 1));
        }
        try {
          return await attemptOnce<T>(url);
        } catch (error) {
          lastError = error;
          if (attempt === maxRetries || !isRetryable(error)) {
            throw error;
          }
        }
      }
      throw lastError;
    },
  };
}

function isRetryable(error: unknown): boolean {
  if (error instanceof NetworkError) {
    return true;
  }
  return error instanceof ApiError && RETRYABLE_STATUS.has(error.status);
}

function buildUrl(
  baseUrl: string,
  path: string,
  query?: Record<string, string | number | undefined>,
): string {
  const url = new URL(baseUrl.replace(/\/+$/, '') + path);
  if (query) {
    for (const [key, value] of Object.entries(query)) {
      if (value !== undefined) {
        url.searchParams.set(key, String(value));
      }
    }
  }
  return url.toString();
}

function safeJsonParse(text: string): unknown {
  try {
    return JSON.parse(text);
  } catch {
    return undefined;
  }
}
