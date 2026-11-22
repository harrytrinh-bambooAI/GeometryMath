import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import type { ReactNode, PointerEvent as RPointerEvent } from 'react';
import { tid } from '../testid.ts';
import { clampPitch, DEFAULT_VIEW, VIEW_PRESETS, wrapYaw, zoomAround, type ViewState } from './camera.ts';
import { add, dot, norm, scale, sub, unit, type Vec3 } from './geometry.ts';
import { sceneHookEnabled } from './hookGate.ts';
import { VIEWER_MSG, VIEWER_TEXT } from './messages.ts';
import {
  addPointOn,
  allLines,
  applyTap,
  clearAll,
  initialState,
  setLock,
  toggleAxes,
  type FigureState,
  type Measure,
  type Notice,
  type Outcome,
} from './model.ts';
import { pick } from './pick.ts';
import { buildRender, type P2, type RenderModel } from './render.ts';
import { overlayShapes, overlaySnapshot, SolutionOverlay, type ViewerOverlay } from './overlay.tsx';
import { installSceneHook, snapshot } from './sceneHook.ts';
import { TOOL_EVENT_CODE, type LockState, type ToolId, type ViewerSolid } from './types.ts';
import './viewer.css';

/**
 * BỘ XEM 3D DÙNG CHUNG (E3; E4 dùng lại cho lời giải đồng bộ hình). API: xem README.md cùng thư mục.
 * Một ngón/chuột kéo để xoay, chụm hai ngón/lăn chuột để thu phóng (BR-GEO-141); chạm theo công cụ đang bật.
 * Bố cục điện thoại (≤ 600px): sáu nút chính + "Thêm" (BR-GEO-161) chỉ đổi bằng CSS để không mất trạng thái khi xoay máy.
 */
export interface ToolEvent {
  tool: ToolId;
  /** Mã công cụ cho event_log.arg (D-GEO-058). */
  code: number;
}
export interface ViewerHighlight {
  points?: string[];
  edges?: string[];
  faces?: string[];
  /** E4: góc được tô, tên ba đỉnh với đỉnh góc ở giữa ("SDA"). */
  angles?: string[];
}
export interface Figure3DViewerProps {
  solid: ViewerSolid;
  /** Trạng thái lời giải: 'locked' thì công cụ đo ẩn đại lượng đề hỏi (BR-GEO-156). Mặc định 'locked'. */
  lock?: LockState;
  /** Gọi mỗi khi có một thao tác trên hình (không gọi khi xoay, thu phóng, đặt lại; BR-GEO-144). */
  onToolEvent?: (e: ToolEvent) => void;
  /** Chỉ xem: xoay, thu phóng, góc nhìn; không có thanh công cụ (ví dụ trang chi tiết đề của giáo viên). */
  readOnly?: boolean;
  /** Thông điệp cố định trên khung hình (ví dụ cảnh báo MSG-GEO-16 của hình do AI dựng). */
  banner?: ReactNode;
  /** Tô màu đối tượng theo tên (E4: bước lời giải). */
  highlight?: ViewerHighlight;
  /** E4: phần vẽ thêm của lời giải (đường phụ, ký hiệu góc vuông, cung góc kèm số đo); xem overlay.tsx. */
  overlays?: ViewerOverlay[];
  /** Tiền tố data-testid (mặc định 'viewer'). */
  testId?: string;
  className?: string;
}

const MAIN_PHONE: ReadonlySet<string> = new Set(['select', 'point', 'line', 'plane', 'angle', 'reset']);
const TOOLS: Array<{ id: ToolId; label: string; icon: string }> = [
  { id: 'select', label: 'Chọn', icon: 'M5 3l13 6-6 2-2 6z' },
  { id: 'point', label: 'Điểm', icon: 'M12 9a3 3 0 1 0 0 6 3 3 0 1 0 0-6z' },
  { id: 'line', label: 'Đường', icon: 'M5 19L19 5M5 19a1.5 1.5 0 1 0 0 .1M19 5a1.5 1.5 0 1 0 0 .1' },
  { id: 'plane', label: 'Mặt phẳng', icon: 'M7 7h13l-3 10H4z' },
  { id: 'extend', label: 'Mở rộng', icon: 'M3 12h18M7 8l-4 4 4 4M17 8l4 4-4 4' },
  { id: 'length', label: 'Đo dài', icon: 'M4 16L16 4l4 4L8 20zM8 12l2 2M11 9l2 2' },
  { id: 'angle', label: 'Đo góc', icon: 'M4 20h16M4 20L15 5M10 20a6 6 0 0 0-2-4.5' },
];

