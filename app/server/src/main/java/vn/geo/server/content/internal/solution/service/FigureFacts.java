package vn.geo.server.content.internal.solution.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.geometry.api.Figure;
import vn.geo.server.geometry.api.GeometryEngine;
import vn.geo.server.subject.api.ProblemKind;
import vn.geo.server.subject.api.SubjectEngine;
import vn.geo.server.subject.api.SubjectRegistry;

/**
 * Ngữ cảnh engine của một đề cho StepCheck (S2-E5): hình engine dựng với a = 1, dữ kiện đề cho và
 * đáp số engine tính lại. {@code figure} của sổ lời giải: {@code {"catalogCode": "HKN-02"}} (đề
 * không gian có mô tả dựng trong geometry/problem-specs.json) hoặc {@code {"plane": {…}}} (mô tả
 * dựng hình phẳng, SCENE-CONTRACT-2D.md §2). Không có hoặc không dựng được: máy không có đáp số đối
 * chiếu (nhãn máy giữ "Cần xem lại").
 */
@Component
public class FigureFacts {

  private final GeometryEngine geometry;
  private final SubjectRegistry subjects;
  private final JsonMapper mapper;

  public FigureFacts(GeometryEngine geometry, SubjectRegistry subjects, JsonMapper mapper) {
    this.geometry = geometry;
    this.subjects = subjects;
    this.mapper = mapper;
  }

  /** Ngữ cảnh theo hợp đồng của bean {@code stepCheckEngine}. */
  public Map<String, Object> context(String statement, String figureJson) {
    Map<String, Object> ctx = new LinkedHashMap<>();
    List<String> givens = new ArrayList<>();
    givens.add(statement);
    ctx.put("givens", givens);
    ctx.put("points", new HashMap<String, double[]>());
    if (figureJson == null) {
      return ctx;
    }
    try {
      JsonNode fig = mapper.readTree(figureJson);
      if (fig.hasNonNull("catalogCode")) {
        space(fig.path("catalogCode").asString(), ctx, givens);
      } else if (fig.path("plane").isObject()) {
        plane(statement, fig.path("plane"), ctx);
      }
    } catch (RuntimeException e) {
      // Hình không dựng được: không có đáp số đối chiếu, không lỗi hệ thống.
      ctx.remove("askedValue");
    }
    return ctx;
  }

  /**
   * Cảnh 3D của đề cho thẻ hình (hợp đồng cảnh v1: không chứa giá trị tính hay đáp số); null khi sổ
   * không gắn đề không gian dựng được.
   */
  public Object scene(String figureJson, String machineStatus) {
    if (figureJson == null) {
      return null;
    }
    try {
      JsonNode fig = mapper.readTree(figureJson);
      if (!fig.hasNonNull("catalogCode")) {
        return null;
      }
      GeometryEngine.ProblemSpec spec =
          geometry.catalogSpec(fig.path("catalogCode").asString()).orElse(null);
      if (spec == null) {
        return null;
      }
      Figure figure = geometry.build(spec.template(), spec.paramsAt(1.0));
      return geometry.toScene(
          figure,
          new GeometryEngine.SceneMeta(
              null, "engine", spec.unit(), spec.chips(), spec.hidden(), machineStatus));
    } catch (RuntimeException e) {
      return null;
    }
  }

  private void space(String code, Map<String, Object> ctx, List<String> givens) {
    GeometryEngine.ProblemSpec spec = geometry.catalogSpec(code).orElse(null);
    if (spec == null) {
      return;
    }
    Figure figure = geometry.build(spec.template(), spec.paramsAt(1.0));
    ctx.put("points", new HashMap<>(figure.points()));
    if (spec.chips() != null) {
      spec.chips().forEach(c -> givens.add(c.text()));
    }
    if (spec.asked() != null && !spec.asked().isEmpty()) {
      GeometryEngine.AskedQuantity asked = spec.asked().get(0);
      ctx.put("askedValue", geometry.measure(figure, asked));
      ctx.put("askedIsAngle", asked.kind().startsWith("angle"));
    }
  }

  private void plane(String statement, JsonNode spec, Map<String, Object> ctx) {
    SubjectEngine engine = subjects.forKind(ProblemKind.PLANE);
    SubjectEngine.Solution s =
        engine.solve(engine.parse(new SubjectEngine.ProblemInput(statement, spec)));
    if (s.solved() && s.value() != null) {
      ctx.put("askedValue", s.value());
      ctx.put("askedIsAngle", "angle".equals(spec.path("asked").path("kind").asString("")));
    }
  }
}
