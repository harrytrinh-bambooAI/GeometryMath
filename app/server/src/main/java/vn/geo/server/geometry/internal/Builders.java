package vn.geo.server.geometry.internal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import vn.geo.server.geometry.api.Constraint;
import vn.geo.server.geometry.api.Figure;
import vn.geo.server.geometry.api.SceneBuildException;
import vn.geo.server.geometry.api.SceneBuildException.Reason;
import vn.geo.server.geometry.api.SceneDto.SceneSurface;

/**
 * Bộ dựng của engine (BR-GEO-128; bảng ở SCENE-CONTRACT.md §3). Mỗi bộ dựng là hàm thuần từ tham số
 * ra tọa độ + ràng buộc. A tại gốc, AB theo trục x, AD (hoặc AC) theo trục y, cạnh bên theo z.
 */
final class Builders {

  /** Khai báo một bộ dựng: loại khối, tham số theo thứ tự, hàm dựng. */
  record Spec(String solidKind, List<String> params, Function<Map<String, Double>, Figure> fn) {}

  static final Map<String, Spec> ALL = new LinkedHashMap<>();

  static {
    ALL.put("pyramid", new Spec("pyramid", List.of("a", "h"), Builders::pyramid));
    ALL.put("pyramid_square_sa", new Spec("pyramid", List.of("a", "sa"), Builders::pyramidSa));
    ALL.put("prism", new Spec("prism", List.of("a", "h"), Builders::prism));
    ALL.put("prism_tri_right", new Spec("prism", List.of("ab", "ac", "h"), Builders::prismRight));
    ALL.put(
        "prism_tri_sides", new Spec("prism", List.of("ab", "bc", "ca", "h"), Builders::prismSides));
    ALL.put("box", new Spec("box", List.of("a", "b", "c"), Builders::box));
    ALL.put("cube", new Spec("cube", List.of("a"), p -> cube(p, "cube")));
    ALL.put("cone", new Spec("cone", List.of("r", "h"), Builders::cone));
    ALL.put("cone_rl", new Spec("cone", List.of("r", "l"), Builders::coneRl));
    ALL.put("cylinder", new Spec("cylinder", List.of("r", "h"), Builders::cylinder));
    ALL.put("sphere", new Spec("sphere", List.of("r"), Builders::sphere));
    ALL.put("sphere_circum_cube", new Spec("sphere", List.of("a"), Builders::sphereCube));
  }

  private Builders() {}

  /** Dựng; kiểm tham số có đủ, hữu hạn và lớn hơn 0 (hình suy biến khi bằng 0, US-GEO-201-FC3). */
  static Figure build(String template, Map<String, Double> params) {
    Spec s = ALL.get(template);
    if (s == null) {
      throw new SceneBuildException(Reason.UNKNOWN_TEMPLATE, "bộ dựng không biết: " + template);
    }
    Map<String, Double> p = new LinkedHashMap<>();
    for (String k : s.params()) {
      Double v = params == null ? null : params.get(k);
      if (v == null || !Double.isFinite(v)) {
        throw new SceneBuildException(Reason.DEGENERATE, "thiếu tham số " + k);
      }
      if (v <= 0) {
        throw new SceneBuildException(Reason.DEGENERATE, "tham số " + k + " phải lớn hơn 0");
      }
      p.put(k, v);
    }
    return s.fn().apply(p);
  }

  // ------------------------------------------------------------------ bộ khung

  private static final class F {
    final String template;
    final String kind;
    final Map<String, Double> params;
    final LinkedHashMap<String, double[]> pts = new LinkedHashMap<>();
    final List<List<String>> edges = new ArrayList<>();
    final List<List<String>> faces = new ArrayList<>();
    final List<SceneSurface> surfaces = new ArrayList<>();
    final List<Constraint> cs = new ArrayList<>();

    F(String template, String kind, Map<String, Double> params) {
      this.template = template;
      this.kind = kind;
      this.params = params;
    }

    F pt(String n, double x, double y, double z) {
      pts.put(n, V.of(x, y, z));
      return this;
    }

