# SCRIPT-VI — Video giới thiệu "Hình Học Không Gian Cùng AI — presented by BambooAI" (T-261128-75)

Phiên bản: v2.0 · 2026-11-28 · T-261128-76 · sinh tự động bởi `app/e2e/demo-vi/make-script-vi.mjs` (không sửa tay: sửa `voice/lines.tsv` hoặc `app/e2e/demo-vi/scenes.json` rồi chạy lại).

- Tổng thời lượng bản nháp: 11:33 (693.9 s) · 35 cảnh · tiếng Việt, phụ đề VI burn-in (≤ 2 dòng × 42 ký tự).
- Style: BambooAI như video cũ (`Video/GeometryMath.mp4`, `Video/slides/*.png`): nền xanh lá đậm gradient, nhãn "PRESENTED BY BAMBOOAI" vàng chanh, thẻ kính có icon, slide chương "0N <TÊN>". Thông điệp: "AI is powerful. But thinking is still human." · "AI executes, humans decide" · contact@bamboox.ai.
- Dữ liệu thật: bài HKG-01..06 + gợi ý + lời giải từng bước của `materials/Ngan-hang-bai-tap-Hinh-khong-gian.xlsx` (đã có trong seed); định hướng và tiến trình của `materials/Giao-an-Hinh-khong-gian-11A1.docx`; giáo án chọn PPCT bài 24 "Phép chiếu vuông góc. Góc giữa đường thẳng và mặt phẳng" (đúng chủ đề giáo án 11A1).
- **Ảnh bài làm viết tay là ảnh MINH HOẠ** (`assets/bai-lam-bn01-p1/p2[-photo].png|jpg`: chữ viết tay font Noteworthy trên giấy kẻ, hình chóp vẽ tay, học sinh giả "Bình An" giải đúng đề được giao "Kiểm tra 15 phút – Khoảng cách": d(A,(SBC)) = a√6/3, khớp rubric và bản đọc chữ của AI giả lập). Owner thay ảnh chụp thật: chép đè `assets/bai-lam-bn01-p1-photo.jpg`, `-p2-photo.jpg` (và `-p1-photo.png` cho slide 19) CÙNG TÊN, chạy `GEO_VI_KEEP_PHOTOS=1 node demo-vi/make-assets-vi.mjs` (giữ ảnh, chỉ dựng lại slide), quay lại cảnh 18, 20 rồi dựng video.
- Mạch kể: tình huống 21:30 của cô Lan và Bình An (00) → 3 khoảnh khắc "wow" (06 dựng hình từ đề, 14 hỏi đáp có nguồn, 23–24 đọc bài viết tay → đề xuất điểm) → cao trào chương 04 → "Một tuần sau" (29) → kêu gọi (33). Không trần thời lượng (owner 28/11).
- AI giả lập (MockAiProvider) trong mọi cảnh. Câu trả lời hỏi đáp (cảnh 14) lấy từ fixture demo riêng `app/e2e/demo-vi/fixtures/qa-hkg01.json` (không đụng fixture e2e).
- Giọng đọc TẠM: macOS "Linh" (vi_VN). Thay giọng: gen bằng Google TTS theo `voice/lines.tsv`, thả `voice/<mã cảnh>.mp3|wav` CÙNG TÊN (tệp mới nhất thắng), chạy lại `node demo-vi/make-video-vi.mjs <node_modules ffmpeg>`: thời lượng cảnh và phụ đề tự khớp.

## Lệnh dựng lại

```bash
cd app && bash e2e/demo/stack-up.sh                       # máy chủ dev + web e2e trên cổng của worktree (AI giả lập)
bash e2e/demo-vi/rebuild.sh                               # MỘT lệnh: slide, giọng còn thiếu, video giới thiệu + 17 video hướng dẫn (GEO_VI_RECORD=1 để quay lại)
cd e2e && node demo-vi/make-assets-vi.mjs <ffmpeg>        # slide PNG + ảnh viết tay minh hoạ
node demo-vi/gen-voice.mjs <ffmpeg>                       # giọng tạm (bỏ qua cảnh đã có tệp giọng)
GEO_PORT=… GEO_WEB_PORT=… npx playwright test -c demo-vi/demo.config.ts   # quay clip câm
node demo-vi/make-video-vi.mjs <node_modules có ffmpeg-static + @ffprobe-installer>
```

## Bảng cảnh

