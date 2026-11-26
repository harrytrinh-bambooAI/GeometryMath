# Hàng đợi agent con · GEO
Phiên bản: v0.3 · 2026-11-26 · tạo từ delivery-lead/templates/queue.md. Định dạng: đầu tệp `<skill>/scripts/queue.py`.
<!-- Mỗi khối = 1 task trong .claude/TASKS.md, CÙNG ID. Trạng thái: todo | running | wait-user | done | failed | blocked | skip. -->
<!-- Dòng thường (không phải #, <!--, Phiên bản) đặt ở đây = ghi chú chung, được chèn vào brief của MỌI task. -->
<!-- Khối dưới là mẫu (skip, không được giao). Xoá hoặc sửa khi có task thật. -->

Ghi chú chung (task code): cổng riêng trong `app/.env.ports`, khoảng số migration do PLAN (T-261126-17, artifacts/GEO/work/s23/PLAN_code-s23.md §4) cấp; AI giữ mock; tiến độ ghi `.claude/progress/<tên>.md`.

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
## T-261126-15 · S23 · L3 soát phản biện + L0 tester · po-ba-qc (adversarial RV) + tester SKILL · wt s23-spec · 2h/2.6M · RV + AC→TC + domain profile tester [done]
- Mục tiêu: phản biện 15 epic (chỉ báo lỗi, BA/QC sửa gom trong cùng task); mở rộng `check_ac_tc.py` nhận S2-/S3- (giữ E1–E7); bản xem nhanh R3; tester `domains/geo/profile.md` + `scope.json` (chỉ localhost, `confirmed:false`)
- Nguồn: epics S2-*, S3-* (task 12, 14); tester SKILL v0.05 (25/11); delivery-profile §3–4 (26/11)
- Xong khi: AC→TC đủ, 0 lỗi; `review/RV_s23.md` 0 blocker mở; hai tệp tester có
- Kiểm: `python3 artifacts/GEO/work/tools/check_ac_tc.py S2-E1 S2-E2 S2-E3 S2-E4 S2-E5 S2-E6 S2-E7 S2-E8 S2-E9 S3-E1 S3-E2 S3-E3 S3-E4 S3-E5 S3-E6 && python3 artifacts/GEO/work/tools/check_ac_tc.py && grep -q localhost ~/.claude/skills/synced/24fb9ac6-fc76-408c-8209-fb31ed5866d5_c730a5c3-d83a-42d5-b500-bea3cf03a2a1/tester/domains/geo/scope.json`
- Ngoài phạm vi: code, quyết định sản phẩm (đưa vào NEEDS §C), staging/prod
- Phụ thuộc: T-261126-12, T-261126-14
## T-261126-16 · GATE · Owner họp R3 + quyết định §5 + duyệt scope tester [done]
- Mục tiêu: owner OK spec chặng 2–3 (R3), chốt Q1–Q9, `scope.json` confirmed (họp một lần)
- Nguồn: bản xem nhanh R3 (task 15); NEEDS §C; §5 plan này
- Xong khi: DECISIONS có dòng "S23 R3 OK"
- Kiểm: `grep -q "S23 R3 OK" DECISIONS.md`
- Ngoài phạm vi: code
- Phụ thuộc: T-261126-15
## T-261126-17 · S23 · L4 Dev kiến trúc · delivery-lead lifecycle L4 · wt s23-plan · 2.5h/5M · PLAN_code chặng 2–3 + epic-common v1.2 [done]
- Mục tiêu: `PLAN_code-s23.md` (khung môn học, module ↔ story, khoảng số migration + cổng mỗi worktree, tệp sở hữu từng task để 2 agent không đụng nhau); epic-common v1.2 (nguồn S2/S3, lệnh kiểm); cập nhật khối queue 18–33 nếu đổi
- Nguồn: epics S2/S3 (sau R3); architecture/ (22/11); app/README.md; epic-common v1.1
- Xong khi: mỗi story có ≥ 1 task; queue hợp lệ
- Kiểm: `python3 ~/.claude/skills/delivery-lead/scripts/queue.py validate && test -s artifacts/GEO/work/s23/PLAN_code-s23.md`
- Ngoài phạm vi: code, spec
- Model: opus · Ngân sách: 2.5 giờ
- Phụ thuộc: T-261126-37
## T-261126-41 · S23 · L5 Dev khung chung · epic-common v1.2 · wt s23-base · 1.5h/4M · Truy vết S2/S3 --strict + chỗ cắm feature + mốc tệp chung [done]
- Mục tiêu: theo PLAN_code-s23 §5 hàng 41: traceability đọc S2-*/S3-*, mã TC-S2En-/TC-S3En- (Playwright, JUnit, vitest, cả UT), cờ --strict (exit 1 khi thiếu/trượt), --epic danh sách + bí danh MVP, --defer US-…; fixtures.ts đọc thêm fixture-ids.d/*.json; MOBILE_P0 theo tên *.p0m.ui.spec.ts; 15 feature rỗng s2e1…s3e6; package-info cho subject, notify; mốc theo task trong 5 tệp chung; đích make bench-s2 gọi scripts/bench-s2.sh
- Nguồn: artifacts/GEO/work/s23/PLAN_code-s23.md v1.0 §4–§5 (26/11); app/e2e/scripts/traceability.ts, app/web/src/features (HEAD 3d154ab); epic-common v1.2
- Xong khi: verify + web-build + e2e 2 lượt xanh; traceability --strict --epic MVP exit 0 (nếu MVP đang thiếu thì ghi danh sách, MVP chỉ báo — PLAN R4); --strict --epic S2-E1 exit 1 (chưa có test) chứng minh cờ chạy
- Kiểm: `cd .claude/worktrees/s23-base/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP" && ! make traceability ARGS="--strict --epic S2-E1"`
- Ngoài phạm vi: code tính năng S2/S3, migration bảng nghiệp vụ, spec, Design/
- Model: opus · Ngân sách: 1.5 giờ
- Phụ thuộc: T-261126-17
- Phạm vi tệp: ^(app/e2e/(scripts|support)/|app/e2e/playwright\.config\.ts|app/web/src/features/|app/db/seed/fixture-ids\.d/|app/server/src/main/java/vn/geo/server/(subject|notify)/package-info\.java|app/server/src/main/resources/db/migration/V10[0-9]__|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/Makefile|app/README\.md|app/scripts/bench-s2\.sh)
- Kế hoạch: PLAN_code-s23 §3 W3b
## T-261126-18 · S2-E1 · L5 Dev · epic-common v1.3 · wt s2-e1 · ‖19 · 3h/8M · Định danh tối giản PO-61 [done]
- Mục tiêu: US-S2E1-01…04: đăng ký tên + vai → mã người dùng + mã khôi phục (GV và HS, G3), đăng nhập mã + mật khẩu, khóa 10 lần/15', tự khôi phục; văn bản riêng tư v1.0 + cờ đã đọc; chuyển tài khoản mẫu MVP; class_member.status (PLAN A6); giữ luồng đăng nhập MVP (A7); không trường dữ liệu cá nhân
- Nguồn: epics/S2-E1 (sau R3, ebec17a 26/11); PLAN_code-s23 v1.0 §1 A6–A7, §4–§6; PO-61, PO-53, PO-66 G3; module iam
- Xong khi: lệnh kiểm epic-common v1.2 xanh, e2e 2 lượt, truy vết --strict S2-E1 0 thiếu
- Kiểm: `cd .claude/worktrees/s2-e1/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2-E1" && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S2-E1`
- Ngoài phạm vi: lớp/mã lớp/duyệt (S3-E1), engine, tắt luồng đăng nhập MVP
- Model: opus · Ngân sách: 3.5 giờ
- Phụ thuộc: T-261126-41 (gộp sau T-50; trước khi kiểm cuối: git merge worktree-mvp-wave0)
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/iam/|app/server/src/test/java/vn/geo/server/(it/s2e1|iam)/|app/server/src/main/resources/db/migration/V1[12][0-9]__|app/web/src/(features/s2e1|shared/auth)/|app/e2e/tests/s2e1/|app/db/seed/(e2e/80[0-9]_|fixture-ids\.d/s2e1)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S2-E1/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s23 §3 W4
## T-261126-19 · S2-E2 · L5 Dev · epic-common v1.3 · wt s2-e2 · ‖18 · 5h/14M · Khung môn học + hình phẳng + bench-s2 [done]
- Mục tiêu: US-S2E2-01…05: module subject (SubjectEngine parse/solve/verify/render/hint/grade/concepts + registry, PLAN A1), Problem.kind (A2), SceneSpec 2D + SCENE-CONTRACT-2D.md + shared/scene2d (A3), màn làm bài 2D (C2-05) iPad/iPhone, bộ đề chuẩn phẳng có đáp mẫu, scripts/bench-s2.sh (≥ 95 % thỏa ràng buộc, ≥ 90 % đáp số, 0 sai mang nhãn)
- Nguồn: epics/S2-E2 (ebec17a 26/11); PLAN_code-s23 v1.0 §1 A1–A3; STAGES §0.3 ý 1, Ch2 §8–10; module geometry
- Xong khi: như 18 + make bench-s2 (mock) đạt ngưỡng + bench-mvp không lùi
- Kiểm: `cd .claude/worktrees/s2-e2/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2-E2" && make bench-s2 && make bench-mvp && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S2-E2`
- Ngoài phạm vi: iam, kho đề, bài luyện, grade dạng tương đương (21)
- Model: opus · Ngân sách: 5 giờ
- Phụ thuộc: T-261126-41 (gộp sau T-50; trước khi kiểm cuối: git merge worktree-mvp-wave0)
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/(subject|geometry|content)/|app/server/src/main/resources/(content/plane/|db/migration/V1[34][0-9]__)|app/server/src/test/java/vn/geo/server/(it/s2e2|subject|geometry|content)/|app/docs/SCENE-CONTRACT-2D\.md|app/web/src/(features/s2e2|shared/scene2d)/|app/web/package(-lock)?\.json|app/server/pom\.xml|app/e2e/tests/s2e2/|app/db/seed/(e2e/81[0-9]_|fixture-ids\.d/s2e2)|app/scripts/bench-s2\.sh|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S2-E2/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s23 §3 W4
## T-261126-20 · S2-E5 · L5 Dev · epic-common v1.3 · wt s2-e5 · ‖21 · 4h/10M · Duyệt lời giải có phiên bản + StepCheck [todo]
- Mục tiêu: US-S2E5-01…04: C2-03, sửa từng bước thành phiên bản mới, StepCheck (geometry/internal/stepcheck), nhãn hai trục + dòng "Máy đã kiểm tra phần tính toán", hộp duyệt chung (nhận/sửa/bỏ), chặn duyệt khi lệch đáp số. US-S2E5-05 dời sang 22 (PLAN §3)
- Nguồn: epics/S2-E5 (ebec17a 26/11); PLAN_code-s23 v1.0 §2, §5; STAGES Ch2 §7; §0.3 ý 2, 4
- Xong khi: như 18 (S2-E5 trừ US-S2E5-05) + bench-s2, bench-mvp không lùi
- Kiểm: `cd .claude/worktrees/s2-e5/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2-E1,S2-E2,S2-E5 --defer US-S2E5-05" && make bench-s2 && make bench-mvp && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S2-E5`
- Ngoài phạm vi: thêm đề (S2-E3), bài luyện, áp lời giải cho bài đang giao (22)
- Model: opus · Ngân sách: 4 giờ
- Phụ thuộc: T-261126-18, T-261126-19, T-261126-45
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/(geometry/internal/stepcheck|content)/|app/server/src/main/resources/db/migration/V1[67][0-9]__|app/server/src/test/java/vn/geo/server/(it/s2e5|geometry|content)/|app/web/src/features/s2e5/|app/e2e/tests/s2e5/|app/db/seed/(e2e/82[0-9]_|fixture-ids\.d/s2e5)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S2-E5/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s23 §3 W5
## T-261126-21 · S2-E8 · L5 Dev · epic-common v1.3 · wt s2-e8 · ‖20 · 5h/12M · Bài luyện điền đáp số + mô hình bộ bài chung [todo]
- Mục tiêu: US-S2E8-01…05: C2-06/07/08, bàn phím toán, grade dạng tương đương (subject/internal/grade), assignment_set/item đủ cột cho S3 + item giao sinh hàng assignment MVP (PLAN A4, PO-66 G1), practice_attempt (A5), khóa đáp số tới giờ mở, nút "Em nghĩ đáp số này đúng"; giao cho lớp mẫu 11A1
- Nguồn: epics/S2-E8 (ebec17a 26/11); PLAN_code-s23 v1.0 §1 A4–A5; STAGES Ch2 §5 luồng C, §7; PO-66 G1
- Xong khi: như 18 (gồm spec *.p0m.ui.spec.ts điện thoại) + bench-mvp không lùi
- Kiểm: `cd .claude/worktrees/s2-e8/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2-E1,S2-E2,S2-E8" && make bench-mvp && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S2-E8`
- Ngoài phạm vi: duyệt lời giải, vòng đời đóng/lưu trữ bộ (S3-E2), trắc nghiệm (25)
- Model: opus · Ngân sách: 5 giờ (quá thì tách US-S2E8-02 thành FIX, PLAN R2)
- Phụ thuộc: T-261126-18, T-261126-19, T-261126-45
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/(assignment|tracking|subject/internal/grade)/|app/server/src/main/resources/db/migration/V1[89][0-9]__|app/server/src/test/java/vn/geo/server/(it/s2e8|assignment|tracking|subject)/|app/web/src/features/s2e8/|app/web/package(-lock)?\.json|app/server/pom\.xml|app/e2e/tests/s2e8/|app/db/seed/(e2e/83[0-9]_|fixture-ids\.d/s2e8)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S2-E8/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s23 §3 W5
## T-261126-22 · S2-E3 · L5 Dev · epic-common v1.3 · wt s2-e3 · ‖23 · 4.5h/11M · Thêm đề có AI soạn nháp (chữ + ảnh) + US-S2E5-05 [todo]
- Mục tiêu: US-S2E3-01…04: C2-02, pipeline đọc–dựng–giải–kiểm qua aigateway (mock + đáp mẫu), ngoại lệ ảnh mờ / không dựng / lệch đáp số, vào ngăn "Của tôi"; US-S2E5-05: lời giải đã giao bất biến, sửa tạo phiên bản mới, chọn áp cho bài đang giao
- Nguồn: epics/S2-E3, S2-E5 (ebec17a 26/11); PLAN_code-s23 v1.0 §3, §5; STAGES Ch2 §5 luồng A; PO-27, PO-30, PO-38
- Xong khi: như 18 (S2-E3 + S2-E5 đủ) + bench-s2, bench-mvp không lùi
- Kiểm: `cd .claude/worktrees/s2-e3/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2-E1,S2-E2,S2-E5,S2-E8,S2-E3" && make bench-s2 && make bench-mvp && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S2-E3 S2-E5`
- Ngoài phạm vi: import file, chat chỉnh nháp (S2-E4), kho đề (23), gọi AI thật
- Model: opus · Ngân sách: 4.5 giờ
- Phụ thuộc: T-261126-20, T-261126-21
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/(content|aigateway/internal|assignment/.*ApplyRevision)|app/server/src/main/resources/(db/migration/V2[12][0-9]__|ai/)|app/server/src/test/java/vn/geo/server/(it/s2e3|it/s2e5|content|aigateway)/|app/web/src/features/(s2e3|s2e5)/|app/server/pom\.xml|app/e2e/tests/(s2e3|s2e5)/|app/db/seed/(e2e/84[0-9]_|fixture-ids\.d/s2e3)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/Makefile|app/README\.md|artifacts/GEO/work/epics/(S2-E3|S2-E5)/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s23 §3 W6
## T-261126-23 · S2-E6+E7 · L5 Dev · epic-common v1.3 · wt s2-e67 · ‖22 · 3.5h/9M · Kho đề mở rộng + Trình chiếu [todo]
- Mục tiêu: US-S2E6-01…03: C2-01 (Của tôi / Kho chung, vai người soát, lọc loại hình; lời giải Kho chung: GV xem ngay, HS tự học chỉ gợi ý — PO-66 G2; giới hạn tần suất thêm đề); US-S2E7-01…03: C2-04 (toàn màn hình, từng bước, che, chạy khi mất mạng, hỏi lại khi lời giải còn khóa)
- Nguồn: epics/S2-E6, S2-E7 (ebec17a 26/11); PLAN_code-s23 v1.0 §1 A10, §5; STAGES Ch2 §5 luồng B, §7; PO-66 G2
- Xong khi: như 18 + bench-mvp không lùi
- Kiểm: `cd .claude/worktrees/s2-e67/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2-E1,S2-E2,S2-E5,S2-E8,S2-E6,S2-E7 --defer US-S2E5-05" && make bench-mvp && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S2-E6 S2-E7`
- Ngoài phạm vi: thêm đề, bài luyện, content intake/draft/solution (22)
- Model: opus · Ngân sách: 3.5 giờ
- Phụ thuộc: T-261126-20, T-261126-21
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/(content/.*(library|shelf|Library|Shelf)|iam/.*Role)|app/server/src/main/resources/db/migration/V2[34][0-9]__|app/server/src/test/java/vn/geo/server/(it/s2e6|it/s2e7|content)/|app/web/src/features/(s2e6|s2e7)/|app/web/vite\.config\.ts|app/web/package(-lock)?\.json|app/e2e/tests/(s2e6|s2e7)/|app/db/seed/(e2e/85[0-9]_|fixture-ids\.d/s2e6)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/Makefile|app/README\.md|artifacts/GEO/work/epics/(S2-E6|S2-E7)/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s23 §3 W6
## T-261126-24 · S2-E4 · L5 Dev (P1) · epic-common v1.3 · wt s2-e4 · ‖25 · 4h/10M · Chat chỉnh nháp + import file [todo]
- Mục tiêu: US-S2E4-01…03: chat chỉnh bản nháp đề (AI hỏi lại, nói rõ trước/sau), import docx/pdf tách bài tập, đối chiếu trùng kho
- Nguồn: epics/S2-E4 (ebec17a 26/11); PLAN_code-s23 v1.0 §5 (chủ pom.xml đợt W7); PO-45–47
- Xong khi: như 18 + bench-mvp không lùi
- Kiểm: `cd .claude/worktrees/s2-e4/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2-E1,S2-E2,S2-E3,S2-E5,S2-E6,S2-E7,S2-E8,S2-E4" && make bench-mvp && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S2-E4`
- Ngoài phạm vi: bài giảng với AI (chặng 5), biến thể/xuất (25), gọi AI thật
- Model: opus · Ngân sách: 4 giờ
- Phụ thuộc: T-261126-22, T-261126-23
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/(content/.*(importer|authoring|dedup|Importer|Authoring|Dedup)|aigateway/internal)|app/server/src/main/resources/(db/migration/V2[67][0-9]__|ai/)|app/server/src/test/java/vn/geo/server/(it/s2e4|content|aigateway)/|app/web/src/features/s2e4/|app/server/pom\.xml|app/e2e/tests/s2e4/|app/db/seed/(e2e/86[0-9]_|fixture-ids\.d/s2e4)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S2-E4/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s23 §3 W7
## T-261126-25 · S2-E9 · L5 Dev (P1) · epic-common v1.3 · wt s2-e9 · ‖24 · 4h/10M · Biến thể đề + xuất PDF + liên kết chỉ xem [todo]
- Mục tiêu: US-S2E9-01…03: ProblemVariant (sinh số liệu + tính lại đáp số qua SubjectEngine), xuất ảnh/PDF (phía web, chủ package.json đợt W7), liên kết chỉ xem (share_grant), trắc nghiệm trong bài luyện
- Nguồn: epics/S2-E9 (ebec17a 26/11); PLAN_code-s23 v1.0 §5; STAGES Ch2 §3 ý 7, §4 P1
- Xong khi: như 18 + bench-s2, bench-mvp không lùi
- Kiểm: `cd .claude/worktrees/s2-e9/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2-E1,S2-E2,S2-E3,S2-E5,S2-E6,S2-E7,S2-E8,S2-E9" && make bench-s2 && make bench-mvp && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S2-E9`
- Ngoài phạm vi: thiết diện động (Q7 để sau), import (24)
- Model: opus · Ngân sách: 4 giờ
- Phụ thuộc: T-261126-22, T-261126-23
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/(content/.*(variant|export|Variant|Export)|geometry/internal/variant|assignment/.*Choice)|app/server/src/main/resources/db/migration/V2[89][0-9]__|app/server/src/test/java/vn/geo/server/(it/s2e9|content|geometry|assignment)/|app/web/src/features/(s2e9|s2e8)/|app/web/package(-lock)?\.json|app/e2e/tests/s2e9/|app/db/seed/(e2e/87[0-9]_|fixture-ids\.d/s2e9)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S2-E9/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s23 §3 W7
## T-261126-26 · S2 · L6 Tester · tester SKILL + Playwright · wt mvp-wave0 (chỉ đọc code) · 2.5h/3M · Kiểm thử chặng 2 [todo]
- Mục tiêu: e2e 2 lượt + TC Manual/khám phá qua API theo scope; bộ câu thử moi đáp án mở rộng (bài luyện, trình chiếu, giả làm giáo viên); bench-s2; báo cáo + bug draft
- Nguồn: epics/S2-*/test_cases.md (ebec17a 26/11); domains/geo/scope.json (confirmed); PLAN_code-s23 v1.0 §7
- Xong khi: mọi TC có status; lệch audit = 0
- Kiểm: `cd app && make e2e && make traceability ARGS="--strict --epic MVP,S2-E1,S2-E2,S2-E3,S2-E4,S2-E5,S2-E6,S2-E7,S2-E8,S2-E9" && make bench-s2 && test -s ../artifacts/GEO/evidence/S2-L6/REPORT.md`
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
## T-261126-42 · S3 · L5 Dev dữ liệu mẫu chung · epic-common v1.3 · wt s3-base · ‖26 · 1h/3M · Seed F-S3-001…053 [todo]
- Mục tiêu: nạp dữ liệu mẫu chung chặng 3 (F-S3-001…053: 2 giáo viên, 3 lớp, học sinh đủ trạng thái Đang học/Chờ duyệt/Đã rời, bộ tuần 15/16, bảng trạng thái 11A1) vào 910_s3_core.sql + fixture-ids.d/s3.json, chỉ dùng schema MVP + S2 (class_member.status từ 18, assignment_set/item từ 21)
- Nguồn: epics/S3-E1/stories_ac.md mục "Dữ liệu mẫu chung" (ebec17a 26/11); PLAN_code-s23 v1.0 §4; app/db/seed/README.md
- Xong khi: seed-reset nạp sạch trên geo_e2e; e2e MVP + S2 vẫn xanh; mỗi mã F-S3 trong bảng có UUID trong fixture-ids.d/s3.json
- Kiểm: `cd .claude/worktrees/s3-base/app && make verify && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2-E1,S2-E2,S2-E3,S2-E4,S2-E5,S2-E6,S2-E7,S2-E8,S2-E9" && test "$(grep -c F-S3- db/seed/fixture-ids.d/s3.json)" -ge 14`
- Ngoài phạm vi: code tính năng S3, migration nghiệp vụ (chỉ V310–V314 nếu thiếu cột, ghi lý do)
- Model: opus · Ngân sách: 1 giờ
- Phụ thuộc: T-261126-24, T-261126-25
- Phạm vi tệp: ^(app/db/seed/(e2e/910_|fixture-ids\.d/s3)|app/server/src/main/resources/db/migration/V31[0-4]__|app/README\.md)
- Kế hoạch: PLAN_code-s23 §3 W8
## T-261126-28 · S3-E1+E5 · L5 Dev · epic-common v1.3 · wt s3-e1 · ‖29 · 4h/10M · Lớp, mã lớp, duyệt vào lớp, đặt lại mật khẩu, riêng tư, công tắc AI [todo]
- Mục tiêu: US-S3E1-01…05: C3-02/07, nhiều lớp + năm học, mã lớp + trang in, vào lớp chờ duyệt (Bị từ chối), chống dò mã lớp 30 lần/24 giờ + duyệt từng em (G4), GV đặt lại mật khẩu HS lớp mình (mã dùng một lần, phát DomainEvent cho audit ở 31), quyền theo quan hệ xuyên suốt; US-S3E5-02 thông báo riêng tư theo phiên bản; US-S3E5-04 công tắc AI phía HS theo lớp
- Nguồn: epics/S3-E1, S3-E5 (ebec17a 26/11); PLAN_code-s23 v1.0 §3 (tách S3-E5), §5; PO-61, PO-66 G4–G5; STAGES Ch3 §7
- Xong khi: như 18 (S3-E1 đủ; S3-E5 trừ 01, 03, 05)
- Kiểm: `cd .claude/worktrees/s3-e1/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2-E1,S2-E2,S2-E3,S2-E4,S2-E5,S2-E6,S2-E7,S2-E8,S2-E9,S3-E1,S3-E5 --defer US-S3E5-01,US-S3E5-03,US-S3E5-05" && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S3-E1 S3-E5`
- Ngoài phạm vi: bộ bài, kết quả lớp, AuditEvent (31), xóa tài khoản và test danh sách trường (32)
- Model: opus · Ngân sách: 4 giờ
- Phụ thuộc: T-261126-26, T-261126-42
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/(iam|aigateway/.*ClassAiSwitch)|app/server/src/main/resources/db/migration/V3(1[5-9]|2[0-9]|3[0-4])__|app/server/src/test/java/vn/geo/server/(it/s3e1|it/s3e5|iam|aigateway)/|app/web/src/features/(s3e1|s3e5)/|app/server/pom\.xml|app/e2e/tests/(s3e1|s3e5)/|app/db/seed/(e2e/91[1-9]_|fixture-ids\.d/s3e1)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/Makefile|app/README\.md|artifacts/GEO/work/epics/(S3-E1|S3-E5)/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s23 §3 W9
## T-261126-29 · S3-E2 · L5 Dev · epic-common v1.3 · wt s3-e2 · ‖28 · 4h/10M · Bộ bài [todo]
- Mục tiêu: US-S3E2-01…04: C3-03/08 trên mô hình bộ bài của 21 (PLAN A4, không đổi schema nếu không buộc), due_at là nhãn, release_at theo bộ hoặc từng bài, xem trước, cảnh báo đề "Cần xem lại", vòng đời Đã đóng/Lưu trữ; câu thử moi đáp án theo bộ
- Nguồn: epics/S3-E2 (ebec17a 26/11); PLAN_code-s23 v1.0 §1 A4; STAGES Ch3 §5 luồng B; PO-66 G1, G5
- Xong khi: như 18
- Kiểm: `cd .claude/worktrees/s3-e2/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2-E1,S2-E2,S2-E3,S2-E4,S2-E5,S2-E6,S2-E7,S2-E8,S2-E9,S3-E2" && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S3-E2`
- Ngoài phạm vi: ClassMembership (28), kết quả lớp, Mở ngay/Đổi giờ từ tổng quan (31)
- Model: opus · Ngân sách: 4 giờ
- Phụ thuộc: T-261126-26, T-261126-42
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/assignment/|app/server/src/main/resources/db/migration/V3(3[5-9]|4[0-9]|5[0-4])__|app/server/src/test/java/vn/geo/server/(it/s3e2|assignment)/|app/web/src/features/s3e2/|app/web/package(-lock)?\.json|app/e2e/tests/s3e2/|app/db/seed/(e2e/92[0-9]_|fixture-ids\.d/s3e2)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S3-E2/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s23 §3 W9
## T-261126-30 · S3-E3 · L5 Dev · epic-common v1.3 · wt s3-e3 · ‖31 · 4h/10M · Kết quả lớp + Cả lớp vướng ở đâu [todo]
- Mục tiêu: US-S3E3-01…03: C3-04, bảng lớp × bài (không cột tổng, không sắp hạng), ô bấm ra bằng chứng, học sinh đã rời đánh dấu, nhận định gộp chỉ khi đủ ngưỡng 5 bài (Q6)
- Nguồn: epics/S3-E3 (ebec17a 26/11); PLAN_code-s23 v1.0 §2 (module tracking); STAGES Ch3 §5 luồng C; §0.3 ý 6
- Xong khi: như 18
- Kiểm: `cd .claude/worktrees/s3-e3/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2-E1,S2-E2,S2-E3,S2-E4,S2-E5,S2-E6,S2-E7,S2-E8,S2-E9,S3-E1,S3-E2,S3-E3 --defer US-S3E5-03" && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S3-E3`
- Ngoài phạm vi: tổng quan giáo viên, notify (31)
- Model: opus · Ngân sách: 4 giờ
- Phụ thuộc: T-261126-28, T-261126-29
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/tracking/|app/server/src/main/resources/db/migration/V3(6[5-9]|7[0-9]|8[0-4])__|app/server/src/test/java/vn/geo/server/(it/s3e3|tracking)/|app/web/src/features/s3e3/|app/server/pom\.xml|app/e2e/tests/s3e3/|app/db/seed/(e2e/93[0-9]_|fixture-ids\.d/s3e3)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S3-E3/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s23 §3 W10
## T-261126-31 · S3-E4+E5-01 · L5 Dev · epic-common v1.3 · wt s3-e4 · ‖30 · 3.5h/9M · Tổng quan giáo viên + theo dõi nhanh + thông báo + AuditEvent [todo]
- Mục tiêu: US-S3E4-01…03: C3-05/06 web + điện thoại, Mở ngay / Đổi giờ, Notification trong ứng dụng (module notify, PLAN A9); US-S3E5-01: AuditEvent trên audit_log (A8) cho mở sớm, đổi giờ, đặt lại mật khẩu (nghe DomainEvent của 28)
- Nguồn: epics/S3-E4, S3-E5 (ebec17a 26/11); PLAN_code-s23 v1.0 §1 A8–A9, §3; STAGES Ch3 §5 luồng C, §7
- Xong khi: như 18 (gồm spec *.p0m.ui.spec.ts điện thoại; S3-E5 trừ 03, 05)
- Kiểm: `cd .claude/worktrees/s3-e4/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2-E1,S2-E2,S2-E3,S2-E4,S2-E5,S2-E6,S2-E7,S2-E8,S2-E9,S3-E1,S3-E2,S3-E4,S3-E5 --defer US-S3E5-03,US-S3E5-05" && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S3-E4 S3-E5`
- Ngoài phạm vi: thông báo đẩy ngoài ứng dụng, kết quả lớp (30)
- Model: opus · Ngân sách: 3.5 giờ
- Phụ thuộc: T-261126-28, T-261126-29
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/(assignment|notify)/|app/server/src/main/resources/db/migration/V(38[5-9]|41[0-9]|42[0-4])__|app/server/src/test/java/vn/geo/server/(it/s3e4|it/s3e5|assignment|notify)/|app/web/src/features/(s3e4|s3e5)/|app/web/package(-lock)?\.json|app/e2e/tests/(s3e4|s3e5)/|app/db/seed/(e2e/94[0-9]_|fixture-ids\.d/s3e4)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/Makefile|app/README\.md|artifacts/GEO/work/epics/(S3-E4|S3-E5)/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s23 §3 W10
## T-261126-32 · S3-E6+E5 · L5 Dev (P0 trước, P1 sau) · epic-common v1.3 · wt s3-e6 · 4h/10M · Danh sách trường + xóa tài khoản (P0); chữa bài, xuất kết quả, lưu trữ lớp, nhắc việc (P1) [todo]
- Mục tiêu: TRƯỚC: US-S3E5-03 (danh sách trường được phép, test chặn trường mới quét mọi bảng/API S2–S3), US-S3E5-05 (xóa tài khoản + chính sách lưu/xóa); SAU: US-S3E6-01…03 (chữa bài sang Trình chiếu, xuất kết quả lớp, lưu trữ lớp cuối năm, nhắc việc trong ứng dụng)
- Nguồn: epics/S3-E5, S3-E6 (ebec17a 26/11); PLAN_code-s23 v1.0 §3; STAGES Ch3 §4 P1; PO-64, PO-66 G5
- Xong khi: như 18 (S3-E5 và S3-E6 đủ, không --defer)
- Kiểm: `cd .claude/worktrees/s3-e6/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2-E1,S2-E2,S2-E3,S2-E4,S2-E5,S2-E6,S2-E7,S2-E8,S2-E9,S3-E1,S3-E2,S3-E3,S3-E4,S3-E5,S3-E6" && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S3-E5 S3-E6`
- Ngoài phạm vi: phụ huynh, điểm chính thức
- Model: opus · Ngân sách: 4 giờ
- Phụ thuộc: T-261126-30, T-261126-31
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/(tracking|iam|notify)/|app/server/src/main/resources/db/migration/V4(1[5-9]|2[0-9]|3[0-9])__|app/server/src/test/java/vn/geo/server/(it/s3e5|it/s3e6|tracking|iam|notify|quality)/|app/web/src/features/(s3e5|s3e6)/|app/web/package(-lock)?\.json|app/server/pom\.xml|app/e2e/tests/(s3e5|s3e6)/|app/db/seed/(e2e/95[0-9]_|fixture-ids\.d/s3e6)|app/server/quality/sql-lint\.properties|app/server/src/main/resources/application\.yml|app/Makefile|app/README\.md|artifacts/GEO/work/epics/(S3-E5|S3-E6)/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s23 §3 W11
## T-261126-33 · S3 · L6 Tester · tester SKILL + Playwright · wt mvp-wave0 · 2.5h/3M · Kiểm thử chặng 3 [todo]
- Mục tiêu: như 26 cho chặng 3; ca quyền theo quan hệ (học sinh lớp khác, giáo viên khác), không lộ trường dữ liệu cá nhân
- Nguồn: epics/S3-*/test_cases.md (ebec17a 26/11); scope.json; PLAN_code-s23 v1.0 §7
- Xong khi: mọi TC có status; lệch audit = 0
- Kiểm: `cd app && make e2e && make traceability ARGS="--strict --epic MVP,S2-E1,S2-E2,S2-E3,S2-E4,S2-E5,S2-E6,S2-E7,S2-E8,S2-E9,S3-E1,S3-E2,S3-E3,S3-E4,S3-E5,S3-E6" && test -s ../artifacts/GEO/evidence/S3-L6/REPORT.md`
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

## T-261126-36 · DESIGN · L2 Designer Dom · designer skill (persona Dom, giữ token GEO) · wt s23-dom · rà + hoàn thiện màn chặng 2–3 [done]
- Mục tiêu: REVIEW-Dom.md nhận xét cải thiện (sửa mức cao); trả lời Q_Dom.md (v0.3); vẽ 8 màn thiếu Q_Dom §C, DQ.md v0.2 (DQ-29+)
- Nguồn: MVP.md, PRODUCT-MASTER.md (22/11); brief/story-map s23, plan §1 §5, PO-61…65 (26/11); Design/s23 + Q_Dom v0.2 (26/11); lệnh owner 26/11
- Xong khi: ≥ 38 ảnh Design/s23, REVIEW-Dom.md, Q_Dom có "Trả lời Dom", DQ-29 có
- Kiểm: `test "$(ls Design/s23/*.png | wc -l)" -ge 38 && test -s Design/s23/REVIEW-Dom.md && grep -q "Trả lời Dom" Design/Q_Dom.md && grep -q "DQ-29" Design/s23/DQ.md`
- Ngoài phạm vi: app/, spec artifacts/, PO-DECISIONS, ảnh MVP + Design/stages*, vẽ chặng 4–9
- Model: sonnet · Ngân sách: 90 bước

## T-261126-37 · S23 · L3 BA/QC áp quyết định gate R3 · po-ba-qc · wt s23-apply · 1.5h/2M · spec S2/S3 theo PO-66 [done]
- Mục tiêu: sửa epic S2-*/S3-* + story map theo PO-66 (G1 một mô hình bộ bài; G2 quyền lời giải Kho chung; G3 mã khôi phục học sinh tự học; G4 chống dò mã lớp; G5 thêm story: xóa tài khoản + lưu/xóa, Bị từ chối, quyền xuyên suốt S3, vòng đời bộ bài); bỏ TẠM các dòng đã chốt; đóng QA tương ứng; RV_s23 → v0.3 (CẦN OWNER → Đã chốt PO-66)
- Nguồn: `.claude/GATE-T16.md` v0.1, PO-DECISIONS PO-66 (26/11); RV_s23 v0.2, R3_s23_quickview (26/11); epics S2-*, S3-* (gộp 1816344)
- Xong khi: AC→TC đủ 0 lỗi; mỗi G1–G5 có ≥ 1 AC trỏ PO-66; không còn "CẦN OWNER" trong RV_s23
- Kiểm: `python3 artifacts/GEO/work/tools/check_ac_tc.py S2-E1 S2-E2 S2-E3 S2-E4 S2-E5 S2-E6 S2-E7 S2-E8 S2-E9 S3-E1 S3-E2 S3-E3 S3-E4 S3-E5 S3-E6 && python3 artifacts/GEO/work/tools/check_ac_tc.py && test "$(grep -rl 'PO-66' artifacts/GEO/work/epics | wc -l)" -ge 5 && ! grep -q 'CẦN OWNER' artifacts/GEO/work/review/RV_s23.md`
- Ngoài phạm vi: code app/, Design/, PO-DECISIONS (đã ghi), D1–D6 chặng 4–9, minor m-* không liên quan
- Model: sonnet · Ngân sách: 60 bước
- Phụ thuộc: T-261126-16

## T-261126-50 · S23 · L5 Dev hệ theme · epic-common v1.3 · wt s23-theme · ‖41 · 2h/5M · Theme D mặc định + đổi A/B/C/D trong Cài đặt [done]
- Mục tiêu: hệ token theme cho app/web (CSS custom properties theo `data-theme`), 4 theme từ `Design/styles/{a,b,c,d}/style.css` (màu, bo góc, font, mật độ, cao chạm, icon set nếu khác), D mặc định; màn/ô Cài đặt (giáo viên + học sinh) chọn theme, lưu theo tài khoản phía máy chủ (cột/khóa user preference, migration trong khoảng V109 (đã đổi từ V440); module máy chủ `settings/` riêng, không sửa `iam/` (thuộc task 18); cổng 54669 · 6719 · 8126 · 5206) và localStorage làm dự phòng; các component dùng chung (`app/web/src/shared/ui`) dùng token, không mã màu cứng; MVP E1–E7 không vỡ
- Nguồn: Design/styles/ (T-39, 26/11) + STYLES.md; Design/prototype/s23*.html (T-38/T-40, 26/11); app/web/src/styles/{tokens,base}.css; DECISIONS 26/11 (theme D)
- Xong khi: verify + web-build + e2e 2 lượt xanh; e2e mới: đổi theme trong Cài đặt → `html[data-theme]` đổi, tải lại vẫn giữ; ảnh chụp 1 màn MVP ở 4 theme lưu `artifacts/GEO/evidence/theme/`
- Kiểm: `cd .claude/worktrees/s23-theme/app && make verify && make web-build && make e2e-affected && ls ../artifacts/GEO/evidence/theme/*.png | wc -l | grep -qE '^ *[4-9]'`
- Ngoài phạm vi: tính năng S2/S3, đổi bố cục màn MVP ngoài token, Design/
- Phạm vi tệp: ^(app/web/src/(styles|shared/ui|shared/theme|app)/|app/web/src/features/settings/|app/web/index\.html|app/web/package\.json|app/server/src/main/java/vn/geo/server/settings/|app/server/src/main/resources/db/migration/V44[0-4]__|app/\.env\.ports|app/server/src/test/|app/e2e/tests/theme/|artifacts/GEO/evidence/theme/)
- Model: opus · Ngân sách: 2.5 giờ
- Phụ thuộc: T-261126-17

## T-261126-43 · FIX · L5 Dev ổn định môi trường + e2e chập chờn · epic-common v1.3 · wt fix-43 · ‖50 · 1.5h/4M · Redis dev-up, test chờ theo trạng thái, giới hạn worker [done]
- Mục tiêu: (a) `app/scripts/dev-up.sh` chờ Redis/PG nhận kết nối (ping lặp có hạn giờ) trước khi kiểm thư mục cổng; (b) làm chắc các test trượt khi máy tải cao: TC-GEO-0428 (`e4/hints.ui.spec.ts`), TC-GEO-0485 (`e4/inherit.ui.spec.ts`), TC-GEO-0102 (`e1/student-join.ui.spec.ts`), TC-GEO-0218 (`e2/labels.ui.spec.ts`), TC-GEO-0617 (`e6/assign.ui.spec.ts`) — chờ theo trạng thái/phản hồi API thay vì thời gian, không nới assertion; (c) `GEO_E2E_WORKERS` (mặc định = nửa số lõi, tối thiểu 2) trong playwright.config, ghi README; không bật retries để che lỗi
- Nguồn: inbox "FIX sau T-41" (26/11); log kiểm lại T-41 (tải máy 36–43, mỗi lượt trượt test khác nhau)
- Xong khi: verify + web-build xanh; `make up` từ sạch 3 lần đạt; e2e 2 lượt 0 trượt
- Kiểm: `cd .claude/worktrees/fix-43/app && make verify && make web-build && make down && make up && make down && make up && make down && make e2e && make e2e` (make e2e tự bật máy chủ: phải make down trước)
- Ngoài phạm vi: tính năng; app/web/src/app/ (T-50 đang sửa); ẩn feature rỗng (để FIX sau T-50)
- Phạm vi tệp: ^(app/scripts/(dev-up|dev-down|env|e2e)\.sh|app/e2e/tests/(e1|e2|e4|e6)/|app/e2e/playwright\.config\.ts|app/e2e/support/|app/README\.md|app/\.env\.ports)
- Model: opus · Ngân sách: 1.5 giờ · Cổng: FIX W4 54649 · 6699 · 8124 · 5204
- Phụ thuộc: T-261126-41

## T-261126-44 · FIX · L5 Dev theme hoàn thiện · epic-common v1.3 · wt fix-44 · 1.5h/3M · Màu cứng → token, chữ D ≥ 14 px, font toán ở theme C, sql-lint user_preference [todo]
- Mục tiêu: (1) đăng ký `user_preference` trong `app/server/quality/sql-lint.properties` (tenant.tables / owner.user_preference=settings, dùng khóa hậu tố dưới mốc task); (2) thay 26 màu cứng ở `app/web/src/features/{e2,e4,e5,e6}/*.css` + `shared/viewer/viewer.css` bằng token; chữ dùng `--pri-t` (không dùng `--pri` làm màu chữ) để đạt AA ở cả 4 theme; (3) theme D: chữ phụ ≥ 14 px (PO-66 G17); (4) theme C mặc định (Bad Script): mọi nội dung toán ở màn MVP E1–E7 (đề, đáp số, bàn phím toán, bảng số, nhãn hình 3D) dùng `--font-math`/`data-math`, kiểm bố cục không tràn với chữ tay; ảnh 4 theme chụp lại
- Nguồn: báo cáo T-50 (26/11) câu mở 1–3; `Design/styles/` (STYLES.md, contrast.js)
- Xong khi: verify + web-build + e2e 2 lượt xanh; `grep -rEc '#[0-9A-Fa-f]{3,8}' app/web/src/features/e{2,4,5,6} app/web/src/shared/viewer` = 0 (trừ comment); ảnh 4 theme mới trong `artifacts/GEO/evidence/theme/`
- Kiểm: `cd .claude/worktrees/fix-44/app && make verify && make web-build && make e2e-affected && ! grep -rEn '#[0-9A-Fa-f]{3,8}\b' web/src/features/e2 web/src/features/e4 web/src/features/e5 web/src/features/e6 web/src/shared/viewer --include=*.css`
- Ngoài phạm vi: tính năng; tệp của task 18/19 (iam/, features/s2e1, s2e2, geometry/)
- Phạm vi tệp: ^(app/web/src/(features/(e[1-7]|settings)|shared/(viewer|ui|theme)|styles)/|app/server/quality/sql-lint\.properties|app/e2e/tests/(theme|e[1-7])/|artifacts/GEO/evidence/theme/|app/\.env\.ports)
- Model: opus · Ngân sách: 1.5 giờ · Cổng: FIX W4 54649 · 6699 · 8124 · 5204 (sau khi T-43 gộp)
- Phụ thuộc: T-261126-50, T-261126-43

## T-261126-45 · FIX · L5 Dev regression theo mức ảnh hưởng · epic-common v1.5 · wt fix-45 · 1.5h/3M · make e2e-affected + smoke + reporter TC-S2/S3 [running]
- Mục tiêu: (1) `make e2e-affected` (script `app/scripts/e2e-affected.*`): diff so với `worktree-mvp-wave0` (BASE đổi được) → mức L0/L1/L2 theo bản đồ sở hữu PLAN_code-s23 §5 (tệp → module/epic → spec phụ thuộc; tệp không có trong bản đồ = L2) → chạy `scripts/e2e.sh` một lượt với đúng spec + project (L0/L1: chromium + api + spec `p0m` của màn bị đụng trên mobile; L2: trọn bộ chromium + api); `--dry-run` in mức, lý do, danh sách spec; ghi `impact.json` vào evidence của lượt; (2) bộ smoke ~30 test đường chính (đăng nhập, kho đề, giao bài, làm bài, kết quả) gắn tag `@smoke`, luôn chạy; (3) `e2e/reporters/evidence-reporter.ts` nhận tag `TC-S[23]E\d+-\d+`; (4) `privacy_notice` vào keep-tables.txt
- Nguồn: DECISIONS 26/11 (chiến lược regression), PLAN_code-s23 §5, inbox "FIX sau T-18"
- Xong khi: dry-run đúng 4 diff mẫu (chỉ features/s2e3 → L0; iam → L1 gồm E1 + S2-E1; styles/tokens.css → L2; tệp lạ → L2); smoke < 4 phút; traceability --strict S2-E1 hiện đạt (không "chưa chạy")
- Kiểm: `cd .claude/worktrees/fix-45/app && make verify && make web-build && make e2e-affected-selftest && make e2e ARGS="-g @smoke" && make traceability ARGS="--strict --epic MVP,S2-E1"`
- Ngoài phạm vi: tính năng; nội dung assertion test
- Phạm vi tệp: ^(app/scripts/(e2e-affected|e2e)[^/]*|app/e2e/(reporters|support|scripts)/|app/e2e/tests/|app/e2e/playwright\.config\.ts|app/Makefile|app/README\.md|app/db/keep-tables\.txt|app/\.env\.ports|artifacts/GEO/work/s23/impact-map\.json)
- Model: opus · Ngân sách: 1.5 giờ · Cổng: FIX W4 54649 · 6699 · 8124 · 5204
- Phụ thuộc: T-261126-41

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.3 | 2026-11-26 | T-17 (PLAN_code-s23 v1.0): thêm T-41 (khung chung), T-42 (dữ liệu mẫu S3); khối 18–33 thêm Kế hoạch + Phạm vi tệp, kiểm traceability --strict + check_ac_tc; dời US-S2E5-05 → 22, US-S3E5-01 → 31, US-S3E5-03/05 → 32; phụ thuộc 21 (+18), 22–25 theo đợt, 28–29 (+42) | runner-T-261126-17 |
| v0.2 | 2026-11-26 | Nạp 25 khối plan chặng 2–3 (T-05, T-08…T-34) | delivery-lead |
| v0.1 | 2026-11-26 | Tạo từ delivery-lead/templates/queue.md | |
