# Inbox · GeometryMath MVP · cập nhật 22/11/2026

## Việc: sửa gom lô phát hiện soát FOUNDATION (sau khi gộp E1)
- **Mục tiêu:** đóng các phát hiện còn lại của bản soát độc lập diff 1c5aa4c..4d3570a.
  1. [major, có từ trước] `app/server/src/main/resources/application.yml:3` `profiles.default: dev` → quên đặt profile khi triển khai sẽ bật `/dev/token` + khóa JWT mặc định. Đề xuất: bỏ default; máy chủ từ chối khởi động khi không có profile, hoặc khi profile `prod` mà khóa còn mặc định.
  2. [minor] `app/scripts/env.sh:9-12` bỏ dòng cuối không có newline; không lọc tên biến (chỉ nên nhận `GEO_*`, `PGBIN`).
  3. [minor] `app/web/src/shared/api/client.ts:30-31` chấp nhận `//host` → JWT có thể gửi sang nguồn khác; từ chối `//` và `://`.
  4. [minor] `app/e2e/support/fixtures.ts` che trường `token` trong http.json/ảnh bằng chứng.
  5. [minor] `app/e2e/reporters/evidence-reporter.ts`: test không chạy tới phải vào summary (interrupted/skipped); đếm test không nhân theo số mã TC; tiêu đề có mã TC sai định dạng → cảnh báo.
- **Nguồn:** bản soát độc lập 22/11/2026 (agent Sonnet), diff ở trên.
- **Xong khi:** `make verify` xanh; `npm run typecheck && npm run lint && npm run build` xanh; `make e2e` đạt; khởi động không có profile → lỗi rõ ràng.
- **Ngoài phạm vi:** tính năng nghiệp vụ; tài liệu sản phẩm.
- Đã sửa ngay (có thể xóa dữ liệu): #1 seed-reset thiếu chốt cụm, #7 tiền tố Redis — xem git log.
- **Trạng thái (FIX1, nhánh mvp-fix1, 22/11/2026):** #1 ĐÓNG b0da801 · #2 ĐÓNG 17cb003 · #3 ĐÓNG 17cb003 · #4 ĐÓNG 17cb003 · #5 ĐÓNG 17cb003.

## Việc: FIX1 — lô A…J (soát Đợt 0 + soát E1) · nhánh mvp-fix1
Kiểm: `make verify` 94 đơn vị + 47 IT đạt; web typecheck/lint/6 test/build đạt; `make e2e` hai lượt liên tiếp 67/67 test (api 23, chromium 22, webkit 22), E1 67/67 TC có test, 0 trượt.
- A [major] profile mặc định, prod thiếu khóa, lộ /dev, /test-hooks — **ĐÓNG** b0da801 (ProfileGuard, ProfileGuardTest, ProdProfileIT, CONFIG.md §8).
- B env.sh — **ĐÓNG** 17cb003.
- C client.ts URL khác nguồn — **ĐÓNG** 17cb003 (url.ts + url.test.ts).
- D che token trong bằng chứng — **ĐÓNG** 17cb003.
- E evidence-reporter — **ĐÓNG** 17cb003.
- F reset thử sai chéo file (TC-0171/0179) — **ĐÓNG** 76952cb (reset theo ips/userIds; khóa liên tiến trình F-GEO-003 giữa TC-0179 và TC-0111/0112).
- G TC-0122 assertion luôn đúng; TC-0120 phía máy chủ — **ĐÓNG** 76952cb (đếm đúng +1, không trùng, bản ghi cũ còn nguyên; khóa AN_AI_LOCK; IT tc0120).
- H TC-0127 chỉ đọc seed — **ĐÓNG** 76952cb. Phát hiện kèm: CatalogImporter ghi lời giải với model/prompt NULL (trái BR-GEO-113) → sửa: ghi `geo.ai.model`/`geo.ai.prompt-version`. Dữ liệu hình (scene_build) chưa có đường tạo trong E1 nên vẫn kiểm bản seed.
- I TC-0802 SQL chép tay — **ĐÓNG** 76952cb (repository thật + auto_explain thu kế hoạch cả trong hàm plpgsql; Q20 chưa có repo, giữ truy vấn danh mục).
- J hỏi đáp lũy đẳng theo requestId — **ĐÓNG** 76952cb (IT: cùng requestId → cùng câu trả lời, số lượt gọi AI không tăng).
- **ĐÓNG · 92f1d05 (FIX4a):** `BenchmarkE1IT.tc0148` thay chuỗi "Tính góc giữa SD và mặt đáy." không khớp XML (chữ có dấu ở dạng thực thể) nên không tạo phiên bản mới như chú thích nói; ca vẫn đạt nhưng kiểm yếu hơn ý định.
- **ĐÓNG · 92f1d05 (FIX4a):** trace/video của dự án UI vẫn chứa JWT (mục D chỉ phạm vi http.json + ảnh bảng).

## Việc: FIX2 — phát hiện soát E3 (diff 0742560..09b0076, 23/11/2026) · sửa gom cùng E2/E4
- **Mục tiêu:** đóng 3 major + 5 minor; thêm test cho từng mục.
  1. [major] `shared/viewer/measures.ts:144-171` đo gián tiếp lộ đại lượng ẩn (dpp thay dff, dpl thay dll, SM=SD/2). Theo PO-56: ẩn mọi số đo có giá trị trùng một đại lượng đang ẩn trong dung sai (AnswerMatch.tolerance) khi lời giải còn khóa; thêm TC Dev.
  2. [major] `hookGate.ts:6`, `Figure3DViewer.tsx:125`, `features/e3/index.tsx:12`: móc `__geoScene` + LPrismSandboxPage còn trong bản production. Dùng `import.meta.env.DEV || import.meta.env.VITE_GEO_TEST_HOOKS === '1'` trực tiếp + import động; thêm kiểm `grep __geoScene dist` = 0 trong `make verify` hoặc web build.
  3. [major] `scripts/e2e.sh:32` + `vite.config.ts:19`: bản e2e ghi vào `web/dist`; dùng outDir `dist-e2e`.
  4. [minor] TC-0309 `tests/e3/events.api.spec.ts:20-35` luôn đúng → thêm bước xoay/thu phóng thật rồi kiểm toolCount.
  5. [minor] `openNowE3` `expectedRev:1` và describe.serial 0309/0313 vỡ khi retry → đọc rev hiện tại / reset làn.
  6. [minor] `FigureEventService.java:68-74` khóa Redis figbatch đặt trước khi ghi DB → đặt sau khi ghi thành công; client giữ batchId khi gửi lại.
  7. [minor] `visibility.ts:40` lấy mẫu 32 đoạn → tìm điểm chuyển chính xác (chia đôi); ghi rõ giới hạn mặt cong.
  8. [minor] `model.ts:325` khóa lại phải ẩn lại số đo đã hiện; `model.ts:271` planarHull sai với thiết diện lõm.
  + Thiếu test an ninh API E3: vai sai (403), học sinh khác bài/khác trường (404) cho figure-events, open, hints, lock.
