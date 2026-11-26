Phiên bản: v0.1 · 2026-11-27
# S3-E1 — trạng thái

| Ngày | Vai | Nội dung |
|---|---|---|
| 2026-11-27 | Dev/Test (runner-T-261126-28) | TC 56: tự động 56 (IT 3: TC-11, 53, 54 ở `ClassS3E1IT`; E2E-API 30; E2E-UI 23). Sau khi gộp `worktree-mvp-wave0` (b86b043, fast-forward): `make verify` exit 0 (đơn vị 290, 1 bỏ qua; IT 206/206); `make web-build` exit 0 (89/89); `make e2e-affected` mức **L1**, lượt 20261127-031749: 459 đạt / 459 (455/455 test, 357/357 mã TC), `app/e2e/evidence/20261127-031749/summary.json` (GEO_E2E_EVIDENCE=off); `make traceability ARGS="--strict --epic MVP,S2-E1…S2-E9,S3-E1,S3-E5 --defer US-S3E5-01,US-S3E5-03,US-S3E5-05"` exit 0 (thiếu test 0, trượt 0); `check_ac_tc.py S3-E1 S3-E5` 0 lỗi. Migration V315, seed `911_s3e1_classes.sql`. Ba lượt e2e-affected trước đó có ca ngoài epic trượt: TC-GEO-0804, TC-GEO-0494 (chập chờn, chạy riêng đạt); TC-GEO-0128, TC-S2E1-030 (do T-28 gây ra, đã sửa: đổi đường dẫn `/api/v1/student/class-ai/{id}`, cổng riêng tư không phát tín hiệu đăng xuất). Phát hiện: `qc_findings.md` mục Dev (12). |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-27 | Dòng Dev/Test đầu tiên | runner-T-261126-28 |
