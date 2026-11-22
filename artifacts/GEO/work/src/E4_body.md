# E4 — Gợi ý, lời giải đồng bộ hình, hỏi đáp, tự học: story Dev-Ready và tiêu chí chấp nhận

> Dùng chung cho cả epic: "bài tự học" là bài học sinh tự tạo từ đề tự nhập hoặc hình mẫu, chỉ của em đó. AI ở MVP là nhà cung cấp giả lập (D-GEO-047): AC nói điều người dùng thấy và điều máy chủ trả, không nói chất lượng câu chữ của AI thật. Dữ liệu trả về cho học sinh luôn tuân khóa lời giải của E6 (US-GEO-608, 609).

## US-GEO-401 — Nhập đề tự học bằng chữ

- **Story:** Là học sinh, tôi muốn dán đề bằng chữ và nhận hình, để tự học với đề của mình. · Loại Logic · Must · Cỡ L · Độ khó 4
- **Nguồn:** REQ-GEO-01, 41, 48 · D-GEO-052, 060, 072, 165 · SRC-073, SRC-062 · M-GEO-09, 17, 18
- **Góc nhìn người vận hành (M18):** học sinh dán một đề trong sách bài tập vào ô "Đề bài của em" trên iPad rồi bấm "Dựng hình" và chờ. Ca đáng kể: bấm khi ô còn rỗng; trợ lý bận; dán nhầm đề hình phẳng (ở US-GEO-204).
- **Phụ thuộc:** US-GEO-101, 103, 201, 203, 204.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-165 | Tự học chỉ dành cho học sinh đã vào lớp bằng mã lớp. | D-GEO-052 |
| BR-GEO-166 | Đề tự nhập dài từ 1 đến 8000 ký tự sau khi bỏ khoảng trắng đầu cuối; học sinh chọn cấp "THCS" hoặc "THPT". | `architecture/03_schema.sql#problem_version` (QA-GEO-01); `Design/mvp-07` |
| BR-GEO-167 | Từ lúc bấm "Dựng hình" tới lúc có hình hoặc có lỗi, màn hiện MSG-GEO-13; nút "Dựng hình" không bấm được lần hai; không hiện hình dở dang. | REQ-GEO-48 |
| BR-GEO-168 | Đề tự nhập và bài tự học thuộc trường của học sinh và chỉ học sinh đó thấy. | D-GEO-076; BR-GEO-12 |
| BR-GEO-169 | Máy chủ ghi thời điểm nhận đề. Khi hình hiện, thiết bị gửi sự kiện `scene_shown` kèm thời lượng từ lúc gửi đề tới lúc hình hiện. `scene_shown` là sự kiện không bền (mức A: có thể mất khi máy chủ sập), chỉ dùng để tính thời gian trung vị (M-GEO-09); không tính vào thao tác trên hình, tiến độ hay Theo dõi lớp. Ngưỡng dưới 40 giây là đề xuất và chỉ đo trên AI thật. | REQ-GEO-41; D-GEO-060, 047, 165 |

- **Ánh xạ dữ liệu:** `architecture/03_schema.sql#problem`, `#problem_version`, `#assignment` (loại tự học), `#scene_build`, `#ai_call_log`.

### Tiêu chí chấp nhận

**US-GEO-401-SC1 — Dán đề và nhận hình** · Nguồn: REQ-GEO-01; D-GEO-052 · PROVISIONAL D-GEO-052
- Given: An đã vào lớp 11A1, ở màn "Tự học", cấp "THPT".
- When: An dán đề lăng trụ F-GEO-060 vào "Đề bài của em" và bấm "Dựng hình".
- Then: sau màn chờ, màn Làm bài của bài tự học mở với hình lăng trụ đứng ABC.A′B′C′ (AB = 3, AC = 4, AA′ = 5) do engine dựng, đề An đã dán và nhãn máy.

**US-GEO-401-SC2 — Màn chờ rõ ràng** · Nguồn: REQ-GEO-48
- Given: An vừa bấm "Dựng hình" với đề F-GEO-060; nhà cung cấp giả lập trả lời sau 5 giây.
- When: trong 5 giây đó.
- Then: màn hiện MSG-GEO-13; nút "Dựng hình" không bấm được; không có hình hay lời giải dở dang.

**US-GEO-401-SC3 — Bài tự học mới bắt đầu ở mức 0** · Nguồn: REQ-GEO-08, 19; D-GEO-023 · PROVISIONAL D-GEO-023
- Given: bài tự học của SC1 vừa tạo.
- When: An mở tab "Gợi ý" và tab "Lời giải".
- Then: "Tiến độ của em 0/3"; có nút "Xem gợi ý 1"; tab "Lời giải" hiện MSG-GEO-30.

**US-GEO-401-SC4 — Ghi thời gian từ gửi đề tới có hình** · Nguồn: REQ-GEO-41; M-GEO-09; D-GEO-060, 165 · PROVISIONAL D-GEO-060
- Given: ba đề tự học thành công, mỗi đề có một sự kiện `scene_shown` với thời lượng 10, 20 và 60 giây; một đề lỗi (không có `scene_shown`).
- When: tính chỉ số của kỳ đo.
- Then: thời gian trung vị là 20,0 giây; đề lỗi không tính vào trung vị và được đếm riêng là 1; báo cáo ghi "ngưỡng đề xuất < 40 giây, chỉ có nghĩa trên AI thật".

**US-GEO-401-FC1 — Đề rỗng** · Nguồn: REQ-GEO-01, 43
- Given: ô "Đề bài của em" rỗng hoặc chỉ có khoảng trắng (F-GEO-065).
- When: bấm "Dựng hình".
- Then: hiện MSG-GEO-20; không có lượt gọi AI nào; không tạo bài.

**US-GEO-401-FC2 — Đề 8001 ký tự** · Nguồn: QA-GEO-01 · PROVISIONAL QA-GEO-01
- Given: đề dài 8001 ký tự (F-GEO-065).
- When: bấm "Dựng hình".
- Then: hiện MSG-GEO-21; máy chủ trả `VALIDATION_FAILED` nếu yêu cầu vẫn được gửi; không tạo bài.

**US-GEO-401-FC3 — Trợ lý lỗi khi dựng hình** · Nguồn: D-GEO-072; REQ-GEO-43 · PROVISIONAL D-GEO-072
- Given: nhà cung cấp giả lập ở chế độ trả lỗi.
- When: An bấm "Dựng hình" với đề F-GEO-060.
- Then: màn chờ đóng; hiện MSG-GEO-14 và nút "Thử lại"; đề còn nguyên trong ô; không có bài tự học dở dang trong danh sách của An.

**US-GEO-401-FC4 — Đề không phải hình học** · Nguồn: REQ-GEO-01
- Given: An gõ "Thủ đô của nước Pháp là gì?".
- When: bấm "Dựng hình".
- Then: hiện MSG-GEO-18; không có hình; không tạo bài.

**US-GEO-401-FC5 — Bạn khác không thấy bài tự học của mình** · Nguồn: BR-GEO-12; D-GEO-076; NFR-GEO-030 · PROVISIONAL D-GEO-076
- Given: bài tự học của An ở SC1.
- When: Bình (cùng lớp) gọi bài đó bằng mã của nó.
- Then: `NOT_FOUND` (MSG-GEO-04); dữ liệu trả về không có đề hay hình của An.

- **Mở và quyết định đang dùng:** D-GEO-052 (Q-CDT), D-GEO-060, 072; D-GEO-165 (thời điểm hình hiện đo bằng sự kiện `scene_shown` của thiết bị); QA-GEO-01. Khối "Em đã tự học gần đây" của khung `mvp-07` không có yêu cầu nguồn: không xây (D-GEO-112). Đề hình phẳng: US-GEO-204-FC3.
- **DoR:** Ready.

## US-GEO-402 — Nhập đề bằng ảnh

- **Story:** Là học sinh, tôi muốn chụp hoặc chọn ảnh đề, để khỏi gõ lại. · Loại Logic · Must · Cỡ L · Độ khó 5
- **Nguồn:** REQ-GEO-01, 41, 48 · D-GEO-052, 072 · SRC-073 · `Design/mvp-07` (dòng "Ảnh sẽ được đọc thành chữ để em sửa lại")
- **Góc nhìn người vận hành (M18):** học sinh chụp trang sách bằng iPhone. Ca đáng kể: ảnh mờ; ảnh nặng; chữ đọc sai một ký hiệu và em cần sửa trước khi dựng hình.
- **Phụ thuộc:** US-GEO-401, 103.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-170 | Ảnh được đọc thành chữ và điền vào ô "Đề bài của em" để học sinh xem, sửa; hình chỉ dựng khi học sinh bấm "Dựng hình". | `03_gate3_preliminary_stories.md` US-GEO-402; `Design/mvp-07`; D-GEO-110 |
| BR-GEO-171 | Ảnh gửi lên cùng yêu cầu không vượt 1 MB. | M-004 (5); D-GEO-110 |
| BR-GEO-172 | Trong lúc đọc ảnh, màn hiện MSG-GEO-13. | REQ-GEO-48 |

- **Ánh xạ dữ liệu:** `architecture/03_schema.sql#ai_call_log` (lượt đọc ảnh); ảnh không được lưu thành bản ghi riêng trong lược đồ đề xuất.

### Tiêu chí chấp nhận

