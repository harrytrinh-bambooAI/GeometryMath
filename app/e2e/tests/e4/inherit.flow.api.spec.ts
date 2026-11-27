import { expect, test } from '../../support/fixtures.ts';
import { captureFinalState } from '../../support/final-state.ts';
import { HKG, MSG, call, hints12, openWorkspace, sandbox, selfStudy } from './e4.ts';

/**
 * US-GEO-406, ca LOẠI B của `inherit.ui.spec.ts` (DECISIONS 27/11 "Chiến lược E2E", đợt 2 E2E-SPLIT2): chỗ bấm tab
 * Gợi ý / Lời giải và thẻ khóa kế thừa do ca loại A 0434, 0435 giữ; ca này chỉ kiểm trạng thái khóa theo giờ mở bằng gọi
 * API đúng thứ tự bước, cuối luồng mở màn Làm bài (không click) và chụp 1 ảnh. Mã TC giữ nguyên trong tiêu đề.
 */

type Help = { solution: { state: string; message?: string } };

test('Dán lại đề của bài giao Chưa đặt giờ thì lời giải tự học cũng khóa @TC-GEO-0481 @loai-B', async ({ request, browser }, testInfo) => {
  const sb = await sandbox(request, [{ students: ['An'], assignments: [{ key: 'BG-2', code: 'HKG-02', release: 'unset' }] }]);
  const an = sb.classes[0]!.students['An']!;
  const id = await selfStudy(request, an.token, HKG['02']);
  await hints12(request, an.token, id);
  const help = async () => (await (await call.help(request, an.token, id)).json()) as Help;
  expect((await help()).solution).toMatchObject({ state: 'inherited', message: MSG['31'] });
  const locked = await call.solution(request, an.token, id);
  expect(locked.status()).toBe(403);
  expect(((await locked.json()) as { message: string }).message).toBe(MSG['31']);
  // giáo viên đặt giờ: vẫn khóa cho tới khi giờ đó tới
  const at = new Date(Date.now() + 4000).toISOString();
  const r = await call.release(request, sb.teacher.token, sb.classes[0]!.assignments['BG-2']!, { releaseAt: at, expectedRev: 0 });
  expect(r.status(), await r.text()).toBe(200);
  expect((await help()).solution).toMatchObject({ state: 'inherited', message: MSG['31'] });
  await new Promise((ok) => setTimeout(ok, Math.max(0, Date.parse(at) - Date.now() + 1000)));
  const after = await help();
  expect(after.solution.state).not.toBe('inherited');
  expect(JSON.stringify(after)).not.toContain(MSG['31']);
  expect((await call.solution(request, an.token, id)).status()).toBe(200);
  await captureFinalState(browser, testInfo, (page) => openWorkspace(page, an, id));
});
