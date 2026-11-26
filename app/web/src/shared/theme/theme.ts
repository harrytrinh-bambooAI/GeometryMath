import { api } from '../api/client.ts';
import { getToken } from '../auth/session.ts';
import { DEFAULT_THEME, THEME_STORAGE_KEY, normalizeTheme } from './themes.ts';
import type { ThemeId } from './themes.ts';

/**
 * Áp và lưu theme (T-261126-50).
 * - Áp: đặt `data-theme` trên <html>; token trong src/styles/tokens.css đổi theo.
 * - Lưu: theo tài khoản phía máy chủ (GET/PUT /api/v1/me/settings) khi đã đăng nhập; localStorage làm dự phòng
 *   (trước đăng nhập, mất mạng). index.html áp giá trị localStorage trước khi React chạy để không nháy màu.
 * - Đã đăng nhập: giá trị của máy chủ thắng giá trị localStorage.
 */
export const THEME_EVENT = 'geo:theme';

export function currentTheme(): ThemeId {
  return normalizeTheme(document.documentElement.dataset.theme);
}

export function applyTheme(id: ThemeId): void {
  document.documentElement.dataset.theme = id;
  window.dispatchEvent(new CustomEvent<ThemeId>(THEME_EVENT, { detail: id }));
}

export function readLocalTheme(): ThemeId {
  try {
    return normalizeTheme(localStorage.getItem(THEME_STORAGE_KEY));
  } catch {
    return DEFAULT_THEME;
  }
}

function writeLocalTheme(id: ThemeId): void {
  try {
    localStorage.setItem(THEME_STORAGE_KEY, id);
  } catch {
    /* chế độ riêng tư: bỏ qua */
  }
}

interface SettingsDto {
  theme: string;
}

let syncedToken: string | null = null;
/** Tăng mỗi lần người dùng tự chọn: kết quả GET đến sau lần chọn không được ghi đè lựa chọn đó. */
let choiceVersion = 0;

/**
 * Lấy theme của tài khoản một lần cho mỗi thẻ đăng nhập (khung gọi khi vào vùng /gv, /hs và sau đăng nhập).
 * Lỗi mạng/máy chủ: giữ theme đang áp (localStorage hoặc mặc định).
 */
export async function syncAccountTheme(): Promise<void> {
  const token = getToken();
  if (!token || token === syncedToken) return;
  syncedToken = token;
  const version = choiceVersion;
  try {
    const s = await api.get<SettingsDto>('/api/v1/me/settings');
    if (getToken() !== token || version !== choiceVersion) return;
    const id = normalizeTheme(s.theme);
    writeLocalTheme(id);
    if (id !== currentTheme()) applyTheme(id);
  } catch {
    syncedToken = null; // thử lại ở lần điều hướng sau
  }
}

/** Kết quả lưu: 'account' = đã lưu theo tài khoản; 'local' = chỉ lưu trên máy này (chưa đăng nhập hoặc lỗi). */
export type SaveResult = 'account' | 'local';

export async function saveTheme(id: ThemeId): Promise<SaveResult> {
  choiceVersion += 1;
  applyTheme(id);
  writeLocalTheme(id);
  if (!getToken()) return 'local';
  try {
    await api.put<SettingsDto>('/api/v1/me/settings', { theme: id });
    return 'account';
  } catch {
    return 'local';
  }
}
