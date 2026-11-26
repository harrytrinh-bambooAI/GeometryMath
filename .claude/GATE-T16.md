Phiên bản: v0.1 · 2026-11-26
# Gate T-261126-16 · Danh sách owner quyết (R3 spec chặng 2–3) — gửi MỘT lần

Cách trả lời: `OK hết` = nhận toàn bộ cột **Nghiêng**; hoặc ghi mã + lựa chọn khác (vd `G1 hai mô hình, G4 b`). Sau khi trả lời, phiên chính ghi DECISIONS "S23 R3 OK" → mở T-17 (PLAN_code, Opus).

Nguồn đã gộp, bỏ trùng: `.claude/NEEDS.md` §C (C1–C9) · `artifacts/GEO/work/review/R3_s23_quickview.md` + `RV_s23.md` v0.2 (B-1, M-01, M-08, M-09, M-10, m-02) · `Design/Q_Dom.md` v0.3 + `Design/s23/DQ.md` v0.2 (13 CẦN OWNER). Số liệu spec: 56 story, 723 AC / 728 TC, `check_ac_tc` 0 lỗi; 0 blocker/major mở ngoài các mục dưới.

## 1. Chặn code chặng 2–3 (phải chốt)
| Mã | Câu hỏi | Lựa chọn | **Nghiêng** | Gộp từ |
|---|---|---|---|---|
| G1 | Bài luyện S2-E8 và bộ bài S3-E2: một mô hình hay hai? Chặng 2 giao bài cho lớp nào khi lớp thật tạo ở S3-E1? | (a) một mô hình "bộ bài", chặng 2 giao cho lớp mẫu 11A1 từ MVP · (b) hai mô hình song song | **(a)** | B-1, C9b |
| G2 | Lời giải Kho chung: giáo viên xem ngay hay theo giờ mở? Học sinh tự học (không lớp) theo luật nào? | (a) GV xem ngay; HS tự học khóa lời giải, chỉ gợi ý · (b) mọi vai theo giờ mở (PO-63) | **(a)** | M-01, C9a |
| G3 | Học sinh tự học / Chờ duyệt quên mật khẩu | (a) cấp mã khôi phục cho cả học sinh tự học · (b) chấp nhận tạo tài khoản mới (mất bài làm) | **(a)** — rẻ, giữ PO-61 | M-09, C6, DQ-17 |
| G4 | Chống dò mã lớp `HK-` + 4 ký tự | (a) giới hạn tổng lượt thử theo tài khoản + bắt buộc GV duyệt em vào lớp · (b) mã dài hơn (6–8 ký tự) · (c) cả hai | **(a)** (duyệt đã có trong spec) | M-08 |
| G5 | Thêm story ngoài story map (CR): xóa tài khoản + chính sách lưu/xóa (PO-64); US-S3E1-05; trạng thái "Bị từ chối"; quyền xuyên suốt S3; vòng đời bộ bài Đã đóng/Lưu trữ | nhận vào chặng 2–3 / bỏ ra sau | **nhận cả**; tick xóa tài khoản ở S2-E1 trỏ văn bản S3-E5 | M-10, m-02, C8b, C8c, C9c |
| G6 | `org_id` kỹ thuật từ MVP có trái PO-61 (không dữ liệu cá nhân) không | giữ / bỏ | **giữ** (kỹ thuật, không phải dữ liệu cá nhân) | C8c |
| G7 | Code chặng 2 chờ đo thật T-05 (B7) — owner chạy lệnh đo (~$3, xem PROGRESS "Việc owner") | chạy đo trước code / cho code song song với mock | **chạy đo trước** (Q1) | C4-Q1, A1 |

## 2. Xác nhận giả định TẠM (OK = giữ)
| Mã | Nội dung | **Nghiêng** | Gộp từ |
|---|---|---|---|
| G8 | PO-62…65 bỏ TẠM; Q1–Q9 plan §5 theo cột Nghiêng | OK | C4, C5 |
| G9 | 3 giả định mới của soát: BR-S3E1-16 (lớp MVP chuyển thành lớp chặng 3), bộ đếm IP tạm (không lưu IP), khóa `problem_image` | OK | R3 §6 |
| G10 | "Bài làm" tính cả em Đang làm; "Cả lớp vướng ở đâu" chỉ hiện khi ≥ 5 bài (cấu hình được) | OK | C8a, C7, DQ-22 |
| G11 | Đáp số vô tỉ (4√3) nộp 6,93: Sai ở kiểu "chính xác" + gợi ý viết dạng chính xác | OK | C9d |
| G12 | Ngưỡng TẠM: 50 ký tự, 15 chữ số, 2000 ký tự, 30 bước, dung sai 0,01; tối đa 20 lớp; p95 2 giây; giữ thông báo 90 ngày | giữ, chỉnh sau L6 | C9e, R3 |
| G13 | GV duyệt được lời giải chưa máy kiểm chứng (nhãn "Cần xem lại"); chặn duyệt khi lệch đáp số nhưng tô cảnh báo ô lệch, không tô đỏ cả hàng | OK | C7/DQ-05, DQ-04 |
| G14 | Liên kết chỉ xem (xuất đề): không cần đăng nhập, ẩn lời giải chưa mở, GV thu hồi được, **không** thu dữ liệu người xem | OK | DQ-38 |
| G15 | Câu MVP còn treo: QF-E2-D10, Dev-E7-10, D-GEO-184, NFR-GEO-056; PO-51…57 · gói 46 đề: prod dùng lệnh nạp riêng · đề thiết diện ngoài kho MVP | OK | C1, C2, C3 |
| G16 | 25 DQ còn lại + DQ-29…43 theo mặc định trong `Design/s23/DQ.md` | OK | C7 |

## 3. Thiết kế (Designer Dom)
| Mã | Câu hỏi | **Nghiêng** |
|---|---|---|
| G17 | Chữ phụ 13–14 px (thay 16 px) | chấp nhận ≥ 14 px |
| G18 | Ai duyệt thiết kế; ngưỡng so ảnh app ↔ bản mẫu | owner/PO; lệch ≤ 2 % điểm ảnh màn tĩnh |
| G19 | Có vẽ màn so sánh lời giải v2↔v3 (DQ-06/30) | có, lô thiết kế sau |

## 4. Không chặn gate (chặng 4–9, Dom trả lời hướng) — trả lời khi tiện
D1 thứ tự chặng theo STAGES-AFTER-MVP · D2 chụp ảnh trước, bút sau · D3 phụ huynh xem trong app, chỉ thông báo trong app · D4 nhận diện trường chỉ logo + tên · D5 giữ PO-61 cho phụ huynh/nhà trường · D6 chấm riêng mở từ ô bảng lớp, không xếp hạng — **có hiện điểm cho học sinh không?**

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo: gộp NEEDS §C + RV_s23/R3 + Q_Dom v0.3 thành 19 mục gate + 6 mục không chặn | delivery-lead (phiên chính) |
