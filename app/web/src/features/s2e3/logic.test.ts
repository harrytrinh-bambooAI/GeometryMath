import assert from 'node:assert/strict';
import { test } from 'node:test';
import { charCount, lengthIssue, MAX_IMAGE_BYTES, MSG, pickImage, stageText } from './logic.ts';

test('S2-E3 độ dài đề: 9 ký tự chặn, 10 nhận, trống báo "Hãy dán hoặc gõ đề" (BR-S2E3-02)', () => {
  assert.equal(lengthIssue('123456789'), MSG.short);
  assert.equal(lengthIssue('1234567890'), null);
  assert.equal(lengthIssue('   '), MSG.empty);
});

test('S2-E3 đề đúng 2000 ký tự nhận, 2001 chặn; ký hiệu toán đếm một ký tự', () => {
  assert.equal(lengthIssue('a'.repeat(2000)), null);
  assert.equal(lengthIssue('a'.repeat(2001)), MSG.long);
  assert.equal(charCount('SA ⊥ (ABCD), SA = a√2, góc 45°'), 30);
});

test('S2-E3 chọn hai ảnh chỉ nhận ảnh đầu, ảnh quá 5 MB bị chặn (BR-S2E3-06)', () => {
  const a = { size: 10, name: 'a' };
  const b = { size: 20, name: 'b' };
  assert.deepEqual(pickImage([a, b]), { file: a, message: MSG.oneImage });
  assert.equal(pickImage([{ size: MAX_IMAGE_BYTES }]).message, null);
  assert.deepEqual(pickImage([{ size: MAX_IMAGE_BYTES + 1 }]), { file: null, message: MSG.imageTooBig });
  assert.equal(stageText('running'), 'Đang chạy…');
});
