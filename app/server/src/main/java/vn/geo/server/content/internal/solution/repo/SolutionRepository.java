package vn.geo.server.content.internal.solution.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.geo.server.common.db.TenantJdbc;

/**
 * S2-E5: sổ lời giải có phiên bản (solution_doc, solution_version), kết quả StepCheck
 * (step_check_result) và ghim phiên bản vào bài giao (solution_assignment_pin). V160.
 */
@Repository
public class SolutionRepository {

  /** Sổ lời giải của một đề. {@code figure} là JSON (có thể null). */
  public record DocRow(
      UUID id,
      UUID ownerId,
      String code,
      String title,
      String statement,
      String figure,
      int latestVersion,
      boolean inMyShelf,
      String statedAnswer,
      String figureSource) {}

  /** Siêu dữ liệu của một phiên bản (không có bước giải: đọc qua {@link #content}). */
  public record VersionRow(
      UUID id,
      int version,
      boolean aiAuthored,
      UUID authorId,
      String authorName,
      String authorCode,
      String note,
      String humanStatus,
      String machineStatus,
      String finalState,
      Instant checkedAt,
      String approvedCode,
      Instant approvedAt,
      Instant createdAt) {}

  /**
   * Nội dung nhạy cảm của một phiên bản, chỉ chủ sổ đọc được: bước giải, StepCheck ({no, status,
   * reason, written, computed}), đề xuất của AI đang chờ ({id, afterStep, content}), ghi chú đối
   * chiếu đáp số. Mọi trường là JSON dạng chuỗi trừ {@code finalNote}.
   */
  public record Content(
      String stepsJson, String checksJson, String proposalsJson, String finalNote) {}

  /** Bài giao đang ghim một phiên bản của sổ. */
  public record PinRow(String className, int version) {}

  /** Tên và mã người dùng (GV-…). */
  public record UserRow(String name, String code) {}

  /** Nhãn của phiên bản ghim vào một bài giao (màn học sinh). */
  public record PinnedLabel(int version, String machineStatus, String humanStatus) {}

  private final TenantJdbc jdbc;

  public SolutionRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  private static Instant instant(ResultSet rs, int col) throws SQLException {
    OffsetDateTime t = rs.getObject(col, OffsetDateTime.class);
    return t == null ? null : t.toInstant();
  }

  private static DocRow docRow(ResultSet rs) throws SQLException {
    return new DocRow(
        rs.getObject(1, UUID.class),
        rs.getObject(2, UUID.class),
        rs.getString(3),
        rs.getString(4),
        rs.getString(5),
        rs.getString(6),
        rs.getInt(7),
        rs.getBoolean(8),
        rs.getString(9),
        rs.getString(10));
  }

  public Optional<DocRow> doc(UUID id) {
    return jdbc.sql(
            """
            SELECT id, owner_user_id, code, title, statement, figure::text, latest_version, in_my_shelf,
                   stated_answer, figure_source
            FROM solution_doc WHERE org_id = :orgId AND id = :id
            """)
        .param("id", id)
        .query((rs, i) -> docRow(rs))
        .optional();
  }

  /** Khóa hàng sổ trong giao dịch đang chạy (lưu phiên bản, duyệt). */
  public Optional<DocRow> lockDoc(UUID id) {
    return jdbc.sql(
            """
            SELECT id, owner_user_id, code, title, statement, figure::text, latest_version, in_my_shelf,
                   stated_answer, figure_source
            FROM solution_doc WHERE org_id = :orgId AND id = :id FOR UPDATE
            """)
        .param("id", id)
        .query((rs, i) -> docRow(rs))
        .optional();
  }

  public List<DocRow> myDocs(UUID ownerId) {
    return jdbc.sql(
            """
            SELECT id, owner_user_id, code, title, statement, figure::text, latest_version, in_my_shelf,
                   stated_answer, figure_source
            FROM solution_doc WHERE org_id = :orgId AND owner_user_id = :owner
            ORDER BY created_at DESC, id DESC LIMIT 100
            """)
        .param("owner", ownerId)
        .query((rs, i) -> docRow(rs))
        .list();
  }

