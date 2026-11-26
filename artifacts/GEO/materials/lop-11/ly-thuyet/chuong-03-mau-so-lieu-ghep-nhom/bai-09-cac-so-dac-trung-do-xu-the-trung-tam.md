# Bài 9. Các số đặc trưng đo xu thế trung tâm

Phiên bản: v1.1 · 27/11/2026

> Tài liệu tổng hợp/biên soạn lại cho mục đích học tập (không sao chép nguyên văn SGK/SBT). Bộ sách xương sống: Toán 11 Kết nối tri thức với cuộc sống.

*Chương 3. Các số đặc trưng đo xu thế trung tâm của mẫu số liệu ghép nhóm (Tập 1)* · Bài tập: [bai-tap](../../bai-tap/chuong-03-mau-so-lieu-ghep-nhom/bai-09-cac-so-dac-trung-do-xu-the-trung-tam.md)

Mẫu ghép nhóm $k$ nhóm, tần số $m_i$, giá trị đại diện $x_i$, cỡ mẫu $n$.

## 1. Số trung bình
$$\bar x=\frac{m_1x_1+\dots+m_kx_k}{n}.$$

## 2. Trung vị $M_e$
Tìm nhóm $[a_p;a_{p+1})$ đầu tiên có tần số tích luỹ $\ge n/2$:
$$M_e=a_p+\frac{\frac n2-cf_{p-1}}{m_p}(a_{p+1}-a_p).$$

## 3. Tứ phân vị
$Q_1$ ứng với $n/4$, $Q_3$ ứng với $3n/4$, tính như trung vị: $Q_j=a_p+\dfrac{\frac{jn}4-cf_{p-1}}{m_p}(a_{p+1}-a_p)$; $Q_2=M_e$.

## 4. Mốt $M_o$
Nhóm có tần số lớn nhất $[a_j;a_{j+1})$:
$$M_o=a_j+\frac{m_j-m_{j-1}}{(m_j-m_{j-1})+(m_j-m_{j+1})}(a_{j+1}-a_j).$$

**Ví dụ.** Dữ liệu ở bài 8 ($8,14,12,6$; $n=40$): $\bar x=\dfrac{8\cdot152{,}5+14\cdot157{,}5+12\cdot162{,}5+6\cdot167{,}5}{40}=159{,}75$. $n/2=20$, nhóm $[155;160)$: $M_e=155+\dfrac{20-8}{14}\cdot5\approx159{,}29$.

## Nguồn

| Nội dung trong bài | Nguồn | Link | Ghi chú |
|---|---|---|---|
| Vị trí bài, thứ tự mục | SGK Toán 11 KNTT (mục lục) | https://loigiaihay.com/sgk-toan-11-ket-noi-tri-thuc-c1370.html | tham chiếu mục lục |
| Định nghĩa, định lí, công thức, ví dụ | Kiến thức chương trình GDPT 2018 | - | tự soạn/viết lại bằng lời riêng |
| Đối chiếu tên bài, thứ tự mục lý thuyết | SGK Toán 11 KNTT – loigiaihay (Bài 9) | https://loigiaihay.com/bai-9-cac-so-dac-trung-do-xu-the-trung-tam-toan-11-ket-noi-tri-thuc-e31520.html | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép |
| Đối chiếu lý thuyết trọng tâm | Lý thuyết Toán 11 KNTT – VietJack (Bài 9) | https://vietjack.com/toan-11-kn/ly-thuyet-bai-9-cac-so-dac-trung-do-xu-the-trung-tam.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép |
