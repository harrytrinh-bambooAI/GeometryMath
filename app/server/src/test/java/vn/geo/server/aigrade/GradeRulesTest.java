package vn.geo.server.aigrade;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.aigrade.internal.engine.service.EngineMatcher;
import vn.geo.server.aigrade.internal.proposal.service.GradeRules;
import vn.geo.server.aigrade.internal.proposal.service.GradeRules.Criterion;

/**
 * Đơn vị (T-261128-61): kiểm cứng + luật chặn KPI §2 (US-S5E6-01) và bộ so đáp số KS-1/KS-2 của tín
 * hiệu engine (US-S5E6-03). Ca E2E gắn mã TC ở e2e/tests/s5e6.
 */
class GradeRulesTest {

  private static final JsonMapper M = JsonMapper.builder().build();

  /**
   * RB-01 (F-S5-051): Hướng 2 · Các bước 4 (phụ thuộc hướng) · Kết quả 2 · Hình 1 · Trình bày 1.
   */
  private static final List<Criterion> RB01 =
      List.of(
          new Criterion(1, "Hướng", new BigDecimal("2"), false),
          new Criterion(2, "Các bước", new BigDecimal("4"), true),
          new Criterion(3, "Kết quả", new BigDecimal("2"), false),
          new Criterion(4, "Hình", new BigDecimal("1"), false),
          new Criterion(5, "Trình bày", new BigDecimal("1"), false));

  private static JsonNode reply(
      double[] pts, double conf, String hinh, String steps, String extraC2) {
    StringBuilder b =
        new StringBuilder("{\"kind\":\"grade\",\"hinh\":\"" + hinh + "\",\"criteria\":[");
    for (int i = 0; i < 5; i++) {
      if (i > 0) {
        b.append(',');
      }
      b.append("{\"ma\":\"C")
          .append(i + 1)
          .append("\",\"diem\":")
          .append(pts[i])
          .append(",\"ly_do\":\"x\",\"tin_cay\":")
          .append(conf)
          .append(",\"vung\":{\"trang\":1,\"x\":1,\"y\":2,\"w\":3,\"h\":4}")
          .append(i == 1 ? extraC2 : "")
          .append('}');
    }
    b.append("],\"buoc\":").append(steps).append('}');
    return M.readTree(b.toString());
  }

  private static final String FOUR_OK =
      "[{\"ma\":\"B1\",\"diem\":1,\"dung\":true},{\"ma\":\"B2\",\"diem\":1,\"dung\":true},"
          + "{\"ma\":\"B3\",\"diem\":1,\"dung\":true},{\"ma\":\"B4\",\"diem\":1,\"dung\":true}]";

  private static GradeRules.Outcome run(JsonNode r) {
    return GradeRules.apply(RB01, BigDecimal.TEN, r, Set.of("KT11-C07-DN-010"));
  }

  @Test
  @DisplayName("F-S5-090: 2/4/2/1/1 = 10, tin cậy 0,86, không điều chỉnh")
  void fullMarks() {
    var o = run(reply(new double[] {2, 4, 2, 1, 1}, 0.86, "đúng", FOUR_OK, ""));
    assertThat(o.valid()).isTrue();
    assertThat(o.total()).isEqualByComparingTo("10");
    assertThat(o.confidence()).isEqualTo(0.86);
    assertThat(o.criteria()).allSatisfy(c -> assertThat(c.adjustments()).isEmpty());
  }

  @Test
  @DisplayName("§2.2 Kết quả đạt nhưng Bước 1/4: Kết quả 2 → 1, tổng 6")
  void rule2() {
    var o = run(reply(new double[] {2, 1, 2, 1, 1}, 0.86, "đúng", "[]", ""));
    assertThat(o.criteria().get(2).points()).isEqualByComparingTo("1");
    assertThat(o.criteria().get(2).adjustments().get(0).rule()).isEqualTo("luật KPI §2.2");
    assertThat(o.total()).isEqualByComparingTo("6");
  }

  @Test
  @DisplayName("§2.1 Hướng 0: Bước (phụ thuộc hướng) 3 → 0")
  void rule1() {
    var o = run(reply(new double[] {0, 3, 0, 1, 1}, 0.86, "đúng", "[]", ""));
    assertThat(o.criteria().get(1).points()).isEqualByComparingTo("0");
    assertThat(o.criteria().get(1).adjustments().get(0).rule()).isEqualTo("luật KPI §2.1");
  }

