import assert from 'node:assert/strict';
import { test } from 'node:test';
import { CHOICE_MSG, maxChoiceAttempts, normalizeChoice, validateChoice, type ChoiceOptionDraft } from './choice.ts';

const opts = (texts: string[], correct = 1): ChoiceOptionDraft[] => texts.map((text, i) => ({ text, correct: i === correct }));

test('US-S2E9-03 câu 4 lựa chọn, đúng một đáp án, mặc định 1 lượt là hợp lệ', () => {
  assert.equal(validateChoice(opts(['30°', '45°', '60°', '90°']), 1), null);
});

test('US-S2E9-03-BV1 số lựa chọn: 2 và 5 nhận, 1 và 6 bị chặn', () => {
  assert.equal(validateChoice(opts(['A'], 0), 1), CHOICE_MSG.count);
  assert.equal(validateChoice(opts(['A', 'B']), 1), null);
  assert.equal(validateChoice(opts(['A', 'B', 'C', 'D', 'E']), 1), null);
  assert.equal(validateChoice(opts(['A', 'B', 'C', 'D', 'E', 'F']), 1), CHOICE_MSG.count);
});

test('US-S2E9-03-BV2 lựa chọn 100 ký tự nhận, 101 bị chặn, rỗng bị chặn', () => {
  assert.equal(validateChoice(opts(['x'.repeat(100), 'B']), 1), null);
  assert.equal(validateChoice(opts(['x'.repeat(101), 'B']), 1), CHOICE_MSG.long);
  assert.equal(validateChoice(opts(['  ', 'B']), 1), CHOICE_MSG.empty);
});

test('US-S2E9-03-FC2, FC3 không có hoặc có hai đáp án đúng bị chặn', () => {
  assert.equal(validateChoice(opts(['A', 'B', 'C', 'D'], -1), 1), CHOICE_MSG.noCorrect);
  const two = opts(['A', 'B', 'C', 'D']).map((o, i) => ({ ...o, correct: i < 2 }));
  assert.equal(validateChoice(two, 1), CHOICE_MSG.manyCorrect);
});

test('US-S2E9-03-FC4 lựa chọn trùng sau khi chuẩn hóa chữ bị chặn', () => {
  assert.equal(normalizeChoice(' 45° '), normalizeChoice('45°'));
  assert.equal(validateChoice(opts(['45°', ' 45° ', '60°']), 1), CHOICE_MSG.duplicate);
  assert.equal(validateChoice(opts(['Góc  SCA', 'góc sca']), 1), CHOICE_MSG.duplicate);
});

test('US-S2E9-03-BV3 số lượt tối đa bằng số lựa chọn trừ 1', () => {
  assert.equal(maxChoiceAttempts(4), 3);
  assert.equal(maxChoiceAttempts(2), 1);
  assert.equal(validateChoice(opts(['A', 'B', 'C', 'D']), 3), null);
  assert.equal(validateChoice(opts(['A', 'B', 'C', 'D']), 4), 'Tối đa 3 lượt cho câu 4 lựa chọn');
});
