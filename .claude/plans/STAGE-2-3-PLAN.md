Phiên bản: v0.2 · 2026-11-26
# Plan giao việc chặng 2 (Trọn môn Hình học) + chặng 3 (Lớp học thật) · GEO · nháp chờ owner duyệt
Nguồn (đọc 26/11): `PRODUCT-MASTER.md` (22/11) B7, C4, C12, D1–D3 · `STAGES-AFTER-MVP.md` (22/11) §0.2–0.4, Chặng 2, Chặng 3, K2, K3 · `PO-DECISIONS.md` PO-05/13/18/21/25/27–32/45–49/53/61 · delivery-lead `lifecycle.md` v0.2, `orchestration.md` v0.2 · po-ba-qc, tester SKILL · `.claude/delivery-profile.md` v1.0, `briefs/epic-common.md` v1.1, TASKS/NEEDS/DECISIONS 26/11.
Owner đã chốt (26/11, ghi vào plan): PO-61 định danh tối giản; quên mật khẩu = giáo viên đặt lại cho học sinh lớp mình, giáo viên tự khôi phục bằng mã khôi phục (TẠM); model theo vai: Dev code = **opus**; PO/BA/QC, thiết kế UI, Tester, soát = **sonnet**; song song Opus ≤ 2, tổng quy đổi ≤ 4 (1 Opus = 2 Sonnet).
## 1. Phạm vi
| Epic | Nội dung (P0 trừ khi ghi P1) | Nguồn |
|---|---|---|
| S2-E1 | Định danh tối giản (PO-61): đăng ký chỉ bằng tên hiển thị + vai → hệ thống sinh mã người dùng; đăng nhập mã + mật khẩu; mã khôi phục cho giáo viên; khóa theo tài khoản như PO-53. Chuyển tài khoản mẫu MVP sang mô hình mới. | PO-61; Ch2 §4 (thay "lời mời"); Ch3 §3 ý 1–2 |
| S2-E2 | Khung môn học (`parse/solve/verify/render/hint` + `grade`, `concepts`) và hình phẳng dựng từ ràng buộc, `Problem.kind`, `SceneSpec` 2D, màn làm bài 2D (C2-05); bộ đề chuẩn hình phẳng + `make bench-s2`. | Ch2 §3 ý 1, §8, §9; §0.3 ý 1 |
| S2-E3 | Thêm đề có AI soạn nháp: dán chữ hoặc ảnh (đọc ảnh), 4 bước đọc–dựng–giải–kiểm, ngoại lệ ảnh mờ / engine không dựng / lệch đáp số (C2-02). | Ch2 §5 luồng A; PO-27, PO-30 |
| S2-E4 | Soạn với AI (P1 theo Q5): chat chỉnh bản nháp đề (AI hỏi lại, nói rõ trước/sau), import file docx/pdf tách bài tập, đối chiếu trùng kho. | D1 chặng 2; PO-45–47 |
| S2-E5 | Soát và duyệt lời giải có phiên bản: sửa từng bước, `StepCheck` kiểm số từng bước, nhãn hai trục, hộp duyệt chung (nhận/sửa/bỏ), lời giải đã giao bất biến (C2-03). | Ch2 §3 ý 3–4, §7; §0.3 ý 2, 4 |
| S2-E6 / S2-E7 | E6 Kho đề mở rộng: ngăn "Của tôi" / "Kho chung", vai người soát nội dung, lọc loại hình (C2-01). E7 Trình chiếu toàn màn hình: hiện từng bước, che lời giải, chạy được khi mất mạng, hỏi lại khi lời giải còn khóa (C2-04). | Ch2 §4, §7; Ch2 §5 luồng B |
| S2-E8 | Bài luyện điền đáp số: bàn phím toán iPad/iPhone, chấm dạng tương đương, lượt thử, kết quả; soạn bài luyện (C2-06/07/08). | Ch2 §3 ý 6, §5 luồng C |
| S2-E9 | P1: biến thể đề (`ProblemVariant`), xuất ảnh/PDF, liên kết chỉ xem, trắc nghiệm; thiết diện động nếu Q7 chọn. | Ch2 §4 P1; PO-25 |
| S3-E1 | Lớp học: nhiều lớp (`academic_year`, `status`), mã lớp + trang in, vào lớp chờ duyệt (`ClassMembership`), giáo viên đặt lại mật khẩu học sinh (C3-02, C3-07, C3-01 rút gọn). | Ch3 §5 luồng A; PO-61 |
| S3-E2 | Bộ bài: `AssignmentSet/Item`, hạn làm (chỉ là nhãn), lịch mở lời giải theo bộ hoặc từng bài, xem trước, cảnh báo đề "Cần xem lại" (C3-03, C3-08). | Ch3 §5 luồng B; PO-25 |
| S3-E3 | Kết quả lớp: bảng lớp × bài (ô = trạng thái có bằng chứng, không cột tổng) + "Cả lớp vướng ở đâu" có ngưỡng tối thiểu (C3-04). | Ch3 §3 ý 5–6; K3 |
| S3-E4 | Tổng quan giáo viên web + điện thoại, theo dõi nhanh (Mở ngay, Đổi giờ), `Notification` trong ứng dụng (C3-05, C3-06). | Ch3 §5 luồng C |
| S3-E5 | An toàn tối thiểu (code trong task 28): `AuditEvent` (mở sớm, đổi giờ, đặt lại mật khẩu), thông báo riêng tư, danh sách trường dữ liệu được phép (test chặn trường mới), công tắc AI theo lớp. | Ch3 §7, §10; §0.3 ý 7 |
| S3-E6 | P1: chữa bài từ kết quả (sang Trình chiếu), xuất kết quả lớp, lưu trữ lớp cuối năm; nhắc việc trong ứng dụng. | Ch3 §4 P1 |

