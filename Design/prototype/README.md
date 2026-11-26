# Bản mẫu tương tác — Hình Học Không Gian Cùng AI

Mở `index.html` bằng trình duyệt (cần mạng để tải phông chữ). Toàn bộ là dữ liệu mẫu chạy trong trình duyệt: không có AI thật, không có máy chủ, tải lại trang là về trạng thái ban đầu.

## Cách dùng
- **Hàng trên:** chuyển vai (Học sinh, Giáo viên, Phụ huynh, Nhà trường, Cơ quan quản lý) và nút đặt lại dữ liệu.
- **Hàng dưới:** bấm lần lượt 5 bước của kịch bản MVP, rồi các màn hình tầm nhìn (viền nét đứt, có nhãn "Concept"). Nút **Giáo trình** ở hàng trên mở thẳng giáo trình của giáo viên. Ở 1440px thanh trên cùng gọn trong hai hàng.
- Trạng thái dùng chung giữa các vai: việc làm ở vai này hiện ra ở vai kia.

## Luồng MVP
| Bước | Vai | Nhập / thao tác | Phản hồi | Ảnh |
|---|---|---|---|---|
| 1 | Giáo viên | Mở kho đề, chọn đề, chọn lớp và giờ mở lời giải, bấm Giao bài | Màn chờ "Đang giao bài", thông báo, chuyển sang Theo dõi lớp | `shots/01`–`03` |
| 2 | Học sinh | Mở bài mới giao, kéo để xoay hình, dùng công cụ (Chọn, Điểm, Đoạn, Mặt phẳng, Mở rộng, Đo dài, Đo góc, Đặt lại), bấm xem gợi ý, bấm từ khóa để đọc bài giảng, hỏi trong tab Hỏi đáp | Hình 3D đổi theo thao tác, đại lượng đề hỏi bị ẩn khi đo, gợi ý hiện dần, trợ lý trả lời kèm nguồn và không nêu đáp số khi lời giải còn khóa | `shots/04`–`08`, `shots/int/` |
| 3 | Giáo viên | Mở Theo dõi lớp, bấm Đổi giờ hoặc Mở ngay | Dòng của An đã có gợi ý và câu hỏi vừa làm, mỗi câu hỏi kèm nguồn mà trợ lý đã dùng; thẻ chuyển sang "Lời giải đã mở" | `shots/09`–`11` |
| 4 | Học sinh | Mở lại bài, bấm từng bước | Phần vẽ thêm của từng bước hiện dần trên hình, có "Vì sao nghĩ ra bước này?"; quay lại tab Gợi ý thì hình về trạng thái em tự vẽ | `shots/12` |
| 5 | Phụ huynh | Xem tiến độ, gửi tin nhắn | Số liệu khớp với việc An đã làm; giáo viên trả lời; tin nhắn hiện ở Tổng quan của giáo viên | `shots/13` |

Thêm: **Tự học** (vai Học sinh → Tự học → Dựng hình) có màn chờ bốn bước, và cảnh báo khi hình chưa qua kiểm chứng (`shots/21`).

## Hình 3D, bài giảng và hỏi đáp theo giáo trình (PO-33 đến PO-37, PO-41, PO-42)
Ảnh từng trạng thái nằm ở `shots/int/` (khung iPad, khung iPhone, và viewport điện thoại thật).

