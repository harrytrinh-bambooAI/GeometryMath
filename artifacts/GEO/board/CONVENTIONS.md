# GEO — working conventions for the PO / BA / QC / ARCH agents

> Owner: **A** (orchestrator). Agents read this before each task. A rule decided once is written here. BA, QC và ARCH chạy song song; sửa tệp của vai khác là vi phạm O1.

## 0. How this package is built (read once)
- **Nguồn sự thật là các tệp markdown của chủ sở hữu** (bảng §1). Không bắt buộc bộ sinh mã (`gen_epic_*.py`); nếu BA hoặc QC dựng bộ sinh thì tệp sinh ra không sửa tay, sửa bộ sinh. Sau mỗi lần sửa chạy `python3 tools/lint/check_feature.py artifacts/GEO` của skill (đọc thư mục skill, không ghi) và chỉ đọc dòng tóm tắt.
- **Giá trị kỳ vọng** lấy từ `work/02_domain_model.md` (công thức M-GEO-nn, một công thức mỗi con số) và `work/fixture.md` (BA lập, hàng `F-GEO-nnn`). Thuật ngữ hiển thị đúng `work/glossary.md` (nguồn duy nhất, không dùng "banned synonyms").
- **Id ổn định.** Story hay AC bị bỏ thì gạch bỏ (kèm lý do), case giữ kết quả N/A. Không đánh số lại, không dùng lại id.
- **Không mốc thời gian** trong mọi tệp (không ngày giao, tuần, tháng, hạn chót); ngày chỉ dùng làm dấu vết sửa tệp. Ngôn ngữ: tiếng Việt có dấu; mã định danh, tên tệp, tên cột ASCII.
- **Final của chủ đầu tư không đổi.** Mọi yêu cầu dẫn nguồn (tệp + mục) hoặc `SRC-nnn` ở `work/00_sources.md`. Điều không có nguồn là giả định: ghi vào `03_decisions.md` do PO.
- **AI giả lập ở MVP** (D-GEO-047): test case không giả định hành vi AI thật; chỉ số cần AI thật đánh dấu PROVISIONAL.

## 1. Roles and owned files
| Role | Who | Owns (only this role edits) |
|---|---|---|
| A | phiên chính (điều phối cơ học) | `board/CONVENTIONS.md`, `board/BOARD.md` §0, `PROGRESS.md`, `work/cr_register.md`, `work/epic_board.md` (trừ cột Stage "R2 locked" do PO), `README.md`, `ai_change_log.md`, `work/metrics_ledger.*` |
| PO | agent | `Input/GeometryMath/GEO/brief.md` (chỉ Revision log), `work/00_*.md`, `work/glossary.md`, `work/01_understanding.md`, `work/02_domain_model.md`, `work/03_decisions.md`, `work/04_story_map.md`, `03_gate3_preliminary_stories.md`, `01_gate1_*`, `02_gate2_brd.md`, `work/actor_profiles.md`, `work/questions_for_stakeholders.md`, `questions/Q_*.md`, `work/known_gaps.md`, `work/review_status.md`, `board/PACK.md`, `board/G5b_key.md`, `references/profiles/bambooai/*`, `review/PO_review.md` |
| BA | agent | `work/epics/*/stories_ac.md`, `work/fixture.md`, `work/05_dictionary.md`, `work/src/` (phần story), `review/BA_review.md`, `work/epics/*/questions.md` (phần BA) |
| QC | agent | `work/epics/*/test_design.md`, `test_cases.md`, `qc_findings.md`, `review/test_plan.md` (PO duyệt §14), `review/QC_test_design.md`, `work/backlog_cases.md` |
| ARCH | agent (kiến trúc sư dữ liệu và phi chức năng; vai ngoài skill, người chịu trách nhiệm đăng ký: TechLead) | `work/architecture/` (toàn bộ), các `NFR-GEO-nnn`, `ADR-GEO-nn`, `work/epics/*/questions.md` (phần ARCH); đề xuất schema dữ liệu và truy vấn |
| RV | agent trên **mô hình khác** | `review/RV_findings.md` duy nhất; tìm cái sai, không bao giờ duyệt |
| Code | agent mã Java | `app/` (khung mã); không sửa `artifacts/` |
| Human | chủ đầu tư | trả lời nghiệp vụ; duyệt đổi phạm vi; câu hỏi cho các stakeholder chỉ đi qua chủ đầu tư |
**Rule O1:** một vai chỉ sửa tệp của mình; muốn đổi tệp vai khác thì gửi `proposal` lên bảng chung. **Rule O2:** nếu cần giá trị của ARCH (ví dụ lược đồ bảng) thì trỏ tới `architecture/<tệp>#mục`, không chép.