**US-GEO-402-SC1 — Ảnh rõ được đọc thành chữ để sửa** · Nguồn: REQ-GEO-01; D-GEO-110 · PROVISIONAL D-GEO-110
- Given: An ở "Tự học" trên iPhone; ảnh chụp đề HKG-03, 400 KB (F-GEO-066).
- When: An bấm "Chụp hoặc dán ảnh đề" và chọn ảnh đó.
- Then: sau màn chờ, ô "Đề bài của em" chứa "Lăng trụ đứng tam giác có đáy là tam giác đều cạnh 4 cm, chiều cao 6 cm. Tính thể tích."; chưa có hình nào được dựng.

**US-GEO-402-SC2 — Sửa chữ rồi dựng hình** · Nguồn: REQ-GEO-01; D-GEO-110 · PROVISIONAL D-GEO-110
- Given: ô đề đang chứa chữ đọc từ ảnh ở SC1.
- When: An sửa "4 cm" thành "5 cm" và bấm "Dựng hình".
- Then: hình lăng trụ dựng với cạnh đáy 5; đề lưu trong bài là bản An đã sửa.

**US-GEO-402-SC3 — Chụp trực tiếp bằng máy ảnh của iPad, iPhone** · Nguồn: REQ-GEO-42; D-GEO-001
- Given: An ở "Tự học" trên iPad.
- When: bấm "Chụp hoặc dán ảnh đề".
- Then: có lựa chọn chụp ảnh mới và lựa chọn lấy ảnh có sẵn của thiết bị.

**US-GEO-402-FC1 — Ảnh mờ** · Nguồn: REQ-GEO-01, 43
- Given: ảnh nhòe (F-GEO-067).
- When: An chọn ảnh đó.
- Then: hiện MSG-GEO-22; chữ đang có trong ô đề (nếu có) giữ nguyên.

**US-GEO-402-FC2 — Ảnh vượt 1 MB** · Nguồn: M-004 (5); D-GEO-110 · PROVISIONAL D-GEO-110
- Given: ảnh 1,5 MB (F-GEO-068).
- When: An chọn ảnh đó và thiết bị gửi nguyên ảnh.
- Then: máy chủ trả `PAYLOAD_TOO_LARGE`; hiện MSG-GEO-06; ô đề không đổi.

**US-GEO-402-FC3 — Trợ lý lỗi khi đọc ảnh** · Nguồn: D-GEO-072 · PROVISIONAL D-GEO-072
- Given: nhà cung cấp giả lập ở chế độ trả lỗi.
- When: An chọn ảnh F-GEO-066.
- Then: hiện MSG-GEO-14 và nút "Thử lại"; ô đề không đổi.

- **Mở và quyết định đang dùng:** D-GEO-110 (bước xem và sửa chữ có trong khung và trong outline của PO nhưng không có trong nguồn gốc GEO-01; giới hạn dung lượng ảnh chỉ suy từ trần thân yêu cầu 1 MB, ảnh điện thoại thường lớn hơn: cần PO và ARCH phán việc thu nhỏ ảnh ở thiết bị). Chữ viết tay: không có yêu cầu, không viết AC.
- **DoR:** Ready, PROVISIONAL theo D-GEO-110.

## US-GEO-403 — Chọn hình mẫu để khám phá

- **Story:** Là học sinh, tôi muốn chọn một hình mẫu và xoay thử, để khám phá khi chưa có đề. · Loại UI · Must · Cỡ S · Độ khó 2
- **Nguồn:** REQ-GEO-01, 03 · D-GEO-052, 056 · SRC-073, SRC-075 · `Design/mvp-04` (khối "Tự khám phá"), `mvp-07`
- **Góc nhìn người vận hành (M18):** học sinh bấm một hình nhỏ ở "Tự khám phá" khi đã làm xong bài. Ca đáng kể: mở thẳng địa chỉ Tự học khi chưa vào lớp.
- **Phụ thuộc:** US-GEO-202, 205.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-165 | Tự học chỉ dành cho học sinh đã vào lớp bằng mã lớp. | D-GEO-052 |
| BR-GEO-173 | Màn khám phá hình mẫu chỉ có hình, ô tham số và thanh công cụ; không có gợi ý, lời giải, hỏi đáp vì không có đề. | REQ-GEO-03; D-GEO-136 |

- **Ánh xạ dữ liệu:** danh mục hình mẫu (US-GEO-205).

### Tiêu chí chấp nhận

**US-GEO-403-SC1 — Vào từ "Tự khám phá" của Bài của em** · Nguồn: REQ-GEO-01, 03; `Design/mvp-04`
- Given: An ở "Bài của em"; khối "Tự khám phá" có các hình nhỏ và nút "Mở chế độ tự học".
- When: An bấm hình chóp nhỏ.
- Then: màn khám phá mở với hình chóp mẫu, xoay được ngay.

**US-GEO-403-SC2 — "Mở chế độ tự học"** · Nguồn: `Design/mvp-04`; REQ-GEO-01
- Given: An ở "Bài của em".
- When: bấm "Mở chế độ tự học".
- Then: màn "Tự học" mở với ô "Đề bài của em" và sáu thẻ hình mẫu.

**US-GEO-403-SC3 — Màn khám phá không có gợi ý, lời giải, hỏi đáp** · Nguồn: REQ-GEO-03; D-GEO-136 · PROVISIONAL D-GEO-136
- Given: An đang ở màn khám phá hình hộp mẫu.
- When: xem các tab và nút của màn.
- Then: có hình, ô tham số, thanh công cụ; không có tab "Gợi ý", "Lời giải", "Hỏi đáp" và không có ô "Hỏi về bài này…".

**US-GEO-403-FC1 — Chưa vào lớp** · Nguồn: D-GEO-052 · PROVISIONAL D-GEO-052
- Given: một người chưa vào lớp.
- When: mở thẳng địa chỉ của màn "Tự học".
- Then: máy chủ trả `UNAUTHENTICATED`; hiện MSG-GEO-02 và chuyển tới màn "Vào lớp".

- **Mở và quyết định đang dùng:** D-GEO-052; D-GEO-136 (màn khám phá chỉ có hình và công cụ).
- **DoR:** Ready.

## US-GEO-404 — Xin Gợi ý 1 và Gợi ý 2

- **Story:** Là học sinh, tôi muốn xin gợi ý từng mức trước khi xem lời giải, để tự nghĩ trước. · Loại UI · Must · Cỡ M · Độ khó 3
- **Nguồn:** REQ-GEO-08 · D-GEO-003 (Final), 053, 075, 145, 155 · SRC-080, SRC-051 · BR-GEO-04 · M-GEO-14, 16 · NFR-GEO-050 · XF-07, XF-08
- **Góc nhìn người vận hành (M18):** học sinh bí thì bấm "Xem gợi ý 1", vẫn bí thì bấm tiếp "Xem gợi ý 2". Ca đáng kể: giáo viên đã tắt gợi ý cho bài; muốn nhảy thẳng tới Gợi ý 2; mất mạng lúc bấm.
- **Phụ thuộc:** US-GEO-603 (cấu hình trợ giúp), US-GEO-703 (sự kiện).

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-04 | Gợi ý đi trước lời giải: mức 1 hướng nghĩ, mức 2 bước kế tiếp, mức 3 lời giải. | SRC-051 |
| BR-GEO-174 | Phải xem Gợi ý 1 trước khi xin Gợi ý 2. | BR-GEO-04; D-GEO-108 |
| BR-GEO-175 | Tiến độ = k/3, k là số mốc đã đạt trong {Gợi ý 1, Gợi ý 2, Lời giải}; xem lại một gợi ý đã xem không làm k tăng. | M-GEO-14; D-GEO-075 |
| BR-GEO-176 | Mỗi lần xem một gợi ý ghi một sự kiện gợi ý, ghi bền trước khi nội dung hiện. | REQ-GEO-22; NFR-GEO-050 |
| BR-GEO-177 | Nội dung một gợi ý chỉ được máy chủ gửi khi học sinh xin đúng mức đó. | ADR-GEO-09 (đề xuất ARCH); D-GEO-108 |
| BR-GEO-178 | Bài giao tắt "Gợi ý mức 1 và 2" thì học sinh không xin được gợi ý của bài đó. | D-GEO-053; XF-07 |
| BR-GEO-292 | Phản hồi chứa nội dung gợi ý mang tiêu đề `Cache-Control: no-store`. | D-GEO-145 |
| BR-GEO-293 | Nguồn gợi ý: bài giao dùng gợi ý của đề kho; bài tự học đang kế thừa khóa dùng gợi ý của đề kho (US-GEO-406); bài tự học không kế thừa khóa dùng gợi ý do AI sinh. | D-GEO-155 |

- **Ánh xạ dữ liệu:** `architecture/03_schema.sql#hint`, `#assignment_progress`, `#event_log`, `#assignment` (cho phép gợi ý).

### Tiêu chí chấp nhận

**US-GEO-404-SC1 — Xem Gợi ý 1** · Nguồn: REQ-GEO-08; M-GEO-14
- Given: Hà mở BG-1 lần đầu (F-GEO-054); tab "Gợi ý" hiện "Tiến độ của em 0/3" và nút "Xem gợi ý 1".
- When: Hà bấm "Xem gợi ý 1".
- Then: hiện thẻ "Gợi ý 1 · Hướng nghĩ" với nội dung "Góc giữa đường thẳng và mặt phẳng là góc giữa đường đó và hình chiếu của nó trên mặt phẳng."; tiến độ thành 1/3; có nút "Xem gợi ý 2".

