# Domain model and metric definitions (F1) — GEO

> Nền cho story: mỗi con số hiển thị hoặc đo có đúng một công thức ở đây (M-GEO-nn). Chủ sở hữu: PO (ý nghĩa nghiệp vụ); ARCH (lược đồ lưu trữ ở `architecture/`). Nguồn mỗi dòng ở `00_sources.md`; giá trị PO đặt thêm trỏ về `03_decisions.md`. Mọi ngưỡng là đề xuất, chưa có số đo thực tế. Loại feature: hệ thống có trạng thái và sự kiện, có máy chủ và nhiều thiết bị (method §9.1), nên F1 gồm thực thể, trạng thái, công thức và định dạng.

## 1. Thực thể và trường

| Thực thể | Ý nghĩa | Trường chính | Nguồn dữ liệu / nguồn tài liệu |
|---|---|---|---|
| `School` | trường, đơn vị cách ly dữ liệu | `school_id`, tên, `schema_version` | SRC-004; D-GEO-012, D-GEO-050 |
| `User` | giáo viên hoặc học sinh | `user_id`, `school_id`, vai (`teacher` / `student`), tên hiển thị, `owner_id` | SRC-095, SRC-096 |
| `Class` | lớp mẫu | `class_id`, `school_id`, tên, cấp (THCS / THPT), mã lớp, `teacher_id` | SRC-096; D-GEO-051 |
| `Problem` | một bài toán | `problem_id`, đề, cấp, chủ đề, nguồn (`kho` / `tu_nhap`), `owner_id`, `school_id` (đề tự nhập) hoặc nội dung hệ thống (kho đề, D-GEO-076) | DOMAIN §9 |
| `SceneSpec` | hình do engine dựng | `problem_id`, version, ràng buộc, tọa độ đã tính, nguồn tọa độ (`engine` / `ai`) | DOMAIN §9; SRC-053, SRC-055 |
| `Solution` | lời giải, gợi ý, đáp số | `problem_id`, version, các bước (mỗi bước có đối tượng tô màu, đường vẽ thêm, "Vì sao nghĩ ra bước này?"), Gợi ý 1, Gợi ý 2, đáp số, `model`, `prompt_version`, `author_id`, cờ "AI soạn" | SRC-110, SRC-111; materials |
| `VerificationReport` | kết quả kiểm chứng | `solution_id`, kết quả từng phép kiểm, trạng thái, quyết định nhãn máy | SRC-076 |
| `Assignment` | đề giao cho lớp | `assignment_id`, `class_id`, `problem_id`, `solution_version`, `release_at`, trợ giúp cho phép (D-GEO-053), `school_id` | SRC-112; D-GEO-035 |
| `Interaction` | sự kiện của học sinh | `user_id`, `school_id`, `assignment_id` hoặc `problem_id`, loại (`open`, `tool`, `hint`, `ask`, `unlock`), chi tiết, thời điểm | SRC-094 |
| `Lecture` | bài giảng mẫu của một khái niệm | `lecture_id`, khái niệm, nội dung, nguồn ("Giáo án mẫu" / "AI soạn"), nhãn người | SRC-116, SRC-035; D-GEO-029, D-GEO-030 |
| `AiCall` | một lượt gọi AI | `call_id`, `school_id`, mục đích, `model`, `prompt_version`, chi phí, trạng thái, thời gian | SRC-099 |

Mọi bản ghi có `owner_id`, `school_id` (trừ nội dung hệ thống) và `schema_version`. Lời giải đã giao không sửa tại chỗ (BR-GEO-11).

## 2. Trạng thái và quy tắc dẫn xuất

**Vòng đời lời giải:** sinh ra → đã kiểm chứng hoặc cần xem lại → vào kho → đã giao (khóa) → đã mở (SRC `DOMAIN-PROFILE.md` §9).

**Trạng thái giờ mở của bài giao** (M-GEO-13). Đầu vào: `release_at` (có thể rỗng) và giờ máy chủ `t`.

| `release_at` | Điều kiện | Trạng thái | Lời giải |
|---|---|---|---|
| rỗng | — | Chưa đặt giờ | khóa |
| có giá trị R | t < R | Sắp mở | khóa |
| có giá trị R | t ≥ R | Đã mở | được xem (D-GEO-041) |

