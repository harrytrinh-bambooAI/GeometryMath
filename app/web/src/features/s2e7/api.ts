import { api } from '../../shared/api/client.ts';
import type { Scene } from '../../shared/scene/types.ts';

/** S2-E7 · dữ liệu Trình chiếu (GET /api/v1/teacher/present/{id}, no-store, chỉ giữ trong bộ nhớ trang). */
export interface Presentation {
  id: string;
  title: string;
  statement: string;
  kind: 'space' | 'plane';
  scene?: Scene | null;
  steps: string[];
  hasSolution: boolean;
  machineLabel: string;
  humanLabel: string;
  lockedClasses: string[];
  reconnectSeconds: number;
}

export interface Preflight {
  ask: boolean;
  lockedClasses: string[];
  serverNow: string;
}

const BASE = '/api/v1/teacher/present';

export const presentApi = {
  preflight: (id: string, signal?: AbortSignal) => api.get<Preflight>(`${BASE}/${encodeURIComponent(id)}/preflight`, { signal }),
  load: (id: string, signal?: AbortSignal) => api.get<Presentation>(`${BASE}/${encodeURIComponent(id)}`, { signal }),
};

/** Đường dẫn màn đề (Kho đề › đề) để quay về khi thoát hoặc hủy. */
export const docPath = (id: string) => `/gv/kho-de-cua-toi/de/${encodeURIComponent(id)}`;
