Phiên bản: v1.0 · 25/11/2026

# Soát độc lập E6 · `git diff 5278808..2f4c5d0` (nhánh worktree-mvp-wave0)
Phạm vi đã đọc: V60, AnswerGuard/AnswerRules/GuardRepository, Release*/Preview*/Probe*/Assignment*/LockState*/TrackingService (diff), hook test, SolutionRateLimiter, web e6 + SolutionTab, seed 600/990, e2e lock.api, test AnswerGuardShort.
Không chạy make/e2e. Không đọc đủ: ui.spec (assign/release/mine/preview), guard.api.spec, E6SandboxService, DataE6IT/ReleaseE6IT (chỉ grep).
Tổng: 0 blocker · 3 major · 7 minor. Hook /test-hooks đều có `@Profile({"dev","test"})`, ProfileGuard cấm prod+dev: không phát hiện lọt prod.

## Major

### [major] M1 · `assign-options` không kiểm bộ đề (bank): đề ngoài bộ đề không trả 404
- Tệp: app/server/src/main/java/vn/geo/server/assignment/internal/service/ReleaseService.java:62-70 (`options`), ReleaseController.java:37-40; repo ReleaseRepository.java `catalogProblem` (không có `fn_catalog_bank_visible`).
- Mô tả: `AssignmentService.create`, `PreviewService.page/ask` đều gọi `bank.requireVisible` (FIX3), nhưng `GET /api/v1/teacher/problems/{versionId}/assign-options` thì không.
- Kịch bản: giáo viên trường không được cấp bộ đề X gọi assign-options với versionId của đề trong X: nhận 200 kèm code, title, statement của đề (và thông tin bài giao hiện có ở lớp mình). Trái "đề ngoài bộ đề = 404" (BR-GEO-112/FIX3).
- Sửa: `bank.requireVisible(versionId)` ở đầu `ReleaseService.options` (và `previewLock` cho chắc); thêm TC e2e/IT: giáo viên không có bộ đề gọi assign-options → 404. Hiện không test nào phủ (e6.ts:136 chỉ gọi với đề hợp lệ).

### [major] M2 · "Ngay bây giờ" là hai lệnh gọi, mỗi lần bấm lại tạo assignmentId mới
- Tệp: app/web/src/features/e6/AssignPage.tsx:52-72.
- Mô tả: `assign` (releaseAt=null) rồi `setRelease({openNow:true})`; `assignmentId: crypto.randomUUID()` được sinh trong `submit` mỗi lần.
- Kịch bản: (a) assign thành công, setRelease lỗi mạng/CONFLICT: UI báo lỗi, nút bật lại; bấm lại → id mới → `CONFLICT_DUPLICATE` hiện MSG-42 "Đề này đã được giao cho lớp …" trong khi bài đang ở "Chưa đặt giờ", giáo viên tưởng đã mở. (b) assign quá thời gian chờ nhưng đã ghi: thử lại cũng ra MSG-42; cơ chế lũy đẳng theo id của `fn_create_assignment` (DUPLICATE) không bao giờ dùng được từ UI. Vi phạm BR-GEO-232 (giờ mở = giờ máy chủ lúc giao) khi lỗi giữa chừng.
- Sửa: giữ `assignmentId` và `releaseRev` trong `useRef` theo trang; nếu đã tạo thì lần bấm sau chỉ gọi `setRelease`; hoặc cho `fn_create_assignment` nhận cờ open_now (hiện `p_release_at <= now()` bị VALIDATION_PAST).

