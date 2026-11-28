Phiên bản: v0.2 · 2026-11-28
# S5-E1 — trạng thái

| Ngày | Vai | Nội dung |
|---|---|---|
| 2026-11-28 | Dev (runner-T-261128-53) | US-S5E1-01…03 (TC-S5E1-01…19 = 19 TC): E2E-UI 3 (TC-01, 17, 18 · `e2e/tests/s5e1/lesson.ui.spec.ts`, chromium + webkit), E2E-API 16 (`lesson.api.spec.ts`); IT 18 bổ trợ (`server/.../it/s5e1/`, gồm so băm system prompt V8 với `prompt-final.md` §2). Hoãn US-S5E1-04, 05 (task 54). AI chỉ giả lập (`MockAiProvider` mốc 53). Kết quả lệnh kiểm: `make verify` xanh (đơn vị 360, IT 284), `make web-build` xanh, `make e2e-affected` L2 lượt 20261128-063802 974/974 (951/951 mã TC), traceability `--strict --epic MVP,S2,S3,S5-E1 --defer US-S5E1-04,US-S5E1-05` ĐẠT (S5-E1 19/19), `check_s5.py` ĐẠT, `check_ac_tc.py S5-E1` 0 lỗi. Phát hiện: `qc_findings.md` Dev-S5E1-01…06. Ghi chú: đặc tả ghi `/api/lesson-plans`, code dùng quy ước app `/api/v1/teacher/lesson-plans`; TC-13 phần "bản xuất DOCX giữ nhãn" thuộc xuất file (task 54). |
| 2026-11-28 | Dev (runner-T-261128-54) | US-S5E1-04, 05 (TC-S5E1-20…31 = 12 TC): E2E-UI 2 (TC-20, 21 · `e2e/tests/s5e1/patch.ui.spec.ts`, chromium + webkit), E2E-API 10 (`patch.api.spec.ts`; tệp xuất đọc lại bằng móc dev/test `/test-hooks/export/inspect`); UT bổ trợ `PatchPathsTest`, `common/export/ExportWritersTest`. Máy chủ: gói `lessonplan/internal/{patch,list,export}`, V555 (`approved_*`, `lesson_plan_version.source`, `lesson_plan_patch`), task `lesson_patch` (mock F-S5-096 + biến thể patch-2/locked/badbefore/question/reply), bộ dựng chung `common/export` (POI 5.4.1 DOCX, pdfbox + phông DejaVu từ `jasperreports-fonts` cho PDF). Ghi chú: API theo quy ước app `/api/v1/teacher/lesson-plans/{id}/{chat,patches/{p}/accept\|skip,undo,locks,approve,export}`, danh sách `GET …?shelf=approved\|draft`; `approvedVersion` thêm vào GET giáo án qua `LessonViewAdvice` (không sửa bộ dựng của 53); Hoàn tác lùi con trỏ phiên bản (phiên bản sau bị ghi đè khi Nhận tiếp); chat một lượt = một lượt soạn (chung hạn mức 30). Kết quả lệnh kiểm: xem báo cáo T-54. |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-28 | Dòng Dev đầu tiên (US-S5E1-01…03) | runner-T-261128-53 |
| v0.2 | 2026-11-28 | Dòng Dev US-S5E1-04, 05 | runner-T-261128-54 |
