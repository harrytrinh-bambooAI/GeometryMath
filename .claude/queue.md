# Hàng đợi agent con · GEO
Phiên bản: v0.4 · 2026-11-28 · tạo từ delivery-lead/templates/queue.md. Định dạng: đầu tệp `<skill>/scripts/queue.py`.
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
- Phụ thuộc: T-261126-41 (gộp sau T-261126-50; trước khi kiểm cuối: git merge worktree-mvp-wave0)
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/iam/|app/server/src/test/java/vn/geo/server/(it/s2e1|iam)/|app/server/src/main/resources/db/migration/V1[12][0-9]__|app/web/src/(features/s2e1|shared/auth)/|app/e2e/tests/s2e1/|app/db/seed/(e2e/80[0-9]_|fixture-ids\.d/s2e1)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S2-E1/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s23 §3 W4
## T-261126-19 · S2-E2 · L5 Dev · epic-common v1.3 · wt s2-e2 · ‖18 · 5h/14M · Khung môn học + hình phẳng + bench-s2 [done]
- Mục tiêu: US-S2E2-01…05: module subject (SubjectEngine parse/solve/verify/render/hint/grade/concepts + registry, PLAN A1), Problem.kind (A2), SceneSpec 2D + SCENE-CONTRACT-2D.md + shared/scene2d (A3), màn làm bài 2D (C2-05) iPad/iPhone, bộ đề chuẩn phẳng có đáp mẫu, scripts/bench-s2.sh (≥ 95 % thỏa ràng buộc, ≥ 90 % đáp số, 0 sai mang nhãn)
- Nguồn: epics/S2-E2 (ebec17a 26/11); PLAN_code-s23 v1.0 §1 A1–A3; STAGES §0.3 ý 1, Ch2 §8–10; module geometry
- Xong khi: như 18 + make bench-s2 (mock) đạt ngưỡng + bench-mvp không lùi
- Kiểm: `cd .claude/worktrees/s2-e2/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2-E2" && make bench-s2 && make bench-mvp && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S2-E2`
- Ngoài phạm vi: iam, kho đề, bài luyện, grade dạng tương đương (21)
- Model: opus · Ngân sách: 5 giờ
- Phụ thuộc: T-261126-41 (gộp sau T-261126-50; trước khi kiểm cuối: git merge worktree-mvp-wave0)
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/(subject|geometry|content)/|app/server/src/main/resources/(content/plane/|db/migration/V1[34][0-9]__)|app/server/src/test/java/vn/geo/server/(it/s2e2|subject|geometry|content)/|app/docs/SCENE-CONTRACT-2D\.md|app/web/src/(features/s2e2|shared/scene2d)/|app/web/package(-lock)?\.json|app/server/pom\.xml|app/e2e/tests/s2e2/|app/db/seed/(e2e/81[0-9]_|fixture-ids\.d/s2e2)|app/scripts/bench-s2\.sh|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S2-E2/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s23 §3 W4
## T-261126-20 · S2-E5 · L5 Dev · epic-common v1.3 · wt s2-e5 · ‖21 · 4h/10M · Duyệt lời giải có phiên bản + StepCheck [done]
- Mục tiêu: US-S2E5-01…04: C2-03, sửa từng bước thành phiên bản mới, StepCheck (geometry/internal/stepcheck), nhãn hai trục + dòng "Máy đã kiểm tra phần tính toán", hộp duyệt chung (nhận/sửa/bỏ), chặn duyệt khi lệch đáp số. US-S2E5-05 dời sang 22 (PLAN §3)
- Nguồn: epics/S2-E5 (ebec17a 26/11); PLAN_code-s23 v1.0 §2, §5; STAGES Ch2 §7; §0.3 ý 2, 4
- Xong khi: như 18 (S2-E5 trừ US-S2E5-05) + bench-s2, bench-mvp không lùi
- Kiểm: `cd .claude/worktrees/s2-e5/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2-E1,S2-E2,S2-E5 --defer US-S2E5-05" && make bench-s2 && make bench-mvp && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S2-E5`
- Ngoài phạm vi: thêm đề (S2-E3), bài luyện, áp lời giải cho bài đang giao (22)
- Model: opus · Ngân sách: 4 giờ
- Phụ thuộc: T-261126-18, T-261126-19, T-261126-45
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/(geometry/internal/stepcheck|content)/|app/server/src/main/resources/db/migration/V1[67][0-9]__|app/server/src/test/java/vn/geo/server/(it/s2e5|geometry|content)/|app/web/src/features/s2e5/|app/e2e/tests/s2e5/|app/db/seed/(e2e/82[0-9]_|fixture-ids\.d/s2e5)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S2-E5/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s23 §3 W5
## T-261126-21 · S2-E8 · L5 Dev · epic-common v1.3 · wt s2-e8 · ‖20 · 5h/12M · Bài luyện điền đáp số + mô hình bộ bài chung [done]
- Mục tiêu: US-S2E8-01…05: C2-06/07/08, bàn phím toán, grade dạng tương đương (subject/internal/grade), assignment_set/item đủ cột cho S3 + item giao sinh hàng assignment MVP (PLAN A4, PO-66 G1), practice_attempt (A5), khóa đáp số tới giờ mở, nút "Em nghĩ đáp số này đúng"; giao cho lớp mẫu 11A1
- Nguồn: epics/S2-E8 (ebec17a 26/11); PLAN_code-s23 v1.0 §1 A4–A5; STAGES Ch2 §5 luồng C, §7; PO-66 G1
- Xong khi: như 18 (gồm spec *.p0m.ui.spec.ts điện thoại) + bench-mvp không lùi
- Kiểm: `cd .claude/worktrees/s2-e8/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2-E1,S2-E2,S2-E8" && make bench-mvp && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S2-E8`
- Ngoài phạm vi: duyệt lời giải, vòng đời đóng/lưu trữ bộ (S3-E2), trắc nghiệm (25)
- Model: opus · Ngân sách: 5 giờ (quá thì tách US-S2E8-02 thành FIX, PLAN R2)
- Phụ thuộc: T-261126-18, T-261126-19, T-261126-45
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/(assignment|tracking|subject/internal/grade)/|app/server/src/main/resources/db/migration/V1[89][0-9]__|app/server/src/test/java/vn/geo/server/(it/s2e8|assignment|tracking|subject)/|app/web/src/features/s2e8/|app/web/package(-lock)?\.json|app/server/pom\.xml|app/e2e/tests/s2e8/|app/db/seed/(e2e/83[0-9]_|fixture-ids\.d/s2e8)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S2-E8/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s23 §3 W5
## T-261126-22 · S2-E3 · L5 Dev · epic-common v1.3 · wt s2-e3 · ‖23 · 4.5h/11M · Thêm đề có AI soạn nháp (chữ + ảnh) + US-S2E5-05 [done]
- Mục tiêu: US-S2E3-01…04: C2-02, pipeline đọc–dựng–giải–kiểm qua aigateway (mock + đáp mẫu), ngoại lệ ảnh mờ / không dựng / lệch đáp số, vào ngăn "Của tôi"; US-S2E5-05: lời giải đã giao bất biến, sửa tạo phiên bản mới, chọn áp cho bài đang giao
- Nguồn: epics/S2-E3, S2-E5 (ebec17a 26/11); PLAN_code-s23 v1.0 §3, §5; STAGES Ch2 §5 luồng A; PO-27, PO-30, PO-38
- Xong khi: như 18 (S2-E3 + S2-E5 đủ) + bench-s2, bench-mvp không lùi
- Kiểm: `cd .claude/worktrees/s2-e3/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2-E1,S2-E2,S2-E5,S2-E8,S2-E3" && make bench-s2 && make bench-mvp && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S2-E3 S2-E5`
- Ngoài phạm vi: import file, chat chỉnh nháp (S2-E4), kho đề (23), gọi AI thật
- Model: opus · Ngân sách: 4.5 giờ
- Phụ thuộc: T-261126-20, T-261126-21
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/(content|aigateway/internal|assignment/.*ApplyRevision)|app/server/src/main/resources/(db/migration/V2[12][0-9]__|ai/)|app/server/src/test/java/vn/geo/server/(it/s2e3|it/s2e5|content|aigateway)/|app/web/src/features/(s2e3|s2e5)/|app/server/pom\.xml|app/e2e/tests/(s2e3|s2e5)/|app/db/seed/(e2e/84[0-9]_|fixture-ids\.d/s2e3)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/Makefile|app/README\.md|artifacts/GEO/work/epics/(S2-E3|S2-E5)/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s23 §3 W6
## T-261126-23 · S2-E6+E7 · L5 Dev · epic-common v1.3 · wt s2-e67 · ‖22 · 3.5h/9M · Kho đề mở rộng + Trình chiếu [done]
- Mục tiêu: US-S2E6-01…03: C2-01 (Của tôi / Kho chung, vai người soát, lọc loại hình; lời giải Kho chung: GV xem ngay, HS tự học chỉ gợi ý — PO-66 G2; giới hạn tần suất thêm đề); US-S2E7-01…03: C2-04 (toàn màn hình, từng bước, che, chạy khi mất mạng, hỏi lại khi lời giải còn khóa)
- Nguồn: epics/S2-E6, S2-E7 (ebec17a 26/11); PLAN_code-s23 v1.0 §1 A10, §5; STAGES Ch2 §5 luồng B, §7; PO-66 G2
- Xong khi: như 18 + bench-mvp không lùi
- Kiểm: `cd .claude/worktrees/s2-e67/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2-E1,S2-E2,S2-E5,S2-E8,S2-E6,S2-E7 --defer US-S2E5-05" && make bench-mvp && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S2-E6 S2-E7`
- Ngoài phạm vi: thêm đề, bài luyện, content intake/draft/solution (22)
- Model: opus · Ngân sách: 3.5 giờ
- Phụ thuộc: T-261126-20, T-261126-21
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/(content/.*(library|shelf|Library|Shelf)|iam/.*Role)|app/server/src/main/resources/db/migration/V2[34][0-9]__|app/server/src/test/java/vn/geo/server/(it/s2e6|it/s2e7|content)/|app/web/src/features/(s2e6|s2e7)/|app/web/vite\.config\.ts|app/web/package(-lock)?\.json|app/e2e/tests/(s2e6|s2e7)/|app/db/seed/(e2e/85[0-9]_|fixture-ids\.d/s2e6)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/Makefile|app/README\.md|artifacts/GEO/work/epics/(S2-E6|S2-E7)/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s23 §3 W6
## T-261126-24 · S2-E4 · L5 Dev (P1) · epic-common v1.3 · wt s2-e4 · ‖25 · 4h/10M · Chat chỉnh nháp + import file [done]
- Mục tiêu: US-S2E4-01…03: chat chỉnh bản nháp đề (AI hỏi lại, nói rõ trước/sau), import docx/pdf tách bài tập, đối chiếu trùng kho
- Nguồn: epics/S2-E4 (ebec17a 26/11); PLAN_code-s23 v1.0 §5 (chủ pom.xml đợt W7); PO-45–47
- Xong khi: như 18 + bench-mvp không lùi
- Kiểm: `cd .claude/worktrees/s2-e4/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2-E1,S2-E2,S2-E3,S2-E5,S2-E6,S2-E7,S2-E8,S2-E4" && make bench-mvp && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S2-E4`
- Ngoài phạm vi: bài giảng với AI (chặng 5), biến thể/xuất (25), gọi AI thật
- Model: opus · Ngân sách: 4 giờ
- Phụ thuộc: T-261126-22, T-261126-23
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/(content/.*(importer|authoring|dedup|Importer|Authoring|Dedup)|aigateway/internal)|app/server/src/main/resources/(db/migration/V2[67][0-9]__|ai/)|app/server/src/test/java/vn/geo/server/(it/s2e4|content|aigateway)/|app/web/src/features/s2e4/|app/server/pom\.xml|app/e2e/tests/s2e4/|app/db/seed/(e2e/86[0-9]_|fixture-ids\.d/s2e4)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S2-E4/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s23 §3 W7
## T-261126-25 · S2-E9 · L5 Dev (P1) · epic-common v1.3 · wt s2-e9 · ‖24 · 4h/10M · Biến thể đề + xuất PDF + liên kết chỉ xem [done]
- Mục tiêu: US-S2E9-01…03: ProblemVariant (sinh số liệu + tính lại đáp số qua SubjectEngine), xuất ảnh/PDF (phía web, chủ package.json đợt W7), liên kết chỉ xem (share_grant), trắc nghiệm trong bài luyện
- Nguồn: epics/S2-E9 (ebec17a 26/11); PLAN_code-s23 v1.0 §5; STAGES Ch2 §3 ý 7, §4 P1
- Xong khi: như 18 + bench-s2, bench-mvp không lùi
- Kiểm: `cd .claude/worktrees/s2-e9/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2-E1,S2-E2,S2-E3,S2-E5,S2-E6,S2-E7,S2-E8,S2-E9" && make bench-s2 && make bench-mvp && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S2-E9`
- Ngoài phạm vi: thiết diện động (Q7 để sau), import (24)
- Model: opus · Ngân sách: 4 giờ
- Phụ thuộc: T-261126-22, T-261126-23
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/(content/.*(variant|export|Variant|Export)|geometry/internal/variant|assignment/.*Choice)|app/server/src/main/resources/db/migration/V2[89][0-9]__|app/server/src/test/java/vn/geo/server/(it/s2e9|content|geometry|assignment)/|app/web/src/features/(s2e9|s2e8)/|app/web/package(-lock)?\.json|app/e2e/tests/s2e9/|app/db/seed/(e2e/87[0-9]_|fixture-ids\.d/s2e9)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S2-E9/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s23 §3 W7
## T-261126-26 · S2 · L6 Tester · tester SKILL + Playwright · wt mvp-wave0 (chỉ đọc code) · 2.5h/3M · Kiểm thử chặng 2 [skip]
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
## T-261126-42 · S3 · L5 Dev dữ liệu mẫu chung · epic-common v1.3 · wt s3-base · ‖26 · 1h/3M · Seed F-S3-001…053 [done]
- Mục tiêu: nạp dữ liệu mẫu chung chặng 3 (F-S3-001…053: 2 giáo viên, 3 lớp, học sinh đủ trạng thái Đang học/Chờ duyệt/Đã rời, bộ tuần 15/16, bảng trạng thái 11A1) vào 910_s3_core.sql + fixture-ids.d/s3.json, chỉ dùng schema MVP + S2 (class_member.status từ 18, assignment_set/item từ 21)
- Nguồn: epics/S3-E1/stories_ac.md mục "Dữ liệu mẫu chung" (ebec17a 26/11); PLAN_code-s23 v1.0 §4; app/db/seed/README.md
- Xong khi: seed-reset nạp sạch trên geo_e2e; e2e MVP + S2 vẫn xanh; mỗi mã F-S3 trong bảng có UUID trong fixture-ids.d/s3.json
- Kiểm: `cd .claude/worktrees/s3-base/app && make verify && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2-E1,S2-E2,S2-E3,S2-E4,S2-E5,S2-E6,S2-E7,S2-E8,S2-E9" && test "$(grep -c F-S3- db/seed/fixture-ids.d/s3.json)" -ge 14`
- Ngoài phạm vi: code tính năng S3, migration nghiệp vụ (chỉ V310–V314 nếu thiếu cột, ghi lý do)
- Model: opus · Ngân sách: 1 giờ
- Phụ thuộc: T-261126-24, T-261126-25
- Phạm vi tệp: ^(app/db/seed/(e2e/910_|fixture-ids\.d/s3)|app/server/src/main/resources/db/migration/V31[0-4]__|app/README\.md)
- Kế hoạch: PLAN_code-s23 §3 W8
## T-261126-28 · S3-E1+E5 · L5 Dev · epic-common v1.3 · wt s3-e1 · ‖29 · 4h/10M · Lớp, mã lớp, duyệt vào lớp, đặt lại mật khẩu, riêng tư, công tắc AI [done]
- Mục tiêu: US-S3E1-01…05: C3-02/07, nhiều lớp + năm học, mã lớp + trang in, vào lớp chờ duyệt (Bị từ chối), chống dò mã lớp 30 lần/24 giờ + duyệt từng em (G4), GV đặt lại mật khẩu HS lớp mình (mã dùng một lần, phát DomainEvent cho audit ở 31), quyền theo quan hệ xuyên suốt; US-S3E5-02 thông báo riêng tư theo phiên bản; US-S3E5-04 công tắc AI phía HS theo lớp
- Nguồn: epics/S3-E1, S3-E5 (ebec17a 26/11); PLAN_code-s23 v1.0 §3 (tách S3-E5), §5; PO-61, PO-66 G4–G5; STAGES Ch3 §7
- Xong khi: như 18 (S3-E1 đủ; S3-E5 trừ 01, 03, 05)
- Kiểm: `cd .claude/worktrees/s3-e1/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2-E1,S2-E2,S2-E3,S2-E4,S2-E5,S2-E6,S2-E7,S2-E8,S2-E9,S3-E1,S3-E5 --defer US-S3E5-01,US-S3E5-03,US-S3E5-05" && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S3-E1 S3-E5`
- Ngoài phạm vi: bộ bài, kết quả lớp, AuditEvent (31), xóa tài khoản và test danh sách trường (32)
- Model: opus · Ngân sách: 4 giờ
- Phụ thuộc: T-261126-26, T-261126-42
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/(iam|aigateway/.*ClassAiSwitch)|app/server/src/main/resources/db/migration/V3(1[5-9]|2[0-9]|3[0-4])__|app/server/src/test/java/vn/geo/server/(it/s3e1|it/s3e5|iam|aigateway)/|app/web/src/features/(s3e1|s3e5)/|app/server/pom\.xml|app/e2e/tests/(s3e1|s3e5)/|app/db/seed/(e2e/91[1-9]_|fixture-ids\.d/s3e1)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/Makefile|app/README\.md|artifacts/GEO/work/epics/(S3-E1|S3-E5)/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s23 §3 W9
## T-261126-29 · S3-E2 · L5 Dev · epic-common v1.3 · wt s3-e2 · ‖28 · 4h/10M · Bộ bài [done]
- Mục tiêu: US-S3E2-01…04: C3-03/08 trên mô hình bộ bài của 21 (PLAN A4, không đổi schema nếu không buộc), due_at là nhãn, release_at theo bộ hoặc từng bài, xem trước, cảnh báo đề "Cần xem lại", vòng đời Đã đóng/Lưu trữ; câu thử moi đáp án theo bộ
- Nguồn: epics/S3-E2 (ebec17a 26/11); PLAN_code-s23 v1.0 §1 A4; STAGES Ch3 §5 luồng B; PO-66 G1, G5
- Xong khi: như 18
- Kiểm: `cd .claude/worktrees/s3-e2/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2-E1,S2-E2,S2-E3,S2-E4,S2-E5,S2-E6,S2-E7,S2-E8,S2-E9,S3-E2" && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S3-E2`
- Ngoài phạm vi: ClassMembership (28), kết quả lớp, Mở ngay/Đổi giờ từ tổng quan (31)
- Model: opus · Ngân sách: 4 giờ
- Phụ thuộc: T-261126-26, T-261126-42
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/assignment/|app/server/src/main/resources/db/migration/V3(3[5-9]|4[0-9]|5[0-4])__|app/server/src/test/java/vn/geo/server/(it/s3e2|assignment)/|app/web/src/features/s3e2/|app/web/package(-lock)?\.json|app/e2e/tests/s3e2/|app/db/seed/(e2e/92[0-9]_|fixture-ids\.d/s3e2)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S3-E2/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s23 §3 W9
## T-261126-30 · S3-E3 · L5 Dev · epic-common v1.3 · wt s3-e3 · ‖31 · 4h/10M · Kết quả lớp + Cả lớp vướng ở đâu [done]
- Mục tiêu: US-S3E3-01…03: C3-04, bảng lớp × bài (không cột tổng, không sắp hạng), ô bấm ra bằng chứng, học sinh đã rời đánh dấu, nhận định gộp chỉ khi đủ ngưỡng 5 bài (Q6)
- Nguồn: epics/S3-E3 (ebec17a 26/11); PLAN_code-s23 v1.0 §2 (module tracking); STAGES Ch3 §5 luồng C; §0.3 ý 6
- Xong khi: như 18
- Kiểm: `cd .claude/worktrees/s3-e3/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2-E1,S2-E2,S2-E3,S2-E4,S2-E5,S2-E6,S2-E7,S2-E8,S2-E9,S3-E1,S3-E2,S3-E3 --defer US-S3E5-03" && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S3-E3`
- Ngoài phạm vi: tổng quan giáo viên, notify (31)
- Model: opus · Ngân sách: 4 giờ
- Phụ thuộc: T-261126-28, T-261126-29
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/tracking/|app/server/src/main/resources/db/migration/V3(6[5-9]|7[0-9]|8[0-4])__|app/server/src/test/java/vn/geo/server/(it/s3e3|tracking)/|app/web/src/features/s3e3/|app/server/pom\.xml|app/e2e/tests/s3e3/|app/db/seed/(e2e/93[0-9]_|fixture-ids\.d/s3e3)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S3-E3/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s23 §3 W10
## T-261126-31 · S3-E4+E5-01 · L5 Dev · epic-common v1.3 · wt s3-e4 · ‖30 · 3.5h/9M · Tổng quan giáo viên + theo dõi nhanh + thông báo + AuditEvent [done]
- Mục tiêu: US-S3E4-01…03: C3-05/06 web + điện thoại, Mở ngay / Đổi giờ, Notification trong ứng dụng (module notify, PLAN A9); US-S3E5-01: AuditEvent trên audit_log (A8) cho mở sớm, đổi giờ, đặt lại mật khẩu (nghe DomainEvent của 28)
- Nguồn: epics/S3-E4, S3-E5 (ebec17a 26/11); PLAN_code-s23 v1.0 §1 A8–A9, §3; STAGES Ch3 §5 luồng C, §7
- Xong khi: như 18 (gồm spec *.p0m.ui.spec.ts điện thoại; S3-E5 trừ 03, 05)
- Kiểm: `cd .claude/worktrees/s3-e4/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2-E1,S2-E2,S2-E3,S2-E4,S2-E5,S2-E6,S2-E7,S2-E8,S2-E9,S3-E1,S3-E2,S3-E4,S3-E5 --defer US-S3E5-03,US-S3E5-05" && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S3-E4 S3-E5`
- Ngoài phạm vi: thông báo đẩy ngoài ứng dụng, kết quả lớp (30)
- Model: opus · Ngân sách: 3.5 giờ
- Phụ thuộc: T-261126-28, T-261126-29
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/(assignment|notify)/|app/server/src/main/resources/db/migration/V(38[5-9]|41[0-9]|42[0-4])__|app/server/src/test/java/vn/geo/server/(it/s3e4|it/s3e5|assignment|notify)/|app/web/src/features/(s3e4|s3e5)/|app/web/package(-lock)?\.json|app/e2e/tests/(s3e4|s3e5)/|app/db/seed/(e2e/94[0-9]_|fixture-ids\.d/s3e4)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/Makefile|app/README\.md|artifacts/GEO/work/epics/(S3-E4|S3-E5)/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s23 §3 W10
## T-261126-32 · S3-E6+E5 · L5 Dev (P0 trước, P1 sau) · epic-common v1.3 · wt s3-e6 · 4h/10M · Danh sách trường + xóa tài khoản (P0); chữa bài, xuất kết quả, lưu trữ lớp, nhắc việc (P1) [done]
- Mục tiêu: TRƯỚC: US-S3E5-03 (danh sách trường được phép, test chặn trường mới quét mọi bảng/API S2–S3), US-S3E5-05 (xóa tài khoản + chính sách lưu/xóa); SAU: US-S3E6-01…03 (chữa bài sang Trình chiếu, xuất kết quả lớp, lưu trữ lớp cuối năm, nhắc việc trong ứng dụng)
- Nguồn: epics/S3-E5, S3-E6 (ebec17a 26/11); PLAN_code-s23 v1.0 §3; STAGES Ch3 §4 P1; PO-64, PO-66 G5
- Xong khi: như 18 (S3-E5 và S3-E6 đủ, không --defer)
- Kiểm: `cd .claude/worktrees/s3-e6/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2-E1,S2-E2,S2-E3,S2-E4,S2-E5,S2-E6,S2-E7,S2-E8,S2-E9,S3-E1,S3-E2,S3-E3,S3-E4,S3-E5,S3-E6" && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S3-E5 S3-E6`
- Ngoài phạm vi: phụ huynh, điểm chính thức
- Model: opus · Ngân sách: 4 giờ
- Phụ thuộc: T-261126-30, T-261126-31
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/(tracking|iam|notify)/|app/server/src/main/resources/db/migration/V4(1[5-9]|2[0-9]|3[0-9])__|app/server/src/test/java/vn/geo/server/(it/s3e5|it/s3e6|tracking|iam|notify|quality)/|app/web/src/features/(s3e5|s3e6)/|app/web/package(-lock)?\.json|app/server/pom\.xml|app/e2e/tests/(s3e5|s3e6)/|app/db/seed/(e2e/95[0-9]_|fixture-ids\.d/s3e6)|app/server/quality/sql-lint\.properties|app/server/src/main/resources/application\.yml|app/Makefile|app/README\.md|artifacts/GEO/work/epics/(S3-E5|S3-E6)/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s23 §3 W11
## T-261126-33 · S3 · L6 Tester · tester SKILL + Playwright · wt mvp-wave0 · 2.5h/3M · Kiểm thử chặng 3 [done]
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

