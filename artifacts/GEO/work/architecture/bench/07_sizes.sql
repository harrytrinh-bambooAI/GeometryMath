-- Kích thước bảng/chỉ mục, số chỉ mục mỗi bảng, và chỉ mục chưa được dùng (idx_scan = 0 từ lần pg_stat_reset gần nhất).
\echo '### Kích thước theo bảng (partition gộp về bảng cha)'
WITH rel AS (
  SELECT c.oid, c.relname, c.relkind, c.relispartition, coalesce(pi.inhparent, c.oid) AS root
  FROM pg_class c LEFT JOIN pg_inherits pi ON pi.inhrelid = c.oid
  WHERE c.relnamespace = 'geo'::regnamespace AND c.relkind IN ('r', 'p'))
SELECT r.root::regclass AS tbl,
       sum(CASE WHEN r.relkind = 'r' THEN greatest(c.reltuples, 0) ELSE 0 END)::bigint AS rows_est,
       pg_size_pretty(sum(pg_table_size(r.oid)) FILTER (WHERE r.relkind = 'r')) AS table_size,
       pg_size_pretty(sum(pg_indexes_size(r.oid)) FILTER (WHERE r.relkind = 'r')) AS index_size,
       (SELECT count(*) FROM pg_indexes i WHERE i.schemaname = 'geo' AND i.tablename = (SELECT relname FROM pg_class WHERE oid = r.root)) AS n_index_on_parent
FROM rel r JOIN pg_class c ON c.oid = r.oid
GROUP BY r.root HAVING r.root::regclass::text NOT LIKE '%_default'
ORDER BY sum(pg_total_relation_size(r.oid)) FILTER (WHERE r.relkind = 'r') DESC NULLS LAST;
\echo '### Chỉ mục (không tính partition con): kích thước và số lần dùng'
SELECT s.relname AS tbl, s.indexrelname AS idx, pg_size_pretty(pg_relation_size(s.indexrelid)) AS size, s.idx_scan
FROM pg_stat_user_indexes s
WHERE s.schemaname = 'geo' AND s.relname !~ '_y20[0-9]{2}m[0-9]{2}$' AND s.relname !~ '_default$'
ORDER BY s.relname, pg_relation_size(s.indexrelid) DESC;
\echo '### Chỉ mục trên partition event_log (một partition đại diện: tháng có nhiều dữ liệu nhất)'
SELECT c.relname AS partition, pg_size_pretty(pg_table_size(c.oid)) AS table_size, pg_size_pretty(pg_indexes_size(c.oid)) AS index_size, c.reltuples::bigint AS rows_est
FROM pg_class c JOIN pg_inherits i ON i.inhrelid = c.oid WHERE i.inhparent = 'geo.event_log'::regclass ORDER BY c.relname;
\echo '### HOT update trên assignment_progress'
SELECT n_tup_upd, n_tup_hot_upd, round(100.0 * n_tup_hot_upd / nullif(n_tup_upd, 0), 1) AS pct_hot FROM pg_stat_user_tables WHERE relname = 'assignment_progress';
