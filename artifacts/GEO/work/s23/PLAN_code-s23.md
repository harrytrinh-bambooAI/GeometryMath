Phiên bản: v1.0 · 2026-11-26
# PLAN_code chặng 2–3 · GEO · L4 Dev kiến trúc (T-261126-17)

**Nguồn (đọc 26/11/2026, bản mới nhất):** epic `artifacts/GEO/work/epics/S2-E1…S2-E9`, `S3-E1…S3-E6` (sau R3, gộp ebec17a, 26/11; 58 story / 746 AC / 751 TC, `check_ac_tc.py` 0 lỗi) · `PO-DECISIONS.md` PO-61…PO-66 (G1–G19, 26/11) · `DECISIONS.md` (26/11) · story map `s23/story-map-s23.md` v0.2 · `.claude/plans/STAGE-2-3-PLAN.md` v0.2 · `artifacts/GEO/work/architecture/` 01–07 (22/11) · `app/README.md`, `app/Makefile`, `app/scripts/env.sh`, `app/db/seed/README.md`, `app/e2e/scripts/traceability.ts`, `app/web/src/features/{index,types}.ts` (HEAD 3d154ab) · `.claude/briefs/epic-common.md` v1.1 → v1.2.

**Số story:** lệnh đếm ở §8 cho **58** story duy nhất (S2 35, S3 23). Brief giao việc và message gộp ebec17a ghi "59" — TẠM · tin cậy cao · nguồn: grep `stories_ac.md` + `story-map-s23.md` (cả hai 58, khớp nhau); câu hỏi Q-P1 ở §9.

## 1. Quyết định kiến trúc (L4, TẠM đến khi phiên chính duyệt)
| # | Quyết định | Lý do / nguồn |
|---|---|---|
| A1 | **Khung môn học** là module mới `subject` (chỉ `api` + registry). `subject.api.SubjectEngine` có `parse`, `solve`, `verify`, `render`, `hint`, `grade`, `concepts`; `SubjectRegistry.forKind(Problem.kind)`. Hình học là bản cài đặt trong `geometry` (`geometry/internal/plane` mới, `geometry/internal/solid` bọc engine MVP). `grade` chung cho biểu thức số nằm ở `subject/internal/grade` | US-S2E2-01; STAGES §0.3 ý 1; môn khác (chặng sau) chỉ thêm bản cài đặt |
| A2 | `Problem.kind` = enum `solid` (mọi đề MVP) \| `plane`. Thêm cột vào `problem` (module `content`), mặc định `solid`, không đổi đề cũ | US-S2E2-02 |
| A3 | `SceneSpec` 2D là hợp đồng riêng `app/docs/SCENE-CONTRACT-2D.md` (giữ `SCENE-CONTRACT.md` 3D nguyên); bộ vẽ 2D ở `app/web/src/shared/scene2d/` (thư mục mới) | US-S2E2-02/03 |
| A4 | **Một mô hình "bộ bài" (PO-66 G1):** `assignment_set` + `assignment_item` (module `assignment`), tạo ở S2-E8 với ĐỦ cột cho S3: `status` (draft, assigned, closed, archived), `due_at` (nhãn), `release_at` theo bộ, `release_at` riêng theo item, cấu hình luyện theo item (kiểu đáp số, sai số, số lượt, ảnh chụp đáp số lúc giao). Khi giao, mỗi item sinh **một hàng `assignment` MVP** (`kind = class`) để dùng lại cổng `release_at`, khóa đáp số (D-GEO-151), `assignment_progress`, bộ chặn moi đáp án. S3-E2 chỉ thêm hành vi, không đổi schema | PO-66 G1; BR-S2E8-32, BR-S3E2-07; tránh hai cổng khóa song song |
| A5 | Lượt thử bài luyện: bảng `practice_attempt` (module `tracking`), khóa chống lặp `(assignment_id, user_id, request_key)` | BR-S2E8-13/14 |
| A6 | Định danh PO-61 ở module `iam`: `app_user` thêm `user_code` (`GV-`/`HS-`), băm mật khẩu, băm mã khôi phục, cờ phải đổi mật khẩu, `privacy_ack_version`; bảng `privacy_notice` (phiên bản văn bản). `class_member.status` (pending, active, left, rejected; mặc định active) thêm ngay ở S2-E1 vì S2-E8 cần "Đang học" (BR-S2E8-19) | PO-61, PO-66 G3; US-S2E1-04 |
| A7 | Luồng đăng nhập MVP (mã giáo viên, học sinh vào lớp bằng tên + mã lớp) **giữ chạy song song** tới khi owner bỏ; test E1 giữ xanh. TẠM · vừa · epic-common "không giảm mức kiểm" | Q-P3 §9 |
| A8 | `AuditEvent` = mở rộng `audit_log` MVP (module `assignment`), thêm giá trị `audit_action` (PASSWORD_RESET, EARLY_RELEASE, RELEASE_TIME_CHANGE, …). Module khác phát `DomainEvents`, `assignment` ghi; không ghi chéo bảng | README "Luật"; US-S3E5-01 |
| A9 | `Notification` trong ứng dụng là module mới `notify` (bảng `notification`), S3-E6 nhắc việc dùng lại | US-S3E4-03, US-S3E6-03 |
| A10 | Ngăn "Của tôi / Kho chung", vai người soát nội dung: cột trên `problem` + vai mới trong `iam` (migration của task S2-E6) | US-S2E6-01/02 |
| A11 | AI: mọi lệnh gọi qua `aigateway` với `MockAiProvider` + đáp mẫu (D-GEO-047); không task nào gọi AI thật | epic-common |

