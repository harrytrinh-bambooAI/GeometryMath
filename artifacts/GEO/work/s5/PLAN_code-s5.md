Phiên bản: v1.0 · 28/11/2026
# PLAN_code chặng 5 · GEO · L4 Dev kế hoạch code (Đánh giá và trợ lý giáo viên)

**Nguồn (đọc 28/11/2026, bản mới nhất, gộp 80e9b6f vào `worktree-mvp-wave0`):** epic `artifacts/GEO/work/epics/S5-E1…S5-E6/{stories_ac,test_cases}.md` (v1.1 sau soát độc lập; 37 US / 219 AC / 219 TC; `check_s5.py` ĐẠT) · `s5/brief-s5.md` v0.2 · `s5/estimate-s5.md` v0.2 (88 ngày dev + 26,2 ngày test) · `s5/questions-s5.md` v0.2 (QS5-01…32, giả định đang dùng) · mẫu `s23/PLAN_code-s23.md` v1.0 · `.claude/queue.md` v0.3 · `.claude/briefs/epic-common.md` v1.6 (27/11) · code HEAD 80e9b6f (chỉ `ls`/`grep`: module `aigateway knowledge assignment content notify tracking iam settings subject …`, migration lớn nhất V423, seed `910…930`, feature `s2e1…s3e6`, Makefile `verify web-build e2e-affected traceability`).

**Giả định:** mọi câu QS5 còn mở dùng đúng giả định ghi trong `questions-s5.md` (cột "Giả định đang dùng"). Không có câu hỏi chặn code (§9).

## 1. Quyết định kế hoạch (L4, TẠM đến khi phiên chính duyệt)
| # | Quyết định | Lý do / nguồn |
|---|---|---|
| P1 | **Module máy chủ mới:** `lessonplan` (S5-E1), `exam` (S5-E2: ma trận, ngân hàng, lắp đề Java, bộ đề), `assessment` (S5-E4: rubric, bài có chấm, bài nộp, ảnh, OCR, chấm tay, trả bài), `aigrade` (S5-E6: đề xuất điểm, tín hiệu engine, chạy song song, KPI, cổng). S5-E3 dùng module có sẵn `knowledge` (thêm `knowledge/internal/kb/**`); S5-E5 thêm vào `aigateway` (công tắc, hạn mức, tắt khẩn), `notify` (thông báo trợ lý), `tracking` (tổng hợp lỗi) | ADR-GEO-01 một khối nhiều module; US-S5E4-01 "module mới `assessment`"; US-S5E3-01 "module `knowledge`" |
| P2 | **Xuất DOCX/PDF dùng chung:** `common/export/docx/**` (Apache POI, QS5-05) + pdfbox có sẵn; task 54 sở hữu (làm US-S5E1-05 rồi US-S5E2-05 trong cùng task) | US-S5E2-05 phụ thuộc US-S5E1-05 "dùng chung bộ dựng DOCX/PDF" |
| P3 | **Lắp đề viết lại bằng Java** trong `exam`, `check.py` + `lap_de.py` giữ làm bộ kiểm đối chứng chạy trong IT của 52 | QS5-06 |
| P4 | **AI luôn mock trong code/test.** Task AI mới (`lesson_plan`, `lesson_patch`, `class_notice`, `kb_qa`, `grade_ocr`, `grade`, `exam_draft`, `class_errors`) có prompt trong `AnthropicPrompts.java` + đáp mẫu `MockAiProvider` theo `F-S5-090…095`. Giá trị `ai_purpose` của cả 8 task do 51 thêm một lần (một migration, `ALTER TYPE … ADD VALUE IF NOT EXISTS`) để hai task song song không cùng sửa enum. Chất lượng AI thật (OCR viết tay, đề xuất điểm) **cần bench owner duyệt (QS5-27)** — ngoài CI, không chặn code | QS5-20, QS5-27; brief §3 ý 4; V421 (mẫu ADD VALUE) |
| P5 | **Cờ tính năng tắt mặc định** vì PO-64 chưa chốt thu ảnh (QS5-01, QS5-30): `geo.s5.grading.enabled=false` (S5-E4 trọn + US-S5E5-02, 03 phía giáo viên dùng điểm đã duyệt) và `geo.s5.ai-grading.enabled=false` (S5-E6). Cờ tắt → API trả 404, menu ẩn, job nền không chạy. Hồ sơ `test`/`e2e` bật cả hai (51 đặt). Phát hành theo cổng L5a/L5b/L5c của brief §5 | QS5-01, QS5-28; brief §5 |
| P6 | **Kho ảnh:** cổng `assessment.api.ImageStore`; bản cài đặt dev/test = tệp cục bộ mã hoá AES-GCM dưới `GEO_DATA_DIR` (không thêm dịch vụ Docker, không thêm cổng); bản S3 tương thích (QS5-18, đặt tại VN) viết sau cổng, bật bằng cấu hình prod, không chạy trong CI | QS5-18; tránh sửa `scripts/dev-up.sh` (tệp khung) |
| P7 | **Seed chặng 5 đặt tên `s5_<nnn>_<feature>_*.sql`** (chữ `s` xếp sau mọi tiền tố số `1xx…9xx` khi `seed-reset.sh` chạy `db/seed/e2e/*.sql` theo tên). Dữ liệu mẫu chung `F-S5-001…057` phần dùng schema sẵn có (giáo viên, lớp, học sinh, đề `D-KC-01`) do 51 nạp; phần cần bảng mới (giáo án, ma trận, kho, rubric, bài có chấm, bài nộp) do task chủ bảng nạp bằng tệp seed của mình với UUID đã đặt sẵn trong `fixture-ids.d/s5.json` | mẫu S23 §4 (42 nạp chung, 28–32 thêm riêng) |
| P8 | **Mã cố định fixture:** `F-S5-nnn` → UUID `00000000-0000-7000-8005-<nnn 12 chữ số>`; biến thể `093b`, `094b` → `…-8005-1000000000nn` (51 ghi bảng trong `fixture-ids.d/s5.json`) | mẫu S23 (8002, 8003) |
| P9 | **Thứ tự epic theo lệnh phiên chính** E2 → E1 → E5 → E3 → E4 → E6, ghép từng cặp; lệch có lý do phụ thuộc: US-S5E2-05 sau US-S5E1-05 (P2); US-S5E2-06 sau US-S5E3-01; US-S5E5-04 lên đợt W2 (điều kiện của US-S5E4-02, R5 estimate); US-S5E5-02, 03 xuống W7 (cần `CriterionAssessment` của S5-E6); S5-E4 bắt đầu cùng S5-E3 (W3) để hai nhánh song song | brief §4 đồ thị phụ thuộc |

