Phiên bản: v0.1 · 2026-11-28
# Brief S5 · GEO · Chặng 5 — Đánh giá và trợ lý giáo viên

Trạng thái: **ready for R3 (goal run, PO quyết cao nhất)**. Mọi điểm mở là giả định của PO, ghi ở `questions-s5.md` để owner xác nhận; đổi sau là CR qua `PO-DECISIONS.md`.

| Mục | Nội dung |
|---|---|
| Mục tiêu | Khép vòng dạy học có AI mà giáo viên vẫn quyết định: (1) trợ lý soạn giáo án, bộ đề theo ma trận, thông báo; (2) kho tri thức do giáo viên quản lý, hỏi đáp của học sinh chỉ dựa trên kho đó, kiểm soát đáp án đầy đủ; (3) nộp bài viết tay, rubric, AI đề xuất điểm, giáo viên duyệt; (4) tổng hợp lỗi sai của lớp (COP-02). Đo được: tỉ lệ khớp AI–giáo viên theo tiêu chí (KPI §4), tỉ lệ tiêu chí bị sửa, tỉ lệ AI từ chối chấm, thời gian duyệt một bài, số lần lộ đáp án trong bộ câu thử (= 0), tỉ lệ câu trả lời có nguồn đã duyệt, tỉ lệ bản nháp trợ lý được duyệt sau khi sửa (STAGES Ch5 §10). |
| Nguồn | `PRODUCT-MASTER.md` (22/11/2026) D1 dòng 5, C1b "Đánh giá và AI chấm bài", "Trợ lý giáo viên", C1c, C4, C5; `STAGES-AFTER-MVP.md` Chặng 5 (22/11/2026, chỉ phần dẫn được về PRODUCT-MASTER theo PO-19); `PO-DECISIONS.md` (mới nhất PO-85, 27/11/2026): PO-02, 03, 22, 25, 27, 28, 30, 32, 33, 34, 37, 43, 44, 45, 46, 47, 48, 49, 61, 64, 65, 78, 79, 83, 85; `DECISIONS.md` 27/11 (prompt 3 lớp, KPI chấm, dữ liệu đề); tài sản 27/11/2026 (bản mới nhất, commit ee94712): `prompt-lab/giao-an/` (`prompt-final.md` v1.1, `truong.md` v1.1, `giao-dien.md` v1.0), `materials/baseline/KPI.md` v1.0, `materials/baseline/bo-mau/` (71 bài), `materials/de-thi/` v1.0, `materials/PLAN-kho-bai-giang.md` v1.0; code `aigateway/internal/AnthropicPrompts.java` (task `scene`, `solution`, `hint`, `ocr`, `qa`, `lecture`, `authoring_chat`, `class_stuck`); kiến trúc `work/architecture/01_adr.md` (ADR-GEO-01, 09, 14), `06_nfr.md`. Khi nguồn mâu thuẫn: PO-DECISIONS > PRODUCT-MASTER > STAGES (PO-19). |
| Xong khi | `python3 artifacts/GEO/work/s5/check_s5.py` thoát 0 (ước lượng mỗi story trong [1, 3] ngày dev và test ≤ 0,3 × dev; mỗi story ≥ 1 AC, mỗi AC ≥ 1 TC; mã US/AC/BR/TC không trùng; mỗi TC là E2E-API hoặc E2E-UI; tổng `estimate-s5.md` khớp tệp epic). |
| Ngoài phạm vi | Multi-tenant, tổ chức cấp trường, quản trị trường và liên trường (C5-09 thu về mức lớp); chặng 6 (phụ huynh, hồ sơ học tập, giáo trình riêng); tự duyệt điểm (STAGES ý 9); học sinh "AI chấm thử" (ý 10); duyệt nhận xét theo cụm (ý 8) và phúc khảo (P1 STAGES, để chặng sau: QS5-29); thi có giám sát; sổ điểm chính thức của trường; thông báo đẩy ngoài ứng dụng; code app, test, Design; gọi AI thật trong CI. |

## 1. Đánh giá lại workload

