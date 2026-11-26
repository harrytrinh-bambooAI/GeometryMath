package vn.geo.server.assignment.internal.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;

/** SQL của câu trắc nghiệm trong bộ bài (S2-E9, US-S2E9-03). */
@Repository
public class ChoiceRepository {

  /** Câu của bộ kèm cấu hình trắc nghiệm ({@code options} null: câu điền đáp số). */
  public record ItemChoice(
      int position,
      UUID problemId,
      UUID assignmentId,
      int maxAttempts,
      String options,
      UUID correctId) {}

  public record Attempt(UUID assignmentId, String outcome, String detail, int wrongAfter) {}

  public record Recorded(String status, String outcome, String detail, int wrongAfter) {}

  private final TenantJdbc jdbc;

  public ChoiceRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  /** Trạng thái bộ bài của chính giáo viên (rỗng: không có hoặc của người khác). */
  public Optional<String> ownSetStatus(UUID teacherId, UUID setId) {
    return jdbc.sql(
            """
            SELECT s.status::text FROM assignment_set s
            WHERE s.org_id = :orgId AND s.id = :id AND s.owner_user_id = :teacher LIMIT 1
            """)
        .param("id", setId)
        .param("teacher", teacherId)
        .query(String.class)
        .optional();
  }

  private static final String ITEM_SELECT =
      """
      SELECT i.position, i.problem_id, i.assignment_id, i.max_attempts, c.options::text, c.correct_id
      FROM assignment_item i
      LEFT JOIN assignment_item_choice c ON c.org_id = :orgId AND c.set_id = i.set_id AND c.problem_id = i.problem_id
      WHERE i.org_id = :orgId
      """;

  private static ItemChoice item(ResultSet rs) throws SQLException {
    return new ItemChoice(
        rs.getInt(1),
        rs.getObject(2, UUID.class),
        rs.getObject(3, UUID.class),
        rs.getInt(4),
        rs.getString(5),
        rs.getObject(6, UUID.class));
  }

  public List<ItemChoice> itemsOfSet(UUID setId) {
    return jdbc.sql(ITEM_SELECT + " AND i.set_id = :set ORDER BY i.position LIMIT 20")
        .param("set", setId)
        .query((rs, i) -> item(rs))
        .list();
  }

  public Optional<ItemChoice> itemOfAssignment(UUID assignmentId) {
    return jdbc.sql(ITEM_SELECT + " AND i.assignment_id = :assignment LIMIT 1")
        .param("assignment", assignmentId)
        .query((rs, i) -> item(rs))
        .optional();
  }

  public void replace(
      UUID setId, UUID problemId, String optionsJson, UUID correctId, int maxAttempts) {
    delete(setId, problemId);
    jdbc.sql(
            """
            INSERT INTO assignment_item_choice (org_id, set_id, problem_id, options, correct_id)
            VALUES (:orgId, :set, :problem, CAST(:options AS jsonb), :correct)
            """)
        .param("set", setId)
        .param("problem", problemId)
        .param("options", optionsJson)
        .param("correct", correctId)
        .update();
    jdbc.sql(
            """
            UPDATE assignment_item SET max_attempts = :max
            WHERE org_id = :orgId AND set_id = :set AND problem_id = :problem
            """)
        .param("set", setId)
        .param("problem", problemId)
        .param("max", maxAttempts)
        .update();
  }

  public int delete(UUID setId, UUID problemId) {
    return jdbc.sql(
            """
            DELETE FROM assignment_item_choice
            WHERE org_id = :orgId AND set_id = :set AND problem_id = :problem
            """)
        .param("set", setId)
        .param("problem", problemId)
        .update();
  }

  /** Lượt thử của một học sinh trên các câu (bảng của module tracking, chỉ đọc). */
  public List<Attempt> attempts(UUID userId, List<UUID> assignmentIds) {
    if (assignmentIds.isEmpty()) {
      return List.of();
    }
    return jdbc.sql(
            """
            SELECT assignment_id, outcome, detail::text, wrong_after
            FROM practice_attempt
            WHERE org_id = :orgId AND user_id = :userId AND assignment_id = ANY(:ids)
            ORDER BY created_at, id LIMIT 2000
            -- sqllint:allow=ownership read model: lựa chọn đã bị chấm Sai của câu trắc nghiệm (module tracking)
            """)
        .param("userId", userId)
        .param("ids", assignmentIds.toArray(new UUID[0]))
        .query((rs, i) ->
            new Attempt(
                rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3), rs.getInt(4)))
        .list();
  }

  public Optional<Attempt> attemptByKey(UUID userId, UUID assignmentId, String key) {
    return jdbc.sql(
            """
            SELECT assignment_id, outcome, detail::text, wrong_after
            FROM practice_attempt
            WHERE org_id = :orgId AND user_id = :userId AND assignment_id = :assignmentId AND request_key = :key
            LIMIT 1
            -- sqllint:allow=ownership read model: gửi lặp cùng mã chống lặp (module tracking)
            """)
        .param("userId", userId)
        .param("assignmentId", assignmentId)
        .param("key", key)
        .query((rs, i) ->
            new Attempt(
                rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3), rs.getInt(4)))
        .optional();
  }

  /** Ghi lượt thử qua đường DUY NHẤT của S2-E8 (khóa theo câu + học sinh, chống lặp). */
  public Recorded record(
      UUID userId,
      UUID assignmentId,
      String key,
      String submitted,
      String outcome,
      String detailJson,
      int maxAttempts) {
    return jdbc.sql(
            """
            SELECT status, outcome, detail::text, wrong_after
            FROM fn_record_practice_attempt(:orgId, :userId, :assignmentId, :key, :submitted, :outcome,
                                            CAST(:detail AS jsonb), :max, 'student')
            """)
        .param("userId", userId)
        .param("assignmentId", assignmentId)
        .param("key", key)
        .param("submitted", submitted)
        .param("outcome", outcome)
        .param("detail", detailJson)
        .param("max", maxAttempts)
        .query((rs, i) ->
            new Recorded(rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4)))
        .single();
  }
}