## 2. Module ↔ epic (chủ sở hữu bảng mới)
| Module | Epic / task | Bảng mới (gợi ý tên, task tự chốt) |
|---|---|---|
| `exam` (mới) | S5-E2 · 52, 54 (xuất), 63 (luồng D) | `exam_matrix`, `exam_item_template`, `exam_bank_item`, `exam_set`, `exam_variant`, `exam_variant_item` |
| `lessonplan` (mới) | S5-E1 · 53, 54 | `lesson_plan_request`, `lesson_plan`, `lesson_plan_version`, `lesson_plan_lock` |
| `aigateway` | S5-E5-04 · 55 | `ai_class_quota`, `ai_usage_counter`, `ai_kill_switch` (+ cột công tắc chấm/trợ lý bên cạnh `ClassAiSwitch` của S3) |
| `notify` | US-S5E5-01 · 55 | `notice_draft` |
| `knowledge` | S5-E3 · 56, 58, 63 | `kb_document`, `kb_document_version`, `kb_section` (tsvector bỏ dấu), `kb_attachment`, `kb_help_policy`, `ai_interaction`, `ai_policy_decision`, `kb_feedback` |
| `assessment` (mới) | S5-E4 · 57, 59, 60 | `rubric`, `rubric_version`, `graded_assignment`, `submission`, `submission_page`, `stored_image`, `ocr_transcript`, `ocr_edit`, `approved_grade`, `grade_audit` |
| `aigrade` (mới) | S5-E6 · 61, 62, 65 | `ai_grade_proposal`, `criterion_assessment`, `engine_signal`, `shadow_pair`, `class_ai_gate` |
| `tracking` | US-S5E5-02, 03 · 64 | `class_error_insight` (hoặc view) |

## 3. Task, đợt và phụ thuộc
ID tiếp theo ID lớn nhất của `.claude/queue.md` (T-261126-50): **T-261128-51 … T-261128-67**. 14 task code (88 ngày dev), 1 task khung, 1 tester, 1 gate.

