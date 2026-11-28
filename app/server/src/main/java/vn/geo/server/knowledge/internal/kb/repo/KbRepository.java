package vn.geo.server.knowledge.internal.kb.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * SQL của tài liệu, phiên bản, mục và lượt tải tệp trong kho tri thức (V605; US-S5E3-01). Mọi câu
 * lệnh lọc {@code org_id}; tài liệu luôn tra theo chủ sở hữu (người khác = rỗng → NOT_FOUND).
 */
@Repository
public class KbRepository {

  public record Doc(
      UUID id,
      UUID ownerId,
      String code,
      String title,
      boolean aiAuthored,
      Instant archivedAt,
      Instant updatedAt) {}

  /** Phiên bản không kèm tệp gốc (tệp đọc riêng bằng {@link #file}). */
  public record Version(
      int version,
      String status,
      boolean wasActive,
      String sourceKind,
      String sourceLabel,
      String fileName,
      String fileMime,
      Integer fileSize,
      String contentMd,
      String contentHash,
      Instant createdAt,
      Instant activatedAt) {}

  public record Section(int ord, String title, String body, List<String> codes) {}

  public record ListRow(
      UUID id,
      String code,
      String title,
      boolean aiAuthored,
      Instant archivedAt,
      Instant updatedAt,
      Integer activeVersion,
      int latestVersion,
      String latestStatus,
      boolean everActive) {}

  public record FileRow(String fileName, String fileMime, byte[] bytes) {}

  public record Upload(UUID id, UUID ownerId, String fileName, int sizeBytes, int received) {}

  public record Duplicate(UUID docId, String code, int version) {}

  private final JdbcClient jdbc;

  public KbRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  // ---------------------------------------------------------------- tài liệu

  public Optional<Doc> doc(UUID orgId, UUID ownerId, String ref) {
    UUID id = asUuid(ref);
    return jdbc.sql(
            """
            SELECT d.id, d.owner_id, d.code, d.title, d.ai_authored, d.archived_at, d.updated_at
            FROM kb_document d
            WHERE d.org_id = :orgId AND d.owner_id = :owner
              AND (d.id = :id OR d.code = :code)
            LIMIT 1
            """)
        .param("orgId", orgId)
        .param("owner", ownerId)
        .param("id", id)
        .param("code", ref.toUpperCase())
        .query(KbRepository::doc)
        .optional();
  }

  public List<ListRow> list(UUID orgId, UUID ownerId, int limit) {
    return jdbc.sql(
            """
            SELECT d.id, d.code, d.title, d.ai_authored, d.archived_at, d.updated_at,
                   (SELECT a.version FROM kb_document_version a
                     WHERE a.org_id = :orgId AND a.doc_id = d.id AND a.status = 'active' LIMIT 1) AS active_version,
                   l.version AS latest_version, l.status AS latest_status,
                   EXISTS (SELECT 1 FROM kb_document_version w
                            WHERE w.org_id = :orgId AND w.doc_id = d.id AND w.was_active) AS ever_active
            FROM kb_document d
            JOIN LATERAL (SELECT v.version, v.status FROM kb_document_version v
                           WHERE v.org_id = :orgId AND v.doc_id = d.id
                           ORDER BY v.version DESC LIMIT 1) l ON true
            WHERE d.org_id = :orgId AND d.owner_id = :owner
            ORDER BY d.code
            LIMIT :limit
            """)
        .param("orgId", orgId)
        .param("owner", ownerId)
        .param("limit", limit)
        .query((rs, i) ->
            new ListRow(
                rs.getObject("id", UUID.class),
                rs.getString("code"),
                rs.getString("title"),
                rs.getBoolean("ai_authored"),
                instant(rs, "archived_at"),
                instant(rs, "updated_at"),
                (Integer) rs.getObject("active_version"),
                rs.getInt("latest_version"),
                rs.getString("latest_status"),
                rs.getBoolean("ever_active")))
        .list();
  }

  /** Mã TL-nn kế tiếp của giáo viên (khóa duy nhất (org, chủ, mã) chặn đua). */
  public String nextCode(UUID orgId, UUID ownerId) {
    int n =
        jdbc.sql(
                """
                SELECT COALESCE(MAX(CAST(substr(d.code, 4) AS int)), 0) FROM kb_document d
                WHERE d.org_id = :orgId AND d.owner_id = :owner
                """)
            .param("orgId", orgId)
            .param("owner", ownerId)
            .query(Integer.class)
            .single();
    return String.format("TL-%02d", n + 1);
  }

