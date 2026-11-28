import type { APIRequestContext, Page } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { API, PV, bearer } from '../e1/e1.ts';

/**
 * Tiện ích E2E của E5 (Kho đề của Giáo viên mẫu). Dữ liệu: app/db/seed/e2e/500_e5_catalog.sql — kho thử F-GEO-080
 * (48 đề HKG-01…48, 44 "Đã kiểm chứng") chỉ hai tổ chức thử E5 thấy, để Kho đề của các trường thật giữ sáu đề (E1):
 * - LANE: "Nhóm thử E5" + Giáo viên mẫu — mọi ca đọc kho thử (không đổi dữ liệu).
 * - LANE_LOAD: "Nhóm thử E5 nạp thêm" + Giáo viên mẫu — thấy thêm bộ 'kho_moi' mà móc /test-hooks/e5/problems nạp đề
 *   mới vào giữa chừng (TC-GEO-0533, 0513).
 */
export { API, PV, bearer };

const e5Id = (n: string) => `00000000-0000-7000-8000-${n}`;
export const LANE = { org: e5Id('000000e50001'), teacher: e5Id('000000e50003') };
export const LANE_LOAD = { org: e5Id('000000e50002'), teacher: e5Id('000000e50004') };
/** Version của đề thử HKG-k (k = 7…48). */
export const PV_TEST = (k: number) => e5Id(`0000e52${String(k).padStart(5, '0')}`);

export const MSG = {
  '01': 'Thông tin chưa hợp lệ. Vui lòng kiểm tra lại các ô được đánh dấu.',
  '03': 'Bạn không có quyền thực hiện thao tác này.',
  '04': 'Không tìm thấy nội dung này.',
  '05': 'Danh sách đã thay đổi. Vui lòng tải lại từ đầu.',
  '40': 'Không có đề nào khớp bộ lọc. Hãy bỏ bớt điều kiện.',
  '54': 'Đề này chưa có lời giải.',
} as const;

/** Dữ liệu vàng F-GEO-031, 037 (HKG-01). */
export const F031 = 'Cho hình chóp S.ABCD, đáy là hình vuông cạnh a, SA ⊥ (ABCD), SA = a. Tính góc giữa SD và mặt đáy.';
export const F031_HINTS = [
  'Góc giữa đường thẳng và mặt phẳng là góc giữa đường đó và hình chiếu của nó trên mặt phẳng.',
  'Hình chiếu của S lên mặt đáy là điểm nào? Từ đó suy ra hình chiếu của SD.',
];
export const F037 = [
  'SA ⊥ (ABCD) nên A là hình chiếu của S. Hình chiếu của SD là AD.',
  'Góc giữa SD và (ABCD) là góc SDA. Tam giác SAD vuông tại A.',
  'tan SDA = SA / AD = a / a = 1, suy ra góc SDA = 45°.',
];
export const ALL_CODES = Array.from({ length: 48 }, (_, i) => `HKG-${String(i + 1).padStart(2, '0')}`);

export type Item = { code: string; level: string; solidKind: string; topicSlug: string; versionId: string; verifyStatus: string; label: string; aiAuthored: boolean; stepCount: number | null };
export type PageBody = { items: Item[]; hasMore: boolean; nextCursor: string | null };

export async function laneToken(request: APIRequestContext, lane = LANE): Promise<string> {
  const r = await request.post(`${API}/dev/token`, { data: { orgId: lane.org, userId: lane.teacher, role: 'TEACHER' } });
  expect(r.status()).toBe(200);
  return ((await r.json()) as { token: string }).token;
}

/** Đăng nhập Giáo viên mẫu của làn E5 trên trình duyệt (thẻ qua /dev/token). */
export async function signInLane(page: Page, request: APIRequestContext, lane = LANE): Promise<void> {
  const token = await laneToken(request, lane);
  const me = { role: 'TEACHER', userId: lane.teacher, displayName: 'Giáo viên mẫu' };
  await page.addInitScript(
    ([t, m]) => {
      sessionStorage.setItem('geo.token', t);
      sessionStorage.setItem('geo.e1.me', m);
    },
    [token, JSON.stringify(me)] as const,
  );
}

/** Gọi danh sách Kho đề tới hết; trả các trang. */
export async function allPages(request: APIRequestContext, token: string, query = ''): Promise<PageBody[]> {
  const pages: PageBody[] = [];
  let cursor: string | null = null;
  do {
    const q = [query, cursor ? `cursor=${encodeURIComponent(cursor)}` : ''].filter(Boolean).join('&');
    const r = await request.get(`${API}/api/v1/catalog/problems${q ? `?${q}` : ''}`, { headers: bearer(token) });
    expect(r.status(), await r.text()).toBe(200);
    const p = (await r.json()) as PageBody;
    pages.push(p);
    cursor = p.nextCursor;
    expect(pages.length, 'không vòng vô hạn').toBeLessThan(10);
  } while (cursor);
  return pages;
}

export const cards = (page: Page) => page.locator('.e5-card');

/** Mở Kho đề và chờ trang đầu. */
export async function openBank(page: Page) {
  await page.goto('/gv/kho-de');
  await expect(page.getByTestId('e5-summary')).toContainText('đề ·');
  await expect(page.getByTestId('e5-list')).toHaveAttribute('data-loading', 'false');
}

/** Chọn một giá trị lọc và chờ danh sách tải xong. */
export async function filterBy(page: Page, group: 'level' | 'solid' | 'topic', value: string) {
  await page.getByTestId(`e5-filter-${group}-${value || 'all'}`).click();
  await expect(page.getByTestId(`e5-filter-${group}-${value || 'all'}`)).toHaveAttribute('aria-pressed', 'true');
  await expect(page.getByTestId('e5-list')).toHaveAttribute('data-loading', 'false');
}

/** Bấm "Tải thêm" và chờ số thẻ tăng. */
export async function loadMore(page: Page) {
  const before = await cards(page).count();
  await page.getByTestId('e5-load-more').click();
  await expect.poll(async () => cards(page).count()).toBeGreaterThan(before);
  await expect(page.getByTestId('e5-list')).toHaveAttribute('data-loading', 'false');
}

export const codesOn = async (page: Page) => cards(page).evaluateAll((els) => els.map((e) => e.getAttribute('data-code') ?? ''));

/** Trang không phải cuộn ngang. */
export async function noHorizontalScroll(page: Page) {
  const [sw, cw] = await page.evaluate(() => [document.documentElement.scrollWidth, document.documentElement.clientWidth]);
  expect(sw, 'không cuộn ngang').toBeLessThanOrEqual(cw + 1);
}
