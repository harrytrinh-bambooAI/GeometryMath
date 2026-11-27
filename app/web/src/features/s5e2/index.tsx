import type { FeatureModule } from '../types.ts';

/**
 * S5-E2 — Ra đề theo ma trận và trợ lý soạn bộ đề. Chỗ cắm rỗng do T-261128-51 tạo; chủ: T-261128-52, 54 (export/), 63 (draft/) (PLAN_code-s5 §5).
 * Thêm màn: khai báo route/mục rail ở đây (path tương đối của /gv hoặc /hs), data-testid bắt đầu bằng 's5e2-'.
 * Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's5e2',
  title: 'Ra đề theo ma trận và trợ lý soạn bộ đề',
  teacherRoutes: [],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
