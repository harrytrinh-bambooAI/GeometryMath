# E4 — thiết kế kiểm thử (gợi ý, lời giải đồng bộ hình, hỏi đáp, tự học)
> Chủ sở hữu: QC. Phần chung (danh mục CL, kỹ thuật, bảng quyết định khóa lời giải DT-n, biên giờ mở BV-n, thang rủi ro, dữ liệu `F-GEO-200…299`, bảng giải thích mã) ở `review/QC_test_design.md`; tệp này chỉ ghi phần riêng của epic. Lập từ `work/04_story_map.md` và `work/03_gate3_preliminary_stories.md`, không chờ AC; dòng thiết kế nào chưa có AC tương ứng sẽ được đối chiếu ở `qc_findings.md`.

## Applicability map (mỗi nhóm CL: Y / N kèm lý do)
| Group | Screen / story | Applies (Y/N) | Reason if N |
|---|---|---|---|
| CL-FN-01 Luồng chính | US-GEO-401…412 | Y | |
| CL-FN-02 Dữ liệu nhập | US-GEO-401 (đề chữ), 402 (ảnh), 409 (câu hỏi) | Y | |
| CL-FN-03 Lỗi và thông báo | US-GEO-401, 402, 409 (AI lỗi, D-GEO-072) | Y | |
| CL-FN-04 Chuyển trạng thái | US-GEO-404, 405 (thang gợi ý), 408 (bước lời giải) | Y | |
| CL-FN-05 Phân quyền theo vai | US-GEO-410 (chỉ thấy câu hỏi của mình) | Y | |
| CL-FN-06 Cách ly trường | US-GEO-401 (đề tự nhập có mã trường), 410 | Y | |
| CL-FN-07 Dữ liệu máy chủ trả về | US-GEO-404, 405, 406, 409, 411 | Y | |
| CL-FN-08 Phân trang | US-GEO-410 (nhật ký) | Y | |
| CL-FN-09 Thời gian | US-GEO-406 (theo giờ mở của bài giao) | Y | |
| CL-FN-10 Đồng thời, gửi lặp | US-GEO-404 (bấm Gợi ý hai lần), 401 (gửi đề hai lần) | Y | |
| CL-FN-11 Nhãn kiểm chứng | US-GEO-401 (đề lạ: "Cần xem lại" kèm cảnh báo) | Y | quy tắc nhãn kiểm ở E2 |
| CL-FN-12 Định dạng | US-GEO-407, 411 (chuỗi nhãn, nguồn) | Y | |
| CL-FN-13 Sự kiện | US-GEO-404, 405, 409 (`hint`, `unlock`, `ask`) | Y | |
| CL-FN-14 Cổng AI | US-GEO-401, 402, 409 | Y | |
| CL-UI-01 Thiết bị | US-GEO-401…411 | Y | |
| CL-UI-02 Chuỗi hiển thị | US-GEO-404 ("Gợi ý 1", "Gợi ý 2", "Lời giải"), 411 | Y | |
| CL-UI-03 Ngoài phạm vi | US-GEO-410 (mục "Lịch sử", D-GEO-078), 401 (đề hình phẳng) | Y | |
| CL-UI-04 Gián đoạn | US-GEO-401 (mất mạng lúc màn chờ) | Y | |
| CL-UI-05 Cảm ứng | US-GEO-407, 408 (bấm bước) | Y | |
| CL-X-01 Tải | — | N | đường AI không nằm trong kịch bản tải đều; hỏi ARCH nếu NFR có |
| CL-X-02, 03 | US-GEO-410 | Y | truy vấn nhật ký qua bộ quét và kế hoạch chung (case ở E1, E7) |
| CL-X-04 Dữ liệu trẻ em | US-GEO-409 (câu hỏi gửi nhà cung cấp không kèm tên hiển thị) | Y | |

