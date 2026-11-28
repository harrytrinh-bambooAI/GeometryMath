import { useCallback, useEffect, useState } from 'react';
import { ErrorNotice } from '../../shared/ui/ErrorNotice.tsx';
import { formatVnDateTime } from '../../shared/time/vnTime.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import {
  activateVersion,
  addGrant,
  archiveDoc,
  createManual,
  editVersion,
  fromLessonPlan,
  getDoc,
  listDocs,
  listGrants,
  myClasses,
  revokeGrant,
  submitVersion,
  uploadFile,
  type ClassLine,
  type KbDoc,
  type KbGrant,
  type KbListItem,
} from './api.ts';
import { ACCEPT, endOfDayVn, fileProblem, nextAction, statusChip } from './logic.ts';

/**
 * "Kho tri thức" (C5, US-S5E3-01, 02): danh sách tài liệu, thêm (tải docx/pdf/md ≤ 10 MB, soạn tay Markdown, giáo án đã
 * duyệt), xem mục đã tách, sửa (bản đã dùng → phiên bản mới, MSG-S5-23), Gửi duyệt → Đưa lên Đang dùng, Lưu trữ, gắn lớp có
 * hạn. data-testid bắt đầu bằng 's5e3-'.
 */
export function KbPage() {
  const [items, setItems] = useState<KbListItem[]>([]);
  const [doc, setDoc] = useState<KbDoc | null>(null);
  const [grants, setGrants] = useState<KbGrant[]>([]);
  const [classes, setClasses] = useState<ClassLine[]>([]);
  const [error, setError] = useState<unknown>(null);
  const [toast, setToast] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [planCode, setPlanCode] = useState('');
  const [fileMsg, setFileMsg] = useState<string | null>(null);
  const [editing, setEditing] = useState<string | null>(null);
  const [grantClass, setGrantClass] = useState('');
  const [grantTo, setGrantTo] = useState('');

  const reload = useCallback(() => {
    listDocs()
      .then((r) => setItems(r.items))
      .catch(setError);
  }, []);

  useEffect(() => {
    reload();
    myClasses()
      .then((r) => {
        setClasses(r.items);
        if (r.items[0]) setGrantClass(r.items[0].id);
      })
      .catch(() => setClasses([]));
  }, [reload]);

  async function open(id: string, version?: number) {
    setError(null);
    try {
      const d = await getDoc(id, version);
      setDoc(d);
      setEditing(null);
      setGrants((await listGrants(d.id)).items);
    } catch (e) {
      setError(e);
    }
  }

  async function run(fn: () => Promise<KbDoc | null>) {
    setBusy(true);
    setError(null);
    setToast(null);
    try {
      const d = await fn();
      if (d) {
        if (d.message) setToast(d.message);
        if (d.duplicate) await open(d.id);
        else {
          setDoc(d);
          setGrants((await listGrants(d.id)).items);
        }
      }
      reload();
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }

  function onFile(f: File | undefined) {
    if (!f) return;
    const p = fileProblem(f.name, f.size);
    setFileMsg(p);
    if (!p) void run(() => uploadFile(f));
  }

  const action = doc ? nextAction(doc.status) : null;
  return (
    <section className="s5e3-page" data-testid="s5e3-kb-page">
      <h1 className="s5e3-title">Kho tri thức</h1>
      {toast && (
        <p className="s5e3-toast" role="status" data-testid="s5e3-toast">
          <Icon name="info" /> {toast}
        </p>
      )}
      {error !== null && <ErrorNotice error={error} testId="s5e3-error" />}

      <div className="card s5e3-card">
        <h2 className="s5e3-h">Thêm tài liệu</h2>
        <label className="s5e3-field">
          Tải tệp (docx, pdf, md · tối đa 10 MB)
          <input type="file" accept={ACCEPT} disabled={busy} onChange={(e) => onFile(e.target.files?.[0])} data-testid="s5e3-file" />
        </label>
        {fileMsg && (
          <p className="s5e3-inline-error" data-testid="s5e3-file-error">
            {fileMsg}
          </p>
        )}
        <div className="s5e3-row">
          <label className="s5e3-field s5e3-grow">
            Hoặc mã giáo án đã duyệt
            <input value={planCode} placeholder="GA-0001" onChange={(e) => setPlanCode(e.target.value)} data-testid="s5e3-plan" />
          </label>
          <button
            type="button"
            className="btn"
            disabled={busy || !planCode.trim()}
            onClick={() => void run(() => fromLessonPlan(planCode.trim()))}
            data-testid="s5e3-from-plan"
          >
            <Icon name="file" /> Đưa giáo án vào kho
          </button>
        </div>
        <label className="s5e3-field">
          Hoặc soạn tay · Tên tài liệu
          <input value={title} maxLength={200} onChange={(e) => setTitle(e.target.value)} data-testid="s5e3-title-input" />
        </label>
        <textarea
          className="s5e3-body"
          rows={5}
          value={content}
          placeholder={'## Tên mục\nNội dung mục…'}
          onChange={(e) => setContent(e.target.value)}
          data-testid="s5e3-content"
        />
        <div className="s5e3-row">
          <button
            type="button"
            className="btn primary"
            disabled={busy || !title.trim() || !content.trim()}
            onClick={() => void run(() => createManual(title.trim(), content))}
            data-testid="s5e3-create"
          >
            <Icon name="plus" /> Lưu Nháp
          </button>
        </div>
      </div>

      <div className="s5e3-split">
        <ul className="card s5e3-list" data-testid="s5e3-list">
          {items.length === 0 && <li className="s5e3-muted">Chưa có tài liệu.</li>}
          {items.map((i) => (
            <li key={i.id}>
              <button type="button" className="s5e3-item" onClick={() => void open(i.id)} data-testid={`s5e3-item-${i.code}`}>
                <strong>{i.code}</strong> {i.title}
                <span className={statusChip(i.status)}>
                  v{i.latestVersion} · {i.statusLabel}
                </span>
                {i.humanLabel === 'AI soạn' && <span className="chip">AI soạn</span>}
              </button>
            </li>
          ))}
        </ul>

        {doc && (
          <div className="card s5e3-card" data-testid="s5e3-doc">
            <div className="s5e3-row">
              <h2 className="s5e3-h s5e3-grow">
                {doc.code} · {doc.title}
              </h2>
              <span className="chip" data-testid="s5e3-human">
                {doc.humanLabel}
              </span>
            </div>
            <div className="s5e3-row">
              <label className="s5e3-field">
                Phiên bản
                <select value={doc.version} onChange={(e) => void open(doc.id, Number(e.target.value))} data-testid="s5e3-version">
                  {doc.versions.map((v) => (
                    <option key={v.version} value={v.version}>
                      v{v.version} · {v.statusLabel}
                    </option>
                  ))}
                </select>
              </label>
              <span className={statusChip(doc.status)} data-testid="s5e3-status">
                {doc.statusLabel}
              </span>
              {doc.sourceLabel && <span className="s5e3-muted">Nguồn: {doc.sourceLabel}</span>}
              <span className="s5e3-grow" />
              {action === 'submit' && (
                <button type="button" className="btn" disabled={busy} onClick={() => void run(() => submitVersion(doc.id, doc.version))} data-testid="s5e3-submit">
                  <Icon name="send" /> Gửi duyệt
                </button>
              )}
              {action === 'activate' && (
                <button
                  type="button"
                  className="btn primary"
                  disabled={busy}
                  onClick={() => void run(() => activateVersion(doc.id, doc.version))}
                  data-testid="s5e3-activate"
                >
                  <Icon name="check" /> Duyệt, đưa lên Đang dùng
                </button>
              )}
              <button type="button" className="btn" disabled={busy} onClick={() => setEditing(doc.content)} data-testid="s5e3-edit">
                <Icon name="pencil" /> Sửa
              </button>
              {!doc.archived && (
                <button type="button" className="btn" disabled={busy} onClick={() => void run(() => archiveDoc(doc.id))} data-testid="s5e3-archive">
                  <Icon name="lock" /> Lưu trữ
                </button>
              )}
            </div>
            {editing !== null && (
              <div className="s5e3-card">
                {doc.wasActive && <p className="s5e3-muted">Phiên bản này đã được dùng, sửa sẽ tạo phiên bản mới.</p>}
                <textarea className="s5e3-body" rows={8} value={editing} onChange={(e) => setEditing(e.target.value)} data-testid="s5e3-edit-body" />
                <div className="s5e3-row">
                  <button
                    type="button"
                    className="btn primary"
                    disabled={busy || !editing.trim()}
                    onClick={() => void run(() => editVersion(doc.id, doc.version, editing))}
                    data-testid="s5e3-save"
                  >
                    <Icon name="save" /> Lưu
                  </button>
                </div>
              </div>
            )}
            <ol className="s5e3-sections" data-testid="s5e3-sections">
              {doc.sections.map((s) => (
                <li key={s.ord}>
                  <strong>{s.title}</strong>
                  {s.codes.map((c) => (
                    <span key={c} className="chip">
                      {c}
                    </span>
                  ))}
                  <p className="s5e3-section-body">{s.body}</p>
                </li>
              ))}
            </ol>

            <h3 className="s5e3-h">Gắn cho lớp</h3>
            <ul className="s5e3-grants" data-testid="s5e3-grants">
              {grants.map((g) => (
                <li key={g.id}>
                  {classes.find((c) => c.id === g.classId)?.name ?? 'Lớp'}
                  {g.validTo ? ` · đến ${formatVnDateTime(g.validTo)}` : ''}
                  {!g.effective && <span className="chip muted">Hết hiệu lực</span>}
                  <button type="button" className="btn ghost" onClick={() => void run(async () => (await revokeGrant(doc.id, g.id), null))} aria-label="Bỏ gắn">
                    <Icon name="x" />
                  </button>
                </li>
              ))}
            </ul>
            <div className="s5e3-row">
              <label className="s5e3-field">
                Lớp
                <select value={grantClass} onChange={(e) => setGrantClass(e.target.value)} data-testid="s5e3-grant-class">
                  {classes.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name}
                    </option>
                  ))}
                </select>
              </label>
              <label className="s5e3-field">
                Hết hiệu lực (không bắt buộc)
                <input type="date" value={grantTo} onChange={(e) => setGrantTo(e.target.value)} data-testid="s5e3-grant-to" />
              </label>
              <button
                type="button"
                className="btn"
                disabled={busy || !grantClass || doc.activeVersion === undefined}
                onClick={() =>
                  void run(async () => {
                    await addGrant(doc.id, { classId: grantClass, validTo: endOfDayVn(grantTo) });
                    setGrants((await listGrants(doc.id)).items);
                    return null;
                  })
                }
                data-testid="s5e3-grant"
              >
                <Icon name="assign" /> Gắn
              </button>
            </div>
          </div>
        )}
      </div>
    </section>
  );
}
