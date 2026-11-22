import assert from 'node:assert/strict';
import { test } from 'node:test';
import { fromCoords } from './adapter.ts';
import { polygonArea, polygonPlane, type Vec3 } from './geometry.ts';
import { addPointOn, applyTap, initialState, setLock, type FigureState, type Target } from './model.ts';
import { segmentRuns, transitionParams, type Occluder } from './visibility.ts';

// Ca bổ sung của lô FIX2 (soát E3 #1, #7, #8; PO-56). Mã ca Dev-E3-xx ghi ở qc_findings.md (mục Dev) của E3.
const HKG01 = { A: [0, 0, 0], B: [1, 0, 0], C: [1, 1, 0], D: [0, 1, 0], S: [0, 0, 1] };
const CUBE = {
  A: [0, 0, 0], B: [1, 0, 0], C: [1, 1, 0], D: [0, 1, 0],
  'A′': [0, 0, 1], 'B′': [1, 0, 1], 'C′': [1, 1, 1], 'D′': [0, 1, 1],
};
const P = (id: string): Target => ({ kind: 'point', id });
const Ln = (id: string): Target => ({ kind: 'line', id });
const F = (id: string): Target => ({ kind: 'face', id });
function taps(s: FigureState, tool: Parameters<typeof applyTap>[1], ...ts: Target[]) {
  let out = { state: s } as ReturnType<typeof applyTap>;
  for (const t of ts) out = applyTap(out.state, tool, t);
  return out;
}
const last = (s: FigureState) => s.measures[s.measures.length - 1]!;

test('Dev-E3-01 PO-56: khóa thì ẩn số đo gián tiếp trùng giá trị đại lượng ẩn (điểm–mặt thay mặt–mặt, điểm–đường thay đường–đường)', () => {
  const cube = initialState(fromCoords('cube', CUBE, { params: { a: 1 }, hiddenMeasures: ['d((ABCD), (A′B′C′D′))'] }), 'locked');
  // trực tiếp (cùng hình học)
  assert.equal(taps(cube, 'length', F('ABCD'), F('A′B′C′D′')).state.measures[0]!.hidden, true);
  // gián tiếp: d(A′, (ABCD)) = 1, cạnh AA′ = 1, d(B′, AB) = 1 — đều bằng đại lượng ẩn
  for (const r of [taps(cube, 'length', P('A′'), F('ABCD')), taps(cube, 'length', P('A'), P('A′')), taps(cube, 'length', P('B′'), Ln('AB'))]) {
    const m = last(r.state);
    assert.equal(m.hidden, true, m.label);
    assert.equal(m.text, null);
    assert.ok(Number.isNaN(m.value));
    assert.equal(r.notice?.code, '24');
  }
  // giá trị khác vẫn hiện: đường chéo mặt AC = a√2
  assert.equal(last(taps(cube, 'length', P('A'), P('C')).state).text, 'a√2');
  // mở khóa: hiện bình thường
  const open = initialState(fromCoords('cube', CUBE, { params: { a: 1 }, hiddenMeasures: ['d((ABCD), (A′B′C′D′))'] }), 'open');
  assert.equal(last(taps(open, 'length', P('A′'), F('ABCD')).state).text, 'a');
});

test('Dev-E3-02 PO-56: ẩn SD thì SB (= SD) cũng ẩn; SM = SD/2 (giá trị khác) vẫn hiện; góc không so với độ dài', () => {
  const s = initialState(fromCoords('pyramid', HKG01, { params: { a: 1 }, hiddenMeasures: ['SD'] }), 'locked');
  assert.equal(last(taps(s, 'length', P('S'), P('D')).state).hidden, true);
  assert.equal(last(taps(s, 'length', P('S'), P('B')).state).hidden, true);
  const withM = addPointOn(s, 'SD', { kind: 'mid' }).state;
  assert.equal(last(taps(withM, 'length', P('S'), P('M')).state).text, 'a√2/2');
  assert.equal(last(taps(s, 'length', P('S'), P('C')).state).text, 'a√3');
  assert.equal(last(taps(s, 'angle', P('S'), P('A'), P('D')).state).text, '90°');
});

test('Dev-E3-03 khóa lại thì số đo đã hiện trùng đại lượng ẩn bị ẩn lại ngay; mở lại không tự hiện (US-GEO-305-SC6)', () => {
  const open = initialState(fromCoords('pyramid', HKG01, { params: { a: 1 }, hiddenMeasures: ['góc SDA'] }), 'open');
  let s = taps(open, 'angle', P('S'), P('D'), P('A')).state;
  s = taps(s, 'angle', P('S'), P('A'), P('D')).state;
  assert.deepEqual(s.measures.map((m) => m.text), ['45°', '90°']);
  s = setLock(s, 'locked');
  assert.equal(s.measures[0]!.hidden, true);
  assert.equal(s.measures[0]!.text, null);
  assert.ok(Number.isNaN(s.measures[0]!.value));
  assert.ok(!JSON.stringify(s.measures).includes('45'));
  assert.equal(s.measures[1]!.text, '90°');
  s = setLock(s, 'open');
  assert.equal(s.measures[0]!.hidden, true);
  assert.equal(last(taps(s, 'angle', P('S'), P('D'), P('A')).state).text, '45°');
});

