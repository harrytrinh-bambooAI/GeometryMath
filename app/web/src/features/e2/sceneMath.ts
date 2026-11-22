import type { Scene, ScenePoint } from '../../shared/scene/types.ts';

/** MSG-GEO-19. */
export const MSG_19 = 'Giá trị phải là số lớn hơn 0.';

/**
 * Tham số hình mẫu (BR-GEO-132): số lớn hơn 0, nhận dấu phẩy thập phân ("2,5"). Trả số hoặc null nếu không hợp lệ.
 */
export function parseParam(raw: string): number | null {
  const s = raw.trim().replace(/[−–]/g, '-').replace(',', '.');
  if (!/^-?\d+(\.\d+)?$/.test(s)) return null;
  const v = Number(s);
  return Number.isFinite(v) && v > 0 ? v : null;
}

/** Số hiển thị: tối đa 2 chữ số thập phân, dấu phẩy, bỏ số 0 thừa (5 → "5", 2.5 → "2,5"). */
export function fmtNum(x: number): string {
  const r = Math.round(x * 100) / 100;
  return String(Object.is(r, -0) ? 0 : r).replace('.', ',');
}

/** Độ dài đoạn PQ theo tọa độ cảnh. */
export function lengthOf(scene: Scene, p: string, q: string): number {
  const a = scene.points.find((x) => x.name === p);
  const b = scene.points.find((x) => x.name === q);
  if (!a || !b) return NaN;
  return Math.hypot(a.x - b.x, a.y - b.y, a.z - b.z);
}

/** Đoạn PQ có thuộc đại lượng phải ẩn (hiddenMeasures 'len:P-Q') không. */
export function lengthHidden(scene: Scene, p: string, q: string): boolean {
  return scene.hiddenMeasures.some((h) => h === `len:${p}-${q}` || h === `len:${q}-${p}`);
}

/** Phép chiếu đơn giản (xoay quanh trục z một góc yaw, nghiêng pitch), trả tọa độ màn hình chưa co giãn. */
export function project(p: ScenePoint, yawDeg: number, pitchDeg: number): [number, number, number] {
  const y = (yawDeg * Math.PI) / 180;
  const t = (pitchDeg * Math.PI) / 180;
  const x1 = p.x * Math.cos(y) - p.y * Math.sin(y);
  const y1 = p.x * Math.sin(y) + p.y * Math.cos(y);
  const sx = x1;
  const sy = -(p.z * Math.cos(t) - y1 * Math.sin(t));
  const depth = p.z * Math.sin(t) + y1 * Math.cos(t);
  return [sx, sy, depth];
}
