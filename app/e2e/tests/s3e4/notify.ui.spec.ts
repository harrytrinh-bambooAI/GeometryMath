import { expect, test } from '../../support/fixtures.ts';
import { teacherApi } from '../s3e1/s3e1.ts';
import { api, assignedSet, klass, lan, notesOf, ok, open, seedNotes, smallWorld, unreadOf, who } from './s3e4.ts';

/** S3-E4 · thông báo trong ứng dụng của học sinh (ui-chromium, ui-webkit iPad): chấm số, Chưa đọc / Đã đọc, người nhận. */

test('@TC-S3E4-28 giao bộ: em Đang học thấy chấm 1 và thông báo "bài mới" Chưa đọc; em Chờ duyệt không có thông báo', async ({ page, request }) => {
  const w = await smallWorld(request, { pending: ['Thu Hà', 'Hoàng Nam', 'Tuấn Anh'] });
  await assignedSet(request, lan(w), klass(w, '11A1').id, { title: 'Bộ bài tuần 16' });
  await open(page, who(w, 'Bình An'), '/hs/bo-bai');
  await expect(page.getByTestId('s3e4-bell-count')).toHaveText('1');
  await page.getByTestId('s3e4-bell').click();
  await expect(page).toHaveURL(/\/hs\/thong-bao$/);
  const notes = page.getByTestId('s3e4-notification');
  await expect(notes).toHaveCount(1);
  await expect(notes).toContainText('Có bài mới: Bộ bài tuần 16');
  await expect(notes).toContainText('Chưa đọc');
  await expect(notes).toHaveAttribute('data-read', 'false');
  for (const name of ['Chi Mai', 'Khánh Linh']) expect(await unreadOf(request, who(w, name).token), name).toBe(1);
  for (const name of ['Thu Hà', 'Hoàng Nam', 'Tuấn Anh']) expect(await notesOf(request, who(w, name).token), name).toEqual([]);
});

test('@TC-S3E4-29 Mở ngay: thông báo "lời giải đã mở" Chưa đọc, bấm vào tới bộ bài với lời giải đã mở', async ({ page, request }) => {
  const w = await smallWorld(request);
  const set = await assignedSet(request, lan(w), klass(w, '11A1').id, { title: 'Bộ bài tuần 15' });
  await ok(await api.openNow(request, lan(w).token, set.id));
  await open(page, who(w, 'Bình An'), '/hs/thong-bao');
  const note = page.getByTestId('s3e4-notification').filter({ hasText: 'Lời giải Bộ bài tuần 15 đã mở' });
  await expect(note).toHaveAttribute('data-read', 'false');
  await note.click();
  await expect(page).toHaveURL(new RegExp(`/hs/bo-bai/${set.id}$`));
  await expect(page.getByTestId('s3e2-hs-set')).toContainText('Bộ bài tuần 15');
  await expect(page.getByTestId('s3e2-hs-release-1')).toContainText('Đã mở');
  await expect(page.getByTestId('s3e2-hs-solution-1')).toBeVisible();
});

test('@TC-S3E4-30 mở một thông báo thì thành Đã đọc, chấm giảm từ 2 xuống 1, thông báo kia vẫn Chưa đọc', async ({ page, request }) => {
  const w = await smallWorld(request);
  await assignedSet(request, lan(w), klass(w, '11A1').id, { title: 'Bộ bài tuần 15', items: [1] });
  await assignedSet(request, lan(w), klass(w, '11A1').id, { title: 'Bộ bài tuần 16', items: [2] });
  await open(page, who(w, 'Bình An'), '/hs/thong-bao');
  await expect(page.getByTestId('s3e4-unread-badge')).toHaveText('2');
  const notes = page.getByTestId('s3e4-notification');
  await expect(notes).toHaveCount(2);
  await expect(notes.first()).toContainText('Có bài mới: Bộ bài tuần 16');
  await notes.first().click();
  await expect(page).toHaveURL(/\/hs\/bo-bai\/[0-9a-f-]+$/);
  await expect(page.getByTestId('s3e4-bell-count')).toHaveText('1');
  await page.getByTestId('s3e4-bell').click();
  await expect(page.getByTestId('s3e4-unread-badge')).toHaveText('1');
  await expect(notes.first()).toHaveAttribute('data-read', 'true');
  await expect(notes.first()).toContainText('Đã đọc');
  await expect(notes.nth(1)).toHaveAttribute('data-read', 'false');
  await expect(notes.nth(1)).toContainText('Có bài mới: Bộ bài tuần 15');
});

