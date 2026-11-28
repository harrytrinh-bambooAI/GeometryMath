package vn.geo.server.aigrade.internal.shadow.repo;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import vn.geo.server.aigrade.internal.proposal.repo.ProposalRepository.Candidate;

/**
 * Chạy song song (US-S5E6-05; T-261128-62): read model bài nộp + chế độ chấm (assessment), việc chờ
 * chấm nền của chế độ "Chạy song song", cặp so sánh {@code ai_shadow_pair} (V755).
 */
@Repository
public class ShadowRepository {

  private final JdbcClient jdbc;

  public ShadowRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  /** Bài nộp + bài có chấm: chế độ chấm, trạng thái chấm, phiên bản chấm. */
  public record SubInfo(
      UUID id,
      UUID assignmentId,
      UUID classId,
      UUID studentId,
      UUID teacherId,
      UUID rubricVersionId,
      int currentVersion,
      String gradingState,
      String gradingMode,
      String academicYear) {

    public boolean approved() {
      return "approved".equals(gradingState) || "returned".equals(gradingState);
    }
  }

  /**
   * Số bài nộp của cùng bài có chấm nộp TRƯỚC bài này (theo giờ tạo, rồi mã) — luật "tối thiểu 2
   * bài" của lấy mẫu song song (US-S5E6-04, BR-S5E6-17; T-261128-68).
   */
  public int earlier(UUID orgId, UUID assignmentId, UUID submissionId) {
    Integer n =
        jdbc.sql(
                """
                SELECT count(*) FROM submission o
                JOIN submission s ON s.org_id = o.org_id AND s.id = :sid
                WHERE o.org_id = :orgId AND s.org_id = :orgId AND o.assignment_id = :a
                  AND (o.created_at, o.id) < (s.created_at, s.id)
                -- sqllint:allow=ownership read model: thứ tự bài nộp (assessment) cho lấy mẫu song song
                """)
            .param("orgId", orgId)
            .param("sid", submissionId)
            .param("a", assignmentId)
            .query(Integer.class)
            .single();
    return n == null ? 0 : n;
  }

  public Optional<SubInfo> info(UUID orgId, UUID submissionId) {
    return jdbc.sql(
            """
            SELECT s.id, s.assignment_id, s.class_id, s.student_id, g.teacher_id, g.rubric_version_id, s.current_version,
                   s.grading_state, g.grading_mode, s.academic_year
            FROM submission s JOIN graded_assignment g ON g.org_id = s.org_id AND g.id = s.assignment_id
            WHERE s.org_id = :orgId AND g.org_id = :orgId AND s.id = :sid LIMIT 1
            -- sqllint:allow=ownership read model: bài nộp và chế độ chấm (assessment) cho chạy song song
            """)
        .param("orgId", orgId)
        .param("sid", submissionId)
        .query((rs, i) ->
            new SubInfo(
                rs.getObject("id", UUID.class),
                rs.getObject("assignment_id", UUID.class),
                rs.getObject("class_id", UUID.class),
                rs.getObject("student_id", UUID.class),
                rs.getObject("teacher_id", UUID.class),
                rs.getObject("rubric_version_id", UUID.class),
                rs.getInt("current_version"),
                rs.getString("grading_state"),
                rs.getString("grading_mode"),
                rs.getString("academic_year")))
        .optional();
  }

  /** Chế độ chấm của bài có chấm (rỗng nếu không thấy). */
  public Optional<String> assignmentMode(UUID orgId, UUID assignmentId) {
    return jdbc.sql(
            """
            SELECT g.grading_mode FROM graded_assignment g WHERE g.org_id = :orgId AND g.id = :a LIMIT 1
            -- sqllint:allow=ownership read model: chế độ chấm (assessment) cho chạy song song
            """)
        .param("orgId", orgId)
        .param("a", assignmentId)
        .query(String.class)
        .optional();
  }

  // ------------------------------------------------------------------ việc chờ chế độ song song

  private static final String CANDIDATES =
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
      WHERE g.grading_mode = 'parallel' AND o.status = 'done'
        AND NOT EXISTS (SELECT 1 FROM ai_grade_proposal p
                        WHERE p.org_id = s.org_id AND p.version_id = v.id AND p.ocr_version = o.version)
      -- sqllint:allow=tenant,ownership read model: bài nộp, bản đọc chữ, bài có chấm (assessment); job nền quét mọi trường
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

  /** Việc chờ chấm nền của chế độ "Chạy song song" ở mọi trường (job nền), cũ trước. */
  public List<Candidate> pending(int limit) {
    return jdbc.sql(
            CANDIDATES
                + """
                ORDER BY o.created_at LIMIT :n
                -- sqllint:allow=tenant,ownership job nền quét mọi trường; đọc bài nộp, bản đọc chữ (assessment)
                """)
        .param("n", limit)
        .query(ShadowRepository::candidate)
        .list();
  }

