package vn.geo.server.aigrade.internal.kpi.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.aigrade.internal.kpi.repo.KpiRepository;

/**
 * Móc kiểm thử S5-E6c (chỉ profile dev/test; T-261128-65): nạp số liệu KPI theo bài cho một lớp
 * (thay cho 20–40 bài chạy song song thật), rồi xét lại cổng như sau bài cuối của đợt.
 */
@Component
@Profile({"dev", "test"})
public class KpiHookSupport {

  /** Một tiêu chí: tối đa, điểm AI, điểm giáo viên, đã sửa (bài AI đề xuất). */
  public record CritBody(double max, Double ai, Double gv, Boolean edited) {}

  public record PaperBody(
      String kind,
      String topic,
      List<CritBody> criteria,
      Boolean resultOk,
      Integer invented,
      Boolean noGrade,
      Integer reviewSeconds) {}

  public record SeedBody(UUID orgId, UUID classId, List<PaperBody> papers) {}

  private final KpiRepository repo;
  private final ClassAiGate gate;
  private final JsonMapper mapper;

  public KpiHookSupport(KpiRepository repo, ClassAiGate gate, JsonMapper mapper) {
    this.repo = repo;
    this.gate = gate;
    this.mapper = mapper;
  }

  public Map<String, Object> seed(SeedBody b) {
    int n = 0;
    for (PaperBody p : b.papers()) {
      List<Map<String, Object>> cs =
          p.criteria() == null
              ? List.of()
              : p.criteria().stream()
                  .map(c ->
                      c.edited() != null
                          ? Map.<String, Object>of("max", c.max(), "edited", c.edited())
                          : Map.<String, Object>of("max", c.max(), "ai", c.ai(), "gv", c.gv()))
                  .toList();
      repo.insertPaper(
          b.orgId(),
          b.classId(),
          p.kind() == null ? "shadow" : p.kind(),
          p.topic() == null ? "khoảng cách" : p.topic(),
          mapper.writeValueAsString(cs),
          p.resultOk(),
          p.invented() == null ? 0 : p.invented(),
          Boolean.TRUE.equals(p.noGrade()),
          p.reviewSeconds());
      n++;
    }
    return Map.of("inserted", n, "gateOpen", gate.evaluate(b.orgId(), b.classId()).open());
  }
}
