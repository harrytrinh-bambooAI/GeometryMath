#!/usr/bin/env node
// T-261128-75 (DEMO-VI) · v1.0 · 2026-11-28
// Dựng bằng HTML → PNG (Chromium của Playwright, không cài thêm gì):
//   1) ảnh bài làm viết tay MINH HOẠ (giấy kẻ ô, chữ viết tay font Noteworthy, hình chóp vẽ tay, tên HS giả)
//      → evidence/assets/bai-lam-hkg01-p{1,2}.png (bản quét) + -photo.jpg (như chụp điện thoại). Owner có thể thay ảnh chụp thật CÙNG TÊN.
//   2) slide 1920x1080 style BambooAI (nền xanh lá đậm gradient, nhãn "PRESENTED BY BAMBOOAI" vàng chanh, thẻ kính có icon,
//      slide chương "0N <TÊN>") → evidence/slides/<mã>.png
//   cd app/e2e && node demo-vi/make-assets-vi.mjs [ffmpeg]   (ffmpeg để xuất JPEG; thiếu thì chỉ có PNG)
import { execFileSync } from 'node:child_process';
import { existsSync, mkdirSync, readFileSync } from 'node:fs';
import path from 'node:path';
import { chromium } from '@playwright/test';
import { EVIDENCE } from './lines.mjs';

const FF = process.argv[2];
const SLIDES = path.join(EVIDENCE, 'slides');
const ASSETS = path.join(EVIDENCE, 'assets');
mkdirSync(SLIDES, { recursive: true });
mkdirSync(ASSETS, { recursive: true });

// ------------------------------------------------------------------ icon (nét kiểu lucide, tự vẽ)
const ICON = {
  cube: '<path d="M12 2 3 7v10l9 5 9-5V7z"/><path d="M3 7l9 5 9-5M12 12v10"/>',
  shield: '<path d="M12 2 4 5v6c0 5 3.5 9 8 11 4.5-2 8-6 8-11V5z"/><path d="m8.5 12 2.5 2.5 4.5-5"/>',
  spark: '<path d="M12 3v4M12 17v4M3 12h4M17 12h4M6 6l2.5 2.5M15.5 15.5 18 18M6 18l2.5-2.5M15.5 8.5 18 6"/>',
  bulb: '<path d="M9 18h6M10 21h4"/><path d="M12 3a6 6 0 0 0-4 10.5c.8.8 1 1.5 1 2.5h6c0-1 .2-1.7 1-2.5A6 6 0 0 0 12 3z"/>',
  camera: '<path d="M4 7h3l2-3h6l2 3h3v13H4z"/><circle cx="12" cy="13" r="4"/>',
  chat: '<path d="M4 4h16v12H8l-4 4z"/>',
  lock: '<rect x="5" y="11" width="14" height="10" rx="2"/><path d="M8 11V7a4 4 0 0 1 8 0v4"/>',
  users: '<circle cx="9" cy="8" r="4"/><path d="M2 21c0-4 3-6 7-6s7 2 7 6"/><path d="M16 4a4 4 0 0 1 0 8M22 21c0-3-2-5-5-5.5"/>',
  book: '<path d="M4 4h7a3 3 0 0 1 3 3v14a2 2 0 0 0-2-2H4z"/><path d="M20 4h-6M20 4v15h-6"/>',
  pen: '<path d="M4 20h4L19 9l-4-4L4 16z"/><path d="m13 7 4 4"/>',
  check: '<path d="M4 12l5 5L20 6"/>',
  clock: '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 3"/>',
  chart: '<path d="M4 20V4M4 20h16"/><path d="M8 16v-5M12 16V8M16 16v-3"/>',
  grad: '<path d="M2 9l10-5 10 5-10 5z"/><path d="M6 11v5c3 2 9 2 12 0v-5"/>',
  file: '<path d="M6 2h8l5 5v15H6z"/><path d="M14 2v5h5"/>',
  eye: '<path d="M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12z"/><circle cx="12" cy="12" r="3"/>',
  ruler: '<path d="M3 17 17 3l4 4L7 21z"/><path d="m7 13 2 2M10 10l2 2M13 7l2 2"/>',
  scan: '<path d="M3 7V3h4M17 3h4v4M21 17v4h-4M7 21H3v-4"/><path d="M7 9h10M7 13h10M7 17h6"/>',
  alert: '<path d="M12 3 2 21h20z"/><path d="M12 10v5M12 18h.01"/>',
  mail: '<rect x="3" y="5" width="18" height="14" rx="2"/><path d="m3 7 9 6 9-6"/>',
};
const icon = (k, size = 30) =>
  `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">${ICON[k]}</svg>`;

