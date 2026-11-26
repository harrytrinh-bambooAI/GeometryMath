Phiên bản: v0.1 · 2026-11-27
# S2-E4 — trạng thái

| Ngày | Vai | Nội dung |
|---|---|---|
| 2026-11-27 | Dev/Test (runner-T-261126-24) | Phạm vi US-S2E4-01…03 (27 AC, 27 TC tự động: E2E-UI 13, E2E-API 7, IT 5 `AuthoringImportS2E4IT`, UT 2 `DedupTest`; thêm ngoài TC: IT "import tính vào giới hạn thêm đề" + UT `ImportFilesTest`, `MockAuthoringRepliesTest`, web `logic.test.ts`). Migration V260 (`problem_draft_chat`, `problem_draft_edit`, `import_job`, `import_item`, quyền UPDATE `solution_doc.statement/title/figure`). Thư viện mới trong `pom.xml`: `org.apache.pdfbox:pdfbox:3.0.5`. Seed `860_s2e4_core.sql`, mã `fixture-ids.d/s2e4.json`. Sau `git merge worktree-mvp-wave0` (be30127): `make verify` exit 0 (đơn vị 282 chạy / 0 trượt / 1 bỏ qua có chủ đích = bộ sinh tệp pdf mẫu; IT 198/198). `make web-build` exit 0 (77/77). `make e2e-affected` mức **L2** (vì `app/server/pom.xml` không có trong bản đồ ảnh hưởng → trọn bộ trên api + ui-chromium), lượt 20261127-003226: 665/665, exit 0, không test chập chờn (lượt riêng spec S2-E4 trước đó: 19/20 — TC-S2E4-017 làm tiến trình test hết bộ nhớ do fixture `request` ghi nguyên thân 10 MB; sửa helper `upload` của ca rồi 20/20). Traceability `--strict --epic MVP,S2-E1,S2-E2,S2-E3,S2-E5,S2-E6,S2-E7,S2-E8,S2-E4` exit 0 (S2-E4: 27/27 có test, thiếu 0, trượt 0; 7 TC tầng IT/UT hiện "chưa chạy" trong bảng vì công cụ chưa nối kết quả JUnit — giống các epic trước; kết quả thật ở `server/target/{surefire,failsafe}-reports`). `make bench-mvp` exit 0 ("BENCH-MVP OK"). `check_ac_tc.py S2-E4` 0 lỗi. Bằng chứng: `app/e2e/evidence/20261127-003226/index.html`. Giả định và việc ngoài phạm vi: `qc_findings.md` (Dev-S2E4-01…15; cần FIX: Dev-S2E4-13 đường dán chữ / ảnh của S2-E3 chưa gọi `LibraryQuota`, Dev-S2E4-07 ngăn "Của tôi" liệt kê cả sổ chưa duyệt). |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-27 | Dòng Dev/Test đầu tiên | runner-T-261126-24 |
