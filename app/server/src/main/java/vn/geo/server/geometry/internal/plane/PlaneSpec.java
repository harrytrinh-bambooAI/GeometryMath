package vn.geo.server.geometry.internal.plane;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import tools.jackson.databind.JsonNode;

/**
 * Mô tả dựng hình phẳng (SCENE-CONTRACT-2D.md §2): dạng hình, nhãn đỉnh, dữ kiện đề cho, đại lượng
 * hỏi, tọa độ do AI dựng (nếu có).
 *
 * @param shape dạng (BR-S2E2-05): triangle, isosceles_triangle, equilateral_triangle,
 *     right_triangle, rectangle, square, parallelogram, trapezoid, circle_chord, circle_tangent
 * @param at đỉnh cân / đỉnh vuông (tam giác), đỉnh vuông của hình thang vuông
 * @param isosceles hình thang cân
 * @param given dữ kiện đề cho (ràng buộc có giá trị)
 * @param asked đại lượng hỏi (null: đề không hỏi số)
 * @param coords tọa độ AI dựng (đề lạ): engine không dựng lại, nhãn "Cần xem lại"
 */
public record PlaneSpec(
    String shape,
    List<String> points,
    String at,
    boolean isosceles,
    List<Given> given,
    Asked asked,
    Map<String, double[]> coords) {

  /** Dạng hỗ trợ ở chặng 2 (BR-S2E2-05, đã chốt PO-66 QA-S2E2-01). */
  public static final Set<String> TRIANGLES =
      Set.of("triangle", "isosceles_triangle", "equilateral_triangle", "right_triangle");

  public static final Set<String> QUADS =
      Set.of("rectangle", "square", "parallelogram", "trapezoid");

  public static final Set<String> CIRCLES = Set.of("circle_chord", "circle_tangent");

  /** Một dữ kiện: length [P,Q], angle [P,O,Q] (độ), radius [O], height [] (hình thang). */
  public record Given(String type, List<String> of, double value) {}

  /** Đại lượng hỏi: length [P,Q], angle [P,O,Q], area, perimeter, distance [P,A,B]. */
  public record Asked(String name, String kind, List<String> of) {}

  /** Nhóm dạng để đếm bộ đề (BR-S2E2-13): tam giác, tứ giác, đường tròn; khác: "khác". */
  public String form() {
    return form(shape);
  }

  public static String form(String shape) {
    if (TRIANGLES.contains(shape)) {
      return "tam giác";
    }
    if (QUADS.contains(shape)) {
      return "tứ giác";
    }
    if (CIRCLES.contains(shape)) {
      return "đường tròn";
    }
    return "khác";
  }

  public boolean supported() {
    return TRIANGLES.contains(shape) || QUADS.contains(shape) || CIRCLES.contains(shape);
  }

  /** Nhãn đỉnh mặc định theo dạng. */
  static List<String> defaultPoints(String shape) {
    if (TRIANGLES.contains(shape)) {
      return List.of("A", "B", "C");
    }
    if (QUADS.contains(shape)) {
      return List.of("A", "B", "C", "D");
    }
    if ("circle_chord".equals(shape)) {
      return List.of("O", "A", "B");
    }
    if ("circle_tangent".equals(shape)) {
      return List.of("O", "M", "T");
    }
    return List.of();
  }

  /**
   * Đọc từ JSON {shape, points?, at?, isosceles?, given[], asked?, coords?}.
   *
   * @throws IllegalArgumentException thiếu {@code shape} hoặc dữ kiện sai dạng
   */
  public static PlaneSpec from(JsonNode n) {
    if (n == null || !n.hasNonNull("shape")) {
      throw new IllegalArgumentException("thiếu dạng hình (shape)");
    }
    String shape = n.path("shape").asString().strip();
    List<String> points = strings(n.path("points"));
    if (points.isEmpty()) {
      points = defaultPoints(shape);
    }
    List<Given> given = new ArrayList<>();
    for (JsonNode g : n.path("given")) {
      if (!g.path("value").isNumber()) {
        throw new IllegalArgumentException("dữ kiện thiếu giá trị số");
      }
      given.add(
          new Given(
              g.path("type").asString(""), strings(g.path("of")), g.path("value").asDouble()));
    }
    Asked asked = null;
    JsonNode a = n.path("asked");
    if (a.isObject()) {
      asked =
          new Asked(
              a.path("name").asString(""), a.path("kind").asString(""), strings(a.path("of")));
    }
    Map<String, double[]> coords = null;
    if (n.path("coords").isObject()) {
      coords = new LinkedHashMap<>();
      for (var e : n.path("coords").properties()) {
        JsonNode v = e.getValue();
        coords.put(e.getKey(), new double[] {v.path(0).asDouble(), v.path(1).asDouble()});
      }
    }
    String at = n.path("at").asString(null);
    return new PlaneSpec(
        shape, points, at, n.path("isosceles").asBoolean(false), given, asked, coords);
  }

  private static List<String> strings(JsonNode arr) {
    List<String> out = new ArrayList<>();
    for (JsonNode x : arr) {
      out.add(x.asString());
    }
    return out;
  }

  /** Dữ kiện đầu tiên theo loại và điểm (thứ tự điểm không quan trọng với length). */
  Double length(String p, String q) {
    for (Given g : given) {
      if ("length".equals(g.type())
          && g.of().size() == 2
          && (g.of().equals(List.of(p, q)) || g.of().equals(List.of(q, p)))) {
        return g.value();
      }
    }
    return null;
  }

  /** Góc tại đỉnh {@code o} giữa hai đỉnh {@code p}, {@code q} (độ). */
  Double angle(String p, String o, String q) {
    for (Given g : given) {
      if ("angle".equals(g.type())
          && g.of().size() == 3
          && g.of().get(1).equals(o)
          && (List.of(g.of().get(0), g.of().get(2)).equals(List.of(p, q))
              || List.of(g.of().get(0), g.of().get(2)).equals(List.of(q, p)))) {
        return g.value();
      }
    }
    return null;
  }

  Double first(String type) {
    return given.stream()
        .filter(g -> type.equals(g.type()))
        .map(Given::value)
        .findFirst()
        .orElse(null);
  }
}
