package vn.geo.server.geometry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.geometry.api.GeometryEngine;
import vn.geo.server.geometry.api.SceneDto;
import vn.geo.server.geometry.internal.DefaultGeometryEngine;
import vn.geo.server.geometry.internal.GeometrySubject;
import vn.geo.server.geometry.internal.plane.PlaneEngine;
import vn.geo.server.geometry.internal.plane.PlaneScene;
import vn.geo.server.geometry.internal.solid.SolidAdapter;
import vn.geo.server.subject.api.ProblemKind;
import vn.geo.server.subject.api.SubjectEngine.ParsedProblem;
import vn.geo.server.subject.api.SubjectEngine.ProblemInput;
import vn.geo.server.subject.api.SubjectEngine.Rendering;
import vn.geo.server.subject.api.SubjectEngine.Solution;

/** US-S2E2-02 — dựng hình phẳng từ ràng buộc, Problem.kind, SceneSpec 2D (hàm thuần). */
class PlaneEngineTest {

  static final JsonMapper M = JsonMapper.builder().build();
  static final DefaultGeometryEngine SOLID = new DefaultGeometryEngine(M);
  final PlaneEngine plane = new PlaneEngine();
  final GeometrySubject geo = new GeometrySubject(plane, new SolidAdapter(SOLID));

  static final String PL01 =
      """
      {"shape":"triangle","given":[{"type":"length","of":["A","B"],"value":5},
       {"type":"length","of":["A","C"],"value":12},{"type":"length","of":["B","C"],"value":13}],
       "asked":{"name":"góc A","kind":"angle","of":["B","A","C"]}}\
      """;
  static final String PL02 =
      """
      {"shape":"isosceles_triangle","at":"A","given":[{"type":"angle","of":["B","A","C"],"value":40}],
       "asked":{"name":"góc B","kind":"angle","of":["A","B","C"]}}\
      """;

  static String tri(double ab, double ac, double bc) {
    return "{\"shape\":\"triangle\",\"given\":[{\"type\":\"length\",\"of\":[\"A\",\"B\"],\"value\":%s},{\"type\":\"length\",\"of\":[\"A\",\"C\"],\"value\":%s},{\"type\":\"length\",\"of\":[\"B\",\"C\"],\"value\":%s}],\"asked\":{\"kind\":\"angle\",\"of\":[\"B\",\"A\",\"C\"]}}"
        .formatted(ab, ac, bc);
  }

  ParsedProblem parse(String statement, String json) {
    return geo.parse(new ProblemInput(statement, json == null ? null : M.readTree(json)));
  }

  static Map<String, double[]> pts(PlaneScene s) {
    Map<String, double[]> m = new java.util.HashMap<>();
    s.points().forEach(p -> m.put(p.id(), new double[] {p.x(), p.y()}));
    return m;
  }

  static double d(Map<String, double[]> m, String a, String b) {
    return Math.hypot(m.get(a)[0] - m.get(b)[0], m.get(a)[1] - m.get(b)[1]);
  }

  static double ang(Map<String, double[]> m, String a, String o, String b) {
    double ux = m.get(a)[0] - m.get(o)[0];
    double uy = m.get(a)[1] - m.get(o)[1];
    double wx = m.get(b)[0] - m.get(o)[0];
    double wy = m.get(b)[1] - m.get(o)[1];
    return Math.toDegrees(
        Math.acos((ux * wx + uy * wy) / (Math.hypot(ux, uy) * Math.hypot(wx, wy))));
  }

  static void rel(double got, double want) {
    assertThat(Math.abs(got - want) / want).isLessThanOrEqualTo(1e-6);
  }

  Map<String, double[]> built(ParsedProblem p) {
    Rendering r = geo.render(p);
    assertThat(r.built()).as(r.reason()).isTrue();
    assertThat(r.label()).isEqualTo(GeometryEngine.LABEL_VERIFIED);
    return pts((PlaneScene) r.scene());
  }

  @Test
  @DisplayName("TC-S2E2-008 Dựng tam giác 5, 12, 13 thỏa ràng buộc")
  void tc008_pl01() {
    ParsedProblem p = parse("Tam giác ABC có AB = 5, AC = 12, BC = 13; tính góc A.", PL01);
    assertThat(p.kind()).isEqualTo(ProblemKind.PLANE);
    var m = built(p);
    rel(d(m, "A", "B"), 5);
    rel(d(m, "A", "C"), 12);
    rel(d(m, "B", "C"), 13);
    assertThat(ang(m, "B", "A", "C")).isCloseTo(90, within(1e-6));
    assertThat(geo.solve(p).answer()).isEqualTo("90°");
  }

  @Test
  @DisplayName("TC-S2E2-009 Dựng tam giác cân tại A góc A 40°")
  void tc009_pl02() {
    var m = built(parse("Tam giác ABC cân tại A, góc A = 40°; tính góc B.", PL02));
    rel(d(m, "A", "B"), d(m, "A", "C"));
    assertThat(ang(m, "B", "A", "C")).isCloseTo(40, within(1e-6));
    assertThat(ang(m, "A", "B", "C")).isCloseTo(70, within(1e-6));
  }

