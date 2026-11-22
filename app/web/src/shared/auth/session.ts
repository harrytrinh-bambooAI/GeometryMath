/**
 * Phiên đăng nhập phía trình duyệt: chỉ giữ thẻ JWT do máy chủ cấp (sessionStorage, mất khi đóng tab).
 * Luồng đăng nhập thật (mã giáo viên, vào lớp) do epic E1 làm; ở đây chỉ có chỗ lưu và sự kiện.
 */
const KEY = 'geo.token';
export const UNAUTHENTICATED_EVENT = 'geo:unauthenticated';

export function getToken(): string | null {
  try {
    return sessionStorage.getItem(KEY);
  } catch {
    return null;
  }
}

export function setToken(token: string): void {
  try {
    sessionStorage.setItem(KEY, token);
  } catch {
    /* chế độ riêng tư: bỏ qua */
  }
}

export function clearToken(): void {
  try {
    sessionStorage.removeItem(KEY);
  } catch {
    /* bỏ qua */
  }
}

/** Gọi khi máy chủ trả 401: xóa thẻ, báo cho khung app (khung tự chuyển về trang chủ). */
export function signalUnauthenticated(): void {
  clearToken();
  window.dispatchEvent(new CustomEvent(UNAUTHENTICATED_EVENT));
}
