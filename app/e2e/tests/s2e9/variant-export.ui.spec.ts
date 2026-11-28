import { expect, test } from '../../support/fixtures.ts';
import {
  API,
  K,
  MSG,
  bearer,
  confirmations,
  downloadBy,
  exportDocs,
  inkByBand,
  listLinks,
  listVariants,
  ok,
  openDoc,
  pdfInfo,
  pngSize,
  signInTeacher,
  slot,
  type ExportView,
} from './s2e9.ts';

/**
 * S2-E9 · giao diện giáo viên (web): biến thể mới trong "Của tôi" (US-S2E9-01-SC3), xuất ảnh PNG / PDF, hộp hỏi khi
 * đề đang giao còn khóa, liên kết chỉ xem (US-S2E9-02). Tệp do trình duyệt dựng (canvas → PNG, JPEG → PDF).
 */

test('@TC-S2E9-003 biến thể mới mang Cần xem lại và AI soạn', async ({ page, request }, info) => {
  const s = slot(3, info);
  const t = await signInTeacher(page, request, s);
  await openDoc(page, s.doc(K.FX12));
  await page.getByTestId('s2e9-variant-count').fill('1');
  await page.getByTestId('s2e9-variant-seed').fill('42');
  await page.getByTestId('s2e9-variant-create').click();
  await expect(page.getByTestId('s2e9-variant-message')).toContainText('Đã tạo 1 biến thể');
  await expect(page.getByTestId('s2e9-variant-1-machine')).toHaveText('Cần xem lại');
  await expect(page.getByTestId('s2e9-variant-1-human')).toHaveText('AI soạn');
  const [variant] = await listVariants(request, t, s.doc(K.FX12));

  // mở biến thể: hai nhãn, hình engine dựng, chưa "Giáo viên đã duyệt"
  await page.getByTestId('s2e9-variant-open-1').click();
  await expect(page.getByTestId('s2e9-doc')).toHaveAttribute('data-doc', variant!.docId);
  await expect(page.getByTestId('s2e9-is-variant')).toBeVisible();
  await expect(page.getByTestId('s2e9-doc-machine')).toHaveText('Cần xem lại');
  await expect(page.getByTestId('s2e9-doc-human')).toHaveText('AI soạn');
  await expect(page.getByTestId('s2e9-doc')).not.toContainText('Giáo viên đã duyệt');
  await expect(page.getByTestId('s2e9-doc-statement')).toHaveText(variant!.statement);
  const figure = page.getByTestId('s2e9-figure');
  await expect(figure).toBeVisible();
  await expect(figure).toHaveAttribute('data-coord-source', 'engine');
  // hình thỏa ràng buộc: độ dài AB, AD trên cảnh đúng bằng số trong đề của biến thể
  const view = await ok<ExportView>(await exportDocs(request, t, [variant!.docId]));
  const nums = /AB = (\d+), AD = (\d+)/.exec(variant!.statement)!;
  const pt = (id: string) => view.items[0]!.scene!.points.find((p) => p.id === id)!;
  const dist = (a: string, b: string) => Math.hypot(pt(a).x - pt(b).x, pt(a).y - pt(b).y);
  expect(dist('A', 'B')).toBeCloseTo(Number(nums[1]), 6);
  expect(dist('A', 'D')).toBeCloseTo(Number(nums[2]), 6);
  expect(variant!.answer).not.toBe('');
  // biến thể nằm ngăn "Của tôi" của Kho đề với đúng hai nhãn
  const mine = (await ok<{ items: Array<{ id: string; machineLabel: string; humanLabel: string }> }>(
    await request.get(`${API}/api/v1/teacher/library?shelf=mine&limit=50`, { headers: bearer(t) }),
  )).items.find((c) => c.id === variant!.docId);
  expect(mine).toMatchObject({ machineLabel: 'Cần xem lại', humanLabel: 'AI soạn' });
});

test('@TC-S2E9-014 xuất ảnh PNG của một đề', async ({ page, request }, info) => {
  const s = slot(14, info);
  await signInTeacher(page, request, s);
  await openDoc(page, s.doc(K.FX10));
  await expect(page.getByTestId('s2e9-with-solution')).not.toBeChecked(); // "Kèm lời giải" mặc định tắt
  const { download, bytes } = await downloadBy(page, () => page.getByTestId('s2e9-export-png').click());
  expect(download.suggestedFilename()).toBe('pl-01-goc-a-cua-tam-giac-5-12-13.png');
  expect(pngSize(bytes)).toEqual({ width: 1240, height: 1754 }); // một trang A4
  const done = page.getByTestId('s2e9-export-done');
  await expect(done).toHaveAttribute('data-format', 'png');
  await expect(done).toHaveAttribute('data-count', '1');
  await expect(done).toHaveAttribute('data-figures', '1');
  await expect(done).toHaveAttribute('data-with-solution', 'false');
  await expect(done).toHaveAttribute('data-steps', '0');
  await expect(done).toContainText('không có lời giải');
  // tệp có chữ đề (dải trên) và hình tam giác (dải giữa), phần dưới hình để trống (không có lời giải)
  const [text, figure, below] = await inkByBand(page, bytes, [
    [90, 330],
    [330, 900],
    [1000, 1650],
  ]);
  expect(text!).toBeGreaterThan(1500);
  expect(figure!).toBeGreaterThan(1500);
  expect(below!).toBe(0);
});

