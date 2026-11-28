import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { canSave, quarter, totalLabel } from './logic.ts';

describe('rubric (US-S5E4-01)', () => {
  it('tổng sống và chặn lưu khi lệch thang', () => {
    assert.equal(totalLabel([2, 4, 2, 1, 1], 10), '10 / 10');
    assert.equal(canSave([2, 4, 2, 1, 1], 10), true);
    assert.equal(totalLabel([2, 4, 3, 1, 1], 10), '11 / 10');
    assert.equal(canSave([2, 4, 3, 1, 1], 10), false);
    assert.equal(totalLabel([2.5, 5, 1.5, 0.75], 10), '9,75 / 10');
  });
  it('điểm bội 0,25', () => {
    assert.equal(quarter(0.25), true);
    assert.equal(quarter(0.3), false);
    assert.equal(quarter(0), false);
    assert.equal(canSave(Array.from({ length: 11 }, () => 1), 11), false);
  });
});
