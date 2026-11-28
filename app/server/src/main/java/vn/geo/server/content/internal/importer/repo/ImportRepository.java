package vn.geo.server.content.internal.importer.repo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;

/**
 * S2-E4 (US-S2E4-02, 03): lượt import tệp và các bài tập đã tách (import_job, import_item; V260).
 */
@Repository
public class ImportRepository {

  /** Một lượt import. {@code received} = số byte tệp đang giữ tạm (0 sau khi tách / từ chối). */
  public record JobRow(
      UUID id,
      UUID ownerId,
      String fileName,
      String fileType,
      int sizeBytes,
      int received,
      String status,
      String error,
      String notesJson,
      int found) {}

  /** Một bài tập đã tách; {@code draftId} khác null khi đã thành nháp. */
  public record ItemRow(int idx, String title, String body, String normKey, UUID draftId) {}

  /** Nháp sinh ra từ một lần import trước của cùng bài (theo norm_key). */
  public record Origin(String normKey, UUID draftId) {}

  private final TenantJdbc jdbc;

  public ImportRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  public void insertJob(UUID id, UUID owner, String fileName, int sizeBytes) {
    jdbc.sql(
            """
            INSERT INTO import_job (id, org_id, owner_user_id, file_name, size_bytes)
            VALUES (:id, :orgId, :owner, :name, :size)
            """)
        .param("id", id)
        .param("owner", owner)
        .param("name", fileName)
        .param("size", sizeBytes)
        .update();
  }

  public Optional<JobRow> job(UUID id) {
    return jdbc.sql(
            """
            SELECT id, owner_user_id, file_name, file_type, size_bytes, COALESCE(length(file_tmp), 0), status,
                   error, notes::text, found
            FROM import_job WHERE org_id = :orgId AND id = :id
            """)
        .param("id", id)
        .query((rs, i) ->
            new JobRow(
                rs.getObject(1, UUID.class),
                rs.getObject(2, UUID.class),
                rs.getString(3),
                rs.getString(4),
                rs.getInt(5),
                rs.getInt(6),
                rs.getString(7),
                rs.getString(8),
                rs.getString(9),
                rs.getInt(10)))
        .optional();
  }

  /** Nối một đoạn tệp (offset phải bằng số byte đã nhận); 0 khi lệch offset hoặc lượt đã đóng. */
  public int appendFile(UUID id, int offset, byte[] chunk) {
    return jdbc.sql(
            """
            UPDATE import_job
            SET file_tmp = CASE WHEN :pos = 0 THEN :chunk ELSE file_tmp || :chunk END, updated_at = now()
            WHERE org_id = :orgId AND id = :id AND status = 'uploading'
              AND COALESCE(length(file_tmp), 0) = CASE WHEN :pos = 0 THEN COALESCE(length(file_tmp), 0) ELSE :pos END
            """)
        .param("id", id)
        .param("pos", offset)
        .param("chunk", chunk)
        .update();
  }

  public byte[] file(UUID id) {
    return jdbc.sql("SELECT file_tmp FROM import_job WHERE org_id = :orgId AND id = :id")
        .param("id", id)
        .query((rs, i) -> rs.getBytes(1))
        .optional()
        .orElse(null);
  }

  /** Đóng lượt: tệp tạm bị xóa (không lưu tệp gốc), ghi trạng thái, ghi chú và số bài tìm thấy. */
  public void close(
      UUID id, String status, String fileType, String error, String notesJson, int found) {
    jdbc.sql(
            """
            UPDATE import_job
            SET file_tmp = NULL, status = :status, file_type = :type, error = :error,
                notes = CAST(:notes AS jsonb), found = :found, updated_at = now()
            WHERE org_id = :orgId AND id = :id
            """)
        .param("id", id)
        .param("status", status)
        .param("type", fileType)
        .param("error", error)
        .param("notes", notesJson)
        .param("found", found)
        .update();
  }

  public void insertItem(UUID jobId, int idx, UUID owner, String title, String body, String key) {
    jdbc.sql(
            """
            INSERT INTO import_item (org_id, job_id, idx, owner_user_id, title, body, norm_key)
            VALUES (:orgId, :job, :idx, :owner, :title, :body, :key)
            """)
        .param("job", jobId)
        .param("idx", idx)
        .param("owner", owner)
        .param("title", title)
        .param("body", body)
        .param("key", key)
        .update();
  }

  public List<ItemRow> items(UUID jobId) {
    return jdbc.sql(
            """
            SELECT idx, title, body, norm_key, draft_id FROM import_item
            WHERE org_id = :orgId AND job_id = :job ORDER BY idx LIMIT 50
            """)
        .param("job", jobId)
        .query((rs, i) ->
            new ItemRow(
                rs.getInt(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getObject(5, UUID.class)))
        .list();
  }

  public void setItemDraft(UUID jobId, int idx, UUID draftId) {
    jdbc.sql(
            """
            UPDATE import_item SET draft_id = :draft
            WHERE org_id = :orgId AND job_id = :job AND idx = :idx
            """)
        .param("job", jobId)
        .param("idx", idx)
        .param("draft", draftId)
        .update();
  }

  /**
   * Nháp sinh từ các lần import TRƯỚC của chính giáo viên cho những bài có cùng norm_key (mới nhất
   * trước) — để nhận ra "bài này đã lưu, nay đã chỉnh" (US-S2E4-03-SC3).
   */
  public List<Origin> origins(UUID owner, UUID exceptJob, Collection<String> keys) {
    if (keys.isEmpty()) {
      return List.of();
    }
    return jdbc.sql(
            """
            SELECT i.norm_key, i.draft_id FROM import_item i
            WHERE i.org_id = :orgId AND i.owner_user_id = :owner AND i.draft_id IS NOT NULL
              AND i.job_id <> :job AND i.norm_key IN (:keys)
            ORDER BY i.created_at DESC LIMIT 500
            """)
        .param("owner", owner)
        .param("job", exceptJob)
        .param("keys", keys)
        .query((rs, i) -> new Origin(rs.getString(1), rs.getObject(2, UUID.class)))
        .list();
  }
}
