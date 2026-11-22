# Thử tải (Gatling Java DSL, D-GEO-170)

Dự án Maven riêng (`loadtest/pom.xml`), dùng Maven Wrapper của máy chủ. Không nằm trong `scripts/verify.sh`.

## Có gì
- `HealthSmokeSimulation`: kịch bản KHÓI tới `/health` (mặc định 5 yêu cầu/giây trong 5 giây; khẳng định 0% lỗi,
  p95 < 500 ms). Chỉ chứng minh bộ công cụ chạy được, KHÔNG phải thử tải chính thức.
  Đã chạy thử với máy chủ ở cổng 18080: 25/25 OK, p95 10 ms (máy dev, chỉ mang tính chỉ báo).

## Chạy
```
cd app && make run                                   # hoặc java -jar ... (cụm 54329/6389)
cd app/loadtest
../server/mvnw -f pom.xml gatling:test               # mặc định http://127.0.0.1:8080
../server/mvnw -f pom.xml gatling:test -Dgeo.baseUrl=http://127.0.0.1:18080 -Dgeo.rps=20 -Dgeo.seconds=30
```
Báo cáo HTML ở `target/gatling/`.

## Còn phải viết (khi có endpoint và schema thật)
- Kịch bản đọc (phân trang theo khóa), ghi (nộp bài, lô sự kiện), hỗn hợp theo `artifacts/GEO/work/architecture/05_capacity.md` §2;
  ngưỡng đạt/không đạt lấy từ NFR-GEO-001…012 (giữ nhãn "đề xuất").
- Dữ liệu: nạp nhiều trường, mỗi trường nhiều học sinh; thẻ phát bởi `TokenService` (không dùng `/dev/token` ngoài dev).
- Ghi kèm: `hikaricp_connections_pending`/`active`, tỉ lệ 429/503 theo mã (`RATE_LIMITED`, `SERVICE_OVERLOADED`),
  CPU/RAM JVM, `pg_stat_statements`, plan truy vấn nóng.

Lưu ý: số đo trên máy dev chỉ để so sánh tương đối, không thay cho thử tải trên cấu hình môi trường thật.