  @Test
  @DisplayName("§2.3 B2 sai, B3 phụ thuộc B2 → 0, B4 độc lập giữ; Bước 3 → 2")
  void rule3() {
    String steps =
        "[{\"ma\":\"B1\",\"diem\":1,\"dung\":true},{\"ma\":\"B2\",\"diem\":0,\"dung\":false},"
            + "{\"ma\":\"B3\",\"diem\":1,\"dung\":true,\"phu_thuoc\":[\"B2\"]},"
            + "{\"ma\":\"B4\",\"diem\":1,\"dung\":true}]";
    var o = run(reply(new double[] {2, 3, 0, 1, 1}, 0.74, "đúng", steps, ""));
    assertThat(o.criteria().get(1).points()).isEqualByComparingTo("2");
    assertThat(o.criteria().get(1).adjustments().get(0).rule()).isEqualTo("luật KPI §2.3");
    assertThat(o.steps().get(2).finalPoints()).isEqualByComparingTo("0");
    assertThat(o.steps().get(3).finalPoints()).isEqualByComparingTo("1");
  }

  @Test
  @DisplayName("§2.4 hình sai: Hình 1 → 0, B1 dựa trên hình → 0")
  void rule4() {
    String steps =
        "[{\"ma\":\"B1\",\"diem\":1,\"dung\":true,\"dua_tren_hinh\":true},"
            + "{\"ma\":\"B2\",\"diem\":1,\"dung\":true}]";
    var o = run(reply(new double[] {2, 4, 2, 1, 1}, 0.86, "hình sai", steps, ""));
    assertThat(o.criteria().get(3).points()).isEqualByComparingTo("0");
    assertThat(o.criteria().get(1).points()).isEqualByComparingTo("3");
    assertThat(o.steps().get(0).zeroedBy()).isEqualTo("luật KPI §2.4");
  }

  @Test
  @DisplayName("§2.5 kiến thức ngoài kho: cờ bịa kiến thức, tin cậy tiêu chí ≤ 0,5")
  void rule5() {
    var o =
        run(
            reply(
                new double[] {2, 4, 2, 1, 1},
                0.86,
                "đúng",
                FOUR_OK,
                ",\"kien_thuc\":[{\"ten\":\"định lí ba đường cao không gian\"}]"));
    assertThat(o.criteria().get(1).flags()).containsExactly(GradeRules.FLAG_INVENTED);
    assertThat(o.criteria().get(1).confidence()).isLessThanOrEqualTo(0.5);
    assertThat(o.flags()).contains(GradeRules.FLAG_INVENTED);
    var ok =
        run(
            reply(
                new double[] {2, 4, 2, 1, 1},
                0.86,
                "đúng",
                FOUR_OK,
                ",\"kien_thuc\":[{\"ma\":\"KT11-C07-DN-010\"}]"));
    assertThat(ok.flags()).isEmpty();
  }

  @Test
  @DisplayName("Kiểm cứng: Bước 5 > 4 điểm ngoài thang; biên 4 hợp lệ; 0,3 không bội 0,25")
  void hardCheck() {
    assertThat(run(reply(new double[] {2, 5, 2, 1, 1}, 0.86, "đúng", "[]", "")).invalidReason())
        .isEqualTo(GradeRules.REASON_OUT_OF_SCALE);
    assertThat(run(reply(new double[] {2, 4, 2, 1, 1}, 0.86, "đúng", "[]", "")).valid()).isTrue();
    assertThat(run(reply(new double[] {2, 3.3, 2, 1, 1}, 0.86, "đúng", "[]", "")).valid())
        .isFalse();
  }

  @Test
  @DisplayName("Engine: KS-1 a√6/3, KS-2 0,816a khớp; a√3/3 lệch; không có dòng kết luận")
  void engine() {
    assertThat(EngineMatcher.compare(List.of("Vậy d(A,(SBC)) = a√6/3"), "a√6/3").status())
        .isEqualTo(EngineMatcher.MATCH);
    var ks2 = EngineMatcher.compare(List.of("Kẻ AH ⊥ SB.", "d ≈ 0,816a"), "a√6/3");
    assertThat(ks2.status()).isEqualTo(EngineMatcher.MATCH);
    assertThat(ks2.rule()).isEqualTo("KS-2");
    assertThat(EngineMatcher.compare(List.of("Vậy d = a√3/3."), "a√6/3").status())
        .isEqualTo(EngineMatcher.MISMATCH);
    assertThat(EngineMatcher.compare(List.of("Kẻ AH ⊥ SB tại H.", "SB = a√3"), "a√6/3").status())
        .isEqualTo(EngineMatcher.NOT_FOUND);
    assertThat(EngineMatcher.compare(List.of("d ≈ 0,8a"), "a√6/3").status())
        .isEqualTo(EngineMatcher.MISMATCH);
    assertThat(EngineMatcher.disagree(EngineMatcher.MISMATCH, BigDecimal.TWO, BigDecimal.TWO))
        .isTrue();
    assertThat(EngineMatcher.disagree(EngineMatcher.MATCH, BigDecimal.TWO, BigDecimal.TWO))
        .isFalse();
  }
}
