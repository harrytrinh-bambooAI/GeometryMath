package vn.geo.server.tracking.internal.errors.repo;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * S5-E5b (US-S5E5-02/03) — read model "Cả lớp hay sai ở đâu": đọc chéo (chỉ đọc, ADR-GEO-01) bài có
 * chấm, điểm duyệt mới nhất, hành động giáo viên trên đề xuất AI, tín hiệu engine, ngân hàng đề và
 * kho tri thức; ghi lượt tổng hợp vào bảng của mình ({@code class_error_summary}). Mọi câu lọc
 * {@code org_id = :orgId}.
 */
@Repository
public class ClassErrorRepository {

  public record Assignment(
      UUID id, UUID classId, UUID teacherId, String code, String title, UUID problemVersionId) {}

  /** Điểm duyệt mới nhất của một bài nộp (criteria: [{ord, name, maxPoints, points}]). */
  public record Grade(
      UUID submissionId, String submissionCode, int seq, String criteria, String comment) {}

  /** Tiêu chí có đề xuất AI mà giáo viên Nhận hoặc Sửa (giữ) ở lượt duyệt {@code seq}. */
  public record Label(UUID submissionId, int seq, int ord, String label, String reason) {}

  public record Engine(UUID submissionId, String status) {}

  public record Person(String displayName, String userCode) {}

  public record Stored(
      UUID id,
      String inputHash,
      String mode,
      String insights,
      String works,
      String checkNote,
      int removed,
      Instant createdAt) {}

  public record BankItem(String code, String body) {}

  public record Catalog(String code, UUID problemVersionId, String statement) {}

  public record Doc(UUID id, String code, String title, int version) {}

  private final JdbcClient jdbc;

  public ClassErrorRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public Optional<Assignment> assignment(UUID orgId, UUID id) {
    return jdbc.sql(
            """
            SELECT g.id, g.class_id, g.teacher_id, g.code, g.title, g.problem_version_id
            FROM graded_assignment g WHERE g.org_id = :orgId AND g.id = :id LIMIT 1
            -- sqllint:allow=ownership bài có chấm (assessment), chỉ đọc cho tổng hợp lỗi của lớp
            """)
        .param("orgId", orgId)
        .param("id", id)
        .query((rs, i) ->
            new Assignment(
                rs.getObject(1, UUID.class),
                rs.getObject(2, UUID.class),
                rs.getObject(3, UUID.class),
                rs.getString(4),
                rs.getString(5),
                rs.getObject(6, UUID.class)))
        .optional();
  }

  /** Điểm duyệt MỚI NHẤT của mỗi bài nộp còn tồn tại (BR-S5E5-04). */
  public List<Grade> latestGrades(UUID orgId, UUID assignmentId) {
    return jdbc.sql(
            """
            SELECT DISTINCT ON (ag.submission_id) ag.submission_id, s.code, ag.seq, ag.criteria::text, ag.comment
            FROM approved_grade ag
            JOIN submission s ON s.org_id = :orgId AND s.id = ag.submission_id
            WHERE ag.org_id = :orgId AND ag.assignment_id = :a AND ag.submission_id IS NOT NULL
            ORDER BY ag.submission_id, ag.seq DESC
            -- sqllint:allow=ownership điểm duyệt và mã bài nộp (assessment), chỉ đọc; ≤ sĩ số một lớp
            """)
        .param("orgId", orgId)
        .param("a", assignmentId)
        .query((rs, i) ->
            new Grade(
                rs.getObject(1, UUID.class),
                rs.getString(2),
                rs.getInt(3),
                rs.getString(4),
                rs.getString(5)))
        .list();
  }

  /**
   * Loại lỗi chỉ lấy từ đề xuất AI mà giáo viên đã Nhận hoặc giữ khi Sửa (BR-S5E5-04); lý do của AI
   * đi kèm (vào dữ liệu ẩn danh của task class_errors).
   */
  public List<Label> labels(UUID orgId, List<UUID> submissions) {
    if (submissions.isEmpty()) {
      return List.of();
    }
    return jdbc.sql(
            """
            SELECT a.submission_id, a.grade_seq, a.ord, ca.error_label, ca.reason
            FROM ai_review_action a
            JOIN criterion_assessment ca ON ca.org_id = :orgId AND ca.proposal_id = a.proposal_id AND ca.ord = a.ord
            WHERE a.org_id = :orgId AND a.submission_id = ANY (:subs) AND a.action IN ('accept', 'edit')
            ORDER BY a.submission_id, a.grade_seq, a.ord
            LIMIT 5000
            -- sqllint:allow=ownership hành động giáo viên trên đề xuất AI và loại lỗi (aigrade), chỉ đọc
            """)
        .param("orgId", orgId)
        .param("subs", submissions.toArray(new UUID[0]))
        .query((rs, i) ->
            new Label(
                rs.getObject(1, UUID.class),
                rs.getInt(2),
                rs.getInt(3),
                rs.getString(4),
                rs.getString(5)))
        .list();
  }

