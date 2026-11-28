import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router';
import { formatVnDateTime } from '../../shared/time/vnTime.ts';
import { ErrorNotice } from '../../shared/ui/ErrorNotice.tsx';
import { Icon } from '../../shared/ui/Icon.tsx';
import { fetchMine, type DraftSummary } from '../s2e3/api.ts';
import { MathText } from '../../shared/math/MathText.tsx';

/**
 * Mục "Nháp" của giáo viên (US-S2E4-01-FC1): mọi nháp đề đang soạn, kể cả nháp vừa nhận đề xuất của AI hoặc vừa nhập từ
 * tệp. Nháp CHƯA vào giáo trình; chọn một nháp để trao đổi với AI.
 */
export function DraftListPage() {
  const [items, setItems] = useState<DraftSummary[] | null>(null);
  const [error, setError] = useState<unknown>(null);

  const load = useCallback(() => {
    fetchMine()
      .then((r) => {
        setError(null);
        setItems(r.items);
      })
      .catch(setError);
  }, []);
  useEffect(load, [load]);

  if (error) return <ErrorNotice error={error} onRetry={load} testId="s2e4-drafts-error" />;
  if (!items) return <p className="muted" aria-busy="true">Đang tải…</p>;

  return (
    <div className="s2e4-page" data-testid="s2e4-drafts">
      <header className="s2e4-row">
        <h1 className="s2e4-title">Chỉnh nháp với AI</h1>
        <span className="s2e4-grow" />
        <Link className="btn ghost" to="/gv/nhap-tu-tep" data-testid="s2e4-drafts-import">
          <Icon name="file" />
          Lấy bài từ tệp Word/PDF
        </Link>
      </header>
      <h2 className="s2e4-h">Nháp</h2>
      <p className="s2e4-sub muted">Nháp chưa vào giáo trình: chỉ khi cô bấm Duyệt thì đề mới vào "Của tôi" như đề đã duyệt.</p>
      {items.length === 0 && (
        <p className="empty" data-testid="s2e4-drafts-empty">
          Chưa có nháp nào.
        </p>
      )}
      <ul className="s2e4-list">
        {items.map((d) => (
          <li key={d.id} className="card s2e4-item" data-testid={`s2e4-draft-${d.id}`} data-status={d.status}>
            <span className="s2e4-item-title" data-math>
              <MathText text={d.title} />
            </span>
            <div className="s2e4-row">
              <span className="chip" data-testid={`s2e4-draft-label-${d.id}`}>
                Nháp · {d.statusLabel}
              </span>
              <span className="s2e4-sub muted">{formatVnDateTime(d.createdAt)}</span>
              <span className="s2e4-grow" />
              <Link className="btn ghost" to={`/gv/chinh-nhap/${d.id}`} data-testid={`s2e4-draft-open-${d.id}`}>
                <Icon name="send" />
                Trao đổi
              </Link>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}
