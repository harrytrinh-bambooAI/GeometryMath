package vn.geo.server.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import tools.jackson.databind.JsonNode;
import vn.geo.server.common.id.Uuid7;

class KeysetPagingIT extends AbstractIT {

  @Autowired JdbcClient jdbc;

  private void insert(UUID org, int n, int distinctTimestamps) {
    Instant base = Instant.parse("2026-02-01T00:00:00Z");
    for (int i = 0; i < n; i++) {
      jdbc.sql(
              "INSERT INTO sample_item (id, org_id, title, created_at) VALUES (:id, :org, :t, :at)")
          .param("id", Uuid7.next())
          .param("org", org)
          .param("t", "mục " + i)
          .param("at", Timestamp.from(base.plusSeconds(i % distinctTimestamps)))
          .update();
    }
  }

  @Test
  void walksAllRowsOncePerTenantEvenWithManyEqualTimestamps() throws Exception {
    UUID orgA = Uuid7.next();
    UUID orgB = Uuid7.next();
    insert(orgA, 250, 5); // 50 dòng trùng mỗi mốc thời gian
    insert(orgB, 30, 30);
    String token = tokenFor(orgA);

    Set<String> ids = new HashSet<>();
    List<Instant> times = new ArrayList<>();
    String cursor = null;
    int pages = 0;
    do {
      var r =
          get("/api/v1/sample/items?limit=40" + (cursor == null ? "" : "&cursor=" + cursor), token);
      assertThat(r.statusCode()).isEqualTo(200);
      JsonNode body = json(r);
      for (JsonNode it : body.get("items")) {
        assertThat(ids.add(it.get("id").asString())).as("không lặp").isTrue();
        times.add(Instant.parse(it.get("createdAt").asString()));
      }
      cursor = body.hasNonNull("nextCursor") ? body.get("nextCursor").asString() : null;
      pages++;
    } while (cursor != null);

    assertThat(pages).isEqualTo(7);
    assertThat(ids).hasSize(250);
    assertThat(times).isSortedAccordingTo(java.util.Comparator.reverseOrder());
  }

  @Test
  void tenantIsTakenFromTokenNotFromClientInput() throws Exception {
    UUID orgA = Uuid7.next();
    UUID orgB = Uuid7.next();
    insert(orgA, 3, 3);
    insert(orgB, 7, 7);
    var r = get("/api/v1/sample/items?orgId=" + orgA + "&org_id=" + orgA, tokenFor(orgB));
    assertThat(json(r).get("items")).hasSize(7);
  }

  @Test
  void cursorFromAnotherTenantIsRejected() throws Exception {
    UUID orgA = Uuid7.next();
    insert(orgA, 5, 5);
    String cursor =
        json(get("/api/v1/sample/items?limit=2", tokenFor(orgA))).get("nextCursor").asString();
    var r = get("/api/v1/sample/items?cursor=" + cursor, tokenFor(Uuid7.next()));
    assertThat(r.statusCode()).isEqualTo(400);
    assertThat(json(r).get("code").asString()).isEqualTo("INVALID_CURSOR");
    assertThat(json(r).get("message").asString()).contains("phân trang");
  }

  @Test
  void garbageCursorIs400AndLimitIsCapped() throws Exception {
    UUID org = Uuid7.next();
    insert(org, 130, 130);
    String t = tokenFor(org);
    assertThat(get("/api/v1/sample/items?cursor=rac-ruoi", t).statusCode()).isEqualTo(400);
    assertThat(get("/api/v1/sample/items?limit=0", t).statusCode()).isEqualTo(400);
    assertThat(get("/api/v1/sample/items?limit=abc", t).statusCode()).isEqualTo(400);
    var big = json(get("/api/v1/sample/items?limit=100000", t));
    assertThat(big.get("items")).hasSize(100);
    assertThat(big.hasNonNull("nextCursor")).isTrue();
  }

  @Test
  void queryPlanUsesCompositeIndexWithoutSortOrSeqScan() {
    UUID org = Uuid7.next();
    jdbc.sql(
            "INSERT INTO sample_item (id, org_id, title, created_at) "
                + "SELECT gen_random_uuid(), :org, 't' || g, now() - (g || ' seconds')::interval FROM generate_series(1, 60000) g")
        .param("org", org)
        .update();
    // dòng của trường khác để chỉ mục phải thật sự lọc
    jdbc.sql(
            "INSERT INTO sample_item (id, org_id, title) SELECT gen_random_uuid(), gen_random_uuid(), 'x' FROM generate_series(1, 20000)")
        .update();
    jdbc.sql("ANALYZE sample_item").update();

    String first =
        "EXPLAIN SELECT id, title, created_at FROM sample_item WHERE org_id = :org ORDER BY created_at DESC, id DESC LIMIT 21";
    String next =
        "EXPLAIN SELECT id, title, created_at FROM sample_item WHERE org_id = :org "
            + "AND (created_at, id) < (now() - interval '1000 seconds', 'ffffffff-ffff-ffff-ffff-ffffffffffff'::uuid) "
            + "ORDER BY created_at DESC, id DESC LIMIT 21";
    for (String sql : List.of(first, next)) {
      String plan = String.join("\n", jdbc.sql(sql).param("org", org).query(String.class).list());
      assertThat(plan)
          .contains("sample_item_org_created_idx")
          .doesNotContain("Seq Scan")
          .doesNotContain("Sort");
    }
  }
}
