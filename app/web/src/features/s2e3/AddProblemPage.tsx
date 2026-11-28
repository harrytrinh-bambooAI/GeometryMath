import { useCallback, useEffect, useRef, useState } from 'react';
import { ApiError } from '../../shared/api/errors.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { checkVersion, setStatedAnswer, takeMachineAnswer } from '../s2e5/api.ts';
import { buildDraft, cancelJob, confirmText, createDraft, fetchCurrent, fetchDraft, fetchJob, retryJob, updateDraft, uploadImage, type DraftView, type JobView } from './api.ts';
import { ImageCells } from './ImageCells.tsx';
import { lengthIssue, MAX_CHARS, MSG, pickImage, charCount } from './logic.ts';
import { Labels, ResultPanel, Stages } from './Parts.tsx';
import { Unbuildable } from './Unbuildable.tsx';
import { MathText } from '../../shared/math/MathText.tsx';

type Notice = { kind: 'error' | 'info'; text: string } | null;

const message = (e: unknown) => (e instanceof ApiError ? (e.code === 'RATE_LIMITED' ? MSG.rate : e.message) : 'Có lỗi, vui lòng thử lại.');

/**
 * C2-02 "Thêm đề" (US-S2E3-01…04; Design/prototype/s23.js c2-02-*): hai tab "Dán chữ" / "Tải ảnh", nháp chữ lưu ở máy
 * chủ (rời màn rồi quay lại vẫn thấy), bốn bước có màn chờ, ngoại lệ ảnh mờ / không dựng / lệch đáp số.
 */
