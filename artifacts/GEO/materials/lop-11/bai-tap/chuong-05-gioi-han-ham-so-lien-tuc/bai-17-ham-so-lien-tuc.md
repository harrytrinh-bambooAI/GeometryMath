# Bài tập – Bài 17. Hàm số liên tục

Phiên bản: v1.2 · 27/11/2026

> Tài liệu tổng hợp/biên soạn lại cho mục đích học tập (không sao chép nguyên văn SGK/SBT). Bộ sách xương sống: Toán 11 Kết nối tri thức với cuộc sống.

*Chương 5. Giới hạn. Hàm số liên tục* · Lý thuyết: [ly-thuyet](../../ly-thuyet/chuong-05-gioi-han-ham-so-lien-tuc/bai-17-ham-so-lien-tuc.md)

### Nhận biết
**1.** $f(x)=\dfrac{1}{x-2}$ liên tục trên tập nào? *Giải.* $\mathbb R\setminus\{2\}$.

**2.** *[Trắc nghiệm]* Hàm số nào liên tục trên $\mathbb R$? A. $y=\dfrac1x$; B. $y=\tan x$; C. $y=x^3-x+1$; D. $y=\sqrt x$.
*Giải.* Đa thức liên tục trên $\mathbb R$; A không xác định tại $0$, B không xác định tại $\dfrac\pi2+k\pi$, D chỉ xác định khi $x\ge0$. Chọn **C**.

### Thông hiểu
**3.** Xét tính liên tục tại $x=1$ của $f(x)=\begin{cases}\dfrac{x^2-1}{x-1}&x\ne1\\ 2&x=1\end{cases}$. *Giải.* $\lim=\lim(x+1)=2=f(1)$: liên tục.

**4.** Tìm $m$ để $f(x)=\begin{cases}x+1&x<2\\ mx-1&x\ge2\end{cases}$ liên tục tại 2. *Giải.* $3=2m-1\Rightarrow m=2$.

### Vận dụng
**5.** Chứng minh phương trình $x^3-3x+1=0$ có ba nghiệm phân biệt.
*Giải.*
- Bước 1: $f(x)=x^3-3x+1$ liên tục trên $\mathbb R$.
- Bước 2: $f(-2)=-1$, $f(0)=1$, $f(1)=-1$, $f(2)=3$.
- Bước 3: $f(-2)f(0)<0$, $f(0)f(1)<0$, $f(1)f(2)<0$, nên có nghiệm trong mỗi khoảng $(-2;0)$, $(0;1)$, $(1;2)$; ba khoảng rời nhau nên ba nghiệm phân biệt.
- Bước 4: đa thức bậc ba có tối đa ba nghiệm nên đúng ba nghiệm.

**6.** *[Trả lời ngắn]* Tìm $m$ để $f(x)=\begin{cases}x^2+m&x<1\\ 2x+3&x\ge1\end{cases}$ liên tục tại $x=1$.
*Giải.*
- $\lim_{x\to1^-}f(x)=1+m$; $\lim_{x\to1^+}f(x)=f(1)=5$.
- Liên tục $\Leftrightarrow1+m=5\Rightarrow m=4$.

### Vận dụng cao
**7.** Chứng minh phương trình $\cos x=x$ có nghiệm trong khoảng $\left(0;\dfrac\pi2\right)$.
*Giải.*
- Bước 1: đặt $g(x)=\cos x-x$, liên tục trên $\left[0;\dfrac\pi2\right]$.
- Bước 2: $g(0)=1>0$; $g\left(\dfrac\pi2\right)=0-\dfrac\pi2<0$.
- Bước 3: $g(0)g\left(\dfrac\pi2\right)<0$ nên tồn tại $c\in\left(0;\dfrac\pi2\right)$ để $g(c)=0$, tức $\cos c=c$. ∎

## Nguồn

| Nội dung trong bài | Nguồn | Link | Ghi chú |
|---|---|---|---|
| Toàn bộ đề và lời giải | Tác giả tài liệu | - | tự soạn (cùng dạng với SGK/SBT, không trích nguyên văn) |
| Đối chiếu dạng bài | SBT Toán 11 KNTT trên loigiaihay | https://loigiaihay.com/sbt-toan-11-ket-noi-tri-thuc-c1567.html | chỉ tham khảo dạng, không chép |
| Đối chiếu dạng bài, mức độ | Giải bài tập SGK Toán 11 KNTT – loigiaihay (Bài 17) | https://loigiaihay.com/bai-17-ham-so-lien-tuc-toan-11-ket-noi-tri-thuc-e31544.html | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép đề/lời giải |
| Đối chiếu dạng bài, mức độ | Giải SGK Toán 11 KNTT – VietJack (Bài 17) | https://vietjack.com/toan-11-kn/bai-17-ham-so-lien-tuc.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép đề/lời giải |
| Đối chiếu dạng đề kiểm tra (trắc nghiệm, đúng/sai, trả lời ngắn) | Đề thi giữa kì 1 Toán 11 KNTT (đề số 9) – loigiaihay | https://loigiaihay.com/de-thi-giua-ki-1-toan-11-ket-noi-tri-thuc-de-so-9-a188921.html | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng/mức độ, không chép đề |
| Đối chiếu cấu trúc đề 2025 | Đề thi giữa kì 1 Toán 11 cấu trúc mới KNTT – VietJack | https://vietjack.com/de-kiem-tra-lop-11/de-thi-giua-ki-1-toan-lop-11-cau-truc-moi-ket-noi.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng, không chép đề |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.1 | 27/11/2026 | Bản trước: 5 bài, lời giải ngắn | agent runner-materials-l11 |
| v1.2 | 27/11/2026 | Thêm 2 bài (5→7, có trắc nghiệm/đúng-sai/trả lời ngắn), viết lại 2 lời giải từng bước, cập nhật nguồn | agent runner-mat-300 |
