import { defineConfig } from '@playwright/test';

/**
 * T-261128-75 (DEMO-VI) — cấu hình Playwright RIÊNG để quay clip cho video giới thiệu tiếng Việt (không thuộc bộ test e2e:
 * testDir là demo-vi/, tệp *.demo.ts không khớp *.spec.ts). Máy chủ bật bằng demo/stack-up.sh (T-73), cổng của worktree:
 *   cd app/e2e && GEO_PORT=… GEO_WEB_PORT=… npx playwright test -c demo-vi/demo.config.ts
 * Mỗi cảnh = một test, quay 1920x1080 (câm); WebM chép vào test-results/demo-vi-raw/<NN-tên>.webm.
 */
const apiURL = process.env.GEO_E2E_BASE_URL ?? `http://127.0.0.1:${process.env.GEO_PORT ?? 8080}`;
const webURL = process.env.GEO_E2E_WEB_URL ?? `http://127.0.0.1:${process.env.GEO_WEB_PORT ?? 5173}`;
process.env.GEO_E2E_BASE_URL = apiURL;

export default defineConfig({
  testDir: '.',
  testMatch: /.*\.demo\.ts/,
  testIgnore: /guides\//, // video hướng dẫn (T-77) quay bằng guides/guides.config.ts
  outputDir: '../test-results/demo-vi-run',
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
    // như demo/demo.config.ts (T-261128-74): màn hình ảo 1920x1080 để Trình chiếu toàn màn hình không co về 800x600
    screen: { width: 1920, height: 1080 },
    launchOptions: { args: ['--window-size=1920,1080'] },
    video: { mode: 'on', size: { width: 1920, height: 1080 } },
    actionTimeout: 15_000,
  },
});