- **Kết quả:** 6 epic · 34 story · 172 AC · 172 TC · **80 ngày dev + 23,8 ngày test = 103,8 người-ngày** (1 dev, ≈ 21 tuần), chưa gồm dự phòng +20 % và các khoản ngoài bảng (bench AI chấm 5 ngày, thiết kế màn 6–8 ngày) ở `estimate-s5.md` §4.
- **So với khung dự kiến (5 epic):** khung gộp nộp bài, AI chấm và duyệt vào một epic S5-E4 ≈ 30 ngày dev, 13 story: quá lớn để giao và soát một lượt. PO tách thành **S5-E4** (đợt bật 1: rubric, nộp bài, OCR, chấm tay, trả bài) và **S5-E6** (đợt bật 2–3: AI đề xuất, engine tín hiệu hai, chạy song song, cổng bật theo KPI), đúng "tách ba đợt bật" của STAGES §10 và điều kiện "chạy song song trước khi bật" của §1. Các điều chỉnh khác ghi ở đầu từng `stories_ac.md`:
  - S5-E1 thêm story máy chủ dựng tin nhắn và kiểm luật A (phần nặng nhất, §4 của `prompt-final.md`); mức P0 thay "giáo án P1" của STAGES vì PO-27, PO-45 là Final.
  - S5-E2 thêm story nạp dữ liệu đề có kiểm và story trợ lý soạn đề nháp từ tài liệu (luồng D).
  - S5-E3 tách kiểm soát đáp án thành kiểm trước khi gọi AI (mức trợ giúp, lịch công bố) và kiểm đầu ra + nhật ký; thêm story giáo viên xem lịch sử và báo chặn sai.
  - S5-E5 thêm công tắc AI chấm/trợ lý và hạn mức theo lớp (thay "công tắc theo trường" vì multi-tenant ngoài phạm vi).
- **Dùng lại, không làm lại:** prompt V8 + bảng trường + bố cục màn giáo án; thuật toán và ràng buộc `lap_de.py`, ngân hàng 1 774 câu có đáp số engine, `check.py` làm bộ kiểm đối chứng; KPI v1.0 làm mẫu rubric, luật chặn và ngưỡng cổng; bộ kiểm KS-1…KS-5 (ADR-GEO-14, đã cài V60); task `qa`, `ocr`, `class_stuck` làm khuôn cho `kb_qa`, `grade_ocr`, `class_errors`; mô hình lớp, bộ bài, thông báo trong ứng dụng, công tắc AI lớp của chặng 3.

## 2. Phạm vi theo epic

| Epic | Tên | Mức | US | Dev | Test | Tổng | Nguồn chính |
|---|---|---|---|---|---|---|---|
| S5-E1 | Trợ lý soạn giáo án | P0 | 5 | 12 | 3,5 | 15,5 | PO-27, 45, 46; prompt-lab giáo án |
| S5-E2 | Ra đề theo ma trận, trợ lý soạn bộ đề | P0 | 6 | 14 | 4,1 | 18,1 | D1 dòng 5; `de-thi/` |
| S5-E3 | Kho tri thức, hỏi đáp theo kho, kiểm soát đáp án đầy đủ | P0 | 6 | 15 | 4,5 | 19,5 | PO-33, 34, 49; D-08; ADR-GEO-14 |
| S5-E4 | Rubric, nộp bài viết tay, OCR, chấm tay, trả bài (đợt 1) | P0 | 7 | 16 | 4,8 | 20,8 | C1b; STAGES §5 A, B |
| S5-E5 | Thông báo trợ lý, tổng hợp lỗi (COP-02), công tắc và hạn mức AI | P1 (01, 04 là P0) | 4 | 9 | 2,7 | 11,7 | C4; D-GEO-078 |
| S5-E6 | AI đề xuất điểm, engine tín hiệu hai, chạy song song (đợt 2–3) | P0 | 6 | 14 | 4,2 | 18,2 | PO-03; KPI v1.0; STAGES §1, §3 |
| **Cộng** | | | **34** | **80** | **23,8** | **103,8** | |

## 3. Giả định chính đang dùng (chi tiết và câu hỏi ở `questions-s5.md`)

1. **QS5-01 — Ảnh bài làm (PO-64):** code S5-E4/S5-E6 làm theo giả định "thông báo riêng tư phiên bản S5, nhắc không ghi họ tên lên bài, ảnh lưu trong nước, giữ tới hết năm học rồi xoá, xoá theo tài khoản"; **phát hành chờ owner chốt**.
2. **QS5-28 — Điều kiện mở chặng (D1: có trường cam kết pilot):** S5-E1, S5-E2, S5-E3 làm và phát hành không cần trường pilot; S5-E4 phát hành khi có lớp pilot; S5-E6 bật cho lớp nào chỉ sau khi lớp đó đạt cổng KPI.
3. **QS5-02 — Không multi-tenant:** mọi công tắc và báo cáo ở mức lớp và giáo viên; `org_id` giữ mặc định như chặng 3.
4. AI giữ giả lập trong CI; chất lượng AI thật đo bằng bench ngoài CI theo KPI v1.0 (QS5-27).

