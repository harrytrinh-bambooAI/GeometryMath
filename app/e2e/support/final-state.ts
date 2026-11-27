import type { Browser, Page, TestInfo } from '@playwright/test';

/**
 * Ảnh trạng thái cuối của ca LOẠI B (DECISIONS 27/11 "Chiến lược E2E"): ca đã chuyển sang luồng gọi API; cuối luồng
 * mở MỘT trang bằng Playwright (không click) rồi chụp 1 ảnh làm bằng chứng. Ảnh đính kèm tên `screenshot-trang-cuoi`
 * nên evidence-reporter chép vào thư mục TC cạnh ảnh bảng yêu cầu/phản hồi (`screenshot.png`).
 * - `open(page)`: đăng nhập sẵn (signIn của spec, chỉ nạp phiên) + `page.goto(đường dẫn)`; được chờ một phần tử
 *   `expect(...).toBeVisible()` nhưng KHÔNG được click/fill. Thẻ JWT không hiện trên trang, ảnh không chứa bí mật;
 *   thân yêu cầu/phản hồi đã che ở http.json (support/redact.ts).
 * - Trang mở ở trình duyệt máy tính (1280×800, vi-VN, Asia/Ho_Chi_Minh) trên GEO_E2E_WEB_URL.
 * Trang cuối không mở được / không hiện phần tử mong đợi thì ca TRƯỢT (kèm ghi chú `trang-cuoi-loi.txt`).
 */
export const webBase = () => process.env.GEO_E2E_WEB_URL ?? `http://127.0.0.1:${process.env.GEO_WEB_PORT ?? 5173}`;

export async function captureFinalState(browser: Browser, testInfo: TestInfo, open: (page: Page) => Promise<void>): Promise<void> {
  const ctx = await browser.newContext({
    baseURL: webBase(),
    viewport: { width: 1280, height: 800 },
    locale: 'vi-VN',
    timezoneId: 'Asia/Ho_Chi_Minh',
  });
  try {
    const page = await ctx.newPage();
    await open(page);
    await page.waitForLoadState('networkidle', { timeout: 5_000 }).catch(() => undefined);
    const png = await page.screenshot({ fullPage: true });
    await testInfo.attach('screenshot-trang-cuoi', { body: png, contentType: 'image/png' });
  } catch (e) {
    await testInfo.attach('trang-cuoi-loi.txt', { body: String((e as Error).message ?? e), contentType: 'text/plain' });
    throw e;
  } finally {
    await ctx.close();
  }
}