export function AddProblemPage() {
  const [draft, setDraft] = useState<DraftView | null>(null);
  const [job, setJob] = useState<JobView | null>(null);
  const [loaded, setLoaded] = useState(false);
  const [tab, setTab] = useState<'text' | 'image'>('text');
  const [text, setText] = useState('');
  const [notice, setNotice] = useState<Notice>(null);
  const [busy, setBusy] = useState(false);
  const draftRef = useRef<DraftView | null>(null);
  const timer = useRef<number | undefined>(undefined);
  const pending = useRef<string | null>(null);
  const creating = useRef<Promise<DraftView> | null>(null);

  const apply = useCallback((d: DraftView) => {
    draftRef.current = d;
    setDraft(d);
    setText(d.statement);
    setTab(d.source);
    setJob(d.job ?? null);
    if (d.notice) setNotice({ kind: 'info', text: d.notice });
  }, []);

  useEffect(() => {
    const ctl = new AbortController();
    fetchCurrent(ctl.signal)
      .then((d) => d && apply(d))
      .catch(() => undefined)
      .finally(() => setLoaded(true));
    return () => ctl.abort();
  }, [apply]);

  /** Lưu chữ đang gõ thành nháp ở máy chủ (BR-S2E3-04); tạo nháp ở lần gõ đầu. */
  const persist = useCallback(async (value: string): Promise<DraftView | null> => {
    pending.current = null;
    if (charCount(value) > MAX_CHARS) return draftRef.current;
    const d = draftRef.current;
    try {
      if (!d) {
        if (!value.trim()) return null;
        creating.current ??= createDraft('text', value);
        const created = await creating.current;
        const latest = created.statement !== value ? await updateDraft(created.id, { statement: value }) : created;
        draftRef.current = latest;
        setDraft(latest);
        return latest;
      }
      if (d.status === 'building' || d.statement === value) return d;
      const next = await updateDraft(d.id, { statement: value });
      draftRef.current = next;
      setDraft(next);
      return next;
    } catch (e) {
      setNotice({ kind: 'error', text: message(e) });
      return null;
    } finally {
      creating.current = null;
    }
  }, []);

  useEffect(
    () => () => {
      if (timer.current !== undefined) window.clearTimeout(timer.current);
      if (pending.current !== null) void persist(pending.current);
    },
    [persist],
  );

  // Theo dõi tác vụ đang chạy (đóng tab rồi mở lại vẫn thấy tiến trình: tác vụ ở máy chủ, BR-S2E3-13).
  useEffect(() => {
    if (!job || job.state !== 'running') return;
    const t = window.setInterval(() => {
      fetchJob(job.id)
        .then((j) => {
          setJob(j);
          if (j.state !== 'running' && draftRef.current) fetchDraft(draftRef.current.id).then(apply).catch(() => undefined);
        })
        .catch(() => undefined);
    }, 500);
    return () => window.clearInterval(t);
  }, [job, apply]);

  const onType = (value: string) => {
    setText(value);
    pending.current = value;
    if (timer.current !== undefined) window.clearTimeout(timer.current);
    timer.current = window.setTimeout(() => void persist(value), 400);
  };

  const act = (call: () => Promise<DraftView>, info?: string) => {
    setBusy(true);
    setNotice(null);
    call()
      .then((d) => {
        apply(d);
        if (info) setNotice({ kind: 'info', text: info });
      })
      .catch((e: unknown) => setNotice({ kind: 'error', text: message(e) }))
      .finally(() => setBusy(false));
  };

  const build = () =>
    act(async () => {
      if (timer.current !== undefined) window.clearTimeout(timer.current);
      const d = (await persist(text)) ?? draftRef.current;
      if (!d) throw new ApiError({ code: 'VALIDATION_FAILED', status: 400, message: MSG.empty });
      if (d.source === 'image' && !d.textConfirmed) await confirmText(d.id);
      return buildDraft(d.id);
    });

  const onFiles = (list: FileList | null) => {
    const { file, message: m } = pickImage(Array.from(list ?? []));
    if (!file) {
      if (m) setNotice({ kind: 'error', text: m });
      return;
    }
    setBusy(true);
    setNotice(null);
    createDraft('image')
      .then((d) => uploadImage(d.id, file))
      .then((d) => {
        apply(d);
        if (m) setNotice({ kind: 'info', text: m });
      })
      .catch((e: unknown) => setNotice({ kind: 'error', text: message(e) }))
      .finally(() => setBusy(false));
  };

  const reset = () => {
    draftRef.current = null;
    setDraft(null);
    setJob(null);
    setText('');
    setNotice(null);
  };

  if (!loaded) return <p className="muted" aria-busy="true" data-testid="s2e3-loading">Đang tải…</p>;

  const issue = lengthIssue(text);
  const running = job?.state === 'running';
  const image = draft?.source === 'image';
  const unsure = draft?.cells.some((c) => c.state === 'unsure') ?? false;
  const showCells = image && (draft?.cells.length ?? 0) > 0 && unsure;
  const allOk = (draft?.cells.length ?? 0) > 0 && (draft?.cells.every((c) => c.state === 'ok') ?? false);
  const locked = running || busy || (draft != null && draft.status !== 'editing');
  const doc = draft?.result?.docId;

  return (
    <div className="s2e3-page" data-testid="s2e3-page" data-status={draft?.status ?? 'new'}>
      <header className="s2e3-row">
        <h1 className="s2e3-title">Thêm đề mới</h1>
        {draft && <Labels machine={draft.machineLabel} />}
        {draft && !draft.inMyShelf && (
          <span className="chip" data-testid="s2e3-shelf">
            Chưa vào "Của tôi"
          </span>
        )}
        <span className="s2e3-grow" />
        {draft && !running && (
          <button type="button" className="btn ghost" onClick={reset} data-testid="s2e3-new">
            <Icon name="plus" />
            Thêm đề khác
          </button>
        )}
      </header>

      <div className="s2e3-tabs" role="tablist" aria-label="Cách thêm đề">
        {(['text', 'image'] as const).map((k) => (
          <button key={k} type="button" role="tab" aria-selected={tab === k} className={`s2e3-tab${tab === k ? ' on' : ''}`} disabled={running} onClick={() => setTab(k)} data-testid={`s2e3-tab-${k}`}>
            <Icon name={k === 'text' ? 'file' : 'camera'} />
            {k === 'text' ? 'Dán chữ' : 'Tải ảnh'}
          </button>
        ))}
      </div>

      {notice && (
        <p className={notice.kind === 'error' ? 'alert' : 's2e3-ban pri'} role={notice.kind === 'error' ? 'alert' : 'status'} data-testid="s2e3-notice">
          <MathText text={notice.text} />
        </p>
      )}

      {tab === 'image' && !locked && (
        <label className="s2e3-drop" data-testid="s2e3-drop">
          <Icon name="camera" />
          <span>
            <b>Chọn ảnh đề</b>
            <span className="s2e3-sub muted"> Ảnh JPG hoặc PNG, tối đa 5 MB, mỗi lần một ảnh. Chụp thẳng, đủ sáng.</span>
          </span>
          <input type="file" multiple accept="image/jpeg,image/png,.jpg,.jpeg,.png,.gif,.pdf" onChange={(e) => onFiles(e.target.files)} data-testid="s2e3-file" />
        </label>
      )}

      {showCells && draft ? (
        <section className="card s2e3-card">
          <ImageCells cells={draft.cells} busy={busy} onChange={(cells) => act(() => updateDraft(draft.id, { cells }))} />
          <div className="s2e3-row">
            <button type="button" className="btn ghost" disabled={busy} onClick={() => act(() => updateDraft(draft.id, { cells: draft.cells.map((c) => ({ text: c.text })) }), 'Đã lưu nháp')} data-testid="s2e3-save-draft">
              <Icon name="save" />
              Lưu nháp
            </button>
            <span className="s2e3-grow" />
            <button type="button" className="btn" disabled data-testid="s2e3-confirm-text">
              <Icon name="check" />
              Xác nhận chữ và dựng tiếp
            </button>
          </div>
        </section>
      ) : (
        (tab === 'text' || draft) && (
          <section className="card s2e3-card">
            <label className="s2e3-cap" htmlFor="s2e3-statement">
              Đề đã chép lại{image ? ' · thầy cô sửa được' : ''}
            </label>
            <textarea
              id="s2e3-statement"
              className="s2e3-text"
              value={text}
              readOnly={locked}
              onChange={(e) => onType(e.target.value)}
              placeholder="Dán hoặc gõ đề vào đây"
              data-math
              data-testid="s2e3-statement"
            />
            <div className="s2e3-row">
              <span className={`s2e3-sub${issue && issue !== MSG.empty ? ' bad' : ' muted'}`} data-testid="s2e3-length">
                {issue ?? `${charCount(text)} / ${MAX_CHARS} ký tự`}
              </span>
              <span className="s2e3-grow" />
              {image && draft?.textConfirmed && (
                <span className="chip grn" data-testid="s2e3-confirmed">
                  Đã xác nhận chữ
                </span>
              )}
              {draft?.status === 'editing' && image && !draft.textConfirmed ? (
                <button type="button" className="btn" disabled={busy || issue !== null} onClick={build} data-testid={allOk ? 's2e3-build' : 's2e3-confirm-text'}>
                  <Icon name="cube" />
                  {allOk ? 'Dựng hình' : 'Xác nhận chữ và dựng tiếp'}
                </button>
              ) : (
                <button type="button" className="btn" disabled={locked || issue !== null} onClick={build} data-testid="s2e3-build">
                  <Icon name="cube" />
                  Dựng hình
                </button>
              )}
            </div>
          </section>
        )
      )}

      {job && (
        <Stages
          job={job}
          busy={busy}
          onCancel={() => act(async () => (await cancelJob(job.id), fetchDraft(job.draftId)))}
          onRetry={() => act(async () => (await retryJob(job.id), fetchDraft(job.draftId)))}
        />
      )}

      {draft?.status === 'ai_figure' && <Unbuildable draft={draft} busy={busy} act={act} />}

      {draft?.result && doc && !running && (
        <ResultPanel
          result={draft.result}
          busy={busy}
          onUseMachine={() => act(async () => (await takeMachineAnswer(doc), fetchDraft(draft.id)))}
          onSetStated={(v) =>
            act(async () => {
              const view = await setStatedAnswer(doc, v);
              await checkVersion(doc, view.latestVersion);
              return fetchDraft(draft.id);
            })
          }
        />
      )}
    </div>
  );
}
