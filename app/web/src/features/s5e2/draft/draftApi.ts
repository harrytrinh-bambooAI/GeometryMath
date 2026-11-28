import { api } from '../../../shared/api/client.ts';

/** S5-E2 · trợ lý soạn đề nháp từ tài liệu Đang dùng (US-S5E2-06, luồng D, C5-08). T-261128-63. */

export interface DraftItem {
  id: string;
  batchId: string;
  ord: number;
  statement: string;
  sourceLabel: string;
  sourceSection: string | null;
  source: string;
  machineLabel: 'Đã kiểm chứng' | 'Cần xem lại';
  machineState: 'verified' | 'needs_review';
  reviewReason: string | null;
  humanLabel: 'AI soạn' | 'Giáo viên đã duyệt';
  status: 'draft' | 'approved';
  preselected: boolean;
  answer: string | null;
  answerSource: 'engine' | null;
  aiAnswer: string | null;
  approvedAt: string | null;
}

export interface DraftBatch {
  id: string;
  classId: string;
  source: { docId: string; docCode: string; version: number; label: string };
  requested: number;
  items: DraftItem[];
  message: string | null;
}

const BASE = '/api/copilot/exam-drafts';

export const createDrafts = (docId: string, classId: string, count: number) =>
  api.post<DraftBatch>(BASE, { docId, classId, count });
export const getDrafts = (id: string) => api.get<DraftBatch>(`${BASE}/${encodeURIComponent(id)}`);
export const approveDraft = (itemId: string) =>
  api.post<DraftItem>(`${BASE}/items/${encodeURIComponent(itemId)}/approve`, {});
export const myDraftBank = () => api.get<{ items: DraftItem[] }>(`${BASE}/bank`);