| Phần | Ở đâu | Nhập / thao tác | Phản hồi |
|---|---|---|---|
| Hình 3D tương tác (PO-35) | Học sinh → mở một bài có hình mẫu (6 bài trong kho đề: `p1`–`p6`); Giáo viên → chi tiết đề; hộp "Xem như học sinh" | Kéo để xoay, chụm hoặc lăn chuột để thu phóng; nút "Nhìn từ trước", "Nhìn từ trên", "Xoay tự do"; thanh công cụ: Chọn, Điểm (trung điểm, chia tỉ lệ, giao điểm), Đoạn, Mặt phẳng, Mở rộng (chạm cạnh để kéo dài, chạm mặt để mở rộng), Đo dài, Đo góc, Đặt lại | Hình vẽ lại theo góc nhìn, cạnh khuất thành nét đứt; mỗi thao tác có thông báo ngắn và được tính vào hoạt động của học sinh ở màn Theo dõi lớp. Trạng thái hình được giữ riêng cho từng bài |
| Giới hạn đo (PO-08, PO-42, PO-22) | Màn làm bài | Đo đại lượng đề hỏi trước giờ mở lời giải | Số đo ghi "ẩn tới khi mở lời giải"; ở bài hình chóp cả góc SDA và góc ASD đều ẩn. Sau khi giáo viên mở lời giải, số đo hiện đủ. Đề tự học trùng bài đang khóa vẫn kế thừa khóa (PO-16) |
| Lời giải theo bước (PO-36) | Tab Lời giải, sau giờ mở | Bấm từng bước | Phần vẽ thêm của bước đó hiện dần trên chính hình học sinh đang dùng; về tab Gợi ý thì hình trở lại phần học sinh tự vẽ |
| Từ khóa và bài giảng (PO-34) | Gợi ý, lời giải, câu trả lời của trợ lý | Bấm chữ gạch chân chấm | Mở bài giảng: tên khái niệm, nội dung ngắn, hình nhỏ, dòng nguồn ("Bài soạn của giáo viên · tên tài liệu, mục" với nhãn "Giáo viên đã duyệt", hoặc "Bài giảng do AI viết" với nhãn "AI soạn · chờ giáo viên duyệt"), từ khóa liên quan, nút "Hỏi thêm về khái niệm này" |
| Duyệt bài giảng | Giáo viên → Giáo trình → khối "Bài giảng và khái niệm" | Bấm một dòng để đọc, bấm "Duyệt" ở bài do AI viết | Nhãn đổi thành "Giáo viên đã duyệt" ở cả phía học sinh |
| Hỏi đáp theo giáo trình (PO-33, PO-37) | Tab Hỏi đáp | Bấm một trong 5 câu hỏi mẫu hoặc gõ câu hỏi | Câu trả lời lấy từ kho bài giảng, có từ khóa và dòng "Nguồn" bấm được. Trước giờ mở: không nêu đáp số, không đi vào từng bước. Câu hỏi ngoài giáo trình: "Giáo trình của lớp chưa có phần này", mời hỏi giáo viên. Sau giờ mở: có liên kết "Xem bước n" |
| Giáo trình là nguồn của bài được giao (PO-41) | Giáo viên → Giáo trình (đứng cạnh Kho đề trên thanh bên) | Soạn hoặc import, chỉnh bằng chat (gộp gợi ý, thêm gợi ý, đổi hướng lời giải, lời giải ngắn gọn), lưu, rồi "Giao bài" | Dòng bài trong giáo trình ghi chỉnh sửa; màn giao bài và bài học sinh làm dùng đúng gợi ý, lời giải đã chỉnh |

Kho bài giảng mẫu có 10 khái niệm: 7 bài lấy từ tài liệu mẫu `Giao-an-Hinh-khong-gian-11A1.docx`, 3 bài do AI viết (Tỉ số lượng giác trong tam giác vuông, Hai đường thẳng chéo nhau, Mặt cầu ngoại tiếp).

## Màn hình tầm nhìn (concept)
| Chặng | Màn hình | Tương tác |
|---|---|---|
| 2 | Thêm đề mới (Kho đề → Thêm đề); Hình học phẳng (Học sinh → Môn khác) | Duyệt đề đưa vào kho |
| 3 | Lớp học (thẻ "Tạo lớp mới") | — |
| 4 | Hàm số và đồ thị (Học sinh → Môn khác) | Kéo thanh trượt hệ số a |
| 5 | Chấm bài; Trợ lý giáo viên | Sửa điểm, nhận từng tiêu chí, duyệt; soạn nháp, duyệt vào kho |
| 6 | Hồ sơ học tập (Lớp học → Hồ sơ); Phụ huynh | Điểm chỉ hiện cho học sinh và phụ huynh sau khi giáo viên duyệt |
| 7 | Nhóm học | Gửi tin, trợ lý nhóm trả lời |
| 8 | Nhà trường | — |
| 9 | Cơ quan quản lý | — |

## Luồng soạn bài và giáo trình (concept · chặng 2, 5 và 6)
Vào bằng vai Giáo viên → mục **Giáo trình** ở thanh bên, hoặc nút **Soạn bài · Giáo trình** trên thanh trên cùng. Các màn này mang nhãn "Concept" (PO-27 đến PO-32). Ảnh từng trạng thái nằm ở `shots/curr/`.

