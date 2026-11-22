import { devices } from '@playwright/test';
import type { APIRequestContext, Browser, BrowserContext, Page, TestInfo } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { API, USER, lane, signInPage } from '../e1/e1.ts';

/**
 * Tiện ích E2E của E2 (Hình đúng). Câu thông điệp, đề của dữ liệu vàng (artifacts/GEO/work/fixture.md, 22/11/2026).
 * Ca sinh lượt gọi AI của An giữ khóa AN_AI_LOCK (dùng chung với E1) để ca đếm lượt gọi chính xác.
 */
export const AN = USER('F-GEO-020');

export const MSG16 = 'Hình này do AI dựng và chưa qua kiểm tra bằng tính toán. Em hãy đối chiếu với đề.';
export const MSG17 = 'Hiện chưa hỗ trợ hình học phẳng. Em hãy nhập đề hình học không gian.';
export const MSG18 = 'Chưa dựng được hình từ đề này. Em kiểm tra lại đề hoặc chọn một hình mẫu.';
export const MSG19 = 'Giá trị phải là số lớn hơn 0.';

export const F061 = 'Cho hình bát diện đều cạnh a. Tính khoảng cách giữa hai mặt đối diện.';
export const F064 = 'Cho tam giác ABC vuông tại A, AB = 3, AC = 4. Tính BC.';
export const HKG01 = 'Cho hình chóp S.ABCD, đáy là hình vuông cạnh a, SA ⊥ (ABCD), SA = a. Tính góc giữa SD và mặt đáy.';
/** Version 99 (không hiện hành) của HKG-06, nhãn "Cần xem lại" (seed 200_e2_core.sql). */
export const PV_NEEDS_REVIEW = '00000000-0000-7000-8000-000000299036';

export const CARDS = ['Hình chóp', 'Lăng trụ', 'Hình hộp', 'Hình nón', 'Hình trụ', 'Hình cầu'];
export const CARD_CODE: Record<string, string> = {
  'Hình chóp': 'pyramid',
  'Lăng trụ': 'prism',
  'Hình hộp': 'box',
  'Hình nón': 'cone',
  'Hình trụ': 'cylinder',
  'Hình cầu': 'sphere',
};

/** Đặt kịch bản trả lời "scene" của AI giả lập cho (An, làn); '' = bỏ. */
export async function setScenario(request: APIRequestContext, testInfo: TestInfo, scenario: string): Promise<void> {
  const r = await request.post(`${API}/test-hooks/ai`, { data: { userId: AN, lane: lane(testInfo), scenario } });
  expect(r.status()).toBe(200);
}

export async function selfStudyCount(request: APIRequestContext, userId = AN): Promise<number> {
  const r = await request.get(`${API}/test-hooks/e2/self-study-count?userId=${userId}`);
  expect(r.status()).toBe(200);
  return ((await r.json()) as { count: number }).count;
}

export async function openSelfStudy(page: Page, request: APIRequestContext, testInfo: TestInfo): Promise<void> {
  await page.setExtraHTTPHeaders({ 'X-Geo-Test-Lane': lane(testInfo) });
  await signInPage(page, request, 'F-GEO-020', '11A1');
  await page.goto('/hs/tu-hoc');
  await expect(page.getByTestId('e2-templates').getByRole('button')).toHaveCount(6);
}

/** Ngữ cảnh iPhone (F-GEO-101) trên trình duyệt của dự án đang chạy (không đổi loại trình duyệt). */
export async function iphone(browser: Browser): Promise<BrowserContext> {
  const { defaultBrowserType: _ignored, ...d } = devices['iPhone 13'];
  void _ignored;
  return browser.newContext({ ...d, locale: 'vi-VN', timezoneId: 'Asia/Ho_Chi_Minh' });
}

/** Mọi giá trị trong JSON (đệ quy) để kiểm "không có giá trị tính". */
export function walk(v: unknown, visit: (key: string, value: unknown) => void, key = ''): void {
  if (Array.isArray(v)) v.forEach((x) => walk(x, visit, key));
  else if (v && typeof v === 'object') for (const [k, x] of Object.entries(v)) walk(x, visit, k);
  else visit(key, v);
}
