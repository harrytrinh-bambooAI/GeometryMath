package vn.geo.server.tracking.internal.classboard.domain;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * S3-E3 — quy tắc thuần của Kết quả lớp: trạng thái ô (BR-S3E3-02), thứ tự hàng theo chữ cái tiếng
 * Việt (BR-S3E3-04), che chuỗi giống email / số điện thoại trước khi gửi bộ gộp (BR-S3E3-36), lọc
 * câu nhận định có phần trăm / xếp hạng (BR-S3E3-03, 33).
 */
public final class BoardRules {

  public static final String CORRECT = "correct";
  public static final String REVIEW = "review";
  public static final String DOING = "doing";
  public static final String TODO = "todo";

  public static final String MASK = "[đã ẩn]";

  /** Hai nhãn cố định của mọi thẻ nhận định (MSG-S3-48). */
  public static final List<String> LABELS = List.of("AI gộp", "Cần thầy cô xác nhận");

  private static final Pattern EMAIL =
      Pattern.compile("[\\p{L}0-9._%+-]+@[\\p{L}0-9.-]+\\.[\\p{L}]{2,}");
  private static final Pattern PHONE = Pattern.compile("(?<![0-9])(\\+?84|0)(?:[ .-]?[0-9]){8,10}");
  private static final Pattern USER_CODE =
      Pattern.compile("\\b(GV|HS|HK|RC)-[A-Z0-9]{4}(-[A-Z0-9]{4}){0,2}\\b");
  private static final Pattern RANKING =
      Pattern.compile(
          "%|phần trăm|xếp hạng|thứ hạng|đứng đầu|đứng cuối|(khó|dễ|tốt|kém|giỏi|yếu|cao|thấp) nhất"
              + "|đã kiểm chứng",
          Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern TITLE_PREFIX = Pattern.compile("^Đề\\s+\\d+\\s*[·:.-]?\\s*");

  private BoardRules() {}

  /** Trạng thái một ô: Đúng thắng, rồi Xem lại (đã gửi đáp số chưa khớp), Đang làm, Chưa làm. */
  public static String status(boolean anyWork, boolean anyCorrect, boolean anyWrong) {
    if (anyCorrect) {
      return CORRECT;
    }
    if (anyWrong) {
      return REVIEW;
    }
    return anyWork ? DOING : TODO;
  }

  /** So tên hiển thị theo bảng chữ cái tiếng Việt (D trước Đ), hòa thì theo mã người dùng. */
  public static <T> Comparator<T> byVietnameseName(
      Function<T, String> name, Function<T, String> code) {
    Collator vi = Collator.getInstance(Locale.forLanguageTag("vi"));
    vi.setStrength(Collator.TERTIARY);
    return (a, b) -> {
      int c = vi.compare(nz(name.apply(a)), nz(name.apply(b)));
      return c != 0 ? c : nz(code.apply(a)).compareTo(nz(code.apply(b)));
    };
  }

  /** Che chuỗi giống email, số điện thoại, mã người dùng / mã lớp thành {@value #MASK}. */
  public static String mask(String text) {
    if (text == null) {
      return "";
    }
    String s = EMAIL.matcher(text).replaceAll(MASK);
    s = PHONE.matcher(s).replaceAll(MASK);
    return USER_CODE.matcher(s).replaceAll(MASK);
  }

  /**
   * Bỏ các câu mang phần trăm, xếp hạng toàn lớp hay nhãn "Đã kiểm chứng" khỏi câu nhận định; trả
   * chuỗi rỗng nếu không còn gì.
   */
  public static String cleanInsight(String text) {
    if (text == null) {
      return "";
    }
    List<String> keep = new ArrayList<>();
    for (String sentence : text.strip().split("(?<=[.!?;])\\s+")) {
      if (!sentence.isBlank() && !RANKING.matcher(sentence).find()) {
        keep.add(sentence.strip());
      }
    }
    String out = String.join(" ", keep);
    return out.length() > 300 ? out.substring(0, 300) : out;
  }

  /** "Đề 3 hai mặt phẳng" → "hai mặt phẳng" (cột hiện "Đề {vị trí} · {tên}"). */
  public static String shortTitle(String title) {
    String t = title == null ? "" : TITLE_PREFIX.matcher(title.strip()).replaceFirst("");
    return t.isBlank() ? nz(title) : t;
  }

  private static String nz(String s) {
    return s == null ? "" : s;
  }
}
