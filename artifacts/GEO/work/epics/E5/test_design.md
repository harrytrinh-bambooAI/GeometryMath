# E5 — thiết kế kiểm thử (Kho đề của Giáo viên mẫu)
> Chủ sở hữu: QC. Phần chung (danh mục CL, kỹ thuật, bảng quyết định khóa lời giải DT-n, biên giờ mở BV-n, thang rủi ro, dữ liệu `F-GEO-200…299`, bảng giải thích mã) ở `review/QC_test_design.md`; tệp này chỉ ghi phần riêng của epic. Lập từ `work/04_story_map.md` và `work/03_gate3_preliminary_stories.md`, không chờ AC; dòng thiết kế nào chưa có AC tương ứng sẽ được đối chiếu ở `qc_findings.md`.

## Applicability map (mỗi nhóm CL: Y / N kèm lý do)
| Group | Screen / story | Applies (Y/N) | Reason if N |
|---|---|---|---|
| CL-FN-01 Luồng chính | US-GEO-501…505 | Y | |
| CL-FN-02 Dữ liệu nhập | US-GEO-501 (bộ lọc), 505 (`cursor`, `limit`) | Y | |
| CL-FN-03 Lỗi và thông báo | US-GEO-505 (`INVALID_CURSOR`) | Y | |
| CL-FN-04 Chuyển trạng thái | — | N | kho đề chỉ đọc trong MVP |
| CL-FN-05 Phân quyền theo vai | US-GEO-501, 503 (học sinh không vào được Kho đề và trang chi tiết có lời giải) | Y | |
| CL-FN-06 Cách ly trường | US-GEO-501 (nội dung hệ thống dùng chung, D-GEO-076) | Y | |
| CL-FN-07 Dữ liệu máy chủ trả về | US-GEO-501 (danh sách không kèm lời giải), 503 | Y | |
| CL-FN-08 Phân trang | US-GEO-505 | Y | |
| CL-FN-09 Thời gian | — | N | không có quy tắc theo giờ |
| CL-FN-10 Đồng thời | US-GEO-505 (lọc đổi giữa chừng) | Y | |
| CL-FN-11 Nhãn kiểm chứng | US-GEO-502, 504 | Y | |
| CL-FN-12 Định dạng | US-GEO-502, 503 (đáp số dạng căn, mã `HKG-nn`) | Y | |
| CL-FN-13 Sự kiện | — | N | REQ-GEO-22 chỉ ghi sự kiện của học sinh |
| CL-FN-14 Cổng AI | — | N | kho đề soạn sẵn, không gọi AI khi xem |
| CL-UI-01 Thiết bị | US-GEO-501…503 (máy tính, điện thoại giáo viên) | Y | |
| CL-UI-02 Chuỗi hiển thị | US-GEO-502 | Y | |
| CL-UI-03 Ngoài phạm vi | US-GEO-503 (nút "Trình chiếu", "Sửa (sắp có)", D-GEO-078) | Y | |
| CL-UI-04, 05 | — | N | màn danh sách và đọc; thao tác hình ở E3 |
| CL-X-01 Tải | US-GEO-501 (đọc kho đề trong kịch bản tải đều) | Y | case tải đặt ở E1 |
| CL-X-02, 03 | US-GEO-505 | Y | |

