import assert from 'node:assert/strict';
import { test } from 'node:test';
import { formatVnDate, formatVnDateTime, formatVnTime } from './vnTime.ts';

// Chạy bằng `npm test` (node --test, không cần trình duyệt). Kết quả không phụ thuộc TZ của máy.
test('UTC 17:30 là 00:30 hôm sau ở Việt Nam', () => {
  assert.equal(formatVnDateTime('2026-11-21T17:30:00Z'), '00:30 22/11/2026');
});

test('ngày và giờ tách riêng', () => {
  assert.equal(formatVnDate('2026-01-01T00:00:00Z'), '01/01/2026');
  assert.equal(formatVnTime('2026-01-01T00:00:00Z'), '07:00');
});

test('giá trị hỏng thì báo lỗi rõ', () => {
  assert.throws(() => formatVnDateTime('không phải ngày'), RangeError);
});
