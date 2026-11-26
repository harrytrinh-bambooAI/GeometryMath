import { useEffect, useState } from 'react';
import { apiRequest } from '../../shared/api/client.ts';

/**
 * S3-E3 — máy khách API Kết quả lớp (C3-04). Thân phản hồi không có tổng, phần trăm, điểm hay hạng (BR-S3E3-03);
 * mỗi hàng chỉ có tên hiển thị, mã người dùng, cờ "đã rời" và trạng thái từng ô.
 */

const enc = encodeURIComponent;
const BASE = '/api/v1/teacher/class-results';

export type * from './types.ts';
import type { Board, ClassOption, Evidence, Sets, Stuck } from './types.ts';

const setPath = (classId: string, setId: string) => `${BASE}/${enc(classId)}/sets/${enc(setId)}`;

export const resultsApi = {
  classes: (signal?: AbortSignal) => apiRequest<{ items: ClassOption[] }>(`${BASE}/classes`, { signal }),
  sets: (classId: string, signal?: AbortSignal) => apiRequest<Sets>(`${BASE}/${enc(classId)}/sets`, { signal }),
  board: (classId: string, setId: string, cursor?: string | null, signal?: AbortSignal) =>
    apiRequest<Board>(`${setPath(classId, setId)}/board`, { query: cursor ? { cursor } : {}, signal }),
  evidence: (classId: string, setId: string, item: number, userCode: string, work: boolean, signal?: AbortSignal) =>
    apiRequest<Evidence>(`${setPath(classId, setId)}/evidence`, { query: { item, userCode, work }, signal }),
  stuck: (classId: string, setId: string, signal?: AbortSignal) =>
    apiRequest<Stuck>(`${setPath(classId, setId)}/stuck`, { signal }),
};

export type Load<T> = { state: 'loading' } | { state: 'error' } | { state: 'ok'; data: T };

/** Tải một tài nguyên theo khóa; khóa null = chưa tải. Hàm trả về ("Thử lại" / "Làm mới") tải lại. */
export function useLoad<T>(key: string | null, fetcher: (signal: AbortSignal) => Promise<T>): [Load<T>, () => void] {
  const [result, setResult] = useState<{ key: string; load: Load<T> } | null>(null);
  const [attempt, setAttempt] = useState(0);
  const k = key === null ? null : `${key}#${attempt}`;
  useEffect(() => {
    if (k === null) return;
    const ctl = new AbortController();
    fetcher(ctl.signal)
      .then((data) => setResult({ key: k, load: { state: 'ok', data } }))
      .catch((e: unknown) => {
        if (e instanceof DOMException && e.name === 'AbortError') return;
        setResult({ key: k, load: { state: 'error' } });
      });
    return () => ctl.abort();
    // eslint-disable-next-line react-hooks/exhaustive-deps -- fetcher chỉ phụ thuộc khóa k
  }, [k]);
  return [result && result.key === k ? result.load : { state: 'loading' }, () => setAttempt((n) => n + 1)];
}