| Đợt | Task (‖ = song song, 2 Opus) | Epic / story chính | Ngày dev | Giờ/token ước | Phụ thuộc | Cần AI thật? |
|---|---|---|---|---|---|---|
| W0 | **51** khung + dữ liệu mẫu chung + BUG-S23-001 (1 Opus) | truy vết `--strict` nhận `S5-E*`, bí danh `S5`, `check_ac_tc` nhận S5, 6 feature rỗng `s5e1…s5e6`, mốc tệp chung, 8 `ai_purpose`, 2 cờ, seed `F-S5-*`, sửa reporter | — | 1.5h/4M | 33 (S3 L6 xong) | Không |
| W1 | **52** ‖ **53** | S5-E2 01–04 ‖ S5-E1 01–03 | 9 ‖ 7 | 4h/10M ‖ 3.5h/9M | 51 | Không (mock `lesson_plan`) |
| W2 | **54** ‖ **55** | S5-E1 04–05 + US-S5E2-05 ‖ US-S5E5-04, 01 | 7 ‖ 5 | 3.5h/9M ‖ 2.5h/6M | 52, 53 | Không |
| W3 | **56** ‖ **57** | S5-E3 01, 02, 07 ‖ S5-E4 01, 02, 08 (cờ) | 7 ‖ 8 | 3.5h/9M ‖ 4h/10M | 54, 55 | Không |
| W4 | **58** ‖ **59** | S5-E3 03, 04, 05 ‖ S5-E4 03, 09, 04 (cờ) | 7 ‖ 7 | 3.5h/9M ‖ 3.5h/9M | 56, 57 | 59: OCR viết tay cần bench (QS5-27), CI mock |
| W5 | **60** ‖ **61** | S5-E4 05, 06, 07 (cờ) ‖ S5-E6 01, 03 (cờ) | 6 ‖ 5 | 3h/8M ‖ 2.5h/6M | 58, 59 | 61: chấm AI cần bench (QS5-27), CI mock |
| W6 | **62** ‖ **63** | S5-E6 02, 04, 05 (cờ) ‖ US-S5E2-06 + US-S5E3-06 | 7 ‖ 5 | 3.5h/9M ‖ 2.5h/6M | 60, 61 | Không (mock) |
| W7 | **64** ‖ **65** | US-S5E5-02, 03 ‖ US-S5E6-06 (cờ) | 5 ‖ 3 | 2.5h/6M ‖ 1.5h/4M | 62, 63 | Không |
| W8 | **66** tester → gate **67** [wait-user] | L6 chặng 5 (UI Playwright trước, còn lại API) → owner L7 | — | 3h/4M | 64, 65 | — |

**Tổng:** 88 ngày dev (khớp `estimate-s5.md`) · agent code + khung 45,5 giờ, tester 3 giờ = **48,5 giờ agent**; theo đợt (2 song song) ≈ **28,5 giờ lịch** · token ước ≈ 123M (task ≤ 10M). Không task code nào quá 4 giờ ước.

**Tách để song song an toàn:**
- 54 giữ cả US-S5E1-05 và US-S5E2-05 vì cùng bộ dựng DOCX (P2) và chỉ một task mỗi đợt được sửa `pom.xml`.
- US-S5E5-04 (công tắc, hạn mức, tắt khẩn) đi W2 với US-S5E5-01: điều kiện cho chế độ chấm của US-S5E4-02 (W3).
- S5-E3: 03, 04, 05 phụ thuộc vòng (03 cần 04, 05; 04 cần 03) nên cùng một task 58. US-S5E3-06 (giáo viên xem lịch sử) dời sang 63 để 58 ≤ 3,5 giờ.
- S5-E4: 08 (kho ảnh) và 03 (chụp nộp) phụ thuộc lẫn nhau; 57 dựng cổng `ImageStore` + mã hoá + URL ký + gỡ EXIF + job xoá (US-S5E4-08), 59 dùng cổng đó cho chụp nộp. "Ảnh gửi AI" của US-S5E4-08 dùng bản gỡ EXIF qua `ImageStore.forAi(…)`; 59 gọi khi OCR.
- S5-E6: 61 (01 đề xuất + 03 tín hiệu engine, máy chủ) song song 60 (hàng chờ + chấm tay + trả bài) vì 61 không có giao diện; 62 (02 từ chối → chấm tay, 04 màn chấm có đề xuất, 05 chạy song song) sau khi màn chấm tay của 60 đã gộp.
- US-S5E5-02 cần US-S5E6-01, 03, 04 → W7; US-S5E6-06 (cổng KPI) độc lập module `aigrade/kpi` nên song song.
- Vòng mềm US-S5E4-02 ↔ US-S5E6-06 (R5): 57 dựng trường "chế độ chấm" + cổng `aigrade` trả "đóng" (stub `ClassAiGateQuery` trong `assessment.api`, 65 thay bản cài đặt).

