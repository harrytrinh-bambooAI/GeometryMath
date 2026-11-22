package vn.geo.server.content.internal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Đáp số dạng chữ → các đại lượng số (M-GEO-03): "Sxq = 15π cm²; V = 12π cm³" thành hai đại lượng;
 * "a" theo tham số a; "24√3 cm³", "45°", "(√3/2)·π·a³". Bỏ đơn vị; dấu phẩy thập phân. Chỉ dùng cho
 * kiểm chứng đáp số của bộ đề chuẩn (engine đầy đủ thuộc E2).
 */
public final class AnswerExpr {

  /** Một đại lượng của đáp số: tên (có thể rỗng), biểu thức gốc, số chữ số thập phân đã ghi. */
  public record Quantity(String name, String expr, int decimals) {
    public boolean dependsOnA() {
      return expr.matches(".*(?<![A-Za-z])a(?![A-Za-z]).*");
    }

    public double valueAt(double a) {
      return new Parser(expr, a).parse();
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
   * 2, 3. Dung sai: max(1e-4·|tham chiếu|, nửa đơn vị chữ số thập phân cuối mà đáp số ghi) + 1e-12
   * (D-GEO-040, 163, 178 PROVISIONAL).
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
          double half = mine.decimals() > 0 ? 0.5 * Math.pow(10, -mine.decimals()) : 0;
          double tol = Math.max(1e-4 * Math.abs(q), half) + 1e-12;
          if (!(Math.abs(v - q) <= tol)) {
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

  /** Bộ phân tích biểu thức: + − · × * / ^ ² ³ √ π a ⅓, nhân ngầm, ngoặc. */
  private static final class Parser {
    private final String s;
    private final double a;
    private int i;

    Parser(String expr, double a) {
      this.s = expr.replace(',', '.').replace('−', '-').replace('–', '-');
      this.a = a;
    }

    double parse() {
      double v = expr();
      if (i != s.length()) {
        throw new IllegalArgumentException("thừa ký tự ở " + i + ": " + s);
      }
      return v;
    }

    private double expr() {
      double v = term();
      while (i < s.length()) {
        char c = s.charAt(i);
        if (c == '+') {
          i++;
          v += term();
        } else if (c == '-') {
          i++;
          v -= term();
        } else {
          break;
        }
      }
      return v;
    }

    private double term() {
      double v = power();
      while (i < s.length()) {
        char c = s.charAt(i);
        if (c == '*' || c == '·' || c == '×' || c == '⋅') {
          i++;
          v *= power();
        } else if (c == '/') {
          i++;
          v /= power();
        } else if (startsPrimary(c)) {
          v *= power();
        } else {
          break;
        }
      }
      return v;
    }

    private double power() {
      double v = unary();
      while (i < s.length()) {
        char c = s.charAt(i);
        if (c == '²') {
          i++;
          v = v * v;
        } else if (c == '³') {
          i++;
          v = v * v * v;
        } else if (c == '^') {
          i++;
          v = Math.pow(v, unary());
        } else {
          break;
        }
      }
      return v;
    }

    private double unary() {
      if (i >= s.length()) {
        throw new IllegalArgumentException("thiếu toán hạng: " + s);
      }
      char c = s.charAt(i);
      if (c == '-') {
        i++;
        return -unary();
      }
      if (c == '√') {
        i++;
        return Math.sqrt(power());
      }
      return primary();
    }

    private double primary() {
      char c = s.charAt(i);
      if (c == '(') {
        i++;
        double v = expr();
        if (i >= s.length() || s.charAt(i) != ')') {
          throw new IllegalArgumentException("thiếu ')': " + s);
        }
        i++;
        return v;
      }
      if (c == 'π') {
        i++;
        return Math.PI;
      }
      if (c == '⅓') {
        i++;
        return 1.0 / 3;
      }
      if (c == 'a') {
        i++;
        return a;
      }
      int st = i;
      while (i < s.length() && (Character.isDigit(s.charAt(i)) || s.charAt(i) == '.')) {
        i++;
      }
      if (st == i) {
        throw new IllegalArgumentException("ký tự lạ '" + c + "' trong: " + s);
      }
      return Double.parseDouble(s.substring(st, i));
    }

    private static boolean startsPrimary(char c) {
      return c == '(' || c == 'π' || c == 'a' || c == '√' || c == '⅓' || Character.isDigit(c);
    }
  }
}
