import assert from 'node:assert/strict';
import { test } from 'node:test';
import {
  buildPdf,
  checkSelection,
  exportSummary,
  fileName,
  itemBlocks,
  LINK_DAYS,
  LINK_DEFAULT_DAYS,
  MSG_EXPORT_MAX,
  MSG_EXPORT_MIN,
  projectScene,
  variantCountError,
  wrapText,
  type ExportItem,
} from './exportModel.ts';

const plane: ExportItem = {
  id: 'fx10',
  title: 'PL-01 Góc A của tam giác 5, 12, 13',
  statement: 'Tam giác ABC có AB = 5, AC = 12, BC = 13. Tính góc A.',
  kind: 'plane',
  sceneKind: 'plane',
  scene: {
    points: [
      { id: 'A', label: 'A', x: 0, y: 0 },
      { id: 'B', label: 'B', x: 5, y: 0 },
      { id: 'C', label: 'C', x: 0, y: 12 },
    ],
    segments: [
      ['A', 'B'],
      ['B', 'C'],
      ['C', 'A'],
    ],
    circles: [],
  },
  machineLabel: 'Cần xem lại',
  humanLabel: 'Giáo viên đã duyệt',
  lockedAssigned: false,
  solution: null,
};
const solved: ExportItem = {
  ...plane,
  id: 'fx14',
  solution: { steps: ['AC = a√2.', 'tan SCA = 1.', 'Góc bằng 45°.'], machineLabel: 'Đã kiểm chứng', humanLabel: 'Giáo viên đã duyệt' },
};

test('US-S2E9-02-SC1 bản xuất không tick lời giải: chỉ tiêu đề, chữ đề và hình', () => {
  const types = itemBlocks(plane, 1).map((b) => b.type);
  assert.deepEqual(types, ['heading', 'statement', 'figure']);
  assert.equal(exportSummary({ withSolution: false, confirmed: false, items: [plane] }).withSolution, false);
});

test('US-S2E9-02-SC3 kèm lời giải: hai nhãn in ở đầu trang, đủ các bước', () => {
  const blocks = itemBlocks(solved, 2);
  assert.deepEqual(blocks[0], { type: 'labels', machine: 'Đã kiểm chứng', human: 'Giáo viên đã duyệt' });
  assert.deepEqual(blocks[1], { type: 'heading', text: 'Đề 2. PL-01 Góc A của tam giác 5, 12, 13' });
  assert.equal(blocks.filter((b) => b.type === 'step').length, 3);
  const sum = exportSummary({ withSolution: true, confirmed: false, items: [plane, solved] });
  assert.deepEqual(sum, { order: ['fx10', 'fx14'], withSolution: true, figures: 2, steps: 3 });
});

test('US-S2E9-02-BV1 số đề mỗi tệp: 1 và 20 nhận, 0 và 21 bị chặn', () => {
  assert.equal(checkSelection(0), MSG_EXPORT_MIN);
  assert.equal(checkSelection(1), null);
  assert.equal(checkSelection(20), null);
  assert.equal(checkSelection(21), MSG_EXPORT_MAX);
});

test('US-S2E9-02-SC2 tệp PDF: mỗi trang một ảnh, đúng số trang, bảng xref trỏ đúng vị trí', () => {
  const jpeg = new Uint8Array([0xff, 0xd8, 0xff, 0xd9]);
  const pdf = buildPdf(
    [1, 2, 3].map(() => ({ jpeg, width: 1240, height: 1754 })),
    'Phiếu đề',
  );
  const text = new TextDecoder('latin1').decode(pdf);
  assert.ok(text.startsWith('%PDF-1.4'));
  assert.ok(text.trimEnd().endsWith('%%EOF'));
  assert.equal((text.match(/\/Type \/Page[^s]/g) ?? []).length, 3);
  assert.match(text, /\/Count 3/);
  assert.equal((text.match(/\/Filter \/DCTDecode/g) ?? []).length, 3);
  const xref = Number(/startxref\n(\d+)/.exec(text)![1]);
  assert.equal(text.slice(xref, xref + 4), 'xref');
  // mục xref của đối tượng 1 trỏ đúng "1 0 obj"
  const first = Number(text.slice(xref).split('\n')[3]!.slice(0, 10));
  assert.equal(text.slice(first, first + 7), '1 0 obj');
  assert.throws(() => buildPdf([], 'x'));
});

test('US-S2E9-02 tên tệp bỏ dấu, ngắt dòng theo bề rộng, chiếu cảnh phẳng giữ tọa độ', () => {
  assert.equal(fileName('PL-01 Góc A của tam giác 5, 12, 13', 'png'), 'pl-01-goc-a-cua-tam-giac-5-12-13.png');
  assert.equal(fileName('Đề', 'pdf'), 'de.pdf');
  assert.deepEqual(wrapText('một hai ba bốn', 7, (s) => s.length), ['một hai', 'ba bốn']);
  const p = projectScene(plane);
  assert.deepEqual(p.points.get('C'), [0, 12]);
  assert.equal(p.segments.length, 3);
  const space = projectScene({ ...plane, sceneKind: 'space', scene: { points: [{ name: 'S', x: 0, y: 2, z: 4 }], edges: [], axes: { up: 'z' } } });
  const sp = space.points.get('S')!;
  assert.ok(Math.abs(sp[0] + 0.9) < 1e-9 && Math.abs(sp[1] - 3.4) < 1e-9);
});

test('US-S2E9-01-BV1, US-S2E9-02-SC6 số biến thể 1…10; hạn liên kết chỉ 1, 7, 30 ngày, mặc định 7', () => {
  assert.equal(variantCountError(0), 'Chọn từ 1 đến 10');
  assert.equal(variantCountError(1), null);
  assert.equal(variantCountError(10), null);
  assert.equal(variantCountError(11), 'Chọn từ 1 đến 10');
  assert.equal(variantCountError(2.5), 'Chọn từ 1 đến 10');
  assert.deepEqual([...LINK_DAYS], [1, 7, 30]);
  assert.equal(LINK_DEFAULT_DAYS, 7);
});
