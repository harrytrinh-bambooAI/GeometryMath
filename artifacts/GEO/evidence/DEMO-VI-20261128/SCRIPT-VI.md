# SCRIPT-VI — Video giới thiệu "Hình Học Không Gian Cùng AI — presented by BambooAI" (T-261128-75)

Phiên bản: v1.0 · 2026-11-28 · sinh tự động bởi `app/e2e/demo-vi/make-script-vi.mjs` (không sửa tay: sửa `voice/lines.tsv` hoặc `app/e2e/demo-vi/scenes.json` rồi chạy lại).

- Tổng thời lượng bản nháp: 07:26 (446.4 s) · 27 cảnh · tiếng Việt, phụ đề VI burn-in (≤ 2 dòng × 42 ký tự).
- Style: BambooAI như video cũ (`Video/GeometryMath.mp4`, `Video/slides/*.png`): nền xanh lá đậm gradient, nhãn "PRESENTED BY BAMBOOAI" vàng chanh, thẻ kính có icon, slide chương "0N <TÊN>". Thông điệp: "AI is powerful. But thinking is still human." · "AI executes, humans decide" · contact@bamboox.ai.
- Dữ liệu thật: bài HKG-01..06 + gợi ý + lời giải từng bước của `materials/Ngan-hang-bai-tap-Hinh-khong-gian.xlsx` (đã có trong seed); định hướng và tiến trình của `materials/Giao-an-Hinh-khong-gian-11A1.docx`; giáo án chọn PPCT bài 24 "Phép chiếu vuông góc. Góc giữa đường thẳng và mặt phẳng" (đúng chủ đề giáo án 11A1).
- **Ảnh bài làm viết tay là ảnh MINH HOẠ** (`assets/bai-lam-hkg01-p1/p2[-photo].png|jpg`: chữ viết tay font Noteworthy trên giấy kẻ, hình chóp vẽ tay, học sinh giả "Nguyễn Minh Khang"). Owner thay ảnh chụp thật: chép đè `assets/bai-lam-hkg01-p1-photo.jpg`, `-p2-photo.jpg` (và `-p1-photo.png` cho slide 19) CÙNG TÊN, chạy `GEO_VI_KEEP_PHOTOS=1 node demo-vi/make-assets-vi.mjs` (giữ ảnh, chỉ dựng lại slide), quay lại cảnh 18, 20 rồi dựng video.
- AI giả lập (MockAiProvider) trong mọi cảnh. Câu trả lời hỏi đáp (cảnh 11) lấy từ fixture demo riêng `app/e2e/demo-vi/fixtures/qa-hkg01.json` (không đụng fixture e2e).
- Giọng đọc TẠM: macOS "Linh" (vi_VN). Thay giọng: gen bằng Google TTS theo `voice/lines.tsv`, thả `voice/<mã cảnh>.mp3|wav` CÙNG TÊN (tệp mới nhất thắng), chạy lại `node demo-vi/make-video-vi.mjs <node_modules ffmpeg>`: thời lượng cảnh và phụ đề tự khớp.

## Lệnh dựng lại

```bash
cd app && bash e2e/demo/stack-up.sh                       # máy chủ dev + web e2e trên cổng của worktree (AI giả lập)
cd e2e && node demo-vi/make-assets-vi.mjs <ffmpeg>        # slide PNG + ảnh viết tay minh hoạ
node demo-vi/gen-voice.mjs <ffmpeg>                       # giọng tạm (bỏ qua cảnh đã có tệp giọng)
GEO_PORT=… GEO_WEB_PORT=… npx playwright test -c demo-vi/demo.config.ts   # quay clip câm
node demo-vi/make-video-vi.mjs <node_modules có ffmpeg-static + @ffprobe-installer>
```

## Bảng cảnh