| Mốc | Cảnh | Chương | Loại | Hình |
|---|---|---|---|---|
| 00:00 | 00-hook | Mở đầu | slide | slides/00-hook.png |
| 00:32 | 01-mo-dau | Mở đầu | slide | slides/00-mo-dau.png |
| 00:49 | 02-van-de | Mở đầu | slide | slides/01-van-de.png |
| 01:10 | 03-gioi-thieu | Mở đầu | slide | slides/02-he-thong.png |
| 01:30 | 04-chuong-hinh-hoc | 01 Hỗ trợ hình học | slide | slides/03-chuong-1.png |
| 01:40 | 05-kho-de-that | 01 Hỗ trợ hình học | clip | clip (demo-vi) |
| 02:01 | 06-wow-dung-hinh | 01 Hỗ trợ hình học | clip | clip (demo-vi) |
| 02:32 | 07-chi-tiet-kiem-chung | 01 Hỗ trợ hình học | clip | clip (demo-vi) |
| 02:54 | 08-xoay-ve-do | 01 Hỗ trợ hình học | clip | clip (demo-vi) |
| 03:18 | 09-trinh-chieu | 01 Hỗ trợ hình học | clip | clip (demo-vi) |
| 03:36 | 10-chuong-ai | 02 AI đồng hành cùng học sinh | slide | slides/08-chuong-2.png |
| 03:46 | 11-vao-lop-goi-y | 02 AI đồng hành cùng học sinh | clip | clip (demo-vi) |
| 04:09 | 12-bai-luyen | 02 AI đồng hành cùng học sinh | clip | clip (demo-vi) |
| 04:28 | 13-kho-tri-thuc | 02 AI đồng hành cùng học sinh | clip | clip (demo-vi) |
| 04:45 | 14-wow-hoi-dap | 02 AI đồng hành cùng học sinh | clip | clip (demo-vi) |
| 05:09 | 15-chuong-soan | 03 Soạn giáo án và đề | slide | slides/12-chuong-3.png |
| 05:18 | 16-soan-giao-an | 03 Soạn giáo án và đề | clip | clip (demo-vi) |
| 05:52 | 17-ra-de-ma-tran | 03 Soạn giáo án và đề | clip | clip (demo-vi) |
| 06:17 | 18-soan-de-tai-lieu | 03 Soạn giáo án và đề | clip | clip (demo-vi) |
| 06:34 | 19-thong-bao-cong-tac | 03 Soạn giáo án và đề | clip | clip (demo-vi) |
| 06:51 | 20-chuong-cham | 04 Chấm bài viết tay | slide | slides/16-chuong-4.png |
| 06:58 | 21-rubric | 04 Chấm bài viết tay | clip | clip (demo-vi) |
| 07:16 | 22-nop-bai-viet-tay | 04 Chấm bài viết tay | clip | clip (demo-vi) |
| 07:34 | 23-wow-nhan-dang | 04 Chấm bài viết tay | slide | slides/19-nhan-dang.png |
| 08:02 | 24-ai-de-xuat-diem | 04 Chấm bài viết tay | clip | clip (demo-vi) |
| 08:25 | 25-song-song-tin-cay | 04 Chấm bài viết tay | clip | clip (demo-vi) |
| 08:47 | 26-loi-sai-lop | 04 Chấm bài viết tay | clip | clip (demo-vi) |
| 09:04 | 27-ket-qua-lop | 04 Chấm bài viết tay | clip | clip (demo-vi) |
| 09:21 | 28-theo-doi-lop | 04 Chấm bài viết tay | clip | clip (demo-vi) |
| 09:35 | 29-tuan-sau | Kết | slide | slides/29-tuan-sau.png |
| 10:02 | 30-loi-ich-hs | Kết | slide | slides/23-loi-ich-hs.png |
| 10:17 | 31-loi-ich-gv | Kết | slide | slides/24-loi-ich-gv.png |
| 10:37 | 32-tong-ket | Kết | slide | slides/25-tong-ket.png |
| 11:02 | 33-keu-goi | Kết | slide | slides/33-keu-goi.png |
| 11:26 | 34-ket | Kết | slide | slides/26-ket.png |

## Kịch bản từng cảnh

### Mở đầu

#### 00-hook · 00:00–00:32 (32.9 s, giọng 30.3 s)

- **Hình / thao tác quay:** Slide tình huống 21:30: Cô Lan (GV) và Bình An (HS) — hai thẻ kính, băng câu hỏi
- **Dữ liệu dùng:** Tình huống minh hoạ, tên giả
- **Tệp giọng:** `voice/00-hook.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Chín giờ rưỡi tối thứ Năm. Cô Lan, giáo viên Toán lớp 11 A1, còn một chồng bài kiểm tra viết tay chưa chấm, đề kiểm tra tuần sau chưa ra, và giáo án tiết sau chưa xong. Cùng lúc đó, học sinh Bình An đang kẹt ở một bài hình chóp. Em hỏi một AI chung chung, nhận ngay đáp số, chép lại, và sáng mai lên lớp vẫn không hiểu. Nếu có một trợ lý hiểu hình học, hiểu lớp học, và luôn để giáo viên quyết định thì sao?
- **Phụ đề (VI):**
  - `00:00` Chín giờ rưỡi tối thứ Năm.
  - `00:02` Cô Lan, giáo viên Toán lớp 11A1, còn một / chồng bài kiểm tra viết tay chưa chấm,
  - `00:08` đề kiểm tra tuần sau chưa ra, / và giáo án tiết sau chưa xong.
  - `00:13` Cùng lúc đó, học sinh Bình An / đang kẹt ở một bài hình chóp.
  - `00:17` Em hỏi một AI chung chung, / nhận ngay đáp số, chép lại,
  - `00:21` và sáng mai lên lớp vẫn không hiểu.
  - `00:24` Nếu có một trợ lý hiểu / hình học, hiểu lớp học,
  - `00:27` và luôn để giáo viên quyết định thì sao?

#### 01-mo-dau · 00:32–00:49 (16.2 s, giọng 13.6 s)

- **Hình / thao tác quay:** Slide tiêu đề + tagline
- **Dữ liệu dùng:** —
- **Tệp giọng:** `voice/01-mo-dau.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Xin giới thiệu Hình Học Không Gian Cùng AI, do BambooAI phát triển cho giáo viên và học sinh trung học. AI rất mạnh, nhưng tư duy vẫn là của con người. Đó là nguyên tắc của mọi tính năng bạn sắp thấy.
- **Phụ đề (VI):**
  - `00:33` Xin giới thiệu Hình / Học Không Gian Cùng AI,
  - `00:36` do BambooAI phát triển cho giáo / viên và học sinh trung học.
  - `00:40` AI rất mạnh, nhưng tư / duy vẫn là của con người.
  - `00:43` Đó là nguyên tắc của mọi / tính năng bạn sắp thấy.

#### 02-van-de · 00:49–01:10 (21.4 s, giọng 18.8 s)

- **Hình / thao tác quay:** Slide 3 vấn đề
- **Dữ liệu dùng:** —
- **Tệp giọng:** `voice/02-van-de.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Vì sao cần một công cụ riêng? Vì hình không gian khó hình dung trên giấy. Vì AI thông thường có thể tính sai mà vẫn rất tự tin, và hay đưa đáp số quá sớm. Và vì giáo viên đang mất rất nhiều giờ mỗi tuần cho những việc lặp lại: soạn đề, soạn giáo án, chấm bài.
- **Phụ đề (VI):**
  - `00:49` Vì sao cần một công cụ riêng?
  - `00:51` Vì hình không gian khó / hình dung trên giấy.
  - `00:54` Vì AI thông thường có thể tính sai mà vẫn / rất tự tin, và hay đưa đáp số quá sớm.
  - `01:00` Và vì giáo viên đang mất rất nhiều giờ / mỗi tuần cho những việc lặp lại: soạn đề,
  - `01:06` soạn giáo án, chấm bài.

#### 03-gioi-thieu · 01:10–01:30 (20.0 s, giọng 17.4 s)

- **Hình / thao tác quay:** Slide ba vai
- **Dữ liệu dùng:** —
- **Tệp giọng:** `voice/03-gioi-thieu.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Hệ thống có ba vai. Học sinh nhìn thấy hình, tự tay thử, và hiểu vì sao. Giáo viên được đỡ công soạn và chấm, nhưng vẫn quyết định cuối cùng. Còn AI đề xuất, và một engine hình học tính lại mọi đáp số. AI thực hiện, con người quyết định.
- **Phụ đề (VI):**
  - `01:11` Hệ thống có ba vai.
  - `01:12` Học sinh nhìn thấy hình, / tự tay thử, và hiểu vì sao.
  - `01:16` Giáo viên được đỡ công soạn và chấm, / nhưng vẫn quyết định cuối cùng.
  - `01:21` Còn AI đề xuất, và một engine / hình học tính lại mọi đáp số.
  - `01:25` AI thực hiện, con người quyết định.

