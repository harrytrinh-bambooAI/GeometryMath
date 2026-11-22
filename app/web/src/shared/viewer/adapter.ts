import { dist, sub, type Vec3 } from './geometry.ts';
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

export function parseHidden(list: readonly string[] | undefined): MeasureRef[] {
  return (list ?? []).flatMap((s) => {
    const m = parseMeasure(s);
    return m ? [m] : [];
  });
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