test('@TC-S2E9-015 xuất PDF nhiều đề', async ({ page, request }, info) => {
  const s = slot(15, info);
  await signInTeacher(page, request, s);
  await page.goto('/gv/xuat-de');
  await expect(page.getByTestId('s2e9-page')).toBeVisible();
  const order = [s.doc(K.FX10), s.doc(K.FX11), s.doc(K.FX13)];
  for (const id of order) await page.locator(`[data-doc="${id}"] input[type="checkbox"]`).check();
  await expect(page.getByTestId('s2e9-picked')).toHaveText('Đã chọn 3/20');
  await expect(page.getByTestId('s2e9-with-solution')).not.toBeChecked();
  const { download, bytes } = await downloadBy(page, () => page.getByTestId('s2e9-export-pdf').click());
  expect(download.suggestedFilename()).toBe('phieu-de.pdf');
  expect(pdfInfo(bytes)).toEqual({ pages: 3, images: 3, count: 3 }); // mỗi đề một trang
  const done = page.getByTestId('s2e9-export-done');
  await expect(done).toHaveAttribute('data-order', order.join(',')); // đúng thứ tự đã chọn
  await expect(done).toHaveAttribute('data-figures', '3'); // mỗi đề có hình
  await expect(done).toHaveAttribute('data-with-solution', 'false');
  await expect(done).toHaveAttribute('data-steps', '0');
  await expect(done).toContainText('Đã xuất 3 đề ra tệp PDF (3 trang), không có lời giải.');
});

test('@TC-S2E9-016 kèm lời giải thì in cả hai nhãn', async ({ page, request }, info) => {
  const s = slot(16, info);
  const t = await signInTeacher(page, request, s);
  await openDoc(page, s.doc(K.FX14));
  await expect(page.getByTestId('s2e9-doc-machine')).toHaveText('Đã kiểm chứng');
  await expect(page.getByTestId('s2e9-doc-human')).toHaveText('Giáo viên đã duyệt');
  await page.getByTestId('s2e9-with-solution').check();
  const { bytes } = await downloadBy(page, () => page.getByTestId('s2e9-export-pdf').click());
  await expect(page.getByTestId('s2e9-confirm')).toHaveCount(0); // đề chưa giao: không hỏi lại
  expect(pdfInfo(bytes).pages).toBeGreaterThanOrEqual(1);
  const done = page.getByTestId('s2e9-export-done');
  await expect(done).toHaveAttribute('data-with-solution', 'true');
  await expect(done).toHaveAttribute('data-steps', '4'); // lời giải đủ 4 bước
  await expect(done).toContainText('có lời giải');
  // nội dung in: hai nhãn (đầu trang) + 4 bước, đúng như máy chủ cấp cho bản xuất
  const view = await ok<ExportView>(await exportDocs(request, t, [s.doc(K.FX14)], true));
  expect(view.items[0]!.solution).toMatchObject({ machineLabel: 'Đã kiểm chứng', humanLabel: 'Giáo viên đã duyệt' });
  expect(view.items[0]!.solution!.steps).toHaveLength(4);
  expect(view.items[0]!.solution!.steps[3]).toContain('45°');
  expect(await confirmations(request, t)).toBe(0);
});

test('@TC-S2E9-018 đề đang giao và khóa phải xác nhận trước khi xuất kèm lời giải', async ({ page, request }, info) => {
  const s = slot(18, info); // seed: FX-14 giao BG-1, lời giải khóa tới T0 + 2 giờ
  const t = await signInTeacher(page, request, s);
  await openDoc(page, s.doc(K.FX14));
  let downloads = 0;
  page.on('download', () => downloads++);
  await page.getByTestId('s2e9-with-solution').check();
  await page.getByTestId('s2e9-export-pdf').click();
  await expect(page.getByTestId('s2e9-confirm')).toBeVisible();
  await expect(page.getByTestId('s2e9-confirm-text')).toHaveText(MSG.confirmLocked);
  await page.getByTestId('s2e9-confirm-no').click(); // "Hủy": không tệp
  await expect(page.getByTestId('s2e9-confirm')).toHaveCount(0);
  await expect(page.getByTestId('s2e9-export-done')).toHaveCount(0);
  expect(downloads).toBe(0);
  expect(await confirmations(request, t)).toBe(0);

  await page.getByTestId('s2e9-export-pdf').click();
  await expect(page.getByTestId('s2e9-confirm')).toBeVisible();
  const { bytes } = await downloadBy(page, () => page.getByTestId('s2e9-confirm-yes').click()); // "Vẫn xuất": có tệp
  expect(pdfInfo(bytes).pages).toBeGreaterThanOrEqual(1);
  await expect(page.getByTestId('s2e9-export-done')).toHaveAttribute('data-with-solution', 'true');
  expect(await confirmations(request, t)).toBe(1); // ghi lại việc xác nhận
  // máy chủ cũng chặn khi chưa xác nhận (không dựa vào giao diện)
  const direct = await exportDocs(request, t, [s.doc(K.FX14)], true, false);
  expect(direct.status()).toBe(409);
  expect(((await direct.json()) as { message: string }).message).toBe(MSG.confirmLocked);
  // không kèm lời giải thì không hỏi
  expect((await exportDocs(request, t, [s.doc(K.FX14)], false, false)).status()).toBe(200);
});