type PointMenu = { lineId: string; t: number; label: string; ends: [string, string] };

export function Figure3DViewer({
  solid,
  lock = 'locked',
  onToolEvent,
  readOnly = false,
  banner,
  highlight,
  overlays,
  testId = 'viewer',
  className,
}: Figure3DViewerProps) {
  const T = useCallback((...parts: Array<string | number>) => tid(testId, ...parts), [testId]);
  const [fig, setFig] = useState<FigureState>(() => initialState(solid, lock));
  const [solidRef, setSolidRef] = useState(solid);
  if (solidRef !== solid) {
    // đổi đề: dựng lại mô hình (React cho phép đặt state khi render để đồng bộ theo prop)
    setSolidRef(solid);
    setFig(initialState(solid, lock));
  }
  const [view, setView] = useState<ViewState>(DEFAULT_VIEW);
  const [tool, setTool] = useState<ToolId>('free');
  const [size, setSize] = useState<{ w: number; h: number }>({ w: 0, h: 0 });
  const [toast, setToast] = useState<Notice | null>(null);
  const [menu, setMenu] = useState<PointMenu | null>(null);
  const [more, setMore] = useState(false);
  const [opCount, setOpCount] = useState(0);
  const wrapRef = useRef<HTMLDivElement | null>(null);
  const svgRef = useRef<SVGSVGElement | null>(null);
  const toastTimer = useRef<number | undefined>(undefined);

  const figL = useMemo(() => setLock(fig, lock), [fig, lock]);
  const rm = useMemo<RenderModel | null>(
    () => (size.w > 0 && size.h > 0 ? buildRender(figL, view, size.w, size.h) : null),
    [figL, view, size],
  );

  // ---------------------------------------------------------------- kích thước khung
  useEffect(() => {
    const el = wrapRef.current;
    if (!el) return;
    const ro = new ResizeObserver(() => {
      const r = el.getBoundingClientRect();
      setSize((s) => (Math.abs(s.w - r.width) < 0.5 && Math.abs(s.h - r.height) < 0.5 ? s : { w: r.width, h: r.height }));
    });
    ro.observe(el);
    return () => ro.disconnect();
  }, []);

  // ---------------------------------------------------------------- móc kiểm thử (PO-52)
  const shapes = useMemo(() => (rm ? overlayShapes(rm, highlight?.angles, overlays) : []), [rm, highlight, overlays]);
  const hookData = useRef<{
    fig: FigureState;
    rm: RenderModel | null;
    view: ViewState;
    tool: ToolId;
    opCount: number;
    menu: PointMenu | null;
    shapes: ReturnType<typeof overlayShapes>;
    highlight?: ViewerHighlight;
  }>(null);
  useEffect(() => {
    hookData.current = { fig: figL, rm, view, tool, opCount, menu, shapes, highlight };
  });
  useEffect(() => {
    if (!sceneHookEnabled(import.meta.env)) return;
    return installSceneHook(() => {
      const d = hookData.current;
      if (!d?.rm || !svgRef.current) return undefined;
      const r = svgRef.current.getBoundingClientRect();
      return snapshot(d.fig, d.rm, d.view, d.tool, { left: r.left, top: r.top }, {
        ready: true,
        opCount: d.opCount,
        pointMenu: d.menu ? { line: d.menu.lineId, t: d.menu.t } : null,
        solution: overlaySnapshot(d.rm, d.shapes, d.highlight, { left: r.left, top: r.top }),
      });
    });
  }, []);

  // ---------------------------------------------------------------- thông báo
  const notify = useCallback((n: Notice | undefined) => {
    if (!n) return;
    setToast(n);
    window.clearTimeout(toastTimer.current);
    toastTimer.current = window.setTimeout(() => setToast(null), 5000);
  }, []);
  useEffect(() => () => window.clearTimeout(toastTimer.current), []);

  const commit = useCallback(
    (o: Outcome) => {
      setFig(o.state);
      notify(o.notice);
      if (o.op) {
        setOpCount((c) => c + 1);
        const code = TOOL_EVENT_CODE[o.op];
        if (code && onToolEvent) onToolEvent({ tool: o.op, code });
      }
    },
    [notify, onToolEvent],
  );

  // ---------------------------------------------------------------- chạm theo công cụ
  const onTap = useCallback(
    (x: number, y: number) => {
      if (!rm || readOnly) return;
      const cur = setLock(fig, lock);
      if (tool === 'free') return;
      if (tool === 'point') {
        const hit = pick(rm, x, y, { points: false, lines: true, faces: false });
        const line = hit && allLines(cur).find((l) => l.id === hit.target.id && l.kind !== 'extended');
        if (!hit || !line) {
          setMenu(null);
          notify({ kind: 'toast', text: VIEWER_TEXT.pickLine });
          return;
        }
        setMenu({ lineId: line.id, t: hit.t ?? 0.5, label: line.label, ends: line.ends });
        return;
      }
      const opts =
        tool === 'extend'
          ? { points: false, lines: true, faces: true }
          : tool === 'plane' || tool === 'angle'
            ? { points: true, lines: false, faces: false }
            : tool === 'line'
              ? { points: true, lines: true, faces: false }
              : { points: true, lines: true, faces: true };
      const hit = pick(rm, x, y, opts);
      commit(applyTap(cur, tool, hit?.target ?? null));
    },
    [rm, readOnly, fig, lock, tool, commit, notify],
  );

  // ---------------------------------------------------------------- cử chỉ: kéo xoay, chụm thu phóng, chạm
  const ptrs = useRef(new Map<number, { x: number; y: number }>());
  const gesture = useRef<{ sx: number; sy: number; moved: boolean; multi: boolean; t0: number; lastDist: number } | null>(null);
  const local = (e: { clientX: number; clientY: number }) => {
    const r = svgRef.current!.getBoundingClientRect();
    return { x: e.clientX - r.left, y: e.clientY - r.top };
  };
  const onPointerDown = (e: RPointerEvent<SVGSVGElement>) => {
    const p = local(e);
    ptrs.current.set(e.pointerId, p);
    try {
      e.currentTarget.setPointerCapture(e.pointerId);
    } catch {
      /* sự kiện giả lập không có con trỏ thật */
    }
    if (ptrs.current.size === 1) gesture.current = { sx: p.x, sy: p.y, moved: false, multi: false, t0: Date.now(), lastDist: 0 };
    else if (gesture.current) {
      gesture.current.multi = true;
      const [a, b] = [...ptrs.current.values()] as [{ x: number; y: number }, { x: number; y: number }];
      gesture.current.lastDist = Math.hypot(a.x - b.x, a.y - b.y);
    }
  };
  const onPointerMove = (e: RPointerEvent<SVGSVGElement>) => {
    const prev = ptrs.current.get(e.pointerId);
    const g = gesture.current;
    if (!prev || !g) return;
    const p = local(e);
    ptrs.current.set(e.pointerId, p);
    if (ptrs.current.size >= 2) {
      const [a, b] = [...ptrs.current.values()] as [{ x: number; y: number }, { x: number; y: number }];
      const d = Math.hypot(a.x - b.x, a.y - b.y);
      if (g.lastDist > 0 && d > 0) {
        const f = d / g.lastDist;
        setView((v) => zoomAround(v, f, (a.x + b.x) / 2, (a.y + b.y) / 2, size.w, size.h));
      }
      g.lastDist = d;
      g.moved = true;
      return;
    }
    if (g.multi) return;
    if (!g.moved && Math.hypot(p.x - g.sx, p.y - g.sy) < 6) return;
    g.moved = true;
    const dx = p.x - prev.x;
    const dy = p.y - prev.y;
    setView((v) => ({ ...v, yaw: wrapYaw(v.yaw - dx * 0.4), pitch: clampPitch(v.pitch + dy * 0.4) }));
  };
  const onPointerUp = (e: RPointerEvent<SVGSVGElement>) => {
    const g = gesture.current;
    const had = ptrs.current.delete(e.pointerId);
    if (ptrs.current.size > 0) return;
    gesture.current = null;
    if (!had || !g || g.moved || g.multi || e.type === 'pointercancel') return;
    const p = local(e);
    onTap(p.x, p.y);
  };

  useEffect(() => {
    const el = svgRef.current;
    if (!el) return;
    const onWheel = (e: WheelEvent) => {
      e.preventDefault(); // trang không cuộn, không phóng (US-GEO-301-SC3)
      const r = el.getBoundingClientRect();
      const f = Math.exp(-e.deltaY * 0.0015);
      setView((v) => zoomAround(v, f, e.clientX - r.left, e.clientY - r.top, r.width, r.height));
    };
    const stop = (e: Event) => e.preventDefault(); // Safari: chụm ngón trên hình không phóng cả trang
    el.addEventListener('wheel', onWheel, { passive: false });
    el.addEventListener('gesturestart', stop);
    el.addEventListener('gesturechange', stop);
    el.addEventListener('touchmove', stop, { passive: false });
    return () => {
      el.removeEventListener('wheel', onWheel);
      el.removeEventListener('gesturestart', stop);
      el.removeEventListener('gesturechange', stop);
      el.removeEventListener('touchmove', stop);
    };
  }, [rm === null]); // eslint-disable-line react-hooks/exhaustive-deps

  // ---------------------------------------------------------------- lệnh thanh công cụ
  const chooseTool = (t: ToolId) => {
    setTool((cur) => (cur === t ? 'free' : t));
    setFig((s) => ({ ...s, pending: [] }));
    setMenu(null);
  };
  const resetView = () => setView(DEFAULT_VIEW); // BR-GEO-143: không xóa đối tượng vẽ thêm
  const preset = (k: 'top' | 'front') => setView((v) => ({ ...v, ...VIEW_PRESETS[k] }));
  const freeRotate = () => {
    setTool('free');
    setMenu(null);
    setFig((s) => ({ ...s, pending: [] }));
  };

  // ---------------------------------------------------------------- vẽ
  const hl = useMemo(
    () => ({ p: new Set(highlight?.points ?? []), e: new Set(highlight?.edges ?? []), f: new Set(highlight?.faces ?? []) }),
    [highlight],
  );
  const sel = figL.selection;

  return (
    <div className={`gv-viewer ${className ?? ''}`} data-testid={T()} data-tool={tool}>
      <div className="gv-stage" ref={wrapRef}>
        <svg
          ref={svgRef}
          className="gv-canvas"
          data-testid={T('canvas')}
          width={size.w}
          height={size.h}
          role="img"
          aria-label="Hình không gian"
          onPointerDown={onPointerDown}
          onPointerMove={onPointerMove}
          onPointerUp={onPointerUp}
          onPointerCancel={onPointerUp}
        >
          {rm && <Scene rm={rm} fig={figL} sel={sel} hl={hl} T={T} />}
          {rm && shapes.length > 0 && <SolutionOverlay rm={rm} shapes={shapes} T={T} />}
        </svg>
        {banner && (
          <div className="gv-banner" data-testid={T('banner')}>
            {banner}
          </div>
        )}
        {sel && (
          <div className="gv-selection chip" data-testid={T('selection')} data-kind={sel.kind} data-id={sel.id}>
            {sel.label}
          </div>
        )}
        <div className="gv-views">
          <button type="button" className="gv-view" data-testid={T('view', 'front')} onClick={() => preset('front')}>
            Nhìn từ trước
          </button>
          <button type="button" className="gv-view" data-testid={T('view', 'top')} onClick={() => preset('top')}>
            Nhìn từ trên
          </button>
          <button type="button" className="gv-view" data-testid={T('view', 'free')} aria-pressed={tool === 'free'} onClick={freeRotate}>
            Xoay tự do
          </button>
        </div>
        <Chips solid={solid} measures={figL.measures} T={T} />
        {toast && (
          <div className="gv-toast" role="status" data-testid={T('toast')} data-code={toast.code ? `MSG-GEO-${toast.code}` : ''}>
            {toast.text}
          </div>
        )}
        {menu && (
          <PointMenuBox
            key={`${menu.lineId}-${menu.t}`}
            menu={menu}
            T={T}
            onClose={() => setMenu(null)}
            onPick={(mode) => {
              const o = addPointOn(setLock(fig, lock), menu.lineId, mode);
              if (o.notice?.kind === 'inline') return o.notice.text;
              commit(o);
              setMenu(null);
              return null;
            }}
          />
        )}
      </div>
      {!readOnly && (
        <div className={`gv-toolbar${more ? ' gv-more-open' : ''}`} role="toolbar" aria-label="Công cụ hình" data-testid={T('toolbar')}>
          {TOOLS.map((t) => (
            <ToolButton
              key={t.id}
              id={t.id}
              label={t.label}
              icon={t.icon}
              on={tool === t.id}
              main={MAIN_PHONE.has(t.id)}
              T={T}
              onClick={() => chooseTool(t.id)}
            />
          ))}
          <ToolButton
            id="axes"
            label="Oxyz"
            icon="M5 19V4M5 19h15M5 19l-3 3"
            on={figL.axes}
            main={false}
            T={T}
            onClick={() => setFig((s) => toggleAxes(s))}
          />
          <ToolButton id="reset" label="Đặt lại" icon="M4 12a8 8 0 1 0 2.3-5.6M4 4v4h4" main T={T} onClick={resetView} />
          <ToolButton
            id="clear"
            label="Xóa hết"
            icon="M5 7h14M10 7V5h4v2M7 7l1 13h8l1-13"
            main={false}
            T={T}
            onClick={() => {
              setFig((s) => clearAll(s));
              setMenu(null);
            }}
          />
          <button
            type="button"
            className="gv-tool gv-tool-more"
            data-testid={T('tool', 'more')}
            aria-expanded={more}
            aria-label="Thêm"
            onClick={() => setMore((m) => !m)}
          >
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path d="M6 12h.01M12 12h.01M18 12h.01" />
            </svg>
            <span>Thêm</span>
          </button>
        </div>
      )}
    </div>
  );
}

