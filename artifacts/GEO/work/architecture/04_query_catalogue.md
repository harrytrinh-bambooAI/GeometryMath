# 04 — Danh mục truy vấn nóng của MVP

> Vai: ARCH. Trạng thái: **đề xuất**. Mọi SQL ở đây là SQL thật, đã chạy trên `geo_arch2` (PostgreSQL 17.11, cụm riêng cổng 54329; lượt sửa sau phản biện RV) với vai `geo_app`, trừ các tệp ghi `-- role: owner` (thân hàm SECURITY DEFINER đọc bảng nhạy cảm hoặc ghi bảng mà `geo_app` không còn quyền ghi thẳng, D-GEO-158). Dữ liệu thử: 50 trường, 600 lớp, 20.000 học sinh, ≈ 2,04 triệu sự kiện (`bench/01_seed.sql`; `07_verification.md` §2).
> Lượt sửa sau RV: bỏ Q11b, Q11c (D-GEO-166); thêm Q05b, Q05c, Q06b, Q06c, Q07b, Q08b, Q21, Q22, Q23, Q24; sửa Q02, Q02b, Q03, Q04, Q04b, Q05, Q06, Q07, Q08, Q09, Q09b, Q10, Q16, Q19b. Ánh xạ mã trạng thái của hàm sang mã lỗi API: mục 6.
> **Số đo là trên máy một nút cục bộ** (10 lõi, 24 GB RAM, bộ nhớ đệm ấm, bộ sinh tải cùng máy), **không phải kết quả sản xuất**. Thời gian là trung vị của 7 lần `EXPLAIN (ANALYZE, BUFFERS)` sau 3 lần khởi động; kế hoạch đầy đủ nằm trong phần mở ra của từng truy vấn. Dữ liệu và kế hoạch được dựng lại bằng `python3 bench/build_catalogue.py` từ `bench/results/`.
> Tần suất tính cho tải đều 333 yêu cầu/giây (S = 1000, T = 3 giây; `02_domain_model.md` M-GEO-10, 11) theo hỗn hợp ở `05_capacity.md` §2 (giả định). Quy ước tham số: `:'tên'` là tham số của psql (ở Java là `?`).
> Quy ước bố cục chỉ mục: `org_id` đứng đầu mọi chỉ mục thuộc phạm vi trường (`02_multi_tenant.md` §2). Cursor luôn là bộ khóa của dòng cuối đúng thứ tự `ORDER BY`; `LIMIT` = kích thước trang + 1; không `count(*)` để biết còn trang sau.

## 1. Tóm tắt

| Mã | Truy vấn | Thực thi (ms) | Lập kế hoạch (ms) | Nút quét | Dòng đọc | Trang bộ đệm |
|---|---|---|---|---|---|---|
| Q01 | Đăng nhập nhẹ bằng tên + mã lớp (IAM-01) | 0.005 | 0.038 | Index Scan, Index Only Scan | 2 | 7 |
| Q02 | Bài của em: danh sách bài giao theo các lớp em đang học (LRN-01) | 0.032 | 0.208 | Index Only Scan, Index Scan | 41 | 115 |
| Q02b | Bài tự học của em | 0.010 | 0.160 | Index Scan | 4 | 13 |
| Q03 | Bài của lớp (giáo viên): kèm số em đã mở và số em đã xem lời giải | 0.076 | 0.169 | Index Only Scan, Index Scan | 581 | 132 |
| Q04 | Chi tiết bài cho học sinh: đề + bản dựng hình, KHÔNG lời giải | 0.013 | 0.139 | Index Scan, Index Only Scan | 5 | 16 |
| Q04b | Trạng thái giờ mở (thăm dò nhẹ của trang đang mở, D-GEO-074) | 0.006 | 0.034 | Index Scan, Index Only Scan | 2 | 7 |
| Q05 | Lấy gợi ý theo mức (BR-04) | 0.009 | 0.088 | Index Scan, Index Only Scan | 4 | 14 |
| Q05b | Gợi ý của bài tự học: đề kho nếu kế thừa khóa, đề tự nhập nếu không (RV-15, D-GEO-155) | 0.023 | 0.042 | Index Scan | 3 | 37 |
| Q05c | Gọi `geo.fn_get_hint` đầy đủ (đọc + ghi bền + tiến độ) | 0.047 | 0.002 |  | 0 | 30 |
| Q06 | Lấy lời giải có kiểm soát giờ mở (BR-01, ANS-02): thân của `geo.fn_get_solution` | 0.007 | 0.078 | Index Scan, Index Only Scan | 3 | 10 |
| Q06b | Gọi `geo.fn_get_solution` cho bài lớp đã mở (đường nóng nhất ở đỉnh giờ mở) | 0.015 | 0.002 |  | 0 | 10 |
| Q06c | Gọi `geo.fn_get_solution` cho bài tự học kế thừa khóa (đã xem Gợi ý 2) | 0.031 | 0.001 |  | 0 | 38 |
| Q07 | Ghi sự kiện hàng loạt (PRF-01): lô 50 sự kiện của một học sinh | 0.092 | 0.058 | Insert, Index Scan, Index Only Scan | 2 | 259 |
| Q07b | Gọi `geo.fn_record_batch` đầy đủ: lô 50 sự kiện + tiến độ | 0.138 | 0.007 |  | 0 | 266 |
| Q08 | Cập nhật tiến độ (bộ đếm phi chuẩn hóa) sau mỗi lô | 0.013 | 0.005 | Insert | 0 | 7 |
| Q08b | Gọi `geo.fn_record_durable` cho sự kiện bền `unlock` (sau khi xem lời giải) | 0.044 | 0.001 |  | 0 | 23 |
| Q09 | Trang theo dõi lớp: ai đã mở, mức gợi ý, số câu hỏi, thời gian (LRN-03) | 0.109 | 0.212 | Index Scan, Index Only Scan | 257 | 575 |
| Q09b | Các thẻ tổng hợp của Theo dõi lớp (M-GEO-21, 22, 23) | 0.024 | 0.055 | Index Only Scan, Index Scan | 124 | 23 |
| Q10 | Đổi giờ mở và mở ngay (ANS-01): các câu lệnh bên trong `geo.fn_set_release` | 0.007 | 0.035 | Index Scan, Update | 3 | 9 |
| Q11 | Bấm từ khóa mở bài giảng (PO-34) | 0.004 | 0.026 | Index Scan | 2 | 6 |
| Q11d | Từ khóa liên quan trong hộp bài giảng (PO-34) | 0.003 | 0.008 | Index Scan | 3 | 3 |
| Q12 | Hỏi đáp tìm nguồn (PO-33): chọn tối đa 5 bài giảng làm nguồn cho câu trả lời | 0.084 | 0.039 | Bitmap Heap Scan, Index Scan | 130 | 67 |
| Q13 | Kho đề có lọc: cấp + loại hình (KNW-01) | 0.007 | 0.031 | Index Scan | 10 | 19 |
| Q13b | Kho đề chỉ lọc theo cấp | 0.013 | 0.031 | Index Scan | 32 | 52 |
| Q14 | Hỏi gì: các câu hỏi gần nhất của cả lớp trong một bài (LRN-03) | 0.047 | 0.048 | Index Scan, Index Only Scan | 144 | 250 |
| Q15 | Lịch sử đổi giờ mở của một bài | 0.002 | 0.014 | Index Scan | 0 | 2 |
| Q16 | Tự học kế thừa khóa: chữ ký chuẩn (hoặc băm) khớp bài lớp đang khóa của em (D-GEO-150, 151) | 0.015 | 0.377 | Index Only Scan, Index Scan | 7 | 28 |
| Q17 | Màn đầu của giáo viên: các lớp của mình kèm sĩ số | 0.015 | 0.024 | Index Scan, Index Only Scan | 141 | 17 |
| Q18 | Chặng 7 (chừa chỗ): trường nhận có cấp phép còn hiệu lực cho tài nguyên? | 0.003 | 0.010 | Seq Scan | 1 | 1 |
| Q19 | Giáo viên xem một đề trong kho: đề, nhãn, hình, gợi ý (KNW-02, LRN-04) | 0.011 | 0.053 | Index Scan | 5 | 12 |
| Q19b | Giáo viên xem lời giải của đề trong kho: thân `geo.fn_get_solution_teacher` | 0.008 | 0.056 | Index Only Scan, Index Scan | 4 | 12 |
| Q20 | Dòng thời gian một học sinh trong một bài (giáo viên bấm xem) | 0.009 | 0.078 | Index Scan | 7 | 13 |
| Q20b | ĐỐI CHỨNG: dòng thời gian không có cận thời gian | 0.014 | 0.119 | Seq Scan, Index Scan | 7 | 13 |
| Q21 | Lịch sử hỏi đáp của chính học sinh trên mọi bài (US-GEO-410, D-GEO-159, RV-19) | 0.006 | 0.014 | Index Scan | 18 | 13 |
| Q22 | Danh sách bài đã giao của giáo viên trên mọi lớp (US-GEO-701-SC6, D-GEO-129, RV-19) | 0.032 | 0.055 | Index Scan | 63 | 131 |
| Q23 | Xem trước "như học sinh" một bài đã giao (US-GEO-607, D-GEO-139, 180; RV-27) | 0.012 | 0.169 | Index Scan, Index Only Scan | 5 | 15 |
| Q24 | Các câu đọc của `geo.fn_teacher_login` (D-GEO-152, 173; RV-02) | 0.008 | 0.046 | Seq Scan, Index Scan | 2 | 5 |

Ghi chú: Q18 và Q24 hiện **Seq Scan** vì bảng chỉ có 35 hàng và 0 hàng (rẻ hơn dùng chỉ mục); chứng minh dùng chỉ mục ở quy mô lớn tại mục 3. Các dòng `…_call` đo cả thân hàm plpgsql (kế hoạch chỉ hiện Function Scan). Mọi `Seq Scan` còn lại trong Q20b nằm ở partition rỗng (tương lai và mặc định). Không truy vấn nóng nào có Seq Scan trên bảng lớn (`python3 bench/lint_queries.py`: 0 vi phạm).

## 2. Từng truy vấn

### Q01 — Đăng nhập nhẹ bằng tên + mã lớp (IAM-01)
- **Ai gọi:** Học sinh (trước khi có thẻ); sau khi thẻ hết hạn là làm mới
- **Tần suất ước tính:** ≈ 7/giây (2%); đỉnh đầu giờ học cao hơn nhưng nhẹ
- **Chỉ mục phục vụ:** `class_join_code_uq`, `class_member_name_uq`
- **Phân trang / giới hạn:** Không (tra khóa duy nhất, tối đa 1 hàng)
- **Độ phức tạp:** O(log N) hai lần tra chỉ mục duy nhất
- **Ghi chú:** Là câu đọc bên trong `geo.fn_student_login` (D-GEO-127): ứng dụng gọi hàm, không chạy câu này trực tiếp. Không có học sinh khớp tên thì hàm tạo đúng một `app_user` student/light + `class_member` (D-GEO-106, 158); `geo_app` không có INSERT trên hai bảng đó. Ngoại lệ org_id đứng đầu ghi ở chỉ mục (D-GEO-051).
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.005 ms, lập kế hoạch 0.038 ms, trang bộ đệm 7, dòng đọc ở các nút quét 2, dòng trả về 1.

```sql
-- lint-allow: R-Q04 (tra khóa duy nhất join_code và (org, lớp, name_key): tối đa 1 hàng)
-- lint-allow: R-Q10 (đăng nhập chưa biết trường; tra theo mã lớp toàn cục, D-GEO-051)
-- Đăng nhập nhẹ bằng tên + mã lớp (IAM-01). Một vòng truy vấn: lớp theo mã, học sinh theo tên chuẩn hóa.
SELECT c.id AS class_id, c.org_id, c.level, cm.user_id
FROM geo.class c
LEFT JOIN geo.class_member cm
       ON cm.org_id = c.org_id AND cm.class_id = c.id AND cm.name_key = :'name_key' AND cm.left_at IS NULL
WHERE c.join_code = :'join_code' AND c.archived_at IS NULL;
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Nested Loop Left Join  (cost=0.69..4.04 rows=1 width=52) (actual time=0.002..0.002 rows=1 loops=1)
   Buffers: shared hit=7
   ->  Index Scan using class_join_code_uq on class c  (cost=0.28..2.49 rows=1 width=36) (actual time=0.001..0.001 rows=1 loops=1)
         Index Cond: (join_code = 'C58A58'::text)
         Filter: (archived_at IS NULL)
         Buffers: shared hit=3
   ->  Index Only Scan using class_member_name_uq on class_member cm  (cost=0.41..1.54 rows=1 width=48) (actual time=0.001..0.001 rows=1 loops=1)
         Index Cond: ((org_id = c.org_id) AND (class_id = c.id) AND (name_key = 'hoc sinh 128'::text))
         Heap Fetches: 0
         Buffers: shared hit=4
 Planning:
   Buffers: shared hit=6
 Planning Time: 0.038 ms
 Execution Time: 0.005 ms
```
</details>

### Q02 — Bài của em: danh sách bài giao theo các lớp em đang học (LRN-01)
- **Ai gọi:** Học sinh, màn "Bài của em"
- **Tần suất ước tính:** ≈ 27/giây (8%)
- **Chỉ mục phục vụ:** `class_member_user_idx`, `assignment_class_idx`, `assignment_progress_pkey`, các khóa chính `problem_version`, `problem`
- **Phân trang / giới hạn:** Cursor = `assignment.id` (UUIDv7, mới nhất trước, `id < cursor`); trang 20 (LIMIT 21), trần 50
- **Độ phức tạp:** O(k·(log A + 21·log N)); k = số lớp của em (≤ 8). Mỗi lớp lấy riêng 21 dòng rồi gộp, vì một học sinh thuộc nhiều lớp mà vẫn tránh sắp xếp toàn bộ
- **Ghi chú:** Không N+1: một câu lệnh nối `problem_version`, `problem`, `assignment_progress` bằng LATERAL/JOIN. Trạng thái giờ mở tính bằng đồng hồ CSDL (D-GEO-041). Nút Sort cuối chỉ sắp ≤ 21·k dòng đã có.
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.032 ms, lập kế hoạch 0.208 ms, trang bộ đệm 115, dòng đọc ở các nút quét 41, dòng trả về 10.

```sql
-- Bài của em (LRN-01): mọi bài lớp của các lớp em đang học, mới nhất trước, phân trang theo khóa (cursor = assignment.id).
-- Trang đầu: cursor = ffffffff-ffff-ffff-ffff-ffffffffffff. Trang kế: cursor = id của dòng cuối trang trước. LIMIT = size + 1.
SELECT a.id, a.class_id, a.release_at,
       CASE WHEN a.release_at IS NULL THEN 'unset' WHEN a.release_at > now() THEN 'scheduled' ELSE 'open' END AS release_state,
       p.title, p.solid_kind, pv.verify_status, pv.approval_status,
       ap.hint_level_max, ap.solution_viewed_at, ap.last_active_at
FROM geo.class_member mc
CROSS JOIN LATERAL (
    SELECT x.id, x.org_id, x.class_id, x.release_at, x.content_org_id, x.problem_version_id
    FROM geo.assignment x
    WHERE x.org_id = mc.org_id AND x.class_id = mc.class_id AND x.kind = 'class' AND x.id < :'cursor_max'
    ORDER BY x.id DESC LIMIT 21) a
JOIN geo.problem_version pv ON pv.org_id = a.content_org_id AND pv.id = a.problem_version_id
JOIN geo.problem p          ON p.org_id = pv.org_id AND p.id = pv.problem_id
LEFT JOIN geo.assignment_progress ap
       ON ap.org_id = a.org_id AND ap.assignment_id = a.id AND ap.user_id = :'student'
WHERE mc.org_id = :'org' AND mc.user_id = :'student' AND mc.left_at IS NULL
ORDER BY a.id DESC
LIMIT 21;
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Limit  (cost=10.02..10.03 rows=1 width=124) (actual time=0.023..0.024 rows=10 loops=1)
   Buffers: shared hit=115
   ->  Sort  (cost=10.02..10.03 rows=1 width=124) (actual time=0.023..0.024 rows=10 loops=1)
         Sort Key: x.id DESC
         Sort Method: quicksort  Memory: 26kB
         Buffers: shared hit=115
         ->  Nested Loop Left Join  (cost=1.68..10.01 rows=1 width=124) (actual time=0.005..0.022 rows=10 loops=1)
               Buffers: shared hit=115
               ->  Nested Loop  (cost=1.26..6.92 rows=1 width=90) (actual time=0.004..0.014 rows=10 loops=1)
                     Buffers: shared hit=76
                     ->  Nested Loop  (cost=0.98..6.56 rows=1 width=112) (actual time=0.003..0.010 rows=10 loops=1)
                           Buffers: shared hit=46
                           ->  Nested Loop  (cost=0.69..4.06 rows=1 width=88) (actual time=0.002..0.004 rows=10 loops=1)
                                 Buffers: shared hit=16
                                 ->  Index Only Scan using class_member_user_idx on class_member mc  (cost=0.41..1.53 rows=1 width=32) (actual time=0.001..0.001 rows=1 loops=1)
                                       Index Cond: ((org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid) AND (user_id = '019e976e-3137-7f97-802d-4fe0a56bf727'::uuid))
                                       Heap Fetches: 0
                                       Buffers: shared hit=4
                                 ->  Limit  (cost=0.28..2.50 rows=1 width=88) (actual time=0.001..0.003 rows=10 loops=1)
                                       Buffers: shared hit=12
                                       ->  Index Scan using assignment_class_idx on assignment x  (cost=0.28..2.50 rows=1 width=88) (actual time=0.001..0.002 rows=10 loops=1)
                                             Index Cond: ((org_id = mc.org_id) AND (class_id = mc.class_id) AND (id < 'ffffffff-ffff-ffff-ffff-ffffffffffff'::uuid))
                                             Buffers: shared hit=12
                           ->  Index Scan using problem_version_org_id_id_key on problem_version pv  (cost=0.28..2.50 rows=1 width=56) (actual time=0.000..0.000 rows=1 loops=10)
                                 Index Cond: ((org_id = x.content_org_id) AND (id = x.problem_version_id))
                                 Buffers: shared hit=30
                     ->  Index Scan using problem_pkey on problem p  (cost=0.28..0.35 rows=1 width=58) (actual time=0.000..0.000 rows=1 loops=10)
                           Index Cond: (id = pv.problem_id)
                           Filter: (pv.org_id = org_id)
                           Buffers: shared hit=30
               ->  Index Scan using assignment_progress_pkey on assignment_progress ap  (cost=0.42..2.64 rows=1 width=50) (actual time=0.001..0.001 rows=1 loops=10)
                     Index Cond: ((org_id = x.org_id) AND (assignment_id = x.id) AND (user_id = '019e976e-3137-7f97-802d-4fe0a56bf727'::uuid))
                     Buffers: shared hit=39
 Planning:
   Buffers: shared hit=21
 Planning Time: 0.208 ms
 Execution Time: 0.032 ms
```
</details>

### Q02b — Bài tự học của em
- **Ai gọi:** Học sinh, tab Tự học
- **Tần suất ước tính:** ≈ 3/giây (một phần của 8% danh sách)
- **Chỉ mục phục vụ:** `assignment_self_idx`
- **Phân trang / giới hạn:** Cursor = `assignment.id`; trang 20, trần 50
- **Độ phức tạp:** O(log A + 21·log N)
- **Ghi chú:** `lock_source_assignment_id` chỉ là dấu vết lúc tạo (D-GEO-151); trạng thái khóa thật tính ở cổng mỗi lần xin.
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.010 ms, lập kế hoạch 0.160 ms, trang bộ đệm 13, dòng đọc ở các nút quét 4, dòng trả về 1.

```sql
-- Bài tự học của em (cùng bảng assignment, kind='self'), keyset theo id. lock_source_assignment_id chỉ là dấu vết lúc tạo (D-GEO-151);
-- trạng thái khóa thật của từng bài tính khi mở bài (fn_solution_gate), không ở danh sách.
SELECT a.id, a.lock_source_assignment_id, p.title, pv.verify_status, ap.hint_level_max, ap.last_active_at
FROM geo.assignment a
JOIN geo.problem_version pv ON pv.org_id = a.content_org_id AND pv.id = a.problem_version_id
JOIN geo.problem p          ON p.org_id = pv.org_id AND p.id = pv.problem_id
LEFT JOIN geo.assignment_progress ap ON ap.org_id = a.org_id AND ap.assignment_id = a.id AND ap.user_id = a.owner_user_id
WHERE a.org_id = :'self_org' AND a.owner_user_id = :'self_student' AND a.kind = 'self' AND a.id < :'cursor_max'
ORDER BY a.id DESC
LIMIT 21;
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Limit  (cost=1.27..8.02 rows=1 width=68) (actual time=0.004..0.004 rows=1 loops=1)
   Buffers: shared hit=13
   ->  Nested Loop Left Join  (cost=1.27..8.02 rows=1 width=68) (actual time=0.004..0.004 rows=1 loops=1)
         Buffers: shared hit=13
         ->  Nested Loop  (cost=0.85..5.37 rows=1 width=90) (actual time=0.003..0.003 rows=1 loops=1)
               Buffers: shared hit=9
               ->  Nested Loop  (cost=0.56..5.01 rows=1 width=116) (actual time=0.002..0.002 rows=1 loops=1)
                     Buffers: shared hit=6
                     ->  Index Scan using assignment_self_idx on assignment a  (cost=0.28..2.50 rows=1 width=96) (actual time=0.001..0.001 rows=1 loops=1)
                           Index Cond: ((org_id = '019dfd06-2d4a-7607-be09-124bf8a4b35a'::uuid) AND (owner_user_id = '019e9780-1357-7f57-bd90-57e19908d609'::uuid) AND (id < 'ffffffff-ffff-ffff-ffff-ffffffffffff'::uuid))
                           Buffers: shared hit=3
                     ->  Index Scan using problem_version_org_id_id_key on problem_version pv  (cost=0.28..2.50 rows=1 width=52) (actual time=0.001..0.001 rows=1 loops=1)
                           Index Cond: ((org_id = a.content_org_id) AND (id = a.problem_version_id))
                           Buffers: shared hit=3
               ->  Index Scan using problem_pkey on problem p  (cost=0.28..0.35 rows=1 width=54) (actual time=0.001..0.001 rows=1 loops=1)
                     Index Cond: (id = pv.problem_id)
                     Filter: (pv.org_id = org_id)
                     Buffers: shared hit=3
         ->  Index Scan using assignment_progress_pkey on assignment_progress ap  (cost=0.42..2.64 rows=1 width=58) (actual time=0.001..0.001 rows=1 loops=1)
               Index Cond: ((org_id = '019dfd06-2d4a-7607-be09-124bf8a4b35a'::uuid) AND (assignment_id = a.id) AND (user_id = '019e9780-1357-7f57-bd90-57e19908d609'::uuid))
               Buffers: shared hit=4
 Planning:
   Buffers: shared hit=33
 Planning Time: 0.160 ms
 Execution Time: 0.010 ms
```
</details>

