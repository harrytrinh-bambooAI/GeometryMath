package vn.geo.server.assessment.api;

import java.util.UUID;

/**
 * Điểm mở rộng của job xoá theo tài khoản và theo năm học (BR-S5E4-28): mỗi phần giữ dữ liệu bài
 * nộp (bản đọc chữ — T-261128-59; đề xuất AI, đánh giá tiêu chí — T-261128-61; điểm duyệt ẩn danh —
 * T-261128-60) cài một bean. Job ảnh (T-261128-57) gọi mọi bean rồi xoá ảnh, ghi AuditEvent số
 * lượng.
 */
public interface SubmissionDataEraser {

  /** Tên ngắn của phần dữ liệu, ví dụ "ocr". */
  String part();

  /** Xoá (hoặc ẩn danh) dữ liệu bài nộp của học sinh đã xoá tài khoản; trả số hàng đã đụng. */
  int eraseStudent(UUID orgId, UUID studentId);

  /**
   * Xoá dữ liệu phái sinh của năm học vừa kết thúc (dạng {@code 2026-2027}); trả số hàng đã xoá.
   */
  default int eraseSchoolYear(String schoolYear) {
    return 0;
  }

  /**
   * Như {@link #eraseSchoolYear(String)} nhưng chỉ trong một bài có chấm khi {@code
   * assignmentScope} khác null (móc kiểm thử chạy song song không xoá dữ liệu của ca khác;
   * T-261128-60). Mặc định: không phạm vi → bản toàn hệ thống; có phạm vi → không xoá gì.
   */
  default int eraseSchoolYear(String schoolYear, UUID assignmentScope) {
    return assignmentScope == null ? eraseSchoolYear(schoolYear) : 0;
  }
}
