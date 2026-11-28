/** Logic thuần của màn chấm có đề xuất AI (US-S5E6-04, 05; T-261128-62). */

export type Act = 'accept' | 'edit' | 'drop';

/** Trạng thái một tiêu chí: hành động (chưa xử lý = null) và ô điểm (chuỗi người dùng gõ). */
export interface CritState {
  act: Act | null;
  value: string;
}

/** Đọc số kiểu Việt Nam ("2,5") hoặc kiểu máy ("2.5"); rỗng/không hợp lệ → null. */
export function parsePoints(raw: string): number | null {
  const s = raw.trim().replace(',', '.');
  if (s === '' || !/^\d+(\.\d+)?$/.test(s)) return null;
  return Number(s);
}

/** 0 ≤ điểm ≤ tối đa, bước 0,25 (BR-S5E4-17). */
export function validPoints(p: number | null, max: number): boolean {
  if (p === null || Number.isNaN(p) || p < 0 || p > max) return false;
  return Math.abs(p * 4 - Math.round(p * 4)) < 1e-9;
}

export function vn(x: number): string {
  return String(Math.round(x * 100) / 100).replace('.', ',');
}

/** Điểm hiện có của tiêu chí (null = chưa có điểm hợp lệ). */
export function pointsOf(s: CritState | undefined, max: number): number | null {
  if (!s) return null;
  const p = parsePoints(s.value);
  return validPoints(p, max) ? p : null;
}

/** BR-S5E6-10: Duyệt chỉ bật khi mọi tiêu chí có điểm hợp lệ. */
export function canApprove(states: Record<number, CritState>, criteria: Array<{ ord: number; maxPoints: number }>): boolean {
  return criteria.length > 0 && criteria.every((c) => pointsOf(states[c.ord], c.maxPoints) !== null);
}

export function total(states: Record<number, CritState>, criteria: Array<{ ord: number; maxPoints: number }>): number {
  return Math.round(criteria.reduce((a, c) => a + (pointsOf(states[c.ord], c.maxPoints) ?? 0), 0) * 100) / 100;
}

/** MSG-S5-49. */
export const msg49 = (name: string) => `Đã nhận đề xuất của tiêu chí ${name}.`;

/** Phím tắt QS5-23 (BR-S5E6-11): J/K tiêu chí kế/trước, A nhận, E sửa, X bỏ, Enter duyệt, Shift+A nhận cả bài. */
export type KeyAction = 'next' | 'prev' | 'accept' | 'edit' | 'drop' | 'approve' | 'acceptAll' | null;

export function keyAction(key: string, shift: boolean, inInput: boolean): KeyAction {
  if (inInput) return key === 'Enter' ? 'approve' : null;
  if (key === 'Enter') return 'approve';
  switch (key.toLowerCase()) {
    case 'j':
      return 'next';
    case 'k':
      return 'prev';
    case 'a':
      return shift ? 'acceptAll' : 'accept';
    case 'e':
      return 'edit';
    case 'x':
      return 'drop';
    default:
      return null;
  }
}

/** Vùng khoanh {trang,x,y,w,h} (điểm ảnh của ảnh gốc) → phần trăm để vẽ trên ảnh co giãn. */
export function regionBox(
  r: { x: number; y: number; w: number; h: number },
  natural: { width: number; height: number },
): { left: string; top: string; width: string; height: string } | null {
  if (!natural.width || !natural.height) return null;
  const pct = (v: number, d: number) => `${Math.max(0, Math.min(100, (v / d) * 100)).toFixed(3)}%`;
  return { left: pct(r.x, natural.width), top: pct(r.y, natural.height), width: pct(r.w, natural.width), height: pct(r.h, natural.height) };
}
