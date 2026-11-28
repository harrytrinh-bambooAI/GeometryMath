package vn.geo.server.assessment.internal.submit.service;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import vn.geo.server.assessment.internal.ocr.service.OcrService;
import vn.geo.server.assessment.internal.submit.repo.SubmissionRepository;

/**
 * Phần việc của móc kiểm thử nộp bài (chỉ profile dev/test; T-261128-59): dời hạn nộp theo giờ máy
 * chủ, đếm dữ liệu bài nộp, prompt đọc chữ thật sự gửi AI của một trang.
 */
@Service
@Profile({"dev", "test"})
public class SubmitHookSupport {

  private final SubmissionRepository repo;
  private final OcrService ocr;
  private final Clock clock;

  public SubmitHookSupport(SubmissionRepository repo, OcrService ocr, Clock clock) {
    this.repo = repo;
    this.ocr = ocr;
    this.clock = clock;
  }

  public Map<String, Object> due(
      UUID orgId, UUID assignmentId, Long secondsFromNow, Boolean allowLate) {
    Instant due = clock.instant().plusSeconds(secondsFromNow == null ? 0 : secondsFromNow);
    int n = repo.setDue(orgId, assignmentId, due, allowLate);
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("updated", n);
    m.put("dueAt", due.toString());
    return m;
  }

  public Map<String, Object> state(UUID orgId, UUID submissionId, UUID studentId) {
    return repo.state(orgId, submissionId, studentId);
  }

  public Map<String, Object> count(UUID orgId, UUID assignmentId, UUID studentId) {
    return Map.of("submissions", repo.countOf(orgId, assignmentId, studentId));
  }

  public Map<String, Object> prompt(UUID orgId, UUID versionId, int pageNo) {
    byte[] img = ocr.aiImage(orgId, versionId, pageNo);
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("prompt", OcrService.prompt(pageNo, img));
    m.put("jpeg", img.length > 2 && (img[0] & 0xFF) == 0xFF && (img[1] & 0xFF) == 0xD8);
    return m;
  }
}
