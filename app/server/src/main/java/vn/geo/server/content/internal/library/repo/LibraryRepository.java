package vn.geo.server.content.internal.library.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;

/**
 * S2-E6: ngăn "Của tôi" (solution_doc của chủ), "Kho chung" (library_problem), hàng đợi soát
 * (library_submission) và bộ đếm thêm đề (problem_create_event). V230.
 */
@Repository
public class LibraryRepository {

  /** Mốc "chưa có con trỏ": mọi hàng đứng trước (trang đầu). */
  public static final OffsetDateTime TOP =
      OffsetDateTime.of(9999, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

  public static final UUID TOP_ID = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

  /** Một đề của ngăn Của tôi kèm nhãn phiên bản mới nhất và trạng thái gửi soát gần nhất. */
  public record DocCard(
      UUID id,
      UUID ownerId,
      String title,
      String statement,
      String kind,
      Instant createdAt,
      int latestVersion,
      String machineStatus,
      String humanStatus,
      String shelfStatus,
      String reason) {}

  /** Một đề Kho chung (không có bước giải). */
  public record LibraryRow(
      UUID id,
      String title,
      String statement,
      String kind,
      String hint,
      String machineStatus,
      Instant acceptedAt) {}

  public record SubmissionRow(
      UUID id,
      UUID docId,
      UUID versionId,
      UUID ownerId,
      String status,
      String reason,
      Instant submittedAt,
      String title,
      String statement,
      String kind,
      String ownerName,
      String machineStatus,
      String humanStatus) {}

  public record HistoryRow(String status, String reason, Instant submittedAt, Instant decidedAt) {}

  /** Bài giao lớp của một đề (Trình chiếu hỏi lại: BR-S2E7-11). */
  public record ClassRelease(String className, Instant releaseAt) {}

  private static final String KIND =
      "CASE WHEN (d.figure -> 'plane') IS NOT NULL THEN 'plane' ELSE 'space' END";

  private final TenantJdbc jdbc;

  public LibraryRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  private static Instant instant(ResultSet rs, String col) throws SQLException {
    OffsetDateTime t = rs.getObject(col, OffsetDateTime.class);
    return t == null ? null : t.toInstant();
  }

  private static OffsetDateTime odt(Instant t) {
    return t == null ? null : t.atOffset(ZoneOffset.UTC);
  }

  private static DocCard docCard(ResultSet rs) throws SQLException {
    return new DocCard(
        rs.getObject("id", UUID.class),
        rs.getObject("owner_user_id", UUID.class),
        rs.getString("title"),
        rs.getString("statement"),
        rs.getString("kind"),
        instant(rs, "created_at"),
        rs.getInt("latest_version"),
        rs.getString("machine_status"),
        rs.getString("human_status"),
        rs.getString("shelf_status"),
        rs.getString("reason"));
  }

  private static final String DOC_SELECT =
      """
      SELECT d.id, d.owner_user_id, d.title, d.statement, %s AS kind, d.created_at, d.latest_version,
             v.machine_status::text AS machine_status, v.human_status::text AS human_status,
             s.status::text AS shelf_status, s.reason
      FROM solution_doc d
      LEFT JOIN solution_version v ON v.org_id = :orgId AND v.doc_id = d.id AND v.version = d.latest_version
      LEFT JOIN LATERAL (SELECT ls.status, ls.reason FROM library_submission ls
                         WHERE ls.org_id = :orgId AND ls.doc_id = d.id
                         ORDER BY ls.submitted_at DESC, ls.id DESC LIMIT 1) s ON true
      WHERE d.org_id = :orgId
      """
          .formatted(KIND);

  /**
   * Trang "Của tôi" (mới nhất trước; khóa (created_at, id)). {@code kind} = all | space | plane.
   */
  public List<DocCard> mine(
      UUID owner, String kind, String q, Instant afterAt, UUID afterId, int fetch) {
    return jdbc.sql(
            DOC_SELECT
                + """
                  AND d.owner_user_id = :owner
                  AND (:kind = 'all' OR %s = :kind)
                  AND (:q = '' OR strpos(lower(d.title), lower(:q)) > 0 OR strpos(lower(d.statement), lower(:q)) > 0)
                  AND (d.created_at, d.id) < (:afterAt, :afterId)
                ORDER BY d.created_at DESC, d.id DESC
                LIMIT :fetch
                """
                    .formatted(KIND))
        .param("owner", owner)
        .param("kind", kind)
        .param("q", q)
        .param("afterAt", afterAt == null ? TOP : odt(afterAt))
        .param("afterId", afterId == null ? TOP_ID : afterId)
        .param("fetch", fetch)
        .query((rs, i) -> docCard(rs))
        .list();
  }

  public Optional<DocCard> doc(UUID id) {
    return jdbc.sql(DOC_SELECT + " AND d.id = :id LIMIT 1")
        .param("id", id)
        .query((rs, i) -> docCard(rs))
        .optional();
  }

  /** Khóa hàng đề trong giao dịch đang chạy (gửi soát). */
  public void lockDoc(UUID id) {
    jdbc.sql("SELECT id FROM solution_doc WHERE org_id = :orgId AND id = :id FOR UPDATE")
        .param("id", id)
        .query(UUID.class)
        .optional();
  }

  private static LibraryRow libraryRow(ResultSet rs) throws SQLException {
    return new LibraryRow(
        rs.getObject("id", UUID.class),
        rs.getString("title"),
        rs.getString("statement"),
        rs.getString("kind"),
        rs.getString("hint"),
        rs.getString("machine_status"),
        instant(rs, "accepted_at"));
  }

  /** Trang "Kho chung" (mới vào trước; khóa (accepted_at, id)). */
  public List<LibraryRow> library(String kind, String q, Instant afterAt, UUID afterId, int fetch) {
    return jdbc.sql(
            """
            SELECT l.id, l.title, l.statement, l.kind::text AS kind, l.hint, l.machine_status::text AS machine_status,
                   l.accepted_at
            FROM library_problem l
            WHERE l.org_id = :orgId
              AND (:kind = 'all' OR l.kind::text = :kind)
              AND (:q = '' OR strpos(lower(l.title), lower(:q)) > 0 OR strpos(lower(l.statement), lower(:q)) > 0)
              AND (l.accepted_at, l.id) < (:afterAt, :afterId)
            ORDER BY l.accepted_at DESC, l.id DESC
            LIMIT :fetch
            """)
        .param("kind", kind)
        .param("q", q)
        .param("afterAt", afterAt == null ? TOP : odt(afterAt))
        .param("afterId", afterId == null ? TOP_ID : afterId)
        .param("fetch", fetch)
        .query((rs, i) -> libraryRow(rs))
        .list();
  }

  public Optional<LibraryRow> libraryItem(UUID id) {
    return jdbc.sql(
            """
            SELECT l.id, l.title, l.statement, l.kind::text AS kind, l.hint, l.machine_status::text AS machine_status,
                   l.accepted_at
            FROM library_problem l WHERE l.org_id = :orgId AND l.id = :id LIMIT 1
            """)
        .param("id", id)
        .query((rs, i) -> libraryRow(rs))
        .optional();
  }

  /** Bước giải Kho chung qua cổng CSDL (chỉ giáo viên đang hoạt động cùng tổ chức). */
  public Optional<String> libraryStepsForTeacher(UUID user, UUID item) {
    return jdbc.sql("SELECT steps::text FROM fn_library_steps_teacher(:orgId, :user, :item)")
        .param("user", user)
        .param("item", item)
        .query(String.class)
        .optional();
  }

  /** Bước giải của đề Chờ soát qua cổng CSDL (chỉ người soát). */
  public Optional<String> submissionSteps(UUID reviewer, UUID submission) {
    return jdbc.sql("SELECT steps::text FROM fn_library_submission_steps(:orgId, :user, :sub)")
        .param("user", reviewer)
        .param("sub", submission)
        .query(String.class)
        .optional();
  }

  // ---------------------------------------------------------------- gửi soát

  /** Phiên bản mới nhất của đề và trục người của nó. */
  public Optional<UUID> latestApprovedVersion(UUID docId) {
    return jdbc.sql(
            """
            SELECT v.id FROM solution_doc d
            JOIN solution_version v ON v.org_id = :orgId AND v.doc_id = d.id AND v.version = d.latest_version
            WHERE d.org_id = :orgId AND d.id = :doc AND v.human_status = 'teacher_approved'
            LIMIT 1
            """)
        .param("doc", docId)
        .query(UUID.class)
        .optional();
  }

  /** Khóa theo chủ đề: đếm "đang Chờ soát" và chèn cùng một giao dịch không chạy đua. */
  public void lockOwner(UUID owner, long salt) {
    jdbc.sql(
            "SELECT pg_advisory_xact_lock(hashtextextended(CAST(:owner AS text) || :orgId, :salt))")
        .param("owner", owner.toString())
        .param("salt", salt)
        .query()
        .singleRow();
  }

  public int pendingCount(UUID owner) {
    return jdbc.sql(
            """
            SELECT count(*) FROM library_submission s
            WHERE s.org_id = :orgId AND s.owner_user_id = :owner AND s.status = 'pending'
            """)
        .param("owner", owner)
        .query(Integer.class)
        .single();
  }

  public void insertSubmission(
      UUID id, UUID doc, UUID version, UUID owner, String hint, Instant at) {
    jdbc.sql(
            """
            INSERT INTO library_submission (id, org_id, doc_id, version_id, owner_user_id, hint, submitted_at)
            VALUES (:id, :orgId, :doc, :version, :owner, :hint, :at)
            """)
        .param("id", id)
        .param("doc", doc)
        .param("version", version)
        .param("owner", owner)
        .param("hint", hint)
        .param("at", odt(at))
        .update();
  }

  public List<HistoryRow> history(UUID docId) {
    return jdbc.sql(
            """
            SELECT s.status::text, s.reason, s.submitted_at, s.decided_at FROM library_submission s
            WHERE s.org_id = :orgId AND s.doc_id = :doc
            ORDER BY s.submitted_at DESC, s.id DESC LIMIT 100
            """)
        .param("doc", docId)
        .query((rs, i) ->
            new HistoryRow(
                rs.getString(1),
                rs.getString(2),
                instant(rs, "submitted_at"),
                instant(rs, "decided_at")))
        .list();
  }

  /** Đề có đang Chờ soát không (người soát chỉ thấy đề Chờ soát: BR-S2E6-11). */
  public boolean pending(UUID docId) {
    return jdbc.sql(
            """
            SELECT EXISTS (SELECT 1 FROM library_submission s
                           WHERE s.org_id = :orgId AND s.doc_id = :doc AND s.status = 'pending')
            """)
        .param("doc", docId)
        .query(Boolean.class)
        .single();
  }

  // ---------------------------------------------------------------- hàng đợi soát

  private static final String SUB_SELECT =
      """
      SELECT s.id, s.doc_id, s.version_id, s.owner_user_id, s.status::text AS status, s.reason, s.submitted_at,
             d.title, d.statement, %s AS kind,
             (SELECT u.display_name FROM app_user u WHERE u.org_id = :orgId AND u.id = s.owner_user_id LIMIT 1)
               AS display_name, v.machine_status::text AS machine_status,
             v.human_status::text AS human_status
      FROM library_submission s
      JOIN solution_doc d ON d.org_id = :orgId AND d.id = s.doc_id
      JOIN solution_version v ON v.org_id = :orgId AND v.id = s.version_id
      -- sqllint:allow=ownership read model: tên giáo viên gửi đề (module iam) trong hàng đợi soát
      WHERE s.org_id = :orgId
      """
          .formatted(KIND);

  private static SubmissionRow subRow(ResultSet rs) throws SQLException {
    return new SubmissionRow(
        rs.getObject("id", UUID.class),
        rs.getObject("doc_id", UUID.class),
        rs.getObject("version_id", UUID.class),
        rs.getObject("owner_user_id", UUID.class),
        rs.getString("status"),
        rs.getString("reason"),
        instant(rs, "submitted_at"),
        rs.getString("title"),
        rs.getString("statement"),
        rs.getString("kind"),
        rs.getString("display_name"),
        rs.getString("machine_status"),
        rs.getString("human_status"));
  }

  public List<SubmissionRow> queue() {
    return jdbc.sql(
            SUB_SELECT
                + """
                  AND s.status = 'pending'
                ORDER BY s.submitted_at, s.id LIMIT 200
                """)
        .query((rs, i) -> subRow(rs))
        .list();
  }

  public Optional<SubmissionRow> submission(UUID id, boolean lock) {
    return jdbc.sql(SUB_SELECT + " AND s.id = :id LIMIT 1" + (lock ? " FOR UPDATE OF s" : ""))
        .param("id", id)
        .query((rs, i) -> subRow(rs))
        .optional();
  }

  public Optional<SubmissionRow> pendingOf(UUID docId) {
    return jdbc.sql(SUB_SELECT + " AND s.doc_id = :doc AND s.status = 'pending' LIMIT 1")
        .param("doc", docId)
        .query((rs, i) -> subRow(rs))
        .optional();
  }

  public int accept(UUID reviewer, UUID submission, UUID item, Instant at) {
    return jdbc.sql("SELECT fn_library_accept(:orgId, :reviewer, :sub, :item, :at)")
        .param("reviewer", reviewer)
        .param("sub", submission)
        .param("item", item)
        .param("at", odt(at))
        .query(Integer.class)
        .single();
  }

  public int reject(UUID reviewer, UUID submission, String reason, Instant at) {
    return jdbc.sql(
            """
            UPDATE library_submission SET status = 'rejected', reason = :reason, decided_by = :reviewer,
                   decided_at = :at
            WHERE org_id = :orgId AND id = :sub AND status = 'pending'
            """)
        .param("reviewer", reviewer)
        .param("sub", submission)
        .param("reason", reason)
        .param("at", odt(at))
        .update();
  }

  // ---------------------------------------------------------------- tạo đề, bộ đếm

  public int createdSince(UUID user, Instant since) {
    return jdbc.sql(
            """
            SELECT count(*) FROM problem_create_event e
            WHERE e.org_id = :orgId AND e.user_id = :user AND e.created_at > :since
            """)
        .param("user", user)
        .param("since", odt(since))
        .query(Integer.class)
        .single();
  }

  public Optional<Instant> oldestSince(UUID user, Instant since) {
    return jdbc.sql(
            """
            SELECT min(e.created_at) FROM problem_create_event e
            WHERE e.org_id = :orgId AND e.user_id = :user AND e.created_at > :since
            """)
        .param("user", user)
        .param("since", odt(since))
        .query((rs, i) -> instant(rs, "min"))
        .optional();
  }

  public void insertCreateEvent(UUID id, UUID user, String channel, UUID ref, Instant at) {
    jdbc.sql(
            """
            INSERT INTO problem_create_event (id, org_id, user_id, channel, ref_id, created_at)
            VALUES (:id, :orgId, :user, :channel, :ref, :at)
            """)
        .param("id", id)
        .param("user", user)
        .param("channel", channel)
        .param("ref", ref)
        .param("at", odt(at))
        .update();
  }

  public void insertDoc(
      UUID id, UUID owner, String title, String statement, String figure, Instant at) {
    jdbc.sql(
            """
            INSERT INTO solution_doc (id, org_id, owner_user_id, title, statement, figure, created_at)
            VALUES (:id, :orgId, :owner, :title, :statement, CAST(:figure AS jsonb), :at)
            """)
        .param("id", id)
        .param("owner", owner)
        .param("title", title)
        .param("statement", statement)
        .param("figure", figure)
        .param("at", odt(at))
        .update();
  }

  /**
   * Bản sao từ Kho chung: chép đề bài, hình và (qua cổng) bước giải thành phiên bản 1 "Đang sửa".
   */
  public Optional<String> libraryFigure(UUID item) {
    return jdbc.sql(
            "SELECT l.figure::text FROM library_problem l WHERE l.org_id = :orgId AND l.id = :id")
        .param("id", item)
        .query(String.class)
        .optional();
  }

  public void insertFirstVersion(UUID id, UUID doc, String stepsJson, UUID author, Instant at) {
    jdbc.sql(
            """
            INSERT INTO solution_version (id, org_id, doc_id, version, steps, ai_authored, author_user_id, note,
                                          human_status, created_at)
            VALUES (:id, :orgId, :doc, 1, CAST(:steps AS jsonb), false, :author, 'Chép từ Kho chung', 'editing', :at)
            """)
        .param("id", id)
        .param("doc", doc)
        .param("steps", stepsJson)
        .param("author", author)
        .param("at", odt(at))
        .update();
    jdbc.sql("UPDATE solution_doc SET latest_version = 1 WHERE org_id = :orgId AND id = :doc")
        .param("doc", doc)
        .update();
  }

  // ---------------------------------------------------------------- Trình chiếu (S2-E7)

  /**
   * Bài giao lớp của giáo viên cho đề này: cùng version đề kho hoặc đã ghim sổ này (chưa lưu trữ).
   */
  public List<ClassRelease> classReleases(UUID teacher, UUID docId) {
    return jdbc.sql(
            """
            SELECT c.name, a.release_at
            FROM solution_doc d
            JOIN assignment a ON a.org_id = :orgId AND a.kind = 'class' AND a.assigned_by = :teacher
                             AND a.archived_at IS NULL
                             AND ((a.content_org_id = d.content_org_id AND a.problem_version_id = d.problem_version_id)
                                  OR EXISTS (SELECT 1 FROM solution_assignment_pin p
                                             WHERE p.org_id = :orgId AND p.assignment_id = a.id AND p.doc_id = d.id))
            JOIN class c ON c.org_id = :orgId AND c.id = a.class_id
            WHERE d.org_id = :orgId AND d.id = :doc
            ORDER BY c.name, a.id LIMIT 100
            -- sqllint:allow=ownership read model: lớp và giờ mở lời giải của bài giao (module assignment, iam)
            """)
        .param("teacher", teacher)
        .param("doc", docId)
        .query((rs, i) -> new ClassRelease(rs.getString(1), instant(rs, "release_at")))
        .list();
  }

  /** Phiên bản để chiếu: bản "Giáo viên đã duyệt" mới nhất, nếu chưa có thì bản mới nhất. */
  public Optional<Integer> presentVersion(UUID docId) {
    return jdbc.sql(
            """
            SELECT v.version FROM solution_version v
            WHERE v.org_id = :orgId AND v.doc_id = :doc
            ORDER BY (v.human_status = 'teacher_approved') DESC, v.version DESC LIMIT 1
            """)
        .param("doc", docId)
        .query(Integer.class)
        .optional();
  }

  public Optional<String[]> versionLabels(UUID docId, int version) {
    return jdbc.sql(
            """
            SELECT v.machine_status::text, v.human_status::text FROM solution_version v
            WHERE v.org_id = :orgId AND v.doc_id = :doc AND v.version = :version LIMIT 1
            """)
        .param("doc", docId)
        .param("version", version)
        .query((rs, i) -> new String[] {rs.getString(1), rs.getString(2)})
        .optional();
  }

  public Optional<String> figure(UUID docId) {
    return jdbc.sql(
            "SELECT d.figure::text FROM solution_doc d WHERE d.org_id = :orgId AND d.id = :doc")
        .param("doc", docId)
        .query(String.class)
        .optional();
  }

  /** Bước giải của chủ sổ qua cổng của S2-E5 (fn_solution_content_teacher). */
  public Optional<String> ownerSteps(UUID owner, UUID docId, int version) {
    return jdbc.sql(
            "SELECT steps::text FROM fn_solution_content_teacher(:orgId, :user, :doc, :version)")
        .param("user", owner)
        .param("doc", docId)
        .param("version", version)
        .query(String.class)
        .optional();
  }
}
