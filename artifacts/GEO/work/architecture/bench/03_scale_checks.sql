-- Kiểm tra "ở quy mô lớn hơn" các bảng nhỏ trong dữ liệu thử (kho đề, bài giảng, từ khóa, cấp phép): trong MỘT giao dịch,
-- chèn thêm dữ liệu giả, chạy lại các truy vấn tương ứng bằng EXPLAIN (ANALYZE, BUFFERS) với vai geo_app, rồi ROLLBACK.
-- Mục đích: chứng minh bộ chỉ mục được dùng khi bảng lớn (kế hoạch ở bảng nhỏ có thể là Seq Scan vì rẻ hơn).
\set ON_ERROR_STOP on
\i bench/02_params.sql
BEGIN;
SET LOCAL synchronous_commit = off;
-- 50.000 bài giảng, 150.000 từ khóa
INSERT INTO geo.lecture (id, org_id, subject_id, slug, title, body, source_kind, approval_status, source_ref)
SELECT geo.uuid_v7(), :'platform', 1, 'scale-' || n, 'Bài giảng quy mô ' || n || ' ' || (ARRAY['thiết diện','góc nhị diện','khoảng cách','thể tích chóp','hình chiếu'])[1 + n % 5],
       'Nội dung ' || n || ' ' || (ARRAY['mặt phẳng cắt hình chóp','đường vuông góc chung','góc giữa hai mặt phẳng','khối lăng trụ','hình hộp chữ nhật'])[1 + n % 5] || ' ' || repeat('văn bản đệm hình học ', 20),
       'teacher_authored', 'teacher_approved', 'Giáo án quy mô'
FROM generate_series(1, 50000) n;
INSERT INTO geo.keyword (org_id, term_key, term, lecture_id)
SELECT l.org_id, 'tk ' || substr(md5(l.id::text || k), 1, 12), 'Từ khóa ' || k, l.id
FROM geo.lecture l CROSS JOIN generate_series(1, 3) k WHERE l.slug LIKE 'scale-%';
-- 20.000 đề kho đề + version
CREATE TEMP TABLE sc AS SELECT n, geo.uuid_v7() AS id, geo.uuid_v7() AS vid FROM generate_series(1, 20000) n;
INSERT INTO geo.problem (id, org_id, subject_id, source, level, solid_kind, topic_id, title)
SELECT id, :'platform', 1, 'catalog', (ARRAY['thcs','thpt'])[1 + n % 2]::geo.edu_level, (ARRAY['pyramid','prism','box','cube','cone','cylinder','sphere'])[1 + n % 7]::geo.solid_kind, 1 + n % 5, 'Đề quy mô ' || n FROM sc;
INSERT INTO geo.problem_version (id, org_id, problem_id, version, statement, statement_hash, verify_status)
SELECT vid, :'platform', id, 1, 'đề ' || n, digest('sc' || n, 'sha256'), 'verified' FROM sc;
UPDATE geo.problem p SET current_version_id = sc.vid FROM sc WHERE p.id = sc.id;
-- 50.000 cấp phép liên trường
INSERT INTO geo.share_grant (id, org_id, grantee_org_id, resource_kind, resource_id, purpose, status, requested_by, grantor_approved_at, grantee_approved_at, valid_from, valid_until)
SELECT geo.uuid_v7(), o1.id, :'org', 'lecture', geo.uuid_v7(), 'Cấp phép quy mô lớn thử nghiệm', 'approved',
       (SELECT id FROM geo.app_user WHERE org_id = o1.id LIMIT 1), now(), now(), now() - interval '1 day', now() + interval '30 days'
FROM (SELECT id FROM geo.org WHERE kind = 'school' AND id <> :'org') o1 CROSS JOIN generate_series(1, 1000) g;
ANALYZE geo.lecture; ANALYZE geo.keyword; ANALYZE geo.problem; ANALYZE geo.problem_version; ANALYZE geo.share_grant;
SET LOCAL ROLE geo_app;
\set explain 'EXPLAIN (ANALYZE, BUFFERS)'
\echo '===== Q11b fuzzy keyword @ 150k từ khóa'
\i bench/queries/Q11b_keyword_fuzzy.sql
\echo '===== Q11 keyword click @ 150k'
\i bench/queries/Q11_keyword_click.sql
\echo '===== Q12 qa retrieval @ 50k bài giảng (cụm hiếm: thiet dien)'
\set q 'thiet dien chop'
\echo '--- v1: xếp hạng mọi bài khớp'
\i bench/queries/history/Q12_v1_rank_all_matches.sql
\echo '--- v2: tập ứng viên giới hạn 200'
\i bench/queries/Q12_qa_source_retrieval.sql
\echo '===== Q13 catalog @ 20k đề kho (lọc cấp + loại hình)'
\i bench/queries/Q13_catalog_list.sql
\echo '===== Q13b catalog @ 20k đề kho (chỉ lọc cấp)'
\i bench/queries/Q13b_catalog_list_level_only.sql
\echo '===== Q18 share_grant @ 50k cấp phép'
\i bench/queries/Q18_share_grant_check.sql
\echo '===== AP3 LIKE %x% @ 50k bài giảng'
\echo '--- xấu: LIKE %x% trên thân bài giảng (không chỉ mục dùng được)'
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF, SUMMARY ON) SELECT l.id FROM geo.lecture l WHERE l.org_id = :'platform' AND l.body LIKE '%khoa-khong-ton-tai-xyz%' LIMIT 10;
\echo '--- đúng: tìm kiếm văn bản (GIN)'
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF, SUMMARY ON) SELECT l.id FROM geo.lecture l WHERE l.org_id = :'platform' AND l.search_tsv @@ websearch_to_tsquery('simple', 'khoa-khong-ton-tai-xyz') LIMIT 10;
ROLLBACK;
-- ROLLBACK để lại mục chỉ mục chết (GIN, btree): dọn ngay để các phép đo sau không bị nhiễu
VACUUM (ANALYZE) geo.lecture, geo.keyword, geo.problem, geo.problem_version, geo.share_grant;
