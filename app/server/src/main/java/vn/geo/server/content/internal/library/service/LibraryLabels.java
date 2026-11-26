package vn.geo.server.content.internal.library.service;

/**
 * Câu chữ hiện cho người dùng của Kho đề S2-E6 và Trình chiếu S2-E7 (nguyên văn theo stories_ac).
 */
public final class LibraryLabels {

  private LibraryLabels() {}

  public static final String APPROVE_FIRST = "Hãy duyệt lời giải trước";
  public static final String PENDING_LIMIT = "Tối đa 10 đề chờ soát cùng lúc";
  public static final String SELF_REVIEW = "Bạn không thể soát đề của chính mình";
  public static final String REASON_RANGE = "Lý do từ 5 đến 500 ký tự";
  public static final String ALREADY_IN = "Đề đã vào Kho chung";
  public static final String ALREADY_PENDING = "Đề đang chờ soát";
  public static final String STUDENT_LOCKED = "Lời giải mở theo bài giao của thầy cô";
  public static final String CREATE_LIMIT =
      "Bạn đã thêm 20 đề trong 60 phút. Vui lòng thử lại sau.";
  public static final String DEFAULT_HINT =
      "Vẽ lại hình, đánh dấu các dữ kiện đề cho rồi tìm mối liên hệ giữa chúng.";

  public static String kind(String kind) {
    return "plane".equals(kind) ? "Phẳng" : "Không gian";
  }

  public static String machine(String status) {
    if (status == null) {
      return "Chưa kiểm";
    }
    return "verified".equals(status) ? "Đã kiểm chứng" : "Cần xem lại";
  }

  public static String human(String status) {
    if (status == null) {
      return "Chưa có lời giải";
    }
    return switch (status) {
      case "ai_drafted" -> "AI soạn";
      case "editing" -> "Đang sửa";
      case "teacher_approved" -> "Giáo viên đã duyệt";
      case "superseded" -> "Bị thay thế";
      default -> status;
    };
  }

  /** Trạng thái đề kho (BR-S2E6-06). null = chưa gửi soát. */
  public static String status(String status) {
    if (status == null) {
      return "mine";
    }
    return status;
  }

  public static String statusLabel(String status) {
    if (status == null) {
      return "Của tôi";
    }
    return switch (status) {
      case "pending" -> "Chờ soát";
      case "accepted" -> "Đã vào Kho chung";
      case "rejected" -> "Bị từ chối";
      default -> "Của tôi";
    };
  }
}