**Gộp sau mỗi đợt** (như S23): merge hai nhánh vào `worktree-mvp-wave0` theo ID nhỏ trước → `cd app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic <mọi epic đã gộp>"` → `/code-review` (sonnet, diff đợt) → đợt kế chỉ mở khi xanh. Lỗi gộp → task FIX trong khoảng FIX của đợt.

## 4. Worktree, cổng, khoảng số migration, seed
Đã dùng (không được trùng): PG 54329–54669 (worktree hiện có 54359–54489; **54399 cấm**), Redis 6389–6719, máy chủ 8080–8182, web 5173–5266; migration ≤ V423 (khoảng S23 cấp tới V444). Chặng 5 dùng **PG 54709–54859, Redis 6739–6889, máy chủ 8200–8215, web 5280–5295**, migration **từ V500**, tăng dần theo đợt, mỗi đợt chừa 10 số FIX.

| Task | Worktree / nhánh (từ `worktree-mvp-wave0`) | `app/.env.ports` (PG · Redis · máy chủ · web) | Migration | Seed `app/db/seed/e2e/` | Feature web / e2e / IT |
|---|---|---|---|---|---|
| 51 | `s5-base` / `s5-base` | 54709 · 6739 · 8200 · 5280 | V500–V504 | `s5_000_core.sql` | khung `s5e1…s5e6`, `fixture-ids.d/s5.json` |
| 52 | `s5-e2a` / `s5-e2a` | 54719 · 6749 · 8201 · 5281 | V505–V524 | `s5_1[0-4]x_s5e2_*` | `s5e2` |
| 53 | `s5-e1a` / `s5-e1a` | 54729 · 6759 · 8202 · 5282 | V525–V544 | `s5_1[5-9]x_s5e1_*` | `s5e1` |
| FIX W1 | theo task FIX | 54849 · 6879 · 8214 · 5294 | V545–V554 | `s5_9xx_fix_*` | — |
| 54 | `s5-e1b` / `s5-e1b` | 54739 · 6769 · 8203 · 5283 | V555–V574 | `s5_2[0-4]x_s5e1b_*` | `s5e1`, `s5e2` (chỉ `export/`) |
| 55 | `s5-e5a` / `s5-e5a` | 54749 · 6779 · 8204 · 5284 | V575–V594 | `s5_2[5-9]x_s5e5_*` | `s5e5` |
| FIX W2 | | như FIX W1 | V595–V604 | `s5_9xx_fix_*` | — |
| 56 | `s5-e3a` / `s5-e3a` | 54759 · 6789 · 8205 · 5285 | V605–V624 | `s5_3[0-4]x_s5e3_*` | `s5e3` |
| 57 | `s5-e4a` / `s5-e4a` | 54769 · 6799 · 8206 · 5286 | V625–V644 | `s5_3[5-9]x_s5e4_*` | `s5e4` (`rubric/`, `assign/`, `storage/`) |
| FIX W3 | | như FIX W1 | V645–V654 | `s5_9xx_fix_*` | — |
| 58 | `s5-e3b` / `s5-e3b` | 54779 · 6809 · 8207 · 5287 | V655–V674 | `s5_4[0-4]x_s5e3b_*` | `s5e3` (+ `s5e4/assign/HelpLevel*`) |
| 59 | `s5-e4b` / `s5-e4b` | 54789 · 6819 · 8208 · 5288 | V675–V694 | `s5_4[5-9]x_s5e4b_*` | `s5e4/submit/`, `s5e4/ocr/` |
| FIX W4 | | như FIX W1 | V695–V704 | `s5_9xx_fix_*` | — |
| 60 | `s5-e4c` / `s5-e4c` | 54799 · 6829 · 8209 · 5289 | V705–V724 | `s5_5[0-4]x_s5e4c_*` | `s5e4/queue/`, `review/`, `return/` |
| 61 | `s5-e6a` / `s5-e6a` | 54809 · 6839 · 8210 · 5290 | V725–V744 | `s5_5[5-9]x_s5e6_*` | IT `s5e6` (không web) |
| FIX W5 | | như FIX W1 | V745–V754 | `s5_9xx_fix_*` | — |
| 62 | `s5-e6b` / `s5-e6b` | 54819 · 6849 · 8211 · 5291 | V755–V774 | `s5_6[0-4]x_s5e6b_*` | `s5e6` |
| 63 | `s5-e2e3` / `s5-e2e3` | 54829 · 6859 · 8212 · 5292 | V775–V794 | `s5_6[5-9]x_s5e2e3_*` | `s5e2/draft/`, `s5e3/history/` |
| FIX W6 | | như FIX W1 | V795–V804 | `s5_9xx_fix_*` | — |
| 64 | `s5-e5b` / `s5-e5b` | 54839 · 6869 · 8213 · 5293 | V805–V824 | `s5_7[0-4]x_s5e5b_*` | `s5e5/insights/` |
| 65 | `s5-e6c` / `s5-e6c` | 54859 · 6889 · 8215 · 5295 | V825–V844 | `s5_7[5-9]x_s5e6c_*` | `s5e6/kpi/` |
| FIX W7 | | như FIX W1 | V845–V854 | `s5_9xx_fix_*` | — |
| 66 tester | `mvp-wave0` (chỉ đọc code) | cổng hiện có của `mvp-wave0` (54359 · 6419 · 8092 · 5176) | — | — | — |

