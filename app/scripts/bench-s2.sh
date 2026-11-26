#!/bin/bash
# Bộ đo hình học phẳng S2-E2 (bộ đề chuẩn, mock): ngưỡng ≥ 95 % thỏa ràng buộc, ≥ 90 % đáp số, 0 sai mang nhãn
# (epic-common v1.2). Gọi qua `make bench-s2`.
# CHỖ CẮM do T-261126-41 tạo: T-261126-19 (chủ tệp, PLAN_code-s23 §5) thay nội dung bằng kịch bản thật.
# Trước khi có kịch bản thật: báo chưa cài đặt và thoát mã 2 (không giả là đạt).
set -euo pipefail
echo "bench-s2: chưa cài đặt (chờ T-261126-19, S2-E2). Không có kết quả đo." >&2
exit 2
