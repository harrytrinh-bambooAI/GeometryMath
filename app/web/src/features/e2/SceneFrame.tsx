import { useMemo, useRef, useState } from 'react';
import type { PointerEvent as RPointerEvent } from 'react';
import type { Scene, ScenePoint } from '../../shared/scene/types.ts';
import { tid } from '../../shared/testid.ts';
import { fmtNum, lengthHidden, lengthOf, project } from './sceneMath.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { verifyIcon } from '../../shared/ui/icons.ts';

/**
 * Khung hình TẠM của E2 (SVG, chiếu song song): đủ cho tiêu chí E2 — hình hiện, xoay được (kéo hoặc nút),
 * thẻ ràng buộc ở góc dưới trái (BR-GEO-130), nhãn máy, cảnh báo MSG-GEO-16 cố định không có nút đóng (BR-GEO-137),
 * công cụ Đo dài tối giản. Bộ xem 3D tương tác thật là việc của E3 (shared/viewer); E3 thay khung này khi xong.
 */
const W = 400;
const H = 320;
const PAD = 36;

type Pt3 = { x: number; y: number; z: number };

function circle(center: number[], r: number, n = 48): Pt3[] {
  const out: Pt3[] = [];
  for (let i = 0; i <= n; i++) {
    const t = (i / n) * Math.PI * 2;
    out.push({ x: center[0]! + r * Math.cos(t), y: center[1]! + r * Math.sin(t), z: center[2]! });
  }
  return out;
}

