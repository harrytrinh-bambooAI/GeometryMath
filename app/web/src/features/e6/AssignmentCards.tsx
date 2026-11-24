import { useEffect, useState } from 'react';
import { Link } from 'react-router';
import { api } from '../../shared/api/client.ts';
import { tid } from '../../shared/testid.ts';
import type { Page, StudentAssignment } from '../e1/studentApi.ts';
import { M, e6Api, errText, vnDateTime } from './api.ts';

/** Nhãn giờ mở theo trạng thái máy chủ trả (BR-GEO-242). */
export function releaseLabel(a: Pick<StudentAssignment, 'releaseState' | 'releaseAt'>): string {
  if (a.releaseState === 'open') return 'Lời giải đã mở';
  if (a.releaseState === 'upcoming' && a.releaseAt) return `Lời giải mở lúc ${vnDateTime(a.releaseAt)}`;
  return 'Lời giải đang khóa';
}
const ACTION: Record<number, string> = { 0: 'Làm tiếp', 1: 'Làm tiếp', 2: 'Làm tiếp', 3: 'Xem lại' };
const SOLID: Record<string, string> = {
  pyramid: 'Hình chóp', prism: 'Lăng trụ', box: 'Hình hộp', cube: 'Lập phương', cone: 'Hình nón', cylinder: 'Hình trụ', sphere: 'Mặt cầu',
};
/** Chu kỳ làm mới danh sách khi còn bài khóa (US-GEO-612-SC4: nhãn đổi không muộn hơn 10 giây). */
const LIST_POLL_MS = 5000;

/**
 * "Bài của em" (US-GEO-606; khung Design/mvp-04): dòng "Hôm nay em có n bài cần làm" (n do máy chủ tính, D-GEO-138, 182),
 * thẻ bài (hình thu nhỏ, dạng bài, tên, nhãn giờ mở, thanh tiến độ ba đoạn, dòng tiến độ, một nút), MSG-GEO-50 khi lớp
 * chưa có bài. Không có thẻ chuỗi ngày, phần trăm theo dạng hay xếp hạng (D-GEO-078). Mã kiểm thử 'e1-…' giữ nguyên.
 */
export function AssignmentCards() {
  const [items, setItems] = useState<StudentAssignment[] | null>(null);
  const [todo, setTodo] = useState<number | null>(null);
  const [error, setError] = useState<unknown>(null);

  const anyLocked = (items ?? []).some((a) => a.releaseState !== 'open');
  useEffect(() => {
    let alive = true;
    const load = () =>
      api
        .get<Page<StudentAssignment>>('/api/v1/student/assignments', { query: { limit: 50 } })
        .then((p) => alive && setItems(p.items))
        .catch((e: unknown) => alive && setError(e));
    void load();
    void e6Api
      .todo()
      .then((t) => alive && setTodo(t.count))
      .catch(() => undefined);
    if (items !== null && !anyLocked) return () => void (alive = false);
    const t = window.setInterval(() => void load(), LIST_POLL_MS);
    return () => {
      alive = false;
      window.clearInterval(t);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps -- chỉ đặt lại khi hết bài khóa
  }, [anyLocked]);

  return (
    <>
      {todo !== null ? (
        <p className="e6-todo" data-testid={tid('e6', 'todo')} data-count={todo}>
          {todo === 0 ? 'Hôm nay em không có bài nào cần làm.' : `Hôm nay em có ${todo} bài cần làm`}
        </p>
      ) : null}
      {error ? (
        <div className="alert" role="alert" data-testid={tid('e1', 'my-assignments', 'error')}>
          {errText(error)}
        </div>
      ) : null}
      {items === null && !error ? <p className="muted">Đang tải…</p> : null}
      {items && items.length === 0 ? (
        <p className="empty" data-testid={tid('e6', 'empty')}>
          {M['50']}
        </p>
      ) : null}
      <div className="e1-list e6-cards">
        {(items ?? []).map((a) => {
          const k = a.progress.k;
          const opened = a.progress.label !== 'Chưa bắt đầu';
          return (
            <div key={a.id} className="card e6-card" data-testid={tid('e6', 'card', a.problemCode)}>
              <Link to={`/hs/bai/${a.id}`} className="e6-card-main" data-testid={tid('e1', 'assignment', a.problemCode)}
                data-assignment-id={a.id}>
                <span className="e6-thumb" aria-hidden="true">{SOLID[a.solidKind ?? ''] ?? 'Hình'}</span>
                <span className="e6-card-body">
                  <span className="small muted" data-testid={tid('e6', 'card', a.problemCode, 'topic')}>
                    {a.topic ?? ''}
                  </span>
                  <b>{a.title ?? a.problemCode}</b>
                  <span className={`chip ${a.releaseState === 'open' ? 'grn' : 'yel'}`}
                    data-testid={tid('e1', 'assignment', a.problemCode, 'release')} data-status={a.releaseState}>
                    {releaseLabel(a)}
                  </span>
                  <span className="e6-bar" data-testid={tid('e6', 'card', a.problemCode, 'bar')} data-k={k}>
                    {[1, 2, 3].map((i) => (
                      <i key={i} className={i <= k ? 'on' : ''} />
                    ))}
                  </span>
                  <span className="small" data-testid={tid('e1', 'assignment', a.problemCode, 'progress')} data-k={k}>
                    {k}/3 · {a.progress.label}
                  </span>
                </span>
              </Link>
              <Link to={`/hs/lam-bai/${a.id}`} className="btn primary sm" data-testid={tid('e6', 'card', a.problemCode, 'action')}>
                {opened ? ACTION[k] : 'Bắt đầu'}
              </Link>
            </div>
          );
        })}
      </div>
    </>
  );
}
