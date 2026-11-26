Phiên bản: v0.2 · 2026-11-26 · GEO — agenda họp SỐNG duy nhất với owner

## 0. Owner cần làm NGAY — tập con của §A, xếp theo số việc mở khoá (không ghi mục mới ở đây)
| # | Việc tay | Mở khoá (task ID + số mục) |
|---|---|---|
| 1 | Chạy đo thật T-05 (lệnh ở PROGRESS.md, ~$3) | T-261126-05 → mở toàn bộ code chặng 2 (T-18…T-26, 9 task) |
| 2 | Họp gate T-16 một lần: §C (C1–C7) | T-17 PLAN_code → mọi task code chặng 2–3 |
| 3 | Tạo designer cho các chặng sau (owner đang làm, 26/11) | thiết kế chặng 4+ |

## A. Việc owner phải làm tay (env, quyền, thiết bị, dữ liệu, deploy)
| # | Việc | Vì sao cần | Task chờ |
|---|---|---|---|
| A1 | Đặt `GEO_AI_API_KEY` (key Anthropic) trong shell chạy đo, báo "có key" | đo thật 4 chỉ số §9 (PO-60, vùng cấm F7) | T-261126-05 |
| A2 | Xem / xóa DB rỗng `geo_e2e` ở cổng 54329 (checkout chính) | dọn rác agent e6 (PROGRESS 25/11) | — |

## B. Quyết định Claude đã tự chốt (owner OK / sửa)
| # | Quyết định | Lý do + nguồn (giờ raw) | Ảnh hưởng | Hoàn tác |
|---|---|---|---|---|
| B1 | Sửa skill delivery-lead: `install.sh` (`-d .git`→`-e`) và `check_project.py` (`isdir`→`exists`) | worktree có `.git` là TỆP → script định `git init` lồng (26/11 11:30) | chỉ 2 dòng trong ~/.claude/skills/delivery-lead/scripts | đổi lại 2 dòng |
| B2 | Lệnh kiểm T-05 trong plan: grep báo cáo provider `anthropic` + "Kết luận chỉ số bắt buộc: **ĐẠT**" (thay "B7: ĐẠT" không bao giờ khớp) | MvpBench.java:425–436 in đúng chuỗi này (26/11 14:30) | cổng B7 kiểm được bằng máy | sửa lại khối T-05 queue |
| B3 | W2 mỗi task spec 1 worktree riêng (s2-spec, s3-spec), sở hữu `epics/S2-*` / `epics/S3-*` — plan ghi chung s23-spec | 2 agent song song cùng worktree dễ ghi đè (26/11 14:58) | không | gộp lại 1 worktree |
| B4 | `Design/s23/shoot.js`: bỏ đường dẫn tuyệt đối máy owner → tương đối + `PW_CORE` | tính di động (26/11 14:55) | chỉ script chụp | đổi lại dòng 4 |

## C. Câu chỉ owner trả lời được (gồm xác nhận brief; một danh sách, gửi MỘT lần; mỗi câu: mã + nghĩa + lựa chọn + Claude nghiêng gì)
| # | Câu hỏi | Lựa chọn → hệ quả | Mặc định đang dùng (TẠM) |
|---|---|---|---|
| C1 | Các câu chờ PO/BA còn mở từ trước: QF-E2-D10 (cận tham số 1000), Dev-E7-10 (BR-GEO-267 theo tên), D-GEO-184 (R3 số cắt cụt), NFR-GEO-056/D-GEO-125 (giới hạn tần suất); giả định PO-51…57 | OK → giữ; sửa → lô sửa sau MVP | giữ giả định hiện tại |
| C2 | Gói 46 đề HKN (`content/catalog-mvp.json`) tự nạp khi máy chủ khởi động ở dev **và prod** (`geo.content.pack.load-on-start`) | giữ tự nạp → đơn giản, mỗi lần khởi động kiểm lũy đẳng; chuyển sang lệnh nạp riêng → kiểm soát lúc nào dữ liệu vào prod. Claude nghiêng: lệnh nạp riêng cho prod | tự nạp BẬT (TẠM, mvp-content 26/11) |
| C3 | Engine chưa dựng được đề thiết diện / một số dạng thể tích chóp → các đề này không vào kho | bổ sung engine (lô sau MVP) / giữ ngoài MVP. Claude nghiêng: ngoài MVP | ngoài kho |
| C4 | Q1–Q9 plan chặng 2–3 §5 (`.claude/plans/STAGE-2-3-PLAN.md`): Q1 code chờ B7 · Q2 cổng chặng 3 · Q3 không xác minh GV · Q4 OCR · Q5 S2-E4 · Q6 ngưỡng gộp · Q7 thiết diện · Q8 dữ liệu bài làm · Q9 soát Kho chung / web / tự học | OK cột Nghiêng → giữ; sửa câu nào → spec + PLAN đổi theo | cột Nghiêng (đã ghi PO-65 TẠM) |
| C5 | PO-62…65 (PO-61 thay một phần PO-05/18/21; gom Q1–Q9) — `PO-DECISIONS.md` | OK → bỏ TẠM; sửa → spec S2/S3 đổi | TẠM |
| C6 | DQ-17: học sinh tự học (không lớp) quên mật khẩu → không có đường khôi phục, phải tạo tài khoản mới | chấp nhận / cấp mã khôi phục cho cả học sinh tự học. Claude nghiêng: cấp mã khôi phục khi tự học (rẻ, giữ PO-61) | chấp nhận tạo mới |
| C7 | DQ-05: GV được duyệt lời giải chưa máy kiểm chứng (giữ nhãn "Cần xem lại") · DQ-22: "Cả lớp vướng ở đâu" chỉ hiện khi ≥ 5 bài · 25 DQ còn lại ở `Design/s23/DQ.md` | OK / sửa từng câu | mặc định trong DQ.md |
| C8 | Spec chặng 3 (T-14) — câu cần owner: (a) QA-S3E3-01/02 "bài làm" có tính em Đang làm không (ảnh hưởng ngưỡng 5 của "Cả lớp vướng"); (b) QA-S3E1-14, QA-S3E2-07 story map thiếu story quyền xuyên suốt S3 và vòng đời bộ bài Đã đóng/Lưu trữ; (c) QA-S3E5-07/10 thiếu story xóa tài khoản + chính sách lưu/xóa (PO-64), `org_id` kỹ thuật MVP có trái PO-61 không | OK mặc định / sửa → T-08 bổ sung story map | (a) tính; (b) thêm story; (c) thêm story xóa tài khoản, `org_id` giữ (kỹ thuật, không phải dữ liệu cá nhân) |

## Sau họp
Cập nhật KQ + bỏ TẠM → chạy lại audit → chuyển dòng đã chốt sang DECISIONS.md.

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.2 | 2026-11-26 | §0 xếp việc owner; B2–B4; C4–C7 cho họp gate T-16 | delivery-lead |
| v0.1 | 2026-11-26 | Tạo từ delivery-lead/templates/NEEDS.md | |
