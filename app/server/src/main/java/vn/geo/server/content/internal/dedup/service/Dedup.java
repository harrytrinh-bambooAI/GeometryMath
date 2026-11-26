package vn.geo.server.content.internal.dedup.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.HexFormat;
import java.util.Locale;

/**
 * S2-E4 (US-S2E4-03, BR-S2E4-11): hàm thuần đối chiếu trùng. Chuẩn hóa chữ đề (cắt khoảng trắng
 * thừa, chữ thường, bỏ dấu câu cuối) rồi so: bằng nhau = "đã có trong kho"; độ giống từ 0,90 đến
 * dưới 1,00 = "gần giống" (chỉ báo, không chặn; ngưỡng đã chốt PO-66, QA-S2E4-05).
 */
public final class Dedup {

  private Dedup() {}

  /** Ngưỡng "gần giống" (BR-S2E4-11). */
  public static final double SIMILAR_FROM = 0.90;

  public static final String EXISTS_LABEL = "đã có trong kho";
  public static final String UPDATE_LABEL = "cập nhật khi lưu";

  /** Kết quả đối chiếu một cặp đề. */
  public enum Match {
    NONE,
    SIMILAR,
    EXISTS
  }

  /** Chuẩn hóa: NFC, chữ thường, gộp khoảng trắng, bỏ dấu câu và khoảng trắng ở cuối. */
  public static String normalize(String s) {
    if (s == null) {
      return "";
    }
    String t = Normalizer.normalize(s, Normalizer.Form.NFC).toLowerCase(Locale.ROOT);
    t = t.replaceAll("[\\s\\u00A0]+", " ").strip();
    int end = t.length();
    while (end > 0 && ".,;:!?…".indexOf(t.charAt(end - 1)) >= 0
        || end > 0 && t.charAt(end - 1) == ' ') {
      end--;
    }
    return t.substring(0, end);
  }

  /** Khóa so trùng tuyệt đối: sha-256 (hex) của chữ đã chuẩn hóa. */
  public static String key(String s) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(md.digest(normalize(s).getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  /** Phân loại theo độ giống (0…1): dưới 0,90 không ghi chú; 0,90…dưới 1 gần giống; 1 là trùng. */
  public static Match classify(double similarity) {
    if (similarity >= 1.0 - 1e-9) {
      return Match.EXISTS;
    }
    return similarity >= SIMILAR_FROM - 1e-9 ? Match.SIMILAR : Match.NONE;
  }

  /**
   * Ghi chú hiện cho giáo viên; null khi không có gì để báo. {@code ref} = mã hoặc tên đề trong
   * kho.
   */
  public static String label(Match m, String ref) {
    return switch (m) {
      case EXISTS -> EXISTS_LABEL;
      case SIMILAR -> "gần giống bài " + ref + ", xem?";
      default -> null;
    };
  }

  /** Độ giống của hai chữ đề THÔ (chuẩn hóa rồi so). */
  public static double similarityRaw(String a, String b) {
    return similarity(normalize(a), normalize(b));
  }

  /**
   * Độ giống của hai chuỗi ĐÃ chuẩn hóa: 1 − khoảng cách sửa (Levenshtein) / độ dài chuỗi dài hơn.
   * Cặp chắc chắn dưới ngưỡng (lệch độ dài quá 10 %) trả 0 mà không tính; khoảng cách vượt ngưỡng
   * dừng sớm.
   */
  public static double similarity(String a, String b) {
    if (a.equals(b)) {
      return 1.0;
    }
    int max = Math.max(a.length(), b.length());
    if (max == 0) {
      return 1.0;
    }
    int budget = (int) Math.floor(max * (1.0 - SIMILAR_FROM) + 1e-9);
    if (Math.abs(a.length() - b.length()) > budget) {
      return 0.0;
    }
    int d = boundedDistance(a, b, budget);
    return d < 0 ? 0.0 : 1.0 - (double) d / max;
  }

  /** Khoảng cách sửa nếu không quá {@code budget}; −1 nếu vượt. */
  static int boundedDistance(String a, String b, int budget) {
    int n = a.length();
    int m = b.length();
    if (Math.abs(n - m) > budget) {
      return -1;
    }
    // Chỉ tính dải |i − j| ≤ budget (ô ngoài dải chắc chắn vượt budget): O(n · budget) thay vì O(n
    // · m).
    final int inf = budget + 1;
    int[] prev = new int[m + 1];
    int[] cur = new int[m + 1];
    for (int j = 0; j <= m; j++) {
      prev[j] = j <= budget ? j : inf;
    }
    for (int i = 1; i <= n; i++) {
      int from = Math.max(1, i - budget);
      int to = Math.min(m, i + budget);
      cur[from - 1] = from == 1 ? Math.min(i, inf) : inf;
      int rowMin = cur[from - 1];
      char ca = a.charAt(i - 1);
      for (int j = from; j <= to; j++) {
        int cost = ca == b.charAt(j - 1) ? 0 : 1;
        int v = Math.min(Math.min(prev[j] + 1, cur[j - 1] + 1), prev[j - 1] + cost);
        if (v > inf) {
          v = inf;
        }
        cur[j] = v;
        if (v < rowMin) {
          rowMin = v;
        }
      }
      if (to < m) {
        cur[to + 1] = inf;
      }
      if (rowMin > budget) {
        return -1;
      }
      int[] t = prev;
      prev = cur;
      cur = t;
    }
    return prev[m] > budget ? -1 : prev[m];
  }
}
