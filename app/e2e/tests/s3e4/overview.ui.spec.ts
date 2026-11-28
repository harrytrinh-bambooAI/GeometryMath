import { expect, test } from '../../support/fixtures.ts';
import { world } from '../s3e1/s3e1.ts';
import { ACTIVE_8, api, assignDraft, assignedSet, draftSet, dueLabel, fullWorld, hours, hoursRound, interact, klass, lan, MSG, ok, open, smallWorld, vnAt, who } from './s3e4.ts';

/** S3-E4 · C3-05 Tổng quan giáo viên trên web (ui-chromium, ui-webkit iPad): ba thẻ, trạng thái rỗng, quá hạn, đã mở. */

test('@TC-S3E4-01 ba thẻ đúng số liệu trên web: bài đang giao, lời giải đang khóa, em chờ duyệt', async ({ page, request }) => {
  const w = await fullWorld(request);
  const due = hoursRound(7 * 24);
  const release = hoursRound(9 * 24);
  const a1 = await assignedSet(request, lan(w), klass(w, '11A1').id, { title: 'Bộ bài tuần 15', items: [1, 2, 3, 4], dueAt: due, releaseAt: release });
  const a2 = await assignedSet(request, lan(w), klass(w, '11A2').id, { title: 'Bộ bài tuần 15', items: [1, 2, 3, 4], dueAt: due, releaseAt: release });
  for (const name of ACTIVE_8.filter((n) => n !== 'Khánh Linh')) await interact(request, who(w, name), a1.items[0]!.assignmentId!);
  for (const name of ['Quốc Bảo', 'Thảo Vy', 'Tuấn Kiệt']) await interact(request, who(w, name), a2.items[2]!.assignmentId!);
  await open(page, lan(w), '/gv/tong-quan');
  const assigned = page.getByTestId('s3e4-card-assigned').getByTestId('s3e4-assigned-row');
  await expect(assigned).toHaveCount(2);
  await expect(assigned.filter({ hasText: '11A1' })).toContainText(`Bộ bài tuần 15 · 11A1 · hạn ${dueLabel(due)} · 1 em chưa mở`);
  await expect(assigned.filter({ hasText: '11A2' })).toContainText('0 em chưa mở');
  const locked = page.getByTestId('s3e4-card-locked').getByTestId('s3e4-locked-row');
  await expect(locked).toHaveCount(2);
  for (const row of await locked.all()) await expect(row).toContainText(`mở lúc ${vnAt(release)}`);
  await expect(page.getByTestId('s3e4-pending-total')).toHaveText('3');
  await expect(page.getByTestId('s3e4-pending-row')).toHaveText(['11A1: 3']);
});

test('@TC-S3E4-03 chưa có lớp: lời mời MSG-S3-66 kèm nút Tạo lớp, không có ba thẻ rỗng', async ({ page, request }) => {
  const w = await world(request, { teachers: ['Cô Lan'], students: [], classes: [] });
  await open(page, lan(w), '/gv/tong-quan');
  await expect(page.getByTestId('s3e4-no-class')).toContainText(MSG.s66);
  for (const card of ['assigned', 'locked', 'pending']) await expect(page.getByTestId(`s3e4-card-${card}`)).toHaveCount(0);
  await page.getByTestId('s3e4-create-class').click();
  await expect(page).toHaveURL(/\/gv\/lop-hoc$/);
});

test('@TC-S3E4-04 có lớp nhưng không có việc: MSG-S3-65, không thẻ nào mang số 0', async ({ page, request }) => {
  const w = await world(request, { teachers: ['Cô Lan'], students: [], classes: [{ key: '11A1', teacher: 'Cô Lan', name: '11A1', active: ['Bình An'] }] });
  await open(page, lan(w), '/gv/tong-quan');
  await expect(page.getByTestId('s3e4-nothing')).toHaveText(MSG.s65);
  for (const card of ['assigned', 'locked', 'pending']) await expect(page.getByTestId(`s3e4-card-${card}`)).toHaveCount(0);
  await expect(page.getByTestId('s3e4-no-class')).toHaveCount(0);
});

