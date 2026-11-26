# PROGRESS · GeometryMath (GEO) · cập nhật 26/11/2026 16:35
Mục tiêu hiện tại: CODE tới hết chặng 3 (lệnh owner 26/11 "start coding"). Owner kiểm sản phẩm lúc cuối; delivery-lead tự quyết + ghi DECISIONS. UI theo HTML bản mẫu + theme D mặc định (đổi A/B/C/D trong Cài đặt). AI mock; T-05 chỉ chặn gate cuối T-34; T-27 delivery-lead chốt theo S2-L6/REPORT.md.
Đã xong:
- MVP: T-01..T-04 gộp (afda3dd, 77850f4, 6b62557); FIX5 (T-07) nghiệm thu + gộp (merge sau 6f096fc; verify 201 + 137 IT, bench-mvp mock exit 0).
- Plan chặng 2–3 rà xong → v0.2 (sửa lệnh kiểm T-05: báo cáo in "Kết luận chỉ số bắt buộc: **ĐẠT**" + provider), nạp `.claude/queue.md` + TASKS (T-08…T-34; queue validate 28 task) (48f7483).
- W1 xong: T-08 PO brief/BRD delta/story map 55 story, PO-62…65 TẠM (0b9ee97); T-10 bản mẫu 24 màn + DQ.md 28 câu TẠM (887b97e). Cả hai đã gộp.
- W2 xong: T-14 spec S3 21 story/280 AC/280 TC (1d16746); T-12 spec S2 35 story/442 AC/447 TC (9493739). Lint 0 lỗi, đã gộp. Câu mở → NEEDS C8, C9.
- Designer: câu hỏi cho Designer Dom ở `Design/Q_Dom.md` v0.2 (owner đang tạo designer cho chặng sau; không tự làm design chặng 4+).
- W3 (soát + thiết kế) xong: T-15 soát phản biện S2/S3 (gộp 1816344: 723 AC/728 TC 0 lỗi; RV_s23 v0.2 mở 0 blocker/0 major, 6 minor; tester domains/geo scope localhost confirmed:false, bản sao ở artifacts/GEO/work/tester/geo); T-36 Designer Dom (gộp 2be47fa: 45 ảnh, Q_Dom v0.3, DQ-29…43).
- Gate T-16 xong 26/11: owner "OK hết" → PO-66 + DECISIONS "S23 R3 OK" (53c4782). Owner giao: delivery-lead tự quyết, owner kiểm sản phẩm cuối.
- Sau gate: T-37 spec áp PO-66 (ebec17a, 58 story / 746 AC / 751 TC); T-38 bản mẫu kịch bản 7 KB (b4610fb, không tự chạy ebedee5); T-39 4 hướng style (3d8c00b, TẠM: A cho HS, D cho GV); PDF style (6fc2493) + PDF kịch bản 61 trang (2158e7a); T-17 PLAN_code-s23 v1.0 (30df78c: 15 task code, mã mới 41 khung chung / 42 seed S3).
- T-40 vẽ 15 màn còn thiếu (d1d5b87, kịch bản 61 bước, PDF 70 trang). T-17 PLAN_code (30df78c).
Đang dở (nền, 2 Opus):
- T-261126-41 khung chung S2/S3 (wt `.claude/worktrees/s23-base`, runner-T-261126-41.md).
- T-261126-50 hệ theme D mặc định + Cài đặt đổi theme (wt `.claude/worktrees/s23-theme`, runner-T-261126-50.md).
Bước tiếp theo:
1. Mỗi agent xong: chạy lại khối Kiểm trong queue (gốc repo) → cd vào worktree commit → merge --no-ff → header queue [done] (sửa tay) → `queue.py next` → tạo worktree (git -C … worktree add <đường dẫn tuyệt đối>) → `queue.py brief <ID>` > $CLAUDE_JOB_DIR/tmp/briefNN.txt → Agent Opus đọc brief.
2. Thứ tự: 41‖50 → 18‖19 → 20‖21 → 22‖23 → 24‖25 → 26‖42 → 27 (delivery-lead) → 28‖29 → 30‖31 → 32 → 33 → 34 (owner + T-05).
3. Sau mỗi epic lớn: soát độc lập (Sonnet, chỉ diff + Xong khi). Ngữ cảnh phiên chính ~450k → ghi PROGRESS, đề nghị /clear.
Inbox còn: không mục mở (FIX5 đóng).
Quyết định: DECISIONS.md (mới nhất: duyệt plan chặng 2–3, model theo vai); PO-DECISIONS.md (PO-61).
Việc owner: T-05 đo thật — `! cd <worktree>/app && eval "$(grep '^export GEO_AI_API_KEY=' ~/.zshrc)" && GEO_AI_PROVIDER=anthropic make bench-mvp > /tmp/geo-bench-real.log 2>&1; echo rc=$?` (~$3); NEEDS A2, C1–C3.
Rủi ro / chặn: code chặng 2 chờ B7 (T-05). Không commit/log key. Ước tính toàn plan ≈ 190M token / 89 agent·giờ → nguy cơ chạm hạn mức; chạy theo đợt, dừng ở đợt đã gộp xanh.
