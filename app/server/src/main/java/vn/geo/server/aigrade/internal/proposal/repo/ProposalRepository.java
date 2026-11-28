package vn.geo.server.aigrade.internal.proposal.repo;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Bảng ai_grade_proposal, criterion_assessment, engine_signal (V725; US-S5E6-01, 03) và read model
 * đọc chéo (ADR-GEO-01) bài nộp, bản đọc chữ, bài có chấm, rubric (assessment), đơn vị kiến thức
 * (lessonplan), đề và đáp số engine (content, qua hàm cổng {@code fn_get_solution_teacher}).
 */
@Repository
public class ProposalRepository {

  private final JdbcClient jdbc;

  public ProposalRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  // ------------------------------------------------------------------ read model

  /** Bài nộp chờ chấm nền: bản đọc chữ mới nhất (đọc được) của phiên bản chấm chưa có đề xuất. */
  public record Candidate(
      UUID orgId,
      UUID submissionId,
      UUID versionId,
      int submissionVersion,
      int pageCount,
      UUID ocrId,
      int ocrVersion,
      String ocrPages,
      UUID assignmentId,
      UUID classId,
      UUID studentId,
      UUID teacherId,
      UUID problemVersionId,
      UUID rubricVersionId,
      BigDecimal scale,
      String academicYear) {}

  private static final String CANDIDATE_SQL =
      """
      SELECT s.org_id, s.id AS submission_id, v.id AS version_id, v.version AS sub_version, v.pages AS page_count,
             o.id AS ocr_id, o.version AS ocr_version, o.pages::text AS ocr_pages,
             g.id AS assignment_id, s.class_id, s.student_id, g.teacher_id, g.problem_version_id,
             g.rubric_version_id, g.scale, s.academic_year
      FROM submission s
      JOIN graded_assignment g ON g.org_id = s.org_id AND g.id = s.assignment_id
      JOIN submission_version v ON v.org_id = s.org_id AND v.submission_id = s.id AND v.version = s.current_version
      JOIN LATERAL (SELECT oa.id, oa.version, oa.status, oa.pages, oa.created_at FROM ocr_artifact oa
                    WHERE oa.org_id = s.org_id AND oa.version_id = v.id ORDER BY oa.version DESC LIMIT 1) o ON TRUE
      WHERE g.grading_mode = 'ai_proposal' AND o.status = 'done'
      -- sqllint:allow=tenant,ownership read model: bài nộp, bản đọc chữ, bài có chấm (assessment); job nền quét mọi trường
      """;

  private static final String NOT_GRADED =
      """
        AND NOT EXISTS (SELECT 1 FROM ai_grade_proposal p
                        WHERE p.org_id = s.org_id AND p.version_id = v.id AND p.ocr_version = o.version)
      -- sqllint:allow=tenant điều kiện nối vào truy vấn read model ở trên (org theo bài nộp)
      """;

  private static Candidate candidate(ResultSet rs, int i) throws SQLException {
    return new Candidate(
        rs.getObject("org_id", UUID.class),
        rs.getObject("submission_id", UUID.class),
        rs.getObject("version_id", UUID.class),
        rs.getInt("sub_version"),
        rs.getInt("page_count"),
        rs.getObject("ocr_id", UUID.class),
        rs.getInt("ocr_version"),
        rs.getString("ocr_pages"),
        rs.getObject("assignment_id", UUID.class),
        rs.getObject("class_id", UUID.class),
        rs.getObject("student_id", UUID.class),
        rs.getObject("teacher_id", UUID.class),
        rs.getObject("problem_version_id", UUID.class),
        rs.getObject("rubric_version_id", UUID.class),
        rs.getBigDecimal("scale"),
        rs.getString("academic_year"));
  }

  /** Việc chờ của mọi trường (job nền), cũ trước. */
  public List<Candidate> pending(int limit) {
    return jdbc.sql(
            CANDIDATE_SQL
                + NOT_GRADED
                + """
                ORDER BY o.created_at LIMIT :n
                -- sqllint:allow=tenant,ownership job nền quét mọi trường; đọc bài nộp, bản đọc chữ, bài có chấm (assessment)
                """)
        .param("n", limit)
        .query(ProposalRepository::candidate)
        .list();
  }

