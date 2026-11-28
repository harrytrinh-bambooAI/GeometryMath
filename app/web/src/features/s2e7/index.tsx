import type { FeatureModule } from '../types.ts';
import { PresentPage } from './PresentPage.tsx';

/**
 * S2-E7 — Trình chiếu trên lớp (T-261126-23, khung C2-04). Lối vào: Kho đề › đề (/gv/kho-de-cua-toi/de/:id, S2-E6)
 * nút "Trình chiếu" → /gv/trinh-chieu/:id. Esc / Hủy về màn đề. Không có mục rail riêng (học sinh không vào được).
 * data-testid bắt đầu bằng 's2e7-'. Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's2e7',
  title: 'Trình chiếu trên lớp',
  teacherRoutes: [{ path: 'trinh-chieu/:id', element: <PresentPage /> }],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
