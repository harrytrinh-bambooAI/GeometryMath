import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { keyAction, msg37, parsePoints, total, validPoints } from './logic.ts';

describe('chấm tay C5-05 (US-S5E4-06)', () => {
  it('điểm 0…tối đa, bước 0,25', () => {
    assert.equal(validPoints(parsePoints('3,5'), 4), true);
    assert.equal(validPoints(parsePoints('4.5'), 4), false);
    assert.equal(validPoints(parsePoints('0,6'), 1), false);
    assert.equal(validPoints(parsePoints(''), 1), false);
    assert.equal(validPoints(parsePoints('abc'), 1), false);
  });
  it('tổng và câu chữ', () => {
    assert.equal(total([2, 3, 0, 1, 1]), 7);
    assert.equal(total([2, 3.5, null, 1, 1]), 7.5);
    assert.equal(msg37('Các bước', 4), 'Điểm tiêu chí Các bước phải từ 0 đến 4, bước 0,25.');
  });
  it('phím tắt QS5-23', () => {
    assert.equal(keyAction('j', false), 'next');
    assert.equal(keyAction('K', false), 'prev');
    assert.equal(keyAction('a', false), 'approve');
    assert.equal(keyAction('Enter', true), 'approve');
    assert.equal(keyAction('j', true), null);
    assert.equal(keyAction('x', false), 'skip');
  });
});
