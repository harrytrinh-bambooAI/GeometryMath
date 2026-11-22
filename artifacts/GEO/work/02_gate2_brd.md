# BRD: GEO — Hình Học Không Gian Cùng AI (MVP)

> **Input snapshot:** như `00_inputs.md`. Mỏng và theo hướng "tại sao, kết quả gì"; logic chi tiết do BA viết sau.

| | |
|---|---|
| **Epic ID** | E1…E7 (feature GEO) |
| **Author / PO** | PO |
| **Status / Version** | Draft goal run · v0.1 |
| **Last updated** | 22-11-2026 |
| **Sources** | Brief: `Input/GeometryMath/GEO/brief.md` · Quyết định: `PO-DECISIONS.md` · Thiết kế: `Design/mvp-*.png`, `Design/prototype/` |
| **Priority (MoSCoW)** | Must cho 26 chức năng P0 và ràng buộc của chủ đầu tư |

## 1. VoC — yêu cầu thô
Toàn bộ 55 yêu cầu thô với id `REQ-GEO-01…55` nằm ở `00_gate0_intake_voc.md` (một nguồn, không chép lại).

## 2. Product vision
- **Đối tượng:** học sinh THCS, THPT cần hiểu hình không gian; giáo viên Toán cần giao bài, kiểm soát giờ mở lời giải, thấy lớp vướng ở đâu.
- **Vấn đề:** hình phẳng khó hình dung, đáp án đưa ngay, hình và đáp số AI có thể sai.
- **Tầm nhìn dài hạn:** hệ sinh thái giáo dục nhiều chặng (`DOMAIN-PROFILE.md` §7). MVP đặt nền: engine thuần hàm, cổng AI, `release_at`, nhật ký tương tác, `school_id`.

## 3. Mục tiêu và chỉ số thành công (đo được; ngưỡng là đề xuất)
Công thức ở `02_domain_model.md`.

| Mục tiêu | Chỉ số | Ngưỡng đề xuất | Mã |
|---|---|---|---|
| Hình đúng | Tỉ lệ hình thỏa ràng buộc trên bộ đề chuẩn | ≥ 95% | M-GEO-01 |
| Đáp số đúng | Tỉ lệ đáp số đúng trên bộ đề chuẩn | ≥ 90% | M-GEO-04 |
| Tin cậy nhãn | Bài sai mang "Đã kiểm chứng" | 0 | M-GEO-05 |
| Học trước, đáp án sau | Lần lộ đáp số trong bộ câu thử | 0 | M-GEO-06 |
| Kho đề | Số đề đã kiểm chứng | ≥ 40 | M-GEO-07 |
| Học bằng thao tác | Phiên có thao tác trên hình | ≥ 60% | M-GEO-08 |
| Tốc độ | Trung vị gửi đề tới có hình | < 40 giây (AI thật) | M-GEO-09 |
| Quy mô | Phiên đang hoạt động; tải đều; tải đỉnh | 1000; 200 đến 333,3; 100 yêu cầu/giây | M-GEO-10…12 |
| Hiệu năng | p95 đọc, p95 ghi, tỉ lệ lỗi | ≤ 300 ms; ≤ 500 ms; < 0,5% | M-GEO-20 |
| Gợi ý, chi phí | Lượt xem gợi ý trước lời giải; chi phí AI mỗi bài | đo, chưa đặt ngưỡng | M-GEO-16, 17 |

## 4. Phạm vi
IN và OUT kèm hệ quả: `01_gate1_vision_alignment.md` §3.

## 5. Quy tắc nghiệp vụ và yêu cầu nghiệp vụ (BR-GEO)
`BR-GEO-01…13` trùng `BR-01…13` của `DOMAIN-PROFILE.md` §8; `BR-GEO-14…20` là quy tắc đã nêu ở `PO-DECISIONS.md` hoặc brief.

