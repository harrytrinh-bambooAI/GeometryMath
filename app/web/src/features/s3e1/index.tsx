import type { FeatureModule } from '../types.ts';
import { ClassDetailPage } from './ClassDetailPage.tsx';
import { ClassesPage } from './ClassesPage.tsx';
import { JoinPage } from './JoinPage.tsx';
import { PrintPage } from './PrintPage.tsx';
import './s3e1.css';

/**
 * S3-E1 — Lớp, mã lớp, duyệt vào lớp, đặt lại mật khẩu (T-261126-28, PLAN_code-s23 §5).
 *   /gv/lop-hoc            Lớp học: thẻ lớp + tạo lớp (C3-02)
 *   /gv/lop-hoc/:id        Một lớp: mã lớp, công tắc, chờ duyệt, học sinh, đặt lại mật khẩu (C3-07)
 *   /gv/lop-hoc/:id/in     Trang in A4 (mã QR + mã lớp + bốn bước)
 *   /hs/lop-cua-em         Lớp của em + vào lớp bằng mã (chờ duyệt / bị từ chối)
 */
export const feature: FeatureModule = {
  id: 's3e1',
  title: 'Lớp, mã lớp, duyệt vào lớp, đặt lại mật khẩu',
  teacherRoutes: [
    { path: 'lop-hoc', element: <ClassesPage /> },
    { path: 'lop-hoc/:id', element: <ClassDetailPage /> },
    { path: 'lop-hoc/:id/in', element: <PrintPage /> },
  ],
  studentRoutes: [{ path: 'lop-cua-em', element: <JoinPage /> }],
  teacherNav: [{ to: 'lop-hoc', label: 'Lớp học', testId: 's3e1-nav-lop-hoc', icon: 'users' }],
  studentNav: [{ to: 'lop-cua-em', label: 'Lớp của em', testId: 's3e1-nav-lop-cua-em', icon: 'users' }],
};
