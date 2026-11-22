# E1 — thiết kế kiểm thử (nền, cổng AI, bộ đề chuẩn, thử tải)
> Chủ sở hữu: QC. Phần chung (danh mục CL, kỹ thuật, bảng quyết định khóa lời giải DT-n, biên giờ mở BV-n, thang rủi ro, dữ liệu `F-GEO-200…299`, bảng giải thích mã) ở `review/QC_test_design.md`; tệp này chỉ ghi phần riêng của epic. Lập từ `work/04_story_map.md` và `work/03_gate3_preliminary_stories.md`, không chờ AC; dòng thiết kế nào chưa có AC tương ứng sẽ được đối chiếu ở `qc_findings.md`.

## Applicability map (mỗi nhóm CL: Y / N kèm lý do)
| Group | Screen / story | Applies (Y/N) | Reason if N |
|---|---|---|---|
| CL-FN-01 Luồng chính | US-GEO-101…108 | Y | |
| CL-FN-02 Dữ liệu nhập | US-GEO-101 (tên hiển thị, mã lớp), 102 (mã giáo viên mẫu) | Y | |
| CL-FN-03 Lỗi và thông báo | US-GEO-101, 102, 103 | Y | |
| CL-FN-04 Chuyển trạng thái | — | N | E1 không có thực thể có vòng đời; phiên đăng nhập chỉ có vào và hết hạn |
| CL-FN-05 Phân quyền theo vai | US-GEO-102, 104 | Y | |
| CL-FN-06 Cách ly trường | US-GEO-104 | Y | |
| CL-FN-07 Dữ liệu máy chủ trả về | US-GEO-103 (khóa AI không xuống trình duyệt), 104, 107 (không lộ khi tải cao) | Y | |
| CL-FN-08 Phân trang | US-GEO-104 (con trỏ của trường khác) | Y | danh sách cụ thể kiểm ở E5, E7 |
| CL-FN-09 Thời gian | — | N | E1 không có quy tắc theo giờ; giờ mở ở E6 |
| CL-FN-10 Đồng thời, gửi lặp | US-GEO-101 (hai học sinh trùng tên), 107 | Y | |
| CL-FN-11 Nhãn kiểm chứng | US-GEO-105 (M-GEO-05 = 0) | Y | |
| CL-FN-12 Định dạng | US-GEO-105 (phần trăm một chữ số, dấu phẩy, "—" khi N = 0) | Y | |
| CL-FN-13 Sự kiện | — | N | ghi sự kiện ở E7; E1 chỉ ghi lượt gọi AI (CL-FN-14) |
| CL-FN-14 Cổng AI | US-GEO-103 | Y | |
| CL-UI-01 Thiết bị | US-GEO-101 (iPad, iPhone), 102 (máy tính, điện thoại) | Y | |
| CL-UI-02 Chuỗi hiển thị | US-GEO-101, 102 | Y | |
| CL-UI-03 Ngoài phạm vi | US-GEO-102 (công tắc vai của bản mẫu không có trong MVP) | Y | |
| CL-UI-04 Gián đoạn | US-GEO-101 (mất mạng lúc vào lớp) | N | hiếm, không mất dữ liệu; đưa vào `backlog_cases.md` |
| CL-UI-05 Cảm ứng | — | N | màn vào lớp chỉ có ô nhập và nút |
| CL-X-01 Tải | US-GEO-107, 108 | Y | |
| CL-X-02 Kế hoạch truy vấn | US-GEO-107 | Y | |
| CL-X-03 Bộ quét SQL | US-GEO-104, 107 | Y | |
| CL-X-04 Dữ liệu trẻ em | US-GEO-101, 103 | Y | |

