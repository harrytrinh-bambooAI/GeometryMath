package vn.geo.server.tracking.internal.errors.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.aigateway.api.AiGateway;
import vn.geo.server.aigateway.api.AiRequest;
import vn.geo.server.aigateway.api.AiResponse;
import vn.geo.server.aigateway.api.quota.AiQuota;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.tracking.internal.errors.domain.ErrorText;
import vn.geo.server.tracking.internal.errors.repo.ClassErrorRepository;
import vn.geo.server.tracking.internal.errors.repo.ClassErrorRepository.Assignment;
import vn.geo.server.tracking.internal.errors.repo.ClassErrorRepository.Grade;
import vn.geo.server.tracking.internal.errors.repo.ClassErrorRepository.Label;
import vn.geo.server.tracking.internal.errors.repo.ClassErrorRepository.Stored;

/**
 * S5-E5b — "Cả lớp hay sai ở đâu" (US-S5E5-02, COP-02) và "Nên nhắc lại trên lớp" (US-S5E5-03).
 *
 * <ul>
 *   <li>Số liệu thô tất định từ điểm duyệt MỚI NHẤT (BR-S5E5-04): mỗi tiêu chí số bài mất điểm và
 *       điểm trung bình; mỗi loại lỗi số bài (chỉ từ đề xuất AI giáo viên Nhận / Sửa); tín hiệu
 *       engine. Dưới 5 bài đã duyệt: MSG-S5-57, không số liệu, không gọi AI.
 *   <li>Task {@code class_errors} nhận dữ liệu ẩn danh "Bài k" (tên / mã học sinh trong nhận xét
 *       thay bằng [HS], BR-S5E5-14); tối đa 3 nhận định, mỗi nhận định dẫn số thứ tự bài
 *       (BR-S5E5-05). Bộ kiểm đầu ra loại nhận định có tên, phần trăm, xếp hạng, nhãn học sinh hoặc
 *       căn cứ không có thật (BR-S5E5-06).
 *   <li>Lớp tắt trợ lý AI, AI lỗi hoặc quá 10 giây: chỉ số liệu thô kèm MSG-S5-58 (BR-S5E5-07).
 *   <li>Gợi ý tất định, không gọi AI (BR-S5E5-08): đề Kho cùng họ dạng ngân hàng, câu có nhiễu cùng
 *       loại lỗi xếp trước; tài liệu Đang dùng gắn lớp nói tới dạng đó.
 * </ul>
 */
@Service
public class ClassErrorService {

  public static final String TASK = "class_errors";
  public static final int MIN_APPROVED = 5; // PO-65 Q6
  public static final int MAX_INSIGHTS = 3;
  public static final int MAX_SUGGESTIONS = 3;
  public static final Duration DEADLINE = Duration.ofSeconds(10); // PO-83
  public static final String MSG_57 = "Cần ít nhất 5 bài đã duyệt để tổng hợp lỗi của lớp.";
  public static final String MSG_58 =
      "AI tạm không tổng hợp được. Đây là số liệu thô theo tiêu chí.";
  public static final String MSG_NO_BANK = "Kho chưa có bài cùng dạng";
  public static final List<String> LABELS =
      List.of("sai-dinh-li", "sai-tinh-toan", "sai-hinh-dung", "nham-dieu-kien");

  private static final Logger log = LoggerFactory.getLogger(ClassErrorService.class);

  private final ClassErrorRepository repo;
  private final AiGateway ai;
  private final AiQuota quota;
  private final JsonMapper mapper;
  private final ObjectProvider<ErrorPromptLog> promptLog;

  public ClassErrorService(
      ClassErrorRepository repo,
      AiGateway ai,
      AiQuota quota,
      JsonMapper mapper,
      ObjectProvider<ErrorPromptLog> promptLog) {
    this.repo = repo;
    this.ai = ai;
    this.quota = quota;
    this.mapper = mapper;
    this.promptLog = promptLog;
  }

  /** Một bài làm ẩn danh "Bài k" (k theo mã bài nộp tăng dần). */
  record Work(int k, Grade grade, List<Lost> lost, Map<Integer, Label> labels, String engine) {}

  record Lost(int ord, String name, BigDecimal points, BigDecimal max) {}

  // ------------------------------------------------------------------ tổng hợp

