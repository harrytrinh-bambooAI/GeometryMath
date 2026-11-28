import { useCallback, useEffect, useRef, useState } from 'react';
import { Link, useLocation, useNavigate, useParams } from 'react-router';
import { ErrorNotice } from '../../../shared/ui/ErrorNotice.tsx';
import { comparison, grade, pageUrl, proposal, retry, review, type Comparison, type Proposal, type ReviewView } from './api.ts';
import { canApprove, keyAction, msg49, pointsOf, regionBox, total, vn, type Act, type CritState } from './logic.ts';
import './review.css';

/**
 * S5-E6b · màn chấm có đề xuất AI C5-05 (US-S5E6-04; T-261128-62). Bấm tiêu chí → khoanh vùng ảnh; Nhận / Sửa / Bỏ từng
 * tiêu chí; Duyệt chỉ bật khi mọi tiêu chí có điểm; "Nhận cả bài" chỉ khi bài không cờ. Phím tắt QS5-23: J/K tiêu chí
 * kế/trước, A nhận, E sửa, X bỏ, Enter duyệt, Shift+A nhận cả bài. "AI không chấm" (US-S5E6-02): MSG-S5-45 + "Thử chấm
 * lại", chấm tay. Chạy song song (US-S5E6-05): MSG-S5-47, không khung đề xuất; sau khi duyệt hiện so sánh.
 */
export function AiReviewPage() {
  const { id = '' } = useParams();
  return <AiReviewScreen key={id} id={id} />;
}

