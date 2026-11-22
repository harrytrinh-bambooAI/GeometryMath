-- Đăng nhập (Q01 qua geo.fn_student_login, tên đã có trong lớp nên không tạo mới). Trọng số: 2%
\set i random(1, 19900)
SELECT c.join_code AS jc, cm.name_key AS nk FROM bench.pairs p JOIN geo.class c ON c.org_id = p.org_id AND c.id = p.class_id JOIN geo.class_member cm ON cm.org_id = p.org_id AND cm.class_id = p.class_id AND cm.user_id = p.student_id WHERE p.id = :i \gset
SELECT status, org_id, class_id, user_id FROM geo.fn_student_login(:jc, :nk, 'x', NULL);
