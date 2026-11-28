package vn.geo.server.it.s2e2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.content.internal.plane.repo.PlaneRepository;
import vn.geo.server.content.internal.plane.service.PlaneBenchCalc;
import vn.geo.server.content.internal.plane.service.PlaneBenchRunner;
import vn.geo.server.content.internal.plane.service.PlaneBenchmarkLoader;
import vn.geo.server.content.internal.plane.service.PlaneBenchmarkLoader.LoadReport;
import vn.geo.server.geometry.api.GeometryEngine;
import vn.geo.server.it.e1.AbstractE1IT;
import vn.geo.server.subject.api.ProblemKind;
import vn.geo.server.subject.api.SubjectEngine;
import vn.geo.server.subject.api.SubjectRegistry;
import vn.geo.server.subject.internal.SubjectService;

/**
 * S2-E2 — tích hợp: môn không gian MVP qua khung (US-S2E2-01-SC2, FC4), lệnh nạp bộ đề chuẩn hình
 * phẳng (US-S2E2-04), bench-s2 trên bộ đã nạp (US-S2E2-05).
 */
class PlaneS2E2IT extends AbstractE1IT {

  @Autowired SubjectRegistry registry;
  @Autowired GeometryEngine engine;
  @Autowired PlaneBenchmarkLoader loader;
  @Autowired PlaneBenchRunner runner;
  @Autowired PlaneRepository repo;

  JsonNode defaultSet() {
    return mapper.readTree(PlaneS2E2IT.class.getResourceAsStream(PlaneBenchmarkLoader.DEFAULT));
  }

  ObjectNode setOf(List<JsonNode> problems) {
    ObjectNode root = mapper.createObjectNode();
    ArrayNode arr = root.putArray("problems");
    problems.forEach(p -> arr.add(p.deepCopy()));
    return root;
  }

  List<JsonNode> problems() {
    List<JsonNode> out = new java.util.ArrayList<>();
    defaultSet().path("problems").forEach(out::add);
    return out;
  }

  @Test
  @DisplayName("TC-S2E2-005 Môn không gian của MVP không đổi kết quả qua khung mới")
  void tc005_mvpUnchanged() {
    JsonNode catalog =
        mapper.readTree(PlaneS2E2IT.class.getResourceAsStream("/content/catalog-mvp.json"));
    SubjectEngine geo = registry.forKind(ProblemKind.SPACE);
    int compared = 0;
    for (JsonNode p : catalog.path("problems")) {
      String code = p.path("code").asString();
      var spec = engine.catalogSpec(code);
      if (spec.isEmpty()) {
        continue;
      }
      String answer = p.path("answer").asString();
      GeometryEngine.Verification before = engine.verify(spec.get(), answer);
      ObjectNode data = mapper.createObjectNode().put("problemCode", code);
      var parsed = geo.parse(new SubjectEngine.ProblemInput(p.path("statement").asString(), data));
      assertThat(parsed.kind()).as(code).isEqualTo(ProblemKind.SPACE);
      SubjectEngine.Check after = geo.verify(parsed, answer);
      assertThat(after.label()).as(code).isEqualTo(before.label());
      assertThat(after.sceneOk()).as(code).isEqualTo(before.sceneOk());
      assertThat(after.answerOk()).as(code).isEqualTo(before.answerOk());
      compared++;
    }
    assertThat(compared).isGreaterThanOrEqualTo(40);
  }

  /** Môn thử: solve ném lỗi (hoặc treo). */
  static class Broken implements SubjectEngine {
    final String id;
    final boolean hang;

    Broken(String id, boolean hang) {
      this.id = id;
      this.hang = hang;
    }

    @Override
    public String id() {
      return id;
    }

    @Override
    public String name() {
      return "Môn lỗi";
    }

    @Override
    public Set<ProblemKind> kinds() {
      return Set.of(ProblemKind.PLANE);
    }

    @Override
    public ParsedProblem parse(ProblemInput in) {
      return new ParsedProblem(id, ProblemKind.PLANE, in.statement(), in.data());
    }

