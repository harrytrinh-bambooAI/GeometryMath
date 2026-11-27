package vn.geo.server.content.internal.repo;

import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Câu mở sau FIX T-05: đọc version đề tự nhập còn thiếu chữ ký bản mới và ghi chữ ký đã tính lại.
 * Đi qua hàm CSDL V422 (fn_signature_stale / fn_signature_put) vì công việc chạy ngoài yêu cầu
 * (không có trường trong ngữ cảnh) và duyệt mọi trường.
 */
@Repository
public class SignatureUpgradeRepository {

  /** Một version đề cần tính lại; {@code spec} là scene_build.spec mới nhất (null = chưa dựng). */
  public record Stale(UUID orgId, UUID problemVersionId, String spec) {}

  private final JdbcClient jdbc;

  public SignatureUpgradeRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public List<Stale> stale(int version, Stale after, int limit) {
    return jdbc.sql(
            """
            SELECT org_id, problem_version_id, spec
            FROM fn_signature_stale(CAST(:v AS smallint), CAST(:afterOrg AS uuid), CAST(:afterPv AS uuid), :limit)
            """)
        .param("v", version)
        .param("afterOrg", after == null ? null : after.orgId())
        .param("afterPv", after == null ? null : after.problemVersionId())
        .param("limit", limit)
        .query((rs, i) ->
            new Stale(rs.getObject(1, UUID.class), rs.getObject(2, UUID.class), rs.getString(3)))
        .list();
  }

  /** Ghi chữ ký bản {@code version}; trả OK | SKIPPED (đã có) | PLATFORM (không ghi đề kho). */
  public String put(
      UUID orgId,
      UUID problemVersionId,
      int version,
      String solidKind,
      String paramsJson,
      String asked,
      byte[] signature) {
    return jdbc.sql(
            """
            SELECT fn_signature_put(:org, :pv, CAST(:v AS smallint), :solid, CAST(:params AS jsonb), :asked, :sig)
            """)
        .param("org", orgId)
        .param("pv", problemVersionId)
        .param("v", version)
        .param("solid", solidKind)
        .param("params", paramsJson)
        .param("asked", asked)
        .param("sig", signature)
        .query(String.class)
        .single();
  }
}