  /** Tín hiệu engine mới nhất của mỗi bài (US-S5E6-03). */
  public List<Engine> engine(UUID orgId, List<UUID> submissions) {
    if (submissions.isEmpty()) {
      return List.of();
    }
    return jdbc.sql(
            """
            SELECT DISTINCT ON (e.submission_id) e.submission_id, e.status
            FROM engine_signal e
            WHERE e.org_id = :orgId AND e.submission_id = ANY (:subs)
            ORDER BY e.submission_id, e.created_at DESC
            -- sqllint:allow=ownership tín hiệu engine (aigrade), chỉ đọc
            """)
        .param("orgId", orgId)
        .param("subs", submissions.toArray(new UUID[0]))
        .query((rs, i) -> new Engine(rs.getObject(1, UUID.class), rs.getString(2)))
        .list();
  }

  /** Mọi học sinh từng ở lớp (mọi trạng thái): tên / mã để gỡ trước và sau AI (BR-S5E5-14). */
  public List<Person> roster(UUID orgId, UUID classId) {
    return jdbc.sql(
            """
            SELECT u.display_name, u.user_code FROM class_member m
            JOIN app_user u ON u.org_id = :orgId AND u.id = m.user_id
            WHERE m.org_id = :orgId AND m.class_id = :classId
            ORDER BY u.id LIMIT 500
            -- sqllint:allow=ownership tên và mã học sinh của lớp (iam) để gỡ khỏi nội dung gửi AI (BR-S5E5-14)
            """)
        .param("orgId", orgId)
        .param("classId", classId)
        .query((rs, i) -> new Person(rs.getString(1), rs.getString(2)))
        .list();
  }

  /** Đề của bài (trường hoặc kho nền tảng). */
  public Optional<String> statement(UUID orgId, UUID problemVersionId) {
    if (problemVersionId == null) {
      return Optional.empty();
    }
    return jdbc.sql(
            """
            SELECT pv.statement FROM problem_version pv
            WHERE pv.id = :pv AND (pv.org_id = :orgId OR pv.org_id = platform_org_id())
            LIMIT 1
            -- sqllint:allow=ownership,tenant đề của bài có chấm: của trường hoặc đề kho nền tảng (content), chỉ đọc
            """)
        .param("orgId", orgId)
        .param("pv", problemVersionId)
        .query(String.class)
        .optional();
  }

  /**
   * Câu ngân hàng đề (dữ liệu tham chiếu dùng chung, không org_id) thuộc họ dạng {@code family}.
   */
  public List<BankItem> bank(String family) {
    return jdbc.sql(
            """
            SELECT b.code, b.body::text FROM exam_bank_item b
            WHERE b.body->>'dang' = :f OR b.body->>'dang' LIKE :prefix
            ORDER BY b.code
            LIMIT 400
            -- sqllint:allow=ownership ngân hàng đề de-thi (exam), dữ liệu tham chiếu chỉ đọc (BR-S5E5-08)
            """)
        .param("f", family)
        .param("prefix", family + ".%")
        .query((rs, i) -> new BankItem(rs.getString(1), rs.getString(2)))
        .list();
  }

  /**
   * Đề kho nền tảng trường thấy (bộ 'main' và các bộ được cấp ở catalog_bank_access): ứng viên bài
   * luyện giao được qua S3-E2.
   */
  public List<Catalog> catalog(UUID orgId) {
    return jdbc.sql(
            """
            SELECT p.code, p.current_version_id, pv.statement FROM problem p
            JOIN problem_version pv ON pv.org_id = p.org_id AND pv.id = p.current_version_id
            WHERE p.org_id = platform_org_id()
              AND (p.bank = 'main' OR EXISTS (SELECT 1 FROM catalog_bank_access b WHERE b.org_id = :orgId AND b.bank = p.bank))
            ORDER BY p.code
            LIMIT 500
            -- sqllint:allow=ownership,tenant đề kho nền tảng trường thấy (content), chỉ đọc (BR-S5E5-08)
            """)
        .param("orgId", orgId)
        .query(
            (rs, i) -> new Catalog(rs.getString(1), rs.getObject(2, UUID.class), rs.getString(3)))
        .list();
  }

  /** Móc kiểm thử: cấp một bộ đề kho cho trường (thế giới E2E). */
  public void grantBank(UUID orgId, String bank) {
    jdbc.sql(
            """
            INSERT INTO catalog_bank_access (org_id, bank) VALUES (:orgId, :bank) ON CONFLICT DO NOTHING
            -- sqllint:allow=ownership móc kiểm thử dev/test: cấp bộ đề kho (content) cho trường của thế giới E2E
            """)
        .param("orgId", orgId)
        .param("bank", bank)
        .update();
  }

