/**
 * Hình học thuần (không DOM, không React) cho bộ xem 3D: vectơ, mặt phẳng, giao điểm, giao tuyến, khoảng cách,
 * chia tam giác mặt đa giác (cả mặt lõm, US-GEO-307). Chạy được trong `node --test`.
 */
export type Vec3 = readonly [number, number, number];

export const sub = (a: Vec3, b: Vec3): Vec3 => [a[0] - b[0], a[1] - b[1], a[2] - b[2]];
export const add = (a: Vec3, b: Vec3): Vec3 => [a[0] + b[0], a[1] + b[1], a[2] + b[2]];
export const scale = (a: Vec3, k: number): Vec3 => [a[0] * k, a[1] * k, a[2] * k];
export const dot = (a: Vec3, b: Vec3): number => a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
export const cross = (a: Vec3, b: Vec3): Vec3 => [
  a[1] * b[2] - a[2] * b[1],
  a[2] * b[0] - a[0] * b[2],
  a[0] * b[1] - a[1] * b[0],
];
export const norm = (a: Vec3): number => Math.sqrt(dot(a, a));
export const dist = (a: Vec3, b: Vec3): number => norm(sub(a, b));
export const lerp = (a: Vec3, b: Vec3, t: number): Vec3 => add(a, scale(sub(b, a), t));
export function unit(a: Vec3): Vec3 {
  const l = norm(a);
  return l === 0 ? [0, 0, 0] : scale(a, 1 / l);
}
export function centroid(ps: readonly Vec3[]): Vec3 {
  let s: Vec3 = [0, 0, 0];
  for (const p of ps) s = add(s, p);
  return ps.length ? scale(s, 1 / ps.length) : s;
}

/** Sai số tương đối theo cỡ hình (hình mẫu có thể có cạnh 10). */
export const TOL = 1e-7;
export const samePoint = (a: Vec3, b: Vec3, tol = 1e-6): boolean => dist(a, b) <= tol;

/** Ba điểm thẳng hàng (kể cả hai điểm trùng). */
export function collinear(a: Vec3, b: Vec3, c: Vec3): boolean {
  const u = sub(b, a);
  const v = sub(c, a);
  const s = Math.max(norm(u), norm(v), 1e-12);
  return norm(cross(u, v)) <= 1e-7 * s * s;
}

/** Mặt phẳng n·x = d với n đơn vị. */
export interface Plane {
  n: Vec3;
  d: number;
}
export function planeFrom3(a: Vec3, b: Vec3, c: Vec3): Plane | null {
  if (collinear(a, b, c)) return null;
  const n = unit(cross(sub(b, a), sub(c, a)));
  return { n, d: dot(n, a) };
}
/** Pháp tuyến theo Newell (đúng cho cả đa giác lõm, không phụ thuộc ba đỉnh đầu). */
export function polygonNormal(ps: readonly Vec3[]): Vec3 {
  let nx = 0;
  let ny = 0;
  let nz = 0;
  for (let i = 0; i < ps.length; i++) {
    const p = ps[i]!;
    const q = ps[(i + 1) % ps.length]!;
    nx += (p[1] - q[1]) * (p[2] + q[2]);
    ny += (p[2] - q[2]) * (p[0] + q[0]);
    nz += (p[0] - q[0]) * (p[1] + q[1]);
  }
  return [nx, ny, nz];
}
export function polygonPlane(ps: readonly Vec3[]): Plane | null {
  const n = polygonNormal(ps);
  if (norm(n) < 1e-12) return null;
  const u = unit(n);
  return { n: u, d: dot(u, centroid(ps)) };
}
export const polygonArea = (ps: readonly Vec3[]): number => norm(polygonNormal(ps)) / 2;
export const signedDist = (pl: Plane, p: Vec3): number => dot(pl.n, p) - pl.d;
export const footOnPlane = (pl: Plane, p: Vec3): Vec3 => sub(p, scale(pl.n, signedDist(pl, p)));
export function samePlane(a: Plane, b: Plane, tol = 1e-6): boolean {
  const c = dot(a.n, b.n);
  if (Math.abs(Math.abs(c) - 1) > 1e-9) return false;
  return Math.abs(a.d - Math.sign(c) * b.d) <= tol;
}

/** Chiếu đa giác phẳng xuống 2D theo trục trội của pháp tuyến (giữ chiều quay). */
function to2D(ps: readonly Vec3[], n: Vec3): Array<[number, number]> {
  const ax = Math.abs(n[0]);
  const ay = Math.abs(n[1]);
  const az = Math.abs(n[2]);
  if (az >= ax && az >= ay) return ps.map((p) => (n[2] >= 0 ? [p[0], p[1]] : [p[1], p[0]]));
  if (ax >= ay) return ps.map((p) => (n[0] >= 0 ? [p[1], p[2]] : [p[2], p[1]]));
  return ps.map((p) => (n[1] >= 0 ? [p[2], p[0]] : [p[0], p[2]]));
}

