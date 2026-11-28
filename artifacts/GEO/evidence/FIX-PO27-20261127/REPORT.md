Phiên bản: v1.0 · 2026-11-27 · runner-fix-po27 + runner-fix-po27-rv (phiên chính ghi tệp)

# FIX theo PO-DECISIONS 27/11 + câu mở sau FIX T-05

Nhánh `fix-po27` (7708ed8, 3e31db8, f7772dd), gộp worktree-mvp-wave0.

## Kết quả lệnh kiểm (lượt cuối, sau gộp wave0 0586365)
| Lệnh | Kết quả |
|---|---|
| `make verify` | ✅ 360 UT (1 skip) + 266 IT, 0 lỗi |
| web typecheck / lint / test | ✅ 0 lỗi / 0 lỗi / 101/101 |
| `make e2e-affected` | ✅ 1189 đạt, 0 trượt (gồm TC-GEO-0484) |
| `make traceability --all` | thiếu test 0 |

## Điểm PO → test
| Điểm | Thay đổi | Test |
|---|---|---|
| PO-72 | em vào lớp bằng tên xóa tài khoản chỉ gõ "XÓA", không tăng bộ đếm sai; `hasPassword` ở `/account/me` | e2e TC-S3E5-61, UT web |
| PO-74 | `userId` ở Row/Person/Evidence; `evidence?userId=`; trang bài làm `hs=<userId>` (hợp đồng API đổi) | IT, e2e UI TC-S3E3-58; TC-S3E3-29 sửa URL |
| PO-75 | gửi lại sau Bị từ chối/Đã rời tính hạn mức 10/15 phút, 30/24 giờ | e2e TC-S3E1-57 (mã -55 đã dùng) |
| PO-76 | ≤ 1 hàng "Từ chối"/giờ mỗi (người, loại đối tượng) cho `audit_log` và `relation_denial`, nguyên tử bằng `pg_advisory_xact_lock` + `INSERT … WHERE NOT EXISTS` | IT `RelationDenialRateIT`, IT song song AuditEvent, e2e TC-S3E5-09, TC-S3E4-23 |
| PO-71 | `auth_failure` hạn 1 giờ (V420), `AuthFailurePurgeJob` mỗi 10 phút | IT TC-S3E5-62 |
| PO-83 | trần chờ 10 s cả màn, độc lập `geo.ai.timeout`; prompt + `ai_purpose` class_stuck (V421) | IT TC-S3E3-60, UT prompt + `StuckDigest.parse` |
| PO-85 | lớp tắt AI không gọi bộ gộp, thẻ MSG-S3-98 | IT + UT web TC-S3E3-59 |
| PO-70 | MSG-S3-97 nguyên văn ở giao diện | e2e TC-S3E6-19 |
| PO-69, PO-68 | code đã khớp | UT; 4 IT (a)–(d) |
| PO-80 | AC US-GEO-606-SC8, "Hôm nay em chưa có bài cần làm." | TC-GEO-0877 |
| T-05 (1) | `SignatureUpgrade` tính lại chữ ký v1→v2 (V422, V423 tối ưu phân trang), chạy nền + advisory lock | IT `SignatureUpgradeT05IT` |
| T-05 (2) | mặt cầu nhận tâm theo tên đề (I/A), giữ cầu ngoại tiếp 4 điểm | UT `SphereCenterNameTest` |

## Soát độc lập
1 major (PO-76 thiếu `relation_denial`) + 5 minor — đã sửa hết ở f7772dd (DECISIONS 4281462).

## Còn mở
- TC-S3E5-62 đặt dưới US-S3E5-05 trong test_cases.md, nội dung thuộc US-S3E5-03-FC5 (BA dời).
- Hàng chữ ký v1 cũ giữ lại (geo_app không có DELETE) — dọn bằng migration khi lên môi trường thật.

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.0 | 2026-11-27 | Bản đầu | phiên chính ghi từ báo cáo agent |