## Design rows: `CL id · condition · expected rule · technique · level`
| CL id | Story | Condition | Expected rule (nguồn) | Technique | Level |
|---|---|---|---|---|---|
| CL-FN-01 | 101 | tên hiển thị và mã lớp hợp lệ của lớp 11A1 | vào được, thấy Bài của em của lớp 11A1 (REQ-GEO-23) | EP | tích hợp API, thủ công |
| CL-FN-02 | 101 | mã lớp sai; mã lớp rỗng; tên rỗng; tên dài quá giới hạn; tên có dấu tiếng Việt | từ chối với `VALIDATION_FAILED` hoặc thông báo của AC; tên có dấu được nhận (độ dài tối đa: chờ BA, TBD ở `glossary.md`) | EP, BVA | đơn vị, tích hợp API |
| CL-FN-10 | 101 | hai học sinh nhập cùng tên trong một lớp | theo quy tắc trùng tên của BA (chưa có nguồn; D-GEO-048) | đoán lỗi | tích hợp API |
| CL-X-04 | 101 | biểu mẫu và thân yêu cầu vào lớp | chỉ có tên hiển thị và mã lớp; không trường email, số điện thoại (REQ-GEO-44) | soát | thủ công, tích hợp API |
| CL-FN-01 | 102 | mã giáo viên mẫu đúng | vào vai giáo viên, thấy Kho đề và hai lớp mẫu (D-GEO-073) | EP | tích hợp API, thủ công |
| CL-FN-05 | 102 | mã sai; học sinh dùng thẻ học sinh gọi đường dẫn của giáo viên | từ chối; `FORBIDDEN` hoặc `UNAUTHENTICATED` (M-004) | DT | tích hợp API |
| CL-FN-14 | 103 | một lượt gọi qua nhà cung cấp giả lập | bản ghi lượt gọi có model, phiên bản prompt, chi phí, trạng thái, mã trường (REQ-GEO-27) | EP | tích hợp API |
| CL-FN-14 | 103 | đổi model trong cấu hình | lượt gọi sau ghi model mới, mã gọi không đổi (REQ-GEO-27) | EP | tích hợp API |
| CL-FN-14 | 103 | nhà cung cấp lỗi; quá thời gian | thông báo dễ hiểu, cho thử lại, không nội dung dở dang, không đổi nhãn (D-GEO-072); `TIMEOUT` (M-004) | EP | tích hợp API |
| CL-FN-07 | 103 | mọi phản hồi và tài nguyên tĩnh gửi xuống trình duyệt | không chứa khóa của nhà cung cấp AI (REQ-GEO-27) | soát | tích hợp API |
| CL-X-04 | 103 | thân yêu cầu gửi nhà cung cấp | không chứa tên hiển thị của học sinh (D-GEO-067) | soát | tích hợp API |
| CL-FN-06 | 104 | học sinh "THCS Mẫu C" gọi bài giao của "THPT Mẫu A" bằng id đúng | bị từ chối, không dữ liệu (BR-GEO-19); mã lỗi chờ M-101 (b) | DT | tích hợp API |
| CL-FN-05 | 104 | học sinh 11A1 gọi bài giao của 9B; gọi dữ liệu của bạn cùng lớp | bị từ chối (BR-GEO-12) | DT | tích hợp API |
| CL-FN-05 | 104 | giáo viên gọi lớp không phải của mình | bị từ chối (BR-GEO-20); MVP chỉ một Giáo viên mẫu nên cần giáo viên thứ hai trong dữ liệu thử | DT | tích hợp API |
| CL-FN-06 | 104 | thân yêu cầu tự kèm mã trường khác với thẻ | mã trường lấy từ thẻ, giá trị client gửi bị bỏ qua (M-004 mục 4) | đoán lỗi | tích hợp API |
| CL-FN-08 | 104 | con trỏ phát cho trường A dùng với thẻ trường B (F-GEO-203) | không trả bản ghi của trường A; mã lỗi chờ M-101 (c) | đoán lỗi | tích hợp API |
| CL-FN-06 | 104 | học sinh "THCS Mẫu C" đọc Kho đề và bài giảng mẫu | đọc được, là nội dung hệ thống dùng chung (D-GEO-076) | EP | tích hợp API |
| CL-X-03 | 104 | bộ quét SQL chạy trên câu mẫu F-GEO-204 | mỗi mẫu cấm bị báo lỗi; bản dựng không qua | EP | đơn vị (bộ quét) |
| CL-FN-01 | 105 | chạy bộ đề chuẩn N đề | báo cáo có M-GEO-01, 04, 05, 07, model, prompt_version (REQ-GEO-25) | EP | tích hợp |
| CL-FN-12 | 105 | N = 40 với 38 hình đạt; N = 0 | 95,0%; "—" (M-GEO-01) | BVA | đơn vị |
| CL-FN-11 | 105 | một đề có đáp số tham chiếu khác giá trị tính mà mang "Đã kiểm chứng" | M-GEO-05 đếm 1; báo cáo không đạt | đoán lỗi | đơn vị |
| CL-FN-01 | 106 | đề nhiều giá trị `HKG-04`; đề theo tham số `HKG-02` | mỗi đề có đáp số tham chiếu cho từng giá trị và ≥ 3 bộ tham số (M-GEO-03) | soát dữ liệu | tích hợp |
| CL-X-01 | 107 | 1000 phiên đang hoạt động, T trong [3, 5] giây | tải 200 đến 333,3 yêu cầu/giây; đo p95 đọc, ghi, tỉ lệ lỗi (M-GEO-11, 20; ngưỡng đề xuất D-GEO-045) | tải | tải |
| CL-X-01 | 107 | 1000 học sinh mở lời giải trong cửa sổ 10 giây | 100 yêu cầu/giây trên đường đọc lời giải (M-GEO-12); không yêu cầu nào trước R nhận lời giải | tải, đồng thời | tải |
| CL-X-02 | 107 | truy vấn nóng trên dữ liệu M-GEO-19 | kế hoạch thực thi không có quét tuần tự trên bảng lớn (REQ-GEO-39) | soát kế hoạch | tích hợp CSDL |
| CL-FN-01 | 108 | bộ sinh dữ liệu thử tải | 50 trường, 600 lớp, 20.000 học sinh, 2.000.000 sự kiện (D-GEO-049); tách khỏi dữ liệu mẫu; tên không phải người thật | soát dữ liệu | tích hợp CSDL |