## 2. Module ↔ epic (chủ sở hữu bảng mới)
| Module (gói `vn.geo.server.*`) | Epic dùng | Bảng/thay đổi mới (chủ sở hữu = module) |
|---|---|---|
| `iam` | S2-E1, S3-E1, S3-E5 (riêng tư, công tắc AI, xóa tài khoản), S2-E6 (vai người soát) | `app_user` (+cột), `privacy_notice`, `class` (+năm học, trạng thái, mã lớp mới, công tắc AI), `class_member.status`, `class_join_attempt` (G4) |
| `subject` (mới) | S2-E2 (interface), S2-E8 (`grade` tương đương), S2-E9 (biến thể) | không bảng |
| `geometry` | S2-E2 (hình phẳng), S2-E5 (`StepCheck`), S2-E9 (sinh số liệu) | không bảng (kết quả lưu ở `content`) |
| `content` | S2-E2 (`kind`, bộ đề chuẩn), S2-E3 (nháp đề), S2-E4 (import, chat nháp), S2-E5 (phiên bản lời giải, hộp duyệt), S2-E6 (ngăn, soát), S2-E9 (`problem_variant`) | `problem` (+kind, +shelf, +owner), `solution_version`, `step_check_result`, `review_inbox_item`, `problem_draft`, `import_job`, `problem_variant` |
| `assignment` | S2-E8, S3-E2, S3-E4 (Mở ngay, Đổi giờ, tổng quan), S3-E5 (`audit_log`) | `assignment_set`, `assignment_item`, `audit_log` (+giá trị enum) |
| `tracking` | S2-E8 (lượt), S3-E3 (kết quả lớp, "Cả lớp vướng"), S3-E6 (xuất kết quả) | `practice_attempt`, `answer_dispute` (nút "Em nghĩ đáp số này đúng") |
| `notify` (mới) | S3-E4, S3-E6 | `notification` |
| `aigateway` | S2-E3, S2-E4 (prompt + đáp mẫu mock), S3-E5 (chặn theo công tắc lớp) | không bảng mới |
| `platform` (`share_grant`) | S2-E9 (liên kết chỉ xem) | dùng `share_grant` sẵn có |
| web `app/web/src/features/<id>/` | id = `s2e1`…`s2e9`, `s3e1`…`s3e6` (một thư mục mỗi epic; S2-E6/E7 hai thư mục) | — |

Luật module giữ nguyên `app/README.md`: module khác chỉ dùng `<module>.api`; không đụng bảng của module khác.

## 3. Task, đợt và phụ thuộc
Hai task mới: **T-261126-41** (khung chung S2/S3, không phụ thuộc AI, chạy được ngay) và **T-261126-42** (dữ liệu mẫu chung chặng 3). ID FIX giữ theo plan §4: T-261126-35, rồi T-261126-43 trở đi.

| Đợt | Task (‖ = song song, 2 Opus) | Epic / story chính | Phụ thuộc | Cần AI thật (B7)? |
|---|---|---|---|---|
| W3b | **41** khung chung (1 Opus) | tooling: truy vết S2/S3 `--strict`, 15 feature rỗng, mục khai báo theo task trong tệp chung, `fixture-ids.d/`, `make bench-s2` | 17 | Không |
| W4 | **18** ‖ **19** | S2-E1 ‖ S2-E2 | 41, 05 (G7) | Không (19: bench-s2 chạy mock) |
| W5 | **20** ‖ **21** | S2-E5 (01–04) ‖ S2-E8 | 18, 19 | Không |
| W6 | **22** ‖ **23** | S2-E3 + US-S2E5-05 ‖ S2-E6 + S2-E7 | 20, 21 | 22: có (đọc ảnh, soạn nháp; CI vẫn mock) · 23: không |
| W7 | **24** ‖ **25** (P1) | S2-E4 ‖ S2-E9 | 22, 23 | 24: có · 25: không |
| W8 | **26** tester ‖ **42** dữ liệu mẫu S3 | L6 chặng 2 ‖ seed `F-S3-*` | 24, 25 | — |
| — | gate **27** | owner L7 chặng 2 | 26 | — |
| W9 | **28** ‖ **29** | S3-E1 + US-S3E5-02, 04 ‖ S3-E2 | 26, 42 | Không |
| W10 | **30** ‖ **31** | S3-E3 ‖ S3-E4 + US-S3E5-01 | 28, 29 | Không |
| W11 | **32** (1 Opus) | S3-E6 (P1) + US-S3E5-03, 05 (P0, làm trước) | 30, 31 | Không |
| W12 | **33** tester → gate **34** | L6 chặng 3 | 32 | — |

