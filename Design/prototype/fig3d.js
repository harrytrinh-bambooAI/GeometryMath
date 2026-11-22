/* ==========================================================================
 * Fig3D — hình không gian tương tác bằng SVG (JavaScript thuần, không thư viện)
 * Nguồn yêu cầu: PO-35 (xoay, vẽ thêm, mở rộng mặt, kéo dài cạnh),
 *                PO-36 (lời giải theo bước cộng dồn lên hình),
 *                PO-08 / PO-17 (không hiện giá trị của đại lượng đề hỏi),
 *                PO-01 (cảm ứng là chính).
 * Cần kèm fig3d.css. Biến toàn cục duy nhất: window.Fig3D.
 *
 * ---- GỌI ------------------------------------------------------------------
 *   var st = Fig3D.newState();                 // object thuần, bên gọi giữ (JSON hóa được)
 *   var h  = Fig3D.mount(el, 'p1', st, {
 *     hidden:  ['ang:S-D-A'],                  // đại lượng chưa được hiện số (bỏ trống = lấy scene.asked;
 *                                              //  [] = hiện hết, dùng khi lời giải đã mở)
 *     onChange: function (state) { ... },      // sau mỗi thay đổi ĐÃ XONG (thả tay sau khi kéo, chạm, gọi API)
 *     onEvent:  function (ev) { toast(ev.detail.text); },   // {type, detail}
 *     hud: true,        // thanh gợi ý + tùy chọn nhỏ (tỉ lệ chia, nét liền/đứt, Xóa) vẽ trong khung hình
 *     autoView: true,   // setStep tự xoay tới góc nhìn của bước (nếu bước có khai báo)
 *     animate: true,    // hiệu ứng vẽ dần / hiện dần và xoay mượt
 *     grid: false       // nền lưới chấm
 *   });
 *   Gọi lại mount() với CÙNG state trên một phần tử mới bất kỳ lúc nào: hình vẽ lại y nguyên
 *   (góc nhìn, thu phóng, đối tượng vẽ thêm, bước, công cụ, điểm đang chọn dở). mount() lên một
 *   phần tử đã gắn thì tự hủy bản cũ. Nếu state.scene khác sceneId thì state được khởi tạo lại.
 *
 * ---- STATE ----------------------------------------------------------------
 *   { v:1, scene:'p1', yaw:25, pitch:20, zoom:1,     // độ; pitch trong [-90, 90]; zoom trong [0.5, 4]
 *     tool:'select', step:0, objs:[...], pick:[...], sel:null,
 *     ptMode:'mid'|'1:2'|'1:3'|'int', dash:false, seq:1, va:null, anim:null }
 *   objs (đối tượng vẽ thêm của người dùng, mỗi cái có id 'u<n>'):
 *     {t:'pt',   name:'M', p:[x,y,z], def:{...}}      điểm
 *     {t:'seg',  a:'S', b:'M', dash:false}            đoạn thẳng
 *     {t:'plane',pts:['S','B','D']}                   mặt phẳng qua ba điểm (tô tam giác)
 *     {t:'ext',  a:'A', b:'B'}                        kéo dài cạnh về hai phía
 *     {t:'grow', pts:['A','B','C','D'], k:1.7}        mở rộng mặt quanh trọng tâm
 *     {t:'ang',  a:'S', o:'A', b:'D'}                 ký hiệu góc (tự thành ô vuông khi 90°)
 *     {t:'hl',   edge:['S','A']} | {t:'hl', face:[..]} tô nổi bật
 *     {t:'mlen', a:'S', b:'A'}                        số đo độ dài
 *     {t:'mang', s1:['S','D'], s2:['A','D']}          số đo góc giữa hai đoạn
 *
 * ---- HANDLE (kết quả của mount) ---------------------------------------------
 *   setTool(id)            'select'|'point'|'segment'|'plane'|'extend'|'length'|'angle'  (Fig3D.TOOLS)
 *   setStep(n) / stepCount()         0 = hình gốc; n hiện cộng dồn bước 1..n
 *   setView('default'|'front'|'top'|'side' | {yaw,pitch,zoom}) / resetView() / zoomBy(k)
 *   addPoint({mid:['S','D']} | {ratio:['S','D'], m:1, n:2} | {int:[['A','C'],['B','D']]} | {at:[x,y,z]}, name?)
 *   addSegment(a, b, {dash})   addPlane(a, b, c)   extendEdge(a, b)   growFace([..], k?)
 *   markAngle(a, o, b)         highlight({edge:[a,b]} | {face:[..]})
 *   measureLength(a, b)        measureAngle([a,b], [c,d])      -> {text, hidden}
 *   remove(id)  clear()        xóa một / xóa hết phần vẽ thêm
 *   reset()                    xóa phần vẽ thêm + về công cụ Chọn + về góc nhìn mặc định (giữ bước)
 *   snapshot()                 {pts:{A:[x,y]}, edges:[{e:'A-B', hidden:bool}], objs:[id], view:{..}}  (để kiểm thử)
 *   redraw()  destroy()  state
 *
 * ---- SỰ KIỆN (onEvent) ------------------------------------------------------
 *   type: 'add' | 'remove' | 'clear' | 'reset' | 'measure' | 'step' | 'view' | 'tool' | 'pick' | 'select' | 'info'
 *   detail.text luôn là câu tiếng Việt dùng được cho thông báo / nhật ký
 *   ('Đã thêm M là trung điểm SD', 'SA = a', '∠SDA: ẩn tới khi mở lời giải').
 *   'add' kèm detail.obj; 'measure' kèm detail.kind ('len'|'ang'), name, hidden và (khi không ẩn) value, valueText.
 *
 * ---- ĐẠI LƯỢNG ẨN (PO-08) ---------------------------------------------------
 *   'ang:S-D-A'  góc tại D giữa DS và DA.   'len:O-O′'  độ dài OO′.
 *   So khớp theo HÌNH HỌC, không theo tên: mọi cặp đoạn song song với hai cạnh của góc ẩn,
 *   mọi đoạn có hai đầu trùng vị trí với đoạn ẩn đều bị ẩn (ví dụ ∠MDA với M thuộc SD).
 *
 * ---- HÌNH MẪU ---------------------------------------------------------------
 *   Fig3D.SCENES.p1 … p6 khớp PROBS trong index.html. Dạng dữ liệu: xem SCENES bên dưới
 *   (pts, edges, faces, cone/cyl/sphere, curves, unit, asked, view, steps[{title, add, hl, view}]).
 * ========================================================================== */
