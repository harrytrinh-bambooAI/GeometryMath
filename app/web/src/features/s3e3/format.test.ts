import assert from 'node:assert/strict';
import { test } from 'node:test';
import type { Insight } from './types.ts';
import { cellLabel, columnLabel, fold, groupByStep, MSG, splitCards, STATUS_ICON, STATUS_LABEL } from './format.ts';

test('TC-S3E3-07 mỗi trạng thái có chữ và biểu tượng riêng; tên truy cập nêu học sinh, đề, trạng thái', () => {
  assert.deepEqual(STATUS_LABEL, { correct: 'Đúng', review: 'Xem lại', doing: 'Đang làm', todo: 'Chưa làm' });
  assert.deepEqual(STATUS_ICON, { correct: 'check', review: 'eye', doing: 'pencil', todo: 'clock' });
  assert.equal(cellLabel('Chi Mai', { position: 3, title: 'hai mặt phẳng' }, 'review'), 'Chi Mai, Đề 3 hai mặt phẳng: Xem lại');
  assert.equal(columnLabel({ position: 1, title: 'SC-đáy' }), 'Đề 1 · SC-đáy');
});

test('TC-S3E3-28 câu hỏi 1000 ký tự gập còn 300, mở ra đủ 1000', () => {
  const long = 'ă'.repeat(1000);
  assert.equal(Array.from(fold(long, false).shown).length, 300);
  assert.equal(fold(long, false).folded, true);
  assert.equal(fold(long, true).shown, long);
  assert.equal(fold('ngắn', false).folded, false);
});

test('TC-S3E3-50 tối đa 5 thẻ hiện ngay, phần còn lại gập', () => {
  const cards = Array.from({ length: 7 }, (_, i) => ({ position: i + 1, n: 7 - i }) as Insight);
  const { shown, rest } = splitCards(cards);
  assert.deepEqual(shown.map((c) => c.n), [7, 6, 5, 4, 3]);
  assert.deepEqual(rest.map((c) => c.n), [2, 1]);
});

test('TC-S3E3-42 dữ liệu thô gom theo bước; TC-S3E3-36 banner nêu m trên ngưỡng lấy từ cấu hình', () => {
  const g = groupByStep([
    { step: 1, kind: 'hint', text: 'a', at: '2026-11-26T08:10:00Z' },
    { step: 0, kind: 'question', text: 'b', at: '2026-11-26T08:12:00Z' },
    { step: 1, kind: 'question', text: 'c', at: '2026-11-26T08:13:00Z' },
  ]);
  assert.deepEqual(g.map((x) => [x.step, x.entries.length]), [[0, 1], [1, 2]]);
  assert.equal(MSG.below(4, 5), 'Mới có 4 trên 5 bài làm cho đề này, chưa đủ để gộp. Dưới đây là dữ liệu thô.');
  assert.equal(MSG.small(5), 'Lớp có ít hơn 5 học sinh, chưa gộp được. Dưới đây là dữ liệu thô.');
});

test('TC-S3E3-59 lớp tắt trợ lý AI: thẻ hiện MSG-S3-98 nguyên văn (PO-85)', () => {
  assert.equal(MSG.aiOff, 'Lớp này đã tắt trợ lý AI nên chưa gộp tự động. Thầy cô vẫn xem được dữ liệu từng bước.');
});
