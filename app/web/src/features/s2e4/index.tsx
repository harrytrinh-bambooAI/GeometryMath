import type { FeatureModule } from '../types.ts';

/**
 * S2-E4 — Chat chỉnh nháp đề, import docx/pdf, đối chiếu trùng. Chỗ cắm rỗng do T-261126-41 tạo; chủ: T-261126-24 (PLAN_code-s23 §5).
 * Thêm màn: khai báo route/mục rail ở đây (path tương đối của /gv hoặc /hs), data-testid bắt đầu bằng 's2e4-'.
 * Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's2e4',
  title: 'Chat chỉnh nháp đề, import docx/pdf, đối chiếu trùng',
  teacherRoutes: [],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
