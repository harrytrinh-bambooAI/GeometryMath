package vn.geo.server.geometry.internal.variant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import vn.geo.server.geometry.internal.plane.PlaneBuildException;
import vn.geo.server.geometry.internal.plane.PlaneEngine;
import vn.geo.server.geometry.internal.plane.PlaneFigure;
import vn.geo.server.geometry.internal.plane.PlaneSpec;

/**
 * Sinh số liệu cho biến thể đề hình phẳng (US-S2E9-01). Hàm thuần, tất định theo hạt giống
 * (BR-S2E9-04): giữ cấu trúc đề gốc, chỉ đổi giá trị các dữ kiện số; mỗi bộ số phải dựng được hình
 * thỏa ràng buộc bằng {@link PlaneEngine} và đáp số do engine đo lại trên hình (không gọi AI,
 * BR-S2E9-01).
 *
 * <p>Miền số (BR-S2E9-03): 1…100, số nguyên nếu số gốc nguyên, ngược lại một chữ số thập phân. Dữ
 * kiện có thể kèm {@code min}/{@code max} để thu hẹp miền. Bộ số mới khác đề gốc và khác nhau đôi
 * một; không đủ bộ khả dĩ thì trả ít hơn (BR-S2E9-02).
 */
public final class VariantGenerator {

  public static final String NO_PARAMS = "Đề này không có số liệu để đổi";
  public static final String UNSUPPORTED = "Dạng này chưa hỗ trợ biến thể";
  public static final double MIN = 1;
  public static final double MAX = 100;

  /** Dưới ngưỡng này thì duyệt hết miền (đếm đúng số bộ khả dĩ); trên thì lấy mẫu. */
  static final long ENUMERATE_LIMIT = 4000;

  private static final Set<String> NUMERIC = Set.of("length", "radius", "height");

  private VariantGenerator() {}

  /**
   * Một dữ kiện đổi được.
   *
   * @param index vị trí trong {@code given}
   * @param lo cận dưới của miền (≥ 1)
   * @param hi cận trên của miền (≤ 100)
   * @param integer chỉ nhận số nguyên
   */
  public record Param(int index, double original, double lo, double hi, boolean integer) {
    long size() {
      double step = integer ? 1 : 0.1;
      return Math.max(0, Math.round((hi - lo) / step) + 1);
    }

    double at(long i) {
      return integer ? lo + i : Math.round((lo + i * 0.1) * 10) / 10.0;
    }
  }

  /**
   * Một biến thể.
   *
   * @param values giá trị mới của MỌI dữ kiện theo thứ tự trong đề (dữ kiện không đổi giữ số gốc)
   * @param answer đáp số dạng chuẩn do engine tính ("15", "9√3", "53,1301°")
   * @param approx xấp xỉ hai chữ số thập phân ("15,59")
   */
  public record Variant(List<Double> values, String answer, double value, String approx) {}

  /**
   * @param refusal lý do từ chối (null khi sinh được)
   * @param requested số biến thể được yêu cầu
   */
  public record Result(String refusal, List<Variant> variants, int requested) {
    /** "Chỉ tạo được N biến thể khác nhau" khi miền không đủ bộ khả dĩ (BR-S2E9-02). */
    public String message() {
      return refusal == null && variants.size() < requested
          ? "Chỉ tạo được " + variants.size() + " biến thể khác nhau"
          : null;
    }
  }

  /** Miền của một dữ kiện; {@code min}/{@code max} null = miền mặc định quanh số gốc. */
  public record Range(Double min, Double max) {}

  /**
   * Các dữ kiện số đổi được của đề (độ dài, bán kính, chiều cao; không có thì tới góc khác 90°).
   */
  public static List<Integer> numericGivens(PlaneSpec spec) {
    List<Integer> out = new ArrayList<>();
    for (int i = 0; i < spec.given().size(); i++) {
      if (NUMERIC.contains(spec.given().get(i).type())) {
        out.add(i);
      }
    }
    if (out.isEmpty()) {
      for (int i = 0; i < spec.given().size(); i++) {
        PlaneSpec.Given g = spec.given().get(i);
        if ("angle".equals(g.type()) && Math.abs(g.value() - 90) > 1e-9) {
          out.add(i);
        }
      }
    }
    return out;
  }

