package vn.geo.server.it.s3e5;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.iam.internal.service.RelationDenials;
import vn.geo.server.it.e1.AbstractE1IT;

/** PO-76 / BR-S3E5-07: relation_denial tối đa 1 hàng mỗi giờ cho (người gọi, loại đối tượng). */
class RelationDenialRateIT extends AbstractE1IT {

  @Autowired RelationDenials denials;

  private long rows(UUID actor) {
    return jdbc.sql("SELECT count(*) FROM relation_denial WHERE actor_id = :a")
        .param("a", actor)
        .query(Long.class)
        .single();
  }

  @Test
  @DisplayName(
      "PO-76 Hai lượt từ chối cùng giờ, cùng loại đối tượng: 1 hàng; khác loại đối tượng: 2 hàng")
  void oneRowPerActorAndObjectTypePerHour() {
    AuthPrincipal p = new AuthPrincipal(ORG_A, UUID.randomUUID(), Role.TEACHER);
    denials.notFound(p, "class");
    denials.forbidden(p, "class");
    assertThat(rows(p.userId())).as("cùng (người gọi, loại đối tượng), khác kind").isEqualTo(1);
    denials.notFound(p, "student");
    assertThat(rows(p.userId())).as("khác loại đối tượng").isEqualTo(2);
    denials.notFound(p, "student");
    assertThat(rows(p.userId())).isEqualTo(2);
  }

  @Test
  @DisplayName("PO-76 Hai luồng song song từ chối cùng (người gọi, loại đối tượng): 1 hàng")
  void parallelDenialsWriteOneRow() throws Exception {
    AuthPrincipal p = new AuthPrincipal(ORG_A, UUID.randomUUID(), Role.TEACHER);
    var pool = java.util.concurrent.Executors.newFixedThreadPool(2);
    var go = new java.util.concurrent.CountDownLatch(1);
    try {
      var f1 =
          pool.submit(() -> {
            go.await();
            return denials.notFound(p, "class");
          });
      var f2 =
          pool.submit(() -> {
            go.await();
            return denials.forbidden(p, "class");
          });
      go.countDown();
      f1.get();
      f2.get();
    } finally {
      pool.shutdown();
    }
    assertThat(rows(p.userId())).isEqualTo(1);
  }
}
