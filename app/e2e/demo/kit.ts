import { execFileSync } from 'node:child_process';
import { createHash } from 'node:crypto';
import { appendFileSync, copyFileSync, existsSync, mkdirSync, writeFileSync } from 'node:fs';
import path from 'node:path';
import { test as base, expect } from '@playwright/test';
import type { Locator, Page } from '@playwright/test';

/**
 * T-261128-73 (DEMO) — tiện ích quay video giới thiệu: màn tiêu đề, phụ đề tiếng Việt, khoanh phần tử trước khi bấm,
 * bước "mềm" (lỗi một bước không làm hỏng cả clip, được ghi vào demo-raw/failures.log), nhật ký mốc phụ đề theo giây.
 * Mã clip = tiêu đề test dạng "NN-ten-kich-ban | Tiêu đề hiển thị".
 */
export const RAW = path.resolve(import.meta.dirname, '../test-results/demo-raw');
mkdirSync(RAW, { recursive: true });
export const AUDIO = path.join(RAW, 'audio');
mkdirSync(AUDIO, { recursive: true });
/** Giọng đọc tiếng Anh offline (macOS `say`); đổi bằng GEO_DEMO_VOICE. */
const VOICE = process.env.GEO_DEMO_VOICE ?? 'Samantha';
const RATE = process.env.GEO_DEMO_RATE ?? '178';
/** Sinh AIFF cho một câu thoại (có bộ đệm theo băm), trả tệp + độ dài giây. */
export function voice(text: string): { file: string; dur: number } {
  const h = createHash('sha1').update(`${VOICE}|${RATE}|${text}`).digest('hex').slice(0, 16);
  const file = path.join(AUDIO, `${h}.aiff`);
  if (!existsSync(file)) execFileSync('say', ['-v', VOICE, '-r', RATE, '-o', file, text]);
  const info = execFileSync('afinfo', [file], { encoding: 'utf8' });
  const dur = Number(/estimated duration: ([\d.]+)/.exec(info)?.[1] ?? '2');
  return { file, dur };
}

type Cue = { t: number; text: string; audio?: string; dur?: number };
interface Demo {
  /** Phụ đề ở đáy màn; giữ `ms` mili giây. */
  say(text: string, ms?: number): Promise<void>;
  /** Màn tiêu đề toàn màn hình (đầu clip/chương). */
  card(title: string, sub?: string, ms?: number, note?: string, vo?: string): Promise<void>;
  /** Bước mềm: lỗi thì ghi lại, clip vẫn chạy tiếp. */
  step(label: string, fn: () => Promise<unknown>): Promise<boolean>;
  /** Khoanh phần tử rồi bấm. */
  tap(loc: Locator, ms?: number): Promise<void>;
  /** Khoanh phần tử (không bấm). */
  mark(loc: Locator, ms?: number): Promise<void>;
  hold(ms?: number): Promise<void>;
  /** Nhãn góc "bản thử" (cờ chặng 5 chỉ bật trong phiên dev local). */
  trial(on: boolean): Promise<void>;
  failures: string[];
}

const OVERLAY = () => {
  const install = () => {
    if (document.getElementById('__demo_cap') || !document.body) return;
    const st = document.createElement('style');
    st.textContent = `#__demo_cap{position:fixed;left:50%;bottom:28px;transform:translateX(-50%);max-width:1500px;z-index:2147483647;
      background:rgba(15,23,42,.86);color:#fff;font:600 30px/1.35 system-ui,-apple-system,"Segoe UI",sans-serif;padding:14px 30px;
      border-radius:14px;box-shadow:0 8px 30px rgba(0,0,0,.35);text-align:center;pointer-events:none;transition:opacity .25s}
      #__demo_cap:empty{opacity:0}
      #__demo_badge{position:fixed;top:12px;right:16px;z-index:2147483647;font:600 16px system-ui,sans-serif;color:#fff;
      background:rgba(15,23,42,.72);padding:6px 12px;border-radius:8px;pointer-events:none}
      #__demo_trial{position:fixed;top:48px;right:16px;z-index:2147483647;font:700 16px system-ui,sans-serif;color:#7c2d12;
      background:#fed7aa;border:2px solid #ea580c;padding:6px 12px;border-radius:8px;pointer-events:none;display:none}
      .__demo_hl{outline:4px solid #f59e0b !important;outline-offset:3px !important;box-shadow:0 0 0 9px rgba(245,158,11,.28) !important;
      border-radius:6px;transition:outline .15s}`;
    document.head.appendChild(st);
    const cap = document.createElement('div');
    cap.id = '__demo_cap';
    cap.textContent = sessionStorage.getItem('__demo_cap') ?? '';
    document.body.appendChild(cap);
    const b = document.createElement('div');
    b.id = '__demo_badge';
    b.textContent = 'GEO demo · sample data · mock AI';
    document.body.appendChild(b);
    const tr = document.createElement('div');
    tr.id = '__demo_trial';
    tr.textContent = 'Stage 5 preview — off in release builds';
    if (sessionStorage.getItem('__demo_trial') === '1') tr.style.display = 'block';
    document.body.appendChild(tr);
  };
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', install);
  else install();
  new MutationObserver(install).observe(document.documentElement, { childList: true, subtree: false });
};

