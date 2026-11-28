import { APP_DIR, API_URL, WEB_URL, pw } from './math-render.env.ts';
import { execFileSync } from 'node:child_process';
import { mkdirSync } from 'node:fs';
import path from 'node:path';
import type { Page } from '@playwright/test';

/**
 * T-261128-74 (FIX, owner 28/11 17:14, video demo T-73 mốc 3:10, 4:21, 4:39, 5:11): mọi chuỗi toán LaTeX hiển thị
 * bằng KaTeX (shared/math/MathText.tsx) ở các màn giáo viên + học sinh; hình SP-01 khi trình chiếu lấp khung ở 1920x1080.
 * Mỗi màn: (1) không còn text node (ngoài .katex) chứa mẫu LaTeX thô; (2) có phần tử .katex ở màn có toán.
 * Chạy: `cd app && npx playwright test e2e/tests/ui/math-render.spec.ts` (tự đọc cổng app/.env.ports; chưa có máy chủ
 * + web thì tự bật bằng e2e/demo/stack-up.sh và tắt khi xong). Helper e2e nạp bằng import() động: xem math-render.env.ts.
 * Ảnh sau sửa: artifacts/GEO/evidence/FIX-MATH-20261128/*-sau.png (ảnh trước trích từ video demo, cùng mốc).
 */
const { test, expect } = pw;
const RAW = /\$[^$]+\$|\\(dfrac|frac|perp|sqrt|approx|circ|parallel|subset)/;
const EVIDENCE = path.resolve(APP_DIR, '../artifacts/GEO/evidence/FIX-MATH-20261128');
mkdirSync(EVIDENCE, { recursive: true });

test.use({ baseURL: WEB_URL, viewport: { width: 1920, height: 1080 }, locale: 'vi-VN', timezoneId: 'Asia/Ho_Chi_Minh' });
test.describe.configure({ timeout: 120_000 });

let started = false;
async function up(): Promise<boolean> {
  try {
    const [a, w] = await Promise.all([fetch(`${API_URL}/health`), fetch(WEB_URL)]);
    return a.ok && w.ok;
  } catch {
    return false;
  }
}
test.beforeAll(async () => {
  test.setTimeout(900_000);
  if (await up()) return;
  // Chưa có máy chủ + web: build (nếu thiếu) rồi bật stack riêng của worktree (AI giả lập, DB geo_e2e, cổng .env.ports).
  const env = { ...process.env };
  delete env.GEO_AI_PROVIDER;
  const sh = (cmd: string) => execFileSync('bash', ['-c', cmd], { cwd: APP_DIR, stdio: 'inherit', env });
  sh('[ -f server/target/geo-server-0.1.0-SNAPSHOT.jar ] || (cd server && ./mvnw -B -ntp -q -DskipTests package)');
  sh('cd web && { [ -d node_modules ] || npm ci --no-audit --no-fund; } && npm run build:e2e >/dev/null');
  sh('bash e2e/demo/stack-up.sh');
  started = true;
});
test.afterAll(() => {
  if (started) execFileSync('bash', ['e2e/demo/stack-down.sh'], { cwd: APP_DIR, stdio: 'inherit' });
});

/** Text node chứa LaTeX thô (bỏ qua bên trong .katex — phần MathML có chú thích TeX gốc). */
async function rawLatex(page: Page, root = 'body'): Promise<string[]> {
  return page.evaluate(
    ([sel, src]) => {
      const re = new RegExp(src);
      const out: string[] = [];
      const host = document.querySelector(sel) ?? document.body;
      const w = document.createTreeWalker(host, NodeFilter.SHOW_TEXT);
      for (let n = w.nextNode(); n; n = w.nextNode()) {
        const el = n.parentElement;
        if (!el || el.closest('.katex, script, style, noscript')) continue;
        const t = n.textContent ?? '';
        if (re.test(t)) out.push(`${el.closest('[data-testid]')?.getAttribute('data-testid') ?? el.tagName}: ${t.slice(0, 140)}`);
      }
      return out;
    },
    [root, RAW.source] as const,
  );
}

async function expectMath(page: Page, opts: { root?: string; katex?: number } = {}) {
  const root = opts.root ?? 'body';
  await expect.poll(() => rawLatex(page, root), { message: 'không còn LaTeX thô', timeout: 10_000 }).toEqual([]);
  if (opts.katex) await expect.poll(() => page.locator(`${root} .katex`).count(), { message: 'có .katex' }).toBeGreaterThanOrEqual(opts.katex);
}

