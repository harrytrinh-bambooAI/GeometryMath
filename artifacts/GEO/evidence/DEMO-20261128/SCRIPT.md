# SCRIPT — Video giới thiệu MVP + chặng 2, 3, 5 (T-261128-73)

Phiên bản: v1.0 · 2026-11-28 · runner-T-261128-73

- Video tổng: `GEO-MVP-demo-full-1080p.mp4` — 09:46 (586.3 s), H.264 1920x1080 30 fps yuv420p, AAC 192k 48 kHz. Clip: `clips/NN-*.mp4` (19 clip).
- Giọng đọc tiếng Anh: macOS `say` offline, giọng **Samantha** (en_US), tốc độ 178 từ/phút; mỗi cảnh giữ màn đúng độ dài lời đọc (đo AIFF trước, `afinfo`). Phụ đề trên màn = lời đọc (tiếng Anh); nhãn giao diện app giữ tiếng Việt.
- Môi trường: worktree demo-video, profile dev, DB geo_e2e (seed vàng + móc /test-hooks), AI giả lập (MockAiProvider, không gọi AI thật). Cờ `geo.s5.grading` / `geo.s5.ai-grading` bật sẵn trong application-dev.yml (chỉ phiên dev local); clip E4/E6 (và E5 lỗi sai lớp) có nhãn góc "Stage 5 preview — off in release builds".
- Dữ liệu: chỉ tên giả của fixture (Cô Lan, Bình An, Chi Mai, …); không dữ liệu học sinh thật.
- Quay: Playwright `app/e2e/demo/*.demo.ts` (cấu hình riêng `demo.config.ts`, không thuộc bộ e2e), ghép bằng `make-video.mjs` (ffmpeg-static ngoài repo).

## Bảng phủ epic → clip

| Chặng | Epic | Clip | Mốc trong video tổng |
|---|---|---|---|
| — | — | 00-intro | 00:00–00:41 |
| 1 (MVP) | E5, E2/E3, E6 | 01-gv-kho-de-giao-bai | 00:41–01:32 |
| 1 (MVP) | E1, E3, E4 | 02-hs-vao-lop-lam-bai | 01:32–02:08 |
| 1 (MVP) | E7 | 03-gv-theo-doi-lop | 02:08–02:28 |
| 2 | S2-E8 | 04-gv-hs-bai-luyen | 02:28–03:02 |
| 2 | S2-E7 | 05-gv-trinh-chieu | 03:02–03:25 |
| 3 | S3-E1, S3-E3 | 06-gv-lop-hoc-ket-qua | 03:25–03:54 |
| 3 | S3-E4 | 07-gv-tong-quan | 03:54–04:09 |
| 5 | S5-E1 | 08-s5e1-giao-an | 04:09–04:47 |
| 5 | S5-E2 | 09-s5e2-ra-de-ma-tran | 04:47–05:24 |
| 5 | S5-E2 | 10-s5e2-de-nhap-tai-lieu | 05:24–05:44 |
| 5 | S5-E3 | 11-s5e3-kho-tri-thuc-hoi-dap | 05:44–06:16 |
| 5 | S5-E4 | 12-s5e4-rubric-nop-bai | 06:16–06:54 |
| 5 | S5-E4 | 13-s5e4-hang-cho-cham-tay | 06:54–07:26 |
| 5 | S5-E5 | 14-s5e5-thong-bao-cong-tac | 07:26–08:00 |
| 5 | S5-E5 | 15-s5e5-loi-sai-lop | 08:00–08:28 |
| 5 | S5-E6 | 16-s5e6-ai-de-xuat-diem | 08:28–08:59 |
| 5 | S5-E6 | 17-s5e6-song-song-bao-cao | 08:59–09:32 |
| — | — | 99-outro | 09:32–09:46 |

Chặng 5 đủ 6 epic: S5-E1 → 08 · S5-E2 → 09, 10 · S5-E3 → 11 · S5-E4 → 12, 13 · S5-E5 → 14, 15 · S5-E6 → 16, 17. Chặng 4 (môn thứ hai) chưa làm nên không có clip.

## Kịch bản từng clip

### 00-intro — Giới thiệu sản phẩm · by BambooAI

