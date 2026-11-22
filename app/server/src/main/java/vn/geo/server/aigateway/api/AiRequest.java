package vn.geo.server.aigateway.api;

/**
 * Yêu cầu gửi tới AI.
 *
 * @param task nhãn tác vụ ít giá trị (dùng làm nhãn số đo), ví dụ "hint"; không chứa dữ liệu người
 *     dùng
 * @param prompt nội dung đầu vào
 */
public record AiRequest(String task, String prompt) {}
