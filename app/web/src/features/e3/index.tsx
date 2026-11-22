import { lazy, Suspense } from 'react';
import type { FeatureModule } from '../types.ts';
import { TeacherFigurePage, TemplateFigurePage } from './FigurePages.tsx';
import { WorkspacePage } from './WorkspacePage.tsx';

/**
 * E3 — Thao tác hình 3D trên thiết bị cảm ứng.
 * Học sinh: /hs/lam-bai/:id (bài giao và bài tự học), /hs/hinh-mau/:code (hình mẫu, mặc định nón r=2,5 h=4),
 * /hs/thu-hinh/lang-tru-l (chỉ khi bật móc kiểm thử). Giáo viên: /gv/de/:versionId/hinh.
 * Bộ xem dùng chung: src/shared/viewer/ (README.md). data-testid của epic bắt đầu bằng 'e3-' (bộ xem: 'viewer-').
 *
 * Trang thử hình (SandboxPage.tsx) chỉ có ở dev hoặc bản build có VITE_GEO_TEST_HOOKS=1 (PO-52). Điều kiện viết trực
 * tiếp bằng import.meta.env và trang nạp bằng import động để Vite loại cả nhánh lẫn tệp khỏi bản production
 * (kiểm bằng scripts/check-dist.mjs sau `npm run build`).
 */
const sandbox =
  import.meta.env.DEV || import.meta.env.VITE_GEO_TEST_HOOKS === '1'
    ? (() => {
        const Page = lazy(() => import('./SandboxPage.tsx'));
        return [
          {
            path: 'thu-hinh/lang-tru-l',
            element: (
              <Suspense fallback={<p className="muted">Đang tải…</p>}>
                <Page />
              </Suspense>
            ),
          },
        ];
      })()
    : [];

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