### 01 Hỗ trợ hình học

#### 04-chuong-hinh-hoc · 01:30–01:40 (9.8 s, giọng 7.2 s)

- **Hình / thao tác quay:** Slide chương 01
- **Dữ liệu dùng:** —
- **Tệp giọng:** `voice/04-chuong-hinh-hoc.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Chương một: hỗ trợ hình học. Hãy bắt đầu với điều mà AI thông thường hay làm sai nhất: hình và đáp số.
- **Phụ đề (VI):**
  - `01:31` Chương một: hỗ trợ hình học.
  - `01:33` Hãy bắt đầu với điều mà AI thông thường / hay làm sai nhất: hình và đáp số.

#### 05-kho-de-that · 01:40–02:01 (21.4 s, giọng 19.2 s)

- **Hình / thao tác quay:** /gv/kho-de: lọc THPT, Góc; danh sách HKG
- **Dữ liệu dùng:** HKG-01..06 (xlsx, đã trong seed)
- **Tệp giọng:** `voice/05-kho-de-that.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Đây là kho đề, chứa chính các bài trong ngân hàng bài tập của tổ Toán: bài H K G không một về góc trong hình chóp, bài H K G không ba về thể tích lăng trụ, và nhiều bài khác. Giáo viên lọc theo cấp học, loại hình và chủ đề. Bài nào cũng có nhãn đã kiểm chứng.
- **Phụ đề (VI):**
  - `01:40` Đây là kho đề, chứa chính các bài / trong ngân hàng bài tập của tổ Toán:
  - `01:46` bài HKG-01 về góc trong hình chóp, / bài HKG-03 về thể tích lăng trụ,
  - `01:51` và nhiều bài khác.
  - `01:53` Giáo viên lọc theo cấp / học, loại hình và chủ đề.
  - `01:57` Bài nào cũng có nhãn đã kiểm chứng.

#### 06-wow-dung-hinh · 02:01–02:32 (30.6 s, giọng 28.4 s)

- **Hình / thao tác quay:** /gv/them-de: gõ đề, Dựng hình, 4 bước, kết quả 'Đã kiểm chứng' 45°
- **Dữ liệu dùng:** Đề biến thể HKG-01 (SA = a√2, góc SC) — khớp quy tắc mock FX-14
- **Tệp giọng:** `voice/06-wow-dung-hinh.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Còn một đề hoàn toàn mới thì sao? Cô Lan chỉ cần gõ đề, như viết lên bảng: cho hình chóp S A B C D, đáy là hình vuông cạnh a, S A vuông góc với đáy, S A bằng a căn hai, tính góc giữa S C và mặt đáy. Bấm Dựng hình. Hệ thống đọc đề, dựng hình 3D, giải từng bước, và cuối cùng tự kiểm chứng bằng tính toán. Bốn mươi lăm độ. Đã kiểm chứng. Chỉ trong vài giây.
- **Phụ đề (VI):**
  - `02:02` Còn một đề hoàn toàn mới thì sao?
  - `02:05` Cô Lan chỉ cần gõ đề, như viết / lên bảng: cho hình chóp S.ABCD,
  - `02:10` đáy là hình vuông cạnh a, SA ⊥ đáy, / SA = a√2, tính góc giữa SC và mặt đáy.
  - `02:17` Bấm Dựng hình.
  - `02:19` Hệ thống đọc đề, dựng / hình 3D, giải từng bước,
  - `02:23` và cuối cùng tự kiểm / chứng bằng tính toán. 45°.
  - `02:27` Đã kiểm chứng. Chỉ trong vài giây.

#### 07-chi-tiet-kiem-chung · 02:32–02:54 (22.0 s, giọng 19.8 s)

- **Hình / thao tác quay:** /gv/de/HKG-01: xoay hình, khoanh đáp số, cuộn lời giải từng bước
- **Dữ liệu dùng:** HKG-01 + lời giải 3 bước (sheet 'Loi giai')
- **Tệp giọng:** `voice/07-chi-tiet-kiem-chung.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Mở một bài trong kho, ví dụ bài góc giữa S D và mặt đáy. Mỗi bài có đề, hình 3D, đáp số, và lời giải từng bước theo đúng định hướng của tổ: ưu tiên hình chiếu và tam giác vuông. Đáp số này không phải do AI tự nói, mà khớp với kết quả engine tự tính lại.
- **Phụ đề (VI):**
  - `02:32` Mở một bài trong kho, ví dụ / bài góc giữa SD và mặt đáy.
  - `02:37` Mỗi bài có đề, hình 3D, đáp số,
  - `02:39` và lời giải từng bước theo / đúng định hướng của tổ:
  - `02:43` ưu tiên hình chiếu và tam giác vuông.
  - `02:46` Đáp số này không phải do AI tự nói, mà / khớp với kết quả engine tự tính lại.

#### 08-xoay-ve-do · 02:54–03:18 (24.0 s, giọng 21.8 s)

