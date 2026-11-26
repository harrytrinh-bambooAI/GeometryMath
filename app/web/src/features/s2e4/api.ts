import { apiRequest } from '../../shared/api/client.ts';
import { ApiError, isApiErrorBody, toErrorCode } from '../../shared/api/errors.ts';
import { buildUrl } from '../../shared/api/url.ts';
import { getToken } from '../../shared/auth/session.ts';
import type { DraftView } from '../s2e3/api.ts';
import { CHUNK_BYTES, type ItemState } from './logic.ts';

/** S2-E4 · màn C2-04a "Chỉnh nháp với AI" (GET /api/v1/teacher/problem-drafts/{id}/chat). */
export interface ProposalView {
  target: 'statement' | 'answer';
  before: string;
  after: string;
  source: string;
  unsure?: string | null;
  state: 'pending' | 'accepted' | 'rejected';
  note?: string | null;
}

export interface MessageView {
  id: string;
  seq: number;
  role: 'teacher' | 'ai' | 'system';
  kind: 'message' | 'question' | 'proposal' | 'note';
  text: string;
  proposal?: ProposalView | null;
  at: string;
}

export interface ChatView {
  draft: DraftView;
  machineLabel: string;
  humanLabel: string;
  saved: boolean;
  messages: MessageView[];
  turns: number;
  maxTurns: number;
  canSend: boolean;
  sendNote?: string | null;
  undoSteps: number;
  canUndo: boolean;
  undoNote?: string | null;
}

const chatBase = (id: string) => `/api/v1/teacher/problem-drafts/${encodeURIComponent(id)}/chat`;

export const fetchChat = (id: string, signal?: AbortSignal) => apiRequest<ChatView>(chatBase(id), { signal });
export const sendMessage = (id: string, text: string) => apiRequest<ChatView>(`${chatBase(id)}/messages`, { method: 'POST', body: { text } });
export const acceptProposal = (id: string, messageId: string, after?: string) =>
  apiRequest<ChatView>(`${chatBase(id)}/proposals/${encodeURIComponent(messageId)}/accept`, { method: 'POST', body: after === undefined ? {} : { after } });
export const rejectProposal = (id: string, messageId: string) =>
  apiRequest<ChatView>(`${chatBase(id)}/proposals/${encodeURIComponent(messageId)}/reject`, { method: 'POST', body: {} });
export const undoEdit = (id: string) => apiRequest<ChatView>(`${chatBase(id)}/undo`, { method: 'POST', body: {} });
export const recheck = (id: string) => apiRequest<ChatView>(`${chatBase(id)}/recheck`, { method: 'POST', body: {} });

/** S2-E4 · màn C2-04b "Nhập từ tệp" (POST /api/v1/teacher/problem-imports). */
export interface ImportItem {
  index: number;
  title: string;
  text: string;
  state: ItemState;
  note?: string | null;
  refKind?: 'library' | 'mine' | 'draft' | null;
  refId?: string | null;
  checked: boolean;
  selectable: boolean;
  draftId?: string | null;
  humanLabel?: string | null;
  machineLabel?: string | null;
}

export interface ImportView {
  id: string;
  fileName: string;
  fileType?: 'docx' | 'pdf' | null;
  status: 'uploading' | 'parsed' | 'rejected';
  sizeBytes: number;
  received: number;
  found: number;
  notes: string[];
  error?: string | null;
  items: ImportItem[];
  created: number;
  updated: number;
  skipped: number;
}

const IMPORTS = '/api/v1/teacher/problem-imports';

export const createDrafts = (id: string, indexes: number[]) =>
  apiRequest<ImportView>(`${IMPORTS}/${encodeURIComponent(id)}/drafts`, { method: 'POST', body: { indexes } });

/**
 * Tạo lượt import rồi gửi tệp theo đoạn (thân yêu cầu của máy chủ ≤ 1 MB): PUT …/{id}/file?offset=&total=
 * (application/octet-stream). Đoạn cuối trả các mục đã tách. Lỗi ném ApiError như apiRequest.
 */
export async function importFile(file: File): Promise<ImportView> {
  const job = await apiRequest<ImportView>(IMPORTS, { method: 'POST', body: { fileName: file.name, sizeBytes: file.size } });
  const total = file.size;
  let last: ImportView = job;
  for (let offset = 0; offset < total; offset += CHUNK_BYTES) {
    const chunk = file.slice(offset, Math.min(total, offset + CHUNK_BYTES));
    const headers: Record<string, string> = { Accept: 'application/json', 'Content-Type': 'application/octet-stream' };
    const token = getToken();
    if (token) headers.Authorization = `Bearer ${token}`;
    let res: Response;
    try {
      res = await fetch(buildUrl(`${IMPORTS}/${encodeURIComponent(job.id)}/file`, { offset, total }), { method: 'PUT', headers, body: chunk });
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
    last = data as ImportView;
  }
  return last;
}
