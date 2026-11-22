# Thư mục db

Migration thật nằm ở `server/src/main/resources/db/migration` (Flyway tìm theo quy ước này, nên không đặt ở đây).
Thư mục này dành cho tài liệu/kịch bản cơ sở dữ liệu ngoài migration (ví dụ dữ liệu nạp thử). Hiện trống.
Cụm cơ sở dữ liệu riêng: `127.0.0.1:54329`, user `geo`; tạo `geo_dev` và `geo_test` bằng `scripts/db-init.sh`.
