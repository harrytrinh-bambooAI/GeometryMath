import { existsSync } from 'node:fs';
import { defineConfig, devices, webkit } from '@playwright/test';
import type { Project } from '@playwright/test';

/**
 * Cấu hình Playwright (D-GEO-170, PO-50). Cách chạy đầy đủ: `cd app && make e2e` (tự bật máy chủ + web).
 * - GEO_E2E_BASE_URL: máy chủ API (mặc định http://127.0.0.1:${GEO_PORT:-8080}).
 * - GEO_E2E_WEB_URL: giao diện (mặc định http://127.0.0.1:${GEO_WEB_PORT:-5173}).
 * - GEO_E2E_RUN: mã lượt chạy = tên thư mục bằng chứng evidence/<run>/ (mặc định giờ VN yyyyMMdd-HHmmss).
 * - GEO_E2E_RESET=0: bỏ bước reset seed vàng ở globalSetup (khi chạy vào máy chủ không dùng geo_e2e).
 * - GEO_E2E_WEBKIT=0: tắt dự án WebKit (mặc định bật nếu trình duyệt WebKit đã cài).
 * Dự án:
 * - `api`        tests/**\/*.api.spec.ts, chỉ HTTP; bằng chứng: trace + ảnh chụp bảng yêu cầu/phản hồi + http.json.
 * - `ui-chromium` tests/**\/*.ui.spec.ts trên Chromium máy tính (giáo viên, F-GEO-102); ảnh chụp + trace + video.
 * - `ui-webkit`  cùng test trên WebKit iPad màn ngang (học sinh, F-GEO-100; iPad Safari = WebKit theo PO-01).
 * - `ui-iphone`  tests/**\/*.iphone.spec.ts trên WebKit iPhone màn dọc (F-GEO-101; E3 US-GEO-306).
 * - `mobile-iphone` (WebKit iPhone 13) và `mobile-pixel` (Chromium Pixel 7): các spec UI P0 trong MOBILE_P0 (T-261126-02).
 * Mọi kiểm tra giờ chạy với múi giờ Asia/Ho_Chi_Minh (BR-GEO-233).
 */
function vnStamp(): string {
  const p = Object.fromEntries(
    new Intl.DateTimeFormat('en-GB', {
      timeZone: 'Asia/Ho_Chi_Minh', year: 'numeric', month: '2-digit', day: '2-digit',
      hour: '2-digit', minute: '2-digit', second: '2-digit', hourCycle: 'h23',
    }).formatToParts(new Date()).map((x) => [x.type, x.value]),
  );
  return `${p.year}${p.month}${p.day}-${p.hour}${p.minute}${p.second}`;
}

// Đặt một lần ở tiến trình chính; tiến trình con (worker) thừa hưởng biến môi trường nên cùng mã lượt.
process.env.GEO_E2E_RUN ??= vnStamp();
const run = process.env.GEO_E2E_RUN;
const apiURL = process.env.GEO_E2E_BASE_URL ?? `http://127.0.0.1:${process.env.GEO_PORT ?? 8080}`;
const webURL = process.env.GEO_E2E_WEB_URL ?? `http://127.0.0.1:${process.env.GEO_WEB_PORT ?? 5173}`;

const webkitInstalled = (() => {
  try {
    return existsSync(webkit.executablePath());
  } catch {
    return false;
  }
})();
const withWebkit = process.env.GEO_E2E_WEBKIT !== '0' && webkitInstalled;

const uiEvidence = { screenshot: 'on', video: 'on', trace: 'on' } as const;
/**
 * Spec UI P0 chạy thêm trên điện thoại (T-261126-02): học sinh vào lớp, làm bài (nhãn, hình), gợi ý, lời giải đã mở,
 * hỏi đáp; giáo viên Kho đề, chi tiết đề, giao bài, giờ mở, theo dõi lớp.
 */
const MOBILE_P0 =
  /(e1\/student-join|e2\/labels|e4\/(hints|solution|qa)|e5\/(catalog|detail)|e6\/(assign|release)|e7\/monitor)\.ui\.spec\.ts$/;
const projects: Project[] = [
  { name: 'api', testMatch: /.*\.api\.spec\.ts/, use: { baseURL: apiURL } },
  { name: 'ui-chromium', testMatch: /.*\.ui\.spec\.ts/, use: { ...devices['Desktop Chrome'], baseURL: webURL, ...uiEvidence } },
];
if (withWebkit) {
  projects.push({
    name: 'ui-webkit',
    testMatch: /.*\.ui\.spec\.ts/,
    use: { ...devices['iPad (gen 7) landscape'], baseURL: webURL, ...uiEvidence },
  });
  // E3 (US-GEO-306): bố cục điện thoại trên WebKit iPhone màn dọc (F-GEO-101); chỉ các tệp *.iphone.spec.ts
  projects.push({
    name: 'ui-iphone',
    testMatch: /.*\.iphone\.spec\.ts/,
    use: { ...devices['iPhone 13'], baseURL: webURL, ...uiEvidence },
  });
  // T-261126-02 (PO-60, MVP.md §3 "Điện thoại · Làm bài", §9 "toàn bộ P0 chạy trên điện thoại"): spec UI P0 trên iPhone
  projects.push({ name: 'mobile-iphone', testMatch: MOBILE_P0, use: { ...devices['iPhone 13'], baseURL: webURL, ...uiEvidence } });
}
// Cùng các spec P0 trên điện thoại Android (Chromium, Pixel 7): chạy cả khi máy không cài WebKit.
projects.push({ name: 'mobile-pixel', testMatch: MOBILE_P0, use: { ...devices['Pixel 7'], baseURL: webURL, ...uiEvidence } });

export default defineConfig({
  testDir: './tests',
  outputDir: `test-results/${run}`,
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  globalSetup: './support/global-setup.ts',
  reporter: [['list'], ['./reporters/evidence-reporter.ts', { outDir: 'evidence' }]],
  use: {
    baseURL: apiURL,
    locale: 'vi-VN',
    timezoneId: 'Asia/Ho_Chi_Minh',
    trace: 'on',
    extraHTTPHeaders: { 'X-E2E-Run': run },
  },
  projects,
});
