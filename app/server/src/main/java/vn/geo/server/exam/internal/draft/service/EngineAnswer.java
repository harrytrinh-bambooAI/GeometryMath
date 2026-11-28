package vn.geo.server.exam.internal.draft.service;

import java.util.Locale;

/**
 * Viết đáp số ENGINE (giá trị đo tại a = 1) thành dạng đọc được cho ô đáp số của đề nháp
 * (US-S5E2-06-FC3: "ô đáp số hiện giá trị engine"). Độ dài dạng {@code p·a·√k/q} (ví dụ {@code
 * a√6/3}, {@code 2a√5/5}) khi khớp trong 1e-9; góc theo độ; không khớp dạng căn thì số thập phân 4
 * chữ số (dấu phẩy).
 */
public final class EngineAnswer {

  private EngineAnswer() {}

  /** Bậc của a theo loại đại lượng: thể tích 3, diện tích 2, góc 0, còn lại 1. */
  static int power(String kind) {
    String k = kind == null ? "" : kind;
    if (k.startsWith("angle")) {
      return 0;
    }
    if (k.contains("volume")) {
      return 3;
    }
    if (k.contains("area") || k.contains("lateral")) {
      return 2;
    }
    return 1;
  }

  /**
   * @param value giá trị engine đo tại a = 1
   * @param kind loại đại lượng (dist_point_plane, angle_line_plane, …)
   * @param parametric đề có tham số a
   */
  public static String format(double value, String kind, boolean parametric) {
    if (Double.isNaN(value) || Double.isInfinite(value)) {
      return null;
    }
    int pw = power(kind);
    if (pw == 0) {
      double r = Math.rint(value);
      return (Math.abs(value - r) < 1e-6 ? String.valueOf((long) r) : decimal(value)) + "°";
    }
    String a = !parametric ? "" : pw == 1 ? "a" : pw == 2 ? "a²" : "a³";
    for (int q = 1; q <= 12; q++) {
      for (int k = 1; k <= 60; k++) {
        if (!squareFree(k)) {
          continue;
        }
        double p = value * q / Math.sqrt(k);
        long pi = Math.round(p);
        if (pi >= 1 && pi <= 60 && Math.abs(p - pi) < 1e-9 * Math.max(1, p) && gcd(pi, q) == 1) {
          StringBuilder sb = new StringBuilder();
          if (pi != 1 || (a.isEmpty() && k == 1)) {
            sb.append(pi);
          }
          sb.append(a);
          if (k != 1) {
            sb.append('√').append(k);
          }
          if (q != 1) {
            sb.append('/').append(q);
          }
          return sb.toString();
        }
      }
    }
    return decimal(value) + (a.isEmpty() ? "" : " (a = 1)");
  }

  private static String decimal(double v) {
    return String.format(Locale.ROOT, "%.4f", v).replace('.', ',');
  }

  private static boolean squareFree(int k) {
    for (int d = 2; d * d <= k; d++) {
      if (k % (d * d) == 0) {
        return false;
      }
    }
    return true;
  }

  private static long gcd(long x, long y) {
    return y == 0 ? x : gcd(y, x % y);
  }
}
