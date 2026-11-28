package vn.geo.server.geometry.internal.plane;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Engine hình phẳng (US-S2E2-02): dựng từ ràng buộc, kiểm ràng buộc (BR-S2E2-06: sai số tương đối ≤
 * 1e-6), đo đại lượng, xuất {@code SceneSpec} 2D (SCENE-CONTRACT-2D.md). Hàm thuần, tất định
 * (BR-S2E2-08), không gọi AI, không đọc CSDL. Dạng ngoài danh sách đi nhánh "Cần xem lại" và ghi
 * một dòng thống kê (US-S2E2-02-FC3).
 */
@Component
public class PlaneEngine {

  private static final Logger LOG = LoggerFactory.getLogger(PlaneEngine.class);

  /** Ngưỡng sai số tương đối (BR-S2E2-06, đã chốt PO-66 QA-S2E2-04). */
  public static final double REL_TOL = 1e-6;

  private final Map<String, AtomicLong> unsupported = new ConcurrentHashMap<>();

  /**
   * Dựng hình.
   *
   * @throws PlaneBuildException lý do không dựng (mâu thuẫn, suy biến, thiếu dữ kiện, dạng chưa hỗ
   *     trợ)
   */
  public PlaneFigure build(PlaneSpec s) {
    if (s.coords() != null) {
      return fromAi(s);
    }
    if (!s.supported()) {
      unsupported.computeIfAbsent(s.shape(), k -> new AtomicLong()).incrementAndGet();
      LOG.info("plane.unsupported shape={}", s.shape());
      throw new PlaneBuildException(PlaneBuildException.UNSUPPORTED);
    }
    if (PlaneSpec.TRIANGLES.contains(s.shape())) {
      return TriangleBuilder.build(s);
    }
    if (PlaneSpec.QUADS.contains(s.shape())) {
      return QuadBuilder.build(s);
    }
    return CircleBuilder.build(s);
  }

  /** Số lần gặp một dạng chưa hỗ trợ (dòng thống kê, FC3). */
  public long unsupportedCount(String shape) {
    AtomicLong n = unsupported.get(shape);
    return n == null ? 0 : n.get();
  }

  /** Hình từ tọa độ AI: vẽ được nhưng không kiểm chứng (nhãn "Cần xem lại"). */
  private PlaneFigure fromAi(PlaneSpec s) {
    List<List<String>> segs = new ArrayList<>();
    List<String> v = new ArrayList<>(s.coords().keySet());
    for (int i = 0; i < v.size(); i++) {
      segs.add(List.of(v.get(i), v.get((i + 1) % v.size())));
    }
    List<PlaneFigure.Rule> rules = new ArrayList<>();
    for (PlaneSpec.Given g : s.given()) {
      rules.add(new PlaneFigure.Rule(g.type(), g.of(), g.value()));
    }
    return new PlaneFigure(s.coords(), List.copyOf(segs), List.of(), List.copyOf(rules), "ai");
  }

  /** Mọi ràng buộc đúng trong sai số {@link #REL_TOL}. */
  public boolean satisfied(PlaneFigure f) {
    for (PlaneFigure.Rule r : f.rules()) {
      if (error(f, r) > REL_TOL + 1e-12) {
        return false;
      }
    }
    return true;
  }

  /** Sai số tương đối của một ràng buộc. */
  public double error(PlaneFigure f, PlaneFigure.Rule r) {
    List<String> of = r.of();
    return switch (r.type()) {
      case "length" -> rel(dist(f.p(of.get(0)), f.p(of.get(1))), r.value());
      case "angle" -> Math.abs(angle(f, of) - r.value()) / Math.max(1, Math.abs(r.value()));
      case "distance" -> rel(lineDist(f.p(of.get(0)), f.p(of.get(1)), f.p(of.get(2))), r.value());
      case "equal" -> {
        double d1 = dist(f.p(of.get(0)), f.p(of.get(1)));
        double d2 = dist(f.p(of.get(2)), f.p(of.get(3)));
        yield Math.abs(d1 - d2) / Math.max(1e-300, Math.max(d1, d2));
      }
      case "parallel", "perpendicular" -> {
        double[] u = sub(f.p(of.get(1)), f.p(of.get(0)));
        double[] w = sub(f.p(of.get(3)), f.p(of.get(2)));
        double n = Math.hypot(u[0], u[1]) * Math.hypot(w[0], w[1]);
        double x =
            "parallel".equals(r.type()) ? u[0] * w[1] - u[1] * w[0] : u[0] * w[0] + u[1] * w[1];
        yield Math.abs(x) / Math.max(1e-300, n);
      }
      case "on_circle" -> {
        double radius =
            f.circles().stream()
                .filter(c -> c.center().equals(of.get(0)))
                .mapToDouble(PlaneFigure.Circle::r)
                .findFirst()
                .orElse(dist(f.p(of.get(0)), f.p(of.get(1))));
        double worst = 0;
        for (String x : of.subList(1, of.size())) {
          worst = Math.max(worst, rel(dist(f.p(of.get(0)), f.p(x)), radius));
        }
        yield worst;
      }
      case "radius" -> 0;
      default -> Double.POSITIVE_INFINITY;
    };
  }

