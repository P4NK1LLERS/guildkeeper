import type {
  AssignmentDto,
  GuildStatusDto,
  MemberDto,
  QuestDto,
  RewardPreviewDto,
} from './dto.ts';
import { createTransport, type Transport } from './transport.ts';

/**
 * Client HTTP de l'API GuildKeeper. **Lecture seule** : les mutations (recruter,
 * assigner, ...) passent deliberement ailleurs (voir `docs/architecture.md`).
 */

export interface GuildKeeperClientOptions {
  /** Racine de l'API, ex. `http://localhost:7070`. Obligatoire, jamais en dur ici. */
  baseUrl: string;
  timeoutMs?: number;
  headers?: Record<string, string>;
  /** `fetch` a utiliser (tests). */
  fetchImpl?: typeof fetch;
}

export interface GuildKeeperClient {
  quests: {
    list(): Promise<QuestDto[]>;
    get(id: string): Promise<QuestDto>;
    rewardPreview(id: string, luck: number): Promise<RewardPreviewDto>;
  };
  members: {
    list(): Promise<MemberDto[]>;
    get(name: string): Promise<MemberDto>;
    assignments(name: string): Promise<AssignmentDto[]>;
  };
  guild(): Promise<GuildStatusDto>;
}

export function createGuildKeeperClient(options: GuildKeeperClientOptions): GuildKeeperClient {
  const transport: Transport = createTransport(options);
  const segment = (value: string): string => encodeURIComponent(value);

  return {
    quests: {
      list: () => transport.request<QuestDto[]>('/api/v1/quests'),
      get: (id) => transport.request<QuestDto>(`/api/v1/quests/${segment(id)}`),
      rewardPreview: (id, luck) =>
        transport.request<RewardPreviewDto>(`/api/v1/quests/${segment(id)}/reward-preview`, {
          query: { luck },
        }),
    },
    members: {
      list: () => transport.request<MemberDto[]>('/api/v1/members'),
      get: (name) => transport.request<MemberDto>(`/api/v1/members/${segment(name)}`),
      assignments: (name) =>
        transport.request<AssignmentDto[]>(`/api/v1/members/${segment(name)}/assignments`),
    },
    guild: () => transport.request<GuildStatusDto>('/api/v1/guild'),
  };
}