/** Tỉ lệ diện tích canvas/SVG lớn nhất (phần nằm trong khung) so với khung chứa. */
async function fill(page: Page, container: string): Promise<number> {
  return page.evaluate((sel) => {
    const box = document.querySelector(sel);
    if (!box) return 0;
    const b = box.getBoundingClientRect();
    let best = 0;
    for (const el of box.querySelectorAll('canvas, svg')) {
      const r = el.getBoundingClientRect();
      const w = Math.max(0, Math.min(r.right, b.right) - Math.max(r.left, b.left));
      const h = Math.max(0, Math.min(r.bottom, b.bottom) - Math.max(r.top, b.top));
      best = Math.max(best, w * h);
    }
    return b.width * b.height > 0 ? best / (b.width * b.height) : 0;
  }, container);
}

const shot = (page: Page, name: string) => page.screenshot({ path: path.join(EVIDENCE, `${name}-sau.png`) });

test('Trình chiếu SP-01: hình lấp khung ở 1920x1080, đổi cỡ vẫn lấp; lời giải KaTeX (mốc 3:10)', async ({ page, request }, info) => {
  const { present, nextBtn } = await import('../s2e7/s2e7.ts');
  await present(page, request, info, 1);
  const fig = '[data-testid="s2e7-stage"] .s2e7-figure';
  await expect(page.locator(fig)).toBeVisible();
  await expect.poll(() => fill(page, fig), { message: 'hình ≥ 80% khung' }).toBeGreaterThanOrEqual(0.8);
  const vp = await page.evaluate(() => ({ w: window.innerWidth, h: window.innerHeight }));
  expect(vp, 'trình chiếu giữ khung 1920x1080').toEqual({ w: 1920, h: 1080 });
  await shot(page, '01-trinh-chieu-hinh-SP01');
  await nextBtn(page).click();
  await nextBtn(page).click();
  await expectMath(page, { root: '[data-testid="s2e7-stage"]' });
  // Cửa sổ toàn màn hình không đổi cỡ được: thoát toàn màn hình (bố cục lớp phủ lấp trang, US-S2E7-01-FC2) rồi đổi cỡ.
  await page.evaluate(() => (document.fullscreenElement ? document.exitFullscreen() : undefined));
  for (const size of [{ width: 1280, height: 800 }, { width: 1920, height: 1080 }]) {
    await page.setViewportSize(size);
    await expect.poll(() => fill(page, fig), { message: `hình ≥ 80% khung ở ${size.width}x${size.height}` }).toBeGreaterThanOrEqual(0.8);
  }
});

test('Chi tiết đề (Kho đề HKG-01): hình lấp khung, đề không còn LaTeX thô', async ({ page, request }) => {
  const { PV, signInLane } = await import('../e5/e5.ts');
  await signInLane(page, request);
  await page.goto(`/gv/de/${PV('HKG-01')}`);
  await expect(page.getByTestId('viewer')).toBeVisible();
  await expectMath(page);
  await expect.poll(() => fill(page, '[data-testid="viewer"]')).toBeGreaterThanOrEqual(0.8);
});

test('Soạn giáo án: Kiến thức trọng tâm + bản nháp (mục tiêu, tiến trình) bằng KaTeX (mốc 4:21, 4:39)', async ({ page, request }) => {
  const { signIn, world } = await import('../s3e1/s3e1.ts');
  const w = await world(request);
  await signIn(page, w.people['Cô Lan']!);
  await page.goto('/gv/giao-an/moi');
  await page.getByTestId('s5e1-form').waitFor();
  await page.getByTestId('s5e1-chapter').selectOption('7');
  await page.getByTestId('s5e1-lesson').selectOption('26');
  await page.getByTestId('s5e1-period').selectOption({ label: 'Tiết 1/2' });
  await expect(page.locator('[data-testid="s5e1-knowledge"] label').first()).toBeVisible();
  await expectMath(page, { root: '[data-testid="s5e1-knowledge"]', katex: 3 });
  await expectMath(page);
  await shot(page, '02-soan-giao-an-kien-thuc');
  await page.getByTestId('s5e1-doituong-kha').click();
  await page.getByTestId('s5e1-submit').click();
  await page.waitForURL(/\/gv\/giao-an\/[0-9a-f-]{36}$/);
  await page.locator('[data-testid="s5e1-result"]:not([data-status="drafting"])').waitFor({ timeout: 30_000 });
  await expectMath(page);
  await shot(page, '03-giao-an-ban-nhap');
});