  @Test
  @DisplayName("TC-S2E2-010 Dựng hình chữ nhật 6 × 8")
  void tc010_pl03() {
    String json =
        """
        {"shape":"rectangle","given":[{"type":"length","of":["A","B"],"value":6},
         {"type":"length","of":["A","D"],"value":8}],"asked":{"kind":"length","of":["A","C"]}}\
        """;
    ParsedProblem p = parse("Hình chữ nhật ABCD có AB = 6, AD = 8; tính AC.", json);
    var m = built(p);
    rel(d(m, "A", "B"), 6);
    rel(d(m, "A", "D"), 8);
    for (String[] v :
        new String[][] {{"D", "A", "B"}, {"A", "B", "C"}, {"B", "C", "D"}, {"C", "D", "A"}}) {
      assertThat(ang(m, v[0], v[1], v[2])).isCloseTo(90, within(1e-6));
    }
    rel(d(m, "A", "C"), 10);
    assertThat(geo.solve(p).value()).isCloseTo(10, within(1e-9));
  }

  @Test
  @DisplayName("TC-S2E2-011 Dựng tam giác đều cạnh 4")
  void tc011_pl04() {
    String json =
        """
        {"shape":"equilateral_triangle","given":[{"type":"length","of":["A","B"],"value":4}],
         "asked":{"name":"S","kind":"area","of":[]}}\
        """;
    ParsedProblem p = parse("Tam giác đều cạnh 4; tính diện tích.", json);
    var m = built(p);
    for (String[] e : new String[][] {{"A", "B"}, {"B", "C"}, {"C", "A"}}) {
      rel(d(m, e[0], e[1]), 4);
    }
    assertThat(ang(m, "B", "A", "C")).isCloseTo(60, within(1e-6));
    assertThat(ang(m, "A", "B", "C")).isCloseTo(60, within(1e-6));
    assertThat(ang(m, "A", "C", "B")).isCloseTo(60, within(1e-6));
    Solution s = geo.solve(p);
    assertThat(s.value()).isCloseTo(4 * Math.sqrt(3), within(1e-9));
    assertThat(s.answer()).isEqualTo("6,9282");
  }

  @Test
  @DisplayName("TC-S2E2-012 Đề không gian vẫn mang loại space")
  void tc012_space() {
    String json =
        """
        {"template":"pyramid_square_sa","params":{"a":"a","sa":"a√2"},"unit":"a",
         "chips":[],"asked":[{"name":"","kind":"angle_line_plane","points":["S","C","A","B","C"]}],
         "hidden":[]}\
        """;
    ParsedProblem p =
        parse(
            "Hình chóp S.ABCD, đáy ABCD là hình vuông cạnh a, SA ⊥ (ABCD), SA = a√2; tính góc"
                + " giữa SC và mặt đáy.",
            json);
    assertThat(p.kind()).isEqualTo(ProblemKind.SPACE);
    Rendering r = geo.render(p);
    assertThat(r.scene()).isInstanceOf(SceneDto.class);
    // SceneSpec 3D như MVP: đúng cảnh engine MVP dựng trực tiếp
    GeometryEngine.ProblemSpec spec = SOLID.specFrom(M.readTree(json));
    SceneDto direct =
        SOLID.toScene(
            SOLID.build(spec.template(), spec.paramsAt(1)),
            new GeometryEngine.SceneMeta(
                null, "engine", spec.unit(), spec.chips(), spec.hidden(), "verified"));
    assertThat(M.writeValueAsString(r.scene())).isEqualTo(M.writeValueAsString(direct));
    assertThat(geo.solve(p).value()).isCloseTo(45, within(1e-6));
    // không nêu mô tả: nhận theo chữ trong đề
    assertThat(parse("Cho hình chóp S.ABC có SA ⊥ (ABC).", null).kind())
        .isEqualTo(ProblemKind.SPACE);
  }

  @Test
  @DisplayName("TC-S2E2-014 Dựng hai lần cho kết quả giống hệt")
  void tc014_deterministic() {
    String a = M.writeValueAsString(geo.render(parse("PL-02", PL02)).scene());
    String b = M.writeValueAsString(geo.render(parse("PL-02", PL02)).scene());
    assertThat(a).isEqualTo(b).contains("scene2d-v1");
  }

  void rejected(String json, String reason) {
    Rendering r = geo.render(parse("đề thử", json));
    assertThat(r.built()).isFalse();
    assertThat(r.scene()).isNull();
    assertThat(r.label()).isEqualTo(GeometryEngine.LABEL_NEEDS_REVIEW);
    assertThat(r.reason()).isEqualTo(reason);
    Solution s = geo.solve(parse("đề thử", json));
    assertThat(s.solved()).isFalse();
    assertThat(s.answer()).isNull();
    assertThat(s.value()).isNull();
  }

