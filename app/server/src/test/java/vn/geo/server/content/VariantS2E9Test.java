package vn.geo.server.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.content.internal.variant.service.VariantService;
import vn.geo.server.content.internal.variant.service.VariantService.Plan;
import vn.geo.server.content.internal.variant.service.VariantService.Planned;
import vn.geo.server.geometry.internal.plane.PlaneEngine;
import vn.geo.server.geometry.internal.plane.PlaneSpec;
import vn.geo.server.geometry.internal.variant.VariantGenerator;
import vn.geo.server.geometry.internal.variant.VariantGeneratorBridge;

/**
 * US-S2E9-01 — biến thể đề (hàm thuần): sinh số liệu theo hạt giống, đáp số do engine hình phẳng
 * tính lại, từ chối đề không có số liệu / thiết diện động, miền 1…100, không bộ vô nghĩa.
 */
class VariantS2E9Test {

  static final JsonMapper MAPPER = JsonMapper.builder().build();
  static final PlaneEngine ENGINE = new PlaneEngine();
  static final VariantService SERVICE =
      new VariantService(
          null, null, new VariantGeneratorBridge(ENGINE, MAPPER), null, MAPPER, null);

  static final String FX12 = "Hình chữ nhật ABCD có AB = 6, AD = 8. Tính AC.";
  static final String FX12_FIG =
      """
      {"plane":{"shape":"rectangle","given":[{"type":"length","of":["A","B"],"value":6},
       {"type":"length","of":["A","D"],"value":8}],"asked":{"name":"AC","kind":"length","of":["A","C"]}}}
      """;
  static final String FX13 = "Tam giác đều ABC cạnh 4. Tính diện tích.";
  static final String FX13_FIG =
      """
      {"plane":{"shape":"equilateral_triangle","given":[{"type":"length","of":["A","B"],"value":4}],
       "asked":{"name":"S","kind":"area","of":[]}}}
      """;
  static final String TRIANGLE =
      """
      {"plane":{"shape":"triangle","given":[{"type":"length","of":["A","B"],"value":5},
       {"type":"length","of":["A","C"],"value":12},{"type":"length","of":["B","C"],"value":13}],
       "asked":{"name":"góc A","kind":"angle","of":["B","A","C"]}}}
      """;

  static String square(int side) {
    return """
    {"plane":{"shape":"square","given":[{"type":"length","of":["A","B"],"value":%d}],
     "asked":{"name":"AC","kind":"length","of":["A","C"]}}}
    """
        .formatted(side);
  }

  static double value(Planned p, int given) {
    return MAPPER
        .readTree(p.figure())
        .path("plane")
        .path("given")
        .get(given)
        .path("value")
        .asDouble();
  }

  @Test
  @DisplayName("TC-S2E9-001 Tạo 3 biến thể hình chữ nhật với đáp số tính lại")
  void tc001_rectangleVariants() {
    Plan plan = SERVICE.plan(FX12, FX12_FIG, 3, 7L);
    assertThat(plan.variants()).hasSize(3);
    assertThat(plan.message()).isNull();
    Set<String> pairs = new HashSet<>();
    for (Planned p : plan.variants()) {
      double ab = value(p, 0);
      double ad = value(p, 1);
      assertThat(ab == 6 && ad == 8).as("khác đề gốc (6, 8)").isFalse();
      assertThat(pairs.add(ab + ";" + ad)).as("khác nhau đôi một").isTrue();
      assertThat(p.value()).isCloseTo(Math.sqrt(ab * ab + ad * ad), within());
      // chữ đề mang đúng bộ số mới, cấu trúc đề giữ nguyên
      assertThat(p.statement())
          .isEqualTo(
              "Hình chữ nhật ABCD có AB = %d, AD = %d. Tính AC.".formatted((long) ab, (long) ad));
      assertThat(p.stepsJson()).contains("engine");
    }
  }