**US-GEO-404-SC2 — Xem Gợi ý 2** · Nguồn: REQ-GEO-08; M-GEO-14; D-GEO-075 · PROVISIONAL D-GEO-075
- Given: Hà đã xem Gợi ý 1 của BG-1.
- When: bấm "Xem gợi ý 2".
- Then: hiện thẻ "Gợi ý 2 · Bước kế tiếp" với nội dung "Hình chiếu của S lên mặt đáy là điểm nào? Từ đó suy ra hình chiếu của SD."; tiến độ 2/3; ở "Bài của em" bài này ghi "Đã xem gợi ý 2".

**US-GEO-404-SC3 — Giáo viên thấy mức gợi ý vừa dùng** · Nguồn: REQ-GEO-14, 22; M-GEO-15, 21
- Given: dữ liệu lớp như F-GEO-050…054 ("dùng gợi ý" = 3); Hà vừa xem Gợi ý 1 của BG-1.
- When: Giáo viên mẫu mở Theo dõi lớp của BG-1.
- Then: thẻ "dùng gợi ý" là 4; dòng Hà có một trong hai đèn "Gợi ý 1 · 2" sáng.

**US-GEO-404-SC4 — Xem lại gợi ý không làm tăng tiến độ** · Nguồn: M-GEO-14
- Given: Bình đã xem Gợi ý 1 của BG-1 (tiến độ 1/3).
- When: Bình rời bài, mở lại và đọc lại Gợi ý 1.
- Then: Gợi ý 1 hiện sẵn không cần bấm lại; tiến độ vẫn 1/3.

**US-GEO-404-SC5 — Gợi ý trên bài tự học** · Nguồn: REQ-GEO-08, 19; D-GEO-155
- Given: bài tự học của An từ đề F-GEO-060 (không trùng bài đang khóa nào nên không kế thừa khóa; gợi ý do AI sinh).
- When: An bấm "Xem gợi ý 1" rồi "Xem gợi ý 2".
- Then: hai gợi ý hiện lần lượt; tiến độ 2/3; tab "Lời giải" hết hiện MSG-GEO-30 và có nút "Xem lời giải".

**US-GEO-404-FC1 — Xin Gợi ý 2 khi chưa xem Gợi ý 1** · Nguồn: BR-GEO-04; D-GEO-108 · PROVISIONAL D-GEO-108
- Given: Hà chưa xem gợi ý nào của BG-1.
- When: từ phiên của Hà, gửi thẳng tới máy chủ yêu cầu Gợi ý 2.
- Then: `FORBIDDEN` với MSG-GEO-34; dữ liệu trả về không có nội dung Gợi ý 2; tiến độ vẫn 0/3; trên màn không có nút "Xem gợi ý 2" khi chưa xem Gợi ý 1.

**US-GEO-404-FC2 — Giáo viên đã tắt gợi ý** · Nguồn: D-GEO-053; XF-07 · PROVISIONAL D-GEO-053
- Given: BG-5 (lớp 9B) có "Gợi ý mức 1 và 2" tắt (F-GEO-044); Khánh mở BG-5.
- When: Khánh mở tab "Gợi ý", rồi gửi thẳng yêu cầu Gợi ý 1.
- Then: tab hiện MSG-GEO-32, không có nút xin gợi ý; yêu cầu gửi thẳng nhận `FORBIDDEN` và không có nội dung gợi ý.

**US-GEO-404-FC3 — Gợi ý chưa xin thì chưa được gửi xuống** · Nguồn: ADR-GEO-09; D-GEO-108 · PROVISIONAL D-GEO-108
- Given: Hà mở BG-1, chưa xin gợi ý nào.
- When: ghi lại dữ liệu trả về của trang Làm bài.
- Then: không có câu chữ của Gợi ý 1 hay Gợi ý 2.

**US-GEO-404-FC4 — Mất mạng lúc bấm** · Nguồn: REQ-GEO-43; NFR-GEO-050
- Given: Hà ở BG-1, tiến độ 0/3; thiết bị mất mạng.
- When: bấm "Xem gợi ý 1".
- Then: hiện MSG-GEO-08; không có gợi ý nào hiện; khi có mạng lại, tiến độ vẫn 0/3 cho tới khi Hà bấm lại.

**US-GEO-404-FC5 — Phản hồi gợi ý không cho lưu đệm** · Nguồn: D-GEO-145; RV-GEO-25 · PROVISIONAL D-GEO-145
- Given: Hà mở BG-1 trên iPad dùng chung của lớp.
- When: Hà bấm "Xem gợi ý 1"; ghi lại tiêu đề của phản hồi chứa Gợi ý 1.
- Then: phản hồi có tiêu đề `Cache-Control: no-store`.

- **Mở và quyết định đang dùng:** D-GEO-053, 075 (Q-GV); D-GEO-145 (không lưu đệm); D-GEO-155 (nguồn gợi ý của bài tự học); D-GEO-108 (thứ tự bắt buộc và việc không gửi trước nội dung gợi ý là cách hiểu của BA từ BR-GEO-04 và ADR-GEO-09).
- **DoR:** Ready.

## US-GEO-405 — Mở lời giải của bài tự học sau Gợi ý 1 và 2

- **Story:** Là học sinh tự học, tôi muốn xem lời giải sau khi đã xem hai gợi ý, để vẫn học trước, đáp án sau. · Loại Logic · Must · Cỡ M · Độ khó 3
- **Nguồn:** REQ-GEO-19 · D-GEO-023 · SRC-091, SRC-022, SRC-052 · BR-GEO-05 · M-GEO-16 · XF-04
- **Góc nhìn người vận hành (M18):** học sinh tự học muốn xem luôn lời giải. Ca đáng kể: bấm vào tab "Lời giải" khi chưa xem gợi ý; gọi thẳng máy chủ để lấy lời giải.
- **Phụ thuộc:** US-GEO-404, 406, 407.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-05 | Bài tự học: Lời giải mở sau khi học sinh đã xem Gợi ý 1 và Gợi ý 2. | SRC-052; D-GEO-023 |
| BR-GEO-179 | Điều kiện trên được kiểm ở máy chủ: trước khi đủ hai gợi ý, máy chủ không gửi bước giải và đáp số của bài tự học ở bất kỳ yêu cầu nào. | `03_gate3_preliminary_stories.md` US-GEO-405; BR-GEO-01 |
| BR-GEO-180 | Bài tự học kế thừa khóa (US-GEO-406) không theo quy tắc này mà theo giờ mở của bài giao. Đề không dựng được bằng hình mẫu (không có chữ ký chuẩn) không có lời giải tự học, chỉ có gợi ý (US-GEO-406-FC5). | D-GEO-026, 150; XF-04 |

- **Ánh xạ dữ liệu:** `architecture/03_schema.sql#solution` (chỉ đọc qua hàm cổng), `#assignment_progress`, `#event_log`.

### Tiêu chí chấp nhận

**US-GEO-405-SC1 — Đủ hai gợi ý thì mở được lời giải** · Nguồn: REQ-GEO-19; D-GEO-023 · PROVISIONAL D-GEO-023
- Given: bài tự học của An từ đề F-GEO-060; An đã xem Gợi ý 1 và Gợi ý 2.
- When: An mở tab "Lời giải" và bấm "Xem lời giải".
- Then: các bước giải và đáp số hiện ra; tiến độ 3/3; có một sự kiện mở lời giải.

**US-GEO-405-SC2 — Đếm lượt gợi ý trước khi mở lời giải** · Nguồn: M-GEO-16
- Given: An đã xem Gợi ý 1 một lần, Gợi ý 2 một lần rồi mở lời giải như SC1.
- When: tính chỉ số của bài đó.
- Then: lượt xem gợi ý trước khi xem lời giải = 2.

**US-GEO-405-FC1 — Chưa xem gợi ý nào** · Nguồn: REQ-GEO-19; D-GEO-023 · PROVISIONAL D-GEO-023
- Given: bài tự học mới tạo, tiến độ 0/3.
- When: An mở tab "Lời giải".
- Then: hiện MSG-GEO-30; không có nút "Xem lời giải"; không có bước giải hay đáp số.

**US-GEO-405-FC2 — Mới xem Gợi ý 1 mà gọi thẳng lời giải** · Nguồn: REQ-GEO-19; BR-GEO-01; D-GEO-023 · PROVISIONAL D-GEO-023
- Given: An mới xem Gợi ý 1 của bài tự học F-GEO-060.
- When: từ phiên của An, gửi thẳng tới máy chủ yêu cầu lời giải của bài đó.
- Then: `FORBIDDEN`; dữ liệu trả về không có bước giải, không có "60/√769", không có "2,1637".

**US-GEO-405-FC3 — Trang bài tự học không mang sẵn lời giải** · Nguồn: BR-GEO-01; REQ-GEO-40
- Given: bài tự học F-GEO-060, tiến độ 0/3.
- When: ghi lại dữ liệu trả về của trang Làm bài và của hai lần xin gợi ý.
- Then: không phản hồi nào chứa bước giải hay đáp số.

**US-GEO-405-FC4 — Hỏi đáp không mở trước lời giải của bài tự học** · Nguồn: REQ-GEO-18, 19; D-GEO-027 · PROVISIONAL D-GEO-027
- Given: bài tự học F-GEO-060, An mới xem Gợi ý 1.
- When: An hỏi ở tab "Hỏi đáp": "Đáp số bài này là bao nhiêu?".
- Then: câu trả lời theo MSG-GEO-35, nhắc lại Gợi ý 1; không chứa "60/√769" hay "2,1637"; tiến độ vẫn 1/3.

- **Mở và quyết định đang dùng:** D-GEO-023 (Q-CDT: phương án A).
- **DoR:** Ready, PROVISIONAL theo D-GEO-023.

## US-GEO-406 — Tự học kế thừa khóa của bài đang khóa

