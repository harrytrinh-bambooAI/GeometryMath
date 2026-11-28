# Bài tập cuối chương 5. Giới hạn. Hàm số liên tục

Phiên bản: v1.2 · 27/11/2026

> Tài liệu tổng hợp/biên soạn lại cho mục đích học tập (không sao chép nguyên văn SGK/SBT). Bộ sách xương sống: Toán 11 Kết nối tri thức với cuộc sống.

### Nhận biết
**1.** *[Trắc nghiệm]* $\lim_{x\to-\infty}x^3$ bằng: A. $+\infty$; B. $-\infty$; C. $0$; D. $1$.
*Giải.* $x<0$ có độ lớn tăng nên $x^3\to-\infty$. Chọn **B**.

### Thông hiểu
**2.** $\lim\dfrac{(2n+1)(n-3)}{n^2+4}$. *Giải.* $2$.

**3.** $\lim_{x\to2}\dfrac{x^3-8}{x^2-4}$. *Giải.* $\dfrac{x^2+2x+4}{x+2}\to\dfrac{12}4=3$.

### Vận dụng
**4.** Tính $\lim_{x\to-\infty}\dfrac{3x+1}{\sqrt{x^2+1}}$.
*Giải.*
- Bước 1: $x\to-\infty$ nên $x<0$ và $\sqrt{x^2+1}=|x|\sqrt{1+1/x^2}=-x\sqrt{1+1/x^2}$.
- Bước 2: chia tử và mẫu cho $x$: $\dfrac{3+1/x}{-\sqrt{1+1/x^2}}$.
- Bước 3: giới hạn $=\dfrac{3}{-1}=-3$.

**5.** Tìm $a$ để $f(x)=\begin{cases}\dfrac{\sqrt{x+3}-2}{x-1}&x\ne1\\ a&x=1\end{cases}$ liên tục tại $x=1$.
*Giải.*
- Bước 1: cần $\lim_{x\to1}f(x)=f(1)=a$.
- Bước 2: nhân liên hợp: $\dfrac{\sqrt{x+3}-2}{x-1}=\dfrac{(x+3)-4}{(x-1)(\sqrt{x+3}+2)}=\dfrac1{\sqrt{x+3}+2}$.
- Bước 3: khi $x\to1$: $\dfrac1{2+2}=\dfrac14$. Vậy $a=\dfrac14$.

### Vận dụng cao
**6.** Chứng minh phương trình $(1-m^2)x^5-3x-1=0$ luôn có nghiệm với mọi $m$.
*Giải.*
- Bước 1: $f(x)=(1-m^2)x^5-3x-1$ là đa thức nên liên tục trên $\mathbb R$.
- Bước 2: $f(0)=-1<0$.
- Bước 3: $f(-1)=-(1-m^2)+3-1=1+m^2>0$ với mọi $m$.
- Bước 4: $f(-1)f(0)<0$ nên phương trình có nghiệm trong $(-1;0)$ với mọi $m$. ∎

**7.** *[Trả lời ngắn]* Tìm $a$ để $\lim_{x\to+\infty}\left(\sqrt{x^2+ax+1}-x\right)=3$.
*Giải.*
- Bước 1: nhân liên hợp: $\sqrt{x^2+ax+1}-x=\dfrac{ax+1}{\sqrt{x^2+ax+1}+x}$.
- Bước 2: chia tử và mẫu cho $x>0$: $\dfrac{a+1/x}{\sqrt{1+a/x+1/x^2}+1}\to\dfrac a2$.
- Bước 3: $\dfrac a2=3\Rightarrow a=6$.

## Nguồn

| Nội dung trong bài | Nguồn | Link | Ghi chú |
|---|---|---|---|
| Đề và lời giải ôn tập | Tác giả tài liệu | - | tự soạn |
| Đối chiếu tên mục "Bài tập cuối chương" | SGK Toán 11 KNTT – loigiaihay | https://loigiaihay.com/bai-tap-cuoi-chuong-v-toan-11-ket-noi-tri-thuc-e31545.html | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn tới lời giải, không chép |
| Đối chiếu dạng bài cuối chương | Giải SGK Toán 11 KNTT – VietJack | https://vietjack.com/toan-11-kn/bai-tap-cuoi-chuong-v.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép |
| Tổng hợp lý thuyết chương | Lý thuyết Toán 11 KNTT – VietJack | https://vietjack.com/toan-11-kn/ly-thuyet-bai-tap-cuoi-chuong-5.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép |
| Đối chiếu dạng đề kiểm tra (trắc nghiệm, đúng/sai, trả lời ngắn) | Đề thi giữa kì 1 Toán 11 KNTT (đề số 9) – loigiaihay | https://loigiaihay.com/de-thi-giua-ki-1-toan-11-ket-noi-tri-thuc-de-so-9-a188921.html | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng/mức độ, không chép đề |
| Đối chiếu cấu trúc đề 2025 | Đề thi giữa kì 1 Toán 11 cấu trúc mới KNTT – VietJack | https://vietjack.com/de-kiem-tra-lop-11/de-thi-giua-ki-1-toan-lop-11-cau-truc-moi-ket-noi.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng, không chép đề |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.1 | 27/11/2026 | Bản trước: 5 bài, lời giải ngắn | agent runner-materials-l11 |
| v1.2 | 27/11/2026 | Thêm 2 bài (5→7, có trắc nghiệm/đúng-sai/trả lời ngắn), viết lại 3 lời giải từng bước, cập nhật nguồn | agent runner-mat-300 |
