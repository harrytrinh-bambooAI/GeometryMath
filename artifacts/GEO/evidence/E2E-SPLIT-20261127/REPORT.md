# E2E tách UI / API · đợt 1 · 27/11/2026

Phiên bản: v1.0 · 27/11/2026 · nguồn: báo cáo runner-e2e-split (nhánh `e2e-split`, commit 9bbd4b5, 5143ed6), theo DECISIONS 27/11 "Chiến lược E2E".

**Kết luận:** đợt 1 chuyển 26/58 ca loại B sang luồng API + ảnh trang cuối. Lượt trọn bộ đạt (0 trượt) nhưng chưa nhanh hơn; lượt affected khi diff chỉ đụng server nhanh hơn 41 %. Còn 32 ca B chưa chuyển.

## Số ca
| Hạng mục | Trước | Sau |
|---|---|---|
| Test UI (`*.ui.spec.ts`) | 564 | 538 (506 loại A + 32 loại B chưa chuyển) |
| Ca B đã chuyển sang `*.flow.api.spec.ts` | 0 | 26 (S3-E1 12, S3-E2 8, S3-E5 1, S3-E6 2, E1 3) |
| Tổng test lượt trọn bộ | 1851 | 1825 |

Ảnh trang cuối dùng helper `app/e2e/support/final-state.ts`; ảnh mẫu ở `anh-trang-cuoi/`. Bảng phân loại: `artifacts/GEO/work/s23/e2e-ui-classification.md`. Phần 506 ca A và 32 ca B còn lại phân loại theo heuristic từ mã nguồn, cần QC/BA rà tay.

## Thời gian
| Lượt | Trước | Sau |
|---|---|---|
| Trọn bộ `make e2e` | 447 s (t33-r1) | 461 s (split-full) |
| Affected, diff chỉ server (iam) | 44 s, 250 test | 26 s, 232 test, không mobile (−41 %) |

## Ca chập chờn
- `retries: 1` chỉ cho dự án UI/mobile; api giữ 0. Reporter ghi `totals.flaky` + `flakyTests[]` và nhãn FLAKY trên trang HTML.
- Lượt trọn bộ split-full: 1821/1825 đạt, 0 trượt, 4 bỏ qua, 1 flaky (TC-S2E1-030 ui-webkit, đạt lần thử 2).

## Kiểm
| Lệnh | Kết quả |
|---|---|
| `make e2e-affected-selftest` | ✅ 7/7 (thêm 3 mẫu: chỉ server, server + web, migration) |
| `cd e2e && npx tsc --noEmit` | ✅ |
| `make e2e` trọn bộ | ✅ mã thoát 0 |
| Traceability e2e | ✅ cùng 936 mã TC như T-33, không mất mã nào |
| `make traceability --strict --all` | ❌ thoát 2: chưa chạy `make verify` trong lượt này (255 TC IT/UT "chưa chạy"); TC-S3E3-58, TC-S3E5-61 chưa có test (mới thêm theo PO-DECISIONS, ngoài phạm vi) |

## Còn lại
- Chuyển 32 ca B còn lại, gắn `@loai-B` để affected không chạy UI cho chúng.
- QC/BA rà tay bảng phân loại A/B.
