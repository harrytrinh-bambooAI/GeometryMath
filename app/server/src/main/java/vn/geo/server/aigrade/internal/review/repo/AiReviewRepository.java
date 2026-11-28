package vn.geo.server.aigrade.internal.review.repo;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Hành động của giáo viên trên tiêu chí có đề xuất AI ({@code ai_review_action}, V755). */
@Repository
public class AiReviewRepository {

  private final JdbcClient jdbc;

  public AiReviewRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public record Action(
      int gradeSeq,
      int ord,
      String name,
      BigDecimal max,
      String action,
      BigDecimal aiPoints,
      BigDecimal teacherPoints) {}

  public void insert(
      UUID orgId,
      UUID id,
      UUID submissionId,
      UUID proposalId,
      UUID studentId,
      String academicYear,
      int seq,
      int ord,
      String name,
      BigDecimal max,
      String action,
      BigDecimal ai,
      BigDecimal teacher) {
    jdbc.sql(
            """
            INSERT INTO ai_review_action (id, org_id, submission_id, proposal_id, student_id, grade_seq, ord, name,
                                          max_points, action, ai_points, teacher_points, academic_year)
            VALUES (:id, :orgId, :sid, :pid, :st, :seq, :ord, :name, :max, :act, :ap, :tp, :y)
            ON CONFLICT (org_id, submission_id, grade_seq, ord) DO NOTHING
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("sid", submissionId)
        .param("pid", proposalId)
        .param("st", studentId)
        .param("seq", seq)
        .param("ord", ord)
        .param("name", name)
        .param("max", max)
        .param("act", action)
        .param("ap", ai)
        .param("tp", teacher)
        .param("y", academicYear)
        .update();
  }

  /** Hành động của lần duyệt mới nhất có ghi (rỗng nếu chưa có). */
  public List<Action> latest(UUID orgId, UUID submissionId) {
    return jdbc.sql(
            """
            SELECT grade_seq, ord, name, max_points, action, ai_points, teacher_points FROM ai_review_action a
            WHERE a.org_id = :orgId AND a.submission_id = :sid
              AND a.grade_seq = (SELECT max(b.grade_seq) FROM ai_review_action b
                                 WHERE b.org_id = :orgId AND b.submission_id = :sid)
            ORDER BY ord LIMIT 10
            """)
        .param("orgId", orgId)
        .param("sid", submissionId)
        .query((rs, i) ->
            new Action(
                rs.getInt("grade_seq"),
                rs.getInt("ord"),
                rs.getString("name"),
                rs.getBigDecimal("max_points"),
                rs.getString("action"),
                rs.getBigDecimal("ai_points"),
                rs.getBigDecimal("teacher_points")))
        .list();
  }
}
