# Tần suất chủ đề theo kỳ thi

Phiên bản: v1.0 · 27/11/2026 · sinh tự động bởi `build_data.py` từ `de/*.json` (không sửa tay).

Cách đọc: ô = `số câu / điểm`. **Cố định** = xuất hiện ≥ 90 % số năm; **xoay vòng** = 30-90 %; **hiếm** = < 30 % (tính trên số năm của nhóm). 
Xu hướng = độ dốc hồi quy tuyến tính của tỉ trọng điểm (điểm chủ đề / 10) theo năm, đơn vị điểm phần trăm mỗi năm (pp/năm); |dốc| < 0,3 coi là ổn định; nhóm ≤ 2 năm: không đủ dữ liệu.
Lưu ý độ tin cậy: nhãn chủ đề các đề 2017-2024 gắn từ text đề (không phải bảng đặc tả của Bộ), mức độ là suy luận; đọc `README.md` mục Giới hạn.

## TN THPT cấu trúc cũ (50 câu TN4) - các năm 2017-2024 (8 đề)

| Chủ đề | 2017 | 2018 | 2019 | 2020 | 2021 | 2022 | 2023 | 2024 | % năm | Phân loại | Xu hướng |
|---|---|---|---|---|---|---|---|---|---|---|---|
| Hàm số mũ - logarit (`ham-so-mu-logarit`) | 10/2 | 6/1.2 | 7/1.4 | 9/1.8 | 8/1.6 | 8/1.6 | 8/1.6 | 9/1.8 | 100 % | co-dinh | ổn định (+0.12 pp/năm) |
| Ứng dụng đạo hàm (`ham-so-ung-dung-dao-ham`) | 8/1.6 | 7/1.4 | 8/1.6 | 6/1.2 | 6/1.2 | 8/1.6 | 4/0.8 | 8/1.6 | 100 % | co-dinh | giảm (-0.36 pp/năm) |
| Số phức (`so-phuc`) | 6/1.2 | 4/0.8 | 5/1 | 5/1 | 6/1.2 | 6/1.2 | 6/1.2 | 5/1 | 100 % | co-dinh | ổn định (+0.17 pp/năm) |
| Nguyên hàm - tích phân (`nguyen-ham-tich-phan`) | 4/0.8 | 4/0.8 | 5/1 | 4/0.8 | 6/1.2 | 6/1.2 | 6/1.2 | 5/1 | 100 % | co-dinh | tăng (+0.52 pp/năm) |
| Hàm số và đồ thị (`ham-so-do-thi`) | 4/0.8 | 5/1 | 4/0.8 | 6/1.2 | 5/1 | 3/0.6 | 6/1.2 | 2/0.4 | 100 % | co-dinh | giảm (-0.31 pp/năm) |
| HHKG - thể tích khối đa diện (`hinh-khong-gian-the-tich`) | 4/0.8 | 3/0.6 | 3/0.6 | 3/0.6 | 3/0.6 | 4/0.8 | 3/0.6 | 4/0.8 | 100 % | co-dinh | ổn định (+0.07 pp/năm) |
| Khối tròn xoay (`khoi-tron-xoay`) | 4/0.8 | 1/0.2 | 3/0.6 | 4/0.8 | 3/0.6 | 3/0.6 | 3/0.6 | 2/0.4 | 100 % | co-dinh | ổn định (-0.12 pp/năm) |
| Oxyz - mặt phẳng (`oxyz-mat-phang`) | 3/0.6 | 2/0.4 | 2/0.4 | 2/0.4 | 3/0.6 | 3/0.6 | 3/0.6 | 2/0.4 | 100 % | co-dinh | ổn định (+0.05 pp/năm) |
| Oxyz - đường thẳng (`oxyz-duong-thang`) | 2/0.4 | 3/0.6 | 3/0.6 | 2/0.4 | 2/0.4 | 2/0.4 | 1/0.2 | 2/0.4 | 100 % | co-dinh | giảm (-0.31 pp/năm) |
| Tổ hợp - xác suất cổ điển (`to-hop-xac-suat`) | - | 4/0.8 | 2/0.4 | 2/0.4 | 2/0.4 | 2/0.4 | 2/0.4 | 2/0.4 | 88 % | xoay-vong | ổn định (+0.10 pp/năm) |
| Oxyz - mặt cầu (`oxyz-mat-cau`) | 2/0.4 | 2/0.4 | 2/0.4 | 1/0.2 | 2/0.4 | 2/0.4 | 3/0.6 | 2/0.4 | 100 % | co-dinh | ổn định (+0.14 pp/năm) |
| HHKG - quan hệ song song, vuông góc (`hinh-khong-gian-quan-he`) | - | 4/0.8 | 2/0.4 | 3/0.6 | 2/0.4 | 1/0.2 | 2/0.4 | 2/0.4 | 88 % | xoay-vong | ổn định (+0.00 pp/năm) |
| Ứng dụng tích phân (`ung-dung-tich-phan`) | 2/0.4 | 3/0.6 | 2/0.4 | 1/0.2 | - | - | 1/0.2 | 2/0.4 | 75 % | xoay-vong | giảm (-0.40 pp/năm) |
| Oxyz - tọa độ điểm, vectơ (`oxyz-toa-do-vecto`) | - | 1/0.2 | 1/0.2 | 1/0.2 | 1/0.2 | 1/0.2 | 1/0.2 | 2/0.4 | 88 % | xoay-vong | tăng (+0.33 pp/năm) |
| Dãy số - cấp số (`day-so-cap-so`) | - | - | 1/0.2 | 1/0.2 | 1/0.2 | 1/0.2 | 1/0.2 | 1/0.2 | 75 % | xoay-vong | ổn định (+0.29 pp/năm) |
| Oxyz - tổng hợp (`oxyz-tong-hop`) | 1/0.2 | - | - | - | - | - | - | - | 12 % | hiem | ổn định (-0.17 pp/năm) |
| Giới hạn - liên tục (`gioi-han-lien-tuc`) | - | 1/0.2 | - | - | - | - | - | - | 12 % | hiem | ổn định (-0.12 pp/năm) |

