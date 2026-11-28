import type { Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { board051, cellOf, event, evidenceUrl, getJson, openBoard, setUrl, student, walk, type Ev, type Sandbox } from './s3e3.ts';

/** S3-E3 · khung "BẰNG CHỨNG" của một ô (US-S3E3-02): đủ tương tác, chữ thuần, chỉ đọc, em đã rời, làm mới. */

const Q = 'Giao tuyến của hai mặt phẳng là đường nào ạ?';
/** Chi Mai × Đề 3 theo US-S3E3-02-SC1: mở đề 15:08, gợi ý mức 1 15:10, hỏi 15:12, gửi "SB" 15:14 không khớp. */
const maiDe3 = (question = Q): { cells: Record<string, ('Đúng' | 'Xem lại' | 'Chưa làm')[]>; events: Ev[] } => ({
  cells: { 'Chi Mai': ['Đúng', 'Xem lại', 'Chưa làm', 'Chưa làm'] },
  events: [
    { student: 'Chi Mai', item: 3, kind: 'open', at: '15:08' },
    { student: 'Chi Mai', item: 3, kind: 'hint', level: 1, at: '15:10' },
    { student: 'Chi Mai', item: 3, kind: 'ask', text: question, at: '15:12' },
    { student: 'Chi Mai', item: 3, kind: 'answer', text: 'SB', correct: false, at: '15:14' },
  ],
});
const panel = (page: Page) => page.getByTestId('s3e3-evidence');
async function pick(page: Page, sb: Sandbox, name: string, item: number) {
  await cellOf(page, student(sb, name), item).click();
  await expect(page.getByTestId('s3e3-ev-title')).toHaveText(`BẰNG CHỨNG · ${name} · Đề ${item}`);
}

test('@TC-S3E3-22 bấm ô Xem lại hiện đủ bằng chứng: đáp số, gợi ý, câu hỏi nguyên văn, nút Mở bài làm', async ({ page, request }) => {
  const sb = await board051(request, maiDe3());
  await openBoard(page, sb);
  await expect(cellOf(page, student(sb, 'Chi Mai'), 3)).toHaveText('Xem lại');
  await pick(page, sb, 'Chi Mai', 3);
  await expect(page.getByTestId('s3e3-ev-code')).toHaveText(student(sb, 'Chi Mai').userCode);
  await expect(page.getByTestId('s3e3-ev-status')).toHaveText('Xem lại');
  await expect(page.getByTestId('s3e3-ev-attempt')).toHaveCount(1);
  await expect(page.getByTestId('s3e3-ev-attempt')).toContainText('15:14');
  await expect(page.getByTestId('s3e3-ev-attempt')).toContainText('SB');
  await expect(page.getByTestId('s3e3-ev-attempt')).toContainText('không khớp');
  await expect(page.getByTestId('s3e3-ev-hint')).toHaveCount(1);
  await expect(page.getByTestId('s3e3-ev-hint')).toHaveText('15:10 xin gợi ý mức 1');
  await expect(page.getByTestId('s3e3-ev-question')).toHaveCount(1);
  await expect(page.getByTestId('s3e3-ev-question')).toContainText('15:12');
  await expect(page.getByTestId('s3e3-ev-question-text')).toHaveText(Q);
  await expect(page.getByTestId('s3e3-ev-opened')).toContainText('15:08');
  await expect(page.getByTestId('s3e3-open-work')).toBeVisible();
  await expect(panel(page).getByText('Nhắn em')).toHaveCount(0);
});

test('@TC-S3E3-23 ô Chưa làm: "Em chưa mở bài này.", không dữ liệu giả, không nút Mở bài làm', async ({ page, request }) => {
  const sb = await board051(request);
  await openBoard(page, sb);
  await pick(page, sb, 'Khánh Linh', 3);
  await expect(page.getByTestId('s3e3-ev-empty')).toHaveText('Em chưa mở bài này.');
  for (const id of ['s3e3-ev-attempt', 's3e3-ev-hint', 's3e3-ev-question', 's3e3-open-work']) await expect(page.getByTestId(id)).toHaveCount(0);
});

test('@TC-S3E3-24 ô Đang làm chỉ có tương tác đã xảy ra: một gợi ý mức 1, không có đáp số', async ({ page, request }) => {
  const sb = await board051(request);
  await openBoard(page, sb);
  await pick(page, sb, 'Gia Bảo', 3);
  await expect(page.getByTestId('s3e3-ev-status')).toHaveText('Đang làm');
  await expect(page.getByTestId('s3e3-ev-hint')).toHaveCount(1);
  await expect(page.getByTestId('s3e3-ev-hint')).toHaveText(/^\d\d:\d\d xin gợi ý mức 1$/);
  await expect(page.getByTestId('s3e3-ev-attempt')).toHaveCount(0);
  await expect(panel(page)).not.toContainText('đáp số');
});

test('@TC-S3E3-25 học sinh Đã rời: khung chỉ đọc có chip "đã rời", đủ dữ liệu cũ, Mở bài làm chỉ đọc', async ({ page, request }) => {
  const sb = await board051(request, { students: [{ name: 'Văn Tú', status: 'left' }], cells: { 'Văn Tú': ['Đúng', 'Chưa làm', 'Chưa làm', 'Chưa làm'] } });
  await openBoard(page, sb);
  await pick(page, sb, 'Văn Tú', 1);
  await expect(page.getByTestId('s3e3-ev-left')).toHaveText('đã rời');
  await expect(page.getByTestId('s3e3-ev-status')).toHaveText('Đúng');
  await expect(page.getByTestId('s3e3-ev-attempt')).toHaveCount(1);
  await expect(panel(page).locator('input, textarea, select')).toHaveCount(0);
  await expect(panel(page).locator('button, a')).toHaveText([/Mở bài làm/, /Làm mới/]);
  await page.getByTestId('s3e3-open-work').click();
  await expect(page.getByTestId('s3e3-readonly')).toHaveText(/Chỉ đọc/);
  await expect(page.getByTestId('s3e3-work-left')).toHaveText('đã rời');
  await expect(page.getByTestId('s3e3-work').locator('input, textarea, select')).toHaveCount(0);
});

test('@TC-S3E3-26 "Mở bài làm" chỉ đọc, không sinh tương tác', async ({ page, request }) => {
  const sb = await board051(request, maiDe3());
  const url = evidenceUrl(sb, 3, student(sb, 'Chi Mai').id);
  const before = await getJson<Record<string, unknown>>(request, url, sb.teacher.token);
  const writes: string[] = [];
  page.on('request', (r) => {
    if (r.url().includes('/api/') && r.method() !== 'GET') writes.push(`${r.method()} ${r.url()}`);
  });
  await openBoard(page, sb);
  await pick(page, sb, 'Chi Mai', 3);
  await page.getByTestId('s3e3-open-work').click();
  await expect(page.getByTestId('s3e3-work-title')).toHaveText('Bài làm của Chi Mai · Đề 3 · hai mặt phẳng');
  await expect(page.getByTestId('s3e3-work-statement')).toContainText('Tính góc giữa (SBC) và (ABC)');
  await expect(page.getByTestId('s3e3-work').locator('input, textarea, select')).toHaveCount(0);
  await expect(page.getByTestId('s3e3-ev-question-text')).toHaveText(Q);
  await page.getByTestId('s3e3-work-back').click();
  await expect(page.getByTestId('s3e3-board')).toBeVisible();
  await expect(cellOf(page, student(sb, 'Chi Mai'), 3)).toHaveText('Xem lại');
  const after = await getJson<Record<string, unknown>>(request, url, sb.teacher.token);
  for (const k of ['status', 'attempts', 'hints', 'questions', 'openedAt', 'stoppedStep']) expect(after[k], k).toEqual(before[k]);
  expect(writes).toEqual([]);
});

test('@TC-S3E3-27 câu hỏi chứa mã HTML hiển thị như chữ thuần', async ({ page, request }) => {
  const html = '<script>alert(1)</script><b>đậm</b>';
  const sb = await board051(request, maiDe3(html));
  let dialogs = 0;
  page.on('dialog', (d) => {
    dialogs++;
    void d.dismiss();
  });
  await openBoard(page, sb);
  await pick(page, sb, 'Chi Mai', 3);
  await expect(page.getByTestId('s3e3-ev-question-text')).toHaveText(html);
  await expect(panel(page).locator('script')).toHaveCount(0);
  await expect(page.getByTestId('s3e3-ev-question-text').locator('b')).toHaveCount(0);
  expect(dialogs).toBe(0);
});

test('@TC-S3E3-28 câu hỏi 1000 ký tự gập còn 300 kèm "Xem thêm", mở ra đủ nội dung, khung không vỡ', async ({ page, request }) => {
  const long = Array.from({ length: 100 }, (_, i) => `câu ${String(i).padStart(2, '0')} ạ`).join(' ').padEnd(1000, '?').slice(0, 1000);
  expect(long).toHaveLength(1000);
  const sb = await board051(request, maiDe3(long));
  await openBoard(page, sb);
  await pick(page, sb, 'Chi Mai', 3);
  const text = page.getByTestId('s3e3-ev-question-text');
  await expect(text).toHaveText(`${long.slice(0, 300)}…`);
  await page.getByTestId('s3e3-ev-more').click();
  await expect(text).toHaveText(long);
  await expect(page.getByTestId('s3e3-ev-more')).toHaveCount(0);
  expect(await panel(page).evaluate((e) => e.scrollWidth <= e.clientWidth + 1)).toBe(true);
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth + 1)).toBe(true);
});