| Mốc | Cảnh | Chương | Loại | Hình |
|---|---|---|---|---|
| 00:00 | 00-mo-dau | Mở đầu | slide | slides/00-mo-dau.png |
| 00:19 | 01-van-de | Mở đầu | slide | slides/01-van-de.png |
| 00:39 | 02-gioi-thieu | Mở đầu | slide | slides/02-he-thong.png |
| 01:00 | 03-chuong-hinh-hoc | 01 Hỗ trợ hình học | slide | slides/03-chuong-1.png |
| 01:10 | 04-kho-de-that | 01 Hỗ trợ hình học | clip | clip (demo-vi) |
| 01:30 | 05-dung-hinh-tu-de | 01 Hỗ trợ hình học | clip | clip (demo-vi) |
| 01:53 | 06-xoay-ve-do | 01 Hỗ trợ hình học | clip | clip (demo-vi) |
| 02:12 | 07-loi-giai-tung-buoc | 01 Hỗ trợ hình học | clip | clip (demo-vi) |
| 02:30 | 08-chuong-ai | 02 AI đồng hành cùng học sinh | slide | slides/08-chuong-2.png |
| 02:40 | 09-goi-y-hoi-ai | 02 AI đồng hành cùng học sinh | clip | clip (demo-vi) |
| 03:01 | 10-kho-tri-thuc | 02 AI đồng hành cùng học sinh | clip | clip (demo-vi) |
| 03:16 | 11-hoi-dap-nguon | 02 AI đồng hành cùng học sinh | clip | clip (demo-vi) |
| 03:31 | 12-chuong-soan | 03 Soạn đề và giáo án cùng AI | slide | slides/12-chuong-3.png |
| 03:41 | 13-soan-de-prompt | 03 Soạn đề và giáo án cùng AI | clip | clip (demo-vi) |
| 03:59 | 14-ra-de-ma-tran | 03 Soạn đề và giáo án cùng AI | clip | clip (demo-vi) |
| 04:17 | 15-soan-giao-an | 03 Soạn đề và giáo án cùng AI | clip | clip (demo-vi) |
| 04:38 | 16-chuong-cham | 04 Chấm điểm và nhận dạng bài làm | slide | slides/16-chuong-4.png |
| 04:45 | 17-rubric | 04 Chấm điểm và nhận dạng bài làm | clip | clip (demo-vi) |
| 05:01 | 18-nop-bai-viet-tay | 04 Chấm điểm và nhận dạng bài làm | clip | clip (demo-vi) |
| 05:18 | 19-nhan-dang-bai-lam | 04 Chấm điểm và nhận dạng bài làm | slide | slides/19-nhan-dang.png |
| 05:36 | 20-ai-de-xuat-diem | 04 Chấm điểm và nhận dạng bài làm | clip | clip (demo-vi) |
| 05:54 | 21-loi-sai-lop | 04 Chấm điểm và nhận dạng bài làm | clip | clip (demo-vi) |
| 06:09 | 22-theo-doi-lop | 04 Chấm điểm và nhận dạng bài làm | clip | clip (demo-vi) |
| 06:25 | 23-loi-ich-hs | Lợi ích | slide | slides/23-loi-ich-hs.png |
| 06:40 | 24-loi-ich-gv | Lợi ích | slide | slides/24-loi-ich-gv.png |
| 06:56 | 25-tong-ket | Kết | slide | slides/25-tong-ket.png |
| 07:13 | 26-ket | Kết | slide | slides/26-ket.png |

## Kịch bản từng cảnh

### Mở đầu

#### 00-mo-dau · 00:00–00:19 (19.8 s, giọng 17.2 s)

- **Hình / thao tác quay:** Slide mở đầu: logo chữ, tiêu đề, tagline "AI is powerful. But thinking is still human."
- **Dữ liệu dùng:** —
- **Tệp giọng:** `voice/00-mo-dau.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Xin chào. Đây là Hình Học Không Gian Cùng AI, sản phẩm do BambooAI phát triển cho giáo viên và học sinh trung học. Trong vài phút tới, chúng tôi sẽ cho bạn thấy hệ thống giúp học hình không gian dễ hơn, và giúp giáo viên tiết kiệm thời gian ra sao.
- **Phụ đề (VI):**
  - `00:00` Xin chào. Đây là Hình / Học Không Gian Cùng AI,
  - `00:03` sản phẩm do BambooAI phát triển cho / giáo viên và học sinh trung học.
  - `00:08` Trong vài phút tới,
  - `00:09` chúng tôi sẽ cho bạn thấy hệ thống / giúp học hình không gian dễ hơn,
  - `00:14` và giúp giáo viên tiết / kiệm thời gian ra sao.

#### 01-van-de · 00:19–00:39 (19.8 s, giọng 17.2 s)

- **Hình / thao tác quay:** Slide 3 thẻ kính: khó hình dung · AI chung chung dễ sai/lộ đáp số · GV quá tải
- **Dữ liệu dùng:** —
- **Tệp giọng:** `voice/01-van-de.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Hình không gian khó hình dung trên trang giấy. Học sinh thường bế tắc vì không nhìn ra quan hệ giữa đường và mặt. AI thông thường lại có thể tính sai, hoặc đưa đáp số quá sớm. Còn giáo viên mất nhiều giờ mỗi tuần để soạn đề, soạn giáo án và chấm bài.
- **Phụ đề (VI):**
  - `00:20` Hình không gian khó hình / dung trên trang giấy.
  - `00:23` Học sinh thường bế tắc vì không / nhìn ra quan hệ giữa đường và mặt.
  - `00:28` AI thông thường lại có thể tính / sai, hoặc đưa đáp số quá sớm.
  - `00:32` Còn giáo viên mất nhiều giờ mỗi tuần / để soạn đề, soạn giáo án và chấm bài.