  /**
   * Sinh tối đa {@code count} biến thể.
   *
   * @param variable vị trí các dữ kiện được đổi (null = mọi dữ kiện số)
   * @param ranges miền riêng theo vị trí dữ kiện (null hoặc phần tử null = mặc định)
   */
  public static Result generate(
      PlaneEngine engine,
      PlaneSpec spec,
      List<Integer> variable,
      List<Range> ranges,
      int count,
      long seed) {
    if (spec == null || spec.coords() != null || !spec.supported() || spec.asked() == null) {
      return new Result(spec == null ? NO_PARAMS : UNSUPPORTED, List.of(), count);
    }
    List<Integer> idx = new ArrayList<>(numericGivens(spec));
    if (variable != null) {
      idx.retainAll(variable);
    }
    if (idx.isEmpty()) {
      return new Result(NO_PARAMS, List.of(), count);
    }
    List<Param> params = new ArrayList<>();
    for (int i : idx) {
      Range r = ranges != null && i < ranges.size() ? ranges.get(i) : null;
      params.add(param(i, spec.given().get(i).value(), r));
    }
    Random random = new Random(seed);
    List<Variant> out = new ArrayList<>();
    Set<String> seen = new HashSet<>();
    long total = 1;
    for (Param p : params) {
      total = Math.min(Long.MAX_VALUE / 200, total * Math.max(1, p.size()));
    }
    if (total <= ENUMERATE_LIMIT) {
      List<Long> order = new ArrayList<>();
      for (long i = 0; i < total; i++) {
        order.add(i);
      }
      Collections.shuffle(order, random);
      for (long code : order) {
        if (out.size() >= count) {
          break;
        }
        double[] v = new double[params.size()];
        long c = code;
        for (int k = 0; k < params.size(); k++) {
          long n = Math.max(1, params.get(k).size());
          v[k] = params.get(k).at(c % n);
          c /= n;
        }
        accept(engine, spec, params, v, seen, out);
      }
    } else {
      int budget = Math.max(2000, 400 * count);
      for (int t = 0; t < budget && out.size() < count; t++) {
        double[] v = new double[params.size()];
        for (int k = 0; k < params.size(); k++) {
          Param p = params.get(k);
          v[k] = p.at((long) (random.nextDouble() * p.size()));
        }
        accept(engine, spec, params, v, seen, out);
      }
    }
    return new Result(null, List.copyOf(out), count);
  }

  private static Param param(int index, double original, Range r) {
    boolean integer = Math.abs(original - Math.rint(original)) < 1e-9;
    double lo = Math.floor(original / 2);
    double hi = Math.ceil(original * 2);
    if (hi - lo < 19) { // miền mặc định có ít nhất 20 giá trị (đề có số gốc sát cận 1)
      double pad = Math.ceil((19 - (hi - lo)) / 2);
      lo -= pad;
      hi += pad;
      if (lo < MIN) {
        hi += MIN - lo;
      }
    }
    if (r != null && r.min() != null) {
      lo = r.min();
    }
    if (r != null && r.max() != null) {
      hi = r.max();
    }
    lo = Math.max(MIN, integer ? Math.ceil(lo) : Math.round(lo * 10) / 10.0);
    hi = Math.min(MAX, integer ? Math.floor(hi) : Math.round(hi * 10) / 10.0);
    return new Param(index, original, lo, hi, integer);
  }

