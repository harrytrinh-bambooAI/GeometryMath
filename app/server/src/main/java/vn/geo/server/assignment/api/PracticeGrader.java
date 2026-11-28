package vn.geo.server.assignment.api;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Chấm đáp số bài luyện bằng {@code grade} của khung môn học (US-S2E8-02). Gọi bean cầu nối {@code
 * subjectAnswerGrader} của {@code subject/internal/grade} (kiểu JDK thuần; xem ghi chú ở
 * AnswerGraderBridge: sẽ thay bằng {@code subject.api} khi có FIX).
 */
@Component
public class PracticeGrader {

  /**
   * @param outcome correct | wrong | syntax
   * @param canonical dạng chuẩn khi Đúng mà viết khác mẫu
   * @param reason lý do Sai cú pháp kèm vị trí ký tự
   * @param note lời nhắc dạng viết khi Sai (không chứa đáp số)
   */
  public record Graded(
      String outcome, String canonical, String reason, Integer position, String note) {}

  private final Function<Map<String, Object>, Map<String, Object>> bridge;

  public PracticeGrader(
      @Qualifier("subjectAnswerGrader") Function<Map<String, Object>, Map<String, Object>> bridge) {
    this.bridge = bridge;
  }

  /** Đáp số mẫu có chấm được bằng máy không (BR-S2E8-27). */
  public boolean gradable(String reference) {
    if (reference == null) {
      return false;
    }
    Map<String, Object> in = new HashMap<>();
    in.put("reference", reference);
    return Boolean.TRUE.equals(bridge.apply(in).get("gradable"));
  }

  /** Chuẩn hóa cách viết (bỏ dấu cách, chấm thành phẩy, * thành ×). */
  public String normalize(String text) {
    Map<String, Object> in = new HashMap<>();
    in.put("reference", text);
    return String.valueOf(bridge.apply(in).get("normalized"));
  }

  /**
   * Chấm theo cấu hình của câu. Dạng viết giáo viên đã xác nhận thêm (acceptedForms) được tính Đúng
   * trước khi so giá trị.
   */
  public Graded grade(
      String reference,
      String answerKind,
      BigDecimal tolerance,
      List<String> acceptedForms,
      String submitted) {
    String s = submitted == null ? "" : submitted;
    if (acceptedForms != null && !acceptedForms.isEmpty() && !s.isBlank()) {
      String norm = normalize(s);
      for (String f : acceptedForms) {
        if (norm.equals(normalize(f))) {
          return new Graded("correct", reference.strip(), null, null, null);
        }
      }
    }
    Map<String, Object> in = new HashMap<>();
    in.put("reference", reference);
    in.put("submitted", s);
    in.put("mode", "approx".equals(answerKind) ? "approx" : "exact");
    in.put("tolerance", tolerance);
    Map<String, Object> out = bridge.apply(in);
    return new Graded(
        (String) out.get("outcome"),
        (String) out.get("canonical"),
        (String) out.get("message"),
        (Integer) out.get("position"),
        (String) out.get("note"));
  }
}
