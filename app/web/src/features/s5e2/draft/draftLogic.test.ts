import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { approvable, countProblem, initialSelection } from './draftLogic.ts';

describe('S5-E2 trợ lý soạn đề nháp', () => {
  const items = [
    {
      id: 'a',
      preselected: true,
      status: 'draft' as const,
      machineState: 'verified' as const,
    },
    {
      id: 'b',
      preselected: false,
      status: 'draft' as const,
      machineState: 'needs_review' as const,
    },
    {
      id: 'c',
      preselected: false,
      status: 'approved' as const,
      machineState: 'verified' as const,
    },
  ];
  it('chỉ chọn sẵn đề đã kiểm chứng còn nháp', () => {
    assert.deepEqual([...initialSelection(items)], ['a']);
  });
  it('đề "Cần xem lại" và đề đã duyệt không duyệt được', () => {
    assert.deepEqual(items.map(approvable), [true, false, false]);
  });
  it('số đề 1…10', () => {
    assert.equal(countProblem(5), null);
    assert.equal(countProblem(0), 'Số đề từ 1 đến 10.');
    assert.equal(countProblem(11), 'Số đề từ 1 đến 10.');
  });
});
