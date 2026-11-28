package vn.geo.server.aigrade.internal.shadow.service;

import java.util.UUID;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import vn.geo.server.aigrade.internal.shadow.repo.ShadowRepository;
import vn.geo.server.assessment.api.SubmissionDataEraser;

/**
 * Phần "ai_shadow" của job xoá (BR-S5E4-28): cặp so sánh chạy song song ẩn danh theo tài khoản học
 * sinh (giữ cho số liệu lớp như điểm duyệt), xoá theo năm học. Hành động giáo viên ({@code
 * ai_review_action}) đi theo đề xuất (khoá ngoại xoá dây chuyền).
 */
@Component
@Order(21)
public class ShadowEraser implements SubmissionDataEraser {

  private final ShadowRepository repo;

  public ShadowEraser(ShadowRepository repo) {
    this.repo = repo;
  }

  @Override
  public String part() {
    return "ai_shadow";
  }

  @Override
  public int eraseStudent(UUID orgId, UUID studentId) {
    return repo.anonymizeStudent(orgId, studentId);
  }

  @Override
  public int eraseSchoolYear(String schoolYear) {
    return repo.deleteOfYear(schoolYear, null);
  }

  @Override
  public int eraseSchoolYear(String schoolYear, UUID assignmentScope) {
    return repo.deleteOfYear(schoolYear, assignmentScope);
  }
}
