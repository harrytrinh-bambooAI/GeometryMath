package vn.geo.server.tracking.internal.classboard.repo;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.id.Uuid7;

/**
 * MÓC KIỂM THỬ S3-E3 (chỉ dev/test, PO-51, D-GEO-169): dựng lớp, học sinh (Đang học / Chờ duyệt /
 * Đã rời), bộ Đã giao hoặc Nháp trên bộ đề 's3e2_kho' (seed 920) và tương tác (mở đề, gợi ý, câu
 * hỏi, lượt gửi đáp số) trong tổ chức thử "Nhóm thử S3-E3" (seed 930) — mỗi ca kiểm một bảng
 * F-S3-051 riêng, không giẫm dữ liệu mẫu chung.
 */
@Repository
@Profile({"dev", "test"})
public class S3E3SandboxRepository {

  public static final UUID ORG = UUID.fromString("00000000-0000-7000-8003-000000930001");

  private final JdbcClient jdbc;

  public S3E3SandboxRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public UUID createTeacher(String displayName) {
    UUID id = Uuid7.next();
    jdbc.sql(
            """
            INSERT INTO app_user (id, org_id, role, account_kind, display_name)
            VALUES (:id, :org, 'teacher', 'sample', :name)
            -- sqllint:allow=ownership móc kiểm thử S3-E3 dev/test: giáo viên riêng cho ca kiểm
            """)
        .param("id", id)
        .param("org", ORG)
        .param("name", displayName)
        .update();
    return id;
  }

  public UUID createStudent(String displayName, String userCode) {
    UUID id = Uuid7.next();
    jdbc.sql(
            """
            INSERT INTO app_user (id, org_id, role, account_kind, display_name, user_code, password_hash,
                                  recovery_hmac, privacy_ack_version, privacy_ack_at)
            VALUES (:id, :org, 'student', 'light', :name, :code, 'x-sandbox', decode(repeat('00', 32), 'hex'), '1.0', now())
            -- sqllint:allow=ownership móc kiểm thử S3-E3 dev/test: học sinh có mã người dùng, không đăng nhập bằng mật khẩu
            """)
        .param("id", id)
        .param("org", ORG)
        .param("name", displayName)
        .param("code", userCode)
        .update();
    return id;
  }

  public void createClass(UUID id, UUID teacher, String name, String joinCode) {
    jdbc.sql(
            """
            INSERT INTO class (id, org_id, name, level, grade, join_code, teacher_id)
            VALUES (:id, :org, :name, 'thpt', 11, :code, :teacher)
            -- sqllint:allow=ownership móc kiểm thử S3-E3 dev/test: lớp riêng cho ca kiểm
            """)
        .param("id", id)
        .param("org", ORG)
        .param("name", name)
        .param("code", joinCode)
        .param("teacher", teacher)
        .update();
  }

  public void addMember(UUID classId, UUID userId, String nameKey, String status) {
    jdbc.sql(
            """
            INSERT INTO class_member (org_id, class_id, user_id, name_key, status, joined_at)
            VALUES (:org, :classId, :userId, :key, CAST(:status AS class_member_status), now() - interval '14 days')
            -- sqllint:allow=ownership,tenant móc kiểm thử S3-E3 dev/test: thành viên lớp theo trạng thái
            """)
        .param("org", ORG)
        .param("classId", classId)
        .param("userId", userId)
        .param("key", nameKey)
        .param("status", status)
        .update();
  }

  public void createSet(
      UUID id, UUID teacher, UUID classId, String title, boolean assigned, int minutesAgo) {
    jdbc.sql(
            """
            INSERT INTO assignment_set (id, org_id, owner_user_id, class_id, title, status, assigned_at, created_at, updated_at)
            VALUES (:id, :org, :teacher, :classId, :title,
                    CAST(:status AS assignment_set_status),
                    CASE WHEN :assigned THEN now() - :ago * interval '1 minute' END,
                    now() - interval '2 days', now())
            -- sqllint:allow=ownership,tenant móc kiểm thử S3-E3 dev/test: bộ bài dựng sẵn
            """)
        .param("id", id)
        .param("org", ORG)
        .param("teacher", teacher)
        .param("classId", classId)
        .param("title", title)
        .param("status", assigned ? "assigned" : "draft")
        .param("assigned", assigned)
        .param("ago", minutesAgo)
        .update();
  }