function ToolButton(props: {
  id: string;
  label: string;
  icon: string;
  on?: boolean;
  main: boolean;
  T: (...p: Array<string | number>) => string;
  onClick: () => void;
}) {
  return (
    <button
      type="button"
      className={`gv-tool${props.main ? ' gv-main' : ' gv-extra'}`}
      data-testid={props.T('tool', props.id)}
      aria-pressed={props.on ?? undefined}
      aria-label={props.label}
      title={props.label}
      onClick={props.onClick}
    >
      <svg viewBox="0 0 24 24" aria-hidden="true">
        <path d={props.icon} />
      </svg>
      <span>{props.label}</span>
    </button>
  );
}

function Chips({ solid, measures, T }: { solid: ViewerSolid; measures: Measure[]; T: (...p: Array<string | number>) => string }) {
  if (!solid.chips?.length && !measures.length) return null;
  return (
    <div className="gv-chips" data-testid={T('chips')}>
      {(solid.chips ?? []).map((c) => (
        <span key={c} className="gv-chip" data-testid={T('chip', c)}>
          {c}
        </span>
      ))}
      {measures.map((m) => (
        <span
          key={m.id}
          className={`gv-chip gv-measure${m.hidden ? ' gv-hidden' : ''}`}
          data-testid={T('measure', m.id)}
          data-hidden={m.hidden}
          title={m.hidden ? VIEWER_MSG['24'] : undefined}
        >
          {m.hidden ? `${m.label} 🔒` : `${m.label} = ${m.text}`}
        </span>
      ))}
    </div>
  );
}

