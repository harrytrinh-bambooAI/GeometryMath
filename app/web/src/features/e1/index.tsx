import type { FeatureModule } from '../types.ts';
import { AssignPage } from './AssignPage.tsx';
import { JoinClassPage } from './JoinClassPage.tsx';
import { MonitorPage } from './MonitorPage.tsx';
import { MyAssignmentsPage } from './MyAssignmentsPage.tsx';
import { StudentAssignmentPage } from './StudentAssignmentPage.tsx';
import { TeacherLoginPage } from './TeacherLoginPage.tsx';
import './e1.css';

/**
 * E1 — Nền tảng, cổng AI, bộ đề chuẩn, thử tải.
 * Học sinh: /hs/vao-lop (Vào lớp), /hs/bai-cua-em (Bài của em), /hs/bai/:id (một bài + Hỏi đáp).
 * Giáo viên: /gv/dang-nhap (Giáo viên mẫu), /gv/kho-de, /gv/giao-bai/:versionId, /gv/theo-doi/:id.
 * Màn kho đề (/gv/kho-de) nay do E5 cắm (features/e5/KhoDePage.tsx); giao bài, theo dõi lớp ở đây là bản tối thiểu
 * cho tiêu chí E1; E6–E7 làm bản đầy đủ.
 */
export const feature: FeatureModule = {
  id: 'e1',
  title: 'Nền tảng, cổng AI, bộ đề chuẩn, thử tải',
  teacherRoutes: [
    { path: 'dang-nhap', element: <TeacherLoginPage /> },
    { path: 'giao-bai/:versionId', element: <AssignPage /> },
    { path: 'theo-doi/:id', element: <MonitorPage /> },
  ],
  studentRoutes: [
    { path: 'vao-lop', element: <JoinClassPage /> },
    { path: 'bai-cua-em', element: <MyAssignmentsPage /> },
    { path: 'bai/:id', element: <StudentAssignmentPage /> },
  ],
  teacherNav: [{ to: 'kho-de', label: 'Kho đề', testId: 'e1-nav-kho-de' }],
  studentNav: [{ to: 'bai-cua-em', label: 'Bài của em', testId: 'e1-nav-bai-cua-em' }],
};
