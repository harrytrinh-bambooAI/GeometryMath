package vn.geo.server.content.internal.authoring.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import vn.geo.server.content.internal.draft.service.DraftViews.DraftView;

/** S2-E4: dữ liệu màn C2-04a "Chỉnh nháp với AI" (chỉ giáo viên chủ nháp nhận được). */
public final class AuthoringViews {

  private AuthoringViews() {}

  public static final int MAX_CHARS = 1000;
  public static final int MAX_TURNS = 30;
  public static final int MAX_UNDO = 10;

  public static final String TOO_LONG = "Tin tối đa 1000 ký tự";
  public static final String EMPTY = "Hãy nhập yêu cầu";
  public static final String TURNS_FULL =
      "Nháp này đã đủ 30 lượt chat, hãy duyệt hoặc tạo nháp mới";
  public static final String NO_UNDO = "Hết bước hoàn tác";
  public static final String SOURCE_DRAFT = "nháp hiện tại";
  public static final String ACCEPTED = "đã nhận";
  public static final String REJECTED = "đã bác";
  public static final String UNDONE = "đã hoàn tác một bước";
  public static final String STALE = "Nháp đã đổi, đề xuất không còn khớp";
  public static final String DECIDED = "Đề xuất này đã được xử lý";

  /**
   * Một đề xuất của AI (BR-S2E4-02): đổi gì (trước / sau), dựa trên nguồn nào, chỗ nào chưa chắc.
   * target = statement (dữ kiện đề) | answer (đáp số đề ghi); state = pending | accepted |
   * rejected; note = "đã nhận" | "đã bác" | "đã sửa tay từ 14 thành 15".
   */
  public record ProposalView(
      String target,
      String before,
      String after,
      String source,
      String unsure,
      String state,
      String note) {}

  /**
   * Một dòng lịch sử chat: role = teacher | ai | system; kind = message | question | proposal |
   * note.
   */
  public record MessageView(
      UUID id, int seq, String role, String kind, String text, ProposalView proposal, Instant at) {}

  /**
   * Toàn màn: nháp (chữ đề, nhãn hai trục, đối chiếu đáp số), lịch sử chat, giới hạn lượt và hoàn
   * tác.
   */
  public record ChatView(
      DraftView draft,
      String machineLabel,
      String humanLabel,
      boolean saved,
      List<MessageView> messages,
      int turns,
      int maxTurns,
      boolean canSend,
      String sendNote,
      int undoSteps,
      boolean canUndo,
      String undoNote) {}
}
