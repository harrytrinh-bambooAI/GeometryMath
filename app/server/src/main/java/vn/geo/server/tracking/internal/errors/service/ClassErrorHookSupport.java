package vn.geo.server.tracking.internal.errors.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import vn.geo.server.tracking.internal.errors.repo.ClassErrorRepository;

/** MÓC KIỂM THỬ S5-E5b (chỉ dev/test): đọc lượt tổng hợp và việc AI nền cho E2E. */
@Service
@Profile({"dev", "test"})
public class ClassErrorHookSupport {

  private final ErrorPromptLog prompts;
  private final ClassErrorRepository repo;

  public ClassErrorHookSupport(ErrorPromptLog prompts, ClassErrorRepository repo) {
    this.prompts = prompts;
    this.repo = repo;
  }

  public List<String> prompts(UUID teacherId) {
    return prompts.of(teacherId);
  }

  public List<Map<String, Object>> runs(UUID orgId, UUID assignmentId) {
    List<Map<String, Object>> out = new ArrayList<>();
    for (var s : repo.recent(orgId, assignmentId, 20)) {
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("mode", s.mode());
      m.put("check", s.checkNote());
      m.put("removed", s.removed());
      m.put("at", s.createdAt().toString());
      out.add(m);
    }
    return out;
  }

  public List<Map<String, Object>> jobs(UUID orgId, UUID refId) {
    List<Map<String, Object>> out = new ArrayList<>();
    for (String[] j : repo.jobs(orgId, refId)) {
      out.add(Map.of("id", j[0], "task", j[1], "status", j[2]));
    }
    return out;
  }

  public void grantBank(UUID orgId, String bank) {
    repo.grantBank(orgId, bank);
  }
}
