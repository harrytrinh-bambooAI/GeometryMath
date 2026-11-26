# Brief chung cho agent epic (Đợt 1+, chặng 2–3) · v1.2 · 26/11/2026

> **Tránh bị cắt phản hồi (bài học E3, v1.1):** mỗi lần Write/Edit không quá ~250 dòng; tệp lớn thì tạo khung rồi Edit thêm từng phần; tách component/spec thành nhiều tệp; không in lại nội dung tệp dài vào phản hồi; đọc tệp có mục tiêu (grep, offset/limit).

Áp cho mọi agent code epic của GEO: MVP E2…E7 (v1.1) và chặng 2–3 S2-E1…S2-E9, S3-E1…S3-E6 (task T-261126-18…32, 41, 42, 50). Phần riêng (epic, worktree, cổng, khoảng migration, tệp sở hữu, phụ thuộc, ngân sách) nằm trong khối task của `.claude/queue.md` và `artifacts/GEO/work/s23/PLAN_code-s23.md`.

## Nguồn sự thật
**Chặng 2–3 (26/11/2026, sau R3, gộp ebec17a):**
- `artifacts/GEO/work/epics/<S2-En|S3-En>/stories_ac.md` (US, BR, AC — nguồn chính), `test_cases.md` (cột "Tự động hóa": E2E-UI / E2E-API / IT / UT / Manual), `test_design.md`, `questions.md`. 58 story / 746 AC / 751 TC.
- `artifacts/GEO/work/s23/PLAN_code-s23.md` (kiến trúc A1–A11, module ↔ epic, cổng, migration, seed, tệp sở hữu, bảng story → task) — **đọc §1, §2, §4, §5 và dòng của task mình ở §6 trước khi code.**
- `PO-DECISIONS.md` PO-61…PO-66 (PO-66 G1–G19 Final). Dòng QA "Open" trong `questions.md` không chặn code: làm theo mặc định ghi cạnh nó, ghi vào báo cáo.
- Dữ liệu mẫu chung chặng 3 `F-S3-001…053`: `epics/S3-E1/stories_ac.md` mục "Dữ liệu mẫu chung" (task 42 nạp). Thông điệp `MSG-S2-…`/`MSG-S3-…`: bảng "Thông điệp" trong `stories_ac.md` của epic.
- Giao diện (DECISIONS 26/11, lệnh owner): dựng màn theo HTML bản mẫu `Design/prototype/s23-screens-c.html` (màn trong `s23.js`, `s23b.js`, `s23c.js`, `s23d.js`; kịch bản `s23k.js` cho luồng) — bố cục, nội dung chữ, trạng thái; ảnh `Design/s23/*.png`; mặc định `Design/s23/DQ.md`. Màu/bo góc/font/mật độ CHỈ qua token theme (T-261126-50, style C "Giấy kẻ ô" mặc định + font Bad Script, `Design/styles/c/`; nội dung toán dùng `var(--font-math)` hoặc thuộc tính `data-math`), không mã màu cứng; dùng component `app/web/src/shared/ui`. Chữ phụ ≥ 14 px (PO-66 G17).

**Chung với MVP (22/11/2026):** kiến trúc `artifacts/GEO/work/architecture/` (01_adr, 02_multi_tenant, 03_schema.sql, 04_query_catalogue, 06_nfr); quy ước mã `app/README.md` (luật module, truy vấn, thêm migration/module), `app/docs/STACK.md`, `app/docs/CONFIG.md` (§8 profile: dev/e2e bật profile `dev` tường minh), `app/docs/SCENE-CONTRACT.md` (3D); `app/db/seed/README.md`. Mẫu để noi theo: code E1…E7 (`app/server/.../iam`, `assignment`, `content`…, `app/web/src/features/e1…e7/`, `app/e2e/tests/e1…e7/`, IT ở `app/server/src/test/java/vn/geo/server/it/e1…e7/`).

