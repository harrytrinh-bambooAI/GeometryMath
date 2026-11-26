package vn.geo.server.tracking.internal.classboard.repo;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;

/** US-S3E6-02: nhật ký xuất kết quả lớp để giới hạn 10 lần / giờ (V416). */
@Repository
public class ClassExportRepository {

  private final TenantJdbc jdbc;

  public ClassExportRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  /** Giờ các lần xuất của giáo viên từ {@code since}, cũ nhất trước (tối đa 50). */
  public List<Instant> exportsSince(UUID teacherId, Instant since) {
    return jdbc.sql(
            """
            SELECT created_at FROM class_export_log
            WHERE org_id = :orgId AND teacher_id = :teacherId AND created_at > :since
            ORDER BY created_at LIMIT 50
            """)
        .param("teacherId", teacherId)
        .param("since", since.atOffset(ZoneOffset.UTC))
        .query((rs, i) -> rs.getObject(1, OffsetDateTime.class).toInstant())
        .list();
  }

  /** Móc kiểm thử (dev/test): giả lập các lần xuất đã xảy ra sớm hơn {@code seconds} giây. */
  public int age(UUID teacherId, long seconds) {
    return jdbc.sql(
            """
            UPDATE class_export_log SET created_at = created_at - make_interval(secs => :s)
            WHERE org_id = :orgId AND teacher_id = :teacherId
            """)
        .param("s", seconds)
        .param("teacherId", teacherId)
        .update();
  }

  public void record(UUID id, UUID teacherId, UUID classId, Instant at) {
    jdbc.sql(
            """
            INSERT INTO class_export_log (id, org_id, teacher_id, class_id, created_at)
            VALUES (:id, :orgId, :teacherId, :classId, :at)
            """)
        .param("id", id)
        .param("teacherId", teacherId)
        .param("classId", classId)
        .param("at", at.atOffset(ZoneOffset.UTC))
        .update();
  }
}
