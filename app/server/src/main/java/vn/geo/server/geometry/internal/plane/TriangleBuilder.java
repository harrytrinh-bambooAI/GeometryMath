package vn.geo.server.geometry.internal.plane;

import static vn.geo.server.geometry.internal.plane.PlaneBuildException.BAD_ANGLE;
import static vn.geo.server.geometry.internal.plane.PlaneBuildException.BAD_LENGTH;
import static vn.geo.server.geometry.internal.plane.PlaneBuildException.CONTRADICTION;
import static vn.geo.server.geometry.internal.plane.PlaneBuildException.NOT_TRIANGLE;
import static vn.geo.server.geometry.internal.plane.PlaneBuildException.UNDETERMINED;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dựng tam giác từ dữ kiện cạnh/góc (BR-S2E2-05): suy dần bằng tổng ba góc, tam giác cân/đều/vuông,
 * định lý sin, cos (c-g-c, g-c-g, c-c-c, c-c-g khi duy nhất). Đặt V0 ở gốc, V1 trên trục hoành. Tam
 * giác chỉ xác định tới đồng dạng (chỉ có góc) thì dựng với cạnh V0V1 = 5 khi đại lượng hỏi là góc;
 * hỏi độ dài/diện tích thì "hình chưa xác định, thiếu dữ kiện".
 */
final class TriangleBuilder {

  static final double DEFAULT_SCALE = 5;
  private static final double TOL = 1e-12;

  private TriangleBuilder() {}

  static PlaneFigure build(PlaneSpec s) {
    List<String> v = s.points();
    if (v.size() != 3) {
      throw new PlaneBuildException(UNDETERMINED);
    }
    Double[] side = new Double[3]; // side[i] đối diện đỉnh i
    Double[] ang = new Double[3]; // radian
    for (PlaneSpec.Given g : s.given()) {
      switch (g.type()) {
        case "length" -> {
          int i = opposite(v, g.of());
          if (!(g.value() > 0) || !Double.isFinite(g.value())) {
            throw new PlaneBuildException(BAD_LENGTH);
          }
          side[i] = merge(side[i], g.value());
        }
        case "angle" -> {
          if (g.of().size() != 3 || !v.contains(g.of().get(1))) {
            throw new PlaneBuildException(UNDETERMINED);
          }
          if (!(g.value() > 0 && g.value() < 180)) {
            throw new PlaneBuildException(BAD_ANGLE);
          }
          int i = v.indexOf(g.of().get(1));
          ang[i] = merge(ang[i], Math.toRadians(g.value()));
        }
        default -> throw new PlaneBuildException(UNDETERMINED);
      }
    }
    List<int[]> eq = new ArrayList<>(); // cặp đỉnh có cạnh đối diện bằng nhau và góc bằng nhau
    List<PlaneFigure.Rule> rules = new ArrayList<>();
    int x = s.at() == null ? 0 : v.indexOf(s.at());
    if (x < 0) {
      throw new PlaneBuildException(UNDETERMINED);
    }
    int j = (x + 1) % 3;
    int k = (x + 2) % 3;
    switch (s.shape()) {
      case "equilateral_triangle" -> {
        eq.add(new int[] {0, 1});
        eq.add(new int[] {1, 2});
        for (int i = 0; i < 3; i++) {
          ang[i] = merge(ang[i], Math.PI / 3);
        }
        rules.add(rule("equal", v.get(0), v.get(1), v.get(1), v.get(2)));
        rules.add(rule("equal", v.get(1), v.get(2), v.get(2), v.get(0)));
      }
      case "isosceles_triangle" -> {
        eq.add(new int[] {j, k});
        rules.add(rule("equal", v.get(x), v.get(j), v.get(x), v.get(k)));
      }
      case "right_triangle" -> {
        ang[x] = merge(ang[x], Math.PI / 2);
        rules.add(new PlaneFigure.Rule("angle", List.of(v.get(j), v.get(x), v.get(k)), 90.0));
      }
      default -> {
        // tam giác thường: không ràng buộc ngầm
      }
    }
    solve(side, ang, eq);
    if (!all(side)) {
      boolean anySide = side[0] != null || side[1] != null || side[2] != null;
      boolean angleAsked = s.asked() != null && "angle".equals(s.asked().kind());
      if (all(ang) && !anySide && angleAsked) {
        side[2] = DEFAULT_SCALE;
        solve(side, ang, eq);
      }
      if (!all(side)) {
        throw new PlaneBuildException(UNDETERMINED);
      }
    }
    checkInequality(side);
    double a = side[0];
    double b = side[1];
    double c = side[2];
    double cosA = (b * b + c * c - a * a) / (2 * b * c);
    double sinA = Math.sqrt(Math.max(0, 1 - cosA * cosA));
    Map<String, double[]> pts = new LinkedHashMap<>();
    pts.put(v.get(0), new double[] {0, 0});
    pts.put(v.get(1), new double[] {c, 0});
    pts.put(v.get(2), new double[] {b * cosA, b * sinA});
    for (PlaneSpec.Given g : s.given()) {
      rules.add(new PlaneFigure.Rule(g.type(), g.of(), g.value()));
    }
    List<List<String>> segs =
        List.of(
            List.of(v.get(0), v.get(1)), List.of(v.get(1), v.get(2)), List.of(v.get(2), v.get(0)));
    return new PlaneFigure(pts, segs, List.of(), List.copyOf(rules), "engine");
  }