- Mốc: **00:00–00:41** (dài 41.4 s) · Chặng — · Epic —
- Mục tiêu: Giới thiệu sản phẩm và BambooAI: vấn đề, người dùng, nguyên tắc, lộ trình chặng 1–5
- Vai: —
- Tính năng thể hiện: Màn tiêu đề "Hình Học Không Gian Cùng AI — Geometry Math Support", "by BambooAI"
- Dữ liệu dùng: DOMAIN-PROFILE.md §1 + bảng chặng; BambooAI chỉ dùng tên (không có logo trong repo, không số liệu/khách hàng)

| Mốc (tổng) | Mốc (clip) | Voice-over (EN) = phụ đề |
|---|---|---|
| 00:00 | 0.5 s | Geometry Math Support. Solid geometry with A I, by BambooAI. |
| 00:06 | 6.2 s | Solid geometry is hard to picture on paper, and generic AI can be wrong, or give answers away too early. |
| 00:13 | 13.6 s | It is built for secondary school math teachers and their students. |
| 00:17 | 17.8 s | Figures and answers are verified by computation. Hints come first, solutions later. And the teacher always decides. |
| 00:26 | 26.1 s | This demo covers stages one, two, three and five. All data is sample data, and the AI is simulated. |

### 01-gv-kho-de-giao-bai — Giáo viên: Kho đề, chi tiết đề, giao bài

- Mốc: **00:41–01:32** (dài 50.9 s) · Chặng 1 (MVP) · Epic E5, E2/E3, E6
- Mục tiêu: Giáo viên tìm đề, xem chi tiết đề, giao bài
- Vai: Giáo viên
- Tính năng thể hiện: Kho đề + bộ lọc; chi tiết đề (hình 3D xoay, đáp số khớp tính toán, lời giải từng bước, gợi ý); giao bài cho lớp
- Dữ liệu dùng: Kho thử F-GEO-080 (HKG-01…48); sandbox E6 (lớp 11A1, 9B, HKG-06)

| Mốc (tổng) | Mốc (clip) | Voice-over (EN) = phụ đề |
|---|---|---|
| 00:42 | 0.8 s | [Title] Problem bank → Assign — Stage 1 · MVP — teacher finds a problem, checks the 3D figure and solution, assigns it |
| 00:50 | 9.1 s | The problem bank: solid geometry problems, labelled “verified” by the geometry engine |
| 00:57 | 16.6 s | Filter by school level, solid type and topic |
| 01:01 | 20.5 s | Upper secondary, angle problems |
| 01:05 | 23.8 s | Each problem has its statement, a 3D figure, the answer and a step-by-step solution |
| 01:11 | 30.6 s | The figure rotates with a mouse or a finger |
| 01:16 | 35.0 s | The answer matches the engine’s own computation |
| 01:20 | 38.7 s | To assign: pick a class, allow hints or questions, and set when the solution unlocks |
| 01:28 | 47.4 s | Assigned to class 11A1 |

### 02-hs-vao-lop-lam-bai — Học sinh: vào lớp, làm bài trên hình 3D, xem gợi ý

- Mốc: **01:32–02:08** (dài 36.3 s) · Chặng 1 (MVP) · Epic E1, E3, E4
- Mục tiêu: Học sinh vào lớp, làm bài trên hình 3D, xem gợi ý
- Vai: Học sinh
- Tính năng thể hiện: Vào lớp bằng tên + mã lớp; "Bài của em"; màn làm bài; gợi ý mức 1
- Dữ liệu dùng: Lớp mẫu mã HK7M2A (seed vàng); sandbox E4 (HS "Hà", HKG-01)

| Mốc (tổng) | Mốc (clip) | Voice-over (EN) = phụ đề |
|---|---|---|
| 01:33 | 0.7 s | [Title] Student: join, solve, hints — Stage 1 · MVP — join with a name and class code, work on the 3D figure, ask for hints step by step |
| 01:40 | 8.1 s | No account needed: just a display name and the class code |
| 01:46 | 14.1 s | “My work” lists everything the teacher has assigned |
| 01:49 | 17.7 s | The workspace: problem on the left, interactive 3D figure on the right |
| 01:55 | 23.7 s | Rotate the figure to see the spatial relations |
| 01:59 | 27.2 s | Hints come in levels, so the answer is not given away |
| 02:04 | 32.1 s | Hint one points the student in the right direction |

