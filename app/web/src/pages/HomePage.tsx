import { useEffect, useState } from 'react';
import { Link } from 'react-router';
import { api } from '../shared/api/client.ts';
import { formatVnDateTime } from '../shared/time/vnTime.ts';
import { tid } from '../shared/testid.ts';
import { ErrorNotice } from '../shared/ui/ErrorNotice.tsx';

type Health = { status: string };

/** Trang chủ: chọn vùng Giáo viên / Học sinh và cho biết máy chủ có sẵn sàng không. */
export function HomePage() {
  const [health, setHealth] = useState<Health | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [now] = useState(() => new Date());

  const [attempt, setAttempt] = useState(0);

  useEffect(() => {
    let alive = true;
    api
      .get<Health>('/health', { anonymous: true })
      .then((h) => alive && setHealth(h))
      .catch((e: unknown) => alive && setError(e));
    return () => {
      alive = false;
    };
  }, [attempt]);

  const retry = () => {
    setError(null);
    setHealth(null);
    setAttempt((a) => a + 1);
  };

  return (
    <div className="main">
      <header className="top">
        <h1 data-testid={tid('home', 'title')}>Hình Học Không Gian Cùng AI</h1>
        <div className="spacer" />
        <span className="small muted" data-testid={tid('home', 'clock')}>
          Giờ Việt Nam: {formatVnDateTime(now)}
        </span>
      </header>
      <main className="body">
        <section className="card" data-testid={tid('home', 'server')}>
          <h3>Trạng thái máy chủ</h3>
          {health ? (
            <p>
              <span className="chip grn" data-testid={tid('home', 'server', 'status')} data-status={health.status}>
                Máy chủ sẵn sàng
              </span>
            </p>
          ) : error ? (
            <ErrorNotice error={error} onRetry={retry} testId={tid('home', 'server', 'error')} />
          ) : (
            <p className="muted">Đang kiểm tra…</p>
          )}
        </section>
        <section className="grid2">
          <Link to="/gv" className="card" data-testid={tid('home', 'role', 'teacher')}>
            <h2>Giáo viên</h2>
            <p className="muted">Kho đề, giao bài, theo dõi lớp.</p>
          </Link>
          <Link to="/hs" className="card" data-testid={tid('home', 'role', 'student')}>
            <h2>Học sinh</h2>
            <p className="muted">Bài của em, làm bài, tự học.</p>
          </Link>
        </section>
      </main>
    </div>
  );
}
