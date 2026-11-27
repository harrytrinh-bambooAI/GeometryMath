import type { FeatureModule } from '../types.ts';

/**
 * S5-E1 — Trợ lý soạn giáo án. Chỗ cắm rỗng do T-261128-51 tạo; chủ: T-261128-53, 54 (PLAN_code-s5 §5).
 * Thêm màn: khai báo route/mục rail ở đây (path tương đối của /gv hoặc /hs), data-testid bắt đầu bằng 's5e1-'.
 * Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's5e1',
  title: 'Trợ lý soạn giáo án',
  teacherRoutes: [],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
