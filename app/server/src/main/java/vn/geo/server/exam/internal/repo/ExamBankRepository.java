package vn.geo.server.exam.internal.repo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Dữ liệu đề dùng chung (không theo trường): phiên bản, ma trận, mẫu câu, ngân hàng câu
 * (US-S5E2-01). Ghi chỉ qua lệnh nạp có kiểm (ExamImportService); đọc cho form ra đề và bộ lắp đề.
 */
@Repository
public class ExamBankRepository {

  public record MatrixRow(
      String code, UUID id, String name, String kind, int grade, double minutes, String body) {}

  public record Row(String code, String hash, String body, String extra) {}

  private final JdbcClient jdbc;
  private final NamedParameterJdbcTemplate named;

  public ExamBankRepository(JdbcClient jdbc, NamedParameterJdbcTemplate named) {
    this.jdbc = jdbc;
    this.named = named;
  }

  public void upsertVersion(String version, String hash, String meta, String report) {
    jdbc.sql(
            """
            INSERT INTO exam_data_version (version, content_hash, meta, report, loaded_at)
            VALUES (:v, :h, CAST(:meta AS jsonb), CAST(:report AS jsonb), now())
            ON CONFLICT (version) DO UPDATE SET content_hash = EXCLUDED.content_hash,
              meta = EXCLUDED.meta, report = EXCLUDED.report, loaded_at = now()
            """)
        .param("v", version)
        .param("h", hash)
        .param("meta", meta)
        .param("report", report)
        .update();
  }

  /** Mã → băm nội dung của bảng dữ liệu đề (exam_matrix | exam_item_template | exam_bank_item). */
  public Map<String, String> hashes(String table) {
    String sql =
        switch (table) {
          case "exam_matrix" ->
              "SELECT code, content_hash FROM exam_matrix -- sqllint:allow=unbounded 4 ma trận";
          case "exam_item_template" ->
              "SELECT code, content_hash FROM exam_item_template -- sqllint:allow=unbounded ~110 mẫu";
          case "exam_bank_item" ->
              "SELECT code, content_hash FROM exam_bank_item -- sqllint:allow=unbounded ~1 774 câu, chỉ lệnh nạp";
          default -> throw new IllegalArgumentException(table);
        };
    Map<String, String> out = new HashMap<>();
    jdbc.sql(sql).query((rs, i) -> out.put(rs.getString(1), rs.getString(2))).list();
    return out;
  }

  public void upsertMatrices(List<Map<String, Object>> rows) {
    named.batchUpdate(
        """
        INSERT INTO exam_matrix (code, id, data_version, name, exam_kind, grade, minutes, total_points,
                                 body, content_hash)
        VALUES (:code, :id, :version, :name, :kind, :grade, :minutes, :total, CAST(:body AS jsonb), :hash)
        ON CONFLICT (code) DO UPDATE SET data_version = EXCLUDED.data_version, name = EXCLUDED.name,
          exam_kind = EXCLUDED.exam_kind, grade = EXCLUDED.grade, minutes = EXCLUDED.minutes,
          total_points = EXCLUDED.total_points, body = EXCLUDED.body, content_hash = EXCLUDED.content_hash
        """,
        params(rows));
  }

  public void upsertTemplates(List<Map<String, Object>> rows) {
    named.batchUpdate(
        """
        INSERT INTO exam_item_template (code, data_version, topic, body, content_hash)
        VALUES (:code, :version, :topic, CAST(:body AS jsonb), :hash)
        ON CONFLICT (code) DO UPDATE SET data_version = EXCLUDED.data_version, topic = EXCLUDED.topic,
          body = EXCLUDED.body, content_hash = EXCLUDED.content_hash
        """,
        params(rows));
  }

