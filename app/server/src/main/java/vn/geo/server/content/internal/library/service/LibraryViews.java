package vn.geo.server.content.internal.library.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Dữ liệu trả về của Kho đề S2-E6 (khung C2-01) và Trình chiếu S2-E7 (khung C2-04). */
public final class LibraryViews {

  private LibraryViews() {}

  /** Thẻ đề trong ngăn "Của tôi" hoặc "Kho chung". */
  public record Card(
      UUID id,
      String shelf,
      String title,
      String kind,
      String kindLabel,
      String machineLabel,
      String humanLabel,
      String status,
      String statusLabel,
      String reason,
      Instant createdAt) {}

  /** Một trang danh sách (không có tổng số bản ghi, BR-S2E6-03). */
  public record CardPage(List<Card> items, String nextCursor, boolean reviewer) {}

  /** Một dòng lịch sử gửi soát (BR-S2E6-06: giữ dòng từ chối cũ). */
  public record HistoryRow(String status, String statusLabel, String reason, Instant at) {}

  /** Chi tiết một đề Của tôi (chủ) hoặc đề đang Chờ soát (người soát). */
  public record DocDetail(
      UUID id,
      String title,
      String statement,
      String kind,
      String kindLabel,
      String machineLabel,
      String humanLabel,
      String status,
      String statusLabel,
      String reason,
      boolean owner,
      boolean canSubmit,
      String submitBlockedReason,
      List<HistoryRow> history) {}

  /** Chi tiết đề Kho chung cho giáo viên: lời giải xem ngay kèm nhãn hai trục (BR-S2E6-14). */
  public record LibraryDetail(
      UUID id,
      String title,
      String statement,
      String kind,
      String kindLabel,
      String machineLabel,
      String humanLabel,
      String hint,
      List<String> steps,
      Instant acceptedAt) {}

  /** Đề Kho chung cho học sinh: chỉ đề bài (không bước giải, không đáp số). */
  public record StudentLibraryItem(UUID id, String title, String statement, String kind) {}

  /** Lời giải đề Kho chung phía học sinh: luôn khóa ngoài bài giao (BR-S2E6-14, PO-66 G2). */
  public record LockedSolution(boolean locked, String message) {}

  /** Gợi ý đề Kho chung (không lộ lời giải). */
  public record Hint(String hint) {}

  /** Một dòng hàng đợi Chờ soát (người soát). */
  public record QueueRow(
      UUID submissionId,
      UUID docId,
      String title,
      String kind,
      String kindLabel,
      String ownerName,
      boolean mine,
      Instant submittedAt) {}

  public record Queue(List<QueueRow> items) {}

  /** Chi tiết đề Chờ soát cho người soát: đề bài và bước giải của phiên bản đã gửi. */
  public record QueueDetail(
      UUID submissionId,
      UUID docId,
      String title,
      String statement,
      String kindLabel,
      String machineLabel,
      String humanLabel,
      List<String> steps,
      boolean mine) {}

  /** Kết quả gửi soát / duyệt / từ chối. */
  public record Decision(UUID docId, String status, String statusLabel, UUID libraryId) {}

  /** Đề vừa tạo trong Của tôi (nhập chữ hoặc sao chép từ Kho chung). */
  public record Created(UUID id, String title) {}

  /** Kết quả đối chiếu trùng: chỉ đề của chính mình và Kho chung (BR-S2E6-11). */
  public record Similar(List<Card> items) {}

  // ---------------------------------------------------------------- S2-E7 Trình chiếu

  /** Hỏi lại trước khi chiếu (BR-S2E7-11…14): theo giờ máy chủ. */
  public record Preflight(boolean ask, List<String> lockedClasses, Instant serverNow) {}

  /** Toàn bộ dữ liệu tải sẵn của một buổi chiếu (BR-S2E7-07). */
  public record Presentation(
      UUID id,
      String title,
      String statement,
      String kind,
      Object scene,
      List<String> steps,
      boolean hasSolution,
      String machineLabel,
      String humanLabel,
      List<String> lockedClasses,
      int reconnectSeconds) {}
}