  /**
   * Câu thứ {@code position} của bộ dùng đề BOB-{@code problemNo}; trả mã bài giao (null nếu Nháp).
   */
  public UUID addItem(
      UUID setId, UUID teacher, UUID classId, int position, int problemNo, boolean assigned) {
    UUID assignmentId = assigned ? Uuid7.next() : null;
    UUID problem = fixed(921_000 + problemNo);
    UUID version = fixed(922_000 + problemNo);
    if (assigned) {
      jdbc.sql(
              """
              INSERT INTO assignment (id, org_id, kind, class_id, content_org_id, problem_id, problem_version_id,
                                      assigned_by, release_at, release_rev, created_at)
              VALUES (:id, :org, 'class', :classId, platform_org_id(), :problem, :version, :teacher,
                      now() + interval '7 days', 1, now() - interval '2 days')
              -- sqllint:allow=ownership,tenant móc kiểm thử S3-E3 dev/test: hàng bài giao của câu trong bộ
              """)
          .param("id", assignmentId)
          .param("org", ORG)
          .param("classId", classId)
          .param("problem", problem)
          .param("version", version)
          .param("teacher", teacher)
          .update();
    }
    jdbc.sql(
            """
            INSERT INTO assignment_item (id, org_id, set_id, position, content_org_id, problem_id, problem_version_id,
                                         assignment_id, answer_snapshot)
            VALUES (:id, :org, :setId, :position, platform_org_id(), :problem, :version, :assignmentId,
                    CASE WHEN CAST(:assignmentId AS uuid) IS NOT NULL THEN '1' END)
            -- sqllint:allow=ownership,tenant móc kiểm thử S3-E3 dev/test: câu của bộ
            """)
        .param("id", Uuid7.next())
        .param("org", ORG)
        .param("setId", setId)
        .param("position", position)
        .param("problem", problem)
        .param("version", version)
        .param("assignmentId", assignmentId)
        .update();
    return assignmentId;
  }

  /** Mở đề / chạm bài: có hàng tiến độ (bộ đếm gợi ý, câu hỏi cộng dồn). */
  public void touch(UUID assignmentId, UUID userId, Instant at, int hintLevel, int asks) {
    jdbc.sql(
            """
            INSERT INTO assignment_progress AS ap (org_id, assignment_id, user_id, first_opened_at, last_active_at,
                   hint_level_max, hint_view_count, ask_count, active_seconds)
            VALUES (:org, :id, :user, :at, :at, CAST(:hint AS smallint), CASE WHEN :hint > 0 THEN 1 ELSE 0 END, :asks, 60)
            ON CONFLICT (org_id, assignment_id, user_id) DO UPDATE
              SET last_active_at = GREATEST(ap.last_active_at, EXCLUDED.last_active_at),
                  hint_level_max = GREATEST(ap.hint_level_max, EXCLUDED.hint_level_max),
                  hint_view_count = ap.hint_view_count + EXCLUDED.hint_view_count,
                  ask_count = ap.ask_count + EXCLUDED.ask_count
            -- sqllint:allow=ownership,tenant móc kiểm thử S3-E3 dev/test: tiến độ dựng sẵn
            """)
        .param("org", ORG)
        .param("id", assignmentId)
        .param("user", userId)
        .param("at", ts(at))
        .param("hint", hintLevel)
        .param("asks", asks)
        .update();
  }

  public void hint(UUID assignmentId, UUID userId, Instant at, int level) {
    jdbc.sql(
            """
            INSERT INTO event_log (occurred_at, org_id, assignment_id, user_id, kind, arg)
            VALUES (:at, :org, :id, :user, 'hint', CAST(:level AS smallint))
            -- sqllint:allow=ownership,tenant móc kiểm thử S3-E3 dev/test: lượt xin gợi ý dựng sẵn
            """)
        .param("at", ts(at))
        .param("org", ORG)
        .param("id", assignmentId)
        .param("user", userId)
        .param("level", level)
        .update();
  }

  public void question(UUID assignmentId, UUID userId, Instant at, String text) {
    jdbc.sql(
            """
            INSERT INTO qa_turn (org_id, assignment_id, user_id, request_id, asked_at, question, reply_kind,
                                 reply_text, guard_version)
            VALUES (:org, :id, :user, :rq, :at, :q, 'concept', 'Câu trả lời mẫu của ca kiểm S3-E3.', 'g0-s3e3')
            -- sqllint:allow=ownership,tenant móc kiểm thử S3-E3 dev/test: câu hỏi dựng sẵn
            """)
        .param("org", ORG)
        .param("id", assignmentId)
        .param("user", userId)
        .param("rq", Uuid7.next())
        .param("at", ts(at))
        .param("q", text)
        .update();
  }

  public void attempt(UUID assignmentId, UUID userId, Instant at, String value, boolean correct) {
    jdbc.sql(
            """
            INSERT INTO practice_attempt (id, org_id, assignment_id, user_id, request_key, submitted, outcome,
                                          wrong_after, created_at)
            VALUES (:id, :org, :assignment, :user, :key, :value, :outcome,
                    (SELECT count(*) FROM practice_attempt p
                      WHERE p.org_id = :org AND p.assignment_id = :assignment AND p.user_id = :user
                        AND p.outcome = 'wrong') + CASE WHEN :outcome = 'wrong' THEN 1 ELSE 0 END,
                    :at)
            -- sqllint:allow=ownership,tenant móc kiểm thử S3-E3 dev/test: lượt gửi đáp số dựng sẵn
            """)
        .param("id", Uuid7.next())
        .param("org", ORG)
        .param("assignment", assignmentId)
        .param("user", userId)
        .param("key", "s3e3-" + UUID.randomUUID().toString().substring(0, 18))
        .param("value", value)
        .param("outcome", correct ? "correct" : "wrong")
        .param("at", ts(at))
        .update();
  }

  private static OffsetDateTime ts(Instant at) {
    return OffsetDateTime.ofInstant(at, ZoneOffset.UTC);
  }

  private static UUID fixed(int n) {
    return UUID.fromString("00000000-0000-7000-8003-" + String.format("%012d", n));
  }
}
