Phiên bản: v0.1 · 2026-11-26
# S2-E6 — trạng thái

| Ngày | Vai | Nội dung |
|---|---|---|
| 2026-11-26 | Dev/Test (runner-T-261126-23) | Phạm vi US-S2E6-01…03 (37 AC, 37 TC tự động: E2E-UI 13, E2E-API 21, IT 3). Migration V230 (user_role_grant, library_submission, library_problem, problem_create_event). `make verify` exit 0 (đơn vị 266/266, IT 184/184, trong đó LibraryS2E6IT 3/3, PresentS2E7IT 3/3). `make web-build` exit 0 (70/70). E2E riêng tests/s2e6 + s2e7 (api, ui-chromium, ui-webkit): 103/103. `make e2e-affected` MỘT lượt, mức **L1**, lượt 20261126-233723: 506/507, trượt TC-GEO-0539 (E5 catalog.ui, mobile-iphone, quá giờ; ngoài epic, không đụng tệp E5) → chạy lại riêng (lượt 20261126-rerun0539, lặp 3): 3/3. Traceability `--strict --epic MVP,S2-E1,S2-E2,S2-E5,S2-E8,S2-E6,S2-E7 --defer US-S2E5-05`: thiếu test 0; lượt L1 exit 2 (do TC-GEO-0539), lượt chạy lại exit 0. `make bench-mvp` exit 0. `check_ac_tc.py S2-E6 S2-E7` 0 lỗi. Seed 850_s2e67_core.sql, mã fixture fixture-ids.d/s2e67.json. Phát hiện: qc_findings.md. |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Dòng Dev/Test đầu tiên | runner-T-261126-23 |
