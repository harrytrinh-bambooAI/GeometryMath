# Lệnh thường dùng (chạy trong thư mục app/)
MVN = cd server && ./mvnw -B -ntp

.PHONY: up down db verify build test fmt run
up:      ; scripts/dev-up.sh && scripts/db-init.sh
down:    ; scripts/dev-down.sh
verify:  ; scripts/verify.sh            # build + lint + đơn vị + quét SQL + kiến trúc + tích hợp
build:   ; $(MVN) -DskipTests package
test:    ; $(MVN) test                  # đơn vị + quét SQL + kiến trúc (không cần hạ tầng)
fmt:     ; $(MVN) spotless:apply
run:     up ; $(MVN) spring-boot:run

.PHONY: loadtest-smoke e2e-smoke
loadtest-smoke: ; cd loadtest && ../server/mvnw -B -ntp -f pom.xml gatling:test   # cần máy chủ chạy ở 8080
e2e-smoke:      ; cd e2e && npm ci && npm run test:smoke                          # cần máy chủ chạy ở 8080
