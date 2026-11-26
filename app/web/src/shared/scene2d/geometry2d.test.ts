import assert from 'node:assert/strict';
import { test } from 'node:test';
import { fitView, formatNumber, isLocked, measure, toPercent, type Scene2D } from './geometry2d.ts';

const PL01: Scene2D = {
  contract: 'scene2d-v1',
  kind: 'plane',
  coordSource: 'engine',
  points: [
    { id: 'A', label: 'A', x: 0, y: 0 },
    { id: 'B', label: 'B', x: 5, y: 0 },
    { id: 'C', label: 'C', x: 0, y: 12 },
  ],
  segments: [['A', 'B'], ['B', 'C'], ['C', 'A']],
  circles: [],
  constraints: [],
};

test('scene2d: định dạng số hai chữ số thập phân, dấu phẩy, bỏ 0 thừa', () => {
  assert.equal(formatNumber(5), '5');
  assert.equal(formatNumber(4.999999999), '5');
  assert.equal(formatNumber(6.92820323), '6,93');
  assert.equal(formatNumber(4.8), '4,8');
});

test('scene2d: đo độ dài và góc trên tọa độ', () => {
  assert.equal(measure(PL01, [], 'length', ['A', 'B'])?.text, 'AB = 5');
  assert.equal(measure(PL01, [], 'angle', ['B', 'A', 'C'])?.text, 'A = 90°');
});

test('scene2d: đại lượng bị khóa không đo được (cùng đỉnh, hai cạnh bất kể thứ tự)', () => {
  const locked = [{ kind: 'angle', of: ['B', 'A', 'C'] }];
  assert.equal(isLocked(locked, 'angle', ['C', 'A', 'B']), true);
  assert.equal(measure(PL01, locked, 'angle', ['C', 'A', 'B']), null);
  assert.equal(isLocked(locked, 'angle', ['A', 'B', 'C']), false);
  assert.equal(isLocked([{ kind: 'length', of: ['A', 'C'] }], 'length', ['C', 'A']), true);
});

test('scene2d: khung nhìn bao điểm, y lật khi đổi sang phần trăm', () => {
  const v = fitView(PL01);
  const a = toPercent(v, 0, 0);
  const c = toPercent(v, 0, 12);
  assert.ok(a.top > c.top);
  assert.ok(a.left > 0 && a.left < 100);
});
