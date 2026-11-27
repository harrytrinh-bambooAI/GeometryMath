# Bài tập cuối chương 1. Hàm số lượng giác và phương trình lượng giác

Phiên bản: v1.2 · 27/11/2026

> Tài liệu tổng hợp/biên soạn lại cho mục đích học tập (không sao chép nguyên văn SGK/SBT). Bộ sách xương sống: Toán 11 Kết nối tri thức với cuộc sống.

### Nhận biết
**1.** *[Trắc nghiệm]* Tập xác định của hàm số $y=\tan2x$ là: A. $\mathbb R\setminus\left\{\dfrac\pi4+\dfrac{k\pi}2\right\}$; B. $\mathbb R\setminus\left\{\dfrac\pi2+k\pi\right\}$; C. $\mathbb R\setminus\{k\pi\}$; D. $\mathbb R\setminus\left\{\dfrac\pi4+k\pi\right\}$.
*Giải.*
- Điều kiện $\cos2x\ne0\Leftrightarrow2x\ne\dfrac\pi2+k\pi$.
- Chia $2$: $x\ne\dfrac\pi4+\dfrac{k\pi}2$. Chọn **A**.

### Thông hiểu
**2.** Cho $\sin x=-\dfrac{4}{5}$, $\pi<x<\dfrac{3\pi}{2}$. Tính $\cos2x$ và $\tan x$. *Giải.* $\cos2x=1-2\sin^2x=1-\dfrac{32}{25}=-\dfrac7{25}$; $\cos x=-\dfrac35$, $\tan x=\dfrac43$.

**3.** *[Đúng/sai]* Xét phương trình $\sin x=\dfrac12$ trên $[0;2\pi]$. a) $x=\dfrac\pi6$ là nghiệm; b) $x=\dfrac{5\pi}6$ là nghiệm; c) $x=\dfrac{7\pi}6$ là nghiệm; d) phương trình có đúng $2$ nghiệm.
*Giải.*
- Nghiệm tổng quát: $x=\dfrac\pi6+k2\pi$ hoặc $x=\dfrac{5\pi}6+k2\pi$.
- a) Đúng; b) Đúng; c) $\sin\dfrac{7\pi}6=-\dfrac12$: Sai; d) Trong $[0;2\pi]$ chỉ có $\dfrac\pi6,\dfrac{5\pi}6$: Đúng.
- Đáp án: Đ – Đ – S – Đ.

### Vận dụng
**4.** Giải $2\cos^2x-3\cos x+1=0$.
*Giải.*
- Bước 1: đặt $t=\cos x\in[-1;1]$: $2t^2-3t+1=0\Rightarrow t=1$ hoặc $t=\dfrac12$ (cả hai thuộc $[-1;1]$).
- Bước 2: $\cos x=1\Rightarrow x=k2\pi$; $\cos x=\dfrac12\Rightarrow x=\pm\dfrac\pi3+k2\pi$ ($k\in\mathbb Z$).

**5.** Giải $\sin x+\cos x=1$.
*Giải.*
- Bước 1: $\sin x+\cos x=\sqrt2\sin\left(x+\dfrac\pi4\right)$ nên phương trình thành $\sin\left(x+\dfrac\pi4\right)=\dfrac{\sqrt2}2=\sin\dfrac\pi4$.
- Bước 2: $x+\dfrac\pi4=\dfrac\pi4+k2\pi\Rightarrow x=k2\pi$; hoặc $x+\dfrac\pi4=\dfrac{3\pi}4+k2\pi\Rightarrow x=\dfrac\pi2+k2\pi$.
- Kết luận: $x=k2\pi$ hoặc $x=\dfrac\pi2+k2\pi$ ($k\in\mathbb Z$).

### Vận dụng cao
**6.** Tìm giá trị lớn nhất và nhỏ nhất của $y=\sin^2x+2\cos x$.
*Giải.*
- Bước 1: $\sin^2x=1-\cos^2x$, đặt $c=\cos x\in[-1;1]$: $y=-c^2+2c+1=-(c-1)^2+2$.
- Bước 2: parabol quay xuống, đỉnh $c=1\in[-1;1]$: $\max y=2$ khi $c=1$.
- Bước 3: so hai đầu mút: $c=-1\Rightarrow y=-1-2+1=-2$; $c=1\Rightarrow y=2$. Vậy $\min y=-2$ khi $c=-1$.

