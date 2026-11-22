# E1 — QC findings (đối chiếu AC ↔ case, E3 của guide)

> Chủ sở hữu: QC. Bảng đối chiếu hai chiều đầy đủ nằm ở `test_cases.md` §3 (sinh tự động). Tệp này ghi điều QC thấy khi đối chiếu: AC khó kiểm, thiếu giá trị, hành vi chưa có AC. Loại: Fixed (QC đã xử lý trong case) · To BA · Question · Rejected. Cột "Cần trước" là một cổng review, không ghi ngày. Giải thích mã: `test_cases.md` §0.

| ID | Type (Fixed / To BA / Question / Rejected) | Where | Finding | Action taken or proposal | Owner · cần trước |
|---|---|---|---|---|---|
| QF-E1-01 | Fixed (BA nhận, M-208) | US-GEO-101-FC2 | Then có vế điều kiện "máy chủ trả `VALIDATION_FAILED` nếu yêu cầu vẫn được gửi": một tiêu chí hai hành vi (màn chặn, máy chủ chặn), kết quả đạt phụ thuộc cách làm giao diện | TC-GEO-0108 chạy ở cả hai tầng (trên màn và gửi thẳng máy chủ 61 ký tự). Đề xuất BA bỏ chữ "nếu": máy chủ luôn trả `VALIDATION_FAILED` khi nhận 61 ký tự | BA · R3 của E1 |
| QF-E1-02 | Fixed | US-GEO-101-SC2, SC6 | Case tạo học sinh mới trong 11A1 làm đổi dữ liệu dùng chung: Theo dõi lớp thành sáu học sinh, lệch số "4 / 5" của `F-GEO-050…054` dùng ở E7 | Remark của TC-GEO-0101, 0105 có dòng khôi phục (xóa học sinh vừa tạo); test plan §8 yêu cầu dữ liệu mẫu nạp lại được | QC · xong |
| QF-E1-03 | Fixed (BA thêm US-GEO-104-FC8, M-208; TC-GEO-0800 nay trỏ FC8) | US-GEO-104 (chưa có tiêu chí) | Con trỏ phân trang của trường này dùng với thẻ trường khác chưa có tiêu chí; ARCH đã trả lời `INVALID_CURSOR`, không trả trang rỗng giả (M-402 c) | TC-GEO-0800 (loại X, trỏ NFR-GEO-030). Đề xuất BA thêm US-GEO-104-FC8 để case trỏ về tiêu chí | BA · R3 của E1 |
| QF-E1-04 | Question (ARCH) | US-GEO-107-FC2; MSG-GEO-57 | Kỳ vọng "máy chủ trả báo quá tải" không có `code` cụ thể; từ điển ghi "mã do ARCH chốt"; danh sách `code` của M-004 không có mã quá tải hay vượt giới hạn tốc độ | ARCH trả lời (M-405): đề xuất `SERVICE_OVERLOADED` (503) và `RATE_LIMITED` (429), chờ A thêm vào danh sách `code`. Tiêu chí 107-FC2 chưa ghi `code` nên TC-GEO-0155 vẫn chỉ kiểm thời gian ≤ 2 giây, thời gian nên thử lại và câu MSG-GEO-57; đề xuất BA thêm `code` khi A chốt (M-110) | A, BA · R3 của E1 |
| QF-E1-05 | To BA | US-GEO-104, 107 (chưa có tiêu chí) | Bộ quét SQL bắt mẫu cấm (NFR-GEO-031, 040), truy vấn nóng không quét tuần tự (NFR-GEO-042), Redis ngừng (NFR-GEO-057), tiền tố trường của khóa đệm (NFR-GEO-034) không có tiêu chí ở E1 | TC-GEO-0801…0804 (loại X, trỏ NFR). BA đã nêu ở M-204 (QA-GEO-21) rằng NFR do ARCH thêm không thành AC khi chưa có PO duyệt: QC giữ case loại X, ưu tiên theo rủi ro | PO phán theo QA-GEO-21 · R3 của E1 |
| QF-E1-06 | Fixed | US-GEO-105-SC1; `F-GEO-036` | Xung đột dữ liệu đã biết: tệp nguồn ghi `HKG-06` "Cần xem lại", phép tính cho "Đã kiểm chứng" (QA-GEO-03) | In cạnh dữ liệu ở Remark của TC-GEO-0135 (M6); case PROVISIONAL | QC · xong |
| QF-E1-07 | Question (PO) | US-GEO-104-FC3; D-01 của `06_nfr.md` | "Dữ liệu của chính mình" chỉ kiểm được qua id; ai biết mã lớp và tên bạn thì vào được dữ liệu của bạn (đăng nhập tên + mã lớp). BA cố ý không viết AC chặn (Q-CDT-03) | Không viết case chống giả mạo; ghi ở test plan §6 | ChuDauTu (qua PO) · R3 của E1 |
| QF-E1-08 | Fixed | US-GEO-103-FC2 | Thời gian chờ tối đa của cổng AI chưa có giá trị (D-GEO-072, NFR-GEO-054) | TC-GEO-0121 dùng giá trị cấu hình của lần chạy, PROVISIONAL D-GEO-072 | TechLead · R3 của E1 |