## T-261126-44 · FIX · L5 Dev theme hoàn thiện · epic-common v1.3 · wt fix-44 · 1.5h/3M · Màu cứng → token, chữ D ≥ 14 px, font toán ở theme C, sql-lint user_preference [done]
- Mục tiêu: (1) đăng ký `user_preference` trong `app/server/quality/sql-lint.properties` (tenant.tables / owner.user_preference=settings, dùng khóa hậu tố dưới mốc task); (2) thay 26 màu cứng ở `app/web/src/features/{e2,e4,e5,e6}/*.css` + `shared/viewer/viewer.css` bằng token; chữ dùng `--pri-t` (không dùng `--pri` làm màu chữ) để đạt AA ở cả 4 theme; (3) theme D: chữ phụ ≥ 14 px (PO-66 G17); (4) theme C mặc định (Bad Script): mọi nội dung toán ở màn MVP E1–E7 (đề, đáp số, bàn phím toán, bảng số, nhãn hình 3D) dùng `--font-math`/`data-math`, kiểm bố cục không tràn với chữ tay; ảnh 4 theme chụp lại
- Nguồn: báo cáo T-50 (26/11) câu mở 1–3; `Design/styles/` (STYLES.md, contrast.js)
- Xong khi: verify + web-build + e2e 2 lượt xanh; `grep -rEc '#[0-9A-Fa-f]{3,8}' app/web/src/features/e{2,4,5,6} app/web/src/shared/viewer` = 0 (trừ comment); ảnh 4 theme mới trong `artifacts/GEO/evidence/theme/`
- Kiểm: `cd .claude/worktrees/fix-44/app && make verify && make web-build && make e2e-affected && ! grep -rEn '#[0-9A-Fa-f]{3,8}\b' web/src/features/e2 web/src/features/e4 web/src/features/e5 web/src/features/e6 web/src/shared/viewer --include=*.css`
- Ngoài phạm vi: tính năng; tệp của task 18/19 (iam/, features/s2e1, s2e2, geometry/)
- Phạm vi tệp: ^(app/web/src/(features/(e[1-7]|settings)|shared/(viewer|ui|theme)|styles)/|app/server/quality/sql-lint\.properties|app/e2e/tests/(theme|e[1-7])/|artifacts/GEO/evidence/theme/|app/\.env\.ports)
- Model: opus · Ngân sách: 1.5 giờ · Cổng: FIX W4 54649 · 6699 · 8124 · 5204 (sau khi T-43 gộp)
- Phụ thuộc: T-261126-50, T-261126-43

