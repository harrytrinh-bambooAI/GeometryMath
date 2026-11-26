Phiên bản: v0.1 · 2026-11-26
# S2-E5 — trạng thái

| Ngày | Vai | Nội dung |
|---|---|---|
| 2026-11-26 | Dev/Test (runner-T-261126-20) | Phạm vi US-S2E5-01…04 (52 AC, 53 TC: tự động 52 = E2E-UI 29, E2E-API 11, IT 9, UT 3; Manual 1 = TC-S2E5-038). US-S2E5-05 (13 TC) dời T-261126-22. `make verify` exit 0 (đơn vị 237/237, IT 172/172, trong đó `SolutionS2E5IT` 11/11, `StepCheckTest` 9/9). `make web-build` exit 0 (55/55). `make e2e-affected` MỘT lượt, mức **L1**, lượt 20261126-221125: 409 đạt / 1 trượt, exit 1; trượt TC-GEO-0209 (E2 `templates.ui.spec.ts`, ô tham số h không nhận giá trị, ngoài S2-E5, không đụng tệp E2), chạy lại riêng spec đó (lượt 20261126-rerun0209, lặp 3 lần): 18/18. Spec S2-E5 trong lượt L1: 40 TC đạt, 0 trượt; chạy riêng `tests/s2e5` đủ dự án (lượt 20261126-220…): 75/75. Traceability `--strict --epic MVP,S2-E1,S2-E2,S2-E5 --defer US-S2E5-05`: thiếu test 0; với lượt L1 exit 1 (do TC-GEO-0209), với lượt chạy lại exit 0. `make bench-s2` exit 0 (M-S2-03 = 0, artifacts/GEO/evidence/S2-BENCH-20261126-2216). `make bench-mvp` exit 0. `check_ac_tc.py S2-E5` 0 lỗi. Migration V160. Mã fixture: `app/db/seed/fixture-ids.d/s2e5.json` (F-S2-82xxxx; sổ theo TC × làn: F-S2-(820000000 + TC × 100 + làn)). Phát hiện: `qc_findings.md` (Dev-S2E5-01…14). |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Dòng Dev/Test đầu tiên | runner-T-261126-20 |
