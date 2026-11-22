# Questions for ChuDauTu — GEO

Feature GEO là MVP web app Hình Học Không Gian Cùng AI: học sinh thao tác hình 3D đúng, được gợi ý trước, chỉ xem lời giải khi giáo viên cho phép; Giáo viên mẫu giao bài và theo dõi lớp; hệ thống phục vụ nhiều trường và 1000 người dùng đồng thời. Anh/chị quyết định phạm vi, ưu tiên, nhà cung cấp AI, định danh học sinh, pháp lý dữ liệu trẻ em và cách hiểu các chỉ số thành công.

**Every question already has a default.** Tài liệu và test case đang dùng nó, nên không có gì bị chặn. Câu trả lời của bạn chỉ xác nhận hoặc đổi mặc định.

**How to answer:** với mỗi câu, đánh dấu **Confirm the default**, hoặc **chọn phương án khác**, hoặc viết ghi chú. Mỗi câu có liên kết tới ảnh màn hình nếu liên quan.

## Assumptions in use (what the documents are built on today)
Đây là các điểm chỉ bạn xác nhận được. Tới khi bạn trả lời, story và test case dùng giả định ở cột 2 và mang nhãn PROVISIONAL.

| Decision | Assumption in use | Why this one (source) | Built on it (stories / AC / TC) | If your answer differs |
|---|---|---|---|---|
| [D-GEO-045](#q-cdt-01) | Đề xuất: p95 đọc ≤ 300 ms, ghi ≤ 500 ms, lỗi < 0,5%; đỉnh 1000 học sinh cùng mở lời giải. Đề xuất, chưa phải ngưỡng chốt | SRC-007 | US-GEO-107; NFR-GEO-nnn; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-045) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-047](#q-cdt-02) | Cổng AI có nhà cung cấp giả lập (nội dung soạn sẵn); thật là bước sau; MVP vẫn có đủ tầng cổng, ghi chi phí, đổi model. Hệ quả: chỉ số thời gian gửi đề tới hình và độ đúng thật chưa đo được trên AI giả lập | SRC-009 | US-GEO-103, 105; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-047) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-048](#q-cdt-03) | Đủ cho MVP: tên hiển thị + mã lớp, không thu email, số điện thoại | SRC-010 | US-GEO-101; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-048) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-050](#q-cdt-04) | A. Hậu quả OUT: quản trị trường, tổ chức nhiều cấp (IAM-04), chia sẻ liên trường vẫn ngoài MVP | SRC-004 và SRC-061; xem C-01 ở `01_understanding.md` | US-GEO-104; `architecture/`; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-050) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-011](#q-cdt-05) | A. MVP chỉ có kho đề và bài giảng mẫu do hệ thống cung cấp; hiển thị trong bản mẫu với nhãn "Concept". Hậu quả OUT: giáo viên không thấy nút soạn hoặc import trong MVP | SRC-033 (PO-32), SRC-072, `MVP.md` §4 | REQ-GEO-52, 53 (OUT); US-GEO-504, 412; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-011) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-035](#q-cdt-06) | A. `MVP.md` §5E và LRN-02 nói "một đề hoặc một bộ đề"; PO-25 (cao hơn) nói MVP giao từng đề. Giao bộ là OUT (chặng 3) | SRC-029 (PO-25), SRC-085, SRC-107 | US-GEO-602; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-035) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-029](#q-cdt-07) | A: nhãn "AI soạn · chờ giáo viên duyệt"; PO-DECISIONS ghi cần chủ đầu tư xác nhận | SRC-039 (PO-43) | US-GEO-411, 412; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-029) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-052](#q-cdt-08) | A; học sinh chưa có mã lớp ngoài MVP (`DOMAIN-PROFILE.md` §13 câu 1 còn mở) | `DOMAIN-PROFILE.md` §13 câu hỏi 1; SRC-095 | US-GEO-401…403; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-052) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-026](#q-cdt-08) | Kế thừa khóa của bài đó; lời giải mở theo giờ giáo viên; bộ câu thử có ca "dán lại đề đang khóa vào tự học". "Gần trùng" đo bằng quy tắc ARCH đề xuất và PO duyệt | SRC-026 (PO-16) | US-GEO-406, 610; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-026) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-058](#q-cdt-09) | Dùng công cụ Chọn, Điểm, Đoạn, Mặt phẳng, Mở rộng, Đo dài, Đo góc; xoay, thu phóng, đặt lại không tính | SRC-108, SRC-119 | US-GEO-703, M-GEO-08; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-058) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-059](#q-cdt-09) | Một lần học sinh mở một bài, kết thúc khi 30 phút không có sự kiện | SRC-108 | M-GEO-08; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-059) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-060](#q-cdt-09) | Trung vị (p50) từ lúc gửi đề tự học tới lúc hình hiện; ngưỡng đề xuất dưới 40 giây; chỉ đo trên AI thật (D-GEO-047) | SRC-062 | NFR-GEO-nnn; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-060) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-023](#q-cdt-10) | A | SRC-022, SRC-052 (PO-07, BR-05); `MVP.md` §10 câu 1 | US-GEO-405; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-023) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-055](#q-cdt-11) | 40 đến 60 đề, ít nhất 40 đề đã kiểm chứng (tính lại bằng engine); người soát nội dung Toán: chưa xác định | SRC-092, SRC-067, SRC-047 | US-GEO-504; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-055) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-020](#q-cdt-12) | Máy: "Đã kiểm chứng" / "Cần xem lại". Người: "AI soạn" / "Giáo viên đã duyệt". Giáo viên không tự gắn nhãn của máy | SRC-019 (PO-02) | US-GEO-203, 502, 411; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-020) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-028](#q-cdt-12) | Mọi lời giải trong kho đề chỉ mang "AI soạn"; nhãn "Giáo viên đã duyệt" chưa dùng vì chưa có giáo viên thật duyệt | SRC-109 (`MVP.md` §10 rủi ro 1); `MVP.md` §1 | US-GEO-502; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-028) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-079](#q-cdt-12) | Dùng đúng chuỗi "Đã kiểm chứng" (PO-02); "Hình đã kiểm chứng" là biến thể của thiết kế, không dùng | SRC-019; `Design/mvp-05` so với `mvp-02` | US-GEO-203, 502; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-079) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-073](#q-cdt-13) | A: một mã truy cập riêng do chủ đầu tư cấp, học sinh không biết; B làm hỏng BR-GEO-03 vì mọi học sinh thành giáo viên. Nguồn chỉ nói "vào vai giáo viên bằng tài khoản mẫu" | `MVP.md` §1 (cách hiểu); SRC-050 | US-GEO-102; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-073) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-030](#q-cdt-14) | Bộ bài giảng và giáo trình mẫu do hệ thống cung cấp (10 khái niệm của prototype làm tham chiếu hành vi); GEO-11 chưa có trong bảng epic của hồ sơ domain §14 nên PO xếp vào E4 | SRC-042 (PO-49), SRC-083, SRC-116 | US-GEO-409, 411, 412; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-030) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-078](#q-cdt-15) | A (M4: câu hỏi mở trên điều khiển đã vẽ là "không xây gì phía sau"). Lý do: Trình chiếu là GEO-13 chặng 2; Sửa lời giải là KNW-03 chặng 2; "Sau buổi học tới" cần thời khóa biểu (PO-26); "Nên nhắc lại" là tổng hợp lỗi sai COP-02 chặng 5; thẻ chuỗi ngày và phần trăm theo dạng không có chức năng nào trong MVP (PO-04, PO-09 nói không "số ngày học liên tiếp" với phụ huynh; chưa có nguồn cho học sinh). "Lịch sử": chỉ nhật ký hỏi đáp lọc theo bài có nguồn (SRC-105) | `Design/mvp-02`, `mvp-03`, `mvp-04`; SRC-018, SRC-021, SRC-072 | US-GEO-503, 602, 604, 606, 701, 410; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-078) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-067](#q-cdt-16) | MVP chỉ thu tên hiển thị và mã lớp; không gửi tên hiển thị cho nhà cung cấp mô hình; chính sách lưu giữ, xóa, xuất, đồng thuận: chưa xác định; AC không viết hành vi xóa hay xuất dữ liệu | SRC-015, SRC-065 | US-GEO-101, 103; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-067) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-021](#q-cdt-17) | Không tự duyệt; MVP không có chấm điểm; nguyên tắc giữ cho mọi chặng | SRC-020 (PO-03) | REQ-GEO-34; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-021) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-022](#q-cdt-17) | Không có xếp hạng ở mọi cấp; Theo dõi lớp liệt kê, không sắp theo điểm hay mức nắm | SRC-021 (PO-04) | US-GEO-701, 702; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-022) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-025](#q-cdt-18) | Lớp 11A1 (THPT) và 9B (THCS); học sinh An, Bình, Chi, Dũng, Hà; "Giáo viên Toán · 11A1"; không tên thật | SRC-025 (PO-14) | US-GEO-601; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-025) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-033](#q-cdt-18) | Chỉ web tới hết chặng 4 | SRC-043 (PO-12) | US-GEO-306; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-033) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-034](#q-cdt-18) | MVP chỉ Giáo viên mẫu; giáo viên thật vào bằng lời mời ở chặng 2 | SRC-044 (PO-05) | US-GEO-102; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-034) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |

## Terms used
- Giờ mở lời giải: thời điểm giáo viên đặt, từ đó học sinh được xem lời giải.
- Nhãn máy: "Đã kiểm chứng" / "Cần xem lại"; nhãn người: "AI soạn" / "Giáo viên đã duyệt".
- Tự học: học sinh làm đề tự nhập, không có giáo viên giao.
- PROVISIONAL: tài liệu và test đang chạy theo mặc định, chưa được xác nhận.

| # | Topic | Ref | Screen | Needed by | Answer |
|---|---|---|---|---|---|
| [Q-CDT-01](#q-cdt-01) | Ngưỡng hiệu năng | D-GEO-045 | — | R3 của E1 | |
| [Q-CDT-02](#q-cdt-02) | Nhà cung cấp AI | D-GEO-047 | — | R3 của E1 | |
| [Q-CDT-03](#q-cdt-03) | Định danh học sinh | D-GEO-048 | — | R3 của E1 | |
| [Q-CDT-04](#q-cdt-04) | "Đa trường" trong MVP nghĩa là gì | D-GEO-050 | — | R3 của E1 | |
| [Q-CDT-05](#q-cdt-05) | Soạn giáo án, import, giáo trình riêng | D-GEO-011 | — | R3 của E1 | |
| [Q-CDT-06](#q-cdt-06) | Giao từng đề hay giao bộ đề | D-GEO-035 | [png](../../../Design/mvp-02-giao-bai.png) | R3 của E6 | |
| [Q-CDT-07](#q-cdt-07) | Bài giảng do AI viết, giáo viên chưa duyệt | D-GEO-029 | — | R3 của E4 | |
| [Q-CDT-08](#q-cdt-08) | Tự học: mã lớp và đề trùng bài đang khóa | D-GEO-052, D-GEO-026 | [png](../../../Design/mvp-07-tu-hoc.png) | R3 của E4 | |
| [Q-CDT-09](#q-cdt-09) | Cách đo chỉ số sử dụng | D-GEO-058, D-GEO-059, D-GEO-060 | — | R3 của E7 | |
| [Q-CDT-10](#q-cdt-10) | Khi nào mở lời giải bài tự học | D-GEO-023 | — | R3 của E4 | |
| [Q-CDT-11](#q-cdt-11) | Kho đề: số lượng và người soát | D-GEO-055 | [png](../../../Design/mvp-01-kho-de.png) | R3 của E5 | |
| [Q-CDT-12](#q-cdt-12) | Nhãn kiểm chứng và nhãn người | D-GEO-020, D-GEO-028, D-GEO-079 | [png](../../../Design/mvp-05-lam-bai.png) | R3 của E2 | |
| [Q-CDT-13](#q-cdt-13) | Cách vào của Giáo viên mẫu | D-GEO-073 | — | R3 của E1 | |
| [Q-CDT-14](#q-cdt-14) | Bài giảng, từ khóa, hỏi đáp có nguồn trong MVP | D-GEO-030 | — | R3 của E4 | |
| [Q-CDT-15](#q-cdt-15) | Điều khiển trên khung thiết kế chưa có nguồn | D-GEO-078 | [png](../../../Design/mvp-03-theo-doi-lop.png) | R3 của E6 | |
| [Q-CDT-16](#q-cdt-16) | Dữ liệu trẻ em và pháp lý | D-GEO-067 | — | R3 của E1 | |
| [Q-CDT-17](#q-cdt-17) | Hai nguyên tắc giữ cho mọi chặng | D-GEO-021, D-GEO-022 | — | R3 của E7 | |
| [Q-CDT-18](#q-cdt-18) | Ba giả định nhỏ | D-GEO-025, D-GEO-033, D-GEO-034 | — | R3 của E6 | |

---

<a id="q-cdt-01"></a>
### Q-CDT-01 · Ngưỡng hiệu năng (ref D-GEO-045)

**Screen:** không có khung riêng

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-045).

<b style="color:#C00000">Question: Các ngưỡng p95 đọc ≤ 300 ms, ghi ≤ 500 ms, lỗi < 0,5%, đỉnh 1000 học sinh cùng mở lời giải có đúng ý anh/chị không, hay đổi?</b>

**Our current understanding (default used in the spec):** A. Đề xuất, chưa phải ngưỡng chốt

**Options:** A: giữ như đề xuất · B: đổi (ghi số)

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-107; NFR-GEO-nnn · **Needed by:** R3 của E1

---

<a id="q-cdt-02"></a>
### Q-CDT-02 · Nhà cung cấp AI (ref D-GEO-047)

**Screen:** không có khung riêng

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-047).

<b style="color:#C00000">Question: MVP dùng nhà cung cấp AI giả lập (nội dung soạn sẵn) thì có chấp nhận được không, và nhà cung cấp thật chọn khi nào?</b>

**Our current understanding (default used in the spec):** A. Chỉ số tốc độ và độ đúng thật chưa đo được

**Options:** A: giả lập trước, thật là bước sau · B: dùng thật ngay (nêu nhà cung cấp)

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-103, 105 · **Needed by:** R3 của E1

---

<a id="q-cdt-03"></a>
### Q-CDT-03 · Định danh học sinh (ref D-GEO-048)

**Screen:** không có khung riêng

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-048).

<b style="color:#C00000">Question: Đăng nhập nhẹ bằng tên hiển thị và mã lớp có đủ cho MVP không (ai biết mã lớp đều vào được lớp)?</b>

**Our current understanding (default used in the spec):** A. Không thu email hay số điện thoại

**Options:** A: đủ cho MVP · B: thêm một lớp bảo vệ (nêu cách)

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-101 · **Needed by:** R3 của E1

---

<a id="q-cdt-04"></a>
### Q-CDT-04 · "Đa trường" trong MVP nghĩa là gì (ref D-GEO-050)

**Screen:** không có khung riêng

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-050).

<b style="color:#C00000">Question: Hồ sơ cũ để trống `org_id` còn anh/chị yêu cầu đa trường từ đầu: trong MVP chỉ cần cách ly dữ liệu theo trường, không có màn quản trị trường, đúng không?</b>

**Our current understanding (default used in the spec):** A

**Options:** A: cách ly dữ liệu bắt buộc, không màn quản trị trường · B: để trống như hồ sơ cũ

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-104; `architecture/` · **Needed by:** R3 của E1

---

<a id="q-cdt-05"></a>
### Q-CDT-05 · Soạn giáo án, import, giáo trình riêng (ref D-GEO-011)

**Screen:** không có khung riêng

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-011).

<b style="color:#C00000">Question: Các yêu cầu soạn với AI, import file và ảnh, chat sửa bản nháp, giáo trình riêng cho từng học sinh (đều Final) nằm ngoài MVP theo PO-32. Có giữ như vậy không?</b>

**Our current understanding (default used in the spec):** A. Giáo viên không có nút soạn hay import trong MVP

**Options:** A: ngoài MVP (chặng 2, 5, 6) · B: kéo một phần vào MVP (nêu phần nào)

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** REQ-GEO-52, 53; US-GEO-504, 412 · **Needed by:** R3 của E1

---

<a id="q-cdt-06"></a>
### Q-CDT-06 · Giao từng đề hay giao bộ đề (ref D-GEO-035)

**Screen:** [png](../../../Design/mvp-02-giao-bai.png)

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-035).

<b style="color:#C00000">Question: `MVP.md` ghi "một đề hoặc một bộ đề", PO-25 ghi MVP giao từng đề. MVP giao từng đề được không?</b>

**Our current understanding (default used in the spec):** A

**Options:** A: từng đề · B: cả bộ đề

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-602 · **Needed by:** R3 của E6

---

<a id="q-cdt-07"></a>
### Q-CDT-07 · Bài giảng do AI viết, giáo viên chưa duyệt (ref D-GEO-029)

**Screen:** không có khung riêng

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-029).

<b style="color:#C00000">Question: Bài giảng AI viết chưa duyệt có hiện cho học sinh (kèm nhãn "AI soạn · chờ giáo viên duyệt") không? Điểm này chạm nguyên tắc chỉ nội dung đã duyệt tới học sinh.</b>

**Our current understanding (default used in the spec):** A (PO-43)

**Options:** A: hiện kèm nhãn · B: chỉ hiện sau khi giáo viên duyệt

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-411, 412 · **Needed by:** R3 của E4

---

<a id="q-cdt-08"></a>
### Q-CDT-08 · Tự học: mã lớp và đề trùng bài đang khóa (ref D-GEO-052, D-GEO-026)

**Screen:** [png](../../../Design/mvp-07-tu-hoc.png)

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-052, D-GEO-026).

<b style="color:#C00000">Question: Học sinh tự học phải đã vào bằng mã lớp; đề dán trùng bài đang khóa thì kế thừa khóa của bài đó. Đúng ý không?</b>

**Our current understanding (default used in the spec):** A

**Options:** A: đúng cả hai · B: tự học cho cả người chưa có mã lớp · C: không kế thừa khóa

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-401…403, 405, 406 · **Needed by:** R3 của E4

---

<a id="q-cdt-09"></a>
### Q-CDT-09 · Cách đo chỉ số sử dụng (ref D-GEO-058, D-GEO-059, D-GEO-060)

**Screen:** không có khung riêng

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-058, D-GEO-059, D-GEO-060).

<b style="color:#C00000">Question: "Thao tác trên hình" chỉ tính dùng công cụ (không tính xoay, thu phóng); "phiên làm bài" là một lần mở một bài, hết sau 30 phút không hoạt động; thời gian gửi đề tới có hình đo trung vị trên AI thật. Có đồng ý không?</b>

**Our current understanding (default used in the spec):** A

**Options:** A: đồng ý · B: đổi (nêu định nghĩa)

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-703; M-GEO-08, 09 · **Needed by:** R3 của E7

---

<a id="q-cdt-10"></a>
### Q-CDT-10 · Khi nào mở lời giải bài tự học (ref D-GEO-023)

**Screen:** không có khung riêng

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-023).

<b style="color:#C00000">Question: Bài tự học mở lời giải sau khi học sinh xem Gợi ý 1 và 2. Giữ không?</b>

**Our current understanding (default used in the spec):** A

**Options:** A: sau Gợi ý 1 và 2 · B: sau khoảng chờ · C: không mở nếu thuộc lớp

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-405 · **Needed by:** R3 của E4

---

<a id="q-cdt-11"></a>
### Q-CDT-11 · Kho đề: số lượng và người soát (ref D-GEO-055)

**Screen:** [png](../../../Design/mvp-01-kho-de.png)

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-055).

<b style="color:#C00000">Question: Kho 40 đến 60 đề, ít nhất 40 đề đã kiểm chứng; ai soát nội dung Toán trước khi học sinh thật dùng, và có nguồn đề ưu tiên (sách giáo khoa, đề thi) không?</b>

**Our current understanding (default used in the spec):** Chưa có người soát; chỉ nhãn "AI soạn"

**Options:** Nêu người và nguồn

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-504 · **Needed by:** R3 của E5

---

<a id="q-cdt-12"></a>
### Q-CDT-12 · Nhãn kiểm chứng và nhãn người (ref D-GEO-020, D-GEO-028, D-GEO-079)

**Screen:** [png](../../../Design/mvp-05-lam-bai.png)

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-020, D-GEO-028, D-GEO-079).

<b style="color:#C00000">Question: Dùng hai trục nhãn (máy: "Đã kiểm chứng" / "Cần xem lại"; người: "AI soạn" / "Giáo viên đã duyệt"); MVP chỉ có "AI soạn"; thiết kế ghi lẫn "Hình đã kiểm chứng", dùng một chuỗi "Đã kiểm chứng". Đồng ý không?</b>

**Our current understanding (default used in the spec):** A

**Options:** A: đồng ý · B: đổi

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-203, 502 · **Needed by:** R3 của E2

---

<a id="q-cdt-13"></a>
### Q-CDT-13 · Cách vào của Giáo viên mẫu (ref D-GEO-073)

**Screen:** không có khung riêng

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-073).

<b style="color:#C00000">Question: Nguồn chỉ nói "vào vai giáo viên bằng tài khoản mẫu". Nếu chọn vai không mật khẩu thì học sinh nào cũng thành giáo viên và đổi được giờ mở. Dùng một mã riêng do anh/chị cấp được không?</b>

**Our current understanding (default used in the spec):** A. PROVISIONAL

**Options:** A: mã giáo viên mẫu riêng · B: chọn vai không mật khẩu (chấp nhận rủi ro)

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-102 · **Needed by:** R3 của E1

---

<a id="q-cdt-14"></a>
### Q-CDT-14 · Bài giảng, từ khóa, hỏi đáp có nguồn trong MVP (ref D-GEO-030)

**Screen:** không có khung riêng

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-030).

<b style="color:#C00000">Question: PO-49 đặt chúng trong MVP ở mức bài giảng và giáo trình mẫu do hệ thống cung cấp; hồ sơ domain không xếp chức năng này vào epic nào nên PO xếp vào E4. Đồng ý không?</b>

**Our current understanding (default used in the spec):** A

**Options:** A: E4, bộ 10 khái niệm làm tham chiếu · B: tách epic riêng

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-409, 411, 412 · **Needed by:** R3 của E4

---

<a id="q-cdt-15"></a>
### Q-CDT-15 · Điều khiển trên khung thiết kế chưa có nguồn (ref D-GEO-078)

**Screen:** [png](../../../Design/mvp-03-theo-doi-lop.png)

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-078).

<b style="color:#C00000">Question: Khung vẽ "Trình chiếu", "Sửa (sắp có)", "Sau buổi học tới", "Nên nhắc lại trên lớp", thẻ chuỗi ngày, phần trăm theo dạng (có "Thiết diện"), mục "Lịch sử". PO đề xuất không xây phía sau, ẩn hoặc gắn nhãn Concept. Đồng ý không?</b>

**Our current understanding (default used in the spec):** A

**Options:** A: không xây, ẩn hoặc nhãn Concept · B: xây (nêu cái nào, nguồn yêu cầu)

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-503, 602, 604, 606, 701, 410 · **Needed by:** R3 của E6

---

<a id="q-cdt-16"></a>
### Q-CDT-16 · Dữ liệu trẻ em và pháp lý (ref D-GEO-067)

**Screen:** không có khung riêng

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-067).

<b style="color:#C00000">Question: Pháp nhân vận hành, quy định áp dụng, nơi lưu, chính sách lưu giữ, xóa, đồng thuận đều chưa xác định. MVP chỉ thu tên hiển thị và mã lớp và không viết hành vi xóa hay xuất dữ liệu. Có đúng không?</b>

**Our current understanding (default used in the spec):** A. TBD — owner ChuDauTu

**Options:** A: đúng · B: cần hành vi xóa hoặc xuất (nêu)

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-101, 103 · **Needed by:** R3 của E1

---

<a id="q-cdt-17"></a>
### Q-CDT-17 · Hai nguyên tắc giữ cho mọi chặng (ref D-GEO-021, D-GEO-022)

**Screen:** không có khung riêng

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-021, D-GEO-022).

<b style="color:#C00000">Question: Điểm AI luôn là đề xuất, không tự duyệt; không xếp hạng học sinh, lớp, giáo viên, trường. Xác nhận.</b>

**Our current understanding (default used in the spec):** A (PO-03, PO-04)

**Options:** A: xác nhận · B: đổi

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-701, 702 · **Needed by:** R3 của E7

---

<a id="q-cdt-18"></a>
### Q-CDT-18 · Ba giả định nhỏ (ref D-GEO-025, D-GEO-033, D-GEO-034)

**Screen:** không có khung riêng

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-025, D-GEO-033, D-GEO-034).

<b style="color:#C00000">Question: Dữ liệu mẫu (lớp 11A1, 9B; học sinh An, Bình, Chi, Dũng, Hà); chạy web trên trình duyệt tới hết chặng 4; giáo viên thật vào bằng lời mời ở chặng 2. Xác nhận.</b>

**Our current understanding (default used in the spec):** A (PO-14, PO-12, PO-05)

**Options:** A: xác nhận · B: đổi

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-601, 306, 102 · **Needed by:** R3 của E6

---

## Not asked any more (superseded by CR-GEO-nn)
- Chưa có.