- **Trạng thái (FIX2, 23/11/2026, nhánh mvp-fix2):** ĐÓNG cả 8 mục + an ninh.
  #1, #2, #3, #7, #8 ĐÓNG · fc3cbcc (Dev-E3-01…06, `viewer.fix2.test.ts`; `scripts/check-dist.mjs` trong `npm run build`).
  #4, #5 ĐÓNG · 5e01fe0 + 8ea025b (TC-0309 E2E-UI làn E3 5/6, seed 12 làn, `openNowE3` đọc rev, TC-0313 theo chênh lệch).
  #6 ĐÓNG · 5e01fe0 + d5be357 (khóa "pending"→"done" sau khi ghi; client giữ batchId; IT Dev-E3-07).
  An ninh ĐÓNG · 5e01fe0 (`tests/e3/security.api.spec.ts`, Dev-E3-08…10). qc_findings E3 QF-E3-D09…D14.
- **Nguồn:** bản soát độc lập E3 (Sonnet) 23/11/2026.
- **Xong khi:** make verify xanh; web typecheck/lint/test/build xanh + grep `__geoScene` trong `web/dist` = 0; make e2e hai lượt đạt toàn bộ.
- **Ngoài phạm vi:** tính năng mới; sửa stories_ac/test_cases.

## Việc: FIX2 (tiếp) — phát hiện soát E2 (diff 0f2bcf4..23f120b, 23/11/2026)
- **Mục tiêu:** đóng 2 major + 6 minor; thêm test với kịch bản AI giả lập trả dữ liệu sai.
  1. [major] `SelfStudyService.java:~147` + `SceneRepository.java:157-158`: `solid` từ AI ghi thẳng vào enum `solid_kind` → 500 với "tetrahedron". Kiểm danh mục; ngoài danh mục → đề lạ kèm MSG-16 (hoặc MSG-18), không 500.
  2. [major] `SelfStudyService.build()` → `DefaultGeometryEngine.java:233-288`: `asked`/điểm thiếu → IndexOutOfBounds 500. Kiểm số điểm theo kind; mọi lỗi dữ liệu AI → MSG-18, không 500.
  3. [minor] `SceneService.java:~85` hình mẫu gắn cứng "verified" không chạy `engine.check`; không có cận trên tham số.
  4. [minor] `SceneService.java:~87` MSG-19 luôn gắn ô tham số đầu tiên → gắn đúng ô gây lỗi.
  5. [minor] `TemplateCatalogService.validate:~85` không kiểm key tham số khớp bộ dựng, không thử dựng bằng mặc định; `load()` không kiểm `solid_kind`.
  6. [minor] `DefaultGeometryEngine.specFrom:62` `Double.toString` ra ký hiệu E mà AnswerMatch không đọc; tọa độ chuỗi "abc" → 0 lặng lẽ.
  7. [minor] TC-0205 `GeometryEngineE2Test` nhãn tự suy (luôn đúng) → đi qua đường kiểm chứng thật.
  8. [minor] TC-0218 `labels.ui.spec.ts` kiểm phủ định yếu; `ProblemDetailPage.tsx:~63` không đặt lại `scene` khi `assignmentId` đổi.
- **Trạng thái (FIX2, 23/11/2026):** ĐÓNG cả 8 mục · 5e01fe0.
  #1, #2, #6: kịch bản AI lỗi trong `mock-ai/scene.json` + `it/e2/AiDataE2IT` (Dev-E2-03…07), engine Dev-E2-01, 02. #3, #4, #5: Dev-E2-08…10.
  #7: TC-0205 qua `verify` thật. #8: TC-0218 kiểm nhãn chặt; `AssignmentScene` gắn mã bài. qc_findings E2 QF-E2-D09…D11.
  Còn chờ PO: cận trên tham số hình mẫu = 1000 là giả định Dev (QF-E2-D10). QC ghi nhận TC-0309 đổi tầng E2E-API → E2E-UI (QF-E3-D13).
- **Nguồn:** bản soát độc lập E2 (Sonnet) 23/11/2026.
- **Xong khi:** như FIX2 ở trên; thêm IT với kịch bản AI giả lập trả solid lạ / thiếu điểm / số dạng chuỗi.

## Việc: FIX3 — tích hợp E4+E5 và phát hiện soát E4/E5 (23/11/2026)
- **Mục tiêu:**
  0. [BLOCKER tích hợp] Trên nhánh tích hợp 317a192: `CatalogE5IT.tc0528_newSolutionVersion:219` trượt — nạp lại tạo phiên bản mới (01a2047a-…) thay vì giữ 00000000-0000-7000-8000-000000100031 khi lời giải không đổi. Nghi tương tác E4 (chữ ký đề, tiêu đề đề trong seed D03, ghi solution/signature) × E5 (importer tạo phiên bản khi lời giải đổi). Mỗi nhánh riêng đạt.
  1. [E5, tự nêu] Giáo viên bất kỳ giao được đề thuộc bank thử qua API; quyền bank chỉ áp ở danh sách/đếm.
  (+ phát hiện soát E4/E5 bổ sung bên dưới khi có)
- **Xong khi:** make verify xanh trên nhánh tích hợp; make e2e hai lượt đạt toàn bộ.
- Phát hiện soát E5 (Sonnet, 23/11/2026) — 4 BLOCKER cùng gốc "quyền bank chỉ áp ở danh sách/đếm" (mở rộng mục 1):
  2. `V10__schema_mvp.sql:886-900` fn_create_assignment không kiểm bank → giao đề ngoài bank (201).
  3. `V10:1033-1048` fn_get_solution_teacher + `GET /api/v1/teacher/problems/{versionId}/solution` lộ lời giải/đáp số đề ngoài bank.
  4. `CatalogRepository.detail()` (~128-137) `GET /api/v1/teacher/catalog/problems/{versionId}` lộ đề/cảnh/gợi ý/lời giải.
  5. `SceneRepository.catalogVersion()` (133-150) + `GeometryController:73` lộ đề/cảnh/gợi ý.
  Sửa: điều kiện `p.bank='main' OR EXISTS(catalog_bank_access WHERE org_id=:orgId AND bank=p.bank)` ở cả 4 chỗ, trả NOT_FOUND; thêm guard Java dùng chung (vì E6 có thể định nghĩa lại fn_create_assignment); TC Dev "giáo viên ngoài bank → 404" cho chi tiết/lời giải/cảnh/giao bài.
  6. [minor] `CatalogTestHooks.java:~35` mã HKM-%02d từ count+1 có thể trùng khi chạy song song → sinh mã duy nhất.
  7. [minor] TC-0533 chưa chạm ca đề mới chèn TRƯỚC con trỏ (BR-GEO-217).
