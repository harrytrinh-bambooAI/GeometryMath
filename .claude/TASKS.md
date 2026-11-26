Phiên bản: v1.1 · 2026-11-26 · GEO — sổ task DUY NHẤT (inbox = ghi thô; nhận làm thì vào đây)
Giao diện cố định (giữ khi đổi nguồn ID sang PMS): ID · Việc · Nguồn (giờ raw) · Xong khi · Ước tính · Trạng thái · Output
Trạng thái: Todo · Doing · Wait-User · Blocked (cần gì) · Done <giờ> · Dropped (lý do). Giờ lấy từ `date`.
Task giao agent con có khối `.claude/queue.md` CÙNG ID; map trạng thái ở delivery-lead `framework.md` §3.

## Đang mở — Claude tự làm
| ID | Việc | Nguồn (raw) | Xong khi (lệnh kiểm) | Ước tính | Trạng thái | Output |
|---|---|---|---|---|---|---|
| T-261126-01 | AI thật: AnthropicAiProvider (Claude, mặc định claude-sonnet-5-5) + ProfileGuard cấm mock ở prod + bộ đo MVP (`make bench-mvp`: 4 chỉ số bắt buộc + thời gian tới hình + chi phí AI/bài) | PO-60 | verify + web-build + e2e 2 lượt xanh; `make bench-mvp` (mock) in báo cáo | 3–4h, Opus | Done | nhánh mvp-ai → afda3dd |
| T-261126-02 | Kho ≥ 40 đề máy kiểm chứng (nhãn AI soạn) + kiểm P0 khổ điện thoại (Playwright mobile) | PO-60 | verify + web-build + e2e 2 lượt xanh (gồm dự án mobile); đếm đề verified ≥ 40 | 3–4h, Opus | Done | nhánh mvp-content → 77850f4 |
| T-261126-03 | Gộp 01+02 vào worktree-mvp-wave0, chạy bench-mvp trên 40 đề (mock) | PO-60 | lệnh kiểm profile §2 xanh sau gộp | 1h | Done | 6b62557 |
| T-261126-04 | Soát độc lập diff 01+02 (Sonnet) + sửa gom | §7b | 0 blocker/major mở | 1–2h | Done | 0/0/3 minor → T-261126-07 |
| T-261126-05 | Đo thật 4 chỉ số với Claude API, báo cáo `artifacts/GEO/evidence/MVP-METRICS-<ngày>/` | PO-60 | báo cáo provider anthropic + "Kết luận chỉ số bắt buộc: **ĐẠT**" (= cổng B7 mở code chặng 2) | 30' | Blocked (owner chạy lệnh đo: quyền đọc khóa bị chặn) | |
| T-261126-06 | Kiểm cuối MVP + cập nhật PR #1 + PROGRESS | §9 | profile §2 + traceability xanh | 1h | Todo | |
| T-261126-07 | FIX5: 3 minor soát T-04 (chi phí model thiếu giá, chi phí theo model thực trả, gói đề không ghi đè đề đã có) | inbox FIX5 | `make verify` + `make bench-mvp` (mock) exit 0; test mới từng mục | 45', Sonnet | Done 14:36 | merge FIX5 (verify 201+137 IT, bench mock exit 0) |
| T-261126-08 | S23 · Brief + BRD delta + story map chặng 2–3 | STAGE-2-3-PLAN v0.2 | `test "$(grep -cE '^[\|] (Mục tiêu\|Nguồn\|Xong khi\|Ngoài phạm vi) [\|] [^<]' artifacts/GEO/work/s23/brief-s23.md)" -eq 4 && test "$(grep -cE '^[\|] S[23]-E[0-9]' artifacts/GEO/work/epic_board.md)" -ge 15` | 2h/3M, Sonnet | Done 14:40 | 0b9ee97 (55 story, PO-62…65 TẠM) |
| T-261126-10 | S23 · Bản mẫu màn chặng 2–3 | STAGE-2-3-PLAN v0.2 | `test "$(ls Design/s23/*.png \| wc -l)" -ge 17 && test -s Design/s23/DQ.md` | 4h/5M, Sonnet | Done 14:45 | 887b97e (24 màn, DQ 28 câu TẠM) |
| T-261126-12 | S2 · Spec S2 (E1–E9) | STAGE-2-3-PLAN v0.2 | `L=~/.claude/skills/synced/0891cb95-9cc4-4f0d-9b03-4f74eb7fe957_32199e3b-7799-4318-8c09-109bf13815a9/po-ba-qc/tools/lint; for e in S2-E1 S2-E2 S2-E3 S2-E4 S2-E5 S2-E6 S2-E7 S2-E8 S2-E9; do python3 $L/lint_stories.py artifacts/GEO/work/epics/$e/stories_ac.md && python3 $L/lint_tests.py artifacts/GEO/work/epics/$e/test_cases.md \|\| exit 1; done` | 5h/7M, Sonnet | Done 15:08 | 9493739 (35 story / 442 AC / 447 TC — commit message ghi nhầm 447/452) |
| T-261126-14 | S3 · Spec S3 (E1–E6) | STAGE-2-3-PLAN v0.2 | `L=~/.claude/skills/synced/0891cb95-9cc4-4f0d-9b03-4f74eb7fe957_32199e3b-7799-4318-8c09-109bf13815a9/po-ba-qc/tools/lint; for e in S3-E1 S3-E2 S3-E3 S3-E4 S3-E5 S3-E6; do python3 $L/lint_stories.py artifacts/GEO/work/epics/$e/stories_ac.md && python3 $L/lint_tests.py artifacts/GEO/work/epics/$e/test_cases.md \|\| exit 1; done` | 3h/4M, Sonnet | Done 15:07 | 1d16746 (21 story / 280 AC / 280 TC) |
| T-261126-15 | S23 · RV + AC→TC + domain profile tester | STAGE-2-3-PLAN v0.2 | `python3 artifacts/GEO/work/tools/check_ac_tc.py S2-E1 S2-E2 S2-E3 S2-E4 S2-E5 S2-E6 S2-E7 S2-E8 S2-E9 S3-E1 S3-E2 S3-E3 S3-E4 S3-E5 S3-E6 && python3 artifacts/GEO/work/tools/check_ac_tc.py && grep -q localhost ~/.claude/skills/synced/24fb9ac6-fc76-408c-8209-fb31ed5866d5_c730a5c3-d83a-42d5-b500-bea3cf03a2a1/tester/domains/geo/scope.json` | 2h/2.6M, Sonnet | Done | gộp 1816344 (723 AC/728 TC 0 lỗi; RV mở 0 blocker/0 major; tester geo confirmed:false) |
| T-261126-16 | GATE · Owner họp R3 + quyết định §5 + duyệt scope tester | STAGE-2-3-PLAN v0.2 | `grep -q "S23 R3 OK" DECISIONS.md` | , Sonnet | Wait-User | |
| T-261126-17 | S23 · PLAN_code chặng 2–3 + epic-common v1.2 | STAGE-2-3-PLAN v0.2 | `python3 ~/.claude/skills/delivery-lead/scripts/queue.py validate && test -s artifacts/GEO/work/s23/PLAN_code-s23.md` | 2.5h/5M, Opus | Todo | |
| T-261126-18 | S2-E1 · Định danh tối giản PO-61 | STAGE-2-3-PLAN v0.2 | `cd .claude/worktrees/s2-e1/app && make verify && make web-build && make e2e && make e2e && make traceability ARGS=--all` | 3h/8M, Opus | Todo | |
| T-261126-19 | S2-E2 · Khung môn học + hình phẳng + bench-s2 | STAGE-2-3-PLAN v0.2 | `cd .claude/worktrees/s2-e2/app && make verify && make web-build && make e2e && make e2e && make traceability ARGS=--all && make bench-s2` | 5h/14M, Opus | Todo | |
| T-261126-20 | S2-E5 · Duyệt lời giải có phiên bản + StepCheck | STAGE-2-3-PLAN v0.2 | `cd .claude/worktrees/s2-e5/app && make verify && make web-build && make e2e && make e2e && make traceability ARGS=--all` | 4h/10M, Opus | Todo | |
| T-261126-21 | S2-E8 · Bài luyện điền đáp số | STAGE-2-3-PLAN v0.2 | `cd .claude/worktrees/s2-e8/app && make verify && make web-build && make e2e && make e2e && make traceability ARGS=--all` | 4h/10M, Opus | Todo | |
| T-261126-22 | S2-E3 · Thêm đề có AI soạn nháp (chữ + ảnh) | STAGE-2-3-PLAN v0.2 | `cd .claude/worktrees/s2-e3/app && make verify && make web-build && make e2e && make e2e && make traceability ARGS=--all` | 4h/10M, Opus | Todo | |
| T-261126-23 | S2-E6+E7 · Kho đề mở rộng + Trình chiếu | STAGE-2-3-PLAN v0.2 | `cd .claude/worktrees/s2-e67/app && make verify && make web-build && make e2e && make e2e && make traceability ARGS=--all` | 3.5h/9M, Opus | Todo | |
| T-261126-24 | S2-E4 · Chat chỉnh nháp + import file | STAGE-2-3-PLAN v0.2 | `cd .claude/worktrees/s2-e4/app && make verify && make web-build && make e2e && make e2e && make traceability ARGS=--all` | 4h/10M, Opus | Todo | |
| T-261126-25 | S2-E9 · Biến thể đề + xuất PDF + liên kết chỉ xem | STAGE-2-3-PLAN v0.2 | `cd .claude/worktrees/s2-e9/app && make verify && make web-build && make e2e && make e2e && make traceability ARGS=--all` | 4h/10M, Opus | Todo | |
| T-261126-26 | S2 · Kiểm thử chặng 2 | STAGE-2-3-PLAN v0.2 | `cd app && make e2e && make e2e && make traceability ARGS=--all && make bench-s2 && test -s ../artifacts/GEO/evidence/S2-L6/REPORT.md` | 2.5h/3M, Sonnet | Todo | |
| T-261126-27 | GATE · Owner L7 chặng 2 | STAGE-2-3-PLAN v0.2 | `grep -q "S2 L7 OK" DECISIONS.md` | , Sonnet | Wait-User | |
| T-261126-28 | S3-E1+E5 · Lớp, mã lớp, duyệt vào lớp, đặt lại mật khẩu + an toàn tối thiểu | STAGE-2-3-PLAN v0.2 | `cd .claude/worktrees/s3-e1/app && make verify && make web-build && make e2e && make e2e && make traceability ARGS=--all` | 4h/10M, Opus | Todo | |
| T-261126-29 | S3-E2 · Bộ bài | STAGE-2-3-PLAN v0.2 | `cd .claude/worktrees/s3-e2/app && make verify && make web-build && make e2e && make e2e && make traceability ARGS=--all` | 4h/10M, Opus | Todo | |
| T-261126-30 | S3-E3 · Kết quả lớp + Cả lớp vướng ở đâu | STAGE-2-3-PLAN v0.2 | `cd .claude/worktrees/s3-e3/app && make verify && make web-build && make e2e && make e2e && make traceability ARGS=--all` | 4h/10M, Opus | Todo | |
| T-261126-31 | S3-E4 · Tổng quan giáo viên + theo dõi nhanh + thông báo trong ứng dụng | STAGE-2-3-PLAN v0.2 | `cd .claude/worktrees/s3-e4/app && make verify && make web-build && make e2e && make e2e && make traceability ARGS=--all` | 3h/8M, Opus | Todo | |
| T-261126-32 | S3-E6 · Chữa bài, xuất kết quả, lưu trữ lớp, nhắc việc | STAGE-2-3-PLAN v0.2 | `cd .claude/worktrees/s3-e6/app && make verify && make web-build && make e2e && make e2e && make traceability ARGS=--all` | 3h/8M, Opus | Todo | |
| T-261126-33 | S3 · Kiểm thử chặng 3 | STAGE-2-3-PLAN v0.2 | `cd app && make e2e && make e2e && make traceability ARGS=--all && test -s ../artifacts/GEO/evidence/S3-L6/REPORT.md` | 2.5h/3M, Sonnet | Todo | |
| T-261126-34 | GATE · Owner L7 chặng 3 + cổng "giáo viên thật dùng lặp lại" | STAGE-2-3-PLAN v0.2 | `grep -q "S3 L7 OK" DECISIONS.md` | , Sonnet | Wait-User | |
| T-261126-36 | DESIGN · Designer Dom: rà + hoàn thiện màn chặng 2–3 (8 màn thiếu, trả lời Q_Dom) | lệnh owner 26/11 | ≥ 38 ảnh + REVIEW-Dom + Q_Dom trả lời + DQ-29 | ~300–500k token, Sonnet | Done | gộp 2be47fa (45 ảnh, Q_Dom v0.3: 13 CẦN OWNER, DQ-29…43) |

## Chờ owner (chi tiết ở NEEDS.md)
| ID | Việc | Cần gì | Mở khoá |
|---|---|---|---|

## Đã xong / bỏ (chuyển xuống đây, giữ 7 ngày)
| ID | Việc | Trạng thái | Output |
|---|---|---|---|

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.1 | 2026-11-26 | Thêm T-08…T-34 từ STAGE-2-3-PLAN v0.2 | delivery-lead |
| v0.1 | 2026-11-26 | Tạo từ delivery-lead/templates/TASKS.md | |
