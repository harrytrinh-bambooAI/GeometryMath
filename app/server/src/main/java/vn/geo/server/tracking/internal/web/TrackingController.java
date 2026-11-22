package vn.geo.server.tracking.internal.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.tracking.internal.service.TrackingService;

/**
 * Hỏi đáp, dữ liệu học tập của học sinh, Theo dõi lớp. Trường lấy từ thẻ; trường trong thân bị bỏ
 * qua.
 */
@RestController
public class TrackingController {

  /** orgId (nếu thiết bị gửi) bị bỏ qua: trường luôn lấy từ thẻ (BR-GEO-111, US-GEO-104-SC4). */
  public record AskBody(String question, UUID requestId) {}

  private final TrackingService service;

  public TrackingController(TrackingService service) {
    this.service = service;
  }

  @NoStore
  @PostMapping("/api/v1/student/assignments/{id}/questions")
  public TrackingService.Asked ask(@PathVariable UUID id, @RequestBody AskBody b) {
    return service.ask(id, b.question(), b.requestId());
  }

  @NoStore
  @GetMapping("/api/v1/questions/{id}/answer")
  public TrackingService.SavedAnswer answer(@PathVariable long id) {
    return service.answer(id);
  }

  @NoStore
  @GetMapping("/api/v1/students/{userId}/questions")
  public Map<String, List<TrackingService.Question>> questions(@PathVariable UUID userId) {
    return Map.of("items", service.questionsOf(userId));
  }

  @NoStore
  @GetMapping("/api/v1/students/{userId}/progress")
  public Map<String, List<TrackingService.StudentProgress>> progress(@PathVariable UUID userId) {
    return Map.of("items", service.progressOf(userId));
  }

  @NoStore
  @GetMapping("/api/v1/teacher/assignments/{id}/monitor")
  public TrackingService.Monitor monitor(@PathVariable UUID id) {
    return service.monitor(id);
  }
}
