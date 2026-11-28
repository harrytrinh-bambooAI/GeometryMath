import { expect, test } from '../../support/fixtures.ts';
import { assignedSet, auditOf, bearer, follow, hoursRound, interact, klass, lan, localValue, ms, MSG, noHorizontalScroll, notesOf, open, SETS, smallWorld, vnAt, who } from './s3e4.ts';

/**
 * S3-E4 · C3-06 Theo dõi nhanh (P0, chạy cả trên điện thoại: mobile-iphone, mobile-pixel; và ui-chromium, ui-webkit):
 * em chưa mở bài, Mở ngay, Đổi giờ, Hủy ở hộp xác nhận.
 */

const SOLUTION = (setId: string) => `${SETS.replace('/teacher/', '/student/')}/${setId}/items/1/solution`;

test('@TC-S3E4-14 danh sách em chưa làm: chỉ em chưa có tương tác nào, kèm mã người dùng, không điểm hay xếp hạng', async ({ page, request }) => {
  const w = await smallWorld(request, { active: ['Bình An', 'Chi Mai', 'Gia Bảo', 'Khánh Linh'] });
  await assignedSet(request, lan(w), klass(w, '11A2').id, { title: 'Bộ của 11A2', items: [3] });
  const set = await assignedSet(request, lan(w), klass(w, '11A1').id, { title: 'Bộ bài tuần 15', items: [1, 2] });
  await interact(request, who(w, 'Bình An'), set.items[0]!.assignmentId!);
  await interact(request, who(w, 'Bình An'), set.items[1]!.assignmentId!);
  await interact(request, who(w, 'Chi Mai'), set.items[0]!.assignmentId!); // làm một phần
  await interact(request, who(w, 'Gia Bảo'), set.items[1]!.assignmentId!); // làm một phần
  await open(page, lan(w), '/gv/theo-doi-nhanh');
  await page.getByTestId('s3e4-class-select').selectOption({ label: '11A1' });
  await page.getByTestId('s3e4-set-select').selectOption({ label: 'Bộ bài tuần 15' });
  const rows = page.getByTestId('s3e4-student');
  await expect(rows).toHaveCount(1);
  await expect(rows).toHaveText(`Khánh Linh · ${who(w, 'Khánh Linh').userCode}`);
  const text = await page.getByTestId('s3e4-follow').innerText();
  for (const absent of ['Chi Mai', 'Gia Bảo', 'Bình An', 'điểm', 'Điểm', 'xếp hạng', 'Xếp hạng']) expect(text, absent).not.toContain(absent);
  await noHorizontalScroll(page);
  for (const id of ['s3e4-open-now', 's3e4-change-time']) {
    const box = await page.getByTestId(id).boundingBox();
    expect(box!.height, `${id} cao ít nhất 44`).toBeGreaterThanOrEqual(44);
  }
});

test('@TC-S3E4-15 Mở ngay: lời giải mở theo giờ máy chủ, chip Đã mở, một EARLY_RELEASE, mỗi em một thông báo', async ({ page, request }) => {
  const w = await smallWorld(request);
  const release = hoursRound(48);
  const set = await assignedSet(request, lan(w), klass(w, '11A1').id, { releaseAt: release });
  await open(page, lan(w), `/gv/theo-doi-nhanh?set=${set.id}`);
  await expect(page.getByTestId('s3e4-state')).toHaveText('Sắp mở');
  await expect(page.getByTestId('s3e4-release-at')).toContainText(vnAt(release));
  const t0 = Date.now();
  await page.getByTestId('s3e4-open-now').click();
  await expect(page.getByTestId('s3e4-confirm')).toContainText(MSG.s61);
  await page.getByTestId('s3e4-confirm-ok').click();
  await expect(page.getByTestId('s3e4-state')).toHaveText(MSG.s64);
  await expect(page.getByTestId('s3e4-open-now')).toBeDisabled();
  const f = await follow(request, lan(w).token, set.id);
  expect(Math.abs(ms(f.releaseAt)! - t0), 'giờ mở = giờ máy chủ lúc xử lý').toBeLessThan(60_000);
  const sol = await request.get(SOLUTION(set.id), { headers: bearer(who(w, 'Bình An').token) });
  expect(sol.status(), await sol.text()).toBe(200);
  const body = (await sol.json()) as { releaseState: string; finalAnswer?: string | null };
  expect(body.releaseState).toBe('open');
  expect(body.finalAnswer, 'Bình An lấy được lời giải ngay').toBeTruthy();
  const events = await auditOf(request, lan(w).token, klass(w, '11A1').id);
  expect(events.map((e) => e.type)).toEqual(['EARLY_RELEASE']);
  expect(ms(events[0]!.before)).toBe(ms(release));
  expect(ms(events[0]!.after)).toBe(ms(f.releaseAt));
  for (const name of ['Bình An', 'Chi Mai', 'Khánh Linh']) {
    const bodies = (await notesOf(request, who(w, name).token)).map((n) => n.body);
    expect(bodies.filter((b) => b === 'Lời giải Bộ bài tuần 15 đã mở'), name).toHaveLength(1);
  }
  for (const name of ['Thu Hà', 'Văn Tú']) expect(await notesOf(request, who(w, name).token), name).toEqual([]);
});

