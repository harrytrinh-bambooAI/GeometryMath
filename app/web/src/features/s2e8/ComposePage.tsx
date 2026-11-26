import { useMemo, useState } from 'react';
import { Link, useParams } from 'react-router';
import { ApiError } from '../../shared/api/errors.ts';
import { formatVnDateTime } from '../../shared/time/vnTime.ts';
import { practiceApi, type AttemptView, type Candidate, type PlayView, type SetDetail, type SetInput } from './api.ts';
import { STATUS_LABEL, useLoad } from './load.ts';
import { LoadError } from './LoadError.tsx';
import { PracticeRunner, type RunnerDriver } from './PracticeRunner.tsx';

export const MSG_PAST = 'Giờ mở đã qua, lời giải mở ngay cho học sinh. Vẫn giao?';
export const MSG_NO_ANSWER = 'Thêm đáp số ở Soát lời giải';

interface Row {
  versionId: string;
  kind: 'exact' | 'approx';
  tolerance: string;
  attempts: number;
  release: string;
}

interface Refs {
  problems: Candidate[];
  classes: Array<{ id: string; name: string }>;
  set: SetDetail | null;
}

/** ISO → giá trị của ô datetime-local (giờ máy) và ngược lại. */
function toLocalInput(iso: string | null): string {
  if (!iso) return '';
  const d = new Date(iso);
  const p = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}`;
}
const toIso = (local: string): string | null => (local ? new Date(local).toISOString() : null);

const lowerFirst = (s: string) => (s ? s.charAt(0).toLocaleLowerCase('vi') + s.slice(1) : s);

/** "Xem trước như học sinh": chấm thử ở máy chủ, không ghi Attempt; lượt chỉ đếm trong phiên xem trước (BR-S2E8-28). */
function previewDriver(setId: string): RunnerDriver {
  const wrong = new Map<number, number>();
  return {
    figures: false,
    check: async (item, answer) => {
      const r = await practiceApi.previewCheck(setId, item.position, answer);
      const used = (wrong.get(item.position) ?? 0) + (r.outcome === 'wrong' ? 1 : 0);
      wrong.set(item.position, used);
      const left = Math.max(0, item.maxAttempts - used);
      const out: AttemptView = {
        outcome: r.outcome,
        reason: r.reason,
        canonical: r.canonical,
        note: r.note,
        hint: r.hint,
        attemptsLeft: left,
        maxAttempts: item.maxAttempts,
        canReport: false,
        state: r.outcome === 'correct' ? 'correct' : left === 0 ? 'review' : 'doing',
        message:
          r.outcome === 'correct'
            ? r.canonical
              ? `Đúng rồi! Dạng chuẩn: ${r.canonical}`
              : 'Đúng rồi!'
            : r.outcome === 'wrong'
              ? left > 0
                ? `Chưa đúng. Còn ${left} lượt.`
                : 'Chưa đúng. Em xem lại cùng thầy cô nhé.'
              : !r.reason || r.reason === 'Em hãy nhập đáp số.'
                ? 'Em hãy nhập đáp số.'
                : `Đáp số chưa viết đúng: ${lowerFirst(r.reason)}. Lượt thử chưa bị trừ.`,
      };
      return out;
    },
  };
}

function previewView(set: SetDetail): PlayView {
  return {
    id: set.id,
    title: set.title,
    status: 'assigned',
    className: set.className ?? '',
    current: 1,
    items: set.items.map((i) => ({
      assignmentId: `xem-truoc-${i.position}`,
      position: i.position,
      statement: i.statement,
      problemKind: i.problemKind,
      maxAttempts: i.maxAttempts,
      attemptsLeft: i.maxAttempts,
      state: 'todo',
      hint: null,
      lastAnswer: null,
      canonical: null,
      canReport: false,
      reported: false,
    })),
  };
}

function Preview({ set, onClose }: { set: SetDetail; onClose: () => void }) {
  const driver = useMemo(() => previewDriver(set.id), [set.id]);
  return (
    <section className="s2e8-preview" data-testid="s2e8-preview">
      <div className="s2e8-head">
        <span className="chip yel">Xem trước như học sinh · không ghi lượt thử</span>
        <button type="button" className="btn ghost" onClick={onClose} data-testid="s2e8-preview-close">
          Thoát xem trước
        </button>
      </div>
      <PracticeRunner view={previewView(set)} driver={driver} onFinish={onClose} finishLabel="Thoát xem trước" preview />
    </section>
  );
}

/** C2-08 · Soạn bài luyện (US-S2E8-05): chọn đề có đáp số máy chấm được, đặt kiểu, sai số, lượt, giờ mở; xem trước; giao. */
export function ComposePage() {
  const { id } = useParams();
  const [load, retry] = useLoad<Refs>(`compose:${id ?? 'moi'}`, async (s) => {
    const [problems, classes, set] = await Promise.all([
      practiceApi.problems(s),
      practiceApi.classes(s),
      id ? practiceApi.set(id, s) : Promise.resolve(null),
    ]);
    return { problems: problems.items, classes: classes.items, set };
  });
  if (load.state === 'loading') return <p className="muted" data-testid="s2e8-loading">Đang tải…</p>;
  if (load.state === 'error') return <LoadError notFound={load.notFound} onRetry={retry} what="bài luyện" />;
  return <ComposeForm key={load.data.set?.id ?? 'moi'} refs={load.data} />;
}

function ComposeForm({ refs }: { refs: Refs }) {
  const [set, setSet] = useState<SetDetail | null>(refs.set);
  const [title, setTitle] = useState(refs.set?.title ?? '');
  const [release, setRelease] = useState(toLocalInput(refs.set?.releaseAt ?? null));
  const [rows, setRows] = useState<Row[]>(
    (refs.set?.items ?? []).map((i) => ({
      versionId: i.problemVersionId,
      kind: i.answerKind,
      tolerance: i.tolerance == null ? '' : String(i.tolerance).replace('.', ','),
      attempts: i.maxAttempts,
      release: refs.set?.status === 'draft' ? toLocalInput(i.releaseAt) : '',
    })),
  );
  const [classId, setClassId] = useState(refs.set?.classId ?? refs.classes[0]?.id ?? '');
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [preview, setPreview] = useState(false);
  const [confirm, setConfirm] = useState<{ past: boolean } | null>(null);

  const locked = set != null && set.status !== 'draft';
  const byVersion = new Map(refs.problems.map((p) => [p.problemVersionId, p]));
  const picked = new Set(rows.map((r) => r.versionId));
  const toggle = (p: Candidate) => {
    setSaved(null);
    setRows((all) =>
      picked.has(p.problemVersionId)
        ? all.filter((r) => r.versionId !== p.problemVersionId)
        : [...all, { versionId: p.problemVersionId, kind: 'exact', tolerance: '', attempts: 3, release: '' }],
    );
  };
  const patch = (versionId: string, change: Partial<Row>) => {
    setSaved(null);
    setRows((all) => all.map((r) => (r.versionId === versionId ? { ...r, ...change } : r)));
  };

  const input = (): SetInput | string => {
    const items = [];
    for (const r of rows) {
      let tolerance: number | null = null;
      if (r.kind === 'approx') {
        tolerance = r.tolerance.trim() === '' ? 0 : Number(r.tolerance.trim().replace(',', '.'));
        if (Number.isNaN(tolerance)) return 'Sai số từ 0 đến 10';
      }
      items.push({ problemVersionId: r.versionId, answerKind: r.kind, tolerance, maxAttempts: r.attempts, releaseAt: toIso(r.release) });
    }
    return { title, releaseAt: toIso(release), items };
  };
  const fail = (e: unknown) => setError(e instanceof ApiError ? e.message : 'Chưa lưu được. Thầy cô thử lại nhé.');

  const save = async (): Promise<SetDetail | null> => {
    const body = input();
    setError(null);
    setSaved(null);
    if (typeof body === 'string') {
      setError(body);
      return null;
    }
    setBusy(true);
    try {
      const out = set ? await practiceApi.update(set.id, body) : await practiceApi.create(body);
      setSet(out);
      setSaved('Đã lưu nháp');
      return out;
    } catch (e) {
      fail(e);
      return null;
    } finally {
      setBusy(false);
    }
  };
  const openPreview = async () => {
    const out = locked ? set : await save();
    if (out) setPreview(true);
  };
  const isPast = () => {
    const now = Date.now();
    return rows.some((r) => {
      const at = r.release || release;
      return at !== '' && new Date(at).getTime() <= now;
    });
  };
  const assign = async (past: boolean) => {
    const out = await save();
    if (!out) {
      setConfirm(null);
      return;
    }
    setBusy(true);
    try {
      setSet(await practiceApi.assign(out.id, classId, past));
      setConfirm(null);
      setSaved('Đã giao bài luyện');
    } catch (e) {
      if (e instanceof ApiError && e.code === 'CONFLICT' && e.message === MSG_PAST) setConfirm({ past: true });
      else {
        setConfirm(null);
        fail(e);
      }
    } finally {
      setBusy(false);
    }
  };

  if (preview && set) return <Preview set={set} onClose={() => setPreview(false)} />;
  const className = refs.classes.find((c) => c.id === classId)?.name ?? set?.className ?? '';
  return (
    <div className="s2e8-compose" data-testid="s2e8-compose" data-status={set?.status ?? 'new'}>
      <section className="card s2e8-pick">
        <div className="s2e8-head">
          <h1 className="s2e8-title">Soạn bài luyện</h1>
          <span className="chip" data-testid="s2e8-compose-status">
            {STATUS_LABEL[set?.status ?? 'draft']}
          </span>
          <Link className="btn ghost" to=".." relative="path">
            Danh sách bài luyện
          </Link>
        </div>
        <label className="s2e8-field">
          Tên bài
          <input
            value={title}
            disabled={locked}
            onChange={(e) => {
              setTitle(e.target.value);
              setSaved(null);
            }}
            placeholder="Ví dụ: Góc trong hình chóp"
            data-testid="s2e8-compose-title"
          />
        </label>
        <h2 className="s2e8-h" data-testid="s2e8-compose-count">
          Chọn đề ({rows.length})
        </h2>
        <ul className="s2e8-list">
          {refs.problems.map((p) => (
            <li
              key={p.problemVersionId}
              className={`s2e8-problem${p.gradable ? '' : ' off'}`}
              data-testid={`s2e8-problem-${p.code}`}
              data-gradable={p.gradable}
            >
              <label className="s2e8-check">
                <input
                  type="checkbox"
                  checked={picked.has(p.problemVersionId)}
                  disabled={!p.gradable || locked}
                  onChange={() => toggle(p)}
                  data-testid={`s2e8-problem-tick-${p.code}`}
                />
                <span className="s2e8-grow">
                  <b>{p.code}</b>{' '}
                  <span data-math className="s2e8-problem-text">
                    {p.statement}
                  </span>
                </span>
              </label>
              {p.gradable ? (
                <span className="chip grn">Máy chấm được</span>
              ) : (
                <span className="s2e8-nograde">
                  <span className="chip yel">Chưa có đáp số máy chấm được</span>
                  <span className="s2e8-help">{MSG_NO_ANSWER}</span>
                </span>
              )}
            </li>
          ))}
        </ul>
      </section>
      <section className="card s2e8-settings">
        <h2 className="s2e8-h">Cài đặt từng câu</h2>
        {rows.length === 0 ? <p className="s2e8-help">Tick đề ở bên để thêm câu.</p> : null}
        <ol className="s2e8-list">
          {rows.map((r, i) => (
            <li key={r.versionId} className="s2e8-item" data-testid={`s2e8-item-${i + 1}`}>
              <b>
                Câu {i + 1} · {byVersion.get(r.versionId)?.code ?? ''}
              </b>
              <div className="s2e8-item-grid">
                <label className="s2e8-field">
                  Kiểu đáp số
                  <select
                    value={r.kind}
                    disabled={locked}
                    onChange={(e) => patch(r.versionId, { kind: e.target.value === 'approx' ? 'approx' : 'exact' })}
                    data-testid={`s2e8-item-kind-${i + 1}`}
                  >
                    <option value="exact">Chính xác</option>
                    <option value="approx">Xấp xỉ</option>
                  </select>
                </label>
                <label className="s2e8-field">
                  Sai số ±
                  <input
                    value={r.kind === 'approx' ? r.tolerance : ''}
                    disabled={locked || r.kind !== 'approx'}
                    inputMode="decimal"
                    placeholder={r.kind === 'approx' ? '0,5' : 'không áp dụng'}
                    onChange={(e) => patch(r.versionId, { tolerance: e.target.value })}
                    data-math
                    data-testid={`s2e8-item-tolerance-${i + 1}`}
                  />
                </label>
                <label className="s2e8-field">
                  Số lượt thử
                  <select
                    value={r.attempts}
                    disabled={locked}
                    onChange={(e) => patch(r.versionId, { attempts: Number(e.target.value) })}
                    data-testid={`s2e8-item-attempts-${i + 1}`}
                  >
                    {[1, 2, 3, 4, 5].map((n) => (
                      <option key={n} value={n}>
                        {n}
                      </option>
                    ))}
                  </select>
                </label>
                <label className="s2e8-field">
                  Giờ mở lời giải riêng
                  <input
                    type="datetime-local"
                    value={r.release}
                    disabled={locked}
                    onChange={(e) => patch(r.versionId, { release: e.target.value })}
                    data-testid={`s2e8-item-release-${i + 1}`}
                  />
                </label>
              </div>
              {locked && set?.items[i]?.releaseAt ? (
                <span className="s2e8-help" data-testid={`s2e8-item-release-label-${i + 1}`}>
                  Lời giải mở lúc {formatVnDateTime(set.items[i]!.releaseAt!)}
                </span>
              ) : null}
            </li>
          ))}
        </ol>
        <label className="s2e8-field">
          Giờ mở lời giải cho cả bài (để trống = chưa đặt, lời giải khóa)
          <input type="datetime-local" value={release} disabled={locked} onChange={(e) => setRelease(e.target.value)} data-testid="s2e8-compose-release" />
        </label>
        <label className="s2e8-field">
          Giao cho lớp
          <select value={classId} disabled={locked} onChange={(e) => setClassId(e.target.value)} data-testid="s2e8-compose-class">
            {refs.classes.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </label>
        {error ? (
          <p className="s2e8-err" role="alert" data-testid="s2e8-compose-error">
            {error}
          </p>
        ) : null}
        {saved ? (
          <p className="s2e8-fb ok" role="status" data-testid="s2e8-compose-saved">
            {saved}
          </p>
        ) : null}
        <div className="s2e8-actions">
          {locked ? null : (
            <button type="button" className="btn ghost" onClick={() => void save()} disabled={busy} data-testid="s2e8-compose-save">
              Lưu nháp
            </button>
          )}
          <button type="button" className="btn ghost" onClick={() => void openPreview()} disabled={busy || rows.length === 0} data-testid="s2e8-compose-preview">
            Xem trước như học sinh
          </button>
          {locked ? null : (
            <button type="button" className="btn" onClick={() => setConfirm({ past: isPast() })} disabled={busy || !classId} data-testid="s2e8-compose-assign">
              Giao
            </button>
          )}
        </div>
        {confirm ? (
          <div className="s2e8-confirm" role="dialog" aria-label="Xác nhận giao bài" data-testid="s2e8-assign-confirm">
            <p>
              Giao bài luyện cho lớp <b>{className}</b>? Sau khi giao thì không sửa được.
            </p>
            {confirm.past ? (
              <p className="s2e8-fb warn" role="alert" data-testid="s2e8-assign-past">
                {MSG_PAST}
              </p>
            ) : null}
            <div className="s2e8-actions">
              <button type="button" className="btn" onClick={() => void assign(confirm.past)} disabled={busy} data-testid="s2e8-assign-yes">
                Xác nhận giao
              </button>
              <button type="button" className="btn ghost" onClick={() => setConfirm(null)} data-testid="s2e8-assign-no">
                Thôi
              </button>
            </div>
          </div>
        ) : null}
      </section>
    </div>
  );
}
