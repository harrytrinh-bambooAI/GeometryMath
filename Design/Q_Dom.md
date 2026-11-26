Phiên bản: v0.3 · 2026-11-26 · GEO — câu hỏi cho Designer (một danh sách, trả lời một lần)
Nguồn: `Design/s23/DQ.md` v0.1 (T-261126-10, 26/11), `artifacts/GEO/work/s23/story-map-s23.md` (T-08, 26/11), `.claude/plans/STAGE-2-3-PLAN.md` v0.2, `STAGES-AFTER-MVP.md` (22/11).
Cách trả lời: mỗi câu ghi OK (giữ mặc định) hoặc đáp án mới. Câu nào không trả lời → giữ mặc định TẠM.

## A. Cách làm việc chung (mọi chặng)
| # | Câu | Mặc định đang dùng (TẠM) |
|---|---|---|
| A1 | Nguồn thiết kế chính là gì: Figma hay bản mẫu HTML `Design/prototype/`? Khi lệch giữa thiết kế và app, bên nào thắng? | HTML prototype là nguồn; app theo prototype; Figma (nếu có) phải kèm link + ngày cập nhật |
| A2 | Hệ token (màu, chữ, khoảng cách, bo góc): giữ token hiện tại (`Design/prototype/s23-base.css`, prototype MVP) hay dựng design system mới? Có dark mode? | giữ token hiện tại; chưa có dark mode |
| A3 | Bàn giao mỗi màn gồm gì? | PNG 3 khổ (web 1440×900, iPad 1180×820, iPhone 390×844) + danh sách trạng thái (rỗng, đang tải, lỗi, mất mạng) + câu DQ có mặc định |
| A4 | Thiết bị theo vai cho chặng 4+ (phụ huynh, nhà trường) | giáo viên web; học sinh iPad chính / iPhone phụ; phụ huynh iPhone; nhà trường web |
| A5 | Chuẩn tiếp cận: cỡ chữ tối thiểu cho học sinh, tương phản, vùng chạm, hiển thị công thức toán tiếng Việt (dấu phẩy thập phân, ký hiệu góc, độ) | chữ thân ≥ 16px, vùng chạm ≥ 44px, WCAG AA, dấu phẩy thập phân |
| A6 | Ai duyệt thiết kế, và ngưỡng so ảnh khi nghiệm thu app với bản mẫu | owner/PO duyệt; so ảnh lệch ≤ 2 % điểm ảnh ở màn tĩnh |
| A7 | Thiết kế đi trước spec (design-first) hay theo story map của PO? | theo story map; Designer đề xuất đổi story qua DQ |
| A8 | Nơi Designer ghi câu hỏi / quyết định | `Design/<chặng>/DQ.md`, cùng mẫu `Design/s23/DQ.md` |

## B. Chặng 2–3: 28 mặc định cần Designer rà (chi tiết ở `Design/s23/DQ.md`)
Ưu tiên rà trước (ảnh hưởng spec / code nhiều nhất):
| # | Câu | Mặc định | Màn |
|---|---|---|---|
| DQ-02 | Ảnh đề mờ: chặn hay cho đi tiếp? | không chặn; tô vàng ô chưa chắc, giáo viên sửa | C2-02 |
| DQ-04 | Đáp số đề ghi lệch máy tính lại | chặn duyệt, bảng đối chiếu 3 cột, 3 cách xử lý | C2-02/03 |
| DQ-05 | Giáo viên duyệt được lời giải chưa kiểm chứng? | được, nhãn "Cần xem lại" giữ nguyên | C2-03 |
| DQ-06 | Sửa lời giải đã giao | tạo phiên bản mới; lớp đã giao giữ bản cũ đến khi "Áp dụng cho lớp" | C2-03 |
| DQ-11 | Bàn phím toán | bàn phím riêng 6 cột, iPad và iPhone cùng bố cục | C2-06 |
| DQ-12 | Lượt thử, sai số | "Còn n/3 lượt", "±0,5°", sai thì gợi ý hướng, không lộ đáp số | C2-06 |
| DQ-16 | Hiện mã sau đăng ký | `GV-XXXX-XXXX` / `HS-…` + mã khôi phục hiện một lần, bắt tick đã lưu | C3-01 |
| DQ-17 | Học sinh tự học (không lớp) quên mật khẩu | chưa có đường khôi phục → tạo tài khoản mới (Claude nghiêng: cấp mã khôi phục khi tự học) | C3-01 |
| DQ-22 | "Cả lớp vướng ở đâu" khi nào hiện | ≥ 5 bài làm / đề; nhãn "AI gộp" + "Cần thầy cô xác nhận" | C3-04 |
| DQ-26 | Đặt lại mật khẩu học sinh | hộp xác nhận → mật khẩu tạm hiện một lần → em tự đặt lại | C3-07 |
Còn lại: DQ-01, 03, 07–10, 13–15, 18–21, 23–25, 27, 28 — OK cả lô hoặc sửa từng câu.