  /**
   * Tối đa 2 tài liệu Đang dùng của giáo viên, gắn lớp còn hiệu lực, có mục nói tới từ khóa của
   * dạng (BR-S5E5-08).
   */
  public List<Doc> docs(UUID orgId, UUID teacherId, UUID classId, String keyword) {
    return jdbc.sql(
            """
            SELECT d.id, d.code, d.title, v.version FROM kb_document d
            JOIN kb_document_version v ON v.org_id = :orgId AND v.doc_id = d.id AND v.status = 'active'
            WHERE d.org_id = :orgId AND d.owner_id = :t AND d.archived_at IS NULL
              AND EXISTS (SELECT 1 FROM kb_grant g WHERE g.org_id = :orgId AND g.doc_id = d.id AND g.class_id = :c
                          AND g.revoked_at IS NULL AND (g.valid_to IS NULL OR g.valid_to > now()))
              AND EXISTS (SELECT 1 FROM kb_section s WHERE s.org_id = :orgId AND s.doc_id = d.id AND s.version = v.version
                          AND s.search_tsv @@ plainto_tsquery('simple', kb_norm(:kw)))
            ORDER BY d.code
            LIMIT 2
            -- sqllint:allow=ownership tài liệu Đang dùng gắn lớp (knowledge), chỉ đọc (BR-S5E5-08)
            """)
        .param("orgId", orgId)
        .param("t", teacherId)
        .param("c", classId)
        .param("kw", keyword)
        .query((rs, i) ->
            new Doc(rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3), rs.getInt(4)))
        .list();
  }

  public void insert(
      UUID orgId,
      UUID id,
      UUID assignmentId,
      UUID teacherId,
      String hash,
      String mode,
      String insights,
      String works,
      String checkNote,
      int removed) {
    jdbc.sql(
            """
            INSERT INTO class_error_summary (id, org_id, assignment_id, teacher_id, input_hash, mode, insights,
              works, check_note, removed)
            VALUES (:id, :orgId, :a, :t, :h, :m, CAST(:ins AS jsonb), CAST(:w AS jsonb), :cn, :r)
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("a", assignmentId)
        .param("t", teacherId)
        .param("h", hash)
        .param("m", mode)
        .param("ins", insights)
        .param("w", works)
        .param("cn", checkNote)
        .param("r", removed)
        .update();
  }

  /** Lượt tổng hợp mới nhất của bài (của giáo viên chủ bài). */
  public Optional<Stored> latest(UUID orgId, UUID assignmentId) {
    return jdbc.sql(
            """
            SELECT x.id, x.input_hash, x.mode, x.insights::text, x.works::text, x.check_note, x.removed, x.created_at
            FROM class_error_summary x
            WHERE x.org_id = :orgId AND x.assignment_id = :a
            ORDER BY x.created_at DESC, x.id DESC
            LIMIT 1
            """)
        .param("orgId", orgId)
        .param("a", assignmentId)
        .query((rs, i) ->
            new Stored(
                rs.getObject(1, UUID.class),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getString(5),
                rs.getString(6),
                rs.getInt(7),
                rs.getTimestamp(8).toInstant()))
        .optional();
  }

  /** Móc kiểm thử: các lượt tổng hợp gần nhất của bài (mới nhất trước). */
  public List<Stored> recent(UUID orgId, UUID assignmentId, int limit) {
    return jdbc.sql(
            """
            SELECT x.id, x.input_hash, x.mode, x.insights::text, x.works::text, x.check_note, x.removed, x.created_at
            FROM class_error_summary x
            WHERE x.org_id = :orgId AND x.assignment_id = :a
            ORDER BY x.created_at DESC, x.id DESC
            LIMIT :n
            """)
        .param("orgId", orgId)
        .param("a", assignmentId)
        .param("n", limit)
        .query((rs, i) ->
            new Stored(
                rs.getObject(1, UUID.class),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getString(5),
                rs.getString(6),
                rs.getInt(7),
                rs.getTimestamp(8).toInstant()))
        .list();
  }

  /**
   * Móc kiểm thử: việc AI nền đã đăng ký cho một bài nộp (chấm nền thật, tắt AI chấm huỷ việc chờ).
   */
  public List<String[]> jobs(UUID orgId, UUID refId) {
    return jdbc.sql(
            """
            SELECT j.id::text, j.task, j.status FROM ai_pending_job j
            WHERE j.org_id = :orgId AND j.ref_id = :r
            ORDER BY j.created_at
            LIMIT 20
            -- sqllint:allow=ownership móc kiểm thử dev/test: việc nền của cổng hạn mức (aigateway), chỉ đọc
            """)
        .param("orgId", orgId)
        .param("r", refId)
        .query((rs, i) -> new String[] {rs.getString(1), rs.getString(2), rs.getString(3)})
        .list();
  }
}
