import type { Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { assignRaw, bearer, CODE, draft, home, hours, localValue, noHorizontalScroll, S, sandbox, T, teacherPage, teacherSet, V, type Sandbox } from './s3e2.ts';

/**
 * S3-E2 · C3-03 soạn bộ bài trên web (ui-chromium, ui-webkit iPad): tạo nháp, kéo thả, xem trước như học sinh, giao,
 * lịch mở lời giải và cảnh báo giờ mở sớm hơn hạn, đề "Cần xem lại" trước khi giao.
 */

const DUE = '2026-12-11T13:00:00Z'; // 11/12/2026 20:00 giờ Việt Nam
const vn = (d: string, hm: string) => new Date(`${d}T${hm}:00+07:00`).toISOString();

async function openSet(page: Page, sb: Sandbox, id: string) {
  await teacherPage(page, sb, `/gv/bo-bai/${id}`);
  await expect(page.getByTestId('s3e2-compose')).toBeVisible();
}
const order = (page: Page) => page.getByTestId('s3e2-items').locator('> li').evaluateAll((els) => els.map((e) => e.getAttribute('data-testid')));
async function giao(page: Page) {
  await page.getByTestId('s3e2-assign').click();
  await expect(page.getByTestId('s3e2-confirm')).toBeVisible();
  await page.getByTestId('s3e2-confirm-ok').click();
  await expect(page.getByTestId('s3e2-status')).toHaveText('Đã giao');
}

test('@TC-S3E2-01 tạo bộ nháp từ bốn đề và lưu', async ({ page, request }) => {
  const sb = await sandbox(request);
  await teacherPage(page, sb, '/gv/bo-bai/moi');
  for (const n of [1, 2, 3, 4]) await page.getByTestId(`s3e2-add-${CODE(n)}`).click();
  await page.getByTestId('s3e2-title').fill('Bộ bài tuần 16');
  await page.getByTestId('s3e2-class').selectOption({ label: '11A1' });
  await page.getByTestId('s3e2-due').fill(await localValue(page, DUE));
  await page.getByTestId('s3e2-save').click();
  await expect(page.getByTestId('s3e2-note')).toHaveText('Đã lưu nháp');
  await expect(page.getByTestId('s3e2-status')).toHaveText('Nháp');
  const list = (await (await request.get(T, { headers: bearer(sb.teacher.token) })).json()) as { items: Array<{ id: string; status: string }> };
  expect(list.items).toHaveLength(1);
  const set = await teacherSet(request, sb.teacher.token, list.items[0]!.id);
  expect(set.status).toBe('draft');
  expect(set.items.map((i) => i.code)).toEqual([1, 2, 3, 4].map(CODE));
  expect(new Date(set.dueAt!).toISOString()).toBe(new Date(DUE).toISOString());
  expect((await home(request, sb.classes[0]!.students['Bình An']!.token)).groups).toEqual([]);
  await noHorizontalScroll(page);
});

test('@TC-S3E2-02 kéo thả Đề 4 lên đầu, thứ tự lưu lại và học sinh thấy đúng thứ tự', async ({ page, request }) => {
  const sb = await sandbox(request);
  const d = await draft(request, sb, { releaseAt: hours(48) });
  await openSet(page, sb, d.id);
  await page.getByTestId(`s3e2-item-${CODE(4)}`).dragTo(page.getByTestId(`s3e2-item-${CODE(1)}`));
  await expect.poll(() => order(page)).toEqual([4, 1, 2, 3].map((n) => `s3e2-item-${CODE(n)}`));
  await page.getByTestId('s3e2-save').click();
  await expect(page.getByTestId('s3e2-note')).toHaveText('Đã lưu nháp');
  await page.reload();
  await expect.poll(() => order(page)).toEqual([4, 1, 2, 3].map((n) => `s3e2-item-${CODE(n)}`));
  const a = await assignRaw(request, sb.teacher.token, d.id);
  expect(a.status()).toBe(200);
  const mine = (await (await request.get(`${S}/${d.id}`, { headers: bearer(sb.classes[0]!.students['Bình An']!.token) })).json()) as { items: Array<{ statement: string }> };
  expect(mine.items[0]!.statement).toContain('đáy là hình vuông cạnh 3'); // Đề 4 thể tích
});

test('@TC-S3E2-03 xem trước như học sinh: làm thử Đề 1, xin gợi ý, không ghi gì, lời giải vẫn khóa', async ({ page, request }) => {
  const sb = await sandbox(request);
  const d = await draft(request, sb, { releaseAt: hours(48) });
  await openSet(page, sb, d.id);
  await page.getByTestId('s3e2-preview-btn').click();
  await expect(page.getByTestId('s3e2-preview')).toBeVisible();
  await page.getByTestId('s3e2-pv-answer-1').fill('45');
  await page.getByTestId('s3e2-pv-check-1').click();
  await expect(page.getByTestId('s3e2-pv-result-1')).toHaveText('Đúng');
  await page.getByTestId('s3e2-pv-hint-1').click();
  await expect(page.getByTestId('s3e2-pv-hint-text-1')).toContainText('Hình chiếu của SC');
  await expect(page.getByTestId('s3e2-pv-lock-1')).toContainText('Sắp mở');
  const after = await teacherSet(request, sb.teacher.token, d.id);
  expect(after.status).toBe('draft');
  expect(after.items.every((i) => !i.assignmentId)).toBe(true); // không có hàng bài giao, không bài làm
  for (const name of ['Bình An', 'Chi Mai', 'Đức Huy']) expect((await home(request, sb.classes[0]!.students[name]!.token)).groups).toEqual([]);
});

test('@TC-S3E2-10 chưa chọn lớp thì nút Giao chờ chọn: MSG-S3-25, gọi thẳng VALIDATION_FAILED', async ({ page, request }) => {
  const sb = await sandbox(request);
  const d = await draft(request, sb, { classId: null });
  await openSet(page, sb, d.id);
  await expect(page.getByTestId('s3e2-assign')).toBeDisabled();
  await expect(page.getByTestId('s3e2-assign-note')).toHaveText('Chưa chọn lớp để giao.');
  const r = await assignRaw(request, sb.teacher.token, d.id);
  expect(r.status()).toBe(400);
  const err = (await r.json()) as { code: string; message: string };
  expect(err.code).toBe('VALIDATION_FAILED');
  expect(JSON.stringify(err)).toContain('Chưa chọn lớp để giao.');
  expect((await teacherSet(request, sb.teacher.token, d.id)).status).toBe('draft');
});
