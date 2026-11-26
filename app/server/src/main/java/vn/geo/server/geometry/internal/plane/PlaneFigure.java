package vn.geo.server.geometry.internal.plane;

import java.util.List;
import java.util.Map;

/**
 * Hình phẳng đã dựng: tọa độ đỉnh, đoạn vẽ, đường tròn, ràng buộc (đề cho + ngầm định theo dạng),
 * nguồn tọa độ ("engine" hoặc "ai").
 */
public record PlaneFigure(
    Map<String, double[]> points,
    List<List<String>> segments,
    List<Circle> circles,
    List<Rule> rules,
    String coordSource) {

  /** Đường tròn tâm {@code center} bán kính {@code r}. */
  public record Circle(String center, double r) {}

  /**
   * Ràng buộc kiểm trên tọa độ. type: length [P,Q] = value · angle [P,O,Q] = value (độ) · equal
   * [P,Q,R,S] (PQ = RS) · parallel [P,Q,R,S] (PQ ∥ RS) · perpendicular [P,Q,R,S] · on_circle [O,
   * X…] (mọi X cách O đúng bán kính của đường tròn tâm O). {@code value} null với ràng buộc không
   * số.
   */
  public record Rule(String type, List<String> of, Double value) {}

  public double[] p(String id) {
    double[] v = points.get(id);
    if (v == null) {
      throw new PlaneBuildException("dữ kiện nhắc điểm " + id + " không có trên hình");
    }
    return v;
  }
}
