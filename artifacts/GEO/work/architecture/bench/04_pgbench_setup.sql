-- Bảng mẫu tham số cho pgbench (lược đồ riêng "bench", không phải một phần của sản phẩm).
DROP SCHEMA IF EXISTS bench CASCADE;
CREATE SCHEMA bench;
CREATE TABLE bench.pairs AS
SELECT row_number() OVER (ORDER BY cm.user_id)::int AS id,
       cm.org_id, cm.class_id, c.teacher_id, cm.user_id AS student_id,
       x.id AS assignment_id, x.rel_id AS released_assignment_id
FROM geo.class_member cm
JOIN geo.class c ON c.org_id = cm.org_id AND c.id = cm.class_id
CROSS JOIN LATERAL (
   SELECT (SELECT a.id FROM geo.assignment a WHERE a.org_id = cm.org_id AND a.class_id = cm.class_id AND a.kind = 'class' ORDER BY a.id DESC OFFSET (abs(hashtext(cm.user_id::text)) % 4) LIMIT 1) AS id,
          (SELECT a.id FROM geo.assignment a WHERE a.org_id = cm.org_id AND a.class_id = cm.class_id AND a.kind = 'class' AND a.release_at < now() - interval '1 hour' ORDER BY a.id DESC OFFSET (abs(hashtext(cm.user_id::text)) % 3) LIMIT 1) AS rel_id) x
WHERE x.id IS NOT NULL AND x.rel_id IS NOT NULL;
ALTER TABLE bench.pairs ADD PRIMARY KEY (id);
CREATE TABLE bench.terms AS SELECT row_number() OVER (ORDER BY term_key)::int AS id, term_key FROM geo.keyword;
ALTER TABLE bench.terms ADD PRIMARY KEY (id);
CREATE TABLE bench.tcls AS SELECT row_number() OVER (ORDER BY a.id)::int AS id, c.org_id, c.teacher_id, c.id AS class_id, a.id AS assignment_id
FROM geo.class c JOIN LATERAL (SELECT id FROM geo.assignment WHERE org_id = c.org_id AND class_id = c.id AND kind = 'class' ORDER BY id DESC LIMIT 1) a ON true;
ALTER TABLE bench.tcls ADD PRIMARY KEY (id);
GRANT USAGE ON SCHEMA bench TO geo_app; GRANT SELECT ON ALL TABLES IN SCHEMA bench TO geo_app;
-- vai đo tải: quyền của ứng dụng + cổng AI (p_qa gọi fn_record_qa của geo_ai). Không phải vai sản phẩm.
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'geo_bench') THEN CREATE ROLE geo_bench NOLOGIN; END IF; END $$;
GRANT geo_app, geo_ai TO geo_bench;
ANALYZE bench.pairs; ANALYZE bench.terms; ANALYZE bench.tcls;
SELECT (SELECT count(*) FROM bench.pairs) AS pairs, (SELECT count(*) FROM bench.terms) AS terms, (SELECT count(*) FROM bench.tcls) AS tcls;