    F edges(String... pairs) {
      for (String e : pairs) {
        String[] ab = e.split("-");
        edges.add(List.of(ab[0], ab[1]));
      }
      return this;
    }

    F face(String... names) {
      faces.add(List.of(names));
      return this;
    }

    F len(String p, String q, double v) {
      cs.add(Constraint.len(p, q, v));
      return this;
    }

    F perp(String p, String q, String r, String s) {
      cs.add(Constraint.perp(p, q, r, s));
      return this;
    }

    F eq(String p, String q, String r, String s) {
      cs.add(Constraint.eq(p, q, r, s));
      return this;
    }

    Figure done() {
      return new Figure(
          template,
          kind,
          new LinkedHashMap<>(params),
          pts,
          List.copyOf(edges),
          List.copyOf(faces),
          List.copyOf(surfaces),
          List.copyOf(cs));
    }
  }

  private static List<Double> l(double x, double y, double z) {
    return List.of(x, y, z);
  }

  // ------------------------------------------------------------------ chóp

  private static F squareBase(F f, double a) {
    return f.pt("A", 0, 0, 0)
        .pt("B", a, 0, 0)
        .pt("C", a, a, 0)
        .pt("D", 0, a, 0)
        .edges("A-B", "B-C", "C-D", "D-A")
        .face("A", "B", "C", "D")
        .len("A", "B", a)
        .len("A", "D", a)
        .eq("A", "B", "D", "C")
        .eq("A", "D", "B", "C")
        .perp("A", "B", "A", "D");
  }

  private static F lateralFaces(F f, String apex) {
    return f.edges(apex + "-A", apex + "-B", apex + "-C", apex + "-D")
        .face(apex, "A", "B")
        .face(apex, "B", "C")
        .face(apex, "C", "D")
        .face(apex, "D", "A");
  }

  /** Chóp tứ giác đều cạnh đáy a, chiều cao h (thẻ "Hình chóp"). */
  static Figure pyramid(Map<String, Double> p) {
    double a = p.get("a");
    double h = p.get("h");
    F f = new F("pyramid", "pyramid", p);
    f.pt("S", a / 2, a / 2, h);
    squareBase(f, a);
    f.pt("O", a / 2, a / 2, 0);
    lateralFaces(f, "S")
        .edges("S-O")
        .len("S", "O", h)
        .perp("S", "O", "A", "B")
        .perp("S", "O", "A", "D")
        .eq("S", "A", "S", "B")
        .eq("S", "A", "S", "C")
        .eq("S", "A", "S", "D");
    return f.done();
  }

  /** Chóp đáy vuông cạnh a, SA ⊥ đáy, SA = sa (HKG-01). */
  static Figure pyramidSa(Map<String, Double> p) {
    double a = p.get("a");
    double sa = p.get("sa");
    F f = new F("pyramid_square_sa", "pyramid", p);
    f.pt("S", 0, 0, sa);
    squareBase(f, a);
    lateralFaces(f, "S").len("S", "A", sa).perp("S", "A", "A", "B").perp("S", "A", "A", "D");
    return f.done();
  }

  // ------------------------------------------------------------------ lăng trụ đứng tam giác

  private static Figure triPrism(
      String template, Map<String, Double> p, double[] c, double ab, double h) {
    F f = new F(template, "prism", p);
    f.pt("A", 0, 0, 0).pt("B", ab, 0, 0).pt("C", c[0], c[1], 0);
    f.pt("A′", 0, 0, h).pt("B′", ab, 0, h).pt("C′", c[0], c[1], h);
    f.edges("A-B", "B-C", "C-A", "A′-B′", "B′-C′", "C′-A′", "A-A′", "B-B′", "C-C′")
        .face("A", "B", "C")
        .face("A′", "B′", "C′")
        .face("A", "B", "B′", "A′")
        .face("B", "C", "C′", "B′")
        .face("C", "A", "A′", "C′");
    f.len("A", "A′", h).len("B", "B′", h).len("C", "C′", h);
    f.perp("A", "A′", "A", "B").perp("A", "A′", "A", "C");
    f.perp("B", "B′", "B", "C").perp("C", "C′", "C", "A");
    f.eq("A", "B", "A′", "B′").eq("B", "C", "B′", "C′").eq("C", "A", "C′", "A′");
    f.len("A", "B", ab);
    return f.done();
  }

