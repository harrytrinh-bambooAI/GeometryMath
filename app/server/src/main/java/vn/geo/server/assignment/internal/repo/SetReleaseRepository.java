package vn.geo.server.assignment.internal.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;

/**
 * S3-E4: dữ liệu của Tổng quan giáo viên (C3-05), Theo dõi nhanh (C3-06) và khóa hàng bộ bài cho Mở
 * ngay / Đổi giờ. "Chưa mở bài" (BR-S3E4-06, QA-S3E4-01): học sinh Đang học chưa có tiến độ và chưa
 * có lượt thử nào trên mọi đề của bộ. Giờ mở của từng đề vẫn chỉ đổi qua fn_set_release.
 */
@Repository
public class SetReleaseRepository {

  public record LockedSet(UUID id, String title, String status, UUID classId, Instant releaseAt) {}

  public record OverviewSet(
      UUID id,
      String title,
      UUID classId,
      String className,
      Instant dueAt,
      Instant setReleaseAt,
      int lockedItems,
      Instant nextReleaseAt,
      int notOpened) {}

  public record ClassPending(UUID id, String name, int pending) {}

  public record FollowSet(
      UUID id,
      String title,
      String status,
      UUID classId,
      String className,
      Instant dueAt,
      Instant setReleaseAt,
      int lockedItems,
      Instant nextReleaseAt) {}

  public record StudentLine(String displayName, String userCode) {}

  /** Học sinh Đang học m chưa có tương tác nào trên mọi đề của bộ s. */
  private static final String NOT_OPENED =
      """
      NOT EXISTS (SELECT 1 FROM assignment_item i
                  WHERE i.org_id = :orgId AND i.set_id = s.id AND i.assignment_id IS NOT NULL
                    AND (EXISTS (SELECT 1 FROM assignment_progress ap WHERE ap.org_id = :orgId
                                   AND ap.assignment_id = i.assignment_id AND ap.user_id = m.user_id)
                      OR EXISTS (SELECT 1 FROM practice_attempt pa WHERE pa.org_id = :orgId
                                   AND pa.assignment_id = i.assignment_id AND pa.user_id = m.user_id)))
      """;

  private static final String LOCKED_ITEMS =
      """
      FROM assignment_item i JOIN assignment a ON a.org_id = :orgId AND a.id = i.assignment_id
      WHERE i.org_id = :orgId AND i.set_id = s.id AND (a.release_at IS NULL OR a.release_at > now())
      """;

  private final TenantJdbc jdbc;

  public SetReleaseRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  /** Khóa hàng bộ của chính giáo viên trong giao dịch Mở ngay / Đổi giờ (người khác: rỗng). */
  public Optional<LockedSet> lock(UUID teacherId, UUID setId) {
    return jdbc.sql(
            """
            SELECT s.id, s.title, s.status::text, s.class_id, s.release_at
            FROM assignment_set s
            WHERE s.org_id = :orgId AND s.id = :id AND s.owner_user_id = :teacherId
            FOR UPDATE
            """)
        .param("id", setId)
        .param("teacherId", teacherId)
        .query((rs, i) ->
            new LockedSet(
                rs.getObject(1, UUID.class),
                rs.getString(2),
                rs.getString(3),
                rs.getObject(4, UUID.class),
                instant(rs, 5)))
        .optional();
  }

  /** Người nhận thông báo: học sinh Đang học của lớp tại lúc gọi (BR-S3E4-21). */
  public List<UUID> activeStudents(UUID classId) {
    return jdbc.sql(
            """
            SELECT m.user_id FROM class_member m
            WHERE m.org_id = :orgId AND m.class_id = :classId AND m.status = 'active'
            ORDER BY m.user_id LIMIT 500
            -- sqllint:allow=ownership thành viên Đang học của lớp (iam) để chốt người nhận thông báo
            """)
        .param("classId", classId)
        .query(UUID.class)
        .list();
  }