#### 02-gioi-thieu · 00:39–01:00 (20.9 s, giọng 18.3 s)

- **Hình / thao tác quay:** Slide giới thiệu hệ thống: Học sinh · Giáo viên · AI + engine kiểm chứng
- **Dữ liệu dùng:** —
- **Tệp giọng:** `voice/02-gioi-thieu.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Hệ thống có ba phần. Học sinh làm bài trên hình 3D, với gợi ý từng bậc. Giáo viên soạn đề, soạn giáo án, giao bài và chấm điểm. AI hỗ trợ ở mọi bước, nhưng hình và đáp số luôn được máy kiểm chứng bằng tính toán. AI đề xuất, giáo viên quyết định.
- **Phụ đề (VI):**
  - `00:40` Hệ thống có ba phần.
  - `00:41` Học sinh làm bài trên hình / 3D, với gợi ý từng bậc.
  - `00:45` Giáo viên soạn đề, soạn giáo / án, giao bài và chấm điểm.
  - `00:49` AI hỗ trợ ở mọi bước, nhưng hình và đáp số / luôn được máy kiểm chứng bằng tính toán.
  - `00:56` AI đề xuất, giáo viên quyết định.

### 01 Hỗ trợ hình học

#### 03-chuong-hinh-hoc · 01:00–01:10 (10.3 s, giọng 7.7 s)

- **Hình / thao tác quay:** Slide chương 01
- **Dữ liệu dùng:** —
- **Tệp giọng:** `voice/03-chuong-hinh-hoc.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Chương một: hỗ trợ hình học. Từ đề bài đến hình 3D, lời giải từng bước, và đáp số đã được kiểm chứng.
- **Phụ đề (VI):**
  - `01:01` Chương một: hỗ trợ hình học.
  - `01:03` Từ đề bài đến hình 3D, lời giải từng / bước, và đáp số đã được kiểm chứng.

#### 04-kho-de-that · 01:10–01:30 (19.7 s, giọng 17.5 s)

- **Hình / thao tác quay:** GV Lan mở /gv/kho-de, lọc THPT, chủ đề Góc; mở chi tiết HKG-01, khoanh đáp số 45°
- **Dữ liệu dùng:** HKG-01..06 từ Ngan-hang-bai-tap-Hinh-khong-gian.xlsx (đã có trong seed)
- **Tệp giọng:** `voice/04-kho-de-that.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Kho đề chứa chính các bài trong ngân hàng bài tập của tổ Toán, ví dụ bài H K G không một về góc trong hình chóp, hay bài H K G không ba về thể tích lăng trụ. Giáo viên lọc theo cấp học, loại hình và chủ đề. Mỗi bài có nhãn đã kiểm chứng.
- **Phụ đề (VI):**
  - `01:11` Kho đề chứa chính các bài trong / ngân hàng bài tập của tổ Toán,
  - `01:16` ví dụ bài HKG-01 về góc trong hình chóp, / hay bài HKG-03 về thể tích lăng trụ.
  - `01:22` Giáo viên lọc theo cấp / học, loại hình và chủ đề.
  - `01:26` Mỗi bài có nhãn đã kiểm chứng.

#### 05-dung-hinh-tu-de · 01:30–01:53 (22.9 s, giọng 20.7 s)

- **Hình / thao tác quay:** GV mở /gv/them-de, gõ đề biến thể của HKG-01 (SA = a√2, góc SC với đáy), bấm Dựng hình; 4 bước Đọc đề → Dựng hình → Giải → Kiểm chứng; kết quả 'Đã kiểm chứng'
- **Dữ liệu dùng:** Đề biến thể HKG-01 do GV gõ (khớp quy tắc mock FX-14 → hình HKN-02, đáp số 45°)
- **Tệp giọng:** `voice/05-dung-hinh-tu-de.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Giáo viên chỉ cần gõ đề. Ví dụ: cho hình chóp S A B C D, đáy là hình vuông cạnh a, S A vuông góc với đáy, S A bằng a căn hai. Tính góc giữa S C và mặt đáy. Hệ thống đọc đề, dựng hình, giải từng bước, rồi kiểm chứng đáp số bằng tính toán: bốn mươi lăm độ.
- **Phụ đề (VI):**
  - `01:31` Giáo viên chỉ cần gõ đề.
  - `01:33` Ví dụ: cho hình chóp S.ABCD, đáy là / hình vuông cạnh a, SA ⊥ đáy, SA = a√2.
  - `01:40` Tính góc giữa SC và mặt đáy.
  - `01:43` Hệ thống đọc đề, dựng / hình, giải từng bước,
  - `01:47` rồi kiểm chứng đáp số bằng tính toán: 45°.

