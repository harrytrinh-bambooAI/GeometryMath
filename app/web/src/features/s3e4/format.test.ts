import assert from 'node:assert/strict';
import { test } from 'node:test';
import { auditLine, badge, minReleaseIso, vnAt } from './format.ts';

test('TC-S3E4-08 TC-S3E4-34 số lớn: 99 ghi "99", 100 trở lên ghi "99+"', () => {
  assert.equal(badge(0), '0');
  assert.equal(badge(99), '99');
  assert.equal(badge(100), '99+');
  assert.equal(badge(345), '99+');
});

test('TC-S3E5-05 giờ hiển thị theo "HH:mm, dd/MM/yyyy" giờ Việt Nam', () => {
  assert.equal(vnAt('2026-12-06T01:00:00Z'), '08:00, 06/12/2026');
  assert.equal(
    auditLine({ type: 'EARLY_RELEASE', actorCode: 'GV-7K2M-9QPA', targetType: 'assignment_set', targetId: 'x', before: '2026-12-06T01:00:00Z', after: '2026-12-05T14:00:00Z', at: '2026-12-05T14:00:00Z' }),
    '21:00, 05/12/2026 · Mở ngay · GV-7K2M-9QPA · trước 08:00, 06/12/2026 → sau 21:00, 05/12/2026',
  );
  assert.equal(
    auditLine({ type: 'EARLY_RELEASE', targetType: 'assignment_set', targetId: 'x', before: null, after: '2026-12-05T14:00:00Z', at: '2026-12-05T14:00:00Z' }),
    '21:00, 05/12/2026 · Mở ngay · trước chưa đặt → sau 21:00, 05/12/2026',
  );
});

test('TC-S3E4-17 giờ tối thiểu của ô Đổi giờ là giờ máy chủ + 1 phút, làm tròn lên phút', () => {
  assert.equal(minReleaseIso('2026-12-05T14:00:00.000Z'), '2026-12-05T14:01:00.000Z');
  assert.equal(minReleaseIso('2026-12-05T14:00:20.000Z'), '2026-12-05T14:02:00.000Z');
});
