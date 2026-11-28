Phiên bản: v1.0 · 2026-11-27

# S3-L6 · Báo cáo kiểm thử chặng 3 (T-261126-33)
Nhánh `t33-s3-l6` (tách từ wave0 18f68d6) · Tester L6 · không sửa code sản phẩm · AI giả lập (D-GEO-047).

## 1. Hai lượt `make e2e` (toàn bộ MVP + S2 + S3, 6 dự án Playwright)
| Lượt | Run id | Đạt / tổng test | Trượt | Bỏ qua | TC đạt / TC có test | Mã thoát |
|---|---|---|---|---|---|---|
| 1 | t33-r1 | 1846 / 1851 | 1 (TC-GEO-0307 ui-webkit) | 4 | 935 / 936 | 2 |
| 2 | t33-r2 | 1844 / 1851 | 3 (TC-GEO-0532+0537 mobile-iphone; TC-GEO-0606 mobile-iphone, quá giờ; TC-S2E8-005 ui-chromium) | 4 | 932 / 936 | 2 |

Bỏ qua (cả hai lượt, như nhau, có chủ ý trong spec): ui-webkit TC-GEO-0302 (lăn chuột, WebKit không hỗ trợ), 3 ca theme/icon/theme ở ui-webkit (không gắn mã TC).

**Phân loại trượt:** chạy lại riêng cả 5 TC trượt (run `t33-rerun`, `GEO_E2E_SKIP_BUILD=1 scripts/e2e.sh -g '@TC-GEO-0307|@TC-GEO-0532|@TC-GEO-0537|@TC-GEO-0606|@TC-S2E8-005'`): 15/15 test đạt. Hai lượt trượt ở các ca KHÁC nhau và không ca nào trượt lặp lại => chập chờn (tải máy/thời gian), không phải lỗi thật. Không có ca trượt thật. Không ca nào thuộc S3 trượt; 0804 và 0494 (chập chờn đã biết) đạt cả hai lượt.
Lưu ý cổng: lệnh `make e2e && make e2e` trả mã thoát 2 ở CẢ hai lượt vì ca chập chờn trên (không có retry); chuỗi lệnh "Xong khi" không chạy hết theo nghĩa đen. Kết quả thực chất xem mục 5.

## 2. Kiểm thử đơn vị / tích hợp (ca IT/UT không nằm trong Playwright)
`make verify` (build + lint + UT + quét SQL + kiến trúc + IT, geo_test): UT 304 (1 bỏ qua), IT 251 / 251 đạt, BUILD SUCCESS. Các IT S3: ClassS3E1IT 3, AssignmentSetS3E2IT 3, ClassBoardS3E3IT 5, NotificationS3E4IT 3, S3E5 (AccountDeletion 2, AllowedFields 6, AuditEvent 4, AuditFailure 1, PrivacyAi 3, SecurityDefinerGrants 2), ClassResultsS3E6IT 4, ReminderS3E6IT 14.

## 3. Bảng theo epic S3 (truy vết `--all --run t33-r2`, đã gộp kết quả IT của make verify)
| Epic | Tổng TC | Tự động (bắt buộc) | Đạt | Trượt | Manual | Không đạt thật |
|---|---|---|---|---|---|---|
| S3-E1 Lớp, mã lớp, duyệt, đặt lại mật khẩu, quyền quan hệ | 56 | 56 | 56 | 0 | 0 | 0 |
| S3-E2 Bộ bài, giao, giờ mở | 45 | 44 | 44 | 0 | 1 | 0 |
| S3-E3 Bảng lớp × bài | 57 | 56 | 56 | 0 | 1 | 0 |
| S3-E4 Thông báo | 41 | 38 | 38 | 0 | 3 | 0 |
| S3-E5 Riêng tư, trường dữ liệu, audit, xóa tài khoản | 60 | 59 | 59 | 0 | 1 | 0 |
| S3-E6 Kết quả lớp, nhắc việc | 40 | 39 | 39 | 0 | 1 | 0 |
| Cộng | 299 | 292 | 292 | 0 | 7 | 0 |