### Q03 — Bài của lớp (giáo viên): kèm số em đã mở và số em đã xem lời giải
- **Ai gọi:** Giáo viên, màn lớp / chọn bài để theo dõi
- **Tần suất ước tính:** ≈ 3/giây (một phần của 3% theo dõi lớp)
- **Chỉ mục phục vụ:** `assignment_class_idx`, `assignment_progress_pkey` (đếm theo khoảng), `class_teacher_idx`
- **Phân trang / giới hạn:** Cursor = `assignment.id` (mới nhất trước); trang 20, trần 50
- **Độ phức tạp:** O(21·(log N + c)) với c ≤ cỡ lớp (đếm quét khoảng chỉ mục của MỘT bài)
- **Ghi chú:** Đếm `count(*)` bị cấm trên bảng lớn, nhưng ở đây phạm vi là một bài (≤ cỡ lớp hàng), có `lint-allow` ghi lý do. Kiểm quyền: giáo viên của lớp (`c.teacher_id`).
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.076 ms, lập kế hoạch 0.169 ms, trang bộ đệm 132, dòng đọc ở các nút quét 581, dòng trả về 10.

```sql
-- lint-allow: R-Q05 (đếm trong phạm vi MỘT bài, tối đa cỡ lớp hàng, quét chỉ mục)
-- Bài của lớp (giáo viên): mới nhất trước, kèm số em đã mở / đã xem lời giải (đếm trong phạm vi một bài, tối đa cỡ lớp).
SELECT a.id, a.release_at, a.release_rev,
       CASE WHEN a.release_at IS NULL THEN 'unset' WHEN a.release_at > now() THEN 'scheduled' ELSE 'open' END AS release_state,
       p.title, pv.verify_status, pv.approval_status, cnt.opened, cnt.viewed_solution
FROM geo.class c
CROSS JOIN LATERAL (
    SELECT x.id, x.org_id, x.release_at, x.release_rev, x.content_org_id, x.problem_version_id
    FROM geo.assignment x
    WHERE x.org_id = c.org_id AND x.class_id = c.id AND x.kind = 'class' AND x.id < :'cursor_max'
    ORDER BY x.id DESC LIMIT 21) a
JOIN geo.problem_version pv ON pv.org_id = a.content_org_id AND pv.id = a.problem_version_id
JOIN geo.problem p          ON p.org_id = pv.org_id AND p.id = pv.problem_id
CROSS JOIN LATERAL (
    SELECT count(*) AS opened, count(ap.solution_viewed_at) AS viewed_solution
    FROM geo.assignment_progress ap
    WHERE ap.org_id = a.org_id AND ap.assignment_id = a.id) cnt
WHERE c.org_id = :'org' AND c.id = :'class' AND c.teacher_id = :'teacher'
ORDER BY a.id DESC
LIMIT 21;
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Limit  (cost=9.47..9.47 rows=1 width=106) (actual time=0.066..0.067 rows=10 loops=1)
   Buffers: shared hit=132
   ->  Sort  (cost=9.47..9.47 rows=1 width=106) (actual time=0.066..0.067 rows=10 loops=1)
         Sort Key: x.id DESC
         Sort Method: quicksort  Memory: 26kB
         Buffers: shared hit=132
         ->  Nested Loop  (cost=3.77..9.46 rows=1 width=106) (actual time=0.010..0.065 rows=10 loops=1)
               Buffers: shared hit=132
               ->  Nested Loop  (cost=1.12..6.79 rows=1 width=74) (actual time=0.004..0.014 rows=10 loops=1)
                     Buffers: shared hit=75
                     ->  Nested Loop  (cost=0.84..6.43 rows=1 width=100) (actual time=0.003..0.010 rows=10 loops=1)
                           Buffers: shared hit=45
                           ->  Nested Loop  (cost=0.56..3.92 rows=1 width=76) (actual time=0.002..0.004 rows=10 loops=1)
                                 Buffers: shared hit=15
                                 ->  Index Only Scan using class_teacher_idx on class c  (cost=0.28..1.40 rows=1 width=32) (actual time=0.001..0.001 rows=1 loops=1)
                                       Index Cond: ((org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid) AND (teacher_id = '019e306e-4994-7421-b184-efe9d1162e64'::uuid) AND (id = '019e63ec-b0e8-7809-a77d-5445ef49f519'::uuid))
                                       Heap Fetches: 0
                                       Buffers: shared hit=3
                                 ->  Limit  (cost=0.28..2.50 rows=1 width=76) (actual time=0.001..0.003 rows=10 loops=1)
                                       Buffers: shared hit=12
                                       ->  Index Scan using assignment_class_idx on assignment x  (cost=0.28..2.50 rows=1 width=76) (actual time=0.001..0.003 rows=10 loops=1)
                                             Index Cond: ((org_id = c.org_id) AND (class_id = c.id) AND (id < 'ffffffff-ffff-ffff-ffff-ffffffffffff'::uuid))
                                             Buffers: shared hit=12
                           ->  Index Scan using problem_version_org_id_id_key on problem_version pv  (cost=0.28..2.50 rows=1 width=56) (actual time=0.000..0.000 rows=1 loops=10)
                                 Index Cond: ((org_id = x.content_org_id) AND (id = x.problem_version_id))
                                 Buffers: shared hit=30
                     ->  Index Scan using problem_pkey on problem p  (cost=0.28..0.35 rows=1 width=54) (actual time=0.000..0.000 rows=1 loops=10)
                           Index Cond: (id = pv.problem_id)
                           Filter: (pv.org_id = org_id)
                           Buffers: shared hit=30
               ->  Aggregate  (cost=2.64..2.65 rows=1 width=16) (actual time=0.005..0.005 rows=1 loops=10)
                     Buffers: shared hit=57
                     ->  Index Scan using assignment_progress_pkey on assignment_progress ap  (cost=0.42..2.64 rows=1 width=8) (actual time=0.001..0.003 rows=55 loops=10)
                           Index Cond: ((org_id = x.org_id) AND (assignment_id = x.id))
                           Buffers: shared hit=57
 Planning:
   Buffers: shared hit=21
 Planning Time: 0.169 ms
 Execution Time: 0.076 ms
```
</details>

### Q04 — Chi tiết bài cho học sinh: đề + bản dựng hình, KHÔNG lời giải
- **Ai gọi:** Học sinh, mở một bài (E4, E6)
- **Tần suất ước tính:** ≈ 33/giây (10%)
- **Chỉ mục phục vụ:** `assignment_org_id_id_key`, `class_member_pkey` (EXISTS), khóa chính `problem_version`/`problem`, `scene_build_org_id_problem_version_id_build_no_key`
- **Phân trang / giới hạn:** Không (1 hàng)
- **Độ phức tạp:** O(log N) × 5 lần tra chỉ mục
- **Ghi chú:** Cột trả về chỉ gồm `problem_version`, `scene_build` (không có bảng `solution`; vai `geo_app` cũng không đọc được bảng đó), thêm `hidden_measures` (biểu thức đại lượng công cụ đo phải ẩn, không có giá trị; D-GEO-024, 133). Tọa độ là dữ kiện của đề (D-GEO-154). Nối đề bằng `(content_org_id, problem_version_id)` (RV-12). Nội dung bất biến theo `problem_version_id`: đệm Redis theo khóa phiên bản, phần phụ thuộc người (giờ mở, quyền) vẫn tra CSDL.
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.013 ms, lập kế hoạch 0.139 ms, trang bộ đệm 16, dòng đọc ở các nút quét 5, dòng trả về 1.

```sql
-- Chi tiết bài cho học sinh: đề + bản dựng hình + danh sách đại lượng ẩn (biểu thức, không giá trị; D-GEO-024, 133), KHÔNG lời giải, KHÔNG gợi ý.
-- Kiểm tư cách lớp ngay trong truy vấn (BR-12). Tọa độ là dữ kiện của đề (D-GEO-154).
SELECT a.id, a.release_at,
       CASE WHEN a.release_at IS NULL THEN 'unset' WHEN a.release_at > now() THEN 'scheduled' ELSE 'open' END AS release_state,
       a.release_rev, a.allow_hint, a.allow_qa,
       pv.id AS problem_version_id, pv.statement, pv.verify_status, pv.approval_status, pv.ai_authored,
       p.title, p.solid_kind, sb.coord_source, sb.schema_version, sb.spec, sb.coords, sb.hidden_measures
FROM geo.assignment a
JOIN geo.problem_version pv ON pv.org_id = a.content_org_id AND pv.id = a.problem_version_id
JOIN geo.problem p          ON p.org_id = pv.org_id AND p.id = pv.problem_id
CROSS JOIN LATERAL (
    SELECT s.coord_source, s.schema_version, s.spec, s.coords, s.hidden_measures
    FROM geo.scene_build s
    WHERE s.org_id = pv.org_id AND s.problem_version_id = pv.id
    ORDER BY s.build_no DESC LIMIT 1) sb
WHERE a.org_id = :'org' AND a.id = :'assignment'
  AND ((a.kind = 'class' AND EXISTS (SELECT 1 FROM geo.class_member cm
                                     WHERE cm.org_id = a.org_id AND cm.class_id = a.class_id AND cm.user_id = :'student' AND cm.left_at IS NULL))
    OR (a.kind = 'self' AND a.owner_user_id = :'student'));
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Nested Loop  (cost=1.13..9.44 rows=1 width=1343) (actual time=0.005..0.006 rows=1 loops=1)
   Buffers: shared hit=16
   ->  Nested Loop  (cost=0.85..6.91 rows=1 width=479) (actual time=0.004..0.004 rows=1 loops=1)
         Buffers: shared hit=13
         ->  Nested Loop  (cost=0.57..6.55 rows=1 width=485) (actual time=0.003..0.003 rows=1 loops=1)
               Buffers: shared hit=10
               ->  Index Scan using assignment_org_id_id_key on assignment a  (cost=0.29..4.05 rows=1 width=62) (actual time=0.002..0.002 rows=1 loops=1)
                     Index Cond: ((org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid) AND (id = '01a1b2ee-51df-79ad-8354-128825f23080'::uuid))
                     Filter: (((kind = 'class'::geo.assignment_kind) AND EXISTS(SubPlan 1)) OR ((kind = 'self'::geo.assignment_kind) AND (owner_user_id = '019e976e-3137-7f97-802d-4fe0a56bf727'::uuid)))
                     Buffers: shared hit=7
                     SubPlan 1
                       ->  Index Only Scan using class_member_user_idx on class_member cm  (cost=0.41..1.54 rows=1 width=0) (actual time=0.001..0.001 rows=1 loops=1)
                             Index Cond: ((org_id = a.org_id) AND (user_id = '019e976e-3137-7f97-802d-4fe0a56bf727'::uuid) AND (class_id = a.class_id))
                             Heap Fetches: 0
                             Buffers: shared hit=4
               ->  Index Scan using problem_version_org_id_id_key on problem_version pv  (cost=0.28..2.50 rows=1 width=439) (actual time=0.001..0.001 rows=1 loops=1)
                     Index Cond: ((org_id = a.content_org_id) AND (id = a.problem_version_id))
                     Buffers: shared hit=3
         ->  Index Scan using problem_pkey on problem p  (cost=0.28..0.35 rows=1 width=58) (actual time=0.000..0.000 rows=1 loops=1)
               Index Cond: (id = pv.problem_id)
               Filter: (pv.org_id = org_id)
               Buffers: shared hit=3
   ->  Limit  (cost=0.28..2.50 rows=1 width=850) (actual time=0.001..0.001 rows=1 loops=1)
         Buffers: shared hit=3
         ->  Index Scan Backward using scene_build_org_id_problem_version_id_build_no_key on scene_build s  (cost=0.28..2.50 rows=1 width=850) (actual time=0.001..0.001 rows=1 loops=1)
               Index Cond: ((org_id = pv.org_id) AND (problem_version_id = pv.id))
               Buffers: shared hit=3
 Planning:
   Buffers: shared hit=18
 Planning Time: 0.139 ms
 Execution Time: 0.013 ms
```
</details>

### Q04b — Trạng thái giờ mở (thăm dò nhẹ của trang đang mở, D-GEO-074)
- **Ai gọi:** Học sinh, trang đang chờ lời giải
- **Tần suất ước tính:** Thường trực khi trang chờ khóa, mỗi 6 + [0; 1,5) giây (D-GEO-157): ca xấu ≈ 148/giây HTTP; ≈ 17/giây xuống CSDL nhờ đệm `release_at` 2 giây theo bài (`05_capacity.md` §2)
- **Chỉ mục phục vụ:** `assignment_org_id_id_key`, `class_member_pkey`
- **Phân trang / giới hạn:** Không (1 hàng)
- **Độ phức tạp:** O(log N) × 2
- **Ghi chú:** Trả `release_in_seconds` để thiết bị đặt hẹn giờ, không dùng giờ thiết bị. Chỉ được đệm `release_at` (≤ 2 giây, xóa khi đổi giờ); KHÔNG đệm và KHÔNG trả quyết định "được xem" (D-GEO-157, RV-09): xem lời giải luôn gọi `fn_get_solution`.
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.006 ms, lập kế hoạch 0.034 ms, trang bộ đệm 7, dòng đọc ở các nút quét 2, dòng trả về 1.

```sql
-- lint-allow: R-Q04 (tra khóa chính: 1 hàng)
-- Trạng thái giờ mở (thăm dò nhẹ của trang đang mở, D-GEO-074): ba trạng thái tính bằng đồng hồ CSDL, kèm số giây còn lại để thiết bị đặt hẹn giờ.
SELECT a.release_rev,
       CASE WHEN a.release_at IS NULL THEN 'unset' WHEN a.release_at > now() THEN 'scheduled' ELSE 'open' END AS release_state,
       CASE WHEN a.release_at > now() THEN ceil(extract(epoch FROM a.release_at - now()))::int END AS release_in_seconds
FROM geo.assignment a
WHERE a.org_id = :'org' AND a.id = :'assignment'
  AND ((a.kind = 'class' AND EXISTS (SELECT 1 FROM geo.class_member cm
                                     WHERE cm.org_id = a.org_id AND cm.class_id = a.class_id AND cm.user_id = :'student' AND cm.left_at IS NULL))
    OR (a.kind = 'self' AND a.owner_user_id = :'student'));
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Index Scan using assignment_org_id_id_key on assignment a  (cost=0.29..4.07 rows=1 width=40) (actual time=0.002..0.002 rows=1 loops=1)
   Index Cond: ((org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid) AND (id = '01a1b2ee-51df-79ad-8354-128825f23080'::uuid))
   Filter: (((kind = 'class'::geo.assignment_kind) AND EXISTS(SubPlan 1)) OR ((kind = 'self'::geo.assignment_kind) AND (owner_user_id = '019e976e-3137-7f97-802d-4fe0a56bf727'::uuid)))
   Buffers: shared hit=7
   SubPlan 1
     ->  Index Only Scan using class_member_user_idx on class_member cm  (cost=0.41..1.54 rows=1 width=0) (actual time=0.001..0.001 rows=1 loops=1)
           Index Cond: ((org_id = a.org_id) AND (user_id = '019e976e-3137-7f97-802d-4fe0a56bf727'::uuid) AND (class_id = a.class_id))
           Heap Fetches: 0
           Buffers: shared hit=4
 Planning Time: 0.034 ms
 Execution Time: 0.006 ms
```
</details>

### Q05 — Lấy gợi ý theo mức (BR-04)
- **Ai gọi:** Học sinh, tab Gợi ý
- **Tần suất ước tính:** ≈ 27/giây (8%)
- **Chỉ mục phục vụ:** `assignment_org_id_id_key`, `hint_pkey`, `class_member_pkey`, `assignment_progress_pkey`
- **Phân trang / giới hạn:** Không (1 hàng)
- **Độ phức tạp:** O(log N) × 5
- **Ghi chú:** Phần đọc của `geo.fn_get_hint` cho bài lớp: bài cho phép gợi ý, em thuộc lớp, mức ≤ mức đã xem + 1 (D-GEO-108). Hàm ghi sự kiện `hint` + tiến độ trong cùng giao dịch, lũy đẳng theo `request_id` (đo đủ ở Q05c); đây là đường DUY NHẤT nâng `hint_level_max` (RV-10 (4)). Bài tự học: Q05b.
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.009 ms, lập kế hoạch 0.088 ms, trang bộ đệm 14, dòng đọc ở các nút quét 4, dòng trả về 1.

```sql
-- lint-allow: R-Q04 (tra khóa chính (org, version, level): 1 hàng)
-- Phần ĐỌC của geo.fn_get_hint cho bài lớp (BR-04): bài cho phép gợi ý, em thuộc lớp, mức <= mức đã xem + 1; gợi ý của đề kho được giao.
-- Hàm ghi thêm sự kiện 'hint' + tiến độ trong cùng giao dịch (đo end-to-end ở Q05c).
SELECT h.level, h.body
FROM geo.assignment a
JOIN geo.hint h ON h.org_id = a.content_org_id AND h.problem_version_id = a.problem_version_id AND h.level = 2
WHERE a.org_id = :'org' AND a.id = :'assignment' AND a.kind = 'class' AND a.allow_hint
  AND EXISTS (SELECT 1 FROM geo.class_member cm
              WHERE cm.org_id = a.org_id AND cm.class_id = a.class_id AND cm.user_id = :'student' AND cm.left_at IS NULL)
  AND 2 <= 1 + COALESCE((SELECT ap.hint_level_max FROM geo.assignment_progress ap
                         WHERE ap.org_id = a.org_id AND ap.assignment_id = a.id AND ap.user_id = :'student'), 0);
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Nested Loop  (cost=0.98..9.21 rows=1 width=184) (actual time=0.004..0.004 rows=1 loops=1)
   Join Filter: (a.class_id = cm.class_id)
   Buffers: shared hit=14
   ->  Nested Loop  (cost=0.57..7.67 rows=1 width=216) (actual time=0.003..0.003 rows=1 loops=1)
         Buffers: shared hit=10
         ->  Index Scan using assignment_org_id_id_key on assignment a  (cost=0.29..5.16 rows=1 width=64) (actual time=0.002..0.002 rows=1 loops=1)
               Index Cond: ((org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid) AND (id = '01a1b2ee-51df-79ad-8354-128825f23080'::uuid))
               Filter: (allow_hint AND (kind = 'class'::geo.assignment_kind) AND (2 <= (1 + COALESCE(((SubPlan 1))::integer, 0))))
               Buffers: shared hit=7
               SubPlan 1
                 ->  Index Scan using assignment_progress_pkey on assignment_progress ap  (cost=0.42..2.64 rows=1 width=2) (actual time=0.001..0.001 rows=1 loops=1)
                       Index Cond: ((org_id = a.org_id) AND (assignment_id = a.id) AND (user_id = '019e976e-3137-7f97-802d-4fe0a56bf727'::uuid))
                       Buffers: shared hit=4
         ->  Index Scan using hint_pkey on hint h  (cost=0.29..2.51 rows=1 width=216) (actual time=0.001..0.001 rows=1 loops=1)
               Index Cond: ((org_id = a.content_org_id) AND (problem_version_id = a.problem_version_id) AND (level = 2))
               Buffers: shared hit=3
   ->  Index Only Scan using class_member_user_idx on class_member cm  (cost=0.41..1.53 rows=1 width=32) (actual time=0.001..0.001 rows=1 loops=1)
         Index Cond: ((org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid) AND (user_id = '019e976e-3137-7f97-802d-4fe0a56bf727'::uuid))
         Heap Fetches: 0
         Buffers: shared hit=4
 Planning Time: 0.088 ms
 Execution Time: 0.009 ms
```
</details>

