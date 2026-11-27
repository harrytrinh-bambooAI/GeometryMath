# Bài tập – Bài 31. Định nghĩa và ý nghĩa của đạo hàm

Phiên bản: v1.2 · 27/11/2026

> Tài liệu tổng hợp/biên soạn lại cho mục đích học tập (không sao chép nguyên văn SGK/SBT). Bộ sách xương sống: Toán 11 Kết nối tri thức với cuộc sống.

*Chương 9. Đạo hàm* · Lý thuyết: [ly-thuyet](../../ly-thuyet/chuong-09-dao-ham/bai-31-dinh-nghia-va-y-nghia-cua-dao-ham.md)

### Nhận biết
**1.** Đạo hàm của $f(x)=x^2$ tại $x_0=3$ theo định nghĩa. *Giải.* $\lim\dfrac{x^2-9}{x-3}=\lim(x+3)=6$.

**2.** *[Trắc nghiệm]* Hệ số góc của tiếp tuyến của đồ thị $y=f(x)$ tại điểm có hoành độ $x_0$ là: A. $f(x_0)$; B. $f'(x_0)$; C. $f''(x_0)$; D. $\dfrac1{f'(x_0)}$.
*Giải.* Ý nghĩa hình học của đạo hàm. Chọn **B**.

### Thông hiểu
**3.** Viết phương trình tiếp tuyến của $y=x^2$ tại điểm có hoành độ 1. *Giải.* $f'(1)=2$, $f(1)=1$: $y=2x-1$.

**4.** *[Đúng/sai]* Cho $y=x^3$. a) $y'(1)=3$; b) tiếp tuyến tại điểm có hoành độ $1$ là $y=3x-2$; c) tiếp tuyến đó cắt $Oy$ tại $(0;-2)$; d) hệ số góc tiếp tuyến tại $x=-1$ bằng $-3$.
*Giải.* $y'=3x^2$.
- a) $y'(1)=3$: **Đúng**.
- b) $y(1)=1$; $y=3(x-1)+1=3x-2$: **Đúng**.
- c) $x=0\Rightarrow y=-2$: **Đúng**.
- d) $y'(-1)=3(-1)^2=3\ne-3$: **Sai**.
- Đáp án: Đ – Đ – Đ – S.

### Vận dụng
**5.** Chất điểm chuyển động có $s(t)=t^2+2t$ (m). Tính vận tốc tại $t=3$ s.
*Giải.*
- Bước 1: vận tốc tức thời là đạo hàm của quãng đường: $v(t)=s'(t)$.
- Bước 2: $s'(t)=2t+2$ (quy tắc ở bài 32).
- Bước 3: $v(3)=2\cdot3+2=8$ m/s.

**6.** *[Trả lời ngắn]* Tiếp tuyến của đồ thị $y=x^2-2x$ tại điểm $M$ có hệ số góc bằng $4$. Tìm hoành độ của $M$.
*Giải.*
- $y'=2x-2$.
- Hệ số góc $=4\Rightarrow2x-2=4\Rightarrow x=3$.

### Vận dụng cao
**7.** Chứng minh $f(x)=|x|$ không có đạo hàm tại $x=0$.
*Giải.*
- Bước 1: xét $\dfrac{f(x)-f(0)}{x-0}=\dfrac{|x|}x$.
- Bước 2: $x\to0^+$: $\dfrac{|x|}x=1\to1$. $x\to0^-$: $\dfrac{|x|}x=-1\to-1$.
- Bước 3: hai giới hạn một phía khác nhau nên không tồn tại $\lim_{x\to0}\dfrac{f(x)-f(0)}x$, tức $f'(0)$ không tồn tại. ∎

## Nguồn

| Nội dung trong bài | Nguồn | Link | Ghi chú |
|---|---|---|---|
| Toàn bộ đề và lời giải | Tác giả tài liệu | - | tự soạn (cùng dạng với SGK/SBT, không trích nguyên văn) |
| Đối chiếu dạng bài | SBT Toán 11 KNTT trên loigiaihay | https://loigiaihay.com/sbt-toan-11-ket-noi-tri-thuc-c1567.html | chỉ tham khảo dạng, không chép |
| Đối chiếu dạng bài, mức độ | Giải bài tập SGK Toán 11 KNTT – loigiaihay (Bài 31) | https://loigiaihay.com/bai-31-dinh-nghia-va-y-nghia-cua-dao-ham-toan-11-ket-noi-tri-thuc-e31972.html | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép đề/lời giải |
| Đối chiếu dạng bài, mức độ | Giải SGK Toán 11 KNTT – VietJack (Bài 31) | https://vietjack.com/toan-11-kn/bai-31-dinh-nghia-va-y-nghia-cua-dao-ham.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép đề/lời giải |
| Đối chiếu dạng đề kiểm tra (trắc nghiệm, đúng/sai, trả lời ngắn) | Đề thi giữa kì 1 Toán 11 KNTT (đề số 9) – loigiaihay | https://loigiaihay.com/de-thi-giua-ki-1-toan-11-ket-noi-tri-thuc-de-so-9-a188921.html | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng/mức độ, không chép đề |
| Đối chiếu cấu trúc đề 2025 | Đề thi giữa kì 1 Toán 11 cấu trúc mới KNTT – VietJack | https://vietjack.com/de-kiem-tra-lop-11/de-thi-giua-ki-1-toan-lop-11-cau-truc-moi-ket-noi.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng, không chép đề |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.1 | 27/11/2026 | Bản trước: 4 bài, lời giải ngắn | agent runner-materials-l11 |
| v1.2 | 27/11/2026 | Thêm 3 bài (4→7, có trắc nghiệm/đúng-sai/trả lời ngắn), viết lại 2 lời giải từng bước, cập nhật nguồn | agent runner-mat-300 |
