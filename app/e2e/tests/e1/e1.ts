import { createHash } from 'node:crypto';
import { mkdirSync, rmSync, statSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import type { APIRequestContext, Page, TestInfo } from '@playwright/test';
import { expect, fixtureIds } from '../../support/fixtures.ts';

/**
 * Tiện ích chung cho test E2E của E1. Dữ liệu từ app/db/seed/fixture-ids.json (seed vàng, bản 22/11/2026).
 * Máy chủ chạy profile dev: có /dev/token và móc kiểm thử /test-hooks/* (PO-51; không có ở prod).
 */
type Ids = typeof fixtureIds & {
  problems: Record<string, { id: string; versionId: string }>;
  assignments: Record<string, { id: string }>;
  teacherCodes: Record<string, string>;
};
export const ids = fixtureIds as Ids;

export const ORG_A = ids.orgs['F-GEO-001']!.id;
export const ORG_C = ids.orgs['F-GEO-002']!.id;
export const BG = (k: string) => ids.assignments[k]!.id;
export const PV = (code: string) => ids.problems[code]!.versionId;
export const USER = (ref: string) => ids.users[ref]!.id;
export const CLASS = (ref: string) => ids.classes[ref]!.id;
export const CODE = ids.teacherCodes;

/** Máy chủ API (test UI chạy với baseURL là web; /test-hooks không qua proxy Vite nên gọi thẳng). */
export const API = process.env.GEO_E2E_BASE_URL ?? `http://127.0.0.1:${process.env.GEO_PORT ?? 8080}`;

export const MSG = {
  '02': 'Phiên đã hết. Vui lòng vào lại.',
  '03': 'Bạn không có quyền thực hiện thao tác này.',
  '04': 'Không tìm thấy nội dung này.',
  '05': 'Danh sách đã thay đổi. Vui lòng tải lại từ đầu.',
  '09': 'Em hãy nhập tên hiển thị.',
  '10': 'Tên hiển thị tối đa 60 ký tự.',
  '11': 'Mã lớp không đúng. Em hỏi lại giáo viên nhé.',
  '12': 'Mã giáo viên không đúng.',
  '14': 'Trợ lý đang bận. Em thử lại sau ít phút nhé.',
  '59': 'Đã nhập sai mã quá nhiều lần. Vui lòng thử lại sau ít phút.',
} as const;

/** Địa chỉ IP riêng cho mỗi test (X-Forwarded-For; máy chủ dev tin tiêu đề này) để bộ đếm thử sai không giẫm nhau. */
export function ipFor(testInfo: TestInfo, salt = ''): string {
  const h = createHash('sha256').update(`${testInfo.titlePath.join('/')}|${testInfo.project.name}|${salt}`).digest();
  return `10.${h[0]}.${h[1]}.${h[2]! % 250 + 1}`;
}

/** Làn kiểm thử cho chế độ của nhà cung cấp AI giả lập (tiêu đề X-Geo-Test-Lane). */
export const lane = (testInfo: TestInfo) => `${testInfo.project.name}-${testInfo.workerIndex}`;

export async function devToken(request: APIRequestContext, userRef: string, ttlSeconds?: number): Promise<string> {
  const u = ids.users[userRef]!;
  const res = await request.post(`${API}/dev/token`, {
    data: { orgId: ids.orgs[u.org!]!.id, userId: u.id, role: u.role, ttlSeconds },
  });
  expect(res.status()).toBe(200);
  return ((await res.json()) as { token: string }).token;
}

export const bearer = (t: string) => ({ Authorization: `Bearer ${t}` });

/** Đăng nhập sẵn trên trình duyệt bằng thẻ (cho ca không kiểm chính luồng đăng nhập). */
export async function signInPage(page: Page, request: APIRequestContext, userRef: string, className?: string): Promise<void> {
  const token = await devToken(request, userRef);
  const u = ids.users[userRef]!;
  const me = { role: u.role, userId: u.id, displayName: u.displayName, className };
  await page.addInitScript(
    ([t, m]) => {
      sessionStorage.setItem('geo.token', t);
      sessionStorage.setItem('geo.e1.me', m);
    },
    [token, JSON.stringify(me)] as const,
  );
}

/**
 * Đặt lại bộ đếm thử sai mã giáo viên THEO PHẠM VI (móc PO-51): chỉ các địa chỉ IP và tài khoản ca kiểm dùng,
 * không xóa toàn bảng (TC-GEO-0171/0179 chạy song song với ca UI trên chromium/webkit).
 */
export async function resetAuthFailures(
  request: APIRequestContext,
  scope: { ips?: string[]; userIds?: string[] },
): Promise<void> {
  expect((await request.post(`${API}/test-hooks/auth-failures/reset`, { data: scope })).status()).toBe(200);
}

/**
 * Khóa liên tiến trình (thư mục tạo nguyên tử) cho tài nguyên dùng chung giữa các worker Playwright, ví dụ bộ đếm
 * thử sai theo tài khoản F-GEO-003: TC-GEO-0179 dò mã tài khoản này còn TC-GEO-0111/0112 đăng nhập đúng bằng nó.
 */
/** Khóa cho mọi ca sinh lượt gọi AI của An (F-GEO-020): TC-GEO-0120/0122 đếm đúng số bản ghi lượt gọi. */
export const AN_AI_LOCK = 'ai-F-GEO-020';

export async function withLock<T>(name: string, fn: () => Promise<T>, timeoutMs = 60_000): Promise<T> {
  const dir = join(tmpdir(), `geo-e2e-lock-${process.env.GEO_E2E_RUN ?? 'local'}-${name}`);
  const deadline = Date.now() + timeoutMs;
  for (;;) {
    try {
      mkdirSync(dir);
      break;
    } catch (e) {
      if ((e as NodeJS.ErrnoException).code !== 'EEXIST') throw e;
      try {
        if (Date.now() - statSync(dir).mtimeMs > timeoutMs) rmSync(dir, { recursive: true, force: true }); // khóa mồ côi
      } catch {
        /* vừa được nhả */
      }
      if (Date.now() > deadline) throw new Error(`Chờ khóa ${name} quá ${timeoutMs} ms`);
      await new Promise((r) => setTimeout(r, 100));
    }
  }
  try {
    return await fn();
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
}

export async function setAi(request: APIRequestContext, body: Record<string, unknown>): Promise<void> {
  expect((await request.post(`${API}/test-hooks/ai`, { data: body })).status()).toBe(200);
}

export async function leaveClass(request: APIRequestContext, joinCode: string, displayName: string): Promise<void> {
  await request.post(`${API}/test-hooks/students/leave`, { data: { joinCode, displayName } });
}

export async function studentJoinUI(page: Page, name: string, code: string): Promise<void> {
  await page.goto('/hs/vao-lop');
  await page.getByTestId('e1-join-name').fill(name);
  await page.getByTestId('e1-join-code').fill(code);
  await page.getByTestId('e1-join-submit').click();
}
