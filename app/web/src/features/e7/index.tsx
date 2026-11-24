import type { FeatureModule } from '../types.ts';
import { AssignedPage } from './AssignedPage.tsx';
import { MonitorPage } from './MonitorPage.tsx';
import './e7.css';

/**
 * E7 — Theo dõi lớp và ghi sự kiện.
 * Giáo viên: /gv/theo-doi (mục "Theo dõi": bài đã giao, US-GEO-701-SC6), /gv/theo-doi/:id (Theo dõi lớp đầy đủ; thay
 * bản tối thiểu của E1 — khung cho route cùng đường dẫn của epic sau thắng).
 * data-testid của epic bắt đầu bằng 'e7-'.
 */
export const feature: FeatureModule = {
  id: 'e7',
  title: 'Theo dõi lớp và ghi sự kiện',
  teacherRoutes: [
    { path: 'theo-doi', element: <AssignedPage /> },
    { path: 'theo-doi/:id', element: <MonitorPage /> },
  ],
  studentRoutes: [],
  teacherNav: [{ to: 'theo-doi', label: 'Theo dõi', testId: 'e7-nav-theo-doi' }],
  studentNav: [],
};
