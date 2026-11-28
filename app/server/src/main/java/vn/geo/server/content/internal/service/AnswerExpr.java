package vn.geo.server.content.internal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import vn.geo.server.geometry.api.AnswerMatch;

/**
 * Đáp số dạng chữ → các đại lượng số (M-GEO-03): "Sxq = 15π cm²; V = 12π cm³" thành hai đại lượng;
 * "a" theo tham số a; "24√3 cm³", "45°", "(√3/2)·π·a³". Bỏ đơn vị; dấu phẩy thập phân. Chỉ dùng cho
 * kiểm chứng đáp số của bộ đề chuẩn; phép tính và dung sai dùng chung {@link AnswerMatch} của E2.
 */
public final class AnswerExpr {

  /** Một đại lượng của đáp số: tên (có thể rỗng), biểu thức gốc, số chữ số thập phân đã ghi. */
  public record Quantity(String name, String expr, int decimals) {
    public boolean dependsOnA() {
      return expr.matches(".*(?<![A-Za-z])a(?![A-Za-z]).*");
    }

    public double valueAt(double a) {
      return new AnswerMatch.Quantity(name, expr, decimals).valueAt(a);
    }
  }

  private AnswerExpr() {}

  public static List<Quantity> parse(String answer) {
    List<Quantity> out = new ArrayList<>();
    if (answer == null || answer.isBlank()) {
      return out;
    }
    for (String part : answer.split(";")) {
      String p = part.strip();
      if (p.isEmpty()) {
        continue;
      }
      String name = "";
      int eq = p.indexOf('=');
      if (eq > 0) {
        name = p.substring(0, eq).strip();
        p = p.substring(eq + 1).strip();
      }
      String expr = stripUnits(p);
      out.add(new Quantity(name, expr, decimals(expr)));
    }
    return out;
  }

  static String stripUnits(String s) {
    return s.replaceAll("cm[²³]?|độ|°", "").replace(" ", "").strip();
  }

  private static int decimals(String expr) {
    var m = java.util.regex.Pattern.compile("\\d+[.,](\\d+)").matcher(expr);
    int d = 0;
    while (m.find()) {
      d = Math.max(d, m.group(1).length());
    }
    return d;
  }

  /**
   * Hai đáp số khớp từng đại lượng (theo tên nếu cả hai có tên, nếu không theo thứ tự), tại a = 1,
   * 2, 3. Dung sai: công thức DUY NHẤT của engine E2 ({@link AnswerMatch#tolerance}, BR-GEO-134;
   * chốt QF-E1-D07): đại lượng có tham số a dùng tương đối 1e-4·max(1, |q|), không tham số dùng
   * max(1e-6, nửa đơn vị chữ số thập phân cuối mà đáp số ghi); cộng ε = 1e-12.
   */
  public static boolean matches(String given, List<Quantity> reference) {
    List<Quantity> g;
    try {
      g = parse(given);
      if (g.isEmpty() || g.size() != reference.size()) {
        return false;
      }
      for (int i = 0; i < reference.size(); i++) {
        Quantity ref = reference.get(i);
        Quantity mine = byName(g, ref.name(), i);
        if (mine == null) {
          return false;
        }
        for (double a : new double[] {1, 2, 3}) {
          double q = ref.valueAt(a);
          double v = mine.valueAt(a);
          boolean parametric = ref.dependsOnA() || mine.dependsOnA();
          if (!AnswerMatch.close(v, q, parametric, mine.decimals())) {
            return false;
          }
        }
      }
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  private static Quantity byName(List<Quantity> g, String name, int index) {
    if (!name.isEmpty()) {
      for (Quantity q : g) {
        if (q.name().toLowerCase(Locale.ROOT).equals(name.toLowerCase(Locale.ROOT))) {
          return q;
        }
      }
      return g.stream().allMatch(q -> q.name().isEmpty()) ? g.get(index) : null;
    }
    return g.get(index);
  }

  /**
   * Giá trị tham chiếu ghi vào solution.answer_forms (D-GEO-040: a = 1, 2, 3 khi đề có tham số).
   */
  public static List<Map<String, Object>> forms(List<Quantity> qs) {
    List<Map<String, Object>> out = new ArrayList<>();
    for (Quantity q : qs) {
      List<Map<String, Object>> values = new ArrayList<>();
      if (q.dependsOnA()) {
        for (int a = 1; a <= 3; a++) {
          values.add(Map.of("a", a, "value", q.valueAt(a)));
        }
      } else {
        values.add(Map.of("value", q.valueAt(1)));
      }
      out.add(Map.of("name", q.name(), "expr", q.expr(), "values", values));
    }
    return out;
  }
}
