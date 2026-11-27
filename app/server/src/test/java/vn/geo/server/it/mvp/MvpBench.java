package vn.geo.server.it.mvp;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.aigateway.api.AiGateway;
import vn.geo.server.aigateway.api.AiRequest;
import vn.geo.server.aigateway.internal.AiSettings;
import vn.geo.server.aigateway.internal.MockAiControl;
import vn.geo.server.assignment.internal.service.ProbeRunner;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.content.internal.repo.ContentRepository;
import vn.geo.server.content.internal.service.AnswerExpr;
import vn.geo.server.content.internal.service.ContentPackLoader;
import vn.geo.server.content.internal.service.SceneChecker;
import vn.geo.server.geometry.api.GeometryEngine;
import vn.geo.server.it.e1.AbstractE1IT;

/**
 * Bộ đo MVP (MVP.md §9, PO-60) — {@code make bench-mvp}. KHÔNG chạy trong {@code make verify} (tên
 * lớp không khớp mẫu *IT/*Test; scripts/bench-mvp.sh gọi tường minh). Nhà cung cấp AI theo cấu hình
 * (GEO_AI_PROVIDER: mock cho CI, anthropic cho đo thật).
 *
 * <p>Bộ đề chuẩn = hợp các mã có trong {@code geometry/problem-specs.json}, {@code
 * content/scene-specs.json}, {@code mock-ai/benchmark.json} (đọc tổng quát: thêm đề vào các tệp này
 * là bộ đo tự phủ); đề bài, đáp số tham chiếu lấy từ kho đề đã nạp (dữ liệu vàng). Với mỗi đề:
 *
 * <ol>
 *   <li>AI "solution" như BenchmarkRunner (US-GEO-105): hình thỏa ràng buộc (SceneChecker; đề không
 *       có trong scene-specs thì ràng buộc của engine), đáp số khớp tham chiếu (AnswerExpr; thiếu
 *       tham chiếu thì engine.verify), ghi nhãn máy qua fn_record_verification → đếm "sai mà Đã
 *       kiểm chứng".
 *   <li>Gửi đề vào Tự học như học sinh (đường thật, HTTP): đo thời gian gửi đề → có hình.
 *   <li>Chi phí AI mỗi bài: tổng cost_micro_usd của ai_call_log các lượt của đề (usage × bảng giá
 *       geo.ai.pricing; mock = chi phí mô phỏng).
 * </ol>
 *
 * Lộ lời giải: bộ câu thử sẵn có (ProbeRunner, US-GEO-610) qua mọi kênh. Xuất {@code
 * artifacts/GEO/evidence/MVP-METRICS-<yyyymmdd-HHmm>/report.md + report.json}; chỉ số bắt buộc
 * trượt thì test đỏ (mã thoát ≠ 0).
 */
@Tag("bench")
class MvpBench extends AbstractE1IT {

  static final double SCENE_MIN = 0.95;
  static final double ANSWER_MIN = 0.90;
  static final double MEDIAN_SCENE_MAX_MS = 40_000;
  static final List<String> SOURCES =
      List.of(
          "/geometry/problem-specs.json", "/content/scene-specs.json", "/mock-ai/benchmark.json");

  @Autowired AiGateway ai;
  @Autowired AiSettings settings;
  @Autowired MockAiControl mock;
  @Autowired SceneChecker scenes;
  @Autowired GeometryEngine engine;
  @Autowired ContentRepository content;
  @Autowired ProbeRunner probes;
  @Autowired ContentPackLoader contentPack;

  @Value("${geo.ai.provider:mock}")
  String provider;

  /** Phạm vi đề đã đo (ghi vào báo cáo). */
  String scope = "";

  /** Một đề trong kho đã nạp. */
  record Problem(String code, UUID versionId, String statement, String finalAnswer) {}

