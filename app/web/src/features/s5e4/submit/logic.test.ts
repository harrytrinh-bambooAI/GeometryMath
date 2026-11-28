import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { canSubmit, clarityScore, isClear, isQuotaError, MSG, move, stripJpeg } from './logic.ts';

describe('nộp bài viết tay (US-S5E4-03, 09)', () => {
  it('ngưỡng độ rõ: 0,50 đạt, 0,49 và 0,31 không đạt (TC-S5E4-15)', () => {
    assert.equal(isClear(0.5), true);
    assert.equal(isClear(0.49), false);
    assert.equal(isClear(0.31), false);
    assert.equal(MSG.s32(2), 'Ảnh trang 2 hơi mờ. Em chụp lại trang này nhé.');
  });

  it('điểm độ rõ: ảnh trơn 0, nét chữ rõ cao', () => {
    const w = 32;
    const h = 32;
    const flat = new Array<number>(w * h).fill(200);
    assert.equal(clarityScore(flat, w, h), 0);
    const lines = Array.from({ length: w * h }, (_, i) => (i % w) % 4 === 0 ? 0 : 255);
    assert.equal(clarityScore(lines, w, h), 1);
  });

  it('xếp trang: kéo trang 2 lên trước rồi kéo lại', () => {
    const p = ['p1', 'p2'];
    assert.deepEqual(move(p, 1, 0), ['p2', 'p1']);
    assert.deepEqual(move(move(p, 1, 0), 1, 0), ['p1', 'p2']);
    assert.deepEqual(move(p, 0, 5), ['p1', 'p2']);
  });

  it('nút Nộp: cần đọc thông báo, 1–10 trang rõ, mỗi trang ≤ 8 MB', () => {
    const ok = { clarity: 0.9, bytes: 1000 };
    assert.equal(canSubmit(false, [ok]), false);
    assert.equal(canSubmit(true, []), false);
    assert.equal(canSubmit(true, [ok, { clarity: 0.31, bytes: 1000 }]), false);
    assert.equal(canSubmit(true, [ok, { clarity: 0.5, bytes: 8 * 1024 * 1024 }]), true);
    assert.equal(canSubmit(true, [{ clarity: 0.9, bytes: 8 * 1024 * 1024 + 1 }]), false);
    assert.equal(canSubmit(true, Array.from({ length: 11 }, () => ok)), false);
  });

  it('gỡ APP1 (EXIF) và COM khỏi JPEG, giữ APP0 và dữ liệu nén', () => {
    const jpeg = Uint8Array.from([
      0xff, 0xd8, // SOI
      0xff, 0xe0, 0x00, 0x04, 0x4a, 0x46, // APP0 (giữ)
      0xff, 0xe1, 0x00, 0x05, 0x45, 0x78, 0x69, // APP1 EXIF (bỏ)
      0xff, 0xfe, 0x00, 0x03, 0x41, // COM (bỏ)
      0xff, 0xda, 0x00, 0x02, 0x11, 0x22, 0xff, 0xd9, // SOS + dữ liệu + EOI
    ]);
    assert.deepEqual(
      [...stripJpeg(jpeg)],
      [0xff, 0xd8, 0xff, 0xe0, 0x00, 0x04, 0x4a, 0x46, 0xff, 0xda, 0x00, 0x02, 0x11, 0x22, 0xff, 0xd9],
    );
    const png = Uint8Array.from([0x89, 0x50, 0x4e, 0x47]);
    assert.equal(stripJpeg(png), png);
  });

  it('nhận ra lỗi hết chỗ lưu', () => {
    assert.equal(isQuotaError({ name: 'QuotaExceededError' }), true);
    assert.equal(isQuotaError(new Error('x')), false);
  });
});
