Phiên bản: v0.1 · 2026-11-27
# S3-E2 — trạng thái

| Ngày | Vai | Nội dung |
|---|---|---|
| 2026-11-27 | Dev/Test (runner-T-261126-29) | AC 45, TC 45: tự động 44 (E2E-UI 20, E2E-API 23, IT 1), Manual 1 (TC-S3E2-37, thiết bị thật). Sau khi gộp `worktree-mvp-wave0` (b86b043; gộp thêm 2a4c736 chỉ đổi PROGRESS.md): `make verify` exit 0 (đơn vị 290, 1 bỏ qua; IT 203/203, trong đó `AssignmentSetS3E2IT` 2/2); `make web-build` exit 0 (91/91); `make e2e-affected` MỘT lượt, mức **L1** (S3-E2 + E6, E3, E4, E7, S2-E8), lượt 20261127-025336: 470 đạt / 470, 0 trượt (TC 399/399), `app/e2e/evidence/20261127-025336/summary.json` (bằng chứng ảnh/video tắt, GEO_E2E_EVIDENCE=off); `make traceability ARGS="--strict --epic MVP,S2-E1…S2-E9,S3-E2"` exit 0 (S3-E2: 44/44 có test, 0 trượt); `check_ac_tc.py S3-E2` 0 lỗi. Migration V335 (chỉ quyền cột `class_id`, không đổi schema). Seed `920_s3e2_core.sql` + `fixture-ids.d/s3e2.json`. Phát hiện: `qc_findings.md` mục Dev (11), đáng chú ý DEV-S3E2-02 (đề "Của tôi" chưa chọn được vào bộ), DEV-S3E2-03 (Notification "bài mới" chờ T-261126-31), DEV-S3E2-07/09 (dữ liệu mẫu F-S3-051/052 lệch TC). |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-27 | Dòng Dev/Test đầu tiên | runner-T-261126-29 |
