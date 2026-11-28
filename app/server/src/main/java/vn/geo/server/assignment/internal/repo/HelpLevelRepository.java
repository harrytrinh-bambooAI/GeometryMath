package vn.geo.server.assignment.internal.repo;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Mức trợ giúp AI theo bài (assignment_help_level, V655; US-S5E3-04). */
@Repository
public class HelpLevelRepository {

  public record Row(
      String targetKind,
      UUID classId,
      String helpLevel,
      String privateNote,
      List<String> knowledgeCodes) {}

  public record Target(String targetKind, UUID classId, String title) {}

  private final JdbcClient jdbc;

  public HelpLevelRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public Optional<Row> find(UUID orgId, UUID targetId) {
    return jdbc.sql(
            """
            SELECT h.target_kind, h.class_id, h.help_level, h.private_note, h.knowledge_codes
            FROM assignment_help_level h
            WHERE h.org_id = :orgId AND h.target_id = :target
            """)
        .param("orgId", orgId)
        .param("target", targetId)
        .query((rs, i) ->
            new Row(
                rs.getString("target_kind"),
                rs.getObject("class_id", UUID.class),
                rs.getString("help_level"),
                rs.getString("private_note"),
                codes(rs)))
        .optional();
  }

  /** Bài giáo viên được đặt mức (fn_help_level_target, V655): bài luyện lớp mình, bài có chấm. */
  public Optional<Target> target(UUID orgId, UUID teacherId, UUID targetId) {
    return jdbc.sql(
            "SELECT target_kind, class_id, title FROM fn_help_level_target(:orgId, :teacher, :target)")
        .param("orgId", orgId)
        .param("teacher", teacherId)
        .param("target", targetId)
        .query((rs, i) ->
            new Target(
                rs.getString("target_kind"),
                rs.getObject("class_id", UUID.class),
                rs.getString("title")))
        .optional();
  }

  public void upsert(
      UUID orgId,
      UUID targetId,
      String targetKind,
      UUID classId,
      String level,
      String privateNote,
      List<String> codes,
      UUID by) {
    jdbc.sql(
            """
            INSERT INTO assignment_help_level (org_id, target_id, target_kind, class_id, help_level,
                                               private_note, knowledge_codes, updated_by, updated_at)
            VALUES (:orgId, :target, :kind, :class, :level, :note, :codes, :by, now())
            ON CONFLICT (org_id, target_id) DO UPDATE
               SET help_level = EXCLUDED.help_level, private_note = EXCLUDED.private_note,
                   knowledge_codes = EXCLUDED.knowledge_codes, updated_by = EXCLUDED.updated_by,
                   updated_at = now()
            """)
        .param("orgId", orgId)
        .param("target", targetId)
        .param("kind", targetKind)
        .param("class", classId)
        .param("level", level)
        .param("note", privateNote)
        .param("codes", codes.toArray(String[]::new))
        .param("by", by)
        .update();
  }

  private static List<String> codes(ResultSet rs) throws SQLException {
    Array a = rs.getArray("knowledge_codes");
    return a == null ? List.of() : Arrays.asList((String[]) a.getArray());
  }
}
