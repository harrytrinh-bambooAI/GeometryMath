Phiên bản: v1.0 · 25/11/2026

# Soát độc lập E7 (Theo dõi lớp, sự kiện) · diff 78a7387..6b7117a
Phạm vi: chỉ đọc. Không chạy make/e2e. Tiêu chí: stories_ac.md (701…704), qc_findings.md (Dev-E7-01…05 là lệch đã biết, không tính lại).
Tổng: blocker 0 · major 3 · minor 8.
Đã kiểm, KHÔNG thấy lỗi: quyền `monitor` (học sinh 403 qua Roles.require; giáo viên lớp khác/trường khác 404 do header() ghép class.teacher_id + org_id); lời giải không vào Monitor/Asked/Event (chỉ id, tên, câu hỏi, số); mọi SQL E7 dùng tham số (không nối chuỗi); đọc event_log có cận `occurred_at >= :from AND < :to`, chỉ mục event_log_timeline_idx (org, assignment, user, occurred_at DESC, id DESC) khớp keyset; /test-hooks/e7 chỉ `@Profile({"dev","test"})`; Q22 lateral theo lớp, không N+1; E3 gửi lại giữ nguyên batchId; thay đổi testid E4/E6 khớp màn mới; bỏ "Tổng quan" chỉ ở vùng gv.

## Major

### [major] M1 · MonitorPage không tải lại sau "Mở ngay"/"Đổi giờ"
- Tệp: app/web/src/features/e7/MonitorPage.tsx:34-46 (chỉ tải một lần), app/web/src/features/e7/LockSlot.tsx:23-34, app/web/src/features/e6/ReleaseCard.tsx:34-41 (không có callback onChanged).
- Mô tả: ReleaseCard tự đọc lại releaseView, nhưng `m` (releaseState, releaseAt, status từng em) giữ nguyên.
- Kịch bản: giáo viên mở bài Sắp mở, bấm "Mở ngay": thẻ E6 biến mất nhưng khung `e7-lock` vẫn còn với dòng "còn 1 giờ 0 phút" cũ; cột Tình trạng vẫn "Đang chờ lời giải" (locked tính ở lần tải trước). Đổi giờ: dòng "còn …" dùng releaseAt cũ, lệch thẻ E6. Trái BR-GEO-269 ("bài Đã mở không có thẻ") ngay sau thao tác.
- Test không bắt được: TC-0703 (monitor.ui.spec.ts:46-58) mở bài qua API rồi `page.reload()`, và gọi /test-hooks/e7/restart; không có ca bấm "Mở ngay" trong trang.
- Sửa: ReleaseCard nhận `onChanged`, MonitorPage gọi `load()`; thêm ca UI bấm Mở ngay rồi kiểm `e7-lock` = 0 và tình trạng đổi.

### [major] M2 · Số phút thao tác hình bị thổi phồng: lô tính nguyên khoảng min…max, không áp quy tắc 30 phút
- Tệp: app/server/src/main/resources/db/migration/V10__schema_mvp.sql:1060-1073 (`_progress_apply`), 1093-1109 (fn_record_batch kẹp thời điểm trong [now-24h, now]); hiển thị ở TrackingService.java:352-364 (`averageMinutes`) và MonitorView.minutes.
- Mô tả: `active_seconds += (p_last - p_first)` của lô, không có ngưỡng nghỉ 30 phút trong lô; chỉ khoảng giữa hai lô mới có ngưỡng. Thời điểm `at` do thiết bị gửi.
- Kịch bản: học sinh (hoặc thiết bị gửi lại lô sau mất mạng dài) gửi một lô hai sự kiện at = now-24h và now: active_seconds +86 400, thẻ "thao tác hình" của cả lớp nhảy theo (1 em 1 440′ / N). Số này trái BR-GEO-278 và hàm `MonitorView.activeSeconds` (đúng quy tắc, nhưng không được dùng để hiển thị).
- Sửa: tính trong hàm SQL theo từng cặp liên tiếp (chỉ cộng khoảng < 30 phút), hoặc kẹp tổng mỗi lô (ví dụ ≤ 60 giây × số sự kiện); thêm IT cho lô 2 sự kiện cách 23 giờ.

