Quy tắc của lượt này: tối đa 3 lượt tối ưu một truy vấn. Truy vấn nào còn tồn tại ghi ở "khoảng trống đã biết" (`07_verification.md` §10).

### Q12 — xếp hạng mọi bài khớp → xếp hạng trên tập ứng viên giới hạn (1 lượt, đạt)
- **Vấn đề:** ở 50.000 bài giảng với cụm từ phổ biến (10.075 bài khớp), phiên bản đầu (`bench/queries/history/Q12_v1_rank_all_matches.sql`) tính `ts_rank` cho **mọi** bài khớp rồi mới lấy 5: chi phí tăng theo số bài khớp (đọc 10.075 hàng, `Sort`).
- **Trước (v1):** <<Q12V1>> ms (Bitmap Heap Scan 10.075 hàng + Sort). **Sau (v2):** <<Q12V2>> ms (ứng viên giới hạn 200, rồi lấy 5, rồi đọc đoạn đầu 600 ký tự của đúng 5 bài). Ở bộ dữ liệu 300 bài giảng: <<Q12BASE>> ms.
- **Đánh đổi:** xếp hạng xấp xỉ khi có hơn 200 bài khớp (cụm từ quá phổ biến); chấp nhận được vì nguồn cho hỏi đáp chỉ cần 5 bài liên quan nhất của giáo trình mẫu (vài trăm bài). Nếu cần chất lượng xếp hạng toàn cục ở quy mô lớn: dùng RUM/chỉ mục xếp hạng hoặc một dịch vụ tìm kiếm (không cần ở MVP).

### Q20 — dòng thời gian: thêm cận thời gian (không phải tối ưu mà là luật)
- **Ghi nhận:** Q20 (có cận `occurred_at`): <<Q20>> ms; Q20b (không cận): <<Q20B>> ms, vì chỉ có 4 partition có dữ liệu và chỉ mục theo `(org_id, assignment_id, user_id, …)` rất chọn lọc. Chi phí Q20b tăng **tuyến tính theo số partition giữ lại** (mỗi partition thêm một lần dò chỉ mục kể cả partition rỗng); với 12 tháng lưu giữ thì 14 lần dò. Luật R-Q09 vẫn bắt buộc cận để cắt partition khi bảng lớn hơn.

### Thiết kế lược đồ (trước/sau khi kiểm chứng)
- Bỏ `payload jsonb` khỏi `event_log`, thay bằng `arg smallint`: nhỏ hơn ≈ 22% (≈ 264 → ≈ 207 byte/hàng trong thí nghiệm `bench/10_event_index_cost.sql`).
- Bỏ ba chỉ mục không phục vụ truy vấn nào (`org_parent_idx`, `verification_report_version_idx`, `share_grant_owner_idx`); đổi `keyword_lecture_idx` thành `(org_id, lecture_id, term_key)` để Q11d không cần Sort.
- Chỉ mục kho đề là chỉ mục **một phần** (`source = 'catalog' AND archived_at IS NULL`): 8.000 đề tự nhập không có trong chỉ mục (nhỏ hơn và nhanh hơn).
- `assignment_progress` dùng `fillfactor = 80`; UPDATE đạt HOT ≥ 99% trong thử tải (`07_verification.md` §7).

### Lượt sửa sau phản biện RV (dấu vết)
- **Q05b / `fn_self_lock_source`:** bản đầu viết `LANGUAGE sql SECURITY DEFINER`: hàm SQL có SECURITY DEFINER không được nội tuyến và lập kế hoạch lại thân ở mỗi lần gọi, Q05b đo **0,469 ms**. Đổi sang plpgsql (giữ kế hoạch đã chuẩn bị trong phiên): **0,024 ms**. Cùng lý do, `fn_get_solution_teacher`, `fn_qa_history` cũng viết bằng plpgsql (1 lượt, đạt).
- **Q21 (RV-19):** thêm `qa_turn_user_idx (org_id, user_id, id DESC)`: 325 → 13 trang bộ đệm, 0,555 → 0,013 ms ở dữ liệu sạch (mục 3).
- **Q08 (RV-17):** `BETWEEN interval '0' AND interval '30 minutes'` tính cả đầu mút nên đúng 30:00 bị coi là cùng phiên; đổi thành `>= 0 AND < 30 phút` (D-GEO-144), kiểm bằng ca G6 (29:59 → +1799 s; 30:00 → +0 s).
- **Q09 (RV-36, D-GEO-167):** phân trang theo `(name_key, user_id)` dùng `class_member_name_uq` (thêm `INCLUDE (user_id)`); thứ tự A → Z ổn định qua các trang, không giới hạn cỡ lớp.
- **Q09b (RV-30):** INNER → LEFT JOIN tiến độ: khi chưa em nào mở vẫn trả `class_size`.
- **Q16 (RV-01, 03):** từ "băm nguyên văn lúc gửi đề" sang chữ ký chuẩn + băm phụ, kiểm ở mỗi lần xin (ADR-GEO-15); chỉ mục `problem_version_hash_idx` đổi thành `(org_id, statement_hash)` nên hết ngoại lệ "org_id đứng đầu".
- **Bỏ** Q11b, Q11c và `keyword_trgm_idx` (D-GEO-166): không còn truy vấn nào phục vụ.
