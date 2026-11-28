package vn.geo.server.geometry.internal.plane;

import static vn.geo.server.geometry.internal.plane.PlaneBuildException.BAD_ANGLE;
import static vn.geo.server.geometry.internal.plane.PlaneBuildException.BAD_LENGTH;
import static vn.geo.server.geometry.internal.plane.PlaneBuildException.CONTRADICTION;
import static vn.geo.server.geometry.internal.plane.PlaneBuildException.UNDETERMINED;
import static vn.geo.server.geometry.internal.plane.TriangleBuilder.rule;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dựng tứ giác ABCD (BR-S2E2-05): chữ nhật, vuông, bình hành, hình thang (AB ∥ CD). A ở gốc, AB
 * trên trục hoành; bình hành/chữ nhật/vuông: D = AD·(cos A, sin A), C = B + D; hình thang: C = D +
 * (CD, 0).
 */
final class QuadBuilder {

  private QuadBuilder() {}

  static PlaneFigure build(PlaneSpec s) {
    List<String> v = s.points();
    if (v.size() != 4) {
      throw new PlaneBuildException(UNDETERMINED);
    }
    String a = v.get(0);
    String b = v.get(1);
    String c = v.get(2);
    String d = v.get(3);
    for (PlaneSpec.Given g : s.given()) {
      if (("length".equals(g.type()) || "height".equals(g.type())) && !(g.value() > 0)) {
        throw new PlaneBuildException(BAD_LENGTH);
      }
      if ("angle".equals(g.type()) && !(g.value() > 0 && g.value() < 180)) {
        throw new PlaneBuildException(BAD_ANGLE);
      }
    }
    Double ab = either(s.length(a, b), "trapezoid".equals(s.shape()) ? null : s.length(c, d));
    Double ad = either(s.length(a, d), "trapezoid".equals(s.shape()) ? null : s.length(b, c));
    Double theta = s.angle(d, a, b);
    Double angB = s.angle(a, b, c);
    Double diagAc = s.length(a, c);
    Double diagBd = s.length(b, d);
    List<PlaneFigure.Rule> rules = new ArrayList<>();
    Map<String, double[]> pts = new LinkedHashMap<>();
    double t;
    switch (s.shape()) {
      case "square" -> {
        Double side = either(either(ab, ad), either(s.length(b, c), s.length(c, d)));
        Double diag = either(diagAc, diagBd);
        if (side == null && diag != null) {
          side = diag / Math.sqrt(2);
        }
        if (side == null) {
          throw new PlaneBuildException(UNDETERMINED);
        }
        ab = side;
        ad = side;
        t = Math.PI / 2;
        rules.add(rule("equal", a, b, b, c));
        rules.add(rule("equal", b, c, c, d));
        rules.add(rule("equal", c, d, d, a));
        rightAngles(rules, v);
      }
      case "rectangle" -> {
        Double diag = either(diagAc, diagBd);
        if (ab == null && ad != null && diag != null) {
          ab = leg(diag, ad);
        } else if (ad == null && ab != null && diag != null) {
          ad = leg(diag, ab);
        }
        if (ab == null || ad == null) {
          throw new PlaneBuildException(UNDETERMINED);
        }
        t = Math.PI / 2;
        rightAngles(rules, v);
      }
      case "parallelogram" -> {
        if (theta == null && angB != null) {
          theta = 180 - angB;
        }
        if (theta == null && ab != null && ad != null && (diagAc != null || diagBd != null)) {
          double cos =
              diagAc != null
                  ? (diagAc * diagAc - ab * ab - ad * ad) / (2 * ab * ad)
                  : (ab * ab + ad * ad - diagBd * diagBd) / (2 * ab * ad);
          if (Math.abs(cos) >= 1) {
            throw new PlaneBuildException(CONTRADICTION);
          }
          theta = Math.toDegrees(Math.acos(cos));
        }
        if (ab == null || ad == null || theta == null) {
          throw new PlaneBuildException(UNDETERMINED);
        }
        t = Math.toRadians(theta);
        rules.add(rule("parallel", a, b, d, c));
        rules.add(rule("parallel", a, d, b, c));
      }
      case "trapezoid" -> {
        return trapezoid(s, v, ab, ad, theta);
      }
      default -> throw new PlaneBuildException(PlaneBuildException.UNSUPPORTED);
    }
    pts.put(a, new double[] {0, 0});
    pts.put(b, new double[] {ab, 0});
    pts.put(c, new double[] {ab + ad * Math.cos(t), ad * Math.sin(t)});
    pts.put(d, new double[] {ad * Math.cos(t), ad * Math.sin(t)});
    return figure(s, v, pts, rules);
  }

