# PROGRESS · GeometryMath (GEO) · cập nhật 27/11/2026 00:00
Mục tiêu hiện tại: CODE tới hết chặng 3. Phiên điều phối chạy Fable; agent con Dev: T-20/T-21 Fable, T-22/T-23 Opus (owner đang so sánh hai model trên cùng quy trình). Luôn 2 agent song song (≤ 2 Opus/Fable). Delivery-lead tự quyết + ghi DECISIONS. Nhánh: `worktree-mvp-wave0` (repo `/Users/harry/IdeaProjects/GeometryMathSupport/.claude/worktrees/mvp-wave0`).
Tiến độ epic (code đã gộp): 7/15 — S2-E1 (T-18), S2-E2 (T-19), S2-E5 đủ (T-20 + US-05 trong T-22), S2-E8 (T-21), S2-E3 (T-22, d9f76eb), S2-E6 + S2-E7 (T-23, 93e3fec).
Đã xong (26/11 tối → đêm):
- T-45 e2e-affected + smoke (b5f15e5) · T-44 theme hoàn thiện (fd23b06) · Icon chung 4 theme: Design/styles v0.2 + app, font tự lưu 7 KB.
- T-20 S2-E5 (Fable): verify 237 UT + 172 IT, e2e-affected L1 409/410 (0209 chập chờn).
- T-21 S2-E8 (Fable): bản kết hợp T-20 verify 266 UT + 178 IT, e2e-affected L1 594/594; FIX test chập chờn `RC-` S2-E1.
- T-23 S2-E6+E7 (Opus): verify 266 UT + 184 IT, e2e-affected L1 506/507 (0539 chập chờn, chạy lại đạt).
- T-22 S2-E3 + US-S2E5-05 (Opus): trên bản đã gộp T-23, verify 272 UT + 192 IT, e2e-affected L1 576/576.
Đang dở: REGRESSION C trọn bộ 6 project trên HEAD wave0 (sau T-22 + T-23), chạy nền từ ~23:57; lúc 00:00 đạt 418/1383, 0 trượt. Log: /Users/harry/.claude/jobs/74cc88b7/tmp/regC.log; bằng chứng: `app/e2e/evidence/<lượt mới nhất>/summary.json` (xem `app/e2e/evidence/latest.txt`). CHƯA có kết quả cuối.
Bước tiếp theo:
1. Đọc kết quả regression C (`summary.json` của lượt mới nhất trong `app/e2e/evidence/`); trượt thì chỉ chạy lại spec trượt, lỗi thật thì mở FIX. Ghi kết quả vào DECISIONS/PROGRESS.
2. Theo queue: 24 ‖ 25 (xong 25: regression B) → 26 gate L6 ‖ 42 → 27 → 28 ‖ 29 → 30 ‖ 31 → 32 → 33 gate L6 → 34. Hỏi owner model cho đợt kế (khuyến nghị Fable: ít token hơn ~34%/LoC, nhanh hơn ~18%).
3. Mỗi task: `git worktree add <mvp-wave0>/.claude/worktrees/<wt> -b <nhánh> HEAD` → `python3 ~/.claude/skills/epic-runner/queue.py --queue .claude/queue.md brief <ID> > $CLAUDE_JOB_DIR/tmp/briefNN.txt` → Agent đọc brief + Bổ sung (worktree tuyệt đối; epic-common v1.5; dùng `shared/ui/Icon`; TRƯỚC KHI BÁO XONG gộp wave0 mới nhất rồi chạy lại verify + web-build + e2e-affected; không commit; báo cáo ≤ 10 dòng kèm L0/L1/L2).
4. Lưu ý vận hành: lệnh git trong worktree lồng phải `cd` vào rồi git trơn, mỗi lệnh một lần gọi. KHÔNG `source scripts/env.sh` trong shell chạy make (đè cổng thành 8080). Nhánh có migration số thấp gộp vào CSDL thử đã ở số cao hơn → `dropdb geo_e2e` của cụm riêng worktree đó. Lượt e2e dài: cắm sạc, không gập máy.
Inbox còn: 7 việc mở mới (sau T-44/T-45, T-20, T-21, icon, T-23, T-22) + BA sửa AC góc 174,87° (sau T-19) + FIX sau T-41 (.claude/inbox.md).
Quyết định đã chốt: DECISIONS.md (mới nhất: đợt 22‖23 Dev chạy Opus; quy tắc migration khi gộp song song; icon một bộ cho 4 theme; giả định tạm T-20/T-21 tới gate T-26). Tự quyết chưa ghi DECISIONS: dời regression C tới sau khi T-22 gộp (một lượt phủ cả 22 + 23).
Rủi ro / chặn:
- CHƯA push: 150+ commit chỉ nằm trên máy (lệnh push bị lớp kiểm quyền từ chối). Owner chạy: `! git push origin worktree-mvp-wave0`.
- T-20…T-23 chưa qua soát độc lập (CLAUDE.md §7); mỗi task ~14–15 giả định tạm trong `epics/<epic>/qc_findings.md`.
- Test chập chờn còn mở: TC-GEO-0209 (e2/templates), TC-GEO-0804 (e1/isolation), TC-GEO-0539 (e5/catalog trên mobile-iphone).
- `LibraryQuota.consume` (T-23) chưa được gọi ở luồng tạo đề (T-22); font chữ vẫn gọi Google (chỉ icon đã tự lưu).
- T-05 đo thật cần owner chạy (trước T-34). Hạn mức tài khoản.
So sánh model (cùng quy trình e2e-affected, 2 epic mỗi bên): Fable 1,00M token quy đổi / 1.000 LoC, 146 LoC/phút, 20 lượt gọi / 1.000 LoC; Opus 1,51M, 124 LoC/phút, 39 lượt gọi. Thời gian mỗi epic 36–50 phút ở cả hai. Script đo: /Users/harry/.claude/jobs/74cc88b7/tmp/cmp.py (tạm, mất khi xoá job).
Việc owner: push (ở trên); T-05 — `! cd /Users/harry/IdeaProjects/GeometryMathSupport/.claude/worktrees/mvp-wave0/app && eval "$(grep '^export GEO_AI_API_KEY=' ~/.zshrc)" && GEO_AI_PROVIDER=anthropic make bench-mvp > /tmp/geo-bench-real.log 2>&1; echo rc=$?` (~$3).
