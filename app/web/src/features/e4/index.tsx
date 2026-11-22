import type { FeatureModule } from '../types.ts';

/**
 * E4 — Gợi ý, lời giải đồng bộ hình, hỏi đáp, tự học.
 * Chỗ cắm của epic: thêm route/mục rail vào đây; màn, hook, gọi API đặt trong thư mục src/features/e4/.
 * data-testid của epic bắt đầu bằng 'e4-' (xem app/web/README.md).
 */
export const feature: FeatureModule = {
  id: 'e4',
  title: 'Gợi ý, lời giải đồng bộ hình, hỏi đáp, tự học',
  teacherRoutes: [],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
