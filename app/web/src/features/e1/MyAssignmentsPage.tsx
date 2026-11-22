import { useEffect, useState } from 'react';
import { Link } from 'react-router';
import { api } from '../../shared/api/client.ts';
import { tid } from '../../shared/testid.ts';
import { formatVnDateTime } from '../../shared/time/vnTime.ts';
import { ExploreBlock } from '../e4/Explore.tsx';
import { getMe } from './e1Session.ts';
import { errorText } from './messages.ts';
import { RELEASE_LABEL } from './studentApi.ts';
import type { Page, StudentAssignment } from './studentApi.ts';

/** Màn "Bài của em" (US-GEO-101): lời chào và bài giao của các lớp em đang thuộc, kèm tiến độ k/3. */
export function MyAssignmentsPage() {
  const me = getMe();
  const [items, setItems] = useState<StudentAssignment[] | null>(null);
  const [error, setError] = useState<unknown>(null);

  useEffect(() => {
    let alive = true;
    api
      .get<Page<StudentAssignment>>('/api/v1/student/assignments', { query: { limit: 50 } })
      .then((p) => alive && setItems(p.items))
      .catch((e: unknown) => alive && setError(e));
    return () => {
      alive = false;
    };
  }, []);

  return (
    <div data-testid={tid('e1', 'my-assignments')}>
      <h2 data-testid={tid('e1', 'greeting')}>Chào {me?.displayName ?? ''}</h2>
      {me?.className ? <p className="muted small">Lớp {me.className}</p> : null}
      {error ? (
        <div className="alert" role="alert" data-testid={tid('e1', 'my-assignments', 'error')}>
          {errorText(error)}
        </div>
      ) : null}
      {items === null && !error ? <p className="muted">Đang tải…</p> : null}
      {items && items.length === 0 ? <p className="empty">Chưa có bài nào.</p> : null}
      <div className="e1-list">
        {(items ?? []).map((a) => (
          <Link
            key={a.id}
            to={`/hs/bai/${a.id}`}
            className="card e1-row"
            data-testid={tid('e1', 'assignment', a.problemCode)}
            data-assignment-id={a.id}
          >
            <div className="e1-row-head">
              <b>{a.problemCode}</b>
              <span className="chip" data-testid={tid('e1', 'assignment', a.problemCode, 'release')} data-status={a.releaseState}>
                {RELEASE_LABEL[a.releaseState]}
                {a.releaseState === 'upcoming' && a.releaseAt ? ` · ${formatVnDateTime(a.releaseAt)}` : ''}
              </span>
            </div>
            <p className="e1-statement">{a.statement}</p>
            <span className="chip" data-testid={tid('e1', 'assignment', a.problemCode, 'progress')} data-k={a.progress.k}>
              {a.progress.k}/3 · {a.progress.label}
            </span>
          </Link>
        ))}
      </div>
      {/* E4: khối "Tự khám phá" (US-GEO-403-SC1, SC2) */}
      <ExploreBlock />
    </div>
  );
}
