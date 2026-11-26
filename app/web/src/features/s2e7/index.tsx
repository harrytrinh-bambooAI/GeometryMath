import type { FeatureModule } from '../types.ts';

/**
 * S2-E7 — Trình chiếu trên lớp. Chỗ cắm rỗng do T-261126-41 tạo; chủ: T-261126-23 (PLAN_code-s23 §5).
 * Thêm màn: khai báo route/mục rail ở đây (path tương đối của /gv hoặc /hs), data-testid bắt đầu bằng 's2e7-'.
 * Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's2e7',
  title: 'Trình chiếu trên lớp',
  teacherRoutes: [],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
