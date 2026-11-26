import { buildPdf, itemBlocks, projectScene, wrapText, type ExportItem, type ExportView, type PdfPage } from './exportModel.ts';

/**
 * S2-E9 (US-S2E9-02) — vẽ bản xuất lên canvas ở trình duyệt và tải tệp về (PNG một đề, PDF 1…20 đề).
 * Trang A4 150 dpi (1240 × 1754 điểm ảnh). Màu giấy, mực và font lấy từ token của theme đang chọn
 * (--panel, --text, --font, --font-math), không mã màu cứng. Mỗi đề bắt đầu một trang mới; đề dài tự sang trang.
 */

const PAGE_W = 1240;
const PAGE_H = 1754;
const MARGIN = 96;
const BODY = 30;
const LINE = 46;
const FIGURE_H = 520;

interface Theme {
  paper: string;
  ink: string;
  font: string;
  math: string;
}

function theme(): Theme {
  const css = getComputedStyle(document.documentElement);
  const pick = (name: string, fallback: string) => css.getPropertyValue(name).trim() || fallback;
  const body = getComputedStyle(document.body);
  return {
    paper: pick('--panel', 'Canvas'),
    ink: pick('--text', 'CanvasText'),
    font: pick('--font', body.fontFamily),
    math: pick('--font-math', pick('--font', body.fontFamily)),
  };
}

class Pager {
  readonly pages: HTMLCanvasElement[] = [];
  ctx!: CanvasRenderingContext2D;
  y = MARGIN;
  private readonly t: Theme;

  constructor(t: Theme) {
    this.t = t;
    this.newPage();
  }

  newPage() {
    const canvas = document.createElement('canvas');
    canvas.width = PAGE_W;
    canvas.height = PAGE_H;
    const ctx = canvas.getContext('2d');
    if (!ctx) throw new Error('Trình duyệt không vẽ được trang xuất.');
    ctx.fillStyle = this.t.paper;
    ctx.fillRect(0, 0, PAGE_W, PAGE_H);
    ctx.fillStyle = this.t.ink;
    ctx.strokeStyle = this.t.ink;
    ctx.textBaseline = 'top';
    this.pages.push(canvas);
    this.ctx = ctx;
    this.y = MARGIN;
  }

  /** Chừa chỗ cao h; không đủ thì sang trang. */
  need(h: number) {
    if (this.y + h > PAGE_H - MARGIN) this.newPage();
  }

  text(text: string, size: number, opts: { bold?: boolean; math?: boolean; indent?: number } = {}) {
    const family = opts.math ? this.t.math : this.t.font;
    const font = `${opts.bold ? '700 ' : ''}${size}px ${family}`;
    this.ctx.font = font;
    const x = MARGIN + (opts.indent ?? 0);
    const lines = wrapText(text, PAGE_W - MARGIN - x, (s) => this.ctx.measureText(s).width);
    const step = Math.round(size * 1.55);
    for (const line of lines) {
      this.need(step);
      this.ctx.font = font; // trang mới: ngữ cảnh mới
      this.ctx.fillText(line, x, this.y);
      this.y += step;
    }
  }

  gap(h: number) {
    this.y += h;
  }
}

