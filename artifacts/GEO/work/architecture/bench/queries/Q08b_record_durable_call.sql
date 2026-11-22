-- role: app
-- tx: rollback
-- lint-allow: R-Q04 (hàm trả 1 giá trị)
-- Gọi hàm như ứng dụng: sự kiện bền 'unlock' sau khi xem lời giải (cổng + khóa lũy đẳng request_id + sự kiện + tiến độ, D-GEO-162, RV-18).
:explain
SELECT geo.fn_record_durable(:'org', :'student', :'assignment', gen_random_uuid(), 'unlock');
