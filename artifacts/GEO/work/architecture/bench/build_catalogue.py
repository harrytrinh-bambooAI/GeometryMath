#!/usr/bin/env python3
"""Dựng 04_query_catalogue.md từ: (1) SQL thật trong bench/queries, (2) kế hoạch EXPLAIN (ANALYZE, BUFFERS) thật trong bench/results,
(3) mô tả viết tay dưới đây. Chạy lại sau mỗi lần chạy bench/run_queries.sh:  python3 bench/build_catalogue.py"""
import os, re, glob

BASE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(BASE)

# (tiêu đề, ai gọi, tần suất ở tải đều 333 yêu cầu/giây, chỉ mục phục vụ, phân trang/giới hạn, độ phức tạp, ghi chú)
META = {
 'Q01_login': ('Đăng nhập nhẹ bằng tên + mã lớp (IAM-01)', 'Học sinh (trước khi có thẻ); sau khi thẻ hết hạn là làm mới', '≈ 7/giây (2%); đỉnh đầu giờ học cao hơn nhưng nhẹ',
   '`class_join_code_uq`, `class_member_name_uq`', 'Không (tra khóa duy nhất, tối đa 1 hàng)',
   'O(log N) hai lần tra chỉ mục duy nhất', 'Là câu đọc bên trong `geo.fn_student_login` (D-GEO-127): ứng dụng gọi hàm, không chạy câu này trực tiếp. Không có học sinh khớp tên thì hàm tạo đúng một `app_user` student/light + `class_member` (D-GEO-106, 158); `geo_app` không có INSERT trên hai bảng đó. Ngoại lệ org_id đứng đầu ghi ở chỉ mục (D-GEO-051).'),
 'Q02_student_assignments': ('Bài của em: danh sách bài giao theo các lớp em đang học (LRN-01)', 'Học sinh, màn "Bài của em"', '≈ 27/giây (8%)',
   '`class_member_user_idx`, `assignment_class_idx`, `assignment_progress_pkey`, các khóa chính `problem_version`, `problem`', 'Cursor = `assignment.id` (UUIDv7, mới nhất trước, `id < cursor`); trang 20 (LIMIT 21), trần 50',
   'O(k·(log A + 21·log N)); k = số lớp của em (≤ 8). Mỗi lớp lấy riêng 21 dòng rồi gộp, vì một học sinh thuộc nhiều lớp mà vẫn tránh sắp xếp toàn bộ',
   'Không N+1: một câu lệnh nối `problem_version`, `problem`, `assignment_progress` bằng LATERAL/JOIN. Trạng thái giờ mở tính bằng đồng hồ CSDL (D-GEO-041). Nút Sort cuối chỉ sắp ≤ 21·k dòng đã có.'),
 'Q02b_student_self_assignments': ('Bài tự học của em', 'Học sinh, tab Tự học', '≈ 3/giây (một phần của 8% danh sách)',
   '`assignment_self_idx`', 'Cursor = `assignment.id`; trang 20, trần 50', 'O(log A + 21·log N)', '`lock_source_assignment_id` chỉ là dấu vết lúc tạo (D-GEO-151); trạng thái khóa thật tính ở cổng mỗi lần xin.'),
 'Q03_class_assignments': ('Bài của lớp (giáo viên): kèm số em đã mở và số em đã xem lời giải', 'Giáo viên, màn lớp / chọn bài để theo dõi', '≈ 3/giây (một phần của 3% theo dõi lớp)',
   '`assignment_class_idx`, `assignment_progress_pkey` (đếm theo khoảng), `class_teacher_idx`', 'Cursor = `assignment.id` (mới nhất trước); trang 20, trần 50',
   'O(21·(log N + c)) với c ≤ cỡ lớp (đếm quét khoảng chỉ mục của MỘT bài)', 'Đếm `count(*)` bị cấm trên bảng lớn, nhưng ở đây phạm vi là một bài (≤ cỡ lớp hàng), có `lint-allow` ghi lý do. Kiểm quyền: giáo viên của lớp (`c.teacher_id`).'),
 'Q04_assignment_detail': ('Chi tiết bài cho học sinh: đề + bản dựng hình, KHÔNG lời giải', 'Học sinh, mở một bài (E4, E6)', '≈ 33/giây (10%)',
   '`assignment_org_id_id_key`, `class_member_pkey` (EXISTS), khóa chính `problem_version`/`problem`, `scene_build_org_id_problem_version_id_build_no_key`', 'Không (1 hàng)',
   'O(log N) × 5 lần tra chỉ mục', 'Cột trả về chỉ gồm `problem_version`, `scene_build` (không có bảng `solution`; vai `geo_app` cũng không đọc được bảng đó), thêm `hidden_measures` (biểu thức đại lượng công cụ đo phải ẩn, không có giá trị; D-GEO-024, 133). Tọa độ là dữ kiện của đề (D-GEO-154). Nối đề bằng `(content_org_id, problem_version_id)` (RV-12). Nội dung bất biến theo `problem_version_id`: đệm Redis theo khóa phiên bản, phần phụ thuộc người (giờ mở, quyền) vẫn tra CSDL.'),
 'Q04b_release_state': ('Trạng thái giờ mở (thăm dò nhẹ của trang đang mở, D-GEO-074)', 'Học sinh, trang đang chờ lời giải', 'Thường trực khi trang chờ khóa, mỗi 6 + [0; 1,5) giây (D-GEO-157): ca xấu ≈ 148/giây HTTP; ≈ 17/giây xuống CSDL nhờ đệm `release_at` 2 giây theo bài (`05_capacity.md` §2)',
   '`assignment_org_id_id_key`, `class_member_pkey`', 'Không (1 hàng)', 'O(log N) × 2', 'Trả `release_in_seconds` để thiết bị đặt hẹn giờ, không dùng giờ thiết bị. Chỉ được đệm `release_at` (≤ 2 giây, xóa khi đổi giờ); KHÔNG đệm và KHÔNG trả quyết định "được xem" (D-GEO-157, RV-09): xem lời giải luôn gọi `fn_get_solution`.'),
 'Q05_hint': ('Lấy gợi ý theo mức (BR-04)', 'Học sinh, tab Gợi ý', '≈ 27/giây (8%)',
   '`assignment_org_id_id_key`, `hint_pkey`, `class_member_pkey`, `assignment_progress_pkey`', 'Không (1 hàng)', 'O(log N) × 5',
   'Phần đọc của `geo.fn_get_hint` cho bài lớp: bài cho phép gợi ý, em thuộc lớp, mức ≤ mức đã xem + 1 (D-GEO-108). Hàm ghi sự kiện `hint` + tiến độ trong cùng giao dịch, lũy đẳng theo `request_id` (đo đủ ở Q05c); đây là đường DUY NHẤT nâng `hint_level_max` (RV-10 (4)). Bài tự học: Q05b.'),
 'Q05b_hint_self_inherited': ('Gợi ý của bài tự học: đề kho nếu kế thừa khóa, đề tự nhập nếu không (RV-15, D-GEO-155)', 'Học sinh, tab Gợi ý của bài tự học (qua `fn_get_hint`)', '≈ 2/giây',
   '`assignment_self_idx`, `assignment_org_id_id_key`, `hint_pkey` + Q16 bên trong `fn_self_lock_source`', 'Không (1 hàng)', 'O(log N) × 3 + Q16',
   'Nhánh tự học còn thiếu ở catalogue trước (RV-15). Bài kế thừa khóa nhận gợi ý của đề kho do Giáo viên mẫu soạn, không phải gợi ý AI (D-GEO-155); kiểm kế thừa ở mỗi lần xin (D-GEO-151). Ca đo: bài tự học đang kế thừa khóa.'),
 'Q05c_get_hint_call': ('Gọi `geo.fn_get_hint` đầy đủ (đọc + ghi bền + tiến độ)', 'Học sinh, tab Gợi ý', '≈ 27/giây (8%)', 'như Q05 + `durable_request_pkey`, `event_log_*`, `assignment_progress_pkey`', 'Không (1 hàng)', 'O(log N) × 8',
   'Một lần gọi, một giao dịch (mức B, commit đồng bộ): khóa lũy đẳng `request_id`, sự kiện `hint`, upsert tiến độ. Kế hoạch chỉ hiện Function Scan; thời gian là toàn bộ thân hàm.'),
 'Q06_solution_gate_body': ('Lấy lời giải có kiểm soát giờ mở (BR-01, ANS-02): thân của `geo.fn_get_solution`', 'Học sinh, tab Lời giải (qua hàm); đo thật bằng pgbench', '≈ 17/giây thường xuyên; đỉnh giờ mở +100/giây (M-GEO-12)',
   '`assignment_org_id_id_key`, `class_member_pkey`, `solution_pkey`', 'Không (1 hàng)', 'O(log N) × 4',
   'Chạy với vai chủ vì `geo_app` KHÔNG đọc được `solution`. Ứng dụng gọi `SELECT status, ... FROM geo.fn_get_solution(:org,:user,:assignment)` (Q06b, Q06c): luôn 1 hàng, `status` ∈ OK / LOCKED (403 FORBIDDEN) / NOT_FOUND (404) / NO_SOLUTION (RV-21). Đề của bài lớp luôn là đề kho (`content_org_id` = nền tảng).'),
 'Q06b_get_solution_call': ('Gọi `geo.fn_get_solution` cho bài lớp đã mở (đường nóng nhất ở đỉnh giờ mở)', 'Học sinh, tab Lời giải', '≈ 17/giây; đỉnh +100/giây', 'như Q06', 'Không (1 hàng)', 'O(log N) × 4',
   'plpgsql: `fn_solution_gate` (tra bài, EXISTS tư cách lớp, giờ mở) rồi đọc `solution`. Thời gian gồm cả gọi hàm lồng; pgbench riêng ở `07_verification.md` §4.6.'),
 'Q06c_get_solution_self_call': ('Gọi `geo.fn_get_solution` cho bài tự học kế thừa khóa (đã xem Gợi ý 2)', 'Học sinh, tab Lời giải của bài tự học', '≈ 1/giây', 'như Q06 + `problem_signature_pkey`, `assignment_progress_pkey` + Q16', 'Không (1 hàng)', 'O(log N) × 6 + Q16',
   'Chạy đủ nhánh tự học: chữ ký có, Gợi ý 2 đã xem, Q16 tìm thấy bài lớp đang khóa ⇒ `LOCKED` (D-GEO-150, 151).'),
 'Q07_event_batch_insert': ('Ghi sự kiện hàng loạt (PRF-01): lô 50 sự kiện của một học sinh', 'Học sinh (thiết bị gom, gửi ≤ 10 giây/lô)', '≈ 117 lô/giây (35%), tức ≈ 3 lô/giây/100 phiên',
   '`event_log_*_org_id_assignment_id_user_id_occurred_at_id_idx` (một chỉ mục btree mỗi partition)', 'Không (ghi)', 'O(m·log N) với m = số dòng trong lô; một câu lệnh, một vòng mạng',
   'Câu INSERT bên trong `geo.fn_record_batch` (vai chủ: `geo_app` không ghi thẳng `event_log`, D-GEO-158). Có EXISTS tư cách lớp không tương quan, tính một lần (RV-26): bài của lớp khác ⇒ 0 hàng, hàm trả NOT_FOUND. Chỉ mức A (`tool`, `scene_shown`). Commit không đồng bộ (ADR-GEO-11). Số chỉ mục trên bảng ghi nhiều: **1**. Gọi cả hàm: Q07b.'),
 'Q07b_record_batch_call': ('Gọi `geo.fn_record_batch` đầy đủ: lô 50 sự kiện + tiến độ', 'Học sinh (thiết bị gom, ≤ 10 giây/lô)', '≈ 117 lô/giây (35%)', 'như Q07 + `assignment_progress_pkey`', 'Không (ghi)', 'O(m·log N)',
   'Truy vấn chậm nhất của catalogue (≈ 0,14 ms), vẫn ≪ 10 ms (NFR-GEO-007). Ứng dụng đặt `SET LOCAL synchronous_commit = off` trước khi gọi.'),
 'Q08_progress_upsert': ('Cập nhật tiến độ (bộ đếm phi chuẩn hóa) sau mỗi lô', 'Mọi hàm ghi sự kiện (một chỗ duy nhất: `geo._progress_apply`)', '≈ 117/giây lô + ≈ 77/giây sự kiện bền',
   '`assignment_progress_pkey`', 'Không (ghi)', 'O(log N), HOT update (không cột nào của bảng nằm trong chỉ mục ngoài khóa chính)',
   'UPSERT, lũy đẳng với `hint_level_max` (GREATEST) và `solution_viewed_at` (COALESCE); `hint_view_count`, `ask_count` lũy đẳng nhờ `request_id` ở hàm gọi (RV-18). `active_seconds` cộng độ dài lô + khoảng nghỉ tới hoạt động trước nếu < 30 phút; ≥ 30 phút 0 giây là phiên mới (D-GEO-144, RV-17). Mỗi hàng chỉ một học sinh ghi nên không tranh chấp khóa hàng.'),
 'Q08b_record_durable_call': ('Gọi `geo.fn_record_durable` cho sự kiện bền `unlock` (sau khi xem lời giải)', 'Học sinh', '≈ 17/giây; đỉnh +100/giây (cùng `open` ≈ 33/giây)', '`fn_solution_gate` + `durable_request_pkey` + `event_log_*` + `assignment_progress_pkey`', 'Không (ghi)', 'O(log N) × 6',
   '`unlock` chỉ ghi khi cổng OK (không thể có "đã xem lời giải" khi bài còn khóa, ca G4); lũy đẳng theo `request_id` (ca G5, RV-18); `open` là mức B (D-GEO-162).'),
 'Q09_class_tracking': ('Trang theo dõi lớp: ai đã mở, mức gợi ý, số câu hỏi, thời gian (LRN-03)', 'Giáo viên, màn Theo dõi lớp (làm mới mỗi ~10 giây khi đang mở)', '≈ 3/giây (một phần của 3%; 30 giáo viên đang mở làm mới mỗi 10 giây)',
   '`assignment_org_id_id_key`, `class_member_name_uq` (INCLUDE user_id), `app_user_pkey`, `assignment_progress_pkey`, `class_pkey`', 'Cursor = `(name_key, user_id)` (tên A → Z, D-GEO-121, 167); trang 100 (LIMIT 101), trần 100; không giới hạn cỡ lớp',
   'O(101·log N); KHÔNG đọc `event_log`', 'Không gom hàng triệu sự kiện: đọc bộ đếm tiến độ (NFR-GEO-047). Thứ tự theo tên chuẩn hóa (không theo kết quả, PO-04); phân trang theo khóa nên thứ tự đúng qua mọi trang (RV-36).'),
 'Q09b_class_cards': ('Các thẻ tổng hợp của Theo dõi lớp (M-GEO-21, 22, 23)', 'Giáo viên, cùng màn Q09', '≈ 3/giây (cùng lúc Q09)',
   '`assignment_progress_pkey` (quét khoảng theo bài), `class_teacher_idx`', 'Không (1 hàng gộp)', 'O(s)', 'Gộp trên bộ đếm của MỘT bài, LEFT JOIN nên khi chưa em nào mở vẫn trả 1 hàng với `class_size` (RV-30). `count(*)` có `lint-allow` ghi phạm vi nhỏ. Thời gian trung bình lớp = `active_seconds_total / class_size` (D-GEO-077).'),
 'Q10_set_release_inner': ('Đổi giờ mở và mở ngay (ANS-01): các câu lệnh bên trong `geo.fn_set_release`', 'Giáo viên, nút "Đổi giờ" / "Mở ngay"', '≈ 0,1/giây thường xuyên; vài lần/giây nếu nhiều giáo viên đặt giờ cùng lúc',
   '`assignment_org_id_id_key` (khóa một hàng), `class_teacher_idx`, `audit_log_target_idx` (ghi)', 'Không (ghi)', 'O(log N), khóa một hàng trong giao dịch ngắn',
   'Đường duy nhất sửa `release_at`. Câu khóa không lọc theo giáo viên/rev để hàm trả mã phân biệt NOT_FOUND / FORBIDDEN / CONFLICT_ALREADY_OPEN / CONFLICT_REV / VALIDATION_PAST kèm rev hiện hành (RV-21, D-GEO-143); luôn ghi `audit_log`. Giờ mở lúc tạo bài đi qua `fn_create_assignment` (D-GEO-158).'),
 'Q11_keyword_click': ('Bấm từ khóa mở bài giảng (PO-34)', 'Học sinh (gợi ý, lời giải, câu trả lời)', '≈ 40/giây (12%)',
   '`keyword_pkey`, `lecture_org_id_id_key`', 'Không (1 hàng)', 'O(log N) × 2', 'Bài giảng bất biến theo thời gian ngắn → đệm Redis theo khóa `p:` (TTL dài). Cột `human_reviewed = false` hiện "Nội dung đang chờ giáo viên soát" cho cả học sinh và giáo viên (D-GEO-137, 181).'),
 'Q11d_lecture_keywords': ('Từ khóa liên quan trong hộp bài giảng (PO-34)', 'Học sinh, mở một bài giảng', '≈ 40/giây (mỗi lần bấm từ khóa mở thêm một hộp)', '`keyword_lecture_idx`', 'Tối đa 10 dòng', 'O(log N + 10)', 'Đã sắp theo `term_key` trong chỉ mục nên không có nút Sort.'),
 'Q12_qa_source_retrieval': ('Hỏi đáp tìm nguồn (PO-33): chọn tối đa 5 bài giảng làm nguồn cho câu trả lời', 'Cổng AI / học sinh, tab Hỏi đáp', '≈ 40/giây (12%) — phần lớn thời gian một lượt hỏi là gọi AI (không giữ kết nối CSDL)',
   '`lecture_search_idx`, `lecture_org_id_id_key`', 'Tối đa 5 dòng; ứng viên giới hạn 200', 'O(log N + min(m, 200)) nhờ giới hạn ứng viên', 'Phiên bản đã tối ưu (v2): xếp hạng trên tập ứng viên ≤ 200 thay vì mọi bài khớp; lịch sử trước/sau ở mục "Lịch sử tối ưu". Khóa lời giải (BR-02) là việc của cổng AI; truy vấn này chỉ đọc bài giảng, không đọc lời giải.'),
 'Q13_catalog_list': ('Kho đề có lọc: cấp + loại hình (KNW-01)', 'Giáo viên, màn Kho đề', '≈ 17/giây (5%)',
   '`problem_catalog_filter_idx` (một phần: kho đề đang dùng)', 'Cursor = bộ `(level, solid_kind, topic_id, id)` của dòng cuối; trang 20, trần 50', 'O(log N + 21)', 'Bộ lọc tùy chọn (dạng bài) thêm `AND p.topic_id = …` khi có; **không** viết `(:x IS NULL OR cột = :x)` vì phá chỉ mục (R-Q08). Chỉ mục một phần nên 8.000 đề tự nhập không có trong chỉ mục.'),
 'Q13b_catalog_list_level_only': ('Kho đề chỉ lọc theo cấp', 'Giáo viên', 'trong 5% của Q13', '`problem_catalog_filter_idx` (đầu chỉ mục)', 'Như Q13', 'O(log N + 21)', 'Thứ tự duyệt = thứ tự chỉ mục nên không cần Sort.'),
 'Q14_qa_recent': ('Hỏi gì: các câu hỏi gần nhất của cả lớp trong một bài (LRN-03)', 'Giáo viên, màn Theo dõi lớp', '≈ 3/giây',
   '`qa_turn_assignment_idx`', 'Cursor = `qa_turn.id` (mới nhất trước); trang 50 (LIMIT 51), trần 100', 'O(log N + 51)', 'Trả câu hỏi, loại câu trả lời và nguồn đã dùng; KHÔNG trả `reply_text` (chỉ chủ câu hỏi đọc, D-GEO-159; `geo_app` không có SELECT cột đó).'),
 'Q15_audit_history': ('Lịch sử đổi giờ mở của một bài', 'Giáo viên/quản trị, chi tiết bài', '≈ 0,1/giây', '`audit_log_target_idx`', 'Cursor = `audit_log.id` (mới nhất trước); trang 20', 'O(log N + 21)', '—'),
 'Q16_dup_locked_check': ('Tự học kế thừa khóa: chữ ký chuẩn (hoặc băm) khớp bài lớp đang khóa của em (D-GEO-150, 151)', 'Hàm cổng (fn_get_hint, fn_solution_gate, fn_create_self_assignment) cho bài tự học', '≈ 3/giây (gợi ý + lời giải của bài tự học)',
   '`problem_signature_pkey`, `problem_signature_lookup_idx`, `problem_version_hash_idx` (org_id, statement_hash), `class_member_user_idx`, `assignment_class_problem_uq`', 'Tối đa 1 dòng', 'O(k·log N), k = số lớp của em × số đề kho cùng chữ ký (thường 1)',
   'Thân của `geo.fn_self_lock_source`: khớp theo chữ ký chuẩn (hình mẫu, tham số quy về a = 1, đại lượng hỏi; cùng `sig_version`) và băm phụ, với bài lớp đang khóa của MỌI lớp em đang thuộc; gọi ở MỖI lần xin gợi ý/lời giải của bài tự học (D-GEO-150, 151; RV-01, 03; ADR-GEO-15). Đổi tên điểm (F-GEO-226) cho cùng chữ ký.'),
 'Q17_teacher_classes': ('Màn đầu của giáo viên: các lớp của mình kèm sĩ số', 'Giáo viên, đầu phiên', '≈ 2/giây',
   '`class_teacher_idx`, `class_member_pkey` (đếm theo khoảng của MỘT lớp)', 'Cursor = `class.id`; trang 20', 'O(20·(log N + s))', 'Một giáo viên có ít lớp nên thường một trang.'),
 'Q18_share_grant_check': ('Chặng 7 (chừa chỗ): trường nhận có cấp phép còn hiệu lực cho tài nguyên?', 'Mọi đường đọc chéo trường ở chặng sau', '0 ở MVP (không có đường đọc chéo trường)',
   '`share_grant_lookup_idx` (một phần: `status = approved`)', 'Tối đa 1 dòng', 'O(log N)', 'Bảng 35 hàng nên Seq Scan; ở 50.000 cấp phép dùng chỉ mục (mục "Kiểm ở quy mô lớn").'),
 'Q19_teacher_problem_detail': ('Giáo viên xem một đề trong kho: đề, nhãn, hình, gợi ý (KNW-02, LRN-04)', 'Giáo viên, màn xem đề / xem như học sinh', '≈ 5/giây (một phần của 5% kho đề)', '`problem_pkey`, `problem_version_pkey`, `scene_build_org_id_problem_version_id_build_no_key`, `hint_pkey`', 'Không (1 hàng)', 'O(log N) × 4', 'Lời giải KHÔNG nằm trong truy vấn này: giáo viên gọi `geo.fn_get_solution_teacher` (Q19b). Cũng là dữ liệu của xem trước đề CHƯA giao (bước 3; giờ đang chọn do giao diện giữ, D-GEO-146).'),
 'Q19b_teacher_solution_body': ('Giáo viên xem lời giải của đề trong kho: thân `geo.fn_get_solution_teacher`', 'Giáo viên (tài khoản mẫu hoặc đã xác nhận, PO-18)', '≈ 3/giây', '`app_user_pkey`, `problem_version_pkey`, `problem_pkey`, `solution_pkey`', 'Không (1 hàng)', 'O(log N) × 4', 'CHỈ đề kho nền tảng (`source = catalog`, D-GEO-153, RV-04): đề tự nhập của trường khác hay trường mình ⇒ 0 hàng ⇒ NOT_FOUND (D-GEO-175). Kiểm vai và loại tài khoản ngay trong CSDL. Chạy với vai chủ vì `geo_app` không đọc `solution`. Ca T1…T6 ở `07_verification.md` §5.'),
 'Q20_event_timeline': ('Dòng thời gian một học sinh trong một bài (giáo viên bấm xem)', 'Giáo viên, chi tiết học sinh', '≈ 1/giây',
   '`event_log_*_org_id_assignment_id_user_id_occurred_at_id_idx` ở các partition thuộc khoảng thời gian', 'Cursor = `(occurred_at, id)` (mới nhất trước); trang 50 (LIMIT 51); BẮT BUỘC có cận `occurred_at`', 'O(P·log N + 51), P = số partition trong cận', 'Cận thời gian lấy từ thời điểm tạo bài (UUIDv7 chứa thời gian) để cắt partition.'),
 'Q20b_event_timeline_no_bound': ('ĐỐI CHỨNG: dòng thời gian không có cận thời gian', 'Không dùng trong mã (chỉ để minh họa luật R-Q09)', '—', 'như Q20', 'như Q20', 'O(P_tất_cả·log N): thăm chỉ mục của mọi partition kể cả rỗng',
   'Ở 7 partition chênh lệch nhỏ (xem số), nhưng chi phí tăng tuyến tính theo số partition giữ lại; luật R-Q09 cấm.'),
 'Q21_qa_history': ('Lịch sử hỏi đáp của chính học sinh trên mọi bài (US-GEO-410, D-GEO-159, RV-19)', 'Học sinh, mục Lịch sử (qua `fn_qa_history`)', '≈ 2/giây', '`qa_turn_user_idx`', 'Cursor = `qa_turn.id` (mới nhất trước); trang 20 (LIMIT 21), trần 100', 'O(log N + 21)',
   'Trả câu trả lời đã lưu (đã qua Kiểm soát đáp án), mở lại không gọi AI. Chỉ chủ câu hỏi: hàm lọc `user_id` từ thẻ; `geo_app` không SELECT được `reply_text`. Trước khi có chỉ mục: 325 trang (mục 3).'),
 'Q22_teacher_assigned_list': ('Danh sách bài đã giao của giáo viên trên mọi lớp (US-GEO-701-SC6, D-GEO-129, RV-19)', 'Giáo viên, màn Bài đã giao', '≈ 1/giây', '`class_teacher_idx`, `assignment_class_idx`, `problem_pkey`', 'Cursor = `assignment.id` (mới nhất trước); trang 20 (LIMIT 21), trần 100', 'O(c·(log A + 21)), c = số lớp của giáo viên',
   'Mỗi lớp lấy 21 dòng theo chỉ mục rồi gộp (như Q02); Sort cuối chỉ trên ≤ 21·c dòng. Không có mục "Tổng quan", "Lớp học" (D-GEO-129).'),
 'Q23_teacher_preview': ('Xem trước "như học sinh" một bài đã giao (US-GEO-607, D-GEO-139, 180; RV-27)', 'Giáo viên, nút Xem trước', '≈ 1/giây', '`assignment_org_id_id_key`, `class_pkey`, `problem_version_org_id_id_key`, `problem_pkey`, khóa duy nhất `scene_build`', 'Không (1 hàng)', 'O(log N) × 5',
   'Đúng dữ liệu của Q04 và trạng thái giờ mở của chính bài giao; KHÔNG gọi hàm lời giải (D-GEO-153); bài đã mở chỉ hiện nhãn MSG-GEO-29 (D-GEO-180); không ghi sự kiện, không tiến độ (D-GEO-120). Đề chưa giao: Q19 + giờ đang chọn ở giao diện (D-GEO-146).'),
 'Q24_teacher_login': ('Các câu đọc của `geo.fn_teacher_login` (D-GEO-152, 173; RV-02)', 'Giáo viên mẫu đăng nhập', '≈ 0,1/giây', '`teacher_access_code_selector_uq`, `auth_failure_ip_idx`, `auth_failure_user_idx`, `app_user_pkey`', 'Đếm dừng ở 10 (LIMIT 10)', 'O(log N)',
   'Bảng `auth_failure` rỗng trong dữ liệu thử nên Seq Scan; ở 300.000 lần sai dùng Index Only Scan, 0,046 ms (mục 3). Không biết trường trước đăng nhập: ngoại lệ T2 ghi ở chỉ mục.'),
}
ORDER = list(META.keys())

