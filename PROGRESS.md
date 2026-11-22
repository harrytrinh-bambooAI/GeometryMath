# PROGRESS · GeometryMath (GEO) · cập nhật 23/11/2026 00:55
Mục tiêu hiện tại: làm hết MVP E1–E7 + các lô sửa sau soát, tự duyệt (PO-58). Nhánh tích hợp: `worktree-mvp-wave0` (worktree `.claude/worktrees/mvp-wave0`, đã push tới 75f6802; phần sau chưa push).

Đã xong (gộp vào nhánh tích hợp):
- Đợt 0: TC 469/453 AC (5261948), FOUNDATION (4d3570a), E1 67/67 TC (f3fe5d3), FIX1 (2f55df8), bằng chứng `artifacts/GEO/evidence/E1-20261122-224604/`
- E2 37/37 TC (0742560) · E3 49/49 TC tự động, 5 Manual (09b0076) · FIX2 16 phát hiện soát E2/E3 (36f7519); e2e 190/190 hai lượt

Đang chạy (agent Opus, worktree con `.claude/worktrees/mvp-wave0/.claude/worktrees/<tên>`):
- E4 nhánh `mvp-e4` (tiến độ `.claude/progress/e4.md` ở repo chính): IT 6/6, E2E-API 29/29; còn 59 E2E-UI; đã được nhắn merge nhánh tích hợp (FIX2)
- E5 nhánh `mvp-e5` (`.claude/progress/e5.md`)

Bước tiếp theo:
1. E4 xong → merge; soát E4 (Sonnet) → FIX3
2. E5 xong → merge; khởi chạy E6 (giao bài, 12 US, 119 TC), rồi E7 (theo dõi lớp, 4 US, 45 TC); soát + sửa gom
3. Kiểm cuối trên nhánh tích hợp: tạo `app/.env.ports` (54359/6419/8092/5176) → `cd app && make up && make verify && make e2e`; lưu bằng chứng vào `artifacts/GEO/evidence/`; commit; nhắc chủ đầu tư push
Mẫu brief: `.claude/briefs/epic-common.md` v1.1 (agent ghi tệp theo phần nhỏ).

Inbox còn: xem `.claude/inbox.md` (FIX1/FIX2 đã đóng; 2 mục cũ mở: tc0148 yếu, JWT trong trace/video).
Quyết định đã chốt: PO-DECISIONS.md (mới nhất PO-58). Giả định PO-51…57 chủ đầu tư xem sau.
Rủi ro / chặn: push bị auto mode chặn → chủ đầu tư tự push. Cổng 54329/6389/8080 là của checkout chính, không đụng.
