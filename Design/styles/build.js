// build.js · Phiên bản: v0.1 · 2026-11-26 · sinh HTML tĩnh 4 hướng: node Design/styles/build.js
const fs = require('fs'), path = require('path'), L = require('./lib');
const H = (s, t, nav = 4) => L.head(s, t, 'web') + `<div class="app">${L.nav(s, L.NAV_GV, nav)}
<header class="bar"><div><h1>Kết quả lớp: Bộ luyện góc</h1><p class="crumb">Theo dõi › 11A1 › Cả lớp vướng ở đâu</p></div><span class="chip info">${L.ic(s, 'spark')}Concept · chưa có trong MVP</span><span class="ava">GV</span></header>`;
const bad = (v) => (v < 60 ? 'bad' : v < 80 ? 'mid' : 'good');
const Q = L.Q, E = L.ERR, S = L.HSL;

// ---------- A: bento thẻ, pill ----------
const gvA = () => H('a', 'GV kết quả lớp') + `<main class="main bento">
<section class="card k k1"><span class="ico">${L.ic('a', 'users')}</span><b>31<small>/36</small></b><p>em đã nộp bài</p></section>
<section class="card k k2"><span class="ico">${L.ic('a', 'check')}</span><b>70%</b><p>trung bình làm đúng</p></section>
<section class="card k k3 hot"><span class="ico">${L.ic('a', 'warn')}</span><b>Câu 3</b><p>nhiều em vướng nhất · chỉ 42% đúng</p></section>
<section class="card q"><div class="row sp"><h2>Từng câu · tỉ lệ đúng</h2><div class="tabs"><a class="on">Cả lớp</a><a>Từng em</a><a>Lỗi hay gặp</a></div></div>
${Q.map((q) => `<div class="qrow ${bad(q[2])}"><b class="num">${q[0].slice(4)}</b><div class="qt"><strong>${q[0]} · ${q[1]}</strong><small>trung bình ${String(q[3]).replace('.', ',')} lượt/em</small></div><div class="prog bar1"><i style="width:${q[2]}%"></i></div><span class="chip ${q[2] < 60 ? 'warn' : 'ok'}">${q[2]}%</span></div>`).join('')}</section>
<section class="card err"><h2>Lỗi hay gặp</h2>${E.map((e, i) => `<div class="erow"><b class="cnt">${e[1]}</b><div><strong>${e[0]}</strong><small>${e[2]}</small></div></div>`).join('')}<button class="btn pri">${L.ic('a', 'redo')}Giao lại câu 3 cho 14 em</button></section>
<section class="card who"><h2>Em cần hỗ trợ</h2>${S.map((h) => `<div class="hrow"><span class="ava s">${h[0].split(' ').pop()[0]}</span><div><strong>${h[0]}</strong><small>${h[1]} · ${h[2]} lượt</small></div><span class="chip ${h[3] === 'Cần hỗ trợ' ? 'err' : 'warn'}">${h[3]}</span></div>`).join('')}<button class="btn sec">Xem cả 36 em</button></section>
</main></div></body></html>`;

// ---------- B: Carbon bảng dữ liệu ----------
const gvB = () => H('b', 'GV kết quả lớp') + `<main class="main tbl">
<div class="kpis"><div class="kpi"><small>Đã nộp</small><b>31/36</b></div><div class="kpi"><small>Trung bình đúng</small><b>70%</b></div><div class="kpi"><small>Lượt làm TB</small><b>1,8</b></div><div class="kpi hot"><small>Câu vướng nhất</small><b>Câu 3 · 42%</b></div></div>
<div class="tabs"><a class="on">Cả lớp</a><a>Từng em</a><a>Lỗi hay gặp</a></div>
<section class="panel"><div class="row sp"><h2>Cả lớp vướng ở đâu · từng câu</h2><div class="row"><button class="btn sec">${L.ic('b', 'redo')}Giao lại</button><button class="btn pri">${L.ic('b', 'file')}Xuất bảng</button></div></div>
<table class="dt"><thead><tr><th>Câu</th><th>Nội dung</th><th>Tỉ lệ đúng</th><th></th><th>Lượt TB</th><th>Trạng thái</th></tr></thead><tbody>
${Q.map((q) => `<tr class="${q[2] < 60 ? 'hl' : ''}"><td>${q[0]}</td><td>${q[1]}</td><td class="mono">${q[2]}%</td><td class="w"><div class="prog bar1 ${bad(q[2])}"><i style="width:${q[2]}%"></i></div></td><td class="mono">${String(q[3]).replace('.', ',')}</td><td><span class="chip ${q[2] < 60 ? 'warn' : 'ok'}">${q[2] < 60 ? 'Vướng' : 'Ổn'}</span></td></tr>`).join('')}</tbody></table></section>
<div class="two"><section class="panel"><h2>Lỗi hay gặp</h2><table class="dt"><thead><tr><th>Lỗi</th><th>Số em</th><th>Câu</th></tr></thead><tbody>${E.map((e) => `<tr><td>${e[0]}</td><td class="mono">${e[1]}</td><td>${e[2]}</td></tr>`).join('')}</tbody></table></section>
<section class="panel"><h2>Em cần hỗ trợ</h2><table class="dt"><thead><tr><th>Họ tên</th><th>Câu vướng</th><th>Lượt</th><th>Mức</th></tr></thead><tbody>${S.map((h) => `<tr><td>${h[0]}</td><td>${h[1]}</td><td class="mono">${h[2]}</td><td><span class="chip ${h[3] === 'Cần hỗ trợ' ? 'err' : 'warn'}">${h[3]}</span></td></tr>`).join('')}</tbody></table></section></div>
</main></div></body></html>`;

