import { OrthographicCamera, Vector3 } from 'three';
import { cross, dot, sub, type Vec3 } from './geometry.ts';

/**
 * Góc nhìn của bộ xem: chiếu song song (đúng quy ước vẽ hình không gian ở trường; "Nhìn từ trên" cho S trùng A).
 * Trục z của đề hướng lên. yaw quay quanh trục thẳng đứng, pitch là độ ngẩng, zoom là hệ số phóng,
 * panX/panY là độ dời màn hình (px) khi thu phóng quanh điểm giữa hai ngón hoặc con trỏ.
 * Phép chiếu dùng OrthographicCamera của Three.js (ghim 0.187.0).
 */
export interface ViewState {
  yaw: number;
  pitch: number;
  zoom: number;
  panX: number;
  panY: number;
}

/** Góc nhìn lúc mới mở (khung mvp-05: A ở phía sau, SA, AB, AD khuất). */
export const DEFAULT_VIEW: ViewState = { yaw: 30, pitch: 20, zoom: 1, panX: 0, panY: 0 };
export const VIEW_PRESETS = {
  top: { yaw: -90, pitch: 90 },
  front: { yaw: -90, pitch: 0 },
} as const;
/** Giới hạn thu phóng (D-GEO-117: nguồn không cho số; chọn để ở hai đầu vẫn thấy hình). */
export const ZOOM_MIN = 0.4;
export const ZOOM_MAX = 4;
export const PITCH_LIMIT = 89.9;

const rad = (d: number) => (d * Math.PI) / 180;

/** Hướng từ tâm hình tới mắt nhìn. */
export function eyeDir(v: Pick<ViewState, 'yaw' | 'pitch'>): Vec3 {
  const y = rad(v.yaw);
  const p = rad(v.pitch);
  return [Math.cos(p) * Math.cos(y), Math.cos(p) * Math.sin(y), Math.sin(p)];
}
export function rightDir(v: Pick<ViewState, 'yaw'>): Vec3 {
  const y = rad(v.yaw);
  return [-Math.sin(y), Math.cos(y), 0];
}
export const upDir = (v: Pick<ViewState, 'yaw' | 'pitch'>): Vec3 => cross(eyeDir(v), rightDir(v));

export const clampZoom = (z: number) => Math.min(ZOOM_MAX, Math.max(ZOOM_MIN, z));
export const clampPitch = (p: number) => Math.min(PITCH_LIMIT, Math.max(-PITCH_LIMIT, p));
export const wrapYaw = (y: number) => ((((y + 180) % 360) + 360) % 360) - 180;

export interface Projector {
  /** Tọa độ màn hình (px, gốc trái trên của khung) và độ sâu (lớn hơn = gần mắt hơn). */
  project(p: Vec3): { x: number; y: number; depth: number };
  eye: Vec3;
  /** Số px cho một đơn vị độ dài ở zoom hiện tại. */
  pxPerUnit: number;
  width: number;
  height: number;
}

/**
 * Dựng phép chiếu cho khung w×h. `center`/`radius`: tâm và bán kính bao của hình gốc (không đổi khi vẽ thêm,
 * để đối tượng vẽ thêm không làm hình nhảy).
 */
export function makeProjector(view: ViewState, w: number, h: number, center: Vec3, radius: number): Projector {
  const base = (0.4 * Math.min(w, h)) / Math.max(radius, 1e-6);
  const s = base * view.zoom;
  const eye = eyeDir(view);
  const up = upDir(view);
  const cam = new OrthographicCamera(-w / 2 / s, w / 2 / s, h / 2 / s, -h / 2 / s, 0.01, 1000);
  const c = new Vector3(...center);
  cam.up.set(...up);
  cam.position.copy(c).addScaledVector(new Vector3(...eye), 100);
  cam.lookAt(c);
  cam.updateMatrixWorld(true);
  cam.updateProjectionMatrix();
  const v = new Vector3();
  return {
    eye,
    pxPerUnit: s,
    width: w,
    height: h,
    project(p: Vec3) {
      v.set(p[0], p[1], p[2]).project(cam);
      return {
        x: ((v.x + 1) / 2) * w + view.panX,
        y: ((1 - v.y) / 2) * h + view.panY,
        depth: dot(sub(p, center), eye),
      };
    },
  };
}

/** Thu phóng quanh điểm (fx, fy) của khung: điểm dưới ngón tay/con trỏ đứng yên. */
export function zoomAround(view: ViewState, factor: number, fx: number, fy: number, w: number, h: number): ViewState {
  const z = clampZoom(view.zoom * factor);
  const k = z / view.zoom;
  if (k === 1) return view;
  // vị trí màn hình = tâm khung + pan + zoom * (tọa độ chưa phóng): giữ (fx, fy) cố định
  const cx = w / 2 + view.panX;
  const cy = h / 2 + view.panY;
  let panX = fx - (fx - cx) * k - w / 2;
  let panY = fy - (fy - cy) * k - h / 2;
  // giữ tâm hình trong khung (US-GEO-301-FC1: ở hai đầu vẫn thấy một phần hình)
  panX = Math.max(-w / 2, Math.min(w / 2, panX));
  panY = Math.max(-h / 2, Math.min(h / 2, panY));
  return { ...view, zoom: z, panX, panY };
}