  /** Nhận bộ số nếu khác gốc, chưa có, mọi số trong 1…100 và hình dựng được thỏa ràng buộc. */
  private static void accept(
      PlaneEngine engine,
      PlaneSpec spec,
      List<Param> params,
      double[] v,
      Set<String> seen,
      List<Variant> out) {
    boolean same = true;
    StringBuilder key = new StringBuilder();
    for (int k = 0; k < v.length; k++) {
      if (!(v[k] >= MIN && v[k] <= MAX)) {
        return;
      }
      same &= Math.abs(v[k] - params.get(k).original()) < 1e-9;
      key.append(String.format(Locale.ROOT, "%.1f;", v[k]));
    }
    if (same || seen.contains(key.toString())) {
      return;
    }
    List<PlaneSpec.Given> given = new ArrayList<>(spec.given());
    for (int k = 0; k < v.length; k++) {
      PlaneSpec.Given g = given.get(params.get(k).index());
      given.set(params.get(k).index(), new PlaneSpec.Given(g.type(), g.of(), v[k]));
    }
    PlaneSpec next =
        new PlaneSpec(
            spec.shape(), spec.points(), spec.at(), spec.isosceles(), given, spec.asked(), null);
    Double value = solve(engine, next);
    if (value == null) {
      return;
    }
    seen.add(key.toString());
    List<Double> values = new ArrayList<>();
    given.forEach(g -> values.add(g.value()));
    out.add(
        new Variant(
            List.copyOf(values),
            exact(value, spec.asked().kind()),
            value,
            PlaneEngine.trim(value, 2)));
  }

  /**
   * Đáp số engine đo trên hình dựng từ {@code spec}; null khi hình không dựng được, suy biến hoặc
   * không thỏa ràng buộc (BR-S2E9-03, BR-S2E2-06).
   */
  public static Double solve(PlaneEngine engine, PlaneSpec spec) {
    try {
      if (!strictTriangle(spec)) {
        return null;
      }
      PlaneFigure f = engine.build(spec);
      if (!"engine".equals(f.coordSource()) || !engine.satisfied(f)) {
        return null;
      }
      double v = engine.measure(f, spec.asked());
      return Double.isFinite(v) && v > 1e-9 ? v : null;
    } catch (PlaneBuildException | IllegalArgumentException | IndexOutOfBoundsException e) {
      return null;
    }
  }

  /** Tam giác cho đủ ba cạnh: bất đẳng thức tam giác CHẶT (không suy biến). */
  static boolean strictTriangle(PlaneSpec spec) {
    if (!PlaneSpec.TRIANGLES.contains(spec.shape()) || spec.points().size() != 3) {
      return true;
    }
    List<Double> sides = new ArrayList<>();
    for (PlaneSpec.Given g : spec.given()) {
      if ("length".equals(g.type()) && g.of().size() == 2) {
        sides.add(g.value());
      }
    }
    if (sides.size() != 3) {
      return true;
    }
    double a = sides.get(0);
    double b = sides.get(1);
    double c = sides.get(2);
    double eps = 1e-9 * Math.max(a, Math.max(b, c));
    return a + b > c + eps && a + c > b + eps && b + c > a + eps;
  }

  /**
   * Dạng chuẩn của đáp số: góc "53,1301°"; số hữu tỉ gọn "15", "7,5"; số có căn "9√3", "5√2/2"
   * (bình phương là phân số có mẫu ≤ 16); còn lại làm tròn 4 chữ số thập phân.
   */
  public static String exact(double v, String kind) {
    if ("angle".equals(kind)) {
      return PlaneEngine.format(v, kind);
    }
    if (Math.abs(v * 10000 - Math.rint(v * 10000)) < 1e-7) {
      return PlaneEngine.trim(v, 4);
    }
    for (long q = 1; q <= 16; q++) {
      double pq = v * v * q;
      long p = Math.round(pq);
      if (p <= 0 || p > 1_000_000_000L || Math.abs(pq - p) > 1e-9 * Math.max(1, pq)) {
        continue;
      }
      long n = p * q; // v = √(p/q) = √(p·q) / q
      long k = 1;
      for (long d = 2; d * d <= n; d++) {
        while (n % (d * d) == 0) {
          n /= d * d;
          k *= d;
        }
      }
      long g = gcd(k, q);
      long num = k / g;
      long den = q / g;
      if (n == 1) {
        return den == 1 ? Long.toString(num) : num + "/" + den;
      }
      String surd = (num == 1 ? "" : Long.toString(num)) + "√" + n;
      return den == 1 ? surd : surd + "/" + den;
    }
    return PlaneEngine.trim(v, 4);
  }

  private static long gcd(long a, long b) {
    return b == 0 ? a : gcd(b, a % b);
  }
}
