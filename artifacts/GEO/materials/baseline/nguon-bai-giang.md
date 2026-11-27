Phiên bản: v1.0 · 27/11/2026

# Danh mục nguồn bài giảng / kiến thức Toán làm baseline kiểm chứng

Dữ liệu đầy đủ: `nguon-bai-giang.json` (41 nguồn, mã BG-001…BG-041). Mọi mã http được kiểm ngày 27/11/2026 bằng `curl -sIL`, nếu lỗi thì thử lại bằng GET có User-Agent trình duyệt.
Chưa trùng với `lop-11/nguon-tong-hop.md` ở cấp trang cụ thể (tệp đó chỉ dẫn trang bài của loigiaihay/vietjack/nxbgd; ở đây chỉ lấy cấp miền).

## Bảng tóm tắt

| Loại | Số nguồn | Tin cậy A | Tin cậy B | Tin cậy C |
|---|---|---|---|---|
| sgk-chinh-thuc | 4 | 4 | 0 | 0 |
| tai-lieu-bo | 3 | 2 | 1 | 0 |
| tong-hop-kien-thuc | 5 | 0 | 5 | 0 |
| giao-vien | 6 | 0 | 5 | 1 |
| nen-tang | 7 | 0 | 7 | 0 |
| quoc-te | 13 | 6 | 7 | 0 |
| forum | 3 | 0 | 0 | 3 |
| Tổng | 41 | 12 | 25 | 4 |

Link sống: 37/41 (90,2%) là 2xx/3xx. Bốn link còn lại trả 403 với curl (chặn bot, mở bằng trình duyệt được): BG-034 olm.vn, BG-039 diendantoanhoc.org, BG-040 math.stackexchange.com, BG-041 artofproblemsolving.com.

### Giáo viên (căn cứ uy tín, số người đăng ký đo ngày 27/11/2026)

| Mã | Giáo viên | Căn cứ | Mức |
|---|---|---|---|
| BG-027 | Thầy Liêm | khoảng 1,45 triệu đăng ký | B |
| BG-028 | Thầy Nguyễn Quốc Chí | khoảng 899 nghìn đăng ký; báo toplist.vn nêu lượng học sinh lớn (nguồn thứ cấp) | B |
| BG-029 | Thầy Nguyễn Phan Tiến | khoảng 639 nghìn đăng ký, có khóa Toán 10-12 | B |
| BG-030 | Toán Thầy Kiên (THCS) | khoảng 50,2 nghìn đăng ký | B |
| BG-031 | Thầy Hồ Thức Thuận (Mclass.vn) | Zing News: hơn 20.000 học viên, 365 nghìn theo dõi cá nhân, 9 năm luyện thi | B |
| BG-032 | Thầy Đặng Việt Hùng | chỉ khoảng 4,95 nghìn đăng ký, chưa xác minh sách/đơn vị | C |

Lưu ý: đơn vị công tác và sách đã xuất bản của các thầy trên chưa xác minh được từ nguồn chính thức. Số người đăng ký chỉ là căn cứ về mức phổ biến, không chứng minh độ chính xác chuyên môn. Cần soát tay trước khi dùng một bài giảng làm đối chiếu.

## Đề xuất dùng làm baseline kiến thức

Nguyên tắc: một phát biểu của AI chỉ được coi là đúng khi khớp một nguồn "chuẩn phát biểu" (tầng 1) và không mâu thuẫn với nguồn đối chiếu (tầng 2). Nguồn tầng 3 chỉ để tham khảo.

Top 10:
1. Hành trang số NXBGDVN (SGK KNTT, đọc online) - chuẩn phát biểu định nghĩa, định lí, ký hiệu.
2. Hành trang số - mục Cánh Diều - đối chiếu thứ 2 khi bộ KNTT diễn đạt khác.
3. Cổng Bộ GD&ĐT (moet.gov.vn) - chuẩn yêu cầu cần đạt và ranh giới nội dung theo Chương trình GDPT 2018.
4. Taphuan.nxbgd.vn (SGV, tập huấn) - phương pháp dạy học và mạch kiến thức.
5. Wolfram MathWorld (Solid Geometry) - chuẩn công thức và định nghĩa hình học.
6. OpenStax Precalculus 2e - chuẩn lượng giác, hàm số, vectơ.
7. Paul's Online Math Notes - đối chiếu giới hạn, đạo hàm (Toán 11).
8. Euclid's Elements (D. Joyce) - gốc tiên đề và định lí hình học.
9. Loigiaihay và VietJack (lý thuyết bám SGK) - đối chiếu thứ 2 phát biểu từng bài; khi lệch SGK thì theo SGK.
10. ProofWiki - kiểm chứng chứng minh định lí do AI nêu.

Cách dùng:
- Chuẩn phát biểu: SGK (KNTT) + Chương trình GDPT 2018. Nếu bài giảng AI khác SGK về định nghĩa thì sửa theo SGK.
- Đối chiếu thứ 2: Cánh Diều/CTST, Loigiaihay, VietJack, MathWorld.
- Kiểm chứng chứng minh/công thức: ProofWiki, MathWorld, OpenStax, Lamar.
- Trực quan/phương pháp: GeoGebra, 3Blue1Brown, bài giảng giáo viên (B). Không dùng làm chuẩn phát biểu.
- Forum (C): chỉ để tìm lập luận, luôn kiểm chéo với tầng 1.

## Rủi ro bản quyền

- SGK, SGV và nội dung NXBGDVN: chỉ ghi mô tả và link, không chép nguyên văn, không dùng bản PDF lậu.
- Loigiaihay, VietJack, HOC247, ToanMath và các nền tảng: nội dung có bản quyền riêng; không sao chép hàng loạt hay cào dữ liệu để đưa vào app. Chỉ dùng để đối chiếu.
- Video của giáo viên: chỉ link; không tải về, không trích đoạn.
- Giấy phép mở (dùng được kèm ghi nguồn): Wikipedia và ProofWiki (CC BY-SA), OpenStax (CC BY 4.0), MIT OCW và Khan Academy (CC BY-NC-SA, hạn chế thương mại). Nếu app thương mại, bản NC không dùng để chép nội dung.
- Chính sách mỗi trang có thể thay đổi; cần xác nhận lại trước khi tích hợp.

## Hạn chế của lần tra cứu này

- WebSearch cho thông tin ít về đơn vị công tác, sách của giáo viên; chưa nêu được thông tin này.
- Không có nguồn nào trong danh sách này đã được đọc kỹ từng bài; đây là danh mục nguồn, chưa phải kiểm chứng nội dung.

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.0 | 27/11/2026 | Lập danh mục 41 nguồn, kiểm link, đề xuất baseline | Agent runner-baseline-baigiang |