- **Hình / thao tác quay:** HS /hs/lam-bai BG-1: xoay, vẽ BD, đo SD, đo góc SAD
- **Dữ liệu dùng:** BG-1 seed = hình chóp HKG-01
- **Tệp giọng:** `voice/08-xoay-ve-do.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Bây giờ là phía Bình An. Hình chóp không còn nằm im trên giấy: em kéo để xoay, nhìn từ mọi phía, và tự thấy S A vuông góc với đáy. Em vẽ thêm đoạn B D, đo S D được a căn hai, đo góc S A D được chín mươi độ. Riêng đại lượng là đáp số thì được khoá, để em vẫn phải tự suy luận.
- **Phụ đề (VI):**
  - `02:54` Bây giờ là phía Bình An.
  - `02:56` Hình chóp không còn nằm im trên giấy: / em kéo để xoay, nhìn từ mọi phía,
  - `03:03` và tự thấy SA vuông góc với đáy.
  - `03:05` Em vẽ thêm đoạn BD, đo SD / được a√2, đo góc SAD được 90°.
  - `03:10` Riêng đại lượng là đáp số thì được / khoá, để em vẫn phải tự suy luận.

#### 09-trinh-chieu · 03:18–03:36 (18.0 s, giọng 14.1 s)

- **Hình / thao tác quay:** /gv/trinh-chieu: toàn màn hình 1920x1080, mở từng bước, phím →
- **Dữ liệu dùng:** Bài trình chiếu seed S2-E7
- **Tệp giọng:** `voice/09-trinh-chieu.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Trên lớp, cô Lan trình chiếu bài toán toàn màn hình. Lời giải được che, và cô mở từng bước một. Mỗi bước, hình tô sáng đúng các yếu tố đang dùng. Cả lớp cùng dự đoán trước khi bước tiếp theo hiện ra.
- **Phụ đề (VI):**
  - `03:18` Trên lớp, cô Lan trình chiếu / bài toán toàn màn hình.
  - `03:22` Lời giải được che, và cô mở từng bước một.
  - `03:25` Mỗi bước, hình tô sáng / đúng các yếu tố đang dùng.
  - `03:29` Cả lớp cùng dự đoán trước / khi bước tiếp theo hiện ra.

### 02 AI đồng hành cùng học sinh

#### 10-chuong-ai · 03:36–03:46 (10.2 s, giọng 7.7 s)

- **Hình / thao tác quay:** Slide chương 02
- **Dữ liệu dùng:** —
- **Tệp giọng:** `voice/10-chuong-ai.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Chương hai: AI đồng hành cùng học sinh. Gợi ý trước, lời giải sau, và mọi câu trả lời đều có nguồn.
- **Phụ đề (VI):**
  - `03:36` Chương hai: AI đồng hành cùng học sinh.
  - `03:39` Gợi ý trước, lời giải sau, và / mọi câu trả lời đều có nguồn.

#### 11-vao-lop-goi-y · 03:46–04:09 (23.4 s, giọng 19.3 s)

- **Hình / thao tác quay:** HS vào lớp (tên + mã), mở HKG-01, gợi ý 1, gợi ý 2, thẻ lời giải khoá
- **Dữ liệu dùng:** Gợi ý 1/2 của HKG-01
- **Tệp giọng:** `voice/11-vao-lop-goi-y.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Học sinh vào lớp chỉ bằng tên và mã lớp, không cần tài khoản. Khi vướng, Bình An mở gợi ý một: góc giữa đường và mặt là góc giữa đường đó và hình chiếu của nó. Gợi ý hai chỉ bước kế tiếp. Còn lời giải thì chờ đến giờ cô mở. Không ai bị lộ đáp án quá sớm.
- **Phụ đề (VI):**
  - `03:47` Học sinh vào lớp chỉ bằng tên / và mã lớp, không cần tài khoản.
  - `03:51` Khi vướng, Bình An mở gợi ý một:
  - `03:54` góc giữa đường và mặt là góc giữa / đường đó và hình chiếu của nó.
  - `03:59` Gợi ý hai chỉ bước kế tiếp.
  - `04:01` Còn lời giải thì chờ đến giờ cô mở.
  - `04:04` Không ai bị lộ đáp án quá sớm.

#### 12-bai-luyen · 04:09–04:28 (18.6 s, giọng 16.4 s)

- **Hình / thao tác quay:** GV tick 5 bài, lưu bộ luyện; HS nhập sai rồi đúng trên bàn phím toán
- **Dữ liệu dùng:** Bộ luyện seed S2-E8
- **Tệp giọng:** `voice/12-bai-luyen.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Với bài luyện, cô chọn những bài mà máy kiểm được đáp số. Học sinh nhập đáp số trên bàn phím toán, có ba lượt thử. Sai thì được báo ngay kèm một gợi ý; đúng thì được xác nhận và sang câu tiếp. Cô không phải chấm tay từng câu.
- **Phụ đề (VI):**
  - `04:10` Với bài luyện, cô chọn những / bài mà máy kiểm được đáp số.
  - `04:14` Học sinh nhập đáp số trên bàn / phím toán, có ba lượt thử.
  - `04:18` Sai thì được báo ngay kèm một gợi ý; / đúng thì được xác nhận và sang câu tiếp.
  - `04:24` Cô không phải chấm tay từng câu.

#### 13-kho-tri-thuc · 04:28–04:45 (16.6 s, giọng 14.4 s)

- **Hình / thao tác quay:** /gv/kho-tri-thuc: tài liệu, phiên bản, trạng thái
- **Dữ liệu dùng:** Tài liệu lớp 11A1 (seed S5-E3)
- **Tệp giọng:** `voice/13-kho-tri-thuc.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Muốn AI trả lời đúng với lớp mình, cô Lan đưa tài liệu của lớp vào kho tri thức. Mỗi tài liệu có phiên bản và trạng thái: đang dùng, chờ duyệt, hay đã lưu trữ. AI chỉ được dùng những gì cô đã duyệt.
- **Phụ đề (VI):**
  - `04:29` Muốn AI trả lời đúng với lớp mình, cô Lan / đưa tài liệu của lớp vào kho tri thức.
  - `04:34` Mỗi tài liệu có phiên bản và trạng thái: / đang dùng, chờ duyệt, hay đã lưu trữ.
  - `04:40` AI chỉ được dùng những gì cô đã duyệt.

#### 14-wow-hoi-dap · 04:45–05:09 (24.4 s, giọng 22.2 s)

