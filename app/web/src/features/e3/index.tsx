import { sceneHookEnabled } from '../../shared/viewer/hookGate.ts';
import type { FeatureModule } from '../types.ts';
import { LPrismSandboxPage, TeacherFigurePage, TemplateFigurePage } from './FigurePages.tsx';
import { WorkspacePage } from './WorkspacePage.tsx';

/**
 * E3 — Thao tác hình 3D trên thiết bị cảm ứng.
 * Học sinh: /hs/lam-bai/:id (bài giao và bài tự học), /hs/hinh-mau/:code (hình mẫu, mặc định nón r=2,5 h=4),
 * /hs/thu-hinh/lang-tru-l (chỉ khi bật móc kiểm thử). Giáo viên: /gv/de/:versionId/hinh.
 * Bộ xem dùng chung: src/shared/viewer/ (README.md). data-testid của epic bắt đầu bằng 'e3-' (bộ xem: 'viewer-').
 */
const sandbox = sceneHookEnabled(import.meta.env) ? [{ path: 'thu-hinh/lang-tru-l', element: <LPrismSandboxPage /> }] : [];

export const feature: FeatureModule = {
  id: 'e3',
  title: 'Thao tác hình 3D trên thiết bị cảm ứng',
  teacherRoutes: [{ path: 'de/:versionId/hinh', element: <TeacherFigurePage /> }],
  studentRoutes: [
    { path: 'lam-bai/:id', element: <WorkspacePage /> },
    { path: 'hinh-mau/:code', element: <TemplateFigurePage /> },
    ...sandbox,
  ],
  teacherNav: [],
  studentNav: [],
};