  @Test
  @DisplayName("TC-S2E9-002 Biến thể giữ dạng chuẩn của đáp số")
  void tc002_canonicalSurd() {
    // engine chọn cạnh 6: đáp số 9√3 ≈ 15,59 (đề gốc cạnh 4: 4√3)
    PlaneSpec side6 =
        PlaneSpec.from(
            MAPPER.readTree(FX13_FIG.replace("\"value\":4", "\"value\":6")).path("plane"));
    Double v = VariantGenerator.solve(ENGINE, side6);
    assertThat(v).isNotNull();
    assertThat(VariantGenerator.exact(v, "area")).isEqualTo("9√3");
    assertThat(PlaneEngine.trim(v, 2)).isEqualTo("15,59");
    assertThat(VariantGenerator.exact(4 * Math.sqrt(3), "area")).isEqualTo("4√3");
    assertThat(VariantGenerator.exact(10, "length")).isEqualTo("10");
    assertThat(VariantGenerator.exact(5 / Math.sqrt(2), "length")).isEqualTo("5√2/2");
    // mọi biến thể của đề tam giác đều giữ dạng có căn k√3 (hoặc phân số của √3) và chữ đề đổi
    // "cạnh n"
    for (Planned p : SERVICE.plan(FX13, FX13_FIG, 5, 11L).variants()) {
      assertThat(p.answer()).contains("√3");
      assertThat(p.statement()).matches("Tam giác đều ABC cạnh \\d+\\. Tính diện tích\\.");
      assertThat(p.stepsJson()).contains("≈ " + p.approx());
    }
  }

  @Test
  @DisplayName("TC-S2E9-004 Cùng hạt giống cho cùng bộ số")
  void tc004_sameSeedSameNumbers() {
    List<Planned> a = SERVICE.plan(FX12, FX12_FIG, 3, 42L).variants();
    List<Planned> b = SERVICE.plan(FX12, FX12_FIG, 3, 42L).variants();
    assertThat(a).hasSize(3);
    assertThat(a.stream().map(Planned::statement).toList())
        .isEqualTo(b.stream().map(Planned::statement).toList());
    assertThat(a.stream().map(Planned::answer).toList())
        .isEqualTo(b.stream().map(Planned::answer).toList());
    List<Planned> other = SERVICE.plan(FX12, FX12_FIG, 3, 43L).variants();
    assertThat(other.stream().map(Planned::statement).toList())
        .isNotEqualTo(a.stream().map(Planned::statement).toList());
  }

  @Test
  @DisplayName("TC-S2E9-005 Đề không có tham số số bị từ chối")
  void tc005_noNumbers() {
    assertThatThrownBy(() -> SERVICE.plan("Chứng minh tam giác cân", null, 3, 1L))
        .isInstanceOfSatisfying(
            AppException.class,
            e -> {
              assertThat(e.code()).isEqualTo(ErrorCode.CONFLICT);
              assertThat(e.getMessage()).isEqualTo("Đề này không có số liệu để đổi");
            });
    // đề không gian tham số a (không có con số) cũng không có số liệu để đổi
    assertThatThrownBy(() ->
            SERVICE.plan("Cho hình chóp S.ABCD cạnh a.", "{\"catalogCode\":\"HKN-02\"}", 1, 1L))
        .hasMessage("Đề này không có số liệu để đổi");
  }

  @Test
  @DisplayName("TC-S2E9-006 Thiết diện động chưa hỗ trợ")
  void tc006_dynamicSection() {
    assertThatThrownBy(() ->
            SERVICE.plan(
                "Điểm M di động trên SA, SA = 8. Tìm thiết diện.",
                "{\"section\":{\"moving\":\"M\"}}",
                3,
                1L))
        .isInstanceOfSatisfying(
            AppException.class,
            e -> {
              assertThat(e.code()).isEqualTo(ErrorCode.CONFLICT);
              assertThat(e.getMessage()).isEqualTo("Dạng này chưa hỗ trợ biến thể");
            });
  }