- Phát hiện soát E4 (Sonnet, 23/11/2026) — 0 blocker:
  8. [major] `HintService.java:generateSelfHints` gợi ý AI tự học không qua AnswerGuard (BR-GEO-179, US-405) → chạy guard trước khi lưu/trả.
  9. [major] `SelfStudyService.ocr` + BodySizeLimitFilter: không kiểm loại tệp (magic bytes JPEG/PNG/HEIC…) và kích thước ảnh sau giải mã (D07 1 MB của ẢNH, không phải thân yêu cầu).
  10. [minor] `AnswerGuard.addAnswer` bỏ qua đáp số < 2 ký tự ("a" của HKG-02, F-GEO-116).
  11. [minor] `SolutionService.open` luôn gán "AI soạn", bỏ qua `aiAuthored()`.
  12. [minor] `FigureEventService` constructor 4 tham số để hints=null → NPE; bỏ hoặc sửa.
  13. [minor] `HintService`: hai yêu cầu đầu đồng thời gọi AI 2 lần; bài kế thừa khóa mà đề kho thiếu gợi ý sinh gợi ý AI rồi trả NOT_FOUND.
  14. [minor] TC-0493 `qa.ui.spec.ts`, TC-0484 `inherit.ui.spec.ts`: gom body response bất đồng bộ → có thể đạt giả; chờ đủ response trước khi kiểm.
  15. [minor] TC-0478 `expect(bearer(anC)).toBeTruthy()` luôn đúng; TC-0445 cần đếm event_log.
  16. [minor] TC-0486 cửa sổ 20 s dễ đỏ giả trên máy chậm → dùng móc điều khiển giờ mở thay vì đồng hồ thật.
- **Trạng thái (FIX3, nhánh mvp-fix3, 23/11/2026):** ĐÓNG cả 17 mục (0…16).
  #0 ĐÓNG · b55989b — gốc: seed E4 (401) thêm khóa `highlight`/`draw` vào bước lời giải; bộ nạp E5 so nguyên jsonb → "lời giải đổi". V59 `fn_content_solution_same` so phần chữ (title/body/why) + đáp số; tc0528 đạt, không nới assertion (QF-E5-D10).
  #1–5 ĐÓNG · b55989b (+ 94c20b8 LIMIT) — V59 `fn_catalog_bank_visible` trong `fn_create_assignment`, `fn_get_solution_teacher`; điều kiện bộ ở `CatalogRepository.detail`, `SceneRepository.catalogVersion`; guard Java `content.api.BankAccess` ở 4 dịch vụ; ghi bắt buộc `app/docs/CONFIG.md` §9; IT `it/e5/BankAccessE5IT` Dev-E5-01…05 (QF-E5-D11).
  #6, #7 ĐÓNG · 94c20b8 — móc nạp đề khóa theo bộ + `prefix`; TC-0533 thêm đề HKA-… trước con trỏ (QF-E5-D12).
  #8–#13 ĐÓNG · fc71662 (+ 94c20b8 `fn_self_lock_inherited`) — IT `it/e4/HelpFix3E4IT` Dev-E4-01…06, unit `AnswerGuardShortAnswerTest` Dev-E4-07 (QF-E4-D08…D13).
  #14–#16 ĐÓNG · 94c20b8 — `recordResponses`, móc `GET /test-hooks/e4/events`, `POST /test-hooks/e4/release-passed` (QF-E4-D14).

## Việc: FIX4a — phát hiện soát E6 + 2 mục cũ (25/11/2026) · nhánh mvp-fix4a
- **Mục tiêu:** đóng 3 major + 7 minor trong `.claude/reviews/E6-review.md` (soát Sonnet, diff 5278808..2f4c5d0) và 2 mục MỞ cũ: `BenchmarkE1IT.tc0148` kiểm yếu (chuỗi thực thể XML); trace/video UI còn JWT.
- **Nguồn:** `.claude/reviews/E6-review.md` v1.0 · 25/11/2026; inbox mục FIX1.
- **Xong khi:** make verify xanh; web typecheck/lint/test/build xanh; make e2e hai lượt đạt toàn bộ; mỗi major có test (TC Dev).
- **Ngoài phạm vi:** code E7; stories_ac/test_cases; tính năng mới.
- **Trạng thái (25/11/2026, nhánh mvp-fix4a):** chi tiết ở `artifacts/GEO/work/epics/E6/qc_findings.md` mục Dev (QF-E6-D01…D12).
  M1 ĐÓNG · 92f1d05 — Dev-E6-01 (`it/e6/FixE6IT`). M2 ĐÓNG · 92f1d05 — Dev-E6-02 (e2e `e6/assign.ui.spec.ts`, 2 ca × 2 trình duyệt). M3 ĐÓNG · 92f1d05 — Dev-E6-03 (`FixE6IT`).
  m1–m6 ĐÓNG · 92f1d05 — Dev-E6-04…07 (`AnswerRulesTest`); m1 không sửa "bao nhiêu độ", "đơn vị là gì" (đòi giá trị, F-GEO-211/228).
  m7 ĐÓNG một phần · 92f1d05 — Dev-E6-08; không sửa: giới hạn tần suất `POST /solution/open` và AI xem trước (chờ NFR-GEO-056/D-GEO-125), nhiều nút, `highestHint` thuộc đề.
  tc0148 ĐÓNG · 92f1d05. Trace JWT ĐÓNG · 92f1d05 (che trong trace.zip; video không chứa chuỗi JWT; quét 563 trace lượt 20261125-071230: 0).
  Kiểm: make verify 123 đơn vị + 112 IT xanh; web 33/33 + build xanh; make e2e 558 đạt / 1 bỏ qua (TC-0302 WebKit, có từ trước) hai lượt 20261125-070629, 20261125-071230; truy vết E6 117/117.
  ĐÓNG (T-261126-01, 26/11/2026): ~~`geo.ai.provider: mock` mặc định, ProfileGuard không cấm mock ở prod~~ — prod mặc định `anthropic`, ProfileGuard từ chối prod + mock và anthropic thiếu GEO_AI_API_KEY (ProfileGuardTest, ProdProfileIT).

## Việc: FIX4b — phát hiện soát E7 (25/11/2026) · nhánh mvp-fix4b
- **Mục tiêu:** đóng 3 major + 8 minor trong `.claude/reviews/E7-review.md` (soát Sonnet, diff 78a7387..6b7117a).
- **Nguồn:** `.claude/reviews/E7-review.md` v1.0 · 25/11/2026.
- **Xong khi:** make verify xanh; web typecheck/lint/test/build xanh; make e2e hai lượt đạt toàn bộ; mỗi major có test (Dev-E7-xx).
- **Ngoài phạm vi:** phần FIX4a (E6: ReleaseService, AssignPage, ProbeRunner, AnswerRules, tc0148, trace JWT); stories_ac/test_cases; tính năng mới.
- **Trạng thái (fix4b, 25/11/2026):**
  - M1 ĐÓNG · fb6cd43 (Dev-E7-06: e2e `e7/fix4b.ui.spec.ts` 2 ca bấm thật + IT đệm)
  - M2 ĐÓNG · 8d4677e (Dev-E7-07: V85 quy tắc 30 phút trong lô; IT lô cách 23 giờ)
  - M3 ĐÓNG · 8d4677e (Dev-E7-08: lũy đẳng lô trong durable_request; IT xóa khóa Redis rồi gửi lại, gửi đồng thời)
  - m1 ĐÓNG · fb6cd43 (toolRetry: 4xx bỏ lô, mạng/5xx tối đa 12 lần; sự kiện mới không kẹt)
  - m2 ĐÓNG · fb6cd43 (@Profile E7Sandbox*, SandboxRepository E4; ArchUnit testHooksAreProfileScoped)
  - m3 ĐÓNG · fb6cd43 (trần 100 em/2000 câu mới nhất, cờ truncated + dòng báo; IT ranh giới 100/101)
  - m4 ĐÓNG · fb6cd43 (một nguồn: câu hỏi của học sinh còn trong lớp; IT em rời lớp)
  - m5 ĐÓNG · fb6cd43 (404 userId ngoài lớp; e2e 403/404); phần "bài lưu trữ" không sửa (Dev-E7-10)
  - m6 ĐÓNG · fb6cd43 (vượt trần báo lỗi, ghi "chưa phơi ra"); chỉ mục không sửa (chưa có điểm cuối)
  - m7 ĐÓNG · fb6cd43 (EVENT_TIME_BOUND chỉ nhận vị từ); móc restart không sửa (Dev-E7-02/08)
  - m8 ĐÓNG · fb6cd43 (routeMerge + test, tự làm mới 30 s, trần 366 ngày); BR-GEO-267 tên/họ tên không sửa — chờ BA (Dev-E7-10)