type PointMode = Parameters<typeof addPointOn>[2];
function PointMenuBox({
  menu,
  T,
  onPick,
  onClose,
}: {
  menu: PointMenu;
  T: (...p: Array<string | number>) => string;
  onPick: (m: PointMode) => string | null;
  onClose: () => void;
}) {
  const [ratio, setRatio] = useState(false);
  const [m, setM] = useState('1');
  const [n, setN] = useState('1');
  const [from, setFrom] = useState(menu.ends[0]);
  const [err, setErr] = useState<string | null>(null);
  const num = (s: string) => {
    const t = s.trim().replace(/[−–]/g, '-').replace(',', '.');
    return t === '' ? NaN : Number(t);
  };
  return (
    <div className="gv-menu card" data-testid={T('point-menu')} role="dialog" aria-label={`Thêm điểm trên ${menu.label}`}>
      <div className="gv-menu-title">{menu.label}</div>
      <div className="gv-menu-row">
        <button type="button" className="btn" data-testid={T('point', 'mid')} onClick={() => onPick({ kind: 'mid' })}>
          Trung điểm
        </button>
        <button type="button" className="btn" data-testid={T('point', 'ratio')} aria-pressed={ratio} onClick={() => setRatio(true)}>
          Chia tỉ lệ
        </button>
        <button type="button" className="btn" data-testid={T('point', 'free')} onClick={() => onPick({ kind: 'free', t: menu.t })}>
          Điểm tự do
        </button>
        <button type="button" className="btn ghost" aria-label="Đóng" data-testid={T('point', 'close')} onClick={onClose}>
          ✕
        </button>
      </div>
      {ratio && (
        <form
          className="gv-menu-row"
          onSubmit={(e) => {
            e.preventDefault();
            setErr(onPick({ kind: 'ratio', m: num(m), n: num(n), from }));
          }}
        >
          <label>
            Tính từ{' '}
            <select data-testid={T('ratio', 'from')} value={from} onChange={(e) => setFrom(e.target.value)}>
              {menu.ends.map((x) => (
                <option key={x} value={x}>
                  {x}
                </option>
              ))}
            </select>
          </label>
          <input aria-label="m" inputMode="decimal" className="gv-ratio" data-testid={T('ratio', 'm')} value={m} onChange={(e) => setM(e.target.value)} />
          <span>:</span>
          <input aria-label="n" inputMode="decimal" className="gv-ratio" data-testid={T('ratio', 'n')} value={n} onChange={(e) => setN(e.target.value)} />
          <button type="submit" className="btn primary" data-testid={T('ratio', 'ok')}>
            Thêm điểm
          </button>
        </form>
      )}
      {err && (
        <div className="gv-inline-error" role="alert" data-testid={T('ratio', 'error')}>
          {err}
        </div>
      )}
    </div>
  );
}

