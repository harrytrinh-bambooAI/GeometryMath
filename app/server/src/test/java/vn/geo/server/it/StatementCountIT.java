package vn.geo.server.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.testing.SqlStatementCounter;

/**
 * Ví dụ đếm câu lệnh (NFR-GEO-009, D-GEO-170): cùng một API, trang 1 dòng và trang đầy phải chạy
 * cùng số câu lệnh, và không vượt trần đọc 4 câu. Mẫu để các module thật chép theo.
 */
class StatementCountIT extends AbstractIT {

  private static final int MAX_READ_STATEMENTS = 4;

  @Autowired JdbcClient jdbc;
  @Autowired SqlStatementCounter counter;

  @Test
  void listQueryCountDoesNotDependOnRowCount() throws Exception {
    UUID org = Uuid7.next();
    jdbc.sql(
            "INSERT INTO sample_item (id, org_id, title) "
                + "SELECT gen_random_uuid(), :org, 't' || g FROM generate_series(1, 120) g")
        .param("org", org)
        .update();
    String token = tokenFor(org);

    String small = "count-small-" + Uuid7.next();
    String full = "count-full-" + Uuid7.next();
    assertThat(get("/api/v1/sample/items?limit=1", token, small).statusCode()).isEqualTo(200);
    assertThat(get("/api/v1/sample/items?limit=100", token, full).statusCode()).isEqualTo(200);

    assertThat(counter.count(small)).as("câu lệnh: %s", counter.statements(small)).isPositive();
    assertThat(counter.count(full))
        .as("trang đầy phải chạy cùng số câu với trang 1 dòng: %s", counter.statements(full))
        .isEqualTo(counter.count(small));
    assertThat(counter.count(full)).isLessThanOrEqualTo(MAX_READ_STATEMENTS);
  }
}
