# DQ · Câu hỏi design chặng 2–3 (S23) · bản mẫu màn C2/C3
Phiên bản: v0.2 · 2026-11-26

Nguồn: `STAGES-AFTER-MVP.md` §0.2 và §6 chặng 2, 3 (22/11); `.claude/plans/STAGE-2-3-PLAN.md` §1, §5 (Q3–Q9); `PO-DECISIONS.md` PO-61, PO-02, PO-38 (26/11/2026).
Bản mẫu: `Design/prototype/s23-screens.html` (+ `s23.js`, `s23b.js`, `s23.css`, `s23-base.css`). Chụp lại: `node Design/s23/shoot.js`.
Mọi câu dưới đây đều đã có mặc định đang dùng (TẠM) trong bản mẫu. Chủ đầu tư/PO chỉ cần sửa chỗ không đồng ý.

## A. Câu hỏi design

| # | Câu hỏi | Mặc định đang dùng (TẠM · tin cậy · nguồn) | Màn |
|---|---|---|---|
| DQ-01 | Thêm đề: dán chữ và tải ảnh cùng một màn hay hai luồng? | Một màn, hai tab "Dán chữ / Tải ảnh". TẠM · vừa · Ch2 §6 C2-02 | C2-02 |
| DQ-02 | Ảnh mờ: chặn hay cho đi tiếp? | Không chặn: tô vàng ô chưa chắc, giáo viên sửa rồi "Xác nhận chữ và dựng tiếp"; nhãn máy giữ "Cần xem lại" (PO-38). TẠM · cao · Q4 nghiêng "ô chưa chắc tô để GV sửa" | C2-02 |
| DQ-03 | Máy không dựng được hình: các lối đi tiếp? | 3 lối: bổ sung điều kiện, dựng tay, lưu bản nháp chữ (chưa giao được). TẠM · vừa · Ch2 §6 | C2-02 |
| DQ-04 | Đáp số đề ghi lệch máy tính lại? | Chặn duyệt, không có đường tắt; hiện bảng đối chiếu Đề ghi / AI giải / Máy tính lại, 3 cách xử lý. TẠM · cao · STAGES §0.2 nguyên tắc 2, 3 | C2-02, C2-03 |
| DQ-05 | Chưa kiểm chứng: giáo viên có duyệt được không? | Duyệt được nhưng cảnh báo nhãn "Cần xem lại" giữ nguyên đến khi máy kiểm đạt (PO-38). TẠM · vừa · PO-38 | C2-03 |
| DQ-06 | Sửa lời giải đã giao? | Bản đã giao khóa; sửa tạo phiên bản mới (v3), bài đã giao giữ v2 đến khi chọn "Áp dụng cho lớp". TẠM · vừa · Ch2 §6 "đã giao (chỉ tạo phiên bản mới)" | C2-03 |
| DQ-07 | Nhãn hai trục hiện ở đâu? | Luôn hiện cùng nhau ở đầu thẻ hình và trong hộp duyệt (máy: Đã kiểm chứng / Cần xem lại; người: AI soạn / GV đã duyệt). TẠM · cao · PO-02 | C2-03 |
| DQ-08 | Trình chiếu: thao tác bước? | Nút "Hiện bước kế" và "Bước trước", phím →/←, nút "Che lời giải" toàn cục. TẠM · vừa | C2-04 |
| DQ-09 | Trình chiếu có làm lộ lời giải trên máy học sinh? | Không: chip "Máy học sinh vẫn khóa lời giải"; trình chiếu chỉ là màn của giáo viên. TẠM · cao · STAGES §0.2 nguyên tắc 1 | C2-04 |
| DQ-10 | Mất mạng khi trình chiếu? | Tải sẵn đề, hình, lời giải khi vào chế độ; chip "Đã tải sẵn · mất mạng vẫn chiếu". TẠM · vừa · Ch2 §6 trạng thái "mất mạng" | C2-04 |
| DQ-11 | Bàn phím toán: dùng bàn phím hệ thống hay bàn phím riêng? | Bàn phím riêng 6 cột: số, ( ), ⌫, + − × ÷, √, π, ^, °, a, x, Xóa hết, Kiểm tra; iPad và iPhone cùng bố cục, iPhone nén hàng. TẠM · vừa | C2-06 |
| DQ-12 | Số lượt thử và sai số hiển thị? | Chip "Còn n/3 lượt" và dòng "Chấp nhận sai số ±0,5°"; sai: gợi ý theo hướng, không lộ đáp số; hết lượt: lời giải còn khóa, vẫn xem gợi ý. TẠM · vừa · STAGES §0.2 nguyên tắc 1 | C2-06 |
| DQ-13 | Kết quả bài luyện có số tổng không? | Không: "x câu đúng / y câu cần xem lại", không điểm, không xếp hạng. TẠM · vừa · Q6 nghiêng | C2-07 |
| DQ-14 | Soạn bài luyện: đề chưa có đáp số máy chấm được? | Hiện nhưng không chọn được (nét đứt, chip vàng), chỉ dẫn thêm đáp số ở Soát lời giải. TẠM · vừa · Ch2 §6 C2-08 | C2-08 |
| DQ-15 | Đăng ký: hỏi gì? | Chỉ tên hiển thị, vai (Giáo viên / Học sinh), mật khẩu tự đặt, tick "đã đọc thông báo riêng tư (bản 1.0)". Không email, SĐT, ngày sinh, trường, ảnh. FINAL · PO-61 | C3-01 |
| DQ-16 | Hiện mã sau đăng ký? | Mã người dùng dạng `GV-XXXX-XXXX` (học sinh `HS-…`) + mã khôi phục `RC-XXXX-XXXX-XXXX` chỉ hiện một lần, bắt tick "đã lưu" mới đi tiếp, có nút Sao chép và In/lưu ảnh. Định dạng mã là TẠM · vừa · PO-61 | C3-01 |
| DQ-17 | Học sinh có mã khôi phục không? | Không (chốt PO-61): quên mật khẩu thì giáo viên của lớp đặt lại. Giáo viên dùng mã khôi phục (TẠM · vừa · PO-61). Học sinh tự học không có lớp mà quên mật khẩu: chưa có đường khôi phục, chấp nhận tạo tài khoản mới (câu mở với PO) | C3-01, C3-07 |
| DQ-18 | Đăng nhập: các ô và lỗi? | Hai ô: mã người dùng + mật khẩu; thông báo lỗi chung "Mã hoặc mật khẩu chưa đúng" (không nói sai ô nào) và giới hạn thử; hai dòng hướng dẫn quên mật khẩu theo vai. TẠM · vừa · PO-61, Q3 | C3-01 |
| DQ-19 | Bộ bài: xung đột giờ? | Cảnh báo đỏ ngay trong cột cài đặt khi giờ mở lời giải sớm hơn hạn làm, kèm hai cách sửa; không chặn giao nhưng nút Giao chờ giáo viên chọn. TẠM · vừa · Ch3 §6 C3-03 | C3-03 |
| DQ-20 | Bộ bài có đề "Cần xem lại": cho giao không? | Dòng đề viền vàng, chip "Cần xem lại trước khi giao" + banner; giáo viên quyết định cuối. TẠM · vừa · STAGES §0.2 nguyên tắc 3 | C3-03 |
| DQ-21 | Bảng lớp × bài: có cột tổng, sắp hạng? | Không; xếp theo tên; ô 3 trạng thái Đúng / Xem lại / Chưa làm (+ Đang làm); bấm ô ra bằng chứng. TẠM · cao · Q6 nghiêng, PLAN §3 C3-04 | C3-04 |
| DQ-22 | "Cả lớp vướng ở đâu": khi nào hiện? | Chỉ khi đủ 5 bài làm cho một đề; nhãn "AI gộp" + "Cần thầy cô xác nhận"; mỗi thẻ có nút xem bài làm căn cứ. TẠM · vừa · Q6 nghiêng "5 bài" | C3-04 |
| DQ-23 | Mã lớp: định dạng và đặt lại? | `HK-4P7Q`, đặt lại mã thì mã cũ hết hiệu lực; công tắc "Cho vào lớp bằng mã" và "Giáo viên duyệt từng em" (bật mặc định). TẠM · vừa · PO-61 | C3-07 |
| DQ-24 | Trang in cho lớp? | Một trang A4: logo, tên lớp, mã QR, mã lớp lớn, 4 bước vào lớp. Mã QR trong bản mẫu là hình giả. TẠM · vừa | C3-07 |
| DQ-25 | Trùng tên trong lớp? | Phân biệt bằng mã người dùng ở dòng chờ duyệt và bảng lớp, không hỏi thêm ký hiệu. FINAL một phần · PLAN §1 "PO-61 bỏ/đơn giản hóa" | C3-07 |
| DQ-26 | Đặt lại mật khẩu học sinh? | Giáo viên bấm "Đặt lại mật khẩu" → hộp xác nhận → mật khẩu tạm hiện một lần, em phải tự đặt mật khẩu mới ở lần đăng nhập đầu. Chốt chủ đề ở PO-61 (26/11); chi tiết mật khẩu tạm là TẠM · vừa | C3-07 |
| DQ-27 | Học sinh nhập mã lớp, chờ duyệt? | Màn một thẻ giữa trang; sau khi gửi hiện "chờ duyệt", em vẫn dùng Tự học trong lúc chờ (khớp Q9 "cho tự học không lớp"). Trạng thái mã sai / bị từ chối: chưa vẽ ảnh riêng, mô tả bằng dòng chữ. TẠM · vừa | C3-07 |
| DQ-28 | Thiết bị từng màn? | Theo STAGES §0.2: giáo viên Web; học sinh iPad (chính) và iPhone (phụ). Đăng nhập chụp ở iPad vì học sinh là người dùng chính. TẠM · cao | tất cả |

