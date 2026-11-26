Phiên bản: v1.0 · 2026-11-26 · GEO — sổ task DUY NHẤT (inbox = ghi thô; nhận làm thì vào đây)
Giao diện cố định (giữ khi đổi nguồn ID sang PMS): ID · Việc · Nguồn (giờ raw) · Xong khi · Ước tính · Trạng thái · Output
Trạng thái: Todo · Doing · Wait-User · Blocked (cần gì) · Done <giờ> · Dropped (lý do). Giờ lấy từ `date`.
Task giao agent con có khối `.claude/queue.md` CÙNG ID; map trạng thái ở delivery-lead `framework.md` §3.

## Đang mở — Claude tự làm
| ID | Việc | Nguồn (raw) | Xong khi (lệnh kiểm) | Ước tính | Trạng thái | Output |
|---|---|---|---|---|---|---|
| T-261126-01 | AI thật: AnthropicAiProvider (Claude, mặc định claude-sonnet-5-5) + ProfileGuard cấm mock ở prod + bộ đo MVP (`make bench-mvp`: 4 chỉ số bắt buộc + thời gian tới hình + chi phí AI/bài) | PO-60 | verify + web-build + e2e 2 lượt xanh; `make bench-mvp` (mock) in báo cáo | 3–4h, Opus | Doing | nhánh mvp-ai |
| T-261126-02 | Kho ≥ 40 đề máy kiểm chứng (nhãn AI soạn) + kiểm P0 khổ điện thoại (Playwright mobile) | PO-60 | verify + web-build + e2e 2 lượt xanh (gồm dự án mobile); đếm đề verified ≥ 40 | 3–4h, Opus | Doing | nhánh mvp-content |
| T-261126-03 | Gộp 01+02 vào worktree-mvp-wave0, chạy bench-mvp trên 40 đề (mock) | PO-60 | lệnh kiểm profile §2 xanh sau gộp | 1h | Todo | |
| T-261126-04 | Soát độc lập diff 01+02 (Sonnet) + sửa gom | §7b | 0 blocker/major mở | 1–2h | Todo | |
| T-261126-05 | Đo thật 4 chỉ số với Claude API, báo cáo `artifacts/GEO/evidence/MVP-METRICS-<ngày>/` | PO-60 | báo cáo có đủ chỉ số §9 | 30' | Todo | |
| T-261126-06 | Kiểm cuối MVP + cập nhật PR #1 + PROGRESS | §9 | profile §2 + traceability xanh | 1h | Todo | |

## Chờ owner (chi tiết ở NEEDS.md)
| ID | Việc | Cần gì | Mở khoá |
|---|---|---|---|

## Đã xong / bỏ (chuyển xuống đây, giữ 7 ngày)
| ID | Việc | Trạng thái | Output |
|---|---|---|---|

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo từ delivery-lead/templates/TASKS.md | |