| BR ID | Quy tắc (kết quả) | REQ | MoSCoW | Nguồn |
|---|---|---|---|---|
| BR-GEO-01 | Trước giờ mở, máy chủ không gửi lời giải và đáp số xuống thiết bị; ẩn ở giao diện là không đủ | 17, 40 | Must | SRC-048 |
| BR-GEO-02 | Lời giải khóa thì hỏi đáp chỉ gợi ý hoặc giải thích khái niệm, không nêu đáp số; câu trả lời kiểm tra trước khi trả về | 18 | Must | SRC-049 |
| BR-GEO-03 | Chỉ giáo viên của lớp được đặt, đổi, mở ngay giờ mở; chưa đặt thì vẫn khóa (suy diễn, D-GEO-043) | 16, 35 | Must | SRC-050 |
| BR-GEO-04 | Gợi ý đi trước lời giải: mức 1, mức 2, mức 3 | 08 | Must | SRC-051 |
| BR-GEO-05 | Bài tự học: mức 3 mở sau khi xem mức 1 và 2 (giả định, D-GEO-023) | 19 | Must | SRC-052 |
| BR-GEO-06 | Hình từ ràng buộc do engine tính tọa độ; không tin tọa độ AI | 02 | Must | SRC-053 |
| BR-GEO-07 | "Đã kiểm chứng" chỉ khi đáp số khớp tính toán; còn lại "Cần xem lại" | 04 | Must | SRC-054 |
| BR-GEO-08 | Đề lạ dùng tọa độ AI và hiện cảnh báo chưa kiểm chứng | 01, 04 | Must | SRC-055 |
| BR-GEO-09 | Engine chỉ kiểm chứng tính toán, không kiểm chứng lập luận; lời giải AI ghi "AI soạn" | 04, 29 | Must | SRC-056 |
| BR-GEO-10 | Kho đề MVP thuộc Giáo viên mẫu; lời giải AI đứng tên giáo viên này | 20, 29 | Must | SRC-057 |
| BR-GEO-11 | Lời giải đã giao không sửa tại chỗ; sửa thì version mới | 30 | Must | SRC-058 |
| BR-GEO-12 | Học sinh chỉ thấy bài của lớp mình và dữ liệu của mình | 28 | Must | SRC-059 |
| BR-GEO-13 | Giáo viên là người quyết định cuối | 34 | Must | SRC-060 |
| BR-GEO-14 | Nhãn hai trục: máy "Đã kiểm chứng" / "Cần xem lại", người "AI soạn" / "Giáo viên đã duyệt"; giáo viên không tự gắn nhãn máy | 04, 29 | Must | SRC-019 |
| BR-GEO-15 | Không xếp hạng ở mọi cấp | 33 | Must | SRC-021 |
| BR-GEO-16 | Công cụ đo ẩn đại lượng đề hỏi và đại lượng cùng giá trị, cùng vai trò trước giờ mở | 31 | Must | SRC-023, SRC-024 |
| BR-GEO-17 | Đề tự nhập trùng hoặc gần trùng bài đang khóa thì kế thừa khóa | 32 | Must | SRC-026 |
| BR-GEO-18 | Giờ mở áp dụng cho mọi kênh trả dữ liệu bài | 17 | Must | SRC-028 |
| BR-GEO-19 | Mọi truy vấn dữ liệu của học sinh, bài giao, sự kiện lọc theo trường; học sinh trường này không thấy dữ liệu trường khác | 28, 37 | Must | SRC-004 |
| BR-GEO-20 | Giáo viên chỉ thấy lớp mình dạy | 14, 28 | Must | `DOMAIN-PROFILE.md` §4.2 ("Giới hạn: không thấy lớp của giáo viên khác") |

## 6. Yêu cầu phi chức năng (REQ → ARCH viết `NFR-GEO-nnn`)
- **Hiệu năng:** REQ-GEO-38, 41; ngưỡng đề xuất (D-GEO-045). **Quy mô:** 1000 phiên đang hoạt động; tải đều 200 đến 333,3 yêu cầu/giây; đỉnh 100 yêu cầu/giây mở lời giải (M-GEO-10…12).
- **Đa trường, bảo mật:** REQ-GEO-37, 28, 40; phân quyền kiểm ở máy chủ.
- **Truy vấn, dữ liệu:** REQ-GEO-39, 30; lời giải tách bảng; bảng sự kiện chia theo thời gian.
- **Khả dụng:** REQ-GEO-42 (thiết bị), 48 (màn chờ).
- **Ngôn ngữ:** REQ-GEO-43. **Riêng tư:** REQ-GEO-44. **Công nghệ:** REQ-GEO-45, 46.
- Chưa có yêu cầu về sao lưu, khả dụng theo phần trăm thời gian chạy, truy cập khẩn cấp: không có nguồn (TRU-04 thuộc chặng 5); ARCH ghi là câu hỏi cho TechLead nếu cần.

