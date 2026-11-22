import type { FeatureModule } from '../types.ts';

/**
 * E2 — Hình đúng: dựng hình, hình mẫu, kiểm chứng, nhãn.
 * Chỗ cắm của epic: thêm route/mục rail vào đây; màn, hook, gọi API đặt trong thư mục src/features/e2/.
 * data-testid của epic bắt đầu bằng 'e2-' (xem app/web/README.md).
 */
export const feature: FeatureModule = {
  id: 'e2',
  title: 'Hình đúng: dựng hình, hình mẫu, kiểm chứng, nhãn',
  teacherRoutes: [],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