  public Map<String, Object> summary(UUID assignmentId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Assignment a = owned(me, assignmentId);
    List<Grade> grades = new ArrayList<>(repo.latestGrades(me.orgId(), a.id()));
    grades.sort(Comparator.comparing(Grade::submissionCode).thenComparing(Grade::submissionId));
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("assignmentId", a.id().toString());
    out.put("code", a.code());
    out.put("title", a.title());
    out.put("approved", grades.size());
    out.put("minApproved", MIN_APPROVED);
    if (grades.size() < MIN_APPROVED) {
      out.put("status", "too_few");
      out.put("message", MSG_57);
      out.put("raw", null);
      out.put("insights", List.of());
      return out;
    }
    List<Work> works = works(me, grades);
    out.put("status", "ok");
    out.put("raw", raw(works));
    List<String> names = names(me, a.classId());
    String prompt = prompt(works, names);
    String hash =
        sha256(prompt + "|" + grades.stream().map(g -> g.submissionId() + "#" + g.seq()).toList());
    String worksJson =
        mapper.writeValueAsString(
            works.stream().map(w -> w.grade().submissionId().toString()).toList());

    String mode;
    List<ObjectNode> insights = new ArrayList<>();
    String check = "";
    int removed = 0;
    if (!quota.assistantOn(me.orgId(), a.classId(), TASK)) {
      mode = "ai_off"; // BR-S5E5-07, PO-85: không gọi AI
    } else {
      Stored last = repo.latest(me.orgId(), a.id()).orElse(null);
      if (last != null && "insight".equals(last.mode()) && hash.equals(last.inputHash())) {
        // đầu vào không đổi: dùng lại nhận định đã kiểm, không gọi lại AI
        for (JsonNode n : mapper.readTree(last.insights())) {
          insights.add((ObjectNode) n);
        }
        out.put("mode", "insight");
        out.put("message", null);
        out.put("check", last.checkNote());
        out.put("insights", view(insights, works));
        out.put("updatedAt", last.createdAt().toString());
        return out;
      }
      String reply = call(me, a, prompt);
      if (reply == null) {
        mode = "failed";
      } else {
        Checked c = check(reply, works.size(), names);
        if (c == null) {
          mode = "failed";
          check = "đầu ra không đọc được";
        } else {
          mode = "insight";
          insights = c.kept();
          removed = c.removed();
          check = removed == 0 ? "đạt" : "loại " + removed + " nhận định";
        }
      }
    }
    repo.insert(
        me.orgId(),
        Uuid7.next(),
        a.id(),
        me.userId(),
        hash,
        mode,
        mapper.writeValueAsString(insights),
        worksJson,
        check,
        removed);
    out.put("mode", mode);
    out.put("message", "insight".equals(mode) ? null : MSG_58);
    out.put("check", check);
    out.put("insights", view(insights, works));
    out.put("updatedAt", java.time.Instant.now().toString());
    return out;
  }

  /** Gọi task class_errors (đã xin lượt); null = AI lỗi / quá 10 giây / hết lượt (MSG-S5-58). */
  private String call(AuthPrincipal me, Assignment a, String prompt) {
    try {
      quota.takeComposeTurn(me.orgId(), me.userId(), a.classId(), TASK);
    } catch (AppException e) {
      return null; // hết lượt soạn hôm nay: lùi về số liệu thô
    }
    ErrorPromptLog pl = promptLog.getIfAvailable();
    if (pl != null) {
      pl.record(me.userId(), prompt);
    }
    long t0 = System.nanoTime();
    try {
      AiResponse r = ai.complete(new AiRequest(TASK, prompt), DEADLINE);
      if (Duration.ofNanos(System.nanoTime() - t0).compareTo(DEADLINE) > 0) {
        return null; // kết quả muộn: bỏ (BR-S5E5-07)
      }
      quota.addTokens(me.orgId(), a.classId(), (prompt.length() + r.content().length()) / 4L);
      return r.content();
    } catch (AppException e) {
      if (e.code() == ErrorCode.FORBIDDEN || e.code() == ErrorCode.UNAUTHENTICATED) {
        throw e;
      }
      log.info("class_errors lỗi: {}", e.code());
      return null;
    } catch (RuntimeException e) {
      log.info("class_errors lỗi: {}", e.getClass().getSimpleName());
      return null;
    }
  }

  record Checked(List<ObjectNode> kept, int removed) {}

