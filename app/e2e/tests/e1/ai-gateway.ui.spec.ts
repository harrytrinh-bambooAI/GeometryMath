import type { APIRequestContext, Page, TestInfo } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { AN_AI_LOCK, API, BG, MSG, USER, bearer, devToken, lane, setAi, signInPage, studentJoinUI, withLock } from './e1.ts';

/**
 * US-GEO-103 (cổng AI: khóa không xuống trình duyệt, lỗi → MSG-GEO-14 + "Thử lại") và US-GEO-104-FC1 (bài lớp khác).
 * Chế độ lỗi của nhà cung cấp giả lập đặt theo (An, làn) để test song song không giẫm nhau; mọi ca sinh lượt gọi AI
 * của An giữ khóa AN_AI_LOCK để ca đếm bản ghi lượt gọi (TC-GEO-0120/0122) chính xác khi chromium/webkit/api chạy song song.
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
  // lượt gọi AI của An: giữ khóa chung để TC-GEO-0120/0122 (dự án khác) đếm bản ghi chính xác
  await withLock(AN_AI_LOCK, async () => {
    await page.getByTestId('e1-qa-send').click();
    await expect(page.getByTestId('e1-qa-answer')).toHaveCount(1);
    // một lượt dựng hình tự học (màn Tự học của E2; QF-E1-D02): đề F-GEO-060 qua cổng AI tác vụ "scene"
    await page.goto('/hs/tu-hoc');
    await page.getByTestId('e2-statement').fill(
      'Cho hình lăng trụ đứng ABC.A′B′C′ có đáy là tam giác vuông tại A, AB = 3, AC = 4, AA′ = 5. Tính khoảng cách từ A đến mặt phẳng (A′BC).',
    );
    await page.getByTestId('e2-build').click();
    await expect(page.getByTestId('e2-self-study-result')).toBeVisible();
  });
  // toàn bộ dữ liệu trả về + mã trang (HTML, JS, CSS) đã tải về thiết bị
  bodies.push(await page.content());
  for (const src of await page.evaluate(() => performance.getEntriesByType('resource').map((e) => e.name))) {
    if (/\.(js|css|html)(\?|$)/.test(src)) bodies.push(await (await request.get(src)).text());
  }
  expect(bodies.length).toBeGreaterThan(3);
  expect(bodies.filter((b) => b.includes('KHOA-THU-KHONG-LO'))).toEqual([]);
});

/** Câu hỏi mới nhất An đã lưu (id) — kiểm "không lưu gì" phía máy chủ. */
async function newestSavedQuestion(request: APIRequestContext): Promise<number | undefined> {
  const r = await request.get(`${API}/api/v1/students/${AN}/questions`, { headers: bearer(await devToken(request, 'F-GEO-020')) });
  expect(r.status()).toBe(200);
  return ((await r.json()) as { items: Array<{ id: number }> }).items[0]?.id;
}

test('Nhà cung cấp trả lỗi @TC-GEO-0120', async ({ page, request }, testInfo) => {
  await withLock(AN_AI_LOCK, async () => {
    await openBg1AsAn(page, request, testInfo);
    await setAi(request, { mode: 'error', userId: AN, lane: lane(testInfo) });
    const before = await calls(request);
    expect(before.length, 'danh sách lượt gọi chưa chạm trần 100').toBeLessThan(100);
    const savedBefore = await newestSavedQuestion(request);
    await page.getByTestId('e1-qa-input').fill(Q);
    const res = page.waitForResponse((r) => r.url().includes('/questions'));
    await page.getByTestId('e1-qa-send').click();
    expect((await res).status()).toBeGreaterThanOrEqual(500);
    await expect(page.getByTestId('e1-qa-error')).toContainText(MSG['14']);
    await expect(page.getByTestId('e1-qa-retry')).toHaveText('Thử lại');
    await expect(page.getByTestId('e1-qa-answer')).toHaveCount(0);
    await expect(page.getByTestId('e1-assignment-page-label')).toHaveText('Đã kiểm chứng');
    // đúng MỘT bản ghi lượt gọi mới, trạng thái lỗi; các bản ghi cũ giữ nguyên
    const after = await calls(request);
    expect(after).toHaveLength(before.length + 1);
    expect(after[0]).toMatchObject({ status: 'error', purpose: 'qa' });
    expect(after.slice(1)).toEqual(before);
    // phía máy chủ không lưu câu hỏi/câu trả lời dở dang nào
    expect(await newestSavedQuestion(request)).toBe(savedBefore);
  });
});

test('Bấm "Thử lại" sau lỗi @TC-GEO-0122', async ({ page, request }, testInfo) => {
  await withLock(AN_AI_LOCK, async () => {
    await openBg1AsAn(page, request, testInfo);
    await setAi(request, { mode: 'error', userId: AN, lane: lane(testInfo) });
    const start = await calls(request);
    expect(start.length, 'danh sách lượt gọi chưa chạm trần 100').toBeLessThan(99);
    await page.getByTestId('e1-qa-input').fill(Q);
    await page.getByTestId('e1-qa-send').click();
    await expect(page.getByTestId('e1-qa-error')).toContainText(MSG['14']);
    const afterError = await calls(request);
    expect(afterError).toHaveLength(start.length + 1);
    expect(afterError[0]).toMatchObject({ status: 'error', purpose: 'qa' });
    // nhà cung cấp giả lập trở lại bình thường; An bấm "Thử lại" (không gõ lại)
    await setAi(request, { mode: 'ok', userId: AN, lane: lane(testInfo) });
    const req = page.waitForRequest((r) => r.url().includes('/questions') && r.method() === 'POST');
    await page.getByTestId('e1-qa-retry').click();
    expect((JSON.parse((await req).postData() ?? '{}') as { question: string }).question).toBe(Q);
    await expect(page.getByTestId('e1-qa-answer')).toHaveCount(1);
    await expect(page.getByTestId('e1-qa-error')).toHaveCount(0);
    // thêm ĐÚNG một bản ghi thành công (không trùng); bản ghi lỗi trước đó vẫn còn nguyên
    const after = await calls(request);
    expect(after).toHaveLength(afterError.length + 1);
    expect(after[0]).toMatchObject({ status: 'ok', purpose: 'qa' });
    expect(after.slice(1)).toEqual(afterError);
    expect(after.filter((c) => c.status === 'ok').length).toBe(afterError.filter((c) => c.status === 'ok').length + 1);
  });
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
