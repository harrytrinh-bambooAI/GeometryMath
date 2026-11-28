import type { FeatureModule } from '../types.ts';
import { KbPage } from './KbPage.tsx';
import './s5e3.css';

/**
 * S5-E3 — Kho tri thức, hỏi đáp theo kho, kiểm soát đáp án. Chỗ cắm rỗng do T-261128-51 tạo; chủ: T-261128-56, 58, 63 (history/) (PLAN_code-s5 §5).
 *   /gv/kho-tri-thuc   "Kho tri thức" (US-S5E3-01 thêm tài liệu, phiên bản, duyệt; US-S5E3-02 gắn lớp có hạn) — T-261128-56
 * Thêm màn: khai báo route/mục rail ở đây (path tương đối của /gv hoặc /hs), data-testid bắt đầu bằng 's5e3-'.
 * Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's5e3',
  title: 'Kho tri thức, hỏi đáp theo kho, kiểm soát đáp án',
  teacherRoutes: [{ path: 'kho-tri-thuc', element: <KbPage /> }],
  studentRoutes: [],
  teacherNav: [{ to: 'kho-tri-thuc', label: 'Kho TT', testId: 's5e3-nav-kho-tri-thuc', icon: 'file' }],
  studentNav: [],
};
