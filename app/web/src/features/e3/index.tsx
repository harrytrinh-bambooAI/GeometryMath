import type { FeatureModule } from '../types.ts';

/**
 * E3 — Thao tác hình 3D trên thiết bị cảm ứng.
 * Chỗ cắm của epic: thêm route/mục rail vào đây; màn, hook, gọi API đặt trong thư mục src/features/e3/.
 * data-testid của epic bắt đầu bằng 'e3-' (xem app/web/README.md).
 */
export const feature: FeatureModule = {
  id: 'e3',
  title: 'Thao tác hình 3D trên thiết bị cảm ứng',
  teacherRoutes: [],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