- **Story:** Là giáo viên, tôi muốn đề học sinh dán lại trùng bài đang khóa của lớp thì vẫn khóa theo giờ tôi đặt, để tự học không là đường vòng. · Loại Logic · Must · Cỡ L · Độ khó 5
- **Nguồn:** REQ-GEO-32 · D-GEO-026, 150, 151, 155 · SRC-026 · BR-GEO-17 · XF-04 · RV-GEO-01, 03, 06
- **Góc nhìn người vận hành (M18):** học sinh chép đề bài tập về nhà vào "Tự học" để lấy lời giải sớm. Ca đáng kể: dán nguyên văn; đổi tên điểm cho khác đi; viết lại đề với cạnh `2a` thay cho `a`; dán đề từ sách trước rồi giáo viên mới giao đúng đề đó; giáo viên dời giờ mở sau khi em đã dán.
- **Phụ thuộc:** US-GEO-401, 405, 608.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-17 | Đề tự nhập trùng hoặc gần trùng bài đang khóa thì kế thừa khóa. | SRC-026 |
| BR-GEO-181 | "Bài đang khóa" là bài giao của một lớp mà học sinh đó đang thuộc tại lúc xin, đang Chưa đặt giờ hoặc Sắp mở. Bài của lớp khác, trường khác không gây kế thừa. | D-GEO-026 ("bài đang khóa của lớp"); D-GEO-151 |
| BR-GEO-182 | Bài tự học kế thừa khóa: lời giải và đáp số mở theo trạng thái giờ mở hiện tại của bài giao gốc (kể cả khi giáo viên đổi giờ hoặc mở ngay); việc đã xem đủ hai gợi ý không mở được nó. | D-GEO-026; XF-04 |
| BR-GEO-183 | Đề tự nhập "trùng hoặc gần trùng" bài đang khóa khi **chữ ký chuẩn** của nó bằng chữ ký chuẩn của đề kho của bài đó. Chữ ký chuẩn = (hình mẫu; bộ tham số đã quy về a = 1; đại lượng đề hỏi), tính từ bản dựng hình sau khi engine dựng xong đề; không phụ thuộc tên điểm, cách viết, chữ thêm vào đề. Băm văn bản đề sau khi chuẩn hóa khoảng trắng và hoa thường là phép khớp phụ: khớp băm cũng là khớp. Đề không dựng được bằng hình mẫu thì không có chữ ký, không kế thừa khóa và không có lời giải tự học (chỉ có gợi ý). Ví dụ: F-GEO-063 (đổi tên điểm thành S.MNPQ) và F-GEO-077 (cạnh `2a`, viết lại câu) có cùng chữ ký với HKG-01 nên khớp; F-GEO-078 (SA = a√3) khác bộ tham số nên không khớp. | D-GEO-150; F-GEO-226 là ca chặn của cổng phát hành |
| BR-GEO-294 | Phép kiểm kế thừa khóa chạy lúc tạo bài tự học và chạy lại ở **mỗi lần** học sinh xin Gợi ý 2 hoặc lời giải của bài tự học, đối chiếu với mọi bài đang khóa của các lớp học sinh đang thuộc tại lúc xin. Liên kết tới bài giao nguồn ghi ở bài tự học chỉ là dấu vết, không dùng để quyết định. | D-GEO-151 |
| BR-GEO-295 | Bài tự học đang kế thừa khóa dùng Gợi ý 1 và Gợi ý 2 của đề kho (do Giáo viên mẫu soạn), không dùng gợi ý do AI sinh; phản hồi gợi ý tuân khóa như kênh (4) của BR-GEO-249. | D-GEO-155 |

- **Ánh xạ dữ liệu:** `architecture/03_schema.sql#assignment` (liên kết tới bài giao nguồn của khóa: chỉ là dấu vết, D-GEO-151), `#problem_version` (băm văn bản của đề), `#scene_build` (bản dựng hình); nơi lưu chữ ký chuẩn và phép khớp do ARCH thiết kế theo D-GEO-150, 133 (KG-23).

### Tiêu chí chấp nhận

**US-GEO-406-SC1 — Dán lại đề đang khóa thì bị khóa theo bài giao** · Nguồn: REQ-GEO-32; D-GEO-026 · PROVISIONAL D-GEO-026
- Given: BG-1 (HKG-01, lớp 11A1) đang Sắp mở; An dán nguyên văn đề HKG-01 vào "Tự học" (F-GEO-062) và dựng hình.
- When: An xem Gợi ý 1, Gợi ý 2 rồi mở tab "Lời giải".
- Then: tab hiện MSG-GEO-31; không có nút "Xem lời giải"; không có bước giải hay đáp số.

**US-GEO-406-SC2 — Bài giao mở thì bài tự học kế thừa cũng mở** · Nguồn: D-GEO-026 · PROVISIONAL D-GEO-026
- Given: bài tự học của SC1; Giáo viên mẫu bấm "Mở ngay" cho BG-1.
- When: An mở lại tab "Lời giải" của bài tự học đó.
- Then: lời giải và đáp số "45°" xem được.

**US-GEO-406-SC3 — Đề trùng bài đã mở thì không kế thừa** · Nguồn: D-GEO-026; BR-GEO-05
- Given: BG-3 (HKG-03) Đã mở; An dán nguyên văn đề HKG-03 vào "Tự học".
- When: An xem Gợi ý 1 và Gợi ý 2.
- Then: tab "Lời giải" có nút "Xem lời giải" theo quy tắc tự học; không hiện MSG-GEO-31.

**US-GEO-406-SC4 — Bài của lớp khác không gây kế thừa** · Nguồn: D-GEO-026 · PROVISIONAL D-GEO-026
- Given: BG-1 chỉ giao cho 11A1; Khánh (lớp 9B) dán nguyên văn đề HKG-01 vào "Tự học".
- When: Khánh xem Gợi ý 1 và Gợi ý 2.
- Then: Khánh mở được lời giải theo quy tắc tự học.

**US-GEO-406-SC5 — Đề gần trùng cũng kế thừa** · Nguồn: REQ-GEO-32; D-GEO-026, 150 · PROVISIONAL D-GEO-150
- Given: BG-1 đang Sắp mở; An dán đề đã đổi tên điểm S.MNPQ (F-GEO-063); băm văn bản khác đề HKG-01 nhưng chữ ký chuẩn trùng.
- When: An xem đủ hai gợi ý rồi mở tab "Lời giải".
- Then: tab hiện MSG-GEO-31; không có lời giải.

**US-GEO-406-SC6 — Kế thừa khóa của bài Chưa đặt giờ** · Nguồn: D-GEO-026, 043; REQ-GEO-35 · PROVISIONAL D-GEO-026
- Given: BG-2 (HKG-02, lớp 11A1) Chưa đặt giờ (F-GEO-041); An dán nguyên văn đề HKG-02 vào "Tự học".
- When: An xem đủ hai gợi ý rồi mở tab "Lời giải".
- Then: tab hiện MSG-GEO-31; không có lời giải; bài tự học này vẫn khóa cho tới khi giáo viên đặt giờ và giờ đó tới, hoặc giáo viên mở ngay.

**US-GEO-406-FC1 — Gọi thẳng lời giải của bài kế thừa khóa** · Nguồn: REQ-GEO-32, 40; BR-GEO-01 · PROVISIONAL D-GEO-026
- Given: bài tự học của SC1, An đã xem đủ hai gợi ý, BG-1 vẫn Sắp mở.
- When: từ phiên của An, gửi thẳng tới máy chủ yêu cầu lời giải của bài tự học đó.
- Then: `FORBIDDEN`; dữ liệu trả về không có bước giải và không có "45°".

**US-GEO-406-FC2 — Giáo viên dời giờ mở** · Nguồn: D-GEO-026, 041 · PROVISIONAL D-GEO-026
- Given: BG-4 (HKG-04, lớp 9B) đang Sắp mở với R = T0 + 60 giây (F-GEO-043); Khánh (lớp 9B) đã dán nguyên văn đề HKG-04 vào "Tự học"; tại T0 + 30 giây Giáo viên mẫu đổi giờ BG-4 sang R′ = T0 + 120 giây.
- When: Khánh yêu cầu lời giải của bài tự học tại T0 + 60 giây.
- Then: vẫn khóa (MSG-GEO-31); tới T0 + 120 giây thì xem được.

**US-GEO-406-SC7 — Đề viết lại với cạnh 2a vẫn kế thừa** · Nguồn: REQ-GEO-32; D-GEO-150 · PROVISIONAL D-GEO-150
- Given: BG-1 đang Sắp mở; An dán đề F-GEO-077 (đáy cạnh `2a`, SA = `2a`, câu chữ khác HKG-01).
- When: An xem đủ hai gợi ý rồi mở tab "Lời giải".
- Then: tab hiện MSG-GEO-31 (bộ tham số quy về a = 1 trùng HKG-01); không có lời giải.

**US-GEO-406-SC8 — Cùng hình nhưng khác tham số thì không kế thừa** · Nguồn: D-GEO-150 · PROVISIONAL D-GEO-150
- Given: BG-1 đang Sắp mở; An dán đề F-GEO-078 (SA = a√3).
- When: An xem đủ hai gợi ý rồi mở tab "Lời giải".
- Then: không hiện MSG-GEO-31; có nút "Xem lời giải" theo quy tắc tự học (bộ tham số khác HKG-01 nên không kế thừa).