    @Override
    public Solution solve(ParsedProblem p) {
      if (hang) {
        try {
          Thread.sleep(10_000);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
      throw new IllegalStateException("lỗi giữa chừng");
    }

    @Override
    public Check verify(ParsedProblem p, String a) {
      throw new IllegalStateException();
    }

    @Override
    public Rendering render(ParsedProblem p) {
      throw new IllegalStateException();
    }

    @Override
    public List<Hint> hint(ParsedProblem p) {
      return List.of();
    }

    @Override
    public GradeResult grade(String r, String s, Double t) {
      return new GradeResult(false, null, null);
    }

    @Override
    public List<String> concepts() {
      return List.of("thu");
    }
  }

  @Test
  @DisplayName("TC-S2E2-007 Môn lỗi giữa chừng không làm sập khung")
  void tc007_brokenSubject() {
    SubjectEngine geo = registry.require("geometry");
    SubjectService svc =
        new SubjectService(
            new SubjectRegistry(List.of(geo, new Broken("loi", false), new Broken("treo", true))),
            Duration.ofMillis(500));
    var input = new SubjectEngine.ProblemInput("đề", mapper.createObjectNode());
    assertThatThrownBy(() -> svc.solve("loi", input))
        .isInstanceOfSatisfying(
            AppException.class,
            e -> assertThat(e.code()).isEqualTo(ErrorCode.DEPENDENCY_UNAVAILABLE));
    assertThatThrownBy(() -> svc.solve("treo", input))
        .isInstanceOfSatisfying(
            AppException.class, e -> assertThat(e.code()).isEqualTo(ErrorCode.TIMEOUT));
    // vẫn phục vụ môn khác
    JsonNode pl01 = problems().get(0).path("spec");
    SubjectService.Solved ok = svc.solve("geometry", new SubjectEngine.ProblemInput("PL-01", pl01));
    assertThat(ok.solution().answer()).isEqualTo("90°");
  }

  @Test
  @DisplayName("TC-S2E2-031 Bộ chuẩn có đủ 30 đề với đủ trường")
  void tc031_load30() {
    LoadReport r = loader.loadDefault();
    assertThat(r.errors()).isEmpty();
    assertThat(r.warnings()).isEmpty();
    assertThat(r.exitCode()).isZero();
    assertThat(r.loaded()).hasSize(30);
    List<PlaneRepository.BenchRow> rows = repo.benchmark();
    assertThat(rows).hasSize(30);
    assertThat(rows)
        .allSatisfy(x -> {
          assertThat(x.code()).matches("PLS-\\d{2}");
          assertThat(x.statement()).isNotBlank();
          assertThat(mapper.readTree(x.spec()).path("given")).isNotEmpty();
          assertThat(x.shape()).isNotBlank();
          assertThat(x.answer()).isNotBlank();
          assertThat(x.answerType()).isNotBlank();
          assertThat(x.reviewer()).isNotBlank();
        });
  }

  @Test
  @DisplayName("TC-S2E2-032 Mỗi dạng hỗ trợ có tối thiểu 5 đề")
  void tc032_perForm() {
    LoadReport r = loader.loadDefault();
    assertThat(r.byForm()).containsOnlyKeys("tam giác", "tứ giác", "đường tròn");
    assertThat(r.byForm().values()).allSatisfy(n -> assertThat(n).isGreaterThanOrEqualTo(5));
    for (String f : List.of("tam giác", "tứ giác", "đường tròn")) {
      assertThat(repo.benchmark().stream().filter(x -> x.form().equals(f)).count())
          .as(f)
          .isGreaterThanOrEqualTo(5);
    }
  }

  @Test
  @DisplayName("TC-S2E2-033 Bốn đề mẫu FX-10…FX-13 có trong bộ với đáp số khớp")
  void tc033_fxProblems() {
    loader.loadDefault();
    var byAlias = new java.util.HashMap<String, String>();
    repo.benchmark().stream()
        .filter(x -> x.alias() != null)
        .forEach(x -> byAlias.put(x.alias(), x.answer()));
    assertThat(byAlias)
        .containsEntry("PL-01", "90°")
        .containsEntry("PL-02", "70°")
        .containsEntry("PL-03", "10")
        .containsEntry("PL-04", "4√3");
  }

  @Test
  @DisplayName("TC-S2E2-034 Đề thiếu đáp số tham chiếu bị từ chối")
  void tc034_missingAnswer() {
    List<JsonNode> ps = problems();
    ((ObjectNode) ps.get(5)).remove("answer");
    String code = ps.get(5).path("code").asString();
    LoadReport r = loader.load(setOf(ps));
    assertThat(r.errors())
        .anySatisfy(e -> {
          assertThat(e.code()).isEqualTo(code);
          assertThat(e.message()).contains("thiếu đáp số tham chiếu");
        });
    assertThat(r.loaded()).hasSize(29).doesNotContain(code);
    assertThat(r.exitCode()).isNotZero();
    assertThat(repo.benchmark()).hasSize(29);
  }

  @Test
  @DisplayName("TC-S2E2-035 Trùng mã đề bị từ chối")
  void tc035_duplicate() {
    List<JsonNode> ps = problems();
    JsonNode dup =
        ps.stream()
            .filter(p -> p.path("code").asString().equals("PLS-07"))
            .findFirst()
            .orElseThrow();
    ps.add(dup);
    LoadReport r = loader.load(setOf(ps));
    assertThat(r.errors())
        .singleElement()
        .satisfies(e -> {
          assertThat(e.code()).isEqualTo("PLS-07");
          assertThat(e.message()).isEqualTo("trùng mã PLS-07");
        });
    assertThat(r.loaded().stream().filter("PLS-07"::equals)).hasSize(1);
    assertThat(r.exitCode()).isNotZero();
  }

  @Test
  @DisplayName("TC-S2E2-036 Đáp số tham chiếu lệch engine bị báo")
  void tc036_wrongReference() {
    List<JsonNode> ps = problems();
    ObjectNode pl03 =
        (ObjectNode)
            ps.stream()
                .filter(p -> "PL-03".equals(p.path("alias").asString("")))
                .findFirst()
                .orElseThrow();
    pl03.put("answer", "12");
    LoadReport r = loader.load(setOf(ps));
    assertThat(r.errors())
        .singleElement()
        .satisfies(e -> {
          assertThat(e.code()).isEqualTo("PLS-03");
          assertThat(e.message()).isEqualTo("đáp số tham chiếu 12 lệch kết quả engine 10");
        });
    assertThat(repo.benchmark())
        .extracting(PlaneRepository.BenchRow::code)
        .doesNotContain("PLS-03");
    assertThat(r.exitCode()).isNotZero();
  }

  @Test
  @DisplayName("TC-S2E2-037 Bộ 29 đề bị báo thiếu, 30 đề đạt")
  void tc037_minimum() {
    List<JsonNode> ps = problems();
    LoadReport r29 = loader.load(setOf(ps.subList(0, 29)));
    assertThat(r29.warnings()).anySatisfy(w -> assertThat(w).contains("dưới 30 đề"));
    assertThat(r29.exitCode()).isNotZero();
    LoadReport r30 = loader.load(setOf(ps.subList(0, 30)));
    assertThat(r30.warnings()).isEmpty();
    assertThat(r30.exitCode()).isZero();
  }

  @Test
  @DisplayName("TC-S2E2-038 Báo cáo có đủ ba chỉ số và danh sách đề rớt")
  void tc038_report() {
    loader.loadDefault();
    PlaneBenchCalc.Report r = runner.run("mock", "");
    assertThat(r.total()).isEqualTo(30);
    assertThat(r.runAt()).matches("\\d{2}:\\d{2}, \\d{2}/\\d{2}/\\d{4}");
    String md = r.markdown();
    assertThat(md)
        .contains("Ngày chạy: " + r.runAt())
        .contains("Số đề: 30")
        .contains("M-S2-01")
        .contains("M-S2-02")
        .contains("M-S2-03")
        .contains("## Đề rớt")
        .contains("(rỗng)");
    assertThat(r.failed()).isEmpty();
  }

  @Test
  @DisplayName("TC-S2E2-039 Đạt cả ba ngưỡng thì thoát 0")
  void tc039_pass() {
    loader.loadDefault();
    PlaneBenchCalc.Report r = runner.run("mock", "");
    assertThat(r.m1Pass()).isTrue();
    assertThat(r.m2Pass()).isTrue();
    assertThat(r.m3Pass()).isTrue();
    assertThat(r.exitCode()).isZero();
    // kết quả mẫu của TC: 30/30 ràng buộc, 28/30 đáp số, 0 sai mang nhãn → thoát 0
    List<PlaneBenchCalc.Item> sample = new java.util.ArrayList<>();
    for (int i = 1; i <= 30; i++) {
      sample.add(new PlaneBenchCalc.Item("PLS-%02d".formatted(i), true, i <= 28, i <= 28));
    }
    PlaneBenchCalc.Report s = PlaneBenchCalc.evaluate(sample, r.runAt());
    assertThat(s.m1Pass() && s.m2Pass() && s.m3Pass()).isTrue();
    assertThat(s.exitCode()).isZero();
  }

  @Test
  @DisplayName("TC-S2E2-044 Có khóa AI thật thì từ chối chạy")
  void tc044_realAiRefused() {
    loader.loadDefault();
    long calls = count("SELECT count(*) FROM ai_call_log");
    assertThatThrownBy(() -> runner.run("anthropic", "sk-ant-thu"))
        .isInstanceOf(PlaneBenchRunner.RealAiRefused.class)
        .hasMessage("chỉ chạy với AI giả lập");
    assertThatThrownBy(() -> runner.run("mock", "sk-ant-thu"))
        .hasMessage("chỉ chạy với AI giả lập");
    assertThat(count("SELECT count(*) FROM ai_call_log")).isEqualTo(calls);
  }

  @Test
  @DisplayName("TC-S2E2-045 Chạy hai lần cho cùng kết quả")
  void tc045_repeatable() {
    loader.loadDefault();
    PlaneBenchCalc.Report a = runner.run("mock", "");
    PlaneBenchCalc.Report b = runner.run("mock", "");
    assertThat(List.of(b.m1(), b.m2(), b.m3())).isEqualTo(List.of(a.m1(), a.m2(), a.m3()));
    assertThat(b.failed()).isEqualTo(a.failed());
    assertThat(b.exitCode()).isEqualTo(a.exitCode());
  }
}
