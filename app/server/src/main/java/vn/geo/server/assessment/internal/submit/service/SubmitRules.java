package vn.geo.server.assessment.internal.submit.service;

import java.time.Duration;
import java.time.Instant;

/**
 * Luật thuần của nộp bài (US-S5E4-03, 09): trần trang và cỡ ảnh (BR-S5E4-07, QS5-19), ngưỡng độ rõ,
 * nhãn nộp muộn theo giờ máy chủ (BR-S5E4-10), câu chữ MSG-S5-32…36.
 */
public final class SubmitRules {

  /** Trần một trang sau khi máy nén: 8 MB (8 388 608 byte). */
  public static final int MAX_PAGE_BYTES = 8 * 1024 * 1024;

  /** Mỗi bài nộp 1–10 trang. */
  public static final int MAX_PAGES = 10;

  /** Điểm độ rõ tối thiểu (0,50 đạt, 0,49 không đạt). */
  public static final double MIN_CLARITY = 0.5;

  /** Đoạn tải tối đa mỗi yêu cầu (thân yêu cầu máy chủ ≤ 1 MB). */
  public static final int PART_BYTES = 1_000_000;

  /** Phiên bản văn bản thông báo riêng tư S5 hiện hành (BR-S5E4-11). */
  public static final String PRIVACY_VERSION = "privacy-s5-1.0";

  public static final String MSG_32 = "Ảnh trang %d hơi mờ. Em chụp lại trang này nhé.";
  public static final String MSG_34 = "Đã quá hạn nộp. Thầy cô không nhận bài muộn cho bài này.";
  public static final String MSG_36 =
      "Nhớ: không ghi họ tên, số điện thoại lên bài. Ảnh chỉ thầy cô lớp em xem, được xử lý bằng AI"
          + " để đọc chữ và đề xuất điểm, và bị xoá cuối năm học.";
  public static final String MSG_RETURNED = "Bài đã được trả. Không nhận bài nộp mới.";
  public static final String MSG_PRIVACY = "Em cần đọc thông báo riêng tư và bấm \"Em đã hiểu\".";

  private SubmitRules() {}

  /** Độ rõ đạt ngưỡng (null = máy không chấm, coi là đạt; máy chủ không tự chấm độ rõ). */
  public static boolean clear(Double clarity) {
    return clarity == null || clarity >= MIN_CLARITY;
  }

  /** Số giây muộn so với hạn (null = đúng hạn; đúng 20:00:00 là đúng hạn, 20:00:01 là muộn). */
  public static Integer lateSeconds(Instant dueAt, Instant at) {
    long s = Duration.between(dueAt, at).getSeconds();
    return s > 0 ? (int) Math.min(Integer.MAX_VALUE, s) : null;
  }

  /** Nhãn "Nộp muộn n phút" (làm tròn lên phút; ≥ 60 phút ghi giờ và phút); null khi đúng hạn. */
  public static String lateLabel(Integer lateSeconds) {
    if (lateSeconds == null || lateSeconds <= 0) {
      return null;
    }
    long min = (lateSeconds + 59) / 60;
    if (min < 60) {
      return "Nộp muộn " + min + " phút";
    }
    long h = min / 60;
    long m = min % 60;
    return "Nộp muộn " + h + " giờ" + (m == 0 ? "" : " " + m + " phút");
  }

  /** MSG-S5-32 cho trang {@code pageNo}. */
  public static String blurry(int pageNo) {
    return String.format(MSG_32, pageNo);
  }
}
