# E2 — thiết kế kiểm thử (hình đúng: dựng hình, hình mẫu, kiểm chứng, nhãn)
> Chủ sở hữu: QC. Phần chung (danh mục CL, kỹ thuật, bảng quyết định khóa lời giải DT-n, biên giờ mở BV-n, thang rủi ro, dữ liệu `F-GEO-200…299`, bảng giải thích mã) ở `review/QC_test_design.md`; tệp này chỉ ghi phần riêng của epic. Lập từ `work/04_story_map.md` và `work/03_gate3_preliminary_stories.md`, không chờ AC; dòng thiết kế nào chưa có AC tương ứng sẽ được đối chiếu ở `qc_findings.md`.

## Applicability map (mỗi nhóm CL: Y / N kèm lý do)
| Group | Screen / story | Applies (Y/N) | Reason if N |
|---|---|---|---|
| CL-FN-01 Luồng chính | US-GEO-201…205 | Y | |
| CL-FN-02 Dữ liệu nhập | US-GEO-202 (tham số hình mẫu), 201 (ràng buộc) | Y | |
| CL-FN-03 Lỗi và thông báo | US-GEO-201 (ràng buộc mâu thuẫn), 202, 204 | Y | |
| CL-FN-04 Chuyển trạng thái | US-GEO-203 (nhãn tính lại khi engine đổi) | Y | |
| CL-FN-05 Phân quyền theo vai | US-GEO-203 (giáo viên không tự gắn nhãn máy, BR-GEO-14) | Y | |
| CL-FN-06 Cách ly trường | — | N | engine thuần hàm, không đọc dữ liệu trường; cách ly kiểm ở E1 |
| CL-FN-07 Dữ liệu máy chủ trả về | US-GEO-203 (báo cáo kiểm chứng chứa giá trị tính) | Y | báo cáo kiểm chứng không được tới học sinh khi bài khóa; case đặt ở E6 |
| CL-FN-08 Phân trang | — | N | không có danh sách |
| CL-FN-09 Thời gian | — | N | không có quy tắc theo giờ |
| CL-FN-10 Đồng thời | — | N | engine thuần hàm (REQ-GEO-46) |
| CL-FN-11 Nhãn kiểm chứng | US-GEO-203, 204 | Y | |
| CL-FN-12 Định dạng | US-GEO-203, 204 (chuỗi nhãn, số dấu phẩy) | Y | |
| CL-FN-13 Sự kiện | — | N | không có hành động người dùng cần ghi |
| CL-FN-14 Cổng AI | US-GEO-201, 204 (ràng buộc và tọa độ từ AI giả lập) | Y | |
| CL-UI-01 Thiết bị | US-GEO-202, 204 | Y | hiển thị hình và cảnh báo trên iPad, iPhone |
| CL-UI-02 Chuỗi hiển thị | US-GEO-203 ("Đã kiểm chứng", không "Hình đã kiểm chứng", D-GEO-079) | Y | |
| CL-UI-03 Ngoài phạm vi | US-GEO-204 (đề hình phẳng: chưa hỗ trợ, REQ-GEO-51) | Y | |
| CL-UI-04 Gián đoạn | — | N | không có thao tác dài |
| CL-UI-05 Cảm ứng | — | N | thao tác hình ở E3 |
| CL-X-01…04 | — | N | không có truy vấn nóng, không có dữ liệu cá nhân |

