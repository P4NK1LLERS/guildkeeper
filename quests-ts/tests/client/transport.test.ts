import { describe, it, expect, vi } from 'vitest';
import { createTransport } from '../../src/client/transport';
import { ApiError, NetworkError, NotFoundError, ValidationError } from '../../src/client/errors';
import { jsonResponse, fetchCall, fetchedUrl } from './httpTestSupport';

const BASE = 'http://api.test';

describe('transport', () => {
  it('GETs the built URL with a JSON Accept header and returns the parsed body', async () => {
    // Arrange
    const fetchImpl = vi.fn().mockResolvedValue(jsonResponse([{ id: '1' }]));
    const transport = createTransport({ baseUrl: BASE, fetchImpl });

    // Act
    const result = await transport.request('/api/v1/quests');

    // Assert
    expect(fetchImpl).toHaveBeenCalledOnce();
    const [url, init] = fetchCall(fetchImpl);
    expect(url).toBe('http://api.test/api/v1/quests');
    expect(init?.method).toBe('GET');
    expect((init?.headers as Record<string, string>).Accept).toBe('application/json');
    expect(result).toEqual([{ id: '1' }]);
  });

  it('appends query parameters', async () => {
    // Arrange
    const fetchImpl = vi.fn().mockResolvedValue(jsonResponse({}));
    const transport = createTransport({ baseUrl: BASE, fetchImpl });

    // Act
    await transport.request('/api/v1/quests/3/reward-preview', { query: { luck: 5 } });

    // Assert
    expect(fetchedUrl(fetchImpl)).toBe('http://api.test/api/v1/quests/3/reward-preview?luck=5');
  });

  it('drops query parameters that are undefined', async () => {
    // Arrange
    const fetchImpl = vi.fn().mockResolvedValue(jsonResponse({}));
    const transport = createTransport({ baseUrl: BASE, fetchImpl });

    // Act
    await transport.request('/x', { query: { a: 1, b: undefined } });

    // Assert
    expect(fetchedUrl(fetchImpl)).toBe('http://api.test/x?a=1');
  });

  it('sends the configured extra headers', async () => {
    // Arrange
    const fetchImpl = vi.fn().mockResolvedValue(jsonResponse({}));
    const transport = createTransport({
      baseUrl: BASE,
      headers: { 'X-Api-Key': 'abc' },
      fetchImpl,
    });

    // Act
    await transport.request('/x');

    // Assert
    expect(fetchCall(fetchImpl)[1]?.headers).toMatchObject({ 'X-Api-Key': 'abc' });
  });

  it('maps 404 to NotFoundError and does not retry', async () => {
    // Arrange
    const fetchImpl = vi
      .fn()
      .mockResolvedValue(jsonResponse({ error: 'NOT_FOUND', message: 'nope' }, { status: 404 }));
    const transport = createTransport({ baseUrl: BASE, fetchImpl, backoffMs: () => 0 });

    // Act & Assert
    await expect(transport.request('/x')).rejects.toBeInstanceOf(NotFoundError);
    expect(fetchImpl).toHaveBeenCalledTimes(1);
  });

  it('maps 400 to ValidationError', async () => {
    // Arrange
    const fetchImpl = vi
      .fn()
      .mockResolvedValue(jsonResponse({ error: 'VALIDATION', message: 'bad' }, { status: 400 }));
    const transport = createTransport({ baseUrl: BASE, fetchImpl });

    // Act & Assert
    await expect(transport.request('/x')).rejects.toBeInstanceOf(ValidationError);
  });

  it('maps other 4xx to ApiError carrying status and code', async () => {
    // Arrange
    const fetchImpl = vi
      .fn()
      .mockResolvedValue(jsonResponse({ error: 'CONFLICT', message: 'busy' }, { status: 409 }));
    const transport = createTransport({ baseUrl: BASE, fetchImpl });

    // Act
    const error = await transport.request('/x').catch((e: unknown) => e);

    // Assert
    expect(error).toBeInstanceOf(ApiError);
    expect((error as ApiError).status).toBe(409);
    expect((error as ApiError).code).toBe('CONFLICT');
  });

  it('retries a 503 up to maxRetries then throws the last ApiError', async () => {
    // Arrange
    const fetchImpl = vi
      .fn()
      .mockImplementation(async () =>
        jsonResponse({ error: 'INTERNAL', message: 'x' }, { status: 503 }),
      );
    const transport = createTransport({
      baseUrl: BASE,
      fetchImpl,
      maxRetries: 2,
      backoffMs: () => 0,
    });

    // Act
    const error = await transport.request('/x').catch((e: unknown) => e);

    // Assert
    expect(fetchImpl).toHaveBeenCalledTimes(3);
    expect((error as ApiError).status).toBe(503);
  });

  it('retries then succeeds when a later attempt is OK', async () => {
    // Arrange
    const fetchImpl = vi
      .fn()
      .mockResolvedValueOnce(jsonResponse({ error: 'INTERNAL', message: 'x' }, { status: 503 }))
      .mockResolvedValueOnce(jsonResponse({ ok: true }));
    const transport = createTransport({ baseUrl: BASE, fetchImpl, backoffMs: () => 0 });

    // Act & Assert
    await expect(transport.request('/x')).resolves.toEqual({ ok: true });
    expect(fetchImpl).toHaveBeenCalledTimes(2);
  });

  it('wraps a fetch rejection in NetworkError and retries it', async () => {
    // Arrange
    const fetchImpl = vi.fn().mockRejectedValue(new TypeError('fetch failed'));
    const transport = createTransport({
      baseUrl: BASE,
      fetchImpl,
      maxRetries: 1,
      backoffMs: () => 0,
    });

    // Act
    const error = await transport.request('/x').catch((e: unknown) => e);

    // Assert
    expect(error).toBeInstanceOf(NetworkError);
    expect(fetchImpl).toHaveBeenCalledTimes(2);
  });

  it('aborts on timeout and reports it as a NetworkError', async () => {
    // Arrange
    const fetchImpl = vi.fn(
      (_url: string | URL | Request, init?: RequestInit) =>
        new Promise<Response>((_resolve, reject) => {
          init?.signal?.addEventListener('abort', () =>
            reject(new DOMException('aborted', 'AbortError')),
          );
        }),
    );
    const transport = createTransport({ baseUrl: BASE, fetchImpl, timeoutMs: 10, maxRetries: 0 });

    // Act
    const error = await transport.request('/x').catch((e: unknown) => e);

    // Assert
    expect(error).toBeInstanceOf(NetworkError);
    expect((error as NetworkError).message).toContain('Delai depasse');
  });
});
