import { expect, test } from '../../support/fixtures.ts';
import { API, bearer, devToken, signInPage } from '../e1/e1.ts';
import { iphone } from '../e2/e2.ts';
import { ALL_CODES, F031, MSG, cards, codesOn, filterBy, loadMore, noHorizontalScroll, openBank, signInLane } from './e5.ts';

/** US-GEO-501 (xem, lọc), US-GEO-502 (nhãn máy, "AI soạn"), US-GEO-505 (tải theo trang) trên kho thử F-GEO-080. */

const SUMMARY = '48 đề · 44 đã kiểm chứng · tài liệu của Giáo viên mẫu';

test('Mở Kho đề: dòng đếm, 20 thẻ, Tải thêm, lọc ở Tất cả @TC-GEO-0500 @smoke', async ({ page, request }) => {
  await signInLane(page, request);
  await openBank(page);
  await expect(page.getByTestId('e5-summary')).toHaveText(SUMMARY);
  await expect(cards(page)).toHaveCount(20);
  await expect(page.getByTestId('e5-load-more')).toBeVisible();
  for (const g of ['level', 'solid', 'topic']) {
    await expect(page.getByTestId(`e5-filter-${g}-all`)).toHaveAttribute('aria-pressed', 'true');
    await expect(page.getByTestId(`e5-filter-${g}`).locator('[aria-pressed="true"]')).toHaveCount(1);
  }
});

test('Lọc Cấp THPT và Dạng bài Góc: 8 đề có HKG-01 @TC-GEO-0501 @smoke', async ({ page, request }) => {
  await signInLane(page, request);
  await openBank(page);
  await filterBy(page, 'level', 'thpt');
  await filterBy(page, 'topic', 'angle');
  await expect(cards(page)).toHaveCount(8);
  expect(await codesOn(page)).toContain('HKG-01');
  for (const c of await cards(page).all()) {
    await expect(c).toHaveAttribute('data-level', 'thpt');
    await expect(c).toHaveAttribute('data-topic', 'angle');
    await expect(c).toContainText('THPT');
    await expect(c).toContainText('Góc');
  }
  await expect(page.getByTestId('e5-load-more')).toHaveCount(0);
});

test('Lọc theo hình chóp: 8 đề @TC-GEO-0502', async ({ page, request }) => {
  await signInLane(page, request);
  await openBank(page);
  await filterBy(page, 'solid', 'pyramid');
  await expect(cards(page)).toHaveCount(8);
  for (const c of await cards(page).all()) await expect(c).toHaveAttribute('data-solid', 'pyramid');
});

test('Dòng đếm không đổi theo bộ lọc; bỏ lọc trở lại 20 thẻ @TC-GEO-0503', async ({ page, request }) => {
  await signInLane(page, request);
  await openBank(page);
  await filterBy(page, 'level', 'thpt');
  await filterBy(page, 'topic', 'angle');
  await expect(cards(page)).toHaveCount(8);
  await expect(page.getByTestId('e5-summary')).toHaveText(SUMMARY);
  await filterBy(page, 'level', '');
  await filterBy(page, 'topic', '');
  await expect(cards(page)).toHaveCount(20);
  await expect(page.getByTestId('e5-summary')).toHaveText(SUMMARY);
  await expect(page.getByTestId('e5-load-more')).toBeVisible();
});

test('Nội dung thẻ HKG-01 @TC-GEO-0504 @TC-GEO-0510', async ({ page, request }) => {
  await signInLane(page, request);
  await openBank(page);
  const card = page.getByTestId('e1-problem-HKG-01');
  await expect(page.getByTestId('e5-thumb-HKG-01')).toHaveAttribute('data-kind', 'pyramid');
  await expect(page.getByTestId('e5-level-HKG-01')).toHaveText('THPT');
  await expect(page.getByTestId('e5-topic-HKG-01')).toHaveText('Góc');
  await expect(page.getByTestId('e5-label-HKG-01')).toHaveText('Đã kiểm chứng');
  await expect(page.getByTestId('e5-open-HKG-01')).toHaveText(F031);
  await expect(page.getByTestId('e5-authored-HKG-01')).toHaveText('AI soạn · 3 bước');
  await expect(page.getByTestId('e1-problem-HKG-01-assign')).toHaveText('Giao bài');
  await expect(card.locator('.e5-label')).toHaveCount(1); // đúng một nhãn máy (BR-GEO-207)
});

