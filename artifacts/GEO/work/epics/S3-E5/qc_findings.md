Phiên bản: v0.3 · 2026-11-27
# S3-E5 — phát hiện QC / Dev

## Dev (runner-T-261126-28: US-S3E5-02, 04)
| Mã | Nội dung | Mức | Xử lý TẠM |
|---|---|---|---|
| Dev-S3E5-01 | Văn bản bản 1.0 (V110, S2-E1) thiếu ý BR-S3E5-25 (lớp, giáo viên đặt lại mật khẩu). V315 sửa nội dung bản 1.0 tại chỗ, KHÔNG tăng phiên bản (tăng bản buộc mọi tài khoản đọc lại, làm đổ ca S2). Bản 1.1 chỉ có ở dữ liệu dev (`911_s3e1_classes.sql`, không hiện hành); ca SC4/FC4 bật 1.1 riêng cho một người qua móc `/test-hooks/s3e5/privacy/current`. | Vừa | PO xác nhận sửa tại chỗ hay phát hành 1.1. |
| Dev-S3E5-02 | Cổng "chưa đọc thì không dùng API khác" (BR-S3E5-24) là bộ lọc `PrivacyGateFilter`: FORBIDDEN, MSG-S3-84, `details` [{field: privacy, message: PRIVACY_ACK_REQUIRED}]; trừ `/api/v1/auth/**`, `/api/v1/account/**`. Tập "phải đọc" nạp lúc khởi động (như mốc hủy phiên), không thêm câu SQL mỗi yêu cầu; tài khoản vừa chuyển bằng lệnh MVP (tiến trình riêng) bị chặn sau khi máy chủ khởi động lại. | Vừa | TẠM (một nút). |
| Dev-S3E5-03 | Màn thông báo hiện khi vào trang đầu sau đăng nhập (bọc route `/hs/bai-cua-em`, `/gv/kho-de` ở `features/s3e5`); trang khác gặp FORBIDDEN của máy chủ. | Thấp | TẠM. |
| Dev-S3E5-04 | TC-S3E5-22: màn Đăng ký S2-E1 khóa nút "Đăng ký" khi chưa tick nên không hiện MSG-S3-84 dưới hộp tick; máy chủ trả MSG-S3-84 (đã đổi `CodeAuthService.MSG_PRIVACY`). Sửa giao diện thuộc S2-E1. | Thấp | FIX S2-E1 nếu cần đúng chữ. |
| Dev-S3E5-05 | Công tắc AI chặn ở `aigateway/internal/web/ClassAiSwitchFilter` trên lượt hỏi trợ lý của học sinh (`POST /api/v1/student/assignments/{id}/questions`, đường AI duy nhất có ngữ cảnh lớp; gợi ý bài lớp lấy từ lời giải đã duyệt, không gọi AI) — FORBIDDEN, MSG-S3-83, `details` AI_OFF_FOR_CLASS, không lượt gọi nhà cung cấp. Giao diện ẩn khung Hỏi đáp của màn `/hs/bai/:id` (bọc route E1). Màn làm bài luyện S2-E8 không có khung trợ lý. | Thấp | — |
| Dev-S3E5-06 | TC-S3E5-51 ("AI gộp" giáo viên): "Cả lớp vướng ở đâu" thuộc S3-E3 (task 30), chưa có; IT kiểm công tắc chỉ áp vai học sinh. | Thấp | Task 30 kiểm lại. |
| Dev-S3E5-07 | TC-S3E5-42 "ô trên Kết quả lớp cập nhật", "mở thông báo": Kết quả lớp (task 30) và thông báo (task 31) chưa có; ca kiểm Bài của em, mở đề, gợi ý mức 1, gửi đáp số, 0 lượt gọi AI. | Thấp | Task 30/31. |

