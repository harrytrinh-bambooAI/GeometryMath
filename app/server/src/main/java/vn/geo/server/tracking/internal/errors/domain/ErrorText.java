package vn.geo.server.tracking.internal.errors.domain;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Văn bản của tổng hợp lỗi sai (US-S5E5-02/03): gỡ tên / mã học sinh trước khi gửi AI (BR-S5E5-14),
 * kiểm nhận định AI trả (BR-S5E5-06: không tên, không phần trăm, không xếp hạng, không nhãn
 * "yếu/kém/lười"), xếp đề vào họ dạng của ngân hàng (BR-S5E5-08). Thuần, không trạng thái.
 */
public final class ErrorText {

  public static final String PLACEHOLDER = "[HS]";

  private static final Pattern USER_CODE =
      Pattern.compile("\\b(HS|GV)-[A-Z0-9]{4}(-[A-Z0-9]{4})?\\b", Pattern.CASE_INSENSITIVE);
  private static final Pattern PERCENT = Pattern.compile("%|phần trăm|phan tram|\\bpercent\\b");
  private static final Pattern RANKING =
      Pattern.compile(
          "(?<![\\p{L}])(xếp hạng|xep hang|hạng nhất|hạng bét|đứng đầu lớp|đứng cuối|top \\d+|"
              + "yếu|kém|lười|dốt|chậm hiểu)(?![\\p{L}])",
          Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

  private ErrorText() {}

  /** Chữ thường, bỏ dấu tiếng Việt (đ → d), gộp khoảng trắng. */
  public static String fold(String s) {
    String n =
        Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
    return n.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
  }

  /** Thay tên hiển thị / mã học sinh (có dấu hoặc bỏ dấu, không phân biệt hoa thường) bằng [HS]. */
  public static String scrub(String text, Collection<String> names) {
    if (text == null || text.isBlank()) {
      return "";
    }
    String out = text;
    List<String> sorted =
        names.stream()
            .filter(n -> n != null && !n.isBlank())
            .sorted((a, c) -> Integer.compare(c.length(), a.length()))
            .toList();
    for (String name : sorted) {
      out = replaceFolded(out, name, PLACEHOLDER);
    }
    return USER_CODE.matcher(out).replaceAll(PLACEHOLDER);
  }

  /** Văn bản có chứa tên/mã học sinh nào không (so bỏ dấu). */
  public static boolean mentions(String text, Collection<String> names) {
    if (USER_CODE.matcher(text == null ? "" : text).find()) {
      return true;
    }
    String f = fold(text);
    for (String n : names) {
      String fn = fold(n).trim();
      if (fn.length() >= 2
          && Pattern.compile("(?<![\\p{L}\\d])" + Pattern.quote(fn) + "(?![\\p{L}\\d])")
              .matcher(f)
              .find()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Lý do loại một nhận định (null = giữ): có tên/mã học sinh, phần trăm, xếp hạng hoặc nhãn học
   * sinh (BR-S5E5-06).
   */
  public static String reject(String text, Collection<String> names) {
    if (text == null || text.isBlank()) {
      return "rỗng";
    }
    if (mentions(text, names)) {
      return "có tên học sinh";
    }
    if (PERCENT.matcher(text.toLowerCase(Locale.ROOT)).find()) {
      return "có phần trăm";
    }
    if (RANKING.matcher(text).find()) {
      return "xếp hạng hoặc nhãn học sinh";
    }
    return null;
  }

  /** Họ dạng của ngân hàng de-thi cho một đề (null = không xếp được). Tất định, không AI. */
  public static String family(String statement) {
    String f = fold(statement);
    if (f.contains("khoang cach giua hai mat phang")) {
      return "kc-hai-mp";
    }
    if (f.contains("khoang cach tu")) {
      return "kc-diem-mp";
    }
    if (f.contains("khoang cach giua")) {
      return "kc-cheo-nhau";
    }
    if (Pattern.compile("goc giua (hai mat phang|mat phang \\(|\\()").matcher(f).find()) {
      return "goc-mp-mp";
    }
    if (f.contains("goc giua")) {
      return "goc-dt-mp";
    }
    if (f.contains("the tich")) {
      return "the-tich";
    }
    if (f.contains("dien tich xung quanh")) {
      return "dien-tich-xq";
    }
    if (f.contains("mat cau ngoai tiep")) {
      return "mat-cau-ngoai-tiep";
    }
    if (f.contains("do dai")) {
      return "do-dai";
    }
    return null;
  }

  /** Nhãn đọc của họ dạng (từ khóa tìm tài liệu trong kho). */
  public static String familyLabel(String family) {
    if (family == null) {
      return "";
    }
    if (family.startsWith("kc-")) {
      return "khoảng cách";
    }
    if (family.startsWith("goc-")) {
      return "góc";
    }
    return switch (family) {
      case "the-tich" -> "thể tích";
      case "dien-tich-xq" -> "diện tích xung quanh";
      case "mat-cau-ngoai-tiep" -> "mặt cầu ngoại tiếp";
      case "do-dai" -> "độ dài";
      default -> family;
    };
  }

  /** Độ giống Jaccard trên tập từ đã bỏ dấu (bỏ ký hiệu LaTeX). */
  public static double similarity(String a, String b) {
    Set<String> x = words(a);
    Set<String> y = words(b);
    if (x.isEmpty() || y.isEmpty()) {
      return 0;
    }
    Set<String> inter = new HashSet<>(x);
    inter.retainAll(y);
    Set<String> union = new HashSet<>(x);
    union.addAll(y);
    return (double) inter.size() / union.size();
  }

  static Set<String> words(String s) {
    String f = fold(s).replaceAll("\\\\[a-z]+", " ").replaceAll("[^a-z0-9]+", " ").trim();
    Set<String> out = new HashSet<>();
    for (String w : f.split(" ")) {
      if (!w.isEmpty()) {
        out.add(w);
      }
    }
    return out;
  }

  /** Thay theo bản bỏ dấu: NFD giữ độ dài theo ký tự gốc nên so khớp trên từng vị trí. */
  private static String replaceFolded(String text, String name, String repl) {
    String fn = fold(name).trim();
    if (fn.length() < 2) {
      return text;
    }
    StringBuilder folded = new StringBuilder();
    List<Integer> map = new ArrayList<>();
    for (int i = 0; i < text.length(); i++) {
      String f = fold(String.valueOf(text.charAt(i)));
      for (int k = 0; k < f.length(); k++) {
        folded.append(f.charAt(k));
        map.add(i);
      }
    }
    Matcher m =
        Pattern.compile("(?<![\\p{L}\\d])" + Pattern.quote(fn) + "(?![\\p{L}\\d])").matcher(folded);
    StringBuilder b = new StringBuilder();
    int last = 0;
    while (m.find()) {
      int start = map.get(m.start());
      int end = m.end() >= map.size() ? text.length() : map.get(m.end());
      b.append(text, last, start).append(repl);
      last = end;
    }
    b.append(text.substring(last));
    return b.toString();
  }
}
