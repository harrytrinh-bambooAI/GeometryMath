Phiên bản: v1.0 · 2026-11-27 · runner-e2e-split2 (phiên chính ghi tệp)

# E2E tách UI / API đợt 2

Nhánh `e2e-split2`, commit 3d4e95d (đã merge wave0 879f7d7). Theo DECISIONS 27/11 "Chiến lược E2E" và "E2E tách UI/API".

## Kết quả lệnh kiểm
| Lệnh | Kết quả |
|---|---|
| `make e2e-affected-selftest` | ✅ 7/7 (thêm kiểm `--grep-invert` chỉ loại ca B ở project UI/mobile) |
| `npx tsc --noEmit` (e2e) | ✅ |
| `make verify` | ✅ VERIFY OK, IT 253/253 |
| `make e2e` trọn bộ một lượt (split2-full) | ❌ exit 2: 1794/1800 đạt, 2 trượt, 3 flaky, 4 bỏ qua; 935/936 mã TC đạt; 506 s (đợt 1: 461 s, tải máy khác) |
| `make traceability ARGS="--strict --all"` | phủ không giảm (1187 TC có test, 936 mã TC e2e); exit 2 do TC-GEO-0484 trượt và TC-S3E3-58, TC-S3E5-61 chờ nhánh fix-po27 |

Trượt: TC-GEO-0484 (`e4/inherit.ui.spec.ts`, ui-chromium + ui-webkit) — "Đề này chưa dựng được bằng hình mẫu…" với đề F235; nghi hồi quy từ gộp content-bal 879f7d7, không thuộc phần đổi của đợt này → giao nhánh `fix-0484`.
Flaky (đạt khi thử lại, ngoài phần đổi): TC-GEO-0469 (ui-webkit), TC-S3E1-12 (ui-webkit), TC-GEO-0539 (mobile-iphone).

## Chuyển ca
- Rà tay đủ 32 ca B (đọc thân từng ca).
- 11 ca chuyển sang `*.flow.api.spec.ts` + ảnh trang cuối, xoá khỏi spec UI: TC-GEO-0102, 0481, 0493, 0507, 0516, 0670, 0703, 0720, TC-S2E8-076, TC-S2E8-107, TC-S3E3-14.
- 32 ca `@loai-B` chạy riêng trên project api: 32/32 đạt, 29 s. Số test UI 538 → 527.
- Còn 1 ca B chạy UI để sau fix-po27: TC-S3E5-53; `s3e5/account.flow.api.spec.ts` chưa gắn `@loai-B`. Không spec nào có `hs=`.

## Đổi loại B → A (20 ca; lý do chi tiết ở bảng phân loại v1.1, mục "Ca đổi loại ở rà tay đợt 2")
- 6 ca giữ chỗ bấm cho ca B đợt 1: TC-S3E1-05, 06, 45, TC-S3E2-01, 02, TC-S3E6-17.
- 14 ca kiểm hành vi trình duyệt (ẩn-hiện, 3D, mất mạng, đồng hồ thiết bị, toast, nhịp hỏi lại, form): TC-GEO-0118, 0410, 0473, 0539, 0609, 0663, 0689, 0697, TC-S2E1-030, TC-S2E3-026, TC-S2E7-005, TC-S2E7-023, TC-S2E9-043, TC-S3E5-16.

## e2e-affected
Diff chỉ server: `--grep-invert` đổi thành `^(ui|mobile)-[a-z]+ .*@loai-B\b` để không loại nhầm ca B chạy API.

## TẠM / còn lại
- Nguyên nhân TC-GEO-0484 (tin cậy cao, nguồn error-context lượt split2-full) → nhánh fix-0484.
- Thời gian trước/sau không so chặt được (tải máy khác nhau).
- 506 ca A đợt 1 vẫn theo heuristic, chưa rà tay.
- Sau khi gộp fix-po27: quyết TC-S3E5-53 (chuyển B hoặc giữ A), gắn `@loai-B` cho `s3e5/account.flow`.

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.0 | 2026-11-27 | Bản đầu từ báo cáo runner-e2e-split2 | phiên chính ghi tệp |
