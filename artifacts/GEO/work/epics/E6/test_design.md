# E6 — thiết kế kiểm thử (giao bài và khóa lời giải)
> Chủ sở hữu: QC. Phần chung (danh mục CL, kỹ thuật, bảng quyết định khóa lời giải DT-n, biên giờ mở BV-n, thang rủi ro, dữ liệu `F-GEO-200…299`, bảng giải thích mã) ở `review/QC_test_design.md`; tệp này chỉ ghi phần riêng của epic. Lập từ `work/04_story_map.md` và `work/03_gate3_preliminary_stories.md`, không chờ AC; dòng thiết kế nào chưa có AC tương ứng sẽ được đối chiếu ở `qc_findings.md`.

## Applicability map (mỗi nhóm CL: Y / N kèm lý do)
| Group | Screen / story | Applies (Y/N) | Reason if N |
|---|---|---|---|
| CL-FN-01 Luồng chính | US-GEO-601…612 | Y | |
| CL-FN-02 Dữ liệu nhập | US-GEO-604, 605 (giờ mở) | Y | |
| CL-FN-03 Lỗi và thông báo | US-GEO-602, 604, 605 | Y | |
| CL-FN-04 Chuyển trạng thái | US-GEO-604, 605, 612 (trạng thái giờ mở) | Y | |
| CL-FN-05 Phân quyền theo vai | US-GEO-602, 605 (chỉ giáo viên của lớp), 606, 607, 608 | Y | |
| CL-FN-06 Cách ly trường | US-GEO-602, 606, 608 | Y | |
| CL-FN-07 Dữ liệu máy chủ trả về | US-GEO-606, 607, 608, 609, 612 | Y | |
| CL-FN-08 Phân trang | US-GEO-606 (Bài của em) | Y | |
| CL-FN-09 Thời gian | US-GEO-604, 605, 606, 608, 612 | Y | |
| CL-FN-10 Đồng thời | US-GEO-605 (đổi giờ và Mở ngay cùng lúc), 602 (giao hai lần) | Y | |
| CL-FN-11 Nhãn kiểm chứng | — | N | nhãn ở E2, E5 |
| CL-FN-12 Định dạng | US-GEO-604, 606 (giờ `HH:mm, dd/MM/yyyy` giờ Việt Nam) | Y | |
| CL-FN-13 Sự kiện | US-GEO-608, 612 (`unlock` khi xem lời giải) | Y | |
| CL-FN-14 Cổng AI | US-GEO-609, 610 | Y | |
| CL-UI-01 Thiết bị | US-GEO-602…607, 612 | Y | |
| CL-UI-02 Chuỗi hiển thị | US-GEO-604, 606 ("Chưa đặt giờ", "Sắp mở", "Đã mở", "Mở ngay") | Y | |
| CL-UI-03 Ngoài phạm vi | US-GEO-604 ("Sau buổi học tới"), 606 (thẻ chuỗi ngày, "Em đang học tới đâu"), 602 (giao bộ đề) | Y | |
| CL-UI-04 Gián đoạn | US-GEO-612 (mạng chập chờn, thiết bị ngủ lúc tới giờ) | Y | |
| CL-UI-05 Cảm ứng | — | N | thao tác hình ở E3 |
| CL-X-01 Tải | US-GEO-608 (đỉnh mở lời giải) | Y | case tải đặt ở E1 (US-GEO-107), trỏ NFR |
| CL-X-02, 03 | US-GEO-606, 608 | Y | |
| CL-X-04 Dữ liệu trẻ em | — | N | không thu thêm dữ liệu cá nhân |

