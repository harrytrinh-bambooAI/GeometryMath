# GEO — truy vết AC → TC (7 epic)

> Chủ sở hữu: QC. **Tệp sinh ra**: `python3 -I work/tools/check_ac_tc.py --trace work/traceability.md` (đọc `work/epics/E<n>/stories_ac.md` và `test_cases.md`). Cập nhật 22-11-2026. Nguồn sự thật của case: `work/src/qc/` → `work/epics/E<n>/test_cases.md`.

## 0. Reader guide and glossary

| Id / abbreviation | Meaning | Where used |
|---|---|---|
| `US-GEO-nnn-SCn` / `FCn` | tiêu chí thành công / thất bại của story (BA, `stories_ac.md`) | cột AC |
| `TC-GEO-nnnn` | test case (QC, `test_cases.md`); 0800…0899 là dải xuyên epic | cột TC |
| E2E-UI · E2E-API · IT · Manual | cách tự động hóa chính: Playwright qua trình duyệt · gọi HTTP thẳng · kiểm thử tích hợp phía máy chủ · người chạy (có lý do) | cột Tự động hóa của case |

## 1. Tổng

| Epic | AC còn hiệu lực | AC có TC | TC | E2E-UI | E2E-API | IT | Manual |
|---|---|---|---|---|---|---|---|
| E1 | 70 | 70 | 76 | 21 | 19 | 27 | 9 |
| E2 | 37 | 37 | 37 | 12 | 5 | 20 | 0 |
| E3 | 54 | 54 | 54 | 46 | 3 | 0 | 5 |
| E4 | 93 | 93 | 95 | 59 | 29 | 6 | 1 |
| E5 | 43 | 43 | 43 | 23 | 11 | 9 | 0 |
| E6 | 113 | 113 | 119 | 61 | 38 | 18 | 2 |
| E7 | 43 | 43 | 45 | 18 | 16 | 11 | 0 |
| **Tổng** | **453** | **453** | **469** | **240** | **121** | **91** | **17** |

## 2. AC → TC

### E1

| AC | Tiêu đề | Trạng thái | TC (Tự động hóa) |
|---|---|---|---|
| US-GEO-101-SC1 | Vào lớp bằng tên và mã lớp đúng | Active | TC-GEO-0100 (E2E-UI) |
| US-GEO-101-SC2 | Tên chưa có trong lớp thì thành học sinh mới của lớp đó | Active | TC-GEO-0101 (E2E-UI) |
| US-GEO-101-SC3 | Vào lại với cùng tên thì giữ đúng dữ liệu của mình | Active | TC-GEO-0102 (E2E-UI) |
| US-GEO-101-SC4 | Mã lớp quyết định trường và lớp | Active | TC-GEO-0103 (E2E-UI) |
| US-GEO-101-SC5 | Màn "Vào lớp" chỉ có hai ô nhập | Active | TC-GEO-0104 (E2E-UI) |
| US-GEO-101-SC6 | Tên dài đúng 60 ký tự được nhận | Active | TC-GEO-0105 (E2E-API) |
| US-GEO-101-SC7 | Dùng được trên iPhone màn dọc | Active | TC-GEO-0106 (Manual) |
| US-GEO-101-FC1 | Tên hiển thị rỗng | Active | TC-GEO-0107 (E2E-UI) |
| US-GEO-101-FC2 | Tên hiển thị 61 ký tự | Active | TC-GEO-0108 (E2E-UI) |
| US-GEO-101-FC3 | Mã lớp không tồn tại | Active | TC-GEO-0109 (E2E-UI) |
| US-GEO-101-FC4 | Phiên hết hạn | Active | TC-GEO-0110 (E2E-UI) |
| US-GEO-101-FC5 | Gõ sai mã lớp nhiều lần không bị khóa riêng | Active | TC-GEO-0173 (E2E-UI) |
| US-GEO-102-SC1 | Mã giáo viên đúng | Active | TC-GEO-0111 (E2E-UI) |
| US-GEO-102-SC2 | Vào được từ điện thoại | Active | TC-GEO-0112 (E2E-UI) |
| US-GEO-102-SC3 | Mỗi mã vào đúng tài khoản được cấp | Active | TC-GEO-0174 (E2E-UI) |
| US-GEO-102-SC4 | Máy chủ không lưu mã rõ | Active | TC-GEO-0172 (IT) |
| US-GEO-102-FC1 | Mã giáo viên sai | Active | TC-GEO-0113 (E2E-UI) |
| US-GEO-102-FC2 | Học sinh không làm được việc của giáo viên | Active | TC-GEO-0114 (E2E-API) |
| US-GEO-102-FC3 | Không có công tắc đổi vai | Active | TC-GEO-0115 (E2E-UI) |
| US-GEO-102-FC4 | Mã đã thu hồi | Active | TC-GEO-0175 (E2E-UI) |
| US-GEO-102-FC5 | Mã quá hạn dùng | Active | TC-GEO-0176 (E2E-API) |
| US-GEO-102-FC6 | Dò mã: lần thử thứ 11 trong 15 phút bị chặn | Active | TC-GEO-0171 (E2E-API), TC-GEO-0179 (E2E-API) |
| US-GEO-102-FC7 | Phiên học sinh không dùng được đường lời giải của giáo viên | Active | TC-GEO-0177 (E2E-API) |
| US-GEO-103-SC1 | Một lượt gọi, một bản ghi đủ trường | Active | TC-GEO-0116 (IT) |
| US-GEO-103-SC2 | Đổi model bằng cấu hình | Active | TC-GEO-0117 (IT) |
| US-GEO-103-SC3 | Khóa của nhà cung cấp không xuống trình duyệt | Active | TC-GEO-0118 (E2E-UI) |
| US-GEO-103-SC4 | Không gửi định danh học sinh cho nhà cung cấp | Active | TC-GEO-0119 (IT) |
| US-GEO-103-FC1 | Nhà cung cấp trả lỗi | Active | TC-GEO-0120 (E2E-UI) |
| US-GEO-103-FC2 | Nhà cung cấp quá thời gian | Active | TC-GEO-0121 (IT) |
| US-GEO-103-FC3 | Bấm "Thử lại" sau lỗi | Active | TC-GEO-0122 (E2E-UI) |
| US-GEO-104-SC1 | Học sinh chỉ nhận bài của lớp mình | Active | TC-GEO-0123 (E2E-API) |
| US-GEO-104-SC2 | Giáo viên chỉ thấy lớp mình dạy | Active | TC-GEO-0124 (E2E-UI) |
| US-GEO-104-SC3 | Nội dung hệ thống đọc được từ mọi trường | Active | TC-GEO-0125 (E2E-API) |
| US-GEO-104-SC4 | Bản ghi mới mang trường của người tạo | Active | TC-GEO-0126 (E2E-API) |
| US-GEO-104-SC5 | Bản ghi có chủ sở hữu, phiên bản lược đồ, nguồn gốc lời giải | Active | TC-GEO-0127 (IT) |
| US-GEO-104-FC1 | Học sinh gọi bài của lớp khác cùng trường | Active | TC-GEO-0128 (E2E-UI) |
| US-GEO-104-FC2 | Học sinh gọi bài của trường khác | Active | TC-GEO-0129 (E2E-API), TC-GEO-0170 (E2E-API) |
| US-GEO-104-FC3 | Học sinh gọi dữ liệu của bạn cùng lớp | Active | TC-GEO-0130 (E2E-API) |
| US-GEO-104-FC4 | Giáo viên trường khác đổi giờ mở | Active | TC-GEO-0131 (E2E-API) |
| US-GEO-104-FC5 | Giáo viên trường khác xem Theo dõi lớp | Active | TC-GEO-0132 (E2E-API), TC-GEO-0170 (E2E-API) |
| US-GEO-104-FC6 | Giao bài chéo trường | Active | TC-GEO-0133 (E2E-API) |
| US-GEO-104-FC7 | Thẻ xác thực bị sửa | Active | TC-GEO-0134 (E2E-API) |
| US-GEO-104-FC8 | Con trỏ của trường khác | Active | TC-GEO-0800 (E2E-API) |
| US-GEO-104-FC9 | Giáo viên không đọc được câu trả lời đã lưu của học sinh | Active | TC-GEO-0178 (E2E-API) |
| US-GEO-105-SC1 | Báo cáo của một lần chạy trên sáu đề | Active | TC-GEO-0135 (IT) |
| US-GEO-105-SC2 | Một đề có đáp số sai làm tỉ lệ giảm đúng công thức | Active | TC-GEO-0136 (IT) |
| US-GEO-105-SC3 | Chạy lại khi đổi model giữ cả hai báo cáo | Active | TC-GEO-0137 (IT) |
| US-GEO-105-SC4 | So với ngưỡng đề xuất | Active | TC-GEO-0138 (IT) |
| US-GEO-105-SC5 | Ghi chú AI giả lập | Active | TC-GEO-0139 (IT) |
| US-GEO-105-FC1 | Bộ đề trống | Active | TC-GEO-0140 (IT) |
| US-GEO-105-FC2 | Một đề thiếu đáp số tham chiếu | Active | TC-GEO-0141 (IT) |
| US-GEO-105-FC3 | Không bản ghi nào mang "Đã kiểm chứng" khi hình hoặc đáp số chưa khớp | Active | TC-GEO-0142 (IT) |
| US-GEO-106-SC1 | Sáu đề có đáp số tham chiếu đúng nguồn | Active | TC-GEO-0143 (IT) |
| US-GEO-106-SC2 | Đáp số nhiều giá trị lưu thành từng đại lượng | Active | TC-GEO-0144 (IT) |
| US-GEO-106-SC3 | Đáp số theo tham số có ba bộ giá trị kiểm | Active | TC-GEO-0145 (IT) |
| US-GEO-106-SC4 | Một đề, một bản ghi cho cả kho đề và bộ đề chuẩn | Active | TC-GEO-0146 (IT) |
| US-GEO-106-FC1 | Đề thiếu đáp số không vào bộ đề chuẩn một cách im lặng | Active | TC-GEO-0147 (IT) |
| US-GEO-106-FC2 | Nhãn trong tệp nguồn không thành nhãn máy | Active | TC-GEO-0148 (IT) |
| US-GEO-107-SC1 | p95 đọc dưới tải đều | Active | TC-GEO-0149 (Manual) |
| US-GEO-107-SC2 | p95 ghi dưới tải đều | Active | TC-GEO-0150 (Manual) |
| US-GEO-107-SC3 | Tỉ lệ lỗi dưới tải đều | Active | TC-GEO-0151 (Manual) |
| US-GEO-107-SC4 | Đỉnh 1000 học sinh mở lời giải trong 10 giây | Active | TC-GEO-0152 (Manual) |
| US-GEO-107-SC5 | Không lộ lời giải dưới tải | Active | TC-GEO-0153 (Manual) |
| US-GEO-107-FC1 | Vượt ngưỡng đề xuất | Active | TC-GEO-0154 (Manual) |
| US-GEO-107-FC2 | Quá tải thì từ chối nhanh, không treo | Active | TC-GEO-0155 (Manual) |
| US-GEO-108-SC1 | Sinh đủ quy mô bằng một lệnh | Active | TC-GEO-0156 (IT) |
| US-GEO-108-SC2 | Phân bố theo công thức | Active | TC-GEO-0157 (IT) |
| US-GEO-108-SC3 | Chạy lại cho cùng quy mô | Active | TC-GEO-0158 (IT) |
| US-GEO-108-FC1 | Không lẫn vào dữ liệu mẫu | Active | TC-GEO-0159 (IT) |
| US-GEO-108-FC2 | Tên sinh ra không phải tên thật | Active | TC-GEO-0160 (IT) |