## 2. Reserved ID ranges (parallel safety)
Id đã dùng trước lượt này: `REQ-GEO-01…55`, `BR-GEO-01…20`, `D-GEO-001…079` (có chỗ trống), `M-GEO-01…24`, `US-GEO-101…108, 201…205, 301…307, 401…412, 501…505, 601…612, 701…704`, `XF-01…09`, `KG-01…10`, `Q-CDT-01…18`, `Q-GV-01…08`, `Q-TL-01…06`.

| Loại id | PO | BA | QC | ARCH |
|---|---|---|---|---|
| Story `US-GEO-nnn` (hàng trăm = epic) | x01…x19 (đã dùng) | x20…x59 mỗi epic, chỉ sau một quyết định của PO | không | không |
| AC `…-SCn` / `-FCn` | không | số kế tiếp trong story | không | không |
| Quy tắc `BR-GEO-nn` | 21…99 | 100…199 (làm rõ, dẫn quyết định PO) | không | không |
| Case `TC-GEO-nnnn` | không | không | E1 0100…0199 · E2 0200…0299 · E3 0300…0399 · E4 0400…0499 · E5 0500…0599 · E6 0600…0699 · E7 0700…0799 · xuyên epic (X) 0800…0899 · 0900…0999 dành khi PO phán | không |
| Dữ liệu mẫu `F-GEO-nnn` | không | 001…199 | 200…299 | 300…399 (dữ liệu thử tải, tên bảng) |
| Yêu cầu phi chức năng `NFR-GEO-nnn` | 900…999 | không | không | 001…899 |
| Quyết định kiến trúc `ADR-GEO-nn` | không | không | không | tất cả |
| Quyết định `D-GEO-nnn` | 001…099 (đã dùng tới 079) | không (gửi `proposal`, PO ghi 100…199) | không (như BA) | không (gửi `proposal`, PO ghi 200…299) |
| Hỏi đáp `QA-GEO-nn` (trong `epics/*/questions.md`) | không | 01…39 | 40…69 | 70…99 |
| Thông báo `MSG-GEO-nn` | không | 01…59 | 60…89 | 90…99 |
| Phát hiện chéo epic `XF-nn` | 01…19 | 20…39 | 40…59 | 60…79 |
| Known gap `KG-nn` | 01…19 | 20…39 | 40…59 | 60…79 |
| Chỉ số `M-GEO-nn` | 01…99 | không | không | không |
| Tin nhắn bảng chung `M-nnn` | 300… | 200… | 100… | 400… (RV 500…, A 001…099) |
Câu hỏi cho stakeholder (`Q-CDT`, `Q-GV`, `Q-TL`) do PO viết; BA, QC, ARCH đề xuất qua `questions.md` hoặc bảng chung, PO gộp. Mã mới ngoài bảng này: đề xuất tới A.

## 3. The board
`board/BOARD.md` là kênh duy nhất. Thêm dưới §1, mới nhất ở cuối, bằng một lệnh `cat >>`. Dải số: A M-001…099, QC M-100…, BA M-200…, PO M-300…, ARCH M-400…, RV M-500…. Định dạng và loại tin: `04_collaboration-playbook.md` §11.3 của skill (status, question, proposal, conflict, decision, escalate, agree).