def read(path):
    return open(path, encoding='utf-8').read() if os.path.exists(path) else ''

def sql_of(name):
    lines = read(f'{BASE}/queries/{name}.sql').splitlines()
    out = []
    for l in lines:
        if l.startswith('-- role:') or l.startswith('-- tx:') or l.strip() == ':explain':
            continue
        out.append(l)
    return "\n".join(out).strip()

def plan_of(name):
    txt = read(f'{BASE}/results/{name}.txt')
    out = []
    for l in txt.splitlines():
        if re.match(r'^-+$', l.strip()) or 'QUERY PLAN' in l or re.match(r'^\(\d+ rows?\)$', l.strip()):
            continue
        out.append(l.rstrip())
    return "\n".join(x for x in out if x.strip())

def scan_rows(plan):
    """Tổng số dòng đọc bởi các nút quét: rows × loops của mọi nút '... Scan on'."""
    tot = 0
    for l in plan.splitlines():
        m = re.search(r'->\s+(Seq Scan|Index Scan|Index Only Scan|Bitmap Heap Scan)[^(]*\(.*actual time=[\d.]+\.\.[\d.]+ rows=(\d+) loops=(\d+)', l) or \
            re.match(r'\s*(Seq Scan|Index Scan|Index Only Scan|Bitmap Heap Scan)[^(]*\(.*actual time=[\d.]+\.\.[\d.]+ rows=(\d+) loops=(\d+)', l)
        if m:
            tot += int(m.group(2)) * int(m.group(3))
    return tot