  /**
   * Giá trị đại lượng hỏi trên hình. kind: length [P,Q] · angle [P,O,Q] (độ) · area [đỉnh…] (rỗng:
   * đa giác của hình, hoặc hình tròn khi hình là đường tròn) · perimeter [đỉnh…] · distance
   * [P,A,B].
   */
  public double measure(PlaneFigure f, PlaneSpec.Asked q) {
    List<String> of = q.of();
    return switch (q.kind()) {
      case "length" -> dist(f.p(of.get(0)), f.p(of.get(1)));
      case "angle" -> angle(f, of);
      case "distance" -> lineDist(f.p(of.get(0)), f.p(of.get(1)), f.p(of.get(2)));
      case "area" -> {
        if (of.isEmpty() && !f.circles().isEmpty()) {
          double r = f.circles().get(0).r();
          yield Math.PI * r * r;
        }
        yield area(f, of.isEmpty() ? polygon(f) : of);
      }
      case "perimeter" -> {
        if (of.isEmpty() && !f.circles().isEmpty()) {
          yield 2 * Math.PI * f.circles().get(0).r();
        }
        List<String> poly = of.isEmpty() ? polygon(f) : of;
        double s = 0;
        for (int i = 0; i < poly.size(); i++) {
          s += dist(f.p(poly.get(i)), f.p(poly.get((i + 1) % poly.size())));
        }
        yield s;
      }
      default -> throw new PlaneBuildException("đại lượng hỏi chưa hỗ trợ: " + q.kind());
    };
  }

  /** Cảnh 2D gửi xuống thiết bị: chỉ điểm, nhãn, ràng buộc đề cho, tọa độ (BR-S2E2-07). */
  public PlaneScene scene(PlaneFigure f) {
    List<PlaneScene.Point> pts = new ArrayList<>();
    f.points().forEach((k, p) -> pts.add(new PlaneScene.Point(k, k, p[0], p[1])));
    List<PlaneScene.Circle> circles =
        f.circles().stream().map(c -> new PlaneScene.Circle(c.center(), c.r())).toList();
    List<PlaneScene.Constraint> cons =
        f.rules().stream()
            .map(r -> new PlaneScene.Constraint(r.type(), r.of(), r.value()))
            .toList();
    return new PlaneScene(
        PlaneScene.CONTRACT, "plane", f.coordSource(), pts, f.segments(), circles, cons);
  }

  /** Đáp số dạng chữ: góc "90°", số khác làm tròn 4 chữ số thập phân, dấu phẩy, bỏ số 0 thừa. */
  public static String format(double v, String kind) {
    String s = trim(v, 4);
    return "angle".equals(kind) ? s + "°" : s;
  }

  /** Làm tròn {@code decimals} chữ số, dấu phẩy, bỏ 0 thừa ("5", "6,9282"). */
  public static String trim(double v, int decimals) {
    String s = String.format(Locale.ROOT, "%." + decimals + "f", v);
    if (s.contains(".")) {
      s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
    }
    if ("-0".equals(s)) {
      s = "0";
    }
    return s.replace('.', ',');
  }

  private static List<String> polygon(PlaneFigure f) {
    return new ArrayList<>(f.points().keySet());
  }

  private static double area(PlaneFigure f, List<String> poly) {
    double s = 0;
    for (int i = 0; i < poly.size(); i++) {
      double[] a = f.p(poly.get(i));
      double[] b = f.p(poly.get((i + 1) % poly.size()));
      s += a[0] * b[1] - a[1] * b[0];
    }
    return Math.abs(s) / 2;
  }

  private static double angle(PlaneFigure f, List<String> of) {
    double[] u = sub(f.p(of.get(0)), f.p(of.get(1)));
    double[] w = sub(f.p(of.get(2)), f.p(of.get(1)));
    double cos = (u[0] * w[0] + u[1] * w[1]) / (Math.hypot(u[0], u[1]) * Math.hypot(w[0], w[1]));
    return Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, cos))));
  }

  static double dist(double[] a, double[] b) {
    return Math.hypot(a[0] - b[0], a[1] - b[1]);
  }

  private static double lineDist(double[] p, double[] a, double[] b) {
    double[] u = sub(b, a);
    double[] w = sub(p, a);
    return Math.abs(u[0] * w[1] - u[1] * w[0]) / Math.hypot(u[0], u[1]);
  }

  private static double[] sub(double[] a, double[] b) {
    return new double[] {a[0] - b[0], a[1] - b[1]};
  }

  private static double rel(double got, Double want) {
    if (want == null) {
      return Double.POSITIVE_INFINITY;
    }
    return Math.abs(got - want) / Math.max(1e-300, Math.abs(want));
  }
}
