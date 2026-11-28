import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { PARTS, exportPath, fileNameOf } from './exportUrl.ts';

describe('xuất mã đề (US-S5E2-05)', () => {
  it('đủ 4 phần gửi parts=all, một phần gửi đúng tên phần', () => {
    assert.equal(exportPath('s1', '101', 'docx', PARTS.map((p) => p.id)), '/api/exams/s1/codes/101/export?format=docx&parts=all');
    assert.equal(exportPath('s1', '102', 'pdf', ['de']), '/api/exams/s1/codes/102/export?format=pdf&parts=de');
    assert.equal(exportPath('s1', '103', 'pdf', ['de', 'dap-an']), '/api/exams/s1/codes/103/export?format=pdf&parts=de%2Cdap-an');
  });
  it('tên tệp từ Content-Disposition', () => {
    assert.equal(fileNameOf("attachment; filename*=UTF-8''KT15-HHKG11_101.zip", 'x'), 'KT15-HHKG11_101.zip');
    assert.equal(fileNameOf('attachment; filename="a.pdf"', 'x'), 'a.pdf');
    assert.equal(fileNameOf(null, 'x'), 'x');
  });
});
