import { useEffect, useState } from 'react';
import { ApiError } from '../../shared/api/errors.ts';

export type Load<T> = { state: 'loading' } | { state: 'error'; notFound: boolean } | { state: 'ok'; data: T };

export const STATUS_LABEL: Record<string, string> = { draft: 'Nháp', assigned: 'Đã giao', closed: 'Đã đóng' };

/** Tải một tài nguyên theo khóa; đổi khóa hoặc gọi hàm trả về ("Thử lại") thì tải lại. */
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