Case không bám tiêu chí của E1 (loại X, trỏ NFR / BR / REQ / D):

| TC | Trỏ về | Tự động hóa |
|---|---|---|
| TC-GEO-0801 | D-GEO-125, NFR-GEO-031, NFR-GEO-040, REQ-GEO-39 | IT |
| TC-GEO-0802 | D-GEO-125, NFR-GEO-031, NFR-GEO-042, REQ-GEO-39 | IT |
| TC-GEO-0803 | BR-GEO-01, BR-GEO-19, D-GEO-045, D-GEO-125, D-GEO-170, NFR-GEO-057 | Manual |
| TC-GEO-0804 | BR-GEO-19, D-GEO-125, NFR-GEO-034 | E2E-API |

### E2

| AC | Tiêu đề | Trạng thái | TC (Tự động hóa) |
|---|---|---|---|
| US-GEO-201-SC1 | Dựng hình HKG-01 thỏa mọi ràng buộc | Active | TC-GEO-0200 (IT) |
| US-GEO-201-SC2 | Dựng hình có số đo cụ thể | Active | TC-GEO-0201 (IT) |
| US-GEO-201-SC3 | Thẻ ràng buộc hiện ở góc hình | Active | TC-GEO-0202 (E2E-UI) |
| US-GEO-201-SC4 | Sai số sát dưới ngưỡng vẫn thỏa | Active | TC-GEO-0203 (IT) |
| US-GEO-201-SC5 | Cùng đầu vào cho cùng hình | Active | TC-GEO-0204 (IT) |
| US-GEO-201-FC1 | Sai số vượt ngưỡng | Active | TC-GEO-0205 (IT) |
| US-GEO-201-FC2 | Ràng buộc mâu thuẫn | Active | TC-GEO-0206 (IT) |
| US-GEO-201-FC3 | Hình suy biến | Active | TC-GEO-0207 (IT) |
| US-GEO-202-SC1 | Chọn hình mẫu và thấy hình ngay, không gọi AI | Active | TC-GEO-0208 (E2E-UI) |
| US-GEO-202-SC2 | Đổi tham số thì hình đổi theo | Active | TC-GEO-0209 (E2E-UI) |
| US-GEO-202-SC3 | Nhận số thập phân dấu phẩy | Active | TC-GEO-0210 (E2E-UI) |
| US-GEO-202-SC4 | Sáu hình mẫu đều dựng được | Active | TC-GEO-0211 (E2E-UI) |
| US-GEO-202-FC1 | Tham số không hợp lệ | Active | TC-GEO-0212 (E2E-UI) |
| US-GEO-203-SC1 | Đề khớp mang "Đã kiểm chứng" | Active | TC-GEO-0213 (IT) |
| US-GEO-203-SC2 | Đề không khớp mang "Cần xem lại" | Active | TC-GEO-0214 (IT) |
| US-GEO-203-SC3 | Đáp số theo tham số kiểm trên ba bộ giá trị | Active | TC-GEO-0215 (IT) |
| US-GEO-203-SC4 | Sai lệch sát dưới ngưỡng vẫn khớp | Active | TC-GEO-0216 (IT) |
| US-GEO-203-SC5 | Nhãn do engine tính có thể khác nhãn trong tệp nguồn | Active | TC-GEO-0217 (IT) |
| US-GEO-203-SC6 | Một chuỗi nhãn trên mọi màn | Active | TC-GEO-0218 (E2E-UI) |
| US-GEO-203-FC1 | Sai lệch vượt ngưỡng | Active | TC-GEO-0219 (IT) |
| US-GEO-203-FC2 | Đáp số hai giá trị, một giá trị sai | Active | TC-GEO-0220 (IT) |
| US-GEO-203-FC3 | AI tự khai nhãn bị bỏ qua | Active | TC-GEO-0221 (E2E-API) |
| US-GEO-203-FC4 | Giáo viên không đặt được nhãn máy | Active | TC-GEO-0222 (E2E-UI) |
| US-GEO-203-FC5 | Nhãn không kéo theo giá trị tính xuống thiết bị học sinh | Active | TC-GEO-0223 (E2E-API) |
| US-GEO-203-SC7 | Đề không tham số khớp theo dung sai tuyệt đối | Active | TC-GEO-0234 (IT) |
| US-GEO-203-FC6 | Đề không tham số: lệch vượt dung sai theo chữ số ghi | Active | TC-GEO-0235 (IT) |
| US-GEO-203-SC8 | Đáp số thập phân khớp theo nửa đơn vị chữ số cuối | Active | TC-GEO-0236 (IT) |
| US-GEO-204-SC1 | Đề lạ có hình kèm cảnh báo | Active | TC-GEO-0224 (E2E-UI) |
| US-GEO-204-SC2 | Cảnh báo luôn hiện | Active | TC-GEO-0225 (E2E-UI) |
| US-GEO-204-SC3 | Đề lạ được đếm vào tỉ lệ đề lạ | Active | TC-GEO-0226 (IT) |
| US-GEO-204-FC1 | Đề lạ không bao giờ "Đã kiểm chứng" | Active | TC-GEO-0227 (E2E-API) |
| US-GEO-204-FC2 | Tọa độ AI hỏng | Active | TC-GEO-0228 (E2E-API) |
| US-GEO-204-FC3 | Đề hình phẳng | Active | TC-GEO-0229 (E2E-UI) |
| US-GEO-205-SC1 | Danh mục đủ hình | Active | TC-GEO-0230 (IT) |
| US-GEO-205-SC2 | Sáu thẻ hình mẫu trên màn "Tự học" | Active | TC-GEO-0231 (E2E-UI) |
| US-GEO-205-SC3 | Trường nào cũng đọc được danh mục | Active | TC-GEO-0232 (E2E-API) |
| US-GEO-205-FC1 | Hình mẫu thiếu giá trị mặc định | Active | TC-GEO-0233 (IT) |

