package vn.geo.server.content.internal.solution.service;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import vn.geo.server.content.internal.service.AnswerExpr;

/**
 * S2-E3 (US-S2E3-04, BR-S2E3-17, 18): đối chiếu ba đáp số "Đề ghi / AI giải / Máy tính lại". Hai
 * đáp số khớp khi bằng nhau về giá trị (dạng viết tương đương, ví dụ {@code 4√3} và 6,9282) với
 * dung sai tuyệt đối 0,01 (đã chốt PO-66, QA-S2E3-06). Đáp số vắng (đề không ghi, máy chưa có)
 * không tính là lệch. Hàm thuần.
 */
public final class AnswerCompare {

  public static final double TOLERANCE = 0.01;
  public static final String MATCH = "match";
  public static final String MISMATCH = "mismatch";
  public static final String UNKNOWN = "unknown";

  private static final Pattern STATED =
      Pattern.compile("(?iu)[đĐ]áp\\s*số\\s*[:=]?\\s*([^\\n;]+?)\\s*\\.?\\s*$");
  private static final Pattern LAST_VALUE = Pattern.compile("(?:=|bằng)\\s*([^=]+?)\\s*\\.?\\s*$");

  private AnswerCompare() {}

  /** Kết quả đối chiếu; giá trị null khi đáp số đó vắng hoặc không đọc được thành số. */
  public record Result(String state, Double stated, Double ai, Double computed) {}

  /** Hai giá trị khớp trong dung sai 0,01 (có bù sai số dấu phẩy động). */
  public static boolean same(double a, double b) {
    return Math.abs(a - b) <= TOLERANCE + 1e-9;
  }

  /**
   * Giá trị số của một đáp số viết: "90°", "6,93", "4√3", "AC = 10 cm"; null khi không đọc được.
   */
  public static Double value(String text) {
    if (text == null || text.isBlank()) {
      return null;
    }
    try {
      List<AnswerExpr.Quantity> qs = AnswerExpr.parse(text.replace("độ", "°"));
      if (qs.isEmpty() || qs.get(0).dependsOnA()) {
        return null;
      }
      double v = qs.get(0).valueAt(1.0);
      return Double.isFinite(v) ? v : null;
    } catch (RuntimeException e) {
      return null;
    }
  }

  /**
   * Đối chiếu: lệch khi đáp số đề ghi hoặc đáp số AI khác máy tính lại; máy chưa có đáp số thì so
   * đề ghi với AI (nếu đủ hai); thiếu thì không lệch.
   */
  public static Result compare(String stated, String ai, Double computed) {
    Double s = value(stated);
    Double a = value(ai);
    String state;
    if (computed != null) {
      boolean bad = (s != null && !same(s, computed)) || (a != null && !same(a, computed));
      state = bad ? MISMATCH : MATCH;
    } else if (s != null && a != null) {
      state = same(s, a) ? MATCH : MISMATCH;
    } else {
      state = UNKNOWN;
    }
    return new Result(state, s, a, computed);
  }

  /** Đáp số đề ghi trong chữ đề: phần sau "Đáp số:" ở cuối đề; null khi đề không ghi. */
  public static String statedIn(String statement) {
    if (statement == null) {
      return null;
    }
    Matcher m = STATED.matcher(statement.strip());
    if (!m.find()) {
      return null;
    }
    String v = m.group(1).strip();
    return v.isEmpty() || v.length() > 100 ? null : v;
  }

  /**
   * Đáp số của lời giải: phần sau dấu "=" (hoặc "bằng") cuối cùng của bước cuối; null khi không có.
   */
  public static String lastValue(List<String> steps) {
    if (steps == null || steps.isEmpty()) {
      return null;
    }
    Matcher m = LAST_VALUE.matcher(steps.get(steps.size() - 1).strip());
    String v = null;
    while (m.find()) {
      v = m.group(1).strip();
    }
    return v == null || v.isEmpty() ? null : v;
  }

  /** Bước cuối với đáp số thay bằng {@code replacement} ("Dùng đáp số máy", US-S2E3-04-SC5). */
  public static String replaceLastValue(String step, String replacement) {
    Matcher m = Pattern.compile("((?:=|bằng)\\s*)([^=]+?)(\\s*\\.?\\s*)$").matcher(step.strip());
    if (m.find()) {
      return step.strip().substring(0, m.start(2)) + replacement + m.group(3);
    }
    return step.strip() + " = " + replacement;
  }

  /** Hiển thị một giá trị máy tính lại: góc kèm "°", dấu phẩy thập phân, tối đa 4 chữ số. */
  public static String format(Double v, boolean angle) {
    if (v == null) {
      return null;
    }
    double r = Math.round(v * 10_000d) / 10_000d;
    String s =
        r == Math.rint(r)
            ? String.valueOf((long) r)
            : String.format(Locale.ROOT, "%.4f", r).replaceAll("0+$", "").replace('.', ',');
    return angle ? s + "°" : s;
  }
}