## Cách làm
1. Tạo `app/.env.ports` đúng cổng được giao (PLAN §4); `cd app && make up`. Không dùng/dừng cổng của checkout hay agent khác.
2. Thứ tự: migration (số trong **khoảng riêng** của task, PLAN §4; không sửa migration đã có) + domain + API + IT → giao diện `app/web/src/features/<id>/` (id `s2e1`…`s3e6`, đã cắm sẵn bởi T-41 — không sửa `features/index.ts`, `types.ts`, `router.tsx`) → E2E `app/e2e/tests/<id>/` theo TC.
3. Mỗi test gắn mã TC: Playwright `@TC-S2E1-001` (hoặc `@TC-GEO-xxxx` cho MVP) trong tiêu đề; JUnit mã TC trong `@DisplayName("TC-S2E1-001 …")`; test đơn vị web (vitest) ghi mã TC trong tên `it(...)`. Spec P0 chạy trên điện thoại: đặt tên `*.p0m.ui.spec.ts` (không sửa `playwright.config.ts`).
4. Seed: tệp riêng `app/db/seed/e2e/<3 chữ số trong khoảng của task>_<id>_*.sql`; mã cố định ở `app/db/seed/fixture-ids.d/<id>.json` (`F-S2-nnn` → `00000000-0000-7000-8002-<12 chữ số>`, `F-S3-nnn` → `…-8003-…`). Không sửa seed hay `fixture-ids` của task khác.
5. **Tệp sở hữu:** chỉ sửa tệp khớp "Phạm vi tệp" của task (regex trong queue; agent_watch canh). Tệp chung có mốc `# --- T-261126-NN (…) ---` (`sql-lint.properties`, `ArchitectureTest.java`, `application.yml`, `Makefile`, `app/README.md`): chỉ thêm dòng ngay dưới mốc của mình. `app/server/pom.xml`, `app/web/package.json`: chỉ task được ghi chủ ở PLAN §5 luật 4. Cần sửa ngoài phạm vi → không sửa, ghi vào báo cáo (phiên chính mở FIX).
6. Module: theo PLAN §2 (A1 `subject` khung môn học; A4 một mô hình bộ bài `assignment_set/item`, item giao sinh hàng `assignment` MVP; A6 định danh PO-61 trong `iam`; A8 `audit_log`; A9 `notify`). Module khác chỉ dùng `<module>.api`; giao tiếp chéo qua dịch vụ ứng dụng hoặc `DomainEvents`.
7. Phần nhỏ của epic khác cần để AC chạy được: làm tối thiểu TRONG phạm vi tệp của mình, ghi rõ trong báo cáo.
8. AI: chỉ qua `aigateway` với `MockAiProvider` + đáp mẫu (D-GEO-047). Không gọi AI thật, không đặt `GEO_AI_API_KEY`.

## Xong khi (lệnh kiểm được bằng máy, chạy trong `app/` của worktree)
- `make verify` xanh (đơn vị + IT + ArchUnit + quét SQL).
- `make web-build` xanh (typecheck, lint, test, build).
- E2E theo mức ảnh hưởng (v1.5, DECISIONS 26/11, CLAUDE.md §5): KHÔNG chạy trọn bộ. Trong lúc làm chỉ chạy spec của epic mình. Cuối task: `git merge worktree-mvp-wave0` → đóng băng mã → `make e2e-affected` MỘT lượt (in mức L0/L1/L2 + danh sách spec; dán vào báo cáo). Trượt chập chờn ngoài epic: chạy lại RIÊNG spec đó, không chạy lại cả lệnh. Sau lượt cuối không sửa tệp mã (phiên chính kiểm summary.json + mtime). Trọn bộ chỉ khi xong feature (phiên chính chạy) và ở gate L6. Khóa e2e toàn máy: chờ khóa là bình thường.
- `make traceability ARGS="--strict --epic MVP,<các epic S2/S3 đã gộp>,<epic của task>"` exit 0: 0 TC tự động hóa (E2E-UI / E2E-API / IT / UT) thiếu test, 0 trượt. (`--strict`, bí danh `MVP` và đọc thư mục `S2-E*`/`S3-E*` do T-41 thêm; trước khi có T-41 lệnh cũ không kiểm gì.)
- `python3 ../artifacts/GEO/work/tools/check_ac_tc.py <epic>` 0 lỗi (chứng minh spec không bị sửa).
- Task đụng `geometry`/`content`/`aigateway`/`assignment` (19–25): thêm `make bench-mvp` (mock) exit 0; 19, 20, 22, 25 thêm `make bench-s2` đạt ngưỡng (≥ 95 % thỏa ràng buộc, ≥ 90 % đáp số, 0 sai mang nhãn).
- Ghi dòng "Dev/Test" (số đạt/tổng theo loại, mã lượt e2e) vào `artifacts/GEO/work/epics/<E>/status.md` (tạo nếu chưa có).

