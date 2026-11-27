# Bài tập cuối chương 2. Dãy số. Cấp số cộng và cấp số nhân

Phiên bản: v1.2 · 27/11/2026

> Tài liệu tổng hợp/biên soạn lại cho mục đích học tập (không sao chép nguyên văn SGK/SBT). Bộ sách xương sống: Toán 11 Kết nối tri thức với cuộc sống.

### Nhận biết
**1.** *[Trắc nghiệm]* Dãy số nào sau đây là cấp số cộng? A. $1,3,6,10$; B. $2,5,8,11$; C. $1,2,4,8$; D. $1,\dfrac12,\dfrac14,\dfrac18$.
*Giải.*
- A: hiệu $2,3,4$ không đổi? Không. C, D: là cấp số nhân (tỉ số $2$ và $\tfrac12$), hiệu không đều.
- B: hiệu liên tiếp luôn $3$. Chọn **B**.

### Thông hiểu
**2.** CSC có $u_1=-2$, $S_{10}=70$. Tìm $d$. *Giải.* $5(-4+9d)=70\Rightarrow d=2$.

**3.** CSN có $S_3=7$, $u_1=1$. Tìm $q$. *Giải.* $1+q+q^2=7\Rightarrow q=2$ hoặc $q=-3$.

### Vận dụng
**4.** Tìm $n$ để $1+3+5+\dots+(2n-1)=169$.
*Giải.*
- Bước 1: các số hạng lập cấp số cộng $u_1=1$, $d=2$, $u_n=2n-1$.
- Bước 2: $S_n=\dfrac{n(1+2n-1)}2=n^2$.
- Bước 3: $n^2=169\Rightarrow n=13$ (loại $n=-13$).

**5.** Một quả bóng thả từ $10$ m, mỗi lần nảy lên bằng $60\%$ độ cao trước. Độ cao sau lần nảy thứ $4$?
*Giải.*
- Mỗi lần nảy, độ cao nhân $0{,}6$: cấp số nhân $u_0=10$, $q=0{,}6$.
- Sau lần thứ $4$: $10\cdot0{,}6^4=10\cdot0{,}1296=1{,}296$ m.

### Vận dụng cao
**6.** Chứng minh nếu $a,b,c$ lập cấp số cộng thì $a^2+ab+b^2,\ a^2+ac+c^2,\ b^2+bc+c^2$ lập cấp số cộng.
*Giải.*
- Bước 1: đặt $X=a^2+ab+b^2$, $Y=a^2+ac+c^2$, $Z=b^2+bc+c^2$. Cần chứng minh $Y-X=Z-Y$.
- Bước 2: $Y-X=a(c-b)+(c^2-b^2)=(c-b)(a+b+c)$.
- Bước 3: $Z-Y=(b^2-a^2)+c(b-a)=(b-a)(a+b+c)$.
- Bước 4: vì $a,b,c$ lập cấp số cộng nên $c-b=b-a$, suy ra $Y-X=Z-Y$. ∎

**7.** *[Trả lời ngắn]* Cấp số nhân dương $(u_n)$ có $u_1u_2u_3=27$ và $u_1+u_3=10$, công bội $q>1$. Tính $u_5$.
*Giải.*
- Bước 1: $u_1u_2u_3=u_2^3=27\Rightarrow u_2=3$.
- Bước 2: $u_1+u_3=\dfrac3q+3q=10\Rightarrow3q^2-10q+3=0\Rightarrow q=3$ hoặc $q=\dfrac13$; chọn $q=3$ (vì $q>1$).
- Bước 3: $u_5=u_2q^3=3\cdot27=81$.

## Nguồn

| Nội dung trong bài | Nguồn | Link | Ghi chú |
|---|---|---|---|
| Đề và lời giải ôn tập | Tác giả tài liệu | - | tự soạn |
| Đối chiếu tên mục "Bài tập cuối chương" | SGK Toán 11 KNTT – loigiaihay | https://loigiaihay.com/bai-tap-cuoi-chuong-ii-toan-11-ket-noi-tri-thuc-e31494.html | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn tới lời giải, không chép |
| Đối chiếu dạng bài cuối chương | Giải SGK Toán 11 KNTT – VietJack | https://vietjack.com/toan-11-kn/bai-tap-cuoi-chuong-2.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép |
| Tổng hợp lý thuyết chương | Lý thuyết Toán 11 KNTT – VietJack | https://vietjack.com/toan-11-kn/ly-thuyet-bai-tap-cuoi-chuong-2.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép |
| Đối chiếu dạng đề kiểm tra (trắc nghiệm, đúng/sai, trả lời ngắn) | Đề thi giữa kì 1 Toán 11 KNTT (đề số 9) – loigiaihay | https://loigiaihay.com/de-thi-giua-ki-1-toan-11-ket-noi-tri-thuc-de-so-9-a188921.html | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng/mức độ, không chép đề |
| Đối chiếu cấu trúc đề 2025 | Đề thi giữa kì 1 Toán 11 cấu trúc mới KNTT – VietJack | https://vietjack.com/de-kiem-tra-lop-11/de-thi-giua-ki-1-toan-lop-11-cau-truc-moi-ket-noi.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng, không chép đề |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.1 | 27/11/2026 | Bản trước: 5 bài, lời giải ngắn | agent runner-materials-l11 |
| v1.2 | 27/11/2026 | Thêm 2 bài (5→7, có trắc nghiệm/đúng-sai/trả lời ngắn), viết lại 3 lời giải từng bước, cập nhật nguồn | agent runner-mat-300 |
