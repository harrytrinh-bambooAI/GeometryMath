package vn.geo.server.assessment.api;

import java.time.Instant;
import java.util.UUID;

/**
 * Cổng kho ảnh bài làm (US-S5E4-08, PLAN_code-s5 P6). Ảnh gốc và phái sinh lưu MÃ HOÁ (AES-256-GCM,
 * khoá máy chủ quản lý, tách theo môi trường); EXIF/XMP bị gỡ trước khi lưu, băm tính sau khi gỡ
 * (BR-S5E4-25, 27). Ảnh chỉ ra ngoài qua URL ký hạn 5 phút (BR-S5E4-26) hoặc qua {@link #forAi} cho
 * task {@code grade_ocr}/{@code grade} (QS5-30). Bản cài đặt dev/test: tệp cục bộ; bản S3 tại Việt
 * Nam bật bằng cấu hình prod (QS5-18). Chủ: T-261128-57; T-261128-59 gọi khi học sinh nộp.
 */
public interface ImageStore {

  /** Ảnh một trang của một bài nộp; {@code bytes} là tệp gốc JPEG/PNG (HEIC đã đổi ở máy khách). */
  record PutRequest(
      UUID orgId,
      UUID assignmentId,
      UUID submissionId,
      int pageNo,
      UUID studentId,
      byte[] bytes,
      String contentType) {}

  /** Kết quả lưu: băm SHA-256 (hex) của ảnh ĐÃ gỡ metadata. */
  record Stored(UUID id, String sha256, int bytes, String contentType) {}

  /** URL ký (đường dẫn tương đối của máy chủ) và thời điểm hết hạn. */
  record SignedUrl(String url, Instant expiresAt) {}

  /**
   * Gỡ EXIF/XMP, mã hoá, lưu ảnh trang {@code pageNo} (ghi đè ảnh cũ cùng trang). Lớp và năm học
   * lấy từ bài có chấm. Ảnh không phải JPEG/PNG hợp lệ: VALIDATION_FAILED.
   */
  Stored put(PutRequest request);

  /**
   * URL ký 5 phút cho ảnh trang {@code pageNo} của bài nộp, cấp cho giáo viên chủ lớp hoặc chính
   * học sinh nộp (người gọi lấy từ ngữ cảnh). Ngoài quan hệ hoặc không có ảnh: NOT_FOUND.
   */
  SignedUrl signedUrl(UUID submissionId, int pageNo);

  /** Bản đã gỡ EXIF để gửi nhà cung cấp AI (BR-S5E4-29); không kèm danh tính. */
  byte[] forAi(UUID orgId, UUID submissionId, int pageNo);

  /** Xoá mọi ảnh của một bài nộp (đối tượng trong kho và siêu dữ liệu); trả số ảnh đã xoá. */
  int deleteSubmission(UUID orgId, UUID submissionId);
}
