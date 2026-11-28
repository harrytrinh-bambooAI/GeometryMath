# Đặc tả giao diện · Soạn giáo án cùng AI

Phiên bản: v1.0 · 27/11/2026

Nguyên tắc: giáo viên chỉ chọn và gõ câu ngắn. Mọi JSON do máy chủ dựng và đọc. Danh sách trường theo `truong.md`.

## 1. Màn "Soạn giáo án mới" (form)

```
┌─ Soạn giáo án mới ──────────────────────────────────────────────┐
│ Tiết PPCT *   [Chương 7 ▾] [Bài 26. Khoảng cách ▾] [Tiết 1/2 ▾] │
│ Số tiết       [ 1 ] (tự điền theo PPCT, sửa được)               │
│ Đối tượng *   ( Cơ bản ) (●Khá ) ( Chuyên )                      │
│ Phong cách *  [●Truyền thống] [Khám phá] [Lớp học đảo ngược]    │
│               [Dự án STEM] [Trò chơi]                           │
│ Kiến thức trọng tâm *  (tích sẵn theo tiết, bấm để bỏ/thêm)      │
│   [x] Khoảng cách từ điểm đến đường thẳng, mặt phẳng            │
│   [x] Hệ thức 1/AH² = 1/SA² + 1/AK²                             │
│   [ ] Khoảng cách hai đường chéo nhau   [ ] … (xem thêm)        │
│ ▸ Tuỳ chọn (thu gọn)                                            │
│   Phiếu học tập  [Bật ●]                                        │
│   Số bài tập [6]  NB[2] TH[2] VD[1] VDC[1]   ✓ tổng = 6          │
│   Phân hoá  [Không ▾]   Phần mềm hình [Không ▾]                  │
│   Thiết bị  [Máy chiếu ✓] [Mô hình ✓] [+ thêm]                  │
│   Ghi chú   [______________________________________] 0/500      │
│                                         [ Soạn giáo án ▶ ]      │
└─────────────────────────────────────────────────────────────────┘
```

Quy tắc form:
- Thiếu trường có dấu `*` thì nút "Soạn" mờ và có dòng nhắc ngay dưới trường.
- Đổi Tiết PPCT thì số tiết và các ô kiến thức tích sẵn được điền lại.
- Ô tỉ lệ: nếu tổng khác số bài thì hiện cảnh báo đỏ và chặn gửi.
- Ghi chú: hiện chú thích nhỏ "AI coi ghi chú là thông tin tham khảo, không đổi được luật soạn".

## 2. Màn kết quả + chat chỉnh sửa

```
┌─ Bài 26. Khoảng cách · Tiết 1/2 · Khá · Truyền thống · 45' ─────┐
│ 🔒 I. Mục tiêu              [mở khoá]                            │
│ 🔒 II. Thiết bị             [mở khoá]                            │
│    III. Tiến trình      ▏5'  HĐ1 Mở đầu: …          [chọn để sửa]│
│                         ▏20' HĐ2 Hình thành: …                    │
│                         ▏15' HĐ3 Luyện tập: …                     │
│                         ▏5'  HĐ4 Vận dụng: …    Σ 45' ✓           │
│    IV. Phiếu học tập PHT1 (3 câu · đáp số: máy tính ✓)           │
│ ⚠ AI đề xuất (2)   ▸ Tổng 50' > 45' …   [Nhận] [Bỏ qua]          │
├─ Chat ──────────────────────────────────────────────────────────┤
│ Đang chọn: HĐ1 Mở đầu                                           │
│ [ Đổi thành trò chơi "Ai nhanh hơn", giữ 5 phút      ] [Gửi]     │
│ AI ▸ Bản vá 7 trường trong HĐ1:                                  │
│   ten: "Khởi động: Khoảng cách…" → "Trò chơi Ai nhanh hơn"      │
│   …                                  [Nhận tất cả] [Sửa] [Bỏ]   │
└─────────────────────────────────────────────────────────────────┘
```

Hiển thị theo `kind` của đầu ra:
| kind | Hiển thị |
|---|---|
| `lesson` | dựng 4 mục theo khung 5512; thanh thời lượng có Σ phút; `unsure` thành nhãn "AI chưa chắc" cạnh mục liên quan; `de_xuat` gom vào khung ⚠ |
| `patch` | hiện từng cặp before → after kèm `ly_do`; giáo viên nhận cả bản vá, nhận từng dòng hoặc bỏ; không áp bản vá trước khi giáo viên bấm Nhận |
| `question` | bong bóng chat có 1 câu hỏi; ô trả lời đặt sẵn tiêu điểm; nếu hỏi về trường của form thì có nút mở trường đó |
| `reply` | bong bóng chat giải thích luật chặn; `de_xuat` hiện như trên |

Trạng thái lỗi:
- Máy chủ đã tự gọi lại một lần mà vẫn lỗi: hiện "Chưa soạn xong, thử lại" (không hiện JSON hỏng).
- Bị từ chối an toàn (`refusal`): hiện "AI không xử lý được yêu cầu này".

## 3. Khoá mục
- Mặc định khoá Mục tiêu và Thiết bị sau khi giáo viên bấm "Chốt mục tiêu". Từng HĐ có thể khoá riêng.
- Máy chủ gửi danh sách khoá trong `<muc_da_khoa>`. Bản vá chạm vào mục khoá bị máy chủ loại trước khi hiện (thí nghiệm ghi nhận 0 lần ở V5–V8).

## 4. Mock HTML tĩnh
Không làm bản HTML riêng: bản ASCII trên đủ cho đặc tả; dựng bằng component của app khi vào code (`app/**` ngoài phạm vi thí nghiệm này).

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.0 | 27/11/2026 | Đặc tả form + màn kết quả/chat theo bảng trường v1.0 | runner-prompt-giaoan (Claude Opus 5.5) |