test('Ra đề theo ma trận: đề + 4 phương án A–D bằng KaTeX (mốc 5:11)', async ({ page, request }) => {
  const { signInPage } = await import('../e1/e1.ts');
  const { KT15, LAN, createSet, importBank, lanToken } = await import('../s5e2/s5e2.ts');
  await importBank(request);
  await signInPage(page, request, LAN);
  const r = await createSet(request, await lanToken(request), { matrixCode: KT15, variantCount: 4, seed: 2026 });
  const set = (await r.json()) as { id: string };
  await page.goto(`/gv/ra-de/${set.id}`);
  await page.getByTestId('s5e2-nhan-nguoi').waitFor();
  await page.getByTestId('s5e2-ma-101').click();
  await expectMath(page, { katex: 12 });
  await expect(page.locator('[data-testid="s5e2-cau-1"] .s5e2-options li .katex')).toHaveCount(4);
  await page.getByTestId('s5e2-cau-3').scrollIntoViewIfNeeded();
  await shot(page, '04-ra-de-ma-tran-phuong-an');
});

test('Đề nháp từ tài liệu: không còn LaTeX thô', async ({ page, request }) => {
  const { signIn, world } = await import('../s3e1/s3e1.ts');
  const { S5E3_WORLD, tl01 } = await import('../s5e3/kb.ts');
  const w = await world(request, S5E3_WORLD);
  const lan = w.people['Cô Lan']!;
  const c11 = w.classes['11A1']!;
  const doc = await tl01(request, lan, c11.id);
  await signIn(page, lan);
  await page.goto(`/gv/ra-de/tu-tai-lieu/${doc.id}?lop=${c11.id}`);
  await page.getByTestId('s5e2-draft').waitFor();
  await page.getByTestId('s5e2-draft-run').click();
  await page.locator('[data-testid^="s5e2-draft-item-"]').first().waitFor({ timeout: 30_000 });
  await expectMath(page);
  await shot(page, '05-de-nhap-tai-lieu');
});

test('Kho tri thức + hỏi đáp: không còn LaTeX thô', async ({ page, request }) => {
  const { signIn } = await import('../s3e1/s3e1.ts');
  const { setup } = await import('../s5e3/qa.ts');
  const c = await setup(request);
  await signIn(page, c.lan);
  await page.goto('/gv/kho-tri-thuc');
  await page.waitForLoadState('networkidle');
  await expectMath(page);
  await signIn(page, c.an);
  await page.goto(`/hs/hoi-dap?bai=${c.bc.id}`);
  await page.waitForLoadState('networkidle');
  await expectMath(page);
});

test('Rubric, Thông báo, Lỗi sai lớp: không còn LaTeX thô', async ({ page, request }) => {
  const { signIn } = await import('../s3e1/s3e1.ts');
  const { rb01, s5e4World } = await import('../s5e4/s5e4.ts');
  const { bc01, s5e5World } = await import('../s5e5/s5e5.ts');
  const { graded, summary } = await import('../s5e5/insights.ts');
  const w4 = await s5e4World(request);
  const lan4 = w4.people['Cô Lan']!;
  const rb = await rb01(request, lan4);
  await signIn(page, lan4);
  await page.goto(`/gv/rubric?id=${rb.id}`);
  await page.getByTestId('s5e4-editor').waitFor();
  await expectMath(page);

  const w5 = await s5e5World(request);
  const lan5 = w5.people['Cô Lan']!;
  await bc01(request, lan5, w5.classes['11A1']!.id);
  await signIn(page, lan5);
  await page.goto('/gv/tro-ly/thong-bao');
  await page.getByTestId('s5e5-notice-page').waitFor();
  await expectMath(page);

  const c = await graded(request);
  await summary(request, c.lan, c.bc.id);
  await signIn(page, c.lan);
  await page.goto(`/gv/bai-co-cham/${c.bc.id}/loi-cua-lop`);
  await page.getByTestId('s5e5-insight-1').waitFor({ timeout: 30_000 });
  await expectMath(page);
});

test('Bài luyện (học sinh): đề + hình không còn LaTeX thô', async ({ page, request }) => {
  const { assigned, openPlay, sandbox } = await import('../s2e8/s2e8.ts');
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  await openPlay(page, sb, set.id);
  await page.waitForLoadState('networkidle');
  await expectMath(page);
});
