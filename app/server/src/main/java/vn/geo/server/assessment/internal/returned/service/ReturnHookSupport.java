package vn.geo.server.assessment.internal.returned.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import vn.geo.server.assessment.internal.review.repo.GradingRepository;

/**
 * Phần việc của móc kiểm thử chấm/trả bài (chỉ profile dev/test; T-261128-60): lùi giờ lượt trả
 * bài, đặt giờ công bố cố định theo đồng hồ CSDL, đếm điểm duyệt của bài nộp.
 */
@Service
@Profile({"dev", "test"})
public class ReturnHookSupport {

  private final GradingRepository repo;

  public ReturnHookSupport(GradingRepository repo) {
    this.repo = repo;
  }

  public Map<String, Object> age(UUID orgId, UUID assignmentId, long seconds) {
    return Map.of("updated", repo.ageReturn(orgId, assignmentId, seconds));
  }

  public Map<String, Object> releaseIn(UUID orgId, UUID assignmentId, long seconds) {
    return Map.of("updated", repo.releaseIn(orgId, assignmentId, seconds));
  }

  public Map<String, Object> grades(UUID orgId, UUID submissionId) {
    int[] s = repo.gradeState(orgId, submissionId);
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("grades", s[0]);
    m.put("withStudent", s[1]);
    return m;
  }
}
