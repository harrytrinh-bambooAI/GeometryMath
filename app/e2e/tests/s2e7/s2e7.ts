import type { APIRequestContext, Page, TestInfo } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { K, signIn, slot, type Slot } from '../s2e6/s2e6.ts';

/**
 * Tiện ích E2E của S2-E7 (Trình chiếu, C2-04). Dữ liệu: 850_s2e67_core.sql, mỗi (TC, làn) một tổ chức (slot(7, …)).
 * Đề: FX-14 (4 bước, đã duyệt, gắn đề kho SP-01) = 101 · FX-12 chưa có lời giải = 105 · đề 30 bước = 106.
 * Bài giao: BG-1 11A1 mở T0 + 2 giờ (TC 24, 25, 26, 29, 31, 32, 33) · BG-2 11A2 đã mở (28, 31) · BG-3 11A3 chưa đặt giờ (30).
 */
export { K };

export const MSG = {
  preloaded: 'Đã tải sẵn · mất mạng vẫn chiếu',
  aiOffline: 'Hỏi đáp AI tạm tắt vì mất mạng',
  needNetwork: 'Cần mạng để mở lần đầu',
  partial: 'Chưa tải sẵn đủ, cần mạng',
  noFullscreen: 'Trình duyệt không cho toàn màn hình, nhấn F11',
  studentLocked: 'Máy học sinh vẫn khóa lời giải',
  ask: 'Lớp sẽ thấy lời giải trên màn chiếu, tiếp tục?',
  lastStep: 'Hết bước',
  noSolution: 'Chưa có lời giải',
  expired: 'Phiên đã hết. Vui lòng vào lại.',
};

export const STEP1 = 'SA ⊥ (ABCD) nên AC là hình chiếu của SC, góc cần tìm là SCA.';

/** Đăng nhập Cô Trang của ca, mở màn đề (Kho đề › đề). */
export async function openDoc(page: Page, request: APIRequestContext, info: TestInfo, tc: number, k = K.FX14, ttlSeconds?: number): Promise<Slot & { token: string }> {
  const s = slot(7, tc, info);
  const token = await signIn(page, request, s.org, s.trang, 'Cô Trang', ttlSeconds);
  await page.goto(`/gv/kho-de-cua-toi/de/${s.doc(k)}`);
  await expect(page.getByTestId('s2e6-doc')).toBeVisible();
  return { ...s, token };
}

/** Bấm "Trình chiếu" trên màn đề. */
export async function clickPresent(page: Page) {
  await page.getByTestId('s2e6-present').click();
}

/** Mở đề rồi vào Trình chiếu (đề không cần hỏi lại). */
export async function present(page: Page, request: APIRequestContext, info: TestInfo, tc: number, k = K.FX14, ttlSeconds?: number) {
  const s = await openDoc(page, request, info, tc, k, ttlSeconds);
  await clickPresent(page);
  await expect(stage(page)).toBeVisible();
  return s;
}

export const stage = (page: Page) => page.getByTestId('s2e7-stage');
export const shown = (page: Page, n: number) => expect(stage(page)).toHaveAttribute('data-shown', String(n));
export const nextBtn = (page: Page) => page.getByTestId('s2e7-next');
export const prevBtn = (page: Page) => page.getByTestId('s2e7-prev');

/** Bỏ tiêu điểm khỏi nút để phím Space không bấm nút đang chọn. */
export async function blur(page: Page) {
  await page.evaluate(() => (document.activeElement as HTMLElement | null)?.blur());
}
