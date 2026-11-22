import type { FeatureModule } from '../types.ts';

/**
 * E1 — Nền tảng, cổng AI, bộ đề chuẩn, thử tải.
 * Chỗ cắm của epic: thêm route/mục rail vào đây; màn, hook, gọi API đặt trong thư mục src/features/e1/.
 * data-testid của epic bắt đầu bằng 'e1-' (xem app/web/README.md).
 */
export const feature: FeatureModule = {
  id: 'e1',
  title: 'Nền tảng, cổng AI, bộ đề chuẩn, thử tải',
  teacherRoutes: [],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
