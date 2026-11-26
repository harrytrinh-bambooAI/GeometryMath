import { useCallback, useEffect, useRef, useState } from 'react';
import { useParams } from 'react-router';
import { ApiError } from '../../shared/api/errors.ts';
import { ErrorNotice } from '../../shared/ui/ErrorNotice.tsx';
import { fromScene } from '../../shared/viewer/adapter.ts';
import { Figure3DViewer } from '../../shared/viewer/Figure3DViewer.tsx';
import { approveVersion, checkVersion, decideProposal, fetchDoc, reuseVersion, saveVersion, type DocView, type ProposalView } from './api.ts';
import { firstChanged, insertAfter, isDirty, MAX_STEPS, move, MSG, validateDraft, type DraftIssue, type DraftStep } from './logic.ts';
import { ReviewBox } from './ReviewBox.tsx';
import { StepList } from './StepList.tsx';
import { TrustLabels } from './TrustLabels.tsx';
import './s2e5.css';
import { Icon } from '../../shared/ui/Icon.tsx';

interface Notice {
  kind: 'info' | 'error';
  text: string;
  retry?: () => void;
}

/**
 * C2-03 "Soát và duyệt lời giải" (US-S2E5-01…04; Design/prototype/s23.js soat()). Ba cột: thẻ hình + nhãn hai trục,
 * các bước lời giải (sửa, thêm, xóa, kéo đổi thứ tự, StepCheck từng bước), hộp duyệt. Mỗi lần Lưu tạo phiên bản mới.
 */
export function SolutionReviewPage() {
  const { id = '' } = useParams();
  return <ReviewScreen key={id} id={id} />;
}

type Solid = ReturnType<typeof fromScene>;

