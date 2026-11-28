import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { DECISION_LABEL, FEEDBACK_LABEL, noteProblem, percentText } from './historyLogic.ts';

describe('S5-E3 lịch sử hỏi đáp', () => {
  it('ghi chú tối đa 200 ký tự', () => {
    assert.equal(noteProblem('Em hỏi so sánh với 1, không phải đáp số'), null);
    assert.equal(noteProblem('ă'.repeat(200)), null);
    assert.equal(noteProblem('x'.repeat(201)), 'Ghi chú tối đa 200 ký tự.');
  });
  it('nhãn và tỉ lệ', () => {
    assert.equal(FEEDBACK_LABEL.false_block, 'Chặn sai');
    assert.equal(DECISION_LABEL.blocked, 'Bị chặn');
    assert.equal(percentText(60), '60 %');
    assert.equal(percentText(12.5), '12,5 %');
  });
});
