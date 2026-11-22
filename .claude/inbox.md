# Inbox · GeometryMath MVP · cập nhật 22/11/2026

## Việc: sửa gom lô phát hiện soát FOUNDATION (sau khi gộp E1)
- **Mục tiêu:** đóng các phát hiện còn lại của bản soát độc lập diff 1c5aa4c..4d3570a.
  1. [major, có từ trước] `app/server/src/main/resources/application.yml:3` `profiles.default: dev` → quên đặt profile khi triển khai sẽ bật `/dev/token` + khóa JWT mặc định. Đề xuất: bỏ default; máy chủ từ chối khởi động khi không có profile, hoặc khi profile `prod` mà khóa còn mặc định.
  2. [minor] `app/scripts/env.sh:9-12` bỏ dòng cuối không có newline; không lọc tên biến (chỉ nên nhận `GEO_*`, `PGBIN`).
  3. [minor] `app/web/src/shared/api/client.ts:30-31` chấp nhận `//host` → JWT có thể gửi sang nguồn khác; từ chối `//` và `://`.
  4. [minor] `app/e2e/support/fixtures.ts` che trường `token` trong http.json/ảnh bằng chứng.
  5. [minor] `app/e2e/reporters/evidence-reporter.ts`: test không chạy tới phải vào summary (interrupted/skipped); đếm test không nhân theo số mã TC; tiêu đề có mã TC sai định dạng → cảnh báo.
- **Nguồn:** bản soát độc lập 22/11/2026 (agent Sonnet), diff ở trên.
- **Xong khi:** `make verify` xanh; `npm run typecheck && npm run lint && npm run build` xanh; `make e2e` đạt; khởi động không có profile → lỗi rõ ràng.
- **Ngoài phạm vi:** tính năng nghiệp vụ; tài liệu sản phẩm.
- Đã sửa ngay (có thể xóa dữ liệu): #1 seed-reset thiếu chốt cụm, #7 tiền tố Redis — xem git log.