### [major] M3 · Lũy đẳng lô (BR-GEO-280) chỉ nằm ở Redis, có cửa sổ cộng đôi
- Tệp: app/server/src/main/java/vn/geo/server/tracking/internal/service/FigureEventService.java:~88-112 (`recordTools`); tăng rủi ro bởi app/web/src/features/e3/useToolEvents.ts:35-47 (gửi lại mỗi 5 s do E7).
- Mô tả: khóa `pending` TTL 2 phút; `DONE` chỉ ghi SAU khi DB đã commit. Nếu `redis.set(DONE)` lỗi hoặc tiến trình chết sau commit, khóa còn `pending`; máy khách nhận 5xx/409 và thử lại mỗi 5 s; hết 2 phút khóa tự hết, lô cùng batchId được ghi lần hai: tool_count và active_seconds cộng đôi. Mất/xóa khóa Redis (flush, evict) cũng cộng đôi. NFR-GEO-052 (đối soát) chưa có (qc_findings QF-E7-05 nói là đề xuất).
- Sửa: đưa batchId vào DB (bảng durable_request như fn_record_durable, `ON CONFLICT DO NOTHING` trong cùng giao dịch với INSERT event_log), giữ Redis chỉ làm đường nhanh.

## Minor

### [minor] m1 · useToolEvents: hàng đợi có thể kẹt; thử lại vô hạn mọi lỗi
- Tệp: app/web/src/features/e3/useToolEvents.ts:19-31, 35-47.
- flush(false) luôn `clearTimeout(timer)`; khi `retry` còn lô thì `queue` không được gửi (điều kiện `batches.length === 0`) và timer không được đặt lại. `retryNow` chỉ chạy khi `retry` không rỗng, nên sau khi `retry` cạn, sự kiện còn trong `queue` đứng đến khi có thao tác mới hoặc `pagehide`. Kịch bản: mất mạng, thao tác A (lô lỗi vào retry) rồi B trong lúc retry còn lô; có mạng lại thì A gửi, B kẹt đến lần thao tác sau. Test TC-0729 qua được vì hai thao tác tạo hai lô riêng (đua thời gian).
- `apiRequest` từ chối mọi mã không 2xx: lô gặp 400/403/404/409 (học sinh rời lớp, bài đổi) bị gửi lại mỗi 5 s đến khi đóng trang.
- Sửa: sau flush nếu `queue` còn thì đặt lại timer (hoặc `retryNow` kiểm cả queue); chỉ thử lại lỗi mạng/5xx, bỏ lô khi 4xx (trừ 409 pending).

### [minor] m2 · E7SandboxService/E7SandboxRepository không có @Profile
- Tệp: app/server/src/main/java/vn/geo/server/tracking/internal/service/E7SandboxService.java:11, .../repo/E7SandboxRepository.java:~18.
- E6/E4 gắn `@Profile({"dev","test"})` cho cả Service và Repository; E7 chỉ gắn controller. Ở prod hai bean ghi trực tiếp app_user/class/assignment_progress/qa_turn vẫn được tạo (không có đường HTTP, nhưng sai quy ước "móc không lọt prod"). Controller phụ thuộc bean không-profile không bị lỗi khởi động nên không ai phát hiện.
- Sửa: thêm `@Profile({"dev","test"})`; thêm kiểm kiến trúc/IT đếm bean `E7*` ở profile prod.

### [minor] m3 · Cắt cụt im lặng: lớp trên 100 em, trên 2000 câu hỏi
- Tệp: app/server/src/main/java/vn/geo/server/tracking/internal/repo/TrackingRepository.java:275 (`LIMIT 101`), .../repo/MonitorRepository.java (`questions`, `ORDER BY q.id LIMIT 2000`), TrackingService.java:339-345.
- Lớp > 101 em: bảng và N (n/N, thẻ trung bình) sai, không báo. Bài có > 2000 câu hỏi: truy vấn lấy 2000 câu CŨ nhất, nên danh sách "20 câu mới nhất" và nhóm "đang hỏi gì" thiếu câu mới.
- Sửa: lấy `ORDER BY q.id DESC LIMIT 2000` rồi đảo; thống nhất trần số học sinh với quy định lớp (nếu ≤ 100 thì ghi vào ràng buộc); thêm ca ở ranh giới.

### [minor] m4 · questionCount và nhóm câu hỏi lấy từ hai nguồn khác nhau
- Tệp: TrackingService.java:359 (tổng ask_count của học sinh còn trong lớp) so với MonitorView.groups(asked) (mọi qa_turn của bài, gồm học sinh đã rời lớp).
- Kịch bản: em An rời lớp: thẻ "câu hỏi" = 4 nhưng khối "đang hỏi gì" tổng lượt = 7. Trái BR-GEO-266/275 khi đối chiếu số.
- Sửa: một nguồn (đếm qa_turn lọc thành viên còn lại) hoặc ghi rõ quy ước.