  /** Bộ kiểm đầu ra (BR-S5E5-06); null = không đọc được. */
  Checked check(String reply, int n, List<String> names) {
    JsonNode root;
    try {
      root = mapper.readTree(reply);
    } catch (RuntimeException e) {
      return null;
    }
    JsonNode arr = root.path("insights");
    if (!arr.isArray()) {
      return null;
    }
    List<ObjectNode> kept = new ArrayList<>();
    int removed = 0;
    for (JsonNode in : arr) {
      String text = in.path("text").asString("").trim();
      Set<Integer> ks = new LinkedHashSet<>();
      boolean bad = false;
      for (JsonNode w : in.path("works")) {
        int k = w.asInt(-1);
        if (k < 1 || k > n) {
          bad = true; // căn cứ phải có trong dữ liệu
        } else {
          ks.add(k);
        }
      }
      if (bad || ks.isEmpty() || text.length() > 300 || ErrorText.reject(text, names) != null) {
        removed++;
        continue;
      }
      if (kept.size() >= MAX_INSIGHTS) {
        removed++;
        continue;
      }
      ObjectNode o = mapper.createObjectNode();
      o.put("text", text);
      ArrayNode wa = o.putArray("works");
      ks.forEach(wa::add);
      String crit = in.path("criterion").asString("");
      o.put("criterion", crit.length() > 60 || ErrorText.reject(crit, names) != null ? "" : crit);
      String label = in.path("label").asString("");
      o.put("label", LABELS.contains(label) ? label : "");
      kept.add(o);
    }
    return new Checked(kept, removed);
  }

  private List<Map<String, Object>> view(List<ObjectNode> insights, List<Work> works) {
    List<Map<String, Object>> out = new ArrayList<>();
    int i = 0;
    for (ObjectNode in : insights) {
      i++;
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("n", i);
      m.put("text", in.path("text").asString(""));
      String crit = in.path("criterion").asString("");
      m.put("criterion", crit.isEmpty() ? null : crit);
      String label = in.path("label").asString("");
      m.put("label", label.isEmpty() ? null : label);
      List<Map<String, Object>> ev = new ArrayList<>();
      for (JsonNode k : in.path("works")) {
        int kk = k.asInt();
        if (kk >= 1 && kk <= works.size()) {
          Map<String, Object> e = new LinkedHashMap<>();
          e.put("work", kk);
          e.put("label", "Bài " + kk);
          e.put("submissionId", works.get(kk - 1).grade().submissionId().toString());
          ev.add(e);
        }
      }
      m.put("evidence", ev);
      out.add(m);
    }
    return out;
  }

  private List<Work> works(AuthPrincipal me, List<Grade> grades) {
    List<UUID> ids = grades.stream().map(Grade::submissionId).toList();
    Map<UUID, Map<Integer, Label>> labels = new java.util.HashMap<>();
    Map<UUID, Integer> seqOf = new java.util.HashMap<>();
    for (Grade g : grades) {
      seqOf.put(g.submissionId(), g.seq());
    }
    for (Label l : repo.labels(me.orgId(), ids)) {
      if (l.seq() == seqOf.getOrDefault(l.submissionId(), -1)) {
        labels.computeIfAbsent(l.submissionId(), x -> new TreeMap<>()).put(l.ord(), l);
      }
    }
    Map<UUID, String> engine = new java.util.HashMap<>();
    for (var e : repo.engine(me.orgId(), ids)) {
      engine.put(e.submissionId(), e.status());
    }
    List<Work> out = new ArrayList<>();
    int k = 0;
    for (Grade g : grades) {
      k++;
      List<Lost> lost = new ArrayList<>();
      for (JsonNode c : mapper.readTree(g.criteria())) {
        BigDecimal p = c.path("points").decimalValue();
        BigDecimal max = c.path("maxPoints").decimalValue();
        if (p.compareTo(max) < 0) {
          lost.add(new Lost(c.path("ord").asInt(), c.path("name").asString(""), p, max));
        }
      }
      out.add(
          new Work(
              k,
              g,
              lost,
              labels.getOrDefault(g.submissionId(), Map.of()),
              engine.get(g.submissionId())));
    }
    return out;
  }

