Phiên bản: v1.0 · 27/11/2026

# Hệ dữ liệu ĐỀ môn Toán THPT (ngân hàng câu, mẫu câu, ma trận, bộ lắp đề)

Khác với bài tập và giáo án: đây là dữ liệu để **lắp đề kiểm tra/thi** từ ma trận. Kiểm máy: `python3 check.py` (exit 0).
Hồ sơ đề lịch sử ở `lich-su/` (đã chốt, commit 3f0f8d4) là nguồn trọng số; thư mục đó không bị sửa.

## Nội dung

| Thư mục / tệp | Nội dung | Số lượng |
|---|---|---|
| `schema/` | JSON Schema: `cau` (câu ngân hàng), `mau_cau` (mẫu có tham số), `ma_tran`, `de` (mã đề đã lắp) | 4 |
| `ma-tran/` | 4 ma trận chuẩn + `yccd.json` (YCCĐ theo CT2018, diễn đạt lại) | KT15-HHKG11, GK2-L11, CK2-L11, TN-THPT-764; 41 YCCĐ |
| `mau-cau/` | Mẫu câu có tham số (khung, miền tham số, ràng buộc, đại lượng dẫn xuất, hàm đáp số sympy, sinh nhiễu gắn lỗi) + `engine-values.json` | 110 mẫu: 53 mẫu đơn từ 53 đề MVP + 4 mẫu đúng/sai ghép từ mẫu MVP; 46 mẫu đơn + 7 mẫu đúng/sai ngoài/ngoại hình không gian |
| `ngan-hang/` | Câu cụ thể sinh từ mẫu, mỗi tệp một chủ đề (id theo `lich-su/taxonomy.json`) | 1 774 câu: 804 TN4, 80 đúng/sai, 812 trả lời ngắn, 78 tự luận |
| `lap_de.py` | Bộ lắp đề: ma trận + số mã + seed → mã đề tương đương (JSON + Markdown: đề, đáp án, hướng dẫn chấm, bảng đặc tả, nhiễu-lỗi, ràng buộc) | |
| `de-mau/` | Đề đã lắp, 4 mã mỗi ma trận (seed 2026). **Đề mẫu cho owner: `de-mau/TN-THPT-764/ma-101.md`** | 16 mã |
| `check.py` | Kiểm toàn bộ (xem mục Kiểm) | |
| `gen/` | Bộ sinh: `mau_hh.py` (mẫu MVP), `mau_khac.py` (mẫu khác + phép kiểm độc lập), `core.py`, `build.py`, `ma_tran_def.py`, `schema_def.py` | |
| `tools/` | `EngineHarness.java` + `engine.py`: biên dịch engine hình học thật ra thư mục tạm và đo đại lượng (không sửa `app/`) | |

## Cách dùng

```bash
python3 gen/schema_def.py          # schema/
python3 gen/build.py               # mau-cau/, ngan-hang/, engine-values.json (gọi engine thật, dừng nếu lệch > 1e-6)
python3 gen/ma_tran_def.py         # ma-tran/ (trọng số tính từ lich-su/de/*.json)
python3 lap_de.py ma-tran/TN-THPT-764.json --so-ma 4 --seed 2026        # → de-mau/TN-THPT-764/
python3 check.py                   # exit 0 = đạt; --no-engine: dùng giá trị engine đã lưu
```

Yêu cầu: Python 3 + `sympy` (đã cài cho người dùng: `pip install --user sympy`); JDK + jar Jackson 3/Spring trong `~/.m2` để chạy lại engine (không có thì `check.py` dùng `mau-cau/engine-values.json`).

## Bốn ma trận chuẩn

| Ma trận | Thời gian | Cấu trúc | Ô xoay vòng | Căn cứ trọng số |
|---|---|---|---|---|
| `KT15-HHKG11` kiểm tra 15 phút, chương Quan hệ vuông góc (lớp 11) | 15 phút | 6 TN4 × 1,0 + 2 trả lời ngắn × 2,0 | 3 | tần suất dạng HHKG trong 8 đề TN 2017-2024 |
| `GK2-L11` giữa kì II lớp 11 (mũ-lôgarit, quan hệ vuông góc) | 90 phút | 12 TN4 × 0,25 + 4 đúng/sai × 1 + 6 trả lời ngắn × 0,5 | 4 | dạng HHKG đề TN cũ |
| `CK2-L11` cuối kì II lớp 11 (mũ-log, vuông góc, xác suất, đạo hàm) | 90 phút | 12 TN4 + 2 đúng/sai + 4 trả lời ngắn + 2 tự luận × 1,5 (7 + 3 điểm) | 5 | chủ đề trong đề 2025, 2026, MH 2025; dạng HHKG đề cũ |
| `TN-THPT-764` thi thử TN THPT | 90 phút | đúng QĐ 764: 12 TN4 × 0,25; 4 đúng/sai (0,1/0,25/0,5/1,0); 6 trả lời ngắn × 0,5 | 9 / 22 ô | vị trí từng phần trong đề 2025, 2026 (×1) và MH 2025 (×0,5), +0,5 làm trơn |

