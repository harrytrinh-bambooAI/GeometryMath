package vn.geo.server.content.internal.authoring.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;

/**
 * S2-E4 (US-S2E4-01): lịch sử chat chỉnh nháp (problem_draft_chat), ngăn xếp hoàn tác
 * (problem_draft_edit) và lệnh đồng bộ chữ đề / mô tả hình sang sổ lời giải CHƯA duyệt của nháp
 * (V260).
 */
@Repository
public class AuthoringRepository {

  /** Một dòng chat; {@code proposalJson} null khi không phải đề xuất. */
  public record ChatRow(
      UUID id,
      UUID draftId,
      int seq,
      String role,
      String kind,
      String content,
      String proposalJson,
      String proposalState,
      String decidedNote,
      Instant createdAt) {}

  /** Một bước hoàn tác: ảnh chụp trạng thái trước khi nhận đề xuất. */
  public record EditRow(UUID id, int seq, UUID chatId, String snapshotJson) {}

  private final TenantJdbc jdbc;

  public AuthoringRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  /** Khóa hàng nháp trong giao dịch đang chạy (xếp hàng các thao tác chat của cùng một nháp). */
  public boolean lockDraft(UUID draftId) {
    return jdbc.sql("SELECT id FROM problem_draft WHERE org_id = :orgId AND id = :id FOR UPDATE")
        .param("id", draftId)
        .query(UUID.class)
        .optional()
        .isPresent();
  }

  private static ChatRow chatRow(ResultSet rs) throws SQLException {
    return new ChatRow(
        rs.getObject(1, UUID.class),
        rs.getObject(2, UUID.class),
        rs.getInt(3),
        rs.getString(4),
        rs.getString(5),
        rs.getString(6),
        rs.getString(7),
        rs.getString(8),
        rs.getString(9),
        rs.getObject(10, OffsetDateTime.class).toInstant());
  }

  private static final String CHAT_COLS =
      "id, draft_id, seq, role, kind, content, proposal::text, proposal_state, decided_note,"
          + " created_at";

  /** Toàn bộ lịch sử chat của nháp theo thứ tự (tối đa 30 lượt nên không quá ~200 dòng). */
  public List<ChatRow> chat(UUID draftId) {
    return jdbc.sql(
            "SELECT "
                + CHAT_COLS
                + """
                 FROM problem_draft_chat WHERE org_id = :orgId AND draft_id = :draft
                ORDER BY seq LIMIT 500
                """)
        .param("draft", draftId)
        .query((rs, i) -> chatRow(rs))
        .list();
  }

  public Optional<ChatRow> chatRow(UUID draftId, UUID id) {
    return jdbc.sql(
            "SELECT "
                + CHAT_COLS
                + " FROM problem_draft_chat WHERE org_id = :orgId AND draft_id = :draft AND id = :id")
        .param("draft", draftId)
        .param("id", id)
        .query((rs, i) -> chatRow(rs))
        .optional();
  }

  /** Số lượt chat đã dùng = số tin của giáo viên (BR-S2E4-06). */
  public int teacherTurns(UUID draftId) {
    return jdbc.sql(
            """
            SELECT count(*) FROM problem_draft_chat
            WHERE org_id = :orgId AND draft_id = :draft AND role = 'teacher'
            """)
        .param("draft", draftId)
        .query(Integer.class)
        .single();
  }

  public void insertChat(
      UUID id,
      UUID draftId,
      UUID owner,
      String role,
      String kind,
      String content,
      String proposalJson) {
    jdbc.sql(
            """
            INSERT INTO problem_draft_chat (id, org_id, draft_id, owner_user_id, seq, role, kind, content, proposal,
                                            proposal_state)
            SELECT :id, :orgId, :draft, :owner,
                   COALESCE((SELECT max(c.seq) FROM problem_draft_chat c
                             WHERE c.org_id = :orgId AND c.draft_id = :draft), 0) + 1,
                   :role, :kind, :content, CAST(:proposal AS jsonb),
                   CASE WHEN CAST(:proposal AS jsonb) IS NULL THEN NULL ELSE 'pending' END
            """)
        .param("id", id)
        .param("draft", draftId)
        .param("owner", owner)
        .param("role", role)
        .param("kind", kind)
        .param("content", content)
        .param("proposal", proposalJson)
        .update();
  }