## T-261126-45 · FIX · L5 Dev regression theo mức ảnh hưởng · epic-common v1.5 · wt fix-45 · 1.5h/3M · make e2e-affected + smoke + reporter TC-S2/S3 [done]
- Mục tiêu: (1) `make e2e-affected` (script `app/scripts/e2e-affected.*`): diff so với `worktree-mvp-wave0` (BASE đổi được) → mức L0/L1/L2 theo bản đồ sở hữu PLAN_code-s23 §5 (tệp → module/epic → spec phụ thuộc; tệp không có trong bản đồ = L2) → chạy `scripts/e2e.sh` một lượt với đúng spec + project (L0/L1: chromium + api + spec `p0m` của màn bị đụng trên mobile; L2: trọn bộ chromium + api); `--dry-run` in mức, lý do, danh sách spec; ghi `impact.json` vào evidence của lượt; (2) bộ smoke ~30 test đường chính (đăng nhập, kho đề, giao bài, làm bài, kết quả) gắn tag `@smoke`, luôn chạy; (3) `e2e/reporters/evidence-reporter.ts` nhận tag `TC-S[23]E\d+-\d+`; (4) `privacy_notice` vào keep-tables.txt
- Nguồn: DECISIONS 26/11 (chiến lược regression), PLAN_code-s23 §5, inbox "FIX sau T-18"
- Xong khi: dry-run đúng 4 diff mẫu (chỉ features/s2e3 → L0; iam → L1 gồm E1 + S2-E1; styles/tokens.css → L2; tệp lạ → L2); smoke < 4 phút; traceability --strict S2-E1 hiện đạt (không "chưa chạy")
- Kiểm: `cd .claude/worktrees/fix-45/app && make verify && make web-build && make e2e-affected-selftest && make e2e ARGS="-g @smoke" && make traceability ARGS="--strict --epic MVP,S2-E1"`
- Ngoài phạm vi: tính năng; nội dung assertion test
- Phạm vi tệp: ^(app/scripts/(e2e-affected|e2e)[^/]*|app/e2e/(reporters|support|scripts)/|app/e2e/tests/|app/e2e/playwright\.config\.ts|app/Makefile|app/README\.md|app/db/keep-tables\.txt|app/\.env\.ports|artifacts/GEO/work/s23/impact-map\.json)
- Model: opus · Ngân sách: 1.5 giờ · Cổng: FIX W4 54649 · 6699 · 8124 · 5204
- Phụ thuộc: T-261126-41

## T-261128-51 · S5 · L5 Dev khung chung + dữ liệu mẫu · epic-common v1.6 · wt s5-base · 1.5h/4M · Truy vết S5 --strict, feature rỗng s5e1…s5e6, mốc tệp chung, ai_purpose, cờ, seed F-S5-*, BUG-S23-001 [done]
- Mục tiêu: (1) `app/e2e/scripts/traceability.ts` nhận thư mục `S5-E\d+`, mã `TC-S5En-nn`, bí danh `S5`; `evidence-reporter.ts` nhận tag `TC-S5E` và **sửa BUG-S23-001** (dòng 227–229: khóa đếm `perTest`/`last` thêm `title` để test vòng lặp cùng dòng không bị gộp; 283 → 285 ở bộ S23); `check_ac_tc.py` nhận `S5-E*`/`US-S5E`; `e2e-affected.mjs` + `s23/impact-map.json` có mục S5 (module mới → feature); (2) 6 feature rỗng `app/web/src/features/s5e1…s5e6/index.tsx` cắm vào `index.ts`/`types.ts`; `package-info.java` cho `lessonplan`, `exam`, `assessment`, `aigrade`; (3) mốc `T-261128-52…65` trong 5 tệp chung S23 + `AnthropicPrompts.java` + `MockAiProvider.java`; (4) V500–V504: 8 giá trị `ai_purpose` (`lesson_plan`, `lesson_patch`, `class_notice`, `kb_qa`, `grade_ocr`, `grade`, `exam_draft`, `class_errors`) bằng `ADD VALUE IF NOT EXISTS`; (5) cờ `geo.s5.grading.enabled`, `geo.s5.ai-grading.enabled` mặc định false, bật ở hồ sơ test/e2e; (6) `fixture-ids.d/s5.json` đủ 27 mã F-S5 (PLAN P8) + `s5_000_core.sql` nạp phần dùng schema sẵn có (F-S5-001, 002, 010, 011, 050); kiểm `s5_*` chạy sau `930_*`
- Nguồn: epics/S5-E1/stories_ac.md §"Dữ liệu mẫu chung của chặng 5" (80e9b6f 28/11); PLAN_code-s5 v1.0 §1 P4–P8, §4, §5; inbox BUG-S23-001 + artifacts/GEO/evidence/E2E-S23-20261128/BUG-LIST.md (28/11); PLAN_code-s23 §5 (mẫu khung T-41)
- Xong khi: verify + web-build + e2e-affected xanh; traceability --strict MVP,S2,S3 vẫn đạt; traceability --epic S5 liệt kê đủ 219 TC (chưa chạy, không --strict); check_ac_tc S5-E1…E6 0 lỗi; s5.json có 27 mã; báo cáo e2e S23 đếm đúng test vòng lặp
- Kiểm: `cd .claude/worktrees/s5-base/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2,S3" && make traceability ARGS="--epic S5" && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S5-E1 S5-E2 S5-E3 S5-E4 S5-E5 S5-E6 && python3 ../artifacts/GEO/work/s5/check_s5.py && test "$(grep -o 'F-S5-' db/seed/fixture-ids.d/s5.json | wc -l)" -ge 27`
- Ngoài phạm vi: code tính năng S5; bảng nghiệp vụ mới (do task chủ); sửa đặc tả S5; assertion test S2/S3
- Model: opus · Ngân sách: 1.5 giờ · Cổng: 54709 · 6739 · 8200 · 5280
- Phụ thuộc: T-261126-33
- Phạm vi tệp: ^(app/e2e/(scripts|reporters|support)/|app/scripts/e2e-affected[^/]*|artifacts/GEO/work/s23/impact-map\.json|artifacts/GEO/work/tools/check_ac_tc\.py|app/web/src/features/(index|types)\.ts|app/web/src/features/s5e[1-6]/index\.tsx|app/server/src/main/java/vn/geo/server/(lessonplan|exam|assessment|aigrade)/package-info\.java|app/server/src/main/resources/db/migration/V50[0-4]__|app/db/seed/(e2e/s5_000_|fixture-ids\.d/s5\.json)|app/server/src/(main|test)/resources/application[^/]*\.yml|app/e2e/tests/s5e[1-6]/\.gitkeep|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/server/src/main/java/vn/geo/server/aigateway/internal/(AnthropicPrompts|MockAiProvider)\.java|app/Makefile|app/README\.md|app/\.env\.ports)
- Kế hoạch: PLAN_code-s5 §3 W0

## T-261128-52 · S5-E2 · L5 Dev · epic-common v1.6 · wt s5-e2a · ‖53 · 4h/10M · Ra đề theo ma trận: nạp ma trận/ngân hàng, form, lắp nhiều mã đề Java, sửa và duyệt bộ đề [done]
- Mục tiêu: US-S5E2-01…04: nạp `materials/de-thi/` (4 ma trận, 110 mẫu câu, 1 774 câu) có kiểm schema + đáp số engine (sai số > 1e-6 thì từ chối); form ra đề (loại, lớp, chương, thời gian, số câu/điểm, tỉ lệ mức, 1–8 mã, QS5-07); lắp nhiều mã tương đương viết lại `lap_de.py` bằng Java giữ 7 ràng buộc, tái lập theo seed (QS5-06; IT đối chứng `check.py`); xem/sửa bộ đề nháp, thay câu cùng ô, nhãn hai trục "Máy lắp" (QS5-08), duyệt vào kho "Của tôi". Seed F-S5-030…032
- Nguồn: epics/S5-E*/{stories_ac,test_cases}.md v1.1 (gộp 80e9b6f 28/11); s5/brief-s5.md v0.2, estimate-s5.md v0.2, questions-s5.md v0.2 (28/11); PLAN_code-s5 v1.0 §3 W1, §5
- Xong khi: verify + web-build + e2e-affected xanh; mọi TC của US trong task có test gắn mã `TC-S5En-nn` và chạy đạt (traceability --strict); AI chỉ mock; status.md dòng Dev
- Kiểm: `cd .claude/worktrees/s5-e2a/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2,S3,S5-E2 --defer US-S5E2-05,US-S5E2-06" && python3 ../artifacts/GEO/work/s5/check_s5.py && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S5-E2`
- Ngoài phạm vi: xuất DOCX/PDF (54), soạn đề từ tài liệu (63), giao bài có chấm (57); sửa đặc tả S5 (stories_ac, test_cases, estimate, check_s5.py); tệp khung của 51; gọi AI thật
- Model: opus · Ngân sách: 4 giờ · Cổng: 54719 · 6749 · 8201 · 5281
- Phụ thuộc: T-261128-51
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/exam/(?!.*/(export|draft)/)|app/server/src/test/java/vn/geo/server/(it/s5e2|exam)/|app/web/src/features/s5e2/(?!(export|draft)/)|app/e2e/tests/s5e2/|app/server/pom\.xml|app/server/src/main/resources/db/migration/V5(0[5-9]|1[0-9]|2[0-4])__|app/db/seed/(e2e/s5_1[0-4]|fixture-ids\.d/s5e2)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/server/src/main/java/vn/geo/server/aigateway/internal/(AnthropicPrompts|MockAiProvider)\.java|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S5-E2/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s5 §3 W1

