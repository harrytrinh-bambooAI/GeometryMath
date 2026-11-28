import { lessonPlanOverride, test } from './kit-vi.ts';
import { signInPage } from '../tests/e1/e1.ts';
import { signIn, world } from '../tests/s3e1/s3e1.ts';
import { KT15, LAN, createSet, importBank, lanToken, type ExamSet } from '../tests/s5e2/s5e2.ts';
import { S5E3_WORLD, tl01 } from '../tests/s5e3/kb.ts';
import { bc01 as s5e5bc01, s5e5World } from '../tests/s5e5/s5e5.ts';

/**
 * T-261128-76 · v2.0 · Chương 03 Soạn giáo án và đề (clip câm).
 * Giáo án: PPCT bài 24 "Phép chiếu vuông góc. Góc giữa đường thẳng và mặt phẳng" — chủ đề của Giao-an-Hinh-khong-gian-11A1.docx;
 * chữ bản nháp lấy từ fixtures/giao-an-11a1.json (AI giả lập).
 */

test('16-soan-giao-an | Soạn giáo án theo PPCT: bài 24', async ({ page, request, demo }) => {
  await lessonPlanOverride(page);
  await demo.step('form giáo án', async () => {
    const w = await world(request);
    await signIn(page, w.people['Cô Lan']!);
    await page.goto('/gv/giao-an/moi');
    await page.getByTestId('s5e1-form').waitFor();
    await demo.hold(1200);
    await page.getByTestId('s5e1-chapter').selectOption('7');
    await demo.hold(600);
    await page.getByTestId('s5e1-lesson').selectOption('24');
    await demo.hold(600);
    await page.getByTestId('s5e1-period').selectOption({ index: 1 }).catch(() => {});
    await demo.hold(1500);
    await demo.tap(page.getByTestId('s5e1-doituong-kha'));
    await demo.hold(800);
    await demo.tap(page.getByTestId('s5e1-submit'));
    await page.waitForURL(/\/gv\/giao-an\/[0-9a-f-]{36}$/);
    await page.locator('[data-testid="s5e1-result"]:not([data-status="drafting"])').waitFor({ timeout: 20_000 });
    await demo.hold(3000);
    for (let i = 0; i < 4; i++) {
      await page.mouse.wheel(0, 500);
      await demo.hold(2800);
    }
  });
});

test('17-ra-de-ma-tran | Ra đề theo ma trận, nhiều mã đề', async ({ page, request, demo }) => {
  await importBank(request);
  await signInPage(page, request, LAN);
  await demo.step('form ra đề', async () => {
    await page.goto('/gv/ra-de');
    await page.getByTestId('s5e2-form').waitFor();
    await demo.hold(1500);
    await page.getByTestId('s5e2-loai-de').selectOption(KT15);
    await demo.hold(2000);
    await demo.mark(page.getByTestId('s5e2-tong-diem'), 2000);
  });
  await demo.step('bộ đề', async () => {
    const r = await createSet(request, await lanToken(request), { matrixCode: KT15, variantCount: 4, seed: 2026 });
    const set = (await r.json()) as ExamSet;
    await page.goto(`/gv/ra-de/${set.id}`);
    await page.getByTestId('s5e2-nhan-nguoi').waitFor();
    await demo.hold(2000);
    await demo.tap(page.getByTestId('s5e2-ma-101'));
    await demo.hold(1500);
    await demo.mark(page.getByTestId('s5e2-cau-3'), 1800);
    await demo.tap(page.getByTestId('s5e2-cau-3-thay'));
    await demo.hold(3000);
  });
});

test('18-soan-de-tai-lieu | AI soạn đề nháp từ tài liệu, máy kiểm chứng, giáo viên duyệt', async ({ page, request, demo }) => {
  await demo.step('đề nháp', async () => {
    const w = await world(request, S5E3_WORLD);
    const lan = w.people['Cô Lan']!;
    const c11 = w.classes['11A1']!;
    const doc = await tl01(request, lan, c11.id);
    await signIn(page, lan);
    await page.goto(`/gv/ra-de/tu-tai-lieu/${doc.id}?lop=${c11.id}`);
    await page.getByTestId('s5e2-draft').waitFor();
    await demo.hold(2000);
    await demo.tap(page.getByTestId('s5e2-draft-run'));
    await page.locator('[data-testid^="s5e2-draft-item-"]').first().waitFor({ timeout: 20_000 });
    await demo.hold(1500);
    await demo.mark(page.locator('[data-testid^="s5e2-draft-item-"]').first(), 2200);
    await page.mouse.wheel(0, 500);
    await demo.hold(2200);
    await demo.mark(page.getByTestId('s5e2-draft-approve'), 2000);
  });
});

test('19-thong-bao-cong-tac | Thông báo do trợ lý soạn, công tắc AI theo lớp', async ({ page, request, demo }) => {
  await demo.step('thông báo', async () => {
    const w = await s5e5World(request);
    const lan = w.people['Cô Lan']!;
    await s5e5bc01(request, lan, w.classes['11A1']!.id);
    await signIn(page, lan);
    await page.goto('/gv/tro-ly/thong-bao');
    await page.getByTestId('s5e5-notice-page').waitFor();
    await page.getByTestId('s5e5-class').selectOption({ label: '11A1' });
    await page.getByTestId('s5e5-kind').selectOption('due');
    await demo.hold(1200);
    await demo.tap(page.getByTestId('s5e5-compose'));
    await page.getByTestId('s5e5-label').waitFor();
    await demo.hold(2000);
    const body = page.getByTestId('s5e5-body');
    const text = await body.inputValue();
    await body.fill(text.replace(/Em nào cần hỏi thêm.*$/, 'Em nào nộp sớm cô sẽ chữa trước giờ học.'));
    await demo.hold(1500);
    await demo.tap(page.getByTestId('s5e5-send'));
    await page.getByTestId('s5e5-toast').waitFor();
    await demo.hold(1200);
    const ai = page.getByTestId('s5e5-ai-class');
    await ai.scrollIntoViewIfNeeded();
    await demo.mark(ai, 1500);
    await demo.mark(page.getByTestId('s5e5-usage'), 2000);
  });
});
