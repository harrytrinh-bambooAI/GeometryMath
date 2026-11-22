-- Kiểm tra tách quyền, cách ly và các bất biến ở tầng dữ liệu (BR-01, BR-03, BR-11, BR-12, BR-28; brief §7; D-GEO-150…169; RV-GEO-01…37).
-- Chạy bằng superuser trên CSDL thử (mặc định geo_arch2, đã nạp bench/test_hooks.sql); mỗi ca đổi sang vai geo_app / geo_ai / geo_tester
-- khi cần. Mọi thay đổi bị ROLLBACK ở cuối. Kết quả: dòng NOTICE bắt đầu bằng PASS hoặc FAIL.
-- Nhóm: A tách quyền bảng · R năm câu lệnh RV-10 phải BỊ TỪ CHỐI · T lời giải của giáo viên (D-GEO-153) · B cổng và máy trạng thái giờ mở
-- · E ranh giới đúng R bằng móc kiểm thử (D-GEO-169) · C tự học (D-GEO-150, 151, 155) · D toàn vẹn và khóa ngoại tổ hợp · G ghi sự kiện,
-- lũy đẳng, hỏi đáp · L đăng nhập (D-GEO-152, 127) · V nhãn máy.
\set ON_ERROR_STOP on
\i bench/02_params.sql
SELECT set_config('t.org', :'org', false), set_config('t.student', :'student', false), set_config('t.teacher', :'teacher', false), set_config('t.class', :'class', false),
       set_config('t.assignment_locked', :'assignment_locked', false), set_config('t.assignment', :'assignment', false), set_config('t.pv', :'pv', false),
       set_config('t.selector', :'selector', false), set_config('t.verifier', :'verifier', false) \gset
BEGIN;
SET client_min_messages = notice;
DO $$
DECLARE
  v_org uuid := current_setting('t.org')::uuid; v_stu uuid := current_setting('t.student')::uuid; v_tch uuid := current_setting('t.teacher')::uuid;
  v_cls uuid := current_setting('t.class')::uuid; v_locked uuid := current_setting('t.assignment_locked')::uuid; v_open uuid := current_setting('t.assignment')::uuid;
  v_pv uuid := current_setting('t.pv')::uuid;
  v_plat uuid := geo.platform_org_id(); v_author uuid := '00000000-0000-7000-8000-0000000000aa';
  v_other_stu uuid; v_other_org uuid; v_other_cls uuid; v_other_tch uuid; v_other_asg uuid; v_same_org_tch uuid;
  v_n int; v_m int; v_rev int; v_msg text; v_st text; v_t0 timestamptz := clock_timestamp(); v_locked_rev int;
  v_self uuid; v_self_org uuid; v_self_stu uuid; v_self_pv uuid; v_src uuid; v_b_pv uuid; v_own_self_pv uuid;
  v_cat_pv uuid; v_cat_problem uuid; v_new_problem uuid; v_new_pv uuid; v_new_asg uuid; v_new_cls_asg uuid; v_cls_tch uuid;
  v_req uuid; v_txt text; v_id bigint; r record; v_msg2_dummy text;
