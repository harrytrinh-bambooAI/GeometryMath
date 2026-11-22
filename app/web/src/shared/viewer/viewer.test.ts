import assert from 'node:assert/strict';
import { test } from 'node:test';
import { fromCoords } from './adapter.ts';
import { DEFAULT_VIEW, makeProjector, VIEW_PRESETS, zoomAround, ZOOM_MAX, ZOOM_MIN } from './camera.ts';
import { formatAngle, formatDecimal, formatLength } from './format.ts';
import { lineRelation, polygonArea, sub, triangulate, type Vec3 } from './geometry.ts';
import { parseMeasure } from './measures.ts';
import { addPointOn, applyTap, clearAll, initialState, pointPos, type FigureState, type Target } from './model.ts';
import { buildRender } from './render.ts';
import { sceneHookEnabled } from './hookGate.ts';

// Hình thử của cả epic: HKG-01, a = 1 (F-GEO-031).
const HKG01 = { A: [0, 0, 0], B: [1, 0, 0], C: [1, 1, 0], D: [0, 1, 0], S: [0, 0, 1] };
const CUBE = {
  A: [0, 0, 0], B: [1, 0, 0], C: [1, 1, 0], D: [0, 1, 0],
  'A′': [0, 0, 1], 'B′': [1, 0, 1], 'C′': [1, 1, 1], 'D′': [0, 1, 1],
};
const pyramid = (lock: 'locked' | 'open' = 'locked') =>
  initialState(fromCoords('pyramid', HKG01, { params: { a: 1 }, hiddenMeasures: ['góc SDA', 'góc ASD'] }), lock);
const P = (id: string): Target => ({ kind: 'point', id });
const Ln = (id: string): Target => ({ kind: 'line', id });
const F = (id: string): Target => ({ kind: 'face', id });
function taps(s: FigureState, tool: Parameters<typeof applyTap>[1], ...ts: Target[]) {
  let out = { state: s } as ReturnType<typeof applyTap>;
  for (const t of ts) out = applyTap(out.state, tool, t);
  return out;
}
const near = (a: readonly number[], b: readonly number[]) => a.every((x, i) => Math.abs(x - b[i]!) < 1e-9);

test('tên cạnh và mặt ổn định: Cạnh SD, Mặt SAD, Mặt ABCD (không lặp chữ "Mặt", US-GEO-307-SC1)', () => {
  const s = pyramid();
  assert.deepEqual(s.base.edges.map((e) => e.id).sort(), ['AB', 'AD', 'BC', 'CD', 'SA', 'SB', 'SC', 'SD']);
  assert.deepEqual(s.base.faces.map((f) => f.label).sort(), ['Mặt ABCD', 'Mặt SAB', 'Mặt SAD', 'Mặt SBC', 'Mặt SCD']);
  assert.ok(s.base.faces.every((f) => !f.label.includes('Mặt Mặt')));
});

test('góc nhìn lúc mới mở: SA, AB, AD nét đứt; SB, SC, SD, BC, CD nét liền (US-GEO-301-SC7)', () => {
  const rm = buildRender(pyramid(), DEFAULT_VIEW, 800, 600);
  const st = Object.fromEntries(rm.lines.map((l) => [l.id, l.style]));
  assert.deepEqual(st, { AB: 'dashed', AD: 'dashed', BC: 'solid', CD: 'solid', SA: 'dashed', SB: 'solid', SC: 'solid', SD: 'solid' });
  const half = buildRender(pyramid(), { ...DEFAULT_VIEW, yaw: DEFAULT_VIEW.yaw + 180 }, 800, 600);
  const st2 = Object.fromEntries(half.lines.map((l) => [l.id, l.style]));
  assert.equal(st2.SA, 'solid');
  assert.equal(st2.SC, 'dashed');
  assert.equal(st2.BC, 'dashed');
  assert.equal(st2.CD, 'dashed');
});

test('"Nhìn từ trên": S trùng A, ABCD là hình vuông; "Nhìn từ trước": SA thẳng đứng, đáy thành một đoạn', () => {
  const s = pyramid();
  const top = makeProjector({ ...DEFAULT_VIEW, ...VIEW_PRESETS.top }, 800, 600, s.base.center, s.base.radius);
  const pt = (n: string) => top.project(pointPos(s, n)!);
  assert.ok(Math.hypot(pt('S').x - pt('A').x, pt('S').y - pt('A').y) < 1e-6);
  const d = (a: string, b: string) => Math.hypot(pt(a).x - pt(b).x, pt(a).y - pt(b).y);
  assert.ok(Math.abs(d('A', 'B') - d('B', 'C')) < 1e-6 && Math.abs(d('A', 'C') - d('B', 'D')) < 1e-6);
  const fr = makeProjector({ ...DEFAULT_VIEW, ...VIEW_PRESETS.front }, 800, 600, s.base.center, s.base.radius);
  const f = (n: string) => fr.project(pointPos(s, n)!);
  assert.ok(Math.abs(f('S').x - f('A').x) < 1e-6);
  assert.ok(['B', 'C', 'D'].every((n) => Math.abs(f(n).y - f('A').y) < 1e-6));
});