## Ngoài phạm vi
Không sửa `stories_ac.md`, `test_cases.md`, `test_design.md` (AC/TC sai → mục "Dev" trong `qc_findings.md` của epic, tạo nếu chưa có). Không sửa tài liệu sản phẩm, `PO-DECISIONS.md`, `Design/`. Không nối AI thật. Không giảm mức kiểm của test sẵn có (kể cả test E1 MVP khi đổi đăng nhập — PLAN A7). Không tắt luồng đăng nhập MVP. Không dùng/dừng cổng của checkout hay agent khác. Không push, không merge (trừ merge nhánh được chỉ định vào nhánh của bạn). Chặng 4–9, thông báo đẩy ngoài ứng dụng, thiết diện động (Q7) ngoài phạm vi.

## Commit và tiến độ
- Commit sau mỗi bước qua lệnh kiểm, message nêu bước + mã US; cuối message: `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`. Không commit node_modules, dist, .data, evidence, `.env.ports`.
- Sau mỗi bước ghi 1 dòng `<giờ> · <bước> · <trạng thái> · <commit>` vào `/Users/harry/IdeaProjects/GeometryMathSupport/.claude/worktrees/mvp-wave0/.claude/progress/<tên-agent>.md` (đường dẫn tuyệt đối của repo điều phối, `echo ... >>`). Không dùng chữ "xong"/"DONE" trừ dòng cuối `<E> · XONG · <commit>`.
- Mỗi lỗi lặp tối đa 3 lần sửa; quá thì ghi lại và chuyển việc.

## Báo cáo cuối (không quá 10 dòng)
AC đạt/tổng; TC tự động đạt/tổng theo loại; kết quả make verify, web, e2e (2 lượt), traceability `--strict`, bench (nếu có); đường dẫn index bằng chứng; phát hiện Dev; phần làm hộ epic khác; tệp ngoài phạm vi cần FIX; danh sách commit; token ước tính.

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.5 | 2026-11-26 | E2E theo mức ảnh hưởng (make e2e-affected), bỏ trọn bộ hai lượt mỗi task | delivery-lead |
| v1.4 | 2026-11-26 | Theme mặc định C + Bad Script; nội dung toán dùng --font-math | delivery-lead |
| v1.3 | 2026-11-26 | Giao diện theo HTML bản mẫu + token theme (style D mặc định, T-50) | delivery-lead |
| v1.2 | 2026-11-26 | Thêm chặng 2–3: nguồn S2/S3 sau R3 + PLAN_code-s23; tệp sở hữu, mốc tệp chung, chủ pom/package; mã TC `TC-S2En-nnn`, `*.p0m.ui.spec.ts`; seed/fixture-ids.d; lệnh kiểm `traceability --strict`, `check_ac_tc`, bench-s2/bench-mvp; đường dẫn tiến độ về repo điều phối mvp-wave0 | runner-T-261126-17 |
| v1.1 | 2026-11-22 | Bản cho E2…E7 + bài học E3 (ghi lại từ tiêu đề; bản cũ không có mục Lịch sử) | — |