test('@TC-S3E3-32 số điện thoại em tự gõ trong câu hỏi: hiện nguyên văn, không tách thành trường riêng', async ({ page, request }) => {
  const q = 'Em là Mai, số em 0912345678, cô giải giúp em';
  const sb = await board051(request, maiDe3(q));
  await openBoard(page, sb);
  await pick(page, sb, 'Chi Mai', 3);
  await expect(page.getByTestId('s3e3-ev-question-text')).toHaveText(q);
  const body = await getJson<unknown>(request, evidenceUrl(sb, 3, student(sb, 'Chi Mai').id), sb.teacher.token);
  const w = walk(body);
  for (const k of w.keys) expect(k).not.toMatch(/phone|tel|mobile|contact/i);
  expect(w.strings.filter((s) => s.includes('0912345678'))).toEqual([q]);
});

test('@TC-S3E3-33 dữ liệu đổi trong lúc đang xem: Làm mới ở khung cập nhật khung và ô trên bảng', async ({ page, request }) => {
  const sb = await board051(request);
  await openBoard(page, sb);
  await pick(page, sb, 'Gia Bảo', 3);
  await expect(page.getByTestId('s3e3-ev-status')).toHaveText('Đang làm');
  await expect(page.getByTestId('s3e3-ev-updated')).toHaveText(/^Cập nhật lúc \d\d:\d\d$/);
  await event(request, sb, { student: 'Gia Bảo', item: 3, kind: 'answer', text: '60', correct: true });
  await expect(page.getByTestId('s3e3-ev-status')).toHaveText('Đang làm');
  await page.getByTestId('s3e3-ev-refresh').click();
  await expect(page.getByTestId('s3e3-ev-status')).toHaveText('Đúng');
  await expect(page.getByTestId('s3e3-ev-attempt')).toHaveCount(1);
  await expect(page.getByTestId('s3e3-ev-attempt')).toContainText('khớp');
  await expect(page.getByTestId('s3e3-ev-updated')).toHaveText(/^Cập nhật lúc \d\d:\d\d$/);
  await expect(cellOf(page, student(sb, 'Gia Bảo'), 3)).toHaveText('Đúng');
});

