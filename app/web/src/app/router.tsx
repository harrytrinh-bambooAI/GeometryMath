import { createBrowserRouter, isRouteErrorResponse, Link, useRouteError } from 'react-router';
import type { RouteObject } from 'react-router';
import { features } from '../features/index.ts';
import { HomePage } from '../pages/HomePage.tsx';
import { tid } from '../shared/testid.ts';
import { AreaLayout, AreaOverview } from './AreaLayout.tsx';

/**
 * Bản đồ đường dẫn:
 *   /            trang chủ
 *   /gv/...      vùng Giáo viên (route con do epic khai báo ở FeatureModule.teacherRoutes)
 *   /hs/...      vùng Học sinh  (FeatureModule.studentRoutes)
 */
function NotFound() {
  return (
    <div className="body" data-testid={tid('shell', 'not-found')}>
      <div className="card">
        <h2>Không tìm thấy trang</h2>
        <p className="muted">Đường dẫn này không tồn tại.</p>
        <Link className="btn" to="/">
          Về trang chủ
        </Link>
      </div>
    </div>
  );
}

function RouteError() {
  const err = useRouteError();
  if (isRouteErrorResponse(err) && err.status === 404) return <NotFound />;
  return (
    <div className="body" data-testid={tid('shell', 'route-error')}>
      <div className="alert" role="alert">
        Màn hình gặp lỗi, vui lòng tải lại trang.
      </div>
    </div>
  );
}

/**
 * Route con của một vùng: epic sau khai báo cùng đường dẫn thì THAY route của epic trước (bản đầy đủ thay bản tối
 * thiểu, ví dụ Theo dõi lớp /gv/theo-doi/:id của E7 thay bản E1).
 */
const childRoutes = (pick: 'teacherRoutes' | 'studentRoutes'): RouteObject[] => {
  const byPath = new Map<string, RouteObject>();
  for (const r of features.flatMap((f) => f[pick] ?? [])) byPath.set(r.path ?? '', r);
  return [...byPath.values()];
};

const overview = (pick: 'teacherRoutes' | 'studentRoutes') =>
  features.map((f) => ({ id: f.id, title: f.title, screens: f[pick]?.length ?? 0 }));

export const routes: RouteObject[] = [
  { path: '/', element: <HomePage />, errorElement: <RouteError /> },
  {
    path: '/gv',
    element: <AreaLayout area="gv" title="Giáo viên" nav={features.flatMap((f) => f.teacherNav ?? [])} />,
    errorElement: <RouteError />,
    children: [
      { index: true, element: <AreaOverview area="gv" items={overview('teacherRoutes')} /> },
      ...childRoutes('teacherRoutes'),
    ],
  },
  {
    path: '/hs',
    element: <AreaLayout area="hs" title="Học sinh" nav={features.flatMap((f) => f.studentNav ?? [])} />,
    errorElement: <RouteError />,
    children: [
      { index: true, element: <AreaOverview area="hs" items={overview('studentRoutes')} /> },
      ...childRoutes('studentRoutes'),
    ],
  },
  { path: '*', element: <NotFound /> },
];

export const router = createBrowserRouter(routes);
