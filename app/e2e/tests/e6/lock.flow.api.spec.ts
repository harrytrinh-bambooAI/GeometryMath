import { expect, test } from '../../support/fixtures.ts';
import { captureFinalState } from '../../support/final-state.ts';
import { api, asg, signIn, stu, twoClasses } from './e6.ts';

/**
 * US-GEO-609, ca LOẠI B của `lock.ui.spec.ts` (DECISIONS 27/11 "Chiến lược E2E", đợt 2 E2E-SPLIT2): ô hỏi và nút gửi
 * trên màn Làm bài do các ca loại A cùng tệp giữ; ca này chỉ kiểm nội dung trả lời sau giờ mở bằng gọi API đúng thứ tự
 * bước, cuối luồng mở màn Làm bài (không click) và chụp 1 ảnh. Mã TC giữ nguyên trong tiêu đề.
 */

test('Sau giờ mở, trợ lý được nêu đáp số @TC-GEO-0670 @loai-B', async ({ request, browser }, testInfo) => {
  const sb = await twoClasses(request, [{ key: 'BG-1', code: 'HKG-01' }]);
  const id = asg(sb, 'BG-1');
  const v = (await (await api.releaseView(request, sb.teacher.token, id)).json()) as { releaseRev: number };
  expect((await api.release(request, sb.teacher.token, id, { openNow: true, expectedRev: v.releaseRev })).status()).toBe(200);
  const an = stu(sb, 'An');
  const r = await api.ask(request, an.token, id, 'Đáp số bài này là bao nhiêu?');
  expect(r.status(), await r.text()).toBe(200);
  expect(((await r.json()) as { reply: string }).reply).toBe('Góc giữa SD và mặt đáy là 45°.');
  await captureFinalState(browser, testInfo, async (page) => {
    await signIn(page, an, 'An', 'student');
    await page.goto(`/hs/lam-bai/${id}`);
    await expect(page.getByTestId('e3-workspace')).toBeVisible();
  });
});