const square = (x0: number, x1: number, y0: number, y1: number, z: number): Occluder => {
  const poly: Vec3[] = [[x0, y0, z], [x1, y0, z], [x1, y1, z], [x0, y1, z]];
  return { poly, plane: polygonPlane(poly)! };
};

test('Dev-E3-04 nét khuất: điểm chuyển chính xác, không bỏ sót khúc khuất ngắn hơn 1/32', () => {
  const eye: Vec3 = [0, 0, 1];
  const a: Vec3 = [-0.5, 0.5, 0];
  const b: Vec3 = [1.5, 0.5, 0];
  const runs = segmentRuns(a, b, eye, [square(0, 1, 0, 1, 1)]);
  assert.equal(runs.length, 3);
  assert.ok(Math.abs(runs[1]!.from - 0.25) < 1e-12 && Math.abs(runs[1]!.to - 0.75) < 1e-12 && runs[1]!.hidden);
  // dải che rộng 0,01 (t ∈ [0,25; 0,255]) rơi giữa hai mẫu cũ (32 khúc) — nay vẫn thấy
  const thin = segmentRuns(a, b, eye, [square(0, 0.01, 0, 1, 1)]);
  const h = thin.filter((r) => r.hidden);
  assert.equal(h.length, 1);
  assert.ok(Math.abs(h[0]!.from - 0.25) < 1e-12 && Math.abs(h[0]!.to - 0.255) < 1e-12);
  // đoạn xuyên qua mặt che: đổi trạng thái đúng tại giao điểm với mặt phẳng
  const ts = transitionParams([0.5, 0.5, 0], [0.5, 0.5, 2], [1, 0, 0], [square(0, 1, 0, 1, 1)]);
  assert.ok(ts.some((t) => Math.abs(t - 0.5) < 1e-12));
});

// Lăng trụ đứng đáy chữ L (TC-GEO-0350), cao 1.
const L_BASE: Array<[string, number, number]> = [['A', 0, 0], ['B', 2, 0], ['C', 2, 1], ['D', 1, 1], ['E', 1, 2], ['F', 0, 2]];
const lPrism = () => {
  const names = L_BASE.map((b) => b[0]);
  const points = [
    ...L_BASE.map(([n, x, y]) => ({ name: n, pos: [x, y, 0] as Vec3 })),
    ...L_BASE.map(([n, x, y]) => ({ name: `${n}′`, pos: [x, y, 1] as Vec3 })),
  ];
  const edges: Array<[string, string]> = [];
  const faces: string[][] = [names, names.map((n) => `${n}′`)];
  names.forEach((n, i) => {
    const m = names[(i + 1) % names.length]!;
    edges.push([n, m], [`${n}′`, `${m}′`], [n, `${n}′`]);
    faces.push([n, m, `${m}′`, `${n}′`]);
  });
  return initialState({ points, edges, faces }, 'open');
};

test('Dev-E3-05 thiết diện lõm: mặt phẳng z = 1/2 cắt lăng trụ chữ L thành hình chữ L diện tích 3 (không phải bao lồi 3,5)', () => {
  let s = lPrism();
  for (const e of ['AA′', 'BB′', 'EE′']) s = addPointOn(s, e, { kind: 'mid' }).state;
  const names = s.userPoints.map((p) => p.name);
  assert.equal(names.length, 3);
  const out = taps(s, 'plane', ...names.map(P));
  assert.equal(out.state.planes.length, 1);
  const poly = out.state.planes[0]!.poly;
  assert.equal(poly.length, 6);
  assert.ok(Math.abs(polygonArea(poly) - 3) < 1e-9);
  assert.ok(poly.some((p) => Math.abs(p[0] - 1) < 1e-9 && Math.abs(p[1] - 1) < 1e-9), 'có đỉnh lõm (1; 1; 0,5)');
});

test('Dev-E3-06 thiết diện hình lồi giữ như cũ: (SBD) của chóp là tam giác SBD', () => {
  const s = initialState(fromCoords('pyramid', HKG01, { params: { a: 1 } }), 'open');
  const poly = taps(s, 'plane', P('S'), P('B'), P('D')).state.planes[0]!.poly;
  assert.equal(poly.length, 3);
  assert.ok(Math.abs(polygonArea(poly) - Math.sqrt(3) / 2) < 1e-9);
});
