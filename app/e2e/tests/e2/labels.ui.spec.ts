import type { Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { API, BG, MSG, PV, bearer, devToken, signInPage } from '../e1/e1.ts';
import { PV_NEEDS_REVIEW, iphone } from './e2.ts';

/** US-GEO-201-SC3 (thẻ ràng buộc), US-GEO-203-SC6 (một chuỗi nhãn), US-GEO-203-FC4 (giáo viên không đặt nhãn). */

async function openBg1(page: Page) {
  await page.goto(`/hs/bai/${BG('BG-1')}`);
  await expect(page.getByTestId('e1-assignment-page')).toBeVisible();
  await expect(page.getByTestId('e2-scene')).toBeVisible();
}

test('Thẻ ràng buộc hiện ở góc hình @TC-GEO-0202', async ({ page, request }) => {
  await signInPage(page, request, 'F-GEO-020', '11A1');
  await openBg1(page);
  const chips = page.getByTestId('e2-scene-chips');
  await expect(chips.locator('.chip')).toHaveText(['SA = a', 'AD = a']);
  // góc dưới trái của khung hình
  const f = (await page.getByTestId('e2-scene').boundingBox())!;
  const c = (await chips.boundingBox())!;
  expect(c.x - f.x).toBeLessThan(f.width / 2);
  expect(c.y + c.height).toBeGreaterThan(f.y + f.height / 2);
});

/**
 * Một chuỗi nhãn (BR-GEO-136): mọi thẻ nhãn máy trên màn là đúng "Đã kiểm chứng" hoặc "Cần xem lại", có ít nhất một
 * thẻ, và không có biến thể chữ khác (soát E2 #8: bản cũ chỉ cấm một biến thể nên gần như luôn đúng).
 */
async function noWrongLabel(page: Page) {
  const text = await page.locator('body').innerText();
  for (const bad of ['Hình đã kiểm chứng', 'Chưa kiểm chứng', 'Đã xác minh', 'Đã được kiểm chứng', 'Verified', 'Needs review']) {
    expect(text, bad).not.toContain(bad);
  }
  const labels = (await page.locator('.chip').allInnerTexts()).map((s) => s.trim()).filter((s) => /kiểm chứng|xem lại/i.test(s));
  expect(labels.length).toBeGreaterThan(0);
  for (const l of labels) expect(['Đã kiểm chứng', 'Cần xem lại']).toContain(l);
}

test('Một chuỗi nhãn trên mọi màn @TC-GEO-0218', async ({ page, request, browser }, testInfo) => {
  // T-261126-43: test duy nhất đi 4 màn trên 2 thiết bị (2 ngữ cảnh trình duyệt, 3 trang có video + trace). Mỗi bước đã
  // chờ theo DOM; riêng tổng thời gian (kể cả tháo ngữ cảnh = ghi video) vượt 30 s khi máy tải cao (43 s, load ~40,
  // 26/11) nên nhân 3 hạn của RIÊNG test này (test.slow), không nới assertion.
  test.slow();
  // Giáo viên mẫu: thẻ đề ở Kho đề và trang chi tiết đề
  await signInPage(page, request, 'F-GEO-003');
  await page.goto('/gv/kho-de');
  await expect(page.getByTestId('e1-problem-HKG-01').locator('.chip', { hasText: 'kiểm chứng' })).toHaveText('Đã kiểm chứng');
  await noWrongLabel(page);
  await page.goto(`/gv/de/${PV('HKG-01')}`);
  await expect(page.getByTestId('e2-problem-label')).toHaveText('Đã kiểm chứng');
  await noWrongLabel(page);
  // An: BG-1 ở màn Làm bài trên iPad (thiết bị của dự án đang chạy: ui-webkit = iPad) và iPhone
  const phone = await iphone(browser);
  try {
    for (const p of [await page.context().newPage(), await phone.newPage()]) {
      await signInPage(p, request, 'F-GEO-020', '11A1');
      await openBg1(p);
      await expect(p.getByTestId('e1-assignment-page-label')).toHaveText('Đã kiểm chứng');
      await expect(p.getByTestId('e2-scene-label')).toHaveText('Đã kiểm chứng');
      await noWrongLabel(p);
      await testInfo.attach(`bg1-${p.context() === phone ? 'iphone' : 'project'}.png`, { body: await p.screenshot(), contentType: 'image/png' });
      await p.close(); // trang phụ: ghi video trong thân test, không dồn vào lúc tháo ngữ cảnh
    }
  } finally {
    await phone.close();
  }
});

test('Giáo viên không đặt được nhãn máy @TC-GEO-0222', async ({ page, request }) => {
  await signInPage(page, request, 'F-GEO-003');
  await page.goto(`/gv/de/${PV_NEEDS_REVIEW}`);
  await expect(page.getByTestId('e2-problem-label')).toHaveText('Cần xem lại');
  // không có điều khiển đổi nhãn máy
  const detail = page.getByTestId('e2-problem-detail');
  await expect(detail.getByRole('button', { name: /kiểm chứng|nhãn|xem lại/i })).toHaveCount(0);
  await expect(detail.getByRole('combobox', { name: /nhãn/i })).toHaveCount(0);
  await expect(detail.getByRole('checkbox')).toHaveCount(0);
  // gửi thẳng yêu cầu đổi nhãn
  const t = await devToken(request, 'F-GEO-003');
  const r = await request.put(`${API}/api/v1/problems/${PV_NEEDS_REVIEW}/verify-status`, {
    headers: bearer(t),
    data: { verifyStatus: 'verified' },
  });
  expect(r.status()).toBe(403);
  const body = (await r.json()) as { code: string; message: string };
  expect(body.code).toBe('FORBIDDEN');
  expect(body.message).toBe(MSG['03']);
  await page.reload();
  await expect(page.getByTestId('e2-problem-label')).toHaveText('Cần xem lại');
});
