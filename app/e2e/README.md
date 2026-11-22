# Kiểm thử đầu-cuối (Playwright, D-GEO-170)

Công cụ cho các case tầng "E2E/trình duyệt" (RV-GEO-35). Hiện chỉ có test khói gọi HTTP vì chưa có giao diện.

## Chạy
```
cd app && make run                     # máy chủ dev ở cổng 8080 (cụm 54329/6389)
cd app/e2e
npm ci                                 # Node 20+; KHÔNG tải trình duyệt
npm run test:smoke                     # dự án api-smoke: tests/*.api.spec.ts
GEO_E2E_BASE_URL=http://127.0.0.1:18080 npm run test:smoke   # máy chủ ở cổng khác
```
Đã chạy thử: 2/2 xanh với máy chủ ở cổng 18080.

## Khi có giao diện
- Đặt test giao diện ở `tests/*.ui.spec.ts`; chạy `npm run install-browsers` một lần, rồi
  `npx playwright test --project=ipad-webkit` (iPad/iPhone Safari = WebKit, PO-01).
- Cấu hình đã cố định `timezoneId: Asia/Ho_Chi_Minh` và `locale: vi-VN`: giao diện luôn hiển thị giờ Việt Nam
  (BR-GEO-233, `docs/CONFIG.md`); test không phụ thuộc múi giờ máy chạy.
- Thẻ đăng nhập trong test lấy qua `/dev/token` (chỉ profile `dev`) hoặc luồng đăng nhập thật khi có.

`e2e/` không nằm trong `scripts/verify.sh` (cần máy chủ đang chạy).
