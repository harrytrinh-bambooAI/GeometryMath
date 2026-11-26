Phiên bản: v0.1 · 2026-11-26
# S2-E1 — phát hiện khi code và kiểm thử (QC findings)

## Dev

| Mã | Chỗ | Phát hiện | Cách xử lý đang dùng (quyết định · tin cậy · nguồn) | Trạng thái |
|---|---|---|---|---|
| DEV-S2E1-01 | US-S2E1-02-SC2, TC-S2E1-020 | "Mở Kho đề với ngăn Của tôi": ngăn "Của tôi" thuộc S2-E6 (A10), chưa có | Ca kiểm xác nhận vào Kho đề (`/gv/kho-de`, tên giáo viên ở góc); phần ngăn kiểm ở S2-E6 · vừa · PLAN A10 | Open |
| DEV-S2E1-02 | US-S2E1-02-FC5, TC-S2E1-030 | "Bấm vào một bài trong Bài của em": học sinh tự đăng ký (Tự học) chưa có bài lớp | Ca kiểm bấm mục rail "Bài của em" (gọi lại máy chủ) sau khi phiên bị hủy; hành vi kiểm (401 UNAUTHENTICATED, câu "Phiên đã hết. Vui lòng vào lại.", về màn Đăng nhập) giữ nguyên · vừa · AC | Open |
| DEV-S2E1-03 | US-S2E1-01-BV7, TC-S2E1-016 | "Chờ theo Retry-After" (tối đa 1 giờ) không chờ thật được trong E2E | Móc dev/test `/test-hooks/s2e1/register-limit/age` lùi mốc của đúng IP đó một khoảng bằng Retry-After · cao · PO-51 (móc chỉ dev/test) | Open |
| DEV-S2E1-04 | US-S2E1-04-SC4, TC-S2E1-043 | Ca UI chạy trên 2 dự án (chromium, iPad) nên không dùng chung một tài khoản chuyển từ MVP | Móc dev/test `/test-hooks/s2e1/temp-password` đưa tài khoản mới đăng ký về trạng thái "vừa chuyển" (mật khẩu tạm + phải đổi); lệnh chuyển thật kiểm ở IT TC-S2E1-039…042 · vừa | Open |
| DEV-S2E1-05 | BR-S2E1-16 | Luật tên giống email/số điện thoại chi tiết ở BR-S3E5-35 (S3-E5) chưa code | TẠM: chặn tên có dạng `x@y.z` hoặc ≥ 9 chữ số (bỏ dấu cách, chấm, ngoặc, +, -) · thấp · BR-S2E1-16 | Open |
| DEV-S2E1-06 | BR-S2E1-11, màn khôi phục | Câu khi khóa đường khôi phục chưa có trong AC | "Đã thử sai mã khôi phục quá 10 lần. Thử lại sau N phút." (RATE_LIMITED + Retry-After); không hiện "còn N lần" để không lộ mã có tồn tại · vừa · DQ-18 | Open |
| DEV-S2E1-07 | Seed | `privacy_notice` không có trong `app/db/seed/keep-tables.txt` (ngoài phạm vi tệp của T-18) nên reset seed xóa văn bản 1.0 | Seed `800_s2e1_core.sql` nạp lại văn bản 1.0; đề nghị FIX thêm `privacy_notice` vào keep-tables · cao | Open |
| DEV-S2E1-08 | Điều hướng | Trang chủ (`app/web/src/pages/HomePage.tsx`, ngoài phạm vi) chưa có liên kết tới màn Đăng ký / Đăng nhập bằng mã | Màn ở `/gv|/hs/dang-ky`, `/dang-nhap-ma`, `/khoi-phuc`, `/dat-mat-khau`; đề nghị FIX thêm liên kết ở trang chủ · vừa | Open |
| DEV-S2E1-09 | Kiến trúc | Bộ đếm đăng ký theo IP và mốc hủy phiên giữ trong bộ nhớ của nút (MVP một nút) | Nhiều nút: chuyển bộ đếm sang Redis TTL 1 giờ, phát sự kiện hủy phiên qua Redis · vừa · BR-S3E5-37 | Open |
| DEV-S2E1-10 | Dữ liệu | Tài khoản tự đăng ký cần một trường (app_user.org_id NOT NULL) nhưng PO-61 không gắn trường | TẠM: trường "Cộng đồng" kiểu `group` (con của platform, id `geo.s2e1.community-org-id`); kiểu `school` làm đỏ TC-GEO-0159/0604 (đếm trường mẫu, bản trình diễn chỉ còn THPT Mẫu A) · vừa · PO-61 | Open |
| DEV-S2E1-11 | Quyền CSDL | geo_app không được INSERT app_user (TC-GEO-0863) | Đăng ký đi qua hàm SECURITY DEFINER `fn_register_code_user` (chỉ student/light hoặc teacher/verified, luôn có mã + băm) · cao · D-GEO-158 | Đã xử lý |
| DEV-S2E1-12 | Bằng chứng E2E | `app/e2e/reporters/evidence-reporter.ts` (ngoài phạm vi T-18) chỉ nhận tag `@TC-GEO-nnnn`/`@TC-KHUNG-n` (dòng `TC_TAG`): tag `@TC-S2E1-nnn` bị cảnh báo "không đúng định dạng", không vào `summary.json` nên truy vết ghi E2E của S2-E1 là "chưa chạy" (--strict vẫn exit 0 vì 0 thiếu, 0 trượt) | Đề nghị FIX: `TC_TAG = /^@(TC-GEO-\d{4}|TC-S[23]E\d+-\d+|TC-KHUNG-\d+)$/`; kết quả từng ca S2-E1 hiện đọc ở danh sách Playwright của lượt · cao | Open |
| DEV-S2E1-13 | Theme T-50 | Gộp theme C (Giấy kẻ ô) | Màn s2e1 chỉ dùng token; ô nhập/nút/chọn vai ≥ 44 px (`--touch`, `--ctl-h`), mã và ô nhập dùng `--font-math`; ca `layout.p0m.ui.spec.ts` kiểm không tràn ngang và ≥ 44 px trên chromium, iPad, iPhone, Pixel (ảnh đính kèm) · cao | Đã xử lý |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mục Dev khi code S2-E1 | runner-T-261126-18 |