  /** Lăng trụ đứng đáy tam giác đều cạnh a, chiều cao h (thẻ "Lăng trụ", HKG-03). */
  static Figure prism(Map<String, Double> p) {
    double a = p.get("a");
    double h = p.get("h");
    Figure base = triPrism("prism", p, V.of(a / 2, a * Math.sqrt(3) / 2, 0), a, h);
    return withExtra(base, Constraint.len("B", "C", a), Constraint.len("C", "A", a));
  }

  /** Lăng trụ đứng đáy tam giác vuông tại A (F-GEO-060). */
  static Figure prismRight(Map<String, Double> p) {
    double ab = p.get("ab");
    double ac = p.get("ac");
    Figure base = triPrism("prism_tri_right", p, V.of(0, ac, 0), ab, p.get("h"));
    return withExtra(base, Constraint.len("A", "C", ac), Constraint.perp("A", "B", "A", "C"));
  }

  /** Lăng trụ đứng đáy tam giác cho ba cạnh; bất đẳng thức tam giác sai ⇒ mâu thuẫn (F-GEO-074). */
  static Figure prismSides(Map<String, Double> p) {
    double ab = p.get("ab");
    double bc = p.get("bc");
    double ca = p.get("ca");
    double max = Math.max(ab, Math.max(bc, ca));
    if (ab + bc + ca - max <= max * (1 + 1e-9)) {
      throw new SceneBuildException(
          Reason.CONTRADICTORY, "ba cạnh đáy không tạo thành tam giác (vi phạm bất đẳng thức)");
    }
    double x = (ab * ab + ca * ca - bc * bc) / (2 * ab);
    double y = Math.sqrt(ca * ca - x * x);
    Figure base = triPrism("prism_tri_sides", p, V.of(x, y, 0), ab, p.get("h"));
    return withExtra(base, Constraint.len("B", "C", bc), Constraint.len("C", "A", ca));
  }

  private static Figure withExtra(Figure f, Constraint... extra) {
    List<Constraint> cs = new ArrayList<>(f.constraints());
    cs.addAll(List.of(extra));
    return new Figure(
        f.template(),
        f.solidKind(),
        f.params(),
        f.points(),
        f.edges(),
        f.faces(),
        f.surfaces(),
        List.copyOf(cs));
  }

  // ------------------------------------------------------------------ hộp, lập phương

  private static F boxF(
      String template, String kind, Map<String, Double> p, double a, double b, double c) {
    F f = new F(template, kind, p);
    f.pt("A", 0, 0, 0).pt("B", a, 0, 0).pt("C", a, b, 0).pt("D", 0, b, 0);
    f.pt("A′", 0, 0, c).pt("B′", a, 0, c).pt("C′", a, b, c).pt("D′", 0, b, c);
    f.edges(
            "A-B", "B-C", "C-D", "D-A", "A′-B′", "B′-C′", "C′-D′", "D′-A′", "A-A′", "B-B′", "C-C′",
            "D-D′")
        .face("A", "B", "C", "D")
        .face("A′", "B′", "C′", "D′")
        .face("A", "B", "B′", "A′")
        .face("B", "C", "C′", "B′")
        .face("C", "D", "D′", "C′")
        .face("D", "A", "A′", "D′");
    f.len("A", "B", a).len("A", "D", b).len("A", "A′", c);
    f.perp("A", "B", "A", "D").perp("A", "A′", "A", "B").perp("A", "A′", "A", "D");
    f.eq("A", "B", "D", "C").eq("A", "D", "B", "C");
    f.eq("A", "C", "A′", "C′").eq("B", "D", "B′", "D′");
    f.len("B", "B′", c).len("C", "C′", c).len("D", "D′", c);
    return f;
  }

