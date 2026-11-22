# E3 — thiết kế kiểm thử (thao tác hình 3D trên thiết bị cảm ứng)
> Chủ sở hữu: QC. Phần chung (danh mục CL, kỹ thuật, bảng quyết định khóa lời giải DT-n, biên giờ mở BV-n, thang rủi ro, dữ liệu `F-GEO-200…299`, bảng giải thích mã) ở `review/QC_test_design.md`; tệp này chỉ ghi phần riêng của epic. Lập từ `work/04_story_map.md` và `work/03_gate3_preliminary_stories.md`, không chờ AC; dòng thiết kế nào chưa có AC tương ứng sẽ được đối chiếu ở `qc_findings.md`.

## Applicability map (mỗi nhóm CL: Y / N kèm lý do)
| Group | Screen / story | Applies (Y/N) | Reason if N |
|---|---|---|---|
| CL-FN-01 Luồng chính | US-GEO-301…307 | Y | |
| CL-FN-02 Dữ liệu nhập | US-GEO-304 (tỉ lệ chia) | Y | |
| CL-FN-03 Lỗi và thông báo | US-GEO-303 (hai đường chéo nhau), 304 | Y | |
| CL-FN-04 Chuyển trạng thái | US-GEO-301 (chế độ nhìn), 305 (ẩn → hiện khi tới giờ mở) | Y | |
| CL-FN-05 Phân quyền theo vai | — | N | công cụ như nhau cho mọi học sinh; giáo viên dùng qua Xem trước (E6) |
| CL-FN-06 Cách ly trường | — | N | thao tác chạy trên thiết bị với hình đã tải; cách ly kiểm ở E1 |
| CL-FN-07 Dữ liệu máy chủ trả về | US-GEO-305 (giá trị đại lượng đề hỏi không có trong dữ liệu gửi xuống khi khóa) | Y | |
| CL-FN-08 Phân trang | — | N | không có danh sách |
| CL-FN-09 Thời gian | US-GEO-305 (giá trị hiện sau giờ mở) | Y | |
| CL-FN-10 Đồng thời | — | N | một người một hình |
| CL-FN-11 Nhãn kiểm chứng | — | N | nhãn ở E2, E5 |
| CL-FN-12 Định dạng | US-GEO-305 (số đo dấu phẩy, góc có °) | Y | |
| CL-FN-13 Sự kiện | US-GEO-302…305 (sự kiện `tool`; xoay, thu phóng không tính, D-GEO-058) | Y | case ghi sự kiện đặt ở E7 |
| CL-FN-14 Cổng AI | — | N | không gọi AI |
| CL-UI-01 Thiết bị | US-GEO-301…306 | Y | |
| CL-UI-02 Chuỗi hiển thị | US-GEO-301 (tên công cụ), 305 ("ẩn tới khi mở lời giải": chờ câu chữ BA) | Y | |
| CL-UI-03 Ngoài phạm vi | kéo mặt phẳng, thiết diện động (REQ-GEO-54) | Y | không có công cụ đó trên thanh công cụ |
| CL-UI-04 Gián đoạn | US-GEO-306 (xoay ngang màn) | Y | |
| CL-UI-05 Cảm ứng | US-GEO-301, 302, 306 | Y | |
| CL-X-01…04 | — | N | không có truy vấn máy chủ riêng |