  /** Việc chờ của một bài nộp (móc kiểm thử chạy ngay). */
  public Optional<Candidate> pendingOf(UUID orgId, UUID submissionId) {
    return of(orgId, submissionId, true);
  }

  /** Bản đọc chữ mới nhất của phiên bản chấm (đã chấm hay chưa; móc xem tin nhắn). */
  public Optional<Candidate> currentOf(UUID orgId, UUID submissionId) {
    return of(orgId, submissionId, false);
  }

  private Optional<Candidate> of(UUID orgId, UUID submissionId, boolean pendingOnly) {
    return jdbc.sql(
            CANDIDATE_SQL
                + (pendingOnly ? NOT_GRADED : "")
                + """
                AND s.org_id = :orgId AND s.id = :sid LIMIT 1
                -- sqllint:allow=tenant,ownership đọc bài nộp, bản đọc chữ, bài có chấm (assessment) của một bài
                """)
        .param("orgId", orgId)
        .param("sid", submissionId)
        .query(ProposalRepository::candidate)
        .optional();
  }

  /** Tiêu chí của rubric phiên bản: ord, tên, tối đa, phụ thuộc hướng. */
  public record RubricRow(int ord, String name, BigDecimal max, boolean directionDependent) {}

  public List<RubricRow> criteria(UUID orgId, UUID rubricVersionId) {
    return jdbc.sql(
            """
            SELECT c.ord, c.name, c.max_points, c.direction_dependent FROM rubric_criterion c
            WHERE c.org_id = :orgId AND c.rubric_version_id = :rv ORDER BY c.ord LIMIT 10
            -- sqllint:allow=ownership tiêu chí rubric (assessment) đưa vào task grade và luật chặn
            """)
        .param("orgId", orgId)
        .param("rv", rubricVersionId)
        .query((rs, i) ->
            new RubricRow(
                rs.getInt("ord"),
                rs.getString("name"),
                rs.getBigDecimal("max_points"),
                rs.getBoolean("direction_dependent")))
        .list();
  }

  /** Số phiên bản của rubric phiên bản. */
  public int rubricVersion(UUID orgId, UUID rubricVersionId) {
    return jdbc.sql(
            """
            SELECT r.version FROM rubric_version r WHERE r.org_id = :orgId AND r.id = :rv
            -- sqllint:allow=ownership số phiên bản rubric (assessment) ghi vào đề xuất
            """)
        .param("orgId", orgId)
        .param("rv", rubricVersionId)
        .query(Integer.class)
        .optional()
        .orElse(1);
  }

  /** Mã đơn vị kiến thức có trong kho (luật KPI §2.5). */
  public Set<String> knowledgeCodes() {
    return new HashSet<>(
        jdbc.sql(
                """
                SELECT k.code FROM lp_knowledge_unit k LIMIT 5000
                -- sqllint:allow=ownership danh mục kiến thức (lessonplan) để gắn cờ "bịa kiến thức"
                """)
            .query(String.class)
            .list());
  }

  /** Đề bài (nội dung đề kho dùng chung). */
  public Optional<String> statement(UUID problemVersionId) {
    if (problemVersionId == null) {
      return Optional.empty();
    }
    return jdbc.sql(
            """
            SELECT pv.statement FROM problem_version pv WHERE pv.org_id = platform_org_id() AND pv.id = :pv LIMIT 1
            -- sqllint:allow=tenant,ownership đề kho dùng chung (content, org nền tảng) đưa vào task grade
            """)
        .param("pv", problemVersionId)
        .query(String.class)
        .optional();
  }

  /** Đáp số engine của đề qua hàm cổng (giáo viên chủ bài thấy lời giải). */
  public Optional<String> engineAnswer(UUID orgId, UUID teacherId, UUID problemVersionId) {
    if (problemVersionId == null) {
      return Optional.empty();
    }
    return jdbc.sql(
            """
            SELECT final_answer FROM fn_get_solution_teacher(:orgId, :teacherId, :pv) LIMIT 1
            """)
        .param("orgId", orgId)
        .param("teacherId", teacherId)
        .param("pv", problemVersionId)
        .query(String.class)
        .optional();
  }

