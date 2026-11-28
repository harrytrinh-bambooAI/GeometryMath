/**
 * US-S5E2-05 · xuất mã đề (T-261128-54): đường dẫn API và nhãn các phần. Thuần (không chạm DOM) để chạy `node --test`.
 */
export type ExportPart = 'de' | 'dap-an' | 'huong-dan-cham' | 'dac-ta';
export type ExportFormat = 'docx' | 'pdf';

export const PARTS: Array<{ id: ExportPart; label: string }> = [
  { id: 'de', label: 'Đề' },
  { id: 'dap-an', label: 'Đáp án' },
  { id: 'huong-dan-cham', label: 'Hướng dẫn chấm' },
  { id: 'dac-ta', label: 'Bảng đặc tả' },
];

/** `/api/exams/{id}/codes/{code}/export?format=…&parts=…`; đủ 4 phần thì gửi `parts=all` (tệp nén). */
export function exportPath(setId: string, code: string, format: ExportFormat, parts: ExportPart[]): string {
  const p = parts.length === PARTS.length ? 'all' : parts.join(',');
  return `/api/exams/${encodeURIComponent(setId)}/codes/${encodeURIComponent(code)}/export?format=${format}&parts=${encodeURIComponent(p)}`;
}

/** Tên tệp từ Content-Disposition (filename* RFC 5987 hoặc filename). */
export function fileNameOf(disposition: string | null, fallback: string): string {
  if (!disposition) return fallback;
  const star = /filename\*=UTF-8''([^;]+)/i.exec(disposition);
  if (star?.[1]) return decodeURIComponent(star[1]);
  const plain = /filename="?([^";]+)"?/i.exec(disposition);
  return plain?.[1] ?? fallback;
}
