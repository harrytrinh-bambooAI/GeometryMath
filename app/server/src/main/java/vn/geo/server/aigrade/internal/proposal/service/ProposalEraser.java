package vn.geo.server.aigrade.internal.proposal.service;

import java.util.UUID;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import vn.geo.server.aigrade.internal.proposal.repo.ProposalRepository;
import vn.geo.server.assessment.api.SubmissionDataEraser;

/**
 * Phần "ai_grade" của job xoá (BR-S5E4-28): đề xuất AI, đánh giá tiêu chí, tín hiệu engine theo tài
 * khoản học sinh và theo năm học (cột academic_year). Chạy trước phần "submission".
 */
@Component
@Order(20)
public class ProposalEraser implements SubmissionDataEraser {

  private final ProposalRepository repo;

  public ProposalEraser(ProposalRepository repo) {
    this.repo = repo;
  }

  @Override
  public String part() {
    return "ai_grade";
  }

  @Override
  public int eraseStudent(UUID orgId, UUID studentId) {
    return repo.deleteOfStudent(orgId, studentId);
  }

  @Override
  public int eraseSchoolYear(String schoolYear) {
    return repo.deleteOfYear(schoolYear);
  }
}