## T-261128-53 · S5-E1 · L5 Dev · epic-common v1.6 · wt s5-e1a · ‖52 · 3.5h/9M · Trợ lý giáo án: form B/C, máy chủ dựng tin nhắn + kiểm luật A, màn kết quả [done]
- Mục tiêu: US-S5E1-01…03: form "Soạn giáo án mới" (trường B bắt buộc, C tuỳ chọn, tích sẵn theo Tiết PPCT); máy chủ dựng tin nhắn theo prompt V8 (`prompt-lab/giao-an/prompt-final.md` v1.1), gọi task `lesson_plan` (mock F-S5-093/093b), kiểm tất định luật A, gọi lại một lần; hạn mức soạn 30 lượt/ngày (QS5-04); kiến thức `nhap-tam` vẫn cấp (QS5-03); màn kết quả dựng theo `kind`, nhãn "AI chưa chắc", khung đề xuất, câu hỏi lại. Seed F-S5-020, 021 THÊM (từ báo cáo T-51): thêm cả 8 purpose S5 (`lesson_plan`, `lesson_patch`, `class_notice`, `kb_qa`, `grade_ocr`, `grade`, `exam_draft`, `class_errors`) một lần vào tập cố định `aigateway/internal/InstrumentedAiGateway.java:45` để các task AI sau không sửa dòng này.
- Nguồn: epics/S5-E*/{stories_ac,test_cases}.md v1.1 (gộp 80e9b6f 28/11); s5/brief-s5.md v0.2, estimate-s5.md v0.2, questions-s5.md v0.2 (28/11); PLAN_code-s5 v1.0 §3 W1, §5
- Xong khi: verify + web-build + e2e-affected xanh; mọi TC của US trong task có test gắn mã `TC-S5En-nn` và chạy đạt (traceability --strict); AI chỉ mock; status.md dòng Dev
- Kiểm: `cd .claude/worktrees/s5-e1a/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2,S3,S5-E1 --defer US-S5E1-04,US-S5E1-05" && python3 ../artifacts/GEO/work/s5/check_s5.py && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S5-E1`
- Ngoài phạm vi: chat vá, duyệt và xuất (54); AI thật; sửa đặc tả S5 (stories_ac, test_cases, estimate, check_s5.py); tệp khung của 51; gọi AI thật
- Model: opus · Ngân sách: 3.5 giờ · Cổng: 54729 · 6759 · 8202 · 5282
- Phụ thuộc: T-261128-51
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/aigateway/internal/InstrumentedAiGateway\.java|app/server/src/main/java/vn/geo/server/lessonplan/(?!.*/(patch|export|list)/)|app/server/src/test/java/vn/geo/server/(it/s5e1|lessonplan)/|app/web/src/features/s5e1/|app/web/package(-lock)?\.json|app/e2e/tests/s5e1/|app/server/src/main/resources/db/migration/V5(2[5-9]|3[0-9]|4[0-4])__|app/db/seed/(e2e/s5_1[5-9]|fixture-ids\.d/s5e1)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/server/src/main/java/vn/geo/server/aigateway/internal/(AnthropicPrompts|MockAiProvider)\.java|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S5-E1/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s5 §3 W1

## T-261128-54 · S5-E1+E2-05 · L5 Dev · epic-common v1.6 · wt s5-e1b · ‖55 · 3.5h/9M · Chat vá giáo án, duyệt + xuất giáo án và mã đề (DOCX/PDF dùng chung) [done]
- Mục tiêu: US-S5E1-04 (chat vá theo mục, bản vá trước → sau, nhận từng dòng/cả bản, khoá mục, hoàn tác; task `lesson_patch` mock); US-S5E1-05 (duyệt, xuất DOCX khung 5512 + PDF, "Giáo án của tôi"); bộ dựng chung `common/export/docx` (Apache POI, QS5-05; pdfbox có sẵn); rồi US-S5E2-05 (xuất mã đề: đề, đáp án, hướng dẫn chấm, bảng đặc tả) dùng lại bộ dựng
- Nguồn: epics/S5-E*/{stories_ac,test_cases}.md v1.1 (gộp 80e9b6f 28/11); s5/brief-s5.md v0.2, estimate-s5.md v0.2, questions-s5.md v0.2 (28/11); PLAN_code-s5 v1.0 §3 W2, §5
- Xong khi: verify + web-build + e2e-affected xanh; mọi TC của US trong task có test gắn mã `TC-S5En-nn` và chạy đạt (traceability --strict); AI chỉ mock; status.md dòng Dev
- Kiểm: `cd .claude/worktrees/s5-e1b/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2,S3,S5-E1,S5-E2 --defer US-S5E2-06" && python3 ../artifacts/GEO/work/s5/check_s5.py && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S5-E1 S5-E2`
- Ngoài phạm vi: kho tri thức (56), luồng D (63); sửa đặc tả S5 (stories_ac, test_cases, estimate, check_s5.py); tệp khung của 51; gọi AI thật
- Model: opus · Ngân sách: 3.5 giờ · Cổng: 54739 · 6769 · 8203 · 5283
- Phụ thuộc: T-261128-52, T-261128-53
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/(lessonplan/.*/(patch|export|list)/|exam/.*/export/|common/export/)|app/server/src/test/java/vn/geo/server/(it/s5e1|it/s5e2|lessonplan|exam|common/export)/|app/web/src/features/(s5e1/|s5e2/export/)|app/e2e/tests/(s5e1|s5e2)/|app/server/pom\.xml|app/server/src/main/resources/db/migration/V5(5[5-9]|6[0-9]|7[0-4])__|app/db/seed/(e2e/s5_2[0-4]|fixture-ids\.d/s5e1b)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/server/src/main/java/vn/geo/server/aigateway/internal/(AnthropicPrompts|MockAiProvider)\.java|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S5-E1/(status|qc_findings)\.md|artifacts/GEO/work/epics/S5-E2/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s5 §3 W2

## T-261128-55 · S5-E5a · L5 Dev · epic-common v1.6 · wt s5-e5a · ‖54 · 2.5h/6M · Công tắc AI lớp, tắt khẩn, hạn mức + thông báo trợ lý soạn [done]
- Mục tiêu: US-S5E5-04: công tắc AI chấm/trợ lý theo lớp (mặc định QS5-26), tắt khẩn toàn hệ thống cho người vận hành, định nghĩa lượt + hạn mức lớp/học sinh/token, cảnh báo 80 % (QS5-04), báo cáo dùng AI; QS5-02 không multi-tenant; US-S5E5-01: trợ lý soạn nháp thông báo lớp (task `class_notice` mock), giáo viên sửa và tự bấm Gửi qua kênh thông báo S3-E4, dòng "Soạn với trợ lý AI, thầy cô đã duyệt" (QS5-25)
- Nguồn: epics/S5-E*/{stories_ac,test_cases}.md v1.1 (gộp 80e9b6f 28/11); s5/brief-s5.md v0.2, estimate-s5.md v0.2, questions-s5.md v0.2 (28/11); PLAN_code-s5 v1.0 §3 W2, §5
- Xong khi: verify + web-build + e2e-affected xanh; mọi TC của US trong task có test gắn mã `TC-S5En-nn` và chạy đạt (traceability --strict); AI chỉ mock; status.md dòng Dev
- Kiểm: `cd .claude/worktrees/s5-e5a/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2,S3,S5-E1,S5-E2,S5-E5 --defer US-S5E1-04,US-S5E1-05,US-S5E2-05,US-S5E2-06,US-S5E5-02,US-S5E5-03" && python3 ../artifacts/GEO/work/s5/check_s5.py && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S5-E5`
- Ngoài phạm vi: tổng hợp lỗi (64), thông báo đẩy ngoài ứng dụng; sửa đặc tả S5 (stories_ac, test_cases, estimate, check_s5.py); tệp khung của 51; gọi AI thật
- Model: opus · Ngân sách: 2.5 giờ · Cổng: 54749 · 6779 · 8204 · 5284
- Phụ thuộc: T-261128-52, T-261128-53
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/(aigateway/.*/(quota|killswitch)/|aigateway/.*ClassAiSwitch|notify/.*/draft/)|app/server/src/test/java/vn/geo/server/(it/s5e5|aigateway|notify)/|app/web/src/features/s5e5/|app/e2e/tests/s5e5/|app/server/src/main/resources/db/migration/V5(7[5-9]|8[0-9]|9[0-4])__|app/db/seed/(e2e/s5_2[5-9]|fixture-ids\.d/s5e5)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/server/src/main/java/vn/geo/server/aigateway/internal/(AnthropicPrompts|MockAiProvider)\.java|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S5-E5/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s5 §3 W2

## T-261128-56 · S5-E3a · L5 Dev · epic-common v1.6 · wt s5-e3a · ‖57 · 3.5h/9M · Kho tri thức: tài liệu có phiên bản, gắn lớp/bài, bộ truy xuất toàn văn tiếng Việt [done]
- Mục tiêu: US-S5E3-01 (thêm tài liệu, đưa giáo án đã duyệt vào kho, phiên bản bất biến, Nháp → Chờ duyệt → Đang dùng → Lưu trữ), US-S5E3-02 (gắn lớp/bài theo tài liệu, QS5-11, thời hạn hiệu lực), US-S5E3-07 (tách mục theo tiêu đề, tìm toàn văn PostgreSQL bỏ dấu, ưu tiên mã `KT11-…`, ngưỡng điểm, QS5-09). Seed F-S5-040, 041
- Nguồn: epics/S5-E*/{stories_ac,test_cases}.md v1.1 (gộp 80e9b6f 28/11); s5/brief-s5.md v0.2, estimate-s5.md v0.2, questions-s5.md v0.2 (28/11); PLAN_code-s5 v1.0 §3 W3, §5
- Xong khi: verify + web-build + e2e-affected xanh; mọi TC của US trong task có test gắn mã `TC-S5En-nn` và chạy đạt (traceability --strict); AI chỉ mock; status.md dòng Dev
- Kiểm: `cd .claude/worktrees/s5-e3a/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2,S3,S5-E1,S5-E2,S5-E3,S5-E5 --defer US-S5E2-06,US-S5E3-03,US-S5E3-04,US-S5E3-05,US-S5E3-06,US-S5E5-02,US-S5E5-03" && python3 ../artifacts/GEO/work/s5/check_s5.py && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S5-E3`
- Ngoài phạm vi: hỏi đáp + kiểm soát đáp án (58), lịch sử (63); sửa đặc tả S5 (stories_ac, test_cases, estimate, check_s5.py); tệp khung của 51; gọi AI thật
- Model: opus · Ngân sách: 3.5 giờ · Cổng: 54759 · 6789 · 8205 · 5285
- Phụ thuộc: T-261128-54, T-261128-55
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/knowledge/(internal/kb/(?!(qa|policy|firewall|history)/)|api/Kb)|app/server/src/test/java/vn/geo/server/(it/s5e3|knowledge)/|app/web/src/features/s5e3/|app/e2e/tests/s5e3/|app/server/src/main/resources/db/migration/V6(0[5-9]|1[0-9]|2[0-4])__|app/db/seed/(e2e/s5_3[0-4]|fixture-ids\.d/s5e3)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/server/src/main/java/vn/geo/server/aigateway/internal/(AnthropicPrompts|MockAiProvider)\.java|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S5-E3/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s5 §3 W3

## T-261128-57 · S5-E4a · L5 Dev · epic-common v1.6 · wt s5-e4a · ‖56 · 4h/10M · Rubric, giao bài có chấm, kho ảnh an toàn (sau cờ grading) [done]
- Mục tiêu: Sau cờ `geo.s5.grading.enabled` (mặc định tắt, PO-64/QS5-01 chưa chốt): US-S5E4-01 (rubric có phiên bản, 2 mẫu hệ thống, 1–10 tiêu chí bội 0,25, bất biến khi đã dùng, QS5-15), US-S5E4-02 (giao bài có chấm: đề hoặc bộ đề đã duyệt, rubric, chế độ chấm QS5-16 với cổng "AI đề xuất" stub luôn đóng `ClassAiGateQuery`, hạn nộp chỉ kéo dài QS5-17, nộp muộn, lịch công bố), US-S5E4-08 (cổng `ImageStore`: mã hoá AES-GCM tệp cục bộ ở dev/test, bản S3 tại VN sau cấu hình prod QS5-18; URL ký 5 phút; gỡ EXIF/GPS; xoá theo năm học và tài khoản; `forAi` bản gỡ EXIF QS5-30). Seed F-S5-051, 052 THÊM (từ T-55): mọi lời gọi AI chấm/OCR/Đọc lại phải đi qua `aigateway.api.quota.AiQuota` (hạn mức lớp/HS/token + kill switch) như móc thử `/test-hooks/s5e5/grading-run`, `/ocr-retry`; khi có luồng thật thì chuyển TC-S5E5-19/23/25/27 sang luồng thật.
- Nguồn: epics/S5-E*/{stories_ac,test_cases}.md v1.1 (gộp 80e9b6f 28/11); s5/brief-s5.md v0.2, estimate-s5.md v0.2, questions-s5.md v0.2 (28/11); PLAN_code-s5 v1.0 §3 W3, §5
- Xong khi: verify + web-build + e2e-affected xanh; mọi TC của US trong task có test gắn mã `TC-S5En-nn` và chạy đạt (traceability --strict); AI chỉ mock; status.md dòng Dev (cờ bật ở hồ sơ test/e2e, tắt mặc định ở prod)
- Kiểm: `cd .claude/worktrees/s5-e4a/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2,S3,S5-E1,S5-E2,S5-E4,S5-E5 --defer US-S5E2-06,US-S5E4-03,US-S5E4-04,US-S5E4-05,US-S5E4-06,US-S5E4-07,US-S5E4-09,US-S5E5-02,US-S5E5-03" && python3 ../artifacts/GEO/work/s5/check_s5.py && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S5-E4`
- Ngoài phạm vi: chụp nộp + OCR (59), chấm tay (60), mức trợ giúp (58); sửa đặc tả S5 (stories_ac, test_cases, estimate, check_s5.py); tệp khung của 51; gọi AI thật
- Model: opus · Ngân sách: 4 giờ · Cổng: 54769 · 6799 · 8206 · 5286
- Phụ thuộc: T-261128-54, T-261128-55
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/assessment/(api/|.*/(rubric|assign|storage)/)|app/server/src/test/java/vn/geo/server/(it/s5e4|assessment)/|app/web/src/features/s5e4/(index\.tsx|rubric/|assign/|storage/)|app/e2e/tests/s5e4/|app/server/pom\.xml|app/server/src/main/resources/db/migration/V6(2[5-9]|3[0-9]|4[0-4])__|app/db/seed/(e2e/s5_3[5-9]|fixture-ids\.d/s5e4)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/server/src/main/java/vn/geo/server/aigateway/internal/(AnthropicPrompts|MockAiProvider)\.java|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S5-E4/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s5 §3 W3

