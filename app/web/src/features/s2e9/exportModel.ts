/**
 * S2-E9 (US-S2E9-02) — mô hình bản xuất và bộ ghi PDF tối giản. Tệp THUẦN (không chạm DOM) để chạy bằng `node --test`.
 *
 * Bản xuất dựng hoàn toàn ở trình duyệt, không thêm thư viện, không gọi CDN: mỗi trang vẽ lên canvas (chữ đề, hình,
 * lời giải khi được phép) rồi PNG tải thẳng, PDF = các trang JPEG đóng vào một tệp PDF 1.4 do `buildPdf` ghi.
 */

/** Cảnh hình phẳng (scene2d-v1) hoặc cảnh 3D v1, đúng như máy chủ trả. */
export interface ExportScenePlane {
  points: Array<{ id: string; label: string; x: number; y: number }>;
  segments: string[][];
  circles: Array<{ center: string; r: number }>;
}
export interface ExportSceneSpace {
  points: Array<{ name: string; x: number; y: number; z: number }>;
  edges: string[][];
  axes?: { up?: string } | null;
}

export interface ExportItem {
  id: string;
  title: string;
  statement: string;
  kind: 'plane' | 'space';
  sceneKind: 'plane' | 'space' | 'none';
  scene: ExportScenePlane | ExportSceneSpace | null;
  machineLabel: string;
  humanLabel: string;
  lockedAssigned: boolean;
  solution: { steps: string[]; machineLabel: string; humanLabel: string } | null;
}

export interface ExportView {
  withSolution: boolean;
  confirmed: boolean;
  items: ExportItem[];
}

export const EXPORT_MIN = 1;
export const EXPORT_MAX = 20;
export const MSG_EXPORT_MIN = 'Chọn ít nhất 1 đề';
export const MSG_EXPORT_MAX = 'Tối đa 20 đề mỗi tệp';
export const MSG_NEED_APPROVAL = 'Cần duyệt lời giải trước';
export const MSG_CONFIRM_LOCKED = 'File xuất có lời giải, lớp có thể thấy nếu giáo viên chia sẻ. Vẫn xuất?';

/** Kiểm số đề trước khi gọi máy chủ (BR-S2E9-07); null = hợp lệ. */
export function checkSelection(count: number): string | null {
  if (count < EXPORT_MIN) return MSG_EXPORT_MIN;
  if (count > EXPORT_MAX) return MSG_EXPORT_MAX;
  return null;
}

/** Một khối trên trang: dòng tiêu đề, nhãn, chữ đề, hình, lời giải. */
export type ExportBlock =
  | { type: 'heading'; text: string }
  | { type: 'labels'; machine: string; human: string }
  | { type: 'statement'; text: string }
  | { type: 'figure'; item: ExportItem }
  | { type: 'solutionTitle'; text: string }
  | { type: 'step'; no: number; text: string };

/**
 * Các khối của MỘT đề theo thứ tự in. Lời giải và hai nhãn (trục máy, trục người — PO-02) CHỈ có khi máy chủ trả
 * `solution` (giáo viên đã tick "Kèm lời giải" và lời giải đã duyệt); nhãn in ở đầu trang (BR-S2E9-07).
 */
export function itemBlocks(item: ExportItem, ordinal: number): ExportBlock[] {
  const blocks: ExportBlock[] = [];
  if (item.solution) blocks.push({ type: 'labels', machine: item.solution.machineLabel, human: item.solution.humanLabel });
  blocks.push({ type: 'heading', text: `Đề ${ordinal}. ${item.title}` });
  blocks.push({ type: 'statement', text: item.statement });
  if (item.sceneKind !== 'none' && item.scene) blocks.push({ type: 'figure', item });
  if (item.solution) {
    blocks.push({ type: 'solutionTitle', text: 'Lời giải' });
    item.solution.steps.forEach((text, i) => blocks.push({ type: 'step', no: i + 1, text }));
  }
  return blocks;
}

/** Tóm tắt bản xuất (ghi vào thuộc tính data-* của màn để kiểm thử và đọc màn hình): thứ tự đề, có lời giải không. */
export function exportSummary(view: ExportView): { order: string[]; withSolution: boolean; figures: number; steps: number } {
  return {
    order: view.items.map((i) => i.id),
    withSolution: view.items.some((i) => i.solution != null),
    figures: view.items.filter((i) => i.sceneKind !== 'none' && i.scene).length,
    steps: view.items.reduce((n, i) => n + (i.solution?.steps.length ?? 0), 0),
  };
}

