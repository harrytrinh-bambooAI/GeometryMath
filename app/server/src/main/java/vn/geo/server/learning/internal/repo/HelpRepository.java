package vn.geo.server.learning.internal.repo;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;

/**
 * Gợi ý, lời giải, Kiểm soát đáp án tối thiểu của E4: chỉ gọi hàm cổng (V10, V40), không chạm bảng
 * trực tiếp. Mọi hàm tự kiểm tư cách (thành viên lớp | chủ bài tự học) và lọc theo trường.
 */
@Repository
public class HelpRepository {

  public record State(
      String status,
      String kind,
      boolean allowHint,
      boolean allowQa,
      int hintLevelMax,
      boolean solutionViewed,
      String solutionState,
      Instant releaseAt,
      String statement,
      String problemTitle,
      String verifyStatus) {}

  public record SeenHint(int level, String body, String source) {}

  public record HintRow(String status, Integer level, String body, String source) {}

  public record Opened(
      String status,
      String finalAnswer,
      String stepsJson,
      boolean aiAuthored,
      String verifyStatus,
      boolean firstView) {}

  public record GuardContext(
      String status,
      boolean locked,
      String finalAnswer,
      String stepsJson,
      String ownFinalAnswer,
      String ownStepsJson) {}

  private final TenantJdbc jdbc;

  public HelpRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  public State state(UUID userId, UUID assignmentId) {
    return jdbc.sql(
            """
            SELECT status, kind, allow_hint, allow_qa, hint_level_max, solution_viewed, solution_state,
                   release_at, statement, problem_title, verify_status
            FROM fn_help_state(:orgId, :userId, :id)
            """)
        .param("userId", userId)
        .param("id", assignmentId)
        .query((rs, i) -> {
          OffsetDateTime rel = rs.getObject(8, OffsetDateTime.class);
          return new State(
              rs.getString(1),
              rs.getString(2),
              rs.getBoolean(3),
              rs.getBoolean(4),
              rs.getInt(5),
              rs.getBoolean(6),
              rs.getString(7),
              rel == null ? null : rel.toInstant(),
              rs.getString(9),
              rs.getString(10),
              rs.getString(11));
        })
        .single();
  }

  /**
   * Bài tự học của chính học sinh đang kế thừa khóa của một bài lớp (D-GEO-151, kiểm mỗi lần): khi
   * đó gợi ý lấy từ đề kho, không sinh gợi ý AI (D-GEO-155).
   */
  public boolean selfLockInherited(UUID userId, UUID assignmentId) {
    return Boolean.TRUE.equals(
        jdbc.sql(
                """
                SELECT fn_self_lock_inherited(:orgId, :userId, :id)
                """)
            .param("userId", userId)
            .param("id", assignmentId)
            .query(Boolean.class)
            .single());
  }

  /** Gợi ý đã xem (đọc lại, không ghi sự kiện). */
  public List<SeenHint> seenHints(UUID userId, UUID assignmentId) {
    return jdbc.sql(
            "SELECT level, body, hint_source FROM fn_seen_hints(:orgId, :userId, :id) ORDER BY level")
        .param("userId", userId)
        .param("id", assignmentId)
        .query((rs, i) -> new SeenHint(rs.getInt(1), rs.getString(2), rs.getString(3)))
        .list();
  }

  /** Q05: xin gợi ý theo mức và ghi nhận trong cùng giao dịch (fn_get_hint, V10). */
  public HintRow hint(UUID userId, UUID assignmentId, int level, UUID requestId) {
    return jdbc.sql(
            """
            SELECT status, level, body, hint_source
            FROM fn_get_hint(:orgId, :userId, :id, CAST(:level AS smallint), :requestId)
            """)
        .param("userId", userId)
        .param("id", assignmentId)
        .param("level", level)
        .param("requestId", requestId)
        .query((rs, i) -> {
          int lv = rs.getInt(2);
          return new HintRow(
              rs.getString(1), rs.wasNull() ? null : lv, rs.getString(3), rs.getString(4));
        })
        .single();
  }

  public String putSelfHints(UUID userId, UUID assignmentId, String h1, String h2) {
    return jdbc.sql("SELECT fn_put_self_hints(:orgId, :userId, :id, :h1, :h2)")
        .param("userId", userId)
        .param("id", assignmentId)
        .param("h1", h1)
        .param("h2", h2)
        .query(String.class)
        .single();
  }

  public Opened openSolution(UUID userId, UUID assignmentId) {
    return jdbc.sql(
            """
            SELECT status, final_answer, steps::text, ai_authored, verify_status, first_view
            FROM fn_open_solution(:orgId, :userId, :id)
            """)
        .param("userId", userId)
        .param("id", assignmentId)
        .query((rs, i) ->
            new Opened(
                rs.getString(1),
                rs.getString(2),
                rs.getString(3),
                rs.getBoolean(4),
                rs.getString(5),
                rs.getBoolean(6)))
        .single();
  }

  public GuardContext guardContext(UUID userId, UUID assignmentId) {
    return jdbc.sql(
            """
            SELECT status, locked, final_answer, steps::text, own_final_answer, own_steps::text
            FROM fn_answer_guard_context(:orgId, :userId, :id)
            """)
        .param("userId", userId)
        .param("id", assignmentId)
        .query((rs, i) ->
            new GuardContext(
                rs.getString(1),
                rs.getBoolean(2),
                rs.getString(3),
                rs.getString(4),
                rs.getString(5),
                rs.getString(6)))
        .single();
  }

  /**
   * Sự kiện không bền 'scene_shown' (mức A) qua fn_record_batch; arg = thời lượng, đơn vị 100 ms.
   */
  public String recordSceneShown(UUID userId, UUID assignmentId, int tenths) {
    return jdbc.sql(
            """
            SELECT status FROM fn_record_batch(:orgId, :userId, :id, ARRAY[now()],
                                               ARRAY['scene_shown']::event_kind[], ARRAY[:arg]::smallint[])
            """)
        .param("userId", userId)
        .param("id", assignmentId)
        .param("arg", tenths)
        .query(String.class)
        .single();
  }
}
