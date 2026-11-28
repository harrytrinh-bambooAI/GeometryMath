Phiên bản: v0.1 · 2026-11-28
# S5-E3 — trạng thái

| Ngày | Vai | Nội dung |
|---|---|---|
| 2026-11-28 | Dev (runner-T-261128-56) | US-S5E3-01 (TC-S5E3-01…07) + US-S5E3-02 (TC-08…11) + US-S5E3-07 (TC-20…23) = 15 TC, đều E2E-API (`e2e/tests/s5e3/kb.api.spec.ts`); IT bổ trợ `it/s5e3/KbS5E3IT` (4), UT `KbSplitterTest` (4). Hoãn US-S5E3-03, 04, 05 (task 58), 06 (task 63). Máy chủ: `knowledge/internal/kb/{repo,service,web}`, cổng `knowledge.api.KbRetriever` cho hỏi đáp; migration V605 (`kb_document`, `kb_document_version`, `kb_section` tsvector bỏ dấu `kb_norm`, `kb_grant`, `kb_upload`); seed `s5_300_s5e3_kb.sql` (F-S5-040, 041) + `fixture-ids.d/s5e3.json`; khóa `geo.kb.min-score` (0,1). AI không dùng (chỉ ca TC-03 qua trợ lý giáo án giả lập). Web `/gv/kho-tri-thuc`. Kết quả lệnh kiểm: `make verify` xanh (đơn vị 376, IT 295), `make web-build` xanh, `make e2e-affected` L2 lượt 20261128-075515 1043/1043 (1020/1020 mã TC), traceability `--strict --epic MVP,S2,S3,S5-E1,S5-E2,S5-E3,S5-E5 --defer …` ĐẠT (S5-E3 15/15, hoãn 27), `check_s5.py` ĐẠT, `check_ac_tc.py S5-E3` 0 lỗi. Phát hiện: `qc_findings.md` Dev-S5E3-01…07. |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-28 | Dòng Dev đầu tiên (US-S5E3-01, 02, 07) | runner-T-261128-56 |