Phân bố mức độ theo năm (số câu; mức độ là suy luận):

| Năm | NB | TH | VD | VDC |
|---|---|---|---|---|
| 2017 | 20 | 15 | 10 | 5 |
| 2018 | 20 | 15 | 10 | 5 |
| 2019 | 20 | 15 | 10 | 5 |
| 2020 | 20 | 15 | 10 | 5 |
| 2021 | 20 | 15 | 10 | 5 |
| 2022 | 20 | 15 | 10 | 5 |
| 2023 | 20 | 15 | 10 | 5 |
| 2024 | 20 | 15 | 10 | 5 |

## TN THPT cấu trúc mới (22 câu: 12 TN4 + 4 đúng/sai + 6 trả lời ngắn) - các năm 2025-2026 (2 đề)

| Chủ đề | 2025 | 2026 | % năm | Phân loại | Xu hướng |
|---|---|---|---|---|---|
| Ứng dụng tích phân (`ung-dung-tich-phan`) | 3/1.75 | 2/1.5 | 100 % | co-dinh | không đủ dữ liệu |
| Ứng dụng đạo hàm (`ham-so-ung-dung-dao-ham`) | 2/1.5 | 2/1.5 | 100 % | co-dinh | không đủ dữ liệu |
| Xác suất có điều kiện (`xac-suat-co-dieu-kien`) | 1/1 | 2/1.25 | 100 % | co-dinh | không đủ dữ liệu |
| Tổ hợp - xác suất cổ điển (`to-hop-xac-suat`) | 2/1 | 2/1 | 100 % | co-dinh | không đủ dữ liệu |
| HHKG - quan hệ song song, vuông góc (`hinh-khong-gian-quan-he`) | 3/1 | 2/0.75 | 100 % | co-dinh | không đủ dữ liệu |
| Hàm số mũ - logarit (`ham-so-mu-logarit`) | 2/1.25 | 1/0.25 | 100 % | co-dinh | không đủ dữ liệu |
| Phương trình, hệ, bất phương trình đại số (`phuong-trinh-he-bpt`) | 1/0.5 | 2/0.75 | 100 % | co-dinh | không đủ dữ liệu |
| Oxyz - mặt cầu (`oxyz-mat-cau`) | - | 1/1 | 50 % | xoay-vong | không đủ dữ liệu |
| Dãy số - cấp số (`day-so-cap-so`) | 1/0.25 | 2/0.5 | 100 % | co-dinh | không đủ dữ liệu |
| Nguyên hàm - tích phân (`nguyen-ham-tich-phan`) | 1/0.25 | 2/0.5 | 100 % | co-dinh | không đủ dữ liệu |
| Thống kê mẫu ghép nhóm (`thong-ke`) | 1/0.25 | 1/0.25 | 100 % | co-dinh | không đủ dữ liệu |
| Hàm số và đồ thị (`ham-so-do-thi`) | 1/0.25 | 1/0.25 | 100 % | co-dinh | không đủ dữ liệu |
| Oxyz - mặt phẳng (`oxyz-mat-phang`) | 1/0.25 | - | 50 % | xoay-vong | không đủ dữ liệu |
| Oxyz - tọa độ điểm, vectơ (`oxyz-toa-do-vecto`) | - | 1/0.25 | 50 % | xoay-vong | không đủ dữ liệu |
| Oxyz - đường thẳng (`oxyz-duong-thang`) | 1/0.25 | - | 50 % | xoay-vong | không đủ dữ liệu |
| HHKG - thể tích khối đa diện (`hinh-khong-gian-the-tich`) | 1/0.25 | - | 50 % | xoay-vong | không đủ dữ liệu |
| Lượng giác (`luong-giac`) | 1/0.25 | - | 50 % | xoay-vong | không đủ dữ liệu |
| Đạo hàm cơ bản (`dao-ham-co-ban`) | - | 1/0.25 | 50 % | xoay-vong | không đủ dữ liệu |

