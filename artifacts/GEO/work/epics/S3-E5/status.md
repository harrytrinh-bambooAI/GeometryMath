Phiên bản: v0.2 · 2026-11-27
# S3-E5 — trạng thái

| Ngày | Vai | Nội dung |
|---|---|---|
| 2026-11-27 | Dev/Test (runner-T-261126-28) | Phần của T-28: US-S3E5-02 (TC-16…26) và US-S3E5-04 (TC-40…52) = 24 TC: tự động 23 (IT 2: TC-17, 51 ở `PrivacyAiS3E5IT`; E2E-API 12; E2E-UI 9), Manual 1 (TC-26, thiết bị thật). Hoãn 36 TC của US-S3E5-01 (task 31), US-S3E5-03 và 05 (task 32). Lệnh kiểm chung với S3-E1 (xem `../S3-E1/status.md`): verify, web-build, e2e-affected L1 lượt 20261127-031749 459/459, traceability `--strict` exit 0, `check_ac_tc.py` 0 lỗi. Phát hiện: `qc_findings.md` mục Dev (7). |
| 2026-11-27 | Dev/Test (runner-T-261126-32) | US-S3E5-03 (TC-27…39) và US-S3E5-05 (TC-53…60) = 21 TC: IT 8 (TC-27, 28, 32, 37, 38 ở `it/s3e5/AllowedFieldsS3E5IT`; TC-28, 30, 31 ở `quality/AllowedFieldsTest`; TC-56 ở `it/s3e5/AccountDeletionS3E5IT`), E2E-API 9, E2E-UI 4 (spec `app/e2e/tests/s3e5/fields.api`, `account.api`, `account.ui` — chỉ typecheck, e2e chạy khi tích hợp). Danh sách `allowed_fields` 1 031 dòng (quét 832 cột, 391 khóa API tĩnh + 77 khóa gọi thật), đã phủ bảng S3-E3/S3-E4 sau khi gộp wave0 (notification, cột mới của audit_log) và bảng mới của S3-E6. Lệnh kiểm: `make verify` xanh (đơn vị 301, IT 243), `make web-build` xanh, `check_ac_tc.py S3-E5 S3-E6` 0 lỗi. Phát hiện: `qc_findings.md` Dev-S3E5-11…21. |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-27 | Dòng Dev/Test đầu tiên | runner-T-261126-28 |
| v0.2 | 2026-11-27 | Dòng Dev/Test US-S3E5-03, 05 | runner-T-261126-32 |
