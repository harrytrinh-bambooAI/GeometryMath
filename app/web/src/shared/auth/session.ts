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

/**
 * Gọi khi máy chủ trả 401: xóa thẻ, báo cho khung app (khung tự chuyển về màn đăng nhập MVP của vùng).
 * Phiên đăng nhập bằng mã người dùng (S2-E1) thì về màn "Đăng nhập" bằng mã kèm câu "Phiên đã hết" (US-S2E1-02-FC5).
 */
export function signalUnauthenticated(): void {
  const area = takeCodeLoginArea();
  clearToken();
  if (area) {
    window.location.assign(`/${area}/dang-nhap-ma?het-phien=1`);
    return;
  }
  window.dispatchEvent(new CustomEvent(UNAUTHENTICATED_EVENT));
}

const MODE_KEY = 'geo.s2e1.area';

/** S2-E1: đánh dấu phiên hiện tại do đăng nhập bằng mã người dùng (vùng 'gv' hoặc 'hs'). */
export function markCodeLogin(area: 'gv' | 'hs'): void {
  try {
    sessionStorage.setItem(MODE_KEY, area);
  } catch {
    /* bỏ qua */
  }
}

/** Lấy và xóa dấu đăng nhập bằng mã (null nếu phiên MVP hoặc chưa đăng nhập). */
export function takeCodeLoginArea(): 'gv' | 'hs' | null {
  try {
    const v = sessionStorage.getItem(MODE_KEY);
    sessionStorage.removeItem(MODE_KEY);
    return v === 'gv' || v === 'hs' ? v : null;
  } catch {
    return null;
  }
}
