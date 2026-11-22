package vn.geo.server.geometry.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Đáp số dạng chữ → đại lượng số, và MỘT công thức dung sai cho mọi chỗ so đáp số (BR-GEO-134;
 * D-GEO-040, 163, 178; chốt QF-E1-D07):
 *
 * <ul>
 *   <li>đề có tham số (đáp số hoặc hình phụ thuộc a): khớp khi {@code |q_AI − q| ≤ 1e-4·max(1, |q|)
 *       + 1e-12}, xét trên từng bộ trong ít nhất 3 bộ giá trị tham số (a = 1, 2, 3);
 *   <li>đề không tham số: tính một lần, khớp khi {@code |q_AI − q| ≤ max(1e-6, nửa đơn vị chữ số
 *       thập phân cuối mà đáp số ghi) + 1e-12}.
 * </ul>
 *
 * Cú pháp: "Sxq = 15π cm²; V = 12π cm³" (hai đại lượng có tên), "a", "24√3 cm³", "45°", "41,57",
 * "(√3/2)·π·a³". Bỏ đơn vị; dấu phẩy thập phân; phép tính + − · × * / ^ ² ³ √ π ⅓, nhân ngầm,
 * ngoặc.
 */
public final class AnswerMatch {

  /** ε máy (D-GEO-163). */
  public static final double EPS = 1e-12;

  public static final double REL = 1e-4;
  public static final double ABS_MIN = 1e-6;

  private static final Pattern DEPENDS_ON_A = Pattern.compile(".*(?<![A-Za-z])a(?![A-Za-z]).*");
  private static final Pattern DECIMALS = Pattern.compile("\\d+[.,](\\d+)");

  /** Một đại lượng của đáp số: tên (có thể rỗng), biểu thức đã bỏ đơn vị, số chữ số thập phân. */
  public record Quantity(String name, String expr, int decimals) {
    public boolean dependsOnA() {
      return DEPENDS_ON_A.matcher(expr).matches();
    }

    /** Giá trị tại a; ném IllegalArgumentException khi biểu thức sai cú pháp. */
    public double valueAt(double a) {
      return new Parser(expr, a).parse();
    }
  }

  private AnswerMatch() {}

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
    return s.replaceAll("cm[²³23]?|độ|°", "").replace(" ", "").strip();
  }

  private static int decimals(String expr) {
    var m = DECIMALS.matcher(expr);
    int d = 0;
    while (m.find()) {
      d = Math.max(d, m.group(1).length());
    }
    return d;
  }

  /**
   * Dung sai (đã cộng ε) khi so một đại lượng.
   *
   * @param parametric đề có tham số
   * @param computed giá trị tính (tham chiếu)
   * @param decimals số chữ số thập phân mà đáp số được so ghi
   */
  public static double tolerance(boolean parametric, double computed, int decimals) {
    if (parametric) {
      return REL * Math.max(1, Math.abs(computed)) + EPS;
    }
    double half = decimals > 0 ? 0.5 * Math.pow(10, -decimals) : 0;
    return Math.max(ABS_MIN, half) + EPS;
  }

  /** {@code |given − computed| ≤ tolerance} (so sánh có ε, D-GEO-163). NaN: không khớp. */
  public static boolean close(double given, double computed, boolean parametric, int decimals) {
    return Math.abs(given - computed) <= tolerance(parametric, computed, decimals);
  }

  /** Tên viết thường để ghép đại lượng theo tên. */
  public static String key(String name) {
    return name == null ? "" : name.strip().toLowerCase(Locale.ROOT);
  }

  /** Đại lượng của {@code given} ứng với đại lượng thứ {@code index} tên {@code name}. */
  public static Quantity pick(List<Quantity> given, String name, int index) {
    if (name != null && !name.isEmpty()) {
      for (Quantity q : given) {
        if (key(q.name()).equals(key(name))) {
          return q;
        }
      }
      return given.stream().allMatch(q -> q.name().isEmpty()) && index < given.size()
          ? given.get(index)
          : null;
    }
    return index < given.size() ? given.get(index) : null;
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