### 03-gv-theo-doi-lop — Giáo viên: theo dõi lớp theo thời gian thực

- Mốc: **02:08–02:28** (dài 19.7 s) · Chặng 1 (MVP) · Epic E7
- Mục tiêu: Giáo viên theo dõi lớp
- Vai: Giáo viên
- Tính năng thể hiện: Bốn thẻ tổng, bảng học sinh (không xếp hạng)
- Dữ liệu dùng: Hộp E7 golden() (5 HS)

| Mốc (tổng) | Mốc (clip) | Voice-over (EN) = phụ đề |
|---|---|---|
| 02:09 | 0.7 s | [Title] Class monitor — Stage 1 · MVP — who opened the problem, who used hints, who is stuck |
| 02:13 | 5.0 s | Four summary cards: opened, hints used, questions asked, and time spent on the figure |
| 02:21 | 13.2 s | Per student: status, time spent and the hint level reached. No rankings. |

### 04-gv-hs-bai-luyen — Chặng 2: soạn bài luyện, học sinh luyện và nhận phản hồi

- Mốc: **02:28–03:02** (dài 34.0 s) · Chặng 2 · Epic S2-E8
- Mục tiêu: Soạn bài luyện chấm tự động; học sinh luyện
- Vai: Giáo viên, Học sinh
- Tính năng thể hiện: Chọn đề có đáp số, lưu nháp; bàn phím toán, chấm ngay, gợi ý mức 1, trừ lượt
- Dữ liệu dùng: Sandbox S2-E8 (Cô Hạnh, HS An, bộ BL-1 "Góc trong hình chóp")

| Mốc (tổng) | Mốc (clip) | Voice-over (EN) = phụ đề |
|---|---|---|
| 02:29 | 0.7 s | [Title] Auto-checked practice sets — Stage 2 — teacher picks problems with verified answers; students answer on a math keypad and get instant feedback |
| 02:33 | 5.1 s | The teacher ticks problems whose answers the engine can check |
| 02:41 | 13.0 s | Five problems saved as a draft, ready to assign |
| 02:45 | 17.4 s | Students see the figure, the question, a math keypad, and three attempts per question |
| 02:52 | 23.7 s | A wrong answer is marked at once, with a level-one hint and one attempt used |
| 02:57 | 29.1 s | A correct answer is confirmed, and the student moves on |

### 05-gv-trinh-chieu — Chặng 2: trình chiếu lời giải từng bước trên lớp

- Mốc: **03:02–03:25** (dài 22.9 s) · Chặng 2 · Epic S2-E7
- Mục tiêu: Trình chiếu lời giải từng bước
- Vai: Giáo viên
- Tính năng thể hiện: Toàn màn, che lời giải, hiện bước + tô màu hình, phím mũi tên
- Dữ liệu dùng: Đề FX-14 của làn S2-E7 (Cô Trang)

| Mốc (tổng) | Mốc (clip) | Voice-over (EN) = phụ đề |
|---|---|---|
| 03:03 | 0.7 s | [Title] Classroom presentation — Stage 2 — big figure, solution covered, revealed one step at a time |
| 03:07 | 4.6 s | Full-screen view: a large figure, with the solution still covered |
| 03:12 | 10.1 s | Reveal step one, and the figure highlights exactly the elements it uses |
| 03:17 | 15.4 s | The arrow key reveals the next step |

### 06-gv-lop-hoc-ket-qua — Chặng 3: lớp học, duyệt học sinh, bảng kết quả lớp × bài

- Mốc: **03:25–03:54** (dài 29.6 s) · Chặng 3 · Epic S3-E1, S3-E3
- Mục tiêu: Quản lý lớp và bảng kết quả
- Vai: Giáo viên
- Tính năng thể hiện: Lớp có mã HK-xxxx; Đang học / Chờ duyệt / Đã rời; bảng lớp × bài, bằng chứng
- Dữ liệu dùng: Thế giới S3-E1 (Cô Lan, 11A1, 11A2); sandbox S3-E3 class051 ("Bộ bài tuần 15")

