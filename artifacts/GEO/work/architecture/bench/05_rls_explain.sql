\i bench/02_params.sql
SET ROLE geo_app;
BEGIN;
SELECT set_config('app.org_id', :'org', true);
\set explain 'EXPLAIN (ANALYZE, BUFFERS)'
\i bench/queries/Q09_class_tracking.sql
ROLLBACK;
