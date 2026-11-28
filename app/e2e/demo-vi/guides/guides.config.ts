import { defineConfig } from '@playwright/test';
import base from '../demo.config.ts';

/**
 * T-261128-77 (DEMO-VI) — quay clip cho bộ video HƯỚNG DẪN SỬ DỤNG (mỗi test = một video, mốc bước bằng demo.chapter):
 *   cd app/e2e && GEO_PORT=… GEO_WEB_PORT=… npx playwright test -c demo-vi/guides/guides.config.ts
 * WebM + mốc ghi vào test-results/demo-vi-guides-raw/<id>.webm|json.
 */
export default defineConfig({
  ...base,
  testDir: '.',
  testIgnore: undefined,
  outputDir: '../../test-results/demo-vi-guides-run',
});
