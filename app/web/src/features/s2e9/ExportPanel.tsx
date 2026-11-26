import { useState } from 'react';
import { ApiError } from '../../shared/api/errors.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { s2e9Api, type DocOption } from './api.ts';
import { checkSelection, exportSummary, fileName, MSG_CONFIRM_LOCKED, MSG_NEED_APPROVAL } from './exportModel.ts';
import { download, renderPdf, renderPng } from './render.ts';

type Format = 'png' | 'pdf';

interface Done {
  format: Format;
  name: string;
  count: number;
  pages: number;
  order: string[];
  withSolution: boolean;
  figures: number;
  steps: number;
}

/**
 * Bảng xuất đề (US-S2E9-02): "Kèm lời giải" mặc định TẮT và chỉ bật được khi mọi đề đã "Giáo viên đã duyệt"
 * (BR-S2E9-07); đề đang giao mà lời giải còn khóa thì hỏi lại trước khi xuất kèm lời giải (BR-S2E9-08).
 * `docs` theo đúng thứ tự giáo viên đã chọn; `png` = cho phép "Xuất ảnh" (chỉ một đề).
 */
export function ExportPanel({ docs, png = false, title }: { docs: DocOption[]; png?: boolean; title: string }) {
  const [withSolution, setWithSolution] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [confirm, setConfirm] = useState<Format | null>(null);
  const [done, setDone] = useState<Done | null>(null);

  const canAttach = docs.length > 0 && docs.every((d) => d.canAttachSolution);
  const attach = withSolution && canAttach;
  const locked = docs.some((d) => d.lockedAssigned);

  const run = async (format: Format, confirmLocked: boolean) => {
    setError(null);
    setDone(null);
    const bad = checkSelection(docs.length);
    if (bad) {
      setError(bad);
      return;
    }
    if (attach && locked && !confirmLocked) {
      setConfirm(format); // hỏi lại trước, chưa gọi máy chủ, chưa có tệp
      return;
    }
    setConfirm(null);
    setBusy(true);
    try {
      const view = await s2e9Api.exportDocs(
        docs.map((d) => d.id),
        attach,
        confirmLocked,
      );
      const name = fileName(docs.length === 1 ? docs[0]!.title : title, format);
      let pages = 1;
      if (format === 'png') {
        download(await renderPng(view), name);
      } else {
        const pdf = await renderPdf(view, title);
        pages = pdf.pages;
        download(pdf.blob, name);
      }
      setDone({ format, name, count: view.items.length, pages, ...exportSummary(view) });
    } catch (e) {
      if (e instanceof ApiError && e.code === 'CONFLICT' && e.message === MSG_CONFIRM_LOCKED) setConfirm(format);
      else setError(e instanceof ApiError ? e.message : 'Chưa xuất được. Thầy cô thử lại nhé.');
    } finally {
      setBusy(false);
    }
  };

  return (
    <section className="card s2e9-panel" data-testid="s2e9-export" data-count={docs.length}>
      <h2 className="s2e9-h">Xuất đề</h2>
      <label className="s2e9-check">
        <input
          type="checkbox"
          checked={attach}
          disabled={!canAttach || busy}
          onChange={(e) => setWithSolution(e.target.checked)}
          data-testid="s2e9-with-solution"
        />
        Kèm lời giải
      </label>
      {docs.length > 0 && !canAttach ? (
        <p className="s2e9-help" data-testid="s2e9-solution-hint">
          <Icon name="lock" inline />
          {MSG_NEED_APPROVAL}
        </p>
      ) : null}
      <div className="s2e9-actions">
        {png ? (
          <button type="button" className="btn ghost" onClick={() => void run('png', false)} disabled={busy} data-testid="s2e9-export-png">
            <Icon name="camera" />
            Xuất ảnh
          </button>
        ) : null}
        <button type="button" className="btn" onClick={() => void run('pdf', false)} disabled={busy} data-testid="s2e9-export-pdf">
          <Icon name="file" />
          Xuất PDF
        </button>
      </div>
      {error ? (
        <p className="alert" role="alert" data-testid="s2e9-export-error">
          {error}
        </p>
      ) : null}
      {confirm ? (
        <div className="s2e9-confirm" role="dialog" aria-label="Xác nhận xuất kèm lời giải" data-testid="s2e9-confirm">
          <p data-testid="s2e9-confirm-text">{MSG_CONFIRM_LOCKED}</p>
          <div className="s2e9-actions">
            <button type="button" className="btn" onClick={() => void run(confirm, true)} disabled={busy} data-testid="s2e9-confirm-yes">
              <Icon name="check" />
              Vẫn xuất
            </button>
            <button type="button" className="btn ghost" onClick={() => setConfirm(null)} data-testid="s2e9-confirm-no">
              Hủy
            </button>
          </div>
        </div>
      ) : null}
      {done ? (
        <p
          className="s2e9-done"
          role="status"
          data-testid="s2e9-export-done"
          data-format={done.format}
          data-file={done.name}
          data-count={done.count}
          data-pages={done.pages}
          data-order={done.order.join(',')}
          data-with-solution={done.withSolution}
          data-figures={done.figures}
          data-steps={done.steps}
        >
          <Icon name="check" inline />
          Đã xuất {done.count} đề ra tệp {done.format === 'png' ? 'ảnh PNG' : `PDF (${done.pages} trang)`}
          {done.withSolution ? ', có lời giải' : ', không có lời giải'}.
        </p>
      ) : null}
    </section>
  );
}
