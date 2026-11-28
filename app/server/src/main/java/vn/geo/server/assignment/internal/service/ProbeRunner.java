package vn.geo.server.assignment.internal.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.aigateway.api.AiGateway;
import vn.geo.server.assignment.internal.repo.ProbeRepository;
import vn.geo.server.learning.api.AnswerGuardApi;

/**
 * Chạy bộ câu thử moi đáp án (US-GEO-610; M-GEO-06; chỉ dev/test vì dựng lớp thử và dùng nhà cung
 * cấp AI giả lập). Mỗi lượt: dựng một lớp riêng có bài HKG-01 Sắp mở (như BG-1), học sinh "An" đã
 * xem Gợi ý 1 và 2, rồi gửi mọi câu của bộ qua ĐƯỜNG THẬT của máy chủ (HTTP vào chính nó) và đếm
 * chuỗi lộ trong dữ liệu trả về của từng kênh: hỏi đáp, dữ liệu trang bài, xem trước, tự học dán
 * lại đề, đường lời giải của giáo viên gọi từ phiên học sinh. Báo cáo lưu vào probe_report (giữ mọi
 * lượt).
 */
@Service
@Profile({"dev", "test"})
public class ProbeRunner {

  public static final String NOTE =
      "Nhà cung cấp AI giả lập: kết quả chứng minh lớp kiểm soát ở máy chủ, chưa chứng minh hành vi của AI thật.";

  /** Ghi chú khi chạy với AI thật (T-05: trước đây luôn ghi NOTE dù provider=anthropic). */
  public static final String NOTE_REAL =
      "Nhà cung cấp AI thật (%s): kết quả gồm cả lớp kiểm soát ở máy chủ và hành vi của AI thật.";

  /** Ghi chú báo cáo theo nhà cung cấp đang dùng. */
  static String note(String provider) {
    return provider == null
            || provider.isBlank()
            || "mock".equals(provider)
            || "unknown".equals(provider)
        ? NOTE
        : String.format(NOTE_REAL, provider);
  }

  static final String CH_QA = "hỏi đáp";
  static final String CH_PAGE = "dữ liệu trang bài";
  static final String CH_PREVIEW = "xem trước";
  static final String CH_SELF = "tự học dán lại đề";
  static final String CH_TEACHER = "đường lời giải của giáo viên";

  public record RunOptions(String runLabel, String model, Boolean guardOff) {}

  private final ProbeService probes;
  private final E6SandboxService sandbox;
  private final AiGateway ai;
  private final AnswerGuardApi guard;
  private final Environment env;
  private final JsonMapper mapper;
  private final HttpClient http = HttpClient.newHttpClient();

  public ProbeRunner(
      ProbeService probes,
      E6SandboxService sandbox,
      AiGateway ai,
      AnswerGuardApi guard,
      Environment env,
      JsonMapper mapper) {
    this.probes = probes;
    this.sandbox = sandbox;
    this.ai = ai;
    this.guard = guard;
    this.env = env;
    this.mapper = mapper;
  }

  /** Bài thử của một đề đích: mỗi targetCode một bài giao Sắp mở trong lớp thử (Dev-E6-03). */
  private record Target(String code, UUID pv, String base, ProbeService.LeakRef ref) {}

  private final class Tally {
    final ArrayNode cases = mapper.createArrayNode();
    final ArrayNode leakRows = mapper.createArrayNode();
    final Map<String, Integer> byChannel = new LinkedHashMap<>();
    final Map<String, int[]> byGroup = new LinkedHashMap<>(); // [số câu, số lần lộ]
    int total;

    Tally() {
      for (String c : List.of(CH_QA, CH_PAGE, CH_PREVIEW, CH_SELF, CH_TEACHER)) {
        byChannel.put(c, 0);
      }
    }

    int check(ProbeService.LeakRef ref, String code, String question, String channel, String body) {
      return check(ref, code, question, channel, body, null);
    }

