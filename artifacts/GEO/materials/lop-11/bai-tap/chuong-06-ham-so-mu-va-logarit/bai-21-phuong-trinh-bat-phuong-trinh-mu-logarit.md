# Bài tập – Bài 21. Phương trình, bất phương trình mũ và lôgarit

Phiên bản: v1.2 · 27/11/2026

> Tài liệu tổng hợp/biên soạn lại cho mục đích học tập (không sao chép nguyên văn SGK/SBT). Bộ sách xương sống: Toán 11 Kết nối tri thức với cuộc sống.

*Chương 6. Hàm số mũ và hàm số lôgarit* · Lý thuyết: [ly-thuyet](../../ly-thuyet/chuong-06-ham-so-mu-va-logarit/bai-21-phuong-trinh-bat-phuong-trinh-mu-logarit.md)

### Nhận biết
**1.** $2^x=8$. *Giải.* $x=3$.

**2.** *[Trắc nghiệm]* Nghiệm của phương trình $\log_3x=2$ là: A. $x=6$; B. $x=9$; C. $x=8$; D. $x=\dfrac23$.
*Giải.* $x=3^2=9$ (điều kiện $x>0$ thỏa). Chọn **B**.

### Thông hiểu
**3.** $\log_2(x-1)=3$. *Giải.* $x=9$.

**4.** $4^x-3\cdot2^x+2=0$. *Giải.* $t=2^x$: $t^2-3t+2=0$, $t=1,2$: $x=0,1$.

### Vận dụng
**5.** Giải $\log_2x+\log_2(x-3)=2$.
*Giải.*
- Bước 1: điều kiện $x>0$ và $x-3>0$, tức $x>3$.
- Bước 2: $\log_2[x(x-3)]=2\Rightarrow x(x-3)=4\Rightarrow x^2-3x-4=0\Rightarrow x=4$ hoặc $x=-1$.
- Bước 3: đối chiếu điều kiện: loại $x=-1$, nhận $x=4$.

**6.** Giải bất phương trình $\left(\tfrac12\right)^{x}>\tfrac14$.
*Giải.*
- Bước 1: $\tfrac14=\left(\tfrac12\right)^2$ nên bất phương trình thành $\left(\tfrac12\right)^x>\left(\tfrac12\right)^2$.
- Bước 2: cơ số $\dfrac12<1$ nên đảo chiều: $x<2$.

### Vận dụng cao
**7.** Gửi $200$ triệu đồng, lãi $7\%$/năm (lãi nhập gốc). Sau ít nhất bao nhiêu năm số tiền vượt $300$ triệu?
*Giải.*
- Bước 1: sau $n$ năm: $200(1{,}07)^n$.
- Bước 2: $200(1{,}07)^n>300\Rightarrow(1{,}07)^n>1{,}5\Rightarrow n>\log_{1{,}07}1{,}5=\dfrac{\ln1{,}5}{\ln1{,}07}\approx5{,}99$.
- Bước 3: $n$ nguyên nên $n=6$. Thử: $200\cdot1{,}07^6\approx300{,}1>300$.

**8.** *[Trả lời ngắn]* Tìm giá trị nguyên lớn nhất của $m$ để phương trình $4^x-2^{x+1}+m=0$ có nghiệm thực.
*Giải.*
- Bước 1: đặt $t=2^x>0$: $t^2-2t+m=0$. Cần phương trình này có nghiệm $t>0$.
- Bước 2: có nghiệm khi $\Delta'=1-m\ge0\Leftrightarrow m\le1$.
- Bước 3: với $m\le1$: tổng hai nghiệm $=2>0$; nếu $m>0$ tích $>0$ nên cả hai nghiệm dương; nếu $m\le0$ thì nghiệm $1+\sqrt{1-m}>0$. Vậy điều kiện chỉ là $m\le1$.
- Số nguyên lớn nhất: $m=1$ (khi đó $t=1$, $x=0$).

## Nguồn

| Nội dung trong bài | Nguồn | Link | Ghi chú |
|---|---|---|---|
| Toàn bộ đề và lời giải | Tác giả tài liệu | - | tự soạn (cùng dạng với SGK/SBT, không trích nguyên văn) |
| Đối chiếu dạng bài | SBT Toán 11 KNTT trên loigiaihay | https://loigiaihay.com/sbt-toan-11-ket-noi-tri-thuc-c1567.html | chỉ tham khảo dạng, không chép |
| Đối chiếu dạng bài, mức độ | Giải bài tập SGK Toán 11 KNTT – loigiaihay (Bài 21) | https://loigiaihay.com/bai-21-phuong-trinh-bat-phuong-trinh-mu-va-logarit-toan-11-ket-noi-tri-thuc-e31939.html | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép đề/lời giải |
| Đối chiếu dạng bài, mức độ | Giải SGK Toán 11 KNTT – VietJack (Bài 21) | https://vietjack.com/toan-11-kn/bai-21-phuong-trinh-bat-phuong-trinh-mu-va-logarit.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép đề/lời giải |
| Đối chiếu dạng đề kiểm tra (trắc nghiệm, đúng/sai, trả lời ngắn) | Đề thi giữa kì 1 Toán 11 KNTT (đề số 9) – loigiaihay | https://loigiaihay.com/de-thi-giua-ki-1-toan-11-ket-noi-tri-thuc-de-so-9-a188921.html | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng/mức độ, không chép đề |
| Đối chiếu cấu trúc đề 2025 | Đề thi giữa kì 1 Toán 11 cấu trúc mới KNTT – VietJack | https://vietjack.com/de-kiem-tra-lop-11/de-thi-giua-ki-1-toan-lop-11-cau-truc-moi-ket-noi.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng, không chép đề |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.1 | 27/11/2026 | Bản trước: 6 bài, lời giải ngắn | agent runner-materials-l11 |
| v1.2 | 27/11/2026 | Thêm 2 bài (6→8, có trắc nghiệm/đúng-sai/trả lời ngắn), viết lại 3 lời giải từng bước, cập nhật nguồn | agent runner-mat-300 |
