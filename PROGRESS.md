# PROGRESS · GeometryMath (GEO) · cập nhật 26/11/2026 23:00
Mục tiêu hiện tại: CODE tới hết chặng 3. Phiên điều phối + agent con Dev đều chạy Fable (DECISIONS 26/11 tối), luôn 2 agent song song (≤ 2 Opus/Fable). Delivery-lead tự quyết + ghi DECISIONS; owner kiểm sản phẩm cuối. Nhánh làm việc: `worktree-mvp-wave0` (repo `/Users/harry/IdeaProjects/GeometryMathSupport/.claude/worktrees/mvp-wave0`).
Tiến độ epic (code đã gộp): 4/15 — S2-E1 (T-18), S2-E2 (T-19), S2-E5 trừ US-05 (T-20, aeff407), S2-E8 (T-21, 75af376).
Đã xong phiên này (26/11 tối, phạm vi owner chọn "2 Fable tới hết 20‖21"):
- T-45 `make e2e-affected` + smoke + reporter (b5f15e5): 26 test trượt L2 do máy ngủ, chạy lại 139/139; e2e.sh có caffeinate + cảnh báo máy ngủ.
- T-44 theme hoàn thiện (fd23b06): e2e theme + e1–e7 369/369, s2e1 + s2e2 48/48.
- T-20 S2-E5 (gộp sau aeff407): verify 237 UT + 172 IT, e2e-affected L1 409/410 (TC-GEO-0209 chập chờn, chạy lại 18/18).
- T-21 S2-E8: bản kết hợp với T-20 verify 266 UT + 178 IT, e2e-affected L1 594/594; kèm FIX test chập chờn `RC-` của S2-E1 (970f4e3).
- Icon chung (lệnh owner): Design/styles v0.2 (1377925) + app (nhánh fix-icon đã gộp): Material Symbols Rounded đặc ở 4 theme, màu theo token, font tự lưu 7 KB; L2 540/542 → sửa 2 test icon → affected 199/199.
Đang dở: không có agent nào chạy. Chưa có lượt trọn bộ nào chạy trên HEAD cuối (sau khi gộp icon).
Bước tiếp theo (cần owner ra lệnh, phạm vi đã chọn kết thúc ở 20‖21):
1. Theo queue: 22 ‖ 23 (xong 23: regression C) → 24 ‖ 25 (regression B) → 26 gate L6 ‖ 42 → 27 → 28 ‖ 29 → 30 ‖ 31 → 32 → 33 gate L6 → 34.
2. Mỗi task: `git worktree add <mvp-wave0>/.claude/worktrees/<wt> -b <nhánh> HEAD` → `python3 ~/.claude/skills/epic-runner/queue.py --queue .claude/queue.md brief <ID> > $CLAUDE_JOB_DIR/tmp/briefNN.txt` → Agent Fable đọc brief + Bổ sung (worktree tuyệt đối, epic-common v1.5, gộp wave0 trước kiểm cuối, không commit, báo cáo ≤ 10 dòng kèm L0/L1/L2).
3. Khi ghép 2 epic song song: gộp wave0 vào nhánh sau, chạy lại `make verify && make web-build && make e2e-affected` trên bản kết hợp. Nếu nhánh sau có số migration CAO hơn bản vừa gộp vào: `dropdb geo_e2e` của worktree đó trước khi chạy e2e (Flyway tắt out-of-order).
4. Lệnh git trong worktree lồng: `cd` vào rồi git trơn, mỗi lệnh một lần gọi (guard chặn lệnh ghép). KHÔNG `source scripts/env.sh` trong shell chạy make (đè cổng thành 8080).
Inbox còn: 5 việc mở mới (sau T-44/T-45, sau T-20, sau T-21, sau icon) + BA sửa AC góc 174,87° (sau T-19) + FIX sau T-41 (.claude/inbox.md).
Quyết định đã chốt: DECISIONS.md (mới nhất 26/11 đêm: Dev = Fable; gộp T-45 không chạy lại L2; icon một bộ cho 4 theme; giả định tạm T-20/T-21 tới gate T-26; quy tắc migration khi gộp song song).
Rủi ro / chặn:
- Lượt e2e dài: cắm sạc, không gập máy (máy ngủ làm hỏng lượt).
- T-20, T-21 chưa qua soát độc lập (CLAUDE.md §7); 14 + 15 giả định tạm ở epics/S2-E5, S2-E8 `qc_findings.md`.
- Test chập chờn còn mở: TC-GEO-0209 (e2/templates), TC-GEO-0804 (e1/isolation).
- T-05 đo thật cần owner chạy (trước T-34). Hạn mức tài khoản.
So sánh model (26/11, 2 epic mỗi bên): Fable ~1,00M token quy đổi / 1.000 LoC, Opus ~1,86M; script `/Users/harry/.claude/jobs/74cc88b7/tmp/cmp.py` (tạm, mất khi xoá job).
Việc owner: T-05 — `! cd /Users/harry/IdeaProjects/GeometryMathSupport/.claude/worktrees/mvp-wave0/app && eval "$(grep '^export GEO_AI_API_KEY=' ~/.zshrc)" && GEO_AI_PROVIDER=anthropic make bench-mvp > /tmp/geo-bench-real.log 2>&1; echo rc=$?` (~$3).
