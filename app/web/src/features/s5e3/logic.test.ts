import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { MSG_22, endOfDayVn, fileProblem, nextAction } from './logic.ts';

describe('S5-E3 logic kho tri thức', () => {
  it('chỉ nhận docx, pdf, md ≤ 10 MB', () => {
    assert.equal(fileProblem('Phieu-on-chuong-VII.docx', 240 * 1024), null);
    assert.equal(fileProblem('ghi-chu.MD', 10), null);
    assert.equal(fileProblem('De.pdf', 11 * 1024 * 1024), MSG_22);
    assert.equal(fileProblem('Diem.xlsx', 50 * 1024), MSG_22);
    assert.equal(fileProblem('docx', 10), MSG_22);
  });
  it('máy trạng thái phiên bản', () => {
    assert.equal(nextAction('draft'), 'submit');
    assert.equal(nextAction('pending'), 'activate');
    assert.equal(nextAction('active'), null);
    assert.equal(nextAction('archived'), null);
  });
  it('hạn hiệu lực theo giờ Việt Nam', () => {
    assert.equal(endOfDayVn('2026-12-11'), '2026-12-11T16:59:59.000Z');
    assert.equal(endOfDayVn(''), null);
  });
});
