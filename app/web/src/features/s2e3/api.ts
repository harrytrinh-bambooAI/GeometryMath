import { apiRequest } from '../../shared/api/client.ts';
import { ApiError, isApiErrorBody, toErrorCode } from '../../shared/api/errors.ts';
import { buildUrl } from '../../shared/api/url.ts';
import { getToken } from '../../shared/auth/session.ts';
import type { AnswersView } from '../s2e5/api.ts';
import { CHUNK_BYTES } from './logic.ts';

/** S2-E3 · dữ liệu màn C2-02 "Thêm đề" (GET /api/v1/teacher/problem-drafts/{id}). */
export type DraftStatus = 'editing' | 'building' | 'ready' | 'ai_figure' | 'text_only';
export type StageState = 'waiting' | 'running' | 'done' | 'error';

export interface CellView {
  index: number;
  text: string;
  confidence: number;
  state: 'ok' | 'unsure' | 'fixed' | 'confirmed';
}

export interface StageView {
  key: 'read' | 'build' | 'solve' | 'verify';
  label: string;
  state: StageState;
  note?: string | null;
  error?: 'TIMEOUT' | 'ERROR' | 'UNBUILDABLE' | null;
  aiCalls: number;
}

export interface JobView {
  id: string;
  draftId: string;
  state: 'running' | 'done' | 'failed' | 'cancelled';
  stages: StageView[];
  retryable: boolean;
}

export interface ResultView {
  docId?: string | null;
  machineLabel: string;
  machineNote?: string | null;
  humanLabel: string;
  sceneKind?: 'plane' | 'space' | null;
  scene?: unknown;
  steps: string[];
  answers?: AnswersView | null;
  approveBlocked: boolean;
  blockedReason?: string | null;
}

export interface DraftView {
  id: string;
  source: 'text' | 'image';
  status: DraftStatus;
  statusLabel: string;
  statement: string;
  cells: CellView[];
  textConfirmed: boolean;
  canConfirmText: boolean;
  hasImage: boolean;
  notice?: string | null;
  machineLabel: string;
  inMyShelf: boolean;
  job?: JobView | null;
  result?: ResultView | null;
  assignable: boolean;
  assignNote?: string | null;
  assignConfirmed: boolean;
  createdAt: string;
}

export interface DraftSummary {
  id: string;
  title: string;
  status: DraftStatus;
  statusLabel: string;
  docId?: string | null;
  assignable: boolean;
  assignNote?: string | null;
  needsAssignConfirm: boolean;
  createdAt: string;
}

const BASE = '/api/v1/teacher/problem-drafts';
const JOBS = '/api/v1/teacher/problem-draft-jobs';
const one = (id: string) => `${BASE}/${encodeURIComponent(id)}`;

export const fetchCurrent = (signal?: AbortSignal) => apiRequest<DraftView | undefined>(`${BASE}/current`, { signal });
export const fetchDraft = (id: string, signal?: AbortSignal) => apiRequest<DraftView>(one(id), { signal });
export const fetchMine = (signal?: AbortSignal) => apiRequest<{ items: DraftSummary[] }>(BASE, { signal });
export const createDraft = (source: 'text' | 'image', statement = '') =>
  apiRequest<DraftView>(BASE, { method: 'POST', body: { source, statement } });
export const updateDraft = (id: string, body: { statement?: string; cells?: Array<{ text?: string; state?: string }> }) =>
  apiRequest<DraftView>(one(id), { method: 'PUT', body });
export const confirmText = (id: string) => apiRequest<DraftView>(`${one(id)}/confirm-text`, { method: 'POST', body: {} });
export const buildDraft = (id: string) => apiRequest<DraftView>(`${one(id)}/build`, { method: 'POST', body: {} });
export const supplement = (id: string, condition: string) =>
  apiRequest<DraftView>(`${one(id)}/supplement`, { method: 'POST', body: { condition } });
export const saveTextOnly = (id: string) => apiRequest<DraftView>(`${one(id)}/text-only`, { method: 'POST', body: {} });
export const assignDraft = (id: string, confirmAiFigure: boolean) =>
  apiRequest<DraftView>(`${one(id)}/assign`, { method: 'POST', body: { confirmAiFigure } });
export const fetchJob = (jobId: string, signal?: AbortSignal) =>
  apiRequest<JobView>(`${JOBS}/${encodeURIComponent(jobId)}`, { signal });
export const retryJob = (jobId: string) => apiRequest<JobView>(`${JOBS}/${encodeURIComponent(jobId)}/retry`, { method: 'POST', body: {} });
export const cancelJob = (jobId: string) => apiRequest<JobView>(`${JOBS}/${encodeURIComponent(jobId)}/cancel`, { method: 'POST', body: {} });

/**
 * Tải ảnh theo đoạn (thân yêu cầu của máy chủ ≤ 1 MB): PUT …/{id}/image?offset=&total= (application/octet-stream).
 * Đoạn cuối trả DraftView (chữ đã đọc, ô vàng). Lỗi ném ApiError như apiRequest.
 */
export async function uploadImage(id: string, file: Blob): Promise<DraftView> {
  const total = file.size;
  let last: unknown = undefined;
  for (let offset = 0; offset < total || (total === 0 && offset === 0); offset += CHUNK_BYTES) {
    const chunk = file.slice(offset, Math.min(total, offset + CHUNK_BYTES));
    const headers: Record<string, string> = { Accept: 'application/json', 'Content-Type': 'application/octet-stream' };
    const token = getToken();
    if (token) headers.Authorization = `Bearer ${token}`;
    let res: Response;
    try {
      res = await fetch(buildUrl(`${one(id)}/image`, { offset, total }), { method: 'PUT', headers, body: chunk });
    } catch {
      throw new ApiError({ code: 'NETWORK', status: 0 });
    }
    const text = await res.text();
    const data: unknown = text ? JSON.parse(text) : undefined;
    if (!res.ok) {
      throw isApiErrorBody(data)
        ? new ApiError({ status: res.status, code: toErrorCode(data.code, res.status), message: data.message, requestId: data.requestId, details: data.details })
        : new ApiError({ status: res.status, code: toErrorCode('', res.status) });
    }
    last = data;
    if (total === 0) break;
  }
  return last as DraftView;
}
