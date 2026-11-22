-- DỮ LIỆU KHUNG, KHÔNG PHẢI FIXTURE: chỉ để chứng minh cơ chế seed + reset + cách ly trường chạy được
-- trên bảng mẫu sample_item (V2). Xóa tệp này cùng gói vn.geo.server.sample khi có bảng thật.
-- Mã trường lấy từ fixture-ids.json (F-GEO-001, F-GEO-002). Mốc giờ tương đối T0 (:'t0').
INSERT INTO sample_item (id, org_id, title, created_at) VALUES
  ('00000000-0000-7000-8000-000000900001', '00000000-0000-7000-8000-000000000001', 'Mẫu A · 1', :'t0'::timestamptz - interval '3 minutes'),
  ('00000000-0000-7000-8000-000000900002', '00000000-0000-7000-8000-000000000001', 'Mẫu A · 2', :'t0'::timestamptz - interval '2 minutes'),
  ('00000000-0000-7000-8000-000000900003', '00000000-0000-7000-8000-000000000001', 'Mẫu A · 3', :'t0'::timestamptz - interval '1 minute'),
  ('00000000-0000-7000-8000-000000900004', '00000000-0000-7000-8000-000000000002', 'Mẫu C · 1', :'t0'::timestamptz - interval '1 minute');
