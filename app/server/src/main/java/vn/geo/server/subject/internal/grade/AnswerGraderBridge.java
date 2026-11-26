package vn.geo.server.subject.internal.grade;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.stereotype.Component;

/**
 * Cầu nối TẠM để module khác gọi {@link AnswerGrader} mà không phụ thuộc gói {@code internal}
 * (ArchUnit) và không tạo chu trình module: bean tên {@value #BEAN} kiểu JDK thuần {@code
 * Function<Map, Map>}. Lý do: giao diện công khai đúng chỗ là {@code subject.api}, nhưng tệp đó
 * ngoài phạm vi tệp của T-261126-21 (PLAN_code-s23 §5) — đề nghị FIX chuyển sang {@code
 * subject.api.AnswerGrader} rồi bỏ lớp này.
 *
 * <p>Vào: {@code reference}, {@code submitted} (vắng = chỉ hỏi "chấm được không"), {@code mode}
 * ("exact" | "approx"), {@code tolerance} (BigDecimal hoặc null). Ra: {@code gradable} hoặc {@code
 * outcome} ("correct" | "wrong" | "syntax"), {@code canonical}, {@code message}, {@code position},
 * {@code note}, {@code normalized}.
 */
@Component(AnswerGraderBridge.BEAN)
public class AnswerGraderBridge implements Function<Map<String, Object>, Map<String, Object>> {

  public static final String BEAN = "subjectAnswerGrader";

  @Override
  public Map<String, Object> apply(Map<String, Object> in) {
    Map<String, Object> out = new HashMap<>();
    String reference = (String) in.get("reference");
    if (!in.containsKey("submitted")) {
      out.put("gradable", reference != null && AnswerGrader.gradable(reference));
      out.put("normalized", AnswerGrader.normalize(reference));
      return out;
    }
    String submitted = (String) in.get("submitted");
    AnswerGrader.Mode mode =
        "approx".equals(in.get("mode")) ? AnswerGrader.Mode.APPROX : AnswerGrader.Mode.EXACT;
    AnswerGrader.Result r =
        AnswerGrader.grade(reference, submitted, mode, (BigDecimal) in.get("tolerance"));
    out.put("outcome", r.outcome().name().toLowerCase(java.util.Locale.ROOT));
    out.put("canonical", r.canonical());
    out.put("message", r.message());
    out.put("position", r.position());
    out.put("note", r.note());
    out.put("normalized", AnswerGrader.normalize(submitted));
    return out;
  }
}
