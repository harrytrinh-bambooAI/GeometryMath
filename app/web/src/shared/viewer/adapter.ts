import type { Scene } from '../scene/types.ts';
import { add, dist, scale, sub, unit as unitVec, type Vec3 } from './geometry.ts';
import { parseMeasure } from './measures.ts';
import type { MeasureRef, ViewerSolid } from './types.ts';

/**
 * LỚP CHUYỂN ĐỔI dữ liệu máy chủ → `ViewerSolid`. Bộ xem chỉ biết `ViewerSolid`; đổi hợp đồng dữ liệu cảnh chỉ sửa tệp này.
 * `fromCoords` dựng đỉnh/cạnh/mặt từ loại hình + bảng tọa độ (dạng `scene_build.coords`), dùng khi dữ liệu cảnh chỉ có
 * tọa độ. Thứ tự đỉnh trong JSON không quan trọng (jsonb sắp lại khóa).
 */
export type SolidKind = 'pyramid' | 'cube' | 'prism' | 'cone' | 'cylinder' | 'sphere' | string;
export type Coords = Record<string, readonly number[]>;

const P = (c: Coords, n: string): Vec3 => {
  const v = c[n]!;
  return [Number(v[0]), Number(v[1]), Number(v[2])];
};
const primed = (n: string) => /′$/.test(n);
const basePart = (n: string) => n.replace(/′+$/, '');

/** Sắp đỉnh đáy theo vòng quanh trọng tâm (đáy nằm trong mặt phẳng z hằng của hình mẫu). */
function ring(c: Coords, ns: string[]): string[] {
  if (ns.length <= 3) return [...ns].sort();
  const cx = ns.reduce((s, n) => s + P(c, n)[0], 0) / ns.length;
  const cy = ns.reduce((s, n) => s + P(c, n)[1], 0) / ns.length;
  return [...ns].sort((a, b) => Math.atan2(P(c, a)[1] - cy, P(c, a)[0] - cx) - Math.atan2(P(c, b)[1] - cy, P(c, b)[0] - cx));
}

export function topologyFor(kind: SolidKind, coords: Coords): Pick<ViewerSolid, 'edges' | 'faces' | 'circles'> {
  const names = Object.keys(coords);
  const edges: Array<[string, string]> = [];
  const faces: string[][] = [];
  const cyc = (r: string[]) => r.forEach((n, i) => edges.push([n, r[(i + 1) % r.length]!]));
  if (kind === 'pyramid') {
    const apex = names.includes('S') ? 'S' : names[names.length - 1]!;
    const base = ring(coords, names.filter((n) => n !== apex));
    cyc(base);
    base.forEach((n) => edges.push([apex, n]));
    faces.push(base);
    base.forEach((n, i) => faces.push([apex, n, base[(i + 1) % base.length]!]));
    return { edges, faces };
  }
  if (kind === 'cube' || kind === 'prism' || kind === 'sphere') {
    const bottom = ring(coords, names.filter((n) => !primed(n) && names.includes(`${n}′`)));
    const top = bottom.map((n) => `${n}′`);
    cyc(bottom);
    cyc(top);
    bottom.forEach((n, i) => edges.push([n, top[i]!]));
    faces.push(bottom, top);
    bottom.forEach((n, i) => {
      const j = (i + 1) % bottom.length;
      faces.push([n, bottom[j]!, top[j]!, top[i]!]);
    });
    return { edges, faces };
  }
  if (kind === 'cone') {
    const o = P(coords, 'O');
    const s = P(coords, 'S');
    const m = P(coords, 'M');
    return {
      edges: [['S', 'M'], ['S', 'O'], ['O', 'M']],
      faces: [],
      circles: [{ id: 'đáy', center: o, radius: dist(o, m), normal: sub(s, o) }],
    };
  }
  if (kind === 'cylinder') {
    const top = names.find((n) => primed(n) && basePart(n) === 'O') ?? 'O′';
    const o = P(coords, 'O');
    const o2 = P(coords, top);
    const m = P(coords, 'M');
    return {
      edges: [['M', 'M′'], ['O', 'M'], [top, 'M′'], ['O', top]],
      faces: [],
      circles: [
        { id: 'đáy dưới', center: o, radius: dist(o, m), normal: sub(o2, o) },
        { id: 'đáy trên', center: o2, radius: dist(o, m), normal: sub(o2, o) },
      ],
    };
  }
  return { edges, faces };
}

