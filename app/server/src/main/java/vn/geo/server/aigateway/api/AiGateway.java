package vn.geo.server.aigateway.api;

/**
 * API công khai của cổng AI. Module khác chỉ gọi giao diện này, không biết nhà cung cấp phía sau.
 */
public interface AiGateway {
  AiResponse complete(AiRequest request);

  /** Model đang hiệu lực (ghi vào báo cáo bộ đề chuẩn). */
  default String model() {
    return "unknown";
  }

  /** Tên nhà cung cấp AI đang dùng ("mock" | "anthropic"; ghi vào báo cáo bộ câu thử). */
  default String provider() {
    return "unknown";
  }

  /** Phiên bản prompt đang hiệu lực. */
  default String promptVersion() {
    return "unknown";
  }
}
