package vn.geo.server.content.internal.draft.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import vn.geo.server.content.internal.solution.service.SolutionViews.AnswersView;

/** S2-E3: dữ liệu màn C2-02 "Thêm đề" (chỉ giáo viên chủ nháp nhận được). */
public final class DraftViews {

  private DraftViews() {}

  /** Ngưỡng tô vàng: ô có độ tin cậy dưới 0,80 (BR-S2E3-07). */
  public static final double UNSURE_BELOW = 0.80;

  public static final String NO_TEXT = "Máy không đọc được chữ trong ảnh, hãy gõ lại đề";
  public static final String AI_FIGURE = "Cần xem lại · hình do AI dựng";
  public static final String TEXT_ONLY = "bản nháp chữ";
  public static final String ASK_AI_FIGURE = "Hình do AI dựng, chưa kiểm chứng. Vẫn giao?";

  /** Bốn bước theo thứ tự (BR-S2E3-11). */
  public static final List<String> STAGE_KEYS = List.of("read", "build", "solve", "verify");

  public static final List<String> STAGE_LABELS =
      List.of("Đọc đề", "Dựng hình", "Giải", "Kiểm chứng");

  /**
   * Một ô chữ đọc từ ảnh. state = ok (tin cậy ≥ 0,80) | unsure (tô vàng) | fixed (đã sửa) |
   * confirmed (đã bấm "đúng").
   */
  public record CellView(int index, String text, double confidence, String state) {
    public boolean unsure() {
      return "unsure".equals(state);
    }
  }

  /**
   * Một bước của tác vụ: state = waiting | running | done | error; error = TIMEOUT | ERROR |
   * UNBUILDABLE; aiCalls = số lượt gọi AI của bước (FC2: mỗi bước một lần).
   */
  public record StageView(
      String key, String label, String state, String note, String error, int aiCalls) {}

  /** Tác vụ bốn bước: state = running | done | failed | cancelled. */
  public record JobView(
      UUID id, UUID draftId, String state, List<StageView> stages, boolean retryable) {}

  /**
   * Kết quả khi bốn bước xong: hình (2D/3D), lời giải nháp, nhãn hai trục, bảng đối chiếu đáp số.
   */
  public record ResultView(
      UUID docId,
      String machineLabel,
      String machineNote,
      String humanLabel,
      String sceneKind,
      Object scene,
      List<String> steps,
      AnswersView answers,
      boolean approveBlocked,
      String blockedReason) {}

  /** Nháp đề và tiến trình. status = editing | building | ready | ai_figure | text_only. */
  public record DraftView(
      UUID id,
      String source,
      String status,
      String statusLabel,
      String statement,
      List<CellView> cells,
      boolean textConfirmed,
      boolean canConfirmText,
      boolean hasImage,
      String notice,
      String machineLabel,
      boolean inMyShelf,
      JobView job,
      ResultView result,
      boolean assignable,
      String assignNote,
      boolean assignConfirmed,
      Instant createdAt) {}

  /** Một dòng ngăn "Của tôi" (nháp đề của giáo viên). */
  public record DraftSummary(
      UUID id,
      String title,
      String status,
      String statusLabel,
      UUID docId,
      boolean assignable,
      String assignNote,
      boolean needsAssignConfirm,
      Instant createdAt) {}

  /** Trạng thái ban đầu của một ô chữ đọc từ ảnh: dưới 0,80 tô vàng (BR-S2E3-07). */
  public static String cellState(double confidence) {
    return confidence < UNSURE_BELOW ? "unsure" : "ok";
  }

  public static String statusLabel(String status) {
    return switch (status) {
      case "building" -> "Đang dựng";
      case "ready" -> "Đã soạn nháp";
      case "ai_figure" -> AI_FIGURE;
      case "text_only" -> "Bản nháp chữ";
      default -> "Đang soạn";
    };
  }
}
