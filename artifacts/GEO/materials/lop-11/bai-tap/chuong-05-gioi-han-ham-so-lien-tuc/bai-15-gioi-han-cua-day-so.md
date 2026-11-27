# Bài tập – Bài 15. Giới hạn của dãy số

Phiên bản: v1.2 · 27/11/2026

> Tài liệu tổng hợp/biên soạn lại cho mục đích học tập (không sao chép nguyên văn SGK/SBT). Bộ sách xương sống: Toán 11 Kết nối tri thức với cuộc sống.

*Chương 5. Giới hạn. Hàm số liên tục* · Lý thuyết: [ly-thuyet](../../ly-thuyet/chuong-05-gioi-han-ham-so-lien-tuc/bai-15-gioi-han-cua-day-so.md)

### Nhận biết
**1.** $\lim\dfrac{2n+1}{n}$? *Giải.* $\lim(2+\tfrac1n)=2$.

**2.** *[Trắc nghiệm]* $\lim\left(-\dfrac12\right)^n$ bằng: A. $0$; B. $-\dfrac12$; C. $1$; D. $-\infty$.
*Giải.* $\left|-\dfrac12\right|<1$ nên $\lim q^n=0$. Chọn **A**.

### Thông hiểu
**3.** $\lim\dfrac{n^2-3n}{4n^2+5}$. *Giải.* $\dfrac14$.

**4.** $\lim\dfrac{3^n+2^n}{3^{n+1}}$. *Giải.* Chia cho $3^n$: $\dfrac{1+(2/3)^n}{3}\to\dfrac13$.

### Vận dụng
**5.** Tính $\lim\left(\sqrt{n^2+n}-n\right)$.
*Giải.*
- Bước 1: nhân liên hợp: $\sqrt{n^2+n}-n=\dfrac{n}{\sqrt{n^2+n}+n}$.
- Bước 2: chia tử và mẫu cho $n$: $\dfrac{1}{\sqrt{1+1/n}+1}$.
- Bước 3: giới hạn $=\dfrac1{1+1}=\dfrac12$.

**6.** Tính $S=1+\tfrac12+\tfrac14+\dots$ (tổng vô hạn).
*Giải.*
- Bước 1: đây là cấp số nhân lùi vô hạn với $u_1=1$, $q=\dfrac12$, $|q|<1$.
- Bước 2: $S=\dfrac{u_1}{1-q}=\dfrac1{1-\frac12}=2$.

**7.** *[Trả lời ngắn]* Tính $\lim\left(\sqrt{4n^2+n}-2n\right)$.
*Giải.*
- Nhân và chia với biểu thức liên hợp: $\sqrt{4n^2+n}-2n=\dfrac{n}{\sqrt{4n^2+n}+2n}$.
- Chia tử và mẫu cho $n$: $\dfrac{1}{\sqrt{4+1/n}+2}\to\dfrac1{2+2}=\dfrac14$.
- Đáp số: $0{,}25$.

### Vận dụng cao
**8.** Biểu diễn số thập phân vô hạn tuần hoàn $0{,}\overline{36}$ thành phân số.
*Giải.*
- Bước 1: $0{,}\overline{36}=0{,}36+0{,}0036+0{,}000036+\dots$, là tổng cấp số nhân lùi vô hạn.
- Bước 2: $u_1=0{,}36$, $q=0{,}01$, $|q|<1$.
- Bước 3: $S=\dfrac{u_1}{1-q}=\dfrac{0{,}36}{0{,}99}=\dfrac{36}{99}=\dfrac4{11}$ (kiểm tra: $4:11=0{,}3636\ldots$).

## Nguồn

| Nội dung trong bài | Nguồn | Link | Ghi chú |
|---|---|---|---|
| Toàn bộ đề và lời giải | Tác giả tài liệu | - | tự soạn (cùng dạng với SGK/SBT, không trích nguyên văn) |
| Đối chiếu dạng bài | SBT Toán 11 KNTT trên loigiaihay | https://loigiaihay.com/sbt-toan-11-ket-noi-tri-thuc-c1567.html | chỉ tham khảo dạng, không chép |
| Đối chiếu dạng bài, mức độ | Giải bài tập SGK Toán 11 KNTT – loigiaihay (Bài 15) | https://loigiaihay.com/bai-15-gioi-han-cua-day-so-toan-11-ket-noi-tri-thuc-e31542.html | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép đề/lời giải |
| Đối chiếu dạng bài, mức độ | Giải SGK Toán 11 KNTT – VietJack (Bài 15) | https://vietjack.com/toan-11-kn/bai-15-gioi-han-cua-day-so.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép đề/lời giải |
| Đối chiếu dạng đề kiểm tra (trắc nghiệm, đúng/sai, trả lời ngắn) | Đề thi giữa kì 1 Toán 11 KNTT (đề số 9) – loigiaihay | https://loigiaihay.com/de-thi-giua-ki-1-toan-11-ket-noi-tri-thuc-de-so-9-a188921.html | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng/mức độ, không chép đề |
| Đối chiếu cấu trúc đề 2025 | Đề thi giữa kì 1 Toán 11 cấu trúc mới KNTT – VietJack | https://vietjack.com/de-kiem-tra-lop-11/de-thi-giua-ki-1-toan-lop-11-cau-truc-moi-ket-noi.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng, không chép đề |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.1 | 27/11/2026 | Bản trước: 6 bài, lời giải ngắn | agent runner-materials-l11 |
| v1.2 | 27/11/2026 | Thêm 2 bài (6→8, có trắc nghiệm/đúng-sai/trả lời ngắn), viết lại 3 lời giải từng bước, cập nhật nguồn | agent runner-mat-300 |