## Design rows: `CL id · condition · expected rule · technique · level`
| CL id | Story | Condition | Expected rule (nguồn) | Technique | Level |
|---|---|---|---|---|---|
| CL-FN-01 | 501 | mở Kho đề không lọc | thẻ đề có hình, đề, nhãn; số đề trong 40…60 (D-GEO-055) | EP | tích hợp API, thủ công |
| CL-FN-02 | 501 | lọc cấp THPT; chủ đề Góc; loại hình chóp; kết hợp ba bộ lọc; bộ lọc không có kết quả | đúng tập đề theo `fixture.md`; trạng thái rỗng theo AC (D-GEO-070) | EP, DT | tích hợp API |
| CL-FN-05 | 501, 503 | thẻ học sinh gọi danh sách Kho đề và trang chi tiết đề | theo ma trận quyền của US-GEO-104; không bao giờ trả lời giải cho học sinh qua đường này | DT | tích hợp API |
| CL-FN-06 | 501 | giáo viên trường thứ hai trong dữ liệu thử | đọc được cùng kho (D-GEO-076) | EP | tích hợp API |
| CL-FN-07 | 501 | thân phản hồi danh sách | chỉ cột của thẻ đề; không có bước lời giải (REQ-GEO-39: chỉ lấy cột cần) | soát | tích hợp API |
| CL-FN-11 | 502 | đề khớp; đề không khớp; đề chưa tính nhãn | "Đã kiểm chứng"; "Cần xem lại"; chưa tính: theo AC, không bao giờ "Đã kiểm chứng" | DT | tích hợp API |
| CL-UI-02 | 502 | mọi thẻ | có "AI soạn"; không thẻ nào "Giáo viên đã duyệt" (D-GEO-028) | soát | tích hợp API, thủ công |
| CL-FN-01 | 503 | mở `HKG-01` | đủ hình, đáp số, lời giải từng bước, gợi ý (REQ-GEO-21) | EP | thủ công |
| CL-UI-03 | 503 | nút "Trình chiếu", "Sửa (sắp có)" | không hoạt động phía sau; hệ quả nhìn thấy theo AC (D-GEO-078) | soát | thủ công |
| CL-FN-11 | 504 | sau nạp kho | M-GEO-07 ≥ 40; M-GEO-05 = 0; nhãn `HKG-06` là kết quả engine, không phải cột sẵn (D-GEO-004) | soát dữ liệu | tích hợp |
| CL-FN-01 | 504 | bản ghi đề và lời giải | có `owner_id` Giáo viên mẫu, `schema_version`, `model`, `prompt_version`, `author_id` (REQ-GEO-30) | soát dữ liệu | tích hợp CSDL |
| CL-FN-08 | 505 | đi hết các trang với `limit` = 20 | hợp các trang = toàn bộ tập, không lặp, không sót; trang cuối không có con trỏ kế | EP | tích hợp API |
| CL-FN-08 | 505 | `limit` = 100; 101; 0; âm | 100 được; còn lại theo AC (kẹp hoặc `VALIDATION_FAILED`); không bao giờ > 100 dòng (M-004) | BVA | tích hợp API |
| CL-FN-08 | 505 | thân phản hồi | không có tổng số bản ghi (M-004) | soát | tích hợp API |
| CL-FN-03 | 505 | con trỏ bị sửa một ký tự; con trỏ của bộ lọc khác | `INVALID_CURSOR` (M-004); đổi lọc: theo AC | đoán lỗi | tích hợp API |
| CL-X-02 | 505 | kế hoạch thực thi truy vấn danh sách kho đề | dùng chỉ mục, không `OFFSET` (REQ-GEO-39) | soát kế hoạch | tích hợp CSDL |

## Rủi ro và ưu tiên của epic
| Rủi ro | Xác suất × tác động | Ưu tiên case |
|---|---|---|
| Nhãn sao chép từ tệp nguồn thay vì tính | trung bình × rất cao | High |
| Học sinh đọc lời giải qua đường của Kho đề | trung bình × rất cao | High |
| Lọc ra sai tập đề | trung bình × trung bình | Medium |
| Phân trang lặp, sót | thấp × trung bình | Medium |
| Kho không đủ 40 đề đã kiểm chứng | trung bình × cao | High (tiêu chí xong MVP, REQ-GEO-49) |

## Dữ liệu cần
- `work/fixture.md`: danh sách đề của kho với cấp, chủ đề, loại hình, nhãn tính được; tập kết quả kỳ vọng cho từng bộ lọc; một đề "Cần xem lại".
- Kho mới có 40…60 đề nên phân trang chỉ có ý nghĩa với `limit` nhỏ; trần 100 kiểm bằng F-GEO-201 ở E7 hoặc dữ liệu thử tải.

## Phần khó kiểm
- Nguồn của 34 đến 54 đề còn lại và người soát nội dung Toán chưa xác định (D-GEO-055, KG-05): case đếm M-GEO-07 PROVISIONAL.
- Quyền của học sinh với Kho đề chưa có nguồn nói rõ (chỉ BR-GEO-10, 12): gửi BA.

## Bổ sung lượt sửa gom sau phản biện (M-304)

| CL id | Condition | Expected rule | Technique | Level |
|---|---|---|---|---|
| CL-FN-08 | kích thước trang 10000 trên kho thử 48 đề | 48 đề, không con trỏ, không tổng số (D-GEO-142); TC-GEO-0540 | BVA | tích hợp API |
| CL-FN-06 | giáo viên trường khác, giáo viên cùng trường gọi lời giải của đề tự nhập | `NOT_FOUND`, hàm trả 0 hàng; đề kho vẫn xem được (D-GEO-153, DT-10); TC-GEO-0541, 0542 | bảng quyết định | tích hợp API + tích hợp CSDL |
| CL-UI-03 | nút "Trình chiếu", "Sửa (sắp có)" ở chi tiết đề | không hiện (D-GEO-164); TC-GEO-0521 | — | thủ công |