export function pointInPolygon2D(x: number, y: number, poly: ReadonlyArray<readonly [number, number]>): boolean {
  let inside = false;
  for (let i = 0, j = poly.length - 1; i < poly.length; j = i++) {
    const [xi, yi] = poly[i]!;
    const [xj, yj] = poly[j]!;
    if (yi > y !== yj > y && x < ((xj - xi) * (y - yi)) / (yj - yi) + xi) inside = !inside;
  }
  return inside;
}

/** Điểm (đã nằm trên mặt phẳng của đa giác) có nằm trong đa giác không. */
export function pointInPolygon3D(p: Vec3, poly: readonly Vec3[]): boolean {
  const n = polygonNormal(poly);
  const [q] = to2D([p], n);
  return pointInPolygon2D(q![0], q![1], to2D(poly, n));
}

/**
 * Chia đa giác phẳng (lồi hoặc lõm, không tự cắt) thành tam giác bằng cắt tai. Trả chỉ số đỉnh.
 * Lỗi bản cũ (SPEC-technical lỗi 5): chia quạt từ đỉnh 0 làm tràn ra ngoài mặt lõm.
 */
export function triangulate(poly: readonly Vec3[]): Array<[number, number, number]> {
  const n = polygonNormal(poly);
  const p2 = to2D(poly, n);
  // to2D giữ chiều: diện tích có dấu dương nếu ngược chiều kim đồng hồ
  const area2 = (a: number, b: number, c: number) => {
    const [ax, ay] = p2[a]!;
    const [bx, by] = p2[b]!;
    const [cx, cy] = p2[c]!;
    return (bx - ax) * (cy - ay) - (by - ay) * (cx - ax);
  };
  let idx = poly.map((_, i) => i);
  let s = 0;
  for (let i = 0; i < idx.length; i++) s += area2(0, i, (i + 1) % idx.length);
  if (s < 0) idx = idx.reverse();
  const out: Array<[number, number, number]> = [];
  let guard = 0;
  while (idx.length > 3 && guard++ < 10_000) {
    let cut = false;
    for (let i = 0; i < idx.length; i++) {
      const a = idx[(i + idx.length - 1) % idx.length]!;
      const b = idx[i]!;
      const c = idx[(i + 1) % idx.length]!;
      if (area2(a, b, c) <= 1e-14) continue; // đỉnh lõm hoặc thẳng hàng
      const tri: Array<[number, number]> = [p2[a]!, p2[b]!, p2[c]!];
      let empty = true;
      for (const k of idx) {
        if (k === a || k === b || k === c) continue;
        const [x, y] = p2[k]!;
        if (pointInTriangle(x, y, tri)) {
          empty = false;
          break;
        }
      }
      if (!empty) continue;
      out.push([a, b, c]);
      idx.splice(i, 1);
      cut = true;
      break;
    }
    if (!cut) break; // đa giác suy biến: dừng, phần còn lại chia quạt
  }
  for (let i = 1; i + 1 < idx.length; i++) out.push([idx[0]!, idx[i]!, idx[i + 1]!]);
  return out;
}
function pointInTriangle(x: number, y: number, t: Array<[number, number]>): boolean {
  const [[ax, ay], [bx, by], [cx, cy]] = t as [[number, number], [number, number], [number, number]];
  const d1 = (x - bx) * (ay - by) - (ax - bx) * (y - by);
  const d2 = (x - cx) * (by - cy) - (bx - cx) * (y - cy);
  const d3 = (x - ax) * (cy - ay) - (cx - ax) * (y - ay);
  const neg = d1 < -1e-12 || d2 < -1e-12 || d3 < -1e-12;
  const pos = d1 > 1e-12 || d2 > 1e-12 || d3 > 1e-12;
  return !(neg && pos);
}

/** Quan hệ hai đường thẳng (vô hạn) qua p1 hướng d1 và p2 hướng d2. */
export type LineRelation =
  | { kind: 'intersect'; point: Vec3; t1: number; t2: number }
  | { kind: 'parallel'; distance: number }
  | { kind: 'same' }
  | { kind: 'skew'; distance: number };

export function lineRelation(p1: Vec3, d1: Vec3, p2: Vec3, d2: Vec3): LineRelation {
  const n = cross(d1, d2);
  const w = sub(p2, p1);
  const scaleRef = Math.max(norm(d1) * norm(d2), 1e-12);
  if (norm(n) <= 1e-9 * scaleRef) {
    const dd = norm(cross(w, d1)) / norm(d1);
    return dd <= 1e-7 * Math.max(1, norm(d1)) ? { kind: 'same' } : { kind: 'parallel', distance: dd };
  }
  const gap = Math.abs(dot(w, unit(n)));
  if (gap > 1e-7 * Math.max(1, norm(w))) return { kind: 'skew', distance: gap };
  const nn = dot(n, n);
  const t1 = dot(cross(w, d2), n) / nn;
  const t2 = dot(cross(w, d1), n) / nn;
  return { kind: 'intersect', point: add(p1, scale(d1, t1)), t1, t2 };
}

