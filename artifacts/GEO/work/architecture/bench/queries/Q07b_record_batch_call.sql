-- role: app
-- tx: rollback
-- lint-allow: R-Q04 (hàm trả 1 hàng)
-- Gọi hàm như ứng dụng: geo.fn_record_batch (Q07 + Q08 trong một lần gọi) với lô 50 sự kiện 'tool'.
:explain
SELECT status, inserted FROM geo.fn_record_batch(:'org', :'student', :'assignment',
       ARRAY(SELECT now() - g * interval '200 milliseconds' FROM generate_series(1, 50) g),
       ARRAY(SELECT 'tool'::geo.event_kind FROM generate_series(1, 50) g),
       ARRAY(SELECT (1 + g % 7)::smallint FROM generate_series(1, 50) g));