## C. Màn còn thiếu ở chặng 2–3 (cần Designer vẽ)
| # | Màn | Vì sao cần | Ưu tiên |
|---|---|---|---|
| C1 | S2-E4: chat chỉnh nháp đề với AI (AI hỏi lại, hiện trước/sau) + import docx/pdf tách bài + đối chiếu trùng kho | story map ghi "khung do task 10 đề xuất", chưa có ảnh | P1 |
| C2 | S2-E9: biến thể đề, xuất ảnh/PDF, liên kết chỉ xem, trắc nghiệm | như trên | P1 |
| C3 | Bài luyện: trạng thái đang tải, mất mạng khi gửi đáp số | chưa vẽ | P0 |
| C4 | C3-07: học sinh nhập mã lớp sai / bị từ chối | chỉ có dòng chữ mô tả | P0 |
| C5 | Bàn phím biểu thức nâng cao (phân số, căn lồng, mũ) | chưa vẽ; ảnh hưởng chấm dạng tương đương | P0 |
| C6 | S3-E4: danh sách thông báo trong ứng dụng | chưa có ảnh riêng | P0 |
| C7 | S3-E6 (P1): chữa bài từ kết quả → Trình chiếu, xuất kết quả lớp, lưu trữ lớp cuối năm, nhắc việc | chưa vẽ | P1 |
| C8 | Trạng thái rỗng / lỗi chung (lớp chưa có học sinh, kho trống, AI lỗi, hết hạn phiên) cho các màn C2/C3 | chưa có bộ chung | P0 |

## D. Chặng 4–9 (Designer mới phụ trách) — cần chốt trước khi vẽ
| # | Câu | Mặc định |
|---|---|---|
| D1 | Thứ tự chặng Designer bắt đầu (rubric chấm, nộp bài viết tay, phụ huynh, trường/tenant, liên trường, giáo trình riêng học sinh) | theo thứ tự `STAGES-AFTER-MVP.md` |
| D2 | Nộp bài viết tay: chụp ảnh trên iPad/iPhone hay viết trực tiếp bằng bút (Apple Pencil)? | chụp ảnh; bút để sau |
| D3 | Phụ huynh: ứng dụng riêng hay chế độ xem trong cùng ứng dụng? Có thông báo đẩy? (chặng 2–3 chỉ có thông báo trong ứng dụng) | chế độ xem trong cùng ứng dụng, chỉ thông báo trong ứng dụng |
| D4 | Trường/tenant: nhận diện thương hiệu trường (logo, màu) có đổi giao diện không? | chỉ logo + tên trường, không đổi màu |
| D5 | PO-61 (không thu dữ liệu cá nhân) có giữ cho phụ huynh và nhà trường không? Nếu cần liên hệ phụ huynh thì qua đâu? | giữ; liên hệ qua giáo viên ngoài hệ thống |
| D6 | Rubric: giáo viên chấm trên màn nào (bảng lớp × bài hiện có hay màn chấm riêng)? Hiện điểm số cho học sinh? (chặng 2–3: không điểm, không xếp hạng) | màn chấm riêng mở từ ô bảng lớp; không xếp hạng |

## Trả lời Dom (Designer Dom, v0.3 · 26/11)
Ký hiệu: OK = giữ mặc định; CẦN OWNER = owner/PO phải chốt. Ảnh ở `Design/s23/`; câu mới ở `Design/s23/DQ.md` v0.2 (DQ-29…43); nhận xét ở `Design/s23/REVIEW-Dom.md`.

### Trả lời A
| # | Trả lời |
|---|---|
| A1 | OK. HTML prototype là nguồn; lệch thì prototype thắng, app sửa theo. Figma chỉ khi có link + ngày. |
| A2 | OK, giữ token hiện tại, không dark mode ở chặng 2–3 (REVIEW R-16). Đổi: chữ phụ học sinh nâng lên 13–14 px (R-02). |
| A3 | OK. Thêm: nhãn nội bộ (TẠM, DQ) không vào ảnh giao Dev (DQ-37). |
| A4 | OK. |
| A5 | OK, đã áp: chữ ≥ 14 px chữ phụ, đề 16 px iPad, phím ≥ 44 px (R-02, R-03). Đo tương phản chưa chạy (R-14). CẦN OWNER: có chấp nhận chữ phụ 13–14 px thay vì 16 px không. |
| A6 | CẦN OWNER: người duyệt và ngưỡng 2 % là quyết định của owner; Dom đề xuất giữ 2 %. |
| A7 | OK, theo story map; Dom đề xuất đổi qua DQ (đã có DQ-29…43). |
| A8 | OK. |

