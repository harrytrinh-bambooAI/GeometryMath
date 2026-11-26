import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router';
import { ApiError } from '../../shared/api/errors.ts';
import { ErrorNotice } from '../../shared/ui/ErrorNotice.tsx';
import { Icon } from '../../shared/ui/Icon.tsx';
import { formatVnDateTime } from '../../shared/time/vnTime.ts';
import { assignDraft, fetchMine, type DraftSummary } from './api.ts';
import { MSG } from './logic.ts';

/**
 * Ngăn "Của tôi" của giáo viên + bước "Soạn bài giao: chọn đề" (US-S2E3-04-SC2, SC3): bản nháp chữ không chọn được để
 * giao (chú thích "bản nháp chữ"); đề "Cần xem lại · hình do AI dựng" phải bấm xác nhận riêng (BR-S2E3-16).
 * TẠM: chọn lớp và giờ mở vẫn ở luồng giao bài hiện có; ở đây chỉ chốt đề nào giao được.
 */
export function MyProblemsPage() {
  const [items, setItems] = useState<DraftSummary[] | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [ask, setAsk] = useState<DraftSummary | null>(null);
  const [done, setDone] = useState<string | null>(null);
  const [fail, setFail] = useState<string | null>(null);

  const load = useCallback(() => {
    fetchMine()
      .then((r) => setItems(r.items))
      .catch(setError);
  }, []);
  useEffect(load, [load]);

  const choose = (d: DraftSummary, confirm: boolean) => {
    setFail(null);
    assignDraft(d.id, confirm)
      .then(() => {
        setAsk(null);
        setDone(d.id);
      })
      .catch((e: unknown) => setFail(e instanceof ApiError ? e.message : 'Có lỗi, vui lòng thử lại.'));
  };

  if (error) return <ErrorNotice error={error} onRetry={load} testId="s2e3-mine-error" />;
  if (!items) return <p className="muted" aria-busy="true">Đang tải…</p>;

  return (
    <div className="s2e3-mine" data-testid="s2e3-mine">
      <header className="s2e3-row">
        <h1 className="s2e3-title">Của tôi</h1>
        <span className="s2e3-grow" />
        <Link className="btn" to="/gv/them-de" data-testid="s2e3-mine-add">
          <Icon name="plus" />
          Thêm đề
        </Link>
      </header>
      <h2 className="s2e3-h">Soạn bài giao · chọn đề</h2>
      {items.length === 0 && <p className="s2e3-sub muted">Chưa có đề nào.</p>}
      {fail && (
        <p className="alert" role="alert" data-testid="s2e3-assign-error">
          {fail}
        </p>
      )}
      <ul className="s2e3-list">
        {items.map((d) => (
          <li key={d.id} className="card s2e3-item" data-testid={`s2e3-item-${d.id}`} data-status={d.status}>
            <span className="s2e3-item-title" data-math>
              {d.title}
            </span>
            <span className="s2e3-sub muted">
              {d.statusLabel} · {formatVnDateTime(d.createdAt)}
            </span>
            <div className="s2e3-row">
              {d.assignNote && (
                <span className={`chip${d.status === 'ai_figure' ? ' yel' : ''}`} data-testid={`s2e3-assign-note-${d.id}`}>
                  {d.assignNote}
                </span>
              )}
              <span className="s2e3-grow" />
              {done === d.id ? (
                <span className="chip grn" data-testid={`s2e3-assign-done-${d.id}`}>
                  Đã chọn để giao
                </span>
              ) : (
                <button
                  type="button"
                  className="btn"
                  disabled={!d.assignable}
                  title={d.assignable ? undefined : d.assignNote ?? undefined}
                  onClick={() => (d.needsAssignConfirm ? setAsk(d) : choose(d, false))}
                  data-testid={`s2e3-assign-${d.id}`}
                >
                  <Icon name="assign" />
                  Chọn để giao
                </button>
              )}
            </div>
          </li>
        ))}
      </ul>
      {ask && (
        <div className="s2e3-overlay">
          <div className="card s2e3-dialog" role="alertdialog" aria-modal="true" aria-labelledby="s2e3-ask-title" data-testid="s2e3-assign-confirm">
            <p id="s2e3-ask-title" className="s2e3-dialog-title">
              {MSG.askAiFigure}
            </p>
            <div className="s2e3-row">
              <button type="button" className="btn" onClick={() => choose(ask, true)} data-testid="s2e3-assign-yes">
                Vẫn giao
              </button>
              <button type="button" className="btn ghost" onClick={() => setAsk(null)} data-testid="s2e3-assign-no">
                Không giao
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
