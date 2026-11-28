package vn.geo.server.content;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.geo.server.content.internal.plane.service.PlaneBenchCalc;
import vn.geo.server.content.internal.plane.service.PlaneBenchCalc.Item;
import vn.geo.server.content.internal.plane.service.PlaneBenchCalc.Report;

/** US-S2E2-05 — tính ba chỉ số bench-s2 trên kết quả giả lập (hàm thuần). */
class PlaneBenchCalcTest {

  /** 30 đề: k đề đầu thỏa ràng buộc, j đề đầu khớp đáp số; mọi đề đều không mang nhãn khi sai. */
  static List<Item> items(int sceneOk, int answerOk) {
    List<Item> out = new ArrayList<>();
    for (int i = 1; i <= 30; i++) {
      out.add(new Item("PLS-%02d".formatted(i), i <= sceneOk, i <= answerOk, i <= answerOk));
    }
    return out;
  }

  static Report run(List<Item> items) {
    return PlaneBenchCalc.evaluate(items, "10:00, 26/11/2026");
  }

  @Test
  @DisplayName("TC-S2E2-040 Thỏa ràng buộc 29/30 đạt, 28/30 rớt")
  void tc040_constraints() {
    Report ok = run(items(29, 30));
    assertThat(ok.m1()).isEqualTo("96,7 %");
    assertThat(ok.m1Pass()).isTrue();
    assertThat(ok.exitCode()).isZero();
    Report bad = run(items(28, 30));
    assertThat(bad.m1()).isEqualTo("93,3 %");
    assertThat(bad.m1Pass()).isFalse();
    assertThat(bad.exitCode()).isNotZero();
    assertThat(bad.failed()).hasSize(2);
    assertThat(bad.failed())
        .allSatisfy(f -> assertThat(f.reason()).contains("không thỏa ràng buộc"));
  }

  @Test
  @DisplayName("TC-S2E2-041 Đáp số khớp 27/30 đạt đúng ngưỡng, 26/30 rớt")
  void tc041_answers() {
    Report ok = run(items(30, 27));
    assertThat(ok.m2()).isEqualTo("90,0 %");
    assertThat(ok.m2Pass()).isTrue();
    assertThat(ok.exitCode()).isZero();
    Report bad = run(items(30, 26));
    assertThat(bad.m2()).isEqualTo("86,7 %");
    assertThat(bad.m2Pass()).isFalse();
    assertThat(bad.exitCode()).isNotZero();
  }

  @Test
  @DisplayName("TC-S2E2-042 Một bài sai mang nhãn Đã kiểm chứng thì rớt")
  void tc042_wrongLabeled() {
    List<Item> list = new ArrayList<>(items(30, 30));
    list.set(6, new Item("PLS-07", true, false, true));
    Report r = run(list);
    assertThat(r.wrongLabeled()).isEqualTo(1);
    assertThat(r.m3()).isEqualTo("1");
    assertThat(r.m3Pass()).isFalse();
    assertThat(r.exitCode()).isNotZero();
    assertThat(r.failed())
        .anySatisfy(f -> {
          assertThat(f.code()).isEqualTo("PLS-07");
          assertThat(f.reason()).contains("sai mang nhãn");
        });
    assertThat(r.markdown()).contains("PLS-07").contains("sai mang nhãn");
  }

  @Test
  @DisplayName("TC-S2E2-043 Bộ đề rỗng không chia cho 0")
  void tc043_empty() {
    Report r = run(List.of());
    assertThat(r.m1()).isEqualTo("—");
    assertThat(r.m2()).isEqualTo("—");
    assertThat(r.exitCode()).isNotZero();
    assertThat(r.message()).isEqualTo("bộ đề rỗng");
    assertThat(r.markdown()).contains("bộ đề rỗng").contains("—");
  }
}
