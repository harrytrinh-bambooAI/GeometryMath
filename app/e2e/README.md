# Kiểm thử đầu-cuối có bằng chứng (Playwright, D-GEO-170, PO-50)

## Chạy
```
cd app && make e2e            # cách chuẩn: tự bật máy chủ (DB geo_e2e) + web (bản build, vite preview) trên GEO_PORT/GEO_WEB_PORT,
                              # reset seed vàng, chạy mọi dự án, sinh bằng chứng + truy vết, rồi tắt
make e2e ARGS="--project=api"           # chỉ một dự án
make e2e ARGS="-g @TC-GEO-0100"         # chỉ test của một TC
GEO_E2E_SKIP_BUILD=1 make e2e           # bỏ bước build (dùng jar và web/dist có sẵn)
make e2e-report                         # truy vết + mở báo cáo lượt mới nhất
```
Lần đầu: `npm ci && npm run install-browsers` trong `app/e2e` (Chromium + WebKit). Node ≥ 22.18.
Chạy tay vào máy chủ đang chạy sẵn (không reset seed): `GEO_E2E_RESET=0 GEO_E2E_BASE_URL=... GEO_E2E_WEB_URL=... npx playwright test`.

## Dự án
| Dự án | Tệp | Môi trường | Bằng chứng mỗi test |
|---|---|---|---|
| `api` | `tests/**/*.api.spec.ts` | HTTP tới máy chủ | `trace.zip`, `http.json`, `screenshot.png` (ảnh bảng yêu cầu/phản hồi) |
| `ui-chromium` | `tests/**/*.ui.spec.ts` | Chromium máy tính (giáo viên, F-GEO-102) | `screenshot.png`, `trace.zip`, `video.webm` |
| `ui-webkit` | `tests/**/*.ui.spec.ts` | WebKit iPad ngang (học sinh, F-GEO-100) — tự bật khi đã cài WebKit; tắt bằng `GEO_E2E_WEBKIT=0` | như trên |

Mọi dự án chạy với `timezoneId: Asia/Ho_Chi_Minh`, `locale: vi-VN` (BR-GEO-233).

## Quy ước viết test
- Import `test`, `expect` từ `support/fixtures.ts` (không từ `@playwright/test`): có sẵn `request` ghi bằng chứng,
  `fixture` (mã cố định từ `db/seed/fixture-ids.json`), `tokenFor('F-GEO-003')` (thẻ qua `/dev/token`), `t0` (mốc T0 của lượt).
- **Gắn mã TC vào tiêu đề**: `test('Vào lớp bằng tên và mã lớp đúng @TC-GEO-0100', ...)`; một test phủ nhiều TC thì gắn nhiều tag.
  Mã phải đúng như cột No của `artifacts/GEO/work/epics/E*/test_cases.md`. Test khung (không thuộc QC) dùng `@TC-KHUNG-nn`.
- Chọn phần tử bằng `getByTestId` theo quy ước của `app/web/README.md`; giá trị dữ liệu lấy từ fixture, không tự bịa.
- Đặt tệp theo epic: `tests/e1/...`, `tests/e2/...` để các agent không đụng nhau.

## Bằng chứng
```
evidence/<run>/index.html            bảng TC -> test -> trạng thái -> ảnh/trace/video/lỗi
evidence/<run>/summary.json          { run, status, t0, totals, tcs: { "TC-…": { status, tests: [ { project, status, evidence } ] } } }
evidence/<run>/traceability.txt|json kết quả script truy vết của lượt này
evidence/<run>/<TC-id>/<dự án>__<tên-test>/{screenshot.png, trace.zip, video.webm, http.json, error.txt}
evidence/latest.txt                  mã lượt mới nhất
```
`<run>` = `GEO_E2E_RUN` hoặc giờ Việt Nam `yyyyMMdd-HHmmss`. Thư mục `evidence/` không vào git.
Xem trace: `npx playwright show-trace evidence/<run>/<TC>/<thư mục>/trace.zip`.

## Truy vết
`npm run traceability -- [--run <mã>] [--all] [--epic E1]` (hoặc `make traceability`): đọc test_cases.md của mọi epic và tag
trong `tests/`, in theo epic: tổng TC, ứng viên E2E (tầng có "E2E", "tích hợp API" hoặc "thủ công trên thiết bị"), bắt buộc
(tầng "E2E trình duyệt"), có test, đạt, trượt, ứng viên chưa có test; liệt kê TC E2E bắt buộc chưa có test.
