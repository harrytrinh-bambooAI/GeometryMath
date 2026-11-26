-- S3-E3 (T-261126-30) · dữ liệu riêng cho Kết quả lớp và "Cả lớp vướng ở đâu" (US-S3E3-01…03, C3-04). Chạy sau 920_s3e2_core.sql;
-- không sửa dữ liệu của task khác. Mã cố định: app/db/seed/fixture-ids.d/s3e3.json.
-- Tổ chức riêng kiểu 'group' "Nhóm thử S3-E3": mỗi ca kiểm dựng Cô Lan / Thầy Khôi / lớp / học sinh / bộ bài / bảng trạng thái
-- mới qua /test-hooks/s3e3/sandbox (bảng F-S3-051 riêng cho từng ca, không làm bẩn bộ F-S3-050 dùng chung). Bộ dựng dùng
-- các đề phẳng BOB-01…22 của bộ đề 's3e2_kho' (seed 920: Đề 1 SC-đáy … Đề 4 thể tích + 18 đề thêm, đều có gợi ý mức 1).
INSERT INTO org (id, parent_id, kind, name) VALUES
  ('00000000-0000-7000-8003-000000930001', '00000000-0000-7000-8000-00000000f001', 'group', 'Nhóm thử S3-E3');
INSERT INTO catalog_bank_access (org_id, bank) VALUES ('00000000-0000-7000-8003-000000930001', 's3e2_kho');
