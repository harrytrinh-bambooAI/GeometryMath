Phiên bản: v0.1 · 2026-11-26 · quyết định đã chốt — 1 dòng / quyết định. Đọc trước khi hỏi lại hoặc đổi hướng.

| Ngày | Quyết định | Lý do | Nguồn (giờ raw / NEEDS §B#) | Ai chốt |
|---|---|---|---|---|
| 2026-11-26 | Dùng delivery-lead v0.2, small_task = direct | phiên ngắn | setup L0 | Harry |
| 2026-11-26 | Quyết định sản phẩm vẫn ghi ở `PO-DECISIONS.md` (PO-xx); tệp này chỉ ghi quyết định điều phối | tránh hai nguồn quyết định | setup L0 | phiên chính |
| 2026-11-26 | Hoàn thành MVP = PO-60 (xem PO-DECISIONS.md); 2 agent Opus song song T-261126-01 ‖ T-261126-02 | AskUserQuestion 26/11 | PO-60 | Harry |
| 2026-11-26 | `make bench-mvp` với mock chỉ đo các đề có câu trả lời mẫu (6 HKG, kiểm hồi quy CI); đo đủ 52 đề (gồm 46 HKN) chỉ với `GEO_AI_PROVIDER=anthropic` (T-05) | mock trả rỗng cho đề mới → 11,5 % là giả tạo, không phản ánh chất lượng | T-03 gộp, bench 13:00 trượt | phiên chính (TẠM) |
| 2026-11-26 | `small_task: delegate`: mọi việc code giao agent con, mỗi task một worktree + nhánh riêng; phiên chính chỉ điều phối + nghiệm thu + gộp | quản lý phiên bản mã nguồn tốt, giữ ngữ cảnh phiên chính nhỏ | lệnh Harry 26/11 | Harry |
| 2026-11-26 | Plan chặng 2–3 (`.claude/plans/STAGE-2-3-PLAN.md`) duyệt; chạy tới hết chặng 3. Model: code (Dev) = Opus; design/spec (PO, BA, QC, thiết kế UI) = Sonnet; Tester (skill tester + Playwright) và soát độc lập = Sonnet; delivery-lead (phiên chính) điều phối, nghiệm thu, gộp | owner duyệt | lệnh Harry 26/11 | Harry |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo từ delivery-lead/templates/DECISIONS.md | |
