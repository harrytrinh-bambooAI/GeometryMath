package vn.geo.server.learning.internal.service;

/** Thông điệp của E4 (nguyên văn work/05_dictionary.md, chép ở đầu stories_ac.md của E4). */
public final class HelpMessages {

  public static final String MSG_30 =
      "Ở chế độ tự học, em xem gợi ý 1 và 2 trước, sau đó mới mở được lời giải.";
  public static final String MSG_31 =
      "Đề này trùng với bài lớp em đang làm. Lời giải mở theo giờ giáo viên đặt.";
  public static final String MSG_32 = "Giáo viên đã tắt gợi ý cho bài này.";
  public static final String MSG_33 = "Giáo viên đã tắt hỏi đáp cho bài này.";
  public static final String MSG_34 = "Em xem Gợi ý 1 trước nhé.";
  public static final String MSG_35_PREFIX =
      "Lời giải đang khóa nên mình chưa nêu đáp số hay các bước giải. Em xem lại gợi ý nhé: ";
  public static final String MSG_36 =
      "Giáo trình của lớp chưa có phần này. Em hãy hỏi giáo viên nhé.";
  public static final String MSG_58 =
      "Đề này chưa dựng được bằng hình mẫu nên chưa có lời giải tự học. Em dùng gợi ý nhé.";
  public static final String RELEASE_LOCKED = "Lời giải chưa mở.";

  public static final String HINT_1_TITLE = "Gợi ý 1 · Hướng nghĩ";
  public static final String HINT_2_TITLE = "Gợi ý 2 · Bước kế tiếp";

  private HelpMessages() {}

  public static String hintTitle(int level) {
    return level == 1 ? HINT_1_TITLE : HINT_2_TITLE;
  }

  /** Câu thay thế khi lời giải còn khóa (MSG-GEO-35) kèm gợi ý cao nhất em đã xem. */
  public static String msg35(String highestSeenHint) {
    return MSG_35_PREFIX + (highestSeenHint == null ? MSG_34 : highestSeenHint);
  }

  /** Thông điệp khóa của tab "Lời giải" theo solution_state của fn_help_state. */
  public static String lockMessage(String solutionState) {
    return switch (solutionState) {
      case "hints" -> MSG_30;
      case "inherited" -> MSG_31;
      case "no_signature" -> MSG_58;
      case "open" -> null;
      default -> RELEASE_LOCKED;
    };
  }
}
