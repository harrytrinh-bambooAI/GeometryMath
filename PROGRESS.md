# PROGRESS · GeometryMath (GEO) · cập nhật 28/11/2026 ~14:10
Mục tiêu hiện tại (/goal 28/11): E2E chặng 2+3 + bug list ảnh nhúng → code chặng 5 theo đặc tả v1.1 — CẢ HAI XONG; chờ owner nghiệm thu (gate T-261128-67).
Nhánh `worktree-mvp-wave0` (repo `/Users/harry/IdeaProjects/GeometryMathSupport/.claude/worktrees/mvp-wave0`), đã push.

Đã xong 28/11:
- E2E chặng 2+3: UI 678/678, API 285/285; 1 lỗi Minor BUG-S23-001 (đã sửa ở T-51). `artifacts/GEO/evidence/E2E-S23-20261128/` (bug list ảnh base64, check_buglist ĐẠT).
- Đặc tả chặng 5 v1.1: 6 epic, 37 US, 219 AC, 219 TC, 114,2 ngày công; soát độc lập 25/27 sửa.
- Code chặng 5: 17 task (khung 51, code 52–65, FIX 68, công cụ E2E lai 69) — mỗi task phiên chính tự chạy lại `queue.py check` (exit 0) rồi gộp.
- Tester chặng 5 (T-66): 219/219 TC đạt (UI 26/26 chromium+webkit, API 193/193), 0 lỗi; `artifacts/GEO/evidence/E2E-S5-20261128/` (REPORT, BUG-LIST .md/.html, check_buglist ĐẠT).
- Hồi quy trọn bộ `make e2e-full` trên bản gộp cuối (wt s5-test @ dff5fb6, lượt 20261128-113923): 2054/2058 test đạt, 4 skipped, 0 trượt, 0 chập chờn; 1158/1158 mã TC; traceability --strict MVP,S2,S3,S5 ĐẠT (258 TC chưa chạy = loại Manual/IT/UT không thuộc E2E).
- FIX T-72 (REVIEW mục 7, tự quyết ghi ở DECISIONS): job ghi bù cặp điểm AI/GV, giữ giao dịch Duyệt; phiên chính chạy lại check exit 0 (verify 295/295, e2e-affected 146/146, strict ĐẠT); REVIEW v1.3 = 7 sửa, 1 chấp nhận.
- Hồi quy trọn bộ lần 2 sau FIX T-70 (HEAD 2af318f): 2059/2063 đạt (1 chập chờn đạt sau retry), 4 skipped, 0 trượt; 1158/1158 mã TC; traceability --strict ĐẠT.

Đang dở: không có agent nào chạy. Soát độc lập §7 + FIX T-70, T-71 đã gộp: REVIEW-S5-20261128 v1.2 v1.3 (mục 7, 8 đã sửa).
Bước tiếp theo:
1. Owner nghiệm thu gate T-261128-67 (chặng 5) + còn L7 chặng 2, 3 và 7 ca tay chặng 3 (S3E2-37, E3-07, E4-02, E4-21, E4-27, E5-26, E6-10) + ca tay HEIC iPhone thật (S5-E4).
2. Việc nhỏ còn mở (PO xác nhận khi nghiệm thu): cổng trần token áp cả task grade (T-71); cờ `sampled` khi ghi bù tính theo cổng lớp lúc ghi bù (T-72); test ghi bù chưa có mã TC-S5E6 chính thức. Xem .claude/inbox.md.
3. Owner trả lời câu chặn: QS5-01 (PO-64 cấm thu ảnh → E4/E6 sau cờ tắt), QS5-27 (bench chấm AI ~$32, cần "Go"), QS5-18 (S3 trong nước + GEO_IMAGE_KEY/SIGN_KEY), QS5-10/28/30/32; câu PO từ T-58/60/61/64/68 trong `.claude/inbox.md`.
Tiến độ %: `python3 .claude/briefs/progress_s5.py`. Script điều phối: `.claude/briefs/{start,sync,land}.sh`, `resolve_allowed_fields.py`.
Quyết định: DECISIONS.md (mới nhất 28/11 "E2E cách lai"; "Code chặng 5 chạy hết 8 đợt").

Lưu ý vận hành: hook worktree chặn lệnh git ghép/biến → `git -C <đường dẫn tuyệt đối>` từng lệnh hoặc gói trong script. Gộp nhánh tích hợp vào worktree task → reset geo_e2e/geo_dev của worktree đó (`.claude/briefs/sync.sh`). Lệnh DB chỉ trên cổng của worktree mình (sự cố 28/11: T-60, T-61 đụng cụm 54329 của checkout chính — không mất dữ liệu; geo_e2e ở đó đã tạo lại rỗng).
Rủi ro: AI chấm chữ viết tay chưa đo (bench QS5-27); PO-64; test chặng 5 ≈ 0,96 giờ/TC (R7).
