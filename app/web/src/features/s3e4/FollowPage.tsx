import { useState } from 'react';
import { useSearchParams } from 'react-router';
import { ApiError } from '../../shared/api/errors.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { errorText, fromLocalInput, newKey, toLocalInput, useLoad } from '../s3e2/api.ts';
import { MSG, overviewApi, RELEASE_LABEL, type AuditEvent, type Follow, type Overview } from './api.ts';
import { auditLine, minReleaseIso, vnAt } from './format.ts';

type Ask = { kind: 'open'; key: string } | { kind: 'change'; key: string; value: string };

/**
 * C3-06 Theo dõi nhanh (US-S3E4-02): em Đang học chưa mở bài của bộ đã chọn, "Mở ngay" và "Đổi giờ" cho cả bộ
 * (BR-S3E4-17). Giờ mở do máy chủ đặt — máy khách không gửi giờ của thiết bị (BR-S3E4-11). Mất mạng: báo MSG-S3-62,
 * không xếp hàng, không tự gửi lại (BR-S3E4-15). Mỗi lần mở hộp xác nhận sinh một khóa chống lặp (BR-S3E4-16).
 */
export function FollowPage() {
  const [params, setParams] = useSearchParams();
  const [o] = useLoad<Overview>('s3e4-follow-overview', (s) => overviewApi.overview(s));
  if (o.state === 'loading') return <p className="muted">Đang tải…</p>;
  if (o.state === 'error')
    return (
      <div className="alert" role="alert">
        Chưa tải được danh sách bộ bài.
      </div>
    );
  const sets = o.data.assigned;
  const wanted = params.get('set');
  const current = sets.find((s) => s.setId === wanted) ?? sets[0];
  const classes = [...new Map(sets.map((s) => [s.classId, s.className])).entries()];
  return (
    <div className="s3e4-page" data-testid="s3e4-follow">
      <h1 className="s3e4-title">Theo dõi nhanh</h1>
      {!current ? (
        <p className="empty" data-testid="s3e4-follow-empty">
          Chưa có bộ nào đang giao.
        </p>
      ) : (
        <>
          <div className="s3e4-pick">
            <label className="s3e4-field">
              Lớp
              <select
                data-testid="s3e4-class-select"
                value={current.classId}
                onChange={(e) => {
                  const first = sets.find((s) => s.classId === e.target.value);
                  if (first) setParams({ set: first.setId });
                }}
              >
                {classes.map(([id, name]) => (
                  <option key={id} value={id}>
                    {name}
                  </option>
                ))}
              </select>
            </label>
            <label className="s3e4-field">
              Bộ bài
              <select data-testid="s3e4-set-select" value={current.setId} onChange={(e) => setParams({ set: e.target.value })}>
                {sets
                  .filter((s) => s.classId === current.classId)
                  .map((s) => (
                    <option key={s.setId} value={s.setId}>
                      {s.title}
                    </option>
                  ))}
              </select>
            </label>
          </div>
          <FollowCard key={current.setId} setId={current.setId} />
        </>
      )}
    </div>
  );
}