  private static PlaneFigure trapezoid(
      PlaneSpec s, List<String> v, Double ab, Double ad, Double theta) {
    String a = v.get(0);
    String b = v.get(1);
    String c = v.get(2);
    String d = v.get(3);
    Double cd = s.length(c, d);
    Double h = s.first("height");
    if (ab == null || cd == null) {
      throw new PlaneBuildException(UNDETERMINED);
    }
    double dx;
    double dy;
    List<PlaneFigure.Rule> rules = new ArrayList<>();
    if (a.equals(s.at())) {
      dy = either(ad, h) == null ? Double.NaN : either(ad, h);
      dx = 0;
      rules.add(new PlaneFigure.Rule("angle", List.of(d, a, b), 90.0));
    } else if (s.isosceles()) {
      double off = (ab - cd) / 2;
      if (h == null && ad != null) {
        if (ad <= Math.abs(off)) {
          throw new PlaneBuildException(CONTRADICTION);
        }
        h = Math.sqrt(ad * ad - off * off);
      }
      dx = off;
      dy = h == null ? Double.NaN : h;
      rules.add(rule("equal", a, d, b, c));
    } else if (ad != null && theta != null) {
      dx = ad * Math.cos(Math.toRadians(theta));
      dy = ad * Math.sin(Math.toRadians(theta));
    } else {
      throw new PlaneBuildException(UNDETERMINED);
    }
    if (Double.isNaN(dy)) {
      throw new PlaneBuildException(UNDETERMINED);
    }
    rules.add(rule("parallel", a, b, d, c));
    Map<String, double[]> pts = new LinkedHashMap<>();
    pts.put(a, new double[] {0, 0});
    pts.put(b, new double[] {ab, 0});
    pts.put(c, new double[] {dx + cd, dy});
    pts.put(d, new double[] {dx, dy});
    return figure(s, v, pts, rules);
  }

  private static PlaneFigure figure(
      PlaneSpec s, List<String> v, Map<String, double[]> pts, List<PlaneFigure.Rule> rules) {
    for (PlaneSpec.Given g : s.given()) {
      if (!"height".equals(g.type())) {
        rules.add(new PlaneFigure.Rule(g.type(), g.of(), g.value()));
      }
    }
    List<List<String>> segs = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      segs.add(List.of(v.get(i), v.get((i + 1) % 4)));
    }
    if (s.asked() != null
        && "length".equals(s.asked().kind())
        && s.asked().of().size() == 2
        && !isSide(v, s.asked().of())) {
      segs.add(List.copyOf(s.asked().of())); // đường chéo đề hỏi: vẽ, không ghi giá trị
    }
    for (PlaneSpec.Given g : s.given()) {
      if ("length".equals(g.type()) && !isSide(v, g.of()) && !segs.contains(g.of())) {
        segs.add(List.copyOf(g.of()));
      }
    }
    return new PlaneFigure(pts, List.copyOf(segs), List.of(), List.copyOf(rules), "engine");
  }

  private static boolean isSide(List<String> v, List<String> of) {
    int i = v.indexOf(of.get(0));
    int j = v.indexOf(of.get(1));
    return i >= 0 && j >= 0 && (Math.abs(i - j) == 1 || Math.abs(i - j) == 3);
  }

  private static void rightAngles(List<PlaneFigure.Rule> rules, List<String> v) {
    for (int i = 0; i < 4; i++) {
      rules.add(
          new PlaneFigure.Rule(
              "angle", List.of(v.get((i + 3) % 4), v.get(i), v.get((i + 1) % 4)), 90.0));
    }
  }

  private static double leg(double hyp, double other) {
    if (hyp <= other) {
      throw new PlaneBuildException(CONTRADICTION);
    }
    return Math.sqrt(hyp * hyp - other * other);
  }

  private static Double either(Double x, Double y) {
    if (x != null && y != null && Math.abs(x - y) > 1e-9 * Math.max(1, x)) {
      throw new PlaneBuildException(CONTRADICTION);
    }
    return x != null ? x : y;
  }
}
