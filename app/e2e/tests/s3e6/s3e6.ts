import type { APIRequestContext } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { API, bearer, setUrl, type Sandbox } from '../s3e3/s3e3.ts';

/**
 * Tiện ích E2E của US-S3E6-01 (chữa bài từ Kết quả lớp) và US-S3E6-02 (xuất kết quả, lưu trữ lớp), T-261126-32.
 * Ca Kết quả lớp dựng sandbox S3-E3 (bảng F-S3-051); ca lưu trữ dựng thế giới S3-E1.
 */
export { API, bearer };

export const MSG = {
  s91: 'Lớp sẽ thấy lời giải trên màn chiếu. Máy học sinh vẫn khóa lời giải. Tiếp tục?',
  s92: 'Đề đã được sửa. Mở Trình chiếu từ đầu đề.',
  s93: (name: string) => `Lưu trữ lớp ${name}? Lớp chỉ còn xem, mã lớp ngừng dùng.`,
  s94: 'Đã xuất quá nhiều lần. Vui lòng thử lại sau ít phút.',
  s96: 'Lớp này đã lưu trữ. Em chỉ xem được.',
  chip: 'Máy học sinh vẫn khóa lời giải',
} as const;

export const T3 = 'Nhiều em xác định sai giao tuyến của (SAB) và (SCD)';

/** Giờ mở lời giải của bộ trong sandbox: âm = Đã mở, dương = Sắp mở, null = Chưa đặt giờ. */
export async function setRelease(rq: APIRequestContext, setId: string, minutesFromNow: number | null) {
  const r = await rq.post(`${API}/test-hooks/s3e6/set-release`, { data: { setId, minutesFromNow } });
  expect(r.status()).toBe(200);
}

export const presentUrl = (sb: Sandbox, position: number, ci = 0, si = 0) => `${setUrl(sb, ci, si)}/items/${position}/present`;

export const present = (rq: APIRequestContext, sb: Sandbox, token: string, position: number, step?: number) =>
  rq.post(presentUrl(sb, position), { headers: bearer(token), data: step ? { step } : {} });

export const exportUrl = (sb: Sandbox, ci = 0, si = 0) => `${setUrl(sb, ci, si)}/export`;

/** Đọc CSV xuất: bỏ BOM, tách dòng. */
export function csvLines(text: string): string[] {
  return text.replace(/^﻿/, '').split('\r\n').filter((l) => l.length > 0);
}