Phân bố mức độ theo năm (số câu; mức độ là suy luận):

| Năm | NB | TH | VD | VDC |
|---|---|---|---|---|
| 2025 | 11 | 2 | 6 | 3 |
| 2026 | 9 | 4 | 5 | 4 |

## ĐH khối A/A1 tự luận - các năm 2009-2014 (6 đề)

| Chủ đề | 2009 | 2010 | 2011 | 2012 | 2013 | 2014 | % năm | Phân loại | Xu hướng |
|---|---|---|---|---|---|---|---|---|---|
| Hàm số và đồ thị (`ham-so-do-thi`) | 2/2 | 2/2 | 2/2 | 1/1 | 1/1 | 2/2 | 100 % | co-dinh | giảm (-1.14 pp/năm) |
| Phương trình, hệ, bất phương trình đại số (`phuong-trinh-he-bpt`) | 1/1 | 2/2 | 1/1 | 1/1 | 1/1 | 1/1 | 100 % | co-dinh | giảm (-0.86 pp/năm) |
| Hình học phẳng Oxy (`oxy-hinh-phang`) | 1/1 | 1/1 | 1/1 | 1/1 | 1/1 | 1/1 | 100 % | co-dinh | ổn định (+0.00 pp/năm) |
| Lượng giác (`luong-giac`) | 1/1 | 1/1 | 1/1 | 1/1 | 1/1 | 1/1 | 100 % | co-dinh | ổn định (+0.00 pp/năm) |
| HHKG - thể tích khối đa diện (`hinh-khong-gian-the-tich`) | 1/1 | 1/1 | 1/1 | 1/1 | 1/1 | 1/1 | 100 % | co-dinh | ổn định (+0.00 pp/năm) |
| Bất đẳng thức - cực trị đại số (`bat-dang-thuc-cuc-tri`) | 1/1 | - | 1/1 | 1/1 | 1/1 | 1/1 | 83 % | xoay-vong | tăng (+0.86 pp/năm) |
| Nguyên hàm - tích phân (`nguyen-ham-tich-phan`) | 1/1 | 1/1 | 1/1 | 1/1 | 1/1 | - | 83 % | xoay-vong | giảm (-1.43 pp/năm) |
| Số phức (`so-phuc`) | 1/1 | 1/1 | 1/1 | - | - | 1/0.5 | 67 % | xoay-vong | giảm (-1.86 pp/năm) |
| Oxyz - mặt phẳng (`oxyz-mat-phang`) | - | 1/1 | - | - | 1/1 | 1/1 | 50 % | xoay-vong | tăng (+1.43 pp/năm) |
| Tổ hợp - xác suất cổ điển (`to-hop-xac-suat`) | - | - | - | 1/1 | 1/1 | 1/0.5 | 50 % | xoay-vong | tăng (+1.86 pp/năm) |
| Oxyz - mặt cầu (`oxyz-mat-cau`) | 1/1 | - | - | 1/1 | - | - | 33 % | xoay-vong | giảm (-1.14 pp/năm) |
| Ứng dụng đạo hàm (`ham-so-ung-dung-dao-ham`) | - | - | - | 1/1 | 1/1 | - | 33 % | xoay-vong | tăng (+1.14 pp/năm) |
| Oxyz - tổng hợp (`oxyz-tong-hop`) | - | - | 1/1 | - | - | - | 17 % | hiem | ổn định (-0.29 pp/năm) |
| Ứng dụng tích phân (`ung-dung-tich-phan`) | - | - | - | - | - | 1/1 | 17 % | hiem | tăng (+1.43 pp/năm) |

