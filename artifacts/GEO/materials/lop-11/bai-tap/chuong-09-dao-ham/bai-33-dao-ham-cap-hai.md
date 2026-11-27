# Bài tập – Bài 33. Đạo hàm cấp hai

Phiên bản: v1.2 · 27/11/2026

> Tài liệu tổng hợp/biên soạn lại cho mục đích học tập (không sao chép nguyên văn SGK/SBT). Bộ sách xương sống: Toán 11 Kết nối tri thức với cuộc sống.

*Chương 9. Đạo hàm* · Lý thuyết: [ly-thuyet](../../ly-thuyet/chuong-09-dao-ham/bai-33-dao-ham-cap-hai.md)

### Nhận biết
**1.** $y=x^4-3x$, $y''$? *Giải.* $y'=4x^3-3$; $y''=12x^2$.

**2.** *[Trắc nghiệm]* Đạo hàm cấp hai của $y=x^3$ là: A. $3x^2$; B. $6x$; C. $6$; D. $x^3$.
*Giải.* $y'=3x^2$, $y''=6x$. Chọn **B**.

### Thông hiểu
**3.** $y=\sin2x$. Tính $y''$ và kiểm tra $y''+4y=0$. *Giải.* $y'=2\cos2x$, $y''=-4\sin2x$; $y''+4y=0$.

**4.** *[Đúng/sai]* Cho $y=e^{2x}$. a) $y'=2e^{2x}$; b) $y''=4e^{2x}$; c) $y''-2y'=0$; d) $y''+y'=0$.
*Giải.*
- a) **Đúng**. b) $y''=2\cdot2e^{2x}=4e^{2x}$: **Đúng**.
- c) $4e^{2x}-2\cdot2e^{2x}=0$: **Đúng**.
- d) $4e^{2x}+2e^{2x}=6e^{2x}\ne0$: **Sai**.
- Đáp án: Đ – Đ – Đ – S.

### Vận dụng
**5.** Chuyển động có $s(t)=t^3-3t^2+2t$. Tính gia tốc tại $t=2$.
*Giải.*
- Bước 1: $v=s'=3t^2-6t+2$.
- Bước 2: $a=s''=6t-6$.
- Bước 3: $a(2)=6\cdot2-6=6$ m/s$^2$.

**6.** *[Trả lời ngắn]* Chuyển động có $s(t)=2t^3-9t^2+12t$ ($t$ giây). Tại thời điểm nào gia tốc bằng $0$?
*Giải.*
- $v=s'=6t^2-18t+12$; $a=s''=12t-18$.
- $a=0\Rightarrow t=\dfrac{18}{12}=1{,}5$ (giây).

### Vận dụng cao
**7.** Chứng minh hàm số $y=xe^x$ thỏa mãn $y''-2y'+y=0$.
*Giải.*
- Bước 1: $y'=e^x+xe^x=(1+x)e^x$.
- Bước 2: $y''=e^x+(1+x)e^x=(2+x)e^x$.
- Bước 3: $y''-2y'+y=\left[(2+x)-2(1+x)+x\right]e^x=0\cdot e^x=0$. ∎

## Nguồn

| Nội dung trong bài | Nguồn | Link | Ghi chú |
|---|---|---|---|
| Toàn bộ đề và lời giải | Tác giả tài liệu | - | tự soạn (cùng dạng với SGK/SBT, không trích nguyên văn) |
| Đối chiếu dạng bài | SBT Toán 11 KNTT trên loigiaihay | https://loigiaihay.com/sbt-toan-11-ket-noi-tri-thuc-c1567.html | chỉ tham khảo dạng, không chép |
| Đối chiếu dạng bài, mức độ | Giải bài tập SGK Toán 11 KNTT – loigiaihay (Bài 33) | https://loigiaihay.com/bai-33-dao-ham-cap-hai-toan-11-ket-noi-tri-thuc-e31974.html | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép đề/lời giải |
| Đối chiếu dạng bài, mức độ | Giải SGK Toán 11 KNTT – VietJack (Bài 33) | https://vietjack.com/toan-11-kn/bai-33-dao-ham-cap-hai.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép đề/lời giải |
| Đối chiếu dạng đề kiểm tra (trắc nghiệm, đúng/sai, trả lời ngắn) | Đề thi giữa kì 1 Toán 11 KNTT (đề số 9) – loigiaihay | https://loigiaihay.com/de-thi-giua-ki-1-toan-11-ket-noi-tri-thuc-de-so-9-a188921.html | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng/mức độ, không chép đề |
| Đối chiếu cấu trúc đề 2025 | Đề thi giữa kì 1 Toán 11 cấu trúc mới KNTT – VietJack | https://vietjack.com/de-kiem-tra-lop-11/de-thi-giua-ki-1-toan-lop-11-cau-truc-moi-ket-noi.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng, không chép đề |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.1 | 27/11/2026 | Bản trước: 4 bài, lời giải ngắn | agent runner-materials-l11 |
| v1.2 | 27/11/2026 | Thêm 3 bài (4→7, có trắc nghiệm/đúng-sai/trả lời ngắn), viết lại 2 lời giải từng bước, cập nhật nguồn | agent runner-mat-300 |
