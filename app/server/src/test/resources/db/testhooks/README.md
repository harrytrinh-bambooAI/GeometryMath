# Móc kiểm thử SQL (D-GEO-169, RV-GEO-24)

Thư mục này CHỈ nằm trên classpath kiểm thử (`src/test/resources`), nên không bao giờ vào gói chạy thật.
Profile `test` (`application-test.yml`) thêm `classpath:db/testhooks` vào `spring.flyway.locations`;
các profile khác không nạp.

Quy ước (ARCH viết nội dung SQL, Code chỉ giữ cơ chế):
- Dùng migration lặp lại của Flyway: `R__<thứ tự>_<mô_tả>.sql` (ví dụ `R__100_fn_test_set_release.sql`).
  Flyway chạy chúng SAU mọi migration có phiên bản `V…`, và chạy lại khi nội dung đổi.
- Viết `CREATE OR REPLACE` để chạy lại được; tên hàm có tiền tố `fn_test_` để dễ grep.
- Không đặt số phiên bản `V…` ở đây: lịch sử Flyway của `geo_test` sẽ lệch với môi trường thật.
- `R__000_testhooks_marker.sql` chỉ là dấu hiệu để kiểm thử cơ chế (`TestHooksIT`); giữ nguyên.