Mỗi ô: phần, loại câu, mức độ, số câu, điểm, đặc tả, YCCĐ; ô cố định có `chu_de` + `can_cu` (liệt kê câu trong đề lịch sử); ô xoay vòng có `nhom` [{chủ đề, (lọc dạng), YCCĐ, trọng số, căn cứ}]. Trường `bang` là bảng hàng chủ đề × cột `loại câu|mức độ` (ô xoay vòng tính số câu kì vọng). Giả định ghi trong `gia_dinh` của từng ma trận — quan trọng nhất: **cấu trúc mới chỉ có 2 đề chính thức + 1 minh họa**, trọng số phải cập nhật khi có đề 2027.

## Trường của bộ lắp đề: cố định / giáo viên chọn qua form / tùy chọn

| Trường | Loại | Giáo viên thấy trên form | Ghi chú |
|---|---|---|---|
| `loai_de` (kiểm tra 15 phút / giữa kì / cuối kì / thi thử TN) | form | danh sách chọn | chọn ma trận chuẩn làm khung |
| `lop`, phạm vi chương (`chu_de` các ô) | form | tick chương | bỏ chương → ô của chương đó chuyển sang chương còn lại cùng loại câu |
| `thoi_gian_phut` | form | ô số | kiểm: tổng thời gian dự kiến các câu ≤ thời gian |
| số câu mỗi phần (`phan[].so_cau`), điểm mỗi câu | form | ô số | tổng điểm phải = `tong_diem` |
| tỉ lệ mức độ NB/TH/VD/VDC | form | thanh trượt | đổi `muc_do` của ô |
| số mã đề, `seed` | form (số mã) / tùy chọn (seed) | ô số | seed giữ để in lại đúng bộ đề |
| trọng số ô xoay vòng | tùy chọn | ẩn, mặc định từ đề lịch sử | chỉ tổ trưởng chuyên môn chỉnh |
| YCCĐ đặc tả từng ô, `dac_ta` | tùy chọn | xem được, sửa nâng cao | mặc định theo ma trận chuẩn |
| cấu trúc phần TN THPT (12/4/6, thang đúng/sai) | cố định | không | theo QĐ 764 |
| ràng buộc: không trùng dạng, cân bằng A/B/C/D (lệch ≤ 1), không lộ đáp số, độ khó các mã lệch ≤ 5 %, phủ đặc tả 100 % | cố định | không | `lap_de.py` bốc lại đến khi đạt (tối đa 600 lần/mã) |
| `tong_diem` = 10 | cố định | không | |
| đảo thứ tự câu trong phần, đảo phương án | cố định | không | luôn bật |

## Cách sinh và kiểm đáp số

- **Mẫu MVP** (`gen/mau_hh.py`): mỗi mẫu giữ bộ dựng + đại lượng hỏi của đề MVP gốc (`nguon.ma`, ví dụ HKN-03), chỉ thay tham số trong miền liệt kê. Đáp số chuẩn = **engine thật** `DefaultGeometryEngine.build(spec, a=1)` + `measure` (biên dịch từ `app/server/src/main/java` hiện hành ra `/tmp`). Biểu thức sympy `dap_so` dùng để hiển thị và phải khớp engine (1e-6): 474/474 giá trị khớp.
- **Mẫu khác** (`gen/mau_khac.py`): đáp số sympy; mỗi mẫu có phép kiểm độc lập `KIEM` (duyệt không gian mẫu, mô phỏng lãi kép, tích phân Simpson, đạo hàm sai phân, duyệt đỉnh miền nghiệm, thay nghiệm) hoặc kiểm cấu trúc (nguyên hàm: đạo hàm lại; mặt phẳng: qua điểm + đúng VTPT; tiếp tuyến: qua tiếp điểm + đúng hệ số góc).
- **Nhiễu**: mỗi phương án nhiễu TN4 và mỗi ý sai của câu đúng/sai sinh từ một biểu thức lỗi, gắn `loi{loai_loi, mo_ta}`; `loai_loi` dùng bộ nhãn của baseline (`sai-dinh-li`, `sai-tinh-toan`, `sai-hinh-dung`, `nham-dieu-kien`). Biến thể nào không đủ 3 nhiễu khác nhau thì không sinh câu TN4.
- **Trả lời ngắn**: đáp án tối đa 4 ký tự (phiếu QĐ 764), chọn mức làm tròn đầu tiên vừa 4 ký tự và ghi vào đề.
- **Độ khó dự kiến** `do_kho` (tỉ lệ làm sai dự kiến) = mức gốc theo mức độ (NB 0,2 · TH 0,4 · VD 0,6 · VDC 0,8) ± loại câu ± độ phức tạp đáp số ± dao động biến thể. Chưa hiệu chỉnh bằng dữ liệu học sinh.

