package vn.geo.server.knowledge.internal.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Kho bài giảng mẫu và từ khóa (E4, US-GEO-411, 412). Nội dung hệ thống của org nền tảng: mọi
 * trường đọc được (BR-GEO-110, D-GEO-076), chỉ vai nạp nội dung ghi (LectureImporter).
 */
@Repository
public class LectureRepository {

  public record LectureRow(
      UUID id,
      String slug,
      String title,
      String body,
      String sourceKind,
      String sourceRef,
      boolean humanReviewed) {}

  public record KeywordRow(String term, String slug) {}

  private static final String COLS =
      "l.id, l.slug, l.title, l.body, l.source_kind::text, l.source_ref, l.human_reviewed";

  private final JdbcClient jdbc;

  public LectureRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  /** Mọi từ khóa (dài trước để thiết bị gạch chân cụm dài nhất). Kho mẫu có khoảng 30 từ khóa. */
  public List<KeywordRow> keywords() {
    return jdbc.sql(
            """
            SELECT k.term, l.slug
            FROM keyword k JOIN lecture l ON l.org_id = k.org_id AND l.id = k.lecture_id
            WHERE k.org_id = platform_org_id() AND l.org_id = platform_org_id()
            ORDER BY length(k.term) DESC, k.term_key LIMIT 500
            -- sqllint:allow=tenant nội dung hệ thống: org nền tảng, mọi trường đọc được (BR-GEO-110)
            """)
        .query((rs, i) -> new KeywordRow(rs.getString(1), rs.getString(2)))
        .list();
  }

  /** Q11: bấm từ khóa -> bài giảng (khớp chính xác term_key). */
  public Optional<LectureRow> byTerm(String termKey) {
    return jdbc.sql(
            "SELECT "
                + COLS
                + """

                FROM keyword k JOIN lecture l ON l.org_id = k.org_id AND l.id = k.lecture_id
                WHERE k.org_id = platform_org_id() AND l.org_id = platform_org_id() AND k.term_key = :key
                LIMIT 1
                -- sqllint:allow=tenant nội dung hệ thống: org nền tảng, mọi trường đọc được (BR-GEO-110)
                """)
        .param("key", termKey)
        .query(LectureRepository::row)
        .optional();
  }

  public Optional<LectureRow> bySlug(String slug) {
    return jdbc.sql(
            "SELECT "
                + COLS
                + """

                FROM lecture l WHERE l.org_id = platform_org_id() AND l.slug = :slug LIMIT 1
                -- sqllint:allow=tenant nội dung hệ thống: org nền tảng, mọi trường đọc được (BR-GEO-110)
                """)
        .param("slug", slug)
        .query(LectureRepository::row)
        .optional();
  }

  public Optional<LectureRow> byId(UUID id) {
    return jdbc.sql(
            "SELECT "
                + COLS
                + """

                FROM lecture l WHERE l.org_id = platform_org_id() AND l.id = :id LIMIT 1
                -- sqllint:allow=tenant nội dung hệ thống: org nền tảng, mọi trường đọc được (BR-GEO-110)
                """)
        .param("id", id)
        .query(LectureRepository::row)
        .optional();
  }

  /** Nạp một bài giảng và các từ khóa của nó (LectureImporter; lặp lại cùng slug: bỏ qua). */
  public boolean insert(
      UUID id,
      String slug,
      String title,
      String body,
      String sourceKind,
      String sourceRef,
      List<String> keywords) {
    int n =
        jdbc.sql(
                """
                INSERT INTO lecture (id, org_id, subject_id, slug, title, body, source_kind,
                                     approval_status, source_ref, human_reviewed)
                VALUES (:id, platform_org_id(), 1, :slug, :title, :body,
                        CAST(:kind AS lecture_source), 'ai_drafted', :ref, false)
                ON CONFLICT (org_id, slug) DO NOTHING
                -- sqllint:allow=tenant nội dung hệ thống: org nền tảng (vai nạp nội dung)
                """)
            .param("id", id)
            .param("slug", slug)
            .param("title", title)
            .param("body", body)
            .param("kind", sourceKind)
            .param("ref", sourceRef)
            .update();
    if (n == 0) {
      return false;
    }
    for (String k : keywords) {
      jdbc.sql(
              """
              INSERT INTO keyword (org_id, term_key, term, lecture_id)
              VALUES (platform_org_id(), :key, :term, :lecture)
              ON CONFLICT DO NOTHING
              -- sqllint:allow=tenant nội dung hệ thống: org nền tảng (vai nạp nội dung)
              """)
          .param("key", k.toLowerCase(java.util.Locale.ROOT))
          .param("term", k)
          .param("lecture", id)
          .update();
    }
    return true;
  }

  private static LectureRow row(ResultSet rs, int i) throws SQLException {
    return new LectureRow(
        rs.getObject(1, UUID.class),
        rs.getString(2),
        rs.getString(3),
        rs.getString(4),
        rs.getString(5),
        rs.getString(6),
        rs.getBoolean(7));
  }
}