// ------------------------------------------------------------------ khung slide
const CSS = `
*{box-sizing:border-box;margin:0;padding:0}
html,body{width:1920px;height:1080px;overflow:hidden}
body{font-family:-apple-system,"SF Pro Display","Helvetica Neue",Arial,sans-serif;color:#fff;
  background:radial-gradient(ellipse 75% 70% at 50% 32%,#2c6a55 0%,#235645 42%,#1a4034 78%,#163529 100%);
  display:flex;flex-direction:column;align-items:center;-webkit-font-smoothing:antialiased}
.lab{font-weight:800;letter-spacing:.22em;color:#c5d82f;font-size:30px;text-transform:uppercase}
h1{font-weight:800;font-size:68px;line-height:1.12;text-align:center;letter-spacing:-.01em}
.sub{font-weight:600;font-size:34px;line-height:1.5;text-align:center;color:#e9f1ea}
.glass{background:linear-gradient(160deg,rgba(255,255,255,.10),rgba(255,255,255,.04));border:2px solid rgba(190,220,150,.30);
  border-radius:30px;box-shadow:0 20px 50px rgba(0,0,0,.18) inset,0 10px 40px rgba(0,0,0,.12)}
.ic{width:62px;height:62px;border-radius:16px;background:rgba(197,216,47,.16);color:#c5d82f;display:flex;align-items:center;justify-content:center;flex:none}
.ch{font-weight:800;letter-spacing:.16em;color:#c5d82f;font-size:26px;text-transform:uppercase}
.bar{background:#c5d82f;color:#173a2e;font-weight:800;border-radius:18px;font-size:30px;padding:18px 40px;text-align:center}
.foot{position:absolute;bottom:34px;left:0;right:0;text-align:center;color:rgba(233,241,234,.72);font-weight:600;font-size:24px}
.tag{display:inline-block;border:2px solid #c5d82f;color:#c5d82f;border-radius:999px;font-weight:800;font-size:20px;letter-spacing:.08em;padding:2px 14px;vertical-align:middle}
b{font-weight:800}
`;
const page = (body) => `<!doctype html><html lang="vi"><meta charset="utf-8"><style>${CSS}</style><body>${body}</body></html>`;

const cards = (items, { cols = items.length, w = 1720, minH = 380 } = {}) => `
  <div style="display:grid;grid-template-columns:repeat(${cols},1fr);gap:34px;width:${w}px">
  ${items
    .map(
      (c) => `<div class="glass" style="padding:40px 40px 44px;min-height:${minH}px;display:flex;flex-direction:column;gap:22px">
      <div style="display:flex;align-items:center;gap:22px"><div class="ic">${icon(c.icon)}</div><div style="font-size:40px;font-weight:800;line-height:1.15">${c.title}</div></div>
      ${c.lead ? `<div style="font-size:28px;font-weight:700;color:#c5d82f;line-height:1.35">${c.lead}</div>` : ''}
      <ul style="list-style:none;display:flex;flex-direction:column;gap:16px">${c.items
        .map((t) => `<li style="font-size:29px;line-height:1.38;font-weight:600;color:#eef5ef;display:flex;gap:14px"><span style="color:#c5d82f;flex:none">•</span><span>${t}</span></li>`)
        .join('')}</ul></div>`,
    )
    .join('')}
  </div>`;

const chapter = (n, title, sub) =>
  page(`<div style="flex:1;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:10px;padding:0 160px">
  <div class="lab">Presented by BambooAI</div>
  <div style="font-weight:900;font-size:230px;line-height:1.05;color:#c5d82f;letter-spacing:-.02em;margin-top:18px">${n}</div>
  <div style="font-weight:800;font-size:104px;line-height:1.1;text-transform:uppercase;text-align:center;color:#f2f2f2">${title}</div>
  <div class="sub" style="margin-top:22px;max-width:1500px">${sub}</div></div>`);