export const test = base.extend<{ demo: Demo }>({
  demo: async ({ page }, use, info) => {
    const started = Date.now();
    const cues: Cue[] = [];
    const failures: string[] = [];
    await page.addInitScript(OVERLAY);
    await page.route(/fonts\.(googleapis|gstatic)\.com/, (r) => r.abort());
    const setCap = async (text: string) => {
      await page
        .evaluate((t) => {
          sessionStorage.setItem('__demo_cap', t);
          const el = document.getElementById('__demo_cap');
          if (el) el.textContent = t;
        }, text)
        .catch(() => {});
    };
    const demo: Demo = {
      failures,
      async say(text, ms = 1500) {
        const v = voice(text);
        await setCap(text);
        cues.push({ t: (Date.now() - started) / 1000, text, audio: v.file, dur: v.dur });
        await page.waitForTimeout(Math.max(ms, v.dur * 1000 + 450));
      },
      async card(title, sub = '', ms = 2800, note = '', vo = '') {
        const v = vo ? voice(vo) : null;
        cues.push({ t: (Date.now() - started) / 1000 + 0.3, text: `[Title] ${title}${sub ? ' — ' + sub : ''}`, audio: v?.file, dur: v?.dur });
        if (v) ms = Math.max(ms, v.dur * 1000 + 900);
        await setCap('');
        await page.goto('about:blank').catch(() => {});
        await page.setContent(`<!doctype html><meta charset="utf-8"><body style="margin:0;height:100vh;display:flex;flex-direction:column;
          align-items:center;justify-content:center;background:linear-gradient(135deg,#0f172a,#1e3a8a);color:#fff;font-family:system-ui,-apple-system,sans-serif">
          <div style="font:600 26px system-ui;letter-spacing:.12em;color:#93c5fd;text-transform:uppercase">Hình Học Không Gian Cùng AI · Geometry Math Support</div>
          <h1 style="font-size:72px;margin:28px 80px 18px;text-align:center">${title}</h1>
          <div style="font-size:34px;color:#cbd5e1;text-align:center;max-width:1500px">${sub}</div>
          ${note ? `<div style="margin-top:36px;font:700 24px system-ui;color:#7c2d12;background:#fed7aa;padding:10px 20px;border-radius:10px">${note}</div>` : ''}
          </body>`);
        await page.waitForTimeout(ms);
      },
      async step(label, fn) {
        try {
          await fn();
          return true;
        } catch (e) {
          const msg = `${info.title} :: ${label} :: ${String(e).split('\n')[0]}`;
          failures.push(msg);
          appendFileSync(path.join(RAW, 'failures.log'), msg + '\n');
          return false;
        }
      },
      async mark(loc, ms = 900) {
        await loc.first().scrollIntoViewIfNeeded().catch(() => {});
        await loc.first().evaluate((el) => el.classList.add('__demo_hl')).catch(() => {});
        await page.waitForTimeout(ms);
        await loc.first().evaluate((el) => el.classList.remove('__demo_hl')).catch(() => {});
      },
      async tap(loc, ms = 700) {
        await demo.mark(loc, ms);
        await loc.first().click();
      },
      async hold(ms = 2000) {
        await page.waitForTimeout(ms);
      },
      async trial(on) {
        await page
          .evaluate((v) => {
            sessionStorage.setItem('__demo_trial', v ? '1' : '0');
            const el = document.getElementById('__demo_trial');
            if (el) el.style.display = v ? 'block' : 'none';
          }, on)
          .catch(() => {});
      },
    };
    await use(demo);
    await setCap('');
    await page.waitForTimeout(600);
    const id = info.title.split('|')[0]!.trim();
    const video = page.video();
    await page.close();
    if (video) {
      const src = await video.path();
      copyFileSync(src, path.join(RAW, `${id}.webm`));
    }
    writeFileSync(path.join(RAW, `${id}.json`), JSON.stringify({ id, title: info.title.split('|')[1]?.trim() ?? id, voice: VOICE, cues, failures }, null, 2));
  },
});

export { expect };
