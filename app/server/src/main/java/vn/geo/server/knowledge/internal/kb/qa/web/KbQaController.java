package vn.geo.server.knowledge.internal.kb.qa.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.knowledge.internal.kb.qa.service.KbQaService;
import vn.geo.server.knowledge.internal.kb.qa.service.KbQaService.AskRequest;

/**
 * Hỏi đáp theo kho (US-S5E3-03…05, T-261128-58). Phản hồi chứa câu trả lời / trạng thái khoá không
 * lưu đệm (D-GEO-145).
 *
 * <ul>
 *   <li>{@code POST /api/v1/student/kb-qa} {assignmentId, question} — một lượt hỏi (bài luyện hoặc
 *       bài có chấm);
 *   <li>{@code GET /api/v1/student/kb-qa/{interactionId}/source} — đúng mục được dẫn;
 *   <li>{@code GET|POST /api/v1/student/kb-qa/privacy} — thông báo riêng tư MSG-S5-26 + "Em đã
 *       hiểu";
 *   <li>{@code GET /api/v1/teacher/assignments/{id}/kb-warnings} — mục tài liệu có đáp số của bài
 *       đang khoá đã bị lọc khỏi AI.
 * </ul>
 */
@RestController
@NoStore
public class KbQaController {

  public record AckRequest(String version) {}

  private final KbQaService service;

  public KbQaController(KbQaService service) {
    this.service = service;
  }

  @PostMapping("/api/v1/student/kb-qa")
  public Map<String, Object> ask(@RequestBody AskRequest body) {
    return service.ask(body);
  }

  @GetMapping("/api/v1/student/kb-qa/privacy")
  public Map<String, Object> privacy() {
    return service.privacy();
  }

  @PostMapping("/api/v1/student/kb-qa/privacy")
  public Map<String, Object> ack(@RequestBody AckRequest body) {
    return service.ack(body == null ? null : body.version());
  }

  @GetMapping("/api/v1/student/kb-qa/{interactionId}/source")
  public Map<String, Object> source(@PathVariable UUID interactionId) {
    return service.source(interactionId);
  }

  @GetMapping("/api/v1/teacher/assignments/{id}/kb-warnings")
  public Map<String, Object> warnings(@PathVariable UUID id) {
    return service.warnings(id);
  }
}