**PO-61 bỏ / đơn giản hóa:** `TeacherInvite` (Ch2 §8) → bỏ · C3-01 "đăng ký và xác nhận giáo viên" → đăng ký bằng tên chung cho hai vai, màn hiện mã người dùng + mã khôi phục · `TeacherVerification`, trạng thái "chờ xác nhận", luật "chưa xác nhận không giao bài / không xem lời giải Kho chung" (Ch3 §7, PO-18) → bỏ, thay bằng Q3 · `ConsentRecord` → cờ "đã đọc thông báo riêng tư" + phiên bản văn bản, không danh tính · trùng tên trong lớp → phân biệt bằng mã, không hỏi thêm ký hiệu · tư vấn pháp lý tài khoản trẻ em (Ch3 §9) → thu hẹp còn chính sách lưu/xóa dữ liệu bài làm (Q8) · PO-05, PO-18, PO-21 cần PO ghi "bị PO-61 thay một phần" (task 08).
**Ngoài phạm vi:** chặng 4–9 (rubric, nộp bài viết tay, phụ huynh, trường/tenant, liên trường, giáo trình riêng học sinh); dựng hình tự do; kiểm chứng chứng minh; bảng xếp hạng; nhiều giáo viên một lớp; đăng nhập mạng xã hội; thông báo đẩy ngoài ứng dụng; deploy prod (F1), push/merge (F5); gọi AI thật trong CI (F7, giữ mock D-GEO-047); sửa `Design/` cũ, `STAGES-AFTER-MVP.md`, `PRODUCT-MASTER.md`.
## 2. Cổng mở chặng và phụ thuộc
- **B7 (mở chặng 2):** T-261126-05 đo thật (owner đặt `GEO_AI_API_KEY`, NEEDS A1). Đề xuất: spec + design + plan (W1–W3, toàn sonnet/1 opus, không đụng code) chạy NGAY; code chặng 2 (W4+) chỉ chạy khi T-05 ĐẠT. T-05 trượt → task sửa MVP trước, W4 lùi.
- **Chặng 3 "giáo viên thật dùng lặp lại":** không kiểm được bằng máy. TẠM: code chặng 3 ngay sau L6 chặng 2; điều kiện này thành cổng **phát hành** L7 chặng 3 (gate 34) do owner chốt. Chặng 2 ghi sẵn chỉ số đo được (số đề giáo viên tự thêm, số phiên trình chiếu, số bài luyện giao) trong `tracking` để owner có số khi quyết.
- Phụ thuộc kỹ thuật: S2-E2 (khung môn + `grade`) trước S2-E5/E8; S2-E5 (duyệt) trước S2-E3/E6; S3-E2 (bộ bài) trước S3-E3/E4.
## 3. Task theo thứ tự chạy (dán thẳng vào `.claude/queue.md`; TASKS cùng ID)
Đầu dòng `##`: ID · epic · cổng + vai + skill · nhánh/worktree `.claude/worktrees/<tên>` · ‖ = chạy song song với · ước tính (giờ / token in gồm cache read, thô — hiệu chỉnh bằng `task_metrics.py` sau W4). Vai code dùng delivery-lead `templates/BRIEF.md` + `.claude/briefs/epic-common.md` (v1.2 do task 17 viết). Không ghi `Model` = sonnet. Ghi chú chung (chép vào queue): mọi task code: cổng riêng trong `app/.env.ports`, khoảng số migration do PLAN (task 17) cấp; AI giữ mock; tiến độ ghi `.claude/progress/<tên>.md`.
## T-261126-05 · GATE · Cổng B7: đo thật 4 chỉ số MVP (owner) [wait-user]
- Mục tiêu: MVP đạt B7 với Claude thật
- Nguồn: PRODUCT-MASTER B7; PO-60; NEEDS A1 (26/11)
- Xong khi: báo cáo đo thật ghi B7 đạt cả 4 chỉ số
- Kiểm: `grep -l 'Nhà cung cấp AI: .anthropic.' artifacts/GEO/evidence/MVP-METRICS-*/report.md | xargs grep -l 'Kết luận chỉ số bắt buộc: [*][*]ĐẠT' | grep -q .`
- Ngoài phạm vi: sửa code MVP
- Chờ: NEEDS §A1 (key) + owner chạy `make bench-mvp` với `GEO_AI_PROVIDER=anthropic`
## T-261126-08 · S23 · L1+L3 PO · po-ba-qc roles/po.md · wt s23-spec · ‖10 · 2h/3M · Brief + BRD delta + story map chặng 2–3 [todo]
- Mục tiêu: `brief-s23.md` 4 dòng (áp PO-61); BRD delta + story map S2-E1…E9, S3-E1…E6, R2 lock; ghi PO-62+ cho phần PO-05/18/21 bị PO-61 thay (TẠM)
- Nguồn: §1 plan này; STAGES Ch2, Ch3 (22/11); PO-DECISIONS PO-61 (26/11); DOMAIN-PROFILE.md
- Xong khi: brief đủ 4 dòng; story map có ≥ 1 story mỗi epic; epic_board có 15 dòng S2/S3
- Kiểm: `test "$(grep -cE '^[|] (Mục tiêu|Nguồn|Xong khi|Ngoài phạm vi) [|] [^<]' artifacts/GEO/work/s23/brief-s23.md)" -eq 4 && test "$(grep -cE '^[|] S[23]-E[0-9]' artifacts/GEO/work/epic_board.md)" -ge 15`
- Ngoài phạm vi: AC/TC (task 12–14), code, Design/
## T-261126-10 · S23 · L2 thiết kế UI · artifact-design + Design/prototype · wt s23-design · ‖08 · 4h/5M · Bản mẫu màn chặng 2–3 [todo]
- Mục tiêu: mở rộng `Design/prototype/` cho màn chưa có concept (C2-02/03/04/06/07/08, C3-01 rút gọn, C3-03/04/07) + chụp 16 màn C2/C3 + màn đăng nhập mã; danh sách câu hỏi design (DQ) có mặc định
- Nguồn: STAGES Ch2 §6, Ch3 §6 (22/11); §0.2 bảng thiết bị; Design/prototype/index.html; PO-61
- Xong khi: ≥ 17 ảnh trong `Design/s23/`, `Design/s23/DQ.md` có mặc định
- Kiểm: `test "$(ls Design/s23/*.png | wc -l)" -ge 17 && test -s Design/s23/DQ.md`
- Ngoài phạm vi: ảnh/màn MVP cũ, code app
## T-261126-12 · S2 · L3 BA+QC goal run · po-ba-qc roles/goal-run.md · wt s23-spec · ‖14 · 5h/7M · Spec S2 (E1–E9) [todo]
- Mục tiêu: stories_ac + test_design + test_cases (cột Tự động hóa) cho S2-E1…E9 (E4, E9 đánh dấu P1); "ready for R3"; dồn công 2 màn ưu tiên C2-03, C2-06 (K3)
- Nguồn: story map task 08; STAGES Ch2 §5–8 (22/11); Design/s23 (task 10); PO-27, PO-30, PO-45–48, PO-61
- Xong khi: lint stories + tests 0 lỗi mỗi epic
- Kiểm: `L=~/.claude/skills/synced/0891cb95-9cc4-4f0d-9b03-4f74eb7fe957_32199e3b-7799-4318-8c09-109bf13815a9/po-ba-qc/tools/lint; for e in S2-E1 S2-E2 S2-E3 S2-E4 S2-E5 S2-E6 S2-E7 S2-E8 S2-E9; do python3 $L/lint_stories.py artifacts/GEO/work/epics/$e/stories_ac.md && python3 $L/lint_tests.py artifacts/GEO/work/epics/$e/test_cases.md || exit 1; done`
- Ngoài phạm vi: chặng 3, story map (chỉ đề xuất đổi qua questions.md)
- Phụ thuộc: T-261126-08, T-261126-10
## T-261126-14 · S3 · L3 BA+QC goal run · po-ba-qc roles/goal-run.md · wt s23-spec · ‖12 · 3h/4M · Spec S3 (E1–E6) [todo]
- Mục tiêu: như task 12 cho S3-E1…E6; màn ưu tiên C3-04; ngưỡng gộp, quyền theo quan hệ, test chặn trường dữ liệu ngoài danh sách
- Nguồn: story map task 08; STAGES Ch3 (22/11); PO-61; Design/s23
- Xong khi: lint stories + tests 0 lỗi mỗi epic
- Kiểm: `L=~/.claude/skills/synced/0891cb95-9cc4-4f0d-9b03-4f74eb7fe957_32199e3b-7799-4318-8c09-109bf13815a9/po-ba-qc/tools/lint; for e in S3-E1 S3-E2 S3-E3 S3-E4 S3-E5 S3-E6; do python3 $L/lint_stories.py artifacts/GEO/work/epics/$e/stories_ac.md && python3 $L/lint_tests.py artifacts/GEO/work/epics/$e/test_cases.md || exit 1; done`
- Ngoài phạm vi: epic chặng 2
- Phụ thuộc: T-261126-08, T-261126-10
## T-261126-15 · S23 · L3 soát phản biện + L0 tester · po-ba-qc (adversarial RV) + tester SKILL · wt s23-spec · 2h/2.6M · RV + AC→TC + domain profile tester [todo]
- Mục tiêu: phản biện 15 epic (chỉ báo lỗi, BA/QC sửa gom trong cùng task); mở rộng `check_ac_tc.py` nhận S2-/S3- (giữ E1–E7); bản xem nhanh R3; tester `domains/geo/profile.md` + `scope.json` (chỉ localhost, `confirmed:false`)
- Nguồn: epics S2-*, S3-* (task 12, 14); tester SKILL v0.05 (25/11); delivery-profile §3–4 (26/11)
- Xong khi: AC→TC đủ, 0 lỗi; `review/RV_s23.md` 0 blocker mở; hai tệp tester có
- Kiểm: `python3 artifacts/GEO/work/tools/check_ac_tc.py S2-E1 S2-E2 S2-E3 S2-E4 S2-E5 S2-E6 S2-E7 S2-E8 S2-E9 S3-E1 S3-E2 S3-E3 S3-E4 S3-E5 S3-E6 && python3 artifacts/GEO/work/tools/check_ac_tc.py && grep -q localhost ~/.claude/skills/synced/24fb9ac6-fc76-408c-8209-fb31ed5866d5_c730a5c3-d83a-42d5-b500-bea3cf03a2a1/tester/domains/geo/scope.json`
- Ngoài phạm vi: code, quyết định sản phẩm (đưa vào NEEDS §C), staging/prod
- Phụ thuộc: T-261126-12, T-261126-14
## T-261126-16 · GATE · Owner họp R3 + quyết định §5 + duyệt scope tester [wait-user]
- Mục tiêu: owner OK spec chặng 2–3 (R3), chốt Q1–Q9, `scope.json` confirmed (họp một lần)
- Nguồn: bản xem nhanh R3 (task 15); NEEDS §C; §5 plan này
- Xong khi: DECISIONS có dòng "S23 R3 OK"
- Kiểm: `grep -q "S23 R3 OK" DECISIONS.md`
- Ngoài phạm vi: code
- Chờ: owner họp một lần (NEEDS §C)
- Phụ thuộc: T-261126-15
## T-261126-17 · S23 · L4 Dev kiến trúc · delivery-lead lifecycle L4 · wt s23-plan · 2.5h/5M · PLAN_code chặng 2–3 + epic-common v1.2 [todo]
- Mục tiêu: `PLAN_code-s23.md` (khung môn học, module ↔ story, khoảng số migration + cổng mỗi worktree, tệp sở hữu từng task để 2 agent không đụng nhau); epic-common v1.2 (nguồn S2/S3, lệnh kiểm); cập nhật khối queue 18–33 nếu đổi
- Nguồn: epics S2/S3 (sau R3); architecture/ (22/11); app/README.md; epic-common v1.1
- Xong khi: mỗi story có ≥ 1 task; queue hợp lệ
- Kiểm: `python3 ~/.claude/skills/delivery-lead/scripts/queue.py validate && test -s artifacts/GEO/work/s23/PLAN_code-s23.md`
- Ngoài phạm vi: code, spec
- Model: opus · Ngân sách: 2.5 giờ
- Phụ thuộc: T-261126-16
## T-261126-18 · S2-E1 · L5 Dev · epic-common v1.2 · wt s2-e1 · ‖19 · 3h/8M · Định danh tối giản PO-61 [todo]
- Mục tiêu: đăng ký bằng tên → mã người dùng, đăng nhập mã + mật khẩu, mã khôi phục giáo viên, khóa 10 lần/15', chuyển tài khoản mẫu; không trường dữ liệu cá nhân
- Nguồn: epics/S2-E1 (sau R3); PO-61, PO-53; module `iam`
- Xong khi: lệnh kiểm profile §2 xanh, e2e 2 lượt, truy vết 0 thiếu
- Kiểm: `cd .claude/worktrees/s2-e1/app && make verify && make web-build && make e2e && make e2e && make traceability ARGS=--all`
- Ngoài phạm vi: lớp/mã lớp (S3-E1), engine
- Model: opus · Ngân sách: 3.5 giờ
- Phụ thuộc: T-261126-17, T-261126-05
## T-261126-19 · S2-E2 · L5 Dev · epic-common v1.2 · wt s2-e2 · ‖18 · 5h/14M · Khung môn học + hình phẳng + bench-s2 [todo]
- Mục tiêu: interface môn (`grade`, `concepts`), `Problem.kind`, SceneSpec 2D dựng–kiểm, render 2D (C2-05), bộ đề chuẩn phẳng có câu trả lời mẫu, `make bench-s2` (≥ 95 % thỏa ràng buộc, ≥ 90 % đáp số, 0 sai mang nhãn)
- Nguồn: epics/S2-E2; STAGES §0.3 ý 1, Ch2 §8–10; module `geometry`
- Xong khi: như 18 + `make bench-s2` (mock) đạt ngưỡng
- Kiểm: `cd .claude/worktrees/s2-e2/app && make verify && make web-build && make e2e && make e2e && make traceability ARGS=--all && make bench-s2`
- Ngoài phạm vi: iam, kho đề, bài luyện
- Model: opus · Ngân sách: 5 giờ
- Phụ thuộc: T-261126-17, T-261126-05
## T-261126-20 · S2-E5 · L5 Dev · epic-common v1.2 · wt s2-e5 · ‖21 · 4h/10M · Duyệt lời giải có phiên bản + StepCheck [todo]
- Mục tiêu: C2-03, `review_status`, `StepCheck`, nhãn hai trục, hộp duyệt chung, chặn duyệt khi lệch đáp số, lời giải đã giao bất biến
- Nguồn: epics/S2-E5; STAGES Ch2 §7; §0.3 ý 2, 4
- Xong khi: như 18
- Kiểm: `cd .claude/worktrees/s2-e5/app && make verify && make web-build && make e2e && make e2e && make traceability ARGS=--all`
- Ngoài phạm vi: thêm đề (S2-E3), bài luyện
- Model: opus · Ngân sách: 4 giờ
- Phụ thuộc: T-261126-18, T-261126-19
## T-261126-21 · S2-E8 · L5 Dev · epic-common v1.2 · wt s2-e8 · ‖20 · 4h/10M · Bài luyện điền đáp số [todo]
- Mục tiêu: C2-06/07/08, bàn phím toán, `grade` dạng tương đương, `PracticeSet/Item`, `Attempt`, khóa đáp số tới giờ mở, nút "Em nghĩ đáp số này đúng"
- Nguồn: epics/S2-E8; STAGES Ch2 §5 luồng C, §7
- Xong khi: như 18 (gồm dự án Playwright mobile)
- Kiểm: `cd .claude/worktrees/s2-e8/app && make verify && make web-build && make e2e && make e2e && make traceability ARGS=--all`
- Ngoài phạm vi: duyệt lời giải, bộ bài S3
- Model: opus · Ngân sách: 4 giờ
- Phụ thuộc: T-261126-19
## T-261126-22 · S2-E3 · L5 Dev · epic-common v1.2 · wt s2-e3 · ‖23 · 4h/10M · Thêm đề có AI soạn nháp (chữ + ảnh) [todo]
- Mục tiêu: C2-02, pipeline đọc–dựng–giải–kiểm qua `aigateway` (mock + đáp mẫu), ngoại lệ ảnh mờ / không dựng / lệch đáp số, vào ngăn "Của tôi"
- Nguồn: epics/S2-E3; STAGES Ch2 §5 luồng A; PO-27, PO-30, PO-38
- Xong khi: như 18
- Kiểm: `cd .claude/worktrees/s2-e3/app && make verify && make web-build && make e2e && make e2e && make traceability ARGS=--all`
- Ngoài phạm vi: import file, chat chỉnh nháp (S2-E4)
- Model: opus · Ngân sách: 4 giờ
- Phụ thuộc: T-261126-20
## T-261126-23 · S2-E6+E7 · L5 Dev · epic-common v1.2 · wt s2-e67 · ‖22 · 3.5h/9M · Kho đề mở rộng + Trình chiếu [todo]
- Mục tiêu: C2-01 (Của tôi / Kho chung, vai người soát), C2-04 (toàn màn hình, từng bước, che, dùng được khi mất mạng)
- Nguồn: epics/S2-E6, S2-E7; STAGES Ch2 §5 luồng B, §7
- Xong khi: như 18
- Kiểm: `cd .claude/worktrees/s2-e67/app && make verify && make web-build && make e2e && make e2e && make traceability ARGS=--all`
- Ngoài phạm vi: thêm đề, bài luyện
- Model: opus · Ngân sách: 3.5 giờ
- Phụ thuộc: T-261126-20
## T-261126-24 · S2-E4 · L5 Dev (P1) · epic-common v1.2 · wt s2-e4 · ‖25 · 4h/10M · Chat chỉnh nháp + import file [todo]
- Mục tiêu: theo Q5: chat chỉnh bản nháp đề, import docx/pdf tách bài tập, đối chiếu trùng
- Nguồn: epics/S2-E4; PO-45–47
- Xong khi: như 18
- Kiểm: `cd .claude/worktrees/s2-e4/app && make verify && make web-build && make e2e && make e2e && make traceability ARGS=--all`
- Ngoài phạm vi: bài giảng với AI (chặng 5)
- Model: opus · Ngân sách: 4 giờ
- Phụ thuộc: T-261126-22
## T-261126-25 · S2-E9 · L5 Dev (P1) · epic-common v1.2 · wt s2-e9 · ‖24 · 4h/10M · Biến thể đề + xuất PDF + liên kết chỉ xem [todo]
- Mục tiêu: `ProblemVariant` engine sinh số liệu + tính lại đáp số; xuất ảnh/PDF; liên kết chỉ xem; trắc nghiệm trong bài luyện
- Nguồn: epics/S2-E9; STAGES Ch2 §3 ý 7, §4 P1
- Xong khi: như 18
- Kiểm: `cd .claude/worktrees/s2-e9/app && make verify && make web-build && make e2e && make e2e && make traceability ARGS=--all`
- Ngoài phạm vi: thiết diện động (trừ khi Q7 chọn)
- Model: opus · Ngân sách: 4 giờ
- Phụ thuộc: T-261126-21, T-261126-23
## T-261126-26 · S2 · L6 Tester · tester SKILL + Playwright · wt mvp-wave0 (chỉ đọc code) · 2.5h/3M · Kiểm thử chặng 2 [todo]
- Mục tiêu: e2e 2 lượt + TC Manual/khám phá qua API theo scope; bộ câu thử moi đáp án mở rộng (bài luyện, trình chiếu, giả làm giáo viên); bench-s2; báo cáo + bug draft
- Nguồn: epics/S2-*/test_cases.md; domains/geo/scope.json (confirmed)
- Xong khi: mọi TC có status; lệch audit = 0
- Kiểm: `cd app && make e2e && make e2e && make traceability ARGS=--all && make bench-s2 && test -s ../artifacts/GEO/evidence/S2-L6/REPORT.md`
- Ngoài phạm vi: sửa code (bug → inbox), staging/prod
- Phụ thuộc: T-261126-24, T-261126-25
## T-261126-27 · GATE · Owner L7 chặng 2 [wait-user]
- Mục tiêu: owner OK phát hành chặng 2 (release note, bug list)
- Nguồn: S2-L6/REPORT.md; checklist L7 do phiên chính soạn
- Xong khi: DECISIONS có "S2 L7 OK"
- Kiểm: `grep -q "S2 L7 OK" DECISIONS.md`
- Ngoài phạm vi: deploy do owner làm
- Chờ: owner duyệt + deploy
- Phụ thuộc: T-261126-26
## T-261126-28 · S3-E1+E5 · L5 Dev · epic-common v1.2 · wt s3-e1 · ‖29 · 4h/10M · Lớp, mã lớp, duyệt vào lớp, đặt lại mật khẩu + an toàn tối thiểu [todo]
- Mục tiêu: C3-02/07, `ClassMembership`, giáo viên đặt lại mật khẩu học sinh lớp mình (mã dùng một lần), `AuditEvent`, thông báo riêng tư, test danh sách trường dữ liệu, công tắc AI theo lớp
- Nguồn: epics/S3-E1, S3-E5; PO-61; STAGES Ch3 §7
- Xong khi: như 18
- Kiểm: `cd .claude/worktrees/s3-e1/app && make verify && make web-build && make e2e && make e2e && make traceability ARGS=--all`
- Ngoài phạm vi: bộ bài, kết quả lớp
- Model: opus · Ngân sách: 4 giờ
- Phụ thuộc: T-261126-26
## T-261126-29 · S3-E2 · L5 Dev · epic-common v1.2 · wt s3-e2 · ‖28 · 4h/10M · Bộ bài [todo]
- Mục tiêu: C3-03/08, `AssignmentSet/Item`, `due_at` là nhãn, `release_at` theo bộ hoặc từng bài, xem trước, cảnh báo đề cần xem lại; câu thử moi đáp án theo bộ
- Nguồn: epics/S3-E2; STAGES Ch3 §5 luồng B
- Xong khi: như 18
- Kiểm: `cd .claude/worktrees/s3-e2/app && make verify && make web-build && make e2e && make e2e && make traceability ARGS=--all`
- Ngoài phạm vi: ClassMembership (dùng Class sẵn có), kết quả lớp
- Model: opus · Ngân sách: 4 giờ
- Phụ thuộc: T-261126-26
## T-261126-30 · S3-E3 · L5 Dev · epic-common v1.2 · wt s3-e3 · ‖31 · 4h/10M · Kết quả lớp + Cả lớp vướng ở đâu [todo]
- Mục tiêu: C3-04, bảng lớp × bài (không cột tổng, không sắp hạng), ô bấm ra bằng chứng, nhận định gộp chỉ khi đủ ngưỡng (Q6)
- Nguồn: epics/S3-E3; STAGES Ch3 §5 luồng C; §0.3 ý 6
- Xong khi: như 18
- Kiểm: `cd .claude/worktrees/s3-e3/app && make verify && make web-build && make e2e && make e2e && make traceability ARGS=--all`
- Ngoài phạm vi: tổng quan giáo viên
- Model: opus · Ngân sách: 4 giờ
- Phụ thuộc: T-261126-28, T-261126-29
## T-261126-31 · S3-E4 · L5 Dev · epic-common v1.2 · wt s3-e4 · ‖30 · 3h/8M · Tổng quan giáo viên + theo dõi nhanh + thông báo trong ứng dụng [todo]
- Mục tiêu: C3-05/06 web + điện thoại, Mở ngay / Đổi giờ (ghi audit), `Notification` trong ứng dụng
- Nguồn: epics/S3-E4; STAGES Ch3 §5 luồng C
- Xong khi: như 18 (gồm Playwright mobile)
- Kiểm: `cd .claude/worktrees/s3-e4/app && make verify && make web-build && make e2e && make e2e && make traceability ARGS=--all`
- Ngoài phạm vi: thông báo đẩy ngoài ứng dụng
- Model: opus · Ngân sách: 3 giờ
- Phụ thuộc: T-261126-28, T-261126-29
## T-261126-32 · S3-E6 · L5 Dev (P1) · epic-common v1.2 · wt s3-e6 · 3h/8M · Chữa bài, xuất kết quả, lưu trữ lớp, nhắc việc [todo]
- Mục tiêu: theo P1 Ch3 §4
- Nguồn: epics/S3-E6
- Xong khi: như 18
- Kiểm: `cd .claude/worktrees/s3-e6/app && make verify && make web-build && make e2e && make e2e && make traceability ARGS=--all`
- Ngoài phạm vi: phụ huynh, điểm chính thức
- Model: opus · Ngân sách: 3 giờ
- Phụ thuộc: T-261126-30, T-261126-31
## T-261126-33 · S3 · L6 Tester · tester SKILL + Playwright · wt mvp-wave0 · 2.5h/3M · Kiểm thử chặng 3 [todo]
- Mục tiêu: như 26 cho chặng 3; ca quyền theo quan hệ (học sinh lớp khác, giáo viên khác), không lộ trường dữ liệu cá nhân
- Nguồn: epics/S3-*/test_cases.md; scope.json
- Xong khi: mọi TC có status; lệch audit = 0
- Kiểm: `cd app && make e2e && make e2e && make traceability ARGS=--all && test -s ../artifacts/GEO/evidence/S3-L6/REPORT.md`
- Ngoài phạm vi: sửa code
- Phụ thuộc: T-261126-32
## T-261126-34 · GATE · Owner L7 chặng 3 + cổng "giáo viên thật dùng lặp lại" [wait-user]
- Mục tiêu: owner quyết phát hành chặng 3 dựa trên số đo chặng 2
- Nguồn: S3-L6/REPORT.md; chỉ số tracking chặng 2
- Xong khi: DECISIONS có "S3 L7 OK"
- Kiểm: `grep -q "S3 L7 OK" DECISIONS.md`
- Ngoài phạm vi: deploy
- Chờ: owner quyết dựa trên số đo
- Phụ thuộc: T-261126-33
## 4. Đợt song song và điểm gộp
- W1 (ngay, không chờ B7): 08 ‖ 10 (2 Sonnet) · W2: 12 ‖ 14 → 15 (2 Sonnet; spec chặng 2 + 3 một khối, K3/PO-13) → gate 16 · W3: 17 (1 Opus).
- W4–W7 (chỉ khi 05 ĐẠT): 18‖19 · 20‖21 · 22‖23 · 24‖25 (2 Opus) · W8: 26 + soát cuối chặng 2 (2 Sonnet) → gate 27.
- W9–W11 (không chờ gate 27, TẠM §2): 28‖29 · 30‖31 (2 Opus) · 32 (1 Opus) · W12: 33 + soát cuối chặng 3 (2 Sonnet) → gate 34.
**Gộp sau MỖI đợt code (phiên chính, giao agent sonnet theo `small_task: delegate`):** merge nhánh vào `worktree-mvp-wave0` → `cd app && make verify && make web-build && make e2e && make e2e && make traceability ARGS=--all` → `/code-review` (sonnet, chỉ diff đợt + Xong khi) → 0 blocker/major; minor → inbox, task FIX (ID tiếp theo từ T-261126-35) chạy đầu đợt kế; commit message nêu ID. Đợt kế không mở khi gộp đỏ.
## 5. Quyết định cần owner (họp một lần ở gate 16; cột "Nghiêng" = TẠM đang dùng). Kèm NEEDS §C sẵn có: C2 (gói đề tự nạp ở prod — nghiêng lệnh nạp riêng), C3 (engine thiết diện — gắn Q7).
| # | Câu | Lựa chọn | Nghiêng |
|---|---|---|---|
| Q1 | Code chặng 2 chờ B7 đo thật? | chờ / code luôn | chờ (spec chạy trước) |
| Q2 | Cổng chặng 3 thành cổng phát hành? | code S3 ngay, owner chốt L7 / chờ số đo | code ngay |
| Q3 | Không xác minh giáo viên → ai cũng xem lời giải Kho chung (PO-18 rủi ro "giả làm GV") | chấp nhận / lời giải Kho chung chỉ hiện sau giờ mở như học sinh / chỉ GV có lớp ≥ N HS | chấp nhận + giới hạn tần suất tạo tài khoản, ghi rủi ro |
| Q4 | Đọc ảnh đề (OCR) | Claude vision qua aigateway / OCR riêng | Claude vision, ô chưa chắc tô để GV sửa |
| Q5 | S2-E4 (chat chỉnh nháp, import file) | chặng 2 P1 / dời chặng 5 | chặng 2 P1; bài giảng với AI dời chặng 5 |
| Q6 | Ngưỡng nhận định gộp; bài luyện có số tổng? | 3 / 5 / 10 bài; số / "đúng – cần xem lại" | 5 bài; không hiện số |
| Q7 | Thiết diện động (PO-25 P1) | thêm vào S2-E9 / để sau | để sau |
| Q8 | Dữ liệu bài làm gắn tên hiển thị: nơi lưu, thời gian giữ, xóa tài khoản | chính sách tối thiểu trước L7 chặng 3 / tư vấn pháp lý | chính sách tối thiểu + nút xóa tài khoản |
| Q9 | Ai soát Kho chung (D3-3); học sinh dùng web hay app (K2-3); học sinh tự học không lớp | owner tạm soát / người Toán; web; cho | owner tạm soát; web (PWA), chỉ thông báo trong ứng dụng; cho tự học |
## 6. Tổng ước tính (thô; token in gồm cache read như METRICS)
- 21 task agent + 4 cổng owner + gộp/soát 8 đợt. Opus: 14 task ≈ 52 giờ, ≈ 132M; Sonnet: 7 task + 8 soát ≈ 29 giờ, ≈ 40M; dự phòng FIX 15 % (Opus) → **≈ 89 agent·giờ, ≈ 190M token** (Opus ≈ 150M, Sonnet ≈ 40M). Thời gian thực ≈ 55–60 giờ chạy + chờ owner (≈ 7–9 ngày làm việc). Hạn mức: 2 Opus chạy liên tục 4–5 giờ mỗi đợt → **nguy cơ cao** chạm hạn mức 5 giờ/tuần. Giảm: một đợt một khối hạn mức; nếu sắp chạm thì chạy 1 Opus + soát Sonnet; dừng ở cuối đợt (đã gộp xanh) rồi ghi PROGRESS.
- Chia phiên: phiên điều phối A = chặng 2 phần tài liệu (W1–W3, tới gate 16); B = chặng 2 code + L6 (W4–W8); C = chặng 3 (W9–W12). Mỗi phiên mở bằng "đọc PROGRESS.md và làm tiếp", đóng bằng PROGRESS + `/clear`; quá 500k thì cắt giữa đợt đã gộp.
## 7. Rủi ro chính
- B7 trượt khi đo thật → chặng 2 lùi: W1–W3 không phụ thuộc B7; task sửa MVP chen trước W4.
- Hình phẳng đa dạng, engine phủ thiếu (S2-E2 lớn nhất): chốt danh sách dạng theo bộ đề chuẩn, ngoài danh sách đi "Cần xem lại"; quá 5 giờ → tách task.
- Hai agent song song đụng tệp chung (router web, nav, Makefile, migration): PLAN (17) cấp tệp sở hữu + khoảng migration + cổng; gộp từng đợt.
- Không xác minh giáo viên → lộ lời giải qua tài khoản giả: Q3; bộ câu thử "giả làm GV"; audit.
- Spec do Sonnet thiếu sâu ở C2-03, C2-06, C3-04: soát phản biện (15) + R3 owner + TC biên cho 3 màn. Đổi model/phiên giữa chừng tốn nạp lại: model theo vai từ đầu, phiên theo §6.
## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.2 | 2026-11-26 | Rà phiên chính: sửa lệnh kiểm T-05 (báo cáo in "Kết luận chỉ số bắt buộc: **ĐẠT**" + provider, không in "B7: ĐẠT"); nạp §3 vào queue | delivery-lead |
| v0.1 | 2026-11-26 | Bản nháp: 15 epic, 21 task agent + 4 cổng, áp PO-61 và chốt model theo vai của owner | agent plan-s23 |
