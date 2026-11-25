package vn.geo.server.learning.internal.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import vn.geo.server.geometry.api.AnswerMatch;

/**
 * Quy tắc của Kiểm soát đáp án (E6, US-GEO-609; ADR-GEO-14, D-GEO-065, 156, 184). Hàm thuần, không
 * truy cập dữ liệu: nhận đáp số, các bước và dạng tương đương của lời giải ĐANG KHÓA, trả mã quy
 * tắc chặn hoặc null.
 *
 * <ul>
 *   <li>R1 câu hỏi đòi đáp số, giá trị hay bước giải (BR-GEO-253).
 *   <li>R2 (KS-1) dạng viết tương đương: nguyên văn, bỏ khoảng trắng/dấu gạch (đáp số viết từng
 *       chữ), radian, độ bằng chữ, tiếng Anh.
 *   <li>R3 (KS-2) giá trị số theo dung sai: số bằng đáp số, hoặc là đáp số đã làm tròn tới ≥ 2 chữ
 *       số có nghĩa (D-GEO-184: "0,79", "0,785" cho π/4).
 *   <li>R4 (KS-3) đáp số ngắn như "a" chỉ chặn khi đứng làm kết luận ("… bằng a.").
 *   <li>R5 (KS-4) câu chỉ xác nhận hay phủ nhận một giá trị học sinh đưa ra.
 *   <li>R6 bước của lời giải đang khóa: nguyên văn bước, từng câu của bước, kết luận "vế = vế".
 * </ul>
 */
public final class AnswerRules {

  /** Lời giải đang khóa cần bảo vệ. */
  public record Locked(String finalAnswer, String[] stepBodies, List<String> forms) {}

  private static final Pattern NUM = Pattern.compile("\\d+(?:[.,]\\d+)?");
  private static final Pattern DEG = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*(?:°|độ)");
  private static final Pattern UNIT =
      Pattern.compile("\\s*(cm²|cm³|cm2|cm3|cm|dm|m²|m³|m|đvdt|đvtt|độ|°)\\s*$");
  private static final Map<String, List<String>> SPECIAL_DEG =
      Map.of(
          "30", List.of("π/6", "pi/6", "ba mươi độ", "thirty degrees"),
          "45", List.of("π/4", "pi/4", "bốn mươi lăm độ", "forty-five degrees", "forty five"),
          "60", List.of("π/3", "pi/3", "sáu mươi độ", "sixty degrees"),
          "90", List.of("π/2", "pi/2", "chín mươi độ", "ninety degrees"),
          "120", List.of("2π/3", "2pi/3", "một trăm hai mươi độ"));

  /** Câu hỏi đòi đáp số/giá trị/bước (so trên chữ không dấu, chữ thường). */
  private static final Pattern ASKS_ANSWER =
      Pattern.compile(
          "dap so|dap an|ket qua|bang bao nhieu|bao nhieu do|la bao nhieu|la may|giai giup|giai ho"
              + "|tung buoc|loi giai|giai bai|bo dong cuoi|(dang|bang|sang|ra|theo) radian"
              + "|tan cua goc can tim|chu so (dau|thu|cuoi|tiep|hang)"
              + "|don vi la gi|lon hon hay nho hon|what is|co phai .*khong|dua loi giai"
              + "|in loi giai|hien loi giai");

  // soát E6 m1: bỏ "radian", "vuong can", "chu so", "the angle" đứng một mình (câu khái niệm như
  // "Radian là gì?", "Tam giác vuông cân có tính chất gì?" được hỏi khi khóa; BR-GEO-252/253). Câu
  // xác nhận "… có phải vuông cân không?" vẫn chặn qua "co phai .*khong".

  /** Số trong câu hỏi chỉ là số thứ tự câu/bài/bước, không phải giá trị đưa ra (soát E6 m2). */
  private static final Pattern NUMBERING =
      Pattern.compile("\\b(cau|bai|phan|y|buoc|goi y|vi du|trang|muc)\\s*\\d+[a-z]?\\b");

  private static final Pattern CONFIRM_HEAD =
      Pattern.compile(
          "^(dung|dung roi|chinh xac|phai|khong phai|khong|sai|chua dung|yes|no|vang|da)\\b");
  private static final Pattern COMPARE = Pattern.compile("\\b(lon hon|nho hon|bang dung)\\b");

  private AnswerRules() {}

