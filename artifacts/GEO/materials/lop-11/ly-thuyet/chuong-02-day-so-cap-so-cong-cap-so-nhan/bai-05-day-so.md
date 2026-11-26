# Bài 5. Dãy số

Phiên bản: v1.1 · 27/11/2026

> Tài liệu tổng hợp/biên soạn lại cho mục đích học tập (không sao chép nguyên văn SGK/SBT). Bộ sách xương sống: Toán 11 Kết nối tri thức với cuộc sống.

*Chương 2. Dãy số. Cấp số cộng và cấp số nhân (Tập 1)* · Bài tập: [bai-tap](../../bai-tap/chuong-02-day-so-cap-so-cong-cap-so-nhan/bai-05-day-so.md)

## 1. Định nghĩa
Dãy số vô hạn là hàm số $u:\mathbb N^*\to\mathbb R$, $n\mapsto u_n$; viết $(u_n)$, $u_1$ là số hạng đầu, $u_n$ là số hạng tổng quát. Dãy số hữu hạn có đủ $m$ số hạng.

## 2. Cách cho dãy số
- Bằng công thức số hạng tổng quát: $u_n=\dfrac{n}{n+1}$.
- Bằng công thức truy hồi: cho $u_1$ (hoặc vài số hạng đầu) và hệ thức $u_{n+1}=f(u_n)$.
- Bằng cách mô tả hoặc liệt kê.

## 3. Dãy số tăng, giảm, bị chặn
- Tăng: $u_{n+1}>u_n\ \forall n$. Giảm: $u_{n+1}<u_n\ \forall n$. (Xét dấu $u_{n+1}-u_n$, hoặc so $\dfrac{u_{n+1}}{u_n}$ với 1 khi $u_n>0$.)
- Bị chặn trên: $\exists M,\ u_n\le M\ \forall n$; bị chặn dưới: $\exists m,\ u_n\ge m$; bị chặn: vừa chặn trên vừa chặn dưới.

**Ví dụ.** $u_n=\dfrac{n}{n+1}$: $u_{n+1}-u_n=\dfrac{1}{(n+1)(n+2)}>0$ nên tăng; $0<u_n<1$ nên bị chặn.

## Nguồn

| Nội dung trong bài | Nguồn | Link | Ghi chú |
|---|---|---|---|
| Vị trí bài, thứ tự mục | SGK Toán 11 KNTT (mục lục) | https://loigiaihay.com/sgk-toan-11-ket-noi-tri-thuc-c1370.html | tham chiếu mục lục |
| Định nghĩa, định lí, công thức, ví dụ | Kiến thức chương trình GDPT 2018 | - | tự soạn/viết lại bằng lời riêng |
| Đối chiếu tên bài, thứ tự mục lý thuyết | SGK Toán 11 KNTT – loigiaihay (Bài 5) | https://loigiaihay.com/bai-5-day-so-toan-11-ket-noi-tri-thuc-e31491.html | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép |
| Đối chiếu lý thuyết trọng tâm | Lý thuyết Toán 11 KNTT – VietJack (Bài 5) | https://vietjack.com/toan-11-kn/ly-thuyet-bai-5-day-so.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép |
