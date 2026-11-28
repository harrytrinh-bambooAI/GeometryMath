import { randomUUID } from 'node:crypto';
import { expect, test } from '../../support/fixtures.ts';
import { captureFinalState } from '../../support/final-state.ts';
import { assigned, assignRaw, bearer, draft, home, S, sandbox, T, teacherPage, teacherSet, walk } from './s3e2.ts';

/**
 * S3-E2 · C3-03, ca LOẠI B của `schedule.ui.spec.ts` (DECISIONS 27/11 "Chiến lược E2E"): chỉ kiểm giờ mở / giao / lưu
 * trữ bằng gọi API đúng thứ tự bước; phần chỗ bấm (chế độ giờ mở, banner MSG-S3-21/22, nút Giao) do ca loại A 15, 20,
 * 21, 32 giữ. Cuối luồng mở trang bộ bài (không click), chụp 1 ảnh. Mã TC giữ nguyên trong tiêu đề.
 */

const DUE = '2026-12-11T13:00:00Z'; // 11/12/2026 20:00 giờ Việt Nam
const vn = (d: string, hm: string) => new Date(`${d}T${hm}:00+07:00`).toISOString();
const ms = (iso: string | null | undefined) => (iso ? new Date(iso).getTime() : null);

test('@TC-S3E2-16 @loai-B giờ mở riêng của Đề 3 ghi đè giờ của bộ, học sinh thấy đúng giờ từng đề', async ({ request, browser }, testInfo) => {
  const sb = await sandbox(request);
  // Bước "chế độ Từng bài + giờ riêng Đề 3 + Lưu nháp" = tạo/lưu nháp với giờ mở riêng của đề 3
  const d = await draft(request, sb, { items: [1, 2, 3, 4], dueAt: DUE, releaseAt: vn('2026-12-13', '08:00'), itemReleases: { 3: vn('2026-12-14', '08:00') } });
  const set = await teacherSet(request, sb.teacher.token, d.id);
  const want = [vn('2026-12-13', '08:00'), vn('2026-12-13', '08:00'), vn('2026-12-14', '08:00'), vn('2026-12-13', '08:00')].map(ms);
  expect(set.items.map((i) => ms(i.releaseAt))).toEqual(want);
  const a = await request.post(`${T}/${d.id}/assign`, { headers: bearer(sb.teacher.token), data: { requestKey: randomUUID() } });
  expect(a.status(), await a.text()).toBe(200);
  const mine = (await (await request.get(`${S}/${d.id}`, { headers: bearer(sb.classes[0]!.students['Bình An']!.token) })).json()) as { items: Array<{ releaseAt: string }> };
  expect(mine.items.map((i) => ms(i.releaseAt))).toEqual(want);
  await captureFinalState(browser, testInfo, async (page) => {
    await teacherPage(page, sb, `/gv/bo-bai/${d.id}`);
    await expect(page.getByTestId('s3e2-compose')).toBeVisible();
  });
});

test('@TC-S3E2-22 @loai-B chọn "Giữ giờ mở sớm" thì giao được với giờ mở đã đặt', async ({ request, browser }, testInfo) => {
  const sb = await sandbox(request);
  const early = vn('2026-12-10', '08:00');
  const d = await draft(request, sb, { items: [1, 2, 4, 5], dueAt: DUE, releaseAt: early });
  expect((await teacherSet(request, sb.teacher.token, d.id)).releaseBeforeDue).toBe(true); // banner MSG-S3-21 hiện ở ca A 20
  const a = await assignRaw(request, sb.teacher.token, d.id); // bước "Giữ giờ mở sớm" + Giao
  expect(a.status(), await a.text()).toBe(200);
  const set = await teacherSet(request, sb.teacher.token, d.id);
  expect(ms(set.releaseAt)).toBe(ms(early));
  const mine = (await (await request.get(`${S}/${d.id}`, { headers: bearer(sb.classes[0]!.students['Bình An']!.token) })).json()) as { releaseAt: string; items: Array<{ releaseAt: string }> };
  expect(ms(mine.releaseAt)).toBe(ms(early));
  expect(mine.items.every((i) => ms(i.releaseAt) === ms(early))).toBe(true);
  await captureFinalState(browser, testInfo, async (page) => {
    await teacherPage(page, sb, `/gv/bo-bai/${d.id}`);
    await expect(page.getByTestId('s3e2-status')).toHaveText('Đã giao');
  });
});

test('@TC-S3E2-33 @loai-B "Vẫn giao": bộ giao đủ 4 đề, Đề 3 giữ nhãn ở Kho đề, học sinh không thấy nhãn', async ({ request, browser }, testInfo) => {
  const sb = await sandbox(request);
  const d = await draft(request, sb, { dueAt: DUE, releaseAt: vn('2026-12-13', '08:00') });
  const a = await assignRaw(request, sb.teacher.token, d.id); // bước "Vẫn giao" + xác nhận
  expect(a.status(), await a.text()).toBe(200);
  const set = await teacherSet(request, sb.teacher.token, d.id);
  expect(set.status).toBe('assigned');
  expect(set.items).toHaveLength(4);
  const opts = (await (await request.get(`${T}/options`, { headers: bearer(sb.teacher.token) })).json()) as { problems: Array<{ code: string; needsReview: boolean }> };
  expect(opts.problems.find((p) => p.code === 'BOB-03')!.needsReview).toBe(true);
  const an = sb.classes[0]!.students['Bình An']!.token;
  for (const r of [await request.get(S, { headers: bearer(an) }), await request.get(`${S}/${d.id}`, { headers: bearer(an) })]) {
    const { keys, strings } = walk(await r.json());
    expect(keys.filter((k) => /review|verify/i.test(k))).toEqual([]);
    expect(strings.join(' ')).not.toContain('Cần xem lại');
  }
  await captureFinalState(browser, testInfo, async (page) => {
    await teacherPage(page, sb, `/gv/bo-bai/${d.id}`);
    await expect(page.getByTestId('s3e2-status')).toHaveText('Đã giao');
  });
});

test('@TC-S3E2-41 @loai-B lưu trữ bộ Đã đóng: rời Bài đang giao và danh sách của học sinh, hiện ở Bộ đã lưu trữ', async ({ request, browser }, testInfo) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb, { title: 'Bộ bài tuần 15', items: [1, 2] });
  const c = await request.post(`${T}/${set.id}/close`, { headers: bearer(sb.teacher.token), data: { requestKey: randomUUID() } });
  expect(c.status()).toBe(200);
  const ar = await request.post(`${T}/${set.id}/archive`, { headers: bearer(sb.teacher.token), data: { requestKey: randomUUID() } }); // bước "Lưu trữ" + xác nhận
  expect(ar.status(), await ar.text()).toBe(200);
  expect((await teacherSet(request, sb.teacher.token, set.id)).status).toBe('archived');
  const an = sb.classes[0]!.students['Bình An']!.token;
  expect((await home(request, an)).groups).toEqual([]);
  expect((await request.get(`${S}/${set.id}`, { headers: bearer(an) })).status()).toBe(200); // kết quả vẫn xem được
  await captureFinalState(browser, testInfo, async (page) => {
    await teacherPage(page, sb, '/gv/bo-bai');
    await expect(page.getByTestId('s3e2-tab-archived')).toBeVisible();
  });
});