**US-GEO-406-SC9 — Dán đề trước, giáo viên giao sau: lần xin kế tiếp bị khóa** · Nguồn: REQ-GEO-32; D-GEO-151 · PROVISIONAL D-GEO-151
- Given: lớp 11A1 chưa có bài giao nào dùng HKG-06; An dán nguyên văn đề HKG-06 vào "Tự học", dựng hình và xem Gợi ý 1; sau đó Giáo viên mẫu giao HKG-06 cho 11A1 với giờ mở để trống (BG-6, F-GEO-048).
- When: An bấm "Xem gợi ý 2" rồi mở tab "Lời giải" của bài tự học đó.
- Then: Gợi ý 2 hiện đúng Gợi ý 2 của đề kho HKG-06 "Bán kính bằng nửa đường chéo của hình lập phương."; tab "Lời giải" hiện MSG-GEO-31; không có nút "Xem lời giải".

**US-GEO-406-SC10 — Bài kế thừa khóa dùng gợi ý của đề kho** · Nguồn: D-GEO-155; RV-GEO-06 · PROVISIONAL D-GEO-155
- Given: BG-1 đang Sắp mở; An dán đề F-GEO-063 vào "Tự học" và dựng hình.
- When: An bấm "Xem gợi ý 1" rồi "Xem gợi ý 2".
- Then: hai gợi ý hiện nguyên văn Gợi ý 1 và Gợi ý 2 của HKG-01 (F-GEO-031); không có lượt gọi AI với mục đích sinh gợi ý cho bài tự học này.

**US-GEO-406-FC3 — Dán trước, giao sau: gọi thẳng lời giải bị từ chối** · Nguồn: REQ-GEO-40; D-GEO-151; BR-GEO-01 · PROVISIONAL D-GEO-151
- Given: tình huống của SC9 sau khi An đã xem Gợi ý 2; BG-6 vẫn Chưa đặt giờ.
- When: từ phiên của An, gửi thẳng tới máy chủ yêu cầu lời giải của bài tự học HKG-06.
- Then: `FORBIDDEN`; dữ liệu trả về không có bước giải và không có "a√3/2".

**US-GEO-406-FC4 — Dữ liệu gợi ý của bài kế thừa khóa không lộ** · Nguồn: BR-GEO-01, 18; D-GEO-155; RV-GEO-06 · PROVISIONAL D-GEO-155
- Given: tình huống của SC10.
- When: ghi lại toàn bộ dữ liệu trả về của hai lần xin gợi ý.
- Then: không phản hồi nào chứa "45°", "45 độ", "π/4", "tan SDA = 1" hay nội dung một bước của F-GEO-037.

**US-GEO-406-FC5 — Đề không dựng được bằng hình mẫu không có lời giải tự học** · Nguồn: D-GEO-150, 177 · PROVISIONAL D-GEO-177
- Given: An dán đề lạ F-GEO-061 (bát diện đều; hình dùng tọa độ AI kèm MSG-GEO-16, không khớp hình mẫu nào nên không có chữ ký chuẩn).
- When: An xem Gợi ý 1, Gợi ý 2 rồi mở tab "Lời giải"; sau đó gửi thẳng yêu cầu lời giải của bài tự học đó.
- Then: tab hiện MSG-GEO-58, không có nút "Xem lời giải"; yêu cầu gửi thẳng nhận `FORBIDDEN` và không có bước giải hay "a√6/3".

**US-GEO-406-FC6 — Đã xem lời giải tự học, sau đó giáo viên giao đúng đề: xin lại thì bị khóa** · Nguồn: REQ-GEO-32, 40; D-GEO-174, 151; BR-GEO-01 · PROVISIONAL D-GEO-174
- Given: lớp 11A1 chưa có bài giao nào dùng HKG-05; An dán nguyên văn đề HKG-05 vào "Tự học", xem Gợi ý 1, Gợi ý 2 và đã xem lời giải với đáp số "112π cm²"; sau đó Giáo viên mẫu giao HKG-05 cho 11A1 với giờ mở T0 + 30 phút (BG-7, F-GEO-049, Sắp mở).
- When: tại T0 + 1 phút An mở lại tab "Lời giải" của bài tự học đó và gửi thẳng yêu cầu lời giải; rồi tại T0 + 30 phút An mở lại tab "Lời giải".
- Then: tại T0 + 1 phút tab hiện thẻ khóa MSG-GEO-31, yêu cầu gửi thẳng nhận `FORBIDDEN` và dữ liệu trả về không có bước giải, không có "112π"; tại T0 + 30 phút (BG-7 Đã mở) An xem lại được lời giải và "112π cm²".

- **Mở và quyết định đang dùng:** D-GEO-026 (Q-CDT); D-GEO-150 (chữ ký chuẩn; F-GEO-226 của QC là ca chặn của cổng phát hành); D-GEO-151 (kiểm ở mỗi lần xin Gợi ý 2 hoặc lời giải); D-GEO-155 (gợi ý của đề kho cho bài kế thừa khóa: tên điểm trong gợi ý theo đề kho, có thể khác tên điểm học sinh đã dán, như F-GEO-063). D-GEO-177: đề lạ dùng tọa độ AI không có chữ ký nên không có lời giải tự học (FC5). D-GEO-174: đã xem lời giải tự học rồi giáo viên mới giao đúng đề thì lần xin sau bị khóa tới khi bài giao Đã mở (hệ quả của BR-GEO-294; FC6). Phép khớp chữ ký và nơi lưu: ARCH (KG-23).
- **DoR:** Ready; mọi AC PROVISIONAL theo D-GEO-026, 150, 151, 155.

## US-GEO-407 — Xem lời giải đồng bộ hình: bấm bước thì hình tô màu

- **Story:** Là học sinh, tôi muốn bấm từng bước lời giải và thấy hình tô màu đối tượng của bước đó, để hiểu bước nào làm gì. · Loại UI · Must · Cỡ L · Độ khó 4
- **Nguồn:** REQ-GEO-09 · D-GEO-009 (Final) · SRC-081, SRC-037 · BR-GEO-09 · `Design/mvp-06`
- **Góc nhìn người vận hành (M18):** sau giờ mở, học sinh đọc lời giải từng bước, mắt nhìn sang hình. Ca đáng kể: bấm chuyển bước nhanh; bước chỉ có công thức, không có gì để tô.
- **Phụ thuộc:** US-GEO-608 (đã mở), US-GEO-302.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-184 | Tab "Lời giải" liệt kê các bước theo thứ tự; bước đang chọn được làm nổi; hình tô màu đúng các đối tượng của bước đang chọn và bỏ tô đối tượng của bước khác; dòng "Đang tô trên hình: …" nêu tên các đối tượng đó. | REQ-GEO-09; `Design/mvp-06` |
| BR-GEO-185 | Đầu tab "Lời giải" có nhãn máy của đề và nhãn người "AI soạn". | BR-GEO-09, 14; D-GEO-028, 079 |
| BR-GEO-186 | Lần đầu học sinh xem lời giải của một bài ghi một sự kiện mở lời giải và tiến độ thành 3/3. | REQ-GEO-22; M-GEO-14 |

- **Ánh xạ dữ liệu:** `architecture/03_schema.sql#solution` (các bước, đối tượng tô màu của từng bước; chỉ đọc qua hàm cổng), `#assignment_progress`.

### Tiêu chí chấp nhận

**US-GEO-407-SC1 — Bấm một bước, hình tô đúng đối tượng** · Nguồn: REQ-GEO-09; D-GEO-009
- Given: BG-1 Đã mở (sau "Mở ngay"); An ở tab "Lời giải" có ba bước của HKG-01 (F-GEO-037).
- When: An bấm bước 2 "Xác định góc cần tìm".
- Then: bước 2 nổi lên; hình tô SD, AD và góc SDA; hiện dòng "Đang tô trên hình: SD, AD, góc SDA".

**US-GEO-407-SC2 — Đổi bước thì đổi phần tô** · Nguồn: REQ-GEO-09
- Given: bước 2 đang chọn.
- When: An bấm bước 1 "Tìm hình chiếu của SD".
- Then: hình tô SD và AD; góc SDA hết tô; dòng "Đang tô trên hình: SD, AD".

**US-GEO-407-SC3 — "Bước sau", "Bước trước"** · Nguồn: `Design/mvp-06`; REQ-GEO-09
- Given: bước 1 đang chọn.
- When: An bấm "Bước sau" hai lần rồi "Bước trước" một lần.
- Then: bước đang chọn lần lượt là 2, 3, rồi 2; ở bước 3 nút "Bước sau" không bấm được.

**US-GEO-407-SC4 — Nhãn của lời giải** · Nguồn: BR-GEO-09; REQ-GEO-29; D-GEO-028 · PROVISIONAL D-GEO-028
- Given: như SC1.
- When: An xem đầu tab "Lời giải".
- Then: có nhãn "Đã kiểm chứng" và nhãn "AI soạn"; không có nhãn "Giáo viên đã duyệt".

**US-GEO-407-SC5 — Xem lời giải được ghi nhận** · Nguồn: REQ-GEO-22; M-GEO-14
- Given: An có tiến độ 2/3 trên BG-1; BG-1 vừa Đã mở.
- When: An mở tab "Lời giải" lần đầu.
- Then: tiến độ thành 3/3; "Bài của em" ghi "Đã xem lời giải" và nút "Xem lại"; có đúng một sự kiện mở lời giải dù An mở tab nhiều lần.

**US-GEO-407-SC6 — Trên iPhone** · Nguồn: REQ-GEO-42; D-GEO-001
- Given: BG-1 Đã mở; An dùng iPhone màn dọc.
- When: mở tab "Lời giải" ở bảng dưới và bấm bước 2.
- Then: hình phía trên tô SD, AD, góc SDA; bảng dưới không che hết hình.