// ---------- C: vở toán, giấy ô li ----------
const gvC = () => H('c', 'GV kết quả lớp') + `<main class="main nb"><section class="paper"><h2 class="hand">Cả lớp vướng ở đâu?</h2><p class="sub">11A1 · 31/36 em đã nộp · trung bình đúng 70%</p>
<div class="chart">${Q.map((q) => `<div class="col ${bad(q[2])}"><span class="pct">${q[2]}%</span><div class="barv" style="height:${q[2] * 2.4}px"></div><b>${q[0]}</b><small>${q[1]}</small></div>`).join('')}<div class="circ"></div><p class="note hand">← câu 3: chỉ 42%, cần giảng lại!</p></div></section>
<section class="paper s2"><h2 class="hand">Lỗi hay gặp</h2><ul class="todo">${E.map((e) => `<li><i></i><span><strong>${e[0]}</strong><small>${e[1]} em · ${e[2]}</small></span></li>`).join('')}</ul><button class="btn pri">${L.ic('c', 'redo')}Giao lại câu 3 cho 14 em</button></section>
<section class="paper s3"><h2 class="hand">Em cần hỗ trợ</h2>${S.map((h) => `<div class="hrow"><span class="ava s">${h[0].split(' ').pop()[0]}</span><div><strong>${h[0]}</strong><small>${h[1]} · ${h[2]} lượt</small></div><span class="chip ${h[3] === 'Cần hỗ trợ' ? 'err' : 'warn'}">${h[3]}</span></div>`).join('')}</section>
</main></div></body></html>`;

// ---------- D: bảng điều khiển gọn ----------
const spark = (v) => { const pts = [v - 14, v - 6, v - 10, v + 4, v].map((y, i) => `${i * 14},${24 - y / 4.4}`).join(' '); return `<svg width="56" height="26" viewBox="0 0 56 26"><polyline points="${pts}" fill="none" stroke="currentColor" stroke-width="1.5"/></svg>`; };
const gvD = () => H('d', 'GV kết quả lớp') + `<main class="main dn">
<div class="filters"><div class="sel">Lớp: 11A1 ${L.ic('d', 'next')}</div><div class="sel">Bộ luyện: Góc trong hình chóp</div><div class="sel">Tuần 12 · 17–23/11</div><span class="grow"></span><div class="in sm">${L.ic('d', 'search')}<span class="val ph">Tìm em…</span></div><button class="btn sec">${L.ic('d', 'file')}Xuất CSV</button></div>
<div class="kpis"><div class="kpi"><small>Đã nộp</small><b>31/36</b><em>86%</em></div><div class="kpi"><small>Đúng TB</small><b>70%</b><em>+4 so với tuần trước</em></div><div class="kpi"><small>Lượt TB</small><b>1,8</b><em>−0,2</em></div><div class="kpi hot"><small>Câu vướng</small><b>Câu 3</b><em>42% đúng</em></div><div class="kpi"><small>Cần hỗ trợ</small><b>5 em</b><em>3 em mức cao</em></div></div>
<div class="split"><section class="panel"><div class="row sp"><h2>Từng câu</h2><div class="tabs"><a class="on">Cả lớp</a><a>Từng em</a><a>Lỗi</a></div></div>
<table class="dt"><thead><tr><th>Câu</th><th>Nội dung</th><th>% đúng</th><th>Xu hướng</th><th>Lượt TB</th><th>Chưa làm</th></tr></thead><tbody>${Q.map((q, i) => `<tr class="${i === 2 ? 'sel' : ''}"><td><strong>${q[0]}</strong></td><td>${q[1]}</td><td><span class="heat ${bad(q[2])}">${q[2]}%</span></td><td class="${bad(q[2])}">${spark(q[2])}</td><td class="mono">${String(q[3]).replace('.', ',')}</td><td class="mono">${[0, 1, 5, 2, 8][i]}</td></tr>`).join('')}</tbody></table>
<h2 class="mt">Em cần hỗ trợ</h2><table class="dt"><thead><tr><th>Họ tên</th><th>Câu vướng</th><th>Lượt</th><th>Mức</th><th></th></tr></thead><tbody>${S.map((h) => `<tr><td>${h[0]}</td><td>${h[1]}</td><td class="mono">${h[2]}</td><td><span class="chip ${h[3] === 'Cần hỗ trợ' ? 'err' : 'warn'}">${h[3]}</span></td><td class="r"><button class="btn gh sm">Nhắn</button></td></tr>`).join('')}</tbody></table></section>
<aside class="panel insp"><div class="row sp"><h2>Câu 3 · chi tiết</h2><span class="chip warn">42% đúng</span></div><p class="help">Khoảng cách từ A đến (SBC)</p><h3>Lỗi hay gặp</h3>${E.map((e) => `<div class="erow"><b class="mono">${e[1]}</b><span>${e[0]}</span></div>`).join('')}<h3>Gợi ý</h3><div class="note">${L.ic('d', 'info')}<p>Giảng lại cách dựng đường cao từ A xuống (SBC) bằng tam giác SAB vuông tại A.</p></div><button class="btn pri">${L.ic('d', 'redo')}Giao lại cho 14 em</button><button class="btn sec">Xem lời giải mẫu</button></aside></div>
</main></div></body></html>`;

