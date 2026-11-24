# PROGRESS · GeometryMath (GEO) · cập nhật 25/11/2026 07:00
Mục tiêu hiện tại: làm hết MVP E1–E7 + các lô sửa sau soát, tự duyệt (PO-58). Nhánh tích hợp: `worktree-mvp-wave0` (worktree `.claude/worktrees/mvp-wave0`, đã push tới 75f6802; phần sau chưa push).

Đã xong (gộp vào nhánh tích hợp):
- Đợt 0: TC 469/453 AC (5261948), FOUNDATION (4d3570a), E1 67/67 TC (f3fe5d3), FIX1 (2f55df8), bằng chứng `artifacts/GEO/evidence/E1-20261122-224604/`
- E2 37/37 TC (0742560) · E3 49/49 TC tự động, 5 Manual (09b0076) · FIX2 16 phát hiện soát E2/E3 (36f7519); e2e 190/190 hai lượt

Đã gộp thêm: E4 94/94 TC (c4951f5) · E5 43/43 TC (317a192) · FIX3 17/17 mục (5278808, gồm tc0528 + quyền bộ đề); make verify tích hợp sau FIX3 xanh (IT 90/90).
E6 117/117 TC tự động (2f4c5d0; e2e 554 đạt hai lượt trên nhánh e6, bằng chứng worktree e6 `app/e2e/evidence/20261125-06*`).

Đang chạy từ 25/11 06:35 (2 Opus song song, tiến độ `/Users/harry/IdeaProjects/GeometryMathSupport/.claude/progress/<tên>.md`):
- review-e6 (Sonnet, chỉ đọc diff 5278808..2f4c5d0) → `.claude/reviews/E6-review.md`
- e7 (nhánh `mvp-e7`, worktree con e7, cổng 54449/6499/8100/5184, migration V70–79): làm E7; gộp mvp-e6 khi e6.md có "gộp tích hợp · đạt"
Agent phiên 23/11 đã dừng khi /clear; FIX3 lúc đó đã xong (inbox commit 359f180).
Bước tiếp theo:
1. review-e6 xong → FIX4a (Opus): phát hiện E6 + 2 mục inbox cũ (tc0148, JWT trace/video); song song e7 (PO-59)
2. e7 XONG → merge mvp-e7, soát E7 (Sonnet) → FIX4
3. Kiểm cuối trên nhánh tích hợp: tạo `app/.env.ports` (54359/6419/8092/5176) → `cd app && make up && make verify && make e2e`; lưu bằng chứng vào `artifacts/GEO/evidence/`; commit; nhắc chủ đầu tư push
Mẫu brief: `.claude/briefs/epic-common.md` v1.1 (agent ghi tệp theo phần nhỏ).

Inbox còn: xem `.claude/inbox.md` (FIX1/FIX2 đã đóng; 2 mục cũ mở: tc0148 yếu, JWT trong trace/video).
Quyết định đã chốt: PO-DECISIONS.md (mới nhất PO-58). Giả định PO-51…57 chủ đầu tư xem sau.
Rủi ro / chặn: agent e6 có thể đã tạo DB rỗng `geo_e2e` ở cổng 54329 (checkout chính) — chờ chủ đầu tư kiểm/xóa. push bị auto mode chặn → chủ đầu tư tự push. Cổng 54329/6389/8080 là của checkout chính, không đụng.
