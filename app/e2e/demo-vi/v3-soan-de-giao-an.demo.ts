import { FIXTURE, test } from './kit-vi.ts';
import { signInPage } from '../tests/e1/e1.ts';
import { signIn, world } from '../tests/s3e1/s3e1.ts';
import { KT15, LAN, createSet, importBank, lanToken, type ExamSet } from '../tests/s5e2/s5e2.ts';
import { S5E3_WORLD, tl01 } from '../tests/s5e3/kb.ts';

/**
 * T-261128-75 · Chương 03 Soạn đề và giáo án cùng AI (clip câm).
 * Giáo án: chọn bài 24 "Phép chiếu vuông góc. Góc giữa đường thẳng và mặt phẳng" (PPCT lớp 11) — đúng chủ đề giáo án
 * Giao-an-Hinh-khong-gian-11A1.docx; bản nháp do AI giả lập soạn từ đơn vị kiến thức của bài.
 */

test('13-soan-de-prompt | AI soạn đề nháp từ tài liệu, máy kiểm chứng, giáo viên duyệt', async ({ page, request, demo }) => {
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
    await demo.mark(page.locator('[data-testid^="s5e2-draft-item-"]').first(), 2000);
    await page.mouse.wheel(0, 500);
    await demo.hold(2000);
    await demo.mark(page.getByTestId('s5e2-draft-approve'), 1800);
  });
});

test('14-ra-de-ma-tran | Ra đề theo ma trận, nhiều mã đề', async ({ page, request, demo }) => {
  await importBank(request);
  await signInPage(page, request, LAN);
  await demo.step('form ra đề', async () => {
    await page.goto('/gv/ra-de');
    await page.getByTestId('s5e2-form').waitFor();
    await demo.hold(1200);
    await page.getByTestId('s5e2-loai-de').selectOption(KT15);
    await demo.hold(1500);
    await demo.mark(page.getByTestId('s5e2-tong-diem'), 1800);
  });
  await demo.step('bộ đề', async () => {
    const r = await createSet(request, await lanToken(request), { matrixCode: KT15, variantCount: 4, seed: 2026 });
    const set = (await r.json()) as ExamSet;
    await page.goto(`/gv/ra-de/${set.id}`);
    await page.getByTestId('s5e2-nhan-nguoi').waitFor();
    await demo.hold(1500);
    await demo.tap(page.getByTestId('s5e2-ma-101'));
    await demo.hold(1200);
    await demo.mark(page.getByTestId('s5e2-cau-3'), 1500);
    await demo.tap(page.getByTestId('s5e2-cau-3-thay'));
    await demo.hold(2500);
  });
});

interface GiaoAn {
  mucTieuKienThuc: string;
  nangLuc: string[];
  hoatDong: Array<{ ten: string; noiDung: string; chuyenGiao: string; thucHien: string }>;
}
type Hd = { ten?: string; mucTieu?: string; noiDung?: string; toChuc?: Record<string, string> };

test('15-soan-giao-an | Soạn giáo án theo PPCT: bài 24, góc giữa đường thẳng và mặt phẳng', async ({ page, request, demo }) => {
  // AI giả lập soạn khung chung; nội dung hiển thị lấy từ giáo án thật 11A1 (fixtures/giao-an-11a1.json)
  const fx = FIXTURE<GiaoAn>('giao-an-11a1.json');
  await page.route(/\/api\/v1\/teacher\/lesson-plans\/[0-9a-f-]{36}$/, async (route) => {
    if (route.request().method() !== 'GET') return route.fallback();
    const res = await route.fetch();
    const body = (await res.json()) as { lesson?: { mucTieu?: { kienThuc?: Array<{ noi_dung?: string }>; nangLuc?: string[] }; hoatDong?: Hd[] } };
    const l = body.lesson;
    if (l?.mucTieu?.kienThuc?.[0]) l.mucTieu.kienThuc[0].noi_dung = fx.mucTieuKienThuc;
    if (l?.mucTieu) l.mucTieu.nangLuc = fx.nangLuc;
    l?.hoatDong?.forEach((h, i) => {
      const f = fx.hoatDong[i];
      if (!f) return;
      h.ten = f.ten;
      h.mucTieu = `HS đạt mục tiêu của ${f.ten}.`;
      h.noiDung = f.noiDung;
      if (h.toChuc) {
        h.toChuc.chuyenGiao = f.chuyenGiao;
        h.toChuc.thucHien = f.thucHien;
      }
    });
    await route.fulfill({ response: res, json: body });
  });
  await demo.step('form giáo án', async () => {
    const w = await world(request);
    await signIn(page, w.people['Cô Lan']!);
    await page.goto('/gv/giao-an/moi');
    await page.getByTestId('s5e1-form').waitFor();
    await demo.hold(1000);
    await page.getByTestId('s5e1-chapter').selectOption('7');
    await demo.hold(500);
    await page.getByTestId('s5e1-lesson').selectOption('24');
    await demo.hold(500);
    await page.getByTestId('s5e1-period').selectOption({ index: 1 }).catch(() => {});
    await demo.hold(1200);
    await demo.tap(page.getByTestId('s5e1-doituong-kha'));
    await demo.hold(800);
    await demo.tap(page.getByTestId('s5e1-submit'));
    await page.waitForURL(/\/gv\/giao-an\/[0-9a-f-]{36}$/);
    await page.locator('[data-testid="s5e1-result"]:not([data-status="drafting"])').waitFor({ timeout: 20_000 });
    await demo.hold(2500);
    await page.mouse.wheel(0, 600);
    await demo.hold(2500);
    await page.mouse.wheel(0, 700);
    await demo.hold(2500);
  });
});