## Design rows: `CL id · condition · expected rule · technique · level`
| CL id | Story | Condition | Expected rule (nguồn) | Technique | Level |
|---|---|---|---|---|---|
| CL-FN-02 | 401 | đề rỗng; đề dài quá giới hạn; đề đúng dạng `HKG-01`; đề không phải hình học không gian; đề hình phẳng | rỗng và quá dài bị từ chối (`VALIDATION_FAILED`; giới hạn của BA; trần thân yêu cầu 1 MB → `PAYLOAD_TOO_LARGE`); đề hợp lệ ra hình; hình phẳng: chưa hỗ trợ (REQ-GEO-51) | EP, BVA | tích hợp API |
| CL-FN-14 | 401 | AI đang xử lý; AI lỗi; quá thời gian | màn chờ rõ ràng (REQ-GEO-48); lỗi dễ hiểu, cho thử lại, không nội dung dở dang (D-GEO-072) | EP | tích hợp API, thủ công |
| CL-FN-11 | 401 | đề không khớp hình mẫu | hình từ tọa độ AI, "Cần xem lại" kèm cảnh báo (BR-GEO-08) | DT | tích hợp API |
| CL-FN-06 | 401 | đề tự nhập của An | bản ghi có mã trường của An; học sinh khác không đọc được | DT | tích hợp API |
| CL-FN-02 | 402 | ảnh hợp lệ; ảnh quá lớn; tệp không phải ảnh | ảnh hợp lệ ra chữ đề (nội dung giả lập); còn lại bị từ chối theo AC | EP | tích hợp API |
| CL-FN-01 | 403 | chọn hình mẫu ở Tự khám phá | vào màn thao tác với hình đó, không gọi AI | EP | thủ công |
| CL-FN-04 | 404 | bài giao bật gợi ý: xin Gợi ý 1; rồi Gợi ý 2; xin Gợi ý 2 khi chưa xem Gợi ý 1 | thang theo thứ tự (BR-GEO-04); trường hợp nhảy mức theo AC | ST | tích hợp API |
| CL-FN-07 | 404 | thân phản hồi của Gợi ý 1 và Gợi ý 2 khi lời giải khóa | không chứa đáp số, không chứa bước lời giải ngoài nội dung gợi ý | soát | tích hợp API |
| CL-FN-10 | 404 | bấm Gợi ý 1 hai lần liên tiếp | tiến độ vẫn 1/3 (M-GEO-14: k là số mốc đạt) | đồng thời | tích hợp API |
| CL-FN-05 | 404 | giáo viên tắt "Gợi ý mức 1 và 2" (D-GEO-053) | yêu cầu gợi ý bị máy chủ từ chối, không chỉ ẩn nút | DT | tích hợp API |
| CL-FN-04 | 405 | bài tự học: xin Lời giải khi k = 0; sau Gợi ý 1; sau Gợi ý 1 và 2 | từ chối; từ chối; trả lời giải (D-GEO-023) | ST, BVA | tích hợp API |
| CL-FN-07 | 405 | gọi thẳng đường dẫn lời giải của bài tự học khi chưa xem đủ gợi ý | máy chủ từ chối, thân không có lời giải | đoán lỗi | tích hợp API |
| CL-FN-07 | 406 | dán nguyên văn đề `HKG-01` đang khóa của lớp (F-GEO-225); đã xem Gợi ý 1 và 2 | lời giải vẫn khóa theo giờ mở của bài giao (D-GEO-026) | DT (DT-8) | tích hợp API |
| CL-FN-07 | 406 | đề đã đổi tên điểm (F-GEO-226) | theo quy tắc "gần trùng" (ARCH đề xuất, PO duyệt): PROVISIONAL | đoán lỗi | tích hợp API |
| CL-FN-09 | 406 | cùng đề sau khi bài giao Đã mở | lời giải xem được | ST | tích hợp API |
| CL-FN-01 | 407 | bài Đã mở: bấm bước 1, 2, 3 | hình tô màu đúng đối tượng của bước theo dữ liệu `fixture.md` | EP | thủ công |
| CL-FN-04 | 408 | đi tới bước 3 rồi lùi về bước 1; nhảy bước | đường vẽ thêm tăng dần; lùi thì bớt; nhảy theo AC | ST | thủ công |
| CL-FN-01 | 408 | bấm "Vì sao nghĩ ra bước này?" | hiện nội dung của bước | EP | thủ công |
| CL-FN-01 | 409 | câu hỏi có trong kho bài giảng mẫu; câu hỏi ngoài giáo trình | câu trả lời ghi nguồn; ngoài giáo trình: nói chưa có và mời hỏi giáo viên (REQ-GEO-10) | EP | tích hợp API |
| CL-FN-05 | 409 | giáo viên tắt "Hỏi đáp (không nêu đáp số)" | tab ẩn hoặc báo đã tắt (D-GEO-053); máy chủ từ chối câu hỏi | DT | tích hợp API |
| CL-FN-05 | 410 | An lọc nhật ký theo bài; Bình gọi nhật ký của An | An chỉ thấy câu của mình trên bài đó; Bình bị từ chối | DT | tích hợp API |
| CL-FN-01 | 411 | bấm từ khóa có bài giảng; từ khóa không có bài giảng | hộp bài giảng có nguồn và nhãn "AI soạn · chờ giáo viên duyệt" khi áp dụng (D-GEO-029) | EP | thủ công |
| CL-FN-07 | 411, 412 | nội dung mọi bài giảng mẫu so với đáp số các bài trong kho | không bài giảng nào nêu đáp số của bài trong kho (D-GEO-027) | soát dữ liệu | tích hợp |
| CL-FN-13 | 404, 405, 409 | mỗi lần xem gợi ý, mở lời giải, hỏi | một sự kiện `hint`, `unlock`, `ask` | EP | tích hợp API |
| CL-UI-03 | 410 | thanh bên | mục "Lịch sử" theo quyết định của BA (D-GEO-078) | soát | thủ công |
| CL-UI-04 | 401 | mất mạng trong màn chờ | thông báo, cho thử lại | gián đoạn | thủ công |

