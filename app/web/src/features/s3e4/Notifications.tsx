import { useState, type ReactNode } from 'react';
import { Link, useNavigate } from 'react-router';
import { Icon } from '../../shared/ui/Icon.tsx';
import { useLoad } from '../s3e2/api.ts';
import { notifyApi, type Notification } from './api.ts';
import { badge, vnAt } from './format.ts';

/** Chấm thông báo: số Chưa đọc, đến 99 rồi "99+" (BR-S3E4-23); 0 thì không có chấm. */
export function NotificationBell() {
  const [load] = useLoad<{ count: number }>('s3e4-bell', (s) => notifyApi.unread(s));
  const count = load.state === 'ok' ? load.data.count : 0;
  return (
    <Link className="btn ghost s3e4-bell" to="/hs/thong-bao" data-testid="s3e4-bell" aria-label={`Thông báo, ${count} chưa đọc`}>
      <Icon name="info" />
      Thông báo
      {count > 0 ? (
        <span className="s3e4-dot" data-testid="s3e4-bell-count">
          {badge(count)}
        </span>
      ) : null}
    </Link>
  );
}

/** Bọc màn Bài của em theo bộ (S3-E2) để học sinh thấy chấm thông báo khi mở ứng dụng (US-S3E4-03-SC1). */
export function WithBell({ children }: { children: ReactNode }) {
  return (
    <>
      <div className="s3e4-bellbar">
        <NotificationBell />
      </div>
      {children}
    </>
  );
}

/**
 * Thông báo trong ứng dụng của học sinh (US-S3E4-03): mới nhất trước, 20 dòng mỗi lần tải; bấm một thông báo là
 * đánh dấu Đã đọc rồi mở bộ bài; "Đánh dấu tất cả đã đọc". Nội dung chỉ có tên bộ và giờ (BR-S3E4-25).
 */
export function NotificationsPage() {
  const nav = useNavigate();
  const [n, setN] = useState(0);
  const [first, retry] = useLoad<{ items: Notification[]; nextCursor?: string | null }>(`s3e4-notifications-${n}`, (s) => notifyApi.list(null, s));
  const [unread] = useLoad<{ count: number }>(`s3e4-unread-${n}`, (s) => notifyApi.unread(s));
  const [more, setMore] = useState<{ key: number; items: Notification[]; cursor: string | null | undefined } | null>(null);
  const [failed, setFailed] = useState(false);
  if (first.state === 'loading') return <p className="muted">Đang tải thông báo…</p>;
  if (first.state === 'error')
    return (
      <div className="alert" role="alert">
        Chưa tải được thông báo.{' '}
        <button className="btn ghost sm" onClick={retry}>
          Thử lại
        </button>
      </div>
    );
  const extra = more && more.key === n ? more : null;
  const items = [...first.data.items, ...(extra?.items ?? [])];
  const cursor = extra ? extra.cursor : first.data.nextCursor;
  const count = unread.state === 'ok' ? unread.data.count : 0;
  const act = async (work: () => Promise<unknown>, then?: () => void) => {
    setFailed(false);
    try {
      await work();
      if (then) then();
      else setN((x) => x + 1);
    } catch {
      setFailed(true);
    }
  };
  const loadMore = async () => {
    if (!cursor) return;
    const page = await notifyApi.list(cursor);
    setMore({ key: n, items: [...(extra?.items ?? []), ...page.items], cursor: page.nextCursor });
  };
  return (
    <div className="s3e4-page" data-testid="s3e4-notifications">
      <div className="s3e4-head">
        <h1 className="s3e4-title">Thông báo</h1>
        {count > 0 ? (
          <span className="s3e4-dot" data-testid="s3e4-unread-badge">
            {badge(count)}
          </span>
        ) : null}
        <span className="s3e4-grow" />
        <button className="btn ghost" data-testid="s3e4-read-all" disabled={count === 0} onClick={() => void act(() => notifyApi.readAll())}>
          <Icon name="check" />
          Đánh dấu tất cả đã đọc
        </button>
      </div>
      {failed ? (
        <p className="alert" role="alert">
          Chưa thực hiện được. Em thử lại nhé.
        </p>
      ) : null}
      {items.length === 0 ? (
        <p className="empty" data-testid="s3e4-no-notification">
          Chưa có thông báo nào.
        </p>
      ) : null}
      <ul className="s3e4-rows">
        {items.map((x) => (
          <li key={x.id}>
            <button
              className={`s3e4-row s3e4-note${x.read ? '' : ' unread'}`}
              data-testid="s3e4-notification"
              data-read={x.read ? 'true' : 'false'}
              onClick={() => void act(() => notifyApi.read(x.id), x.setId ? () => void nav(`/hs/bo-bai/${x.setId}`) : undefined)}
            >
              <span className="s3e4-grow">
                <b>{x.body}</b>
                <span className="s3e4-help s3e4-block">{vnAt(x.createdAt)}</span>
              </span>
              <span className={`chip ${x.read ? '' : 'yel'}`}>{x.read ? 'Đã đọc' : 'Chưa đọc'}</span>
            </button>
          </li>
        ))}
      </ul>
      {cursor ? (
        <button className="btn ghost" data-testid="s3e4-more" onClick={() => void loadMore()}>
          Xem thêm
        </button>
      ) : null}
    </div>
  );
}