/** Khoảng cách điểm tới đường thẳng qua a hướng d. */
export const distPointLine = (p: Vec3, a: Vec3, d: Vec3): number => norm(cross(sub(p, a), d)) / norm(d);
export function footOnLine(p: Vec3, a: Vec3, d: Vec3): Vec3 {
  return add(a, scale(d, dot(sub(p, a), d) / dot(d, d)));
}

/** Khoảng cách giữa hai đường thẳng (0 nếu cắt hoặc trùng). */
export function lineLineDistance(p1: Vec3, d1: Vec3, p2: Vec3, d2: Vec3): number {
  const r = lineRelation(p1, d1, p2, d2);
  if (r.kind === 'skew' || r.kind === 'parallel') return r.distance;
  return 0;
}

/** Góc AOB (độ). */
export function angleAt(a: Vec3, o: Vec3, b: Vec3): number {
  const u = sub(a, o);
  const v = sub(b, o);
  const c = dot(u, v) / (norm(u) * norm(v));
  return (Math.acos(Math.max(-1, Math.min(1, c))) * 180) / Math.PI;
}

/** Giao của mặt phẳng với cạnh [a,b]: điểm nếu cắt (kể cả tại đầu mút), null nếu không. */
export function planeSegment(pl: Plane, a: Vec3, b: Vec3): Vec3 | null {
  const sa = signedDist(pl, a);
  const sb = signedDist(pl, b);
  const tol = 1e-9;
  if (Math.abs(sa) <= tol) return a;
  if (Math.abs(sb) <= tol) return b;
  if (sa * sb > 0) return null;
  return lerp(a, b, sa / (sa - sb));
}

function dedupe(ps: Vec3[]): Vec3[] {
  const out: Vec3[] = [];
  for (const p of ps) if (!out.some((q) => samePoint(p, q, 1e-7))) out.push(p);
  return out;
}

/**
 * Giao tuyến của mặt phẳng `pl` với một mặt đa giác: các đoạn nằm trong đa giác (đa giác lõm có thể cho nhiều đoạn).
 * Mặt nằm trong `pl` hoặc song song: [].
 */
export function planePolygonTrace(pl: Plane, poly: readonly Vec3[]): Array<[Vec3, Vec3]> {
  const fp = polygonPlane(poly);
  if (!fp) return [];
  const dir = cross(pl.n, fp.n);
  if (norm(dir) < 1e-9) return [];
  const pts: Vec3[] = [];
  for (let i = 0; i < poly.length; i++) {
    const x = planeSegment(pl, poly[i]!, poly[(i + 1) % poly.length]!);
    if (x) pts.push(x);
  }
  const uniq = dedupe(pts).sort((a, b) => dot(a, dir) - dot(b, dir));
  const out: Array<[Vec3, Vec3]> = [];
  for (let i = 0; i + 1 < uniq.length; i++) {
    const a = uniq[i]!;
    const b = uniq[i + 1]!;
    if (dist(a, b) < 1e-7) continue;
    if (pointInPolygon3D(lerp(a, b, 0.5), poly)) out.push([a, b]);
  }
  return mergeCollinear(out);
}
function mergeCollinear(segs: Array<[Vec3, Vec3]>): Array<[Vec3, Vec3]> {
  const out: Array<[Vec3, Vec3]> = [];
  for (const s of segs) {
    const last = out[out.length - 1];
    if (last && samePoint(last[1], s[0], 1e-7)) last[1] = s[1];
    else out.push([s[0], s[1]]);
  }
  return out;
}

/** Đa giác phẳng phóng to quanh trọng tâm (mở rộng mặt vượt ra ngoài mép). */
export const enlarge = (poly: readonly Vec3[], k: number): Vec3[] => {
  const c = centroid(poly);
  return poly.map((p) => add(c, scale(sub(p, c), k)));
};

/** Bao lồi các điểm đồng phẳng (để vẽ thiết diện của mặt phẳng mới), theo thứ tự quanh pháp tuyến. */
export function planarHull(ps: readonly Vec3[], n: Vec3): Vec3[] {
  const pts = dedupe([...ps]);
  if (pts.length < 3) return pts;
  const c = centroid(pts);
  const u = unit(Math.abs(n[0]) < 0.9 ? cross(n, [1, 0, 0]) : cross(n, [0, 1, 0]));
  const v = cross(n, u);
  return pts
    .map((p) => ({ p, a: Math.atan2(dot(sub(p, c), v), dot(sub(p, c), u)) }))
    .sort((x, y) => x.a - y.a)
    .map((x) => x.p);
}
