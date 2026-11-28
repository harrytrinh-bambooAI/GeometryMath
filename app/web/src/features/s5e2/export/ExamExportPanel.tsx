import { useState } from 'react';
import { buildUrl } from '../../../shared/api/url.ts';
import { getToken } from '../../../shared/auth/session.ts';
import { Icon } from '../../../shared/ui/Icon.tsx';
import { PARTS, exportPath, fileNameOf, type ExportFormat, type ExportPart } from './exportUrl.ts';

/**
 * Khung "Xuất mã đề" (US-S5E2-05, T-261128-54): chọn mã, phần (đề, đáp án, hướng dẫn chấm, bảng đặc tả) và định dạng;
 * tải bằng thẻ của giáo viên. Bộ đề chưa duyệt vẫn xuất được, tệp mang chân trang "Bản nháp – chưa duyệt" (máy chủ).
 * Gắn vào màn bộ đề: `<ExamExportPanel setId={set.id} codes={set.variants.map((v) => v.code)} />`.
 */
export function ExamExportPanel({ setId, codes }: { setId: string; codes: string[] }) {
  const [code, setCode] = useState(codes[0] ?? '');
  const [parts, setParts] = useState<ExportPart[]>(PARTS.map((p) => p.id));
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  const toggle = (p: ExportPart) => setParts(parts.includes(p) ? parts.filter((x) => x !== p) : [...parts, p]);

  async function download(format: ExportFormat) {
    setBusy(true);
    setMessage(null);
    try {
      const res = await fetch(buildUrl(exportPath(setId, code, format, parts)), {
        headers: { Authorization: `Bearer ${getToken() ?? ''}` },
        cache: 'no-store',
      });
      if (!res.ok) {
        const body = (await res.json().catch(() => null)) as { message?: string } | null;
        setMessage(body?.message ?? 'Chưa xuất được, thầy cô thử lại.');
        return;
      }
      const url = URL.createObjectURL(await res.blob());
      const a = document.createElement('a');
      a.href = url;
      a.download = fileNameOf(res.headers.get('Content-Disposition'), `ma-de-${code}.${format}`);
      document.body.appendChild(a);
      a.click();
      a.remove();
      setTimeout(() => URL.revokeObjectURL(url), 10_000);
    } catch {
      setMessage('Chưa xuất được. Thầy cô kiểm tra kết nối mạng rồi thử lại.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="card" data-testid="s5e2-export">
      <h2>Xuất mã đề</h2>
      <label>
        Mã đề{' '}
        <select value={code} onChange={(e) => setCode(e.target.value)} data-testid="s5e2-export-code">
          {codes.map((c) => (
            <option key={c} value={c}>
              {c}
            </option>
          ))}
        </select>
      </label>
      <fieldset>
        <legend>Phần</legend>
        {PARTS.map((p) => (
          <label key={p.id}>
            <input type="checkbox" checked={parts.includes(p.id)} onChange={() => toggle(p.id)} data-testid={`s5e2-export-part-${p.id}`} /> {p.label}
          </label>
        ))}
      </fieldset>
      <button type="button" className="btn" disabled={busy || parts.length === 0} onClick={() => void download('docx')} data-testid="s5e2-export-docx">
        <Icon name="file" />
        Tải Word
      </button>{' '}
      <button type="button" className="btn" disabled={busy || parts.length === 0} onClick={() => void download('pdf')} data-testid="s5e2-export-pdf">
        <Icon name="print" />
        Tải PDF
      </button>
      {message && (
        <p role="alert" className="small" data-testid="s5e2-export-error">
          {message}
        </p>
      )}
    </section>
  );
}
