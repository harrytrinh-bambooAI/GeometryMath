package vn.geo.server.iam.internal.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;

/**
 * Đăng nhập (qua hàm SECURITY DEFINER của V10, D-GEO-127), lớp của giáo viên, móc đặt lại bộ đếm.
 */
@Repository
public class AuthRepository {

  public record StudentLogin(String status, UUID orgId, UUID classId, UUID userId) {}

  public record TeacherLogin(String status, UUID orgId, UUID userId) {}

  public record ClassRow(UUID id, String name, String level) {}

  private final JdbcClient jdbc;
  private final TenantJdbc tenant;

  public AuthRepository(JdbcClient jdbc, TenantJdbc tenant) {
    this.jdbc = jdbc;
    this.tenant = tenant;
  }

  /** Q01: tra mã lớp, tạo học sinh mới nếu tên chưa có trong lớp (D-GEO-106). */
  public StudentLogin studentLogin(
      String joinCode, String nameKey, String displayName, UUID newUserId) {
    return jdbc.sql(
            "SELECT status, org_id, class_id, user_id FROM fn_student_login(:code, :nameKey, :name, :newId)")
        .param("code", joinCode)
        .param("nameKey", nameKey)
        .param("name", displayName)
        .param("newId", newUserId)
        .query((rs, i) ->
            new StudentLogin(
                rs.getString(1),
                rs.getObject(2, UUID.class),
                rs.getObject(3, UUID.class),
                rs.getObject(4, UUID.class)))
        .single();
  }

  /** Q24: đăng nhập Giáo viên mẫu; CSDL chỉ thấy selector, HMAC của mã và HMAC của IP. */
  public TeacherLogin teacherLogin(String selector, byte[] verifier, byte[] ipKey) {
    return jdbc.sql(
            "SELECT status, org_id, user_id FROM fn_teacher_login(:selector, :verifier, :ip)")
        .param("selector", selector)
        .param("verifier", verifier)
        .param("ip", ipKey)
        .query((rs, i) ->
            new TeacherLogin(
                rs.getString(1), rs.getObject(2, UUID.class), rs.getObject(3, UUID.class)))
        .single();
  }

  /** Tên hiển thị đã lưu (trước khi có thẻ: trường lấy từ kết quả đăng nhập, không từ client). */
  public Optional<String> displayName(UUID orgId, UUID userId) {
    return jdbc.sql(
            "SELECT display_name FROM app_user WHERE org_id = :orgId AND id = :userId LIMIT 1")
        .param("orgId", orgId)
        .param("userId", userId)
        .query(String.class)
        .optional();
  }

  public Optional<String> className(UUID orgId, UUID classId) {
    return jdbc.sql("SELECT name FROM class WHERE org_id = :orgId AND id = :classId")
        .param("orgId", orgId)
        .param("classId", classId)
        .query(String.class)
        .optional();
  }

  /** Q17: lớp của giáo viên đang đăng nhập (chỉ lớp mình dạy, BR-GEO-20). */
  public List<ClassRow> teacherClasses(UUID teacherId) {
    return tenant
        .sql(
            """
            SELECT id, name, level::text FROM class
            WHERE org_id = :orgId AND teacher_id = :teacherId AND archived_at IS NULL
            ORDER BY name, id LIMIT 100
            """)
        .param("teacherId", teacherId)
        .query(
            (rs, i) -> new ClassRow(rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3)))
        .list();
  }

  /** Móc kiểm thử: học sinh rời lớp (left_at), dọn học sinh do ca kiểm tạo. */
  public int leaveClass(String joinCode, String nameKey) {
    return jdbc.sql(
            """
            UPDATE class_member SET left_at = now()
            WHERE class_id = (SELECT id FROM class WHERE join_code = :code) AND name_key = :nameKey AND left_at IS NULL
            -- sqllint:allow=tenant móc kiểm thử dev/test: tra lớp theo mã lớp toàn cục (D-GEO-051)
            """)
        .param("code", joinCode)
        .param("nameKey", nameKey)
        .update();
  }

  /**
   * Móc kiểm thử PO-51: đặt lại bộ đếm thử sai mã giáo viên THEO PHẠM VI (các khóa IP và tài khoản
   * ca kiểm dùng), không xóa toàn bảng để các ca chạy song song không giẫm nhau.
   */
  public int resetAuthFailures(List<byte[]> ipKeys, List<UUID> userIds) {
    int n = 0;
    if (!ipKeys.isEmpty()) {
      n +=
          jdbc.sql("DELETE FROM auth_failure WHERE ip_key = ANY(:ipKeys)")
              .param("ipKeys", ipKeys.toArray(new byte[0][]))
              .update();
    }
    if (!userIds.isEmpty()) {
      n +=
          jdbc.sql("DELETE FROM auth_failure WHERE user_id = ANY(:userIds)")
              .param("userIds", userIds.toArray(new UUID[0]))
              .update();
    }
    return n;
  }
}
