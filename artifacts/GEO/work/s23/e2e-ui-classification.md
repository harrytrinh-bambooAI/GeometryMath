Phiên bản: v1.1 · 2026-11-27

# Phân loại ca UI E2E: A (kiểm giao diện/hành động) và B (chỉ dữ liệu/nghiệp vụ) · DECISIONS 27/11 "Chiến lược E2E"

Nguồn: `app/e2e/tests/**/*.ui.spec.ts` (nhánh e2e-split, từ wave0 95806af, 27/11/2026), `app/e2e/playwright.config.ts`.

Quy tắc: **A** = hiển thị, ẩn-hiện, bố cục, khung 3D, di động P0, đúng chỗ bấm, a11y, nút khóa/mở, hộp xác nhận -> giữ Playwright UI. **B** = đã kiểm click ở ca A cùng màn, chỉ còn kiểm dữ liệu/nghiệp vụ (biến thể dữ liệu, biên, hệ quả phía máy chủ) -> chạy bằng gọi API đúng thứ tự bước, cuối luồng mở trang trạng thái cuối (không click) và chụp 1 ảnh (`support/final-state.ts`). Mã TC giữ nguyên trong tiêu đề.

## Tổng hợp
| Mục | Số |
|---|---|
| Test UI trước (tiêu đề test trong `*.ui.spec.ts`) | 564 |
| Đã chuyển sang API (B, `*.flow.api.spec.ts`, gắn `@loai-B`) | 37 (đợt 1: 26, đợt 2: 11) |
| Còn lại UI loại A | 526 (506 + 20 ca đổi B->A ở rà tay đợt 2) |
| B còn chạy UI | 1, để sau fix-po27: TC-S3E5-53 |

Ghi chú đợt 2 (E2E-SPLIT2, 27/11): đã RÀ TAY đủ 32 ca B theo heuristic (đọc thân từng ca): 11 chuyển sang API, 20 đổi sang A (cột "Lý do" ghi lý do mới; 6 trong đó là ca A giữ chỗ bấm cho ca B đợt 1 nên không thể là B), 1 để sau fix-po27. Phần 506 ca A còn lại vẫn theo heuristic đợt 1, chưa rà tay từng ca (TẠM). Ca B đã chuyển gắn `@loai-B` để truy vết; `make e2e-affected` khi diff chỉ server chỉ loại ca `@loai-B` ở project UI/mobile, không loại ca B chạy API.

### Ca đổi loại ở rà tay đợt 2 (B -> A)
| TC | Lý do |
|---|---|
| TC-GEO-0118 | kiểm toàn bộ nội dung trình duyệt nhận (phản hồi + HTML/JS/CSS đã tải), cần trình duyệt thật |
| TC-GEO-0410 | chụp ảnh + sửa ô chữ + Dựng hình trên iPhone |
| TC-GEO-0473 | ẩn-hiện nút bài giảng trong câu trả lời |
| TC-GEO-0539 | bấm "Tải thêm" với con trỏ hỏng, màn hiện lỗi |
| TC-GEO-0609 | bấm Giao, màn hiện lỗi MSG-42 tại chỗ |
| TC-GEO-0663 | đồng hồ thiết bị bị chỉnh (page.clock): hành vi phía trình duyệt |
| TC-GEO-0689 | trang đang mở tự hiện thông báo trong 10 giây |
| TC-GEO-0697 | nhịp hỏi lại trạng thái của trang (≤ 8 giây) |
| TC-S2E1-030 | trang chuyển về đăng nhập + thông báo hết phiên |
| TC-S2E3-026 | đóng tab, mở lại trang khôi phục tiến trình |
| TC-S2E7-005 | khung 3D: xoay, đo, thêm đường phụ |
| TC-S2E7-023 | mất mạng lúc trình chiếu, bấm tiếp/thoát |
| TC-S2E9-043 | ẩn-hiện nút "Em nghĩ đáp số này đúng" |
| TC-S3E5-16 | màn Đăng ký: tick riêng tư, nhãn phiên bản, bấm Đăng ký |
| TC-S3E1-05, 06, 45 | ca A giữ công tắc mã lớp / lỗi form Tạo lớp / nút Hủy cho ca B đợt 1 |
| TC-S3E2-01, 02 | ca A giữ thêm đề, lưu nháp, kéo thả cho ca B đợt 1 |
| TC-S3E6-17 | ca A giữ nút Bỏ lưu trữ + hộp xác nhận cho ca B đợt 1 |

## 1. Ca B đã chuyển sang API
| TC | Tệp mới (project api) | Ca A giữ chỗ bấm cùng màn |
|---|---|---|
| TC-GEO-0108 | `e1/student-join.flow.api.spec.ts` | TC-GEO-0100, 0104, 0107 |
| TC-GEO-0109 | `e1/student-join.flow.api.spec.ts` | TC-GEO-0100, 0104, 0107 |
| TC-GEO-0173 | `e1/student-join.flow.api.spec.ts` | TC-GEO-0100, 0104, 0107 |
| TC-S3E1-03 | `s3e1/classes.flow.api.spec.ts` | TC-S3E1-01, 04, 05, 06, 12, 17, 24, 25, 27, 30, 34, 45 |
| TC-S3E1-07 | `s3e1/classes.flow.api.spec.ts` | TC-S3E1-01, 04, 05, 06, 12, 17, 24, 25, 27, 30, 34, 45 |
| TC-S3E1-08 | `s3e1/classes.flow.api.spec.ts` | TC-S3E1-01, 04, 05, 06, 12, 17, 24, 25, 27, 30, 34, 45 |
| TC-S3E1-10 | `s3e1/classes.flow.api.spec.ts` | TC-S3E1-01, 04, 05, 06, 12, 17, 24, 25, 27, 30, 34, 45 |
| TC-S3E1-13 | `s3e1/classes.flow.api.spec.ts` | TC-S3E1-01, 04, 05, 06, 12, 17, 24, 25, 27, 30, 34, 45 |
| TC-S3E1-15 | `s3e1/classes.flow.api.spec.ts` | TC-S3E1-01, 04, 05, 06, 12, 17, 24, 25, 27, 30, 34, 45 |
| TC-S3E1-16 | `s3e1/classes.flow.api.spec.ts` | TC-S3E1-01, 04, 05, 06, 12, 17, 24, 25, 27, 30, 34, 45 |
| TC-S3E1-18 | `s3e1/classes.flow.api.spec.ts` | TC-S3E1-01, 04, 05, 06, 12, 17, 24, 25, 27, 30, 34, 45 |
| TC-S3E1-19 | `s3e1/classes.flow.api.spec.ts` | TC-S3E1-01, 04, 05, 06, 12, 17, 24, 25, 27, 30, 34, 45 |
| TC-S3E1-22 | `s3e1/classes.flow.api.spec.ts` | TC-S3E1-01, 04, 05, 06, 12, 17, 24, 25, 27, 30, 34, 45 |
| TC-S3E1-26 | `s3e1/classes.flow.api.spec.ts` | TC-S3E1-01, 04, 05, 06, 12, 17, 24, 25, 27, 30, 34, 45 |
| TC-S3E1-35 | `s3e1/classes.flow.api.spec.ts` | TC-S3E1-01, 04, 05, 06, 12, 17, 24, 25, 27, 30, 34, 45 |
| TC-S3E2-04 | `s3e2/compose.flow.api.spec.ts` | TC-S3E2-01, 02, 03, 10 |
| TC-S3E2-07 | `s3e2/compose.flow.api.spec.ts` | TC-S3E2-01, 02, 03, 10 |
| TC-S3E2-09 | `s3e2/compose.flow.api.spec.ts` | TC-S3E2-01, 02, 03, 10 |
| TC-S3E2-13 | `s3e2/compose.flow.api.spec.ts` | TC-S3E2-01, 02, 03, 10 |
| TC-S3E2-16 | `s3e2/schedule.flow.api.spec.ts` | TC-S3E2-15, 20, 21, 32 |
| TC-S3E2-22 | `s3e2/schedule.flow.api.spec.ts` | TC-S3E2-15, 20, 21, 32 |
| TC-S3E2-33 | `s3e2/schedule.flow.api.spec.ts` | TC-S3E2-15, 20, 21, 32 |
| TC-S3E2-41 | `s3e2/schedule.flow.api.spec.ts` | TC-S3E2-15, 20, 21, 32 |
| TC-S3E5-33 | `s3e5/account.flow.api.spec.ts` | TC-S3E5-53, 59, 55 |
| TC-S3E6-16 | `s3e6/archive.flow.api.spec.ts` | TC-S3E6-15, 17, 26 |
| TC-S3E6-18 | `s3e6/archive.flow.api.spec.ts` | TC-S3E6-15, 17, 26 |
| TC-GEO-0102 | `e1/student-join.flow.api.spec.ts` | TC-GEO-0100, 0101, 0104 |
| TC-GEO-0481 | `e4/inherit.flow.api.spec.ts` | TC-GEO-0434, 0435 |
| TC-GEO-0493 | `e4/qa.flow.api.spec.ts` | TC-GEO-0463 |
| TC-GEO-0507 | `e5/catalog.flow.api.spec.ts` | TC-GEO-0500 |
| TC-GEO-0516 | `e5/detail.flow.api.spec.ts` | TC-GEO-0515 |
| TC-GEO-0670 | `e6/lock.flow.api.spec.ts` | TC-GEO-0656 |
| TC-GEO-0703 | `e7/monitor.flow.api.spec.ts` | TC-GEO-0700, 0701, 0702 |
| TC-GEO-0720 | `e7/monitor.flow.api.spec.ts` | TC-GEO-0700, 0701, 0702 |
| TC-S2E8-107 | `s2e8/compose.flow.api.spec.ts` | TC-S2E8-105 |
| TC-S2E8-076 | `s2e8/result.flow.api.spec.ts` | TC-S2E8-075 |
| TC-S3E3-14 | `s3e3/board.flow.api.spec.ts` | TC-S3E3-04 (cùng tệp board.ui) |

