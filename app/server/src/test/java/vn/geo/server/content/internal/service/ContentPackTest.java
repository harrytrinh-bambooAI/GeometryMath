package vn.geo.server.content.internal.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.geometry.api.Figure;
import vn.geo.server.geometry.api.GeometryEngine.ProblemSpec;
import vn.geo.server.geometry.api.GeometryEngine.Verification;
import vn.geo.server.geometry.internal.DefaultGeometryEngine;

/**
 * Gói nội dung Kho đề MVP (T-261126-02, PO-60 / MVP.md §5 P0): từng đề dựng được bằng engine, hình
 * thỏa mọi ràng buộc ở cả ba bộ tham số, đáp số của lời giải khớp giá trị tính; tổng số đề máy kiểm
 * chứng ≥ 40. Hàm thuần, không cần CSDL (bản có CSDL: ContentPackMvpIT).
 */
class ContentPackTest {

  static final JsonMapper MAPPER = JsonMapper.builder().build();
  static final DefaultGeometryEngine ENGINE = new DefaultGeometryEngine(MAPPER);
  static final ContentPack PACK = ContentPack.read(MAPPER);

  /** Loại khối của đề (cột problem.solid_kind) theo bộ dựng. */
  static final Map<String, String> SOLID_OF_TEMPLATE =
      Map.ofEntries(
          Map.entry("pyramid", "pyramid"),
          Map.entry("pyramid_square_sa", "pyramid"),
          Map.entry("prism", "prism"),
          Map.entry("prism_tri_right", "prism"),
          Map.entry("prism_tri_sides", "prism"),
          Map.entry("box", "box"),
          Map.entry("cube", "cube"),
          Map.entry("cone", "cone"),
          Map.entry("cone_rl", "cone"),
          Map.entry("cylinder", "cylinder"),
          Map.entry("sphere", "sphere"),
          Map.entry("sphere_circum_cube", "sphere"));

  static Stream<ContentPack.Problem> problems() {
    return PACK.problems().stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("problems")
  @DisplayName(
      "Đề của gói: dựng bằng engine, hình thỏa ràng buộc, đáp số khớp, đủ gợi ý và lời giải")
  void eachProblemVerifiedByEngine(ContentPack.Problem p) {
    ProblemSpec spec = ENGINE.catalogSpec(p.code()).orElseThrow();
    assertThat(SOLID_OF_TEMPLATE.get(spec.template()))
        .as("loại khối %s", p.code())
        .isEqualTo(p.solid());
    double[] sets = spec.parametric() ? new double[] {1, 2, 3} : new double[] {1};
    for (double a : sets) {
      Figure f = ENGINE.build(spec.template(), spec.paramsAt(a));
      assertThat(ENGINE.check(f.points(), f.constraints()).satisfied())
          .as("%s: hình thỏa ràng buộc tại a = %s", p.code(), a)
          .isTrue();
      spec.chips()
          .forEach(c -> assertThat(f.points()).containsKeys(c.points().toArray(String[]::new)));
    }
    Verification v = ENGINE.verify(spec, p.answer());
    assertThat(v.report()).as("%s", p.code()).containsEntry("result", "verified");
    assertThat(v.sceneOk()).isTrue();
    assertThat(v.answerOk()).as("%s: đáp số %s khớp giá trị tính", p.code(), p.answer()).isTrue();
    assertThat(AnswerExpr.parse(p.answer())).hasSize(spec.asked().size());
    assertThat(p.level()).isIn("thcs", "thpt");
    assertThat(p.topic()).isIn("Góc", "Khoảng cách", "Thể tích", "Diện tích", "Mặt cầu");
    assertThat(p.hints()).hasSize(2).allMatch(h -> !h.isBlank());
    assertThat(p.steps()).isNotEmpty();
    p.steps()
        .forEach(
            s -> assertThat(List.of(s.title(), s.body(), s.why())).allMatch(x -> !x.isBlank()));
    assertThat(p.statement()).isNotBlank();
  }

  @Test
  @DisplayName("Gói có ≥ 40 đề máy kiểm chứng, mã duy nhất, không trùng bộ đề chuẩn HKG, đủ dạng")
  void atLeastFortyVerified() {
    Set<String> codes = new HashSet<>();
    long verified = 0;
    Set<String> templates = new HashSet<>();
    Set<String> kinds = new HashSet<>();
    for (ContentPack.Problem p : PACK.problems()) {
      assertThat(codes.add(p.code())).as("mã trùng %s", p.code()).isTrue();
      assertThat(p.code()).startsWith("HKN-");
      ProblemSpec spec = ENGINE.catalogSpec(p.code()).orElseThrow();
      templates.add(spec.template());
      spec.asked().forEach(q -> kinds.add(q.kind()));
      if ("verified".equals(ENGINE.verify(spec, p.answer()).status())) {
        verified++;
      }
    }
    assertThat(verified).isGreaterThanOrEqualTo(40);
    assertThat(PACK.bank()).isEqualTo("main");
    assertThat(templates).hasSizeGreaterThanOrEqualTo(10);
    assertThat(kinds)
        .contains("angle_line_plane", "dist_point_plane", "dist_lines", "volume_prism", "length");
    assertThat(PACK.problems().stream().map(ContentPack.Problem::level).distinct())
        .containsExactlyInAnyOrder("thcs", "thpt");
  }

  @Test
  @DisplayName("Cân chuyên đề (27/11): mỗi dạng bài 16–20 đề, có cả THCS và THPT")
  void topicsBalanced() {
    for (String topic : List.of("Góc", "Khoảng cách", "Thể tích", "Diện tích", "Mặt cầu")) {
      List<ContentPack.Problem> ps =
          PACK.problems().stream().filter(p -> topic.equals(p.topic())).toList();
      assertThat(ps).as("số đề dạng %s", topic).hasSizeBetween(16, 20);
      assertThat(ps.stream().map(ContentPack.Problem::level).distinct())
          .as("cấp của dạng %s", topic)
          .containsExactlyInAnyOrder("thcs", "thpt");
    }
  }
}
