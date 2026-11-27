Phiên bản: v0.1 · 2026-11-28
# Câu hỏi chặng 5 cho owner (kèm giả định đang dùng)

> Chủ sở hữu: PO (goal run). Mỗi câu có giả định của PO để việc không dừng; AC dựa trên giả định ghi mã `QS5-nn`. Owner trả lời "OK hết" hoặc sửa từng dòng; câu nào đổi thì PO ghi PO-nn mới và BA cập nhật AC theo mã. Không gửi tệp này cho ai; người dùng chuyển tiếp.
> Mức tin cậy: cao = có nguồn trực tiếp; vừa = suy từ nguồn gần; thấp = chưa có nguồn, chọn mặc định an toàn.

## Assumptions in use (giả định đang dùng)

| Mã | Câu hỏi | Giả định đang dùng (tin cậy · căn cứ) | Ảnh hưởng nếu đổi | Hỏi ai |
|---|---|---|---|---|
| QS5-01 | **PO-64 cấm thu ảnh bài làm trước khi có quyết định riêng. Cho phép thu ảnh bài viết tay ở chặng 5 với điều kiện nào?** | Cho phép với: thông báo riêng tư phiên bản S5 học sinh bấm "Em đã hiểu" trước lần nộp đầu; nhắc không ghi họ tên, số điện thoại lên bài; ảnh lưu trong nước, mã hoá khi lưu, chỉ giáo viên chủ lớp xem; giữ tới hết năm học rồi xoá; xoá tài khoản thì xoá ảnh. Code làm trước, **phát hành S5-E4/S5-E6 chờ chốt** (vừa · PO-64, PO-61, STAGES Ch5 §9 "thỏa thuận với trường pilot") | US-S5E4-03 BR-11; cả S5-E4, S5-E6 không phát hành | Chủ đầu tư, tư vấn pháp lý |
| QS5-02 | Không làm multi-tenant ở chặng 5: công tắc "theo trường, môn" (STAGES §4, C5-09) thu về theo lớp? | Có: công tắc và báo cáo AI theo lớp, giáo viên chủ lớp điều khiển; `org_id` mặc định như chặng 3 (cao · brief giao việc "không đặc tả multi-tenant") | US-S5E5-04, US-S5E6-06 | Chủ đầu tư |
| QS5-03 | Kiến thức cấp cho AI soạn giáo án gồm cả đơn vị trạng thái `nhap-tam` (chưa soát chéo) hay chỉ `da-kiem`? | Cả hai cho tới khi kho 3 tầng đạt ≥ 90 % `da-kiem` (PLAN-kho-bai-giang), sau đó chỉ `da-kiem` (vừa · prompt-lab dùng `kien-thuc.json` có `nhap-tam`) | BR-S5E1-05 | PO, người soát kho |
| QS5-04 | Hạn mức lượt AI? | Soạn (giáo án, vá, đề, thông báo): 30 lượt/giáo viên/ngày; mỗi lớp/ngày: 300 lượt hỏi đáp học sinh, 200 lượt chấm (đọc chữ + đề xuất) (thấp · chưa có số chi phí thật; prompt-lab giáo án ≈ 19k token/lượt) | BR-S5E1-08, BR-S5E5-12 | Chủ đầu tư |
| QS5-05 | Định dạng xuất giáo án và đề? | DOCX (Apache POI) và PDF (pdfbox có sẵn); DOCX là chính vì giáo viên chỉnh tiếp theo mẫu tổ (vừa · thói quen giáo viên VN; PO-77 có pdfbox) | US-S5E1-05, US-S5E2-05 | Chủ đầu tư, TechLead |
| QS5-06 | Lắp đề: viết lại `lap_de.py` bằng Java trong khối chính, hay chạy Python cạnh máy chủ? | Viết lại bằng Java (ADR-GEO-01 một khối, không thêm tiến trình); giữ `check.py` làm bộ kiểm đối chứng trong CI (vừa · ADR-GEO-01) | US-S5E2-03 (+1–2 ngày nếu chọn Python sidecar vì hạ tầng) | TechLead |
| QS5-07 | Số mã đề tối đa mỗi lần lắp? | 1–8 (đủ cho phòng thi 24 chỗ xếp xen kẽ; `lap_de.py` thử tới 600 lần/mã) (thấp) | BR-S5E2-04 | PO |
| QS5-08 | Bộ đề máy lắp từ ngân hàng mang nhãn người gì trước khi duyệt? PO-02 chỉ có "AI soạn" / "Giáo viên đã duyệt". | Thêm giá trị "Máy lắp" (không phải AI viết, nhưng chưa có người duyệt) (vừa · PO-02 nhằm phân biệt ai chịu trách nhiệm nội dung) | BR-S5E2-10 | Chủ đầu tư |
| QS5-09 | Truy xuất kho tri thức bằng gì? Định dạng tệp? | Tách tài liệu thành mục theo tiêu đề; tìm kiếm toàn văn PostgreSQL (cấu hình tiếng Việt bỏ dấu) + ưu tiên mục cùng mã kiến thức `KT11-…`; tối đa 5 mục/lượt; tệp docx, pdf, md ≤ 10 MB; chưa dùng vector (vừa · ADR-GEO-01, không thêm hạ tầng; PLAN-kho-bai-giang "mục ngắn rõ cho RAG") | US-S5E3-01, 03 | TechLead |
| QS5-10 | PO-43 (bài giảng AI viết chưa duyệt vẫn làm nguồn) có áp cho kho tri thức của giáo viên không? | Không: PO-43 chỉ áp cho bài giảng mẫu của hệ thống; kho giáo viên chỉ dùng phiên bản "Đang dùng" (STAGES Ch5 §7) (vừa · PO-43 tự ghi "cần chủ đầu tư xác nhận") | BR-S5E3-05, 08 | Chủ đầu tư |
| QS5-11 | Gắn tài liệu cho lớp theo tài liệu (luôn dùng bản Đang dùng mới nhất) hay ghim một phiên bản? | Theo tài liệu; mỗi câu trả lời lưu đúng phiên bản đã dùng để truy lại (vừa · STAGES §7 "lưu nguồn") | BR-S5E3-06 | PO |
| QS5-12 | Mức trợ giúp mặc định của bài? | Bài có chấm: Tắt; bài luyện: Gợi ý và khái niệm (vừa · C5 "trong lúc làm bài AI chỉ gợi ý") | BR-S5E3-12 | PO |
| QS5-13 | Tài liệu của giáo viên chứa đáp số của bài đang khoá: lọc mục đó khỏi AI hay chặn cả tài liệu? | Lọc từng mục khớp KS-1/KS-2 và báo giáo viên (vừa · ADR-GEO-14) | BR-S5E3-16 | PO |
| QS5-14 | Giữ nhật ký hỏi đáp và quyết định (`AIInteraction`, `AIPolicyDecision`) bao lâu? | Tới hết năm học như dữ liệu bài làm; xoá tài khoản thì xoá phần gắn với em đó (vừa · PO-64 Q8) | BR-S5E3-18 | Chủ đầu tư, pháp lý |
| QS5-15 | Ràng buộc rubric? | 1–10 tiêu chí, điểm bội 0,25, thang mặc định 10; hai mẫu hệ thống "Tính toán", "Chứng minh" suy từ KPI §1 (vừa · KPI v1.0) | US-S5E4-01 | PO, giáo viên pilot |
| QS5-16 | Ba chế độ chấm (Chấm tay · Chạy song song · AI đề xuất) chọn theo từng bài? | Có, theo từng bài có chấm; AI đề xuất chỉ chọn được khi lớp đạt cổng (vừa · STAGES §1, §10) | BR-S5E4-05 | PO |
| QS5-17 | Đổi hạn, đổi rubric sau khi đã có bài nộp? | Hạn chỉ được kéo dài; rubric không đổi (vừa · công bằng giữa các em) | BR-S5E4-06 | PO |
| QS5-18 | Lưu ảnh ở đâu? | Kho lưu đối tượng tương thích S3 đặt tại Việt Nam, mã hoá khi lưu, URL ký ngắn hạn 5 phút, không công khai (thấp · STAGES §9 "chốt nơi lưu ảnh") | US-S5E4-03 | TechLead, pháp lý |
| QS5-19 | Giới hạn ảnh nộp? | 1–10 trang, ≤ 8 MB/trang sau nén, ngưỡng độ rõ 0,5 (thấp) | BR-S5E4-07 | PO, TechLead |
| QS5-20 | Đọc chữ viết tay bằng gì? Ngưỡng "chưa chắc"? | Claude vision qua `aigateway` (task `grade_ocr`), như PO-65 Q4 cho ảnh đề; đoạn tin cậy < 0,6 tô vàng (vừa · PO-65) | BR-S5E4-12 | TechLead |
| QS5-21 | Cần bao nhiêu bài chạy song song trước khi mở cổng "AI đề xuất" cho một lớp? | ≥ 20 bài, đạt KPI §4 (KPI ghi "giáo viên chấm đối chứng ~10 bài" cho bench; lớp thật cần nhiều hơn để KTC hẹp) (vừa) | BR-S5E6-16 | Chủ đầu tư |
| QS5-22 | Ngưỡng tin cậy để AI tự từ chối chấm? | Tổng < 0,6 (thấp · chỉnh sau bench QS5-27) | BR-S5E6-05 | PO |
| QS5-23 | Phím tắt duyệt? | J/K, A, E, X, Enter (thấp · STAGES §3 phản biện) | BR-S5E6-11 | Designer |
| QS5-24 | Sau khi bật AI đề xuất: lấy mẫu chạy song song bao nhiêu, khi nào tự đóng cổng? | 10 % bài mỗi bài có chấm; hai đợt liên tiếp dưới ngưỡng thì tự đóng (thấp · STAGES §10) | BR-S5E6-17 | Chủ đầu tư |
| QS5-25 | Thông báo trợ lý soạn, sau khi giáo viên gửi, có ghi "AI soạn" cho học sinh thấy không? | Không: giáo viên đã sửa và tự gửi nên đứng tên giáo viên; nháp vẫn lưu nhãn "AI soạn" trong lịch sử (vừa · PO-02 áp cho nội dung chờ duyệt) | BR-S5E5-02 | Chủ đầu tư |
| QS5-26 | Mặc định công tắc AI của lớp mới? | AI chấm: tắt; Trợ lý: bật; AI phía học sinh: theo S3-E5 (vừa · STAGES §1) | BR-S5E5-10 | PO |
| QS5-27 | Có chạy bench AI chấm bài viết tay (prompt-lab `grade`, `grade_ocr`) trước khi code S5-E6? | Có: 5 người-ngày + 3–5M token API, cần ≥ 30 bài viết tay có điểm giáo viên; nằm ngoài ước lượng 103,8 (vừa · DECISIONS 27/11 "đo trước rồi mở rộng") | S5-E6 lịch và rủi ro R2 | Chủ đầu tư |
| QS5-28 | Điều kiện mở chặng 5 "có ít nhất một trường cam kết pilot" (D1) chặn cả chặng hay chỉ phần chấm bài? | Chỉ chặn phát hành S5-E4 và S5-E6; S5-E1, S5-E2, S5-E3, US-S5E5-01, 04 phát hành không cần pilot (vừa · các phần này không thu dữ liệu học sinh mới) | brief §5 cổng L5a–L5c | Chủ đầu tư |
| QS5-29 | Phúc khảo và duyệt nhận xét theo cụm (STAGES P1) có vào chặng 5 không? | Không vào ước lượng chặng 5; đưa vào backlog sau L5c (vừa · STAGES §4 P1, giữ chặng vừa sức) | +2 story ≈ 5 người-ngày nếu kéo vào | Chủ đầu tư |

## Đề xuất cho owner (chưa áp dụng)
| Đề xuất | Căn cứ |
|---|---|
| Ghi PO mới chốt việc tách S5-E4 / S5-E6 theo đợt bật và thứ tự làm ở `brief-s5.md` §4 | STAGES §10; brief §1 |
| Duyệt bench AI chấm (QS5-27) trước khi bắt đầu S5-E6 | rủi ro R2 `estimate-s5.md` |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-28 | 29 câu, mỗi câu có giả định đang dùng (goal run) | agent spec-s5 (PO) |