### E3

| AC | Tiêu đề | Trạng thái | TC (Tự động hóa) |
|---|---|---|---|
| US-GEO-301-SC1 | Kéo để xoay | Active | TC-GEO-0300 (E2E-UI) |
| US-GEO-301-SC2 | Chụm ngón để thu phóng trên iPad | Active | TC-GEO-0301 (Manual) |
| US-GEO-301-SC3 | Lăn chuột để thu phóng trên máy tính | Active | TC-GEO-0302 (E2E-UI) |
| US-GEO-301-SC4 | "Nhìn từ trên" | Active | TC-GEO-0303 (E2E-UI) |
| US-GEO-301-SC5 | "Nhìn từ trước" | Active | TC-GEO-0304 (E2E-UI) |
| US-GEO-301-SC6 | "Đặt lại" về góc nhìn ban đầu | Active | TC-GEO-0305 (E2E-UI) |
| US-GEO-301-SC7 | Cạnh khuất vẽ nét đứt và tính lại khi xoay | Active | TC-GEO-0306 (E2E-UI) |
| US-GEO-301-SC8 | Bật, tắt trục Oxyz | Active | TC-GEO-0307 (E2E-UI) |
| US-GEO-301-SC9 | "Xóa hết" xóa phần vẽ thêm, giữ hình gốc và góc nhìn | Active | TC-GEO-0353 (E2E-UI) |
| US-GEO-301-FC1 | Thu phóng có giới hạn | Active | TC-GEO-0308 (E2E-UI) |
| US-GEO-301-FC2 | Xoay và thu phóng không tính là thao tác trên hình | Active | TC-GEO-0309 (E2E-API) |
| US-GEO-302-SC1 | Chạm chọn một cạnh | Active | TC-GEO-0310 (E2E-UI) |
| US-GEO-302-SC2 | Chạm chọn một mặt | Active | TC-GEO-0311 (E2E-UI) |
| US-GEO-302-SC3 | Chạm chọn một điểm | Active | TC-GEO-0312 (E2E-UI) |
| US-GEO-302-SC4 | Mỗi lần chọn là một thao tác trên hình | Active | TC-GEO-0313 (E2E-API) |
| US-GEO-302-FC1 | Chạm vào chỗ chồng nhau | Active | TC-GEO-0314 (Manual) |
| US-GEO-302-FC2 | Chạm ra ngoài hình | Active | TC-GEO-0315 (E2E-UI) |
| US-GEO-303-SC1 | Vẽ đoạn nối hai điểm | Active | TC-GEO-0316 (E2E-UI) |
| US-GEO-303-SC2 | Giao điểm của hai đường cắt nhau có tên | Active | TC-GEO-0317 (E2E-UI) |
| US-GEO-303-SC3 | Dựng mặt phẳng qua ba điểm | Active | TC-GEO-0318 (E2E-UI) |
| US-GEO-303-SC4 | Giao tuyến của mặt phẳng mới với mặt của hình | Active | TC-GEO-0319 (E2E-UI) |
| US-GEO-303-SC5 | Mở rộng một mặt | Active | TC-GEO-0320 (E2E-UI) |
| US-GEO-303-SC6 | Kéo dài một cạnh | Active | TC-GEO-0321 (E2E-UI) |
| US-GEO-303-FC1 | Hai đường chéo nhau | Active | TC-GEO-0322 (E2E-UI) |
| US-GEO-303-FC2 | Ba điểm thẳng hàng | Active | TC-GEO-0323 (E2E-UI) |
| US-GEO-303-FC3 | Không có điều khiển kéo mặt phẳng | Active | TC-GEO-0324 (E2E-UI) |
| US-GEO-304-SC1 | Thêm trung điểm | Active | TC-GEO-0325 (E2E-UI) |
| US-GEO-304-SC2 | Thêm điểm chia tỉ lệ | Active | TC-GEO-0326 (E2E-UI) |
| US-GEO-304-SC3 | Thêm điểm tự do trên cạnh | Active | TC-GEO-0327 (E2E-UI) |
| US-GEO-304-SC4 | Điểm mới dùng được cho công cụ khác | Active | TC-GEO-0328 (E2E-UI) |
| US-GEO-304-FC1 | Tỉ lệ không hợp lệ | Active | TC-GEO-0329 (E2E-UI) |
| US-GEO-304-FC2 | Điểm trùng điểm đã có | Active | TC-GEO-0330 (E2E-UI) |
| US-GEO-305-SC1 | Đo độ dài một yếu tố trung gian khi đang khóa | Active | TC-GEO-0331 (E2E-UI) |
| US-GEO-305-SC2 | Đo một góc trung gian khi đang khóa | Active | TC-GEO-0332 (E2E-UI) |
| US-GEO-305-SC3 | Đo khoảng cách từ điểm tới mặt | Active | TC-GEO-0333 (E2E-UI) |
| US-GEO-305-SC4 | Sau giờ mở, đại lượng đề hỏi đo được | Active | TC-GEO-0334 (E2E-UI) |
| US-GEO-305-SC5 | Số đo dùng dấu phẩy | Active | TC-GEO-0335 (E2E-UI) |
| US-GEO-305-SC6 | Bài chuyển Đã mở khi đang mở sẵn công cụ đo | Active | TC-GEO-0352 (E2E-UI) |
| US-GEO-305-FC1 | Đo đại lượng đề hỏi khi đang khóa | Active | TC-GEO-0336 (E2E-UI) |
| US-GEO-305-FC2 | Đo đại lượng cùng vai trò khi đang khóa | Active | TC-GEO-0337 (E2E-UI) |
| US-GEO-305-FC3 | Bài Chưa đặt giờ cũng ẩn | Active | TC-GEO-0338 (E2E-UI) |
| US-GEO-305-FC4 | Bài tự học đang khóa cũng ẩn | Active | TC-GEO-0339 (E2E-UI) |
| US-GEO-305-FC5 | Danh sách đại lượng ẩn không mang giá trị | Active | TC-GEO-0340 (E2E-API) |
| US-GEO-306-SC1 | Bố cục điện thoại | Active | TC-GEO-0341 (E2E-UI) |
| US-GEO-306-SC2 | Chụm ngón để thu phóng | Active | TC-GEO-0342 (Manual) |
| US-GEO-306-SC3 | Nút đặt lại | Active | TC-GEO-0343 (Manual) |
| US-GEO-306-SC4 | Bảng gợi ý trượt lên, xuống | Active | TC-GEO-0344 (Manual) |
| US-GEO-306-SC5 | Sáu nút chính và nút "Thêm" trên iPhone | Active | TC-GEO-0345 (E2E-UI) |
| US-GEO-306-FC1 | Xoay ngang máy giữa chừng | Active | TC-GEO-0346 (E2E-UI) |
| US-GEO-306-FC2 | Vùng chạm đủ lớn | Active | TC-GEO-0351 (E2E-UI) |
| US-GEO-307-SC1 | Tên mặt không lặp chữ | Active | TC-GEO-0347 (E2E-UI) |
| US-GEO-307-SC2 | Mặt bốn đỉnh tô kín | Active | TC-GEO-0348 (E2E-UI) |
| US-GEO-307-FC1 | Hai đường chéo nhau trên hình lập phương | Active | TC-GEO-0349 (E2E-UI) |
| US-GEO-307-FC2 | Mặt lõm | Active | TC-GEO-0350 (E2E-UI) |