  private static VersionRow versionRow(ResultSet rs) throws SQLException {
    return new VersionRow(
        rs.getObject(1, UUID.class),
        rs.getInt(2),
        rs.getBoolean(3),
        rs.getObject(4, UUID.class),
        rs.getString(5),
        rs.getString(6),
        rs.getString(7),
        rs.getString(8),
        rs.getString(9),
        rs.getString(10),
        instant(rs, 11),
        rs.getString(12),
        instant(rs, 13),
        instant(rs, 14));
  }

  public Optional<VersionRow> version(UUID docId, int version) {
    return jdbc.sql(
            """
            SELECT v.id, v.version, v.ai_authored, v.author_user_id, u.display_name, u.user_code,
                   v.note, v.human_status::text, v.machine_status::text, v.final_state,
                   v.checked_at, v.approved_code, v.approved_at, v.created_at
            FROM solution_version v
            LEFT JOIN app_user u ON u.org_id = :orgId AND u.id = v.author_user_id
            WHERE v.org_id = :orgId AND v.doc_id = :doc AND v.version = :version
            LIMIT 1
            -- sqllint:allow=ownership read model: tên và mã người sửa (module iam) cho lịch sử phiên bản
            """)
        .param("doc", docId)
        .param("version", version)
        .query((rs, i) -> versionRow(rs))
        .optional();
  }

  /** Lịch sử phiên bản, mới nhất ở trên (BR-S2E5-03). */
  public List<VersionRow> history(UUID docId) {
    return jdbc.sql(
            """
            SELECT v.id, v.version, v.ai_authored, v.author_user_id, u.display_name, u.user_code,
                   v.note, v.human_status::text, v.machine_status::text, v.final_state,
                   v.checked_at, v.approved_code, v.approved_at, v.created_at
            FROM solution_version v
            LEFT JOIN app_user u ON u.org_id = :orgId AND u.id = v.author_user_id
            WHERE v.org_id = :orgId AND v.doc_id = :doc
            ORDER BY v.version DESC LIMIT 200
            -- sqllint:allow=ownership read model: tên và mã người sửa (module iam) cho lịch sử phiên bản
            """)
        .param("doc", docId)
        .query((rs, i) -> versionRow(rs))
        .list();
  }