/** Tên tệp an toàn từ tên đề: bỏ dấu, chỉ chữ số và gạch nối. */
export function fileName(title: string, ext: 'png' | 'pdf'): string {
  const base = title
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .replace(/[đĐ]/g, 'd')
    .replace(/[^a-zA-Z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
    .toLowerCase()
    .slice(0, 60);
  return `${base || 'de'}.${ext}`;
}

/** Ngắt một đoạn chữ thành các dòng vừa bề rộng, theo hàm đo do nơi gọi cấp (canvas measureText). */
export function wrapText(text: string, maxWidth: number, measure: (s: string) => number): string[] {
  const lines: string[] = [];
  for (const para of text.split(/\r?\n/)) {
    let line = '';
    for (const word of para.split(/\s+/).filter(Boolean)) {
      const next = line ? `${line} ${word}` : word;
      if (line && measure(next) > maxWidth) {
        lines.push(line);
        line = word;
      } else {
        line = next;
      }
    }
    lines.push(line);
  }
  return lines;
}

/** Chiếu cảnh về mặt phẳng trang: phẳng giữ nguyên (x, y); không gian chiếu xiên (trục sâu lệch 45 %, 30 %). */
export function projectScene(item: ExportItem): { points: Map<string, [number, number]>; segments: string[][]; circles: Array<{ center: string; r: number }> } {
  const points = new Map<string, [number, number]>();
  if (item.sceneKind === 'plane' && item.scene) {
    const s = item.scene as ExportScenePlane;
    for (const p of s.points) points.set(p.id, [p.x, p.y]);
    return { points, segments: s.segments, circles: s.circles ?? [] };
  }
  if (item.sceneKind === 'space' && item.scene) {
    const s = item.scene as ExportSceneSpace;
    const upY = (s.axes?.up ?? 'z').toLowerCase() === 'y';
    for (const p of s.points) {
      const depth = upY ? p.z : p.y;
      const up = upY ? p.y : p.z;
      points.set(p.name, [p.x - 0.45 * depth, up - 0.3 * depth]);
    }
    return { points, segments: s.edges, circles: [] };
  }
  return { points, segments: [], circles: [] };
}

/** Lớp màu của chip nhãn (trục máy, trục người). */
export function labelTone(label: string): string {
  if (label === 'Đã kiểm chứng' || label === 'Giáo viên đã duyệt') return 'grn';
  if (label === 'Cần xem lại') return 'yel';
  return '';
}

/** Số biến thể mỗi lần tạo: 1…10 (BR-S2E9-02). */
export const VARIANT_MIN = 1;
export const VARIANT_MAX = 10;
export const MSG_VARIANT_COUNT = 'Chọn từ 1 đến 10';
export const variantCountError = (count: number): string | null =>
  Number.isInteger(count) && count >= VARIANT_MIN && count <= VARIANT_MAX ? null : MSG_VARIANT_COUNT;

/** Hạn của liên kết chỉ xem: chỉ 1, 7 hoặc 30 ngày, mặc định 7 (BR-S2E9-09); không có "không giới hạn". */
export const LINK_DAYS = [1, 7, 30] as const;
export const LINK_DEFAULT_DAYS = 7;
export const LINK_MAX_ACTIVE = 5;

// ------------------------------------------------------------------ PDF tối giản (mỗi trang một ảnh JPEG)

export interface PdfPage {
  /** dữ liệu JPEG (baseline, RGB) của cả trang */
  jpeg: Uint8Array;
  /** kích thước ảnh theo điểm ảnh */
  width: number;
  height: number;
}

/** Khổ A4 theo điểm PDF (1/72 inch). */
export const A4 = { width: 595, height: 842 } as const;

/**
 * Ghi tệp PDF 1.4: mỗi trang A4 chứa một ảnh JPEG phủ kín (bộ lọc DCTDecode, không nén lại). Bảng xref ghi đúng vị trí
 * byte nên mọi trình đọc PDF mở được. Tiêu đề tài liệu ghi ở /Info (chuỗi hex UTF-16BE để giữ tiếng Việt).
 */
export function buildPdf(pages: PdfPage[], title: string): Uint8Array {
  if (pages.length === 0) throw new Error(MSG_EXPORT_MIN);
  const enc = new TextEncoder();
  const chunks: Uint8Array[] = [];
  const offsets: number[] = [];
  let size = 0;
  const push = (data: Uint8Array | string) => {
    const bytes = typeof data === 'string' ? enc.encode(data) : data;
    chunks.push(bytes);
    size += bytes.length;
  };
  const obj = (n: number, body: string) => {
    offsets[n] = size;
    push(`${n} 0 obj\n${body}\nendobj\n`);
  };
  push('%PDF-1.4\n%âãÏÓ\n');
  const pageIds = pages.map((_, i) => 4 + i * 3);
  obj(1, '<< /Type /Catalog /Pages 2 0 R >>');
  obj(2, `<< /Type /Pages /Count ${pages.length} /Kids [${pageIds.map((id) => `${id} 0 R`).join(' ')}] >>`);
  let hex = 'FEFF';
  for (const ch of title) {
    const cp = ch.codePointAt(0)!;
    const units = cp > 0xffff ? [0xd800 + ((cp - 0x10000) >> 10), 0xdc00 + ((cp - 0x10000) & 0x3ff)] : [cp];
    for (const u of units) hex += u.toString(16).toUpperCase().padStart(4, '0');
  }
  obj(3, `<< /Title <${hex}> /Producer (GEO S2-E9) >>`);
  pages.forEach((p, i) => {
    const page = 4 + i * 3;
    const image = page + 1;
    const content = page + 2;
    obj(
      page,
      `<< /Type /Page /Parent 2 0 R /MediaBox [0 0 ${A4.width} ${A4.height}] ` +
        `/Resources << /XObject << /Im${i} ${image} 0 R >> >> /Contents ${content} 0 R >>`,
    );
    offsets[image] = size;
    push(
      `${image} 0 obj\n<< /Type /XObject /Subtype /Image /Width ${p.width} /Height ${p.height} ` +
        `/ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode /Length ${p.jpeg.length} >>\nstream\n`,
    );
    push(p.jpeg);
    push('\nendstream\nendobj\n');
    const draw = `q ${A4.width} 0 0 ${A4.height} 0 0 cm /Im${i} Do Q`;
    obj(content, `<< /Length ${draw.length} >>\nstream\n${draw}\nendstream`);
  });
  const count = 4 + pages.length * 3;
  const xref = size;
  let table = `xref\n0 ${count}\n0000000000 65535 f \n`;
  for (let n = 1; n < count; n++) table += `${String(offsets[n]).padStart(10, '0')} 00000 n \n`;
  push(table);
  push(`trailer\n<< /Size ${count} /Root 1 0 R /Info 3 0 R >>\nstartxref\n${xref}\n%%EOF\n`);
  const out = new Uint8Array(size);
  let at = 0;
  for (const c of chunks) {
    out.set(c, at);
    at += c.length;
  }
  return out;
}
