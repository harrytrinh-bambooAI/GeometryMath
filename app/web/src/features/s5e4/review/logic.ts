/** Logic thuần của màn chấm tay C5-05 (US-S5E4-06; T-261128-60). */

/** Đọc số kiểu Việt Nam ("3,5") hoặc kiểu máy ("3.5"); rỗng/không hợp lệ → null. */
export function parsePoints(raw: string): number | null {
  const s = raw.trim().replace(',', '.');
  if (s === '' || !/^\d+(\.\d+)?$/.test(s)) return null;
  return Number(s);
}

/** BR-S5E4-17: 0 ≤ điểm ≤ tối đa, bước 0,25. */
export function validPoints(p: number | null, max: number): boolean {
  if (p === null || Number.isNaN(p) || p < 0 || p > max) return false;
  return Math.abs(p * 4 - Math.round(p * 4)) < 1e-9;
}

/** MSG-S5-37. */
export function msg37(name: string, max: number): string {
  return `Điểm tiêu chí ${name} phải từ 0 đến ${vn(max)}, bước 0,25.`;
}

export function vn(x: number): string {
  return String(Math.round(x * 100) / 100).replace('.', ',');
}

/** Tổng các điểm đã nhập hợp lệ (tiêu chí chưa nhập tính 0). */
export function total(points: Array<number | null>): number {
  return Math.round(points.reduce<number>((a, p) => a + (p ?? 0), 0) * 100) / 100;
}

/** Phím tắt QS5-23: J/K bài kế/trước, A hoặc Enter Duyệt, E sửa (ô đầu), X bỏ qua sang bài kế. */
export type KeyAction = 'next' | 'prev' | 'approve' | 'edit' | 'skip' | null;

export function keyAction(key: string, inInput: boolean): KeyAction {
  if (inInput) return key === 'Enter' ? 'approve' : null;
  switch (key.toLowerCase()) {
    case 'j':
      return 'next';
    case 'k':
      return 'prev';
    case 'a':
    case 'enter':
      return 'approve';
    case 'e':
      return 'edit';
    case 'x':
      return 'skip';
    default:
      return null;
  }
}
