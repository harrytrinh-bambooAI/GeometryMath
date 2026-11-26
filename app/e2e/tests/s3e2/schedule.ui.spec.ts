import type { Page } from '@playwright/test';
import { randomUUID } from 'node:crypto';
import { expect, test } from '../../support/fixtures.ts';
import { assigned, bearer, CODE, draft, home, localValue, S, sandbox, T, teacherPage, teacherSet, walk, type Sandbox } from './s3e2.ts';

/**
 * S3-E2 · C3-03 lịch mở lời giải (Cả bộ / Từng bài), cảnh báo giờ mở sớm hơn hạn (MSG-S3-21), đề "Cần xem lại" trước
 * khi giao (MSG-S3-22), lưu trữ bộ Đã đóng (ui-chromium, ui-webkit iPad).
 */

const DUE = '2026-12-11T13:00:00Z'; // 11/12/2026 20:00 giờ Việt Nam
const vn = (d: string, hm: string) => new Date(`${d}T${hm}:00+07:00`).toISOString();
const MSG21 = 'Giờ mở lời giải sớm hơn hạn làm. Em sẽ thấy lời giải trước khi hết hạn.';
const MSG22 = 'Bộ có đề đang ở trạng thái Cần xem lại. Thầy cô xem lại trước khi giao.';
const ms = (iso: string | null | undefined) => (iso ? new Date(iso).getTime() : null);

async function openSet(page: Page, sb: Sandbox, id: string) {
  await teacherPage(page, sb, `/gv/bo-bai/${id}`);
  await expect(page.getByTestId('s3e2-compose')).toBeVisible();
}
async function setRelease(page: Page, iso: string) {
  await page.getByTestId('s3e2-release').fill(await localValue(page, iso));
}
async function giao(page: Page) {
  await page.getByTestId('s3e2-assign').click();
  await expect(page.getByTestId('s3e2-confirm')).toBeVisible();
  await page.getByTestId('s3e2-confirm-ok').click();
  await expect(page.getByTestId('s3e2-status')).toHaveText('Đã giao');
}

test('@TC-S3E2-15 đặt giờ mở cho cả bộ: bốn đề cùng giờ, không cảnh báo vì sau hạn', async ({ page, request }) => {
  const sb = await sandbox(request);
  const d = await draft(request, sb, { dueAt: DUE });
  await openSet(page, sb, d.id);
  await page.getByTestId('s3e2-mode-set').check();
  await setRelease(page, vn('2026-12-13', '08:00'));
  await expect(page.getByTestId('s3e2-warn-early')).toHaveCount(0);
  await page.getByTestId('s3e2-save').click();
  await expect(page.getByTestId('s3e2-note')).toHaveText('Đã lưu nháp');
  const set = await teacherSet(request, sb.teacher.token, d.id);
  expect(set.items.map((i) => ms(i.releaseAt))).toEqual(Array(4).fill(ms(vn('2026-12-13', '08:00'))));
  expect(set.releaseBeforeDue).toBe(false);
});

test('@TC-S3E2-16 giờ mở riêng của Đề 3 ghi đè giờ của bộ, học sinh thấy đúng giờ từng đề', async ({ page, request }) => {
  const sb = await sandbox(request);
  const d = await draft(request, sb, { items: [1, 2, 3, 4], dueAt: DUE, releaseAt: vn('2026-12-13', '08:00') });
  await openSet(page, sb, d.id);
  await page.getByTestId('s3e2-mode-item').check();
  await page.getByTestId(`s3e2-item-release-${CODE(3)}`).fill(await localValue(page, vn('2026-12-14', '08:00')));
  await page.getByTestId('s3e2-save').click();
  await expect(page.getByTestId('s3e2-note')).toHaveText('Đã lưu nháp');
  const set = await teacherSet(request, sb.teacher.token, d.id);
  const want = [vn('2026-12-13', '08:00'), vn('2026-12-13', '08:00'), vn('2026-12-14', '08:00'), vn('2026-12-13', '08:00')].map(ms);
  expect(set.items.map((i) => ms(i.releaseAt))).toEqual(want);
  const a = await request.post(`${T}/${d.id}/assign`, { headers: bearer(sb.teacher.token), data: { requestKey: randomUUID() } });
  expect(a.status(), await a.text()).toBe(200);
  const mine = (await (await request.get(`${S}/${d.id}`, { headers: bearer(sb.classes[0]!.students['Bình An']!.token) })).json()) as { items: Array<{ releaseAt: string }> };
  expect(mine.items.map((i) => ms(i.releaseAt))).toEqual(want);
});

test('@TC-S3E2-20 giờ mở sớm hơn hạn: banner đỏ MSG-S3-21 với hai cách sửa, nút Giao chờ chọn', async ({ page, request }) => {
  const sb = await sandbox(request);
  const d = await draft(request, sb, { items: [1, 2, 4, 5], dueAt: DUE });
  await openSet(page, sb, d.id);
  await setRelease(page, vn('2026-12-10', '08:00'));
  const warn = page.getByTestId('s3e2-warn-early');
  await expect(warn).toContainText(MSG21);
  await expect(page.getByTestId('s3e2-fix-later')).toHaveText('Dời giờ mở sau hạn');
  await expect(page.getByTestId('s3e2-keep-early')).toHaveText('Giữ giờ mở sớm');
  await expect(page.getByTestId('s3e2-assign')).toBeDisabled();
  await page.getByTestId('s3e2-fix-later').click();
  await expect(warn).toHaveCount(0);
  await expect(page.getByTestId('s3e2-assign')).toBeEnabled();
});

test('@TC-S3E2-21 biên cảnh báo: giờ mở bằng đúng hạn không cảnh báo, 19:59 thì cảnh báo', async ({ page, request }) => {
  const sb = await sandbox(request);
  const d = await draft(request, sb, { dueAt: DUE });
  await openSet(page, sb, d.id);
  await setRelease(page, vn('2026-12-11', '20:00'));
  await expect(page.getByTestId('s3e2-warn-early')).toHaveCount(0);
  await setRelease(page, vn('2026-12-11', '19:59'));
  await expect(page.getByTestId('s3e2-warn-early')).toContainText(MSG21);
});

test('@TC-S3E2-22 chọn "Giữ giờ mở sớm" thì giao được với giờ mở đã đặt', async ({ page, request }) => {
  const sb = await sandbox(request);
  const early = vn('2026-12-10', '08:00');
  const d = await draft(request, sb, { items: [1, 2, 4, 5], dueAt: DUE, releaseAt: early });
  await openSet(page, sb, d.id);
  await expect(page.getByTestId('s3e2-warn-early')).toBeVisible();
  await page.getByTestId('s3e2-keep-early').click();
  await giao(page);
  const set = await teacherSet(request, sb.teacher.token, d.id);
  expect(ms(set.releaseAt)).toBe(ms(early));
  const mine = (await (await request.get(`${S}/${d.id}`, { headers: bearer(sb.classes[0]!.students['Bình An']!.token) })).json()) as { releaseAt: string; items: Array<{ releaseAt: string }> };
  expect(ms(mine.releaseAt)).toBe(ms(early));
  expect(mine.items.every((i) => ms(i.releaseAt) === ms(early))).toBe(true);
});

test('@TC-S3E2-32 màn Giao liệt kê đúng một đề "Cần xem lại", chưa chọn thì bộ chưa giao', async ({ page, request }) => {
  const sb = await sandbox(request);
  const d = await draft(request, sb, { dueAt: DUE, releaseAt: vn('2026-12-13', '08:00') });
  await openSet(page, sb, d.id);
  await page.getByTestId('s3e2-assign').click();
  const banner = page.getByTestId('s3e2-review-banner');
  await expect(banner).toContainText(MSG22);
  await expect(banner.locator('li')).toHaveCount(1);
  await expect(page.getByTestId(`s3e2-review-item-${CODE(3)}`)).toHaveText('Đề 3 hai mặt phẳng');
  await expect(page.getByTestId('s3e2-review-keep')).toHaveText('Vẫn giao');
  await expect(page.getByTestId('s3e2-review-drop')).toHaveText('Bỏ đề khỏi bộ');
  expect((await teacherSet(request, sb.teacher.token, d.id)).status).toBe('draft');
});

test('@TC-S3E2-33 "Vẫn giao": bộ giao đủ 4 đề, Đề 3 giữ nhãn ở Kho đề, học sinh không thấy nhãn', async ({ page, request }) => {
  const sb = await sandbox(request);
  const d = await draft(request, sb, { dueAt: DUE, releaseAt: vn('2026-12-13', '08:00') });
  await openSet(page, sb, d.id);
  await page.getByTestId('s3e2-assign').click();
  await page.getByTestId('s3e2-review-keep').click();
  await page.getByTestId('s3e2-confirm-ok').click();
  await expect(page.getByTestId('s3e2-status')).toHaveText('Đã giao');
  const set = await teacherSet(request, sb.teacher.token, d.id);
  expect(set.items).toHaveLength(4);
  const opts = (await (await request.get(`${T}/options`, { headers: bearer(sb.teacher.token) })).json()) as { problems: Array<{ code: string; needsReview: boolean }> };
  expect(opts.problems.find((p) => p.code === CODE(3))!.needsReview).toBe(true);
  const an = sb.classes[0]!.students['Bình An']!.token;
  for (const r of [await request.get(S, { headers: bearer(an) }), await request.get(`${S}/${d.id}`, { headers: bearer(an) })]) {
    const { keys, strings } = walk(await r.json());
    expect(keys.filter((k) => /review|verify/i.test(k))).toEqual([]);
    expect(strings.join(' ')).not.toContain('Cần xem lại');
  }
});

test('@TC-S3E2-41 lưu trữ bộ Đã đóng: rời Bài đang giao và danh sách của học sinh, hiện ở Bộ đã lưu trữ', async ({ page, request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb, { title: 'Bộ bài tuần 15', items: [1, 2] });
  const c = await request.post(`${T}/${set.id}/close`, { headers: bearer(sb.teacher.token), data: { requestKey: randomUUID() } });
  expect(c.status()).toBe(200);
  await teacherPage(page, sb, '/gv/bo-bai');
  await page.getByTestId('s3e2-tab-closed').click();
  await page.getByTestId(`s3e2-archive-${set.id}`).click();
  await page.getByTestId('s3e2-ask-ok').click();
  await page.getByTestId('s3e2-tab-archived').click();
  await expect(page.getByTestId(`s3e2-set-${set.id}`)).toBeVisible();
  await page.getByTestId('s3e2-tab-assigned').click();
  await expect(page.getByTestId(`s3e2-set-${set.id}`)).toHaveCount(0);
  expect((await teacherSet(request, sb.teacher.token, set.id)).status).toBe('archived');
  const an = sb.classes[0]!.students['Bình An']!.token;
  expect((await home(request, an)).groups).toEqual([]);
  expect((await request.get(`${S}/${set.id}`, { headers: bearer(an) })).status()).toBe(200); // kết quả vẫn xem được
});
