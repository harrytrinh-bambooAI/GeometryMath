package vn.geo.server.geometry.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.geometry.api.AnswerMatch;
import vn.geo.server.geometry.api.Constraint;
import vn.geo.server.geometry.api.Figure;
import vn.geo.server.geometry.api.GeometryEngine.AskedQuantity;
import vn.geo.server.geometry.api.GeometryEngine.ProblemSpec;
import vn.geo.server.geometry.api.GeometryEngine.SceneMeta;
import vn.geo.server.geometry.api.GeometryEngine.Verification;
import vn.geo.server.geometry.api.SceneBuildException;
import vn.geo.server.geometry.api.SceneDto;

/** Engine dựng hình và kiểm chứng của E2 (US-GEO-201, 203): hàm thuần, không cần CSDL. */
class GeometryEngineE2Test {

  private final DefaultGeometryEngine engine =
      new DefaultGeometryEngine(JsonMapper.builder().build());

  private ProblemSpec spec(String code) {
    return engine.catalogSpec(code).orElseThrow();
  }

  private Figure figure(String code, double a) {
    ProblemSpec s = spec(code);
    return engine.build(s.template(), s.paramsAt(a));
  }

  private static double len(Figure f, String p, String q) {
    return V.dist(f.point(p), f.point(q));
  }

  private static double cos(Figure f, String p, String q, String r, String s) {
    double[] u = V.sub(f.point(q), f.point(p));
    double[] v = V.sub(f.point(s), f.point(r));
    return Math.abs(V.dot(u, v)) / (V.norm(u) * V.norm(v));
  }

  // ------------------------------------------------------------------ US-GEO-201

  @Test
  @DisplayName("TC-GEO-0200 Dựng hình HKG-01 thỏa mọi ràng buộc (US-GEO-201-SC1)")
  void tc0200_hkg01SatisfiesConstraints() {
    Figure f = figure("HKG-01", 1);
    assertThat(f.points().keySet()).containsExactlyInAnyOrder("S", "A", "B", "C", "D");
    for (String[] e : new String[][] {{"A", "B"}, {"B", "C"}, {"C", "D"}, {"D", "A"}, {"S", "A"}}) {
      assertThat(Math.abs(len(f, e[0], e[1]) - 1)).isLessThanOrEqualTo(1e-6);
    }
    assertThat(cos(f, "S", "A", "A", "B")).isLessThanOrEqualTo(1e-6);
    assertThat(cos(f, "S", "A", "A", "D")).isLessThanOrEqualTo(1e-6);
    assertThat(engine.check(f.points(), f.constraints()).satisfied()).isTrue();
    SceneDto scene =
        engine.toScene(f, new SceneMeta(null, "engine", "a", List.of(), List.of(), "verified"));
    assertThat(scene.coordSource()).isEqualTo("engine");
  }

  @Test
  @DisplayName("TC-GEO-0201 Dựng hình có số đo cụ thể (US-GEO-201-SC2)")
  void tc0201_concreteMeasures() {
    Figure f = figure("HKG-03", 1);
    for (String[] e : new String[][] {{"A", "B"}, {"B", "C"}, {"C", "A"}}) {
      assertThat(len(f, e[0], e[1])).isCloseTo(4, org.assertj.core.data.Offset.offset(1e-9));
    }
    for (String p : List.of("A", "B", "C")) {
      assertThat(len(f, p, p + "′")).isCloseTo(6, org.assertj.core.data.Offset.offset(1e-9));
      String q = p.equals("A") ? "B" : "A";
      assertThat(cos(f, p, p + "′", p, q)).isLessThanOrEqualTo(1e-9);
    }
    assertThat(engine.check(f.points(), f.constraints()).satisfied()).isTrue();
    // công cụ Đo dài: độ dài cạnh đáy + đơn vị của cảnh => "4 cm"
    assertThat(spec("HKG-03").unit()).isEqualTo("cm");
    assertThat(fmt(len(f, "A", "B")) + " " + spec("HKG-03").unit()).isEqualTo("4 cm");
  }

  private static String fmt(double x) {
    return new java.text.DecimalFormat("0.##").format(x).replace('.', ',');
  }

  /** Hai đoạn vuông góc mà |cos| bằng đúng giá trị cho trước (F-GEO-073). */
  private static Map<String, double[]> perpWithError(double c) {
    Map<String, double[]> pts = new LinkedHashMap<>();
    pts.put("P", V.of(0, 0, 0));
    pts.put("Q", V.of(1, 0, 0));
    pts.put("R", V.of(0, 0, 0));
    pts.put("S", V.of(c, Math.sqrt(1 - c * c), 0));
    return pts;
  }