def top_rows(plan):
    m = re.search(r'actual time=[\d.]+\.\.[\d.]+ rows=(\d+)', plan)
    return int(m.group(1)) if m else 0

def nodes(plan):
    seen = []
    for m in re.finditer(r'(Index Only Scan|Index Scan|Bitmap Heap Scan|Seq Scan|Insert|Update)', plan):
        if m.group(1) not in seen: seen.append(m.group(1))
    return ", ".join(seen)

def metric(plan, pat):
    m = re.search(pat, plan)
    return m.group(1) if m else '?'


def ap_values():
    """Rút số đo thật cho cột "Đo thật mẫu xấu" từ bench/results/antipatterns.txt, scale_checks.txt, pgbench.txt."""
    def sections(txt, marker):
        parts = re.split(rf'\n(?={marker})', txt)
        return {'AP' + re.match(rf'{marker}(\w+)', x).group(1): x for x in parts if re.match(rf'{marker}(\w+)', x)}
    ap = read(f'{BASE}/results/antipatterns.txt')
    sec = sections('\n' + ap, '### AP')
    def plans(sec_txt):
        out = []
        for chunk in re.split(r'Execution Time: [\d.]+ ms', sec_txt)[:-1]:
            m = re.search(r'Buffers: shared hit=(\d+)', chunk)
            out.append(int(m.group(1)) if m else 0)
        return out, [float(x) for x in re.findall(r'Execution Time: ([\d.]+) ms', sec_txt)]
    v = {}
    def two(k, bad_label, good_label):
        b, t = plans(sec.get(k, ''))
        if len(t) >= 2: return f"{bad_label}: {t[0]:.3f} ms, {b[0]} trang; {good_label}: {t[1]:.3f} ms, {b[1]} trang"
        return 'chưa đo'
    v['AP1'] = two('AP1', 'OFFSET 150.000', 'keyset')
    v['AP2'] = two('AP2', 'count(*) trên event_log (≈ 45 triệu hàng sau thử tải pgbench)', 'LIMIT n+1 có cận')
    v['AP4'] = two('AP4', 'lower() có org_id', 'khóa đủ (org_id, class_id, name_key)')
    v['AP5'] = two('AP5', 'quên org_id (Seq Scan ~20.000 hàng)', 'đủ org_id, class_id')
    b, t = plans(sec.get('AP7', ''))
    v['AP7'] = f"{t[0]:.1f} ms, {b[0]} trang (sắp xếp một partition ≈ 45 triệu hàng sau thử tải pgbench)" if t else 'chưa đo'
    m = re.search(r'bytes_select_star \| bytes_needed \| ratio\s*\n[-+]+\n\s*(\d+) \|\s*(\d+) \|\s*([\d.]+)', sec.get('AP6', ''))
    v['AP6'] = f"{int(m.group(1)):,} byte kéo về so với {int(m.group(2)):,} byte cần (gấp {m.group(3)} lần)".replace(',', '.') if m else 'chưa đo'
    sc = read(f'{BASE}/results/scale_checks.txt')
    m = re.search(r'===== AP3.*?Execution Time: ([\d.]+) ms.*?Execution Time: ([\d.]+) ms', sc, re.S)
    v['AP3'] = f"LIKE trên 50.000 bài giảng: {float(m.group(1)):.1f} ms; tìm văn bản có chỉ mục: {float(m.group(2)):.3f} ms" if m else 'chưa đo'
    pb = read(f'{BASE}/results/pgbench.txt')
    mj = re.search(r'join\s+c=16\s+.*?latency average = ([\d.]+) ms tps = ([\d.]+)', pb)
    mn = re.search(r'n\+1\s+c=16\s+.*?latency average = ([\d.]+) ms tps = ([\d.]+)', pb)
    v['AP8'] = (f"1 câu JOIN: {float(mj.group(1)):.2f} ms/yêu cầu, {float(mj.group(2)):,.0f} tps; N+1 (21 câu): {float(mn.group(1)):.2f} ms, {float(mn.group(2)):,.0f} tps (cục bộ; qua mạng thật mỗi vòng thêm 0,3 đến 1 ms)".replace(',', '.')) if mj and mn else 'chưa đo'
    return v

