import {
  cross,
  dot,
  norm,
  planeFrom3,
  polygonPlane,
  samePlane,
  samePoint,
  sub,
  unit,
  type Plane,
  type Vec3,
} from './geometry.ts';
import type { MeasureRef } from './types.ts';

/**
 * Đại lượng ẩn (BR-GEO-16, 155): đọc mô tả chữ của máy chủ ("góc SDA", "d(AC, B′D′)", "d(S, (ABCD))",
 * "d((ABCD), (A′B′C′D′))", "SD") thành MeasureRef, và so một kết quả đo với danh sách ẩn THEO HÌNH HỌC
 * (cùng đỉnh góc và cùng hai tia; cùng đường thẳng; cùng mặt phẳng), không theo chuỗi tên.
 */
const NAME = /[A-Z][0-9]*[′']*/g;
const names = (s: string): string[] => (s.match(NAME) ?? []).map((n) => n.replace(/'/g, '′'));

function splitArgs(s: string): string[] {
  const out: string[] = [];
  let depth = 0;
  let cur = '';
  for (const ch of s) {
    if (ch === '(') depth++;
    if (ch === ')') depth--;
    if (ch === ',' && depth === 0) {
      out.push(cur.trim());
      cur = '';
    } else cur += ch;
  }
  if (cur.trim()) out.push(cur.trim());
  return out;
}

type Arg = { k: 'pt'; n: string } | { k: 'ln'; l: [string, string] } | { k: 'pl'; f: string[] };
function arg(s: string): Arg | null {
  const isPlane = /^\(.*\)$/.test(s.trim());
  const ns = names(s);
  if (isPlane && ns.length >= 3) return { k: 'pl', f: ns };
  if (ns.length === 1) return { k: 'pt', n: ns[0]! };
  if (ns.length === 2) return { k: 'ln', l: [ns[0]!, ns[1]!] };
  if (ns.length >= 3) return { k: 'pl', f: ns };
  return null;
}

export function parseMeasure(text: string): MeasureRef | null {
  const s = text.trim();
  const lower = s.toLowerCase();
  if (lower.startsWith('góc') || s.startsWith('∠')) {
    const ns = names(s);
    return ns.length === 3 ? { kind: 'ang', a: ns[0]!, o: ns[1]!, b: ns[2]! } : null;
  }
  const m = /^(?:d|khoảng cách)\s*\((.*)\)\s*$/i.exec(s) ?? /^khoảng cách\s+(.*)$/i.exec(s);
  if (m) {
    const parts = splitArgs(m[1]!);
    if (parts.length !== 2) return null;
    const x = arg(parts[0]!);
    const y = arg(parts[1]!);
    if (!x || !y) return null;
    if (x.k === 'pt' && y.k === 'pt') return { kind: 'len', a: x.n, b: y.n };
    if (x.k === 'pt' && y.k === 'pl') return { kind: 'dpp', p: x.n, plane: y.f };
    if (x.k === 'pl' && y.k === 'pt') return { kind: 'dpp', p: y.n, plane: x.f };
    if (x.k === 'pt' && y.k === 'ln') return { kind: 'dpl', p: x.n, line: y.l };
    if (x.k === 'ln' && y.k === 'pt') return { kind: 'dpl', p: y.n, line: x.l };
    if (x.k === 'ln' && y.k === 'ln') return { kind: 'dll', l1: x.l, l2: y.l };
    if (x.k === 'pl' && y.k === 'pl') return { kind: 'dff', f1: x.f, f2: y.f };
    return null;
  }
  const ns = names(s.replace(/^độ dài/i, ''));
  return ns.length === 2 ? { kind: 'len', a: ns[0]!, b: ns[1]! } : null;
}

/** Dạng hình học của một đại lượng để so khớp. */
export type GeoMeasure =
  | { kind: 'len'; a: Vec3; b: Vec3 }
  | { kind: 'ang'; o: Vec3; u: Vec3; v: Vec3 }
  | { kind: 'dpp'; p: Vec3; plane: Plane }
  | { kind: 'dpl'; p: Vec3; line: Line }
  | { kind: 'dll'; l1: Line; l2: Line }
  | { kind: 'dff'; f1: Plane; f2: Plane };
export interface Line {
  p: Vec3;
  d: Vec3;
}

export function resolveMeasure(ref: MeasureRef, pt: (n: string) => Vec3 | undefined): GeoMeasure | null {
  const L = (l: [string, string]): Line | null => {
    const a = pt(l[0]);
    const b = pt(l[1]);
    return a && b && !samePoint(a, b) ? { p: a, d: unit(sub(b, a)) } : null;
  };
  const P = (f: string[]): Plane | null => {
    const ps = f.map(pt);
    if (ps.some((x) => !x)) return null;
    const vs = ps as Vec3[];
    return vs.length === 3 ? planeFrom3(vs[0]!, vs[1]!, vs[2]!) : polygonPlane(vs);
  };
  switch (ref.kind) {
    case 'len': {
      const a = pt(ref.a);
      const b = pt(ref.b);
      return a && b ? { kind: 'len', a, b } : null;
    }
    case 'ang': {
      const a = pt(ref.a);
      const o = pt(ref.o);
      const b = pt(ref.b);
      return a && o && b ? { kind: 'ang', o, u: unit(sub(a, o)), v: unit(sub(b, o)) } : null;
    }
    case 'dpp': {
      const p = pt(ref.p);
      const plane = P(ref.plane);
      return p && plane ? { kind: 'dpp', p, plane } : null;
    }
    case 'dpl': {
      const p = pt(ref.p);
      const line = L(ref.line);
      return p && line ? { kind: 'dpl', p, line } : null;
    }
    case 'dll': {
      const l1 = L(ref.l1);
      const l2 = L(ref.l2);
      return l1 && l2 ? { kind: 'dll', l1, l2 } : null;
    }
    case 'dff': {
      const f1 = P(ref.f1);
      const f2 = P(ref.f2);
      return f1 && f2 ? { kind: 'dff', f1, f2 } : null;
    }
  }
}

const sameDir = (u: Vec3, v: Vec3) => dot(u, v) > 1 - 1e-9;
const sameLine = (x: Line, y: Line) =>
  norm(cross(x.d, y.d)) < 1e-9 && norm(cross(sub(y.p, x.p), x.d)) < 1e-7;
const pair = <T>(x1: T, x2: T, y1: T, y2: T, eq: (a: T, b: T) => boolean) =>
  (eq(x1, y1) && eq(x2, y2)) || (eq(x1, y2) && eq(x2, y1));

export function sameMeasure(x: GeoMeasure, y: GeoMeasure): boolean {
  if (x.kind !== y.kind) return false;
  switch (x.kind) {
    case 'len': {
      const o = y as typeof x;
      return pair(x.a, x.b, o.a, o.b, (a, b) => samePoint(a, b));
    }
    case 'ang': {
      const o = y as typeof x;
      return samePoint(x.o, o.o) && pair(x.u, x.v, o.u, o.v, sameDir);
    }
    case 'dpp': {
      const o = y as typeof x;
      return samePoint(x.p, o.p) && samePlane(x.plane, o.plane);
    }
    case 'dpl': {
      const o = y as typeof x;
      return samePoint(x.p, o.p) && sameLine(x.line, o.line);
    }
    case 'dll': {
      const o = y as typeof x;
      return pair(x.l1, x.l2, o.l1, o.l2, sameLine);
    }
    case 'dff': {
      const o = y as typeof x;
      return pair(x.f1, x.f2, o.f1, o.f2, (a, b) => samePlane(a, b));
    }
  }
}