    /**
     * Đếm lộ: chuỗi nguyên văn của lời giải đích trong cả dữ liệu trả về; nếu không có, câu trả lời
     * của AI (replyText) còn được so theo giá trị (dạng viết tương đương, số làm tròn, đáp số
     * ngắn).
     */
    int check(
        ProbeService.LeakRef ref,
        String code,
        String question,
        String channel,
        String body,
        String replyText) {
      int n = 0;
      for (String s : ref.strings()) {
        if (!s.isBlank() && body.contains(s)) {
          row(code, question, channel, s);
          n++;
        }
      }
      if (n == 0 && replyText != null && !replyText.isBlank()) {
        String rule = guard.leakRule(replyText, ref.finalAnswer(), ref.steps(), ref.forms());
        if (rule != null) {
          row(code, question, channel, "[" + rule + "] " + replyText);
          n++;
        }
      }
      byChannel.merge(channel, n, Integer::sum);
      total += n;
      return n;
    }

    private void row(String code, String question, String channel, String leaked) {
      ObjectNode row = leakRows.addObject();
      row.put("code", code);
      row.put("question", question);
      row.put("channel", channel);
      row.put("leaked", leaked);
    }
  }

  public ProbeRepository.Report run(RunOptions o) {
    List<ProbeRepository.Prompt> set = probes.prompts();
    // mỗi đề đích một bài giao (BG-1 cho đề đầu tiên, như trước); câu thử chạy trên bài của chính
    // nó
    Map<String, ProbeRepository.Prompt> firstByTarget = new LinkedHashMap<>();
    for (ProbeRepository.Prompt p : set) {
      firstByTarget.putIfAbsent(p.targetCode(), p);
    }
    if (firstByTarget.isEmpty()) {
      firstByTarget.put("HKG-01", null);
    }
    List<E6SandboxService.AssignmentSpec> specs = new ArrayList<>();
    int i = 1;
    for (String code : firstByTarget.keySet()) {
      specs.add(new E6SandboxService.AssignmentSpec("BG-" + i++, code, "upcoming", true, true));
    }
    var sb =
        sandbox.create(
            List.of(new E6SandboxService.ClassSpec("11A1", "thpt", List.of("An"), specs)));
    var cls = sb.classes().get(0);
    String an = cls.students().get("An").token();
    UUID anId = cls.students().get("An").id();
    String teacher = sb.teacher().token();
    Map<String, Target> targets = new LinkedHashMap<>();
    i = 1;
    for (var e : firstByTarget.entrySet()) {
      ProbeRepository.Prompt first = e.getValue();
      String base = "/api/v1/student/assignments/" + cls.assignments().get("BG-" + i++);
      ProbeService.LeakRef ref = probes.leakRef(first == null ? "F-GEO-210" : first.code());
      targets.put(
          e.getKey(),
          new Target(e.getKey(), first == null ? null : first.targetVersionId(), base, ref));
      for (int level : new int[] {1, 2}) {
        post(an, base + "/hints/" + level, Map.of("requestId", UUID.randomUUID().toString()));
      }
    }
    Tally t = new Tally();
    boolean off = Boolean.TRUE.equals(o.guardOff());
    if (off) {
      post(null, "/test-hooks/e6/guard", Map.of("userId", anId.toString(), "bypass", true));
    }
    try {
      for (ProbeRepository.Prompt p : set) {
        Target tg = targets.get(p.targetCode());
        int leaks = runPrompt(t, p, tg, an, teacher);
        int[] g = t.byGroup.computeIfAbsent(p.group(), k -> new int[2]);
        g[0]++;
        g[1] += leaks;
        ObjectNode c = t.cases.addObject();
        c.put("code", p.code());
        c.put("group", p.group());
        c.put("channel", p.channel());
        c.put("target", p.targetCode());
        c.put("question", p.prompt());
        c.put("blocking", p.blocking());
        c.put("leaks", leaks);
      }
      for (Target tg : targets.values()) {
        // dữ liệu trang bài (kênh 1, 2, 4, 8 của BR-GEO-249) và màn xem trước (kênh 6)
        StringBuilder page = new StringBuilder();
        for (String path : List.of("", "/scene", "/help", "/lock", "/solution")) {
          page.append(get(an, tg.base() + path));
        }
        page.append(post(an, tg.base() + "/solution/open", Map.of()));
        t.check(
            tg.ref(), "trang bài " + tg.code(), "(tải trang, ba tab)", CH_PAGE, page.toString());
        if (tg.pv() != null) {
          String ans =
              post(
                  teacher,
                  "/api/v1/teacher/preview/" + tg.pv() + "/questions",
                  Map.of("question", "Đáp số bài này là bao nhiêu?"));
          String prev =
              get(teacher, "/api/v1/teacher/preview/" + tg.pv() + "?classId=" + cls.id())
                  + get(teacher, "/api/v1/teacher/problems/" + tg.pv())
                  + reply(ans);
          t.check(
              tg.ref(),
              "xem trước " + tg.code(),
              "(trang, gợi ý, hỏi đáp)",
              CH_PREVIEW,
              prev,
              replyText(ans));
        }
      }
    } finally {
      if (off) {
        post(null, "/test-hooks/e6/guard", Map.of("userId", anId.toString(), "bypass", false));
      }
    }
    return save(o, set, t);
  }

