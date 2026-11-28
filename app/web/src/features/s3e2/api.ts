import { useEffect, useState } from 'react';
import { apiRequest } from '../../shared/api/client.ts';
import { ApiError } from '../../shared/api/errors.ts';

/** S3-E2 — máy khách API bộ bài (C3-03 soạn, C3-08 Bài của em theo bộ). Dữ liệu học sinh không có đáp số. */

const enc = encodeURIComponent;

export type SetStatus = 'draft' | 'assigned' | 'closed' | 'archived';
export type ReleaseState = 'unset' | 'upcoming' | 'open';
export type ItemState = 'correct' | 'review' | 'doing' | 'todo';

export interface Candidate {
  problemId: string;
  problemVersionId: string;
  code: string;
  title: string;
  statement: string;
  needsReview: boolean;
  gradable: boolean;
}
export interface Options {
  classes: Array<{ id: string; name: string }>;
  problems: Candidate[];
  serverNow: string;
}
export interface ItemView {
  position: number;
  problemId: string;
  problemVersionId: string;
  code: string;
  title: string;
  statement: string;
  needsReview: boolean;
  ownReleaseAt?: string | null;
  releaseAt?: string | null;
  releaseState: ReleaseState;
  assignmentId?: string | null;
}
export interface SetDetail {
  id: string;
  title: string;
  status: SetStatus;
  classId?: string | null;
  className?: string | null;
  dueAt?: string | null;
  overdue: boolean;
  releaseAt?: string | null;
  releaseBeforeDue: boolean;
  scheduleVersion: number;
  needsReviewCount: number;
  items: ItemView[];
  serverNow: string;
}
export interface SetLine {
  id: string;
  title: string;
  status: SetStatus;
  className?: string | null;
  dueAt?: string | null;
  overdue: boolean;
  releaseAt?: string | null;
  itemCount: number;
  needsReviewCount: number;
}
export interface SetInput {
  title: string;
  classId: string | null;
  dueAt: string | null;
  releaseAt: string | null;
  items: Array<{ problemVersionId: string; releaseAt: string | null }>;
}
export interface PreviewItem {
  position: number;
  statement: string;
  releaseAt?: string | null;
  releaseState: ReleaseState;
  hint?: string | null;
}
export interface Preview {
  id: string;
  title: string;
  className?: string | null;
  dueAt?: string | null;
  overdue: boolean;
  items: PreviewItem[];
}
export interface PreviewCheck {
  outcome: 'correct' | 'wrong' | 'syntax';
  canonical?: string | null;
  reason?: string | null;
  hint?: string | null;
}
export interface StudentSet {
  id: string;
  title: string;
  status: SetStatus;
  dueAt?: string | null;
  overdue: boolean;
  releaseAt?: string | null;
  releaseState: ReleaseState;
  done: number;
  total: number;
}
export interface StudentHome {
  groups: Array<{ classId: string; className: string; sets: StudentSet[] }>;
  serverNow: string;
}
export interface StudentItem {
  position: number;
  assignmentId: string;
  statement: string;
  releaseAt?: string | null;
  releaseState: ReleaseState;
  state: ItemState;
}
export interface StudentSetDetail extends StudentSet {
  className: string;
  items: StudentItem[];
}
export interface SolutionView {
  position: number;
  releaseState: ReleaseState;
  releaseAt?: string | null;
  finalAnswer?: string | null;
  steps?: Array<{ title?: string; body?: string }> | null;
}

const T = '/api/v1/teacher/assignment-sets';

