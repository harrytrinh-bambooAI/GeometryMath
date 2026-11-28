import { existsSync } from 'node:fs';
import { availableParallelism } from 'node:os';
import { defineConfig, devices, webkit } from '@playwright/test';
import type { Project } from '@playwright/test';

/**
 * Cấu hình Playwright (D-GEO-170, PO-50). Cách chạy đầy đủ: `cd app && make e2e` (tự bật máy chủ + web).
 * - GEO_E2E_BASE_URL: máy chủ API (mặc định http://127.0.0.1:${GEO_PORT:-8080}).
 * - GEO_E2E_WEB_URL: giao diện (mặc định http://127.0.0.1:${GEO_WEB_PORT:-5173}).
 * - GEO_E2E_RUN: mã lượt chạy = tên thư mục bằng chứng evidence/<run>/ (mặc định giờ VN yyyyMMdd-HHmmss).
 * - GEO_E2E_RESET=0: bỏ bước reset seed vàng ở globalSetup (khi chạy vào máy chủ không dùng geo_e2e).
 * - GEO_E2E_WEBKIT=0: tắt dự án WebKit (mặc định bật nếu trình duyệt WebKit đã cài).
 * - GEO_E2E_EVIDENCE=full|fail: bằng chứng video + trace cho mọi test (full) hay chỉ test trượt (fail, mặc định).
 * - GEO_E2E_RETRIES: số lần thử lại của dự án UI/mobile (ui-*, mobile-*); mặc định 1 (E2E-SPLIT 27/11, T-33 đề xuất).
 *   Dự án `api` KHÔNG thử lại (lỗi API là lỗi thật). Ca đạt sau retry = "flaky": reporter đếm riêng (totals.flaky,
 *   flakyTests ở summary.json), vẫn tính đạt, không lẫn với đạt ngay. GEO_E2E_RETRIES=0 để tắt.
 * - GEO_E2E_WORKERS: số worker Playwright (mặc định nửa số lõi, tối thiểu 2; T-261126-43). Máy dùng chung đang tải cao
 *   thì hạ xuống (ví dụ 2) thay vì nới thời gian chờ. Retries chỉ cho UI/mobile (xem GEO_E2E_RETRIES), luôn báo riêng ca flaky.
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
function e2eWorkers(): number {
  const v = Number.parseInt(process.env.GEO_E2E_WORKERS ?? '', 10);
  if (Number.isFinite(v) && v > 0) return v;
  return Math.max(2, Math.floor(availableParallelism() / 2));
}

const uiRetries = (() => {
  const v = Number.parseInt(process.env.GEO_E2E_RETRIES ?? '', 10);
  return Number.isFinite(v) && v >= 0 ? v : 1;
})();
const withWebkit = process.env.GEO_E2E_WEBKIT !== '0' && webkitInstalled;

/**
 * GEO_E2E_EVIDENCE (T-261126-43): `fail` (mặc định, lượt chạy của task Dev) = ảnh chụp mọi test, video + trace chỉ giữ
 * khi trượt; `full` = như trước (video + trace mọi test), dùng ở gate tester và khi cần báo cáo bằng chứng đủ.
 * Lý do: video (ffmpeg) + trace cho MỌI test UI là phần tải lớn nhất khi máy dùng chung đang tải cao.
 * `off` (MẶC ĐỊNH từ 27/11, lệnh owner "chưa cần evidence" tới hết code chặng 3): không ảnh, không video, không trace;
 * reporter vẫn ghi summary.json + kết quả từng TC (traceability cần). Muốn có bằng chứng: GEO_E2E_EVIDENCE=fail|full.
 */
const evidenceMode = process.env.GEO_E2E_EVIDENCE ?? 'off';
const evidenceFull = evidenceMode === 'full';
const evidenceOff = evidenceMode === 'off';
const keep = evidenceOff ? 'off' : evidenceFull ? 'on' : 'retain-on-failure';
const uiEvidence = { screenshot: evidenceOff ? 'off' : 'on', video: keep, trace: keep } as const;
/**
 * Spec UI P0 chạy thêm trên điện thoại (T-261126-02): học sinh vào lớp, làm bài (nhãn, hình), gợi ý, lời giải đã mở,
 * hỏi đáp; giáo viên Kho đề, chi tiết đề, giao bài, giờ mở, theo dõi lớp.
 * Chặng 2–3 (T-261126-41): spec P0 cần chạy trên điện thoại đặt tên `*.p0m.ui.spec.ts` (vẫn chạy cả ui-chromium,
 * ui-webkit vì khớp `.ui.spec.ts`); task không sửa tệp cấu hình này.
 */
const MOBILE_P0_MVP =
  /(e1\/student-join|e2\/labels|e4\/(hints|solution|qa)|e5\/(catalog|detail)|e6\/(assign|release)|e7\/monitor)\.ui\.spec\.ts$/;
const MOBILE_P0 = [MOBILE_P0_MVP, /\.p0m\.ui\.spec\.ts$/];
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

// Chỉ dự án UI/mobile được thử lại (E2E-SPLIT): ca UI chập chờn do tải máy; ca API loại B không thử lại.
for (const p of projects) if (p.name !== 'api') p.retries = uiRetries;

export default defineConfig({
  testDir: './tests',
  outputDir: `test-results/${run}`,
  fullyParallel: true,
  workers: e2eWorkers(),
  forbidOnly: !!process.env.CI,
  retries: 0, // dự án UI/mobile đặt retries riêng bên dưới (uiRetries); api không thử lại
  globalSetup: './support/global-setup.ts',
  reporter: [['list'], ['./reporters/evidence-reporter.ts', { outDir: 'evidence' }]],
  use: {
    baseURL: apiURL,
    locale: 'vi-VN',
    timezoneId: 'Asia/Ho_Chi_Minh',
    trace: keep,
    extraHTTPHeaders: { 'X-E2E-Run': run },
  },
  projects,
});
