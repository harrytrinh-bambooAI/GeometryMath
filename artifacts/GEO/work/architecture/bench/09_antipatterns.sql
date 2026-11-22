-- Đo thật chi phí của các "mẫu bị cấm" trên dữ liệu thử (so với cách đúng). Chạy bằng vai geo_app; chỉ đọc.
\i bench/02_params.sql
SET ROLE geo_app;
\timing off
\echo '### AP1 OFFSET sâu so với keyset (qa_turn ~170.000 hàng, trang 20, trang thứ 7.500)'
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF, SUMMARY ON) SELECT q.id, q.user_id, q.asked_at FROM geo.qa_turn q ORDER BY q.id DESC OFFSET 150000 LIMIT 20;
SELECT q.id AS k FROM geo.qa_turn q ORDER BY q.id DESC OFFSET 150000 LIMIT 1 \gset
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF, SUMMARY ON) SELECT q.id, q.user_id, q.asked_at FROM geo.qa_turn q WHERE q.id < :k ORDER BY q.id DESC LIMIT 20;
\echo '### AP2 count(*) trên bảng lớn (event_log ~2 triệu hàng) so với LIMIT n+1'
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF, SUMMARY ON) SELECT count(*) FROM geo.event_log;
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF, SUMMARY ON) SELECT e.id FROM geo.event_log e WHERE e.org_id = :'ev_org' AND e.assignment_id = :'ev_assignment' AND e.user_id = :'ev_student' AND e.occurred_at >= :'lo_ts' AND e.occurred_at < :'hi_ts' ORDER BY e.occurred_at DESC, e.id DESC LIMIT 51;
\echo '### AP3 LIKE ''%x%'': đo ở quy mô 50.000 bài giảng nằm trong bench/03_scale_checks.sql (mục AP3), vì bảng thử chỉ có 300 bài'
\echo '### AP4 hàm bọc cột: lower(name_key) = x (chỉ có org_id dẫn đầu giúp) so với name_key = x kèm org_id, class_id (class_member ~20.000 hàng)'
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF, SUMMARY ON) SELECT cm.user_id FROM geo.class_member cm WHERE cm.org_id = :'org' AND lower(cm.name_key) = :'name_key';
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF, SUMMARY ON) SELECT cm.user_id FROM geo.class_member cm WHERE cm.org_id = :'org' AND cm.class_id = :'class' AND cm.name_key = :'name_key';
\echo '### AP5 quên org_id: tra theo name_key trên toàn bảng so với có org_id, class_id'
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF, SUMMARY ON) SELECT cm.user_id FROM geo.class_member cm WHERE cm.name_key = :'name_key';
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF, SUMMARY ON) SELECT cm.user_id FROM geo.class_member cm WHERE cm.org_id = :'org' AND cm.class_id = :'class' AND cm.name_key = :'name_key';
\echo '### AP6 SELECT * kéo cột lớn: số byte kéo về cho 50 hàng bài giảng (lecture.body) so với chỉ cột cần'
SELECT sum(pg_column_size(l.*)) AS bytes_select_star, sum(pg_column_size(l.id) + pg_column_size(l.title) + pg_column_size(l.source_kind)) AS bytes_needed,
       round(sum(pg_column_size(l.*))::numeric / sum(pg_column_size(l.id) + pg_column_size(l.title) + pg_column_size(l.source_kind)), 1) AS ratio
FROM (SELECT * FROM geo.lecture WHERE org_id = :'platform' ORDER BY id LIMIT 50) l;
SELECT sum(pg_column_size(sb.*)) AS bytes_select_star_scene_build_20_rows, sum(pg_column_size(sb.id) + pg_column_size(sb.coord_source)) AS bytes_needed
FROM (SELECT * FROM geo.scene_build WHERE org_id = :'platform' ORDER BY problem_version_id LIMIT 20) sb;
\echo '### AP7 sắp xếp không có chỉ mục: ORDER BY occurred_at trên một partition lớn không có chỉ mục theo thời gian'
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF, SUMMARY ON) SELECT e.id FROM geo.event_log e WHERE e.occurred_at >= date_trunc('month', now()) ORDER BY e.occurred_at DESC LIMIT 50;
