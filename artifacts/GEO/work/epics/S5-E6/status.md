Phiên bản: v0.1 · 2026-11-28
# S5-E6 — trạng thái

| Ngày | Vai | Nội dung |
|---|---|---|
| 2026-11-28 | Dev (runner-T-261128-61) | Sau cờ `geo.s5.ai-grading.enabled` (mặc định TẮT ở `application.yml`/prod; BẬT ở hồ sơ dev/test — e2e). Chỉ API, không giao diện. US-S5E6-01 (TC-S5E6-01…08) + US-S5E6-03 (TC-14…17) = 12 TC E2E-API (`e2e/tests/s5e6/proposal.api.spec.ts`) + 1 ca luồng thật `@TC-S5E4-43` (xoá đề xuất/tín hiệu theo tài khoản); UT bổ trợ `aigrade/GradeRulesTest` (luật KPI §2.1–§2.5, kiểm cứng, KS-1/KS-2). Hoãn US-S5E6-02, 04, 05, 06 (task 62, 65). Máy chủ: migration V725 (`ai_grade_proposal`, `criterion_assessment`, `engine_signal`, bất biến, cột `academic_year`), `aigrade/api/AiGradeSignals`, `aigrade/internal/proposal/{repo,service,web}` (job nền `ProposalJob` quét bản đọc chữ mới ở chế độ `ai_proposal`, lượt qua `AiQuota.admitGrading` + `registerJob`, gọi lại 1 lần, `GradeRules`, `SubmissionDataEraser` "ai_grade"), `aigrade/internal/engine/service/EngineMatcher`; API `GET /api/v1/submissions/{id}/proposal`, `GET /api/v1/teacher/graded-assignments/{id}/ai-signals`; móc dev/test `/test-hooks/s5e6/{setup,run,prompt,counts}`. AI chỉ giả lập (mốc 61 `MockAiProvider` F-S5-090/091 + biến thể); prompt AI thật `AnthropicPrompts.GRADE` (chất lượng cần bench QS5-27). Bảng `ai_grade_proposal` dùng cột `step_marks` (tên `steps` bị ReleaseE6IT cấm ngoài bảng lời giải). Kết quả lệnh kiểm: `make verify` VERIFY OK (IT 295), `make web-build` xanh, `make e2e-affected` L2 lượt 20261128-093107 1113 passed, traceability `--strict … --defer …` ĐẠT (S5-E6 12/12, hoãn 25), `check_s5.py` ĐẠT, `check_ac_tc.py S5-E6` 0 lỗi. Phát hiện: `qc_findings.md` Dev-S5E6-01…07. |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-28 | Dòng Dev đầu tiên (US-S5E6-01, 03) | runner-T-261128-61 |