- Tệp migration: `app/server/src/main/resources/db/migration/V<số>__s5e<n>_<mô_tả>.sql`, số trong khoảng của task; không sửa migration đã áp dụng.
- Seed: `app/db/seed/e2e/s5_<nnn>_<feature>_*.sql` (P7). Không sửa seed của task khác; thêm dữ liệu thì thêm tệp mới trong khoảng của mình. 51 kiểm thứ tự glob (`s5_*` chạy sau `930_*`) trong `seed-reset.sh` mà không sửa script.
- Mã fixture: `fixture-ids.d/s5.json` (51, toàn bộ 27 mã); task thêm id riêng ở `fixture-ids.d/<feature>.json` của mình.
- Cờ (P5): khóa `geo.s5.grading.enabled`, `geo.s5.ai-grading.enabled` trong `application.yml` (mặc định `false`), bật trong `application-test.yml`/hồ sơ e2e (51 đặt; task không sửa).

## 5. Tệp sở hữu (để 2 agent song song không đụng nhau)
**Luật chung** (giữ luật 1–6 của `PLAN_code-s23.md` §5, thêm):
1. Mỗi task chỉ sửa tệp khớp regex "Phạm vi tệp" trong `.claude/queue.md`; ngoài regex → dừng, ghi báo cáo.
2. **Tệp chung có mục theo task** (51 tạo mốc `# --- T-261128-NN (<epic>) ---` / `// --- T-261128-NN ---`): 5 tệp của S23 (`sql-lint.properties`, `ArchitectureTest.java`, `application.yml`, `app/Makefile`, `app/README.md`) **cộng** `aigateway/internal/AnthropicPrompts.java` (nhánh `case` + hằng prompt) và `aigateway/internal/MockAiProvider.java` (đáp mẫu theo `F-S5-09x`). Mỗi task chỉ thêm dòng dưới mốc của mình.
3. **Tệp khung, KHÔNG sửa** sau 51: `app/web/src/features/{index,types}.ts`, `router.tsx`, `AreaLayout.tsx`, `app/e2e/playwright.config.ts`, `app/e2e/scripts/traceability.ts`, `app/e2e/reporters/evidence-reporter.ts`, `app/scripts/*.sh`, `artifacts/GEO/work/tools/check_ac_tc.py`, `artifacts/GEO/work/s23/impact-map.json` (51 thêm mục S5).
4. **Thư viện:** mỗi đợt một chủ `pom.xml`, một chủ `package.json`: W1 pom 52 (không cần thì bỏ), package 53 · W2 pom 54 (Apache POI) · W3 pom 57 (gỡ EXIF, mã hoá) · W4 package 59 (HEIC → JPEG phía web, nén ảnh) · W5 pom 61 · W6 package 62 (khoanh vùng ảnh) · W7 không.
5. `shared/**` (web), `common/**` (máy chủ): chỉ thêm tệp mới; 54 là chủ `common/export/**`.
6. `artifacts/GEO/work/epics/S5-E*/status.md` và mục "Dev" trong `qc_findings.md` là của task sở hữu story; không sửa `stories_ac.md`, `test_cases.md`, `estimate-s5.md`, `check_s5.py`.

