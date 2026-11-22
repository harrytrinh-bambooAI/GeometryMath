-- Kiểm ở quy mô lớn hơn cho các bảng/chỉ mục mới của lượt sửa sau RV (mọi thay đổi ROLLBACK).
-- (1) Q24: auth_failure 300.000 lần sai (dò từ 3.000 IP trong 24 giờ) => vẫn dùng chỉ mục (ip_key, at) / (user_id, at), dừng ở 10.
-- (2) Q21 trước/sau (RV-19): lịch sử hỏi đáp của một học sinh khi KHÔNG có qa_turn_user_idx so với có.
-- (3) Q16: 40.000 đề tự nhập cùng chữ ký với một đề kho (nhiều học sinh dán cùng đề) => tra chữ ký đề kho vẫn theo org_id nền tảng.
\i bench/02_params.sql
BEGIN;
INSERT INTO geo.auth_failure (at, ip_key, user_id)
SELECT now() - random() * interval '24 hours', digest('ip-' || (g % 3000), 'sha256'), NULL FROM generate_series(1, 300000) g;
ANALYZE geo.auth_failure;
\echo '===== (1) Q24 với 300.000 lần sai'
EXPLAIN (ANALYZE, BUFFERS)
SELECT (SELECT count(*) FROM (SELECT 1 FROM geo.auth_failure f WHERE f.ip_key = digest('ip-7', 'sha256') AND f.at > now() - interval '15 minutes' LIMIT 10) x) AS fail_ip,
       (SELECT count(*) FROM (SELECT 1 FROM geo.auth_failure f WHERE f.user_id = t.user_id AND f.at > now() - interval '15 minutes' LIMIT 10) y) AS fail_acct,
       t.id, t.user_id FROM geo.teacher_access_code t WHERE t.selector = :'selector';
\echo '===== (2a) Q21 KHÔNG có qa_turn_user_idx (như lược đồ trước, RV-19 đo 90 ms)'
DROP INDEX geo.qa_turn_user_idx;
EXPLAIN (ANALYZE, BUFFERS)
SELECT q.id, q.assignment_id, q.asked_at, q.question, q.reply_kind, q.reply_text FROM geo.qa_turn q
WHERE q.org_id = :'qa_org' AND q.user_id = :'qa_student' AND q.id < 9223372036854775807 ORDER BY q.id DESC LIMIT 21;
ROLLBACK;
\echo '===== (2b) Q21 CÓ qa_turn_user_idx'
EXPLAIN (ANALYZE, BUFFERS)
SELECT q.id, q.assignment_id, q.asked_at, q.question, q.reply_kind, q.reply_text FROM geo.qa_turn q
WHERE q.org_id = :'qa_org' AND q.user_id = :'qa_student' AND q.id < 9223372036854775807 ORDER BY q.id DESC LIMIT 21;
BEGIN;
INSERT INTO geo.problem_signature (org_id, problem_version_id, sig_version, template_id, params_norm, asked_quantity, signature)
SELECT pv.org_id, pv.id, 2, 1, '{}', 'x', (SELECT s.signature FROM geo.problem_signature s WHERE s.org_id = :'platform' ORDER BY s.problem_version_id LIMIT 1)
FROM geo.problem_version pv WHERE pv.org_id <> :'platform';
INSERT INTO geo.problem_signature (org_id, problem_version_id, sig_version, template_id, params_norm, asked_quantity, signature)
SELECT pv.org_id, pv.id, 3, 1, '{}', 'x', (SELECT s.signature FROM geo.problem_signature s WHERE s.org_id = :'platform' ORDER BY s.problem_version_id LIMIT 1)
FROM geo.problem_version pv WHERE pv.org_id <> :'platform';
ANALYZE geo.problem_signature;
\echo '===== (3) Q16 khi hàng chục nghìn đề tự nhập trùng chữ ký (sig_version khác) với đề kho'
EXPLAIN (ANALYZE, BUFFERS)
SELECT cat.problem_version_id FROM geo.problem_signature cat
WHERE cat.org_id = :'platform' AND cat.signature = (SELECT s.signature FROM geo.problem_signature s WHERE s.org_id = :'platform' ORDER BY s.problem_version_id LIMIT 1) AND cat.sig_version = 1;
SELECT count(*) AS signature_rows FROM geo.problem_signature;
ROLLBACK;
