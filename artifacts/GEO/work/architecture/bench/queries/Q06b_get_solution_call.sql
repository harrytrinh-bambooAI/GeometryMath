-- role: app
-- lint-allow: R-Q04 (hàm trả đúng 1 hàng)
-- Gọi hàm như ứng dụng (đường nóng nhất ở đỉnh giờ mở): geo.fn_get_solution cho bài lớp đã mở. Trả status + nội dung.
:explain
SELECT status, problem_version_id, release_rev FROM geo.fn_get_solution(:'org', :'student', :'assignment');
