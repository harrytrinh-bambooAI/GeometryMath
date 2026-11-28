import { useCallback, useEffect, useRef, useState } from 'react';
import { Link, useLocation, useNavigate, useParams } from 'react-router';
import { ApiError } from '../../../shared/api/errors.ts';
import { ErrorNotice } from '../../../shared/ui/ErrorNotice.tsx';
import { formatVnDateTime } from '../../../shared/time/vnTime.ts';
import { grade, pageUrl, review, type ReviewView } from './api.ts';
import { keyAction, msg37, parsePoints, total, validPoints, vn } from './logic.ts';
import './review.css';

/**
 * S5-E4 · chấm tay theo rubric C5-05 (US-S5E4-06). T-261128-60. Ảnh bài làm bên trái, rubric bên phải; tổng tự tính;
 * Duyệt → MSG-S5-38, bài sang Đã duyệt, mở bài kế tiếp trong hàng chờ. Phím tắt QS5-23: J/K bài kế/trước, A hoặc Enter
 * Duyệt, E sửa ô đầu, X bỏ qua. Không có đề xuất AI ở màn này (T-62).
 */
export function ReviewPage() {
  const { id = '' } = useParams();
  return <ReviewScreen key={id} id={id} />;
}

function ReviewScreen({ id }: { id: string }) {
  const navigate = useNavigate();
  const location = useLocation();
  const [view, setView] = useState<ReviewView | null>(null);
  const [urls, setUrls] = useState<string[]>([]);
  const [points, setPoints] = useState<Record<number, string>>({});
  const [comment, setComment] = useState('');
  const [errors, setErrors] = useState<Record<number, string>>({});
  const [toast, setToast] = useState<string>((location.state as { toast?: string } | null)?.toast ?? '');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<unknown>(null);
  const first = useRef<HTMLInputElement | null>(null);

  const apply = useCallback((v: ReviewView) => {
    setView(v);
    const p: Record<number, string> = {};
    for (const c of v.criteria) {
      const g = v.grade?.criteria.find((x) => x.ord === c.ord);
      p[c.ord] = g ? vn(g.points) : '';
    }
    setPoints(p);
    setComment(v.grade?.comment ?? '');
    setErrors({});
  }, []);

  useEffect(() => {
    review(id)
      .then(async (v) => {
        apply(v);
        const out: string[] = [];
        for (let n = 1; n <= v.pages; n++) out.push((await pageUrl(v.versionId, n)).url);
        setUrls(out);
      })
      .catch(setError);
  }, [id, apply]);

  const parsed = view ? view.criteria.map((c) => parsePoints(points[c.ord] ?? '')) : [];
  const sum = total(parsed);

  const approve = async () => {
    if (!view || busy) return;
    const errs: Record<number, string> = {};
    view.criteria.forEach((c, k) => {
      if (!validPoints(parsed[k] ?? null, c.maxPoints)) errs[c.ord] = msg37(c.name, c.maxPoints);
    });
    setErrors(errs);
    if (Object.keys(errs).length > 0) return;
    setBusy(true);
    setError(null);
    try {
      const r = await grade(view.submissionId, {
        version: view.version,
        criteria: view.criteria.map((c, k) => ({ ord: c.ord, points: parsed[k] ?? 0 })),
        comment,
      });
      const msg = r.message ?? '';
      if (r.nextSubmissionId && r.nextSubmissionId !== view.submissionId) {
        navigate(`/gv/cham/${r.nextSubmissionId}`, { state: { toast: msg } });
      } else {
        apply({ ...view, ...r, criteria: view.criteria, versionId: view.versionId, assignment: view.assignment });
        setToast(msg);
      }
    } catch (e) {
      if (e instanceof ApiError && e.code === 'VALIDATION_FAILED') {
        const m: Record<number, string> = {};
        for (const d of e.details) {
          const ord = Number(/criteria\[(\d+)\]/.exec(d.field)?.[1]);
          if (ord) m[ord] = d.message;
        }
        setErrors(m);
      }
      setError(e);
    } finally {
      setBusy(false);
    }
  };
  const approveRef = useRef(approve);
  useEffect(() => {
    approveRef.current = approve;
  });

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      const t = e.target as HTMLElement | null;
      const inInput = !!t && (t.tagName === 'INPUT' || t.tagName === 'TEXTAREA' || t.tagName === 'SELECT');
      if (inInput && t?.tagName === 'TEXTAREA') return;
      const a = keyAction(e.key, inInput);
      if (!a || !view) return;
      e.preventDefault();
      if (a === 'approve') void approveRef.current();
      if (a === 'edit') first.current?.focus();
      if ((a === 'next' || a === 'skip') && view.nextSubmissionId) navigate(`/gv/cham/${view.nextSubmissionId}`);
      if (a === 'prev' && view.prevSubmissionId) navigate(`/gv/cham/${view.prevSubmissionId}`);
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [view, navigate]);

  return (
    <section className="s5e4-page" style={{ maxWidth: 1200 }} data-testid="s5e4-review">
      <div className="s5e4-row">
        <Link to={view ? `/gv/hang-cho?assignmentId=${view.assignmentId}` : '/gv/hang-cho'} data-testid="s5e4-review-back">
          ← Hàng chờ
        </Link>
        {view && (
          <h1 className="s5e4-title" data-testid="s5e4-review-title">
            {view.code} · {view.studentName} · {view.assignment.title}
          </h1>
        )}
      </div>
      {toast && (
        <div className="s5e4-toast" role="status" data-testid="s5e4-review-toast">
          {toast}
        </div>
      )}
      {error ? <ErrorNotice error={error} testId="s5e4-review-error" /> : null}
      {view && (
        <div className="s5e4-review">
          <div className="s5e4-pages card" data-testid="s5e4-review-pages">
            <span className="s5e4-muted">
              Nộp lúc {formatVnDateTime(view.submittedAt)} · {view.pages} trang
              {view.flags.map((f) => (
                <span key={f.code} className="s5e4-flag" style={{ marginLeft: 6 }}>
                  {f.label}
                </span>
              ))}
            </span>
            {urls.map((u, k) => (
              <img key={u} src={u} alt={`Trang ${k + 1}`} data-testid={`s5e4-review-page-${k + 1}`} />
            ))}
          </div>
          <div className="card s5e4-card" data-testid="s5e4-review-rubric">
            <span className="s5e4-muted">
              {view.assignment.rubricCode} v{view.assignment.rubricVersion} · {view.assignment.rubricName} ·{' '}
              <span data-testid="s5e4-review-status">{view.statusLabel}</span>
            </span>
            {view.criteria.map((c, k) => (
              <div key={c.ord}>
                <label className="s5e4-crit" data-testid={`s5e4-crit-${c.ord}`}>
                  <span>
                    {c.name} <span className="s5e4-muted">(tối đa {vn(c.maxPoints)})</span>
                  </span>
                  <input
                    ref={k === 0 ? first : undefined}
                    inputMode="decimal"
                    value={points[c.ord] ?? ''}
                    onChange={(e) => setPoints((p) => ({ ...p, [c.ord]: e.target.value }))}
                    data-testid={`s5e4-crit-input-${c.ord}`}
                  />
                </label>
                {errors[c.ord] && (
                  <span className="s5e4-error" data-testid={`s5e4-crit-error-${c.ord}`}>
                    {errors[c.ord]}
                  </span>
                )}
              </div>
            ))}
            <p className="s5e4-total" data-testid="s5e4-review-total">
              Tổng: {vn(sum)} / {vn(view.assignment.scale)}
            </p>
            <label className="s5e4-field">
              <span>Nhận xét</span>
              <textarea value={comment} maxLength={1000} onChange={(e) => setComment(e.target.value)} data-testid="s5e4-review-comment" />
            </label>
            <div className="s5e4-row">
              <button type="button" className="btn primary" disabled={busy} onClick={() => void approve()} data-testid="s5e4-review-approve">
                {view.grade ? 'Lưu sửa điểm' : 'Duyệt'}
              </button>
              <span className="s5e4-muted">Phím tắt: J/K bài kế/trước · A hoặc Enter Duyệt · E sửa · X bỏ qua</span>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}
