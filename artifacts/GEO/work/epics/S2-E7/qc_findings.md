Phiên bản: v0.1 · 2026-11-26
# S2-E7 — phát hiện của QC / Dev

## Dev (runner-T-261126-23)

| Mã | Nguồn | Phát hiện | Cách làm TẠM (quyết định · tin cậy · nguồn) | Ai xử lý tiếp |
|---|---|---|---|---|
| Dev-S2E7-01 | US-S2E7-01-SC1, FC1 | Lối vào "Trình chiếu" theo bản mẫu nằm ở C2-03 (`features/s2e5`, ngoài phạm vi tệp của task). | Nút "Trình chiếu" ở màn đề của Kho đề (`/gv/kho-de-cua-toi/de/:id`); Esc / Hủy về màn đề này (có nút "Soát lời giải" sang C2-03). Thêm nút ở C2-03 là một dòng `navigate('/gv/trinh-chieu/:id')`. TẠM · vừa · PLAN §5 | T-22 / FIX (chủ s2e5) |
| Dev-S2E7-02 | BR-S2E7-07…10, PLAN §5 luật 4 | Không cần PWA / service worker: dữ liệu buổi chiếu tải MỘT lần vào bộ nhớ trang (phản hồi no-store), mất mạng giữa giờ vẫn chiếu; thoát là bỏ dữ liệu, không ghi cache / lưu trữ cục bộ. Không đổi `vite.config.ts`, `package.json`. | TẠM · cao · QA-S2E7-03 | — |
| Dev-S2E7-03 | US-S2E7-02-FC3 | "Đang tải sẵn 2 trong 4 bước" không áp dụng: tải một lần cả buổi chiếu. | Mất mạng sau bước hỏi lại, trong lúc tải dữ liệu chiếu → "Chưa tải sẵn đủ, cần mạng" + "Thử lại"; mất mạng trước khi mở → "Cần mạng để mở lần đầu". TẠM · vừa | QC |
| Dev-S2E7-04 | US-S2E7-02-FC1, SC3 | Hỏi đáp AI cho giáo viên trong Trình chiếu chưa có đường AI (AI thật chờ B7; mock của S2-E3/S2-E4). | Nút "Hỏi đáp AI" mở khung giả lập, không gửi yêu cầu; mất mạng thì tắt kèm thông báo, có mạng lại bật trong ≤ 5 giây (sự kiện online + kiểm lại mỗi 1 giây, `geo.s2e7.reconnect-seconds`). TC-S2E7-019 (IT) kiểm cấu hình hạn bật lại máy chủ cấp cho màn chiếu; hành vi bật lại kiểm ở TC-S2E7-018 (E2E-UI) và test đơn vị `features/s2e7/logic.test.ts`. TẠM · vừa | T-22 / T-24 nối AI |
| Dev-S2E7-05 | BR-S2E7-11 | "Đề đang giao": bài giao lớp của chính giáo viên cùng version đề kho với sổ lời giải, hoặc đã ghim sổ này (`solution_assignment_pin`), chưa lưu trữ. Đọc chéo `assignment`, `class` (read model, `sqllint:allow=ownership`). | TẠM · vừa · ADR-GEO-01 | L4 |
| Dev-S2E7-06 | US-S2E7-01-SC2 | Phần hình của một bước suy từ chữ của bước (đoạn hai chữ hoa, góc ba chữ hoa / "góc XYZ", mặt "(ABCD)"); chưa có dữ liệu hình riêng cho từng bước. | TẠM · vừa | S2-E5 (dữ liệu bước) |
| Dev-S2E7-07 | US-S2E7-01-FC4 | Đề phẳng chưa có cảnh vẽ trong Trình chiếu (`FigureFacts.scene` của S2-E5 chỉ dựng đề không gian). | Khung "Hình phẳng của đề" giữ chỗ. TẠM · vừa | T-19/T-25 (scene2d) |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Mục Dev đầu tiên | runner-T-261126-23 |