## 2. Ca UI còn lại (một dòng mỗi test; loại A trừ khi ghi B chưa chuyển)
| Spec | Mã TC | A/B | Trạng thái | Lý do | Tiêu đề |
|---|---|---|---|---|---|
| e1/ai-gateway.ui.spec.ts | TC-GEO-0118 | A | giữ UI (A) · đổi B->A ở rà tay đợt 2 | kiểm toàn bộ nội dung trình duyệt nhận (phản hồi + HTML/JS/CSS đã tải) -> cần trình duyệt thật | test('Khóa của nhà cung cấp không xuống trình duyệt  async ({ page, re |
| e1/ai-gateway.ui.spec.ts | TC-GEO-0120 | A | giữ UI (A) | trạng thái trợ lý AI/gợi ý bận hiện ở giao diện | test('Nhà cung cấp trả lỗi  async ({ page, request }, testInfo) => { |
| e1/ai-gateway.ui.spec.ts | TC-GEO-0122 | A | giữ UI (A) | trạng thái trợ lý AI/gợi ý bận hiện ở giao diện | test('Bấm "Thử lại" sau lỗi  async ({ page, request }, testInfo) => { |
| e1/ai-gateway.ui.spec.ts | TC-GEO-0128 | A | giữ UI (A) | trạng thái trợ lý AI/gợi ý bận hiện ở giao diện | test('Học sinh gọi bài của lớp khác cùng trường  async ({ page, reques |
| e1/student-join.ui.spec.ts | TC-GEO-0100 | A | giữ UI (A) | màn Vào lớp: nhập, chuyển trang, phiên hết hạn · chạy thêm mobile-iphone/mobile-pixel | test('Vào lớp bằng tên và mã lớp đúng   async ({ page }) => { |
| e1/student-join.ui.spec.ts | TC-GEO-0101 | A | giữ UI (A) | màn Vào lớp: nhập, chuyển trang, phiên hết hạn · chạy thêm mobile-iphone/mobile-pixel | test('Tên chưa có trong lớp thì thành học sinh mới của lớp đó  async ( |
| e1/student-join.ui.spec.ts | TC-GEO-0103 | A | giữ UI (A) | màn Vào lớp: nhập, chuyển trang, phiên hết hạn · chạy thêm mobile-iphone/mobile-pixel | test('Mã lớp quyết định trường và lớp  async ({ page }) => { |
| e1/student-join.ui.spec.ts | TC-GEO-0104 | A | giữ UI (A) | màn Vào lớp: nhập, chuyển trang, phiên hết hạn · chạy thêm mobile-iphone/mobile-pixel | test('Màn "Vào lớp" chỉ có hai ô nhập  async ({ page }) => { |
| e1/student-join.ui.spec.ts | TC-GEO-0107 | A | giữ UI (A) | màn Vào lớp: nhập, chuyển trang, phiên hết hạn · chạy thêm mobile-iphone/mobile-pixel | test('Tên hiển thị rỗng  async ({ page }) => { |
| e1/student-join.ui.spec.ts | TC-GEO-0110 | A | giữ UI (A) | màn Vào lớp: nhập, chuyển trang, phiên hết hạn · chạy thêm mobile-iphone/mobile-pixel | test('Phiên hết hạn  async ({ page, request }) => { |
| e1/teacher-login.ui.spec.ts | TC-GEO-0111 | A | giữ UI (A) | màn đăng nhập giáo viên: lỗi, khóa nút, ẩn-hiện | test('Mã giáo viên đúng   async ({ page, request }, testInfo) => { |
| e1/teacher-login.ui.spec.ts | TC-GEO-0112 | A | giữ UI (A) | màn đăng nhập giáo viên: lỗi, khóa nút, ẩn-hiện | test('Vào được từ điện thoại  async ({ page, request }, testInfo) => { |
| e1/teacher-login.ui.spec.ts | TC-GEO-0113 | A | giữ UI (A) | màn đăng nhập giáo viên: lỗi, khóa nút, ẩn-hiện | test('Mã giáo viên sai   async ({ page }) => { |
| e1/teacher-login.ui.spec.ts | TC-GEO-0115 | A | giữ UI (A) | màn đăng nhập giáo viên: lỗi, khóa nút, ẩn-hiện | test('Không có công tắc đổi vai  async ({ page }) => { |
| e1/teacher-login.ui.spec.ts | TC-GEO-0174 | A | giữ UI (A) | màn đăng nhập giáo viên: lỗi, khóa nút, ẩn-hiện | test('Mỗi mã vào đúng tài khoản được cấp  async ({ page, request }, te |
| e1/teacher-login.ui.spec.ts | TC-GEO-0175 | A | giữ UI (A) | màn đăng nhập giáo viên: lỗi, khóa nút, ẩn-hiện | test('Mã đã thu hồi  async ({ page }) => { |
| e1/teacher-login.ui.spec.ts | TC-GEO-0124 | A | giữ UI (A) | màn đăng nhập giáo viên: lỗi, khóa nút, ẩn-hiện | test('Giáo viên chỉ thấy lớp mình dạy  async ({ page, request }) => { |
| e2/labels.ui.spec.ts | TC-GEO-0202 | A | giữ UI (A) | nhãn trên hình hiển thị đúng chỗ (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Thẻ ràng buộc hiện ở góc hình  async ({ page, request }) => { |
| e2/labels.ui.spec.ts | TC-GEO-0218 | A | giữ UI (A) | nhãn trên hình hiển thị đúng chỗ (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Một chuỗi nhãn trên mọi màn  async ({ page, request, browser },  |
| e2/labels.ui.spec.ts | TC-GEO-0222 | A | giữ UI (A) | nhãn trên hình hiển thị đúng chỗ (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Giáo viên không đặt được nhãn máy  async ({ page, request }) =>  |
| e2/self-study.ui.spec.ts | TC-GEO-0224 | A | giữ UI (A) | màn Tự học: nhập đề, hiển thị kết quả | test('Đề lạ có hình kèm cảnh báo  async ({ page, request }, testInfo)  |
| e2/self-study.ui.spec.ts | TC-GEO-0225 | A | giữ UI (A) | màn Tự học: nhập đề, hiển thị kết quả | test('Cảnh báo luôn hiện  async ({ page, request }, testInfo) => { |
| e2/self-study.ui.spec.ts | TC-GEO-0229 | A | giữ UI (A) | màn Tự học: nhập đề, hiển thị kết quả | test('Đề hình phẳng  async ({ page, request }, testInfo) => { |
| e2/templates.ui.spec.ts | TC-GEO-0208 | A | giữ UI (A) | chọn mẫu đề bằng bấm, hiển thị | test('Chọn hình mẫu và thấy hình ngay, không gọi AI  async ({ page, re |
| e2/templates.ui.spec.ts | TC-GEO-0209 | A | giữ UI (A) | chọn mẫu đề bằng bấm, hiển thị | test('Đổi tham số thì hình đổi theo  async ({ page, request }, testInf |
| e2/templates.ui.spec.ts | TC-GEO-0210 | A | giữ UI (A) | chọn mẫu đề bằng bấm, hiển thị | test('Nhận số thập phân dấu phẩy  async ({ page, request }, testInfo)  |
| e2/templates.ui.spec.ts | TC-GEO-0212 | A | giữ UI (A) | chọn mẫu đề bằng bấm, hiển thị | test('Tham số không hợp lệ  async ({ page, request }, testInfo) => { |
| e2/templates.ui.spec.ts | TC-GEO-0231 | A | giữ UI (A) | chọn mẫu đề bằng bấm, hiển thị | test('Sáu thẻ hình mẫu trên màn "Tự học"  async ({ page, request }, te |
| e2/templates.ui.spec.ts | TC-GEO-0211 | A | giữ UI (A) | chọn mẫu đề bằng bấm, hiển thị | test('Sáu hình mẫu đều dựng được  async ({ page, request, browser }, t |
| e3/draw.ui.spec.ts | TC-GEO-0310 | A | giữ UI (A) | khung 3D: vẽ hình, đường nét | test('Chạm chọn một cạnh  async ({ page, request }) => { |
| e3/draw.ui.spec.ts | TC-GEO-0311 | A | giữ UI (A) | khung 3D: vẽ hình, đường nét | test('Chạm chọn một mặt  async ({ page, request }) => { |
| e3/draw.ui.spec.ts | TC-GEO-0312 | A | giữ UI (A) | khung 3D: vẽ hình, đường nét | test('Chạm chọn một điểm  async ({ page, request }) => { |
| e3/draw.ui.spec.ts | TC-GEO-0315 | A | giữ UI (A) | khung 3D: vẽ hình, đường nét | test('Chạm ra ngoài hình  async ({ page, request }) => { |
| e3/draw.ui.spec.ts | TC-GEO-0316 | A | giữ UI (A) | khung 3D: vẽ hình, đường nét | test('Vẽ đoạn nối hai điểm  async ({ page, request }) => { |
| e3/draw.ui.spec.ts | TC-GEO-0317 | A | giữ UI (A) | khung 3D: vẽ hình, đường nét | test('Giao điểm của hai đường cắt nhau có tên  async ({ page, request  |
| e3/draw.ui.spec.ts | TC-GEO-0318 | A | giữ UI (A) | khung 3D: vẽ hình, đường nét | test('Dựng mặt phẳng qua ba điểm  async ({ page, request }) => { |
| e3/draw.ui.spec.ts | TC-GEO-0319 | A | giữ UI (A) | khung 3D: vẽ hình, đường nét | test('Giao tuyến của mặt phẳng mới với mặt của hình  async ({ page, re |
| e3/draw.ui.spec.ts | TC-GEO-0320 | A | giữ UI (A) | khung 3D: vẽ hình, đường nét | test('Mở rộng một mặt  async ({ page, request }) => { |
| e3/draw.ui.spec.ts | TC-GEO-0321 | A | giữ UI (A) | khung 3D: vẽ hình, đường nét | test('Kéo dài một cạnh  async ({ page, request }) => { |
| e3/draw.ui.spec.ts | TC-GEO-0322 | A | giữ UI (A) | khung 3D: vẽ hình, đường nét | test('Hai đường chéo nhau  async ({ page, request }) => { |
| e3/draw.ui.spec.ts | TC-GEO-0323 | A | giữ UI (A) | khung 3D: vẽ hình, đường nét | test('Ba điểm thẳng hàng  async ({ page, request }) => { |
| e3/draw.ui.spec.ts | TC-GEO-0324 | A | giữ UI (A) | khung 3D: vẽ hình, đường nét | test('Không có điều khiển kéo mặt phẳng  async ({ page, request }) =>  |
| e3/events.ui.spec.ts | TC-GEO-0309 | A | giữ UI (A) | khung 3D: sự kiện cảm ứng/chuột | test('Xoay và thu phóng không tính là thao tác trên hình  async ({ pag |
| e3/fixes.ui.spec.ts | TC-GEO-0347 | A | giữ UI (A) | khung 3D: sửa lỗi hiển thị | test('Tên mặt không lặp chữ  async ({ page, request }) => { |
| e3/fixes.ui.spec.ts | TC-GEO-0348 | A | giữ UI (A) | khung 3D: sửa lỗi hiển thị | test('Mặt bốn đỉnh tô kín  async ({ page, request }, testInfo) => { |
| e3/fixes.ui.spec.ts | TC-GEO-0349 | A | giữ UI (A) | khung 3D: sửa lỗi hiển thị | test('Hai đường chéo nhau trên hình lập phương  async ({ page, request |
| e3/fixes.ui.spec.ts | TC-GEO-0350 | A | giữ UI (A) | khung 3D: sửa lỗi hiển thị | test('Mặt lõm  async ({ page, request }, testInfo) => { |
| e3/measure.ui.spec.ts | TC-GEO-0331 | A | giữ UI (A) | khung 3D: đo, nhãn số đo | test('Đo độ dài một yếu tố trung gian khi đang khóa  async ({ page, re |
| e3/measure.ui.spec.ts | TC-GEO-0332 | A | giữ UI (A) | khung 3D: đo, nhãn số đo | test('Đo một góc trung gian khi đang khóa  async ({ page, request }) = |
| e3/measure.ui.spec.ts | TC-GEO-0333 | A | giữ UI (A) | khung 3D: đo, nhãn số đo | test('Đo khoảng cách từ điểm tới mặt  async ({ page, request }) => { |
| e3/measure.ui.spec.ts | TC-GEO-0334 | A | giữ UI (A) | khung 3D: đo, nhãn số đo | test('Sau giờ mở, đại lượng đề hỏi đo được  async ({ page, request },  |
| e3/measure.ui.spec.ts | TC-GEO-0335 | A | giữ UI (A) | khung 3D: đo, nhãn số đo | test('Số đo dùng dấu phẩy  async ({ page, request }) => { |
| e3/measure.ui.spec.ts | TC-GEO-0336 | A | giữ UI (A) | khung 3D: đo, nhãn số đo | test('Đo đại lượng đề hỏi khi đang khóa  async ({ page, request }) =>  |
| e3/measure.ui.spec.ts | TC-GEO-0337 | A | giữ UI (A) | khung 3D: đo, nhãn số đo | test('Đo đại lượng cùng vai trò khi đang khóa  async ({ page, request  |
| e3/measure.ui.spec.ts | TC-GEO-0338 | A | giữ UI (A) | khung 3D: đo, nhãn số đo | test('Bài Chưa đặt giờ cũng ẩn  async ({ page, request }) => { |
| e3/measure.ui.spec.ts | TC-GEO-0339 | A | giữ UI (A) | khung 3D: đo, nhãn số đo | test('Bài tự học đang khóa cũng ẩn  async ({ page, request }) => { |
| e3/measure.ui.spec.ts | TC-GEO-0352 | A | giữ UI (A) | khung 3D: đo, nhãn số đo | test('Bài chuyển Đã mở khi đang mở sẵn công cụ đo  async ({ page, requ |
| e3/points.ui.spec.ts | TC-GEO-0325 | A | giữ UI (A) | khung 3D: điểm, chọn điểm | test('Thêm trung điểm  async ({ page, request }) => { |
| e3/points.ui.spec.ts | TC-GEO-0326 | A | giữ UI (A) | khung 3D: điểm, chọn điểm | test('Thêm điểm chia tỉ lệ  async ({ page, request }) => { |
| e3/points.ui.spec.ts | TC-GEO-0327 | A | giữ UI (A) | khung 3D: điểm, chọn điểm | test('Thêm điểm tự do trên cạnh  async ({ page, request }) => { |
| e3/points.ui.spec.ts | TC-GEO-0328 | A | giữ UI (A) | khung 3D: điểm, chọn điểm | test('Điểm mới dùng được cho công cụ khác  async ({ page, request }) = |
| e3/points.ui.spec.ts | TC-GEO-0329 | A | giữ UI (A) | khung 3D: điểm, chọn điểm | test('Tỉ lệ không hợp lệ  async ({ page, request }) => { |
| e3/points.ui.spec.ts | TC-GEO-0330 | A | giữ UI (A) | khung 3D: điểm, chọn điểm | test('Điểm trùng điểm đã có  async ({ page, request }) => { |
| e3/view.ui.spec.ts | TC-GEO-0300 | A | giữ UI (A) | khung 3D: góc nhìn, xoay, thu phóng | test('Kéo để xoay  async ({ page, request }) => { |
| e3/view.ui.spec.ts | TC-GEO-0302 | A | giữ UI (A) | khung 3D: góc nhìn, xoay, thu phóng | test('Lăn chuột để thu phóng trên máy tính  async ({ page, request },  |
| e3/view.ui.spec.ts | TC-GEO-0303 | A | giữ UI (A) | khung 3D: góc nhìn, xoay, thu phóng | test('"Nhìn từ trên"   async ({ page, request }) => { |
| e3/view.ui.spec.ts | TC-GEO-0304 | A | giữ UI (A) | khung 3D: góc nhìn, xoay, thu phóng | test('"Nhìn từ trước"  async ({ page, request }) => { |
| e3/view.ui.spec.ts | TC-GEO-0305 | A | giữ UI (A) | khung 3D: góc nhìn, xoay, thu phóng | test('"Đặt lại" về góc nhìn ban đầu  async ({ page, request }) => { |
| e3/view.ui.spec.ts | TC-GEO-0306 | A | giữ UI (A) | khung 3D: góc nhìn, xoay, thu phóng | test('Cạnh khuất vẽ nét đứt và tính lại khi xoay  async ({ page, reque |
| e3/view.ui.spec.ts | TC-GEO-0307 | A | giữ UI (A) | khung 3D: góc nhìn, xoay, thu phóng | test('Bật, tắt trục Oxyz  async ({ page, request }) => { |
| e3/view.ui.spec.ts | TC-GEO-0308 | A | giữ UI (A) | khung 3D: góc nhìn, xoay, thu phóng | test('Thu phóng có giới hạn  async ({ page, request }) => { |
| e3/view.ui.spec.ts | TC-GEO-0353 | A | giữ UI (A) | khung 3D: góc nhìn, xoay, thu phóng | test('"Xóa hết" xóa phần vẽ thêm, giữ hình gốc và góc nhìn  async ({ p |
| e4/hints.ui.spec.ts | TC-GEO-0419 | A | giữ UI (A) | hiển thị gợi ý theo bậc, ẩn-hiện · chạy thêm mobile-iphone/mobile-pixel | test('Xem Gợi ý 1   async ({ page, request }) => { |
| e4/hints.ui.spec.ts | TC-GEO-0420 | A | giữ UI (A) | hiển thị gợi ý theo bậc, ẩn-hiện · chạy thêm mobile-iphone/mobile-pixel | test('Xem Gợi ý 2  async ({ page, request }) => { |
| e4/hints.ui.spec.ts | TC-GEO-0421 | A | giữ UI (A) | hiển thị gợi ý theo bậc, ẩn-hiện · chạy thêm mobile-iphone/mobile-pixel | test('Giáo viên thấy mức gợi ý vừa dùng  async ({ page, request }) =>  |
| e4/hints.ui.spec.ts | TC-GEO-0423 | A | giữ UI (A) | hiển thị gợi ý theo bậc, ẩn-hiện · chạy thêm mobile-iphone/mobile-pixel | test('Gợi ý trên bài tự học (gợi ý do AI sinh)  async ({ page, request |
| e4/hints.ui.spec.ts | TC-GEO-0424 | A | giữ UI (A) | hiển thị gợi ý theo bậc, ẩn-hiện · chạy thêm mobile-iphone/mobile-pixel | test('Xin Gợi ý 2 khi chưa xem Gợi ý 1  async ({ page, request }) => { |
| e4/hints.ui.spec.ts | TC-GEO-0425 | A | giữ UI (A) | hiển thị gợi ý theo bậc, ẩn-hiện · chạy thêm mobile-iphone/mobile-pixel | test('Giáo viên đã tắt gợi ý  async ({ page, request }) => { |
| e4/hints.ui.spec.ts | TC-GEO-0427 | A | giữ UI (A) | hiển thị gợi ý theo bậc, ẩn-hiện · chạy thêm mobile-iphone/mobile-pixel | test('Mất mạng lúc bấm xem gợi ý  async ({ page, request, context }) = |
| e4/hints.ui.spec.ts | TC-GEO-0428 | A | giữ UI (A) | hiển thị gợi ý theo bậc, ẩn-hiện · chạy thêm mobile-iphone/mobile-pixel | test('Đủ hai gợi ý thì mở được lời giải tự học  async ({ page, request |
| e4/hints.ui.spec.ts | TC-GEO-0430 | A | giữ UI (A) | hiển thị gợi ý theo bậc, ẩn-hiện · chạy thêm mobile-iphone/mobile-pixel | test('Bài tự học chưa xem gợi ý nào: tab Lời giải khóa  async ({ page, |
| e4/inherit.ui.spec.ts | TC-GEO-0434 | A | giữ UI (A) | kế thừa trạng thái gợi ý khi mở lại | test('Dán lại đề đang khóa thì bị khóa theo bài giao  async ({ page, r |
| e4/inherit.ui.spec.ts | TC-GEO-0435 | A | giữ UI (A) | kế thừa trạng thái gợi ý khi mở lại | test('Bài giao mở thì bài tự học kế thừa cũng mở  async ({ page, reque |
| e4/inherit.ui.spec.ts | TC-GEO-0482 | A | giữ UI (A) | kế thừa trạng thái gợi ý khi mở lại | test('Đề viết lại với cạnh 2a vẫn kế thừa  async ({ page, request }) = |
| e4/inherit.ui.spec.ts | TC-GEO-0483 | A | giữ UI (A) | kế thừa trạng thái gợi ý khi mở lại | test('Đề viết lại câu chữ, thêm chữ vẫn kế thừa khóa  async ({ page, r |
| e4/inherit.ui.spec.ts | TC-GEO-0484 | A | giữ UI (A) | kế thừa trạng thái gợi ý khi mở lại | test('Cùng hình nhưng hỏi đại lượng khác thì không kế thừa khóa  async |
| e4/inherit.ui.spec.ts | TC-GEO-0489 | A | giữ UI (A) | kế thừa trạng thái gợi ý khi mở lại | test('Cùng hình nhưng khác tham số thì không kế thừa  async ({ page, r |
| e4/inherit.ui.spec.ts | TC-GEO-0485 | A | giữ UI (A) | kế thừa trạng thái gợi ý khi mở lại | test('Dán đề trước, giáo viên giao sau: lần xin kế tiếp bị khóa  async |
| e4/inherit.ui.spec.ts | TC-GEO-0486 | A | giữ UI (A) | kế thừa trạng thái gợi ý khi mở lại | test('Đã xem lời giải tự học, sau đó giáo viên giao đúng đề: xin lại t |
| e4/inherit.ui.spec.ts | TC-GEO-0492 | A | giữ UI (A) | kế thừa trạng thái gợi ý khi mở lại | test('Đề không dựng được bằng hình mẫu không có lời giải tự học  async |
| e4/qa.ui.spec.ts | TC-GEO-0454 | A | giữ UI (A) | hỏi đáp: ô nhập, hiển thị trả lời (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Câu trả lời có nguồn "Giáo án mẫu"  async ({ page, request }) => |
| e4/qa.ui.spec.ts | TC-GEO-0455 | A | giữ UI (A) | hỏi đáp: ô nhập, hiển thị trả lời (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Câu trả lời có nguồn "AI soạn"  async ({ page, request }) => { |
| e4/qa.ui.spec.ts | TC-GEO-0456 | A | giữ UI (A) | hỏi đáp: ô nhập, hiển thị trả lời (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Câu hỏi ngoài giáo trình  async ({ page, request }) => { |
| e4/qa.ui.spec.ts | TC-GEO-0457 | A | giữ UI (A) | hỏi đáp: ô nhập, hiển thị trả lời (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Màn chờ khi trợ lý đang trả lời  async ({ page, request }) => { |
| e4/qa.ui.spec.ts | TC-GEO-0459 | A | giữ UI (A) | hỏi đáp: ô nhập, hiển thị trả lời (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Nguồn nêu ra luôn mở được  async ({ page, request }) => { |
| e4/qa.ui.spec.ts | TC-GEO-0460 | A | giữ UI (A) | hỏi đáp: ô nhập, hiển thị trả lời (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Trợ lý lỗi khi hỏi  async ({ page, request }) => { |
| e4/qa.ui.spec.ts | TC-GEO-0462 | A | giữ UI (A) | hỏi đáp: ô nhập, hiển thị trả lời (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Giáo viên đã tắt hỏi đáp  async ({ page, request }) => { |
| e4/qa.ui.spec.ts | TC-GEO-0463 | A | giữ UI (A) | hỏi đáp: ô nhập, hiển thị trả lời (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Xem nhật ký của mình  async ({ page, request }) => { |
| e4/qa.ui.spec.ts | TC-GEO-0464 | A | giữ UI (A) | hỏi đáp: ô nhập, hiển thị trả lời (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Lọc nhật ký theo bài  async ({ page, request }) => { |
| e4/qa.ui.spec.ts | TC-GEO-0465 | A | giữ UI (A) | hỏi đáp: ô nhập, hiển thị trả lời (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Mở lại câu trả lời đã lưu, không gọi AI lại  async ({ page, requ |
| e4/qa.ui.spec.ts | TC-GEO-0467 | A | giữ UI (A) | hỏi đáp: ô nhập, hiển thị trả lời (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Bài chưa có câu hỏi  async ({ page, request }) => { |
| e4/qa.ui.spec.ts | TC-GEO-0469 | A | giữ UI (A) | hỏi đáp: ô nhập, hiển thị trả lời (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Bấm từ khóa trong gợi ý  async ({ page, request }) => { |
| e4/qa.ui.spec.ts | TC-GEO-0470 | A | giữ UI (A) | hỏi đáp: ô nhập, hiển thị trả lời (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Bài giảng do AI viết mang nhãn chờ duyệt  async ({ page, request |
| e4/qa.ui.spec.ts | TC-GEO-0471 | A | giữ UI (A) | hỏi đáp: ô nhập, hiển thị trả lời (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Từ khóa trong lời giải  async ({ page, request }) => { |
| e4/qa.ui.spec.ts | TC-GEO-0472 | A | giữ UI (A) | hỏi đáp: ô nhập, hiển thị trả lời (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Đóng hộp bài giảng: về đúng chỗ, điểm M và tiến độ không đổi  as |
| e4/qa.ui.spec.ts | TC-GEO-0473 | A | giữ UI (A) · đổi B->A ở rà tay đợt 2 | ẩn-hiện nút bài giảng trong câu trả lời trên màn | test('Cụm từ không có bài giảng  async ({ page, request }) => { |
| e4/qa.ui.spec.ts | TC-GEO-0475 | A | giữ UI (A) | hỏi đáp: ô nhập, hiển thị trả lời (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Không dùng nhãn "Giáo viên đã duyệt" cho bảy bài Giáo án mẫu  as |
| e4/qa.ui.spec.ts | TC-GEO-0494 | A | giữ UI (A) | hỏi đáp: ô nhập, hiển thị trả lời (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Cờ chờ soát hiện cho học sinh và giáo viên  async ({ page, reque |
| e4/selfstudy.ui.spec.ts | TC-GEO-0400 | A | giữ UI (A) | Tự học: nhập, hiển thị | test('Dán đề và nhận hình  async ({ page, request }) => { |
| e4/selfstudy.ui.spec.ts | TC-GEO-0401 | A | giữ UI (A) | Tự học: nhập, hiển thị | test('Màn chờ rõ ràng khi trợ lý trả lời chậm  async ({ page, request  |
| e4/selfstudy.ui.spec.ts | TC-GEO-0404 | A | giữ UI (A) | Tự học: nhập, hiển thị | test('Đề rỗng  async ({ page, request }) => { |
| e4/selfstudy.ui.spec.ts | TC-GEO-0406 | A | giữ UI (A) | Tự học: nhập, hiển thị | test('Trợ lý lỗi khi dựng hình  async ({ page, request }) => { |
| e4/selfstudy.ui.spec.ts | TC-GEO-0409 | A | giữ UI (A) | Tự học: nhập, hiển thị | test('Ảnh rõ được đọc thành chữ để sửa (iPhone)  async ({ browser, req |
| e4/selfstudy.ui.spec.ts | TC-GEO-0410 | A | giữ UI (A) · đổi B->A ở rà tay đợt 2 | chụp ảnh + sửa ô chữ + Dựng hình trên iPhone (hành động nhập trên điện thoại) | test('Sửa chữ đọc từ ảnh rồi dựng hình  async ({ browser, request }) = |
| e4/selfstudy.ui.spec.ts | TC-GEO-0415 | A | giữ UI (A) | Tự học: nhập, hiển thị | test('Vào màn khám phá từ "Tự khám phá" của Bài của em  async ({ page, |
| e4/selfstudy.ui.spec.ts | TC-GEO-0416 | A | giữ UI (A) | Tự học: nhập, hiển thị | test('"Mở chế độ tự học"  async ({ page, request }) => { |
| e4/selfstudy.ui.spec.ts | TC-GEO-0417 | A | giữ UI (A) | Tự học: nhập, hiển thị | test('Màn khám phá không có gợi ý, lời giải, hỏi đáp  async ({ page, r |
| e4/solution.ui.spec.ts | TC-GEO-0441 | A | giữ UI (A) | lời giải khóa/mở, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Bấm một bước, hình tô đúng đối tượng  async ({ page, request })  |
| e4/solution.ui.spec.ts | TC-GEO-0442 | A | giữ UI (A) | lời giải khóa/mở, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Đổi bước thì đổi phần tô  async ({ page, request }) => { |
| e4/solution.ui.spec.ts | TC-GEO-0443 | A | giữ UI (A) | lời giải khóa/mở, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('"Bước sau", "Bước trước"   async ({ page, request }) => { |
| e4/solution.ui.spec.ts | TC-GEO-0444 | A | giữ UI (A) | lời giải khóa/mở, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Nhãn của lời giải  async ({ page, request }) => { |
| e4/solution.ui.spec.ts | TC-GEO-0446 | A | giữ UI (A) | lời giải khóa/mở, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Lời giải đồng bộ hình trên iPhone  async ({ browser, request })  |
| e4/solution.ui.spec.ts | TC-GEO-0447 | A | giữ UI (A) | lời giải khóa/mở, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Bước không có đối tượng để tô  async ({ page, request }) => { |
| e4/solution.ui.spec.ts | TC-GEO-0448 | A | giữ UI (A) | lời giải khóa/mở, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Chuyển bước nhanh  async ({ page, request }) => { |
| e4/solution.ui.spec.ts | TC-GEO-0449 | A | giữ UI (A) | lời giải khóa/mở, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Phần vẽ thêm hiện dần theo bước  async ({ page, request }) => { |
| e4/solution.ui.spec.ts | TC-GEO-0450 | A | giữ UI (A) | lời giải khóa/mở, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('"Vì sao nghĩ ra bước này?"  async ({ page, request }) => { |
| e4/solution.ui.spec.ts | TC-GEO-0451 | A | giữ UI (A) | lời giải khóa/mở, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Quay lại bước trước thì phần vẽ thêm bớt đi  async ({ page, requ |
| e4/solution.ui.spec.ts | TC-GEO-0452 | A | giữ UI (A) | lời giải khóa/mở, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Nhảy thẳng tới bước cuối  async ({ page, request }) => { |
| e4/solution.ui.spec.ts | TC-GEO-0453 | A | giữ UI (A) | lời giải khóa/mở, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Bước không có phần vẽ thêm  async ({ page, request }) => { |
| e5/catalog.ui.spec.ts | TC-GEO-0500 | A | giữ UI (A) | Kho đề: lọc, danh sách, ẩn-hiện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Mở Kho đề: dòng đếm, 20 thẻ, Tải thêm, lọc ở Tất cả   async ({ p |
| e5/catalog.ui.spec.ts | TC-GEO-0501 | A | giữ UI (A) | Kho đề: lọc, danh sách, ẩn-hiện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Lọc Cấp THPT và Dạng bài Góc: 8 đề có HKG-01   async ({ page, re |
| e5/catalog.ui.spec.ts | TC-GEO-0502 | A | giữ UI (A) | Kho đề: lọc, danh sách, ẩn-hiện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Lọc theo hình chóp: 8 đề  async ({ page, request }) => { |
| e5/catalog.ui.spec.ts | TC-GEO-0503 | A | giữ UI (A) | Kho đề: lọc, danh sách, ẩn-hiện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Dòng đếm không đổi theo bộ lọc; bỏ lọc trở lại 20 thẻ  async ({  |
| e5/catalog.ui.spec.ts | TC-GEO-0504, TC-GEO-0510 | A | giữ UI (A) | Kho đề: lọc, danh sách, ẩn-hiện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Nội dung thẻ HKG-01   async ({ page, request }) => { |
| e5/catalog.ui.spec.ts | TC-GEO-0505 | A | giữ UI (A) | Kho đề: lọc, danh sách, ẩn-hiện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Kho đề trên điện thoại: một cột, lọc THCS, Giao bài bấm được  as |
| e5/catalog.ui.spec.ts | TC-GEO-0506 | A | giữ UI (A) | Kho đề: lọc, danh sách, ẩn-hiện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Lọc không có kết quả: THCS + Mặt cầu  async ({ page, request })  |
| e5/catalog.ui.spec.ts | TC-GEO-0508 | A | giữ UI (A) | Kho đề: lọc, danh sách, ẩn-hiện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Không có tìm kiếm, Thêm đề, soạn, import, Thiết diện  async ({ p |
| e5/catalog.ui.spec.ts | TC-GEO-0511 | A | giữ UI (A) | Kho đề: lọc, danh sách, ẩn-hiện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Đề cần xem lại: nhãn khác màu, vẫn AI soạn, Giao bài bấm được  a |
| e5/catalog.ui.spec.ts | TC-GEO-0512 | A | giữ UI (A) | Kho đề: lọc, danh sách, ẩn-hiện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Giáo viên C: kho và lời giải đứng tên Giáo viên mẫu  async ({ pa |
| e5/catalog.ui.spec.ts | TC-GEO-0514 | A | giữ UI (A) | Kho đề: lọc, danh sách, ẩn-hiện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Không có "Giáo viên đã duyệt" trên 48 thẻ và trang chi tiết  asy |
| e5/catalog.ui.spec.ts | TC-GEO-0532, TC-GEO-0537 | A | giữ UI (A) | Kho đề: lọc, danh sách, ẩn-hiện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Tải lần lượt hết kho: 20, 20, 8; mỗi mã đúng một lần   async ({  |
| e5/catalog.ui.spec.ts | TC-GEO-0538 | A | giữ UI (A) | Kho đề: lọc, danh sách, ẩn-hiện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Đổi bộ lọc giữa chừng: làm lại từ đầu, 20 đề THCS  async ({ page |
| e5/catalog.ui.spec.ts | TC-GEO-0539 | A | giữ UI (A) · đổi B->A ở rà tay đợt 2 | bấm "Tải thêm" với con trỏ hỏng, màn hiện lỗi, không thêm thẻ | test('Con trỏ hỏng: màn hiện MSG-GEO-05  async ({ page, request }) => |
| e5/detail.ui.spec.ts | TC-GEO-0515 | A | giữ UI (A) | chi tiết đề: hiển thị (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Bốn phần của trang chi tiết HKG-01   async ({ page, request }) = |
| e5/detail.ui.spec.ts | TC-GEO-0517 | A | giữ UI (A) | chi tiết đề: hiển thị (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Chip đáp số khớp tính toán chỉ ở đề đã kiểm chứng  async ({ page |
| e5/detail.ui.spec.ts | TC-GEO-0518 | A | giữ UI (A) | chi tiết đề: hiển thị (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Hình xoay được trên trang chi tiết  async ({ page, request }) => |
| e5/detail.ui.spec.ts | TC-GEO-0519 | A | giữ UI (A) | chi tiết đề: hiển thị (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Trang chi tiết trên điện thoại: xếp dọc đúng thứ tự, không cuộn  |
| e5/detail.ui.spec.ts | TC-GEO-0520 | A | giữ UI (A) | chi tiết đề: hiển thị (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Đề không có lời giải: MSG-GEO-54, đề và hình vẫn hiện  async ({  |
| e5/detail.ui.spec.ts | TC-GEO-0521 | A | giữ UI (A) | chi tiết đề: hiển thị (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Không có "Sửa (sắp có)", "Trình chiếu", "Concept · Chặng 2"  asy |
| e5/detail.ui.spec.ts | TC-GEO-0523 | A | giữ UI (A) | chi tiết đề: hiển thị (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Mã đề không tồn tại: màn hiện MSG-GEO-04  async ({ page, request |
| e6/assign.ui.spec.ts | TC-GEO-0605 | A | giữ UI (A) | màn Giao bài: chọn lớp, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Danh sách lớp ở bước 1  async ({ page, request }) => { |
| e6/assign.ui.spec.ts | TC-GEO-0606 | A | giữ UI (A) | màn Giao bài: chọn lớp, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Giao cho một lớp   async ({ page, request }) => { |
| e6/assign.ui.spec.ts | TC-GEO-0607 | A | giữ UI (A) | màn Giao bài: chọn lớp, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Nút giao ghi tên lớp đang chọn  async ({ page, request }) => { |
| e6/assign.ui.spec.ts | TC-GEO-0609 | A | giữ UI (A) · đổi B->A ở rà tay đợt 2 | bấm Giao, màn hiện lỗi MSG-42 tại chỗ | test('Giao lại cùng đề cho cùng lớp  async ({ page, request }) => { |
| e6/assign.ui.spec.ts | TC-GEO-0610 | A | giữ UI (A) | màn Giao bài: chọn lớp, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Bấm đúp nút giao: một bài, một thông báo  async ({ page, request |
| e6/assign.ui.spec.ts | TC-GEO-0611 | A | giữ UI (A) | màn Giao bài: chọn lớp, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Không có giao theo bộ  async ({ page, request }) => { |
| e6/assign.ui.spec.ts | TC-GEO-0612 | A | giữ UI (A) | màn Giao bài: chọn lớp, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Mặc định cả hai công tắc bật  async ({ page, request }) => { |
| e6/assign.ui.spec.ts | TC-GEO-0613 | A | giữ UI (A) | màn Giao bài: chọn lớp, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Tắt gợi ý rồi giao  async ({ page, request }) => { |
| e6/assign.ui.spec.ts | TC-GEO-0614 | A | giữ UI (A) | màn Giao bài: chọn lớp, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Tắt hỏi đáp rồi giao  async ({ page, request }) => { |
| e6/assign.ui.spec.ts | TC-GEO-0615 | A | giữ UI (A) | màn Giao bài: chọn lớp, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Tắt cả hai vẫn giao được (BG-5)  async ({ page, request }) => { |
| e6/assign.ui.spec.ts | TC-GEO-0617 | A | giữ UI (A) | màn Giao bài: chọn lớp, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Không đổi được công tắc sau khi giao  async ({ page, request })  |
| e6/assign.ui.spec.ts | TC-GEO-0618 | A | giữ UI (A) | màn Giao bài: chọn lớp, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Đặt giờ tương lai: Sắp mở, thẻ khóa ghi đúng giờ  async ({ page, |
| e6/assign.ui.spec.ts | TC-GEO-0619 | A | giữ UI (A) | màn Giao bài: chọn lớp, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Để trống giờ mở: MSG-GEO-45, Chưa đặt giờ  async ({ page, reques |
| e6/assign.ui.spec.ts | TC-GEO-0620 | A | giữ UI (A) | màn Giao bài: chọn lớp, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Dòng nhắc trước khi giao (19:00)  async ({ page, request }) => { |
| e6/assign.ui.spec.ts | TC-GEO-0621 | A | giữ UI (A) | màn Giao bài: chọn lớp, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('"Ngay bây giờ": Đã mở ngay sau khi giao  async ({ page, request  |
| e6/assign.ui.spec.ts | TC-GEO-0622 | A | giữ UI (A) | màn Giao bài: chọn lớp, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Giờ Việt Nam, lưu UTC (19:00 → 12:00 UTC)  async ({ page, reques |
| e6/assign.ui.spec.ts | TC-GEO-0623 | A | giữ UI (A) | màn Giao bài: chọn lớp, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Thiết bị ở múi giờ khác vẫn hiện giờ Việt Nam  async ({ browser, |
| e6/assign.ui.spec.ts | TC-GEO-0624 | A | giữ UI (A) | màn Giao bài: chọn lớp, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Giờ ở quá khứ: VALIDATION_FAILED, MSG-GEO-43, không tạo bài  asy |
| e6/assign.ui.spec.ts | TC-GEO-0626 | A | giữ UI (A) | màn Giao bài: chọn lớp, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Không có "Sau buổi học tới"  async ({ page, request }) => { |
| e6/assign.ui.spec.ts | (không gắn mã TC) | A | giữ UI (A) | màn Giao bài: chọn lớp, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Dev-E6-02 "Ngay bây giờ": mất phản hồi lệnh giao, bấm lại → một  |
| e6/assign.ui.spec.ts | (không gắn mã TC) | A | giữ UI (A) | màn Giao bài: chọn lớp, bố cục (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Dev-E6-02 "Ngay bây giờ": mất phản hồi lệnh mở, bấm lại → một bà |
| e6/auto.ui.spec.ts | TC-GEO-0687 | A | giữ UI (A) | tự mở lời giải: hiển thị trạng thái | test('Tới giờ thì lời giải tự mở, không tải lại  async ({ page, reques |
| e6/auto.ui.spec.ts | TC-GEO-0688 | A | giữ UI (A) | tự mở lời giải: hiển thị trạng thái | test('Đếm ngược trên thẻ khóa giảm dần, không bao giờ âm  async ({ pag |
| e6/auto.ui.spec.ts | TC-GEO-0689 | A | giữ UI (A) · đổi B->A ở rà tay đợt 2 | trang đang mở tự hiện thông báo trong 10 giây (hành vi thời gian thực của trang) | test('Giáo viên mở ngay khi học sinh đang xem  async ({ page, request |
| e6/auto.ui.spec.ts | TC-GEO-0690 | A | giữ UI (A) | tự mở lời giải: hiển thị trạng thái | test('"Bài của em" đang mở cũng đổi nhãn  async ({ page, request }) => |
| e6/auto.ui.spec.ts | TC-GEO-0693 | A | giữ UI (A) | tự mở lời giải: hiển thị trạng thái | test('Mất mạng đúng lúc tới giờ: thẻ khóa giữ, có mạng lại thì mở tron |
| e6/auto.ui.spec.ts | TC-GEO-0694 | A | giữ UI (A) | tự mở lời giải: hiển thị trạng thái | test('Đồng hồ thiết bị nhanh 30 giây: chỉ mở theo giờ máy chủ  async ( |
| e6/auto.ui.spec.ts | TC-GEO-0697 | A | giữ UI (A) · đổi B->A ở rà tay đợt 2 | nhịp hỏi lại trạng thái của trang (hành vi trình duyệt) | test('Trang đang chờ khóa hỏi lại trạng thái không quá 8 giây một lần |
| e6/auto.ui.spec.ts | TC-GEO-0864 | A | giữ UI (A) | tự mở lời giải: hiển thị trạng thái | test('Phản hồi trạng thái khóa không cho lưu đệm  async ({ page, reque |
| e6/lock.ui.spec.ts | TC-GEO-0656 | A | giữ UI (A) | thẻ khóa lời giải | test('Đã mở thì trả lời giải (BG-3: hai bước, 24√3 cm³)  async ({ page |
| e6/lock.ui.spec.ts | TC-GEO-0658 | A | giữ UI (A) | thẻ khóa lời giải | test('Thẻ khóa của bài Sắp mở: giờ mở và "Còn 1 giờ 0 phút"  async ({  |
| e6/lock.ui.spec.ts | TC-GEO-0659 | A | giữ UI (A) | thẻ khóa lời giải | test('Thẻ khóa của bài Chưa đặt giờ: không giờ, không đếm ngược  async |
| e6/lock.ui.spec.ts | TC-GEO-0663 | A | giữ UI (A) · đổi B->A ở rà tay đợt 2 | đồng hồ thiết bị (page.clock) vẫn khóa: hành vi phía trình duyệt | test('Đồng hồ thiết bị bị chỉnh nhanh 2 giờ: vẫn khóa  async ({ page, |
| e6/lock.ui.spec.ts | TC-GEO-0667 | A | giữ UI (A) | thẻ khóa lời giải | test('Hỏi thẳng đáp số khi đang khóa: MSG-GEO-35 kèm Gợi ý 2  async ({ |
| e6/lock.ui.spec.ts | TC-GEO-0669 | A | giữ UI (A) | thẻ khóa lời giải | test('Công thức và phương pháp tổng quát vẫn được trả lời, kèm Nguồn   |
| e6/mine.ui.spec.ts | TC-GEO-0639 | A | giữ UI (A) | Bài của em: danh sách | test('Bài Sắp mở, đang làm dở  async ({ page, request }) => { |
| e6/mine.ui.spec.ts | TC-GEO-0640 | A | giữ UI (A) | Bài của em: danh sách | test('Bài Chưa đặt giờ, chưa bắt đầu  async ({ page, request }) => { |
| e6/mine.ui.spec.ts | TC-GEO-0641 | A | giữ UI (A) | Bài của em: danh sách | test('Bài Đã mở, đã xem lời giải  async ({ page, request }) => { |
| e6/mine.ui.spec.ts | TC-GEO-0642 | A | giữ UI (A) | Bài của em: danh sách | test('Đã mở bài nhưng chưa dùng gợi ý  async ({ page, request }) => { |
| e6/mine.ui.spec.ts | TC-GEO-0643 | A | giữ UI (A) | Bài của em: danh sách | test('Bấm vào một bài: màn Làm bài với hai gợi ý đã xem  async ({ page |
| e6/mine.ui.spec.ts | TC-GEO-0645 | A | giữ UI (A) | Bài của em: danh sách | test('Lớp chưa có bài: MSG-GEO-50, vẫn có "Tự khám phá"  async ({ page |
| e6/mine.ui.spec.ts | TC-GEO-0647 | A | giữ UI (A) | Bài của em: danh sách | test('Không có thẻ chuỗi ngày và phần trăm theo dạng  async ({ page, r |
| e6/mine.ui.spec.ts | TC-GEO-0869 | A | giữ UI (A) | Bài của em: danh sách | test('Dòng "Hôm nay em có n bài cần làm"  async ({ page, request }) => |
| e6/mine.ui.spec.ts | TC-GEO-0877 | A | giữ UI (A) | Bài của em: danh sách | test('Không còn bài cần làm  async ({ page, request }) => { |
| e6/preview.ui.spec.ts | TC-GEO-0649 | A | giữ UI (A) | xem trước như học sinh: bấm, hiển thị | test('Mở xem trước: đề, hình, công cụ, ba tab, dải MSG-GEO-51  async ( |
| e6/preview.ui.spec.ts | TC-GEO-0650 | A | giữ UI (A) | xem trước như học sinh: bấm, hiển thị | test('Lời giải khóa trong xem trước (chưa giao, chưa chọn giờ)  async  |
| e6/preview.ui.spec.ts | TC-GEO-0651 | A | giữ UI (A) | xem trước như học sinh: bấm, hiển thị | test('Gợi ý xem được, không ghi sự kiện  async ({ page, request }) =>  |
| e6/preview.ui.spec.ts | TC-GEO-0652 | A | giữ UI (A) | xem trước như học sinh: bấm, hiển thị | test('Thoát xem trước: về trang chi tiết đề với lời giải đầy đủ  async |
| e6/preview.ui.spec.ts | TC-GEO-0655 | A | giữ UI (A) | xem trước như học sinh: bấm, hiển thị | test('Công cụ đo trong xem trước ẩn đại lượng đề hỏi  async ({ page, r |
| e6/preview.ui.spec.ts | TC-GEO-0870 | A | giữ UI (A) | xem trước như học sinh: bấm, hiển thị | test('Đã chọn giờ ở bước 3: thẻ khóa ghi giờ đang chọn  async ({ page, |
| e6/preview.ui.spec.ts | TC-GEO-0871 | A | giữ UI (A) | xem trước như học sinh: bấm, hiển thị | test('Đề đã giao: theo giờ mở của chính bài giao  async ({ page, reque |
| e6/release.ui.spec.ts | TC-GEO-0627 | A | giữ UI (A) | thẻ vàng Mở ngay/Đổi giờ (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Đổi giờ khi Sắp mở  async ({ page, request }) => { |
| e6/release.ui.spec.ts | TC-GEO-0628 | A | giữ UI (A) | thẻ vàng Mở ngay/Đổi giờ (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Đặt giờ lần đầu cho bài Chưa đặt giờ  async ({ page, request })  |
| e6/release.ui.spec.ts | TC-GEO-0629 | A | giữ UI (A) | thẻ vàng Mở ngay/Đổi giờ (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Mở ngay có hộp xác nhận   async ({ page, request }) => { |
| e6/release.ui.spec.ts | TC-GEO-0630 | A | giữ UI (A) | thẻ vàng Mở ngay/Đổi giờ (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Xác nhận mở ngay: giờ mở = giờ máy chủ lúc xác nhận  async ({ pa |
| e6/release.ui.spec.ts | TC-GEO-0631 | A | giữ UI (A) | thẻ vàng Mở ngay/Đổi giờ (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Hủy mở ngay: không đổi, không ghi nhật ký  async ({ page, reques |
| e6/release.ui.spec.ts | TC-GEO-0632 | A | giữ UI (A) | thẻ vàng Mở ngay/Đổi giờ (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Mở ngay từ Chưa đặt giờ  async ({ page, request }) => { |
| e6/release.ui.spec.ts | TC-GEO-0635 | A | giữ UI (A) | thẻ vàng Mở ngay/Đổi giờ (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Đổi giờ khi đã Đã mở: không có thẻ vàng; gửi thẳng nhận CONFLICT |
| e6/release.ui.spec.ts | TC-GEO-0638 | A | giữ UI (A) | thẻ vàng Mở ngay/Đổi giờ (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Không có thao tác xóa giờ đã đặt  async ({ page, request }) => { |
| e6/release.ui.spec.ts | TC-GEO-0699 | A | giữ UI (A) | thẻ vàng Mở ngay/Đổi giờ (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Giờ mở trôi qua trong lúc đang mở hộp "Đổi giờ"  async ({ page,  |
| e7/fix4b.ui.spec.ts | (không gắn mã TC) | A | giữ UI (A) | theo dõi lớp: làm mới giao diện | test('Dev-E7-06 bấm "Mở ngay" trên Theo dõi lớp: thẻ khóa biến mất, Tì |
| e7/fix4b.ui.spec.ts | (không gắn mã TC) | A | giữ UI (A) | theo dõi lớp: làm mới giao diện | test('Dev-E7-06 bấm "Đổi giờ" trên Theo dõi lớp: dòng "còn …" theo giờ |
| e7/monitor.ui.spec.ts | TC-GEO-0700 | A | giữ UI (A) | Theo dõi lớp: bảng, ẩn-hiện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Bốn thẻ tổng   async ({ page, request }) => { |
| e7/monitor.ui.spec.ts | TC-GEO-0701 | A | giữ UI (A) | Theo dõi lớp: bảng, ẩn-hiện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Bảng học sinh   async ({ page, request }) => { |
| e7/monitor.ui.spec.ts | TC-GEO-0702 | A | giữ UI (A) | Theo dõi lớp: bảng, ẩn-hiện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Tình trạng khi bài còn khóa  async ({ page, request }) => { |
| e7/monitor.ui.spec.ts | TC-GEO-0704 | A | giữ UI (A) | Theo dõi lớp: bảng, ẩn-hiện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Thẻ vàng khi lời giải khóa  async ({ page, request }) => { |
| e7/monitor.ui.spec.ts | TC-GEO-0705 | A | giữ UI (A) | Theo dõi lớp: bảng, ẩn-hiện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Vào trang từ mục "Theo dõi"  async ({ page, request }) => { |
| e7/monitor.ui.spec.ts | TC-GEO-0707 | A | giữ UI (A) | Theo dõi lớp: bảng, ẩn-hiện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Chưa em nào mở bài  async ({ page, request }) => { |
| e7/monitor.ui.spec.ts | TC-GEO-0708 | A | giữ UI (A) | Theo dõi lớp: bảng, ẩn-hiện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Không có xếp hạng, không sắp theo kết quả  async ({ page, reques |
| e7/monitor.ui.spec.ts | TC-GEO-0709 | A | giữ UI (A) | Theo dõi lớp: bảng, ẩn-hiện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Không có "Nên nhắc lại trên lớp"  async ({ page, request }) => { |
| e7/monitor.ui.spec.ts | TC-GEO-0744 | A | giữ UI (A) | Theo dõi lớp: bảng, ẩn-hiện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Thanh bên không có "Tổng quan" và "Lớp học"  async ({ page, requ |
| e7/monitor.ui.spec.ts | TC-GEO-0713 | A | giữ UI (A) | Theo dõi lớp: bảng, ẩn-hiện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Mức gợi ý của từng em  async ({ page, request }) => { |
| e7/monitor.ui.spec.ts | TC-GEO-0714 | A | giữ UI (A) | Theo dõi lớp: bảng, ẩn-hiện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Số câu hỏi của từng em  async ({ page, request }) => { |
| e7/monitor.ui.spec.ts | TC-GEO-0715 | A | giữ UI (A) | Theo dõi lớp: bảng, ẩn-hiện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Câu hỏi của một em  async ({ page, request }) => { |
| e7/monitor.ui.spec.ts | TC-GEO-0716 | A | giữ UI (A) | Theo dõi lớp: bảng, ẩn-hiện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Khối "Học sinh đang hỏi gì" gộp câu trùng  async ({ page, reques |
| e7/monitor.ui.spec.ts | TC-GEO-0718 | A | giữ UI (A) | Theo dõi lớp: bảng, ẩn-hiện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test('Chưa ai hỏi  async ({ page, request }) => { |
| e7/offline.ui.spec.ts | TC-GEO-0729 | A | giữ UI (A) | mất mạng: nút tắt, thông báo | test('Mất mạng khi đang thao tác  async ({ page, request, context }) = |
| home.ui.spec.ts | TC-KHUNG-03 | A | giữ UI (A) | trang chủ hiển thị | test('trang chủ mở, máy chủ sẵn sàng, giờ Việt Nam, vào vùng Giáo viên |
| s2e1/layout.p0m.ui.spec.ts | (không gắn mã TC) | A | giữ UI (A) | bố cục di động P0 · chạy thêm mobile-iphone/mobile-pixel | test('S2-E1 bố cục đăng ký, màn mã, đăng nhập, khôi phục không tràn và |
| s2e1/login.ui.spec.ts | TC-S2E1-019 | A | giữ UI (A) | đăng nhập/khôi phục: nhập, lỗi chung | test('Học sinh đăng nhập đúng   async ({ page }) => { |
| s2e1/login.ui.spec.ts | TC-S2E1-020 | A | giữ UI (A) | đăng nhập/khôi phục: nhập, lỗi chung | test('Giáo viên đăng nhập đúng   async ({ page }) => { |
| s2e1/login.ui.spec.ts | TC-S2E1-022 | A | giữ UI (A) | đăng nhập/khôi phục: nhập, lỗi chung | test('Sai mật khẩu hiện câu chung  async ({ page }) => { |
| s2e1/login.ui.spec.ts | TC-S2E1-029 | A | giữ UI (A) | đăng nhập/khôi phục: nhập, lỗi chung | test('Để trống một trong hai ô  async ({ page }) => { |
| s2e1/login.ui.spec.ts | TC-S2E1-030 | A | giữ UI (A) · đổi B->A ở rà tay đợt 2 | trang chuyển về đăng nhập + thông báo hết phiên khi bấm điều hướng | test('Phiên hết hạn thì yêu cầu bị từ chối  async ({ page, request }, |
| s2e1/login.ui.spec.ts | TC-S2E1-031 | A | giữ UI (A) | đăng nhập/khôi phục: nhập, lỗi chung | test('Khôi phục đúng đặt được mật khẩu mới  async ({ page, request },  |
| s2e1/login.ui.spec.ts | TC-S2E1-033 | A | giữ UI (A) | đăng nhập/khôi phục: nhập, lỗi chung | test('Sai mã khôi phục hiện câu chung  async ({ page, request }, testI |
| s2e1/login.ui.spec.ts | TC-S2E1-043 | A | giữ UI (A) | đăng nhập/khôi phục: nhập, lỗi chung | test('Tài khoản chuyển phải đặt mật khẩu mới ở lần đăng nhập đầu  asyn |
| s2e1/register.ui.spec.ts | TC-S2E1-001 | A | giữ UI (A) | đăng ký: nhập, lỗi, khóa nút | test('Giáo viên đăng ký và nhận hai mã  async ({ page }) => { |
| s2e1/register.ui.spec.ts | TC-S2E1-002 | A | giữ UI (A) | đăng ký: nhập, lỗi, khóa nút | test('Học sinh đăng ký nhận mã người dùng và mã khôi phục  async ({ pa |
| s2e1/register.ui.spec.ts | TC-S2E1-003 | A | giữ UI (A) | đăng ký: nhập, lỗi, khóa nút | test('Nút Tiếp tục chỉ mở khi đã tick "Tôi đã lưu mã"  async ({ page } |
| s2e1/register.ui.spec.ts | TC-S2E1-007 | A | giữ UI (A) | đăng ký: nhập, lỗi, khóa nút | test('Tên rỗng hoặc chỉ khoảng trắng bị chặn  async ({ page }) => { |
| s2e1/register.ui.spec.ts | TC-S2E1-008 | A | giữ UI (A) | đăng ký: nhập, lỗi, khóa nút | test('Tên đúng 60 ký tự được nhận  async ({ page }) => { |
| s2e1/register.ui.spec.ts | TC-S2E1-009 | A | giữ UI (A) | đăng ký: nhập, lỗi, khóa nút | test('Tên 61 ký tự bị chặn  async ({ page }) => { |
| s2e1/register.ui.spec.ts | TC-S2E1-012 | A | giữ UI (A) | đăng ký: nhập, lỗi, khóa nút | test('Mật khẩu 7 ký tự bị chặn  async ({ page }) => { |
| s2e1/register.ui.spec.ts | TC-S2E1-018 | A | giữ UI (A) | đăng ký: nhập, lỗi, khóa nút | test('Bấm Đăng ký liên tiếp chỉ tạo một tài khoản  async ({ page }) => |
| s2e2/phone.p0m.ui.spec.ts | (không gắn mã TC) | A | giữ UI (A) | hình phẳng trên điện thoại (bố cục) · chạy thêm mobile-iphone/mobile-pixel | test('S2-E2 màn hình phẳng theme C: không cuộn ngang, chạm ≥ 44, chữ t |
| s2e2/plane.ui.spec.ts | TC-S2E2-022 | A | giữ UI (A) | hình phẳng: vẽ, hiển thị | test(' mở bài phẳng hiện hình, nhãn đỉnh, công cụ đo, gợi ý và nhãn ha |
| s2e2/plane.ui.spec.ts | TC-S2E2-023 | A | giữ UI (A) | hình phẳng: vẽ, hiển thị | test(' đo cạnh AB cho đúng 5', async ({ page, request }) => { |
| s2e2/plane.ui.spec.ts | TC-S2E2-024 | A | giữ UI (A) | hình phẳng: vẽ, hiển thị | test(' đo đại lượng là đáp số (góc A) bị khóa', async ({ page, request |
| s2e2/plane.ui.spec.ts | TC-S2E2-025 | A | giữ UI (A) | hình phẳng: vẽ, hiển thị | test(' sau release_at công cụ đo mở cho đáp số', async ({ page, reques |
| s2e2/plane.ui.spec.ts | TC-S2E2-027 | A | giữ UI (A) | hình phẳng: vẽ, hiển thị | test(' hình chưa kiểm chứng hiện nhãn "Cần xem lại" và "AI soạn", vẫn  |
| s2e2/plane.ui.spec.ts | TC-S2E2-029 | A | giữ UI (A) | hình phẳng: vẽ, hiển thị | test(' trạng thái đang tải có khung giữ chỗ; mất mạng hiện thông báo v |
| s2e3/image.ui.spec.ts | TC-S2E3-010 | A | giữ UI (A) | nhập đề bằng ảnh: bấm, hiển thị | test(' ảnh rõ thì hiện chữ đã đọc, không có ô vàng, nút Dựng hình mở', |
| s2e3/image.ui.spec.ts | TC-S2E3-011 | A | giữ UI (A) | nhập đề bằng ảnh: bấm, hiển thị | test(' ô chưa chắc tô vàng, nút xác nhận khóa, nhãn máy Cần xem lại',  |
| s2e3/image.ui.spec.ts | TC-S2E3-013 | A | giữ UI (A) | nhập đề bằng ảnh: bấm, hiển thị | test(' sửa hết ô vàng mới xác nhận được; sau xác nhận nhãn máy vẫn Cần |
| s2e3/image.ui.spec.ts | TC-S2E3-014 | A | giữ UI (A) | nhập đề bằng ảnh: bấm, hiển thị | test(' chưa xác nhận ô vàng vẫn lưu được nháp, mở lại còn nguyên', asy |
| s2e3/image.ui.spec.ts | TC-S2E3-015 | A | giữ UI (A) | nhập đề bằng ảnh: bấm, hiển thị | test(' ảnh không có chữ: yêu cầu gõ lại, ô sửa để trống, không có đề đ |
| s2e3/image.ui.spec.ts | TC-S2E3-019 | A | giữ UI (A) | nhập đề bằng ảnh: bấm, hiển thị | test(' chọn hai ảnh một lúc chỉ đọc ảnh đầu, báo "Mỗi lần tải một ảnh" |
| s2e3/paste.ui.spec.ts | TC-S2E3-001 | A | giữ UI (A) | dán đề: nhập, hiển thị | test(' dán đề FX-10 thì thấy ô "Đề đã chép lại" đúng chữ, sửa được, có |
| s2e3/paste.ui.spec.ts | TC-S2E3-002 | A | giữ UI (A) | dán đề: nhập, hiển thị | test(' ký hiệu toán giữ nguyên khi dán', async ({ page, request }, inf |
| s2e3/paste.ui.spec.ts | TC-S2E3-003 | A | giữ UI (A) | dán đề: nhập, hiển thị | test(' ô trống không cho dựng, không tạo tác vụ', async ({ page, reque |
| s2e3/paste.ui.spec.ts | TC-S2E3-004 | A | giữ UI (A) | dán đề: nhập, hiển thị | test(' đề 9 ký tự bị chặn, 10 ký tự được nhận', async ({ page, request |
| s2e3/paste.ui.spec.ts | TC-S2E3-005 | A | giữ UI (A) | dán đề: nhập, hiển thị | test(' đề đúng 2000 ký tự được nhận', async ({ page, request }, info)  |
| s2e3/paste.ui.spec.ts | TC-S2E3-007 | A | giữ UI (A) | dán đề: nhập, hiển thị | test(' mã HTML và script hiện thành chữ, không thực thi', async ({ pag |
| s2e3/paste.ui.spec.ts | TC-S2E3-008 | A | giữ UI (A) | dán đề: nhập, hiển thị | test(' nháp chữ còn khi rời màn rồi quay lại', async ({ page, request  |
| s2e3/pipeline.ui.spec.ts | TC-S2E3-021 | A | giữ UI (A) | luồng đọc đề: bước, bố cục | test(' bốn bước chạy đúng thứ tự, kết thúc có hình, lời giải nháp, Đã  |
| s2e3/pipeline.ui.spec.ts | TC-S2E3-026 | A | giữ UI (A) · đổi B->A ở rà tay đợt 2 | đóng tab, mở lại trang khôi phục tiến trình trên màn | test(' đóng tab rồi mở lại vẫn thấy tiến trình, không tạo tác vụ thứ h |
| s2e3/pipeline.ui.spec.ts | TC-S2E3-028 | A | giữ UI (A) | luồng đọc đề: bước, bố cục | test(' hủy tác vụ đang dựng: dừng, "Đã hủy", đề vẫn là nháp chữ', asyn |
| s2e3/pipeline.ui.spec.ts | TC-S2E3-030 | A | giữ UI (A) | luồng đọc đề: bước, bố cục | test(' engine không dựng thì lưu "Cần xem lại · hình do AI dựng" với b |
| s2e3/pipeline.ui.spec.ts | TC-S2E3-031 | A | giữ UI (A) | luồng đọc đề: bước, bố cục | test(' lưu bản nháp chữ thì đề không chọn được để giao, kèm "bản nháp  |
| s2e3/pipeline.ui.spec.ts | TC-S2E3-032 | A | giữ UI (A) | luồng đọc đề: bước, bố cục | test(' giao đề "Cần xem lại · hình do AI dựng" phải bấm xác nhận riêng |
| s2e3/pipeline.ui.spec.ts | TC-S2E3-033 | A | giữ UI (A) | luồng đọc đề: bước, bố cục | test(' lệch đáp số hiện bảng Đề ghi / AI giải / Máy tính lại và chặn D |
| s2e3/pipeline.ui.spec.ts | TC-S2E3-035 | A | giữ UI (A) | luồng đọc đề: bước, bố cục | test(' sửa đáp số đề ghi cho khớp rồi Kiểm tra lại thì mở Duyệt', asyn |
| s2e4/chat.ui.spec.ts | TC-S2E4-001 | A | giữ UI (A) | trò chuyện soạn đề: hiển thị | test(' định hướng rõ cho một đề xuất có trước và sau', async ({ page,  |
| s2e4/chat.ui.spec.ts | TC-S2E4-002 | A | giữ UI (A) | trò chuyện soạn đề: hiển thị | test(' định hướng mơ hồ thì AI hỏi lại, không tự đoán', async ({ page, |
| s2e4/chat.ui.spec.ts | TC-S2E4-003 | A | giữ UI (A) | trò chuyện soạn đề: hiển thị | test(' nhận đề xuất thì nháp đổi và nhãn máy chuyển Cần xem lại', asyn |
| s2e4/chat.ui.spec.ts | TC-S2E4-004 | A | giữ UI (A) | trò chuyện soạn đề: hiển thị | test(' bác đề xuất thì nháp giữ nguyên', async ({ page, request }, inf |
| s2e4/chat.ui.spec.ts | TC-S2E4-005 | A | giữ UI (A) | trò chuyện soạn đề: hiển thị | test(' sửa tay đề xuất rồi mới nhận', async ({ page, request }, info)  |
| s2e4/chat.ui.spec.ts | TC-S2E4-006 | A | giữ UI (A) | trò chuyện soạn đề: hiển thị | test(' hoàn tác trả nháp về bước trước', async ({ page, request }, inf |
| s2e4/chat.ui.spec.ts | TC-S2E4-007 | A | giữ UI (A) | trò chuyện soạn đề: hiển thị | test(' hoàn tác 10 bước được, bước thứ 11 không', async ({ page, reque |
| s2e4/chat.ui.spec.ts | TC-S2E4-008 | A | giữ UI (A) | trò chuyện soạn đề: hiển thị | test(' AI không tự lưu: nháp không vào giáo trình khi chưa duyệt', asy |
| s2e4/import.ui.spec.ts | TC-S2E4-012 | A | giữ UI (A) | nhập tệp: chọn tệp, hiển thị | test(' tách đúng từng bài tập của tệp docx', async ({ page, request }, |
| s2e4/import.ui.spec.ts | TC-S2E4-013 | A | giữ UI (A) | nhập tệp: chọn tệp, hiển thị | test(' chỉ bài được tick mới thành nháp', async ({ page, request }, in |
| s2e4/import.ui.spec.ts | TC-S2E4-014 | A | giữ UI (A) | nhập tệp: chọn tệp, hiển thị | test(' tệp pdf có lớp chữ được tách như docx', async ({ page, request  |
| s2e4/import.ui.spec.ts | TC-S2E4-015 | A | giữ UI (A) | nhập tệp: chọn tệp, hiển thị | test(' pdf ảnh quét bị từ chối', async ({ page, request }, info) => { |
| s2e4/import.ui.spec.ts | TC-S2E4-021 | A | giữ UI (A) | nhập tệp: chọn tệp, hiển thị | test(' bài trùng đề trong Kho chung hiện đã có trong kho', async ({ pa |
| s2e5/apply.ui.spec.ts | TC-S2E5-053 | A | giữ UI (A) | áp dụng đề: bấm, hiển thị | test(' sửa đề đã giao tạo v3, BG-1 và BG-2 vẫn dùng v2, có lựa chọn ch |
| s2e5/apply.ui.spec.ts | TC-S2E5-054 | A | giữ UI (A) | áp dụng đề: bấm, hiển thị | test(' hộp đã giao hiện đúng phiên bản đang dùng và chú thích bất biến |
| s2e5/apply.ui.spec.ts | TC-S2E5-057 | A | giữ UI (A) | áp dụng đề: bấm, hiển thị | test(' duyệt v3 rồi áp dụng cho BG-1: 11A1 dùng v3, 11A2 vẫn v2', asyn |
| s2e5/apply.ui.spec.ts | TC-S2E5-059 | A | giữ UI (A) | áp dụng đề: bấm, hiển thị | test(' chọn Giữ v2 cho cả hai bài giao thì không đổi, v3 chỉ ở "Của tô |
| s2e5/apply.ui.spec.ts | TC-S2E5-060 | A | giữ UI (A) | áp dụng đề: bấm, hiển thị | test(' áp dụng cho bài đã có 2 học sinh xem lời giải v2 phải xác nhận' |
| s2e5/apply.ui.spec.ts | TC-S2E5-064 | A | giữ UI (A) | áp dụng đề: bấm, hiển thị | test(' học sinh đang mở bài thấy ổn định, tải lại thấy nhãn theo v3',  |
| s2e5/approve.ui.spec.ts | TC-S2E5-039 | A | giữ UI (A) | duyệt đề: hiển thị trạng thái | test(' duyệt khi máy Đã kiểm chứng: không hộp cảnh báo, ghi mã người d |
| s2e5/approve.ui.spec.ts | TC-S2E5-040 | A | giữ UI (A) | duyệt đề: hiển thị trạng thái | test(' duyệt khi Cần xem lại phải xác nhận cảnh báo', async ({ page, r |
| s2e5/approve.ui.spec.ts | TC-S2E5-041 | A | giữ UI (A) | duyệt đề: hiển thị trạng thái | test(' hủy ở hộp cảnh báo thì không duyệt', async ({ page, request },  |
| s2e5/approve.ui.spec.ts | TC-S2E5-043 | A | giữ UI (A) | duyệt đề: hiển thị trạng thái | test(' 0 bước lệch thì Duyệt mở, 1 bước lệch thì khóa', async ({ page, |
| s2e5/approve.ui.spec.ts | TC-S2E5-044 | A | giữ UI (A) | duyệt đề: hiển thị trạng thái | test(' còn 3 bước lệch thì thông báo đúng số', async ({ page, request  |
| s2e5/approve.ui.spec.ts | TC-S2E5-045 | A | giữ UI (A) | duyệt đề: hiển thị trạng thái | test(' nhận đề xuất của AI thành phiên bản mới', async ({ page, reques |
| s2e5/approve.ui.spec.ts | TC-S2E5-046 | A | giữ UI (A) | duyệt đề: hiển thị trạng thái | test(' sửa đề xuất mở soạn với nội dung đề xuất, chưa tạo phiên bản tớ |
| s2e5/approve.ui.spec.ts | TC-S2E5-047 | A | giữ UI (A) | duyệt đề: hiển thị trạng thái | test(' bỏ đề xuất thì loại và còn dấu vết', async ({ page, request },  |
| s2e5/approve.ui.spec.ts | TC-S2E5-052 | A | giữ UI (A) | duyệt đề: hiển thị trạng thái | test(' hộp duyệt rỗng: nút Duyệt vẫn theo trạng thái lời giải', async  |
| s2e5/approve.ui.spec.ts | TC-S2E5-065 | A | giữ UI (A) | duyệt đề: hiển thị trạng thái | test(' luồng đầy đủ: AI soạn sai, sửa, kiểm, duyệt, giao', async ({ pa |
| s2e5/phone.p0m.ui.spec.ts | TC-S2E5-038 | A | giữ UI (A) | duyệt đề trên điện thoại (bố cục) · chạy thêm mobile-iphone/mobile-pixel | test(' học sinh: hai nhãn của phiên bản đã giao hiện đủ trên điện thoạ |
| s2e5/phone.p0m.ui.spec.ts | (không gắn mã TC) | A | giữ UI (A) | duyệt đề trên điện thoại (bố cục) · chạy thêm mobile-iphone/mobile-pixel | test('S2-E5 C2-03 theme C: không cuộn ngang, chạm ≥ 44, chữ toán dùng  |
| s2e5/stepcheck.ui.spec.ts | TC-S2E5-015 | A | giữ UI (A) | kiểm bước giải: hiển thị | test(' bước 3 sai tô đỏ kèm lý do, các bước khác Đạt', async ({ page,  |
| s2e5/stepcheck.ui.spec.ts | TC-S2E5-016 | A | giữ UI (A) | kiểm bước giải: hiển thị | test(' sửa đúng bước 3 rồi kiểm lại thì mọi bước có phép tính Đạt', as |
| s2e5/stepcheck.ui.spec.ts | TC-S2E5-017 | A | giữ UI (A) | kiểm bước giải: hiển thị | test(' bước chỉ chữ là Không kiểm được, không tính là lệch', async ({  |
| s2e5/stepcheck.ui.spec.ts | TC-S2E5-023 | A | giữ UI (A) | kiểm bước giải: hiển thị | test(' xem v1 hiện kết quả kiểm của v1, không phải của v2', async ({ p |
| s2e5/stepcheck.ui.spec.ts | TC-S2E5-028 | A | giữ UI (A) | kiểm bước giải: hiển thị | test(' lời giải AI chưa kiểm hiện Cần xem lại và AI soạn cùng nhau', a |
| s2e5/stepcheck.ui.spec.ts | TC-S2E5-029 | A | giữ UI (A) | kiểm bước giải: hiển thị | test(' kiểm đạt toàn bộ hiện Đã kiểm chứng kèm dòng chú thích', async  |
| s2e5/stepcheck.ui.spec.ts | TC-S2E5-030 | A | giữ UI (A) | kiểm bước giải: hiển thị | test(' duyệt không đổi nhãn máy', async ({ page, request }, info) => { |
| s2e5/stepcheck.ui.spec.ts | TC-S2E5-035 | A | giữ UI (A) | kiểm bước giải: hiển thị | test(' sửa sau khi Đã kiểm chứng thì về Cần xem lại, bản cũ giữ nhãn', |
| s2e5/stepcheck.ui.spec.ts | TC-S2E5-036 | A | giữ UI (A) | kiểm bước giải: hiển thị | test(' học sinh thấy nhãn của phiên bản đã giao, không thấy nhãn của b |
| s2e5/versions.ui.spec.ts | TC-S2E5-001 | A | giữ UI (A) | phiên bản đề: bấm, hiển thị | test(' sửa bước 3 rồi lưu thành v2, v1 còn nguyên', async ({ page, req |
| s2e5/versions.ui.spec.ts | TC-S2E5-002 | A | giữ UI (A) | phiên bản đề: bấm, hiển thị | test(' thêm một bước ở giữa thì đánh số lại, lưu thành v3', async ({ p |
| s2e5/versions.ui.spec.ts | TC-S2E5-003 | A | giữ UI (A) | phiên bản đề: bấm, hiển thị | test(' xóa một bước và kéo đổi thứ tự: một lần lưu một phiên bản', asy |
| s2e5/versions.ui.spec.ts | TC-S2E5-004 | A | giữ UI (A) | phiên bản đề: bấm, hiển thị | test(' lịch sử phiên bản đủ thông tin và mới nhất ở trên', async ({ pa |
| s2e5/versions.ui.spec.ts | TC-S2E5-005 | A | giữ UI (A) | phiên bản đề: bấm, hiển thị | test(' dùng bản cũ tạo phiên bản mới là bản sao', async ({ page, reque |
| s2e5/versions.ui.spec.ts | TC-S2E5-006 | A | giữ UI (A) | phiên bản đề: bấm, hiển thị | test(' lưu khi chưa đổi gì không tạo phiên bản', async ({ page, reques |
| s2e5/versions.ui.spec.ts | TC-S2E5-007 | A | giữ UI (A) | phiên bản đề: bấm, hiển thị | test(' bước trống bị chặn, viền đỏ, không tạo phiên bản', async ({ pag |
| s2e5/versions.ui.spec.ts | TC-S2E5-008 | A | giữ UI (A) | phiên bản đề: bấm, hiển thị | test(' bước đúng 2000 ký tự được lưu', async ({ page, request }, info) |
| s2e5/versions.ui.spec.ts | TC-S2E5-011 | A | giữ UI (A) | phiên bản đề: bấm, hiển thị | test(' xóa còn 1 bước được, xóa bước cuối cùng bị chặn', async ({ page |
| s2e6/library.ui.spec.ts | TC-S2E6-001 | A | giữ UI (A) | thư viện đề: lọc, bấm, bố cục | test(' ngăn Của tôi mặc định hiện đề của chủ, mới nhất ở trên', async  |
| s2e6/library.ui.spec.ts | TC-S2E6-002 | A | giữ UI (A) | thư viện đề: lọc, bấm, bố cục | test(' ngăn Kho chung chỉ có đề đã soát', async ({ page, request }, in |
| s2e6/library.ui.spec.ts | TC-S2E6-003 | A | giữ UI (A) | thư viện đề: lọc, bấm, bố cục | test(' lọc Phẳng chỉ giữ đề phẳng, không tải lại trang', async ({ page |
| s2e6/library.ui.spec.ts | TC-S2E6-004 | A | giữ UI (A) | thư viện đề: lọc, bấm, bố cục | test(' đổi ngăn đưa bộ lọc về Tất cả', async ({ page, request }, info) |
| s2e6/library.ui.spec.ts | TC-S2E6-005 | A | giữ UI (A) | thư viện đề: lọc, bấm, bố cục | test(' chưa có đề riêng hiện trạng thái trống', async ({ page, request |
| s2e6/library.ui.spec.ts | TC-S2E6-006 | A | giữ UI (A) | thư viện đề: lọc, bấm, bố cục | test(' lọc không có kết quả, Xóa bộ lọc trở về danh sách đủ', async ({ |
| s2e6/library.ui.spec.ts | TC-S2E6-007 | A | giữ UI (A) | thư viện đề: lọc, bấm, bố cục | test(' trạng thái đang tải hiện khung giữ chỗ, không hiện trống', asyn |
| s2e6/library.ui.spec.ts | TC-S2E6-013 | A | giữ UI (A) | thư viện đề: lọc, bấm, bố cục | test(' giáo viên gửi đề đã duyệt vào hàng đợi', async ({ page, request |
| s2e6/library.ui.spec.ts | TC-S2E6-014 | A | giữ UI (A) | thư viện đề: lọc, bấm, bố cục | test(' người soát duyệt thì đề vào Kho chung', async ({ page, request  |
| s2e6/library.ui.spec.ts | TC-S2E6-015 | A | giữ UI (A) | thư viện đề: lọc, bấm, bố cục | test(' người soát từ chối có lý do', async ({ page, request }, info) = |
| s2e6/library.ui.spec.ts | TC-S2E6-020 | A | giữ UI (A) | thư viện đề: lọc, bấm, bố cục | test(' lý do từ chối 4 ký tự bị chặn, 5 ký tự nhận', async ({ page, re |
| s2e6/library.ui.spec.ts | TC-S2E6-024 | A | giữ UI (A) | thư viện đề: lọc, bấm, bố cục | test(' đề bị từ chối gửi lại được sau khi sửa, lịch sử giữ dòng từ chố |
| s2e6/library.ui.spec.ts | TC-S2E6-034 | A | giữ UI (A) | thư viện đề: lọc, bấm, bố cục | test(' giáo viên xem được lời giải đề Kho chung kèm nhãn hai trục', as |
| s2e7/present.ui.spec.ts | TC-S2E7-001 | A | giữ UI (A) | trình chiếu: toàn màn hình, bố cục | test(' vào Trình chiếu thấy hình lớn, đề và lời giải che sẵn', async ( |
| s2e7/present.ui.spec.ts | TC-S2E7-002 | A | giữ UI (A) | trình chiếu: toàn màn hình, bố cục | test(' hiện bước kế hiện đúng một bước và tô màu hình', async ({ page, |
| s2e7/present.ui.spec.ts | TC-S2E7-003 | A | giữ UI (A) | trình chiếu: toàn màn hình, bố cục | test(' phím mũi tên và Space điều khiển bước', async ({ page, request  |
| s2e7/present.ui.spec.ts | TC-S2E7-004 | A | giữ UI (A) | trình chiếu: toàn màn hình, bố cục | test(' che lời giải ẩn tất cả, bỏ che hiện lại đúng bước', async ({ pa |
| s2e7/present.ui.spec.ts | TC-S2E7-005 | A | giữ UI (A) · đổi B->A ở rà tay đợt 2 | khung 3D: xoay, đo, thêm đường phụ | test(' xoay hình, đo, thêm đường phụ thì hình đổi ngay', async ({ page |
| s2e7/present.ui.spec.ts | TC-S2E7-006 | A | giữ UI (A) | trình chiếu: toàn màn hình, bố cục | test(' ở bước cuối nút Hiện bước kế khóa', async ({ page, request }, i |
| s2e7/present.ui.spec.ts | TC-S2E7-007 | A | giữ UI (A) | trình chiếu: toàn màn hình, bố cục | test(' ở bước 0 nút Bước trước khóa', async ({ page, request }, info)  |
| s2e7/present.ui.spec.ts | TC-S2E7-008 | A | giữ UI (A) | trình chiếu: toàn màn hình, bố cục | test(' lời giải 30 bước chiếu đủ 30 bước, chuyển bước dưới 200 ms', as |
| s2e7/present.ui.spec.ts | TC-S2E7-009 | A | giữ UI (A) | trình chiếu: toàn màn hình, bố cục | test(' Esc thoát về màn đề và che lại lời giải', async ({ page, reques |
| s2e7/present.ui.spec.ts | TC-S2E7-010 | A | giữ UI (A) | trình chiếu: toàn màn hình, bố cục | test(' trình duyệt từ chối toàn màn hình vẫn chiếu được', async ({ pag |
| s2e7/present.ui.spec.ts | TC-S2E7-012 | A | giữ UI (A) | trình chiếu: toàn màn hình, bố cục | test(' đề chưa có lời giải thì khóa nút hiện bước', async ({ page, req |
| s2e7/present.ui.spec.ts | TC-S2E7-013 | A | giữ UI (A) | trình chiếu: toàn màn hình, bố cục | test(' hai nhãn luôn nằm ở góc màn hình', async ({ page, request }, in |
| s2e7/present.ui.spec.ts | TC-S2E7-015 | A | giữ UI (A) | trình chiếu: toàn màn hình, bố cục | test(' vào Trình chiếu có mạng thì hiện chip Đã tải sẵn', async ({ pag |
| s2e7/present.ui.spec.ts | TC-S2E7-016 | A | giữ UI (A) | trình chiếu: toàn màn hình, bố cục | test(' mất mạng giữa giờ vẫn hiện bước và xoay hình', async ({ page, r |
| s2e7/present.ui.spec.ts | TC-S2E7-017 | A | giữ UI (A) | trình chiếu: toàn màn hình, bố cục | test(' mất mạng thì hỏi đáp AI tắt kèm thông báo, không gửi yêu cầu',  |
| s2e7/present.ui.spec.ts | TC-S2E7-018 | A | giữ UI (A) | trình chiếu: toàn màn hình, bố cục | test(' có mạng lại thì hỏi đáp AI bật lại trong 5 giây', async ({ page |
| s2e7/present.ui.spec.ts | TC-S2E7-020 | A | giữ UI (A) | trình chiếu: toàn màn hình, bố cục | test(' vào Trình chiếu khi đã mất mạng và chưa tải sẵn', async ({ page |
| s2e7/present.ui.spec.ts | TC-S2E7-021 | A | giữ UI (A) | trình chiếu: toàn màn hình, bố cục | test(' mất mạng khi đang tải sẵn thì báo chưa đủ', async ({ page, requ |
| s2e7/present.ui.spec.ts | TC-S2E7-022 | A | giữ UI (A) | trình chiếu: toàn màn hình, bố cục | test(' thoát Trình chiếu xóa dữ liệu tải sẵn khỏi bộ nhớ trình duyệt', |
| s2e7/present.ui.spec.ts | TC-S2E7-023 | A | giữ UI (A) · đổi B->A ở rà tay đợt 2 | mất mạng lúc trình chiếu, bấm tiếp/thoát | test(' phiên hết hạn lúc mất mạng thì chiếu tiếp, về lại thì đăng nhập |
| s2e7/present.ui.spec.ts | TC-S2E7-024 | A | giữ UI (A) | trình chiếu: toàn màn hình, bố cục | test(' đề đang giao và lời giải khóa thì hỏi lại', async ({ page, requ |
| s2e7/present.ui.spec.ts | TC-S2E7-025 | A | giữ UI (A) | trình chiếu: toàn màn hình, bố cục | test(' Tiếp tục thì vào chiếu, máy học sinh vẫn khóa', async ({ page,  |
| s2e7/present.ui.spec.ts | TC-S2E7-026 | A | giữ UI (A) | trình chiếu: toàn màn hình, bố cục | test(' Hủy thì ở lại màn đề và không thay đổi gì', async ({ page, requ |
| s2e7/present.ui.spec.ts | TC-S2E7-027 | A | giữ UI (A) | trình chiếu: toàn màn hình, bố cục | test(' đề chưa giao thì không hỏi', async ({ page, request }, info) => |
| s2e7/present.ui.spec.ts | TC-S2E7-028 | A | giữ UI (A) | trình chiếu: toàn màn hình, bố cục | test(' bài giao đã mở lời giải thì không hỏi', async ({ page, request  |
| s2e7/present.ui.spec.ts | TC-S2E7-030 | A | giữ UI (A) | trình chiếu: toàn màn hình, bố cục | test(' chưa đặt giờ mở thì cũng hỏi', async ({ page, request }, info)  |
| s2e7/present.ui.spec.ts | TC-S2E7-031 | A | giữ UI (A) | trình chiếu: toàn màn hình, bố cục | test(' nhiều lớp thì chỉ nêu tên lớp còn khóa', async ({ page, request |
| s2e8/compose.ui.spec.ts | TC-S2E8-088 | A | giữ UI (A) | soạn bộ luyện: kéo thả, bấm | test(' chọn đề có đáp số và tạo bài nháp', async ({ page, request }) = |
| s2e8/compose.ui.spec.ts | TC-S2E8-089 | A | giữ UI (A) | soạn bộ luyện: kéo thả, bấm | test(' đề chưa có đáp số máy chấm hiện nhưng không chọn được', async ( |
| s2e8/compose.ui.spec.ts | TC-S2E8-090 | A | giữ UI (A) | soạn bộ luyện: kéo thả, bấm | test(' đặt kiểu xấp xỉ, sai số, lượt và giờ mở', async ({ page, reques |
| s2e8/compose.ui.spec.ts | TC-S2E8-095 | A | giữ UI (A) | soạn bộ luyện: kéo thả, bấm | test(' tên bài 80 ký tự nhận, 81 bị chặn, rỗng bị chặn', async ({ page |
| s2e8/compose.ui.spec.ts | TC-S2E8-100 | A | giữ UI (A) | soạn bộ luyện: kéo thả, bấm | test(' giờ mở đã qua cần xác nhận', async ({ page, request }) => { |
| s2e8/compose.ui.spec.ts | TC-S2E8-101 | A | giữ UI (A) | soạn bộ luyện: kéo thả, bấm | test(' học sinh gửi báo Em nghĩ đáp số này đúng sau khi bị chấm Sai',  |
| s2e8/compose.ui.spec.ts | TC-S2E8-105 | A | giữ UI (A) | soạn bộ luyện: kéo thả, bấm | test(' giáo viên thấy báo kèm đáp số và dạng chuẩn', async ({ page, re |
| s2e8/keypad.p0m.ui.spec.ts | TC-S2E8-001 | A | giữ UI (A) | bàn phím số trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' phím số chèn chữ số vào ô', async ({ page, request }) => { |
| s2e8/keypad.p0m.ui.spec.ts | TC-S2E8-002 | A | giữ UI (A) | bàn phím số trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' phím căn và phân số chèn đúng ký hiệu', async ({ page, request  |
| s2e8/keypad.p0m.ui.spec.ts | TC-S2E8-003 | A | giữ UI (A) | bàn phím số trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' phím π, mũ và độ chèn đúng', async ({ page, request }) => { |
| s2e8/keypad.p0m.ui.spec.ts | TC-S2E8-004 | A | giữ UI (A) | bàn phím số trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' dấu phẩy nhập số thập phân', async ({ page, request }) => { |
| s2e8/keypad.p0m.ui.spec.ts | TC-S2E8-005 | A | giữ UI (A) | bàn phím số trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' xóa lùi bỏ ký tự ngay trước con trỏ', async ({ page, request }) |
| s2e8/keypad.p0m.ui.spec.ts | TC-S2E8-006 | A | giữ UI (A) | bàn phím số trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' chèn giữa chuỗi theo vị trí con trỏ', async ({ page, request }) |
| s2e8/keypad.p0m.ui.spec.ts | TC-S2E8-007 | A | giữ UI (A) | bàn phím số trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' Xóa hết làm trống ô', async ({ page, request }) => { |
| s2e8/keypad.p0m.ui.spec.ts | TC-S2E8-008 | A | giữ UI (A) | bàn phím số trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' ô trống thì nút Kiểm tra khóa', async ({ page, request }) => { |
| s2e8/keypad.p0m.ui.spec.ts | TC-S2E8-009 | A | giữ UI (A) | bàn phím số trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' đủ 50 ký tự vẫn nhập được', async ({ page, request }) => { |
| s2e8/keypad.p0m.ui.spec.ts | TC-S2E8-010 | A | giữ UI (A) | bàn phím số trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' ký tự thứ 51 không được chèn', async ({ page, request }) => { |
| s2e8/keypad.p0m.ui.spec.ts | TC-S2E8-012 | A | giữ UI (A) | bàn phím số trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' chuỗi dán bị lọc theo bảng phím', async ({ page, request }) =>  |
| s2e8/keypad.p0m.ui.spec.ts | TC-S2E8-012 | A | giữ UI (A) | bàn phím số trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test('S2-E8 bàn phím toán theme C: 6 cột cùng thứ tự, phím ≥ 44, không |
| s2e8/play.p0m.ui.spec.ts | TC-S2E8-056 | A | giữ UI (A) | làm bài luyện trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' mở bài hiện câu 1 với đủ thành phần', async ({ page, request }) |
| s2e8/play.p0m.ui.spec.ts | TC-S2E8-057 | A | giữ UI (A) | làm bài luyện trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' đáp số đúng thì xác nhận và sang câu kế', async ({ page, reques |
| s2e8/play.p0m.ui.spec.ts | TC-S2E8-058 | A | giữ UI (A) | làm bài luyện trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' đáp số sai hiện gợi ý mức 1 và trừ lượt', async ({ page, reques |
| s2e8/play.p0m.ui.spec.ts | TC-S2E8-059 | A | giữ UI (A) | làm bài luyện trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' sai cú pháp báo chỗ sai và không trừ lượt', async ({ page, requ |
| s2e8/play.p0m.ui.spec.ts | TC-S2E8-060 | A | giữ UI (A) | làm bài luyện trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' hết lượt đánh dấu xem lại cùng thầy cô và khóa nhập', async ({  |
| s2e8/play.p0m.ui.spec.ts | TC-S2E8-061 | A | giữ UI (A) | làm bài luyện trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' hết lượt vẫn xem được gợi ý mức 1', async ({ page, request }) = |
| s2e8/play.p0m.ui.spec.ts | TC-S2E8-062 | A | giữ UI (A) | làm bài luyện trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' chip lượt đổi 3, 2, 1, 0 theo từng lần Sai', async ({ page, req |
| s2e8/play.p0m.ui.spec.ts | TC-S2E8-063 | A | giữ UI (A) | làm bài luyện trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' thoát giữa chừng rồi vào lại giữ trạng thái', async ({ page, re |
| s2e8/play.p0m.ui.spec.ts | TC-S2E8-064 | A | giữ UI (A) | làm bài luyện trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' mất mạng khi gửi không trừ lượt và giữ đáp số', async ({ page,  |
| s2e8/play.p0m.ui.spec.ts | TC-S2E8-065 | A | giữ UI (A) | làm bài luyện trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' đang tải hiện khung giữ chỗ', async ({ page, request }) => { |
| s2e8/play.p0m.ui.spec.ts | TC-S2E8-068 | A | giữ UI (A) | làm bài luyện trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' câu đã đúng không làm lại', async ({ page, request }) => { |
| s2e8/play.p0m.ui.spec.ts | TC-S2E8-070 | A | giữ UI (A) | làm bài luyện trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' làm hết 5 câu thì sang kết quả', async ({ page, request }) => { |
| s2e8/play.p0m.ui.spec.ts | TC-S2E8-071 | A | giữ UI (A) | làm bài luyện trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' bài 1 câu: tiến độ một đoạn, câu cuối', async ({ page, request  |
| s2e8/play.p0m.ui.spec.ts | TC-S2E8-072 | A | giữ UI (A) | làm bài luyện trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' bài 20 câu thanh tiến độ 20 đoạn', async ({ page, request }) => |
| s2e8/play.p0m.ui.spec.ts | TC-S2E8-112 | A | giữ UI (A) | làm bài luyện trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' luồng C đầy đủ: đúng, sai cú pháp, sai, hết lượt, khác dạng viế |
| s2e8/result.p0m.ui.spec.ts | TC-S2E8-075 | A | giữ UI (A) | màn kết quả luyện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test(' làm hết thì hiện số câu đúng và câu cần xem lại', async ({ page |
| s2e8/result.p0m.ui.spec.ts | TC-S2E8-077 | A | giữ UI (A) | màn kết quả luyện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test(' chưa làm xong thì báo còn bao nhiêu câu', async ({ page, reques |
| s2e8/result.p0m.ui.spec.ts | TC-S2E8-079 | A | giữ UI (A) | màn kết quả luyện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test(' tới giờ mở thì xem được lời giải', async ({ page, request }) => |
| s2e8/result.p0m.ui.spec.ts | TC-S2E8-081 | A | giữ UI (A) | màn kết quả luyện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test(' chưa đặt giờ mở thì ghi Thầy cô chưa đặt giờ mở', async ({ page |
| s2e8/result.p0m.ui.spec.ts | TC-S2E8-083 | A | giữ UI (A) | màn kết quả luyện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test(' chưa làm câu nào', async ({ page, request }) => { |
| s2e8/result.p0m.ui.spec.ts | TC-S2E8-084 | A | giữ UI (A) | màn kết quả luyện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test(' làm đúng cả 5 câu', async ({ page, request }) => { |
| s2e8/result.p0m.ui.spec.ts | TC-S2E8-085 | A | giữ UI (A) | màn kết quả luyện (di động P0) · chạy thêm mobile-iphone/mobile-pixel | test(' không câu nào đúng', async ({ page, request }) => { |
| s2e9/choice.ui.spec.ts | TC-S2E9-029 | A | giữ UI (A) | trắc nghiệm: chọn, hiển thị | test(' soạn câu trắc nghiệm 4 lựa chọn', async ({ page, request }) =>  |
| s2e9/choice.ui.spec.ts | TC-S2E9-036 | A | giữ UI (A) | trắc nghiệm: chọn, hiển thị | test(' không có lựa chọn đúng bị chặn', async ({ page, request }) => { |
| s2e9/choice.ui.spec.ts | TC-S2E9-038 | A | giữ UI (A) | trắc nghiệm: chọn, hiển thị | test(' lựa chọn trùng nhau bị chặn', async ({ page, request }) => { |
| s2e9/choice.ui.spec.ts | TC-S2E9-030 | A | giữ UI (A) | trắc nghiệm: chọn, hiển thị | test(' chọn đúng thì Đúng', async ({ page, request }) => { |
| s2e9/choice.ui.spec.ts | TC-S2E9-031 | A | giữ UI (A) | trắc nghiệm: chọn, hiển thị | test(' chọn sai trừ lượt, hết lượt thì xem lại cùng thầy cô', async ({ |
| s2e9/choice.ui.spec.ts | TC-S2E9-032 | A | giữ UI (A) | trắc nghiệm: chọn, hiển thị | test(' câu có nhiều lượt: lựa chọn sai bị làm mờ', async ({ page, requ |
| s2e9/choice.ui.spec.ts | TC-S2E9-033 | A | giữ UI (A) | trắc nghiệm: chọn, hiển thị | test(' chưa chọn thì nút Kiểm tra khóa', async ({ page, request }) =>  |
| s2e9/choice.ui.spec.ts | TC-S2E9-042 | A | giữ UI (A) | trắc nghiệm: chọn, hiển thị | test(' bài luyện hỗn hợp: kết quả đếm chung hai kiểu câu', async ({ pa |
| s2e9/choice.ui.spec.ts | TC-S2E9-043 | A | giữ UI (A) · đổi B->A ở rà tay đợt 2 | ẩn-hiện nút "Em nghĩ đáp số này đúng" ở câu trắc nghiệm | test(' nút Em nghĩ đáp số này đúng không hiện ở câu trắc nghiệm', asyn |
| s2e9/variant-export.ui.spec.ts | TC-S2E9-003 | A | giữ UI (A) | biến thể đề, xuất: bấm, hiển thị | test(' biến thể mới mang Cần xem lại và AI soạn', async ({ page, reque |
| s2e9/variant-export.ui.spec.ts | TC-S2E9-014 | A | giữ UI (A) | biến thể đề, xuất: bấm, hiển thị | test(' xuất ảnh PNG của một đề', async ({ page, request }, info) => { |
| s2e9/variant-export.ui.spec.ts | TC-S2E9-015 | A | giữ UI (A) | biến thể đề, xuất: bấm, hiển thị | test(' xuất PDF nhiều đề', async ({ page, request }, info) => { |
| s2e9/variant-export.ui.spec.ts | TC-S2E9-016 | A | giữ UI (A) | biến thể đề, xuất: bấm, hiển thị | test(' kèm lời giải thì in cả hai nhãn', async ({ page, request }, inf |
| s2e9/variant-export.ui.spec.ts | TC-S2E9-018 | A | giữ UI (A) | biến thể đề, xuất: bấm, hiển thị | test(' đề đang giao và khóa phải xác nhận trước khi xuất kèm lời giải' |
| s2e9/variant-export.ui.spec.ts | TC-S2E9-020 | A | giữ UI (A) | biến thể đề, xuất: bấm, hiển thị | test(' tạo liên kết chỉ xem mặc định hạn 7 ngày', async ({ page, reque |
| s2e9/variant-export.ui.spec.ts | TC-S2E9-028 | A | giữ UI (A) | biến thể đề, xuất: bấm, hiển thị | test(' chọn hạn 1 hoặc 30 ngày', async ({ page, request }, info) => { |
| s3e1/classes.ui.spec.ts | TC-S3E1-01 | A | giữ UI (A) | màn Lớp học: nút, hộp xác nhận, hai tab | test('Tạo lớp hợp lệ, nhận mã lớp và trang in  async ({ page, request  |
| s3e1/classes.ui.spec.ts | TC-S3E1-04 | A | giữ UI (A) | màn Lớp học: nút, hộp xác nhận, hai tab | test('Đặt lại mã lớp: mã cũ hết hiệu lực, thành viên giữ nguyên  async |
| s3e1/classes.ui.spec.ts | TC-S3E1-05 | A | giữ UI (A) · đổi B->A ở rà tay đợt 2 | ca A giữ chỗ bấm công tắc "Cho vào lớp bằng mã" cho các ca B đợt 1 | test('Tắt "Cho vào lớp bằng mã" thì mã bị khóa, bật lại thì dùng được |
| s3e1/classes.ui.spec.ts | TC-S3E1-06 | A | giữ UI (A) · đổi B->A ở rà tay đợt 2 | ca A giữ lỗi ô Tên lớp trên form Tạo lớp | test('Tên lớp rỗng  async ({ page, request }) => { |
| s3e1/classes.ui.spec.ts | TC-S3E1-12 | A | giữ UI (A) | màn Lớp học: nút, hộp xác nhận, hai tab | test('Nhập mã đúng, vào trạng thái Chờ duyệt  async ({ page, request } |
| s3e1/classes.ui.spec.ts | TC-S3E1-17 | A | giữ UI (A) | màn Lớp học: nút, hộp xác nhận, hai tab | test('Mã sai  async ({ page, request }) => { |
| s3e1/classes.ui.spec.ts | TC-S3E1-24 | A | giữ UI (A) | màn Lớp học: nút, hộp xác nhận, hai tab | test('Duyệt một em, em thấy bài của lớp  async ({ page, request }) =>  |
| s3e1/classes.ui.spec.ts | TC-S3E1-25 | A | giữ UI (A) | màn Lớp học: nút, hộp xác nhận, hai tab | test('Hai em trùng tên được phân biệt bằng mã người dùng  async ({ pag |
| s3e1/classes.ui.spec.ts | TC-S3E1-27 | A | giữ UI (A) | màn Lớp học: nút, hộp xác nhận, hai tab | test('Cho em rời lớp, dữ liệu cũ vẫn ở Kết quả lớp  async ({ page, req |
| s3e1/classes.ui.spec.ts | TC-S3E1-30 | A | giữ UI (A) | màn Lớp học: nút, hộp xác nhận, hai tab | test('Hai tab cùng duyệt một em  async ({ page, request }) => { |
| s3e1/classes.ui.spec.ts | TC-S3E1-34 | A | giữ UI (A) | màn Lớp học: nút, hộp xác nhận, hai tab | test('Đặt lại mật khẩu, mật khẩu tạm hiện một lần  async ({ page, requ |
| s3e1/classes.ui.spec.ts | TC-S3E1-45 | A | giữ UI (A) · đổi B->A ở rà tay đợt 2 | ca A giữ nút Hủy của hộp xác nhận | test('Hủy ở hộp xác nhận thì không đổi gì  async ({ page, request }) = |
| s3e2/compose.ui.spec.ts | TC-S3E2-01 | A | giữ UI (A) · đổi B->A ở rà tay đợt 2 | ca A giữ chỗ bấm thêm đề / lưu nháp cho các ca B đợt 1 | test(' tạo bộ nháp từ bốn đề và lưu', async ({ page, request }) => { |
| s3e2/compose.ui.spec.ts | TC-S3E2-02 | A | giữ UI (A) · đổi B->A ở rà tay đợt 2 | kéo thả thứ tự đề (ca A giữ chỗ bấm cho ca B đợt 1) | test(' kéo thả Đề 4 lên đầu, thứ tự lưu lại và học sinh thấy đúng thứ |
| s3e2/compose.ui.spec.ts | TC-S3E2-03 | A | giữ UI (A) | soạn bộ bài: kéo thả, xem trước, nút Giao chờ chọn | test(' xem trước như học sinh: làm thử Đề 1, xin gợi ý, không ghi gì,  |
| s3e2/compose.ui.spec.ts | TC-S3E2-10 | A | giữ UI (A) | soạn bộ bài: kéo thả, xem trước, nút Giao chờ chọn | test(' chưa chọn lớp thì nút Giao chờ chọn: MSG-S3-25, gọi thẳng VALID |
| s3e2/schedule.ui.spec.ts | TC-S3E2-15 | A | giữ UI (A) | lịch mở: chế độ giờ, banner cảnh báo | test(' đặt giờ mở cho cả bộ: bốn đề cùng giờ, không cảnh báo vì sau hạ |
| s3e2/schedule.ui.spec.ts | TC-S3E2-20 | A | giữ UI (A) | lịch mở: chế độ giờ, banner cảnh báo | test(' giờ mở sớm hơn hạn: banner đỏ MSG-S3-21 với hai cách sửa, nút G |
| s3e2/schedule.ui.spec.ts | TC-S3E2-21 | A | giữ UI (A) | lịch mở: chế độ giờ, banner cảnh báo | test(' biên cảnh báo: giờ mở bằng đúng hạn không cảnh báo, 19:59 thì c |
| s3e2/schedule.ui.spec.ts | TC-S3E2-32 | A | giữ UI (A) | lịch mở: chế độ giờ, banner cảnh báo | test(' màn Giao liệt kê đúng một đề "Cần xem lại", chưa chọn thì bộ ch |
| s3e2/student.ui.spec.ts | TC-S3E2-29 | A | giữ UI (A) | học sinh xem bộ bài: hiển thị | test(' Bài của em hiện theo lớp → bộ với hạn, tiến độ 2 / 4, giờ mở và |
| s3e2/student.ui.spec.ts | TC-S3E2-05 | A | giữ UI (A) | học sinh xem bộ bài: hiển thị | test(' quá hạn chỉ là nhãn: chip Quá hạn, em vẫn gửi đáp số và được tí |
| s3e2/student.ui.spec.ts | TC-S3E2-30 | A | giữ UI (A) | học sinh xem bộ bài: hiển thị | test(' Chi Mai Đang học 11A1 và 10B thấy hai nhóm, mỗi nhóm chỉ bộ của |
| s3e2/student.ui.spec.ts | TC-S3E2-31 | A | giữ UI (A) | học sinh xem bộ bài: hiển thị | test(' chưa có bài: hiện MSG-S3-27, không có nhóm nào', async ({ page, |
| s3e3/board.p0m.ui.spec.ts | TC-S3E3-01 | A | giữ UI (A) | bảng lớp × bài: ô, màu+chữ, bố cục · chạy thêm mobile-iphone/mobile-pixel | test(' bảng 8 học sinh × 4 đề đúng từng ô theo F-S3-051', async ({ pag |
| s3e3/board.p0m.ui.spec.ts | TC-S3E3-02 | A | giữ UI (A) | bảng lớp × bài: ô, màu+chữ, bố cục · chạy thêm mobile-iphone/mobile-pixel | test(' giao diện không có cột tổng, dòng tổng, phần trăm, điểm hay xếp |
| s3e3/board.ui.spec.ts | TC-S3E3-04 | A | giữ UI (A) | bảng lớp × bài: ô, màu+chữ, bố cục | test(' hàng xếp theo tên theo chữ cái tiếng Việt: Dũng, Đạt, Đức Huy', |
| s3e3/board.ui.spec.ts | TC-S3E3-05 | A | giữ UI (A) | bảng lớp × bài: ô, màu+chữ, bố cục | test(' hai em cùng tên Hoàng Nam liền nhau, xếp theo mã người dùng, mỗ |
| s3e3/board.ui.spec.ts | TC-S3E3-06 | A | giữ UI (A) | bảng lớp × bài: ô, màu+chữ, bố cục | test(' đổi kết quả không đổi thứ tự hàng: Gia Bảo vẫn ở hàng thứ 4', a |
| s3e3/board.ui.spec.ts | TC-S3E3-08 | A | giữ UI (A) | bảng lớp × bài: ô, màu+chữ, bố cục | test(' bộ mặc định là bộ mới giao nhất; chọn bộ khác thì cột đổi theo' |
| s3e3/board.ui.spec.ts | TC-S3E3-09 | A | giữ UI (A) | bảng lớp × bài: ô, màu+chữ, bố cục | test(' bộ 1 đề có 1 cột; bộ 20 đề có 20 cột, cột Học sinh cố định, các |
| s3e3/board.ui.spec.ts | TC-S3E3-11 | A | giữ UI (A) | bảng lớp × bài: ô, màu+chữ, bố cục | test(' chưa ai làm bài của bộ: 8 hàng Chưa làm, MSG-S3-41, không thẻ n |
| s3e3/board.ui.spec.ts | TC-S3E3-12 | A | giữ UI (A) | bảng lớp × bài: ô, màu+chữ, bố cục | test(' lớp không có học sinh Đang học: MSG-S3-42 kèm liên kết tới danh |
| s3e3/board.ui.spec.ts | TC-S3E3-13 | A | giữ UI (A) | bảng lớp × bài: ô, màu+chữ, bố cục | test(' học sinh Chờ duyệt không có hàng và không có trong thân phản hồ |
| s3e3/board.ui.spec.ts | TC-S3E3-15 | A | giữ UI (A) | bảng lớp × bài: ô, màu+chữ, bố cục | test(' đang tải hiện khung chờ dạng bảng; lỗi tải hiện MSG-S3-47 và Th |
| s3e3/board.ui.spec.ts | TC-S3E3-21 | A | giữ UI (A) | bảng lớp × bài: ô, màu+chữ, bố cục | test(' học sinh Đã rời có bài làm: có hàng, chip "đã rời", vẫn xếp the |
| s3e3/evidence.ui.spec.ts | TC-S3E3-22 | A | giữ UI (A) | bằng chứng bài làm: hiển thị | test(' bấm ô Xem lại hiện đủ bằng chứng: đáp số, gợi ý, câu hỏi nguyên |
| s3e3/evidence.ui.spec.ts | TC-S3E3-23 | A | giữ UI (A) | bằng chứng bài làm: hiển thị | test(' ô Chưa làm: "Em chưa mở bài này.", không dữ liệu giả, không nút |
| s3e3/evidence.ui.spec.ts | TC-S3E3-24 | A | giữ UI (A) | bằng chứng bài làm: hiển thị | test(' ô Đang làm chỉ có tương tác đã xảy ra: một gợi ý mức 1, không c |
| s3e3/evidence.ui.spec.ts | TC-S3E3-25 | A | giữ UI (A) | bằng chứng bài làm: hiển thị | test(' học sinh Đã rời: khung chỉ đọc có chip "đã rời", đủ dữ liệu cũ, |
| s3e3/evidence.ui.spec.ts | TC-S3E3-26 | A | giữ UI (A) | bằng chứng bài làm: hiển thị | test(' "Mở bài làm" chỉ đọc, không sinh tương tác', async ({ page, req |
| s3e3/evidence.ui.spec.ts | TC-S3E3-27 | A | giữ UI (A) | bằng chứng bài làm: hiển thị | test(' câu hỏi chứa mã HTML hiển thị như chữ thuần', async ({ page, re |
| s3e3/evidence.ui.spec.ts | TC-S3E3-28 | A | giữ UI (A) | bằng chứng bài làm: hiển thị | test(' câu hỏi 1000 ký tự gập còn 300 kèm "Xem thêm", mở ra đủ nội dun |
| s3e3/evidence.ui.spec.ts | TC-S3E3-32 | A | giữ UI (A) | bằng chứng bài làm: hiển thị | test(' số điện thoại em tự gõ trong câu hỏi: hiện nguyên văn, không tá |
| s3e3/evidence.ui.spec.ts | TC-S3E3-33 | A | giữ UI (A) | bằng chứng bài làm: hiển thị | test(' dữ liệu đổi trong lúc đang xem: Làm mới ở khung cập nhật khung  |
| s3e3/stuck-edge.ui.spec.ts | TC-S3E3-48 | A | giữ UI (A) | thẻ "vướng ở đâu": biên hiển thị | test(' bộ gộp quá thời gian: lùi về dữ liệu thô kèm "Chưa gộp được. Th |
| s3e3/stuck-edge.ui.spec.ts | TC-S3E3-49 | A | giữ UI (A) | thẻ "vướng ở đâu": biên hiển thị | test(' nhận định dẫn tới bài làm không tồn tại bị loại, không có thẻ r |
| s3e3/stuck-edge.ui.spec.ts | TC-S3E3-50 | A | giữ UI (A) | thẻ "vướng ở đâu": biên hiển thị | test(' quá 5 thẻ thì gập: 5 thẻ n lớn nhất, "Còn 2 nhận định khác" mở  |
| s3e3/stuck-edge.ui.spec.ts | TC-S3E3-57 | A | giữ UI (A) | thẻ "vướng ở đâu": biên hiển thị | test(' chưa ai làm thì không có thẻ nhận định, có câu chung "Chưa có b |
| s3e3/stuck.ui.spec.ts | TC-S3E3-34 | A | giữ UI (A) | thẻ "vướng ở đâu": hiển thị | test(' đề đủ bài làm hiện thẻ gộp đúng cấu trúc', async ({ page, reque |
| s3e3/stuck.ui.spec.ts | TC-S3E3-35 | A | giữ UI (A) | thẻ "vướng ở đâu": hiển thị | test(' biên 5: đúng 5 bài làm thì gộp, bộ gộp được gọi một lần cho mỗi |
| s3e3/stuck.ui.spec.ts | TC-S3E3-36 | A | giữ UI (A) | thẻ "vướng ở đâu": hiển thị | test(' biên 4: 4 bài làm thì chỉ dữ liệu thô với MSG-S3-43, bộ gộp khô |
| s3e3/stuck.ui.spec.ts | TC-S3E3-37 | A | giữ UI (A) | thẻ "vướng ở đâu": hiển thị | test(' biên 6: 6 bài làm thì gộp', async ({ page, request }) => { |
| s3e3/stuck.ui.spec.ts | TC-S3E3-38 | A | giữ UI (A) | thẻ "vướng ở đâu": hiển thị | test(' mỗi đề xét riêng ngưỡng: Đề 3 có thẻ, Đề 4 dữ liệu thô trên cùn |
| s3e3/stuck.ui.spec.ts | TC-S3E3-39 | A | giữ UI (A) | thẻ "vướng ở đâu": hiển thị | test(' thẻ sắp theo số bài làm căn cứ giảm dần; cùng n thì đề đứng trư |
| s3e3/stuck.ui.spec.ts | TC-S3E3-40 | A | giữ UI (A) | thẻ "vướng ở đâu": hiển thị | test(' "Xem 6 bài làm" mở đúng 6 bài làm căn cứ; bấm một dòng mở bằng  |
| s3e3/stuck.ui.spec.ts | TC-S3E3-42 | A | giữ UI (A) | thẻ "vướng ở đâu": hiển thị | test(' dưới ngưỡng: gợi ý và câu hỏi thô gom theo bước, có loại, nội d |
| s3e3/stuck.ui.spec.ts | TC-S3E3-43 | A | giữ UI (A) | thẻ "vướng ở đâu": hiển thị | test(' lớp dưới 5 học sinh luôn dữ liệu thô với MSG-S3-44', async ({ p |
| s3e3/stuck.ui.spec.ts | TC-S3E3-44 | A | giữ UI (A) | thẻ "vướng ở đâu": hiển thị | test(' một em làm nhiều lần chỉ tính một bài làm: Đề 4 vẫn 4 bài làm', |
| s3e3/stuck.ui.spec.ts | TC-S3E3-46 | A | giữ UI (A) | thẻ "vướng ở đâu": hiển thị | test(' làm mới sau bài làm thứ 5 thì xuất hiện nhận định và ô chuyển Đ |
| s3e3/stuck.ui.spec.ts | TC-S3E3-47 | A | giữ UI (A) | thẻ "vướng ở đâu": hiển thị | test(' học sinh Đã rời không tính vào ngưỡng: 4 em Đang học + Văn Tú v |
| s3e4/follow.p0m.ui.spec.ts | TC-S3E4-14 | A | giữ UI (A) | theo dõi bài trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' danh sách em chưa làm: chỉ em chưa có tương tác nào, kèm mã ngư |
| s3e4/follow.p0m.ui.spec.ts | TC-S3E4-15 | A | giữ UI (A) | theo dõi bài trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' Mở ngay: lời giải mở theo giờ máy chủ, chip Đã mở, một EARLY_RE |
| s3e4/follow.p0m.ui.spec.ts | TC-S3E4-16 | A | giữ UI (A) | theo dõi bài trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' Đổi giờ sang giờ tương lai: vẫn Sắp mở, một RELEASE_TIME_CHANGE |
| s3e4/follow.p0m.ui.spec.ts | TC-S3E4-18 | A | giữ UI (A) | theo dõi bài trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' Mở ngay khi bộ chưa đặt giờ: lời giải mở, EARLY_RELEASE ghi giờ |
| s3e4/follow.p0m.ui.spec.ts | TC-S3E4-26 | A | giữ UI (A) | theo dõi bài trên điện thoại · chạy thêm mobile-iphone/mobile-pixel | test(' Hủy ở hộp xác nhận: lời giải vẫn khóa, không AuditEvent, không  |
| s3e4/notify.ui.spec.ts | TC-S3E4-28 | A | giữ UI (A) | thông báo: chuông, ẩn-hiện | test(' giao bộ: em Đang học thấy chấm 1 và thông báo "bài mới" Chưa đọ |
| s3e4/notify.ui.spec.ts | TC-S3E4-29 | A | giữ UI (A) | thông báo: chuông, ẩn-hiện | test(' Mở ngay: thông báo "lời giải đã mở" Chưa đọc, bấm vào tới bộ bà |
| s3e4/notify.ui.spec.ts | TC-S3E4-30 | A | giữ UI (A) | thông báo: chuông, ẩn-hiện | test(' mở một thông báo thì thành Đã đọc, chấm giảm từ 2 xuống 1, thôn |
| s3e4/notify.ui.spec.ts | TC-S3E4-31 | A | giữ UI (A) | thông báo: chuông, ẩn-hiện | test(' "Đánh dấu tất cả đã đọc": cả 5 thành Đã đọc, chấm biến mất, thô |
| s3e4/notify.ui.spec.ts | TC-S3E4-33 | A | giữ UI (A) | thông báo: chuông, ẩn-hiện | test(' em được duyệt sau khi giao không nhận thông báo cũ nhưng vẫn th |
| s3e4/notify.ui.spec.ts | TC-S3E4-39 | A | giữ UI (A) | thông báo: chuông, ẩn-hiện | test(' học sinh Đã rời không nhận thông báo "bài mới"', async ({ page, |
| s3e4/overview.ui.spec.ts | TC-S3E4-01 | A | giữ UI (A) | tổng quan lớp: hiển thị | test(' ba thẻ đúng số liệu trên web: bài đang giao, lời giải đang khóa |
| s3e4/overview.ui.spec.ts | TC-S3E4-03 | A | giữ UI (A) | tổng quan lớp: hiển thị | test(' chưa có lớp: lời mời MSG-S3-66 kèm nút Tạo lớp, không có ba thẻ |
| s3e4/overview.ui.spec.ts | TC-S3E4-04 | A | giữ UI (A) | tổng quan lớp: hiển thị | test(' có lớp nhưng không có việc: MSG-S3-65, không thẻ nào mang số 0' |
| s3e4/overview.ui.spec.ts | TC-S3E4-05 | A | giữ UI (A) | tổng quan lớp: hiển thị | test(' giao thêm "Bộ bài tuần 16": tổng quan thêm một dòng với 8 em ch |
| s3e4/overview.ui.spec.ts | TC-S3E4-06 | A | giữ UI (A) | tổng quan lớp: hiển thị | test(' bộ quá hạn vẫn nằm trong "Bài đang giao" kèm nhãn Quá hạn', asy |
| s3e4/overview.ui.spec.ts | TC-S3E4-07 | A | giữ UI (A) | tổng quan lớp: hiển thị | test(' lời giải đã mở: rời thẻ "Lời giải đang khóa", có chip Đã mở ở " |
| s3e4/overview.ui.spec.ts | TC-S3E4-09 | A | giữ UI (A) | tổng quan lớp: hiển thị | test(' không em nào chờ duyệt: thẻ "Em chờ duyệt" ẩn, hai thẻ còn lại  |
| s3e4/overview.ui.spec.ts | TC-S3E4-13 | A | giữ UI (A) | tổng quan lớp: hiển thị | test(' bộ Nháp không xuất hiện ở thẻ nào', async ({ page, request }) = |
| s3e5/account.ui.spec.ts | TC-S3E5-53 | B | để sau fix-po27 (TC thuộc danh sách PO-74 / PO-DECISIONS 27/11; là ca A giữ chỗ bấm cho TC-S3E5-33) | Cài đặt › Xóa tài khoản: nút khóa/mở, hộp nhập | test('Học sinh xóa tài khoản của mình  async ({ page, request }) => { |
| s3e5/account.ui.spec.ts | TC-S3E5-55 | A | giữ UI (A) | Cài đặt › Xóa tài khoản: nút khóa/mở, hộp nhập | test('Văn bản thông báo riêng tư nêu chính sách lưu và xóa  async ({ p |
| s3e5/account.ui.spec.ts | TC-S3E5-59 | A | giữ UI (A) | Cài đặt › Xóa tài khoản: nút khóa/mở, hộp nhập | test('Chưa gõ XÓA thì nút xác nhận bị khóa  async ({ page, request })  |
| s3e5/ai.ui.spec.ts | TC-S3E5-40 | A | giữ UI (A) | công tắc AI của lớp: hiển thị | test('Tắt công tắc thì khung trợ lý ẩn ở học sinh  async ({ page, brow |
| s3e5/ai.ui.spec.ts | TC-S3E5-42 | A | giữ UI (A) | công tắc AI của lớp: hiển thị | test('Luồng học cơ bản vẫn chạy khi tắt  async ({ page, request }) =>  |
| s3e5/ai.ui.spec.ts | TC-S3E5-45 | A | giữ UI (A) | công tắc AI của lớp: hiển thị | test('Lớp mới mặc định bật  async ({ page, request }) => { |
| s3e5/privacy.ui.spec.ts | TC-S3E5-16 | A | giữ UI (A) · đổi B->A ở rà tay đợt 2 | màn Đăng ký: tick riêng tư, nhãn phiên bản, bấm Đăng ký | test('Đăng ký có tick thì lưu cờ và phiên bản  async ({ page, request |
| s3e5/privacy.ui.spec.ts | TC-S3E5-18 | A | giữ UI (A) | thông báo riêng tư: hiển thị, tick | test('Tài khoản MVP chuyển sang thấy thông báo ở lần vào đầu tiên  asy |
| s3e5/privacy.ui.spec.ts | TC-S3E5-19 | A | giữ UI (A) | thông báo riêng tư: hiển thị, tick | test('Phiên bản văn bản tăng thì hiện lại một lần  async ({ page, requ |
| s3e5/privacy.ui.spec.ts | TC-S3E5-20 | A | giữ UI (A) | thông báo riêng tư: hiển thị, tick | test('Văn bản nêu đủ các ý bắt buộc  async ({ page, request }) => { |
| s3e5/privacy.ui.spec.ts | TC-S3E5-21 | A | giữ UI (A) | thông báo riêng tư: hiển thị, tick | test('Đã đọc bản hiện hành thì không hiện lại  async ({ page, request  |
| s3e5/privacy.ui.spec.ts | TC-S3E5-22 | A | giữ UI (A) | thông báo riêng tư: hiển thị, tick | test('Đăng ký không tick bị từ chối  async ({ page, request }, testInf |
| s3e6/archive.ui.spec.ts | TC-S3E6-15 | A | giữ UI (A) | lưu trữ lớp: nút, hộp xác nhận | test('Lưu trữ lớp: trạng thái, mã và hiển thị của học sinh  async ({ p |
| s3e6/archive.ui.spec.ts | TC-S3E6-17 | A | giữ UI (A) · đổi B->A ở rà tay đợt 2 | ca A giữ nút Bỏ lưu trữ + hộp xác nhận cho ca B đợt 1 | test('Bỏ lưu trữ thì lớp về Đang dạy với mã lớp mới  async ({ page, re |
| s3e6/archive.ui.spec.ts | TC-S3E6-26 | A | giữ UI (A) | lưu trữ lớp: nút, hộp xác nhận | test('Hủy ở hộp lưu trữ không đổi gì  async ({ page, request }) => { |
| s3e6/export.ui.spec.ts | TC-S3E6-11 | A | giữ UI (A) | xuất kết quả: hiển thị | test('Xuất bảng tuần 15 đúng hàng và cột  async ({ page, request }) => |
| s3e6/export.ui.spec.ts | TC-S3E6-12 | A | giữ UI (A) | xuất kết quả: hiển thị | test('Tên tệp không chứa tên học sinh  async ({ page, request }) => { |
| s3e6/export.ui.spec.ts | TC-S3E6-13 | A | giữ UI (A) | xuất kết quả: hiển thị | test('Ô bắt đầu bằng dấu công thức được vô hiệu  async ({ page, reques |
| s3e6/present.ui.spec.ts | TC-S3E6-01 | A | giữ UI (A) | trình chiếu kết quả: bố cục | test('Bấm "Chữa bài này" mở Trình chiếu đúng đề và bước  async ({ page |
| s3e6/present.ui.spec.ts | TC-S3E6-02 | A | giữ UI (A) | trình chiếu kết quả: bố cục | test('Lời giải còn khóa thì hỏi lại trước khi chiếu  async ({ page, re |
| s3e6/present.ui.spec.ts | TC-S3E6-05 | A | giữ UI (A) | trình chiếu kết quả: bố cục | test('Thẻ dữ liệu thô không có nút  async ({ page, request }) => { |
| s3e6/present.ui.spec.ts | TC-S3E6-06 | A | giữ UI (A) | trình chiếu kết quả: bố cục | test('Đề đã sửa phiên bản, bước không còn  async ({ page, request }) = |
| theme/icon.ui.spec.ts | (không gắn mã TC) | A | giữ UI (A) | biểu tượng/giao diện (so ảnh) | test('Icon ở 4 theme: một bộ Material Symbols Rounded tự lưu, màu theo |
| theme/icon.ui.spec.ts | (không gắn mã TC) | A | giữ UI (A) | biểu tượng/giao diện (so ảnh) | test('Icon trên thanh điều hướng học sinh (mọi thiết bị): font tự lưu, |
| theme/theme.ui.spec.ts | (không gắn mã TC) | A | giữ UI (A) | giao diện sáng-tối, token (so ảnh) | test('Học sinh đổi theme trong Cài đặt, tải lại vẫn giữ, lưu theo tài  |
| theme/theme.ui.spec.ts | (không gắn mã TC) | A | giữ UI (A) | giao diện sáng-tối, token (so ảnh) | test('Giáo viên có Cài đặt, mặc định C; cảm ứng có vùng chạm ≥ 44 px', |
| theme/theme.ui.spec.ts | (không gắn mã TC) | A | giữ UI (A) | giao diện sáng-tối, token (so ảnh) | test('Chưa đăng nhập: lưu trên thiết bị (localStorage), tải lại vẫn gi |
| theme/theme.ui.spec.ts | (không gắn mã TC) | A | giữ UI (A) | giao diện sáng-tối, token (so ảnh) | test('Ảnh màn MVP (Kho đề, Chi tiết đề) ở 4 theme; chữ ≥ 14 px, không  |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.0 | 2026-11-27 | Phân loại đầu tiên, chuyển 26 ca B | runner-e2e-split |
| v1.1 | 2026-11-27 | Rà tay 32 ca B: chuyển 11 sang API, đổi 20 sang A, 1 để sau fix-po27 (TC-S3E5-53) | runner-e2e-split2 |