  @Test
  @DisplayName("TC-GEO-0203 Sai số sát dưới ngưỡng vẫn thỏa (US-GEO-201-SC4)")
  void tc0203_justBelowThreshold() {
    var r = engine.check(perpWithError(9.9e-7), List.of(Constraint.perp("P", "Q", "R", "S")));
    assertThat(r.satisfied()).isTrue();
    assertThat(r.results().get(0).error())
        .isCloseTo(9.9e-7, org.assertj.core.data.Offset.offset(1e-12));
  }

  @Test
  @DisplayName("TC-GEO-0204 Cùng đầu vào cho cùng hình (US-GEO-201-SC5)")
  void tc0204_deterministic() {
    Figure f1 = figure("HKG-02", 2);
    Figure f2 = figure("HKG-02", 2);
    assertThat(f1.points().keySet()).containsExactlyElementsOf(f2.points().keySet());
    f1.points().forEach((k, p) -> assertThat(f2.point(k)).containsExactly(p));
    assertThat(f1.edges()).isEqualTo(f2.edges());
  }

  @Test
  @DisplayName("TC-GEO-0205 Sai số vượt ngưỡng (US-GEO-201-FC1)")
  void tc0205_aboveThreshold() {
    var r = engine.check(perpWithError(1.01e-6), List.of(Constraint.perp("P", "Q", "R", "S")));
    assertThat(r.satisfied()).isFalse();
    // hình không thỏa ràng buộc => nhãn máy "Cần xem lại" dù đáp số khớp (bảng nhãn, dòng 3)
    assertThat(DefaultGeometryEngine.label(r.satisfied() ? "verified" : "needs_review"))
        .isEqualTo("Cần xem lại");
  }

  @Test
  @DisplayName("Ràng buộc mâu thuẫn và hình suy biến: engine từ chối (US-GEO-201-FC2, FC3)")
  void contradictoryAndDegenerate() {
    assertThatThrownBy(() ->
            engine.build("prism_tri_sides", Map.of("ab", 1.0, "bc", 1.0, "ca", 3.0, "h", 2.0)))
        .isInstanceOfSatisfying(
            SceneBuildException.class,
            e -> assertThat(e.reason()).isEqualTo(SceneBuildException.Reason.CONTRADICTORY));
    assertThatThrownBy(() -> engine.build("prism", Map.of("a", 4.0, "h", 0.0)))
        .isInstanceOfSatisfying(
            SceneBuildException.class,
            e -> assertThat(e.reason()).isEqualTo(SceneBuildException.Reason.DEGENERATE));
  }

  @Test
  @DisplayName("Mọi bộ dựng cho hình thỏa ràng buộc với tham số mặc định")
  void allBuildersSatisfyConstraints() {
    Map<String, Map<String, Double>> p =
        Map.ofEntries(
            Map.entry("pyramid", Map.of("a", 2.0, "h", 3.0)),
            Map.entry("pyramid_square_sa", Map.of("a", 2.0, "sa", 3.0)),
            Map.entry("prism", Map.of("a", 2.0, "h", 3.0)),
            Map.entry("prism_tri_right", Map.of("ab", 3.0, "ac", 4.0, "h", 5.0)),
            Map.entry("prism_tri_sides", Map.of("ab", 3.0, "bc", 5.0, "ca", 4.0, "h", 2.0)),
            Map.entry("box", Map.of("a", 3.0, "b", 2.0, "c", 1.5)),
            Map.entry("cube", Map.of("a", 2.0)),
            Map.entry("cone", Map.of("r", 3.0, "h", 4.0)),
            Map.entry("cone_rl", Map.of("r", 3.0, "l", 5.0)),
            Map.entry("cylinder", Map.of("r", 2.0, "h", 3.0)),
            Map.entry("sphere", Map.of("r", 2.0)),
            Map.entry("sphere_circum_cube", Map.of("a", 1.0)));
    assertThat(p.keySet()).containsExactlyInAnyOrderElementsOf(Builders.ALL.keySet());
    p.forEach((t, params) -> {
      Figure f = engine.build(t, params);
      assertThat(engine.check(f.points(), f.constraints()).satisfied()).as(t).isTrue();
    });
  }

