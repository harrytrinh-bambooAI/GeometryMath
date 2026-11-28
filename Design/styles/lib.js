// lib.js · Phiên bản: v0.2 · 2026-11-26 · dữ liệu mẫu + icon + hình chóp dùng chung cho 4 hướng style
// Lịch sử: v0.1 T-261126-39 tạo · v0.2 (lệnh owner 26/11) cả 4 hướng dùng bộ icon của A (Material Symbols Rounded, đặc); màu icon theo token từng theme (luật .ic trong <hướng>/style.css)
const L = {
  I: { // [Lucide path (ISC), Material Symbols name]
    home: ['<path d="M15 21v-8a1 1 0 0 0-1-1h-4a1 1 0 0 0-1 1v8"/><path d="M3 10a2 2 0 0 1 .709-1.528l7-5.999a2 2 0 0 1 2.582 0l7 5.999A2 2 0 0 1 21 10v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>', 'home'],
    cube: ['<path d="M21 8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16Z"/><path d="m3.3 7 8.7 5 8.7-5"/><path d="M12 22V12"/>', 'deployed_code'],
    users: ['<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>', 'groups'],
    layers: ['<path d="m12.83 2.18a2 2 0 0 0-1.66 0L2.6 6.08a1 1 0 0 0 0 1.83l8.58 3.91a2 2 0 0 0 1.66 0l8.58-3.9a1 1 0 0 0 0-1.83Z"/><path d="m22 17.65-9.17 4.16a2 2 0 0 1-1.66 0L2 17.65"/><path d="m22 12.65-9.17 4.16a2 2 0 0 1-1.66 0L2 12.65"/>', 'layers'],
    clock: ['<circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>', 'history'],
    check: ['<path d="M20 6 9 17l-5-5"/>', 'check'],
    lock: ['<rect width="18" height="11" x="3" y="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>', 'lock'],
    eye: ['<path d="M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0"/><circle cx="12" cy="12" r="3"/>', 'visibility'],
    redo: ['<path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/>', 'replay'],
    back: ['<path d="M10 5a2 2 0 0 0-1.344.519l-6.328 5.74a1 1 0 0 0 0 1.481l6.328 5.741A2 2 0 0 0 10 19h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2z"/><path d="m12 9 6 6"/><path d="m18 9-6 6"/>', 'backspace'],
    file: ['<path d="M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z"/><path d="M14 2v4a2 2 0 0 0 2 2h4"/>', 'description'],
    chart: ['<line x1="12" x2="12" y1="20" y2="10"/><line x1="18" x2="18" y1="20" y2="4"/><line x1="6" x2="6" y1="20" y2="16"/>', 'bar_chart'],
    pencil: ['<path d="M21.174 6.812a1 1 0 0 0-3.986-3.987L3.842 16.174a2 2 0 0 0-.5.83l-1.321 4.352a.5.5 0 0 0 .623.622l4.353-1.32a2 2 0 0 0 .83-.497z"/>', 'edit'],
    spark: ['<path d="M12 3l1.9 5.8L20 11l-6.1 2.2L12 19l-1.9-5.8L4 11l6.1-2.2z"/>', 'auto_awesome'],
    shield: ['<path d="M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 0 1 1z"/><path d="m9 12 2 2 4-4"/>', 'verified_user'],
    next: ['<path d="M5 12h14"/><path d="m12 5 7 7-7 7"/>', 'arrow_forward'],
    prev: ['<path d="m12 19-7-7 7-7"/><path d="M19 12H5"/>', 'arrow_back'],
    x: ['<path d="M18 6 6 18"/><path d="m6 6 12 12"/>', 'close'],
    plus: ['<path d="M5 12h14"/><path d="M12 5v14"/>', 'add'],
    warn: ['<path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3"/><path d="M12 9v4"/><path d="M12 17h.01"/>', 'warning'],
    info: ['<circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/>', 'info'],
    bolt: ['<path d="M4 14a1 1 0 0 1-.78-1.63l9.9-10.2a.5.5 0 0 1 .86.46l-1.92 6.02A1 1 0 0 0 13 10h7a1 1 0 0 1 .78 1.63l-9.9 10.2a.5.5 0 0 1-.86-.46l1.92-6.02A1 1 0 0 0 11 14z"/>', 'bolt'],
    search: ['<circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/>', 'search'],
  },
  ic(s, n, cls = '') { // v0.2: một bộ icon cho cả 4 hướng; lớp .ic để style.css từng hướng đặt cỡ + màu theo token
    return `<span class="ms ic ${cls}" aria-hidden="true">${this.I[n][1]}</span>`;
  },
  FONTS: {
    a: 'family=Nunito:wght@400;600;700;800&family=Material+Symbols+Rounded:opsz,wght,FILL,GRAD@24,500,1,0',
    b: 'family=IBM+Plex+Sans:wght@400;500;600&family=IBM+Plex+Mono:wght@400;500&family=Patrick+Hand&family=Material+Symbols+Rounded:opsz,wght,FILL,GRAD@24,500,1,0',
    c: 'family=Be+Vietnam+Pro:wght@400;500;600;700&family=Caveat:wght@500;700&family=Material+Symbols+Rounded:opsz,wght,FILL,GRAD@24,500,1,0',
    d: 'family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500&family=Material+Symbols+Rounded:opsz,wght,FILL,GRAD@24,500,1,0',
  },
  head(s, title, dev) {
    return `<!doctype html><html lang="vi"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>${title}</title>
<link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?${this.FONTS[s]}&display=swap"><link rel="stylesheet" href="style.css"></head>
<body class="s-${s} dev-${dev}">`;
  },
  // Hình chóp S.ABCD. Toạ độ viewBox 460x440 (đã dịch từ ảnh bản mẫu)
  P: { S: [170, 32], A: [197, 258], B: [43, 330], C: [276, 388], D: [430, 304] },
  pyr(s) {
    const { S, A, B, C, D } = this.P, pt = (p) => p.join(',');
    const cfg = {
      a: { st: '#00796B', sw: 4, f1: 'rgba(0,121,107,.16)', f2: 'rgba(255,200,61,.45)', dash: '9 8', dot: '#FF6F61', lab: '#004D40', font: 'Nunito', fw: 800, fs: 28, ang: '#FF6F61' },
      b: { st: '#F4F4F4', sw: 2.5, f1: 'rgba(255,255,255,.05)', f2: 'rgba(69,137,255,.22)', dash: '8 7', dot: '#FFD966', lab: '#FFD966', font: 'Patrick Hand', fw: 400, fs: 32, ang: '#FFD966' },
      c: { st: '#3A3A3C', sw: 2.2, f1: 'url(#hat)', f2: 'rgba(255,214,10,.28)', dash: '7 6', dot: '#3A3A3C', lab: '#1C1C1E', font: 'Caveat', fw: 700, fs: 36, ang: '#E5483B' },
      d: { st: '#0F172A', sw: 1.6, f1: '#F1F5F9', f2: '#E2E8F0', dash: '5 4', dot: '#0F172A', lab: '#0F172A', font: 'Inter', fw: 600, fs: 15, ang: '#DC2626' },
    }[s];
    const lab = (n, p, dx, dy) => `<text x="${p[0] + dx}" y="${p[1] + dy}" font-family="${cfg.font}" font-weight="${cfg.fw}" font-size="${cfg.fs}" fill="${cfg.lab}">${n}</text>`;
    const defs = `<defs><pattern id="hat" width="8" height="8" patternUnits="userSpaceOnUse" patternTransform="rotate(45)"><line x1="0" y1="0" x2="0" y2="8" stroke="#8E8E93" stroke-width="1"/></pattern>
<filter id="rough"><feTurbulence type="fractalNoise" baseFrequency="0.035" numOctaves="2" seed="4" result="n"/><feDisplacementMap in="SourceGraphic" in2="n" scale="3.2"/></filter>
<filter id="chalk"><feTurbulence type="fractalNoise" baseFrequency="0.9" numOctaves="2" seed="2" result="n"/><feDisplacementMap in="SourceGraphic" in2="n" scale="2.2"/><feGaussianBlur stdDeviation=".5"/></filter></defs>`;
    const filt = s === 'c' ? ' filter="url(#rough)"' : s === 'b' ? ' filter="url(#chalk)"' : '';
    const ln = (a, b2, d) => `<line x1="${a[0]}" y1="${a[1]}" x2="${b2[0]}" y2="${b2[1]}" ${d ? `stroke-dasharray="${cfg.dash}" opacity=".75"` : ''}/>`;
    const shadow = s === 'a' ? '<ellipse cx="240" cy="410" rx="190" ry="16" fill="rgba(0,121,107,.12)"/>' : '';
    const extra = s === 'd' ? `<text x="${(S[0] + C[0]) / 2 + 12}" y="${(S[1] + C[1]) / 2}" font-family="Inter" font-size="14" fill="#475569">SC</text><text x="${(A[0] + D[0]) / 2 - 4}" y="${(A[1] + D[1]) / 2 + 22}" font-family="Inter" font-size="14" fill="#475569">a</text><text x="${(S[0] + A[0]) / 2 - 36}" y="${(S[1] + A[1]) / 2}" font-family="Inter" font-size="14" fill="#475569">a√2</text>` : '';
    return `<svg class="pyr" viewBox="0 -26 460 466" role="img" aria-label="Hình chóp S.ABCD">${defs}${shadow}
<g${filt} stroke="${cfg.st}" stroke-width="${cfg.sw}" stroke-linecap="round" stroke-linejoin="round" fill="none">
<polygon points="${pt(A)} ${pt(B)} ${pt(C)} ${pt(D)}" fill="${cfg.f2}" stroke="none"/>
<polygon points="${pt(S)} ${pt(B)} ${pt(C)}" fill="${cfg.f1}" stroke="none"/><polygon points="${pt(S)} ${pt(C)} ${pt(D)}" fill="${cfg.f1}" stroke="none"/>
${ln(S, B)}${ln(S, C)}${ln(S, D)}${ln(B, C)}${ln(C, D)}${ln(S, A, 1)}${ln(A, B, 1)}${ln(A, D, 1)}${ln(A, C, 1)}
<path d="M 264.6 349.7 A 40 40 0 0 1 255.2 353.8" stroke="${cfg.ang}" stroke-width="${cfg.sw}"/></g>${extra}
<g fill="${cfg.dot}">${[S, A, B, C, D].map((p) => `<circle cx="${p[0]}" cy="${p[1]}" r="${s === 'd' ? 4 : 6}"/>`).join('')}</g>
${lab('S', S, -8, -14)}${lab('A', A, -34, -4)}${lab('B', B, -30, 12)}${lab('C', C, 6, 30)}${lab('D', D, 12, 6)}
<text x="${C[0] - 62}" y="${C[1] - 52}" font-family="${cfg.font}" font-weight="${cfg.fw}" font-size="${s === 'd' ? 14 : cfg.fs - 6}" fill="${cfg.ang}">45°</text></svg>`;
  },
  NAV_HS: [['home', 'Bài của em'], ['cube', 'Tự học'], ['users', 'Nhóm học'], ['layers', 'Môn khác'], ['clock', 'Lịch sử']],
  NAV_GV: [['home', 'Tổng quan'], ['file', 'Kho đề'], ['layers', 'Giáo trình'], ['users', 'Lớp học'], ['chart', 'Theo dõi'], ['pencil', 'Chấm bài'], ['spark', 'Trợ lý']],
  nav(s, items, on, extra = '') {
    return `<nav class="nav" aria-label="Điều hướng"><div class="logo">${this.ic(s, 'cube')}<b>GEO</b></div>${items.map((it, i) => `<a class="${i === on ? 'on' : ''}">${this.ic(s, it[0])}<span>${it[1]}</span></a>`).join('')}${extra}</nav>`;
  },
  KEYS: ['7', '8', '9', '(', ')', '⌫', '4', '5', '6', '+', '−', '√', '1', '2', '3', '×', '÷', 'π', '0', ',', '°', '/', '^', 'a'],
  keypad(s) {
    return `<div class="keys">${this.KEYS.map((k) => `<button class="key ${k === '⌫' ? 'fn' : /[()+−√×÷π/^°]/.test(k) ? 'op' : ''}" aria-label="${k === '⌫' ? 'Xóa lùi' : k}">${k === '⌫' ? this.ic(s, 'back') : k}</button>`).join('')}</div>
<div class="acts"><button class="btn sec">x</button><button class="btn sec">Xóa hết</button><button class="btn pri">${this.ic(s, 'check')}Kiểm tra</button></div>`;
  },
  // Màn 1: Học sinh làm bài
  hs(s, dev) {
    const prog = '<div class="prog seg"><i class="ok"></i><i class="cur"></i><i></i><i></i><i></i></div>';
    return this.head(s, 'HS làm bài', dev) + `<div class="app">${this.nav(s, this.NAV_HS, 0)}
<header class="bar"><div><h1>Bài luyện: Góc trong hình chóp</h1><p class="crumb">Bài của em › Bộ luyện 11A1 › Câu 2</p></div><span class="chip info">${this.ic(s, 'spark')}Concept · chưa có trong MVP</span><span class="ava">A</span></header>
<main class="main stage"><section class="prob"><span class="ico">${this.ic(s, 'file')}</span><p>Cho hình chóp S.ABCD, đáy ABCD là hình vuông cạnh a, SA ⊥ (ABCD), SA = a√2. Tính góc giữa SC và mặt phẳng đáy.</p></section>
<section class="fig">${this.pyr(s)}</section>
<aside class="ans"><div class="row"><span class="chip info">${this.ic(s, 'cube')}Câu 2/5</span><span class="chip warn">${this.ic(s, 'bolt')}Còn 2/3 lượt</span></div>${prog}
<label class="lbl">Đáp số của em</label><div class="in focus"><span class="val">45°<i class="caret"></i></span><small>đơn vị: độ</small></div>
<p class="help">Nhập đáp số rồi bấm Kiểm tra. Chấp nhận sai số ±0,5°.</p>${this.keypad(s)}</aside></main></div></body></html>`;
  },
  Q: [['Câu 1', 'Góc giữa SA và mặt đáy', 94, 1.2], ['Câu 2', 'Góc giữa SC và (ABCD)', 81, 1.6], ['Câu 3', 'Khoảng cách từ A đến (SBC)', 42, 2.8], ['Câu 4', 'Thể tích khối chóp S.ABCD', 77, 1.4], ['Câu 5', 'Thiết diện qua trung điểm SA', 58, 2.1]],
  ERR: [['Nhầm góc SCB với góc giữa SC và (ABCD)', 11, 'Câu 2, 3'], ['Quên AC = a√2, lấy AC = a', 8, 'Câu 2'], ['Chọn sai chân đường vuông góc hạ từ A', 7, 'Câu 3'], ['Nhập thiếu đơn vị ° hoặc dấu √', 4, 'Câu 1, 4']],
  HSL: [['Nguyễn Minh Anh', 'Câu 3, 5', 3, 'Cần hỗ trợ'], ['Trần Gia Bảo', 'Câu 3', 3, 'Cần hỗ trợ'], ['Lê Khánh Chi', 'Câu 5', 2, 'Theo dõi'], ['Phạm Đức Duy', 'Câu 3, 4', 3, 'Cần hỗ trợ'], ['Võ Hà Vy', 'Câu 2', 2, 'Theo dõi']],
  // Màn 3: Thêm đề - duyệt lời giải
  duyet(s, dev) {
    const steps = ['Vì SA ⊥ (ABCD) nên AC là hình chiếu của SC lên mặt đáy, suy ra góc (SC, (ABCD)) = góc SCA.', 'ABCD là hình vuông cạnh a nên AC = a√2.', 'Tam giác SAC vuông tại A: tan SCA = SA / AC = a√2 / a√2 = 1.', 'Vậy góc giữa SC và mặt phẳng đáy bằng 45°.'];
    const stepper = ['Chọn tệp', 'Tách bài', 'Soát lời giải', 'Giao lớp'].map((t, i) => `<li class="${i < 2 ? 'done' : i === 2 ? 'cur' : ''}"><b>${i < 2 ? this.ic(s, 'check') : i + 1}</b>${t}</li>`).join('');
    return this.head(s, 'Thêm đề - duyệt lời giải', dev) + `<div class="app">${this.nav(s, this.NAV_GV, 1)}
<header class="bar"><div><h1>Soát và duyệt lời giải</h1><p class="crumb">Kho đề › Thêm đề › Hình chóp › Góc giữa SC và đáy</p></div><span class="chip info">${this.ic(s, 'spark')}Concept · chưa có trong MVP</span><span class="ava">GV</span></header>
<main class="main review"><ol class="stepper">${stepper}</ol>
<section class="card figc"><div class="row sp"><h2>Hình</h2><span class="chip ok">${this.ic(s, 'check')}Đã kiểm chứng</span></div><div class="figbox">${this.pyr(s)}</div><p class="de">Đề: Cho hình chóp S.ABCD, đáy ABCD là hình vuông cạnh a, SA ⊥ (ABCD), SA = a√2. Tính góc giữa SC và mặt phẳng đáy.</p></section>
<section class="card sol"><div class="row sp"><h2>Lời giải · v2 <span class="chip neu">${this.ic(s, 'clock')}Đã giao</span></h2><button class="btn pri">${this.ic(s, 'pencil')}Tạo phiên bản mới (v3)</button></div>
<div class="note">${this.ic(s, 'lock')}<p>Đề đã giao cho 11A1 (hạn 18:00 thứ Sáu). Sửa sẽ tạo phiên bản mới v3; bài đã giao vẫn dùng v2 cho đến khi thầy cô chọn “Áp dụng cho lớp”.</p></div>
<ol class="steps">${steps.map((t, i) => `<li><b class="num">${i + 1}</b><p>${t}</p><span class="chip ok">${this.ic(s, 'check')}Máy khớp</span></li>`).join('')}</ol>
<button class="btn gh">${this.ic(s, 'plus')}Thêm bước · kéo để đổi thứ tự</button></section>
<aside class="card inbox"><h2>Hộp duyệt</h2><p class="lbl">Nhãn hai trục</p><div class="row"><span class="chip ok">${this.ic(s, 'check')}Đã kiểm chứng</span><span class="chip info">${this.ic(s, 'shield')}Giáo viên đã duyệt</span></div>
<p class="help">Trục máy do máy gắn. Trục người do thầy cô gắn. Hai trục độc lập.</p><p class="lbl">Lịch sử phiên bản</p>
<ul class="ver"><li class="on"><b>v2</b><div><strong>Đã giao 11A1 · 14:30</strong><small>Hôm nay 11:58</small></div></li><li><b>v1</b><div><strong>AI soạn · máy chưa kiểm</strong><small>Hôm nay 11:44</small></div></li></ul>
<div class="okbox">${this.ic(s, 'shield')}<p>Giáo viên đã duyệt v2 · máy đã kiểm chứng</p></div></aside></main></div></body></html>`;
  },
  // Bảng component (dùng chung DOM, CSS khác nhau)
  comp(s, spec) {
    const I = Object.keys(this.I);
    const btn = (c, label, st = '', ic = '') => `<button class="btn ${c} ${st}">${ic ? this.ic(s, ic) : ''}${label}</button>`;
    const col = (name, c, ic) => `<div class="cell"><small>${name}</small>${btn(c, 'Kiểm tra', '', ic)}${btn(c, 'Di chuột', 'hov')}${btn(c, 'Nhấn', 'act')}${btn(c, 'Tiêu điểm', 'foc')}${btn(c, 'Vô hiệu', 'dis')}</div>`;
    return this.head(s, 'Bảng component', 'web') + `<div class="sheet"><header class="sh"><h1>Bảng component · ${spec.name}</h1><p>${spec.line}</p></header>
<div class="grid"><section class="panel"><h3>Nút</h3><div class="cols">${col('Chính', 'pri', 'check')}${col('Viền / phụ', 'sec')}${col('Ghost / chữ', 'gh')}${col('Nguy hiểm', 'dg', 'x')}</div></section>
<section class="panel"><h3>Ô nhập</h3><div class="stack"><label class="lbl">Đáp số · mặc định</label><div class="in"><span class="val ph">Nhập đáp số</span><small>độ</small></div>
<label class="lbl">Đang nhập (tiêu điểm)</label><div class="in focus"><span class="val">45°<i class="caret"></i></span><small>độ</small></div>
<label class="lbl">Lỗi</label><div class="in err"><span class="val">4,5 5</span><small>độ</small></div><p class="errt">${this.ic(s, 'warn')}Sai định dạng số. Ví dụ: 45 hoặc a√2/3.</p>
<label class="lbl">Vô hiệu</label><div class="in dis"><span class="val ph">Hết lượt</span></div></div></section>
<section class="panel"><h3>Chip / Badge</h3><div class="wrap"><span class="chip ok">${this.ic(s, 'check')}Đúng</span><span class="chip warn">Cần xem lại</span><span class="chip err">${this.ic(s, 'x')}Sai</span><span class="chip info">${this.ic(s, 'shield')}Giáo viên đã duyệt</span><span class="chip neu">${this.ic(s, 'lock')}Lời giải khóa</span><span class="chip warn">${this.ic(s, 'layers')}Trùng kho · 92%</span><span class="chip ok">${this.ic(s, 'check')}Máy khớp</span></div>
<h3>Tab</h3><div class="tabs"><a class="on">Cả lớp</a><a>Từng em</a><a>Lỗi hay gặp</a></div>
<h3>Tiến độ</h3><div class="prog seg"><i class="ok"></i><i class="cur"></i><i></i><i></i><i></i></div><div class="prog bar1"><i style="width:66%"></i></div><small class="mut">Câu 2/5 · 66% lớp làm đúng</small></section>
<section class="panel half"><h3>Thẻ</h3><div class="card mini"><div class="row sp"><strong>Bộ luyện góc · 11A1</strong><span class="chip warn">Hạn 18:00 T6</span></div><p>5 câu · 31/36 em đã nộp · câu 3 nhiều em vướng nhất.</p><div class="row">${btn('pri', 'Xem kết quả', '', 'chart')}${btn('sec', 'Giao lại')}</div></div>
<h3>Toast</h3><div class="toasts"><div class="toast ok">${this.ic(s, 'check')}Đã lưu v3 · chưa áp dụng cho lớp</div><div class="toast err">${this.ic(s, 'warn')}Mất mạng · bài làm được giữ trên máy</div></div></section>
<section class="panel half"><h3>Bàn phím toán</h3><div class="kp">${this.keypad(s)}</div></section>
<section class="panel wide"><h3>Bộ icon · ${spec.icons}</h3><div class="icons">${I.map((n) => `<div class="icell">${this.ic(s, n)}<small>${n}</small></div>`).join('')}</div></section>
<section class="panel wide"><h3>Thông số</h3><table class="spec">${spec.rows.map((r) => `<tr><th>${r[0]}</th><td>${r[1]}</td></tr>`).join('')}</table></section></div></div></body></html>`;
  },
};
module.exports = L;