  @Test
  void bench() throws Exception {
    reseed();
    // Kho đề MVP (content/catalog-mvp.json): profile test tắt nạp lúc khởi động -> nạp tường minh.
    contentPack.load();
    mock.reset();
    settings.reset();
    TreeSet<String> codes = new TreeSet<>();
    TreeSet<String> sceneSpecCodes = new TreeSet<>();
    for (String src : SOURCES) {
      JsonNode root = resource(src);
      for (var e : root.properties()) {
        if (!e.getKey().startsWith("_")) {
          codes.add(e.getKey());
          if (src.contains("scene-specs")) {
            sceneSpecCodes.add(e.getKey());
          }
        }
      }
    }
    // AI giả lập chỉ có câu trả lời mẫu cho các đề trong mock-ai/benchmark.json: đo bằng mock chỉ
    // là
    // kiểm hồi quy đường đo (CI) trên các đề đó; đo đủ kho đề (gồm gói HKN) bằng AI thật.
    if ("mock".equals(provider)) {
      TreeSet<String> mockCodes = new TreeSet<>();
      resource("/mock-ai/benchmark.json")
          .properties()
          .forEach(e -> {
            if (!e.getKey().startsWith("_")) {
              mockCodes.add(e.getKey());
            }
          });
      int all = codes.size();
      codes.retainAll(mockCodes);
      scope =
          "mock: " + codes.size() + "/" + all + " đề có câu trả lời mẫu; đủ kho chỉ đo với AI thật";
    } else {
      scope = "đủ kho: " + codes.size() + " đề";
    }
    String an = an();
    List<Map<String, Object>> rows = new ArrayList<>();
    for (String code : codes) {
      rows.add(runOne(code, sceneSpecCodes.contains(code), an));
    }
    var rep = probes.run(new ProbeRunner.RunOptions("bench-mvp", null, false));
    Map<String, Object> probe = new LinkedHashMap<>(probes.view(rep));
    probe.put("promptCount", rep.promptCount());
    writeAndAssert(rows, probe);
  }

  private Map<String, Object> runOne(String code, boolean hasSceneSpec, String an)
      throws Exception {
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("code", code);
    Optional<Problem> found = problem(code);
    if (found.isEmpty()) {
      row.put("error", "không có trong kho đề đã nạp");
      row.put("sceneOk", false);
      row.put("answerOk", false);
      row.put("label", "-");
      return row;
    }
    Problem p = found.get();
    long logFrom = maxLogId();
    // 1) đường bộ đề chuẩn (như BenchmarkRunner)
    String aiAnswer = "";
    JsonNode points = null;
    String error = null;
    long t0 = System.nanoTime();
    try {
      JsonNode out =
          mapper.readTree(
              ai.complete(
                      new AiRequest("solution", "Mã bài: " + code + "\nĐề bài: " + p.statement()))
                  .content());
      aiAnswer = out.path("answer").asString("");
      points = out.get("points");
    } catch (AppException | tools.jackson.core.JacksonException e) {
      error = "AI: " + e.getMessage();
    }
    long solutionMs = (System.nanoTime() - t0) / 1_000_000;
    boolean sceneOk = error == null && sceneOk(code, hasSceneSpec, points);
    boolean answerOk = error == null && answerOk(code, p, aiAnswer);
    Map<String, Object> checks = new LinkedHashMap<>();
    checks.put("scene_ok", sceneOk);
    checks.put("answer_ok", answerOk);
    checks.put("source", "bench-mvp");
    String label =
        content.recordVerification(
            p.versionId(), Uuid7.next(), sceneOk, answerOk, mapper.writeValueAsString(checks));
    // 2) gửi đề vào Tự học như học sinh: thời gian tới khi có hình
    long t1 = System.nanoTime();
    var resp =
        postJson(
            "/api/v1/student/self-study",
            an,
            mapper.writeValueAsString(Map.of("statement", p.statement())));
    long sceneMs = (System.nanoTime() - t1) / 1_000_000;
    boolean figure = false;
    if (resp.statusCode() == 200) {
      JsonNode s = mapper.readTree(resp.body()).path("scene");
      figure = s.path("points").isArray() && !s.path("points").isEmpty();
    }
    // 3) chi phí và token của mọi lượt AI của đề
    Map<String, Object> cost =
        jdbc.sql(
                """
                SELECT count(*) AS calls, coalesce(sum(cost_micro_usd), 0) AS cost,
                       coalesce(sum(tokens_in), 0) AS tin, coalesce(sum(tokens_out), 0) AS tout
                FROM ai_call_log WHERE id > :from
                """)
            .param("from", logFrom)
            .query()
            .singleRow();
    row.put("sceneOk", sceneOk);
    row.put("answerOk", answerOk);
    row.put("label", "verified".equals(label) ? GeometryEngine.LABEL_VERIFIED : "Cần xem lại");
    row.put("wrongVerified", "verified".equals(label) && !(sceneOk && answerOk));
    row.put("aiAnswer", aiAnswer);
    row.put("reference", p.finalAnswer());
    row.put("solutionMs", solutionMs);
    row.put("selfStudyStatus", resp.statusCode());
    row.put("figure", figure);
    row.put("submitToFigureMs", sceneMs);
    row.put("aiCalls", ((Number) cost.get("calls")).longValue());
    row.put("tokensIn", ((Number) cost.get("tin")).longValue());
    row.put("tokensOut", ((Number) cost.get("tout")).longValue());
    row.put("costMicroUsd", ((Number) cost.get("cost")).longValue());
    if (error != null) {
      row.put("error", error);
    }
    return row;
  }

