import type { APIRequestContext, APIResponse, Page, TestInfo } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { API, bearer } from '../e1/e1.ts';

/**
 * Tiện ích E2E của S2-E6 (Kho đề, C2-01) và S2-E7 (Trình chiếu, C2-04). Dữ liệu: app/db/seed/e2e/850_s2e67_core.sql +
 * fixture-ids.d/s2e67.json. Mỗi (epic, TC, làn = dự án Playwright) có MỘT tổ chức riêng (Kho chung theo tổ chức):
 * gốc B = 850000000 + (epic − 6) × 5000000 + TC × 100000 + làn × 10000.
 */
export { API, bearer };

const LANES = ['api', 'ui-chromium', 'ui-webkit', 'ui-iphone', 'mobile-iphone', 'mobile-pixel'];
export const f = (n: number) => `00000000-0000-7000-8002-${String(n).padStart(12, '0')}`;
export const laneOf = (info: TestInfo) => Math.max(0, LANES.indexOf(info.project.name));

export interface Slot {
  org: string;
  trang: string;
  mai: string;
  lan: string;
  an: string;
  ha: string;
  class11A1: string;
  /** đề B+k */
  doc: (k: number) => string;
  /** đề xuất soát của đề B+k */
  sub: (k: number) => string;
  /** bản Kho chung của đề B+k */
  lib: (k: number) => string;
  bg: (k: number) => string;
}

export function slot(epic: 6 | 7, tc: number, info: TestInfo): Slot {
  const b = 850000000 + (epic - 6) * 5000000 + tc * 100000 + laneOf(info) * 10000;
  return {
    org: f(b + 1),
    trang: f(b + 2),
    mai: f(b + 3),
    lan: f(b + 4),
    an: f(b + 5),
    ha: f(b + 7),
    class11A1: f(b + 6),
    doc: (k) => f(b + k),
    sub: (k) => f(b + k + 3000),
    lib: (k) => f(b + k + 7000),
    bg: (k) => f(b + k),
  };
}

/** Mã đề theo seed: FX-10 = 100, FX-14 = 101, FX-13 = 102, FX-11 (AI soạn) = 103; Kho chung của Cô Lan 110…112. */
export const K = { FX10: 100, FX14: 101, FX13: 102, FX11_AI: 103, MAI_FX13: 104, FX12_NONE: 105, STEPS30: 106, LIB_FX11: 110, LIB_FX14: 111, LIB_PENDING: 112 };

export const TITLE = {
  FX10: 'PL-01 Góc A của tam giác 5, 12, 13',
  FX11: 'PL-02 Tam giác cân có góc A = 40°',
  FX12: 'PL-03 Đường chéo hình chữ nhật',
  FX13: 'PL-04 Diện tích tam giác đều cạnh 4',
  FX14: 'SP-01 Góc giữa SC và mặt đáy',
};

export const MSG = {
  emptyMine: 'Chưa có đề riêng',
  noMatch: 'Không có đề phù hợp',
  invalidCursor: 'Danh sách đã thay đổi. Vui lòng tải lại từ đầu.',
  approveFirst: 'Hãy duyệt lời giải trước',
  selfReview: 'Bạn không thể soát đề của chính mình',
  reasonRange: 'Lý do từ 5 đến 500 ký tự',
  pendingLimit: 'Tối đa 10 đề chờ soát cùng lúc',
  studentLocked: 'Lời giải mở theo bài giao của thầy cô',
};

export async function token(request: APIRequestContext, org: string, userId: string, role: 'TEACHER' | 'STUDENT', ttlSeconds?: number) {
  const res = await request.post(`${API}/dev/token`, { data: { orgId: org, userId, role, ttlSeconds } });
  expect(res.status()).toBe(200);
  return ((await res.json()) as { token: string }).token;
}

/** Đăng nhập sẵn trên trình duyệt bằng thẻ dev (ca không kiểm chính luồng đăng nhập). */
export async function signIn(page: Page, request: APIRequestContext, org: string, userId: string, displayName: string, ttlSeconds?: number) {
  const t = await token(request, org, userId, 'TEACHER', ttlSeconds);
  const me = { role: 'TEACHER', userId, displayName };
  await page.addInitScript(
    ([tk, m]) => {
      sessionStorage.setItem('geo.token', tk);
      sessionStorage.setItem('geo.e1.me', m);
    },
    [t, JSON.stringify(me)] as const,
  );
  return t;
}

export const lib = (path = '') => `${API}/api/v1/teacher/library${path}`;

export async function list(request: APIRequestContext, t: string, query: Record<string, string | number>): Promise<APIResponse> {
  const q = new URLSearchParams(Object.entries(query).map(([k, v]) => [k, String(v)])).toString();
  return request.get(`${lib()}?${q}`, { headers: bearer(t) });
}

export interface CardPage {
  items: Array<{ id: string; title: string; kind: string; shelf: string; status: string; statusLabel: string; machineLabel: string; humanLabel: string }>;
  nextCursor?: string | null;
  reviewer: boolean;
}

export async function ok<T>(r: APIResponse, status = 200): Promise<T> {
  expect(r.status(), await r.text()).toBe(status);
  return (await r.json()) as T;
}

export async function errorCode(r: APIResponse): Promise<{ code: string; message: string }> {
  return (await r.json()) as { code: string; message: string };
}

export const submit = (request: APIRequestContext, t: string, doc: string) =>
  request.post(lib(`/problems/${doc}/submit`), { headers: bearer(t), data: {} });
export const queue = (request: APIRequestContext, t: string) => request.get(lib('/review-queue'), { headers: bearer(t) });
export const accept = (request: APIRequestContext, t: string, sub: string) =>
  request.post(lib(`/review-queue/${sub}/accept`), { headers: bearer(t), data: {} });
export const reject = (request: APIRequestContext, t: string, sub: string, reason: string) =>
  request.post(lib(`/review-queue/${sub}/reject`), { headers: bearer(t), data: { reason } });
export const detail = (request: APIRequestContext, t: string, doc: string) => request.get(lib(`/problems/${doc}`), { headers: bearer(t) });
export const create = (request: APIRequestContext, t: string, i: number) =>
  request.post(lib('/problems'), { headers: bearer(t), data: { title: `Đề thêm ${i}`, statement: `Tam giác ABC có AB = ${i + 3}, AC = 4. Tính BC.` } });

/** Mở Kho đề của giáo viên (rail "Kho của tôi"). */
export async function openLibrary(page: Page) {
  await page.goto('/gv/kho-de-cua-toi');
  await expect(page.getByTestId('s2e6-page')).toBeVisible();
}

export const cards = (page: Page) => page.locator('[data-testid^="s2e6-card-"]').filter({ has: page.getByTestId('s2e6-card-title') });
export const cardTitles = async (page: Page) => cards(page).getByTestId('s2e6-card-title').allInnerTexts();
