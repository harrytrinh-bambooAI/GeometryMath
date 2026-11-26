package vn.geo.server.content.internal.draft.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;

/** S2-E3: nháp đề và tác vụ bốn bước (problem_draft, problem_draft_job; V210). */
@Repository
public class DraftRepository {

  /** Một nháp đề. {@code imageBytes} = số byte ảnh tạm đang giữ (0 = không có ảnh). */
  public record DraftRow(
      UUID id,
      UUID ownerId,
      String source,
      String statement,
      String cellsJson,
      boolean textConfirmed,
      int imageBytes,
      Integer imageTotal,
      String status,
      UUID docId,
      Instant assignConfirmedAt,
      Instant createdAt) {}

  /** Một tác vụ bốn bước; stages, outcome là JSON dạng chuỗi. */
  public record JobRow(
      UUID id, UUID draftId, UUID ownerId, String state, String stagesJson, String outcomeJson) {}

  private final TenantJdbc jdbc;

  public DraftRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  private static Instant instant(ResultSet rs, int col) throws SQLException {
    OffsetDateTime t = rs.getObject(col, OffsetDateTime.class);
    return t == null ? null : t.toInstant();
  }

  private static final String DRAFT_COLS =
      """
      id, owner_user_id, source, statement, cells::text, text_confirmed, COALESCE(length(image_tmp), 0),
      image_total, status::text, doc_id, assign_confirmed_at, created_at
      """;

  private static DraftRow draftRow(ResultSet rs) throws SQLException {
    return new DraftRow(
        rs.getObject(1, UUID.class),
        rs.getObject(2, UUID.class),
        rs.getString(3),
        rs.getString(4),
        rs.getString(5),
        rs.getBoolean(6),
        rs.getInt(7),
        (Integer) rs.getObject(8),
        rs.getString(9),
        rs.getObject(10, UUID.class),
        instant(rs, 11),
        instant(rs, 12));
  }

  public Optional<DraftRow> draft(UUID id) {
    return jdbc.sql(
            "SELECT " + DRAFT_COLS + " FROM problem_draft WHERE org_id = :orgId AND id = :id")
        .param("id", id)
        .query((rs, i) -> draftRow(rs))
        .optional();
  }

  public List<DraftRow> mine(UUID ownerId) {
    return jdbc.sql(
            "SELECT "
                + DRAFT_COLS
                + """
                FROM problem_draft WHERE org_id = :orgId AND owner_user_id = :owner
                ORDER BY created_at DESC, id DESC LIMIT 100
                """)
        .param("owner", ownerId)
        .query((rs, i) -> draftRow(rs))
        .list();
  }

  /** Nháp đụng tới gần nhất (rời màn / đóng tab rồi quay lại "Thêm đề", BR-S2E3-04, 13). */
  public Optional<DraftRow> current(UUID ownerId) {
    return jdbc.sql(
            "SELECT "
                + DRAFT_COLS
                + """
                FROM problem_draft
                WHERE org_id = :orgId AND owner_user_id = :owner
                ORDER BY updated_at DESC, id DESC LIMIT 1
                """)
        .param("owner", ownerId)
        .query((rs, i) -> draftRow(rs))
        .optional();
  }

  /** Số nháp tạo trong 1 giờ gần nhất (BR-S2E3-05). */
  public int createdLastHour(UUID ownerId) {
    return jdbc.sql(
            """
            SELECT count(*) FROM problem_draft
            WHERE org_id = :orgId AND owner_user_id = :owner AND created_at > now() - interval '1 hour'
            """)
        .param("owner", ownerId)
        .query(Integer.class)
        .single();
  }

  /** Giây còn lại tới khi nháp cũ nhất trong giờ hết tính (Retry-After). */
  public long retryAfterSeconds(UUID ownerId) {
    Long s =
        jdbc.sql(
                """
                SELECT ceil(extract(epoch FROM min(created_at) + interval '1 hour' - now()))::bigint
                FROM problem_draft
                WHERE org_id = :orgId AND owner_user_id = :owner AND created_at > now() - interval '1 hour'
                """)
            .param("owner", ownerId)
            .query(Long.class)
            .optional()
            .orElse(null);
    return s == null ? 60 : Math.max(1, s);
  }

  public void insert(UUID id, UUID ownerId, String source, String statement) {
    jdbc.sql(
            """
            INSERT INTO problem_draft (id, org_id, owner_user_id, source, statement)
            VALUES (:id, :orgId, :owner, :source, :statement)
            """)
        .param("id", id)
        .param("owner", ownerId)
        .param("source", source)
        .param("statement", statement)
        .update();
  }

  public void updateText(UUID id, String statement, String cellsJson, boolean confirmed) {
    jdbc.sql(
            """
            UPDATE problem_draft SET statement = :statement, cells = CAST(:cells AS jsonb),
                   text_confirmed = :confirmed, updated_at = now()
            WHERE org_id = :orgId AND id = :id
            """)
        .param("id", id)
        .param("statement", statement)
        .param("cells", cellsJson)
        .param("confirmed", confirmed)
        .update();
  }

