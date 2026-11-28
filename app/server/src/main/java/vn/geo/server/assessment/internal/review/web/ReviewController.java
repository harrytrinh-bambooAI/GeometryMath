package vn.geo.server.assessment.internal.review.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.assessment.internal.review.service.ReviewService;
import vn.geo.server.assessment.internal.review.service.ReviewService.GradeRequest;
import vn.geo.server.common.web.NoStore;

/**
 * Chấm tay C5-05 (US-S5E4-06). Sau cờ {@code geo.s5.grading.enabled}. Đặc tả ghi {@code POST
 * /api/submissions/{id}/grade}; app: {@code /api/v1/submissions/{id}/grade} {version,
 * criteria[{ord, points}], comment}.
 */
@RestController
@ConditionalOnProperty(name = "geo.s5.grading.enabled", havingValue = "true")
public class ReviewController {

  private final ReviewService service;

  public ReviewController(ReviewService service) {
    this.service = service;
  }

  @NoStore
  @GetMapping("/api/v1/teacher/grading/submissions/{id}")
  public Map<String, Object> get(@PathVariable UUID id) {
    return service.get(id);
  }

  @NoStore
  @PostMapping("/api/v1/submissions/{id}/grade")
  public Map<String, Object> grade(
      @PathVariable UUID id, @RequestBody(required = false) GradeRequest body) {
    return service.approve(id, body);
  }

  @NoStore
  @GetMapping("/api/v1/submissions/{id}/grade/history")
  public Map<String, Object> history(@PathVariable UUID id) {
    return service.history(id);
  }
}