summary_rows = []
sections = []
for n in ORDER:
    title, who, freq, idx, paging, cx, note = META[n]
    plan = plan_of(n)
    ex = metric(plan, r'Execution Time: ([\d.]+) ms'); pl = metric(plan, r'Planning Time: ([\d.]+) ms')
    hit = metric(plan, r'Buffers: shared hit=(\d+)')
    rd = scan_rows(plan); tr = top_rows(plan)
    summary_rows.append((n.split('_')[0], title, ex, pl, nodes(plan), str(rd), hit))
    sections.append(f"""### {n.split('_')[0]} — {title}
- **Ai gọi:** {who}
- **Tần suất ước tính:** {freq}
- **Chỉ mục phục vụ:** {idx}
- **Phân trang / giới hạn:** {paging}
- **Độ phức tạp:** {cx}
- **Ghi chú:** {note}
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi {ex} ms, lập kế hoạch {pl} ms, trang bộ đệm {hit}, dòng đọc ở các nút quét {rd}, dòng trả về {tr}.

```sql
{sql_of(n)}
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
{plan}
```
</details>
""")

head = """# 04 — Danh mục truy vấn nóng của MVP

> Vai: ARCH. Trạng thái: **đề xuất**. Mọi SQL ở đây là SQL thật, đã chạy trên `geo_arch2` (PostgreSQL 17.11, cụm riêng cổng 54329; lượt sửa sau phản biện RV) với vai `geo_app`, trừ các tệp ghi `-- role: owner` (thân hàm SECURITY DEFINER đọc bảng nhạy cảm hoặc ghi bảng mà `geo_app` không còn quyền ghi thẳng, D-GEO-158). Dữ liệu thử: 50 trường, 600 lớp, 20.000 học sinh, ≈ 2,04 triệu sự kiện (`bench/01_seed.sql`; `07_verification.md` §2).\n> Lượt sửa sau RV: bỏ Q11b, Q11c (D-GEO-166); thêm Q05b, Q05c, Q06b, Q06c, Q07b, Q08b, Q21, Q22, Q23, Q24; sửa Q02, Q02b, Q03, Q04, Q04b, Q05, Q06, Q07, Q08, Q09, Q09b, Q10, Q16, Q19b. Ánh xạ mã trạng thái của hàm sang mã lỗi API: mục 6.
> **Số đo là trên máy một nút cục bộ** (10 lõi, 24 GB RAM, bộ nhớ đệm ấm, bộ sinh tải cùng máy), **không phải kết quả sản xuất**. Thời gian là trung vị của 7 lần `EXPLAIN (ANALYZE, BUFFERS)` sau 3 lần khởi động; kế hoạch đầy đủ nằm trong phần mở ra của từng truy vấn. Dữ liệu và kế hoạch được dựng lại bằng `python3 bench/build_catalogue.py` từ `bench/results/`.
> Tần suất tính cho tải đều 333 yêu cầu/giây (S = 1000, T = 3 giây; `02_domain_model.md` M-GEO-10, 11) theo hỗn hợp ở `05_capacity.md` §2 (giả định). Quy ước tham số: `:'tên'` là tham số của psql (ở Java là `?`).
> Quy ước bố cục chỉ mục: `org_id` đứng đầu mọi chỉ mục thuộc phạm vi trường (`02_multi_tenant.md` §2). Cursor luôn là bộ khóa của dòng cuối đúng thứ tự `ORDER BY`; `LIMIT` = kích thước trang + 1; không `count(*)` để biết còn trang sau.

## 1. Tóm tắt

| Mã | Truy vấn | Thực thi (ms) | Lập kế hoạch (ms) | Nút quét | Dòng đọc | Trang bộ đệm |
|---|---|---|---|---|---|---|
"""
tbl = "\n".join(f"| {a} | {b} | {c} | {d} | {e} | {f} | {g} |" for a, b, c, d, e, f, g in summary_rows)