### Q05b — Gợi ý của bài tự học: đề kho nếu kế thừa khóa, đề tự nhập nếu không (RV-15, D-GEO-155)
- **Ai gọi:** Học sinh, tab Gợi ý của bài tự học (qua `fn_get_hint`)
- **Tần suất ước tính:** ≈ 2/giây
- **Chỉ mục phục vụ:** `assignment_self_idx`, `assignment_org_id_id_key`, `hint_pkey` + Q16 bên trong `fn_self_lock_source`
- **Phân trang / giới hạn:** Không (1 hàng)
- **Độ phức tạp:** O(log N) × 3 + Q16
- **Ghi chú:** Nhánh tự học còn thiếu ở catalogue trước (RV-15). Bài kế thừa khóa nhận gợi ý của đề kho do Giáo viên mẫu soạn, không phải gợi ý AI (D-GEO-155); kiểm kế thừa ở mỗi lần xin (D-GEO-151). Ca đo: bài tự học đang kế thừa khóa.
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.023 ms, lập kế hoạch 0.042 ms, trang bộ đệm 37, dòng đọc ở các nút quét 3, dòng trả về 1.

```sql
-- lint-allow: R-Q04 (tra khóa chính: 1 hàng)
-- Phần ĐỌC của geo.fn_get_hint cho bài tự học (RV-15, D-GEO-151, 155): nếu bài tự học khớp một bài lớp đang khóa (Q16) thì
-- trả gợi ý của ĐỀ KHO của bài lớp đó, ngược lại gợi ý của đề tự nhập. Ca đo: bài tự học đang kế thừa khóa.
SELECT h.level, h.body, CASE WHEN l.id IS NULL THEN 'self' ELSE 'catalog_inherited' END AS hint_source
FROM geo.assignment a
LEFT JOIN geo.assignment l ON l.org_id = a.org_id AND l.id = geo.fn_self_lock_source(a.org_id, a.owner_user_id, a.problem_version_id)
JOIN geo.hint h ON h.org_id = COALESCE(l.content_org_id, a.content_org_id)
               AND h.problem_version_id = COALESCE(l.problem_version_id, a.problem_version_id) AND h.level = 1
WHERE a.org_id = :'inh_org' AND a.id = :'inh_assignment' AND a.kind = 'self' AND a.owner_user_id = :'inh_student';
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Nested Loop  (cost=1.10..7.78 rows=1 width=216) (actual time=0.019..0.020 rows=1 loops=1)
   Buffers: shared hit=37
   ->  Nested Loop Left Join  (cost=0.82..5.27 rows=1 width=80) (actual time=0.018..0.018 rows=1 loops=1)
         Buffers: shared hit=34
         ->  Index Scan using assignment_self_idx on assignment a  (cost=0.28..2.50 rows=1 width=64) (actual time=0.001..0.001 rows=1 loops=1)
               Index Cond: ((org_id = '019dfd12-fe8a-7bbe-a7c0-a4cce1d9db38'::uuid) AND (owner_user_id = '019e981c-0537-71ac-aa2a-4cbb0981d745'::uuid) AND (id = '01a0d429-a6c7-7df5-bc5f-9addc08c6884'::uuid))
               Buffers: shared hit=3
         ->  Index Scan using assignment_org_id_id_key on assignment l  (cost=0.54..2.75 rows=1 width=64) (actual time=0.001..0.001 rows=1 loops=1)
               Index Cond: ((org_id = '019dfd12-fe8a-7bbe-a7c0-a4cce1d9db38'::uuid) AND (id = fn_self_lock_source(a.org_id, a.owner_user_id, a.problem_version_id)))
               Buffers: shared hit=3
   ->  Index Scan using hint_pkey on hint h  (cost=0.29..2.51 rows=1 width=216) (actual time=0.001..0.001 rows=1 loops=1)
         Index Cond: ((org_id = COALESCE(l.content_org_id, a.content_org_id)) AND (problem_version_id = COALESCE(l.problem_version_id, a.problem_version_id)) AND (level = 1))
         Buffers: shared hit=3
 Planning Time: 0.042 ms
 Execution Time: 0.023 ms
```
</details>

### Q05c — Gọi `geo.fn_get_hint` đầy đủ (đọc + ghi bền + tiến độ)
- **Ai gọi:** Học sinh, tab Gợi ý
- **Tần suất ước tính:** ≈ 27/giây (8%)
- **Chỉ mục phục vụ:** như Q05 + `durable_request_pkey`, `event_log_*`, `assignment_progress_pkey`
- **Phân trang / giới hạn:** Không (1 hàng)
- **Độ phức tạp:** O(log N) × 8
- **Ghi chú:** Một lần gọi, một giao dịch (mức B, commit đồng bộ): khóa lũy đẳng `request_id`, sự kiện `hint`, upsert tiến độ. Kế hoạch chỉ hiện Function Scan; thời gian là toàn bộ thân hàm.
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.047 ms, lập kế hoạch 0.002 ms, trang bộ đệm 30, dòng đọc ở các nút quét 0, dòng trả về 1.

```sql
-- lint-allow: R-Q04 (hàm trả 1 hàng)
-- Gọi hàm đầy đủ như ứng dụng: geo.fn_get_hint đọc gợi ý mức 1 VÀ ghi sự kiện 'hint' + khóa lũy đẳng + tiến độ (một giao dịch, mức B).
SELECT status, level, hint_source FROM geo.fn_get_hint(:'org', :'student', :'assignment', 1::smallint, gen_random_uuid());
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Function Scan on fn_get_hint  (cost=0.25..10.25 rows=1000 width=66) (actual time=0.046..0.046 rows=1 loops=1)
   Buffers: shared hit=30
 Planning Time: 0.002 ms
 Execution Time: 0.047 ms
```
</details>

### Q06 — Lấy lời giải có kiểm soát giờ mở (BR-01, ANS-02): thân của `geo.fn_get_solution`
- **Ai gọi:** Học sinh, tab Lời giải (qua hàm); đo thật bằng pgbench
- **Tần suất ước tính:** ≈ 17/giây thường xuyên; đỉnh giờ mở +100/giây (M-GEO-12)
- **Chỉ mục phục vụ:** `assignment_org_id_id_key`, `class_member_pkey`, `solution_pkey`
- **Phân trang / giới hạn:** Không (1 hàng)
- **Độ phức tạp:** O(log N) × 4
- **Ghi chú:** Chạy với vai chủ vì `geo_app` KHÔNG đọc được `solution`. Ứng dụng gọi `SELECT status, ... FROM geo.fn_get_solution(:org,:user,:assignment)` (Q06b, Q06c): luôn 1 hàng, `status` ∈ OK / LOCKED (403 FORBIDDEN) / NOT_FOUND (404) / NO_SOLUTION (RV-21). Đề của bài lớp luôn là đề kho (`content_org_id` = nền tảng).
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.007 ms, lập kế hoạch 0.078 ms, trang bộ đệm 10, dòng đọc ở các nút quét 3, dòng trả về 1.

```sql
-- lint-allow: R-Q04 (tra khóa chính: 1 hàng)
-- lint-allow: R-Q14 (thân của hàm SECURITY DEFINER duy nhất được đọc bảng solution)
-- Nhánh bài lớp của geo.fn_solution_gate + geo.fn_get_solution, viết thành một câu để xem kế hoạch (chạy với vai chủ vì geo_app
-- KHÔNG đọc được solution). Đề của bài lớp luôn là đề kho: content_org_id = platform (CHECK assignment_content_ck).
SELECT s.problem_version_id, s.final_answer, s.steps, s.schema_version, s.ai_authored, a.release_rev
FROM geo.assignment a
JOIN geo.solution s ON s.org_id = a.content_org_id AND s.problem_version_id = a.problem_version_id
WHERE a.org_id = :'org' AND a.id = :'assignment'
  AND a.kind = 'class'
  AND EXISTS (SELECT 1 FROM geo.class_member cm
              WHERE cm.org_id = a.org_id AND cm.class_id = a.class_id AND cm.user_id = :'student' AND cm.left_at IS NULL)
  AND a.release_at IS NOT NULL AND a.release_at <= now();
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Nested Loop  (cost=0.98..6.56 rows=1 width=290) (actual time=0.003..0.003 rows=1 loops=1)
   Join Filter: (a.class_id = cm.class_id)
   Buffers: shared hit=10
   ->  Nested Loop  (cost=0.57..5.02 rows=1 width=322) (actual time=0.002..0.002 rows=1 loops=1)
         Buffers: shared hit=6
         ->  Index Scan using assignment_org_id_id_key on assignment a  (cost=0.29..2.51 rows=1 width=68) (actual time=0.001..0.001 rows=1 loops=1)
               Index Cond: ((org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid) AND (id = '01a1b2ee-51df-79ad-8354-128825f23080'::uuid))
               Filter: ((release_at IS NOT NULL) AND (kind = 'class'::assignment_kind) AND (release_at <= now()))
               Buffers: shared hit=3
         ->  Index Scan using solution_pkey on solution s  (cost=0.28..2.50 rows=1 width=302) (actual time=0.001..0.001 rows=1 loops=1)
               Index Cond: ((org_id = a.content_org_id) AND (problem_version_id = a.problem_version_id))
               Buffers: shared hit=3
   ->  Index Only Scan using class_member_user_idx on class_member cm  (cost=0.41..1.53 rows=1 width=32) (actual time=0.001..0.001 rows=1 loops=1)
         Index Cond: ((org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid) AND (user_id = '019e976e-3137-7f97-802d-4fe0a56bf727'::uuid))
         Heap Fetches: 0
         Buffers: shared hit=4
 Planning Time: 0.078 ms
 Execution Time: 0.007 ms
```
</details>

### Q06b — Gọi `geo.fn_get_solution` cho bài lớp đã mở (đường nóng nhất ở đỉnh giờ mở)
- **Ai gọi:** Học sinh, tab Lời giải
- **Tần suất ước tính:** ≈ 17/giây; đỉnh +100/giây
- **Chỉ mục phục vụ:** như Q06
- **Phân trang / giới hạn:** Không (1 hàng)
- **Độ phức tạp:** O(log N) × 4
- **Ghi chú:** plpgsql: `fn_solution_gate` (tra bài, EXISTS tư cách lớp, giờ mở) rồi đọc `solution`. Thời gian gồm cả gọi hàm lồng; pgbench riêng ở `07_verification.md` §4.6.
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.015 ms, lập kế hoạch 0.002 ms, trang bộ đệm 10, dòng đọc ở các nút quét 0, dòng trả về 1.

```sql
-- lint-allow: R-Q04 (hàm trả đúng 1 hàng)
-- Gọi hàm như ứng dụng (đường nóng nhất ở đỉnh giờ mở): geo.fn_get_solution cho bài lớp đã mở. Trả status + nội dung.
SELECT status, problem_version_id, release_rev FROM geo.fn_get_solution(:'org', :'student', :'assignment');
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Function Scan on fn_get_solution  (cost=0.25..10.25 rows=1000 width=52) (actual time=0.014..0.014 rows=1 loops=1)
   Buffers: shared hit=10
 Planning Time: 0.002 ms
 Execution Time: 0.015 ms
```
</details>

### Q06c — Gọi `geo.fn_get_solution` cho bài tự học kế thừa khóa (đã xem Gợi ý 2)
- **Ai gọi:** Học sinh, tab Lời giải của bài tự học
- **Tần suất ước tính:** ≈ 1/giây
- **Chỉ mục phục vụ:** như Q06 + `problem_signature_pkey`, `assignment_progress_pkey` + Q16
- **Phân trang / giới hạn:** Không (1 hàng)
- **Độ phức tạp:** O(log N) × 6 + Q16
- **Ghi chú:** Chạy đủ nhánh tự học: chữ ký có, Gợi ý 2 đã xem, Q16 tìm thấy bài lớp đang khóa ⇒ `LOCKED` (D-GEO-150, 151).
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.031 ms, lập kế hoạch 0.001 ms, trang bộ đệm 38, dòng đọc ở các nút quét 0, dòng trả về 1.

```sql
-- lint-allow: R-Q04 (hàm trả đúng 1 hàng)
-- Gọi hàm cho bài tự học đang kế thừa khóa (đã xem Gợi ý 2): cổng chạy đủ nhánh tự học gồm Q16 => status LOCKED (D-GEO-151).
SELECT status, problem_version_id FROM geo.fn_get_solution(:'inh_org', :'inh_student', :'inh_assignment');
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Function Scan on fn_get_solution  (cost=0.25..10.25 rows=1000 width=48) (actual time=0.029..0.029 rows=1 loops=1)
   Buffers: shared hit=38
 Planning Time: 0.001 ms
 Execution Time: 0.031 ms
```
</details>

### Q07 — Ghi sự kiện hàng loạt (PRF-01): lô 50 sự kiện của một học sinh
- **Ai gọi:** Học sinh (thiết bị gom, gửi ≤ 10 giây/lô)
- **Tần suất ước tính:** ≈ 117 lô/giây (35%), tức ≈ 3 lô/giây/100 phiên
- **Chỉ mục phục vụ:** `event_log_*_org_id_assignment_id_user_id_occurred_at_id_idx` (một chỉ mục btree mỗi partition)
- **Phân trang / giới hạn:** Không (ghi)
- **Độ phức tạp:** O(m·log N) với m = số dòng trong lô; một câu lệnh, một vòng mạng
- **Ghi chú:** Câu INSERT bên trong `geo.fn_record_batch` (vai chủ: `geo_app` không ghi thẳng `event_log`, D-GEO-158). Có EXISTS tư cách lớp không tương quan, tính một lần (RV-26): bài của lớp khác ⇒ 0 hàng, hàm trả NOT_FOUND. Chỉ mức A (`tool`, `scene_shown`). Commit không đồng bộ (ADR-GEO-11). Số chỉ mục trên bảng ghi nhiều: **1**. Gọi cả hàm: Q07b.
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.092 ms, lập kế hoạch 0.058 ms, trang bộ đệm 259, dòng đọc ở các nút quét 2, dòng trả về 0.

```sql
-- Câu INSERT bên trong geo.fn_record_batch: lô 50 sự kiện mức A của MỘT học sinh, một câu lệnh, CÓ kiểm tư cách lớp (RV-26) bằng
-- EXISTS không tương quan (tính một lần). Thời điểm thiết bị gửi bị kẹp vào [now() - 24 giờ, now()] (ADR-GEO-06).
-- geo_app không INSERT event_log trực tiếp (D-GEO-158): chạy với vai chủ như thân hàm SECURITY DEFINER.
INSERT INTO geo.event_log (occurred_at, org_id, assignment_id, user_id, kind, arg)
SELECT LEAST(GREATEST(e.t, now() - interval '24 hours'), now()), :'org', :'assignment', :'student', e.k, e.a
FROM unnest(ARRAY(SELECT now() - g * interval '200 milliseconds' FROM generate_series(1, 50) g),
            ARRAY(SELECT 'tool'::geo.event_kind FROM generate_series(1, 50) g),
            ARRAY(SELECT (1 + g % 7)::smallint FROM generate_series(1, 50) g)) AS e(t, k, a)
WHERE EXISTS (SELECT 1 FROM geo.assignment x WHERE x.org_id = :'org' AND x.id = :'assignment'
                AND ((x.kind = 'class' AND EXISTS (SELECT 1 FROM geo.class_member cm WHERE cm.org_id = x.org_id AND cm.class_id = x.class_id
                                                    AND cm.user_id = :'student' AND cm.left_at IS NULL))
                  OR (x.kind = 'self' AND x.owner_user_id = :'student')));
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Insert on event_log  (cost=6.44..6.69 rows=0 width=0) (actual time=0.084..0.085 rows=0 loops=1)
   Buffers: shared hit=259 dirtied=1 written=1
   I/O Timings: shared write=0.003
   InitPlan 3
     ->  Index Scan using assignment_org_id_id_key on assignment x  (cost=0.29..4.05 rows=1 width=0) (actual time=0.002..0.002 rows=1 loops=1)
           Index Cond: ((org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid) AND (id = '01a1b2ee-51df-79ad-8354-128825f23080'::uuid))
           Filter: (((kind = 'class'::assignment_kind) AND EXISTS(SubPlan 1)) OR ((kind = 'self'::assignment_kind) AND (owner_user_id = '019e976e-3137-7f97-802d-4fe0a56bf727'::uuid)))
           Buffers: shared hit=7
           SubPlan 1
             ->  Index Only Scan using class_member_user_idx on class_member cm  (cost=0.41..1.54 rows=1 width=0) (actual time=0.001..0.001 rows=1 loops=1)
                   Index Cond: ((org_id = x.org_id) AND (user_id = '019e976e-3137-7f97-802d-4fe0a56bf727'::uuid) AND (class_id = x.class_id))
                   Heap Fetches: 0
                   Buffers: shared hit=4
   InitPlan 4
     ->  Function Scan on generate_series g  (cost=0.00..1.00 rows=50 width=8) (actual time=0.001..0.004 rows=50 loops=1)
   InitPlan 5
     ->  Function Scan on generate_series g_1  (cost=0.00..0.50 rows=50 width=4) (actual time=0.001..0.002 rows=50 loops=1)
   InitPlan 6
     ->  Function Scan on generate_series g_2  (cost=0.00..0.88 rows=50 width=2) (actual time=0.001..0.002 rows=50 loops=1)
   ->  Result  (cost=0.01..0.26 rows=10 width=70) (actual time=0.019..0.027 rows=50 loops=1)
         One-Time Filter: (InitPlan 3).col1
         Buffers: shared hit=57
         ->  Function Scan on e  (cost=0.01..0.11 rows=10 width=14) (actual time=0.016..0.018 rows=50 loops=1)
 Planning Time: 0.058 ms
 Execution Time: 0.092 ms
```
</details>

### Q07b — Gọi `geo.fn_record_batch` đầy đủ: lô 50 sự kiện + tiến độ
- **Ai gọi:** Học sinh (thiết bị gom, ≤ 10 giây/lô)
- **Tần suất ước tính:** ≈ 117 lô/giây (35%)
- **Chỉ mục phục vụ:** như Q07 + `assignment_progress_pkey`
- **Phân trang / giới hạn:** Không (ghi)
- **Độ phức tạp:** O(m·log N)
- **Ghi chú:** Truy vấn chậm nhất của catalogue (≈ 0,14 ms), vẫn ≪ 10 ms (NFR-GEO-007). Ứng dụng đặt `SET LOCAL synchronous_commit = off` trước khi gọi.
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.138 ms, lập kế hoạch 0.007 ms, trang bộ đệm 266, dòng đọc ở các nút quét 0, dòng trả về 1.

```sql
-- lint-allow: R-Q04 (hàm trả 1 hàng)
-- Gọi hàm như ứng dụng: geo.fn_record_batch (Q07 + Q08 trong một lần gọi) với lô 50 sự kiện 'tool'.
SELECT status, inserted FROM geo.fn_record_batch(:'org', :'student', :'assignment',
       ARRAY(SELECT now() - g * interval '200 milliseconds' FROM generate_series(1, 50) g),
       ARRAY(SELECT 'tool'::geo.event_kind FROM generate_series(1, 50) g),
       ARRAY(SELECT (1 + g % 7)::smallint FROM generate_series(1, 50) g));
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Function Scan on fn_record_batch  (cost=2.63..12.63 rows=1000 width=36) (actual time=0.135..0.135 rows=1 loops=1)
   Buffers: shared hit=266 dirtied=1 written=1
   I/O Timings: shared write=0.005
   InitPlan 1
     ->  Function Scan on generate_series g  (cost=0.00..1.00 rows=50 width=8) (actual time=0.001..0.004 rows=50 loops=1)
   InitPlan 2
     ->  Function Scan on generate_series g_1  (cost=0.00..0.50 rows=50 width=4) (actual time=0.001..0.002 rows=50 loops=1)
   InitPlan 3
     ->  Function Scan on generate_series g_2  (cost=0.00..0.88 rows=50 width=2) (actual time=0.001..0.002 rows=50 loops=1)
 Planning Time: 0.007 ms
 Execution Time: 0.138 ms
```
</details>

### Q08 — Cập nhật tiến độ (bộ đếm phi chuẩn hóa) sau mỗi lô
- **Ai gọi:** Mọi hàm ghi sự kiện (một chỗ duy nhất: `geo._progress_apply`)
- **Tần suất ước tính:** ≈ 117/giây lô + ≈ 77/giây sự kiện bền
- **Chỉ mục phục vụ:** `assignment_progress_pkey`
- **Phân trang / giới hạn:** Không (ghi)
- **Độ phức tạp:** O(log N), HOT update (không cột nào của bảng nằm trong chỉ mục ngoài khóa chính)
- **Ghi chú:** UPSERT, lũy đẳng với `hint_level_max` (GREATEST) và `solution_viewed_at` (COALESCE); `hint_view_count`, `ask_count` lũy đẳng nhờ `request_id` ở hàm gọi (RV-18). `active_seconds` cộng độ dài lô + khoảng nghỉ tới hoạt động trước nếu < 30 phút; ≥ 30 phút 0 giây là phiên mới (D-GEO-144, RV-17). Mỗi hàng chỉ một học sinh ghi nên không tranh chấp khóa hàng.
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.013 ms, lập kế hoạch 0.005 ms, trang bộ đệm 7, dòng đọc ở các nút quét 0, dòng trả về 0.

