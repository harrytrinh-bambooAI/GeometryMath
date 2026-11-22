import type { FeatureModule } from '../types.ts';

/**
 * E7 — Theo dõi lớp và ghi sự kiện.
 * Chỗ cắm của epic: thêm route/mục rail vào đây; màn, hook, gọi API đặt trong thư mục src/features/e7/.
 * data-testid của epic bắt đầu bằng 'e7-' (xem app/web/README.md).
 */
export const feature: FeatureModule = {
  id: 'e7',
  title: 'Theo dõi lớp và ghi sự kiện',
  teacherRoutes: [],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
