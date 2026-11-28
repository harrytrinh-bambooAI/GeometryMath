import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router';
import { tid } from '../../shared/testid.ts';
import { M, e6Api, errText, msg41, msg44, vnToIso } from './api.ts';
import type { AssignOptions } from './api.ts';
import { Icon } from '../../shared/ui/Icon.tsx';

/**
 * "Chi tiết đề &amp; giao bài" (US-GEO-602, 603, 604; khung Design/mvp-02). Ba bước: 1 · Chọn lớp ("{tên lớp} · {cấp}
 * ({số học sinh} HS)", chọn đúng một lớp), 2 · trợ giúp (hai công tắc, mặc định bật), 3 · thời điểm mở lời giải (ngày,
 * giờ theo giờ Việt Nam, lối tắt "Ngay bây giờ"; để trống = Chưa đặt giờ). Giao từng đề (không có giao theo bộ).
 * "Xem như học sinh" mở màn xem trước với lớp và giờ đang chọn (US-GEO-607). Mọi so sánh giờ do máy chủ làm.
 */
export function AssignPage() {
  const { versionId = '' } = useParams();
  const nav = useNavigate();
  const [opt, setOpt] = useState<AssignOptions | null>(null);
  const [loadErr, setLoadErr] = useState<unknown>(null);
  const [picked, setPicked] = useState<string | null>(null);
  const [hint, setHint] = useState(true);
  const [qa, setQa] = useState(true);
  const [date, setDate] = useState('');
  const [time, setTime] = useState('');
  const [now, setNow] = useState(false);
  const [busy, setBusy] = useState(false);
  const [done, setDone] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const sending = useRef(false);
  // Lũy đẳng khi bấm lại sau lỗi (Dev-E6-02): cùng lớp + cài đặt thì giữ nguyên assignmentId, nên lệnh giao
  // lặp lại trả DUPLICATE (không phải MSG-42); "Ngay bây giờ" chỉ mở khi bài chưa mở.
  const attempt = useRef<{ key: string; id: string; created: boolean } | null>(null);

  useEffect(() => {
    let alive = true;
    e6Api
      .options(versionId)
      .then((o) => alive && setOpt(o))
      .catch((e: unknown) => alive && setLoadErr(e));
    return () => {
      alive = false;
    };
  }, [versionId]);

  if (loadErr) {
    return (
      <div className="alert" role="alert" data-testid={tid('e6', 'assign', 'load-error')}>
        {errText(loadErr)}
      </div>
    );
  }
  if (!opt) return <p className="muted">Đang tải…</p>;

  const cls = opt.classes.find((c) => c.id === picked) ?? null;
  const iso = now ? null : vnToIso(date, time);
  const empty = !now && !date && !time;

  const submit = async () => {
    if (!cls || sending.current || done) return; // bấm đúp: chỉ một yêu cầu (US-GEO-602-FC2)
    sending.current = true;
    setBusy(true);
    setError(null);
    const key = JSON.stringify([cls.id, iso, now, hint, qa]);
    if (attempt.current?.key !== key) attempt.current = { key, id: crypto.randomUUID(), created: false };
    const a = attempt.current;
    try {
      const retry = a.created;
      const r = await e6Api.assign({
        assignmentId: a.id,
        classId: cls.id,
        problemVersionId: versionId,
        releaseAt: iso,
        allowHint: hint,
        allowQa: qa,
      });
      a.created = true;
      if (now) {
        // Lần bấm lại: lần trước có thể đã mở được (chỉ mất phản hồi) → đọc trạng thái trước khi mở.
        const v = retry ? await e6Api.releaseView(r.id) : null;
        if (v?.releaseState !== 'open') {
          await e6Api.setRelease(r.id, { openNow: true, expectedRev: v ? v.releaseRev : r.releaseRev });
        }
      }
      setDone(msg41(cls.name));
    } catch (e) {
      setError(errText(e));
      sending.current = false;
    } finally {
      setBusy(false);
    }
  };

  const previewQuery = new URLSearchParams();
  if (cls) previewQuery.set('classId', cls.id);
  if (iso) previewQuery.set('releaseAt', iso);

  return (
    <div className="e6-assign" data-testid={tid('e6', 'assign')}>
      <div className="row e6-head">
        <h2>Chi tiết đề &amp; giao bài</h2>
        <button type="button" className="btn ghost" data-testid={tid('e6', 'preview', 'open')}
          onClick={() => void nav(`/gv/xem-truoc/${versionId}?${previewQuery.toString()}`)}><Icon name="eye" />
          Xem như học sinh
        </button>
      </div>
      <section className="card" data-testid={tid('e6', 'assign', 'problem')}>
        <b>{opt.problem.code}</b> · {opt.problem.title}
        <p className="e1-statement">{opt.problem.statement}</p>
      </section>

      <section className="card" data-testid={tid('e1', 'assign', 'step-class')}>
        <h3>1 · Chọn lớp</h3>
        {opt.classes.map((c) => (
          <label key={c.id} className="e1-radio" data-testid={tid('e6', 'class', c.name)}>
            <input type="radio" name="class" value={c.id} checked={picked === c.id} disabled={!!done}
              onChange={() => setPicked(c.id)} />
            {c.label}
          </label>
        ))}
        {cls?.assignment ? (
          <p className="small muted" data-testid={tid('e6', 'assign', 'existing')}>
            Lớp {cls.name} đã có bài này. <Link to={`/gv/theo-doi/${cls.assignment.id}`}>Theo dõi lớp</Link>
          </p>
        ) : null}
      </section>

      <section className="card" data-testid={tid('e6', 'assign', 'step-help')}>
        <h3>2 · Học sinh được trợ giúp gì trong lúc làm</h3>
        <label className="e6-switch">
          <input type="checkbox" role="switch" checked={hint} disabled={!!done} data-testid={tid('e6', 'switch', 'hint')}
            onChange={(e) => setHint(e.target.checked)} />
          Gợi ý mức 1 và 2
        </label>
        <label className="e6-switch">
          <input type="checkbox" role="switch" checked={qa} disabled={!!done} data-testid={tid('e6', 'switch', 'qa')}
            onChange={(e) => setQa(e.target.checked)} />
          Hỏi đáp (không nêu đáp số)
        </label>
      </section>

      <section className="card" data-testid={tid('e6', 'assign', 'step-time')}>
        <h3>3 · Thời điểm mở lời giải</h3>
        <div className="row e6-time">
          <label>
            Ngày{' '}
            <input type="date" value={date} disabled={now || !!done} data-testid={tid('e6', 'release', 'date')}
              onChange={(e) => setDate(e.target.value)} />
          </label>
          <label>
            Giờ{' '}
            <input type="time" value={time} disabled={now || !!done} data-testid={tid('e6', 'release', 'time')}
              onChange={(e) => setTime(e.target.value)} />
          </label>
          <button type="button" className={`btn ${now ? 'primary' : 'ghost'}`} aria-pressed={now} disabled={!!done}
            data-testid={tid('e6', 'release', 'now')} onClick={() => setNow((x) => !x)}>
            Ngay bây giờ
          </button>
        </div>
        {now ? (
          <p className="small" data-testid={tid('e6', 'release', 'info')}>Lời giải mở ngay khi giao bài.</p>
        ) : iso ? (
          <p className="small" data-testid={tid('e6', 'release', 'info')}>{msg44(iso)}</p>
        ) : empty ? (
          <p className="small" data-testid={tid('e6', 'release', 'info')}>{M['45']}</p>
        ) : null}
      </section>

      {error ? (
        <div className="alert" role="alert" data-testid={tid('e6', 'assign', 'error')}>
          {error}
        </div>
      ) : null}
      {done ? (
        <p className="toast chip grn" role="status" data-testid={tid('e6', 'assign', 'done')}><Icon name="check" />
          {done}
        </p>
      ) : null}
      <button type="button" className="btn primary" disabled={!cls || busy || !!done || (!now && !empty && !iso)}
        data-testid={tid('e6', 'assign', 'submit')} onClick={() => void submit()}>
        {cls ? `Giao bài cho ${cls.name}` : 'Giao bài'}
      </button>
    </div>
  );
}