```sql
-- Thân của geo._progress_apply (gọi từ mọi hàm ghi sự kiện): bộ đếm lũy đẳng với mức gợi ý (GREATEST) và giờ xem lời giải (COALESCE);
-- thời gian làm bài cộng độ dài lô + khoảng nghỉ tới hoạt động trước nếu < 30 phút (khoảng nghỉ >= 30 phút 0 giây mở phiên mới:
-- D-GEO-144, RV-17; trước đây BETWEEN tính cả đầu mút 30:00). Tham số: đầu lô = now() - 10 s, cuối lô = now().
INSERT INTO geo.assignment_progress AS ap
       (org_id, assignment_id, user_id, first_opened_at, last_active_at, hint_level_max, hint_view_count, ask_count, tool_count, active_seconds, solution_viewed_at)
VALUES (:'org', :'assignment', :'student', NULL, now(), 0, 0, 0, 50, 10, NULL)
ON CONFLICT (org_id, assignment_id, user_id) DO UPDATE SET
       first_opened_at    = COALESCE(ap.first_opened_at, EXCLUDED.first_opened_at),
       last_active_at     = GREATEST(ap.last_active_at, EXCLUDED.last_active_at),
       hint_level_max     = GREATEST(ap.hint_level_max, EXCLUDED.hint_level_max),
       hint_view_count    = ap.hint_view_count + EXCLUDED.hint_view_count,
       ask_count          = ap.ask_count + EXCLUDED.ask_count,
       tool_count         = ap.tool_count + EXCLUDED.tool_count,
       active_seconds     = ap.active_seconds + EXCLUDED.active_seconds
                            + CASE WHEN (now() - interval '10 seconds') - ap.last_active_at >= interval '0'
                                    AND (now() - interval '10 seconds') - ap.last_active_at <  interval '30 minutes'
                                   THEN extract(epoch FROM (now() - interval '10 seconds') - ap.last_active_at)::int ELSE 0 END,
       solution_viewed_at = COALESCE(ap.solution_viewed_at, EXCLUDED.solution_viewed_at),
       updated_at         = now();
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Insert on assignment_progress ap  (cost=0.00..0.01 rows=0 width=0) (actual time=0.010..0.010 rows=0 loops=1)
   Conflict Resolution: UPDATE
   Conflict Arbiter Indexes: assignment_progress_pkey
   Tuples Inserted: 0
   Conflicting Tuples: 1
   Buffers: shared hit=7
   ->  Result  (cost=0.00..0.01 rows=1 width=98) (actual time=0.000..0.000 rows=1 loops=1)
 Planning Time: 0.005 ms
 Execution Time: 0.013 ms
```
</details>

### Q08b — Gọi `geo.fn_record_durable` cho sự kiện bền `unlock` (sau khi xem lời giải)
- **Ai gọi:** Học sinh
- **Tần suất ước tính:** ≈ 17/giây; đỉnh +100/giây (cùng `open` ≈ 33/giây)
- **Chỉ mục phục vụ:** `fn_solution_gate` + `durable_request_pkey` + `event_log_*` + `assignment_progress_pkey`
- **Phân trang / giới hạn:** Không (ghi)
- **Độ phức tạp:** O(log N) × 6
- **Ghi chú:** `unlock` chỉ ghi khi cổng OK (không thể có "đã xem lời giải" khi bài còn khóa, ca G4); lũy đẳng theo `request_id` (ca G5, RV-18); `open` là mức B (D-GEO-162).
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.044 ms, lập kế hoạch 0.001 ms, trang bộ đệm 23, dòng đọc ở các nút quét 0, dòng trả về 1.

```sql
-- lint-allow: R-Q04 (hàm trả 1 giá trị)
-- Gọi hàm như ứng dụng: sự kiện bền 'unlock' sau khi xem lời giải (cổng + khóa lũy đẳng request_id + sự kiện + tiến độ, D-GEO-162, RV-18).
SELECT geo.fn_record_durable(:'org', :'student', :'assignment', gen_random_uuid(), 'unlock');
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Result  (cost=0.00..0.26 rows=1 width=32) (actual time=0.044..0.044 rows=1 loops=1)
   Buffers: shared hit=23
 Planning Time: 0.001 ms
 Execution Time: 0.044 ms
```
</details>

### Q09 — Trang theo dõi lớp: ai đã mở, mức gợi ý, số câu hỏi, thời gian (LRN-03)
- **Ai gọi:** Giáo viên, màn Theo dõi lớp (làm mới mỗi ~10 giây khi đang mở)
- **Tần suất ước tính:** ≈ 3/giây (một phần của 3%; 30 giáo viên đang mở làm mới mỗi 10 giây)
- **Chỉ mục phục vụ:** `assignment_org_id_id_key`, `class_member_name_uq` (INCLUDE user_id), `app_user_pkey`, `assignment_progress_pkey`, `class_pkey`
- **Phân trang / giới hạn:** Cursor = `(name_key, user_id)` (tên A → Z, D-GEO-121, 167); trang 100 (LIMIT 101), trần 100; không giới hạn cỡ lớp
- **Độ phức tạp:** O(101·log N); KHÔNG đọc `event_log`
- **Ghi chú:** Không gom hàng triệu sự kiện: đọc bộ đếm tiến độ (NFR-GEO-047). Thứ tự theo tên chuẩn hóa (không theo kết quả, PO-04); phân trang theo khóa nên thứ tự đúng qua mọi trang (RV-36).
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.109 ms, lập kế hoạch 0.212 ms, trang bộ đệm 575, dòng đọc ở các nút quét 257, dòng trả về 64.

```sql
-- Trang theo dõi lớp: danh sách em + tiến độ, sắp theo tên A → Z (D-GEO-121), phân trang theo khóa (name_key, user_id) (D-GEO-167, RV-36),
-- trang 100 (LIMIT 101), không giới hạn cỡ lớp. Không đụng event_log (NFR-GEO-047). Trang đầu: cursor = ('', 0000...).
SELECT u.id AS user_id, u.display_name, cm.name_key,
       ap.first_opened_at, ap.last_active_at, ap.hint_level_max, ap.hint_view_count, ap.ask_count, ap.tool_count, ap.active_seconds, ap.solution_viewed_at
FROM geo.assignment a
JOIN geo.class c ON c.org_id = a.org_id AND c.id = a.class_id AND c.teacher_id = :'teacher'
JOIN geo.class_member cm ON cm.org_id = a.org_id AND cm.class_id = a.class_id AND cm.left_at IS NULL
                        AND (cm.name_key, cm.user_id) > ('', '00000000-0000-0000-0000-000000000000'::uuid)
JOIN geo.app_user u ON u.org_id = cm.org_id AND u.id = cm.user_id
LEFT JOIN geo.assignment_progress ap ON ap.org_id = a.org_id AND ap.assignment_id = a.id AND ap.user_id = cm.user_id
WHERE a.org_id = :'org' AND a.id = :'assignment'
ORDER BY cm.name_key, cm.user_id
LIMIT 101;
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Limit  (cost=8.87..8.95 rows=34 width=104) (actual time=0.095..0.099 rows=64 loops=1)
   Buffers: shared hit=575
   ->  Sort  (cost=8.87..8.95 rows=34 width=104) (actual time=0.095..0.097 rows=64 loops=1)
         Sort Key: cm.name_key, u.id
         Sort Method: quicksort  Memory: 33kB
         Buffers: shared hit=575
         ->  Nested Loop  (cost=1.68..8.01 rows=34 width=104) (actual time=0.006..0.088 rows=64 loops=1)
               Join Filter: (c.id = a.class_id)
               Buffers: shared hit=575
               ->  Nested Loop Left Join  (cost=1.41..7.67 rows=1 width=152) (actual time=0.005..0.067 rows=64 loops=1)
                     Buffers: shared hit=446
                     ->  Nested Loop  (cost=0.98..5.18 rows=1 width=126) (actual time=0.004..0.032 rows=64 loops=1)
                           Buffers: shared hit=200
                           ->  Nested Loop  (cost=0.70..4.05 rows=1 width=110) (actual time=0.003..0.008 rows=64 loops=1)
                                 Buffers: shared hit=8
                                 ->  Index Scan using assignment_org_id_id_key on assignment a  (cost=0.29..2.50 rows=1 width=48) (actual time=0.001..0.001 rows=1 loops=1)
                                       Index Cond: ((org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid) AND (id = '01a1b2ee-51df-79ad-8354-128825f23080'::uuid))
                                       Buffers: shared hit=3
                                 ->  Index Only Scan using class_member_name_uq on class_member cm  (cost=0.41..1.54 rows=1 width=62) (actual time=0.002..0.005 rows=64 loops=1)
                                       Index Cond: ((org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid) AND (class_id = a.class_id) AND (name_key >= ''::text))
                                       Filter: (ROW(name_key, user_id) > ROW(''::text, '00000000-0000-0000-0000-000000000000'::uuid))
                                       Heap Fetches: 0
                                       Buffers: shared hit=5
                           ->  Index Scan using app_user_pkey on app_user u  (cost=0.29..1.12 rows=1 width=48) (actual time=0.000..0.000 rows=1 loops=64)
                                 Index Cond: (id = cm.user_id)
                                 Filter: (org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid)
                                 Buffers: shared hit=192
                     ->  Index Scan using assignment_progress_pkey on assignment_progress ap  (cost=0.42..2.49 rows=1 width=90) (actual time=0.000..0.000 rows=1 loops=64)
                           Index Cond: ((org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid) AND (assignment_id = '01a1b2ee-51df-79ad-8354-128825f23080'::uuid) AND (user_id = cm.user_id))
                           Buffers: shared hit=246
               ->  Index Only Scan using class_teacher_idx on class c  (cost=0.28..0.32 rows=1 width=32) (actual time=0.000..0.000 rows=1 loops=64)
                     Index Cond: ((org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid) AND (teacher_id = '019e306e-4994-7421-b184-efe9d1162e64'::uuid) AND (id = cm.class_id))
                     Heap Fetches: 0
                     Buffers: shared hit=129
 Planning:
   Buffers: shared hit=18
 Planning Time: 0.212 ms
 Execution Time: 0.109 ms
```
</details>

### Q09b — Các thẻ tổng hợp của Theo dõi lớp (M-GEO-21, 22, 23)
- **Ai gọi:** Giáo viên, cùng màn Q09
- **Tần suất ước tính:** ≈ 3/giây (cùng lúc Q09)
- **Chỉ mục phục vụ:** `assignment_progress_pkey` (quét khoảng theo bài), `class_teacher_idx`
- **Phân trang / giới hạn:** Không (1 hàng gộp)
- **Độ phức tạp:** O(s)
- **Ghi chú:** Gộp trên bộ đếm của MỘT bài, LEFT JOIN nên khi chưa em nào mở vẫn trả 1 hàng với `class_size` (RV-30). `count(*)` có `lint-allow` ghi phạm vi nhỏ. Thời gian trung bình lớp = `active_seconds_total / class_size` (D-GEO-077).
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.024 ms, lập kế hoạch 0.055 ms, trang bộ đệm 23, dòng đọc ở các nút quét 124, dòng trả về 1.

```sql
-- lint-allow: R-Q05 (đếm trong phạm vi MỘT bài và MỘT lớp, tối đa cỡ lớp hàng)
-- Các thẻ tổng hợp của Theo dõi lớp (M-GEO-21, 22, 23): LEFT JOIN bộ đếm tiến độ của MỘT bài (RV-30) nên khi chưa em nào mở
-- vẫn trả 1 hàng với class_size (thẻ "0/5"). Không đụng event_log.
SELECT count(ap.first_opened_at) AS opened,
       count(*) FILTER (WHERE ap.hint_view_count > 0) AS used_hint,
       COALESCE(sum(ap.ask_count), 0) AS asks,
       COALESCE(sum(ap.active_seconds), 0) AS active_seconds_total,
       count(ap.solution_viewed_at) AS viewed_solution,
       (SELECT count(*) FROM geo.class_member cm WHERE cm.org_id = a.org_id AND cm.class_id = a.class_id AND cm.left_at IS NULL) AS class_size
FROM geo.assignment a
JOIN geo.class c ON c.org_id = a.org_id AND c.id = a.class_id AND c.teacher_id = :'teacher'
LEFT JOIN geo.assignment_progress ap ON ap.org_id = a.org_id AND ap.assignment_id = a.id
WHERE a.org_id = :'org' AND a.id = :'assignment'
GROUP BY a.org_id, a.class_id;
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 GroupAggregate  (cost=0.98..8.13 rows=1 width=80) (actual time=0.016..0.017 rows=1 loops=1)
   Group Key: a.class_id
   Buffers: shared hit=23
   ->  Nested Loop Left Join  (cost=0.98..6.56 rows=1 width=60) (actual time=0.003..0.009 rows=54 loops=1)
         Buffers: shared hit=18
         ->  Nested Loop  (cost=0.56..3.91 rows=1 width=48) (actual time=0.002..0.003 rows=1 loops=1)
               Join Filter: (c.id = a.class_id)
               Rows Removed by Join Filter: 2
               Buffers: shared hit=12
               ->  Index Only Scan using class_teacher_idx on class c  (cost=0.28..1.40 rows=1 width=32) (actual time=0.001..0.001 rows=3 loops=1)
                     Index Cond: ((org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid) AND (teacher_id = '019e306e-4994-7421-b184-efe9d1162e64'::uuid))
                     Heap Fetches: 0
                     Buffers: shared hit=3
               ->  Index Scan using assignment_org_id_id_key on assignment a  (cost=0.29..2.50 rows=1 width=48) (actual time=0.000..0.000 rows=1 loops=3)
                     Index Cond: ((org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid) AND (id = '01a1b2ee-51df-79ad-8354-128825f23080'::uuid))
                     Buffers: shared hit=9
         ->  Index Scan using assignment_progress_pkey on assignment_progress ap  (cost=0.42..2.64 rows=1 width=60) (actual time=0.001..0.003 rows=54 loops=1)
               Index Cond: ((org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid) AND (assignment_id = '01a1b2ee-51df-79ad-8354-128825f23080'::uuid))
               Buffers: shared hit=6
   SubPlan 1
     ->  Aggregate  (cost=1.54..1.55 rows=1 width=8) (actual time=0.005..0.005 rows=1 loops=1)
           Buffers: shared hit=5
           ->  Index Only Scan using class_member_name_uq on class_member cm  (cost=0.41..1.53 rows=1 width=0) (actual time=0.001..0.003 rows=64 loops=1)
                 Index Cond: ((org_id = a.org_id) AND (class_id = a.class_id))
                 Heap Fetches: 0
                 Buffers: shared hit=5
 Planning:
   Buffers: shared hit=9
 Planning Time: 0.055 ms
 Execution Time: 0.024 ms
```
</details>

### Q10 — Đổi giờ mở và mở ngay (ANS-01): các câu lệnh bên trong `geo.fn_set_release`
- **Ai gọi:** Giáo viên, nút "Đổi giờ" / "Mở ngay"
- **Tần suất ước tính:** ≈ 0,1/giây thường xuyên; vài lần/giây nếu nhiều giáo viên đặt giờ cùng lúc
- **Chỉ mục phục vụ:** `assignment_org_id_id_key` (khóa một hàng), `class_teacher_idx`, `audit_log_target_idx` (ghi)
- **Phân trang / giới hạn:** Không (ghi)
- **Độ phức tạp:** O(log N), khóa một hàng trong giao dịch ngắn
- **Ghi chú:** Đường duy nhất sửa `release_at`. Câu khóa không lọc theo giáo viên/rev để hàm trả mã phân biệt NOT_FOUND / FORBIDDEN / CONFLICT_ALREADY_OPEN / CONFLICT_REV / VALIDATION_PAST kèm rev hiện hành (RV-21, D-GEO-143); luôn ghi `audit_log`. Giờ mở lúc tạo bài đi qua `fn_create_assignment` (D-GEO-158).
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.007 ms, lập kế hoạch 0.035 ms, trang bộ đệm 9, dòng đọc ở các nút quét 3, dòng trả về 1.

```sql
-- Hai câu lệnh bên trong geo.fn_set_release (khóa một hàng, rồi cập nhật; thêm một INSERT audit_log). Câu khóa không lọc theo giáo viên
-- và release_rev nữa để hàm phân biệt NOT_FOUND / FORBIDDEN / CONFLICT_REV / CONFLICT_ALREADY_OPEN (RV-21).
SELECT a.release_at, a.release_rev, c.teacher_id
FROM geo.assignment a JOIN geo.class c ON c.org_id = a.org_id AND c.id = a.class_id
WHERE a.org_id = :'org' AND a.id = :'assignment' AND a.kind = 'class'
FOR UPDATE OF a;
UPDATE geo.assignment SET release_at = now(), release_rev = release_rev + 1 WHERE org_id = :'org' AND id = :'assignment';
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 LockRows  (cost=0.56..5.35 rows=1 width=40) (actual time=0.003..0.004 rows=1 loops=1)
   Buffers: shared hit=9
   ->  Nested Loop  (cost=0.56..5.34 rows=1 width=40) (actual time=0.003..0.003 rows=1 loops=1)
         Buffers: shared hit=8
         ->  Index Scan using assignment_org_id_id_key on assignment a  (cost=0.29..2.51 rows=1 width=50) (actual time=0.002..0.002 rows=1 loops=1)
               Index Cond: ((org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid) AND (id = '01a1b2ee-51df-79ad-8354-128825f23080'::uuid))
               Filter: (kind = 'class'::assignment_kind)
               Buffers: shared hit=5
         ->  Index Scan using class_org_id_id_key on class c  (cost=0.28..2.49 rows=1 width=54) (actual time=0.001..0.001 rows=1 loops=1)
               Index Cond: ((org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid) AND (id = a.class_id))
               Buffers: shared hit=3
 Planning:
   Buffers: shared hit=6
 Planning Time: 0.035 ms
 Execution Time: 0.007 ms
 Update on assignment  (cost=0.29..2.51 rows=0 width=0) (actual time=0.022..0.022 rows=0 loops=1)
   Buffers: shared hit=20
   ->  Index Scan using assignment_org_id_id_key on assignment  (cost=0.29..2.51 rows=1 width=18) (actual time=0.001..0.001 rows=1 loops=1)
         Index Cond: ((org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid) AND (id = '01a1b2ee-51df-79ad-8354-128825f23080'::uuid))
         Buffers: shared hit=3
 Planning Time: 0.008 ms
 Execution Time: 0.024 ms
```
</details>

### Q11 — Bấm từ khóa mở bài giảng (PO-34)
- **Ai gọi:** Học sinh (gợi ý, lời giải, câu trả lời)
- **Tần suất ước tính:** ≈ 40/giây (12%)
- **Chỉ mục phục vụ:** `keyword_pkey`, `lecture_org_id_id_key`
- **Phân trang / giới hạn:** Không (1 hàng)
- **Độ phức tạp:** O(log N) × 2
- **Ghi chú:** Bài giảng bất biến theo thời gian ngắn → đệm Redis theo khóa `p:` (TTL dài). Cột `human_reviewed = false` hiện "Nội dung đang chờ giáo viên soát" cho cả học sinh và giáo viên (D-GEO-137, 181).
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.004 ms, lập kế hoạch 0.026 ms, trang bộ đệm 6, dòng đọc ở các nút quét 2, dòng trả về 1.

```sql
-- lint-allow: R-Q04 (tra khóa chính (org, term_key): 1 hàng)
-- Bấm từ khóa -> bài giảng (khớp chính xác theo term_key đã chuẩn hóa).
SELECT l.id, l.title, l.body, l.source_kind, l.approval_status, l.source_ref
FROM geo.keyword k
JOIN geo.lecture l ON l.org_id = k.org_id AND l.id = k.lecture_id
WHERE k.org_id = :'platform' AND k.term_key = :'term_key';
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Nested Loop  (cost=0.55..5.00 rows=1 width=256) (actual time=0.002..0.002 rows=1 loops=1)
   Buffers: shared hit=6
   ->  Index Scan using keyword_pkey on keyword k  (cost=0.28..2.49 rows=1 width=32) (actual time=0.001..0.001 rows=1 loops=1)
         Index Cond: ((org_id = '00000000-0000-7000-8000-000000000001'::uuid) AND (term_key = 'duong vuong goc 127'::text))
         Buffers: shared hit=3
   ->  Index Scan using lecture_org_id_id_key on lecture l  (cost=0.27..2.49 rows=1 width=272) (actual time=0.001..0.001 rows=1 loops=1)
         Index Cond: ((org_id = '00000000-0000-7000-8000-000000000001'::uuid) AND (id = k.lecture_id))
         Buffers: shared hit=3
 Planning:
   Buffers: shared hit=7
 Planning Time: 0.026 ms
 Execution Time: 0.004 ms
```
</details>

### Q11d — Từ khóa liên quan trong hộp bài giảng (PO-34)
- **Ai gọi:** Học sinh, mở một bài giảng
- **Tần suất ước tính:** ≈ 40/giây (mỗi lần bấm từ khóa mở thêm một hộp)
- **Chỉ mục phục vụ:** `keyword_lecture_idx`
- **Phân trang / giới hạn:** Tối đa 10 dòng
- **Độ phức tạp:** O(log N + 10)
- **Ghi chú:** Đã sắp theo `term_key` trong chỉ mục nên không có nút Sort.
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.003 ms, lập kế hoạch 0.008 ms, trang bộ đệm 3, dòng đọc ở các nút quét 3, dòng trả về 3.

