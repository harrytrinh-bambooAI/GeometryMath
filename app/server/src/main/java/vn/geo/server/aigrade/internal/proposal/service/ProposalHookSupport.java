package vn.geo.server.aigrade.internal.proposal.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import vn.geo.server.aigrade.internal.proposal.repo.ProposalHookRepository;

/**
 * Móc kiểm thử S5-E6 (chỉ profile dev/test): cấp bộ đề F-S5-050, đặt chế độ "AI đề xuất" (cổng lớp
 * còn là stub tới T-261128-65), chạy chấm ngay, xem tin nhắn task grade, đếm hàng dữ liệu.
 */
@Component
@Profile({"dev", "test"})
public class ProposalHookSupport {

  private final ProposalHookRepository repo;
  private final ProposalService service;

  public ProposalHookSupport(ProposalHookRepository repo, ProposalService service) {
    this.repo = repo;
    this.service = service;
  }

  public Map<String, Object> setup(UUID orgId, UUID assignmentId) {
    Map<String, Object> m = new LinkedHashMap<>();
    repo.grantBank(orgId, "s5_core");
    m.put("aiProposal", assignmentId == null ? 0 : repo.aiProposalMode(orgId, assignmentId));
    return m;
  }

  public Map<String, Object> run(UUID orgId, UUID submissionId) {
    return Map.of("ran", service.runNow(orgId, submissionId));
  }

  public Map<String, Object> prompt(UUID orgId, UUID submissionId) {
    return Map.of("prompt", service.previewPrompt(orgId, submissionId));
  }

  public Map<String, Object> counts(UUID orgId, UUID submissionId) {
    return repo.counts(orgId, submissionId);
  }
}
