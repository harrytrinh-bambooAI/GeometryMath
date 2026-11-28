package vn.geo.server.knowledge.internal.kb.history.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.knowledge.internal.kb.history.repo.QaHistoryTestHookRepository;

/**
 * Móc kiểm thử US-S5E3-06 (chỉ profile dev/test): ghi sẵn các lượt hỏi đáp của một bài theo dữ liệu
 * của ca (quyết định, luật, loại nguồn) như bộ kiểm KS-1…KS-5 đã ghi; đọc lại câu trả lời đã gửi.
 */
@Service
@Profile({"dev", "test"})
public class QaHistoryTestHookService {

  public record SeedRow(
      UUID studentId, String decision, String rule, String sourceType, String question) {}

  public record Seed(UUID orgId, UUID classId, UUID targetId, List<SeedRow> rows) {}

  static final String REFUSED =
      "Câu trả lời chưa an toàn để hiện. Em thử hỏi cách khác hoặc xem gợi ý.";

  private final QaHistoryTestHookRepository repo;
  private final JsonMapper mapper;

  public QaHistoryTestHookService(QaHistoryTestHookRepository repo, JsonMapper mapper) {
    this.repo = repo;
    this.mapper = mapper;
  }

  @Transactional
  public Map<String, Object> seed(Seed s) {
    List<String> ids = new ArrayList<>();
    int n = s.rows().size();
    for (int i = 0; i < n; i++) {
      SeedRow r = s.rows().get(i);
      UUID id = Uuid7.next();
      String decision = r.decision() == null ? "allowed" : r.decision();
      boolean blocked = !"allowed".equals(decision);
      String sourceJson = null;
      if (!blocked && "kb".equals(r.sourceType())) {
        ObjectNode src = mapper.createObjectNode();
        src.put("type", "kb");
        src.put("docCode", "TL-01");
        src.put("version", 2);
        src.put("sectionTitle", "Hệ thức 1/AH²");
        sourceJson = mapper.writeValueAsString(src);
      } else if (!blocked && "lecture".equals(r.sourceType())) {
        ObjectNode src = mapper.createObjectNode();
        src.put("type", "lecture");
        src.put("title", "Mặt phẳng trung trực");
        sourceJson = mapper.writeValueAsString(src);
      }
      String kind =
          blocked
              ? ("error_closed".equals(decision) ? "error_closed" : "refused_answer")
              : sourceJson == null ? "no_source" : "concept";
      String reply =
          blocked
              ? REFUSED
              : sourceJson == null
                  ? "Thầy cô chưa cung cấp tài liệu cho phần này. Em hỏi thầy cô trên lớp nhé."
                  : "Dựng AH ⊥ SB rồi dùng hệ thức 1/AH² = 1/SA² + 1/AB².";
      String q = r.question() == null ? "Câu hỏi số " + (i + 1) : r.question();
      repo.interaction(
          s.orgId(),
          id,
          r.studentId(),
          s.classId(),
          s.targetId(),
          q,
          kind,
          reply,
          sourceJson,
          n - i);
      repo.decision(s.orgId(), Uuid7.next(), id, r.studentId(), s.targetId(), decision, r.rule());
      ids.add(id.toString());
    }
    return Map.of("ids", ids);
  }

  public Map<String, Object> reply(UUID orgId, UUID interactionId) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("reply", repo.reply(orgId, interactionId));
    return m;
  }
}
