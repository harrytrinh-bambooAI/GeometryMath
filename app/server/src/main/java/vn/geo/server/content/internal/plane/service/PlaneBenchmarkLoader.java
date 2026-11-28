package vn.geo.server.content.internal.plane.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.content.internal.plane.repo.PlaneRepository;
import vn.geo.server.subject.api.ProblemKind;
import vn.geo.server.subject.api.SubjectEngine;
import vn.geo.server.subject.api.SubjectEngine.ProblemInput;
import vn.geo.server.subject.api.SubjectRegistry;

/**
 * Lệnh nạp bộ đề chuẩn hình phẳng (US-S2E2-04). Mỗi đề cần mã {@code PLS-nn}, đề, mô tả dựng (dạng
 * + ràng buộc + đại lượng hỏi), đáp số tham chiếu, kiểu đáp số, người soát (BR-S2E2-12). Đề lỗi
 * (thiếu trường, trùng mã, đáp số tham chiếu lệch kết quả engine — BR-S2E2-14) vào báo cáo lỗi kèm
 * mã đề và không vào bộ; đề khác vẫn nạp. Bộ dưới 30 đề hoặc một dạng dưới 5 đề: cảnh báo
 * (BR-S2E2-13). Có lỗi hoặc cảnh báo: mã thoát khác 0.
 */
@Service
public class PlaneBenchmarkLoader {

  /** Tệp bộ đề chuẩn mặc định (classpath). */
  public static final String DEFAULT = "/content/plane/plane-benchmark.json";

  public static final int MIN_TOTAL = 30;
  public static final int MIN_PER_FORM = 5;
  public static final List<String> FORMS = List.of("tam giác", "tứ giác", "đường tròn");

  private final PlaneRepository repo;
  private final SubjectRegistry subjects;
  private final JsonMapper mapper;

  public PlaneBenchmarkLoader(PlaneRepository repo, SubjectRegistry subjects, JsonMapper mapper) {
    this.repo = repo;
    this.subjects = subjects;
    this.mapper = mapper;
  }

  /** Một lỗi dữ liệu kèm mã đề. */
  public record Issue(String code, String message) {}

  /** Báo cáo nạp: mã đề đã nạp, lỗi, cảnh báo, số đề theo dạng, mã thoát. */
  public record LoadReport(
      List<String> loaded,
      List<Issue> errors,
      List<String> warnings,
      Map<String, Integer> byForm,
      int exitCode) {}

  public LoadReport loadDefault() {
    try (InputStream in = PlaneBenchmarkLoader.class.getResourceAsStream(DEFAULT)) {
      if (in == null) {
        throw new IllegalStateException("thiếu " + DEFAULT);
      }
      return load(mapper.readTree(in));
    } catch (IOException e) {
      throw new IllegalStateException("không đọc được " + DEFAULT, e);
    }
  }

  /** Nạp bộ đề từ JSON {problems: [...]}: kiểm từng đề, ghi các đề hợp lệ (thay cả bộ). */
  public LoadReport load(JsonNode root) {
    List<PlaneRepository.BenchRow> ok = new ArrayList<>();
    List<Issue> errors = new ArrayList<>();
    Set<String> seen = new HashSet<>();
    for (JsonNode p : root.path("problems")) {
      String code = p.path("code").asString("").strip();
      String err = check(p, code, seen);
      if (err != null) {
        errors.add(new Issue(code.isEmpty() ? "(không mã)" : code, err));
        continue;
      }
      seen.add(code);
      JsonNode spec = p.path("spec");
      String shape = spec.path("shape").asString();
      ok.add(
          new PlaneRepository.BenchRow(
              code,
              p.path("alias").asString(null),
              p.path("statement").asString(),
              shape,
              form(shape),
              mapper.writeValueAsString(spec),
              p.path("answer").asString(),
              p.path("answerType").asString(),
              p.path("reviewer").asString()));
    }
    repo.replaceBenchmark(ok);
    Map<String, Integer> byForm = new LinkedHashMap<>();
    FORMS.forEach(f -> byForm.put(f, 0));
    ok.forEach(r -> byForm.merge(r.form(), 1, Integer::sum));
    List<String> warnings = new ArrayList<>();
    if (ok.size() < MIN_TOTAL) {
      warnings.add("dưới " + MIN_TOTAL + " đề (" + ok.size() + ")");
    }
    for (String f : FORMS) {
      if (byForm.get(f) < MIN_PER_FORM) {
        warnings.add("dạng " + f + " dưới " + MIN_PER_FORM + " đề (" + byForm.get(f) + ")");
      }
    }
    int exit = errors.isEmpty() && warnings.isEmpty() ? 0 : 1;
    return new LoadReport(
        ok.stream().map(PlaneRepository.BenchRow::code).toList(),
        List.copyOf(errors),
        List.copyOf(warnings),
        byForm,
        exit);
  }

  private String check(JsonNode p, String code, Set<String> seen) {
    if (!code.matches("PLS-[0-9]{2,3}")) {
      return "mã đề phải có dạng PLS-nn";
    }
    if (seen.contains(code)) {
      return "trùng mã " + code;
    }
    for (String f : List.of("statement", "answerType", "reviewer")) {
      if (p.path(f).asString("").isBlank()) {
        return "thiếu trường " + f;
      }
    }
    if (p.path("answer").asString("").isBlank()) {
      return "thiếu đáp số tham chiếu";
    }
    if (!p.path("spec").isObject() || p.path("spec").path("shape").asString("").isBlank()) {
      return "thiếu mô tả dựng (spec.shape)";
    }
    if (p.path("spec").path("given").isEmpty()) {
      return "thiếu danh sách ràng buộc (spec.given)";
    }
    Evaluated e =
        evaluate(p.path("statement").asString(), p.path("spec"), p.path("answer").asString());
    if (!e.solution().solved()) {
      return "engine không giải được: " + e.solution().reason();
    }
    if (!e.answerOk()) {
      return "đáp số tham chiếu "
          + p.path("answer").asString()
          + " lệch kết quả engine "
          + e.solution().answer();
    }
    return null;
  }

  /** Kết quả engine trên một đề: hình dựng, lời giải, đáp số engine khớp tham chiếu. */
  public record Evaluated(
      SubjectEngine.Rendering rendering, SubjectEngine.Solution solution, boolean answerOk) {}

  /** Chạy một đề qua khung môn học (môn phục vụ đề phẳng). */
  public Evaluated evaluate(String statement, JsonNode spec, String reference) {
    SubjectEngine engine = subjects.forKind(ProblemKind.PLANE);
    SubjectEngine.ParsedProblem parsed = engine.parse(new ProblemInput(statement, spec));
    SubjectEngine.Rendering r = engine.render(parsed);
    SubjectEngine.Solution s = engine.solve(parsed);
    boolean ok =
        s.solved()
            && s.value() != null
            && engine.grade(reference, plain(s.value()), null).correct();
    return new Evaluated(r, s, ok);
  }

  /** Số đủ chữ số để so với dung sai 1e-6 ("6,9282032303"). */
  static String plain(double v) {
    return String.format(java.util.Locale.ROOT, "%.10f", v).replace('.', ',');
  }

  static String form(String shape) {
    return switch (shape) {
      case "triangle", "isosceles_triangle", "equilateral_triangle", "right_triangle" -> "tam giác";
      case "rectangle", "square", "parallelogram", "trapezoid" -> "tứ giác";
      case "circle_chord", "circle_tangent" -> "đường tròn";
      default -> "khác";
    };
  }
}