Không có TC S3 nào "bỏ qua"; 0 TC bắt buộc thiếu test. (Truy vết lượt chỉ đọc kết quả Playwright của một run: các TC S3 là API/UI nên có trong cả r1 và r2.)

## 4. Ca quyền theo quan hệ và trường dữ liệu cá nhân (đều đạt cả hai lượt + IT)
- Học sinh lớp khác / chưa được duyệt: S3E1-28, 47, 48, 49, 51, 52 (Chờ duyệt, Đã rời, Bị từ chối không thấy bài lớp; đổi mã lớp/mã học sinh trên URL; con trỏ phân trang lớp khác); S3E2-11, 12 (chọn lớp / đề của giáo viên khác).
- Giáo viên khác: S3E1-29 (duyệt lớp người khác), 38 (đặt lại mật khẩu em ngoài lớp), 39-42, 46, 50; hai giáo viên cùng em: 28, 37.
- Học sinh gọi API giáo viên: S3E1-09, 23, 32, 41, 50.
- Trường dữ liệu cá nhân: S3E5-04 (bản ghi chỉ có trường được phép), 27-31 (danh sách trường cho phép, build đỏ khi thêm trường, đăng ký thừa trường bị từ chối, tên trường cấm mọi dạng), 39 (mã kỹ thuật không lộ); IT AllowedFieldsS3E5IT 6/6, AllowedFieldsTest, PrivacyAiS3E5IT 3/3 (không gửi định danh học sinh cho AI).
Không phát hiện lộ trường dữ liệu cá nhân hay vượt quyền theo quan hệ.

## 5. Truy vết (`make traceability ARGS="--all --run t33-r2"`, sau make verify)
Mã thoát 0; tệp `traceability-all.txt`. TC "chưa chạy" = 0 (trước make verify có 39 TC S3 loại IT/UT ở trạng thái chưa chạy; sau make verify đều đạt). Bắt buộc chưa có test = 0 ở mọi epic: lệch audit/truy vết = 0. Còn 4 TC "trượt" trong bảng toàn cục (không thuộc S3): TC-GEO-0532, 0537, 0606, TC-S2E8-005 = đúng 4 ca chập chờn của lượt t33-r2, đã đạt khi chạy lại (mục 1).

## 6. TC manual còn lại (7, cần thiết bị thật, chưa chạy trong lượt này)
TC-S3E2-37 (bộ bài iPhone dọc), TC-S3E3-07 (ô có chữ + biểu tượng, không chỉ màu), TC-S3E4-02 (một cột trên điện thoại), TC-S3E4-21 (mất mạng, nút tắt), TC-S3E4-27 (hiển thị điện thoại dọc), TC-S3E5-26 (thông báo trên iPad/iPhone), TC-S3E6-10 (mất mạng khi bấm). Khuyến nghị owner duyệt tay trên iPhone/iPad thật ở L7.

## 7. Ca trượt thật
Không có.

## 8. Kết luận cho gate T-34
Kỹ thuật: ĐẠT. 292/292 TC tự động của S3 đạt, 7 manual chờ thiết bị thật, quyền theo quan hệ và trường dữ liệu đã phủ, truy vết không lệch. Rủi ro còn lại: (a) bộ e2e có ca chập chờn rải rác (mỗi lượt 1-3 ca khác nhau, chạy lại đạt; gợi ý bật retry=1 hoặc ghi vào inbox kiểm tra tải máy/ mobile-iphone), (b) 7 ca manual. Chưa đo chỉ số "giáo viên thật dùng lặp lại" (ngoài phạm vi tester).

## Bằng chứng
Lượt: `app/e2e/evidence/t33-r1`, `t33-r2`, `t33-rerun` (cục bộ, không commit). Nhẹ trong repo: `summary-*.json.gz`, `traceability-all.txt`, `it-s3.txt`.

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.0 | 2026-11-27 | Báo cáo đầu | Tester L6 (agent runner-t33) |
