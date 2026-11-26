package vn.geo.server.iam.internal.repo;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Lệnh dữ liệu của móc kiểm thử S3-E1/S3-E5 (chỉ gọi từ controller profile dev/test). */
@Repository
@Profile({"dev", "test"})
public class ClassTestHookRepository {

  private final JdbcClient jdbc;

  public ClassTestHookRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public int ageJoinAttempts(UUID userId, long seconds) {
    return jdbc.sql(
            """
            UPDATE class_join_attempt SET attempted_at = attempted_at - make_interval(secs => :s)
            WHERE user_id = :userId
            -- sqllint:allow=tenant móc kiểm thử dev/test: giả lập thời gian trôi cho bộ đếm của một tài khoản
            """)
        .param("s", seconds)
        .param("userId", userId)
        .update();
  }

  public void seedJoinAttempt(UUID orgId, UUID id, UUID userId, Instant at) {
    jdbc.sql(
            "INSERT INTO class_join_attempt (id, org_id, user_id, attempted_at) VALUES (:id, :orgId, :userId, :at)")
        .param("id", id)
        .param("orgId", orgId)
        .param("userId", userId)
        .param("at", at.atOffset(ZoneOffset.UTC))
        .update();
  }

  public int ageTempPassword(UUID userId, long seconds) {
    return jdbc.sql(
            """
            UPDATE app_user SET temp_password_expires_at = temp_password_expires_at - make_interval(secs => :s)
            WHERE id = :userId AND temp_password_expires_at IS NOT NULL
            -- sqllint:allow=tenant móc kiểm thử dev/test: giả lập mật khẩu tạm sinh sớm hơn
            """)
        .param("s", seconds)
        .param("userId", userId)
        .update();
  }

  public int ageResets(UUID teacherId, long seconds) {
    return jdbc.sql(
            """
            UPDATE student_password_reset SET created_at = created_at - make_interval(secs => :s)
            WHERE teacher_id = :teacherId
            -- sqllint:allow=tenant móc kiểm thử dev/test: giả lập thời gian trôi cho giới hạn đặt lại
            """)
        .param("s", seconds)
        .param("teacherId", teacherId)
        .update();
  }

  public int deactivate(UUID userId, Instant at) {
    return jdbc.sql(
            """
            UPDATE app_user SET deactivated_at = :at WHERE id = :userId
            -- sqllint:allow=tenant móc kiểm thử dev/test: giả lập tài khoản đã xóa (US-S3E1-03-FC5)
            """)
        .param("at", at.atOffset(ZoneOffset.UTC))
        .param("userId", userId)
        .update();
  }

  public long denials(UUID actorId) {
    return jdbc.sql(
            """
            SELECT count(*) FROM relation_denial WHERE actor_id = :actorId
            -- sqllint:allow=tenant móc kiểm thử dev/test: đếm sự kiện bảo mật của một người gọi (M-S3-06)
            """)
        .param("actorId", actorId)
        .query(Long.class)
        .single();
  }

  public UUID orgOf(UUID userId) {
    return jdbc.sql(
            """
            SELECT org_id FROM app_user WHERE id = :id LIMIT 1
            -- sqllint:allow=tenant móc kiểm thử dev/test: trường của một tài khoản
            """)
        .param("id", userId)
        .query(UUID.class)
        .single();
  }
}