## Design rows: `CL id · condition · expected rule · technique · level`
| CL id | Story | Condition | Expected rule (nguồn) | Technique | Level |
|---|---|---|---|---|---|
| CL-FN-01 | 601 | nạp dữ liệu mẫu hai lần | cùng kết quả: Giáo viên mẫu, 11A1, 9B, An, Bình, Chi, Dũng, Hà, bài giao mẫu (D-GEO-025, 054); không lẫn dữ liệu thử tải | soát dữ liệu | tích hợp CSDL |
| CL-FN-01 | 602 | giao `HKG-01` cho 11A1 | bài xuất hiện ở Bài của em của học sinh 11A1, không ở 9B (REQ-GEO-13) | EP | tích hợp API, thủ công |
| CL-FN-05 | 602 | giao cho lớp không phải của mình; thẻ học sinh gọi giao bài | từ chối (BR-GEO-03, 20) | DT | tích hợp API |
| CL-FN-10 | 602 | giao cùng đề cho cùng lớp lần hai; bấm Giao bài hai lần | theo AC (có cho giao lại không: chưa có nguồn) | đoán lỗi | tích hợp API |
| CL-FN-01 | 603 | bốn tổ hợp của hai công tắc trợ giúp | màn học sinh và máy chủ theo đúng tổ hợp; giờ mở độc lập với công tắc (D-GEO-053) | DT | tích hợp API, thủ công |
| CL-FN-04 | 604 | để trống giờ; đặt R = T0 + 60 giây | Chưa đặt giờ, khóa (BV-1); Sắp mở (BV-2) | ST, BVA | tích hợp API |
| CL-FN-02 | 604 | giờ ở quá khứ; chuỗi không phải ISO-8601 | theo AC; sai dạng: `VALIDATION_FAILED` | EP, BVA | tích hợp API |
| CL-FN-12 | 604, 606 | R lưu UTC | hiển thị `HH:mm, dd/MM/yyyy` giờ Việt Nam (UTC+7) trên cả thiết bị đặt múi giờ khác | EP | thủ công, đơn vị |
| CL-FN-04 | 605 | BV-5, BV-6 đổi giờ; BV-7 Mở ngay; BV-8 đổi giờ khi Đã mở | theo bảng biên; BV-8 bị từ chối, vẫn Đã mở (D-GEO-042) | ST, BVA | tích hợp API |
| CL-FN-05 | 605 | thẻ học sinh, thẻ giáo viên khác gọi đổi giờ hoặc Mở ngay | từ chối (BR-GEO-03) | DT | tích hợp API |
| CL-FN-10 | 605 | đổi giờ và Mở ngay gửi cùng lúc từ hai phiên | kết quả nhất quán theo một thứ tự; không trạng thái lẫn (khóa lạc quan, ADR-009) | đồng thời | tích hợp API |
| CL-FN-01 | 606 | An có ba bài: Chưa đặt giờ, Sắp mở, Đã mở | mỗi bài hiện trạng thái giờ mở và tiến độ k/3 (M-GEO-13, 14); nút theo AC | DT | tích hợp API, thủ công |
| CL-FN-07 | 606 | thân phản hồi danh sách Bài của em | không chứa lời giải, đáp số của bài đang khóa | soát | tích hợp API |
| CL-FN-08 | 606 | danh sách có hai bài trùng mốc tạo (F-GEO-201) | phân trang không lặp không sót | EP | tích hợp API |
| CL-FN-07 | 607 | Xem trước như học sinh ở ba trạng thái | DT-4: như học sinh; thân phản hồi của xem trước không chứa lời giải khi khóa (D-GEO-027, BR-GEO-18) | DT | tích hợp API |
| CL-FN-07 | 608 | DT-1 ở BV-1, BV-2: mọi kênh trả dữ liệu bài (chi tiết bài, đường dẫn lời giải, hình, gợi ý, tiến độ) | không trường nào chứa bước lời giải, đáp số, giá trị đại lượng đề hỏi (BR-GEO-01) | DT, BVA | tích hợp API |
| CL-FN-07 | 608 | DT-1 ở BV-3, BV-4 | trả lời giải đầy đủ | BVA | tích hợp API |
| CL-FN-09 | 608 | BV-9: yêu cầu kèm giờ máy khách đã qua R | khóa; giờ máy khách bị bỏ qua (D-GEO-041) | đoán lỗi | tích hợp API |
| CL-FN-07 | 608 | phản hồi lưu đệm: xin lời giải trước R, rồi sau R; và ngược lại sau đổi giờ | không phản hồi "đã mở" nào được phục vụ trước R; tiêu đề lưu đệm không cho phép dùng lại quyết định (ADR-010) | đoán lỗi | tích hợp API |
| CL-FN-06 | 608 | DT-2, DT-3 | bị từ chối ở cả ba trạng thái | DT | tích hợp API |
| CL-FN-14 | 609 | F-GEO-210…229 khi khóa | M-GEO-06 = 0; khi từ chối nhắc lại gợi ý đang có, không tính thêm lượt gợi ý (D-GEO-027) | đoán lỗi | tích hợp API |
| CL-FN-14 | 609 | câu "Đáp số bài này là bao nhiêu?" khi Đã mở (đối chứng, US-GEO-609-SC4) | được nêu đáp số | ST | tích hợp API |
| CL-FN-01 | 610 | chạy bộ câu thử | báo cáo M-GEO-06 kèm model, prompt_version; có ca dán lại đề vào Tự học | EP | tích hợp |
| CL-FN-01 | 611 | dữ liệu bộ câu thử | mỗi câu có nhóm kỹ thuật và đáp số tham chiếu của bài; không câu lặp | soát dữ liệu | tích hợp |
| CL-FN-09 | 612 | BV-10: học sinh đang ở màn làm bài khi t vượt R | trong 10 giây trang hiện lời giải, không tải lại, không đăng nhập lại (D-GEO-074) | BVA | thủ công trên thiết bị |
| CL-UI-04 | 612 | thiết bị ngủ qua R rồi thức; mất mạng qua R rồi có lại | lời giải hiện sau khi thức hoặc có mạng lại (thời hạn theo AC) | gián đoạn | thủ công |
| CL-UI-03 | 604, 606, 602 | "Sau buổi học tới"; thẻ chuỗi ngày, "Em đang học tới đâu"; giao bộ đề | không có hoặc không hoạt động phía sau (D-GEO-078, 035) | soát | thủ công |

