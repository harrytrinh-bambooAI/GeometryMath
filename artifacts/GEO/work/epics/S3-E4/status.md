Phiên bản: v0.1 · 2026-11-27
# S3-E4 — trạng thái

| Ngày | Vai | Nội dung |
|---|---|---|
| 2026-11-27 | Dev/Test (runner-T-261126-31) | S3-E4 41 TC: IT 3 (TC-36, 37, 40 ở `it/s3e4/NotificationS3E4IT`), E2E 35 TC viết + typecheck (api: `overview`, `release`, `notify`; ui: `overview`, `notify`; điện thoại `follow.p0m.ui.spec.ts`), Manual 3 (TC-02, 21, 27). Kèm US-S3E5-01 (TC-S3E5-01…15: IT 3 ở `it/s3e5/AuditEventS3E5IT`, `AuditFailureS3E5IT`; E2E-API 12 ở `tests/s3e5/audit.api.spec.ts`) và US-S3E6-03 (TC-S3E6-27…40: IT 13 ở `it/s3e6/ReminderS3E6IT`; E2E-API 1 ở `tests/s3e6/reminders.api.spec.ts`). Lệnh kiểm sau khi gộp wave0 0b1de51: `make verify` VERIFY OK (đơn vị 297, IT 232), `make web-build` xanh, `check_ac_tc.py S3-E4 S3-E5 S3-E6` 0 lỗi, `tsc` e2e xanh. E2E chưa chạy (lệnh owner: chạy khi tích hợp nhóm epic). Phát hiện: `qc_findings.md`. |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-27 | Dòng Dev/Test đầu tiên | runner-T-261126-31 |
