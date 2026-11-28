package vn.geo.server.knowledge.internal.kb.qa.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.assignment.api.HelpLevelApi;
import vn.geo.server.knowledge.api.KbRetriever;
import vn.geo.server.knowledge.internal.kb.qa.repo.KbQaRepository;

/**
 * Tin nhắn task {@code kb_qa} và đọc câu trả lời (T-261128-58). Tin nhắn KHÔNG có tên hiển thị, mã
 * người dùng, mã lớp (BR-S5E3-11, NFR-GEO-071): chỉ mức trợ giúp, các mục được phép dẫn (mỗi mục
 * một mã trong ngoặc vuông) và câu hỏi trong thẻ dữ liệu.
 */
final class KbQaPrompt {

  private KbQaPrompt() {}

  /** Một mục được gửi AI: mã dẫn, loại nguồn, thân và nguồn hiển thị cho học sinh. */
  record Ref(String code, String typeLabel, String title, String body, Map<String, Object> source) {

    static Ref of(KbRetriever.Hit h) {
      String code = h.docCode() + " v" + h.version() + " #" + h.ord();
      Map<String, Object> s = new LinkedHashMap<>();
      s.put("type", "kb");
      s.put("typeLabel", "Tài liệu của thầy cô");
      s.put("docId", h.docId().toString());
      s.put("docCode", h.docCode());
      s.put("docTitle", h.docTitle());
      s.put("version", h.version());
      s.put("sectionId", h.sectionId().toString());
      s.put("sectionTitle", h.title());
      s.put("label", "Theo: " + h.docTitle() + " · v" + h.version() + " · mục " + h.title());
      return new Ref(code, "Tài liệu của thầy cô", h.title(), h.body(), s);
    }

    static Ref of(KbQaRepository.Lecture l) {
      String code = "BGM-" + l.slug();
      Map<String, Object> s = new LinkedHashMap<>();
      s.put("type", "lecture");
      s.put("typeLabel", "Bài giảng mẫu");
      s.put("lectureId", l.id().toString());
      s.put("slug", l.slug());
      s.put("title", l.title());
      s.put("label", "Theo: Bài giảng mẫu · " + l.title());
      return new Ref(code, "Bài giảng mẫu", l.title(), l.body(), s);
    }

    /** Mục có mã {@code code} trong danh sách đã gửi (so không phân biệt hoa thường, bỏ ngoặc). */
    static Ref find(List<Ref> refs, String code) {
      String c = code.replace("[", "").replace("]", "").strip();
      for (Ref r : refs) {
        if (r.code().equalsIgnoreCase(c)) {
          return r;
        }
      }
      return null;
    }
  }

  /** Câu trả lời đã đọc: kind (concept | hint | explain_step | refuse | khác), reply, source. */
  record Reply(String kind, String reply, String source) {}

  static String build(String helpLevel, List<Ref> refs, String question) {
    StringBuilder sb = new StringBuilder();
    sb.append("Mức trợ giúp: ").append(HelpLevelApi.label(helpLevel));
    sb.append(
        HelpLevelApi.HINTS.equals(helpLevel)
            ? " (chỉ được trả lời kind hint hoặc explain_step)\n"
            : " (được trả lời kind concept, hint hoặc explain_step)\n");
    sb.append("Mục tài liệu được dùng (chỉ dẫn đúng một mã trong ngoặc vuông):\n");
    for (Ref r : refs) {
      sb.append('[').append(r.code()).append("] (").append(r.typeLabel()).append(") ");
      sb.append(r.title()).append('\n');
      String body = r.body() == null ? "" : r.body();
      sb.append(body.length() > 3000 ? body.substring(0, 3000) : body).append("\n\n");
    }
    sb.append("<cau_hoi_hoc_sinh>\n")
        .append(question.replace("<", "‹"))
        .append("\n</cau_hoi_hoc_sinh>");
    return sb.toString();
  }

  static Reply parse(JsonMapper mapper, String content) {
    String c = content == null ? "" : content.strip();
    int a = c.indexOf('{');
    int b = c.lastIndexOf('}');
    if (a >= 0 && b > a) {
      try {
        JsonNode n = mapper.readTree(c.substring(a, b + 1));
        String kind = n.path("kind").asString("");
        String reply = n.path("reply").asString("");
        String source =
            n.path("source").isMissingNode() || n.path("source").isNull()
                ? null
                : n.path("source").asString("");
        return new Reply(kind, reply, source == null || source.isBlank() ? null : source);
      } catch (RuntimeException e) {
        // không phải JSON: coi là văn bản thuần không nguồn
      }
    }
    return new Reply("concept", c, null);
  }
}