  /** Bộ Đã giao chưa Đã đóng của các lớp đang dạy (BR-S3E4-01), mới nhất trước. */
  public List<OverviewSet> assignedSets(UUID teacherId) {
    return jdbc.sql(
            "SELECT s.id, s.title, c.id, c.name, s.due_at, s.release_at, (SELECT count(*) "
                + LOCKED_ITEMS
                + "), (SELECT min(a.release_at) "
                + LOCKED_ITEMS
                + "), (SELECT count(*) FROM class_member m WHERE m.org_id = :orgId AND m.class_id = s.class_id"
                + " AND m.status = 'active' AND "
                + NOT_OPENED
                + """
                )
                FROM assignment_set s
                JOIN class c ON c.org_id = :orgId AND c.id = s.class_id
                WHERE s.org_id = :orgId AND s.owner_user_id = :teacherId AND s.status = 'assigned'
                  AND c.teacher_id = :teacherId AND c.archived_at IS NULL
                ORDER BY s.id DESC LIMIT 100
                -- sqllint:allow=ownership,unbounded lớp + thành viên (iam), tiến độ + lượt thử (tracking) của các bộ đang giao của chính giáo viên; đếm trong một lớp
                """)
        .param("teacherId", teacherId)
        .query((rs, i) ->
            new OverviewSet(
                rs.getObject(1, UUID.class),
                rs.getString(2),
                rs.getObject(3, UUID.class),
                rs.getString(4),
                instant(rs, 5),
                instant(rs, 6),
                rs.getInt(7),
                instant(rs, 8),
                rs.getInt(9)))
        .list();
  }

  /** Lớp đang dạy kèm số em Chờ duyệt (chỉ số và tên lớp, BR-S3E4-02). */
  public List<ClassPending> classesWithPending(UUID teacherId) {
    return jdbc.sql(
            """
            SELECT c.id, c.name,
                   (SELECT count(*) FROM class_member m WHERE m.org_id = :orgId AND m.class_id = c.id
                      AND m.status = 'pending')
            FROM class c
            WHERE c.org_id = :orgId AND c.teacher_id = :teacherId AND c.archived_at IS NULL
            ORDER BY c.name, c.id LIMIT 100
            -- sqllint:allow=ownership,unbounded lớp đang dạy + số em Chờ duyệt (iam) cho Tổng quan
            """)
        .param("teacherId", teacherId)
        .query(
            (rs, i) -> new ClassPending(rs.getObject(1, UUID.class), rs.getString(2), rs.getInt(3)))
        .list();
  }

  /** Một bộ của chính giáo viên cho Theo dõi nhanh (người khác: rỗng → NOT_FOUND). */
  public Optional<FollowSet> followSet(UUID teacherId, UUID setId) {
    return jdbc.sql(
            "SELECT s.id, s.title, s.status::text, c.id, c.name, s.due_at, s.release_at, (SELECT count(*) "
                + LOCKED_ITEMS
                + "), (SELECT min(a.release_at) "
                + LOCKED_ITEMS
                + """
                )
                FROM assignment_set s
                JOIN class c ON c.org_id = :orgId AND c.id = s.class_id
                WHERE s.org_id = :orgId AND s.id = :id AND s.owner_user_id = :teacherId
                  AND c.teacher_id = :teacherId
                LIMIT 1
                -- sqllint:allow=ownership,unbounded tên lớp (iam) + giờ mở các đề của một bộ (tối đa 20 đề)
                """)
        .param("id", setId)
        .param("teacherId", teacherId)
        .query((rs, i) ->
            new FollowSet(
                rs.getObject(1, UUID.class),
                rs.getString(2),
                rs.getString(3),
                rs.getObject(4, UUID.class),
                rs.getString(5),
                instant(rs, 6),
                instant(rs, 7),
                rs.getInt(8),
                instant(rs, 9)))
        .optional();
  }

  /** Em Đang học chưa mở bài của bộ: chỉ tên hiển thị và mã người dùng (BR-S3E4-10). */
  public List<StudentLine> notOpened(UUID setId, UUID classId) {
    return jdbc.sql(
            """
            SELECT u.display_name, u.user_code
            FROM assignment_set s
            JOIN class_member m ON m.org_id = :orgId AND m.class_id = s.class_id AND m.status = 'active'
            JOIN app_user u ON u.org_id = :orgId AND u.id = m.user_id
            WHERE s.org_id = :orgId AND s.id = :id AND s.class_id = :classId AND
            -- sqllint:allow=ownership,unbounded thành viên + tên, mã người dùng (iam) của lớp mình dạy; LIMIT ở cuối câu
            """
                + NOT_OPENED
                + """
                ORDER BY u.display_name, u.id LIMIT 200
                -- sqllint:allow=ownership thành viên + tên, mã người dùng (iam), tiến độ + lượt thử (tracking) của lớp mình dạy
                """)
        .param("id", setId)
        .param("classId", classId)
        .query((rs, i) -> new StudentLine(rs.getString(1), rs.getString(2)))
        .list();
  }

  private static Instant instant(ResultSet rs, int col) throws SQLException {
    OffsetDateTime t = rs.getObject(col, OffsetDateTime.class);
    return t == null ? null : t.toInstant();
  }
}
