-- FIX4b (soát E7, 25/11/2026): lô sự kiện mức A
--  M2 (Dev-E7-07): thời gian làm bài của MỘT lô cộng theo từng cặp sự kiện liên tiếp, chỉ cộng khoảng < 30 phút
--      (BR-GEO-278, D-GEO-144). Trước đây cộng nguyên khoảng min…max của lô.
--  M3 (Dev-E7-08): lũy đẳng lô bền trong CSDL (BR-GEO-280, NFR-GEO-052): batch_id vào durable_request, chèn
--      ON CONFLICT DO NOTHING trong CÙNG giao dịch với INSERT event_log. Redis chỉ còn là đường nhanh.
-- Không sửa V10: định nghĩa hàm mới/ghi đè bằng migration này.

-- Cập nhật tiến độ cho lô: như _progress_apply nhưng thời gian trong lô do bên gọi tính sẵn (p_active giây).
CREATE FUNCTION _progress_apply_batch(p_org uuid, p_assignment uuid, p_user uuid, p_first timestamptz, p_last timestamptz,
                                      p_active int, p_tools int)
RETURNS void LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
BEGIN
  INSERT INTO assignment_progress AS ap
         (org_id, assignment_id, user_id, first_opened_at, last_active_at, hint_level_max, hint_view_count, ask_count, tool_count,
          active_seconds, solution_viewed_at)
  VALUES (p_org, p_assignment, p_user, NULL, p_last, 0, 0, 0, p_tools, GREATEST(0, p_active), NULL)
  ON CONFLICT (org_id, assignment_id, user_id) DO UPDATE SET
         last_active_at = GREATEST(ap.last_active_at, EXCLUDED.last_active_at),
         tool_count     = ap.tool_count + EXCLUDED.tool_count,
         active_seconds = ap.active_seconds + EXCLUDED.active_seconds
                          + CASE WHEN p_first - ap.last_active_at >= interval '0' AND p_first - ap.last_active_at < interval '30 minutes'
                                 THEN extract(epoch FROM p_first - ap.last_active_at)::int ELSE 0 END,
         updated_at     = now();
END $$;

-- Q07 + Q08 (bản FIX4b): lô sự kiện mức A, lũy đẳng theo p_batch (NULL = không lũy đẳng, dùng cho scene_shown một sự kiện).
-- Trả 'DUPLICATE' khi lô đã ghi (không cộng đôi dù khóa Redis đã hết hạn hoặc bị xóa).
CREATE FUNCTION fn_record_batch(p_org uuid, p_user uuid, p_assignment uuid,
                                p_occurred_at timestamptz[], p_kind event_kind[], p_arg smallint[], p_batch uuid)
RETURNS TABLE (status text, inserted integer)
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
DECLARE v_n int; v_tools int; v_min timestamptz; v_max timestamptz; v_active int;
BEGIN
  IF cardinality(p_kind) IS NULL OR cardinality(p_kind) = 0 OR cardinality(p_kind) > 200
     OR cardinality(p_kind) <> cardinality(p_occurred_at) OR cardinality(p_kind) <> cardinality(p_arg)
     OR EXISTS (SELECT 1 FROM unnest(p_kind) k WHERE k NOT IN ('tool', 'scene_shown')) THEN
    RETURN QUERY SELECT 'VALIDATION_KIND'::text, 0; RETURN;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM assignment x WHERE x.org_id = p_org AND x.id = p_assignment
                   AND ((x.kind = 'class' AND EXISTS (SELECT 1 FROM class_member cm WHERE cm.org_id = x.org_id AND cm.class_id = x.class_id
                                                       AND cm.user_id = p_user AND cm.left_at IS NULL))
                     OR (x.kind = 'self' AND x.owner_user_id = p_user))) THEN
    RETURN QUERY SELECT 'NOT_FOUND'::text, 0; RETURN;
  END IF;
  IF p_batch IS NOT NULL THEN
    INSERT INTO durable_request (org_id, request_id, kind) VALUES (p_org, p_batch, p_kind[1]) ON CONFLICT DO NOTHING;
    GET DIAGNOSTICS v_n = ROW_COUNT;
    IF v_n = 0 THEN RETURN QUERY SELECT 'DUPLICATE'::text, 0; RETURN; END IF;
  END IF;
  WITH ins AS (
    INSERT INTO event_log (occurred_at, org_id, assignment_id, user_id, kind, arg)
    SELECT LEAST(GREATEST(e.t, now() - interval '24 hours'), now()), p_org, p_assignment, p_user, e.k, e.a
    FROM unnest(p_occurred_at, p_kind, p_arg) AS e(t, k, a)
    RETURNING occurred_at, kind),
  o AS (SELECT ins.occurred_at, ins.kind, ins.occurred_at - lag(ins.occurred_at) OVER (ORDER BY ins.occurred_at) AS gap FROM ins)
  SELECT count(*), count(*) FILTER (WHERE o.kind = 'tool'), min(o.occurred_at), max(o.occurred_at),
         COALESCE(round(sum(extract(epoch FROM o.gap)) FILTER (WHERE o.gap < interval '30 minutes')), 0)::int
    INTO v_n, v_tools, v_min, v_max, v_active FROM o;
  PERFORM _progress_apply_batch(p_org, p_assignment, p_user, v_min, v_max, v_active, v_tools);
  RETURN QUERY SELECT 'OK'::text, v_n;
END $$;
COMMENT ON FUNCTION fn_record_batch(uuid, uuid, uuid, timestamptz[], event_kind[], smallint[], uuid) IS
  'FIX4b: lô mức A lũy đẳng bền theo batch_id (durable_request, BR-GEO-280) và thời gian theo cặp liên tiếp < 30 phút (BR-GEO-278).';

-- Bản 6 tham số cũ (scene_shown của E4, IT) giữ chữ ký, chuyển sang bản mới không lũy đẳng: cùng quy tắc thời gian.
CREATE OR REPLACE FUNCTION fn_record_batch(p_org uuid, p_user uuid, p_assignment uuid,
                                           p_occurred_at timestamptz[], p_kind event_kind[], p_arg smallint[])
RETURNS TABLE (status text, inserted integer)
LANGUAGE sql SECURITY DEFINER SET search_path = public, pg_temp
AS $$ SELECT * FROM fn_record_batch(p_org, p_user, p_assignment, p_occurred_at, p_kind, p_arg, NULL::uuid) $$;

REVOKE ALL ON FUNCTION _progress_apply_batch(uuid, uuid, uuid, timestamptz, timestamptz, int, int),
                       fn_record_batch(uuid, uuid, uuid, timestamptz[], event_kind[], smallint[], uuid) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION fn_record_batch(uuid, uuid, uuid, timestamptz[], event_kind[], smallint[], uuid) TO geo_app;
