package vn.geo.server.geometry.internal.plane;

import java.util.List;

/**
 * {@code SceneSpec} 2D (app/docs/SCENE-CONTRACT-2D.md, hợp đồng "scene2d-v1"): chỉ điểm, nhãn, đoạn
 * vẽ, đường tròn, ràng buộc đề cho và tọa độ. Không có đáp số, bước giải hay gợi ý (BR-S2E2-07).
 * Thứ tự trường và phần tử cố định nên dựng hai lần cho chuỗi JSON giống hệt (BR-S2E2-08).
 */
public record PlaneScene(
    String contract,
    String kind,
    String coordSource,
    List<Point> points,
    List<List<String>> segments,
    List<Circle> circles,
    List<Constraint> constraints) {

  public static final String CONTRACT = "scene2d-v1";

  /** Điểm: mã, nhãn hiện, tọa độ (đơn vị của đề, trục y hướng lên). */
  public record Point(String id, String label, double x, double y) {}

  /** Đường tròn tâm {@code center} bán kính {@code r}. */
  public record Circle(String center, double r) {}

  /** Ràng buộc đề cho (value null với ràng buộc không số: equal, parallel, on_circle…). */
  public record Constraint(String type, List<String> of, Double value) {}
}