- **Hình / thao tác quay:** HS /hs/hoi-dap: hỏi về khoảng cách A→(SBC), câu trả lời không nêu đáp số, bấm nguồn mở đoạn 'Khoảng cách từ điểm đến mặt phẳng'
- **Dữ liệu dùng:** fixtures/qa-hkg01.json (câu hỏi + trả lời khớp ghi chú Khoảng cách 11A1 v2)
- **Tệp giọng:** `voice/14-wow-hoi-dap.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Và đây là khác biệt thứ hai. Trước giờ kiểm tra, Bình An hỏi: muốn tính khoảng cách từ A đến mặt phẳng S B C thì bắt đầu từ đâu? AI không đưa đáp số. Nó giải thích cách nghĩ, gợi ý em tìm mặt phẳng vuông góc, và ghi rõ nguồn. Bấm vào nguồn, em đọc đúng đoạn trong ghi chú khoảng cách của lớp 11 A1.
- **Phụ đề (VI):**
  - `04:45` Và đây là khác biệt thứ hai.
  - `04:47` Trước giờ kiểm tra, Bình An hỏi:
  - `04:50` muốn tính khoảng cách từ A đến mặt / phẳng (SBC) thì bắt đầu từ đâu?
  - `04:55` AI không đưa đáp số.
  - `04:56` Nó giải thích cách nghĩ, gợi ý em tìm / mặt phẳng vuông góc, và ghi rõ nguồn.
  - `05:02` Bấm vào nguồn, em đọc đúng đoạn trong / ghi chú Khoảng cách của lớp 11A1.

### 03 Soạn giáo án và đề

#### 15-chuong-soan · 05:09–05:18 (8.9 s, giọng 6.3 s)

- **Hình / thao tác quay:** Slide chương 03
- **Dữ liệu dùng:** —
- **Tệp giọng:** `voice/15-chuong-soan.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Chương ba: soạn giáo án và soạn đề. Đây là nơi cô Lan lấy lại những buổi tối của mình.
- **Phụ đề (VI):**
  - `05:10` Chương ba: soạn giáo án và soạn đề.
  - `05:12` Đây là nơi cô Lan lấy lại / những buổi tối của mình.

#### 16-soan-giao-an · 05:18–05:52 (34.1 s, giọng 31.9 s)

- **Hình / thao tác quay:** /gv/giao-an/moi: chương 7, bài 24, tiết, đối tượng; bản nháp; cuộn mục tiêu, tiến trình, phiếu học tập
- **Dữ liệu dùng:** fixtures/giao-an-11a1.json (Giao-an-Hinh-khong-gian-11A1.docx)
- **Tệp giọng:** `voice/16-soan-giao-an.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Soạn giáo án theo phân phối chương trình: cô chọn chương, bài hai mươi bốn, góc giữa đường thẳng và mặt phẳng, chọn tiết và đối tượng học sinh. AI soạn bản nháp đầy đủ: mục tiêu, thiết bị, tiến trình từng hoạt động với thời lượng, và phiếu học tập. Nội dung bám đúng giáo án lớp 11 A1: khởi động bằng hình chóp xoay trên máy chiếu, hình thành kiến thức qua bài H K G không một. Mọi mục đều sửa được, và bản nháp gắn nhãn AI soạn cho đến khi cô duyệt.
- **Phụ đề (VI):**
  - `05:19` Soạn giáo án theo phân phối chương / trình: cô chọn chương, bài 24,
  - `05:23` góc giữa đường thẳng và mặt phẳng, / chọn tiết và đối tượng học sinh.
  - `05:28` AI soạn bản nháp đầy / đủ: mục tiêu, thiết bị,
  - `05:32` tiến trình từng hoạt động với / thời lượng, và phiếu học tập.
  - `05:36` Nội dung bám đúng giáo án lớp 11A1: khởi / động bằng hình chóp xoay trên máy chiếu,
  - `05:42` hình thành kiến thức qua bài HKG-01.
  - `05:45` Mọi mục đều sửa được, và bản nháp gắn / nhãn AI soạn cho đến khi cô duyệt.

#### 17-ra-de-ma-tran · 05:52–06:17 (24.5 s, giọng 22.3 s)

- **Hình / thao tác quay:** /gv/ra-de: KT 15 phút; bộ 4 mã đề; mã 101; thay câu 3
- **Dữ liệu dùng:** Ma trận chuẩn + ngân hàng câu kiểm chứng (importBank)
- **Tệp giọng:** `voice/17-ra-de-ma-tran.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Đề kiểm tra tuần sau? Cô chọn loại đề kiểm tra mười lăm phút. Hệ thống điền sẵn số câu trắc nghiệm, tự luận và tổng điểm theo ma trận chuẩn, rồi sinh bốn mã đề từ ngân hàng đã kiểm chứng. Mỗi câu mang hai nhãn: máy kiểm chứng và người duyệt. Không ưng câu nào, bấm thay: câu mới giữ nguyên chủ đề, mức độ và điểm.
- **Phụ đề (VI):**
  - `05:53` Đề kiểm tra tuần sau? Cô chọn / loại đề kiểm tra 15 phút.
  - `05:57` Hệ thống điền sẵn số câu trắc nghiệm, tự / luận và tổng điểm theo ma trận chuẩn,
  - `06:03` rồi sinh 4 mã đề từ / ngân hàng đã kiểm chứng.
  - `06:06` Mỗi câu mang hai nhãn: máy / kiểm chứng và người duyệt.
  - `06:10` Không ưng câu nào, bấm thay: câu mới / giữ nguyên chủ đề, mức độ và điểm.

#### 18-soan-de-tai-lieu · 06:17–06:34 (17.4 s, giọng 15.2 s)