  @Test
  @DisplayName("TC-S2E9-007 Không bộ nào vô nghĩa trong 1000 lần sinh")
  void tc007_noDegenerateIn1000() {
    PlaneSpec spec = PlaneSpec.from(MAPPER.readTree(TRIANGLE).path("plane"));
    int produced = 0;
    for (long seed = 1; seed <= 1000; seed++) {
      VariantGenerator.Result r = VariantGenerator.generate(ENGINE, spec, null, null, 1, seed);
      assertThat(r.refusal()).isNull();
      for (VariantGenerator.Variant v : r.variants()) {
        produced++;
        double a = v.values().get(0);
        double b = v.values().get(1);
        double c = v.values().get(2);
        assertThat(List.of(a, b, c)).allSatisfy(x -> assertThat(x).isBetween(1.0, 100.0));
        assertThat(a + b).isGreaterThan(c); // bất đẳng thức tam giác chặt
        assertThat(a + c).isGreaterThan(b);
        assertThat(b + c).isGreaterThan(a);
        List<PlaneSpec.Given> given =
            List.of(
                new PlaneSpec.Given("length", List.of("A", "B"), a),
                new PlaneSpec.Given("length", List.of("A", "C"), b),
                new PlaneSpec.Given("length", List.of("B", "C"), c));
        PlaneSpec built =
            new PlaneSpec(spec.shape(), spec.points(), spec.at(), false, given, spec.asked(), null);
        assertThat(ENGINE.satisfied(ENGINE.build(built))).as("dựng được, thỏa ràng buộc").isTrue();
        assertThat(v.value()).isBetween(0.0001, 179.9999);
      }
    }
    assertThat(produced).isEqualTo(1000);
  }

  @Test
  @DisplayName("TC-S2E9-009 Số gốc sát cận 1 và 100 không sinh số ngoài 1 đến 100")
  void tc009_boundsNearLimits() {
    for (int origin : new int[] {1, 100}) {
      Plan plan =
          SERVICE.plan(
              "Hình vuông ABCD có AB = %d. Tính AC.".formatted(origin), square(origin), 10, 5L);
      assertThat(plan.variants()).hasSize(10);
      Set<Double> seen = new HashSet<>();
      for (Planned p : plan.variants()) {
        double side = value(p, 0);
        assertThat(side).isBetween(1.0, 100.0);
        assertThat(side).isEqualTo(Math.rint(side)); // số gốc nguyên: không có 0,5 hay 100,5
        assertThat(side).isNotEqualTo((double) origin);
        assertThat(seen.add(side)).isTrue();
        Matcher m = Pattern.compile("AB = (\\d+)\\.").matcher(p.statement());
        assertThat(m.find()).isTrue();
        assertThat(Double.parseDouble(m.group(1))).isEqualTo(side);
      }
    }
  }

  @Test
  @DisplayName("TC-S2E9-010 Ít bộ khả dĩ thì trả ít hơn kèm thông báo")
  void tc010_fewerThanRequested() {
    String narrow =
        """
        {"plane":{"shape":"rectangle","given":[{"type":"length","of":["A","B"],"value":5,"min":3,"max":7},
         {"type":"length","of":["A","D"],"value":12,"min":12,"max":12}],
         "asked":{"name":"AC","kind":"length","of":["A","C"]}}}
        """;
    Plan plan = SERVICE.plan("Hình chữ nhật ABCD có AB = 5, AD = 12. Tính AC.", narrow, 10, 3L);
    assertThat(plan.variants()).hasSize(4);
    assertThat(plan.message()).isEqualTo("Chỉ tạo được 4 biến thể khác nhau");
    Set<Double> ab = new HashSet<>();
    for (Planned p : plan.variants()) {
      JsonNode given = MAPPER.readTree(p.figure()).path("plane").path("given");
      ab.add(given.get(0).path("value").asDouble());
      assertThat(given.get(1).path("value").asDouble()).isEqualTo(12.0);
    }
    assertThat(ab).containsExactlyInAnyOrder(3.0, 4.0, 6.0, 7.0);
  }

  private static org.assertj.core.data.Offset<Double> within() {
    return org.assertj.core.data.Offset.offset(1e-9);
  }
}
