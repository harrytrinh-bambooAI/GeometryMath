package vn.geo.server.it.mvp;

import java.util.LinkedHashMap;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import vn.geo.server.content.internal.service.SceneChecker;
import vn.geo.server.geometry.api.Figure;
import vn.geo.server.geometry.api.GeometryEngine;

/**
 * Chỉ số "hình thỏa ràng buộc" của bộ đo MVP (MVP.md §9) cho tọa độ AI trả ở task "solution": đề có
 * trong {@code content/scene-specs.json} kiểm bằng SceneChecker, đề khác bằng ràng buộc của bộ dựng
 * trong engine (tham số tại a = 1). Thiếu đỉnh hoặc không có ràng buộc: không đạt. Dùng chung cho
 * {@link MvpBench} và test tái hiện T-05 ({@code RoundSolidSceneT05Test}).
 */
final class BenchSceneCheck {

  private final SceneChecker scenes;
  private final GeometryEngine engine;

  BenchSceneCheck(SceneChecker scenes, GeometryEngine engine) {
    this.scenes = scenes;
    this.engine = engine;
  }

  boolean ok(String code, boolean hasSceneSpec, JsonNode points) {
    if (points == null || !points.isObject()) {
      return false;
    }
    if (hasSceneSpec) {
      return scenes.check(code, points);
    }
    var spec = engine.catalogSpec(code);
    if (spec.isEmpty()) {
      return false; // không có ràng buộc để kiểm: không tính là đạt
    }
    try {
      Figure f = engine.build(spec.get().template(), spec.get().paramsAt(1));
      Map<String, double[]> pts = new LinkedHashMap<>();
      for (var e : points.properties()) {
        JsonNode c = e.getValue();
        if (c.isArray() && c.size() == 3) {
          pts.put(
              e.getKey(),
              new double[] {c.get(0).asDouble(), c.get(1).asDouble(), c.get(2).asDouble()});
        }
      }
      return engine.check(pts, f.constraints()).satisfied();
    } catch (RuntimeException e) {
      return false;
    }
  }
}
