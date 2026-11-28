package vn.geo.server.exam.internal.draft.web;

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
import vn.geo.server.exam.internal.draft.service.ExamDraftService;
import vn.geo.server.exam.internal.draft.service.ExamDraftService.CreateRequest;

/**
 * Trợ lý soạn đề nháp từ tài liệu (US-S5E2-06, C5-08). Chỉ giáo viên; phản hồi có đáp số nên
 * no-store.
 *
 * <ul>
 *   <li>{@code POST /api/copilot/exam-drafts} {docId, classId, count} — soạn N đề nháp;
 *   <li>{@code GET /api/copilot/exam-drafts/{id}} — xem lại lần soạn;
 *   <li>{@code POST /api/copilot/exam-drafts/items/{itemId}/approve} — duyệt một đề vào "Của tôi";
 *   <li>{@code GET /api/copilot/exam-drafts/bank} — ngân hàng "Của tôi" (đề đã duyệt).
 * </ul>
 */
@RestController
@NoStore
public class ExamDraftController {

  private final ExamDraftService service;

  public ExamDraftController(ExamDraftService service) {
    this.service = service;
  }

  @PostMapping("/api/copilot/exam-drafts")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> create(@RequestBody(required = false) CreateRequest body) {
    return service.create(body);
  }

  @GetMapping("/api/copilot/exam-drafts/bank")
  public Map<String, Object> bank() {
    return service.bank();
  }

  @GetMapping("/api/copilot/exam-drafts/{id}")
  public Map<String, Object> get(@PathVariable UUID id) {
    return service.get(id);
  }

  @PostMapping("/api/copilot/exam-drafts/items/{itemId}/approve")
  public Map<String, Object> approve(@PathVariable UUID itemId) {
    return service.approve(itemId);
  }
}