| Mốc (tổng) | Mốc (clip) | Voice-over (EN) = phụ đề |
|---|---|---|
| 03:25 | 0.7 s | [Title] Classes and results — Stage 3 — classes with join codes, approving students, a class-by-problem results board |
| 03:29 | 4.2 s | Each class has its own join code |
| 03:32 | 7.0 s | Inside a class: active students, students waiting for approval, and those who left |
| 03:38 | 13.1 s | The teacher approves or rejects join requests, and can reset a temporary password |
| 03:44 | 19.1 s | The results board: one cell per student and per problem |
| 03:49 | 24.5 s | Open a cell to see the evidence: attempts and hints used |

### 07-gv-tong-quan — Chặng 3: Tổng quan giáo viên

- Mốc: **03:54–04:09** (dài 14.8 s) · Chặng 3 · Epic S3-E4
- Mục tiêu: Tổng quan giáo viên
- Vai: Giáo viên
- Tính năng thể hiện: Ba thẻ: bài đang giao, lời giải đang khóa, em chờ duyệt
- Dữ liệu dùng: fullWorld S3-E4 (11A1, 8 HS Đang học)

| Mốc (tổng) | Mốc (clip) | Voice-over (EN) = phụ đề |
|---|---|---|
| 03:55 | 0.7 s | [Title] Teacher overview — Stage 3 — assigned work, locked solutions and pending students on one screen |
| 03:59 | 4.2 s | Three cards: assigned sets and who has not opened them, solutions still locked, and students waiting |

### 08-s5e1-giao-an — S5-E1: trợ lý soạn giáo án theo PPCT

- Mốc: **04:09–04:47** (dài 38.0 s) · Chặng 5 · Epic S5-E1
- Mục tiêu: Trợ lý soạn giáo án theo PPCT
- Vai: Giáo viên
- Tính năng thể hiện: Form C5-08 (chương 7, bài 26, tiết 1/2) tự điền số tiết + kiến thức; AI soạn nháp; nhãn "AI soạn"
- Dữ liệu dùng: Thế giới S3-E1 (Cô Lan); AI giả lập lesson_plan mặc định (F-S5-093)

| Mốc (tổng) | Mốc (clip) | Voice-over (EN) = phụ đề |
|---|---|---|
| 04:10 | 0.7 s | [Title] Lesson plan assistant — Stage 5 · AI assistant for teachers · S5-E1 — pick a lesson from the syllabus, AI drafts the plan, the teacher reviews it |
| 04:16 | 6.6 s | Pick the chapter, lesson and period from the official syllabus |
| 04:20 | 10.9 s | The number of periods and the key knowledge points are filled in automatically |
| 04:25 | 16.1 s | Choose the student level and the teaching style |
| 04:29 | 20.1 s | The AI drafts the plan. In this demo the AI is simulated. |
| 04:34 | 24.7 s | The draft has objectives, a sequence of activities, and minutes per activity |
| 04:39 | 30.2 s | It is labelled as written by AI, so the teacher reviews and edits before using it |

### 09-s5e2-ra-de-ma-tran — S5-E2: ra đề theo ma trận, nhiều mã đề

- Mốc: **04:47–05:24** (dài 36.3 s) · Chặng 5 · Epic S5-E2
- Mục tiêu: Ra đề theo ma trận, nhiều mã đề
- Vai: Giáo viên
- Tính năng thể hiện: Form ma trận KT15-HHKG11; bộ đề "Máy lắp" 4 mã; nhãn hai trục; thay câu cùng ô
- Dữ liệu dùng: Ngân hàng materials/de-thi (móc s5e2/import); Cô Lan F-S5-001; seed 2026

| Mốc (tổng) | Mốc (clip) | Voice-over (EN) = phụ đề |
|---|---|---|
| 04:48 | 0.7 s | [Title] Exams from a test matrix — Stage 5 · AI assistant for teachers · S5-E2 — choose an exam type; the engine fills it from the verified question bank, in several versions |
| 04:53 | 5.4 s | Choosing an exam type pre-fills the form from the standard matrix |
| 04:57 | 9.9 s | A fifteen-minute test: six multiple-choice and two short-answer questions, ten points, four versions |
| 05:06 | 18.7 s | Every question carries two labels: machine-verified, and approved by a person |
| 05:12 | 25.2 s | Version one oh one |
| 05:16 | 29.1 s | Swap one question, and the new one keeps the same topic, type, difficulty and points |