test('@TC-S3E3-58 địa chỉ trang bài làm dùng userId (hs=), không dùng mã người dùng; mã vẫn hiện trên màn hình', async ({ page, request }) => {
  const sb = await board051(request, maiDe3());
  const mai = student(sb, 'Chi Mai');
  const calls: string[] = [];
  page.on('request', (r) => {
    if (r.url().includes('/api/')) calls.push(r.url());
  });
  await openBoard(page, sb);
  await pick(page, sb, 'Chi Mai', 3);
  await expect(page.getByTestId('s3e3-ev-code')).toHaveText(mai.userCode);
  await page.getByTestId('s3e3-open-work').click();
  await expect(page.getByTestId('s3e3-work-title')).toHaveText('Bài làm của Chi Mai · Đề 3 · hai mặt phẳng');
  const url = new URL(page.url());
  expect(url.searchParams.get('hs')).toBe(mai.id);
  expect(page.url()).not.toContain(mai.userCode);
  expect(calls.length).toBeGreaterThan(0);
  for (const c of calls) expect(c, c).not.toContain(mai.userCode);
  expect(calls.some((c) => c.includes(`userId=${mai.id}`))).toBe(true);
  const board = await getJson<{ rows: Array<{ userId: string; userCode: string }> }>(request, `${setUrl(sb)}/board`, sb.teacher.token);
  for (const row of board.rows) expect(row.userId).toMatch(/^[0-9a-f-]{36}$/);
});
