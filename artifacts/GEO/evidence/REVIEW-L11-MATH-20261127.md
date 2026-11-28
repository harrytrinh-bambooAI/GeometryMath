Phiên bản: v1.0 · 2026-11-27 · review-l11-math

# Soát chéo toán Toán 11: 305 bài tập

Phạm vi: `artifacts/GEO/materials/lop-11/bai-tap/` (9 chương). Đã soát 305/305 bài. Phương pháp: đọc từng tệp, tự tính lại đáp số (tay và python3 -I trong `~/.claude/jobs/74cc88b7/tmp/chk.py`), kiểm trắc nghiệm đúng một phương án, từng ý Đúng/Sai, dữ kiện đề, mức độ.

## Bảng tổng theo chương
| Chương | Số bài soát | Major | Minor |
|---|---|---|---|
| 1. Lượng giác | 40 | 1 | 0 |
| 2. Dãy số, CSC, CSN | 31 | 0 | 0 |
| 3. Mẫu số liệu ghép nhóm | 21 | 1 | 0 |
| 4. Quan hệ song song | 42 | 0 | 1 |
| 5. Giới hạn, liên tục | 30 | 0 | 0 |
| 6. Hàm số mũ, logarit | 36 | 0 | 0 |
| 7. Quan hệ vuông góc | 50 | 2 | 1 |
| 8. Xác suất | 27 | 0 | 0 |
| 9. Đạo hàm | 28 | 0 | 0 |
| **Tổng** | **305** | **4** | **2** |

Nhận xét chung: không phát hiện sai đáp số hay sai lời giải tính toán; mọi lỗi major là đề thiếu dữ kiện / mâu thuẫn do tham chiếu "bài trên/bài 3" bị sai khi đánh số lại. Mọi câu trắc nghiệm có đúng một phương án đúng; mọi ý Đúng/Sai khớp.

## Bảng lỗi
| tệp | bài số | mức | vấn đề | đề xuất sửa |
|---|---|---|---|---|
| chuong-03-mau-so-lieu-ghep-nhom/on-tap-chuong.md | 2 | major | "Tìm mốt." không có bảng số liệu. Lời giải ngầm dùng m_j-m_{j-1}=15 và m_j-m_{j+1}=5 cho nhóm [10;20), không truy được từ đề (có lẽ bảng bị mất khi đánh số lại) | Thêm bảng khớp công thức, ví dụ [0;10): 5; [10;20): 20; [20;30): 15 (cho 10+15/(15+5)·10=17,5), hoặc gộp vào bài 4 |
| chuong-01-ham-so-luong-giac-va-phuong-trinh-luong-giac/on-tap-chuong.md | 7 | major | Đề ghi "(một nghiệm trong [0;365])" nhưng lời giải tìm hai nghiệm (t≈110 và t≈232), tự mâu thuẫn. Kiểm: d(110,4)=12,9995; t=232,08 | Bỏ cụm "(một nghiệm...)" và hỏi "Có những ngày nào d=13?", hoặc hỏi ngày sớm nhất (110) |
| chuong-07-quan-he-vuong-goc/bai-25-hai-mat-phang-vuong-goc.md | 7 | major | "Với giả thiết bài 3" nhưng bài 3 chỉ có "chóp S.ABCD, SA vuông góc đáy" (không nói đáy hình vuông); chứng minh dùng BD⊥AC cần đáy hình vuông/hình thoi. Tham chiếu "xem bài 4" ở ý c bài 4 cũng mơ hồ | Sửa thành "Chóp S.ABCD đáy hình vuông, SA⊥(ABCD)"; ý c bài 4 ghi "xem bài 5 bài 23 / chứng minh BD⊥(SAC)" |
| chuong-07-quan-he-vuong-goc/on-tap-chuong.md | 2 | major | "Tính thể tích khối chóp ở trên" nhưng bài 1 là khối lập phương; không có khối chóp nào. Lời giải (1/3·2a²·a) dùng dữ kiện không có trong đề (đáy 2a², cao a) | Thêm đề: "Chóp S.ABCD đáy hình chữ nhật AB=a, AD=2a, SA⊥đáy, SA=a. Tính V" (khớp 2a³/3), đặt lên bài 3, hoặc viết lại bài 2 độc lập |
| chuong-04-quan-he-song-song/bai-12-duong-thang-va-mat-phang-song-song.md | 3 (ý b) | minor | Lời giải ý b "song song với hình chiếu của a" là diễn đạt lạ, không có trong chương trình; kết luận đúng | Viết: trong (P) lấy đường thẳng bất kì song song với giao tuyến b của (P) với một mặt phẳng chứa a thì song song a |
| chuong-07-quan-he-vuong-goc/bai-25-hai-mat-phang-vuong-goc.md | 4 (ý c) | minor | Lời giải ý c "xem bài 4" không rõ bài nào (ý là bài 23, câu 4/5) | Ghi "xem bài 23, câu 5" hoặc chứng minh tại chỗ |

## Ghi chú mức độ và lưu ý (không tính lỗi)
- Chương 9 bài 32 dùng đạo hàm của ln, e^x, e^{2x}; nên đối chiếu với lý thuyết bài 32 để chắc chắn các công thức đã được trình bày (ngoài phạm vi soát này).
- Các bài "Vận dụng cao" ở chương 2, 5, 6 phần lớn thực chất là mức Vận dụng; không đủ độ lệch để tính minor.

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.0 | 2026-11-27 | Soát 305 bài, 4 major, 2 minor | review-l11-math |