  /** Bài nộp (để kiểm quan hệ): lớp, học sinh, bài có chấm, phiên bản chấm. */
  public record SubRef(
      UUID id,
      UUID assignmentId,
      UUID classId,
      UUID studentId,
      int currentVersion,
      UUID teacherId) {}

  public Optional<SubRef> submission(UUID orgId, UUID submissionId) {
    return jdbc.sql(
            """
            SELECT s.id, s.assignment_id, s.class_id, s.student_id, s.current_version, c.teacher_id
            FROM submission s JOIN class c ON c.org_id = s.org_id AND c.id = s.class_id
            WHERE s.org_id = :orgId AND s.id = :sid AND c.org_id = :orgId
            -- sqllint:allow=ownership bài nộp (assessment) và chủ lớp (iam) để kiểm quan hệ
            """)
        .param("orgId", orgId)
        .param("sid", submissionId)
        .query((rs, i) ->
            new SubRef(
                rs.getObject("id", UUID.class),
                rs.getObject("assignment_id", UUID.class),
                rs.getObject("class_id", UUID.class),
                rs.getObject("student_id", UUID.class),
                rs.getInt("current_version"),
                rs.getObject("teacher_id", UUID.class)))
        .optional();
  }

  /** Chủ lớp của bài có chấm. */
  public Optional<UUID> assignmentTeacher(UUID orgId, UUID assignmentId) {
    return jdbc.sql(
            """
            SELECT c.teacher_id FROM graded_assignment g JOIN class c ON c.org_id = g.org_id AND c.id = g.class_id
            WHERE g.org_id = :orgId AND g.id = :a AND c.org_id = :orgId
            -- sqllint:allow=ownership bài có chấm (assessment) và chủ lớp (iam) để kiểm quan hệ
            """)
        .param("orgId", orgId)
        .param("a", assignmentId)
        .query(UUID.class)
        .optional();
  }

  // ------------------------------------------------------------------ ghi

  public record NewProposal(
      UUID id,
      Candidate c,
      int rubricVersion,
      String status,
      String reason,
      BigDecimal total,
      Double confidence,
      String flagsJson,
      String stepsJson,
      String model,
      String promptVersion,
      int attempts,
      int tokens) {}

  /** Thêm đề xuất; false nếu đã có (cùng phiên bản bản đọc chữ, chạy trùng). */
  public boolean insert(NewProposal p) {
    Candidate c = p.c();
    return jdbc.sql(
                """
                INSERT INTO ai_grade_proposal (id, org_id, submission_id, version_id, submission_version, ocr_artifact_id,
                    ocr_version, assignment_id, class_id, student_id, problem_version_id, rubric_version_id, rubric_version,
                    status, reason, total, scale, confidence, flags, step_marks, model, prompt_version, attempts, tokens, academic_year)
                VALUES (:id, :orgId, :sid, :vid, :sv, :oid, :ov, :aid, :cid, :stid, :pv, :rv, :rvn,
                    :status, :reason, :total, :scale, :conf, CAST(:flags AS jsonb), CAST(:steps AS jsonb), :model, :pver,
                    :att, :tok, :y)
                ON CONFLICT (org_id, version_id, ocr_version) DO NOTHING
                """)
            .param("id", p.id())
            .param("orgId", c.orgId())
            .param("sid", c.submissionId())
            .param("vid", c.versionId())
            .param("sv", c.submissionVersion())
            .param("oid", c.ocrId())
            .param("ov", c.ocrVersion())
            .param("aid", c.assignmentId())
            .param("cid", c.classId())
            .param("stid", c.studentId())
            .param("pv", c.problemVersionId())
            .param("rv", c.rubricVersionId())
            .param("rvn", p.rubricVersion())
            .param("status", p.status())
            .param("reason", p.reason())
            .param("total", p.total())
            .param("scale", c.scale())
            .param("conf", p.confidence())
            .param("flags", p.flagsJson())
            .param("steps", p.stepsJson())
            .param("model", p.model())
            .param("pver", p.promptVersion())
            .param("att", p.attempts())
            .param("tok", p.tokens())
            .param("y", c.academicYear())
            .update()
        == 1;
  }

  public record NewCriterion(
      int ord,
      String name,
      BigDecimal max,
      BigDecimal aiPoints,
      BigDecimal points,
      String reason,
      String regionJson,
      double confidence,
      String errorLabel,
      String adjustmentsJson,
      String flagsJson) {}

