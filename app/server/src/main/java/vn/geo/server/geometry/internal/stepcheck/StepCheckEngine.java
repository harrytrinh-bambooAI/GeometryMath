package vn.geo.server.geometry.internal.stepcheck;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import org.springframework.stereotype.Component;

/**
 * Cửa gọi StepCheck cho module khác (content) mà không lộ lớp internal: bean {@code
 * stepCheckEngine} kiểu {@code BiFunction<List<String>, Map<String, Object>, Map<String, Object>>}.
 *
 * <p>Đầu vào: danh sách bước; ngữ cảnh {@code points} (Map tên đỉnh -> double[]), {@code givens}
 * (List câu dữ kiện), {@code askedValue} (Double, có thể thiếu), {@code askedIsAngle} (Boolean).
 * Đầu ra: {@code steps} = List các Map {stepNo, status (pass | fail | unverifiable), reason,
 * written, computed}; {@code finalState} (match | mismatch | unknown), {@code finalNote}.
 */
@Component("stepCheckEngine")
public class StepCheckEngine
    implements BiFunction<List<String>, Map<String, Object>, Map<String, Object>> {

  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Object> apply(List<String> steps, Map<String, Object> context) {
    Map<String, double[]> points =
        context.get("points") instanceof Map<?, ?> m ? (Map<String, double[]>) m : Map.of();
    List<String> givens = context.get("givens") instanceof List<?> l ? (List<String>) l : List.of();
    Double asked = context.get("askedValue") instanceof Number n ? n.doubleValue() : null;
    boolean angle = Boolean.TRUE.equals(context.get("askedIsAngle"));
    StepCheck.Outcome o = StepCheck.run(steps, new StepCheck.Context(points, givens, asked, angle));
    List<Map<String, Object>> out = new ArrayList<>();
    for (StepCheck.StepResult r : o.steps()) {
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("stepNo", r.stepNo());
      m.put("status", r.status().name().toLowerCase(Locale.ROOT));
      m.put("reason", r.reason());
      m.put("written", r.written());
      m.put("computed", r.computed());
      out.add(m);
    }
    Map<String, Object> res = new LinkedHashMap<>();
    res.put("steps", out);
    res.put("finalState", o.fin().state());
    res.put("finalNote", o.fin().note());
    return res;
  }
}
