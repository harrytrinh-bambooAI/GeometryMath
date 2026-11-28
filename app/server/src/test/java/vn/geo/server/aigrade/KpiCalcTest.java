package vn.geo.server.aigrade;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.geo.server.aigrade.internal.kpi.service.KpiCalc;
import vn.geo.server.aigrade.internal.kpi.service.KpiCalc.Crit;
import vn.geo.server.aigrade.internal.kpi.service.KpiCalc.Paper;
import vn.geo.server.aigrade.internal.kpi.service.KpiCalc.Rate;
import vn.geo.server.aigrade.internal.kpi.service.KpiCalc.Thresholds;

/** US-S5E6-06 (T-261128-65): KTC Wilson, t, điều kiện KPI §4 của cổng "AI đề xuất". */
class KpiCalcTest {

  private static final double[] MAX = {2, 4, 2, 1, 1};

  private static Paper paper(String topic, double[] gv, double[] ai) {
    List<Crit> cs = new ArrayList<>();
    for (int i = 0; i < MAX.length; i++) {
      cs.add(new Crit(MAX[i], ai[i], gv[i], false));
    }
    return new Paper(topic, cs, true, 0, 0, false, null);
  }

  @Test
  @DisplayName("Wilson 88/100 = 80,2 %–93,0 % (TC-S5E6-28), 190/200 cận dưới 91,0 % (TC-S5E6-29)")
  void wilson() {
    Rate r = KpiCalc.wilson(88, 100);
    assertThat(KpiCalc.pct(r.lower())).isEqualTo("80,2");
    assertThat(KpiCalc.pct(r.upper())).isEqualTo("93");
    assertThat(KpiCalc.pct(KpiCalc.wilson(190, 200).lower())).isEqualTo("91");
    assertThat(KpiCalc.t975(39)).isBetween(2.02, 2.03);
    assertThat(KpiCalc.median(List.of(30, 10, 20, 40))).isEqualTo(25.0);
  }

  @Test
  @DisplayName("Chưa đủ 20 bài thì không đạt kèm MSG-S5-48; đủ 20 bài khớp hoàn toàn thì đạt")
  void minPapers() {
    double[] full = {2, 4, 2, 1, 1};
    List<Paper> ps = new ArrayList<>();
    for (int i = 0; i < 19; i++) {
      ps.add(paper("khoảng cách", full, full));
    }
    var r = KpiCalc.compute(ps, List.of(), Thresholds.kpiDefault(), true);
    assertThat(r.reasons())
        .containsExactly("Lớp chưa đủ 19/20 bài chạy song song để bật AI đề xuất.");
    ps.add(paper("khoảng cách", full, full));
    assertThat(KpiCalc.compute(ps, List.of(), Thresholds.kpiDefault(), true).pass()).isTrue();
  }

  @Test
  @DisplayName("Một dạng dưới 70 % và báo nhầm, bịa kiến thức đều nêu lý do")
  void reasons() {
    double[] full = {2, 4, 2, 1, 1};
    List<Paper> ps = new ArrayList<>();
    for (int i = 0; i < 38; i++) {
      ps.add(paper(i % 2 == 0 ? "khoảng cách" : "thể tích", full, full));
    }
    ps.add(paper("góc", new double[] {2, 0.6, 2, 1, 1}, full));
    ps.add(
        new Paper(
            "góc",
            paper("góc", new double[] {2, 0.6, 2, 1, 1}, full).criteria(),
            true,
            1,
            0,
            false,
            null));
    var r = KpiCalc.compute(ps, List.of(), Thresholds.kpiDefault(), true);
    assertThat(r.reasons()).contains("dạng góc 66 % < 70 %", "bịa kiến thức: 1");
    @SuppressWarnings("unchecked")
    Map<String, Object> e = (Map<String, Object>) r.metrics().get("e");
    assertThat(e.get("invented")).isEqualTo(1);
  }
}