| Bước | Màn hình | Nhập / thao tác | Phản hồi | Ảnh |
|---|---|---|---|---|
| A | Giáo trình chung (`gv-curr`) | Mở mục Giáo trình | Giáo trình lớp 11A1 theo chương → bài học → bài tập; mỗi bài ghi nguồn (Kho đề, Soạn với AI, Import tài liệu, Import ảnh) và hai nhãn: "Đã kiểm chứng" / "Cần xem lại", "AI soạn" / "Giáo viên đã duyệt" | `01` |
| B1 | Soạn với AI (`gv-compose`) | Chọn chủ đề, loại bài tập, số bài, mức độ, hướng lời giải, định dạng lời giải; gõ thêm yêu cầu hoặc bấm câu gợi ý; bấm gửi | Màn chờ, rồi bản nháp bên phải: mỗi bài có đề, hình, gợi ý 1 và 2, lời giải, đáp số, nhãn kiểm chứng và nhãn "AI soạn" | `02`–`04` |
| B2 | Soạn với AI | Chat: "Đổi bài 2 sang mức dễ hơn", "Viết lại lời giải bài 1 ngắn gọn hơn", "Thêm một gợi ý cho bài 3" (bấm câu mẫu hoặc gõ kèm số bài), rồi bấm "Nhận" ở đề xuất | Bản nháp đổi sau khi nhận; thẻ vừa đổi có viền cam và nhãn "Vừa đổi" | `05`–`07` |
| B3 | Soạn với AI | "Xem như học sinh" ở một bài; "Duyệt và lưu vào giáo trình" | Bản xem trước: gợi ý trước, lời giải khóa. Sau khi duyệt, bài vào giáo trình với nguồn "Soạn với AI", nhãn "Giáo viên đã duyệt" | `08`–`10` |
| C1 | Import (`gv-import`), tab Tài liệu | Chọn tệp mẫu `Giao-an-Hinh-khong-gian-11A1.docx`, chọn định dạng lời giải, bấm "Đọc và nhận diện" | Tiến trình bốn bước; kết quả chia hai nhóm: 6 bài tập (1 bài "Cần xem lại") và 7 bài giảng; bài đã có trong giáo trình được ghi chú và không lưu lại | `11`–`14`, `16` |
| C2 | Import, tab Ảnh chụp | "Chụp hoặc chọn nhiều ảnh" (3 trang vở mẫu), bỏ ảnh bằng ×, đổi thứ tự bằng ← →, bấm "Đọc và nhận diện" | Mỗi bài: bên trái là chữ đọc được, chỗ máy chưa chắc tô vàng, sửa rồi bấm ✓; bên phải là bài đã chuẩn hóa | `18`–`23` |
| C3 | Import | Chatbot "Sửa bằng AI": "Bài 2: viết lời giải theo hướng dùng hình chiếu", "Gộp gợi ý của bài 3 thành một gợi ý ngắn" (với tài liệu: "Bài 4: viết lời giải ngắn gọn hơn", "Gộp gợi ý của bài 5…"); "Xem như học sinh"; "Duyệt và lưu vào giáo trình" | Bài đổi theo lệnh sau khi bấm "Nhận"; sau khi duyệt, bài vào giáo trình với nguồn "Import tài liệu" hoặc "Import ảnh". Bài "Cần xem lại" vẫn lưu được và giữ nhãn đó | `15`, `17`, `24`–`28` |
| E | Giáo trình chung | Bấm "Giao bài" ở một bài | Mở màn giao bài sẵn có (chọn lớp, giờ mở lời giải) → Theo dõi lớp → học sinh thấy bài mới | `29`–`33` |
| D1 | Giáo trình riêng (`gv-personal`) | Chọn học sinh, bấm "Đề xuất giáo trình riêng" | Phần "đã vững" và "đang luyện" kèm bài và câu hỏi làm bằng chứng; lộ trình rút từ giáo trình chung, mỗi thay đổi có lý do | `34`–`39` |
| D2 | Giáo trình riêng | Bấm "Bỏ" / "Thêm" ở từng mục, rồi "Duyệt và áp dụng" | Lộ trình chuyển sang "Giáo viên đã duyệt · đang áp dụng" | `40`–`42` |
| D3 | Học sinh (`hs-path`), Phụ huynh | Vai Học sinh → thẻ "Lộ trình của em"; vai Phụ huynh → Tổng quan | Học sinh thấy đúng lộ trình đã duyệt; phụ huynh chỉ thấy một dòng tóm tắt | `43`–`49` |

