import { apiRequest } from '../../shared/api/client.ts';
import type { ExportView } from './exportModel.ts';

/** S2-E9 — máy khách API: biến thể đề (US-S2E9-01), xuất ảnh/PDF và liên kết chỉ xem (US-S2E9-02). */

const enc = encodeURIComponent;

/** Một đề "Của tôi" trong danh sách chọn để xuất. */
export interface DocOption {
  id: string;
  title: string;
  kind: 'plane' | 'space';
  machineLabel: string;
  humanLabel: string;
  /** có phiên bản "Giáo viên đã duyệt" nên được kèm lời giải */
  canAttachSolution: boolean;
  /** đang giao cho lớp mà lời giải còn khóa: xuất kèm lời giải phải xác nhận */
  lockedAssigned: boolean;
  variant: boolean;
}

export interface VariantItem {
  id: string;
  docId: string;
  sourceDocId: string;
  title: string;
  statement: string;
  seed: number;
  ordinal: number;
  params: Array<{ name: string; value: string }>;
  answer: string;
  approx: string | null;
  answerSource: 'engine';
  machineLabel: string;
  humanLabel: string;
  createdAt: string;
}

export interface VariantBatch {
  requested: number;
  created: number;
  seed: number;
  message: string | null;
  items: VariantItem[];
}

export interface ShareLink {
  id: string;
  docId: string;
  /** chỉ có ở phản hồi tạo mới: máy chủ không lưu mã nên không xem lại được */
  token: string | null;
  hint: string;
  days: 1 | 7 | 30;
  daysLabel: string;
  createdAt: string;
  expiresAt: string;
}

export const s2e9Api = {
  docs: (signal?: AbortSignal) => apiRequest<{ items: DocOption[] }>('/api/v1/teacher/exports/problems', { signal }),
  exportDocs: (docIds: string[], withSolution: boolean, confirmLocked: boolean, signal?: AbortSignal) =>
    apiRequest<ExportView>('/api/v1/teacher/exports', {
      method: 'POST',
      body: { docIds, withSolution, confirmLocked },
      signal,
    }),
  variants: (sourceId: string, signal?: AbortSignal) =>
    apiRequest<{ items: VariantItem[] }>(`/api/v1/teacher/variants?sourceId=${enc(sourceId)}`, { signal }),
  createVariants: (docId: string, count: number, seed: number | null) =>
    apiRequest<VariantBatch>('/api/v1/teacher/variants', { method: 'POST', body: { docId, count, seed } }),
  links: (docId: string, signal?: AbortSignal) =>
    apiRequest<{ items: ShareLink[] }>(`/api/v1/teacher/share-links?docId=${enc(docId)}`, { signal }),
  createLink: (docId: string, days: number) =>
    apiRequest<ShareLink>('/api/v1/teacher/share-links', { method: 'POST', body: { docId, days } }),
  revokeLink: (id: string) => apiRequest<void>(`/api/v1/teacher/share-links/${enc(id)}`, { method: 'DELETE' }),
};

/** Địa chỉ đầy đủ của liên kết chỉ xem (mở bằng trình duyệt không cần đăng nhập). */
export const shareUrl = (origin: string, token: string): string => `${origin}/api/v1/share/${token}`;
