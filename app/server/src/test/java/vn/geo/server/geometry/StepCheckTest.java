package vn.geo.server.geometry;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.geo.server.geometry.internal.stepcheck.StepCheck;
import vn.geo.server.geometry.internal.stepcheck.StepCheck.Outcome;
import vn.geo.server.geometry.internal.stepcheck.StepCheck.Status;

/** S2-E5 — StepCheck là hàm thuần (US-S2E5-02): dung sai 0,01, lỗi không lan, bước chữ. */
class StepCheckTest {

  static final String STATEMENT =
      "Cho hình chóp S.ABCD có đáy ABCD là hình vuông cạnh a, SA vuông góc với mặt phẳng (ABCD)"
          + " và SA = a√2. Tính góc giữa đường thẳng SC và mặt đáy.";

  /** FX-14 (SP-01): hình engine dựng với a = 1; đáp số engine 45°. */
  static StepCheck.Context fx14() {
    Map<String, double[]> p = new LinkedHashMap<>();
    p.put("A", new double[] {0, 0, 0});
    p.put("B", new double[] {1, 0, 0});
    p.put("C", new double[] {1, 1, 0});
    p.put("D", new double[] {0, 1, 0});
    p.put("S", new double[] {0, 0, Math.sqrt(2)});
    return new StepCheck.Context(p, List.of(STATEMENT, "AB = a", "SA = a√2"), 45.0, true);
  }

  static final String S1 = "SA ⊥ (ABCD) nên AC là hình chiếu của SC, góc cần tìm là SCA.";
  static final String S2 = "ABCD vuông cạnh a nên AC = a√2.";
  static final String S3_WRONG = "tan SCA = SA / AC = √2.";
  static final String S4_WRONG = "Vậy góc giữa SC và mặt phẳng đáy bằng 54,7°.";
  static final String S3_RIGHT = "tan SCA = a√2 / a√2 = 1.";
  static final String S4_RIGHT = "Vậy góc giữa SC và mặt phẳng đáy bằng 45°.";

  static List<Status> statuses(Outcome o) {
    return o.steps().stream().map(StepCheck.StepResult::status).toList();
  }

  @Test
  @DisplayName("TC-S2E5-018 Lệch 0,0018 (6,93 so với 6,9282) là Đạt")
  void tc018() {
    assertThat(StepCheck.within("6,93", 6.93, 6.9282)).isTrue();
    Outcome o =
        StepCheck.run(
            List.of("S = 4√3 = 6,93"), new StepCheck.Context(Map.of(), List.of(), null, false));
    assertThat(statuses(o)).containsExactly(Status.PASS);
  }

  @Test
  @DisplayName("TC-S2E5-019 Lệch đúng 0,01 là Đạt")
  void tc019() {
    assertThat(StepCheck.within("6,94", 6.94, 6.93)).isTrue();
    assertThat(StepCheck.within("6,92", 6.92, 6.93)).isTrue();
  }

  @Test
  @DisplayName("TC-S2E5-020 Lệch 0,011 là Không đạt, lý do ghi cả hai giá trị")
  void tc020() {
    assertThat(StepCheck.within("6,941", 6.941, 6.93)).isFalse();
    Outcome o =
        StepCheck.run(
            List.of("x = 6,93", "y = x = 6,941"),
            new StepCheck.Context(Map.of(), List.of(), null, false));
    assertThat(o.steps().get(1).status()).isEqualTo(Status.FAIL);
    assertThat(o.steps().get(1).reason()).contains("6,941").contains("6,93");
  }

  @Test
  @DisplayName("US-S2E5-02-SC1 bước 3 sai tô đỏ kèm lý do; bước 4 đạt theo giá trị bước 3")
  void wrongStep3DoesNotPropagate() {
    Outcome o = StepCheck.run(List.of(S1, S2, S3_WRONG, S4_WRONG), fx14());
    assertThat(statuses(o))
        .containsExactly(Status.UNVERIFIABLE, Status.PASS, Status.FAIL, Status.PASS);
    assertThat(o.steps().get(0).reason()).isEqualTo("bước không có phép tính");
    assertThat(o.steps().get(2).reason()).isEqualTo("ghi √2 ≈ 1,41; máy tính lại 1");
    assertThat(o.fin().state()).isEqualTo("mismatch");
  }

  @Test
  @DisplayName("US-S2E5-02-SC2 lời giải đúng: mọi bước có phép tính Đạt, đáp số khớp 45°")
  void rightSolution() {
    Outcome o = StepCheck.run(List.of(S1, S2, S3_RIGHT, S4_RIGHT), fx14());
    assertThat(statuses(o))
        .containsExactly(Status.UNVERIFIABLE, Status.PASS, Status.PASS, Status.PASS);
    assertThat(o.fin().state()).isEqualTo("match");
  }

  @Test
  @DisplayName("US-S2E5-02-FC1, FC3 không đọc được biểu thức; thiếu giá trị bước trước")
  void unreadableAndMissing() {
    Outcome a = StepCheck.run(List.of(S1, S2, "tan SCA = abc", S4_RIGHT), fx14());
    assertThat(a.steps().get(2).status()).isEqualTo(Status.UNVERIFIABLE);
    assertThat(a.steps().get(2).reason()).isEqualTo("không đọc được biểu thức");
    assertThat(a.steps().get(1).status()).isEqualTo(Status.PASS);
    Outcome b = StepCheck.run(List.of(S1, "tan SCA = SA / AC = 1.", S4_RIGHT), fx14());
    assertThat(b.steps().get(1).status()).isEqualTo(Status.UNVERIFIABLE);
    assertThat(b.steps().get(1).reason()).isEqualTo("thiếu giá trị đầu vào từ bước trước");
  }

  @Test
  @DisplayName("US-S2E5-03-FC4 mọi bước đạt theo bước trước nhưng đáp số cuối lệch engine")
  void consistentButWrongFinal() {
    Outcome o = StepCheck.run(List.of(S1, "tan SCA = SA / AB = a√2 / a = √2.", S4_WRONG), fx14());
    assertThat(statuses(o)).containsExactly(Status.UNVERIFIABLE, Status.PASS, Status.PASS);
    assertThat(o.fin().state()).isEqualTo("mismatch");
    assertThat(o.fin().note()).contains("54,7°").contains("45°");
  }

  @Test
  @DisplayName("US-S2E5-02-BV5 30 bước có đủ 30 kết quả theo thứ tự; chữ lạ không gây lỗi")
  void thirtySteps() {
    List<String> steps = new ArrayList<>();
    for (int i = 1; i <= 30; i++) {
      steps.add(i % 3 == 0 ? "Bước lập luận số " + i + " ((( = %%% √" : i + " + 1 = " + (i + 1));
    }
    Outcome o = StepCheck.run(steps, fx14());
    assertThat(o.steps()).hasSize(30);
    for (int i = 0; i < 30; i++) {
      assertThat(o.steps().get(i).stepNo()).isEqualTo(i + 1);
    }
  }

  @Test
  @DisplayName("Đáp số đúng số nhưng sai đơn vị không khớp engine")
  void wrongUnit() {
    Outcome o = StepCheck.run(List.of(S1, S2, S3_RIGHT, "Vậy góc cần tìm bằng 45 cm."), fx14());
    assertThat(o.fin().state()).isEqualTo("mismatch");
  }
}