**Thay đổi so với queue cũ (lý do):**
- US-S2E5-05 (lời giải đã giao bất biến, áp cho bài đang giao) chuyển 20 → **22**: story phụ thuộc S2-E8 (bài luyện đang giao) mà 21 chạy song song với 20. 22 thêm phụ thuộc 21.
- S3-E5 tách khỏi 28 cho cân tải (28 cũ ≈ 116 TC so với 29 ≈ 45 TC): US-S3E5-01 (AuditEvent) → **31** (story phụ thuộc US-S3E4-02 Mở ngay/Đổi giờ); US-S3E5-03 (danh sách trường được phép, test chặn trường mới) và US-S3E5-05 (xóa tài khoản) → **32** chạy một mình sau khi mọi bảng S3 đã có, nên test chặn trường quét đủ; 32 làm phần P0 này TRƯỚC phần P1 S3-E6. 28 giữ US-S3E5-02 (thông báo riêng tư) và US-S3E5-04 (công tắc AI theo lớp) vì cùng module `iam`/lớp.
- 21 tăng ngân sách 4 → 5 giờ (S2-E8 ≈ 115 TC, lớn nhất); quá 5 giờ thì tách US-S2E8-02 (`grade`, 43 TC) thành task FIX kế.
- Lệnh kiểm truy vết đổi `make traceability ARGS=--all` (lệnh này luôn exit 0, không kiểm gì) → `make traceability ARGS="--strict --epic …"` (do 41 thêm).

**Phần làm được trước B7 (G7 vẫn hiệu lực):** 41 không phải code tính năng chặng 2 (chỉ khung kiểm thử + chỗ cắm), không gọi AI → chạy ngay sau 17. TẠM · vừa · Q-P2. Nếu owner nới G7 cho phần không dùng AI: 18, 19, 20, 21, 23, 25 và toàn bộ S3 không dùng AI thật; chỉ 22 và 24 cần B7 (khi đó bỏ "05" khỏi 18, 19 và thêm "05" vào 22). Plan giữ G7 nguyên (18, 19 chờ 05) cho đến khi owner nói khác.

**Gộp sau mỗi đợt** (giữ plan §4): merge hai nhánh vào `worktree-mvp-wave0` theo thứ tự ID nhỏ trước → `cd app && make verify && make web-build && make e2e && make e2e && make traceability ARGS="--strict --epic <mọi epic đã gộp>"` → `/code-review` (sonnet, diff đợt) → đợt kế chỉ mở khi xanh.

## 4. Worktree, cổng, khoảng số migration, seed
Đã dùng (không được trùng): PG 54329–54499, Redis 6389–6549, máy chủ 8080–8105, web 5173–5189; migration ≤ V85 (V90–V99 cấp cho T-01/T-02, chưa dùng). Khoảng migration **tăng dần theo đợt** để CSDL của nhánh gộp không gặp migration số nhỏ đến sau (Flyway không bật out-of-order). Mỗi đợt chừa 10 số cho task FIX.

| Task | Worktree / nhánh (từ `worktree-mvp-wave0`) | `app/.env.ports` (PG · Redis · máy chủ · web) | Migration | Seed `app/db/seed/e2e/` | Feature web / e2e / IT |
|---|---|---|---|---|---|
| 41 | `s23-base` / `s23-base` | 54509 · 6559 · 8110 · 5190 | V100–V109 | — | khung (§5) |
| 18 | `s2-e1` / `s2-e1` | 54519 · 6569 · 8111 · 5191 | V110–V129 | `80x_s2e1_*` | `s2e1` |
| 19 | `s2-e2` / `s2-e2` | 54529 · 6579 · 8112 · 5192 | V130–V149 | `81x_s2e2_*` | `s2e2` |
| FIX W4 | theo task FIX | 54649 · 6699 · 8124 · 5204 | V150–V159 | `88x_fix_*` | — |
| 20 | `s2-e5` / `s2-e5` | 54539 · 6589 · 8113 · 5193 | V160–V179 | `82x_s2e5_*` | `s2e5` |
| 21 | `s2-e8` / `s2-e8` | 54549 · 6599 · 8114 · 5194 | V180–V199 | `83x_s2e8_*` | `s2e8` |
| FIX W5 | | như FIX W4 | V200–V209 | `88x_fix_*` | — |
| 22 | `s2-e3` / `s2-e3` | 54559 · 6609 · 8115 · 5195 | V210–V229 | `84x_s2e3_*` | `s2e3` (+ `s2e5` cho US-S2E5-05) |
| 23 | `s2-e67` / `s2-e67` | 54569 · 6619 · 8116 · 5196 | V230–V249 | `85x_s2e67_*` | `s2e6`, `s2e7` |
| FIX W6 | | như FIX W4 | V250–V259 | `88x_fix_*` | — |
| 24 | `s2-e4` / `s2-e4` | 54579 · 6629 · 8117 · 5197 | V260–V279 | `86x_s2e4_*` | `s2e4` |
| 25 | `s2-e9` / `s2-e9` | 54589 · 6639 · 8118 · 5198 | V280–V299 | `87x_s2e9_*` | `s2e9` (+ `s2e8` cho trắc nghiệm) |
| FIX W7 | | như FIX W4 | V300–V309 | `89x_fix_*` | — |
| 42 | `s3-base` / `s3-base` | 54599 · 6649 · 8119 · 5199 | V310–V314 (chỉ khi thiếu cột) | `910_s3_core.sql` | `fixture-ids.d/s3.json` |
| 28 | `s3-e1` / `s3-e1` | 54609 · 6659 · 8120 · 5200 | V315–V334 | `91[1-9]_s3e1_*` | `s3e1`, `s3e5` |
| 29 | `s3-e2` / `s3-e2` | 54619 · 6669 · 8121 · 5201 | V335–V354 | `92x_s3e2_*` | `s3e2` |
| FIX W9 | | như FIX W4 | V355–V364 | `96x_fix_*` | — |
| 30 | `s3-e3` / `s3-e3` | 54629 · 6679 · 8122 · 5202 | V365–V384 | `93x_s3e3_*` | `s3e3` |
| 31 | `s3-e4` / `s3-e4` | 54639 · 6689 · 8123 · 5203 | V385–V404 | `94x_s3e4_*` | `s3e4` |
| FIX W10 | | như FIX W4 | V405–V414 | `97x_fix_*` | — |
| 32 | `s3-e6` / `s3-e6` | 54659 · 6709 · 8125 · 5205 | V415–V439 | `95x_s3e6_*` | `s3e6`, `s3e5` |
| 26, 33 tester | `mvp-wave0` (chỉ đọc code) | cổng hiện có của `mvp-wave0` | — | — | — |

