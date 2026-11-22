-- E4 · KHO BÀI GIẢNG MẪU (US-GEO-412; fixture F-GEO-090, F-GEO-091, F-GEO-092; D-GEO-030, 137, 181).
-- SINH TỰ ĐỘNG bởi app/db/tools/gen-e4-lectures.py từ server/src/main/resources/knowledge/lectures.json: không sửa tay.
-- Nội dung hệ thống của org nền tảng (mọi trường đọc được, BR-GEO-110). 7 bài "Giáo án mẫu" human_reviewed=false
-- (dòng "Nội dung đang chờ giáo viên soát"); 3 bài "AI soạn". Không bài nào ở trạng thái "Giáo viên đã duyệt" (D-GEO-028).
INSERT INTO lecture (id, org_id, subject_id, slug, title, body, source_kind, approval_status, source_ref, human_reviewed) VALUES ('00000000-0000-7000-8000-000000900401', platform_org_id(), 1, 'duong-thang-vuong-goc-voi-mat-phang', 'Đường thẳng vuông góc với mặt phẳng', 'Đường thẳng d gọi là vuông góc với mặt phẳng (P) nếu d vuông góc với mọi đường thẳng nằm trong (P). Kí hiệu: d ⊥ (P).

Dấu hiệu nhận biết: nếu d vuông góc với hai đường thẳng cắt nhau cùng nằm trong (P) thì d ⊥ (P). Chỉ một đường thì chưa đủ, và hai đường song song cũng chưa đủ.

Hệ quả hay dùng: khi d ⊥ (P) thì d vuông góc với mọi đường thẳng trong (P), kể cả những đường không đi qua chân của d. Nhờ đó ta có ngay các tam giác vuông để tính độ dài và góc.', 'teacher_authored', 'ai_drafted', 'Giáo án Hình không gian 11A1 (bản mẫu tương tác), mục 1.1', false);
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'đường thẳng vuông góc với mặt phẳng', 'đường thẳng vuông góc với mặt phẳng', '00000000-0000-7000-8000-000000900401');
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'vuông góc với mặt phẳng', 'vuông góc với mặt phẳng', '00000000-0000-7000-8000-000000900401');
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'vuông góc với mặt đáy', 'vuông góc với mặt đáy', '00000000-0000-7000-8000-000000900401');
INSERT INTO lecture (id, org_id, subject_id, slug, title, body, source_kind, approval_status, source_ref, human_reviewed) VALUES ('00000000-0000-7000-8000-000000900402', platform_org_id(), 1, 'hinh-chieu-vuong-goc', 'Hình chiếu vuông góc', 'Cho mặt phẳng (P) và điểm M không nằm trên (P). Qua M kẻ đường thẳng vuông góc với (P), cắt (P) tại H. Điểm H gọi là hình chiếu vuông góc của M trên (P). Nếu M đã nằm trên (P) thì hình chiếu của M là chính nó.

Muốn tìm hình chiếu của một đoạn thẳng MN trên (P), ta tìm hình chiếu H của M và K của N, rồi nối HK. Trường hợp hay gặp: N đã nằm trên (P), khi đó hình chiếu của MN là đoạn HN.

Cách nhận ra nhanh: nếu trong đề có một đường thẳng đi qua M và vuông góc với (P), thì chân của đường thẳng đó trên (P) chính là hình chiếu của M.', 'teacher_authored', 'ai_drafted', 'Giáo án Hình không gian 11A1 (bản mẫu tương tác), mục 1.2', false);
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'hình chiếu vuông góc', 'hình chiếu vuông góc', '00000000-0000-7000-8000-000000900402');
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'hình chiếu', 'hình chiếu', '00000000-0000-7000-8000-000000900402');
INSERT INTO lecture (id, org_id, subject_id, slug, title, body, source_kind, approval_status, source_ref, human_reviewed) VALUES ('00000000-0000-7000-8000-000000900403', platform_org_id(), 1, 'goc-giua-duong-thang-va-mat-phang', 'Góc giữa đường thẳng và mặt phẳng', 'Nếu đường thẳng d vuông góc với mặt phẳng (P) thì góc giữa d và (P) bằng 90°. Nếu không, góc giữa d và (P) là góc giữa d và hình chiếu vuông góc d′ của nó trên (P). Góc này luôn nằm trong khoảng từ 0° đến 90°.

Ba việc cần làm: (1) tìm giao điểm I của d với (P); (2) lấy một điểm M khác trên d, tìm hình chiếu H của M trên (P), nối HI để được d′; (3) góc cần tìm là góc nhọn tại I.

Góc đó nằm trong tam giác MHI, là một tam giác vuông tại H, nên tính được bằng tỉ số lượng giác.', 'teacher_authored', 'ai_drafted', 'Giáo án Hình không gian 11A1 (bản mẫu tương tác), mục 1.3', false);
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'góc giữa đường thẳng và mặt phẳng', 'góc giữa đường thẳng và mặt phẳng', '00000000-0000-7000-8000-000000900403');
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'góc giữa đường và mặt', 'góc giữa đường và mặt', '00000000-0000-7000-8000-000000900403');
INSERT INTO lecture (id, org_id, subject_id, slug, title, body, source_kind, approval_status, source_ref, human_reviewed) VALUES ('00000000-0000-7000-8000-000000900404', platform_org_id(), 1, 'khoang-cach-giua-hai-mat-phang-song-song', 'Khoảng cách giữa hai mặt phẳng song song', 'Cho hai mặt phẳng song song (P) và (Q). Khoảng cách giữa chúng là khoảng cách từ một điểm bất kì trên (P) đến (Q). Kết quả không phụ thuộc vào điểm được chọn.

Để tính, chọn một điểm M thuận tiện trên (P), hạ đường vuông góc MH xuống (Q); độ dài MH là khoảng cách cần tìm.

Nếu trên hình đã có sẵn một đường thẳng vuông góc với mặt phẳng (P) và (Q), thì đoạn của nó nằm giữa hai mặt phẳng chính là MH, không cần dựng thêm.', 'teacher_authored', 'ai_drafted', 'Giáo án Hình không gian 11A1 (bản mẫu tương tác), mục 2.2', false);
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'khoảng cách giữa hai mặt phẳng song song', 'khoảng cách giữa hai mặt phẳng song song', '00000000-0000-7000-8000-000000900404');
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'khoảng cách giữa hai mặt phẳng', 'khoảng cách giữa hai mặt phẳng', '00000000-0000-7000-8000-000000900404');
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'hai mặt phẳng song song', 'hai mặt phẳng song song', '00000000-0000-7000-8000-000000900404');
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'mặt phẳng song song', 'mặt phẳng song song', '00000000-0000-7000-8000-000000900404');
INSERT INTO lecture (id, org_id, subject_id, slug, title, body, source_kind, approval_status, source_ref, human_reviewed) VALUES ('00000000-0000-7000-8000-000000900405', platform_org_id(), 1, 'the-tich-khoi-lang-tru', 'Thể tích khối lăng trụ', 'Thể tích khối lăng trụ bằng diện tích đáy nhân với chiều cao: V = S · h. Chiều cao là khoảng cách giữa hai mặt đáy; với lăng trụ đứng, chiều cao bằng độ dài cạnh bên.

Diện tích đáy tính theo hình dạng của đáy. Một số công thức hay dùng: tam giác đều cạnh a có S = a²√3/4; tam giác vuông có S bằng nửa tích hai cạnh góc vuông; hình chữ nhật có S bằng dài nhân rộng.

Nhớ kiểm tra đơn vị: diện tích đáy tính bằng cm² và chiều cao tính bằng cm thì thể tích tính bằng cm³.', 'teacher_authored', 'ai_drafted', 'Giáo án Hình không gian 11A1 (bản mẫu tương tác), mục 3.1', false);
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'thể tích khối lăng trụ', 'thể tích khối lăng trụ', '00000000-0000-7000-8000-000000900405');
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'thể tích lăng trụ', 'thể tích lăng trụ', '00000000-0000-7000-8000-000000900405');
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'lăng trụ đứng', 'lăng trụ đứng', '00000000-0000-7000-8000-000000900405');
INSERT INTO lecture (id, org_id, subject_id, slug, title, body, source_kind, approval_status, source_ref, human_reviewed) VALUES ('00000000-0000-7000-8000-000000900406', platform_org_id(), 1, 'dien-tich-xung-quanh-hinh-non', 'Diện tích xung quanh hình nón', 'Hình nón có bán kính đáy r, chiều cao h và đường sinh l. Đường sinh là đoạn nối đỉnh với một điểm trên đường tròn đáy; chiều cao là đoạn nối đỉnh với tâm đáy. Ba đoạn r, h, l tạo thành một tam giác vuông, nên l² = r² + h².

Diện tích xung quanh: Sxq = πrl. Diện tích toàn phần: Stp = πrl + πr². Thể tích: V = ⅓πr²h.

Lỗi hay gặp: dùng đường sinh thay cho chiều cao trong công thức thể tích. Nếu đề cho r và l, phải tính h trước rồi mới tính thể tích.', 'teacher_authored', 'ai_drafted', 'Giáo án Hình không gian 11A1 (bản mẫu tương tác), mục 4.1', false);
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'diện tích xung quanh hình nón', 'diện tích xung quanh hình nón', '00000000-0000-7000-8000-000000900406');
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'diện tích xung quanh', 'diện tích xung quanh', '00000000-0000-7000-8000-000000900406');
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'đường sinh', 'đường sinh', '00000000-0000-7000-8000-000000900406');
INSERT INTO lecture (id, org_id, subject_id, slug, title, body, source_kind, approval_status, source_ref, human_reviewed) VALUES ('00000000-0000-7000-8000-000000900407', platform_org_id(), 1, 'dien-tich-toan-phan-hinh-tru', 'Diện tích toàn phần hình trụ', 'Hình trụ có bán kính đáy r và chiều cao h. Mặt xung quanh khi trải phẳng là một hình chữ nhật có một cạnh bằng chu vi đáy 2πr và cạnh kia bằng h, nên Sxq = 2πrh.

Diện tích toàn phần gồm diện tích xung quanh và diện tích hai đáy: Stp = 2πrh + 2πr² = 2πr(r + h).

Thể tích: V = πr²h. Khi làm bài, viết công thức trước, thay số sau, và giữ nguyên π trong kết quả nếu đề không yêu cầu làm tròn.', 'teacher_authored', 'ai_drafted', 'Giáo án Hình không gian 11A1 (bản mẫu tương tác), mục 4.2', false);
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'diện tích toàn phần hình trụ', 'diện tích toàn phần hình trụ', '00000000-0000-7000-8000-000000900407');
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'diện tích toàn phần', 'diện tích toàn phần', '00000000-0000-7000-8000-000000900407');
INSERT INTO lecture (id, org_id, subject_id, slug, title, body, source_kind, approval_status, source_ref, human_reviewed) VALUES ('00000000-0000-7000-8000-000000900408', platform_org_id(), 1, 'ti-so-luong-giac-trong-tam-giac-vuong', 'Tỉ số lượng giác trong tam giác vuông', 'Trong tam giác vuông, xét một góc nhọn α. Cạnh đối là cạnh góc vuông không chạm vào α, cạnh kề là cạnh góc vuông chạm vào α, cạnh huyền là cạnh đối diện với góc vuông.

sin α = cạnh đối / cạnh huyền; cos α = cạnh kề / cạnh huyền; tan α = cạnh đối / cạnh kề.

Khi biết hai cạnh góc vuông, dùng tan là nhanh nhất vì không cần tính cạnh huyền. Có giá trị của tan rồi, em tra bảng giá trị lượng giác của các góc đặc biệt hoặc dùng máy tính để suy ra số đo góc.

Lưu ý: phải chỉ ra đúng tam giác vuông và đúng đỉnh góc vuông trước khi lập tỉ số.', 'ai_written', 'ai_drafted', NULL, false);
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'tỉ số lượng giác trong tam giác vuông', 'tỉ số lượng giác trong tam giác vuông', '00000000-0000-7000-8000-000000900408');
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'tỉ số lượng giác', 'tỉ số lượng giác', '00000000-0000-7000-8000-000000900408');
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'tam giác vuông', 'tam giác vuông', '00000000-0000-7000-8000-000000900408');
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'tan của góc nhọn', 'tan của góc nhọn', '00000000-0000-7000-8000-000000900408');
INSERT INTO lecture (id, org_id, subject_id, slug, title, body, source_kind, approval_status, source_ref, human_reviewed) VALUES ('00000000-0000-7000-8000-000000900409', platform_org_id(), 1, 'hai-duong-thang-cheo-nhau', 'Hai đường thẳng chéo nhau', 'Hai đường thẳng chéo nhau là hai đường thẳng không cùng nằm trong một mặt phẳng nào: chúng không cắt nhau và cũng không song song.

Khoảng cách giữa hai đường thẳng chéo nhau là độ dài đoạn vuông góc chung, tức đoạn thẳng nối hai đường và vuông góc với cả hai.

Cách làm thường gặp: nếu hai đường chéo nhau lần lượt nằm trong hai mặt phẳng song song thì khoảng cách giữa hai đường bằng khoảng cách giữa hai mặt phẳng đó. Vì thế, việc đầu tiên là tìm hai mặt phẳng song song, mỗi mặt chứa một đường.', 'ai_written', 'ai_drafted', NULL, false);
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'hai đường thẳng chéo nhau', 'hai đường thẳng chéo nhau', '00000000-0000-7000-8000-000000900409');
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'hai đường chéo nhau', 'hai đường chéo nhau', '00000000-0000-7000-8000-000000900409');
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'đường chéo nhau', 'đường chéo nhau', '00000000-0000-7000-8000-000000900409');
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'chéo nhau', 'chéo nhau', '00000000-0000-7000-8000-000000900409');
INSERT INTO lecture (id, org_id, subject_id, slug, title, body, source_kind, approval_status, source_ref, human_reviewed) VALUES ('00000000-0000-7000-8000-000000900410', platform_org_id(), 1, 'mat-cau-ngoai-tiep', 'Mặt cầu ngoại tiếp', 'Mặt cầu ngoại tiếp một hình đa diện là mặt cầu đi qua tất cả các đỉnh của hình đó. Tâm của mặt cầu cách đều mọi đỉnh; khoảng cách đó là bán kính R.

Với hình hộp chữ nhật (hình lập phương là trường hợp riêng), các đường chéo cắt nhau tại trung điểm của mỗi đường. Điểm đó cách đều tám đỉnh nên là tâm mặt cầu ngoại tiếp, và R bằng nửa độ dài đường chéo. Hình hộp chữ nhật có ba kích thước m, n, p thì đường chéo dài √(m² + n² + p²), tính bằng định lí Pythagore trong hai tam giác vuông.

Khi đã có R: diện tích mặt cầu S = 4πR², thể tích khối cầu V = 4/3·πR³.', 'ai_written', 'ai_drafted', NULL, false);
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'mặt cầu ngoại tiếp', 'mặt cầu ngoại tiếp', '00000000-0000-7000-8000-000000900410');
INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), 'tâm mặt cầu', 'tâm mặt cầu', '00000000-0000-7000-8000-000000900410');
