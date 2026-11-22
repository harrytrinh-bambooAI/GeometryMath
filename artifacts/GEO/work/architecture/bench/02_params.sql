-- Chọn tham số đại diện từ dữ liệu thử (một lớp lớn, một học sinh, một bài đã mở, một bài chưa đặt giờ...).
-- Dùng \gset: sau file này các biến :'platform', :'org', :'class', ... đã có giá trị.
SELECT '00000000-0000-7000-8000-000000000001' AS platform, 'ffffffff-ffff-ffff-ffff-ffffffffffff' AS cursor_max \gset
SELECT c.id AS class, c.org_id AS org, c.teacher_id AS teacher, c.join_code AS join_code
FROM geo.class c WHERE c.id = (SELECT class_id FROM geo.class_member GROUP BY class_id ORDER BY count(*) DESC, class_id LIMIT 1) \gset
SELECT cm.user_id AS student, cm.name_key AS name_key FROM geo.class_member cm
WHERE cm.org_id = :'org' AND cm.class_id = :'class' ORDER BY cm.user_id OFFSET 7 LIMIT 1 \gset
-- bài đã mở giờ lời giải, có nhiều tiến độ: bài mới nhất của lớp lớn đã tới giờ mở
SELECT a.id AS assignment, a.problem_version_id AS pv, a.release_rev AS rev FROM geo.assignment a
WHERE a.org_id = :'org' AND a.class_id = :'class' AND a.release_at < now() ORDER BY a.id DESC LIMIT 1 \gset
SELECT a.id AS assignment_locked FROM geo.assignment a
WHERE a.org_id = :'org' AND a.class_id = :'class' AND (a.release_at IS NULL OR a.release_at > now()) ORDER BY a.id DESC LIMIT 1 \gset
SELECT a.id AS assignment_self, a.owner_user_id AS self_student, a.org_id AS self_org FROM geo.assignment a WHERE a.kind = 'self' ORDER BY a.id LIMIT 1 \gset
SELECT encode(pv.statement_hash, 'hex') AS hash FROM geo.problem_version pv WHERE pv.id = :'pv' \gset
SELECT k.term_key AS term_key FROM geo.keyword k WHERE k.org_id = :'platform' ORDER BY k.term_key OFFSET 11 LIMIT 1 \gset
SELECT 'duong vuong goc' AS q, 'duong vuon goc' AS q_fuzzy, 'goc nhi dien' AS q_phrase \gset
-- học sinh có nhiều sự kiện trong một bài (cho dòng thời gian)
SELECT e.user_id AS ev_student, e.assignment_id AS ev_assignment, e.org_id AS ev_org
FROM geo.event_log e WHERE e.occurred_at > now() - interval '30 days' LIMIT 1 \gset
SELECT ap.user_id AS pr_student, ap.assignment_id AS pr_assignment, ap.org_id AS pr_org FROM geo.assignment_progress ap
WHERE ap.org_id = :'org' AND ap.assignment_id = :'assignment' ORDER BY ap.tool_count DESC LIMIT 1 \gset
SELECT now() - interval '90 days' AS lo_ts, now() + interval '1 day' AS hi_ts \gset
SELECT g.grantee_org_id AS grantee_org, g.resource_id AS grant_res FROM geo.share_grant g WHERE g.status = 'approved' LIMIT 1 \gset
SELECT l.id AS lecture FROM geo.lecture l WHERE l.org_id = :'platform' ORDER BY l.id OFFSET 5 LIMIT 1 \gset
SELECT p.id AS problem, p.current_version_id AS problem_pv FROM geo.problem p WHERE p.org_id = :'platform' AND p.source = 'catalog' ORDER BY p.id OFFSET 3 LIMIT 1 \gset
-- Lượt sửa sau RV: bài tự học đang kế thừa khóa (khớp chữ ký với đề kho của bài lớp còn khóa), học sinh có nhiều câu hỏi, mã giáo viên
SELECT a.id AS inh_assignment, a.org_id AS inh_org, a.owner_user_id AS inh_student, a.problem_version_id AS inh_pv FROM geo.assignment a
WHERE a.kind = 'self' AND geo.fn_self_lock_source(a.org_id, a.owner_user_id, a.problem_version_id) IS NOT NULL
  AND EXISTS (SELECT 1 FROM geo.assignment_progress ap WHERE ap.org_id = a.org_id AND ap.assignment_id = a.id AND ap.hint_level_max = 2)
ORDER BY a.id LIMIT 1 \gset
SELECT q.org_id AS qa_org, q.user_id AS qa_student FROM geo.qa_turn q GROUP BY q.org_id, q.user_id ORDER BY count(*) DESC, q.user_id LIMIT 1 \gset
SELECT t.selector AS selector, encode(t.verifier_hmac, 'hex') AS verifier, encode(digest('203.0.113.7', 'sha256'), 'hex') AS ip_key
FROM geo.teacher_access_code t WHERE t.org_id = :'org' AND t.user_id = :'teacher' \gset
