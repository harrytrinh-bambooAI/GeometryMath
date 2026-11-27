# Bài tập – Bài 22. Hai đường thẳng vuông góc

Phiên bản: v1.2 · 27/11/2026

> Tài liệu tổng hợp/biên soạn lại cho mục đích học tập (không sao chép nguyên văn SGK/SBT). Bộ sách xương sống: Toán 11 Kết nối tri thức với cuộc sống.

*Chương 7. Quan hệ vuông góc trong không gian* · Lý thuyết: [ly-thuyet](../../ly-thuyet/chuong-07-quan-he-vuong-goc/bai-22-hai-duong-thang-vuong-goc.md)

### Nhận biết
**1.** Góc giữa $AB$ và $CD$ trong hình lập phương $ABCD.A'B'C'D'$? *Giải.* $AB\parallel CD$: $0^\circ$.

**2.** *[Trắc nghiệm]* Trong hình lập phương $ABCD.A'B'C'D'$, góc giữa $AB$ và $A'D'$ bằng: A. $0^\circ$; B. $45^\circ$; C. $60^\circ$; D. $90^\circ$.
*Giải.* $A'D'\parallel AD$ nên góc $(AB,A'D')=(AB,AD)=90^\circ$ ($ABCD$ là hình vuông). Chọn **D**.

### Thông hiểu
**3.** Hình lập phương $ABCD.A'B'C'D'$. Tính góc giữa $AB'$ và $BC'$.
![Hình lập phương/hình hộp ABCD.A'B'C'D'](../../hinh/hinh-hop.svg)
*Giải.*
- Bước 1: $BC'\parallel AD'$ (vì $ABC'D'$ là hình bình hành), nên góc $(AB',BC')$ bằng góc $(AB',AD')$.
- Bước 2: $\triangle AB'D'$ có $AB'=B'D'=D'A=a\sqrt2$ (ba đường chéo các mặt) nên là tam giác đều.
- Bước 3: $\widehat{B'AD'}=60^\circ$, vậy góc giữa hai đường thẳng là $60^\circ$.

**4.** *[Đúng/sai]* Hình lập phương $ABCD.A'B'C'D'$. a) $AC\perp BD$; b) $AC\perp B'D'$; c) góc giữa $AC$ và $A'D$ bằng $60^\circ$; d) góc giữa $BD'$ và $AC$ bằng $60^\circ$.
![Hình lập phương/hình hộp ABCD.A'B'C'D'](../../hinh/hinh-hop.svg)
*Giải.*
- a) Hai đường chéo hình vuông $ABCD$: **Đúng**.
- b) $AC\parallel A'C'$ và $A'C'\perp B'D'$ (đường chéo hình vuông $A'B'C'D'$): **Đúng**.
- c) $A'D\parallel B'C$; $\triangle ACB'$ đều (cạnh $a\sqrt2$) nên góc $(AC,B'C)=60^\circ$: **Đúng**.
- d) $AC\perp BD$ và $AC\perp DD'$ nên $AC\perp(BDD'B')\Rightarrow AC\perp BD'$, góc $90^\circ$: **Sai**.
- Đáp án: Đ – Đ – Đ – S.

### Vận dụng
**5.** Tứ diện đều $ABCD$ cạnh $a$. Chứng minh $AB\perp CD$.
*Giải.*
- Bước 1: gọi $M$ là trung điểm $CD$.
- Bước 2: $\triangle ACD$ đều nên $AM\perp CD$; $\triangle BCD$ đều nên $BM\perp CD$.
- Bước 3: $AM,BM$ cắt nhau trong $(ABM)$ nên $CD\perp(ABM)$, mà $AB\subset(ABM)$ nên $CD\perp AB$. ∎

