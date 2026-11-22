import { add, dot, norm, scale, sub, unit, type Vec3 } from './geometry.ts';
import type { RenderModel } from './render.ts';

/**
 * LỚP VẼ CỦA LỜI GIẢI (E4, US-GEO-407, 408) — thêm vào bộ xem dùng chung, không đổi lõi: góc được tô (`highlight.angles`)
 * và phần vẽ thêm của bước (`overlays`). Mọi hình vẽ dựng trong 3D rồi chiếu, nên xoay hình vẫn đúng chỗ.
 * - segment {a, b}: đường phụ nổi bật (nét đỏ cam đậm).
 * - right {at, a, b}: ký hiệu góc vuông tại `at` giữa hai tia at→a, at→b.
 * - arc {at, a, b, label?}: cung góc tại `at` giữa hai tia, kèm số đo (ví dụ "45°").
 * Góc tô `angles: ["SDA"]` = góc tại đỉnh giữa (D) giữa hai tia D→S, D→A.
 */
export type ViewerOverlay =
  | { kind: 'segment'; a: string; b: string }
  | { kind: 'right'; at: string; a: string; b: string }
  | { kind: 'arc'; at: string; a: string; b: string; label?: string };

type Named = Map<string, Vec3>;

function named(rm: RenderModel): Named {
  return new Map(rm.points.map((p) => [p.name, p.pos as Vec3]));
}

/** Đỉnh và hai tia của góc "SDA" (đỉnh giữa) hoặc của overlay {at, a, b}. */
function rays(pts: Named, at: string, a: string, b: string) {
  const o = pts.get(at);
  const pa = pts.get(a);
  const pb = pts.get(b);
  if (!o || !pa || !pb) return null;
  const da = sub(pa, o);
  const db = sub(pb, o);
  if (norm(da) < 1e-9 || norm(db) < 1e-9) return null;
  const u = unit(da);
  // w: thành phần của db vuông góc với u (mặt phẳng của góc)
  const perp = sub(db, scale(u, dot(db, u)));
  if (norm(perp) < 1e-9) return null;
  const w = unit(perp);
  const angle = Math.acos(Math.max(-1, Math.min(1, dot(u, unit(db)))));
  const size = Math.min(norm(da), norm(db)) * 0.22;
  return { o, u, w, v: unit(db), angle, size };
}

function arcPoints(r: NonNullable<ReturnType<typeof rays>>, radius: number, steps = 16): Vec3[] {
  const out: Vec3[] = [];
  for (let i = 0; i <= steps; i++) {
    const t = (r.angle * i) / steps;
    out.push(add(r.o, add(scale(r.u, radius * Math.cos(t)), scale(r.w, radius * Math.sin(t)))));
  }
  return out;
}

export interface OverlayShape {
  kind: 'angle' | 'segment' | 'right' | 'arc';
  id: string;
  label?: string;
  /** Điểm 3D đã dựng (đường gấp khúc). */
  path: Vec3[];
  /** Vị trí nhãn (3D), nếu có. */
  labelAt?: Vec3;
}

/** Dựng các hình của lớp vẽ lời giải (thuần, dùng chung cho SVG và móc kiểm thử). */
export function overlayShapes(rm: RenderModel, angles: string[] = [], overlays: ViewerOverlay[] = []): OverlayShape[] {
  const pts = named(rm);
  const out: OverlayShape[] = [];
  for (const g of angles) {
    // tên đỉnh có thể có dấu phẩy trên (A′): tách theo đỉnh của hình
    const names = splitNames(g, pts);
    if (!names) continue;
    const [na, at, nb] = names as [string, string, string];
    const r = rays(pts, at, na, nb);
    if (!r) continue;
    out.push({ kind: 'angle', id: `angle-${g}`, label: `góc ${g}`, path: [r.o, ...arcPoints(r, r.size * 1.25), r.o] });
  }
  overlays.forEach((o, i) => {
    if (o.kind === 'segment') {
      const a = pts.get(o.a);
      const b = pts.get(o.b);
      if (a && b) out.push({ kind: 'segment', id: `segment-${o.a}${o.b}`, path: [a, b] });
      return;
    }
    const r = rays(pts, o.at, o.a, o.b);
    if (!r) return;
    if (o.kind === 'right') {
      const s = r.size * 0.7;
      const p1 = add(r.o, scale(r.u, s));
      const p3 = add(r.o, scale(r.v, s));
      out.push({ kind: 'right', id: `right-${o.at}`, path: [p1, add(p1, scale(r.v, s)), p3] });
      return;
    }
    const ap = arcPoints(r, r.size);
    out.push({
      kind: 'arc',
      id: `arc-${o.at}-${i}`,
      label: o.label,
      path: ap,
      labelAt: add(r.o, scale(unit(add(r.u, r.w)), r.size * 1.9)),
    });
  });
  return out;
}

/** Tách "SDA" / "A′BC" thành tên đỉnh có trong hình (tham lam: tên dài trước). */
function splitNames(g: string, pts: Named): string[] | null {
  const names = [...pts.keys()].sort((a, b) => b.length - a.length);
  const out: string[] = [];
  let i = 0;
  while (i < g.length) {
    const hit = names.find((n) => g.startsWith(n, i));
    if (!hit) return null;
    out.push(hit);
    i += hit.length;
  }
  return out.length === 3 ? out : null;
}

/** Phần vẽ của lời giải trên SVG của bộ xem. */
export function SolutionOverlay({
  rm,
  shapes,
  T,
}: {
  rm: RenderModel;
  shapes: OverlayShape[];
  T: (...p: Array<string | number>) => string;
}) {
  const pr = (v: Vec3) => rm.proj.project(v);
  return (
    <g className="gv-solution" data-testid={T('solution')}>
      {shapes.map((s) => {
        const d = s.path.map((v, i) => {
          const q = pr(v);
          return `${i === 0 ? 'M' : 'L'}${q.x.toFixed(1)},${q.y.toFixed(1)}`;
        });
        const lab = s.labelAt ? pr(s.labelAt) : null;
        return (
          <g key={s.id} className={`gv-sol gv-sol-${s.kind}`} data-testid={T('sol', s.id)}>
            <path d={d.join(' ')} />
            {lab && s.label && (
              <text x={lab.x} y={lab.y} data-testid={T('sol', s.id, 'label')}>
                {s.label}
              </text>
            )}
          </g>
        );
      })}
    </g>
  );
}

/** Phần lời giải trong móc kiểm thử của bộ xem, khóa "solution" (tọa độ màn hình px trang). */
export function overlaySnapshot(
  rm: RenderModel,
  shapes: OverlayShape[],
  highlight: { points?: string[]; edges?: string[]; faces?: string[]; angles?: string[] } | undefined,
  origin: { left: number; top: number },
) {
  const r2 = (n: number) => Math.round(n * 100) / 100;
  return {
    highlight: {
      points: [...(highlight?.points ?? [])],
      edges: [...(highlight?.edges ?? [])],
      faces: [...(highlight?.faces ?? [])],
      angles: [...(highlight?.angles ?? [])],
    },
    shapes: shapes.map((s) => ({
      kind: s.kind,
      id: s.id,
      label: s.label ?? null,
      screen: s.path.map((v) => {
        const q = rm.proj.project(v);
        return [r2(q.x + origin.left), r2(q.y + origin.top)];
      }),
    })),
  };
}
