import { expect, test } from '../../support/fixtures.ts';
import { captureFinalState } from '../../support/final-state.ts';
import { API, assigned, attempt, bearer, play, report, sandbox, signIn, student } from './s2e8.ts';

/**
 * US-S2E8-05, ca LOẠI B của `compose.ui.spec.ts` (DECISIONS 27/11 "Chiến lược E2E", đợt 2 E2E-SPLIT2): màn "Báo chấm sai"
 * và nút Xác nhận / Giữ nguyên do ca loại A 105, 106 giữ; ca này chỉ kiểm hệ quả "Giữ nguyên" bằng gọi API đúng thứ tự
 * bước, cuối luồng mở màn Báo chấm sai (không click) và chụp 1 ảnh. Mã TC giữ nguyên trong tiêu đề.
 */

test('@TC-S2E8-107 @loai-B Giữ nguyên thì kết quả không đổi', async ({ request, browser }, testInfo) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An');
  await attempt(request, an.token, set.items[1]!.assignmentId, '44,4');
  expect((await report(request, an.token, set.items[1]!.assignmentId, 'Em đo ra 44,4')).status()).toBe(201);
  const t = sb.teacher.token;
  const metrics = async () =>
    ((await (await request.get(`${API}/api/v1/teacher/practice-reports/metrics`, { headers: bearer(t) })).json()) as { confirmed: number }).confirmed;
  const before = await metrics();
  const list = (await (await request.get(`${API}/api/v1/teacher/practice-reports`, { headers: bearer(t) })).json()) as { items: Array<{ id: string }> };
  expect(list.items).toHaveLength(1);
  const d = await request.post(`${API}/api/v1/teacher/practice-reports/${list.items[0]!.id}/decision`, { headers: bearer(t), data: { decision: 'keep' } });
  expect(d.status(), await d.text()).toBe(200);
  expect((await d.json()) as { status: string; statusLabel: string }).toMatchObject({ status: 'kept', statusLabel: 'Đã xem, giữ nguyên' });
  const item = (await play(request, an.token, set.id)).items[1]!;
  expect(item).toMatchObject({ state: 'doing', attemptsLeft: 2 });
  expect(await metrics()).toBe(before);
  // dạng viết "44,4" không được thêm vào dạng chấp nhận: bạn cùng lớp nộp vẫn Sai
  expect((await attempt(request, student(sb, 'Bình').token, set.items[1]!.assignmentId, '44,4')).outcome).toBe('wrong');
  await captureFinalState(browser, testInfo, async (page) => {
    await signIn(page, sb.teacher, 'TEACHER', 'Cô Hạnh');
    await page.goto('/gv/bai-luyen/bao-cham-sai');
    const row = page.locator('[data-testid^="s2e8-report-"][data-status]');
    await expect(row).toHaveAttribute('data-status', 'kept');
    await expect(row.getByTestId('s2e8-report-status')).toHaveText('Đã xem, giữ nguyên');
  });
});