  /** Mã quy tắc chặn (R1…R6) hoặc null khi câu trả lời được phép. */
  public static String check(String question, String reply, Locked lk) {
    String q = question == null ? "" : question;
    String r = reply == null ? "" : reply;
    if (ASKS_ANSWER.matcher(plain(q)).find()) {
      return "R1";
    }
    String v = valueLeak(r, lk);
    if (v != null) {
      return v;
    }
    if (confirms(q, r)) {
      return "R5";
    }
    String rn = norm(r);
    for (String s : stepTerms(lk.stepBodies())) {
      String sn = norm(s);
      if (sn.length() >= 6 && rn.contains(sn)) {
        return "R6";
      }
    }
    return null;
  }

  /**
   * Chỉ phần dựa trên giá trị (R2, R3, R4) của {@link #check}, không xét câu hỏi: dùng để đếm lộ
   * trong bộ câu thử (Dev-E6-03), bắt cả dạng viết/làm tròn chứ không chỉ chuỗi nguyên văn.
   */
  public static String valueLeak(String text, Locked lk) {
    String r = text == null ? "" : text;
    String rn = norm(r);
    String rs = squash(r);
    for (String f : forms(lk)) {
      String fn = norm(f);
      String fs = squash(f);
      if (DIGITS.matcher(fn).matches()) {
        // dạng toàn chữ số ("45"): khớp theo ranh giới số, không khớp "450", "2045" (soát E6 m3)
        if (numberAt(r, fn)) {
          return "R2";
        }
        continue;
      }
      if ((fn.length() >= 2 && rn.contains(fn)) || (fs.length() >= 3 && rs.contains(fs))) {
        return "R2";
      }
    }
    if (numberLeak(r, values(lk))) {
      return "R3";
    }
    return shortConclusion(r, lk.finalAnswer()) ? "R4" : null;
  }

  private static final Pattern DIGITS = Pattern.compile("\\d+(?:[.,]\\d+)?");

  /** Văn bản có số đứng riêng bằng đúng chuỗi số n (không là một phần của số dài hơn). */
  static boolean numberAt(String text, String n) {
    Matcher m = NUM.matcher(text);
    String want = n.replace(',', '.');
    while (m.find()) {
      if (m.group().replace(',', '.').equals(want)) {
        return true;
      }
    }
    return false;
  }

  // ---------------------------------------------------------------- dạng viết (R2)
  static Set<String> forms(Locked lk) {
    Set<String> out = new LinkedHashSet<>();
    if (lk.forms() != null) {
      out.addAll(lk.forms());
    }
    String ans = lk.finalAnswer();
    if (ans == null || ans.isBlank()) {
      return out;
    }
    for (String part : ans.split(";")) {
      String v = part.contains("=") ? part.substring(part.lastIndexOf('=') + 1) : part;
      v = v.strip();
      if (v.length() < 2) {
        continue; // đáp số ngắn ("a"): chỉ R4
      }
      out.add(v);
      String bare = UNIT.matcher(v).replaceAll("").strip();
      if (bare.length() >= 2) {
        out.add(bare);
        if (bare.contains("π")) {
          out.add(bare.replace("π", "pi")); // "15 pi", "15pi" (soát E6 M3)
        }
      }
      Matcher d = DEG.matcher(v);
      if (d.find()) {
        String n = d.group(1).replace(',', '.');
        out.add(n + "°");
        out.add(n + " độ");
        out.add(n + " degrees");
        out.addAll(SPECIAL_DEG.getOrDefault(n, List.of()));
      }
    }
    return out;
  }

  // ---------------------------------------------------------------- số (R3)
  /** Giá trị số của đáp số: số đo độ, radian tương ứng, giá trị của biểu thức (15π, 24√3…). */
  static List<Double> values(Locked lk) {
    List<Double> out = new ArrayList<>();
    String ans = lk.finalAnswer();
    if (ans == null) {
      return out;
    }
    for (String part : ans.split(";")) {
      String v = part.contains("=") ? part.substring(part.lastIndexOf('=') + 1) : part;
      v = v.strip();
      Matcher d = DEG.matcher(v);
      if (d.find()) {
        double deg = Double.parseDouble(d.group(1).replace(',', '.'));
        out.add(deg);
        out.add(Math.toRadians(deg));
        continue;
      }
      String bare = UNIT.matcher(v).replaceAll("").strip();
      try {
        double x = new AnswerMatch.Quantity("", bare, 0).valueAt(1);
        if (Double.isFinite(x)) {
          out.add(x);
        }
      } catch (RuntimeException e) {
        // biểu thức chứa ẩn (a, R…): không có giá trị số cố định
      }
    }
    return out;
  }

