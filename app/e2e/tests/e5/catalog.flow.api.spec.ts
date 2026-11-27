import { expect, test } from '../../support/fixtures.ts';
import { captureFinalState } from '../../support/final-state.ts';
import { API, bearer, devToken, signInPage } from '../e1/e1.ts';
import { MSG } from './e5.ts';

/**
 * US-GEO-501, ca LOẠI B của `catalog.ui.spec.ts` (DECISIONS 27/11 "Chiến lược E2E", đợt 2 E2E-SPLIT2): màn Kho đề và
 * thanh điều hướng do các ca loại A cùng tệp giữ; ca này chỉ kiểm quyền bằng gọi API, cuối luồng mở "Bài của em" của học
 * sinh (không click) để thấy thanh bên không có mục Kho đề và chụp 1 ảnh. Mã TC giữ nguyên trong tiêu đề.
 */

test('Học sinh gọi Kho đề: FORBIDDEN, không có mục Kho đề @TC-GEO-0507 @loai-B', async ({ request, browser }, testInfo) => {
  const an = await devToken(request, 'F-GEO-020');
  const r = await request.get(`${API}/api/v1/catalog/problems`, { headers: bearer(an) });
  expect(r.status()).toBe(403);
  const body = await r.text();
  expect((JSON.parse(body) as { code: string }).code).toBe('FORBIDDEN');
  expect(body).not.toMatch(/HKG-|"items"/);
  expect((JSON.parse(body) as { message: string }).message).toBe(MSG['03']);
  await captureFinalState(browser, testInfo, async (page) => {
    await signInPage(page, request, 'F-GEO-020', '11A1');
    await page.goto('/hs/bai-cua-em');
    await expect(page.getByTestId('hs-rail')).toBeVisible();
    await expect(page.getByTestId('hs-rail')).not.toContainText('Kho đề');
  });
});
