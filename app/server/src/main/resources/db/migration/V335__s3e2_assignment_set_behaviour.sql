-- S3-E2 (T-261126-29, PLAN_code-s23 A4; PO-66 G1, G5; US-S3E2-01…04). KHÔNG đổi schema: bộ bài S3 dùng đúng
-- assignment_set / assignment_item của V180 (S2-E8). Chỉ thêm quyền cột cho hành vi mới:
--   * bộ Nháp chọn / đổi lớp trước khi giao (US-S3E2-01 SC1, FC4): geo_app cần UPDATE class_id. Bộ đã giao không đổi lớp
--     được vì mọi câu lệnh sửa của ứng dụng lọc status = 'draft' (và fn_assign_practice_set ghi class_id lúc giao).
-- Đóng / lưu trữ (status, closed_at, archived_at) và hạn làm due_at đã có quyền từ V180.
GRANT UPDATE (class_id) ON assignment_set TO geo_app;

COMMENT ON COLUMN assignment_set.due_at IS 'Hạn làm (S3-E2, BR-S3E2-03): CHỈ là nhãn "Quá hạn", không khóa gì.';
