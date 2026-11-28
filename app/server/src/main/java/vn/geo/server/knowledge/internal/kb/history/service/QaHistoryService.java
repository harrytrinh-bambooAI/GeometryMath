package vn.geo.server.knowledge.internal.kb.history.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.knowledge.internal.kb.history.repo.QaHistoryRepository;
import vn.geo.server.knowledge.internal.kb.history.repo.QaHistoryRepository.Feedback;
import vn.geo.server.knowledge.internal.kb.history.repo.QaHistoryRepository.Mix;
import vn.geo.server.knowledge.internal.kb.history.repo.QaHistoryRepository.Row;
import vn.geo.server.knowledge.internal.kb.history.repo.QaHistoryRepository.Target;

/**
 * Giáo viên xem lịch sử hỏi đáp theo bài, đánh dấu "chặn sai" / "lẽ ra phải chặn" (US-S5E3-06):
 *
 * <ul>
 *   <li>BR-S5E3-19: danh sách theo bài (giờ, tên hiển thị, câu hỏi, câu trả lời / câu từ chối đã
 *       hiện, nguồn, quyết định, luật), lọc theo quyết định, phân trang con trỏ 50 dòng; không xếp
 *       hạng hay đếm theo học sinh (PO-04);
 *   <li>BR-S5E3-20: một đánh dấu mỗi lượt, sửa được, ghi chú ≤ 200 ký tự; bộ đếm theo luật cho
 *       người vận hành; câu trả lời đã gửi không đổi;
 *   <li>BR-S5E3-21: giáo viên lớp khác NOT_FOUND; học sinh gọi API giáo viên FORBIDDEN, chỉ thấy
 *       lượt của mình;
 *   <li>BR-S5E3-23: báo cáo tỉ lệ theo loại nguồn và tỉ lệ bị chặn theo luật, kèm n, không tên.
 * </ul>
 */
@Service
public class QaHistoryService {

  public static final String MSG_25 = "Đã ghi báo cáo. Cảm ơn thầy cô.";
  public static final int PAGE = 50;
  public static final int MAX_NOTE = 200;
  static final Set<String> DECISIONS = Set.of("allowed", "blocked", "error_closed");
  static final Set<String> TYPES = Set.of("false_block", "should_block");

  public record FeedbackRequest(String type, String note) {}

  private final QaHistoryRepository repo;
  private final JsonMapper mapper;

  public QaHistoryService(QaHistoryRepository repo, JsonMapper mapper) {
    this.repo = repo;
    this.mapper = mapper;
  }

