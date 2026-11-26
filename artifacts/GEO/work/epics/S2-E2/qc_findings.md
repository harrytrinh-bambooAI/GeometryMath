Phiên bản: v0.1 · 2026-11-26
# S2-E2 — Phát hiện QC

## Dev

Ghi bởi runner-T-261126-19 (L5 Dev). Không sửa `stories_ac.md`, `test_cases.md`, `test_design.md`; BA/QC xử lý.

| Mã | Chỗ | Phát hiện | Cách Dev làm (TẠM · tin cậy · nguồn) | Cần ai |
|---|---|---|---|---|
| Dev-S2E2-01 | US-S2E2-02-BV2, TC-S2E2-017 | Tam giác AB = 5, AC = 5, BC = 9,99: AC ghi "góc A xấp xỉ 179,8°", nhưng theo định lý cosin góc A = arccos((25 + 25 − 9,99²)/50) ≈ **174,87°**. | Test kiểm giá trị đúng 174,875° (sai số 1e-6) và > 174°; ba cạnh đúng 5, 5, 9,99 trong 1e-6 như AC. TẠM · cao · tính lại bằng định lý cosin | BA sửa số trong AC/TC |
| Dev-S2E2-02 | PLAN_code-s23 A2 | PLAN ghi `Problem.kind` = `solid` \| `plane`; stories_ac và TC ghi `space` \| `plane`. | Dùng `space` (enum `problem_kind`, V130) theo spec. TẠM · cao · stories_ac là nguồn chính (epic-common) | L4 cập nhật PLAN |
| Dev-S2E2-03 | US-S2E2-03 (FX-02 An đăng nhập bằng mã HS-) | Đăng nhập mã HS-/mật khẩu là của S2-E1 (T-261126-18, chạy song song, chưa gộp). | E2E đăng nhập An bằng thẻ dev (như E3 MVP), tổ chức riêng "Nhóm thử S2-E2" (seed 810). Khi S2-E1 gộp có thể đổi sang đăng nhập mã. TẠM · vừa · PLAN §3 W4 song song | delivery-lead |
| Dev-S2E2-04 | US-S2E2-03 "bài PL-01 đã giao" | Chưa có bộ bài S2-E8 (A4). | Đề phẳng là đề kho nền tảng `kind = 'plane'`, bộ `s2e2_plane` (không phải `main`, Kho đề MVP giữ sáu đề), `solid_kind` để trống (V130 đổi NOT NULL thành CHECK theo `kind`); bài giao MVP `assignment` (lớp) trỏ tới đề; mô tả dựng ở `plane_problem_spec`. `fn_catalog_reference` (bộ kiểm chứng cả kho MVP) chỉ xét `kind = 'space'`. TẠM · vừa · PLAN A2, A4 | L4/S2-E8 xác nhận |
| Dev-S2E2-05 | US-S2E2-04 BR-S2E2-12 (người soát) | Bộ đề chuẩn 30 đề cần người có chuyên môn Toán soát đáp số (STAGES Ch2 §9). | Đáp số do Dev tính tay, lệnh nạp đối chiếu với engine (BR-S2E2-14); trường `reviewer` ghi "TẠM · … chờ giáo viên Toán soát (QA-S2E2-02)". | Owner/PO chỉ định người soát |
| Dev-S2E2-06 | US-S2E2-05 (AI giả lập) | Bench chặng 2 với AI giả lập: chưa có đáp mẫu AI cho 30 đề phẳng (mock-ai thuộc `aigateway`, ngoài phạm vi tệp). | "Máy" = khung môn học (engine dựng, giải, gắn nhãn), không gọi AI; lệnh từ chối khi có AI thật. Chỉ số đo độ đúng engine trên bộ chuẩn. TẠM · vừa · DECISIONS 26/11 (AI giữ mock) | Task 22/24 khi thêm đáp mẫu AI |
| Dev-S2E2-07 | TC-S2E2-028, 030 | Manual (thiết bị thật). | Có kiểm máy một phần trong TC-S2E2-022 (vùng chạm đỉnh ≥ 44 px trên iPad gen 7); bố cục iPhone một cột qua CSS lưới, chưa chạy máy thật. | Tester thiết bị |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mục Dev (7 phát hiện) | runner-T-261126-19 |
