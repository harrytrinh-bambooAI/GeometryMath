-- Chi phí ghi của chỉ mục trên bảng sự kiện: 1.000.000 hàng vào (A) bảng cấu hình đề xuất: 1 chỉ mục btree (org, asg, user, time, id)
-- và (B) cấu hình "phòng thủ thái quá": khóa chính (id, occurred_at) + 3 chỉ mục thêm. Cùng cột, cùng dữ liệu, một phiên, commit không đồng bộ.
\set ON_ERROR_STOP on
SET search_path = geo, public;
DROP SCHEMA IF EXISTS bench_ev CASCADE; CREATE SCHEMA bench_ev;
CREATE TABLE bench_ev.a (id bigint GENERATED ALWAYS AS IDENTITY, occurred_at timestamptz NOT NULL, org_id uuid NOT NULL, assignment_id uuid NOT NULL, user_id uuid NOT NULL, kind geo.event_kind NOT NULL, arg smallint);
CREATE INDEX ON bench_ev.a (org_id, assignment_id, user_id, occurred_at DESC, id DESC);
CREATE TABLE bench_ev.c (LIKE bench_ev.a INCLUDING DEFAULTS INCLUDING IDENTITY);
CREATE TABLE bench_ev.d (LIKE bench_ev.a INCLUDING DEFAULTS INCLUDING IDENTITY);
CREATE INDEX ON bench_ev.d (assignment_id, user_id, occurred_at DESC);
CREATE TABLE bench_ev.e (LIKE bench_ev.a INCLUDING DEFAULTS INCLUDING IDENTITY);
CREATE INDEX ON bench_ev.e (assignment_id, user_id, occurred_at DESC, id DESC);
CREATE TABLE bench_ev.b (LIKE bench_ev.a INCLUDING DEFAULTS INCLUDING IDENTITY);
CREATE TABLE bench_ev.j (LIKE bench_ev.a INCLUDING DEFAULTS INCLUDING IDENTITY); ALTER TABLE bench_ev.j ADD COLUMN payload jsonb;
CREATE INDEX ON bench_ev.j (org_id, assignment_id, user_id, occurred_at DESC, id DESC);
ALTER TABLE bench_ev.b ADD PRIMARY KEY (id, occurred_at);
CREATE INDEX ON bench_ev.b (org_id, assignment_id, user_id, occurred_at DESC, id DESC);
CREATE INDEX ON bench_ev.b (org_id, occurred_at);
CREATE INDEX ON bench_ev.b (user_id, occurred_at);
CREATE INDEX ON bench_ev.b (assignment_id, kind, occurred_at);
SET synchronous_commit = off;
CREATE TEMP TABLE src AS
  SELECT now() - random() * interval '30 days' AS t, p.org_id, p.assignment_id AS asg, p.student_id AS usr,
         (ARRAY['tool','tool','tool','hint','ask','open'])[1 + (g % 6)]::geo.event_kind AS k
  FROM generate_series(1, 1000000) g JOIN bench.pairs p ON p.id = 1 + ((g::bigint * 7919) % 19900);
CREATE INDEX ON src (t);
ANALYZE src;
\timing on
\echo '=== C: không chỉ mục (chỉ nối thêm)'
INSERT INTO bench_ev.c (occurred_at, org_id, assignment_id, user_id, kind, arg) SELECT t, org_id, asg, usr, k, CASE WHEN k = 'tool' THEN 1 + (extract(second FROM t)::int % 7) WHEN k = 'hint' THEN 1 END::smallint FROM src ORDER BY t;
\echo '=== A: cấu hình đề xuất (1 chỉ mục, không JSON)'
INSERT INTO bench_ev.a (occurred_at, org_id, assignment_id, user_id, kind, arg) SELECT t, org_id, asg, usr, k, CASE WHEN k = 'tool' THEN 1 + (extract(second FROM t)::int % 7) WHEN k = 'hint' THEN 1 END::smallint FROM src ORDER BY t;
\echo '=== D: chỉ mục gọn (assignment_id, user_id, occurred_at), không org_id, không id'
INSERT INTO bench_ev.d (occurred_at, org_id, assignment_id, user_id, kind, arg) SELECT t, org_id, asg, usr, k, CASE WHEN k = 'tool' THEN 1 + (extract(second FROM t)::int % 7) WHEN k = 'hint' THEN 1 END::smallint FROM src ORDER BY t;
\echo '=== E: assignment_id, user_id, occurred_at DESC, id DESC (không org_id trong khóa chỉ mục; có id làm khóa cursor)'
INSERT INTO bench_ev.e (occurred_at, org_id, assignment_id, user_id, kind, arg) SELECT t, org_id, asg, usr, k, CASE WHEN k = 'tool' THEN 1 + (extract(second FROM t)::int % 7) WHEN k = 'hint' THEN 1 END::smallint FROM src ORDER BY t;
\echo '=== J: như A nhưng thêm cột payload jsonb ~40 byte'
INSERT INTO bench_ev.j (occurred_at, org_id, assignment_id, user_id, kind, arg, payload) SELECT t, org_id, asg, usr, k, CASE WHEN k = 'tool' THEN 1 + (extract(second FROM t)::int % 7) WHEN k = 'hint' THEN 1 END::smallint, '{"schema_version":1,"tool":"rotate"}'::jsonb FROM src ORDER BY t;
\echo '=== B: khóa chính + 3 chỉ mục thêm (4 chỉ mục + khóa chính)'
INSERT INTO bench_ev.b (occurred_at, org_id, assignment_id, user_id, kind, arg) SELECT t, org_id, asg, usr, k, CASE WHEN k = 'tool' THEN 1 + (extract(second FROM t)::int % 7) WHEN k = 'hint' THEN 1 END::smallint FROM src ORDER BY t;
\timing off
SELECT cfg, pg_size_pretty(ts) AS table_size, pg_size_pretty(isz) AS index_size, n_idx, round((ts + isz)::numeric / 1000000) AS total_bytes_per_row FROM (
  SELECT 'C không chỉ mục' AS cfg, pg_table_size('bench_ev.c') ts, pg_indexes_size('bench_ev.c') isz, 0 AS n_idx
  UNION ALL SELECT 'A đề xuất (1 chỉ mục)', pg_table_size('bench_ev.a'), pg_indexes_size('bench_ev.a'), 1
  UNION ALL SELECT 'D chỉ mục gọn (không org_id, không id)', pg_table_size('bench_ev.d'), pg_indexes_size('bench_ev.d'), 1
  UNION ALL SELECT 'E khóa (assignment, user, time, id), không org_id', pg_table_size('bench_ev.e'), pg_indexes_size('bench_ev.e'), 1
  UNION ALL SELECT 'J A + payload jsonb', pg_table_size('bench_ev.j'), pg_indexes_size('bench_ev.j'), 1
  UNION ALL SELECT 'B PK + 4 chỉ mục', pg_table_size('bench_ev.b'), pg_indexes_size('bench_ev.b'), 5) x;
DROP SCHEMA bench_ev CASCADE;
