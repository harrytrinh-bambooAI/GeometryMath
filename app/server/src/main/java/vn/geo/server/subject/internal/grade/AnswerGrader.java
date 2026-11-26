package vn.geo.server.subject.internal.grade;

import java.math.BigDecimal;
import java.math.BigInteger;
import vn.geo.server.subject.internal.grade.Expression.Parsed;
import vn.geo.server.subject.internal.grade.Expression.SyntaxError;
import vn.geo.server.subject.internal.grade.Expression.Value;

/**
 * {@code grade} chung cho biểu thức số (PLAN_code-s23 A1; US-S2E8-02): so đáp số nộp với đáp số mẫu
 * theo kiểu ("chính xác" hoặc "xấp xỉ") và sai số. Ba kết quả: Đúng (kèm dạng chuẩn khi cách viết
 * khác mẫu), Sai, Sai cú pháp (kèm lý do và vị trí ký tự) — BR-S2E8-06…11. Hàm thuần.
 */
public final class AnswerGrader {

  /** Kết quả chấm. */
  public enum Outcome {
    CORRECT,
    WRONG,
    SYNTAX
  }

  /** Kiểu đáp số (QA-S2E8-01, đã chốt PO-66). */
  public enum Mode {
    EXACT,
    APPROX
  }

  /**
   * @param canonical dạng chuẩn (cách viết đáp số mẫu) khi Đúng mà cách viết khác mẫu; ngược lại
   *     null
   * @param message lý do Sai cú pháp kèm vị trí, ví dụ "Thiếu số sau dấu căn (ký tự 1)"
   * @param position vị trí ký tự lỗi tính từ 1; null khi lỗi không gắn vị trí
   * @param note lời nhắc dạng viết khi Sai (không chứa đáp số), ví dụ "Em hãy viết đáp số ở dạng
   *     chính xác (có căn)"
   */
  public record Result(
      Outcome outcome, String canonical, String message, Integer position, String note) {}

  static final double REL_EPS = 1e-9;

  /** Giá trị thử cho a, x (dương, không đặc biệt) — so khớp theo giá trị (BR-S2E8-10). */
  private static final double[][] SAMPLES = {
    {1.3716049382716, 0.7390851332152}, {2.6180339887499, 1.9129311827724}, {0.5324, 3.1778}
  };

  private AnswerGrader() {}

  /** Đáp số mẫu có đọc và tính được không ("đề có đáp số máy chấm được", BR-S2E8-27). */
  public static boolean gradable(String reference) {
    try {
      Parsed p = Expression.parse(reference);
      for (double[] s : SAMPLES) {
        Expression.eval(p.root(), s[0], s[1]);
      }
      return true;
    } catch (SyntaxError e) {
      return false;
    }
  }

  /**
   * Chấm.
   *
   * @param tolerance sai số tuyệt đối của kiểu xấp xỉ (null = 0); bỏ qua ở kiểu chính xác
   * @throws IllegalArgumentException khi đáp số mẫu không đọc được
   */
  public static Result grade(String reference, String submitted, Mode mode, BigDecimal tolerance) {
    Parsed ref;
    Value[] refValues = new Value[SAMPLES.length];
    try {
      ref = Expression.parse(reference);
      for (int i = 0; i < SAMPLES.length; i++) {
        refValues[i] = Expression.eval(ref.root(), SAMPLES[i][0], SAMPLES[i][1]);
      }
    } catch (SyntaxError e) {
      throw new IllegalArgumentException("đáp số mẫu không đọc được: " + e.text());
    }
    Parsed sub;
    Value[] subValues = new Value[SAMPLES.length];
    try {
      sub = Expression.parse(submitted);
      for (int i = 0; i < SAMPLES.length; i++) {
        subValues[i] = Expression.eval(sub.root(), SAMPLES[i][0], SAMPLES[i][1]);
      }
    } catch (SyntaxError e) {
      return new Result(Outcome.SYNTAX, null, e.text(), e.position > 0 ? e.position : null, null);
    }
    boolean ok = true;
    for (int i = 0; i < SAMPLES.length && ok; i++) {
      ok =
          mode == Mode.APPROX
              ? within(refValues[i], subValues[i], tolerance == null ? BigDecimal.ZERO : tolerance)
              : same(refValues[i], subValues[i]);
    }
    if (ok) {
      boolean sameForm =
          normalize(reference).equals(normalize(submitted))
              || (ref.plainNumber() && sub.plainNumber());
      return new Result(Outcome.CORRECT, sameForm ? null : reference.strip(), null, null, null);
    }
    String note = null;
    if (mode == Mode.EXACT && sub.plainNumber() && !refValues[0].exact() && !ref.hasVar()) {
      // QA-S2E8-02: thập phân làm tròn của số vô tỉ là Sai; chỉ nhắc dạng viết, không nói gần hay
      // xa
      note =
          "Em hãy viết đáp số ở dạng chính xác"
              + (ref.hasRoot() ? " (có căn)" : ref.hasPi() ? " (có π)" : "");
    }
    return new Result(Outcome.WRONG, null, null, null, note);
  }

  /** Chuẩn hóa cách viết để so "cùng dạng viết" và so với dạng chấp nhận thêm (BR-S2E8-31). */
  public static String normalize(String s) {
    if (s == null) {
      return "";
    }
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (Character.isWhitespace(c) || c == ' ') {
        continue;
      }
      b.append(
          switch (c) {
            case '.' -> ',';
            case '*', '·' -> '×';
            case ':' -> '÷';
            case '−', '–' -> '-';
            default -> c;
          });
    }
    return b.toString();
  }

  /** Kiểu chính xác: sai lệch tương đối ≤ 1e-9 (tuyệt đối khi đáp số mẫu bằng 0) — BR-S2E8-08. */
  private static boolean same(Value ref, Value sub) {
    if (ref.exact() && sub.exact() && ref.n().equals(sub.n()) && ref.d().equals(sub.d())) {
      return true;
    }
    double r = ref.approx();
    double s = sub.approx();
    if (ref.isZero()) {
      return Math.abs(s) <= REL_EPS;
    }
    return Math.abs(s - r) <= REL_EPS * Math.abs(r);
  }

  /** Kiểu xấp xỉ: |nộp − mẫu| ≤ tol, tính bằng phân số chính xác khi có thể — BR-S2E8-09. */
  private static boolean within(Value ref, Value sub, BigDecimal tol) {
    if (ref.exact() && sub.exact()) {
      // |sn/sd − rn/rd| ≤ tn/td  ⇔  |sn·rd − rn·sd| · td ≤ tn · sd · rd
      BigInteger tn = tol.unscaledValue();
      BigInteger td = BigInteger.TEN.pow(Math.max(0, tol.scale()));
      if (tol.scale() < 0) {
        tn = tn.multiply(BigInteger.TEN.pow(-tol.scale()));
      }
      BigInteger diff = sub.n().multiply(ref.d()).subtract(ref.n().multiply(sub.d())).abs();
      return diff.multiply(td).compareTo(tn.multiply(sub.d()).multiply(ref.d())) <= 0;
    }
    double d = Math.abs(sub.approx() - ref.approx());
    return d <= tol.doubleValue() + REL_EPS * Math.max(1.0, Math.abs(ref.approx()));
  }
}
