# Hàng đợi agent con · GEO
Phiên bản: v0.1 · 2026-11-26 · tạo từ delivery-lead/templates/queue.md. Định dạng: đầu tệp `<skill>/scripts/queue.py`.
<!-- Mỗi khối = 1 task trong .claude/TASKS.md, CÙNG ID. Trạng thái: todo | running | wait-user | done | failed | blocked | skip. -->
<!-- Dòng thường (không phải #, <!--, Phiên bản) đặt ở đây = ghi chú chung, được chèn vào brief của MỌI task. -->
<!-- Khối dưới là mẫu (skip, không được giao). Xoá hoặc sửa khi có task thật. -->

Ghi chú chung (task code): cổng riêng trong `app/.env.ports`, khoảng số migration do PLAN (T-261126-17) cấp; AI giữ mock; tiến độ ghi `.claude/progress/<tên>.md`.

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

## T-261126-07 · FIX5 · 3 minor soát T-04 [done]
- Mục tiêu: đóng inbox FIX5 #1–#3, mỗi mục có test
- Nguồn: .claude/inbox.md "FIX5" (26/11); brief .claude/briefs/T-261126-07-fix5.md
- Xong khi: verify xanh; bench-mvp mock exit 0
- Kiểm: `cd .claude/worktrees/fix5/app && make verify && make bench-mvp`
- Ngoài phạm vi: dữ liệu đề, e2e spec, worktree-mvp-wave0
- Model: sonnet · Ngân sách: 60 phút

## T-261126-05 · GATE · Cổng B7: đo thật 4 chỉ số MVP (owner) [wait-user]
- Mục tiêu: MVP đạt B7 với Claude thật
- Nguồn: PRODUCT-MASTER B7; PO-60; NEEDS A1 (26/11)
- Xong khi: báo cáo đo thật ghi B7 đạt cả 4 chỉ số
- Kiểm: `grep -l 'Nhà cung cấp AI: .anthropic.' artifacts/GEO/evidence/MVP-METRICS-*/report.md | xargs grep -l 'Kết luận chỉ số bắt buộc: [*][*]ĐẠT' | grep -q .`
- Ngoài phạm vi: sửa code MVP
- Chờ: NEEDS §A1 (key) + owner chạy `make bench-mvp` với `GEO_AI_PROVIDER=anthropic`
## T-261126-08 · S23 · L1+L3 PO · po-ba-qc roles/po.md · wt s23-spec · ‖10 · 2h/3M · Brief + BRD delta + story map chặng 2–3 [done]
- Mục tiêu: `brief-s23.md` 4 dòng (áp PO-61); BRD delta + story map S2-E1…E9, S3-E1…E6, R2 lock; ghi PO-62+ cho phần PO-05/18/21 bị PO-61 thay (TẠM)
- Nguồn: §1 plan này; STAGES Ch2, Ch3 (22/11); PO-DECISIONS PO-61 (26/11); DOMAIN-PROFILE.md
- Xong khi: brief đủ 4 dòng; story map có ≥ 1 story mỗi epic; epic_board có 15 dòng S2/S3
- Kiểm: `test "$(grep -cE '^[|] (Mục tiêu|Nguồn|Xong khi|Ngoài phạm vi) [|] [^<]' artifacts/GEO/work/s23/brief-s23.md)" -eq 4 && test "$(grep -cE '^[|] S[23]-E[0-9]' artifacts/GEO/work/epic_board.md)" -ge 15`
- Ngoài phạm vi: AC/TC (task 12–14), code, Design/
## T-261126-10 · S23 · L2 thiết kế UI · artifact-design + Design/prototype · wt s23-design · ‖08 · 4h/5M · Bản mẫu màn chặng 2–3 [done]
- Mục tiêu: mở rộng `Design/prototype/` cho màn chưa có concept (C2-02/03/04/06/07/08, C3-01 rút gọn, C3-03/04/07) + chụp 16 màn C2/C3 + màn đăng nhập mã; danh sách câu hỏi design (DQ) có mặc định
- Nguồn: STAGES Ch2 §6, Ch3 §6 (22/11); §0.2 bảng thiết bị; Design/prototype/index.html; PO-61
- Xong khi: ≥ 17 ảnh trong `Design/s23/`, `Design/s23/DQ.md` có mặc định
- Kiểm: `test "$(ls Design/s23/*.png | wc -l)" -ge 17 && test -s Design/s23/DQ.md`
- Ngoài phạm vi: ảnh/màn MVP cũ, code app
## T-261126-12 · S2 · L3 BA+QC goal run · po-ba-qc roles/goal-run.md · wt s23-spec · ‖14 · 5h/7M · Spec S2 (E1–E9) [done]
- Mục tiêu: stories_ac + test_design + test_cases (cột Tự động hóa) cho S2-E1…E9 (E4, E9 đánh dấu P1); "ready for R3"; dồn công 2 màn ưu tiên C2-03, C2-06 (K3)
- Nguồn: story map task 08; STAGES Ch2 §5–8 (22/11); Design/s23 (task 10); PO-27, PO-30, PO-45–48, PO-61
- Xong khi: lint stories + tests 0 lỗi mỗi epic
- Kiểm: `L=~/.claude/skills/synced/0891cb95-9cc4-4f0d-9b03-4f74eb7fe957_32199e3b-7799-4318-8c09-109bf13815a9/po-ba-qc/tools/lint; for e in S2-E1 S2-E2 S2-E3 S2-E4 S2-E5 S2-E6 S2-E7 S2-E8 S2-E9; do python3 $L/lint_stories.py artifacts/GEO/work/epics/$e/stories_ac.md && python3 $L/lint_tests.py artifacts/GEO/work/epics/$e/test_cases.md || exit 1; done`
- Ngoài phạm vi: chặng 3, story map (chỉ đề xuất đổi qua questions.md)
- Phụ thuộc: T-261126-08, T-261126-10
## T-261126-14 · S3 · L3 BA+QC goal run · po-ba-qc roles/goal-run.md · wt s23-spec · ‖12 · 3h/4M · Spec S3 (E1–E6) [done]
- Mục tiêu: như task 12 cho S3-E1…E6; màn ưu tiên C3-04; ngưỡng gộp, quyền theo quan hệ, test chặn trường dữ liệu ngoài danh sách
- Nguồn: story map task 08; STAGES Ch3 (22/11); PO-61; Design/s23
- Xong khi: lint stories + tests 0 lỗi mỗi epic
- Kiểm: `L=~/.claude/skills/synced/0891cb95-9cc4-4f0d-9b03-4f74eb7fe957_32199e3b-7799-4318-8c09-109bf13815a9/po-ba-qc/tools/lint; for e in S3-E1 S3-E2 S3-E3 S3-E4 S3-E5 S3-E6; do python3 $L/lint_stories.py artifacts/GEO/work/epics/$e/stories_ac.md && python3 $L/lint_tests.py artifacts/GEO/work/epics/$e/test_cases.md || exit 1; done`
- Ngoài phạm vi: epic chặng 2
- Phụ thuộc: T-261126-08, T-261126-10
## T-261126-15 · S23 · L3 soát phản biện + L0 tester · po-ba-qc (adversarial RV) + tester SKILL · wt s23-spec · 2h/2.6M · RV + AC→TC + domain profile tester [running]
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

## T-261126-36 · DESIGN · L2 Designer Dom · designer skill (persona Dom, giữ token GEO) · wt s23-dom · rà + hoàn thiện màn chặng 2–3 [running]
- Mục tiêu: REVIEW-Dom.md nhận xét cải thiện (sửa mức cao); trả lời Q_Dom.md (v0.3); vẽ 8 màn thiếu Q_Dom §C, DQ.md v0.2 (DQ-29+)
- Nguồn: MVP.md, PRODUCT-MASTER.md (22/11); brief/story-map s23, plan §1 §5, PO-61…65 (26/11); Design/s23 + Q_Dom v0.2 (26/11); lệnh owner 26/11
- Xong khi: ≥ 38 ảnh Design/s23, REVIEW-Dom.md, Q_Dom có "Trả lời Dom", DQ-29 có
- Kiểm: `test "$(ls Design/s23/*.png | wc -l)" -ge 38 && test -s Design/s23/REVIEW-Dom.md && grep -q "Trả lời Dom" Design/Q_Dom.md && grep -q "DQ-29" Design/s23/DQ.md`
- Ngoài phạm vi: app/, spec artifacts/, PO-DECISIONS, ảnh MVP + Design/stages*, vẽ chặng 4–9
- Model: sonnet · Ngân sách: 90 bước

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.2 | 2026-11-26 | Nạp 25 khối plan chặng 2–3 (T-05, T-08…T-34) | delivery-lead |
| v0.1 | 2026-11-26 | Tạo từ delivery-lead/templates/queue.md | |
