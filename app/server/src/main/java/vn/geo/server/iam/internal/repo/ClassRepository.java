package vn.geo.server.iam.internal.repo;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Lớp chặng 3 (S3-E1): tạo lớp, mã lớp, công tắc, thành viên theo trạng thái. Mọi lệnh lọc đúng
 * {@code org_id} của người gọi; riêng tra mã lớp là khóa toàn cục (D-GEO-051: chưa biết trường).
 */
@Repository
public class ClassRepository {

  public record ClassRow(
      UUID id,
      UUID orgId,
      UUID teacherId,
      String name,
      int grade,
      String academicYear,
      String joinCode,
      boolean archived,
      boolean codeJoinEnabled,
      boolean aiEnabled,
      long active,
      long pending) {}

  public record MemberRow(
      UUID userId,
      String displayName,
      String userCode,
      String status,
      String nameKey,
      Instant joinedAt,
      Instant leftAt) {}

  public record MyClass(
      UUID classId, String name, String status, boolean aiEnabled, boolean archived) {}

  private final JdbcClient jdbc;

  public ClassRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  private static final String CLASS_COLS =
      """
      c.id, c.org_id, c.teacher_id, c.name, c.grade, c.academic_year, c.join_code, c.archived_at IS NOT NULL,
      c.code_join_enabled, c.ai_enabled,
      (SELECT count(*) FROM class_member m WHERE m.org_id = c.org_id AND m.class_id = c.id AND m.status = 'active'),
      (SELECT count(*) FROM class_member m WHERE m.org_id = c.org_id AND m.class_id = c.id AND m.status = 'pending')
      """;

  private static ClassRow classRow(java.sql.ResultSet rs) throws java.sql.SQLException {
    return new ClassRow(
        rs.getObject(1, UUID.class),
        rs.getObject(2, UUID.class),
        rs.getObject(3, UUID.class),
        rs.getString(4),
        rs.getInt(5),
        rs.getString(6),
        rs.getString(7),
        rs.getBoolean(8),
        rs.getBoolean(9),
        rs.getBoolean(10),
        rs.getLong(11),
        rs.getLong(12));
  }

  /** Lớp chặng 3 của giáo viên (Đang dạy trước, rồi theo tên). */
  public List<ClassRow> teacherClasses(UUID orgId, UUID teacherId) {
    return jdbc.sql(
            "SELECT "
                + CLASS_COLS
                + """
                FROM class c WHERE c.org_id = :orgId AND c.teacher_id = :teacherId
                ORDER BY c.archived_at IS NOT NULL, c.name, c.id LIMIT 200
                """)
        .param("orgId", orgId)
        .param("teacherId", teacherId)
        .query((rs, i) -> classRow(rs))
        .list();
  }

  /** Lớp của chính giáo viên (không phải chủ lớp thì rỗng: NOT_FOUND ở tầng dịch vụ). */
  public Optional<ClassRow> ownedClass(UUID orgId, UUID teacherId, UUID classId) {
    return jdbc.sql(
            "SELECT "
                + CLASS_COLS
                + " FROM class c WHERE c.org_id = :orgId AND c.id = :id AND c.teacher_id = :teacherId LIMIT 1")
        .param("orgId", orgId)
        .param("id", classId)
        .param("teacherId", teacherId)
        .query((rs, i) -> classRow(rs))
        .optional();
  }

  public long activeClassCount(UUID orgId, UUID teacherId) {
    return jdbc.sql(
            "SELECT count(*) FROM class WHERE org_id = :orgId AND teacher_id = :teacherId AND archived_at IS NULL")
        .param("orgId", orgId)
        .param("teacherId", teacherId)
        .query(Long.class)
        .single();
  }

  public boolean joinCodeTaken(String joinCode) {
    return jdbc.sql(
            """
            SELECT EXISTS (SELECT 1 FROM class WHERE join_code = :code)
            -- sqllint:allow=tenant mã lớp là khóa toàn cục (D-GEO-051), sinh mã mới phải tránh mọi trường
            """)
        .param("code", joinCode)
        .query(Boolean.class)
        .single();
  }