| Task | Sở hữu (thêm/sửa) | Đọc, không sửa |
|---|---|---|
| 51 | `traceability.ts`, `evidence-reporter.ts` (BUG-S23-001 + tag `TC-S5E`), `e2e-affected.mjs` + `s23/impact-map.json` (mục S5), `check_ac_tc.py` (regex `S[235]`), `features/{index,types}.ts` + `s5e1…s5e6/index.tsx` rỗng, `fixture-ids.d/s5.json`, `s5_000_core.sql`, V500–V504 (8 `ai_purpose`), mốc 7 tệp chung, 2 cờ trong `application*.yml`, `package-info.java` cho `lessonplan exam assessment aigrade` | mọi code nghiệp vụ |
| 52 | `exam/**` (trừ `exam/**/export/**`, `exam/**/draft/**`), `features/s5e2/**` (trừ `export/`, `draft/`), `tests/s5e2`, `it/s5e2`, nạp `materials/de-thi/**` (chỉ đọc), V505–V524 | `content`, `geometry` |
| 53 | `lessonplan/**` (trừ `export/`, `patch/`), `features/s5e1/**`, `tests/s5e1`, `it/s5e1`, mốc 53 trong `AnthropicPrompts`/`MockAiProvider`, `app/web/package.json`, V525–V544 | `aigateway` khác mốc, `knowledge` |
| 54 | `lessonplan/**/{patch,export,list}/**`, `exam/**/export/**`, `common/export/**`, `features/s5e1/**`, `features/s5e2/export/**`, `tests/s5e1`, `tests/s5e2/export*`, `it/s5e1`, `it/s5e2`, `app/server/pom.xml`, V555–V574 | `exam` khác `export` |
| 55 | `aigateway/**/{quota,killswitch}/**`, `aigateway/**/ClassAiSwitch*`, `notify/**/draft/**`, `features/s5e5/**`, `tests/s5e5`, `it/s5e5`, V575–V594 | `lessonplan`, `exam` |
| 56 | `knowledge/internal/kb/**` (tài liệu, phiên bản, gắn, truy xuất), `knowledge/api/Kb*`, `features/s5e3/**`, `tests/s5e3`, `it/s5e3`, V605–V624 | `lessonplan.api` (giáo án đã duyệt) |
| 57 | `assessment/**/{rubric,assign,storage}/**`, `assessment/api/**`, `features/s5e4/{rubric,assign,storage}/**` + `features/s5e4/index.tsx`, `tests/s5e4`, `it/s5e4`, `app/server/pom.xml`, V625–V644 | `exam.api`, `assignment`, `aigateway.api` |
| 58 | `knowledge/internal/kb/{qa,policy,firewall}/**`, `answers/**/KsS5*` (mở rộng KS-1…KS-5, không sửa lõi V60), `assignment/**/HelpLevel*`, `features/s5e3/**`, `features/s5e4/assign/HelpLevel*`, `tests/s5e3`, `it/s5e3`, V655–V674 | `assessment` khác `HelpLevel*` |
| 59 | `assessment/**/{submit,ocr}/**`, `features/s5e4/{submit,ocr}/**`, `tests/s5e4/{submit,ocr}*`, `it/s5e4/{submit,ocr}*`, `app/web/package.json`, `app/web/vite.config.ts` (camera PWA), V675–V694 | `assessment/**/storage` (gọi qua `api`) |
| 60 | `assessment/**/{queue,review,returned}/**`, `features/s5e4/{queue,review,return}/**`, `tests/s5e4/{queue,review,return}*`, `it/s5e4/{queue,review,return}*`, V705–V724 | `notify.api` |
| 61 | `aigrade/**/{proposal,engine}/**`, `aigrade/api/**`, `tests/s5e6/*.api.spec.ts`, `it/s5e6`, `app/server/pom.xml`, V725–V744 | `assessment.api`, `geometry`, `answers` |
| 62 | `aigrade/**/{refusal,shadow,review}/**`, `features/s5e6/**` (trừ `kpi/`), `assessment/**/review/AiProposalSlot*` (một điểm cắm), `tests/s5e6`, `it/s5e6`, `app/web/package.json`, V755–V774 | `assessment` khác điểm cắm |
| 63 | `exam/**/draft/**`, `knowledge/internal/kb/history/**`, `features/s5e2/draft/**`, `features/s5e3/history/**`, `tests/s5e2/draft*`, `tests/s5e3/history*`, `it/s5e2`, `it/s5e3`, V775–V794 | `knowledge/internal/kb` khác `history` |
| 64 | `tracking/**/errors/**`, `features/s5e5/insights/**`, `tests/s5e5/insights*`, `it/s5e5`, V805–V824 | `aigrade.api`, `assessment.api`, `exam.api`, `knowledge.api` |
| 65 | `aigrade/**/kpi/**`, `aigrade/**/gate/**` (thay stub `ClassAiGateQuery`), `features/s5e6/kpi/**`, `tests/s5e6/kpi*`, `it/s5e6`, V825–V844 | `tracking` |