## Việc: FIX5 — phát hiện soát T-04 (diff 1ce17bd..6b62557, 26/11/2026, Sonnet) · 0 blocker, 0 major
- **Mục tiêu:** đóng 3 minor, mỗi mục có test.
  1. [minor] `AiSettings.costMicro` model không có trong `geo.ai.pricing` → cost 0 lặng lẽ; InstrumentedAiGateway không cảnh báo. Sửa: log cảnh báo (hoặc từ chối khởi động khi provider=anthropic mà model không có giá).
  2. [minor] `AnthropicAiProvider.java:150-166` + `application.yml` `ai.anthropic.fallbacks: default`: chi phí tính theo model yêu cầu, không theo model API trả về khi fallback; header beta chưa kiểm với API thật (xem kết quả T-05).
  3. [minor] `ContentPackLoader.java:72-114` mỗi lần khởi động `setArchived(code,false)` + ghi version mới → bật lại/ghi đè đề quản trị đã lưu trữ/sửa. Gắn NEEDS C2 (nạp ở prod).
- **Nguồn:** báo cáo soát T-04 26/11/2026; tiến độ `.claude/progress/review-t04.md`.
- **Xong khi:** `make verify` xanh; `make bench-mvp` (mock) exit 0; test mới cho từng mục.
- **Ngoài phạm vi:** dữ liệu đề; e2e spec.
- **Trạng thái (26/11 14:36):** #1–#3 ĐÓNG 51ff36c, gộp vào worktree-mvp-wave0; phiên chính chạy lại verify 201 + 137 IT, bench-mvp mock exit 0.

## Việc: delivery-lead agent_watch.py báo DONE nhầm (26/11 14:50)
- **Mục tiêu:** watcher chỉ báo DONE ở bước cuối, không ở bước trung gian có trạng thái `xong`.
- **Nguồn:** T-261126-08: watcher exit DONE ở dòng "14:38 · PO-62..65 vào PO-DECISIONS · xong" khi agent còn chạy. Brief (queue.py brief) dặn mọi bước ghi trạng thái → mâu thuẫn.
- **Xong khi:** quy ước bước cuối riêng (ví dụ `· HOÀN TẤT ·` hoặc `DONE`) trong brief + agent_watch; chạy thử trên tệp tiến độ mẫu không báo DONE ở bước trung gian.
- **Ngoài phạm vi:** code dự án GEO.
- **Trạng thái (26/11):** ĐÓNG — queue.py v0.4 dòng 235: brief ghi "bước giữa `ok`, CHỈ bước cuối `xong`"; BRIEF.md đã có luật; agent_watch giữ nguyên. Kiểm: `queue.py brief T-261126-15` có dòng mới, ast parse OK.

## Việc: Dom vẽ bổ sung sau T-38 (kịch bản bản mẫu, 26/11/2026)
- **Mục tiêu:** vẽ các bước "chưa vẽ" trong kịch bản + sửa màn lệch PO-66.
  1. [lệch] `c3-01-ma-nguoi-dung` còn ghi "học sinh không có mã khôi phục" — trái PO-66 G3 → vẽ lại.
  2. [thiếu] form tạo lớp (KB1 b4); màn "đã vào Kho" (KB2 b9); danh sách "Bài của em" (KB3 b5, KB4 b2); nhập mã khôi phục / đặt mật khẩu mới (KB6 b6); nút Trình chiếu từ Soát đề / Soạn bộ bài + màn mất mạng riêng khi Trình chiếu (KB7).
  3. [nhỏ] KB3 chỉ 2/6 bước tô sáng được (selector không khớp); hộp chú thích che tiêu đề màn.
- **Nguồn:** báo cáo T-38, `Design/s23/KICH-BAN.md` (26/11).
- **Xong khi:** `Design/s23/kb-shoot.js` chạy 0 pageerror; không còn "chưa vẽ" trong s23k.js; ảnh mới trong Design/s23/.
- **Ngoài phạm vi:** app/, spec. Chờ owner chọn style (T-39) để vẽ cùng lô.

## Việc: PO soát sau T-37 (26/11/2026)
- **Mục tiêu:** đối chiếu story map S3 (ghi 23) với thực tế 24 story; vẽ khung màn cho story mới US-S3E5-05 (xóa tài khoản) và US-S3E2-04 (đóng/lưu trữ bộ) — gộp vào lô Dom sau T-38.
- **Nguồn:** báo cáo T-37, DECISIONS 26/11.
- **Xong khi:** story map = số story `check_ac_tc` đếm; ảnh 2 màn mới trong Design/s23/.
- **Ngoài phạm vi:** code.

## Việc: BA sửa spec sau T-19 (26/11/2026)
- **Mục tiêu:** AC US-S2E2-02-BV2 ghi góc A ≈ 179,8°, đúng là 174,87° (Dev-S2E2-01) → sửa AC + TC; thống nhất `space` (spec) thay `solid` (PLAN A2); đáp số tham chiếu bộ đề phẳng 30 đề là TẠM chờ giáo viên Toán soát (QA-S2E2-02).
- **Nguồn:** báo cáo T-19, `epics/S2-E2/qc_findings.md`.
- **Xong khi:** `check_ac_tc.py S2-E2` 0 lỗi, AC/TC khớp test.
- **Ngoài phạm vi:** code.

## Việc: FIX sau T-18 (26/11/2026) — gom vào FIX kế tiếp (cùng e2e-affected)
- **Mục tiêu:** (1) [quan trọng] `app/e2e/reporters/evidence-reporter.ts` TC_TAG chỉ nhận TC-GEO/TC-KHUNG → kết quả e2e S2/S3 ghi "chưa chạy" trong truy vết; thêm `TC-S[23]E\d+-\d+`; (2) `privacy_notice` vào `keep-tables.txt`; (3) trang chủ: liên kết Đăng ký/Đăng nhập bằng mã; (4) TC-S2E1-020 kiểm ngăn "Của tôi" khi S2-E6 xong.
- **Nguồn:** báo cáo T-18 + `epics/S2-E1/qc_findings.md` DEV-S2E1-01…13.
- **Xong khi:** traceability --strict S2-E1 hiện kết quả đạt (không "chưa chạy"); e2e affected xanh.
- **Ngoài phạm vi:** tính năng mới.