### [major, có thể] M3 · Bộ câu thử chạy sai đề với câu gắn đề khác; phát hiện lộ chỉ bằng chuỗi nguyên văn
- Tệp: app/server/src/main/java/vn/geo/server/assignment/internal/service/ProbeRunner.java:100-112 (`target = set.get(0).targetCode()`, `Tally(probes.leakStrings(set.get(0).code()))`), `Tally.check` (`body.contains(s)`); ProbeService.load cho nạp câu với targetCode bất kỳ (ProbeService.java:67-80).
- Mô tả: mọi câu kênh `qa`/`teacher_route` hỏi trên bài BG-1 của đề của câu đầu tiên và so với chuỗi lộ của đề đó, bỏ qua `targetCode` của từng câu. Hiện 22 câu seed đều thuộc HKG-01 nên chưa lộ lỗi; câu nạp thêm cho đề khác (BR-GEO-261 cho phép) sẽ được chấm trên đề sai, báo "đạt" giả. Ngoài ra chỉ đếm chuỗi nguyên văn (đáp số, answer_forms, thân bước) nên lỗi hồi quy ở R3 (làm tròn "0,79"), chữ số viết tay, "15 pi" không bị bắt: verdict "đạt" yếu hơn BR-GEO-252.
- Liên quan: chỉ HKG-01 có `answer_forms` trong seed (600_e6_core.sql:13); các đề khác (vd 15π của HKG-03/04) chỉ có dạng sinh từ `final_answer`, "15 pi", "15·π", "mười lăm π" lọt R2/R3 (AnswerRules.java forms/values). Nếu answer_forms các đề khác do E5/E4 nạp thì bỏ qua ý này, cần xác nhận.
- Sửa: chạy mỗi nhóm câu theo `targetCode` (tạo BG tương ứng), `Tally` theo đề; thêm so khớp bằng `AnswerRules` (hoặc giá trị số) vào đếm lộ; thêm câu thử cho ≥1 đề khác HKG-01.

## Minor

### [minor] m1 · R1 chặn cả câu hỏi khái niệm
- AnswerRules.java:53-58 (`ASKS_ANSWER`: "radian", "vuong can", "chu so", "don vi la gi", "co phai .*khong", "the angle", "bao nhieu do").
- Kịch bản: học sinh hỏi "Radian là gì?" hoặc "Tam giác vuông cân có tính chất gì?" khi lời giải khóa → MSG-GEO-35. BR-GEO-252/253 cho phép giải thích khái niệm; chỉ chặn câu đòi đáp số/bước. Sửa: gắn từ khóa với động từ đòi kết quả, hoặc bỏ R1 cho các từ khái niệm đơn.

### [minor] m2 · R5 chặn nhầm gợi ý bình thường
- AnswerRules.java `confirms` (CONFIRM_HEAD có "da", "vang", "phai", "khong"; COMPARE tìm "lớn hơn/nhỏ hơn" ở bất kỳ chỗ nào của câu trả lời) khi câu hỏi chứa một chữ số bất kỳ.
- Kịch bản: hỏi "Câu 2 em nên bắt đầu từ đâu?" (có chữ số), AI trả "Phải dựng hình chiếu của SD…" hoặc "Em so sánh SD lớn hơn AD…" → bị chặn dù không xác nhận giá trị nào. Sửa: chỉ xét khi câu hỏi nêu một giá trị cho đại lượng đề hỏi (so với values/forms).

### [minor] m3 · R2 khớp chuỗi con của dạng ngắn
- AnswerRules.java:77-80 (`fn.length() >= 2 && rn.contains(fn)` trên chuỗi đã bỏ khoảng trắng; dạng "45" sinh từ bỏ đơn vị ở `forms`).
- Kịch bản: lời giải khóa "45°", AI trả "cạnh 450 mm"/"năm 2045" → R2 chặn. Chỉ gây chặn thừa (an toàn), nhưng làm gợi ý AI tự học bị loại (`leaks`). Sửa: khớp theo ranh giới số cho dạng toàn chữ số.

### [minor] m4 · R3 không bắt số cắt cụt
- AnswerRules.java:133-139: dung sai chỉ `0.5·10^-decimals` quanh giá trị; "0,78" cho π/4 = 0,7854 (cắt, không làm tròn) và "0,7" (sig=1) không bị chặn.
- Kịch bản: AI trả "xấp xỉ 0,78 rad" khi khóa. Sửa: dùng `|x-v| < 10^-decimals` cho số ≥2 chữ số có nghĩa (D-GEO-040/184 cần chốt dung sai).

### [minor] m5 · Thiếu @NoStore ở vài kênh dữ liệu bài
- GeometryController.java:68 (`/student/assignments/{id}/scene`), :73 (`/teacher/problems/{versionId}`), HelpController.java:49 (scene-shown, không nhạy). BR-GEO-302 yêu cầu no-store cho phản hồi có lời giải, gợi ý hoặc trạng thái khóa; scene hiện không phụ thuộc khóa nên rủi ro thấp, nhưng BR-GEO-249 (11) "mọi phản hồi lưu đệm". Sửa: thêm `@NoStore` cho scene học sinh.

