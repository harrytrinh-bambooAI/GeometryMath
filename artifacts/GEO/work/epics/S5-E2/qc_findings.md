Phiên bản: v0.1 · 2026-11-28
# S5-E2 — phát hiện QC / Dev

## Dev
| Mã | Phát hiện | Xử lý TẠM | Cần ai |
|---|---|---|---|
| Dev-S5E2-01 | "Đường đọc–dựng–giải–kiểm của S2-E3" (`content/internal/draft`, bất đồng bộ, lưu sổ lời giải) thuộc module content, không gọi chéo được: đề nháp đi qua lõi của đường đó trong `geometry.api.GeometryEngine` — AI trả mô tả dựng {template, params, asked} + đáp số; engine `specFrom` → `build` → `measure` (đáp số engine) → `verify` (so đáp số AI). Bộ dựng lạ / dựng lỗi = "Cần xem lại · máy không dựng được hình"; lệch = "đáp số AI khác máy tính". | TẠM | ARCH/PO xác nhận |
| Dev-S5E2-02 | Ngân hàng "Của tôi" của trợ lý = đề nháp đã duyệt (`exam_draft_item.status = approved`, `GET /api/copilot/exam-drafts/bank`); chưa trộn vào `exam_bank_item` (ngân hàng chung đã kiểm theo `de-thi`) nên bộ lắp đề US-S5E2-03 chưa dùng các đề này. Đề "Cần xem lại" không duyệt thẳng được (CONFLICT) vì chưa có đáp số engine đã kiểm chứng; sửa đề rồi kiểm lại chưa có TC. | TẠM | PO xác nhận |
| Dev-S5E2-03 | Thứ tự kiểm: tài liệu không của mình (hoặc lưu trữ) NOT_FOUND → lớp không của mình NOT_FOUND → không có bản Đang dùng CONFLICT MSG-S5-15 → trợ lý của lớp tắt CONFLICT → hết 30 lượt RATE_LIMITED MSG-S5-06; mọi nhánh lỗi trước lời gọi AI. Một lần soạn = một lượt soạn (BR-S5E5-12) dù nhiều đề. | TẠM | QC xác nhận |
| Dev-S5E2-04 | Đáp số engine viết dạng `p·a·√k/q` (khớp 1e-9; góc theo độ, thể tích a³, diện tích a²); không khớp thì số thập phân 4 chữ số. Giao diện `features/s5e2/draft/ExamDraftPanel.tsx` (+ `draftApi.ts`, `draftLogic.ts`) CHƯA gắn vào `features/s5e2/index.tsx` (của 52, ngoài phạm vi 63). | TẠM | Phiên chính: gắn panel |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-28 | Dev-S5E2-01…04 (trợ lý soạn đề nháp) | runner-T-261128-63 |
