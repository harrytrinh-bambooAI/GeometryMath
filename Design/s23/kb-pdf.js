// Xuất Design/s23/GEO-kich-ban-S23.pdf: trang mở đầu "Cách hệ thống vận hành" + mỗi bước kịch bản một trang
// (ảnh màn kèm hộp chú thích, dưới là chữ: Đang làm gì / Vì sao / Phía sau / Chưa vẽ).
// Chạy: PW_CORE=<playwright-core> node Design/s23/kb-pdf.js   (mặc định như kb-shoot.js)
const path = require('path'), fs = require('fs');
const PW = process.env.PW_CORE || path.resolve(__dirname, '../../app/e2e/node_modules/playwright-core');
const { chromium } = require(PW);
const page = 'file://' + path.resolve(__dirname, '../prototype/s23-screens-c.html');
const esc = s => String(s || '').replace(/[&<>"]/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]));
const ROLE = { gv: 'Giáo viên', hs: 'Học sinh', ht: 'Hệ thống', ai: 'AI' };
(async () => {
  const b = await chromium.launch();
  const p = await b.newPage({ viewport: { width: 1440, height: 900 } });
  const errs = []; p.on('pageerror', e => errs.push(e.message));
  await p.goto(page, { waitUntil: 'load' }); await p.waitForTimeout(400);
  const KBS = await p.evaluate(() => window.S23K.KBS.map(k => ({ id: k.id, name: k.name, sum: k.sum, steps: k.steps.map(s => ({ s: s.s, w: s.w, t: s.t, a: s.a, y: s.y, b: s.b, m: s.m })) })));
  const shot = async () => (await p.screenshot({ fullPage: false })).toString('base64');
  const parts = [];
  // trang mở đầu (chụp toàn trang)
  const intro = (await p.screenshot({ fullPage: true })).toString('base64');
  parts.push(`<section class="intro"><h1>Bản mẫu chặng 2–3 · Kịch bản vận hành</h1><p>${KBS.length} kịch bản · ${KBS.reduce((n, k) => n + k.steps.length, 0)} bước · GEO · ${new Date().toLocaleDateString('vi-VN')}</p><img src="data:image/png;base64,${intro}"></section>`);
  // mục lục
  parts.push(`<section class="toc"><h1>Mục lục</h1><ol>${KBS.map(k => `<li><b>${esc(k.name)}</b> — ${k.steps.length} bước<br><span>${esc(k.sum)}</span></li>`).join('')}</ol></section>`);
  for (const k of KBS) {
    parts.push(`<section class="kb"><h1>${esc(k.name)}</h1><p>${esc(k.sum)}</p><ol>${k.steps.map(s => `<li><b>${esc(ROLE[s.w] || s.w)}:</b> ${esc(s.t)}${s.m ? ' <i>(chưa vẽ)</i>' : ''}</li>`).join('')}</ol></section>`);
    for (let i = 1; i <= k.steps.length; i++) {
      await p.goto(page + `#kb=${k.id}&b=${i}`, { waitUntil: 'load' }); await p.waitForTimeout(350);
      const img = await shot(), s = k.steps[i - 1];
      parts.push(`<section class="st"><h2>${esc(k.name)} · Bước ${i}/${k.steps.length} — <span class="r">${esc(ROLE[s.w] || s.w)}</span> ${esc(s.t)}</h2><img src="data:image/png;base64,${img}"><div class="x"><div><b>Đang làm gì:</b> ${esc(s.a)}</div><div><b>Vì sao:</b> ${esc(s.y)}</div><div><b>Phía sau:</b> ${esc(s.b)}</div>${s.m ? `<div class="m"><b>Chưa vẽ:</b> ${esc(s.m)}</div>` : ''}</div></section>`);
    }
  }
  await p.setContent(`<!doctype html><meta charset="utf-8"><style>
    @page{size:A4 landscape;margin:8mm}body{margin:0;font:12px system-ui,-apple-system,sans-serif;color:#111}
    section{page-break-after:always}h1{font-size:20px;margin:0 0 3mm}h2{font-size:13px;margin:0 0 2mm}
    .intro img{max-width:100%;max-height:165mm;display:block;margin:auto;object-fit:contain}
    .toc li,.kb li{margin:0 0 2.5mm;font-size:13px}.toc span{color:#555}.kb p{color:#444;font-size:13px}
    .st img{display:block;max-width:100%;max-height:150mm;margin:0 auto 2mm;border:1px solid #ccc;object-fit:contain}
    .x{display:grid;grid-template-columns:1fr 1fr 1fr;gap:3mm;font-size:11px}.x .m{grid-column:1/-1;color:#9a5b00}
    .r{background:#e8edff;padding:0 4px;border-radius:4px}i{color:#9a5b00}
  </style>${parts.join('')}`, { waitUntil: 'load' });
  const out = path.join(__dirname, 'GEO-kich-ban-S23.pdf');
  fs.writeFileSync(out, await p.pdf({ format: 'A4', landscape: true, printBackground: true }));
  await b.close();
  console.log('PDF:', out, '| bước:', KBS.reduce((n, k) => n + k.steps.length, 0), '| pageerror:', errs.length);
})();
