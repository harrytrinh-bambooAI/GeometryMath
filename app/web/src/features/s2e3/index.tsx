import type { FeatureModule } from '../types.ts';
import { AddProblemPage } from './AddProblemPage.tsx';
import { MyProblemsPage } from './MyProblemsPage.tsx';
import './s2e3.css';

/**
 * S2-E3 — Thêm đề bằng chữ hoặc ảnh, AI soạn nháp (T-261126-22; Design/prototype/s23.js c2-02-*).
 *   /gv/them-de     C2-02: dán chữ / tải ảnh, bốn bước đọc–dựng–giải–kiểm, ngoại lệ (ảnh mờ, không dựng, lệch đáp số)
 *   /gv/de-cua-toi  ngăn "Của tôi" + chọn đề để giao (bản nháp chữ không giao được; hình do AI dựng phải xác nhận)
 * data-testid bắt đầu bằng 's2e3-'. Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's2e3',
  title: 'Thêm đề bằng chữ hoặc ảnh',
  teacherRoutes: [
    { path: 'them-de', element: <AddProblemPage /> },
    { path: 'de-cua-toi', element: <MyProblemsPage /> },
  ],
  studentRoutes: [],
  teacherNav: [
    { to: 'them-de', label: 'Đề mới', testId: 's2e3-nav-them-de', icon: 'plus' },
    { to: 'de-cua-toi', label: 'Đề của tôi', testId: 's2e3-nav-cua-toi', icon: 'layers' },
  ],
  studentNav: [],
};