Chuyển trạng thái: Chưa đặt giờ → Sắp mở (giáo viên đặt giờ) · Chưa đặt giờ, Sắp mở → Đã mở (mở ngay: R := t lúc bấm) · Sắp mở → Sắp mở (đổi giờ sang R′ > t) · Sắp mở → Chưa đặt giờ: không có trong nguồn, không làm · Đã mở → không đổi nữa (D-GEO-042). Chỉ giáo viên của lớp được đặt, đổi, mở ngay (BR-GEO-03).

**Ví dụ quy tắc giờ mở** (T0 là giờ máy chủ lúc giao bài; mốc là tương đối, không phải lịch):

| # | Tình huống | Quan sát |
|---|---|---|
| 1 | giao bài, chưa đặt giờ; học sinh yêu cầu lời giải tại T0 + 1 giờ | Chưa đặt giờ → khóa; máy chủ không trả bước, không trả đáp số |
| 2 | đặt R = T0 + 60 giây; yêu cầu tại T0 + 59 giây | Sắp mở → khóa |
| 3 | cùng R; yêu cầu tại T0 + 60 giây | Đã mở → trả lời giải |
| 4 | đang Sắp mở với R = T0 + 60 giây; giáo viên đổi thành R′ = T0 + 120 giây tại T0 + 30 giây; yêu cầu tại T0 + 60 giây | vẫn khóa; mở tại T0 + 120 giây |
| 5 | giáo viên bấm Mở ngay tại T0 + 10 giây | R := T0 + 10 giây; yêu cầu từ T0 + 10 giây được trả lời giải |
| 6 | học sinh đang xem bài khi giờ tới R | trong 10 giây sau R trang hiện lời giải, không cần tải lại (D-GEO-074) |
| 7 | đã Đã mở; giáo viên đổi giờ sang tương lai | bị từ chối; lời giải đã mở không khóa lại (D-GEO-042) |
| 8 | máy khách báo giờ máy của nó đã tới R nhưng giờ máy chủ chưa tới R | khóa; chỉ giờ máy chủ có giá trị |

**Trạng thái nhãn của đề** (M-GEO-02, 03, 05; D-GEO-061):

| Hình do engine dựng thỏa ràng buộc | Đáp số khớp | Nguồn tọa độ | Nhãn máy |
|---|---|---|---|
| có | có | engine | Đã kiểm chứng |
| có | không | engine | Cần xem lại |
| không | bất kỳ | engine | Cần xem lại |
| không áp dụng (đề lạ) | không kiểm chứng được bằng engine | AI | Cần xem lại, kèm cảnh báo chưa kiểm chứng |

Nhãn người luôn là "AI soạn" trong MVP (D-GEO-028). Engine chỉ kiểm chứng phần tính toán, không kiểm chứng lập luận chứng minh (BR-GEO-09), nên "Đã kiểm chứng" không có nghĩa giáo viên đã duyệt lập luận.

**Hiển thị lời giải theo thang gợi ý:** bài giao: Gợi ý 1 → Gợi ý 2 → Lời giải (Lời giải chỉ khi Đã mở). Bài tự học: Lời giải chỉ sau khi học sinh đã xem Gợi ý 1 và Gợi ý 2 (D-GEO-023), trừ khi đề kế thừa khóa của một bài giao đang khóa (D-GEO-026) thì theo giờ mở của bài giao đó.

## 3. Bảng chỉ số (mỗi con số một công thức)