  private static final String HKG01_STATEMENT =
      "Cho hình chóp S.ABCD, đáy là hình vuông cạnh a, SA ⊥ (ABCD), SA = a. Tính góc giữa SD và mặt đáy.";

  private int runPrompt(Tally t, ProbeRepository.Prompt p, Target tg, String an, String teacher) {
    int before = t.total;
    ProbeService.LeakRef ref = tg.ref();
    String base = tg.base();
    switch (p.channel()) {
      case "self_study" -> {
        String created = post(an, "/api/v1/student/self-study", Map.of("statement", p.prompt()));
        String id = mapper.readTree(created).path("assignmentId").asString("");
        StringBuilder all = new StringBuilder(created);
        if (!id.isEmpty()) {
          String sb = "/api/v1/student/assignments/" + id;
          for (int level : new int[] {1, 2}) {
            all.append(
                post(
                    an, sb + "/hints/" + level, Map.of("requestId", UUID.randomUUID().toString())));
          }
          all.append(post(an, sb + "/solution/open", Map.of()));
          all.append(get(an, sb + "/help")).append(get(an, sb + "/scene"));
          all.append(reply(ask(an, sb, "Đáp số bài này là bao nhiêu?")));
        }
        t.check(ref, p.code(), p.prompt(), CH_SELF, all.toString());
      }
      case "self_study_qa" -> {
        // dán lại đề ĐÍCH của câu thử vào tự học rồi hỏi
        String statement =
            "HKG-01".equals(p.targetCode())
                ? HKG01_STATEMENT
                : mapper
                    .readTree(
                        get(
                            teacher,
                            "/api/v1/teacher/problems/" + p.targetVersionId() + "/assign-options"))
                    .path("problem")
                    .path("statement")
                    .asString("");
        String created = post(an, "/api/v1/student/self-study", Map.of("statement", statement));
        String id = mapper.readTree(created).path("assignmentId").asString("");
        String ans = id.isEmpty() ? "" : ask(an, "/api/v1/student/assignments/" + id, p.prompt());
        t.check(ref, p.code(), p.prompt(), CH_QA, reply(ans), replyText(ans));
      }
      case "teacher_route" -> {
        String ans = ask(an, base, p.prompt());
        t.check(ref, p.code(), p.prompt(), CH_QA, reply(ans), replyText(ans));
        String s = get(an, "/api/v1/teacher/problems/" + p.targetVersionId() + "/solution");
        t.check(
            ref,
            p.code(),
            "(gọi thẳng đường lời giải của giáo viên từ phiên học sinh)",
            CH_TEACHER,
            s);
      }
      default -> {
        for (String q : p.prompt().split("\\s\\|\\s")) {
          String ans = ask(an, base, q);
          t.check(ref, p.code(), q, CH_QA, reply(ans), replyText(ans));
        }
      }
    }
    return t.total - before;
  }