Phân bố mức độ theo năm (số câu; mức độ là suy luận):

| Năm | NB | TH | VD | VDC |
|---|---|---|---|---|
| 2009 | 2 | 3 | 4 | 1 |
| 2010 | 2 | 2 | 5 | 1 |
| 2011 | 1 | 3 | 5 | 1 |
| 2012 | 1 | 4 | 4 | 1 |
| 2013 | 1 | 5 | 3 | 1 |
| 2014 | 2 | 4 | 3 | 2 |

## ĐH khối B tự luận - các năm 2009-2014 (6 đề)

| Chủ đề | 2009 | 2010 | 2011 | 2012 | 2013 | 2014 | % năm | Phân loại | Xu hướng |
|---|---|---|---|---|---|---|---|---|---|
| Hàm số và đồ thị (`ham-so-do-thi`) | 2/2 | 2/2 | 1/1 | 1/1 | 1/1 | 1/1 | 100 % | co-dinh | giảm (-2.29 pp/năm) |
| Hình học phẳng Oxy (`oxy-hinh-phang`) | 1/1 | 1/1 | 1/1 | 1/1 | 1/1 | 1/1 | 100 % | co-dinh | ổn định (+0.00 pp/năm) |
| Phương trình, hệ, bất phương trình đại số (`phuong-trinh-he-bpt`) | 1/1 | 1/1 | 1/1 | 1/1 | 1/1 | 1/1 | 100 % | co-dinh | ổn định (+0.00 pp/năm) |
| Bất đẳng thức - cực trị đại số (`bat-dang-thuc-cuc-tri`) | 1/1 | 1/1 | 1/1 | 1/1 | 1/1 | 1/1 | 100 % | co-dinh | ổn định (+0.00 pp/năm) |
| Lượng giác (`luong-giac`) | 1/1 | 1/1 | 1/1 | 1/1 | 1/1 | 1/1 | 100 % | co-dinh | ổn định (+0.00 pp/năm) |
| HHKG - thể tích khối đa diện (`hinh-khong-gian-the-tich`) | 1/1 | 1/1 | 1/1 | 1/1 | 1/1 | 1/1 | 100 % | co-dinh | ổn định (+0.00 pp/năm) |
| Nguyên hàm - tích phân (`nguyen-ham-tich-phan`) | 1/1 | 1/1 | 1/1 | 1/1 | 1/1 | 1/1 | 100 % | co-dinh | ổn định (+0.00 pp/năm) |
| Ứng dụng đạo hàm (`ham-so-ung-dung-dao-ham`) | - | - | 1/1 | 1/1 | 1/1 | 1/1 | 67 % | xoay-vong | tăng (+2.29 pp/năm) |
| Số phức (`so-phuc`) | 1/1 | 1/1 | 1/1 | - | - | 1/0.5 | 67 % | xoay-vong | giảm (-1.86 pp/năm) |
| Oxyz - mặt phẳng (`oxyz-mat-phang`) | 1/1 | 1/1 | - | - | - | 1/1 | 50 % | xoay-vong | giảm (-0.86 pp/năm) |
| Tổ hợp - xác suất cổ điển (`to-hop-xac-suat`) | - | - | - | 1/1 | 1/1 | 1/0.5 | 50 % | xoay-vong | tăng (+1.86 pp/năm) |
| Oxyz - tổng hợp (`oxyz-tong-hop`) | - | - | 1/1 | - | - | - | 17 % | hiem | ổn định (-0.29 pp/năm) |
| Oxyz - mặt cầu (`oxyz-mat-cau`) | - | - | - | 1/1 | - | - | 17 % | hiem | ổn định (+0.29 pp/năm) |
| Oxyz - đường thẳng (`oxyz-duong-thang`) | - | - | - | - | 1/1 | - | 17 % | hiem | tăng (+0.86 pp/năm) |

