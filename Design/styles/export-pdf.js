// Xuất Design/styles/GEO-4-huong-style.pdf: trang 1 = index.html (bảng so sánh), sau đó mỗi ảnh png một trang.
// Chạy: PW_CORE=<playwright-core> node Design/styles/export-pdf.js   (mặc định như shoot.js)
const path = require('path'), fs = require('fs');
const PW = process.env.PW_CORE || path.resolve(__dirname, '../../app/e2e/node_modules/playwright-core');
const { chromium } = require(PW);
const NAMES = { a: 'A · Lớp học sáng (Material 3)', b: 'B · Bảng đen tối (IBM Carbon)', c: 'C · Giấy kẻ ô (Apple HIG)', d: 'D · Bảng điều khiển gọn (shadcn / Fluent 2)' };
const SCREENS = { 'hs-lam-bai': 'Học sinh làm bài', 'gv-lop': 'Giáo viên · kết quả lớp', 'them-de': 'Thêm đề · duyệt lời giải', component: 'Bảng component' };
(async () => {
  const dir = __dirname, out = path.join(dir, 'GEO-4-huong-style.pdf');
  const b = await chromium.launch();
  const p = await b.newPage({ viewport: { width: 1440, height: 900 } });
  await p.goto('file://' + path.join(dir, 'index.html'), { waitUntil: 'networkidle' });
  await p.emulateMedia({ media: 'screen' });
  // ảnh trong index dùng loading=lazy → ép tải hết trước khi in
  await p.evaluate(async () => {
    document.querySelectorAll('img').forEach(i => { i.loading = 'eager'; });
    await Promise.all([...document.images].map(i => i.complete && i.naturalWidth ? 0 : new Promise(r => { i.onload = i.onerror = r; const s = i.src; i.src = ''; i.src = s; })));
  });
  const cover = await p.pdf({ format: 'A4', landscape: true, printBackground: true, margin: { top: '10mm', bottom: '10mm', left: '10mm', right: '10mm' } });
  const imgs = fs.readdirSync(path.join(dir, 'png')).filter(f => f.endsWith('.png')).sort();
  const pages = imgs.map(f => {
    const [k, ...rest] = f.replace('.png', '').split('-'); const s = rest.join('-');
    return `<section><h2>${NAMES[k] || k} — ${SCREENS[s] || s}</h2><img src="file://${path.join(dir, 'png', f)}"></section>`;
  }).join('');
  await p.setContent(`<!doctype html><meta charset="utf-8"><style>
    @page{size:A4 landscape;margin:8mm}body{margin:0;font:14px system-ui,sans-serif;color:#111}
    section{page-break-after:always;height:190mm;display:flex;flex-direction:column;align-items:center}
    h2{margin:0 0 4mm;font-size:16px;align-self:flex-start}img{max-width:100%;max-height:178mm;object-fit:contain;border:1px solid #ddd}
  </style>${pages}`, { waitUntil: 'load' });
  const body = await p.pdf({ format: 'A4', landscape: true, printBackground: true });
  await b.close();
  // Ghép hai PDF bằng pdf-lib nếu có, nếu không thì ghi hai tệp.
  let merged = null;
  try {
    const { PDFDocument } = require(process.env.PDF_LIB || 'pdf-lib');
    const doc = await PDFDocument.create();
    for (const buf of [cover, body]) { const src = await PDFDocument.load(buf); (await doc.copyPages(src, src.getPageIndices())).forEach(pg => doc.addPage(pg)); }
    merged = await doc.save();
  } catch (e) { /* không có pdf-lib */ }
  if (merged) { fs.writeFileSync(out, merged); console.log('PDF:', out, imgs.length + ' ảnh'); }
  else { fs.writeFileSync(out.replace('.pdf', '-1-so-sanh.pdf'), cover); fs.writeFileSync(out.replace('.pdf', '-2-anh.pdf'), body); console.log('PDF (2 tệp, thiếu pdf-lib):', out.replace('.pdf', '-*.pdf')); }
})();