## Design rows: `CL id · condition · expected rule · technique · level`
| CL id | Story | Condition | Expected rule (nguồn) | Technique | Level |
|---|---|---|---|---|---|
| CL-FN-01 | 201 | `HKG-01` (chóp S.ABCD, đáy vuông cạnh a, SA ⊥ đáy, SA = a), a = 1 | tọa độ A(0,0,0), B(1,0,0), D(0,1,0), S(0,0,1); mọi ràng buộc sai số ≤ 1e-6 (M-GEO-02; `02_domain_model.md` §5) | EP | đơn vị |
| CL-FN-03 | 201 | ràng buộc mâu thuẫn; không dựng được; hình suy biến (ba điểm thẳng hàng làm đáy) | không trả hình; không nhãn "Đã kiểm chứng"; thông báo theo AC | EP | đơn vị |
| CL-FN-02 | 202 | mỗi hình mẫu (chóp, lăng trụ, hộp, lập phương, nón, trụ, cầu) với tham số mặc định | hình hiện ngay, không gọi AI (REQ-GEO-03, D-GEO-056) | EP | đơn vị, tích hợp API |
| CL-FN-02 | 202 | tham số = 0; âm; không phải số; rất lớn | từ chối theo AC (biên trên: chờ BA) | BVA | đơn vị |
| CL-FN-11 | 203 | DT nhãn dòng 1: hình đạt, đáp số khớp, tọa độ engine | "Đã kiểm chứng" (D-GEO-061) | DT | đơn vị |
| CL-FN-11 | 203 | DT dòng 2: hình đạt, đáp số không khớp | "Cần xem lại" | DT | đơn vị |
| CL-FN-11 | 203 | DT dòng 3: hình không đạt | "Cần xem lại" | DT | đơn vị |
| CL-FN-11 | 203 | đáp số nhiều giá trị `HKG-04`: một giá trị khớp, một không | "Cần xem lại" (M-GEO-03: phải khớp từng giá trị) | DT | đơn vị |
| CL-FN-11 | 203 | q_AI = 45,0045 và 45,0046 với q_tính = 45 (F-GEO-071) | khớp; không khớp (M-GEO-03, D-GEO-040, PROVISIONAL) | BVA | đơn vị |
| CL-FN-11 | 203 | sai số ràng buộc vuông góc = 1e-6 và = 2e-6 (F-GEO-073) | đạt; không đạt (M-GEO-02) | BVA | đơn vị |
| CL-FN-11 | 203 | chỉ kiểm 2 bộ giá trị tham số | không đủ điều kiện "Đã kiểm chứng" (≥ 3 bộ, D-GEO-040) | BVA | đơn vị |
| CL-FN-11 | 203 | AI trả kèm nhãn "Đã kiểm chứng" trong nội dung; cột nhãn sẵn trong `materials/*.xlsx` | bị bỏ qua; nhãn chỉ do engine tính (D-GEO-004, R16); `HKG-06` tính lại | đoán lỗi | đơn vị, tích hợp |
| CL-FN-05 | 203 | giáo viên gọi thao tác đổi nhãn máy | không có thao tác đó; bị từ chối (BR-GEO-14) | DT | tích hợp API |
| CL-FN-11 | 204 | DT dòng 4: đề lạ, tọa độ AI | "Cần xem lại" kèm cảnh báo chưa kiểm chứng (BR-GEO-08) | DT | tích hợp API, thủ công |
| CL-FN-03 | 204 | tọa độ AI suy biến | theo AC (không hình hoặc thông báo) | đoán lỗi | đơn vị |
| CL-UI-03 | 204 | đề hình phẳng | thông báo chưa hỗ trợ (REQ-GEO-51; câu chữ của BA) | EP | tích hợp API |
| CL-UI-02 | 203 | nhãn trên `mvp-01`, `mvp-02`, `mvp-05` | đúng chuỗi "Đã kiểm chứng" / "Cần xem lại" (D-GEO-079) | soát | thủ công |
| CL-FN-01 | 205 | danh mục hình mẫu | đủ chóp, lăng trụ, hộp, lập phương, nón, trụ, cầu, Oxyz; mỗi hình có tham số mặc định | soát dữ liệu | tích hợp |

## Rủi ro và ưu tiên của epic
| Rủi ro | Xác suất × tác động | Ưu tiên case |
|---|---|---|
| Bài sai mang "Đã kiểm chứng" (dung sai, nhiều giá trị, tọa độ AI) | thấp × rất cao | High; bốn dòng DT nhãn và hai cặp biên đều có case |
| AI hoặc tệp nguồn tự gắn nhãn | thấp × rất cao | High |
| Tham số hình mẫu không hợp lệ làm hỏng màn | trung bình × trung bình | Medium |
| Chuỗi nhãn sai biến thể | trung bình × thấp | Medium |

## Dữ liệu cần
- `work/fixture.md`: `HKG-01`, `HKG-02` (theo tham số), `HKG-04` (hai giá trị), `HKG-06` (nhãn sẵn "Cần xem lại"), một đề lạ, một đề hình phẳng, tham số mặc định từng hình mẫu.
- Giá trị sát ngưỡng: F-GEO-071, F-GEO-073 của BA (QC không lập hàng riêng).

## Phần khó kiểm
- Dung sai là giả định (D-GEO-040): mọi case biên PROVISIONAL.
- Engine chỉ kiểm phần tính toán, không kiểm lập luận (BR-GEO-09): không viết case về tính đúng của lập luận lời giải.
- Ràng buộc do AI giả lập trả về là nội dung soạn sẵn: không kiểm khả năng đọc đề của AI.

## Bổ sung lượt sửa gom sau phản biện (M-304)

| CL id | Condition | Expected rule | Technique | Level |
|---|---|---|---|---|
| CL-FN-11 | đáp số AI 45,0044 / 45,0046 với giá trị tính 45 | khớp / không khớp; so sánh có ε 1e-12 (D-GEO-163); TC-GEO-0216, 0219 | BVA | đơn vị |
