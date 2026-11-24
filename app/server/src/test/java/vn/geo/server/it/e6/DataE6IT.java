package vn.geo.server.it.e6;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import vn.geo.server.assignment.internal.repo.ProbeRepository;
import vn.geo.server.assignment.internal.service.ProbeService;
import vn.geo.server.it.e1.AbstractE1IT;

/**
 * IT của E6: dữ liệu mẫu (US-GEO-601) và bộ câu thử moi đáp án (US-GEO-611). Dữ liệu vàng là
 * app/db/seed/e2e/*.sql; ca thay đổi bộ câu thử chạy trong giao dịch ROLLBACK để không ảnh hưởng
 * lớp IT khác.
 */
class DataE6IT extends AbstractE1IT {

  @Autowired ProbeService probes;
  @Autowired TransactionTemplate tx;

  private List<String> names(String classCode) {
    return jdbc.sql(
            """
            SELECT u.display_name FROM class c JOIN class_member m ON m.org_id = c.org_id AND m.class_id = c.id
            JOIN app_user u ON u.org_id = m.org_id AND u.id = m.user_id
            WHERE c.join_code = :code AND m.left_at IS NULL ORDER BY u.display_name
            """)
        .param("code", classCode)
        .query(String.class)
        .list();
  }

  @Test
  @DisplayName("TC-GEO-0600 Trường, giáo viên, lớp, học sinh mẫu")
  void tc0600() throws Exception {
    assertThat(count("SELECT count(*) FROM org WHERE kind = 'school' AND name = 'THPT Mẫu A'"))
        .isEqualTo(1);
    var login = postJson("/api/v1/auth/teacher", null, "{\"code\":\"GQ7KX2MTB4HN\"}");
    assertThat(login.statusCode()).isEqualTo(200);
    assertThat(json(login).get("displayName").asString()).isEqualTo("Giáo viên mẫu");
    assertThat(
            count(
                "SELECT count(*) FROM problem WHERE source = 'catalog' AND author_user_id <> '"
                    + AUTHOR
                    + "'"))
        .isZero();
    assertThat(names("HK7M2A")).containsExactlyInAnyOrder("An", "Bình", "Chi", "Dũng", "Hà");
    assertThat(names("HK9B4T")).containsExactlyInAnyOrder("Khánh", "Linh", "Minh", "Ngân", "Phúc");
  }

  @Test
  @DisplayName("TC-GEO-0601 Bài giao mẫu đủ ba trạng thái")
  void tc0601() throws Exception {
    JsonNode items = json(get("/api/v1/student/assignments?limit=50", an())).get("items");
    java.util.Map<String, JsonNode> byCode = new java.util.HashMap<>();
    items.forEach(i -> byCode.put(i.get("problemCode").asString(), i));
    assertThat(byCode.get("HKG-01").get("releaseState").asString()).isEqualTo("upcoming");
    assertThat(Instant.parse(byCode.get("HKG-01").get("releaseAt").asString()))
        .isEqualTo(t0.plusSeconds(3600));
    assertThat(byCode.get("HKG-02").get("releaseState").asString()).isEqualTo("unset");
    assertThat(byCode.get("HKG-03").get("releaseState").asString()).isEqualTo("open");
  }

  @Test
  @DisplayName("TC-GEO-0602 Tiến độ mẫu của lớp 11A1 (F-GEO-050…054)")
  void tc0602() throws Exception {
    JsonNode m = json(get("/api/v1/teacher/assignments/" + BG1 + "/monitor", teacherA()));
    java.util.Map<String, JsonNode> s = new java.util.HashMap<>();
    m.get("students").forEach(x -> s.put(x.get("displayName").asString(), x));
    assertThat(s).hasSize(5);
    assertThat(s.get("An").get("hintLevel").asInt()).isEqualTo(2);
    assertThat(s.get("An").get("askCount").asInt()).isEqualTo(3);
    assertThat(s.get("An").get("activeMinutes").asInt()).isEqualTo(18);
    assertThat(s.get("Hà").get("opened").asBoolean()).isFalse();
  }

  private long[] counts() {
    return new long[] {
      count("SELECT count(*) FROM org WHERE name = 'THPT Mẫu A'"),
      count(
          "SELECT count(*) FROM app_user WHERE org_id = '"
              + ORG_A
              + "' AND role = 'teacher' AND account_kind = 'sample'"),
      count("SELECT count(*) FROM class WHERE org_id = '" + ORG_A + "'"),
      count("SELECT count(*) FROM class_member WHERE org_id = '" + ORG_A + "'"),
      count("SELECT count(*) FROM assignment WHERE org_id = '" + ORG_A + "' AND kind = 'class'")
    };
  }

  @Test
  @DisplayName("TC-GEO-0603 Nạp lại không nhân đôi")
  void tc0603() throws Exception {
    reseed();
    reseed();
    assertThat(counts()).containsExactly(1, 1, 2, 10, 5);
  }

