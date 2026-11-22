-- role: owner
-- tx: rollback
-- Hai câu lệnh bên trong geo.fn_set_release (khóa một hàng, rồi cập nhật; thêm một INSERT audit_log). Câu khóa không lọc theo giáo viên
-- và release_rev nữa để hàm phân biệt NOT_FOUND / FORBIDDEN / CONFLICT_REV / CONFLICT_ALREADY_OPEN (RV-21).
:explain
SELECT a.release_at, a.release_rev, c.teacher_id
FROM geo.assignment a JOIN geo.class c ON c.org_id = a.org_id AND c.id = a.class_id
WHERE a.org_id = :'org' AND a.id = :'assignment' AND a.kind = 'class'
FOR UPDATE OF a;
:explain
UPDATE geo.assignment SET release_at = now(), release_rev = release_rev + 1 WHERE org_id = :'org' AND id = :'assignment';