Phân bố mức độ theo năm (số câu; mức độ là suy luận):

| Năm | NB | TH | VD | VDC |
|---|---|---|---|---|
| 2009 | 1 | 3 | 5 | 1 |
| 2010 | 1 | 3 | 5 | 1 |
| 2011 | 1 | 4 | 4 | 1 |
| 2012 | 1 | 4 | 4 | 1 |
| 2013 | 1 | 4 | 4 | 1 |
| 2014 | 2 | 4 | 3 | 2 |

## Đề minh họa 2025 (cấu trúc mới, 1 đề - không phân loại cố định/xoay vòng)

| Chủ đề | số câu / điểm |
|---|---|
| Dãy số - cấp số | 1/0.25 |
| Hàm số và đồ thị | 1/0.25 |
| Hàm số mũ - logarit | 2/0.5 |
| Ứng dụng đạo hàm | 3/1.75 |
| HHKG - quan hệ song song, vuông góc | 3/1 |
| Nguyên hàm - tích phân | 1/0.25 |
| Oxyz - đường thẳng | 1/0.25 |
| Oxyz - mặt cầu | 2/1.5 |
| Oxyz - mặt phẳng | 1/0.25 |
| Thống kê mẫu ghép nhóm | 1/0.25 |
| Tổ hợp - xác suất cổ điển | 1/0.5 |
| Ứng dụng tích phân | 3/1.75 |
| Xác suất có điều kiện | 2/1.5 |

## ĐH (khối A ∪ B) - chủ đề xuất hiện trong ít nhất một khối mỗi năm, 2009-2014

| Chủ đề | 2009 | 2010 | 2011 | 2012 | 2013 | 2014 | % năm | Phân loại |
|---|---|---|---|---|---|---|---|---|
| Bất đẳng thức - cực trị đại số | 2 | 1 | 2 | 2 | 2 | 2 | 100 % | co-dinh |
| Hàm số và đồ thị | 4 | 4 | 3 | 2 | 2 | 3 | 100 % | co-dinh |
| Ứng dụng đạo hàm | - | - | 1 | 2 | 2 | 1 | 67 % | xoay-vong |
| HHKG - thể tích khối đa diện | 2 | 2 | 2 | 2 | 2 | 2 | 100 % | co-dinh |
| Lượng giác | 2 | 2 | 2 | 2 | 2 | 2 | 100 % | co-dinh |
| Nguyên hàm - tích phân | 2 | 2 | 2 | 2 | 2 | 1 | 100 % | co-dinh |
| Hình học phẳng Oxy | 2 | 2 | 2 | 2 | 2 | 2 | 100 % | co-dinh |
| Oxyz - đường thẳng | - | - | - | - | 1 | - | 17 % | hiem |
| Oxyz - mặt cầu | 1 | - | - | 2 | - | - | 33 % | xoay-vong |
| Oxyz - mặt phẳng | 1 | 2 | - | - | 1 | 2 | 67 % | xoay-vong |
| Oxyz - tổng hợp | - | - | 2 | - | - | - | 17 % | hiem |
| Phương trình, hệ, bất phương trình đại số | 2 | 3 | 2 | 2 | 2 | 2 | 100 % | co-dinh |
| Số phức | 2 | 2 | 2 | - | - | 2 | 67 % | xoay-vong |
| Tổ hợp - xác suất cổ điển | - | - | - | 2 | 2 | 2 | 50 % | xoay-vong |
| Ứng dụng tích phân | - | - | - | - | - | 1 | 17 % | hiem |