- **Hình / thao tác quay:** /gv/ra-de/tu-tai-lieu: soạn đề nháp, câu kiểm chứng / chưa kiểm chứng, nút duyệt
- **Dữ liệu dùng:** Tài liệu lớp 11A1 v2
- **Tệp giọng:** `voice/18-soan-de-tai-lieu.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Cô còn có thể yêu cầu AI soạn đề nháp từ chính tài liệu của lớp. Câu nào máy dựng được hình thì có đáp số đã tính lại. Câu chưa kiểm chứng được xếp cuối, và không được chọn sẵn. Chỉ câu cô duyệt mới vào ngân hàng.
- **Phụ đề (VI):**
  - `06:17` Cô còn có thể yêu cầu AI soạn đề / nháp từ chính tài liệu của lớp.
  - `06:22` Câu nào máy dựng được hình / thì có đáp số đã tính lại.
  - `06:26` Câu chưa kiểm chứng được xếp / cuối, và không được chọn sẵn.
  - `06:30` Chỉ câu cô duyệt mới vào ngân hàng.

#### 19-thong-bao-cong-tac · 06:34–06:51 (17.1 s, giọng 14.8 s)

- **Hình / thao tác quay:** /gv/tro-ly/thong-bao: nhắc hạn, sửa câu, gửi; công tắc AI theo lớp, hạn mức
- **Dữ liệu dùng:** Seed S5-E5
- **Tệp giọng:** `voice/19-thong-bao-cong-tac.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Trợ lý cũng soạn giúp thông báo cho lớp, ví dụ nhắc hạn nộp với đúng giờ địa phương. Cô sửa một câu rồi gửi. Và ở mỗi lớp, cô có công tắc bật hoặc tắt AI chấm bài và trợ lý, kèm mức sử dụng ba mươi ngày.
- **Phụ đề (VI):**
  - `06:35` Trợ lý cũng soạn giúp thông báo cho lớp,
  - `06:38` ví dụ nhắc hạn nộp với / đúng giờ địa phương.
  - `06:41` Cô sửa một câu rồi gửi.
  - `06:43` Và ở mỗi lớp, cô có công tắc bật / hoặc tắt AI chấm bài và trợ lý,
  - `06:48` kèm mức sử dụng 30 ngày.

### 04 Chấm bài viết tay

#### 20-chuong-cham · 06:51–06:58 (7.3 s, giọng 4.7 s)

- **Hình / thao tác quay:** Slide chương 04
- **Dữ liệu dùng:** —
- **Tệp giọng:** `voice/20-chuong-cham.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Chương bốn: chấm bài viết tay. Đây là khoảnh khắc cô Lan chờ đợi nhất.
- **Phụ đề (VI):**
  - `06:52` Chương bốn: chấm bài viết tay.
  - `06:54` Đây là khoảnh khắc cô Lan chờ đợi nhất.

#### 21-rubric · 06:58–07:16 (17.5 s, giọng 15.3 s)

- **Hình / thao tác quay:** /gv/rubric: 5 tiêu chí, sửa điểm → cảnh báo, sửa lại
- **Dữ liệu dùng:** RB-01 'Tính toán – khoảng cách': Hướng 2 · Các bước 4 · Kết quả 2 · Hình 1 · Trình bày 1
- **Tệp giọng:** `voice/21-rubric.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Mọi thứ bắt đầu từ rubric của cô: năm tiêu chí cho bài kiểm tra khoảng cách, gồm hướng giải, các bước, kết quả, hình vẽ và trình bày. Tổng điểm tính ngay. Nếu tổng không đủ mười, hệ thống cảnh báo và chặn lưu.
- **Phụ đề (VI):**
  - `06:59` Mọi thứ bắt đầu từ rubric của cô: năm / tiêu chí cho bài kiểm tra khoảng cách,
  - `07:05` gồm hướng giải, các bước, kết / quả, hình vẽ và trình bày.
  - `07:09` Tổng điểm tính ngay.
  - `07:10` Nếu tổng không đủ mười, hệ / thống cảnh báo và chặn lưu.

#### 22-nop-bai-viet-tay · 07:16–07:34 (18.0 s, giọng 14.6 s)

- **Hình / thao tác quay:** HS Bình An /hs/nop-bai 'Kiểm tra 15 phút – Khoảng cách': chụp 2 trang, nộp
- **Dữ liệu dùng:** Ảnh viết tay minh hoạ assets/bai-lam-bn01-p{1,2}-photo.jpg (đề khoảng cách, đáp số a√6/3)
- **Tệp giọng:** `voice/22-nop-bai-viet-tay.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Bình An làm bài kiểm tra trên giấy như mọi khi, rồi chụp từng trang bằng điện thoại. Ảnh được kiểm tra độ nét ngay khi chụp. Em bấm nộp, và nhận xác nhận kèm giờ của máy chủ. Không cần máy quét, không cần nhập lại.
- **Phụ đề (VI):**
  - `07:17` Bình An làm bài kiểm tra trên giấy như mọi / khi, rồi chụp từng trang bằng điện thoại.
  - `07:22` Ảnh được kiểm tra độ nét ngay khi chụp.
  - `07:25` Em bấm nộp, và nhận xác / nhận kèm giờ của máy chủ.
  - `07:28` Không cần máy quét, không cần nhập lại.

#### 23-wow-nhan-dang · 07:34–08:02 (27.8 s, giọng 25.2 s)

- **Hình / thao tác quay:** Slide: ảnh bài ↔ dòng đọc được (2 chỗ < 0,6 đánh dấu) ↔ rubric
- **Dữ liệu dùng:** Khớp đáp đọc chữ giả lập F-S5-095 ('Kẻ AH ⊥ SB … AH = a√6/3', chưa chắc '1/SA²', 'a√6/3')
- **Tệp giọng:** `voice/23-wow-nhan-dang.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Và đây là khác biệt thứ ba. Hệ thống đọc chữ viết tay của Bình An, tách từng dòng lập luận: kẻ A H vuông góc S B, chứng minh A H vuông góc mặt phẳng S B C, rồi tính A H bằng a căn sáu phần ba. Mỗi dòng có độ tin cậy. Hai chỗ chữ nhoè được đánh dấu để cô xem lại, thay vì đoán bừa. Rồi từng dòng được đối chiếu với từng tiêu chí của rubric.
- **Phụ đề (VI):**
  - `07:35` Và đây là khác biệt thứ ba.
  - `07:37` Hệ thống đọc chữ viết tay của Bình An, / tách từng dòng lập luận: kẻ AH ⊥ SB,
  - `07:43` chứng minh AH ⊥ (SBC), / rồi tính AH = a√6/3.
  - `07:47` Mỗi dòng có độ tin cậy.
  - `07:49` Hai chỗ chữ nhoè được đánh dấu / để cô xem lại, thay vì đoán bừa.
  - `07:55` Rồi từng dòng được đối chiếu / với từng tiêu chí của rubric.

#### 24-ai-de-xuat-diem · 08:02–08:25 (23.3 s, giọng 21.1 s)