#### 06-xoay-ve-do · 01:53–02:12 (18.7 s, giọng 16.5 s)

- **Hình / thao tác quay:** HS mở bài HKG-01 ở /hs/lam-bai, kéo xoay hình 3D; mở công cụ vẽ thêm / đo nếu có trên UI hiện tại
- **Dữ liệu dùng:** HKG-01 (hình chóp S.ABCD, SA ⊥ đáy, SA = a)
- **Tệp giọng:** `voice/06-xoay-ve-do.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Hình 3D xoay được bằng chuột hoặc ngón tay, để thấy rõ S A vuông góc với đáy. Học sinh có thể vẽ thêm điểm, nối đoạn, và đo độ dài hay góc ngay trên hình. Đại lượng là đáp số thì được khoá, để các em vẫn phải tự suy luận.
- **Phụ đề (VI):**
  - `01:54` Hình 3D xoay được bằng chuột hoặc ngón / tay, để thấy rõ SA vuông góc với đáy.
  - `01:59` Học sinh có thể vẽ thêm điểm, nối đoạn, / và đo độ dài hay góc ngay trên hình.
  - `02:05` Đại lượng là đáp số thì được khoá, / để các em vẫn phải tự suy luận.

#### 07-loi-giai-tung-buoc · 02:12–02:30 (18.4 s, giọng 16.2 s)

- **Hình / thao tác quay:** GV trình chiếu /gv/trinh-chieu: lời giải che, mở bước 1, phím → mở bước tiếp; hình tô sáng yếu tố
- **Dữ liệu dùng:** Lời giải từng bước + câu 'vì sao' (sheet 'Loi giai' của xlsx)
- **Tệp giọng:** `voice/07-loi-giai-tung-buoc.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Lời giải trình bày từng bước, mỗi bước kèm một câu: vì sao nghĩ ra bước này, đúng định hướng của giáo viên, ưu tiên hình chiếu và tam giác vuông. Khi trình chiếu trên lớp, giáo viên mở từng bước, và hình tô sáng đúng các yếu tố đang dùng.
- **Phụ đề (VI):**
  - `02:12` Lời giải trình bày từng bước, mỗi bước / kèm một câu: vì sao nghĩ ra bước này,
  - `02:18` đúng định hướng của giáo viên, ưu / tiên hình chiếu và tam giác vuông.
  - `02:22` Khi trình chiếu trên lớp, / giáo viên mở từng bước,
  - `02:26` và hình tô sáng đúng các yếu tố đang dùng.

### 02 AI đồng hành cùng học sinh

#### 08-chuong-ai · 02:30–02:40 (10.0 s, giọng 7.4 s)

- **Hình / thao tác quay:** Slide chương 02
- **Dữ liệu dùng:** —
- **Tệp giọng:** `voice/08-chuong-ai.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Chương hai: AI đồng hành cùng học sinh. Gợi ý trước, lời giải sau, và câu trả lời luôn có nguồn.
- **Phụ đề (VI):**
  - `02:31` Chương hai: AI đồng hành cùng học sinh.
  - `02:34` Gợi ý trước, lời giải sau, / và câu trả lời luôn có nguồn.

#### 09-goi-y-hoi-ai · 02:40–03:01 (20.5 s, giọng 16.7 s)

- **Hình / thao tác quay:** HS vào lớp (tên + mã), mở bài HKG-01, tab Gợi ý, mở gợi ý 1
- **Dữ liệu dùng:** Gợi ý 1/2 của HKG-01 (xlsx)
- **Tệp giọng:** `voice/09-goi-y-hoi-ai.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Học sinh vào lớp bằng tên và mã lớp, không cần tài khoản. Khi vướng, các em mở gợi ý một: góc giữa đường và mặt là góc giữa đường đó và hình chiếu của nó. Gợi ý hai mới chỉ bước kế tiếp. Lời giải chỉ mở vào giờ giáo viên đặt.
- **Phụ đề (VI):**
  - `02:41` Học sinh vào lớp bằng tên và / mã lớp, không cần tài khoản.
  - `02:45` Khi vướng, các em mở gợi ý một:
  - `02:47` góc giữa đường và mặt là góc giữa / đường đó và hình chiếu của nó.
  - `02:52` Gợi ý hai mới chỉ bước kế tiếp.
  - `02:54` Lời giải chỉ mở vào giờ giáo viên đặt.

