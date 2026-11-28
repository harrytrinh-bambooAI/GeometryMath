import { expect, test } from '../../support/fixtures.ts';
import { signIn, world } from '../s3e1/s3e1.ts';
import { accept, chatOk, patchScript, readyPlan, view } from './patch.ts';

/** US-S5E1-04 chat vá trên trình duyệt (màn kết quả C5-08): chọn mục, bản vá trước → sau, Nhận / Bỏ, Hoàn tác. */

test('Chọn HĐ1, gửi yêu cầu rõ, nhận cả bản vá @TC-S5E1-20', async ({ page, request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!;
  const { id } = await readyPlan(request, lan);
  await signIn(page, lan);
  await page.goto(`/gv/giao-an/${id}`);
  await expect(page.getByTestId('s5e1-version')).toHaveText('Phiên bản 1');
  const ten = page.getByTestId('s5e1-hd-ten-HD1');
  await expect(ten).toContainText('Khởi động: Khoảng cách từ điểm');
  await page.getByTestId('s5e1-pick-HD1').click();
  await expect(page.getByTestId('s5e1-selected')).toHaveText('HĐ1');
  await page.getByTestId('s5e1-chat-input').fill('Đổi thành trò chơi Ai nhanh hơn, giữ 5 phút');
  await page.getByTestId('s5e1-chat-send').click();
  await expect(page.getByTestId('s5e1-patch')).toBeVisible();
  // 7 cặp trước → sau, mỗi cặp có lý do; HĐ1 chưa đổi trước khi Nhận
  for (let i = 0; i < 7; i++) {
    await expect(page.getByTestId(`s5e1-patch-after-${i}`)).not.toBeEmpty();
    await expect(page.getByTestId(`s5e1-patch-why-${i}`)).toContainText('Lý do: ');
  }
  await expect(page.getByTestId('s5e1-patch-before-0')).toHaveText('Khởi động: Khoảng cách từ điểm');
  await expect(page.getByTestId('s5e1-patch-after-0')).toHaveText('Trò chơi: Ai nhanh hơn');
  await expect(ten).toContainText('Khởi động: Khoảng cách từ điểm');
  await expect(page.getByTestId('s5e1-version')).toHaveText('Phiên bản 1');
  await page.getByTestId('s5e1-patch-accept-all').click();
  await expect(ten).toContainText('Trò chơi: Ai nhanh hơn');
  await expect(page.getByTestId('s5e1-total')).toContainText("Σ 45'");
  await expect(page.getByTestId('s5e1-version')).toHaveText('Phiên bản 2');
  const v = await view(request, lan, id);
  expect(v.version).toBe(2);
  expect(v.lesson.hoatDong[0]!.thoiLuongPhut).toBe(5);
});

test('Nhận một dòng, bỏ dòng còn lại, rồi hoàn tác @TC-S5E1-21', async ({ page, request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!;
  const { id } = await readyPlan(request, lan);
  // phiên bản 2: một bản vá HĐ2 đã nhận
  const p = await chatOk(request, lan, id, 'HD2', 'Thêm ví dụ thực tế');
  await accept(request, lan, id, p.patchId!);
  const v2 = await view(request, lan, id);
  expect(v2.version).toBe(2);
  const hd1 = v2.lesson.hoatDong[0]!;
  await patchScript(request, lan, ['patch-2']);
  await signIn(page, lan);
  await page.goto(`/gv/giao-an/${id}`);
  await expect(page.getByTestId('s5e1-version')).toHaveText('Phiên bản 2');
  await page.getByTestId('s5e1-pick-HD1').click();
  await page.getByTestId('s5e1-chat-input').fill('Đổi thành trò chơi Ai nhanh hơn, giữ 5 phút');
  await page.getByTestId('s5e1-chat-send').click();
  await expect(page.getByTestId('s5e1-patch-line-1')).toBeVisible();
  await expect(page.getByTestId('s5e1-patch-line-2')).toHaveCount(0);
  // Nhận dòng 1 (tên), Bỏ dòng 2 (nội dung)
  await page.getByTestId('s5e1-patch-accept-0').click();
  await expect(page.getByTestId('s5e1-patch-line-0')).toHaveAttribute('data-decision', 'accepted');
  await page.getByTestId('s5e1-patch-skip-1').click();
  await expect(page.getByTestId('s5e1-patch-line-1')).toHaveAttribute('data-decision', 'skipped');
  await expect(page.getByTestId('s5e1-version')).toHaveText('Phiên bản 3');
  await expect(page.getByTestId('s5e1-hd-ten-HD1')).toContainText('Trò chơi: Ai nhanh hơn');
  await expect(page.getByTestId('s5e1-hd-noi-dung-HD1')).toHaveText(hd1.noiDung);
  // Hoàn tác: HĐ1 về đúng phiên bản 2
  await page.getByTestId('s5e1-undo').click();
  await expect(page.getByTestId('s5e1-version')).toHaveText('Phiên bản 2');
  await expect(page.getByTestId('s5e1-hd-ten-HD1')).toContainText(hd1.ten);
  const back = await view(request, lan, id);
  expect(back.version).toBe(2);
  expect(back.lesson.hoatDong[0]).toEqual(hd1);
});
