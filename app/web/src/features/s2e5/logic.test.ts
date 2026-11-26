import assert from 'node:assert/strict';
import { test } from 'node:test';
import { blockedByFails, firstChanged, insertAfter, isDirty, move, validateDraft } from './logic.ts';

const four = ['b1', 'b2', 'b3', 'b4'];

test('US-S2E5-01-FC2 bước trống hoặc chỉ có dấu cách bị chặn, báo đúng số bước', () => {
  assert.deepEqual(validateDraft(['b1', 'b2', '', 'b4']), { index: 2, message: 'Bước 3 đang trống' });
  assert.deepEqual(validateDraft(['b1', 'b2', '   ', 'b4']), { index: 2, message: 'Bước 3 đang trống' });
  assert.equal(validateDraft(four), null);
});

test('US-S2E5-01-BV1, BV2 bước 2000 ký tự lưu được, 2001 ký tự bị chặn', () => {
  assert.equal(validateDraft(['b1', 'x'.repeat(2000)]), null);
  assert.deepEqual(validateDraft(['b1', 'x'.repeat(2001)]), { index: 1, message: 'Bước tối đa 2000 ký tự' });
  assert.equal(validateDraft(['b1', `  ${'√'.repeat(2000)}  `]), null);
});

test('US-S2E5-01-BV3, BV4 từ 1 đến 30 bước', () => {
  assert.equal(validateDraft(Array.from({ length: 30 }, (_, i) => `b${i}`)), null);
  assert.deepEqual(validateDraft(Array.from({ length: 31 }, (_, i) => `b${i}`)), { index: null, message: 'Tối đa 30 bước' });
  assert.deepEqual(validateDraft([]), { index: null, message: 'Lời giải cần ít nhất 1 bước' });
  assert.equal(validateDraft(['b1']), null);
});

test('US-S2E5-01-FC1 chưa đổi gì thì không có gì để lưu', () => {
  assert.equal(isDirty(four, four), false);
  assert.equal(isDirty(four, ['b1', 'b2', ' b3 ', 'b4']), false);
  assert.equal(isDirty(four, ['b1', 'b2', 'b3 sửa', 'b4']), true);
  assert.equal(isDirty(four, ['b1', 'b2', 'b3']), true);
});

test('US-S2E5-02-SC4 bước bị sửa và các bước sau về Chưa kiểm', () => {
  assert.equal(firstChanged(four, ['b1', 'b2', 'x', 'b4']), 2);
  assert.equal(firstChanged(four, four), 4);
  assert.equal(firstChanged(four, ['b1', 'mới', 'b2', 'b3', 'b4']), 1);
});

test('US-S2E5-01-SC2, SC3 thêm bước ở giữa và kéo đổi thứ tự', () => {
  assert.deepEqual(insertAfter(four, 2, 'mới'), ['b1', 'b2', 'mới', 'b3', 'b4']);
  assert.deepEqual(move(four, 3, 2), ['b1', 'b2', 'b4', 'b3']);
  assert.deepEqual(move(four, 0, 9), four);
});

test('US-S2E5-04-BV2 câu chặn duyệt ghi đúng số bước lệch', () => {
  assert.equal(blockedByFails(1), 'Chặn duyệt: còn 1 bước lệch');
  assert.equal(blockedByFails(3), 'Chặn duyệt: còn 3 bước lệch');
});
