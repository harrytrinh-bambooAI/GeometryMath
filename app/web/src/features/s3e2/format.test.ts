import assert from 'node:assert/strict';
import { test } from 'node:test';
import { dueLabel, releaseBeforeDue } from './format.ts';

test('TC-S3E2-29 nhãn hạn theo giờ Việt Nam có thứ: "T6 04/12/2026 20:00"', () => {
  assert.equal(dueLabel('2026-12-04T13:00:00Z'), 'T6 04/12/2026 20:00');
});

test('TC-S3E2-21 giờ mở bằng đúng hạn không cảnh báo, sớm hơn 1 phút thì cảnh báo', () => {
  const due = '2026-12-11T13:00:00Z';
  assert.equal(releaseBeforeDue('2026-12-11T13:00:00Z', due), false);
  assert.equal(releaseBeforeDue('2026-12-11T12:59:00Z', due), true);
  assert.equal(releaseBeforeDue('2026-12-13T01:00:00Z', due), false);
  assert.equal(releaseBeforeDue(null, due), false);
});