  /** Chốt một đề xuất đang chờ; 0 khi đề xuất đã được xử lý. */
  public int decide(UUID id, String state, String note) {
    return jdbc.sql(
            """
            UPDATE problem_draft_chat SET proposal_state = :state, decided_note = :note
            WHERE org_id = :orgId AND id = :id AND proposal_state = 'pending'
            """)
        .param("id", id)
        .param("state", state)
        .param("note", note)
        .update();
  }

  // ---------------------------------------------------------------- hoàn tác

  public int editCount(UUID draftId) {
    return jdbc.sql(
            "SELECT count(*) FROM problem_draft_edit WHERE org_id = :orgId AND draft_id = :draft")
        .param("draft", draftId)
        .query(Integer.class)
        .single();
  }

  public Optional<EditRow> topEdit(UUID draftId) {
    return jdbc.sql(
            """
            SELECT id, seq, chat_id, snapshot::text FROM problem_draft_edit
            WHERE org_id = :orgId AND draft_id = :draft ORDER BY seq DESC LIMIT 1
            """)
        .param("draft", draftId)
        .query((rs, i) ->
            new EditRow(
                rs.getObject(1, UUID.class),
                rs.getInt(2),
                rs.getObject(3, UUID.class),
                rs.getString(4)))
        .optional();
  }

  /** Thêm một bước hoàn tác rồi chỉ giữ {@code keep} bước gần nhất (BR-S2E4-03). */
  public void pushEdit(UUID id, UUID draftId, UUID chatId, String snapshotJson, int keep) {
    jdbc.sql(
            """
            INSERT INTO problem_draft_edit (id, org_id, draft_id, seq, chat_id, snapshot)
            SELECT :id, :orgId, :draft,
                   COALESCE((SELECT max(e.seq) FROM problem_draft_edit e
                             WHERE e.org_id = :orgId AND e.draft_id = :draft), 0) + 1,
                   :chat, CAST(:snapshot AS jsonb)
            """)
        .param("id", id)
        .param("draft", draftId)
        .param("chat", chatId)
        .param("snapshot", snapshotJson)
        .update();
    jdbc.sql(
            """
            DELETE FROM problem_draft_edit
            WHERE org_id = :orgId AND draft_id = :draft
              AND seq <= (SELECT max(e.seq) FROM problem_draft_edit e
                          WHERE e.org_id = :orgId AND e.draft_id = :draft) - :keep
            """)
        .param("draft", draftId)
        .param("keep", keep)
        .update();
  }

  public void deleteEdit(UUID id) {
    jdbc.sql("DELETE FROM problem_draft_edit WHERE org_id = :orgId AND id = :id")
        .param("id", id)
        .update();
  }

  // ---------------------------------------------------------------- sổ lời giải của nháp

  /** Đồng bộ chữ đề và mô tả hình của sổ lời giải CHƯA duyệt theo nháp (figure null = bỏ mô tả). */
  public void updateDoc(UUID docId, String title, String statement, String figureJson) {
    jdbc.sql(
            """
            UPDATE solution_doc SET title = :title, statement = :statement, figure = CAST(:figure AS jsonb)
            WHERE org_id = :orgId AND id = :doc
            """)
        .param("doc", docId)
        .param("title", title)
        .param("statement", statement)
        .param("figure", figureJson)
        .update();
  }

  /** Đáp số đề ghi (null = bỏ; dùng khi hoàn tác về trạng thái chưa ghi đáp số). */
  public void setStatedAnswer(UUID docId, String statedAnswer) {
    jdbc.sql("UPDATE solution_doc SET stated_answer = :stated WHERE org_id = :orgId AND id = :doc")
        .param("doc", docId)
        .param("stated", statedAnswer)
        .update();
  }
}