test('@TC-S3E4-05 giao thêm "Bộ bài tuần 16": tổng quan thêm một dòng với 8 em chưa mở', async ({ page, request }) => {
  const w = await fullWorld(request);
  await assignedSet(request, lan(w), klass(w, '11A1').id, { title: 'Bộ bài tuần 15', items: [1] });
  await assignedSet(request, lan(w), klass(w, '11A2').id, { title: 'Bộ bài tuần 15', items: [1] });
  const d = await draftSet(request, lan(w), klass(w, '11A1').id, { title: 'Bộ bài tuần 16', items: [5, 6] });
  await open(page, lan(w), '/gv/tong-quan');
  await expect(page.getByTestId('s3e4-assigned-row')).toHaveCount(2);
  await assignDraft(request, lan(w), d.id);
  await page.reload();
  const rows = page.getByTestId('s3e4-assigned-row');
  await expect(rows).toHaveCount(3);
  const added = rows.filter({ hasText: 'Bộ bài tuần 16' });
  await expect(added).toContainText('Bộ bài tuần 16 · 11A1');
  await expect(added).toContainText('8 em chưa mở');
});

test('@TC-S3E4-06 bộ quá hạn vẫn nằm trong "Bài đang giao" kèm nhãn Quá hạn', async ({ page, request }) => {
  const w = await smallWorld(request);
  const due = new Date(Date.now() + 4_000).toISOString();
  await assignedSet(request, lan(w), klass(w, '11A1').id, { dueAt: due });
  await expect
    .poll(async () => (await ok<{ assigned: Array<{ overdue: boolean }> }>(await api.overview(request, lan(w).token))).assigned[0]!.overdue, { timeout: 15_000 })
    .toBe(true);
  await open(page, lan(w), '/gv/tong-quan');
  const row = page.getByTestId('s3e4-card-assigned').getByTestId('s3e4-assigned-row');
  await expect(row).toHaveCount(1);
  await expect(row).toContainText('Bộ bài tuần 15 · 11A1');
  await expect(row.getByText('Quá hạn')).toBeVisible();
});

test('@TC-S3E4-07 lời giải đã mở: rời thẻ "Lời giải đang khóa", có chip Đã mở ở "Bài đang giao"', async ({ page, request }) => {
  const w = await smallWorld(request);
  const opened = await assignedSet(request, lan(w), klass(w, '11A1').id, { title: 'Bộ bài tuần 15', items: [1] });
  await assignedSet(request, lan(w), klass(w, '11A1').id, { title: 'Bộ bài tuần 14', items: [2], releaseAt: hours(30) });
  await ok(await api.openNow(request, lan(w).token, opened.id));
  await open(page, lan(w), '/gv/tong-quan');
  const row = page.getByTestId('s3e4-assigned-row').filter({ hasText: 'Bộ bài tuần 15' });
  await expect(row.getByText(MSG.s64, { exact: true })).toBeVisible();
  const locked = page.getByTestId('s3e4-locked-row');
  await expect(locked).toHaveCount(1);
  await expect(locked).toContainText('Bộ bài tuần 14');
  await expect(page.getByTestId('s3e4-card-locked')).not.toContainText('Bộ bài tuần 15');
});

test('@TC-S3E4-09 không em nào chờ duyệt: thẻ "Em chờ duyệt" ẩn, hai thẻ còn lại hiện bình thường', async ({ page, request }) => {
  const w = await smallWorld(request, { pending: [] });
  await assignedSet(request, lan(w), klass(w, '11A1').id);
  await open(page, lan(w), '/gv/tong-quan');
  await expect(page.getByTestId('s3e4-card-assigned')).toBeVisible();
  await expect(page.getByTestId('s3e4-card-locked')).toBeVisible();
  await expect(page.getByTestId('s3e4-card-pending')).toHaveCount(0);
  await expect(page.getByTestId('s3e4-overview')).not.toContainText('Em chờ duyệt');
});

test('@TC-S3E4-13 bộ Nháp không xuất hiện ở thẻ nào', async ({ page, request }) => {
  const w = await smallWorld(request);
  await assignedSet(request, lan(w), klass(w, '11A1').id, { title: 'Bộ bài tuần 15', items: [1] });
  await draftSet(request, lan(w), klass(w, '11A1').id, { title: 'Bộ bài tuần 16', items: [2] });
  await open(page, lan(w), '/gv/tong-quan');
  await expect(page.getByTestId('s3e4-assigned-row')).toHaveCount(1);
  await expect(page.getByTestId('s3e4-overview')).toContainText('Bộ bài tuần 15');
  await expect(page.getByTestId('s3e4-overview')).not.toContainText('Bộ bài tuần 16');
});
