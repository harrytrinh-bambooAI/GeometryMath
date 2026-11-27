import type { FeatureModule } from '../types.ts';

/**
 * S5-E5 — Thông báo do trợ lý soạn, lỗi sai của lớp, công tắc và hạn mức AI. Chỗ cắm rỗng do T-261128-51 tạo; chủ: T-261128-55, 64 (insights/) (PLAN_code-s5 §5).
 * Thêm màn: khai báo route/mục rail ở đây (path tương đối của /gv hoặc /hs), data-testid bắt đầu bằng 's5e5-'.
 * Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's5e5',
  title: 'Thông báo do trợ lý soạn, lỗi sai của lớp, công tắc và hạn mức AI',
  teacherRoutes: [],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