## Kết quả đối chiếu
- Chiều xuôi: 62 / 62 tiêu chí có case (100%). Chiều ngược: 67 / 67 case trỏ về tiêu chí của BA hoặc NFR-GEO (100%); 63 / 67 trỏ về tiêu chí của BA, 4 case loại X chỉ trỏ NFR (QF-E1-05).
- Không tiêu chí nào của E1 mâu thuẫn với nhau hoặc với `02_domain_model.md`. Giá trị đã soát: 95,0% / 90,0% / 92,5% / 87,5% (US-GEO-105-SC4, N = 40) khớp M-GEO-01, 04; 83,3% = 5/6; quy mô 50 / 600 / 20.000 / 1.900.000…2.100.000 khớp M-GEO-19 và NFR-GEO-049.
- Tiêu chí không kiểm được như đang viết: không có. Tiêu chí kiểm được nhưng kỳ vọng còn phụ thuộc điểm mở: QF-E1-01, QF-E1-04, QF-E1-08.

## Lượt sửa gom sau phản biện (M-304, RV-GEO-01…37, D-GEO-125…170)

| ID | Type | Where | Finding | Action taken or proposal | Owner · cần trước |
|---|---|---|---|---|---|
| QF-E1-09 | Fixed | US-GEO-102; RV-GEO-02; D-GEO-152 | Không có ca dò mã giáo viên và ca lưu băm | Thêm TC-GEO-0171 (10 lần sai / 15 phút theo IP → `RATE_LIMITED`), TC-GEO-0172 (mã lưu băm), PROVISIONAL D-GEO-152 | QC · xong |
| QF-E1-10 | To BA | `F-GEO-003`, `F-GEO-004`, `F-GEO-005`; US-GEO-102 | Mã mẫu `GVMAU-2K7Q` chỉ 4 ký tự biến đổi, trái D-GEO-152 (≥ 12 ký tự base32, theo tài khoản); chưa có tiêu chí thu hồi, hạn dùng, giới hạn thử sai | Case dùng mã của fixture hiện có; đề nghị BA đổi fixture và thêm tiêu chí (bảng chung) | BA · R3 của E1 |
| QF-E1-11 | Question (PO) | D-GEO-152 | (1) "theo tài khoản" khi mã sai thì nhận ra tài khoản bằng gì; (2) mã đúng gửi trong cửa sổ bị chặn có bị chặn không | TC-GEO-0171 giả định: chặn theo IP, mã đúng trong cửa sổ vẫn `RATE_LIMITED` | PO · R3 của E1 |
| QF-E1-04 | Fixed (D-GEO-143) | US-GEO-107-FC2 | Mã quá tải đã có: `SERVICE_OVERLOADED` | TC-GEO-0155 chỉnh theo D-GEO-143 qua `E1_override.txt`; gỡ chỉnh khi BA ghi mã vào tiêu chí | BA · R3 của E1 |
| QF-E1-12 | Fixed | case X TC-GEO-0801…0804; RV-GEO-35; D-GEO-125, 170 | Tầng và công cụ của case X chưa rõ; dẫn "ARCH đề xuất" | Ghi công cụ ở Remark (bộ quét `SqlLint`, `psql`, Gatling, kiểm thử tích hợp API và `redis-cli`), dẫn D-GEO-125; case tải ghi Gatling | QC · xong |

