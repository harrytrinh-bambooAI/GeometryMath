-- role: owner
-- tx: rollback
-- Câu INSERT bên trong geo.fn_record_batch: lô 50 sự kiện mức A của MỘT học sinh, một câu lệnh, CÓ kiểm tư cách lớp (RV-26) bằng
-- EXISTS không tương quan (tính một lần). Thời điểm thiết bị gửi bị kẹp vào [now() - 24 giờ, now()] (ADR-GEO-06).
-- geo_app không INSERT event_log trực tiếp (D-GEO-158): chạy với vai chủ như thân hàm SECURITY DEFINER.
:explain
INSERT INTO geo.event_log (occurred_at, org_id, assignment_id, user_id, kind, arg)
SELECT LEAST(GREATEST(e.t, now() - interval '24 hours'), now()), :'org', :'assignment', :'student', e.k, e.a
FROM unnest(ARRAY(SELECT now() - g * interval '200 milliseconds' FROM generate_series(1, 50) g),
            ARRAY(SELECT 'tool'::geo.event_kind FROM generate_series(1, 50) g),
            ARRAY(SELECT (1 + g % 7)::smallint FROM generate_series(1, 50) g)) AS e(t, k, a)
WHERE EXISTS (SELECT 1 FROM geo.assignment x WHERE x.org_id = :'org' AND x.id = :'assignment'
                AND ((x.kind = 'class' AND EXISTS (SELECT 1 FROM geo.class_member cm WHERE cm.org_id = x.org_id AND cm.class_id = x.class_id
                                                    AND cm.user_id = :'student' AND cm.left_at IS NULL))
                  OR (x.kind = 'self' AND x.owner_user_id = :'student')));
