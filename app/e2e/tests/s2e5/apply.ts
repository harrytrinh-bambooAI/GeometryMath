import type { APIRequestContext, Page, TestInfo } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { API, bearer } from '../e1/e1.ts';

/**
 * Tiện ích E2E của US-S2E5-05 (T-261126-22): lời giải đã giao bất biến, "Áp dụng cho lớp". Dữ liệu:
 * app/db/seed/e2e/840_s2e3_core.sql — theo (TC, làn), n = TC × 10 + làn: sổ lời giải F-S2-(843000000 + n) có v2
 * "Giáo viên đã duyệt" (45°) ghim vào BG-1 (11A1, khóa tới T0 + 2 giờ; TC 60 đã mở và 2 học sinh đã xem) và BG-2
 * (11A2, đã mở). Chủ sổ Cô Trang F-S2-840003; An (11A1) F-S2-840021; Bình (11A2) F-S2-840023; Cô Lan F-S2-840002.
 */
export { API, bearer };

export const ORG = '00000000-0000-7000-8002-000000840001';
export const TRANG = '00000000-0000-7000-8002-000000840003';
export const LAN = '00000000-0000-7000-8002-000000840002';
export const AN = '00000000-0000-7000-8002-000000840021';
export const BINH = '00000000-0000-7000-8002-000000840023';
const LANES = ['api', 'ui-chromium', 'ui-webkit', 'ui-iphone', 'mobile-iphone', 'mobile-pixel'];
const lane = (info: TestInfo) => Math.max(0, LANES.indexOf(info.project.name));
const f = (n: number) => `00000000-0000-7000-8002-${String(n).padStart(12, '0')}`;
const n = (tc: number, info: TestInfo) => tc * 10 + lane(info);

export const doc = (tc: number, info: TestInfo) => f(843000000 + n(tc, info));
export const bg1 = (tc: number, info: TestInfo) => f(845000000 + n(tc, info) * 10 + 1);
export const bg2 = (tc: number, info: TestInfo) => f(845000000 + n(tc, info) * 10 + 2);

export const S1 = 'SA ⊥ (ABCD) nên AC là hình chiếu của SC, góc cần tìm là SCA.';
export const S2 = 'ABCD vuông cạnh a nên AC = a√2.';
export const V2_S3 = 'tan SCA = a√2 / a√2 = 1.';
export const V2_S4 = 'Vậy góc giữa SC và mặt phẳng đáy bằng 45°.';
export const V3_S3 = 'Tam giác SAC vuông cân tại A nên tan SCA = 1.';
export const V3 = [S1, S2, V3_S3, V2_S4];

export async function tokenFor(request: APIRequestContext, userId: string, role: 'TEACHER' | 'STUDENT' = 'TEACHER') {
  const r = await request.post(`${API}/dev/token`, { data: { orgId: ORG, userId, role } });
  expect(r.status()).toBe(200);
  return ((await r.json()) as { token: string }).token;
}

export interface Doc {
  latestVersion: number;
  version: { version: number; steps: Array<{ text: string }> };
  approve: { allowed: boolean; approved: boolean };
  assignments: Array<{ className: string; version: number; assignmentId: string; viewers: number; locked: boolean }>;
}

const url = (d: string, tail = '') => `${API}/api/v1/teacher/solutions/${d}${tail}`;
export const applyUrl = (d: string, assignment: string) => url(d, `/assignments/${assignment}/apply`);

export async function view(request: APIRequestContext, t: string, d: string, version?: number): Promise<Doc> {
  const r = await request.get(url(d, version ? `?version=${version}` : ''), { headers: bearer(t) });
  expect(r.status(), await r.text()).toBe(200);
  return (await r.json()) as Doc;
}

/** Lưu v3 từ v2; {@code approve} = duyệt luôn (gửi xác nhận nhãn máy Cần xem lại). */
export async function v3(request: APIRequestContext, t: string, d: string, steps = V3, approve = true): Promise<Doc> {
  const s = await request.post(url(d, '/versions'), { headers: bearer(t), data: { baseVersion: 2, steps } });
  expect(s.status(), await s.text()).toBe(200);
  if (approve) {
    const a = await request.post(url(d, '/approve'), { headers: bearer(t), data: { version: 3, confirmNeedsReview: true } });
    expect(a.status(), await a.text()).toBe(200);
  }
  return view(request, t, d);
}

export const pinned = (d: Doc, className: string) => d.assignments.find((a) => a.className === className)?.version;

/** Đăng nhập trình duyệt bằng thẻ dev rồi mở một màn. */
export async function signIn(page: Page, request: APIRequestContext, userId: string, role: 'TEACHER' | 'STUDENT', path: string) {
  const token = await tokenFor(request, userId, role);
  const me = { role, userId, displayName: role === 'TEACHER' ? 'Cô Trang' : 'An', className: role === 'STUDENT' ? '11A1' : undefined };
  await page.addInitScript(
    ([tk, m]) => {
      sessionStorage.setItem('geo.token', tk);
      sessionStorage.setItem('geo.e1.me', m);
    },
    [token, JSON.stringify(me)] as const,
  );
  await page.goto(path);
}