## 6. Bảng story → task (37 story; giờ ước gồm test của story)
| Story | Epic | Ngày dev | Task | Đợt | Cờ |
|---|---|---|---|---|---|
| US-S5E2-01 | S5-E2 | 2 | T-261128-52 | W1 | — |
| US-S5E2-02 | S5-E2 | 2 | T-261128-52 | W1 | — |
| US-S5E2-03 | S5-E2 | 3 | T-261128-52 | W1 | — |
| US-S5E2-04 | S5-E2 | 2 | T-261128-52 | W1 | — |
| US-S5E1-01 | S5-E1 | 2 | T-261128-53 | W1 | — |
| US-S5E1-02 | S5-E1 | 3 | T-261128-53 | W1 | — |
| US-S5E1-03 | S5-E1 | 2 | T-261128-53 | W1 | — |
| US-S5E1-04 | S5-E1 | 3 | T-261128-54 | W2 | — |
| US-S5E1-05 | S5-E1 | 2 | T-261128-54 | W2 | — |
| US-S5E2-05 | S5-E2 | 2 | T-261128-54 | W2 | — |
| US-S5E5-04 | S5-E5 | 3 | T-261128-55 | W2 | — |
| US-S5E5-01 | S5-E5 | 2 | T-261128-55 | W2 | — |
| US-S5E3-01 | S5-E3 | 3 | T-261128-56 | W3 | — |
| US-S5E3-02 | S5-E3 | 2 | T-261128-56 | W3 | — |
| US-S5E3-07 | S5-E3 | 2 | T-261128-56 | W3 | — |
| US-S5E4-01 | S5-E4 | 3 | T-261128-57 | W3 | grading |
| US-S5E4-02 | S5-E4 | 2 | T-261128-57 | W3 | grading |
| US-S5E4-08 | S5-E4 | 3 | T-261128-57 | W3 | grading |
| US-S5E3-03 | S5-E3 | 2 | T-261128-58 | W4 | — |
| US-S5E3-04 | S5-E3 | 2 | T-261128-58 | W4 | — |
| US-S5E3-05 | S5-E3 | 3 | T-261128-58 | W4 | — |
| US-S5E4-03 | S5-E4 | 3 | T-261128-59 | W4 | grading |
| US-S5E4-09 | S5-E4 | 2 | T-261128-59 | W4 | grading |
| US-S5E4-04 | S5-E4 | 2 | T-261128-59 | W4 | grading |
| US-S5E4-05 | S5-E4 | 2 | T-261128-60 | W5 | grading |
| US-S5E4-06 | S5-E4 | 2 | T-261128-60 | W5 | grading |
| US-S5E4-07 | S5-E4 | 2 | T-261128-60 | W5 | grading |
| US-S5E6-01 | S5-E6 | 3 | T-261128-61 | W5 | ai-grading |
| US-S5E6-03 | S5-E6 | 2 | T-261128-61 | W5 | ai-grading |
| US-S5E6-02 | S5-E6 | 2 | T-261128-62 | W6 | ai-grading |
| US-S5E6-04 | S5-E6 | 3 | T-261128-62 | W6 | ai-grading |
| US-S5E6-05 | S5-E6 | 2 | T-261128-62 | W6 | ai-grading |
| US-S5E2-06 | S5-E2 | 3 | T-261128-63 | W6 | — |
| US-S5E3-06 | S5-E3 | 2 | T-261128-63 | W6 | — |
| US-S5E5-02 | S5-E5 | 3 | T-261128-64 | W7 | grading |
| US-S5E5-03 | S5-E5 | 2 | T-261128-64 | W7 | grading |
| US-S5E6-06 | S5-E6 | 3 | T-261128-65 | W7 | ai-grading |
| **Cộng** | | **88** | 14 task | | |

