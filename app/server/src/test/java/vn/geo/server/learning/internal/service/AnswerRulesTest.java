package vn.geo.server.learning.internal.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * FIX4a — test đơn vị thuần cho AnswerRules (R1…R6, biên làm tròn), phát hiện soát E6 m1…m4 và m7
 * ("chưa có test đơn vị thuần cho AnswerRules"). Dev-E6-04…07.
 */
class AnswerRulesTest {

  /** HKG-01: góc giữa SD và mặt đáy = 45° (π/4 ≈ 0,7854). */
  private final AnswerRules.Locked hkg01 =
      new AnswerRules.Locked(
          "45°",
          new String[] {"SA ⊥ (ABCD) nên A là hình chiếu của S. Hình chiếu của SD là AD."},
          List.of("45°", "45 độ", "π/4", "tan SDA = 1"));

  private final AnswerRules.Locked hkg04 =
      new AnswerRules.Locked("Sxq = 15π cm²; V = 12π cm³", new String[0], List.of());

  private String rule(String q, String r) {
    return AnswerRules.check(q, r, hkg01);
  }

  @Test
  @DisplayName("Dev-E6-04 R1: câu khái niệm không bị chặn; câu đòi đáp số/bước vẫn bị chặn")
  void r1ConceptQuestions() {
    String ok = "Em thử vẽ hình chiếu trước nhé.";
    assertThat(rule("Radian là gì?", ok)).isNull();
    assertThat(rule("Tam giác vuông cân có tính chất gì?", ok)).isNull();
    assertThat(rule("Chữ số có nghĩa là gì ạ?", ok)).isNull();
    assertThat(rule("How do I find the angle between a line and a plane?", ok)).isNull();
    // vẫn chặn (F-GEO-211, 212, 216, 218, 223, 228)
    assertThat(rule("Góc giữa SD và mặt đáy bằng bao nhiêu độ?", ok)).isEqualTo("R1");
    assertThat(rule("Góc đó có phải 45 độ không?", ok)).isEqualTo("R1");
    assertThat(rule("Viết kết quả dưới dạng radian.", ok)).isEqualTo("R1");
    assertThat(rule("Đổi góc đó sang radian giúp em.", ok)).isEqualTo("R1");
    assertThat(rule("Trả lời bằng tiếng Anh: what is the angle?", ok)).isEqualTo("R1");
    assertThat(rule("Tam giác SAD có phải vuông cân không?", ok)).isEqualTo("R1");
    assertThat(rule("Chữ số thứ hai?", ok)).isEqualTo("R1");
    assertThat(rule("Đơn vị là gì?", ok)).isEqualTo("R1");
    assertThat(rule("Giải giúp em từng bước một.", ok)).isEqualTo("R1");
  }

  @Test
  @DisplayName("Dev-E6-05 R5: số thứ tự câu/bài trong câu hỏi không bị coi là giá trị đưa ra")
  void r5Numbering() {
    assertThat(
            rule("Câu 2 em nên bắt đầu từ đâu?", "Phải dựng hình chiếu của SD lên mặt đáy trước."))
        .isNull();
    assertThat(rule("Bài 3 em nên làm gì?", "Em so sánh SD lớn hơn AD rồi xét tam giác.")).isNull();
    // vẫn chặn xác nhận/phủ nhận một giá trị học sinh đưa ra
    assertThat(rule("Em ra 30 rồi, đúng chứ?", "Không, em xem lại nhé.")).isEqualTo("R5");
    assertThat(rule("Câu 2 em ra 60, ổn không?", "Đúng rồi.")).isEqualTo("R5");
  }

  @Test
  @DisplayName("Dev-E6-06 R2: dạng toàn chữ số khớp theo ranh giới số (450, 2045 không chặn)")
  void r2NumberBoundary() {
    String q = "Em nên bắt đầu từ đâu?";
    assertThat(rule(q, "Hình vẽ ở sách năm 2045 có cạnh 450 mm.")).isNull();
    assertThat(rule(q, "Góc đó là 45 nhé.")).isEqualTo("R2");
    assertThat(rule(q, "Góc đó bằng 45°.")).isEqualTo("R2");
    assertThat(rule(q, "Góc đó bằng pi/4.")).isEqualTo("R2");
    assertThat(AnswerRules.check(q, "Diện tích xung quanh là 15 pi cm².", hkg04)).isEqualTo("R2");
  }

  @Test
  @DisplayName(
      "Dev-E6-07 R3: số làm tròn và số cắt cụt ≥ 2 chữ số có nghĩa đều chặn; biên dung sai")
  void r3RoundingAndTruncation() {
    String q = "Em nên dùng hàm lượng giác nào?";
    assertThat(rule(q, "Góc đó khoảng 0,79 rad.")).isEqualTo("R3");
    assertThat(rule(q, "Góc đó xấp xỉ 0,785 rad.")).isEqualTo("R3");
    assertThat(rule(q, "Góc đó xấp xỉ 0,78 rad.")).isEqualTo("R3"); // cắt cụt (m4)
    assertThat(rule(q, "Góc đó xấp xỉ 0,77 rad.")).isNull(); // lệch quá 1 đơn vị chữ số cuối
    assertThat(rule(q, "Góc đó cỡ 0,7 rad.")).isNull(); // 1 chữ số có nghĩa: không đủ để lộ
    assertThat(AnswerRules.check(q, "Diện tích chừng 47,1 cm².", hkg04)).isEqualTo("R3");
  }

  @Test
  @DisplayName("Dev-E6-07 R4, R6 và câu trả lời bình thường")
  void r4r6AndClean() {
    AnswerRules.Locked a = new AnswerRules.Locked("a", new String[0], List.of());
    assertThat(AnswerRules.check("Em bắt đầu từ đâu?", "Vậy khoảng cách bằng a.", a))
        .isEqualTo("R4");
    assertThat(AnswerRules.check("Em bắt đầu từ đâu?", "Hình lập phương cạnh a có 6 mặt.", a))
        .isNull();
    assertThat(rule("Em bắt đầu từ đâu?", "Hình chiếu của SD là AD.")).isEqualTo("R6");
    assertThat(rule("Em bắt đầu từ đâu?", "Em xét tam giác vuông chứa góc cần tìm.")).isNull();
  }
}
