import { useEffect, useState } from 'react';

export type Fetched<T> = { state: 'loading' } | { state: 'error'; error: unknown } | { state: 'ok'; data: T };

/** Tải một tài nguyên theo khóa (mẫu useFetch của S2-E6); gọi reload() để tải lại. */
export function useFetch<T>(key: string, fetcher: (signal: AbortSignal) => Promise<T>): [Fetched<T>, () => void] {
  const [result, setResult] = useState<{ key: string; value: Fetched<T> } | null>(null);
  const [attempt, setAttempt] = useState(0);
  const k = `${key}#${attempt}`;
  useEffect(() => {
    const ctl = new AbortController();
    fetcher(ctl.signal)
      .then((data) => setResult({ key: k, value: { state: 'ok', data } }))
      .catch((e: unknown) => {
        if (e instanceof DOMException && e.name === 'AbortError') return;
        setResult({ key: k, value: { state: 'error', error: e } });
      });
    return () => ctl.abort();
    // eslint-disable-next-line react-hooks/exhaustive-deps -- fetcher chỉ phụ thuộc khóa k
  }, [k]);
  return [result?.key === k ? result.value : { state: 'loading' }, () => setAttempt((n) => n + 1)];
}
