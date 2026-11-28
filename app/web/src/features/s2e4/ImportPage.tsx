import { useRef, useState } from 'react';
import { Link } from 'react-router';
import { ApiError } from '../../shared/api/errors.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { createDrafts, importFile, type ImportItem, type ImportView } from './api.ts';
import { defaultSelection, formatSize, MSG, pickFile, refLink, summarize } from './logic.ts';
import { MathText } from '../../shared/math/MathText.tsx';

/**
 * C2-04b…d "Nhập từ tệp" (US-S2E4-02, 03; Design/prototype/s23c.js c2-04b-import-tach-bai, c2-04d-import-loi-tep).
 * Chọn một tệp docx / pdf (≤ 10 MB) → máy tách từng bài tập, mỗi mục một ô chọn, kèm đối chiếu trùng với Kho chung và
 * đề của chính giáo viên. Chỉ mục được tick mới thành nháp ("AI soạn", "Cần xem lại"); chưa đề nào vào Kho.
 */
export function ImportPage() {
  const input = useRef<HTMLInputElement | null>(null);
  const [view, setView] = useState<ImportView | null>(null);
  const [selected, setSelected] = useState<number[]>([]);
  const [fileName, setFileName] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [fail, setFail] = useState<string | null>(null);
  const [hint, setHint] = useState<string | null>(null);
  const [done, setDone] = useState<string | null>(null);

  const choose = (files: File[]) => {
    const picked = pickFile(files);
    setHint(null);
    setDone(null);
    if (files[0]) setFileName(files[0].name);
    if (!picked.file) {
      setFail(picked.message);
      if (picked.message) setView(null);
      return;
    }
    setHint(picked.message);
    setFail(null);
    setBusy(true);
    importFile(picked.file)
      .then((v) => {
        setView(v);
        setSelected(defaultSelection(v.items));
      })
      .catch((e: unknown) => {
        setView(null);
        setFail(e instanceof ApiError ? e.message : MSG.generic);
      })
      .finally(() => setBusy(false));
  };

  const toggle = (index: number) => setSelected((s) => (s.includes(index) ? s.filter((i) => i !== index) : [...s, index]));

  const save = () => {
    if (!view || selected.length === 0) return;
    setBusy(true);
    setFail(null);
    createDrafts(view.id, selected)
      .then((v) => {
        setView(v);
        setSelected([]);
        const parts = [`Đã tạo ${v.created} nháp`];
        if (v.updated > 0) parts.push(`cập nhật ${v.updated} nháp`);
        if (v.skipped > 0) parts.push(`bỏ qua ${v.skipped} bài đã có trong kho`);
        setDone(parts.join(' · '));
      })
      .catch((e: unknown) => setFail(e instanceof ApiError ? e.message : MSG.generic))
      .finally(() => setBusy(false));
  };

  const sum = view ? summarize(view.items, selected) : null;

  return (
    <div className="s2e4-page" data-testid="s2e4-import">
      <header className="s2e4-row">
        <h1 className="s2e4-title">Nhập từ tệp</h1>
        <span className="s2e4-grow" />
        <Link className="btn ghost" to="/gv/chinh-nhap" data-testid="s2e4-import-drafts">
          <Icon name="layers" />
          Xem nháp
        </Link>
      </header>

      <section className="card s2e4-card">
        <div className="s2e4-row">
          <Icon name="file" />
          <div className="s2e4-grow">
            <b data-testid="s2e4-import-name">{fileName ?? 'Chưa chọn tệp'}</b>
            <div className="s2e4-sub muted" data-testid="s2e4-import-meta">
              {view ? `${formatSize(view.sizeBytes)} · máy tìm thấy ${view.found} bài tập` : 'Tệp Word (docx) hoặc PDF có lớp chữ, tối đa 10 MB, tối đa 50 bài mỗi tệp'}
            </div>
          </div>
          <input
            ref={input}
            type="file"
            accept=".docx,.pdf"
            className="s2e4-file"
            aria-label="Chọn tệp docx hoặc pdf"
            data-testid="s2e4-import-file"
            onChange={(e) => {
              choose(Array.from(e.target.files ?? []));
              e.target.value = '';
            }}
          />
          <button type="button" className="btn" disabled={busy} onClick={() => input.current?.click()} data-testid="s2e4-import-pick">
            <Icon name="file" />
            {view || fail ? 'Đổi tệp' : 'Chọn tệp'}
          </button>
        </div>
        {busy && (
          <p className="s2e4-sub muted" aria-busy="true" data-testid="s2e4-import-busy">
            Đang xử lý…
          </p>
        )}
        {hint && <p className="s2e4-sub muted">{hint}</p>}
        {fail && (
          <p className="alert" role="alert" data-testid="s2e4-import-error">
            <Icon name="warn" />
            {fail}
          </p>
        )}
        {fail && <p className="s2e4-sub muted">Chưa có gì bị thay đổi trong Kho. Cô chọn tệp khác hoặc sửa tệp rồi tải lại.</p>}
      </section>

      {view && (
        <div className="s2e4-cols">
          <section className="card s2e4-card" aria-label="Các bài tìm thấy">
            <h2 className="s2e4-h">
              {view.found} bài tìm thấy · chọn bài muốn lấy
            </h2>
            <ul className="s2e4-notes" data-testid="s2e4-import-notes">
              {view.notes.map((n) => (
                <li key={n} className="s2e4-sub" data-testid="s2e4-import-note">
                  <Icon name="info" inline />
                  {n}
                </li>
              ))}
            </ul>
            <ul className="s2e4-list" data-testid="s2e4-import-items">
              {view.items.map((it) => (
                <ItemRow key={it.index} it={it} checked={selected.includes(it.index)} disabled={busy} onToggle={() => toggle(it.index)} />
              ))}
            </ul>
          </section>

          <aside className="card s2e4-card s2e4-summary" aria-label="Sẽ lấy">
            <h2 className="s2e4-h">Sẽ lấy</h2>
            <p className="s2e4-sub" data-testid="s2e4-import-count">
              <b>{sum?.toCreate ?? 0} bài</b> vào Nháp
            </p>
            {sum && sum.toUpdate > 0 && (
              <p className="s2e4-sub" data-testid="s2e4-import-update-count">
                <b>{sum.toUpdate} bài</b> cập nhật nháp đã có
              </p>
            )}
            <p className="s2e4-sub" data-testid="s2e4-import-dup-count">
              <b>{sum?.duplicates ?? 0} bài</b> đã có trong kho
            </p>
            <p className="s2e4-sub muted">
              <Icon name="shield" inline />
              Mỗi bài thành một nháp riêng, mang nhãn "AI soạn · Cần xem lại". Chưa bài nào vào Kho cho đến khi cô duyệt. Hệ thống chỉ so với Kho chung và đề
              của chính cô.
            </p>
            {done && (
              <p className="s2e4-sub s2e4-done" role="status" data-testid="s2e4-import-done">
                <Icon name="check" inline />
                {done}
              </p>
            )}
            <button type="button" className="btn" disabled={busy || selected.length === 0} onClick={save} data-testid="s2e4-import-save">
              <Icon name="next" />
              Tạo nháp
            </button>
          </aside>
        </div>
      )}
    </div>
  );
}

