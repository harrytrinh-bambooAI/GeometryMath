Phiên bản: v0.2 · 2026-11-27
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

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-27 | Mục Dev đầu tiên | runner-T-261126-28 |
| v0.2 | 2026-11-27 | Mục Dev của US-S3E5-01 (Dev-S3E5-08…10) | runner-T-261126-31 |
