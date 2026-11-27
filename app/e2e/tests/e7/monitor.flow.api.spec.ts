import type { Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { captureFinalState } from '../../support/final-state.ts';
import { call } from '../e4/e4.ts';
import { API, NAMES, asg, box, freshMonitor, golden, monitor, row, signInAs, st } from './e7.ts';
import type { Box } from './e7.ts';

/**
 * US-GEO-701, 702, ca LOẠI B của `monitor.ui.spec.ts` (DECISIONS 27/11 "Chiến lược E2E", đợt 2 E2E-SPLIT2): bảng, thẻ
 * tổng và nhóm câu hỏi của màn Theo dõi lớp do ca loại A 0700, 0701, 0702 giữ; hai ca này chỉ kiểm dữ liệu (tình trạng
 * sau khi mở lời giải, không gọi AI) bằng gọi API đúng thứ tự bước, cuối luồng mở màn Theo dõi lớp (không click) và chụp
 * 1 ảnh. Mã TC giữ nguyên trong tiêu đề.
 */

const openMonitor = (b: Box, key = 'BG-1') => async (page: Page) => {
  await signInAs(page, b.teacher, 'Giáo viên mẫu', 'teacher');
  await page.goto(`/gv/theo-doi/${asg(b, key)}`);
  await expect(page.getByTestId('e7-monitor')).toBeVisible();
};

test('Tình trạng sau khi mở lời giải @TC-GEO-0703 @loai-B', async ({ request, browser }, testInfo) => {
  const b = await box(request, golden());
  const id = asg(b, 'BG-1');
  // Giáo viên mẫu "Mở ngay" BG-1; An xem lời giải; Dũng chưa xem
  const r = await call.release(request, b.teacher.token, id, { openNow: true });
  expect(r.status(), await r.text()).toBe(200);
  expect((await call.solution(request, st(b, 'An').token, id)).status()).toBe(200);
  const m = await freshMonitor(request, b.teacher.token, id);
  expect(NAMES.map((n) => row(m, n).status)).toEqual(['Đã xem lời giải', 'Đang làm', 'Tự làm, không cần gợi ý', 'Đang làm', 'Chưa bắt đầu']);
  await captureFinalState(browser, testInfo, async (page) => {
    await openMonitor(b)(page);
    await expect(page.getByTestId('e7-lock')).toHaveCount(0);
  });
});

test('Không có nhận định do AI viết @TC-GEO-0720 @loai-B', async ({ request, browser }, testInfo) => {
  const b = await box(request, golden());
  const callsOf = async (userId: string) =>
    ((await (await request.get(`${API}/test-hooks/ai/calls?userId=${userId}&limit=100`)).json()) as unknown[]).length;
  const n = await callsOf(b.teacher.id);
  const m = await monitor(request, b.teacher.token, asg(b, 'BG-1'));
  expect(m.questionGroups).toHaveLength(4);
  const text = JSON.stringify({ groups: m.questionGroups, asked: m.askedQuestions });
  for (const s of ['nên', 'vướng', 'gợi ý cho giáo viên', 'AI']) expect(text).not.toContain(s);
  expect(await callsOf(b.teacher.id)).toBe(n);
  for (const name of NAMES) expect(await callsOf(st(b, name).id)).toBe(0);
  await captureFinalState(browser, testInfo, async (page) => {
    await openMonitor(b)(page);
    await expect(page.getByTestId('e7-group')).toHaveCount(4);
    const asking = await page.getByTestId('e7-asking').innerText();
    for (const s of ['nên', 'vướng', 'gợi ý cho giáo viên', 'AI']) expect(asking).not.toContain(s);
  });
  // mở trang cũng không gọi AI
  expect(await callsOf(b.teacher.id)).toBe(n);
});