## Kiểm (`check.py`)

1. Schema hợp lệ cho 110 mẫu, 1 774 câu, 4 ma trận, mọi mã đề trong `de-mau/`; chủ đề/dạng khớp `lich-su/taxonomy.json`, YCCĐ khớp `ma-tran/yccd.json`.
2. Số lượng: 4 ma trận bắt buộc; ≥ 30 mẫu MVP (có 53); ≥ 20 mẫu ngoài hình không gian (có 46 mẫu đơn + 7 đúng/sai).
3. Đáp số: tính lại từ hàm đáp số trong `mau-cau/*.json` khớp ngân hàng; phép kiểm độc lập cho mẫu khác; chạy lại engine cho mọi câu/ý từ mẫu MVP (sai số 1e-6).
4. 100 % nhiễu gắn một lỗi; TN4 đủ 3 nhiễu, đúng 1 đáp án; trả lời ngắn ≤ 4 ký tự và đúng là giá trị làm tròn.
5. Ma trận: tổng điểm = 10, số câu/loại/điểm từng phần, trọng số mỗi nhóm cộng = 1 và có căn cứ dẫn đề lịch sử.
6. Mỗi ma trận lắp 3 mã × 2 seed; kiểm lại **độc lập với `lap_de.py`**: phủ đặc tả 100 %, khớp ô (chủ đề, dạng, YCCĐ, loại, mức độ, điểm), không trùng dạng, tổng điểm, tổng thời gian, A/B/C/D lệch ≤ 1, không chung bối cảnh và đáp số (biểu thức) không xuất hiện trong đề câu khác, đáp án chữ khớp ngân hàng, hướng dẫn chấm tự luận cộng đủ điểm, độ khó các mã lệch ≤ 5 %.
7. Có đề mẫu `de-mau/TN-THPT-764/ma-101.md` và dòng phiên bản README.

## Giới hạn

- Cấu trúc mới chỉ có 2 đề chính thức (2025, 2026) + MH 2025: trọng số xoay vòng TN-THPT-764 rất thô; ô "cố định" = có mặt ở phần đó trong ≥ 2/3 đề. Đề lớp 11 không có dữ liệu đề kiểm tra thật: trọng số dựa vào tần suất dạng trong đề TN 2017-2024 và giả định định dạng (CV 7991 chưa đọc toàn văn).
- Mức độ (NB/TH/VD/VDC) của mẫu là gán tay theo dạng; độ khó dự kiến chưa hiệu chỉnh bằng kết quả làm bài.
- Một số chủ đề còn mỏng: lượng giác (1 mẫu), hàm số - đồ thị (1 mẫu, chỉ tiệm cận đứng), Oxyz mặt cầu (1 mẫu + 1 đúng/sai). Không có câu cần hình vẽ/đồ thị/bảng biến thiên (đọc đồ thị, BBT) vì chưa có bộ vẽ; câu hình không gian chỉ mô tả bằng lời.
- Câu VDC thật sự (tìm m, hàm hợp, cực trị hình học) chưa có mẫu; ô VDC dùng các mẫu vận dụng dài (thể tích tròn xoay, toàn phần, hệ phương trình thực tế).
- Các mã đề tương đương dùng cùng mẫu câu với tham số khác (không trùng câu giữa các mã trong hầu hết trường hợp); học sinh làm nhiều mã sẽ nhận ra khung đề.
- "Không lộ đáp số" kiểm bằng: không hai câu chung bối cảnh (cùng hình + tham số) và chuỗi đáp số không xuất hiện trong đề câu khác; chưa kiểm suy luận ngầm giữa các câu khác bối cảnh.
- Ý của 7 mẫu đúng/sai ngoài hình không gian chỉ có đáp số sympy (chưa có phép kiểm độc lập như mẫu đơn); ý của 4 mẫu đúng/sai từ MVP được kiểm bằng engine.
- Thời gian dự kiến mỗi câu là bảng định sẵn theo loại × mức độ (`gen/core.py`), chưa đo thực tế.
- Không chép nguyên văn đề có bản quyền: toàn bộ câu là sinh từ mẫu tự soạn hoặc từ kho đề MVP của dự án.

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Người/agent |
|---|---|---|---|
| v1.0 | 27/11/2026 | Tạo schema (4), 4 ma trận chuẩn + YCCĐ, 110 mẫu câu (53 + 4 từ MVP, đáp số engine 474/474), ngân hàng 1 774 câu, `lap_de.py`, `check.py`, đề mẫu 16 mã | agent runner-de-thi-lap |