  /**
   * Bước giải, StepCheck và đề xuất đang chờ của một phiên bản, qua hàm cổng của CSDL (vai ứng dụng
   * không đọc thẳng các cột này); rỗng khi {@code userId} không phải giáo viên chủ sổ.
   */
  public Optional<Content> content(UUID userId, UUID docId, int version) {
    return jdbc.sql(
            """
            SELECT steps::text, checks::text, proposals::text, final_note
            FROM fn_solution_content_teacher(:orgId, :user, :doc, :version)
            """)
        .param("user", userId)
        .param("doc", docId)
        .param("version", version)
        .query((rs, i) ->
            new Content(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)))
        .optional();
  }

  /** Số bước Không đạt của một phiên bản. */
  public int failCount(UUID versionId) {
    return jdbc.sql(
            """
            SELECT count(*) FROM step_check_result
            WHERE org_id = :orgId AND version_id = :version AND status = 'fail'
            """)
        .param("version", versionId)
        .query(Integer.class)
        .single();
  }

  public List<PinRow> pins(UUID docId) {
    return jdbc.sql(
            """
            SELECT c.name, v.version
            FROM solution_assignment_pin sp
            JOIN solution_version v ON v.org_id = :orgId AND v.id = sp.version_id
            JOIN assignment a ON a.org_id = :orgId AND a.id = sp.assignment_id AND a.archived_at IS NULL
            JOIN class c ON c.org_id = :orgId AND c.id = a.class_id
            WHERE sp.org_id = :orgId AND sp.doc_id = :doc
            ORDER BY c.name LIMIT 100
            -- sqllint:allow=ownership read model: lớp đang dùng phiên bản nào (module assignment, iam)
            """)
        .param("doc", docId)
        .query((rs, i) -> new PinRow(rs.getString(1), rs.getInt(2)))
        .list();
  }

  public Optional<UserRow> user(UUID userId) {
    return jdbc.sql(
            """
            SELECT display_name, user_code FROM app_user WHERE org_id = :orgId AND id = :id LIMIT 1
            -- sqllint:allow=ownership read model: mã người duyệt (module iam) ghi vào bản duyệt
            """)
        .param("id", userId)
        .query((rs, i) -> new UserRow(rs.getString(1), rs.getString(2)))
        .optional();
  }

  /** Tăng số phiên bản mới nhất nếu vẫn bằng {@code base} (khóa lạc quan, BR-S2E5-05). */
  public boolean bumpLatest(UUID docId, int base) {
    return jdbc.sql(
                """
                UPDATE solution_doc SET latest_version = latest_version + 1
                WHERE org_id = :orgId AND id = :id AND latest_version = :base
                """)
            .param("id", docId)
            .param("base", base)
            .update()
        == 1;
  }

  public void insertVersion(
      UUID id, UUID docId, int version, String stepsJson, UUID authorId, String note) {
    jdbc.sql(
            """
            INSERT INTO solution_version (id, org_id, doc_id, version, steps, ai_authored, author_user_id, note,
                                          human_status)
            VALUES (:id, :orgId, :doc, :version, CAST(:steps AS jsonb), false, :author, :note, 'editing')
            """)
        .param("id", id)
        .param("doc", docId)
        .param("version", version)
        .param("steps", stepsJson)
        .param("author", authorId)
        .param("note", note)
        .update();
  }

  /** Chép kết quả kiểm của các bước 1…{@code maxStep} sang phiên bản mới (BR-S2E5-10). */
  public void copyChecks(UUID fromVersionId, UUID toVersionId, int maxStep) {
    jdbc.sql("SELECT fn_solution_copy_checks(:orgId, :from, :to, :maxStep)")
        .param("from", fromVersionId)
        .param("to", toVersionId)
        .param("maxStep", maxStep)
        .query(Integer.class)
        .single();
  }

  /** Xóa kết quả kiểm và đưa nhãn máy về "Cần xem lại" (trước khi kiểm lại, hoặc khi quá hạn). */
  @Transactional
  public void clearChecks(UUID versionId) {
    jdbc.sql(
            """
            UPDATE solution_version
            SET machine_status = 'needs_review', final_state = NULL, final_note = NULL, checked_at = NULL
            WHERE org_id = :orgId AND id = :version
            """)
        .param("version", versionId)
        .update();
    jdbc.sql("DELETE FROM step_check_result WHERE org_id = :orgId AND version_id = :version")
        .param("version", versionId)
        .update();
  }

  /** Thay toàn bộ kết quả kiểm của một phiên bản trong một giao dịch. */
  @Transactional
  public void replaceChecks(
      UUID versionId, String rowsJson, String finalState, String finalNote, boolean verified) {
    clearChecks(versionId);
    saveChecks(versionId, rowsJson, finalState, finalNote, verified);
  }

  /**
   * Ghi kết quả kiểm ({@code rowsJson}: mảng {no, status, reason, written, computed}) rồi nhãn máy.
   * Trigger CSDL từ chối 'verified' khi kết quả không đủ điều kiện (BR-S2E5-12).
   */
  public void saveChecks(
      UUID versionId, String rowsJson, String finalState, String finalNote, boolean verified) {
    jdbc.sql(
            """
            INSERT INTO step_check_result (org_id, version_id, step_no, status, reason, written, computed)
            SELECT :orgId, :version, s.no, CAST(s.status AS step_check_status), s.reason, s.written, s.computed
            FROM jsonb_to_recordset(CAST(:rows AS jsonb))
              AS s(no smallint, status text, reason text, written double precision, computed double precision)
            """)
        .param("version", versionId)
        .param("rows", rowsJson)
        .update();
    jdbc.sql(
            """
            UPDATE solution_version
            SET final_state = :finalState, final_note = :finalNote, checked_at = now(),
                machine_status = CAST(:machine AS verify_status)
            WHERE org_id = :orgId AND id = :version
            """)
        .param("version", versionId)
        .param("finalState", finalState)
        .param("finalNote", finalNote)
        .param("machine", verified ? "verified" : "needs_review")
        .update();
  }

  /** Ghi bản duyệt; 0 khi phiên bản đã được duyệt trước đó (BR-S2E5-21: chỉ một lần). */
  public int approve(UUID versionId, UUID by, String code) {
    return jdbc.sql(
            """
            UPDATE solution_version
            SET human_status = 'teacher_approved', approved_by = :by, approved_code = :code, approved_at = now()
            WHERE org_id = :orgId AND id = :version AND approved_at IS NULL
            """)
        .param("version", versionId)
        .param("by", by)
        .param("code", code)
        .update();
  }

  /** Bản duyệt trước thành "Bị thay thế"; đề vào ngăn "Của tôi" (BR-S2E5-19). */
  public void afterApprove(UUID docId, UUID versionId) {
    jdbc.sql(
            """
            UPDATE solution_version SET human_status = 'superseded'
            WHERE org_id = :orgId AND doc_id = :doc AND id <> :version AND human_status = 'teacher_approved'
            """)
        .param("doc", docId)
        .param("version", versionId)
        .update();
    jdbc.sql("UPDATE solution_doc SET in_my_shelf = true WHERE org_id = :orgId AND id = :doc")
        .param("doc", docId)
        .update();
  }

  /** Nhãn của phiên bản ghim vào bài giao mà học sinh đang thuộc lớp (US-S2E5-03-SC5). */
  public Optional<PinnedLabel> pinnedLabel(UUID userId, UUID assignmentId) {
    return jdbc.sql(
            """
            SELECT v.version, v.machine_status::text, v.human_status::text
            FROM solution_assignment_pin sp
            JOIN solution_version v ON v.org_id = :orgId AND v.id = sp.version_id
            JOIN assignment a ON a.org_id = :orgId AND a.id = sp.assignment_id AND a.archived_at IS NULL
            WHERE sp.org_id = :orgId AND sp.assignment_id = :id AND a.kind = 'class'
              AND EXISTS (SELECT 1 FROM class_member cm
                          WHERE cm.org_id = :orgId AND cm.class_id = a.class_id AND cm.user_id = :userId
                            AND cm.left_at IS NULL)
            LIMIT 1
            -- sqllint:allow=ownership read model: bài giao của lớp em (module assignment, iam)
            """)
        .param("id", assignmentId)
        .param("userId", userId)
        .query((rs, i) -> new PinnedLabel(rs.getInt(1), rs.getString(2), rs.getString(3)))
        .optional();
  }

  // ---------------------------------------------------------------- S2-E3 (T-261126-22)

  /** Sổ lời giải mới của một nháp đề (bốn bước soạn nháp, US-S2E3-03); chưa có phiên bản. */
  public void insertDraftDoc(
      UUID id,
      UUID ownerId,
      String title,
      String statement,
      String figureJson,
      String figureSource,
      String statedAnswer,
      UUID draftId) {
    jdbc.sql(
            """
            INSERT INTO solution_doc (id, org_id, owner_user_id, title, statement, figure, figure_source, stated_answer,
                                      draft_id, latest_version)
            VALUES (:id, :orgId, :owner, :title, :statement, CAST(:figure AS jsonb), :figureSource, :stated, :draft, 0)
            """)
        .param("id", id)
        .param("owner", ownerId)
        .param("title", title)
        .param("statement", statement)
        .param("figure", figureJson)
        .param("figureSource", figureSource)
        .param("stated", statedAnswer)
        .param("draft", draftId)
        .update();
  }

  /** Phiên bản do AI soạn (người: "AI soạn"; máy: "Cần xem lại" tới khi kiểm). */
  public void insertAiVersion(UUID id, UUID docId, int version, String stepsJson) {
    jdbc.sql(
            """
            INSERT INTO solution_version (id, org_id, doc_id, version, steps, ai_authored, author_user_id, note,
                                          human_status)
            VALUES (:id, :orgId, :doc, :version, CAST(:steps AS jsonb), true, NULL, 'AI soạn', 'ai_drafted')
            """)
        .param("id", id)
        .param("doc", docId)
        .param("version", version)
        .param("steps", stepsJson)
        .update();
  }

  /** Đáp số đề ghi (BR-S2E3-17, US-S2E3-04-SC6 "Sửa đáp số đề ghi"). */
  public void updateStatedAnswer(UUID docId, String statedAnswer) {
    jdbc.sql("UPDATE solution_doc SET stated_answer = :stated WHERE org_id = :orgId AND id = :doc")
        .param("doc", docId)
        .param("stated", statedAnswer)
        .update();
  }

  // ---------------------------------------------------------------- US-S2E5-05 (T-261126-22)

  /**
   * Bài giao đang ghim một phiên bản của sổ, kèm số học sinh đã xem lời giải và giờ mở
   * (BR-S2E5-25).
   */
  public record AssignedPin(
      UUID assignmentId,
      String className,
      UUID versionId,
      int version,
      int viewers,
      Instant releaseAt,
      UUID assignedBy) {}

  public List<AssignedPin> assignedPins(UUID docId) {
    return jdbc.sql(
            """
            SELECT sp.assignment_id, c.name, v.id, v.version,
                   (SELECT count(*) FROM assignment_progress ap
                    WHERE ap.org_id = :orgId AND ap.assignment_id = sp.assignment_id
                      AND ap.solution_viewed_at IS NOT NULL)::int,
                   a.release_at, a.assigned_by
            FROM solution_assignment_pin sp
            JOIN solution_version v ON v.org_id = :orgId AND v.id = sp.version_id
            JOIN assignment a ON a.org_id = :orgId AND a.id = sp.assignment_id AND a.archived_at IS NULL
            JOIN class c ON c.org_id = :orgId AND c.id = a.class_id
            WHERE sp.org_id = :orgId AND sp.doc_id = :doc
            ORDER BY c.name, sp.assignment_id LIMIT 100
            -- sqllint:allow=ownership read model: lớp, giờ mở, số học sinh đã xem lời giải (module assignment, tracking, iam)
            """)
        .param("doc", docId)
        .query((rs, i) ->
            new AssignedPin(
                rs.getObject(1, UUID.class),
                rs.getString(2),
                rs.getObject(3, UUID.class),
                rs.getInt(4),
                rs.getInt(5),
                instant(rs, 6),
                rs.getObject(7, UUID.class)))
        .list();
  }

  /**
   * "Áp dụng cho lớp": đổi phiên bản ghim của MỘT bài giao (BR-S2E5-23). 0 khi bài giao không ghim
   * sổ này.
   */
  public int repin(UUID assignmentId, UUID docId, UUID versionId) {
    return jdbc.sql(
            """
            UPDATE solution_assignment_pin SET version_id = :version, pinned_at = now()
            WHERE org_id = :orgId AND assignment_id = :assignment AND doc_id = :doc
            """)
        .param("assignment", assignmentId)
        .param("doc", docId)
        .param("version", versionId)
        .update();
  }
}
