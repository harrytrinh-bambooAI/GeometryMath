# Từ điển thông điệp và thành phần dùng chung — GEO

> Chủ sở hữu: BA. Mã `MSG-GEO-01…59` (dải của BA theo `board/CONVENTIONS.md` §2; trong mẫu story của skill gọi là `MSG_nn`). Story và test case dẫn mã; câu chữ đúng từng ký tự nằm ở đây và được chép vào §0 của tệp epic dùng nó. Tiếng Việt có dấu, số thập phân dùng dấu phẩy, giờ theo `HH:mm, dd/MM/yyyy` giờ Việt Nam (`glossary.md`). Phần trong `{…}` là giá trị thay thế. Câu chữ là **đề xuất của BA** (REQ-GEO-43 chỉ yêu cầu "lỗi dễ hiểu"); câu lấy từ khung thiết kế có ghi nguồn khung. Xưng hô: với học sinh dùng "em"; với giáo viên dùng câu trung tính.
> Quy tắc độ dài: thông báo một dòng ≤ 120 ký tự; nhãn nút ≤ 24 ký tự.
> Mã lỗi API ổn định (tin M-004, D-GEO-143): VALIDATION_FAILED, INVALID_CURSOR, UNAUTHENTICATED, FORBIDDEN, NOT_FOUND, PAYLOAD_TOO_LARGE, TIMEOUT, CONFLICT (HTTP 409, xung đột trạng thái), RATE_LIMITED (HTTP 429, kèm `Retry-After`), SERVICE_OVERLOADED (HTTP 503, kèm `Retry-After`).

## 1. Thông điệp

