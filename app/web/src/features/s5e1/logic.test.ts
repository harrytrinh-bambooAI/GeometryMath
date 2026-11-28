import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { EMPTY, MSG_NOTE, MSG_RATIO, MSG_REQUIRED, choosePeriod, intOrNull, sectionLabel, validate, versionLine, type CatalogChapter } from './logic.ts';

const chapters: CatalogChapter[] = [
  {
    no: 7,
    label: 'Chương VII',
    title: 'Quan hệ vuông góc trong không gian',
    lessons: [
      {
        no: 25,
        title: 'Bài 25. Hai mặt phẳng vuông góc',
        periods: [{ id: 'B25-T1-2', label: 'Tiết 1-2/3', periods: 2, focus: ['KT11-C07-DN-007', 'KT11-C07-DN-008'] }],
        knowledge: [],
      },
      {
        no: 26,
        title: 'Bài 26. Khoảng cách',
        periods: [{ id: 'B26-T1', label: 'Tiết 1/2', periods: 1, focus: ['KT11-C07-DN-010', 'KT11-C07-CT-003'] }],
        knowledge: [],
      },
    ],
  },
];

describe('S5-E1 form soạn giáo án', () => {
  it('đổi Tiết PPCT thì số tiết và kiến thức tích sẵn được điền lại (US-S5E1-01-SC1)', () => {
    const a = choosePeriod(EMPTY, chapters, 'B26-T1');
    assert.equal(a.soTiet, 1);
    assert.deepEqual(a.kienThuc, ['KT11-C07-DN-010', 'KT11-C07-CT-003']);
    assert.equal(a.phongCach, 'truyen-thong');
    const b = choosePeriod(a, chapters, 'B25-T1-2');
    assert.equal(b.soTiet, 2);
    assert.deepEqual(b.kienThuc, ['KT11-C07-DN-007', 'KT11-C07-DN-008']);
  });

  it('thiếu trường B thì báo MSG-S5-01 đúng trường', () => {
    const e = validate(choosePeriod(EMPTY, chapters, 'B26-T1'));
    assert.deepEqual(e, { doiTuong: MSG_REQUIRED });
    assert.deepEqual(validate({ ...EMPTY, doiTuong: 'kha' }), { tietPpct: MSG_REQUIRED, kienThuc: MSG_REQUIRED });
  });

  it('tổng tỉ lệ khác số bài tập và ghi chú quá 500 ký tự bị chặn', () => {
    const s = { ...choosePeriod(EMPTY, chapters, 'B26-T1'), doiTuong: 'kha' };
    assert.deepEqual(validate({ ...s, soBaiTap: 6, tiLe: { NB: 2, TH: 2, VD: 1, VDC: 2 } }), { tiLe: MSG_RATIO });
    assert.deepEqual(validate({ ...s, soBaiTap: 6, tiLe: { NB: 2, TH: 2, VD: 1, VDC: 1 } }), {});
    assert.deepEqual(validate({ ...s, ghiChu: 'a'.repeat(501) }), { ghiChu: MSG_NOTE });
    assert.deepEqual(validate({ ...s, ghiChu: 'a'.repeat(500) }), {});
  });

  it('ô số trống là null', () => {
    assert.equal(intOrNull(''), null);
    assert.equal(intOrNull('3'), 3);
    assert.equal(intOrNull('2.5'), null);
  });
});

describe('chat vá, duyệt (T-261128-54)', () => {
  it('nhãn mục', () => {
    assert.equal(sectionLabel('muc_tieu'), 'I. Mục tiêu');
    assert.equal(sectionLabel('thiet_bi'), 'II. Thiết bị');
    assert.equal(sectionLabel('HD3'), 'HĐ3');
    assert.equal(sectionLabel('PHT1'), 'Phiếu PHT1');
  });
  it('dòng phiên bản', () => {
    assert.equal(versionLine(1, null), 'Phiên bản 1');
    assert.equal(versionLine(2, 2), 'Phiên bản 2 · đã duyệt');
    assert.equal(versionLine(3, 2), 'Phiên bản 3 · đã duyệt phiên bản 2');
  });
});
