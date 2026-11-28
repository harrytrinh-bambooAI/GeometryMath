package vn.geo.server.aigrade.internal.proposal.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.aigrade.internal.proposal.service.ProposalService;
import vn.geo.server.common.web.NoStore;

/**
 * Đề xuất điểm AI và tín hiệu engine (US-S5E6-01, 03): chỉ giáo viên chủ lớp đọc; học sinh
 * FORBIDDEN; ngoài quan hệ NOT_FOUND. Sau cờ {@code geo.s5.ai-grading.enabled} (tắt thì không có
 * đường).
 */
@RestController
@ConditionalOnProperty(name = "geo.s5.ai-grading.enabled", havingValue = "true")
public class ProposalController {

  private final ProposalService service;

  public ProposalController(ProposalService service) {
    this.service = service;
  }

  @NoStore
  @GetMapping("/api/v1/submissions/{id}/proposal")
  public Map<String, Object> get(@PathVariable UUID id) {
    return service.get(id);
  }

  @NoStore
  @GetMapping("/api/v1/teacher/graded-assignments/{id}/ai-signals")
  public Map<String, Object> signals(@PathVariable UUID id) {
    return service.assignmentSignals(id);
  }
}
