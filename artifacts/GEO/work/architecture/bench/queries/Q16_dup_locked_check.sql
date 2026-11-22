-- role: owner
-- Thân của geo.fn_self_lock_source (D-GEO-150, 151; RV-01, 03): bài tự học có khớp một bài lớp ĐANG KHÓA của bất kỳ lớp nào em đang
-- thuộc không. Khớp chính theo chữ ký chuẩn (hình mẫu, tham số quy về a = 1, đại lượng hỏi) cùng sig_version; khớp phụ theo băm đề
-- chuẩn hóa. Đề kho có org_id = platform nên cả hai nhánh tra chỉ mục có org_id đứng đầu. Chạy mỗi lần xin Gợi ý / lời giải của bài tự học.
-- Vai chủ vì geo_app không đọc problem_signature (không cần).
:explain
SELECT x.id FROM (
  SELECT a.id
  FROM geo.problem_signature me
  JOIN geo.problem_signature cat ON cat.org_id = :'platform' AND cat.signature = me.signature AND cat.sig_version = me.sig_version
  JOIN geo.problem_version cpv   ON cpv.org_id = cat.org_id AND cpv.id = cat.problem_version_id
  JOIN geo.class_member cm       ON cm.org_id = :'inh_org' AND cm.user_id = :'inh_student' AND cm.left_at IS NULL
  JOIN geo.assignment a          ON a.org_id = cm.org_id AND a.class_id = cm.class_id AND a.problem_id = cpv.problem_id
                                AND a.kind = 'class' AND a.archived_at IS NULL
  WHERE me.org_id = :'inh_org' AND me.problem_version_id = :'inh_pv'
    AND (a.release_at IS NULL OR a.release_at > now())
  UNION ALL
  SELECT a.id
  FROM geo.problem_version me
  JOIN geo.problem_version cpv   ON cpv.org_id = :'platform' AND cpv.statement_hash = me.statement_hash
  JOIN geo.class_member cm       ON cm.org_id = :'inh_org' AND cm.user_id = :'inh_student' AND cm.left_at IS NULL
  JOIN geo.assignment a          ON a.org_id = cm.org_id AND a.class_id = cm.class_id AND a.problem_id = cpv.problem_id
                                AND a.kind = 'class' AND a.archived_at IS NULL
  WHERE me.org_id = :'inh_org' AND me.id = :'inh_pv'
    AND (a.release_at IS NULL OR a.release_at > now())
) x
LIMIT 1;