  public void insertDoc(
      UUID orgId, UUID id, UUID ownerId, String code, String title, boolean aiAuthored) {
    jdbc.sql(
            """
            INSERT INTO kb_document (id, org_id, owner_id, code, title, ai_authored)
            VALUES (:id, :orgId, :owner, :code, :title, :ai)
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("owner", ownerId)
        .param("code", code)
        .param("title", title)
        .param("ai", aiAuthored)
        .update();
  }

  public void touchDoc(UUID orgId, UUID docId, Instant now, boolean clearArchived) {
    jdbc.sql(
            """
            UPDATE kb_document SET updated_at = :now,
                   archived_at = CASE WHEN :clear THEN NULL ELSE archived_at END
            WHERE org_id = :orgId AND id = :id
            """)
        .param("orgId", orgId)
        .param("id", docId)
        .param("now", Timestamp.from(now))
        .param("clear", clearArchived)
        .update();
  }

  public void archiveDoc(UUID orgId, UUID docId, Instant now) {
    jdbc.sql(
            """
            UPDATE kb_document_version SET status = 'archived', archived_at = :now
            WHERE org_id = :orgId AND doc_id = :id AND status <> 'archived'
            """)
        .param("orgId", orgId)
        .param("id", docId)
        .param("now", Timestamp.from(now))
        .update();
    jdbc.sql(
            """
            UPDATE kb_document SET archived_at = :now, updated_at = :now
            WHERE org_id = :orgId AND id = :id
            """)
        .param("orgId", orgId)
        .param("id", docId)
        .param("now", Timestamp.from(now))
        .update();
  }

  // ---------------------------------------------------------------- phiên bản

  public List<Version> versions(UUID orgId, UUID docId) {
    return jdbc.sql(
            """
            SELECT v.version, v.status, v.was_active, v.source_kind, v.source_label, v.file_name,
                   v.file_mime, v.file_size, v.content_md, v.content_hash, v.created_at, v.activated_at
            FROM kb_document_version v
            WHERE v.org_id = :orgId AND v.doc_id = :id
            ORDER BY v.version
            """)
        .param("orgId", orgId)
        .param("id", docId)
        .query(KbRepository::version)
        .list();
  }

  public int maxVersion(UUID orgId, UUID docId) {
    return jdbc.sql(
            """
            SELECT COALESCE(MAX(v.version), 0) FROM kb_document_version v
            WHERE v.org_id = :orgId AND v.doc_id = :id
            """)
        .param("orgId", orgId)
        .param("id", docId)
        .query(Integer.class)
        .single();
  }

  public void insertVersion(
      UUID orgId,
      UUID docId,
      int version,
      String sourceKind,
      String sourceLabel,
      String fileName,
      String fileMime,
      byte[] fileBytes,
      String contentMd,
      String contentHash,
      UUID createdBy) {
    jdbc.sql(
            """
            INSERT INTO kb_document_version (org_id, doc_id, version, status, source_kind, source_label,
                   file_name, file_mime, file_size, file_bytes, content_md, content_hash, created_by)
            VALUES (:orgId, :doc, :version, 'draft', :kind, :label, :fileName, :mime, :size, :bytes,
                    :md, :hash, :by)
            """)
        .param("orgId", orgId)
        .param("doc", docId)
        .param("version", version)
        .param("kind", sourceKind)
        .param("label", sourceLabel)
        .param("fileName", fileName)
        .param("mime", fileMime)
        .param("size", fileBytes == null ? null : fileBytes.length)
        .param("bytes", fileBytes)
        .param("md", contentMd)
        .param("hash", contentHash)
        .param("by", createdBy)
        .update();
  }

  /** Sửa tại chỗ bản Nháp chưa từng dùng; 0 = bản đã rời Nháp (bất biến). */
  public int updateDraft(UUID orgId, UUID docId, int version, String contentMd, String hash) {
    return jdbc.sql(
            """
            UPDATE kb_document_version SET content_md = :md, content_hash = :hash
            WHERE org_id = :orgId AND doc_id = :doc AND version = :version
              AND status = 'draft' AND NOT was_active
            """)
        .param("orgId", orgId)
        .param("doc", docId)
        .param("version", version)
        .param("md", contentMd)
        .param("hash", hash)
        .update();
  }

  /** Đổi trạng thái có điều kiện trạng thái cũ; 1 = đã đổi. */
  public int setStatus(UUID orgId, UUID docId, int version, String from, String to, Instant now) {
    return jdbc.sql(
            """
            UPDATE kb_document_version
            SET status = :to,
                was_active = was_active OR :to = 'active',
                activated_at = CASE WHEN :to = 'active' THEN :now ELSE activated_at END,
                archived_at = CASE WHEN :to = 'archived' THEN :now ELSE archived_at END
            WHERE org_id = :orgId AND doc_id = :doc AND version = :version AND status = :from
            """)
        .param("orgId", orgId)
        .param("doc", docId)
        .param("version", version)
        .param("from", from)
        .param("to", to)
        .param("now", Timestamp.from(now))
        .update();
  }

  /** Bản Đang dùng hiện tại sang Lưu trữ (trước khi đưa bản mới lên). */
  public int archiveActive(UUID orgId, UUID docId, Instant now) {
    return jdbc.sql(
            """
            UPDATE kb_document_version SET status = 'archived', archived_at = :now
            WHERE org_id = :orgId AND doc_id = :doc AND status = 'active'
            """)
        .param("orgId", orgId)
        .param("doc", docId)
        .param("now", Timestamp.from(now))
        .update();
  }

  public Optional<FileRow> file(UUID orgId, UUID docId, int version) {
    return jdbc.sql(
            """
            SELECT v.file_name, v.file_mime, v.file_bytes FROM kb_document_version v
            WHERE v.org_id = :orgId AND v.doc_id = :doc AND v.version = :version
              AND v.file_bytes IS NOT NULL
            LIMIT 1
            """)
        .param("orgId", orgId)
        .param("doc", docId)
        .param("version", version)
        .query((rs, i) ->
            new FileRow(rs.getString("file_name"), rs.getString("file_mime"), rs.getBytes(3)))
        .optional();
  }

  /** Phiên bản cùng băm nội dung trong kho của chính giáo viên (BR-S5E3-04). */
  public Optional<Duplicate> duplicate(UUID orgId, UUID ownerId, String hash) {
    return jdbc.sql(
            """
            SELECT d.id, d.code, v.version FROM kb_document_version v
            JOIN kb_document d ON d.org_id = :orgId AND d.id = v.doc_id
            WHERE v.org_id = :orgId AND v.content_hash = :hash AND d.owner_id = :owner
            ORDER BY v.created_at LIMIT 1
            """)
        .param("orgId", orgId)
        .param("owner", ownerId)
        .param("hash", hash)
        .query((rs, i) ->
            new Duplicate(rs.getObject("id", UUID.class), rs.getString("code"), rs.getInt(3)))
        .optional();
  }

  // ---------------------------------------------------------------- mục

  public void insertSections(UUID orgId, UUID docId, int version, List<Section> sections) {
    int ord = 0;
    for (Section s : sections) {
      ord++;
      jdbc.sql(
              """
              INSERT INTO kb_section (id, org_id, doc_id, version, ord, title, body, codes)
              VALUES (:id, :orgId, :doc, :version, :ord, :title, :body, :codes)
              """)
          .param("id", vn.geo.server.common.id.Uuid7.next())
          .param("orgId", orgId)
          .param("doc", docId)
          .param("version", version)
          .param("ord", ord)
          .param("title", s.title())
          .param("body", s.body())
          .param("codes", s.codes().toArray(String[]::new))
          .update();
    }
  }

  public void deleteSections(UUID orgId, UUID docId, int version) {
    jdbc.sql(
            """
            DELETE FROM kb_section WHERE org_id = :orgId AND doc_id = :doc AND version = :version
            """)
        .param("orgId", orgId)
        .param("doc", docId)
        .param("version", version)
        .update();
  }

  public List<Section> sections(UUID orgId, UUID docId, int version) {
    return jdbc.sql(
            """
            SELECT s.ord, s.title, s.body, s.codes FROM kb_section s
            WHERE s.org_id = :orgId AND s.doc_id = :doc AND s.version = :version
            ORDER BY s.ord
            LIMIT 2000
            """)
        .param("orgId", orgId)
        .param("doc", docId)
        .param("version", version)
        .query((rs, i) ->
            new Section(rs.getInt("ord"), rs.getString("title"), rs.getString("body"), codes(rs)))
        .list();
  }

  // ---------------------------------------------------------------- tải theo đoạn

  public void insertUpload(UUID orgId, UUID id, UUID ownerId, String fileName, int size) {
    jdbc.sql(
            """
            INSERT INTO kb_upload (id, org_id, owner_id, file_name, size_bytes)
            VALUES (:id, :orgId, :owner, :name, :size)
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("owner", ownerId)
        .param("name", fileName)
        .param("size", size)
        .update();
  }

  public Optional<Upload> upload(UUID orgId, UUID ownerId, UUID id) {
    return jdbc.sql(
            """
            SELECT u.id, u.owner_id, u.file_name, u.size_bytes, COALESCE(length(u.file_tmp), 0) AS received
            FROM kb_upload u
            WHERE u.org_id = :orgId AND u.id = :id AND u.owner_id = :owner
            LIMIT 1
            """)
        .param("orgId", orgId)
        .param("id", id)
        .param("owner", ownerId)
        .query((rs, i) ->
            new Upload(
                rs.getObject("id", UUID.class),
                rs.getObject("owner_id", UUID.class),
                rs.getString("file_name"),
                rs.getInt("size_bytes"),
                rs.getInt("received")))
        .optional();
  }

  /** Nối một đoạn đúng vị trí; 0 = lệch vị trí. */
  public int appendUpload(UUID orgId, UUID id, int offset, byte[] chunk) {
    return jdbc.sql(
            """
            UPDATE kb_upload
            SET file_tmp = CASE WHEN :pos = 0 THEN :chunk ELSE file_tmp || :chunk END
            WHERE org_id = :orgId AND id = :id
              AND COALESCE(length(file_tmp), 0) = :pos
              AND COALESCE(length(file_tmp), 0) + length(:chunk) <= size_bytes
            """)
        .param("orgId", orgId)
        .param("id", id)
        .param("pos", offset)
        .param("chunk", chunk)
        .update();
  }

  public byte[] uploadBytes(UUID orgId, UUID id) {
    return jdbc.sql(
            """
            SELECT u.file_tmp FROM kb_upload u WHERE u.org_id = :orgId AND u.id = :id LIMIT 1
            """)
        .param("orgId", orgId)
        .param("id", id)
        .query((rs, i) -> rs.getBytes(1))
        .optional()
        .orElse(null);
  }

  public void deleteUpload(UUID orgId, UUID id) {
    jdbc.sql("DELETE FROM kb_upload WHERE org_id = :orgId AND id = :id")
        .param("orgId", orgId)
        .param("id", id)
        .update();
  }

  // ---------------------------------------------------------------- tiện ích

  static UUID asUuid(String s) {
    try {
      return UUID.fromString(s);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  static Instant instant(ResultSet rs, String col) throws SQLException {
    Timestamp t = rs.getTimestamp(col);
    return t == null ? null : t.toInstant();
  }

  static List<String> codes(ResultSet rs) throws SQLException {
    java.sql.Array a = rs.getArray("codes");
    return a == null ? List.of() : Arrays.asList((String[]) a.getArray());
  }

  private static Doc doc(ResultSet rs, int i) throws SQLException {
    return new Doc(
        rs.getObject("id", UUID.class),
        rs.getObject("owner_id", UUID.class),
        rs.getString("code"),
        rs.getString("title"),
        rs.getBoolean("ai_authored"),
        instant(rs, "archived_at"),
        instant(rs, "updated_at"));
  }

  private static Version version(ResultSet rs, int i) throws SQLException {
    return new Version(
        rs.getInt("version"),
        rs.getString("status"),
        rs.getBoolean("was_active"),
        rs.getString("source_kind"),
        rs.getString("source_label"),
        rs.getString("file_name"),
        rs.getString("file_mime"),
        (Integer) rs.getObject("file_size"),
        rs.getString("content_md"),
        rs.getString("content_hash"),
        instant(rs, "created_at"),
        instant(rs, "activated_at"));
  }
}