### Trả lời B (chặng 2–3)
| # | Trả lời |
|---|---|
| DQ-02 | OK (không chặn, tô vàng). Ảnh `c2-02-anh-mo.png`. |
| DQ-04 | Giữ chặn duyệt, nhưng sửa màu: tô cảnh báo ở ô lệch, không tô đỏ hàng máy (R-06). CẦN OWNER. |
| DQ-05 | OK. |
| DQ-06 | OK, thêm nút "Áp dụng cho lớp" + so sánh v2↔v3 (DQ-30). CẦN OWNER: màn so sánh có vẽ không. |
| DQ-11 | OK; thêm bàn phím nâng cao (DQ-34). |
| DQ-12 | OK; hết lượt có lối đi tiếp (R-04). |
| DQ-16 | OK; thêm trạng thái khóa (DQ-43). |
| DQ-17 | Nghiêng cấp mã khôi phục khi tự học. CẦN OWNER (ảnh hưởng PO-61). |
| DQ-22 | OK, giữ ngưỡng ≥ 5 bài làm. CẦN OWNER: ngưỡng 5. |
| DQ-26 | OK. |
| Còn lại (01, 03, 07–10, 13–15, 18–21, 23–25, 27, 28) | OK cả lô; DQ-27 bổ sung màn lỗi (DQ-36). |

### Trả lời C (đã vẽ)
| # | Ảnh đã vẽ |
|---|---|
| C1 | `c2-04a-chat-chinh-nhap`, `c2-04b-import-tach-bai`, `c2-04c-doi-chieu-trung`, `c2-04d-import-loi-tep` (DQ-31…33) |
| C2 | `c2-09a-bien-the`, `c2-09b-xuat-lien-ket`, `c2-09c-trac-nghiem-gv`, `c2-09d-trac-nghiem-hs` (DQ-38, 39) |
| C3 | `c2-06-dang-tai`, `c2-06-mat-mang` (DQ-35) |
| C4 | `c3-07-ma-sai`, `c3-07-bi-tu-choi` (DQ-36) |
| C5 | `c2-08-ban-phim-nang-cao` (DQ-34) |
| C6 | `c3-04-thong-bao-ipad`, `c3-04-thong-bao-iphone` (DQ-40) |
| C7 | `c3-06a-chua-bai-trinh-chieu`, `c3-06b-xuat-ket-qua`, `c3-06c-luu-tru-lop` (DQ-41). Nhắc việc: chưa vẽ riêng. |
| C8 | `c8-trang-thai-gv`, `c8-trang-thai-hs` (DQ-42) |

### Trả lời D (chỉ hướng, chưa vẽ)
| # | Trả lời |
|---|---|
| D1 | OK, theo `STAGES-AFTER-MVP.md`. CẦN OWNER: xác nhận thứ tự. |
| D2 | OK, chụp ảnh trước; bút để sau. CẦN OWNER. |
| D3 | OK, chế độ xem trong cùng ứng dụng, chỉ thông báo trong ứng dụng. CẦN OWNER (ảnh hưởng PO-61 và kênh liên lạc). |
| D4 | OK, chỉ logo + tên trường. CẦN OWNER. |
| D5 | OK, giữ PO-61 cho phụ huynh và nhà trường; liên hệ qua giáo viên. CẦN OWNER xác nhận. |
| D6 | OK, màn chấm riêng mở từ ô bảng lớp; không xếp hạng. CẦN OWNER: có hiện điểm cho học sinh không. |

Tổng: A 8/8, B 28/28 (10 ưu tiên + 18 cả lô) đã trả lời; C 8/8 đã vẽ; D 6/6 trả lời hướng. Số CẦN OWNER: A 2 (A5, A6), B 4 (DQ-04, 06, 17, 22), D 6, DQ-38 1 = 13.

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.2 | 2026-11-26 | Đổi tên DESIGNER-QUESTIONS.md → Q_Dom.md theo yêu cầu owner | delivery-lead |
| v0.1 | 2026-11-26 | Bản đầu: A cách làm, B 10 DQ ưu tiên + 18 còn lại, C 8 màn thiếu, D 6 câu chặng 4–9 | delivery-lead |
| v0.3 | 2026-11-26 | Thêm mục "Trả lời Dom" cho A, B, C (trỏ ảnh), D (hướng) | runner-design-dom |
