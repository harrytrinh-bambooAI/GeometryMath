package vn.geo.server.assessment.internal.review.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Luật chấm tay (US-S5E4-06, BR-S5E4-17): điểm mỗi tiêu chí 0…tối đa, bước 0,25; nhận xét ≤ 1 000
 * ký tự; câu chữ MSG-S5-37, MSG-S5-38, MSG-S5-39 (T-261128-60). Hàm thuần.
 */
public final class ReviewRules {

  public static final int COMMENT_MAX = 1000;
  public static final long UNDO_SECONDS = 15 * 60; // QS5-31
  public static final String MSG_38 = "Đã duyệt điểm. Học sinh sẽ thấy khi thầy cô bấm Trả bài.";
  public static final String MSG_WAIT_RETURN = "Đang chờ thầy cô trả bài";
  public static final String MSG_WAIT_GRADE = "Đang chờ thầy cô chấm";
  public static final String MSG_LOCKED = "Lời giải mở khi thầy cô trả bài của em.";

  private static final BigDecimal FOUR = BigDecimal.valueOf(4);

  private ReviewRules() {}

  /** Điểm hợp lệ: 0 ≤ p ≤ max và p là bội 0,25. */
  public static boolean valid(BigDecimal p, BigDecimal max) {
    if (p == null || max == null || p.signum() < 0 || p.compareTo(max) > 0) {
      return false;
    }
    BigDecimal q = p.multiply(FOUR);
    return q.compareTo(q.setScale(0, RoundingMode.DOWN)) == 0;
  }

  /** MSG-S5-37 "Điểm tiêu chí {tên} phải từ 0 đến {tối đa}, bước 0,25." */
  public static String msg37(String name, BigDecimal max) {
    return "Điểm tiêu chí " + name + " phải từ 0 đến " + vn(max) + ", bước 0,25.";
  }

  /** MSG-S5-39 "Em có bài mới được trả: {tên bài}." */
  public static String msg39(String title) {
    return clip("Em có bài mới được trả: " + title + ".");
  }

  /** Sửa điểm bài đã trả (BR-S5E4-24). */
  public static String updated(String title) {
    return clip("Điểm bài " + title + " đã được thầy cô cập nhật");
  }

  /** Số kiểu Việt Nam: 3,5 · 10 · 0,25. */
  public static String vn(BigDecimal x) {
    return x.stripTrailingZeros().toPlainString().replace('.', ',');
  }

  /** Tóm tắt hàng chờ "4 chờ chấm · 1 chưa nộp · 0 đã duyệt". */
  public static String summary(int waiting, int notSubmitted, int approved) {
    return waiting + " chờ chấm · " + notSubmitted + " chưa nộp · " + approved + " đã duyệt";
  }

  private static String clip(String s) {
    return s.length() <= 240 ? s : s.substring(0, 239) + "…";
  }
}
