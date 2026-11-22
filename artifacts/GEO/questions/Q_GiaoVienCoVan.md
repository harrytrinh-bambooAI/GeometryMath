# Questions for GiaoVienCoVan — GEO

Feature GEO là MVP web app Hình Học Không Gian Cùng AI. Thầy cô quyết định các điểm chuyên môn Toán và sư phạm: dung sai kiểm chứng, thế nào là lộ đáp số, giờ mở lời giải, công cụ đo, trợ giúp khi giao bài, bộ lọc kho đề và cách đọc Theo dõi lớp.

**Every question already has a default.** Tài liệu và test case đang dùng nó, nên không có gì bị chặn. Câu trả lời của bạn chỉ xác nhận hoặc đổi mặc định.

**How to answer:** với mỗi câu, đánh dấu **Confirm the default**, hoặc **chọn phương án khác**, hoặc viết ghi chú. Mỗi câu có liên kết tới ảnh màn hình nếu liên quan.

## Assumptions in use (what the documents are built on today)
Đây là các điểm chỉ bạn xác nhận được. Tới khi bạn trả lời, story và test case dùng giả định ở cột 2 và mang nhãn PROVISIONAL.

| Decision | Assumption in use | Why this one (source) | Built on it (stories / AC / TC) | If your answer differs |
|---|---|---|---|---|
| [D-GEO-040](#q-gv-01) | Ràng buộc hình (vuông góc, song song, bằng nhau): sai số tuyệt đối ≤ 1e-6 trên hình có cạnh lớn nhất quy về 1. Đáp số: sai số tương đối ≤ 1e-4 so với giá trị tính từ tọa độ; kiểm với ít nhất 3 bộ giá trị tham số cụ thể (a = 1 và hai giá trị khác). Công thức ở `02_domain_model.md` M-GEO-02, 03 | không có nguồn số (`MVP.md` §5B chỉ nói "thay giá trị cụ thể") | US-GEO-203, 105; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-040) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-065](#q-gv-02) | Chứa đáp số hoặc giá trị quyết định ở mọi dạng viết tương đương (thập phân, phân số, căn thức, kèm hay không kèm đơn vị), hoặc chứa các bước của lời giải đang khóa; công thức và phương pháp tổng quát không tính (PO-44) | SRC-040, SRC-049 | US-GEO-609, 610, 611; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-065) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-041](#q-gv-03) | A; đồng hồ máy chủ là đồng hồ duy nhất; giờ máy khách không được tin | SRC-069 (ca giờ mở) | US-GEO-604, 608, 612; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-041) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-042](#q-gv-03) | A (lời giải đã lộ không thu hồi được). Mở ngay đặt `release_at` bằng giờ máy chủ lúc bấm | SRC-088 (ANS-01); SRC-069 | US-GEO-605; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-042) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-043](#q-gv-03) | Vẫn khóa tới khi giáo viên đặt giờ hoặc mở ngay | SRC-050 (BR-03, ghi suy diễn) | US-GEO-604, 608; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-043) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-053](#q-gv-04) | hai công tắc như khung `Design/mvp-02`: "Gợi ý mức 1 và 2" bật hoặc tắt; "Hỏi đáp (không nêu đáp số)" bật hoặc tắt; giờ mở lời giải độc lập với hai công tắc; tắt Hỏi đáp thì tab Hỏi đáp ẩn hoặc báo giáo viên đã tắt (câu chữ do BA đề xuất) | SRC-120 (câu hỏi mở), SRC-085 | US-GEO-603; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-053) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-024](#q-gv-05) | Ẩn giá trị của đại lượng đề hỏi và mọi đại lượng cùng giá trị, cùng vai trò trong cùng tam giác lời giải; vẫn đo được yếu tố trung gian; áp dụng cả bài tự học đang khóa | SRC-023, SRC-024, SRC-027 (PO-08, 17, 42) | US-GEO-305; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-024) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-027](#q-gv-06) | Trước giờ mở: dẫn tới khái niệm và bài giảng, nêu công thức và phương pháp tổng quát, không nêu đáp số hay các bước của bài đang làm; khi từ chối nhắc lại gợi ý đang có, không tính thêm lượt gợi ý; mọi kênh (xem trước, theo dõi lớp, hỏi đáp) tuân giờ mở | SRC-028, SRC-038, SRC-040 (PO-22, 37, 44) | US-GEO-608, 609, 607; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-027) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-070](#q-gv-07) | Cấp {THCS, THPT}; chủ đề {Góc, Khoảng cách, Thể tích, Diện tích, Mặt cầu} theo `materials/`; loại hình {chóp, lăng trụ, hộp, lập phương, nón, trụ, cầu}; "dạng bài" = chủ đề | SRC-092, SRC-117 | US-GEO-501; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-070) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-075](#q-gv-08) | Tiến độ = k/3 với k là số mốc đã đạt trong Gợi ý 1, Gợi ý 2, Lời giải (khung `Design/mvp-04`, `mvp-05` vẽ thanh ba đoạn). Công thức M-GEO-14 | `MVP.md` §3 (màn "Bài của em"), SRC-084, `Design/mvp-04` | US-GEO-606; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-075) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-077](#q-gv-08) | Công thức M-GEO-21…24; khung thiết kế không ghi định nghĩa | `Design/mvp-03-theo-doi-lop.png` | US-GEO-701, 702; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-077) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |

## Terms used
- Giờ mở lời giải: thời điểm giáo viên đặt, từ đó học sinh được xem lời giải.
- Gợi ý 1 (hướng nghĩ), Gợi ý 2 (bước kế tiếp), Lời giải (mức 3).
- Đại lượng đề hỏi: đại lượng bài yêu cầu tính; công cụ đo ẩn nó trước giờ mở.
- Lộ đáp số: câu trả lời hoặc dữ liệu chứa đáp số, giá trị quyết định hoặc bước giải của lời giải đang khóa.

| # | Topic | Ref | Screen | Needed by | Answer |
|---|---|---|---|---|---|
| [Q-GV-01](#q-gv-01) | Dung sai kiểm chứng | D-GEO-040 | — | R3 của E2 | |
| [Q-GV-02](#q-gv-02) | Thế nào là "lộ đáp số" | D-GEO-065 | [png](../../../Design/mvp-05-lam-bai.png) | R3 của E6 | |
| [Q-GV-03](#q-gv-03) | Giờ mở: ranh giới, đổi giờ, chưa đặt | D-GEO-041, D-GEO-042, D-GEO-043 | [png](../../../Design/mvp-03-theo-doi-lop.png) | R3 của E6 | |
| [Q-GV-04](#q-gv-04) | Trợ giúp học sinh được dùng khi giao bài | D-GEO-053 | [png](../../../Design/mvp-02-giao-bai.png) | R3 của E6 | |
| [Q-GV-05](#q-gv-05) | Công cụ đo ẩn gì trước giờ mở | D-GEO-024 | [png](../../../Design/mvp-05-lam-bai.png) | R3 của E3 | |
| [Q-GV-06](#q-gv-06) | Hỏi đáp trước giờ mở nói được gì | D-GEO-027 | — | R3 của E6 | |
| [Q-GV-07](#q-gv-07) | Bộ lọc Kho đề | D-GEO-070 | [png](../../../Design/mvp-01-kho-de.png) | R3 của E5 | |
| [Q-GV-08](#q-gv-08) | Tiến độ và số liệu Theo dõi lớp | D-GEO-075, D-GEO-077 | [png](../../../Design/mvp-03-theo-doi-lop.png) | R3 của E7 | |

---

<a id="q-gv-01"></a>
### Q-GV-01 · Dung sai kiểm chứng (ref D-GEO-040)

**Screen:** không có khung riêng

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-040).

<b style="color:#C00000">Question: Một đáp số được coi là khớp khi sai số tương đối ≤ 1e-4 so với giá trị tính từ tọa độ, thử với ít nhất 3 bộ giá trị tham số; ràng buộc hình khớp khi sai số tuyệt đối ≤ 1e-6. Có hợp lý về mặt Toán không?</b>

**Our current understanding (default used in the spec):** A. Không có nguồn số, do PO đặt

**Options:** A: đồng ý · B: đổi (nêu sai số và số bộ giá trị)

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-203, 105; M-GEO-02, 03 · **Needed by:** R3 của E2

---

<a id="q-gv-02"></a>
### Q-GV-02 · Thế nào là "lộ đáp số" (ref D-GEO-065)

**Screen:** [png](../../../Design/mvp-05-lam-bai.png)

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-065).

<b style="color:#C00000">Question: Câu trả lời lộ khi chứa đáp số hoặc giá trị quyết định ở mọi dạng viết tương đương (thập phân, phân số, căn thức), hoặc các bước của lời giải đang khóa; công thức và phương pháp tổng quát thì không. Đúng không?</b>

**Our current understanding (default used in the spec):** A

**Options:** A: đồng ý · B: đổi (nêu ranh giới)

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-609, 610, 611 · **Needed by:** R3 của E6

---

<a id="q-gv-03"></a>
### Q-GV-03 · Giờ mở: ranh giới, đổi giờ, chưa đặt (ref D-GEO-041, D-GEO-042, D-GEO-043)

**Screen:** [png](../../../Design/mvp-03-theo-doi-lop.png)

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-041, D-GEO-042, D-GEO-043).

<b style="color:#C00000">Question: Đúng giờ đặt thì mở; đã mở rồi thì không khóa lại và không đổi giờ nữa; bài chưa đặt giờ thì lời giải vẫn khóa. Có đúng cách thầy cô muốn dùng trong lớp không?</b>

**Our current understanding (default used in the spec):** A

**Options:** A: đúng · B: cho khóa lại lời giải đã mở · C: bài chưa đặt giờ tự mở sau một lúc

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-604, 605, 608, 612 · **Needed by:** R3 của E6

---

<a id="q-gv-04"></a>
### Q-GV-04 · Trợ giúp học sinh được dùng khi giao bài (ref D-GEO-053)

**Screen:** [png](../../../Design/mvp-02-giao-bai.png)

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-053).

<b style="color:#C00000">Question: Khung thiết kế có hai công tắc: "Gợi ý mức 1 và 2" và "Hỏi đáp (không nêu đáp số)". Đủ chưa, hay cần lựa chọn khác (ví dụ giới hạn một mức gợi ý)?</b>

**Our current understanding (default used in the spec):** A

**Options:** A: hai công tắc · B: thêm lựa chọn (nêu)

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-603, 404 · **Needed by:** R3 của E6

---

<a id="q-gv-05"></a>
### Q-GV-05 · Công cụ đo ẩn gì trước giờ mở (ref D-GEO-024)

**Screen:** [png](../../../Design/mvp-05-lam-bai.png)

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-024).

<b style="color:#C00000">Question: Ẩn giá trị đại lượng đề hỏi và mọi đại lượng cùng giá trị, cùng vai trò trong cùng tam giác lời giải (ví dụ góc ASD ẩn như góc SDA); vẫn đo được yếu tố trung gian. Đủ và đúng không?</b>

**Our current understanding (default used in the spec):** A (PO-08, 17, 42)

**Options:** A: đồng ý · B: đổi

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-305 · **Needed by:** R3 của E3

---

<a id="q-gv-06"></a>
### Q-GV-06 · Hỏi đáp trước giờ mở nói được gì (ref D-GEO-027)

**Screen:** không có khung riêng

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-027).

<b style="color:#C00000">Question: Trước giờ mở, trợ lý nêu công thức và phương pháp tổng quát, dẫn tới bài giảng; không nêu đáp số hay các bước của bài đang làm; khi từ chối thì nhắc lại gợi ý đang có, không tính thêm lượt gợi ý. Đồng ý không?</b>

**Our current understanding (default used in the spec):** A (PO-22, 37, 44)

**Options:** A: đồng ý · B: đổi

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-609, 607, 411 · **Needed by:** R3 của E6

---

<a id="q-gv-07"></a>
### Q-GV-07 · Bộ lọc Kho đề (ref D-GEO-070)

**Screen:** [png](../../../Design/mvp-01-kho-de.png)

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-070).

<b style="color:#C00000">Question: Lọc theo cấp (THCS, THPT); chủ đề (Góc, Khoảng cách, Thể tích, Diện tích, Mặt cầu theo tài liệu mẫu); loại hình; "dạng bài" hiểu là chủ đề. Có thiếu chủ đề nào không?</b>

**Our current understanding (default used in the spec):** A

**Options:** A: đồng ý · B: thêm chủ đề (nêu)

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-501 · **Needed by:** R3 của E5

---

<a id="q-gv-08"></a>
### Q-GV-08 · Tiến độ và số liệu Theo dõi lớp (ref D-GEO-075, D-GEO-077)

**Screen:** [png](../../../Design/mvp-03-theo-doi-lop.png)

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-075, D-GEO-077).

<b style="color:#C00000">Question: Tiến độ bài là k/3 (Gợi ý 1, Gợi ý 2, Lời giải). Trên Theo dõi lớp: thẻ "12′" tính bằng tổng thời gian cả lớp chia số học sinh; tình trạng gồm Chưa bắt đầu, Đang làm, Đang chờ lời giải, Tự làm không cần gợi ý. Có đúng ý thầy cô không?</b>

**Our current understanding (default used in the spec):** A. Khung thiết kế không ghi định nghĩa

**Options:** A: đồng ý · B: đổi (nêu định nghĩa)

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-606, 701, 702 · **Needed by:** R3 của E7

---

## Not asked any more (superseded by CR-GEO-nn)
- Chưa có.