- Đối chiếu sau lượt sửa: 62 / 62 tiêu chí có case (100%); 69 / 69 case trỏ về tiêu chí của BA hoặc NFR-GEO; 65 / 69 trỏ về tiêu chí của BA, 4 case X.

### Đồng bộ với bàn giao của BA (M-217, M-218)
- TC-GEO-0171 → US-GEO-102-FC6, 0172 → 102-SC4 (case viết tay trước đó chuyển thành case bám tiêu chí, giữ id); mới: 0173 (101-FC5), 0174 (102-SC3), 0175 (102-FC4), 0176 (102-FC5), 0177 (102-FC7), 0178 (104-FC9). Gỡ chỉnh TC-GEO-0155 (AC đã ghi `SERVICE_OVERLOADED`). QF-E1-10 đóng (fixture mã 12 ký tự `F-GEO-003…009`); QF-E1-11 còn mở ở M-113 và M-216.
- Đối chiếu cuối: 70 / 70 tiêu chí có case (100%); 75 / 75 case trỏ về tiêu chí có thật của BA hoặc NFR-GEO; 0 case trỏ tiêu chí không tồn tại (bộ sinh và phép đối chiếu độc lập).
- Sau M-306, M-220, M-221 (D-GEO-173…183): TC-GEO-0171 theo D-GEO-173, TC-GEO-0172…0177 không đổi. Đối chiếu: 70 / 70 tiêu chí, 75 / 75 case (71 trỏ tiêu chí của BA, 4 case X).

## Sinh lại theo AC hiện hành (PO-50, Đợt 0; 22-11-2026)

| ID | Type | Where | Finding | Action taken or proposal | Owner · cần trước |
|---|---|---|---|---|---|
| QF-E1-13 | To BA | US-GEO-102-FC6; D-GEO-185 (M-307) | PO nhận thêm quy tắc đếm thử sai theo tài khoản (4 ký tự đầu của mã định vị tài khoản) bên cạnh theo IP; AC giữ nguyên quy tắc theo IP nên hành vi mới chưa có tiêu chí và chưa có ngưỡng riêng | Thêm TC-GEO-0179 (ba IP, cùng tiền tố `GQ7K`, lần 11 → `RATE_LIMITED`), PROVISIONAL D-GEO-185, ngưỡng giả định bằng D-GEO-173; dữ liệu `F-GEO-240` ở `review/QC_test_design.md` §6. Đề xuất BA thêm một tiêu chí cho quy tắc theo tài khoản và ghi ngưỡng | BA · R3 của E1 |
| QF-E1-14 | Question (TechLead) | Bộ E2E (D-GEO-170, PO-50); TC-GEO-0111, 0113, 0171, 0175, 0179 | Bộ E2E chạy nhiều lần đăng nhập giáo viên từ cùng một máy: giới hạn 10 lần sai / 15 phút (theo IP và theo tài khoản) làm case sau nhận `RATE_LIMITED` không do lỗi của case | Đề nghị profile test có móc đặt lại bộ đếm thử sai giữa các case và cho đặt IP nguồn (proxy tin cậy, `X-Forwarded-For`); thiếu móc này thì chạy các case đăng nhập sai ở cuối lượt, tách khỏi case đăng nhập đúng | TechLead · trước khi viết E2E của E1 |
| QF-E1-15 | Fixed | mọi case của E1 | Case chưa có trường cho biết tự động hóa bằng cách nào; cột "Tầng tự động hóa" cũ ghi E2E trình duyệt = 0 | Thêm cột "Tự động hóa" do bộ sinh suy từ tầng và loại; QC chỉnh từng case ở `work/src/qc/E1_auto.txt` (12 case). Kết quả: E2E-UI 21 · E2E-API 19 · IT 27 · Manual 9 (8 case thử tải Gatling, 1 case bàn phím ảo iPhone) | QC · xong |

- Sinh lại: bộ sinh chạy trên `stories_ac.md` hiện hành cho đúng nội dung tệp cũ (0 case lệch AC); thay đổi chỉ là cột "Tự động hóa" và TC-GEO-0179. Không case nào bị bỏ.
- Đối chiếu: 70 / 70 tiêu chí có case (100%); 76 / 76 case trỏ về tiêu chí của BA hoặc NFR-GEO (72 trỏ tiêu chí của BA, 4 case X); kiểm bằng `work/tools/check_ac_tc.py`.