### 10-s5e2-de-nhap-tai-lieu — S5-E2: trợ lý soạn đề nháp từ tài liệu

- Mốc: **05:24–05:44** (dài 20.0 s) · Chặng 5 · Epic S5-E2
- Mục tiêu: Đề nháp từ tài liệu
- Vai: Giáo viên
- Tính năng thể hiện: AI soạn đề từ TL-01 v2; engine tính đáp số; đề chưa kiểm chứng xếp cuối, không chọn sẵn
- Dữ liệu dùng: Thế giới S5-E3 (Cô Lan, 11A1); TL-01 "Ghi chú Khoảng cách 11A1" v2; AI giả lập exam_draft

| Mốc (tổng) | Mốc (clip) | Voice-over (EN) = phụ đề |
|---|---|---|
| 05:24 | 0.7 s | [Title] Draft questions from documents — Stage 5 · AI assistant for teachers · S5-E2 — AI drafts questions from an active document; the geometry engine checks every answer |
| 05:29 | 5.8 s | The source is an active class document, version two |
| 05:34 | 10.5 s | Questions the engine can build come with a computed answer; unverified ones go last and are not pre-selected |

### 11-s5e3-kho-tri-thuc-hoi-dap — S5-E3: kho tri thức và hỏi đáp có nguồn

- Mốc: **05:44–06:16** (dài 32.6 s) · Chặng 5 · Epic S5-E3
- Mục tiêu: Kho tri thức và hỏi đáp có nguồn
- Vai: Giáo viên, Học sinh
- Tính năng thể hiện: Kho tài liệu có phiên bản; học sinh hỏi, trả lời kèm nguồn, mở đúng mục
- Dữ liệu dùng: qa.setup (Cô Lan, Bình An, BC-01, TL-01); AI giả lập F-S5-094

| Mốc (tổng) | Mốc (clip) | Voice-over (EN) = phụ đề |
|---|---|---|
| 05:44 | 0.7 s | [Title] Knowledge base and sourced Q&A — Stage 5 · AI assistant for teachers · S5-E3 — versioned teacher documents; students ask, the AI answers with a cited source |
| 05:50 | 6.4 s | Teachers keep versioned documents: active, waiting for review, or archived |
| 05:57 | 13.3 s | Students can ask within the scope of the assignment and the help level the teacher allows |
| 06:05 | 21.3 s | The answer explains the concept without solving the problem, and cites its source |
| 06:11 | 27.7 s | Tapping the source opens exactly the section that was quoted |

### 12-s5e4-rubric-nop-bai — S5-E4: rubric và học sinh chụp nộp bài

- Mốc: **06:16–06:54** (dài 37.8 s) · Chặng 5 · Epic S5-E4 · nhãn bản thử
- Mục tiêu: Rubric và nộp bài giấy
- Vai: Giáo viên, Học sinh
- Tính năng thể hiện: Rubric C5-02 tổng sống, chặn lưu khi lệch; học sinh chụp 2 trang (kiểm độ rõ), nộp (MSG-S5-33)
- Dữ liệu dùng: Thế giới S5-E4; RB-01; BC-01; ảnh mẫu bn01-p1/p2.jpg

| Mốc (tổng) | Mốc (clip) | Voice-over (EN) = phụ đề |
|---|---|---|
| 06:17 | 0.7 s | [Title] Rubrics and paper submissions — Stage 5 · AI assistant for teachers · S5-E4 — teacher writes a rubric; students photograph each page of their work and submit |
| 06:23 | 6.8 s | A five-criterion rubric with a live total: ten out of ten |
| 06:28 | 11.4 s | If the points do not add up to ten, a warning appears and saving is blocked |
| 06:34 | 17.5 s | The student opens a graded assignment |
| 06:38 | 22.3 s | Page one is photographed, and checked for sharpness before it is accepted |
| 06:44 | 28.2 s | Page two. Pages can be reordered if needed. |
| 06:49 | 32.8 s | Submitted, with a confirmation that shows the server time |

