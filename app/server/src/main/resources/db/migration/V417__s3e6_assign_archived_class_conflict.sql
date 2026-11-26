-- FIX trước gate T-34 (inbox 27/11/2026, Dev-S3E6-01 phần giao bộ): giao qua practice-sets/{id}/assign (và
-- assignment-sets/{id}/assign khi lớp vừa lưu trữ sau khi tạo nháp) tới lớp CỦA MÌNH đã lưu trữ trả CONFLICT_ARCHIVED
-- (ứng dụng trả CONFLICT như đường tạo bộ), thay vì NOT_FOUND. Lớp của người khác / không có vẫn NOT_FOUND.
-- Chỉ thay thân hàm (CREATE OR REPLACE giữ quyền EXECUTE của V180); phần còn lại y như V180.

CREATE OR REPLACE FUNCTION fn_assign_practice_set(p_org uuid, p_actor uuid, p_set uuid, p_class uuid, p_allow_past boolean,
                                       p_request uuid DEFAULT NULL)
RETURNS text
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
DECLARE s record; c record; it record; v_answer text; v_rel timestamptz; v_aid uuid; v_ok boolean;
BEGIN
  SELECT x.owner_user_id, x.status, x.release_at INTO s
    FROM assignment_set x WHERE x.org_id = p_org AND x.id = p_set FOR UPDATE;
  IF s.owner_user_id IS NULL OR s.owner_user_id <> p_actor THEN RETURN 'NOT_FOUND'; END IF;
  SELECT x.teacher_id, x.archived_at INTO c FROM class x WHERE x.org_id = p_org AND x.id = p_class;
  IF c.teacher_id IS NULL OR c.teacher_id <> p_actor THEN RETURN 'NOT_FOUND'; END IF;
  -- lớp của chính mình nhưng Đã lưu trữ: không giao bộ mới (BR-S3E6-14, US-S3E6-02-FC2)
  IF c.archived_at IS NOT NULL THEN RETURN 'CONFLICT_ARCHIVED'; END IF;
  IF s.status <> 'draft' THEN RETURN 'CONFLICT_ASSIGNED'; END IF;
  IF NOT EXISTS (SELECT 1 FROM assignment_item i WHERE i.org_id = p_org AND i.set_id = p_set) THEN RETURN 'EMPTY'; END IF;
  IF NOT COALESCE(p_allow_past, false) AND EXISTS (
       SELECT 1 FROM assignment_item i WHERE i.org_id = p_org AND i.set_id = p_set
          AND COALESCE(i.release_at, s.release_at) <= now()) THEN
    RETURN 'PAST_RELEASE';
  END IF;
  BEGIN
    FOR it IN SELECT i.id, i.content_org_id, i.problem_id, i.problem_version_id, i.release_at
                FROM assignment_item i WHERE i.org_id = p_org AND i.set_id = p_set ORDER BY i.position LOOP
      SELECT true INTO v_ok FROM problem p
       WHERE p.org_id = platform_org_id() AND p.id = it.problem_id AND it.content_org_id = platform_org_id()
         AND p.source = 'catalog' AND p.archived_at IS NULL AND fn_catalog_bank_visible(p_org, p.bank);
      IF v_ok IS NULL THEN RAISE EXCEPTION 'ITEM_NOT_FOUND' USING ERRCODE = 'GS281'; END IF;
      v_ok := NULL;
      SELECT so.final_answer INTO v_answer FROM solution so
       WHERE so.org_id = it.content_org_id AND so.problem_version_id = it.problem_version_id;
      IF v_answer IS NULL THEN RAISE EXCEPTION 'NO_ANSWER' USING ERRCODE = 'GS282'; END IF;
      v_rel := COALESCE(it.release_at, s.release_at);
      IF v_rel IS NOT NULL AND v_rel < now() THEN v_rel := now(); END IF;
      v_aid := uuid_v7();
      INSERT INTO assignment (id, org_id, kind, class_id, content_org_id, problem_id, problem_version_id, assigned_by,
                              release_at, allow_hint, allow_qa, created_at)
      VALUES (v_aid, p_org, 'class', p_class, it.content_org_id, it.problem_id, it.problem_version_id, p_actor,
              v_rel, true, true, now());
      INSERT INTO audit_log (org_id, actor_user_id, action, target_id, before_release_at, after_release_at, request_id)
      VALUES (p_org, p_actor, 'assignment_create', v_aid, NULL, v_rel, p_request);
      UPDATE assignment_item SET assignment_id = v_aid, answer_snapshot = v_answer WHERE org_id = p_org AND id = it.id;
      v_answer := NULL;
    END LOOP;
  EXCEPTION
    WHEN unique_violation THEN RETURN 'CONFLICT_DUPLICATE';
    WHEN SQLSTATE 'GS281' THEN RETURN 'ITEM_NOT_FOUND';
    WHEN SQLSTATE 'GS282' THEN RETURN 'NO_ANSWER';
  END;
  UPDATE assignment_set SET status = 'assigned', class_id = p_class, assigned_at = now(), updated_at = now()
   WHERE org_id = p_org AND id = p_set;
  RETURN 'OK';
END $$;
COMMENT ON FUNCTION fn_assign_practice_set IS 'Giao bộ bài (S2-E8/S3-E2): mỗi item một hàng assignment MVP + ảnh chụp đáp số. Trả OK | NOT_FOUND | CONFLICT_ARCHIVED | CONFLICT_ASSIGNED | EMPTY | PAST_RELEASE | CONFLICT_DUPLICATE | ITEM_NOT_FOUND | NO_ANSWER.';
