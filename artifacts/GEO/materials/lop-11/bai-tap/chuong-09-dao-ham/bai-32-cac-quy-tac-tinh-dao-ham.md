# Bài tập – Bài 32. Các quy tắc tính đạo hàm

Phiên bản: v1.2 · 27/11/2026

> Tài liệu tổng hợp/biên soạn lại cho mục đích học tập (không sao chép nguyên văn SGK/SBT). Bộ sách xương sống: Toán 11 Kết nối tri thức với cuộc sống.

*Chương 9. Đạo hàm* · Lý thuyết: [ly-thuyet](../../ly-thuyet/chuong-09-dao-ham/bai-32-cac-quy-tac-tinh-dao-ham.md)

### Nhận biết
**1.** $y=x^3-2x+5$. $y'$? *Giải.* $3x^2-2$.

### Thông hiểu
**2.** $y=x\sin x$. *Giải.* $y'=\sin x+x\cos x$.

**3.** $y=\dfrac{x+1}{x-1}$. *Giải.* $y'=\dfrac{(x-1)-(x+1)}{(x-1)^2}=\dfrac{-2}{(x-1)^2}$.

**4.** *[Đúng/sai]* a) $(\sin x)'=\cos x$; b) $(\cos2x)'=-2\sin2x$; c) $(x\ln x)'=\ln x$; d) $(e^{2x})'=2e^{2x}$.
*Giải.*
- a) **Đúng**. b) $-\sin2x\cdot(2x)'=-2\sin2x$: **Đúng**.
- c) $(x\ln x)'=\ln x+x\cdot\dfrac1x=\ln x+1$: **Sai**.
- d) **Đúng**.
- Đáp án: Đ – Đ – S – Đ.

### Vận dụng
**5.** Tính đạo hàm của $y=\ln(x^2+1)$.
*Giải.*
- Quy tắc: $(\ln u)'=\dfrac{u'}{u}$ với $u=x^2+1$, $u'=2x$.
- $y'=\dfrac{2x}{x^2+1}$.

**6.** Viết phương trình tiếp tuyến của $y=x^3-3x$ tại điểm có hoành độ $2$.
*Giải.*
- Bước 1: $y'=3x^2-3$, $y'(2)=9$; $y(2)=8-6=2$.
- Bước 2: phương trình $y=9(x-2)+2=9x-16$.

**7.** *[Trả lời ngắn]* Cho $y=x^2e^x$. Tính $y'(1)$.
*Giải.*
- $y'=2xe^x+x^2e^x=(x^2+2x)e^x$.
- $y'(1)=(1+2)e=3e$.

### Vận dụng cao
**8.** Tìm $m$ để tiếp tuyến của $y=x^3-mx$ tại điểm có hoành độ $1$ song song với đường thẳng $y=2x+5$.
*Giải.*
- Bước 1: $y'=3x^2-m$, hệ số góc tại $x=1$ là $3-m$.
- Bước 2: song song với $y=2x+5$ nên $3-m=2\Rightarrow m=1$.
- Bước 3: kiểm tra không trùng: $y(1)=1-m=0$, tiếp tuyến $y=2(x-1)=2x-2\ne2x+5$ (không trùng). Vậy $m=1$.

## Nguồn

| Nội dung trong bài | Nguồn | Link | Ghi chú |
|---|---|---|---|
| Toàn bộ đề và lời giải | Tác giả tài liệu | - | tự soạn (cùng dạng với SGK/SBT, không trích nguyên văn) |
| Đối chiếu dạng bài | SBT Toán 11 KNTT trên loigiaihay | https://loigiaihay.com/sbt-toan-11-ket-noi-tri-thuc-c1567.html | chỉ tham khảo dạng, không chép |
| Đối chiếu dạng bài, mức độ | Giải bài tập SGK Toán 11 KNTT – loigiaihay (Bài 32) | https://loigiaihay.com/bai-32-cac-quy-tac-tinh-dao-ham-toan-11-ket-noi-tri-thuc-e31973.html | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép đề/lời giải |
| Đối chiếu dạng bài, mức độ | Giải SGK Toán 11 KNTT – VietJack (Bài 32) | https://vietjack.com/toan-11-kn/bai-32-cac-quy-tac-tinh-dao-ham.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép đề/lời giải |
| Đối chiếu dạng đề kiểm tra (trắc nghiệm, đúng/sai, trả lời ngắn) | Đề thi giữa kì 1 Toán 11 KNTT (đề số 9) – loigiaihay | https://loigiaihay.com/de-thi-giua-ki-1-toan-11-ket-noi-tri-thuc-de-so-9-a188921.html | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng/mức độ, không chép đề |
| Đối chiếu cấu trúc đề 2025 | Đề thi giữa kì 1 Toán 11 cấu trúc mới KNTT – VietJack | https://vietjack.com/de-kiem-tra-lop-11/de-thi-giua-ki-1-toan-lop-11-cau-truc-moi-ket-noi.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng, không chép đề |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.1 | 27/11/2026 | Bản trước: 6 bài, lời giải ngắn | agent runner-materials-l11 |
| v1.2 | 27/11/2026 | Thêm 2 bài (6→8, có trắc nghiệm/đúng-sai/trả lời ngắn), viết lại 3 lời giải từng bước, cập nhật nguồn | agent runner-mat-300 |
