# Thư mục db

Migration thật nằm ở `server/src/main/resources/db/migration` (Flyway tìm theo quy ước này, nên không đặt ở đây).
Thư mục này dành cho kịch bản cơ sở dữ liệu ngoài migration:
- `seed/`: dữ liệu vàng (fixture) cho dev/E2E, nạp bằng `scripts/seed-reset.sh` — xem [seed/README.md](seed/README.md).

Cụm cơ sở dữ liệu riêng: `127.0.0.1:${GEO_PG_PORT:-54329}`, user `geo`; tạo `geo_dev`, `geo_test`, `geo_e2e` bằng `scripts/db-init.sh`.
