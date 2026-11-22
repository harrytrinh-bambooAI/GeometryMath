package vn.geo.server.common.error;

import org.springframework.http.HttpStatus;

/** Mã lỗi ổn định cho máy khách (không đổi tên), kèm thông điệp tiếng Việt mặc định. */
public enum ErrorCode {
  VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Dữ liệu gửi lên không hợp lệ."),
  INVALID_CURSOR(HttpStatus.BAD_REQUEST, "Con trỏ phân trang không hợp lệ."),
  UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "Bạn cần đăng nhập để tiếp tục."),
  FORBIDDEN(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này."),
  NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy dữ liệu yêu cầu."),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Phương thức không được hỗ trợ."),
  /** Xung đột trạng thái (D-GEO-143, 161): ví dụ đổi giờ khi đã mở, giao lại cùng đề. */
  CONFLICT(HttpStatus.CONFLICT, "Thao tác xung đột với trạng thái hiện tại của dữ liệu."),
  PAYLOAD_TOO_LARGE(HttpStatus.CONTENT_TOO_LARGE, "Dữ liệu gửi lên quá lớn."),
  UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Định dạng dữ liệu không được hỗ trợ."),
  /** Vượt giới hạn tốc độ (D-GEO-143, NFR-GEO-056); kèm Retry-After. */
  RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "Bạn thao tác quá nhanh, vui lòng thử lại sau."),
  DEPENDENCY_UNAVAILABLE(
      HttpStatus.SERVICE_UNAVAILABLE, "Dịch vụ phụ trợ tạm thời chưa sẵn sàng, vui lòng thử lại."),
  /**
   * Máy chủ quá tải (D-GEO-143, NFR-GEO-055): hết chờ nhóm kết nối, rào cách ly đầy. Kèm
   * Retry-After; thất bại nhanh thay vì xếp hàng vô hạn.
   */
  SERVICE_OVERLOADED(
      HttpStatus.SERVICE_UNAVAILABLE, "Hệ thống đang quá tải, vui lòng thử lại sau ít giây."),
  TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "Yêu cầu xử lý quá thời gian cho phép."),
  INTERNAL(HttpStatus.INTERNAL_SERVER_ERROR, "Hệ thống gặp lỗi, vui lòng thử lại sau.");

  private final HttpStatus status;
  private final String defaultMessage;

  ErrorCode(HttpStatus status, String defaultMessage) {
    this.status = status;
    this.defaultMessage = defaultMessage;
  }

  public HttpStatus status() {
    return status;
  }

  public String defaultMessage() {
    return defaultMessage;
  }

  /** Có gửi kèm tiêu đề Retry-After không (429 và 503 quá tải). */
  public boolean retryable() {
    return this == RATE_LIMITED || this == SERVICE_OVERLOADED;
  }

  /** Chọn mã lỗi gần nhất theo mã HTTP (cho lỗi do khung Spring ném ra). */
  public static ErrorCode forStatus(int httpStatus) {
    for (ErrorCode c : values()) {
      if (c.status.value() == httpStatus) {
        return c;
      }
    }
    return httpStatus >= 500 ? INTERNAL : VALIDATION_FAILED;
  }
}
