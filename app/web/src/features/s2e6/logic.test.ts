import assert from 'node:assert/strict';
import { test } from 'node:test';
import { chipTone, MSG, reasonError } from './logic.ts';

test('US-S2E6-02-BV1, BV2 lý do từ chối từ 5 đến 500 ký tự', () => {
  assert.equal(reasonError('abcd'), MSG.reasonRange);
  assert.equal(reasonError('abcde'), null);
  assert.equal(reasonError('a'.repeat(500)), null);
  assert.equal(reasonError('a'.repeat(501)), MSG.reasonRange);
  assert.equal(reasonError('  abcd  '), MSG.reasonRange);
});

test('US-S2E6-01-SC1 màu chip theo nhãn', () => {
  assert.equal(chipTone('Giáo viên đã duyệt'), 'grn');
  assert.equal(chipTone('Chờ soát'), 'yel');
  assert.equal(chipTone('Bị từ chối'), 'red');
});