  /** Lịch sử của bài cho giáo viên chủ lớp. */
  public Map<String, Object> teacherList(UUID targetId, String decision, String cursor) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    owned(me, targetId);
    String d = decision == null || decision.isBlank() ? null : decision.strip();
    if (d != null && !DECISIONS.contains(d)) {
      throw AppException.invalidField(
          "decision", "Quyết định chỉ nhận allowed, blocked, error_closed.");
    }
    return page(me, targetId, null, d, cursor, true);
  }

  /** Lượt hỏi của chính học sinh trên một bài. */
  public Map<String, Object> mine(UUID targetId, String cursor) {
    AuthPrincipal me = Roles.require(Role.STUDENT);
    if (targetId == null) {
      throw AppException.invalidField("assignment", "Thiếu mã bài.");
    }
    return page(me, targetId, me.userId(), null, cursor, false);
  }

  @Transactional
  public Map<String, Object> feedback(UUID interactionId, FeedbackRequest in) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    String type = in == null || in.type() == null ? "" : in.type().strip();
    if (!TYPES.contains(type)) {
      throw AppException.invalidField("type", "Chọn \"chặn sai\" hoặc \"lẽ ra phải chặn\".");
    }
    String note = in.note() == null ? null : in.note().strip();
    if (note != null && note.codePointCount(0, note.length()) > MAX_NOTE) {
      throw AppException.invalidField("note", "Ghi chú tối đa 200 ký tự.");
    }
    if (note != null && note.isEmpty()) {
      note = null;
    }
    Target t =
        repo.interaction(me.orgId(), interactionId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    owned(me, t.targetId());
    String rule = ruleOf(t.decision(), t.rule());
    Feedback before = repo.feedback(me.orgId(), interactionId).orElse(null);
    repo.upsertFeedback(me.orgId(), interactionId, me.userId(), type, note, rule);
    if (before == null) {
      repo.count(me.orgId(), rule, type, 1);
    } else if (!before.type().equals(type)) {
      repo.count(me.orgId(), before.rule(), before.type(), -1);
      repo.count(me.orgId(), rule, type, 1);
    }
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("interactionId", interactionId.toString());
    m.put("type", type);
    m.put("typeLabel", typeLabel(type));
    m.put("note", note);
    m.put("rule", rule.isEmpty() ? null : rule);
    m.put("message", MSG_25);
    return m;
  }

  /** Báo cáo hỏi đáp của bài (BR-S5E3-23). */
  public Map<String, Object> report(UUID targetId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    owned(me, targetId);
    long n = 0;
    long kb = 0;
    long lecture = 0;
    long none = 0;
    long blocked = 0;
    Map<String, Long> byRule = new TreeMap<>();
    for (Mix x : repo.mix(me.orgId(), targetId)) {
      n += x.n();
      if ("blocked".equals(x.decision()) || "error_closed".equals(x.decision())) {
        blocked += x.n();
        byRule.merge(ruleOf(x.decision(), x.rule()), x.n(), Long::sum);
      } else if ("kb".equals(x.sourceType())) {
        kb += x.n();
      } else if ("lecture".equals(x.sourceType())) {
        lecture += x.n();
      } else {
        none += x.n();
      }
    }
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("targetId", targetId.toString());
    m.put("n", n);
    m.put("teacherDocs", share("Tài liệu của thầy cô", kb, n));
    m.put("lectures", share("Bài giảng mẫu", lecture, n));
    m.put("noSource", share("Không nguồn", none, n));
    m.put("blocked", share("Bị chặn", blocked, n));
    List<Map<String, Object>> rules = new ArrayList<>();
    for (Map.Entry<String, Long> e : byRule.entrySet()) {
      Map<String, Object> r = share(e.getKey(), e.getValue(), n);
      r.put("rule", e.getKey());
      rules.add(r);
    }
    m.put("blockedByRule", rules);
    return m;
  }

  /** Tổng hợp đánh dấu theo luật cho người vận hành (vai Vận hành; không nội dung, không tên). */
  public Map<String, Object> operatorSummary(UUID orgFilter) {
    AuthPrincipal me = vn.geo.server.common.security.RequestContext.principal();
    if (!repo.operator(me.userId())) {
      throw new AppException(ErrorCode.FORBIDDEN);
    }
    List<Map<String, Object>> rows = new ArrayList<>();
    for (QaHistoryRepository.Count c : repo.counts(orgFilter)) {
      Map<String, Object> r = new LinkedHashMap<>();
      r.put("rule", c.rule().isEmpty() ? null : c.rule());
      r.put("type", c.type());
      r.put("n", c.n());
      rows.add(r);
    }
    return Map.of("items", rows);
  }

  // ---------------------------------------------------------------------------------------------

  private void owned(AuthPrincipal me, UUID targetId) {
    if (targetId == null || !repo.teacherOwns(me.orgId(), me.userId(), targetId)) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
  }

  /** Luật của một lượt: luật bộ kiểm; lỗi đóng không ghi luật thì KS-5. */
  static String ruleOf(String decision, String rule) {
    if (rule != null && !rule.isBlank()) {
      return rule;
    }
    return "error_closed".equals(decision) ? "KS-5" : "";
  }

  static String typeLabel(String type) {
    return "false_block".equals(type) ? "Chặn sai" : "Lẽ ra phải chặn";
  }

  static Map<String, Object> share(String label, long count, long n) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("label", label);
    m.put("count", count);
    m.put("percent", n == 0 ? 0.0 : Math.round(count * 1000.0 / n) / 10.0);
    return m;
  }

  private Map<String, Object> page(
      AuthPrincipal me,
      UUID targetId,
      UUID studentId,
      String decision,
      String cursor,
      boolean teacher) {
    Instant afterAt = null;
    UUID afterId = null;
    if (cursor != null && !cursor.isBlank()) {
      try {
        String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
        int bar = raw.indexOf('|');
        afterAt = Instant.parse(raw.substring(0, bar));
        afterId = UUID.fromString(raw.substring(bar + 1));
      } catch (RuntimeException e) {
        throw new AppException(ErrorCode.INVALID_CURSOR);
      }
    }
    List<Row> rows =
        repo.page(me.orgId(), targetId, studentId, decision, afterAt, afterId, PAGE + 1);
    boolean more = rows.size() > PAGE;
    if (more) {
      rows = rows.subList(0, PAGE);
    }
    List<Map<String, Object>> items = new ArrayList<>();
    for (Row r : rows) {
      items.add(item(r, teacher));
    }
    String next = null;
    if (more) {
      Row last = rows.get(rows.size() - 1);
      next =
          Base64.getUrlEncoder()
              .withoutPadding()
              .encodeToString((last.at() + "|" + last.id()).getBytes(StandardCharsets.UTF_8));
    }
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("items", items);
    m.put("nextCursor", next);
    return m;
  }

  private Map<String, Object> item(Row r, boolean teacher) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", r.id().toString());
    m.put("at", r.at().toString());
    if (teacher) {
      m.put("studentName", r.studentName());
    }
    m.put("question", r.question());
    m.put("replyKind", r.replyKind());
    m.put("reply", r.reply());
    m.put("source", source(r.sourceJson()));
    if (teacher) {
      m.put("decision", r.decision());
      m.put("rule", r.decision() == null ? null : nullIfEmpty(ruleOf(r.decision(), r.rule())));
      if (r.feedbackType() != null) {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("type", r.feedbackType());
        f.put("typeLabel", typeLabel(r.feedbackType()));
        f.put("note", r.feedbackNote());
        m.put("feedback", f);
      } else {
        m.put("feedback", null);
      }
    }
    return m;
  }

  private static String nullIfEmpty(String s) {
    return s == null || s.isEmpty() ? null : s;
  }

  private Map<String, Object> source(String json) {
    if (json == null || json.isBlank()) {
      return null;
    }
    JsonNode s;
    try {
      s = mapper.readTree(json);
    } catch (RuntimeException e) {
      return null;
    }
    if (!s.isObject()) {
      return null;
    }
    String type = s.path("type").asString("");
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("type", type);
    m.put("typeLabel", "lecture".equals(type) ? "Bài giảng mẫu" : "Tài liệu của thầy cô");
    String label;
    if ("lecture".equals(type)) {
      label = s.path("title").asString("");
    } else {
      label =
          (s.path("docCode").asString("") + " v" + s.path("version").asInt(0)).strip()
              + (s.hasNonNull("sectionTitle") ? " · " + s.path("sectionTitle").asString() : "");
    }
    m.put("label", label);
    return m;
  }
}
