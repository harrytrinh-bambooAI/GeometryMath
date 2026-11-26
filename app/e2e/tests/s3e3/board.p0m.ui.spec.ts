import { expect, test } from '../../support/fixtures.ts';
import { BOARD, board051, cellOf, COLUMNS, NAMES, openBoard, rowNames, rows, STATUS, student } from './s3e3.ts';

/** S3-E3 · C3-04 bảng lớp × bài (P0, chạy cả trên điện thoại): đúng từng ô, không cột tổng, không xếp hạng. */

test('@TC-S3E3-01 bảng 8 học sinh × 4 đề đúng từng ô theo F-S3-051', async ({ page, request }) => {
  const sb = await board051(request);
  await openBoard(page, sb);
  await expect(rows(page)).toHaveCount(8);
  await expect(page.getByTestId('s3e3-col')).toHaveText(COLUMNS);
  expect(await rowNames(page)).toEqual(NAMES);
  for (const name of NAMES) {
    for (let item = 1; item <= 4; item++) {
      const want = BOARD[name]![item - 1]!;
      const cell = cellOf(page, student(sb, name), item);
      await expect(cell).toHaveAttribute('data-status', STATUS[want]);
      await expect(cell).toHaveText(want);
    }
  }
  await expect(page.getByTestId('s3e3-active')).toHaveText('8 em Đang học');
});

test('@TC-S3E3-02 giao diện không có cột tổng, dòng tổng, phần trăm, điểm hay xếp hạng', async ({ page, request }) => {
  const sb = await board051(request);
  await openBoard(page, sb);
  await expect(page.getByTestId('s3e3-sub')).toHaveText('Xếp theo tên. Không có cột tổng, không xếp hạng.');
  const banned = /tổng|%|điểm|hạng|tốt nhất|kém nhất|trung bình/i;
  for (const id of ['s3e3-board', 's3e3-legend', 's3e3-active']) {
    expect(await page.getByTestId(id).innerText(), id).not.toMatch(banned);
  }
  await expect(page.getByTestId('s3e3-col')).toHaveCount(4);
  await expect(rows(page).last().locator('b')).toHaveText('Ngọc Anh');
  const card = page.getByTestId('s3e3-board-card');
  await expect(card.locator('[title]')).toHaveCount(0);
  for (const label of await card.locator('[aria-label]').evaluateAll((els) => els.map((e) => e.getAttribute('aria-label') ?? ''))) {
    expect(label).not.toMatch(banned);
  }
  await page.emulateMedia({ media: 'print' });
  expect(await page.getByTestId('s3e3-board').innerText()).not.toMatch(banned);
  await expect(page.getByTestId('s3e3-col')).toHaveCount(4);
});
