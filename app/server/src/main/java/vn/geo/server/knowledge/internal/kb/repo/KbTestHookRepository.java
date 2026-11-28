package vn.geo.server.knowledge.internal.kb.repo;

import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * SQL của móc kiểm thử S5-E3 (chỉ profile dev/test): dựng kho thử nhiều mục (US-S5E3-07-FC2) bằng
 * một câu INSERT … SELECT generate_series.
 */
@Repository
@Profile({"dev", "test"})
public class KbTestHookRepository {

  private final JdbcClient jdbc;

  public KbTestHookRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  /** Tài liệu Đang dùng v1 có {@code n} mục chủ đề hình học không gian, gắn cả lớp. */
  public void bulk(
      UUID orgId, UUID docId, UUID owner, String code, UUID classId, int n, String hash) {
    String md = "# Kho thử " + n + " mục";
    jdbc.sql(
            """
            INSERT INTO kb_document (id, org_id, owner_id, code, title)
            VALUES (:id, :orgId, :owner, :code, :title)
            """)
        .param("id", docId)
        .param("orgId", orgId)
        .param("owner", owner)
        .param("code", code)
        .param("title", "Kho thử " + n + " mục")
        .update();
    jdbc.sql(
            """
            INSERT INTO kb_document_version (org_id, doc_id, version, status, was_active, source_kind,
                   content_md, content_hash, created_by, activated_at)
            VALUES (:orgId, :id, 1, 'active', true, 'manual', :md, :hash, :owner, now())
            """)
        .param("orgId", orgId)
        .param("id", docId)
        .param("md", md)
        .param("hash", hash)
        .param("owner", owner)
        .update();
    jdbc.sql(
            """
            INSERT INTO kb_section (id, org_id, doc_id, version, ord, title, body, codes)
            SELECT gen_random_uuid(), :orgId, :id, 1, i,
                   (ARRAY['Khoảng cách', 'Góc giữa hai mặt phẳng', 'Thể tích khối chóp', 'Hai mặt phẳng vuông góc',
                          'Đường thẳng vuông góc mặt phẳng', 'Hình lăng trụ', 'Phép chiếu vuông góc',
                          'Góc giữa đường thẳng và mặt phẳng'])[1 + i % 8] || ' · mục ' || i,
                   'Mục ' || i || ': ' ||
                   (ARRAY['khoảng cách từ điểm đến mặt phẳng bằng độ dài đoạn vuông góc',
                          'góc giữa hai mặt phẳng là góc giữa hai đường thẳng lần lượt vuông góc',
                          'thể tích khối chóp bằng một phần ba diện tích đáy nhân chiều cao',
                          'hai mặt phẳng vuông góc khi một mặt chứa đường thẳng vuông góc mặt kia',
                          'đường thẳng vuông góc mặt phẳng khi vuông góc hai đường cắt nhau trong mặt',
                          'hình lăng trụ đứng có cạnh bên vuông góc với đáy',
                          'phép chiếu vuông góc giữ tỉ số độ dài trên đường thẳng song song',
                          'góc giữa đường thẳng và mặt phẳng là góc với hình chiếu'])[1 + (i / 8) % 8]
                   || '. Ví dụ hình chóp S.ABCD đáy hình vuông cạnh a, bài số ' || i || '.',
                   CASE WHEN i % 25 = 0 THEN ARRAY['KT11-C07-CT-003'] ELSE ARRAY[]::text[] END
            FROM generate_series(1, :n) AS i
            """)
        .param("orgId", orgId)
        .param("id", docId)
        .param("n", n)
        .update();
    jdbc.sql(
            """
            INSERT INTO kb_grant (id, org_id, doc_id, class_id, created_by)
            VALUES (gen_random_uuid(), :orgId, :id, :class, :owner)
            """)
        .param("orgId", orgId)
        .param("id", docId)
        .param("class", classId)
        .param("owner", owner)
        .update();
  }
}
