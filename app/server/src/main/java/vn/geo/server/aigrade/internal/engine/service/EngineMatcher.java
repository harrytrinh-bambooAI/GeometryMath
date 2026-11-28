package vn.geo.server.aigrade.internal.engine.service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import vn.geo.server.geometry.api.AnswerMatch;

/**
 * Tín hiệu engine (US-S5E6-03; BR-S5E6-08, 09): lấy đáp số cuối từ bản đọc chữ (dòng kết luận,
 * KS-3: "Vậy …", "Kết luận …", "Đáp số …", hoặc dòng khoảng cách "d = …", "d ≈ …", "d(A,(SBC)) =
 * …") rồi so với đáp số engine bằng KS-1 (biểu thức tương đương, dung sai {@link AnswerMatch}, xét
 * a = 1, 2, 3 khi có tham số) và KS-2 (số thập phân là đáp số đã làm tròn tới ≥ 2 chữ số sau dấu
 * phẩy, giữ đúng phụ thuộc vào a). Hàm thuần, không gọi AI.
 */
public final class EngineMatcher {

  public static final String MATCH = "match";
  public static final String MISMATCH = "mismatch";
  public static final String NOT_FOUND = "not_found";
  public static final String NO_ENGINE = "no_engine";

  /** Ghi chú khi không thấy dòng kết luận (US-S5E6-03-FC2). */
  public static final String NOTE_NOT_FOUND = "Không thấy đáp số cuối trong bản đọc chữ";

  private static final Pattern CONCLUSION =
      Pattern.compile(
          "^((vay|ket luan|kl|dap so|ds|kq)\\b|d\\s*[(=≈]).*", Pattern.CASE_INSENSITIVE);

  private static final double[] AS = {1, 2, 3};

  private EngineMatcher() {}

  /** Kết quả so: trạng thái, đáp số trong bài (đã tách), luật khớp (KS-1/KS-2 hoặc null). */
  public record Result(String status, String studentAnswer, String rule) {}

  /** Nhãn hiển thị của trạng thái. */
  public static String label(String status) {
    return switch (status) {
      case MATCH -> "Khớp";
      case MISMATCH -> "Lệch";
      case NOT_FOUND -> "Không tìm thấy";
      default -> "Không có đáp số engine";
    };
  }

  /** So đáp số cuối trong các dòng bản đọc chữ (theo thứ tự) với đáp số engine. */
  public static Result compare(List<String> lines, String engineAnswer) {
    String answer = finalAnswer(lines);
    if (engineAnswer == null || engineAnswer.isBlank()) {
      return new Result(NO_ENGINE, answer, null);
    }
    if (answer == null) {
      return new Result(NOT_FOUND, null, null);
    }
    String rule = match(answer, engineAnswer);
    return new Result(rule == null ? MISMATCH : MATCH, answer, rule);
  }

  /**
   * Đáp số cuối: vế phải (sau "=" hoặc "≈" cuối cùng) của dòng kết luận cuối; null nếu không có.
   */
  public static String finalAnswer(List<String> lines) {
    List<String> all = new ArrayList<>();
    for (String l : lines) {
      if (l != null) {
        for (String part : l.split("\\R")) {
          all.add(part.strip());
        }
      }
    }
    for (int i = all.size() - 1; i >= 0; i--) {
      String line = all.get(i);
      if (!CONCLUSION.matcher(plain(line)).matches()) {
        continue;
      }
      int cut = Math.max(line.lastIndexOf('='), line.lastIndexOf('≈'));
      String rhs = cut >= 0 ? line.substring(cut + 1) : line.replaceFirst("^\\S+\\s*", "");
      rhs = rhs.strip().replaceAll("[.;:]+$", "").strip();
      if (rhs.length() > 200) {
        rhs = rhs.substring(0, 200);
      }
      return rhs.isEmpty() ? null : rhs;
    }
    return null;
  }

  /** "KS-1" | "KS-2" khi khớp, null khi lệch hoặc không phân tích được. */
  public static String match(String given, String engine) {
    try {
      List<AnswerMatch.Quantity> e = AnswerMatch.parse(engine);
      List<AnswerMatch.Quantity> g = AnswerMatch.parse(given);
      if (e.isEmpty() || g.isEmpty()) {
        return null;
      }
      boolean ks2 = false;
      for (int i = 0; i < e.size(); i++) {
        AnswerMatch.Quantity eq = e.get(i);
        AnswerMatch.Quantity gq = AnswerMatch.pick(g, eq.name(), i);
        if (gq == null) {
          return null;
        }
        if (ks1(gq, eq)) {
          continue;
        }
        if (ks2(gq, eq)) {
          ks2 = true;
          continue;
        }
        return null;
      }
      return ks2 ? "KS-2" : "KS-1";
    } catch (RuntimeException ex) {
      return null;
    }
  }

  private static boolean ks1(AnswerMatch.Quantity g, AnswerMatch.Quantity e) {
    boolean param = e.dependsOnA() || g.dependsOnA();
    for (double a : param ? AS : new double[] {1}) {
      double c = e.valueAt(a);
      double x = g.valueAt(a);
      if (!Double.isFinite(x) || !AnswerMatch.close(x, c, param, e.decimals())) {
        return false;
      }
    }
    return true;
  }

  /** Số đã làm tròn (≥ 2 chữ số thập phân): khớp từng giá trị a theo nửa đơn vị chữ số cuối. */
  private static boolean ks2(AnswerMatch.Quantity g, AnswerMatch.Quantity e) {
    if (g.decimals() < 2 || e.dependsOnA() != g.dependsOnA()) {
      return false;
    }
    double half = 0.5 * Math.pow(10, -g.decimals()) + AnswerMatch.EPS;
    double c1 = e.valueAt(1);
    double g1 = g.valueAt(1);
    if (Math.abs(g1 - c1) > half) {
      return false;
    }
    if (!e.dependsOnA()) {
      return true;
    }
    // cùng dạng phụ thuộc a: tỉ số giá trị tại a = 2, 3 so với a = 1 phải như nhau
    for (double a : new double[] {2, 3}) {
      double rc = e.valueAt(a) / c1;
      double rg = g.valueAt(a) / g1;
      if (!AnswerMatch.close(rg, rc, true, 0)) {
        return false;
      }
    }
    return true;
  }

  /** Cờ "AI và engine lệch" (BR-S5E6-09) theo điểm Kết quả của đề xuất; null = bài chứng minh. */
  public static boolean disagree(String status, BigDecimal result, BigDecimal resultMax) {
    if (result == null || resultMax == null || resultMax.signum() <= 0) {
      return false;
    }
    int cmp = result.multiply(BigDecimal.valueOf(2)).compareTo(resultMax);
    return (MATCH.equals(status) && cmp < 0) || (MISMATCH.equals(status) && cmp > 0);
  }

  static String plain(String s) {
    String n = Normalizer.normalize(s.replace('đ', 'd').replace('Đ', 'D'), Normalizer.Form.NFD);
    return n.replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).strip();
  }
}
