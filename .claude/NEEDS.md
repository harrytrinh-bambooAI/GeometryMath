Phiên bản: v0.1 · 2026-11-26 · <dự án> — agenda họp SỐNG duy nhất với owner

## 0. Owner cần làm NGAY — tập con của §A, xếp theo số việc mở khoá (không ghi mục mới ở đây)
| # | Việc tay | Mở khoá (task ID + số mục) |
|---|---|---|

## A. Việc owner phải làm tay (env, quyền, thiết bị, dữ liệu, deploy)
| # | Việc | Vì sao cần | Task chờ |
|---|---|---|---|
| A1 | Đặt `GEO_AI_API_KEY` (key Anthropic) trong shell chạy đo, báo "có key" | đo thật 4 chỉ số §9 (PO-60, vùng cấm F7) | T-261126-05 |
| A2 | Xem / xóa DB rỗng `geo_e2e` ở cổng 54329 (checkout chính) | dọn rác agent e6 (PROGRESS 25/11) | — |

## B. Quyết định Claude đã tự chốt (owner OK / sửa)
| # | Quyết định | Lý do + nguồn (giờ raw) | Ảnh hưởng | Hoàn tác |
|---|---|---|---|---|
| B1 | Sửa skill delivery-lead: `install.sh` (`-d .git`→`-e`) và `check_project.py` (`isdir`→`exists`) | worktree có `.git` là TỆP → script định `git init` lồng (26/11 11:30) | chỉ 2 dòng trong ~/.claude/skills/delivery-lead/scripts | đổi lại 2 dòng |

## C. Câu chỉ owner trả lời được (gồm xác nhận brief; một danh sách, gửi MỘT lần; mỗi câu: mã + nghĩa + lựa chọn + Claude nghiêng gì)
| # | Câu hỏi | Lựa chọn → hệ quả | Mặc định đang dùng (TẠM) |
|---|---|---|---|
| C1 | Các câu chờ PO/BA còn mở từ trước: QF-E2-D10 (cận tham số 1000), Dev-E7-10 (BR-GEO-267 theo tên), D-GEO-184 (R3 số cắt cụt), NFR-GEO-056/D-GEO-125 (giới hạn tần suất); giả định PO-51…57 | OK → giữ; sửa → lô sửa sau MVP | giữ giả định hiện tại |
| C2 | Gói 46 đề HKN (`content/catalog-mvp.json`) tự nạp khi máy chủ khởi động ở dev **và prod** (`geo.content.pack.load-on-start`) | giữ tự nạp → đơn giản, mỗi lần khởi động kiểm lũy đẳng; chuyển sang lệnh nạp riêng → kiểm soát lúc nào dữ liệu vào prod. Claude nghiêng: lệnh nạp riêng cho prod | tự nạp BẬT (TẠM, mvp-content 26/11) |
| C3 | Engine chưa dựng được đề thiết diện / một số dạng thể tích chóp → các đề này không vào kho | bổ sung engine (lô sau MVP) / giữ ngoài MVP. Claude nghiêng: ngoài MVP | ngoài kho |

## Sau họp
Cập nhật KQ + bỏ TẠM → chạy lại audit → chuyển dòng đã chốt sang DECISIONS.md.

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo từ delivery-lead/templates/NEEDS.md | |