// ------------------------------------------------------------------ bài làm viết tay (minh hoạ)
const INK = '#1d2f7a';
const jitter = (seed) => {
  let s = seed;
  return () => ((s = (s * 9301 + 49297) % 233280) / 233280) * 2 - 1;
};
function handLine(x1, y1, x2, y2, r, dashed = false) {
  // đường hơi run tay: 4 điểm giữa lệch ±1.6 px
  const pts = [[x1, y1]];
  for (let i = 1; i < 5; i++) pts.push([x1 + ((x2 - x1) * i) / 5 + r() * 1.6, y1 + ((y2 - y1) * i) / 5 + r() * 1.6]);
  pts.push([x2 + r(), y2 + r()]);
  return `<path d="M${pts.map((p) => p.map((v) => v.toFixed(1)).join(' ')).join(' L')}" fill="none" stroke="${INK}" stroke-width="2.6" stroke-linecap="round" ${dashed ? 'stroke-dasharray="11 9"' : ''}/>`;
}
function pyramid() {
  const r = jitter(7);
  const P = { S: [120, 40], A: [120, 330], B: [380, 330], C: [510, 230], D: [250, 230] };
  const seg = (a, b, d) => handLine(...P[a], ...P[b], r, d);
  const lab = (k, dx, dy) => `<text x="${P[k][0] + dx}" y="${P[k][1] + dy}" font-family="Noteworthy" font-size="34" fill="${INK}">${k}</text>`;
  return `<svg width="580" height="390" viewBox="0 0 580 390">
    ${seg('S', 'A')}${seg('S', 'B')}${seg('S', 'C')}${seg('A', 'B')}${seg('B', 'C')}
    ${seg('S', 'D', true)}${seg('A', 'D', true)}${seg('D', 'C', true)}
    <path d="M120 306 L140 300 L140 324" fill="none" stroke="${INK}" stroke-width="2.2"/>
    <path d="M222 214 Q214 226 222 238" fill="none" stroke="#b3261e" stroke-width="2.4"/>
    ${lab('S', -10, -10)}${lab('A', -34, 18)}${lab('B', 0, 36)}${lab('C', 12, 8)}${lab('D', -6, -14)}
    <text x="62" y="190" font-family="Noteworthy" font-size="28" fill="${INK}">a</text>
    <text x="240" y="364" font-family="Noteworthy" font-size="28" fill="${INK}">a</text>
  </svg>`;
}
function handPage(lines, extra = '') {
  const r = jitter(lines.length * 13 + 5);
  const LH = 62;
  const TOP = 190;
  const rows = lines
    .map((l, i) => {
      if (!l) return '';
      const o = typeof l === 'string' ? { t: l } : l;
      const y = TOP + (o.row ?? i) * LH - 50;
      const rot = (r() * 0.7).toFixed(2);
      const dx = (o.x ?? 168) + r() * 6;
      const st = o.strike ? 'text-decoration:line-through;text-decoration-thickness:3px;text-decoration-color:#1d2f7a;opacity:.85' : '';
      return `<div style="position:absolute;left:${dx.toFixed(1)}px;top:${y}px;transform:rotate(${rot}deg);font-family:Noteworthy;font-size:${o.size ?? 40}px;color:${o.color ?? INK};white-space:nowrap;${st}">${o.t}</div>`;
    })
    .join('');
  return `<!doctype html><html lang="vi"><meta charset="utf-8"><style>*{margin:0}html,body{width:1240px;height:1754px}
  body{background:#fbfaf3;background-image:radial-gradient(ellipse at 30% 20%,rgba(255,255,255,.7),transparent 60%),
  repeating-linear-gradient(to bottom,transparent 0,transparent ${LH - 2}px,#a9c4e6 ${LH - 2}px,#a9c4e6 ${LH}px);background-position:0 ${TOP - LH + 12}px;position:relative;overflow:hidden}
  .m{position:absolute;top:0;bottom:0;left:132px;width:2px;background:#e59a9a}
  .holes{position:absolute;left:40px;top:0;bottom:0;width:26px;background:radial-gradient(circle at 13px 13px,#d9d6cc 11px,transparent 12px) 0 120px/26px 210px repeat-y}
  </style><body><div class="m"></div><div class="holes"></div>${rows}${extra}</body></html>`;
}
const PAGE1 = handPage(
  [
    { t: 'Họ và tên: Nguyễn Minh Khang', row: 0 },
    { t: 'Lớp: 11A1', row: 0, x: 860 },
    { t: 'Bài HKG-01. Tính góc giữa SD và mặt đáy (ABCD).', row: 1 },
    { t: 'Giải:', row: 9 },
    { t: 'Vì SA ⊥ (ABCD) nên A là hình chiếu của S', row: 10 },
    { t: 'lên mặt phẳng (ABCD).', row: 11, x: 200 },
    { t: '⇒ Hình chiếu của SD lên (ABCD) là AD.', row: 12 },
    { t: '⇒ Góc giữa SD và (ABCD) là góc SDA.', row: 13 },
    { t: '(xem tiếp trang 2)', row: 22, x: 820, size: 32 },
  ],
  `<div style="position:absolute;left:300px;top:268px;transform:rotate(-0.8deg)">${pyramid()}</div>
   <div style="position:absolute;right:70px;top:120px;font-family:Noteworthy;font-size:30px;color:${INK}">Trang 1</div>`,
);
const PAGE2 = handPage(
  [
    { t: 'Trang 2', row: -1, x: 1040, size: 30 },
    { t: 'Xét tam giác SAD vuông tại A có:', row: 0 },
    { t: 'SA = a ; AD = a (cạnh hình vuông ABCD)', row: 1, x: 200 },
    { t: 'sin SDA = SA / SD', row: 2, strike: true },
    { t: 'tan SDA = SA / AD = a / a = 1', row: 3 },
    { t: '⇒ góc SDA = 45°', row: 4 },
    { t: 'Vậy góc giữa SD và mặt đáy bằng 45°.', row: 6 },
  ],
  `<div style="position:absolute;left:760px;top:520px;width:330px;height:92px;border:3px solid ${INK};border-radius:46% 54% 48% 52%;transform:rotate(-3deg)"></div>`,
);

