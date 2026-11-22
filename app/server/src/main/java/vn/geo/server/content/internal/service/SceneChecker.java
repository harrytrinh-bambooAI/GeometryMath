package vn.geo.server.content.internal.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Kiểm tọa độ đỉnh do AI trả có thỏa ràng buộc mô tả hình của đề không (M-GEO-01). Ràng buộc ở
 * {@code content/scene-specs.json}. Thiếu đỉnh hoặc đề không có ràng buộc: không thỏa.
 */
@Component
public class SceneChecker {

  static final double LEN_REL = 1e-6;
  static final double PERP = 1e-6 + 1e-12;

  private final Map<String, JsonNode> specs = new HashMap<>();

  public SceneChecker(JsonMapper mapper) {
    try (InputStream in = SceneChecker.class.getResourceAsStream("/content/scene-specs.json")) {
      JsonNode root = mapper.readTree(in);
      for (var e : root.properties()) {
        if (e.getValue().isArray()) {
          specs.put(e.getKey(), e.getValue());
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException("không đọc được content/scene-specs.json", e);
    }
  }

  public boolean check(String code, JsonNode points) {
    JsonNode spec = specs.get(code);
    if (spec == null || points == null || !points.isObject()) {
      return false;
    }
    for (JsonNode c : spec) {
      String kind = c.get(0).asString();
      double[] p = pt(points, c.get(1).asString());
      double[] q = pt(points, c.get(2).asString());
      if (p == null || q == null) {
        return false;
      }
      boolean ok =
          switch (kind) {
            case "len" -> {
              double want = c.get(3).asDouble();
              yield Math.abs(norm(sub(q, p)) - want) <= LEN_REL * Math.max(1, want);
            }
            case "perp", "eq" -> {
              double[] r = pt(points, c.get(3).asString());
              double[] s = pt(points, c.get(4).asString());
              if (r == null || s == null) {
                yield false;
              }
              double[] u = sub(q, p);
              double[] v = sub(s, r);
              if ("perp".equals(kind)) {
                double nu = norm(u);
                double nv = norm(v);
                yield nu > 0 && nv > 0 && Math.abs(dot(u, v)) / (nu * nv) <= PERP;
              }
              double a = norm(u);
              double b = norm(v);
              yield a > 0 && Math.abs(a - b) <= LEN_REL * Math.max(1, Math.max(a, b));
            }
            default -> false;
          };
      if (!ok) {
        return false;
      }
    }
    return true;
  }

  private static double[] pt(JsonNode points, String name) {
    JsonNode a = points.get(name);
    if (a == null || !a.isArray() || a.size() != 3) {
      return null;
    }
    return new double[] {a.get(0).asDouble(), a.get(1).asDouble(), a.get(2).asDouble()};
  }

  private static double[] sub(double[] a, double[] b) {
    return new double[] {a[0] - b[0], a[1] - b[1], a[2] - b[2]};
  }

  private static double dot(double[] a, double[] b) {
    return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
  }

  private static double norm(double[] a) {
    return Math.sqrt(dot(a, a));
  }
}