## Việc: FIX sau T-41 (26/11/2026) — gom vào FIX T-261126-43
- **Mục tiêu:** (a) `scripts/dev-up.sh` chờ Redis nhận kết nối (ping lặp có hạn giờ) trước khi kiểm thư mục cổng — hết lỗi "cổng thuộc thư mục khác ()" lần đầu; (b) `router.tsx`: ẩn feature 0 màn ở trang tổng quan /gv, /hs; (c) test chập chờn khi 2 worktree chạy e2e cùng lúc: TC-GEO-0428 (`e4/hints.ui.spec.ts:129`, chromium, toBeVisible 4,3 s) và TC-GEO-0485 (`e4/inherit.ui.spec.ts:135`, webkit) — trượt ở lượt kiểm lại T-41 16:37, đạt ở 2 lượt của agent; chờ theo trạng thái thay vì thời gian.
  (e) Test nhạy tải (3 bộ e2e cùng lúc, load ~48, lượt 29,7 phút): TC-GEO-0804 (`e1/isolation.api.spec.ts:257`, không thấy khóa `:mon:` trong Redis — có thể test khác reset Redis), TC-GEO-0688 (`e6/auto.ui.spec.ts:58`, đếm ngược không bắt được mẫu nào trong cửa sổ thời gian), TC-GEO-0222 (`e2/labels.ui.spec.ts`, toHaveText 5 s). Đạt ở lượt khác cùng mã. Cân nhắc: giới hạn 1 bộ e2e chạy cùng lúc trên máy (khóa tệp trong e2e.sh) thay vì nới test.
  (d) `scripts/e2e.sh`: khi bị ngắt (agent chết) để lại máy chủ/vite mồ côi giữ cổng → `make down` không dọn được → e2e sau báo "cổng đang bận". Thêm trap EXIT/INT/TERM tắt tiến trình con + ghi pid để `make down` dọn. (gặp 26/11 18:40 ở fix-43)
- **Nguồn:** báo cáo T-41 (26/11).
- **Xong khi:** `make up` từ trạng thái sạch đạt 3 lần liên tiếp; e2e tổng quan /gv không có dòng feature rỗng.
- **Ngoài phạm vi:** tính năng.

## Việc: câu mở sau T-44 (theme) và T-45 (regression) · ghi 26/11/2026 tối
- **Mục tiêu:** đóng các điểm còn mở hai agent báo.
  1. [Design] `--org` làm màu chữ chỉ 3,7:1 ở theme A (`.gv-chip`, `.gv-sol text`) → cấp token `--org-t`.
  2. [PO/Design] `--fs-rail` 12 px (nhãn thanh dọc) < 14 px ở cả 4 theme: G17 có áp cho nhãn này không.
  3. [Design] theme C: chip "Đáp số khớp tính toán", mã đề HKG-01, tiêu đề bước giải vẫn chữ tay.
  4. [Dev/QC] kiểm font toán + tràn bằng máy mới phủ Kho đề + Chi tiết đề (giáo viên 1440×900); màn học sinh và điện thoại chưa đo.
  5. [Dev] `make traceability` chỉ đọc lượt e2e mới nhất: chạy sau smoke thì S2-E1 hiện 34 "chưa chạy" dù `--strict` exit 0 → gộp kết quả nhiều lượt hoặc đổi thứ tự lệnh kiểm.
  6. [Dev] nhánh cảnh báo máy ngủ trong `app/scripts/e2e.sh` chưa được kích hoạt thử.
- **Nguồn:** báo cáo agent T-44, T-45 (26/11/2026).
- **Xong khi:** contrast.js đạt AA cho `--org-t` ở 4 theme; quyết định G17 ghi PO-DECISIONS; traceability hiện đủ S2-E1 sau khối kiểm.
- **Ngoài phạm vi:** tính năng nghiệp vụ.

## Việc: icon chung cho app theo theme (lệnh owner 26/11/2026 tối)
- **Mục tiêu:** app dùng bộ icon của style A (Material Symbols Rounded đặc) ở cả 4 theme; màu icon theo token của theme đang chọn trong Cài đặt (`currentColor` + token, không màu cứng); font icon tự lưu trong `app/web` (không gọi Google, chạy offline).
- **Nguồn:** `Design/styles/` v0.2 (26/11/2026; `lib.js` bảng tên icon, luật `.ic` trong `<a|b|c|d>/style.css`); DECISIONS 26/11.
- **Xong khi:** `make verify && make web-build && make e2e-affected` xanh; ảnh 4 theme mới trong `artifacts/GEO/evidence/theme/`; grep `fonts.googleapis` trong `app/web/dist` = 0.
- **Ngoài phạm vi:** tính năng nghiệp vụ; tệp của T-20/T-21 đang chạy (làm sau khi hai task gộp).

## Việc: câu mở sau T-20 (S2-E5) · ghi 26/11/2026 đêm
- **Mục tiêu:** đóng các điểm T-20 để mở.
  1. [FIX Dev] TC-GEO-0209 `e2/templates.ui.spec.ts` chập chờn (ô tham số h giữ 3 thay vì 4; T-18 và T-20 đều gặp) → tìm gốc, sửa test hoặc mã.
  2. [L4/Dev] thay trigger AFTER INSERT trên `assignment` bằng lời gọi từ module assignment; thêm `geometry/api/StepCheck` có kiểu thay bean JDK.
  3. [PO/BA] xác nhận quy tắc làm tròn StepCheck (lệch ≤ 0,01 hoặc làm tròn đúng số chữ số); sửa US-S2E5-02-SC2 ("cả bốn bước Đạt" mâu thuẫn SC3).
  4. [Dev, T-22] nối `fn_get_solution` theo phiên bản ghim + "Áp dụng cho lớp"; gắn nhãn vào màn làm bài E3/E4 (hiện ở màn riêng `/hs/nhan-loi-giai/:id`); thêm mục rail `/gv/soat-loi-giai`.
  5. [Dev] traceability: IT/UT hiện "chưa chạy" vì báo cáo JUnit bị xoá khi e2e build lại (cùng gốc với mục 5 của việc sau T-45).
- **Nguồn:** báo cáo agent T-20; `artifacts/GEO/work/epics/S2-E5/qc_findings.md` (26/11/2026).
- **Xong khi:** TC-GEO-0209 đạt 20 lượt lặp liên tiếp; mục 2–3 có quyết định ghi DECISIONS/PO-DECISIONS trước gate T-26.
- **Ngoài phạm vi:** US-S2E5-05 (thuộc T-22).

## Việc: câu mở sau T-21 (S2-E8) · ghi 26/11/2026 đêm
- **Mục tiêu:** đóng các điểm T-21 để mở.
  1. [FIX Dev] thêm `subject.api.AnswerGrader` có kiểu thay bean cầu nối JDK `subjectAnswerGrader` (cùng loại với `geometry/api/StepCheck` của T-20).
  2. [PO/BA] có cho hai bài luyện cùng lớp trùng đề không (hiện CONFLICT, D-GEO-161); có giữ việc ẩn câu bài luyện khỏi "Bài của em" không; QA-S2E8-10 danh sách "câu cần xem lại" cho giáo viên chưa có AC; lời nhắc "viết dạng chính xác (có căn)" hiện với mọi số thập phân Sai.
  3. [Dev] bài luyện chưa có hình 3D, "Xem trước như học sinh" chưa có hình; "đề máy chấm được" chưa nhận đáp số có đơn vị chữ.
  4. [QC] e2e-affected không chạy ui-webkit (iPad) cho spec s2e8; 5 TC Manual cần thiết bị thật; ca kiểm dùng lớp dựng qua móc sandbox, không phải lớp mẫu MVP.
  5. [Dev] các IT để lại lớp/học sinh trong ORG_A (không gây lỗi nhưng làm test phụ thuộc trạng thái chung).
