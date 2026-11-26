import { apiRequest } from '../../shared/api/client.ts';
import type { LockedQuantity, Scene2D } from '../../shared/scene2d/geometry2d.ts';

/** Dữ liệu màn làm bài hình phẳng (GET /api/v1/student/assignments/{id}/plane). Không có đáp số. */
export interface PlaneView {
  assignmentId: string;
  code: string;
  title: string;
  statement: string;
  kind: 'plane';
  machineLabel: string;
  humanLabel: string;
  built: boolean;
  reason: string | null;
  scene: Scene2D | null;
  released: boolean;
  releaseAt: string | null;
  locked: LockedQuantity[];
  hints: Array<{ level: number; text: string }>;
}

export function fetchPlane(id: string, signal?: AbortSignal): Promise<PlaneView> {
  return apiRequest<PlaneView>(`/api/v1/student/assignments/${encodeURIComponent(id)}/plane`, { signal });
}
