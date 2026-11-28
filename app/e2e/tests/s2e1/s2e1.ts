import { createHash } from 'node:crypto';
import type { APIRequestContext, Page, TestInfo } from '@playwright/test';
import { expect, fixtureIds } from '../../support/fixtures.ts';
import { API } from '../e1/e1.ts';

export { API };

/**
 * Hỗ trợ chung cho ca S2-E1 (T-261126-18). Tài khoản mẫu FX-01…FX-03 (fixture-ids.d/s2e1.json) chỉ dùng cho ca
 * KHÔNG làm bẩn; ca khóa, khôi phục, đổi mật khẩu tự đăng ký tài khoản riêng qua `register()` với một địa chỉ IP
 * riêng (X-Forwarded-For, máy chủ dev tin tiêu đề này) để giới hạn 5 lần đăng ký/giờ/IP không giẫm nhau.
 */
type FxUser = { id: string; displayName: string; userCode: string; password: string; recoveryCode: string; role: string };
export const FX = (ref: 'F-S2-001' | 'F-S2-002' | 'F-S2-003') =>
  (fixtureIds.users as unknown as Record<string, FxUser>)[ref]!;

export type Err = { code: string; message: string; requestId?: string; details?: Array<{ field: string; message: string }> };

export type Registered = {
  token: string;
  role: 'TEACHER' | 'STUDENT';
  userId: string;
  displayName: string;
  userCode: string;
  recoveryCode: string;
};

export const MSG = {
  login: 'Mã hoặc mật khẩu chưa đúng',
  recovery: 'Mã người dùng hoặc mã khôi phục chưa đúng',
  locked: 'Tài khoản tạm khóa, thử lại sau 15 phút',
  nameEmpty: 'Hãy nhập tên hiển thị.',
  nameLong: 'Tên hiển thị tối đa 60 ký tự.',
  password: 'Mật khẩu từ 8 đến 64 ký tự.',
  expired: 'Phiên đã hết. Vui lòng vào lại.',
} as const;

export const USER_CODE = /^(GV|HS)-[A-HJ-NP-Z2-9]{4}-[A-HJ-NP-Z2-9]{4}$/;
export const RECOVERY_CODE = /^RC-[A-HJ-NP-Z2-9]{4}-[A-HJ-NP-Z2-9]{4}-[A-HJ-NP-Z2-9]{4}$/;

/** IP riêng cho mỗi test (và mỗi dự án Playwright), dải 10.200+ để không trùng ipFor của E1. */
export function ipOf(testInfo: TestInfo, salt = ''): string {
  const h = createHash('sha256').update(`s2e1|${testInfo.titlePath.join('/')}|${testInfo.project.name}|${salt}`).digest();
  return `10.${200 + (h[0]! % 50)}.${h[1]}.${(h[2]! % 250) + 1}`;
}

export const xff = (ip: string) => ({ 'X-Forwarded-For': ip });

/** Đăng ký qua API (trả cả phản hồi để ca kiểm mã lỗi). */
export async function registerRaw(
  request: APIRequestContext,
  ip: string,
  body: Record<string, unknown>,
) {
  return request.post(`${API}/api/v1/auth/register`, { headers: xff(ip), data: body });
}

export async function register(
  request: APIRequestContext,
  ip: string,
  opts: { name?: string; role?: 'teacher' | 'student'; password?: string } = {},
): Promise<Registered & { password: string }> {
  const password = opts.password ?? 'MatKhau#2026';
  const r = await registerRaw(request, ip, {
    displayName: opts.name ?? 'Học sinh thử',
    role: opts.role ?? 'student',
    password,
    privacyAck: true,
    privacyVersion: '1.0',
  });
  expect(r.status(), await r.text()).toBe(200);
  return { ...((await r.json()) as Registered), password };
}

export async function login(request: APIRequestContext, userCode: string, password: string) {
  return request.post(`${API}/api/v1/auth/code-login`, { data: { userCode, password } });
}

export async function recover(request: APIRequestContext, userCode: string, recoveryCode: string, newPassword: string) {
  return request.post(`${API}/api/v1/auth/recover`, { data: { userCode, recoveryCode, newPassword } });
}

/** Giả lập "đã chờ đủ Retry-After" cho bộ đếm đăng ký của một IP (móc dev/test). */
export async function ageRegisterLimit(request: APIRequestContext, ip: string, seconds: number) {
  expect((await request.post(`${API}/test-hooks/s2e1/register-limit/age`, { data: { ip, seconds } })).status()).toBe(200);
}

/** Giả lập phiên hết hạn ở máy chủ cho một mã người dùng (móc dev/test). */
export async function revokeSessions(request: APIRequestContext, userCode: string) {
  expect((await request.post(`${API}/test-hooks/s2e1/revoke-sessions`, { data: { userCode } })).status()).toBe(200);
}

/** Trang dùng IP riêng (mọi yêu cầu từ trình duyệt đi qua proxy của web kèm X-Forwarded-For). */
export async function useIp(page: Page, ip: string) {
  await page.setExtraHTTPHeaders(xff(ip));
}

/** Điền và gửi màn Đăng ký (đã tick thông báo riêng tư nếu ack). */
export async function fillRegister(
  page: Page,
  v: { role: 'teacher' | 'student'; name: string; password: string; ack?: boolean },
) {
  await page.getByTestId(`s2e1-role-${v.role}`).click();
  await page.getByTestId('s2e1-name').fill(v.name);
  await page.getByTestId('s2e1-password').fill(v.password);
  if (v.ack !== false) await page.getByTestId('s2e1-privacy').check();
}

export async function loginUI(page: Page, area: 'gv' | 'hs', userCode: string, password: string) {
  await page.goto(`/${area}/dang-nhap-ma`);
  await page.getByTestId('s2e1-login-code').fill(userCode);
  await page.getByTestId('s2e1-login-password').fill(password);
  await page.getByTestId('s2e1-login-submit').click();
}

/** Đưa tài khoản về trạng thái "vừa chuyển từ MVP": mật khẩu tạm, phải đặt mật khẩu mới (móc dev/test). */
export async function setTempPassword(request: APIRequestContext, userCode: string, tempPassword: string) {
  const r = await request.post(`${API}/test-hooks/s2e1/temp-password`, { data: { userCode, tempPassword } });
  expect(r.status()).toBe(200);
  expect(((await r.json()) as { updated: number }).updated).toBe(1);
}