test('@TC-S3E4-16 Đổi giờ sang giờ tương lai: vẫn Sắp mở, một RELEASE_TIME_CHANGE, thông báo ghi giờ mới', async ({ page, request }) => {
  const w = await smallWorld(request);
  const release = hoursRound(48);
  const next = hoursRound(59);
  const set = await assignedSet(request, lan(w), klass(w, '11A1').id, { releaseAt: release });
  await open(page, lan(w), `/gv/theo-doi-nhanh?set=${set.id}`);
  await page.getByTestId('s3e4-change-time').click();
  await page.getByTestId('s3e4-time-input').fill(await localValue(page, next));
  await page.getByTestId('s3e4-confirm-ok').click();
  await expect(page.getByTestId('s3e4-release-at')).toContainText(vnAt(next));
  await expect(page.getByTestId('s3e4-state')).toHaveText('Sắp mở');
  expect(ms((await follow(request, lan(w).token, set.id)).releaseAt)).toBe(ms(next));
  const events = await auditOf(request, lan(w).token, klass(w, '11A1').id);
  expect(events.map((e) => e.type)).toEqual(['RELEASE_TIME_CHANGE']);
  expect(ms(events[0]!.before)).toBe(ms(release));
  expect(ms(events[0]!.after)).toBe(ms(next));
  for (const name of ['Bình An', 'Chi Mai', 'Khánh Linh']) {
    const bodies = (await notesOf(request, who(w, name).token)).map((n) => n.body);
    expect(bodies, name).toContain(`Giờ mở lời giải Bộ bài tuần 15 đổi sang ${vnAt(next)}`);
  }
});

test('@TC-S3E4-18 Mở ngay khi bộ chưa đặt giờ: lời giải mở, EARLY_RELEASE ghi giờ cũ "chưa đặt"', async ({ page, request }) => {
  const w = await smallWorld(request);
  const set = await assignedSet(request, lan(w), klass(w, '11A1').id, { title: 'Bộ bài tuần 16', releaseAt: null });
  await open(page, lan(w), `/gv/theo-doi-nhanh?set=${set.id}`);
  await expect(page.getByTestId('s3e4-state')).toHaveText('Chưa đặt giờ');
  const t0 = Date.now();
  await page.getByTestId('s3e4-open-now').click();
  await page.getByTestId('s3e4-confirm-ok').click();
  await expect(page.getByTestId('s3e4-state')).toHaveText(MSG.s64);
  const events = await auditOf(request, lan(w).token, klass(w, '11A1').id);
  expect(events.map((e) => e.type)).toEqual(['EARLY_RELEASE']);
  expect(events[0]!.before ?? null, 'giờ cũ: chưa đặt').toBeNull();
  expect(Math.abs(ms(events[0]!.after)! - t0)).toBeLessThan(60_000);
  await page.getByTestId('s3e4-audit').locator('summary').click();
  await expect(page.getByTestId('s3e4-audit-row')).toContainText('Mở ngay');
  await expect(page.getByTestId('s3e4-audit-row')).toContainText('trước chưa đặt');
  const sol = (await (await request.get(SOLUTION(set.id), { headers: bearer(who(w, 'Bình An').token) })).json()) as { releaseState: string };
  expect(sol.releaseState).toBe('open');
});

test('@TC-S3E4-26 Hủy ở hộp xác nhận: lời giải vẫn khóa, không AuditEvent, không thông báo', async ({ page, request }) => {
  const w = await smallWorld(request);
  const set = await assignedSet(request, lan(w), klass(w, '11A1').id);
  const before = await follow(request, lan(w).token, set.id);
  await open(page, lan(w), `/gv/theo-doi-nhanh?set=${set.id}`);
  await page.getByTestId('s3e4-open-now').click();
  await expect(page.getByTestId('s3e4-confirm')).toContainText(MSG.s61);
  await page.getByTestId('s3e4-cancel').click();
  await expect(page.getByTestId('s3e4-confirm')).toHaveCount(0);
  await expect(page.getByTestId('s3e4-state')).toHaveText('Sắp mở');
  const after = await follow(request, lan(w).token, set.id);
  expect(after.releaseState).toBe('upcoming');
  expect(ms(after.releaseAt)).toBe(ms(before.releaseAt));
  expect(await auditOf(request, lan(w).token, klass(w, '11A1').id)).toEqual([]);
  expect((await notesOf(request, who(w, 'Bình An').token)).map((n) => n.body)).toEqual(['Có bài mới: Bộ bài tuần 15']);
});