// ---------------------------------------------------------------- lớp SVG
function path(ps: P2[]): string {
  return ps.map((p, i) => `${i ? 'L' : 'M'}${p.x.toFixed(2)} ${p.y.toFixed(2)}`).join(' ');
}
const at = (a: P2, b: P2, t: number): P2 => ({ x: a.x + (b.x - a.x) * t, y: a.y + (b.y - a.y) * t });

function Scene({
  rm,
  fig,
  sel,
  hl,
  T,
}: {
  rm: RenderModel;
  fig: FigureState;
  sel: FigureState['selection'];
  hl: { p: Set<string>; e: Set<string>; f: Set<string> };
  T: (...p: Array<string | number>) => string;
}) {
  const center = rm.proj.project(fig.base.center);
  const pr = (v: Vec3) => {
    const q = rm.proj.project(v);
    return { x: q.x, y: q.y };
  };
  return (
    <g>
      {rm.axes.map((a) => (
        <g key={a.axis} data-testid={T('axis', a.axis)} className="gv-axis">
          <line x1={a.from.x} y1={a.from.y} x2={a.to.x} y2={a.to.y} />
          <text x={a.to.x + 6} y={a.to.y - 6}>
            {a.axis}
          </text>
        </g>
      ))}
      {rm.faces.map((f) => {
        const selected = sel?.id === f.id;
        const cls =
          f.kind === 'face' ? 'gv-face' : f.kind === 'plane' ? 'gv-plane' : 'gv-extface';
        const fill = f.tris.map(([i, j, k]) => `${path([f.pts[i]!, f.pts[j]!, f.pts[k]!])} Z`).join(' ');
        return (
          <path
            key={f.id}
            d={fill}
            className={`${cls}${selected ? ' gv-selected' : ''}${hl.f.has(f.id) ? ' gv-hl' : ''}`}
            data-testid={T('face', f.id)}
            data-selected={selected}
          />
        );
      })}
      {rm.circles.map((c) => (
        <path key={c.id} d={`${path(c.pts)} Z`} className="gv-circle" />
      ))}
      {rm.lines.map((l) => {
        const selected = sel?.id === l.id;
        const cls = `gv-line gv-${l.kind}${selected ? ' gv-selected' : ''}${hl.e.has(l.id) ? ' gv-hl' : ''}`;
        return (
          <g key={l.id} className={cls} data-testid={T('line', l.id)} data-style={l.style} data-selected={selected}>
            {l.runs.map((r, i) => {
              const a = at(l.a, l.b, r.from);
              const b = at(l.a, l.b, r.to);
              return <line key={i} x1={a.x} y1={a.y} x2={b.x} y2={b.y} className={r.hidden ? 'gv-dash' : 'gv-solid'} />;
            })}
          </g>
        );
      })}
      {rm.traces.map((t) => (
        <g key={t.id} className="gv-trace" data-testid={T('trace', t.id)}>
          {t.runs.map((r, i) => {
            const a = at(t.a, t.b, r.from);
            const b = at(t.a, t.b, r.to);
            return <line key={i} x1={a.x} y1={a.y} x2={b.x} y2={b.y} className={r.hidden ? 'gv-dash' : 'gv-solid'} />;
          })}
        </g>
      ))}
      {fig.measures.map((m) => (
        <MeasureDraw key={m.id} m={m} pr={pr} T={T} />
      ))}
      {rm.points.map((p) => {
        const selected = sel?.kind === 'point' && sel.id === p.name;
        let dx = p.x - center.x;
        let dy = p.y - center.y;
        const l = Math.hypot(dx, dy) || 1;
        dx = (dx / l) * 16;
        dy = (dy / l) * 16;
        return (
          <g
            key={p.name}
            className={`gv-point${p.user ? ' gv-user' : ''}${selected ? ' gv-selected' : ''}${hl.p.has(p.name) ? ' gv-hl' : ''}`}
            data-testid={T('point', p.name)}
            data-selected={selected}
          >
            <circle cx={p.x} cy={p.y} r={selected ? 7.5 : 5} />
            <text x={p.x + dx} y={p.y + dy} textAnchor="middle" dominantBaseline="central" data-testid={T('label', p.name)}>
              {p.name}
            </text>
          </g>
        );
      })}
    </g>
  );
}