- **Nguồn:** báo cáo agent T-21 và agent sửa IT; `artifacts/GEO/work/epics/S2-E8/qc_findings.md` (26/11/2026).
- **Xong khi:** mục 1–2 có quyết định ghi DECISIONS/PO-DECISIONS trước gate T-26; mục 4 chạy ở lượt regression trọn bộ 6 project.
- **Ngoài phạm vi:** tính năng mới ngoài S2-E8.

## Việc: sau khi gộp icon chung cho app · ghi 26/11/2026 đêm
- **Trạng thái:** việc "icon chung cho app theo theme" ở trên ĐÃ ĐÓNG (gộp 26/11 đêm, nhánh fix-icon).
- **Mục tiêu còn lại:**
  1. [Dev] font CHỮ (Inter, Nunito, IBM Plex, Be Vietnam Pro, Bad Script…) vẫn gọi fonts.googleapis.com trong `app/web/dist/index.html` (có từ trước); icon đã tự lưu. Muốn chạy offline phải tự lưu cả font chữ.
  2. [Design] glyph của màn S2-E8 do Dev tự chọn (rail = pencil, Báo chấm sai = warn, Giữ nguyên = x…); chưa ai xem bằng mắt các màn S2-E8 sau khi gắn icon.
  3. [Dev] sau bản sửa cuối của icon chỉ chạy lượt affected (199/199), chưa chạy lại trọn bộ L2; lượt regression trọn bộ kế tiếp sẽ phủ.
  4. [Design] `Design/styles/GEO-4-huong-style.pdf` (bản gộp) còn icon cũ vì thiếu `pdf-lib`; hai tệp rời `-1-so-sanh.pdf`, `-2-anh.pdf` là bản mới.
  5. [FIX Dev] TC-GEO-0804 `e1/isolation.api.spec.ts` chập chờn (khóa đệm `:mon:` chưa kịp có; chạy lại 39/39).
- **Nguồn:** báo cáo agent icon (26/11/2026), `artifacts/GEO/evidence/theme/icon-*.png`.
- **Xong khi:** grep `fonts.(googleapis|gstatic)` trong `app/web/dist` = 0; Design duyệt ảnh S2-E8 ở 4 theme.
- **Ngoài phạm vi:** tính năng nghiệp vụ.

## Việc: câu mở sau T-23 (S2-E6 + S2-E7) · ghi 26/11/2026 khuya
- **Mục tiêu:** đóng các điểm T-23 để mở.
  1. [Dev, T-22/T-24] gọi `content.api.LibraryQuota.consume` khi tạo đề (giới hạn tần suất thêm đề chưa được nối vào luồng tạo đề).
  2. [FIX Dev] thêm nút "Trình chiếu" vào màn C2-03 (s2e5); hiện nút nằm ở `/gv/kho-de-cua-toi/de/:id`.
  3. [PO/ARCH] Kho chung dùng chung toàn nền tảng hay theo tổ chức (hiện là bản chép `library_problem` theo tổ chức); vai người soát hiện chỉ gán bằng SQL/seed.
  4. [Dev] hỏi đáp AI trong Trình chiếu mới là khung giả lập, chưa nối đường AI.
  5. [FIX Dev] TC-GEO-0539 `e5/catalog.ui` quá giờ trên mobile-iphone (chập chờn, chạy lại đạt).
- **Nguồn:** báo cáo agent T-23; `artifacts/GEO/work/epics/S2-E6/qc_findings.md`, `S2-E7/qc_findings.md` (26/11/2026).
- **Xong khi:** mục 1 có IT "vượt hạn mức thêm đề → bị chặn" qua luồng tạo đề thật; mục 3 ghi PO-DECISIONS trước gate T-26.
- **Ngoài phạm vi:** thêm đề (T-22), bài luyện.

## Việc: câu mở sau T-22 (S2-E3 + US-S2E5-05) · ghi 27/11/2026 00:05
- **Mục tiêu:** đóng các điểm T-22 để mở.
  1. [FIX Dev] bộ lọc chung giới hạn thân yêu cầu 1 MB → ảnh đề tới 5 MB đang gửi thành nhiều đoạn ≤ 1 MB; quyết có nới bộ lọc cho đường tải ảnh đề không.
  2. [L4/Dev] "Chọn để giao" mới ghi nhận xác nhận, chưa tạo bài giao thật; "Dựng tay" mới là ghi chú; nối đề nháp với bài giao thật.
  3. [Design/Dev] rail giáo viên có cả "Đề của tôi" (T-22) lẫn "Kho của tôi" (T-23), chưa gộp; mục rail thêm đề đặt tên "Đề mới" để không vi phạm TC-GEO-0508 (E5 cấm link "Thêm đề" trên màn Kho đề).
  4. [Dev] màn làm bài E3/E4 chưa hiện nhãn và nội dung theo phiên bản ghim; `LibraryQuota.consume` (T-23) chưa được gọi ở luồng tạo đề.
  5. [PO/BA] "Đáp số đề ghi" = phần sau "Đáp số:" ở cuối chữ đề (giả định); dữ liệu AI giả lập để trong mã Java thay vì `resources/ai/` (để e2e-affected không nhảy L2) → xem lại bản đồ ảnh hưởng.
- **Nguồn:** báo cáo agent T-22; `artifacts/GEO/work/epics/S2-E3/qc_findings.md`, `S2-E5/qc_findings.md` mục 15–19 (26/11/2026).
- **Xong khi:** mục 1–3 có quyết định ghi DECISIONS/PO-DECISIONS trước gate T-26; mục 4 có TC e2e.
- **Ngoài phạm vi:** import file, chat chỉnh nháp (S2-E4).

## Việc: câu mở sau T-24 (S2-E4) · ghi 27/11/2026 00:45
- **Mục tiêu:** đóng các điểm T-24 để mở.
  1. [FIX Dev] `DraftService.create` (dán chữ / ảnh, S2-E3) chưa gọi `LibraryQuota.consume` (import đã gọi); ngăn "Của tôi" (S2-E6) liệt kê cả sổ nháp chưa duyệt với nhãn "AI soạn".
  2. [Dev] `impact-map.json` thiếu luật cho `app/server/pom.xml` → task nào đụng pom cũng lên L2.
  3. [PO/BA] nhận đề xuất đổi dữ kiện chỉ vá mô tả hình khi là độ dài cạnh; "Cập nhật khi lưu" = ghi đè nháp cũ; tách bài bằng quy tắc "Bài n." / "Câu n:" (không AI); độ giống = Levenshtein chuẩn hóa.
  4. [Design/Dev] chưa làm phần bản mẫu ngoài AC: màn so sánh C2-04c 3 lựa chọn, "Chỉ áp dụng ý 1", tách/gộp tay.
  5. [Dev] prompt `authoring_chat` chưa đo với AI thật; traceability IT/UT vẫn "chưa chạy" (cùng gốc việc sau T-45).