## Hình học không gian (thể tích + quan hệ + khối tròn xoay) - dạng nào hay ra

Đếm theo `dang` (dạng ghép bằng '+' được tính cho từng dạng thành phần). Cột = số câu có dạng đó / tổng số đề của nhóm.

### TN THPT cấu trúc cũ (50 câu TN4)

Tổng câu hình học không gian: 66 trong 8 đề (trung bình 8.2 câu/đề).

| Dạng | Số câu | Số năm xuất hiện | Mô tả |
|---|---|---|---|
| `qh.goc` | 10 | 7/8 | Góc giữa đường thẳng với mặt phẳng, hai đường thẳng |
| `th.chop` | 7 | 7/8 | Thể tích khối chóp |
| `tx.non` | 7 | 6/8 | Hình nón: thể tích, diện tích xung quanh |
| `tx.tru` | 6 | 6/8 | Hình trụ: thể tích, diện tích xung quanh |
| `qh.khoang-cach` | 5 | 4/8 | Khoảng cách (điểm - mặt phẳng, hai đường chéo nhau) |
| `tx.cau` | 4 | 4/8 | Mặt cầu/khối cầu: diện tích, thể tích, bán kính |
| `th.lang-tru-vdc` | 4 | 4/8 | Thể tích lăng trụ/khối đa diện (VDC) |
| `th.lang-tru` | 4 | 3/8 | Thể tích khối lăng trụ |
| `tx.thiet-dien` | 3 | 3/8 | Thiết diện của nón/trụ |
| `th.chop-vdc` | 3 | 3/8 | Thể tích khối chóp (VDC) |
| `th.ty-so` | 2 | 2/8 | Tỉ số thể tích, chia khối |
| `th.hop` | 2 | 2/8 | Thể tích hình hộp/lập phương |
| `tx.noi-tiep-cau` | 2 | 2/8 | Nón/trụ nội tiếp cầu, mặt cầu ngoại tiếp |
| `th.khai-niem` | 1 | 1/8 | Khái niệm khối đa diện, mặt đối xứng |
| `th.chop-goc` | 1 | 1/8 | Thể tích chóp khi cho góc/quan hệ vuông góc |
| `th.lang-tru-thuc-te` | 1 | 1/8 | Thể tích lăng trụ, bài toán thực tế |
| `qh.goc-mp` | 1 | 1/8 | Góc giữa hai mặt phẳng |
| `tx.thuc-te` | 1 | 1/8 | Khối tròn xoay trong bài toán thực tế |
| `th.hop-vdc` | 1 | 1/8 | Thể tích hình hộp (VDC) |
| `th.lang-tru-goc` | 1 | 1/8 | Thể tích lăng trụ khi cho góc/hình chiếu |

### TN THPT cấu trúc mới (22 câu: 12 TN4 + 4 đúng/sai + 6 trả lời ngắn)

Tổng câu hình học không gian: 6 trong 2 đề (trung bình 3.0 câu/đề).

| Dạng | Số câu | Số năm xuất hiện | Mô tả |
|---|---|---|---|
| `qh.vecto` | 2 | 2/2 | Vectơ trong không gian |
| `qh.khoang-cach` | 2 | 2/2 | Khoảng cách (điểm - mặt phẳng, hai đường chéo nhau) |
| `qh.song-song` | 1 | 1/2 | Quan hệ song song |
| `th.chop` | 1 | 1/2 | Thể tích khối chóp |

### ĐH khối A/A1 tự luận

Tổng câu hình học không gian: 6 trong 6 đề (trung bình 1.0 câu/đề).