#### 10-kho-tri-thuc · 03:01–03:16 (15.3 s, giọng 13.2 s)

- **Hình / thao tác quay:** GV mở /gv/kho-tri-thuc: danh sách tài liệu theo phiên bản, trạng thái
- **Dữ liệu dùng:** Tài liệu mẫu của lớp 11A1 (seed S5-E3)
- **Tệp giọng:** `voice/10-kho-tri-thuc.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Giáo viên đưa giáo án và tài liệu của lớp vào kho tri thức. Mỗi tài liệu có phiên bản: đang dùng, chờ duyệt, hoặc đã lưu trữ. AI chỉ trả lời dựa trên tài liệu mà giáo viên đã duyệt.
- **Phụ đề (VI):**
  - `03:01` Giáo viên đưa giáo án và tài / liệu của lớp vào kho tri thức.
  - `03:06` Mỗi tài liệu có phiên bản: đang / dùng, chờ duyệt, hoặc đã lưu trữ.
  - `03:10` AI chỉ trả lời dựa trên tài / liệu mà giáo viên đã duyệt.

#### 11-hoi-dap-nguon · 03:16–03:31 (15.1 s, giọng 12.9 s)

- **Hình / thao tác quay:** HS mở /hs/hoi-dap, gõ câu hỏi thật về HKG-01, gửi; câu trả lời có nguồn; bấm nguồn mở đoạn trích
- **Dữ liệu dùng:** Câu hỏi + câu trả lời giả lập theo giáo án 11A1 (fixtures/qa-hkg01.json, ghi đè hiển thị bằng page.route)
- **Tệp giọng:** `voice/11-hoi-dap-nguon.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Học sinh hỏi: vì sao hình chiếu của S D lên mặt đáy là A D? AI giải thích khái niệm, không làm hộ bài, và ghi rõ nguồn. Bấm vào nguồn là mở đúng đoạn trong tài liệu của lớp.
- **Phụ đề (VI):**
  - `03:17` Học sinh hỏi: vì sao hình / chiếu của SD lên mặt đáy là AD?
  - `03:21` AI giải thích khái niệm, không / làm hộ bài, và ghi rõ nguồn.
  - `03:25` Bấm vào nguồn là mở đúng / đoạn trong tài liệu của lớp.

### 03 Soạn đề và giáo án cùng AI

#### 12-chuong-soan · 03:31–03:41 (9.8 s, giọng 7.2 s)

- **Hình / thao tác quay:** Slide chương 03
- **Dữ liệu dùng:** —
- **Tệp giọng:** `voice/12-chuong-soan.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Chương ba: soạn đề và soạn giáo án cùng AI. Giáo viên ra yêu cầu, AI soạn nháp, giáo viên duyệt.
- **Phụ đề (VI):**
  - `03:32` Chương ba: soạn đề và / soạn giáo án cùng AI.
  - `03:35` Giáo viên ra yêu cầu, AI / soạn nháp, giáo viên duyệt.

#### 13-soan-de-prompt · 03:41–03:59 (17.9 s, giọng 15.7 s)

- **Hình / thao tác quay:** GV mở /gv/ra-de/tu-tai-lieu/<doc>, bấm soạn đề nháp; danh sách câu nháp có/không kiểm chứng
- **Dữ liệu dùng:** Tài liệu lớp 11A1 phiên bản 2 (seed S5-E3)
- **Tệp giọng:** `voice/13-soan-de-prompt.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Từ tài liệu đã có trong kho, giáo viên yêu cầu AI soạn đề nháp. Câu nào máy dựng được hình thì có đáp số đã tính lại. Câu chưa kiểm chứng được xếp cuối và không được chọn sẵn. Giáo viên duyệt từng câu trước khi đưa vào ngân hàng.
- **Phụ đề (VI):**
  - `03:41` Từ tài liệu đã có trong kho, giáo / viên yêu cầu AI soạn đề nháp.
  - `03:46` Câu nào máy dựng được hình / thì có đáp số đã tính lại.
  - `03:50` Câu chưa kiểm chứng được xếp / cuối và không được chọn sẵn.
  - `03:54` Giáo viên duyệt từng câu / trước khi đưa vào ngân hàng.

#### 14-ra-de-ma-tran · 03:59–04:17 (18.4 s, giọng 16.2 s)