  @Test
  @DisplayName("Tọa độ AI: hợp lệ dùng được, sáu điểm trùng nhau bị từ chối (F-GEO-110, 111)")
  void aiCoordinates() {
    double k = 0.7071;
    Map<String, double[]> ok = new LinkedHashMap<>();
    ok.put("A", V.of(k, 0, 0));
    ok.put("B", V.of(-k, 0, 0));
    ok.put("C", V.of(0, k, 0));
    ok.put("D", V.of(0, -k, 0));
    ok.put("E", V.of(0, 0, k));
    ok.put("F", V.of(0, 0, -k));
    Figure f = engine.fromAi(ok, List.of(List.of("A", "C"), List.of("C", "E")), List.of());
    assertThat(f.points()).hasSize(6);
    Map<String, double[]> bad = new LinkedHashMap<>();
    for (String n : List.of("A", "B", "C", "D", "E", "F")) {
      bad.put(n, V.of(0, 0, 0));
    }
    assertThatThrownBy(() -> engine.fromAi(bad, List.of(List.of("A", "B")), List.of()))
        .isInstanceOf(SceneBuildException.class);
  }

  // ------------------------------------------------------------------ US-GEO-203

  @Test
  @DisplayName("TC-GEO-0213 Đề khớp mang \"Đã kiểm chứng\" (US-GEO-203-SC1)")
  void tc0213_matchVerified() {
    Verification v = engine.verify(spec("HKG-01"), "45°");
    assertThat(engine.measure(figure("HKG-01", 1), spec("HKG-01").asked().get(0)))
        .isCloseTo(45, org.assertj.core.data.Offset.offset(1e-9));
    assertThat(v.label()).isEqualTo("Đã kiểm chứng");
    assertThat(v.status()).isEqualTo("verified");
  }

  @Test
  @DisplayName("TC-GEO-0214 Đề không khớp mang \"Cần xem lại\" (US-GEO-203-SC2)")
  void tc0214_mismatchNeedsReview() {
    assertThat(engine.verify(spec("HKG-01"), "60°").label()).isEqualTo("Cần xem lại");
  }

  @Test
  @DisplayName("TC-GEO-0215 Đáp số theo tham số kiểm trên ba bộ giá trị (US-GEO-203-SC3)")
  @SuppressWarnings("unchecked")
  void tc0215_threeParameterSets() {
    Verification v = engine.verify(spec("HKG-02"), "a");
    assertThat(v.label()).isEqualTo("Đã kiểm chứng");
    var sets = (List<Map<String, Object>>) v.report().get("param_sets");
    assertThat(sets).hasSize(3);
    for (int i = 0; i < 3; i++) {
      assertThat(sets.get(i).get("a")).isEqualTo(i + 1.0);
      var q = ((List<Map<String, Object>>) sets.get(i).get("quantities")).get(0);
      assertThat((double) q.get("computed"))
          .isCloseTo(i + 1, org.assertj.core.data.Offset.offset(1e-9));
      assertThat(q.get("match")).isEqualTo(true);
    }
  }

  @Test
  @DisplayName("TC-GEO-0216 Sai lệch sát dưới ngưỡng vẫn khớp (US-GEO-203-SC4)")
  void tc0216_relativeJustBelow() {
    assertThat(AnswerMatch.tolerance(true, 45, 4))
        .isCloseTo(0.0045, org.assertj.core.data.Offset.offset(1e-11));
    assertThat(AnswerMatch.close(45.0044, 45, true, 4)).isTrue();
    // cùng phép so trên tọa độ engine: góc HKG-01 tính được là 45
    assertThat(engine.verify(spec("HKG-01"), "45,0044°").answerOk()).isTrue();
  }

  @Test
  @DisplayName("TC-GEO-0217 Nhãn do engine tính có thể khác nhãn trong tệp nguồn (US-GEO-203-SC5)")
  @SuppressWarnings("unchecked")
  void tc0217_engineOverridesSourceLabel() {
    Verification v = engine.verify(spec("HKG-06"), "R = a√3/2; V = (√3/2)·π·a³");
    var q =
        (List<Map<String, Object>>)
            ((List<Map<String, Object>>) v.report().get("param_sets")).get(0).get("quantities");
    assertThat((double) q.get(0).get("computed"))
        .isCloseTo(0.8660, org.assertj.core.data.Offset.offset(1e-4));
    assertThat((double) q.get(1).get("computed"))
        .isCloseTo(2.7207, org.assertj.core.data.Offset.offset(1e-4));
    // tệp nguồn ghi "Cần xem lại" nhưng nhãn máy chỉ do engine tính (D-GEO-004)
    assertThat(v.label()).isEqualTo("Đã kiểm chứng");
  }

