package vn.geo.server.geometry.internal.solid;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import vn.geo.server.geometry.api.Figure;
import vn.geo.server.geometry.api.GeometryEngine;
import vn.geo.server.geometry.api.GeometryEngine.ProblemSpec;
import vn.geo.server.geometry.api.SceneBuildException;
import vn.geo.server.geometry.api.SceneDto;
import vn.geo.server.geometry.internal.plane.PlaneEngine;
import vn.geo.server.subject.api.SubjectEngine.Check;
import vn.geo.server.subject.api.SubjectEngine.Rendering;
import vn.geo.server.subject.api.SubjectEngine.Solution;

/**
 * Bọc engine hình không gian của MVP sau khung môn học (US-S2E2-01-SC2, BR-S2E2-03): mọi tính toán
 * đi thẳng vào {@link GeometryEngine} như trước, không đổi kết quả. Mô tả dựng lấy từ {@code
 * data.problemCode} (đề kho, problem-specs.json) hoặc từ chính {@code data} ({template, params,
 * …}).
 */
@Component
public class SolidAdapter {

  private final GeometryEngine engine;

  public SolidAdapter(GeometryEngine engine) {
    this.engine = engine;
  }

  /** Mô tả dựng của đề không gian; không có: null. */
  public ProblemSpec spec(JsonNode data) {
    if (data == null) {
      return null;
    }
    if (data.hasNonNull("problemCode")) {
      return engine.catalogSpec(data.path("problemCode").asString()).orElse(null);
    }
    if (data.hasNonNull("template")) {
      return engine.specFrom(data);
    }
    return null;
  }

  public Solution solve(JsonNode data) {
    ProblemSpec spec = spec(data);
    if (spec == null || spec.asked().isEmpty()) {
      return new Solution(
          false, null, null, GeometryEngine.LABEL_NEEDS_REVIEW, "chưa có mô tả dựng không gian");
    }
    try {
      Figure f = engine.build(spec, 1);
      GeometryEngine.AskedQuantity q = spec.asked().get(0);
      double v = engine.measure(f, q);
      String unit = q.kind().startsWith("angle") ? "angle" : "number";
      return new Solution(
          true, PlaneEngine.format(v, unit), v, GeometryEngine.LABEL_VERIFIED, null);
    } catch (SceneBuildException e) {
      return new Solution(false, null, null, GeometryEngine.LABEL_NEEDS_REVIEW, e.getMessage());
    }
  }

  /** Kiểm chứng như MVP: cùng {@link GeometryEngine#verify}, cùng nhãn. */
  public Check verify(JsonNode data, String answer) {
    ProblemSpec spec = spec(data);
    if (spec == null) {
      return new Check(
          false, false, GeometryEngine.LABEL_NEEDS_REVIEW, "chưa có mô tả dựng không gian");
    }
    GeometryEngine.Verification v = engine.verify(spec, answer);
    return new Check(v.sceneOk(), v.answerOk(), v.label(), null);
  }

  /** Cảnh 3D theo hợp đồng v1 của MVP (SCENE-CONTRACT.md), không đổi. */
  public Rendering render(JsonNode data) {
    ProblemSpec spec = spec(data);
    if (spec == null) {
      return new Rendering(
          false, null, GeometryEngine.LABEL_NEEDS_REVIEW, "chưa có mô tả dựng không gian");
    }
    try {
      Figure f = engine.build(spec, 1);
      SceneDto scene =
          engine.toScene(
              f,
              new GeometryEngine.SceneMeta(
                  null, "engine", spec.unit(), spec.chips(), spec.hidden(), "verified"));
      return new Rendering(true, scene, GeometryEngine.LABEL_VERIFIED, null);
    } catch (SceneBuildException e) {
      return new Rendering(false, null, GeometryEngine.LABEL_NEEDS_REVIEW, e.getMessage());
    }
  }

  /** Gợi ý chung cho đề không gian (không chứa đáp số). */
  public List<String> hints() {
    return List.of(
        "Vẽ hình, đánh dấu các dữ kiện đề cho.",
        "Tìm hình chiếu hoặc mặt phẳng phụ đưa bài toán về tam giác phẳng.");
  }

  /** Tham số số của mô tả dựng tại a = 1 (dùng cho kiểm thử so kết quả). */
  public Map<String, Double> params(ProblemSpec spec) {
    return spec.paramsAt(1);
  }
}
