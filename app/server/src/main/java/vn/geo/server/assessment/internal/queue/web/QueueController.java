package vn.geo.server.assessment.internal.queue.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.assessment.internal.queue.service.QueueService;
import vn.geo.server.common.web.NoStore;

/**
 * Hàng chờ chấm C5-04 (US-S5E4-05). Sau cờ {@code geo.s5.grading.enabled}: tắt thì 404. Đặc tả ghi
 * {@code /api/grading/queue}; app dùng quy ước {@code /api/v1/teacher/grading/queue}.
 */
@RestController
@ConditionalOnProperty(name = "geo.s5.grading.enabled", havingValue = "true")
public class QueueController {

  private final QueueService service;

  public QueueController(QueueService service) {
    this.service = service;
  }

  @NoStore
  @GetMapping("/api/v1/teacher/grading/queue")
  public Map<String, Object> queue(
      @RequestParam(required = false) UUID classId,
      @RequestParam(required = false) UUID assignmentId) {
    return service.queue(classId, assignmentId);
  }

  @NoStore
  @GetMapping("/api/v1/teacher/grading/queue/summary")
  public Map<String, Object> summary() {
    return service.summary();
  }
}