async function shot(browser, html, file, w, h) {
  const p = await browser.newPage({ viewport: { width: w, height: h } });
  await p.setContent(html, { waitUntil: 'load' });
  await p.evaluate(() => document.fonts.ready);
  await p.screenshot({ path: file });
  await p.close();
}
const b64 = (f) => `data:image/png;base64,${readFileSync(f).toString('base64')}`;

// ảnh "chụp điện thoại": trang giấy hơi nghiêng trên mặt bàn, ánh sáng không đều, bóng đổ
const photo = (png) => `<!doctype html><meta charset="utf-8"><style>*{margin:0}html,body{width:1500px;height:2000px;overflow:hidden}
  body{background:radial-gradient(ellipse at 40% 35%,#8a7a66,#5d5143 70%,#463c31)}
  img{position:absolute;left:120px;top:110px;width:1240px;transform:rotate(-2.2deg) perspective(2400px) rotateX(3deg);box-shadow:0 30px 60px rgba(0,0,0,.45)}
  .light{position:absolute;inset:0;background:radial-gradient(ellipse at 30% 20%,rgba(255,250,235,.22),transparent 55%),linear-gradient(180deg,transparent 60%,rgba(0,0,0,.18))}
  </style><body><img src="${b64(png)}"><div class="light"></div></body>`;

