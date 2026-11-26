package vn.geo.server.geometry.internal.variant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.geometry.internal.plane.PlaneEngine;
import vn.geo.server.geometry.internal.plane.PlaneSpec;

/**
 * Cầu nối TẠM để module {@code content} gọi {@link VariantGenerator} mà không phụ thuộc gói {@code
 * internal} (ArchUnit): bean tên {@value #BEAN} kiểu JDK thuần {@code Function<Map, Map>} (cùng mẫu
 * với {@code subjectAnswerGrader} của S2-E8). Giao diện công khai đúng chỗ là {@code subject.api} /
 * {@code geometry.api}, nhưng hai gói đó ngoài phạm vi tệp của T-261126-25 (PLAN_code-s23 §5) — đề
 * nghị FIX chuyển sang {@code SubjectEngine.variants(…)} rồi bỏ lớp này.
 *
 * <p>Vào: {@code figure} (chuỗi JSON cột {@code solution_doc.figure}, có thể null), {@code op}:
 *
 * <ul>
 *   <li>{@code "params"} → ra {@code refusal} hoặc {@code params}: danh sách {@code {index, type,
 *       of, value}} các dữ kiện số đổi được;
 *   <li>{@code "generate"} (mặc định) kèm {@code variable} (List&lt;Integer&gt; hoặc null), {@code
 *       count}, {@code seed} → ra {@code refusal} hoặc {@code variants}: danh sách {@code {figure,
 *       values, answer, value, approx}} và {@code message} khi trả ít hơn yêu cầu.
 * </ul>
 */
@Component(VariantGeneratorBridge.BEAN)
public class VariantGeneratorBridge implements Function<Map<String, Object>, Map<String, Object>> {

  public static final String BEAN = "planeVariantGenerator";

  private final PlaneEngine engine;
  private final JsonMapper mapper;

  public VariantGeneratorBridge(PlaneEngine engine, JsonMapper mapper) {
    this.engine = engine;
    this.mapper = mapper;
  }

  @Override
  public Map<String, Object> apply(Map<String, Object> in) {
    Map<String, Object> out = new HashMap<>();
    JsonNode figure;
    PlaneSpec spec;
    try {
      String json = (String) in.get("figure");
      figure = json == null ? null : mapper.readTree(json);
      String refusal = refusal(figure);
      if (refusal != null) {
        out.put("refusal", refusal);
        return out;
      }
      spec = PlaneSpec.from(figure.path("plane"));
    } catch (RuntimeException e) {
      out.put("refusal", VariantGenerator.NO_PARAMS);
      return out;
    }
    if (spec.coords() != null || !spec.supported() || spec.asked() == null) {
      out.put("refusal", VariantGenerator.UNSUPPORTED);
      return out;
    }
    List<Integer> numeric = VariantGenerator.numericGivens(spec);
    if ("params".equals(in.get("op"))) {
      List<Map<String, Object>> params = new ArrayList<>();
      for (int i : numeric) {
        PlaneSpec.Given g = spec.given().get(i);
        Map<String, Object> p = new HashMap<>();
        p.put("index", i);
        p.put("type", g.type());
        p.put("of", g.of());
        p.put("value", g.value());
        params.add(p);
      }
      if (params.isEmpty()) {
        out.put("refusal", VariantGenerator.NO_PARAMS);
      }
      out.put("params", params);
      return out;
    }
    @SuppressWarnings("unchecked")
    List<Integer> variable = (List<Integer>) in.get("variable");
    int count = ((Number) in.get("count")).intValue();
    long seed = ((Number) in.get("seed")).longValue();
    List<VariantGenerator.Range> ranges = new ArrayList<>();
    for (JsonNode g : figure.path("plane").path("given")) {
      ranges.add(
          new VariantGenerator.Range(
              g.path("min").isNumber() ? g.path("min").asDouble() : null,
              g.path("max").isNumber() ? g.path("max").asDouble() : null));
    }
    VariantGenerator.Result r =
        VariantGenerator.generate(engine, spec, variable, ranges, count, seed);
    if (r.refusal() != null) {
      out.put("refusal", r.refusal());
      return out;
    }
    List<Map<String, Object>> variants = new ArrayList<>();
    for (VariantGenerator.Variant v : r.variants()) {
      ObjectNode copy = (ObjectNode) figure.deepCopy();
      ArrayNode given = (ArrayNode) copy.path("plane").path("given");
      for (int i = 0; i < given.size(); i++) {
        double x = v.values().get(i);
        ObjectNode g = (ObjectNode) given.get(i);
        if (Math.abs(x - Math.rint(x)) < 1e-9) {
          g.put("value", (long) Math.rint(x));
        } else {
          g.put("value", x);
        }
      }
      Map<String, Object> m = new HashMap<>();
      m.put("figure", copy.toString());
      m.put("values", v.values());
      m.put("answer", v.answer());
      m.put("value", v.value());
      m.put("approx", v.approx());
      variants.add(m);
    }
    out.put("variants", variants);
    out.put("message", r.message());
    return out;
  }

  /**
   * Từ chối theo BR-S2E9-05: đề không có mô tả số (thuần chữ, đề tham số a) hoặc thiết diện động.
   */
  static String refusal(JsonNode figure) {
    if (figure == null || !figure.isObject()) {
      return VariantGenerator.NO_PARAMS;
    }
    if (figure.has("section")
        || figure.path("dynamicSection").asBoolean(false)
        || "dynamic_section".equals(figure.path("plane").path("shape").asString(""))) {
      return VariantGenerator.UNSUPPORTED;
    }
    if (!figure.path("plane").isObject() || !figure.path("plane").hasNonNull("shape")) {
      return VariantGenerator.NO_PARAMS;
    }
    return null;
  }
}
