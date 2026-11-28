package vn.geo.server.tracking.internal.practice.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.tracking.internal.practice.service.PracticeService;

/**
 * Bài luyện điền đáp số: học sinh làm bài, xem kết quả, báo "Em nghĩ đáp số này đúng"; giáo viên
 * đọc và quyết định báo. Mọi phản hồi mang {@code Cache-Control: no-store} (D-GEO-145). Trường và
 * người gọi lấy từ thẻ.
 */
@RestController
public class PracticeController {

  /** Chỉ hai trường này được dùng; trường khác thiết bị gửi (tol, type…) bị bỏ qua. */
  public record AttemptBody(String answer, String requestKey) {}

  public record ReportBody(String note) {}

  public record DecisionBody(String decision) {}

  private final PracticeService service;

  public PracticeController(PracticeService service) {
    this.service = service;
  }

  @NoStore
  @GetMapping("/api/v1/student/practice-sets")
  public Map<String, List<PracticeService.SetLine>> mine() {
    return Map.of("items", service.mySets());
  }

  @NoStore
  @GetMapping("/api/v1/student/practice-sets/{id}")
  public PracticeService.PlayView open(@PathVariable UUID id) {
    return service.open(id);
  }

  @NoStore
  @GetMapping("/api/v1/student/practice-sets/{id}/result")
  public PracticeService.ResultView result(@PathVariable UUID id) {
    return service.result(id);
  }

  @NoStore
  @PostMapping("/api/v1/student/practice-items/{assignmentId}/attempts")
  public PracticeService.AttemptView attempt(
      @PathVariable UUID assignmentId, @RequestBody AttemptBody body) {
    return service.submit(assignmentId, body.answer(), body.requestKey());
  }

  @NoStore
  @PostMapping("/api/v1/student/practice-items/{assignmentId}/report")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, String> report(
      @PathVariable UUID assignmentId, @RequestBody(required = false) ReportBody body) {
    return service.report(assignmentId, body == null ? null : body.note());
  }

  @NoStore
  @GetMapping("/api/v1/teacher/practice-reports")
  public Map<String, List<PracticeService.ReportView>> reports() {
    return Map.of("items", service.reports());
  }

  @NoStore
  @GetMapping("/api/v1/teacher/practice-reports/metrics")
  public Map<String, Long> metrics() {
    return service.metrics();
  }

  @NoStore
  @PostMapping("/api/v1/teacher/practice-reports/{id}/decision")
  public PracticeService.ReportView decide(@PathVariable UUID id, @RequestBody DecisionBody body) {
    return service.decide(id, body.decision());
  }
}
