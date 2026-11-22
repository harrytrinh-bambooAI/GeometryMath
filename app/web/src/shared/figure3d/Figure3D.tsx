import { tid } from '../testid.ts';

/**
 * CHỖ ĐẶT SẴN cho hình 3D (SPEC-technical FR-SOL-2: `figure.type = 3d` → Three.js).
 * Chưa làm tính năng: epic E2/E3 thay phần thân bằng scene thật. Three.js (ghim 0.187.0) chỉ được nạp
 * động qua loadThree() để không làm nặng gói đầu cho màn không có hình.
 */
export interface SolidVertex {
  name: string;
  x: number;
  y: number;
  z: number;
}

export interface Solid {
  vertices: SolidVertex[];
  edges: Array<[string, string]>;
  faces?: string[][];
}

/** Nạp Three.js theo nhu cầu (tách thành chunk riêng khi build). */
export function loadThree() {
  return import('three');
}

export function Figure3D({ solid, testId = tid('figure3d') }: { solid?: Solid; testId?: string }) {
  return (
    <div className="empty" data-testid={testId} data-figure-vertices={solid?.vertices.length ?? 0}>
      Hình 3D sẽ hiển thị ở đây (chưa làm trong Đợt 0).
    </div>
  );
}
