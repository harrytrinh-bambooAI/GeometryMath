import type { FeatureModule } from '../types.ts';

/**
 * E5 — Kho đề của Giáo viên mẫu.
 * Chỗ cắm của epic: thêm route/mục rail vào đây; màn, hook, gọi API đặt trong thư mục src/features/e5/.
 * data-testid của epic bắt đầu bằng 'e5-' (xem app/web/README.md).
 */
export const feature: FeatureModule = {
  id: 'e5',
  title: 'Kho đề của Giáo viên mẫu',
  teacherRoutes: [],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
