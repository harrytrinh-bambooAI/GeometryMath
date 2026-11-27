import { expect, test } from '../../support/fixtures.ts';
import { captureFinalState } from '../../support/final-state.ts';
import { API, BG, bearer, devToken, signInPage } from '../e1/e1.ts';
import { F037, PV } from './e5.ts';

/**
 * US-GEO-503, ca LOẠI B của `detail.ui.spec.ts` (DECISIONS 27/11 "Chiến lược E2E", đợt 2 E2E-SPLIT2): bố cục trang chi
 * tiết đề do ca loại A 0515 giữ; ca này chỉ kiểm dữ liệu lời giải giáo viên khi bài giao còn Sắp mở bằng gọi API, cuối
 * luồng mở trang chi tiết đề (không click) và chụp 1 ảnh. Mã TC giữ nguyên trong tiêu đề.
 */

test('Giáo viên thấy lời giải dù BG-1 đang Sắp mở @TC-GEO-0516 @loai-B', async ({ request, browser }, testInfo) => {
  const an = await devToken(request, 'F-GEO-020');
  const mine = (await (await request.get(`${API}/api/v1/student/assignments`, { headers: bearer(an) })).json()) as {
    items: Array<{ id: string; releaseState: string }>;
  };
  expect(mine.items.find((a) => a.id === BG('BG-1'))?.releaseState).toBe('upcoming');
  const teacher = await devToken(request, 'F-GEO-003');
  const r = await request.get(`${API}/api/v1/teacher/catalog/problems/${PV('HKG-01')}`, { headers: bearer(teacher) });
  expect(r.status(), await r.text()).toBe(200);
  const d = (await r.json()) as { solution: { finalAnswer: string; steps: Array<{ body: string }> } | null };
  expect(d.solution?.finalAnswer).toBe('45°');
  expect(d.solution!.steps.map((s) => s.body)).toEqual(F037); // đủ ba bước, không có bước 4
  await captureFinalState(browser, testInfo, async (page) => {
    await signInPage(page, request, 'F-GEO-003');
    await page.goto(`/gv/de/${PV('HKG-01')}`);
    await expect(page.getByTestId('e2-problem-detail')).toBeVisible();
    await expect(page.getByTestId('e5-answer')).toHaveText('45°');
  });
});
