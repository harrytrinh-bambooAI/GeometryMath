# 07 — Kiểm chứng kiến trúc, lược đồ và truy vấn (số đo thật)

> Vai: ARCH. Tệp này do `python3 bench/build_verification.py` dựng từ `bench/results/` (kết quả chạy thật) cộng phần lời; không gõ tay số đo. Trạng thái: **đề xuất**.
> **Lượt sửa gom sau phản biện RV** (`review/RV_findings.md`, D-GEO-150…183): lược đồ dựng lại vào CSDL **mới `geo_arch2`** (cụm `127.0.0.1:54329`; `geo_arch` của lượt trước giữ nguyên làm bằng chứng). Đã chạy lại ở `geo_arch2`: nạp dữ liệu cỡ brief đầy đủ (§2), EXPLAIN toàn bộ catalogue (§3), kế hoạch tổng quát (§3.1), kiểm quy mô cho bảng/chỉ mục mới (§3.2), pgbench rút gọn cho đường ghi mới qua hàm (§4.6), bộ kiểm cách ly **103 ca, cả khi RLS tắt và bật** (§5), kiểm RLS (§6), kích thước sạch (§7.2), lint (§8). **Giữ số của lượt trước** (lược đồ cũ trên `geo_arch`, ghi rõ tại chỗ): pgbench đầy đủ §4.1…4.5, chi phí RLS §6 (bảng), thí nghiệm chỉ mục §7.1, kích thước sau thử tải §7.3, đo mẫu xấu ở catalogue §5.
> **Phạm vi và giới hạn của số đo (đọc trước):** tất cả số đo là trên **máy một nút cục bộ** (10 lõi, 24 GB RAM), PostgreSQL 17.11 và Redis dựng bằng `app/scripts/dev-up.sh` (cụm riêng, cổng 54329 và 6389), **bộ sinh tải (`pgbench`) chạy cùng máy** nên tranh CPU với CSDL; bộ nhớ đệm ấm (dữ liệu nằm hết trong RAM: 24 GB so với vài GB dữ liệu); macOS không ép ghi đĩa vật lý ở `fsync` nên độ trễ commit đồng bộ thấp hơn đĩa thật; các tiến trình khác trên máy (các agent khác, biên dịch) làm số đo nhiễu (vì vậy mỗi truy vấn lấy **trung vị của 7 lần**; thông lượng `pgbench` dao động ± 20% giữa các lần). **Không đo:** tầng Java (không có mã Java của ARCH), Redis, mạng thật, nhiều nút ứng dụng, CSDL tách máy. Đây là bằng chứng về **hình dạng**: chỉ mục được dùng hay không, đường cong theo số kết nối, chi phí tương đối; **không phải kết quả sản xuất**, không đủ để cam kết ngưỡng.

## 1. Cách chạy lại
```
cd artifacts/GEO/work/architecture
./bench/00_reset.sh                    # dựng lại $PGDATABASE (mặc định geo_arch2): lược đồ + móc kiểm thử + dữ liệu (≈ 15 giây); từ chối geo_arch
./bench/run_queries.sh                 # EXPLAIN (ANALYZE, BUFFERS) mọi truy vấn của catalogue
psql -f bench/06_isolation_test.sql    # 103 ca cách ly; lặp lại sau psql -f bench/rls_enable.sql để chạy khi RLS bật
psql -f bench/11_rv_scale_checks.sql   # kiểm quy mô cho bảng/chỉ mục mới của lượt sửa sau RV
./bench/run_pgbench_rv.sh              # pgbench rút gọn cho đường ghi qua hàm (≈ 5 phút)
./bench/run_all.sh                     # (lượt trước) toàn bộ gồm pgbench và RLS đầy đủ (≈ 25 phút)
python3 bench/build_catalogue.py       # dựng lại 04_query_catalogue.md từ kết quả
python3 bench/build_verification.py    # dựng lại tệp này
python3 bench/lint_queries.py          # kiểm tự động "mẫu bị cấm" trên catalogue
```
Tệp: `03_schema.sql` (lược đồ), `bench/test_hooks.sql` (móc kiểm thử, chỉ profile test, D-GEO-169), `bench/rls_enable.sql` (bật RLS, D-GEO-127), `bench/01_seed.sql` (dữ liệu), `bench/queries/*.sql` (truy vấn), `bench/run_queries.sh` (EXPLAIN), `bench/03_scale_checks.sql`, `bench/06_isolation_test.sql`, `bench/08_generic_plan_check.sql`, `bench/09_antipatterns.sql`, `bench/10_event_index_cost.sql`, `bench/pgbench/*.sql` + `run_pgbench.sh`, `bench/05_rls_*.sql` + `run_rls.sh`, `bench/07_sizes.sql`, `bench/lint_queries.py`.

## 2. Bộ dữ liệu thử (brief §6 câu 5, D-GEO-049: 50 trường, 600 lớp, 20.000 học sinh, 2 triệu sự kiện)
Số hàng sau khi nạp ở `geo_arch2` (`bench/results_seed.txt`):
```
 org              |      56
 class            |     600
 student          |   20000
 assignment_class |    6000
 assignment_self  |    8000
 progress         |  177762
 event_log        | 2044126
 qa_turn          |  169697
 problem          |    8060
 lecture          |     300
 keyword          |     900
```
- **50 trường** (5 nhóm × 10 trường) + 1 nền tảng; **600 lớp** (12 mỗi trường; 30 lớp lớn khoảng 55 đến 65 em, còn lại 18 đến 45 em; lớp lớn nhất ≈ 65); **20.000 học sinh**; 200 giáo viên; **6.000 bài giao cho lớp** (4 đến 12 mỗi lớp trong 90 ngày; 15% chưa đặt giờ mở, 60% đã mở, 25% sắp mở) và 8.000 bài tự học (5% kế thừa khóa); **≈ 178.000 hàng tiến độ** (85% học sinh có mở mỗi bài); **≈ 2,04 triệu sự kiện** (10 đến 20 mỗi học sinh-bài; 50% tập trung 19 đến 21 giờ mỗi ngày: đỉnh buổi tối), phân vào 4 partition tháng; ≈ 170.000 câu hỏi (`qa_turn`, có câu trả lời đã lưu, D-GEO-159); kho 60 đề + 8.000 đề tự nhập; 300 bài giảng (7 bài "chưa có người soát"), 900 từ khóa; 35 cấp phép liên trường mẫu. Lượt sửa sau RV thêm: tác giả nền tảng `system` (D-GEO-160); 200 mã Giáo viên mẫu (selector + HMAC, D-GEO-152); nhãn máy đặt qua báo cáo kiểm chứng (D-GEO-158); 7.260 chữ ký chuẩn (đề kho + 90% đề tự nhập; 10% "không dựng được hình"); 400 bài tự học dán lại đề lớp (cùng chữ ký với đề kho, văn bản khác); mỗi lớp không giao trùng `problem` (D-GEO-161).
- Sinh bằng SQL (`generate_series`, hạt giống cố định), không dùng tên người hay tổ chức thật (PO-14: tên "Trường mẫu n", "Học sinh n"). Số lớp THCS: một phần ba (D-GEO-054 yêu cầu có thêm một trường THCS để kiểm cách ly: đủ).
- Kích thước bộ dữ liệu sạch (trước mọi thử tải), xem §7.2.

## 3. Kết quả từng truy vấn nóng (EXPLAIN (ANALYZE, BUFFERS), vai `geo_app`)
Kế hoạch đầy đủ, SQL, chỉ mục phục vụ, cursor: `04_query_catalogue.md`. Tóm tắt (trung vị 7 lần):

