import { add, cross, dot, lerp, pointInPolygon3D, polygonPlane, scale, unit, type Plane, type Vec3 } from './geometry.ts';

/**
 * Cạnh khuất (BR-GEO-142): một điểm bị khuất nếu tia từ nó về phía mắt (chiếu song song) cắt một mặt của hình
 * trước khi ra ngoài. Đoạn được chia tại các điểm chuyển chính xác; các khúc liền nhau cùng trạng thái gộp thành "run"
 * để vẽ nét liền / nét đứt.
 * Tính lại mỗi lần vẽ (sau mỗi lần xoay).
 */
export interface Occluder {
  poly: readonly Vec3[];
  plane: Plane;
}
export const occludersOf = (faces: ReadonlyArray<readonly Vec3[]>): Occluder[] =>
  faces.flatMap((poly) => {
    const plane = polygonPlane(poly);
    return plane ? [{ poly, plane }] : [];
  });

export function isHidden(p: Vec3, eye: Vec3, occ: readonly Occluder[], scaleRef = 1): boolean {
  const eps = 1e-6 * Math.max(1, scaleRef);
  for (const o of occ) {
    const den = dot(o.plane.n, eye);
    if (Math.abs(den) < 1e-9) continue;
    const t = (o.plane.d - dot(o.plane.n, p)) / den;
    if (t <= eps) continue;
    const q = add(p, scale(eye, t));
    if (pointInPolygon3D(q, o.poly)) return true;
  }
  return false;
}

export interface Run {
  from: number;
  to: number;
  hidden: boolean;
}
export type LineStyle = 'solid' | 'dashed' | 'mixed';

/**
 * Chia đoạn [a,b] thành các khúc thấy / khuất (tham số 0…1) với điểm chuyển CHÍNH XÁC (soát E3 #7, trước đây lấy mẫu
 * 32 khúc nên điểm chuyển lệch tới 1/32 và bỏ sót khúc khuất ngắn). Trạng thái khuất chỉ đổi tại:
 * (1) chỗ hình chiếu (theo hướng nhìn) của đoạn cắt hình chiếu một cạnh của mặt che, hoặc
 * (2) chỗ đoạn xuyên qua mặt phẳng của mặt che.
 * Tính mọi tham số đó, sắp xếp, rồi xét trung điểm từng khoảng. Giới hạn: chỉ mặt phẳng đa giác che khuất; đường tròn
 * đáy của hình tròn xoay (mặt cong) chỉ vẽ, không che, nên mặt xung quanh của nón/trụ không làm nét đứt.
 */
export function segmentRuns(a: Vec3, b: Vec3, eye: Vec3, occ: readonly Occluder[], scaleRef = 1): Run[] {
  const ts = transitionParams(a, b, eye, occ);
  const runs: Run[] = [];
  for (let i = 0; i + 1 < ts.length; i++) {
    const t0 = ts[i]!;
    const t1 = ts[i + 1]!;
    const h = isHidden(lerp(a, b, (t0 + t1) / 2), eye, occ, scaleRef);
    const last = runs[runs.length - 1];
    if (last && last.hidden === h) last.to = t1;
    else runs.push({ from: t0, to: t1, hidden: h });
  }
  return runs;
}
/** Tham số 0 = t0 < … < 1 nơi trạng thái khuất có thể đổi (bỏ các điểm cách nhau dưới 1e-9). */
export function transitionParams(a: Vec3, b: Vec3, eye: Vec3, occ: readonly Occluder[]): number[] {
  const e = unit(eye);
  const u = unit(Math.abs(e[0]) < 0.9 ? cross(e, [1, 0, 0]) : cross(e, [0, 1, 0]));
  const v = cross(e, u);
  const P = (p: Vec3): [number, number] => [dot(p, u), dot(p, v)];
  const [ax, ay] = P(a);
  const [bx, by] = P(b);
  const dx = bx - ax;
  const dy = by - ay;
  const ts = [0, 1];
  const inside = (t: number) => t > 1e-9 && t < 1 - 1e-9;
  for (const o of occ) {
    const sa = dot(o.plane.n, a) - o.plane.d;
    const sb = dot(o.plane.n, b) - o.plane.d;
    if (sa !== sb) {
      const t = sa / (sa - sb);
      if (inside(t)) ts.push(t);
    }
    for (let i = 0; i < o.poly.length; i++) {
      const [px, py] = P(o.poly[i]!);
      const [qx, qy] = P(o.poly[(i + 1) % o.poly.length]!);
      const ex = qx - px;
      const ey = qy - py;
      const den = dx * ey - dy * ex;
      if (Math.abs(den) < 1e-12) continue;
      const t = ((px - ax) * ey - (py - ay) * ex) / den;
      const k = ((px - ax) * dy - (py - ay) * dx) / den;
      if (inside(t) && k >= -1e-9 && k <= 1 + 1e-9) ts.push(t);
    }
  }
  ts.sort((x, y) => x - y);
  return ts.filter((t, i) => i === 0 || t - ts[i - 1]! > 1e-9);
}
export function styleOf(runs: readonly Run[]): LineStyle {
  if (runs.every((r) => !r.hidden)) return 'solid';
  if (runs.every((r) => r.hidden)) return 'dashed';
  return 'mixed';
}
export const hiddenRatio = (runs: readonly Run[]): number =>
  runs.reduce((s, r) => s + (r.hidden ? r.to - r.from : 0), 0);
