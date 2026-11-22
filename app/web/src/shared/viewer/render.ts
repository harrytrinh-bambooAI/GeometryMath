import { makeProjector, type Projector, type ViewState } from './camera.ts';
import { add, cross, polygonArea, scale, triangulate, unit, type Vec3 } from './geometry.ts';
import { allFaces, allLines, allPoints, type FaceKind, type FigureState, type LineKind } from './model.ts';
import { hiddenRatio, occludersOf, segmentRuns, styleOf, type LineStyle, type Run } from './visibility.ts';

/**
 * Mô hình vẽ: chiếu mọi đối tượng lên màn hình và tính nét đứt. Thuần (không DOM) nên dùng chung cho SVG, cho chọn
 * bằng chạm và cho móc kiểm thử `window.__geoScene`.
 */
export interface P2 {
  x: number;
  y: number;
}
export interface RPoint extends P2 {
  name: string;
  pos: Vec3;
  user: boolean;
  depth: number;
}
export interface RLine {
  id: string;
  label: string;
  kind: LineKind;
  ends: [string, string];
  a: P2;
  b: P2;
  a3: Vec3;
  b3: Vec3;
  runs: Run[];
  style: LineStyle;
  hiddenRatio: number;
}
export interface RFace {
  id: string;
  label: string;
  kind: FaceKind;
  pts: P2[];
  tris: Array<[number, number, number]>;
  depth: number;
  area: number;
}
export interface RTrace {
  id: string;
  plane: string;
  face: string;
  a: P2;
  b: P2;
  runs: Run[];
}
export interface RenderModel {
  proj: Projector;
  points: RPoint[];
  lines: RLine[];
  faces: RFace[];
  traces: RTrace[];
  circles: Array<{ id: string; pts: P2[] }>;
  axes: Array<{ axis: 'x' | 'y' | 'z'; from: P2; to: P2 }>;
}

export function buildRender(s: FigureState, view: ViewState, w: number, h: number): RenderModel {
  const proj = makeProjector(view, w, h, s.base.center, s.base.radius);
  const pr = (p: Vec3): P2 => {
    const q = proj.project(p);
    return { x: q.x, y: q.y };
  };
  const occ = occludersOf(s.base.faces.map((f) => f.poly));
  const ref = s.base.radius;
  const points = allPoints(s).map((p) => {
    const q = proj.project(p.pos);
    return { name: p.name, pos: p.pos, user: p.user, x: q.x, y: q.y, depth: q.depth };
  });
  const lines = allLines(s).map((l): RLine => {
    const runs = segmentRuns(l.a, l.b, proj.eye, occ, ref);
    return {
      id: l.id,
      label: l.label,
      kind: l.kind,
      ends: l.ends,
      a: pr(l.a),
      b: pr(l.b),
      a3: l.a,
      b3: l.b,
      runs,
      style: styleOf(runs),
      hiddenRatio: hiddenRatio(runs),
    };
  });
  const faces = allFaces(s).map((f): RFace => {
    const pts = f.poly.map(pr);
    const depth = f.poly.reduce((acc, p) => acc + proj.project(p).depth, 0) / f.poly.length;
    return { id: f.id, label: f.label, kind: f.kind, pts, tris: triangulate(f.poly), depth, area: polygonArea(f.poly) };
  });
  const traces = s.planes.flatMap((p) =>
    p.traces.map((t) => ({ id: t.id, plane: t.plane, face: t.face, a: pr(t.a), b: pr(t.b), runs: segmentRuns(t.a, t.b, proj.eye, occ, ref) })),
  );
  const circles = s.base.circles.map((c) => {
    const n = unit(c.normal);
    const u = unit(Math.abs(n[0]) < 0.9 ? cross(n, [1, 0, 0]) : cross(n, [0, 1, 0]));
    const v = cross(n, u);
    const pts: P2[] = [];
    for (let i = 0; i < 64; i++) {
      const t = (i / 64) * Math.PI * 2;
      pts.push(pr(add(c.center, add(scale(u, c.radius * Math.cos(t)), scale(v, c.radius * Math.sin(t))))));
    }
    return { id: c.id, pts };
  });
  const L = s.base.radius * 1.4;
  const o: Vec3 = [0, 0, 0];
  const axes = s.axes
    ? (['x', 'y', 'z'] as const).map((axis, i) => {
        const d: Vec3 = [i === 0 ? L : 0, i === 1 ? L : 0, i === 2 ? L : 0];
        return { axis, from: pr(o), to: pr(d) };
      })
    : [];
  return { proj, points, lines, faces, traces, circles, axes };
}

/** Điểm trên đoạn [a,b] (2D) gần (x,y) nhất: tham số t và khoảng cách. */
export function nearestOnSegment(a: P2, b: P2, x: number, y: number): { t: number; d: number } {
  const dx = b.x - a.x;
  const dy = b.y - a.y;
  const l2 = dx * dx + dy * dy || 1e-12;
  const t = Math.max(0, Math.min(1, ((x - a.x) * dx + (y - a.y) * dy) / l2));
  return { t, d: Math.hypot(a.x + t * dx - x, a.y + t * dy - y) };
}
export function inPoly(pts: P2[], x: number, y: number): boolean {
  let inside = false;
  for (let i = 0, j = pts.length - 1; i < pts.length; j = i++) {
    const a = pts[i]!;
    const b = pts[j]!;
    if (a.y > y !== b.y > y && x < ((b.x - a.x) * (y - a.y)) / (b.y - a.y) + a.x) inside = !inside;
  }
  return inside;
}