| Dạng | Số câu | Số năm xuất hiện | Mô tả |
|---|---|---|---|
| `qh.khoang-cach` | 5 | 5/6 | Khoảng cách (điểm - mặt phẳng, hai đường chéo nhau) |
| `th.chop` | 4 | 4/6 | Thể tích khối chóp |
| `th.chop-goc` | 2 | 2/6 | Thể tích chóp khi cho góc/quan hệ vuông góc |
| `qh.goc` | 1 | 1/6 | Góc giữa đường thẳng với mặt phẳng, hai đường thẳng |

### ĐH khối B tự luận

Tổng câu hình học không gian: 6 trong 6 đề (trung bình 1.0 câu/đề).

| Dạng | Số câu | Số năm xuất hiện | Mô tả |
|---|---|---|---|
| `th.lang-tru-goc` | 4 | 4/6 | Thể tích lăng trụ khi cho góc/hình chiếu |
| `qh.khoang-cach` | 3 | 3/6 | Khoảng cách (điểm - mặt phẳng, hai đường chéo nhau) |
| `th.chop` | 2 | 2/6 | Thể tích khối chóp |
| `tx.noi-tiep-cau` | 1 | 1/6 | Nón/trụ nội tiếp cầu, mặt cầu ngoại tiếp |
| `qh.vuong-goc` | 1 | 1/6 | Quan hệ vuông góc, chứng minh vuông góc |

## Tóm tắt phân loại (cố định / xoay vòng / hiếm)

- **TN THPT cấu trúc cũ (50 câu TN4)**
  - Cố định: Hàm số mũ - logarit, Ứng dụng đạo hàm, Số phức, Nguyên hàm - tích phân, Hàm số và đồ thị, HHKG - thể tích khối đa diện, Khối tròn xoay, Oxyz - mặt phẳng, Oxyz - đường thẳng, Oxyz - mặt cầu
  - Xoay vòng: Tổ hợp - xác suất cổ điển, HHKG - quan hệ song song, vuông góc, Ứng dụng tích phân, Oxyz - tọa độ điểm, vectơ, Dãy số - cấp số
  - Hiếm: Oxyz - tổng hợp, Giới hạn - liên tục
- **TN THPT cấu trúc mới (22 câu: 12 TN4 + 4 đúng/sai + 6 trả lời ngắn)**
  - Cố định: Ứng dụng tích phân, Ứng dụng đạo hàm, Xác suất có điều kiện, Tổ hợp - xác suất cổ điển, HHKG - quan hệ song song, vuông góc, Hàm số mũ - logarit, Phương trình, hệ, bất phương trình đại số, Dãy số - cấp số, Nguyên hàm - tích phân, Thống kê mẫu ghép nhóm, Hàm số và đồ thị
  - Xoay vòng: Oxyz - mặt cầu, Oxyz - mặt phẳng, Oxyz - tọa độ điểm, vectơ, Oxyz - đường thẳng, HHKG - thể tích khối đa diện, Lượng giác, Đạo hàm cơ bản
  - Hiếm: (không có)
- **ĐH khối A/A1 tự luận**
  - Cố định: Hàm số và đồ thị, Phương trình, hệ, bất phương trình đại số, Hình học phẳng Oxy, Lượng giác, HHKG - thể tích khối đa diện
  - Xoay vòng: Bất đẳng thức - cực trị đại số, Nguyên hàm - tích phân, Số phức, Oxyz - mặt phẳng, Tổ hợp - xác suất cổ điển, Oxyz - mặt cầu, Ứng dụng đạo hàm
  - Hiếm: Oxyz - tổng hợp, Ứng dụng tích phân
- **ĐH khối B tự luận**
  - Cố định: Hàm số và đồ thị, Hình học phẳng Oxy, Phương trình, hệ, bất phương trình đại số, Bất đẳng thức - cực trị đại số, Lượng giác, HHKG - thể tích khối đa diện, Nguyên hàm - tích phân
  - Xoay vòng: Ứng dụng đạo hàm, Số phức, Oxyz - mặt phẳng, Tổ hợp - xác suất cổ điển
  - Hiếm: Oxyz - tổng hợp, Oxyz - mặt cầu, Oxyz - đường thẳng