| Truy vấn | Thực thi (ms) | Lập kế hoạch (ms) | Số lần "Seq Scan" trong kế hoạch | Trang bộ đệm |
|---|---|---|---|---|
| Q01 | 0.005 | 0.038 | 0 | 7 |
| Q02 | 0.032 | 0.208 | 0 | 115 |
| Q02b | 0.010 | 0.160 | 0 | 13 |
| Q03 | 0.076 | 0.169 | 0 | 132 |
| Q04 | 0.013 | 0.139 | 0 | 16 |
| Q04b | 0.006 | 0.034 | 0 | 7 |
| Q05 | 0.009 | 0.088 | 0 | 14 |
| Q05b | 0.023 | 0.042 | 0 | 37 |
| Q05c | 0.047 | 0.002 | 0 | 30 |
| Q06 | 0.007 | 0.078 | 0 | 10 |
| Q06b | 0.015 | 0.002 | 0 | 10 |
| Q06c | 0.031 | 0.001 | 0 | 38 |
| Q07 | 0.092 | 0.058 | 0 | 259 |
| Q07b | 0.138 | 0.007 | 0 | 266 |
| Q08 | 0.013 | 0.005 | 0 | 7 |
| Q08b | 0.044 | 0.001 | 0 | 23 |
| Q09 | 0.109 | 0.212 | 0 | 575 |
| Q09b | 0.024 | 0.055 | 0 | 23 |
| Q10 | 0.024 | 0.008 | 0 | 9 |
| Q11 | 0.004 | 0.026 | 0 | 6 |
| Q11d | 0.003 | 0.008 | 0 | 3 |
| Q12 | 0.084 | 0.039 | 0 | 67 |
| Q13 | 0.007 | 0.031 | 0 | 19 |
| Q13b | 0.013 | 0.031 | 0 | 52 |
| Q14 | 0.047 | 0.048 | 0 | 250 |
| Q15 | 0.002 | 0.014 | 0 | 2 |
| Q16 | 0.015 | 0.377 | 0 | 28 |
| Q17 | 0.015 | 0.024 | 0 | 17 |
| Q18 | 0.003 | 0.010 | 1 | 1 |
| Q19 | 0.011 | 0.053 | 0 | 12 |
| Q19b | 0.008 | 0.056 | 0 | 12 |
| Q20 | 0.009 | 0.078 | 0 | 13 |
| Q20b | 0.014 | 0.119 | 4 | 13 |
| Q21 | 0.006 | 0.014 | 0 | 13 |
| Q22 | 0.032 | 0.055 | 0 | 131 |
| Q23 | 0.012 | 0.169 | 0 | 15 |
| Q24 | 0.008 | 0.046 | 2 | 5 |

- **Tiêu chí đạt:** (a) không `Seq Scan` trên bảng lớn ở truy vấn nóng; (b) mọi truy vấn nóng ≪ ngưỡng 300 ms và ≤ 10 ms (NFR-GEO-007). Kết quả: thời gian thực thi lớn nhất là **0,138 ms** (Q07b: gọi `fn_record_batch` ghi lô 50 sự kiện + tiến độ); truy vấn đọc chậm nhất là Q09 (theo dõi lớp, cả lớp 64 em theo tên) ≈ 0,11 ms; đường nóng nhất ở đỉnh giờ mở (Q06b, gọi `fn_get_solution`) ≈ 0,015 ms, 10 trang. `Seq Scan` còn lại: Q18 (35 hàng), Q24 (`auth_failure` rỗng; ở 300.000 hàng dùng chỉ mục, §3.2) và các partition rỗng ở Q20b; **không có Seq Scan trên bảng có hơn 10.000 hàng** (`bench/lint_queries.py`: 0 vi phạm).
- Dòng `…_call` đo cả thân hàm plpgsql qua một lần gọi (kế hoạch chỉ hiện Function Scan). Số trang bộ đệm: truy vấn một dòng ≤ 40 trang; Q09 575 trang (đọc theo tên, ≤ 1000 của NFR-GEO-043); Q07/Q07b (ghi 50 sự kiện) ≈ 260 trang.

### 3.1. Kế hoạch tổng quát (generic plan)
JDBC chuyển sang kế hoạch tổng quát sau vài lần thực thi câu lệnh chuẩn bị; ép bằng `plan_cache_mode = force_generic_plan` để kiểm các truy vấn có tham số kiểu khác nhau (uuid, enum, timestamptz, text). Chạy lại trên `geo_arch2` với dạng câu của lượt trước (Q02, Q09 theo `user_id`, Q12, Q13, Q16 theo băm, Q20); các hàm plpgsql mới tự dùng kế hoạch đã chuẩn bị trong phiên:

| Truy vấn (kế hoạch tổng quát ép bằng `plan_cache_mode = force_generic_plan`) | Thực thi (ms) | Nút quét chính |
|---|---|---|
| Q02 bài của em | 0.074 | Index Only Scan class_member_user_idx; Index Scan assignment_class_idx; Index Scan problem_version_pkey; Index Scan problem_pkey |
| Q09 theo dõi lớp | 0.159 | Index Scan assignment_org_id_id_key; Index Only Scan class_member_name_uq; Index Scan assignment_progress_pkey; Index Scan app_user_pkey |
| Q13 kho đề (kiểu enum làm tham số) | 0.050 | Index Scan problem_catalog_filter_idx; Index Scan problem_version_pkey |
| Q20 dòng thời gian (khoảng thời gian làm tham số) | 0.098 |  |
| Q12 tìm nguồn (chuỗi tìm kiếm làm tham số) | 1.524 | Bitmap Heap Scan lecture; Index Scan lecture_search_idx; Index Scan lecture_org_id_id_key |
| Q16 trùng đề đang khóa (băm làm tham số) | 0.047 | Index Only Scan class_member_user_idx; Index Scan assignment_class_idx; Index Scan problem_version_pkey |

Kết luận: kế hoạch vẫn dùng chỉ mục như kế hoạch riêng; Q12 chậm hơn ~1 ms ở lần gọi đầu vì phiên nạp từ điển `unaccent` (làm nóng một lần cho mỗi kết nối; nhóm kết nối giữ phiên ấm).

### 3.2. Ở quy mô lớn hơn
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


## 4. Tải đồng thời (pgbench, vai `geo_app`, giao thức prepared)
**§4.1…4.5 là số của lượt trước** (lược đồ cũ, ghi thẳng bảng, CSDL `geo_arch`); đường ghi mới qua hàm SECURITY DEFINER đo ở **§4.6**. Mỗi phép đo 20 giây, 8 luồng `pgbench`, cùng máy. Kịch bản mô phỏng đúng các câu lệnh của catalogue (gồm giao dịch, sự kiện bền và cập nhật tiến độ), tham số lấy ngẫu nhiên từ 19.900 cặp (học sinh, bài). Chỉ đo phần CSDL.

### 4.1. Từng kịch bản riêng (16 kết nối)
| Kịch bản (16 kết nối) | Trung bình (ms) | tps |
|---|---|---|
| Đăng nhập (Q01) | 0,48 | 33.310 |
| Danh sách bài của em (Q02) | 0,55 | 29.227 |
| Mở bài: Q04 + sự kiện + tiến độ | 1,21 | 13.199 |
| Lô 50 sự kiện + tiến độ (Q07 + Q08), commit không đồng bộ | 1,78 | 8.978 |
| Xin gợi ý: Q05 + sự kiện bền + tiến độ | 1,25 | 12.817 |
| Xem lời giải: cổng + sự kiện bền + tiến độ (commit đồng bộ) | 2,32 | 6.905 |
| Chỉ cổng `fn_get_solution` | 1,31 | 12.251 |
| Hỏi đáp: Q12 + qa_turn + sự kiện + tiến độ | 2,01 | 7.958 |
| Bấm từ khóa (Q11) | 0,39 | 40.884 |
| Theo dõi lớp (Q09) | 0,78 | 20.571 |
| Kho đề (Q13) | 0,21 | 76.590 |

### 4.2. Hỗn hợp theo mô hình `05_capacity.md` §2, theo số kết nối (độ co giãn)
| Số kết nối | tps | Trung bình (ms) |
|---|---|---|
| 2 | 6.985 | 0,29 |
| 4 | 7.569 | 0,53 |
| 8 | 9.894 | 0,81 |
| 16 | 11.873 | 1,35 |
| 32 | 10.432 | 3,07 |
| 64 | 9.952 | 6,43 |
| 128 | 8.632 | 14,83 |
| 256 | 4.043 | 63,32 |

Thông lượng tăng tới điểm gối rồi **giảm** khi thêm kết nối: bằng chứng cho ADR-GEO-04 (nhóm kết nối nhỏ, không nâng theo số người dùng).

### 4.3. Đỉnh giờ mở (1000 học sinh mở lời giải; cổng ở CSDL)
| Kịch bản | Kết nối | tps | Trung bình (ms) |
|---|---|---|---|
| Chỉ cổng quyết định (đọc) | 20 | 6.295 | 3,18 |
| Chỉ cổng quyết định (đọc) | 50 | 8.637 | 5,79 |
| Chỉ cổng quyết định (đọc) | 100 | 12.358 | 8,09 |
| Giao dịch đầy đủ (cổng + sự kiện `unlock` bền + tiến độ) | 20 | 8.578 | 2,33 |
| Giao dịch đầy đủ (cổng + sự kiện `unlock` bền + tiến độ) | 50 | 8.381 | 5,97 |
| Giao dịch đầy đủ (cổng + sự kiện `unlock` bền + tiến độ) | 100 | 6.901 | 14,49 |

Nhu cầu: 100 yêu cầu/giây (M-GEO-12), ca xấu nhất 1000 dồn trong 1 giây. Cả đường chỉ đọc lẫn giao dịch đầy đủ (có ghi bền, commit đồng bộ) vượt nhu cầu nhiều lần ở tầng CSDL trên máy này.

### 4.4. Mức chịu tải cố định 500 giao dịch/giây (1,5× tải thiết kế), hỗn hợp, 32 kết nối, 30 giây
Trung bình **2,83 ms**, độ lệch chuẩn 2,63 ms, thông lượng thực 500 tps; giao dịch vượt 50 ms: **10/14998 (0.067%)**; giao dịch bị bỏ qua (không theo kịp tốc độ yêu cầu): 0; không lỗi.