// ------------------------------------------------------------------ các slide
function slides(p1photo) {
  return {
    '00-mo-dau': page(`<div style="flex:1;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:30px;padding:0 140px">
      <div class="lab" style="font-size:34px">Presented by BambooAI</div>
      <h1 style="font-size:124px">Hình Học Không Gian<br>Cùng AI</h1>
      <div class="sub" style="max-width:1450px">Hình 3D, lời giải từng bước và đáp số kiểm chứng cho học sinh ·<br>soạn đề, soạn giáo án, chấm bài cùng AI cho giáo viên</div>
      <div style="margin-top:34px;font-size:40px;font-weight:700;font-style:italic;color:#c5d82f">“AI is powerful. But thinking is still human.”</div></div>
      <div class="foot">Bản giới thiệu · dữ liệu bài giảng và bài tập thật của tổ Toán · AI giả lập trong video</div>`),
    '01-van-de': page(`<div style="margin-top:90px" class="lab">Presented by BambooAI</div>
      <h1 style="margin:22px 0 60px">Vì sao cần Hình Học Không Gian Cùng AI?</h1>
      ${cards([
        { icon: 'eye', title: 'Học sinh: khó hình dung', items: ['Hình không gian vẽ trên giấy khó thấy quan hệ đường, mặt', 'Bế tắc ở bước tìm hình chiếu, dựng góc', 'Ít cơ hội tự thử và kiểm tra lại'] },
        { icon: 'alert', title: 'AI chung chung: dễ sai', items: ['Có thể tính sai mà vẫn tự tin', 'Đưa đáp số quá sớm, học sinh không tự nghĩ', 'Không bám giáo trình của lớp'] },
        { icon: 'clock', title: 'Giáo viên: quá tải', items: ['Soạn đề, ma trận, nhiều mã đề mất nhiều giờ', 'Soạn giáo án cho từng tiết, từng đối tượng', 'Chấm bài viết tay, tổng hợp lỗi của lớp'] },
      ], { minH: 520 })}`),
    '02-he-thong': page(`<div style="margin-top:80px" class="lab">Presented by BambooAI</div>
      <h1 style="margin:22px 0 54px">Một hệ thống, ba vai trò</h1>
      ${cards([
        { icon: 'grad', title: 'Học sinh', lead: 'Nhìn thấy, tự tay thử, hiểu vì sao', items: ['Hình 3D xoay, vẽ thêm, đo trên hình', 'Gợi ý từng bậc, lời giải mở theo giờ', 'Hỏi AI, câu trả lời có nguồn'] },
        { icon: 'book', title: 'Giáo viên', lead: 'Đỡ công soạn, vẫn quyết định cuối', items: ['Soạn đề bằng yêu cầu, ra đề theo ma trận', 'Soạn giáo án theo PPCT', 'Rubric, chấm bài, theo dõi lớp'] },
        { icon: 'shield', title: 'AI + máy kiểm chứng', lead: 'AI đề xuất, máy tính lại', items: ['Dựng hình từ đề, giải từng bước', 'Engine hình học tính lại mọi đáp số', 'Đọc bài viết tay, đề xuất điểm'] },
      ], { minH: 500 })}
      <div class="bar" style="margin-top:44px;width:1720px">AI đề xuất · giáo viên quyết định — “AI executes, humans decide”</div>`),
    '03-chuong-1': chapter('01', 'Hỗ trợ hình học', 'Gõ đề → dựng hình 3D → xoay, vẽ thêm, đo → lời giải từng bước → đáp số kiểm chứng bằng tính toán.'),
    '08-chuong-2': chapter('02', 'AI đồng hành cùng học sinh', 'Gợi ý trước, lời giải sau → kho tri thức của lớp → hỏi đáp có nguồn, không làm hộ bài.'),
    '12-chuong-3': chapter('03', 'Soạn đề và giáo án', 'Giáo viên ra yêu cầu → AI soạn nháp → máy kiểm chứng đáp số → giáo viên duyệt. Ra đề theo ma trận, soạn giáo án theo PPCT.'),
    '16-chuong-4': chapter('04', 'Chấm điểm & nhận dạng bài làm', 'Rubric → học sinh chụp bài viết tay → đọc chữ, đối chiếu tiêu chí → AI đề xuất điểm → giáo viên nhận từng tiêu chí.'),
    '19-nhan-dang': page(`<div style="margin-top:56px" class="lab">Presented by BambooAI</div>
      <h1 style="margin:16px 0 36px;font-size:60px">Nhận dạng bài làm viết tay của học sinh</h1>
      <div style="display:flex;gap:36px;width:1760px">
        <div class="glass" style="padding:22px;width:610px;flex:none"><img src="${p1photo}" style="width:100%;height:700px;object-fit:cover;object-position:50% 30%;border-radius:18px"></div>
        <div style="flex:1;display:flex;flex-direction:column;gap:26px">
          <div class="glass" style="padding:30px 36px">
            <div class="ch" style="display:flex;align-items:center;gap:14px"><span class="ic" style="width:48px;height:48px">${icon('scan', 26)}</span>Chữ đọc được · độ tin cậy</div>
            <div style="margin-top:18px;display:flex;flex-direction:column;gap:12px;font-size:27px;font-weight:600">
              ${[
                ['Vì SA ⊥ (ABCD) nên A là hình chiếu của S', '0,96', 0],
                ['⇒ Hình chiếu của SD lên (ABCD) là AD', '0,94', 0],
                ['⇒ Góc giữa SD và (ABCD) là góc SDA', '0,93', 0],
                ['tan SDA = SA / AD = a / a = 1', '0,91', 0],
                ['sin SDA = SA / SD  (dòng bị gạch)', '0,58', 1],
                ['⇒ góc SDA = 45°', '0,95', 0],
              ]
                .map(([t, c, low]) => `<div style="display:flex;justify-content:space-between;gap:20px;padding:8px 14px;border-radius:12px;${low ? 'background:rgba(245,170,60,.18);border:2px dashed #f0b44c' : ''}"><span>${t}</span><span style="color:${low ? '#f5c46a' : '#c5d82f'};font-weight:800">${c}${low ? ' · GV xem lại' : ''}</span></div>`)
                .join('')}
            </div></div>
          <div class="glass" style="padding:30px 36px">
            <div class="ch" style="display:flex;align-items:center;gap:14px"><span class="ic" style="width:48px;height:48px">${icon('check', 26)}</span>Đối chiếu rubric (10 điểm)</div>
            <div style="margin-top:16px;display:grid;grid-template-columns:1fr 1fr;gap:10px 30px;font-size:26px;font-weight:600">
              ${[['Hướng giải: hình chiếu → góc SDA', '2/2'], ['Các bước lập luận', '4/4'], ['Kết quả 45°', '2/2'], ['Hình vẽ', '1/1'], ['Trình bày', '1/1'], ['Tổng AI đề xuất', '10/10']]
                .map(([t, s]) => `<div style="display:flex;justify-content:space-between"><span>${t}</span><b style="color:#c5d82f">${s}</b></div>`)
                .join('')}
            </div></div>
        </div></div>
      <div class="foot">Minh hoạ · ảnh bài làm tạo bằng chữ viết tay trên giấy kẻ, tên học sinh giả · AI giả lập · điểm cuối do giáo viên duyệt</div>`),
    '23-loi-ich-hs': page(`<div style="margin-top:90px" class="lab">Presented by BambooAI</div>
      <h1 style="margin:22px 0 60px">Lợi ích cho học sinh</h1>
      ${cards([
        { icon: 'cube', title: 'Nhìn thấy hình', items: ['Hình 3D dựng đúng từ đề, xoay mọi phía', 'Vẽ thêm điểm, đoạn; đo độ dài, góc', 'Đáp số được khoá để tự suy luận'] },
        { icon: 'bulb', title: 'Hiểu vì sao', items: ['Gợi ý 1 → gợi ý 2 → lời giải', 'Mỗi bước có câu “vì sao nghĩ ra”', 'Lời giải mở đúng giờ giáo viên đặt'] },
        { icon: 'chat', title: 'Hỏi được, có nguồn', items: ['Hỏi AI theo tài liệu của lớp', 'Bấm nguồn để đọc lại đúng đoạn', 'Không bị làm hộ bài'] },
      ], { minH: 520 })}`),
    '24-loi-ich-gv': page(`<div style="margin-top:90px" class="lab">Presented by BambooAI</div>
      <h1 style="margin:22px 0 60px">Lợi ích cho giáo viên</h1>
      ${cards([
        { icon: 'pen', title: 'Soạn nhanh', items: ['Soạn đề bằng yêu cầu, từ tài liệu', 'Ra đề theo ma trận, nhiều mã đề', 'Giáo án theo PPCT, sửa trên nháp'] },
        { icon: 'shield', title: 'Tin được', items: ['Đáp số được engine tính lại', 'Nhãn “máy kiểm chứng” và “người duyệt”', 'AI soạn nháp, không tự lưu'] },
        { icon: 'chart', title: 'Chấm và hiểu lớp', items: ['Rubric, AI đề xuất điểm từng tiêu chí', 'Đọc bài viết tay, đánh dấu chỗ chưa chắc', 'Tổng hợp lỗi sai chung, không xếp hạng'] },
        { icon: 'check', title: 'Quyết định cuối', items: ['Nhận, sửa hoặc bỏ từng đề xuất', 'Bật/tắt AI theo từng lớp', 'Theo dõi hạn mức sử dụng'] },
      ], { cols: 4, w: 1780, minH: 520 })}`),
    '25-tong-ket': page(`<div style="margin-top:70px" class="lab">Presented by BambooAI</div>
      <h1 style="margin:18px 0 44px">Tổng kết: cách vận hành</h1>
      <div style="display:grid;grid-template-columns:repeat(6,1fr);gap:24px;width:1780px">
      ${[
        ['Gõ đề', 'AI dựng hình 3D, giải từng bước'],
        ['Kiểm chứng', 'Engine tính lại đáp số'],
        ['Soạn & duyệt', 'Đề, ma trận, giáo án do AI soạn nháp'],
        ['Giao bài', 'Đặt giờ mở lời giải'],
        ['Học sinh làm', 'Xoay hình, gợi ý, hỏi AI có nguồn'],
        ['Chấm & theo dõi', 'Rubric, bài viết tay, lỗi của lớp'],
      ]
        .map(([t, d], i) => `<div class="glass" style="padding:30px 26px;min-height:300px"><div style="width:56px;height:56px;border-radius:14px;background:#c5d82f;color:#173a2e;font-weight:900;font-size:32px;display:flex;align-items:center;justify-content:center">${i + 1}</div><div style="margin-top:22px;font-size:34px;font-weight:800">${t}</div><div style="margin-top:12px;font-size:26px;font-weight:600;color:#e3eee4;line-height:1.4">${d}</div></div>`)
        .join('')}
      </div>
      <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:24px;width:1780px;margin-top:28px">
      ${[
        ['Gợi ý trước, đáp số sau', 'Học sinh tự nghĩ trước khi xem lời giải'],
        ['AI đề xuất, giáo viên quyết định', 'Không đề xuất nào được lưu khi chưa duyệt'],
        ['Đúng trước, đẹp sau', 'Hình và đáp số luôn được kiểm chứng'],
      ]
        .map(([t, d]) => `<div class="glass" style="padding:26px 30px"><div style="font-size:31px;font-weight:800;color:#c5d82f">${t}</div><div style="margin-top:8px;font-size:26px;font-weight:600;color:#e3eee4">${d}</div></div>`)
        .join('')}
      </div>
      <div class="bar" style="margin-top:30px;width:1780px">AI executes, humans decide</div>`),
    '26-ket': page(`<div style="flex:1;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:28px">
      <div class="lab" style="font-size:34px">Presented by BambooAI</div>
      <h1 style="font-size:96px">AI is powerful.<br><span style="color:#c5d82f">But thinking is still human.</span></h1>
      <div class="sub">AI rất mạnh, nhưng tư duy vẫn là của con người.</div>
      <div class="glass" style="margin-top:30px;padding:26px 48px;display:flex;align-items:center;gap:22px;font-size:42px;font-weight:800">
        <span class="ic">${icon('mail')}</span>contact@bamboox.ai</div>
      <div style="font-size:30px;font-weight:700;color:#e9f1ea;margin-top:6px">Hình Học Không Gian Cùng AI · BambooAI</div></div>`),
  };
}