const SPEC = {
  a: { name: 'A · Lớp học sáng', line: 'Material 3 · pill · màu tươi · Nunito · Material Symbols Rounded (đặc)', icons: 'Material Symbols Rounded, FILL 1', rows: [['Chuẩn', 'Material Design 3 (tonal, pill)'], ['Bo góc', 'Nút/chip/ô nhập 999 px (pill) · thẻ 24 px · phím 16 px'], ['Cao chạm', 'Nút 52 px · phím 52 px · chip 36 px (không chạm riêng) ≥ 44'], ['Màu chính', '#00796B nền #F4FAF7 · chữ trắng/#00796B: 5,3:1 (AA)'], ['Font', 'Nunito 400–800, chữ ≥ 14 px']] },
  b: { name: 'B · Bảng đen tối', line: 'IBM Carbon (g100) · vuông góc · nét phấn · IBM Plex · icon nét 1,5 px', icons: 'nét 1,5 px vuông đầu (kiểu Carbon)', rows: [['Chuẩn', 'IBM Carbon Design System, theme g100'], ['Bo góc', '0 px mọi thành phần (chip 24 px bo tròn theo Carbon tag)'], ['Cao chạm', 'Nút 48 px · phím 52 px · hàng bảng 48 px'], ['Màu chính', '#0F62FE chữ trắng 5,3:1 · chữ #F4F4F4 trên #161616 16:1 (AA/AAA)'], ['Font', 'IBM Plex Sans/Mono, chữ ≥ 14 px']] },
  c: { name: 'C · Giấy kẻ ô', line: 'Apple HIG · bo nhẹ · nền vở ô li · nét bút chì · Be Vietnam Pro + Caveat', icons: 'nét mảnh 1,25 px (kiểu SF Symbols)', rows: [['Chuẩn', 'Apple Human Interface Guidelines (iOS/iPadOS)'], ['Bo góc', 'Nút/ô nhập 10 px · thẻ/giấy 14 px · phím 8 px · segmented 9 px'], ['Cao chạm', 'Nút 44 px · phím 48 px · hàng 52 px'], ['Màu chính', '#0A62C9 (xanh hệ thống đậm) chữ trắng 6,0:1 (AA)'], ['Font', 'Be Vietnam Pro, ghi chú Caveat; chữ ≥ 14 px']] },
  d: { name: 'D · Bảng điều khiển gọn', line: 'Fluent 2 / shadcn · bo 6 px · mật độ cao · Inter · Lucide 2 px', icons: 'Lucide (nét 2 px, ISC)', rows: [['Chuẩn', 'Radix/shadcn (token) + Fluent 2 (mật độ, tiêu điểm)'], ['Bo góc', '6 px nút/ô nhập/thẻ · chip 4 px · phím 6 px'], ['Cao chạm', 'Nút 36 px (web) / 44 px (cảm ứng iPad) · phím 48 px'], ['Màu chính', '#2563EB… dùng #1D4ED8 chữ trắng 6,7:1 (AA)'], ['Font', 'Inter 400–700, JetBrains Mono số; chữ ≥ 14 px']] },
};

const out = { a: gvA, b: gvB, c: gvC, d: gvD };
const dev = { a: 'phone', b: 'ipad', c: 'ipad', d: 'ipad' };
for (const s of ['a', 'b', 'c', 'd']) {
  const dir = path.join(__dirname, s);
  fs.writeFileSync(path.join(dir, 'hs-lam-bai.html'), L.hs(s, dev[s]));
  fs.writeFileSync(path.join(dir, 'gv-lop.html'), out[s]());
  fs.writeFileSync(path.join(dir, 'them-de.html'), L.duyet(s, 'web'));
  fs.writeFileSync(path.join(dir, 'component.html'), L.comp(s, SPEC[s]));
}
console.log('ok');
