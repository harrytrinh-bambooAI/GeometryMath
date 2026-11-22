import { add, dot, lerp, pointInPolygon3D, polygonPlane, scale, type Plane, type Vec3 } from './geometry.ts';

/**
 * Cạnh khuất (BR-GEO-142): một điểm bị khuất nếu tia từ nó về phía mắt (chiếu song song) cắt một mặt của hình
 * trước khi ra ngoài. Đoạn được lấy mẫu; các khúc liền nhau cùng trạng thái gộp thành "run" để vẽ nét liền / nét đứt.
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

const SAMPLES = 32;
/** Chia đoạn [a,b] thành các khúc thấy / khuất (tham số 0…1). */
export function segmentRuns(a: Vec3, b: Vec3, eye: Vec3, occ: readonly Occluder[], scaleRef = 1): Run[] {
  const runs: Run[] = [];
  for (let i = 0; i < SAMPLES; i++) {
    const t0 = i / SAMPLES;
    const t1 = (i + 1) / SAMPLES;
    const h = isHidden(lerp(a, b, (t0 + t1) / 2), eye, occ, scaleRef);
    const last = runs[runs.length - 1];
    if (last && last.hidden === h) last.to = t1;
    else runs.push({ from: t0, to: t1, hidden: h });
  }
  return runs;
}
export function styleOf(runs: readonly Run[]): LineStyle {
  if (runs.every((r) => !r.hidden)) return 'solid';
  if (runs.every((r) => r.hidden)) return 'dashed';
  return 'mixed';
}
export const hiddenRatio = (runs: readonly Run[]): number =>
  runs.reduce((s, r) => s + (r.hidden ? r.to - r.from : 0), 0);