```sql
-- Từ khóa liên quan hiển thị trong hộp bài giảng (PO-34): tối đa 10, đã sắp theo term_key.
SELECT k.term_key, k.term
FROM geo.keyword k
WHERE k.org_id = :'platform' AND k.lecture_id = :'lecture'
ORDER BY k.term_key
LIMIT 10;
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Limit  (cost=0.28..3.50 rows=3 width=35) (actual time=0.001..0.001 rows=3 loops=1)
   Buffers: shared hit=3
   ->  Index Scan using keyword_lecture_idx on keyword k  (cost=0.28..3.50 rows=3 width=35) (actual time=0.001..0.001 rows=3 loops=1)
         Index Cond: ((org_id = '00000000-0000-7000-8000-000000000001'::uuid) AND (lecture_id = '019f31eb-297b-7775-8312-2f942639c7bc'::uuid))
         Buffers: shared hit=3
 Planning Time: 0.008 ms
 Execution Time: 0.003 ms
```
</details>

### Q12 — Hỏi đáp tìm nguồn (PO-33): chọn tối đa 5 bài giảng làm nguồn cho câu trả lời
- **Ai gọi:** Cổng AI / học sinh, tab Hỏi đáp
- **Tần suất ước tính:** ≈ 40/giây (12%) — phần lớn thời gian một lượt hỏi là gọi AI (không giữ kết nối CSDL)
- **Chỉ mục phục vụ:** `lecture_search_idx`, `lecture_org_id_id_key`
- **Phân trang / giới hạn:** Tối đa 5 dòng; ứng viên giới hạn 200
- **Độ phức tạp:** O(log N + min(m, 200)) nhờ giới hạn ứng viên
- **Ghi chú:** Phiên bản đã tối ưu (v2): xếp hạng trên tập ứng viên ≤ 200 thay vì mọi bài khớp; lịch sử trước/sau ở mục "Lịch sử tối ưu". Khóa lời giải (BR-02) là việc của cổng AI; truy vấn này chỉ đọc bài giảng, không đọc lời giải.
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.084 ms, lập kế hoạch 0.039 ms, trang bộ đệm 67, dòng đọc ở các nút quét 130, dòng trả về 5.

```sql
-- Hỏi đáp tìm nguồn (PO-33): từ câu hỏi lấy tối đa 5 bài giảng làm nguồn. v2: xếp hạng trên TẬP ỨNG VIÊN GIỚI HẠN 200 (không xếp hạng mọi bài khớp),
-- rồi mới lấy đoạn đầu thân bài (600 ký tự) của đúng 5 bài được chọn. Lịch sử tối ưu: queries/history/Q12_v1_rank_all_matches.sql.
WITH q AS (SELECT websearch_to_tsquery('simple', geo.f_unaccent(lower(:'q'))) AS tsq),
cand AS MATERIALIZED (
    SELECT l.id, l.search_tsv
    FROM geo.lecture l, q
    WHERE l.org_id = :'platform' AND l.search_tsv @@ q.tsq
    LIMIT 200),
top5 AS (
    SELECT c.id
    FROM cand c, q
    ORDER BY ts_rank(c.search_tsv, q.tsq) DESC, c.id
    LIMIT 5)
SELECT l.id, l.title, l.source_kind, l.approval_status, l.source_ref, left(l.body, 600) AS excerpt
FROM top5 t
JOIN geo.lecture l ON l.org_id = :'platform' AND l.id = t.id;
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Nested Loop  (cost=6.90..11.66 rows=2 width=120) (actual time=0.071..0.079 rows=5 loops=1)
   Buffers: shared hit=67
   CTE q
     ->  Result  (cost=0.00..0.01 rows=1 width=32) (actual time=0.000..0.000 rows=1 loops=1)
   CTE cand
     ->  Limit  (cost=4.28..6.52 rows=2 width=828) (actual time=0.013..0.031 rows=125 loops=1)
           Buffers: shared hit=52
           ->  Nested Loop  (cost=4.28..6.52 rows=2 width=828) (actual time=0.013..0.027 rows=125 loops=1)
                 Buffers: shared hit=52
                 ->  CTE Scan on q q_1  (cost=0.00..0.02 rows=1 width=32) (actual time=0.000..0.000 rows=1 loops=1)
                 ->  Bitmap Heap Scan on lecture l_1  (cost=4.28..6.48 rows=2 width=828) (actual time=0.013..0.019 rows=125 loops=1)
                       Recheck Cond: ((org_id = '00000000-0000-7000-8000-000000000001'::uuid) AND (search_tsv @@ q_1.tsq))
                       Heap Blocks: exact=43
                       Buffers: shared hit=52
                       ->  Bitmap Index Scan on lecture_search_idx  (cost=0.00..4.28 rows=2 width=0) (actual time=0.011..0.011 rows=125 loops=1)
                             Index Cond: ((org_id = '00000000-0000-7000-8000-000000000001'::uuid) AND (search_tsv @@ q_1.tsq))
                             Buffers: shared hit=9
   ->  Limit  (cost=0.10..0.10 rows=2 width=20) (actual time=0.068..0.069 rows=5 loops=1)
         Buffers: shared hit=52
         ->  Sort  (cost=0.10..0.10 rows=2 width=20) (actual time=0.068..0.068 rows=5 loops=1)
               Sort Key: (ts_rank(c.search_tsv, q.tsq)) DESC, c.id
               Sort Method: top-N heapsort  Memory: 25kB
               Buffers: shared hit=52
               ->  Nested Loop  (cost=0.00..0.09 rows=2 width=20) (actual time=0.014..0.065 rows=125 loops=1)
                     Buffers: shared hit=52
                     ->  CTE Scan on q  (cost=0.00..0.02 rows=1 width=32) (actual time=0.000..0.000 rows=1 loops=1)
                     ->  CTE Scan on cand c  (cost=0.00..0.04 rows=2 width=48) (actual time=0.013..0.043 rows=125 loops=1)
                           Buffers: shared hit=52
   ->  Index Scan using lecture_org_id_id_key on lecture l  (cost=0.27..2.49 rows=1 width=256) (actual time=0.000..0.000 rows=1 loops=5)
         Index Cond: ((org_id = '00000000-0000-7000-8000-000000000001'::uuid) AND (id = c.id))
         Buffers: shared hit=15
 Planning:
   Buffers: shared hit=9
 Planning Time: 0.039 ms
 Execution Time: 0.084 ms
```
</details>

### Q13 — Kho đề có lọc: cấp + loại hình (KNW-01)
- **Ai gọi:** Giáo viên, màn Kho đề
- **Tần suất ước tính:** ≈ 17/giây (5%)
- **Chỉ mục phục vụ:** `problem_catalog_filter_idx` (một phần: kho đề đang dùng)
- **Phân trang / giới hạn:** Cursor = bộ `(level, solid_kind, topic_id, id)` của dòng cuối; trang 20, trần 50
- **Độ phức tạp:** O(log N + 21)
- **Ghi chú:** Bộ lọc tùy chọn (dạng bài) thêm `AND p.topic_id = …` khi có; **không** viết `(:x IS NULL OR cột = :x)` vì phá chỉ mục (R-Q08). Chỉ mục một phần nên 8.000 đề tự nhập không có trong chỉ mục.
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.007 ms, lập kế hoạch 0.031 ms, trang bộ đệm 19, dòng đọc ở các nút quét 10, dòng trả về 5.

```sql
-- Kho đề có lọc cấp + loại hình (dạng bài tùy chọn: thêm 'AND p.topic_id = ...' khi có). Thứ tự duyệt = (level, solid_kind, topic_id, id).
-- Trang đầu cursor = (thcs, pyramid, 0, 0000...). Trang kế: bộ bốn của dòng cuối.
SELECT p.id, p.title, p.level, p.solid_kind, p.topic_id, pv.verify_status, pv.approval_status
FROM geo.problem p
JOIN geo.problem_version pv ON pv.id = p.current_version_id
WHERE p.org_id = :'platform' AND p.source = 'catalog' AND p.archived_at IS NULL
  AND p.level = 'thpt' AND p.solid_kind = 'box'
  AND (p.level, p.solid_kind, p.topic_id, p.id) > ('thpt', 'box', 0, '00000000-0000-0000-0000-000000000000')
ORDER BY p.level, p.solid_kind, p.topic_id, p.id
LIMIT 21;
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Limit  (cost=0.42..4.86 rows=1 width=56) (actual time=0.002..0.004 rows=5 loops=1)
   Buffers: shared hit=19
   ->  Nested Loop  (cost=0.42..4.86 rows=1 width=56) (actual time=0.001..0.004 rows=5 loops=1)
         Buffers: shared hit=19
         ->  Index Scan using problem_catalog_filter_idx on problem p  (cost=0.14..2.36 rows=1 width=64) (actual time=0.001..0.001 rows=5 loops=1)
               Index Cond: ((org_id = '00000000-0000-7000-8000-000000000001'::uuid) AND (ROW(level, solid_kind, topic_id, id) > ROW('thpt'::geo.edu_level, 'box'::geo.solid_kind, 0, '00000000-0000-0000-0000-000000000000'::uuid)) AND (level = 'thpt'::geo.edu_level) AND (solid_kind = 'box'::geo.solid_kind))
               Buffers: shared hit=4
         ->  Index Scan using problem_version_pkey on problem_version pv  (cost=0.28..2.50 rows=1 width=24) (actual time=0.000..0.000 rows=1 loops=5)
               Index Cond: (id = p.current_version_id)
               Buffers: shared hit=15
 Planning:
   Buffers: shared hit=6
 Planning Time: 0.031 ms
 Execution Time: 0.007 ms
```
</details>

### Q13b — Kho đề chỉ lọc theo cấp
- **Ai gọi:** Giáo viên
- **Tần suất ước tính:** trong 5% của Q13
- **Chỉ mục phục vụ:** `problem_catalog_filter_idx` (đầu chỉ mục)
- **Phân trang / giới hạn:** Như Q13
- **Độ phức tạp:** O(log N + 21)
- **Ghi chú:** Thứ tự duyệt = thứ tự chỉ mục nên không cần Sort.
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.013 ms, lập kế hoạch 0.031 ms, trang bộ đệm 52, dòng đọc ở các nút quét 32, dòng trả về 16.

```sql
-- Kho đề chỉ lọc theo cấp (phần đầu của chỉ mục).
SELECT p.id, p.title, p.level, p.solid_kind, p.topic_id, pv.verify_status, pv.approval_status
FROM geo.problem p
JOIN geo.problem_version pv ON pv.id = p.current_version_id
WHERE p.org_id = :'platform' AND p.source = 'catalog' AND p.archived_at IS NULL AND p.level = 'thcs'
  AND (p.level, p.solid_kind, p.topic_id, p.id) > ('thcs', 'cube', 2, '00000000-0000-0000-0000-000000000000')
ORDER BY p.level, p.solid_kind, p.topic_id, p.id
LIMIT 21;
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Limit  (cost=0.42..4.86 rows=1 width=56) (actual time=0.002..0.009 rows=16 loops=1)
   Buffers: shared hit=52
   ->  Nested Loop  (cost=0.42..4.86 rows=1 width=56) (actual time=0.002..0.009 rows=16 loops=1)
         Buffers: shared hit=52
         ->  Index Scan using problem_catalog_filter_idx on problem p  (cost=0.14..2.36 rows=1 width=64) (actual time=0.001..0.002 rows=16 loops=1)
               Index Cond: ((org_id = '00000000-0000-7000-8000-000000000001'::uuid) AND (ROW(level, solid_kind, topic_id, id) > ROW('thcs'::geo.edu_level, 'cube'::geo.solid_kind, 2, '00000000-0000-0000-0000-000000000000'::uuid)) AND (level = 'thcs'::geo.edu_level))
               Buffers: shared hit=4
         ->  Index Scan using problem_version_pkey on problem_version pv  (cost=0.28..2.50 rows=1 width=24) (actual time=0.000..0.000 rows=1 loops=16)
               Index Cond: (id = p.current_version_id)
               Buffers: shared hit=48
 Planning:
   Buffers: shared hit=6
 Planning Time: 0.031 ms
 Execution Time: 0.013 ms
```
</details>

### Q14 — Hỏi gì: các câu hỏi gần nhất của cả lớp trong một bài (LRN-03)
- **Ai gọi:** Giáo viên, màn Theo dõi lớp
- **Tần suất ước tính:** ≈ 3/giây
- **Chỉ mục phục vụ:** `qa_turn_assignment_idx`
- **Phân trang / giới hạn:** Cursor = `qa_turn.id` (mới nhất trước); trang 50 (LIMIT 51), trần 100
- **Độ phức tạp:** O(log N + 51)
- **Ghi chú:** Trả câu hỏi, loại câu trả lời và nguồn đã dùng; KHÔNG trả `reply_text` (chỉ chủ câu hỏi đọc, D-GEO-159; `geo_app` không có SELECT cột đó).
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.047 ms, lập kế hoạch 0.048 ms, trang bộ đệm 250, dòng đọc ở các nút quét 144, dòng trả về 48.

```sql
-- Hỏi gì: các câu hỏi gần nhất của cả lớp trong một bài (giáo viên), keyset theo id, 50 mỗi trang.
SELECT q.id, q.user_id, q.asked_at, q.question, q.reply_kind, q.source_lecture_ids
FROM geo.qa_turn q
JOIN geo.assignment a ON a.org_id = q.org_id AND a.id = q.assignment_id
JOIN geo.class c ON c.org_id = a.org_id AND c.id = a.class_id AND c.teacher_id = :'teacher'
WHERE q.org_id = :'org' AND q.assignment_id = :'assignment' AND q.id < 9223372036854775807
ORDER BY q.id DESC
LIMIT 51;
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Limit  (cost=0.98..6.57 rows=1 width=121) (actual time=0.003..0.042 rows=48 loops=1)
   Buffers: shared hit=250
   ->  Nested Loop  (cost=0.98..6.57 rows=1 width=121) (actual time=0.003..0.040 rows=48 loops=1)
         Join Filter: (c.id = a.class_id)
         Buffers: shared hit=250
         ->  Nested Loop  (cost=0.71..5.16 rows=1 width=153) (actual time=0.002..0.024 rows=48 loops=1)
               Buffers: shared hit=153
               ->  Index Scan using qa_turn_assignment_idx on qa_turn q  (cost=0.42..2.64 rows=1 width=153) (actual time=0.001..0.004 rows=48 loops=1)
                     Index Cond: ((org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid) AND (assignment_id = '01a1b2ee-51df-79ad-8354-128825f23080'::uuid) AND (id < '9223372036854775807'::bigint))
                     Buffers: shared hit=9
               ->  Index Scan using assignment_org_id_id_key on assignment a  (cost=0.29..2.50 rows=1 width=48) (actual time=0.000..0.000 rows=1 loops=48)
                     Index Cond: ((org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid) AND (id = '01a1b2ee-51df-79ad-8354-128825f23080'::uuid))
                     Buffers: shared hit=144
         ->  Index Only Scan using class_teacher_idx on class c  (cost=0.28..1.40 rows=1 width=32) (actual time=0.000..0.000 rows=1 loops=48)
               Index Cond: ((org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid) AND (teacher_id = '019e306e-4994-7421-b184-efe9d1162e64'::uuid))
               Heap Fetches: 0
               Buffers: shared hit=97
 Planning:
   Buffers: shared hit=10
 Planning Time: 0.048 ms
 Execution Time: 0.047 ms
```
</details>

### Q15 — Lịch sử đổi giờ mở của một bài
- **Ai gọi:** Giáo viên/quản trị, chi tiết bài
- **Tần suất ước tính:** ≈ 0,1/giây
- **Chỉ mục phục vụ:** `audit_log_target_idx`
- **Phân trang / giới hạn:** Cursor = `audit_log.id` (mới nhất trước); trang 20
- **Độ phức tạp:** O(log N + 21)
- **Ghi chú:** —
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.002 ms, lập kế hoạch 0.014 ms, trang bộ đệm 2, dòng đọc ở các nút quét 0, dòng trả về 0.

```sql
-- Lịch sử đổi giờ mở của một bài, mới nhất trước.
SELECT al.id, al.occurred_at, al.actor_user_id, al.action, al.before_release_at, al.after_release_at
FROM geo.audit_log al
WHERE al.org_id = :'org' AND al.target_id = :'assignment' AND al.id < 9223372036854775807
ORDER BY al.id DESC
LIMIT 21;
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Limit  (cost=0.28..2.50 rows=1 width=52) (actual time=0.001..0.001 rows=0 loops=1)
   Buffers: shared hit=2
   ->  Index Scan using audit_log_target_idx on audit_log al  (cost=0.28..2.50 rows=1 width=52) (actual time=0.001..0.001 rows=0 loops=1)
         Index Cond: ((org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid) AND (target_id = '01a1b2ee-51df-79ad-8354-128825f23080'::uuid) AND (id < '9223372036854775807'::bigint))
         Buffers: shared hit=2
 Planning:
   Buffers: shared hit=3
 Planning Time: 0.014 ms
 Execution Time: 0.002 ms
```
</details>

### Q16 — Tự học kế thừa khóa: chữ ký chuẩn (hoặc băm) khớp bài lớp đang khóa của em (D-GEO-150, 151)
- **Ai gọi:** Hàm cổng (fn_get_hint, fn_solution_gate, fn_create_self_assignment) cho bài tự học
- **Tần suất ước tính:** ≈ 3/giây (gợi ý + lời giải của bài tự học)
- **Chỉ mục phục vụ:** `problem_signature_pkey`, `problem_signature_lookup_idx`, `problem_version_hash_idx` (org_id, statement_hash), `class_member_user_idx`, `assignment_class_problem_uq`
- **Phân trang / giới hạn:** Tối đa 1 dòng
- **Độ phức tạp:** O(k·log N), k = số lớp của em × số đề kho cùng chữ ký (thường 1)
- **Ghi chú:** Thân của `geo.fn_self_lock_source`: khớp theo chữ ký chuẩn (hình mẫu, tham số quy về a = 1, đại lượng hỏi; cùng `sig_version`) và băm phụ, với bài lớp đang khóa của MỌI lớp em đang thuộc; gọi ở MỖI lần xin gợi ý/lời giải của bài tự học (D-GEO-150, 151; RV-01, 03; ADR-GEO-15). Đổi tên điểm (F-GEO-226) cho cùng chữ ký.
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.015 ms, lập kế hoạch 0.377 ms, trang bộ đệm 28, dòng đọc ở các nút quét 7, dòng trả về 1.

