import type { Mock } from 'vitest';

/** Fabrique une `Response` JSON pour un `fetch` mocke. */
export function jsonResponse(body: unknown, init: { status?: number } = {}): Response {
  return new Response(JSON.stringify(body), {
    status: init.status ?? 200,
    headers: { 'Content-Type': 'application/json' },
  });
}

/** Arguments (`[url, init]`) du n-ieme appel au `fetch` mocke. Echoue si absent. */
export function fetchCall(fetchImpl: Mock, index = 0): [string, RequestInit | undefined] {
  const call = fetchImpl.mock.calls[index];
  if (!call) {
    throw new Error(`fetch n'a pas ete appele ${index + 1} fois`);
  }
  return [call[0] as string, call[1] as RequestInit | undefined];
}

/** Raccourci : URL du n-ieme appel au `fetch` mocke. */
export function fetchedUrl(fetchImpl: Mock, index = 0): string {
  return fetchCall(fetchImpl, index)[0];
}

export interface Route {
  match: RegExp;
  body: unknown;
  status?: number;
}

/**
 * `fetch` factice qui route par chemin (query comprise). Renvoie un 404
 * `NOT_FOUND` si aucune route ne correspond.
 */
export function routingFetch(routes: Route[]): typeof fetch {
  return async (input) => {
    const path = String(input).replace(/^https?:\/\/[^/]+/, '');
    for (const route of routes) {
      if (route.match.test(path)) {
        return jsonResponse(route.body, { status: route.status ?? 200 });
      }
    }
    return jsonResponse(
      { error: 'NOT_FOUND', message: `route absente : ${path}` },
      { status: 404 },
    );
  };
}