function FollowCard({ setId }: { setId: string }) {
  const [n, setN] = useState(0);
  const [load, retry] = useLoad<Follow>(`s3e4-follow-${setId}-${n}`, (s) => overviewApi.follow(setId, s));
  const [ask, setAsk] = useState<Ask | null>(null);
  const [busy, setBusy] = useState(false);
  const [toast, setToast] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  if (load.state === 'loading') return <p className="muted">Đang tải bộ bài…</p>;
  if (load.state === 'error')
    return (
      <div className="alert" role="alert">
        Chưa tải được bộ bài.{' '}
        <button className="btn ghost sm" onClick={retry}>
          Thử lại
        </button>
      </div>
    );
  const f = load.data;
  const open = f.releaseState === 'open';
  const run = async () => {
    if (!ask || busy) return;
    setToast(null);
    setError(null);
    if (typeof navigator !== 'undefined' && navigator.onLine === false) {
      setToast(MSG.s62); // không gửi, không xếp hàng (BR-S3E4-15)
      return;
    }
    let releaseAt: string | null = null;
    if (ask.kind === 'change') {
      releaseAt = fromLocalInput(ask.value);
      if (!releaseAt) {
        setError(MSG.s63);
        return;
      }
    }
    setBusy(true);
    try {
      if (ask.kind === 'open') await overviewApi.openNow(setId, ask.key, f.version);
      else await overviewApi.changeTime(setId, releaseAt ?? '', ask.key, f.version);
      setAsk(null);
      setN((x) => x + 1);
    } catch (e) {
      if (e instanceof ApiError && e.code === 'NETWORK') {
        setToast(MSG.s62);
      } else {
        setError(errorText(e, MSG.s62));
        if (e instanceof ApiError && e.code === 'CONFLICT') {
          setAsk(null);
          setN((x) => x + 1); // tải lại để thấy giờ mới (US-S3E4-02-FC4)
        }
      }
    } finally {
      setBusy(false);
    }
  };
  return (
    <>
      <section className="card s3e4-card" data-testid="s3e4-follow-card">
        <div className="s3e4-head">
          <h2 className="s3e4-h2 s3e4-grow">
            {f.title} · {f.className}
          </h2>
          {f.overdue ? <span className="chip red">Quá hạn</span> : null}
          <span className={`chip ${open ? 'grn' : 'yel'}`} data-testid="s3e4-state">
            <Icon name={open ? 'unlock' : 'lock'} />
            {RELEASE_LABEL[f.releaseState]}
          </span>
        </div>
        <p className="s3e4-help" data-testid="s3e4-release-at">
          {open ? 'Lời giải đã mở cho lớp.' : f.releaseAt ? `Giờ mở lời giải: ${vnAt(f.releaseAt)}` : 'Chưa đặt giờ mở lời giải.'}
        </p>
        <div className="s3e4-actions">
          <button className="btn" data-testid="s3e4-open-now" disabled={open} onClick={() => setAsk({ kind: 'open', key: newKey() })}>
            <Icon name="unlock" />
            Mở ngay
          </button>
          <button
            className="btn ghost"
            data-testid="s3e4-change-time"
            disabled={open}
            onClick={() => setAsk({ kind: 'change', key: newKey(), value: toLocalInput(f.releaseAt ?? minReleaseIso(f.serverNow)) })}
          >
            <Icon name="schedule" />
            Đổi giờ
          </button>
        </div>
        {open ? <p className="s3e4-help">{MSG.s67}</p> : null}
        {error ? (
          <p className="alert" role="alert" data-testid="s3e4-error">
            {error}
          </p>
        ) : null}
        {toast ? (
          <p className="alert" role="alert" data-testid="s3e4-toast">
            {toast}
          </p>
        ) : null}
        {ask ? (
          <div className="s3e4-banner" role="dialog" aria-label={ask.kind === 'open' ? 'Mở ngay' : 'Đổi giờ'} data-testid="s3e4-confirm">
            {ask.kind === 'open' ? (
              <b>{MSG.s61}</b>
            ) : (
              <label className="s3e4-field">
                Giờ mở mới
                <input
                  type="datetime-local"
                  data-testid="s3e4-time-input"
                  value={ask.value}
                  min={toLocalInput(minReleaseIso(f.serverNow))}
                  onChange={(e) => setAsk({ ...ask, value: e.target.value })}
                />
              </label>
            )}
            <div className="s3e4-actions">
              <button className="btn ghost" data-testid="s3e4-cancel" onClick={() => setAsk(null)}>
                Hủy
              </button>
              <button className="btn" data-testid="s3e4-confirm-ok" disabled={busy} onClick={() => void run()}>
                {ask.kind === 'open' ? 'Mở ngay' : 'Xác nhận'}
              </button>
            </div>
          </div>
        ) : null}
      </section>
      <section className="card s3e4-card" aria-label="Em chưa mở bài">
        <h2 className="s3e4-h2">
          <Icon name="users" inline />
          Em chưa mở bài · {f.notOpened.length}
        </h2>
        {f.notOpened.length === 0 ? (
          <p className="s3e4-help" data-testid="s3e4-all-opened">
            Cả lớp đã mở bài.
          </p>
        ) : null}
        <ul className="s3e4-rows" data-testid="s3e4-not-opened">
          {f.notOpened.map((s) => (
            <li key={`${s.displayName}-${s.userCode ?? ''}`} className="s3e4-row" data-testid="s3e4-student">
              {s.displayName}
              {s.userCode ? (
                <>
                  {' · '}
                  <span className="mono">{s.userCode}</span>
                </>
              ) : null}
            </li>
          ))}
        </ul>
      </section>
      <AuditLog key={n} classId={f.classId} />
    </>
  );
}

/** Nhật ký thao tác nhạy cảm của lớp (US-S3E5-01-SC5, SC6): chỉ đọc, giờ "HH:mm, dd/MM/yyyy" giờ Việt Nam. */
function AuditLog({ classId }: { classId: string }) {
  const [load] = useLoad<{ items: AuditEvent[] }>(`s3e4-audit-${classId}`, (s) => overviewApi.audit(classId, s));
  if (load.state !== 'ok') return null;
  return (
    <details className="card s3e4-card" data-testid="s3e4-audit">
      <summary className="s3e4-summary">Nhật ký của lớp · {load.data.items.length}</summary>
      {load.data.items.length === 0 ? <p className="s3e4-help">Chưa có thao tác nào được ghi.</p> : null}
      <ul className="s3e4-rows">
        {load.data.items.map((e) => (
          <li key={e.id} className="s3e4-row s3e4-log" data-testid="s3e4-audit-row">
            {auditLine(e)}
          </li>
        ))}
      </ul>
    </details>
  );
}