### 4.5. Đường nhẹ nhất (chỉ cổng quyết định) theo số kết nối
| Số kết nối | tps | Trung bình (ms) |
|---|---|---|
| 1 | 3.612 | 0,28 |
| 4 | 8.140 | 0,49 |
| 8 | 12.625 | 0,63 |
| 16 | 14.370 | 1,11 |
| 32 | 14.039 | 2,28 |
| 64 | 12.122 | 5,28 |
| 128 | 13.818 | 9,26 |

### 4.6. Lượt sửa sau RV: đường ghi qua hàm, cổng trả `status`, thăm dò thường trực (`bench/run_pgbench_rv.sh`, `geo_arch2`)
Vai `geo_bench` (= `geo_app` + `geo_ai`, chỉ để đo; `p_qa` gọi `fn_record_qa` của cổng AI), giao thức prepared, **10 giây** mỗi phép đo, 8 luồng `pgbench`, cùng máy. Mọi ghi sự kiện/tiến độ đi qua `fn_record_batch`, `fn_record_durable`, `fn_get_hint`, `fn_record_qa`; xem lời giải gọi `fn_get_solution` rồi `fn_record_durable('unlock')` với `request_id` mới mỗi lần. Số lượt này cao hơn lượt trước ở nhiều kịch bản: máy ít nhiễu hơn và CSDL sạch (không phình sau thử tải), nên **không** đọc là "hàm nhanh hơn ghi thẳng"; điều đọc được là đường mới không có nút cổ chai và vẫn ≫ nhu cầu.

| Kịch bản (16 kết nối, 10 giây) | Trung bình (ms) | tps |
|---|---|---|
| Đăng nhập qua `fn_student_login` | 0,36 | 44.930 |
| Danh sách bài của em (Q02) | 0,38 | 41.771 |
| Mở bài: Q04 + `fn_record_durable(open)` (bền) | 1,67 | 9.566 |
| Lô 50 sự kiện qua `fn_record_batch`, commit không đồng bộ | 1,33 | 12.051 |
| Gợi ý qua `fn_get_hint` (đọc + ghi bền) | 0,57 | 28.087 |
| Xem lời giải: `fn_get_solution` + `fn_record_durable(unlock)` (commit đồng bộ) | 1,24 | 12.945 |
| Chỉ cổng `fn_get_solution` | 0,31 | 51.016 |
| Hỏi đáp: Q12 + `fn_record_qa` | 1,83 | 8.730 |
| Bấm từ khóa (Q11) | 0,18 | 90.318 |
| Theo dõi lớp (Q09 theo tên) | 0,43 | 37.137 |
| Kho đề (Q13) | 0,10 | 158.957 |
| Thăm dò trạng thái khóa Q04b (ca đệm trượt) | 0,28 | 56.231 |

Hỗn hợp `05_capacity.md` §2 (không kể thăm dò) theo số kết nối:

| Số kết nối | tps | Trung bình (ms) |
|---|---|---|
| 8 | 8.589 | 0,93 |
| 16 | 16.138 | 0,99 |
| 32 | 16.646 | 1,92 |
| 64 | 18.807 | 3,40 |

Đỉnh giờ mở (1000 học sinh mở lời giải; cổng ở CSDL, không đệm quyết định, D-GEO-157):

| Kịch bản | Kết nối | tps | Trung bình (ms) |
|---|---|---|---|
| Chỉ cổng `fn_get_solution` (đọc) | 20 | 46.634 | 0,43 |
| Chỉ cổng `fn_get_solution` (đọc) | 50 | 61.392 | 0,81 |
| Chỉ cổng `fn_get_solution` (đọc) | 100 | 62.586 | 1,60 |
| Giao dịch đầy đủ (cổng + `unlock` bền lũy đẳng) | 20 | 19.593 | 1,02 |
| Giao dịch đầy đủ (cổng + `unlock` bền lũy đẳng) | 50 | 20.516 | 2,44 |
| Giao dịch đầy đủ (cổng + `unlock` bền lũy đẳng) | 100 | 21.087 | 4,74 |

Mức chịu tải cố định: hỗn hợp + **thăm dò Q04b ca xấu** (mọi lần thăm dò xuống CSDL, ≈ 23% giao dịch) ở 650 giao dịch/giây (nhu cầu đỉnh mới 581,4 yêu cầu/giây, `05_capacity.md` §2), 32 kết nối, 30 giây: thông lượng thực **629,6** tps, trung bình **3,96 ms** (gồm độ trễ lập lịch của `-R`), độ lệch chuẩn 2,84 ms, giao dịch vượt 50 ms: **0/18882 (0.000%)**, bị bỏ qua: 0, không lỗi.

## 5. Kiểm tra tách quyền, cách ly và bất biến ở tầng dữ liệu (`geo_arch2`: RLS tắt 103 PASS, 0 FAIL · RLS bật 103 PASS, 0 FAIL)
Chạy bằng `bench/06_isolation_test.sql` (superuser đổi vai `geo_app` / `geo_ai` / `geo_ops` / `geo_tester` theo ca; mọi thay đổi ROLLBACK), lần hai sau `bench/rls_enable.sql` (ứng dụng đặt `app.org_id` như `withTenant`). Nhóm ca:
- **A** (A1…A12) tách quyền bảng: không đọc/ghi `solution`, `verification_report`; không đọc `qa_turn.reply_text`, bảng mã giáo viên; không ghi thẳng `event_log`, `qa_turn`, `audit_log`.
- **R** (R1…R5c) **năm câu lệnh RV-GEO-10 phải BỊ TỪ CHỐI với vai `geo_app`**: tạo giáo viên mẫu, ghi kho nền tảng (đề và version), gắn nhãn "Đã kiểm chứng" (UPDATE và gọi hàm), sửa/chèn tiến độ, chèn bài giao có giờ mở quá khứ (cả qua hàm và CHECK với vai chủ).
- **T** (T1…T6) `fn_get_solution_teacher` chỉ đề kho (D-GEO-153; T2 tái hiện đúng ca RV-04: giáo viên trường A gọi lời giải bài tự học trường B ⇒ 0 hàng).
- **B** (B1…B11) cổng lời giải và máy trạng thái giờ mở, mã `NOT_FOUND` / `LOCKED` / `FORBIDDEN` / `CONFLICT_*` / `VALIDATION_PAST` (RV-21).
- **E** (E0…E3) ranh giới đúng R bằng móc kiểm thử `fn_test_set_release` của profile test (D-GEO-169); `geo_app` không gọi được móc.
- **C** (C1…C8) tự học: thứ tự gợi ý, lũy đẳng gợi ý, kế thừa khóa theo **chữ ký** (văn bản khác, băm khác), gợi ý đề kho cho bài kế thừa (D-GEO-155), "dán trước, giao sau" (RV-03), đề không dựng được hình không có lời giải tự học.
- **D** (D1…D17) bất biến, khóa ngoại tổ hợp và CHECK chéo trường (RV-12), `topic_id` đề kho (RV-31), `CONFLICT_DUPLICATE` khi giao trùng (D-GEO-161), nhật ký tạo bài.
- **G** (G1…G9) ghi sự kiện: EXISTS tư cách lớp (RV-26), mức A/B, `scene_shown`, `unlock` khi khóa bị từ chối, `open` bền lũy đẳng (RV-18, D-GEO-162), biên phiên 29:59/30:00 (RV-17), lưu câu trả lời hỏi đáp và lịch sử chỉ chủ đọc (D-GEO-159).
- **L** (L1…L8) đăng nhập: mã Giáo viên mẫu theo tài khoản, 10 lần sai/15 phút theo IP và theo tài khoản, thu hồi, hết hạn, chỉ HMAC (D-GEO-152, 173); `fn_student_login` chỉ tạo `student/light` (D-GEO-158).
- **V** (V1…V3) nhãn máy chỉ theo báo cáo kiểm chứng của chính version (D-GEO-158).