(function () {
  'use strict';

  var DEG = Math.PI / 180;
  var INK = '#253150', PRI = '#3B5BDB', ACC = '#F0522B', VIO = '#7C4DFF', GRN = '#0F8A5F';
  var COL = { ink: INK, pri: PRI, acc: ACC, vio: VIO, grn: GRN };
  var HIDDEN_TEXT = 'ẩn tới khi mở lời giải';
  var TOOLS = [['select', 'Chọn'], ['point', 'Điểm'], ['segment', 'Đoạn'], ['plane', 'Mặt phẳng'],
    ['extend', 'Kéo dài'], ['length', 'Đo dài'], ['angle', 'Đo góc']];
  var VIEWS = { front: [0, 0], top: [0, 90], side: [90, 0] };
  var VIEW_NAMES = { 'default': 'mặc định', front: 'từ trước', top: 'từ trên', side: 'từ bên' };
  var NEAR = 13; /* công cụ nhận cả cạnh lẫn mặt: trong lòng một mặt, cạnh chỉ thắng khi chạm cách nó không quá NEAR px */
  var NAMES = ['M', 'N', 'P', 'Q', 'I', 'J', 'K', 'H', 'E', 'F', 'G', 'T', 'U', 'V'];

  /* ---------- vector ---------- */
  function sub(a, b) { return [a[0] - b[0], a[1] - b[1], a[2] - b[2]]; }
  function add(a, b) { return [a[0] + b[0], a[1] + b[1], a[2] + b[2]]; }
  function mul(a, k) { return [a[0] * k, a[1] * k, a[2] * k]; }
  function dot(a, b) { return a[0] * b[0] + a[1] * b[1] + a[2] * b[2]; }
  function cross(a, b) { return [a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]]; }
  function len(a) { return Math.sqrt(dot(a, a)); }
  function unit(a) { var l = len(a) || 1; return mul(a, 1 / l); }
  function mix(a, b, t) { return [a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, a[2] + (b[2] - a[2]) * t]; }
  function clamp(x, a, b) { return x < a ? a : x > b ? b : x; }
  function esc(s) { return String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;'); }
  function f1(x) { return (Math.round(x * 10) / 10).toString(); }
  function gcd(a, b) { return b ? gcd(b, a % b) : a; }
  function dec(x, d) { return (+x.toFixed(d == null ? 2 : d)).toString().replace('.', ','); }

  /* ---------- hình mẫu (khớp PROBS p1…p6 trong index.html) ----------
   * Hệ trục: z hướng lên, người nhìn "từ trước" đứng ở phía y âm. */
  function cubePts() {
    return { 'A': [0, 1, 0], 'B': [0, 0, 0], 'C': [1, 0, 0], 'D': [1, 1, 0],
      'A′': [0, 1, 1], 'B′': [0, 0, 1], 'C′': [1, 0, 1], 'D′': [1, 1, 1] };
  }
  var CUBE_E = [['A', 'B'], ['B', 'C'], ['C', 'D'], ['D', 'A'], ['A′', 'B′'], ['B′', 'C′'], ['C′', 'D′'], ['D′', 'A′'],
    ['A', 'A′'], ['B', 'B′'], ['C', 'C′'], ['D', 'D′']];
  var CUBE_F = [['A', 'B', 'C', 'D'], ['A′', 'B′', 'C′', 'D′'], ['A', 'B', 'B′', 'A′'], ['B', 'C', 'C′', 'B′'],
    ['C', 'D', 'D′', 'C′'], ['D', 'A', 'A′', 'D′']];
  var S3 = Math.sqrt(3);

  var SCENES = {
    p1: {
      id: 'p1', title: 'Hình chóp S.ABCD', unit: 'a', view: [25, 20],
      pts: { 'S': [0, 1, 1], 'A': [0, 1, 0], 'B': [0, 0, 0], 'C': [1, 0, 0], 'D': [1, 1, 0] },
      edges: [['A', 'B'], ['B', 'C'], ['C', 'D'], ['D', 'A'], ['S', 'A'], ['S', 'B'], ['S', 'C'], ['S', 'D']],
      faces: [['A', 'B', 'C', 'D'], ['S', 'A', 'B'], ['S', 'B', 'C'], ['S', 'C', 'D'], ['S', 'D', 'A']],
      asked: ['ang:S-D-A'],
      sym: { 'len:A-S': 'a', 'len:A-D': 'a', 'len:D-S': 'a√2' },
      steps: [
        { title: 'Tìm hình chiếu của SD',
          hl: [{ edge: ['S', 'A'] }, { edge: ['A', 'D'], c: 'grn' }],
          add: [{ t: 'ang', a: 'S', o: 'A', b: 'D' }] },
        { title: 'Xác định góc cần tìm', view: [14, 14],
          hl: [{ face: ['S', 'A', 'D'] }, { edge: ['S', 'D'] }],
          add: [{ t: 'ang', a: 'S', o: 'D', b: 'A' }] },
        { title: 'Tính góc', view: [14, 14],
          add: [{ t: 'ang', a: 'S', o: 'D', b: 'A', text: '45°' }] }
      ]
    },
    p2: {
      id: 'p2', title: 'Hình lập phương ABCD.A′B′C′D′', unit: 'a', view: [25, 20],
      pts: cubePts(), edges: CUBE_E, faces: CUBE_F,
      asked: ['len:O-O′'],
      steps: [
        { title: 'Nhận xét vị trí',
          hl: [{ face: ['A', 'B', 'C', 'D'], c: 'pri' }, { face: ['A′', 'B′', 'C′', 'D′'], c: 'pri' }],
          add: [{ t: 'seg', a: 'A', b: 'C' }, { t: 'seg', a: 'B′', b: 'D′' }] },
        { title: 'Tính khoảng cách',
          add: [{ t: 'pt', name: 'O', p: [0.5, 0.5, 0] }, { t: 'pt', name: 'O′', p: [0.5, 0.5, 1] },
            { t: 'seg', a: 'O', b: 'O′', text: 'a' },
            { t: 'ang', a: 'O′', o: 'O', b: 'C' }, { t: 'ang', a: 'O', o: 'O′', b: 'D′' }] }
      ]
    },
    p3: {
      id: 'p3', title: 'Lăng trụ đứng tam giác đều', unit: 'cm', view: [20, 18],
      pts: { 'A': [0, 2 * S3, 0], 'B': [-2, 0, 0], 'C': [2, 0, 0], 'A′': [0, 2 * S3, 6], 'B′': [-2, 0, 6], 'C′': [2, 0, 6] },
      edges: [['A', 'B'], ['B', 'C'], ['C', 'A'], ['A′', 'B′'], ['B′', 'C′'], ['C′', 'A′'], ['A', 'A′'], ['B', 'B′'], ['C', 'C′']],
      faces: [['A', 'B', 'C'], ['A′', 'B′', 'C′'], ['A', 'B', 'B′', 'A′'], ['B', 'C', 'C′', 'B′'], ['C', 'A', 'A′', 'C′']],
      asked: [],
      steps: [
        { title: 'Tính diện tích đáy', hl: [{ face: ['A', 'B', 'C'] }, { edge: ['B', 'C'], text: '4 cm' }] },
        { title: 'Tính thể tích', hl: [{ edge: ['C', 'C′'], text: 'h = 6 cm' }] }
      ]
    },
    p4: {
      id: 'p4', title: 'Hình nón', unit: 'cm', view: [20, 16],
      pts: { 'S': [0, 0, 4], 'O': [0, 0, 0], 'A': [3, 0, 0] },
      edges: [['S', 'A']], faces: [],
      cone: { c: [0, 0, 0], r: 3, apex: [0, 0, 4] },
      curves: [{ id: 'base', c: [0, 0, 0], r: 3 }],
      asked: [],
      steps: [
        { title: 'Diện tích xung quanh', hl: [{ edge: ['S', 'A'], text: 'l = 5 cm' }] },
        { title: 'Thể tích',
          hl: [{ face: ['S', 'O', 'A'] }],
          add: [{ t: 'seg', a: 'S', b: 'O', text: 'h = 4 cm' }, { t: 'seg', a: 'O', b: 'A', text: 'r = 3 cm' },
            { t: 'ang', a: 'S', o: 'O', b: 'A' }] }
      ]
    },
    p5: {
      id: 'p5', title: 'Hình trụ', unit: 'cm', view: [20, 16],
      pts: { 'O': [0, 0, 0], 'O′': [0, 0, 10], 'A': [4, 0, 0], 'A′': [4, 0, 10] },
      edges: [['O', 'A'], ['A', 'A′'], ['O′', 'A′']], faces: [],
      cyl: { c: [0, 0, 0], r: 4, h: 10 },
      curves: [{ id: 'bot', c: [0, 0, 0], r: 4 }, { id: 'top', c: [0, 0, 10], r: 4 }],
      asked: [],
      steps: [
        { title: 'Áp dụng công thức',
          hl: [{ surf: 1, c: 'pri' }, { disc: 'bot' }, { disc: 'top' },
            { edge: ['O', 'A'], text: 'r = 4 cm' }, { edge: ['A', 'A′'], text: 'h = 10 cm' }] }
      ]
    },
    p6: {
      id: 'p6', title: 'Mặt cầu ngoại tiếp hình lập phương', unit: 'a', view: [25, 20],
      pts: cubePts(), edges: CUBE_E, faces: CUBE_F,
      sphere: { c: [0.5, 0.5, 0.5], r: S3 / 2 },
      curves: [{ id: 'equator', c: [0.5, 0.5, 0.5], r: S3 / 2, tag: 'sphere' }],
      asked: ['A', 'B', 'C', 'D', 'A′', 'B′', 'C′', 'D′'].map(function (n) { return 'len:O-' + n; }),
      steps: [
        { title: 'Bán kính',
          add: [{ t: 'seg', a: 'A', b: 'C′', text: 'a√3' }, { t: 'pt', name: 'O', p: [0.5, 0.5, 0.5] }] },
        { title: 'Thể tích',
          add: [{ t: 'seg', a: 'O', b: 'C′', text: 'R = a√3/2', w: 4.2 }] }
      ]
    }
  };

  /* ---------- biên dịch hình: tâm, bán kính bao, khối che khuất ---------- */
  var CACHE = {};
  function circlePt(cv, t) { return [cv.c[0] + cv.r * Math.cos(t), cv.c[1] + cv.r * Math.sin(t), cv.c[2]]; }
  function planeOf(p, q, r, inside) {
    var n = unit(cross(sub(q, p), sub(r, p))), d = dot(n, p);
    if (dot(n, inside) > d) { n = mul(n, -1); d = -d; }
    return { n: n, d: d };
  }
  function compile(sc) {
    if (CACHE[sc.id]) return CACHE[sc.id];
    var C = { all: {}, solids: [], faceN: [] }, k, names = Object.keys(sc.pts), lo = [1e9, 1e9, 1e9], hi = [-1e9, -1e9, -1e9];
    function grow(p) { for (var i = 0; i < 3; i++) { lo[i] = Math.min(lo[i], p[i]); hi[i] = Math.max(hi[i], p[i]); } }
    names.forEach(function (n) { C.all[n] = sc.pts[n]; grow(sc.pts[n]); });
    (sc.steps || []).forEach(function (s) { (s.add || []).forEach(function (o) { if (o.t === 'pt') C.all[o.name] = o.p; }); });
    if (sc.sphere) { var s = sc.sphere; grow(add(s.c, [s.r, s.r, s.r])); grow(sub(s.c, [s.r, s.r, s.r])); }
    (sc.curves || []).forEach(function (cv) { grow(add(cv.c, [cv.r, cv.r, 0])); grow(sub(cv.c, [cv.r, cv.r, 0])); });
    C.center = mul(add(lo, hi), 0.5);
    C.R = 0;
    names.forEach(function (n) { C.R = Math.max(C.R, len(sub(sc.pts[n], C.center))); });
    (sc.curves || []).forEach(function (cv) { for (k = 0; k < 8; k++) C.R = Math.max(C.R, len(sub(circlePt(cv, k * Math.PI / 4), C.center))); });
    if (sc.sphere) C.R = Math.max(C.R, sc.sphere.r + len(sub(sc.sphere.c, C.center)));
    C.tol = C.R * 1e-3;
    if (sc.faces && sc.faces.length) {
      var g = [0, 0, 0];
      names.forEach(function (n) { g = add(g, sc.pts[n]); });
      g = mul(g, 1 / names.length);
      var planes = sc.faces.map(function (f) { return planeOf(sc.pts[f[0]], sc.pts[f[1]], sc.pts[f[2]], g); });
      C.faceN = planes.map(function (p) { return p.n; });
      C.solids.push({ planes: planes });
    }
    var N = 72, i, pl = [], a0, a1;
    if (sc.cone) {
      var cn = sc.cone, ins = mix(cn.c, cn.apex, 0.3);
      for (i = 0; i < N; i++) {
        a0 = i * 2 * Math.PI / N; a1 = (i + 1) * 2 * Math.PI / N;
        pl.push(planeOf(cn.apex, circlePt(cn, a0), circlePt(cn, a1), ins));
      }
      pl.push(planeOf(circlePt(cn, 0), circlePt(cn, 2), circlePt(cn, 4), ins));
      C.solids.push({ planes: pl });
    }
    if (sc.cyl) {
      var cy = sc.cyl, top = { c: add(cy.c, [0, 0, cy.h]), r: cy.r }, mid = add(cy.c, [0, 0, cy.h / 2]);
      for (i = 0; i < N; i++) {
        a0 = i * 2 * Math.PI / N; a1 = (i + 1) * 2 * Math.PI / N;
        pl.push(planeOf(circlePt(cy, a0), circlePt(cy, a1), circlePt(top, a0), mid));
      }
      pl.push(planeOf(circlePt(cy, 0), circlePt(cy, 2), circlePt(cy, 4), mid));
      pl.push(planeOf(circlePt(top, 0), circlePt(top, 2), circlePt(top, 4), mid));
      C.solids.push({ planes: pl });
    }
    if (sc.sphere) C.solids.push({ only: 'sphere', sphere: sc.sphere });
    CACHE[sc.id] = C;
    return C;
  }

  /* ---------- định dạng số đo ---------- */
  function surd(L) {
    for (var m = 1; m <= 6; m++) {
      var n = L * L * m * m, r = Math.round(n);
      if (r > 0 && r <= 600 && Math.abs(n - r) < 1e-6) {
        var k = 1, i;
        for (i = 2; i * i <= r; i++) while (r % (i * i) === 0) { r /= i * i; k *= i; }
        var g = gcd(k, m);
        return { k: k / g, r: r, m: m / g };
      }
    }
    return null;
  }
  function fmtLen(L, unitName) {
    var s = surd(L);
    if (unitName === 'a') {
      if (!s) return dec(L) + 'a';
      return (s.k > 1 ? s.k : '') + 'a' + (s.r > 1 ? '√' + s.r : '') + (s.m > 1 ? '/' + s.m : '');
    }
    var u = unitName ? ' ' + unitName : '';
    if (!s || s.r === 1) return dec(L) + u;
    return (s.k > 1 ? s.k : '') + '√' + s.r + (s.m > 1 ? '/' + s.m : '') + u;
  }
  function fmtAng(d) { return (Math.abs(d - Math.round(d)) < 1e-6 ? Math.round(d).toString() : dec(d, 1)) + '°'; }
  function keyLen(a, b) { return 'len:' + [a, b].sort().join('-'); }

  /* ---------- state ---------- */
  function initState(st, id) {
    var sc = SCENES[id], k;
    for (k in st) if (Object.prototype.hasOwnProperty.call(st, k)) delete st[k];
    st.v = 1; st.scene = id; st.yaw = sc.view[0]; st.pitch = sc.view[1]; st.zoom = 1;
    st.tool = 'select'; st.step = 0; st.objs = []; st.pick = []; st.sel = null;
    st.ptMode = 'mid'; st.dash = false; st.seq = 1; st.va = null; st.anim = null;
    return st;
  }
  function newState(id) { var st = {}; return id && SCENES[id] ? initState(st, id) : st; }

  var LIVE = typeof WeakMap !== 'undefined' ? new WeakMap() : null;

  /* ====================================================================== */
  function mount(box, sceneId, st, opt) {
    var sc = SCENES[sceneId];
    if (!box || !sc) throw new Error('Fig3D.mount: thiếu phần tử hoặc không có hình "' + sceneId + '"');
    if (!st || typeof st !== 'object') throw new Error('Fig3D.mount: thiếu state');
    opt = opt || {};
    if (LIVE && LIVE.get(box)) LIVE.get(box).destroy();
    if (st.scene !== sceneId || st.v !== 1) initState(st, sceneId);
    var C = compile(sc);
    var dead = false, raf = 0, looping = false, wheelT = 0, lastHud = null;
    var W = 400, H = 320, cam = null, hit = { pts: [], segs: [], faces: [], pills: [] };

    box.classList.add('fig3d');
    if (opt.grid) box.classList.add('f3-grid');
    box.innerHTML = '<svg class="f3-svg" role="img" aria-label="' + esc(sc.title) + '" style="touch-action:none;display:block;width:100%;height:100%"></svg>' +
      (opt.hud === false ? '' : '<div class="f3-hud"></div>');
    var svg = box.firstChild, hudEl = opt.hud === false ? null : box.lastChild;

    /* ----- tiện ích điểm ----- */
    function userPt(n) { for (var i = 0; i < st.objs.length; i++) if (st.objs[i].t === 'pt' && st.objs[i].name === n) return st.objs[i]; return null; }
    function P(n) { var u; return C.all[n] || ((u = userPt(n)) ? u.p : null); }
    function nm(arr) { return arr.join(''); }
    function emit(type, detail) { if (opt.onEvent) opt.onEvent({ type: type, detail: detail || {} }); }
    function changed() { if (opt.onChange) opt.onChange(st); }
    function commit() { draw(); changed(); }

    /* ----- đại lượng ẩn (PO-08) ----- */
    function hiddenList() {
      var src = opt.hidden || sc.asked || [], out = [];
      src.forEach(function (h) {
        var t, names;
        if (typeof h === 'string') { t = h.split(':')[0]; names = (h.split(':')[1] || '').split('-'); }
        else { t = h.t; names = h.pts || []; }
        var ps = names.map(P);
        if (ps.some(function (p) { return !p; })) return;
        if (t === 'len' && ps.length === 2) out.push({ t: 'len', a: ps[0], b: ps[1] });
        if (t === 'ang' && ps.length === 3) out.push({ t: 'ang', d1: unit(sub(ps[0], ps[1])), d2: unit(sub(ps[2], ps[1])) });
      });
      return out;
    }
    function par(u, w) { return len(cross(u, w)) < 1e-6; }
    function same(p, q) { return len(sub(p, q)) < C.tol; }
    function lenHidden(A, B) {
      return hiddenList().some(function (h) { return h.t === 'len' && ((same(h.a, A) && same(h.b, B)) || (same(h.a, B) && same(h.b, A))); });
    }
    function angHidden(u1, u2) {
      return hiddenList().some(function (h) { return h.t === 'ang' && ((par(u1, h.d1) && par(u2, h.d2)) || (par(u1, h.d2) && par(u2, h.d1))); });
    }
    function lenInfo(a, b) {
      var A = P(a), B = P(b), name = nm([a, b]);
      if (lenHidden(A, B)) return { kind: 'len', name: name, hidden: true, text: name + ': ' + HIDDEN_TEXT };
      var L = len(sub(A, B)), v = (sc.sym && sc.sym[keyLen(a, b)]) || fmtLen(L, sc.unit);
      return { kind: 'len', name: name, hidden: false, value: L, valueText: v, text: name + ' = ' + v };
    }
    function angInfo(s1, s2) {
      var o = null, a, b, name, deg, u1, u2;
      [[0, 0], [0, 1], [1, 0], [1, 1]].forEach(function (ix) { if (!o && s1[ix[0]] === s2[ix[1]]) { o = s1[ix[0]]; a = s1[1 - ix[0]]; b = s2[1 - ix[1]]; } });
      if (o) {
        u1 = unit(sub(P(a), P(o))); u2 = unit(sub(P(b), P(o)));
        deg = Math.acos(clamp(dot(u1, u2), -1, 1)) / DEG; name = '∠' + nm([a, o, b]);
      } else {
        u1 = unit(sub(P(s1[1]), P(s1[0]))); u2 = unit(sub(P(s2[1]), P(s2[0])));
        deg = Math.acos(clamp(Math.abs(dot(u1, u2)), 0, 1)) / DEG; name = '(' + nm(s1) + ', ' + nm(s2) + ')';
      }
      if (angHidden(u1, u2)) return { kind: 'ang', name: name, hidden: true, text: name + ': ' + HIDDEN_TEXT, o: o, a: a, b: b };
      return { kind: 'ang', name: name, hidden: false, value: deg, valueText: fmtAng(deg), text: name + ' = ' + fmtAng(deg), o: o, a: a, b: b };
    }

    /* ----- góc nhìn ----- */
    function wrap(y) { y = ((y + 180) % 360 + 360) % 360 - 180; return y; }
    function curView() {
      var a = st.va;
      if (a) {
        var k = (Date.now() - a.t0) / a.d;
        if (k >= 1 || opt.animate === false) { st.yaw = wrap(a.to[0]); st.pitch = a.to[1]; st.zoom = a.to[2]; st.va = null; }
        else {
          k = k < 0 ? 0 : k; k = k * k * (3 - 2 * k);
          return { yaw: a.from[0] + wrap(a.to[0] - a.from[0]) * k, pitch: a.from[1] + (a.to[1] - a.from[1]) * k, zoom: a.from[2] + (a.to[2] - a.from[2]) * k };
        }
      }
      return { yaw: st.yaw, pitch: st.pitch, zoom: st.zoom };
    }
    function freeze() { var v = curView(); st.yaw = wrap(v.yaw); st.pitch = v.pitch; st.zoom = v.zoom; st.va = null; }
    function goView(y, p, z) {
      var v = curView();
      y = wrap(y); p = clamp(p, -90, 90); z = clamp(z, 0.5, 4);
      if (opt.animate === false) { st.yaw = y; st.pitch = p; st.zoom = z; st.va = null; }
      else { st.va = { from: [v.yaw, v.pitch, v.zoom], to: [y, p, z], t0: Date.now(), d: 480 }; loop(); }
    }
    function loop() {
      if (looping || dead) return;
      looping = true;
      (function tick() {
        if (dead || !svg.isConnected) { looping = false; return; }
        draw();
        if (st.va) requestAnimationFrame(tick); else looping = false;
      })();
    }
    function later() { if (!raf && !dead) raf = requestAnimationFrame(function () { raf = 0; if (!dead) draw(); }); }

    /* ----- danh sách đối tượng đang hiện ----- */
    function items() {
      var out = [], k, steps = sc.steps || [];
      for (k = 1; k <= Math.min(st.step, steps.length); k++) {
        var s = steps[k - 1], cur = k === st.step, n = 0;
        (s.hl || []).forEach(function (h, i) {
          var o = { t: 'hl' }, key; for (key in h) o[key] = h[key];
          out.push({ o: o, id: 's' + k + 'h' + i, cur: cur, old: !cur, n: n++ });
        });
        (s.add || []).forEach(function (o, i) { out.push({ o: o, id: 's' + k + 'a' + i, cur: cur, old: !cur, n: n++ }); });
      }
      st.objs.forEach(function (o) { out.push({ o: o, id: o.id, user: true }); });
      return out;
    }

    /* ====================== VẼ ====================== */
    function draw() {
      if (dead) return;
      W = box.clientWidth || 400; H = box.clientHeight || 320;
      var v = curView(), yw = v.yaw * DEG, pt = v.pitch * DEG;
      var cy = Math.cos(yw), sy = Math.sin(yw), cp = Math.cos(pt), sp = Math.sin(pt);
      var RIGHT = [cy, sy, 0], UP = [-sy * sp, cy * sp, cp], V = [sy * cp, -cy * cp, sp];
      var S = Math.min(W, H) * 0.5 * 0.72 / C.R * v.zoom, cx = W / 2, cyy = H / 2 + (hudEl ? 8 : 0);
      cam = { S: S };
      function pr(p) { var d = sub(p, C.center); return [cx + S * dot(d, RIGHT), cyy - S * dot(d, UP)]; }
      function occ(p, tag) {
        for (var i = 0; i < C.solids.length; i++) {
          var so = C.solids[i];
          if ((so.only || '') !== (tag || '')) continue;
          if (so.sphere) {
            var d = sub(p, so.sphere.c), b = dot(d, V), c0 = dot(d, d) - so.sphere.r * so.sphere.r, disc = b * b - c0;
            if (disc > 0 && (-b + Math.sqrt(disc)) - Math.max(-b - Math.sqrt(disc), C.tol) > C.tol) return true;
            continue;
          }
          var tmin = C.tol, tmax = 1e9, ok = true;
          for (var j = 0; j < so.planes.length; j++) {
            var plj = so.planes[j], s = plj.d - dot(plj.n, p), q = dot(plj.n, V);
            if (Math.abs(q) < 1e-9) { if (s < C.tol) { ok = false; break; } }
            else if (q > 0) tmax = Math.min(tmax, s / q);
            else tmin = Math.max(tmin, s / q);
            if (tmax - tmin <= C.tol) { ok = false; break; }
          }
          if (ok) return true;
        }
        return false;
      }
      function runs(A, B, tag) {
        var N = 24, cls = [], i, out = [], t0 = 0;
        for (i = 0; i < N; i++) cls.push(occ(mix(A, B, (i + 0.5) / N), tag));
        for (i = 1; i < N; i++) if (cls[i] !== cls[i - 1]) {
          var lo = (i - 0.5) / N, hi = (i + 0.5) / N;
          for (var j = 0; j < 10; j++) { var m = (lo + hi) / 2; if (occ(mix(A, B, m), tag) === cls[i - 1]) lo = m; else hi = m; }
          out.push({ t0: t0, t1: lo, h: cls[i - 1] }); t0 = lo;
        }
        out.push({ t0: t0, t1: 1, h: cls[N - 1] });
        return out;
      }

      var now = Date.now(), animOn = opt.animate !== false && st.anim && st.anim.step === st.step && now - st.anim.t0 < 4000;
      function an(it, kind) {
        if (!animOn || !it || !it.cur) return '';
        var dl = Math.round(it.n * 140 - (now - st.anim.t0));
        return (kind === 'draw' ? ' pathLength="1"' : '') + ' class="f3-' + kind + '" style="animation-delay:' + dl + 'ms"';
      }
      var Lfill = [], Lhid = [], Lline = [], Lmark = [], Lpt = [], labels = [], obst = [];
      function lineSvg(p, q, c, w, hid, extra) {
        return '<line x1="' + f1(p[0]) + '" y1="' + f1(p[1]) + '" x2="' + f1(q[0]) + '" y2="' + f1(q[1]) + '" stroke="' + c +
          '" stroke-width="' + (hid ? +(w * 0.72).toFixed(2) : w) + '" stroke-linecap="round"' + (hid ? ' stroke-dasharray="6 5"' : '') + (extra || '') + '/>';
      }
      /* đoạn thẳng có xét che khuất; o: {c,w,dash,op,it,data,noocc} */
      function seg3(A, B, o) {
        var rs = (o.dash || o.noocc) ? [{ t0: 0, t1: 1, h: !!o.dash }] : runs(A, B, o.tag);
        rs.forEach(function (r) {
          if (r.t1 - r.t0 < 1e-4) return;
          var p = pr(mix(A, B, r.t0)), q = pr(mix(A, B, r.t1));
          var ex = (o.op != null && o.op < 1 ? ' opacity="' + o.op + '"' : '') + (o.data || '') + (o.data ? ' data-h="' + (r.h ? 1 : 0) + '"' : '') + an(o.it, r.h ? 'fade' : 'draw');
          (r.h ? Lhid : Lline).push(lineSvg(p, q, o.c, o.w, r.h, ex));
        });
      }
      function poly(ps, fill, fo, stroke, sw, dash, extra) {
        return '<polygon points="' + ps.map(function (p) { return f1(p[0]) + ',' + f1(p[1]); }).join(' ') + '" fill="' + fill + '" fill-opacity="' + fo + '"' +
          (stroke ? ' stroke="' + stroke + '" stroke-width="' + sw + '" stroke-linejoin="round"' + (dash ? ' stroke-dasharray="7 5"' : '') : '') + (extra || '') + '/>';
      }
      function hull(ps) {
        ps = ps.slice().sort(function (a, b) { return a[0] - b[0] || a[1] - b[1]; });
        function cr(o, a, b) { return (a[0] - o[0]) * (b[1] - o[1]) - (a[1] - o[1]) * (b[0] - o[0]); }
        var lo = [], up = [], i;
        for (i = 0; i < ps.length; i++) { while (lo.length >= 2 && cr(lo[lo.length - 2], lo[lo.length - 1], ps[i]) <= 0) lo.pop(); lo.push(ps[i]); }
        for (i = ps.length - 1; i >= 0; i--) { while (up.length >= 2 && cr(up[up.length - 2], up[up.length - 1], ps[i]) <= 0) up.pop(); up.push(ps[i]); }
        lo.pop(); up.pop();
        return lo.concat(up);
      }
      function circ3(cv, n) { var a = []; for (var i = 0; i < n; i++) a.push(circlePt(cv, i * 2 * Math.PI / n)); return a; }
      function curveDraw(cv, c, w, op, it) {
        var n = 72, cls = [], i, start = 0;
        for (i = 0; i < n; i++) cls.push(occ(circlePt(cv, (i + 0.5) * 2 * Math.PI / n), cv.tag));
        for (i = 0; i < n; i++) if (cls[i] !== cls[(i + n - 1) % n]) { start = i; break; }
        i = 0;
        while (i < n) {
          var j = i, h = cls[(start + i) % n], d = '';
          while (j < n && cls[(start + j) % n] === h) j++;
          for (var k = i; k <= j; k++) { var p = pr(circlePt(cv, (start + k) * 2 * Math.PI / n)); d += (k === i ? 'M' : 'L') + f1(p[0]) + ' ' + f1(p[1]); }
          (h ? Lhid : Lline).push('<path d="' + d + '" fill="none" stroke="' + c + '" stroke-width="' + (h ? +(w * 0.72).toFixed(2) : w) + '" stroke-linecap="round"' +
            (h ? ' stroke-dasharray="6 5"' : '') + (op < 1 ? ' opacity="' + op + '"' : '') + ' data-curve="' + cv.id + '" data-h="' + (h ? 1 : 0) + '"' + an(it, 'fade') + '/>');
          i = j;
        }
      }
      function curveById(id) { return (sc.curves || []).filter(function (c) { return c.id === id; })[0]; }
      function surfHull() {
        var ps = [];
        if (sc.cone) { ps = circ3(sc.cone, 72); ps.push(sc.cone.apex); }
        if (sc.cyl) ps = circ3(sc.cyl, 72).concat(circ3({ c: add(sc.cyl.c, [0, 0, sc.cyl.h]), r: sc.cyl.r }, 72));
        return hull(ps.map(pr));
      }

      /* ----- thân hình gốc ----- */
      var screenC = pr(C.center);
      (sc.faces || []).forEach(function (f, i) {
        var front = dot(C.faceN[i], V) > 1e-9;
        Lfill.push(poly(f.map(function (n) { return pr(sc.pts[n]); }), PRI, front ? 0.09 : 0.045, null, 0, 0, ' data-f="' + f.join('-') + '"'));
      });
      if (sc.cone || sc.cyl) Lfill.push(poly(surfHull(), PRI, 0.1));
      if (sc.sphere) {
        var sp0 = pr(sc.sphere.c), sr = sc.sphere.r * S;
        Lfill.push('<circle cx="' + f1(sp0[0]) + '" cy="' + f1(sp0[1]) + '" r="' + f1(sr) + '" fill="' + PRI + '" fill-opacity="0.05" stroke="' + PRI + '" stroke-width="1.8"/>');
      }
      (sc.curves || []).forEach(function (cv) { curveDraw(cv, cv.tag === 'sphere' ? PRI : INK, cv.tag === 'sphere' ? 1.5 : 2.2, 1); });
      if (sc.cone || sc.cyl) {
        var hx = V[0], hy = V[1], hl = Math.sqrt(hx * hx + hy * hy), beta = Math.atan2(hy, hx), body = sc.cone || sc.cyl, dA = null;
        if (hl > 1e-6) {
          if (sc.cyl) dA = Math.PI / 2;
          else { var cc = -body.r * V[2] / ((body.apex[2] - body.c[2]) * hl); if (Math.abs(cc) < 1) dA = Math.acos(cc); }
        }
        if (dA != null) [beta + dA, beta - dA].forEach(function (a) {
          var b0 = circlePt(body, a), b1 = sc.cone ? body.apex : add(b0, [0, 0, body.h]);
          Lline.push(lineSvg(pr(b0), pr(b1), INK, 2.2, false, ' data-sil="1"'));
        });
      }
      hit = { pts: [], segs: [], faces: [], pills: [] };
      (sc.edges || []).forEach(function (e) {
        var A = sc.pts[e[0]], B = sc.pts[e[1]];
        seg3(A, B, { c: INK, w: 2.2, data: ' data-e="' + e[0] + '-' + e[1] + '"' });
        hit.segs.push({ a: e[0], b: e[1], p: pr(A), q: pr(B) });
      });
      (sc.faces || []).forEach(function (f, i) {
        hit.faces.push({ pts: f, poly: f.map(function (n) { return pr(sc.pts[n]); }), front: dot(C.faceN[i], V) > 1e-9, base: true });
      });

      /* ----- điểm: thu thập để vẽ sau cùng ----- */
      var pts = [];
      Object.keys(sc.pts).forEach(function (n) { pts.push({ name: n, p: sc.pts[n], c: INK, r: 3.4 }); });

      /* ----- đối tượng vẽ thêm (bước + người dùng) ----- */
      function colOf(it, d) { return it.o.c ? COL[it.o.c] : (it.user ? d : ACC); }
      function opOf(it) { return it.old ? 0.45 : 1; }
      function segLabel(A, B, text, c, it, pill, id) {
        var p = pr(A), q = pr(B), m = [(p[0] + q[0]) / 2, (p[1] + q[1]) / 2], dx = q[0] - p[0], dy = q[1] - p[1], l = Math.sqrt(dx * dx + dy * dy) || 1;
        var nx = -dy / l, ny = dx / l;
        if (nx * (m[0] - screenC[0]) + ny * (m[1] - screenC[1]) < 0) { nx = -nx; ny = -ny; }
        labels.push({ x: m[0], y: m[1], dx: nx, dy: ny, d: 7, text: text, c: c, size: 13.5, it: it, pill: pill, id: id, op: it ? opOf(it) : 1, alt: 1 });
      }
      function angDraw(it, A, O, B, text, c, pill) {
        var u = unit(sub(A, O)), w = unit(sub(B, O)), ang = Math.acos(clamp(dot(u, w), -1, 1));
        var rho = Math.min(Math.min(len(sub(A, O)), len(sub(B, O))) * 0.26, C.R * 0.24), o2 = pr(O), ps, d, op = opOf(it);
        var ex = (op < 1 ? ' opacity="' + op + '"' : '') + ' data-o="' + it.id + '"' + an(it, 'fade');
        if (Math.abs(ang - Math.PI / 2) < 1e-6) {
          var q = rho * 0.62;
          ps = [pr(add(O, mul(u, q))), pr(add(O, add(mul(u, q), mul(w, q)))), pr(add(O, mul(w, q)))];
          d = 'M' + ps.map(function (p) { return f1(p[0]) + ' ' + f1(p[1]); }).join('L');
        } else {
          var e = unit(sub(w, mul(u, dot(u, w))));
          ps = [];
          for (var i = 0; i <= 16; i++) { var t = ang * i / 16; ps.push(pr(add(O, add(mul(u, rho * Math.cos(t)), mul(e, rho * Math.sin(t)))))); }
          d = 'M' + ps.map(function (p) { return f1(p[0]) + ' ' + f1(p[1]); }).join('L');
        }
        Lmark.push('<path d="M' + f1(o2[0]) + ' ' + f1(o2[1]) + 'L' + d.slice(1) + 'Z" fill="' + c + '" fill-opacity="0.18"' + ex + '/>');
        Lmark.push('<path d="' + d + '" fill="none" stroke="' + c + '" stroke-width="2.2" stroke-linejoin="round" stroke-linecap="round"' + ex + '/>');
        if (text) {
          var bis = unit(add(u, w)), a2 = pr(add(O, mul(bis, rho))), dx = a2[0] - o2[0], dy = a2[1] - o2[1], l = Math.sqrt(dx * dx + dy * dy) || 1;
          labels.push({ x: a2[0], y: a2[1], dx: dx / l, dy: dy / l, d: 6, text: text, c: c, size: 14, it: it, pill: pill, id: it.id, op: op });
        }
      }
      function selGlow(it) { return it.user && st.sel === it.id; }

      items().forEach(function (it) {
        var o = it.o, c, A, B, ps, op = opOf(it), dat = ' data-o="' + it.id + '"';
        if (o.t === 'pt') { pts.push({ name: o.name, p: o.p, c: colOf(it, VIO), r: 4.6, it: it, user: it.user, id: it.id }); return; }
        if (o.t === 'seg') {
          A = P(o.a); B = P(o.b); if (!A || !B) return; c = colOf(it, VIO);
          if (selGlow(it)) Lfill.push(lineSvg(pr(A), pr(B), c, 12, false, ' opacity="0.25"'));
          seg3(A, B, { c: c, w: o.w || 2.6, dash: o.dash, op: op, it: it, data: dat });
          hit.segs.push({ a: o.a, b: o.b, p: pr(A), q: pr(B), id: it.user ? it.id : null });
          if (o.text) segLabel(A, B, o.text, c, it);
          return;
        }
        if (o.t === 'plane' || o.t === 'grow') {
          ps = o.pts.map(P); if (ps.some(function (p) { return !p; })) return;
          if (o.t === 'grow') {
            var g = [0, 0, 0]; ps.forEach(function (p) { g = add(g, p); }); g = mul(g, 1 / ps.length);
            ps = ps.map(function (p) { return add(g, mul(sub(p, g), o.k || 1.7)); });
          }
          c = colOf(it, o.t === 'grow' ? GRN : VIO);
          var pp = ps.map(pr);
          Lfill.push(poly(pp, c, (o.t === 'grow' ? 0.11 : 0.16) * (selGlow(it) ? 2 : 1), c, selGlow(it) ? 3 : 1.7, o.t === 'grow', dat + an(it, 'fade')));
          hit.faces.push({ pts: o.pts, poly: pp, id: it.user ? it.id : null, kind: o.t });
          return;
        }
        if (o.t === 'ext') {
          A = P(o.a); B = P(o.b); if (!A || !B) return; c = colOf(it, GRN);
          var dv = mul(sub(B, A), o.k || 0.6), A2 = sub(A, dv), B2 = add(B, dv);
          if (selGlow(it)) { Lfill.push(lineSvg(pr(A2), pr(A), c, 12, false, ' opacity="0.25"')); Lfill.push(lineSvg(pr(B), pr(B2), c, 12, false, ' opacity="0.25"')); }
          seg3(A, A2, { c: c, w: 2.4, dash: true, it: it, data: dat });
          seg3(B, B2, { c: c, w: 2.4, dash: true, it: it, data: dat });
          hit.segs.push({ a: o.a, b: o.b, p: pr(A2), q: pr(A), id: it.id, ext: true });
          hit.segs.push({ a: o.a, b: o.b, p: pr(B), q: pr(B2), id: it.id, ext: true });
          return;
        }
        if (o.t === 'ang') {
          A = P(o.a); B = P(o.b); var O = P(o.o); if (!A || !B || !O) return;
          angDraw(it, A, O, B, o.text, colOf(it, ACC));
          if (it.user) { var ao = pr(O); hit.pills.push({ id: it.id, box: [ao[0] - 16, ao[1] - 16, ao[0] + 16, ao[1] + 16] }); }
          return;
        }
        if (o.t === 'hl') {
          c = colOf(it, ACC);
          if (o.edge) {
            A = P(o.edge[0]); B = P(o.edge[1]); if (!A || !B) return;
            seg3(A, B, { c: c, w: 4.6, op: op, it: it, data: dat });
            if (it.user) hit.segs.push({ a: o.edge[0], b: o.edge[1], p: pr(A), q: pr(B), id: it.id, hl: true });
            if (o.text) segLabel(A, B, o.text, c, it);
          } else if (o.face) {
            ps = o.face.map(P); if (ps.some(function (p) { return !p; })) return;
            Lfill.push(poly(ps.map(pr), c, 0.24 * op, c, 1.4, false, dat + an(it, 'fade')));
          } else if (o.disc) {
            var cv = curveById(o.disc); if (!cv) return;
            Lfill.push(poly(circ3(cv, 72).map(pr), c, 0.26 * op, null, 0, false, dat + an(it, 'fade')));
            curveDraw(cv, c, 3.4, op, it);
          } else if (o.surf) {
            Lfill.push(poly(surfHull(), c, 0.2 * op, null, 0, false, dat + an(it, 'fade')));
          }
          return;
        }
        if (o.t === 'mlen') {
          A = P(o.a); B = P(o.b); if (!A || !B) return;
          var li = lenInfo(o.a, o.b);
          Lfill.push(lineSvg(pr(A), pr(B), GRN, selGlow(it) ? 12 : 7, false, ' opacity="0.28"' + dat));
          segLabel(A, B, li.text, GRN, null, true, it.id);
          return;
        }
        if (o.t === 'mang') {
          var s1 = o.s1.map(P), s2 = o.s2.map(P); if (s1.concat(s2).some(function (p) { return !p; })) return;
          var ai = angInfo(o.s1, o.s2);
          if (ai.o) angDraw(it, P(ai.a), P(ai.o), P(ai.b), ai.text, GRN, true);
          else {
            var m1 = pr(mix(s1[0], s1[1], 0.5)), m2 = pr(mix(s2[0], s2[1], 0.5));
            Lfill.push(lineSvg(pr(s1[0]), pr(s1[1]), GRN, 7, false, ' opacity="0.28"' + dat));
            Lfill.push(lineSvg(pr(s2[0]), pr(s2[1]), GRN, 7, false, ' opacity="0.28"'));
            labels.push({ x: (m1[0] + m2[0]) / 2, y: (m1[1] + m2[1]) / 2, dx: 0, dy: -1, d: 0, text: ai.text, c: GRN, size: 13.5, pill: true, id: it.id });
          }
          return;
        }
      });

      /* ----- điểm và nhãn điểm ----- */
      var inc = {};
      hit.segs.forEach(function (s) {
        if (s.ext || s.hl) return;
        var dx = s.q[0] - s.p[0], dy = s.q[1] - s.p[1], l = Math.sqrt(dx * dx + dy * dy);
        if (l < 2) return;
        (inc[s.a] = inc[s.a] || [0, 0])[0] += dx / l; inc[s.a][1] += dy / l;
        (inc[s.b] = inc[s.b] || [0, 0])[0] -= dx / l; inc[s.b][1] -= dy / l;
      });
      var ptLabels = [];
      pts.forEach(function (q) {
        var p = pr(q.p), op = q.it ? opOf(q.it) : 1;
        obst.push([p[0] - 5, p[1] - 5, p[0] + 5, p[1] + 5]);
        if (q.user && st.sel === q.id) Lpt.push('<circle cx="' + f1(p[0]) + '" cy="' + f1(p[1]) + '" r="11" fill="' + q.c + '" fill-opacity="0.25"/>');
        Lpt.push('<circle cx="' + f1(p[0]) + '" cy="' + f1(p[1]) + '" r="' + q.r + '" fill="' + q.c + '" stroke="#fff" stroke-width="1.4" data-pt="' + esc(q.name) + '"' +
          (q.id ? ' data-o="' + q.id + '"' : '') + (op < 1 ? ' opacity="' + op + '"' : '') + an(q.it, 'fade') + '/>');
        hit.pts.push({ name: q.name, x: p[0], y: p[1], id: q.user ? q.id : null });
        var v0 = inc[q.name], dx, dy, l;
        if (v0 && (l = Math.sqrt(v0[0] * v0[0] + v0[1] * v0[1])) > 0.35) { dx = -v0[0] / l; dy = -v0[1] / l; }
        else {
          dx = p[0] - screenC[0]; dy = p[1] - screenC[1]; l = Math.sqrt(dx * dx + dy * dy);
          if (l > 6) { dx /= l; dy /= l; } else { dx = 0.7; dy = -0.7; }
        }
        ptLabels.push({ x: p[0], y: p[1], dx: dx, dy: dy, d: 9, text: q.name, c: q.c, size: 16, it: q.it, op: op, bold: 1 });
      });
      labels = ptLabels.concat(labels);

      /* ----- điểm / đoạn đang chọn dở ----- */
      st.pick.forEach(function (k) {
        if (k.pt && P(k.pt)) { var p = pr(P(k.pt)); Lpt.push('<circle cx="' + f1(p[0]) + '" cy="' + f1(p[1]) + '" r="11" fill="none" stroke="' + ACC + '" stroke-width="2.4" data-pick="1"/>'); }
        if (k.seg && P(k.seg[0]) && P(k.seg[1])) Lfill.push(lineSvg(pr(P(k.seg[0])), pr(P(k.seg[1])), ACC, 11, false, ' opacity="0.3" data-pick="1"'));
      });

      /* ----- xếp nhãn: tránh đè điểm và đè nhau ----- */
      var placed = [], Llab = [];
      function tw(t, size) { return t.length * size * 0.6 + 2; }
      function hitBox(a, b) { return a[0] < b[2] && b[0] < a[2] && a[1] < b[3] && b[1] < a[3]; }
      labels.forEach(function (lb) {
        var w = tw(lb.text, lb.size) + (lb.pill ? 12 : 0), h = lb.size + (lb.pill ? 9 : 2), best = null;
        var angs = lb.alt ? [0, 180, 30, -30, 150, -150, 60, -60] : [0, 35, -35, 70, -70, 110, -110, 150, -150, 180];
        [lb.d, lb.d + 11, lb.d + 24].forEach(function (d, di) {
          angs.forEach(function (a, ai) {
            var ca = Math.cos(a * DEG), sa = Math.sin(a * DEG), dx = lb.dx * ca - lb.dy * sa, dy = lb.dx * sa + lb.dy * ca;
            var r = d + Math.abs(dx) * w / 2 + Math.abs(dy) * h / 2, x = lb.x + dx * r, y = lb.y + dy * r;
            var bx = [x - w / 2, y - h / 2, x + w / 2, y + h / 2], score = di * 0.35 + ai * 0.02, i;
            for (i = 0; i < placed.length; i++) if (hitBox(bx, placed[i])) score += 10;
            for (i = 0; i < obst.length; i++) if (hitBox(bx, obst[i])) score += 6;
            if (bx[0] < 2 || bx[1] < 2 || bx[2] > W - 2 || bx[3] > H - 2) score += 8;
            if (!best || score < best.s) best = { s: score, x: x, y: y, bx: bx };
          });
        });
        placed.push(best.bx);
        var ex = (lb.op != null && lb.op < 1 ? ' opacity="' + lb.op + '"' : '') + an(lb.it, 'fade');
        if (lb.pill) {
          Llab.push('<g' + ex + ' data-pill="' + (lb.id || '') + '"><rect x="' + f1(best.bx[0]) + '" y="' + f1(best.bx[1]) + '" width="' + f1(w) + '" height="' + f1(h) + '" rx="' + f1(h / 2) +
            '" fill="#fff" stroke="' + lb.c + '" stroke-width="' + (st.sel && st.sel === lb.id ? 2.6 : 1.3) + '"/><text x="' + f1(best.x) + '" y="' + f1(best.y) + '" text-anchor="middle" dominant-baseline="central" font-size="' + lb.size +
            '" font-weight="700" fill="' + lb.c + '">' + esc(lb.text) + '</text></g>');
          if (lb.id) hit.pills.push({ id: lb.id, box: best.bx });
        } else {
          Llab.push('<text x="' + f1(best.x) + '" y="' + f1(best.y) + '" text-anchor="middle" dominant-baseline="central" font-size="' + lb.size + '" font-weight="' + (lb.bold ? 800 : 700) +
            '" fill="' + lb.c + '" stroke="#fff" stroke-width="3.6" stroke-linejoin="round" paint-order="stroke"' + (lb.bold ? ' data-label="' + esc(lb.text) + '"' : '') + ex + '>' + esc(lb.text) + '</text>');
        }
      });

      svg.setAttribute('viewBox', '0 0 ' + W + ' ' + H);
      svg.innerHTML = '<g class="f3-fill">' + Lfill.join('') + '</g><g class="f3-hid">' + Lhid.join('') + '</g><g class="f3-line">' + Lline.join('') +
        '</g><g class="f3-mark">' + Lmark.join('') + '</g><g class="f3-pt">' + Lpt.join('') + '</g><g class="f3-lab">' + Llab.join('') + '</g>';
      drawHud();
      if (st.va) loop();
    }

    /* ----- thanh gợi ý trong khung hình ----- */
    function objName(o) {
      if (o.t === 'pt') return 'điểm ' + o.name;
      if (o.t === 'seg') return 'đoạn ' + nm([o.a, o.b]);
      if (o.t === 'plane') return 'mặt phẳng (' + nm(o.pts) + ')';
      if (o.t === 'ext') return 'phần kéo dài ' + nm([o.a, o.b]);
      if (o.t === 'grow') return 'phần mở rộng (' + nm(o.pts) + ')';
      if (o.t === 'ang') return 'ký hiệu góc ' + nm([o.a, o.o, o.b]);
      if (o.t === 'hl') return o.edge ? 'phần tô ' + nm(o.edge) : 'phần tô (' + nm(o.face) + ')';
      if (o.t === 'mlen') return 'số đo ' + nm([o.a, o.b]);
      if (o.t === 'mang') return 'số đo góc';
      return 'đối tượng';
    }
    function pickNames() { return st.pick.map(function (k) { return k.pt || nm(k.seg); }).join(', '); }
    function drawHud() {
      if (!hudEl) return;
      var t = st.tool, h = '', chips = '', np = st.pick.length;
      function chip(act, label, on) { return '<button type="button" class="f3-chip' + (on ? ' on' : '') + '" data-f3="' + act + '">' + label + '</button>'; }
      if (t === 'select') {
        h = st.objs.length ? 'Chạm cạnh hoặc mặt để tô. Chạm phần vẽ thêm để chọn.' : 'Kéo để xoay. Chạm cạnh hoặc mặt để tô nổi bật.';
        var so = st.sel && st.objs.filter(function (o) { return o.id === st.sel; })[0];
        if (so) chips += chip('del', 'Xóa ' + esc(objName(so)), true);
        if (st.objs.length) chips += chip('clear', 'Xóa hết');
      } else if (t === 'point') {
        chips = chip('pm:mid', 'Trung điểm', st.ptMode === 'mid') + chip('pm:1:2', 'Chia 1 : 2', st.ptMode === '1:2') + chip('pm:1:3', 'Chia 1 : 3', st.ptMode === '1:3') + chip('pm:int', 'Giao điểm', st.ptMode === 'int');
        h = st.ptMode === 'int' ? (np ? 'Đã chọn ' + esc(pickNames()) + '. Chạm đoạn thứ hai.' : 'Chạm lần lượt hai đoạn cắt nhau.')
          : st.ptMode === 'mid' ? 'Chạm vào một cạnh để lấy trung điểm.' : 'Chạm vào cạnh, gần đầu mút lấy làm gốc chia.';
      } else if (t === 'segment') {
        chips = chip('dash:0', 'Nét liền', !st.dash) + chip('dash:1', 'Nét đứt', st.dash);
        h = np ? 'Đã chọn ' + esc(pickNames()) + '. Chạm điểm thứ hai.' : 'Chạm lần lượt hai điểm để nối.';
      } else if (t === 'plane') h = np ? 'Đã chọn ' + esc(pickNames()) + '. Chạm thêm ' + (3 - np) + ' điểm.' : 'Chạm lần lượt ba điểm để dựng mặt phẳng.';
      else if (t === 'extend') h = 'Chạm cạnh để kéo dài, chạm mặt để mở rộng.';
      else if (t === 'length') h = 'Chạm một đoạn để đo độ dài.';
      else if (t === 'angle') h = np ? 'Đã chọn ' + esc(pickNames()) + '. Chạm đoạn thứ hai.' : 'Chạm lần lượt hai đoạn để đo góc.';
      var html = '<span class="f3-hint">' + h + '</span>' + (chips ? '<span class="f3-chips">' + chips + '</span>' : '');
      if (html !== lastHud) { hudEl.innerHTML = html; lastHud = html; }
    }
    function onHud(e) {
      var b = e.target.closest ? e.target.closest('[data-f3]') : null;
      if (!b) return;
      var a = b.getAttribute('data-f3');
      if (a === 'del') api.remove(st.sel);
      else if (a === 'clear') api.clear();
      else if (a.indexOf('pm:') === 0) { st.ptMode = a.slice(3); st.pick = []; commit(); }
      else if (a.indexOf('dash:') === 0) { st.dash = a === 'dash:1'; commit(); }
    }

    /* ====================== THAO TÁC TRÊN HÌNH ====================== */
    function nearPt(x, y, userOnly) {
      var best = null, bd = 22;
      hit.pts.forEach(function (p) {
        if (userOnly && !p.id) return;
        var d = Math.sqrt((p.x - x) * (p.x - x) + (p.y - y) * (p.y - y));
        if (d < bd) { bd = d; best = p; }
      });
      return best;
    }
    function dSeg(x, y, s) {
      var dx = s.q[0] - s.p[0], dy = s.q[1] - s.p[1], l2 = dx * dx + dy * dy, t = l2 ? clamp(((x - s.p[0]) * dx + (y - s.p[1]) * dy) / l2, 0, 1) : 0;
      var ex = s.p[0] + t * dx - x, ey = s.p[1] + t * dy - y;
      return { d: Math.sqrt(ex * ex + ey * ey), t: t };
    }
    function nearSeg(x, y, filter) {
      var best = null, bd = 21;
      hit.segs.forEach(function (s) {
        if (filter && !filter(s)) return;
        var r = dSeg(x, y, s);
        if (r.d < bd - 0.01) { bd = r.d; best = { a: s.a, b: s.b, id: s.id, ext: s.ext, hl: s.hl, t: r.t, d: r.d }; }
      });
      return best;
    }
    function inPoly(x, y, ps) {
      var c = false;
      for (var i = 0, j = ps.length - 1; i < ps.length; j = i++)
        if ((ps[i][1] > y) !== (ps[j][1] > y) && x < (ps[j][0] - ps[i][0]) * (y - ps[i][1]) / (ps[j][1] - ps[i][1]) + ps[i][0]) c = !c;
      return c;
    }
    function facesAt(x, y) {
      var fs = hit.faces.filter(function (f) { return inPoly(x, y, f.poly); });
      return fs.filter(function (f) { return f.base && f.front; }).concat(fs.filter(function (f) { return !f.base; }), fs.filter(function (f) { return f.base && !f.front; }));
    }
    function sameSet(a, b) { return a.length === b.length && a.slice().sort().join('|') === b.slice().sort().join('|'); }
    function findObj(fn) { return st.objs.filter(fn)[0]; }
    function info(text) { emit('info', { text: text }); }

    function tap(x, y) {
      var t = st.tool, hp, hs, fs, i, o;
      if (t === 'select') {
        hp = nearPt(x, y, true);
        if (hp) return select(hp.id);
        for (i = 0; i < hit.pills.length; i++) { var b = hit.pills[i].box; if (x >= b[0] && x <= b[2] && y >= b[1] && y <= b[3]) return select(hit.pills[i].id); }
        fs = facesAt(x, y);
        hs = nearSeg(x, y, function (s) { return !!s.id; });
        if (hs && hs.hl) return api.remove(hs.id);
        if (hs) return select(hs.id);
        hs = nearSeg(x, y);
        if (hs && (hs.d <= NEAR || !fs.length)) return api.highlight({ edge: [hs.a, hs.b] });
        for (i = 0; i < fs.length; i++) if (!fs[i].base && fs[i].id) return select(fs[i].id);
        for (i = 0; i < fs.length; i++) {
          o = findObj(function (u) { return u.t === 'hl' && u.face && sameSet(u.face, fs[i].pts); });
          if (!o) return api.highlight({ face: fs[i].pts });
        }
        if (fs.length) { o = findObj(function (u) { return u.t === 'hl' && u.face && sameSet(u.face, fs[0].pts); }); if (o) return api.remove(o.id); }
        if (st.sel) { st.sel = null; commit(); }
        return;
      }
      if (t === 'point') {
        hs = nearSeg(x, y, function (s) { return !s.hl; });
        if (!hs) return info('Chạm vào một cạnh hoặc một đoạn');
        if (st.ptMode === 'int') {
          if (!st.pick.length) { st.pick = [{ seg: [hs.a, hs.b] }]; emit('pick', { text: 'Đã chọn ' + nm([hs.a, hs.b]) }); return commit(); }
          var s0 = st.pick[0].seg; st.pick = [];
          if (sameSet(s0, [hs.a, hs.b])) return commit();
          if (!api.addPoint({ int: [s0, [hs.a, hs.b]] })) commit();
          return;
        }
        if (st.ptMode === 'mid') return api.addPoint({ mid: [hs.a, hs.b] });
        var n = +st.ptMode.split(':')[1];
        return api.addPoint(hs.t <= 0.5 ? { ratio: [hs.a, hs.b], m: 1, n: n } : { ratio: [hs.b, hs.a], m: 1, n: n });
      }
      if (t === 'segment' || t === 'plane') {
        hp = nearPt(x, y);
        if (!hp) return info('Chạm vào một điểm');
        if (st.pick.some(function (k) { return k.pt === hp.name; })) { st.pick = st.pick.filter(function (k) { return k.pt !== hp.name; }); return commit(); }
        st.pick.push({ pt: hp.name });
        var need = t === 'segment' ? 2 : 3;
        if (st.pick.length < need) { emit('pick', { text: 'Đã chọn ' + pickNames() }); return commit(); }
        var ns = st.pick.map(function (k) { return k.pt; }); st.pick = [];
        if (!(t === 'segment' ? api.addSegment(ns[0], ns[1], { dash: st.dash }) : api.addPlane(ns[0], ns[1], ns[2]))) commit();
        return;
      }
      if (t === 'extend') {
        hs = nearSeg(x, y, function (s) { return !s.ext && !s.hl; });
        fs = facesAt(x, y).filter(function (f) { return f.kind !== 'grow'; });
        if (hs && (hs.d <= NEAR || !fs.length)) {
          o = findObj(function (u) { return u.t === 'ext' && sameSet([u.a, u.b], [hs.a, hs.b]); });
          return o ? api.remove(o.id) : api.extendEdge(hs.a, hs.b);
        }
        for (i = 0; i < fs.length; i++) if (!findObj(function (u) { return u.t === 'grow' && sameSet(u.pts, fs[i].pts); })) return api.growFace(fs[i].pts);
        if (fs.length) { o = findObj(function (u) { return u.t === 'grow' && sameSet(u.pts, fs[0].pts); }); return api.remove(o.id); }
        return info('Chạm vào một cạnh hoặc một mặt');
      }
      if (t === 'length') {
        hs = nearSeg(x, y, function (s) { return !s.ext && !s.hl; });
        if (!hs) return info('Chạm vào một đoạn để đo');
        return api.measureLength(hs.a, hs.b);
      }
      if (t === 'angle') {
        hs = nearSeg(x, y, function (s) { return !s.ext && !s.hl; });
        if (!hs) return info('Chạm vào một đoạn');
        if (!st.pick.length) { st.pick = [{ seg: [hs.a, hs.b] }]; emit('pick', { text: 'Đã chọn ' + nm([hs.a, hs.b]) }); return commit(); }
        var s1 = st.pick[0].seg; st.pick = [];
        if (sameSet(s1, [hs.a, hs.b])) return commit();
        api.measureAngle(s1, [hs.a, hs.b]);
      }
    }
    function select(id) {
      st.sel = st.sel === id ? null : id;
      var o = st.sel && findObj(function (u) { return u.id === id; });
      if (o) emit('select', { id: id, text: 'Đã chọn ' + objName(o) });
      commit();
    }

    /* ----- con trỏ: kéo để xoay, chụm để thu phóng, chạm để dùng công cụ ----- */
    var ptrs = {}, nPtr = 0, drag = null, pinch = null;
    /* tọa độ con trỏ quy về hệ điểm ảnh của khung hình; chia tỉ lệ khi khung bị thu phóng bằng CSS transform (khung iPad, iPhone của bản mẫu) */
    function pos(e) {
      var r = svg.getBoundingClientRect(), kx = r.width ? (box.clientWidth || r.width) / r.width : 1, ky = r.height ? (box.clientHeight || r.height) / r.height : 1;
      return [(e.clientX - r.left) * kx, (e.clientY - r.top) * ky];
    }
    function onDown(e) {
      if (e.pointerType === 'mouse' && e.button !== 0) return;
      try { svg.setPointerCapture(e.pointerId); } catch (x) { /* bỏ qua */ }
      ptrs[e.pointerId] = pos(e); nPtr = Object.keys(ptrs).length;
      if (nPtr === 1) { freeze(); drag = { id: e.pointerId, x0: ptrs[e.pointerId][0], y0: ptrs[e.pointerId][1], yaw: st.yaw, pitch: st.pitch, moved: false }; }
      else if (nPtr === 2) {
        var k = Object.keys(ptrs), a = ptrs[k[0]], b = ptrs[k[1]];
        pinch = { d: Math.max(10, Math.hypot(a[0] - b[0], a[1] - b[1])), zoom: st.zoom };
        if (drag) drag.moved = true;
      }
      e.preventDefault();
    }
    function onMove(e) {
      if (!ptrs[e.pointerId]) return;
      ptrs[e.pointerId] = pos(e);
      if (pinch && nPtr >= 2) {
        var k = Object.keys(ptrs), a = ptrs[k[0]], b = ptrs[k[1]];
        st.zoom = clamp(pinch.zoom * Math.hypot(a[0] - b[0], a[1] - b[1]) / pinch.d, 0.5, 4);
        return later();
      }
      if (drag && drag.id === e.pointerId && !pinch) {
        var dx = ptrs[e.pointerId][0] - drag.x0, dy = ptrs[e.pointerId][1] - drag.y0;
        if (!drag.moved && Math.abs(dx) + Math.abs(dy) < 7) return;
        drag.moved = true;
        st.yaw = wrap(drag.yaw - dx * 0.45); st.pitch = clamp(drag.pitch + dy * 0.45, -90, 90);
        later();
      }
    }
    function onUp(e) {
      if (!ptrs[e.pointerId]) return;
      var p = ptrs[e.pointerId];
      delete ptrs[e.pointerId]; nPtr = Object.keys(ptrs).length;
      if (pinch) {
        if (nPtr === 0) { pinch = null; drag = null; emit('view', { text: 'Thu phóng ' + dec(st.zoom, 1) + '×', yaw: st.yaw, pitch: st.pitch, zoom: st.zoom }); commit(); }
        return;
      }
      if (drag && drag.id === e.pointerId) {
        var d = drag; drag = null;
        if (e.type === 'pointercancel') return commit();
        if (!d.moved) return tap(p[0], p[1]);
        emit('view', { text: 'Đã xoay hình', yaw: st.yaw, pitch: st.pitch, zoom: st.zoom });
        commit();
      }
    }
    function onWheel(e) {
      e.preventDefault();
      freeze();
      st.zoom = clamp(st.zoom * Math.exp(-e.deltaY * 0.0015), 0.5, 4);
      later();
      clearTimeout(wheelT);
      wheelT = setTimeout(function () { if (!dead) { emit('view', { text: 'Thu phóng ' + dec(st.zoom, 1) + '×', yaw: st.yaw, pitch: st.pitch, zoom: st.zoom }); changed(); } }, 220);
    }
    svg.addEventListener('pointerdown', onDown);
    svg.addEventListener('pointermove', onMove);
    svg.addEventListener('pointerup', onUp);
    svg.addEventListener('pointercancel', onUp);
    svg.addEventListener('wheel', onWheel, { passive: false });
    if (hudEl) hudEl.addEventListener('click', onHud);
    var ro = typeof ResizeObserver !== 'undefined' ? new ResizeObserver(function () { if (box.clientWidth !== W || box.clientHeight !== H) later(); }) : null;
    if (ro) ro.observe(box);

    /* ====================== API ====================== */
    function nextName() {
      for (var r = 0; r < 9; r++) for (var i = 0; i < NAMES.length; i++) { var n = NAMES[i] + (r ? r : ''); if (!P(n)) return n; }
      return 'X' + st.seq;
    }
    function push(o, text) {
      o.id = 'u' + (st.seq++);
      st.objs.push(o);
      emit('add', { obj: o, id: o.id, text: text });
      commit();
      return o;
    }
    function need() { for (var i = 0; i < arguments.length; i++) if (!P(arguments[i])) { info('Không có điểm ' + arguments[i]); return false; } return true; }
    function visiblePtAt(p) {
      var r = null;
      hit.pts.forEach(function (h) { if (!r && same(P(h.name), p)) r = h.name; });
      return r;
    }
    var api = {
      state: st,
      setTool: function (id) {
        if (!TOOLS.some(function (t) { return t[0] === id; })) return;
        st.tool = id; st.pick = []; st.sel = null;
        emit('tool', { tool: id, text: 'Công cụ: ' + TOOLS.filter(function (t) { return t[0] === id; })[0][1] });
        commit();
      },
      stepCount: function () { return (sc.steps || []).length; },
      setStep: function (n) {
        n = clamp(Math.round(n) || 0, 0, api.stepCount());
        st.step = n; st.anim = n ? { step: n, t0: Date.now() } : null;
        var s = n ? sc.steps[n - 1] : null;
        if (opt.autoView !== false && s && s.view) goView(s.view[0], s.view[1], st.zoom);
        emit('step', { step: n, title: s ? s.title : '', text: n ? 'Bước ' + n + ': ' + s.title : 'Hình gốc' });
        commit();
      },
      setView: function (v) {
        var y, p, z = curView().zoom, name = typeof v === 'string' ? v : null;
        if (name === 'default') { y = sc.view[0]; p = sc.view[1]; }
        else if (name && VIEWS[name]) { y = VIEWS[name][0]; p = VIEWS[name][1]; }
        else if (v && typeof v === 'object') { var c = curView(); y = v.yaw == null ? c.yaw : v.yaw; p = v.pitch == null ? c.pitch : v.pitch; z = v.zoom == null ? z : v.zoom; }
        else return;
        goView(y, p, z);
        emit('view', { view: name, text: name ? 'Góc nhìn ' + VIEW_NAMES[name] : 'Đã đổi góc nhìn' });
        commit();
      },
      resetView: function () {
        goView(sc.view[0], sc.view[1], 1);
        emit('view', { view: 'default', text: 'Đã đặt lại góc nhìn' });
        commit();
      },
      zoomBy: function (k) { var v = curView(); goView(v.yaw, v.pitch, v.zoom * k); commit(); },
      addPoint: function (def, name) {
        var p, text, a, b, n;
        if (def.mid) {
          a = def.mid[0]; b = def.mid[1]; if (!need(a, b)) return null;
          p = mix(P(a), P(b), 0.5); text = ' là trung điểm ' + nm([a, b]);
        } else if (def.ratio) {
          a = def.ratio[0]; b = def.ratio[1]; if (!need(a, b)) return null;
          var m = def.m || 1, k = def.n || 1;
          p = mix(P(a), P(b), m / (m + k)); text = null;
        } else if (def.int) {
          var s1 = def.int[0], s2 = def.int[1]; if (!need(s1[0], s1[1], s2[0], s2[1])) return null;
          var p1 = P(s1[0]), d1 = sub(P(s1[1]), p1), p2 = P(s2[0]), d2 = sub(P(s2[1]), p2), nn = cross(d1, d2), w = sub(p2, p1);
          if (len(nn) < 1e-9 * (len(d1) * len(d2) || 1)) { info(nm(s1) + ' và ' + nm(s2) + ' song song hoặc trùng nhau, không có giao điểm'); return null; }
          if (Math.abs(dot(w, unit(nn))) > C.tol) { info(nm(s1) + ' và ' + nm(s2) + ' chéo nhau (không đồng phẳng), không có giao điểm'); return null; }
          p = add(p1, mul(d1, dot(cross(w, d2), nn) / dot(nn, nn)));
          if (len(sub(p, C.center)) > C.R * 6) { info('Giao điểm nằm quá xa hình'); return null; }
          text = ' là giao điểm của ' + nm(s1) + ' và ' + nm(s2);
        } else if (def.at) { p = def.at.slice(0, 3); text = ''; }
        else return null;
        var ex = visiblePtAt(p);
        if (ex) { info('Điểm này đã có: ' + ex); return null; }
        n = name && !P(name) ? name : nextName();
        if (def.ratio) text = ' trên ' + nm([a, b]) + ' với ' + a + n + ' : ' + n + b + ' = ' + (def.m || 1) + ' : ' + (def.n || 1);
        return push({ t: 'pt', name: n, p: p, def: def }, 'Đã thêm ' + n + text);
      },
      addSegment: function (a, b, o) {
        if (a === b || !need(a, b)) return null;
        if (findObj(function (u) { return u.t === 'seg' && sameSet([u.a, u.b], [a, b]); }) || (sc.edges || []).some(function (e) { return sameSet(e, [a, b]); })) { info('Đoạn ' + nm([a, b]) + ' đã có'); return null; }
        return push({ t: 'seg', a: a, b: b, dash: !!(o && o.dash) }, 'Đã nối ' + nm([a, b]) + (o && o.dash ? ' (nét đứt)' : ''));
      },
      addPlane: function (a, b, c) {
        if (!need(a, b, c)) return null;
        if (len(cross(sub(P(b), P(a)), sub(P(c), P(a)))) < 1e-9) { info('Ba điểm ' + nm([a, b, c]) + ' thẳng hàng, không xác định được mặt phẳng'); return null; }
        if (findObj(function (u) { return u.t === 'plane' && sameSet(u.pts, [a, b, c]); })) { info('Mặt phẳng (' + nm([a, b, c]) + ') đã có'); return null; }
        return push({ t: 'plane', pts: [a, b, c] }, 'Đã dựng mặt phẳng (' + nm([a, b, c]) + ')');
      },
      extendEdge: function (a, b) {
        if (a === b || !need(a, b)) return null;
        return push({ t: 'ext', a: a, b: b }, 'Đã kéo dài ' + nm([a, b]) + ' về hai phía');
      },
      growFace: function (names, k) {
        if (!names || names.length < 3 || !need.apply(null, names)) return null;
        return push({ t: 'grow', pts: names.slice(), k: k || 1.7 }, 'Đã mở rộng mặt (' + nm(names) + ')');
      },
      markAngle: function (a, o, b) {
        if (!need(a, o, b)) return null;
        var right = Math.abs(dot(unit(sub(P(a), P(o))), unit(sub(P(b), P(o))))) < 1e-6;
        return push({ t: 'ang', a: a, o: o, b: b }, 'Đã đánh dấu góc ' + (right ? 'vuông ' : '') + nm([a, o, b]));
      },
      highlight: function (ref) {
        if (ref.edge) {
          if (!need(ref.edge[0], ref.edge[1])) return null;
          var o = findObj(function (u) { return u.t === 'hl' && u.edge && sameSet(u.edge, ref.edge); });
          if (o) { api.remove(o.id); return null; }
          return push({ t: 'hl', edge: ref.edge.slice() }, 'Đã tô cạnh ' + nm(ref.edge));
        }
        if (ref.face) {
          if (!need.apply(null, ref.face)) return null;
          return push({ t: 'hl', face: ref.face.slice() }, 'Đã tô mặt (' + nm(ref.face) + ')');
        }
        return null;
      },
      measureLength: function (a, b) {
        if (a === b || !need(a, b)) return null;
        var r = lenInfo(a, b), o = findObj(function (u) { return u.t === 'mlen' && sameSet([u.a, u.b], [a, b]); });
        if (!o) { o = { t: 'mlen', a: a, b: b, id: 'u' + (st.seq++) }; st.objs.push(o); }
        r.id = o.id;
        emit('measure', r);
        commit();
        return r;
      },
      measureAngle: function (s1, s2) {
        if (!need(s1[0], s1[1], s2[0], s2[1])) return null;
        var r = angInfo(s1, s2), o = findObj(function (u) { return u.t === 'mang' && ((sameSet(u.s1, s1) && sameSet(u.s2, s2)) || (sameSet(u.s1, s2) && sameSet(u.s2, s1))); });
        if (!o) { o = { t: 'mang', s1: s1.slice(), s2: s2.slice(), id: 'u' + (st.seq++) }; st.objs.push(o); }
        r = { kind: r.kind, name: r.name, hidden: r.hidden, value: r.value, valueText: r.valueText, text: r.text, id: o.id };
        if (r.hidden) { delete r.value; delete r.valueText; }
        emit('measure', r);
        commit();
        return r;
      },
      remove: function (id) {
        var o = findObj(function (u) { return u.id === id; });
        if (!o) return false;
        var gone = [id];
        if (o.t === 'pt') st.objs.forEach(function (u) {
          var refs = [u.a, u.b, u.o].concat(u.pts || [], u.edge || [], u.face || [], u.s1 || [], u.s2 || []);
          if (u !== o && refs.indexOf(o.name) >= 0) gone.push(u.id);
        });
        st.objs = st.objs.filter(function (u) { return gone.indexOf(u.id) < 0; });
        st.pick = []; if (gone.indexOf(st.sel) >= 0) st.sel = null;
        emit('remove', { id: id, ids: gone, text: 'Đã xóa ' + objName(o) + (gone.length > 1 ? ' và ' + (gone.length - 1) + ' đối tượng phụ thuộc' : '') });
        commit();
        return true;
      },
      clear: function () {
        st.objs = []; st.pick = []; st.sel = null;
        emit('clear', { text: 'Đã xóa hết phần vẽ thêm' });
        commit();
      },
      reset: function () {
        st.objs = []; st.pick = []; st.sel = null; st.tool = 'select'; st.ptMode = 'mid'; st.dash = false;
        goView(sc.view[0], sc.view[1], 1);
        emit('reset', { text: 'Đã đặt lại hình' });
        commit();
      },
      snapshot: function () {
        var r = { pts: {}, edges: [], objs: items().map(function (i) { return i.id; }), view: curView(), size: [W, H] }, seen = {};
        hit.pts.forEach(function (p) { r.pts[p.name] = [p.x, p.y]; });
        Array.prototype.forEach.call(svg.querySelectorAll('[data-e]'), function (l) {
          var e = l.getAttribute('data-e'), h = l.getAttribute('data-h') === '1';
          if (seen[e] == null) { seen[e] = r.edges.length; r.edges.push({ e: e, hidden: h, mixed: false }); }
          else if (r.edges[seen[e]].hidden !== h) r.edges[seen[e]].mixed = true;
        });
        return r;
      },
      redraw: draw,
      destroy: function () {
        if (dead) return;
        dead = true;
        if (raf) cancelAnimationFrame(raf);
        clearTimeout(wheelT);
        if (ro) ro.disconnect();
        svg.removeEventListener('pointerdown', onDown); svg.removeEventListener('pointermove', onMove);
        svg.removeEventListener('pointerup', onUp); svg.removeEventListener('pointercancel', onUp); svg.removeEventListener('wheel', onWheel);
        if (LIVE && LIVE.get(box) === api) LIVE['delete'](box);
      }
    };
    if (LIVE) LIVE.set(box, api);
    draw();
    return api;
  }

  window.Fig3D = { mount: mount, newState: newState, SCENES: SCENES, TOOLS: TOOLS, VIEWS: VIEWS, HIDDEN_TEXT: HIDDEN_TEXT, fmtLen: fmtLen, fmtAng: fmtAng };
})();
