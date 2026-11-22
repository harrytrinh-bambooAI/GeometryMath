package vn.geo.server.assignment.internal.repo;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;

/** Trạng thái khóa lời giải của một bài (lớp hoặc tự học) theo cổng chung fn_solution_gate. */
@Repository
public class LockStateRepository {

  public record Gate(String status, Instant releaseAt, String kind) {}

  private final TenantJdbc jdbc;

  public LockStateRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  public Gate gate(UUID userId, UUID assignmentId) {
    return jdbc.sql(
            """
            SELECT g.status, a.release_at, CAST(a.kind AS text)
            FROM fn_solution_gate(:orgId, :userId, :id) g
            LEFT JOIN assignment a ON a.org_id = :orgId AND a.id = :id
            LIMIT 1
            """)
        .param("userId", userId)
        .param("id", assignmentId)
        .query((rs, i) -> {
          OffsetDateTime ts = rs.getObject(2, OffsetDateTime.class);
          return new Gate(rs.getString(1), ts == null ? null : ts.toInstant(), rs.getString(3));
        })
        .single();
  }
}