- Tệp migration: `app/server/src/main/resources/db/migration/V<số>__<epic>_<mô_tả>.sql`, số trong khoảng của task; không sửa migration đã áp dụng (kể cả của task trước).
- Seed: `app/db/seed/e2e/<3 chữ số>_<epic>_*.sql` (glob `*.sql` chạy theo tên, 8xx sau MVP 1xx–7xx; 9xx sau `900_sample_scaffold`). Không sửa seed của task khác; cần thêm dữ liệu thì thêm tệp mới trong khoảng của mình.
- Mã cố định fixture: chặng 2 viết `F-S2-nnn` → UUID `00000000-0000-7000-8002-<nnn 12 chữ số>`; chặng 3 `F-S3-nnn` → `00000000-0000-7000-8003-<…>`. Khai báo trong `app/db/seed/fixture-ids.d/<feature>.json` (41 cho `e2e/support/fixtures.ts` đọc gộp `fixture-ids.json` + `fixture-ids.d/*.json`). Dữ liệu mẫu chung chặng 3 (`F-S3-001…053`, định nghĩa ở `epics/S3-E1/stories_ac.md` §"Dữ liệu mẫu chung") chỉ do 42 nạp; 28–32 thêm trạng thái/hàng riêng bằng tệp seed của mình.

## 5. Tệp sở hữu (để 2 agent song song không đụng nhau)
**Luật chung**
1. Mỗi task chỉ sửa tệp khớp regex "Phạm vi tệp" của nó trong `.claude/queue.md` (agent_watch `--scope` canh). Ngoài regex → dừng, ghi vào báo cáo, không tự sửa.
2. **Tệp chung có mục theo task** (41 tạo sẵn các dòng mốc `# --- T-261126-NN (<epic>) ---`, mỗi task chỉ thêm dòng ngay dưới mốc của mình → git gộp không xung đột): `app/server/quality/sql-lint.properties`, `app/server/src/test/java/vn/geo/server/quality/ArchitectureTest.java` (danh sách module), `app/server/src/main/resources/application.yml` (khóa cấu hình `geo.<feature>.*` mới), `app/Makefile` (đích mới cuối tệp), `app/README.md` (mục "Chặng 2–3").
3. **Tệp khung đã cắm sẵn, KHÔNG sửa:** `app/web/src/features/index.ts`, `types.ts` (41 thêm 15 feature rỗng), `app/web/src/app/router.tsx`, `AreaLayout.tsx`, `app/e2e/playwright.config.ts` (41 đổi `MOBILE_P0` sang quy ước tên tệp `*.p0m.ui.spec.ts` — task chỉ đặt tên tệp, không sửa cấu hình), `app/e2e/scripts/traceability.ts`, `app/scripts/*.sh` (trừ tệp mới của task).
4. **Phụ thuộc thư viện:** trong một đợt chỉ một task được sửa `app/server/pom.xml` và một task được sửa `app/web/package.json` (+ lock). W4: pom 19 (nếu cần thư viện hình học 2D), package 19. W5: pom 21, package 21. W6: pom 22, package 23 (PWA/offline Trình chiếu). W7: pom 24 (đọc docx/pdf), package 25 (xuất ảnh/PDF phía web). S3: pom 28/30/32, package 29/31/32. Task khác cần thư viện → ghi báo cáo, FIX đợt kế.
5. **Thư mục dùng chung đọc được, chỉ thêm tệp mới:** `app/web/src/shared/**`, `common/**` phía máy chủ. Sửa tệp có sẵn trong đó chỉ khi task là chủ của đợt ghi ở bảng dưới, và ghi rõ trong báo cáo.
6. `artifacts/GEO/work/epics/<E>/status.md` (dòng Dev/Test) và mục "Dev" trong `qc_findings.md` (tạo nếu chưa có) là của task sở hữu epic đó; không sửa `stories_ac.md`, `test_cases.md`, `test_design.md`.