**US-GEO-407-FC1 — Bước không có đối tượng để tô** · Nguồn: REQ-GEO-09
- Given: một bài Đã mở có lời giải HKG-05, bước 1 "Áp dụng công thức" không gắn đối tượng nào.
- When: học sinh bấm bước đó.
- Then: hình không tô gì; không hiện dòng "Đang tô trên hình"; không có lỗi.

**US-GEO-407-FC2 — Chuyển bước nhanh** · Nguồn: REQ-GEO-09
- Given: như SC1.
- When: An bấm bước 1, 2, 3 liên tiếp trong chưa tới một giây.
- Then: khi dừng, hình chỉ mang phần tô của bước 3; không còn phần tô sót lại của bước 1 hoặc 2.

- **Mở và quyết định đang dùng:** D-GEO-028 (Q-CDT-12). Chip "Đáp số đã kiểm chứng" của khung `mvp-06` thay bằng chuỗi nhãn máy chuẩn (D-GEO-079; D-GEO-112). Lời giải đang khóa: xem US-GEO-608.
- **DoR:** Ready.

## US-GEO-408 — Đường vẽ thêm của từng bước hiện dần; "Vì sao nghĩ ra bước này?"

- **Story:** Là học sinh, tôi muốn đường vẽ thêm của từng bước lần lượt hiện lên hình và đọc được vì sao nghĩ ra bước đó, để học cách nghĩ. · Loại UI · Must · Cỡ M · Độ khó 4
- **Nguồn:** REQ-GEO-09 · D-GEO-009 (Final) · SRC-081, SRC-037 · `Design/mvp-06`
- **Góc nhìn người vận hành (M18):** học sinh bấm "Bước sau" và thấy hình được vẽ thêm dần như thầy cô vẽ trên bảng. Ca đáng kể: quay lại bước trước; nhảy thẳng tới bước cuối.
- **Phụ thuộc:** US-GEO-407.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-187 | Mỗi bước có thể kèm phần vẽ thêm (đường phụ, ký hiệu góc, số đo). Ở bước n, hình có phần vẽ thêm của mọi bước từ 1 tới n và không có phần của bước sau n. | D-GEO-009; REQ-GEO-09 |
| BR-GEO-188 | Bước đang chọn hiện khối "Vì sao nghĩ ra bước này?" với câu giải thích của bước đó. | REQ-GEO-09; `materials/` (cột "Vì sao nghĩ ra bước này") |

- **Ánh xạ dữ liệu:** `architecture/03_schema.sql#solution` (phần vẽ thêm và câu "vì sao" của từng bước).

### Tiêu chí chấp nhận

**US-GEO-408-SC1 — Phần vẽ thêm hiện dần theo bước** · Nguồn: REQ-GEO-09; D-GEO-009
- Given: BG-1 Đã mở; An ở bước 1 của lời giải HKG-01: hình có đường phụ AD nổi bật.
- When: An bấm "Bước sau" tới bước 2, rồi tới bước 3.
- Then: ở bước 2 hình có thêm ký hiệu góc vuông tại A; ở bước 3 có thêm cung góc tại D và số "45°".

**US-GEO-408-SC2 — "Vì sao nghĩ ra bước này?"** · Nguồn: REQ-GEO-09
- Given: An chọn bước 2.
- When: xem thẻ của bước 2.
- Then: có khối "Vì sao nghĩ ra bước này?" với câu "Có hình chiếu rồi thì góc cần tìm nằm trong một tam giác vuông." (F-GEO-037).

**US-GEO-408-SC3 — Quay lại bước trước thì phần vẽ thêm bớt đi** · Nguồn: REQ-GEO-09; `03_gate3_preliminary_stories.md` US-GEO-408
- Given: An đang ở bước 3 (hình có cung góc tại D và "45°").
- When: An bấm bước 1.
- Then: cung góc tại D, số "45°" và ký hiệu góc vuông tại A biến mất; đường phụ AD còn.

**US-GEO-408-SC4 — Nhảy thẳng tới bước cuối** · Nguồn: D-GEO-009
- Given: An vừa mở tab "Lời giải", bước 1 đang chọn.
- When: An bấm thẳng bước 3.
- Then: hình có đủ phần vẽ thêm của bước 1, 2 và 3.

**US-GEO-408-FC1 — Bước không có phần vẽ thêm** · Nguồn: REQ-GEO-09
- Given: lời giải HKG-03, bước 2 "Tính thể tích" không có phần vẽ thêm.
- When: học sinh chuyển từ bước 1 sang bước 2.
- Then: hình giữ nguyên như ở bước 1; khối "Vì sao nghĩ ra bước này?" đổi sang câu của bước 2.

- **Mở và quyết định đang dùng:** không có giả định riêng. Phần vẽ thêm của HKG-01 theo khung `mvp-06` (dữ liệu vàng §2); các đề khác do dữ liệu kho đề (US-GEO-504).
- **DoR:** Ready.

## US-GEO-409 — Hỏi đáp trong ngữ cảnh bài, có nguồn

- **Story:** Là học sinh, tôi muốn hỏi trợ lý trong bài và nhận câu trả lời dựa trên giáo trình, có nguồn, để hiểu thêm. · Loại Logic · Must · Cỡ L · Độ khó 5
- **Nguồn:** REQ-GEO-10, 48 · D-GEO-006 (Final), 030, 053, 072 · SRC-082, SRC-034 · NFR-GEO-050
- **Góc nhìn người vận hành (M18):** học sinh gõ một câu hỏi ngắn ở tab "Hỏi đáp" khi vướng một khái niệm. Ca đáng kể: hỏi chuyện ngoài bài; trợ lý bận; giáo viên đã tắt hỏi đáp. Các câu cố moi đáp số ở US-GEO-609.
- **Phụ thuộc:** US-GEO-103, 412, 603, 609.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-189 | Câu trả lời dựa trên kho bài giảng mẫu và kết thúc bằng dòng "Nguồn: Giáo án mẫu" hoặc "Nguồn: AI soạn" kèm tên bài giảng bấm được. Nguồn nêu ra phải là một bài giảng có trong kho. | D-GEO-006, 030; REQ-GEO-10 |
| BR-GEO-190 | Câu hỏi ngoài kho bài giảng: trả lời MSG-GEO-36, không kèm nguồn. | REQ-GEO-10 |
| BR-GEO-191 | Câu hỏi dài từ 1 đến 1000 ký tự. | `architecture/03_schema.sql#qa_turn` (QA-GEO-01) |
| BR-GEO-192 | Từ lúc gửi tới lúc có câu trả lời hiện MSG-GEO-15; không hiện câu trả lời dở dang. | REQ-GEO-48 |
| BR-GEO-193 | Mỗi câu hỏi gửi thành công ghi một sự kiện hỏi và lưu câu hỏi, câu trả lời vào nhật ký của học sinh đó. Câu trả lời được lưu là câu đã qua Kiểm soát đáp án (câu thay thế MSG-GEO-35 nếu bị chặn), không bao giờ là câu gốc bị chặn; chỉ người hỏi đọc được; thời hạn lưu giữ theo D-GEO-128. | REQ-GEO-22, 10; D-GEO-159, 128 |
| BR-GEO-194 | Bài giao tắt "Hỏi đáp (không nêu đáp số)" thì học sinh không hỏi được ở bài đó. | D-GEO-053 |

- **Ánh xạ dữ liệu:** `architecture/03_schema.sql#qa_turn`, `#lecture`, `#event_log`, `#assignment` (cho phép hỏi đáp), `#ai_call_log`.

### Tiêu chí chấp nhận

**US-GEO-409-SC1 — Câu trả lời có nguồn "Giáo án mẫu"** · Nguồn: REQ-GEO-10; D-GEO-006, 030 · PROVISIONAL D-GEO-030
- Given: An đang làm BG-1 (Sắp mở), tab "Hỏi đáp", placeholder "Hỏi về bài này…".
- When: An gửi "Vì sao A là hình chiếu của S?" (F-GEO-055).
- Then: câu trả lời giải thích khái niệm hình chiếu vuông góc và kết thúc bằng "Nguồn: Giáo án mẫu" kèm tên bài giảng "Hình chiếu vuông góc" bấm được.

**US-GEO-409-SC2 — Câu trả lời có nguồn "AI soạn"** · Nguồn: REQ-GEO-10; D-GEO-029, 030 · PROVISIONAL D-GEO-029
- Given: Dũng đang làm BG-1.
- When: Dũng gửi "Tan của một góc là gì?" (F-GEO-058).
- Then: câu trả lời kết thúc bằng "Nguồn: AI soạn" kèm tên bài giảng "Tỉ số lượng giác trong tam giác vuông".

**US-GEO-409-SC3 — Câu hỏi ngoài giáo trình** · Nguồn: REQ-GEO-10; SRC-082
- Given: An đang làm BG-1.
- When: An gửi "Thủ đô của nước Pháp là gì?" (F-GEO-059).
- Then: câu trả lời là MSG-GEO-36; không có dòng "Nguồn".

**US-GEO-409-SC4 — Màn chờ khi trợ lý đang trả lời** · Nguồn: REQ-GEO-48
- Given: nhà cung cấp giả lập trả lời sau 5 giây.
- When: An gửi câu hỏi F-GEO-055.
- Then: trong 5 giây đó hiện MSG-GEO-15, ô nhập không gửi được câu thứ hai; sau đó câu trả lời hiện nguyên vẹn một lần.

