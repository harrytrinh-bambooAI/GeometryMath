package vn.geo.server.knowledge.internal.kb.service;

import java.util.Map;

/** Thông điệp và nhãn của kho tri thức (S5-E3 "Thông điệp", T-261128-56). */
public final class KbMessages {

  private KbMessages() {}

  /** MSG-S5-22 (Inline, VALIDATION_FAILED). */
  public static final String MSG_22 = "Tệp tối đa 10 MB, định dạng docx, pdf hoặc md.";

  /** MSG-S5-23 (Toast). */
  public static final String MSG_23 = "Phiên bản này đã được dùng, sửa sẽ tạo phiên bản mới.";

  public static final String DUPLICATE = "Tệp này đã có trong kho.";
  public static final String NOT_APPROVED_PLAN = "Chỉ đưa vào kho giáo án đã duyệt.";
  public static final String GRANT_NOT_ACTIVE =
      "Chỉ gắn được tài liệu có phiên bản Đang dùng. Thầy cô duyệt phiên bản trước khi gắn.";
  public static final String SUBMIT_ONLY_DRAFT = "Chỉ gửi duyệt được phiên bản Nháp.";
  public static final String ACTIVATE_ONLY_PENDING =
      "Chỉ đưa lên Đang dùng phiên bản đang Chờ duyệt.";
  public static final String VALID_RANGE = "Ngày hết hiệu lực phải sau ngày bắt đầu.";
  public static final String EMPTY_CONTENT = "Nội dung tài liệu không được trống.";
  public static final String TITLE = "Tên tài liệu từ 1 đến 200 ký tự.";

  public static final String AI_LABEL = "AI soạn";
  public static final String TEACHER_LABEL = "Giáo viên đã duyệt";

  public static final Map<String, String> STATUS =
      Map.of(
          "draft", "Nháp",
          "pending", "Chờ duyệt",
          "active", "Đang dùng",
          "archived", "Lưu trữ");

  public static final int MAX_BYTES = 10 * 1024 * 1024;

  /** Đoạn tải tối đa (thân yêu cầu của máy chủ ≤ 1 MB). */
  public static final int PART_BYTES = 768 * 1024;
}
