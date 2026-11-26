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
   * Gọi và trả kèm số token thật (nếu nhà cung cấp có). Mặc định: {@link #complete} không có usage
   * (cổng ước lượng token, chi phí mô phỏng).
   */
  default Result call(ProviderCall call) throws Exception {
    return new Result(complete(call), null);
  }

  /**
   * Kết quả một lượt gọi.
   *
   * @param content nội dung trả về (đã đúng định dạng của task)
   * @param usage token thật; null = nhà cung cấp không báo
   * @param model model thực trả lời (phản hồi API); null = dùng model đã yêu cầu
   */
  record Result(String content, Usage usage, String model) {
    public Result(String content, Usage usage) {
      this(content, usage, null);
    }
  }

  /** Token của một lượt theo nhà cung cấp (để tính chi phí/bài). */
  record Usage(int inputTokens, int outputTokens, int cacheReadTokens, int cacheWriteTokens) {}

  /**
   * Nội dung gửi nhà cung cấp.
   *
   * @param task mục đích (qa, solution, scene, hint, lecture)
   * @param model model đang cấu hình
   * @param prompt nội dung (đề, câu hỏi); không chứa định danh
   * @param simulation chỉ nhà cung cấp giả lập dùng (chế độ lỗi/quá thời gian của móc kiểm thử)
   * @param scenario chỉ nhà cung cấp giả lập dùng: kịch bản trả lời của móc kiểm thử (E2:
   *     F-GEO-111, F-GEO-112); không phải định danh
   * @param delayMs chỉ nhà cung cấp giả lập dùng: độ trễ kịch bản của móc kiểm thử (E4: "trả lời
   *     sau 5 giây", US-GEO-401-SC2, 409-SC4)
   */
  record ProviderCall(
      String task,
      String model,
      String prompt,
      Simulation simulation,
      String scenario,
      long delayMs) {
    public ProviderCall(String task, String model, String prompt, Simulation simulation) {
      this(task, model, prompt, simulation, null, 0);
    }

    public ProviderCall(
        String task, String model, String prompt, Simulation simulation, String scenario) {
      this(task, model, prompt, simulation, scenario, 0);
    }
  }

  /** Chế độ của nhà cung cấp giả lập (US-GEO-103-FC1, FC2). */
  enum Simulation {
    OK,
    ERROR,
    TIMEOUT
  }
}