- **Hình / thao tác quay:** GV /gv/ra-de: chọn loại đề KT 15 phút; mở bộ 4 mã đề, mã 101, đổi câu 3
- **Dữ liệu dùng:** Ma trận chuẩn + ngân hàng câu đã kiểm chứng (materials/de-thi qua importBank)
- **Tệp giọng:** `voice/14-ra-de-ma-tran.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Ra đề theo ma trận chuẩn: chọn loại đề kiểm tra mười lăm phút, hệ thống điền sẵn số câu trắc nghiệm, tự luận và tổng điểm, rồi sinh bốn mã đề từ ngân hàng đã kiểm chứng. Đổi một câu, câu mới vẫn giữ chủ đề, mức độ và điểm.
- **Phụ đề (VI):**
  - `03:59` Ra đề theo ma trận chuẩn: / chọn loại đề kiểm tra 15 phút,
  - `04:04` hệ thống điền sẵn số câu trắc / nghiệm, tự luận và tổng điểm,
  - `04:08` rồi sinh 4 mã đề từ / ngân hàng đã kiểm chứng.
  - `04:12` Đổi một câu, câu mới vẫn / giữ chủ đề, mức độ và điểm.

#### 15-soan-giao-an · 04:17–04:38 (20.8 s, giọng 18.6 s)

- **Hình / thao tác quay:** GV /gv/giao-an/moi: chọn chương, bài, tiết, đối tượng; bấm soạn; cuộn bản nháp
- **Dữ liệu dùng:** Nội dung bản nháp theo Giao-an-Hinh-khong-gian-11A1.docx (mục tiêu, tiến trình) — fixtures/giao-an-11a1.json ghi đè hiển thị
- **Tệp giọng:** `voice/15-soan-giao-an.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Soạn giáo án theo phân phối chương trình: chọn chương, bài và tiết, chọn đối tượng học sinh. AI soạn bản nháp có mục tiêu, tiến trình hoạt động và thời lượng, như giáo án Hình không gian của lớp 11 A1. Bản nháp gắn nhãn AI soạn, để giáo viên sửa trước khi dùng.
- **Phụ đề (VI):**
  - `04:18` Soạn giáo án theo phân phối chương / trình: chọn chương, bài và tiết,
  - `04:23` chọn đối tượng học sinh.
  - `04:24` AI soạn bản nháp có mục tiêu, tiến / trình hoạt động và thời lượng,
  - `04:29` như giáo án Hình không gian của lớp 11A1.
  - `04:32` Bản nháp gắn nhãn AI soạn, để / giáo viên sửa trước khi dùng.

### 04 Chấm điểm và nhận dạng bài làm

#### 16-chuong-cham · 04:38–04:45 (7.2 s, giọng 4.6 s)

- **Hình / thao tác quay:** Slide chương 04
- **Dữ liệu dùng:** —
- **Tệp giọng:** `voice/16-chuong-cham.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Chương bốn: chấm điểm, và nhận dạng bài làm viết tay của học sinh.
- **Phụ đề (VI):**
  - `04:39` Chương bốn: chấm điểm, và nhận / dạng bài làm viết tay của học sinh.

#### 17-rubric · 04:45–05:01 (16.3 s, giọng 14.1 s)

- **Hình / thao tác quay:** GV /gv/rubric: rubric 5 tiêu chí, sửa điểm tiêu chí 2 → cảnh báo tổng ≠ 10, sửa lại
- **Dữ liệu dùng:** Rubric 5 tiêu chí Hướng giải 2 · Các bước 4 · Kết quả 2 · Hình 1 · Trình bày 1 (seed rb01 của S5-E4)
- **Tệp giọng:** `voice/17-rubric.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Giáo viên tạo rubric năm tiêu chí cho bài kiểm tra: hướng giải, các bước, kết quả, hình vẽ, và trình bày. Tổng điểm được tính ngay. Nếu tổng không đủ mười điểm, hệ thống cảnh báo và chặn lưu.
- **Phụ đề (VI):**
  - `04:46` Giáo viên tạo rubric năm tiêu chí cho bài / kiểm tra: hướng giải, các bước, kết quả,
  - `04:52` hình vẽ, và trình bày.
  - `04:54` Tổng điểm được tính ngay.
  - `04:55` Nếu tổng không đủ mười điểm, / hệ thống cảnh báo và chặn lưu.

#### 18-nop-bai-viet-tay · 05:01–05:18 (16.5 s, giọng 11.3 s)