  private boolean sceneOk(String code, boolean hasSceneSpec, JsonNode points) {
    return new BenchSceneCheck(scenes, engine).ok(code, hasSceneSpec, points);
  }

  private boolean answerOk(String code, Problem p, String aiAnswer) {
    if (p.finalAnswer() != null && !p.finalAnswer().isBlank()) {
      return AnswerExpr.matches(aiAnswer, AnswerExpr.parse(p.finalAnswer()));
    }
    var spec = engine.catalogSpec(code);
    if (spec.isEmpty() || spec.get().asked().isEmpty()) {
      return false;
    }
    try {
      return engine.verify(spec.get(), aiAnswer).answerOk();
    } catch (RuntimeException e) {
      return false;
    }
  }

  private Optional<Problem> problem(String code) {
    return jdbc.sql(
            """
            SELECT p.code, pv.id, pv.statement, s.final_answer
            FROM problem p JOIN problem_version pv ON pv.org_id = p.org_id AND pv.id = p.current_version_id
            LEFT JOIN solution s ON s.org_id = pv.org_id AND s.problem_version_id = pv.id
            WHERE p.org_id = platform_org_id() AND p.code = :code
            """)
        .param("code", code)
        .query((rs, i) ->
            new Problem(
                rs.getString(1), rs.getObject(2, UUID.class), rs.getString(3), rs.getString(4)))
        .optional();
  }

  private long maxLogId() {
    return jdbc.sql("SELECT coalesce(max(id), 0) FROM ai_call_log").query(Long.class).single();
  }

  // ------------------------------------------------------------------ báo cáo

