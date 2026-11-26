import { expect, test } from '../../support/fixtures.ts';
import { active, aiMode, board051, insight, NAMES, openBoard, raw, rows, sandbox, script, stuckReady, type Cell, type ScriptInsight } from './s3e3.ts';

/** S3-E3 · "Cả lớp vướng ở đâu" — ca biên: bộ gộp quá giờ, nhận định không căn cứ, quá 5 thẻ, chưa ai làm. */

const T3 = 'Nhiều em xác định sai giao tuyến của (SAB) và (SCD)';
/** Chỉ Đề 3 đủ ngưỡng (7 bài làm): các đề khác chưa ai làm, nên bộ gộp chỉ được gọi cho Đề 3. */
const ONLY3: Record<string, Cell[]> = Object.fromEntries(
  NAMES.map((n) => [n, n === 'Khánh Linh' ? ['Chưa làm'] : ['Chưa làm', 'Chưa làm', 'Xem lại', 'Chưa làm']] as [string, Cell[]]),
);

test('@TC-S3E3-48 bộ gộp quá thời gian: lùi về dữ liệu thô kèm "Chưa gộp được. Thử lại."; Thử lại hiện thẻ', async ({ page, request }) => {
  test.setTimeout(90_000);
  const sb = await board051(request, { cells: ONLY3, events: [{ student: 'Chi Mai', item: 3, kind: 'hint', level: 1, at: '15:10' }] });
  await script(request, sb, { 3: [{ text: T3, works: [1, 2, 3, 4, 5, 6] }] });
  await aiMode(request, sb.teacher.id, 'timeout');
  try {
    await openBoard(page, sb);
    await expect(rows(page)).toHaveCount(8); // bảng lớp × bài dùng được trong lúc chờ bộ gộp
    await expect(page.getByTestId('s3e3-stuck-loading')).toBeVisible();
    await expect(raw(page, 3)).toHaveAttribute('data-mode', 'failed', { timeout: 40_000 });
    await expect(raw(page, 3).getByTestId('s3e3-msg-failed')).toContainText('Chưa gộp được. Thử lại.');
    await expect(raw(page, 3).getByTestId('s3e3-raw-entry')).toHaveCount(1);
    await expect(page.getByTestId('s3e3-insight')).toHaveCount(0);
  } finally {
    await aiMode(request, sb.teacher.id, 'ok');
  }
  await raw(page, 3).getByTestId('s3e3-stuck-retry').click();
  await expect(insight(page, 3).getByTestId('s3e3-insight-text')).toHaveText(T3, { timeout: 30_000 });
  await expect(page.getByTestId('s3e3-msg-failed')).toHaveCount(0);
  await expect(raw(page, 3)).toHaveCount(0);
});

test('@TC-S3E3-49 nhận định dẫn tới bài làm không tồn tại bị loại, không có thẻ rỗng', async ({ page, request }) => {
  const sb = await board051(request);
  await script(request, sb, {
    3: [
      { text: T3, works: [1, 2, 3] },
      { step: 2, text: 'Nhiều em tính sai góc', works: [42] },
    ],
  });
  await openBoard(page, sb);
  await stuckReady(page);
  await expect(page.getByTestId('s3e3-insight')).toHaveCount(1);
  await expect(insight(page, 3).getByTestId('s3e3-insight-text')).toHaveText(T3);
  await expect(insight(page, 3).getByTestId('s3e3-insight-open')).toHaveText('Xem 3 bài làm');
  await expect(page.getByTestId('s3e3-stuck')).not.toContainText('Nhiều em tính sai góc');
});

test('@TC-S3E3-50 quá 5 thẻ thì gập: 5 thẻ n lớn nhất, "Còn 2 nhận định khác" mở thêm 2 thẻ', async ({ page, request }) => {
  const five = ['Bình An', 'Chi Mai', 'Đức Huy', 'Gia Bảo', 'Hà My'];
  const doing: Cell[] = Array.from({ length: 20 }, (_, i) => (i < 7 ? 'Đang làm' : 'Chưa làm'));
  const sb = await sandbox(request, [
    { name: '11A1', students: active(five), sets: [{ title: 'Bộ hai mươi đề', items: 20, cells: Object.fromEntries(five.map((n) => [n, doing])) }] },
  ]);
  const sizes = [3, 5, 1, 4, 2, 5, 4]; // số bài làm căn cứ của nhận định ở Đề 1…7
  const byItem: Record<number, ScriptInsight[]> = {};
  sizes.forEach((n, i) => {
    byItem[i + 1] = [{ text: `Nhiều em dừng ở phần ${i + 1}`, works: Array.from({ length: n }, (_, k) => k + 1) }];
  });
  await script(request, sb, byItem);
  await openBoard(page, sb);
  await stuckReady(page);
  const opens = page.getByTestId('s3e3-insight-open');
  await expect(opens).toHaveText(['Xem 5 bài làm', 'Xem 5 bài làm', 'Xem 4 bài làm', 'Xem 4 bài làm', 'Xem 3 bài làm']);
  await expect(page.getByTestId('s3e3-insight-text')).toHaveText(['Nhiều em dừng ở phần 2', 'Nhiều em dừng ở phần 6', 'Nhiều em dừng ở phần 4', 'Nhiều em dừng ở phần 7', 'Nhiều em dừng ở phần 1']);
  await expect(page.getByTestId('s3e3-insight-more')).toHaveText('Còn 2 nhận định khác');
  await page.getByTestId('s3e3-insight-more').click();
  await expect(opens).toHaveText(['Xem 5 bài làm', 'Xem 5 bài làm', 'Xem 4 bài làm', 'Xem 4 bài làm', 'Xem 3 bài làm', 'Xem 2 bài làm', 'Xem 1 bài làm']);
  await expect(page.getByTestId('s3e3-insight-more')).toHaveCount(0);
});

test('@TC-S3E3-57 chưa ai làm thì không có thẻ nhận định, có câu chung "Chưa có bài làm."', async ({ page, request }) => {
  const sb = await sandbox(request, [{ name: '11A1', students: active(NAMES), sets: [{ title: 'Bộ bài tuần 16', items: 2 }] }]);
  await openBoard(page, sb);
  await stuckReady(page);
  await expect(page.getByTestId('s3e3-stuck-empty')).toHaveText('Chưa có bài làm.');
  await expect(page.getByTestId('s3e3-insight')).toHaveCount(0);
  await expect(page.getByTestId('s3e3-raw')).toHaveCount(0);
  await expect(page.getByTestId('s3e3-msg-failed')).toHaveCount(0);
});