| MSG code | Loại | Câu chữ (nguyên văn) | Khi nào | Mã lỗi API (M-004) | Dùng ở |
|---|---|---|---|---|---|
| MSG-GEO-01 | Inline | "Thông tin chưa hợp lệ. Vui lòng kiểm tra lại các ô được đánh dấu." | dữ liệu gửi lên sai định dạng mà không có câu riêng | VALIDATION_FAILED | mọi biểu mẫu |
| MSG-GEO-02 | Page state | "Phiên đã hết. Vui lòng vào lại." | chưa xác thực hoặc thẻ hết hạn | UNAUTHENTICATED | mọi màn |
| MSG-GEO-03 | Toast | "Bạn không có quyền thực hiện thao tác này." | sai vai | FORBIDDEN | mọi màn |
| MSG-GEO-04 | Page state | "Không tìm thấy nội dung này." | tài nguyên không tồn tại hoặc không thuộc lớp, trường của người gọi | NOT_FOUND | mọi màn |
| MSG-GEO-05 | Inline | "Danh sách đã thay đổi. Vui lòng tải lại từ đầu." | con trỏ phân trang hỏng hoặc không còn hợp lệ | INVALID_CURSOR | mọi danh sách |
| MSG-GEO-06 | Inline | "Nội dung gửi lên quá lớn (tối đa 1 MB)." | thân yêu cầu vượt 1 MB | PAYLOAD_TOO_LARGE | nhập ảnh đề |
| MSG-GEO-07 | Toast | "Máy chủ phản hồi chậm. Vui lòng thử lại." | quá thời gian phía máy chủ | TIMEOUT | mọi màn |
| MSG-GEO-08 | Toast | "Không kết nối được. Vui lòng kiểm tra mạng và thử lại." | mất mạng phía thiết bị | — | mọi màn |
| MSG-GEO-09 | Inline | "Em hãy nhập tên hiển thị." | tên rỗng hoặc chỉ khoảng trắng | VALIDATION_FAILED | US-GEO-101 |
| MSG-GEO-10 | Inline | "Tên hiển thị tối đa 60 ký tự." | tên dài hơn 60 ký tự | VALIDATION_FAILED | US-GEO-101 |
| MSG-GEO-11 | Inline | "Mã lớp không đúng. Em hỏi lại giáo viên nhé." | mã lớp không tồn tại hoặc sai định dạng | VALIDATION_FAILED | US-GEO-101 |
| MSG-GEO-12 | Inline | "Mã giáo viên không đúng." | mã giáo viên sai, đã thu hồi hoặc quá hạn dùng (cùng một câu, không nói lý do; D-GEO-152) | VALIDATION_FAILED | US-GEO-102 |
| MSG-GEO-13 | Page state | "Đang dựng hình, em chờ một chút…" | từ lúc gửi đề tới lúc có hình hoặc có lỗi | — | US-GEO-401, 402 |
| MSG-GEO-14 | Inline + nút "Thử lại" | "Trợ lý đang bận. Em thử lại sau ít phút nhé." | cổng AI lỗi hoặc quá thời gian | TIMEOUT hoặc lỗi máy chủ | US-GEO-103, 401, 402, 409 |
| MSG-GEO-15 | Page state | "Trợ lý đang soạn câu trả lời…" | từ lúc gửi câu hỏi tới lúc có câu trả lời | — | US-GEO-409 |
| MSG-GEO-16 | Info (cố định trên hình) | "Hình này do AI dựng và chưa qua kiểm tra bằng tính toán. Em hãy đối chiếu với đề." | đề lạ, nguồn tọa độ là AI | — | US-GEO-204 |
| MSG-GEO-17 | Inline | "Hiện chưa hỗ trợ hình học phẳng. Em hãy nhập đề hình học không gian." | đề tự nhập là hình phẳng | VALIDATION_FAILED | US-GEO-204, 401 |
| MSG-GEO-18 | Inline | "Chưa dựng được hình từ đề này. Em kiểm tra lại đề hoặc chọn một hình mẫu." | ràng buộc mâu thuẫn, hình suy biến, đề không phải hình học | VALIDATION_FAILED | US-GEO-201, 204, 401 |
| MSG-GEO-19 | Inline | "Giá trị phải là số lớn hơn 0." | tham số hình mẫu hoặc tỉ lệ chia không hợp lệ | VALIDATION_FAILED | US-GEO-202, 304 |
| MSG-GEO-20 | Inline | "Em hãy nhập đề bài." | đề rỗng | VALIDATION_FAILED | US-GEO-401 |
| MSG-GEO-21 | Inline | "Đề bài tối đa 8000 ký tự." | đề dài hơn 8000 ký tự | VALIDATION_FAILED | US-GEO-401 |
| MSG-GEO-22 | Inline | "Chưa đọc được chữ trong ảnh. Em chụp lại rõ hơn hoặc gõ đề." | ảnh mờ, không phải đề | VALIDATION_FAILED | US-GEO-402 |
| MSG-GEO-23 | Toast | "Hai đường thẳng này chéo nhau nên không có giao điểm." | tìm giao điểm của hai đường chéo nhau | — | US-GEO-303, 307 |
| MSG-GEO-24 | Tooltip trên kết quả đo | "Đây là đại lượng đề hỏi. Giá trị sẽ hiện khi lời giải mở." | đo đại lượng đề hỏi hoặc đại lượng cùng vai trò khi lời giải khóa | — | US-GEO-305 |
| MSG-GEO-25 | Toast | "Ba điểm thẳng hàng nên chưa xác định được mặt phẳng." | dựng mặt phẳng suy biến | — | US-GEO-303 |
| MSG-GEO-26 | Toast | "Điểm này đã có trên hình." | thêm điểm trùng điểm có sẵn | — | US-GEO-304 |
| MSG-GEO-27 | Thẻ khóa | "Lời giải mở lúc {HH:mm, dd/MM/yyyy}" · dòng phụ "Do giáo viên của em đặt. Còn {thời gian còn lại}." (khung `mvp-05`) | bài giao ở trạng thái Sắp mở | — | US-GEO-606, 608, 612 |
| MSG-GEO-28 | Thẻ khóa | "Lời giải đang khóa" · dòng phụ "Giáo viên của em chưa đặt giờ mở." | bài giao ở trạng thái Chưa đặt giờ | — | US-GEO-606, 608 |
| MSG-GEO-29 | Nhãn | "Lời giải đã mở" (khung `mvp-04`) | bài giao ở trạng thái Đã mở | — | US-GEO-606, 612 |
| MSG-GEO-30 | Info | "Ở chế độ tự học, em xem gợi ý 1 và 2 trước, sau đó mới mở được lời giải." (khung `mvp-07`) | bài tự học chưa xem đủ hai gợi ý | FORBIDDEN khi gọi thẳng lời giải | US-GEO-405 |
| MSG-GEO-31 | Thẻ khóa | "Đề này trùng với bài lớp em đang làm. Lời giải mở theo giờ giáo viên đặt." | bài tự học kế thừa khóa | FORBIDDEN khi gọi thẳng lời giải | US-GEO-406 |
| MSG-GEO-32 | Empty | "Giáo viên đã tắt gợi ý cho bài này." | bài giao tắt "Gợi ý mức 1 và 2" | FORBIDDEN | US-GEO-404, 603 |
| MSG-GEO-33 | Empty | "Giáo viên đã tắt hỏi đáp cho bài này." | bài giao tắt "Hỏi đáp (không nêu đáp số)" | FORBIDDEN | US-GEO-409, 603 |
| MSG-GEO-34 | Inline | "Em xem Gợi ý 1 trước nhé." | xin Gợi ý 2 khi chưa xem Gợi ý 1 | FORBIDDEN | US-GEO-404 |
| MSG-GEO-35 | Câu trả lời của trợ lý | "Lời giải đang khóa nên mình chưa nêu đáp số hay các bước giải. Em xem lại gợi ý nhé: {gợi ý cao nhất em đã xem}" | câu hỏi đòi đáp số hoặc bước giải khi lời giải khóa, hoặc câu trả lời bị Kiểm soát đáp án chặn | — | US-GEO-609 |
| MSG-GEO-36 | Câu trả lời của trợ lý | "Giáo trình của lớp chưa có phần này. Em hãy hỏi giáo viên nhé." (prototype) | câu hỏi ngoài kho bài giảng mẫu | — | US-GEO-409 |
| MSG-GEO-37 | Inline | "Câu hỏi cần từ 1 đến 1000 ký tự." | câu hỏi rỗng hoặc quá dài | VALIDATION_FAILED | US-GEO-409 |
| MSG-GEO-38 | Empty | "Em chưa hỏi câu nào ở bài này." | nhật ký hỏi đáp của bài rỗng | — | US-GEO-410 |
| MSG-GEO-39 | Toast | "Chưa có bài giảng cho từ khóa này." | từ khóa không có bài giảng | NOT_FOUND | US-GEO-411 |
| MSG-GEO-40 | Empty | "Không có đề nào khớp bộ lọc. Hãy bỏ bớt điều kiện." | lọc kho đề ra 0 đề | — | US-GEO-501 |
| MSG-GEO-41 | Toast | "Đã giao bài cho lớp {tên lớp}." | giao bài thành công | — | US-GEO-602 |
| MSG-GEO-42 | Inline | "Đề này đã được giao cho lớp {tên lớp}." | giao lại cùng đề (cùng `problem`) cho cùng lớp (D-GEO-161) | CONFLICT | US-GEO-602 |
| MSG-GEO-43 | Inline | "Giờ mở lời giải phải sau thời điểm hiện tại. Muốn mở luôn, hãy chọn “Ngay bây giờ”." | đặt hoặc đổi giờ về quá khứ | VALIDATION_FAILED | US-GEO-604, 605 |
| MSG-GEO-44 | Info | "Trước {HH:mm} ngày {dd/MM}, học sinh chỉ thấy đề, hình và gợi ý. Lời giải và đáp số được giữ ở máy chủ." (khung `mvp-02`) | đã chọn giờ mở ở bước 3 giao bài | — | US-GEO-604 |
| MSG-GEO-45 | Info | "Chưa đặt giờ mở: lời giải và đáp số vẫn khóa cho tới khi đặt giờ hoặc mở ngay." | để trống giờ mở ở bước 3 giao bài | — | US-GEO-604 |
| MSG-GEO-46 | Toast | "Đã mở lời giải cho lớp {tên lớp}." | Mở ngay thành công | — | US-GEO-605 |
| MSG-GEO-47 | Toast | "Đã đổi giờ mở lời giải thành {HH:mm, dd/MM/yyyy}." | đặt hoặc đổi giờ thành công | — | US-GEO-605 |
| MSG-GEO-48 | Inline | "Lời giải đã mở nên không đổi giờ hay khóa lại được." | đổi giờ, mở ngay khi đã Đã mở, kể cả khi giờ mở trôi qua trong lúc đang mở hộp "Đổi giờ" | CONFLICT | US-GEO-605 |
| MSG-GEO-49 | Inline | "Giờ mở vừa được thay đổi ở nơi khác. Hãy tải lại để xem giờ mới." | hai thao tác đổi giờ chồng nhau | CONFLICT | US-GEO-605 |
| MSG-GEO-50 | Empty | "Lớp em chưa có bài được giao." | Bài của em không có bài | — | US-GEO-606 |
| MSG-GEO-51 | Dải cố định | "Đang xem như học sinh. Lời giải theo đúng giờ mở của bài." | giáo viên ở chế độ Xem trước như học sinh | — | US-GEO-607 |
| MSG-GEO-52 | Empty | "Chưa có học sinh nào mở bài." | Theo dõi lớp, 0 học sinh có sự kiện mở bài | — | US-GEO-701 |
| MSG-GEO-53 | Empty | "Chưa có câu hỏi nào." | Theo dõi lớp, khối câu hỏi rỗng | — | US-GEO-702 |
| MSG-GEO-54 | Empty | "Đề này chưa có lời giải." | đề kho không có lời giải | — | US-GEO-503 |
| MSG-GEO-55 | Toast | "Lời giải đã mở. Em xem ngay được rồi." | trang đang mở nhận trạng thái Đã mở | — | US-GEO-612 |
| MSG-GEO-56 | Nhãn | "Concept · Chặng {N}" | điều khiển ngoài MVP còn hiện trên màn | — | hiện không màn nào dùng: điều khiển không có yêu cầu thì không hiện (D-GEO-112, 164) |
| MSG-GEO-57 | Toast | "Hệ thống đang đông. Vui lòng thử lại sau vài giây." | máy chủ báo quá tải hoặc vượt giới hạn tốc độ chung | SERVICE_OVERLOADED hoặc RATE_LIMITED (D-GEO-143; NFR-GEO-055, 056) | mọi màn; US-GEO-107 |
| MSG-GEO-58 | Thẻ khóa | "Đề này chưa dựng được bằng hình mẫu nên chưa có lời giải tự học. Em dùng gợi ý nhé." | bài tự học của đề không có chữ ký chuẩn (đề lạ, tọa độ AI) sau khi đã xem đủ hai gợi ý | FORBIDDEN khi gọi thẳng lời giải | US-GEO-406 |
| MSG-GEO-59 | Inline | "Đã nhập sai mã quá nhiều lần. Vui lòng thử lại sau ít phút." | đăng nhập giáo viên vượt 10 lần sai trong 15 phút (D-GEO-152) | RATE_LIMITED | US-GEO-102 |
Dải `MSG-GEO-01…59` của BA đã dùng hết; mã mới xin A theo `board/CONVENTIONS.md` §2.

