import type { FeatureModule } from '../types.ts';
import { CodeLoginPage } from './CodeLoginPage.tsx';
import { FirstPasswordPage } from './FirstPasswordPage.tsx';
import { RecoverPage } from './RecoverPage.tsx';
import { RegisterPage } from './RegisterPage.tsx';
import './s2e1.css';

/**
 * S2-E1 — Đăng ký, đăng nhập bằng mã và khôi phục (PO-61, T-261126-18). Màn C3-01 theo Design/prototype/s23b.js, s23d.js.
 * Cùng bộ màn ở hai vùng (vai mặc định theo vùng; đăng nhập xong vào Kho đề hoặc "Bài của em" theo vai của tài khoản):
 *   /gv|/hs/dang-ky        Tạo tài khoản (tên + vai + mật khẩu + tick thông báo riêng tư) → màn mã
 *   /gv|/hs/dang-nhap-ma   Đăng nhập bằng mã người dùng + mật khẩu
 *   /gv|/hs/khoi-phuc      Lấy lại tài khoản bằng mã khôi phục
 *   /gv|/hs/dat-mat-khau   Đặt mật khẩu mới lần đầu (tài khoản chuyển từ MVP)
 * Luồng MVP (/gv/dang-nhap mã giáo viên, /hs/vao-lop) giữ nguyên (PLAN A7). Không thêm mục rail (màn trước đăng nhập).
 */
const routes = (area: 'gv' | 'hs') => [
  { path: 'dang-ky', element: <RegisterPage area={area} /> },
  { path: 'dang-nhap-ma', element: <CodeLoginPage area={area} /> },
  { path: 'khoi-phuc', element: <RecoverPage area={area} /> },
  { path: 'dat-mat-khau', element: <FirstPasswordPage area={area} /> },
];

export const feature: FeatureModule = {
  id: 's2e1',
  title: 'Đăng ký, đăng nhập bằng mã và khôi phục',
  teacherRoutes: routes('gv'),
  studentRoutes: routes('hs'),
  teacherNav: [],
  studentNav: [],
};
