# Questions for TechLead — GEO

Feature GEO là MVP web app Hình Học Không Gian Cùng AI, máy chủ Java, đa trường, 1000 người dùng đồng thời. Anh/chị quyết định cách làm kỹ thuật: hạ tầng, dữ liệu thử tải, cách đo tải, dữ liệu mẫu ở tầng dữ liệu, phiên bản Java và thiết bị tối thiểu; không đổi phạm vi.

**Every question already has a default.** Tài liệu và test case đang dùng nó, nên không có gì bị chặn. Câu trả lời của bạn chỉ xác nhận hoặc đổi mặc định.

**How to answer:** với mỗi câu, đánh dấu **Confirm the default**, hoặc **chọn phương án khác**, hoặc viết ghi chú. Mỗi câu có liên kết tới ảnh màn hình nếu liên quan.

## Assumptions in use (what the documents are built on today)
Đây là các điểm chỉ bạn xác nhận được. Tới khi bạn trả lời, story và test case dùng giả định ở cột 2 và mang nhãn PROVISIONAL.

| Decision | Assumption in use | Why this one (source) | Built on it (stories / AC / TC) | If your answer differs |
|---|---|---|---|---|
| [D-GEO-046](#q-tl-01) | PostgreSQL cổng 54329 và Redis cổng 6389 cục bộ; chưa chọn đám mây | SRC-008 | `app/`, `architecture/`; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-046) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-049](#q-tl-02) | 50 trường, 600 lớp, 20.000 học sinh, 2 triệu sự kiện | SRC-011 | US-GEO-107, 108; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-049) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-044](#q-tl-03) | Phiên đang hoạt động = có ít nhất một yêu cầu trong 60 giây gần nhất; cửa sổ đỉnh mở lời giải = 10 giây; công thức ở `02_domain_model.md` M-GEO-10…12 | SRC-007 (brief §6 câu 1); 60 và 10 là PO đặt | US-GEO-107; `architecture/`; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-044) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-054](#q-tl-04) | Giáo viên mẫu và hai lớp mẫu thuộc một trường mẫu "THPT Mẫu A" (cấp là thuộc tính của lớp); để kiểm cách ly, bộ dữ liệu thử có thêm trường "THCS Mẫu C" với lớp và học sinh riêng | SRC-025, SRC-096 | US-GEO-601, 104; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-054) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-051](#q-tl-04) | A (từ mã lớp suy ra trường khi đăng nhập nhẹ) | SRC-010, SRC-004 | US-GEO-101; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-051) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-076](#q-tl-04) | A. Kho đề và kho bài giảng mẫu do Giáo viên mẫu sở hữu (BR-GEO-10) là nội dung hệ thống, mọi trường đọc được, chỉ hệ thống ghi. Bài giao, sự kiện, đề tự nhập, câu hỏi của học sinh luôn thuộc đúng một trường | SRC-057 (BR-10), SRC-004 | US-GEO-104, 504, 412; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-076) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-068](#q-tl-05) | Java 26 cài sẵn; khung, công cụ build, thư viện: TBD — owner TechLead; không ảnh hưởng AC | SRC-013 | `app/`; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-068) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-072](#q-tl-06) | Thông báo dễ hiểu, cho thử lại, không hiện nội dung dở dang, không đổi nhãn; thời gian chờ tối đa: TBD — owner TechLead | SRC-062, SRC-066 | US-GEO-103, 401, 402, 409; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-072) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |
| [D-GEO-074](#q-tl-06) | Trang đang mở tự cập nhật trạng thái khóa không muộn hơn 10 giây sau `release_at`, không cần tải lại hay đăng nhập lại; cơ chế (làm mới định kỳ hay máy chủ đẩy) do ARCH đề xuất | SRC-069 (ca "học sinh đang mở bài đúng lúc tới giờ"); 10 giây là PO đặt | US-GEO-612; AC và TC chưa viết (BA, QC đánh dấu PROVISIONAL cùng D-GEO-074) | story và AC dùng giá trị đó viết lại; chỉ phần bị ảnh hưởng chạy lại (Q7) |

> Cập nhật 27/11/2026 (PO, goal run): sáu câu đã có trả lời PO ở cột Answer; điểm cần TechLead thật: nơi vận hành prod (Q-TL-01) và iOS tối thiểu (Q-TL-05).

## Terms used
- Phiên đang hoạt động: có ít nhất một yêu cầu trong 60 giây gần nhất.
- `school_id`: định danh trường; cách ly dữ liệu theo trường.
- Nội dung hệ thống: kho đề và bài giảng mẫu, dùng chung mọi trường, chỉ đọc.
- Cổng AI: lớp phía máy chủ gọi nhà cung cấp mô hình, giấu khóa, ghi chi phí.

| # | Topic | Ref | Screen | Needed by | Answer |
|---|---|---|---|---|---|
| [Q-TL-01](#q-tl-01) | Hạ tầng chạy thử và vận hành | D-GEO-046 | — | R3 của E1 | PO 27/11/2026: Confirm A (PO-84); nơi vận hành thật chờ TechLead/owner khi lên prod |
| [Q-TL-02](#q-tl-02) | Dữ liệu thử tải | D-GEO-049 | — | R3 của E1 | PO 27/11/2026: Confirm the default (PO-84); bộ đo `bench/` đã chạy cỡ này |
| [Q-TL-03](#q-tl-03) | "Đang hoạt động" và cửa sổ đỉnh | D-GEO-044 | — | R3 của E1 | PO 27/11/2026: Confirm the default (PO-84) |
| [Q-TL-04](#q-tl-04) | Dữ liệu mẫu, mã lớp, nội dung dùng chung | D-GEO-054, D-GEO-051, D-GEO-076 | — | R3 của E6 | PO 27/11/2026: Confirm the default (PO-84) |
| [Q-TL-05](#q-tl-05) | Java, khung và thiết bị tối thiểu | D-GEO-068 | — | R3 của E1 | PO 27/11/2026: chốt theo code, Java 26 · Spring Boot 4.1.2 · Maven (PO-77); thiết bị kiểm: Chrome desktop, iPad gen 7, iPhone 13, Pixel 7; iOS tối thiểu chờ TechLead |
| [Q-TL-06](#q-tl-06) | Cổng AI lỗi và độ trễ khi tới giờ mở | D-GEO-072, D-GEO-074 | — | R3 của E4 | PO 27/11/2026: chốt 10 giây dev/test, 90 giây prod, 45 giây một lần gọi (PO-78); trễ khóa ≤ 10 giây giữ mặc định (PO-84) |

---

<a id="q-tl-01"></a>
### Q-TL-01 · Hạ tầng chạy thử và vận hành (ref D-GEO-046)

**Screen:** không có khung riêng

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-046).

<b style="color:#C00000">Question: PostgreSQL (cổng 54329) và Redis (cổng 6389) cục bộ làm nền; môi trường chạy thử và vận hành nơi nào?</b>

**Our current understanding (default used in the spec):** A

**Options:** A: giữ cục bộ cho MVP · B: nêu nơi đặt

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** `architecture/`; `app/` · **Needed by:** R3 của E1

---

<a id="q-tl-02"></a>
### Q-TL-02 · Dữ liệu thử tải (ref D-GEO-049)

**Screen:** không có khung riêng

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-049).

<b style="color:#C00000">Question: Quy mô 50 trường, 600 lớp, 20.000 học sinh, 2 triệu sự kiện có đủ không? Nơi chạy và công cụ thử tải?</b>

**Our current understanding (default used in the spec):** A

**Options:** A: giữ quy mô · B: đổi

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-107, 108 · **Needed by:** R3 của E1

---

<a id="q-tl-03"></a>
### Q-TL-03 · "Đang hoạt động" và cửa sổ đỉnh (ref D-GEO-044)

**Screen:** không có khung riêng

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-044).

<b style="color:#C00000">Question: Phiên đang hoạt động là có yêu cầu trong 60 giây gần nhất; cửa sổ đỉnh mở lời giải 10 giây (1000 / 10 = 100 yêu cầu/giây). Đúng cách đo không?</b>

**Our current understanding (default used in the spec):** A

**Options:** A: đồng ý · B: đổi cửa sổ

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-107; M-GEO-10…12 · **Needed by:** R3 của E1

---

<a id="q-tl-04"></a>
### Q-TL-04 · Dữ liệu mẫu, mã lớp, nội dung dùng chung (ref D-GEO-054, D-GEO-051, D-GEO-076)

**Screen:** không có khung riêng

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-054, D-GEO-051, D-GEO-076).

<b style="color:#C00000">Question: Mẫu có một trường "THPT Mẫu A" chứa hai lớp (cấp là thuộc tính lớp), dữ liệu thử có thêm "THCS Mẫu C"; mã lớp duy nhất toàn hệ thống; kho đề và bài giảng mẫu là nội dung hệ thống dùng chung, đọc được từ mọi trường. Khả thi không?</b>

**Our current understanding (default used in the spec):** A

**Options:** A: đồng ý · B: đổi

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-104, 601, 504, 412, 101 · **Needed by:** R3 của E6

---

<a id="q-tl-05"></a>
### Q-TL-05 · Java, khung và thiết bị tối thiểu (ref D-GEO-068)

**Screen:** không có khung riêng

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-068).

<b style="color:#C00000">Question: Java 26 cài sẵn; khung, công cụ build, thư viện chọn gì? Phiên bản iOS và trình duyệt tối thiểu cho iPad, iPhone?</b>

**Our current understanding (default used in the spec):** Chưa chọn; không ảnh hưởng AC

**Options:** Nêu lựa chọn

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** `app/`; US-GEO-306 · **Needed by:** R3 của E1

---

<a id="q-tl-06"></a>
### Q-TL-06 · Cổng AI lỗi và độ trễ khi tới giờ mở (ref D-GEO-072, D-GEO-074)

**Screen:** không có khung riêng

**Context:** Nguồn chưa trả lời điểm này; PO đặt mặc định để công việc không dừng. Chi tiết và nguồn: `work/03_decisions.md` (D-GEO-072, D-GEO-074).

<b style="color:#C00000">Question: Khi cổng AI lỗi hoặc quá thời gian: báo lỗi dễ hiểu, cho thử lại (thời gian chờ tối đa?). Trang đang mở cập nhật khóa không muộn hơn 10 giây sau giờ mở. Đồng ý và cơ chế nào?</b>

**Our current understanding (default used in the spec):** A

**Options:** A: đồng ý · B: đổi (nêu số)

**Please reply:** ☐ Confirm the default · ☐ Choose option ___ · ☐ Other / comment: ____

**Affects:** US-GEO-103, 401, 409, 612 · **Needed by:** R3 của E4

---

## Not asked any more (superseded by CR-GEO-nn)
- Chưa có.