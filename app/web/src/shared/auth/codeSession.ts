import { markCodeLogin, setToken } from './session.ts';

/**
 * Phiên đăng nhập bằng mã người dùng (S2-E1, PO-61). Ghi thẻ và thông tin hiển thị cùng chỗ với luồng MVP
 * (khóa 'geo.e1.me' mà màn "Bài của em", Kho đề đọc để hiện "Chào An"), và đánh dấu phiên mã để khi hết
 * phiên thì về màn "Đăng nhập" bằng mã (US-S2E1-02-FC5). Quyền luôn do máy chủ kiểm qua thẻ.
 */
export interface CodeSession {
  token: string;
  role: 'TEACHER' | 'STUDENT';
  userId: string;
  displayName: string;
  className?: string | null;
  userCode: string;
}

const ME_KEY = 'geo.e1.me';

export function saveCodeLogin(s: CodeSession): void {
  setToken(s.token);
  markCodeLogin(s.role === 'TEACHER' ? 'gv' : 'hs');
  const me = { role: s.role, userId: s.userId, displayName: s.displayName, className: s.className ?? undefined };
  try {
    sessionStorage.setItem(ME_KEY, JSON.stringify(me));
  } catch {
    /* chế độ riêng tư: bỏ qua */
  }
}

/** Màn đầu tiên sau khi vào: giáo viên vào Kho đề, học sinh vào "Bài của em". */
export function homeOf(role: 'TEACHER' | 'STUDENT'): string {
  return role === 'TEACHER' ? '/gv/kho-de' : '/hs/bai-cua-em';
}
