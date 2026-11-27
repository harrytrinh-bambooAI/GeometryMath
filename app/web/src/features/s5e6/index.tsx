import type { FeatureModule } from '../types.ts';

/**
 * S5-E6 — AI đề xuất điểm theo rubric, chạy song song, giáo viên duyệt. Chỗ cắm rỗng do T-261128-51 tạo; chủ: T-261128-61, 62, 65 (kpi/; cờ geo.s5.ai-grading.enabled) (PLAN_code-s5 §5).
 * Thêm màn: khai báo route/mục rail ở đây (path tương đối của /gv hoặc /hs), data-testid bắt đầu bằng 's5e6-'.
 * Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's5e6',
  title: 'AI đề xuất điểm theo rubric, chạy song song, giáo viên duyệt',
  teacherRoutes: [],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