function AiReviewScreen({ id }: { id: string }) {
  const navigate = useNavigate();
  const location = useLocation();
  const [view, setView] = useState<ReviewView | null>(null);
  const [prop, setProp] = useState<Proposal | null>(null);
  const [urls, setUrls] = useState<string[]>([]);
  const [natural, setNatural] = useState<Record<number, { width: number; height: number }>>({});
  const [states, setStates] = useState<Record<number, CritState>>({});
  const [selected, setSelected] = useState<number | null>(null);
  const [all, setAll] = useState(false);
  const [toast, setToast] = useState<string>((location.state as { toast?: string } | null)?.toast ?? '');
  const [cmp, setCmp] = useState<Comparison | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<unknown>(null);
  const inputs = useRef<Record<number, HTMLInputElement | null>>({});

  const apply = useCallback(async (v: ReviewView) => {
    setView(v);
    const st: Record<number, CritState> = {};
    for (const c of v.criteria) {
      const g = v.grade?.criteria.find((x) => x.ord === c.ord);
      st[c.ord] = { act: null, value: g ? vn(g.points) : '' };
    }
    setStates(st);
    if (v.ai?.aiStatus === 'proposed') setProp(await proposal(id));
    if (v.ai?.shadow && v.grade) setCmp(await comparison(id));
    const out: string[] = [];
    for (let n = 1; n <= v.pages; n++) out.push((await pageUrl(v.versionId, n)).url);
    setUrls(out);
  }, [id]);

  useEffect(() => {
    review(id).then(apply).catch(setError);
  }, [id, apply]);

  const ai = view?.ai;
  const hidden = !!ai?.shadow && !!ai.message && !view?.grade;
  const crits = prop?.criteria ?? [];
  const withAi = !hidden && prop?.status === 'proposed' && crits.length > 0;
  const criteria = view?.criteria ?? [];
  const ready = canApprove(states, criteria);

  const act = (ord: number, a: Act) => {
    const c = crits.find((x) => x.ord === ord);
    if (!c) return;
    setAll(false);
    setSelected(ord);
    if (a === 'accept') {
      setStates((s) => ({ ...s, [ord]: { act: 'accept', value: vn(c.points) } }));
      setToast(msg49(c.name));
    } else {
      setStates((s) => ({ ...s, [ord]: { act: a, value: a === 'drop' ? '' : s[ord]?.value || vn(c.points) } }));
      setTimeout(() => inputs.current[ord]?.focus(), 0);
    }
  };

  const acceptAll = () => {
    if (!ai?.acceptAll || !withAi) return;
    const st: Record<number, CritState> = {};
    for (const c of crits) st[c.ord] = { act: 'accept', value: vn(c.points) };
    setStates(st);
    setAll(true);
    setToast('Đã nhận đề xuất của cả bài.');
  };

  const approve = async () => {
    if (!view || busy || !ready) return;
    setBusy(true);
    setError(null);
    try {
      const body = all
        ? { version: view.version, acceptAll: true, comment: '' }
        : {
            version: view.version,
            criteria: criteria.map((c) => ({ ord: c.ord, points: pointsOf(states[c.ord], c.maxPoints) ?? 0 })),
            aiActions: withAi ? criteria.filter((c) => states[c.ord]?.act).map((c) => ({ ord: c.ord, action: states[c.ord]!.act! })) : undefined,
            comment: '',
          };
      const r = await grade(view.submissionId, body);
      const msg = r.message ?? '';
      if (r.ai?.shadow) {
        setView({ ...view, ...r, criteria: view.criteria, versionId: view.versionId, assignment: view.assignment, ai: { ...r.ai } });
        setCmp(await comparison(view.submissionId));
        setToast(msg);
      } else if (r.nextSubmissionId && r.nextSubmissionId !== view.submissionId) {
        navigate(`/gv/cham-ai/${r.nextSubmissionId}`, { state: { toast: msg } });
      } else {
        setView({ ...view, ...r, criteria: view.criteria, versionId: view.versionId, assignment: view.assignment });
        setToast(msg);
      }
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  };

  const onRetry = async () => {
    try {
      const r = await retry(id);
      setToast(r.message);
    } catch (e) {
      setError(e);
    }
  };

  const handler = useRef<(e: KeyboardEvent) => void>(() => {});
  useEffect(() => {
    handler.current = (e: KeyboardEvent) => {
      const t = e.target as HTMLElement | null;
      if (t?.tagName === 'TEXTAREA') return;
      const inInput = !!t && (t.tagName === 'INPUT' || t.tagName === 'SELECT');
      const a = keyAction(e.key, e.shiftKey, inInput);
      if (!a || !view) return;
      e.preventDefault();
      const ords = (withAi ? crits : criteria).map((c) => c.ord);
      const at = selected === null ? -1 : ords.indexOf(selected);
      if (a === 'approve') void approve();
      if (a === 'acceptAll') acceptAll();
      if (a === 'next' && ords.length) setSelected(ords[Math.min(ords.length - 1, at + 1)] ?? null);
      if (a === 'prev' && ords.length) setSelected(ords[Math.max(0, at - 1)] ?? null);
      if (withAi && selected !== null && (a === 'accept' || a === 'edit' || a === 'drop')) act(selected, a);
      if (!withAi && a === 'edit' && ords[0] !== undefined) inputs.current[selected ?? ords[0]]?.focus();
    };
  });
  useEffect(() => {
    const on = (e: KeyboardEvent) => handler.current(e);
    window.addEventListener('keydown', on);
    return () => window.removeEventListener('keydown', on);
  }, []);

  const sel = crits.find((c) => c.ord === selected);
  const region = withAi && sel?.region ? sel.region : null;

  return (
    <section className="s5e6-page" data-testid="s5e6-review">
      <div className="s5e6-row">
        <Link to={view ? `/gv/hang-cho?assignmentId=${view.assignmentId}` : '/gv/hang-cho'} data-testid="s5e6-review-back">
          ← Hàng chờ
        </Link>
        {view && (
          <h1 className="s5e6-title" data-testid="s5e6-review-title">
            {view.code} · {view.studentName} · {view.assignment.title}
          </h1>
        )}
      </div>
      {toast && (
        <div className="s5e6-toast" role="status" data-testid="s5e6-toast">
          {toast}
        </div>
      )}
      {error ? <ErrorNotice error={error} testId="s5e6-review-error" /> : null}
      {view && hidden && (
        <p className="s5e6-banner" role="status" data-testid="s5e6-shadow-msg">
          {ai?.message}
        </p>
      )}
      {view && ai?.aiStatus === 'no_grade' && (
        <div className="s5e6-banner" role="status" data-testid="s5e6-nograde">
          <span data-testid="s5e6-nograde-msg">{ai.message}</span>
          <button type="button" className="btn" onClick={() => void onRetry()} data-testid="s5e6-retry">
            Thử chấm lại
          </button>
        </div>
      )}
      {view && ai?.aiStatus === 'off' && (
        <p className="s5e6-banner" data-testid="s5e6-ai-off">
          {ai.aiStatusLabel}
        </p>
      )}
      {view && (
        <div className="s5e6-review">
          <div className="s5e6-pages card" data-testid="s5e6-pages">
            {urls.map((u, k) => {
              const box = region && region.trang === k + 1 ? regionBox(region, natural[k + 1] ?? { width: 0, height: 0 }) : null;
              return (
                <div key={u} className="s5e6-page-img">
                  <img
                    src={u}
                    alt={`Trang ${k + 1}`}
                    data-testid={`s5e6-page-${k + 1}`}
                    onLoad={(e) => {
                      const img = e.currentTarget;
                      setNatural((n) => ({ ...n, [k + 1]: { width: img.naturalWidth, height: img.naturalHeight } }));
                    }}
                  />
                  {box && region && (
                    <div
                      className="s5e6-region"
                      style={box}
                      data-testid="s5e6-region"
                      data-page={region.trang}
                      data-x={region.x}
                      data-y={region.y}
                      data-w={region.w}
                      data-h={region.h}
                    />
                  )}
                </div>
              );
            })}
          </div>
          <div className="card s5e6-card" data-testid="s5e6-rubric">
            <span className="s5e6-muted">
              {view.assignment.rubricCode} v{view.assignment.rubricVersion} · <span data-testid="s5e6-status">{view.statusLabel}</span>
              {withAi && <> · Tin cậy {vn(prop?.confidence ?? 0)}</>}
            </span>
            {withAi && (ai?.aiFlags ?? []).length > 0 && (
              <p data-testid="s5e6-flags">
                {(ai?.aiFlags ?? []).map((f) => (
                  <span key={f} className="s5e6-flag">
                    {f}
                  </span>
                ))}
              </p>
            )}
            {withAi && prop?.engine?.message && (
              <p className="s5e6-muted" data-testid="s5e6-engine">
                {prop.engine.message}
              </p>
            )}
            {criteria.map((c) => {
              const pc = withAi ? crits.find((x) => x.ord === c.ord) : undefined;
              const st = states[c.ord] ?? { act: null, value: '' };
              const showInput = !pc || st.act === 'edit' || st.act === 'drop';
              return (
                <div
                  key={c.ord}
                  className={`s5e6-crit${selected === c.ord ? ' is-selected' : ''}`}
                  data-testid={`s5e6-crit-${c.ord}`}
                  data-act={st.act ?? ''}
                  onClick={() => setSelected(c.ord)}
                >
                  <div className="s5e6-crit-head">
                    <span data-testid={`s5e6-crit-name-${c.ord}`}>
                      {c.name} <span className="s5e6-muted">(tối đa {vn(c.maxPoints)})</span>
                    </span>
                    {pc && (
                      <span className="s5e6-ai-points" data-testid={`s5e6-ai-points-${c.ord}`}>
                        AI {vn(pc.points)}
                      </span>
                    )}
                    {st.act === 'accept' && (
                      <span className="s5e6-ok" data-testid={`s5e6-accepted-${c.ord}`}>
                        ✓ Đã nhận
                      </span>
                    )}
                  </div>
                  {pc && <p className="s5e6-muted">{pc.reason}</p>}
                  <div className="s5e6-row">
                    {pc && (
                      <>
                        <button type="button" className="btn" onClick={() => act(c.ord, 'accept')} data-testid={`s5e6-accept-${c.ord}`}>
                          Nhận
                        </button>
                        <button type="button" className="btn" onClick={() => act(c.ord, 'edit')} data-testid={`s5e6-edit-${c.ord}`}>
                          Sửa
                        </button>
                        <button type="button" className="btn" onClick={() => act(c.ord, 'drop')} data-testid={`s5e6-drop-${c.ord}`}>
                          Bỏ
                        </button>
                      </>
                    )}
                    {showInput && (
                      <input
                        ref={(el) => {
                          inputs.current[c.ord] = el;
                        }}
                        inputMode="decimal"
                        aria-label={`Điểm ${c.name}`}
                        value={st.value}
                        onChange={(e) => setStates((s) => ({ ...s, [c.ord]: { act: s[c.ord]?.act ?? null, value: e.target.value } }))}
                        data-testid={`s5e6-input-${c.ord}`}
                      />
                    )}
                  </div>
                </div>
              );
            })}
            <p className="s5e6-total" data-testid="s5e6-total">
              Tổng: {vn(total(states, criteria))} / {vn(view.assignment.scale)}
            </p>
            <div className="s5e6-row">
              {withAi && ai?.acceptAll && (
                <button type="button" className="btn" onClick={acceptAll} data-testid="s5e6-accept-all">
                  Nhận cả bài
                </button>
              )}
              <button type="button" className="btn primary" disabled={busy || !ready} onClick={() => void approve()} data-testid="s5e6-approve">
                {view.grade ? 'Lưu sửa điểm' : 'Duyệt'}
              </button>
            </div>
            <span className="s5e6-muted">Phím tắt: J/K tiêu chí · A nhận · E sửa · X bỏ · Enter duyệt · Shift+A nhận cả bài</span>
            {cmp && (
              <table className="s5e6-cmp" data-testid="s5e6-comparison">
                <thead>
                  <tr>
                    <th>Tiêu chí</th>
                    <th>AI</th>
                    <th>Giáo viên</th>
                  </tr>
                </thead>
                <tbody>
                  {cmp.criteria.map((r) => (
                    <tr key={r.ord} className={r.differs ? 'is-diff' : ''} data-testid={`s5e6-cmp-${r.ord}`}>
                      <td>{r.name}</td>
                      <td>{r.aiPoints === null ? '—' : vn(r.aiPoints)}</td>
                      <td>{vn(r.teacherPoints)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}
    </section>
  );
}
