-- role: app
-- tx: rollback
-- lint-allow: R-Q04 (hàm trả 1 hàng)
-- Gọi hàm đầy đủ như ứng dụng: geo.fn_get_hint đọc gợi ý mức 1 VÀ ghi sự kiện 'hint' + khóa lũy đẳng + tiến độ (một giao dịch, mức B).
:explain
SELECT status, level, hint_source FROM geo.fn_get_hint(:'org', :'student', :'assignment', 1::smallint, gen_random_uuid());
