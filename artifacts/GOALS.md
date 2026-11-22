# GOALS — Hình Học Không Gian Cùng AI

> Thẻ mục tiêu của goal run, lưu ngày 22-11-2026. Chi tiết trạng thái từng epic: `artifacts/GEO/work/epic_board.md`. Chỉ ghi thứ tự, không ghi mốc thời gian.

```
OVERALL GOAL   Mọi feature của dự án GeometryMath xong: GEO (MVP hình học không gian)         1 feature
  FEATURE GOAL   GEO xong = bảy epic dưới đây xong + README nêu phiên bản hiện hành
    EPIC GOAL    E1 Nền tảng và gọi AI phía máy chủ; bộ đề chuẩn          — xong = user story + AC + test case, đối chiếu 100%, R3 duyệt   [ ]
    EPIC GOAL    E2 Hình đúng: dựng hình, hình mẫu, kiểm chứng, nhãn       — như trên                                                          [ ]
    EPIC GOAL    E3 Thao tác hình 3D trên thiết bị cảm ứng                  — như trên                                                          [ ]
    EPIC GOAL    E4 Gợi ý, lời giải đồng bộ hình, hỏi đáp, tự học           — như trên                                                          [ ]
    EPIC GOAL    E5 Kho đề của Giáo viên mẫu                                — như trên                                                          [ ]
    EPIC GOAL    E6 Giao bài và kiểm soát lời giải                          — như trên                                                          [ ]
    EPIC GOAL    E7 Theo dõi lớp và ghi sự kiện                             — như trên                                                          [ ]
THIS RUN       goal run: bảy epic tới user story + AC + test case, sẵn sàng cho R3 của chủ đầu tư;
               song song: kiến trúc dữ liệu và phi chức năng (1000 người dùng đồng thời, đa trường, truy vấn tối ưu);
               song song: khung mã nguồn Java (build chạy được, cấu trúc module, hạ tầng thử)
NOT IN THIS RUN  viết mã chức năng (chia việc cho agent code SAU khi user story, test case và khung mã xong); chặng 2 đến 9; tích hợp AI thật
OPEN INPUTS    5 câu hỏi trong brief mục 6 (ngưỡng hiệu năng, hạ tầng, nhà cung cấp AI, định danh học sinh, dữ liệu thử tải): dùng mặc định ghi trong brief, mọi thứ dựa trên chúng đánh dấu tạm
```

## Cách chạy
- **Agent chính (A + PO):** lập mục tiêu, giữ bảng chung, phán xung đột sau 2 lượt, kiểm G1–G7 từng epic, không để agent con bịa yêu cầu hoặc lặp vô hạn (tối đa 3 lượt mỗi epic).
- **Ba agent con song song, trao đổi qua `artifacts/GEO/board/BOARD.md`:** BA (user story, AC), QC (test case), kiến trúc sư (dữ liệu, truy vấn, phi chức năng).
- **Một agent dựng khung mã nguồn Java** chạy cùng lúc, làm việc trong `app/`.
- Ngân sách: lean.

## Nhật ký thay đổi mục tiêu
| Ngày | Thay đổi | Ai |
|---|---|---|
| 22-11-2026 | Tạo thẻ. Thêm khung mã Java vào THIS RUN theo yêu cầu của chủ đầu tư | PO |
