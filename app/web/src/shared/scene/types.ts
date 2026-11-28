/**
 * HỢP ĐỒNG DỮ LIỆU CẢNH v1 — kiểu TypeScript khớp `vn.geo.server.geometry.api.SceneDto` (E2 sở hữu).
 * Tài liệu, ví dụ JSON và quy tắc đổi: app/docs/SCENE-CONTRACT.md.
 * E3 (bộ xem 3D) và E4 (lời giải đồng bộ hình) chỉ đọc kiểu ở đây; chỉ THÊM trường tùy chọn, không đổi trường có sẵn.
 * Máy chủ bỏ trường rỗng (null) khỏi JSON, nên trường đánh dấu `?` có thể vắng mặt.
 */

export const SCENE_SCHEMA_VERSION = 1;

export type FigureType = '3d' | '2d' | 'none';
export type CoordSource = 'engine' | 'ai';
export type SolidKind = 'pyramid' | 'prism' | 'box' | 'cube' | 'cone' | 'cylinder' | 'sphere';
export type VerifyStatus = 'verified' | 'needs_review';

/** Chuỗi nhãn máy duy nhất trên mọi màn (BR-GEO-136, D-GEO-079). */
export const VERIFY_LABEL: Record<VerifyStatus, string> = {
  verified: 'Đã kiểm chứng',
  needs_review: 'Cần xem lại',
};

/** Một đỉnh có tên; hệ trục toán học, z hướng lên (bộ xem Three.js tự đổi sang y-lên). */
export interface ScenePoint {
  name: string;
  x: number;
  y: number;
  z: number;
}

/** Bộ ba số [x, y, z]. */
export type Vec3 = [number, number, number];

/**
 * Mặt cong.
 * - cone: đáy tâm `center`, bán kính `radius`, pháp tuyến đáy `axis`, đỉnh `apex`.
 * - cylinder: đáy dưới tâm `center`, bán kính `radius`, trục đơn vị `axis`, chiều cao `height`.
 * - sphere: tâm `center`, bán kính `radius`.
 */
export interface SceneSurface {
  kind: 'cone' | 'cylinder' | 'sphere';
  center: Vec3;
  radius: number;
  axis?: Vec3;
  apex?: Vec3;
  height?: number;
}

/** Thẻ ràng buộc ở góc dưới trái khung hình (BR-GEO-130), ví dụ { text: 'SA = a', points: ['S', 'A'] }. */
export interface SceneChip {
  text: string;
  points: string[];
}

export interface SceneAxes {
  /** Hệ trục Oxyz bật được (luôn true ở MVP, US-GEO-205-SC1). */
  available: boolean;
  /** Gốc tại gốc tọa độ của đề (không phải tâm hình). */
  origin: Vec3;
  /** Trục "lên" của dữ liệu: luôn 'z'. */
  up: 'z';
}

export interface SceneWarning {
  /** 'MSG-GEO-16' khi hình do AI dựng. */
  code: string;
  /** Câu hiện nguyên văn, cố định trên khung hình. */
  text: string;
  /** Luôn false: học sinh không tắt được cảnh báo (BR-GEO-137). */
  dismissible: false;
}

export interface SceneVerification {
  status: VerifyStatus;
  /** Đúng 'Đã kiểm chứng' hoặc 'Cần xem lại'. */
  label: string;
  warning?: SceneWarning;
}

export interface Scene {
  schemaVersion: number;
  figureType: FigureType;
  /** id bản dựng đã lưu (scene_build); vắng khi là bản xem trước hình mẫu. */
  sceneId?: string;
  coordSource: CoordSource;
  /** Mã bộ dựng của engine; vắng khi coordSource = 'ai'. */
  template?: string;
  solidKind?: SolidKind;
  /** Đơn vị hiển thị số đo: 'a', 'cm' hoặc ''. */
  unit: string;
  params: Record<string, number>;
  points: ScenePoint[];
  /** Cạnh: cặp tên đỉnh. Cạnh khuất (nét đứt) do bộ xem tính theo góc nhìn, máy chủ không gửi. */
  edges: Array<[string, string]>;
  /** Mặt phẳng: tên đỉnh theo thứ tự vòng (đa giác lồi; chia tam giác kiểu quạt từ đỉnh đầu). */
  faces: string[][];
  surfaces: SceneSurface[];
  chips: SceneChip[];
  /** Đại lượng phải ẩn giá trị khi đo: 'ang:S-D-A' (góc tại D giữa DS, DA) | 'len:O-O′'. */
  hiddenMeasures: string[];
  axes: SceneAxes;
  verification: SceneVerification;
}

/** Tham số của một hình mẫu (US-GEO-205). */
export interface TemplateParam {
  key: string;
  label: string;
  default: number;
}

/** Một hình mẫu của danh mục (GET /api/v1/geometry/templates). */
export interface SolidTemplate {
  code: string;
  name: string;
  solidKind: SolidKind;
  /** Có thẻ ở mục "Hoặc chọn một hình mẫu" (sáu thẻ; lập phương là biến thể của hộp, không có thẻ riêng). */
  card: boolean;
  order: number;
  params: TemplateParam[];
}

/** Bài tự học đã dựng hình (POST /api/v1/student/self-study, GET /api/v1/student/self-study/{assignmentId}). */
export interface SelfStudy {
  assignmentId: string;
  problemVersionId: string;
  statement: string;
  scene: Scene;
}
