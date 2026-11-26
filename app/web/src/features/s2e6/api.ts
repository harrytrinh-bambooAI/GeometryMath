import { api } from '../../shared/api/client.ts';

/** S2-E6 · Kho đề (C2-01): ngăn Của tôi / Kho chung, hàng đợi người soát (GET/POST /api/v1/teacher/library…). */
export type Shelf = 'mine' | 'library';
export type KindFilter = 'all' | 'space' | 'plane';
export type ShelfStatus = 'mine' | 'pending' | 'accepted' | 'rejected';

export interface Card {
  id: string;
  shelf: Shelf;
  title: string;
  kind: 'space' | 'plane';
  kindLabel: string;
  machineLabel: string;
  humanLabel: string;
  status: ShelfStatus;
  statusLabel: string;
  reason?: string | null;
  createdAt: string;
}

export interface CardPage {
  items: Card[];
  nextCursor?: string | null;
  reviewer: boolean;
}

export interface HistoryRow {
  status: ShelfStatus;
  statusLabel: string;
  reason?: string | null;
  at: string;
}

export interface DocDetail {
  id: string;
  title: string;
  statement: string;
  kind: 'space' | 'plane';
  kindLabel: string;
  machineLabel: string;
  humanLabel: string;
  status: ShelfStatus;
  statusLabel: string;
  reason?: string | null;
  owner: boolean;
  canSubmit: boolean;
  submitBlockedReason?: string | null;
  history: HistoryRow[];
}

export interface LibraryDetail {
  id: string;
  title: string;
  statement: string;
  kind: 'space' | 'plane';
  kindLabel: string;
  machineLabel: string;
  humanLabel: string;
  hint: string;
  steps: string[];
  acceptedAt: string;
}

export interface QueueRow {
  submissionId: string;
  docId: string;
  title: string;
  kindLabel: string;
  ownerName: string;
  mine: boolean;
  submittedAt: string;
}

export interface QueueDetail {
  submissionId: string;
  docId: string;
  title: string;
  statement: string;
  kindLabel: string;
  machineLabel: string;
  humanLabel: string;
  steps: string[];
  mine: boolean;
}

export interface Decision {
  docId: string;
  status: ShelfStatus;
  statusLabel: string;
  libraryId?: string | null;
}

const BASE = '/api/v1/teacher/library';

export const libraryApi = {
  list: (shelf: Shelf, kind: KindFilter, cursor?: string | null, signal?: AbortSignal) =>
    api.get<CardPage>(BASE, { query: { shelf, kind, limit: 20, ...(cursor ? { cursor } : {}) }, signal }),
  doc: (id: string, signal?: AbortSignal) => api.get<DocDetail>(`${BASE}/problems/${encodeURIComponent(id)}`, { signal }),
  submit: (id: string) => api.post<Decision>(`${BASE}/problems/${encodeURIComponent(id)}/submit`, {}),
  shared: (id: string, signal?: AbortSignal) => api.get<LibraryDetail>(`${BASE}/shared/${encodeURIComponent(id)}`, { signal }),
  copy: (id: string) => api.post<{ id: string; title: string }>(`${BASE}/shared/${encodeURIComponent(id)}/copy`, {}),
  queue: (signal?: AbortSignal) => api.get<{ items: QueueRow[] }>(`${BASE}/review-queue`, { signal }),
  queueItem: (id: string, signal?: AbortSignal) =>
    api.get<QueueDetail>(`${BASE}/review-queue/${encodeURIComponent(id)}`, { signal }),
  accept: (id: string) => api.post<Decision>(`${BASE}/review-queue/${encodeURIComponent(id)}/accept`, {}),
  reject: (id: string, reason: string) =>
    api.post<Decision>(`${BASE}/review-queue/${encodeURIComponent(id)}/reject`, { reason }),
};