  public void upsertItems(List<Map<String, Object>> rows) {
    named.batchUpdate(
        """
        INSERT INTO exam_bank_item (code, data_version, template_code, topic, item_type, level, body,
                                    content_hash)
        VALUES (:code, :version, :template, :topic, :type, :level, CAST(:body AS jsonb), :hash)
        ON CONFLICT (code) DO UPDATE SET data_version = EXCLUDED.data_version,
          template_code = EXCLUDED.template_code, topic = EXCLUDED.topic, item_type = EXCLUDED.item_type,
          level = EXCLUDED.level, body = EXCLUDED.body, content_hash = EXCLUDED.content_hash
        """,
        params(rows));
  }

  /** Xóa bản ghi không còn trong lần nạp (câu bị loại, câu đã gỡ khỏi dữ liệu). */
  public int deleteMissing(String table, List<String> keep) {
    String sql =
        switch (table) {
          case "exam_matrix" -> "DELETE FROM exam_matrix WHERE NOT (code = ANY (:keep))";
          case "exam_item_template" ->
              "DELETE FROM exam_item_template WHERE NOT (code = ANY (:keep))";
          case "exam_bank_item" -> "DELETE FROM exam_bank_item WHERE NOT (code = ANY (:keep))";
          default -> throw new IllegalArgumentException(table);
        };
    return jdbc.sql(sql).param("keep", keep.toArray(String[]::new)).update();
  }

  /** Xóa toàn bộ dữ liệu đề (móc kiểm thử TC-S5E2-01, trong giao dịch sẽ hoàn tác). */
  public void deleteAll() {
    jdbc.sql("DELETE FROM exam_bank_item").update();
    jdbc.sql("DELETE FROM exam_item_template").update();
    jdbc.sql("DELETE FROM exam_matrix").update();
    jdbc.sql("DELETE FROM exam_data_version").update();
  }

  public Map<String, Long> counts() {
    Map<String, Long> out = new HashMap<>();
    out.put("matrices", jdbc.sql("SELECT count(*) FROM exam_matrix").query(Long.class).single());
    out.put(
        "templates",
        jdbc.sql("SELECT count(*) FROM exam_item_template").query(Long.class).single());
    out.put("items", jdbc.sql("SELECT count(*) FROM exam_bank_item").query(Long.class).single());
    return out;
  }

  public boolean itemExists(String code) {
    return jdbc.sql("SELECT EXISTS (SELECT 1 FROM exam_bank_item WHERE code = :c LIMIT 1)")
        .param("c", code)
        .query(Boolean.class)
        .single();
  }

  public List<MatrixRow> matrices() {
    return jdbc.sql(
            """
            SELECT code, id, name, exam_kind, grade, minutes, body::text FROM exam_matrix
            ORDER BY grade, minutes, code LIMIT 50
            """)
        .query((rs, i) ->
            new MatrixRow(
                rs.getString(1),
                rs.getObject(2, UUID.class),
                rs.getString(3),
                rs.getString(4),
                rs.getInt(5),
                rs.getDouble(6),
                rs.getString(7)))
        .list();
  }

  /** Toàn bộ câu ngân hàng (bộ lắp đề giữ trong bộ nhớ, nạp lại khi phiên bản dữ liệu đổi). */
  public List<String> itemBodies() {
    return jdbc.sql(
            "SELECT body::text FROM exam_bank_item ORDER BY code"
                + " -- sqllint:allow=unbounded ngân hàng ~1 774 câu, đọc một lần vào bộ nhớ")
        .query(String.class)
        .list();
  }

  public record Version(String version, String hash, String meta) {}

  public Optional<Version> latestVersion() {
    return jdbc.sql(
            """
            SELECT version, content_hash, meta::text FROM exam_data_version
            ORDER BY loaded_at DESC LIMIT 1
            """)
        .query((rs, i) -> new Version(rs.getString(1), rs.getString(2), rs.getString(3)))
        .optional();
  }

  private static MapSqlParameterSource[] params(List<Map<String, Object>> rows) {
    return rows.stream().map(MapSqlParameterSource::new).toArray(MapSqlParameterSource[]::new);
  }
}
