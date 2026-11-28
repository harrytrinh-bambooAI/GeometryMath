package vn.geo.server.tracking.internal.errors.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.tracking.internal.errors.service.ClassErrorService;

/**
 * "Cả lớp hay sai ở đâu" (US-S5E5-02) và "Nên nhắc lại trên lớp" (US-S5E5-03) của bài có chấm. Sau
 * cờ {@code geo.s5.grading.enabled} (tắt: 404). Chỉ giáo viên chủ bài; học sinh FORBIDDEN.
 */
@RestController
@ConditionalOnProperty(name = "geo.s5.grading.enabled", havingValue = "true")
public class ClassErrorController {

  private final ClassErrorService service;

  public ClassErrorController(ClassErrorService service) {
    this.service = service;
  }

  @NoStore
  @GetMapping("/api/v1/teacher/graded-assignments/{id}/error-summary")
  public Map<String, Object> summary(@PathVariable UUID id) {
    return service.summary(id);
  }

  @NoStore
  @GetMapping("/api/v1/teacher/graded-assignments/{id}/error-summary/{n}/suggestions")
  public Map<String, Object> suggestions(@PathVariable UUID id, @PathVariable int n) {
    return service.suggestions(id, n);
  }
}
