import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router';
import { tid } from '../../shared/testid.ts';
import { errorText } from '../e1/messages.ts';
import { e7Api } from './api.ts';
import type { Assigned } from './api.ts';
import { RELEASE_STATE, vnDateTime } from './format.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { releaseIcon } from '../../shared/ui/icons.ts';

/**
 * Mục "Theo dõi" (US-GEO-701-SC6, D-GEO-129): bài đã giao của các lớp mình dạy, mới giao ở trên, tải theo con trỏ.
 * Bấm một bài để mở Theo dõi lớp của bài đó.
 */
export function AssignedPage() {
  const [items, setItems] = useState<Assigned[]>([]);
  const [next, setNext] = useState<string | null>(null);
  const [loaded, setLoaded] = useState(false);
  const [error, setError] = useState<unknown>(null);

  const more = useCallback((cursor: string | null) => {
    e7Api
      .assigned(cursor)
      .then((p) => {
        setItems((xs) => (cursor ? [...xs, ...p.items] : p.items));
        setNext(p.nextCursor);
        setLoaded(true);
      })
      .catch((e: unknown) => setError(e));
  }, []);
  useEffect(() => {
    const t = window.setTimeout(() => more(null), 0);
    return () => window.clearTimeout(t);
  }, [more]);

  return (
    <div className="e7-assigned" data-testid={tid('e7', 'assigned')}>
      <h1>Theo dõi</h1>
      {error ? (
        <p className="alert" role="alert" data-testid={tid('e7', 'assigned', 'error')}>
          {errorText(error)}
        </p>
      ) : null}
      {loaded && items.length === 0 ? <p className="empty">Chưa có bài nào được giao.</p> : null}
      <ul className="e7-list">
        {items.map((a) => (
          <li key={a.id} className="card" data-testid={tid('e7', 'assigned', 'row')} data-id={a.id}>
            <Link to={`/gv/theo-doi/${a.id}`} data-testid={tid('e7', 'assigned', 'link', a.problemCode, a.className)}>
              <b>{a.problemTitle}</b>
            </Link>
            <span className="small muted">
              {' '}
              · {a.className} · giao lúc {vnDateTime(a.assignedAt)}
            </span>{' '}
            <span className={`chip ${a.releaseState === 'open' ? 'grn' : 'yel'}`} data-testid={tid('e7', 'assigned', 'state')}><Icon name={releaseIcon(a.releaseState)} />
              {RELEASE_STATE[a.releaseState] ?? a.releaseState}
            </span>
          </li>
        ))}
      </ul>
      {next ? (
        <button type="button" className="btn ghost" data-testid={tid('e7', 'assigned', 'more')} onClick={() => more(next)}>
          Xem thêm
        </button>
      ) : null}
    </div>
  );
}
