# Bài 15. Giới hạn của dãy số

Phiên bản: v1.0 · 27/11/2026

> Tài liệu tổng hợp/biên soạn lại cho mục đích học tập (không sao chép nguyên văn SGK/SBT). Bộ sách xương sống: Toán 11 Kết nối tri thức với cuộc sống.

*Chương 5. Giới hạn. Hàm số liên tục (Tập 2)* · Bài tập: [bai-tap](../../bai-tap/chuong-05-gioi-han-ham-so-lien-tuc/bai-15-gioi-han-cua-day-so.md)

## 1. Giới hạn hữu hạn
$\lim u_n=L$ nếu $|u_n-L|$ nhỏ tuỳ ý khi $n$ đủ lớn.
- $\lim\dfrac1{n^k}=0$ ($k>0$); $\lim q^n=0$ khi $|q|<1$; $\lim c=c$.
- Nếu $\lim u_n=a$, $\lim v_n=b$: $\lim(u_n\pm v_n)=a\pm b$, $\lim u_nv_n=ab$, $\lim\dfrac{u_n}{v_n}=\dfrac ab$ ($b\ne0$), $\lim\sqrt{u_n}=\sqrt a$ ($u_n\ge0$).

## 2. Giới hạn vô cực
$\lim u_n=+\infty$ nếu $u_n$ lớn hơn mọi số dương bất kì khi $n$ đủ lớn. $\lim n^k=+\infty$; $\lim q^n=+\infty$ khi $q>1$. $\lim u_n=+\infty\Rightarrow\lim\dfrac1{u_n}=0$.

## 3. Tổng cấp số nhân lùi vô hạn
$|q|<1$: $S=u_1+u_1q+\dots=\dfrac{u_1}{1-q}$.

## 4. Mẹo khử dạng vô định
Chia tử và mẫu cho luỹ thừa bậc cao nhất của $n$; hoặc nhân liên hợp với căn thức.

**Ví dụ.** $\lim\dfrac{3n^2+n}{2n^2-1}=\lim\dfrac{3+1/n}{2-1/n^2}=\dfrac32$.

## Nguồn

| Nội dung trong bài | Nguồn | Link | Ghi chú |
|---|---|---|---|
| Vị trí bài, thứ tự mục | SGK Toán 11 KNTT (mục lục) | https://loigiaihay.com/sgk-toan-11-ket-noi-tri-thuc-c1370.html | tham chiếu mục lục |
| Định nghĩa, định lí, công thức, ví dụ | Kiến thức chương trình GDPT 2018 | - | tự soạn/viết lại bằng lời riêng |
