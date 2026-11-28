Phiên bản: v0.1 · 2026-11-27
# S3-E6 — trạng thái

| Ngày | Vai | Nội dung |
|---|---|---|
| 2026-11-27 | Dev/Test (runner-T-261126-32) | US-S3E6-01 (TC-01…10) và US-S3E6-02 (TC-11…26) = 26 TC: IT 1 (TC-04 ở `it/s3e6/ClassResultsS3E6IT`, kèm 2 IT không gắn mã cho xuất CSV và lưu trữ), E2E-API 12, E2E-UI 12 (spec ở `app/e2e/tests/s3e6/`, chỉ typecheck — e2e chạy khi tích hợp nhóm epic), Manual 1 (TC-10). Lệnh kiểm: `make verify` xanh (đơn vị 301, IT 243), `make web-build` xanh, `check_ac_tc.py S3-E5 S3-E6` 0 lỗi. Phát hiện: `qc_findings.md` (8); TC-19 chờ FIX assignment (Dev-S3E6-01). US-S3E6-03 do T-31. |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-27 | Dòng Dev/Test đầu tiên | runner-T-261126-32 |
