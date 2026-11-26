package vn.geo.server.content.internal.solution.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** S2-E5: dữ liệu màn C2-03 "Soát và duyệt lời giải" (chỉ giáo viên chủ đề nhận được). */
public final class SolutionViews {

  private SolutionViews() {}

  public static final String MACHINE_VERIFIED = "Đã kiểm chứng";
  public static final String MACHINE_NEEDS_REVIEW = "Cần xem lại";
  public static final String MACHINE_NOTE = "Máy đã kiểm tra phần tính toán";
  public static final String HUMAN_AI = "AI soạn";
  public static final String HUMAN_EDITING = "Đang sửa";
  public static final String HUMAN_APPROVED = "Giáo viên đã duyệt";
  public static final String HUMAN_SUPERSEDED = "Bị thay thế";

  /** Một bước kèm StepCheck: check = pass | fail | unverifiable | unchecked. */
  public record StepView(int no, String text, String check, String checkLabel, String reason) {}

  /** Phiên bản đang xem. */
  public record VersionView(
      int version,
      boolean latest,
      List<StepView> steps,
      String machineLabel,
      String machineNote,
      String humanLabel,
      boolean checked,
      int failCount,
      String summary,
      String finalState,
      String finalNote,
      String approvedCode,
      Instant approvedAt) {}

  /** Trạng thái nút Duyệt (BR-S2E5-17, 18). */
  public record ApproveView(
      boolean allowed, boolean approved, boolean needsConfirm, String blockedReason) {}

  /** Một dòng lịch sử: kind = version | proposal. */
  public record HistoryItem(
      String kind,
      Integer version,
      String description,
      String author,
      String authorCode,
      String humanLabel,
      Instant at) {}

  /** Đề xuất của AI đang chờ. */
  public record ProposalView(UUID id, int afterStep, String content) {}

  /** Bài giao đang dùng phiên bản nào. */
  public record AssignedView(String className, int version) {}

  /**
   * Toàn bộ màn C2-03. {@code saved} sai khi bấm Lưu mà không có thay đổi; {@code scene} là cảnh 3D
   * của đề (không chứa đáp số), null khi không dựng được.
   */
  public record DocView(
      UUID id,
      String code,
      String title,
      String statement,
      int latestVersion,
      boolean inMyShelf,
      boolean saved,
      String notice,
      Object scene,
      VersionView version,
      ApproveView approve,
      List<HistoryItem> history,
      List<ProposalView> proposals,
      List<AssignedView> assignments) {}

  /** Một dòng danh sách đề đang soát. */
  public record DocSummary(
      UUID id, String code, String title, int latestVersion, boolean inMyShelf) {}

  /** Nhãn hai trục của phiên bản đã giao (màn học sinh, không có bước giải hay StepCheck). */
  public record PinnedLabels(
      UUID assignmentId, int version, String machineLabel, String machineNote, String humanLabel) {}

  public static String machineLabel(String status) {
    return "verified".equals(status) ? MACHINE_VERIFIED : MACHINE_NEEDS_REVIEW;
  }

  public static String humanLabel(String status) {
    return switch (status) {
      case "ai_drafted" -> HUMAN_AI;
      case "editing" -> HUMAN_EDITING;
      case "teacher_approved" -> HUMAN_APPROVED;
      default -> HUMAN_SUPERSEDED;
    };
  }
}
