-- role: app
-- lint-allow: R-Q04 (hàm trả đúng 1 hàng)
-- Gọi hàm cho bài tự học đang kế thừa khóa (đã xem Gợi ý 2): cổng chạy đủ nhánh tự học gồm Q16 => status LOCKED (D-GEO-151).
:explain
SELECT status, problem_version_id FROM geo.fn_get_solution(:'inh_org', :'inh_student', :'inh_assignment');
