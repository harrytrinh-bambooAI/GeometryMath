# GEO — package index

> Chỉ dùng tiếng Việt có dấu; không mốc thời gian. Phiên bản hiện hành mỗi epic: xem bảng Epics.

| Status | |
|---|---|
| Gate reached | PO-FOUNDATION: Gate 0 đến Gate 3 xong (R0, R1, R2 do PO lấy, "Not reviewed (TBD)"); BA, QC, ARCH chưa bắt đầu |
| Epics | E1…E7: story map v1 (R2 locked); stories_ac v0 (chưa có); test_cases v0 (chưa có); R3 chưa |
| Counts | 55 REQ · 53 story sơ bộ · 0 AC · 0 TC · 65 dòng quyết định · 0 CR |
| Highest-risk open item | khóa lời giải ở tầng dữ liệu và bộ câu thử moi đáp án (E6); đa trường D-GEO-050 |

## Reading path by role
- Dev: `board/PACK.md` → `work/04_story_map.md` → `work/epics/<EPIC>/stories_ac.md` (khi BA xong) → `work/architecture/` (khi ARCH xong). · QC: `PACK.md` → `work/02_domain_model.md` → `work/epics/<EPIC>/stories_ac.md`. · PO: `work/03_decisions.md`, `questions_for_stakeholders.md`. · Agent tiếp tục: `PROGRESS.md` trước.

## Files
| # | File | What it is |
|---|---|---|
| — | `PROGRESS.md`, `board/*` | ghi chú tiếp tục; bảng chung và gói kiến thức |
| 00 | `work/00_kickoff.md`, `00_inputs.md`, `00_sources.md`, `glossary.md` | kickoff, đăng ký đầu vào, trích nguồn, thuật ngữ |
| 00 | `work/00_gate0_intake_voc.md` | 55 REQ và độ phủ |
| 01 | `work/01_gate1_vision_alignment.md` | vấn đề, IN / OUT và hệ quả |
| 02 | `work/02_gate2_brd.md`, `actor_profiles.md` | BRD, persona |
| — | `work/01_understanding.md`, `02_domain_model.md`, `03_decisions.md` | xung đột, công thức, quyết định |
| 03 | `work/03_gate3_preliminary_stories.md`, `04_story_map.md` | story sơ bộ và bản đồ (khóa R2) |
| — | `work/questions_for_stakeholders.md`, `questions/Q_*.md` | câu hỏi gộp và theo người |
| — | `work/epic_board.md`, `review_status.md`, `known_gaps.md` | trạng thái, review, khoảng trống |
| — | `references/profiles/bambooai/` (thư mục gốc dự án) | hồ sơ domain và đội |