### 13-s5e4-hang-cho-cham-tay — S5-E4: hàng chờ chấm và chấm tay theo rubric

- Mốc: **06:54–07:26** (dài 31.8 s) · Chặng 5 · Epic S5-E4 · nhãn bản thử
- Mục tiêu: Hàng chờ và chấm tay theo rubric
- Vai: Giáo viên
- Tính năng thể hiện: Hàng chờ C5-04 (cờ trước, không xếp hạng); màn chấm C5-05, tổng tự tính 7/10, Duyệt → bài kế
- Dữ liệu dùng: gradingWorld + sc1 (BN-01…05); BN-02 của Chi Mai

| Mốc (tổng) | Mốc (clip) | Voice-over (EN) = phụ đề |
|---|---|---|
| 06:55 | 0.7 s | [Title] Grading queue and manual grading — Stage 5 · AI assistant for teachers · S5-E4 — flagged work first; photo on the left, rubric on the right, shortcut A to approve |
| 06:59 | 5.0 s | Flagged submissions, such as unreadable or late work, come first. There is no ranking column. |
| 07:07 | 13.5 s | The student’s pages on the left, the rubric on the right |
| 07:14 | 20.3 s | The total is computed automatically: seven out of ten |
| 07:20 | 26.4 s | After approval, the next submission in the queue opens automatically |

### 14-s5e5-thong-bao-cong-tac — S5-E5: thông báo do trợ lý soạn, công tắc AI và hạn mức

- Mốc: **07:26–08:00** (dài 33.8 s) · Chặng 5 · Epic S5-E5
- Mục tiêu: Thông báo do trợ lý soạn, công tắc AI, hạn mức
- Vai: Giáo viên
- Tính năng thể hiện: Nháp "AI soạn" nhắc hạn BC-01, sửa câu cuối, gửi 8 em; khối "AI của lớp": công tắc + lượt 30 ngày
- Dữ liệu dùng: Thế giới S5-E5 (11A1, 8 HS Đang học + Văn Tú Đã rời); AI giả lập class_notice

| Mốc (tổng) | Mốc (clip) | Voice-over (EN) = phụ đề |
|---|---|---|
| 07:26 | 0.7 s | [Title] Class notices, AI switch and usage — Stage 5 · AI assistant for teachers · S5-E5 — AI drafts a notice, the teacher edits and sends; AI on or off per class; usage over 30 days |
| 07:33 | 6.8 s | Pick a class and a notice type, here a due-date reminder |
| 07:37 | 11.6 s | An AI draft with the correct local due time. The teacher edits the last sentence. |
| 07:45 | 19.3 s | Sent to the eight active students. A student who left the class gets nothing. |
| 07:51 | 25.3 s | Per class, AI grading and the assistant can be switched on or off, with thirty-day usage |

### 15-s5e5-loi-sai-lop — S5-E5: tổng hợp lỗi sai của lớp và bộ luyện nhắc lại

- Mốc: **08:00–08:28** (dài 28.1 s) · Chặng 5 · Epic S5-E5 · nhãn bản thử
- Mục tiêu: Tổng hợp lỗi sai của lớp
- Vai: Giáo viên
- Tính năng thể hiện: "Cả lớp hay sai ở đâu" kèm căn cứ; gợi ý bộ luyện nhắc lại; bấm căn cứ mở bài làm
- Dữ liệu dùng: insights.graded() — 6 bài đã chấm qua luồng thật S5-E4/E6

| Mốc (tổng) | Mốc (clip) | Voice-over (EN) = phụ đề |
|---|---|---|
| 08:00 | 0.7 s | [Title] Where the class goes wrong — Stage 5 · AI assistant for teachers · S5-E5 — common errors from graded work, with evidence; a follow-up practice set |
| 08:08 | 8.1 s | Each insight lists its evidence, by submission. No rankings and no percentages. |
| 08:16 | 16.3 s | It suggests up to three practice questions on the same topic, plus related document sections |
| 08:23 | 23.5 s | Clicking the evidence opens that student’s actual work |