- **Hình / thao tác quay:** HS /hs/nop-bai: chụp trang 1, trang 2 (ảnh viết tay), nộp; xác nhận giờ máy chủ
- **Dữ liệu dùng:** Ảnh bài làm viết tay MINH HOẠ assets/bai-lam-hkg01-p1.png, p2.png (HS giả 'Nguyễn Minh Khang')
- **Tệp giọng:** `voice/18-nop-bai-viet-tay.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Học sinh làm bài trên giấy như thường lệ, rồi chụp từng trang để nộp. Ảnh được kiểm tra độ nét trước khi nhận. Sau khi nộp, các em thấy xác nhận kèm giờ của máy chủ.
- **Phụ đề (VI):**
  - `05:02` Học sinh làm bài trên giấy như thường / lệ, rồi chụp từng trang để nộp.
  - `05:07` Ảnh được kiểm tra độ nét trước khi nhận.
  - `05:10` Sau khi nộp, các em thấy xác / nhận kèm giờ của máy chủ.

#### 19-nhan-dang-bai-lam · 05:18–05:36 (17.8 s, giọng 15.2 s)

- **Hình / thao tác quay:** Slide minh hoạ: ảnh bài viết tay ↔ dòng chữ đọc được ↔ tiêu chí rubric; dòng độ tin cậy thấp đánh dấu
- **Dữ liệu dùng:** Ảnh minh hoạ + kết quả đọc chữ giả lập theo bài HKG-01
- **Tệp giọng:** `voice/19-nhan-dang-bai-lam.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Hệ thống đọc chữ viết tay trong ảnh, tách từng dòng lập luận, rồi đối chiếu với từng tiêu chí của rubric. Dòng nào đọc chưa chắc được đánh dấu để giáo viên xem lại. Ảnh trong video là ảnh minh hoạ, tên học sinh là tên giả.
- **Phụ đề (VI):**
  - `05:19` Hệ thống đọc chữ viết tay trong / ảnh, tách từng dòng lập luận,
  - `05:23` rồi đối chiếu với từng / tiêu chí của rubric.
  - `05:26` Dòng nào đọc chưa chắc được / đánh dấu để giáo viên xem lại.
  - `05:30` Ảnh trong video là ảnh minh / hoạ, tên học sinh là tên giả.

#### 20-ai-de-xuat-diem · 05:36–05:54 (17.8 s, giọng 15.6 s)

- **Hình / thao tác quay:** GV /gv/cham-ai/<bài>: điểm AI từng tiêu chí, bấm tiêu chí xem vùng ảnh, nhận 5 tiêu chí, duyệt
- **Dữ liệu dùng:** Bài nộp giả lập (seed S5-E6), ảnh trang hiển thị = ảnh viết tay minh hoạ
- **Tệp giọng:** `voice/20-ai-de-xuat-diem.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** AI đề xuất điểm cho từng tiêu chí, và khoanh vùng trên trang bài làm mà nó dựa vào. Giáo viên nhận, sửa, hoặc bỏ từng tiêu chí. Nút duyệt chỉ mở khi mọi tiêu chí đã được xử lý. Điểm cuối cùng luôn do giáo viên quyết định.
- **Phụ đề (VI):**
  - `05:36` AI đề xuất điểm cho từng tiêu chí,
  - `05:39` và khoanh vùng trên trang / bài làm mà nó dựa vào.
  - `05:42` Giáo viên nhận, sửa, / hoặc bỏ từng tiêu chí.
  - `05:45` Nút duyệt chỉ mở khi mọi / tiêu chí đã được xử lý.
  - `05:49` Điểm cuối cùng luôn do / giáo viên quyết định.

#### 21-loi-sai-lop · 05:54–06:09 (15.8 s, giọng 13.6 s)

- **Hình / thao tác quay:** GV /gv/bai-co-cham/<bc>/loi-cua-lop: thẻ lỗi kèm bằng chứng, bộ luyện nhắc lại
- **Dữ liệu dùng:** Bài đã chấm (seed S5-E5)
- **Tệp giọng:** `voice/21-loi-sai-lop.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Từ các bài đã chấm, trợ lý tổng hợp lỗi sai chung của cả lớp, kèm bằng chứng là bài làm cụ thể, và không xếp hạng học sinh. Giáo viên có thể gợi ý ngay một bộ luyện nhắc lại cùng chủ đề.
- **Phụ đề (VI):**
  - `05:54` Từ các bài đã chấm, trợ lý tổng / hợp lỗi sai chung của cả lớp,
  - `05:59` kèm bằng chứng là bài làm cụ / thể, và không xếp hạng học sinh.
  - `06:03` Giáo viên có thể gợi ý ngay một / bộ luyện nhắc lại cùng chủ đề.

#### 22-theo-doi-lop · 06:09–06:25 (15.3 s, giọng 13.1 s)

