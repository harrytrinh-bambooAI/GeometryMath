Các hàm SECURITY DEFINER (`03_schema.sql` §9) trả **mã trạng thái** thay cho "0 hàng / NULL / ngoại lệ" để ứng dụng phân biệt lý do mà không cần truy vấn thứ hai (RV-21). Ánh xạ sang mã lỗi API của `app/docs/CONFIG.md` (bảng mã lỗi, D-GEO-143):

| Mã trạng thái của hàm | Hàm trả | Mã lỗi API (HTTP) | Ghi chú |
|---|---|---|---|
| `OK` | mọi hàm | 200 / 201 | |
| `DUPLICATE` | `fn_record_durable`, `fn_record_qa`, `fn_create_assignment` (cùng id) | 200 như lần đầu | gửi lại cùng `request_id` hoặc cùng id bài giao (RV-18) |
| `NOT_FOUND` | `fn_get_solution`, `fn_solution_gate`, `fn_get_hint`, `fn_record_*`, `fn_set_release`, `fn_create_assignment`, `fn_create_self_assignment`, `fn_student_login`; `fn_get_solution_teacher` trả 0 hàng | `NOT_FOUND` (404) | tài nguyên của trường khác, lớp mình không thuộc, bài tự học của người khác; giáo viên xem lời giải đề tự nhập (D-GEO-175) |
| `LOCKED` | `fn_get_solution`, `fn_solution_gate`, `fn_record_durable('unlock')` | `FORBIDDEN` (403) | bài của lớp mình còn khóa, bài tự học còn khóa hoặc kế thừa khóa (BR-GEO-251); không kèm nội dung |
| `FORBIDDEN` | `fn_set_release`, `fn_create_assignment` | `FORBIDDEN` (403) | có bài/lớp trong trường nhưng người gọi không phải giáo viên của lớp |
| `HINT_DISABLED`, `QA_DISABLED` | `fn_get_hint`, `fn_record_qa` | `FORBIDDEN` (403) | giáo viên tắt trợ giúp khi giao (D-GEO-053) |
| `LEVEL_ORDER` | `fn_get_hint` | `FORBIDDEN` (403) | xin Gợi ý 2 trước Gợi ý 1 (D-GEO-108); BA có thể chọn `VALIDATION_FAILED` nếu AC muốn, ghi ở bảng thông điệp |
| `CONFLICT_ALREADY_OPEN` | `fn_set_release` | `CONFLICT` (409), MSG-GEO-48 | kèm `release_rev`, `release_at` hiện hành (D-GEO-143, RV-28) |
| `CONFLICT_REV` | `fn_set_release` | `CONFLICT` (409), MSG-GEO-49 | hai lần đổi chồng nhau; kèm rev hiện hành |
| `CONFLICT_DUPLICATE` | `fn_create_assignment` | `CONFLICT` (409), MSG-GEO-42 | cùng `problem` cho cùng lớp, bài chưa lưu trữ (D-GEO-161, chỉ mục duy nhất một phần) |
| `VALIDATION_PAST` | `fn_set_release`, `fn_create_assignment`, `fn_issue_teacher_code` | `VALIDATION_FAILED` (400), MSG-GEO-43 | giờ mở không ở tương lai |
| `VALIDATION_KIND` | `fn_record_batch`, `fn_record_durable`, `fn_get_hint` | `VALIDATION_FAILED` (400) | lô mức A mang sự kiện mức B, thiếu `request_id`, mức gợi ý ngoài {1, 2} |
| `NO_SOLUTION`, `NO_HINT` | `fn_get_solution`, `fn_get_hint` | `NOT_FOUND` (404) | dữ liệu nội dung thiếu (không xảy ra với đề kho đã nạp đủ) |
| `RATE_LIMITED` | `fn_teacher_login` | `RATE_LIMITED` (429) + `Retry-After`, MSG-GEO-59 | 10 lần sai/15 phút theo IP (D-GEO-152, 173) |
| `INVALID_CODE` | `fn_teacher_login` | `UNAUTHENTICATED` (401), MSG-GEO-12 | sai, thu hồi, hết hạn: một thông báo chung |

Ghi chú: trạng thái "Đã mở" của bài giao không bao giờ được đệm thay cho hàm (D-GEO-157); điểm cuối trạng thái (Q04b) không trả lời giải và không trả quyết định.
