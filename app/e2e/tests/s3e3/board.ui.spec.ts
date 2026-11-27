import { expect, test } from '../../support/fixtures.ts';
import {
  active,
  bearer,
  BOARD,
  board051,
  cellOf,
  event,
  NAMES,
  openBoard,
  openResults,
  rowNames,
  rowOf,
  rows,
  sandbox,
  setUrl,
  student,
  WEEK15,
  type Cell,
} from './s3e3.ts';

/** S3-E3 · C3-04 bảng lớp × bài: thứ tự hàng, chọn bộ, biên số đề, trạng thái rỗng / đang tải / lỗi, Chờ duyệt, Đã rời. */

const WEEK16 = 'Bộ bài tuần 16';

test('@TC-S3E3-04 hàng xếp theo tên theo chữ cái tiếng Việt: Dũng, Đạt, Đức Huy', async ({ page, request }) => {
  const sb = await board051(request, {
    students: [{ name: 'Dũng' }, { name: 'Đạt' }],
    cells: { Dũng: ['Đúng'], Đạt: ['Đúng'] },
  });
  await openBoard(page, sb);
  expect(await rowNames(page)).toEqual(['Bình An', 'Chi Mai', 'Dũng', 'Đạt', 'Đức Huy', 'Gia Bảo', 'Hà My', 'Khánh Linh', 'Minh Quân', 'Ngọc Anh']);
});

test('@TC-S3E3-05 hai em cùng tên Hoàng Nam liền nhau, xếp theo mã người dùng, mỗi hàng hiện mã', async ({ page, request }) => {
  const sb = await board051(request, { students: [{ name: 'Hoàng Nam' }, { name: 'Hoàng Nam' }] });
  await openBoard(page, sb);
  const codes = [student(sb, 'Hoàng Nam').userCode, student(sb, 'Hoàng Nam#2').userCode].sort();
  const names = await rowNames(page);
  const first = names.indexOf('Hoàng Nam');
  expect(names[first + 1]).toBe('Hoàng Nam');
  expect(names.slice(0, first)).toEqual(['Bình An', 'Chi Mai', 'Đức Huy', 'Gia Bảo', 'Hà My']);
  await expect(rows(page).nth(first)).toHaveAttribute('data-code', codes[0]!);
  await expect(rows(page).nth(first + 1)).toHaveAttribute('data-code', codes[1]!);
  await expect(rows(page).nth(first)).toContainText(codes[0]!);
  await expect(rows(page).nth(first + 1)).toContainText(codes[1]!);
});

test('@TC-S3E3-06 đổi kết quả không đổi thứ tự hàng: Gia Bảo vẫn ở hàng thứ 4', async ({ page, request }) => {
  const sb = await board051(request);
  await openBoard(page, sb);
  const bao = student(sb, 'Gia Bảo');
  await expect(cellOf(page, bao, 3)).toHaveAttribute('data-status', 'doing');
  await event(request, sb, { student: 'Gia Bảo', item: 3, kind: 'answer', text: '60', correct: true });
  await page.getByTestId('s3e3-refresh').click();
  await expect(cellOf(page, bao, 3)).toHaveAttribute('data-status', 'correct');
  await expect(cellOf(page, bao, 3)).toHaveText('Đúng');
  await expect(rows(page).nth(3)).toHaveAttribute('data-code', bao.userCode);
  expect(await rowNames(page)).toEqual(NAMES);
});

test('@TC-S3E3-08 bộ mặc định là bộ mới giao nhất; chọn bộ khác thì cột đổi theo', async ({ page, request }) => {
  const sb = await board051(request, { moreSets: [{ title: WEEK16, items: 2 }] });
  await openBoard(page, sb, 0, null);
  await expect(page.getByTestId('s3e3-set-select').locator('option:checked')).toHaveText(WEEK16);
  await expect(page.getByTestId('s3e3-col')).toHaveCount(2);
  await page.getByTestId('s3e3-set-select').selectOption({ label: WEEK15 });
  await expect(page.getByTestId('s3e3-col')).toHaveCount(4);
  await expect(page.getByTestId('s3e3-col').first()).toHaveText('Đề 1 · SC-đáy');
  await expect(cellOf(page, student(sb, 'Bình An'), 1)).toHaveText('Đúng');
  await expect(cellOf(page, student(sb, 'Chi Mai'), 4)).toHaveText('Chưa làm');
  await expect(cellOf(page, student(sb, 'Gia Bảo'), 3)).toHaveText('Đang làm');
});

test('@TC-S3E3-09 bộ 1 đề có 1 cột; bộ 20 đề có 20 cột, cột Học sinh cố định, các cột đề cuộn ngang', async ({ page, request }) => {
  const twenty: Cell[] = Array.from({ length: 20 }, (_, i) => (['Đúng', 'Xem lại', 'Đang làm', 'Chưa làm'] as const)[i % 4]!);
  const sb = await sandbox(request, [
    { name: '11A1', students: active(NAMES), sets: [{ title: 'Bộ một đề', items: 1, cells: { 'Bình An': ['Đúng'] } }, { title: 'Bộ hai mươi đề', items: 20, cells: { 'Bình An': twenty } }] },
  ]);
  await openBoard(page, sb, 0, 0);
  await expect(page.getByTestId('s3e3-col')).toHaveCount(1);
  await page.getByTestId('s3e3-set-select').selectOption({ label: 'Bộ hai mươi đề' });
  await expect(page.getByTestId('s3e3-col')).toHaveCount(20);
  await expect(page.getByTestId('s3e3-cell')).toHaveCount(160);
  const table = page.getByTestId('s3e3-board');
  expect(await table.locator('thead th').first().evaluate((e) => getComputedStyle(e).position)).toBe('sticky');
  const scroll = table.locator('xpath=..');
  expect(await scroll.evaluate((e) => e.scrollWidth > e.clientWidth)).toBe(true);
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth + 1)).toBe(true);
  const last = cellOf(page, student(sb, 'Bình An'), 20);
  await last.scrollIntoViewIfNeeded();
  await expect(last).toHaveText('Chưa làm');
  await expect(rowOf(page, student(sb, 'Bình An')).locator('b')).toBeInViewport();
});