test('Kho đề trên điện thoại: một cột, lọc THCS, Giao bài bấm được @TC-GEO-0505', async ({ browser, request }) => {
  const ctx = await iphone(browser);
  try {
    const page = await ctx.newPage();
    await signInLane(page, request);
    await openBank(page);
    await filterBy(page, 'level', 'thcs');
    await expect(cards(page)).toHaveCount(20);
    const xs = new Set<number>();
    for (const c of await cards(page).all()) {
      await expect(c).toHaveAttribute('data-level', 'thcs');
      xs.add(Math.round((await c.boundingBox())!.x));
    }
    expect([...xs], 'các thẻ xếp một cột').toHaveLength(1);
    await noHorizontalScroll(page);
    const first = (await codesOn(page))[0]!;
    await page.getByTestId(`e1-problem-${first}-assign`).click();
    await expect(page).toHaveURL(/\/gv\/giao-bai\//);
  } finally {
    await ctx.close();
  }
});

test('Lọc không có kết quả: THCS + Mặt cầu @TC-GEO-0506', async ({ page, request }) => {
  await signInLane(page, request);
  await openBank(page);
  await filterBy(page, 'level', 'thcs');
  await filterBy(page, 'topic', 'sphere');
  await expect(cards(page)).toHaveCount(0);
  await expect(page.getByTestId('e5-empty')).toHaveText(MSG['40']);
  await expect(page.getByTestId('e5-load-more')).toHaveCount(0);
});

test('Học sinh gọi Kho đề: FORBIDDEN, không có mục Kho đề @TC-GEO-0507', async ({ page, request }) => {
  const an = await devToken(request, 'F-GEO-020');
  const r = await request.get(`${API}/api/v1/catalog/problems`, { headers: bearer(an) });
  expect(r.status()).toBe(403);
  const body = await r.text();
  expect((JSON.parse(body) as { code: string }).code).toBe('FORBIDDEN');
  expect(body).not.toMatch(/HKG-|"items"/);
  expect((JSON.parse(body) as { message: string }).message).toBe(MSG['03']);
  await signInPage(page, request, 'F-GEO-020', '11A1');
  await page.goto('/hs/bai-cua-em');
  await expect(page.getByTestId('hs-rail')).toBeVisible();
  await expect(page.getByTestId('hs-rail')).not.toContainText('Kho đề');
});

test('Không có tìm kiếm, Thêm đề, soạn, import, Thiết diện @TC-GEO-0508', async ({ page, request }) => {
  await signInLane(page, request);
  await openBank(page);
  const bank = page.getByTestId('e1-catalog');
  await expect(bank.locator('input')).toHaveCount(0);
  await expect(page.getByRole('searchbox')).toHaveCount(0);
  await expect(page.getByRole('button', { name: /Thêm đề|Soạn|Import|Nhập đề/i })).toHaveCount(0);
  await expect(page.getByRole('link', { name: /Thêm đề|Soạn|Import|Nhập đề/i })).toHaveCount(0);
  await expect(page.getByTestId('e5-filter-topic')).not.toContainText('Thiết diện');
  await expect(page.getByTestId('e5-filter-topic')).not.toContainText('Độ dài');
  await expect(page.getByTestId('e5-filter-topic').getByRole('button')).toHaveText(['Tất cả', 'Góc', 'Khoảng cách', 'Thể tích', 'Diện tích', 'Mặt cầu']);
});

test('Đề cần xem lại: nhãn khác màu, vẫn AI soạn, Giao bài bấm được @TC-GEO-0511', async ({ page, request }) => {
  await signInLane(page, request);
  await openBank(page);
  await loadMore(page);
  await loadMore(page);
  const bad = page.getByTestId('e5-label-HKG-45');
  await expect(bad).toHaveText('Cần xem lại');
  await expect(bad).toHaveAttribute('data-status', 'needs_review');
  const bgBad = await bad.evaluate((e) => getComputedStyle(e).backgroundColor);
  const bgOk = await page.getByTestId('e5-label-HKG-01').evaluate((e) => getComputedStyle(e).backgroundColor);
  expect(bgBad).not.toBe(bgOk);
  await expect(page.getByTestId('e5-authored-HKG-45')).toContainText('AI soạn');
  await page.getByTestId('e1-problem-HKG-45-assign').click();
  await expect(page).toHaveURL(/\/gv\/giao-bai\//);
});

test('Giáo viên C: kho và lời giải đứng tên Giáo viên mẫu @TC-GEO-0512', async ({ page, request }) => {
  await signInPage(page, request, 'F-GEO-005');
  await openBank(page);
  await expect(page.getByTestId('e5-summary')).toContainText('tài liệu của Giáo viên mẫu');
  await page.getByTestId('e5-open-HKG-01').click();
  await expect(page.getByTestId('e5-solution-author')).toHaveText('AI soạn · tài liệu của Giáo viên mẫu');
});

test('Không có "Giáo viên đã duyệt" trên 48 thẻ và trang chi tiết @TC-GEO-0514', async ({ page, request }) => {
  await signInLane(page, request);
  await openBank(page);
  await loadMore(page);
  await loadMore(page);
  await expect(cards(page)).toHaveCount(48);
  await expect(page.locator('body')).not.toContainText('Giáo viên đã duyệt');
  await page.getByTestId('e5-open-HKG-01').click();
  await expect(page.getByTestId('e2-problem-detail')).toBeVisible();
  await expect(page.locator('body')).not.toContainText('Giáo viên đã duyệt');
  await expect(page.getByRole('button', { name: /duyệt/i })).toHaveCount(0);
});

test('Tải lần lượt hết kho: 20, 20, 8; mỗi mã đúng một lần @TC-GEO-0532 @TC-GEO-0537', async ({ page, request }) => {
  await signInLane(page, request);
  await openBank(page);
  await expect(cards(page)).toHaveCount(20);
  await loadMore(page);
  await expect(cards(page)).toHaveCount(40);
  const last = page.waitForResponse((r) => r.url().includes('/api/v1/catalog/problems') && r.url().includes('cursor='));
  await loadMore(page);
  const lastBody = (await (await last).json()) as { items: unknown[]; hasMore: boolean; nextCursor: string | null };
  expect(lastBody.items).toHaveLength(8);
  expect(lastBody.hasMore).toBe(false);
  await expect(cards(page)).toHaveCount(48);
  const codes = await codesOn(page);
  expect([...codes].sort()).toEqual(ALL_CODES);
  await expect(page.getByTestId('e5-load-more')).toHaveCount(0);
  await expect(page.getByTestId('e5-error')).toHaveCount(0);
  await expect(page.getByRole('alert')).toHaveCount(0);
});

test('Đổi bộ lọc giữa chừng: làm lại từ đầu, 20 đề THCS @TC-GEO-0538', async ({ page, request }) => {
  await signInLane(page, request);
  await openBank(page);
  await loadMore(page);
  await expect(cards(page)).toHaveCount(40);
  await filterBy(page, 'level', 'thcs');
  await expect(cards(page)).toHaveCount(20);
  await expect(page.locator('.e5-card[data-level="thpt"]')).toHaveCount(0);
});

test('Con trỏ hỏng: màn hiện MSG-GEO-05 @TC-GEO-0539', async ({ page, request }) => {
  await signInLane(page, request);
  // trang đầu trả con trỏ hỏng F-GEO-084 thay cho con trỏ thật: bấm "Tải thêm" gửi con trỏ đó lên máy chủ
  await page.route('**/api/v1/catalog/problems', async (route) => {
    const res = await route.fetch();
    const body = (await res.json()) as { nextCursor: string | null };
    await route.fulfill({ response: res, json: { ...body, nextCursor: 'khong-phai-con-tro' } });
  });
  await openBank(page);
  const bad = page.waitForResponse((r) => r.url().includes('cursor=khong-phai-con-tro'));
  await page.getByTestId('e5-load-more').click();
  expect((await bad).status()).toBe(400);
  await expect(page.getByTestId('e5-error')).toHaveText(MSG['05']);
  await expect(page.getByTestId('e5-error')).toHaveAttribute('data-error-code', 'INVALID_CURSOR');
  await expect(cards(page)).toHaveCount(20); // không thêm đề nào
});
