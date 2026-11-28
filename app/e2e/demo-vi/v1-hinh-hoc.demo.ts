import { test } from './kit-vi.ts';
import { PV as PV5, openBank, signInLane, filterBy } from '../tests/e5/e5.ts';
import { openWorkspace as e3Open, tapPoint as e3Tap, tool as e3Tool } from '../tests/e3/e3.ts';
import { signIn as s2e3SignIn, teacher as s2e3Teacher, stage } from '../tests/s2e3/s2e3.ts';
import { present } from '../tests/s2e7/s2e7.ts';

/**
 * T-261128-76 · v2.0 · Chương 01 Hỗ trợ hình học (clip câm; lời + phụ đề ghép ở make-video-vi.mjs).
 * Dữ liệu thật: HKG-01..06 của Ngan-hang-bai-tap-Hinh-khong-gian.xlsx (đã có trong seed); đề biến thể HKG-01 do giáo viên gõ.
 */

/** Đề biến thể của HKG-01 (SA = a√2, hỏi góc của SC) — khớp quy tắc mock FX-14 → hình HKN-02, đáp số 45°. */
const DE_BIEN_THE = 'Cho hình chóp S.ABCD có đáy là hình vuông cạnh a, SA ⊥ (ABCD), SA = a√2. Tính góc giữa SC và mặt phẳng (ABCD).';

test('05-kho-de-that | Kho đề: bài thật HKG-01..06, lọc', async ({ page, request, demo }) => {
  await signInLane(page, request);
  await demo.step('kho đề', async () => {
    await openBank(page);
    await demo.hold(2500);
    await demo.tap(page.getByTestId('e5-filter-level-thpt'));
    await demo.hold(1500);
    await filterBy(page, 'topic', 'angle');
    await demo.hold(2000);
    await page.mouse.wheel(0, 400);
    await demo.hold(2500);
  });
});

test('06-wow-dung-hinh | Gõ đề → dựng hình 3D → giải → kiểm chứng', async ({ page, request, demo }) => {
  await demo.step('gõ đề', async () => {
    await s2e3SignIn(page, request, s2e3Teacher(21, test.info()), '/gv/them-de');
    await page.getByTestId('s2e3-page').waitFor();
    await demo.hold(1000);
    const saved = page.waitForResponse((r) => r.url().includes('/api/v1/teacher/problem-drafts') && ['POST', 'PUT'].includes(r.request().method()));
    await demo.type(page.getByTestId('s2e3-statement'), DE_BIEN_THE, 45);
    await saved.catch(() => {});
    await demo.hold(800);
    await demo.tap(page.getByTestId('s2e3-build'));
  });
  await demo.step('bốn bước', async () => {
    await page.getByTestId('s2e3-stages').waitFor();
    await demo.mark(page.getByTestId('s2e3-stages'), 1500);
    await demo.mark(stage(page, 'build'), 1200);
    await page.locator('[data-testid="s2e3-stage-verify"][data-state="done"]').waitFor({ timeout: 25_000 }).catch(() => {});
    await page.getByTestId('s2e3-result').waitFor({ timeout: 25_000 });
    await demo.hold(1200);
    await page.getByTestId('s2e3-result').scrollIntoViewIfNeeded();
    await demo.mark(page.getByTestId('s2e3-machine'), 2500);
    await page.mouse.wheel(0, 400);
    await demo.hold(3000);
  });
});

test('07-chi-tiet-kiem-chung | Chi tiết HKG-01: hình, đáp số kiểm chứng, lời giải', async ({ page, request, demo }) => {
  await signInLane(page, request);
  await demo.step('chi tiết HKG-01', async () => {
    await page.goto(`/gv/de/${PV5('HKG-01')}`);
    await page.getByTestId('e2-problem-detail').waitFor();
    await demo.hold(2000);
    await demo.drag('viewer', 220, 30);
    await demo.hold(800);
    await demo.mark(page.getByTestId('e5-answer'), 2500);
    await page.mouse.wheel(0, 500);
    await demo.hold(3000);
    await page.mouse.wheel(0, 500);
    await demo.hold(3000);
  });
});

test('08-xoay-ve-do | Xoay hình 3D, vẽ thêm đoạn, đo độ dài và góc', async ({ page, request, demo }) => {
  await demo.step('xoay', async () => {
    await e3Open(page, request); // bài BG-1 của seed: hình chóp S.ABCD, SA ⊥ đáy, SA = a (HKG-01)
    await demo.hold(1500);
    await demo.drag('viewer', 240, 20);
    await demo.hold(600);
    await demo.drag('viewer', -160, 60);
    await demo.hold(1200);
  });
  await demo.step('vẽ thêm BD', async () => {
    await e3Tool(page, 'line');
    await e3Tap(page, 'B');
    await e3Tap(page, 'D');
    await demo.hold(1800);
  });
  await demo.step('đo SD', async () => {
    await e3Tool(page, 'length');
    await e3Tap(page, 'S');
    await e3Tap(page, 'D');
    await demo.hold(2200);
  });
  await demo.step('đo góc SAD', async () => {
    await e3Tool(page, 'angle');
    for (const x of ['S', 'A', 'D']) await e3Tap(page, x);
    await demo.hold(3000);
  });
});

test('09-trinh-chieu | Trình chiếu lời giải từng bước', async ({ page, request, demo }) => {
  await demo.step('trình chiếu', async () => {
    await present(page, request, test.info(), 1);
    await demo.hold(3000);
    await demo.tap(page.getByTestId('s2e7-next'));
    await demo.hold(3500);
    await page.evaluate(() => (document.activeElement as HTMLElement | null)?.blur());
    await page.keyboard.press('ArrowRight');
    await demo.hold(3500);
    await page.keyboard.press('ArrowRight');
    await demo.hold(3500);
  });
});
