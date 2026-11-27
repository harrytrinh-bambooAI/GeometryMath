# Bài tập – Bài 16. Giới hạn của hàm số

Phiên bản: v1.2 · 27/11/2026

> Tài liệu tổng hợp/biên soạn lại cho mục đích học tập (không sao chép nguyên văn SGK/SBT). Bộ sách xương sống: Toán 11 Kết nối tri thức với cuộc sống.

*Chương 5. Giới hạn. Hàm số liên tục* · Lý thuyết: [ly-thuyet](../../ly-thuyet/chuong-05-gioi-han-ham-so-lien-tuc/bai-16-gioi-han-cua-ham-so.md)

### Nhận biết
**1.** $\lim_{x\to1}(x^2+3x)$. *Giải.* $4$.

### Thông hiểu
**2.** $\lim_{x\to3}\dfrac{x^2-9}{x-3}$. *Giải.* $6$.

**3.** $\lim_{x\to+\infty}\dfrac{2x^2+x}{x^2-1}$. *Giải.* $2$.

**4.** *[Đúng/sai]* a) $\lim_{x\to2}(x^2-x)=2$; b) $\lim_{x\to+\infty}(x^3-3x)=+\infty$; c) $\lim_{x\to-\infty}(x^3+1)=+\infty$; d) $\lim_{x\to2^-}\dfrac1{x-2}=-\infty$.
*Giải.*
- a) $4-2=2$: **Đúng**.
- b) $x^3\left(1-\dfrac3{x^2}\right)\to+\infty$: **Đúng**.
- c) $x^3\to-\infty$: **Sai**.
- d) $x<2$ nên $x-2\to0^-$, kết quả $-\infty$: **Đúng**.
- Đáp án: Đ – Đ – S – Đ.

### Vận dụng
**5.** $\lim_{x\to1^+}\dfrac{x+1}{x-1}$ và $\lim_{x\to1^-}\dfrac{x+1}{x-1}$. *Giải.* Tử $\to2>0$; $x-1\to0^+$ nên $+\infty$; từ trái $-\infty$.

**6.** $\lim_{x\to0}\dfrac{\sqrt{1+x}-1}{x}$. *Giải.* $=\lim\dfrac1{\sqrt{1+x}+1}=\dfrac12$.

**7.** *[Trả lời ngắn]* Tính $\lim_{x\to1}\dfrac{x^2-1}{x^3-1}$.
*Giải.*
- Dạng $\dfrac00$; phân tích: $x^2-1=(x-1)(x+1)$, $x^3-1=(x-1)(x^2+x+1)$.
- Rút gọn $x-1\ne0$: $\dfrac{x+1}{x^2+x+1}\to\dfrac23$.
- Đáp số: $\dfrac23$.

### Vận dụng cao
**8.** Tính $\lim_{x\to+\infty}\left(\sqrt{x^2+2x}-x\right)$.
*Giải.*
- Bước 1: dạng $\infty-\infty$, nhân liên hợp: $\sqrt{x^2+2x}-x=\dfrac{2x}{\sqrt{x^2+2x}+x}$.
- Bước 2: với $x>0$ chia tử và mẫu cho $x$: $\dfrac{2}{\sqrt{1+2/x}+1}$.
- Bước 3: khi $x\to+\infty$: $\dfrac{2}{1+1}=1$.

## Nguồn

| Nội dung trong bài | Nguồn | Link | Ghi chú |
|---|---|---|---|
| Toàn bộ đề và lời giải | Tác giả tài liệu | - | tự soạn (cùng dạng với SGK/SBT, không trích nguyên văn) |
| Đối chiếu dạng bài | SBT Toán 11 KNTT trên loigiaihay | https://loigiaihay.com/sbt-toan-11-ket-noi-tri-thuc-c1567.html | chỉ tham khảo dạng, không chép |
| Đối chiếu dạng bài, mức độ | Giải bài tập SGK Toán 11 KNTT – loigiaihay (Bài 16) | https://loigiaihay.com/bai-16-gioi-han-cua-ham-so-toan-11-ket-noi-tri-thuc-e31543.html | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép đề/lời giải |
| Đối chiếu dạng bài, mức độ | Giải SGK Toán 11 KNTT – VietJack (Bài 16) | https://vietjack.com/toan-11-kn/bai-16-gioi-han-cua-ham-so.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép đề/lời giải |
| Đối chiếu dạng đề kiểm tra (trắc nghiệm, đúng/sai, trả lời ngắn) | Đề thi giữa kì 1 Toán 11 KNTT (đề số 9) – loigiaihay | https://loigiaihay.com/de-thi-giua-ki-1-toan-11-ket-noi-tri-thuc-de-so-9-a188921.html | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng/mức độ, không chép đề |
| Đối chiếu cấu trúc đề 2025 | Đề thi giữa kì 1 Toán 11 cấu trúc mới KNTT – VietJack | https://vietjack.com/de-kiem-tra-lop-11/de-thi-giua-ki-1-toan-lop-11-cau-truc-moi-ket-noi.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng, không chép đề |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.1 | 27/11/2026 | Bản trước: 6 bài, lời giải ngắn | agent runner-materials-l11 |
| v1.2 | 27/11/2026 | Thêm 2 bài (6→8, có trắc nghiệm/đúng-sai/trả lời ngắn), viết lại 1 lời giải từng bước, cập nhật nguồn | agent runner-mat-300 |