### Soạn bài giảng, import bài giảng, hội thoại hai chiều (PO-45, PO-46)
Ảnh từng trạng thái nằm ở `shots/lect/` (`lect-test.js`).

| Phần | Ở đâu | Nhập / thao tác | Phản hồi | Ảnh |
|---|---|---|---|---|
| A. Soạn bài giảng với AI | Soạn với AI → loại nội dung "Bài giảng" | Chọn khái niệm (3 khái niệm giáo án chưa có), lớp (11A1 / 9B), có ví dụ hay không, độ dài, có hình hay không; gõ thêm yêu cầu; bấm gửi | Bản nháp bài giảng bên phải: tên khái niệm, nội dung, ví dụ, hình nhỏ, từ khóa liên quan, nhãn máy và nhãn "AI soạn" | `11`–`12` |
| A | Soạn với AI | Chat chỉnh (xem mục C); "Xem như học sinh"; "Duyệt và lưu vào giáo trình" | Bản xem trước là đúng hộp bài giảng học sinh mở từ từ khóa. Sau khi duyệt: bài giảng vào khối "Bài giảng và khái niệm" với nguồn "Soạn với AI", nhãn "Giáo viên đã duyệt"; học sinh bấm từ khóa thấy đúng bài này | `13`–`18` |
| B. Import tách cả bài giảng | Import, tab Tài liệu | Chọn tệp mẫu, "Đọc và nhận diện"; chuyển giữa hai nhóm "Bài tập · 6 bài" và "Bài giảng · 7 mục lý thuyết"; bỏ dấu chọn ở mục không muốn lưu; sửa qua chat theo số ("Bài giảng 2: …"); "Xem như học sinh"; "Duyệt và lưu" | Mỗi bài giảng ghi mục của giáo án và trạng thái đối chiếu với kho ("nội dung trùng khớp" / "sẽ cập nhật khi lưu"). Sau khi lưu: dòng bài giảng trong giáo trình ghi "Vừa cập nhật từ file" hoặc "Vừa đối chiếu · trùng khớp"; mục bỏ chọn không bị ghi | `19`–`29` |
| C. Hội thoại hai chiều | Soạn với AI và "Sửa bằng AI" của Import, cho cả bài tập lẫn bài giảng | Bấm câu mẫu hoặc gõ. Yêu cầu chưa rõ ("Sửa lại bài 2", "Sửa lại bài giảng") | AI hỏi lại kèm lựa chọn bấm được, chưa đổi gì | `01`, `30` |
| C | như trên | Lệnh rõ ("Đổi bài 2 sang mức dễ hơn", "Thêm một ví dụ có số cụ thể"…) | Đề xuất hiện trên chính thẻ bị đổi: Trước → Sau, "Vì sao" (dựa trên định hướng hay mục nào của tài liệu), chỗ AI chưa chắc; nút "Nhận" / "Bỏ". Nhận xong có dòng máy kiểm lại và nút "Hoàn tác" | `02`–`06`, `13`–`14`, `22`–`23`, `26` |
| C | như trên | Bấm vào chữ của đề, gợi ý, nội dung bài giảng hoặc ví dụ rồi gõ lại (Enter hoặc bấm ra ngoài để xong) | Nhãn "Thầy cô đã sửa"; AI phản hồi đã kiểm lại, cảnh báo nếu gợi ý nêu đáp số, hoặc chuyển nhãn máy sang "Cần xem lại" nếu số liệu của đề bị đổi | `07`–`08`, `15` |
| C | như trên | Bấm "Lịch sử thay đổi (n)" | Danh sách ai đổi gì: AI đề xuất (thầy cô đã nhận), thầy cô bỏ, hoàn tác, sửa tay | `09`, `27` |
| C | như trên | Không bấm duyệt | Giáo trình và kho bài giảng không đổi; đề xuất chưa quyết không được lưu | `10` |

Kho bài giảng ở trạng thái ban đầu vẫn có 7 bài nguồn giáo án (kịch bản chính cần chúng), nên khi import lại giáo án, hệ thống đối chiếu với kho thay vì tạo bài mới.