## 2. Chuỗi màn hình (nhãn, nút, placeholder, tiêu đề; không mang mã)

| Màn | Chuỗi (nguyên văn) | Nguồn |
|---|---|---|
| Vào lớp (học sinh) | tiêu đề "Vào lớp"; nhãn "Tên hiển thị", placeholder "Ví dụ: An"; nhãn "Mã lớp", placeholder "6 ký tự giáo viên đưa"; nút "Vào lớp"; liên kết "Tôi là giáo viên" | BA đề xuất (IAM-01 không có khung) |
| Vào vai giáo viên | tiêu đề "Giáo viên mẫu"; nhãn "Mã giáo viên" (mã riêng của tài khoản, ít nhất 12 ký tự); nút "Vào" | BA đề xuất (D-GEO-073, 152) |
| Nhãn máy | "Đã kiểm chứng" · "Cần xem lại" | glossary; D-GEO-079 (không dùng "Hình đã kiểm chứng") |
| Nhãn người | "AI soạn" · "AI soạn · chờ giáo viên duyệt" (bài giảng AI) | glossary; D-GEO-028, 029 |
| Nguồn | "Nguồn: Giáo án mẫu" · "Nguồn: AI soạn"; dòng cờ của bài giảng chưa soát: "Nội dung đang chờ giáo viên soát" (D-GEO-181) | glossary; D-GEO-181 |
| Kho đề | tiêu đề "Kho đề"; dòng đếm "{n} đề · {m} đã kiểm chứng · tài liệu của Giáo viên mẫu"; nhóm lọc "Cấp": Tất cả, THCS, THPT; "Hình": Tất cả, Hình chóp, Lăng trụ, Hình hộp, Lập phương, Nón, Trụ, Cầu; "Dạng bài": Tất cả, Góc, Khoảng cách, Thể tích, Diện tích, Mặt cầu; trên thẻ "AI soạn · {k} bước"; nút "Giao bài"; nút "Tải thêm" | `Design/mvp-01`; D-GEO-070 |
| Chi tiết đề và giao bài | "Giao bài cho lớp"; "1 · Chọn lớp"; "2 · Học sinh được trợ giúp gì trong lúc làm"; công tắc "Gợi ý mức 1 và 2", "Hỏi đáp (không nêu đáp số)"; "3 · Thời điểm mở lời giải"; lối tắt "Ngay bây giờ"; nút "Giao bài cho {tên lớp}"; nút "Xem như học sinh", "Thoát xem trước"; khối "Lời giải"; chip "Đáp số khớp tính toán: {đáp số}" | `Design/mvp-02`; D-GEO-053 |
| Theo dõi lớp | thanh bên giáo viên chỉ có mục "Theo dõi" (không có "Tổng quan", "Lớp học", D-GEO-129); tiêu đề "Theo dõi lớp {tên lớp}"; dòng "Bài: {tên bài} · giao lúc {HH:mm, dd/MM/yyyy}"; dòng phụ thẻ vàng "Mở lúc {HH:mm, dd/MM/yyyy} · còn {thời gian}" hoặc "Chưa đặt giờ mở"; hộp xác nhận "Mở lời giải ngay cho lớp {tên lớp}? Sau khi mở không khóa lại được." với nút "Mở ngay", "Hủy"; nhóm câu hỏi "{câu hỏi}" · "{tên các học sinh} · {n} lượt"; mục thanh bên "Theo dõi"; thẻ "đã mở bài", "dùng gợi ý", "câu hỏi", "thao tác hình"; cột "Học sinh", "Mở bài", "Gợi ý 1 · 2", "Hỏi đáp", "Thời gian", "Tình trạng"; giá trị "Đã mở bài", "Chưa mở", "{n} câu", "{n} phút", "—"; tình trạng "Chưa bắt đầu", "Đang làm", "Đang chờ lời giải", "Tự làm, không cần gợi ý", "Đã xem lời giải" (D-GEO-102); thẻ vàng "Lời giải đang khóa"; nút "Mở ngay", "Đổi giờ"; khối "Học sinh đang hỏi gì" | `Design/mvp-03`; M-GEO-15, 21…24; XF-02 (cột Mở bài dùng "Đã mở bài") |
| Bài của em | "Chào {tên hiển thị}"; "Hôm nay em có {n} bài cần làm" (`mvp-04`; D-GEO-138), khi n = 0: "Hôm nay em không có bài nào cần làm." (D-GEO-182); "Bài được giao"; dòng tiến độ "Chưa bắt đầu", "Đã mở bài" (D-GEO-101), "Đã xem gợi ý 1", "Đã xem gợi ý 2", "Đã xem lời giải"; nút "Bắt đầu", "Làm tiếp", "Xem lại"; khối "Tự khám phá"; nút "Mở chế độ tự học" | `Design/mvp-04`; M-GEO-14 |
| Làm bài | tab "Gợi ý", "Lời giải", "Hỏi đáp"; "Tiến độ của em {k}/3"; "Gợi ý 1 · Hướng nghĩ"; "Gợi ý 2 · Bước kế tiếp"; nút "Xem gợi ý 1", "Xem gợi ý 2"; công cụ "Chọn", "Điểm", "Đường", "Mặt phẳng", "Mở rộng", "Đo dài", "Đo góc", "Oxyz", "Đặt lại"; lệnh "Xóa hết" (D-GEO-134); góc nhìn "Nhìn từ trước", "Nhìn từ trên", "Xoay tự do"; placeholder "Hỏi về bài này…" | `Design/mvp-05`, `Design/prototype/README.md` (bộ công cụ) |
| Lời giải đã mở | "Bước trước", "Bước sau"; "Đang tô trên hình: {đối tượng}"; "Vì sao nghĩ ra bước này?" | `Design/mvp-06` |
| Tự học | tiêu đề "Tự học"; "Đề bài của em"; chọn cấp "THCS", "THPT"; nút "Chụp hoặc dán ảnh đề"; dòng "Ảnh sẽ được đọc thành chữ để em sửa lại"; nút "Dựng hình"; "Hoặc chọn một hình mẫu": "Hình chóp", "Lăng trụ", "Hình hộp", "Hình nón", "Hình trụ", "Hình cầu" | `Design/mvp-07`; D-GEO-056 |
| Điện thoại · làm bài | tab "Gợi ý", "Lời giải", "Hỏi"; nút "Thêm" mở các công cụ ngoài sáu nút chính | `Design/mvp-08`; D-GEO-135 |

