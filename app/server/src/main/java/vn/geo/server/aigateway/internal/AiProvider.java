package vn.geo.server.aigateway.internal;

/**
 * Điểm gắn nhà cung cấp AI. Thêm nhà cung cấp thật bằng cách cài giao diện này và đặt
 * geo.ai.provider. Nhà cung cấp chỉ nhận {@link ProviderCall}: không có tên, mã lớp, định danh
 * người dùng hay trường (BR-GEO-108).
 */
public interface AiProvider {
  String name();

  String complete(ProviderCall call) throws Exception;

  /**
   * Nội dung gửi nhà cung cấp.
   *
   * @param task mục đích (qa, solution, scene, hint, lecture)
   * @param model model đang cấu hình
   * @param prompt nội dung (đề, câu hỏi); không chứa định danh
   * @param simulation chỉ nhà cung cấp giả lập dùng (chế độ lỗi/quá thời gian của móc kiểm thử)
   */
  record ProviderCall(String task, String model, String prompt, Simulation simulation) {}

  /** Chế độ của nhà cung cấp giả lập (US-GEO-103-FC1, FC2). */
  enum Simulation {
    OK,
    ERROR,
    TIMEOUT
  }
}
