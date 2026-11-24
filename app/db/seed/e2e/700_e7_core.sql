-- E7 (Theo dõi lớp và ghi sự kiện): tổ chức thử riêng "Nhóm thử E7" (kiểu 'group', như E3, E4: không đổi số trường/lớp/
-- học sinh của THPT Mẫu A mà E1 đếm). Giáo viên, lớp, học sinh, bài giao, tiến độ và câu hỏi của từng ca kiểm do móc
-- POST /test-hooks/e7/sandbox tạo khi chạy (mỗi ca × mỗi dự án Playwright một giáo viên riêng), dựng lại đúng dữ liệu
-- F-GEO-050…058 của fixture §4 khi cần, nên ca đổi trạng thái (Hà mở bài, Mở ngay, hỏi thêm) không giẫm nhau và không
-- giẫm dữ liệu vàng.
INSERT INTO org (id, parent_id, kind, name) VALUES
  ('00000000-0000-7000-8000-000000e70001', '00000000-0000-7000-8000-00000000f001', 'group', 'Nhóm thử E7');
