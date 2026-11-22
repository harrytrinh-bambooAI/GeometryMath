import { formatAngle, formatLength } from './format.ts';
import {
  add,
  angleAt,
  centroid,
  collinear,
  cross,
  dist,
  dot,
  enlarge,
  footOnLine,
  footOnPlane,
  lerp,
  lineRelation,
  norm,
  planarHull,
  planeFrom3,
  planePolygonTrace,
  planeSegment,
  polygonPlane,
  samePlane,
  samePoint,
  scale,
  signedDist,
  sub,
  unit,
  type Plane,
  type Vec3,
} from './geometry.ts';
import { resolveMeasure, sameMeasure, type GeoMeasure } from './measures.ts';
import { VIEWER_MSG, VIEWER_TEXT, type ViewerMsgCode } from './messages.ts';
import type { LockState, MeasureRef, ToolId, ViewerSolid } from './types.ts';

/**
 * Mô hình hình học của bộ xem (thuần, không DOM): hình gốc + đối tượng học sinh vẽ thêm + phép đo + đối tượng
 * đang chọn. Mọi thao tác là hàm thuần `(state, ...) => Outcome`; giao diện chỉ gọi và vẽ lại.
 * Quy tắc: BR-GEO-145…164, 290.
 */
export interface PointObj {
  name: string;
  pos: Vec3;
  user: boolean;
  origin?: 'mid' | 'ratio' | 'free' | 'intersection';
  /** Nhãn đối tượng chứa điểm (ví dụ "SD") cho điểm trên cạnh. */
  on?: string;
}
export type LineKind = 'edge' | 'segment' | 'extended';
export interface LineObj {
  id: string;
  label: string;
  kind: LineKind;
  /** Hai đầu mút (đã sắp theo tên) của đoạn gốc. */
  ends: [string, string];
  a: Vec3;
  b: Vec3;
}
export type FaceKind = 'face' | 'plane' | 'extended';
export interface FaceObj {
  id: string;
  label: string;
  kind: FaceKind;
  names: string[];
  poly: Vec3[];
}
export interface Trace {
  id: string;
  plane: string;
  face: string;
  a: Vec3;
  b: Vec3;
}
export interface Measure {
  id: string;
  kind: 'length' | 'angle';
  ref: MeasureRef;
  label: string;
  value: number;
  /** null khi đại lượng bị ẩn (BR-GEO-156): giá trị không bao giờ được định dạng ra chuỗi. */
  text: string | null;
  hidden: boolean;
  draw:
    | { kind: 'segment'; a: Vec3; b: Vec3 }
    | { kind: 'angle'; a: Vec3; o: Vec3; b: Vec3 }
    | { kind: 'perp'; from: Vec3; foot: Vec3 }
    | { kind: 'none' };
}
export type Target = { kind: 'point'; id: string } | { kind: 'line'; id: string } | { kind: 'face'; id: string };
export interface Selection {
  kind: 'point' | LineKind | FaceKind;
  id: string;
  label: string;
}

export interface Base {
  points: PointObj[];
  edges: LineObj[];
  faces: FaceObj[];
  circles: NonNullable<ViewerSolid['circles']>;
  unit: ViewerSolid['unit'];
  hidden: GeoMeasure[];
  center: Vec3;
  radius: number;
  /** Thứ tự tên đỉnh để gọi tên cạnh, mặt ổn định ("Cạnh SD", "Mặt SAD", "Mặt ABCD"). */
  rank: Map<string, number>;
}

export interface FigureState {
  base: Base;
  userPoints: PointObj[];
  segments: Array<{ id: string; ends: [string, string] }>;
  planes: Array<{ id: string; label: string; names: [string, string, string]; poly: Vec3[]; traces: Trace[] }>;
  extensions: Array<{ id: string; kind: 'face' | 'edge'; target: string; label: string; poly?: Vec3[]; a?: Vec3; b?: Vec3 }>;
  measures: Measure[];
  selection: Selection | null;
  /** Các lần chạm đang chờ đủ (Đường, Mặt phẳng, Đo dài, Đo góc). */
  pending: Target[];
  axes: boolean;
  lock: LockState;
  seq: number;
}

