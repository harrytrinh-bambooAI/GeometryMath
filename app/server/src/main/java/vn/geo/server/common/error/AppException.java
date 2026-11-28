package vn.geo.server.common.error;

import java.time.Duration;
import java.util.List;

/** Lỗi nghiệp vụ/ứng dụng có mã ổn định; được {@link GlobalExceptionHandler} đổi thành ApiError. */
public class AppException extends RuntimeException {

  private final ErrorCode code;
  private final Duration retryAfter;
  private final List<ApiError.FieldIssue> details;

  public AppException(ErrorCode code) {
    this(code, code.defaultMessage(), null, null);
  }

  public AppException(ErrorCode code, String message) {
    this(code, message, null, null);
  }

  public AppException(ErrorCode code, String message, Throwable cause) {
    this(code, message, cause, null);
  }

  private AppException(ErrorCode code, String message, Throwable cause, Duration retryAfter) {
    this(code, message, cause, retryAfter, List.of());
  }

  private AppException(
      ErrorCode code,
      String message,
      Throwable cause,
      Duration retryAfter,
      List<ApiError.FieldIssue> details) {
    super(message, cause);
    this.code = code;
    this.retryAfter = retryAfter;
    this.details = List.copyOf(details);
  }

  /** VALIDATION_FAILED gắn với một ô nhập: thông điệp hiện dưới ô đó (details[0].field). */
  public static AppException invalidField(String field, String message) {
    return new AppException(
        ErrorCode.VALIDATION_FAILED,
        message,
        null,
        null,
        List.of(new ApiError.FieldIssue(field, message)));
  }

  /** 429 kèm Retry-After và câu thông điệp riêng. */
  public static AppException rateLimited(Duration retryAfter, String message) {
    return new AppException(ErrorCode.RATE_LIMITED, message, null, retryAfter);
  }

  public List<ApiError.FieldIssue> details() {
    return details;
  }

  /** 429 kèm Retry-After (giây, làm tròn lên, tối thiểu 1). */
  public static AppException rateLimited(Duration retryAfter) {
    return new AppException(
        ErrorCode.RATE_LIMITED, ErrorCode.RATE_LIMITED.defaultMessage(), null, retryAfter);
  }

  /** 503 quá tải kèm Retry-After (giây, làm tròn lên, tối thiểu 1). */
  public static AppException overloaded(Duration retryAfter) {
    return new AppException(
        ErrorCode.SERVICE_OVERLOADED,
        ErrorCode.SERVICE_OVERLOADED.defaultMessage(),
        null,
        retryAfter);
  }

  public ErrorCode code() {
    return code;
  }

  /** Thời gian chờ đề xuất cho máy khách, hoặc null. */
  public Duration retryAfter() {
    return retryAfter;
  }
}