  public void insertClass(
      UUID orgId, UUID id, UUID teacherId, String name, int grade, String year, String joinCode) {
    jdbc.sql(
            """
            INSERT INTO class (id, org_id, name, level, grade, join_code, teacher_id, academic_year, join_mode)
            VALUES (:id, :orgId, :name, CASE WHEN :grade >= 10 THEN 'thpt'::edu_level ELSE 'thcs'::edu_level END,
                    :grade, :code, :teacherId, :year, 'code')
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("name", name)
        .param("grade", grade)
        .param("code", joinCode)
        .param("teacherId", teacherId)
        .param("year", year)
        .update();
  }

  public int setJoinCode(UUID orgId, UUID classId, String joinCode) {
    return jdbc.sql("UPDATE class SET join_code = :code WHERE org_id = :orgId AND id = :id")
        .param("code", joinCode)
        .param("orgId", orgId)
        .param("id", classId)
        .update();
  }

  public int setSwitches(UUID orgId, UUID classId, Boolean codeJoin, Boolean ai) {
    return jdbc.sql(
            """
            UPDATE class SET code_join_enabled = COALESCE(:codeJoin, code_join_enabled),
                             ai_enabled = COALESCE(:ai, ai_enabled)
            WHERE org_id = :orgId AND id = :id
            """)
        .param("codeJoin", codeJoin)
        .param("ai", ai)
        .param("orgId", orgId)
        .param("id", classId)
        .update();
  }

  public int archive(UUID orgId, UUID classId, Instant at) {
    return jdbc.sql(
            "UPDATE class SET archived_at = :at WHERE org_id = :orgId AND id = :id AND archived_at IS NULL")
        .param("at", ts(at))
        .param("orgId", orgId)
        .param("id", classId)
        .update();
  }

  /** US-S3E6-02 (BR-S3E6-14): lưu trữ lớp thì em Chờ duyệt tự thành Bị từ chối. */
  public int rejectPending(UUID orgId, UUID classId) {
    return jdbc.sql(
            """
            UPDATE class_member SET status = 'rejected'
            WHERE org_id = :orgId AND class_id = :id AND status = 'pending'
            """)
        .param("orgId", orgId)
        .param("id", classId)
        .update();
  }

  /** US-S3E6-02 (BR-S3E6-16): bỏ lưu trữ, lớp về Đang dạy với mã lớp mới. */
  public int unarchive(UUID orgId, UUID classId, String joinCode) {
    return jdbc.sql(
            """
            UPDATE class SET archived_at = NULL, join_code = :code
            WHERE org_id = :orgId AND id = :id AND archived_at IS NOT NULL
            """)
        .param("code", joinCode)
        .param("orgId", orgId)
        .param("id", classId)
        .update();
  }

  /** Lớp chặng 3 theo mã (khóa toàn cục): còn nhận người mới khi Đang dạy và công tắc bật. */
  public record JoinTarget(UUID orgId, UUID classId, String name, boolean open) {}

  public Optional<JoinTarget> byJoinCode(String joinCode) {
    return jdbc.sql(
            """
            SELECT org_id, id, name, archived_at IS NULL AND code_join_enabled
            FROM class WHERE join_code = :code AND join_mode = 'code' LIMIT 1
            -- sqllint:allow=tenant học sinh nhập mã lớp: mã là khóa toàn cục (D-GEO-051, DQ-23)
            """)
        .param("code", joinCode)
        .query((rs, i) ->
            new JoinTarget(
                rs.getObject(1, UUID.class),
                rs.getObject(2, UUID.class),
                rs.getString(3),
                rs.getBoolean(4)))
        .optional();
  }

  /**
   * Gửi yêu cầu vào lớp; trả trạng thái TRƯỚC lệnh ('new', 'pending', 'active', 'left',
   * 'rejected').
   */
  public String joinRequest(UUID orgId, UUID classId, UUID userId, String nameKey) {
    return jdbc.sql("SELECT fn_class_join_request(:orgId, :classId, :userId, :nameKey)")
        .param("orgId", orgId)
        .param("classId", classId)
        .param("userId", userId)
        .param("nameKey", nameKey)
        .query(String.class)
        .single();
  }

  /** Lớp chặng 3 của học sinh với trạng thái thành viên (Bài của em / màn Vào lớp). */
  public List<MyClass> myClasses(UUID orgId, UUID userId) {
    return jdbc.sql(
            """
            SELECT c.id, c.name, m.status::text, c.ai_enabled, c.archived_at IS NOT NULL
            FROM class_member m JOIN class c ON c.org_id = :orgId AND c.id = m.class_id
            WHERE m.org_id = :orgId AND m.user_id = :userId AND c.join_mode = 'code'
            ORDER BY m.joined_at DESC, c.id LIMIT 50
            """)
        .param("orgId", orgId)
        .param("userId", userId)
        .query((rs, i) ->
            new MyClass(
                rs.getObject(1, UUID.class),
                rs.getString(2),
                rs.getString(3),
                rs.getBoolean(4),
                rs.getBoolean(5)))
        .list();
  }

  private static final String MEMBER_COLS =
      """
      u.id, u.display_name, u.user_code, m.status::text, m.name_key, m.joined_at, m.left_at
      FROM class_member m JOIN app_user u ON u.org_id = :orgId AND u.id = m.user_id
      """;

  private static MemberRow memberRow(java.sql.ResultSet rs) throws java.sql.SQLException {
    return new MemberRow(
        rs.getObject(1, UUID.class),
        rs.getString(2),
        rs.getString(3),
        rs.getString(4),
        rs.getString(5),
        instant(rs.getObject(6, OffsetDateTime.class)),
        instant(rs.getObject(7, OffsetDateTime.class)));
  }

  /** Chờ duyệt, cũ trước (giờ gửi); bỏ tài khoản đã xóa. */
  public List<MemberRow> pending(UUID orgId, UUID classId) {
    return jdbc.sql(
            "SELECT "
                + MEMBER_COLS
                + """
                WHERE m.org_id = :orgId AND m.class_id = :classId AND m.status = 'pending'
                  AND u.deactivated_at IS NULL
                ORDER BY m.joined_at, u.id LIMIT 200
                """)
        .param("orgId", orgId)
        .param("classId", classId)
        .query((rs, i) -> memberRow(rs))
        .list();
  }

  /**
   * Học sinh trong lớp (Đang học + Đã rời), keyset theo (name_key, user_id) tăng dần; lấy {@code
   * fetch} dòng.
   */
  public List<MemberRow> students(
      UUID orgId, UUID classId, String afterKey, UUID afterUser, int fetch) {
    return jdbc.sql(
            "SELECT "
                + MEMBER_COLS
                + """
                WHERE m.org_id = :orgId AND m.class_id = :classId AND m.status IN ('active', 'left')
                  AND u.deactivated_at IS NULL
                  AND (CAST(:afterKey AS text) IS NULL OR (m.name_key, m.user_id) > (CAST(:afterKey AS text), :afterUser))
                ORDER BY m.name_key, m.user_id LIMIT :fetch
                """)
        .param("orgId", orgId)
        .param("classId", classId)
        .param("afterKey", afterKey)
        .param("afterUser", afterUser == null ? new UUID(0, 0) : afterUser)
        .param("fetch", fetch)
        .query((rs, i) -> memberRow(rs))
        .list();
  }

  public Optional<MemberRow> member(UUID orgId, UUID classId, UUID userId) {
    return jdbc.sql(
            "SELECT "
                + MEMBER_COLS
                + """
                WHERE m.org_id = :orgId AND m.class_id = :classId AND m.user_id = :userId
                  AND u.deactivated_at IS NULL LIMIT 1
                FOR UPDATE OF m
                """)
        .param("orgId", orgId)
        .param("classId", classId)
        .param("userId", userId)
        .query((rs, i) -> memberRow(rs))
        .optional();
  }

  public int setStatus(UUID orgId, UUID classId, UUID userId, String status, String nameKey) {
    return jdbc.sql(
            """
            UPDATE class_member SET status = CAST(:status AS class_member_status), name_key = COALESCE(:nameKey, name_key)
            WHERE org_id = :orgId AND class_id = :classId AND user_id = :userId
            """)
        .param("status", status)
        .param("nameKey", nameKey)
        .param("orgId", orgId)
        .param("classId", classId)
        .param("userId", userId)
        .update();
  }

  /** Có em Đang học khác trong lớp cùng khóa tên (chỉ mục MVP class_member_name_uq). */
  public boolean activeNameTaken(UUID orgId, UUID classId, String nameKey, UUID except) {
    return jdbc.sql(
            """
            SELECT EXISTS (SELECT 1 FROM class_member WHERE org_id = :orgId AND class_id = :classId
                           AND name_key = :nameKey AND left_at IS NULL AND user_id <> :except LIMIT 1)
            """)
        .param("orgId", orgId)
        .param("classId", classId)
        .param("nameKey", nameKey)
        .param("except", except)
        .query(Boolean.class)
        .single();
  }

  // ------------------------------------------------------------------ lượt nhập mã sai (G4)

  public record AttemptWindow(long last15m, long last24h, Instant oldest15m, Instant oldest24h) {}

  public AttemptWindow attempts(UUID orgId, UUID userId, Instant now) {
    return jdbc.sql(
            """
            SELECT count(*) FILTER (WHERE attempted_at > :t15), count(*),
                   min(attempted_at) FILTER (WHERE attempted_at > :t15), min(attempted_at)
            FROM class_join_attempt
            WHERE org_id = :orgId AND user_id = :userId AND attempted_at > :t24
            """)
        .param("t15", ts(now.minusSeconds(15 * 60)))
        .param("t24", ts(now.minusSeconds(24 * 3600)))
        .param("orgId", orgId)
        .param("userId", userId)
        .query((rs, i) ->
            new AttemptWindow(
                rs.getLong(1),
                rs.getLong(2),
                instant(rs.getObject(3, OffsetDateTime.class)),
                instant(rs.getObject(4, OffsetDateTime.class))))
        .single();
  }

  public void recordFailedAttempt(UUID orgId, UUID id, UUID userId, Instant at) {
    jdbc.sql(
            "DELETE FROM class_join_attempt WHERE org_id = :orgId AND user_id = :userId AND attempted_at < :old")
        .param("orgId", orgId)
        .param("userId", userId)
        .param("old", ts(at.minusSeconds(48 * 3600)))
        .update();
    jdbc.sql(
            "INSERT INTO class_join_attempt (id, org_id, user_id, attempted_at) VALUES (:id, :orgId, :userId, :at)")
        .param("id", id)
        .param("orgId", orgId)
        .param("userId", userId)
        .param("at", ts(at))
        .update();
  }

  // ------------------------------------------------------------------ đặt lại mật khẩu

  public record ResetWindow(long count, Instant oldest) {}

  public ResetWindow resetsLastHour(UUID orgId, UUID teacherId, Instant now) {
    return jdbc.sql(
            """
            SELECT count(*), min(created_at) FROM student_password_reset
            WHERE org_id = :orgId AND teacher_id = :teacherId AND created_at > :since
            """)
        .param("orgId", orgId)
        .param("teacherId", teacherId)
        .param("since", ts(now.minusSeconds(3600)))
        .query((rs, i) ->
            new ResetWindow(rs.getLong(1), instant(rs.getObject(2, OffsetDateTime.class))))
        .single();
  }

  public void recordReset(
      UUID orgId, UUID id, UUID teacherId, UUID studentId, UUID classId, Instant at) {
    jdbc.sql(
            """
            INSERT INTO student_password_reset (id, org_id, teacher_id, student_id, class_id, created_at)
            VALUES (:id, :orgId, :teacherId, :studentId, :classId, :at)
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("teacherId", teacherId)
        .param("studentId", studentId)
        .param("classId", classId)
        .param("at", ts(at))
        .update();
  }

  /** Mật khẩu tạm: băm mới, phải đổi, hạn 24 giờ, hủy mọi phiên (BR-S3E1-31, 34). */
  public int setTempPassword(UUID orgId, UUID userId, String hash, Instant expires, Instant now) {
    return jdbc.sql(
            """
            UPDATE app_user SET password_hash = :hash, must_change_password = true,
                   temp_password_expires_at = :expires, sessions_valid_after = :now,
                   login_fail_count = 0, login_locked_until = NULL
            WHERE org_id = :orgId AND id = :id AND role = 'student' AND user_code IS NOT NULL
            """)
        .param("hash", hash)
        .param("expires", ts(expires))
        .param("now", ts(now))
        .param("orgId", orgId)
        .param("id", userId)
        .update();
  }

  /**
   * Lớp MVP (vào bằng tên) có chủ lớp đã chuyển sang mã người dùng (US-S2E1-04): ứng viên chuyển hồ
   * sơ lớp sang mô hình chặng 3 (BR-S3E1-16).
   */
  public List<UUID[]> mvpClassesToConvert(UUID orgId) {
    return jdbc.sql(
            """
            SELECT c.org_id, c.id FROM class c
            JOIN app_user u ON u.org_id = c.org_id AND u.id = c.teacher_id
            WHERE c.join_mode = 'name' AND u.user_code IS NOT NULL
              AND (CAST(:orgId AS uuid) IS NULL OR c.org_id = :orgId)
            ORDER BY c.org_id, c.id LIMIT 10000
            -- sqllint:allow=tenant lệnh chuyển dữ liệu dev/test chạy trên mọi trường (hoặc một trường chỉ định)
            """)
        .param("orgId", orgId)
        .query((rs, i) -> new UUID[] {rs.getObject(1, UUID.class), rs.getObject(2, UUID.class)})
        .list();
  }

  /**
   * Đổi lớp MVP sang lớp chặng 3: mã HK mới, vào bằng mã; chỉ khi còn là lớp MVP (chạy lại an
   * toàn).
   */
  public int convertToCodeClass(UUID orgId, UUID classId, String joinCode) {
    return jdbc.sql(
            """
            UPDATE class SET join_code = :code, join_mode = 'code'
            WHERE org_id = :orgId AND id = :id AND join_mode = 'name'
            """)
        .param("code", joinCode)
        .param("orgId", orgId)
        .param("id", classId)
        .update();
  }

  public Optional<String> displayName(UUID orgId, UUID userId) {
    return jdbc.sql("SELECT display_name FROM app_user WHERE org_id = :orgId AND id = :id LIMIT 1")
        .param("orgId", orgId)
        .param("id", userId)
        .query(String.class)
        .optional();
  }

  /**
   * Trợ lý AI có bị tắt cho bài lớp này với học sinh này không (US-S3E5-04): bài của lớp đã tắt,
   * học sinh là thành viên lớp đó.
   */
  public boolean aiBlocked(UUID orgId, UUID userId, UUID assignmentId) {
    return jdbc.sql(
            """
            SELECT EXISTS (
              SELECT 1 FROM assignment a
              JOIN class c ON c.org_id = :orgId AND c.id = a.class_id
              WHERE a.org_id = :orgId AND a.id = :aid AND a.kind = 'class' AND NOT c.ai_enabled
                AND EXISTS (SELECT 1 FROM class_member m WHERE m.org_id = :orgId AND m.class_id = c.id
                            AND m.user_id = :userId LIMIT 1)
              LIMIT 1)
            -- sqllint:allow=ownership,unbounded đọc lớp của một bài (assignment.class_id) để áp công tắc AI của lớp (US-S3E5-04)
            """)
        .param("orgId", orgId)
        .param("aid", assignmentId)
        .param("userId", userId)
        .query(Boolean.class)
        .single();
  }

  // ------------------------------------------------------------------ sự kiện bảo mật

  /**
   * PO-76 / BR-S3E5-07: tối đa một hàng mỗi (người gọi, loại đối tượng) trong {@code window}.
   * Nguyên tử nhờ khóa tư vấn mức giao dịch theo (người gọi, loại đối tượng): lượt song song chờ
   * lượt trước xong rồi mới kiểm (READ COMMITTED thấy hàng đã commit). Gọi trong giao dịch. Trả
   * true nếu đã ghi.
   */
  public boolean recordDenial(
      UUID orgId, UUID id, UUID actorId, String kind, String objectType, Duration window) {
    jdbc.sql("SELECT pg_advisory_xact_lock(hashtextextended(:k, 0))")
        .param("k", "relation_denial:" + orgId + ":" + actorId + ":" + objectType)
        .query((rs, i) -> 0)
        .list();
    return jdbc.sql(
                """
                INSERT INTO relation_denial (id, org_id, actor_id, kind, object_type)
                SELECT :id, :orgId, :actorId, :kind, :objectType
                WHERE NOT EXISTS (SELECT 1 FROM relation_denial d
                                  WHERE d.org_id = :orgId AND d.actor_id = :actorId
                                    AND d.object_type = :objectType
                                    AND d.at > now() - make_interval(secs => :secs))
                """)
            .param("id", id)
            .param("orgId", orgId)
            .param("actorId", actorId)
            .param("kind", kind)
            .param("objectType", objectType)
            .param("secs", (double) window.toSeconds())
            .update()
        > 0;
  }

  static OffsetDateTime ts(Instant i) {
    return i == null ? null : i.atOffset(ZoneOffset.UTC);
  }

  static Instant instant(OffsetDateTime t) {
    return t == null ? null : t.toInstant();
  }
}
