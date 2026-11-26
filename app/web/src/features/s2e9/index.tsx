import type { FeatureModule } from '../types.ts';
import { DocToolsPage } from './DocToolsPage.tsx';
import { ToolsPage } from './ToolsPage.tsx';

/**
 * S2-E9 — Biến thể đề, xuất ảnh/PDF, liên kết chỉ xem (T-261126-25; câu trắc nghiệm của US-S2E9-03 nằm ở features/s2e8).
 *   /gv/xuat-de            đề "Của tôi": chọn 1…20 đề theo thứ tự rồi "Xuất PDF" (US-S2E9-02)
 *   /gv/xuat-de/de/:id     một đề: hình + hai nhãn, "Tạo biến thể" (US-S2E9-01), "Xuất ảnh", "Tạo liên kết chỉ xem"
 * Trang công khai của liên kết chỉ xem do máy chủ dựng (GET /api/v1/share/<mã>), không nằm trong ứng dụng đăng nhập.
 * data-testid bắt đầu bằng 's2e9-'. Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's2e9',
  title: 'Biến thể đề, xuất ảnh/PDF, liên kết chỉ xem',
  teacherRoutes: [
    { path: 'xuat-de', element: <ToolsPage /> },
    { path: 'xuat-de/de/:id', element: <DocToolsPage /> },
  ],
  studentRoutes: [],
  teacherNav: [{ to: 'xuat-de', label: 'Xuất đề', testId: 's2e9-nav-xuat-de', icon: 'print' }],
  studentNav: [],
};