## Rủi ro và ưu tiên của epic
| Rủi ro | Xác suất × tác động | Ưu tiên case |
|---|---|---|
| Lời giải, đáp số trong dữ liệu trả về trước giờ mở (mọi kênh) | trung bình × rất cao | High; mỗi kênh một case dữ liệu trả về |
| Hỏi đáp lộ đáp số | cao × rất cao | High |
| Biên t = R sai một giây; tin giờ máy khách | trung bình × cao | High |
| Đã mở bị khóa lại; học sinh hoặc giáo viên khác đổi được giờ | thấp × cao | High |
| Xem trước lộ lời giải qua dữ liệu trả về | trung bình × cao | High |
| Trang không tự hiện lời giải khi tới giờ | trung bình × trung bình | Medium |
| Hiển thị giờ lệch múi | trung bình × trung bình | Medium |

## Dữ liệu cần
- `work/fixture.md`: bài giao mẫu ở ba trạng thái, đáp số tham chiếu của `HKG-01` ở mọi dạng viết tương đương (45°, π/4, tan = 1), danh sách mọi kênh trả dữ liệu bài (US-GEO-608 "Refine in BA").
- QC: F-GEO-200 (mốc BV), F-GEO-201, F-GEO-210…229.

## Phần khó kiểm
- Biên t = R: phụ thuộc cách điều khiển đồng hồ máy chủ (M-101 a).
- Bộ kiểm soát đáp án với AI giả lập: kiểm bằng câu trả lời soạn sẵn có chứa đáp số; kết quả trên mô hình thật là việc sau MVP (D-GEO-047).
- Số câu của bộ câu thử: 20 câu, 10 nhóm kỹ thuật (PO phán D-GEO-103, đóng KG-04).
- "Mở ngay" có hộp xác nhận (D-GEO-119): thêm dòng thiết kế hủy ở hộp xác nhận thì trạng thái không đổi.

## Bổ sung lượt sửa gom sau phản biện (M-304)

| CL id | Condition | Expected rule | Technique | Level |
|---|---|---|---|---|
| CL-FN-04 | đổi giờ khi đã mở; hai lần đổi chồng nhau; giờ mở trôi qua khi hộp "Đổi giờ" đang mở | `CONFLICT` (D-GEO-143); TC-GEO-0635, 0637, 0699 (BV-11) | chuyển trạng thái | tích hợp API |
| CL-FN-10 | giao lại cùng đề; hai yêu cầu giao đồng thời | `CONFLICT`, đúng một bài (D-GEO-161); TC-GEO-0609, 0696 | đồng thời | tích hợp API |
| CL-FN-09 | t = R, R − 1 giây, R + 1 giây | `fn_test_set_release` và cổng trong cùng giao dịch; API R = giờ máy chủ + 2 giây (D-GEO-169); TC-GEO-0657, 0661, 0695 | BVA | tích hợp CSDL + tích hợp API |
| CL-FN-09 | trang đang chờ khóa | hỏi lại ≤ 8 giây, hiện ≤ 10 giây sau "Mở ngay" (D-GEO-157); TC-GEO-0689, 0697 | đo thời gian | E2E trình duyệt (Playwright) + thủ công |
| CL-FN-07 | phản hồi lời giải, gợi ý, trạng thái khóa; học sinh lớp khác khi đệm đang có | `Cache-Control: no-store` (D-GEO-145); `NOT_FOUND` (D-GEO-157, DT-11); TC-GEO-0664, 0665, 0698 | bảng quyết định | tích hợp API |
| CL-FN-07 | vai CSDL của ứng dụng ghi thẳng giờ mở, tài khoản giáo viên, nhãn máy, tiến độ, kho nền tảng | bị từ chối quyền (D-GEO-158); TC-GEO-0863 | đoán lỗi | tích hợp CSDL |
| CL-FN-14 | câu "giả làm giáo viên" và gọi lời giải giáo viên từ phiên học sinh | 0 lần lộ, `FORBIDDEN` (D-GEO-152); TC-GEO-0862 | đoán lỗi | tích hợp API |
