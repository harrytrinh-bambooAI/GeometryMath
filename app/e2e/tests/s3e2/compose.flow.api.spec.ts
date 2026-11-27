import { expect, test } from '../../support/fixtures.ts';
import { captureFinalState } from '../../support/final-state.ts';
import { assignRaw, bearer, CODE, draft, home, hours, sandbox, T, teacherPage, teacherSet, V } from './s3e2.ts';

/**
 * S3-E2 · C3-03, ca LOẠI B của `compose.ui.spec.ts` (DECISIONS 27/11 "Chiến lược E2E"): ca đã có ca loại A cùng màn
 * kiểm đúng chỗ bấm (tạo nháp, kéo thả, xem trước, nút Giao chờ chọn); nay chỉ kiểm nghiệp vụ bằng gọi API đúng thứ tự
 * bước, cuối luồng mở trang bộ bài (không click) và chụp 1 ảnh. Mã TC giữ nguyên trong tiêu đề.
 */

const DUE = '2026-12-11T13:00:00Z'; // 11/12/2026 20:00 giờ Việt Nam
const vn = (d: string, hm: string) => new Date(`${d}T${hm}:00+07:00`).toISOString();

test('@TC-S3E2-04 @loai-B giao bộ: học sinh Đang học thấy, em Chờ duyệt không thấy', async ({ request, browser }, testInfo) => {
  const sb = await sandbox(request);
  const d = await draft(request, sb, { items: [1, 2, 4, 5], dueAt: DUE, releaseAt: vn('2026-12-13', '08:00') });
  const a = await assignRaw(request, sb.teacher.token, d.id);
  expect(a.status(), await a.text()).toBe(200);
  for (const name of ['Bình An', 'Chi Mai', 'Đức Huy']) {
    const h = await home(request, sb.classes[0]!.students[name]!.token);
    expect(h.groups.flatMap((g) => g.sets).map((s) => s.title)).toEqual(['Bộ bài tuần 16']);
  }
  for (const name of ['Hoàng Nam', 'Thu Hà']) expect((await home(request, sb.classes[0]!.pending[name]!.token)).groups).toEqual([]);
  const list = (await (await request.get(T, { headers: bearer(sb.teacher.token) })).json()) as { items: Array<{ status: string }> };
  expect(list.items.filter((s) => s.status === 'assigned')).toHaveLength(1);
  await captureFinalState(browser, testInfo, async (page) => {
    await teacherPage(page, sb, `/gv/bo-bai/${d.id}`);
    await expect(page.getByTestId('s3e2-status')).toHaveText('Đã giao');
  });
});

test('@TC-S3E2-07 @loai-B bộ không có đề không giao được: MSG-S3-23, VALIDATION_FAILED, vẫn Nháp', async ({ request, browser }, testInfo) => {
  const sb = await sandbox(request);
  const d = await draft(request, sb, { items: [] });
  const r = await assignRaw(request, sb.teacher.token, d.id);
  expect(r.status()).toBe(400);
  const e = (await r.json()) as { code: string };
  expect(e.code).toBe('VALIDATION_FAILED');
  expect(JSON.stringify(e)).toContain('Bộ bài cần từ 1 đến 20 đề.');
  expect((await teacherSet(request, sb.teacher.token, d.id)).status).toBe('draft');
  await captureFinalState(browser, testInfo, async (page) => {
    await teacherPage(page, sb, `/gv/bo-bai/${d.id}`);
    await expect(page.getByTestId('s3e2-status')).toHaveText('Nháp');
  });
});

test('@TC-S3E2-09 @loai-B thêm trùng Đề 1: MSG-S3-24, bộ vẫn một lần Đề 1', async ({ request, browser }, testInfo) => {
  const sb = await sandbox(request);
  const d = await draft(request, sb, { items: [1] });
  const r = await request.post(`${T}/${d.id}/items`, { headers: bearer(sb.teacher.token), data: { problemVersionId: V(1) } });
  expect(r.status()).toBe(400);
  expect(JSON.stringify(await r.json())).toContain('Đề này đã có trong bộ.');
  const set = await teacherSet(request, sb.teacher.token, d.id);
  expect(set.items.map((i) => i.code)).toEqual([CODE(1)]);
  await captureFinalState(browser, testInfo, async (page) => {
    await teacherPage(page, sb, `/gv/bo-bai/${d.id}`);
    await expect(page.getByTestId('s3e2-count')).toHaveText('Đề trong bộ (1)');
  });
});

test('@TC-S3E2-13 @loai-B hạn làm đã qua khi giao: giáo viên xác nhận thì giao, học sinh thấy Quá hạn ngay', async ({ request, browser }, testInfo) => {
  const sb = await sandbox(request);
  const d = await draft(request, sb, { items: [1, 2], dueAt: hours(-1), releaseAt: hours(48) });
  const a = await assignRaw(request, sb.teacher.token, d.id); // confirmPastRelease: true = bước "xác nhận" của hộp cảnh báo
  expect(a.status(), await a.text()).toBe(200);
  expect((await teacherSet(request, sb.teacher.token, d.id)).status).toBe('assigned');
  const h = await home(request, sb.classes[0]!.students['Bình An']!.token);
  expect(h.groups[0]!.sets[0]!.overdue).toBe(true);
  await captureFinalState(browser, testInfo, async (page) => {
    await teacherPage(page, sb, `/gv/bo-bai/${d.id}`);
    await expect(page.getByTestId('s3e2-status')).toHaveText('Đã giao');
  });
});
