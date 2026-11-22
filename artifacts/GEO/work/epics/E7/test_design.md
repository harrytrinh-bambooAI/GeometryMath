# E7 — thiết kế kiểm thử (Theo dõi lớp và ghi sự kiện)
> Chủ sở hữu: QC. Phần chung (danh mục CL, kỹ thuật, bảng quyết định khóa lời giải DT-n, biên giờ mở BV-n, thang rủi ro, dữ liệu `F-GEO-200…299`, bảng giải thích mã) ở `review/QC_test_design.md`; tệp này chỉ ghi phần riêng của epic. Lập từ `work/04_story_map.md` và `work/03_gate3_preliminary_stories.md`, không chờ AC; dòng thiết kế nào chưa có AC tương ứng sẽ được đối chiếu ở `qc_findings.md`.

## Applicability map (mỗi nhóm CL: Y / N kèm lý do)
| Group | Screen / story | Applies (Y/N) | Reason if N |
|---|---|---|---|
| CL-FN-01 Luồng chính | US-GEO-701…704 | Y | |
| CL-FN-02 Dữ liệu nhập | US-GEO-703 (lô sự kiện) | Y | |
| CL-FN-03 Lỗi và thông báo | US-GEO-703 (lô sai dạng, quá 1 MB) | Y | |
| CL-FN-04 Chuyển trạng thái | US-GEO-701 (tình trạng học sinh M-GEO-24) | Y | |
| CL-FN-05 Phân quyền theo vai | US-GEO-701, 702 (chỉ giáo viên của lớp; học sinh không vào được) | Y | |
| CL-FN-06 Cách ly trường | US-GEO-701, 703, 704 | Y | |
| CL-FN-07 Dữ liệu máy chủ trả về | US-GEO-701, 702 (dữ liệu Theo dõi lớp không tới học sinh) | Y | |
| CL-FN-08 Phân trang | US-GEO-702 (danh sách câu hỏi), 704 | Y | |
| CL-FN-09 Thời gian | US-GEO-703 (thời điểm sự kiện theo giờ máy chủ), M-GEO-23 | Y | |
| CL-FN-10 Đồng thời, gửi lặp | US-GEO-703 (lô trùng) | Y | |
| CL-FN-11 Nhãn kiểm chứng | — | N | |
| CL-FN-12 Định dạng | US-GEO-701 ("3 / 5", "12′", "18 phút", "—") | Y | |
| CL-FN-13 Sự kiện | US-GEO-703 | Y | |
| CL-FN-14 Cổng AI | US-GEO-702 (nhóm câu hỏi không dùng AI tự nhận định) | Y | |
| CL-UI-01 Thiết bị | US-GEO-701, 702 (máy tính, điện thoại giáo viên) | Y | |
| CL-UI-02 Chuỗi hiển thị | US-GEO-701 (bốn nhãn tình trạng) | Y | |
| CL-UI-03 Ngoài phạm vi | US-GEO-701 (thẻ "Nên nhắc lại trên lớp"; không xếp hạng, BR-GEO-15) | Y | |
| CL-UI-04, 05 | — | N | màn đọc |
| CL-X-01 Tải | US-GEO-703 (ghi sự kiện trong tải đều) | Y | case tải ở E1 |
| CL-X-02 Kế hoạch truy vấn | US-GEO-704 | Y | |
| CL-X-03 Bộ quét SQL | US-GEO-704 | Y | |
| CL-X-04 Dữ liệu trẻ em | US-GEO-703 (sự kiện không chứa dữ liệu ngoài tên hiển thị, mã lớp) | Y | |