  @Test
  @DisplayName("TC-GEO-0219 Sai lệch vượt ngưỡng (US-GEO-203-FC1)")
  void tc0219_relativeJustAbove() {
    assertThat(AnswerMatch.close(45.0046, 45, true, 4)).isFalse();
    Verification v = engine.verify(spec("HKG-01"), "45,0046°");
    assertThat(v.answerOk()).isFalse();
    assertThat(v.label()).isEqualTo("Cần xem lại");
  }

  @Test
  @DisplayName("TC-GEO-0220 Đáp số hai giá trị, một giá trị sai (US-GEO-203-FC2)")
  @SuppressWarnings("unchecked")
  void tc0220_twoValuesOneWrong() {
    Verification v = engine.verify(spec("HKG-04"), "Sxq = 15π cm²; V = 16π cm³");
    assertThat(v.label()).isEqualTo("Cần xem lại");
    var q =
        (List<Map<String, Object>>)
            ((List<Map<String, Object>>) v.report().get("param_sets")).get(0).get("quantities");
    assertThat(q.get(0).get("name")).isEqualTo("Sxq");
    assertThat((double) q.get(0).get("computed"))
        .isCloseTo(15 * Math.PI, org.assertj.core.data.Offset.offset(1e-9));
    assertThat(q.get(0).get("match")).isEqualTo(true);
    assertThat(q.get(1).get("name")).isEqualTo("V");
    assertThat((double) q.get(1).get("computed"))
        .isCloseTo(12 * Math.PI, org.assertj.core.data.Offset.offset(1e-9));
    assertThat(q.get(1).get("match")).isEqualTo(false);
  }

  @Test
  @DisplayName("TC-GEO-0234 Đề không tham số khớp theo dung sai tuyệt đối (US-GEO-203-SC7)")
  @SuppressWarnings("unchecked")
  void tc0234_nonParametricAbsolute() {
    Verification v = engine.verify(spec("HKG-03"), "24√3 cm³");
    var sets = (List<Map<String, Object>>) v.report().get("param_sets");
    assertThat(sets).hasSize(1); // tính một lần
    var q = ((List<Map<String, Object>>) sets.get(0).get("quantities")).get(0);
    assertThat(Math.abs(24 * Math.sqrt(3) - (double) q.get("computed")))
        .isLessThanOrEqualTo(1e-6 + 1e-12);
    assertThat((double) q.get("tolerance")).isEqualTo(1e-6 + 1e-12);
    assertThat(((Map<String, Object>) v.report().get("tolerance")).get("kind"))
        .isEqualTo("absolute");
    assertThat(v.label()).isEqualTo("Đã kiểm chứng");
  }

  @Test
  @DisplayName("TC-GEO-0235 Đề không tham số: lệch vượt dung sai theo chữ số ghi (US-GEO-203-FC6)")
  @SuppressWarnings("unchecked")
  void tc0235_decimalsTooFar() {
    Verification v = engine.verify(spec("HKG-03"), "41,56 cm³");
    var q =
        (List<Map<String, Object>>)
            ((List<Map<String, Object>>) v.report().get("param_sets")).get(0).get("quantities");
    assertThat((double) q.get(0).get("tolerance")).isEqualTo(0.005 + 1e-12);
    assertThat(q.get(0).get("match")).isEqualTo(false);
    assertThat(v.label()).isEqualTo("Cần xem lại");
  }

  @Test
  @DisplayName("TC-GEO-0236 Đáp số thập phân khớp theo nửa đơn vị chữ số cuối (US-GEO-203-SC8)")
  void tc0236_decimalsHalfUnit() {
    Verification v = engine.verify(spec("HKG-03"), "41,57 cm³");
    assertThat(v.answerOk()).isTrue();
    assertThat(v.label()).isEqualTo("Đã kiểm chứng");
  }

  @Test
  @DisplayName("Đại lượng: khoảng cách điểm–mặt của F-GEO-060 = 60/√769")
  void distPointPlane() {
    Figure f = engine.build("prism_tri_right", Map.of("ab", 3.0, "ac", 4.0, "h", 5.0));
    double d =
        engine.measure(f, new AskedQuantity("", "dist_point_plane", List.of("A", "A′", "B", "C")));
    assertThat(d).isCloseTo(60 / Math.sqrt(769), org.assertj.core.data.Offset.offset(1e-9));
  }
}