history = read(f'{BASE}/catalogue_history.md')
_sc = read(f'{BASE}/results/scale_checks.txt')
_m = re.search(r'===== Q12.*?Execution Time: ([\d.]+) ms.*?Execution Time: ([\d.]+) ms', _sc, re.S)
_q12 = lambda: re.search(r'Execution Time: ([\d.]+) ms', read(f'{BASE}/results/Q12_qa_source_retrieval.txt'))
_q20 = re.search(r'Execution Time: ([\d.]+) ms', read(f'{BASE}/results/Q20_event_timeline.txt'))
_q20b = re.search(r'Execution Time: ([\d.]+) ms', read(f'{BASE}/results/Q20b_event_timeline_no_bound.txt'))
for k, val in {'Q12V1': _m.group(1) if _m else '?', 'Q12V2': _m.group(2) if _m else '?', 'Q12BASE': _q12().group(1) if _q12() else '?', 'Q20': _q20.group(1) if _q20 else '?', 'Q20B': _q20b.group(1) if _q20b else '?'}.items():
    history = history.replace('<<' + k + '>>', val)
anti = read(f'{BASE}/catalogue_antipatterns.md')
for k, val in ap_values().items():
    anti = anti.replace('<<' + k + '>>', val)
scale = read(f'{BASE}/catalogue_scale.md')

