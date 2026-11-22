import type { FeatureModule } from '../types.ts';

/**
 * E6 — Giao bài và khóa lời giải.
 * Chỗ cắm của epic: thêm route/mục rail vào đây; màn, hook, gọi API đặt trong thư mục src/features/e6/.
 * data-testid của epic bắt đầu bằng 'e6-' (xem app/web/README.md).
 */
export const feature: FeatureModule = {
  id: 'e6',
  title: 'Giao bài và khóa lời giải',
  teacherRoutes: [],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