  /** Nối một đoạn ảnh (offset phải bằng số byte đã nhận); 0 khi lệch offset. */
  public int appendImage(UUID id, int offset, int total, String type, byte[] chunk) {
    return jdbc.sql(
            """
            UPDATE problem_draft
            SET image_tmp = CASE WHEN :pos = 0 THEN :chunk ELSE image_tmp || :chunk END,
                image_total = :total, image_type = COALESCE(CAST(:type AS text), image_type), source = 'image', text_confirmed = false,
                updated_at = now()
            WHERE org_id = :orgId AND id = :id AND COALESCE(length(image_tmp), 0) = CASE WHEN :pos = 0
                  THEN COALESCE(length(image_tmp), 0) ELSE :pos END
            """)
        .param("id", id)
        .param("pos", offset)
        .param("total", total)
        .param("type", type)
        .param("chunk", chunk)
        .update();
  }

  public byte[] image(UUID id) {
    return jdbc.sql("SELECT image_tmp FROM problem_draft WHERE org_id = :orgId AND id = :id")
        .param("id", id)
        .query((rs, i) -> rs.getBytes(1))
        .optional()
        .orElse(null);
  }

  /** Thay ảnh tạm (bản đã xóa siêu dữ liệu) hoặc xóa ảnh (null, BR-S2E3-09). */
  public void replaceImage(UUID id, byte[] bytes) {
    jdbc.sql(
            """
            UPDATE problem_draft SET image_tmp = CAST(:bytes AS bytea),
                   image_total = length(CAST(:bytes AS bytea)), updated_at = now()
            WHERE org_id = :orgId AND id = :id
            """)
        .param("id", id)
        .param("bytes", bytes)
        .update();
  }

  public void setStatus(UUID id, String status) {
    jdbc.sql(
            """
            UPDATE problem_draft SET status = CAST(:status AS problem_draft_status), updated_at = now()
            WHERE org_id = :orgId AND id = :id
            """)
        .param("id", id)
        .param("status", status)
        .update();
  }

  public void setDoc(UUID id, UUID docId) {
    jdbc.sql(
            "UPDATE problem_draft SET doc_id = :doc, updated_at = now() WHERE org_id = :orgId AND id = :id")
        .param("id", id)
        .param("doc", docId)
        .update();
  }

  public void confirmAssign(UUID id) {
    jdbc.sql(
            """
            UPDATE problem_draft SET assign_confirmed_at = now(), updated_at = now()
            WHERE org_id = :orgId AND id = :id
            """)
        .param("id", id)
        .update();
  }

  // ---------------------------------------------------------------- tác vụ

  private static JobRow jobRow(ResultSet rs) throws SQLException {
    return new JobRow(
        rs.getObject(1, UUID.class),
        rs.getObject(2, UUID.class),
        rs.getObject(3, UUID.class),
        rs.getString(4),
        rs.getString(5),
        rs.getString(6));
  }

  public Optional<JobRow> job(UUID id) {
    return jdbc.sql(
            """
            SELECT id, draft_id, owner_user_id, state, stages::text, outcome::text
            FROM problem_draft_job WHERE org_id = :orgId AND id = :id
            """)
        .param("id", id)
        .query((rs, i) -> jobRow(rs))
        .optional();
  }

  /** Tác vụ gần nhất của nháp. */
  public Optional<JobRow> lastJob(UUID draftId) {
    return jdbc.sql(
            """
            SELECT id, draft_id, owner_user_id, state, stages::text, outcome::text
            FROM problem_draft_job WHERE org_id = :orgId AND draft_id = :draft
            ORDER BY created_at DESC, id DESC LIMIT 1
            """)
        .param("draft", draftId)
        .query((rs, i) -> jobRow(rs))
        .optional();
  }

  public int countJobs(UUID draftId) {
    return jdbc.sql(
            "SELECT count(*) FROM problem_draft_job WHERE org_id = :orgId AND draft_id = :draft")
        .param("draft", draftId)
        .query(Integer.class)
        .single();
  }

  /** Tạo tác vụ; false khi nháp đã có tác vụ đang chạy (chỉ mục duy nhất một phần). */
  public boolean insertJob(UUID id, UUID draftId, UUID ownerId, String stagesJson) {
    return jdbc.sql(
                """
                INSERT INTO problem_draft_job (id, org_id, draft_id, owner_user_id, stages)
                VALUES (:id, :orgId, :draft, :owner, CAST(:stages AS jsonb))
                ON CONFLICT (org_id, draft_id) WHERE state = 'running' DO NOTHING
                """)
            .param("id", id)
            .param("draft", draftId)
            .param("owner", ownerId)
            .param("stages", stagesJson)
            .update()
        == 1;
  }

  /** Ghi trạng thái tác vụ; chỉ khi tác vụ còn {@code expectState} (hủy thắng ghi muộn). */
  public boolean updateJob(
      UUID id, String expectState, String state, String stagesJson, String outcomeJson) {
    return jdbc.sql(
                """
                UPDATE problem_draft_job SET state = :state, stages = CAST(:stages AS jsonb),
                       outcome = CAST(:outcome AS jsonb), updated_at = now()
                WHERE org_id = :orgId AND id = :id AND state = :expect
                """)
            .param("id", id)
            .param("expect", expectState)
            .param("state", state)
            .param("stages", stagesJson)
            .param("outcome", outcomeJson)
            .update()
        == 1;
  }
}