function ItemRow({ it, checked, disabled, onToggle }: { it: ImportItem; checked: boolean; disabled: boolean; onToggle: () => void }) {
  const link = refLink(it.refKind, it.refId);
  const tone = it.state === 'exists' || it.state === 'similar' || it.state === 'too_long' ? ' yel' : it.state === 'created' ? ' grn' : '';
  return (
    <li className="s2e4-imp" data-testid={`s2e4-item-${it.index}`} data-state={it.state}>
      <input
        type="checkbox"
        id={`s2e4-item-check-${it.index}`}
        checked={checked}
        disabled={disabled || !it.selectable || Boolean(it.draftId)}
        onChange={onToggle}
        aria-label={`Chọn ${it.title}`}
        data-testid={`s2e4-item-check-${it.index}`}
      />
      <label className="s2e4-imp-body" htmlFor={`s2e4-item-check-${it.index}`}>
        <b>{it.title}.</b> <span data-math><MathText text={it.text} /></span>
      </label>
      <div className="s2e4-row">
        {it.note && (
          <span className={`chip${tone}`} data-testid={`s2e4-item-note-${it.index}`}>
            <MathText text={it.note} />
          </span>
        )}
        {it.draftId && (
          <>
            <span className="chip" data-testid={`s2e4-item-human-${it.index}`}>
              {it.humanLabel}
            </span>
            <span className="chip yel" data-testid={`s2e4-item-machine-${it.index}`}>
              {it.machineLabel}
            </span>
          </>
        )}
        {link && (
          <Link className="btn ghost" to={link} data-testid={`s2e4-item-link-${it.index}`}>
            <Icon name="eye" />
            {it.draftId ? 'Mở nháp' : 'Mở đề trong kho'}
          </Link>
        )}
      </div>
    </li>
  );
}