  /** Số liệu thô (không AI): theo tiêu chí, theo loại lỗi, tín hiệu engine. */
  private Map<String, Object> raw(List<Work> works) {
    Map<Integer, String> names = new TreeMap<>();
    Map<Integer, BigDecimal> maxOf = new TreeMap<>();
    Map<Integer, BigDecimal> sum = new TreeMap<>();
    Map<Integer, Integer> lostN = new TreeMap<>();
    Map<String, Integer> labelN = new LinkedHashMap<>();
    LABELS.forEach(l -> labelN.put(l, 0));
    Map<String, Integer> engineN = new LinkedHashMap<>();
    for (Work w : works) {
      for (JsonNode c : mapper.readTree(w.grade().criteria())) {
        int ord = c.path("ord").asInt();
        names.putIfAbsent(ord, c.path("name").asString(""));
        maxOf.putIfAbsent(ord, c.path("maxPoints").decimalValue());
        sum.merge(ord, c.path("points").decimalValue(), BigDecimal::add);
      }
      for (Lost l : w.lost()) {
        lostN.merge(l.ord(), 1, Integer::sum);
      }
      Set<String> mine = new LinkedHashSet<>();
      for (Lost l : w.lost()) {
        Label lab = w.labels().get(l.ord());
        if (lab != null && lab.label() != null) {
          mine.add(lab.label());
        }
      }
      mine.forEach(x -> labelN.merge(x, 1, Integer::sum));
      if (w.engine() != null) {
        engineN.merge(w.engine(), 1, Integer::sum);
      }
    }
    List<Map<String, Object>> crit = new ArrayList<>();
    for (var e : names.entrySet()) {
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("ord", e.getKey());
      m.put("name", e.getValue());
      m.put("maxPoints", maxOf.get(e.getKey()).doubleValue());
      m.put("lost", lostN.getOrDefault(e.getKey(), 0));
      m.put("works", works.size());
      m.put(
          "avgPoints",
          sum.get(e.getKey())
              .divide(BigDecimal.valueOf(works.size()), 2, RoundingMode.HALF_UP)
              .doubleValue());
      crit.add(m);
    }
    List<Map<String, Object>> labs = new ArrayList<>();
    for (var e : labelN.entrySet()) {
      labs.add(Map.of("label", e.getKey(), "works", e.getValue()));
    }
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("works", works.size());
    m.put("criteria", crit);
    m.put("labels", labs);
    m.put(
        "engine",
        Map.of(
            "match", engineN.getOrDefault("match", 0),
            "mismatch", engineN.getOrDefault("mismatch", 0),
            "notFound", engineN.getOrDefault("not_found", 0)));
    return m;
  }

  /** Tin nhắn ẩn danh cho task class_errors (BR-S5E5-05, 14). */
  String prompt(List<Work> works, List<String> names) {
    StringBuilder b = new StringBuilder();
    b.append("Bài làm ẩn danh của lớp (").append(works.size()).append(" bài):\n");
    for (Work w : works) {
      b.append("Bài ").append(w.k()).append(": ");
      List<String> parts = new ArrayList<>();
      if (w.lost().isEmpty()) {
        parts.add("không mất điểm");
      } else {
        List<String> l = new ArrayList<>();
        for (Lost x : w.lost()) {
          l.add(x.name() + " (" + num(x.points()) + "/" + num(x.max()) + ")");
        }
        parts.add("mất điểm " + String.join(", ", l));
        for (Lost x : w.lost()) {
          Label lab = w.labels().get(x.ord());
          if (lab != null && lab.label() != null) {
            parts.add("loại lỗi " + lab.label() + " (" + x.name() + ")");
          }
        }
        for (Lost x : w.lost()) {
          Label lab = w.labels().get(x.ord());
          if (lab != null && lab.reason() != null && !lab.reason().isBlank()) {
            parts.add("lý do " + x.name() + ": " + oneLine(ErrorText.scrub(lab.reason(), names)));
          }
        }
      }
      if ("mismatch".equals(w.engine())) {
        parts.add("engine: đáp số lệch");
      }
      String c = ErrorText.scrub(w.grade().comment(), names);
      if (!c.isBlank()) {
        parts.add("nhận xét: " + oneLine(c));
      }
      b.append(String.join("; ", parts)).append('\n');
    }
    return b.toString();
  }

  private static String oneLine(String s) {
    String t = s.replaceAll("[\\r\\n;]+", " ").trim();
    return t.length() > 300 ? t.substring(0, 300) : t;
  }

  private static String num(BigDecimal d) {
    return d.stripTrailingZeros().toPlainString();
  }

  private List<String> names(AuthPrincipal me, UUID classId) {
    List<String> out = new ArrayList<>();
    for (var p : repo.roster(me.orgId(), classId)) {
      out.add(p.displayName());
      if (p.userCode() != null) {
        out.add(p.userCode());
      }
    }
    return out;
  }

  // ------------------------------------------------------------------ gợi ý

