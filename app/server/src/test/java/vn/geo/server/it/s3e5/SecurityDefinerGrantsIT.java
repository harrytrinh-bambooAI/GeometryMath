package vn.geo.server.it.s3e5;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import vn.geo.server.it.AbstractIT;

/**
 * Dev-REV-B1 (REVIEW-T20-T32-20261127 mục 3, V418): hàm SECURITY DEFINER chạy bằng quyền chủ
 * schema, nên PUBLIC không được có quyền EXECUTE và đường tìm tên phải cố định. Ca này quét toàn bộ
 * pg_proc, nên hàm SECURITY DEFINER mới trong migration sau cũng phải REVOKE … FROM PUBLIC.
 */
class SecurityDefinerGrantsIT extends AbstractIT {

  @Autowired JdbcClient jdbc;

  @Test
  @DisplayName(
      "Dev-REV-B1 Không hàm SECURITY DEFINER nào cho PUBLIC quyền EXECUTE; mọi hàm đặt search_path cố định")
  void noPublicExecuteOnSecurityDefiner() {
    List<String> publicExec =
        jdbc.sql(
                """
                SELECT p.oid::regprocedure::text FROM pg_proc p
                  JOIN pg_namespace n ON n.oid = p.pronamespace
                 WHERE n.nspname = 'public' AND p.prosecdef
                   AND (p.proacl IS NULL
                        OR EXISTS (SELECT 1 FROM aclexplode(p.proacl) a
                                    WHERE a.grantee = 0 AND a.privilege_type = 'EXECUTE'))
                 ORDER BY 1
                """)
            .query(String.class)
            .list();
    assertThat(publicExec).as("hàm SECURITY DEFINER còn quyền EXECUTE của PUBLIC").isEmpty();

    List<String> noPath =
        jdbc.sql(
                """
                SELECT p.oid::regprocedure::text FROM pg_proc p
                  JOIN pg_namespace n ON n.oid = p.pronamespace
                 WHERE n.nspname = 'public' AND p.prosecdef
                   AND NOT EXISTS (SELECT 1 FROM unnest(coalesce(p.proconfig, '{}')) c
                                    WHERE c LIKE 'search_path=%')
                 ORDER BY 1
                """)
            .query(String.class)
            .list();
    assertThat(noPath).as("hàm SECURITY DEFINER thiếu SET search_path").isEmpty();

    long secdef =
        jdbc.sql(
                "SELECT count(*) FROM pg_proc p JOIN pg_namespace n ON n.oid = p.pronamespace"
                    + " WHERE n.nspname = 'public' AND p.prosecdef")
            .query(Long.class)
            .single();
    assertThat(secdef).as("có hàm để quét").isGreaterThan(50);
  }

  @Test
  @DisplayName(
      "Dev-REV-B1 Vai CSDL khác không gọi được fn_account_purge_rows, fn_audit_event, fn_class_join_request; geo_app vẫn gọi được hàm của mình")
  void rolesKeepOnlyTheirGrants() {
    String purgeRows = "fn_account_purge_rows(uuid,uuid)";
    String audit =
        "fn_audit_event(uuid,uuid,text,text,text,uuid,uuid,timestamptz,timestamptz,text,text)";
    String join = "fn_class_join_request(uuid,uuid,uuid,text)";
    for (String role : List.of("geo_app", "geo_ai", "geo_content", "geo_ops")) {
      assertThat(can(role, purgeRows)).as(role + " " + purgeRows).isFalse();
      assertThat(can(role, "fn_user_code_sha256(text)"))
          .as(role + " fn_user_code_sha256")
          .isFalse();
    }
    for (String role : List.of("geo_ai", "geo_content", "geo_ops")) {
      assertThat(can(role, audit)).as(role + " " + audit).isFalse();
      assertThat(can(role, join)).as(role + " " + join).isFalse();
      assertThat(can(role, "fn_account_delete(uuid,uuid,timestamptz)")).as(role).isFalse();
    }
    for (String fn :
        List.of(
            audit,
            join,
            "fn_account_delete(uuid,uuid,timestamptz)",
            "fn_account_purge(timestamptz)",
            "fn_register_code_user(uuid,uuid,text,boolean,text,text,text,bytea,text,timestamptz)",
            "fn_qa_answer(uuid,uuid,bigint)")) {
      assertThat(can("geo_app", fn)).as("geo_app " + fn).isTrue();
    }
    assertThat(can("geo_content", "fn_content_put_solution(uuid,text,jsonb,jsonb,text,text)"))
        .isTrue();
    assertThat(can("geo_app", "fn_content_put_solution(uuid,text,jsonb,jsonb,text,text)"))
        .isFalse();
  }

  private boolean can(String role, String fn) {
    return Boolean.TRUE.equals(
        jdbc.sql("SELECT has_function_privilege(:r, :f, 'EXECUTE')")
            .param("r", role)
            .param("f", fn)
            .query(Boolean.class)
            .single());
  }
}
