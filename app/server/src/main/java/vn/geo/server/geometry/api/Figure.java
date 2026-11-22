package vn.geo.server.geometry.api;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Hình do engine (hoặc AI, với đề lạ) dựng: đỉnh, cạnh, mặt, mặt cong và ràng buộc phải thỏa.
 *
 * @param template mã bộ dựng (rỗng khi tọa độ AI)
 * @param solidKind loại khối (rỗng khi hình lạ)
 * @param params tham số đã dùng
 * @param points tọa độ đỉnh theo thứ tự khai báo, z hướng lên
 * @param edges cạnh [P, Q]
 * @param faces mặt phẳng (thứ tự vòng)
 * @param surfaces mặt cong
 * @param constraints ràng buộc của hình (BR-GEO-127)
 */
public record Figure(
    String template,
    String solidKind,
    Map<String, Double> params,
    LinkedHashMap<String, double[]> points,
    List<List<String>> edges,
    List<List<String>> faces,
    List<SceneDto.SceneSurface> surfaces,
    List<Constraint> constraints) {

  public double[] point(String name) {
    double[] p = points.get(name);
    if (p == null) {
      throw new IllegalArgumentException("không có đỉnh " + name);
    }
    return p;
  }
}