out = head + tbl + "\n\n" + (
    "Ghi chú: Q18 và Q24 hiện **Seq Scan** vì bảng chỉ có 35 hàng và 0 hàng (rẻ hơn dùng chỉ mục); chứng minh dùng chỉ mục ở quy mô lớn tại mục 3. Các dòng `…_call` đo cả thân hàm plpgsql (kế hoạch chỉ hiện Function Scan). Mọi `Seq Scan` còn lại trong Q20b nằm ở partition rỗng (tương lai và mặc định). Không truy vấn nóng nào có Seq Scan trên bảng lớn (`python3 bench/lint_queries.py`: 0 vi phạm).\n\n"
    "## 2. Từng truy vấn\n\n" + "\n".join(sections) +
    "\n## 3. Kiểm ở quy mô lớn hơn (bảng nhỏ trong dữ liệu thử)\n\n" + scale +
    "\n## 4. Lịch sử tối ưu (trước/sau)\n\n" + history +
    "\n## 5. Mẫu bị cấm và cách kiểm tự động\n\n" + anti + "\n" +
    "\n## 6. Mã trạng thái của hàm và mã lỗi API\n\n" + read(f'{BASE}/catalogue_status.md') + "\n")
open(f'{ROOT}/04_query_catalogue.md', 'w', encoding='utf-8').write(out)
print('ok', len(out))
