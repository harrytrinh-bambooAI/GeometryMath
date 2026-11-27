Phiên bản: v1.0 · 2026-11-27 · runner-content-bal (phiên chính ghi hộ)

# Cân chuyên đề Kho đề MVP: 46 → 84 đề
Thêm HKN-47…84: Góc +4 (THCS), Thể tích +8 (6 THPT), Diện tích +13, Mặt cầu +13. Khoảng cách giữ 20.

## Kết quả lệnh kiểm
- `verify_new.py`: 84/84 khớp, SAI = 0.
- `ContentPackTest` 86/86; `ContentPackMvpIT` 3/3 (0 "Cần xem lại"); `GeometryEngineE2Test` 22/22; `CatalogE5IT` 9/9.
- `make bench-mvp` (mock): OK, 4/4 Pass, `MVP-METRICS-20261127-0906/`.
- `make verify`: OK (đơn vị 353, tích hợp 253, 0 lỗi).

## Phân bố trước (46 đề)
| Dạng | Cấp | pyramid | prism | box | cube | cone | cylinder | sphere | Cộng |
|---|---|---|---|---|---|---|---|---|---|
| Góc | THCS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| Góc | THPT | 6 | 3 | 1 | 2 | 0 | 0 | 0 | 12 |
| Khoảng cách | THCS | 1 | 0 | 1 | 1 | 0 | 0 | 0 | 3 |
| Khoảng cách | THPT | 9 | 3 | 2 | 3 | 0 | 0 | 0 | 17 |
| Thể tích | THCS | 0 | 4 | 1 | 1 | 2 | 0 | 0 | 8 |
| Thể tích | THPT | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| Diện tích | THCS | 0 | 0 | 0 | 0 | 0 | 1 | 0 | 1 |
| Diện tích | THPT | 0 | 0 | 0 | 0 | 1 | 1 | 0 | 2 |
| Mặt cầu | THCS | 0 | 0 | 0 | 0 | 0 | 0 | 1 | 1 |
| Mặt cầu | THPT | 0 | 0 | 0 | 0 | 0 | 0 | 2 | 2 |

## Phân bố sau (84 đề)
| Dạng | Cấp | pyramid | prism | box | cube | cone | cylinder | sphere | Cộng |
|---|---|---|---|---|---|---|---|---|---|
| Góc | THCS | 1 | 0 | 0 | 1 | 2 | 0 | 0 | 4 |
| Góc | THPT | 6 | 3 | 1 | 2 | 0 | 0 | 0 | 12 |
| Khoảng cách | THCS | 1 | 0 | 1 | 1 | 0 | 0 | 0 | 3 |
| Khoảng cách | THPT | 9 | 3 | 2 | 3 | 0 | 0 | 0 | 17 |
| Thể tích | THCS | 1 | 4 | 1 | 1 | 2 | 1 | 0 | 10 |
| Thể tích | THPT | 2 | 2 | 0 | 0 | 1 | 1 | 0 | 6 |
| Diện tích | THCS | 1 | 2 | 1 | 0 | 2 | 2 | 0 | 8 |
| Diện tích | THPT | 2 | 1 | 0 | 0 | 2 | 3 | 0 | 8 |
| Mặt cầu | THCS | 0 | 0 | 0 | 0 | 0 | 0 | 7 | 7 |
| Mặt cầu | THPT | 3 | 2 | 1 | 0 | 0 | 0 | 3 | 9 |

Tổng theo dạng: Góc 16, Khoảng cách 20, Thể tích 16, Diện tích 16, Mặt cầu 16. Sinh lại bảng: `python3 -I distribution.py <catalog-mvp.json>`.

## Tự kiểm đáp số
`verify_new.py` dựng lại tọa độ bằng Python thuần và đo theo định nghĩa hình học (cầu ngoại tiếp tính bằng khử Gauss, khác công thức Java), so với biểu thức đáp số tại a = 1, 2, 3. Sau đó engine Java kiểm lại chuỗi đáp số thật của từng đề.

## Thay đổi engine và tiêu chí MVP (delivery-lead chấp nhận, DECISIONS 27/11)
1. Engine chỉ thêm phép đo, không đổi bộ dựng hay đại lượng cũ: `sphere_area`, `cyl_lateral`, `cyl_volume`, `cone_total_area`, `pyramid_volume`, `pyramid_lateral`, `prism_lateral`; ba loại mặt cầu nhận thêm bốn điểm không đồng phẳng (cầu ngoại tiếp); test `areaVolumeSphereKinds`. Prompt AI (`AnthropicPrompts`) chưa biết loại mới → việc tiếp theo trong inbox.
2. `CatalogService.MVP_MAX` 60 → 100 (kho 84 + 6 HKG = 90); `CatalogE5IT` sửa câu thông báo; MVP.md dòng 142, 200, 231 sửa 40–60 → 84.

## Nguồn tham khảo
Soạn theo dạng bài SGK Toán 8–9, 11, 12 và đề thi tốt nghiệp THPT công khai, không chép nguyên văn; lượt này không mở tài liệu cụ thể nào.

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.0 | 2026-11-27 | Bản đầu | runner-content-bal (phiên chính ghi tệp) |
| v1.1 | 2026-11-27 | Sửa 5 phát hiện soát độc lập: chip HKN-52, gợi ý HKN-69/71/75 thành câu hỏi dẫn, HKN-77 đổi sang thể tích cầu; engine prism_lateral kiểm lăng trụ đứng | runner-content-bal-fix |
