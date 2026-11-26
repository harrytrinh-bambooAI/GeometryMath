/**
 * Danh mục theme giao diện (T-261126-50). Nguồn: Design/styles/STYLES.md v0.1 (26/11/2026).
 * Giá trị token của từng theme ở src/styles/tokens.css, khối [data-theme='<id>'].
 * Mặc định D "Bảng điều khiển gọn" (DECISIONS 26/11, lệnh owner).
 *
 * Tệp này thuần (không chạm DOM) để chạy được bằng `node --test`.
 */
export type ThemeId = 'a' | 'b' | 'c' | 'd';

export interface ThemeInfo {
  id: ThemeId;
  name: string;
  /** Một dòng mô tả cho ô chọn trong Cài đặt. */
  summary: string;
}

export const DEFAULT_THEME: ThemeId = 'c';

/** Thứ tự hiện trong Cài đặt: mặc định lên đầu. */
export const THEMES: readonly ThemeInfo[] = [
  { id: 'c', name: 'Giấy kẻ ô', summary: 'Nền giấy ô li như vở toán. Mặc định.' },
  { id: 'd', name: 'Bảng điều khiển gọn', summary: 'Gọn, nhiều thông tin trên một màn.' },
  { id: 'a', name: 'Lớp học sáng', summary: 'Sáng, nút to, bo tròn. Thân thiện cho học sinh.' },
  { id: 'b', name: 'Bảng đen tối', summary: 'Nền tối như bảng đen. Hợp trình chiếu trên lớp.' },
];

/** Khóa localStorage (dự phòng khi chưa đăng nhập hoặc máy chủ không trả lời). index.html đọc cùng khóa. */
export const THEME_STORAGE_KEY = 'geo.theme';

export function isThemeId(v: unknown): v is ThemeId {
  return typeof v === 'string' && THEMES.some((t) => t.id === v);
}

/** Giá trị lạ hoặc rỗng -> theme mặc định. */
export function normalizeTheme(v: unknown): ThemeId {
  return isThemeId(v) ? v : DEFAULT_THEME;
}