const browser = await chromium.launch();
try {
  // GEO_VI_KEEP_PHOTOS=1: giữ ảnh bài làm hiện có (vd. owner đã chép đè ảnh chụp thật), chỉ dựng lại slide
  const pages = process.env.GEO_VI_KEEP_PHOTOS === '1' ? {} : { 'bai-lam-hkg01-p1': PAGE1, 'bai-lam-hkg01-p2': PAGE2 };
  for (const [name, html] of Object.entries(pages)) {
    const png = path.join(ASSETS, `${name}.png`);
    await shot(browser, html, png, 1240, 1754);
    const photoPng = path.join(ASSETS, `${name}-photo.png`);
    await shot(browser, photo(png), photoPng, 1500, 2000);
    if (FF && existsSync(FF)) {
      execFileSync(FF, ['-y', '-v', 'error', '-i', photoPng, '-q:v', '3', path.join(ASSETS, `${name}-photo.jpg`)]);
    }
    console.log(`ảnh ${name}`);
  }
  const p1 = b64(path.join(ASSETS, 'bai-lam-hkg01-p1-photo.png'));
  for (const [name, html] of Object.entries(slides(p1))) {
    await shot(browser, html, path.join(SLIDES, `${name}.png`), 1920, 1080);
    console.log(`slide ${name}`);
  }
} finally {
  await browser.close();
}