  /** Việc chờ của một bài nộp chế độ song song (Thử chấm lại, móc kiểm thử). */
  public Optional<Candidate> pendingOf(UUID orgId, UUID submissionId) {
    return jdbc.sql(
            CANDIDATES
                + """
                AND s.org_id = :orgId AND s.id = :sid LIMIT 1
                -- sqllint:allow=tenant,ownership đọc bài nộp, bản đọc chữ (assessment) của một bài
                """)
        .param("orgId", orgId)
        .param("sid", submissionId)
        .query(ShadowRepository::candidate)
        .optional();
  }

  // ------------------------------------------------------------------ cặp so sánh

  public record Pair(
      int ord,
      String name,
      BigDecimal max,
      BigDecimal teacherPoints,
      BigDecimal aiPoints,
      UUID proposalId,
      boolean sampled) {}

  /** Ghi cặp của lần duyệt đầu; đã có thì giữ nguyên (BR-S5E6-14). */
  public void insertPair(
      UUID orgId,
      UUID id,
      SubInfo s,
      boolean sampled,
      int ord,
      String name,
      BigDecimal max,
      BigDecimal teacher,
      BigDecimal ai,
      UUID proposalId) {
    jdbc.sql(
            """
            INSERT INTO ai_shadow_pair (id, org_id, submission_id, assignment_id, class_id, student_id, sampled,
                                        ord, name, max_points, teacher_points, ai_points, proposal_id, academic_year)
            VALUES (:id, :orgId, :sid, :a, :c, :st, :sampled, :ord, :name, :max, :tp, :ap, :pid, :y)
            ON CONFLICT (org_id, submission_id, ord) DO NOTHING
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("sid", s.id())
        .param("a", s.assignmentId())
        .param("c", s.classId())
        .param("st", s.studentId())
        .param("sampled", sampled)
        .param("ord", ord)
        .param("name", name)
        .param("max", max)
        .param("tp", teacher)
        .param("ap", ai)
        .param("pid", proposalId)
        .param("y", s.academicYear())
        .update();
  }

  /** Điền điểm AI MỘT lần cho cặp còn thiếu (AI chấm xong sau lúc giáo viên duyệt). */
  public void fillAi(UUID orgId, UUID submissionId, int ord, BigDecimal ai, UUID proposalId) {
    jdbc.sql(
            """
            UPDATE ai_shadow_pair SET ai_points = :ap, proposal_id = :pid
            WHERE org_id = :orgId AND submission_id = :sid AND ord = :ord AND ai_points IS NULL
            """)
        .param("ap", ai)
        .param("pid", proposalId)
        .param("orgId", orgId)
        .param("sid", submissionId)
        .param("ord", ord)
        .update();
  }

  public List<Pair> pairs(UUID orgId, UUID submissionId) {
    return jdbc.sql(
            """
            SELECT ord, name, max_points, teacher_points, ai_points, proposal_id, sampled FROM ai_shadow_pair
            WHERE org_id = :orgId AND submission_id = :sid ORDER BY ord LIMIT 10
            """)
        .param("orgId", orgId)
        .param("sid", submissionId)
        .query((rs, i) ->
            new Pair(
                rs.getInt("ord"),
                rs.getString("name"),
                rs.getBigDecimal("max_points"),
                rs.getBigDecimal("teacher_points"),
                rs.getBigDecimal("ai_points"),
                rs.getObject("proposal_id", UUID.class),
                rs.getBoolean("sampled")))
        .list();
  }

  // ------------------------------------------------------------------ xoá (BR-S5E4-28)

  /** Ẩn danh cặp của học sinh đã xoá tài khoản (giữ cho số liệu lớp). */
  public int anonymizeStudent(UUID orgId, UUID studentId) {
    return jdbc.sql(
            "UPDATE ai_shadow_pair SET student_id = NULL WHERE org_id = :orgId AND student_id = :st")
        .param("orgId", orgId)
        .param("st", studentId)
        .update();
  }

  public int deleteOfYear(String year, UUID assignmentScope) {
    if (assignmentScope != null) {
      return jdbc.sql(
              """
              DELETE FROM ai_shadow_pair WHERE academic_year = :y AND assignment_id = :a
              -- sqllint:allow=tenant móc kiểm thử: job xoá theo năm học trong phạm vi một bài có chấm
              """)
          .param("y", year)
          .param("a", assignmentScope)
          .update();
    }
    return jdbc.sql(
            """
            DELETE FROM ai_shadow_pair WHERE academic_year = :y
            -- sqllint:allow=tenant job xoá theo năm học chạy cho mọi trường (BR-S5E4-28)
            """)
        .param("y", year)
        .update();
  }
}