## 3. Thành phần dùng chung (COM)

| Mã | Thành phần | Hành vi chung | Nơi dùng |
|---|---|---|---|
| COM_LOI | Khối lỗi | hiện `message` tiếng Việt của mô hình lỗi `{code, message, requestId, details[]}` (M-004); không hiện chi tiết kỹ thuật; lỗi theo ô hiện ngay dưới ô lấy từ `details[]` | mọi màn |
| COM_NHAN_MAY | Chip nhãn máy | đúng một trong hai chuỗi "Đã kiểm chứng" / "Cần xem lại", lấy từ dữ liệu máy chủ; thiết bị không tự tính | Kho đề, chi tiết đề, Làm bài, Tự học |
| COM_NHAN_NGUOI | Chip nhãn người | "AI soạn" (MVP) | Kho đề, Lời giải, bài giảng |
| COM_THE_KHOA | Thẻ trạng thái giờ mở | ba trạng thái Chưa đặt giờ / Sắp mở / Đã mở với MSG-GEO-28 / 27 / 29; trạng thái do máy chủ trả; bài tự học kế thừa khóa MSG-GEO-31, đề không có chữ ký chuẩn MSG-GEO-58 | Bài của em, Làm bài, Theo dõi lớp, giao bài, Xem trước |
| COM_HINH_3D | Khung hình 3D và thanh công cụ | xoay, thu phóng, công cụ, nét đứt cạnh khuất (E3) | Làm bài, Lời giải, Tự học, chi tiết đề, Xem trước |
| COM_THANG_GOI_Y | Thanh tiến độ ba đoạn | k/3 theo M-GEO-14 | Bài của em, tab Gợi ý |
| COM_DS_CON_TRO | Danh sách tải theo con trỏ | nút "Tải thêm" khi máy chủ báo còn trang; không hiện tổng số trang; con trỏ hỏng → MSG-GEO-05 | Kho đề, Bài của em, nhật ký hỏi đáp, Theo dõi lớp |
| COM_HOP_BAI_GIANG | Hộp bài giảng | tên khái niệm, nội dung, dòng "Nguồn: …", nhãn người | Gợi ý, Lời giải, Hỏi đáp |
| COM_MAN_CHO | Màn chờ AI | MSG-GEO-13 hoặc MSG-GEO-15, không hiện nội dung dở dang | Tự học, Hỏi đáp |
