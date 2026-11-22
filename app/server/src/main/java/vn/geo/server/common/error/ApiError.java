package vn.geo.server.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/** Thân phản hồi lỗi thống nhất cho mọi endpoint. */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(String code, String message, String requestId, List<FieldIssue> details) {

  public record FieldIssue(String field, String message) {}

  public static ApiError of(ErrorCode code, String message, String requestId) {
    return new ApiError(code.name(), message, requestId, List.of());
  }
}
