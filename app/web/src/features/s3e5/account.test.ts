import assert from 'node:assert/strict';
import { test } from 'node:test';
import { canConfirm } from './confirm.ts';

test('US-S3E5-05 nút xác nhận xóa tài khoản chỉ mở khi có mật khẩu và gõ đúng chữ XÓA', () => {
  assert.equal(canConfirm('MatKhau#2026', 'XÓA'), true);
  assert.equal(canConfirm('MatKhau#2026', ' XÓA '), true);
  assert.equal(canConfirm('MatKhau#2026', ''), false);
  assert.equal(canConfirm('MatKhau#2026', 'xoa'), false);
  assert.equal(canConfirm('MatKhau#2026', 'XOA'), false);
  assert.equal(canConfirm('', 'XÓA'), false);
});

test('TC-S3E5-61 tài khoản không có mật khẩu (vào lớp bằng tên): chỉ cần gõ đúng XÓA (PO-72)', () => {
  assert.equal(canConfirm('', 'XÓA', false), true);
  assert.equal(canConfirm('', 'xoa', false), false);
  assert.equal(canConfirm('', 'XÓA', true), false);
});