```sql
-- Thân của geo.fn_self_lock_source (D-GEO-150, 151; RV-01, 03): bài tự học có khớp một bài lớp ĐANG KHÓA của bất kỳ lớp nào em đang
-- thuộc không. Khớp chính theo chữ ký chuẩn (hình mẫu, tham số quy về a = 1, đại lượng hỏi) cùng sig_version; khớp phụ theo băm đề
-- chuẩn hóa. Đề kho có org_id = platform nên cả hai nhánh tra chỉ mục có org_id đứng đầu. Chạy mỗi lần xin Gợi ý / lời giải của bài tự học.
-- Vai chủ vì geo_app không đọc problem_signature (không cần).
SELECT x.id FROM (
  SELECT a.id
  FROM geo.problem_signature me
  JOIN geo.problem_signature cat ON cat.org_id = :'platform' AND cat.signature = me.signature AND cat.sig_version = me.sig_version
  JOIN geo.problem_version cpv   ON cpv.org_id = cat.org_id AND cpv.id = cat.problem_version_id
  JOIN geo.class_member cm       ON cm.org_id = :'inh_org' AND cm.user_id = :'inh_student' AND cm.left_at IS NULL
  JOIN geo.assignment a          ON a.org_id = cm.org_id AND a.class_id = cm.class_id AND a.problem_id = cpv.problem_id
                                AND a.kind = 'class' AND a.archived_at IS NULL
  WHERE me.org_id = :'inh_org' AND me.problem_version_id = :'inh_pv'
    AND (a.release_at IS NULL OR a.release_at > now())
  UNION ALL
  SELECT a.id
  FROM geo.problem_version me
  JOIN geo.problem_version cpv   ON cpv.org_id = :'platform' AND cpv.statement_hash = me.statement_hash
  JOIN geo.class_member cm       ON cm.org_id = :'inh_org' AND cm.user_id = :'inh_student' AND cm.left_at IS NULL
  JOIN geo.assignment a          ON a.org_id = cm.org_id AND a.class_id = cm.class_id AND a.problem_id = cpv.problem_id
                                AND a.kind = 'class' AND a.archived_at IS NULL
  WHERE me.org_id = :'inh_org' AND me.id = :'inh_pv'
    AND (a.release_at IS NULL OR a.release_at > now())
) x
LIMIT 1;
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Limit  (cost=1.54..9.79 rows=1 width=16) (actual time=0.007..0.007 rows=1 loops=1)
   Buffers: shared hit=28
   ->  Append  (cost=1.54..18.03 rows=2 width=16) (actual time=0.006..0.007 rows=1 loops=1)
         Buffers: shared hit=28
         ->  Nested Loop  (cost=1.54..9.32 rows=1 width=16) (actual time=0.006..0.007 rows=1 loops=1)
               Buffers: shared hit=28
               ->  Nested Loop  (cost=1.26..7.26 rows=1 width=51) (actual time=0.004..0.005 rows=2 loops=1)
                     Buffers: shared hit=22
                     ->  Nested Loop  (cost=0.98..5.21 rows=1 width=48) (actual time=0.003..0.004 rows=2 loops=1)
                           Buffers: shared hit=16
                           ->  Nested Loop  (cost=0.69..4.05 rows=1 width=32) (actual time=0.003..0.003 rows=2 loops=1)
                                 Buffers: shared hit=11
                                 ->  Index Only Scan using class_member_user_idx on class_member cm  (cost=0.41..1.53 rows=1 width=32) (actual time=0.001..0.001 rows=1 loops=1)
                                       Index Cond: ((org_id = '019dfd12-fe8a-7bbe-a7c0-a4cce1d9db38'::uuid) AND (user_id = '019e981c-0537-71ac-aa2a-4cbb0981d745'::uuid))
                                       Heap Fetches: 0
                                       Buffers: shared hit=4
                                 ->  Index Scan using assignment_class_problem_uq on assignment a  (cost=0.28..2.51 rows=1 width=64) (actual time=0.001..0.002 rows=2 loops=1)
                                       Index Cond: ((org_id = '019dfd12-fe8a-7bbe-a7c0-a4cce1d9db38'::uuid) AND (class_id = cm.class_id))
                                       Filter: ((release_at IS NULL) OR (release_at > now()))
                                       Rows Removed by Filter: 3
                                       Buffers: shared hit=7
                           ->  Index Only Scan using problem_version_org_id_problem_id_id_key on problem_version cpv  (cost=0.28..1.15 rows=1 width=48) (actual time=0.000..0.000 rows=1 loops=2)
                                 Index Cond: ((org_id = '00000000-0000-7000-8000-000000000001'::uuid) AND (problem_id = a.problem_id))
                                 Heap Fetches: 0
                                 Buffers: shared hit=5
                     ->  Index Scan using problem_signature_pkey on problem_signature cat  (cost=0.28..2.04 rows=1 width=67) (actual time=0.000..0.000 rows=1 loops=2)
                           Index Cond: ((org_id = '00000000-0000-7000-8000-000000000001'::uuid) AND (problem_version_id = cpv.id))
                           Buffers: shared hit=6
               ->  Index Scan using problem_signature_pkey on problem_signature me  (cost=0.28..2.05 rows=1 width=35) (actual time=0.001..0.001 rows=0 loops=2)
                     Index Cond: ((org_id = '019dfd12-fe8a-7bbe-a7c0-a4cce1d9db38'::uuid) AND (problem_version_id = '019f03ea-9ac9-769a-8260-ef29d1c901b8'::uuid) AND (sig_version = cat.sig_version))
                     Filter: (signature = cat.signature)
                     Rows Removed by Filter: 0
                     Buffers: shared hit=6
         ->  Nested Loop  (cost=1.26..8.71 rows=1 width=16) (never executed)
               Join Filter: (me_1.statement_hash = cpv_1.statement_hash)
               ->  Nested Loop  (cost=0.98..6.56 rows=1 width=65) (never executed)
                     ->  Nested Loop  (cost=0.69..4.04 rows=1 width=65) (never executed)
                           ->  Index Only Scan using class_member_user_idx on class_member cm_1  (cost=0.41..1.53 rows=1 width=32) (never executed)
                                 Index Cond: ((org_id = '019dfd12-fe8a-7bbe-a7c0-a4cce1d9db38'::uuid) AND (user_id = '019e981c-0537-71ac-aa2a-4cbb0981d745'::uuid))
                                 Heap Fetches: 0
                           ->  Index Scan using problem_version_org_id_id_key on problem_version me_1  (cost=0.28..2.50 rows=1 width=33) (never executed)
                                 Index Cond: ((org_id = '019dfd12-fe8a-7bbe-a7c0-a4cce1d9db38'::uuid) AND (id = '019f03ea-9ac9-769a-8260-ef29d1c901b8'::uuid))
                     ->  Index Scan using assignment_class_problem_uq on assignment a_1  (cost=0.28..2.51 rows=1 width=64) (never executed)
                           Index Cond: ((org_id = '019dfd12-fe8a-7bbe-a7c0-a4cce1d9db38'::uuid) AND (class_id = cm_1.class_id))
                           Filter: ((release_at IS NULL) OR (release_at > now()))
               ->  Index Scan using problem_version_problem_id_version_key on problem_version cpv_1  (cost=0.28..2.13 rows=1 width=49) (never executed)
                     Index Cond: (problem_id = a_1.problem_id)
                     Filter: (org_id = '00000000-0000-7000-8000-000000000001'::uuid)
 Planning:
   Buffers: shared hit=18
 Planning Time: 0.377 ms
 Execution Time: 0.015 ms
```
</details>

### Q17 — Màn đầu của giáo viên: các lớp của mình kèm sĩ số
- **Ai gọi:** Giáo viên, đầu phiên
- **Tần suất ước tính:** ≈ 2/giây
- **Chỉ mục phục vụ:** `class_teacher_idx`, `class_member_pkey` (đếm theo khoảng của MỘT lớp)
- **Phân trang / giới hạn:** Cursor = `class.id`; trang 20
- **Độ phức tạp:** O(20·(log N + s))
- **Ghi chú:** Một giáo viên có ít lớp nên thường một trang.
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.015 ms, lập kế hoạch 0.024 ms, trang bộ đệm 17, dòng đọc ở các nút quét 141, dòng trả về 3.

```sql
-- lint-allow: R-Q05 (đếm thành viên trong phạm vi MỘT lớp)
-- Màn đầu của giáo viên: các lớp của mình kèm sĩ số, phân trang theo id lớp.
SELECT c.id, c.name, c.level, c.grade, c.join_code, m.members
FROM geo.class c
CROSS JOIN LATERAL (SELECT count(*) AS members FROM geo.class_member cm
                    WHERE cm.org_id = c.org_id AND cm.class_id = c.id AND cm.left_at IS NULL) m
WHERE c.org_id = :'org' AND c.teacher_id = :'teacher' AND c.archived_at IS NULL AND c.id > '00000000-0000-0000-0000-000000000000'
ORDER BY c.id
LIMIT 21;
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Limit  (cost=1.81..4.05 rows=1 width=43) (actual time=0.006..0.012 rows=3 loops=1)
   Buffers: shared hit=17
   ->  Nested Loop  (cost=1.81..4.05 rows=1 width=43) (actual time=0.006..0.011 rows=3 loops=1)
         Buffers: shared hit=17
         ->  Index Scan using class_teacher_idx on class c  (cost=0.28..2.50 rows=1 width=51) (actual time=0.001..0.001 rows=3 loops=1)
               Index Cond: ((org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid) AND (teacher_id = '019e306e-4994-7421-b184-efe9d1162e64'::uuid) AND (id > '00000000-0000-0000-0000-000000000000'::uuid))
               Filter: (archived_at IS NULL)
               Buffers: shared hit=5
         ->  Aggregate  (cost=1.54..1.55 rows=1 width=8) (actual time=0.003..0.003 rows=1 loops=3)
               Buffers: shared hit=12
               ->  Index Only Scan using class_member_name_uq on class_member cm  (cost=0.41..1.53 rows=1 width=0) (actual time=0.001..0.002 rows=46 loops=3)
                     Index Cond: ((org_id = c.org_id) AND (class_id = c.id))
                     Heap Fetches: 0
                     Buffers: shared hit=12
 Planning:
   Buffers: shared hit=3
 Planning Time: 0.024 ms
 Execution Time: 0.015 ms
```
</details>

### Q18 — Chặng 7 (chừa chỗ): trường nhận có cấp phép còn hiệu lực cho tài nguyên?
- **Ai gọi:** Mọi đường đọc chéo trường ở chặng sau
- **Tần suất ước tính:** 0 ở MVP (không có đường đọc chéo trường)
- **Chỉ mục phục vụ:** `share_grant_lookup_idx` (một phần: `status = approved`)
- **Phân trang / giới hạn:** Tối đa 1 dòng
- **Độ phức tạp:** O(log N)
- **Ghi chú:** Bảng 35 hàng nên Seq Scan; ở 50.000 cấp phép dùng chỉ mục (mục "Kiểm ở quy mô lớn").
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.003 ms, lập kế hoạch 0.010 ms, trang bộ đệm 1, dòng đọc ở các nút quét 1, dòng trả về 1.

```sql
-- Chặng 7 (chừa chỗ): trường nhận có cấp phép còn hiệu lực cho tài nguyên không.
SELECT g.id, g.valid_until
FROM geo.share_grant g
WHERE g.grantee_org_id = :'grantee_org' AND g.resource_kind = 'lecture'
  AND g.resource_id = :'grant_res'
  AND g.status = 'approved' AND g.valid_from <= now() AND g.valid_until > now()
LIMIT 1;
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Limit  (cost=0.00..2.05 rows=1 width=24) (actual time=0.001..0.001 rows=1 loops=1)
   Buffers: shared hit=1
   ->  Seq Scan on share_grant g  (cost=0.00..2.05 rows=1 width=24) (actual time=0.001..0.001 rows=1 loops=1)
         Filter: ((grantee_org_id = '019dfcf3-ddca-7505-8514-57b4d0fbc64b'::uuid) AND (resource_kind = 'lecture'::geo.share_resource) AND (resource_id = '019f31eb-2d63-7aab-89b5-5d0ca2712ae5'::uuid) AND (status = 'approved'::geo.share_status) AND (valid_from <= now()) AND (valid_until > now()))
         Buffers: shared hit=1
 Planning Time: 0.010 ms
 Execution Time: 0.003 ms
```
</details>

### Q19 — Giáo viên xem một đề trong kho: đề, nhãn, hình, gợi ý (KNW-02, LRN-04)
- **Ai gọi:** Giáo viên, màn xem đề / xem như học sinh
- **Tần suất ước tính:** ≈ 5/giây (một phần của 5% kho đề)
- **Chỉ mục phục vụ:** `problem_pkey`, `problem_version_pkey`, `scene_build_org_id_problem_version_id_build_no_key`, `hint_pkey`
- **Phân trang / giới hạn:** Không (1 hàng)
- **Độ phức tạp:** O(log N) × 4
- **Ghi chú:** Lời giải KHÔNG nằm trong truy vấn này: giáo viên gọi `geo.fn_get_solution_teacher` (Q19b). Cũng là dữ liệu của xem trước đề CHƯA giao (bước 3; giờ đang chọn do giao diện giữ, D-GEO-146).
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.011 ms, lập kế hoạch 0.053 ms, trang bộ đệm 12, dòng đọc ở các nút quét 5, dòng trả về 1.

```sql
-- Giáo viên xem một đề trong kho (KNW-02, LRN-04): đề + nhãn + hình + gợi ý. Lời giải lấy riêng bằng geo.fn_get_solution_teacher (Q19b).
SELECT p.id, p.title, p.level, p.solid_kind, p.topic_id,
       pv.id AS problem_version_id, pv.statement, pv.verify_status, pv.approval_status, pv.ai_authored,
       sb.coord_source, sb.spec, sb.coords, h.hints
FROM geo.problem p
JOIN geo.problem_version pv ON pv.id = p.current_version_id
CROSS JOIN LATERAL (
    SELECT s.coord_source, s.spec, s.coords
    FROM geo.scene_build s
    WHERE s.org_id = pv.org_id AND s.problem_version_id = pv.id
    ORDER BY s.build_no DESC LIMIT 1) sb
CROSS JOIN LATERAL (
    SELECT array_agg(x.body ORDER BY x.level) AS hints
    FROM geo.hint x
    WHERE x.org_id = pv.org_id AND x.problem_version_id = pv.id) h
WHERE p.org_id = :'platform' AND p.id = :'problem' AND p.source = 'catalog' AND p.archived_at IS NULL;
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Nested Loop  (cost=3.21..9.91 rows=1 width=1240) (actual time=0.005..0.005 rows=1 loops=1)
   Buffers: shared hit=12
   ->  Nested Loop  (cost=0.70..7.38 rows=1 width=1224) (actual time=0.003..0.003 rows=1 loops=1)
         Buffers: shared hit=8
         ->  Nested Loop  (cost=0.42..4.86 rows=1 width=471) (actual time=0.002..0.002 rows=1 loops=1)
               Buffers: shared hit=5
               ->  Index Scan using problem_catalog_filter_idx on problem p  (cost=0.14..2.36 rows=1 width=64) (actual time=0.001..0.001 rows=1 loops=1)
                     Index Cond: ((org_id = '00000000-0000-7000-8000-000000000001'::uuid) AND (id = '019efe6b-8009-71bf-854f-2c8e8603c15a'::uuid))
                     Buffers: shared hit=2
               ->  Index Scan using problem_version_pkey on problem_version pv  (cost=0.28..2.50 rows=1 width=423) (actual time=0.000..0.001 rows=1 loops=1)
                     Index Cond: (id = p.current_version_id)
                     Buffers: shared hit=3
         ->  Limit  (cost=0.28..2.50 rows=1 width=755) (actual time=0.001..0.001 rows=1 loops=1)
               Buffers: shared hit=3
               ->  Index Scan Backward using scene_build_org_id_problem_version_id_build_no_key on scene_build s  (cost=0.28..2.50 rows=1 width=755) (actual time=0.001..0.001 rows=1 loops=1)
                     Index Cond: ((org_id = pv.org_id) AND (problem_version_id = pv.id))
                     Buffers: shared hit=3
   ->  Aggregate  (cost=2.51..2.52 rows=1 width=32) (actual time=0.001..0.001 rows=1 loops=1)
         Buffers: shared hit=4
         ->  Index Scan using hint_pkey on hint x  (cost=0.29..2.50 rows=1 width=184) (actual time=0.001..0.001 rows=2 loops=1)
               Index Cond: ((org_id = pv.org_id) AND (problem_version_id = pv.id))
               Buffers: shared hit=4
 Planning:
   Buffers: shared hit=6
 Planning Time: 0.053 ms
 Execution Time: 0.011 ms
```
</details>

### Q19b — Giáo viên xem lời giải của đề trong kho: thân `geo.fn_get_solution_teacher`
- **Ai gọi:** Giáo viên (tài khoản mẫu hoặc đã xác nhận, PO-18)
- **Tần suất ước tính:** ≈ 3/giây
- **Chỉ mục phục vụ:** `app_user_pkey`, `problem_version_pkey`, `problem_pkey`, `solution_pkey`
- **Phân trang / giới hạn:** Không (1 hàng)
- **Độ phức tạp:** O(log N) × 4
- **Ghi chú:** CHỈ đề kho nền tảng (`source = catalog`, D-GEO-153, RV-04): đề tự nhập của trường khác hay trường mình ⇒ 0 hàng ⇒ NOT_FOUND (D-GEO-175). Kiểm vai và loại tài khoản ngay trong CSDL. Chạy với vai chủ vì `geo_app` không đọc `solution`. Ca T1…T6 ở `07_verification.md` §5.
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.008 ms, lập kế hoạch 0.056 ms, trang bộ đệm 12, dòng đọc ở các nút quét 4, dòng trả về 1.

```sql
-- lint-allow: R-Q14 (thân của hàm SECURITY DEFINER fn_get_solution_teacher)
-- lint-allow: R-Q04 (tra khóa chính: 1 hàng)
-- Thân của geo.fn_get_solution_teacher (D-GEO-153, RV-04): giáo viên (mẫu hoặc đã xác nhận, PO-18) chỉ xem lời giải của ĐỀ KHO nền tảng
-- (problem.source = 'catalog', org_id = platform). Đề tự nhập hay nội dung trường khác: 0 hàng.
SELECT s.problem_version_id, s.final_answer, s.steps, s.schema_version, s.ai_authored
FROM geo.app_user u
JOIN geo.problem_version pv ON pv.org_id = :'platform' AND pv.id = :'problem_pv'
JOIN geo.problem p          ON p.org_id = pv.org_id AND p.id = pv.problem_id AND p.source = 'catalog'
JOIN geo.solution s         ON s.org_id = pv.org_id AND s.problem_version_id = pv.id
WHERE u.org_id = :'org' AND u.id = :'teacher' AND u.role = 'teacher' AND u.deactivated_at IS NULL
  AND u.account_kind IN ('sample', 'verified');
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Nested Loop  (cost=1.13..11.75 rows=1 width=286) (actual time=0.004..0.004 rows=1 loops=1)
   Buffers: shared hit=12
   ->  Nested Loop  (cost=0.85..9.24 rows=1 width=32) (actual time=0.003..0.003 rows=1 loops=1)
         Buffers: shared hit=9
         ->  Nested Loop  (cost=0.57..4.52 rows=1 width=48) (actual time=0.002..0.002 rows=1 loops=1)
               Buffers: shared hit=6
               ->  Index Only Scan using problem_version_org_id_problem_id_id_key on problem_version pv  (cost=0.28..1.99 rows=1 width=48) (actual time=0.001..0.001 rows=1 loops=1)
                     Index Cond: ((org_id = '00000000-0000-7000-8000-000000000001'::uuid) AND (id = '019f0391-dc09-7ac8-8cf8-f0472515434b'::uuid))
                     Heap Fetches: 0
                     Buffers: shared hit=3
               ->  Index Scan using app_user_pkey on app_user u  (cost=0.29..2.51 rows=1 width=0) (actual time=0.001..0.001 rows=1 loops=1)
                     Index Cond: (id = '019e306e-4994-7421-b184-efe9d1162e64'::uuid)
                     Filter: ((deactivated_at IS NULL) AND (account_kind = ANY ('{sample,verified}'::account_kind[])) AND (org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid) AND (role = 'teacher'::user_role))
                     Buffers: shared hit=3
         ->  Index Scan using problem_org_id_id_owner_user_id_key on problem p  (cost=0.28..2.50 rows=1 width=32) (actual time=0.001..0.001 rows=1 loops=1)
               Index Cond: ((org_id = '00000000-0000-7000-8000-000000000001'::uuid) AND (id = pv.problem_id))
               Filter: (source = 'catalog'::problem_source)
               Buffers: shared hit=3
   ->  Index Scan using solution_pkey on solution s  (cost=0.28..2.50 rows=1 width=302) (actual time=0.001..0.001 rows=1 loops=1)
         Index Cond: ((org_id = '00000000-0000-7000-8000-000000000001'::uuid) AND (problem_version_id = '019f0391-dc09-7ac8-8cf8-f0472515434b'::uuid))
         Buffers: shared hit=3
 Planning:
   Buffers: shared hit=12
 Planning Time: 0.056 ms
 Execution Time: 0.008 ms
```
</details>

### Q20 — Dòng thời gian một học sinh trong một bài (giáo viên bấm xem)
- **Ai gọi:** Giáo viên, chi tiết học sinh
- **Tần suất ước tính:** ≈ 1/giây
- **Chỉ mục phục vụ:** `event_log_*_org_id_assignment_id_user_id_occurred_at_id_idx` ở các partition thuộc khoảng thời gian
- **Phân trang / giới hạn:** Cursor = `(occurred_at, id)` (mới nhất trước); trang 50 (LIMIT 51); BẮT BUỘC có cận `occurred_at`
- **Độ phức tạp:** O(P·log N + 51), P = số partition trong cận
- **Ghi chú:** Cận thời gian lấy từ thời điểm tạo bài (UUIDv7 chứa thời gian) để cắt partition.
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.009 ms, lập kế hoạch 0.078 ms, trang bộ đệm 13, dòng đọc ở các nút quét 7, dòng trả về 7.

```sql
-- Dòng thời gian một học sinh trong một bài (giáo viên bấm xem), keyset (occurred_at, id), CÓ cận thời gian để cắt partition.
SELECT e.id, e.occurred_at, e.kind, e.arg
FROM geo.event_log e
WHERE e.org_id = :'ev_org' AND e.assignment_id = :'ev_assignment' AND e.user_id = :'ev_student'
  AND e.occurred_at >= :'lo_ts' AND e.occurred_at < :'hi_ts'
ORDER BY e.occurred_at DESC, e.id DESC
LIMIT 51;
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Limit  (cost=1.69..10.62 rows=4 width=22) (actual time=0.002..0.005 rows=7 loops=1)
   Buffers: shared hit=13
   ->  Append  (cost=1.69..10.62 rows=4 width=22) (actual time=0.002..0.004 rows=7 loops=1)
         Buffers: shared hit=13
         ->  Index Scan using event_log_y2026m11_org_id_assignment_id_user_id_occurred_at_idx on event_log_y2026m11 e_4  (cost=0.42..2.65 rows=1 width=22) (actual time=0.001..0.001 rows=0 loops=1)
               Index Cond: ((org_id = '019dfcf2-f36a-7856-9078-881ceaa9959e'::uuid) AND (assignment_id = '01a1860e-19e4-7717-b3b9-7532ba79b755'::uuid) AND (user_id = '019e976c-4507-7958-922f-5ee5e4dd16fe'::uuid) AND (occurred_at >= '2026-08-24 17:55:39.585054+07'::timestamp with time zone) AND (occurred_at < '2026-11-23 17:55:39.585054+07'::timestamp with time zone))
               Buffers: shared hit=3
         ->  Index Scan using event_log_y2026m10_org_id_assignment_id_user_id_occurred_at_idx on event_log_y2026m10 e_3  (cost=0.42..2.65 rows=1 width=22) (actual time=0.001..0.002 rows=7 loops=1)
               Index Cond: ((org_id = '019dfcf2-f36a-7856-9078-881ceaa9959e'::uuid) AND (assignment_id = '01a1860e-19e4-7717-b3b9-7532ba79b755'::uuid) AND (user_id = '019e976c-4507-7958-922f-5ee5e4dd16fe'::uuid) AND (occurred_at >= '2026-08-24 17:55:39.585054+07'::timestamp with time zone) AND (occurred_at < '2026-11-23 17:55:39.585054+07'::timestamp with time zone))
               Buffers: shared hit=4
         ->  Index Scan using event_log_y2026m09_org_id_assignment_id_user_id_occurred_at_idx on event_log_y2026m09 e_2  (cost=0.42..2.65 rows=1 width=22) (actual time=0.001..0.001 rows=0 loops=1)
               Index Cond: ((org_id = '019dfcf2-f36a-7856-9078-881ceaa9959e'::uuid) AND (assignment_id = '01a1860e-19e4-7717-b3b9-7532ba79b755'::uuid) AND (user_id = '019e976c-4507-7958-922f-5ee5e4dd16fe'::uuid) AND (occurred_at >= '2026-08-24 17:55:39.585054+07'::timestamp with time zone) AND (occurred_at < '2026-11-23 17:55:39.585054+07'::timestamp with time zone))
               Buffers: shared hit=3
         ->  Index Scan using event_log_y2026m08_org_id_assignment_id_user_id_occurred_at_idx on event_log_y2026m08 e_1  (cost=0.42..2.65 rows=1 width=22) (actual time=0.001..0.001 rows=0 loops=1)
               Index Cond: ((org_id = '019dfcf2-f36a-7856-9078-881ceaa9959e'::uuid) AND (assignment_id = '01a1860e-19e4-7717-b3b9-7532ba79b755'::uuid) AND (user_id = '019e976c-4507-7958-922f-5ee5e4dd16fe'::uuid) AND (occurred_at >= '2026-08-24 17:55:39.585054+07'::timestamp with time zone) AND (occurred_at < '2026-11-23 17:55:39.585054+07'::timestamp with time zone))
               Buffers: shared hit=3
 Planning Time: 0.078 ms
 Execution Time: 0.009 ms
```
</details>

