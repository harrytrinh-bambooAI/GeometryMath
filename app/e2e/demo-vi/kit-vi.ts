import { appendFileSync, copyFileSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import path from 'node:path';
import { test as base, expect } from '@playwright/test';
import type { Locator, Page } from '@playwright/test';

/**
 * T-261128-75 (DEMO-VI) · v1.0 — bộ quay clip CÂM cho video giới thiệu tiếng Việt. Lời đọc + phụ đề ghép sau (make-video-vi.mjs),
 * thời lượng cảnh căn theo tệp giọng. Mã clip = tiêu đề test dạng "NN-ten-canh | mô tả" (trùng mã cảnh trong scenes.json).
 * Bước "mềm": lỗi một bước ghi vào demo-vi-raw/failures.log, clip vẫn quay tiếp.
 */
export const RAW = path.resolve(import.meta.dirname, '../test-results/demo-vi-raw');
mkdirSync(RAW, { recursive: true });
/** Clip của bộ video hướng dẫn (T-77): tệp trong demo-vi/guides/ ghi vào thư mục riêng. */
export const GUIDES_RAW = path.resolve(import.meta.dirname, '../test-results/demo-vi-guides-raw');
mkdirSync(GUIDES_RAW, { recursive: true });
export const EVIDENCE = path.resolve(import.meta.dirname, '../../../artifacts/GEO/evidence/DEMO-VI-20261128');
export const ASSET = (n: string) => path.join(EVIDENCE, 'assets', n);
export const FIXTURE = <T>(n: string): T => JSON.parse(readFileSync(path.join(import.meta.dirname, 'fixtures', n), 'utf8')) as T;

interface Demo {
  step(label: string, fn: () => Promise<unknown>): Promise<boolean>;
  tap(loc: Locator, ms?: number): Promise<void>;
  mark(loc: Locator, ms?: number): Promise<void>;
  hold(ms?: number): Promise<void>;
  drag(testId: string, dx?: number, dy?: number): Promise<void>;
  type(loc: Locator, text: string, delay?: number): Promise<void>;
  /** Mốc bắt đầu một bước (giây kể từ đầu video) — dựng video hướng dẫn cắt clip theo mốc. */
  chapter(code: string): Promise<void>;
  /** Nhãn bước ở góc trái trên (null = ẩn). */
  badge(text: string | null): Promise<void>;
  failures: string[];
}

const OVERLAY = () => {
  const install = () => {
    if (document.getElementById('__demo_badge') || !document.body) return;
    const st = document.createElement('style');
    st.textContent = `#__demo_badge{position:fixed;top:12px;right:16px;z-index:2147483647;font:600 16px system-ui,sans-serif;color:#fff;
      background:rgba(23,58,46,.78);padding:6px 12px;border-radius:8px;pointer-events:none}
      #__demo_step{position:fixed;top:14px;left:50%;transform:translateX(-50%);z-index:2147483647;font:800 26px system-ui,sans-serif;color:#173a2e;
      background:#c5d82f;padding:10px 26px;border-radius:14px;box-shadow:0 8px 24px rgba(0,0,0,.25);pointer-events:none}
      #__demo_step:empty{display:none}
      .__demo_hl{outline:4px solid #c5d82f !important;outline-offset:3px !important;box-shadow:0 0 0 9px rgba(197,216,47,.30) !important;
      border-radius:6px;transition:outline .15s}`;
    document.head.appendChild(st);
    const b = document.createElement('div');
    b.id = '__demo_badge';
    b.textContent = 'Bản demo · dữ liệu mẫu · AI giả lập';
    document.body.appendChild(b);
    const s = document.createElement('div');
    s.id = '__demo_step';
    s.textContent = sessionStorage.getItem('__demo_step') ?? '';
    document.body.appendChild(s);
  };
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', install);
  else install();
  new MutationObserver(install).observe(document.documentElement, { childList: true, subtree: false });
};

export const test = base.extend<{ demo: Demo }>({
  demo: async ({ page }, use, info) => {
    const failures: string[] = [];
    const started = Date.now();
    const marks: Array<{ code: string; t: number }> = [];
    const raw = info.file.includes(`${path.sep}guides${path.sep}`) ? GUIDES_RAW : RAW;
    await page.addInitScript(OVERLAY);
    await page.route(/fonts\.(googleapis|gstatic)\.com/, (r) => r.abort());
    const demo: Demo = {
      failures,
      async step(label, fn) {
        try {
          await fn();
          return true;
        } catch (e) {
          const msg = `${info.title} :: ${label} :: ${String(e).split('\n')[0]}`;
          failures.push(msg);
          appendFileSync(path.join(raw, 'failures.log'), msg + '\n');
          return false;
        }
      },
      async mark(loc, ms = 1000) {
        // phần tử không có trên màn: bỏ qua ngay (không chờ actionTimeout 15 s)
        if (!(await loc.first().isVisible().catch(() => false))) {
          await page.waitForTimeout(Math.min(ms, 600));
          return;
        }
        await loc.first().scrollIntoViewIfNeeded().catch(() => {});
        await loc.first().evaluate((el) => el.classList.add('__demo_hl')).catch(() => {});
        await page.waitForTimeout(ms);
        await loc.first().evaluate((el) => el.classList.remove('__demo_hl')).catch(() => {});
      },
      async tap(loc, ms = 800) {
        await demo.mark(loc, ms);
        await loc.first().click();
      },
      async hold(ms = 2000) {
        await page.waitForTimeout(ms);
      },
      async drag(testId, dx = 200, dy = 30) {
        const b = await page.getByTestId(testId).first().boundingBox();
        if (!b) return;
        const cx = b.x + b.width / 2;
        const cy = b.y + b.height / 2;
        await page.mouse.move(cx, cy);
        await page.mouse.down();
        await page.mouse.move(cx + dx, cy + dy, { steps: 40 });
        await page.mouse.up();
      },
      async chapter(code) {
        marks.push({ code, t: (Date.now() - started) / 1000 });
      },
      async badge(text) {
        await page
          .evaluate((t) => {
            sessionStorage.setItem('__demo_step', t ?? '');
            const el = document.getElementById('__demo_step');
            if (el) el.textContent = t ?? '';
          }, text)
          .catch(() => {});
      },
      async type(loc, text, delay = 35) {
        await loc.first().click().catch(() => {});
        await loc.first().pressSequentially(text, { delay });
      },
    };
    await use(demo);
    await page.waitForTimeout(500);
    const id = info.title.split('|')[0]!.trim();
    const video = page.video();
    await page.close();
    const end = (Date.now() - started) / 1000;
    if (video) copyFileSync(await video.path(), path.join(raw, `${id}.webm`));
    writeFileSync(path.join(raw, `${id}.json`), JSON.stringify({ id, title: info.title.split('|')[1]?.trim() ?? id, marks, end, failures }, null, 2));
  },
});

/** Ảnh trang bài làm do API trả về (ảnh nộp) → thay bằng ảnh viết tay minh hoạ (chỉ hiển thị trong clip). */
export async function handwrittenImages(page: Page) {
  const p1 = readFileSync(ASSET('bai-lam-bn01-p1-photo.jpg'));
  const p2 = readFileSync(ASSET('bai-lam-bn01-p2-photo.jpg'));
  let n = 0;
  await page.route(
    (u) => u.pathname.startsWith('/api/') && /(page|image|file|photo|anh)/i.test(u.pathname),
    async (route) => {
      if (route.request().resourceType() !== 'image' && !/image/.test(route.request().headers()['accept'] ?? '')) return route.fallback();
      const body = n++ % 2 === 0 ? p1 : p2;
      await route.fulfill({ status: 200, contentType: 'image/jpeg', body });
    },
  );
}

export { expect };