  static boolean numberLeak(String reply, List<Double> values) {
    if (values.isEmpty()) {
      return false;
    }
    Matcher m = NUM.matcher(reply);
    while (m.find()) {
      String s = m.group().replace(',', '.');
      double x = Double.parseDouble(s);
      int dot = s.indexOf('.');
      int decimals = dot < 0 ? 0 : s.length() - dot - 1;
      int sig = s.replace(".", "").replaceFirst("^0+", "").length();
      for (double v : values) {
        if (Math.abs(x - v) < 1e-9) {
          return true; // đúng bằng đáp số (45, 12…)
        }
        if (sig >= 2 && decimals > 0 && Math.abs(x - v) < Math.pow(10, -decimals) - 1e-12) {
          // đáp số đã làm tròn (D-GEO-184) hoặc cắt cụt ("0,78" cho 0,7854; soát E6 m4) tới ≥ 2 chữ
          // số có nghĩa
          return true;
        }
      }
    }
    return false;
  }

  // ---------------------------------------------------------------- đáp số ngắn (R4)
  static boolean shortConclusion(String reply, String answer) {
    if (answer == null) {
      return false;
    }
    for (String part : answer.split(";")) {
      String v = part.contains("=") ? part.substring(part.lastIndexOf('=') + 1) : part;
      v = v.strip();
      if (v.isEmpty() || v.length() >= 2) {
        continue;
      }
      Pattern p =
          Pattern.compile(
              "(bằng|=|là|chính là|equals?|is)\\s*"
                  + Pattern.quote(v)
                  + "\\s*([.,;!?)]|$|\\s+(cm|đơn vị))",
              Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
      if (p.matcher(reply).find()) {
        return true;
      }
    }
    return false;
  }

  // ---------------------------------------------------------------- xác nhận / phủ nhận (R5)
  static boolean confirms(String question, String reply) {
    String q = plain(question);
    String qv = NUMBERING.matcher(q).replaceAll(" ");
    boolean offersValue = NUM.matcher(qv).find() || q.contains("vuong can") || q.contains("bang a");
    if (!offersValue) {
      return false;
    }
    String r = plain(reply).replaceAll("^[\\[\\]\\p{Punct}\\s]+", "");
    return CONFIRM_HEAD.matcher(r).find() || COMPARE.matcher(r).find();
  }

  // ---------------------------------------------------------------- bước giải (R6)
  static List<String> stepTerms(String[] bodies) {
    List<String> out = new ArrayList<>();
    if (bodies == null) {
      return out;
    }
    for (String body : bodies) {
      if (body == null || body.isBlank()) {
        continue;
      }
      out.add(body);
      for (String sentence : body.split("[.;]\\s|\\.$")) {
        if (sentence.strip().length() >= 12) {
          out.add(sentence.strip());
        }
      }
      for (String clause : body.split("[,;.]|suy ra|Vậy")) {
        String[] parts = clause.split("=");
        if (parts.length >= 2) {
          String lhs = parts[0].strip();
          String rhs = parts[parts.length - 1].strip();
          if (!lhs.isEmpty() && lhs.length() <= 24 && !rhs.isEmpty()) {
            out.add(lhs + " = " + rhs);
          }
        }
      }
    }
    return out;
  }

  // ---------------------------------------------------------------- chuẩn hóa
  /** NFC, chữ thường, bỏ khoảng trắng. */
  static String norm(String s) {
    return Normalizer.normalize(s, Normalizer.Form.NFC)
        .toLowerCase(Locale.ROOT)
        .replaceAll("\\s+", "");
  }

  /** Như norm và bỏ mọi dấu câu, gạch nối (đáp số viết tách từng chữ "b-ố-n m-ư-ơ-i …"). */
  static String squash(String s) {
    return norm(s).replaceAll("[\\p{Punct}‐-―…“”‘’]", "");
  }

  /** Không dấu, chữ thường, khoảng trắng gộp: để so ý định của câu hỏi. */
  static String plain(String s) {
    String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
    return n.replace('đ', 'd')
        .replace('Đ', 'd')
        .toLowerCase(Locale.ROOT)
        .replaceAll("\\s+", " ")
        .strip();
  }
}
