import assert from 'node:assert/strict';
import { test } from 'node:test';
import { highlightOf, next, pollMs, prev, START, toggleCover, visible } from './logic.ts';

test('US-S2E7-01-SC2, BV1, BV2 hiện bước kế đúng một bước, khóa ở bước cuối và bước 0', () => {
  let s = START;
  assert.equal(prev(s), s);
  s = next(s, 4);
  assert.equal(visible(s), 1);
  s = next(next(next(s, 4), 4), 4);
  assert.equal(visible(s), 4);
  assert.equal(next(s, 4), s);
});

test('US-S2E7-01-SC4 che lời giải nhớ vị trí, bỏ che hiện lại đúng bước', () => {
  const s = next(next(next(START, 4), 4), 4);
  const covered = toggleCover(s);
  assert.equal(visible(covered), 0);
  assert.equal(visible(toggleCover(covered)), 3);
});

test('US-S2E7-01-BV3 lời giải 30 bước hiện đủ 30, lần thứ 31 không đổi', () => {
  let s = START;
  for (let i = 0; i < 31; i++) s = next(s, 30);
  assert.equal(visible(s), 30);
});

test('US-S2E7-01-SC2 phần hình của bước 1: AC, SC và góc SCA', () => {
  const h = highlightOf('SA ⊥ (ABCD) nên AC là hình chiếu của SC, góc cần tìm là SCA.');
  assert.deepEqual(h.faces, ['ABCD']);
  assert.ok(h.edges.includes('AC') && h.edges.includes('SC'));
  assert.deepEqual(h.angles, ['SCA']);
});

test('US-S2E7-02-BV1 kiểm mạng dày hơn hạn bật lại 5 giây', () => {
  assert.ok(pollMs(5) <= 5000);
  assert.equal(pollMs(5), 1000);
});
