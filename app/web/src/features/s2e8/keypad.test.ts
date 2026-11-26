import assert from 'node:assert/strict';
import { test } from 'node:test';
import { ALLOWED, KEY_ROWS, MAX_LENGTH, backspace, emptyField, filterText, insert, paste, setCaret, type Field } from './keypad.ts';

const type = (f: Field, s: string): Field => [...s].reduce((acc, c) => insert(acc, c), f);

test('US-S2E8-01 bảng phím 4 hàng × 6 cột, mọi phím nhập đều thuộc bảng ký tự nhận', () => {
  assert.equal(KEY_ROWS.length, 4);
  for (const row of KEY_ROWS) {
    assert.equal(row.length, 6);
    for (const k of row) if (k !== '⌫') assert.ok(ALLOWED.has(k), k);
  }
  assert.ok(ALLOWED.has('x'));
});

test('US-S2E8-01-SC1…SC4 chèn số, căn, phân số, π, mũ, độ, dấu phẩy', () => {
  assert.equal(type(emptyField, '45').value, '45');
  assert.equal(type(emptyField, '4√3').value, '4√3');
  assert.equal(type(emptyField, '1/2').value, '1/2');
  assert.equal(type(emptyField, '2π').value, '2π');
  assert.equal(type(emptyField, '2^3').value, '2^3');
  assert.equal(type(emptyField, '45°').value, '45°');
  assert.equal(type(emptyField, '0,5').value, '0,5');
});

test('US-S2E8-01-SC5, SC6 xóa lùi trước con trỏ và chèn giữa chuỗi', () => {
  assert.equal(backspace(backspace(type(emptyField, '4√3'))).value, '4');
  const mid = insert(setCaret(type(emptyField, '45'), 1), '°');
  assert.equal(mid.value, '4°5');
  assert.equal(mid.caret, 2);
  assert.equal(backspace(setCaret(type(emptyField, '45'), 0)).value, '45');
});

test('US-S2E8-01-BV1, BV2 đủ 50 ký tự vẫn nhập, ký tự 51 bị chặn', () => {
  const f49 = type(emptyField, '1'.repeat(49));
  const f50 = insert(f49, '2');
  assert.equal([...f50.value].length, MAX_LENGTH);
  assert.equal(f50.full, false);
  const f51 = insert(f50, '3');
  assert.equal([...f51.value].length, MAX_LENGTH);
  assert.equal(f51.full, true);
  assert.equal(backspace(f51).full, false);
});

test('US-S2E8-01-FC3 chuỗi dán bị lọc theo bảng phím', () => {
  assert.equal(filterText('45 độ <b>'), '45');
  assert.equal(paste(emptyField, '45 độ <b>').value, '45');
  assert.equal(filterText('2*3−1'), '2×3-1');
  assert.equal([...paste(emptyField, '9'.repeat(80)).value].length, MAX_LENGTH);
});