### [minor] m5 · Đọc sự kiện: thiếu test phân quyền, hai chỗ lệch nhỏ
- Tệp: app/server/src/main/java/vn/geo/server/tracking/internal/service/ClassMonitorService.java:87-100; MonitorRepository.java (`header`, không lọc `archived_at`, còn `assigned` lọc).
- Không có ca nào gọi `/teacher/assignments/{id}/students/{userId}/events` bằng học sinh, giáo viên lớp khác, giáo viên trường khác (grep trong e2e/e7 và server test chỉ thấy happy path và con trỏ hỏng); đường này trả dữ liệu học sinh nên cần ca 403/404 (theo mẫu TC-0710/0719).
- `userId` không kiểm là thành viên lớp: trả trang rỗng 200 cho mọi UUID (không rò dữ liệu vì lọc theo assignment_id + user_id, nhưng nên 404).
- Bài đã lưu trữ vẫn xem được qua id nhưng không có trong danh sách: nên thống nhất.

### [minor] m6 · sessionReport (M-GEO-08) chưa có đường vào, truy vấn nền không có chỉ mục phục vụ
- Tệp: ClassMonitorService.java:103-110, MonitorRepository.java (`eventsForSessions`).
- Không controller nào gọi (chỉ IT); chỉ mục timeline bắt đầu bằng (org_id, assignment_id) nên lọc org_id + occurred_at quét các partition của khoảng; `LIMIT 200000 ORDER BY occurred_at` cắt bỏ sự kiện mới nhất mà không báo, sai tỉ lệ phiên khi dữ liệu lớn. Lint EVENT_TIME_BOUND cho qua vì có `occurred_at >=`.
- Sửa: ghi rõ "chưa phơi ra" ở status.md hoặc thêm điểm cuối + tính theo SQL (window function) / theo từng bài; không cắt im lặng.

### [minor] m7 · Test yếu hoặc đánh lừa
- app/e2e/tests/e7/monitor.ui.spec.ts:46-58 (TC-0703) và events.api.spec.ts (TC-0723/0743): "khởi động lại máy chủ" = xóa khóa Redis `mon` (E7TestHookController.java:~139-143); không mô phỏng được mất trạng thái tiến trình. Kết quả đúng vì trạng thái đã ở CSDL, nhưng tên ca và Dev-E7-02 nói nhiều hơn điều được kiểm; khóa `figbatch` (M3) không bị xóa nên không kiểm lũy đẳng sau khởi động lại.
- SqlLint.java: `TIME_BOUND = \boccurred_at\s*(>=|>|between)` thỏa bởi bất kỳ chỗ nào có `occurred_at >` (kể cả trong ORDER BY/SELECT), không bảo đảm cận áp vào WHERE của event_log.
- TC-0712 (EventsE7IT) chỉ thử 5 và 34 học sinh, không phủ trần 101 (m3).

### [minor] m8 · Chi tiết web/định tuyến
- app/web/src/app/router.tsx:40-48: `childRoutes` khóa theo `r.path ?? ''`; route `index` (không có path) của hai epic sẽ đè nhau im lặng (hiện chưa có). `overview()` vẫn đếm cả màn bị thay (Theo dõi tính 2 màn E1+E7 trên trang tổng quan). Trình tự feature quyết định route nào thắng, chưa có test.
- app/web/src/features/e7/MonitorPage.tsx: không tự làm mới (bộ đệm 5 s phía máy chủ gợi ý có thăm dò); giáo viên phải F5 để thấy em mới mở bài.
- ClassMonitorController.java:~26-36: tham số `from`/`to` kiểu Instant chỉ nhận ISO "Z"; dạng `+07:00` trả 400 (dấu `+` chưa mã hóa còn thành khoảng trắng). Kẹp khoảng [from,to) không giới hạn bề rộng: `from=1970` quét mọi partition (chỉ 50 dòng nhờ chỉ mục, nhưng nên đặt trần, ví dụ 366 ngày).
- BR-GEO-267 "xếp theo tên A → Z": SQL xếp theo `name_key` toàn tên (họ đứng đầu); dữ liệu vàng toàn tên đơn nên test không phân biệt. Cần BA xác nhận "tên" là tên gọi hay họ tên.
