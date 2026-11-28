package vn.geo.server.assessment.internal.ocr.service;

import java.util.UUID;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import vn.geo.server.assessment.api.SubmissionDataEraser;
import vn.geo.server.assessment.internal.ocr.repo.OcrRepository;

/** Phần "ocr" của job xoá (BR-S5E4-28): bản đọc chữ theo tài khoản và theo năm học. */
@Component
@Order(10)
public class OcrEraser implements SubmissionDataEraser {

  private final OcrRepository repo;

  public OcrEraser(OcrRepository repo) {
    this.repo = repo;
  }

  @Override
  public String part() {
    return "ocr";
  }

  @Override
  public int eraseStudent(UUID orgId, UUID studentId) {
    return repo.deleteOfStudent(orgId, studentId);
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
