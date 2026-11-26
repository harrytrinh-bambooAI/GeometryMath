Phiên bản: v0.1 · 2026-11-26
# S2-E3 — trạng thái

| Ngày | Vai | Nội dung |
|---|---|---|
| 2026-11-26 | Dev/Test (runner-T-261126-22) | Phạm vi US-S2E3-01…04 (39 AC, 39 TC tự động: E2E-UI 21, E2E-API 8, IT 6 `DraftS2E3IT`, UT 4 `DraftS2E3Test`). Migration V210 (`problem_draft`, `problem_draft_job`, cột `solution_doc.stated_answer/figure_source/draft_id`, `fn_get_solution` theo phiên bản ghim). Seed `840_s2e3_core.sql`, mã `fixture-ids.d/s2e3.json`. `make verify` exit 0 (đơn vị 272/272, IT 192/192). `make web-build` exit 0 (73/73). `make e2e-affected` mức **L1**, lượt 20261126-235040: 576/576, 440/440 mã TC, exit 0 (lượt trước 20261126-234453: 571/576 — TC-GEO-0508 do mục rail "Thêm đề", TC-S2E5-065/036 do đổi dạng `assignments`; đã sửa rồi chạy lại cả lượt). Traceability `--strict --epic MVP,S2-E1,S2-E2,S2-E5,S2-E8,S2-E3` exit 0 (thiếu 0, trượt 0). `make bench-s2`, `make bench-mvp` exit 0. `check_ac_tc.py S2-E3 S2-E5` 0 lỗi. Phát hiện: `qc_findings.md` (Dev-S2E3-01…10). |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Dòng Dev/Test đầu tiên | runner-T-261126-22 |
