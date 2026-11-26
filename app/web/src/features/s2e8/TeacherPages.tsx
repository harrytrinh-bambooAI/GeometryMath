import { useState } from 'react';
import { Link } from 'react-router';
import { ApiError } from '../../shared/api/errors.ts';
import { formatVnDateTime } from '../../shared/time/vnTime.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { practiceApi, type ReportView, type SetLine } from './api.ts';
import { STATUS_LABEL, useLoad } from './load.ts';
import { LoadError } from './LoadError.tsx';

/** Danh sách bài luyện của giáo viên: Nháp, Đã giao, Đã đóng. */
export function TeacherListPage() {
  const [load, retry] = useLoad<{ items: SetLine[] }>('sets', (s) => practiceApi.sets(s));
  if (load.state === 'loading') return <p className="muted" data-testid="s2e8-loading">Đang tải bài luyện…</p>;
  if (load.state === 'error') return <LoadError notFound={load.notFound} onRetry={retry} what="danh sách bài luyện" />;
  return (
    <div className="s2e8-page" data-testid="s2e8-teacher-list">
      <div className="s2e8-head">
        <h1 className="s2e8-title">Bài luyện</h1>
        <Link className="btn" to="moi" data-testid="s2e8-new"><Icon name="plus" />
          Soạn bài luyện
        </Link>
        <Link className="btn ghost" to="bao-cham-sai" data-testid="s2e8-reports-link"><Icon name="warn" />
          Báo chấm sai
        </Link>
      </div>
      {load.data.items.length === 0 ? <p className="empty">Chưa có bài luyện nào. Bấm “Soạn bài luyện” để bắt đầu.</p> : null}
      <ul className="s2e8-list">
        {load.data.items.map((s) => (
          <li key={s.id} className="card s2e8-row" data-testid={`s2e8-set-${s.id}`}>
            <div className="s2e8-grow">
              <b>{s.title}</b>
              <div className="s2e8-help">
                {s.itemCount} câu{s.className ? ` · lớp ${s.className}` : ''}
                {s.releaseAt ? ` · lời giải mở lúc ${formatVnDateTime(s.releaseAt)}` : ''}
              </div>
            </div>
            <span className={`chip ${s.status === 'assigned' ? 'grn' : s.status === 'draft' ? 'yel' : ''}`}>{STATUS_LABEL[s.status]}</span>
            <Link className="btn ghost" to={s.id}>
              {s.status === 'draft' ? 'Sửa' : 'Xem'}
            </Link>
          </li>
        ))}
      </ul>
    </div>
  );
}

/**
 * "Báo chấm sai" (BR-S2E8-31): báo "Em nghĩ đáp số này đúng" của học sinh lớp mình kèm đáp số em nhập và dạng chuẩn.
 * "Xác nhận đúng": câu của em thành Đúng, dạng viết đó được chấp nhận cho câu. "Giữ nguyên": kết quả không đổi.
 */
export function ReportsPage() {
  const [load, retry] = useLoad<{ items: ReportView[] }>('reports', (s) => practiceApi.reports(s));
  const [changed, setChanged] = useState<Record<string, ReportView>>({});
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState<string | null>(null);
  if (load.state === 'loading') return <p className="muted" data-testid="s2e8-loading">Đang tải báo chấm sai…</p>;
  if (load.state === 'error') return <LoadError notFound={load.notFound} onRetry={retry} what="danh sách báo chấm sai" />;
  const decide = async (id: string, decision: 'confirm' | 'keep') => {
    setBusy(id);
    setError(null);
    try {
      const out = await practiceApi.decide(id, decision);
      setChanged((all) => ({ ...all, [id]: out }));
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Chưa lưu được. Thầy cô thử lại nhé.');
    } finally {
      setBusy(null);
    }
  };
  const items = load.data.items.map((r) => changed[r.id] ?? r);
  return (
    <div className="s2e8-page" data-testid="s2e8-reports">
      <div className="s2e8-head">
        <h1 className="s2e8-title">Báo chấm sai</h1>
        <Link className="btn ghost" to=".." relative="path">
          Danh sách bài luyện
        </Link>
      </div>
      {error ? (
        <p className="s2e8-err" role="alert" data-testid="s2e8-reports-error">
          {error}
        </p>
      ) : null}
      {items.length === 0 ? <p className="empty" data-testid="s2e8-reports-empty">Chưa có báo nào từ học sinh.</p> : null}
      <ul className="s2e8-list">
        {items.map((r) => (
          <li key={r.id} className="card s2e8-report-row" data-testid={`s2e8-report-${r.id}`} data-status={r.status}>
            <div className="s2e8-grow">
              <b data-testid="s2e8-report-student">
                {r.studentName}
                {r.studentCode ? ` (${r.studentCode})` : ''}
              </b>
              <div className="s2e8-help" data-testid="s2e8-report-where">
                {r.setTitle} · Câu {r.position} · {formatVnDateTime(r.createdAt)}
              </div>
              <div data-testid="s2e8-report-submitted">
                Đáp số em nhập: <span data-math>{r.submitted}</span>
              </div>
              <div data-testid="s2e8-report-canonical">
                Dạng chuẩn: <span data-math>{r.canonical}</span>
              </div>
              {r.note ? <div data-testid="s2e8-report-note">Ghi chú: {r.note}</div> : null}
            </div>
            <span className={`chip ${r.status === 'confirmed' ? 'grn' : r.status === 'kept' ? '' : 'yel'}`} data-testid="s2e8-report-status">
              {r.statusLabel}
            </span>
            {r.status === 'pending' ? (
              <div className="s2e8-actions">
                <button type="button" className="btn" disabled={busy === r.id} onClick={() => void decide(r.id, 'confirm')} data-testid="s2e8-report-confirm"><Icon name="check" />
                  Xác nhận đúng
                </button>
                <button type="button" className="btn ghost" disabled={busy === r.id} onClick={() => void decide(r.id, 'keep')} data-testid="s2e8-report-keep"><Icon name="x" />
                  Giữ nguyên
                </button>
              </div>
            ) : null}
          </li>
        ))}
      </ul>
    </div>
  );
}