### [minor] m6 · `990_demo_prune.sql` dùng `SET LOCAL` ngoài giao dịch khi chạy theo hướng dẫn trong tệp
- app/db/seed/demo/990_demo_prune.sql:1-8: nêu `psql -f` (không `-1`, không BEGIN) thì `SET LOCAL session_replication_role` vô hiệu, các trigger chỉ-thêm chặn DELETE, script lỗi. Test DataE6IT.tc0604 chạy trong giao dịch nên không bắt được. Sửa: bọc `BEGIN; … COMMIT;` trong tệp. Ngoài ra `kind = 'group'` xóa mọi tổ chức nhóm, ổn với DB demo nhưng nên nêu rõ không chạy trên DB có dữ liệu thật.

### [minor] m7 · Chi tiết nhỏ khác
- SolutionRateLimiter.java: map `hits` không bao giờ dọn (mỗi học sinh một Deque tồn tại mãi); giới hạn theo nút, nhiều nút thì mỗi nút 5 yêu cầu. Chỉ áp cho `GET /solution`, không áp cho `POST /solution/open` (cùng nội dung).
- ReleaseService.previewLock (ReleaseService.java:~108-118): giờ chọn đã qua (ví dụ cuối ngày) trả state "open" cho đề chưa giao; BR-GEO-246 chỉ định thẻ khóa MSG-27 với giờ chọn. Nên trả 422 hoặc "unset".
- PreviewService.ask: `highestHint` do client gửi, trả nguyên văn trong MSG-35 (không giới hạn độ dài, không kiểm là gợi ý của đề); không giới hạn tần suất gọi AI cho giáo viên.
- ProbeService.load: tăng phiên bản không có khóa (hai lần nạp đồng thời cùng version+1 sẽ lỗi PK); `channel` không kiểm trước khi chạm CHECK (500 thay vì 422). Chỉ dùng ở dev/test.
- Chưa có test đơn vị thuần cho AnswerRules (R1…R6, biên làm tròn); chỉ có AnswerGuardShortAnswerTest và e2e. TC khó bắt các lỗi m1-m4.
- `geo.ai.provider: mock` là mặc định trong application.yml và ProfileGuard không cấm mock ở prod; không thuộc E6 nhưng khiến hỏi đáp prod trả câu giả lập nếu quên cấu hình.

## Đã kiểm, không thấy lỗi
- Khóa ở CSDL: `fn_solution_gate` dùng `release_at > now()` / `<=` nhất quán với `fn_set_release`, `classOptions`, `release()`; học sinh lớp khác/ngoài trường → NOT_FOUND; chưa mở → FORBIDDEN (TC-0664/0665 đúng ý).
- `fn_set_release`: khóa hàng, kiểm giáo viên của lớp, CONFLICT_ALREADY_OPEN trước CONFLICT_REV (đúng BR-238/239), audit đúng một dòng khi thành công; Java chặn "không có thao tác xóa giờ".
- `fn_create_assignment` (V59) giữ `fn_catalog_bank_visible`; lớp giáo viên khác cùng trường ánh xạ NOT_FOUND; ràng buộc duy nhất bắt race (unique_violation).
- SQL: không nối chuỗi với dữ liệu người dùng (mọi tham số bind); không thấy SQL injection. Hàm DEFINER mới chỉ GRANT cho geo_ai / geo_ops, REVOKE PUBLIC.
- `fn_guard_context` chỉ trả đáp số khi khóa và chỉ cho server; không hàm nào trả cho thiết bị. Guard đóng khi lỗi (GuardFailure → DEPENDENCY_UNAVAILABLE, không lưu câu AI). Câu bị chặn lưu bản thay thế, không lưu bản gốc.
- Xem trước: không gọi `fn_get_solution*`; chỉ `fn_guard_context_catalog` phía máy chủ (giáo viên mẫu/đã xác minh).
- Hook test: `E6TestHookController`, `E6GuardTestHookController`, `AiTestHookController`, `E6SandboxService/Repository`, `ProbeRunner` đều `@Profile({"dev","test"})`. `GuardFaults.bypass/MockAiControl` tồn tại ở prod nhưng không có đường ghi (tập rỗng).
