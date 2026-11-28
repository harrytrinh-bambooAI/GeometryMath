import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { canApprove, keyAction, parsePoints, regionBox, total, validPoints, type CritState } from './logic.ts';

const RB01 = [
  { ord: 1, maxPoints: 2 },
  { ord: 2, maxPoints: 4 },
  { ord: 3, maxPoints: 2 },
  { ord: 4, maxPoints: 1 },
  { ord: 5, maxPoints: 1 },
];

describe('màn chấm có đề xuất AI (US-S5E6-04)', () => {
  it('đọc điểm kiểu Việt Nam, bước 0,25', () => {
    assert.equal(parsePoints('2,5'), 2.5);
    assert.equal(validPoints(2.5, 4), true);
    assert.equal(validPoints(2.3, 4), false);
    assert.equal(validPoints(5, 4), false);
  });

  it('Duyệt chỉ bật khi mọi tiêu chí có điểm (BR-S5E6-10)', () => {
    const s: Record<number, CritState> = {
      1: { act: 'accept', value: '2' },
      2: { act: 'edit', value: '2,5' },
      3: { act: 'accept', value: '0' },
      4: { act: 'accept', value: '1' },
      5: { act: 'drop', value: '' },
    };
    assert.equal(canApprove(s, RB01), false);
    const done = { ...s, 5: { act: 'drop' as const, value: '0,5' } };
    assert.equal(canApprove(done, RB01), true);
    assert.equal(total(done, RB01), 6);
  });

  it('phím tắt QS5-23', () => {
    assert.equal(keyAction('j', false, false), 'next');
    assert.equal(keyAction('K', false, false), 'prev');
    assert.equal(keyAction('a', false, false), 'accept');
    assert.equal(keyAction('A', true, false), 'acceptAll');
    assert.equal(keyAction('x', false, false), 'drop');
    assert.equal(keyAction('Enter', false, true), 'approve');
    assert.equal(keyAction('a', false, true), null);
  });

  it('vùng khoanh theo phần trăm ảnh gốc', () => {
    assert.deepEqual(regionBox({ x: 40, y: 140, w: 480, h: 60 }, { width: 800, height: 1000 }), {
      left: '5.000%',
      top: '14.000%',
      width: '60.000%',
      height: '6.000%',
    });
    assert.equal(regionBox({ x: 1, y: 1, w: 1, h: 1 }, { width: 0, height: 0 }), null);
  });
});