- **Hình / thao tác quay:** /gv/cham-ai: điểm AI từng tiêu chí, bấm tiêu chí xem vùng, nhận 5, duyệt 10/10
- **Dữ liệu dùng:** F-S5-090 (2/4/2/1/1, tin cậy 0,86); ảnh trang = ảnh viết tay minh hoạ
- **Tệp giọng:** `voice/24-ai-de-xuat-diem.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Cô Lan mở bài của Bình An. AI đề xuất điểm cho từng tiêu chí, và khi cô bấm vào một tiêu chí, nó khoanh đúng vùng trên trang bài làm mà nó dựa vào. Cô nhận, sửa, hoặc bỏ từng tiêu chí. Nút duyệt chỉ mở khi mọi tiêu chí đã được xử lý. Mười trên mười. Và điểm cuối cùng là điểm của cô.
- **Phụ đề (VI):**
  - `08:02` Cô Lan mở bài của Bình An.
  - `08:04` AI đề xuất điểm cho từng tiêu chí, / và khi cô bấm vào một tiêu chí,
  - `08:09` nó khoanh đúng vùng trên / trang bài làm mà nó dựa vào.
  - `08:13` Cô nhận, sửa, hoặc bỏ từng tiêu chí.
  - `08:16` Nút duyệt chỉ mở khi mọi / tiêu chí đã được xử lý.
  - `08:20` Mười trên mười. Và điểm / cuối cùng là điểm của cô.

#### 25-song-song-tin-cay · 08:25–08:47 (22.2 s, giọng 20.0 s)

- **Hình / thao tác quay:** /gv/cham-ai chế độ song song: điểm AI ẩn, GV chấm, duyệt, bảng so sánh
- **Dữ liệu dùng:** F-S5-091 (seed S5-E6)
- **Tệp giọng:** `voice/25-song-song-tin-cay.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Làm sao biết có nên tin AI chấm? Trước khi bật đề xuất điểm cho một lớp, hệ thống chạy song song: AI chấm ngầm, điểm AI bị ẩn trong lúc cô chấm để cô không bị dẫn dắt. Sau khi duyệt, điểm của AI và của cô được so từng tiêu chí. Đề xuất của AI chỉ được bật khi độ khớp đạt ngưỡng.
- **Phụ đề (VI):**
  - `08:26` Làm sao biết có nên tin AI chấm?
  - `08:28` Trước khi bật đề xuất điểm cho một lớp, / hệ thống chạy song song: AI chấm ngầm,
  - `08:34` điểm AI bị ẩn trong lúc cô / chấm để cô không bị dẫn dắt.
  - `08:38` Sau khi duyệt, điểm của AI và / của cô được so từng tiêu chí.
  - `08:42` Đề xuất của AI chỉ được / bật khi độ khớp đạt ngưỡng.

#### 26-loi-sai-lop · 08:47–09:04 (16.8 s, giọng 14.6 s)

- **Hình / thao tác quay:** /gv/bai-co-cham/<bc>/loi-cua-lop: thẻ lỗi + bằng chứng, nhắc lại
- **Dữ liệu dùng:** Seed S5-E5
- **Tệp giọng:** `voice/26-loi-sai-lop.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Chấm xong cả lớp, trợ lý tổng hợp những lỗi sai chung, kèm bằng chứng là bài làm cụ thể, không xếp hạng, không phần trăm theo học sinh. Từ đó, cô giao ngay một bộ luyện nhắc lại cùng chủ đề cho buổi sau.
- **Phụ đề (VI):**
  - `08:48` Chấm xong cả lớp, trợ lý / tổng hợp những lỗi sai chung,
  - `08:52` kèm bằng chứng là bài làm cụ thể, không / xếp hạng, không phần trăm theo học sinh.
  - `08:58` Từ đó, cô giao ngay một bộ luyện / nhắc lại cùng chủ đề cho buổi sau.

#### 27-ket-qua-lop · 09:04–09:21 (16.7 s, giọng 14.5 s)

- **Hình / thao tác quay:** Bảng kết quả lớp × bài, mở một ô bằng chứng
- **Dữ liệu dùng:** Seed S3-E3
- **Tệp giọng:** `voice/27-ket-qua-lop.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Ở cấp lớp, bảng kết quả cho cô một ô cho mỗi học sinh và mỗi bài. Mở một ô là thấy bằng chứng: số lần thử, gợi ý đã dùng. Lớp học thật, với mã vào lớp và duyệt học sinh, đều có sẵn.
- **Phụ đề (VI):**
  - `09:05` Ở cấp lớp, bảng kết quả cho cô / một ô cho mỗi học sinh và mỗi bài.
  - `09:10` Mở một ô là thấy bằng chứng: / số lần thử, gợi ý đã dùng.
  - `09:14` Lớp học thật, với mã vào lớp / và duyệt học sinh, đều có sẵn.

#### 28-theo-doi-lop · 09:21–09:35 (14.7 s, giọng 12.5 s)

- **Hình / thao tác quay:** /gv/theo-doi: 4 thẻ, bảng học sinh
- **Dữ liệu dùng:** Seed E7 golden
- **Tệp giọng:** `voice/28-theo-doi-lop.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Và trong lúc học sinh làm bài, cô theo dõi cả lớp theo thời gian thực: ai đã mở bài, ai dùng gợi ý đến bậc nào, ai đang vướng. Số liệu để hỗ trợ đúng em, đúng lúc.
- **Phụ đề (VI):**
  - `09:21` Và trong lúc học sinh làm bài, cô theo dõi / cả lớp theo thời gian thực: ai đã mở bài,
  - `09:28` ai dùng gợi ý đến bậc nào, ai đang vướng.
  - `09:31` Số liệu để hỗ trợ đúng em, đúng lúc.

### Kết

#### 29-tuan-sau · 09:35–10:02 (26.2 s, giọng 23.6 s)

- **Hình / thao tác quay:** Slide 'Một tuần sau, 21:30'
- **Dữ liệu dùng:** Tình huống minh hoạ
- **Tệp giọng:** `voice/29-tuan-sau.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Một tuần sau, cũng chín giờ rưỡi tối. Cô Lan đã xong việc từ sớm: bài viết tay chấm xong, AI đề xuất, cô duyệt từng tiêu chí. Đề bốn mã đề đã sẵn sàng, đáp số đã kiểm chứng. Cô biết lớp hay sai ở bước nào để chữa đầu giờ. Còn Bình An? Em tự xoay hình, tự thấy hình chiếu, và lần này em hiểu vì sao.
- **Phụ đề (VI):**
  - `09:36` Một tuần sau, cũng chín giờ rưỡi tối.
  - `09:39` Cô Lan đã xong việc từ sớm: bài / viết tay chấm xong, AI đề xuất,
  - `09:44` cô duyệt từng tiêu chí.
  - `09:46` Đề bốn mã đề đã sẵn sàng, / đáp số đã kiểm chứng.
  - `09:50` Cô biết lớp hay sai ở / bước nào để chữa đầu giờ.
  - `09:54` Còn Bình An?
  - `09:55` Em tự xoay hình, tự thấy hình / chiếu, và lần này em hiểu vì sao.