**6.** *[Trả lời ngắn]* Tứ diện đều $ABCD$ cạnh $a$, $M$ là trung điểm $BC$. Tính $\cos$ góc giữa $AB$ và $DM$.
*Giải.*
- Bước 1: gọi $P$ là trung điểm $AC$; $PM\parallel AB$, $PM=\dfrac a2$; nên góc $(AB,DM)$ bằng góc $\widehat{PMD}$.
- Bước 2: $DM=PD=\dfrac{a\sqrt3}2$ (đường trung tuyến của tam giác đều cạnh $a$).
- Bước 3: $\cos\widehat{PMD}=\dfrac{MP^2+MD^2-PD^2}{2\,MP\cdot MD}=\dfrac{\frac{a^2}4}{2\cdot\frac a2\cdot\frac{a\sqrt3}2}=\dfrac1{2\sqrt3}=\dfrac{\sqrt3}6$.
- Đáp số: $\dfrac{\sqrt3}6$ ($\approx0{,}289$).

### Vận dụng cao
**7.** Tứ diện $ABCD$ có $AB=CD=a$; $M,N$ lần lượt là trung điểm $BC,AD$ và $MN=\dfrac{a\sqrt3}{2}$. Tính góc giữa $AB$ và $CD$.
*Giải.*
- Bước 1: gọi $P$ là trung điểm $AC$. $PM$ là đường trung bình của $\triangle ABC$: $PM\parallel AB$, $PM=\dfrac a2$. $PN$ là đường trung bình của $\triangle ACD$: $PN\parallel CD$, $PN=\dfrac a2$.
- Bước 2: góc $(AB,CD)$ bằng góc giữa $PM$ và $PN$ (hoặc bù với nó).
- Bước 3: $\cos\widehat{MPN}=\dfrac{PM^2+PN^2-MN^2}{2\,PM\cdot PN}=\dfrac{\frac{a^2}4+\frac{a^2}4-\frac{3a^2}4}{\frac{a^2}2}=-\dfrac12\Rightarrow\widehat{MPN}=120^\circ$.
- Bước 4: góc giữa hai đường thẳng nằm trong $[0^\circ;90^\circ]$ nên bằng $180^\circ-120^\circ=60^\circ$.

## Nguồn

| Nội dung trong bài | Nguồn | Link | Ghi chú |
|---|---|---|---|
| Toàn bộ đề và lời giải | Tác giả tài liệu | - | tự soạn (cùng dạng với SGK/SBT, không trích nguyên văn) |
| Đối chiếu dạng bài | SBT Toán 11 KNTT trên loigiaihay | https://loigiaihay.com/sbt-toan-11-ket-noi-tri-thuc-c1567.html | chỉ tham khảo dạng, không chép |
| Đối chiếu dạng bài, mức độ | Giải bài tập SGK Toán 11 KNTT – loigiaihay (Bài 22) | https://loigiaihay.com/bai-22-hai-duong-thang-vuong-goc-toan-11-ket-noi-tri-thuc-e31959.html | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép đề/lời giải |
| Đối chiếu dạng bài, mức độ | Giải SGK Toán 11 KNTT – VietJack (Bài 22) | https://vietjack.com/toan-11-kn/bai-22-hai-duong-thang-vuong-goc.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ dẫn, không chép đề/lời giải |
| Đối chiếu dạng đề kiểm tra (trắc nghiệm, đúng/sai, trả lời ngắn) | Đề thi giữa kì 1 Toán 11 KNTT (đề số 9) – loigiaihay | https://loigiaihay.com/de-thi-giua-ki-1-toan-11-ket-noi-tri-thuc-de-so-9-a188921.html | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng/mức độ, không chép đề |
| Đối chiếu cấu trúc đề 2025 | Đề thi giữa kì 1 Toán 11 cấu trúc mới KNTT – VietJack | https://vietjack.com/de-kiem-tra-lop-11/de-thi-giua-ki-1-toan-lop-11-cau-truc-moi-ket-noi.jsp | đã mở được (HTTP 200, 27/11/2026); chỉ tham khảo dạng, không chép đề |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.1 | 27/11/2026 | Bản trước: 4 bài, lời giải ngắn | agent runner-materials-l11 |
| v1.2 | 27/11/2026 | Thêm 3 bài (4→7, có trắc nghiệm/đúng-sai/trả lời ngắn), viết lại 3 lời giải từng bước, cập nhật nguồn | agent runner-mat-300 |
