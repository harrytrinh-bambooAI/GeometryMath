# PROGRESS · GeometryMath (GEO) · cập nhật 23/11/2026 01:40
Mục tiêu hiện tại: làm hết MVP E1–E7 + các lô sửa sau soát, tự duyệt (PO-58). Nhánh tích hợp: `worktree-mvp-wave0` (worktree `.claude/worktrees/mvp-wave0`, đã push tới 75f6802; phần sau chưa push).

Đã xong (gộp vào nhánh tích hợp):
- Đợt 0: TC 469/453 AC (5261948), FOUNDATION (4d3570a), E1 67/67 TC (f3fe5d3), FIX1 (2f55df8), bằng chứng `artifacts/GEO/evidence/E1-20261122-224604/`
- E2 37/37 TC (0742560) · E3 49/49 TC tự động, 5 Manual (09b0076) · FIX2 16 phát hiện soát E2/E3 (36f7519); e2e 190/190 hai lượt

Đã gộp thêm: E4 94/94 TC (c4951f5) · E5 43/43 TC (317a192, gỡ xung đột trang chi tiết đề).
Nhánh tích hợp 317a192: make verify ĐỎ 1 IT (CatalogE5IT.tc0528, tương tác E4×E5) → FIX3 mục 0.

Đang chạy (Opus, worktree con `.claude/worktrees/mvp-wave0/.claude/worktrees/<tên>`, tiến độ `.claude/progress/<tên>.md` ở repo chính):
- E6 nhánh `mvp-e6` (giao bài, khóa lời giải, 119 TC); đã được báo merge nhánh tích hợp, tc0528 là lỗi đã biết
- FIX3 nhánh `mvp-fix3`: inbox mục 0–16 (tc0528, 4 blocker quyền bank E5, 2 major + 7 minor E4)

Bước tiếp theo:
1. FIX3 xong → merge, chạy make verify + e2e trên nhánh tích hợp
2. Khi có slot: E7 (theo dõi lớp, 4 US, 45 TC); E6 xong → merge (kiểm guard bank còn ở fn_create_assignment), soát E6/E7 → FIX4
3. Kiểm cuối trên nhánh tích hợp: tạo `app/.env.ports` (54359/6419/8092/5176) → `cd app && make up && make verify && make e2e`; lưu bằng chứng vào `artifacts/GEO/evidence/`; commit; nhắc chủ đầu tư push
Mẫu brief: `.claude/briefs/epic-common.md` v1.1 (agent ghi tệp theo phần nhỏ).

Inbox còn: xem `.claude/inbox.md` (FIX1/FIX2 đã đóng; 2 mục cũ mở: tc0148 yếu, JWT trong trace/video).
Quyết định đã chốt: PO-DECISIONS.md (mới nhất PO-58). Giả định PO-51…57 chủ đầu tư xem sau.
Rủi ro / chặn: push bị auto mode chặn → chủ đầu tư tự push. Cổng 54329/6389/8080 là của checkout chính, không đụng.
