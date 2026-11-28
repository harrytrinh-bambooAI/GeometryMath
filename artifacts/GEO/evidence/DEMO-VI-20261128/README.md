# README — Video giới thiệu + bộ video hướng dẫn tiếng Việt (BambooAI)

Phiên bản: v1.0 · 2026-11-28 · T-261128-76 / T-261128-77

## Có gì trong thư mục này

| Tệp | Nội dung |
|---|---|
| `GEO-intro-vi-1080p.mp4` | Video giới thiệu bản cuối (H.264 1920x1080 + AAC, phụ đề VI burn-in). Không lưu trong git, sinh lại bằng lệnh dưới |
| `subtitles-vi.srt`, `probe.txt`, `timeline-vi.json` | Phụ đề riêng, thông số kỹ thuật, mốc từng cảnh |
| `SCRIPT-VI.md` | Kịch bản 35 cảnh: lời đọc, phụ đề, thao tác quay, dữ liệu dùng (sinh tự động) |
| `COMPARE.md` | Đối chiếu tính năng ↔ các video cũ (T-75) |
| `voice/lines.tsv` + `voice/<mã>.wav` | Lời đọc từng cảnh + tệp giọng (giọng tạm macOS "Linh") |
| `slides/`, `assets/` | Slide PNG style BambooAI; ảnh bài viết tay **minh hoạ** (học sinh giả "Bình An") |
| `frames/<mã>.jpg` | 1 khung/cảnh để soát (LaTeX thô, hình bị cắt) |
| `GUIDE.md` | Mục lục bộ video hướng dẫn (đặt vào trang trợ giúp) |
| `guides/<id>.mp4`, `guides/<id>.srt` | 17 video hướng dẫn (12 giáo viên, 5 học sinh) |
| `guides/voice/<id>/lines.tsv` + giọng | Lời đọc + giọng từng video hướng dẫn |
| `guides/slides/`, `guides/frames/` | Slide mở / tóm tắt; 1 khung/video để soát |

## Thay giọng đọc bằng Google TTS

1. Mở `voice/lines.tsv` (video giới thiệu) hoặc `guides/voice/<id>/lines.tsv` (từng video hướng dẫn). Cột 1 là **mã**, cột 2 là **lời đọc**; cột 3 (nếu có) là phụ đề hiển thị khi khác lời đọc (ví dụ "HKG-01" đọc là "H K G không một").
2. Sinh giọng Google TTS cho từng dòng, lưu **cùng thư mục, tên = mã**: ví dụ `voice/06-wow-dung-hinh.mp3`, `guides/voice/02-gv-giao-bai/b3.mp3`. Có thể giữ hoặc xoá tệp `.wav` tạm: **tệp mới nhất thắng**.
3. Chạy một lệnh (từ gốc repo):

```bash
bash app/e2e/demo-vi/rebuild.sh
```

Lệnh này dựng lại slide, sinh giọng tạm cho dòng còn thiếu, rồi dựng video giới thiệu và cả 17 video hướng dẫn. Thời lượng mỗi cảnh và mốc phụ đề **tự khớp theo độ dài tệp giọng mới**, không cần quay lại. Nếu sửa lời ở `lines.tsv`, phải gen lại giọng của dòng đó (hoặc xoá `.wav` cũ để giọng tạm được sinh lại).

- Đổi giọng tạm macOS: `GEO_VI_VOICE=Linh`.
- Thay ảnh bài viết tay bằng ảnh chụp thật: chép đè `assets/bai-lam-bn01-p1-photo.jpg|png`, `-p2-photo.jpg` cùng tên, rồi quay lại (bước dưới).

## Quay lại (khi giao diện thay đổi)

```bash
cd app && bash e2e/demo/stack-up.sh             # máy chủ dev + web e2e trên cổng của worktree, AI giả lập
GEO_VI_RECORD=1 bash e2e/demo-vi/rebuild.sh      # quay 20 clip giới thiệu + 17 clip hướng dẫn, rồi dựng
bash e2e/demo/stack-down.sh
```

## Ghi chú dữ liệu

- Bài HKG-01..06, gợi ý, lời giải: `materials/Ngan-hang-bai-tap-Hinh-khong-gian.xlsx`. Giáo án: `materials/Giao-an-Hinh-khong-gian-11A1.docx` (fixture demo `app/e2e/demo-vi/fixtures/giao-an-11a1.json`).
- Bài kiểm tra viết tay của "Bình An" giải đúng đề bài được giao "Kiểm tra 15 phút – Khoảng cách" (d(A,(SBC)) = a√6/3), khớp rubric "Tính toán – khoảng cách" và bản đọc chữ của AI giả lập.
- AI giả lập trong mọi cảnh. Cờ chặng 5 chỉ bật trong phiên dev local khi quay.

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.0 | 2026-11-28 | Bản đầu (T-76 + T-77) | runner-T-261128-77 |
