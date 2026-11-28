package vn.geo.server.assessment.internal.submit.service;

import java.util.UUID;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import vn.geo.server.assessment.api.SubmissionDataEraser;
import vn.geo.server.assessment.internal.submit.repo.SubmissionRepository;

/**
 * Phần "submission" của job xoá (BR-S5E4-28): (a) học sinh đã xoá tài khoản → xoá lượt tải dở, cờ
 * đã đọc thông báo riêng tư, bỏ khóa lũy đẳng sinh trên máy (khung bài nộp giữ cho điểm duyệt ẩn
 * danh, T-261128-60); (b) hết năm học → xoá bài nộp của năm đó (phiên bản, bản đọc chữ theo khoá
 * ngoại). Chạy sau phần "ocr".
 */
@Component
@Order(100)
public class SubmissionEraser implements SubmissionDataEraser {

  private final SubmissionRepository repo;

  public SubmissionEraser(SubmissionRepository repo) {
    this.repo = repo;
  }

  @Override
  public String part() {
    return "submission";
  }

  @Override
  public int eraseStudent(UUID orgId, UUID studentId) {
    return repo.deleteUploadsOf(orgId, studentId)
        + repo.deleteAcksOf(orgId, studentId)
        + repo.scrubVersionsOf(orgId, studentId);
  }

  @Override
  public int eraseSchoolYear(String schoolYear) {
    return repo.deleteOfYear(schoolYear);
  }
}
