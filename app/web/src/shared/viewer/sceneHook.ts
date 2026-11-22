import type { ViewState } from './camera.ts';
import type { FigureState } from './model.ts';
import type { RenderModel } from './render.ts';
import type { ToolId } from './types.ts';

export { sceneHookEnabled } from './hookGate.ts';

/**
 * Móc kiểm thử CHỈ ĐỌC `window.__geoScene` (PO-52, trả lời QF-E3-08): trạng thái cảnh 3D để E2E-UI kiểm hình bằng dữ
 * liệu (điểm, cạnh, mặt, nét đứt, đối tượng chọn/vẽ/đo, camera). Chỉ bật ở bản dev (`vite`) hoặc bản build có
 * `VITE_GEO_TEST_HOOKS=1` (make e2e); bản production không có biến này nên Vite loại bỏ cả nhánh khi build.
 * Toạ độ màn hình là px theo khung nhìn của trang (clientX/Y), dùng trực tiếp cho chuột/chạm của Playwright.
 */
const r2 = (n: number) => Math.round(n * 100) / 100;
const r6 = (n: number) => Math.round(n * 1e6) / 1e6;

export function snapshot(
  s: FigureState,
  rm: RenderModel,
  view: ViewState,
  tool: ToolId,
  origin: { left: number; top: number },
  extra: Record<string, unknown> = {},
) {
  const sc = (p: { x: number; y: number }) => [r2(p.x + origin.left), r2(p.y + origin.top)];
  return {
    version: 1,
    tool,
    camera: { yaw: r2(view.yaw), pitch: r2(view.pitch), zoom: r6(view.zoom), panX: r2(view.panX), panY: r2(view.panY) },
    viewport: { left: r2(origin.left), top: r2(origin.top), width: r2(rm.proj.width), height: r2(rm.proj.height) },
    axes: rm.axes.map((a) => ({ axis: a.axis, from: sc(a.from), to: sc(a.to) })),
    points: rm.points.map((p) => ({ name: p.name, pos: p.pos.map(r6), screen: sc(p), user: p.user })),
    edges: rm.lines.map((l) => ({
      id: l.id,
      label: l.label,
      kind: l.kind,
      ends: l.ends,
      from: l.a3.map(r6),
      to: l.b3.map(r6),
      screen: [sc(l.a), sc(l.b)],
      style: l.style,
      hiddenRatio: r2(l.hiddenRatio),
    })),
    faces: rm.faces.map((f) => ({
      id: f.id,
      label: f.label,
      kind: f.kind,
      screen: f.pts.map(sc),
      triangles: f.tris,
      area: r6(f.area),
    })),
    planes: s.planes.map((p) => ({ id: p.id, through: p.names, polygon: p.poly.map((v) => v.map(r6)) })),
    traces: rm.traces.map((t) => ({ id: t.id, plane: t.plane, face: t.face, screen: [sc(t.a), sc(t.b)] })),
    extensions: s.extensions.map((e) => ({ id: e.id, kind: e.kind, target: e.target })),
    measures: s.measures.map((m) => ({ id: m.id, kind: m.kind, label: m.label, text: m.text, hidden: m.hidden })),
    selection: s.selection,
    pending: s.pending,
    lock: s.lock,
    ...extra,
  };
}
export type SceneSnapshot = ReturnType<typeof snapshot>;

declare global {
  interface Window {
    __geoScene?: SceneSnapshot;
  }
}

/** Gắn getter chỉ đọc; trả hàm gỡ. Mỗi lần đọc là một bản sao đóng băng (sửa không ảnh hưởng ứng dụng). */
export function installSceneHook(read: () => SceneSnapshot | undefined): () => void {
  Object.defineProperty(window, '__geoScene', {
    configurable: true,
    enumerable: false,
    get: () => {
      const v = read();
      return v ? deepFreeze(structuredClone(v)) : undefined;
    },
  });
  return () => {
    delete (window as { __geoScene?: unknown }).__geoScene;
  };
}
function deepFreeze<T>(o: T): T {
  if (o && typeof o === 'object') {
    for (const v of Object.values(o)) deepFreeze(v);
    Object.freeze(o);
  }
  return o;
}