test('@TC-S3E4-31 "Đánh dấu tất cả đã đọc": cả 5 thành Đã đọc, chấm biến mất, thông báo của bạn không đổi', async ({ page, request }) => {
  const w = await smallWorld(request);
  await seedNotes(request, who(w, 'Bình An').id, 5);
  await seedNotes(request, who(w, 'Chi Mai').id, 2);
  await open(page, who(w, 'Bình An'), '/hs/thong-bao');
  await expect(page.getByTestId('s3e4-unread-badge')).toHaveText('5');
  await page.getByTestId('s3e4-read-all').click();
  await expect(page.getByTestId('s3e4-unread-badge')).toHaveCount(0);
  const notes = page.getByTestId('s3e4-notification');
  await expect(notes).toHaveCount(5);
  for (const n of await notes.all()) await expect(n).toHaveAttribute('data-read', 'true');
  expect(await unreadOf(request, who(w, 'Bình An').token)).toBe(0);
  expect(await unreadOf(request, who(w, 'Chi Mai').token), 'thông báo của Chi Mai không đổi').toBe(2);
  await page.goto('/hs/bo-bai');
  await expect(page.getByTestId('s3e4-bell')).toBeVisible();
  await expect(page.getByTestId('s3e4-bell-count')).toHaveCount(0);
});

test('@TC-S3E4-33 em được duyệt sau khi giao không nhận thông báo cũ nhưng vẫn thấy bộ trong Bài của em', async ({ page, request }) => {
  const w = await smallWorld(request);
  await assignedSet(request, lan(w), klass(w, '11A1').id, { title: 'Bộ bài tuần 15' });
  const ha = who(w, 'Thu Hà');
  const approve = await teacherApi.decide(request, lan(w).token, klass(w, '11A1').id, ha.id, 'approve');
  expect(approve.status(), await approve.text()).toBeLessThan(300);
  await open(page, ha, '/hs/thong-bao');
  await expect(page.getByTestId('s3e4-no-notification')).toBeVisible();
  await expect(page.getByTestId('s3e4-notification')).toHaveCount(0);
  expect(await notesOf(request, ha.token)).toEqual([]);
  await page.goto('/hs/bo-bai');
  await expect(page.getByTestId('s3e2-home')).toContainText('Bộ bài tuần 15');
  await expect(page.getByTestId('s3e4-bell-count')).toHaveCount(0);
});

test('@TC-S3E4-39 học sinh Đã rời không nhận thông báo "bài mới"', async ({ page, request }) => {
  const w = await smallWorld(request);
  await assignedSet(request, lan(w), klass(w, '11A1').id, { title: 'Bộ bài tuần 16' });
  expect((await notesOf(request, who(w, 'Bình An').token)).map((n) => n.body)).toEqual(['Có bài mới: Bộ bài tuần 16']);
  await open(page, who(w, 'Văn Tú'), '/hs/thong-bao');
  await expect(page.getByTestId('s3e4-notifications')).toBeVisible();
  await expect(page.getByTestId('s3e4-notifications')).not.toContainText('Bộ bài tuần 16');
  await expect(page.getByTestId('s3e4-notification')).toHaveCount(0);
  expect(await notesOf(request, who(w, 'Văn Tú').token)).toEqual([]);
});
