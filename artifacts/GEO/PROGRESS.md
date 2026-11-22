# GEO — progress (running)

> Read this first in a new session. Then `board/CONVENTIONS.md`, `board/BOARD.md` §0 and the newest messages. Skills: po-ba-qc (roles/po.md v3.04, 07_GUIDE_goal_run v1.00); shared rules: `references/02_method.md`, `03_mindset.md`, `04_collaboration-playbook.md` của skill (chỉ đọc).

## 22-11-2026 — Goal run GEO (Human approved: appetite lean, thẻ `artifacts/GOALS.md`)
| Step | Who / model | Task | Budget | Status |
|---|---|---|---|---|
| S0 | PO | hồ sơ dự án `references/profiles/bambooai/domain.md`, `team_profile.md` | lean | done — 2 tệp |
| S1 | PO | kickoff, inputs, sources (120 trích dẫn), glossary, VoC 55 REQ (R0 do PO) | lean | done |
| S2 | PO | vision, BRD, understanding (15 xung đột), domain model (24 chỉ số), decisions (65 dòng, 46 giả định), questions (R1 do PO) | lean | done |
| S3 | PO | story map 53 story, V14 (9 phát hiện đã xử lý), epic board (R2 do PO, khóa) | lean | done |
| Board | PO thay A | CONVENTIONS, PACK, G5b, BOARD §0 | lean | done |
| E1…E7 | BA ‖ QC ‖ ARCH | story và AC; test case; kiến trúc và NFR | — | waiting |
| Code | agent mã | khung mã Java trong `app/` | — | chạy song song, ngoài phạm vi PO |

**Token ledger:** PO-FOUNDATION: số ước tính ghi trong báo cáo cuối của lượt này (chưa có số đo chính xác); tổng đã duyệt: lean (không ghi số).

## Where the work stands
| Area | State |
|---|---|
| Gates | Gate 0 đến 3 xong (PO); R0, R1, R2 "Not reviewed (TBD) — goal run, ruled by PO" |
| Epics (R3) | E1…E7: R2 locked (phần PO); AC, TC chưa làm; chưa epic nào "ready for R3" |
| Package | 55 REQ · 20 BR · 53 story sơ bộ · 24 chỉ số · 65 dòng quyết định (16 Final, 46 Assumption, 3 nguồn tài liệu) · 32 câu hỏi |
| Last change | không có CR |
| Checks | lint `check_feature.py`: thiếu story, test spec, test plan do chưa tới lượt BA, QC; V14 trên bản đồ story: 9 phát hiện đã xử lý |
| Questions | ChuDauTu 18 · GiaoVienCoVan 8 · TechLead 6 (tất cả Open, đều có mặc định đang dùng) |

## Open items to pick up
1. BA: đọc `SPEC.md` cho danh sách công cụ hình (KG-01), mở docx giáo án (KG-02), xem ảnh `mvp-01, 06, 07, 08` (KG-03) — BA — `work/known_gaps.md`.
2. QC: đề xuất số câu và nhóm kỹ thuật bộ câu thử (KG-04) — QC — PO phán.
3. ARCH: viết `work/architecture/` và `NFR-GEO-nnn`, đề xuất định nghĩa "gần trùng" cho D-GEO-026 — ARCH — `work/03_decisions.md`.
4. A: chạy lại lint và ledger khi BA, QC xong; cập nhật README.
5. Nguồn mâu thuẫn cần chủ đầu tư: xem `work/01_understanding.md` mục 2 (C-01…C-15).

## How to regenerate
Không có bộ sinh trong lượt PO. Kiểm: `python3 <skill>/tools/lint/check_feature.py artifacts/GEO` (đọc thư mục skill, không ghi vào đó).
