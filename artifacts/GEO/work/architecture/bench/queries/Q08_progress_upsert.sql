-- role: owner
-- tx: rollback
-- Thân của geo._progress_apply (gọi từ mọi hàm ghi sự kiện): bộ đếm lũy đẳng với mức gợi ý (GREATEST) và giờ xem lời giải (COALESCE);
-- thời gian làm bài cộng độ dài lô + khoảng nghỉ tới hoạt động trước nếu < 30 phút (khoảng nghỉ >= 30 phút 0 giây mở phiên mới:
-- D-GEO-144, RV-17; trước đây BETWEEN tính cả đầu mút 30:00). Tham số: đầu lô = now() - 10 s, cuối lô = now().
:explain
INSERT INTO geo.assignment_progress AS ap
       (org_id, assignment_id, user_id, first_opened_at, last_active_at, hint_level_max, hint_view_count, ask_count, tool_count, active_seconds, solution_viewed_at)
VALUES (:'org', :'assignment', :'student', NULL, now(), 0, 0, 0, 50, 10, NULL)
ON CONFLICT (org_id, assignment_id, user_id) DO UPDATE SET
       first_opened_at    = COALESCE(ap.first_opened_at, EXCLUDED.first_opened_at),
       last_active_at     = GREATEST(ap.last_active_at, EXCLUDED.last_active_at),
       hint_level_max     = GREATEST(ap.hint_level_max, EXCLUDED.hint_level_max),
       hint_view_count    = ap.hint_view_count + EXCLUDED.hint_view_count,
       ask_count          = ap.ask_count + EXCLUDED.ask_count,
       tool_count         = ap.tool_count + EXCLUDED.tool_count,
       active_seconds     = ap.active_seconds + EXCLUDED.active_seconds
                            + CASE WHEN (now() - interval '10 seconds') - ap.last_active_at >= interval '0'
                                    AND (now() - interval '10 seconds') - ap.last_active_at <  interval '30 minutes'
                                   THEN extract(epoch FROM (now() - interval '10 seconds') - ap.last_active_at)::int ELSE 0 END,
       solution_viewed_at = COALESCE(ap.solution_viewed_at, EXCLUDED.solution_viewed_at),
       updated_at         = now();
