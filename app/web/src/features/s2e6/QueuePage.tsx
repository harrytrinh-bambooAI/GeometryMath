import { useState } from 'react';
import { Link } from 'react-router';
import { formatVnDateTime } from '../../shared/time/vnTime.ts';
import { ErrorNotice } from '../../shared/ui/ErrorNotice.tsx';
import { Icon } from '../../shared/ui/Icon.tsx';
import { libraryApi, type QueueDetail, type QueueRow } from './api.ts';
import { reasonError } from './logic.ts';
import { useFetch } from './useFetch.ts';
import './s2e6.css';
import { MathText } from '../../shared/math/MathText.tsx';

/**
 * Hàng đợi Chờ soát của người soát nội dung (US-S2E6-02): xem đề và lời giải đã gửi, "Duyệt vào Kho chung" hoặc
 * "Từ chối" kèm lý do 5…500 ký tự. Không soát đề của chính mình (BR-S2E6-07).
 */
export function QueuePage() {
  const [fetched, reload] = useFetch<{ items: QueueRow[] }>('queue', (signal) => libraryApi.queue(signal));
  const [done, setDone] = useState<Record<string, string>>({});

  if (fetched.state === 'error') return <ErrorNotice error={fetched.error} onRetry={reload} testId="s2e6-queue-error" />;
  if (fetched.state === 'loading') return <p className="muted">Đang tải hàng đợi…</p>;
  const rows = fetched.data.items;
  return (
    <div className="s2e6-page" data-testid="s2e6-queue">
      <div className="s2e6-head">
        <Link className="btn ghost" to="/gv/kho-de-cua-toi">
          <Icon name="prev" />
          Kho đề
        </Link>
        <h1 className="s2e6-title">Hàng đợi soát</h1>
      </div>
      {rows.length === 0 ? <p className="empty">Không có đề chờ soát.</p> : null}
      <ul className="s2e6-list">
        {rows.map((r) => (
          <QueueItem key={r.submissionId} row={r} result={done[r.submissionId]} onDone={(label) => setDone((d) => ({ ...d, [r.submissionId]: label }))} />
        ))}
      </ul>
    </div>
  );
}

function QueueItem({ row, result, onDone }: { row: QueueRow; result?: string; onDone: (label: string) => void }) {
  const [detail, setDetail] = useState<QueueDetail | null>(null);
  const [rejecting, setRejecting] = useState(false);
  const [reason, setReason] = useState('');
  const [localError, setLocalError] = useState<string | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [busy, setBusy] = useState(false);

  const open = async () => {
    setError(null);
    try {
      setDetail(await libraryApi.queueItem(row.submissionId));
    } catch (e) {
      setError(e);
    }
  };

  const act = async (fn: () => Promise<{ statusLabel: string }>) => {
    setBusy(true);
    setError(null);
    try {
      onDone((await fn()).statusLabel);
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  };

  const confirmReject = () => {
    const why = reasonError(reason);
    setLocalError(why);
    if (!why) void act(() => libraryApi.reject(row.submissionId, reason.trim()));
  };

  return (
    <li className="card s2e6-card s2e6-queue-row" data-testid={`s2e6-queue-${row.docId}`}>
      <div className="s2e6-row">
        <b className="s2e6-grow">{row.title}</b>
        <span className="chip">{row.kindLabel}</span>
        <span className="s2e6-help">
          {row.ownerName} · {formatVnDateTime(row.submittedAt)}
        </span>
      </div>
      {result ? (
        <span className="chip grn" data-testid="s2e6-queue-result">
          <Icon name="check" />
          {result}
        </span>
      ) : (
        <>
          <div className="s2e6-row">
            <button type="button" className="btn ghost" data-testid="s2e6-queue-open" onClick={open}>
              <Icon name="eye" />
              Xem đề và lời giải
            </button>
            <button
              type="button"
              className="btn"
              data-testid="s2e6-accept"
              disabled={busy || row.mine}
              onClick={() => void act(() => libraryApi.accept(row.submissionId))}
            >
              <Icon name="check" />
              Duyệt vào Kho chung
            </button>
            <button type="button" className="btn ghost" data-testid="s2e6-reject" disabled={busy || row.mine} onClick={() => setRejecting(true)}>
              <Icon name="x" />
              Từ chối
            </button>
            {row.mine ? <span className="s2e6-help">Bạn không thể soát đề của chính mình</span> : null}
          </div>
          {rejecting ? (
            <div className="s2e6-reject-box">
              <label className="s2e6-help" htmlFor={`s2e6-reason-${row.submissionId}`}>
                Lý do từ chối (5–500 ký tự)
              </label>
              <textarea
                id={`s2e6-reason-${row.submissionId}`}
                rows={2}
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                data-testid="s2e6-reason"
              />
              {localError ? (
                <p className="s2e6-error" role="alert" data-testid="s2e6-reason-error">
                  {localError}
                </p>
              ) : null}
              <button type="button" className="btn" data-testid="s2e6-reject-confirm" disabled={busy} onClick={confirmReject}>
                <Icon name="send" />
                Xác nhận từ chối
              </button>
            </div>
          ) : null}
        </>
      )}
      {error ? <ErrorNotice error={error} testId="s2e6-queue-action-error" /> : null}
      {detail ? (
        <div className="s2e6-detail" data-testid="s2e6-queue-detail">
          <p className="s2e6-statement" data-math>
            <MathText text={detail.statement} />
          </p>
          <ol className="s2e6-steps">
            {detail.steps.map((s, i) => (
              <li key={i} data-math>
                {s}
              </li>
            ))}
          </ol>
        </div>
      ) : null}
    </li>
  );
}
