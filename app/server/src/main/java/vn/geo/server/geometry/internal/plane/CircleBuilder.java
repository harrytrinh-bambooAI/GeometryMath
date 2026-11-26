package vn.geo.server.geometry.internal.plane;

import static vn.geo.server.geometry.internal.plane.PlaneBuildException.BAD_ANGLE;
import static vn.geo.server.geometry.internal.plane.PlaneBuildException.BAD_LENGTH;
import static vn.geo.server.geometry.internal.plane.PlaneBuildException.CONTRADICTION;
import static vn.geo.server.geometry.internal.plane.PlaneBuildException.UNDETERMINED;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Đường tròn với dây và tiếp tuyến (BR-S2E2-05). Tâm O ở gốc.
 *
 * <ul>
 *   <li>circle_chord [O, A, B]: bán kính R cùng một trong dây AB, góc ở tâm AOB, khoảng cách từ O
 *       tới AB (hoặc AB cùng góc AOB). Dây nằm ngang phía dưới tâm.
 *   <li>circle_tangent [O, M, T]: R cùng OM hoặc độ dài tiếp tuyến MT (hoặc OM cùng MT); M trên
 *       trục hoành, T là tiếp điểm phía trên.
 * </ul>
 */
final class CircleBuilder {

  private CircleBuilder() {}

  static PlaneFigure build(PlaneSpec s) {
    List<String> v = s.points();
    if (v.size() < 3) {
      throw new PlaneBuildException(UNDETERMINED);
    }
    for (PlaneSpec.Given g : s.given()) {
      if (!"angle".equals(g.type()) && !(g.value() > 0)) {
        throw new PlaneBuildException(BAD_LENGTH);
      }
      if ("angle".equals(g.type()) && !(g.value() > 0 && g.value() <= 180)) {
        throw new PlaneBuildException(BAD_ANGLE);
      }
    }
    String o = v.get(0);
    String p = v.get(1);
    String q = v.get(2);
    Double r = s.first("radius");
    if (r == null) {
      r = s.length(o, "circle_chord".equals(s.shape()) ? p : q);
    }
    Map<String, double[]> pts = new LinkedHashMap<>();
    List<PlaneFigure.Rule> rules = new ArrayList<>();
    List<List<String>> segs = new ArrayList<>();
    if ("circle_chord".equals(s.shape())) {
      Double chord = s.length(p, q);
      Double central = s.angle(p, o, q);
      Double dist = null;
      for (PlaneSpec.Given g : s.given()) {
        if ("distance".equals(g.type())) {
          dist = g.value();
        }
      }
      if (r == null && chord != null && central != null) {
        r = chord / (2 * Math.sin(Math.toRadians(central) / 2));
      }
      if (r == null && chord != null && dist != null) {
        r = Math.sqrt(dist * dist + chord * chord / 4);
      }
      if (r == null) {
        throw new PlaneBuildException(UNDETERMINED);
      }
      double half;
      if (chord != null) {
        if (chord > 2 * r * (1 + 1e-12)) {
          throw new PlaneBuildException(CONTRADICTION);
        }
        half = Math.asin(Math.min(1, chord / (2 * r)));
      } else if (central != null) {
        half = Math.toRadians(central) / 2;
      } else if (dist != null) {
        if (dist >= r) {
          throw new PlaneBuildException(CONTRADICTION);
        }
        half = Math.acos(dist / r);
      } else {
        throw new PlaneBuildException(UNDETERMINED);
      }
      pts.put(o, new double[] {0, 0});
      pts.put(p, new double[] {-r * Math.sin(half), -r * Math.cos(half)});
      pts.put(q, new double[] {r * Math.sin(half), -r * Math.cos(half)});
      rules.add(new PlaneFigure.Rule("on_circle", List.of(o, p, q), null));
      segs.add(List.of(p, q));
      segs.add(List.of(o, p));
      segs.add(List.of(o, q));
    } else if ("circle_tangent".equals(s.shape())) {
      Double om = s.length(o, p);
      Double mt = s.length(p, q);
      if (r == null && om != null && mt != null) {
        if (om <= mt) {
          throw new PlaneBuildException(CONTRADICTION);
        }
        r = Math.sqrt(om * om - mt * mt);
      }
      if (r == null) {
        throw new PlaneBuildException(UNDETERMINED);
      }
      if (om == null && mt != null) {
        om = Math.sqrt(r * r + mt * mt);
      }
      if (om == null) {
        throw new PlaneBuildException(UNDETERMINED);
      }
      if (om <= r) {
        throw new PlaneBuildException(CONTRADICTION);
      }
      double tx = r * r / om;
      double ty = r * Math.sqrt(om * om - r * r) / om;
      pts.put(o, new double[] {0, 0});
      pts.put(p, new double[] {om, 0});
      pts.put(q, new double[] {tx, ty});
      if (v.size() > 3) {
        pts.put(v.get(3), new double[] {tx, -ty});
        rules.add(new PlaneFigure.Rule("on_circle", List.of(o, q, v.get(3)), null));
        rules.add(TriangleBuilder.rule("perpendicular", o, v.get(3), v.get(3), p));
        segs.add(List.of(p, v.get(3)));
      } else {
        rules.add(new PlaneFigure.Rule("on_circle", List.of(o, q), null));
      }
      rules.add(TriangleBuilder.rule("perpendicular", o, q, q, p));
      segs.add(List.of(p, q));
      segs.add(List.of(o, q));
      segs.add(List.of(o, p));
    } else {
      throw new PlaneBuildException(PlaneBuildException.UNSUPPORTED);
    }
    for (PlaneSpec.Given g : s.given()) {
      if (!"radius".equals(g.type())) {
        rules.add(new PlaneFigure.Rule(g.type(), g.of(), g.value()));
      }
    }
    for (String extra : v) {
      if (!pts.containsKey(extra)) {
        throw new PlaneBuildException(UNDETERMINED);
      }
    }
    return new PlaneFigure(
        pts,
        List.copyOf(segs),
        List.of(new PlaneFigure.Circle(o, r)),
        List.copyOf(rules),
        "engine");
  }
}