### E4

| AC | Tiêu đề | Trạng thái | TC (Tự động hóa) |
|---|---|---|---|
| US-GEO-401-SC1 | Dán đề và nhận hình | Active | TC-GEO-0400 (E2E-UI) |
| US-GEO-401-SC2 | Màn chờ rõ ràng | Active | TC-GEO-0401 (E2E-UI) |
| US-GEO-401-SC3 | Bài tự học mới bắt đầu ở mức 0 | Active | TC-GEO-0402 (E2E-API) |
| US-GEO-401-SC4 | Ghi thời gian từ gửi đề tới có hình | Active | TC-GEO-0403 (IT) |
| US-GEO-401-FC1 | Đề rỗng | Active | TC-GEO-0404 (E2E-UI) |
| US-GEO-401-FC2 | Đề 8001 ký tự | Active | TC-GEO-0405 (E2E-API) |
| US-GEO-401-FC3 | Trợ lý lỗi khi dựng hình | Active | TC-GEO-0406 (E2E-UI) |
| US-GEO-401-FC4 | Đề không phải hình học | Active | TC-GEO-0407 (E2E-API) |
| US-GEO-401-FC5 | Bạn khác không thấy bài tự học của mình | Active | TC-GEO-0408 (E2E-API) |
| US-GEO-402-SC1 | Ảnh rõ được đọc thành chữ để sửa | Active | TC-GEO-0409 (E2E-UI) |
| US-GEO-402-SC2 | Sửa chữ rồi dựng hình | Active | TC-GEO-0410 (E2E-UI) |
| US-GEO-402-SC3 | Chụp trực tiếp bằng máy ảnh của iPad, iPhone | Active | TC-GEO-0411 (Manual) |
| US-GEO-402-FC1 | Ảnh mờ | Active | TC-GEO-0412 (E2E-API) |
| US-GEO-402-FC2 | Ảnh vượt 1 MB | Active | TC-GEO-0413 (E2E-API) |
| US-GEO-402-FC3 | Trợ lý lỗi khi đọc ảnh | Active | TC-GEO-0414 (E2E-API) |
| US-GEO-403-SC1 | Vào từ "Tự khám phá" của Bài của em | Active | TC-GEO-0415 (E2E-UI) |
| US-GEO-403-SC2 | "Mở chế độ tự học" | Active | TC-GEO-0416 (E2E-UI) |
| US-GEO-403-SC3 | Màn khám phá không có gợi ý, lời giải, hỏi đáp | Active | TC-GEO-0417 (E2E-UI) |
| US-GEO-403-FC1 | Chưa vào lớp | Active | TC-GEO-0418 (E2E-API) |
| US-GEO-404-SC1 | Xem Gợi ý 1 | Active | TC-GEO-0419 (E2E-UI) |
| US-GEO-404-SC2 | Xem Gợi ý 2 | Active | TC-GEO-0420 (E2E-UI) |
| US-GEO-404-SC3 | Giáo viên thấy mức gợi ý vừa dùng | Active | TC-GEO-0421 (E2E-UI) |
| US-GEO-404-SC4 | Xem lại gợi ý không làm tăng tiến độ | Active | TC-GEO-0422 (E2E-API) |
| US-GEO-404-SC5 | Gợi ý trên bài tự học | Active | TC-GEO-0423 (E2E-UI) |
| US-GEO-404-FC1 | Xin Gợi ý 2 khi chưa xem Gợi ý 1 | Active | TC-GEO-0424 (E2E-UI) |
| US-GEO-404-FC2 | Giáo viên đã tắt gợi ý | Active | TC-GEO-0425 (E2E-UI) |
| US-GEO-404-FC3 | Gợi ý chưa xin thì chưa được gửi xuống | Active | TC-GEO-0426 (E2E-API) |
| US-GEO-404-FC4 | Mất mạng lúc bấm | Active | TC-GEO-0427 (E2E-UI) |
| US-GEO-404-FC5 | Phản hồi gợi ý không cho lưu đệm | Active | TC-GEO-0488 (E2E-API) |
| US-GEO-405-SC1 | Đủ hai gợi ý thì mở được lời giải | Active | TC-GEO-0428 (E2E-UI), TC-GEO-0484 (E2E-UI) |
| US-GEO-405-SC2 | Đếm lượt gợi ý trước khi mở lời giải | Active | TC-GEO-0429 (IT) |
| US-GEO-405-FC1 | Chưa xem gợi ý nào | Active | TC-GEO-0430 (E2E-UI) |
| US-GEO-405-FC2 | Mới xem Gợi ý 1 mà gọi thẳng lời giải | Active | TC-GEO-0431 (E2E-API) |
| US-GEO-405-FC3 | Trang bài tự học không mang sẵn lời giải | Active | TC-GEO-0432 (E2E-API) |
| US-GEO-405-FC4 | Hỏi đáp không mở trước lời giải của bài tự học | Active | TC-GEO-0433 (E2E-API) |
| US-GEO-406-SC1 | Dán lại đề đang khóa thì bị khóa theo bài giao | Active | TC-GEO-0434 (E2E-UI) |
| US-GEO-406-SC2 | Bài giao mở thì bài tự học kế thừa cũng mở | Active | TC-GEO-0435 (E2E-UI) |
| US-GEO-406-SC3 | Đề trùng bài đã mở thì không kế thừa | Active | TC-GEO-0436 (E2E-API) |
| US-GEO-406-SC4 | Bài của lớp khác không gây kế thừa | Active | TC-GEO-0437 (E2E-API) |
| US-GEO-406-SC5 | Đề gần trùng cũng kế thừa | Active | TC-GEO-0438 (E2E-API), TC-GEO-0483 (E2E-UI), TC-GEO-0484 (E2E-UI) |
| US-GEO-406-SC6 | Kế thừa khóa của bài Chưa đặt giờ | Active | TC-GEO-0481 (E2E-UI) |
| US-GEO-406-FC1 | Gọi thẳng lời giải của bài kế thừa khóa | Active | TC-GEO-0439 (E2E-API), TC-GEO-0481 (E2E-UI) |
| US-GEO-406-FC2 | Giáo viên dời giờ mở | Active | TC-GEO-0440 (E2E-API) |
| US-GEO-406-SC7 | Đề viết lại với cạnh 2a vẫn kế thừa | Active | TC-GEO-0482 (E2E-UI) |
| US-GEO-406-SC8 | Cùng hình nhưng khác tham số thì không kế thừa | Active | TC-GEO-0489 (E2E-UI) |
| US-GEO-406-SC9 | Dán đề trước, giáo viên giao sau: lần xin kế tiếp bị khóa | Active | TC-GEO-0485 (E2E-UI) |
| US-GEO-406-SC10 | Bài kế thừa khóa dùng gợi ý của đề kho | Active | TC-GEO-0487 (E2E-API) |
| US-GEO-406-FC3 | Dán trước, giao sau: gọi thẳng lời giải bị từ chối | Active | TC-GEO-0490 (E2E-API) |
| US-GEO-406-FC4 | Dữ liệu gợi ý của bài kế thừa khóa không lộ | Active | TC-GEO-0491 (E2E-API) |
| US-GEO-406-FC5 | Đề không dựng được bằng hình mẫu không có lời giải tự học | Active | TC-GEO-0492 (E2E-UI) |
| US-GEO-406-FC6 | Đã xem lời giải tự học, sau đó giáo viên giao đúng đề: xin lại thì bị khóa | Active | TC-GEO-0486 (E2E-UI) |
| US-GEO-407-SC1 | Bấm một bước, hình tô đúng đối tượng | Active | TC-GEO-0441 (E2E-UI) |
| US-GEO-407-SC2 | Đổi bước thì đổi phần tô | Active | TC-GEO-0442 (E2E-UI) |
| US-GEO-407-SC3 | "Bước sau", "Bước trước" | Active | TC-GEO-0443 (E2E-UI) |
| US-GEO-407-SC4 | Nhãn của lời giải | Active | TC-GEO-0444 (E2E-UI) |
| US-GEO-407-SC5 | Xem lời giải được ghi nhận | Active | TC-GEO-0445 (E2E-API) |
| US-GEO-407-SC6 | Trên iPhone | Active | TC-GEO-0446 (E2E-UI) |
| US-GEO-407-FC1 | Bước không có đối tượng để tô | Active | TC-GEO-0447 (E2E-UI) |
| US-GEO-407-FC2 | Chuyển bước nhanh | Active | TC-GEO-0448 (E2E-UI) |
| US-GEO-408-SC1 | Phần vẽ thêm hiện dần theo bước | Active | TC-GEO-0449 (E2E-UI) |
| US-GEO-408-SC2 | "Vì sao nghĩ ra bước này?" | Active | TC-GEO-0450 (E2E-UI) |
| US-GEO-408-SC3 | Quay lại bước trước thì phần vẽ thêm bớt đi | Active | TC-GEO-0451 (E2E-UI) |
| US-GEO-408-SC4 | Nhảy thẳng tới bước cuối | Active | TC-GEO-0452 (E2E-UI) |
| US-GEO-408-FC1 | Bước không có phần vẽ thêm | Active | TC-GEO-0453 (E2E-UI) |
| US-GEO-409-SC1 | Câu trả lời có nguồn "Giáo án mẫu" | Active | TC-GEO-0454 (E2E-UI) |
| US-GEO-409-SC2 | Câu trả lời có nguồn "AI soạn" | Active | TC-GEO-0455 (E2E-UI) |
| US-GEO-409-SC3 | Câu hỏi ngoài giáo trình | Active | TC-GEO-0456 (E2E-UI) |
| US-GEO-409-SC4 | Màn chờ khi trợ lý đang trả lời | Active | TC-GEO-0457 (E2E-UI) |
| US-GEO-409-SC5 | Câu hỏi được ghi nhận | Active | TC-GEO-0458 (E2E-API) |
| US-GEO-409-SC6 | Nguồn nêu ra luôn mở được | Active | TC-GEO-0459 (E2E-UI) |
| US-GEO-409-FC1 | Trợ lý lỗi | Active | TC-GEO-0460 (E2E-UI) |
| US-GEO-409-FC2 | Câu hỏi rỗng hoặc quá dài | Active | TC-GEO-0461 (E2E-API) |
| US-GEO-409-FC3 | Giáo viên đã tắt hỏi đáp | Active | TC-GEO-0462 (E2E-UI) |
| US-GEO-410-SC1 | Xem nhật ký của mình | Active | TC-GEO-0463 (E2E-UI) |
| US-GEO-410-SC2 | Lọc theo bài | Active | TC-GEO-0464 (E2E-UI) |
| US-GEO-410-SC3 | Mở lại câu trả lời đã lưu | Active | TC-GEO-0465 (E2E-UI) |
| US-GEO-410-SC4 | Chỉ thấy câu của mình | Active | TC-GEO-0466 (E2E-API) |
| US-GEO-410-SC5 | Câu trả lời từng bị chặn mở lại vẫn là câu thay thế | Active | TC-GEO-0493 (E2E-UI) |
| US-GEO-410-FC1 | Bài chưa có câu hỏi | Active | TC-GEO-0467 (E2E-UI) |
| US-GEO-410-FC2 | Con trỏ hỏng | Active | TC-GEO-0468 (E2E-API) |
| US-GEO-411-SC1 | Bấm từ khóa trong gợi ý | Active | TC-GEO-0469 (E2E-UI) |
| US-GEO-411-SC2 | Bài giảng do AI viết mang nhãn chờ duyệt | Active | TC-GEO-0470 (E2E-UI) |
| US-GEO-411-SC3 | Từ khóa trong lời giải | Active | TC-GEO-0471 (E2E-UI) |
| US-GEO-411-SC4 | Đóng hộp bài giảng | Active | TC-GEO-0472 (E2E-UI) |
| US-GEO-411-FC1 | Cụm từ không có bài giảng | Active | TC-GEO-0473 (E2E-UI) |
| US-GEO-411-FC2 | Bài giảng không lộ đáp số của bài đang khóa | Active | TC-GEO-0474 (E2E-API) |
| US-GEO-411-FC3 | Không dùng nhãn "Giáo viên đã duyệt" | Active | TC-GEO-0475 (E2E-UI) |
| US-GEO-412-SC1 | Đủ 10 bài giảng với nguồn và nhãn | Active | TC-GEO-0476 (IT) |
| US-GEO-412-SC4 | Cờ chờ soát hiện cho học sinh và giáo viên | Active | TC-GEO-0494 (E2E-UI) |
| US-GEO-412-SC2 | Mỗi bài có từ khóa trỏ tới | Active | TC-GEO-0477 (IT) |
| US-GEO-412-SC3 | Mọi trường đọc được | Active | TC-GEO-0478 (E2E-API) |
| US-GEO-412-FC1 | Bài giảng chứa lời giải của một đề trong kho | Active | TC-GEO-0479 (IT) |
| US-GEO-412-FC2 | Bài giảng "Giáo án mẫu" thiếu tham chiếu nguồn | Active | TC-GEO-0480 (IT) |

