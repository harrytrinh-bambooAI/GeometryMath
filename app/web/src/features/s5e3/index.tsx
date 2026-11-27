import type { FeatureModule } from '../types.ts';

/**
 * S5-E3 — Kho tri thức, hỏi đáp theo kho, kiểm soát đáp án. Chỗ cắm rỗng do T-261128-51 tạo; chủ: T-261128-56, 58, 63 (history/) (PLAN_code-s5 §5).
 * Thêm màn: khai báo route/mục rail ở đây (path tương đối của /gv hoặc /hs), data-testid bắt đầu bằng 's5e3-'.
 * Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's5e3',
  title: 'Kho tri thức, hỏi đáp theo kho, kiểm soát đáp án',
  teacherRoutes: [],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