export function SceneFrame({ scene, testId = tid('e2', 'scene') }: { scene: Scene; testId?: string }) {
  const [view, setView] = useState({ yaw: 25, pitch: 20 });
  const drag = useRef<{ x: number; y: number; yaw: number; pitch: number } | null>(null);
  const [mp, setMp] = useState('');
  const [mq, setMq] = useState('');

  const geo = useMemo(() => {
    const curves: Pt3[][] = [];
    for (const s of scene.surfaces) {
      if (s.kind === 'cone') curves.push(circle(s.center, s.radius));
      if (s.kind === 'cylinder') {
        curves.push(circle(s.center, s.radius));
        const h = s.height ?? 0;
        curves.push(circle([s.center[0], s.center[1], s.center[2] + h], s.radius));
      }
      if (s.kind === 'sphere') {
        curves.push(circle(s.center, s.radius));
        curves.push(
          circle([0, 0, 0], s.radius).map((p) => ({ x: s.center[0] + p.x, y: s.center[1], z: s.center[2] + p.y })),
        );
        curves.push(
          circle([0, 0, 0], s.radius).map((p) => ({ x: s.center[0], y: s.center[1] + p.x, z: s.center[2] + p.y })),
        );
      }
    }
    const all = [...scene.points, ...curves.flat()];
    const pr = (p: Pt3) => project(p as ScenePoint, view.yaw, view.pitch);
    const proj = all.map(pr);
    const xs = proj.map((p) => p[0]);
    const ys = proj.map((p) => p[1]);
    const minX = Math.min(...xs);
    const maxX = Math.max(...xs);
    const minY = Math.min(...ys);
    const maxY = Math.max(...ys);
    const k = Math.min((W - 2 * PAD) / Math.max(maxX - minX, 1e-9), (H - 2 * PAD) / Math.max(maxY - minY, 1e-9));
    const ox = (W - k * (maxX - minX)) / 2 - k * minX;
    const oy = (H - k * (maxY - minY)) / 2 - k * minY;
    const to = (p: Pt3): [number, number] => {
      const q = pr(p);
      return [ox + k * q[0], oy + k * q[1]];
    };
    const at = new Map(scene.points.map((p) => [p.name, to(p)]));
    return { at, curves: curves.map((c) => c.map(to)) };
  }, [scene, view]);

  const onDown = (e: RPointerEvent<SVGSVGElement>) => {
    drag.current = { x: e.clientX, y: e.clientY, ...view };
    e.currentTarget.setPointerCapture?.(e.pointerId);
  };
  const onMove = (e: RPointerEvent<SVGSVGElement>) => {
    const d = drag.current;
    if (!d) return;
    setView({
      yaw: d.yaw + (e.clientX - d.x) * 0.5,
      pitch: Math.max(-89, Math.min(89, d.pitch + (e.clientY - d.y) * 0.5)),
    });
  };
  const onUp = () => {
    drag.current = null;
  };

  const warning = scene.verification.warning;
  const names = scene.points.map((p) => p.name);
  const measured =
    mp && mq && mp !== mq
      ? lengthHidden(scene, mp, mq)
        ? `${mp}${mq}: ẩn tới khi mở lời giải`
        : `${mp}${mq} = ${fmtNum(lengthOf(scene, mp, mq))}${scene.unit === 'cm' ? ' cm' : ''}`
      : '';

  return (
    <div
      className="e2-frame"
      data-testid={testId}
      data-coord-source={scene.coordSource}
      data-template={scene.template ?? ''}
      data-points={scene.points.length}
      data-yaw={Math.round(view.yaw)}
      data-pitch={Math.round(view.pitch)}
    >
      <div className="e2-frame-top">
        <span
          className={`chip ${scene.verification.status === 'verified' ? 'grn' : 'yel'}`}
          data-testid={tid('e2', 'scene', 'label')}
          data-status={scene.verification.status}
        ><Icon name={verifyIcon(scene.verification.status === 'verified')} />
          {scene.verification.label}
        </span>
        <span className="e2-rotate">
          <button type="button" className="btn ghost" aria-label="Xoay trái" data-testid={tid('e2', 'scene', 'rotate', 'left')}
            onClick={() => setView((v) => ({ ...v, yaw: v.yaw - 15 }))}>
            <Icon name="rotateLeft" />
          </button>
          <button type="button" className="btn ghost" aria-label="Xoay phải" data-testid={tid('e2', 'scene', 'rotate', 'right')}
            onClick={() => setView((v) => ({ ...v, yaw: v.yaw + 15 }))}>
            <Icon name="rotateRight" />
          </button>
        </span>
      </div>
      {warning ? (
        <div className="e2-warning" role="note" data-testid={tid('e2', 'scene', 'warning')} data-code={warning.code}><Icon name="warn" inline />
          {warning.text}
        </div>
      ) : null}
      <svg
        viewBox={`0 0 ${W} ${H}`}
        className="e2-svg"
        role="img"
        aria-label="Hình vẽ"
        onPointerDown={onDown}
        onPointerMove={onMove}
        onPointerUp={onUp}
        onPointerCancel={onUp}
        data-testid={tid('e2', 'scene', 'svg')}
      >
        {scene.faces.map((f) => {
          const pts = f.map((n) => geo.at.get(n)).filter((p): p is [number, number] => !!p);
          return <polygon key={f.join('')} points={pts.map((p) => p.join(',')).join(' ')} className="e2-face" />;
        })}
        {geo.curves.map((c, i) => (
          <polyline key={`c${i}`} points={c.map((p) => p.join(',')).join(' ')} className="e2-curve" />
        ))}
        {scene.edges.map(([a, b]) => {
          const p = geo.at.get(a);
          const q = geo.at.get(b);
          if (!p || !q) return null;
          return <line key={`${a}-${b}`} x1={p[0]} y1={p[1]} x2={q[0]} y2={q[1]} className="e2-edge" />;
        })}
        {scene.points.map((p) => {
          const s = geo.at.get(p.name)!;
          return (
            <g key={p.name}>
              <circle cx={s[0]} cy={s[1]} r={3} className="e2-vertex" />
              <text x={s[0] + 6} y={s[1] - 6} className="e2-vlabel">
                {p.name}
              </text>
            </g>
          );
        })}
      </svg>
      {scene.chips.length ? (
        <div className="e2-chips" data-testid={tid('e2', 'scene', 'chips')}>
          {scene.chips.map((c) => (
            <span key={c.text} className="chip" data-testid={tid('e2', 'scene', 'chip', c.points.join(''))}>
              {c.text}
            </span>
          ))}
        </div>
      ) : null}
      <div className="e2-measure" data-testid={tid('e2', 'measure')}>
        <span className="small muted">Đo dài</span>
        <select value={mp} onChange={(e) => setMp(e.target.value)} data-testid={tid('e2', 'measure', 'p')} aria-label="Điểm đầu">
          <option value="">–</option>
          {names.map((n) => (
            <option key={n} value={n}>
              {n}
            </option>
          ))}
        </select>
        <select value={mq} onChange={(e) => setMq(e.target.value)} data-testid={tid('e2', 'measure', 'q')} aria-label="Điểm cuối">
          <option value="">–</option>
          {names.map((n) => (
            <option key={n} value={n}>
              {n}
            </option>
          ))}
        </select>
        <output data-testid={tid('e2', 'measure', 'result')}>{measured}</output>
      </div>
    </div>
  );
}
