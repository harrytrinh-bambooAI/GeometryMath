package vn.geo.server.content.internal.review.repo;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;

/** S2-E5: đề xuất của AI trong hộp duyệt (review_inbox_item, V160; BR-S2E5-20). Không xóa hàng. */
@Repository
public class ReviewInboxRepository {

  /**
   * Một đề xuất chèn bước mới sau bước {@code afterStep}. Nội dung đề xuất là lời giải: chỉ đọc qua
   * hàm cổng (SolutionRepository.content), không có ở đây.
   */
  public record ItemRow(
      UUID id,
      int afterStep,
      String status,
      Instant decidedAt,
      Integer resultVersion,
      Instant createdAt) {}

  private final TenantJdbc jdbc;

  public ReviewInboxRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  private static Instant instant(OffsetDateTime t) {
    return t == null ? null : t.toInstant();
  }

  /** Mọi đề xuất của sổ (chờ, đã nhận, đã bỏ), mới nhất trước. */
  public List<ItemRow> items(UUID docId) {
    return jdbc.sql(
            """
            SELECT id, after_step, status::text, decided_at, result_version, created_at
            FROM review_inbox_item WHERE org_id = :orgId AND doc_id = :doc
            ORDER BY created_at DESC, id DESC LIMIT 100
            """)
        .param("doc", docId)
        .query((rs, i) ->
            new ItemRow(
                rs.getObject(1, UUID.class),
                rs.getInt(2),
                rs.getString(3),
                instant(rs.getObject(4, OffsetDateTime.class)),
                (Integer) rs.getObject(5),
                instant(rs.getObject(6, OffsetDateTime.class))))
        .list();
  }

  public Optional<ItemRow> item(UUID docId, UUID id) {
    return items(docId).stream().filter(x -> x.id().equals(id)).findFirst();
  }

  /** Chốt một đề xuất đang chờ; 0 khi đã chốt trước đó. */
  public int decide(UUID id, String status, UUID by, Integer resultVersion) {
    return jdbc.sql(
            """
            UPDATE review_inbox_item
            SET status = CAST(:status AS review_item_status), decided_by = :by, decided_at = now(),
                result_version = :resultVersion
            WHERE org_id = :orgId AND id = :id AND status = 'pending'
            """)
        .param("id", id)
        .param("status", status)
        .param("by", by)
        .param("resultVersion", resultVersion)
        .update();
  }
}