### 16-s5e6-ai-de-xuat-diem — S5-E6: AI đề xuất điểm, giáo viên nhận từng tiêu chí

- Mốc: **08:28–08:59** (dài 31.2 s) · Chặng 5 · Epic S5-E6 · nhãn bản thử
- Mục tiêu: AI đề xuất điểm, giáo viên nhận từng tiêu chí
- Vai: Giáo viên
- Tính năng thể hiện: Điểm AI từng tiêu chí, khoanh vùng ảnh, nhận 5 tiêu chí, Duyệt 10/10
- Dữ liệu dùng: s5e6b.setup("ai_proposal"); AI giả lập F-S5-090 (BN-01 Bình An)

| Mốc (tổng) | Mốc (clip) | Voice-over (EN) = phụ đề |
|---|---|---|
| 08:28 | 0.7 s | [Title] AI-proposed grades — Stage 5 · AI assistant for teachers · S5-E6 — AI proposes a grade per criterion and marks the region it used; the teacher accepts, edits or drops each |
| 08:34 | 6.3 s | Each criterion shows the AI’s proposed points. Approve stays disabled until every criterion is handled. |
| 08:42 | 14.5 s | Click a criterion to see the region of the page the AI relied on |
| 08:49 | 21.3 s | Accept each criterion. The total is ten out of ten. |
| 08:54 | 26.2 s | Approved. The final grade is always the teacher’s decision. |

### 17-s5e6-song-song-bao-cao — S5-E6: chạy song song và báo cáo khớp AI–GV

- Mốc: **08:59–09:32** (dài 33.5 s) · Chặng 5 · Epic S5-E6 · nhãn bản thử
- Mục tiêu: Chạy song song và báo cáo khớp AI–GV
- Vai: Giáo viên
- Tính năng thể hiện: Song song: ẩn điểm AI, chấm tay, so sánh sau duyệt; báo cáo KPI khớp (API /ai-grading/report hiển thị dạng bảng — chưa có màn riêng)
- Dữ liệu dùng: s5e6b.setup("parallel"), F-S5-091; móc s5e6/kpi-seed 20 bài (15 khớp + 5 lệch 1 điểm)

| Mốc (tổng) | Mốc (clip) | Voice-over (EN) = phụ đề |
|---|---|---|
| 09:00 | 0.7 s | [Title] Parallel mode and agreement report — Stage 5 · AI assistant for teachers · S5-E6 — AI grades silently while the teacher grades; compare afterwards; a KPI gate decides when AI proposals open |
| 09:06 | 7.1 s | In parallel mode the AI score stays hidden while the teacher grades, to avoid anchoring |
| 09:14 | 15.6 s | After approval, AI and teacher points are compared criterion by criterion |
| 09:20 | 21.0 s | [Title] AI–teacher agreement · class 11A1 — 20 papers · 100 criterion pairs · AI-proposal gate: OPEN |
| 09:21 | 21.6 s | The agreement report counts how often AI and teacher points fall within a quarter point, with a ninety-five percent confidence bound. The gate for AI proposals opens only when every K P I is met. |

### 99-outro — Kết thúc

- Mốc: **09:32–09:46** (dài 13.4 s) · Chặng — · Epic —
- Mục tiêu: Tóm tắt + BambooAI
- Vai: —
- Tính năng thể hiện: Màn kết
- Dữ liệu dùng: —

| Mốc (tổng) | Mốc (clip) | Voice-over (EN) = phụ đề |
|---|---|---|
| 09:33 | 0.4 s | Verified geometry, hints before answers, and an AI assistant that leaves the final decision to the teacher. Geometry Math Support, by BambooAI. Thank you for watching. |

## Không quay được

- Không có màn nào lỗi khi quay. Ghi chú: báo cáo khớp AI–GV (US-S5E6-06) chưa có màn giao diện riêng — clip 17 hiển thị dữ liệu thật từ API `/ai-grading/report` dạng bảng.

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.0 | 2026-11-28 | Bản đầu: 19 clip (mở đầu, chặng 1–3, chặng 5 đủ 6 epic, kết), giọng đọc tiếng Anh, mở đầu "by BambooAI" | runner-T-261128-73 |
