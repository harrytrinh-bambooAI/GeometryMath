\i bench/02_params.sql
SET ROLE geo_app;
BEGIN;
SELECT set_config('app.org_id', :'org', true);
\echo 'Đúng trường: số bài của lớp (mong đợi > 0)'
SELECT count(*) FROM geo.assignment WHERE class_id = :'class';
SELECT set_config('app.org_id', (SELECT id::text FROM geo.org WHERE kind = 'school' AND id <> :'org' LIMIT 1), true);
\echo 'Sai trường (dù viết sai lọc org_id hay quên lọc): số bài của lớp (mong đợi 0)'
SELECT count(*) FROM geo.assignment WHERE class_id = :'class';
SELECT count(*) FROM geo.assignment_progress WHERE assignment_id = :'assignment';
SELECT count(*) FROM geo.event_log WHERE assignment_id = :'assignment';
\echo 'Sai trường nhưng nội dung nền tảng vẫn đọc được (kho đề, mong đợi > 0)'
SELECT count(*) FROM geo.problem WHERE source = 'catalog';
\echo 'Không đặt app.org_id (đặt rỗng): mong đợi lỗi hoặc 0 hàng'
SELECT set_config('app.org_id', '', true);
SELECT count(*) FROM geo.assignment WHERE class_id = :'class';
ROLLBACK;