- PASS A1: geo_app KHÔNG đọc được geo.solution (insufficient_privilege)
- PASS A2: geo_app KHÔNG đọc được verification_report
- PASS A3: đọc cột final_answer bị chặn
- PASS A4: geo_app KHÔNG ghi được solution
- PASS A5: không bảng nào geo_app đọc được có cột tên solution/answer/steps (0)
- PASS A6: geo_app KHÔNG sửa được release_at trực tiếp (chỉ qua fn_set_release)
- PASS A7: geo_app KHÔNG xóa được audit_log
- PASS A8: geo_app KHÔNG đọc trực tiếp câu trả lời đã lưu (chỉ chủ câu hỏi qua fn_qa_history, D-GEO-159)
- PASS A9: geo_app KHÔNG đọc bảng mã Giáo viên mẫu (D-GEO-152)
- PASS A10: geo_app KHÔNG ghi thẳng event_log (chỉ qua hàm ghi sự kiện)
- PASS A11: geo_app KHÔNG ghi thẳng qa_turn (câu trả lời phải qua Kiểm soát đáp án, fn_record_qa của geo_ai)
- PASS A12: geo_app KHÔNG ghi thẳng audit_log (chỉ hàm SECURITY DEFINER ghi)
- PASS R1: geo_app KHÔNG tạo được tài khoản (giáo viên mẫu) bằng INSERT app_user
- PASS R2: geo_app KHÔNG ghi được kho đề nền tảng (chỉ geo_content)
- PASS R2b: geo_app KHÔNG thêm được version cho đề kho nền tảng
- PASS R3: geo_app KHÔNG tự gắn nhãn "Đã kiểm chứng" (UPDATE verify_status bị từ chối)
- PASS R3b: geo_app KHÔNG gọi được hàm đổi nhãn máy (chỉ geo_ai, geo_content kèm báo cáo)
- PASS R4: geo_app KHÔNG tự nâng hint_level_max (UPDATE assignment_progress bị từ chối)
- PASS R4b: geo_app KHÔNG chèn được hàng tiến độ
- PASS R5: geo_app KHÔNG INSERT bài giao (giờ mở quá khứ không qua hàm, không nhật ký)
- PASS R5b: fn_create_assignment từ chối giờ mở ở quá khứ (VALIDATION_PAST)
- PASS R5c: CHECK assignment_release_ck chặn giờ mở trước giờ tạo kể cả với vai chủ
- PASS T1: giáo viên mẫu xem được lời giải đề kho (1)
- PASS T2: giáo viên trường A KHÔNG xem được lời giải bài tự học của trường B (ca RV-04 tái hiện) (0)
- PASS T3: giáo viên KHÔNG xem được lời giải bài tự học của học sinh trường mình (0)
- PASS T4: tài khoản học sinh gọi hàm giáo viên => 0 hàng (0)
- PASS T5: tài khoản hệ thống (tác giả nền tảng) không dùng được để xem lời giải (0)
- PASS T6: org trong ngữ cảnh khác trường của giáo viên => 0 hàng (0)
- PASS B1: bài đã tới giờ mở, học sinh trong lớp nhận lời giải (status=OK)
- PASS B2: bài chưa đặt giờ / chưa tới giờ => LOCKED (FORBIDDEN, BR-GEO-251), không có nội dung (LOCKED)
- PASS B3: học sinh lớp/trường khác dù biết id bài => NOT_FOUND (NOT_FOUND)
- PASS B4: org_id sai trong ngữ cảnh => NOT_FOUND (NOT_FOUND)
- PASS B5a: chưa đặt giờ => khóa (BR-03, D-GEO-043)
- PASS B5: học sinh KHÔNG đặt được giờ mở => FORBIDDEN (FORBIDDEN)
- PASS B5b: giáo viên khác cùng trường => FORBIDDEN (FORBIDDEN)
- PASS B5c: bài của trường khác => NOT_FOUND (NOT_FOUND)
- PASS B6: sai release_rev => CONFLICT_REV kèm rev hiện hành (CONFLICT_REV)
- PASS B6b: đặt giờ ở quá khứ => VALIDATION_PAST (VALIDATION_PAST)
- PASS B7: giáo viên của lớp đặt giờ tương lai (chưa đặt -> sắp mở), rev tăng
- PASS B8: giờ mở ở tương lai => vẫn khóa
- PASS B8b: đổi giờ khi sắp mở thành công
- PASS B9: "mở ngay" (release_at := now() của CSDL) => học sinh trong lớp nhận lời giải
- PASS B10: đã mở thì không đổi giờ nữa => CONFLICT_ALREADY_OPEN (D-GEO-042 A, D-GEO-143)
- PASS B10b: bấm "mở ngay" khi đã mở cũng bị từ chối
- PASS B10c: sau các lần bị từ chối, lời giải vẫn mở
- PASS B11: audit_log ghi đúng 3 dòng theo thứ tự (release_set,release_change,release_now), lần bị từ chối không ghi
- PASS E0: vai ứng dụng KHÔNG gọi được fn_test_set_release (chỉ geo_tester ở profile test)
- PASS E1: t = R - 1 giây => khóa
- PASS E2: t = R đúng (release_at = now() của CSDL) => mở (D-GEO-041 phương án A)
- PASS E3: t = R + 1 giây => mở
- PASS C1: bài tự học chưa xem Gợi ý 1, 2 => khóa (BR-05, D-GEO-023)
- PASS C1b: xin Gợi ý 2 trước Gợi ý 1 => LEVEL_ORDER (D-GEO-108) (LEVEL_ORDER)
- PASS C1c: Gợi ý 1 của đề tự nhập; gửi lại cùng request_id không ghi thêm sự kiện (sự kiện hint mới=1, mức=1, RV-18)
- PASS C2: xem Gợi ý 2 qua fn_get_hint (đường duy nhất nâng mức) => mở (OK)
- PASS C3: học sinh khác không xem được bài tự học của bạn (NOT_FOUND)
- PASS C4: tự học khớp CHỮ KÝ (văn bản khác, băm khác) với đề kho của bài lớp còn khóa => khóa dù đã xem Gợi ý 2 (D-GEO-150)
- PASS C5: bài tự học kế thừa khóa nhận Gợi ý 2 của ĐỀ KHO, không phải gợi ý AI (D-GEO-155, RV-06) (catalog_inherited)
- PASS C6: bài lớp nguồn đã mở => bài tự học cũng mở
- PASS C7a: dán đề trước khi giáo viên giao: chưa có bài lớp khớp => mở sau Gợi ý 2 (OK)
- PASS C7b: giáo viên giao đúng đề kho đó SAU (chưa đặt giờ) => bài tự học khóa ngay ở lần xin kế tiếp (RV-03) (LOCKED)
- PASS C7c: từ lúc đó gợi ý lấy từ đề kho (catalog_inherited)
- PASS C8: đề tự nhập không dựng được hình (không chữ ký) => không có lời giải tự học dù đã xem Gợi ý 2 (LOCKED)
- PASS D1: sửa nội dung version đề bị chặn (BR-11)
- PASS D2: sửa đáp số lời giải tại chỗ bị chặn (BR-11)
- PASS D3: audit_log chỉ thêm
- PASS D4: khóa ngoại tổ hợp chặn tham chiếu chéo trường (progress -> assignment)
- PASS D5: khóa ngoại tổ hợp chặn thêm em trường khác vào lớp
- PASS D6: share_grant approved bắt buộc hai bên duyệt và có thời hạn (BR-28)
- PASS D7: báo cáo không thể ghi verified khi đáp số chưa khớp (BR-07)
- PASS D8: bài tự học không thể có lớp/giờ mở (CHECK assignment_kind_ck)
- PASS D9: bài giao không thể trỏ đề tự nhập của trường khác (CHECK nội dung + FK tổ hợp, RV-12)
- PASS D9b: bài tự học chỉ dùng đề tự nhập của chính em (FK (org, problem, owner))
- PASS D10: lock_source chỉ trỏ bài cùng trường (FK tổ hợp, RV-12)
- PASS D11: qa_turn.user_id thuộc đúng trường (FK tổ hợp, RV-12)
- PASS D12: đề kho bắt buộc topic_id (RV-31) và tác giả nền tảng (D-GEO-160)
- PASS D13: bài lớp chỉ giao đề kho nền tảng (CHECK assignment_content_ck)
- PASS D14: tài khoản sample phải là giáo viên của một trường; system chỉ ở nền tảng (app_user_kind_ck)
- PASS D15: giao lại cùng problem cho cùng lớp => CONFLICT_DUPLICATE (D-GEO-161, chỉ mục duy nhất một phần) (CONFLICT_DUPLICATE)
- PASS D15b: gửi lại cùng id bài giao không tạo bài thứ hai (DUPLICATE)
- PASS D16: giáo viên không dạy lớp giao bài => FORBIDDEN (FORBIDDEN)
- PASS D16b: lớp của trường khác => NOT_FOUND (NOT_FOUND)
- PASS D16c: giao đề tự nhập của học sinh cho lớp => NOT_FOUND (chỉ đề kho) (NOT_FOUND)
- PASS D17: tạo bài giao ghi đúng 1 dòng audit_log assignment_create (BR-GEO-240)
- PASS G1: lô sự kiện cho bài của lớp khác => NOT_FOUND, 0 hàng ghi (RV-26)
- PASS G2: lô mức A không mang được sự kiện mức B (unlock) (VALIDATION_KIND)
- PASS G3: lô mức A gồm tool + scene_shown (D-GEO-165) ghi đủ, giờ tương lai bị kẹp về giờ nhận
- PASS G4: ghi "đã xem lời giải" cho bài còn khóa bị từ chối (LOCKED) (LOCKED)
- PASS G5: open là sự kiện bền (D-GEO-162); gửi lại cùng request_id => DUPLICATE, 1 sự kiện (RV-18)
- PASS G6: biên phiên 29:59 cùng phiên (+1799 s), 30:00 phiên mới (+0 s) (D-GEO-144, RV-17) (1799, 0)
- PASS G7: chỉ cổng AI (geo_ai) ghi lượt hỏi đáp (câu trả lời đã qua Kiểm soát đáp án)
- PASS G8: lưu câu trả lời hỏi đáp (D-GEO-159); gửi lại cùng request_id không nhân đôi câu hỏi/ask_count (RV-18)
- PASS G9: lịch sử hỏi đáp (Q21) trả câu trả lời đã lưu cho chủ câu hỏi, người khác không thấy
- PASS L1: Giáo viên mẫu đăng nhập bằng mã của chính tài khoản (OK)
- PASS L2: 10 lần sai từ một IP trong 15 phút => lần 11 RATE_LIMITED kể cả mã đúng (D-GEO-173) (RATE_LIMITED)
- PASS L3: 10 lần sai vào một tài khoản từ 10 IP => tài khoản RATE_LIMITED (dò phân tán) (RATE_LIMITED)
- PASS L4: mã đã thu hồi (geo_ops) không đăng nhập được, dù đúng (INVALID_CODE)
- PASS L5: mã hết hạn không đăng nhập được (INVALID_CODE)
- PASS L6: bảng mã chỉ có selector + HMAC, không có cột mã rõ (D-GEO-152)
- PASS L7: fn_student_login tạo đúng một học sinh student/light; vào lại cùng tên => cùng người (D-GEO-106, D-GEO-158)
- PASS L8: chỉ geo_ops tạo Giáo viên mẫu và cấp mã (geo_app bị từ chối)
- PASS V1: không đổi được nhãn bằng báo cáo của version khác (NOT_FOUND)
- PASS V2: nhãn "Đã kiểm chứng" bắt buộc báo cáo verified của chính version, kể cả với vai chủ (trigger + CHECK)
- PASS V3: version mới do ứng dụng tạo luôn needs_review (không thể chèn sẵn verified)

