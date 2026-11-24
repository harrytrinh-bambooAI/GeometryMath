import assert from 'node:assert/strict';
import { test } from 'node:test';
import { lockLine, remaining, vnDateTime } from './format.ts';

test('giờ Việt Nam dạng HH:mm, dd/MM/yyyy', () => {
  assert.equal(vnDateTime('2026-11-25T07:30:00Z'), '14:30, 25/11/2026');
});

test('còn 1 giờ 0 phút khi còn 59 phút 58 giây (US-GEO-701-SC5)', () => {
  assert.equal(remaining(59 * 60_000 + 58_000), 'còn 1 giờ 0 phút');
  assert.equal(remaining(12 * 60_000), 'còn 12 phút');
  assert.equal(remaining(-5000), 'còn 0 phút');
});

test('dòng phụ của thẻ vàng', () => {
  assert.equal(lockLine(null, 0), 'Chưa đặt giờ mở');
  const now = Date.parse('2026-11-25T07:00:00Z');
  assert.equal(lockLine('2026-11-25T08:00:00Z', now), 'Mở lúc 15:00, 25/11/2026 · còn 1 giờ 0 phút');
});