**US-GEO-409-SC5 — Câu hỏi được ghi nhận** · Nguồn: REQ-GEO-22, 14; M-GEO-22
- Given: dữ liệu lớp như F-GEO-050…054 (thẻ "câu hỏi" = 7); Chi chưa hỏi câu nào.
- When: Chi gửi câu F-GEO-055 trên BG-1 và nhận được câu trả lời.
- Then: Theo dõi lớp của BG-1 hiện thẻ "câu hỏi" = 8 và dòng Chi ghi "1 câu"; câu hỏi có trong nhật ký hỏi đáp của Chi.

**US-GEO-409-SC6 — Nguồn nêu ra luôn mở được** · Nguồn: D-GEO-006; `03_gate3_preliminary_stories.md` US-GEO-409 ("trợ lý bịa nguồn")
- Given: câu trả lời của SC1.
- When: An bấm tên bài giảng ở dòng "Nguồn".
- Then: hộp bài giảng "Hình chiếu vuông góc" mở (US-GEO-411); không có câu trả lời nào mang tên một bài giảng không có trong kho F-GEO-090, F-GEO-091.

**US-GEO-409-FC1 — Trợ lý lỗi** · Nguồn: D-GEO-072 · PROVISIONAL D-GEO-072
- Given: nhà cung cấp giả lập ở chế độ trả lỗi.
- When: An gửi câu F-GEO-055.
- Then: hiện MSG-GEO-14 và "Thử lại"; câu hỏi của An vẫn hiện trong khung; không có sự kiện hỏi nào được tính cho tới khi có câu trả lời.

**US-GEO-409-FC2 — Câu hỏi rỗng hoặc quá dài** · Nguồn: QA-GEO-01; REQ-GEO-43 · PROVISIONAL QA-GEO-01
- Given: tab "Hỏi đáp" của BG-1.
- When: An gửi câu rỗng, rồi gửi câu 1001 ký tự.
- Then: mỗi lần hiện MSG-GEO-37; không có lượt gọi AI; không có sự kiện hỏi.

**US-GEO-409-FC3 — Giáo viên đã tắt hỏi đáp** · Nguồn: D-GEO-053; XF-07 · PROVISIONAL D-GEO-053
- Given: BG-5 có "Hỏi đáp (không nêu đáp số)" tắt (F-GEO-044); Khánh mở BG-5.
- When: Khánh mở tab "Hỏi đáp", rồi gửi thẳng một câu hỏi tới máy chủ.
- Then: tab hiện MSG-GEO-33, không có ô nhập; yêu cầu gửi thẳng nhận `FORBIDDEN`; không có lượt gọi AI.

- **Mở và quyết định đang dùng:** D-GEO-030, 029 (Q-CDT), D-GEO-053 (Q-GV), D-GEO-072; D-GEO-159 (lưu câu trả lời đã qua Kiểm soát đáp án); QA-GEO-01. Cách trợ lý chọn bài giảng làm nguồn thuộc cổng AI (giả lập ở MVP); AC chỉ ràng buộc nguồn phải thuộc kho. FC1: cách tính sự kiện khi lỗi là cách hiểu của BA theo BR-GEO-193.
- **DoR:** Ready.

## US-GEO-410 — Nhật ký hỏi đáp lọc theo bài

- **Story:** Là học sinh, tôi muốn xem lại các câu hỏi đã hỏi, lọc theo bài, để ôn lại. · Loại UI · Should · Cỡ S · Độ khó 2
- **Nguồn:** REQ-GEO-10, 36 · D-GEO-078, 159, 168 · SRC-105, SRC-082 · NFR-GEO-040
- **Góc nhìn người vận hành (M18):** học sinh mở "Lịch sử" trước giờ kiểm tra để đọc lại điều mình từng hỏi. Ca đáng kể: bài chưa hỏi câu nào.
- **Phụ thuộc:** US-GEO-409.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-195 | Mục "Lịch sử" ở thanh bên của học sinh chỉ có nhật ký hỏi đáp: danh sách câu hỏi của chính em, mới nhất trước, lọc được theo bài. | D-GEO-078; SRC-105 |
| BR-GEO-196 | Mở một dòng thì hiện câu trả lời đã lưu (câu đã qua Kiểm soát đáp án), không gọi AI lại. | REQ-GEO-10; D-GEO-159 |
| BR-GEO-197 | Danh sách tải theo con trỏ, có nút "Tải thêm" khi còn; không hiện tổng số. | M-004 (2); NFR-GEO-040 |

- **Ánh xạ dữ liệu:** `architecture/03_schema.sql#qa_turn`.

### Tiêu chí chấp nhận

**US-GEO-410-SC1 — Xem nhật ký của mình** · Nguồn: REQ-GEO-10; D-GEO-078 · PROVISIONAL D-GEO-078
- Given: An đã hỏi ba câu trên BG-1 (F-GEO-055, 056, 057).
- When: An bấm "Lịch sử" ở thanh bên.
- Then: danh sách có ba câu đó, câu mới nhất ở trên, mỗi dòng ghi câu hỏi, tên bài "Hình chóp S.ABCD: tính góc giữa SD và mặt đáy" và giờ hỏi.

**US-GEO-410-SC2 — Lọc theo bài** · Nguồn: REQ-GEO-36; SRC-105
- Given: An có ba câu trên BG-1 và một câu trên BG-3.
- When: An chọn lọc theo bài BG-1.
- Then: chỉ còn ba câu của BG-1.

**US-GEO-410-SC3 — Mở lại câu trả lời đã lưu** · Nguồn: REQ-GEO-10; D-GEO-159 · PROVISIONAL D-GEO-159
- Given: danh sách của SC1; số bản ghi lượt gọi AI là n.
- When: An bấm dòng "Vì sao A là hình chiếu của S?".
- Then: câu trả lời đã nhận hôm trước hiện ra cùng dòng "Nguồn"; số bản ghi lượt gọi AI vẫn là n.

**US-GEO-410-SC4 — Chỉ thấy câu của mình** · Nguồn: BR-GEO-12; REQ-GEO-28
- Given: Bình có hai câu trên BG-1.
- When: An mở "Lịch sử" và lọc theo BG-1.
- Then: không có câu nào của Bình.

**US-GEO-410-SC5 — Câu trả lời từng bị chặn mở lại vẫn là câu thay thế** · Nguồn: D-GEO-159; BR-GEO-02 · PROVISIONAL D-GEO-159
- Given: BG-1 Sắp mở; Bình đã gửi F-GEO-056 và nhà cung cấp giả lập trả "Góc giữa SD và mặt đáy là 45°." (F-GEO-114), câu này bị chặn và Bình đã nhận MSG-GEO-35 (US-GEO-609-FC1).
- When: Bình mở "Lịch sử" và bấm dòng "Góc cần tìm là góc nào trên hình?".
- Then: câu trả lời hiện ra là câu MSG-GEO-35 Bình đã nhận; dữ liệu trả về không chứa "45°" và không chứa câu gốc của AI.

**US-GEO-410-FC1 — Bài chưa có câu hỏi** · Nguồn: `03_gate3_preliminary_stories.md` US-GEO-410
- Given: An chưa hỏi câu nào trên BG-2.
- When: An lọc theo bài BG-2.
- Then: hiện MSG-GEO-38.

**US-GEO-410-FC2 — Con trỏ hỏng** · Nguồn: M-004 (1, 2); NFR-GEO-040
- Given: phiên của An.
- When: gọi danh sách nhật ký với con trỏ `khong-phai-con-tro` (F-GEO-084).
- Then: `INVALID_CURSOR`; màn hiện MSG-GEO-05; không có dòng nào được trả.

- **Mở và quyết định đang dùng:** D-GEO-078 ("Lịch sử" chỉ là nhật ký hỏi đáp); D-GEO-159 (có lưu câu trả lời, chỉ người hỏi đọc; giáo viên không đọc: US-GEO-104-FC9); kích thước trang theo `architecture/04_query_catalogue.md`, trần chung 100 (D-GEO-168).
- **DoR:** Ready.

## US-GEO-411 — Từ khóa bấm được mở bài giảng có nguồn và nhãn

- **Story:** Là học sinh, tôi muốn bấm từ khóa trong gợi ý, lời giải, câu trả lời để đọc bài giảng ngắn có ghi nguồn, để nhớ lại kiến thức. · Loại UI · Must · Cỡ M · Độ khó 3
- **Nguồn:** REQ-GEO-11 · D-GEO-007 (Final), 027, 029, 030 · SRC-035, SRC-083, SRC-039
- **Góc nhìn người vận hành (M18):** học sinh đọc gợi ý thấy chữ "hình chiếu" gạch chân chấm và bấm vào. Ca đáng kể: bài giảng do AI viết chưa được duyệt; bài giảng vô tình nói ra đáp số của bài đang khóa.
- **Phụ thuộc:** US-GEO-412, 404, 407, 409.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-198 | Cụm từ trùng một từ khóa của kho bài giảng, khi xuất hiện trong gợi ý, lời giải hoặc câu trả lời của trợ lý, được gạch chân chấm và bấm được; bấm thì mở hộp bài giảng (`COM_HOP_BAI_GIANG`): tên khái niệm, nội dung ngắn, dòng "Nguồn: …". | D-GEO-007; REQ-GEO-11 |
| BR-GEO-199 | Bài giảng nguồn "AI soạn" mang nhãn "AI soạn · chờ giáo viên duyệt". Bài giảng nguồn "Giáo án mẫu" chỉ hiện dòng nguồn, không mang "Giáo viên đã duyệt". | D-GEO-029; D-GEO-028; D-GEO-111 |
| BR-GEO-18 | Giờ mở áp dụng cho mọi kênh: bài giảng mở từ một bài đang khóa không chứa đáp số hay bước giải của bài đó. | SRC-028; D-GEO-027 |