| Mã | Tên và công thức | Quy tắc bao gồm | Kỳ / ranh giới ngày | Làm tròn, giá trị rỗng | Hiện ở màn |
|---|---|---|---|---|---|
| M-GEO-01 | **Tỉ lệ hình thỏa ràng buộc** = `N_hinh_ok / N_de` trên bộ đề chuẩn | `N_de` = số đề trong bộ đề chuẩn (dùng chung với kho đề); `N_hinh_ok` = số đề có M-GEO-02 đúng. Ngưỡng đề xuất ≥ 95%. Ví dụ N = 40 → cần ≥ 38; N = 60 → cần ≥ 57 | mỗi lần chạy bộ đề chuẩn (kể cả khi đổi model hay prompt) | phần trăm một chữ số thập phân, dấu phẩy; N = 0 → "—" | báo cáo bộ đề chuẩn (không có màn người dùng) |
| M-GEO-02 | **Hình thỏa ràng buộc** (một đề) = với mọi ràng buộc `c` của đề: `sai_so_c ≤ 1e-6`, trên hình quy về cạnh lớn nhất = 1 (D-GEO-040). Ví dụ vuông góc: `|tích vô hướng| / (|u|·|v|) ≤ 1e-6` | ràng buộc lấy từ `SceneSpec`; hình do engine dựng | — | đúng / sai | nhãn trên đề (E2, E5) |
| M-GEO-03 | **Đáp số khớp** (một đề) = với mọi đại lượng đề hỏi `q`, trên mỗi bộ giá trị tham số trong ít nhất 3 bộ (a = 1 và hai giá trị khác): `|q_AI − q_tính| ≤ 1e-4 · max(1, |q_tính|)` (D-GEO-040) | `q_tính` tính từ tọa độ engine; đáp số nhiều giá trị (ví dụ `HKG-04`: Sxq và V) phải khớp từng giá trị | — | đúng / sai | nhãn trên đề |
| M-GEO-04 | **Tỉ lệ đáp số đúng** = `N_dapso_ok / N_de`; `N_dapso_ok` = số đề có M-GEO-03 đúng. Đề xuất ≥ 90%. N = 40 → ≥ 36; N = 60 → ≥ 54 | như M-GEO-01 | như M-GEO-01 | như M-GEO-01 | báo cáo bộ đề chuẩn |
| M-GEO-05 | **Bài sai mang "Đã kiểm chứng"** = số đề có nhãn "Đã kiểm chứng" mà M-GEO-02 hoặc M-GEO-03 sai (hoặc đáp số tham chiếu khác `q_tính`). Phải bằng 0 | tính trên toàn bộ kho đề và bộ đề chuẩn | mỗi lần chạy | số nguyên; không bao giờ rỗng | báo cáo bộ đề chuẩn |
| M-GEO-06 | **Số lần lộ đáp số** = số câu trong bộ câu thử mà câu trả lời hoặc dữ liệu trả về thỏa định nghĩa "lộ đáp số" (D-GEO-065) khi lời giải đang khóa. Phải bằng 0 | mọi kênh: hỏi đáp, xem trước, dữ liệu trả về của bài (BR-GEO-18) | mỗi lần chạy | số nguyên | báo cáo bộ câu thử |
| M-GEO-07 | **Số đề đã kiểm chứng trong kho** = số đề kho có nhãn "Đã kiểm chứng". MVP cần ≥ 40; kho có 40 đến 60 đề | nhãn do engine tính lại, không lấy từ cột có sẵn trong `materials/*.xlsx` (D-GEO-004) | khi nạp kho và mỗi lần đổi engine | số nguyên | Kho đề (đếm) |
| M-GEO-08 | **Tỉ lệ phiên làm bài có thao tác trên hình** = `P_thaotac / P_tong`. `P_tong` = số phiên làm bài (D-GEO-059); `P_thaotac` = số phiên có ít nhất một sự kiện `tool` (D-GEO-058). Đề xuất ≥ 60% | chỉ phiên học sinh trong dữ liệu thật hoặc dữ liệu thử | mỗi kỳ đo; ranh giới ngày theo giờ Việt Nam | phần trăm một chữ số, dấu phẩy; `P_tong` = 0 → "—" | báo cáo đo lường (không có màn người dùng) |
| M-GEO-09 | **Thời gian trung vị gửi đề tới có hình** = trung vị của (`t_hinh_hien − t_gui_de`) trên các đề tự học thành công. Đề xuất < 40 giây (D-GEO-060) | chỉ đo trên AI thật; đề lỗi bị loại và đếm riêng | mỗi kỳ đo | giây, một chữ số thập phân; không có mẫu → "—" | báo cáo đo lường |
| M-GEO-10 | **Phiên đang hoạt động** `S(t)` = số phiên có ít nhất một yêu cầu tới máy chủ trong khoảng (t − 60 giây, t] (D-GEO-044). Mục tiêu tải: S = 1000 | một phiên học sinh hoặc giáo viên | tức thời | số nguyên | thử tải |
| M-GEO-11 | **Tải đều** = `S / T` yêu cầu mỗi giây, T là khoảng giữa hai yêu cầu của một phiên, T trong [3, 5] giây. S = 1000 → từ 200 đến 333,3 yêu cầu/giây (1000 / 5 = 200; 1000 / 3 ≈ 333,3) | giả định brief §6 câu 1 | — | một chữ số thập phân | thử tải |
| M-GEO-12 | **Tải đỉnh mở lời giải** = `S_mo / W`, `S_mo` = số học sinh cùng mở lời giải khi giờ tới = 1000, `W` = cửa sổ đỉnh 10 giây (D-GEO-044) → 100 yêu cầu/giây trên đường đọc lời giải, cộng với tải đều M-GEO-11 nếu xảy ra đồng thời (tối đa 333,3 + 100 = 433,3 yêu cầu/giây) | đường đọc lời giải đã qua kiểm tra khóa | — | một chữ số thập phân | thử tải |
| M-GEO-13 | **Trạng thái giờ mở** — hàm của (`release_at`, t), bảng ở mục 2 | giờ máy chủ | tức thời | 3 trạng thái, không rỗng | Bài của em, Theo dõi lớp, giao bài |
| M-GEO-14 | **Tiến độ trên bài giao** = `k / 3`, k = số mốc học sinh đã đạt trong {Gợi ý 1, Gợi ý 2, Lời giải} (thanh ba đoạn và "Tiến độ của em 2/3" ở `Design/mvp-04`, `mvp-05`). Dòng chữ: k = 0 và chưa mở → "Chưa bắt đầu"; "Đã xem gợi ý 1"; "Đã xem gợi ý 2"; "Đã xem lời giải" (D-GEO-075). Mốc Lời giải chỉ đạt được khi Đã mở | từ sự kiện `hint`, `unlock` của chính học sinh | không theo ngày | k nguyên 0 đến 3 | Bài của em, tab Gợi ý |
| M-GEO-15 | **Đã mở bài** = số học sinh của lớp có ≥ 1 sự kiện `open` trên bài giao, hiển thị `n / N` với N = số học sinh của lớp (ví dụ 3 / 5). **Mức gợi ý đã dùng** của một học sinh = 0, 1 hoặc 2, lấy max mức gợi ý đã xem. **Hỏi gì** = danh sách câu hỏi của học sinh trên bài đó, mới nhất trước. Không có sắp xếp theo điểm hay xếp hạng (BR-GEO-15) | chỉ học sinh của lớp mình dạy, cùng trường | không theo ngày | số nguyên; chưa có → 0 hoặc "Chưa mở"; không hỏi → "Chưa hỏi" | Theo dõi lớp |
| M-GEO-16 | **Lượt xem gợi ý trước khi xem lời giải** (bài tự học) = số sự kiện `hint` của học sinh trước sự kiện `unlock` đầu tiên của bài đó. Đo, chưa đặt ngưỡng | bài tự học | mỗi kỳ đo | số nguyên; chưa mở lời giải → "—" | báo cáo đo lường |
| M-GEO-17 | **Chi phí AI mỗi bài** = `Σ chi_phi(AiCall của bài) / N_bai` trong kỳ. Đo, chưa đặt ngưỡng | mọi lượt gọi AI gắn với bài, kể cả gọi lỗi; đơn vị tiền tệ: TBD — owner ChuDauTu (với nhà cung cấp giả lập chi phí là số mô phỏng) | mỗi kỳ đo | hai chữ số thập phân; N_bai = 0 → "—" | báo cáo đo lường |
| M-GEO-18 | **Tỉ lệ đề lạ** = `N_de_la / N_de_tu_nhap`, đề lạ = không khớp hình mẫu, rơi về tọa độ AI (BR-GEO-08). Đo | đề tự nhập | mỗi kỳ đo | phần trăm một chữ số | báo cáo đo lường |
| M-GEO-19 | **Quy mô dữ liệu thử tải** (D-GEO-049): học sinh mỗi lớp = 20.000 / 600 ≈ 33,3; lớp mỗi trường = 600 / 50 = 12; học sinh mỗi trường = 20.000 / 50 = 400; sự kiện mỗi học sinh = 2.000.000 / 20.000 = 100 | giả định brief §6 câu 5 | — | một chữ số thập phân khi không chia hết | thử tải |
| M-GEO-20 | **p95 đọc, p95 ghi, tỉ lệ lỗi** = bách phân vị 95 thời gian phản hồi của yêu cầu đọc và ghi; `lỗi / tổng` yêu cầu. Đề xuất đọc ≤ 300 ms, ghi ≤ 500 ms, lỗi < 0,5% (D-GEO-045). Một yêu cầu "lỗi" = mã trả về lỗi phía máy chủ hoặc quá thời gian; lỗi do người dùng gửi sai không tính | trong lúc tải M-GEO-11 và M-GEO-12 | mỗi lần thử tải | ms số nguyên; phần trăm hai chữ số | thử tải |
| M-GEO-21 | **Dùng gợi ý** (thẻ trên Theo dõi lớp, ví dụ "3") = số học sinh của lớp có ≥ 1 sự kiện `hint` trên bài giao (`Design/mvp-03`: An, Bình, Dũng → 3) | học sinh của lớp, cùng trường | không theo ngày | số nguyên; 0 hiển thị "0" | Theo dõi lớp |
| M-GEO-22 | **Câu hỏi** (thẻ, ví dụ "7") = tổng số sự kiện `ask` của lớp trên bài giao (mvp-03: 3 + 2 + 2 = 7); cột Hỏi đáp mỗi học sinh = số `ask` của em, "—" khi không hỏi | như trên | không theo ngày | số nguyên | Theo dõi lớp |
| M-GEO-23 | **Thời gian của một học sinh** (cột Thời gian, ví dụ "18 phút") = tổng độ dài các phiên làm bài (D-GEO-059) của em trên bài giao, làm tròn phút; chưa mở → "—". **Thẻ "thao tác hình" (ví dụ "12′")** = tổng thời gian của cả lớp / N, N = số học sinh của lớp, làm tròn số nguyên phút (mvp-03: (18 + 11 + 9 + 21 + 0) / 5 = 11,8 → 12). Cách hiểu này là suy luận của PO từ khung thiết kế (D-GEO-077) | như trên | phút nguyên | "—" khi chưa mở | Theo dõi lớp |
| M-GEO-24 | **Tình trạng của học sinh** (cột Tình trạng) = Chưa bắt đầu (chưa có `open`) · Đang chờ lời giải (đã xem Gợi ý 1 và 2, Lời giải còn khóa) · Đang làm (đã mở, chưa thuộc hai trạng thái còn lại và chưa phải tự làm) · Tự làm, không cần gợi ý (đã mở, 0 gợi ý, 0 câu hỏi). Ranh giới giữa "Đang làm" và "Tự làm" là suy luận của PO (D-GEO-077) | như trên | tức thời | một trong bốn nhãn | Theo dõi lớp |