## A2. Câu hỏi design phát sinh khi vẽ màn mới (Designer Dom, v0.2)
Tất cả TẠM, chờ owner/PO duyệt. Ảnh mới ở `Design/s23/`; nguồn nhận xét `REVIEW-Dom.md` v0.1.
| # | Câu | Mặc định đang dùng (TẠM) | Màn / ảnh |
|---|---|---|---|
| DQ-29 | Học sinh đang làm bài trên iPhone: có ẩn thanh điều hướng dưới? (R-08) | Chế độ tập trung: ẩn thanh dưới, nút "Thoát" có hộp xác nhận. TẠM · vừa | C2-06 `c2-06-luyen-iphone.png` |
| DQ-30 | Sửa lời giải đã giao: so sánh v2↔v3 và nút "Áp dụng cho lớp" ở đâu? (R-07, DQ-06) | Nút trong hộp duyệt + màn so sánh hai phiên bản trước khi áp dụng. Chưa vẽ riêng màn so sánh. TẠM · vừa | C2-03 `c2-03-da-giao.png` |
| DQ-31 | Chat chỉnh nháp đề với AI: AI hỏi lại khi nào, hiện trước/sau ra sao? | AI chỉ hỏi khi thiếu dữ liệu để dựng hình; mỗi lượt sửa hiện trước/sau cạnh nhau, giáo viên chấp nhận hoặc hoàn tác từng lượt. TẠM · vừa | C2-04a `c2-04a-chat-chinh-nhap.png` |
| DQ-32 | Import docx/pdf tách bài: tách sai thì sửa thế nào? Trùng kho xử lý ra sao? | Danh sách bài đã tách, giáo viên gộp/tách/bỏ từng bài; đối chiếu trùng hiện bài cũ cạnh bài mới, 3 lựa chọn: giữ cả hai / dùng bài cũ / thay bằng bài mới. TẠM · vừa | C2-04b `c2-04b-import-tach-bai.png`, C2-04c `c2-04c-doi-chieu-trung.png` |
| DQ-33 | Tệp import lỗi (sai định dạng, quá nặng, có mật khẩu)? | Một thông báo nêu đúng lý do + cách xử lý, không mất phần đã tách được. TẠM · cao | C2-04d `c2-04d-import-loi-tep.png` |
| DQ-34 | Bàn phím biểu thức nâng cao (phân số, căn lồng, mũ) mở bằng gì? | Phím "Nâng cao" trên bàn phím 6 cột mở bảng phụ; xem trước biểu thức dạng toán phía trên ô nhập; chấm theo dạng tương đương. TẠM · vừa | C2-08 `c2-08-ban-phim-nang-cao.png` |
| DQ-35 | Bài luyện đang tải và mất mạng khi gửi đáp số? | Đang tải: khung xương, không chặn. Mất mạng: đáp số giữ trên máy, hiện "Chưa gửi", tự gửi lại khi có mạng, không trừ lượt khi chưa gửi được. TẠM · vừa | C2-06 `c2-06-dang-tai.png`, `c2-06-mat-mang.png` |
| DQ-36 | Học sinh nhập mã lớp sai / bị từ chối? | Mã sai: báo tại chỗ, không nói mã có tồn tại hay không, cho thử lại; bị từ chối: lời trung tính, em vẫn dùng Tự học. TẠM · cao | C3-07 `c3-07-ma-sai.png`, `c3-07-bi-tu-choi.png` |
| DQ-37 | Nhãn nội bộ (TẠM, Q6, DQ) có hiện trong app? (R-11) | Không; chỉ ở khung flow của bản mẫu để owner thấy giả định, tách thành chú thích handoff. TẠM · cao | tất cả |
| DQ-38 | Biến thể đề, xuất ảnh/PDF, liên kết chỉ xem: ai xem được liên kết? | Liên kết chỉ xem không cần đăng nhập, không hiện lời giải nếu chưa mở, giáo viên thu hồi được. TẠM · thấp (CẦN OWNER: có thu dữ liệu người xem không, PO-61) | C2-09a `c2-09a-bien-the.png`, `c2-09b-xuat-lien-ket.png` |
| DQ-39 | Trắc nghiệm: giáo viên soạn và học sinh làm ra sao? | Giáo viên chọn đáp án đúng + giải thích; học sinh chọn một đáp án, thấy đúng/sai và gợi ý, không điểm, không xếp hạng. TẠM · vừa | C2-09c `c2-09c-trac-nghiem-gv.png`, `c2-09d-trac-nghiem-hs.png` |
| DQ-40 | Thông báo trong ứng dụng: gom, đọc/chưa đọc, không đẩy? | Danh sách theo ngày, chấm chưa đọc, chạm đi tới màn liên quan; chỉ trong ứng dụng (khớp Q_Dom D3). TẠM · vừa | C3-04 `c3-04-thong-bao-ipad.png`, `c3-04-thong-bao-iphone.png` |
| DQ-41 | Chữa bài từ kết quả, xuất kết quả lớp, lưu trữ lớp cuối năm? | Từ bảng lớp mở thẳng Trình chiếu bài ít em làm được; xuất bảng không chứa dữ liệu cá nhân ngoài tên hiển thị + mã; lưu trữ lớp: chỉ đọc, xác nhận hai bước. TẠM · vừa | C3-06 `c3-06a-chua-bai-trinh-chieu.png`, `c3-06b-xuat-ket-qua.png`, `c3-06c-luu-tru-lop.png` |
| DQ-42 | Trạng thái rỗng/lỗi chung (lớp chưa có học sinh, kho trống, AI lỗi, hết phiên)? | Một bộ mẫu chung: biểu tượng + một câu nêu tình huống + một hành động; hết phiên giữ bản nháp. TẠM · cao | C8 `c8-trang-thai-gv.png`, `c8-trang-thai-hs.png` |
| DQ-43 | Màn hiện mã chưa tick "đã lưu" thì khóa gì? (R-05, DQ-16) | Nút "Tiếp tục" xám + dòng nêu lý do khóa; tick xong mới sáng. TẠM · cao | C3-01 `c3-01-ma-chua-tick.png` |

