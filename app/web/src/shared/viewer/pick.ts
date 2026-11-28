import type { Target } from './model.ts';
import { inPoly, nearestOnSegment, type RenderModel } from './render.ts';

/**
 * Chạm trúng đối tượng nào (BR-GEO-146): ưu tiên điểm, rồi đường, rồi mặt (mặt gần mắt nhất).
 * Bán kính chạm theo px màn hình, đủ cho ngón tay (vùng chạm 44 điểm, D-GEO-117).
 */
export interface PickOptions {
  points?: boolean;
  lines?: boolean;
  faces?: boolean;
}
export const POINT_RADIUS = 18;
export const LINE_RADIUS = 11;

export interface Hit {
  target: Target;
  /** Tham số dọc đường (0…1) tại chỗ chạm, khi trúng đường. */
  t?: number;
}

export function pick(rm: RenderModel, x: number, y: number, opt: PickOptions = { points: true, lines: true, faces: true }): Hit | null {
  if (opt.points !== false) {
    let best: { name: string; d: number; depth: number } | null = null;
    for (const p of rm.points) {
      const d = Math.hypot(p.x - x, p.y - y);
      if (d > POINT_RADIUS) continue;
      if (!best || d < best.d - 0.5 || (Math.abs(d - best.d) <= 0.5 && p.depth > best.depth)) best = { name: p.name, d, depth: p.depth };
    }
    if (best) return { target: { kind: 'point', id: best.name } };
  }
  if (opt.lines !== false) {
    let best: { id: string; d: number; t: number; rank: number } | null = null;
    for (const l of rm.lines) {
      const { t, d } = nearestOnSegment(l.a, l.b, x, y);
      if (d > LINE_RADIUS) continue;
      const rank = l.kind === 'extended' ? 1 : 0;
      if (!best || d < best.d - 1 || (Math.abs(d - best.d) <= 1 && rank < best.rank)) best = { id: l.id, d, t, rank };
    }
    if (best) return { target: { kind: 'line', id: best.id }, t: best.t };
  }
  if (opt.faces !== false) {
    // Mặt vẽ trong suốt nên nhiều mặt chồng nhau trên màn: chọn mặt mà chỗ chạm gần "giữa mặt" nhất
    // (khoảng cách tới trọng tâm hình chiếu, chia cho cỡ mặt), rồi mặt gần mắt hơn.
    let best: { id: string; score: number; depth: number; rank: number } | null = null;
    for (const f of rm.faces) {
      if (!inPoly(f.pts, x, y)) continue;
      const rank = f.kind === 'face' ? 0 : f.kind === 'plane' ? 1 : 2;
      const [cx, cy] = areaCentroid(f.pts, f.tris);
      const size = Math.sqrt(Math.max(area2(f.pts), 1));
      const score = Math.hypot(x - cx, y - cy) / size;
      if (!best || rank < best.rank || (rank === best.rank && (score < best.score - 0.02 || (Math.abs(score - best.score) <= 0.02 && f.depth > best.depth)))) {
        best = { id: f.id, score, depth: f.depth, rank };
      }
    }
    if (best) return { target: { kind: 'face', id: best.id } };
  }
  return null;
}

/**
 * Trọng tâm diện tích của mặt trên màn (theo các tam giác tô). Với tam giác và hình bình hành trùng trọng tâm đỉnh;
 * với mặt lõm (BR-GEO-164, đáy chữ L) trọng tâm đỉnh có thể rơi vào đỉnh lõm, trọng tâm diện tích thì nằm trong mặt.
 */
function areaCentroid(pts: Array<{ x: number; y: number }>, tris: Array<[number, number, number]> | undefined): [number, number] {
  let a = 0;
  let sx = 0;
  let sy = 0;
  for (const [i, j, k] of tris ?? []) {
    const p = pts[i]!;
    const q = pts[j]!;
    const r = pts[k]!;
    const w = Math.abs((q.x - p.x) * (r.y - p.y) - (r.x - p.x) * (q.y - p.y)) / 2;
    a += w;
    sx += (w * (p.x + q.x + r.x)) / 3;
    sy += (w * (p.y + q.y + r.y)) / 3;
  }
  if (a > 1e-9) return [sx / a, sy / a];
  return [pts.reduce((s, p) => s + p.x, 0) / pts.length, pts.reduce((s, p) => s + p.y, 0) / pts.length];
}

function area2(pts: Array<{ x: number; y: number }>): number {
  let a = 0;
  for (let i = 0; i < pts.length; i++) {
    const p = pts[i]!;
    const q = pts[(i + 1) % pts.length]!;
    a += p.x * q.y - q.x * p.y;
  }
  return Math.abs(a) / 2;
}