## 4. Thứ tự làm và phụ thuộc

Đồ thị phụ thuộc chính (→ nghĩa là "cần trước"):

- S3-E1 lớp, S3-E2 bộ bài, S3-E4 thông báo, S3-E5 công tắc AI lớp, S2-E3 đường kiểm chứng đề → mọi epic S5.
- US-S5E1-05 (giáo án đã duyệt) → US-S5E3-01 (đưa giáo án vào kho).
- US-S5E3-01 (kho) → US-S5E2-06 (soạn đề từ tài liệu), US-S5E5-03 (gợi ý tài liệu).
- US-S5E2-01 (ngân hàng) → US-S5E2-03 → US-S5E2-04 → US-S5E4-02 (giao bộ đề đã duyệt), US-S5E5-03.
- US-S5E5-04 (công tắc AI chấm) → US-S5E4-02 (chế độ Chạy song song) → US-S5E6-02.
- US-S5E4-01 … 06 → US-S5E6-01 … 06 → US-S5E5-02 (tổng hợp lỗi cần `CriterionAssessment` và loại lỗi).
- Vòng mềm US-S5E4-02 ↔ US-S5E6-06: US-S5E4-02 dựng trường "chế độ chấm" và luôn trả cổng "đóng" cho tới khi US-S5E6-06 xong (R5).

**Trình tự cho 1 dev** (lũy kế người-ngày):

| Bước | Việc | Người-ngày | Lũy kế | Lý do thứ tự |
|---|---|---|---|---|
| 1 | S5-E1 trọn | 15,5 | 15,5 | tài sản sẵn nhất (prompt 300 lượt), không cần ảnh hay trường pilot; ra giá trị sớm |
| 2 | US-S5E5-04 công tắc và hạn mức | 2,6 | 18,1 | điều kiện của chế độ chấm và chi phí AI mọi epic sau |
| 3 | S5-E4 trọn (đợt bật 1) | 20,8 | 38,9 | nền dữ liệu cho S5-E6; phát hành chờ QS5-01 |
| 4 | S5-E3 trọn | 19,5 | 58,4 | kho cần cho S5-E2-06; mức trợ giúp gắn vào màn tạo bài của S5-E4 |
| 5 | S5-E2 trọn | 18,1 | 76,5 | cần kho (US-S5E3-01) và bộ dựng tệp xuất của S5-E1 |
| 6 | S5-E6 trọn (đợt bật 2–3) | 18,2 | 94,7 | cần dữ liệu chấm tay thật của lớp pilot và kết quả bench (QS5-27) |
| 7 | US-S5E5-01, 02, 03 | 9,1 | 103,8 | tổng hợp lỗi cần điểm đã duyệt và loại lỗi |

Nếu có 2 dev: nhánh A = bước 1 → 4 → 5; nhánh B = bước 2 → 3 → 6 → 7; điểm gặp: US-S5E3-04 nối vào màn tạo bài của US-S5E4-02, US-S5E2-04 nối vào giao bài có chấm. Ước tính lịch ≈ 11–12 tuần thay vì 21.

## 5. Cổng phát hành đề xuất (theo STAGES §10 "ba đợt bật")
| Cổng | Gồm | Điều kiện |
|---|---|---|
| L5a | S5-E1, S5-E3, S5-E2, US-S5E5-01, 04 | check của từng epic đạt; bộ câu thử lộ đáp án (NFR-GEO-020) = 0 với kho tri thức |
| L5b | S5-E4 (nộp bài, chấm tay) | QS5-01 được owner chốt; có lớp pilot |
| L5c | S5-E6, US-S5E5-02, 03 | bench AI chấm đạt KPI §4 trên bộ mẫu; lớp đạt cổng US-S5E6-06 |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-28 | Tạo brief chặng 5, đánh giá lại workload, thứ tự làm (goal run) | agent spec-s5 (PO) |