### Q20b — ĐỐI CHỨNG: dòng thời gian không có cận thời gian
- **Ai gọi:** Không dùng trong mã (chỉ để minh họa luật R-Q09)
- **Tần suất ước tính:** —
- **Chỉ mục phục vụ:** như Q20
- **Phân trang / giới hạn:** như Q20
- **Độ phức tạp:** O(P_tất_cả·log N): thăm chỉ mục của mọi partition kể cả rỗng
- **Ghi chú:** Ở 7 partition chênh lệch nhỏ (xem số), nhưng chi phí tăng tuyến tính theo số partition giữ lại; luật R-Q09 cấm.
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.014 ms, lập kế hoạch 0.119 ms, trang bộ đệm 13, dòng đọc ở các nút quét 7, dòng trả về 7.

```sql
-- lint-allow: R-Q09 (ví dụ đối chứng cố ý; không dùng trong mã)
-- ĐỐI CHỨNG (xấu): cùng truy vấn nhưng không có cận thời gian => phải thăm chỉ mục của mọi partition.
SELECT e.id, e.occurred_at, e.kind, e.arg
FROM geo.event_log e
WHERE e.org_id = :'ev_org' AND e.assignment_id = :'ev_assignment' AND e.user_id = :'ev_student'
ORDER BY e.occurred_at DESC, e.id DESC
LIMIT 51;
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Limit  (cost=10.74..10.76 rows=8 width=22) (actual time=0.007..0.007 rows=7 loops=1)
   Buffers: shared hit=13
   ->  Sort  (cost=10.74..10.76 rows=8 width=22) (actual time=0.007..0.007 rows=7 loops=1)
         Sort Key: e.occurred_at DESC, e.id DESC
         Sort Method: quicksort  Memory: 25kB
         Buffers: shared hit=13
         ->  Append  (cost=0.00..10.62 rows=8 width=22) (actual time=0.004..0.006 rows=7 loops=1)
               Buffers: shared hit=13
               ->  Seq Scan on event_log_y2026m07 e_1  (cost=0.00..0.00 rows=1 width=22) (actual time=0.001..0.001 rows=0 loops=1)
                     Filter: ((org_id = '019dfcf2-f36a-7856-9078-881ceaa9959e'::uuid) AND (assignment_id = '01a1860e-19e4-7717-b3b9-7532ba79b755'::uuid) AND (user_id = '019e976c-4507-7958-922f-5ee5e4dd16fe'::uuid))
               ->  Index Scan using event_log_y2026m08_org_id_assignment_id_user_id_occurred_at_idx on event_log_y2026m08 e_2  (cost=0.42..2.64 rows=1 width=22) (actual time=0.001..0.001 rows=0 loops=1)
                     Index Cond: ((org_id = '019dfcf2-f36a-7856-9078-881ceaa9959e'::uuid) AND (assignment_id = '01a1860e-19e4-7717-b3b9-7532ba79b755'::uuid) AND (user_id = '019e976c-4507-7958-922f-5ee5e4dd16fe'::uuid))
                     Buffers: shared hit=3
               ->  Index Scan using event_log_y2026m09_org_id_assignment_id_user_id_occurred_at_idx on event_log_y2026m09 e_3  (cost=0.42..2.65 rows=1 width=22) (actual time=0.001..0.001 rows=0 loops=1)
                     Index Cond: ((org_id = '019dfcf2-f36a-7856-9078-881ceaa9959e'::uuid) AND (assignment_id = '01a1860e-19e4-7717-b3b9-7532ba79b755'::uuid) AND (user_id = '019e976c-4507-7958-922f-5ee5e4dd16fe'::uuid))
                     Buffers: shared hit=3
               ->  Index Scan using event_log_y2026m10_org_id_assignment_id_user_id_occurred_at_idx on event_log_y2026m10 e_4  (cost=0.42..2.65 rows=1 width=22) (actual time=0.001..0.002 rows=7 loops=1)
                     Index Cond: ((org_id = '019dfcf2-f36a-7856-9078-881ceaa9959e'::uuid) AND (assignment_id = '01a1860e-19e4-7717-b3b9-7532ba79b755'::uuid) AND (user_id = '019e976c-4507-7958-922f-5ee5e4dd16fe'::uuid))
                     Buffers: shared hit=4
               ->  Index Scan using event_log_y2026m11_org_id_assignment_id_user_id_occurred_at_idx on event_log_y2026m11 e_5  (cost=0.42..2.65 rows=1 width=22) (actual time=0.001..0.001 rows=0 loops=1)
                     Index Cond: ((org_id = '019dfcf2-f36a-7856-9078-881ceaa9959e'::uuid) AND (assignment_id = '01a1860e-19e4-7717-b3b9-7532ba79b755'::uuid) AND (user_id = '019e976c-4507-7958-922f-5ee5e4dd16fe'::uuid))
                     Buffers: shared hit=3
               ->  Seq Scan on event_log_y2026m12 e_6  (cost=0.00..0.00 rows=1 width=22) (actual time=0.000..0.000 rows=0 loops=1)
                     Filter: ((org_id = '019dfcf2-f36a-7856-9078-881ceaa9959e'::uuid) AND (assignment_id = '01a1860e-19e4-7717-b3b9-7532ba79b755'::uuid) AND (user_id = '019e976c-4507-7958-922f-5ee5e4dd16fe'::uuid))
               ->  Seq Scan on event_log_y2027m01 e_7  (cost=0.00..0.00 rows=1 width=22) (actual time=0.000..0.000 rows=0 loops=1)
                     Filter: ((org_id = '019dfcf2-f36a-7856-9078-881ceaa9959e'::uuid) AND (assignment_id = '01a1860e-19e4-7717-b3b9-7532ba79b755'::uuid) AND (user_id = '019e976c-4507-7958-922f-5ee5e4dd16fe'::uuid))
               ->  Seq Scan on event_log_default e_8  (cost=0.00..0.00 rows=1 width=22) (actual time=0.000..0.000 rows=0 loops=1)
                     Filter: ((org_id = '019dfcf2-f36a-7856-9078-881ceaa9959e'::uuid) AND (assignment_id = '01a1860e-19e4-7717-b3b9-7532ba79b755'::uuid) AND (user_id = '019e976c-4507-7958-922f-5ee5e4dd16fe'::uuid))
 Planning:
   Buffers: shared hit=4
 Planning Time: 0.119 ms
 Execution Time: 0.014 ms
```
</details>

### Q21 — Lịch sử hỏi đáp của chính học sinh trên mọi bài (US-GEO-410, D-GEO-159, RV-19)
- **Ai gọi:** Học sinh, mục Lịch sử (qua `fn_qa_history`)
- **Tần suất ước tính:** ≈ 2/giây
- **Chỉ mục phục vụ:** `qa_turn_user_idx`
- **Phân trang / giới hạn:** Cursor = `qa_turn.id` (mới nhất trước); trang 20 (LIMIT 21), trần 100
- **Độ phức tạp:** O(log N + 21)
- **Ghi chú:** Trả câu trả lời đã lưu (đã qua Kiểm soát đáp án), mở lại không gọi AI. Chỉ chủ câu hỏi: hàm lọc `user_id` từ thẻ; `geo_app` không SELECT được `reply_text`. Trước khi có chỉ mục: 325 trang (mục 3).
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.006 ms, lập kế hoạch 0.014 ms, trang bộ đệm 13, dòng đọc ở các nút quét 18, dòng trả về 18.

```sql
-- Thân của geo.fn_qa_history (US-GEO-410, D-GEO-159, RV-19): lịch sử hỏi đáp của CHÍNH học sinh trên mọi bài, mới nhất trước,
-- keyset theo id, trang 20 (LIMIT 21), trần 100. Gồm câu trả lời đã lưu (đã qua Kiểm soát đáp án), mở lại không gọi AI.
-- Vai chủ vì geo_app không có SELECT cột reply_text (chỉ chủ câu hỏi đọc, qua hàm).
SELECT q.id, q.assignment_id, q.asked_at, q.question, q.reply_kind, q.reply_text, q.source_lecture_ids
FROM geo.qa_turn q
WHERE q.org_id = :'qa_org' AND q.user_id = :'qa_student' AND q.id < 9223372036854775807
ORDER BY q.id DESC
LIMIT 21;
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Limit  (cost=0.42..2.64 rows=1 width=595) (actual time=0.001..0.004 rows=18 loops=1)
   Buffers: shared hit=13
   ->  Index Scan using qa_turn_user_idx on qa_turn q  (cost=0.42..2.64 rows=1 width=595) (actual time=0.001..0.003 rows=18 loops=1)
         Index Cond: ((org_id = '019dfd19-672a-7a09-be6b-20d63008896e'::uuid) AND (user_id = '019e97be-702f-7701-8695-ca06c246cdca'::uuid) AND (id < '9223372036854775807'::bigint))
         Buffers: shared hit=13
 Planning:
   Buffers: shared hit=4
 Planning Time: 0.014 ms
 Execution Time: 0.006 ms
```
</details>

### Q22 — Danh sách bài đã giao của giáo viên trên mọi lớp (US-GEO-701-SC6, D-GEO-129, RV-19)
- **Ai gọi:** Giáo viên, màn Bài đã giao
- **Tần suất ước tính:** ≈ 1/giây
- **Chỉ mục phục vụ:** `class_teacher_idx`, `assignment_class_idx`, `problem_pkey`
- **Phân trang / giới hạn:** Cursor = `assignment.id` (mới nhất trước); trang 20 (LIMIT 21), trần 100
- **Độ phức tạp:** O(c·(log A + 21)), c = số lớp của giáo viên
- **Ghi chú:** Mỗi lớp lấy 21 dòng theo chỉ mục rồi gộp (như Q02); Sort cuối chỉ trên ≤ 21·c dòng. Không có mục "Tổng quan", "Lớp học" (D-GEO-129).
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.032 ms, lập kế hoạch 0.055 ms, trang bộ đệm 131, dòng đọc ở các nút quét 63, dòng trả về 21.

```sql
-- Danh sách bài đã giao của giáo viên trên MỌI lớp của mình (US-GEO-701-SC6, D-GEO-129, RV-19), mới nhất trước, keyset theo
-- assignment.id (UUIDv7), trang 20 (LIMIT 21), trần 100. Mỗi lớp lấy riêng 21 dòng theo chỉ mục rồi gộp (như Q02), tránh sắp toàn bộ.
SELECT a.id, a.class_id, c.name AS class_name, a.release_at,
       CASE WHEN a.release_at IS NULL THEN 'unset' WHEN a.release_at > now() THEN 'scheduled' ELSE 'open' END AS release_state,
       p.title, p.solid_kind
FROM geo.class c
CROSS JOIN LATERAL (
    SELECT x.id, x.class_id, x.release_at, x.content_org_id, x.problem_id
    FROM geo.assignment x
    WHERE x.org_id = c.org_id AND x.class_id = c.id AND x.kind = 'class' AND x.id < :'cursor_max'
    ORDER BY x.id DESC LIMIT 21) a
JOIN geo.problem p ON p.org_id = a.content_org_id AND p.id = a.problem_id
WHERE c.org_id = :'org' AND c.teacher_id = :'teacher' AND c.archived_at IS NULL
ORDER BY a.id DESC
LIMIT 21;
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Limit  (cost=7.53..7.53 rows=1 width=104) (actual time=0.026..0.027 rows=21 loops=1)
   Buffers: shared hit=131
   ->  Sort  (cost=7.53..7.53 rows=1 width=104) (actual time=0.026..0.026 rows=21 loops=1)
         Sort Key: x.id DESC
         Sort Method: quicksort  Memory: 28kB
         Buffers: shared hit=131
         ->  Nested Loop  (cost=0.84..7.52 rows=1 width=104) (actual time=0.003..0.023 rows=30 loops=1)
               Buffers: shared hit=131
               ->  Nested Loop  (cost=0.56..5.01 rows=1 width=78) (actual time=0.002..0.009 rows=30 loops=1)
                     Buffers: shared hit=41
                     ->  Index Scan using class_teacher_idx on class c  (cost=0.28..2.49 rows=1 width=38) (actual time=0.001..0.001 rows=3 loops=1)
                           Index Cond: ((org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid) AND (teacher_id = '019e306e-4994-7421-b184-efe9d1162e64'::uuid))
                           Filter: (archived_at IS NULL)
                           Buffers: shared hit=5
                     ->  Limit  (cost=0.28..2.50 rows=1 width=72) (actual time=0.001..0.002 rows=10 loops=3)
                           Buffers: shared hit=36
                           ->  Index Scan using assignment_class_idx on assignment x  (cost=0.28..2.50 rows=1 width=72) (actual time=0.001..0.002 rows=10 loops=3)
                                 Index Cond: ((org_id = c.org_id) AND (class_id = c.id) AND (id < 'ffffffff-ffff-ffff-ffff-ffffffffffff'::uuid))
                                 Buffers: shared hit=36
               ->  Index Scan using problem_org_id_id_owner_user_id_key on problem p  (cost=0.28..2.50 rows=1 width=58) (actual time=0.000..0.000 rows=1 loops=30)
                     Index Cond: ((org_id = x.content_org_id) AND (id = x.problem_id))
                     Buffers: shared hit=90
 Planning:
   Buffers: shared hit=9
 Planning Time: 0.055 ms
 Execution Time: 0.032 ms
```
</details>

### Q23 — Xem trước "như học sinh" một bài đã giao (US-GEO-607, D-GEO-139, 180; RV-27)
- **Ai gọi:** Giáo viên, nút Xem trước
- **Tần suất ước tính:** ≈ 1/giây
- **Chỉ mục phục vụ:** `assignment_org_id_id_key`, `class_pkey`, `problem_version_org_id_id_key`, `problem_pkey`, khóa duy nhất `scene_build`
- **Phân trang / giới hạn:** Không (1 hàng)
- **Độ phức tạp:** O(log N) × 5
- **Ghi chú:** Đúng dữ liệu của Q04 và trạng thái giờ mở của chính bài giao; KHÔNG gọi hàm lời giải (D-GEO-153); bài đã mở chỉ hiện nhãn MSG-GEO-29 (D-GEO-180); không ghi sự kiện, không tiến độ (D-GEO-120). Đề chưa giao: Q19 + giờ đang chọn ở giao diện (D-GEO-146).
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.012 ms, lập kế hoạch 0.169 ms, trang bộ đệm 15, dòng đọc ở các nút quét 5, dòng trả về 1.

```sql
-- lint-allow: R-Q04 (tra khóa chính: 1 hàng)
-- Xem trước "như học sinh" một bài ĐÃ GIAO (US-GEO-607, D-GEO-139, RV-27): đúng dữ liệu của Q04 (đề, hình, đại lượng ẩn) và trạng thái
-- giờ mở của CHÍNH bài giao đó; KHÔNG gọi hàm lời giải (D-GEO-153), không ghi sự kiện, không tính tiến độ (D-GEO-120). Kiểm giáo viên của lớp.
-- Đề CHƯA giao (bước 3 của giao bài): dùng Q19 (không lời giải) + giờ đang chọn do giao diện giữ, không có truy vấn trạng thái (D-GEO-146).
SELECT a.id, a.release_at, a.release_rev,
       CASE WHEN a.release_at IS NULL THEN 'unset' WHEN a.release_at > now() THEN 'scheduled' ELSE 'open' END AS release_state,
       a.allow_hint, a.allow_qa,
       pv.id AS problem_version_id, pv.statement, pv.verify_status, pv.approval_status, pv.ai_authored,
       p.title, p.solid_kind, sb.coord_source, sb.schema_version, sb.spec, sb.coords, sb.hidden_measures
FROM geo.assignment a
JOIN geo.class c            ON c.org_id = a.org_id AND c.id = a.class_id AND c.teacher_id = :'teacher'
JOIN geo.problem_version pv ON pv.org_id = a.content_org_id AND pv.id = a.problem_version_id
JOIN geo.problem p          ON p.org_id = pv.org_id AND p.id = pv.problem_id
CROSS JOIN LATERAL (
    SELECT s.coord_source, s.schema_version, s.spec, s.coords, s.hidden_measures
    FROM geo.scene_build s
    WHERE s.org_id = pv.org_id AND s.problem_version_id = pv.id
    ORDER BY s.build_no DESC LIMIT 1) sb
WHERE a.org_id = :'org' AND a.id = :'assignment' AND a.kind = 'class';
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Nested Loop  (cost=1.41..9.31 rows=1 width=1343) (actual time=0.005..0.005 rows=1 loops=1)
   Buffers: shared hit=15
   ->  Nested Loop  (cost=1.12..6.78 rows=1 width=479) (actual time=0.003..0.004 rows=1 loops=1)
         Buffers: shared hit=12
         ->  Nested Loop  (cost=0.84..6.42 rows=1 width=485) (actual time=0.003..0.003 rows=1 loops=1)
               Buffers: shared hit=9
               ->  Nested Loop  (cost=0.56..3.92 rows=1 width=62) (actual time=0.002..0.002 rows=1 loops=1)
                     Join Filter: (c.id = a.class_id)
                     Buffers: shared hit=6
                     ->  Index Scan using assignment_org_id_id_key on assignment a  (cost=0.29..2.51 rows=1 width=94) (actual time=0.001..0.001 rows=1 loops=1)
                           Index Cond: ((org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid) AND (id = '01a1b2ee-51df-79ad-8354-128825f23080'::uuid))
                           Filter: (kind = 'class'::geo.assignment_kind)
                           Buffers: shared hit=3
                     ->  Index Only Scan using class_teacher_idx on class c  (cost=0.28..1.40 rows=1 width=32) (actual time=0.001..0.001 rows=1 loops=1)
                           Index Cond: ((org_id = '019dfcf4-c82a-7567-8143-476430b3f9cd'::uuid) AND (teacher_id = '019e306e-4994-7421-b184-efe9d1162e64'::uuid))
                           Heap Fetches: 0
                           Buffers: shared hit=3
               ->  Index Scan using problem_version_org_id_id_key on problem_version pv  (cost=0.28..2.50 rows=1 width=439) (actual time=0.001..0.001 rows=1 loops=1)
                     Index Cond: ((org_id = a.content_org_id) AND (id = a.problem_version_id))
                     Buffers: shared hit=3
         ->  Index Scan using problem_pkey on problem p  (cost=0.28..0.35 rows=1 width=58) (actual time=0.001..0.001 rows=1 loops=1)
               Index Cond: (id = pv.problem_id)
               Filter: (pv.org_id = org_id)
               Buffers: shared hit=3
   ->  Limit  (cost=0.28..2.50 rows=1 width=850) (actual time=0.001..0.001 rows=1 loops=1)
         Buffers: shared hit=3
         ->  Index Scan Backward using scene_build_org_id_problem_version_id_build_no_key on scene_build s  (cost=0.28..2.50 rows=1 width=850) (actual time=0.001..0.001 rows=1 loops=1)
               Index Cond: ((org_id = pv.org_id) AND (problem_version_id = pv.id))
               Buffers: shared hit=3
 Planning:
   Buffers: shared hit=24
 Planning Time: 0.169 ms
 Execution Time: 0.012 ms
```
</details>

### Q24 — Các câu đọc của `geo.fn_teacher_login` (D-GEO-152, 173; RV-02)
- **Ai gọi:** Giáo viên mẫu đăng nhập
- **Tần suất ước tính:** ≈ 0,1/giây
- **Chỉ mục phục vụ:** `teacher_access_code_selector_uq`, `auth_failure_ip_idx`, `auth_failure_user_idx`, `app_user_pkey`
- **Phân trang / giới hạn:** Đếm dừng ở 10 (LIMIT 10)
- **Độ phức tạp:** O(log N)
- **Ghi chú:** Bảng `auth_failure` rỗng trong dữ liệu thử nên Seq Scan; ở 300.000 lần sai dùng Index Only Scan, 0,046 ms (mục 3). Không biết trường trước đăng nhập: ngoại lệ T2 ghi ở chỉ mục.
- **Đo (máy cục bộ, trung vị 7 lần, vai `geo_app`):** thực thi 0.008 ms, lập kế hoạch 0.046 ms, trang bộ đệm 5, dòng đọc ở các nút quét 2, dòng trả về 1.

