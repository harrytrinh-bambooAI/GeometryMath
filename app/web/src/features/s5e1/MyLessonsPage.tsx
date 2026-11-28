import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router';
import { ErrorNotice } from '../../shared/ui/ErrorNotice.tsx';
import { Icon } from '../../shared/ui/Icon.tsx';
import { fetchMyLessons, type MyLesson } from './api.ts';
import { versionLine } from './logic.ts';

type Shelf = 'approved' | 'draft' | null;
const SHELVES: Array<{ id: Shelf; label: string }> = [
  { id: null, label: 'Tất cả' },
  { id: 'approved', label: 'Đã duyệt' },
  { id: 'draft', label: 'Bản nháp' },
];

/** "Giáo án của tôi" (US-S5E1-05): ngăn Đã duyệt / Bản nháp, mới sửa trước; mở màn kết quả để sửa, duyệt, xuất. */
export function MyLessonsPage() {
  const [shelf, setShelf] = useState<Shelf>(null);
  const [items, setItems] = useState<MyLesson[] | null>(null);
  const [error, setError] = useState<unknown>(null);

  const load = useCallback(() => {
    const ctl = new AbortController();
    fetchMyLessons(shelf, ctl.signal)
      .then((r) => {
        setError(null);
        setItems(r.items);
      })
      .catch((e: unknown) => {
        if (!(e instanceof DOMException)) setError(e);
      });
    return () => ctl.abort();
  }, [shelf]);
  useEffect(load, [load]);

  return (
    <div className="s5e1-page" data-testid="s5e1-my-lessons">
      <header className="s5e1-row">
        <h1 className="s5e1-title">Giáo án của tôi</h1>
        <span className="s5e1-grow" />
        <Link className="btn primary" to="/gv/giao-an/moi" data-testid="s5e1-new">
          <Icon name="plus" />
          Soạn giáo án mới
        </Link>
      </header>
      <div className="s5e1-row" role="tablist" aria-label="Ngăn giáo án">
        {SHELVES.map((s) => (
          <button
            key={s.label}
            type="button"
            role="tab"
            aria-selected={shelf === s.id}
            className={`btn ghost s5e1-pick${shelf === s.id ? ' on' : ''}`}
            data-testid={`s5e1-shelf-${s.id ?? 'all'}`}
            onClick={() => setShelf(s.id)}
          >
            {s.label}
          </button>
        ))}
      </div>
      {error !== null && <ErrorNotice error={error} onRetry={load} testId="s5e1-my-error" />}
      {items === null && error === null && (
        <p className="muted" aria-busy="true">
          Đang tải…
        </p>
      )}
      {items?.length === 0 && (
        <p className="card muted" data-testid="s5e1-my-empty">
          Chưa có giáo án nào ở ngăn này.
        </p>
      )}
      {items && items.length > 0 && (
        <ul className="s5e1-list">
          {items.map((i) => (
            <li key={i.draftId} className="card s5e1-row" data-testid={`s5e1-my-${i.code}`}>
              <Link to={`/gv/giao-an/${i.draftId}`} className="s5e1-grow">
                <strong>{i.code}</strong> · {i.title}
              </Link>
              <span className="chip">{i.humanLabel}</span>
              <span className="chip">{i.statusLabel}</span>
              <span className="small muted">{versionLine(i.version, i.approvedVersion)}</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