test('thu phóng có giới hạn và quanh điểm cố định', () => {
  let v = DEFAULT_VIEW;
  for (let i = 0; i < 50; i++) v = zoomAround(v, 0.8, 100, 100, 800, 600);
  assert.equal(v.zoom, ZOOM_MIN);
  for (let i = 0; i < 80; i++) v = zoomAround(v, 1.25, 400, 300, 800, 600);
  assert.equal(v.zoom, ZOOM_MAX);
});

test('đoạn AC nét đứt lúc mới mở; AC cắt BD tạo điểm M(0,5; 0,5; 0) (US-GEO-303-SC1, SC2)', () => {
  let s = taps(pyramid(), 'line', P('A'), P('C')).state;
  const rm = buildRender(s, DEFAULT_VIEW, 800, 600);
  assert.equal(rm.lines.find((l) => l.id === 'seg:AC')!.style, 'dashed');
  const o = taps(s, 'line', P('B'), P('D'));
  s = o.state;
  assert.equal(o.op, 'line');
  const m = s.userPoints.find((p) => p.name === 'M')!;
  assert.ok(near(m.pos, [0.5, 0.5, 0]));
  s = taps(s, 'line', P('S'), P('M')).state;
  assert.ok(s.segments.some((g) => g.id === 'SM'));
});

test('mặt phẳng (SBD): bán trong suốt, giao tuyến với đáy là BD; ba điểm thẳng hàng báo MSG-GEO-25', () => {
  const o = taps(pyramid(), 'plane', P('S'), P('B'), P('D'));
  const pl = o.state.planes[0]!;
  assert.equal(pl.id, '(SBD)');
  const tr = pl.traces.find((t) => t.face === 'ABCD')!;
  const ends = [tr.a, tr.b].map((v) => v.join(',')).sort();
  assert.deepEqual(ends, ['0,1,0', '1,0,0']);
  const s = addPointOn(pyramid(), 'SD', { kind: 'mid' }).state;
  const bad = taps(s, 'plane', P('S'), P('M'), P('D'));
  assert.equal(bad.notice?.code, '25');
  assert.equal(bad.state.planes.length, 0);
});

test('điểm: trung điểm M, chia 1 : 2 từ S cho N(0; 1/3; 2/3); tỉ lệ sai MSG-GEO-19; trùng MSG-GEO-26', () => {
  let s = addPointOn(pyramid(), 'SD', { kind: 'mid' }).state;
  assert.ok(near(pointPos(s, 'M')!, [0, 0.5, 0.5]));
  s = addPointOn(s, 'SD', { kind: 'ratio', m: 1, n: 2, from: 'S' }).state;
  assert.ok(near(pointPos(s, 'N')!, [0, 1 / 3, 2 / 3]));
  for (const m of [0, -1]) {
    const o = addPointOn(s, 'SD', { kind: 'ratio', m, n: 2, from: 'S' });
    assert.equal(o.notice?.code, '19');
    assert.equal(o.state.userPoints.length, 2);
  }
  const dup = addPointOn(s, 'SD', { kind: 'mid' });
  assert.equal(dup.notice?.code, '26');
  assert.equal(dup.state.userPoints.filter((p) => p.name === 'M').length, 1);
});

test('kéo dài SB, AD rồi hỏi giao điểm: chéo nhau → MSG-GEO-23 (US-GEO-303-FC1)', () => {
  let s = taps(pyramid(), 'extend', Ln('SB'), Ln('AD')).state;
  assert.equal(s.extensions.length, 2);
  const o = taps(s, 'line', Ln('ext:SB'), Ln('ext:AD'));
  assert.equal(o.notice?.code, '23');
  assert.equal(o.state.userPoints.length, 0);
  s = taps(s, 'extend', F('ABCD')).state;
  assert.ok(s.extensions.some((e) => e.id === 'ext:(ABCD)'));
});

test('lập phương: AC và B′D′ chéo nhau, không tạo giao điểm (US-GEO-307-FC1)', () => {
  let s = initialState(fromCoords('cube', CUBE, { params: { a: 1 }, hiddenMeasures: ['d(AC, B′D′)'] }));
  s = taps(s, 'line', P('A'), P('C'), P('B′'), P('D′')).state;
  assert.equal(s.userPoints.length, 0);
  const o = taps(s, 'line', Ln('seg:AC'), Ln('seg:B′D′'));
  assert.equal(o.notice?.code, '23');
  const m = taps(s, 'length', Ln('seg:AC'), Ln('seg:B′D′'));
  assert.equal(m.state.measures[0]!.hidden, true);
  assert.equal(m.state.measures[0]!.text, null);
});