## Dev (runner-T-261126-31: US-S3E5-01 AuditEvent)
| Mã | Nội dung | Mức | Xử lý TẠM |
|---|---|---|---|
| Dev-S3E5-08 | TC-S3E5-07 (BR-S3E5-04) với đặt lại mật khẩu: iam (T-28) phát `StudentPasswordResetEvent` SAU khi giao dịch đổi mật khẩu đã ghi, nên khi ghi AuditEvent lỗi thì máy chủ trả lỗi, không có AuditEvent nhưng mật khẩu ĐÃ đổi. Mở ngay / Đổi giờ (assignment) cùng giao dịch nên quay lui trọn. `AuditFailureS3E5IT` chỉ khẳng định phần đúng được. | Vừa | FIX iam: phát sự kiện trong giao dịch (hoặc gọi ghi AuditEvent trước khi commit). |
| Dev-S3E5-09 | TC-S3E5-14: bản ghi "Từ chối" của lần đặt lại thứ 11 (RATE_LIMITED) do luồng giới hạn tần suất của iam (T-28) quyết định; spec chỉ kiểm có đúng 10 PASSWORD_RESET thành công. | Thấp | — |
| Dev-S3E5-10 | AuditEvent ghi bằng `fn_audit_event` (V386) trên `audit_log` (A8), chỉ ba loại EARLY_RELEASE, RELEASE_TIME_CHANGE, PASSWORD_RESET; bản ghi Từ chối ghi ở giao dịch riêng (vẫn còn khi thao tác bị từ chối). Móc đọc bản ghi Từ chối `/test-hooks/s3e4/denied` chỉ profile dev/test. | Thấp | — |

