package vn.geo.server.assessment.internal.review.service;

import java.util.UUID;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import vn.geo.server.assessment.api.SubmissionDataEraser;
import vn.geo.server.assessment.internal.review.repo.GradingRepository;

/**
 * Phần "grade" của job xoá (BR-S5E4-28; T-261128-60): học sinh đã xoá tài khoản → {@code
 * approved_grade} giữ dạng ẩn danh (bỏ {@code student_id}) cho số liệu lớp. Điểm chính thức không
 * xoá theo năm học (job năm học chỉ xoá ảnh và bản đọc chữ).
 */
@Component
@Order(50)
public class GradeEraser implements SubmissionDataEraser {

  private final GradingRepository repo;

  public GradeEraser(GradingRepository repo) {
    this.repo = repo;
  }

  @Override
  public String part() {
    return "grade";
  }

  @Override
  public int eraseStudent(UUID orgId, UUID studentId) {
    return repo.anonymize(orgId, studentId);
  }
}