test('@TC-S3E3-11 chưa ai làm bài của bộ: 8 hàng Chưa làm, MSG-S3-41, không thẻ nhận định', async ({ page, request }) => {
  const sb = await sandbox(request, [{ name: '11A1', students: active(NAMES), sets: [{ title: WEEK16, items: 2 }] }]);
  await openBoard(page, sb);
  await expect(rows(page)).toHaveCount(8);
  await expect(page.locator('[data-testid="s3e3-cell"][data-status="todo"]')).toHaveCount(16);
  await expect(page.getByTestId('s3e3-msg-none')).toHaveText('Chưa em nào làm bài của bộ này.');
  await expect(page.getByTestId('s3e3-stuck-note')).toBeVisible();
  await expect(page.getByTestId('s3e3-insight')).toHaveCount(0);
  await expect(page.getByTestId('s3e3-error')).toHaveCount(0);
});

test('@TC-S3E3-12 lớp không có học sinh Đang học: MSG-S3-42 kèm liên kết tới danh sách Chờ duyệt', async ({ page, request }) => {
  const sb = await sandbox(request, [{ name: '11B3', students: [], sets: [{ title: WEEK15, items: 2 }] }]);
  await openResults(page, sb);
  await expect(page.getByTestId('s3e3-msg-noactive')).toContainText('Lớp chưa có học sinh Đang học. Duyệt các em đang chờ để bắt đầu.');
  await expect(page.getByTestId('s3e3-to-pending')).toHaveAttribute('href', `/gv/lop-hoc/${sb.classes[0]!.id}`);
  await expect(page.getByTestId('s3e3-board')).toHaveCount(0);
});

test('@TC-S3E3-13 học sinh Chờ duyệt không có hàng và không có trong thân phản hồi', async ({ page, request }) => {
  const sb = await board051(request, {
    students: [{ name: 'Hoàng Nam', status: 'pending' }, { name: 'Thu Hà', status: 'pending' }, { name: 'Hoàng Nam', status: 'pending' }],
  });
  await openBoard(page, sb);
  await expect(rows(page)).toHaveCount(8);
  expect(await rowNames(page)).toEqual(NAMES);
  const text = await (await request.get(`${setUrl(sb)}/board`, { headers: bearer(sb.teacher.token) })).text();
  for (const who of ['Hoàng Nam', 'Hoàng Nam#2', 'Thu Hà']) expect(text).not.toContain(student(sb, who).userCode);
  expect(text).not.toContain('Thu Hà');
});

test('@TC-S3E3-15 đang tải hiện khung chờ dạng bảng; lỗi tải hiện MSG-S3-47 và Thử lại tải lại được', async ({ page, request }) => {
  const sb = await board051(request);
  let fail = true;
  await page.route(/class-results\/[^/]+\/sets\/[^/]+\/board/, async (route) => {
    if (!fail) return route.continue();
    await new Promise((r) => setTimeout(r, 3000));
    await route.fulfill({ status: 500, contentType: 'application/json', body: JSON.stringify({ code: 'INTERNAL', message: 'Lỗi giả lập', requestId: 'x', details: [] }) });
  });
  await openResults(page, sb);
  await expect(page.getByTestId('s3e3-skeleton')).toBeVisible();
  await expect(page.getByTestId('s3e3-board')).toHaveCount(0);
  await expect(rows(page)).toHaveCount(0);
  await expect(page.getByTestId('s3e3-error')).toContainText('Chưa tải được kết quả lớp.', { timeout: 20_000 });
  fail = false;
  await page.getByTestId('s3e3-retry').click();
  await expect(page.getByTestId('s3e3-board')).toBeVisible();
  await expect(rows(page)).toHaveCount(8);
  await expect(page.getByTestId('s3e3-error')).toHaveCount(0);
});

test('@TC-S3E3-21 học sinh Đã rời có bài làm: có hàng, chip "đã rời", vẫn xếp theo tên', async ({ page, request }) => {
  const sb = await board051(request, {
    students: [{ name: 'Văn Tú', status: 'left' }, { name: 'An Khê', status: 'left' }],
    cells: { 'Văn Tú': ['Đúng', 'Chưa làm', 'Chưa làm', 'Chưa làm'] },
  });
  await openBoard(page, sb);
  await expect(rows(page)).toHaveCount(9);
  expect(await rowNames(page)).toEqual([...NAMES, 'Văn Tú']);
  const tu = student(sb, 'Văn Tú');
  await expect(rowOf(page, tu).getByTestId('s3e3-left-chip')).toHaveText('đã rời');
  await expect(page.getByTestId('s3e3-left-chip')).toHaveCount(1);
  await expect(cellOf(page, tu, 1)).toHaveText('Đúng');
  await expect(cellOf(page, tu, 2)).toHaveText('Chưa làm');
  await expect(page.getByTestId('s3e3-active')).toHaveText('8 em Đang học');
  expect(BOARD['Bình An']).toHaveLength(4);
});
