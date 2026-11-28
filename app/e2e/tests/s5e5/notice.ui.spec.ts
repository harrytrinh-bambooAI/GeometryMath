import { expect, test } from '../../support/fixtures.ts';
import { signIn } from '../s3e1/s3e1.ts';
import { BC01_TITLE, EIGHT, MSG, bc01, notifications, s5e5World } from './s5e5.ts';

/** US-S5E5-01 trên trình duyệt: "Trợ lý · Thông báo" → nháp "AI soạn" → sửa câu cuối → Gửi (AI giả lập class_notice). */

test('Soạn nháp nhắc hạn BC-01, sửa một câu và gửi @TC-S5E5-01', async ({ page, request }) => {
  const w = await s5e5World(request);
  const lan = w.people['Cô Lan']!;
  await bc01(request, lan, w.classes['11A1']!.id);
  await signIn(page, lan);
  await page.goto('/gv/tro-ly/thong-bao');
  await expect(page.getByTestId('s5e5-notice-page')).toBeVisible();
  await page.getByTestId('s5e5-class').selectOption({ label: '11A1' });
  await page.getByTestId('s5e5-kind').selectOption('due');
  await expect(page.getByTestId('s5e5-set')).toContainText(BC01_TITLE);
  await page.getByTestId('s5e5-compose').click();
  await expect(page.getByTestId('s5e5-label')).toHaveText('AI soạn');
  const body = page.getByTestId('s5e5-body');
  await expect(body).toHaveValue(/20:00, 11\/12\/2026/);
  // sửa câu cuối
  const text = await body.inputValue();
  const edited = text.replace(/Em nào cần hỏi thêm.*$/, 'Em nào nộp sớm cô sẽ chữa trước giờ học.');
  await body.fill(edited);
  await page.getByTestId('s5e5-send').click();
  await expect(page.getByTestId('s5e5-toast')).toHaveText(MSG.s55(8, '11A1'));
  // 8 em Đang học có thông báo; Văn Tú (Đã rời) không có
  for (const name of EIGHT) {
    const mine = (await notifications(request, w.people[name]!)).filter((n) => n.kind === 'class_notice');
    expect(mine, name).toHaveLength(1);
    expect(mine[0]!.body).toContain('Em nào nộp sớm cô sẽ chữa trước giờ học.');
  }
  expect((await notifications(request, w.people['Văn Tú']!)).filter((n) => n.kind === 'class_notice')).toHaveLength(0);
});