Chưa vẽ (ngoài số lượng ảnh yêu cầu, ghi để vòng sau): trạng thái "đang tải" và "mất mạng khi gửi" của bài luyện; bàn phím biểu thức nâng cao; màn "mã sai / bị từ chối" của C3-07; C3-02, C3-05, C3-06, C3-08 đã có concept sẵn nên không vẽ lại.

## B. Bảng màn ↔ ảnh
Khổ ảnh: Web 1440×900 · iPad 1180×820 · iPhone 390×844. Mở bản mẫu: `Design/prototype/s23-screens.html#<id>`.

| Mã màn | Trạng thái | Thiết bị | Ảnh (`Design/s23/`) | Status |
|---|---|---|---|---|
| C2-02 | Nhập (dán chữ / ảnh) | Web | `c2-02-nhap.png` | Pass |
| C2-02 | Đang dựng, 4 bước | Web | `c2-02-dang-dung.png` | Pass |
| C2-02 | Ngoại lệ: ảnh mờ | Web | `c2-02-anh-mo.png` | Pass |
| C2-02 | Ngoại lệ: engine không dựng được | Web | `c2-02-engine-khong-dung.png` | Pass |
| C2-02 | Ngoại lệ: lệch đáp số | Web | `c2-02-lech-dap-so.png` | Pass |
| C2-03 | Chưa kiểm chứng | Web | `c2-03-chua-kiem.png` | Pass |
| C2-03 | Lệch đáp số, chặn duyệt | Web | `c2-03-lech-chan-duyet.png` | Pass |
| C2-03 | Đã giao, chỉ tạo phiên bản mới | Web | `c2-03-da-giao.png` | Pass |
| C2-04 | Trình chiếu, hiện từng bước | Web | `c2-04-trinh-chieu.png` | Pass |
| C2-04 | Che lời giải | Web | `c2-04-che-loi-giai.png` | Pass |
| C2-06 | Bài luyện + bàn phím toán | iPad | `c2-06-luyen-ipad.png` | Pass |
| C2-06 | Bài luyện + bàn phím toán | iPhone | `c2-06-luyen-iphone.png` | Pass |
| C2-06 | Sai, có gợi ý | iPad | `c2-06-sai-goi-y.png` | Pass |
| C2-06 | Hết lượt | iPhone | `c2-06-het-luot.png` | Pass |
| C2-07 | Kết quả bài luyện | iPad | `c2-07-ket-qua.png` | Pass |
| C2-08 | Soạn bài luyện | Web | `c2-08-soan-bai-luyen.png` | Pass |
| C3-01 | Đăng ký bằng tên + vai | Web | `c3-01-dang-ky.png` | Pass |
| C3-01 | Hiện mã người dùng + mã khôi phục | Web | `c3-01-ma-nguoi-dung.png` | Pass |
| C3-01 | Đăng nhập mã + mật khẩu | iPad | `c3-01-dang-nhap.png` | Pass |
| C3-03 | Soạn bộ bài, xung đột giờ | Web | `c3-03-bo-bai.png` | Pass |
| C3-04 | Bảng lớp × bài + Cả lớp vướng ở đâu | Web | `c3-04-ket-qua-lop.png` | Pass |
| C3-07 | Mã lớp + trang in | Web | `c3-07-ma-lop.png` | Pass |
| C3-07 | Duyệt vào lớp + đặt lại mật khẩu HS | Web | `c3-07-duyet-dat-lai-mk.png` | Pass |
| C3-07 | HS nhập mã lớp, chờ duyệt | iPad | `c3-07-vao-lop.png` | Pass |

Status "Pass" ở đây nghĩa là ảnh chụp ra và không lỗi JS; chưa có người dùng duyệt thiết kế (UT: chờ PO/chủ đầu tư xem).

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 28 câu design, bảng màn ↔ 24 ảnh | agent runner-T-261126-10 |
| v0.2 | 2026-11-26 | Thêm mục A2: DQ-29…DQ-43 (màn mới C2-04a–d, C2-06 tải/mất mạng, C2-08 nâng cao, C2-09a–d, C3-04 thông báo, C3-06a–c, C3-07 lỗi, C3-01 chưa tick, C8); đồng bộ REVIEW-Dom | runner-design-dom |