## 7. Lệnh kiểm theo task (chạy ở gốc `worktree-mvp-wave0`, exit 0)
Mẫu: `cd .claude/worktrees/<wt>/app && make verify && make web-build && make e2e-affected && make traceability ARGS="--strict --epic MVP,S2,S3,<epic S5 đã gộp + của task> --defer <US S5 của các epic đó chưa làm>" && python3 ../artifacts/GEO/work/s5/check_s5.py && python3 ../artifacts/GEO/work/tools/check_ac_tc.py <epic S5 của task>`. Bí danh `S2`, `S3` có sẵn trong `traceability.ts`; `S5` do 51 thêm. `check_ac_tc.py` nhận S5 sau 51 (trước đó regex chỉ `S[23]`). Lệnh đầy đủ từng task ở khối "Kiểm" của `.claude/queue.md`.

## 8. Kiểm phủ story (tự kiểm, chạy ở gốc worktree)
`python3 -c "import re,glob;ids={i for f in glob.glob('artifacts/GEO/work/epics/S5-E*/stories_ac.md') for i in re.findall(r'^## (US-S5E\d+-\d+)',open(f).read(),re.M)};rows=re.findall(r'^\| (US-S5E\d+-\d+) \|.*?\| (T-261128-\d+) \|',open('artifacts/GEO/work/s5/PLAN_code-s5.md').read(),re.M);m={s for s,t in rows};dup=[s for s in m if sum(1 for r in rows if r[0]==s)>1];print(len(ids),'story trong epic;',len(m),'story có task; thiếu',sorted(ids-m),'; thừa',sorted(m-ids),'; trùng',dup);exit(1 if ids-m or m-ids or dup else 0)"`

Kết quả (28/11/2026, chạy sau khi viết bảng §6): `37 story trong epic; 37 story có task; thiếu [] ; thừa [] ; trùng []`, exit 0. Hàng đợi: `python3 ~/.claude/skills/epic-runner/queue.py --queue .claude/queue.md validate` → `ĐẠT: 49 task` (sau khi sửa ghi chú "T-50" ở T-261126-18/19).

## 9. Rủi ro và câu hỏi mở
**Rủi ro**
- R1 Xung đột tệp chung: chặn bằng §5 (mốc theo task, kể cả `AnthropicPrompts`/`MockAiProvider`; một chủ `pom.xml`/`package.json` mỗi đợt; `ai_purpose` thêm một lần ở 51).
- R2 52 (9 ngày dev, lắp đề Java giữ đúng 7 ràng buộc `lap_de.py`) vượt 4 giờ: tách US-S5E2-04 thành FIX W1 (54 chỉ cần `exam_set` đã duyệt cho xuất).
- R3 59 camera PWA iOS + HEIC: e2e chỉ chạy Chromium/WebKit giả lập; thiết bị thật để tester 66 ghi Manual (estimate R7).
- R4 Chất lượng OCR viết tay và đề xuất điểm chưa đo: **cần bench owner duyệt (QS5-27)** trước khi bật cờ `ai-grading` cho lớp nào; không chặn code (CI mock `F-S5-090…095`).
- R5 Phát hành S5-E4/S5-E6 chờ PO-64 (QS5-01, QS5-30): code sau cờ tắt mặc định (P5); L5a (E1, E2, E3, US-S5E5-01, 04) phát hành độc lập.
- R6 Vòng US-S5E4-02 ↔ US-S5E6-06: stub cổng "đóng" ở 57, 65 thay bản thật.
- R7 Tên seed `s5_*` (P7) phụ thuộc thứ tự glob của bash: 51 kiểm bằng `make e2e-reset` và ghi vào README; nếu locale đảo thứ tự thì 51 đổi sang tiền tố `99x`/`98x` và ghi báo cáo.

**Câu hỏi (gom cho phiên chính / owner, không chặn code)**
- Q-P1 Cho 51 chạy ngay sau tester S3 (T-261126-33 đã xong) dù gate S3 (T-261126-34) còn wait-user? Plan giả định có (51 không đổi hành vi S3).
- Q-P2 Bench AI chấm viết tay (QS5-27, 5 người-ngày + 3–5M token, ≥ 30 bài mẫu): owner duyệt lúc nào? Chỉ ảnh hưởng bật cờ `ai-grading`, không chặn 61, 62, 65.
- Q-P3 PO-64 (QS5-01) + QS5-30: cần chốt trước gate L5b; trước đó cờ `grading` tắt ở prod.

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.0 | 28/11/2026 | Bản đầu: quyết định P1–P9, module ↔ epic, 17 task (51 khung + BUG-S23-001, 52–65 code, 66 tester, 67 gate), 8 đợt, cổng/migration V500+/seed `s5_*`/feature, tệp sở hữu, bảng 37 story → task, lệnh kiểm phủ | agent plan-s5 (opus) |