## 4. Rules
| ID | Rule |
|---|---|
| R1 | **Source first.** Trích nguồn gốc (tệp + mục, hoặc `SRC-nnn`). Ý kiến của agent khác không phải nguồn. |
| R2 | **Bounded debate:** ≤ 2 lượt mỗi phía, rồi PO phán (xem R13). |
| R3 | **Escalate, don't guess.** Không có nguồn thì không tự đặt: gửi `proposal` cho PO; giữ mặc định đã ghi, đánh dấu AC và case PROVISIONAL kèm mã D-GEO. |
| R4 | **Decision log.** Mọi thread nghiệp vụ đã đóng → một dòng D- (PO); quy tắc quy trình → tệp này (A). |
| R5 | **Checkpoints** CP-1 bắt đầu (câu trả lời G5b ở đầu báo cáo) · CP-2 ≈ 50% · CP-3 bản nháp xong và lint sạch · CP-4 cross-review đóng. |
| R6 | **Cross-review chỉ trên chỗ chồng nhau** (AC ↔ case, quyết định ↔ giá trị kỳ vọng, epic ↔ epic, NFR ↔ AC của khóa lời giải). |
| R7 | **RV** soát phần thay đổi trước bản cuối; mỗi phát hiện sửa hoặc bác kèm nguồn và quyết định của PO. |
| R8 | **Anti-sycophancy.** Lời đồng ý nói rõ đã kiểm gì ở đâu; nếu không là "not reviewed". |
| R9 | A nén các thread đã đóng vào BOARD §0 sau mỗi checkpoint. |
| R10 | **Run discipline.** Sau khi sửa chạy lint và chỉ đọc dòng tóm tắt hoặc FAIL. Tối đa 3 lượt E5…E8 mỗi epic. |
| R11 | **Token economy.** grep và khoảng dòng; không đọc nguyên tệp > 30 KB; báo cáo token đã dùng. |
| R12 | **Open items** (câu hỏi thiết kế, To BA, Dev-confirm) vào `work/known_gaps.md` kèm chủ và cổng cần có (không ghi ngày). |
| R13 | **Goal run** (`07_GUIDE_goal_run.md`): sau 2 lượt thì **PO** phán; điểm không có nguồn không phải `escalate` chờ mà là **giả định PO** (`03_decisions.md`, "Assumption (PO) — confirm with <người>"), dấu PROVISIONAL và một khối ở `questions/Q_<Tên>.md`. A chỉ phán cơ học. |
| R14 | **Không mốc thời gian, không số tuần, tháng, hạn chót**, kể cả trong ví dụ (dùng mốc tương đối như T0 + 60 giây). Không tên người hay tổ chức thật. |
| R15 | **Không kéo chặng 2 đến 9 vào MVP.** `STAGES-AFTER-MVP.md` không phải nguồn. Điều khiển có trên khung thiết kế mà không có nguồn: không xây phía sau (D-GEO-078). |
| R16 | **Không để AI tự quyết:** nhãn "Đã kiểm chứng" chỉ do engine tính (BR-GEO-07, D-GEO-004); không tự công bố điểm; lời giải AI luôn "AI soạn". Ngưỡng hiệu năng giữ nhãn "đề xuất". |
| R17 | **Khóa lời giải ở tầng dữ liệu trả về** (BR-GEO-01): mọi story và case chạm lời giải phải có ca kiểm dữ liệu trả về, không chỉ giao diện; ARCH chịu trách nhiệm thiết kế bảng riêng. |
| R18 | **ARCH không đổi phạm vi.** Phát hiện yêu cầu phi chức năng mới: gửi `proposal` cho PO; NFR dẫn REQ-GEO-37…50 hoặc D-GEO. Ngưỡng nào có trong brief giữ nguyên nhãn đề xuất. |
| R19 | **Độ phủ:** mỗi REQ có story, hoặc OUT có lý do (`work/04_story_map.md`); mỗi AC trỏ về story, mỗi case trỏ về AC; đối chiếu 100% hai chiều. |
| R20 | **Đổi một Final hay đảo một quyết định là CR** (`work/cr_register.md`), qua vòng thay đổi; không sửa tệp đã giao. |
