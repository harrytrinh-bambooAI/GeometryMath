# Lệnh thường dùng (chạy trong thư mục app/)
# Cổng: biến môi trường > app/.env.ports (mỗi checkout/worktree một bản, không commit) > mặc định 54329/6389/8080/5173.
# Mọi lệnh nạp cổng qua scripts/env.sh (ENV dưới đây).
SHELL := /bin/bash
ENV = source scripts/env.sh &&
MVN = cd server && ./mvnw -B -ntp

.PHONY: up down db verify build test fmt run ports
up:      ; scripts/dev-up.sh && scripts/db-init.sh
down:    ; scripts/dev-down.sh
verify:  ; scripts/verify.sh            # build + lint + đơn vị + quét SQL + kiến trúc + tích hợp
build:   ; $(MVN) -DskipTests package
test:    ; $(MVN) test                  # đơn vị + quét SQL + kiến trúc (không cần hạ tầng)
fmt:     ; $(MVN) spotless:apply
run:     up ; $(ENV) $(MVN) spring-boot:run -Dspring-boot.run.profiles=dev   # profile dev tường minh (không còn mặc định)
ports:   ; @$(ENV) echo "PG $$GEO_PG_PORT · Redis $$GEO_REDIS_PORT · máy chủ $$GEO_PORT · web $$GEO_WEB_PORT"

.PHONY: bench-mvp
bench-mvp: ; scripts/bench-mvp.sh   # bộ đo MVP.md §9 (mock mặc định; GEO_AI_PROVIDER=anthropic GEO_AI_API_KEY=… đo AI thật) -> artifacts/GEO/evidence/MVP-METRICS-*/

.PHONY: loadtest-smoke e2e-smoke bench-seed
bench-seed:     ; scripts/bench-seed.sh $(or $(DB),geo_bench)   # dữ liệu thử tải US-GEO-108 vào CSDL riêng geo_bench
loadtest-smoke: ; cd loadtest && ../server/mvnw -B -ntp -f pom.xml gatling:test   # cần máy chủ chạy ở 8080 (-Dgeo.baseUrl=...)
e2e-smoke:      ; $(ENV) cd e2e && npm ci && GEO_E2E_RESET=0 npm run test:smoke   # chỉ dự án api, vào máy chủ đang chạy ở GEO_PORT

# Giao diện (app/web) và E2E có bằng chứng (app/e2e)
.PHONY: web-install web-build web-dev e2e e2e-report e2e-reset traceability
web-install:  ; cd web && npm ci
web-build:    ; cd web && npm run typecheck && npm run lint && npm test && npm run build
web-dev:      ; $(ENV) cd web && npm run dev       # Vite ở GEO_WEB_PORT, proxy -> máy chủ GEO_PORT (chạy `make run` song song)
e2e:          ; scripts/e2e.sh $(ARGS)             # bật máy chủ + web riêng, reset seed, chạy api + ui, sinh e2e/evidence/<run>/
e2e-report:   ; @scripts/e2e-report.sh             # in đường dẫn và mở báo cáo bằng chứng của lượt mới nhất
e2e-reset:    ; scripts/seed-reset.sh $(or $(DB),geo_e2e)   # đưa geo_e2e (hoặc DB=geo_dev) về trạng thái vàng
traceability: ; cd e2e && node scripts/traceability.ts $(ARGS)   # ARGS="--all" hoặc "--epic E1"
