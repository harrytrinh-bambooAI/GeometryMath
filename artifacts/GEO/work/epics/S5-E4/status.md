Phiên bản: v0.1 · 2026-11-28
# S5-E4 — trạng thái

| Ngày | Vai | Nội dung |
|---|---|---|
| 2026-11-28 | Dev (runner-T-261128-57) | Sau cờ `geo.s5.grading.enabled` (mặc định TẮT ở `application.yml`/prod; BẬT ở hồ sơ dev/test — e2e chạy profile dev). US-S5E4-01 (TC-S5E4-01…06) + US-S5E4-02 (TC-07…11) + US-S5E4-08 (TC-40…45) = 17 TC: E2E-UI 1 (TC-03 · `e2e/tests/s5e4/rubric.ui.spec.ts`), E2E-API 16 (`rubric.api.spec.ts`, `assign.api.spec.ts`, `storage.api.spec.ts`); UT bổ trợ `MetadataStripperTest`, web `rubric/logic.test.ts`. Hoãn US-S5E4-03…07, 09 (task 58–60). Máy chủ: `assessment/api/{ImageStore, ClassAiGateQuery (stub luôn đóng), SubmissionDataEraser}`, `assessment/internal/{rubric,assign,storage}/{web,service,repo}`; migration V625 (trigger bất biến phiên bản rubric đã dùng); kho ảnh AES-256-GCM tệp cục bộ (bản S3 tại VN chờ QS5-18), URL ký 5 phút, gỡ EXIF/XMP, job xoá theo tài khoản và năm học (mỗi giờ); lượt `grade_ocr` có ảnh qua `AiQuota.admitGrading` + `AiGateway`, AI chỉ giả lập (mốc 57 của `MockAiProvider`). Seed `s5_350_s5e4_rubric_bc.sql` (F-S5-051 RB-01, F-S5-052 BC-01). Web `/gv/rubric` (C5-02), `/gv/bai-co-cham` (C5-01). Kết quả lệnh kiểm: `make verify` VERIFY OK (IT 291), `make web-build` xanh, `make e2e-affected` L2 lượt 20261128-080656 1045 passed, traceability `--strict --epic MVP,S2,S3,S5-E1,S5-E2,S5-E4,S5-E5 --defer …` ĐẠT (S5-E4 17/17, hoãn 34), `check_s5.py` ĐẠT, `check_ac_tc.py S5-E4` 0 lỗi. Phát hiện: `qc_findings.md` Dev-S5E4-01…05. |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-28 | Dòng Dev đầu tiên (US-S5E4-01, 02, 08) | runner-T-261128-57 |