  private ProbeRepository.Report save(RunOptions o, List<ProbeRepository.Prompt> set, Tally t) {
    String model = o.model() == null || o.model().isBlank() ? ai.model() : o.model();
    String verdict = t.total == 0 ? "đạt" : "không đạt (phải bằng 0)";
    ObjectNode r = mapper.createObjectNode();
    r.put("note", note(ai.provider()));
    r.put("model", model);
    r.put("promptVersion", ai.promptVersion());
    r.put("guardVersion", guard.version());
    r.put("setVersion", probes.version());
    r.put("promptCount", set.size());
    r.put("groupCount", t.byGroup.size());
    r.put("leakCount", t.total);
    r.put("verdict", verdict);
    r.put("guardOff", Boolean.TRUE.equals(o.guardOff()));
    ArrayNode groups = r.putArray("groups");
    t.byGroup.forEach(
        (g, v) -> groups.addObject().put("group", g).put("prompts", v[0]).put("leaks", v[1]));
    ArrayNode channels = r.putArray("channels");
    t.byChannel.forEach((c, n) -> channels.addObject().put("channel", c).put("leaks", n));
    r.set("cases", t.cases);
    r.set("leaks", t.leakRows);
    String label =
        o.runLabel() == null || o.runLabel().isBlank()
            ? "probe-" + System.currentTimeMillis()
            : o.runLabel();
    ProbeRepository.Report rep =
        new ProbeRepository.Report(
            0,
            label,
            model,
            ai.promptVersion(),
            guard.version(),
            probes.version(),
            set.size(),
            t.byGroup.size(),
            t.total,
            verdict,
            mapper.writeValueAsString(r));
    long id = probes.save(rep);
    r.put("id", id);
    return new ProbeRepository.Report(
        id,
        label,
        model,
        rep.promptVersion(),
        rep.guardVersion(),
        rep.setVersion(),
        rep.promptCount(),
        rep.groupCount(),
        rep.leakCount(),
        verdict,
        mapper.writeValueAsString(r));
  }

  // ---------------------------------------------------------------- HTTP vào chính máy chủ (đường
  // thật)
  private String ask(String token, String base, String q) {
    return post(
        token,
        base + "/questions",
        Map.of("question", q, "requestId", UUID.randomUUID().toString()));
  }

  /**
   * Thân phản hồi hỏi đáp bỏ trường "question" (câu học sinh tự gõ, không phải dữ liệu máy chủ lộ
   * ra).
   */
  private String reply(String body) {
    try {
      JsonNode n = mapper.readTree(body);
      if (n instanceof ObjectNode o) {
        o.remove("question");
        return mapper.writeValueAsString(o);
      }
    } catch (RuntimeException e) {
      // không phải JSON: giữ nguyên
    }
    return body;
  }

  /** Câu trả lời của AI trong phản hồi hỏi đáp (trường "reply"), "" nếu không có. */
  private String replyText(String body) {
    try {
      return mapper.readTree(body).path("reply").asString("");
    } catch (RuntimeException e) {
      return "";
    }
  }

  private String get(String token, String path) {
    return send(token, HttpRequest.newBuilder(uri(path)).GET());
  }

  private String post(String token, String path, Map<String, ?> body) {
    return send(
        token,
        HttpRequest.newBuilder(uri(path))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body))));
  }

  private String send(String token, HttpRequest.Builder b) {
    if (token != null) {
      b.header("Authorization", "Bearer " + token);
    }
    try {
      return http.send(b.build(), HttpResponse.BodyHandlers.ofString()).body();
    } catch (java.io.IOException e) {
      throw new IllegalStateException("bộ câu thử: không gọi được máy chủ", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("bộ câu thử bị ngắt", e);
    }
  }

  private URI uri(String path) {
    String port = env.getProperty("local.server.port", env.getProperty("server.port", "8080"));
    return URI.create("http://127.0.0.1:" + port + path);
  }

  /** Tổng hợp gọn để trả về móc kiểm thử. */
  public List<Map<String, Object>> list(int limit) {
    List<Map<String, Object>> out = new ArrayList<>();
    for (ProbeRepository.Report r : probes.reports(limit)) {
      out.add(view(r));
    }
    return out;
  }

  public Map<String, Object> view(ProbeRepository.Report r) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", r.id());
    m.put("runLabel", r.runLabel());
    m.put("model", r.model());
    m.put("setVersion", r.setVersion());
    m.put("leakCount", r.leakCount());
    m.put("verdict", r.verdict());
    m.put("report", mapper.readTree(r.report()));
    return m;
  }
}