| Task | Sở hữu (thêm/sửa) | Đọc, không sửa |
|---|---|---|
| 41 | `app/e2e/scripts/traceability.ts`, `app/e2e/support/fixtures.ts`, `app/e2e/playwright.config.ts`, `app/web/src/features/{index,types}.ts` + 15 thư mục feature rỗng `s2e1…s3e6/index.tsx`, `app/db/seed/fixture-ids.d/`, mốc mục trong 5 tệp chung (luật 2), đích `bench-s2` trong `app/Makefile`, `app/server/.../subject/` và `notify/` chỉ `package-info.java` | mọi thứ khác |
| 18 | `iam/**`, `app/web/src/shared/auth/**` (đăng nhập mã + mật khẩu), `features/s2e1/**`, `e2e/tests/s2e1/**`, `it/s2e1/**`, migration V110–V129, seed 80x | E1 MVP (giữ xanh, không xóa luồng cũ) |
| 19 | `subject/**`, `geometry/internal/plane/**`, `geometry/internal/solid/**` (chỉ bọc engine MVP sau `SubjectEngine`), `content/**/ProblemKind*`, bộ đề chuẩn `content/**/plane/**` hoặc `app/server/src/main/resources/content/plane/**`, `app/docs/SCENE-CONTRACT-2D.md`, `app/web/src/shared/scene2d/**`, `features/s2e2/**`, `scripts/bench-s2.sh`, `tests/s2e2`, `it/s2e2`, V130–V149, seed 81x | `iam` |
| 20 | `geometry/internal/stepcheck/**`, `content/**/solution/**`, `content/**/review/**`, `features/s2e5/**`, `tests/s2e5`, `it/s2e5`, V160–V179, seed 82x | `subject.api`, `assignment` |
| 21 | `assignment/**` (bộ bài, item, giao), `tracking/**/practice/**`, `subject/internal/grade/**`, `features/s2e8/**`, `tests/s2e8`, `it/s2e8`, V180–V199, seed 83x | `content`, `iam.api` |
| 22 | `content/**/intake/**`, `content/**/draft/**`, `content/**/solution/**` (US-S2E5-05: phiên bản + áp cho bài đang giao), `assignment/**/ApplyRevision*` (một lớp ứng dụng mới), `aigateway/internal/**` (prompt + đáp mẫu mock mới), `features/s2e3/**`, `features/s2e5/**`, `tests/s2e3`, `tests/s2e5`, `it/s2e3`, V210–V229, seed 84x | `assignment` khác `ApplyRevision*` |
| 23 | `content/**/library/**`, `content/**/shelf/**`, `iam/**/Role*` (vai người soát), `features/s2e6/**`, `features/s2e7/**`, `app/web/vite.config.ts` (PWA/offline), `tests/s2e6`, `tests/s2e7`, `it/s2e6`, `it/s2e7`, V230–V249, seed 85x | `content/**/intake|draft|solution` |
| 24 | `content/**/importer/**`, `content/**/authoring/**`, `content/**/dedup/**`, `aigateway/internal/**` (prompt mới), `features/s2e4/**`, `tests/s2e4`, `it/s2e4`, `app/server/pom.xml`, V260–V279, seed 86x | `content/**/variant` |
| 25 | `content/**/variant/**`, `content/**/export/**`, `geometry/internal/variant/**`, `assignment/**/Choice*` (trắc nghiệm), `features/s2e9/**`, `features/s2e8/**` (chỉ câu trắc nghiệm), `app/web/package.json`, `tests/s2e9`, `it/s2e9`, V280–V299, seed 87x | `content/**/importer` |
| 42 | `app/db/seed/e2e/910_s3_core.sql`, `fixture-ids.d/s3.json`, V310–V314 (chỉ nếu thiếu cột) | mọi code |
| 28 | `iam/**` (lớp, mã lớp, duyệt, đặt lại mật khẩu, riêng tư, công tắc AI), `aigateway/**/ClassAiSwitch*` (chặn theo lớp), `features/s3e1/**`, `features/s3e5/**`, `tests/s3e1`, `tests/s3e5`, `it/s3e1`, `it/s3e5`, V315–V334, seed 911–919 | `assignment` |
| 29 | `assignment/**` (vòng đời bộ, lịch mở theo item, xem trước, cảnh báo), `features/s3e2/**`, `tests/s3e2`, `it/s3e2`, V335–V354, seed 92x | `iam` |
| 30 | `tracking/**` (bảng lớp × bài, bằng chứng, "Cả lớp vướng ở đâu"), `features/s3e3/**`, `tests/s3e3`, `it/s3e3`, V365–V384, seed 93x | `assignment`, `notify` |
| 31 | `assignment/**/overview/**`, `assignment/**/release/**` (Mở ngay, Đổi giờ), `assignment/**/audit/**` + `audit_log`, `notify/**`, `features/s3e4/**`, `tests/s3e4`, `it/s3e4`, V385–V404, seed 94x | `tracking` |
| 32 | `features/s3e6/**`, `features/s3e5/**`, `tracking/**/export/**`, `iam/**/AccountDeletion*`, `notify/**/reminder/**`, test danh sách trường (`app/server/src/test/java/vn/geo/server/quality/AllowedFields*`), `tests/s3e6`, `tests/s3e5`, `it/s3e6`, V415–V439, seed 95x | — (chạy một mình) |

## 6. Bảng story → task (58 story; cột TC = số TC nhắc story trong test_cases.md của epic)
Mọi task code dùng 41 (khung) làm nền; task S3 dùng thêm 42 (dữ liệu mẫu chung).

