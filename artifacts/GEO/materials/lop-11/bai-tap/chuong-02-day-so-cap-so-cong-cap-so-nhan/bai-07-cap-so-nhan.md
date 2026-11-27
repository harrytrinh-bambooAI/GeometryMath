# Bài tập – Bài 7. Cấp số nhân

Phiên bản: v1.2 · 27/11/2026

> Tài liệu tổng hợp/biên soạn lại cho mục đích học tập (không sao chép nguyên văn SGK/SBT). Bộ sách xương sống: Toán 11 Kết nối tri thức với cuộc sống.

*Chương 2. Dãy số. Cấp số cộng và cấp số nhân* · Lý thuyết: [ly-thuyet](../../ly-thuyet/chuong-02-day-so-cap-so-cong-cap-so-nhan/bai-07-cap-so-nhan.md)

### Nhận biết
**1.** CSN $u_1=3$, $q=2$. Tính $u_6$. *Giải.* $3\cdot32=96$.

**2.** Dãy $2,6,18,54$ có là CSN? *Giải.* Có, $q=3$.

### Thông hiểu
**3.** CSN $u_3=12$, $u_6=96$. Tìm $q,u_1$. *Giải.* $q^3=8$, $q=2$, $u_1=3$.

**4.** Tính $1+2+4+\dots+2^{9}$. *Giải.* $\dfrac{2^{10}-1}{1}=1023$.

**5.** *[Đúng/sai]* Cấp số nhân $(u_n)$ có $u_1=2$, $q=-3$. a) $u_2=-6$; b) $u_4=-54$; c) dãy tăng; d) $S_3=14$.
*Giải.*
- a) $u_2=2\cdot(-3)=-6$: **Đúng**. b) $u_4=2\cdot(-3)^3=-54$: **Đúng**.
- c) $u_1=2>u_2=-6$, dãy đan dấu, không tăng: **Sai**.
- d) $S_3=2-6+18=14$: **Đúng**.
- Đáp án: Đ – Đ – S – Đ.

### Vận dụng
**6.** Dân số tăng $2\%$/năm, hiện nay $1$ triệu người. Sau $5$ năm là bao nhiêu?
*Giải.*
- Mỗi năm dân số nhân $1{,}02$: cấp số nhân với $u_0=10^6$, $q=1{,}02$.
- Sau $5$ năm: $10^6\cdot1{,}02^5=10^6\cdot1{,}10408\ldots\approx1\,104\,081$ người.

**7.** *[Trả lời ngắn]* Cấp số nhân có $5$ số hạng, $u_1=3$, $u_5=48$, công bội $q>0$. Tính tổng $5$ số hạng.
*Giải.*
- $u_5=u_1q^4\Rightarrow q^4=16\Rightarrow q=2$ (vì $q>0$).
- $S_5=u_1\dfrac{q^5-1}{q-1}=3\cdot\dfrac{32-1}{1}=93$.
- Đáp số: $93$ (các số: $3,6,12,24,48$).

### Vận dụng cao
**8.** Ba số dương lập cấp số nhân có tổng $21$, tích $216$. Tìm chúng.
*Giải.*
- Bước 1: gọi ba số $\dfrac aq,\ a,\ aq$. Tích $a^3=216\Rightarrow a=6$.
- Bước 2: tổng $6\left(\dfrac1q+1+q\right)=21\Rightarrow\dfrac1q+1+q=\dfrac72$.
- Bước 3: nhân $2q$: $2+2q+2q^2=7q\Rightarrow2q^2-5q+2=0\Rightarrow q=2$ hoặc $q=\dfrac12$.
- Bước 4: ba số là $3,6,12$ (hoặc $12,6,3$). Thử: $3+6+12=21$, $3\cdot6\cdot12=216$.

## Nguồn

| Nội dung trong bài | Nguồn | Link | Ghi chú |
|---|---|---|---|
| Toàn bộ đề và lời giải | Tác giả tài liệu | - | tự soạn (cùng dạng với SGK/SBT, không trích nguyên văn) |
| Đối chiếu dạng bài | SBT Toán 11 KNTT trên loigiaihay | https://loigiaihay.com/sbt-toan-11-ket-noi-tri-thuc-c1567.html | chỉ tham khảo dạng, không chép |
| Đối chiếu dạng bài, mức độ | Giải bài tập SGK Toán 11 KNTT – loigiaihay (Bài 7) | https://loigiaihay.com/bai-7-cap-so-nhan-toan-11-ket-noi-tri-thuc-e31493.html | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép đề/lời giải |
| Đối chiếu dạng bài, mức độ | Giải SGK Toán 11 KNTT – VietJack (Bài 7) | https://vietjack.com/toan-11-kn/bai-7-cap-so-nhan.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép đề/lời giải |
| Đối chiếu dạng đề kiểm tra (trắc nghiệm, đúng/sai, trả lời ngắn) | Đề thi giữa kì 1 Toán 11 KNTT (đề số 9) – loigiaihay | https://loigiaihay.com/de-thi-giua-ki-1-toan-11-ket-noi-tri-thuc-de-so-9-a188921.html | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng/mức độ, không chép đề |
| Đối chiếu cấu trúc đề 2025 | Đề thi giữa kì 1 Toán 11 cấu trúc mới KNTT – VietJack | https://vietjack.com/de-kiem-tra-lop-11/de-thi-giua-ki-1-toan-lop-11-cau-truc-moi-ket-noi.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng, không chép đề |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.1 | 27/11/2026 | Bản trước: 6 bài, lời giải ngắn | agent runner-materials-l11 |
| v1.2 | 27/11/2026 | Thêm 2 bài (6→8, có trắc nghiệm/đúng-sai/trả lời ngắn), viết lại 2 lời giải từng bước, cập nhật nguồn | agent runner-mat-300 |