test('@TC-S2E9-020 tạo liên kết chỉ xem mặc định hạn 7 ngày', async ({ page, request, browser }, info) => {
  const s = slot(20, info);
  const t = await signInTeacher(page, request, s);
  await openDoc(page, s.doc(K.FX10));
  await expect(page.getByTestId('s2e9-link-days')).toHaveValue('7');
  await page.getByTestId('s2e9-link-create').click();
  await expect(page.getByTestId('s2e9-link-new')).toHaveAttribute('data-days', '7');
  const url = await page.getByTestId('s2e9-link-url').inputValue();
  expect(url).toMatch(/\/api\/v1\/share\/[A-Za-z0-9_-]{43}$/); // mã ngẫu nhiên dài
  await expect(page.getByTestId('s2e9-link-copy')).toBeVisible();
  await expect(page.getByTestId('s2e9-link-copy')).toHaveText('Sao chép');
  await expect(page.getByTestId('s2e9-link-label-1')).toHaveText('Hạn 7 ngày');
  await expect(page.getByTestId('s2e9-link-revoke-1')).toHaveText('Thu hồi');
  const [link] = await listLinks(request, t, s.doc(K.FX10));
  expect(link!.days).toBe(7);
  expect(new Date(link!.expiresAt).getTime() - new Date(link!.createdAt).getTime()).toBe(7 * 86_400_000);

  // người nhận mở liên kết ở trình duyệt CHƯA đăng nhập: chữ đề + hình, không nút sửa, không danh tính giáo viên
  const guest = await browser.newContext();
  const view = await guest.newPage();
  await view.goto(url);
  await expect(view.getByTestId('s2e9-share-statement')).toHaveText('Tam giác ABC có AB = 5, AC = 12, BC = 13. Tính góc A.');
  await expect(view.getByTestId('s2e9-share-figure')).toBeVisible();
  await expect(view.locator('button, input, form, a')).toHaveCount(0);
  await expect(view.locator('body')).not.toContainText(/Cô Trang|GV-|90°/);
  await guest.close();

  // "Thu hồi" trên màn: liên kết biến mất và không mở được nữa
  await page.getByTestId('s2e9-link-revoke-1').click();
  await expect(page.getByTestId('s2e9-link-empty')).toBeVisible();
  expect((await request.get(url)).status()).toBe(404);
});

test('@TC-S2E9-028 chọn hạn 1 hoặc 30 ngày', async ({ page, request }, info) => {
  const s = slot(28, info);
  const t = await signInTeacher(page, request, s);
  await openDoc(page, s.doc(K.FX10));
  const days = page.getByTestId('s2e9-link-days');
  await expect(days.locator('option')).toHaveText(['1 ngày', '7 ngày', '30 ngày']); // không có lựa chọn khác
  await days.selectOption('1');
  await page.getByTestId('s2e9-link-create').click();
  await expect(page.getByTestId('s2e9-link-new')).toHaveAttribute('data-days', '1');
  await expect(page.getByTestId('s2e9-link-label-1')).toHaveText('Hạn 1 ngày');
  await days.selectOption('30');
  await page.getByTestId('s2e9-link-create').click();
  await expect(page.getByTestId('s2e9-link-new')).toHaveAttribute('data-days', '30');
  await expect(page.getByTestId('s2e9-link-label-1')).toHaveText('Hạn 30 ngày');
  await expect(page.getByTestId('s2e9-link-label-2')).toHaveText('Hạn 1 ngày');
  const span = (l: { createdAt: string; expiresAt: string }) => (new Date(l.expiresAt).getTime() - new Date(l.createdAt).getTime()) / 86_400_000;
  const links = await listLinks(request, t, s.doc(K.FX10));
  expect(links.map((l) => l.days).sort((a, b) => a - b)).toEqual([1, 30]);
  expect(links.map(span).sort((a, b) => a - b)).toEqual([1, 30]); // hạn ghi đúng 1 ngày và 30 ngày
});
