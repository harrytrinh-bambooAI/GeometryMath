Phiên bản: v1.0 · 27/11/2026

# Bộ mẫu baseline kiểm kết quả AI (BM-001…BM-071)

Đây là bộ mẫu gồm 71 bài toán, dùng làm baseline để kiểm bốn việc AI làm: (1) đáp số, (2) lời giải từng bước, (3) chấm điểm theo bước, (4) phát hiện bước sai. Phạm vi là app học hình học không gian THPT, trọng tâm Toán 11. Mỗi bài nằm trong một tệp `items/BM-NNN.json` theo schema đã giao. Mỗi bài có 2 lời giải sai, mỗi lời giải cài lỗi vào một bước cụ thể và thuộc một loại lỗi khác nhau. Mỗi lời giải sai có sẵn điểm đúng phải chấm.

Lệnh kiểm: `python3 check.py` (exit 0 khi đạt). Lần chạy ngày 27/11/2026: 71 bài, 10 bài có biểu điểm chính thức, cả 71 bài ở trạng thái `da-soat`, 0 lỗi.

## Thành phần

| Nguồn | Số bài | Lấy từ |
|---|---|---|
| `mvp` | 30 (BM-001…030) | `app/server/src/main/resources/content/catalog-mvp.json` v1.2 và `geometry/problem-specs.json` v1.1 (27/11/2026). `dap_so_engine` là giá trị engine thật đo tại a = 1 (xem dưới). |
| `lop11` | 31 (BM-031…061) | `artifacts/GEO/materials/lop-11/bai-tap/` (chương 7: 19 bài, chương 4: 7 bài, đại số / giải tích / xác suất: 5 bài). Thang điểm tự soạn theo kiểu HDC Sở, mọi điểm là bội 0,25. |
| `de-chinh-thuc` | 10 (BM-062…071) | HDC chính thức của Sở GD&ĐT Quảng Nam, tuyển sinh lớp 10 năm 2025–2026 (5 bài). HDC của Phòng GD&ĐT Quốc Oai, đề thi thử vào 10 năm 2024–2025, mã LG-004 (5 bài). Giữ nguyên thang điểm gốc, kể cả bậc 0,125. `nguon.ref` ghi URL và trang. |

## Bảng phủ dạng × mức độ × nguồn

Ô ghi số bài theo thứ tự mvp / lop11 / chính thức.

| Dạng | NB | TH | VD | VDC | Tổng |
|---|---|---|---|---|---|
| Góc (đường–đường, đường–mặt, mặt–mặt, nhị diện) | – | 5/2/– | 1/5/– | –/1/– | 14 |
| Khoảng cách (điểm–mặt, chéo nhau) | – | 3/1/– | 4/3/– | 1/2/– | 14 |
| Thể tích (chóp, lăng trụ, nón, tỉ số; trụ + cầu thực tế) | 4/–/– | 2/–/1 | –/2/1 | –/2/– | 12 |
| Diện tích (nón, chóp đều, mặt bên) | 2/–/– | 2/–/– | 1/–/– | – | 5 |
| Mặt cầu ngoại tiếp (lập phương, chóp, lăng trụ) | – | 1/–/– | 3/–/– | 1/–/– | 5 |
| Quan hệ song song (giao điểm, giao tuyến, Thalès, thiết diện) | – | –/1/– | –/4/– | –/2/– | 7 |
| Quan hệ vuông góc, bài chứng minh (BM-034) | – | – | – | –/1/– | 1 |
| Đại số, xác suất, giải tích (có bước rõ) | –/–/1 | –/–/4 | –/5/3 | – | 13 |
| **Tổng** | 7 | 22 | 32 | 10 | **71** |

Số bài theo cấp: THPT 57, THCS 14. THCS gồm 10 bài chính thức thi vào 10 và 4 bài MVP cấp THCS.

