import { defineConfig, devices } from '@playwright/test';

/**
 * Cấu hình Playwright (D-GEO-170).
 * - GEO_E2E_BASE_URL: địa chỉ máy chủ (mặc định http://127.0.0.1:8080). Máy chủ phải chạy sẵn
 *   (`make run` ở app/), Playwright không tự bật để không đụng nhầm cơ sở dữ liệu.
 * - Dự án "api-smoke": chỉ gọi HTTP, KHÔNG cần tải trình duyệt.
 * - Dự án trình duyệt (iPad/iPhone Safari = WebKit theo PO-01) bật khi có giao diện:
 *   chạy `npm run install-browsers` rồi `npx playwright test --project=ipad-webkit`.
 * - Mọi kiểm tra giờ trên giao diện chạy với múi giờ Asia/Ho_Chi_Minh (BR-GEO-233, CONFIG.md).
 */
const baseURL = process.env.GEO_E2E_BASE_URL ?? 'http://127.0.0.1:8080';

export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: [['list'], ['html', { open: 'never', outputFolder: 'playwright-report' }]],
  use: {
    baseURL,
    locale: 'vi-VN',
    timezoneId: 'Asia/Ho_Chi_Minh',
    trace: 'retain-on-failure',
  },
  projects: [
    { name: 'api-smoke', testMatch: /.*\.api\.spec\.ts/ },
    { name: 'ipad-webkit', testMatch: /.*\.ui\.spec\.ts/, use: { ...devices['iPad (gen 7)'] } },
    { name: 'iphone-webkit', testMatch: /.*\.ui\.spec\.ts/, use: { ...devices['iPhone 13'] } },
  ],
});