## 6. Chi phí Row-Level Security (RLS)
Ba điều kiện cho cùng kịch bản (16 kết nối, 15 giây): (1) cơ sở: không RLS, không `set_config`; (2) có `set_config('app.org_id', …, true)` và giao dịch bọc nhưng RLS tắt (tách chi phí của riêng việc đặt ngữ cảnh); (3) RLS bật trên 14 bảng (chính sách `org_id = (SELECT current_setting('app.org_id')::uuid)`; bảng nội dung dùng chung: `org_id IN (nền tảng, trường hiện tại)`).

| Kịch bản (16 kết nối) | 1. Cơ sở (không RLS, không set_config) tps | 2. Có `set_config` + giao dịch bọc, RLS tắt | 3. RLS bật |
|---|---|---|---|
| list | 32.826 | 19.375 (-41%) | 18.761 (-3% so với cột 2; -43% so với cột 1) |
| tracking | 24.956 | 15.524 (-38%) | 15.712 (+1% so với cột 2; -37% so với cột 1) |
| event_batch | 7.563 | 4.886 (-35%) | 5.957 (+22% so với cột 2; -21% so với cột 1) |
| open | 9.533 | 8.130 (-15%) | 9.760 (+20% so với cột 2; +2% so với cột 1) |

Đọc số: phần chi phí lớn nhất **không phải bản thân chính sách RLS mà là bắt buộc bọc mỗi thao tác trong giao dịch có `set_config`** (thêm vòng mạng: cột 2 so với cột 1, −15% đến −41% ở thử tải cục bộ, nặng nhất với kịch bản một câu lệnh). Bản thân chính sách (cột 3 so với cột 2) nằm trong nhiễu đo: −3% đến +22% (mỗi lần chạy trên máy dùng chung lệch ± 20%), tức không phát hiện được chi phí RLS lớn hơn nhiễu. Kế hoạch giữ nguyên chỉ mục (điều kiện chính sách thành `InitPlan` + `One-Time Filter`, tính một lần; kế hoạch Q09 với RLS bật vẫn dùng đúng chỉ mục). Kết luận thận trọng: RLS rẻ hơn nhiều so với yêu cầu giao dịch tường minh; mọi kịch bản vẫn ≫ nhu cầu.

**Lượt sửa sau RV:** chính sách RLS nay nằm sẵn trong `03_schema.sql` §13 (chưa bật), bật bằng `bench/rls_enable.sql` (D-GEO-127). Kiểm lại trên `geo_arch2` với chính sách mới (`app.org_id` không đặt ⇒ 0 hàng thay vì lỗi):
```
Đúng trường: số bài của lớp (mong đợi > 0)
 count 
    10
Sai trường (dù viết sai lọc org_id hay quên lọc): số bài của lớp (mong đợi 0)
 count 
     0
 count 
     0
 count 
     0
Sai trường nhưng nội dung nền tảng vẫn đọc được (kho đề, mong đợi > 0)
 count 
    60
Không đặt app.org_id (đặt rỗng): mong đợi lỗi hoặc 0 hàng
 
 count 
     0
```
Bộ kiểm cách ly 103 ca khi RLS bật: xem §5. Bảng chi phí trên là của lượt trước (chính sách cũ `bench/05_rls_on.sql`, cùng dạng điều kiện).

Kiểm cô lập khi RLS bật, lượt trước (`bench/05_rls_check.sql`): đúng trường đọc được; **sai trường, kể cả quên điều kiện `org_id` trong câu SQL, cho 0 hàng** ở `assignment`, `assignment_progress`, `event_log`; nội dung nền tảng (kho đề) vẫn đọc được từ mọi trường; không đặt `app.org_id` thì **lỗi** (đóng an toàn, không rò rỉ).
```
Đúng trường: số bài của lớp (mong đợi > 0)
 count 
    10
Sai trường (dù viết sai lọc org_id hay quên lọc): số bài của lớp (mong đợi 0)
 count 
     0
 count 
     0
 count 
     0
Sai trường nhưng nội dung nền tảng vẫn đọc được (kho đề, mong đợi > 0)
 count 
    60
Không đặt app.org_id (đặt rỗng): mong đợi lỗi hoặc 0 hàng
psql:bench/05_rls_check.sql:16: ERROR:  invalid input syntax for type uuid: ""
```
**Đã phán (D-GEO-127):** bật RLS cho bảng dữ liệu học sinh như lớp phòng thủ thứ hai sau khi đo chi phí trên khung mã thật; đường tiền xác thực đã thành hàm (`fn_student_login`, `fn_teacher_login`).

## 7. Chi phí ghi, kích thước và số chỉ mục
### 7.1. Số chỉ mục trên bảng ghi nhiều và chi phí mỗi chỉ mục
`event_log`: **đúng 1 chỉ mục btree mỗi partition** (không khóa chính, không khóa ngoại, không cột JSON); `ai_call_log`, `answer_guard_log`: 0 chỉ mục; `assignment_progress`: chỉ khóa chính, UPDATE đạt tỉ lệ HOT (§7.3); `qa_turn`: khóa chính + 3 chỉ mục (`qa_turn_assignment_idx` cho Q14, `qa_turn_user_idx` cho Q21, khóa duy nhất `(org_id, request_id)` cho lũy đẳng; ≈ 40 ghi/giây nên chi phí chấp nhận được); `durable_request`: chỉ khóa chính, dọn sau 24 giờ. Thí nghiệm dưới là của lượt trước (bảng `event_log` không đổi cấu trúc ngoài kiểu sự kiện `scene_shown`).

Thí nghiệm riêng (`bench/10_event_index_cost.sql`) cho 1.000.000 sự kiện, cùng dữ liệu, cùng một phiên:

| Cấu hình (1.000.000 sự kiện, chèn theo thứ tự thời gian, khóa ngẫu nhiên theo học sinh) | Bảng | Chỉ mục | Số chỉ mục | Byte/hàng (bảng + chỉ mục) | Thời gian chèn (s) |
|---|---|---|---|---|---|
| C không chỉ mục | 97 MB | 0 bytes | 0 | 101 | 1,6 |
| A đề xuất (1 chỉ mục) | 97 MB | 102 MB | 1 | 208 | 3,5 |
| D chỉ mục gọn (không org_id, không id) | 97 MB | 69 MB | 1 | 173 | 3,3 |
| E khóa (assignment, user, time, id), không org_id | 97 MB | 88 MB | 1 | 193 | 4,3 |
| J A + payload jsonb | 150 MB | 102 MB | 1 | 264 | 4,6 |
| B PK + 4 chỉ mục | 97 MB | 315 MB | 5 | 431 | 9,4 |


