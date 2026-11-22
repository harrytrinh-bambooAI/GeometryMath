import type { RouteObject } from 'react-router';

/** Mục trên thanh dọc (rail) của vùng Giáo viên / Học sinh. */
export interface NavItem {
  /** Đường dẫn tương đối trong vùng, ví dụ 'kho-de' → /gv/kho-de. */
  to: string;
  /** Nhãn tiếng Việt ngắn (≤ 12 ký tự để vừa rail 76px). */
  label: string;
  /** data-testid của mục, theo quy ước tid(): ví dụ 'e2-nav-kho-de'. */
  testId: string;
}

/**
 * Mỗi epic cắm vào khung qua MỘT đối tượng này, khai báo trong `src/features/<epic>/index.tsx`.
 * Khung tự gắn route và mục rail; epic không sửa tệp của khung hay của epic khác.
 * - teacherRoutes: con của /gv (path tương đối, không có dấu / đầu).
 * - studentRoutes: con của /hs.
 */
export interface FeatureModule {
  id: 'e1' | 'e2' | 'e3' | 'e4' | 'e5' | 'e6' | 'e7';
  title: string;
  teacherRoutes?: RouteObject[];
  studentRoutes?: RouteObject[];
  teacherNav?: NavItem[];
  studentNav?: NavItem[];
}