| Story | Epic | Tên | TC | Task | Ghi chú |
|---|---|---|---|---|---|
| US-S2E1-01 | S2-E1 | Đăng ký bằng tên hiển thị và vai, nhận mã người dùng và mã khôi phục | 18 | T-261126-18 | văn bản riêng tư v1.0 + cờ đã đọc làm ở đây; phần đổi phiên bản ở 28 (US-S3E5-02) |
| US-S2E1-02 | S2-E1 | Đăng nhập bằng mã và mật khẩu; khóa 10 lần sai trong 15 phút theo tài … | 12 | T-261126-18 |  |
| US-S2E1-03 | S2-E1 | Giáo viên và học sinh tự khôi phục mật khẩu bằng mã khôi phục | 10 | T-261126-18 |  |
| US-S2E1-04 | S2-E1 | Dữ liệu: chuyển tài khoản mẫu MVP sang mô hình mã người dùng | 6 | T-261126-18 | thêm `class_member.status` (A6) |
| US-S3E1-01 | S3-E1 | Giáo viên tạo lớp (tên, cấp, năm học), sinh mã lớp và trang in | 12 | T-261126-28 |  |
| US-S3E1-02 | S3-E1 | Học sinh nhập mã lớp, trạng thái chờ duyệt; mã sai hoặc đã khóa | 14 | T-261126-28 |  |
| US-S3E1-03 | S3-E1 | Giáo viên duyệt danh sách chờ; nhiều lớp; ClassMembership (chờ, đang h… | 10 | T-261126-28 |  |
| US-S3E1-04 | S3-E1 | Giáo viên đặt lại mật khẩu cho học sinh lớp mình | 12 | T-261126-28 |  |
| US-S3E1-05 | S3-E1 | Quyền theo quan hệ giáo viên, lớp, học sinh (xuyên suốt; nhận vào chặn… | 8 | T-261126-28 | lõi ở 28; 29–32 áp vào API của mình |
| US-S2E2-01 | S2-E2 | Khung môn học: `parse/solve/verify/render/hint` cộng `grade`, `concepts` | 7 | T-261126-19 | `SubjectEngine` + `grade` tối thiểu; `grade` tương đương ở 21 |
| US-S2E2-02 | S2-E2 | Dựng hình phẳng từ ràng buộc; `Problem.kind`; `SceneSpec` 2D | 14 | T-261126-19 |  |
| US-S2E2-03 | S2-E2 | Làm bài hình phẳng trên iPad và iPhone | 9 | T-261126-19 |  |
| US-S2E2-04 | S2-E2 | Dữ liệu: bộ đề chuẩn hình phẳng và đáp số mẫu | 7 | T-261126-19 |  |
| US-S2E2-05 | S2-E2 | Đo `make bench-s2` (mock): ≥ 95 % thỏa ràng buộc, ≥ 90 % đáp số, 0 sai… | 8 | T-261126-19 |  |
| US-S3E2-01 | S3-E2 | Soạn bộ bài: chọn đề, thứ tự, hạn làm (chỉ là nhãn), xem trước như học… | 15 | T-261126-29 |  |
| US-S3E2-02 | S3-E2 | Lịch mở lời giải theo bộ hoặc từng bài; cảnh báo giờ mở sớm hơn hạn | 14 | T-261126-29 |  |
| US-S3E2-03 | S3-E2 | Bài của em theo bộ; liệt kê đề "Cần xem lại" trước khi giao | 9 | T-261126-29 |  |
| US-S3E2-04 | S3-E2 | Đóng bộ và lưu trữ bộ (vòng đời Đã đóng, Lưu trữ) | 7 | T-261126-29 |  |
| US-S2E3-01 | S2-E3 | Thêm đề bằng dán chữ, chép lại để sửa | 9 | T-261126-22 |  |
| US-S2E3-02 | S2-E3 | Thêm đề bằng ảnh; ô chưa chắc tô để giáo viên sửa | 11 | T-261126-22 |  |
| US-S2E3-03 | S2-E3 | Bốn bước đọc–dựng–giải–kiểm với màn chờ từng bước | 9 | T-261126-22 |  |
| US-S2E3-04 | S2-E3 | Ngoại lệ: engine không dựng thì lưu "Cần xem lại"; lệch đáp số thì chặ… | 10 | T-261126-22 |  |
| US-S3E3-01 | S3-E3 | Bảng lớp × bài, ô là trạng thái, không cột tổng, không sắp "tốt nhất" | 21 | T-261126-30 |  |
| US-S3E3-02 | S3-E3 | Bấm một ô xem gợi ý, câu hỏi và bằng chứng; học sinh đã rời đánh dấu | 13 | T-261126-30 |  |
| US-S3E3-03 | S3-E3 | "Cả lớp vướng ở đâu": bước nhiều em dừng nhất; dưới ngưỡng 5 bài chỉ h… | 24 | T-261126-30 |  |
| US-S2E4-01 | S2-E4 | Chat chỉnh nháp đề, AI hỏi lại và nói rõ trước/sau | 11 | T-261126-24 |  |
| US-S2E4-02 | S2-E4 | Import docx/pdf, tách từng bài tập | 9 | T-261126-24 |  |
| US-S2E4-03 | S2-E4 | Đối chiếu trùng với kho | 7 | T-261126-24 |  |
| US-S3E4-01 | S3-E4 | Tổng quan giáo viên web + điện thoại: bài đang giao, lời giải đang khó… | 13 | T-261126-31 |  |
| US-S3E4-02 | S3-E4 | Theo dõi nhanh trên điện thoại: em chưa làm, Mở ngay, Đổi giờ | 14 | T-261126-31 |  |
| US-S3E4-03 | S3-E4 | Notification trong ứng dụng cho học sinh (chưa đọc, đã đọc) | 14 | T-261126-31 |  |
| US-S2E5-01 | S2-E5 | Sửa lời giải từng bước, mỗi lần lưu thành phiên bản mới | 15 | T-261126-20 |  |
| US-S2E5-02 | S2-E5 | `StepCheck`: kiểm số từng bước, bước sai tô đỏ kèm lý do | 13 | T-261126-20 |  |
| US-S2E5-03 | S2-E5 | Nhãn hai trục và dòng "Máy đã kiểm tra phần tính toán"; chỉ engine gắn… | 11 | T-261126-20 |  |
| US-S2E5-04 | S2-E5 | Hộp duyệt chung: nhận, sửa, bỏ | 15 | T-261126-20 |  |
| US-S2E5-05 | S2-E5 | Lời giải đã giao bất biến; sửa tạo phiên bản mới và chọn có áp cho bài… | 13 | T-261126-22 | chuyển từ 20: cần bài luyện đang giao (21) |
| US-S3E5-01 | S3-E5 | AuditEvent cho mở sớm, đổi giờ, đặt lại mật khẩu, kèm người thực hiện | 15 | T-261126-31 | chuyển từ 28: cần Mở ngay/Đổi giờ (US-S3E4-02); 28 phát DomainEvent đặt lại mật khẩu |
| US-S3E5-02 | S3-E5 | Thông báo riêng tư, cờ "đã đọc" cùng phiên bản văn bản | 11 | T-261126-28 |  |
| US-S3E5-03 | S3-E5 | Danh sách trường dữ liệu được phép thu; test chặn trường mới | 13 | T-261126-32 | chuyển từ 28: chạy khi mọi bảng S3 đã có |
| US-S3E5-04 | S3-E5 | Công tắc AI phía học sinh theo lớp; tắt thì luồng học cơ bản vẫn chạy | 13 | T-261126-28 |  |
| US-S3E5-05 | S3-E5 | Xóa tài khoản và chính sách lưu/xóa dữ liệu bài làm | 8 | T-261126-32 | chuyển từ 28 (cân tải); P0, làm trước S3-E6 |
| US-S2E6-01 | S2-E6 | Kho đề có ngăn Của tôi / Kho chung và lọc loại hình | 12 | T-261126-23 |  |
| US-S2E6-02 | S2-E6 | Người soát nội dung duyệt đề vào Kho chung | 12 | T-261126-23 |  |
| US-S2E6-03 | S2-E6 | Đề Của tôi chỉ chủ sở hữu thấy; giới hạn tần suất thêm đề | 13 | T-261126-23 |  |
| US-S3E6-01 | S3-E6 | Chữa bài từ kết quả sang Trình chiếu | 10 | T-261126-32 |  |
| US-S3E6-02 | S3-E6 | Xuất kết quả lớp; lưu trữ lớp cuối năm | 16 | T-261126-32 |  |
| US-S3E6-03 | S3-E6 | Nhắc việc trong ứng dụng: bài mới, sắp hết hạn, lời giải đã mở | 14 | T-261126-32 |  |
| US-S2E7-01 | S2-E7 | Trình chiếu toàn màn hình, hiện từng bước, nút che lời giải | 14 | T-261126-23 |  |
| US-S2E7-02 | S2-E7 | Chạy được khi mất mạng; hỏi đáp AI tạm tắt kèm thông báo | 9 | T-261126-23 |  |
| US-S2E7-03 | S2-E7 | Hỏi lại "Lớp sẽ thấy lời giải trên màn chiếu" khi lời giải đang khóa | 10 | T-261126-23 |  |
| US-S2E8-01 | S2-E8 | Bàn phím toán (căn, phân số, π) trên iPad và iPhone | 15 | T-261126-21 |  |
| US-S2E8-02 | S2-E8 | Chấm đáp số dạng tương đương bằng `grade`; sai cú pháp không trừ lượt | 41 | T-261126-21 |  |
| US-S2E8-03 | S2-E8 | Làm bài luyện: lượt thử, gợi ý mức 1, "xem lại cùng thầy cô" khi hết l… | 21 | T-261126-21 |  |
| US-S2E8-04 | S2-E8 | Kết quả bài luyện; không hiện số tổng; đáp số khóa tới giờ mở | 14 | T-261126-21 |  |
| US-S2E8-05 | S2-E8 | Soạn bài luyện; nút "Em nghĩ đáp số này đúng" gửi về giáo viên | 25 | T-261126-21 | tạo mô hình bộ bài đủ cột cho S3 (A4) |
| US-S2E9-01 | S2-E9 | Biến thể đề `ProblemVariant`, đáp số tính lại | 13 | T-261126-25 |  |
| US-S2E9-02 | S2-E9 | Xuất ảnh/PDF và liên kết chỉ xem | 15 | T-261126-25 |  |
| US-S2E9-03 | S2-E9 | Câu trắc nghiệm trong bài luyện | 16 | T-261126-25 | sửa `features/s2e8` phần trắc nghiệm |

**Tải theo task (TC theo bảng trên):** 18 ≈ 46 · 19 ≈ 45 · 20 ≈ 54 · 21 ≈ 118 · 22 ≈ 52 · 23 ≈ 70 · 24 ≈ 27 · 25 ≈ 44 · 28 ≈ 81 · 29 ≈ 45 · 30 ≈ 58 · 31 ≈ 56 · 32 ≈ 61 (gần đúng: một TC có thể nhắc hai story).

## 7. Lệnh kiểm theo task (gốc repo `worktree-mvp-wave0`, exit 0)
Mẫu chung (`<wt>` = worktree; `<E>` = các epic S2/S3 đã gộp CỘNG DỒN + epic của task; `MVP` = E1…E7, bí danh do 41 thêm):
`cd .claude/worktrees/<wt>/app && make verify && make web-build && make e2e && make e2e && make traceability ARGS="--strict --epic MVP,<E>"`, cộng `python3 artifacts/GEO/work/tools/check_ac_tc.py <epic của task>` (spec không bị sửa).
Thêm: 19, 20, 22, 25 chạy `make bench-s2`; 19–25 chạy `make bench-mvp` (mock, hồi quy MVP). Lệnh đầy đủ từng task ở khối "Kiểm" của `.claude/queue.md`.

## 8. Kiểm phủ story (tự kiểm, chạy ở gốc worktree)
Lệnh (một dòng, exit 1 nếu có story thiếu task):
`python3 -c "import re,glob;ids={i for f in glob.glob('artifacts/GEO/work/epics/S[23]-E*/stories_ac.md') for i in re.findall(r'^## (US-S[23]E\d+-\d+)',open(f).read(),re.M)};m={s for s,t in re.findall(r'^\| (US-S[23]E\d+-\d+) \|.*\| (T-261126-\d+) \|',open('artifacts/GEO/work/s23/PLAN_code-s23.md').read(),re.M)};print(len(ids),'story trong epic;',len(m),'story có task; thiếu',sorted(ids-m),'; thừa',sorted(m-ids));exit(1 if ids-m else 0)"`

Kết quả (26/11/2026, chạy sau khi viết bảng §6): `58 story trong epic; 58 story có task; thiếu [] ; thừa []`, exit 0.

## 9. Rủi ro và câu hỏi mở
**Rủi ro**
- R1 Xung đột gộp ở tệp chung: chặn bằng §5 (mốc theo task, tệp khung không sửa, một chủ `pom.xml`/`package.json` mỗi đợt); phiên chính gộp ID nhỏ trước, xung đột còn lại → FIX đầu đợt kế.
- R2 21 (S2-E8 ≈ 118 TC) vượt 5 giờ: tách US-S2E8-02 (`grade`) thành FIX; 22 và 25 chỉ cần `AssignmentSet` + giao bài.
- R3 Test E1 MVP đỏ khi đổi đăng nhập (US-S2E1-04): A7 giữ luồng cũ; 18 không được tắt test E1.
- R4 Truy vết hiện không kiểm gì (`traceability.ts` chỉ đọc thư mục `E\d+`, mã `TC-GEO-`, luôn exit 0): 41 sửa trước mọi task code; nếu MVP đang thiếu test cho TC bắt buộc thì 41 ghi danh sách và dùng `--strict` chỉ cho S2/S3 (`MVP` chỉ báo, không chặn) — TẠM · vừa.
- R5 Dữ liệu mẫu chặng 2 không có mã `F-S2-*` trong spec (chỉ chặng 3 có `F-S3-*`): mỗi task S2 tự đặt mã `F-S2-<task><nn>` trong seed + `fixture-ids.d/`, ghi bảng mã vào `status.md` của epic.
- R6 Flyway: khoảng số tăng theo đợt (§4); task FIX dùng khoảng FIX của đợt nó chạy, không dùng khoảng của task khác.

**Câu hỏi (gom cho phiên chính / owner)**
- Q-P1 Số story: spec có 58 story (grep cả `stories_ac.md` và story map), brief và message ebec17a ghi 59. Plan dùng 58 — xin phiên chính xác nhận hoặc chỉ ra story thứ 59.
- Q-P2 G7 (code chặng 2 chờ T-05): plan cho 41 (khung kiểm thử, không phải tính năng, không gọi AI) chạy ngay. Nếu owner nới G7 cho phần không dùng AI thì 18–21, 23, 25 chạy trước B7 và chỉ 22, 24 chờ 05 (§3).
- Q-P3 Bỏ luồng đăng nhập MVP (mã giáo viên, học sinh vào lớp bằng tên + mã lớp) lúc nào? Plan giữ song song (A7); bỏ thì cần CR đánh dấu TC E1 bị thay.
- Q-P4 42 chạy song song 26 (tester chặng 2) trước gate 27 — chỉ nạp seed, không code tính năng; đồng ý?

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.0 | 2026-11-26 | Bản đầu: kiến trúc A1–A11, module ↔ epic, 2 task mới (41 khung, 42 dữ liệu S3), dời US-S2E5-05 → 22, US-S3E5-01 → 31, US-S3E5-03/05 → 32; cổng, migration, seed, tệp sở hữu; bảng 58 story → task (kiểm phủ 0 thiếu) | runner-T-261126-17 (opus) |
