package vn.geo.server.assessment.internal.assign.service;

/**
 * CONFLICT kèm {@code details[]} gắn trường (US-S5E4-02-FC1: "lớp chưa đạt ngưỡng chạy song song").
 * {@code AppException} chung chỉ mang details cho VALIDATION_FAILED nên module tự dựng thân lỗi.
 */
public class GradingConflict extends RuntimeException {

  private final String field;
  private final String detail;

  public GradingConflict(String message, String field, String detail) {
    super(message);
    this.field = field;
    this.detail = detail;
  }

  public String field() {
    return field;
  }

  public String detail() {
    return detail;
  }
}
