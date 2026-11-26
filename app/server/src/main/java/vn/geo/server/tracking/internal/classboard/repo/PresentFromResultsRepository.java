package vn.geo.server.tracking.internal.classboard.repo;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;

/**
 * US-S3E6-01: dữ liệu cho Trình chiếu mở từ Kết quả lớp (đề, lời giải của phiên bản hiện hành, giờ
 * mở lời giải của đề trong bộ) và sự kiện đo M-S3-04 (V416).
 */
@Repository
public class PresentFromResultsRepository {

  /** Phiên bản hiện hành của đề (có thể mới hơn phiên bản đã ghim vào bộ) và lời giải của nó. */
  public record Source(
      UUID versionId, String statement, String stepsJson, boolean newerThanPinned) {}

  private final TenantJdbc jdbc;

  /** Chỉ cho móc kiểm thử: gọi không kèm phiên nên không có ngữ cảnh trường. */
  private final JdbcClient raw;

  public PresentFromResultsRepository(TenantJdbc jdbc, JdbcClient raw) {
    this.jdbc = jdbc;
    this.raw = raw;
  }

  public Optional<Source> source(UUID teacherId, UUID contentOrgId, UUID pinnedVersionId) {
    Optional<Object[]> cur =
        jdbc.sql(
                """
                SELECT cur.id, cur.statement, cur.id <> pv.id
                FROM problem_version pv
                JOIN problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id
                JOIN problem_version cur ON cur.org_id = p.org_id AND cur.id = coalesce(p.current_version_id, pv.id)
                WHERE pv.org_id = :contentOrg AND pv.id = :pv
                LIMIT 1
                -- sqllint:allow=ownership,tenant đề kho (content) của bộ đã kiểm chủ lớp, chỉ đọc
                """)
            .param("contentOrg", contentOrgId)
            .param("pv", pinnedVersionId)
            .query((rs, i) ->
                new Object[] {rs.getObject(1, UUID.class), rs.getString(2), rs.getBoolean(3)})
            .optional();
    if (cur.isEmpty()) {
      return Optional.empty();
    }
    UUID version = (UUID) cur.get()[0];
    // lời giải chỉ qua hàm cổng của CSDL (bảng solution cấm đọc thẳng, D-GEO-153)
    String steps =
        jdbc.sql(
                """
                SELECT steps::text FROM fn_get_solution_teacher(:orgId, :teacherId, :versionId)
                LIMIT 1
                """)
            .param("teacherId", teacherId)
            .param("versionId", version)
            .query(String.class)
            .optional()
            .orElse(null);
    return Optional.of(new Source(version, (String) cur.get()[1], steps, (Boolean) cur.get()[2]));
  }

  /** Giờ mở lời giải của đề ở vị trí {@code position} trong bộ (giờ riêng của đề, rồi của bộ). */
  public Optional<Instant> releaseAt(UUID setId, int position) {
    return jdbc.sql(
            """
            SELECT coalesce(i.release_at, s.release_at)
            FROM assignment_item i JOIN assignment_set s ON s.org_id = i.org_id AND s.id = i.set_id
            WHERE i.org_id = :orgId AND s.org_id = :orgId AND i.set_id = :setId AND i.position = :pos
            LIMIT 1
            -- sqllint:allow=ownership bộ bài (assignment) của lớp đã kiểm chủ, chỉ đọc
            """)
        .param("setId", setId)
        .param("pos", position)
        .query((rs, i) -> {
          OffsetDateTime t = rs.getObject(1, OffsetDateTime.class);
          return Optional.ofNullable(t == null ? null : t.toInstant());
        })
        .optional()
        .flatMap(o -> o);
  }

  /**
   * Ghi một sự kiện đo nếu trong {@code mergeWindow} giây chưa có sự kiện cùng giáo viên và đề; trả
   * mã sự kiện (mới hoặc sự kiện vừa có) — mã này là mã phiên Trình chiếu.
   */
  public UUID recordOnce(UUID id, UUID teacherId, UUID versionId, Instant at, long mergeSeconds) {
    jdbc.sql("SELECT 1 FROM (SELECT pg_advisory_xact_lock(hashtext(:k))) l")
        .param("k", "present:" + teacherId + ":" + versionId)
        .query(Integer.class)
        .single();
    Optional<UUID> recent =
        jdbc.sql(
                """
                SELECT id FROM present_from_results_event
                WHERE org_id = :orgId AND teacher_id = :teacherId AND problem_version_id = :pv
                  AND created_at > :since
                ORDER BY created_at DESC LIMIT 1
                """)
            .param("teacherId", teacherId)
            .param("pv", versionId)
            .param("since", at.minusSeconds(mergeSeconds).atOffset(ZoneOffset.UTC))
            .query(UUID.class)
            .optional();
    if (recent.isPresent()) {
      return recent.get();
    }
    jdbc.sql(
            """
            INSERT INTO present_from_results_event (id, org_id, teacher_id, problem_version_id, created_at)
            VALUES (:id, :orgId, :teacherId, :pv, :at)
            """)
        .param("id", id)
        .param("teacherId", teacherId)
        .param("pv", versionId)
        .param("at", at.atOffset(ZoneOffset.UTC))
        .update();
    return id;
  }

  /** Móc kiểm thử (dev/test): đặt giờ mở lời giải của bộ (null = Chưa đặt giờ). */
  public int setReleaseForTest(UUID setId, Instant at) {
    // giờ mở thật của học sinh nằm ở hàng assignment (fn_solution_gate): câu theo giờ của bộ đi
    // theo giờ mới
    raw.sql(
            """
            UPDATE assignment a SET release_at = CAST(:at AS timestamptz),
              -- ràng buộc release_at >= created_at: lùi created_at theo (như SandboxRepository)
              created_at = LEAST(a.created_at, COALESCE(CAST(:at AS timestamptz), a.created_at))
            FROM assignment_item i
            WHERE i.set_id = :setId AND i.assignment_id = a.id AND i.release_at IS NULL
            -- sqllint:allow=tenant,ownership móc kiểm thử S3-E6 dev/test, gọi không kèm phiên
            """)
        .param("at", at == null ? null : at.atOffset(ZoneOffset.UTC))
        .param("setId", setId)
        .update();
    return raw.sql(
            """
            UPDATE assignment_set SET release_at = :at
            WHERE id = :setId
            -- sqllint:allow=tenant,ownership móc kiểm thử S3-E6 dev/test, gọi không kèm phiên
            """)
        .param("at", at == null ? null : at.atOffset(ZoneOffset.UTC))
        .param("setId", setId)
        .update();
  }
}