## T-261128-58 · S5-E3b · L5 Dev · epic-common v1.6 · wt s5-e3b · ‖59 · 3.5h/9M · Hỏi đáp theo kho có nguồn, mức trợ giúp + lịch đáp số, kiểm soát đáp án đầy đủ [done]
- Mục tiêu: US-S5E3-03 (hỏi đáp học sinh dựa trên kho đã gắn, luôn kèm nguồn, thông báo riêng tư trước lần hỏi đầu, task `kb_qa` mock F-S5-094/094b, QS5-10), US-S5E3-04 (mức trợ giúp theo bài mặc định QS5-12, lịch công bố đáp số, kiểm ở máy chủ trước khi gọi AI; ô chọn trong form bài có chấm `features/s5e4/assign/HelpLevel*`), US-S5E3-05 (bộ kiểm đầu ra KS-1…KS-5 mở rộng, lọc mục khớp đáp số QS5-13, không lộ ghi chú riêng/bài của bạn, nhật ký `ai_interaction`/`ai_policy_decision` QS5-14; bộ câu thử `fixtures/firewall-s5.json` ≥ 60 câu lộ = 0) THÊM (từ T-56): dùng cổng `knowledge.api.KbRetriever`; đề chưa có cột mã kiến thức → tự truyền mã KT của đề vào KbRetriever; gắn kết kho hiện theo bài giao S3 (`assignment`) — nối thêm bài có chấm của S5-E4 (bảng do T-57 tạo) nếu đã có.
- Nguồn: epics/S5-E*/{stories_ac,test_cases}.md v1.1 (gộp 80e9b6f 28/11); s5/brief-s5.md v0.2, estimate-s5.md v0.2, questions-s5.md v0.2 (28/11); PLAN_code-s5 v1.0 §3 W4, §5
- Xong khi: verify + web-build + e2e-affected xanh; mọi TC của US trong task có test gắn mã `TC-S5En-nn` và chạy đạt (traceability --strict); AI chỉ mock; status.md dòng Dev
- Kiểm: `cd .claude/worktrees/s5-e3b/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2,S3,S5-E1,S5-E2,S5-E3,S5-E4,S5-E5 --defer US-S5E2-06,US-S5E3-06,US-S5E4-03,US-S5E4-04,US-S5E4-05,US-S5E4-06,US-S5E4-07,US-S5E4-09,US-S5E5-02,US-S5E5-03" && python3 ../artifacts/GEO/work/s5/check_s5.py && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S5-E3`
- Ngoài phạm vi: lịch sử + báo chặn sai (63); sửa lõi KS MVP (V60); sửa đặc tả S5 (stories_ac, test_cases, estimate, check_s5.py); tệp khung của 51; gọi AI thật
- Model: opus · Ngân sách: 3.5 giờ · Cổng: 54779 · 6809 · 8207 · 5287
- Phụ thuộc: T-261128-56, T-261128-57
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/(knowledge/internal/kb/(qa|policy|firewall)/|answers/.*KsS5|assignment/.*HelpLevel)|app/server/src/test/java/vn/geo/server/(it/s5e3|knowledge|answers)/|app/web/src/features/(s5e3/|s5e4/assign/HelpLevel)|app/e2e/tests/s5e3/|app/e2e/fixtures/firewall-s5\.json|app/server/src/main/resources/db/migration/V6(5[5-9]|6[0-9]|7[0-4])__|app/db/seed/(e2e/s5_4[0-4]|fixture-ids\.d/s5e3b)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/server/src/main/java/vn/geo/server/aigateway/internal/(AnthropicPrompts|MockAiProvider)\.java|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S5-E3/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s5 §3 W4

## T-261128-59 · S5-E4b · L5 Dev · epic-common v1.6 · wt s5-e4b · ‖58 · 3.5h/9M · Chụp và nộp nhiều trang trên iPhone, gửi lại lũy đẳng, OCR bài nộp (sau cờ grading) [done]
- Mục tiêu: Sau cờ grading: US-S5E4-03 (chụp nhiều trang PWA, kiểm độ rõ ≥ 0,5, xếp trang, HEIC → JPEG, 1–10 trang ≤ 8 MB QS5-19, thông báo riêng tư S5 QS5-01), US-S5E4-09 (gửi lại khi mất mạng lũy đẳng, nộp lại trước hạn, nộp muộn có nhãn, tải dồn), US-S5E4-04 (bản đọc chữ phái sinh task `grade_ocr` mock F-S5-095, tô đoạn < 0,6 QS5-20, giáo viên sửa có lịch sử). OCR viết tay thật **cần bench owner duyệt (QS5-27)**, không chặn code. Seed F-S5-053…057 THÊM (từ T-55): mọi lời gọi AI chấm/OCR/Đọc lại phải đi qua `aigateway.api.quota.AiQuota` (hạn mức lớp/HS/token + kill switch) như móc thử `/test-hooks/s5e5/grading-run`, `/ocr-retry`; khi có luồng thật thì chuyển TC-S5E5-19/23/25/27 sang luồng thật. THÊM (từ T-57): dùng cổng `assessment.api.{ImageStore, SubmissionDataEraser, ClassAiGateQuery}`; task tạo bảng `submission`/`ocr_artifact`/`grading_proposal`/`approved_grade` phải implement `SubmissionDataEraser` (xoá theo tài khoản + năm học, cột `academic_year`) và thay bộ đếm bài nộp/móc `/test-hooks/s5e4/image` bằng luồng thật (Dev-S5E4-02, TC-S5E4-43 kiểm đủ dữ liệu bài nộp).
- Nguồn: epics/S5-E*/{stories_ac,test_cases}.md v1.1 (gộp 80e9b6f 28/11); s5/brief-s5.md v0.2, estimate-s5.md v0.2, questions-s5.md v0.2 (28/11); PLAN_code-s5 v1.0 §3 W4, §5
- Xong khi: verify + web-build + e2e-affected xanh; mọi TC của US trong task có test gắn mã `TC-S5En-nn` và chạy đạt (traceability --strict); AI chỉ mock; status.md dòng Dev (cờ bật ở hồ sơ test/e2e, tắt mặc định ở prod)
- Kiểm: `cd .claude/worktrees/s5-e4b/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2,S3,S5-E1,S5-E2,S5-E3,S5-E4,S5-E5 --defer US-S5E2-06,US-S5E3-03,US-S5E3-04,US-S5E3-05,US-S5E3-06,US-S5E4-05,US-S5E4-06,US-S5E4-07,US-S5E5-02,US-S5E5-03" && python3 ../artifacts/GEO/work/s5/check_s5.py && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S5-E4`
- Ngoài phạm vi: hàng chờ + chấm tay (60), đề xuất điểm (61); thiết bị iOS thật (tester ghi Manual); sửa đặc tả S5 (stories_ac, test_cases, estimate, check_s5.py); tệp khung của 51; gọi AI thật
- Model: opus · Ngân sách: 3.5 giờ · Cổng: 54789 · 6819 · 8208 · 5288
- Phụ thuộc: T-261128-56, T-261128-57
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/assessment/.*/(submit|ocr)/|app/server/src/test/java/vn/geo/server/(it/s5e4|assessment)/.*(Submit|Ocr|submit|ocr)|app/web/src/features/s5e4/(submit|ocr)/|app/web/package(-lock)?\.json|app/web/vite\.config\.ts|app/e2e/tests/s5e4/(submit|ocr)|app/server/src/main/resources/db/migration/V6(7[5-9]|8[0-9]|9[0-4])__|app/db/seed/(e2e/s5_4[5-9]|fixture-ids\.d/s5e4b)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/server/src/main/java/vn/geo/server/aigateway/internal/(AnthropicPrompts|MockAiProvider)\.java|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S5-E4/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s5 §3 W4