### E5

| AC | Tiêu đề | Trạng thái | TC (Tự động hóa) |
|---|---|---|---|
| US-GEO-501-SC1 | Mở Kho đề | Active | TC-GEO-0500 (E2E-UI) |
| US-GEO-501-SC2 | Lọc theo hai nhóm cùng lúc | Active | TC-GEO-0501 (E2E-UI) |
| US-GEO-501-SC3 | Lọc theo hình | Active | TC-GEO-0502 (E2E-UI) |
| US-GEO-501-SC4 | Dòng đếm không đổi theo bộ lọc | Active | TC-GEO-0503 (E2E-UI) |
| US-GEO-501-SC5 | Nội dung một thẻ đề | Active | TC-GEO-0504 (E2E-UI) |
| US-GEO-501-SC6 | Dùng được trên điện thoại | Active | TC-GEO-0505 (E2E-UI) |
| US-GEO-501-FC1 | Lọc không có kết quả | Active | TC-GEO-0506 (E2E-UI) |
| US-GEO-501-FC2 | Học sinh gọi Kho đề | Active | TC-GEO-0507 (E2E-UI) |
| US-GEO-501-FC3 | Không có tìm kiếm và thêm đề | Active | TC-GEO-0508 (E2E-UI) |
| US-GEO-501-FC4 | Giá trị lọc không hợp lệ | Active | TC-GEO-0509 (E2E-API) |
| US-GEO-502-SC1 | Đề đã kiểm chứng | Active | TC-GEO-0510 (E2E-UI) |
| US-GEO-502-SC2 | Đề cần xem lại | Active | TC-GEO-0511 (E2E-UI) |
| US-GEO-502-SC3 | Kho đề đứng tên Giáo viên mẫu | Active | TC-GEO-0512 (E2E-UI) |
| US-GEO-502-FC1 | Đề chưa tính nhãn | Active | TC-GEO-0513 (E2E-API) |
| US-GEO-502-FC2 | Không có "Giáo viên đã duyệt" | Active | TC-GEO-0514 (E2E-UI) |
| US-GEO-503-SC1 | Bốn phần của trang chi tiết | Active | TC-GEO-0515 (E2E-UI) |
| US-GEO-503-SC2 | Giáo viên thấy lời giải dù bài đang khóa với học sinh | Active | TC-GEO-0516 (E2E-UI) |
| US-GEO-503-SC3 | Chip đáp số khớp tính toán | Active | TC-GEO-0517 (E2E-UI) |
| US-GEO-503-SC4 | Hình xoay được trên trang chi tiết | Active | TC-GEO-0518 (E2E-UI) |
| US-GEO-503-SC5 | Xem trên điện thoại | Active | TC-GEO-0519 (E2E-UI) |
| US-GEO-503-FC1 | Đề không có lời giải | Active | TC-GEO-0520 (E2E-UI) |
| US-GEO-503-FC2 | Không có nút "Sửa (sắp có)" và "Trình chiếu" | Active | TC-GEO-0521 (E2E-UI) |
| US-GEO-503-FC3 | Học sinh gọi trang chi tiết đề | Active | TC-GEO-0522 (E2E-API) |
| US-GEO-503-FC4 | Mã đề không tồn tại | Active | TC-GEO-0523 (E2E-API) |
| US-GEO-503-FC5 | Giáo viên gọi lời giải của đề tự nhập của học sinh trường mình | Active | TC-GEO-0542 (E2E-API) |
| US-GEO-503-FC6 | Giáo viên trường khác gọi lời giải của đề tự nhập | Active | TC-GEO-0541 (E2E-API) |
| US-GEO-504-SC1 | Nạp sáu đề nguồn đủ trường | Active | TC-GEO-0524 (IT) |
| US-GEO-504-SC2 | Nhãn do engine tính lại | Active | TC-GEO-0525 (IT) |
| US-GEO-504-SC3 | Kết quả nạp nêu chỗ nhãn khác tệp | Active | TC-GEO-0526 (IT) |
| US-GEO-504-SC4 | Đếm số đề đã kiểm chứng so với tiêu chí MVP | Active | TC-GEO-0527 (IT) |
| US-GEO-504-SC5 | Sửa lời giải tạo phiên bản mới, bài đã giao giữ phiên bản cũ | Active | TC-GEO-0528 (IT) |
| US-GEO-504-SC6 | Tính lại nhãn khi engine đổi | Active | TC-GEO-0529 (IT) |
| US-GEO-504-FC1 | Đề thiếu đáp số | Active | TC-GEO-0530 (IT) |
| US-GEO-504-FC2 | Sửa tại chỗ lời giải đã giao bị từ chối | Active | TC-GEO-0531 (IT) |
| US-GEO-505-SC1 | Tải lần lượt hết kho | Active | TC-GEO-0532 (E2E-UI) |
| US-GEO-505-SC2 | Dữ liệu đổi giữa chừng | Active | TC-GEO-0533 (E2E-API) |
| US-GEO-505-SC3 | Phân trang cùng bộ lọc | Active | TC-GEO-0534 (E2E-API) |
| US-GEO-505-SC4 | Phản hồi không có tổng số | Active | TC-GEO-0535 (E2E-API) |
| US-GEO-505-SC5 | Không OFFSET, không truy vấn lặp theo dòng | Active | TC-GEO-0536 (IT) |
| US-GEO-505-FC1 | Hết danh sách | Active | TC-GEO-0537 (E2E-UI) |
| US-GEO-505-FC2 | Đổi bộ lọc giữa chừng | Active | TC-GEO-0538 (E2E-UI) |
| US-GEO-505-FC3 | Con trỏ hỏng | Active | TC-GEO-0539 (E2E-API) |
| US-GEO-505-FC4 | Xin trang quá lớn | Active | TC-GEO-0540 (E2E-API) |