test('đo: SD = a√2, góc SAD = 90°, d(S, (ABCD)) = a; góc SDA và ASD ẩn khi khóa, hiện 45° khi mở', () => {
  const s = pyramid();
  assert.equal(taps(s, 'length', P('S'), P('D')).state.measures[0]!.text, 'a√2');
  assert.equal(taps(s, 'angle', P('S'), P('A'), P('D')).state.measures[0]!.text, '90°');
  const d = taps(s, 'length', P('S'), F('ABCD')).state.measures[0]!;
  assert.equal(`${d.label} = ${d.text}`, 'd(S, (ABCD)) = a');
  for (const [a, o, b] of [['S', 'D', 'A'], ['A', 'S', 'D'], ['A', 'D', 'S']] as const) {
    const r = taps(s, 'angle', P(a), P(o), P(b));
    assert.equal(r.state.measures[0]!.hidden, true);
    assert.equal(r.state.measures[0]!.text, null);
    assert.equal(r.notice?.code, '24');
    assert.ok(!JSON.stringify(r.state.measures).includes('45'));
  }
  assert.equal(taps(pyramid('open'), 'angle', P('S'), P('D'), P('A')).state.measures[0]!.text, '45°');
});

test('định dạng: dấu phẩy, căn theo tham số', () => {
  assert.equal(formatDecimal(Math.sqrt(22.25)), '4,72');
  assert.equal(formatLength(Math.sqrt(22.25)), '4,72');
  assert.equal(formatLength(Math.sqrt(3) / 2, { symbol: 'a', value: 1 }), 'a√3/2');
  assert.equal(formatLength(2, { symbol: 'a', value: 1 }), '2a');
  assert.equal(formatAngle(45.0000001), '45°');
  assert.equal(formatAngle(30.25), '30,3°');
});

test('đọc mô tả đại lượng ẩn', () => {
  assert.deepEqual(parseMeasure('góc SDA'), { kind: 'ang', a: 'S', o: 'D', b: 'A' });
  assert.deepEqual(parseMeasure('d(AC, B′D′)'), { kind: 'dll', l1: ['A', 'C'], l2: ['B′', 'D′'] });
  assert.deepEqual(parseMeasure('d(S, (ABCD))'), { kind: 'dpp', p: 'S', plane: ['A', 'B', 'C', 'D'] });
  assert.deepEqual(parseMeasure('d((ABCD), (A′B′C′D′))'), { kind: 'dff', f1: ['A', 'B', 'C', 'D'], f2: ['A′', 'B′', 'C′', 'D′'] });
});

test('mặt lõm chữ L: tam giác phủ đúng diện tích 3, không phủ ô lõm (US-GEO-307-FC2)', () => {
  const L: Vec3[] = [[0, 0, 0], [2, 0, 0], [2, 1, 0], [1, 1, 0], [1, 2, 0], [0, 2, 0]];
  const tris = triangulate(L);
  const area = tris.reduce((a, [i, j, k]) => a + polygonArea([L[i]!, L[j]!, L[k]!]), 0);
  assert.ok(Math.abs(area - 3) < 1e-9);
  assert.ok(Math.abs(polygonArea(L) - 3) < 1e-9);
});

test('"Chọn": chạm trúng là một thao tác, chạm ngoài bỏ chọn không tính; "Xóa hết" giữ hình gốc', () => {
  const s = pyramid();
  const a = applyTap(s, 'select', P('S'));
  assert.equal(a.op, 'select');
  assert.equal(a.state.selection?.label, 'Điểm S');
  const b = applyTap(a.state, 'select', null);
  assert.equal(b.op, undefined);
  assert.equal(b.state.selection, null);
  let t = addPointOn(s, 'SD', { kind: 'mid' }).state;
  t = taps(t, 'line', P('A'), P('C')).state;
  t = clearAll(t);
  assert.equal(t.userPoints.length + t.segments.length, 0);
  assert.equal(t.base.points.length, 5);
});

test('giao của hai đường', () => {
  const r = lineRelation([0, 0, 0], [1, 1, 0], [1, 0, 0], sub([0, 1, 0], [1, 0, 0]));
  assert.equal(r.kind, 'intersect');
});

test('móc __geoScene chỉ bật ở dev hoặc VITE_GEO_TEST_HOOKS=1 (PO-52)', () => {
  assert.equal(sceneHookEnabled({ DEV: false }), false);
  assert.equal(sceneHookEnabled({ DEV: false, VITE_GEO_TEST_HOOKS: '0' }), false);
  assert.equal(sceneHookEnabled({ DEV: true }), true);
  assert.equal(sceneHookEnabled({ DEV: false, VITE_GEO_TEST_HOOKS: '1' }), true);
});
