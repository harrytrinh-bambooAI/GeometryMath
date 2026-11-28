# PROGRESS · GeometryMath (GEO) · cập nhật 28/11/2026 ~01:35
Mục tiêu hiện tại (/goal 28/11): E2E chặng 2+3 + bug list ảnh nhúng (XONG) → code chặng 5 theo đặc tả v1.1.
Nhánh `worktree-mvp-wave0` (repo `/Users/harry/IdeaProjects/GeometryMathSupport/.claude/worktrees/mvp-wave0`).

Đã xong 28/11:
- E2E chặng 2+3 trọn bộ: UI 678/678 test (290/290 TC), API 285/285 (284/284 TC), 0 trượt/chập chờn; 1 lỗi Minor BUG-S23-001 (evidence-reporter đếm thiếu test vòng lặp). `artifacts/GEO/evidence/E2E-S23-20261128/` (BUG-LIST.md/.html ảnh base64, check_buglist.py ĐẠT). Gộp từ nhánh e2e-s23 (5e20479).
- Đặc tả chặng 5 v1.1 (PO/BA/QC): 6 epic, 37 US, 219 AC, 219 TC; 88 dev + 26,2 test = 114,2 ngày công (1 dev); story 2–3 ngày; `artifacts/GEO/work/s5/check_s5.py` ĐẠT. Soát độc lập (Sonnet) 27 lỗi → 25 sửa, 1 sửa một phần (trần test 30 % theo owner), 1 chấp nhận. Gộp từ spec-s5 (84bd6cb).

Code chặng 5 (owner "Chạy hết 8 đợt", PLAN_code-s5 v1.0, queue v0.4): W0 T-51 ✅, W1 T-52 ✅ T-53 ✅, W2 T-54 ✅ T-55 ✅, W3 T-56 ✅ T-57 ✅, W4 T-58 ✅ T-59 ✅, W5 T-60 ✅ T-61 ✅, W6 T-62 ✅ T-63 ✅, W7 T-64 ✅ T-65 ✅ — CODE 14/14 TASK XONG, đã gộp. Đang chạy FIX T-68 (wt s5-fix, việc nối tiếp còn dở: Trả bài↔KsS5Firewall, TC-S5E4-45, lấy mẫu song song ≥2 bài, giờ mở màn chấm cho KPI g, TC-S5E5-23 lý do hạn mức, TC-S5E4-49 webkit).
Điểm resume: `.claude/progress/runner.md` + `tail -5 .claude/progress/runner-T-261128-*.md`; brief sinh bằng `.claude/briefs/mkbrief.sh <ID> <wt> <song song>` (nội dung: queue.py brief + phần bổ sung worktree/cổng/allowed_fields).
Bài học W1: gộp nhánh tích hợp vào worktree task → reset geo_e2e/geo_dev của worktree đó (Flyway không out-of-order); allowed_fields.tsv dùng chung → mỗi task thêm dưới `# --- <ID> ---`, không đổi dòng Phiên bản.

Bước tiếp theo: chờ T-68 → queue.py check T-261128-68 → `.claude/briefs/land.sh T-261128-68 s5-fix "…"` → T-66 tester (wt mvp-wave0, cổng 54359·6419·8092·5176; UI Playwright trước, còn lại API; REPORT + BUG-LIST ảnh nhúng ở artifacts/GEO/evidence/E2E-S5/) → gate T-67 owner. Đề xuất chờ owner: E2E "cách lai" (mỗi task API + UI màn của task trên chromium; trọn bộ 2–3 đợt/lần) — owner gõ "Go lai".
Owner cần trả lời (artifacts/GEO/work/s5/questions-s5.md): QS5-01 (PO-64 cấm thu ảnh bài làm → E4/E6 code được, chưa phát hành), QS5-10, QS5-27 (bench chấm AI ~$32, cần "Go"), QS5-28 (điều kiện mở chặng 5), QS5-30 (ảnh gửi AI ngoài nước), QS5-32 (cổng KPI giá trị điểm vs KTC).
Owner vẫn còn: kiểm tay 7 ca chặng 3 (S3E2-37, E3-07, E4-02, E4-21, E4-27, E5-26, E6-10) + nghiệm thu L7 chặng 2, 3.
Inbox: .claude/inbox.md (+ BUG-S23-001 sửa reporter dòng 227–229). Quyết định: DECISIONS.md (mới nhất 28/11 làm trọn chặng 2-3 song song đặc tả chặng 5).

Lưu ý vận hành: hook worktree chặn `source`, lệnh ghép `cd …; git`, `git -C $VAR` → `git -C <đường dẫn tuyệt đối>`. Worktree con ở `.claude/worktrees/` của wave0, mỗi cái app/.env.ports riêng; cổng 54399 không dùng. AI giữ mock; AI thật cần owner duyệt chi phí.
Rủi ro: AI chấm chữ viết tay chưa đo; PO-64; test chặng 5 chỉ ≈ 0,96 giờ/TC (R7 estimate-s5.md).