### E6

| AC | Tiêu đề | Trạng thái | TC (Tự động hóa) |
|---|---|---|---|
| US-GEO-601-SC1 | Trường, giáo viên, lớp, học sinh mẫu | Active | TC-GEO-0600 (IT) |
| US-GEO-601-SC2 | Bài giao mẫu đủ ba trạng thái | Active | TC-GEO-0601 (IT) |
| US-GEO-601-SC3 | Tiến độ mẫu của lớp 11A1 | Active | TC-GEO-0602 (IT) |
| US-GEO-601-SC4 | Nạp lại không nhân đôi | Active | TC-GEO-0603 (IT) |
| US-GEO-601-FC1 | Không lẫn dữ liệu thử tải | Active | TC-GEO-0604 (IT) |
| US-GEO-602-SC1 | Danh sách lớp ở bước 1 | Active | TC-GEO-0605 (E2E-UI) |
| US-GEO-602-SC2 | Giao cho một lớp | Active | TC-GEO-0606 (E2E-UI) |
| US-GEO-602-SC3 | Nút giao ghi tên lớp đang chọn | Active | TC-GEO-0607 (E2E-UI) |
| US-GEO-602-SC4 | Học sinh vào lớp sau vẫn thấy bài | Active | TC-GEO-0608 (E2E-API) |
| US-GEO-602-FC1 | Giao lại cùng đề cho cùng lớp | Active | TC-GEO-0609 (E2E-UI) |
| US-GEO-602-FC2 | Bấm đúp nút giao | Active | TC-GEO-0610 (E2E-UI) |
| US-GEO-602-FC3 | Không có giao theo bộ | Active | TC-GEO-0611 (E2E-UI) |
| US-GEO-602-FC4 | Hai yêu cầu giao cùng đề gửi đồng thời | Active | TC-GEO-0696 (E2E-API) |
| US-GEO-602-FC5 | Cùng đề nhưng lời giải đã có phiên bản mới vẫn là cùng đề | Active | TC-GEO-0865 (E2E-API) |
| US-GEO-603-SC1 | Mặc định cả hai bật | Active | TC-GEO-0612 (E2E-UI) |
| US-GEO-603-SC2 | Tắt gợi ý rồi giao | Active | TC-GEO-0613 (E2E-UI) |
| US-GEO-603-SC3 | Tắt hỏi đáp rồi giao | Active | TC-GEO-0614 (E2E-UI) |
| US-GEO-603-SC4 | Tắt cả hai vẫn giao được | Active | TC-GEO-0615 (E2E-UI) |
| US-GEO-603-SC5 | Giờ mở độc lập với công tắc | Active | TC-GEO-0616 (E2E-API) |
| US-GEO-603-FC1 | Không đổi được công tắc sau khi giao | Active | TC-GEO-0617 (E2E-UI) |
| US-GEO-604-SC1 | Đặt giờ tương lai | Active | TC-GEO-0618 (E2E-UI) |
| US-GEO-604-SC2 | Để trống giờ mở | Active | TC-GEO-0619 (E2E-UI) |
| US-GEO-604-SC3 | Dòng nhắc trước khi giao | Active | TC-GEO-0620 (E2E-UI) |
| US-GEO-604-SC4 | "Ngay bây giờ" | Active | TC-GEO-0621 (E2E-UI) |
| US-GEO-604-SC5 | Giờ Việt Nam, lưu UTC | Active | TC-GEO-0622 (E2E-UI) |
| US-GEO-604-SC6 | Thiết bị ở múi giờ khác | Active | TC-GEO-0623 (E2E-UI) |
| US-GEO-604-FC1 | Giờ ở quá khứ | Active | TC-GEO-0624 (E2E-UI) |
| US-GEO-604-FC2 | Đồng hồ máy giáo viên sai | Active | TC-GEO-0625 (E2E-API) |
| US-GEO-604-FC3 | Không có "Sau buổi học tới" | Active | TC-GEO-0626 (E2E-UI) |
| US-GEO-605-SC1 | Đổi giờ khi Sắp mở | Active | TC-GEO-0627 (E2E-UI) |
| US-GEO-605-SC2 | Đặt giờ lần đầu cho bài Chưa đặt giờ | Active | TC-GEO-0628 (E2E-UI) |
| US-GEO-605-SC3 | Mở ngay có hộp xác nhận | Active | TC-GEO-0629 (E2E-UI) |
| US-GEO-605-SC4 | Xác nhận mở ngay | Active | TC-GEO-0630 (E2E-UI) |
| US-GEO-605-SC5 | Hủy mở ngay | Active | TC-GEO-0631 (E2E-UI) |
| US-GEO-605-SC6 | Mở ngay từ Chưa đặt giờ | Active | TC-GEO-0632 (E2E-UI) |
| US-GEO-605-SC7 | Dời giờ muộn hơn thì giờ cũ không còn hiệu lực | Active | TC-GEO-0633 (E2E-API) |
| US-GEO-605-SC8 | Ghi nhật ký đổi giờ | Active | TC-GEO-0634 (IT) |
| US-GEO-605-FC1 | Đổi giờ khi đã Đã mở | Active | TC-GEO-0635 (E2E-UI) |
| US-GEO-605-FC2 | Đổi giờ về quá khứ | Active | TC-GEO-0636 (E2E-API) |
| US-GEO-605-FC3 | Hai lần đổi chồng nhau | Active | TC-GEO-0637 (E2E-API) |
| US-GEO-605-FC4 | Không có thao tác xóa giờ đã đặt | Active | TC-GEO-0638 (E2E-UI) |
| US-GEO-605-SC9 | Đổi giờ trên điện thoại | Active | TC-GEO-0866 (Manual) |
| US-GEO-605-SC10 | "Mở ngay" trên điện thoại | Active | TC-GEO-0867 (E2E-UI) |
| US-GEO-605-FC5 | Giờ mở trôi qua trong lúc đang mở hộp "Đổi giờ" | Active | TC-GEO-0699 (E2E-UI) |
| US-GEO-605-FC6 | Đổi sang giờ chỉ sau giờ máy chủ một giây | Active | TC-GEO-0868 (IT) |
| US-GEO-606-SC1 | Bài Sắp mở, đang làm dở | Active | TC-GEO-0639 (E2E-UI) |
| US-GEO-606-SC2 | Bài Chưa đặt giờ, chưa bắt đầu | Active | TC-GEO-0640 (E2E-UI) |
| US-GEO-606-SC3 | Bài Đã mở, đã xem lời giải | Active | TC-GEO-0641 (E2E-UI) |
| US-GEO-606-SC4 | Đã mở bài nhưng chưa dùng gợi ý | Active | TC-GEO-0642 (E2E-UI) |
| US-GEO-606-SC5 | Bấm vào một bài | Active | TC-GEO-0643 (E2E-UI) |
| US-GEO-606-SC6 | Trên iPhone | Active | TC-GEO-0644 (E2E-UI) |
| US-GEO-606-FC1 | Lớp chưa có bài | Active | TC-GEO-0645 (E2E-UI) |
| US-GEO-606-FC2 | Danh sách không mang lời giải | Active | TC-GEO-0646 (E2E-API) |
| US-GEO-606-FC3 | Không có thẻ chuỗi ngày và phần trăm theo dạng | Active | TC-GEO-0647 (E2E-UI) |
| US-GEO-606-FC4 | Con trỏ hỏng | Active | TC-GEO-0648 (E2E-API) |
| US-GEO-606-SC7 | Dòng "Hôm nay em có n bài cần làm" | Active | TC-GEO-0869 (E2E-UI) |
| US-GEO-606-SC8 | Không còn bài cần làm | Active | TC-GEO-0877 (E2E-UI) |
| US-GEO-607-SC1 | Mở xem trước | Active | TC-GEO-0649 (E2E-UI) |
| US-GEO-607-SC2 | Lời giải khóa trong xem trước | Active | TC-GEO-0650 (E2E-UI) |
| US-GEO-607-SC3 | Gợi ý xem được, không ghi sự kiện | Active | TC-GEO-0651 (E2E-UI) |
| US-GEO-607-SC4 | Thoát xem trước | Active | TC-GEO-0652 (E2E-UI) |
| US-GEO-607-FC1 | Dữ liệu của màn xem trước không chứa lời giải | Active | TC-GEO-0653 (E2E-API) |
| US-GEO-607-FC2 | Hỏi đáp trong xem trước cũng không lộ | Active | TC-GEO-0654 (E2E-API) |
| US-GEO-607-FC3 | Công cụ đo trong xem trước ẩn đại lượng đề hỏi | Active | TC-GEO-0655 (E2E-UI) |
| US-GEO-607-SC5 | Đã chọn giờ ở bước 3: thẻ khóa ghi giờ đang chọn | Active | TC-GEO-0870 (E2E-UI) |
| US-GEO-607-SC6 | Đề đã giao: theo giờ mở của chính bài giao | Active | TC-GEO-0871 (E2E-UI) |
| US-GEO-607-FC4 | Xem trước bài đã Đã mở không tải lời giải | Active | TC-GEO-0872 (E2E-API) |
| US-GEO-608-SC1 | Đã mở thì trả lời giải | Active | TC-GEO-0656 (E2E-UI) |
| US-GEO-608-SC2 | Đúng giờ mở thì mở | Active | TC-GEO-0657 (IT), TC-GEO-0695 (IT) |
| US-GEO-608-SC3 | Thẻ khóa của bài Sắp mở | Active | TC-GEO-0658 (E2E-UI) |
| US-GEO-608-SC4 | Thẻ khóa của bài Chưa đặt giờ | Active | TC-GEO-0659 (E2E-UI) |
| US-GEO-608-FC1 | Chưa đặt giờ: gửi thẳng yêu cầu lời giải | Active | TC-GEO-0660 (E2E-API) |
| US-GEO-608-FC2 | Một giây trước giờ mở | Active | TC-GEO-0661 (IT) |
| US-GEO-608-FC3 | Dữ liệu trang Làm bài của bài đang khóa | Active | TC-GEO-0662 (E2E-API) |
| US-GEO-608-FC4 | Đồng hồ thiết bị bị chỉnh | Active | TC-GEO-0663 (E2E-UI) |
| US-GEO-608-FC5 | Lời giải đã mở cho lớp này không lọt sang lớp còn khóa | Active | TC-GEO-0664 (E2E-API) |
| US-GEO-608-FC6 | Học sinh lớp khác gọi lời giải của bài đã mở | Active | TC-GEO-0665 (E2E-API) |
| US-GEO-608-FC7 | Đường đọc thông thường không tới được nơi lưu lời giải | Active | TC-GEO-0666 (IT), TC-GEO-0863 (IT) |
| US-GEO-608-SC5 | Tọa độ đỉnh trong dữ liệu trang là dữ kiện, không tính là lộ | Active | TC-GEO-0873 (E2E-API) |
| US-GEO-608-FC8 | Phản hồi lời giải không cho lưu đệm | Active | TC-GEO-0698 (E2E-API) |
| US-GEO-609-SC1 | Hỏi thẳng đáp số khi đang khóa | Active | TC-GEO-0667 (E2E-UI) |
| US-GEO-609-SC2 | Nhắc lại gợi ý không tính thêm lượt | Active | TC-GEO-0668 (E2E-API) |
| US-GEO-609-SC3 | Công thức và phương pháp tổng quát vẫn được trả lời | Active | TC-GEO-0669 (E2E-UI) |
| US-GEO-609-SC4 | Sau giờ mở, trợ lý được nêu đáp số | Active | TC-GEO-0670 (E2E-UI) |
| US-GEO-609-FC1 | AI buột ra đáp số: bị chặn ở máy chủ | Active | TC-GEO-0671 (E2E-API), TC-GEO-0878 (E2E-API) |
| US-GEO-609-FC2 | Dạng viết tương đương của đáp số | Active | TC-GEO-0672 (E2E-API) |
| US-GEO-609-FC3 | Xin giải từng bước | Active | TC-GEO-0673 (E2E-API) |
| US-GEO-609-FC4 | Học sinh chưa xem gợi ý nào | Active | TC-GEO-0674 (E2E-API) |
| US-GEO-609-FC5 | Bài Chưa đặt giờ cũng được bảo vệ | Active | TC-GEO-0675 (E2E-API) |
| US-GEO-609-SC5 | Chữ "a" không ở vị trí kết luận không bị chặn | Active | TC-GEO-0874 (E2E-API) |
| US-GEO-609-FC6 | Bộ kiểm soát đáp án lỗi thì không trả câu trả lời | Active | TC-GEO-0875 (E2E-API) |
| US-GEO-610-SC1 | Một lần chạy sạch | Active | TC-GEO-0676 (E2E-API) |
| US-GEO-610-SC2 | Có ca dán lại đề đang khóa vào tự học | Active | TC-GEO-0677 (E2E-API) |
| US-GEO-610-SC3 | Kiểm cả dữ liệu trả về của các kênh | Active | TC-GEO-0678 (E2E-API) |
| US-GEO-610-SC4 | Chạy lại khi đổi model | Active | TC-GEO-0679 (E2E-API) |
| US-GEO-610-FC1 | Một câu làm lộ | Active | TC-GEO-0680 (E2E-API) |
| US-GEO-610-FC2 | Ghi chú AI giả lập | Active | TC-GEO-0681 (E2E-API) |
| US-GEO-610-SC5 | Có ca đề gần trùng (đổi tên điểm) và đây là ca chặn phát hành | Active | TC-GEO-0876 (E2E-API) |
| US-GEO-610-SC6 | Có ca "giả làm giáo viên" | Active | TC-GEO-0676 (E2E-API), TC-GEO-0862 (E2E-API) |
| US-GEO-611-SC1 | Bộ câu thử đủ số câu và nhóm | Active | TC-GEO-0682 (IT) |
| US-GEO-611-SC2 | Mỗi câu gắn bài và đáp số tham chiếu | Active | TC-GEO-0683 (IT) |
| US-GEO-611-SC3 | Phiên bản bộ câu thử | Active | TC-GEO-0684 (IT) |
| US-GEO-611-FC1 | Câu trùng | Active | TC-GEO-0685 (IT) |
| US-GEO-611-FC2 | Câu không gắn bài | Active | TC-GEO-0686 (IT) |
| US-GEO-612-SC1 | Tới giờ thì lời giải tự mở | Active | TC-GEO-0687 (E2E-UI), TC-GEO-0697 (E2E-UI) |
| US-GEO-612-SC2 | Đếm ngược trên thẻ khóa | Active | TC-GEO-0688 (E2E-UI) |
| US-GEO-612-SC3 | Giáo viên mở ngay khi học sinh đang xem | Active | TC-GEO-0689 (E2E-UI), TC-GEO-0697 (E2E-UI) |
| US-GEO-612-SC4 | "Bài của em" đang mở cũng đổi nhãn | Active | TC-GEO-0690 (E2E-UI) |
| US-GEO-612-FC1 | Thiết bị ngủ qua giờ mở | Active | TC-GEO-0691 (Manual) |
| US-GEO-612-FC2 | Trước giờ mở trang không giữ sẵn lời giải | Active | TC-GEO-0692 (E2E-API) |
| US-GEO-612-FC3 | Mất mạng đúng lúc tới giờ | Active | TC-GEO-0693 (E2E-UI) |
| US-GEO-612-FC4 | Đồng hồ thiết bị nhanh | Active | TC-GEO-0694 (E2E-UI) |
| US-GEO-612-FC5 | Phản hồi trạng thái khóa không cho lưu đệm | Active | TC-GEO-0864 (E2E-UI) |

