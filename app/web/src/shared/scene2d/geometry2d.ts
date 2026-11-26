/**
 * Bộ vẽ 2D dùng chung (PLAN_code-s23 A3; hợp đồng app/docs/SCENE-CONTRACT-2D.md "scene2d-v1").
 * Hàm thuần: khung nhìn, đo độ dài/góc trên tọa độ, định dạng số kiểu Việt Nam, so đại lượng bị khóa.
 * Không có đáp số trong cảnh: thiết bị tự đo trên tọa độ; đại lượng đề hỏi bị khóa tới giờ mở lời giải (BR-S2E2-09).
 */

export interface Scene2DPoint {
  id: string;
  label: string;
  x: number;
  y: number;
}

export interface Scene2DConstraint {
  type: string;
  of: string[];
  value: number | null;
}

export interface Scene2D {
  contract: 'scene2d-v1';
  kind: 'plane';
  coordSource: 'engine' | 'ai';
  points: Scene2DPoint[];
  segments: string[][];
  circles: Array<{ center: string; r: number }>;
  constraints: Scene2DConstraint[];
}

/** Đại lượng bị khóa đo (không có giá trị). */
export interface LockedQuantity {
  kind: string;
  of: string[];
}

export type MeasureKind = 'length' | 'angle';

export interface Measure {
  kind: MeasureKind;
  of: string[];
  /** Nhãn hiện: "AB = 5", "A = 90°". */
  text: string;
}

/** Khung nhìn (đơn vị của đề, trục y hướng lên → lật khi vẽ). */
export interface View {
  minX: number;
  minY: number;
  width: number;
  height: number;
}

/** Khung bao các điểm và đường tròn, chừa lề 12 %, tối thiểu 1 đơn vị mỗi chiều. */
export function fitView(scene: Scene2D): View {
  const xs: number[] = [];
  const ys: number[] = [];
  for (const p of scene.points) {
    xs.push(p.x);
    ys.push(p.y);
  }
  for (const c of scene.circles) {
    const o = scene.points.find((p) => p.id === c.center);
    if (!o) continue;
    xs.push(o.x - c.r, o.x + c.r);
    ys.push(o.y - c.r, o.y + c.r);
  }
  if (xs.length === 0) return { minX: -1, minY: -1, width: 2, height: 2 };
  let minX = Math.min(...xs);
  let maxX = Math.max(...xs);
  let minY = Math.min(...ys);
  let maxY = Math.max(...ys);
  const span = Math.max(maxX - minX, maxY - minY, 1);
  const pad = span * 0.12;
  minX -= pad;
  maxX += pad;
  minY -= pad;
  maxY += pad;
  return { minX, minY, width: Math.max(maxX - minX, 1), height: Math.max(maxY - minY, 1) };
}

/** Tọa độ đề → phần trăm trong khung (0…100, y lật). */
export function toPercent(view: View, x: number, y: number): { left: number; top: number } {
  return {
    left: ((x - view.minX) / view.width) * 100,
    top: ((view.minY + view.height - y) / view.height) * 100,
  };
}

function pt(scene: Scene2D, id: string): Scene2DPoint {
  const p = scene.points.find((q) => q.id === id);
  if (!p) throw new Error(`Không có điểm ${id}`);
  return p;
}

export function distance(scene: Scene2D, a: string, b: string): number {
  const p = pt(scene, a);
  const q = pt(scene, b);
  return Math.hypot(p.x - q.x, p.y - q.y);
}

/** Góc tại {@code o} giữa {@code a}, {@code b} (độ). */
export function angle(scene: Scene2D, a: string, o: string, b: string): number {
  const p = pt(scene, a);
  const v = pt(scene, o);
  const q = pt(scene, b);
  const ux = p.x - v.x;
  const uy = p.y - v.y;
  const wx = q.x - v.x;
  const wy = q.y - v.y;
  const cos = (ux * wx + uy * wy) / (Math.hypot(ux, uy) * Math.hypot(wx, wy));
  return (Math.acos(Math.max(-1, Math.min(1, cos))) * 180) / Math.PI;
}

/** Làm tròn hai chữ số thập phân, dấu phẩy, bỏ số 0 thừa: 5 → "5", 6.9282 → "6,93". */
export function formatNumber(v: number): string {
  const s = (Math.round(v * 100) / 100).toFixed(2).replace(/0+$/, '').replace(/\.$/, '');
  return (s === '-0' ? '0' : s).replace('.', ',');
}

/** Đại lượng đo có trùng đại lượng bị khóa không (độ dài: cặp không thứ tự; góc: cùng đỉnh, cùng hai cạnh). */
export function isLocked(locked: LockedQuantity[], kind: MeasureKind, of: string[]): boolean {
  return locked.some((l) => {
    if (l.kind !== kind || l.of.length !== of.length) return false;
    if (kind === 'length') return new Set(l.of).size === 2 && l.of.every((x) => of.includes(x));
    return l.of[1] === of[1] && [l.of[0], l.of[2]].every((x) => x === of[0] || x === of[2]);
  });
}

/** Đo theo công cụ: null khi đại lượng bị khóa (không tính, không hiện). */
export function measure(scene: Scene2D, locked: LockedQuantity[], kind: MeasureKind, of: string[]): Measure | null {
  if (isLocked(locked, kind, of)) return null;
  if (kind === 'length') {
    return { kind, of, text: `${of[0]}${of[1]} = ${formatNumber(distance(scene, of[0]!, of[1]!))}` };
  }
  const name = scene.points.length === 3 ? of[1]! : `${of[0]}${of[1]}${of[2]}`;
  return { kind, of, text: `${name} = ${formatNumber(angle(scene, of[0]!, of[1]!, of[2]!))}°` };
}