## T-261128-60 · S5-E4c · L5 Dev · epic-common v1.6 · wt s5-e4c · ‖61 · 3h/8M · Hàng chờ chấm, chấm tay theo rubric + duyệt điểm, trả bài (sau cờ grading) [done]
- Mục tiêu: Sau cờ grading: US-S5E4-05 (hàng chờ theo bài, trạng thái, cờ xếp trước, lọc lớp/bài), US-S5E4-06 (chấm tay theo rubric trên web, Duyệt thành `approved_grade`, nhật ký trước/sau, phím tắt QS5-23), US-S5E4-07 (trả bài theo lịch công bố, không lộ gì của AI, thu hồi 15 phút QS5-31, thông báo trong ứng dụng) THÊM (từ T-57): dùng cổng `assessment.api.{ImageStore, SubmissionDataEraser, ClassAiGateQuery}`; task tạo bảng `submission`/`ocr_artifact`/`grading_proposal`/`approved_grade` phải implement `SubmissionDataEraser` (xoá theo tài khoản + năm học, cột `academic_year`) và thay bộ đếm bài nộp/móc `/test-hooks/s5e4/image` bằng luồng thật (Dev-S5E4-02, TC-S5E4-43 kiểm đủ dữ liệu bài nộp). THÊM (từ T-59): (a) gỡ EXIF/GPS cả bản gốc HEIC khi lưu (BR ảnh bài làm, US-S5E4-08) — hiện chỉ bản JPEG đi kèm được gỡ; (b) `GradedAssignmentRepository` đếm bài nộp = `count(submission)` (hiện tăng mỗi lần nộp lại); (c) chuyển TC-S5E4-43/45 và TC-S5E5-19/23/25/27 từ móc thử sang luồng nộp/OCR thật nếu tệp nằm trong phạm vi; được sửa `app/web/src/features/s5e4/index.tsx` (route/rail). THÊM (từ T-58): gắn `features/s5e4/assign/HelpLevelPanel` vào form bài có chấm C5-01 (GradedPage của T-57) thay màn tạm `/gv/tro-giup-ai`; nối "Trả bài" với kiểm lộ đáp án KsS5Firewall (bộ kiểm so được nội dung bài của HS).
- Nguồn: epics/S5-E*/{stories_ac,test_cases}.md v1.1 (gộp 80e9b6f 28/11); s5/brief-s5.md v0.2, estimate-s5.md v0.2, questions-s5.md v0.2 (28/11); PLAN_code-s5 v1.0 §3 W5, §5
- Xong khi: verify + web-build + e2e-affected xanh; mọi TC của US trong task có test gắn mã `TC-S5En-nn` và chạy đạt (traceability --strict); AI chỉ mock; status.md dòng Dev (cờ bật ở hồ sơ test/e2e, tắt mặc định ở prod)
- Kiểm: `cd .claude/worktrees/s5-e4c/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2,S3,S5-E1,S5-E2,S5-E3,S5-E4,S5-E5 --defer US-S5E2-06,US-S5E3-06,US-S5E5-02,US-S5E5-03" && python3 ../artifacts/GEO/work/s5/check_s5.py && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S5-E4`
- Ngoài phạm vi: đề xuất AI trên màn chấm (62), phúc khảo (QS5-29); sửa đặc tả S5 (stories_ac, test_cases, estimate, check_s5.py); tệp khung của 51; gọi AI thật
- Model: opus · Ngân sách: 3 giờ · Cổng: 54799 · 6829 · 8209 · 5289
- Phụ thuộc: T-261128-58, T-261128-59
- Phạm vi tệp: ^(app/web/src/features/s5e4/index\.tsx|app/server/src/main/java/vn/geo/server/assessment/|app/server/src/main/java/vn/geo/server/assessment/.*/(queue|review|returned)/|app/server/src/test/java/vn/geo/server/(it/s5e4|assessment)/.*(Queue|Review|Return|queue|review|return)|app/web/src/features/s5e4/(queue|review|return)/|app/e2e/tests/s5e4/(queue|review|return)|app/server/src/main/resources/db/migration/V7(0[5-9]|1[0-9]|2[0-4])__|app/db/seed/(e2e/s5_5[0-4]|fixture-ids\.d/s5e4c)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/server/src/main/java/vn/geo/server/aigateway/internal/(AnthropicPrompts|MockAiProvider)\.java|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S5-E4/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s5 §3 W5

## T-261128-61 · S5-E6a · L5 Dev · epic-common v1.6 · wt s5-e6a · ‖60 · 2.5h/6M · AI đề xuất điểm theo tiêu chí + engine làm tín hiệu thứ hai (máy chủ, sau cờ ai-grading) [done]
- Mục tiêu: Sau cờ `geo.s5.ai-grading.enabled` (mặc định tắt): US-S5E6-01 (task `grade` mock F-S5-090/091: điểm từng tiêu chí, lý do, vùng ảnh, tin cậy; máy chủ kiểm thang, tổng, luật chặn KPI v1.0), US-S5E6-03 (đối chiếu đáp số cuối với engine qua bộ so khớp KS-1/KS-2, lệch thì gắn cờ). Chỉ API + IT, không giao diện. Chất lượng chấm AI thật **cần bench owner duyệt (QS5-27)**, không chặn code THÊM (từ T-55): mọi lời gọi AI chấm/OCR/Đọc lại phải đi qua `aigateway.api.quota.AiQuota` (hạn mức lớp/HS/token + kill switch) như móc thử `/test-hooks/s5e5/grading-run`, `/ocr-retry`; khi có luồng thật thì chuyển TC-S5E5-19/23/25/27 sang luồng thật. THÊM (từ T-57): dùng cổng `assessment.api.{ImageStore, SubmissionDataEraser, ClassAiGateQuery}`; task tạo bảng `submission`/`ocr_artifact`/`grading_proposal`/`approved_grade` phải implement `SubmissionDataEraser` (xoá theo tài khoản + năm học, cột `academic_year`) và thay bộ đếm bài nộp/móc `/test-hooks/s5e4/image` bằng luồng thật (Dev-S5E4-02, TC-S5E4-43 kiểm đủ dữ liệu bài nộp).
- Nguồn: epics/S5-E*/{stories_ac,test_cases}.md v1.1 (gộp 80e9b6f 28/11); s5/brief-s5.md v0.2, estimate-s5.md v0.2, questions-s5.md v0.2 (28/11); PLAN_code-s5 v1.0 §3 W5, §5
- Xong khi: verify + web-build + e2e-affected xanh; mọi TC của US trong task có test gắn mã `TC-S5En-nn` và chạy đạt (traceability --strict); AI chỉ mock; status.md dòng Dev (cờ bật ở hồ sơ test/e2e, tắt mặc định ở prod)
- Kiểm: `cd .claude/worktrees/s5-e6a/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2,S3,S5-E1,S5-E2,S5-E3,S5-E4,S5-E5,S5-E6 --defer US-S5E2-06,US-S5E3-06,US-S5E4-05,US-S5E4-06,US-S5E4-07,US-S5E5-02,US-S5E5-03,US-S5E6-02,US-S5E6-04,US-S5E6-05,US-S5E6-06" && python3 ../artifacts/GEO/work/s5/check_s5.py && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S5-E6`
- Ngoài phạm vi: giao diện chấm có đề xuất (62), KPI/cổng (65); sửa đặc tả S5 (stories_ac, test_cases, estimate, check_s5.py); tệp khung của 51; gọi AI thật
- Model: opus · Ngân sách: 2.5 giờ · Cổng: 54809 · 6839 · 8210 · 5290
- Phụ thuộc: T-261128-58, T-261128-59
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/aigrade/(api/|.*/(proposal|engine)/)|app/server/src/test/java/vn/geo/server/(it/s5e6|aigrade)/|app/e2e/tests/s5e6/[^/]*\.api\.spec\.ts|app/server/pom\.xml|app/server/src/main/resources/db/migration/V7(2[5-9]|3[0-9]|4[0-4])__|app/db/seed/(e2e/s5_5[5-9]|fixture-ids\.d/s5e6)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/server/src/main/java/vn/geo/server/aigateway/internal/(AnthropicPrompts|MockAiProvider)\.java|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S5-E6/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s5 §3 W5

## T-261128-62 · S5-E6b · L5 Dev · epic-common v1.6 · wt s5-e6b · ‖63 · 3.5h/9M · AI từ chối chấm, màn chấm có đề xuất AI, chạy song song (sau cờ ai-grading) [done]
- Mục tiêu: Sau cờ ai-grading: US-S5E6-02 (từ chối: ảnh kém F-S5-092, tin cậy < 0,6 QS5-22, hết giờ → chuyển chấm tay có lý do), US-S5E6-04 (màn chấm: nhận/sửa/bỏ từng tiêu chí, khoanh vùng ảnh, nhận cả bài khi không có cờ; cắm qua `assessment/**/review/AiProposalSlot*`), US-S5E6-05 (chạy song song: giáo viên chấm tay trước, đề xuất hiện sau khi duyệt, lưu cặp so sánh, lấy mẫu QS5-24)
- Nguồn: epics/S5-E*/{stories_ac,test_cases}.md v1.1 (gộp 80e9b6f 28/11); s5/brief-s5.md v0.2, estimate-s5.md v0.2, questions-s5.md v0.2 (28/11); PLAN_code-s5 v1.0 §3 W6, §5
- Xong khi: verify + web-build + e2e-affected xanh; mọi TC của US trong task có test gắn mã `TC-S5En-nn` và chạy đạt (traceability --strict); AI chỉ mock; status.md dòng Dev (cờ bật ở hồ sơ test/e2e, tắt mặc định ở prod)
- Kiểm: `cd .claude/worktrees/s5-e6b/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2,S3,S5 --defer US-S5E2-06,US-S5E3-06,US-S5E5-02,US-S5E5-03,US-S5E6-06" && python3 ../artifacts/GEO/work/s5/check_s5.py && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S5-E6`
- Ngoài phạm vi: báo cáo KPI + cổng (65); sửa đặc tả S5 (stories_ac, test_cases, estimate, check_s5.py); tệp khung của 51; gọi AI thật
- Model: opus · Ngân sách: 3.5 giờ · Cổng: 54819 · 6849 · 8211 · 5291
- Phụ thuộc: T-261128-60, T-261128-61
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/(aigrade/.*/(refusal|shadow|review)/|assessment/.*/review/AiProposalSlot)|app/server/src/test/java/vn/geo/server/(it/s5e6|aigrade)/|app/web/src/features/s5e6/(?!kpi/)|app/web/package(-lock)?\.json|app/e2e/tests/s5e6/|app/server/src/main/resources/db/migration/V7(5[5-9]|6[0-9]|7[0-4])__|app/db/seed/(e2e/s5_6[0-4]|fixture-ids\.d/s5e6b)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/server/src/main/java/vn/geo/server/aigateway/internal/(AnthropicPrompts|MockAiProvider)\.java|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S5-E6/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s5 §3 W6

## T-261128-63 · S5-E2-06+E3-06 · L5 Dev · epic-common v1.6 · wt s5-e2e3 · ‖62 · 2.5h/6M · Trợ lý soạn đề nháp từ tài liệu (luồng D) + giáo viên xem lịch sử hỏi đáp, báo chặn sai [done]
- Mục tiêu: US-S5E2-06 (soạn đề nháp từ tài liệu Đang dùng của giáo viên, task `exam_draft` mock, đi qua đường kiểm chứng đề S2-E3, hạn mức BR-S5E1-08, chặn tài liệu của giáo viên khác F-S5-041); US-S5E3-06 (lịch sử hỏi đáp theo bài, báo "chặn sai"/"lẽ ra phải chặn")
- Nguồn: epics/S5-E*/{stories_ac,test_cases}.md v1.1 (gộp 80e9b6f 28/11); s5/brief-s5.md v0.2, estimate-s5.md v0.2, questions-s5.md v0.2 (28/11); PLAN_code-s5 v1.0 §3 W6, §5
- Xong khi: verify + web-build + e2e-affected xanh; mọi TC của US trong task có test gắn mã `TC-S5En-nn` và chạy đạt (traceability --strict); AI chỉ mock; status.md dòng Dev
- Kiểm: `cd .claude/worktrees/s5-e2e3/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2,S3,S5 --defer US-S5E5-02,US-S5E5-03,US-S5E6-02,US-S5E6-04,US-S5E6-05,US-S5E6-06" && python3 ../artifacts/GEO/work/s5/check_s5.py && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S5-E2 S5-E3`
- Ngoài phạm vi: sửa lắp đề (52), bộ kiểm KS (58); sửa đặc tả S5 (stories_ac, test_cases, estimate, check_s5.py); tệp khung của 51; gọi AI thật
- Model: opus · Ngân sách: 2.5 giờ · Cổng: 54829 · 6859 · 8212 · 5292
- Phụ thuộc: T-261128-60, T-261128-61
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/(exam/.*/draft/|knowledge/internal/kb/history/)|app/server/src/test/java/vn/geo/server/(it/s5e2|it/s5e3|exam|knowledge)/.*(Draft|History|draft|history)|app/web/src/features/(s5e2/draft|s5e3/history)/|app/e2e/tests/(s5e2/draft|s5e3/history)|app/server/src/main/resources/db/migration/V7(7[5-9]|8[0-9]|9[0-4])__|app/db/seed/(e2e/s5_6[5-9]|fixture-ids\.d/s5e2e3)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/server/src/main/java/vn/geo/server/aigateway/internal/(AnthropicPrompts|MockAiProvider)\.java|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S5-E2/(status|qc_findings)\.md|artifacts/GEO/work/epics/S5-E3/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s5 §3 W6

## T-261128-64 · S5-E5b · L5 Dev · epic-common v1.6 · wt s5-e5b · ‖65 · 2.5h/6M · Tổng hợp lỗi sai của lớp (COP-02) + Nên nhắc lại trên lớp (sau cờ grading) [done]
- Mục tiêu: Sau cờ grading: US-S5E5-02 (tổng hợp lỗi theo tiêu chí và loại lỗi từ `approved_grade` + `criterion_assessment` + tín hiệu engine, có bằng chứng, không gắn nhãn học sinh; task `class_errors` mock), US-S5E5-03 (từ nhận định gợi ý bài luyện từ ngân hàng + tài liệu trong kho, giáo viên chọn rồi giao bộ bài S3-E2 hoặc chiếu) THÊM (từ T-55/T-60): chuyển TC-S5E5-19/23/25/27 từ móc `/test-hooks/s5e5/grading-run`, `/ocr-retry` sang luồng chấm/OCR thật (assessment + aigrade đã có).
- Nguồn: epics/S5-E*/{stories_ac,test_cases}.md v1.1 (gộp 80e9b6f 28/11); s5/brief-s5.md v0.2, estimate-s5.md v0.2, questions-s5.md v0.2 (28/11); PLAN_code-s5 v1.0 §3 W7, §5
- Xong khi: verify + web-build + e2e-affected xanh; mọi TC của US trong task có test gắn mã `TC-S5En-nn` và chạy đạt (traceability --strict); AI chỉ mock; status.md dòng Dev (cờ bật ở hồ sơ test/e2e, tắt mặc định ở prod)
- Kiểm: `cd .claude/worktrees/s5-e5b/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2,S3,S5 --defer US-S5E6-06" && python3 ../artifacts/GEO/work/s5/check_s5.py && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S5-E5`
- Ngoài phạm vi: cổng KPI (65); sửa đặc tả S5 (stories_ac, test_cases, estimate, check_s5.py); tệp khung của 51; gọi AI thật
- Model: opus · Ngân sách: 2.5 giờ · Cổng: 54839 · 6869 · 8213 · 5293
- Phụ thuộc: T-261128-62, T-261128-63
- Phạm vi tệp: ^(app/e2e/tests/s5e5/|app/server/src/main/java/vn/geo/server/tracking/.*/errors/|app/server/src/test/java/vn/geo/server/(it/s5e5|tracking)/.*(Error|error|Insight|insight)|app/web/src/features/s5e5/insights/|app/e2e/tests/s5e5/insights|app/server/src/main/resources/db/migration/V8(0[5-9]|1[0-9]|2[0-4])__|app/db/seed/(e2e/s5_7[0-4]|fixture-ids\.d/s5e5b)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/server/src/main/java/vn/geo/server/aigateway/internal/(AnthropicPrompts|MockAiProvider)\.java|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S5-E5/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s5 §3 W7

## T-261128-65 · S5-E6c · L5 Dev · epic-common v1.6 · wt s5-e6c · ‖64 · 1.5h/4M · Báo cáo khớp AI–giáo viên theo KPI §2–§4 (KTC) + cổng bật/đóng/mở lại AI đề xuất (sau cờ ai-grading) [done]
- Mục tiêu: Sau cờ ai-grading: US-S5E6-06 (báo cáo khớp theo KPI v1.0 §2–§4 với KTC 95 % Wilson/t QS5-32, ≥ 20 bài QS5-21, cổng bật/đóng/mở lại theo lớp, tự đóng sau hai đợt dưới ngưỡng QS5-24); thay stub `ClassAiGateQuery` của 57 bằng bản thật THÊM (từ T-58/T-60): (a) gắn `features/s5e4/assign/HelpLevelPanel` vào form bài có chấm C5-01 (GradedPage), bỏ màn tạm `/gv/tro-giup-ai` nếu thừa; (b) nối "Trả bài"/hỏi đáp với KsS5Firewall để bộ kiểm so được nội dung bài HS; (c) chuyển TC-S5E4-45 sang luồng thật. Lưu ý: V705 định nghĩa lại cả `notification_kind_check` — thêm kind mới thì viết lại đủ danh sách. THÊM (từ T-62): (d) bỏ gọi reflection trong `aigrade/internal/shadow/ShadowGrader` → `ProposalService.grade` public hoặc job `pending` nhận cả chế độ `parallel` (Dev-S5E6-08); (e) hàng chờ `/gv/hang-cho` mở `/gv/cham-ai/:id` khi bài có đề xuất AI và xếp bài có cờ lên đầu qua `AiGradeSignals` (trừ chế độ chấm mù/parallel).
- Nguồn: epics/S5-E*/{stories_ac,test_cases}.md v1.1 (gộp 80e9b6f 28/11); s5/brief-s5.md v0.2, estimate-s5.md v0.2, questions-s5.md v0.2 (28/11); PLAN_code-s5 v1.0 §3 W7, §5
- Xong khi: verify + web-build + e2e-affected xanh; mọi TC của US trong task có test gắn mã `TC-S5En-nn` và chạy đạt (traceability --strict); AI chỉ mock; status.md dòng Dev (cờ bật ở hồ sơ test/e2e, tắt mặc định ở prod)
- Kiểm: `cd .claude/worktrees/s5-e6c/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2,S3,S5 --defer US-S5E5-02,US-S5E5-03" && python3 ../artifacts/GEO/work/s5/check_s5.py && python3 ../artifacts/GEO/work/tools/check_ac_tc.py S5-E6`
- Ngoài phạm vi: tổng hợp lỗi (64); bật cổng thật cho lớp (cần bench QS5-27 + lớp pilot); sửa đặc tả S5 (stories_ac, test_cases, estimate, check_s5.py); tệp khung của 51; gọi AI thật
- Model: opus · Ngân sách: 1.5 giờ · Cổng: 54859 · 6889 · 8215 · 5295
- Phụ thuộc: T-261128-62, T-261128-63
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/aigrade/|app/web/src/features/(s5e3|s5e4)/|app/e2e/tests/(s5e3|s5e4)/|app/server/src/main/java/vn/geo/server/(knowledge|answers|assessment)/|app/server/src/main/java/vn/geo/server/(aigrade/.*/(kpi|gate)/|assessment/api/ClassAiGate)|app/server/src/test/java/vn/geo/server/(it/s5e6|aigrade)/.*(Kpi|Gate|kpi|gate)|app/web/src/features/s5e6/kpi/|app/e2e/tests/s5e6/kpi|app/server/src/main/resources/db/migration/V8(2[5-9]|3[0-9]|4[0-4])__|app/db/seed/(e2e/s5_7[5-9]|fixture-ids\.d/s5e6c)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|app/server/src/main/java/vn/geo/server/aigateway/internal/(AnthropicPrompts|MockAiProvider)\.java|app/Makefile|app/README\.md|artifacts/GEO/work/epics/S5-E6/(status|qc_findings)\.md)
- Kế hoạch: PLAN_code-s5 §3 W7

## T-261128-68 · S5-FIX · L5 Dev FIX W7 · epic-common v1.6 · wt s5-fix · 2h/5M · Việc nối tiếp còn lại của chặng 5 [done]
- Mục tiêu: (1) nối "Trả bài" thật (`grade_return`) và nội dung bài nộp của HS vào KsS5Firewall để bộ kiểm lộ đáp án so được nội dung bài (Dev-S5E3-13); (2) chuyển TC-S5E4-45 và (nếu T-64 chưa làm) TC-S5E5-19/23/25/27 từ móc thử sang luồng nộp/OCR/chấm thật; (3) lấy mẫu chấm song song có điều kiện "tối thiểu 2 bài" (US-S5E6-04); (4) ghi giờ mở màn chấm để KPI (g) có số liệu thật; (5) cập nhật `S5-E3/qc_findings.md` (Dev-S5E3-12 đã xử lý ở T-65); (6) nếu còn ngân sách: TC-S5E4-49 trượt riêng ui-webkit. (7) TC-S5E5-23: khi hết hạn mức 200/200, màn hiển thị lý do hạn mức thay vì "Chưa đọc được" (code assessment, Dev-S5E5-10).
- Nguồn: báo cáo T-261128-58/60/62/65 (28/11); epics/S5-E3, S5-E4, S5-E6 qc_findings.md v0.3; PLAN_code-s5 v1.0 §4 (FIX W7)
- Xong khi: verify + web-build + e2e-affected xanh; traceability --strict MVP,S2,S3,S5 đạt; các TC nêu trên chạy luồng thật (grep không còn móc `/test-hooks/s5e5/grading-run` trong tests/s5e5 nếu đã chuyển).
- Kiểm: `cd .claude/worktrees/s5-fix/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2,S3,S5" && python3 ../artifacts/GEO/work/s5/check_s5.py`
- Ngoài phạm vi: tính năng mới ngoài danh sách trên; sửa đặc tả S5 (stories_ac, test_cases); gọi AI thật · Chỉ sửa tệp khớp regex: ^(app/server/src/main/java/vn/geo/server/(assessment|aigrade|knowledge|answers)/|app/server/src/test/java/vn/geo/server/(it/s5e[3-6]|assessment|aigrade|knowledge|answers)/|app/web/src/features/s5e[3-6]/|app/e2e/tests/s5e[3-6]/|app/server/src/main/resources/db/migration/V8(4[5-9]|5[0-4])__|app/db/seed/(e2e/s5_9|fixture-ids\.d/s5fix)|app/server/quality/sql-lint\.properties|app/server/src/test/java/vn/geo/server/quality/|app/server/src/main/resources/application\.yml|artifacts/GEO/work/epics/S5-E[3-6]/(status|qc_findings)\.md)
- Model: opus · Ngân sách: 2 giờ
- Phụ thuộc: T-261128-64, T-261128-65
- Cổng: 54889
- Kế hoạch: PLAN_code-s5 §4 FIX W7

## T-261128-69 · TOOL · L5 Dev công cụ E2E cách lai · wt e2e-lai · 1h/2M · e2e-affected: tệp dùng chung → API trọn bộ, UI chỉ màn của task [done]
- Mục tiêu: theo DECISIONS 28/11 "E2E cách lai": (1) `app/scripts/e2e-affected.mjs` + `artifacts/GEO/work/s23/impact-map.json`: tệp dùng chung (quality/allowed_fields.tsv, quality/sql-lint.properties, application*.yml, aigateway/internal/{AnthropicPrompts,MockAiProvider}.java, Makefile, README) không kéo lên L2 nữa mà ra mức mới `LA` = project api trọn bộ + KHÔNG UI; (2) thêm cờ `--ui-own` (mặc định bật khi mức ≤ L1): UI chỉ chạy spec `*.ui.spec.ts` của feature/epic có tệp đổi, trên ui-chromium; mobile P0 giữ như cũ; (3) thêm target `make e2e-full` = `scripts/e2e.sh` trọn bộ mọi project (dùng cho lượt định kỳ 2–3 đợt và tester); (4) selftest của bản đồ thêm ≥ 3 ca mới (tệp dùng chung → LA; feature s5e4 web → UI chỉ s5e4; migration ngoài khoảng → L2) và đều đạt; README mục E2E ghi cách lai.
- Nguồn: DECISIONS.md dòng 28/11 "E2E cách lai"; app/scripts/e2e-affected.mjs (đầu tệp mô tả L0/L1/L2); impact-map.json v1.1 (T-51); số liệu: UI ≈ 90 % thời gian (E2E-S23: UI 678 test 3,4 phút, API 285 test 24 giây)
- Xong khi: selftest đạt; dry-run trên diff mẫu chỉ tệp allowed_fields cho mức LA không có UI.
- Kiểm: `cd .claude/worktrees/e2e-lai/app && make e2e-affected-selftest && node scripts/e2e-affected.mjs --dry-run --files app/server/src/test/java/vn/geo/server/quality/allowed_fields.tsv | grep -q 'LA' && make verify`
- Ngoài phạm vi: test/spec nghiệp vụ; code máy chủ/web; cấu hình playwright projects (chỉ đọc)
- Model: sonnet · Ngân sách: 1 giờ
- Phụ thuộc: T-261128-65
- Phạm vi tệp: ^(app/scripts/e2e-affected[^/]*|artifacts/GEO/work/s23/impact-map\.json|app/Makefile|app/README\.md|app/\.env\.ports)
- Cổng: 54869

## T-261128-66 · S5 · L6 Tester · tester SKILL + Playwright · wt s5-test · 3h/4M · Kiểm thử chặng 5 [done]
- Mục tiêu: chạy 219 TC chặng 5 trên `worktree-mvp-wave0` đã gộp W0–W7 (cờ grading + ai-grading bật ở hồ sơ e2e): E2E-UI bằng Playwright trước, còn lại E2E-API; ca quyền theo quan hệ (giáo viên khác, học sinh lớp khác), không lộ đáp án (bộ `firewall-s5.json` lộ = 0), không lộ gì của AI cho học sinh; iOS/HEIC thiết bị thật ghi Manual; bằng chứng + REPORT.md + BUG-LIST.md theo mẫu `artifacts/GEO/evidence/E2E-S23-20261128/` THEO DECISIONS 28/11 "E2E cách lai" + /goal: chạy `make e2e-full` trên nhánh tích hợp (UI + tương tác bằng Playwright TRƯỚC, phần còn lại qua API); ca trượt chạy lại 1 lần để tách lỗi thật / chập chờn; KHÔNG sửa code app. Ra `artifacts/GEO/evidence/E2E-S5-<ngày>/` gồm REPORT.md (đạt/tổng theo epic, UI/API), BUG-LIST.md + BUG-LIST.html: mỗi lỗi ≥ 1 ảnh NHÚNG base64 (data URI, ≤ 300 KB/ảnh, KHÔNG link tệp; lỗi API chụp trang HTML request/response bằng Playwright) + check_buglist.py — theo đúng mẫu `artifacts/GEO/evidence/E2E-S23-20261128/`. Commit + không push (phiên chính push).
- Nguồn: epics/S5-*/test_cases.md v1.1 (80e9b6f 28/11); mẫu E2E-S23-20261128/REPORT.md (28/11); scope.json tester; PLAN_code-s5 v1.0 §3 W8
- Xong khi: mọi TC S5 có status; lệch audit = 0; REPORT.md có bảng UI/API đạt/tổng
- Kiểm: `cd .claude/worktrees/s5-test/app && make traceability ARGS="--strict --epic MVP,S2,S3,S5" && D=$(ls -d ../artifacts/GEO/evidence/E2E-S5-* | tail -1) && test -s $D/REPORT.md && test -s $D/BUG-LIST.md && test -s $D/BUG-LIST.html && python3 $D/check_buglist.py`
- Ngoài phạm vi: sửa code (lỗi → BUG-LIST, phiên chính tạo FIX); AI thật; bench QS5-27
- Model: opus · Ngân sách: 3 giờ
- Phụ thuộc: T-261128-64, T-261128-65, T-261128-68
- Phạm vi tệp: ^(artifacts/GEO/evidence/E2E-S5-|app/e2e/evidence/|artifacts/GEO/work/epics/S5-E[1-6]/status\.md)
- Kế hoạch: PLAN_code-s5 §3 W8

## T-261128-70 · S5-FIX · L5 Dev FIX soát độc lập · epic-common v1.6 · wt s5-fix2 · 1.5h/4M · Sửa 3 Major + 3 Minor từ REVIEW-S5-20261128 [done]
- Mục tiêu: sửa theo `artifacts/GEO/evidence/REVIEW-S5-20261128.md`: (M1) chấm mù — `GET /teacher/graded-assignments/{id}/ai-signals` (ProposalService:396) không trả total/flags/reason của AI cho bài chế độ song song chưa duyệt (BR-S5E6-13), mở rộng ShadowGuard; (M2) `POST /student/kb-qa` đi qua hạn mức HS 30/lớp 300 và kill switch như `/assignments/{id}/questions` (StudentQaQuotaFilter:48); (M3) MetadataStripper JPEG cắt tại EOI, bỏ dữ liệu sau EOI (MPF/thumbnail) — có test ảnh mẫu chứa EXIF sau EOI; (m4) ImagePurgeService xoá dữ liệu bài nộp/OCR/đề xuất theo tài khoản/năm học kể cả khi không còn `stored_image`; (m5) HeicStripper gỡ EXIF thất bại → từ chối lưu (fail closed, thông báo HS chụp lại), không lưu bản gốc; (m6) V705:91 mở lời giải theo at_time: đối chiếu BR-S5E4-23 với `ReturnService.result`, sửa cho thống nhất (migration mới trong khoảng). Mỗi mục có test (IT hoặc e2e API) gắn mã TC liên quan. Cập nhật bảng REVIEW: thêm cột Trạng thái (Đã sửa / Chấp nhận + lý do).
- Nguồn: artifacts/GEO/evidence/REVIEW-S5-20261128.md v1.0 (28/11); epics/S5-E4, S5-E6, S5-E5 stories_ac.md (BR-S5E6-13, BR-S5E4-23); DECISIONS 28/11 "E2E cách lai"
- Xong khi: verify + web-build + e2e-affected xanh; traceability --strict MVP,S2,S3,S5 đạt; REVIEW có cột Trạng thái cho 8 mục.
- Kiểm: `cd .claude/worktrees/s5-fix2/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2,S3,S5" && python3 ../artifacts/GEO/work/s5/check_s5.py && grep -q 'Trạng thái' ../artifacts/GEO/evidence/REVIEW-S5-20261128.md`
- Ngoài phạm vi: tính năng mới; sửa đặc tả; gọi AI thật
- Model: opus · Ngân sách: 1.5 giờ
- Phụ thuộc: T-261128-66
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/(assessment|aigrade|aigateway|knowledge|answers)/|app/server/src/test/java/vn/geo/server/|app/e2e/tests/s5e[3-6]/|app/e2e/fixtures/|app/server/src/main/resources/db/migration/V8(4[8-9]|5[0-4])__|app/server/quality/sql-lint\.properties|app/server/src/main/resources/application\.yml|artifacts/GEO/evidence/REVIEW-S5-20261128\.md|artifacts/GEO/work/epics/S5-E[3-6]/(status|qc_findings)\.md)
- Cổng: 54889