  private static void solve(Double[] side, Double[] ang, List<int[]> eq) {
    boolean changed = true;
    for (int guard = 0; changed && guard < 20; guard++) {
      changed = false;
      for (int[] p : eq) {
        changed |= copy(side, p[0], p[1]) | copy(ang, p[0], p[1]);
      }
      int known = count(ang);
      if (known == 2) {
        double sum = 0;
        int miss = -1;
        for (int i = 0; i < 3; i++) {
          if (ang[i] == null) {
            miss = i;
          } else {
            sum += ang[i];
          }
        }
        if (sum >= Math.PI - TOL) {
          throw new PlaneBuildException(BAD_ANGLE);
        }
        ang[miss] = Math.PI - sum;
        changed = true;
      } else if (known == 3) {
        if (Math.abs(ang[0] + ang[1] + ang[2] - Math.PI) > 1e-9) {
          throw new PlaneBuildException(CONTRADICTION);
        }
      }
      for (int[] p : eq) {
        int third = 3 - p[0] - p[1];
        if (ang[p[0]] == null && ang[p[1]] == null && ang[third] != null) {
          double base = (Math.PI - ang[third]) / 2;
          ang[p[0]] = base;
          ang[p[1]] = base;
          changed = true;
        }
      }
      if (all(ang) && !all(side) && count(side) > 0) {
        int i = side[0] != null ? 0 : side[1] != null ? 1 : 2;
        double ratio = side[i] / Math.sin(ang[i]);
        for (int t = 0; t < 3; t++) {
          if (side[t] == null) {
            side[t] = ratio * Math.sin(ang[t]);
          }
        }
        changed = true;
      }
      for (int i = 0; i < 3; i++) {
        int p = (i + 1) % 3;
        int q = (i + 2) % 3;
        if (ang[i] != null && side[i] == null && side[p] != null && side[q] != null) {
          side[i] =
              Math.sqrt(
                  side[p] * side[p] + side[q] * side[q] - 2 * side[p] * side[q] * Math.cos(ang[i]));
          changed = true;
        }
        for (int t : new int[] {p, q}) {
          if (ang[i] != null && side[i] != null && side[t] != null && ang[t] == null) {
            double sin = side[t] * Math.sin(ang[i]) / side[i];
            if (sin > 1 + 1e-12) {
              throw new PlaneBuildException(CONTRADICTION);
            }
            if (side[i] >= side[t] - TOL) {
              ang[t] = Math.asin(Math.min(1, sin));
              changed = true;
            }
          }
        }
      }
      if (all(side) && !all(ang)) {
        checkInequality(side);
        for (int i = 0; i < 3; i++) {
          if (ang[i] == null) {
            int p = (i + 1) % 3;
            int q = (i + 2) % 3;
            double cos =
                (side[p] * side[p] + side[q] * side[q] - side[i] * side[i])
                    / (2 * side[p] * side[q]);
            ang[i] = Math.acos(Math.max(-1, Math.min(1, cos)));
          }
        }
        changed = true;
      }
    }
  }

  private static void checkInequality(Double[] s) {
    double max = Math.max(s[0], Math.max(s[1], s[2]));
    for (int i = 0; i < 3; i++) {
      double rest = s[(i + 1) % 3] + s[(i + 2) % 3];
      if (rest - s[i] <= 1e-9 * max) {
        throw new PlaneBuildException(NOT_TRIANGLE);
      }
    }
  }

  private static int opposite(List<String> v, List<String> of) {
    if (of.size() != 2 || of.get(0).equals(of.get(1)) || !v.containsAll(of)) {
      throw new PlaneBuildException(UNDETERMINED);
    }
    for (int i = 0; i < 3; i++) {
      if (!of.contains(v.get(i))) {
        return i;
      }
    }
    throw new PlaneBuildException(UNDETERMINED);
  }

  private static Double merge(Double old, double val) {
    if (old != null && Math.abs(old - val) > 1e-9 * Math.max(1, Math.abs(val))) {
      throw new PlaneBuildException(CONTRADICTION);
    }
    return val;
  }

  private static boolean copy(Double[] arr, int a, int b) {
    if (arr[a] != null && arr[b] == null) {
      arr[b] = arr[a];
      return true;
    }
    if (arr[b] != null && arr[a] == null) {
      arr[a] = arr[b];
      return true;
    }
    if (arr[a] != null && Math.abs(arr[a] - arr[b]) > 1e-9 * Math.max(1, Math.abs(arr[a]))) {
      throw new PlaneBuildException(CONTRADICTION);
    }
    return false;
  }

  private static int count(Double[] arr) {
    int n = 0;
    for (Double d : arr) {
      if (d != null) {
        n++;
      }
    }
    return n;
  }

  private static boolean all(Double[] arr) {
    return count(arr) == 3;
  }

  static PlaneFigure.Rule rule(String type, String a, String b, String c, String d) {
    return new PlaneFigure.Rule(type, List.of(a, b, c, d), null);
  }
}