function drawFigure(p: Pager, item: ExportItem) {
  const { points, segments, circles } = projectScene(item);
  if (points.size === 0) return;
  p.need(FIGURE_H + 24);
  const ctx = p.ctx;
  let minX = Infinity;
  let minY = Infinity;
  let maxX = -Infinity;
  let maxY = -Infinity;
  const grow = (x: number, y: number) => {
    minX = Math.min(minX, x);
    maxX = Math.max(maxX, x);
    minY = Math.min(minY, y);
    maxY = Math.max(maxY, y);
  };
  for (const [x, y] of points.values()) grow(x, y);
  for (const c of circles) {
    const o = points.get(c.center);
    if (o) {
      grow(o[0] - c.r, o[1] - c.r);
      grow(o[0] + c.r, o[1] + c.r);
    }
  }
  const pad = 56;
  const boxW = PAGE_W - 2 * MARGIN;
  const k = Math.min((boxW - 2 * pad) / Math.max(1e-9, maxX - minX), (FIGURE_H - 2 * pad) / Math.max(1e-9, maxY - minY));
  const offX = MARGIN + (boxW - (maxX - minX) * k) / 2;
  const top = p.y;
  const at = (id: string): [number, number] | null => {
    const q = points.get(id);
    return q ? [offX + (q[0] - minX) * k, top + FIGURE_H - pad - (q[1] - minY) * k] : null;
  };
  ctx.lineWidth = 3;
  ctx.lineJoin = 'round';
  for (const [a, b] of segments) {
    const pa = a ? at(a) : null;
    const pb = b ? at(b) : null;
    if (!pa || !pb) continue;
    ctx.beginPath();
    ctx.moveTo(pa[0], pa[1]);
    ctx.lineTo(pb[0], pb[1]);
    ctx.stroke();
  }
  for (const c of circles) {
    const o = at(c.center);
    if (!o) continue;
    ctx.beginPath();
    ctx.arc(o[0], o[1], c.r * k, 0, Math.PI * 2);
    ctx.stroke();
  }
  ctx.font = `700 ${BODY}px ${getComputedStyle(document.body).fontFamily}`;
  for (const id of points.keys()) {
    const q = at(id);
    if (q) ctx.fillText(id, q[0] + 10, q[1] - BODY - 6);
  }
  p.y = top + FIGURE_H + 24;
}

/** Vẽ toàn bộ bản xuất thành các trang canvas (mỗi đề bắt đầu một trang mới). */
export function renderPages(view: ExportView): HTMLCanvasElement[] {
  const pager = new Pager(theme());
  view.items.forEach((item, i) => {
    if (i > 0) pager.newPage();
    for (const b of itemBlocks(item, i + 1)) {
      switch (b.type) {
        case 'labels':
          pager.text(`${b.machine} · ${b.human}`, 26, { bold: true });
          pager.gap(12);
          break;
        case 'heading':
          pager.text(b.text, 38, { bold: true });
          pager.gap(14);
          break;
        case 'statement':
          pager.text(b.text, BODY, { math: true });
          pager.gap(18);
          break;
        case 'figure':
          drawFigure(pager, b.item);
          break;
        case 'solutionTitle':
          pager.gap(10);
          pager.text(b.text, 32, { bold: true });
          pager.gap(6);
          break;
        case 'step':
          pager.text(`Bước ${b.no}. ${b.text}`, BODY, { math: true, indent: 12 });
          pager.gap(LINE - BODY - 8);
          break;
      }
    }
  });
  return pager.pages;
}

const toBlob = (canvas: HTMLCanvasElement, type: string, quality?: number) =>
  new Promise<Blob>((resolve, reject) =>
    canvas.toBlob((b) => (b ? resolve(b) : reject(new Error('Không tạo được ảnh xuất.'))), type, quality),
  );

/** Tệp PNG của MỘT đề: các trang của đề ghép dọc thành một ảnh (thường là một trang). */
export async function renderPng(view: ExportView): Promise<Blob> {
  const pages = renderPages(view);
  if (pages.length === 1) return toBlob(pages[0]!, 'image/png');
  const all = document.createElement('canvas');
  all.width = PAGE_W;
  all.height = PAGE_H * pages.length;
  const ctx = all.getContext('2d');
  if (!ctx) throw new Error('Trình duyệt không vẽ được trang xuất.');
  pages.forEach((p, i) => ctx.drawImage(p, 0, i * PAGE_H));
  return toBlob(all, 'image/png');
}

/** Tệp PDF: mỗi trang canvas thành một trang A4. Trả kèm số trang để màn hiện cho giáo viên. */
export async function renderPdf(view: ExportView, title: string): Promise<{ blob: Blob; pages: number }> {
  const canvases = renderPages(view);
  const pages: PdfPage[] = [];
  for (const c of canvases) {
    const jpeg = new Uint8Array(await (await toBlob(c, 'image/jpeg', 0.92)).arrayBuffer());
    pages.push({ jpeg, width: c.width, height: c.height });
  }
  const bytes = buildPdf(pages, title);
  return { blob: new Blob([bytes.buffer as ArrayBuffer], { type: 'application/pdf' }), pages: pages.length };
}

/** Tải một tệp về máy qua thẻ <a download> (không rời trang). */
export function download(blob: Blob, name: string) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = name;
  document.body.appendChild(a);
  a.click();
  a.remove();
  setTimeout(() => URL.revokeObjectURL(url), 10_000);
}