Đọc số: mỗi chỉ mục btree ngẫu nhiên theo học sinh làm **bảng sự kiện nở gấp đôi và chậm đi vài lần**; khóa chính cộng 3 chỉ mục "phòng thủ" (B) làm kích thước gấp ≈ 4 lần và thời gian chèn gấp ≈ 5 đến 6 lần so với không chỉ mục. Cấu hình đề xuất (A) giữ đúng một chỉ mục, phục vụ truy vấn drill-down Q20 (giáo viên xem dòng thời gian một học sinh) vì giá trị đo: 0,015 ms so với quét một partition cỡ trăm MB. Cũng đo hai phương án gọn hơn: **D** (bỏ `org_id` và `id` khỏi khóa) nhỏ hơn ≈ 32% nhưng mất khóa cursor ổn định; **E** (bỏ riêng `org_id`) nhỏ hơn ≈ 14%: không đáng đổi quy tắc "org_id dẫn đầu" (`02_multi_tenant.md` T2). **J** cho thấy một cột JSON nhỏ (≈ 40 byte) trên mọi hàng làm thêm ≈ 56 byte/hàng (+27%): vì vậy `event_log.arg` là `smallint` (mức gợi ý hoặc mã công cụ) thay cho `payload jsonb`. Nếu khối lượng sự kiện thực sự bền vững cao (xem `05_capacity.md` §3), chuyển sang D/E hoặc partition ngắn hơn là bước xem xét đầu tiên.

### 7.2. Kích thước bộ dữ liệu sạch (≈ 2 triệu sự kiện, `geo_arch2`)
```
### Kích thước theo bảng (partition gộp về bảng cha)
         tbl         | rows_est | table_size | index_size | n_index_on_parent 
---------------------+----------+------------+------------+-------------------
 event_log           |  2044126 | 197 MB     | 286 MB     |                 1
 qa_turn             |   169697 | 111 MB     | 41 MB      |                 4
 assignment_progress |   177762 | 28 MB      | 16 MB      |                 1
 problem_version     |     8060 | 8952 kB    | 3840 kB    |                 5
 class_member        |    20000 | 2016 kB    | 7136 kB    |                 3
 scene_build         |     8060 | 8104 kB    | 1024 kB    |                 2
 assignment          |    14000 | 2464 kB    | 3392 kB    |                 5
 hint                |    16120 | 4072 kB    | 1448 kB    |                 1
 problem             |     8060 | 2376 kB    | 2192 kB    |                 4
 solution            |     8060 | 3840 kB    | 480 kB     |                 1
 app_user            |    20201 | 1960 kB    | 2232 kB    |                 2
 problem_signature   |     7260 | 1472 kB    | 1344 kB    |                 2
 verification_report |     8060 | 1280 kB    | 784 kB     |                 2
 lecture             |      300 | 720 kB     | 560 kB     |                 4
 audit_log           |     2218 | 304 kB     | 232 kB     |                 2
 keyword             |      900 | 136 kB     | 176 kB     |                 2
 class               |      600 | 112 kB     | 200 kB     |                 4
 teacher_access_code |      200 | 72 kB      | 32 kB      |                 2
 share_grant         |       35 | 48 kB      | 32 kB      |                 2
 org                 |       56 | 48 kB      | 32 kB      |                 2
 problem_topic       |        5 | 48 kB      | 32 kB      |                 2
 subject             |        1 | 48 kB      | 32 kB      |                 2
 solid_template      |        7 | 48 kB      | 32 kB      |                 2
 ai_call_log         |        0 | 64 kB      | 0 bytes    |                 0
 answer_guard_log    |        0 | 32 kB      | 0 bytes    |                 0
 auth_failure        |        0 | 8192 bytes | 24 kB      |                 3
 probe_run           |        0 | 8192 bytes | 16 kB      |                 2
 probe_prompt        |        0 | 8192 bytes | 8192 bytes |                 1
 durable_request     |        0 | 0 bytes    | 8192 bytes |                 1
(29 rows)

### Chỉ mục (không tính partition con): kích thước và số lần dùng
         tbl         |                        idx                         |    size    | idx_scan 
---------------------+----------------------------------------------------+------------+----------
 app_user            | app_user_org_id_id_key                             | 1592 kB    |        0
 app_user            | app_user_pkey                                      | 640 kB     |        0
 assignment          | assignment_org_id_id_key                           | 896 kB     |        0
 assignment          | assignment_self_idx                                | 792 kB     |        0
 assignment          | assignment_class_problem_uq                        | 584 kB     |        0
 assignment          | assignment_class_idx                               | 584 kB     |        0
 assignment          | assignment_pkey                                    | 536 kB     |        0
 assignment_progress | assignment_progress_pkey                           | 16 MB      |        0
 audit_log           | audit_log_target_idx                               | 168 kB     |        0
 audit_log           | audit_log_pkey                                     | 64 kB      |        0
 auth_failure        | auth_failure_user_idx                              | 8192 bytes |        0
 auth_failure        | auth_failure_ip_idx                                | 8192 bytes |        0
 auth_failure        | auth_failure_pkey                                  | 8192 bytes |        0
 class               | class_teacher_idx                                  | 64 kB      |        0
 class               | class_org_id_id_key                                | 56 kB      |        0
 class               | class_pkey                                         | 40 kB      |        0
 class               | class_join_code_uq                                 | 40 kB      |        0
 class_member        | class_member_name_uq                               | 2808 kB    |        0
 class_member        | class_member_pkey                                  | 2184 kB    |        0
 class_member        | class_member_user_idx                              | 2144 kB    |        0
 durable_request     | durable_request_pkey                               | 8192 bytes |        0
 hint                | hint_pkey                                          | 1448 kB    |        0
 keyword             | keyword_pkey                                       | 96 kB      |        0
 keyword             | keyword_lecture_idx                                | 80 kB      |        0
 lecture             | lecture_search_idx                                 | 408 kB     |        0
 lecture             | lecture_org_id_slug_key                            | 48 kB      |        0
 lecture             | lecture_org_id_id_key                              | 40 kB      |        0
 lecture             | lecture_pkey                                       | 40 kB      |        0
 org                 | org_single_platform_uq                             | 16 kB      |        0
 org                 | org_pkey                                           | 16 kB      |        0
 probe_prompt        | probe_prompt_pkey                                  | 8192 bytes |        0
 probe_run           | probe_run_pkey                                     | 8192 bytes |        0
 probe_run           | probe_run_label_idx                                | 8192 bytes |        0
 problem             | problem_org_id_id_owner_user_id_key                | 920 kB     |        0
 problem             | problem_org_id_id_key                              | 736 kB     |        0
 problem             | problem_pkey                                       | 520 kB     |        0
 problem             | problem_catalog_filter_idx                         | 16 kB      |        0
 problem_signature   | problem_signature_lookup_idx                       | 760 kB     |        0
 problem_signature   | problem_signature_pkey                             | 584 kB     |        0
 problem_topic       | problem_topic_pkey                                 | 16 kB      |        0
 problem_topic       | problem_topic_subject_id_slug_key                  | 16 kB      |        0
 problem_version     | problem_version_hash_idx                           | 984 kB     |        0
 problem_version     | problem_version_org_id_problem_id_id_key           | 952 kB     |        0
 problem_version     | problem_version_org_id_id_key                      | 736 kB     |        0
 problem_version     | problem_version_problem_id_version_key             | 648 kB     |        0
 problem_version     | problem_version_pkey                               | 520 kB     |        0
 qa_turn             | qa_turn_user_idx                                   | 14 MB      |        0
 qa_turn             | qa_turn_assignment_idx                             | 13 MB      |        0
 qa_turn             | qa_turn_org_id_request_id_key                      | 11 MB      |        0
 qa_turn             | qa_turn_pkey                                       | 3744 kB    |        0
 scene_build         | scene_build_org_id_problem_version_id_build_no_key | 656 kB     |        0
 scene_build         | scene_build_pkey                                   | 368 kB     |        0
 share_grant         | share_grant_pkey                                   | 16 kB      |        0
 share_grant         | share_grant_lookup_idx                             | 16 kB      |        0
 solid_template      | solid_template_pkey                                | 16 kB      |        0
 solid_template      | solid_template_code_key                            | 16 kB      |        0
 solution            | solution_pkey                                      | 480 kB     |        0
 subject             | subject_code_key                                   | 16 kB      |        0
 subject             | subject_pkey                                       | 16 kB      |        0
 teacher_access_code | teacher_access_code_pkey                           | 16 kB      |        0
 teacher_access_code | teacher_access_code_selector_uq                    | 16 kB      |        0
 verification_report | verification_report_org_id_id_key                  | 480 kB     |        0
 verification_report | verification_report_pkey                           | 304 kB     |        0
(63 rows)

### Chỉ mục trên partition event_log (một partition đại diện: tháng có nhiều dữ liệu nhất)
     partition      | table_size | index_size | rows_est 
--------------------+------------+------------+----------
 event_log_default  | 0 bytes    | 8192 bytes |        0
 event_log_y2026m07 | 0 bytes    | 8192 bytes |        0
 event_log_y2026m08 | 13 MB      | 19 MB      |   135682
 event_log_y2026m09 | 66 MB      | 95 MB      |   679803
 event_log_y2026m10 | 69 MB      | 100 MB     |   713464
 event_log_y2026m11 | 50 MB      | 73 MB      |   515177
 event_log_y2026m12 | 0 bytes    | 8192 bytes |        0
 event_log_y2027m01 | 0 bytes    | 8192 bytes |        0
(8 rows)

### HOT update trên assignment_progress
 n_tup_upd | n_tup_hot_upd | pct_hot 
-----------+---------------+---------
         0 |             0 |        
(1 row)
```

