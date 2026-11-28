package vn.geo.server.assessment.internal.returned.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.assessment.internal.returned.service.ReturnService;
import vn.geo.server.common.web.NoStore;

/**
 * Trả bài và kết quả của học sinh (US-S5E4-07; C5-06). Sau cờ {@code geo.s5.grading.enabled}. Đặc
 * tả ghi {@code /api/graded-assignments/{id}/return}, {@code …/solution}; app theo quy ước vai:
 * {@code /api/v1/teacher/graded-assignments/{id}/return[/undo]}, {@code
 * /api/v1/student/graded-assignments/{id}/solution}, {@code /api/v1/submissions/{id}/result}.
 */
@RestController
@ConditionalOnProperty(name = "geo.s5.grading.enabled", havingValue = "true")
public class ReturnController {

  private final ReturnService service;

  public ReturnController(ReturnService service) {
    this.service = service;
  }

  @NoStore
  @PostMapping("/api/v1/teacher/graded-assignments/{id}/return")
  public Map<String, Object> returnAll(@PathVariable UUID id) {
    return service.returnAll(id);
  }

  @NoStore
  @PostMapping("/api/v1/teacher/graded-assignments/{id}/return/undo")
  public Map<String, Object> undo(@PathVariable UUID id) {
    return service.undo(id);
  }

  @NoStore
  @GetMapping("/api/v1/teacher/graded-assignments/{id}/return")
  public Map<String, Object> status(@PathVariable UUID id) {
    return service.status(id);
  }

  @NoStore
  @GetMapping("/api/v1/submissions/{id}/result")
  public Map<String, Object> result(@PathVariable UUID id) {
    return service.result(id);
  }

  @NoStore
  @GetMapping("/api/v1/student/graded-assignments/{id}/solution")
  public Map<String, Object> solution(@PathVariable UUID id) {
    return service.solution(id);
  }
}