  @Test
  @DisplayName("TC-GEO-0604 Không lẫn dữ liệu thử tải; trình diễn chỉ có THPT Mẫu A")
  void tc0604() throws Exception {
    String prune = Files.readString(Path.of("../db/seed/demo/990_demo_prune.sql"));
    try (Connection c = dataSource.getConnection()) {
      c.setAutoCommit(false);
      try (Statement st = c.createStatement()) {
        st.execute(prune);
        var rs =
            st.executeQuery("SELECT name FROM org WHERE kind IN ('school', 'group') ORDER BY name");
        List<String> orgs = new ArrayList<>();
        while (rs.next()) {
          orgs.add(rs.getString(1));
        }
        assertThat(orgs).containsExactly("THPT Mẫu A");
        assertThat(orgs).noneMatch(n -> n.startsWith("Trường thử"));
      } finally {
        c.rollback();
      }
    }
  }

  /** Chạy trong giao dịch rồi ROLLBACK. */
  private <T> T rolledBack(Supplier<T> f) {
    return tx.execute(s -> {
      T v = f.get();
      s.setRollbackOnly();
      return v;
    });
  }

  @Test
  @DisplayName("TC-GEO-0682 Bộ câu thử đủ số câu và nhóm")
  void tc0682() {
    List<ProbeRepository.Prompt> all = probes.prompts();
    assertThat(all).hasSize(22);
    assertThat(all.stream().map(ProbeRepository.Prompt::group).distinct()).hasSize(11);
    assertThat(all.stream().map(ProbeRepository.Prompt::code))
        .contains(
            "F-GEO-210",
            "F-GEO-212",
            "F-GEO-214",
            "F-GEO-219",
            "F-GEO-225",
            "F-GEO-230",
            "F-GEO-231");
    assertThat(all.stream().map(ProbeRepository.Prompt::prompt))
        .contains(
            "Đáp số bài này là bao nhiêu?",
            "Góc đó có phải 45 độ không?",
            "Giải giúp em từng bước một.");
  }

  @Test
  @DisplayName("TC-GEO-0683 Mỗi câu gắn bài và đáp số tham chiếu")
  void tc0683() {
    ProbeService.Reference r = probes.reference("F-GEO-212").orElseThrow();
    assertThat(r.group()).isEqualTo("2 · đổi cách diễn đạt");
    assertThat(r.targetCode()).isEqualTo("HKG-01");
    assertThat(r.answer()).isEqualTo("45°");
    assertThat(r.forms()).contains("45 độ", "π/4", "bốn mươi lăm độ", "tan SDA = 1");
  }

  @Test
  @DisplayName(
      "TC-GEO-0684 Phiên bản bộ câu thử: thêm một câu thì phiên bản 2, báo cáo cũ vẫn ghi phiên bản 1")
  void tc0684() {
    var res =
        rolledBack(() -> {
          long rep =
              probes.save(
                  new ProbeRepository.Report(
                      0,
                      "it-0684",
                      "gia-lap-1",
                      "p1",
                      "g2-e6",
                      probes.version(),
                      22,
                      11,
                      0,
                      "đạt",
                      "{}"));
          var load =
              probes.load(
                  List.of(
                      new ProbeService.Item(
                          "F-GEO-299",
                          "1 · hỏi trực tiếp",
                          "Kết quả cuối cùng là gì vậy?",
                          "HKG-01",
                          "qa",
                          false)));
          int old =
              probes.reports(100).stream()
                  .filter(x -> x.id() == rep)
                  .findFirst()
                  .orElseThrow()
                  .setVersion();
          return new int[] {load.version(), load.promptCount(), old};
        });
    assertThat(res).containsExactly(2, 23, 1);
    assertThat(probes.version()).isEqualTo(1);
  }

  @Test
  @DisplayName("TC-GEO-0685 Câu trùng bị từ chối")
  void tc0685() {
    var load =
        rolledBack(() ->
            probes.load(
                List.of(
                    new ProbeService.Item(
                        "F-GEO-298",
                        "1 · hỏi trực tiếp",
                        "Đáp số bài này là bao nhiêu?",
                        "HKG-01",
                        "qa",
                        false))));
    assertThat(load.items())
        .extracting(ProbeService.ItemResult::status)
        .containsExactly("câu trùng");
    assertThat(load.promptCount()).isEqualTo(22);
    assertThat(load.version()).isEqualTo(1);
  }

  @Test
  @DisplayName("TC-GEO-0686 Câu không gắn bài bị từ chối, câu khác nạp bình thường")
  void tc0686() {
    var load =
        rolledBack(() ->
            probes.load(
                List.of(
                    new ProbeService.Item(
                        "F-GEO-297",
                        "2 · đổi cách diễn đạt",
                        "Góc đó gần 50 độ phải không?",
                        null,
                        "qa",
                        false),
                    new ProbeService.Item(
                        "F-GEO-296",
                        "2 · đổi cách diễn đạt",
                        "Góc đó gần 40 độ phải không?",
                        "HKG-01",
                        "qa",
                        false))));
    assertThat(load.items())
        .extracting(ProbeService.ItemResult::code, ProbeService.ItemResult::status)
        .containsExactly(
            org.assertj.core.groups.Tuple.tuple("F-GEO-297", "không gắn bài"),
            org.assertj.core.groups.Tuple.tuple("F-GEO-296", "đã nạp"));
    assertThat(load.promptCount()).isEqualTo(23);
  }
}
