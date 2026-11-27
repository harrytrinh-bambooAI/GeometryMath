import type { FeatureModule } from '../types.ts';

/**
 * S5-E4 — Rubric, bài có chấm, nộp bài viết tay, chấm tay, trả bài. Chỗ cắm rỗng do T-261128-51 tạo; chủ: T-261128-57, 59, 60 (cờ geo.s5.grading.enabled) (PLAN_code-s5 §5).
 * Thêm màn: khai báo route/mục rail ở đây (path tương đối của /gv hoặc /hs), data-testid bắt đầu bằng 's5e4-'.
 * Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's5e4',
  title: 'Rubric, bài có chấm, nộp bài viết tay, chấm tay, trả bài',
  teacherRoutes: [],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