export interface Notice {
  kind: 'toast' | 'inline';
  code?: ViewerMsgCode;
  text: string;
}
export interface Outcome {
  state: FigureState;
  /** Công cụ vừa tạo ra một thao tác trên hình (để ghi sự kiện, D-GEO-058); không có = không ghi. */
  op?: ToolId;
  notice?: Notice;
}
const msg = (code: ViewerMsgCode, kind: Notice['kind'] = 'toast'): Notice => ({ kind, code, text: VIEWER_MSG[code] });

// ------------------------------------------------------------------ dựng hình gốc
function baseLetter(n: string) {
  return n.replace(/[0-9′']/g, '');
}
function primes(n: string) {
  return (n.match(/[′']/g) ?? []).length;
}
/** Đỉnh S (đỉnh chóp) gọi trước, rồi theo chữ cái, rồi theo số dấu phẩy trên (A, B, …, A′, B′). */
function rankNames(ns: string[]): Map<string, number> {
  const sorted = [...ns].sort((x, y) => {
    const sx = x === 'S' ? 0 : 1;
    const sy = y === 'S' ? 0 : 1;
    if (sx !== sy) return sx - sy;
    if (primes(x) !== primes(y)) return primes(x) - primes(y);
    return baseLetter(x).localeCompare(baseLetter(y)) || x.localeCompare(y);
  });
  return new Map(sorted.map((n, i) => [n, i]));
}

export function buildBase(solid: ViewerSolid): Base {
  const points: PointObj[] = solid.points.map((p) => ({ name: p.name, pos: p.pos, user: false }));
  const pos = new Map(points.map((p) => [p.name, p.pos]));
  const rank = rankNames(points.map((p) => p.name));
  const r = (n: string) => rank.get(n) ?? 1e6;
  const edges: LineObj[] = [];
  for (const [x, y] of solid.edges) {
    const a = pos.get(x);
    const b = pos.get(y);
    if (!a || !b) continue;
    const ends: [string, string] = r(x) <= r(y) ? [x, y] : [y, x];
    const id = ends.join('');
    if (edges.some((e) => e.id === id)) continue;
    edges.push({ id, label: `Cạnh ${id}`, kind: 'edge', ends, a: pos.get(ends[0])!, b: pos.get(ends[1])! });
  }
  const faces: FaceObj[] = [];
  for (const f of solid.faces) {
    if (f.length < 3 || f.some((n) => !pos.has(n))) continue;
    const names = canonicalCycle(f, r);
    const id = names.join('');
    faces.push({ id, label: `Mặt ${id}`, kind: 'face', names, poly: names.map((n) => pos.get(n)!) });
  }
  const all = [...points.map((p) => p.pos), ...(solid.circles ?? []).flatMap((c) => circleBox(c))];
  const lo: [number, number, number] = [Infinity, Infinity, Infinity];
  const hi: [number, number, number] = [-Infinity, -Infinity, -Infinity];
  for (const p of all) for (let i = 0; i < 3; i++) {
    lo[i] = Math.min(lo[i]!, p[i]!);
    hi[i] = Math.max(hi[i]!, p[i]!);
  }
  const center: Vec3 = all.length ? scale(add(lo, hi), 0.5) : [0, 0, 0];
  const radius = Math.max(1e-6, ...all.map((p) => dist(p, center)));
  const pt = (n: string) => pos.get(n);
  const hidden = (solid.hiddenMeasures ?? []).flatMap((m) => {
    const g = resolveMeasure(m, pt);
    return g ? [g] : [];
  });
  return { points, edges, faces, circles: solid.circles ?? [], unit: solid.unit ?? null, hidden, center, radius, rank };
}
function circleBox(c: { center: Vec3; radius: number; normal: Vec3 }): Vec3[] {
  const n = unit(c.normal);
  const u = unit(Math.abs(n[0]) < 0.9 ? cross(n, [1, 0, 0]) : cross(n, [0, 1, 0]));
  const v = cross(n, u);
  return [add(c.center, scale(u, c.radius)), add(c.center, scale(u, -c.radius)), add(c.center, scale(v, c.radius)), add(c.center, scale(v, -c.radius))];
}
/** Xoay vòng đỉnh để bắt đầu từ đỉnh có hạng nhỏ nhất, đi về phía láng giềng hạng nhỏ hơn. */
function canonicalCycle(f: string[], r: (n: string) => number): string[] {
  let k = 0;
  for (let i = 1; i < f.length; i++) if (r(f[i]!) < r(f[k]!)) k = i;
  const next = f[(k + 1) % f.length]!;
  const prev = f[(k + f.length - 1) % f.length]!;
  const out: string[] = [];
  const step = r(next) <= r(prev) ? 1 : -1;
  for (let i = 0; i < f.length; i++) out.push(f[(k + step * i + f.length * 2) % f.length]!);
  return out;
}

export function initialState(solid: ViewerSolid, lock: LockState = 'locked'): FigureState {
  return {
    base: buildBase(solid),
    userPoints: [],
    segments: [],
    planes: [],
    extensions: [],
    measures: [],
    selection: null,
    pending: [],
    axes: false,
    lock,
    seq: 0,
  };
}

// ------------------------------------------------------------------ tra cứu
export const allPoints = (s: FigureState): PointObj[] => [...s.base.points, ...s.userPoints];
export function pointPos(s: FigureState, name: string): Vec3 | undefined {
  return allPoints(s).find((p) => p.name === name)?.pos;
}
function nameRank(s: FigureState, n: string): number {
  const r = s.base.rank.get(n);
  if (r !== undefined) return r;
  const i = s.userPoints.findIndex((p) => p.name === n);
  return 1000 + (i < 0 ? 999 : i);
}
const sortNames = (s: FigureState, ns: string[]) => [...ns].sort((x, y) => nameRank(s, x) - nameRank(s, y));

/** Mọi đường: cạnh hình, đoạn vẽ thêm, đường kéo dài. */
export function allLines(s: FigureState): LineObj[] {
  const segs: LineObj[] = s.segments.map((g) => ({
    id: `seg:${g.id}`,
    label: `Đoạn ${g.id}`,
    kind: 'segment',
    ends: g.ends,
    a: pointPos(s, g.ends[0])!,
    b: pointPos(s, g.ends[1])!,
  }));
  const ext: LineObj[] = s.extensions
    .filter((e) => e.kind === 'edge')
    .map((e) => {
      const src = findLine(s, e.target, [...s.base.edges, ...segs]);
      return { id: e.id, label: e.label, kind: 'extended', ends: src?.ends ?? ['', ''], a: e.a!, b: e.b! };
    });
  return [...s.base.edges, ...segs, ...ext];
}
function findLine(_s: FigureState, id: string, lines: LineObj[]) {
  return lines.find((l) => l.id === id);
}
/** Mọi mặt chạm được: mặt hình, mặt phẳng dựng thêm, mặt mở rộng. */
export function allFaces(s: FigureState): FaceObj[] {
  return [
    ...s.base.faces,
    ...s.planes.map((p): FaceObj => ({ id: p.id, label: p.label, kind: 'plane', names: p.names, poly: p.poly })),
    ...s.extensions
      .filter((e) => e.kind === 'face')
      .map((e): FaceObj => ({ id: e.id, label: e.label, kind: 'extended', names: [], poly: e.poly! })),
  ];
}
const lineById = (s: FigureState, id: string) => allLines(s).find((l) => l.id === id);
const faceById = (s: FigureState, id: string) => allFaces(s).find((f) => f.id === id);
function planeOfFace(f: FaceObj): Plane | null {
  return polygonPlane(f.poly);
}
const shortFace = (f: FaceObj) => (f.kind === 'face' ? f.id : f.id.replace(/^ext:/, '').replace(/[()]/g, ''));

function labelOf(s: FigureState, t: Target): Selection | null {
  if (t.kind === 'point') return allPoints(s).some((p) => p.name === t.id) ? { kind: 'point', id: t.id, label: `Điểm ${t.id}` } : null;
  if (t.kind === 'line') {
    const l = lineById(s, t.id);
    return l ? { kind: l.kind, id: l.id, label: l.label } : null;
  }
  const f = faceById(s, t.id);
  return f ? { kind: f.kind, id: f.id, label: f.label } : null;
}

/** Chữ cái in hoa kế tiếp chưa dùng, bắt đầu từ M (BR-GEO-153, D-GEO-118). */
export function nextName(s: FigureState): string {
  const used = new Set(allPoints(s).map((p) => p.name));
  const order = 'MNOPQRSTUVWXYZABCDEFGHIJKL'.split('');
  for (let k = 0; ; k++) {
    for (const c of order) {
      const n = k === 0 ? c : `${c}${k}`;
      if (!used.has(n)) return n;
    }
  }
}
function existingAt(s: FigureState, p: Vec3): PointObj | undefined {
  const tol = 1e-6 * Math.max(1, s.base.radius);
  return allPoints(s).find((q) => samePoint(q.pos, p, tol));
}

// ------------------------------------------------------------------ thao tác
const bump = (s: FigureState): FigureState => ({ ...s, seq: s.seq + 1 });

/** "Chọn" (BR-GEO-145…147): chạm trúng thì chọn và tính một thao tác; chạm ra ngoài thì bỏ chọn, không tính. */
export function select(s: FigureState, t: Target | null): Outcome {
  if (!t) return { state: { ...s, selection: null } };
  const sel = labelOf(s, t);
  if (!sel) return { state: { ...s, selection: null } };
  return { state: bump({ ...s, selection: sel }), op: 'select' };
}

export function toggleAxes(s: FigureState): FigureState {
  return { ...s, axes: !s.axes };
}
export const clearPending = (s: FigureState): FigureState => ({ ...s, pending: [] });

/** "Xóa hết" (BR-GEO-290): bỏ mọi đối tượng vẽ thêm; hình gốc giữ nguyên. Góc nhìn nằm ngoài mô hình nên không đổi. */
export function clearAll(s: FigureState): FigureState {
  return bump({ ...s, userPoints: [], segments: [], planes: [], extensions: [], measures: [], selection: null, pending: [] });
}

export function setLock(s: FigureState, lock: LockState): FigureState {
  if (s.lock === lock) return s;
  // đổi trạng thái khóa: số đo đã hiện giữ nguyên; số đo đang ẩn chỉ hiện ở LẦN ĐO KẾ TIẾP (US-GEO-305-SC6)
  return { ...s, lock };
}

export type PointMode = { kind: 'mid' } | { kind: 'ratio'; m: number; n: number; from: string } | { kind: 'free'; t: number };

/** "Điểm" (BR-GEO-152): trung điểm, chia tỉ lệ m : n tính từ đầu mút `from`, điểm tự do tại tham số t. */
export function addPointOn(s: FigureState, lineId: string, mode: PointMode): Outcome {
  const l = lineById(s, lineId);
  if (!l || l.kind === 'extended') return { state: s };
  let p: Vec3;
  if (mode.kind === 'mid') p = lerp(l.a, l.b, 0.5);
  else if (mode.kind === 'ratio') {
    if (!(Number.isFinite(mode.m) && Number.isFinite(mode.n) && mode.m > 0 && mode.n > 0)) {
      return { state: s, notice: msg('19', 'inline') };
    }
    const [x, y] = mode.from === l.ends[1] ? [l.b, l.a] : [l.a, l.b];
    p = lerp(x, y, mode.m / (mode.m + mode.n));
  } else {
    const t = Math.min(1, Math.max(0, mode.t));
    p = lerp(l.a, l.b, t);
  }
  if (existingAt(s, p)) return { state: s, notice: msg('26') };
  const name = nextName(s);
  const origin = mode.kind;
  const pt: PointObj = { name, pos: p, user: true, origin, on: l.ends.join('') };
  return { state: bump({ ...s, userPoints: [...s.userPoints, pt], pending: [] }), op: 'point' };
}

/** Tham số t (0…1) của hình chiếu một điểm 3D lên đoạn của đường `lineId`. */
export function lineParam(s: FigureState, lineId: string, p: Vec3): number {
  const l = lineById(s, lineId);
  if (!l) return 0.5;
  const d = sub(l.b, l.a);
  return dot(sub(p, l.a), d) / dot(d, d);
}

function addSegment(s: FigureState, a: string, b: string): Outcome {
  if (a === b) return { state: { ...s, pending: [] } };
  const ends = sortNames(s, [a, b]) as [string, string];
  const id = ends.join('');
  if (s.segments.some((g) => g.id === id)) return { state: { ...s, pending: [] } };
  let next: FigureState = { ...s, segments: [...s.segments, { id, ends }], pending: [] };
  // BR-GEO-149: đoạn mới cắt đoạn vẽ thêm khác (cùng mặt phẳng) thì giao điểm thành điểm có tên
  const pa = pointPos(next, ends[0])!;
  const pb = pointPos(next, ends[1])!;
  for (const g of s.segments) {
    const qa = pointPos(next, g.ends[0])!;
    const qb = pointPos(next, g.ends[1])!;
    const r = lineRelation(pa, sub(pb, pa), qa, sub(qb, qa));
    if (r.kind !== 'intersect') continue;
    const inside = (t: number) => t > 1e-9 && t < 1 - 1e-9;
    if (!inside(r.t1) || !inside(r.t2) || existingAt(next, r.point)) continue;
    next = { ...next, userPoints: [...next.userPoints, { name: nextName(next), pos: r.point, user: true, origin: 'intersection' }] };
  }
  return { state: bump(next), op: 'line' };
}

function intersectLines(s: FigureState, id1: string, id2: string): Outcome {
  const l1 = lineById(s, id1);
  const l2 = lineById(s, id2);
  const cleared = { ...s, pending: [] };
  if (!l1 || !l2 || id1 === id2) return { state: cleared };
  const r = lineRelation(l1.a, sub(l1.b, l1.a), l2.a, sub(l2.b, l2.a));
  if (r.kind === 'skew') return { state: cleared, notice: msg('23') };
  if (r.kind === 'parallel') return { state: cleared, notice: { kind: 'toast', text: VIEWER_TEXT.parallel } };
  if (r.kind === 'same') return { state: cleared };
  if (existingAt(s, r.point)) return { state: cleared, notice: msg('26') };
  const pt: PointObj = { name: nextName(s), pos: r.point, user: true, origin: 'intersection' };
  return { state: bump({ ...cleared, userPoints: [...s.userPoints, pt] }), op: 'line' };
}

/** "Đường" (BR-GEO-148, 149, 163): hai điểm → đoạn; hai đường → giao điểm, hoặc MSG-GEO-23 nếu chéo nhau. */
export function lineTap(s: FigureState, t: Target | null): Outcome {
  if (!t || t.kind === 'face') return { state: { ...s, pending: [] } };
  const first = s.pending[0];
  if (!first || first.kind !== t.kind) return { state: { ...s, pending: [t] } };
  if (first.kind === 'point') return addSegment(s, first.id, t.id);
  return intersectLines(s, first.id, t.id);
}

/** "Mặt phẳng" (BR-GEO-150): ba điểm không thẳng hàng; vẽ thiết diện và giao tuyến với các mặt của hình. */
export function planeTap(s: FigureState, t: Target | null): Outcome {
  if (!t || t.kind !== 'point') return { state: { ...s, pending: [] } };
  if (s.pending.some((p) => p.id === t.id)) return { state: s };
  const pend = [...s.pending, t];
  if (pend.length < 3) return { state: { ...s, pending: pend } };
  const ns = pend.map((p) => p.id) as [string, string, string];
  const [a, b, c] = ns.map((n) => pointPos(s, n)!) as [Vec3, Vec3, Vec3];
  if (collinear(a, b, c)) return { state: { ...s, pending: [] }, notice: msg('25') };
  const pl = planeFrom3(a, b, c)!;
  const names = sortNames(s, ns) as [string, string, string];
  const id = `(${names.join('')})`;
  if (s.planes.some((p) => samePlane(planeFrom3(...(p.names.map((n) => pointPos(s, n)!) as [Vec3, Vec3, Vec3]))!, pl))) {
    return { state: { ...s, pending: [] } };
  }
  const cut: Vec3[] = [a, b, c];
  for (const e of s.base.edges) {
    const x = planeSegment(pl, e.a, e.b);
    if (x) cut.push(x);
  }
  const poly = planarHull(cut, pl.n);
  const traces: Trace[] = [];
  for (const f of s.base.faces) {
    const fp = planeOfFace(f);
    if (!fp || samePlane(fp, pl)) continue;
    for (const [x, y] of planePolygonTrace(pl, f.poly)) traces.push({ id: `${id}∩${f.id}`, plane: id, face: f.id, a: x, b: y });
  }
  const plane = { id, label: `Mặt phẳng ${id}`, names, poly, traces };
  return { state: bump({ ...s, planes: [...s.planes, plane], pending: [] }), op: 'plane' };
}

/** "Mở rộng" (BR-GEO-151): mặt → mặt phẳng chứa nó vượt ra ngoài mép; cạnh → đường thẳng kéo dài hai phía. */
export function extendTap(s: FigureState, t: Target | null): Outcome {
  if (!t || t.kind === 'point') return { state: s };
  if (t.kind === 'line') {
    const l = lineById(s, t.id);
    if (!l || l.kind === 'extended') return { state: s };
    const id = `ext:${l.ends.join('')}`;
    if (s.extensions.some((e) => e.id === id)) return { state: s };
    const d = sub(l.b, l.a);
    const k = Math.max(0.75, (0.9 * s.base.radius) / Math.max(norm(d), 1e-9));
    const e = { id, kind: 'edge' as const, target: l.id, label: `Đường thẳng ${l.ends.join('')}`, a: sub(l.a, scale(d, k)), b: add(l.b, scale(d, k)) };
    return { state: bump({ ...s, extensions: [...s.extensions, e] }), op: 'extend' };
  }
  const f = faceById(s, t.id);
  if (!f || f.kind !== 'face') return { state: s };
  const id = `ext:(${f.id})`;
  if (s.extensions.some((e) => e.id === id)) return { state: s };
  const e = { id, kind: 'face' as const, target: f.id, label: `Mặt phẳng (${f.id})`, poly: enlarge(f.poly, 2) };
  return { state: bump({ ...s, extensions: [...s.extensions, e] }), op: 'extend' };
}

// ------------------------------------------------------------------ đo (BR-GEO-16, 155…158)
function finishMeasure(s: FigureState, kind: Measure['kind'], ref: MeasureRef, label: string, value: number, draw: Measure['draw']): Outcome {
  const g = resolveMeasure(ref, (n) => pointPos(s, n));
  const hidden = s.lock === 'locked' && !!g && s.base.hidden.some((h) => sameMeasure(h, g));
  const text = hidden ? null : kind === 'angle' ? formatAngle(value) : formatLength(value, s.base.unit);
  const m: Measure = { id: `m${s.seq + 1}`, kind, ref, label, value: hidden ? NaN : value, text, hidden, draw };
  const state = bump({ ...s, measures: [...s.measures, m], pending: [] });
  return { state, op: kind === 'angle' ? 'angle' : 'length', notice: hidden ? msg('24') : undefined };
}

function lineEndsRef(s: FigureState, l: LineObj): [string, string] {
  return sortNames(s, l.ends) as [string, string];
}
function faceRefNames(f: FaceObj): string[] {
  return f.kind === 'face' ? f.names : f.kind === 'plane' ? f.names : shortFace(f).split(/(?=[A-Z])/);
}

/** "Đo dài": điểm–điểm, điểm–mặt, điểm–đường, đường–đường, mặt–mặt (khoảng cách). */
export function lengthTap(s: FigureState, t: Target | null): Outcome {
  if (!t) return { state: { ...s, pending: [] } };
  const first = s.pending[0];
  if (!first) return { state: { ...s, pending: [t] } };
  if (first.kind === t.kind && first.id === t.id) return { state: s };
  const [x, y] = first.kind === 'point' || (first.kind === 'line' && t.kind === 'face') ? [first, t] : [t, first];
  if (x.kind === 'point' && y.kind === 'point') {
    const a = pointPos(s, x.id)!;
    const b = pointPos(s, y.id)!;
    return finishMeasure(s, 'length', { kind: 'len', a: x.id, b: y.id }, `${x.id}${y.id}`, dist(a, b), { kind: 'segment', a, b });
  }
  if (x.kind === 'point' && y.kind === 'face') {
    const p = pointPos(s, x.id)!;
    const f = faceById(s, y.id)!;
    const pl = planeOfFace(f)!;
    const foot = footOnPlane(pl, p);
    const fl = shortFace(f);
    return finishMeasure(s, 'length', { kind: 'dpp', p: x.id, plane: faceRefNames(f) }, `d(${x.id}, (${fl}))`, Math.abs(signedDist(pl, p)), {
      kind: 'perp',
      from: p,
      foot,
    });
  }
  if (x.kind === 'point' && y.kind === 'line') {
    const p = pointPos(s, x.id)!;
    const l = lineById(s, y.id)!;
    const foot = footOnLine(p, l.a, sub(l.b, l.a));
    const le = lineEndsRef(s, l);
    return finishMeasure(s, 'length', { kind: 'dpl', p: x.id, line: le }, `d(${x.id}, ${le.join('')})`, dist(p, foot), {
      kind: 'perp',
      from: p,
      foot,
    });
  }
  if (x.kind === 'line' && y.kind === 'line') {
    const l1 = lineById(s, x.id)!;
    const l2 = lineById(s, y.id)!;
    const r = lineRelation(l1.a, sub(l1.b, l1.a), l2.a, sub(l2.b, l2.a));
    const v = r.kind === 'skew' || r.kind === 'parallel' ? r.distance : 0;
    const e1 = lineEndsRef(s, l1);
    const e2 = lineEndsRef(s, l2);
    return finishMeasure(s, 'length', { kind: 'dll', l1: e1, l2: e2 }, `d(${e1.join('')}, ${e2.join('')})`, v, { kind: 'none' });
  }
  if (x.kind === 'face' && y.kind === 'face') {
    const f1 = faceById(s, x.id)!;
    const f2 = faceById(s, y.id)!;
    const p1 = planeOfFace(f1)!;
    const p2 = planeOfFace(f2)!;
    const parallel = Math.abs(Math.abs(dot(p1.n, p2.n)) - 1) < 1e-9;
    const v = parallel ? Math.abs(signedDist(p2, centroid(f1.poly))) : 0;
    return finishMeasure(s, 'length', { kind: 'dff', f1: faceRefNames(f1), f2: faceRefNames(f2) }, `d((${shortFace(f1)}), (${shortFace(f2)}))`, v, {
      kind: 'none',
    });
  }
  // đường – mặt: khoảng cách khi song song, 0 khi cắt
  const l = lineById(s, x.id)!;
  const f = faceById(s, y.id)!;
  const pl = planeOfFace(f)!;
  const par = Math.abs(dot(unit(sub(l.b, l.a)), pl.n)) < 1e-9;
  const v = par ? Math.abs(signedDist(pl, l.a)) : 0;
  const le = lineEndsRef(s, l);
  return finishMeasure(s, 'length', { kind: 'dpp', p: le[0], plane: faceRefNames(f) }, `d(${le.join('')}, (${shortFace(f)}))`, v, { kind: 'none' });
}

/** "Đo góc": ba điểm, điểm giữa là đỉnh góc. */
export function angleTap(s: FigureState, t: Target | null): Outcome {
  if (!t || t.kind !== 'point') return { state: { ...s, pending: [] }, notice: t ? { kind: 'toast', text: VIEWER_TEXT.angleNeedsPoints } : undefined };
  const last = s.pending[s.pending.length - 1];
  if (last && last.id === t.id) return { state: s };
  const pend = [...s.pending, t];
  if (pend.length < 3) return { state: { ...s, pending: pend } };
  const [a, o, b] = pend.map((p) => p.id) as [string, string, string];
  const pa = pointPos(s, a)!;
  const po = pointPos(s, o)!;
  const pb = pointPos(s, b)!;
  if (samePoint(pa, po) || samePoint(pb, po)) return { state: { ...s, pending: [] } };
  return finishMeasure(s, 'angle', { kind: 'ang', a, o, b }, `góc ${a}${o}${b}`, angleAt(pa, po, pb), { kind: 'angle', a: pa, o: po, b: pb });
}

/** Một lần chạm theo công cụ đang bật. */
export function applyTap(s: FigureState, tool: ToolId, t: Target | null): Outcome {
  switch (tool) {
    case 'select':
      return select(s, t);
    case 'line':
      return lineTap(s, t);
    case 'plane':
      return planeTap(s, t);
    case 'extend':
      return extendTap(s, t);
    case 'length':
      return lengthTap(s, t);
    case 'angle':
      return angleTap(s, t);
    default:
      return { state: s };
  }
}
