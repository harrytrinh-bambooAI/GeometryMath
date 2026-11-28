import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { lengthLabel, overLimit, taskLabel } from './logic.ts';

describe('S5-E5 logic màn thông báo', () => {
  it('đếm ký tự và chặn quá 1 000', () => {
    assert.equal(lengthLabel('a'.repeat(1001), 1000), '1 001 / 1 000 ký tự');
    assert.equal(overLimit('a'.repeat(1000), 1000), false);
    assert.equal(overLimit('a'.repeat(1001), 1000), true);
  });
  it('nhãn task AI', () => {
    assert.equal(taskLabel('kb_qa'), 'Hỏi đáp');
    assert.equal(taskLabel('la'), 'la');
  });
});