  public Map<String, Object> suggestions(UUID assignmentId, int n) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Assignment a = owned(me, assignmentId);
    if (n < 1 || n > MAX_INSIGHTS) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    String label = null;
    String criterion = null;
    Stored last = repo.latest(me.orgId(), a.id()).orElse(null);
    if (last != null) {
      JsonNode arr = mapper.readTree(last.insights());
      if (arr.size() >= n) {
        label = blankToNull(arr.get(n - 1).path("label").asString(""));
        criterion = blankToNull(arr.get(n - 1).path("criterion").asString(""));
      }
    }
    String statement = repo.statement(me.orgId(), a.problemVersionId()).orElse("");
    String family = ErrorText.family(statement);
    List<Map<String, Object>> items =
        family == null ? List.of() : bankItems(me.orgId(), family, label, statement, a);
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("assignmentId", a.id().toString());
    out.put("n", n);
    out.put("label", label);
    out.put("criterion", criterion);
    out.put("family", family);
    out.put("familyLabel", family == null ? null : ErrorText.familyLabel(family));
    out.put("items", items);
    out.put("message", items.isEmpty() ? MSG_NO_BANK : null);
    List<Map<String, Object>> docs = new ArrayList<>();
    if (family != null) {
      for (var d : repo.docs(me.orgId(), me.userId(), a.classId(), ErrorText.familyLabel(family))) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.id().toString());
        m.put("code", d.code());
        m.put("title", d.title());
        m.put("version", d.version());
        docs.add(m);
      }
    }
    out.put("docs", docs);
    return out;
  }

  /** Một đề kho ứng viên: câu ngân hàng giống nó nhất trong họ dạng (dạng, nhiễu). */
  private record Candidate(
      ClassErrorRepository.Catalog c, boolean sameDang, boolean labelMatch, String distractor) {}

  private List<Map<String, Object>> bankItems(
      UUID orgId, String family, String label, String statement, Assignment a) {
    var bank = repo.bank(family);
    if (bank.isEmpty()) {
      return List.of(); // ngân hàng chưa có dạng này
    }
    List<JsonNode> bodies = new ArrayList<>();
    for (var b : bank) {
      bodies.add(mapper.readTree(b.body()));
    }
    String target = closest(bodies, statement).path("dang").asString("");
    Map<String, String> byDang = new java.util.HashMap<>(); // dạng → mô tả nhiễu gắn loại lỗi label
    if (label != null) {
      for (JsonNode b : bodies) {
        for (JsonNode nh : b.path("nhieu")) {
          if (label.equals(nh.path("loi").path("loai_loi").asString(""))) {
            byDang.putIfAbsent(
                b.path("dang").asString(""), nh.path("loi").path("mo_ta").asString(""));
          }
        }
      }
    }
    List<Candidate> cands = new ArrayList<>();
    for (var c : repo.catalog(orgId)) {
      if (c.problemVersionId().equals(a.problemVersionId())
          || !family.equals(ErrorText.family(c.statement()))) {
        continue;
      }
      JsonNode near = closest(bodies, c.statement());
      // nhiễu theo DẠNG của đề (mọi câu ngân hàng cùng dạng), không chỉ câu giống nhất
      String distractor = label == null ? null : byDang.get(near.path("dang").asString(""));
      cands.add(
          new Candidate(
              c, near.path("dang").asString("").equals(target), distractor != null, distractor));
    }
    return cands.stream()
        .sorted(
            Comparator.comparing((Candidate x) -> !x.labelMatch())
                .thenComparing(x -> !x.sameDang())
                .thenComparing(x -> x.c().code()))
        .limit(MAX_SUGGESTIONS)
        .map(x -> {
          Map<String, Object> m = new LinkedHashMap<>();
          m.put("code", x.c().code());
          m.put("problemVersionId", x.c().problemVersionId().toString());
          m.put("statement", x.c().statement());
          m.put("sameDang", x.sameDang());
          m.put("distractorMatch", x.labelMatch());
          m.put("distractor", x.distractor());
          return m;
        })
        .toList();
  }

  /** Câu ngân hàng giống đề nhất (Jaccard, tất định: hòa thì câu đầu theo mã). */
  private static JsonNode closest(List<JsonNode> bodies, String statement) {
    JsonNode best = bodies.get(0);
    double score = -1;
    for (JsonNode b : bodies) {
      double s = ErrorText.similarity(statement, b.path("de_bai").asString(""));
      if (s > score) {
        score = s;
        best = b;
      }
    }
    return best;
  }

  private static String blankToNull(String s) {
    return s == null || s.isBlank() ? null : s;
  }

  // ------------------------------------------------------------------ chung

  /** Bài có chấm của chính giáo viên; khác: NOT_FOUND (không lộ có tồn tại). */
  private Assignment owned(AuthPrincipal me, UUID id) {
    return repo.assignment(me.orgId(), id)
        .filter(x -> x.teacherId().equals(me.userId()))
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  static String sha256(String s) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