## Design rows: `CL id · condition · expected rule · technique · level`
| CL id | Story | Condition | Expected rule (nguồn) | Technique | Level |
|---|---|---|---|---|---|
| CL-UI-05 | 301 | kéo một ngón trên hình; chụm hai ngón; lăn chuột trên máy tính | hình xoay; thu phóng; trong giới hạn của AC | EP | thủ công trên thiết bị |
| CL-FN-04 | 301 | Nhìn từ trước → Nhìn từ trên → Xoay tự do → Đặt lại | góc nhìn theo từng chế độ; Đặt lại về góc ban đầu | ST | thủ công |
| CL-FN-01 | 301 | hình có cạnh khuất; bật tắt Oxyz | cạnh khuất vẽ nét đứt; trục hiện và ẩn (REQ-GEO-05, 07) | EP | thủ công, đơn vị (tính cạnh khuất) |
| CL-UI-05 | 302 | chạm cạnh, mặt, đỉnh; chạm chỗ hai đối tượng gần nhau; chạm ngoài hình | đối tượng được tô màu; ưu tiên theo AC; chạm ngoài bỏ chọn | EP | thủ công trên thiết bị |
| CL-FN-01 | 303 | vẽ đoạn nối hai điểm; dựng mặt phẳng qua ba điểm; mở rộng mặt; kéo dài cạnh | đối tượng mới hiện trên hình; giao điểm, giao tuyến tính đúng với tọa độ | EP | đơn vị (tính giao), thủ công |
| CL-FN-03 | 303 | hai đường chéo nhau (`HKG-02`: AC và B′D′); ba điểm thẳng hàng | cảnh báo hai đường chéo nhau, không giao điểm (REQ-GEO-36); mặt phẳng suy biến bị từ chối | EP | đơn vị, thủ công |
| CL-FN-02 | 304 | trung điểm; chia tỉ lệ 1:2; tỉ lệ 0, âm, không phải số; điểm trùng đỉnh có sẵn | điểm mới có nhãn; tỉ lệ không hợp lệ bị từ chối; quy tắc tên theo AC | EP, BVA | đơn vị, thủ công |
| CL-FN-01 | 305 | đo độ dài SA trên `HKG-01`, a = 1 | hiện giá trị yếu tố trung gian (D-GEO-024) | EP | thủ công |
| CL-FN-07 | 305 | bài `HKG-01` đang khóa: đo góc SDA; đo góc ASD | cả hai giá trị ẩn; dữ liệu gửi xuống thiết bị không chứa giá trị đó (D-GEO-024, BR-GEO-16, PO-42) | DT (DT-7) | tích hợp API, thủ công |
| CL-FN-09 | 305 | cùng bài sau giờ mở | hai giá trị hiện 45° | ST | thủ công |
| CL-FN-07 | 305 | bài tự học đang khóa (kế thừa, D-GEO-026) | như bài giao đang khóa | DT (DT-8) | tích hợp API |
| CL-FN-12 | 305 | số đo không nguyên | dấu phẩy thập phân; góc có ký hiệu ° | soát | thủ công |
| CL-UI-01 | 306 | iPhone dọc: chụm ngón, nút Đặt lại, bảng gợi ý trượt lên | bố cục theo `mvp-08`; không che hình | soát | thủ công trên iPhone |
| CL-UI-04 | 306 | xoay ngang iPhone khi đang làm bài | theo AC (chưa có nguồn về hướng màn) | gián đoạn | thủ công |
| CL-FN-01 | 307 | tái hiện ba lỗi: nhãn "Mặt Mặt …"; thiếu cảnh báo hai đường chéo nhau; chia tam giác mặt nhiều đỉnh | không còn tái hiện (REQ-GEO-36) | hồi quy | thủ công, đơn vị |
| CL-UI-03 | E3 | thanh công cụ | không có kéo mặt phẳng, thiết diện động (REQ-GEO-54) | soát | thủ công |

## Rủi ro và ưu tiên của epic
| Rủi ro | Xác suất × tác động | Ưu tiên case |
|---|---|---|
| Công cụ đo thành đường tắt lộ đáp số (góc ASD, bài tự học) | trung bình × rất cao | High; kiểm cả dữ liệu gửi xuống |
| Thao tác cảm ứng hỏng trên iPhone, iPad | cao × cao | High; thủ công thiết bị thật |
| Giao điểm, giao tuyến tính sai | thấp × cao | High ở tầng đơn vị |
| Ba lỗi cũ tái hiện | trung bình × trung bình | Medium |
| Chạm nhầm trên màn nhỏ | cao × thấp | Medium |

## Dữ liệu cần
- `work/fixture.md`: `HKG-01` (tọa độ a = 1, tập đại lượng ẩn: góc SDA, góc ASD), `HKG-02` (hai đường chéo nhau), một hình có mặt nhiều đỉnh, danh sách công cụ đầy đủ (KG-01).
- Thiết bị: iPad, iPhone thật; phiên bản tối thiểu chờ KG-09 (PROVISIONAL).

## Phần khó kiểm
- Cảm ứng thật: chỉ thủ công; mô phỏng trên máy tính không thay được.
- "Đại lượng cùng giá trị, cùng vai trò trong cùng tam giác lời giải": tập ẩn phải có trong dữ liệu của từng đề (BA); không có thì case chỉ kiểm được `HKG-01`.
- Đo gián tiếp (đo các cạnh rồi tự tính góc) là được phép theo D-GEO-024 ("vẫn đo được yếu tố trung gian"): không viết case cấm.

## Bổ sung lượt sửa gom sau phản biện (M-304)

| CL id | Condition | Expected rule | Technique | Level |
|---|---|---|---|---|
| CL-FN-07 | tọa độ đỉnh trong dữ liệu trả về của trang Làm bài | là dữ kiện của đề, không tính là lộ; lộ là khi trả hoặc hiện giá trị đáp số (D-GEO-154); TC-GEO-0340 | bảng quyết định | tích hợp API |