## 7. Tác động hệ thống (module)
| Module | Tác động | Ghi chú |
|---|---|---|
| `tenancy` | mọi truy vấn có bối cảnh trường | nền cho E1 |
| `iam` | đăng nhập nhẹ, vai | E1, E6 |
| `geometry` | engine, hình mẫu, kiểm chứng | E2, E3 |
| `content`, `knowledge` | đề, lời giải, kho đề, bài giảng | E4, E5 |
| `learning`, `answers` | bài giao, giờ mở, khóa, hỏi đáp | E6 |
| `events` | ghi và đọc sự kiện | E7 |
| `ai-gateway` | mọi lượt gọi AI | E1 |

## 8. Khả năng mở rộng
Nền đặt sẵn (`PRODUCT-MASTER.md` B8): `schema_version`, `owner_id`, `school_id`, `release_at`, nhật ký tương tác, cổng AI đổi model. Không xây phân quyền nhiều tầng, kế thừa policy, liên trường. Không kéo tính năng chặng 2 đến 9 vào MVP.

## 9. Giả định và ràng buộc
Giả định đang dùng: bảng `03_decisions.md` mục B và C. Ràng buộc cứng: D-GEO-001…016, 031 (Final).

## 10. Phụ thuộc và rủi ro
| Rủi ro | Xử lý |
|---|---|
| Lời giải AI đứng tên giáo viên nhưng chưa có người duyệt (`MVP.md` §10) | nhãn "AI soạn"; người soát kho đề chưa xác định (D-GEO-055) |
| Engine không phủ hết dạng đề | đề lạ rơi về tọa độ AI kèm cảnh báo; đo M-GEO-18 |
| Lộ đáp số qua hỏi đáp | kiểm tra đầu ra; bộ câu thử; định nghĩa D-GEO-065 |
| AI giả lập khác AI thật | chỉ số tốc độ và độ đúng thật chưa đo được (D-GEO-047) |
| Định danh học sinh yếu (tên và mã lớp) | chấp nhận ở MVP (D-GEO-048); câu hỏi cho ChuDauTu |
| Giáo viên mẫu không cho biết giáo viên thật có dùng không | MVP chứng minh luồng, chưa chứng minh nhu cầu |
| Tải 1000 đồng thời và đỉnh mở lời giải | thiết kế bộ nhớ đệm, truy vấn theo khóa; thử tải; ngưỡng là đề xuất |

## 11. Open Questions & Assumptions register
Nguồn duy nhất là `questions_for_stakeholders.md`; bảng dưới chỉ trỏ.

| # | Câu hỏi | Giả định đang dùng | Người trả lời | Trạng thái |
|---|---|---|---|---|
| 1 | Ngưỡng và nghĩa của "1000 đồng thời" | D-GEO-044, 045 | ChuDauTu, TechLead | Open |
| 2 | Hạ tầng | D-GEO-046 | TechLead | Open |
| 3 | Nhà cung cấp AI | D-GEO-047 | ChuDauTu | Open |
| 4 | Định danh học sinh, cách vào của giáo viên mẫu | D-GEO-048, 051, 052, 073 | ChuDauTu, TechLead | Open |
| 5 | Dữ liệu thử tải | D-GEO-049 | TechLead | Open |
| 6 | Soạn, import, giáo trình riêng ngoài MVP | D-GEO-011 | ChuDauTu | Open |
| 7 | Dung sai, lộ đáp số, giờ mở, trợ giúp, công cụ đo | D-GEO-024, 027, 040…043, 053, 065, 070, 075, 077 | GiaoVienCoVan | Open |

## 12. VoC coverage report
- **Total REQ_IDs received:** 55
- **Mapped to the epic / NFR:** 51
- **Deferred / rejected (with reason):** REQ-GEO-51…54 (OUT: chặng 2 đến 9, hệ quả ở Gate 1 §3)
- **Coverage:** 100% (55 / 55 đã xử lý)
