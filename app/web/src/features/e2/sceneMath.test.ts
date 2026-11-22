import assert from 'node:assert/strict';
import { test } from 'node:test';
import { fmtNum, parseParam } from './sceneMath.ts';

// Chạy bằng `npm test` (node --test).
test('parseParam nhận số dương, dấu phẩy thập phân (BR-GEO-132)', () => {
  assert.equal(parseParam('3'), 3);
  assert.equal(parseParam('2,5'), 2.5);
  assert.equal(parseParam(' 4.25 '), 4.25);
});

test('parseParam từ chối 0, số âm, chữ (F-GEO-075)', () => {
  for (const s of ['0', '−2', '-2', 'abc', '']) assert.equal(parseParam(s), null, s);
});

test('fmtNum: dấu phẩy, tối đa 2 chữ số', () => {
  assert.equal(fmtNum(5), '5');
  assert.equal(fmtNum(2.5), '2,5');
  assert.equal(fmtNum(4.000000001), '4');
  assert.equal(fmtNum(Math.SQRT2), '1,41');
});