**7.** Số giờ có ánh sáng ở một thành phố là $d(t)=12+2\sin\dfrac{2\pi(t-80)}{365}$ ($t$ là ngày thứ $t$ trong năm). Ngày nào $d=13$ (một nghiệm trong $[0;365]$)?
*Giải.*
- Bước 1: $d=13\Rightarrow\sin\dfrac{2\pi(t-80)}{365}=\dfrac12$.
- Bước 2: lấy nghiệm nhỏ nhất: $\dfrac{2\pi(t-80)}{365}=\dfrac\pi6\Rightarrow t-80=\dfrac{365}{12}\approx30{,}4$.
- Bước 3: $t\approx110$ (ngày thứ 110). Nghiệm thứ hai $\dfrac{2\pi(t-80)}{365}=\dfrac{5\pi}6$ cho $t-80=\dfrac{5\cdot365}{12}\approx152$, $t\approx232$ cũng thuộc $[0;365]$. Vậy có hai ngày: khoảng ngày 110 và ngày 232.

**8.** *[Trả lời ngắn]* Có bao nhiêu nghiệm của phương trình $\sin x+\sqrt3\cos x=1$ trong đoạn $[0;2\pi]$?
*Giải.*
- Bước 1: $\sin x+\sqrt3\cos x=2\sin\left(x+\dfrac\pi3\right)$, phương trình thành $\sin\left(x+\dfrac\pi3\right)=\dfrac12$.
- Bước 2: $x+\dfrac\pi3=\dfrac\pi6+k2\pi\Rightarrow x=-\dfrac\pi6+k2\pi$, thuộc $[0;2\pi]$ khi $k=1$: $x=\dfrac{11\pi}6$.
- Bước 3: $x+\dfrac\pi3=\dfrac{5\pi}6+k2\pi\Rightarrow x=\dfrac\pi2+k2\pi$, thuộc $[0;2\pi]$ khi $k=0$: $x=\dfrac\pi2$.
- Có đúng $2$ nghiệm.

## Nguồn

| Nội dung trong bài | Nguồn | Link | Ghi chú |
|---|---|---|---|
| Đề và lời giải ôn tập | Tác giả tài liệu | - | tự soạn |
| Đối chiếu tên mục "Bài tập cuối chương" | SGK Toán 11 KNTT – loigiaihay | https://loigiaihay.com/bai-tap-cuoi-chuong-1-toan-11-ket-noi-tri-thuc-e30804.html | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn tới lời giải, không chép |
| Đối chiếu dạng bài cuối chương | Giải SGK Toán 11 KNTT – VietJack | https://vietjack.com/toan-11-kn/bai-tap-cuoi-chuong-1.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép |
| Tổng hợp lý thuyết chương | Lý thuyết Toán 11 KNTT – VietJack | https://vietjack.com/toan-11-kn/ly-thuyet-bai-tap-cuoi-chuong-1.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép |
| Đối chiếu dạng đề kiểm tra (trắc nghiệm, đúng/sai, trả lời ngắn) | Đề thi giữa kì 1 Toán 11 KNTT (đề số 9) – loigiaihay | https://loigiaihay.com/de-thi-giua-ki-1-toan-11-ket-noi-tri-thuc-de-so-9-a188921.html | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng/mức độ, không chép đề |
| Đối chiếu cấu trúc đề 2025 | Đề thi giữa kì 1 Toán 11 cấu trúc mới KNTT – VietJack | https://vietjack.com/de-kiem-tra-lop-11/de-thi-giua-ki-1-toan-lop-11-cau-truc-moi-ket-noi.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng, không chép đề |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.1 | 27/11/2026 | Bản trước: 5 bài, lời giải ngắn | agent runner-materials-l11 |
| v1.2 | 27/11/2026 | Thêm 3 bài (5→8, có trắc nghiệm/đúng-sai/trả lời ngắn), viết lại 4 lời giải từng bước, cập nhật nguồn | agent runner-mat-300 |
