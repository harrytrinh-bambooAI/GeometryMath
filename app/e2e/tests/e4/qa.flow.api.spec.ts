import { expect, test } from '../../support/fixtures.ts';
import { captureFinalState } from '../../support/final-state.ts';
import { API, MSG, bearer, call, sandbox, signIn } from './e4.ts';

/**
 * US-GEO-410, ca LOẠI B của `qa.ui.spec.ts` (DECISIONS 27/11 "Chiến lược E2E", đợt 2 E2E-SPLIT2): chỗ bấm mở dòng nhật
 * ký do ca loại A 0463 giữ; ca này chỉ kiểm dữ liệu câu trả lời đã chặn bằng gọi API đúng thứ tự bước, cuối luồng mở màn
 * Lịch sử (không click) và chụp 1 ảnh. Mã TC giữ nguyên trong tiêu đề.
 */

const F056 = 'Góc cần tìm là góc nào trên hình?';

test('Câu trả lời từng bị chặn mở lại vẫn là câu thay thế @TC-GEO-0493 @loai-B', async ({ request, browser }, testInfo) => {
  const sb = await sandbox(request, [{ students: ['Bình'], assignments: [{ key: 'BG-1', code: 'HKG-01' }] }]);
  const binh = sb.classes[0]!.students['Bình']!;
  const bg1 = sb.classes[0]!.assignments['BG-1']!;
  expect((await call.hint(request, binh.token, bg1, 1)).status()).toBe(200);
  const asked = (await (await call.ask(request, binh.token, bg1, F056)).json()) as { reply: string };
  expect(asked.reply.startsWith(MSG['35'])).toBe(true);
  // mở lại nhật ký: danh sách + câu trả lời của dòng đó (đúng các lời gọi màn Lịch sử dùng)
  const logRes = await call.log(request, binh.token);
  expect(logRes.status()).toBe(200);
  const logText = await logRes.text();
  const items = (JSON.parse(logText) as { items: Array<{ id: number; question: string }> }).items;
  const mine = items.find((x) => x.question === F056);
  expect(mine, 'nhật ký có câu đã hỏi').toBeTruthy();
  const ans = await request.get(`${API}/api/v1/questions/${mine!.id}/answer`, { headers: bearer(binh.token) });
  expect(ans.status()).toBe(200);
  const ansText = await ans.text();
  expect((JSON.parse(ansText) as { reply: string }).reply).toBe(asked.reply);
  for (const t of [logText, ansText]) {
    expect(t).not.toContain('45°');
    expect(t).not.toContain('Góc giữa SD và mặt đáy là 45°.');
  }
  await captureFinalState(browser, testInfo, async (page) => {
    await signIn(page, binh, 'Bình');
    await page.goto('/hs/lich-su');
    await expect(page.getByTestId('e4-history')).toBeVisible();
  });
});