  @Test
  @DisplayName("TC-S2E2-015 Số liệu mâu thuẫn thì không dựng")
  void tc015_contradiction() {
    rejected(tri(5, 12, 20), "ba cạnh không lập thành tam giác");
  }

  @Test
  @DisplayName("TC-S2E2-016 Tam giác suy biến (tổng hai cạnh bằng cạnh còn lại) bị từ chối")
  void tc016_degenerate() {
    rejected(tri(5, 5, 10), "ba cạnh không lập thành tam giác");
  }

  @Test
  @DisplayName("TC-S2E2-017 Tam giác gần suy biến vẫn dựng được")
  void tc017_nearDegenerate() {
    var m = built(parse("AB = 5, AC = 5, BC = 9,99", tri(5, 5, 9.99)));
    rel(d(m, "A", "B"), 5);
    rel(d(m, "A", "C"), 5);
    rel(d(m, "B", "C"), 9.99);
    // AC ghi "góc A xấp xỉ 179,8°"; giá trị đúng của tam giác 5, 5, 9,99 là 174,87° (qc_findings
    // Dev-1)
    double a = Math.toDegrees(Math.acos((25 + 25 - 9.99 * 9.99) / 50));
    assertThat(ang(m, "B", "A", "C")).isCloseTo(a, within(1e-6)).isGreaterThan(174);
  }

  @Test
  @DisplayName("TC-S2E2-018 Góc đề cho sát 0° hoặc 180°")
  void tc018_extremeAngles() {
    String iso =
        "{\"shape\":\"isosceles_triangle\",\"at\":\"A\",\"given\":[{\"type\":\"angle\",\"of\":[\"B\",\"A\",\"C\"],\"value\":%s}],\"asked\":{\"kind\":\"angle\",\"of\":[\"A\",\"B\",\"C\"]}}";
    rejected(iso.formatted(0), "góc không hợp lệ");
    var m = built(parse("góc A = 179°", iso.formatted(179)));
    assertThat(ang(m, "B", "A", "C")).isCloseTo(179, within(1e-6));
    rel(d(m, "A", "B"), d(m, "A", "C"));
  }

  @Test
  @DisplayName("TC-S2E2-019 Đề thiếu ràng buộc thì nhãn Cần xem lại")
  void tc019_undetermined() {
    rejected(
        """
        {"shape":"triangle","given":[{"type":"length","of":["A","B"],"value":5}],
         "asked":{"kind":"angle","of":["B","A","C"]}}\
        """,
        "hình chưa xác định, thiếu dữ kiện");
  }

  @Test
  @DisplayName("TC-S2E2-020 Dạng ngoài danh sách hỗ trợ đi nhánh Cần xem lại")
  void tc020_unsupported() {
    long before = plane.unsupportedCount("ellipse");
    rejected(
        """
        {"shape":"ellipse","given":[{"type":"length","of":["F1","F2"],"value":6}],
         "asked":{"kind":"length","of":["A","B"]}}\
        """,
        "dạng chưa hỗ trợ");
    // một dòng thống kê mỗi lần gặp dạng chưa hỗ trợ (render + solve ở trên = 2 lần)
    assertThat(plane.unsupportedCount("ellipse")).isEqualTo(before + 2);
  }

  @Test
  @DisplayName("TC-S2E2-021 Đề có cả điểm ngoài mặt phẳng chạy nhánh không gian")
  void tc021_outOfPlane() {
    String json =
        """
        {"shape":"triangle","outOfPlane":["S"],"given":[{"type":"length","of":["A","B"],"value":5}]}\
        """;
    ParsedProblem p = parse("Cho S nằm ngoài mặt phẳng (ABC).", json);
    assertThat(p.kind()).isEqualTo(ProblemKind.SPACE);
    long before = plane.unsupportedCount("triangle");
    Rendering r = geo.render(p);
    assertThat(r.scene() instanceof PlaneScene).isFalse();
    assertThat(r.reason()).isEqualTo("chưa có mô tả dựng không gian");
    assertThat(plane.unsupportedCount("triangle")).isEqualTo(before);
  }

  @Test
  @DisplayName("Bộ dựng tứ giác và đường tròn thỏa ràng buộc")
  void quadsAndCircles() {
    JsonNode set =
        M.readTree(
            PlaneEngineTest.class.getResourceAsStream("/content/plane/plane-benchmark.json"));
    for (JsonNode e : set.path("problems")) {
      Rendering r =
          geo.render(geo.parse(new ProblemInput(e.path("statement").asString(), e.path("spec"))));
      assertThat(r.built()).as(e.path("code").asString() + ": " + r.reason()).isTrue();
      assertThat(
              geo.grade(
                      e.path("answer").asString(),
                      String.valueOf(
                          geo.solve(geo.parse(new ProblemInput("", e.path("spec")))).value()),
                      null)
                  .correct())
          .as(e.path("code").asString())
          .isTrue();
    }
  }
}
