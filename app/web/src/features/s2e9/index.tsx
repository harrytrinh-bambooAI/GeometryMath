import type { FeatureModule } from '../types.ts';

/**
 * S2-E9 — Biến thể đề, xuất ảnh/PDF, liên kết chỉ xem. Chỗ cắm rỗng do T-261126-41 tạo; chủ: T-261126-25 (PLAN_code-s23 §5).
 * Thêm màn: khai báo route/mục rail ở đây (path tương đối của /gv hoặc /hs), data-testid bắt đầu bằng 's2e9-'.
 * Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's2e9',
  title: 'Biến thể đề, xuất ảnh/PDF, liên kết chỉ xem',
  teacherRoutes: [],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