  static Figure box(Map<String, Double> p) {
    return boxF("box", "box", p, p.get("a"), p.get("b"), p.get("c")).done();
  }

  static Figure cube(Map<String, Double> p, String template) {
    double a = p.get("a");
    return boxF(template, "cube", p, a, a, a).done();
  }

  // ------------------------------------------------------------------ nón, trụ, cầu

  /** Nón bán kính r, chiều cao h: S đỉnh, O tâm đáy, M trên đường tròn đáy. */
  static Figure cone(Map<String, Double> p) {
    return coneF("cone", p, p.get("r"), p.get("h"));
  }

  /** Nón cho bán kính r và đường sinh l (HKG-04): h = √(l² − r²), l ≤ r ⇒ mâu thuẫn. */
  static Figure coneRl(Map<String, Double> p) {
    double r = p.get("r");
    double ll = p.get("l");
    if (ll <= r * (1 + 1e-9)) {
      throw new SceneBuildException(Reason.CONTRADICTORY, "đường sinh phải dài hơn bán kính đáy");
    }
    Figure f = coneF("cone_rl", p, r, Math.sqrt(ll * ll - r * r));
    return withExtra(f, Constraint.len("S", "M", ll));
  }

  private static Figure coneF(String template, Map<String, Double> p, double r, double h) {
    F f = new F(template, "cone", p);
    f.pt("S", 0, 0, h).pt("O", 0, 0, 0).pt("M", r, 0, 0);
    f.edges("S-O", "O-M", "S-M");
    f.surfaces.add(new SceneSurface("cone", l(0, 0, 0), r, l(0, 0, 1), l(0, 0, h), null));
    f.len("O", "M", r).len("S", "O", h).perp("S", "O", "O", "M");
    return f.done();
  }

  /** Trụ bán kính r, chiều cao h: O, O′ tâm hai đáy; M, M′ trên hai đường tròn đáy. */
  static Figure cylinder(Map<String, Double> p) {
    double r = p.get("r");
    double h = p.get("h");
    F f = new F("cylinder", "cylinder", p);
    f.pt("O", 0, 0, 0).pt("O′", 0, 0, h).pt("M", r, 0, 0).pt("M′", r, 0, h);
    f.edges("O-O′", "O-M", "O′-M′", "M-M′");
    f.surfaces.add(new SceneSurface("cylinder", l(0, 0, 0), r, l(0, 0, 1), null, h));
    f.len("O", "M", r).len("O", "O′", h).perp("O", "O′", "O", "M");
    f.len("O′", "M′", r).len("M", "M′", h);
    return f.done();
  }

  /** Cầu bán kính r: O tâm, M trên mặt cầu. */
  static Figure sphere(Map<String, Double> p) {
    double r = p.get("r");
    F f = new F("sphere", "sphere", p);
    f.pt("O", 0, 0, 0).pt("M", r, 0, 0);
    f.edges("O-M");
    f.surfaces.add(new SceneSurface("sphere", l(0, 0, 0), r, null, null, null));
    f.len("O", "M", r);
    return f.done();
  }

  /** Mặt cầu ngoại tiếp hình lập phương cạnh a (HKG-06): I tâm, bán kính a√3/2. */
  static Figure sphereCube(Map<String, Double> p) {
    double a = p.get("a");
    F f = boxF("sphere_circum_cube", "sphere", p, a, a, a);
    f.pt("I", a / 2, a / 2, a / 2);
    f.surfaces.add(
        new SceneSurface("sphere", l(a / 2, a / 2, a / 2), a * Math.sqrt(3) / 2, null, null, null));
    f.eq("I", "A", "I", "C′")
        .eq("I", "B", "I", "D′")
        .eq("I", "C", "I", "A′")
        .eq("I", "D", "I", "B′");
    f.eq("I", "A", "I", "B").eq("I", "A", "I", "D");
    return f.done();
  }
}