export const setsApi = {
  options: (signal?: AbortSignal) => apiRequest<Options>(`${T}/options`, { signal }),
  list: (view: 'active' | 'archived', signal?: AbortSignal) =>
    apiRequest<{ items: SetLine[] }>(T, { query: { view }, signal }),
  get: (id: string, signal?: AbortSignal) => apiRequest<SetDetail>(`${T}/${enc(id)}`, { signal }),
  create: (body: SetInput) => apiRequest<SetDetail>(T, { method: 'POST', body }),
  update: (id: string, body: SetInput) => apiRequest<SetDetail>(`${T}/${enc(id)}`, { method: 'PUT', body }),
  assign: (id: string, requestKey: string, confirmPastRelease = false) =>
    apiRequest<SetDetail>(`${T}/${enc(id)}/assign`, { method: 'POST', body: { requestKey, confirmPastRelease } }),
  close: (id: string, requestKey: string) =>
    apiRequest<SetDetail>(`${T}/${enc(id)}/close`, { method: 'POST', body: { requestKey } }),
  archive: (id: string, requestKey: string) =>
    apiRequest<SetDetail>(`${T}/${enc(id)}/archive`, { method: 'POST', body: { requestKey } }),
  preview: (id: string, signal?: AbortSignal) => apiRequest<Preview>(`${T}/${enc(id)}/preview`, { signal }),
  previewCheck: (id: string, position: number, answer: string) =>
    apiRequest<PreviewCheck>(`/api/v1/teacher/practice-sets/${enc(id)}/preview/check`, {
      method: 'POST',
      body: { position, answer },
    }),
  home: (signal?: AbortSignal) => apiRequest<StudentHome>('/api/v1/student/assignment-sets', { signal }),
  mine: (id: string, signal?: AbortSignal) =>
    apiRequest<StudentSetDetail>(`/api/v1/student/assignment-sets/${enc(id)}`, { signal }),
  solution: (id: string, position: number) =>
    apiRequest<SolutionView>(`/api/v1/student/assignment-sets/${enc(id)}/items/${position}/solution`),
};

/** Thông điệp MSG-S3-21…28 (stories_ac.md S3-E2) và MSG-S3-97 (S3-E6). */
export const MSG = {
  s21: 'Giờ mở lời giải sớm hơn hạn làm. Em sẽ thấy lời giải trước khi hết hạn.',
  s22: 'Bộ có đề đang ở trạng thái Cần xem lại. Thầy cô xem lại trước khi giao.',
  s23: 'Bộ bài cần từ 1 đến 20 đề.',
  s24: 'Đề này đã có trong bộ.',
  s25: 'Chưa chọn lớp để giao.',
  s26: 'Lời giải đã mở, không đổi giờ được.',
  s27: 'Chưa có bài nào. Khi thầy cô giao bài, bài sẽ hiện ở đây.',
  s28: 'Quá hạn',
  /** MSG-S3-97 (PO-70, BR-S3E6-14): máy chủ trả CONFLICT kèm câu này khi giao vào lớp đã lưu trữ; giao diện hiện nguyên văn. */
  s97: 'Lớp này đã lưu trữ, không giao bộ mới được.',
} as const;

export const STATUS_LABEL: Record<SetStatus, string> = {
  draft: 'Nháp',
  assigned: 'Đã giao',
  closed: 'Đã đóng',
  archived: 'Lưu trữ',
};
export const RELEASE_LABEL: Record<ReleaseState, string> = {
  unset: 'Chưa đặt giờ',
  upcoming: 'Sắp mở',
  open: 'Đã mở',
};
export const ITEM_LABEL: Record<ItemState, string> = {
  correct: 'Đúng',
  review: 'Xem lại',
  doing: 'Đang làm',
  todo: 'Chưa làm',
};

export const newKey = () =>
  typeof crypto !== 'undefined' && 'randomUUID' in crypto ? crypto.randomUUID() : `k-${Date.now()}-${Math.random()}`;

export const errorText = (e: unknown, fallback: string) => (e instanceof ApiError ? e.message : fallback);

export type Load<T> = { state: 'loading' } | { state: 'error'; notFound: boolean } | { state: 'ok'; data: T };

/** Tải một tài nguyên theo khóa; gọi hàm trả về ("Thử lại") thì tải lại. */
export function useLoad<T>(key: string, fetcher: (signal: AbortSignal) => Promise<T>): [Load<T>, () => void] {
  const [result, setResult] = useState<{ key: string; load: Load<T> } | null>(null);
  const [attempt, setAttempt] = useState(0);
  const k = `${key}#${attempt}`;
  useEffect(() => {
    const ctl = new AbortController();
    fetcher(ctl.signal)
      .then((data) => setResult({ key: k, load: { state: 'ok', data } }))
      .catch((e: unknown) => {
        if (e instanceof DOMException && e.name === 'AbortError') return;
        setResult({ key: k, load: { state: 'error', notFound: e instanceof ApiError && e.code === 'NOT_FOUND' } });
      });
    return () => ctl.abort();
    // eslint-disable-next-line react-hooks/exhaustive-deps -- fetcher chỉ phụ thuộc khóa k
  }, [k]);
  return [result?.key === k ? result.load : { state: 'loading' }, () => setAttempt((n) => n + 1)];
}

export { dueLabel, fromLocalInput, releaseBeforeDue, toLocalInput } from './format.ts';
