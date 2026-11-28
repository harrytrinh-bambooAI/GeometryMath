package vn.geo.server.knowledge.internal.kb.qa.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import vn.geo.server.knowledge.internal.kb.qa.repo.KbQaTestHookRepository;

/** Dịch vụ của móc kiểm thử hỏi đáp theo kho (chỉ dev/test, T-261128-58). */
@Service
@Profile({"dev", "test"})
public class KbQaTestHookService {

  private final KbQaProbe probe;
  private final KbQaTestHookRepository repo;

  public KbQaTestHookService(KbQaProbe probe, KbQaTestHookRepository repo) {
    this.probe = probe;
    this.repo = repo;
  }

  public String prompt(UUID interactionId) {
    return probe.prompt(interactionId);
  }

  public Map<String, Object> state(UUID orgId, UUID userId, UUID targetId) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("interactions", repo.interactions(orgId, userId, targetId));
    m.put("decisions", repo.decisions(orgId, userId, targetId));
    m.put("counters", repo.counters(orgId));
    return m;
  }

  public void firewall(UUID userId, Long delayMs, Boolean fault) {
    if (delayMs != null) {
      probe.delay(userId, delayMs);
    }
    if (fault != null) {
      probe.fault(userId, fault);
    }
  }

  public int release(UUID orgId, UUID gradedId) {
    return repo.release(orgId, gradedId);
  }

  public int lecture(String slug, boolean approved) {
    return repo.lectureApproval(slug, approved);
  }
}
