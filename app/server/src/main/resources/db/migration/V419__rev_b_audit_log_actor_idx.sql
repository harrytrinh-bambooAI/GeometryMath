-- FIX lô soát độc lập T-20…T-32 (REVIEW-T20-T32-20261127 mục 7, Dev-REV-B2): ẩn danh hóa AuditEvent khi xóa / dọn tài khoản
-- (fn_account_purge_rows, V415/V416) chạy UPDATE audit_log … WHERE org_id = ? AND actor_user_id = ?. Trước đây chỉ có chỉ mục
-- một phần cho hàng denied (V386 audit_log_denied_idx) nên hàng thành công phải quét tuần tự cả bảng (bảng chỉ thêm, lớn dần).
-- Vế target_id đã có audit_log_target_idx (V10).
CREATE INDEX audit_log_actor_idx ON audit_log (org_id, actor_user_id);
