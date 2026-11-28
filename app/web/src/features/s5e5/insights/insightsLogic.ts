/** Logic thuần của thẻ "Cả lớp hay sai ở đâu" / "Nên nhắc lại trên lớp" (T-261128-64). */

export const LABEL_NAME: Record<string, string> = {
  'sai-dinh-li': 'Sai định lí',
  'sai-tinh-toan': 'Sai tính toán',
  'sai-hinh-dung': 'Sai hình dung',
  'nham-dieu-kien': 'Nhầm điều kiện',
};

export const labelName = (label: string | null | undefined): string => (label ? (LABEL_NAME[label] ?? label) : '');

/** "4/6 bài" — số bài, không phần trăm, không xếp hạng (BR-S5E5-06, PO-04). */
export const lostText = (lost: number, works: number): string => `${lost}/${works} bài`;

/** Điểm kiểu Việt: 2,5. */
export const points = (n: number): string => (Number.isInteger(n) ? String(n) : String(n).replace('.', ','));

/** Đổi lựa chọn (tick / bỏ tick) một câu gợi ý. */
export function toggle(selected: string[], id: string): string[] {
  return selected.includes(id) ? selected.filter((x) => x !== id) : [...selected, id];
}

/** Tên bộ luyện nháp từ bài có chấm. */
export const practiceTitle = (code: string, title: string): string => `Nhắc lại ${code} · ${title}`.slice(0, 120);
