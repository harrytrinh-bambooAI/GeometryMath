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
- Mới, còn MỞ (ngoài phạm vi FIX1): `BenchmarkE1IT.tc0148` thay chuỗi "Tính góc giữa SD và mặt đáy." không khớp XML (chữ có dấu ở dạng thực thể) nên không tạo phiên bản mới như chú thích nói; ca vẫn đạt nhưng kiểm yếu hơn ý định.
- Mới, còn MỞ: trace/video của dự án UI vẫn chứa JWT (mục D chỉ phạm vi http.json + ảnh bảng).

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
- **Nguồn:** bản soát độc lập E2 (Sonnet) 23/11/2026.
- **Xong khi:** như FIX2 ở trên; thêm IT với kịch bản AI giả lập trả solid lạ / thiếu điểm / số dạng chuỗi.
