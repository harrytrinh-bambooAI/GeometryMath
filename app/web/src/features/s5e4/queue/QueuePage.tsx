import { useCallback, useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router';
import { ErrorNotice } from '../../../shared/ui/ErrorNotice.tsx';
import { formatVnDateTime } from '../../../shared/time/vnTime.ts';
import { listGraded, myClasses, type Graded } from '../assign/api.ts';
import { queue, returnAll, returnStatus, undoReturn, type Queue, type ReturnStatus } from '../review/api.ts';
import { reviewPath } from '../review/logic.ts';
import '../review/review.css';

/**
 * S5-E4 · hàng chờ chấm C5-04 (US-S5E4-05) + Trả bài / Thu hồi (US-S5E4-07). T-261128-60.
 * Bài có cờ ("Chưa đọc được", "Nộp muộn") đứng trước, rồi theo giờ nộp; không có cột điểm hay xếp hạng (BR-S5E4-16).
 */
export function QueuePage() {
  const [params, setParams] = useSearchParams();
  const classId = params.get('classId') ?? '';
  const assignmentId = params.get('assignmentId') ?? '';
  const [classes, setClasses] = useState<Array<{ id: string; name: string }>>([]);
  const [graded, setGraded] = useState<Graded[]>([]);
  const [data, setData] = useState<Queue | null>(null);
  const [ret, setRet] = useState<ReturnStatus | null>(null);
  const [toast, setToast] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<unknown>(null);

  useEffect(() => {
    myClasses()
      .then((r) => setClasses(r.items))
      .catch(setError);
    listGraded()
      .then((r) => setGraded(r.items))
      .catch(setError);
  }, []);

  const load = useCallback(() => {
    queue({ classId: classId || undefined, assignmentId: assignmentId || undefined })
      .then((q) => {
        setData(q);
        setError(null);
      })
      .catch(setError);
    if (assignmentId) {
      returnStatus(assignmentId)
        .then(setRet)
        .catch(setError);
    }
  }, [classId, assignmentId]);

  useEffect(load, [load]);

  const pick = (k: 'classId' | 'assignmentId', v: string) => {
    const p = new URLSearchParams(params);
    if (v) p.set(k, v);
    else p.delete(k);
    if (k === 'classId') p.delete('assignmentId');
    setParams(p, { replace: true });
  };

  const act = async (fn: () => Promise<ReturnStatus>) => {
    setBusy(true);
    setError(null);
    try {
      const r = await fn();
      setRet(r);
      setToast(r.message ?? '');
      load();
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  };

  const shown = graded.filter((g) => !classId || g.classId === classId);

  return (
    <section className="s5e4-page" data-testid="s5e4-queue">
      <h1 className="s5e4-title">Hàng chờ chấm</h1>
      <div className="card s5e4-card">
        <div className="s5e4-row">
          <label className="s5e4-field">
            <span>Lớp</span>
            <select value={classId} onChange={(e) => pick('classId', e.target.value)} data-testid="s5e4-queue-class">
              <option value="">Tất cả lớp</option>
              {classes.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </label>
          <label className="s5e4-field s5e4-grow">
            <span>Bài</span>
            <select value={assignmentId} onChange={(e) => pick('assignmentId', e.target.value)} data-testid="s5e4-queue-assignment">
              <option value="">Tất cả bài</option>
              {shown.map((g) => (
                <option key={g.id} value={g.id}>
                  {g.code} · {g.title}
                </option>
              ))}
            </select>
          </label>
        </div>
        {data && (
          <p className="s5e4-total" data-testid="s5e4-queue-summary">
            {data.summaryLine}
          </p>
        )}
        {ret && assignmentId && ret.assignmentId === assignmentId && (
          <div className="s5e4-row" data-testid="s5e4-return-box">
            <button
              type="button"
              className="btn primary"
              disabled={busy || ret.approved === 0}
              onClick={() => act(() => returnAll(ret.assignmentId))}
              data-testid="s5e4-return"
            >
              Trả bài ({ret.approved} đã duyệt)
            </button>
            {ret.canUndo && (
              <button type="button" className="btn" disabled={busy} onClick={() => act(() => undoReturn(ret.assignmentId))} data-testid="s5e4-return-undo">
                Thu hồi (đến {ret.undoUntil ? formatVnDateTime(ret.undoUntil) : ''})
              </button>
            )}
            <span className="s5e4-muted">Bài chưa duyệt vẫn ở hàng chờ. Trả bài đóng nhận bài nộp mới.</span>
          </div>
        )}
        {toast && (
          <div className="s5e4-toast" role="status" data-testid="s5e4-queue-toast">
            {toast}
          </div>
        )}
      </div>
      {error ? <ErrorNotice error={error} onRetry={load} testId="s5e4-queue-error" /> : null}
      {data && data.items.length === 0 && (
        <p className="card s5e4-muted" data-testid="s5e4-queue-empty">
          {data.state === 'empty'
            ? `Chưa có bài nộp.${data.dueAt ? ` Hạn nộp ${formatVnDateTime(data.dueAt)}.` : ''}`
            : 'Đã chấm hết bài trong hàng chờ.'}
        </p>
      )}
      {data && data.items.length > 0 && (
        <table className="s5e4-table card" data-testid="s5e4-queue-list">
          <thead>
            <tr>
              <th>Mã</th>
              <th>Học sinh</th>
              <th>Giờ nộp</th>
              <th>Trạng thái</th>
              <th>Cờ</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {data.items.map((i) => (
              <tr key={i.submissionId} data-testid={`s5e4-queue-row-${i.code}`} className="s5e4-queue-row">
                <td>{i.code}</td>
                <td>{i.studentName}</td>
                <td>{formatVnDateTime(i.submittedAt)}</td>
                <td>{i.statusLabel}</td>
                <td>
                  {i.flags.map((f) => (
                    <span key={f.code} className="s5e4-flag" data-testid={`s5e4-flag-${f.code}`}>
                      {f.label}
                    </span>
                  ))}
                </td>
                <td>
                  <Link to={reviewPath(i)} data-testid={`s5e4-queue-open-${i.code}`}>
                    Chấm
                  </Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
