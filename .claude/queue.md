# Hàng đợi agent con · GEO
Phiên bản: v0.1 · 2026-11-26 · tạo từ delivery-lead/templates/queue.md. Định dạng: đầu tệp `<skill>/scripts/queue.py`.
<!-- Mỗi khối = 1 task trong .claude/TASKS.md, CÙNG ID. Trạng thái: todo | running | wait-user | done | failed | blocked | skip. -->
<!-- Dòng thường (không phải #, <!--, Phiên bản) đặt ở đây = ghi chú chung, được chèn vào brief của MỌI task. -->
<!-- Khối dưới là mẫu (skip, không được giao). Xoá hoặc sửa khi có task thật. -->

## T-261126-01 · AI · AI thật + ProfileGuard + bộ đo MVP [done]
- Mục tiêu: AnthropicAiProvider sau AiProvider; prod cấm mock; `make bench-mvp` đo §9
- Nguồn: MVP.md §8–9; PO-60 (26/11); brief .claude/briefs/T-261126-01-ai.md
- Xong khi: verify + web-build + e2e 2 lượt xanh; bench-mvp (mock) ra báo cáo
- Kiểm: `cd .claude/worktrees/mvp-ai/app && make verify`
- Ngoài phạm vi: nội dung đề, giao diện ngoài nhãn AI
- Model: opus · Ngân sách: 4 giờ

## T-261126-02 · CONTENT · Kho 40 đề + P0 điện thoại [done]
- Mục tiêu: ≥ 40 đề verified (AI soạn) + Playwright mobile cho P0
- Nguồn: MVP.md §5, §9; PO-60 (26/11); brief .claude/briefs/T-261126-02-content.md
- Xong khi: verify + web-build + e2e 2 lượt xanh (gồm mobile); ≥ 40 đề verified
- Kiểm: `cd .claude/worktrees/mvp-content/app && make verify`
- Ngoài phạm vi: aigateway, ProfileGuard, bench
- Model: opus · Ngân sách: 4 giờ

## T-261126-07 · FIX5 · 3 minor soát T-04 [running]
- Mục tiêu: đóng inbox FIX5 #1–#3, mỗi mục có test
- Nguồn: .claude/inbox.md "FIX5" (26/11); brief .claude/briefs/T-261126-07-fix5.md
- Xong khi: verify xanh; bench-mvp mock exit 0
- Kiểm: `cd .claude/worktrees/fix5/app && make verify && make bench-mvp`
- Ngoài phạm vi: dữ liệu đề, e2e spec, worktree-mvp-wave0
- Model: sonnet · Ngân sách: 60 phút

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo từ delivery-lead/templates/queue.md | |
