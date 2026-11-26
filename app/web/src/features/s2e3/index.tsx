import type { FeatureModule } from '../types.ts';

/**
 * S2-E3 — Thêm đề bằng chữ hoặc ảnh. Chỗ cắm rỗng do T-261126-41 tạo; chủ: T-261126-22 (PLAN_code-s23 §5).
 * Thêm màn: khai báo route/mục rail ở đây (path tương đối của /gv hoặc /hs), data-testid bắt đầu bằng 's2e3-'.
 * Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's2e3',
  title: 'Thêm đề bằng chữ hoặc ảnh',
  teacherRoutes: [],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