- **Hình / thao tác quay:** GV /gv/theo-doi/<bài>: 4 thẻ tổng hợp, bảng theo học sinh
- **Dữ liệu dùng:** Lớp 11A1 mẫu (seed E7 golden)
- **Tệp giọng:** `voice/22-theo-doi-lop.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Trong lúc học sinh làm bài, giáo viên theo dõi cả lớp: ai đã mở bài, ai dùng gợi ý đến bậc nào, ai đang vướng. Số liệu dùng để hỗ trợ từng em, không để so sánh học sinh.
- **Phụ đề (VI):**
  - `06:10` Trong lúc học sinh làm bài, giáo / viên theo dõi cả lớp: ai đã mở bài,
  - `06:15` ai dùng gợi ý đến bậc nào, ai đang vướng.
  - `06:18` Số liệu dùng để hỗ trợ từng / em, không để so sánh học sinh.

### Lợi ích

#### 23-loi-ich-hs · 06:25–06:40 (15.8 s, giọng 13.2 s)

- **Hình / thao tác quay:** Slide lợi ích học sinh (thẻ kính có icon)
- **Dữ liệu dùng:** —
- **Tệp giọng:** `voice/23-loi-ich-hs.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Với học sinh: nhìn thấy hình, tự tay thử, và hiểu vì sao. Gợi ý đến đúng lúc, không bị lộ đáp án. Câu hỏi được trả lời theo đúng tài liệu của lớp, có nguồn để tự đọc lại.
- **Phụ đề (VI):**
  - `06:25` Với học sinh: nhìn thấy hình, / tự tay thử, và hiểu vì sao.
  - `06:30` Gợi ý đến đúng lúc, không bị lộ đáp án.
  - `06:33` Câu hỏi được trả lời theo đúng tài / liệu của lớp, có nguồn để tự đọc lại.

#### 24-loi-ich-gv · 06:40–06:56 (15.7 s, giọng 13.1 s)

- **Hình / thao tác quay:** Slide lợi ích giáo viên (thẻ kính có icon)
- **Dữ liệu dùng:** —
- **Tệp giọng:** `voice/24-loi-ich-gv.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Với giáo viên: soạn đề, soạn giáo án và chấm bài nhanh hơn. Đáp số đã được máy kiểm chứng. Biết lớp sai ở đâu để dạy lại đúng chỗ. Và giáo viên luôn giữ quyền quyết định cuối cùng.
- **Phụ đề (VI):**
  - `06:41` Với giáo viên: soạn đề, soạn / giáo án và chấm bài nhanh hơn.
  - `06:45` Đáp số đã được máy kiểm chứng.
  - `06:48` Biết lớp sai ở đâu để dạy lại đúng chỗ.
  - `06:50` Và giáo viên luôn giữ / quyền quyết định cuối cùng.

### Kết

#### 25-tong-ket · 06:56–07:13 (16.9 s, giọng 14.3 s)

- **Hình / thao tác quay:** Slide tổng kết 6 bước vận hành + băng 'AI executes, humans decide'
- **Dữ liệu dùng:** —
- **Tệp giọng:** `voice/25-tong-ket.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** Tóm lại: dựng hình từ đề, kiểm chứng bằng tính toán, gợi ý trước lời giải sau, soạn đề và giáo án bằng yêu cầu, chấm bài theo rubric và nhận dạng bài viết tay. AI thực hiện, con người quyết định.
- **Phụ đề (VI):**
  - `06:57` Tóm lại: dựng hình từ đề, kiểm chứng bằng / tính toán, gợi ý trước lời giải sau,
  - `07:02` soạn đề và giáo án bằng yêu cầu, chấm bài / theo rubric và nhận dạng bài viết tay.
  - `07:08` AI thực hiện, con người quyết định.

#### 26-ket · 07:13–07:26 (12.9 s, giọng 10.3 s)

- **Hình / thao tác quay:** Slide kết: tagline, contact@bamboox.ai
- **Dữ liệu dùng:** —
- **Tệp giọng:** `voice/26-ket.wav` (thay được bằng `.mp3` cùng tên)
- **Lời đọc (VI):** AI rất mạnh, nhưng tư duy vẫn là của con người. Cảm ơn bạn đã theo dõi. Liên hệ BambooAI qua địa chỉ contact a còng bamboox chấm ai.
- **Phụ đề (VI):**
  - `07:14` AI rất mạnh, nhưng tư / duy vẫn là của con người.
  - `07:18` Cảm ơn bạn đã theo dõi.
  - `07:20` Liên hệ BambooAI: contact@bamboox.ai

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.0 | 2026-11-28 | Bản đầu: 27 cảnh, 12 slide, giọng tạm Linh, bản nháp video | runner-T-261128-75 |
