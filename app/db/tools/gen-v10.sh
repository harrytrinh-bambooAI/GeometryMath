#!/bin/bash
# Sinh migration V10 từ lược đồ đề xuất của ARCH (artifacts/GEO/work/architecture/03_schema.sql).
# Chạy lại khi ARCH sửa lược đồ TRƯỚC khi V10 được áp dụng ở môi trường thật; sau đó chỉ thêm V11+.
# Khác bản ARCH: schema public thay cho geo; platform_org_id() = ...f001 (…0001 là F-GEO-001 của fixture);
# bỏ REVOKE ALL ON SCHEMA (schema public dùng chung với Flyway).
set -euo pipefail
cd "$(dirname "$0")/../../.."
SRC=artifacts/GEO/work/architecture/03_schema.sql
OUT=app/server/src/main/resources/db/migration/V10__schema_mvp.sql
{
  echo "-- V10: lược đồ chính thức MVP, sinh bằng app/db/tools/gen-v10.sh từ $SRC (đề xuất ARCH, bản 22/11/2026)."
  echo "-- Khác bản ARCH: schema public (seed E2E của khung làm việc trên public); platform_org_id() = ...f001 vì ...0001"
  echo "-- là F-GEO-001 \"THPT Mẫu A\" trong app/db/seed/fixture-ids.json; bỏ REVOKE ALL ON SCHEMA."
  sed -e '/^CREATE SCHEMA IF NOT EXISTS geo;/d' \
      -e '/^SET search_path = geo, public;/d' \
      -e 's/SET search_path = geo, pg_temp/SET search_path = public, pg_temp/g' \
      -e 's/geo\.platform_org_id()/platform_org_id()/g' \
      -e "s/'geo\.%I_default'/'public.%I_default'/" \
      -e "s/to_regclass('geo\.' || nm)/to_regclass('public.' || nm)/" \
      -e 's/CREATE TABLE geo\.%I PARTITION/CREATE TABLE public.%I PARTITION/' \
      -e "s/ensure_monthly_partitions('geo\./ensure_monthly_partitions('public./" \
      -e 's/00000000-0000-7000-8000-000000000001/00000000-0000-7000-8000-00000000f001/g' \
      -e '/^REVOKE ALL ON SCHEMA geo FROM PUBLIC;/d' \
      -e 's/ON SCHEMA geo TO/ON SCHEMA public TO/' \
      -e 's/IN SCHEMA geo/IN SCHEMA public/g' \
      -e 's/ON geo\.%I FOR ALL/ON public.%I FOR ALL/g' \
      -e 's/geo\.fn_get_solution/fn_get_solution/g' \
      -e 's/^ANALYZE;$/-- ANALYZE bỏ khỏi migration (chạy trong giao dịch của Flyway)./' \
      "$SRC"
} > "$OUT"
echo "đã sinh $OUT"
