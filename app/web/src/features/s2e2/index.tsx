import type { FeatureModule } from '../types.ts';

/**
 * S2-E2 — Khung môn học và hình học phẳng. Chỗ cắm rỗng do T-261126-41 tạo; chủ: T-261126-19 (PLAN_code-s23 §5).
 * Thêm màn: khai báo route/mục rail ở đây (path tương đối của /gv hoặc /hs), data-testid bắt đầu bằng 's2e2-'.
 * Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's2e2',
  title: 'Khung môn học và hình học phẳng',
  teacherRoutes: [],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