## Design rows: `CL id · condition · expected rule · technique · level`
| CL id | Story | Condition | Expected rule (nguồn) | Technique | Level |
|---|---|---|---|---|---|
| CL-FN-01 | 701 | dữ liệu khung `mvp-03`: 11A1, 5 học sinh; An, Bình, Dũng dùng gợi ý; câu hỏi 3 + 2 + 2; thời gian 18, 11, 9, 21, 0 phút | Đã mở bài `n / 5` (M-GEO-15); Dùng gợi ý 3 (M-GEO-21); Câu hỏi 7 (M-GEO-22); thẻ thao tác hình 12′ ((18+11+9+21+0)/5 = 11,8 → 12, M-GEO-23, PROVISIONAL D-GEO-077) | EP | tích hợp API, thủ công |
| CL-FN-01 | 701 | lớp chưa có sự kiện | 0 / 5; "0"; "—" theo M-GEO-15, 21…23 | BVA | tích hợp API |
| CL-FN-04 | 701 | học sinh: chưa `open`; đã mở 0 gợi ý 0 câu hỏi; đã mở có gợi ý; đã xem Gợi ý 1 và 2 khi khóa | Chưa bắt đầu; Tự làm, không cần gợi ý; Đang làm; Đang chờ lời giải (M-GEO-24, PROVISIONAL D-GEO-077); thêm nhãn thứ năm "Đã xem lời giải" khi k = 3 (D-GEO-102) | ST, DT | tích hợp API |
| CL-UI-03 | 701 | thứ tự dòng học sinh; thẻ "Nên nhắc lại trên lớp" | xếp theo tên A → Z (D-GEO-121), không sắp theo điểm hay mức nắm, không cột xếp hạng (BR-GEO-15, D-GEO-022); không có thẻ đó (D-GEO-078) | soát | thủ công |
| CL-FN-01 | 701 | bài đang khóa | thẻ vàng có Mở ngay, Đổi giờ (US-GEO-605) | EP | thủ công |
| CL-FN-01 | 702 | mỗi học sinh | mức gợi ý 0, 1, 2 = max mức đã xem; cột Hỏi đáp = số `ask`, "—" khi không hỏi (M-GEO-15, 22) | EP, BVA | tích hợp API |
| CL-FN-01 | 702 | danh sách câu hỏi | mới nhất trước; nhóm theo nội dung theo quy tắc của AC, không dùng AI tự nhận định | EP | tích hợp API |
| CL-FN-05 | 701, 702 | thẻ học sinh gọi Theo dõi lớp; giáo viên thứ hai gọi lớp 11A1 | từ chối (BR-GEO-12, 20) | DT | tích hợp API |
| CL-FN-06 | 701 | giáo viên "THCS Mẫu C" gọi id bài giao của "THPT Mẫu A" | từ chối (BR-GEO-19) | DT | tích hợp API |
| CL-FN-13 | 703 | mở bài; dùng công cụ Điểm; xem Gợi ý 1; hỏi; mở lời giải | sự kiện `open`, `tool`, `hint`, `ask`, `unlock` có người dùng, trường, bài, thời điểm (REQ-GEO-22) | EP | tích hợp API |
| CL-FN-13 | 703 | chỉ xoay, thu phóng, đặt lại | không sự kiện `tool` (D-GEO-058); phiên không tính vào `P_thaotac` | EP | tích hợp API |
| CL-FN-13 | 703 | 10 phiên, 6 phiên có `tool`; 0 phiên | M-GEO-08 = 60,0%; "—" | BVA | đơn vị |
| CL-FN-09 | 703 | hai sự kiện cách nhau 29 phút 59 giây; cách 30 phút | một phiên; hai phiên theo AC ở đúng mốc 30 phút (D-GEO-059, PROVISIONAL) | BVA | đơn vị |
| CL-FN-10 | 703 | gửi lại cùng một lô | không tạo sự kiện trùng (ADR-011) | gửi lặp | tích hợp API |
| CL-FN-06 | 703 | lô sự kiện tự kèm mã trường khác | mã trường lấy từ thẻ (M-004) | đoán lỗi | tích hợp API |
| CL-FN-03 | 703 | lô sai dạng; thân > 1 MB | `VALIDATION_FAILED`; `PAYLOAD_TOO_LARGE` (M-004) | EP, BVA | tích hợp API |
| CL-FN-08 | 704 | F-GEO-201: hai bản ghi trùng mốc thời gian, 101 bản ghi; đi hết các trang | không lặp, không sót; trang ≤ 100 dòng; không tổng số | EP, BVA | tích hợp API |
| CL-FN-08 | 704 | chèn bản ghi mới giữa hai lần tải trang | các bản ghi đã có không lặp, không sót | đồng thời | tích hợp API |
| CL-X-02 | 704 | kế hoạch thực thi truy vấn nóng trên dữ liệu 2.000.000 sự kiện | không quét tuần tự bảng sự kiện; chỉ đọc phân vùng trong cận thời gian (REQ-GEO-39) | soát kế hoạch | tích hợp CSDL |
| CL-X-03 | 704 | truy vấn bảng sự kiện có `OFFSET` hoặc thiếu lọc mã trường (F-GEO-204) | bộ quét báo lỗi | EP | đơn vị (bộ quét) |

## Rủi ro và ưu tiên của epic
| Rủi ro | Xác suất × tác động | Ưu tiên case |
|---|---|---|
| Giáo viên thấy lớp khác, trường khác; học sinh vào được Theo dõi lớp | thấp × rất cao | High |
| Số liệu thẻ sai công thức | trung bình × trung bình | High cho bộ số của khung `mvp-03`, Medium cho biên |
| Sự kiện trùng hoặc mất làm sai tiến độ | trung bình × trung bình | Medium |
| Xoay, thu phóng bị đếm là thao tác | trung bình × trung bình | Medium |
| Truy vấn sự kiện quét cả bảng | trung bình × cao | High (ENG) |
| Có xếp hạng trá hình | thấp × cao | Medium |

## Dữ liệu cần
- `work/fixture.md`: bộ sự kiện của 11A1 đúng số của khung `mvp-03`; học sinh thứ năm chưa mở bài.
- QC: F-GEO-201, 202, 204. ARCH: dữ liệu thử tải 2.000.000 sự kiện, danh sách truy vấn nóng.

## Phần khó kiểm
- Công thức M-GEO-21…24 là suy luận của PO từ khung thiết kế (D-GEO-077): mọi case số liệu PROVISIONAL.
- Mất sự kiện khi máy chủ CSDL sập (ADR-011 chấp nhận): không viết case; ghi vào `backlog_cases.md`.
- Câu hỏi nhạy cảm của học sinh: không có nguồn về xử lý; gửi BA.

## Bổ sung lượt sửa gom sau phản biện (M-304)

| CL id | Condition | Expected rule | Technique | Level |
|---|---|---|---|---|
| CL-FN-09 | khoảng lặng 29 phút 59 giây / 30 phút 0 giây | một phiên 40 phút / hai phiên 10 phút (D-GEO-144); TC-GEO-0740, 0741 | BVA | đơn vị + tích hợp CSDL |
| CL-FN-10 | gửi lại câu hỏi sau hết thời gian chờ | đếm một lần (ADR-GEO-11, RV-GEO-18); TC-GEO-0742 | đồng thời | tích hợp API |
