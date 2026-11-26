Phiên bản: v0.1 · 2026-11-26
# S2-E1 — trạng thái Dev/Test

| Ngày | Dòng | Nội dung |
|---|---|---|
| 2026-11-26 | Dev | T-261126-18 (nhánh s2-e1, chưa commit — phiên chính commit sau khi kiểm lại). 4 story US-S2E1-01…04: đăng ký tên + vai → mã GV-/HS- + mã khôi phục (GV và HS, G3), đăng nhập mã + mật khẩu khóa 10 lần/15 phút, tự khôi phục (hủy phiên khác), văn bản riêng tư 1.0 + cờ đã đọc, lệnh chuyển MVP `make s2e1-migrate` (từ chối prod), `class_member.status` (A6); luồng đăng nhập MVP giữ nguyên (A7). Migration V110; seed 800; fixture-ids.d/s2e1.json. Phát hiện Dev: `qc_findings.md` mục Dev. |
| 2026-11-26 | Test | Sau khi gộp worktree-mvp-wave0 (T-43 + T-50 theme C). TC tự động 45/45 có test (E2E-UI 16, E2E-API 21, IT 8; UT 0); Manual 1 (TC-S2E1-037, Not run). `make verify` xanh (đơn vị 206, IT 148). `make web-build` xanh. E2E lượt A 20261126-191439: 921 đạt / 925 (3 bỏ qua, 1 trượt ngoài S2-E1: TC-GEO-0209 ui-chromium, mẫu hình E2 chờ quá 5 s khi tải cao); lượt B 20261126-193504: 922 đạt / 925, 3 bỏ qua, 0 trượt. S2-E1: 57/57 mỗi lượt (api 21, ui-chromium 16, ui-webkit 16, layout p0m 4). `traceability --strict --epic MVP,S2-E1` ĐẠT (0 thiếu, 0 trượt; E2E S2-E1 ghi "chưa chạy" do evidence-reporter chưa nhận tag TC-S2E1, DEV-S2E1-12). `check_ac_tc.py S2-E1` 0 lỗi. |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo dòng Dev/Test | runner-T-261126-18 |