### 7.3. Sau thử tải pgbench (lượt trước, `geo_arch`: bảng sự kiện đã nhận hàng chục triệu hàng từ các kịch bản ghi)
Chỉ để thấy tăng trưởng: kích thước `event_log_y2026m11` sau thử tải; các bảng khác gần như không đổi. Cho thấy partition theo tháng cần thiết khi tải thật; MVP **không** dọn (D-GEO-128), nên giám sát kích thước là đầu vào cho quyết định lưu giữ.
```
### Kích thước theo bảng (partition gộp về bảng cha)
         tbl         | rows_est | table_size | index_size | n_index_on_parent 
---------------------+----------+------------+------------+-------------------
 event_log           | 40770360 | 4987 MB    | 6494 MB    |                 1
 qa_turn             |   456779 | 77 MB      | 50 MB      |                 2
 assignment_progress |   183284 | 33 MB      | 17 MB      |                 1
 keyword             |      900 | 136 kB     | 33 MB      |                 3
 lecture             |      300 | 384 kB     | 16 MB      |                 4
 problem_version     |     8060 | 4344 kB    | 6600 kB    |                 4
 problem             |     8060 | 2376 kB    | 6712 kB    |                 3
 class_member        |    20000 | 2016 kB    | 6528 kB    |                 3
 scene_build         |     8060 | 7208 kB    | 920 kB     |                 2
 share_grant         |       35 | 48 kB      | 6256 kB    |                 2
 hint                |    16120 | 4072 kB    | 1448 kB    |                 1
 assignment          |    14000 | 2016 kB    | 2768 kB    |                 4
 app_user            |    20201 | 1960 kB    | 2232 kB    |                 2
 solution            |     8060 | 2976 kB    | 480 kB     |                 1
 verification_report |     8060 | 1280 kB    | 368 kB     |                 1
 audit_log           |     2257 | 304 kB     | 248 kB     |                 2
 class               |      600 | 112 kB     | 200 kB     |                 4
 problem_topic       |        5 | 48 kB      | 32 kB      |                 2
 subject             |        1 | 48 kB      | 32 kB      |                 2
 org                 |       56 | 48 kB      | 32 kB      |                 2
 ai_call_log         |        0 | 64 kB      | 0 bytes    |                 0
(21 rows)

### Chỉ mục (không tính partition con): kích thước và số lần dùng
         tbl         |                        idx                         |  size   | idx_scan 
---------------------+----------------------------------------------------+---------+----------
 app_user            | app_user_org_id_id_key                             | 1592 kB |    49000
 app_user            | app_user_pkey                                      | 640 kB  | 43282458
 assignment          | assignment_org_id_id_key                           | 896 kB  |  6697061
 assignment          | assignment_self_idx                                | 792 kB  |       10
 assignment          | assignment_class_idx                               | 552 kB  |  2514025
 assignment          | assignment_pkey                                    | 528 kB  |     3893
 assignment_progress | assignment_progress_pkey                           | 17 MB   | 29589083
 audit_log           | audit_log_target_idx                               | 176 kB  |       12
 audit_log           | audit_log_pkey                                     | 72 kB   |       12
 class               | class_teacher_idx                                  | 64 kB   | 43221208
 class               | class_org_id_id_key                                | 56 kB   |        5
 class               | class_join_code_uq                                 | 40 kB   |   694499
 class               | class_pkey                                         | 40 kB   |   727900
 class_member        | class_member_name_uq                               | 2200 kB |  1990996
 class_member        | class_member_pkey                                  | 2184 kB |   694527
 class_member        | class_member_user_idx                              | 2144 kB |  6666972
 hint                | hint_pkey                                          | 1448 kB |   368676
 keyword             | keyword_lecture_idx                                | 13 MB   |       10
 keyword             | keyword_trgm_idx                                   | 10 MB   |        1
 keyword             | keyword_pkey                                       | 9800 kB |   986312
 lecture             | lecture_search_idx                                 | 7904 kB |   328277
 lecture             | lecture_org_id_slug_key                            | 4296 kB |        0
 lecture             | lecture_org_id_id_key                              | 2456 kB |       98
 lecture             | lecture_pkey                                       | 1560 kB |  1143018
 org                 | org_pkey                                           | 16 kB   |        0
 org                 | org_single_platform_uq                             | 16 kB   |        0
 problem             | problem_org_id_id_key                              | 2520 kB |       36
 problem             | problem_catalog_filter_idx                         | 2320 kB |  1600936
 problem             | problem_pkey                                       | 1800 kB | 25993270
 problem_topic       | problem_topic_pkey                                 | 16 kB   |        0
 problem_topic       | problem_topic_subject_id_slug_key                  | 16 kB   |        0
 problem_version     | problem_version_hash_idx                           | 2280 kB |       11
 problem_version     | problem_version_org_id_id_key                      | 1760 kB |        0
 problem_version     | problem_version_problem_id_version_key             | 1392 kB |    16836
 problem_version     | problem_version_pkey                               | 1096 kB | 49616501
 qa_turn             | qa_turn_assignment_idx                             | 40 MB   |       12
 qa_turn             | qa_turn_pkey                                       | 11 MB   |       13
 scene_build         | scene_build_org_id_problem_version_id_build_no_key | 656 kB  |   816294
 scene_build         | scene_build_pkey                                   | 264 kB  |        0
 share_grant         | share_grant_lookup_idx                             | 4184 kB |        1
 share_grant         | share_grant_pkey                                   | 2024 kB |        0
 solution            | solution_pkey                                      | 480 kB  |  3051026
 subject             | subject_code_key                                   | 16 kB   |        0
 subject             | subject_pkey                                       | 16 kB   |        0
 verification_report | verification_report_pkey                           | 368 kB  |        0
(45 rows)

### Chỉ mục trên partition event_log (một partition đại diện: tháng có nhiều dữ liệu nhất)
     partition      | table_size | index_size | rows_est 
--------------------+------------+------------+----------
 event_log_default  | 0 bytes    | 8192 bytes |        0
 event_log_y2026m07 | 0 bytes    | 8192 bytes |        0
 event_log_y2026m08 | 14 MB      | 20 MB      |   143483
 event_log_y2026m09 | 65 MB      | 94 MB      |   674894
 event_log_y2026m10 | 68 MB      | 101 MB     |   706978
 event_log_y2026m11 | 4840 MB    | 6279 MB    | 39245004
 event_log_y2026m12 | 0 bytes    | 8192 bytes |        0
 event_log_y2027m01 | 0 bytes    | 8192 bytes |        0
(8 rows)

### HOT update trên assignment_progress
 n_tup_upd | n_tup_hot_upd | pct_hot 
-----------+---------------+---------
   3143762 |       3124084 |    99.4
(1 row)
```

## 8. Kiểm tự động "mẫu bị cấm"
`python3 bench/lint_queries.py` (luật S trên SQL của catalogue và luật K trên kế hoạch đã ghi):
```
lint: 453 kiểm đạt, 0 vi phạm
```
Tự kiểm bộ kiểm: 11 truy vấn xấu cố ý trong `bench/lint_selftest/` (OFFSET, `SELECT *`, không LIMIT, `count(*)`, `LIKE '%x%'`, hàm bọc cột, sự kiện không cận thời gian, thiếu `org_id`, JSON trong WHERE, đọc `solution`, "tham số IS NULL OR"): `lint: 109 kiểm đạt, 12 vi phạm`; cả 11 bị bắt.