## T-261128-71 · S5-FIX · L5 Dev FIX REVIEW mục 8 · epic-common v1.6 · wt s5-fix3 · 1h/2M · OcrService cộng token AI vào trần token của lớp [done]
- Mục tiêu: REVIEW-S5-20261128 mục 8: mọi lời gọi `grade_ocr` (OCR tự động + "Đọc lại") cộng token AI vào trần token của lớp qua `aigateway.api.quota.AiQuota.addTokensSince` (cách T-70 làm cho `KbQaService.ask`); khi trần token lớp đã chạm thì OCR không gọi AI và ghi lý do hạn mức (`ocr_artifact.reason`, như V846). Có IT hoặc e2e API kiểm: sau N lượt OCR mock, bộ đếm token lớp tăng đúng; chạm trần → không gọi AI. Cập nhật REVIEW mục 8 → "Đã sửa" (tăng v1.2) và qc_findings S5-E4.
- Nguồn: artifacts/GEO/evidence/REVIEW-S5-20261128.md v1.1 mục 8 (28/11); T-70 (KbQaService + AiQuota.addTokensSince); epics/S5-E5 stories_ac.md (hạn mức/trần token)
- Xong khi: verify + web-build + e2e-affected xanh; traceability --strict đạt; REVIEW mục 8 "Đã sửa".
- Kiểm: `cd .claude/worktrees/s5-fix3/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2,S3,S5" && grep -q 'v1.2' ../artifacts/GEO/evidence/REVIEW-S5-20261128.md`
- Ngoài phạm vi: tính năng khác; sửa đặc tả; gọi AI thật
- Model: sonnet · Ngân sách: 1 giờ
- Phụ thuộc: T-261128-70
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/(assessment|aigateway)/|app/server/src/test/java/vn/geo/server/|app/e2e/tests/s5e[45]/|artifacts/GEO/evidence/REVIEW-S5-20261128\.md|artifacts/GEO/work/epics/S5-E4/(status|qc_findings)\.md)
- Cổng: 54899