## 4. Định dạng (một định nghĩa mỗi loại; chủ sở hữu ở `glossary.md`)

| Loại | Định dạng | Ví dụ |
|---|---|---|
| Số thập phân hiển thị | dấu phẩy, không dấu phân cách hàng nghìn trong số đo hình | 3,5 · 0,7071 |
| Số thập phân trong dữ liệu | dấu chấm | 3.5 |
| Giờ mở lời giải hiển thị | `HH:mm, dd/MM/yyyy` giờ Việt Nam; lưu UTC | (chỉ quy định định dạng; không có ngày ví dụ cố định) |
| Phần trăm | một chữ số thập phân, dấu phẩy, thêm "%" | 95,0% |
| Đáp số | số kèm đơn vị khi đề có đơn vị; dạng căn viết `a√3/2` như `materials/` | `24√3 cm³` |
| Góc | độ, ký hiệu ° | 45° |
| Tên lớp | như dữ liệu mẫu | 11A1 · 9B |
| Mã bài kho đề | `HKG-nn` | HKG-01 |
| Phần trăm, giây, ms | giữ như bảng chỉ số | — |

## 5. Ví dụ kiểm chứng (minh họa công thức, không phải AC)
- `HKG-01` (chóp S.ABCD, đáy vuông cạnh a, SA ⊥ đáy, SA = a): với a = 1, A(0,0,0), B(1,0,0), D(0,1,0), S(0,0,1): góc giữa SD và đáy tính từ tọa độ = 45°. Đáp số AI "45°": sai số 0 ≤ 1e-4 → M-GEO-03 đúng. Đại lượng đề hỏi là góc SDA; công cụ đo ẩn cả góc SDA và góc ASD trước giờ mở (PO-42, D-GEO-024).
- `HKG-02` (lập phương cạnh a, khoảng cách hai đường chéo nhau AC và B′D′): đáp số "a"; kiểm với a = 1, 2, 3 bộ giá trị tham số.
- `HKG-06` (mặt cầu): bảng tính ghi "Cần xem lại" trước khi engine tính; sau khi engine tính lại nhãn mới có thể đổi (D-GEO-004); không sao chép nhãn có sẵn.
