package vn.geo.server.content.internal.dedup.repo;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;

/**
 * S2-E4 (US-S2E4-03, BR-S2E4-12): nguồn đối chiếu trùng — CHỈ ngăn "Của tôi" của chính giáo viên
 * (sổ đề solution_doc + nháp đề chưa có sổ) và "Kho chung" của tổ chức (library_problem). Không bao
 * giờ đọc đề "Của tôi" của người khác.
 */
@Repository
public class DedupRepository {

  /** Số đề tối đa lấy ở mỗi nguồn (mới nhất trước). */
  public static final int MAX_PER_SOURCE = 500;

  /** Một đề để so: kind = library | mine | draft; ref = mã hoặc tên đề hiện cho giáo viên. */
  public record Row(String kind, UUID id, String ref, String statement) {}

  private final TenantJdbc jdbc;

  public DedupRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  /** Đề trong Kho chung của tổ chức. */
  public List<Row> library() {
    return jdbc.sql(
            """
            SELECT l.id, l.title, l.statement FROM library_problem l
            WHERE l.org_id = :orgId
            ORDER BY l.accepted_at DESC, l.id DESC LIMIT :max
            """)
        .param("max", MAX_PER_SOURCE)
        .query((rs, i) ->
            new Row("library", rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3)))
        .list();
  }

  /** Sổ đề của chính giáo viên (ngăn "Của tôi", kể cả đề còn là nháp đã dựng). */
  public List<Row> mineDocs(UUID owner) {
    return jdbc.sql(
            """
            SELECT d.id, COALESCE(d.code, d.title), d.statement FROM solution_doc d
            WHERE d.org_id = :orgId AND d.owner_user_id = :owner
            ORDER BY d.created_at DESC, d.id DESC LIMIT :max
            """)
        .param("owner", owner)
        .param("max", MAX_PER_SOURCE)
        .query((rs, i) ->
            new Row("mine", rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3)))
        .list();
  }

  /** Nháp đề của chính giáo viên chưa có sổ lời giải (ví dụ nháp vừa import). */
  public List<Row> mineDrafts(UUID owner) {
    return jdbc.sql(
            """
            SELECT p.id, left(btrim(p.statement), 60), p.statement FROM problem_draft p
            WHERE p.org_id = :orgId AND p.owner_user_id = :owner AND p.doc_id IS NULL
              AND length(btrim(p.statement)) > 0
            ORDER BY p.created_at DESC, p.id DESC LIMIT :max
            """)
        .param("owner", owner)
        .param("max", MAX_PER_SOURCE)
        .query((rs, i) ->
            new Row("draft", rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3)))
        .list();
  }
}
