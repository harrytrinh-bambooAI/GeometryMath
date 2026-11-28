import { clearToken, setToken } from '../../shared/auth/session.ts';

/**
 * Người đang đăng nhập (chỉ để hiển thị: "Chào An", tên giáo viên ở góc màn). Quyền luôn do máy chủ kiểm
 * qua thẻ; thông tin ở đây không bao giờ được gửi lên làm căn cứ phân quyền (BR-GEO-111).
 */
export interface Me {
  role: 'STUDENT' | 'TEACHER';
  userId: string;
  displayName: string;
  className?: string;
}

export interface LoginResponse extends Me {
  token: string;
}

const KEY = 'geo.e1.me';

export function saveLogin(r: LoginResponse): void {
  setToken(r.token);
  const me: Me = { role: r.role, userId: r.userId, displayName: r.displayName, className: r.className };
  try {
    sessionStorage.setItem(KEY, JSON.stringify(me));
  } catch {
    /* chế độ riêng tư: bỏ qua */
  }
}

export function getMe(): Me | null {
  try {
    const v = sessionStorage.getItem(KEY);
    return v ? (JSON.parse(v) as Me) : null;
  } catch {
    return null;
  }
}

export function logout(): void {
  clearToken();
  try {
    sessionStorage.removeItem(KEY);
  } catch {
    /* bỏ qua */
  }
}