## Rủi ro và ưu tiên của epic
| Rủi ro | Xác suất × tác động | Ưu tiên case |
|---|---|---|
| Tự học là đường vòng tới lời giải đang khóa | trung bình × rất cao | High |
| Lời giải tự học mở khi chưa xem đủ gợi ý, chặn chỉ ở giao diện | trung bình × cao | High |
| Gợi ý hoặc bài giảng chứa đáp số | thấp × rất cao | High |
| Hình không đồng bộ với bước lời giải | trung bình × trung bình | Medium |
| AI lỗi để lại nội dung dở dang | trung bình × trung bình | Medium |
| Nhật ký hỏi đáp lộ câu của bạn khác | thấp × cao | High |

## Dữ liệu cần
- `work/fixture.md`: nội dung Gợi ý 1, Gợi ý 2, các bước lời giải và đối tượng tô màu của `HKG-01`; kho bài giảng mẫu (khái niệm, nguồn, nhãn); danh sách từ khóa; một câu hỏi trong và một câu ngoài giáo trình; câu trả lời soạn sẵn của AI giả lập.
- QC: F-GEO-225, 226.

## Phần khó kiểm
- Câu trả lời của AI giả lập là soạn sẵn: case chỉ kiểm có nguồn, có màn chờ, có xử lý lỗi; không kiểm độ đúng nội dung.
- "Gần trùng" chưa có định nghĩa (D-GEO-026): case PROVISIONAL tới khi ARCH đề xuất và PO duyệt.
- Thời gian gửi đề tới có hình (M-GEO-09, < 40 giây đề xuất) chỉ đo được trên AI thật: không có case đo ở MVP.

## Bổ sung lượt sửa gom sau phản biện (M-304)

| CL id | Condition | Expected rule | Technique | Level |
|---|---|---|---|---|
| CL-FN-07 | đề tự học gần trùng: đổi tên điểm, đổi a thành 2a, viết lại thêm chữ; cùng hình khác đại lượng hỏi | ba ca đầu kế thừa khóa, ca cuối không (D-GEO-150); TC-GEO-0438, 0482, 0483, 0484 | EP, cặp đảo | tích hợp API + thủ công |
| CL-FN-09 | dán đề trước, giáo viên giao sau; đã xem lời giải tự học rồi giáo viên giao | khóa theo bài giao ở mỗi lần xin (D-GEO-151); TC-GEO-0485, 0486 | chuyển trạng thái | tích hợp API |
| CL-FN-07 | gợi ý của bài tự học kế thừa khóa; tiêu đề phản hồi gợi ý | gợi ý đề kho nguyên văn (D-GEO-155); `Cache-Control: no-store` (D-GEO-145); TC-GEO-0487, 0488 | đoán lỗi | tích hợp API |
| CL-FN-09 | đổi giờ 60 → 120 giây với bài tự học trùng | chờ đồng hồ thật, T0 từ `Date`, kiểm tại R′ + 2 giây (D-GEO-169); TC-GEO-0440 | BVA | tích hợp API |