Các bài chứng minh, bài có họ nghiệm hoặc nhiều nghiệm thì để `dap_so.so = null`. Có 10 bài như vậy: BM-034, 052, 054, 057, 059, 062, 064, 067, 068, 071. Các bài này được soát bằng cách kiểm mệnh đề hoặc kiểm nghiệm.

Thống kê lời giải sai (142 lời giải):

| Loại lỗi | Số lời giải |
|---|---|
| sai-tinh-toan | 50 |
| sai-dinh-li | 45 |
| sai-hinh-dung | 22 |
| nham-dieu-kien | 15 |
| thieu-chung-minh | 10 |

Toàn bộ 71 bài có 224 bước.

## Quy ước chấm

- `tong_diem` bằng tổng `diem` của các bước. Thang tự soạn chia điểm theo bội 0,25, theo kiểu HDC Sở. Thang chính thức giữ nguyên số liệu gốc.
- `tieu_chi_dat`: điều kiện để bước được đủ điểm. Ví dụ: phải nêu lí do $AH\perp BC$, không chỉ kẻ $AH\perp SB$.
- `cach_khac`: những cách làm khác đúng mà vẫn cho đủ điểm bước. HDC luôn ghi: "làm cách khác đúng cho điểm tương đương".
- `loi_thuong_gap[].tru`: số điểm bị trừ ở bước đó. Lỗi cài trong lời giải sai cũng được ghi vào đây.
- `diem_dung` của lời giải sai tính như sau:
  - Mặc định: bằng tổng điểm các bước đứng trước `buoc_sai`. Bước sai và các bước dùng kết quả của nó đều không được điểm.
  - Ngoại lệ 1: nếu một bước sau không phụ thuộc bước sai và vẫn đúng, bước đó vẫn được điểm.
  - Ngoại lệ 2: nếu lỗi chỉ là `thieu-chung-minh` thì chỉ mất điểm của bước đó.
  - Bài thuộc ngoại lệ có giải thích trong `ghi_chu`. Ví dụ: BM-001 S1, BM-028 S1, BM-065.
- `ket_qua_so` (trường thêm, không bắt buộc): giá trị số của kết quả trung gian, tính tại a = 1. Dùng để so máy với kết quả của AI.

## Cách dùng để kiểm AI

1. **So đáp số.** Cho AI giải `de_bai`. Quy đáp số của AI về số tại a = 1, góc tính theo độ. So với `dap_so.so` với sai số tương đối 1e-6, hoặc 1e-3 nếu AI làm tròn. Bài MVP còn phải khớp `dap_so_engine`. Bài có `so = null` thì so tập nghiệm hoặc mệnh đề với `gia_tri`.
2. **So bước.** Ghép bước của AI với `buoc` theo `ket_qua_trung_gian`. Ưu tiên so `ket_qua_so` nếu bước có trường này, nếu không thì so biểu thức. Một bước của AI được tính là có nếu đạt `tieu_chi_dat`, hoặc khớp một mục trong `cach_khac`.
3. **Chấm theo bước.** Cho AI chấm lời giải chuẩn, kết quả phải bằng `tong_diem`. Cho AI chấm từng lời giải trong `loi_giai_sai`, kết quả phải bằng `diem_dung`. Có thể đặt ngưỡng chênh ±0,25 nếu cho phép AI chấm thô.
4. **Phát hiện bước sai.** Với mỗi lời giải sai, AI phải chỉ ra đúng `buoc_sai`, tức bước sai đầu tiên, và nên phân loại đúng `loai_loi`. Đo các chỉ số sau:
   - tỉ lệ chỉ đúng bước sai (142 ca);
   - tỉ lệ phân loại đúng loại lỗi;
   - tỉ lệ báo nhầm khi đưa lời giải chuẩn (71 ca, mong đợi 0).

   Các ca khó cần theo dõi riêng:
   - BM-033 S2: sai bước B3 nhưng đáp số cuối vẫn đúng.
   - Các ca `thieu-chung-minh`: đáp số đúng nhưng mất điểm lập luận.
   - BM-065: hai lời giải sai cùng ở bước B2 nhưng khác loại lỗi.

