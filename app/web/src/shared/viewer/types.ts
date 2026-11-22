import type { LengthUnit } from './format.ts';
import type { Vec3 } from './geometry.ts';

/**
 * Đầu vào của bộ xem (độc lập với hợp đồng dữ liệu cảnh của máy chủ; lớp chuyển đổi ở `adapter.ts`).
 * - points: đỉnh có tên và tọa độ (dữ kiện của đề, D-GEO-154).
 * - edges: cạnh nối hai đỉnh theo tên. faces: mặt phẳng của hình, đỉnh theo thứ tự đi vòng (có thể lõm).
 * - circles: đường tròn đáy của hình tròn xoay (chỉ vẽ, không che khuất).
 * - unit: tham số độ dài của đề (ví dụ a = 1) để hiện số đo dạng "a√2"; null thì hiện số thập phân.
 * - hiddenMeasures: đại lượng đề hỏi và đại lượng cùng vai trò (chỉ đối tượng, không giá trị; BR-GEO-155).
 */
export interface ViewerSolid {
  points: Array<{ name: string; pos: Vec3 }>;
  edges: Array<[string, string]>;
  faces: string[][];
  circles?: Array<{ id: string; center: Vec3; radius: number; normal: Vec3 }>;
  unit?: LengthUnit | null;
  /** Đơn vị viết sau số đo khi hình không có tham số (ví dụ "cm"). */
  unitSuffix?: string;
  hiddenMeasures?: MeasureRef[];
  /** Thẻ ràng buộc ở góc dưới trái (BR-GEO-130), ví dụ "SA = a". */
  chips?: string[];
}

/** Một đại lượng đo được, mô tả bằng tên đối tượng (dùng cho danh sách ẩn và cho kết quả đo). */
export type MeasureRef =
  | { kind: 'len'; a: string; b: string }
  | { kind: 'ang'; a: string; o: string; b: string }
  | { kind: 'dpp'; p: string; plane: string[] }
  | { kind: 'dpl'; p: string; line: [string, string] }
  | { kind: 'dll'; l1: [string, string]; l2: [string, string] }
  | { kind: 'dff'; f1: string[]; f2: string[] };

export type ToolId =
  | 'free'
  | 'select'
  | 'point'
  | 'line'
  | 'plane'
  | 'extend'
  | 'length'
  | 'angle';

/** Mã công cụ ghi vào sự kiện thao tác (event_log.arg, D-GEO-058). */
export const TOOL_EVENT_CODE: Partial<Record<ToolId, number>> = {
  select: 1,
  point: 2,
  line: 3,
  plane: 4,
  extend: 5,
  length: 6,
  angle: 7,
};

/** Trạng thái lời giải: khóa thì công cụ đo ẩn giá trị của đại lượng trong danh sách ẩn (BR-GEO-156). */
export type LockState = 'locked' | 'open';
