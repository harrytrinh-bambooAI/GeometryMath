import type { APIRequestContext, Page, TestInfo } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { API, BG, MSG, USER, lane, setAi, signInPage, studentJoinUI } from './e1.ts';

/**
 * US-GEO-103 (cổng AI: khóa không xuống trình duyệt, lỗi → MSG-GEO-14 + "Thử lại") và US-GEO-104-FC1 (bài lớp khác).
 * Chế độ lỗi của nhà cung cấp giả lập đặt theo (An, làn) để test song song không giẫm nhau.
 */
test.describe.configure({ mode: 'serial' });

const AN = USER('F-GEO-020');
const Q = 'Vì sao A là hình chiếu của S?'; // F-GEO-055

async function openBg1AsAn(page: Page, request: APIRequestContext, testInfo: TestInfo) {
  await page.setExtraHTTPHeaders({ 'X-Geo-Test-Lane': lane(testInfo) });
  await signInPage(page, request, 'F-GEO-020', '11A1');
  await page.goto(`/hs/bai/${BG('BG-1')}`);
  await expect(page.getByTestId('e1-qa')).toBeVisible();
}

async function calls(request: APIRequestContext) {
  const r = await request.get(`${API}/test-hooks/ai/calls?userId=${AN}&limit=100`);
  return (await r.json()) as Array<{ status: string; model: string; purpose: string }>;
}

test.afterEach(async ({ request }, testInfo) => {
  await setAi(request, { mode: 'ok', userId: AN, lane: lane(testInfo) });
});

test('Khóa của nhà cung cấp không xuống trình duyệt @TC-GEO-0118', async ({ page, request }, testInfo) => {
  const bodies: string[] = [];
  page.on('response', async (r) => {
    try {
      bodies.push(`${r.url()}\n${await r.text()}`);
    } catch {
      /* phản hồi không có thân (chuyển hướng) */
    }
  });
  await page.setExtraHTTPHeaders({ 'X-Geo-Test-Lane': lane(testInfo) });
  await studentJoinUI(page, 'An', 'HK7M2A');
  await page.getByTestId('e1-assignment-HKG-01').click();
  await page.getByTestId('e1-qa-input').fill(Q);
  await page.getByTestId('e1-qa-send').click();
  await expect(page.getByTestId('e1-qa-answer')).toHaveCount(1);
  // toàn bộ dữ liệu trả về + mã trang (HTML, JS, CSS) đã tải về thiết bị
  bodies.push(await page.content());
  for (const src of await page.evaluate(() => performance.getEntriesByType('resource').map((e) => e.name))) {
    if (/\.(js|css|html)(\?|$)/.test(src)) bodies.push(await (await request.get(src)).text());
  }
  expect(bodies.length).toBeGreaterThan(3);
  expect(bodies.filter((b) => b.includes('KHOA-THU-KHONG-LO'))).toEqual([]);
  // Phần "một lượt dựng hình tự học" cần màn Tự học của E2: ghi ở qc_findings mục Dev (QF-E1-D03).
});

test('Nhà cung cấp trả lỗi @TC-GEO-0120', async ({ page, request }, testInfo) => {
  await openBg1AsAn(page, request, testInfo);
  await setAi(request, { mode: 'error', userId: AN, lane: lane(testInfo) });
  await page.getByTestId('e1-qa-input').fill(Q);
  const res = page.waitForResponse((r) => r.url().includes('/questions'));
  await page.getByTestId('e1-qa-send').click();
  expect((await res).status()).toBeGreaterThanOrEqual(500);
  await expect(page.getByTestId('e1-qa-error')).toContainText(MSG['14']);
  await expect(page.getByTestId('e1-qa-retry')).toHaveText('Thử lại');
  await expect(page.getByTestId('e1-qa-answer')).toHaveCount(0);
  await expect(page.getByTestId('e1-assignment-page-label')).toHaveText('Đã kiểm chứng');
  expect((await calls(request))[0]).toMatchObject({ status: 'error', purpose: 'qa' });
});

test('Bấm "Thử lại" sau lỗi @TC-GEO-0122', async ({ page, request }, testInfo) => {
  await openBg1AsAn(page, request, testInfo);
  await setAi(request, { mode: 'error', userId: AN, lane: lane(testInfo) });
  await page.getByTestId('e1-qa-input').fill(Q);
  await page.getByTestId('e1-qa-send').click();
  await expect(page.getByTestId('e1-qa-error')).toContainText(MSG['14']);
  const errorCalls = (await calls(request)).filter((c) => c.status === 'error').length;
  // nhà cung cấp giả lập trở lại bình thường; An bấm "Thử lại" (không gõ lại)
  await setAi(request, { mode: 'ok', userId: AN, lane: lane(testInfo) });
  const req = page.waitForRequest((r) => r.url().includes('/questions') && r.method() === 'POST');
  await page.getByTestId('e1-qa-retry').click();
  expect((JSON.parse((await req).postData() ?? '{}') as { question: string }).question).toBe(Q);
  await expect(page.getByTestId('e1-qa-answer')).toHaveCount(1);
  await expect(page.getByTestId('e1-qa-error')).toHaveCount(0);
  const after = await calls(request);
  expect(after[0]!.status).toBe('ok');
  expect(after.filter((c) => c.status === 'error').length).toBeGreaterThanOrEqual(errorCalls);
});

test('Học sinh gọi bài của lớp khác cùng trường @TC-GEO-0128', async ({ page, request }) => {
  await signInPage(page, request, 'F-GEO-020', '11A1');
  const res = page.waitForResponse((r) => r.url().includes(`/api/v1/student/assignments/${BG('BG-4')}`));
  await page.goto(`/hs/bai/${BG('BG-4')}`);
  const r = await res;
  expect(r.status()).toBe(404);
  const body = await r.text();
  expect(JSON.parse(body)).toMatchObject({ code: 'NOT_FOUND' });
  for (const leak of ['Hình nón', 'đường sinh', 'Diện tích xung quanh', 'release', 'HKG-04']) expect(body).not.toContain(leak);
  await expect(page.getByTestId('e1-assignment-page-error')).toHaveText(MSG['04']);
});
