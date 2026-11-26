# PLAN · Kho bài giảng Toán 11 (3 tầng) · Phiên bản: v1.0 · 27/11/2026

Trạng thái: **ĐÃ DUYỆT** (owner "Go, phiên mới" 27/11/2026). Chạy ở PHIÊN MỚI: `đọc artifacts/GEO/materials/PLAN-kho-bai-giang.md và làm`.

## 1. Giao việc 4 dòng
- **Mục tiêu:** kho bài giảng Toán 11 phong phú — mỗi tiết theo PPCT có 2–3 bài giảng biến thể (khác phong cách dạy, trường, giáo viên, nguồn), tổng ~200–300 bài giảng; MỌI bài giảng và bài tập tham chiếu MỘT kho kiến thức chuẩn duy nhất (không lệch kiến thức), cải tiến dần, sẵn cho AI đồng hành (chặng 5 "bài giảng với AI").
- **Nguồn:** bộ tài liệu `artifacts/GEO/materials/lop-11/` v1.1 (27/11/2026: 9 chương / 33 bài KNTT, mục lục khớp loigiaihay c1370 + VietJack toan-11-kn, 252 link đã kiểm, `nguon-tong-hop.md` 15 nguồn); PPCT Toán 11 KNTT (~105 tiết, cần tra); mẫu giáo án Công văn 5512/BGDĐT; đối chiếu thêm Chân trời sáng tạo, Cánh diều.
- **Xong khi (máy kiểm):** `python3 artifacts/GEO/materials/tools/lint.py lop-11` exit 0 trên toàn kho; 0 mã kiến thức treo; mọi tiết PPCT có ≥ 2 biến thể; ≥ 90% đơn vị kiến thức trạng thái `da-kiem`; README có bảng PPCT × biến thể.
- **Ngoài phạm vi:** code app (`app/**`); chép nguyên văn SGK/SBT/giáo án có bản quyền (tự soạn, chỉ dẫn link); link tải lậu.

## 2. Kiến trúc 3 tầng (thư mục `artifacts/GEO/materials/lop-11/`)
| Tầng | Thư mục | Nội dung | Quy tắc |
|---|---|---|---|
| 1 Kiến thức chuẩn | `kien-thuc/chuong-NN/` | Mỗi đơn vị (định nghĩa/định lí/công thức/tính chất/phương pháp) một mục, mã cố định `KT11-CNN-<LOAI>-NNN` (LOAI: DN, DL, CT, TC, PP), phát biểu chuẩn (LaTeX), điều kiện, ví dụ ngắn, ≥ 2 nguồn đối chiếu, `trang_thai: nhap|da-kiem`, phiên bản | **Nguồn sự thật duy nhất** — chỉ tầng này định nghĩa công thức |
| 2 Bài giảng biến thể | `bai-giang/tiet-NNN-<ten>/<bien-the>.md` | Front-matter YAML: `tiet, bai, chuong, kien_thuc: [mã…], phong_cach (truyen-thong|kham-pha|lop-hoc-dao-nguoc|du-an-stem|tro-choi), doi_tuong (co-ban|kha|chuyen), thoi_luong, nguon: [...], tac_gia, danh_gia`; thân theo CV 5512: Mục tiêu (kiến thức, năng lực, phẩm chất) · Thiết bị · Tiến trình 4 hoạt động (Mở đầu → Hình thành kiến thức → Luyện tập → Vận dụng; mỗi HĐ: mục tiêu, nội dung, sản phẩm, tổ chức) · Phiếu học tập + đáp án · Bảng nguồn | Chỉ DẪN mã kiến thức; diễn giải tự do nhưng công thức phải khớp tầng 1 |
| 3 Bài tập | `bai-tap/` (đã có) | Thêm front-matter `kien_thuc: [mã…]`, mức độ, lời giải | Như tầng 2 |
Hình: `hinh/` (SVG, đã có 19).

## 3. Kiểm soát chính xác (script trong `artifacts/GEO/materials/tools/`)
- `lint.py`: front-matter đủ trường; mã kiến thức tồn tại; công thức LaTeX trong bài giảng/bài tập (khối `$$…$$` có gắn `<!-- KT:mã -->`) khớp phát biểu chuẩn sau chuẩn hoá; link nội bộ hợp lệ; mỗi tệp có bảng Nguồn; mỗi tiết PPCT ≥ 2 biến thể.
- `impact.py <mã>`: liệt kê mọi bài giảng/bài tập dẫn tới mã đó (cập nhật đồng loạt khi sửa kiến thức).
- Soát chéo toán học bằng agent khác (model khác người soạn); chỉ `da-kiem` khi lint + soát chéo đạt.
- Mỗi tệp: `Phiên bản` + mục `Lịch sử`; `CHANGELOG.md` chung; mục "Góp ý" cuối tệp (sau này: ★ của giáo viên trong app).
- Sẵn cho AI: mã cố định, front-matter có cấu trúc, mục ngắn rõ (RAG); AI đồng hành chỉ trả lời dựa trên kiến thức `da-kiem`.

## 4. Các bước + ước tính
| Bước | Việc | Agent | Ước tính |
|---|---|---|---|
| 1 Nền | Schema + `lint.py` + `impact.py` + bảng PPCT ~105 tiết (tra nguồn) | 1 Opus | ~0,5M · 1 giờ |
| 1b | Kho kiến thức chuẩn ~250–400 đơn vị từ v1.1 (chia 3 nhóm chương) | 3 Sonnet song song | ~1,5M · 1 giờ |
| 2 Bài giảng | ~105 tiết × 2–3 biến thể ≈ 200–300 bài giảng; chia theo chương | 3–4 Sonnet song song (≤ 4) | ~6–9M · 4–6 giờ |
| 3 Soát | Soát chéo toán học + độ phủ nguồn; sửa; gắn `da-kiem` | 1–2 Sonnet/Opus | ~1–2M |
| 4 (sau) | Nối vào app (kho đề, bài giảng AI chặng 5) | theo roadmap | — |
Commit sau mỗi bước đạt lint. Agent con: brief theo CLAUDE.md §6 (Mục tiêu / Input / Xong khi / Ngoài phạm vi / Ngân sách / báo cáo ≤ 10 dòng, tiến độ `.claude/progress/runner-<tên>.md`).

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.0 | 27/11/2026 | Phương án 3 tầng được owner duyệt | delivery-lead (Claude) |