  private void writeAndAssert(List<Map<String, Object>> rows, Map<String, Object> probe)
      throws IOException {
    int n = rows.size();
    long scene = rows.stream().filter(r -> Boolean.TRUE.equals(r.get("sceneOk"))).count();
    long answer = rows.stream().filter(r -> Boolean.TRUE.equals(r.get("answerOk"))).count();
    long wrong = rows.stream().filter(r -> Boolean.TRUE.equals(r.get("wrongVerified"))).count();
    long verified =
        rows.stream().filter(r -> GeometryEngine.LABEL_VERIFIED.equals(r.get("label"))).count();
    int leaks = ((Number) probe.getOrDefault("leakCount", -1)).intValue();
    int probeCount = ((Number) probe.getOrDefault("promptCount", 0)).intValue();
    List<Long> times =
        rows.stream()
            .filter(r -> Boolean.TRUE.equals(r.get("figure")))
            .map(r -> ((Number) r.get("submitToFigureMs")).longValue())
            .sorted()
            .toList();
    Long median = times.isEmpty() ? null : median(times);
    List<Long> costs =
        rows.stream()
            .filter(r -> r.containsKey("costMicroUsd"))
            .map(r -> ((Number) r.get("costMicroUsd")).longValue())
            .sorted()
            .toList();
    long costTotal = costs.stream().mapToLong(Long::longValue).sum();
    double sceneRate = n == 0 ? 0 : (double) scene / n;
    double answerRate = n == 0 ? 0 : (double) answer / n;
    boolean sceneP = n > 0 && sceneRate >= SCENE_MIN;
    boolean answerP = n > 0 && answerRate >= ANSWER_MIN;
    boolean wrongP = n > 0 && wrong == 0;
    boolean leakP = probeCount > 0 && leaks == 0;
    boolean medianP = median != null && median < MEDIAN_SCENE_MAX_MS;
    boolean mandatory = sceneP && answerP && wrongP && leakP;

    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
    String stamp = now.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
    String outBase = System.getenv().getOrDefault("GEO_BENCH_OUT", "../../artifacts/GEO/evidence");
    Path dir = Path.of(outBase, "MVP-METRICS-" + stamp).toAbsolutePath().normalize();
    Files.createDirectories(dir);

    ObjectNode json = mapper.createObjectNode();
    json.put("generatedAt", now.toOffsetDateTime().toString());
    json.put("provider", provider);
    json.put("model", ai.model());
    json.put("promptVersion", ai.promptVersion());
    json.put("pricingSource", PRICING_SOURCE);
    json.put("n", n);
    ObjectNode m = json.putObject("metrics");
    metric(m, "sceneSatisfied", sceneRate, ">= " + SCENE_MIN, sceneP, true);
    metric(m, "answerCorrect", answerRate, ">= " + ANSWER_MIN, answerP, true);
    metric(m, "wrongButVerified", wrong, "= 0", wrongP, true);
    metric(m, "leaksBeforeRelease", leaks, "= 0", leakP, true);
    metric(m, "medianSubmitToFigureMs", median, "< " + (long) MEDIAN_SCENE_MAX_MS, medianP, false);
    ObjectNode c = m.putObject("aiCostPerProblemUsd");
    c.put("mean", n == 0 ? 0 : costTotal / 1e6 / n);
    c.put("median", costs.isEmpty() ? 0 : median(costs) / 1e6);
    c.put("total", costTotal / 1e6);
    c.put("threshold", "đo, chưa đặt ngưỡng");
    json.put("verifiedCount", verified);
    json.set("probe", mapper.valueToTree(probe));
    json.set("problems", mapper.valueToTree(rows));
    json.put("mandatoryPass", mandatory);
    Files.writeString(
        dir.resolve("report.json"),
        mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
    Files.writeString(
        dir.resolve("report.md"),
        markdown(
            now,
            rows,
            n,
            sceneRate,
            answerRate,
            wrong,
            leaks,
            probeCount,
            median,
            costs,
            costTotal,
            sceneP,
            answerP,
            wrongP,
            leakP,
            medianP,
            mandatory));
    System.out.println("BENCH-MVP báo cáo: " + dir);

    assertThat(mandatory)
        .as(
            "chỉ số bắt buộc MVP.md §9: hình %.1f%% (≥95), đáp số %.1f%% (≥90), sai mà Đã kiểm chứng %d"
                + " (=0), lộ %d (=0) — xem %s",
            sceneRate * 100, answerRate * 100, wrong, leaks, dir)
        .isTrue();
  }

  static final String PRICING_SOURCE =
      "geo.ai.pricing (application.yml): bảng giá Anthropic theo skill claude-api, cached"
          + " 2026-11-03, chép 2026-11-26; mock = chi phí mô phỏng geo.ai.cost-per-call";

  private static long median(List<Long> sorted) {
    int k = sorted.size();
    return k % 2 == 1 ? sorted.get(k / 2) : (sorted.get(k / 2 - 1) + sorted.get(k / 2)) / 2;
  }

  private void metric(
      ObjectNode m, String name, Object value, String threshold, boolean pass, boolean mandatory) {
    ObjectNode o = m.putObject(name);
    o.set("value", mapper.valueToTree(value));
    o.put("threshold", threshold);
    o.put("pass", pass);
    o.put("mandatory", mandatory);
  }

  private static String pf(boolean b) {
    return b ? "Pass" : "**Fail**";
  }

  private String markdown(
      ZonedDateTime now,
      List<Map<String, Object>> rows,
      int n,
      double sceneRate,
      double answerRate,
      long wrong,
      int leaks,
      int probeCount,
      Long median,
      List<Long> costs,
      long costTotal,
      boolean sceneP,
      boolean answerP,
      boolean wrongP,
      boolean leakP,
      boolean medianP,
      boolean mandatory) {
    StringBuilder b = new StringBuilder();
    b.append("# Bộ đo MVP (MVP.md §9) · ").append(now.toOffsetDateTime()).append("\n\n");
    b.append("- Nhà cung cấp AI: `").append(provider).append("` · model `").append(ai.model());
    b.append("` · prompt `").append(ai.promptVersion()).append("`\n");
    b.append("- Bộ đề chuẩn: ")
        .append(n)
        .append(" đề (hợp mã của ")
        .append(String.join(", ", SOURCES))
        .append("; đề + tham chiếu từ kho đã nạp) · phạm vi ")
        .append(scope)
        .append("\n");
    b.append("- Giá: ").append(PRICING_SOURCE).append("\n");
    b.append("- Kết luận chỉ số bắt buộc: ")
        .append(mandatory ? "**ĐẠT**" : "**TRƯỢT**")
        .append("\n\n");
    b.append("| Chỉ số | Giá trị | Ngưỡng | Kết quả |\n|---|---|---|---|\n");
    b.append(
        String.format(
            "| Hình thỏa ràng buộc (bắt buộc) | %.1f%% | ≥ 95%% | %s |%n",
            sceneRate * 100, pf(sceneP)));
    b.append(
        String.format(
            "| Đáp số đúng (bắt buộc) | %.1f%% | ≥ 90%% | %s |%n", answerRate * 100, pf(answerP)));
    b.append(
        String.format(
            "| Bài sai mà gắn \"Đã kiểm chứng\" (bắt buộc) | %d | 0 | %s |%n", wrong, pf(wrongP)));
    b.append(
        String.format(
            "| Lộ lời giải/đáp số trước giờ mở (bắt buộc, %d câu thử) | %d | 0 | %s |%n",
            probeCount, leaks, pf(leakP)));
    b.append(
        String.format(
            "| Trung vị gửi đề → có hình | %s | < 40 s | %s |%n",
            median == null ? "không có hình nào" : String.format("%.2f s", median / 1000.0),
            pf(medianP)));
    b.append(
        String.format(
            "| Chi phí AI mỗi bài | TB %.6f USD · trung vị %.6f USD · tổng %.6f USD | đo | - |%n%n",
            n == 0 ? 0 : costTotal / 1e6 / n,
            costs.isEmpty() ? 0 : median(costs) / 1e6,
            costTotal / 1e6));
    b.append(
        "| Mã | Hình | Đáp số | Nhãn | Sai mà ĐKC | AI đáp số | Tham chiếu | Gửi đề→hình (ms) | Lượt AI | Token vào/ra | Chi phí (USD) | Lỗi |\n");
    b.append("|---|---|---|---|---|---|---|---|---|---|---|---|\n");
    for (Map<String, Object> r : rows) {
      b.append("| ")
          .append(r.get("code"))
          .append(" | ")
          .append(yes(r.get("sceneOk")))
          .append(" | ")
          .append(yes(r.get("answerOk")))
          .append(" | ")
          .append(r.getOrDefault("label", "-"))
          .append(" | ")
          .append(yes(r.get("wrongVerified")))
          .append(" | ")
          .append(cell(r.get("aiAnswer")))
          .append(" | ")
          .append(cell(r.get("reference")))
          .append(" | ")
          .append(
              Boolean.TRUE.equals(r.get("figure"))
                  ? r.get("submitToFigureMs")
                  : "không có hình (HTTP " + r.getOrDefault("selfStudyStatus", "-") + ")")
          .append(" | ")
          .append(r.getOrDefault("aiCalls", "-"))
          .append(" | ")
          .append(r.getOrDefault("tokensIn", "-"))
          .append("/")
          .append(r.getOrDefault("tokensOut", "-"))
          .append(" | ")
          .append(
              r.containsKey("costMicroUsd")
                  ? String.format("%.6f", ((Number) r.get("costMicroUsd")).longValue() / 1e6)
                  : "-")
          .append(" | ")
          .append(cell(r.get("error")))
          .append(" |\n");
    }
    return b.toString();
  }

  private static String yes(Object o) {
    return o == null ? "-" : Boolean.TRUE.equals(o) ? "✓" : "✗";
  }

  private static String cell(Object o) {
    return o == null ? "" : o.toString().replace("|", "\\|").replace("\n", " ");
  }

  private JsonNode resource(String path) throws IOException {
    try (InputStream in = MvpBench.class.getResourceAsStream(path)) {
      return in == null ? mapper.createObjectNode() : mapper.readTree(in);
    }
  }
}