## 9. Lịch sử sửa (trước/sau) trong quá trình kiểm chứng
| # | Phát hiện | Cách phát hiện | Trước | Sau |
|---|---|---|---|---|
| 1 | Q12 xếp hạng mọi bài khớp: chậm theo số bài khớp | `bench/03_scale_checks.sql` với 50.000 bài giảng (10.075 bài khớp) | v1: 22 đến 67 ms (nhiều lần đo, nhiễu máy), Bitmap Heap Scan 10.075 hàng + Sort | v2 (tập ứng viên ≤ 200 rồi lấy 5): 3,7 đến 6 ms |
| 2 | `geo_app` không gọi được `f_unaccent` | Chạy truy vấn bằng vai `geo_app` | `permission denied for function f_unaccent` | `GRANT EXECUTE ON FUNCTION f_unaccent TO geo_app, geo_ai` |
| 3 | Cột `qa_turn.answer_kind` làm kiểm "không bảng đọc được nào có cột answer" đỏ | Ca A5 của `06_isolation_test.sql` | tên gây nhầm với đáp số | đổi thành `reply_kind` (kiểu `qa_reply_kind`) |
| 4 | `fn_set_release` bản đầu cho khóa lại lời giải đã mở | Đối chiếu D-GEO-042 (phương án A) sau khi đọc `work/03_decisions.md` | cho đổi giờ ở mọi trạng thái | chỉ đổi khi chưa mở; giờ mới phải ở tương lai; "mở ngay" đặt `now()` của CSDL; ca B6b, B10, B10b |
| 5 | "Mở ngay" không thấy ngay trong cùng giao dịch kiểm thử | Ca B9: `now()` đứng yên trong giao dịch | truyền `clock_timestamp()` ⇒ lớn hơn `now()` ⇒ vẫn khóa | hàm tự lấy `now()`; ghi mẹo kiểm thử ở M-402 |
| 6 | Bảng sự kiện mỗi hàng có JSON nhỏ | `bench/10_event_index_cost.sql` cấu hình J | `payload jsonb` ≈ 264 byte/hàng | `arg smallint`: ≈ 207 byte/hàng |
| 7 | Ba chỉ mục không phục vụ truy vấn nào | Rà soát "mỗi chỉ mục có lý do truy vấn" | `org_parent_idx`, `verification_report_version_idx`, `share_grant_owner_idx` | đã bỏ; `keyword_lecture_idx` đổi thành `(org_id, lecture_id, term_key)` để phục vụ Q11d không Sort |
| 8 | Dữ liệu sinh ra 3,07 triệu sự kiện, lệch mục tiêu 2 triệu | Đếm sau nạp | 3.065.399 | 2.045.041 (chỉnh phân phối thao tác) |
| 9 | Bảng nhỏ trong dữ liệu thử cho kế hoạch Seq Scan (Q11b, Q18) | Đọc kế hoạch | Seq Scan 900 và 35 hàng | chứng minh dùng chỉ mục ở 150.000 từ khóa và 50.000 cấp phép (§3.2) |
| 10 | Khung kiểm "Seq Scan trên bảng lớn" báo nhầm partition rỗng | `lint_queries.py` | cảnh báo giả ở Q20b | phân biệt partition rỗng (không tính) |
| 11 | (RV-10) `geo_app` ghi được người dùng, kho nền tảng, nhãn máy, tiến độ, bài giao | RV chạy thử trên `geo_arch` | cả năm câu lệnh thành công | vai `geo_content`, `geo_ops`; hàm SECURITY DEFINER; trigger nền tảng và nhãn; CHECK giờ mở; ca R1…R5c PASS |
| 12 | (RV-04) `fn_get_solution_teacher` trả lời giải trường khác | RV chạy thử | nhận `final_answer` của trường B | chỉ đề kho nền tảng; ca T1…T6 PASS |
| 13 | Ca R1/R2 bản nháp dùng `geo.uuid_v7()` trong câu của `geo_app` ⇒ lỗi "permission denied for function" cũng là `insufficient_privilege`, PASS sai lý do | Đọc lỗi của lần chạy đầu | ca có thể PASS vì quyền hàm, không vì quyền bảng | ca dùng `gen_random_uuid()`; R1/R2 nay từ chối đúng ở bảng/trigger |
| 14 | `fn_self_lock_source` viết bằng `LANGUAGE sql SECURITY DEFINER` lập kế hoạch lại mỗi lần gọi | EXPLAIN Q05b | 0,469 ms | plpgsql: 0,024 ms (catalogue §4) |
| 15 | Bộ kiểm cách ly khi RLS bật: ca C5 FAIL và một INSERT bị chính sách chặn | Chạy lại với `rls_enable.sql` | ca đọc bảng bằng `geo_app` không đặt `app.org_id` | ca đặt `app.org_id` như `withTenant` của ứng dụng; 103/103 PASS (đúng hành vi: RLS chặn khi thiếu ngữ cảnh) |
| 16 | Dữ liệu thử lượt trước giao trùng `problem` cho cùng lớp (400 cặp, RV-16) | Chỉ mục duy nhất một phần mới làm nạp lỗi | `prob_n` ngẫu nhiên | `prob_n = 1 + (7i + 13g) mod 60` (khác nhau trong lớp) |
Tối đa 3 lượt tối ưu một truy vấn: Q12 dùng 1 lượt (lượt trước), Q05b/Q16 dùng 1 lượt (lượt này); không truy vấn nào còn vấn đề vượt ngưỡng.

## 10. Khoảng trống đã biết và điều chưa kiểm
| # | Khoảng trống | Hệ quả | Chủ |
|---|---|---|---|
| G-01 | Chưa đo tầng Java (CPU, luồng ảo, ghim, chữ ký thẻ), Redis, mạng, 2 nút ứng dụng, CSDL tách máy | NFR-GEO-001…006, 012 chỉ kiểm được khi có khung mã chạy được và thử tải HTTP | TechLead |
| G-02 | ~~Gần trùng chỉ theo băm~~ **Đóng (D-GEO-150, ADR-GEO-15):** chữ ký chuẩn; lược đồ, phép khớp, kiểm mỗi lần đã có và đã kiểm (C4, C7). Còn lại: thuật toán chuẩn hóa nhãn đỉnh/đối xứng nằm ở engine hình học (mã Java), chưa có | Ca F-GEO-226 chỉ đạt khi engine tính chữ ký đúng | Code (engine), QC (F-GEO-226) |
| G-03 | `qa_turn` (nay có câu trả lời ≤ 4.000 ký tự) chưa chia partition; MVP không dọn (D-GEO-128) | Bảng lớn nhanh hơn trước (≈ +400 byte/câu); ở tải bền vững cao cần chia | ChuDauTu, pháp lý (PO-21) |
| G-04 | Khối lượng sự kiện bền vững (`05_capacity.md` §3) có thể vượt xa 2 triệu; partition theo tháng có thể quá thô | Đổi tham số partition (tuần/ngày), xem D/E ở §7.1 | TechLead |
| G-05 | Chính sách RLS và `fn_student_login`/`fn_teacher_login` đã có; chưa đo chi phí trên khung mã thật (D-GEO-127) | Bật bằng `bench/rls_enable.sql` sau khi đo | Code, TechLead |
| G-06 | Truy vấn kho đề khi có đề của trường (chặng 2) cần `org_id IN (platform, :org)` | Dùng hợp hai nhánh có LIMIT; chưa đo | ARCH (chặng 2) |
| G-07 | ~~Cursor mờ~~ **Đóng:** khung mã đã cài AES-GCM gắn trường + `ListKind` + phiên bản 2 (M-504, `app/docs/CONFIG.md`) | — | — |
| G-08 | Độ bền commit đồng bộ đo trên macOS (không ép đĩa vật lý) | Độ trễ ghi bền trên đĩa thật cao hơn; cần đo lại | TechLead |
| G-09 | PgBouncer chế độ giao dịch chưa kiểm với câu lệnh chuẩn bị | Chỉ cần nếu số kết nối vượt giới hạn | TechLead |
| G-10 | ~~Lưu mã Giáo viên mẫu~~ **Đóng (D-GEO-152, 173):** `teacher_access_code`, `auth_failure`, `fn_teacher_login`; ca L1…L8 | Pepper và hạn dùng là cấu hình của khung mã | Code |
| G-11 | Đồng hồ thiết bị lệch chỉ ảnh hưởng hẹn giờ phía khách (ADR-GEO-10), không ảnh hưởng quyết định; chưa có ca thiết bị thật | Cần ca trình duyệt | QC |
| G-12 | Hỗn hợp tải và `s̄ = 20` sự kiện/lô là giả định | Cần số liệu thật từ giai đoạn thử | PO |
| G-13 | Kiểm soát đáp án (ADR-GEO-14) là mã Java của `ai-gateway`; lược đồ chỉ có `solution.answer_forms`, `answer_guard_log`, `probe_*`. Ngưỡng shingle và quy tắc số làm tròn (KS-2) là đề xuất | Cổng phát hành chỉ đo được khi bộ kiểm chạy trên bộ câu thử 22 câu | Code, QC; PO (câu hỏi M-406) |
| G-14 | Móc kiểm thử `fn_test_set_release` ở `bench/test_hooks.sql`; CODE chép vào `app/server/src/test/resources/db/testhooks/` (M-504) | Ca biên TC-GEO-0440, 0633, 0657, 0661, 0695 cần tệp ở profile test | Code |
| G-15 | Thăm dò trạng thái khóa (ADR-GEO-10) và đệm `release_at` chưa đo ở tầng Java/Redis; số 148 yêu cầu/giây là ca xấu tính tay | NFR-GEO-008, 064 đo bằng Playwright + Gatling | Code, QC |
