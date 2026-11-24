import assert from 'node:assert/strict';
import { test } from 'node:test';
import { remaining, vnToIso } from './time.ts';

test('đếm ngược: làm tròn lên theo phút từ 10 phút, không bao giờ âm (US-GEO-608-SC3, 612-SC2)', () => {
  assert.equal(remaining(3_600_000), 'Còn 1 giờ 0 phút');
  assert.equal(remaining(3_598_500), 'Còn 1 giờ 0 phút');
  assert.equal(remaining(7_200_000), 'Còn 2 giờ 0 phút');
  assert.equal(remaining(15 * 60_000), 'Còn 15 phút');
  assert.equal(remaining(65_000), 'Còn 1 phút 5 giây');
  assert.equal(remaining(9_000), 'Còn 9 giây');
  assert.equal(remaining(-5_000), 'Còn 0 giây');
});

test('19:00 giờ Việt Nam là 12:00 UTC cùng ngày, bất kể múi giờ thiết bị (US-GEO-604-SC5)', () => {
  assert.equal(vnToIso('2030-05-20', '19:00'), '2030-05-20T12:00:00.000Z');
  assert.equal(vnToIso('2030-05-20', '06:30'), '2030-05-19T23:30:00.000Z');
  assert.equal(vnToIso('', '19:00'), null);
  assert.equal(vnToIso('2030-05-20', ''), null);
});
