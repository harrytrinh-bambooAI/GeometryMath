# Bài tập – Bài 28. Biến cố hợp, biến cố giao, biến cố độc lập

Phiên bản: v1.2 · 27/11/2026

> Tài liệu tổng hợp/biên soạn lại cho mục đích học tập (không sao chép nguyên văn SGK/SBT). Bộ sách xương sống: Toán 11 Kết nối tri thức với cuộc sống.

*Chương 8. Các quy tắc tính xác suất* · Lý thuyết: [ly-thuyet](../../ly-thuyet/chuong-08-quy-tac-tinh-xac-suat/bai-28-bien-co-hop-bien-co-giao-bien-co-doc-lap.md)

### Nhận biết
**1.** Gieo xúc xắc. $A$: "số chẵn", $B$: "số chia hết cho 3". Liệt kê $A\cup B$, $A\cap B$. *Giải.* $A\cup B=\{2,3,4,6\}$, $A\cap B=\{6\}$.

**2.** *[Trắc nghiệm]* Gieo một con xúc xắc; $A$ là biến cố "số chấm là số nguyên tố". Biến cố đối $\bar A$ là: A. $\{1,4,6\}$; B. $\{1,2,3\}$; C. $\{4,6\}$; D. $\{1,6\}$.
*Giải.* $A=\{2,3,5\}$ nên $\bar A=\{1,2,3,4,5,6\}\setminus A=\{1,4,6\}$. Chọn **A**.

### Thông hiểu
**3.** $A,B$ có xung khắc không (câu 1)? *Giải.* Không, vì giao là $\{6\}$.

**4.** *[Đúng/sai]* Gieo một đồng xu cân đối hai lần. $A$: "lần 1 sấp"; $B$: "lần 2 ngửa"; $C$: "hai lần giống nhau". a) $A,B$ độc lập; b) $A,C$ độc lập; c) $A,B$ xung khắc; d) $P(A\cup B)=\dfrac34$.
*Giải.* Không gian mẫu có $4$ kết quả đồng khả năng.
- a) $P(A)=P(B)=\dfrac12$, $P(AB)=\dfrac14=P(A)P(B)$: **Đúng**.
- b) $P(C)=\dfrac12$, $P(AC)=P(\text{SS})=\dfrac14=P(A)P(C)$: **Đúng**.
- c) Có kết quả "lần 1 sấp, lần 2 ngửa" chung: **Sai**.
- d) $\dfrac12+\dfrac12-\dfrac14=\dfrac34$: **Đúng**.
- Đáp án: Đ – Đ – S – Đ.

### Vận dụng
**5.** Hai xạ thủ bắn độc lập. $A$: "xạ thủ 1 trúng", $B$: "xạ thủ 2 trúng". Mô tả biến cố "có đúng một người trúng". *Giải.* $(A\cap\bar B)\cup(\bar A\cap B)$.

**6.** *[Trả lời ngắn]* Gieo hai con xúc xắc. $A$ là biến cố "tổng số chấm $\ge10$". Có bao nhiêu kết quả thuận lợi cho $A$?
*Giải.*
- Tổng $10$: $(4,6),(5,5),(6,4)$ — $3$ kết quả.
- Tổng $11$: $(5,6),(6,5)$ — $2$ kết quả. Tổng $12$: $(6,6)$ — $1$ kết quả.
- Tổng cộng: $3+2+1=6$.

### Vận dụng cao
**7.** Chứng minh nếu $A,B$ xung khắc và cùng có xác suất dương thì $A,B$ không độc lập.
*Giải.*
- Bước 1: giả sử $A,B$ độc lập, khi đó $P(A\cap B)=P(A)P(B)>0$ (vì cả hai dương).
- Bước 2: nhưng $A,B$ xung khắc nên $A\cap B=\varnothing$, $P(A\cap B)=0$.
- Bước 3: mâu thuẫn, vậy $A,B$ không độc lập. ∎

## Nguồn

| Nội dung trong bài | Nguồn | Link | Ghi chú |
|---|---|---|---|
| Toàn bộ đề và lời giải | Tác giả tài liệu | - | tự soạn (cùng dạng với SGK/SBT, không trích nguyên văn) |
| Đối chiếu dạng bài | SBT Toán 11 KNTT trên loigiaihay | https://loigiaihay.com/sbt-toan-11-ket-noi-tri-thuc-c1567.html | chỉ tham khảo dạng, không chép |
| Đối chiếu dạng bài, mức độ | Giải bài tập SGK Toán 11 KNTT – loigiaihay (Bài 28) | https://loigiaihay.com/bai-28-bien-co-hop-bien-co-giao-bien-co-doc-lap-toan-11-ket-noi-tri-thuc-e31967.html | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép đề/lời giải |
| Đối chiếu dạng bài, mức độ | Giải SGK Toán 11 KNTT – VietJack (Bài 28) | https://vietjack.com/toan-11-kn/bai-28-bien-co-hop-bien-co-giao.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép đề/lời giải |
| Đối chiếu dạng đề kiểm tra (trắc nghiệm, đúng/sai, trả lời ngắn) | Đề thi giữa kì 1 Toán 11 KNTT (đề số 9) – loigiaihay | https://loigiaihay.com/de-thi-giua-ki-1-toan-11-ket-noi-tri-thuc-de-so-9-a188921.html | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng/mức độ, không chép đề |
| Đối chiếu cấu trúc đề 2025 | Đề thi giữa kì 1 Toán 11 cấu trúc mới KNTT – VietJack | https://vietjack.com/de-kiem-tra-lop-11/de-thi-giua-ki-1-toan-lop-11-cau-truc-moi-ket-noi.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng, không chép đề |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.1 | 27/11/2026 | Bản trước: 4 bài, lời giải ngắn | agent runner-materials-l11 |
| v1.2 | 27/11/2026 | Thêm 3 bài (4→7, có trắc nghiệm/đúng-sai/trả lời ngắn), viết lại 1 lời giải từng bước, cập nhật nguồn | agent runner-mat-300 |
