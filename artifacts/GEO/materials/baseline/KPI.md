# KPI · độ chính xác AI trên lời giải / chấm bài

Phiên bản: v1.0 · 27/11/2026 · Owner duyệt "KPI OK" 27/11/2026 (xem DECISIONS.md)

Dùng chung cho mọi bench chấm AI (bộ mẫu `bo-mau/`, prompt-lab, bench-mvp). Dữ liệu chuẩn: `bo-mau/items/` (bước, điểm, tiêu chí, lời giải sai), đáp số engine, kho kiến thức `lop-11/ly-thuyet/`.

## 1. Điểm một lời giải (0–100 %)

| Thành phần | Bài tính toán | Bài chứng minh | Cách đo |
|---|---|---|---|
| Đúng hướng / phương pháp | 20 | 25 | so `buoc` + `cach_khac` của bộ mẫu |
| Đúng các bước / ý lập luận | 40 | 50 | điểm + `tieu_chi_dat` từng bước; khớp `ket_qua_trung_gian` |
| Đúng kết quả cuối | 20 | 0 | so đáp số engine (sai số 1e-6) hoặc tương đương biểu thức |
| Vẽ hình đúng | 10 | 15 | engine kiểm thao tác dựng: điểm, đường, quan hệ ⊥ / ∥ |
| Trình bày | 10 | 10 | judge có tham chiếu, rubric 1–5 (kí hiệu SGK, lập luận đủ) |

Trắc nghiệm / đúng-sai: kết quả 100 %; các bước chỉ ghi để chẩn đoán.

## 2. Luật chặn (chống điểm đẹp giả)
1. Sai hướng → 0 điểm các bước phụ thuộc hướng đó.
2. Đúng kết quả nhưng lập luận sai/thiếu → điểm kết quả tối đa 50 %.
3. Bước sai → bước sau phụ thuộc = 0; bước độc lập vẫn tính (`diem_dung`, lệch cho phép ±0,25).
4. Bài hình không gian không có hình / hình sai → mất phần hình + các bước dựa trên hình sai.
5. Dùng định lí / công thức ngoài kho kiến thức → bước đó 0 điểm, gắn cờ "bịa kiến thức".

## 3. KPI tổng hợp
- **Theo dạng × mức độ:** điểm trung bình, tỉ lệ đạt (≥ 80 %); điểm chung = trung bình đều giữa các dạng (macro). Luôn ghi n và KTC 95 %.
- **Theo bước:** độ phủ bước, độ đúng bước, khớp kết quả trung gian, bắt đúng bước sai (+ tỉ lệ báo nhầm), độ lệch chấm (MAE, tỉ lệ trong ±0,25).
- **Theo kết quả:** tỉ lệ đáp số đúng; riêng "đúng kết quả – sai lập luận" là chỉ số rủi ro.
- **An toàn:** bịa kiến thức, phạm luật cố định, lộ đáp số cho học sinh ở chế độ gợi mở.
- **Lặp:** mỗi cấu hình chạy ≥ 3 lần/ca; báo trung bình ± KTC 95 % và tỉ lệ ổn định (đạt cả 3 lần). Chỉ kết luận A tốt hơn B khi chênh lệch > KTC.

## 4. Ngưỡng phát hành (khởi điểm, chỉnh sau lần đo đầu + giáo viên chấm đối chứng ~10 bài)

| KPI | Ngưỡng |
|---|---|
| Kết quả đúng trên bài MVP | ≥ 95 % |
| Điểm tổng (macro theo dạng) | ≥ 85 %, không dạng < 70 % |
| Bắt đúng bước sai | ≥ 80 %, báo nhầm ≤ 10 % |
| Độ lệch chấm trong ±0,25 | ≥ 85 % |
| Bịa kiến thức / phạm luật cố định | 0 |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.0 | 27/11/2026 | Tạo theo đề xuất được owner duyệt | delivery-lead (Claude) |
