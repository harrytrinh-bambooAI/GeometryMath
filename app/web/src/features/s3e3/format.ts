import type { IconName } from '../../shared/ui/icons.ts';
import type { CellStatus, Insight, RawEntry } from './types.ts';

/** S3-E3 — chữ và quy tắc trình bày thuần của Kết quả lớp (kiểm bằng format.test.ts). */

export const STATUS_LABEL: Record<CellStatus, string> = {
  correct: 'Đúng',
  review: 'Xem lại',
  doing: 'Đang làm',
  todo: 'Chưa làm',
};

/** Mỗi trạng thái có chữ VÀ biểu tượng, không dựa riêng vào màu (BR-S3E3-05). */
export const STATUS_ICON: Record<CellStatus, IconName> = {
  correct: 'check',
  review: 'eye',
  doing: 'pencil',
  todo: 'clock',
};

export const MSG = {
  none: 'Chưa em nào làm bài của bộ này.',
  noActive: 'Lớp chưa có học sinh Đang học. Duyệt các em đang chờ để bắt đầu.',
  small: (min: number) => `Lớp có ít hơn ${min} học sinh, chưa gộp được. Dưới đây là dữ liệu thô.`,
  below: (m: number, min: number) => `Mới có ${m} trên ${min} bài làm cho đề này, chưa đủ để gộp. Dưới đây là dữ liệu thô.`,
  failed: 'Chưa gộp được. Thử lại.',
  notOpened: 'Em chưa mở bài này.',
  loadFailed: 'Chưa tải được kết quả lớp.',
  subtitle: 'Xếp theo tên. Không có cột tổng, không xếp hạng.',
  note: (min: number) => `Chỉ gộp khi đủ ${min} bài làm cho một đề. Mỗi nhận định bấm vào được bài làm căn cứ.`,
} as const;

export const FOLD_AT = 300;
export const CARDS_SHOWN = 5;

/** Câu hỏi dài gập sau 300 ký tự (PO-66 QA-S3E3-04); bấm "Xem thêm" hiện đủ. */
export function fold(text: string, open: boolean): { shown: string; folded: boolean } {
  const chars = Array.from(text);
  if (open || chars.length <= FOLD_AT) return { shown: text, folded: false };
  return { shown: chars.slice(0, FOLD_AT).join(''), folded: true };
}

export const columnLabel = (c: { position: number; title: string }) => `Đề ${c.position} · ${c.title}`;

/** Tên truy cập của một ô: tên học sinh, tên đề và trạng thái (US-S3E3-01-SC7). */
export const cellLabel = (name: string, c: { position: number; title: string }, s: CellStatus) =>
  `${name}, Đề ${c.position} ${c.title}: ${STATUS_LABEL[s]}`;

export const stepLabel = (step: number) => (step <= 0 ? 'Bước đọc đề' : `Bước ${step}`);

/** Thẻ hiện ngay (tối đa 5, n lớn trước — máy chủ đã sắp) và phần gập "Còn k nhận định khác" (BR-S3E3-32). */
export function splitCards(insights: Insight[]): { shown: Insight[]; rest: Insight[] } {
  return { shown: insights.slice(0, CARDS_SHOWN), rest: insights.slice(CARDS_SHOWN) };
}

/** Dữ liệu thô gom theo bước, giữ thứ tự thời gian trong từng bước (BR-S3E3-34). */
export function groupByStep(raw: RawEntry[]): Array<{ step: number; entries: RawEntry[] }> {
  const map = new Map<number, RawEntry[]>();
  for (const r of raw) {
    const list = map.get(r.step) ?? [];
    list.push(r);
    map.set(r.step, list);
  }
  return [...map.entries()].sort((a, b) => a[0] - b[0]).map(([step, entries]) => ({ step, entries }));
}
