package vn.geo.server.aigateway.api;

/**
 * Kết quả từ AI.
 *
 * @param content nội dung trả về
 * @param provider tên nhà cung cấp đã trả lời
 * @param latencyMillis độ trễ đo được
 */
public record AiResponse(String content, String provider, long latencyMillis) {}
