package vn.geo.server.quality;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SqlLintTest {

  private static final SqlLint.Config CFG =
      new SqlLint.Config(
          Set.of("big"),
          Set.of("big", "mine"),
          Map.of("big", "mod", "mine", "mod"),
          Set.of("secret"));

  private static List<String> rules(String sql) {
    return SqlLint.lint(sql, "mod", CFG, "X.java", 1).stream()
        .map(SqlLint.Violation::rule)
        .toList();
  }

  @Test
  void goodQueriesPass() {
    assertThat(
            rules(
                "SELECT id, title FROM big WHERE org_id = :orgId ORDER BY created_at DESC, id DESC LIMIT :n"))
        .isEmpty();
    assertThat(rules("SELECT count(*) FROM small")).isEmpty();
    assertThat(rules("INSERT INTO big (id, org_id, title) VALUES (:id, :orgId, :t)")).isEmpty();
    assertThat(rules("UPDATE big SET title = :t WHERE org_id = :orgId AND id = :id")).isEmpty();
    assertThat(rules("DELETE FROM big WHERE org_id = :orgId AND id = :id")).isEmpty();
    assertThat(rules("SELECT id FROM small WHERE name LIKE 'abc%'")).isEmpty();
  }

  @Test
  void catchesOffset() {
    assertThat(rules("SELECT id FROM small ORDER BY id LIMIT 10 OFFSET 20")).contains("NO_OFFSET");
  }

  @Test
  void catchesSelectStar() {
    assertThat(rules("SELECT * FROM small")).contains("NO_SELECT_STAR");
    assertThat(rules("SELECT s.* FROM small s")).contains("NO_SELECT_STAR");
    assertThat(rules("INSERT INTO small (a) VALUES (1) RETURNING *")).contains("NO_SELECT_STAR");
  }

  @Test
  void catchesLeadingWildcardLike() {
    assertThat(rules("SELECT id FROM small WHERE name LIKE '%abc'"))
        .contains("NO_LEADING_WILDCARD_LIKE");
    assertThat(rules("SELECT id FROM small WHERE name ILIKE '%' || :q || '%'"))
        .contains("NO_LEADING_WILDCARD_LIKE");
  }

  @Test
  void catchesMissingLimitOnLargeTable() {
    assertThat(rules("SELECT id FROM big WHERE org_id = :orgId"))
        .contains("LARGE_TABLE_NEEDS_LIMIT");
  }

  @Test
  void catchesMissingTenantFilter() {
    assertThat(rules("SELECT id FROM big LIMIT 5")).contains("TENANT_FILTER_REQUIRED");
    assertThat(rules("SELECT id FROM mine WHERE org_id = :clientOrg"))
        .contains("TENANT_FILTER_REQUIRED");
    assertThat(rules("UPDATE big SET title = :t WHERE id = :id"))
        .contains("TENANT_FILTER_REQUIRED");
    assertThat(rules("DELETE FROM big WHERE id = :id")).contains("TENANT_FILTER_REQUIRED");
    assertThat(rules("INSERT INTO big (id, title) VALUES (:id, :t)"))
        .contains("TENANT_FILTER_REQUIRED");
  }

  @Test
  void joinNeedsFilterPerTenantTable() {
    String oneFilter =
        "SELECT b.id FROM big b JOIN mine m ON m.org_id = b.org_id WHERE b.org_id = :orgId LIMIT 5";
    assertThat(rules(oneFilter)).contains("TENANT_FILTER_REQUIRED");
    String both =
        "SELECT b.id FROM big b JOIN mine m ON m.org_id = :orgId AND m.id = b.mine_id WHERE b.org_id = :orgId LIMIT 5";
    assertThat(rules(both)).isEmpty();
  }

  @Test
  void catchesCrossModuleTableAccess() {
    var v =
        SqlLint.lint("SELECT id FROM big WHERE org_id = :orgId LIMIT 1", "other", CFG, "X.java", 1);
    assertThat(v).extracting(SqlLint.Violation::rule).containsExactly("TABLE_OWNERSHIP");
  }

  @Test
  void forUpdateAndOnConflictAreNotTables() {
    assertThat(rules("SELECT id FROM small WHERE id = :id FOR UPDATE")).isEmpty();
    assertThat(rules("INSERT INTO small (id) VALUES (1) ON CONFLICT (id) DO UPDATE SET id = 2"))
        .isEmpty();
  }

  @Test
  void explicitAllowMarkerSuppressesOnlyThatRule() {
    String sql =
        "SELECT id FROM big WHERE org_id = :orgId -- sqllint:allow=large_table_needs_limit\n";
    assertThat(rules(sql)).isEmpty();
    assertThat(rules("SELECT * FROM small -- sqllint:allow=no_offset")).contains("NO_SELECT_STAR");
  }

  @Test
  void extractsTextBlocksAndConcatenatedLiteralsIgnoringComments() {
    String src =
        """
        class A {
          // "SELECT * FROM commented"
          String a = "SELECT * " + "FROM small";
          String b = \"\"\"
              SELECT id
              FROM small OFFSET 5
              \"\"\";
          String c = "hello";
        }
        """;
    var lits = SqlLint.extractLiterals(src);
    var sqls = lits.stream().filter(l -> SqlLint.looksLikeSql(l.text())).toList();
    assertThat(sqls).hasSize(2);
    assertThat(sqls.get(0).text()).isEqualTo("SELECT * FROM small");
    assertThat(sqls.get(0).line()).isEqualTo(3);
    assertThat(sqls.get(1).line()).isEqualTo(4);
  }

  @Test
  void ordinaryEnglishAndVietnameseStringsAreNotSql() {
    assertThat(SqlLint.looksLikeSql("Update failed")).isFalse();
    assertThat(SqlLint.looksLikeSql("Select an option from the list"))
        .isTrue(); // chấp nhận nhầm có lợi
    assertThat(SqlLint.looksLikeSql("Dữ liệu không hợp lệ.")).isFalse();
  }

  @Test
  void schemaPrefixIsIgnoredWhenMatchingTables() {
    assertThat(rules("SELECT id FROM geo.big LIMIT 5")).contains("TENANT_FILTER_REQUIRED");
    assertThat(rules("SELECT id FROM geo.big WHERE org_id = :orgId"))
        .contains("LARGE_TABLE_NEEDS_LIMIT");
    assertThat(
            SqlLint.lint("SELECT id FROM geo.big WHERE org_id = :orgId LIMIT 1", "x", CFG, "X", 1))
        .extracting(SqlLint.Violation::rule)
        .contains("TABLE_OWNERSHIP");
  }

  @Test
  void forbiddenAppTableIsAlwaysRejectedEvenWithAllowMarker() {
    assertThat(rules("SELECT body FROM secret WHERE id = :id")).contains("FORBIDDEN_TABLE");
    assertThat(rules("SELECT s.body FROM small x JOIN geo.secret s ON s.id = x.id"))
        .contains("FORBIDDEN_TABLE");
    assertThat(rules("SELECT body FROM secret WHERE id = :id -- sqllint:allow=forbidden_table"))
        .contains("FORBIDDEN_TABLE");
    assertThat(rules("SELECT body FROM fn_get_solution(:id)")).isEmpty();
  }

  @Test
  void allowAliasesUsedByArchitectureDocsWork() {
    assertThat(rules("SELECT id FROM big WHERE org_id = :orgId -- sqllint:allow=unbounded"))
        .isEmpty();
    assertThat(rules("SELECT id FROM mine WHERE class_code = :c -- sqllint:allow=tenant"))
        .isEmpty();
  }

  @Test
  void configRejectsUnsupportedKeysAndLoadsSupportedOnes() {
    Properties ok = new Properties();
    ok.setProperty("large.tables", "a, B");
    ok.setProperty("tenant.tables", "a");
    ok.setProperty("owner.a", "iam");
    ok.setProperty("forbidden.app.tables", "solution,verification_report");
    var cfg = SqlLint.Config.from(ok);
    assertThat(cfg.largeTables()).containsExactlyInAnyOrder("a", "b");
    assertThat(cfg.forbiddenAppTables())
        .containsExactlyInAnyOrder("solution", "verification_report");
    assertThat(cfg.owners()).containsEntry("a", "iam");

    // Khóa có hậu tố theo task (T-261126-41) gộp vào danh sách gốc
    ok.setProperty("large.tables.t18", "practice_attempt, Notification");
    ok.setProperty("tenant.tables.t31", "notification");
    var merged = SqlLint.Config.from(ok);
    assertThat(merged.largeTables())
        .containsExactlyInAnyOrder("a", "b", "practice_attempt", "notification");
    assertThat(merged.tenantTables()).containsExactlyInAnyOrder("a", "notification");

    Properties dangling = new Properties();
    dangling.setProperty("large.tables.", "a");
    assertThatThrownBy(() -> SqlLint.Config.from(dangling))
        .isInstanceOf(IllegalArgumentException.class);

    Properties bad = new Properties();
    bad.setProperty("huge.tables", "a");
    assertThatThrownBy(() -> SqlLint.Config.from(bad))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("huge.tables");
  }
}
