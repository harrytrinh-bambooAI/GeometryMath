Phiên bản: v0.1 · 2026-11-28
# S5-E1 — phát hiện QC / Dev

## Dev
| Mã | Phát hiện | Cách xử lý tạm | Ai |
|---|---|---|---|
| Dev-S5E1-01 | AC/TC ghi đường dẫn `/api/lesson-plans/...`; quy ước app là `/api/v1/teacher/...` | Code dùng `/api/v1/teacher/lesson-plans`; mã GA-nnnn và id đều tra được | runner-T-261128-53 |
| Dev-S5E1-02 | US-S5E1-01-SC1 ghi "Tiết 1-2/3 của bài 25"; `prompt-lab/giao-an/cases.json` ghi bài 25 "Tiết 1-2/2" | Danh mục PPCT (V526) theo AC: bài 25 có "Tiết 1-2/3", "Tiết 3/3" | runner-T-261128-53 |
| Dev-S5E1-03 | Kho kiến thức nguồn (`kien-thuc.json`) có 80/80 đơn vị `nhap-tam`: mọi mã được dùng đều mang nhãn "Kiến thức chưa soát" | Giữ đúng nguồn; nhãn hiện theo từng mã | runner-T-261128-53 |
| Dev-S5E1-04 | `ai_call_log` không có cột nội dung/kết quả kiểm; AC "dòng 2 chứa lỗi … trong phần dữ liệu" | Kiểm qua tin nhắn đã gửi (móc `/test-hooks/s5e1/plans/{id}`), nhật ký kiểm lưu ở `lesson_plan.check_log` | runner-T-261128-53 |
| Dev-S5E1-05 | Câu `moi` (BR-S5E1-07) chưa nối engine; nhãn máy "Cần xem lại", đáp số trống | Ghi rõ; nối engine khi có đặc tả đề cho câu tự soạn | runner-T-261128-53 |
| Dev-S5E1-06 | `AnthropicAiProvider` dùng `max_tokens`/effort chung, chưa theo §1 prompt-final (16000, medium) cho `lesson_plan`; tệp ngoài phạm vi T-53 | Cần FIX khi bật AI thật | runner-T-261128-53 |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-28 | Mục Dev đầu tiên | runner-T-261128-53 |
