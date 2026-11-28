/** Tính tổng và kiểm rubric phía màn (BR-S5E4-01); máy chủ kiểm lại. */

export const MSG_30 = 'Tổng điểm các tiêu chí phải bằng thang điểm của bài.';

/** Bội 0,25 và > 0. */
export const quarter = (v: number) => Number.isFinite(v) && v > 0 && Math.abs(v * 4 - Math.round(v * 4)) < 1e-9;

export const total = (points: number[]) => Math.round(points.reduce((s, p) => s + (Number.isFinite(p) ? p : 0), 0) * 100) / 100;

/** "11 / 10" (dấu phẩy thập phân kiểu Việt Nam). */
export const fmt = (v: number) => (Number.isInteger(v) ? String(v) : String(v).replace('.', ','));

export const totalLabel = (points: number[], scale: number) => `${fmt(total(points))} / ${fmt(scale)}`;

/** Lưu được: 1–10 tiêu chí, mỗi điểm bội 0,25, tổng đúng thang. */
export function canSave(points: number[], scale: number): boolean {
  return points.length >= 1 && points.length <= 10 && points.every(quarter) && total(points) === scale;
}