## Cách tạo và soát

- `gen/` là bộ sinh của tệp `items/`:
  - `mvp.py`, `lop11.py`, `chinhthuc.py` chứa dữ liệu bài.
  - `_lib.py` ghép lời giải sai và tính `diem_dung`.
  - Chạy `python3 gen/build.py` để sinh lại tệp.
  - Chạy `python3 gen/build.py --da-soat` để chỉ gắn `da-soat` cho bài đạt soát chéo.
- `engine-values.json` chứa 90 giá trị engine thật, gồm 84 đề HKN và 6 đề HKG. Cách lấy:
  - biên dịch `app/server/src/main/java` hiện hành (27/11/2026) ra thư mục tạm, không sửa `app/`;
  - gọi `DefaultGeometryEngine.build(spec, 1.0)` rồi `measure`.
- `gen/verify.py` là phần soát chéo độc lập:
  - tính lại đáp số và `ket_qua_so` bằng tọa độ, vectơ và duyệt số, không dùng công thức của lời giải;
  - với bài chứng minh, kiểm mệnh đề trên tọa độ cụ thể hoặc ngẫu nhiên;
  - kết quả ghi vào `soat-cheo.json`. Lần chạy ngày 27/11/2026 đạt 71/71.
- Các giá trị gần đúng trong lời giải sai đã được kiểm tay bằng máy tính, ví dụ 67,79°, 2,91, 959,6.

## Giới hạn

- Có 5 bài đề MVP được viết lại theo kiểu Toán 11, nên có trùng dạng với bài lớp 11. Ví dụ: BM-004 và BM-031 cùng dựa trên tam giác đều $AB'D'$.
- Có 61 bài dùng thang tự soạn, gồm 30 bài MVP và 31 bài lớp 11. Thang này chưa được giáo viên thật duyệt. Chỉ 10 bài có thang gốc của cơ quan quản lý giáo dục.
- Các bài chính thức chủ yếu là đại số THCS, vì HDC thi vào 10 tải được ít có hình không gian. Chỉ BM-066 (hình trụ và hình cầu) và BM-070 (hình nón) là bài hình không gian.
- Chưa có HDC chính thức nào cho tự luận hình không gian THPT. Đề tốt nghiệp THPT của Bộ (LG-003) chỉ có đáp án trắc nghiệm.
- Kết quả thử tải PDF HDC chính thức ngày 27/11/2026 (8 PDF):
  - 2 dùng được: Quốc Oai LG-004, Quảng Nam HDC TS10 2025–2026.
  - 3 là công văn hành chính, không có HDC: Hải Phòng (2 tệp) và TP.HCM, trên cdn.thuvienphapluat.vn.
  - 1 đề minh họa không có HDC: An Giang 2025.
  - 1 đề khảo sát không có HDC: Chuyên Lam Sơn 2024–2025.
  - 2 trả lỗi 404: hai trường THPT ở Ninh Bình.
  - Không dùng trang giải bài có bản quyền.
- BM-070 lấy kích thước từ hình vẽ của đề, khớp với số liệu HDC dùng ($r=6$, $h=12$). Bản PDF dạng chữ không thể hiện hình.
- Phần chấm `diem_dung` theo ngoại lệ (bước sau độc lập) là quy ước của bộ mẫu. HDC thật cho phép giám khảo linh hoạt, nên khi đo AI nên dùng ngưỡng ±0,25.

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Người/agent |
|---|---|---|---|
| v1.0 | 27/11/2026 | Tạo 71 bài (30 mvp, 31 lop11, 10 chính thức), check.py, bộ sinh `gen/`, giá trị engine thật, soát chéo độc lập 71/71 | agent runner-bo-mau |
