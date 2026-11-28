import { useState } from 'react';
import { Link, useParams } from 'react-router';
import { formatVnDateTime } from '../../shared/time/vnTime.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import {
  errorText,
  fromLocalInput,
  MSG,
  newKey,
  releaseBeforeDue,
  setsApi,
  STATUS_LABEL,
  toLocalInput,
  useLoad,
  type Candidate,
  type Options,
  type SetDetail,
} from './api.ts';
import { PreviewPanel } from './PreviewPanel.tsx';
import { MathText } from '../../shared/math/MathText.tsx';

interface Row {
  problemVersionId: string;
  code: string;
  title: string;
  needsReview: boolean;
  release: string; // datetime-local khi lịch "Từng bài"
}

/** C3-03 · Soạn bộ bài: chọn đề, kéo thả thứ tự, hạn làm (nhãn), lịch mở lời giải, xem trước, giao (US-S3E2-01…03). */
export function ComposeSetPage() {
  const { id } = useParams();
  const [opts, retryOpts] = useLoad<Options>('s3e2-options', (s) => setsApi.options(s));
  const [set, retrySet] = useLoad<SetDetail | null>(`s3e2-set-${id ?? 'new'}`, (s) =>
    id ? setsApi.get(id, s) : Promise.resolve(null),
  );
  if (opts.state === 'loading' || set.state === 'loading') return <p className="muted">Đang tải bộ bài…</p>;
  if (opts.state === 'error' || set.state === 'error')
    return (
      <div className="alert" role="alert" data-testid="s3e2-load-error">
        Chưa tải được bộ bài.{' '}
        <button
          className="btn ghost sm"
          onClick={() => {
            retryOpts();
            retrySet();
          }}
        >
          Thử lại
        </button>
      </div>
    );
  return <Compose key={set.data?.id ?? 'new'} options={opts.data} initial={set.data} />;
}

