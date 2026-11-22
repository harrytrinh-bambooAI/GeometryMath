-- role: app
-- lint-allow: R-Q04 (tra khóa chính: 1 hàng)
-- Xem trước "như học sinh" một bài ĐÃ GIAO (US-GEO-607, D-GEO-139, RV-27): đúng dữ liệu của Q04 (đề, hình, đại lượng ẩn) và trạng thái
-- giờ mở của CHÍNH bài giao đó; KHÔNG gọi hàm lời giải (D-GEO-153), không ghi sự kiện, không tính tiến độ (D-GEO-120). Kiểm giáo viên của lớp.
-- Đề CHƯA giao (bước 3 của giao bài): dùng Q19 (không lời giải) + giờ đang chọn do giao diện giữ, không có truy vấn trạng thái (D-GEO-146).
:explain
SELECT a.id, a.release_at, a.release_rev,
       CASE WHEN a.release_at IS NULL THEN 'unset' WHEN a.release_at > now() THEN 'scheduled' ELSE 'open' END AS release_state,
       a.allow_hint, a.allow_qa,
       pv.id AS problem_version_id, pv.statement, pv.verify_status, pv.approval_status, pv.ai_authored,
       p.title, p.solid_kind, sb.coord_source, sb.schema_version, sb.spec, sb.coords, sb.hidden_measures
FROM geo.assignment a
JOIN geo.class c            ON c.org_id = a.org_id AND c.id = a.class_id AND c.teacher_id = :'teacher'
JOIN geo.problem_version pv ON pv.org_id = a.content_org_id AND pv.id = a.problem_version_id
JOIN geo.problem p          ON p.org_id = pv.org_id AND p.id = pv.problem_id
CROSS JOIN LATERAL (
    SELECT s.coord_source, s.schema_version, s.spec, s.coords, s.hidden_measures
    FROM geo.scene_build s
    WHERE s.org_id = pv.org_id AND s.problem_version_id = pv.id
    ORDER BY s.build_no DESC LIMIT 1) sb
WHERE a.org_id = :'org' AND a.id = :'assignment' AND a.kind = 'class';