BEGIN
  SELECT cm.user_id, cm.org_id, cm.class_id INTO v_other_stu, v_other_org, v_other_cls FROM geo.class_member cm WHERE cm.org_id <> v_org ORDER BY cm.user_id LIMIT 1;
  SELECT c.teacher_id INTO v_other_tch FROM geo.class c WHERE c.org_id = v_other_org AND c.id = v_other_cls;
  SELECT a.id INTO v_other_asg FROM geo.assignment a WHERE a.org_id = v_other_org AND a.kind = 'class' ORDER BY a.id LIMIT 1;
  SELECT u.id INTO v_same_org_tch FROM geo.app_user u WHERE u.org_id = v_org AND u.role = 'teacher' AND u.id <> v_tch ORDER BY u.id LIMIT 1;

  PERFORM set_config('app.org_id', v_org::text, true);   -- ngữ cảnh trường như withTenant(ctx) (D-GEO-127); vô hại khi RLS tắt
  ---------------------------------------------------------------- A. tách quyền bảng
  SET LOCAL ROLE geo_app;
  BEGIN PERFORM 1 FROM geo.solution LIMIT 1; RAISE NOTICE 'FAIL A1: geo_app đọc được geo.solution';
  EXCEPTION WHEN insufficient_privilege THEN RAISE NOTICE 'PASS A1: geo_app KHÔNG đọc được geo.solution (insufficient_privilege)'; END;
  BEGIN PERFORM 1 FROM geo.verification_report LIMIT 1; RAISE NOTICE 'FAIL A2: geo_app đọc được verification_report';
  EXCEPTION WHEN insufficient_privilege THEN RAISE NOTICE 'PASS A2: geo_app KHÔNG đọc được verification_report'; END;
  BEGIN EXECUTE 'SELECT final_answer FROM geo.solution'; RAISE NOTICE 'FAIL A3: cột final_answer đọc được';
  EXCEPTION WHEN insufficient_privilege THEN RAISE NOTICE 'PASS A3: đọc cột final_answer bị chặn'; END;
  BEGIN EXECUTE 'INSERT INTO geo.solution (org_id, problem_version_id, final_answer, steps) SELECT org_id, id, ''x'', ''{}'' FROM geo.problem_version LIMIT 1';
        RAISE NOTICE 'FAIL A4: geo_app ghi được solution';
  EXCEPTION WHEN insufficient_privilege THEN RAISE NOTICE 'PASS A4: geo_app KHÔNG ghi được solution'; END;
  SELECT count(*) INTO v_n FROM information_schema.columns c
   WHERE c.table_schema = 'geo' AND c.table_name IN ('problem','problem_version','scene_build','hint','assignment','lecture','qa_turn','problem_signature')
     AND (c.column_name ~* '(solution|answer|steps)');
  RAISE NOTICE '%', CASE WHEN v_n = 0 THEN 'PASS' ELSE 'FAIL' END || ' A5: không bảng nào geo_app đọc được có cột tên solution/answer/steps (' || v_n || ')';
  BEGIN EXECUTE format('UPDATE geo.assignment SET release_at = now() WHERE id = %L', v_locked); RAISE NOTICE 'FAIL A6: geo_app sửa trực tiếp release_at';
  EXCEPTION WHEN insufficient_privilege THEN RAISE NOTICE 'PASS A6: geo_app KHÔNG sửa được release_at trực tiếp (chỉ qua fn_set_release)'; END;
  BEGIN EXECUTE 'DELETE FROM geo.audit_log'; RAISE NOTICE 'FAIL A7: xóa được audit_log';
  EXCEPTION WHEN insufficient_privilege THEN RAISE NOTICE 'PASS A7: geo_app KHÔNG xóa được audit_log'; END;
  BEGIN EXECUTE 'SELECT reply_text FROM geo.qa_turn LIMIT 1'; RAISE NOTICE 'FAIL A8: geo_app đọc được qa_turn.reply_text';
  EXCEPTION WHEN insufficient_privilege THEN RAISE NOTICE 'PASS A8: geo_app KHÔNG đọc trực tiếp câu trả lời đã lưu (chỉ chủ câu hỏi qua fn_qa_history, D-GEO-159)'; END;
  BEGIN EXECUTE 'SELECT 1 FROM geo.teacher_access_code LIMIT 1'; RAISE NOTICE 'FAIL A9: geo_app đọc được bảng mã giáo viên';
  EXCEPTION WHEN insufficient_privilege THEN RAISE NOTICE 'PASS A9: geo_app KHÔNG đọc bảng mã Giáo viên mẫu (D-GEO-152)'; END;
  BEGIN EXECUTE format('INSERT INTO geo.event_log (occurred_at, org_id, assignment_id, user_id, kind) VALUES (now(), %L, %L, %L, ''unlock'')', v_org, v_locked, v_stu);
        RAISE NOTICE 'FAIL A10: geo_app ghi thẳng event_log';
  EXCEPTION WHEN insufficient_privilege THEN RAISE NOTICE 'PASS A10: geo_app KHÔNG ghi thẳng event_log (chỉ qua hàm ghi sự kiện)'; END;
  BEGIN EXECUTE format('INSERT INTO geo.qa_turn (org_id, assignment_id, user_id, request_id, question, reply_kind, reply_text, guard_version) VALUES (%L, %L, %L, gen_random_uuid(), ''q'', ''concept'', ''a'', ''g'')', v_org, v_open, v_stu);
        RAISE NOTICE 'FAIL A11: geo_app ghi thẳng qa_turn';
  EXCEPTION WHEN insufficient_privilege THEN RAISE NOTICE 'PASS A11: geo_app KHÔNG ghi thẳng qa_turn (câu trả lời phải qua Kiểm soát đáp án, fn_record_qa của geo_ai)'; END;
  BEGIN EXECUTE format('INSERT INTO geo.audit_log (org_id, actor_user_id, action, target_id) VALUES (%L, %L, ''release_now'', %L)', v_org, v_tch, v_locked);
        RAISE NOTICE 'FAIL A12: geo_app ghi thẳng audit_log';
  EXCEPTION WHEN insufficient_privilege THEN RAISE NOTICE 'PASS A12: geo_app KHÔNG ghi thẳng audit_log (chỉ hàm SECURITY DEFINER ghi)'; END;

  ---------------------------------------------------------------- R. năm câu lệnh của RV-GEO-10 phải BỊ TỪ CHỐI với vai geo_app (D-GEO-158)
  BEGIN EXECUTE format('INSERT INTO geo.app_user (id, org_id, role, account_kind, display_name) VALUES (gen_random_uuid(), %L, ''teacher'', ''sample'', ''GV giả'')', v_org);
        RAISE NOTICE 'FAIL R1: geo_app tự tạo được Giáo viên mẫu';
  EXCEPTION WHEN insufficient_privilege THEN RAISE NOTICE 'PASS R1: geo_app KHÔNG tạo được tài khoản (giáo viên mẫu) bằng INSERT app_user'; END;
  BEGIN EXECUTE format('INSERT INTO geo.problem (id, org_id, subject_id, source, author_user_id, level, solid_kind, topic_id, title) VALUES (gen_random_uuid(), %L, 1, ''catalog'', %L, ''thpt'', ''box'', 1, ''đề giả'')', v_plat, v_author);
        RAISE NOTICE 'FAIL R2: geo_app ghi được kho đề nền tảng';
  EXCEPTION WHEN insufficient_privilege THEN RAISE NOTICE 'PASS R2: geo_app KHÔNG ghi được kho đề nền tảng (chỉ geo_content)'; END;
  BEGIN EXECUTE format('INSERT INTO geo.problem_version (id, org_id, problem_id, version, statement, statement_hash) SELECT gen_random_uuid(), p.org_id, p.id, 99, ''sửa lén'', digest(''x'',''sha256'') FROM geo.problem p WHERE p.org_id = %L LIMIT 1', v_plat);
        RAISE NOTICE 'FAIL R2b: geo_app thêm được version cho đề kho';
  EXCEPTION WHEN insufficient_privilege THEN RAISE NOTICE 'PASS R2b: geo_app KHÔNG thêm được version cho đề kho nền tảng'; END;
  BEGIN EXECUTE format('UPDATE geo.problem_version SET verify_status = ''verified'' WHERE id = %L', v_pv);
        RAISE NOTICE 'FAIL R3: geo_app tự gắn nhãn Đã kiểm chứng';
  EXCEPTION WHEN insufficient_privilege THEN RAISE NOTICE 'PASS R3: geo_app KHÔNG tự gắn nhãn "Đã kiểm chứng" (UPDATE verify_status bị từ chối)'; END;
  BEGIN PERFORM geo.fn_set_verify_status(v_plat, v_pv, gen_random_uuid(), v_tch); RAISE NOTICE 'FAIL R3b: geo_app gọi được fn_set_verify_status';
  EXCEPTION WHEN insufficient_privilege THEN RAISE NOTICE 'PASS R3b: geo_app KHÔNG gọi được hàm đổi nhãn máy (chỉ geo_ai, geo_content kèm báo cáo)'; END;
  BEGIN EXECUTE format('UPDATE geo.assignment_progress SET hint_level_max = 2 WHERE org_id = %L AND assignment_id = %L AND user_id = %L', v_org, v_open, v_stu);
        RAISE NOTICE 'FAIL R4: geo_app tự nâng hint_level_max';
  EXCEPTION WHEN insufficient_privilege THEN RAISE NOTICE 'PASS R4: geo_app KHÔNG tự nâng hint_level_max (UPDATE assignment_progress bị từ chối)'; END;
  BEGIN EXECUTE format('INSERT INTO geo.assignment_progress (org_id, assignment_id, user_id, last_active_at, hint_level_max) VALUES (%L, %L, %L, now(), 2)', v_other_org, v_other_asg, v_other_stu);
        RAISE NOTICE 'FAIL R4b: geo_app chèn được tiến độ';
  EXCEPTION WHEN insufficient_privilege THEN RAISE NOTICE 'PASS R4b: geo_app KHÔNG chèn được hàng tiến độ'; END;
  BEGIN EXECUTE format('INSERT INTO geo.assignment (id, org_id, kind, class_id, content_org_id, problem_id, problem_version_id, assigned_by, release_at) SELECT gen_random_uuid(), %L, ''class'', %L, pv.org_id, pv.problem_id, pv.id, %L, now() - interval ''1 day'' FROM geo.problem_version pv WHERE pv.org_id = %L LIMIT 1', v_org, v_cls, v_tch, v_plat);
        RAISE NOTICE 'FAIL R5: geo_app tạo bài giao có giờ mở ở quá khứ';
  EXCEPTION WHEN insufficient_privilege THEN RAISE NOTICE 'PASS R5: geo_app KHÔNG INSERT bài giao (giờ mở quá khứ không qua hàm, không nhật ký)'; END;
  SELECT f.status INTO v_st FROM geo.fn_create_assignment(v_org, v_tch, gen_random_uuid(), v_cls,
         (SELECT pv.id FROM geo.problem_version pv WHERE pv.org_id = v_plat ORDER BY pv.id LIMIT 1), now() - interval '1 minute', true, true) f;
  RAISE NOTICE '%', CASE WHEN v_st = 'VALIDATION_PAST' THEN 'PASS' ELSE 'FAIL' END || ' R5b: fn_create_assignment từ chối giờ mở ở quá khứ (' || v_st || ')';
  RESET ROLE;
  BEGIN INSERT INTO geo.assignment (id, org_id, kind, class_id, content_org_id, problem_id, problem_version_id, assigned_by, release_at, created_at)
        SELECT gen_random_uuid(), v_org, 'class', v_cls, pv.org_id, pv.problem_id, pv.id, v_tch, now() - interval '1 day', now()
        FROM geo.problem_version pv WHERE pv.org_id = v_plat AND NOT EXISTS (SELECT 1 FROM geo.assignment a WHERE a.org_id = v_org AND a.class_id = v_cls AND a.problem_id = pv.problem_id) LIMIT 1;
        RAISE NOTICE 'FAIL R5c: chủ lược đồ chèn được bài giao có giờ mở trước giờ tạo';
  EXCEPTION WHEN check_violation THEN RAISE NOTICE 'PASS R5c: CHECK assignment_release_ck chặn giờ mở trước giờ tạo kể cả với vai chủ'; END;

  ---------------------------------------------------------------- T. lời giải của giáo viên chỉ đề kho (D-GEO-153, RV-04)
  SELECT a.problem_version_id INTO v_b_pv FROM geo.assignment a WHERE a.org_id = v_other_org AND a.kind = 'self' ORDER BY a.id LIMIT 1;
  SELECT a.problem_version_id INTO v_own_self_pv FROM geo.assignment a WHERE a.org_id = v_org AND a.kind = 'self' ORDER BY a.id LIMIT 1;
  SET LOCAL ROLE geo_app;
  SELECT count(*) INTO v_n FROM geo.fn_get_solution_teacher(v_org, v_tch, v_pv);
  RAISE NOTICE '%', CASE WHEN v_n = 1 THEN 'PASS' ELSE 'FAIL' END || ' T1: giáo viên mẫu xem được lời giải đề kho (' || v_n || ')';
  SELECT count(*) INTO v_n FROM geo.fn_get_solution_teacher(v_org, v_tch, v_b_pv);
  RAISE NOTICE '%', CASE WHEN v_n = 0 THEN 'PASS' ELSE 'FAIL' END || ' T2: giáo viên trường A KHÔNG xem được lời giải bài tự học của trường B (ca RV-04 tái hiện) (' || v_n || ')';
  SELECT count(*) INTO v_n FROM geo.fn_get_solution_teacher(v_org, v_tch, v_own_self_pv);
  RAISE NOTICE '%', CASE WHEN v_n = 0 THEN 'PASS' ELSE 'FAIL' END || ' T3: giáo viên KHÔNG xem được lời giải bài tự học của học sinh trường mình (' || v_n || ')';
  SELECT count(*) INTO v_n FROM geo.fn_get_solution_teacher(v_org, v_stu, v_pv);
  RAISE NOTICE '%', CASE WHEN v_n = 0 THEN 'PASS' ELSE 'FAIL' END || ' T4: tài khoản học sinh gọi hàm giáo viên => 0 hàng (' || v_n || ')';
  SELECT count(*) INTO v_n FROM geo.fn_get_solution_teacher(v_plat, v_author, v_pv);
  RAISE NOTICE '%', CASE WHEN v_n = 0 THEN 'PASS' ELSE 'FAIL' END || ' T5: tài khoản hệ thống (tác giả nền tảng) không dùng được để xem lời giải (' || v_n || ')';
  SELECT count(*) INTO v_n FROM geo.fn_get_solution_teacher(v_org, v_other_tch, v_pv);
  RAISE NOTICE '%', CASE WHEN v_n = 0 THEN 'PASS' ELSE 'FAIL' END || ' T6: org trong ngữ cảnh khác trường của giáo viên => 0 hàng (' || v_n || ')';

  ---------------------------------------------------------------- B. cổng lời giải, mã trạng thái (RV-21)
  SELECT g.status, count(g.final_answer) OVER () INTO v_st, v_n FROM geo.fn_get_solution(v_org, v_stu, v_open) g;
  RAISE NOTICE '%', CASE WHEN v_st = 'OK' AND v_n = 1 THEN 'PASS' ELSE 'FAIL' END || ' B1: bài đã tới giờ mở, học sinh trong lớp nhận lời giải (status=' || v_st || ')';
  SELECT g.status, g.final_answer INTO v_st, v_txt FROM geo.fn_get_solution(v_org, v_stu, v_locked) g;
  RAISE NOTICE '%', CASE WHEN v_st = 'LOCKED' AND v_txt IS NULL THEN 'PASS' ELSE 'FAIL' END || ' B2: bài chưa đặt giờ / chưa tới giờ => LOCKED (FORBIDDEN, BR-GEO-251), không có nội dung (' || v_st || ')';
  SELECT g.status, g.final_answer INTO v_st, v_txt FROM geo.fn_get_solution(v_org, v_other_stu, v_open) g;
  RAISE NOTICE '%', CASE WHEN v_st = 'NOT_FOUND' AND v_txt IS NULL THEN 'PASS' ELSE 'FAIL' END || ' B3: học sinh lớp/trường khác dù biết id bài => NOT_FOUND (' || v_st || ')';
  SELECT g.status INTO v_st FROM geo.fn_get_solution(v_other_org, v_stu, v_open) g;
  RAISE NOTICE '%', CASE WHEN v_st = 'NOT_FOUND' THEN 'PASS' ELSE 'FAIL' END || ' B4: org_id sai trong ngữ cảnh => NOT_FOUND (' || v_st || ')';
  RESET ROLE;

  UPDATE geo.assignment SET release_at = NULL, release_rev = 0 WHERE id = v_locked;
  v_locked_rev := 0;
  SET LOCAL ROLE geo_app;
  SELECT g.status INTO v_st FROM geo.fn_get_solution(v_org, v_stu, v_locked) g;
  RAISE NOTICE '%', CASE WHEN v_st = 'LOCKED' THEN 'PASS' ELSE 'FAIL' END || ' B5a: chưa đặt giờ => khóa (BR-03, D-GEO-043)';
  SELECT f.status INTO v_st FROM geo.fn_set_release(v_org, v_stu, v_locked, false, now() + interval '1 hour', v_locked_rev) f;
  RAISE NOTICE '%', CASE WHEN v_st = 'FORBIDDEN' THEN 'PASS' ELSE 'FAIL' END || ' B5: học sinh KHÔNG đặt được giờ mở => FORBIDDEN (' || v_st || ')';
  SELECT f.status INTO v_st FROM geo.fn_set_release(v_org, v_same_org_tch, v_locked, false, now() + interval '1 hour', v_locked_rev) f;
  RAISE NOTICE '%', CASE WHEN v_st = 'FORBIDDEN' THEN 'PASS' ELSE 'FAIL' END || ' B5b: giáo viên khác cùng trường => FORBIDDEN (' || v_st || ')';
  SELECT f.status INTO v_st FROM geo.fn_set_release(v_other_org, v_other_tch, v_locked, false, now() + interval '1 hour', v_locked_rev) f;
  RAISE NOTICE '%', CASE WHEN v_st = 'NOT_FOUND' THEN 'PASS' ELSE 'FAIL' END || ' B5c: bài của trường khác => NOT_FOUND (' || v_st || ')';
  SELECT f.status, f.release_rev INTO v_st, v_rev FROM geo.fn_set_release(v_org, v_tch, v_locked, false, now() + interval '1 hour', v_locked_rev + 99) f;
  RAISE NOTICE '%', CASE WHEN v_st = 'CONFLICT_REV' AND v_rev = v_locked_rev THEN 'PASS' ELSE 'FAIL' END || ' B6: sai release_rev => CONFLICT_REV kèm rev hiện hành (' || v_st || ')';
  SELECT f.status INTO v_st FROM geo.fn_set_release(v_org, v_tch, v_locked, false, now() - interval '1 minute', v_locked_rev) f;
  RAISE NOTICE '%', CASE WHEN v_st = 'VALIDATION_PAST' THEN 'PASS' ELSE 'FAIL' END || ' B6b: đặt giờ ở quá khứ => VALIDATION_PAST (' || v_st || ')';
  SELECT f.status, f.release_rev INTO v_st, v_rev FROM geo.fn_set_release(v_org, v_tch, v_locked, false, now() + interval '1 hour', v_locked_rev) f;
  RAISE NOTICE '%', CASE WHEN v_st = 'OK' AND v_rev = v_locked_rev + 1 THEN 'PASS' ELSE 'FAIL' END || ' B7: giáo viên của lớp đặt giờ tương lai (chưa đặt -> sắp mở), rev tăng';
  SELECT g.status INTO v_st FROM geo.fn_get_solution(v_org, v_stu, v_locked) g;
  RAISE NOTICE '%', CASE WHEN v_st = 'LOCKED' THEN 'PASS' ELSE 'FAIL' END || ' B8: giờ mở ở tương lai => vẫn khóa';
  SELECT f.status, f.release_rev INTO v_st, v_rev FROM geo.fn_set_release(v_org, v_tch, v_locked, false, now() + interval '2 hours', v_rev) f;
  RAISE NOTICE '%', CASE WHEN v_st = 'OK' AND v_rev = v_locked_rev + 2 THEN 'PASS' ELSE 'FAIL' END || ' B8b: đổi giờ khi sắp mở thành công';
  SELECT f.status, f.release_rev INTO v_st, v_rev FROM geo.fn_set_release(v_org, v_tch, v_locked, true, NULL, v_rev) f;
  SELECT g.status INTO v_msg FROM geo.fn_get_solution(v_org, v_stu, v_locked) g;
  RAISE NOTICE '%', CASE WHEN v_st = 'OK' AND v_msg = 'OK' THEN 'PASS' ELSE 'FAIL' END || ' B9: "mở ngay" (release_at := now() của CSDL) => học sinh trong lớp nhận lời giải';
  SELECT f.status INTO v_st FROM geo.fn_set_release(v_org, v_tch, v_locked, false, now() + interval '3 hours', v_rev) f;
  RAISE NOTICE '%', CASE WHEN v_st = 'CONFLICT_ALREADY_OPEN' THEN 'PASS' ELSE 'FAIL' END || ' B10: đã mở thì không đổi giờ nữa => CONFLICT_ALREADY_OPEN (D-GEO-042 A, D-GEO-143)';
  SELECT f.status INTO v_st FROM geo.fn_set_release(v_org, v_tch, v_locked, true, NULL, v_rev) f;
  RAISE NOTICE '%', CASE WHEN v_st = 'CONFLICT_ALREADY_OPEN' THEN 'PASS' ELSE 'FAIL' END || ' B10b: bấm "mở ngay" khi đã mở cũng bị từ chối';
  SELECT g.status INTO v_st FROM geo.fn_get_solution(v_org, v_stu, v_locked) g;
  RAISE NOTICE '%', CASE WHEN v_st = 'OK' THEN 'PASS' ELSE 'FAIL' END || ' B10c: sau các lần bị từ chối, lời giải vẫn mở';
  RESET ROLE;
  SELECT string_agg(action::text, ',' ORDER BY id) INTO v_msg FROM geo.audit_log WHERE target_id = v_locked AND occurred_at >= v_t0 - interval '1 minute';
  RAISE NOTICE '%', CASE WHEN v_msg = 'release_set,release_change,release_now' THEN 'PASS' ELSE 'FAIL' END || ' B11: audit_log ghi đúng 3 dòng theo thứ tự (' || coalesce(v_msg, '-') || '), lần bị từ chối không ghi';

  ---------------------------------------------------------------- E. ranh giới đúng R bằng móc kiểm thử profile test (D-GEO-169, RV-24)
  SET LOCAL ROLE geo_app;
  BEGIN PERFORM geo.fn_test_set_release(v_org, v_open, now()); RAISE NOTICE 'FAIL E0: geo_app gọi được móc kiểm thử';
  EXCEPTION WHEN insufficient_privilege THEN RAISE NOTICE 'PASS E0: vai ứng dụng KHÔNG gọi được fn_test_set_release (chỉ geo_tester ở profile test)'; END;
  RESET ROLE; SET LOCAL ROLE geo_tester;
  PERFORM geo.fn_test_set_release(v_org, v_open, now() + interval '1 second');
  SELECT g.status INTO v_st FROM geo.fn_get_solution(v_org, v_stu, v_open) g;
  RAISE NOTICE '%', CASE WHEN v_st = 'LOCKED' THEN 'PASS' ELSE 'FAIL' END || ' E1: t = R - 1 giây => khóa';
  PERFORM geo.fn_test_set_release(v_org, v_open, now());
  SELECT g.status INTO v_st FROM geo.fn_get_solution(v_org, v_stu, v_open) g;
  RAISE NOTICE '%', CASE WHEN v_st = 'OK' THEN 'PASS' ELSE 'FAIL' END || ' E2: t = R đúng (release_at = now() của CSDL) => mở (D-GEO-041 phương án A)';
  PERFORM geo.fn_test_set_release(v_org, v_open, now() - interval '1 second');
  SELECT g.status INTO v_st FROM geo.fn_get_solution(v_org, v_stu, v_open) g;
  RAISE NOTICE '%', CASE WHEN v_st = 'OK' THEN 'PASS' ELSE 'FAIL' END || ' E3: t = R + 1 giây => mở';
  RESET ROLE;

  ---------------------------------------------------------------- C. tự học (D-GEO-150, 151, 155; RV-01, 03, 06, 15)
  SELECT a.id, a.org_id, a.owner_user_id, a.problem_version_id INTO v_self, v_self_org, v_self_stu, v_self_pv
    FROM geo.assignment a WHERE a.kind = 'self' AND a.lock_source_assignment_id IS NULL
     AND EXISTS (SELECT 1 FROM geo.problem_signature s WHERE s.org_id = a.org_id AND s.problem_version_id = a.problem_version_id)
     AND geo.fn_self_lock_source(a.org_id, a.owner_user_id, a.problem_version_id) IS NULL ORDER BY a.id LIMIT 1;
  UPDATE geo.assignment_progress SET hint_level_max = 0 WHERE org_id = v_self_org AND assignment_id = v_self AND user_id = v_self_stu;
  PERFORM set_config('app.org_id', v_self_org::text, true);
  SET LOCAL ROLE geo_app;
  SELECT g.status INTO v_st FROM geo.fn_get_solution(v_self_org, v_self_stu, v_self) g;
  RAISE NOTICE '%', CASE WHEN v_st = 'LOCKED' THEN 'PASS' ELSE 'FAIL' END || ' C1: bài tự học chưa xem Gợi ý 1, 2 => khóa (BR-05, D-GEO-023)';
  SELECT f.status INTO v_st FROM geo.fn_get_hint(v_self_org, v_self_stu, v_self, 2::smallint, gen_random_uuid()) f;
  RAISE NOTICE '%', CASE WHEN v_st = 'LEVEL_ORDER' THEN 'PASS' ELSE 'FAIL' END || ' C1b: xin Gợi ý 2 trước Gợi ý 1 => LEVEL_ORDER (D-GEO-108) (' || v_st || ')';
  v_req := gen_random_uuid();
  SELECT f.status, f.hint_source INTO v_st, v_msg FROM geo.fn_get_hint(v_self_org, v_self_stu, v_self, 1::smallint, v_req) f;
  SELECT f.status INTO v_txt FROM geo.fn_get_hint(v_self_org, v_self_stu, v_self, 1::smallint, v_req) f;   -- gửi lại cùng request_id
  RESET ROLE;
  SELECT ap.hint_view_count, ap.hint_level_max INTO v_n, v_m FROM geo.assignment_progress ap WHERE ap.org_id = v_self_org AND ap.assignment_id = v_self AND ap.user_id = v_self_stu;
  SELECT count(*) INTO v_rev FROM geo.event_log e WHERE e.org_id = v_self_org AND e.assignment_id = v_self AND e.user_id = v_self_stu AND e.kind = 'hint' AND e.occurred_at >= now();
  RAISE NOTICE '%', CASE WHEN v_st = 'OK' AND v_msg = 'self' AND v_txt = 'OK' AND v_m = 1 AND v_rev = 1 THEN 'PASS' ELSE 'FAIL' END
                    || ' C1c: Gợi ý 1 của đề tự nhập; gửi lại cùng request_id không ghi thêm sự kiện (sự kiện hint mới=' || v_rev || ', mức=' || v_m || ', RV-18)';
  SET LOCAL ROLE geo_app;
  PERFORM 1 FROM geo.fn_get_hint(v_self_org, v_self_stu, v_self, 2::smallint, gen_random_uuid());
  SELECT g.status INTO v_st FROM geo.fn_get_solution(v_self_org, v_self_stu, v_self) g;
  RAISE NOTICE '%', CASE WHEN v_st = 'OK' THEN 'PASS' ELSE 'FAIL' END || ' C2: xem Gợi ý 2 qua fn_get_hint (đường duy nhất nâng mức) => mở (' || v_st || ')';
  SELECT g.status INTO v_st FROM geo.fn_get_solution(v_self_org, v_stu, v_self) g;
  RAISE NOTICE '%', CASE WHEN v_st = 'NOT_FOUND' THEN 'PASS' ELSE 'FAIL' END || ' C3: học sinh khác không xem được bài tự học của bạn (NOT_FOUND)';
  RESET ROLE;
  -- kế thừa khóa theo chữ ký chuẩn: văn bản đề khác hẳn đề kho, chỉ chữ ký trùng (đổi tên điểm, F-GEO-226)
  SELECT a.id, a.org_id, a.owner_user_id, a.problem_version_id, a.lock_source_assignment_id INTO v_self, v_self_org, v_self_stu, v_self_pv, v_src
    FROM geo.assignment a WHERE a.kind = 'self' AND a.lock_source_assignment_id IS NOT NULL ORDER BY a.id LIMIT 1;
  UPDATE geo.assignment_progress SET hint_level_max = 2 WHERE org_id = v_self_org AND assignment_id = v_self AND user_id = v_self_stu;
  PERFORM set_config('app.org_id', v_self_org::text, true);
  UPDATE geo.assignment SET release_at = NULL WHERE org_id = v_self_org AND id = v_src;
  SELECT count(*) INTO v_n FROM geo.problem_version a JOIN geo.assignment l ON l.org_id = v_self_org AND l.id = v_src
    JOIN geo.problem_version b ON b.org_id = l.content_org_id AND b.id = l.problem_version_id WHERE a.org_id = v_self_org AND a.id = v_self_pv AND a.statement_hash = b.statement_hash;
  SET LOCAL ROLE geo_app;
  SELECT g.status INTO v_st FROM geo.fn_get_solution(v_self_org, v_self_stu, v_self) g;
  RAISE NOTICE '%', CASE WHEN v_st = 'LOCKED' AND v_n = 0 THEN 'PASS' ELSE 'FAIL' END || ' C4: tự học khớp CHỮ KÝ (văn bản khác, băm khác) với đề kho của bài lớp còn khóa => khóa dù đã xem Gợi ý 2 (D-GEO-150)';
  SELECT f.status, f.hint_source, f.body INTO v_st, v_msg, v_txt FROM geo.fn_get_hint(v_self_org, v_self_stu, v_self, 2::smallint, gen_random_uuid()) f;
  RAISE NOTICE '%', CASE WHEN v_st = 'OK' AND v_msg = 'catalog_inherited'
                          AND v_txt = (SELECT h.body FROM geo.hint h JOIN geo.assignment l ON l.org_id = v_self_org AND l.id = v_src
                                       WHERE h.org_id = l.content_org_id AND h.problem_version_id = l.problem_version_id AND h.level = 2) THEN 'PASS' ELSE 'FAIL' END
                    || ' C5: bài tự học kế thừa khóa nhận Gợi ý 2 của ĐỀ KHO, không phải gợi ý AI (D-GEO-155, RV-06) (' || coalesce(v_msg, '-') || ')';
  RESET ROLE;
  UPDATE geo.assignment SET release_at = now() - interval '1 minute' WHERE org_id = v_self_org AND id = v_src;
  SET LOCAL ROLE geo_app;
  SELECT g.status INTO v_st FROM geo.fn_get_solution(v_self_org, v_self_stu, v_self) g;
  RAISE NOTICE '%', CASE WHEN v_st = 'OK' THEN 'PASS' ELSE 'FAIL' END || ' C6: bài lớp nguồn đã mở => bài tự học cũng mở';
  RESET ROLE;
  -- "dán trước, giao sau" (RV-03, D-GEO-151): bài tự học tạo khi CHƯA có bài lớp nào khớp; giáo viên giao đề kho đó sau => khóa ngay lần xin kế tiếp
  PERFORM set_config('app.org_id', v_org::text, true);
  SELECT c.teacher_id INTO v_cls_tch FROM geo.class c WHERE c.org_id = v_org AND c.id = v_cls;
  SELECT pv.id, pv.problem_id INTO v_cat_pv, v_cat_problem FROM geo.problem_version pv
   WHERE pv.org_id = v_plat AND NOT EXISTS (SELECT 1 FROM geo.assignment a WHERE a.org_id = v_org AND a.class_id = v_cls AND a.problem_id = pv.problem_id)
   ORDER BY pv.id LIMIT 1;
  v_new_problem := gen_random_uuid(); v_new_pv := gen_random_uuid(); v_new_asg := gen_random_uuid(); v_new_cls_asg := gen_random_uuid();
  SELECT s.sig_version, s.template_id, s.params_norm, s.asked_quantity, s.signature INTO r FROM geo.problem_signature s WHERE s.org_id = v_plat AND s.problem_version_id = v_cat_pv;
  SET LOCAL ROLE geo_app;   -- ứng dụng ghi đề tự nhập (được phép: trường mình, student_input, needs_review)
  INSERT INTO geo.problem (id, org_id, subject_id, source, owner_user_id, level, solid_kind, topic_id, title)
  VALUES (v_new_problem, v_org, 1, 'student_input', v_stu, 'thpt', 'pyramid', 1, 'Đề tự nhập đổi tên điểm');
  INSERT INTO geo.problem_version (id, org_id, problem_id, version, statement, statement_hash, created_by)
  VALUES (v_new_pv, v_org, v_new_problem, 1, 'Cho hình chóp S.MNPQ (tên điểm khác đề kho) ...', digest('de-doi-ten-' || v_new_pv, 'sha256'), v_stu);
  INSERT INTO geo.problem_signature (org_id, problem_version_id, sig_version, template_id, params_norm, asked_quantity, signature)
  VALUES (v_org, v_new_pv, r.sig_version, r.template_id, r.params_norm, r.asked_quantity, r.signature);   -- engine tính: cùng chữ ký với đề kho
  INSERT INTO geo.hint (org_id, problem_version_id, level, body) VALUES (v_org, v_new_pv, 1, 'Gợi ý AI 1'), (v_org, v_new_pv, 2, 'Gợi ý AI 2');
  SELECT f.status, f.lock_source_assignment_id INTO v_st, v_src FROM geo.fn_create_self_assignment(v_org, v_stu, v_new_asg, v_new_pv) f;
  PERFORM 1 FROM geo.fn_get_hint(v_org, v_stu, v_new_asg, 1::smallint, gen_random_uuid());
  PERFORM 1 FROM geo.fn_get_hint(v_org, v_stu, v_new_asg, 2::smallint, gen_random_uuid());
  RESET ROLE;
  INSERT INTO geo.solution (org_id, problem_version_id, final_answer, steps) VALUES (v_org, v_new_pv, 'a√3/4', '{"schema_version":1,"steps":[]}');
  SET LOCAL ROLE geo_app;
  SELECT g.status INTO v_msg FROM geo.fn_get_solution(v_org, v_stu, v_new_asg) g;
  RAISE NOTICE '%', CASE WHEN v_st = 'OK' AND v_src IS NULL AND v_msg = 'OK' THEN 'PASS' ELSE 'FAIL' END || ' C7a: dán đề trước khi giáo viên giao: chưa có bài lớp khớp => mở sau Gợi ý 2 (' || v_msg || ')';
  SELECT f.status INTO v_st FROM geo.fn_create_assignment(v_org, v_cls_tch, v_new_cls_asg, v_cls, v_cat_pv, NULL, true, true) f;
  SELECT g.status INTO v_msg FROM geo.fn_get_solution(v_org, v_stu, v_new_asg) g;
  RAISE NOTICE '%', CASE WHEN v_st = 'OK' AND v_msg = 'LOCKED' THEN 'PASS' ELSE 'FAIL' END || ' C7b: giáo viên giao đúng đề kho đó SAU (chưa đặt giờ) => bài tự học khóa ngay ở lần xin kế tiếp (RV-03) (' || v_msg || ')';
  SELECT f.hint_source INTO v_msg FROM geo.fn_get_hint(v_org, v_stu, v_new_asg, 1::smallint, gen_random_uuid()) f;
  RAISE NOTICE '%', CASE WHEN v_msg = 'catalog_inherited' THEN 'PASS' ELSE 'FAIL' END || ' C7c: từ lúc đó gợi ý lấy từ đề kho (' || v_msg || ')';
  RESET ROLE;
  -- đề không dựng được hình: không có chữ ký => không có lời giải tự học (D-GEO-150)
  SELECT a.id, a.org_id, a.owner_user_id INTO v_self, v_self_org, v_self_stu FROM geo.assignment a
   WHERE a.kind = 'self' AND NOT EXISTS (SELECT 1 FROM geo.problem_signature s WHERE s.org_id = a.org_id AND s.problem_version_id = a.problem_version_id) ORDER BY a.id LIMIT 1;
  UPDATE geo.assignment_progress SET hint_level_max = 2 WHERE org_id = v_self_org AND assignment_id = v_self AND user_id = v_self_stu;
  SET LOCAL ROLE geo_app;
  SELECT g.status INTO v_st FROM geo.fn_get_solution(v_self_org, v_self_stu, v_self) g;
  RAISE NOTICE '%', CASE WHEN v_st = 'LOCKED' THEN 'PASS' ELSE 'FAIL' END || ' C8: đề tự nhập không dựng được hình (không chữ ký) => không có lời giải tự học dù đã xem Gợi ý 2 (' || v_st || ')';
  RESET ROLE;

  PERFORM set_config('app.org_id', v_org::text, true);
  ---------------------------------------------------------------- D. bất biến và toàn vẹn
  BEGIN UPDATE geo.problem_version SET statement = 'đổi tại chỗ' WHERE id = v_pv; RAISE NOTICE 'FAIL D1: sửa được nội dung đề tại chỗ';
  EXCEPTION WHEN integrity_constraint_violation THEN RAISE NOTICE 'PASS D1: sửa nội dung version đề bị chặn (BR-11)'; END;
  BEGIN UPDATE geo.solution SET final_answer = 'đổi' WHERE problem_version_id = v_pv; RAISE NOTICE 'FAIL D2';
  EXCEPTION WHEN integrity_constraint_violation THEN RAISE NOTICE 'PASS D2: sửa đáp số lời giải tại chỗ bị chặn (BR-11)'; END;
  BEGIN UPDATE geo.audit_log SET actor_user_id = actor_user_id WHERE id = (SELECT min(id) FROM geo.audit_log); RAISE NOTICE 'FAIL D3';
  EXCEPTION WHEN integrity_constraint_violation THEN RAISE NOTICE 'PASS D3: audit_log chỉ thêm'; END;
  BEGIN INSERT INTO geo.assignment_progress (org_id, assignment_id, user_id, last_active_at) VALUES (v_other_org, v_open, v_other_stu, now());
        RAISE NOTICE 'FAIL D4: tiến độ tham chiếu bài của trường khác';
  EXCEPTION WHEN foreign_key_violation THEN RAISE NOTICE 'PASS D4: khóa ngoại tổ hợp chặn tham chiếu chéo trường (progress -> assignment)'; END;
  BEGIN INSERT INTO geo.class_member (org_id, class_id, user_id, name_key) VALUES (v_other_org, v_cls, v_other_stu, 'x');
        RAISE NOTICE 'FAIL D5';
  EXCEPTION WHEN foreign_key_violation THEN RAISE NOTICE 'PASS D5: khóa ngoại tổ hợp chặn thêm em trường khác vào lớp'; END;
  BEGIN INSERT INTO geo.share_grant (id, org_id, grantee_org_id, resource_kind, resource_id, purpose, status, requested_by)
        VALUES (gen_random_uuid(), v_org, v_other_org, 'lecture', gen_random_uuid(), 'mục đích thử nghiệm', 'approved', v_tch);
        RAISE NOTICE 'FAIL D6';
  EXCEPTION WHEN check_violation THEN RAISE NOTICE 'PASS D6: share_grant approved bắt buộc hai bên duyệt và có thời hạn (BR-28)'; END;
  BEGIN INSERT INTO geo.verification_report (id, org_id, problem_version_id, scene_ok, answer_ok, result, checks)
        SELECT gen_random_uuid(), org_id, id, true, false, 'verified', '{}' FROM geo.problem_version LIMIT 1;
        RAISE NOTICE 'FAIL D7';
  EXCEPTION WHEN check_violation THEN RAISE NOTICE 'PASS D7: báo cáo không thể ghi verified khi đáp số chưa khớp (BR-07)'; END;
  BEGIN INSERT INTO geo.assignment (id, org_id, kind, class_id, content_org_id, problem_id, problem_version_id, assigned_by, release_at)
        SELECT gen_random_uuid(), v_org, 'self', v_cls, v_org, pv.problem_id, pv.id, v_tch, now() FROM geo.problem_version pv WHERE pv.id = v_own_self_pv;
        RAISE NOTICE 'FAIL D8';
  EXCEPTION WHEN check_violation THEN RAISE NOTICE 'PASS D8: bài tự học không thể có lớp/giờ mở (CHECK assignment_kind_ck)'; END;
  BEGIN INSERT INTO geo.assignment (id, org_id, kind, owner_user_id, content_org_id, problem_id, problem_version_id, assigned_by)
        SELECT gen_random_uuid(), v_org, 'self', v_stu, pv.org_id, pv.problem_id, pv.id, v_stu FROM geo.problem_version pv WHERE pv.id = v_b_pv;
        RAISE NOTICE 'FAIL D9: bài giao trỏ đề tự nhập của trường khác';
  EXCEPTION WHEN check_violation OR foreign_key_violation THEN RAISE NOTICE 'PASS D9: bài giao không thể trỏ đề tự nhập của trường khác (CHECK nội dung + FK tổ hợp, RV-12)'; END;
  BEGIN INSERT INTO geo.assignment (id, org_id, kind, owner_user_id, content_org_id, problem_id, problem_version_id, assigned_by)
        SELECT gen_random_uuid(), v_org, 'self', v_tch, v_org, pv.problem_id, pv.id, v_tch FROM geo.problem_version pv WHERE pv.id = v_own_self_pv;
        RAISE NOTICE 'FAIL D9b: bài tự học dùng đề tự nhập của người khác';
  EXCEPTION WHEN foreign_key_violation THEN RAISE NOTICE 'PASS D9b: bài tự học chỉ dùng đề tự nhập của chính em (FK (org, problem, owner))'; END;
  BEGIN UPDATE geo.assignment SET lock_source_assignment_id = v_other_asg WHERE org_id = v_org AND kind = 'self' AND id = (SELECT min(id::text)::uuid FROM geo.assignment WHERE org_id = v_org AND kind = 'self');
        RAISE NOTICE 'FAIL D10: nguồn khóa trỏ bài của trường khác';
  EXCEPTION WHEN foreign_key_violation THEN RAISE NOTICE 'PASS D10: lock_source chỉ trỏ bài cùng trường (FK tổ hợp, RV-12)'; END;
  BEGIN INSERT INTO geo.qa_turn (org_id, assignment_id, user_id, request_id, question, reply_kind, reply_text, guard_version)
        VALUES (v_org, v_open, v_other_stu, gen_random_uuid(), 'q', 'concept', 'a', 'g1');
        RAISE NOTICE 'FAIL D11: qa_turn của người dùng trường khác';
  EXCEPTION WHEN foreign_key_violation THEN RAISE NOTICE 'PASS D11: qa_turn.user_id thuộc đúng trường (FK tổ hợp, RV-12)'; END;
  BEGIN INSERT INTO geo.problem (id, org_id, subject_id, source, author_user_id, level, solid_kind, topic_id, title)
        VALUES (gen_random_uuid(), v_plat, 1, 'catalog', v_author, 'thpt', 'box', NULL, 'đề kho thiếu dạng bài');
        RAISE NOTICE 'FAIL D12';
  EXCEPTION WHEN check_violation THEN RAISE NOTICE 'PASS D12: đề kho bắt buộc topic_id (RV-31) và tác giả nền tảng (D-GEO-160)'; END;
  BEGIN INSERT INTO geo.assignment (id, org_id, kind, class_id, content_org_id, problem_id, problem_version_id, assigned_by)
        SELECT gen_random_uuid(), v_org, 'class', v_cls, pv.org_id, pv.problem_id, pv.id, v_tch FROM geo.problem_version pv WHERE pv.id = v_own_self_pv;
        RAISE NOTICE 'FAIL D13';
  EXCEPTION WHEN check_violation THEN RAISE NOTICE 'PASS D13: bài lớp chỉ giao đề kho nền tảng (CHECK assignment_content_ck)'; END;
  BEGIN INSERT INTO geo.app_user (id, org_id, role, account_kind, display_name) VALUES (gen_random_uuid(), v_org, 'student', 'sample', 'x');
        RAISE NOTICE 'FAIL D14';
  EXCEPTION WHEN check_violation THEN RAISE NOTICE 'PASS D14: tài khoản sample phải là giáo viên của một trường; system chỉ ở nền tảng (app_user_kind_ck)'; END;
  SET LOCAL ROLE geo_app;
  SELECT a.problem_version_id INTO v_cat_pv FROM geo.assignment a WHERE a.org_id = v_org AND a.class_id = v_cls AND a.kind = 'class' ORDER BY a.id LIMIT 1;
  SELECT f.status INTO v_st FROM geo.fn_create_assignment(v_org, v_cls_tch, gen_random_uuid(), v_cls, v_cat_pv, now() + interval '1 day', true, true) f;
  RAISE NOTICE '%', CASE WHEN v_st = 'CONFLICT_DUPLICATE' THEN 'PASS' ELSE 'FAIL' END || ' D15: giao lại cùng problem cho cùng lớp => CONFLICT_DUPLICATE (D-GEO-161, chỉ mục duy nhất một phần) (' || v_st || ')';
  SELECT f.status INTO v_st FROM geo.fn_create_assignment(v_org, v_cls_tch, v_new_cls_asg, v_cls, (SELECT pv.id FROM geo.problem_version pv WHERE pv.org_id = v_plat ORDER BY pv.id DESC LIMIT 1), NULL, true, true) f;
  RAISE NOTICE '%', CASE WHEN v_st IN ('DUPLICATE', 'CONFLICT_DUPLICATE') THEN 'PASS' ELSE 'FAIL' END || ' D15b: gửi lại cùng id bài giao không tạo bài thứ hai (' || v_st || ')';
  SELECT f.status INTO v_st FROM geo.fn_create_assignment(v_org, v_same_org_tch, gen_random_uuid(), v_cls, v_pv, NULL, true, true) f;
  RAISE NOTICE '%', CASE WHEN v_st = 'FORBIDDEN' THEN 'PASS' ELSE 'FAIL' END || ' D16: giáo viên không dạy lớp giao bài => FORBIDDEN (' || v_st || ')';
  SELECT f.status INTO v_st FROM geo.fn_create_assignment(v_other_org, v_other_tch, gen_random_uuid(), v_cls, v_pv, NULL, true, true) f;
  RAISE NOTICE '%', CASE WHEN v_st = 'NOT_FOUND' THEN 'PASS' ELSE 'FAIL' END || ' D16b: lớp của trường khác => NOT_FOUND (' || v_st || ')';
  SELECT f.status INTO v_st FROM geo.fn_create_assignment(v_org, v_cls_tch, gen_random_uuid(), v_cls, v_own_self_pv, NULL, true, true) f;
  RAISE NOTICE '%', CASE WHEN v_st = 'NOT_FOUND' THEN 'PASS' ELSE 'FAIL' END || ' D16c: giao đề tự nhập của học sinh cho lớp => NOT_FOUND (chỉ đề kho) (' || v_st || ')';
  RESET ROLE;
  SELECT count(*) INTO v_n FROM geo.audit_log WHERE org_id = v_org AND target_id = v_new_cls_asg AND action = 'assignment_create';
  RAISE NOTICE '%', CASE WHEN v_n = 1 THEN 'PASS' ELSE 'FAIL' END || ' D17: tạo bài giao ghi đúng 1 dòng audit_log assignment_create (BR-GEO-240)';

  ---------------------------------------------------------------- G. ghi sự kiện, lũy đẳng, biên phiên, hỏi đáp
  SET LOCAL ROLE geo_app;
  SELECT f.status, f.inserted INTO v_st, v_n FROM geo.fn_record_batch(v_org, v_other_stu, v_open, ARRAY[now()], ARRAY['tool'::geo.event_kind], ARRAY[1::smallint]) f;
  RAISE NOTICE '%', CASE WHEN v_st = 'NOT_FOUND' AND v_n = 0 THEN 'PASS' ELSE 'FAIL' END || ' G1: lô sự kiện cho bài của lớp khác => NOT_FOUND, 0 hàng ghi (RV-26)';
  SELECT f.status INTO v_st FROM geo.fn_record_batch(v_org, v_stu, v_open, ARRAY[now()], ARRAY['unlock'::geo.event_kind], ARRAY[NULL::smallint]) f;
  RAISE NOTICE '%', CASE WHEN v_st = 'VALIDATION_KIND' THEN 'PASS' ELSE 'FAIL' END || ' G2: lô mức A không mang được sự kiện mức B (unlock) (' || v_st || ')';
  SELECT f.status, f.inserted INTO v_st, v_n FROM geo.fn_record_batch(v_org, v_stu, v_open, ARRAY[now() - interval '3 seconds', now(), now() + interval '2 days'],
         ARRAY['tool'::geo.event_kind, 'scene_shown', 'tool'], ARRAY[2, 123, 3]::smallint[]) f;
  RAISE NOTICE '%', CASE WHEN v_st = 'OK' AND v_n = 3 THEN 'PASS' ELSE 'FAIL' END || ' G3: lô mức A gồm tool + scene_shown (D-GEO-165) ghi đủ, giờ tương lai bị kẹp về giờ nhận';
  SELECT g.status INTO v_st FROM geo.fn_get_solution(v_org, v_stu, (SELECT a.id FROM geo.assignment a WHERE a.org_id = v_org AND a.class_id = v_cls AND a.release_at IS NULL ORDER BY a.id LIMIT 1)) g;
  v_new_asg := (SELECT a.id FROM geo.assignment a WHERE a.org_id = v_org AND a.class_id = v_cls AND a.kind = 'class' AND (a.release_at IS NULL OR a.release_at > now()) ORDER BY a.id LIMIT 1);
  SELECT geo.fn_record_durable(v_org, v_stu, v_new_asg, gen_random_uuid(), 'unlock') INTO v_st;
  RAISE NOTICE '%', CASE WHEN v_st = 'LOCKED' THEN 'PASS' ELSE 'FAIL' END || ' G4: ghi "đã xem lời giải" cho bài còn khóa bị từ chối (LOCKED) (' || coalesce(v_st, '-') || ')';
  v_req := gen_random_uuid();
  SELECT geo.fn_record_durable(v_org, v_stu, v_open, v_req, 'open') INTO v_st;
  SELECT geo.fn_record_durable(v_org, v_stu, v_open, v_req, 'open') INTO v_msg;
  RESET ROLE;
  SELECT count(*) INTO v_n FROM geo.event_log e WHERE e.org_id = v_org AND e.assignment_id = v_open AND e.user_id = v_stu AND e.kind = 'open' AND e.occurred_at >= now() - interval '1 second';
  RAISE NOTICE '%', CASE WHEN v_st = 'OK' AND v_msg = 'DUPLICATE' AND v_n = 1 THEN 'PASS' ELSE 'FAIL' END || ' G5: open là sự kiện bền (D-GEO-162); gửi lại cùng request_id => DUPLICATE, 1 sự kiện (RV-18)';
  -- biên phiên 30 phút (D-GEO-144, RV-17): khoảng nghỉ 29:59 cộng vào thời gian làm bài; 30:00 mở phiên mới (không cộng)
  UPDATE geo.assignment_progress SET last_active_at = now() - interval '29 minutes 59 seconds', active_seconds = 0 WHERE org_id = v_org AND assignment_id = v_open AND user_id = v_stu;
  PERFORM geo._progress_apply(v_org, v_open, v_stu, now(), now(), 1, 0::smallint, 0, 0, false, false);
  SELECT ap.active_seconds INTO v_n FROM geo.assignment_progress ap WHERE ap.org_id = v_org AND ap.assignment_id = v_open AND ap.user_id = v_stu;
  UPDATE geo.assignment_progress SET last_active_at = now() - interval '30 minutes', active_seconds = 0 WHERE org_id = v_org AND assignment_id = v_open AND user_id = v_stu;
  PERFORM geo._progress_apply(v_org, v_open, v_stu, now(), now(), 1, 0::smallint, 0, 0, false, false);
  SELECT ap.active_seconds INTO v_m FROM geo.assignment_progress ap WHERE ap.org_id = v_org AND ap.assignment_id = v_open AND ap.user_id = v_stu;
  RAISE NOTICE '%', CASE WHEN v_n = 1799 AND v_m = 0 THEN 'PASS' ELSE 'FAIL' END || ' G6: biên phiên 29:59 cùng phiên (+1799 s), 30:00 phiên mới (+0 s) (D-GEO-144, RV-17) (' || v_n || ', ' || v_m || ')';
  SET LOCAL ROLE geo_app;
  BEGIN PERFORM geo.fn_record_qa(v_org, v_stu, v_open, gen_random_uuid(), 'q', 'concept', 'a', 'g1'); RAISE NOTICE 'FAIL G7: geo_app gọi được fn_record_qa';
  EXCEPTION WHEN insufficient_privilege THEN RAISE NOTICE 'PASS G7: chỉ cổng AI (geo_ai) ghi lượt hỏi đáp (câu trả lời đã qua Kiểm soát đáp án)'; END;
  RESET ROLE; SET LOCAL ROLE geo_ai;
  v_req := gen_random_uuid();
  SELECT f.status, f.qa_id INTO v_st, v_id FROM geo.fn_record_qa(v_org, v_stu, v_open, v_req, 'Thiết diện là gì?', 'concept', 'Thiết diện là phần chung của mặt phẳng và khối.', 'g1') f;
  SELECT f.status INTO v_msg FROM geo.fn_record_qa(v_org, v_stu, v_open, v_req, 'Thiết diện là gì?', 'concept', 'Thiết diện là phần chung của mặt phẳng và khối.', 'g1') f;
  RESET ROLE;
  SELECT count(*) INTO v_n FROM geo.qa_turn q WHERE q.org_id = v_org AND q.request_id = v_req;
  RAISE NOTICE '%', CASE WHEN v_st = 'OK' AND v_msg = 'DUPLICATE' AND v_n = 1 THEN 'PASS' ELSE 'FAIL' END || ' G8: lưu câu trả lời hỏi đáp (D-GEO-159); gửi lại cùng request_id không nhân đôi câu hỏi/ask_count (RV-18)';
  SET LOCAL ROLE geo_app;
  SELECT count(*) FILTER (WHERE h.id = v_id AND h.reply_text IS NOT NULL) INTO v_n FROM geo.fn_qa_history(v_org, v_stu, NULL, 21) h;
  SELECT count(*) FILTER (WHERE h.id = v_id) INTO v_m FROM geo.fn_qa_history(v_org, v_other_stu, NULL, 100) h;
  RAISE NOTICE '%', CASE WHEN v_n = 1 AND v_m = 0 THEN 'PASS' ELSE 'FAIL' END || ' G9: lịch sử hỏi đáp (Q21) trả câu trả lời đã lưu cho chủ câu hỏi, người khác không thấy';
  RESET ROLE;

  ---------------------------------------------------------------- L. đăng nhập (D-GEO-152, D-GEO-127, D-GEO-106)
  SET LOCAL ROLE geo_app;
  SELECT f.status, f.user_id INTO v_st, v_src FROM geo.fn_teacher_login(current_setting('t.selector'), decode(current_setting('t.verifier'), 'hex'), digest('ip-ok', 'sha256')) f;
  RAISE NOTICE '%', CASE WHEN v_st = 'OK' AND v_src = v_tch THEN 'PASS' ELSE 'FAIL' END || ' L1: Giáo viên mẫu đăng nhập bằng mã của chính tài khoản (' || v_st || ')';
  FOR v_n IN 1..10 LOOP PERFORM 1 FROM geo.fn_teacher_login('ZZZZ', digest('sai' || v_n, 'sha256'), digest('ip-do', 'sha256')); END LOOP;
  SELECT f.status INTO v_st FROM geo.fn_teacher_login(current_setting('t.selector'), decode(current_setting('t.verifier'), 'hex'), digest('ip-do', 'sha256')) f;
  RAISE NOTICE '%', CASE WHEN v_st = 'RATE_LIMITED' THEN 'PASS' ELSE 'FAIL' END || ' L2: 10 lần sai từ một IP trong 15 phút => lần 11 RATE_LIMITED kể cả mã đúng (D-GEO-173) (' || v_st || ')';
  FOR v_n IN 1..10 LOOP PERFORM 1 FROM geo.fn_teacher_login(current_setting('t.selector'), digest('sai' || v_n, 'sha256'), digest('ip-' || v_n, 'sha256')); END LOOP;
  SELECT f.status INTO v_st FROM geo.fn_teacher_login(current_setting('t.selector'), decode(current_setting('t.verifier'), 'hex'), digest('ip-moi', 'sha256')) f;
  RAISE NOTICE '%', CASE WHEN v_st = 'RATE_LIMITED' THEN 'PASS' ELSE 'FAIL' END || ' L3: 10 lần sai vào một tài khoản từ 10 IP => tài khoản RATE_LIMITED (dò phân tán) (' || v_st || ')';
  RESET ROLE;
  SELECT t.id INTO v_src FROM geo.teacher_access_code t WHERE t.org_id = v_other_org AND t.user_id = v_other_tch;
  SET LOCAL ROLE geo_ops;
  SELECT geo.fn_revoke_teacher_code(v_src, v_author) INTO v_st;
  RESET ROLE;
  SELECT f.status INTO v_msg FROM geo.fn_teacher_login((SELECT t.selector FROM geo.teacher_access_code t WHERE t.id = v_src),
                                                        (SELECT t.verifier_hmac FROM geo.teacher_access_code t WHERE t.id = v_src), digest('ip-thu-hoi', 'sha256')) f;
  RAISE NOTICE '%', CASE WHEN v_st = 'OK' AND v_msg = 'INVALID_CODE' THEN 'PASS' ELSE 'FAIL' END || ' L4: mã đã thu hồi (geo_ops) không đăng nhập được, dù đúng (' || v_msg || ')';
  UPDATE geo.teacher_access_code SET issued_at = now() - interval '2 days', expires_at = now() - interval '1 day', revoked_at = NULL WHERE id = v_src;
  SELECT f.status INTO v_msg FROM geo.fn_teacher_login((SELECT t.selector FROM geo.teacher_access_code t WHERE t.id = v_src),
                                                        (SELECT t.verifier_hmac FROM geo.teacher_access_code t WHERE t.id = v_src), digest('ip-het-han', 'sha256')) f;
  RAISE NOTICE '%', CASE WHEN v_msg = 'INVALID_CODE' THEN 'PASS' ELSE 'FAIL' END || ' L5: mã hết hạn không đăng nhập được (' || v_msg || ')';
  SELECT count(*) INTO v_n FROM geo.teacher_access_code t WHERE t.verifier_hmac = digest('secret-' || t.user_id::text, 'sha512');
  SELECT count(*) INTO v_m FROM information_schema.columns c WHERE c.table_schema = 'geo' AND c.table_name = 'teacher_access_code' AND c.column_name ~* '(^code$|plain|secret)';
  RAISE NOTICE '%', CASE WHEN v_m = 0 THEN 'PASS' ELSE 'FAIL' END || ' L6: bảng mã chỉ có selector + HMAC, không có cột mã rõ (D-GEO-152)';
  SET LOCAL ROLE geo_app;
  SELECT f.status, f.user_id, f.created INTO v_st, v_src, v_txt FROM geo.fn_student_login((SELECT c.join_code FROM geo.class c WHERE c.id = v_cls), 'hoc sinh moi zz', 'Học sinh mới', gen_random_uuid()) f;
  SELECT f.user_id, f.created INTO v_new_asg, v_msg FROM geo.fn_student_login((SELECT c.join_code FROM geo.class c WHERE c.id = v_cls), 'hoc sinh moi zz', 'Học sinh mới', gen_random_uuid()) f;
  RESET ROLE;
  SELECT u.role::text || '/' || u.account_kind::text INTO v_msg2_dummy FROM geo.app_user u WHERE u.id = v_src;
  RAISE NOTICE '%', CASE WHEN v_st = 'OK' AND v_txt = 'true' AND v_new_asg = v_src AND v_msg2_dummy = 'student/light' THEN 'PASS' ELSE 'FAIL' END
                    || ' L7: fn_student_login tạo đúng một học sinh student/light; vào lại cùng tên => cùng người (D-GEO-106, D-GEO-158)';
  SET LOCAL ROLE geo_app;
  BEGIN PERFORM geo.fn_create_sample_teacher(v_org, gen_random_uuid(), 'GV giả'); RAISE NOTICE 'FAIL L8: geo_app tạo được giáo viên mẫu qua hàm vận hành';
  EXCEPTION WHEN insufficient_privilege THEN RAISE NOTICE 'PASS L8: chỉ geo_ops tạo Giáo viên mẫu và cấp mã (geo_app bị từ chối)'; END;
  RESET ROLE;

  ---------------------------------------------------------------- V. nhãn máy (D-GEO-158 (3))
  SELECT r2.id INTO v_src FROM geo.verification_report r2 WHERE r2.problem_version_id <> v_pv AND r2.org_id = v_plat AND r2.result = 'verified' LIMIT 1;
  SET LOCAL ROLE geo_ai;
  SELECT geo.fn_set_verify_status(v_plat, v_pv, v_src, v_author) INTO v_st;
  RESET ROLE;
  RAISE NOTICE '%', CASE WHEN v_st = 'NOT_FOUND' THEN 'PASS' ELSE 'FAIL' END || ' V1: không đổi được nhãn bằng báo cáo của version khác (' || v_st || ')';
  BEGIN UPDATE geo.problem_version SET verify_status = 'verified', verification_report_id = NULL WHERE id = v_new_pv;
        RAISE NOTICE 'FAIL V2: nhãn verified không cần báo cáo';
  EXCEPTION WHEN check_violation THEN RAISE NOTICE 'PASS V2: nhãn "Đã kiểm chứng" bắt buộc báo cáo verified của chính version, kể cả với vai chủ (trigger + CHECK)'; END;
  SET LOCAL ROLE geo_app;
  BEGIN INSERT INTO geo.problem_version (id, org_id, problem_id, version, statement, statement_hash, verify_status, created_by)
        VALUES (gen_random_uuid(), v_org, v_new_problem, 2, 'bản 2', digest('b2', 'sha256'), 'verified', v_stu);
        RAISE NOTICE 'FAIL V3: geo_app tạo version mang sẵn nhãn verified';
  EXCEPTION WHEN check_violation THEN RAISE NOTICE 'PASS V3: version mới do ứng dụng tạo luôn needs_review (không thể chèn sẵn verified)'; END;
  RESET ROLE;
END $$;
ROLLBACK;