- **Nguồn:** báo cáo agent T-24; `artifacts/GEO/work/epics/S2-E4/qc_findings.md` (27/11/2026).
- **Xong khi:** mục 1 có IT "vượt hạn mức dán chữ → bị chặn"; mục 3 ghi PO-DECISIONS trước gate T-26.
- **Ngoài phạm vi:** bài giảng với AI (chặng 5).

## Việc: câu mở sau T-25 (S2-E9) và T-42 (seed S3) · ghi 27/11/2026 02:30
- **Mục tiêu:** đóng các điểm hai task để mở.
  1. [FIX Dev] thêm `/api/v1/share/` vào AuthFilter (hiện là bộ lọc riêng ShareLinkPublicFilter); gộp trắc nghiệm vào PracticeSetService/PracticeService (hiện ChoiceGuardInterceptor + đọc practice_attempt); thêm `SubjectEngine.variants` thay bean cầu nối planeVariantGenerator.
  2. [ARCH/PO] liên kết chỉ xem dùng bảng riêng `problem_share_link` thay `share_grant`; ngưỡng chống dò mã 100 lần sai/10 phút/IP; miền số mặc định của biến thể; có cần PDF chữ thật (hiện mỗi trang là ảnh JPEG); khung thiết kế Xuất đề + trang chia sẻ (QA-S2E9-01).
  3. [Dev, T-28] thêm cột `class.academic_year` và cập nhật lớp F-S3; nới chỉ mục `class_member_name_uq` (trùng tên trong lớp — "Hoàng Nam" thứ hai đang dùng name_key kèm mã người dùng).
  4. [PO/BA] lớp 10B "4 em" — seed chọn Mi Na, Bình An (2), Phương Thảo, Chi Mai; giờ hạn/mở lời giải ghi tuyệt đối nên "Sắp mở" chỉ đúng tới 06/12/2026 08:00.
- **Nguồn:** báo cáo agent T-25, T-42; `epics/S2-E9/qc_findings.md` (14 giả định); đầu tệp `app/db/seed/e2e/910_s3_core.sql` (27/11/2026).
- **Xong khi:** mục 2 ghi PO-DECISIONS trước gate T-26; mục 3 làm trong T-28.
- **Ngoài phạm vi:** thiết diện động (Q7).

## Việc: câu mở sau T-29 (S3-E2) · ghi 27/11/2026
- **Mục tiêu:** đóng các điểm T-29 để mở.
  1. [PO] đề "Của tôi" của chính giáo viên CHƯA chọn được vào bộ (fn_assign_practice_set chỉ nhận đề Kho chung có đáp số) — P0 hay FIX sau.
  2. [Dev, T-31] S3-E2 chưa phát DomainEvent khi giao → thông báo "bài mới" (TC-S3E2-14) chờ module notify của T-31; tổng quan giáo viên thật thuộc T-31.
  3. [QC/seed] F-S3-052 chỉ 2 đề (TC ghi 4); mọi đề 's3_tuan' đều "Cần xem lại"; TC-29 "2 / 4" lệch F-S3-051 (4/4). E2E S3-E2 dùng sandbox riêng 's3e2_kho'.
  4. [Dev, T-28/30/31] seed 920 thêm một bộ Đã giao cho lớp 10B (F-S3E2-050) — test đếm bộ của 10B thấy thêm 1.
- **Nguồn:** báo cáo agent T-29; `epics/S3-E2/qc_findings.md` (11 mục, 27/11/2026).
- **Xong khi:** mục 1 ghi PO-DECISIONS; mục 2 làm trong T-31.
- **Ngoài phạm vi:** —

## Việc: câu mở sau T-28 (S3-E1 + S3-E5 02/04) · ghi 27/11/2026
- **Mục tiêu:** đóng các điểm T-28 để mở.
  1. [QC] TC-S3E1-01 phản hồi tạo lớp có thêm `id` — chấp nhận? API lớp ở `/api/v1/teacher/my-classes/**` (vì `/teacher/classes` là của MVP).
  2. [FIX S2-E1?] TC-S3E5-22: màn Đăng ký S2-E1 khoá nút khi chưa tick nên không hiện MSG-S3-84 (máy chủ trả đúng).
  3. [PO] văn bản riêng tư 1.0 sửa tại chỗ hay phát hành 1.1; tập "phải đọc thông báo" nạp lúc khởi động (chuyển tài khoản bằng lệnh MVP chỉ bị chặn sau khi khởi động lại).
  4. [Dev] KHÔNG nới `class_member_name_uq` (đăng nhập tên MVP dựa vào nó): em trùng tên nhận name_key kèm mã người dùng; `fn_student_login` chỉ nhận lớp join_mode='name'.
  5. [Test chập chờn] TC-GEO-0804 (khoá Redis `:mon:`, trượt 2/4 lượt), TC-GEO-0494 (e4 mobile).
  6. [Tổ hợp] T-28 kiểm trên wave0 chưa có T-29 — T-30/T-31 kiểm trên bản kết hợp.
- **Nguồn:** báo cáo agent T-28; `epics/S3-E1/qc_findings.md` (12), `S3-E5/qc_findings.md` (7).
- **Xong khi:** mục 1–3 ghi PO-DECISIONS trước gate T-34; mục 5 FIX riêng.
- **Ngoài phạm vi:** —

## Việc: câu mở sau T-31 lượt 2 (S3-E4 + S3-E5-01 + S3-E6-03) · ghi 27/11/2026
- **Mục tiêu:** đóng các điểm agent A để mở.
  1. [FIX iam, Dev-S3E5-08] iam phát `StudentPasswordResetEvent` SAU commit → ghi AuditEvent lỗi thì mật khẩu vẫn đổi; TC-S3E5-07 chưa đạt trọn (Mở ngay/Đổi giờ quay lui đúng). Cần phát trong giao dịch hoặc outbox.
  2. [PO] giờ yên lặng chỉ áp cho nhắc "sắp hết hạn"; lời giải mở theo lịch chỉ xét bộ có giờ mở trong 1 ngày gần nhất; em đã nhận "Mở ngay" không nhận lần hai; lùi hạn sau khi nhắc → nhắc thêm theo hạn mới.
  3. [BA] MSG-S3-95 "Bộ {tên bộ}" lệch câu ví dụ AC; tạm: tên đã bắt đầu "Bộ " thì không thêm.
  4. [Vận hành] tác vụ nhắc chạy mỗi phút ở mọi profile; tắt bằng `GEO_S3E6_REMINDER_CRON=-`.
  5. [Manual] TC-S3E4-02, 21, 27.
- **Nguồn:** báo cáo agent A; `epics/S3-E4/qc_findings.md`, `S3-E5/qc_findings.md` v0.2 (Dev-S3E5-08…10).
- **Xong khi:** mục 1 FIX trước gate T-34; mục 2–3 ghi PO-DECISIONS.
- **Ngoài phạm vi:** —

