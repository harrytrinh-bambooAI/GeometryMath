package vn.geo.server.assignment.internal.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * US-S3E6-03 — dữ liệu của tác vụ nhắc việc chạy nền (không có ngữ cảnh trường: duyệt mọi trường,
 * mỗi câu lọc theo org_id của bộ). Chỉ bộ Đã giao (BR-S3E6-25: không nhắc bộ Đã đóng / Lưu trữ),
 * chỉ học sinh Đang học.
 */
@Repository
public class ReminderRepository {

  public record DueSet(UUID id, UUID orgId, UUID classId, String title, Instant dueAt) {}

  public record ReleasedSet(UUID id, UUID orgId, UUID classId, String title, Instant releaseAt) {}

  private final JdbcClient raw;

  public ReminderRepository(JdbcClient raw) {
    this.raw = raw;
  }

  /**
   * Bộ Đã giao có hạn trong (now, now + 24 giờ] và được giao trước mốc 24 giờ trước hạn
   * (BR-S3E6-20, BR-S3E6-22). Giờ yên lặng do dịch vụ xét.
   */
  public List<DueSet> dueWithinDay(Instant now) {
    return raw.sql(
            """
            SELECT s.id, s.org_id, s.class_id, s.title, s.due_at FROM assignment_set s
            WHERE s.status = 'assigned' AND s.due_at IS NOT NULL AND s.class_id IS NOT NULL
              AND s.due_at > CAST(:now AS timestamptz)
              AND s.due_at <= CAST(:now AS timestamptz) + interval '24 hours'
              AND s.assigned_at <= s.due_at - interval '24 hours'
            ORDER BY s.due_at, s.id LIMIT 1000
            -- sqllint:allow=tenant tác vụ nền nhắc việc cho mọi trường (không có ngữ cảnh trường)
            """)
        .param("now", at(now))
        .query((rs, i) ->
            new DueSet(
                rs.getObject(1, UUID.class),
                rs.getObject(2, UUID.class),
                rs.getObject(3, UUID.class),
                rs.getString(4),
                instant(rs, 5)))
        .list();
  }

  /**
   * Bộ Đã giao có giờ mở lời giải theo lịch rơi trong (now - 1 ngày, now], đặt sau lúc giao
   * (BR-S3E6-24). Cửa sổ 1 ngày cho phép bù khi tác vụ lỡ vài lượt mà không gửi lại bộ cũ.
   */
  public List<ReleasedSet> releasedWithinDay(Instant now) {
    return raw.sql(
            """
            SELECT s.id, s.org_id, s.class_id, s.title, s.release_at FROM assignment_set s
            WHERE s.status = 'assigned' AND s.release_at IS NOT NULL AND s.class_id IS NOT NULL
              AND s.release_at <= CAST(:now AS timestamptz)
              AND s.release_at > CAST(:now AS timestamptz) - interval '1 day'
              AND (s.assigned_at IS NULL OR s.release_at > s.assigned_at)
            ORDER BY s.release_at, s.id LIMIT 1000
            -- sqllint:allow=tenant tác vụ nền nhắc việc cho mọi trường (không có ngữ cảnh trường)
            """)
        .param("now", at(now))
        .query((rs, i) ->
            new ReleasedSet(
                rs.getObject(1, UUID.class),
                rs.getObject(2, UUID.class),
                rs.getObject(3, UUID.class),
                rs.getString(4),
                instant(rs, 5)))
        .list();
  }

  /** Học sinh Đang học của lớp (BR-S3E6-25: không Chờ duyệt, Đã rời, Bị từ chối). */
  public List<UUID> activeStudents(UUID orgId, UUID classId) {
    return raw.sql(
            """
            SELECT m.user_id FROM class_member m
            WHERE m.org_id = :orgId AND m.class_id = :classId AND m.status = 'active' AND m.left_at IS NULL
            ORDER BY m.user_id LIMIT 500
            -- sqllint:allow=ownership thành viên Đang học của lớp (iam) để chốt người nhận nhắc việc
            """)
        .param("orgId", orgId)
        .param("classId", classId)
        .query(UUID.class)
        .list();
  }

  /**
   * Học sinh Đang học còn ít nhất một đề Chưa làm hoặc Đang làm trong bộ (BR-S3E6-20). "Xong" một
   * đề như màn học sinh S3-E2: có lượt Đúng, hoặc đã dùng hết số lượt Sai.
   */
  public List<UUID> unfinishedStudents(UUID orgId, UUID classId, UUID setId) {
    return raw.sql(
            """
            SELECT m.user_id FROM class_member m
            WHERE m.org_id = :orgId AND m.class_id = :classId AND m.status = 'active' AND m.left_at IS NULL
              AND EXISTS (
                SELECT 1 FROM assignment_item i WHERE i.org_id = :orgId AND i.set_id = :setId
                  AND NOT (i.assignment_id IS NOT NULL AND (
                    EXISTS (SELECT 1 FROM practice_attempt a WHERE a.org_id = :orgId
                              AND a.assignment_id = i.assignment_id AND a.user_id = m.user_id
                              AND a.outcome = 'correct')
                    OR (SELECT count(*) FROM practice_attempt a WHERE a.org_id = :orgId
                          AND a.assignment_id = i.assignment_id AND a.user_id = m.user_id
                          AND a.outcome = 'wrong') >= i.max_attempts)))
            ORDER BY m.user_id LIMIT 500
            -- sqllint:allow=ownership thành viên Đang học (iam) + lượt thử của em (tracking) để chọn em chưa xong
            """)
        .param("orgId", orgId)
        .param("classId", classId)
        .param("setId", setId)
        .query(UUID.class)
        .list();
  }

  private static OffsetDateTime at(Instant t) {
    return OffsetDateTime.ofInstant(t, ZoneOffset.UTC);
  }

  private static Instant instant(ResultSet rs, int col) throws SQLException {
    OffsetDateTime t = rs.getObject(col, OffsetDateTime.class);
    return t == null ? null : t.toInstant();
  }
}
