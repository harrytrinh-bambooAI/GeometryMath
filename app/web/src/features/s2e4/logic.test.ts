import assert from 'node:assert/strict';
import { test } from 'node:test';
import { defaultSelection, formatSize, MAX_FILE_BYTES, messageIssue, MSG, pickFile, refLink, summarize, type ItemLike } from './logic.ts';

test('S2-E4 tin chat: 1000 ký tự gửi được, 1001 báo "Tin tối đa 1000 ký tự", rỗng không gửi (BR-S2E4-06)', () => {
  assert.deepEqual(messageIssue('a'.repeat(1000)), { sendable: true, message: null });
  assert.deepEqual(messageIssue('a'.repeat(1001)), { sendable: false, message: MSG.tooLong });
  assert.deepEqual(messageIssue('   '), { sendable: false, message: null });
  assert.equal(messageIssue('√'.repeat(1000)).sendable, true);
});

test('S2-E4 chọn tệp: chỉ docx / pdf, đúng 10 MB nhận, thêm 1 byte chặn (BR-S2E4-07)', () => {
  assert.equal(pickFile([{ name: 'GiaoAn.DOCX', size: MAX_FILE_BYTES }]).message, null);
  assert.deepEqual(pickFile([{ name: 'a.docx', size: MAX_FILE_BYTES + 1 }]), { file: null, message: MSG.fileTooBig });
  assert.deepEqual(pickFile([{ name: 'bang.xlsx', size: 10 }]), { file: null, message: MSG.onlyDocxPdf });
  const a = { name: 'a.pdf', size: 5 };
  assert.deepEqual(pickFile([a, { name: 'b.pdf', size: 5 }]), { file: a, message: MSG.oneFile });
  assert.deepEqual(pickFile([]), { file: null, message: null });
});

test('S2-E4 mục đã có trong kho mặc định không tick; tóm tắt đếm đúng tạo / cập nhật / trùng (BR-S2E4-13)', () => {
  const items: ItemLike[] = [
    { index: 1, state: 'new', selectable: true, checked: true },
    { index: 2, state: 'exists', selectable: true, checked: false },
    { index: 3, state: 'update', selectable: true, checked: true },
    { index: 4, state: 'similar', selectable: true, checked: true },
    { index: 5, state: 'too_long', selectable: false, checked: false },
    { index: 6, state: 'created', selectable: false, checked: false, draftId: 'd' },
  ];
  assert.deepEqual(defaultSelection(items), [1, 3, 4]);
  assert.deepEqual(summarize(items, [1, 3, 4]), { toCreate: 2, toUpdate: 1, duplicates: 1, done: 1 });
  assert.deepEqual(summarize(items, []), { toCreate: 0, toUpdate: 0, duplicates: 1, done: 1 });
});

test('S2-E4 liên kết mở đề trong kho theo loại nguồn; dung lượng dùng dấu phẩy', () => {
  assert.equal(refLink('library', 'x'), '/gv/kho-de-cua-toi/kho-chung/x');
  assert.equal(refLink('mine', 'x'), '/gv/kho-de-cua-toi/de/x');
  assert.equal(refLink('draft', 'x'), '/gv/chinh-nhap/x');
  assert.equal(refLink(null, 'x'), null);
  assert.equal(formatSize(1_258_291), '1,2 MB');
  assert.equal(formatSize(2048), '2,0 KB');
});
