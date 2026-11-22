package vn.geo.server.sample.repo;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;
import vn.geo.server.common.paging.KeysetCursor;
import vn.geo.server.common.paging.PageRequest;
import vn.geo.server.sample.SampleItem;

@Repository
public class SampleItemRepository {

  private final TenantJdbc db;

  public SampleItemRepository(TenantJdbc db) {
    this.db = db;
  }

  /** Lấy limit+1 dòng, mới nhất trước, thứ tự ổn định (created_at, id); đi thẳng theo chỉ mục. */
  public List<SampleItem> findPage(PageRequest page) {
    if (page.isFirstPage()) {
      return db.sql(
              """
              SELECT id, title, created_at
              FROM sample_item
              WHERE org_id = :orgId
              ORDER BY created_at DESC, id DESC
              LIMIT :fetch
              """)
          .param("fetch", page.fetchSize())
          .query((rs, n) -> map(rs))
          .list();
    }
    KeysetCursor after = page.after();
    return db.sql(
            """
            SELECT id, title, created_at
            FROM sample_item
            WHERE org_id = :orgId
              AND (created_at, id) < (:afterTs, :afterId)
            ORDER BY created_at DESC, id DESC
            LIMIT :fetch
            """)
        .param("afterTs", after.instant(0).atOffset(ZoneOffset.UTC))
        .param("afterId", after.id())
        .param("fetch", page.fetchSize())
        .query((rs, n) -> map(rs))
        .list();
  }

  private static SampleItem map(java.sql.ResultSet rs) throws java.sql.SQLException {
    return new SampleItem(
        rs.getObject("id", java.util.UUID.class),
        rs.getString("title"),
        rs.getObject("created_at", OffsetDateTime.class).toInstant());
  }
}
