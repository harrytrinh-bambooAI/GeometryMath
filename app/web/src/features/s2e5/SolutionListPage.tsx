import { useEffect, useState } from 'react';
import { Link } from 'react-router';
import { ErrorNotice } from '../../shared/ui/ErrorNotice.tsx';
import { fetchDocs, type DocSummary } from './api.ts';
import './s2e5.css';

/** Danh sách đề đang soát lời giải của giáo viên (lối vào C2-03). */
export function SolutionListPage() {
  const [items, setItems] = useState<DocSummary[] | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [attempt, setAttempt] = useState(0);

  useEffect(() => {
    const ctl = new AbortController();
    fetchDocs(ctl.signal)
      .then((r) => {
        setItems(r.items);
        setError(null);
      })
      .catch((e: unknown) => {
        if (!(e instanceof DOMException && e.name === 'AbortError')) setError(e);
      });
    return () => ctl.abort();
  }, [attempt]);

  if (error) return <ErrorNotice error={error} onRetry={() => setAttempt((n) => n + 1)} testId="s2e5-list-error" />;
  if (!items) {
    return (
      <p className="muted" aria-busy="true" data-testid="s2e5-list-loading">
        Đang tải…
      </p>
    );
  }
  return (
    <div className="s2e5-listpage" data-testid="s2e5-list">
      <h1 className="s2e5-title">Soát và duyệt lời giải</h1>
      {items.length === 0 ? (
        <p className="empty" data-testid="s2e5-list-empty">
          Chưa có lời giải nào chờ soát.
        </p>
      ) : (
        <ul className="s2e5-list">
          {items.map((d) => (
            <li key={d.id}>
              <Link className="card s2e5-doc" to={d.id} data-testid="s2e5-list-item">
                <span className="s2e5-hist-title">
                  {d.code ? `${d.code} · ` : ''}
                  {d.title}
                </span>
                <span className="s2e5-sub muted">
                  Phiên bản v{d.latestVersion}
                  {d.inMyShelf ? ' · Của tôi' : ''}
                </span>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