  public void insertCriterion(UUID orgId, UUID proposalId, String year, NewCriterion n) {
    jdbc.sql(
            """
            INSERT INTO criterion_assessment (id, org_id, proposal_id, ord, name, max_points, ai_points, points, reason,
                region, confidence, error_label, adjustments, flags, academic_year)
            VALUES (gen_random_uuid(), :orgId, :pid, :ord, :name, :max, :ai, :pts, :reason, CAST(:region AS jsonb), :conf,
                :label, CAST(:adj AS jsonb), CAST(:flags AS jsonb), :y)
            """)
        .param("orgId", orgId)
        .param("pid", proposalId)
        .param("ord", n.ord())
        .param("name", n.name())
        .param("max", n.max())
        .param("ai", n.aiPoints())
        .param("pts", n.points())
        .param("reason", n.reason())
        .param("region", n.regionJson())
        .param("conf", n.confidence())
        .param("label", n.errorLabel())
        .param("adj", n.adjustmentsJson())
        .param("flags", n.flagsJson())
        .param("y", year)
        .update();
  }

  public void insertSignal(
      UUID orgId,
      UUID proposalId,
      UUID submissionId,
      String status,
      String studentAnswer,
      String engineAnswer,
      String rule,
      boolean disagree,
      String year) {
    jdbc.sql(
            """
            INSERT INTO engine_signal (id, org_id, proposal_id, submission_id, status, student_answer, engine_answer, rule,
                disagree, academic_year)
            VALUES (gen_random_uuid(), :orgId, :pid, :sid, :status, :sa, :ea, :rule, :dis, :y)
            """)
        .param("orgId", orgId)
        .param("pid", proposalId)
        .param("sid", submissionId)
        .param("status", status)
        .param("sa", studentAnswer)
        .param("ea", engineAnswer)
        .param("rule", rule)
        .param("dis", disagree)
        .param("y", year)
        .update();
  }

  // ------------------------------------------------------------------ đọc

  public record Proposal(
      UUID id,
      UUID submissionId,
      int submissionVersion,
      int ocrVersion,
      UUID problemVersionId,
      UUID rubricVersionId,
      int rubricVersion,
      String status,
      String reason,
      BigDecimal total,
      BigDecimal scale,
      BigDecimal confidence,
      String flags,
      String steps,
      String model,
      String promptVersion,
      int attempts,
      int tokens,
      Instant createdAt,
      String engineStatus,
      String studentAnswer,
      String engineAnswer,
      String engineRule,
      Boolean disagree) {}

  private static final String PROPOSAL_COLS =
      """
      p.id, p.submission_id, p.submission_version, p.ocr_version, p.problem_version_id, p.rubric_version_id,
      p.rubric_version, p.status, p.reason, p.total, p.scale, p.confidence, p.flags::text AS flags, p.step_marks::text AS steps,
      p.model, p.prompt_version, p.attempts, p.tokens, p.created_at,
      e.status AS engine_status, e.student_answer, e.engine_answer, e.rule AS engine_rule, e.disagree
      """;

  private static Proposal proposal(ResultSet rs, int i) throws SQLException {
    return new Proposal(
        rs.getObject("id", UUID.class),
        rs.getObject("submission_id", UUID.class),
        rs.getInt("submission_version"),
        rs.getInt("ocr_version"),
        rs.getObject("problem_version_id", UUID.class),
        rs.getObject("rubric_version_id", UUID.class),
        rs.getInt("rubric_version"),
        rs.getString("status"),
        rs.getString("reason"),
        rs.getBigDecimal("total"),
        rs.getBigDecimal("scale"),
        rs.getBigDecimal("confidence"),
        rs.getString("flags"),
        rs.getString("steps"),
        rs.getString("model"),
        rs.getString("prompt_version"),
        rs.getInt("attempts"),
        rs.getInt("tokens"),
        rs.getTimestamp("created_at").toInstant(),
        rs.getString("engine_status"),
        rs.getString("student_answer"),
        rs.getString("engine_answer"),
        rs.getString("engine_rule"),
        (Boolean) rs.getObject("disagree"));
  }