## Rủi ro và ưu tiên của epic
| Rủi ro | Xác suất × tác động | Ưu tiên case |
|---|---|---|
| Lộ dữ liệu trường khác hoặc lớp khác qua id, con trỏ | trung bình × rất cao | High; có ở mọi thực thể có id trên đường dẫn |
| Học sinh vào được vai giáo viên | thấp × rất cao | High |
| Khóa AI xuống trình duyệt; tên hiển thị gửi nhà cung cấp | thấp × cao | High |
| Thử tải không đạt ngưỡng đề xuất | trung bình × cao | High, PROVISIONAL (KG-06) |
| Báo cáo bộ đề chuẩn sai công thức | thấp × trung bình | Medium |
| Quy tắc trùng tên học sinh | trung bình × thấp | Medium, chờ AC |

## Dữ liệu cần
- `work/fixture.md` (BA): lớp 11A1, 9B và mã lớp; An, Bình, Chi, Dũng, Hà; mã giáo viên mẫu; trường "THPT Mẫu A".
- QC: F-GEO-202, 203, 204 (`review/QC_test_design.md` §6); một giáo viên thứ hai chỉ có trong dữ liệu thử để kiểm BR-GEO-20.
- ARCH: bộ dữ liệu thử tải (`F-GEO-300…`), danh sách truy vấn nóng (`architecture/04_query_catalogue.md`), NFR-GEO-nnn.

## Phần khó kiểm
- Thử tải: số đo trên máy phát triển chỉ so sánh tương đối; ngưỡng là đề xuất (D-GEO-045).
- Bộ đề chuẩn trên AI giả lập: tỉ lệ đo được không phản ánh AI thật (D-GEO-047); case chỉ kiểm công thức và định dạng báo cáo.
- Thời gian chờ tối đa của cổng AI: TBD — owner TechLead (D-GEO-072); case quá thời gian dùng giá trị cấu hình của lần chạy.

## Bổ sung lượt sửa gom sau phản biện (M-304)

| CL id | Condition | Expected rule | Technique | Level |
|---|---|---|---|---|
| CL-FN-05 | 10 mã giáo viên sai trong 15 phút từ một IP, rồi lần 11 và mã đúng | lần 11 trở đi `RATE_LIMITED` (D-GEO-152); TC-GEO-0171 | BVA (10 / 11) | tích hợp API |
| CL-FN-07 | mã giáo viên trong CSDL | chỉ có giá trị băm (D-GEO-152); TC-GEO-0172 | đoán lỗi | tích hợp CSDL |
| CL-X-01 | quá tải gấp đôi | `SERVICE_OVERLOADED` kèm `Retry-After` (D-GEO-143); TC-GEO-0155, công cụ Gatling (D-GEO-170) | tải | tải |
