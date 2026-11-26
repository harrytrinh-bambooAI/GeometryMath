package vn.geo.server.content.internal.authoring.web;

import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.content.internal.authoring.service.AuthoringViews.ChatView;
import vn.geo.server.content.internal.authoring.service.DraftChatService;

/**
 * S2-E4: màn C2-04a "Chỉnh nháp với AI" (giáo viên chủ nháp; người khác NOT_FOUND, học sinh
 * FORBIDDEN). No-store (D-GEO-145): kết quả có lời giải nháp.
 *
 * <ul>
 *   <li>{@code GET /api/v1/teacher/problem-drafts/{id}/chat} nháp + lịch sử chat + giới hạn.
 *   <li>{@code POST …/chat/messages} {text}: 1…1000 ký tự; lượt 31 → CONFLICT, không gọi AI.
 *   <li>{@code POST …/chat/proposals/{messageId}/accept} {after?} (sửa tay) · {@code /reject}.
 *   <li>{@code POST …/chat/undo} (tối đa 10 bước gần nhất) · {@code POST …/chat/recheck}.
 * </ul>
 */
@RestController
@NoStore
public class DraftChatController {

  private static final String BASE = "/api/v1/teacher/problem-drafts/{id}/chat";

  private final DraftChatService chat;

  public DraftChatController(DraftChatService chat) {
    this.chat = chat;
  }

  public record MessageRequest(String text) {}

  public record AcceptRequest(String after) {}

  @GetMapping(BASE)
  public ChatView view(@PathVariable UUID id) {
    return chat.view(id);
  }

  @PostMapping(BASE + "/messages")
  public ChatView send(@PathVariable UUID id, @RequestBody(required = false) MessageRequest body) {
    return chat.send(id, body == null ? null : body.text());
  }

  @PostMapping(BASE + "/proposals/{messageId}/accept")
  public ChatView accept(
      @PathVariable UUID id,
      @PathVariable UUID messageId,
      @RequestBody(required = false) AcceptRequest body) {
    return chat.accept(id, messageId, body == null ? null : body.after());
  }

  @PostMapping(BASE + "/proposals/{messageId}/reject")
  public ChatView reject(@PathVariable UUID id, @PathVariable UUID messageId) {
    return chat.reject(id, messageId);
  }

  @PostMapping(BASE + "/undo")
  public ChatView undo(@PathVariable UUID id) {
    return chat.undo(id);
  }

  @PostMapping(BASE + "/recheck")
  public ChatView recheck(@PathVariable UUID id) {
    return chat.recheck(id);
  }
}