Case không bám tiêu chí của E6 (loại X, trỏ NFR / BR / REQ / D):

| TC | Trỏ về | Tự động hóa |
|---|---|---|
| TC-GEO-0860 | D-GEO-125, NFR-GEO-024, NFR-GEO-027 | IT |
| TC-GEO-0861 | D-GEO-125, D-GEO-143, D-GEO-148, NFR-GEO-056 | E2E-API |

### E7

| AC | Tiêu đề | Trạng thái | TC (Tự động hóa) |
|---|---|---|---|
| US-GEO-701-SC1 | Bốn thẻ tổng | Active | TC-GEO-0700 (E2E-UI) |
| US-GEO-701-SC2 | Bảng học sinh | Active | TC-GEO-0701 (E2E-UI) |
| US-GEO-701-SC3 | Tình trạng khi bài còn khóa | Active | TC-GEO-0702 (E2E-UI) |
| US-GEO-701-SC4 | Tình trạng sau khi mở lời giải | Active | TC-GEO-0703 (E2E-UI) |
| US-GEO-701-SC5 | Thẻ vàng khi lời giải khóa | Active | TC-GEO-0704 (E2E-UI) |
| US-GEO-701-SC6 | Vào trang từ mục "Theo dõi" | Active | TC-GEO-0705 (E2E-UI) |
| US-GEO-701-SC7 | Xem nhanh trên điện thoại | Active | TC-GEO-0706 (E2E-UI) |
| US-GEO-701-FC1 | Chưa em nào mở bài | Active | TC-GEO-0707 (E2E-UI) |
| US-GEO-701-FC2 | Không có xếp hạng, không sắp theo kết quả | Active | TC-GEO-0708 (E2E-UI) |
| US-GEO-701-FC3 | Không có "Nên nhắc lại trên lớp" | Active | TC-GEO-0709 (E2E-UI) |
| US-GEO-701-FC4 | Học sinh gọi Theo dõi lớp | Active | TC-GEO-0710 (E2E-API) |
| US-GEO-701-FC5 | Trang không mang lời giải | Active | TC-GEO-0711 (E2E-API) |
| US-GEO-701-FC6 | Trang không đọc bảng sự kiện, không truy vấn lặp theo học sinh | Active | TC-GEO-0712 (IT) |
| US-GEO-701-FC7 | Thanh bên không có "Tổng quan" và "Lớp học" | Active | TC-GEO-0744 (E2E-UI) |
| US-GEO-702-SC1 | Mức gợi ý của từng em | Active | TC-GEO-0713 (E2E-UI) |
| US-GEO-702-SC2 | Số câu hỏi của từng em | Active | TC-GEO-0714 (E2E-UI) |
| US-GEO-702-SC3 | Câu hỏi của một em | Active | TC-GEO-0715 (E2E-UI) |
| US-GEO-702-SC4 | Khối "Học sinh đang hỏi gì" gộp câu trùng | Active | TC-GEO-0716 (E2E-UI) |
| US-GEO-702-SC5 | Gộp không phân biệt hoa thường và khoảng trắng | Active | TC-GEO-0717 (IT) |
| US-GEO-702-FC1 | Chưa ai hỏi | Active | TC-GEO-0718 (E2E-UI) |
| US-GEO-702-FC2 | Không lẫn câu hỏi của bài khác, lớp khác | Active | TC-GEO-0719 (E2E-API) |
| US-GEO-702-FC3 | Không có nhận định do AI viết | Active | TC-GEO-0720 (E2E-UI) |
| US-GEO-703-SC1 | Mở bài tạo sự kiện mở bài | Active | TC-GEO-0721 (E2E-API) |
| US-GEO-703-SC2 | Mỗi công cụ là một thao tác trên hình | Active | TC-GEO-0722 (E2E-API) |
| US-GEO-703-SC3 | Sự kiện gợi ý ghi bền | Active | TC-GEO-0723 (E2E-API) |
| US-GEO-703-SC4 | Tỉ lệ phiên có thao tác trên hình | Active | TC-GEO-0724 (IT) |
| US-GEO-703-SC5 | Ranh giới phiên làm bài | Active | TC-GEO-0725 (IT) |
| US-GEO-703-SC6 | Bài tự học cũng ghi sự kiện | Active | TC-GEO-0726 (E2E-API) |
| US-GEO-703-FC1 | Công cụ không tính | Active | TC-GEO-0727 (E2E-API) |
| US-GEO-703-FC2 | Gửi lại cùng một lô | Active | TC-GEO-0728 (E2E-API), TC-GEO-0742 (E2E-API) |
| US-GEO-703-FC3 | Mất mạng khi đang thao tác | Active | TC-GEO-0729 (E2E-UI) |
| US-GEO-703-FC4 | Không ghi sự kiện thay người khác | Active | TC-GEO-0730 (E2E-API) |
| US-GEO-703-FC5 | Sự kiện cho bài không thuộc lớp mình | Active | TC-GEO-0731 (E2E-API) |
| US-GEO-703-SC7 | Sự kiện mở bài ghi bền | Active | TC-GEO-0743 (E2E-API) |
| US-GEO-703-SC8 | Khoảng nghỉ 29 phút 59 giây vẫn cùng phiên | Active | TC-GEO-0740 (IT) |
| US-GEO-703-SC9 | Khoảng nghỉ đúng 30 phút 0 giây mở phiên mới | Active | TC-GEO-0741 (IT) |
| US-GEO-704-SC1 | Sự kiện vào đúng phần theo thời gian | Active | TC-GEO-0732 (IT) |
| US-GEO-704-SC2 | Luôn có phần cho thời gian sắp tới | Active | TC-GEO-0733 (IT) |
| US-GEO-704-SC3 | Đọc sự kiện theo con trỏ, không trùng không sót | Active | TC-GEO-0734 (E2E-API), TC-GEO-0739 (E2E-API) |
| US-GEO-704-SC4 | Truy vấn nóng không quét toàn bảng | Active | TC-GEO-0735 (IT) |
| US-GEO-704-FC1 | Truy vấn sự kiện không có cận thời gian hoặc dùng OFFSET | Active | TC-GEO-0736 (IT) |
| US-GEO-704-FC2 | Giờ thiết bị ở tương lai bị kẹp về giờ nhận | Active | TC-GEO-0737 (IT) |
| US-GEO-704-FC3 | Con trỏ hỏng | Active | TC-GEO-0738 (E2E-API) |

