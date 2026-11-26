import type { FeatureModule } from '../types.ts';
import { PlaneWorkPage } from './PlaneWorkPage.tsx';

/**
 * S2-E2 — Khung môn học và hình học phẳng (T-261126-19). Học sinh: /hs/bai-phang/:id — C2-05 làm bài hình phẳng
 * trên iPad/iPhone (US-S2E2-03), vẽ bằng bộ vẽ 2D dùng chung src/shared/scene2d/ (SCENE-CONTRACT-2D.md).
 * data-testid bắt đầu bằng 's2e2-'. Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's2e2',
  title: 'Khung môn học và hình học phẳng',
  teacherRoutes: [],
  studentRoutes: [{ path: 'bai-phang/:id', element: <PlaneWorkPage /> }],
  teacherNav: [],
  studentNav: [],
};