function ReviewScreen({ id }: { id: string }) {
  const [doc, setDoc] = useState<DocView | null>(null);
  const [solid, setSolid] = useState<Solid | null>(null);
  const [draft, setDraft] = useState<DraftStep[]>([]);
  const [loadError, setLoadError] = useState<unknown>(null);
  const [notice, setNotice] = useState<Notice | null>(null);
  const [issue, setIssue] = useState<DraftIssue | null>(null);
  const [conflict, setConflict] = useState<string | null>(null);
  const [mine, setMine] = useState<string[] | null>(null);
  const [proposalId, setProposalId] = useState<string | undefined>(undefined);
  const [busy, setBusy] = useState(false);
  const seq = useRef(0);

  const apply = useCallback((d: DocView) => {
    setDoc(d);
    setSolid((prev) => prev ?? (d.scene ? fromScene(d.scene) : null)); // hình của đề không đổi theo phiên bản
    setDraft(d.version.steps.map((s) => ({ key: `v${d.version.version}-${s.no}`, text: s.text })));
    setIssue(null);
    setConflict(null);
    setProposalId(undefined);
    setLoadError(null);
  }, []);

  const load = useCallback(
    (version?: number) => {
      setBusy(true);
      fetchDoc(id, version)
        .then(apply)
        .catch((e: unknown) => setLoadError(e))
        .finally(() => setBusy(false));
    },
    [id, apply],
  );

  useEffect(() => {
    const ctl = new AbortController();
    fetchDoc(id, undefined, ctl.signal)
      .then(apply)
      .catch((e: unknown) => {
        if (!(e instanceof DOMException && e.name === 'AbortError')) setLoadError(e);
      });
    return () => ctl.abort();
  }, [id, apply]);

  if (loadError && !doc) return <ErrorNotice error={loadError} onRetry={() => load()} testId="s2e5-error" />;
  if (!doc) {
    return (
      <p className="muted" aria-busy="true" data-testid="s2e5-loading">
        Đang tải lời giải…
      </p>
    );
  }

  const v = doc.version;
  const savedTexts = v.steps.map((s) => s.text);
  const texts = draft.map((d) => d.text);
  const readOnly = !v.latest;
  const dirty = !readOnly && isDirty(savedTexts, texts);
  const newKey = () => `n${++seq.current}`;

  /** Chạy một lệnh ghi; lỗi hiện thành thông báo, không làm mất bản nháp. */
  const run = (call: () => Promise<DocView>, onError?: (e: ApiError) => boolean) => {
    setBusy(true);
    setNotice(null);
    call()
      .then((d) => {
        apply(d);
        if (!d.saved && d.notice) setNotice({ kind: 'info', text: d.notice });
      })
      .catch((e: unknown) => {
        if (e instanceof ApiError && onError?.(e)) return;
        setNotice({ kind: 'error', text: e instanceof Error ? e.message : 'Có lỗi, vui lòng thử lại.' });
      })
      .finally(() => setBusy(false));
  };

  const save = () => {
    setNotice(null);
    const bad = validateDraft(texts);
    setIssue(bad);
    if (bad) return;
    if (!dirty) {
      setNotice({ kind: 'info', text: MSG.noChange });
      return;
    }
    run(
      () => saveVersion(id, v.version, texts.map((t) => t.trim()), proposalId),
      (e) => {
        if (e.code === 'CONFLICT') {
          setConflict(e.message);
          return true;
        }
        const field = e.details[0]?.field ?? '';
        const m = /^steps\[(\d+)\]$/.exec(field);
        if (e.code === 'VALIDATION_FAILED' && field.startsWith('steps')) {
          setIssue({ index: m ? Number(m[1]) : null, message: e.message });
          return true;
        }
        return false;
      },
    );
  };

  const check = () =>
    run(
      () => checkVersion(id, v.version),
      (e) => {
        if (e.code !== 'TIMEOUT') return false;
        load(v.version);
        setNotice({ kind: 'error', text: e.message, retry: check });
        return true;
      },
    );

  const remove = (index: number) => {
    if (draft.length <= 1) {
      setIssue({ index: null, message: MSG.minSteps });
      return;
    }
    setIssue(null);
    setDraft(draft.filter((_, i) => i !== index));
  };

  const addAfter = (no: number, text = '') => {
    if (draft.length >= MAX_STEPS) {
      setIssue({ index: null, message: MSG.maxSteps });
      return;
    }
    setDraft(insertAfter(draft, no, { key: newKey(), text }));
  };

  const editProposal = (p: ProposalView) => {
    addAfter(p.afterStep, p.content);
    setProposalId(p.id);
  };

  const viewVersion = (n: number) => {
    if (dirty) {
      setNotice({ kind: 'info', text: 'Lưu thay đổi trước khi xem phiên bản khác.' });
      return;
    }
    setNotice(null);
    load(n === doc.latestVersion ? undefined : n);
  };

  return (
    <div className="s2e5-page" data-testid="s2e5-page" data-version={v.version} data-latest={doc.latestVersion}>
      <section className="card s2e5-figure" data-testid="s2e5-figure">
        <header className="s2e5-figure-head">
          <h2 className="s2e5-h">Hình</h2>
          <TrustLabels machineLabel={v.machineLabel} machineNote={v.machineNote} humanLabel={v.humanLabel} where="figure" />
        </header>
        {solid && (
          <div className="s2e5-viewer">
            <Figure3DViewer solid={solid} lock="open" readOnly className="s2e5-viewer-inner" testId="s2e5-viewer" />
          </div>
        )}
        <p className="s2e5-statement" data-math data-testid="s2e5-statement">
          <b>Đề{doc.code ? ` ${doc.code}` : ''}:</b> {doc.statement}
        </p>
      </section>

      <section className="s2e5-main">
        <header className="s2e5-main-head">
          <h1 className="s2e5-title" data-testid="s2e5-title">
            Lời giải · v{v.version}
          </h1>
          <span className="chip" data-testid="s2e5-version-state">
            {readOnly ? `Bản cũ, chỉ đọc (mới nhất v${doc.latestVersion})` : dirty ? 'Có thay đổi chưa lưu' : 'Đang soạn'}
          </span>
          <span className="s2e5-grow" />
          <button type="button" className="btn ghost" disabled={busy || dirty} onClick={check} title={dirty ? 'Lưu trước khi kiểm' : undefined} data-testid="s2e5-check"><Icon name="check" />
            Kiểm tra lại
          </button>
          {readOnly ? (
            <button type="button" className="btn" disabled={busy} onClick={() => run(() => reuseVersion(id, v.version))} data-testid="s2e5-reuse"><Icon name="redo" />
              Dùng bản này
            </button>
          ) : (
            <button type="button" className="btn" disabled={busy} onClick={save} data-testid="s2e5-save"><Icon name="save" />
              Lưu
            </button>
          )}
        </header>

        <p className={`s2e5-summary${v.failCount > 0 ? ' bad' : ''}`} role="status" data-testid="s2e5-summary">
          {v.summary}
          {v.failCount > 0 ? '. Máy chặn duyệt: sửa bước lệch rồi bấm "Kiểm tra lại".' : ''}
        </p>

        {conflict && (
          <div className="s2e5-ban yel" role="alert" data-testid="s2e5-conflict">
            <p className="s2e5-ban-title">{conflict}</p>
            <div className="s2e5-row">
              <button type="button" className="btn" onClick={() => load()} data-testid="s2e5-conflict-reload">
                Tải bản mới
              </button>
              <button
                type="button"
                className="btn ghost"
                onClick={() => {
                  setMine(texts);
                  load();
                }}
                data-testid="s2e5-conflict-keep"
              >
                Giữ phần sửa của tôi để ghép tay
              </button>
            </div>
          </div>
        )}
        {issue && (
          <p className="alert" role="alert" data-testid="s2e5-issue"><Icon name="warn" />
            {issue.message}
          </p>
        )}
        {notice && (
          <div className={notice.kind === 'error' ? 'alert' : 's2e5-ban pri'} role="status" data-testid="s2e5-notice">
            {notice.text}
            {notice.retry && (
              <button type="button" className="btn ghost s2e5-retry" onClick={notice.retry} data-testid="s2e5-retry"><Icon name="redo" />
                Thử lại
              </button>
            )}
          </div>
        )}

        <StepList
          draft={draft}
          saved={v.steps}
          changedFrom={readOnly ? v.steps.length : firstChanged(savedTexts, texts)}
          readOnly={readOnly}
          issueIndex={issue?.index ?? null}
          onChange={(i, text) => setDraft(draft.map((d, k) => (k === i ? { ...d, text } : d)))}
          onAddAfter={(no) => addAfter(no)}
          onDelete={remove}
          onMove={(from, to) => setDraft(move(draft, from, to))}
        />
        {!readOnly && draft.length >= MAX_STEPS && (
          <p className="s2e5-sub muted" data-testid="s2e5-max-steps">
            {MSG.maxSteps}
          </p>
        )}

        {mine && (
          <section className="card s2e5-mine" data-testid="s2e5-mine">
            <h3 className="s2e5-cap">Phần sửa của tôi (chưa lưu, để ghép tay)</h3>
            <ol>
              {mine.map((t, i) => (
                <li key={`${i}-${t.length}`} data-math>
                  {t}
                </li>
              ))}
            </ol>
            <button type="button" className="btn ghost" onClick={() => setMine(null)}>
              Đóng
            </button>
          </section>
        )}
      </section>

      <ReviewBox
        doc={doc}
        dirty={dirty}
        busy={busy}
        onApprove={(confirm) => run(() => approveVersion(id, v.version, confirm))}
        onProposal={(action, p) => run(() => decideProposal(id, p.id, action))}
        onEditProposal={editProposal}
        onViewVersion={viewVersion}
      />
    </div>
  );
}
