# Bài tập cuối chương 8. Các quy tắc tính xác suất

Phiên bản: v1.2 · 27/11/2026

> Tài liệu tổng hợp/biên soạn lại cho mục đích học tập (không sao chép nguyên văn SGK/SBT). Bộ sách xương sống: Toán 11 Kết nối tri thức với cuộc sống.

### Nhận biết
**1.** *[Trắc nghiệm]* Xác suất của biến cố chắc chắn bằng: A. $0$; B. $\dfrac12$; C. $1$; D. không xác định.
*Giải.* Biến cố chắc chắn luôn xảy ra nên $P=1$. Chọn **C**.

### Thông hiểu
**2.** Hộp có 5 bi đỏ, 3 bi xanh. Lấy ngẫu nhiên 2 bi. Xác suất cùng màu. *Giải.* $\dfrac{C_5^2+C_3^2}{C_8^2}=\dfrac{13}{28}$.

**3.** Xác suất sinh viên đỗ môn A là $0{,}7$, đỗ môn B là $0{,}6$ (độc lập). Tính xác suất đỗ ít nhất một môn.
*Giải.*
- Bước 1: biến cố đối: trượt cả hai môn.
- Bước 2: $P(\text{trượt A})=0{,}3$, $P(\text{trượt B})=0{,}4$, độc lập nên $P(\text{trượt cả hai})=0{,}3\cdot0{,}4=0{,}12$.
- Bước 3: $P(\text{đỗ ít nhất một môn})=1-0{,}12=0{,}88$.

### Vận dụng
**4.** Gieo hai xúc xắc cân đối. $A$: "tổng bằng $7$"; $B$: "xúc xắc thứ nhất ra $3$". Kiểm tra tính độc lập của $A,B$ và tính $P(A\cup B)$.
*Giải.*
- Bước 1: không gian mẫu $36$ kết quả. $P(A)=\dfrac6{36}=\dfrac16$ (có $6$ cặp tổng $7$), $P(B)=\dfrac6{36}=\dfrac16$.
- Bước 2: $A\cap B=\{(3,4)\}$ nên $P(AB)=\dfrac1{36}=\dfrac16\cdot\dfrac16$: hai biến cố độc lập.
- Bước 3: $P(A\cup B)=\dfrac16+\dfrac16-\dfrac1{36}=\dfrac{11}{36}$.

**5.** Một cầu thủ sút phạt đền thành công $0{,}8$. Sút 3 lần độc lập, xác suất thành công đúng 2 lần. *Giải.* $3\cdot0{,}8^2\cdot0{,}2=0{,}384$.

### Vận dụng cao
**6.** *[Trả lời ngắn]* Một cầu thủ sút $3$ lần độc lập, mỗi lần thành công với xác suất $p$. Biết xác suất có ít nhất một lần thành công là $0{,}992$. Tìm $p$.
*Giải.*
- Bước 1: biến cố đối: cả ba lần đều trượt, xác suất $(1-p)^3$.
- Bước 2: $1-(1-p)^3=0{,}992\Rightarrow(1-p)^3=0{,}008=0{,}2^3$.
- Bước 3: $1-p=0{,}2\Rightarrow p=0{,}8$.

## Nguồn

| Nội dung trong bài | Nguồn | Link | Ghi chú |
|---|---|---|---|
| Đề và lời giải ôn tập | Tác giả tài liệu | - | tự soạn |
| Đối chiếu tên mục "Bài tập cuối chương" | SGK Toán 11 KNTT – loigiaihay | https://loigiaihay.com/bai-tap-cuoi-chuong-viii-toan-11-ket-noi-tri-thuc-e31970.html | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn tới lời giải, không chép |
| Đối chiếu dạng bài cuối chương | Giải SGK Toán 11 KNTT – VietJack | https://vietjack.com/toan-11-kn/bai-tap-cuoi-chuong-8.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép |
| Tổng hợp lý thuyết chương | Lý thuyết Toán 11 KNTT – VietJack | https://vietjack.com/toan-11-kn/ly-thuyet-bai-tap-cuoi-chuong-8.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép |
| Đối chiếu dạng đề kiểm tra (trắc nghiệm, đúng/sai, trả lời ngắn) | Đề thi giữa kì 1 Toán 11 KNTT (đề số 9) – loigiaihay | https://loigiaihay.com/de-thi-giua-ki-1-toan-11-ket-noi-tri-thuc-de-so-9-a188921.html | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng/mức độ, không chép đề |
| Đối chiếu cấu trúc đề 2025 | Đề thi giữa kì 1 Toán 11 cấu trúc mới KNTT – VietJack | https://vietjack.com/de-kiem-tra-lop-11/de-thi-giua-ki-1-toan-lop-11-cau-truc-moi-ket-noi.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng, không chép đề |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.1 | 27/11/2026 | Bản trước: 4 bài, lời giải ngắn | agent runner-materials-l11 |
| v1.2 | 27/11/2026 | Thêm 2 bài (4→6, có trắc nghiệm/đúng-sai/trả lời ngắn), viết lại 2 lời giải từng bước, cập nhật nguồn | agent runner-mat-300 |
