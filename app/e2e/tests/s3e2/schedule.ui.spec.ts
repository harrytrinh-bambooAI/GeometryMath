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
