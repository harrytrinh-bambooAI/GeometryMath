import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { labelName, lostText, points, practiceTitle, toggle } from './insightsLogic.ts';

describe('S5-E5b lỗi sai của lớp', () => {
  it('nhãn loại lỗi và số bài (không phần trăm)', () => {
    assert.equal(labelName('sai-tinh-toan'), 'Sai tính toán');
    assert.equal(labelName(null), '');
    assert.equal(lostText(4, 6), '4/6 bài');
    assert.ok(!lostText(4, 6).includes('%'));
    assert.equal(points(2.5), '2,5');
    assert.equal(points(3), '3');
  });
  it('tick câu gợi ý và tên bộ luyện', () => {
    assert.deepEqual(toggle([], 'a'), ['a']);
    assert.deepEqual(toggle(['a', 'b'], 'a'), ['b']);
    assert.equal(practiceTitle('BC-01', 'Kiểm tra 15 phút'), 'Nhắc lại BC-01 · Kiểm tra 15 phút');
  });
});
