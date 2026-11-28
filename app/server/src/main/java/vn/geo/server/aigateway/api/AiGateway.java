package vn.geo.server.aigateway.api;

/**
 * API công khai của cổng AI. Module khác chỉ gọi giao diện này, không biết nhà cung cấp phía sau.
 */
public interface AiGateway {
  AiResponse complete(AiRequest request);

  /**
   * Như {@link #complete(AiRequest)} với trần chờ riêng của người gọi: thời gian chờ = nhỏ hơn giữa
   * {@code maxWait} và {@code geo.ai.timeout} (PO-83: bộ gộp "Cả lớp vướng ở đâu" có trần tổng 10
   * giây cho cả màn, không phụ thuộc {@code geo.ai.timeout} 90 giây ở prod). Quá trần: TIMEOUT.
   */
  default AiResponse complete(AiRequest request, java.time.Duration maxWait) {
    return complete(request);
  }

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