## Việc: câu mở sau F3 (S3-E5-03/05 + S3-E6-01/02) và FIX tích hợp · ghi 27/11/2026
- **Mục tiêu:** đóng các điểm còn mở sau khi code chặng 3 đủ.
  1. [FIX Dev] danh sách AuditEvent (S3-E4) JOIN `app_user` → dòng của người đã xoá không hiện (Dev-S3E5-16) → LEFT JOIN.
  2. [FIX Dev] giao qua `practice-sets/{id}/assign` tới lớp đã lưu trữ vẫn NOT_FOUND (fn_assign_practice_set); đường tạo bộ đã trả CONFLICT.
  3. [BA] câu CONFLICT "Lớp này đã lưu trữ, không giao bộ mới được." tự đặt (spec chưa có MSG riêng); ghi chú US-GEO-701-FC7 là "chỉ MVP" (thanh bên chặng 3 có Tổng quan, Lớp học).
  4. [PO] `auth_failure.ip_key` lưu HMAC IP trong CSDL 1 ngày, lệch BR-S3E5-37 (bộ nhớ tạm ≤ 1 giờ) — đang ghi ngoại lệ PO-53; văn bản riêng tư 1.0 sửa tại chỗ hay phát hành 1.1; màn chiếu từ kết quả bản rút gọn, chưa có hình.
  5. [QC] thay đổi hợp đồng: thân có trường thừa nay nhận 400 (BR-S3E5-34) — đã sửa test E1/E7/S2-E1/S2-E8 + test_cases.md; các ca mobile của FIX F3 chưa chạy (regression cuối phase phủ).
  6. [Vận hành] job dọn bia mộ tài khoản xoá chạy hằng ngày; xuất CSV giới hạn 10 lần/giờ.
- **Nguồn:** báo cáo agent F3 + runner-fix-s3int + runner-fix-s3f3; `epics/S3-E5/qc_findings.md` (Dev-S3E5-11…21), `S3-E6/qc_findings.md` (Dev-S3E6-01…08), `E7/qc_findings.md` (Dev-E7-12).
- **Xong khi:** mục 1–2 FIX trước gate T-34; mục 3–4 ghi PO-DECISIONS.
- **Ngoài phạm vi:** chặng 4.

## Việc: FIX lô soát độc lập T-20…T-32 · ghi 27/11/2026
- **Mục tiêu:** đóng 4 major + minor chọn lọc của bản soát; mỗi mục có test.
  1. [major, cần ca thử xác nhận trước] `aigateway/internal/web/ClassAiSwitchFilter.java:41,54,63` khớp regex trên `getRequestURI()` thô → `…/questions;x=1` có thể né tắt AI theo lớp. Dùng đường đã chuẩn hoá (servletPath/PathPattern) hoặc kiểm trong service.
  2. [major] `AuditEventService.java:82-85` (từ `SetReleaseService.java:219-226`) ghi audit "denied" không giới hạn → phình `audit_log` không xoá được. Giới hạn tần suất/gộp.
  3. [major, phòng thủ chiều sâu] `V415:48`, `V416:32` `fn_account_purge_rows` SECURITY DEFINER không `REVOKE … FROM PUBLIC`; cùng `fn_audit_event`, `fn_class_join_request`. Migration mới REVOKE + GRANT đúng vai.
  4. [major] `AccountPurgeJob.java:51` trễ đầu 24 giờ → máy chủ khởi động lại hằng ngày thì job dọn không bao giờ chạy.
  5. [minor ×9] xem báo cáo (chỉ mục `audit_log.actor_user_id`, CSV chống công thức thiếu TAB/CR, `sourcemap: true` bản production, `userCode` trong query string…).
  6. [PO] mục 8 báo cáo: phạm vi US-S3E5-05 có gồm học sinh vào lớp bằng tên không.
- **Nguồn:** `artifacts/GEO/evidence/REVIEW-T20-T32-20261127.md` (Sonnet, 27/11/2026).
- **Xong khi:** make verify xanh; e2e-affected 0 trượt; ca thử mục 1 tái hiện trước khi sửa (hoặc ghi "không tái hiện").
- **Ngoài phạm vi:** tính năng mới; vùng chưa soát (liệt kê cuối báo cáo).

## Trạng thái FIX trước gate T-34 (nhánh fix-t34, gộp wave0 27/11/2026)
- Dev-S3E5-08 (iam event sau commit) **ĐÓNG** adc4a30 · Dev-S3E5-16 (LEFT JOIN app_user) **ĐÓNG** 77ccd29 · assign lớp lưu trữ → CONFLICT **ĐÓNG** 3f1d412 (V417).
- Kiểm: make verify 301 UT (1 bỏ qua) + 243 IT đạt; e2e-affected L1 805/806 (run 20261127-064727), TC-GEO-0605 mobile-iphone chạy lại 1/1 (20261127-065117).
- FIX soát B (fix-rv-b, gộp 27/11): mục 3 REVOKE **ĐÓNG** 1f0e612 (V418); mục 7 chỉ mục **ĐÓNG** db8af83 (V419); mục 5+6 CSV **ĐÓNG** 08c0ad2; mục 9 nhắc việc phân trang **ĐÓNG** a1568bb; mục 11 sourcemap **ĐÓNG** 27b0890. Kiểm: verify 302 UT + 248 IT; web xanh; e2e L2 952/952 (20261127-070819).
- [PO] mục 8: HS vào lớp bằng tên không có mật khẩu → không xoá được tài khoản. Đề xuất: xác nhận bằng gõ "XÓA", không tăng bộ đếm sai.
- [PO] mục 10: HS đọc đề Kho chung theo id — PO-66 G2 đã cho phép (gợi ý, lời giải khoá). Đề xuất: giữ, ghi DECISIONS.
- [PO/BA] mục 12: `userCode` trong query string. Đề xuất: thêm userId (UUID) vào Row/Person, trang bài làm dùng `hs=<uuid>`, cập nhật TC S3-E3 (đổi hợp đồng API).
- [PO] mục 13: gửi lại yêu cầu vào lớp sau Bị từ chối/Đã rời không giới hạn. Đề xuất: tính vào hạn mức 10 lần/15 phút, 30 lần/24 giờ (đổi BR-S3E1-14).
- FIX soát A (fix-rv-a, gộp 27/11): mục 1 ClassAiSwitchFilter **ĐÓNG** 87abfd1 (tái hiện: `;x=1`, `%71uestions`, `%2D` lọt); mục 2 audit denied ≤1 hàng/giờ **ĐÓNG** 1750377; mục 4 AccountPurgeJob cron 03:00 **ĐÓNG** 5507c5d; probeLockedSolution **ĐÓNG** fbc5cce; TC-GEO-0605 **ĐÓNG** 3ff281b (240/240). Verify bản kết hợp A+B 304 UT + 251 IT.
- [PO/BA] BR-S3E5-07 + tên TC-S3E4-23 ("mỗi yêu cầu một bản ghi Từ chối") → nay ≤1 hàng/giờ mỗi (người, loại): chỉnh câu chữ.
- [Web/UX] WebKit di động đôi khi nhận chạm vào `<html>` khi dòng tóm tắt kho đề về muộn làm bố cục dịch → đặt chiều cao tối thiểu cho dòng tóm tắt.
- [Vận hành] lượt e2e ghi đè `artifacts/GEO/evidence/theme/*.png` → cho script e2e ghi ra thư mục khác.
- Câu mở mới: [Test chập chờn] `AssignmentSetS3E2IT.probeLockedSolution` regex LEAK khớp "45" trong giờ "23:45:42" → trượt khi chạy ở phút 45; sửa regex. [Test chập chờn] TC-GEO-0605 mobile-iphone hết giờ chờ UI 1 lần.
