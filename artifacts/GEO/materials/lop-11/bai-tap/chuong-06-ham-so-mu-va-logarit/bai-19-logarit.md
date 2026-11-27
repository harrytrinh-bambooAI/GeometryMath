# Bài tập – Bài 19. Lôgarit

Phiên bản: v1.2 · 27/11/2026

> Tài liệu tổng hợp/biên soạn lại cho mục đích học tập (không sao chép nguyên văn SGK/SBT). Bộ sách xương sống: Toán 11 Kết nối tri thức với cuộc sống.

*Chương 6. Hàm số mũ và hàm số lôgarit* · Lý thuyết: [ly-thuyet](../../ly-thuyet/chuong-06-ham-so-mu-va-logarit/bai-19-logarit.md)

### Nhận biết
**1.** $\log_2 32$? *Giải.* $5$.

**2.** *[Trắc nghiệm]* Giá trị của $\log_2\dfrac18$ là: A. $-3$; B. $3$; C. $\dfrac13$; D. $-\dfrac13$.
*Giải.* $\dfrac18=2^{-3}$ nên $\log_22^{-3}=-3$. Chọn **A**.

### Thông hiểu
**3.** Cho $\log_23=a$. Tính $\log_{12}18$ theo $a$.
*Giải.*
- Bước 1: đổi cơ số về $2$: $\log_{12}18=\dfrac{\log_218}{\log_212}$.
- Bước 2: $\log_218=\log_2(2\cdot3^2)=1+2a$; $\log_212=\log_2(2^2\cdot3)=2+a$.
- Bước 3: $\log_{12}18=\dfrac{1+2a}{2+a}$.

**4.** *[Đúng/sai]* a) $\log_212=2+\log_23$; b) $\log_35\cdot\log_59=2$; c) $\lg100=3$; d) $\log_ab^2=2\log_ab$ với $a>0,a\ne1,b>0$.
*Giải.*
- a) $\log_2(4\cdot3)=2+\log_23$: **Đúng**.
- b) Đổi cơ số: $\log_35\cdot\log_59=\log_39=2$: **Đúng**.
- c) $\lg100=2$: **Sai**.
- d) Công thức lũy thừa của logarit: **Đúng**.
- Đáp án: Đ – Đ – S – Đ.

### Vận dụng
**5.** Tính $A=\log_26+\log_2\dfrac13$.
*Giải.*
- Bước 1: $\log_2x+\log_2y=\log_2(xy)$ với $x,y>0$.
- Bước 2: $A=\log_2\left(6\cdot\dfrac13\right)=\log_22=1$.

**6.** *[Trả lời ngắn]* Tính $B=2\log_36-\log_34$.
*Giải.*
- $2\log_36=\log_3 36$.
- $B=\log_336-\log_34=\log_3\dfrac{36}4=\log_39=2$.

### Vận dụng cao
**7.** Độ pH được tính bởi $\mathrm{pH}=-\lg[H^+]$. Tìm pH khi $[H^+]=10^{-7}$ và khi $[H^+]=4\cdot10^{-5}$.
*Giải.*
- Bước 1: $[H^+]=10^{-7}$: $\mathrm{pH}=-\lg10^{-7}=7$.
- Bước 2: $[H^+]=4\cdot10^{-5}$: $\lg(4\cdot10^{-5})=\lg4-5$.
- Bước 3: $\mathrm{pH}=5-\lg4\approx5-0{,}602=4{,}40$.

## Nguồn

| Nội dung trong bài | Nguồn | Link | Ghi chú |
|---|---|---|---|
| Toàn bộ đề và lời giải | Tác giả tài liệu | - | tự soạn (cùng dạng với SGK/SBT, không trích nguyên văn) |
| Đối chiếu dạng bài | SBT Toán 11 KNTT trên loigiaihay | https://loigiaihay.com/sbt-toan-11-ket-noi-tri-thuc-c1567.html | chỉ tham khảo dạng, không chép |
| Đối chiếu dạng bài, mức độ | Giải bài tập SGK Toán 11 KNTT – loigiaihay (Bài 19) | https://loigiaihay.com/bai-19-logarit-toan-11-ket-noi-tri-thuc-e31936.html | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép đề/lời giải |
| Đối chiếu dạng bài, mức độ | Giải SGK Toán 11 KNTT – VietJack (Bài 19) | https://vietjack.com/toan-11-kn/bai-19-logarit.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép đề/lời giải |
| Đối chiếu dạng đề kiểm tra (trắc nghiệm, đúng/sai, trả lời ngắn) | Đề thi giữa kì 1 Toán 11 KNTT (đề số 9) – loigiaihay | https://loigiaihay.com/de-thi-giua-ki-1-toan-11-ket-noi-tri-thuc-de-so-9-a188921.html | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng/mức độ, không chép đề |
| Đối chiếu cấu trúc đề 2025 | Đề thi giữa kì 1 Toán 11 cấu trúc mới KNTT – VietJack | https://vietjack.com/de-kiem-tra-lop-11/de-thi-giua-ki-1-toan-lop-11-cau-truc-moi-ket-noi.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng, không chép đề |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.1 | 27/11/2026 | Bản trước: 4 bài, lời giải ngắn | agent runner-materials-l11 |
| v1.2 | 27/11/2026 | Thêm 3 bài (4→7, có trắc nghiệm/đúng-sai/trả lời ngắn), viết lại 3 lời giải từng bước, cập nhật nguồn | agent runner-mat-300 |
