# E2 — status

| Stage | Stories | AC | TC | V1–V14 | Adversarial review | R3 | Last update |
|---|---|---|---|---|---|---|---|
| BA: lượt sửa gom sau phản biện RV đã xong (áp RV-GEO-01…37 phần BA và D-GEO-125…170; tin M-216…M-218) và lượt nhỏ áp D-GEO-173…183 (tin M-220, M-221). QC: đã sinh lại case theo AC hiện hành (PO-50, Đợt 0; 0 case lệch AC), thêm cột "Tự động hóa" | 5 (US-GEO-201…205) | 37 (SC 23 / FC 14; PROVISIONAL 17) | 37 (bám tiêu chí 37, viết tay 0; PROVISIONAL 17) · Tự động hóa: E2E-UI 12 / E2E-API 5 / IT 20 / Manual 0 · AC→TC 37/37 · lint_tests 0, check_ac_tc 0 lỗi | lint_stories: 5 stories, 0 findings; V14 trên bản đồ: đã chạy (XF-01…09) | RV đã phản biện (review/RV_findings.md); các phát hiện phần BA đã áp | Not reviewed (TBD) | 22-11-2026 |
| Dev/Test (E2, Đợt 1): engine + API + giao diện + test. make verify xanh (đơn vị 113/113, IT 54/54 gồm GeometryE2IT 7/7); web typecheck/lint/test 9/9/build xanh; make e2e 96/96 hai lượt liên tiếp (20261123-000140, 20261123-000205). Truy vết E2: 37/37 TC có test và đạt (IT 20, E2E-UI 12, E2E-API 5); AC 37/37. Phát hiện Dev: qc_findings.md QF-E2-D01…D08 | 5 | 37 | 37 đạt | — | — | — | 23-11-2026 |