## Dev (runner-T-261126-32: US-S3E5-03, 05)
| Mã | Nội dung | Mức | Xử lý TẠM |
|---|---|---|---|
| Dev-S3E5-11 | BR-S3E5-37 lệch MVP: bảng `auth_failure.ip_key` (V10, D-GEO-152) lưu HMAC địa chỉ mạng ở CSDL, dọn sau 1 ngày; BR-37 chỉ cho khóa băm ở bộ nhớ tạm ≤ 1 giờ. Ghi là ngoại lệ có tên trong `allowed_fields` (PO-53). Không có IP rõ ở bất kỳ bảng nào (TC-37 quét giá trị). | Vừa | PO quyết: giữ ngoại lệ hay FIX iam chuyển sang Redis ≤ 1 giờ. |
| Dev-S3E5-12 | Tệp danh sách `allowed_fields` ở `app/server/src/test/java/vn/geo/server/quality/allowed_fields.tsv` (phạm vi tệp của task không có `src/test/resources`); 1 018 dòng: `db` (bảng.cột, bảng con phân vùng tính theo bảng cha) và `api` (khóa JSON phẳng, mọi cấp). Lý do ghi theo bảng; mã quyết định theo nguồn bảng (MVP PO-60, định danh PO-61, riêng tư PO-64, S2 PO-65, S3 PO-66). Ngoại lệ có tên: `problem_draft.image_*`, khóa `image`/`hasImage` (PO-65 Q4), `auth_failure.ip_key`. Khóa sự kiện đo: `event_log` chỉ có `kind` + `arg` số nhỏ, quét như cột. | Thấp | — |
| Dev-S3E5-13 | BR-S3E5-34 (`RequestFieldGuard`): thân yêu cầu kiểu record có khóa cấp một ngoài record → VALIDATION_FAILED, `details[0].field` = khóa. Ngoại lệ hợp đồng MVP TC-GEO-0119 (lượt hỏi trợ lý, test MVP kỳ vọng 200 khi gửi kèm `orgId` và chỉ kiểm không tới nhà cung cấp): giữ "bỏ qua" cho đúng một đường. Thân tự do (Map/JsonNode) và khóa lồng không xét. | Vừa | FIX: đổi kỳ vọng TC-GEO-0119 sang 400 rồi bỏ ngoại lệ. Rủi ro: ca E2E cũ gửi khóa thừa sẽ đỏ ở lượt e2e tích hợp. |
| Dev-S3E5-14 | Test chặn trường mới: quét tĩnh mọi `@RestController` (không gắn `@Profile`) trong `make test` + quét `information_schema` và gọi thật 26 API GET chặng 3 bằng F-S3 trong IT. Kiểu trả về tự do (Map) ngoài các API đã gọi không được phủ tĩnh. | Thấp | — |
| Dev-S3E5-15 | Xóa tài khoản: có hiệu lực ngay (bia mộ "Đã xóa", hết mã, mật khẩu, mã khôi phục, cờ đã đọc; mọi phiên hủy) và xóa NGAY bài làm, lượt hỏi AI, thành viên lớp, thông báo, nhật ký đo (≤ 30 ngày). Công việc dọn hằng ngày (`geo.s3e5.account-purge-interval`) xóa hẳn bia mộ ở T+30 nếu không còn nội dung dùng chung trỏ tới; giáo viên còn đề/lời giải/lớp đã lưu trữ giữ bia mộ ẩn danh. Mã người dùng không cấp lại: chỉ giữ SHA-256 ở `retired_user_code`. | Vừa | PO xác nhận nội dung dùng chung của giáo viên đã xóa (đề trong Kho chung) được giữ ẩn danh. |
| Dev-S3E5-16 | AuditEvent của người đã xóa: mã người dùng (người làm hoặc đối tượng) thay bằng UUID 0 ("đã xóa"); `fn_account_purge_rows` tạm tắt trigger chỉ-thêm bằng `session_replication_role` (cần chủ migration là superuser như hiện tại). Danh sách AuditEvent của S3-E4 (`AuditEventRepository`, `ReleaseRepository`) JOIN `app_user` nên dòng có người làm đã xóa sẽ không hiện. | Vừa | FIX S3-E4: LEFT JOIN, hiện "đã xóa". |
| Dev-S3E5-17 | BR-S3E5-55 idempotent: lần gọi lại sau khi đã xóa gặp 401 (thẻ đã hủy) thay vì 200 như lần đầu; `requestId` nhận nhưng không lưu. Nút giao diện khóa khi đang gửi. | Thấp | TẠM. |
| Dev-S3E5-18 | "Xóa tài khoản" đặt ở Cài đặt bằng route `cai-dat` của `features/s3e5` (bọc màn Cài đặt chung, không sửa `features/settings`); màn xem lại thông báo `/gv|hs/rieng-tu`; liên kết "Cài đặt › Xóa tài khoản" ở cả màn cổng thông báo. | Thấp | — |
| Dev-S3E5-19 | Câu lỗi tên hiển thị (S2-E1) đổi sang MSG-S3-82 "…không nên là email…" (trước: "không được"); văn bản thông báo 1.0 sửa tại chỗ ở V415 thêm chính sách lưu/xóa (như Dev-S3E5-01, không tăng bản). | Thấp | PO xác nhận như Dev-S3E5-01. |
| Dev-S3E5-20 | TC-S3E1-33 (S3-E1, ngoài phạm vi tệp) vẫn giả lập bằng móc `deactivate`; nay có xóa thật (`POST /api/v1/account/users/{id}/delete`) và xóa ngay `class_member` nên duyệt em đã xóa trả NOT_FOUND. | Thấp | FIX S3-E1 đổi ca sang xóa thật nếu muốn. |
| Dev-S3E5-21 | TC-S3E5-32 (nhật ký) làm ở IT: bắt nhật ký ứng dụng trong lúc gửi email/điện thoại/câu hỏi; chưa quét nhật ký của cả lượt E2E chặng 3. | Thấp | Phiên chính có thể thêm bước quét log máy chủ sau e2e. |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-27 | Mục Dev đầu tiên | runner-T-261126-28 |
| v0.2 | 2026-11-27 | Mục Dev của US-S3E5-01 (Dev-S3E5-08…10) | runner-T-261126-31 |
| v0.3 | 2026-11-27 | Mục Dev của US-S3E5-03, 05 (Dev-S3E5-11…21) | runner-T-261126-32 |
