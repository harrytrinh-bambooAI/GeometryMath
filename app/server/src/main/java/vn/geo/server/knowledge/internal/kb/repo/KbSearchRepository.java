package vn.geo.server.knowledge.internal.kb.repo;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Truy xuất toàn văn trên mục tài liệu (US-S5E3-07, BR-S5E3-24): câu hỏi chuẩn hoá bằng {@code
 * kb_norm} (bỏ dấu, chữ thường), các từ (trừ từ dừng) nối OR thành {@code tsquery}, xếp theo {@code
 * ts_rank_cd} chuẩn hoá (cờ 1|32: chia 1 + log độ dài mục, rồi đưa về 0…1). Chỉ mục của phiên bản
 * Đang dùng thuộc tài liệu gắn cho lớp / bài còn hiệu lực tại {@code at}.
 */
@Repository
public class KbSearchRepository {

  public record Row(
      UUID sectionId,
      UUID docId,
      String docCode,
      String docTitle,
      int version,
      int ord,
      String title,
      String body,
      List<String> codes,
      double rank) {}

  private final JdbcClient jdbc;

  public KbSearchRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  /** Từ (đã chuẩn hoá, không dấu) của câu hỏi theo đúng bộ tách từ của chỉ mục. */
  public List<String> terms(String query) {
    return jdbc.sql(
            """
            SELECT DISTINCT t.lexeme FROM unnest(to_tsvector('simple', kb_norm(:q))) t
            ORDER BY t.lexeme
            LIMIT 64
            """)
        .param("q", query)
        .query(String.class)
        .list();
  }

  /** {@code tsquery} = các từ nối OR (từ đã lọc ở dịch vụ: chỉ [a-z0-9]). */
  public List<Row> search(
      UUID orgId, UUID classId, UUID assignmentId, String tsquery, Instant at, int limit) {
    return jdbc.sql(
            """
            SELECT s.id, s.doc_id, d.code, d.title AS doc_title, s.version, s.ord, s.title, s.body, s.codes,
                   ts_rank_cd(s.search_tsv, q.q, 1 | 32) AS rank
            FROM to_tsquery('simple', :tsq) AS q(q),
                 kb_section s
            JOIN kb_document_version v ON v.org_id = :orgId AND v.doc_id = s.doc_id
                                       AND v.version = s.version AND v.status = 'active'
            JOIN kb_document d ON d.org_id = :orgId AND d.id = s.doc_id
            WHERE s.org_id = :orgId AND s.search_tsv @@ q.q
              AND s.doc_id IN (
                SELECT g.doc_id FROM kb_grant g
                WHERE g.org_id = :orgId AND g.class_id = :class AND g.revoked_at IS NULL
                  AND (g.assignment_id IS NULL OR g.assignment_id = :assignment)
                  AND (g.valid_from IS NULL OR g.valid_from <= :at)
                  AND (g.valid_to IS NULL OR g.valid_to > :at))
            ORDER BY rank DESC, d.code, s.ord
            LIMIT :limit
            """)
        .param("orgId", orgId)
        .param("class", classId)
        .param("assignment", assignmentId)
        .param("tsq", tsquery)
        .param("at", Timestamp.from(at))
        .param("limit", limit)
        .query((rs, i) ->
            new Row(
                rs.getObject("id", UUID.class),
                rs.getObject("doc_id", UUID.class),
                rs.getString("code"),
                rs.getString("doc_title"),
                rs.getInt("version"),
                rs.getInt("ord"),
                rs.getString("title"),
                rs.getString("body"),
                KbRepository.codes(rs),
                rs.getDouble("rank")))
        .list();
  }
}
