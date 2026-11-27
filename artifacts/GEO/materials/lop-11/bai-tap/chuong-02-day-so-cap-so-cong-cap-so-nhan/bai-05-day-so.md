# Bài tập – Bài 5. Dãy số

Phiên bản: v1.2 · 27/11/2026

> Tài liệu tổng hợp/biên soạn lại cho mục đích học tập (không sao chép nguyên văn SGK/SBT). Bộ sách xương sống: Toán 11 Kết nối tri thức với cuộc sống.

*Chương 2. Dãy số. Cấp số cộng và cấp số nhân* · Lý thuyết: [ly-thuyet](../../ly-thuyet/chuong-02-day-so-cap-so-cong-cap-so-nhan/bai-05-day-so.md)

### Nhận biết
**1.** Viết 4 số hạng đầu của $u_n=2n^2-1$. *Giải.* $1,7,17,31$.

**2.** Dãy $u_1=2$, $u_{n+1}=3u_n-1$. Tính $u_3$. *Giải.* $u_2=5$, $u_3=14$.

### Thông hiểu
**3.** Xét tính tăng giảm của $u_n=\dfrac{2n+1}{n+2}$. *Giải.* $u_{n+1}-u_n=\dfrac{2n+3}{n+3}-\dfrac{2n+1}{n+2}=\dfrac{3}{(n+2)(n+3)}>0$: tăng.

**4.** Chứng minh $u_n=\dfrac{n}{n^2+1}$ bị chặn. *Giải.* $0<u_n\le\dfrac12$ vì $n^2+1\ge2n$.

**5.** *[Đúng/sai]* Cho dãy $u_n=\dfrac{2n-1}{n+1}$. a) $u_1=\dfrac12$; b) $u_3=\dfrac54$; c) dãy tăng; d) $u_n\ge1$ với mọi $n\ge1$.
*Giải.*
- a) $u_1=\dfrac{1}{2}$: **Đúng**. b) $u_3=\dfrac{5}{4}$: **Đúng**.
- c) $u_n=2-\dfrac3{n+1}$ và $\dfrac3{n+1}$ giảm nên $u_n$ tăng: **Đúng**.
- d) $u_1=\dfrac12<1$: **Sai**.
- Đáp án: Đ – Đ – Đ – S.

### Vận dụng
**6.** Gửi 100 triệu, lãi 6%/năm, lãi nhập gốc. Viết công thức số tiền sau $n$ năm. *Giải.* $A_n=100(1{,}06)^n$ (triệu); dãy tăng.

**7.** *[Trả lời ngắn]* Cho dãy $u_1=1$, $u_{n+1}=2u_n+1$. Tính $u_5$.
*Giải.*
- $u_2=2\cdot1+1=3$; $u_3=2\cdot3+1=7$; $u_4=2\cdot7+1=15$; $u_5=2\cdot15+1=31$.
- Đáp số: $31$ (công thức tổng quát $u_n=2^n-1$).

### Vận dụng cao
**8.** Cho $u_1=1$, $u_{n+1}=\sqrt{2+u_n}$. Chứng minh $u_n<2$ với mọi $n\ge1$.
*Giải.* (quy nạp)
- Bước 1 (cơ sở): $u_1=1<2$.
- Bước 2 (giả thiết): giả sử $u_k<2$ với một số $k\ge1$.
- Bước 3 (suy ra): $2+u_k<4$ nên $u_{k+1}=\sqrt{2+u_k}<\sqrt4=2$.
- Bước 4: theo nguyên lí quy nạp, $u_n<2$ với mọi $n\ge1$. ∎

## Nguồn

| Nội dung trong bài | Nguồn | Link | Ghi chú |
|---|---|---|---|
| Toàn bộ đề và lời giải | Tác giả tài liệu | - | tự soạn (cùng dạng với SGK/SBT, không trích nguyên văn) |
| Đối chiếu dạng bài | SBT Toán 11 KNTT trên loigiaihay | https://loigiaihay.com/sbt-toan-11-ket-noi-tri-thuc-c1567.html | chỉ tham khảo dạng, không chép |
| Đối chiếu dạng bài, mức độ | Giải bài tập SGK Toán 11 KNTT – loigiaihay (Bài 5) | https://loigiaihay.com/bai-5-day-so-toan-11-ket-noi-tri-thuc-e31491.html | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép đề/lời giải |
| Đối chiếu dạng bài, mức độ | Giải SGK Toán 11 KNTT – VietJack (Bài 5) | https://vietjack.com/toan-11-kn/bai-5-day-so.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép đề/lời giải |
| Đối chiếu dạng đề kiểm tra (trắc nghiệm, đúng/sai, trả lời ngắn) | Đề thi giữa kì 1 Toán 11 KNTT (đề số 9) – loigiaihay | https://loigiaihay.com/de-thi-giua-ki-1-toan-11-ket-noi-tri-thuc-de-so-9-a188921.html | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng/mức độ, không chép đề |
| Đối chiếu cấu trúc đề 2025 | Đề thi giữa kì 1 Toán 11 cấu trúc mới KNTT – VietJack | https://vietjack.com/de-kiem-tra-lop-11/de-thi-giua-ki-1-toan-lop-11-cau-truc-moi-ket-noi.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng, không chép đề |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.1 | 27/11/2026 | Bản trước: 6 bài, lời giải ngắn | agent runner-materials-l11 |
| v1.2 | 27/11/2026 | Thêm 2 bài (6→8, có trắc nghiệm/đúng-sai/trả lời ngắn), viết lại 1 lời giải từng bước, cập nhật nguồn | agent runner-mat-300 |