## T-261128-72 · S5-FIX · L5 Dev FIX REVIEW mục 7 · epic-common v1.6 · wt s5-fix4 · 1.5h/3M · job ghi bù cặp điểm AI/GV khi điểm cắm AI lỗi lúc Duyệt [done]
- Mục tiêu: REVIEW-S5-20261128 mục 7: khi `AiProposalSlot.approved(...)`/`recordFirstApproval` lỗi trong `AiProposalSlotAdvice` (điểm GV đã lưu, chỉ log.warn), cặp (điểm AI, điểm GV) không được mất êm. Cách đã chốt (DECISIONS 28/11): GIỮ giao dịch Duyệt không đổi (AI lỗi không làm hỏng chấm tay) + job ghi bù định kỳ/idempotent điền điểm GV còn thiếu vào bản so sánh song song (mở rộng cách ShadowService.comparison đang điền bù điểm AI), đếm số lần ghi bù (log/metric). Có IT: giả lập recordFirstApproval ném lỗi → chạy job → cặp đủ, KPI/cổng AI (BR-S5E6-14) tính đúng; chạy job 2 lần không nhân đôi. Cập nhật REVIEW mục 7 → "Đã sửa" (tăng v1.3) và qc_findings S5-E6.
- Nguồn: artifacts/GEO/evidence/REVIEW-S5-20261128.md v1.2 mục 7 (28/11); epics/S5-E6 stories_ac.md (BR-S5E6-14); DECISIONS.md dòng 28/11 "REVIEW mục 7: job ghi bù"
- Xong khi: verify + web-build + e2e-affected xanh; traceability --strict đạt; REVIEW mục 7 "Đã sửa", phiên bản v1.3.
- Kiểm: `cd .claude/worktrees/s5-fix4/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2,S3,S5" && grep -q 'v1.3' ../artifacts/GEO/evidence/REVIEW-S5-20261128.md`
- Ngoài phạm vi: đổi giao dịch Duyệt; tính năng khác; sửa đặc tả; gọi AI thật; bật cờ grading
- Model: sonnet · Ngân sách: 1,5 giờ
- Phụ thuộc: T-261128-71
- Phạm vi tệp: ^(app/server/src/main/java/vn/geo/server/(assessment|aigrade|aigateway)/|app/server/src/main/resources/db/migration/|app/server/src/test/java/vn/geo/server/|app/e2e/tests/s5e6/|artifacts/GEO/evidence/REVIEW-S5-20261128\.md|artifacts/GEO/work/epics/S5-E6/(status|qc_findings)\.md)
- Cổng: 54909

## T-261128-73 · DEMO · Video giới thiệu tính năng MVP (chặng 1, 2, 3, 5) · wt demo-video · 3h/5M · chạy local + quay video Full HD MP4 theo kịch bản [done]
- Mục tiêu: Chạy app hiện tại ở local (profile dev, AI mock, cổng của worktree), nạp dữ liệu test/dummy (seed-reset, fixtures e2e, móc /test-hooks dev) đủ để trình diễn, rồi quay video giới thiệu các tính năng theo kịch bản người dùng (giáo viên, học sinh; chặng 1 MVP, chặng 2, 3, 5). Có thể bật cờ geo.s5.grading/ai-grading CHỈ trong phiên dev local để trình diễn E4/E6 (ghi chú trên màn "bản thử, tắt trong bản phát hành"). Output: (1) clip từng kịch bản `artifacts/GEO/evidence/DEMO-20261128/clips/NN-<tên>.mp4`; (2) video tổng `artifacts/GEO/evidence/DEMO-20261128/GEO-MVP-demo-full-1080p.mp4` (H.264, yuv420p, 1920x1080, 30fps, có màn tiêu đề + phụ đề tiếng Việt ngắn mỗi bước, nhịp xem được: dừng ~1,5–3 s mỗi màn chính); (3) `artifacts/GEO/evidence/DEMO-20261128/SCRIPT.md` tóm tắt từng kịch bản (mục tiêu, vai, các bước, tính năng thể hiện, mốc thời gian trong video tổng, dữ liệu dùng); (4) `artifacts/GEO/evidence/DEMO-20261128/probe.txt` = kết quả ffprobe của video tổng. Quay bằng Playwright (viewport + recordVideo 1920x1080, script riêng trong `app/e2e/demo/`, KHÔNG đưa vào bộ test e2e); chuyển WebM → MP4 bằng npm `ffmpeg-static` + `ffprobe-static` cài trong thư mục tạm ngoài repo (không cài hệ thống, không thêm vào package.json của dự án).
- Nguồn: app hiện tại nhánh worktree-mvp-wave0 @ HEAD; features `app/web/src/features/*`; kịch bản/US: artifacts/GEO/work/epics/*/stories_ac.md, MVP.md, PRODUCT-MASTER.md; E2E UI hiện có `app/e2e/tests/**` (cách đăng nhập + nạp dữ liệu)
- Xong khi: video tổng tồn tại, ffprobe báo h264 1920x1080, thời lượng ≥ 180 s; ≥ 6 clip kịch bản; SCRIPT.md có mốc thời gian cho mọi clip.
- Kiểm: `test -s artifacts/GEO/evidence/DEMO-20261128/GEO-MVP-demo-full-1080p.mp4 && grep -q 'h264' artifacts/GEO/evidence/DEMO-20261128/probe.txt && grep -q '1920' artifacts/GEO/evidence/DEMO-20261128/probe.txt && grep -q '1080' artifacts/GEO/evidence/DEMO-20261128/probe.txt && grep -q 'aac' artifacts/GEO/evidence/DEMO-20261128/probe.txt && test $(ls artifacts/GEO/evidence/DEMO-20261128/clips/*.mp4 | wc -l) -ge 6 && test -s artifacts/GEO/evidence/DEMO-20261128/SCRIPT.md`
- Ngoài phạm vi: sửa code sản phẩm (server/web) — chỉ thêm script quay trong app/e2e/demo/; gọi AI thật; profile prod; cổng 5432/6379/54329; dữ liệu thật của học sinh (chỉ tên giả); gửi/đăng video ra ngoài
- Model: opus · Ngân sách: 3 giờ
- Phạm vi tệp: ^(app/e2e/demo/|artifacts/GEO/evidence/DEMO-20261128/|app/\.env\.ports)
- Cổng: 54919

## T-261128-67 · GATE · Owner L7 chặng 5 (L5a phát hành; L5b/L5c chờ PO-64, bench QS5-27, lớp pilot) [wait-user]
- Mục tiêu: owner quyết phát hành L5a (S5-E1, S5-E2, S5-E3, US-S5E5-01, 04); chốt QS5-01/QS5-30 (PO-64) để bật cờ grading (L5b); duyệt bench QS5-27 trước khi bật ai-grading cho lớp (L5c)
- Nguồn: artifacts/GEO/evidence/E2E-S5/REPORT.md; s5/questions-s5.md v0.2; brief-s5 §5
- Xong khi: DECISIONS có "S5 L7 OK"
- Kiểm: `grep -q "S5 L7 OK" DECISIONS.md`
- Ngoài phạm vi: deploy
- Chờ: owner quyết
- Phụ thuộc: T-261128-66

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.4 | 2026-11-28 | Thêm 17 khối chặng 5 theo PLAN_code-s5 v1.0: T-261128-51 (khung + seed F-S5 + BUG-S23-001), 52–65 (14 task code, đợt W1–W7, 88 ngày dev), 66 tester L6, 67 gate L7 [wait-user]; sửa ghi chú phụ thuộc 'T-50' ở 18, 19 → T-261126-50 (validate báo thiếu) | agent plan-s5 |
| v0.3 | 2026-11-26 | T-17 (PLAN_code-s23 v1.0): thêm T-41 (khung chung), T-42 (dữ liệu mẫu S3); khối 18–33 thêm Kế hoạch + Phạm vi tệp, kiểm traceability --strict + check_ac_tc; dời US-S2E5-05 → 22, US-S3E5-01 → 31, US-S3E5-03/05 → 32; phụ thuộc 21 (+18), 22–25 theo đợt, 28–29 (+42) | runner-T-261126-17 |
| v0.2 | 2026-11-26 | Nạp 25 khối plan chặng 2–3 (T-05, T-08…T-34) | delivery-lead |
| v0.1 | 2026-11-26 | Tạo từ delivery-lead/templates/queue.md | |
