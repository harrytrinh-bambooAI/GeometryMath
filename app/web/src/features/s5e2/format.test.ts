import assert from 'node:assert/strict';
import { test } from 'node:test';
import { ApiError } from '../../shared/api/errors.ts';
import { fieldError, MACHINE_LABEL, vn } from './format.ts';

test('S5-E2 vn: điểm dạng Việt Nam (1,5 · 0,25 · 10)', () => {
  assert.equal(vn(1.5), '1,5');
  assert.equal(vn(0.25), '0,25');
  assert.equal(vn(10), '10');
});

test('S5-E2 fieldError: lấy thông điệp của ô nhập từ details (MSG-S5-13)', () => {
  const e = new ApiError({
    status: 400,
    code: 'VALIDATION_FAILED',
    message: 'Số mã đề từ 1 đến 8.',
    details: [{ field: 'variantCount', message: 'Số mã đề từ 1 đến 8.' }],
  });
  assert.equal(fieldError(e, 'variantCount'), 'Số mã đề từ 1 đến 8.');
  assert.equal(fieldError(e, 'minutes'), null);
  assert.equal(fieldError(new Error('x'), 'minutes'), null);
});

test('S5-E2 nhãn máy theo câu (QS5-08)', () => {
  assert.equal(MACHINE_LABEL['da-kiem-chung'], 'Đã kiểm chứng');
});