```sql
-- lint-allow: R-Q10 (trước đăng nhập chưa biết trường: tra theo selector và theo khóa IP, D-GEO-152)
-- lint-allow: R-Q04 (tra khóa duy nhất selector: 1 hàng)
-- lint-allow: R-Q05 (đếm có LIMIT 10 trên khoảng chỉ mục (ip_key, at) và (user_id, at): tối đa 10 hàng)
-- Các câu đọc bên trong geo.fn_teacher_login: đếm lần sai theo IP và theo tài khoản trong 15 phút (dừng ở 10), tra mã theo selector.
SELECT (SELECT count(*) FROM (SELECT 1 FROM geo.auth_failure f WHERE f.ip_key = decode(:'ip_key', 'hex') AND f.at > now() - interval '15 minutes' LIMIT 10) x) AS fail_ip,
       (SELECT count(*) FROM (SELECT 1 FROM geo.auth_failure f WHERE f.user_id = t.user_id AND f.at > now() - interval '15 minutes' LIMIT 10) y) AS fail_acct,
       t.id, t.org_id, t.user_id, t.verifier_hmac = decode(:'verifier', 'hex') AS ok, t.expires_at, t.revoked_at, u.account_kind
FROM geo.teacher_access_code t
JOIN geo.app_user u ON u.org_id = t.org_id AND u.id = t.user_id
WHERE t.selector = :'selector';
```

<details><summary>EXPLAIN (ANALYZE, BUFFERS) thật</summary>

```
 Nested Loop  (cost=0.46..4.93 rows=1 width=85) (actual time=0.003..0.004 rows=1 loops=1)
   Buffers: shared hit=5
   InitPlan 1
     ->  Aggregate  (cost=0.01..0.02 rows=1 width=8) (actual time=0.001..0.001 rows=1 loops=1)
           ->  Limit  (cost=0.00..0.00 rows=1 width=4) (actual time=0.001..0.001 rows=0 loops=1)
                 ->  Seq Scan on auth_failure f  (cost=0.00..0.00 rows=1 width=4) (actual time=0.001..0.001 rows=0 loops=1)
                       Filter: ((ip_key = '\xfec52565aa0cf18f57d7cf5b3ac728503b8992d2d6f7d46da1d1201090902b02'::bytea) AND (at > (now() - '00:15:00'::interval)))
   ->  Index Scan using teacher_access_code_selector_uq on teacher_access_code t  (cost=0.14..2.36 rows=1 width=97) (actual time=0.001..0.001 rows=1 loops=1)
         Index Cond: (selector = 'CFTH'::text)
         Buffers: shared hit=2
   ->  Index Scan using app_user_pkey on app_user u  (cost=0.29..2.51 rows=1 width=36) (actual time=0.001..0.001 rows=1 loops=1)
         Index Cond: (id = t.user_id)
         Filter: (org_id = t.org_id)
         Buffers: shared hit=3
   SubPlan 2
     ->  Aggregate  (cost=0.01..0.02 rows=1 width=8) (actual time=0.001..0.001 rows=1 loops=1)
           ->  Limit  (cost=0.00..0.00 rows=1 width=4) (actual time=0.000..0.000 rows=0 loops=1)
                 ->  Seq Scan on auth_failure f_1  (cost=0.00..0.00 rows=1 width=4) (actual time=0.000..0.000 rows=0 loops=1)
                       Filter: ((user_id = t.user_id) AND (at > (now() - '00:15:00'::interval)))
 Planning:
   Buffers: shared hit=12
 Planning Time: 0.046 ms
 Execution Time: 0.008 ms
```
</details>

## 3. Kiểm ở quy mô lớn hơn (bảng nhỏ trong dữ liệu thử)

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

## 4. Lịch sử tối ưu (trước/sau)

Quy tắc của lượt này: tối đa 3 lượt tối ưu một truy vấn. Truy vấn nào còn tồn tại ghi ở "khoảng trống đã biết" (`07_verification.md` §10).

### Q12 — xếp hạng mọi bài khớp → xếp hạng trên tập ứng viên giới hạn (1 lượt, đạt)
- **Vấn đề:** ở 50.000 bài giảng với cụm từ phổ biến (10.075 bài khớp), phiên bản đầu (`bench/queries/history/Q12_v1_rank_all_matches.sql`) tính `ts_rank` cho **mọi** bài khớp rồi mới lấy 5: chi phí tăng theo số bài khớp (đọc 10.075 hàng, `Sort`).
- **Trước (v1):** 21.776 ms (Bitmap Heap Scan 10.075 hàng + Sort). **Sau (v2):** 3.732 ms (ứng viên giới hạn 200, rồi lấy 5, rồi đọc đoạn đầu 600 ký tự của đúng 5 bài). Ở bộ dữ liệu 300 bài giảng: 0.084 ms.
- **Đánh đổi:** xếp hạng xấp xỉ khi có hơn 200 bài khớp (cụm từ quá phổ biến); chấp nhận được vì nguồn cho hỏi đáp chỉ cần 5 bài liên quan nhất của giáo trình mẫu (vài trăm bài). Nếu cần chất lượng xếp hạng toàn cục ở quy mô lớn: dùng RUM/chỉ mục xếp hạng hoặc một dịch vụ tìm kiếm (không cần ở MVP).

### Q20 — dòng thời gian: thêm cận thời gian (không phải tối ưu mà là luật)
- **Ghi nhận:** Q20 (có cận `occurred_at`): 0.009 ms; Q20b (không cận): 0.014 ms, vì chỉ có 4 partition có dữ liệu và chỉ mục theo `(org_id, assignment_id, user_id, …)` rất chọn lọc. Chi phí Q20b tăng **tuyến tính theo số partition giữ lại** (mỗi partition thêm một lần dò chỉ mục kể cả partition rỗng); với 12 tháng lưu giữ thì 14 lần dò. Luật R-Q09 vẫn bắt buộc cận để cắt partition khi bảng lớn hơn.

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

## 5. Mẫu bị cấm và cách kiểm tự động

Bảng dưới là luật đề xuất cho kiểm thử kiến trúc. Hai lớp kiểm: **(S) tĩnh** — đọc SQL (tệp `.sql`, hằng chuỗi trong lớp truy cập dữ liệu) và mã; **(K) kế hoạch** — chạy `EXPLAIN (FORMAT JSON)` từng truy vấn của catalogue trên dữ liệu thử rồi kiểm các nút. Bản mẫu chạy được: `python3 bench/lint_queries.py` (luật S + K cho catalogue; đã chạy: 321 kiểm, 0 vi phạm; và `QDIR=bench/lint_selftest python3 bench/lint_queries.py` cho 11 truy vấn xấu cố ý: **cả 11 bị bắt**, nên bộ kiểm không "qua mọi thứ"). Ở sản phẩm, luật S nối vào `SqlLint` của khung mã (đã đọc `sql-lint.properties`: bảng lớn phải có LIMIT; bảng thuộc trường phải có `org_id`; chủ sở hữu mô-đun) và ArchUnit; luật K nối vào kiểm thử tích hợp có PostgreSQL thật. Ngoại lệ ghi tại chỗ bằng chú thích `lint-allow: R-Qnn (lý do)` và được rà soát từng cái.

Cột "Đo thật mẫu xấu" là số đo của lượt trước trên `geo_arch` (`bench/09_antipatterns.sql`, `03_scale_checks.sql`, pgbench); lượt sửa sau RV không đổi các luật nên không đo lại. Lượt này chạy lại bộ kiểm tự động trên catalogue mới: `lint: 453 kiểm đạt, 0 vi phạm`; tự kiểm 11 truy vấn xấu vẫn bị bắt đủ. Luật thêm của SqlLint ở khung mã (D-GEO-171): câu đọc nội dung nền tảng `org_id = :platformOrgId` phải mang `-- sqllint:allow=tenant` kèm lý do.

| Luật | Mẫu bị cấm | Vì sao | Cách đúng (đã dùng ở đâu) | Cách kiểm tự động | Đo thật mẫu xấu (máy cục bộ) |
|---|---|---|---|---|---|
| R-Q01 | `OFFSET n` hoặc "bỏ n trang" | Chi phí tăng theo số trang; đọc rồi vứt n dòng | Phân trang theo khóa (cursor = bộ khóa dòng cuối), `LIMIT n+1` (Q02, Q03, Q09, Q13, Q14, Q15, Q17, Q20) | (S) regex `\boffset\b` trong SQL; trong mã: cấm tham số `page`/`offset` ở API danh sách; (K) không có nút `Limit` với `Offset` | AP1: OFFSET 150.000: 46.684 ms, 142611 trang; keyset: 0.018 ms, 23 trang |
| R-Q02 | N+1: lặp một truy vấn cho từng dòng của danh sách | Mỗi vòng thêm độ trễ mạng; đổi đường cong tải theo kích thước danh sách | Một câu JOIN/LATERAL cho cả trang (Q02 nối 4 bảng trong 1 câu) | (K) bộ đếm câu lệnh JDBC trong kiểm thử tích hợp: số câu mỗi API ≤ ngưỡng (NFR-GEO-009) và **bằng nhau** giữa trang 1 dòng và trang đầy | AP8 (mô phỏng N+1): 1 câu JOIN: 0.48 ms/yêu cầu. 33.511 tps; N+1 (21 câu): 3.85 ms. 4.152 tps (cục bộ; qua mạng thật mỗi vòng thêm 0.3 đến 1 ms) |
| R-Q03 | `SELECT *`, `RETURNING *`, thực thể "lấy mọi cột" | Kéo cột lớn (JSON, thân bài giảng, TOAST); thêm cột mới tự rò rỉ; cột nhạy cảm bị kéo theo | Liệt kê cột cần (mọi truy vấn trong catalogue) | (S) regex `select\s+\*`, `returning\s+\*`, `\.\*` | AP6: 56.504 byte kéo về so với 2.874 byte cần (gấp 19.7 lần) |
| R-Q04 | Danh sách không `LIMIT` hoặc `limit` do khách quyết định không kẹp | Bộ nhớ và thời gian không giới hạn | `LIMIT` cố định theo danh sách (20, 50 hoặc 100); máy chủ kẹp `limit` của khách | (S) mọi `SELECT` trên bảng lớn có `LIMIT` (khớp `large.tables` của `SqlLint`); (K) ca API `limit=100000` bị kẹp | — |
| R-Q05 | `count(*)` (hoặc `COUNT` để biết số trang) trên bảng lớn | Quét hết chỉ mục/bảng; chậm theo kích thước dữ liệu | `LIMIT n+1` để biết "còn trang sau"; bộ đếm phi chuẩn hóa (`assignment_progress`); `count(*)` chỉ trong phạm vi một bài/một lớp, có `lint-allow` | (S) regex `count\(\s*\*\s*\)` (cần `lint-allow` nêu phạm vi nhỏ); (K) nút `Aggregate` trên bảng lớn | AP2: count(*) trên event_log (≈ 45 triệu hàng sau thử tải pgbench): 2925.087 ms, 109473 trang; LIMIT n+1 có cận: 0.336 ms, 12 trang |
| R-Q06 | Sắp xếp không có chỉ mục phục vụ (nút `Sort` trên nhiều dòng, sắp xếp ra đĩa) | Tốn CPU/bộ nhớ, không giới hạn bằng `LIMIT` | `ORDER BY` trùng thứ tự chỉ mục (id DESC theo `(org_id, class_id, id DESC)`) | (K) `Sort Method: external` hoặc `Sort` có `rows` > 1000 trong kế hoạch; `Rows Removed by Filter` > 5000 | AP7: 3639.6 ms, 109602 trang (sắp xếp một partition ≈ 45 triệu hàng sau thử tải pgbench) |
| R-Q07 | `LIKE '%x%'`, `ILIKE` không có chỉ mục trigram/GIN | Quét tuần tự toàn bảng | Tìm không dấu bằng `search_tsv` GIN (Q11c, Q12), trigram (Q11b) | (S) regex `like\s+'%`, `ilike`; (K) `Seq Scan` + `Filter: ... ~~` | AP3: LIKE trên 50.000 bài giảng: 21.9 ms; tìm văn bản có chỉ mục: 3.304 ms |
| R-Q08 | Hàm bọc cột đã đánh chỉ mục (`lower(col) = x`, `date(col)`, `col::text`) và mẫu `(:x IS NULL OR cột = :x)` | Chỉ mục không dùng được; kế hoạch tổng quát xấu | Chuẩn hóa dữ liệu khi ghi (`name_key`, `term_key`); dựng SQL có điều kiện khi tham số có mặt | (S) regex hàm trên cột sau `WHERE/AND`, mẫu `IS NULL OR` quanh tham số | AP4: lower() có org_id: 0.540 ms, 21 trang; khóa đủ (org_id, class_id, name_key): 0.008 ms, 5 trang |
| R-Q09 | Truy vấn `event_log` không có cận `occurred_at` | Không cắt được partition; thăm mọi partition | Cận thời gian lấy từ UUIDv7 của bài (Q20) | (S) `event_log` không kèm `occurred_at` | Q20 so với Q20b (xem trên) |
| R-Q10 | Truy vấn bảng của trường không có `org_id` | Rò rỉ chéo trường; không dùng được chỉ mục dẫn đầu `org_id` | Mọi truy vấn có `org_id = :org` (từ thẻ); ngoại lệ có lý do ghi ở `02_multi_tenant.md` §2 | (S) khớp `tenant.tables` của `SqlLint`; (K) `Filter: org_id` hoặc `Seq Scan` trên bảng lớn | AP5: quên org_id (Seq Scan ~20.000 hàng): 0.511 ms, 247 trang; đủ org_id, class_id: 0.006 ms, 5 trang |
| R-Q11 | Chỉ mục của bảng thuộc trường không bắt đầu bằng `org_id` (ngoài ngoại lệ có ghi chú) | Mất vị trí cục bộ; truy vấn quên `org_id` không bị lộ ở kiểm kế hoạch | `org_id` đứng đầu | (S) quét `pg_index` trong kiểm thử lược đồ: cột đầu của chỉ mục trên bảng thuộc trường phải là `org_id` trừ danh sách ngoại lệ | — |
| R-Q12 | JSON trong `WHERE`/`ORDER BY`/`JOIN`; JSON thay cho cột cần lọc/sắp xếp | Không chỉ mục, không ràng buộc kiểu | Cột thật cho thứ cần lọc; JSON chỉ cho khối đọc nguyên (`spec`, `coords`, `steps`, `payload`) kèm `schema_version` | (S) regex `->>`, `@>`, `jsonb_path` trong điều kiện; quét lược đồ: cột `jsonb` phải nằm trong danh sách cho phép | — |
| R-Q13 | Giao dịch giữ lâu: gọi mạng (AI, Redis) bên trong giao dịch; giao dịch qua thao tác người dùng; `idle in transaction` | Giữ kết nối, giữ khóa, làm cạn nhóm | Giao dịch ≤ vài câu lệnh, p99 ≤ 100 ms; AI/Redis ngoài giao dịch | (S) ArchUnit: lớp gọi cổng AI/Redis không được nằm trong phương thức `@Transactional`; (K) `idle_in_transaction_session_timeout=10s` và giám sát | — |
| R-Q14 | Đọc `solution`/`verification_report` ngoài hàm cổng | Rò rỉ lời giải (BR-01) | Chỉ `fn_get_solution*`; vai `geo_app` không có quyền | (S) cấm tham chiếu tên bảng trong mã ứng dụng (`forbidden.app.tables`); (K) `bench/06_isolation_test.sql` A1…A5 trong CI | `permission denied` (PASS) |
| R-Q15 | Ghi từng dòng trong vòng lặp (n lệnh `INSERT`) | n vòng mạng, n lần ghi WAL | Một câu `INSERT … SELECT unnest(…)` hoặc COPY (Q07) | (K) bộ đếm câu lệnh: số `INSERT` mỗi lô ≤ 2 | — |
| R-Q16 | `UPDATE` tiến độ bằng "đọc rồi ghi" (đọc giá trị rồi ghi lại) | Mất cập nhật khi đồng thời | UPSERT lũy đẳng (`GREATEST`, `COALESCE`, `+`) trong một câu (Q08) | (S) cấm mẫu `SELECT … FOR UPDATE` ngoài danh sách (chỉ `fn_set_release`) | — |
| R-Q17 | Ghép chuỗi tham số vào SQL (nối chuỗi) | Chèn SQL; kế hoạch không dùng lại | `PreparedStatement` với `?` | (S) cấm nối chuỗi trong SQL; cấm `Statement` thường | — |
| R-Q18 | `Seq Scan` trên bảng có > 10.000 hàng ở truy vấn nóng | Chi phí theo kích thước dữ liệu | Chỉ mục phục vụ, chứng minh bằng kế hoạch | (K) `lint_queries.py` đọc kế hoạch, tra `reltuples` (NFR-GEO-042) | — |


## 6. Mã trạng thái của hàm và mã lỗi API

Các hàm SECURITY DEFINER (`03_schema.sql` §9) trả **mã trạng thái** thay cho "0 hàng / NULL / ngoại lệ" để ứng dụng phân biệt lý do mà không cần truy vấn thứ hai (RV-21). Ánh xạ sang mã lỗi API của `app/docs/CONFIG.md` (bảng mã lỗi, D-GEO-143):

| Mã trạng thái của hàm | Hàm trả | Mã lỗi API (HTTP) | Ghi chú |
|---|---|---|---|
| `OK` | mọi hàm | 200 / 201 | |
| `DUPLICATE` | `fn_record_durable`, `fn_record_qa`, `fn_create_assignment` (cùng id) | 200 như lần đầu | gửi lại cùng `request_id` hoặc cùng id bài giao (RV-18) |
| `NOT_FOUND` | `fn_get_solution`, `fn_solution_gate`, `fn_get_hint`, `fn_record_*`, `fn_set_release`, `fn_create_assignment`, `fn_create_self_assignment`, `fn_student_login`; `fn_get_solution_teacher` trả 0 hàng | `NOT_FOUND` (404) | tài nguyên của trường khác, lớp mình không thuộc, bài tự học của người khác; giáo viên xem lời giải đề tự nhập (D-GEO-175) |
| `LOCKED` | `fn_get_solution`, `fn_solution_gate`, `fn_record_durable('unlock')` | `FORBIDDEN` (403) | bài của lớp mình còn khóa, bài tự học còn khóa hoặc kế thừa khóa (BR-GEO-251); không kèm nội dung |
| `FORBIDDEN` | `fn_set_release`, `fn_create_assignment` | `FORBIDDEN` (403) | có bài/lớp trong trường nhưng người gọi không phải giáo viên của lớp |
| `HINT_DISABLED`, `QA_DISABLED` | `fn_get_hint`, `fn_record_qa` | `FORBIDDEN` (403) | giáo viên tắt trợ giúp khi giao (D-GEO-053) |
| `LEVEL_ORDER` | `fn_get_hint` | `FORBIDDEN` (403) | xin Gợi ý 2 trước Gợi ý 1 (D-GEO-108); BA có thể chọn `VALIDATION_FAILED` nếu AC muốn, ghi ở bảng thông điệp |
| `CONFLICT_ALREADY_OPEN` | `fn_set_release` | `CONFLICT` (409), MSG-GEO-48 | kèm `release_rev`, `release_at` hiện hành (D-GEO-143, RV-28) |
| `CONFLICT_REV` | `fn_set_release` | `CONFLICT` (409), MSG-GEO-49 | hai lần đổi chồng nhau; kèm rev hiện hành |
| `CONFLICT_DUPLICATE` | `fn_create_assignment` | `CONFLICT` (409), MSG-GEO-42 | cùng `problem` cho cùng lớp, bài chưa lưu trữ (D-GEO-161, chỉ mục duy nhất một phần) |
| `VALIDATION_PAST` | `fn_set_release`, `fn_create_assignment`, `fn_issue_teacher_code` | `VALIDATION_FAILED` (400), MSG-GEO-43 | giờ mở không ở tương lai |
| `VALIDATION_KIND` | `fn_record_batch`, `fn_record_durable`, `fn_get_hint` | `VALIDATION_FAILED` (400) | lô mức A mang sự kiện mức B, thiếu `request_id`, mức gợi ý ngoài {1, 2} |
| `NO_SOLUTION`, `NO_HINT` | `fn_get_solution`, `fn_get_hint` | `NOT_FOUND` (404) | dữ liệu nội dung thiếu (không xảy ra với đề kho đã nạp đủ) |
| `RATE_LIMITED` | `fn_teacher_login` | `RATE_LIMITED` (429) + `Retry-After`, MSG-GEO-59 | 10 lần sai/15 phút theo IP (D-GEO-152, 173) |
| `INVALID_CODE` | `fn_teacher_login` | `UNAUTHENTICATED` (401), MSG-GEO-12 | sai, thu hồi, hết hạn: một thông báo chung |

Ghi chú: trạng thái "Đã mở" của bài giao không bao giờ được đệm thay cho hàm (D-GEO-157); điểm cuối trạng thái (Q04b) không trả lời giải và không trả quyết định.