function Compose({ options, initial }: { options: Options; initial: SetDetail | null }) {
  const [setId, setSetId] = useState<string | null>(initial?.id ?? null);
  const [status, setStatus] = useState(initial?.status ?? 'draft');
  const [title, setTitle] = useState(initial?.title ?? '');
  const [classId, setClassId] = useState(initial?.classId ?? '');
  const [due, setDue] = useState(toLocalInput(initial?.dueAt));
  const [mode, setMode] = useState<'set' | 'item'>(initial?.items.some((i) => i.ownReleaseAt) ? 'item' : 'set');
  const [release, setRelease] = useState(toLocalInput(initial?.releaseAt));
  const [rows, setRows] = useState<Row[]>(
    (initial?.items ?? []).map((i) => ({
      problemVersionId: i.problemVersionId,
      code: i.code,
      title: i.title,
      needsReview: i.needsReview,
      release: toLocalInput(i.ownReleaseAt),
    })),
  );
  const [keepEarly, setKeepEarly] = useState(false);
  const [note, setNote] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [step, setStep] = useState<'none' | 'review' | 'confirm'>('none');
  const [assignKey, setAssignKey] = useState(newKey);
  const [preview, setPreview] = useState(false);
  const [dragFrom, setDragFrom] = useState<number | null>(null);
  const draft = status === 'draft';
  const dueIso = fromLocalInput(due);
  const releaseOf = (r: Row) => (mode === 'item' && r.release ? fromLocalInput(r.release) : fromLocalInput(release));
  const early =
    releaseBeforeDue(fromLocalInput(release), dueIso) || (mode === 'item' && rows.some((r) => releaseBeforeDue(releaseOf(r), dueIso)));
  const className = options.classes.find((c) => c.id === classId)?.name ?? initial?.className ?? '';
  const duePast = !!dueIso && new Date(dueIso).getTime() < new Date(options.serverNow).getTime();

  const add = (p: Candidate) => {
    setNote(null);
    if (rows.some((r) => r.problemVersionId === p.problemVersionId)) return setNote(MSG.s24); // FC3
    if (rows.length >= 20) return setNote(MSG.s23); // FC2
    setRows([...rows, { problemVersionId: p.problemVersionId, code: p.code, title: p.title, needsReview: p.needsReview, release: '' }]);
  };
  const move = (from: number, to: number) => {
    if (from === to) return;
    const next = [...rows];
    const [r] = next.splice(from, 1);
    next.splice(to, 0, r!);
    setRows(next);
  };
  const body = (list: Row[] = rows) => ({
    title,
    classId: classId || null,
    dueAt: dueIso,
    releaseAt: fromLocalInput(release),
    items: list.map((r) => ({ problemVersionId: r.problemVersionId, releaseAt: mode === 'item' && r.release ? fromLocalInput(r.release) : null })),
  });
  const save = async (list: Row[] = rows): Promise<SetDetail | null> => {
    setBusy(true);
    setError(null);
    try {
      const out = setId ? await setsApi.update(setId, body(list)) : await setsApi.create(body(list));
      setSetId(out.id);
      setNote('Đã lưu nháp');
      if (!setId) window.history.replaceState(window.history.state, '', `/gv/bo-bai/${out.id}`);
      return out;
    } catch (e) {
      setError(errorText(e, 'Chưa lưu được bộ bài. Thầy cô thử lại nhé.'));
      return null;
    } finally {
      setBusy(false);
    }
  };
  const startAssign = async () => {
    setNote(null);
    const saved = await save();
    if (!saved) return;
    if (rows.length < 1 || rows.length > 20) return setError(MSG.s23); // US-S3E2-01-FC1 (máy chủ cũng chặn)
    setAssignKey(newKey());
    setStep(rows.some((r) => r.needsReview) ? 'review' : 'confirm');
  };
  const dropReview = async () => {
    const left = rows.filter((r) => !r.needsReview);
    setRows(left);
    if (await save(left)) setStep('confirm');
  };
  const doAssign = async () => {
    if (!setId) return;
    setBusy(true);
    setError(null);
    try {
      const out = await setsApi.assign(setId, assignKey, true);
      setStatus(out.status);
      setStep('none');
      setNote('Đã giao bộ bài');
    } catch (e) {
      setStep('none');
      setError(errorText(e, 'Chưa giao được bộ bài. Thầy cô thử lại nhé.'));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="s3e2-page" data-testid="s3e2-compose">
      <div className="s3e2-head">
        <h1 className="s3e2-title">{setId ? title || 'Bộ bài' : 'Soạn bộ bài'}</h1>
        <span className={`chip ${draft ? 'yel' : 'grn'}`} data-testid="s3e2-status">
          {STATUS_LABEL[status]}
        </span>
        <span className="s3e2-grow" />
        <Link className="btn ghost" to="/gv/bo-bai" data-testid="s3e2-back">
          <Icon name="back" />
          Danh sách bộ bài
        </Link>
      </div>
      {error ? (
        <div className="alert" role="alert" data-testid="s3e2-error">
          {error}
        </div>
      ) : null}
      {note ? (
        <p className="s3e2-note" role="status" data-testid="s3e2-note">
          {note}
        </p>
      ) : null}
      <div className="s3e2-compose">
        <section className="card s3e2-card" aria-label="Đề trong bộ">
          <h2 className="s3e2-h2" data-testid="s3e2-count">
            Đề trong bộ ({rows.length})
          </h2>
          {rows.length === 0 ? <p className="empty">Chưa có đề nào. Thêm đề từ Kho đề bên dưới.</p> : null}
          <ol className="s3e2-items" data-testid="s3e2-items">
            {rows.map((r, k) => (
              <li
                key={r.problemVersionId}
                className={`s3e2-item${r.needsReview ? ' review' : ''}`}
                data-testid={`s3e2-item-${r.code}`}
                draggable={draft}
                onDragStart={() => setDragFrom(k)}
                onDragOver={(e) => e.preventDefault()}
                onDrop={(e) => {
                  e.preventDefault();
                  if (dragFrom !== null) move(dragFrom, k);
                  setDragFrom(null);
                }}
              >
                <span className="s3e2-num">{k + 1}</span>
                <div className="s3e2-grow">
                  <b data-math><MathText text={r.title} /></b>
                  {mode === 'item' && draft ? (
                    <label className="s3e2-inline">
                      Giờ mở riêng
                      <input
                        type="datetime-local"
                        value={r.release}
                        data-testid={`s3e2-item-release-${r.code}`}
                        onChange={(e) => setRows(rows.map((x, j) => (j === k ? { ...x, release: e.target.value } : x)))}
                      />
                    </label>
                  ) : null}
                </div>
                {r.needsReview ? (
                  <span className="chip yel">
                    <Icon name="warn" />
                    Cần xem lại trước khi giao
                  </span>
                ) : (
                  <span className="chip grn">
                    <Icon name="check" />
                    Sẵn sàng
                  </span>
                )}
                {draft ? (
                  <button className="btn ghost sm" data-testid={`s3e2-remove-${r.code}`} onClick={() => setRows(rows.filter((_, j) => j !== k))}>
                    Bỏ
                  </button>
                ) : null}
              </li>
            ))}
          </ol>
          {draft ? <ProblemPicker problems={options.problems} onAdd={add} /> : null}
        </section>
        <section className="card s3e2-card" aria-label="Hạn và lời giải">
          <h2 className="s3e2-h2">Hạn và lời giải</h2>
          <label className="s3e2-field">
            Tên bộ bài
            <input value={title} maxLength={80} disabled={!draft} data-testid="s3e2-title" onChange={(e) => setTitle(e.target.value)} />
          </label>
          <label className="s3e2-field">
            Giao cho lớp
            <select value={classId} disabled={!draft} data-testid="s3e2-class" onChange={(e) => setClassId(e.target.value)}>
              <option value="">— Chọn lớp —</option>
              {options.classes.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </label>
          <label className="s3e2-field">
            Hạn làm (chỉ là nhãn)
            <input type="datetime-local" value={due} disabled={!draft} data-testid="s3e2-due" onChange={(e) => setDue(e.target.value)} />
          </label>
          <fieldset className="s3e2-field" disabled={!draft}>
            <legend>Lịch mở lời giải</legend>
            <label className="s3e2-inline">
              <input type="radio" name="s3e2-mode" checked={mode === 'set'} data-testid="s3e2-mode-set" onChange={() => setMode('set')} />
              Cả bộ
            </label>
            <label className="s3e2-inline">
              <input type="radio" name="s3e2-mode" checked={mode === 'item'} data-testid="s3e2-mode-item" onChange={() => setMode('item')} />
              Từng bài
            </label>
            <input
              type="datetime-local"
              value={release}
              aria-label="Giờ mở lời giải của bộ"
              data-testid="s3e2-release"
              onChange={(e) => {
                setRelease(e.target.value);
                setKeepEarly(false);
              }}
            />
          </fieldset>
          {early && draft && !keepEarly ? (
            <div className="s3e2-banner red" role="alert" data-testid="s3e2-warn-early">
              <b>{MSG.s21}</b>
              <div className="s3e2-actions">
                <button
                  className="btn ghost sm"
                  data-testid="s3e2-fix-later"
                  onClick={() => {
                    if (dueIso) setRelease(toLocalInput(new Date(new Date(dueIso).getTime() + 3_600_000).toISOString()));
                    setRows(rows.map((r) => ({ ...r, release: '' })));
                  }}
                >
                  Dời giờ mở sau hạn
                </button>
                <button className="btn ghost sm" data-testid="s3e2-keep-early" onClick={() => setKeepEarly(true)}>
                  Giữ giờ mở sớm
                </button>
              </div>
            </div>
          ) : null}
          {initial && !draft && initial.releaseAt ? (
            <p className="s3e2-help">Lời giải mở lúc {formatVnDateTime(initial.releaseAt)}</p>
          ) : null}
          {draft ? (
            <div className="s3e2-actions">
              <button className="btn ghost" disabled={busy} data-testid="s3e2-save" onClick={() => void save()}>
                <Icon name="save" />
                Lưu nháp
              </button>
              <button className="btn ghost" disabled={!setId} data-testid="s3e2-preview-btn" onClick={() => setPreview(!preview)}>
                <Icon name="eye" />
                Xem trước như học sinh
              </button>
              <button
                className="btn"
                disabled={busy || !classId || (early && !keepEarly)}
                data-testid="s3e2-assign"
                onClick={() => void startAssign()}
              >
                <Icon name="send" />
                Giao
              </button>
              {!classId ? (
                <span className="s3e2-help" data-testid="s3e2-assign-note">
                  {MSG.s25}
                </span>
              ) : null}
            </div>
          ) : null}
        </section>
      </div>
      {step === 'review' ? (
        <div className="s3e2-banner yel" role="alert" data-testid="s3e2-review-banner">
          <b>{MSG.s22}</b>
          <ul className="s3e2-review-list">
            {rows
              .filter((r) => r.needsReview)
              .map((r) => (
                <li key={r.problemVersionId} className="s3e2-item review" data-testid={`s3e2-review-item-${r.code}`}>
                  {r.title}
                </li>
              ))}
          </ul>
          <div className="s3e2-actions">
            <button className="btn ghost" data-testid="s3e2-review-drop" onClick={() => void dropReview()}>
              Bỏ đề khỏi bộ
            </button>
            <button className="btn" data-testid="s3e2-review-keep" onClick={() => setStep('confirm')}>
              Vẫn giao
            </button>
          </div>
        </div>
      ) : null}
      {step === 'confirm' ? (
        <div className="s3e2-banner" role="dialog" aria-label="Xác nhận giao" data-testid="s3e2-confirm">
          <b>
            Giao “{title}” cho lớp {className}?
          </b>
          {duePast ? (
            <p className="s3e2-help" data-testid="s3e2-due-past">
              Hạn làm đã qua. Học sinh sẽ thấy nhãn “{MSG.s28}” nhưng vẫn làm được.
            </p>
          ) : null}
          <div className="s3e2-actions">
            <button className="btn ghost" onClick={() => setStep('none')}>
              Để sau
            </button>
            <button className="btn" disabled={busy} data-testid="s3e2-confirm-ok" onClick={() => void doAssign()}>
              Xác nhận
            </button>
          </div>
        </div>
      ) : null}
      {preview && setId ? <PreviewPanel setId={setId} /> : null}
    </div>
  );
}

function ProblemPicker({ problems, onAdd }: { problems: Candidate[]; onAdd: (p: Candidate) => void }) {
  return (
    <details className="s3e2-picker" open data-testid="s3e2-picker">
      <summary>Thêm đề từ Kho đề</summary>
      <ul className="s3e2-items">
        {problems.map((p) => (
          <li key={p.problemVersionId} className="s3e2-item" data-testid={`s3e2-problem-${p.code}`}>
            <div className="s3e2-grow">
              <b data-math><MathText text={p.title} /></b>
              <div className="s3e2-help">
                {p.code}
                {p.needsReview ? ' · Cần xem lại' : ' · Máy kiểm chứng'}
                {p.gradable ? '' : ' · chưa có đáp số máy chấm được'}
              </div>
            </div>
            <button className="btn ghost sm" disabled={!p.gradable} data-testid={`s3e2-add-${p.code}`} onClick={() => onAdd(p)}>
              <Icon name="plus" />
              Thêm
            </button>
          </li>
        ))}
      </ul>
    </details>
  );
}
