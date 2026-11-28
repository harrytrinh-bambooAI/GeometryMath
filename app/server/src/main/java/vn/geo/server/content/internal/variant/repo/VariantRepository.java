package vn.geo.server.content.internal.variant.repo;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;

/** SQL của biến thể đề (S2-E9): đề gốc "Của tôi", đề mới + phiên bản 1 "AI soạn", hàng biến thể. */
@Repository
public class VariantRepository {

  /** Đề gốc của chính giáo viên. */
  public record SourceDoc(UUID id, String title, String statement, String figure) {}

  public record VariantRow(
      UUID id,
      UUID docId,
      UUID sourceDocId,
      String title,
      String statement,
      long seed,
      int ordinal,
      String params,
      String answer,
      Double answerValue,
      String answerSource,
      String machineStatus,
      String humanStatus,
      Instant createdAt) {}

  private final TenantJdbc jdbc;

  public VariantRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  private static OffsetDateTime odt(Instant t) {
    return t.atOffset(ZoneOffset.UTC);
  }

  public Optional<SourceDoc> source(UUID owner, UUID docId) {
    return jdbc.sql(
            """
            SELECT d.id, d.title, d.statement, d.figure::text
            FROM solution_doc d
            WHERE d.org_id = :orgId AND d.id = :id AND d.owner_user_id = :owner LIMIT 1
            """)
        .param("id", docId)
        .param("owner", owner)
        .query((rs, i) ->
            new SourceDoc(
                rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3), rs.getString(4)))
        .optional();
  }

  public void insertDoc(
      UUID id,
      UUID owner,
      String title,
      String statement,
      String figure,
      String answer,
      Instant at) {
    jdbc.sql(
            """
            INSERT INTO solution_doc (id, org_id, owner_user_id, title, statement, figure, stated_answer,
                                      figure_source, created_at)
            VALUES (:id, :orgId, :owner, :title, :statement, CAST(:figure AS jsonb), :answer, 'engine', :at)
            """)
        .param("id", id)
        .param("owner", owner)
        .param("title", title)
        .param("statement", statement)
        .param("figure", figure)
        .param("answer", answer)
        .param("at", odt(at))
        .update();
  }

  /** Phiên bản 1 của biến thể: nhãn người "AI soạn", nhãn máy "Cần xem lại" (BR-S2E9-06). */
  public void insertFirstVersion(UUID id, UUID doc, String stepsJson, String note, Instant at) {
    jdbc.sql(
            """
            INSERT INTO solution_version (id, org_id, doc_id, version, steps, ai_authored, author_user_id, note,
                                          human_status, created_at)
            VALUES (:id, :orgId, :doc, 1, CAST(:steps AS jsonb), true, NULL, :note, 'ai_drafted', :at)
            """)
        .param("id", id)
        .param("doc", doc)
        .param("steps", stepsJson)
        .param("note", note)
        .param("at", odt(at))
        .update();
    jdbc.sql("UPDATE solution_doc SET latest_version = 1 WHERE org_id = :orgId AND id = :doc")
        .param("doc", doc)
        .update();
  }

  public void insertVariant(
      UUID id,
      UUID source,
      UUID doc,
      UUID owner,
      long seed,
      int ordinal,
      String paramsJson,
      String answer,
      double value,
      Instant at) {
    jdbc.sql(
            """
            INSERT INTO problem_variant (id, org_id, source_doc_id, doc_id, owner_user_id, seed, ordinal, params,
                                         answer, answer_value, answer_source, created_at)
            VALUES (:id, :orgId, :source, :doc, :owner, :seed, :ordinal, CAST(:params AS jsonb), :answer, :value,
                    'engine', :at)
            """)
        .param("id", id)
        .param("source", source)
        .param("doc", doc)
        .param("owner", owner)
        .param("seed", seed)
        .param("ordinal", ordinal)
        .param("params", paramsJson)
        .param("answer", answer)
        .param("value", value)
        .param("at", odt(at))
        .update();
  }

  private static final String SELECT =
      """
      SELECT pv.id, pv.doc_id, pv.source_doc_id, d.title, d.statement, pv.seed, pv.ordinal, pv.params::text,
             pv.answer, pv.answer_value, pv.answer_source, v.machine_status::text, v.human_status::text,
             pv.created_at
      FROM problem_variant pv
      JOIN solution_doc d ON d.org_id = :orgId AND d.id = pv.doc_id
      LEFT JOIN solution_version v ON v.org_id = :orgId AND v.doc_id = d.id AND v.version = d.latest_version
      WHERE pv.org_id = :orgId AND pv.owner_user_id = :owner
      """;

  private static VariantRow row(java.sql.ResultSet rs) throws java.sql.SQLException {
    return new VariantRow(
        rs.getObject(1, UUID.class),
        rs.getObject(2, UUID.class),
        rs.getObject(3, UUID.class),
        rs.getString(4),
        rs.getString(5),
        rs.getLong(6),
        rs.getInt(7),
        rs.getString(8),
        rs.getString(9),
        (Double) rs.getObject(10),
        rs.getString(11),
        rs.getString(12),
        rs.getString(13),
        rs.getObject(14, OffsetDateTime.class).toInstant());
  }

  /** Biến thể của một đề gốc, mới nhất trước. */
  public List<VariantRow> bySource(UUID owner, UUID sourceDocId) {
    return jdbc.sql(
            SELECT
                + " AND pv.source_doc_id = :source ORDER BY pv.created_at DESC, pv.id DESC LIMIT 200")
        .param("owner", owner)
        .param("source", sourceDocId)
        .query((rs, i) -> row(rs))
        .list();
  }

  /** Hàng biến thể của một đề (đề đó là biến thể), nếu có. */
  public Optional<VariantRow> byDoc(UUID owner, UUID docId) {
    return jdbc.sql(SELECT + " AND pv.doc_id = :doc LIMIT 1")
        .param("owner", owner)
        .param("doc", docId)
        .query((rs, i) -> row(rs))
        .optional();
  }
}
