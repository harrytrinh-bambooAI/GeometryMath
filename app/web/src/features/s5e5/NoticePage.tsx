import { useEffect, useState } from 'react';
import { ErrorNotice } from '../../shared/ui/ErrorNotice.tsx';
import { Icon } from '../../shared/ui/Icon.tsx';
import {
  createDraft,
  getSwitches,
  getUsage,
  myClasses,
  putSwitches,
  sendDraft,
  sources,
  type ClassLine,
  type NoticeDraft,
  type Sources,
  type Switches,
  type Usage,
} from './api.ts';
import { lengthLabel, overLimit, taskLabel } from './logic.ts';
import { MathText } from '../../shared/math/MathText.tsx';

/**
 * "Trợ lý · Thông báo" (US-S5E5-01): chọn lớp, loại, bài → trợ lý soạn nháp (nhãn "AI soạn", chỗ máy chủ sửa gắn
 * "AI chưa chắc") → giáo viên sửa → tự bấm Gửi. Kèm khối "AI của lớp" (US-S5E5-04): công tắc AI chấm / Trợ lý, số lượt
 * 30 ngày. data-testid bắt đầu bằng 's5e5-'.
 */
export function NoticePage() {
  const [classes, setClasses] = useState<ClassLine[]>([]);
  const [classId, setClassId] = useState('');
  const [src, setSrc] = useState<Sources | null>(null);
  const [kind, setKind] = useState('due');
  const [refId, setRefId] = useState('');
  const [note, setNote] = useState('');
  const [draft, setDraft] = useState<NoticeDraft | null>(null);
  const [body, setBody] = useState('');
  const [busy, setBusy] = useState(false);
  const [toast, setToast] = useState<string | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [sw, setSw] = useState<Switches | null>(null);
  const [usage, setUsage] = useState<Usage | null>(null);

  useEffect(() => {
    myClasses()
      .then((r) => {
        setClasses(r.items);
        if (r.items[0]) setClassId(r.items[0].id);
      })
      .catch(setError);
  }, []);

  useEffect(() => {
    if (!classId) return;
    sources(classId)
      .then((s) => {
        setSrc(s);
        setRefId(s.sets[0]?.id ?? '');
      })
      .catch(setError);
    getSwitches(classId).then(setSw).catch(() => setSw(null));
    getUsage(classId).then(setUsage).catch(() => setUsage(null));
  }, [classId]);

  async function compose() {
    setBusy(true);
    setError(null);
    setToast(null);
    try {
      const d = await createDraft({ classId, kind, refId: kind === 'free' ? null : refId || null, note: note || null });
      setDraft(d);
      setBody(d.body);
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }

  async function send() {
    if (!draft) return;
    setBusy(true);
    setError(null);
    try {
      const d = await sendDraft(draft.id, body);
      setDraft(d);
      setToast(d.message ?? null);
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }

  async function toggle(key: 'aiGrading' | 'assistant', value: boolean) {
    try {
      const s = await putSwitches(classId, { [key]: value });
      setSw(s);
      if (s.message) setToast(s.message);
    } catch (e) {
      setError(e);
    }
  }

  const sent = draft?.status === 'sent';
  return (
    <section className="s5e5-page" data-testid="s5e5-notice-page">
      <h1 className="s5e5-title">Trợ lý · Thông báo</h1>
      {toast && (
        <p className="s5e5-toast" role="status" data-testid="s5e5-toast">
          <Icon name="check" /> {toast}
        </p>
      )}
      {error !== null && <ErrorNotice error={error} testId="s5e5-error" />}
      <div className="card s5e5-card">
        <div className="s5e5-row">
          <label className="s5e5-field">
            Lớp
            <select value={classId} onChange={(e) => {
                setClassId(e.target.value);
                setDraft(null);
                setToast(null);
              }}data-testid="s5e5-class">
              {classes.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </label>
          <label className="s5e5-field">
            Loại thông báo
            <select value={kind} onChange={(e) => setKind(e.target.value)} data-testid="s5e5-kind">
              {(src?.kinds ?? []).map((k) => (
                <option key={k.kind} value={k.kind}>
                  {k.label}
                </option>
              ))}
            </select>
          </label>
          {kind !== 'free' && (
            <label className="s5e5-field s5e5-grow">
              Bài
              <select value={refId} onChange={(e) => setRefId(e.target.value)} data-testid="s5e5-set">
                {(src?.sets ?? []).map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.title}
                    {s.dueLabel ? ` · hạn ${s.dueLabel}` : ''}
                  </option>
                ))}
              </select>
            </label>
          )}
        </div>
        <label className="s5e5-field">
          Ghi chú cho trợ lý (không bắt buộc)
          <input value={note} maxLength={500} onChange={(e) => setNote(e.target.value)} data-testid="s5e5-note" />
        </label>
        <div className="s5e5-row">
          <button type="button" className="btn primary" disabled={busy || !classId} onClick={compose} data-testid="s5e5-compose">
            <Icon name="spark" /> Soạn nháp
          </button>
        </div>
      </div>

      {draft && (
        <div className="card s5e5-card" data-testid="s5e5-draft">
          <div className="s5e5-row">
            <span className="chip" data-testid="s5e5-label">
              {draft.humanLabel}
            </span>
            {draft.dueLabel && <span className="s5e5-muted">Hạn: {draft.dueLabel}</span>}
          </div>
          {draft.unsure.length > 0 && (
            <ul className="s5e5-unsure" data-testid="s5e5-unsure">
              {draft.unsure.map((u, i) => (
                <li key={i}>
                  <span className="chip s5e5-chip-warn">{u.label}</span> <MathText text={u.text} /> <span className="s5e5-muted">({u.why})</span>
                </li>
              ))}
            </ul>
          )}
          <textarea
            className="s5e5-body"
            value={body}
            rows={6}
            disabled={sent}
            onChange={(e) => setBody(e.target.value)}
            data-testid="s5e5-body"
          />
          <div className="s5e5-row">
            <span className={overLimit(body, draft.maxLength) ? 's5e5-over' : 's5e5-muted'} data-testid="s5e5-length">
              {lengthLabel(body, draft.maxLength)}
            </span>
            <span className="s5e5-grow" />
            {!sent && (
              <button
                type="button"
                className="btn primary"
                disabled={busy || overLimit(body, draft.maxLength) || !body.trim()}
                onClick={send}
                data-testid="s5e5-send"
              >
                <Icon name="send" /> Gửi
              </button>
            )}
          </div>
        </div>
      )}

      {sw && (
        <div className="card s5e5-card" data-testid="s5e5-ai-class">
          <h2 className="s5e5-h">AI của lớp {sw.className}</h2>
          <label className="s5e5-switch">
            <input type="checkbox" checked={sw.aiGrading} onChange={(e) => toggle('aiGrading', e.target.checked)} data-testid="s5e5-sw-grading" />
            AI chấm
          </label>
          <label className="s5e5-switch">
            <input type="checkbox" checked={sw.assistant} onChange={(e) => toggle('assistant', e.target.checked)} data-testid="s5e5-sw-assistant" />
            Trợ lý (giáo án, đề, thông báo)
          </label>
          {usage && (
            <p className="s5e5-muted" data-testid="s5e5-usage">
              30 ngày: {usage.tasks.map((t) => `${taskLabel(t.task)} ${t.turns}`).join(' · ') || 'chưa dùng'} · bị chặn {usage.blocked}
            </p>
          )}
        </div>
      )}
    </section>
  );
}
