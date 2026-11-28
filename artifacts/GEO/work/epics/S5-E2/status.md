Phiên bản: v0.2 · 2026-11-28
# S5-E2 — trạng thái Dev / Test

| Vai | Ngày | Phạm vi | Kết quả | Ghi chú |
|---|---|---|---|---|
| Dev (T-261128-52, runner-T-261128-52) | 2026-11-28 | US-S5E2-01…04 (TC-S5E2-01…19) | E2E-API 17/17 · E2E-UI 2/2 · IT S5-E2 7/7 (LapDeCheckPyIT 4 đối chứng `check.py`, ExamImportS5E2IT 3) · UT web 3/3 | make verify VERIFY OK (UT 360, IT 273/273) · web-build xanh · e2e-affected L2 974/974 (lượt 20261128-063034) · traceability --strict ĐẠT (S5-E2 19/19, hoãn 10) · check_s5 ĐẠT · check_ac_tc S5-E2 0 lỗi. US-S5E2-05 (xuất, T-54) và US-S5E2-06 (luồng D, T-63) hoãn. |
| Dev (T-261128-54, runner-T-261128-54) | 2026-11-28 | US-S5E2-05 (TC-S5E2-20…23) | E2E-API 4/4 (`e2e/tests/s5e2/export.api.spec.ts`) | `GET /api/exams/{id}/codes/{code}/export?format=docx\|pdf&parts=all\|de,dap-an,huong-dan-cham,dac-ta` (gói `exam/internal/export`, dùng chung `common/export`); một phần = một tệp, nhiều phần = zip; học sinh FORBIDDEN, giáo viên khác NOT_FOUND; bộ chưa duyệt chân trang "Bản nháp – chưa duyệt". Giao diện: `features/s5e2/export/ExamExportPanel.tsx` CHƯA gắn vào `ExamSetPage.tsx` (tệp của 52, ngoài phạm vi 54) — cần một dòng `<ExamExportPanel setId={set.id} codes={set.variants.map(v => v.code)} />`. |

## Ghi chú Dev (giả định TẠM, chờ QC/PO)
- "Đáp số engine" khi nạp = giá trị engine đã đo lưu ở `mau-cau/engine-values.json` (spec của 474 giá trị là `null` nên không chạy lại engine trong lệnh nạp); câu nguồn MVP có `|engine − đáp số| > 1e-6·max(1,|đáp số|)` bị loại (đúng công thức `check.py` Kiểm 3).
- Lắp đề Java tái lập theo seed bằng `java.util.Random` (không trùng từng câu với `lap_de.py` Python cùng seed — hai bộ sinh số khác nhau); đạt cùng 7 ràng buộc, đối chứng bằng `check.py` `kiem_doc_lap` trong IT và E2E.
- Chương "Quan hệ vuông góc trong không gian" gộp hai chủ đề ngân hàng `hinh-khong-gian-quan-he` + `hinh-khong-gian-the-tich`; chương khác = một chủ đề taxonomy. Chương chọn được = chương trong ma trận + chủ đề cùng lớp (taxonomy `lop_ct2018`), nên TC-S5E2-14 (CK2 chỉ giữ lượng giác) chạy được.
- Ô chuyển chương (BR-S5E2-05) lấy chủ đề của ô còn lại cùng loại câu, bỏ lọc dạng/YCCĐ; thời gian dự kiến (MSG-S5-11) = tổng thời gian câu ngắn nhất hợp từng ô.
- TC-S5E2-18 (ô chỉ còn 1 câu) và TC-S5E2-19 (tạo bài có chấm, module của T-57 chưa có) chạy qua móc dev/test gọi đúng đường máy chủ (`ExamSetService.replace` có lọc ngân hàng; `exam.api.ExamSetGuard`).
- `exam_variant_item` (PLAN §2) không tạo: câu của mã đề lưu trong `exam_variant.body` (JSON, cùng khóa `lap_de.py`).

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-28 | Dòng Dev US-S5E2-01…04 | runner-T-261128-52 |
| v0.2 | 2026-11-28 | Dòng Dev US-S5E2-05 | runner-T-261128-54 |