Nội dung của luồng này là dữ liệu soạn sẵn: bản nháp lấy từ một nhóm bài cố định theo chủ đề và loại bài đã chọn; lựa chọn mức độ và hướng lời giải được ghi lại trong lời nhắn nhưng không sinh bài mới; chatbot hiểu sáu loại lệnh cho bài tập (dễ hơn, ngắn gọn hơn, từng bước kèm vì sao, theo hướng hình chiếu, thêm gợi ý, gộp gợi ý) kèm số bài, và bốn loại cho bài giảng (thêm hoặc bỏ ví dụ, viết dễ hiểu hơn, rút ngắn, thêm hoặc bỏ hình); câu không khớp thì AI hỏi lại. Ví dụ minh họa soạn sẵn cho 5 bài giảng. Việc "máy kiểm lại" sau khi sửa tay chỉ so số liệu và kí hiệu trước và sau, không tính lại bài. Tệp tài liệu không được đọc thật; ba trang vở là hình vẽ minh họa.

## Trình chiếu có chú thích
Bấm **▶ Trình chiếu** trên thanh trên cùng rồi chọn một trong ba kịch bản:

| Kịch bản | Số bước | Nội dung |
|---|---|---|
| Kịch bản chính: từ giao bài tới phụ huynh | 22 | Luồng MVP. Đoạn học sinh: xoay hình → vẽ thêm trung điểm, đoạn, mở rộng mặt đáy → đo và thấy đại lượng đề hỏi bị ẩn → gợi ý 1 → bấm từ khóa, đọc bài giảng → gợi ý 2 → hỏi trợ lý, nhận câu trả lời có nguồn → hỏi đáp số, bị từ chối → (giáo viên mở lời giải) → xem từng bước, hình được vẽ thêm dần |
| Tầm nhìn: 9 chặng | 12 | Các màn hình concept từ chặng 2 đến chặng 9 |
| Soạn bài và giáo trình | 25 | Mở giáo trình → định hướng và soạn bài tập với AI → hội thoại hai chiều chỉnh bản nháp (AI hỏi lại → giáo viên trả lời, đề xuất trước → sau → nhận → bỏ, hoàn tác → sửa tay, lịch sử) → xem như học sinh → duyệt và lưu → soạn một bài giảng với AI → chỉnh qua chat, AI nêu chỗ chưa chắc → xem bài giảng như học sinh → duyệt và lưu → import tài liệu mẫu: nhóm Bài tập và nhóm Bài giảng → chọn mục, sửa bằng chat → lưu → chọn nhiều ảnh, đọc chữ, sửa chỗ máy chưa chắc → sửa bằng chatbot → lưu → duyệt một bài giảng do AI viết → giao một bài cho lớp → giáo trình riêng cho An → học sinh (iPad) thấy lộ trình → phụ huynh (iPhone) thấy dòng tóm tắt |

Điều khiển: nút **Trước**, **Tiếp**, **Tự chạy**, **Thoát**; phím ← → chuyển bước, Space tự chạy, Esc thoát. Ảnh từng bước nằm ở `shots/tour/` (`mvp-01` đến `mvp-22`, `vision-01` đến `vision-12`, `curr-01` đến `curr-25`).

## Giới hạn của bản mẫu
- Hình 3D tương tác (`fig3d.js`) có cho 6 bài của kho đề. Các bài bổ sung trong luồng soạn bài và import giữ hình tĩnh, kèm ghi chú trên màn làm bài.
- Thanh công cụ không còn nút Oxyz vì bộ hình chưa có hệ trục tọa độ. Trên khung iPhone các nút góc nhìn bị ẩn; xoay bằng cách kéo.
- Câu trả lời của trợ lý là câu soạn sẵn: nhận ra câu hỏi theo từ khóa (khái niệm trong kho bài giảng, hỏi đáp số, hỏi về một bước, hỏi hướng làm); câu không khớp được coi là ngoài giáo trình.
- Bài giảng là nội dung mẫu viết sẵn; tệp tài liệu không được đọc thật, số mục chỉ để minh họa.
- Trong trình chiếu, thao tác trên hình được gọi qua API của hình (không mô phỏng ngón tay), nên không có hiệu ứng "bấm" tại điểm chạm.
- Bố cục thiết kế cho màn hình rộng từ 1280px.

## Chặng 2–3 (S23, v0.1 · 26/11/2026)
Mở `s23-screens.html` (hoặc nút "Chặng 2–3 (S23)" góc phải dưới của `index.html`). Hàng trên cùng chuyển giữa 24 màn C2/C3 (Web, iPad, iPhone theo STAGES §0.2). Ảnh chụp và danh sách câu hỏi design: `../s23/` (`DQ.md`). Chụp lại: `node Design/s23/shoot.js`.
