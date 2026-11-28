Phiên bản: v0.2 · 2026-11-28
# S5-E5 — trạng thái

| Ngày | Vai | Nội dung |
|---|---|---|
| 2026-11-28 | Dev (runner-T-261128-55) | US-S5E5-01 (TC-S5E5-01…08) + US-S5E5-04 (TC-S5E5-19…29) = 19 TC: E2E-UI 1 (TC-01 · `e2e/tests/s5e5/notice.ui.spec.ts`), E2E-API 18 (`notice.api.spec.ts`, `quota.api.spec.ts`); UT bổ trợ `NoticeTextTest`. Hoãn US-S5E5-02, 03 (task 64). Máy chủ: `aigateway/api/quota/AiQuota` (API cho module khác), `aigateway/internal/{repo,service,web}/{quota,killswitch}`, `notify/internal/{repo,service,web}/draft`; migration V575; AI chỉ giả lập (`class_notice` + bộ chạy chấm nền giả, mốc 55 của `MockAiProvider`/`AnthropicPrompts`). Web `/gv/tro-ly/thong-bao`. Kết quả lệnh kiểm: `make verify` xanh (đơn vị 363, IT 291), `make web-build` xanh, `make e2e-affected` L1 lượt 20261128-072009 467/467 (378/378 mã TC), traceability `--strict --epic MVP,S2,S3,S5-E1,S5-E2,S5-E5 --defer …` ĐẠT (S5-E5 19/19, hoãn 10), `check_s5.py` ĐẠT, `check_ac_tc.py S5-E5` 0 lỗi. Phát hiện: `qc_findings.md` Dev-S5E5-01…05. |
| 2026-11-28 | Dev (runner-T-261128-64) | US-S5E5-02 (TC-S5E5-09…15) + US-S5E5-03 (TC-S5E5-16…18) = 10 TC: E2E-UI 2 (TC-10, 16 · `e2e/tests/s5e5/insights.ui.spec.ts`), E2E-API 8 (`insights.api.spec.ts`); UT bổ trợ `insightsLogic.test.ts`. TC-19, 23, 25, 27 chuyển từ móc `/test-hooks/s5e5/grading-run`, `/ocr-retry` sang luồng chấm thật (nộp bài → đọc chữ nền → AI đề xuất S5-E6 → "Đọc lại"). Máy chủ: `tracking/internal/errors` (read model chỉ đọc điểm duyệt, đề xuất AI + hành động giáo viên, tín hiệu engine, ngân hàng de-thi, kho tri thức), API `GET /api/v1/teacher/graded-assignments/{id}/error-summary` và `/error-summary/{n}/suggestions` sau cờ `geo.s5.grading.enabled` (bật ở hồ sơ dev/test/e2e, tắt mặc định ở prod); migration V805 `class_error_summary`; task `class_errors` chỉ giả lập (mốc 64 của `MockAiProvider`/`AnthropicPrompts`). Web `/gv/bai-co-cham/:id/loi-cua-lop`. Seed `s5_700_s5e5b_kc.sql` (bộ đề kho `s5_kc`, 3 đề khoảng cách) + móc `/test-hooks/s5e5b/*`. Kết quả lệnh kiểm: `make verify` VERIFY OK (IT 295), `make web-build` xanh, `make e2e-affected` L2 lượt 20261128-105213 1170/1170 (1141/1141 mã TC), e2e s5e5 32/32, traceability `--strict --epic MVP,S2,S3,S5 --defer US-S5E6-06` ĐẠT (S5-E5 29/29), `check_s5.py` ĐẠT, `check_ac_tc.py S5-E5` 0 lỗi. Phát hiện: `qc_findings.md` Dev-S5E5-06…10. |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-28 | Dòng Dev đầu tiên (US-S5E5-01, 04) | runner-T-261128-55 |
| v0.2 | 2026-11-28 | Dòng Dev US-S5E5-02, 03; TC-19/23/25/27 sang luồng chấm thật | runner-T-261128-64 |
