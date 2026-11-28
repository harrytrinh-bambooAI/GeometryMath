package vn.geo.server.knowledge.internal.kb.history.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.knowledge.internal.kb.history.service.QaHistoryService;
import vn.geo.server.knowledge.internal.kb.history.service.QaHistoryService.FeedbackRequest;

/**
 * Lịch sử hỏi đáp theo bài (US-S5E3-06, T-261128-63). Đường dẫn theo quy ước app ({@code
 * /api/v1/teacher|student|admin}); đặc tả viết {@code /api/assignments/{id}/qa}, {@code
 * /api/qa/{id}/feedback}, {@code /api/qa/mine}. Phản hồi chứa câu trả lời AI: no-store.
 *
 * <ul>
 *   <li>{@code GET /api/v1/teacher/assignments/{id}/qa?decision=&cursor=} — lịch sử (50
 *       dòng/trang);
 *   <li>{@code GET /api/v1/teacher/assignments/{id}/qa/report} — tỉ lệ theo loại nguồn, bị chặn;
 *   <li>{@code POST /api/v1/teacher/qa/{interactionId}/feedback} {type, note} — đánh dấu;
 *   <li>{@code GET /api/v1/student/qa/mine?assignment=} — lượt hỏi của chính học sinh;
 *   <li>{@code GET /api/v1/admin/ai/qa-feedback?orgId=} — tổng hợp theo luật cho người vận hành.
 * </ul>
 */
@RestController
@NoStore
public class QaHistoryController {

  private final QaHistoryService service;

  public QaHistoryController(QaHistoryService service) {
    this.service = service;
  }

  @GetMapping("/api/v1/teacher/assignments/{id}/qa")
  public Map<String, Object> list(
      @PathVariable UUID id,
      @RequestParam(required = false) String decision,
      @RequestParam(required = false) String cursor) {
    return service.teacherList(id, decision, cursor);
  }

  @GetMapping("/api/v1/teacher/assignments/{id}/qa/report")
  public Map<String, Object> report(@PathVariable UUID id) {
    return service.report(id);
  }

  @PostMapping("/api/v1/teacher/qa/{interactionId}/feedback")
  public Map<String, Object> feedback(
      @PathVariable UUID interactionId, @RequestBody(required = false) FeedbackRequest body) {
    return service.feedback(interactionId, body);
  }

  @GetMapping("/api/v1/student/qa/mine")
  public Map<String, Object> mine(
      @RequestParam(required = false) UUID assignment,
      @RequestParam(required = false) String cursor) {
    return service.mine(assignment, cursor);
  }

  @GetMapping("/api/v1/admin/ai/qa-feedback")
  public Map<String, Object> summary(@RequestParam(required = false) UUID orgId) {
    return service.operatorSummary(orgId);
  }
}