/**
 * Đại lượng ẩn theo hợp đồng cảnh v1 (`ang:S-D-A`, `len:O-O′`) hoặc dạng chữ (`góc SDA`, `d(AC, B′D′)`).
 * Mở rộng của E3 cùng cú pháp: `dll:A-C|B′-D′`, `dpp:S|A-B-C-D`, `dff:A-B-C-D|A′-B′-C′-D′`, `dpl:S|A-B`.
 */
export function parseHiddenOne(s: string): MeasureRef | null {
  const m = /^(ang|len|dll|dpp|dff|dpl):(.+)$/.exec(s.trim());
  if (!m) return parseMeasure(s);
  const parts = m[2]!.split('|').map((x) => x.split('-').map((n) => n.trim()).filter(Boolean));
  const [x = [], y = []] = parts;
  switch (m[1]) {
    case 'ang':
      return x.length === 3 ? { kind: 'ang', a: x[0]!, o: x[1]!, b: x[2]! } : null;
    case 'len':
      return x.length === 2 ? { kind: 'len', a: x[0]!, b: x[1]! } : null;
    case 'dll':
      return x.length === 2 && y.length === 2 ? { kind: 'dll', l1: [x[0]!, x[1]!], l2: [y[0]!, y[1]!] } : null;
    case 'dpp':
      return x.length === 1 && y.length >= 3 ? { kind: 'dpp', p: x[0]!, plane: y } : null;
    case 'dpl':
      return x.length === 1 && y.length === 2 ? { kind: 'dpl', p: x[0]!, line: [y[0]!, y[1]!] } : null;
    case 'dff':
      return x.length >= 3 && y.length >= 3 ? { kind: 'dff', f1: x, f2: y } : null;
  }
  return null;
}
export function parseHidden(list: readonly string[] | undefined): MeasureRef[] {
  return (list ?? []).flatMap((s) => {
    const m = parseHiddenOne(s);
    return m ? [m] : [];
  });
}

/** Hợp đồng cảnh v1 của E2 (`app/docs/SCENE-CONTRACT.md`) → đầu vào bộ xem. */
export function fromScene(scene: Scene): ViewerSolid {
  const points = scene.points.map((p) => ({ name: p.name, pos: [p.x, p.y, p.z] as Vec3 }));
  const circles: NonNullable<ViewerSolid['circles']> = [];
  for (const [i, sf] of scene.surfaces.entries()) {
    const axis = unitVec(sf.axis ?? [0, 0, 1]);
    if (sf.kind === 'cone') circles.push({ id: `đáy-${i}`, center: sf.center, radius: sf.radius, normal: axis });
    if (sf.kind === 'cylinder') {
      circles.push({ id: `đáy-dưới-${i}`, center: sf.center, radius: sf.radius, normal: axis });
      circles.push({ id: `đáy-trên-${i}`, center: add(sf.center, scale(axis, sf.height ?? 0)), radius: sf.radius, normal: axis });
    }
    if (sf.kind === 'sphere') {
      circles.push({ id: `xích-đạo-${i}`, center: sf.center, radius: sf.radius, normal: [0, 0, 1] });
      circles.push({ id: `kinh-tuyến-${i}`, center: sf.center, radius: sf.radius, normal: [0, 1, 0] });
    }
  }
  const a = scene.params.a;
  return {
    points,
    edges: scene.edges.map(([x, y]) => [x, y] as [string, string]),
    faces: scene.faces.map((f) => [...f]),
    circles,
    unit: scene.unit === 'a' && typeof a === 'number' && a > 0 ? { symbol: 'a', value: a } : null,
    unitSuffix: scene.unit === 'cm' ? 'cm' : '',
    hiddenMeasures: parseHidden(scene.hiddenMeasures),
    chips: scene.chips.map((c) => c.text),
  };
}

export function fromCoords(
  kind: SolidKind,
  coords: Coords,
  opts: { params?: Record<string, number>; hiddenMeasures?: readonly string[] } = {},
): ViewerSolid {
  const points = Object.keys(coords).map((name) => ({ name, pos: P(coords, name) }));
  const a = opts.params?.a;
  return {
    points,
    ...topologyFor(kind, coords),
    unit: typeof a === 'number' && a > 0 ? { symbol: 'a', value: a } : null,
    hiddenMeasures: parseHidden(opts.hiddenMeasures),
  };
}
