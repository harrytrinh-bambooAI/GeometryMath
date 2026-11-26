// shoot.js · v0.1 · 2026-11-26 · chụp 4 hướng x 4 màn -> Design/styles/png/<hướng>-<màn>.png
// Chạy: node Design/styles/shoot.js [a|b|c|d ...]   (PW_CORE=đường dẫn playwright-core nếu worktree thiếu node_modules)
const path = require('path');
const PW = process.env.PW_CORE || path.resolve(__dirname, '../../app/e2e/node_modules/playwright-core');
const { chromium } = require(PW);
const SIZE = { 'hs-lam-bai': { a: [390, 844], b: [1024, 768], c: [1024, 768], d: [1024, 768] }, 'gv-lop': [1440, 900], 'them-de': [1440, 900], component: [1440, 900] };
(async () => {
  const only = process.argv.slice(2); const b = await chromium.launch();
  for (const s of ['a', 'b', 'c', 'd']) {
    if (only.length && !only.includes(s)) continue;
    for (const m of Object.keys(SIZE)) {
      const [w, h] = Array.isArray(SIZE[m]) ? SIZE[m] : SIZE[m][s];
      const ctx = await b.newContext({ deviceScaleFactor: 1, viewport: { width: w, height: h } }); const p = await ctx.newPage(); const errs = [];
      p.on('pageerror', (e) => errs.push(e.message));
      await p.goto('file://' + path.join(__dirname, s, m + '.html'), { waitUntil: 'networkidle' });
      await p.evaluate(() => document.fonts.ready); await p.waitForTimeout(300);
      const full = m === 'component';
      await p.screenshot({ path: path.join(__dirname, 'png', s + '-' + m + '.png'), fullPage: full, clip: full ? undefined : { x: 0, y: 0, width: w, height: h } });
      const ov = await p.evaluate(() => [...document.querySelectorAll('.app *')].filter((e) => (e.scrollHeight > e.clientHeight + 2 || e.scrollWidth > e.clientWidth + 2) && !['svg', 'path'].includes(e.tagName.toLowerCase()) && getComputedStyle(e).display !== 'inline').map((e) => e.className || e.tagName).slice(0, 6));
      console.log(s, m, w + 'x' + h, 'tràn:', JSON.stringify(ov), errs.join('|')); await ctx.close();
    }
  }
  await b.close();
})();