  /** Đề xuất mới nhất của phiên bản chấm (bản đọc chữ mới nhất đã chấm). */
  public Optional<Proposal> latest(UUID orgId, UUID submissionId, int submissionVersion) {
    return jdbc.sql(
            "SELECT "
                + PROPOSAL_COLS
                + """
                FROM ai_grade_proposal p
                LEFT JOIN engine_signal e ON e.org_id = p.org_id AND e.proposal_id = p.id
                WHERE p.org_id = :orgId AND p.submission_id = :sid AND p.submission_version = :sv
                ORDER BY p.ocr_version DESC, p.created_at DESC LIMIT 1
                """)
        .param("orgId", orgId)
        .param("sid", submissionId)
        .param("sv", submissionVersion)
        .query(ProposalRepository::proposal)
        .optional();
  }

  /** Đề xuất mới nhất của mỗi bài nộp (phiên bản chấm) của một bài có chấm. */
  public List<Proposal> latestOfAssignment(UUID orgId, UUID assignmentId) {
    return jdbc.sql(
            "SELECT DISTINCT ON (p.submission_id) "
                + PROPOSAL_COLS
                + """
                FROM ai_grade_proposal p
                JOIN submission s ON s.org_id = p.org_id AND s.id = p.submission_id
                     AND s.current_version = p.submission_version
                LEFT JOIN engine_signal e ON e.org_id = p.org_id AND e.proposal_id = p.id
                WHERE p.org_id = :orgId AND p.assignment_id = :a
                ORDER BY p.submission_id, p.ocr_version DESC, p.created_at DESC LIMIT 500
                -- sqllint:allow=ownership phiên bản chấm hiện tại của bài nộp (assessment)
                """)
        .param("orgId", orgId)
        .param("a", assignmentId)
        .query(ProposalRepository::proposal)
        .list();
  }

  public record CriterionRow(
      int ord,
      String name,
      BigDecimal max,
      BigDecimal aiPoints,
      BigDecimal points,
      String reason,
      String region,
      BigDecimal confidence,
      String errorLabel,
      String adjustments,
      String flags) {}

  public List<CriterionRow> criteriaOf(UUID orgId, UUID proposalId) {
    return jdbc.sql(
            """
            SELECT c.ord, c.name, c.max_points, c.ai_points, c.points, c.reason, c.region::text AS region, c.confidence,
                   c.error_label, c.adjustments::text AS adjustments, c.flags::text AS flags
            FROM criterion_assessment c WHERE c.org_id = :orgId AND c.proposal_id = :pid ORDER BY c.ord LIMIT 10
            """)
        .param("orgId", orgId)
        .param("pid", proposalId)
        .query((rs, i) ->
            new CriterionRow(
                rs.getInt("ord"),
                rs.getString("name"),
                rs.getBigDecimal("max_points"),
                rs.getBigDecimal("ai_points"),
                rs.getBigDecimal("points"),
                rs.getString("reason"),
                rs.getString("region"),
                rs.getBigDecimal("confidence"),
                rs.getString("error_label"),
                rs.getString("adjustments"),
                rs.getString("flags")))
        .list();
  }

  // ------------------------------------------------------------------ xoá (BR-S5E4-28)

  public int deleteOfStudent(UUID orgId, UUID studentId) {
    return jdbc.sql("DELETE FROM ai_grade_proposal WHERE org_id = :orgId AND student_id = :st")
        .param("orgId", orgId)
        .param("st", studentId)
        .update();
  }

  public int deleteOfYear(String year) {
    int n =
        jdbc.sql(
                """
                DELETE FROM engine_signal WHERE academic_year = :y
                -- sqllint:allow=tenant job xoá theo năm học chạy cho mọi trường (BR-S5E4-28)
                """)
            .param("y", year)
            .update();
    n +=
        jdbc.sql(
                """
                DELETE FROM criterion_assessment WHERE academic_year = :y
                -- sqllint:allow=tenant job xoá theo năm học chạy cho mọi trường (BR-S5E4-28)
                """)
            .param("y", year)
            .update();
    return n
        + jdbc.sql(
                """
                DELETE FROM ai_grade_proposal WHERE academic_year = :y
                -- sqllint:allow=tenant job xoá theo năm học chạy cho mọi trường (BR-S5E4-28)
                """)
            .param("y", year)
            .update();
  }
}
