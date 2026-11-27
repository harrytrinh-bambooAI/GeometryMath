import type { RouteObject } from 'react-router';
import type { IconName } from '../shared/ui/icons.ts';

/** Mục trên thanh dọc (rail) của vùng Giáo viên / Học sinh. */
export interface NavItem {
  /** Đường dẫn tương đối trong vùng, ví dụ 'kho-de' → /gv/kho-de. */
  to: string;
  /** Nhãn tiếng Việt ngắn (≤ 12 ký tự để vừa rail 76px). */
  label: string;
  /** data-testid của mục, theo quy ước tid(): ví dụ 'e2-nav-kho-de'. */
  testId: string;
  /** Icon trên thanh dọc (tên trong shared/ui/icons.ts); bỏ trống = icon chung 'layers'. */
  icon?: IconName;
}

/** Mã feature: MVP e1…e7; chặng 2 s2e1…s2e9; chặng 3 s3e1…s3e6 (chỗ cắm do T-261126-41 tạo sẵn); chặng 5 s5e1…s5e6 (T-261128-51). */
export type FeatureId =
  | 'e1' | 'e2' | 'e3' | 'e4' | 'e5' | 'e6' | 'e7'
  | 's2e1' | 's2e2' | 's2e3' | 's2e4' | 's2e5' | 's2e6' | 's2e7' | 's2e8' | 's2e9'
  | 's3e1' | 's3e2' | 's3e3' | 's3e4' | 's3e5' | 's3e6'
  | 's5e1' | 's5e2' | 's5e3' | 's5e4' | 's5e5' | 's5e6';

/**
 * Mỗi epic cắm vào khung qua MỘT đối tượng này, khai báo trong `src/features/<epic>/index.tsx`.
 * Khung tự gắn route và mục rail; epic không sửa tệp của khung hay của epic khác.
 * - teacherRoutes: con của /gv (path tương đối, không có dấu / đầu).
 * - studentRoutes: con của /hs.
 */
export interface FeatureModule {
  id: FeatureId;
  title: string;
  teacherRoutes?: RouteObject[];
  studentRoutes?: RouteObject[];
  teacherNav?: NavItem[];
  studentNav?: NavItem[];
}