#### 30-loi-ich-hs · 10:02–10:17 (15.8 s, giọng 13.2 s)

- **Hình / thao tác quay:** Slide lợi ích HS
- **Dữ liệu dùng:** —
- **Tệp giọng:** `voice/30-loi-ich-hs.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Với học sinh: nhìn thấy hình, tự tay thử, và hiểu vì sao. Gợi ý đến đúng lúc, không bị lộ đáp án. Câu hỏi được trả lời theo đúng tài liệu của lớp, có nguồn để tự đọc lại.
- **Phụ đề (VI):**
  - `10:02` Với học sinh: nhìn thấy hình, / tự tay thử, và hiểu vì sao.
  - `10:07` Gợi ý đến đúng lúc, không bị lộ đáp án.
  - `10:10` Câu hỏi được trả lời theo đúng tài / liệu của lớp, có nguồn để tự đọc lại.

#### 31-loi-ich-gv · 10:17–10:37 (19.3 s, giọng 16.7 s)

- **Hình / thao tác quay:** Slide lợi ích GV
- **Dữ liệu dùng:** —
- **Tệp giọng:** `voice/31-loi-ich-gv.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Với giáo viên: soạn nhanh hơn, tin được đáp số vì máy đã kiểm chứng, chấm bài viết tay có trợ lý, và hiểu lớp sai ở đâu. Trên hết, giáo viên luôn giữ quyền quyết định cuối cùng: bật tắt AI theo từng lớp, nhận hay bỏ từng đề xuất.
- **Phụ đề (VI):**
  - `10:18` Với giáo viên: soạn nhanh hơn, tin / được đáp số vì máy đã kiểm chứng,
  - `10:23` chấm bài viết tay có trợ / lý, và hiểu lớp sai ở đâu.
  - `10:27` Trên hết, giáo viên luôn giữ quyền quyết / định cuối cùng: bật tắt AI theo từng lớp,
  - `10:33` nhận hay bỏ từng đề xuất.

#### 32-tong-ket · 10:37–11:02 (25.6 s, giọng 22.9 s)

- **Hình / thao tác quay:** Slide tổng kết 6 bước
- **Dữ liệu dùng:** —
- **Tệp giọng:** `voice/32-tong-ket.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Tóm lại, một vòng khép kín: gõ đề và dựng hình, kiểm chứng bằng tính toán, soạn và duyệt đề, giáo án, giao bài với giờ mở lời giải, học sinh làm bài với gợi ý và hỏi đáp có nguồn, rồi chấm bài viết tay và hiểu lớp. Gợi ý trước, đáp số sau. AI đề xuất, giáo viên quyết định. Đúng trước, đẹp sau.
- **Phụ đề (VI):**
  - `10:37` Tóm lại, một vòng khép kín: gõ đề và / dựng hình, kiểm chứng bằng tính toán,
  - `10:43` soạn và duyệt đề, giáo án, / giao bài với giờ mở lời giải,
  - `10:48` học sinh làm bài với gợi ý và hỏi đáp có / nguồn, rồi chấm bài viết tay và hiểu lớp.
  - `10:54` Gợi ý trước, đáp số sau.
  - `10:56` AI đề xuất, giáo viên quyết / định. Đúng trước, đẹp sau.

#### 33-keu-goi · 11:02–11:26 (23.2 s, giọng 20.6 s)

- **Hình / thao tác quay:** Slide kêu gọi thí điểm + contact@bamboox.ai
- **Dữ liệu dùng:** —
- **Tệp giọng:** `voice/33-keu-goi.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Bạn muốn mang Hình Học Không Gian Cùng AI về tổ Toán của trường mình? Chúng tôi thí điểm với chính ngân hàng bài tập và giáo án của trường, giữ an toàn dữ liệu học sinh, và đồng hành cùng bộ video hướng dẫn cho từng tác vụ. Hãy liên hệ BambooAI qua địa chỉ contact a còng bamboox chấm ai.
- **Phụ đề (VI):**
  - `11:03` Bạn muốn mang Hình Học Không Gian / Cùng AI về tổ Toán của trường mình?
  - `11:08` Chúng tôi thí điểm với chính ngân / hàng bài tập và giáo án của trường,
  - `11:14` giữ an toàn dữ liệu học sinh, và đồng hành / cùng bộ video hướng dẫn cho từng tác vụ.
  - `11:21` Liên hệ BambooAI: contact@bamboox.ai

#### 34-ket · 11:26–11:33 (7.8 s, giọng 5.2 s)

- **Hình / thao tác quay:** Slide tagline kết
- **Dữ liệu dùng:** —
- **Tệp giọng:** `voice/34-ket.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** AI rất mạnh. Nhưng tư duy vẫn là của con người. Cảm ơn bạn đã theo dõi.
- **Phụ đề (VI):**
  - `11:26` AI rất mạnh. Nhưng tư / duy vẫn là của con người.
  - `11:30` Cảm ơn bạn đã theo dõi.

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.0 | 2026-11-28 | Bản đầu: 27 cảnh, 12 slide, giọng tạm Linh, bản nháp video | runner-T-261128-75 |
| v2.0 | 2026-11-28 | Bản cuối sau T-74: 35 cảnh, mạch kể hook → wow → cao trào → kêu gọi; bài viết tay khớp đề giao; hỏi đáp khớp nguồn; frames/ | runner-T-261128-77 |