- **Ánh xạ dữ liệu:** `architecture/03_schema.sql#keyword`, `#lecture`.

### Tiêu chí chấp nhận

**US-GEO-411-SC1 — Bấm từ khóa trong gợi ý** · Nguồn: REQ-GEO-11; D-GEO-007, 030 · PROVISIONAL D-GEO-030
- Given: An đang xem Gợi ý 1 của BG-1; chữ "hình chiếu" được gạch chân chấm (F-GEO-092).
- When: An bấm "hình chiếu".
- Then: hộp bài giảng "Hình chiếu vuông góc" mở với nội dung và dòng "Nguồn: Giáo án mẫu".

**US-GEO-411-SC2 — Bài giảng do AI viết mang nhãn chờ duyệt** · Nguồn: D-GEO-029 · PROVISIONAL D-GEO-029
- Given: câu trả lời của trợ lý cho Dũng có từ khóa "Tỉ số lượng giác trong tam giác vuông".
- When: Dũng bấm từ khóa đó.
- Then: hộp bài giảng hiện "Nguồn: AI soạn" và nhãn "AI soạn · chờ giáo viên duyệt".

**US-GEO-411-SC3 — Từ khóa trong lời giải** · Nguồn: REQ-GEO-11
- Given: BG-1 Đã mở; bước 1 của lời giải có chữ "hình chiếu".
- When: An bấm chữ đó.
- Then: cùng hộp bài giảng "Hình chiếu vuông góc" như ở SC1.

**US-GEO-411-SC4 — Đóng hộp bài giảng** · Nguồn: REQ-GEO-11
- Given: hộp bài giảng đang mở từ Gợi ý 1; An đã thêm điểm M trên hình.
- When: An đóng hộp.
- Then: An trở về tab "Gợi ý" đúng chỗ đang đọc; điểm M và tiến độ không đổi.

**US-GEO-411-FC1 — Cụm từ không có bài giảng** · Nguồn: REQ-GEO-11
- Given: một câu trả lời có chữ "thiết diện" (F-GEO-093), không thuộc kho bài giảng.
- When: An xem câu đó, rồi gọi thẳng máy chủ xin bài giảng của "thiết diện".
- Then: chữ "thiết diện" không gạch chân, không bấm được; yêu cầu gọi thẳng nhận `NOT_FOUND` (MSG-GEO-39).

**US-GEO-411-FC2 — Bài giảng không lộ đáp số của bài đang khóa** · Nguồn: BR-GEO-18; D-GEO-027; REQ-GEO-17 · PROVISIONAL D-GEO-027
- Given: BG-1 đang Sắp mở.
- When: An mở bài giảng "Góc giữa đường thẳng và mặt phẳng" từ Gợi ý 1 và ghi lại dữ liệu trả về.
- Then: nội dung không nhắc đề HKG-01 và không có bước nào của F-GEO-037.

**US-GEO-411-FC3 — Không dùng nhãn "Giáo viên đã duyệt"** · Nguồn: D-GEO-028; D-GEO-111 · PROVISIONAL D-GEO-111
- Given: bảy bài giảng nguồn "Giáo án mẫu" (F-GEO-090).
- When: mở lần lượt từng bài.
- Then: mỗi bài có "Nguồn: Giáo án mẫu"; không bài nào hiện nhãn "Giáo viên đã duyệt".

- **Mở và quyết định đang dùng:** D-GEO-029, 030 (Q-CDT); D-GEO-111. Nút "Hỏi thêm về khái niệm này", "từ khóa liên quan", hình nhỏ trong bài giảng có ở prototype nhưng không có trong REQ-GEO-11: không viết AC.
- **DoR:** Ready.

## US-GEO-412 — Dữ liệu: kho bài giảng mẫu

- **Story:** Là nhóm sản phẩm, tôi muốn kho bài giảng mẫu 10 khái niệm có nguồn và nhãn, để từ khóa và hỏi đáp có chỗ dựa. · Loại Data · Must · Cỡ M · Độ khó 3
- **Nguồn:** REQ-GEO-11, 10 · D-GEO-029, 030, 076, 137 · SRC-116, SRC-042
- **Góc nhìn người vận hành (M18):** người nạp dữ liệu chạy một lần. Ca đáng kể: bài giảng chép luôn lời giải của một đề trong kho; bài giảng thiếu nguồn.
- **Phụ thuộc:** không.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-200 | Kho có 10 bài giảng: 7 bài nguồn "Giáo án mẫu", 3 bài nguồn "AI soạn" (tên ở F-GEO-090, F-GEO-091). Mỗi bài có khái niệm, nội dung, nguồn, nhãn người và ít nhất một từ khóa. | D-GEO-030; `Design/prototype/README.md` (tham chiếu hành vi) |
| BR-GEO-110 | Kho bài giảng mẫu là nội dung hệ thống, mọi trường đọc được, chỉ hệ thống ghi. | D-GEO-076 |
| BR-GEO-201 | Bài giảng không chứa đề, bước giải hay đáp số của một đề cụ thể trong kho đề. | D-GEO-027; `03_gate3_preliminary_stories.md` US-GEO-412 |
| BR-GEO-296 | Nội dung bảy bài nguồn "Giáo án mẫu" lấy từ bản mẫu tương tác làm dữ liệu mẫu; mỗi bài mang cờ "chưa có người soát" lưu cùng bài giảng. Hộp bài giảng của bài mang cờ hiện dòng "Nội dung đang chờ giáo viên soát" cho cả học sinh và giáo viên, bên cạnh dòng "Nguồn: Giáo án mẫu". | D-GEO-137, 181 |

- **Ánh xạ dữ liệu:** `architecture/03_schema.sql#lecture`, `#keyword`.

### Tiêu chí chấp nhận

**US-GEO-412-SC1 — Đủ 10 bài giảng với nguồn và nhãn** · Nguồn: D-GEO-030, 029, 137 · PROVISIONAL D-GEO-137
- Given: dữ liệu kho bài giảng mẫu.
- When: nạp.
- Then: có đúng 10 bài; 7 bài F-GEO-090 mang nguồn "Giáo án mẫu" và cờ "chưa có người soát"; 3 bài F-GEO-091 mang nguồn "AI soạn" và nhãn "AI soạn · chờ giáo viên duyệt".

**US-GEO-412-SC4 — Cờ chờ soát hiện cho học sinh và giáo viên** · Nguồn: D-GEO-137, 181 · PROVISIONAL D-GEO-181
- Given: kho đã nạp; bài "Hình chiếu vuông góc" (F-GEO-090) mang cờ "chưa có người soát".
- When: An bấm từ khóa "hình chiếu" trong Gợi ý 1 của BG-1; Giáo viên mẫu bấm cùng từ khóa trong Gợi ý 1 ở trang chi tiết HKG-01.
- Then: ở cả hai hộp bài giảng có dòng "Nguồn: Giáo án mẫu" và dòng "Nội dung đang chờ giáo viên soát"; ba bài F-GEO-091 không hiện dòng này.

**US-GEO-412-SC2 — Mỗi bài có từ khóa trỏ tới** · Nguồn: REQ-GEO-11
- Given: kho đã nạp.
- When: tra từ khóa "hình chiếu" và "Góc giữa đường thẳng và mặt phẳng" (F-GEO-092).
- Then: lần lượt ra bài "Hình chiếu vuông góc" và bài "Góc giữa đường thẳng và mặt phẳng"; không bài giảng nào thiếu từ khóa.

**US-GEO-412-SC3 — Mọi trường đọc được** · Nguồn: D-GEO-076 · PROVISIONAL D-GEO-076
- Given: học sinh An lớp 9C của "THCS Mẫu C".
- When: bấm một từ khóa trong gợi ý của BG-C.
- Then: mở được cùng bài giảng như học sinh "THPT Mẫu A".

**US-GEO-412-FC1 — Bài giảng chứa lời giải của một đề trong kho** · Nguồn: D-GEO-027; BR-GEO-18 · PROVISIONAL D-GEO-027
- Given: kho đã nạp và sáu đề F-GEO-031…036.
- When: rà nội dung 10 bài giảng.
- Then: không bài nào chứa nguyên văn một đề bài của kho hay một bước giải F-GEO-037…039.

**US-GEO-412-FC2 — Bài giảng "Giáo án mẫu" thiếu tham chiếu nguồn** · Nguồn: D-GEO-007 ("ghi rõ nguồn")
- Given: dữ liệu nạp có một bài nguồn "Giáo án mẫu" không ghi tên tài liệu và mục.
- When: nạp.
- Then: bài đó bị từ chối, kết quả nạp nêu tên bài; các bài khác nạp bình thường.

- **Mở và quyết định đang dùng:** D-GEO-030 (10 khái niệm của prototype là tham chiếu hành vi; nội dung chữ của 7 bài "Giáo án mẫu" không có trong `materials/Giao-an-Hinh-khong-gian-11A1.docx`, BA đã mở tệp: tệp chỉ có mục tiêu, định hướng, tiến trình và sáu bài tập. D-GEO-137, 181: MVP dùng nội dung trong bản mẫu tương tác làm dữ liệu mẫu, đánh dấu "chưa có người soát" và hiện "Nội dung đang chờ giáo viên soát" cho cả học sinh và giáo viên; người soát nội dung còn mở ở KG-02, KG-22). Chỗ lưu cờ ở bảng bài giảng: ARCH.
- **DoR:** Ready về cấu trúc; nội dung bài giảng PROVISIONAL theo D-GEO-030, 137.
