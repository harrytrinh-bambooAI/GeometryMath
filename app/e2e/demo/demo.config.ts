import { defineConfig } from '@playwright/test';

/**
 * T-261128-73 (DEMO) — cấu hình Playwright RIÊNG để quay video giới thiệu (không thuộc bộ test e2e: testDir là demo/,
 * tệp *.demo.ts không khớp *.spec.ts). Chạy vào máy chủ đang bật bởi demo/stack-up.sh:
 *   cd app/e2e && npx playwright test -c demo/demo.config.ts
 * Mỗi kịch bản = một test, quay 1920x1080; video gốc (WebM) chép vào test-results/demo-raw/<NN-tên>.webm.
 */
const apiURL = process.env.GEO_E2E_BASE_URL ?? `http://127.0.0.1:${process.env.GEO_PORT ?? 8080}`;
const webURL = process.env.GEO_E2E_WEB_URL ?? `http://127.0.0.1:${process.env.GEO_WEB_PORT ?? 5173}`;
process.env.GEO_E2E_BASE_URL = apiURL;

export default defineConfig({
  testDir: '.',
  testMatch: /.*\.demo\.ts/,
  outputDir: '../test-results/demo-run',
  fullyParallel: false,
  workers: 1,
  retries: 0,
  timeout: 300_000,
  reporter: [['list']],
  use: {
    baseURL: webURL,
    locale: 'vi-VN',
    timezoneId: 'Asia/Ho_Chi_Minh',
    viewport: { width: 1920, height: 1080 },
    // T-261128-74: màn hình ảo 1920x1080 — Trình chiếu xin toàn màn hình; không đặt thì Chromium headless co trang về màn
    // mặc định 800x600 và video chỉ hiện góc trên trái + nền xám (video T-73 mốc 3:10).
    screen: { width: 1920, height: 1080 },
    launchOptions: { args: ['--window-size=1920,1080'] },
    video: { mode: 'on', size: { width: 1920, height: 1080 } },
    actionTimeout: 15_000,
  },
});
