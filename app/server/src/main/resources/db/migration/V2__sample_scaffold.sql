-- BẢNG MẪU của lát cắt dọc khung. Xóa cùng gói vn.geo.server.sample khi có schema thật.
CREATE TABLE sample_item (
    id         uuid PRIMARY KEY,
    org_id     uuid NOT NULL,
    title      text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);
-- Chỉ mục khớp đúng thứ tự phân trang theo khóa: lọc trường, sắp xếp (created_at, id) giảm dần.
CREATE INDEX sample_item_org_created_idx ON sample_item (org_id, created_at DESC, id DESC);
