# Bài tập – Bài 29. Công thức cộng xác suất

Phiên bản: v1.2 · 27/11/2026

> Tài liệu tổng hợp/biên soạn lại cho mục đích học tập (không sao chép nguyên văn SGK/SBT). Bộ sách xương sống: Toán 11 Kết nối tri thức với cuộc sống.

*Chương 8. Các quy tắc tính xác suất* · Lý thuyết: [ly-thuyet](../../ly-thuyet/chuong-08-quy-tac-tinh-xac-suat/bai-29-cong-thuc-cong-xac-suat.md)

### Nhận biết
**1.** $P(A)=0{,}4$, $P(B)=0{,}3$, $A,B$ xung khắc. $P(A\cup B)$? *Giải.* $0{,}7$.

**2.** *[Trắc nghiệm]* Với hai biến cố $A,B$ bất kì, công thức đúng là: A. $P(A\cup B)=P(A)+P(B)$; B. $P(A\cup B)=P(A)+P(B)-P(A\cap B)$; C. $P(A\cup B)=P(A)P(B)$; D. $P(A\cup B)=P(A)-P(B)$.
*Giải.* Công thức cộng xác suất tổng quát. Chọn **B** (A chỉ đúng khi $A,B$ xung khắc).

### Thông hiểu
**3.** Rút một lá bài từ 52 lá. Xác suất lá "cơ" hoặc "K"? *Giải.* $\dfrac{13}{52}+\dfrac4{52}-\dfrac1{52}=\dfrac{16}{52}=\dfrac4{13}$.

**4.** *[Đúng/sai]* Rút ngẫu nhiên một thẻ trong $10$ thẻ đánh số $1\ldots10$. $A$: "số chia hết cho $2$"; $B$: "số chia hết cho $3$". a) $P(A)=\dfrac12$; b) $P(B)=\dfrac3{10}$; c) $P(A\cap B)=\dfrac1{10}$; d) $P(A\cup B)=\dfrac45$.
*Giải.* $A=\{2,4,6,8,10\}$, $B=\{3,6,9\}$, $A\cap B=\{6\}$.
- a) **Đúng**; b) **Đúng**; c) **Đúng**.
- d) $P(A\cup B)=\dfrac{5+3-1}{10}=\dfrac7{10}\ne\dfrac45$: **Sai**.
- Đáp án: Đ – Đ – Đ – S.

### Vận dụng
**5.** Lớp có $40$ HS: $22$ thích bóng đá, $18$ thích bóng rổ, $8$ thích cả hai. Chọn ngẫu nhiên $1$ HS, tính xác suất bạn đó thích ít nhất một môn.
*Giải.*
- Bước 1: $P(A)=\dfrac{22}{40}$, $P(B)=\dfrac{18}{40}$, $P(A\cap B)=\dfrac8{40}$.
- Bước 2: $P(A\cup B)=P(A)+P(B)-P(A\cap B)=\dfrac{22+18-8}{40}=\dfrac{32}{40}=0{,}8$.

**6.** *[Trả lời ngắn]* Cho $P(A)=0{,}5$, $P(B)=0{,}4$, $P(A\cup B)=0{,}7$. Tính $P(A\cap B)$.
*Giải.*
- $P(A\cap B)=P(A)+P(B)-P(A\cup B)=0{,}5+0{,}4-0{,}7=0{,}2$.

### Vận dụng cao
**7.** Gieo xúc xắc $3$ lần. Tính xác suất có ít nhất một lần được mặt $6$.
*Giải.*
- Bước 1: biến cố đối: "không lần nào được mặt $6$".
- Bước 2: mỗi lần không ra $6$ có xác suất $\dfrac56$; ba lần độc lập: $\left(\dfrac56\right)^3=\dfrac{125}{216}$.
- Bước 3: xác suất cần tìm $=1-\dfrac{125}{216}=\dfrac{91}{216}\approx0{,}421$.

## Nguồn

| Nội dung trong bài | Nguồn | Link | Ghi chú |
|---|---|---|---|
| Toàn bộ đề và lời giải | Tác giả tài liệu | - | tự soạn (cùng dạng với SGK/SBT, không trích nguyên văn) |
| Đối chiếu dạng bài | SBT Toán 11 KNTT trên loigiaihay | https://loigiaihay.com/sbt-toan-11-ket-noi-tri-thuc-c1567.html | chỉ tham khảo dạng, không chép |
| Đối chiếu dạng bài, mức độ | Giải bài tập SGK Toán 11 KNTT – loigiaihay (Bài 29) | https://loigiaihay.com/bai-29-cong-thuc-cong-xac-suat-toan-11-ket-noi-tri-thuc-e31968.html | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép đề/lời giải |
| Đối chiếu dạng bài, mức độ | Giải SGK Toán 11 KNTT – VietJack (Bài 29) | https://vietjack.com/toan-11-kn/bai-29-cong-thuc-cong-xac-suat.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép đề/lời giải |
| Đối chiếu dạng đề kiểm tra (trắc nghiệm, đúng/sai, trả lời ngắn) | Đề thi giữa kì 1 Toán 11 KNTT (đề số 9) – loigiaihay | https://loigiaihay.com/de-thi-giua-ki-1-toan-11-ket-noi-tri-thuc-de-so-9-a188921.html | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng/mức độ, không chép đề |
| Đối chiếu cấu trúc đề 2025 | Đề thi giữa kì 1 Toán 11 cấu trúc mới KNTT – VietJack | https://vietjack.com/de-kiem-tra-lop-11/de-thi-giua-ki-1-toan-lop-11-cau-truc-moi-ket-noi.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng, không chép đề |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.1 | 27/11/2026 | Bản trước: 4 bài, lời giải ngắn | agent runner-materials-l11 |
| v1.2 | 27/11/2026 | Thêm 3 bài (4→7, có trắc nghiệm/đúng-sai/trả lời ngắn), viết lại 2 lời giải từng bước, cập nhật nguồn | agent runner-mat-300 |
