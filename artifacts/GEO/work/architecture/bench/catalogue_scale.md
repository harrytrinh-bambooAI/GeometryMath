Trong dữ liệu thử, một số bảng nhỏ (900 từ khóa, 300 bài giảng, 60 đề kho, 35 cấp phép) nên bộ lập kế hoạch chọn quét tuần tự vì rẻ hơn. Để chứng minh chỉ mục vẫn phục vụ khi bảng lớn, `bench/03_scale_checks.sql` chèn tạm trong **một giao dịch** thêm 50.000 bài giảng, 150.000 từ khóa, 20.000 đề kho, 50.000 cấp phép, chạy lại các truy vấn tương ứng với vai `geo_app`, rồi ROLLBACK (và VACUUM). Kết quả (máy cục bộ):

| Truy vấn | Quy mô | Nút chính trong kế hoạch | Thực thi (ms) |
|---|---|---|---|
| ~~Q11b fuzzy keyword~~ (không thuộc MVP, D-GEO-166; chỉ mục `keyword_trgm_idx` đã bỏ) | 150k từ khóa | (lượt trước) Bitmap Index Scan keyword_trgm_idx | 1.896 |
| Q11 keyword click | 150k | Index Scan keyword_pkey; Index Scan lecture_pkey | 0.014 |
| Q12 qa retrieval | 50k bài giảng (cụm hiếm: thiet dien) v1: xếp hạng mọi bài khớp | Bitmap Heap Scan lecture; Bitmap Index Scan lecture_search_idx | 21.776 |
| Q12 qa retrieval | 50k bài giảng (cụm hiếm: thiet dien) v2: tập ứng viên giới hạn 200 | Bitmap Heap Scan lecture; Bitmap Index Scan lecture_search_idx; Index Scan lecture_pkey | 3.732 |
| Q13 catalog | 20k đề kho (lọc cấp + loại hình) | Index Scan problem_catalog_filter_idx; Index Scan problem_version_pkey | 0.071 |
| Q13b catalog | 20k đề kho (chỉ lọc cấp) | Index Scan problem_catalog_filter_idx; Index Scan problem_version_pkey | 0.291 |
| Q18 share_grant | 50k cấp phép | Index Scan share_grant_lookup_idx | 0.118 |
| AP3 LIKE %x% | 50k bài giảng | Seq Scan lecture | 21.897 |
| AP3 LIKE %x% | 50k bài giảng đúng: tìm kiếm văn bản (GIN) | Bitmap Heap Scan lecture; Bitmap Index Scan lecture_search_idx | 3.304 |

Kết luận (lượt trước, `geo_arch`): Q13/Q13b dùng `problem_catalog_filter_idx` (một phần), Q18 dùng `share_grant_lookup_idx` (một phần), Q12 dùng `lecture_search_idx` (GIN); không có Seq Scan trên bảng lớn. Q12 v1 (xếp hạng mọi bài khớp) chậm đi theo số bài khớp, v2 (ứng viên giới hạn 200) giữ ổn định: xem mục 4.

**Lượt sửa sau RV** (`bench/11_rv_scale_checks.sql` trên `geo_arch2`, mọi thay đổi ROLLBACK; kết quả `bench/results/rv_scale_checks.txt`):

| Truy vấn | Quy mô | Nút chính trong kế hoạch | Thực thi (ms) |
|---|---|---|---|
| Q24 đăng nhập giáo viên | 300.000 lần sai từ 3.000 IP | Index Scan teacher_access_code_selector_uq; Index Only Scan auth_failure_ip_idx; Index Only Scan auth_failure_user_idx (dừng ở 10) | 0.046 |
| Q21 lịch sử hỏi đáp, KHÔNG có `qa_turn_user_idx` (lược đồ trước; RV-19 đo 90 ms trên `geo_arch` đã phình) | 169.697 câu hỏi | Bitmap Heap Scan qa_turn theo org_id, lọc user_id, 325 trang | 0.555 |
| Q21 lịch sử hỏi đáp, CÓ `qa_turn_user_idx` | 169.697 câu hỏi | Index Scan qa_turn_user_idx, 13 trang | 0.013 |
| Q16 tra chữ ký đề kho | 23.260 chữ ký (thêm 16.000 đề tự nhập trùng chữ ký đề kho) | Index Scan problem_signature_lookup_idx (org_id nền tảng đứng đầu) | 0.016 |