function MeasureDraw({ m, pr, T }: { m: Measure; pr: (v: Vec3) => P2; T: (...p: Array<string | number>) => string }) {
  const d = m.draw;
  if (d.kind === 'segment') {
    const a = pr(d.a);
    const b = pr(d.b);
    return <line className="gv-mline" data-testid={T('mdraw', m.id)} x1={a.x} y1={a.y} x2={b.x} y2={b.y} />;
  }
  if (d.kind === 'perp') {
    const a = pr(d.from);
    const b = pr(d.foot);
    return <line className="gv-mline gv-perp" data-testid={T('mdraw', m.id)} x1={a.x} y1={a.y} x2={b.x} y2={b.y} />;
  }
  if (d.kind === 'angle') {
    const u = unit(sub(d.a, d.o));
    const vb = sub(d.b, d.o);
    const w0 = sub(vb, scale(u, dot(vb, u)));
    if (norm(w0) < 1e-9) return null;
    const w = unit(w0);
    const r = 0.25 * Math.min(norm(sub(d.a, d.o)), norm(vb));
    const ang = Math.acos(Math.max(-1, Math.min(1, dot(u, unit(vb)))));
    const pts: P2[] = [];
    for (let i = 0; i <= 24; i++) {
      const t = (ang * i) / 24;
      pts.push(pr(add(d.o, add(scale(u, r * Math.cos(t)), scale(w, r * Math.sin(t))))));
    }
    return <path className="gv-arc" data-testid={T('mdraw', m.id)} d={path(pts)} />;
  }
  return null;
}
